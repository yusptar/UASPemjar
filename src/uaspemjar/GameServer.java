
package uaspemjar;

import java.io.*;
import java.net.*;
import javax.swing.JFrame;

public class GameServer extends JFrame {

    private ServerSocket ss;
    private int numPlayers;
    private ServerSideConnection player1;
    private ServerSideConnection player2;
    private int turnsMade;
    private int maxTurns;
    private int[] values;
    private int player1ButtonNum;
    private int player2ButtonNum;

    public GameServer() {
        System.out.println("        -----Game Turn Based-----");
        System.out.println("");
        numPlayers = 0;
        turnsMade = 0;
        maxTurns = 4;
        values = new int[4];
        System.out.println("Keterangan Point");
        
        GameFrame gameFrame = new GameFrame();

        for (int i = 0; i < values.length; i++) {
            values[i] = (int) Math.ceil(Math.random() * 100);
            System.out.println("Point ke-" + (i + 1) + " : " + values[i]);
            gameFrame.update(String.valueOf(values[i]));

        }
        System.out.println("");

        try {
            ss = new ServerSocket(51734);
        } catch (IOException ex) {
            System.out.println("Error");
        }
    }

    public void acceptConnections() {
        try {
            System.out.println("Menunggu Koneksi...");
            while (numPlayers < 2) {
                Socket s = ss.accept();
                numPlayers++;
                System.out.println("Player " + numPlayers + " Berhasil Koneksi");
                ServerSideConnection ssc = new ServerSideConnection(s, numPlayers);
                if (numPlayers == 1) {
                    player1 = ssc;
                } else {
                    player2 = ssc;
                }
                Thread t = new Thread(ssc);
                t.start();
            }
            System.out.println("Kita mempunyai 2 pemain");
        } catch (IOException ex) {
            System.out.println("Error");
        }
        System.out.println("");
    }

    private class ServerSideConnection implements Runnable {

        private Socket socket;
        private DataInputStream dataIn;
        private DataOutputStream dataOut;
        private int playerID;

        public ServerSideConnection(Socket s, int id) {
            socket = s;
            playerID = id;
            try {
                dataIn = new DataInputStream(socket.getInputStream());
                dataOut = new DataOutputStream(socket.getOutputStream());
            } catch (IOException ex) {
                System.err.println("Error");
            }
        }

        public void run() {
            try {
                dataOut.writeInt(playerID);
                dataOut.writeInt(maxTurns);
                dataOut.writeInt(values[0]);
                dataOut.writeInt(values[1]);
                dataOut.writeInt(values[2]);
                dataOut.writeInt(values[3]);
                dataOut.flush();

                while (true) {
                    if (playerID == 1) {
                        player1ButtonNum = dataIn.readInt();
                        System.out.println("Player 1 pilih kotak ke- " + player1ButtonNum);
                        player2.sendButtonNum(player1ButtonNum);
                    } else {
                        player2ButtonNum = dataIn.readInt();
                        System.out.println("Player 2 pilih kotak ke- " + player2ButtonNum);
                        player1.sendButtonNum(player2ButtonNum);
                    }
                    turnsMade++;
                    if (turnsMade == maxTurns) {
                        System.out.println("Max Putaran Telah Terpenuhi");
                        break;
                    }
                }
                System.out.println("");
                player1.closeConnection();
                player2.closeConnection();
            } catch (IOException ex) {
                System.err.println("Error");
            }

        }

        public void sendButtonNum(int n) {
            try {
                dataOut.writeInt(n);
                dataOut.flush();
            } catch (IOException ex) {
                System.err.println("Error");
            }
        }

        public void closeConnection() {
            try {
                socket.close();
                System.out.println("Koneksi Selesai");
            } catch (IOException ex) {
                System.err.println("IOException Connection Closed");
            }
        }
    }

    public static void main(String[] args) {
        GameServer gs = new GameServer();
        gs.acceptConnections();
    }
}
