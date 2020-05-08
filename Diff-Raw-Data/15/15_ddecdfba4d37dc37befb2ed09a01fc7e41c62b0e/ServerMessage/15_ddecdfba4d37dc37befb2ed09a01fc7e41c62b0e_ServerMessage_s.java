 package iago;
 
 import iago.players.Player.PlayerType;
 
 import java.awt.Point;
 import java.io.DataInputStream;
 import java.io.IOException;
 
 public class ServerMessage {
     
     private enum Status {
         GIVE_MOVE, NO_MOVE, GAME_END, ABORT;
         
         private static Status fromInteger(int s) {
             switch (s) {
             case 0:
                 return GIVE_MOVE;
             case 1:
                 return NO_MOVE;
             case 2:
                 return GAME_END;
             case 3:
                 return ABORT;
             }
             return null;
         }
     }
     
     private Status status;
     private long timeRemaining;
    private Point opponentMove;
     private int winner;
     private byte[] boardArray;
     
     public ServerMessage() {
         this.boardArray = new byte[Board.BOARD_SIZE*Board.BOARD_SIZE];
     }
     
     public void receive(DataInputStream pipe) throws IOException {
         this.status = Status.fromInteger(pipe.readInt());
         
         // reading integer into long to prevent truncation
         // TODO is this actually a genuine concern?
         this.timeRemaining = pipe.readInt();
         
         int x = pipe.readInt();
         int y = pipe.readInt();
        this.opponentMove = new Point(x, y);
         
         this.winner = pipe.readInt();
         
         if (pipe.read(boardArray) != Board.BOARD_SIZE*Board.BOARD_SIZE) {
             throw new IOException();
         }
     }
 
     public boolean gameAborted() {
         return (this.status == Status.ABORT);
     }
 
     public boolean gameHasEnded() {
         return (this.status == Status.GAME_END);
     }
 
     public boolean cantMakeMove() {
         return (this.status == Status.NO_MOVE);
     }
 
     public PlayerType getWinner() {
         // Communications messages use inconsistent values
         // side == 0 <-> white
         // but
         // winner == 1 <-> white
         switch (this.winner){
         case 1:
             return PlayerType.WHITE;
         case 2:
             return PlayerType.BLACK;
         }
         return PlayerType.NONE;
     }
     
     public byte[] getBoardArray() {
         return this.boardArray.clone();
     }
 }
