 /**
  * Represent a cell of a tic tac toe board.
  */
 public class TicTacToeCell{
 
   private TicTacToeGame.Player playedBy;
 
   /**
    * Initialize a cell that has not been played yet.
    */
   public TicTacToeCell() {
     this.playedBy = TicTacToeGame.Player.Nobody;
   }
 
   /**
    * Determine whether it is legal to play this cell.
   * @return false if this cell has already been played.
    */
   private boolean canPlay() {
     return playedBy == TicTacToeGame.Player.Nobody;
   }
 
   /**
    * Play this cell, if it has not already been played.
    * @param player the player (X or O) making a move on this cell.
    * @throws IllegalArgumentException if the player is not X or O.
    * @return true if the cell was successfully played.
    */
   public boolean play(TicTacToeGame.Player player) {
     if (!canPlay()) {
       return false;
     }
     if (player == TicTacToeGame.Player.X || player == TicTacToeGame.Player.O) {
       playedBy = player;
       return true;
     }
     throw new IllegalArgumentException("Illegal player type: " + player);
   }
 
   /**
    * Get the player of this cell.
    * @return the player of this cell.
    */
   public TicTacToeGame.Player getPlayer() {
     return playedBy;
   }
   
   /**
    * Two TicTacToeCells are equal if they were played by the same person.
    * @param other the Object to compare this to.
    * @return true if this equals other.
    */
   public boolean equals(Object other) {
     if (!(other instanceof TicTacToeCell)) {
       return false;
     }
     return this.playedBy == ((TicTacToeCell)other).playedBy;
   }
   
   /**
    * A cell's string representation is equal to the representation of its
    *    player.
    * @return the string representation of this cell's player
    */
   public String toString() {
     return playedBy.toString();
   }
 }
