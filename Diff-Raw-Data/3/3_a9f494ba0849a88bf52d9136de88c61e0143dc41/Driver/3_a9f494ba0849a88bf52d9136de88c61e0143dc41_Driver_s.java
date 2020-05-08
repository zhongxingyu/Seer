 package tictactoe;
 
 import java.util.Scanner;
 
 /**
  * The driver of a TicTacToe Game. Each driver will only play one game
  * <p>
  * Can be run as a {@code Thread}.
  *
  * @author Todd Taomae
  */
 public class Driver implements Runnable
 {
     private Player playerX;
     private Player playerO;
     private Board board;
 
     /**
      * Constructs a new {@code Driver} with the specified {@code Player}s.
      *
      * @param x     the X {@code Player}.
      * @param o     the O {@code Player}.
      */
     public Driver(Player x, Player o)
     {
         this.playerX = x;
         this.playerO = o;
         this.board = new Board();
     }
 
     /**
      * Plays one match of TicTacToe on a new {@code Board}.
      * <p>
      * Notifies all threads synchronized on this object after each turn is played.
      *
      * @param   print   whether or not the status of the game should be printed.
      */
     public void playGame(boolean print)
     {
         this.board = new Board();
 
         // loop until there is a winner
         while (this.board.getWinner() == Mark.NONE) {
             int move = Board.INVALID_MOVE;
 
             if (this.board.getCurrentPlayer() == Mark.X) {
                 move = this.playerX.getMove((Board)this.board.clone());
             } else {
                 move = this.playerO.getMove((Board)this.board.clone());
             }
 
             try {
                 // notify all threads that a move has been played
                 synchronized(this) {
                     this.board.play(move);
                     this.notifyAll();
                 }
 
             } catch (IllegalMoveException e) {
                 System.err.println("Player " + this.board.getCurrentPlayer()
                                  + " has performed an illegal move!");
                // exit fromwhile loop
                break;
             }
 
             // print curent state
             if (print) {
                 System.out.println(this.board + "\n");
             }
         }
 
         // print winner
         if (print) {
             System.out.println("Winner is " + this.board.getWinner());
         }
     }
 
     /**
      * Starts the {@code playGame()} method without printing.
      */
     public void run()
     {
         this.playGame(false);
     }
 
     /**
      * Returns the winner of the current game.
      *
      * @return  the winner of the current game.
      */
     public Mark getWinner()
     {
         return this.board.getWinner();
     }
 
     /**
      * Returns the state of the current game as a 1-D array of {@code Mark}s
      * with indices corresponding to those of the {@code Board} class.
      * @return     the state of the current game.
      */
     public Mark[] getState()
     {
         Mark[] result = new Mark[9];
 
         for (int i = 0; i < 9; i++) {
             result[i] = this.board.markAt(i);
         }
 
         return result;
     }
 
     /**
      * Sets the X {@code Player} of this {@code TicTacToeDriver}
      *
      * @param   x   new X {@code Player} of this {@code TicTacToeDriver}
      */
     public void setPlayerX(Player x)
     {
         this.playerX = x;
     }
 
     /**
      * Sets the O {@code Player} of this {@code TicTacToeDriver}
      *
      * @param   o   new O {@code Player} of this {@code TicTacToeDriver}
      */
     public void setPlayerO(Player o)
     {
         this.playerO = o;
     }
 
     /**
      * Creates a new board for this driver. It is the responsibility of the user
      * ensure that the driver is not in the middle of a game.
      */
     public void newBoard()
     {
         this.board = new Board();
     }
 }
