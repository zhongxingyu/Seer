 package net.server;
 
 import game.Board;
 import net.packet.GamePacket;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.util.ArrayList;
 
 public class TicTacToeServer {
 
     public static final int PORT = 12345;
     public static final int MAX_PLAYERS = 2;
     public static final int BOARD_SIZE = 3;
 
     private Board board;
     private ServerSocket serverSocket;
     private ArrayList<PlayerHandler> players;
 
     public boolean gameOver;
 
     public TicTacToeServer() {
         try {
             serverSocket = new ServerSocket(PORT);
             System.out.println("Tic Tac Toe game server started");
         } catch (IOException e) {
             System.out.println(e.getMessage());
         }
     }
 
     public void start() {
         while (true) {
             waitForPlayers();
             // start game
             System.out.println("Starting game...");
             startGame();
         }
 
     }
 
     private void waitForPlayers() {
         if (players == null) {
             players = new ArrayList<PlayerHandler>();
         }
         // wait for players to connect to start game.
         try {
             for (int i = 0; i < MAX_PLAYERS; i++) {
                 char piece = (i == 0) ? 'X' : 'O';
                 System.out.println("Waiting for player " + piece);
                 Socket socket = serverSocket.accept();
                 System.out.println("Player '" + piece + "' from " + socket.getInetAddress().getHostName());
                 PlayerHandler player = new PlayerHandler(socket, this, board, piece);
                 players.add(player);
             }
         } catch (IOException ex) {
             System.err.println(ex.getMessage());
         }
     }
 
     private void startGame() {
 
         int turn = 0;
         gameOver = false;
         board = new Board(BOARD_SIZE, BOARD_SIZE);
 
         // TODO clean up this logic
         while (!gameOver) {
             try {
                 PlayerHandler currentPlayer = players.get(turn % MAX_PLAYERS);
                 PlayerHandler otherPlayer = players.get((turn + 1) % MAX_PLAYERS);
                 System.out.println("Notifying player " + currentPlayer.piece + " to make move");
                 currentPlayer.output.writeObject(true);
                 System.out.println("Waiting for player " + currentPlayer.piece + " to make move");
                 GamePacket packet = (GamePacket) currentPlayer.input.readObject();
                 if (packet.getGamePiece() == currentPlayer.piece) {
                     if (isValidMove(packet)) {
                         currentPlayer.output.writeObject(false);
                         currentPlayer.output.writeObject(packet);
                         otherPlayer.output.writeObject(packet);
                         System.out.println("Valid move");
                         if (board.isWinner(packet.getGamePiece())) {
                             System.out.println("Player " + currentPlayer.piece + " wins!");
                             currentPlayer.output.writeObject("You Win Player " + currentPlayer.piece );
                             otherPlayer.output.writeObject("You lose Player " + otherPlayer.piece);
                             endGame();
                         }
                         board.display();
                         turn++;
                        if (turn == (BOARD_SIZE * BOARD_SIZE)) {
                            for (PlayerHandler p : players) {
                                p.output.writeObject("Tie Game!");
                            }
                            endGame();
                        }

                     } else {
                         System.out.println("Invalid move");
                     }
                 } else {
                     System.out.println("Expected a move from " + currentPlayer.piece);
                 }
             } catch (IOException ioex) {
                 System.err.println("Error writing to player socket");
             } catch (ClassNotFoundException cnfex) {
                 System.err.println("Invalid object type found");
             }
         }
     }
 
     private boolean isValidMove(GamePacket packet) {
         char piece = packet.getGamePiece();
         int x = packet.getxPos();
         int y = packet.getyPos();
 
         return board.insertPos(x, y, piece);
     }
 
    private void endGame() throws IOException {
        for (PlayerHandler p : players) {
            p.output.writeObject("SERVER$RESET");
        }
         gameOver = true;
         players = null;
 //        for (PlayerHandler p : players) {
 //            p.close();
 //            players.remove(p);
 //        }
     }
 
     public static void main(String[] args) {
         new TicTacToeServer().start();
     }
 }
