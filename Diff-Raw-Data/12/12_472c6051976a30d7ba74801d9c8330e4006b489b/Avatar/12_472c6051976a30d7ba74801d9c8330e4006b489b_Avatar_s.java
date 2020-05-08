 package be.kuleuven.cs.ogp.project;
 
 import be.kuleuven.cs.som.annotate.Basic;
 
 /**
  * Represents an avatar in the game.
  *
  * @author  Frederic Hannes
  */
 public class Avatar {
 
     /**
      * The square the avatar is on.
      */
     private Square square = null;
 
     /**
      * Whether the avatar is alive or not.
      */
     private boolean alive = true;
 
     /**
      * Creates an instance of the avatar and assigns it to a square.
      *
      * @param   square
      *          The given square.
      */
     public Avatar(Square square) {
         setSquare(square);
     }
 
     /**
      * Returns the square the avatar is on.
      */
     @Basic
     public Square getSquare() {
         return square;
     }
 
     /**
      * Returns true if the avatar is alive.
      */
     @Basic
     public boolean isAlive() {
         return alive;
     }
 
     /**
      * Internal method to kill the avatar.
      */
     protected void kill() {
         this.alive = false;
         this.square = null;
     }
 
     /**
      * Sets the square the avatar is on.
      *
      * @param   square
      *          The given square.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given square is invalid.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the avatar is already on the given square.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the avatar is dead.
      * @post    The new square equals the given square.
      */
     public void setSquare(Square square) throws IllegalArgumentException {
         if (square == null)
             throw new IllegalArgumentException("Invalid square!");
         if (getSquare().equals(square))
             throw new IllegalArgumentException("Avatar already on given square!");
         if (!isAlive())
             throw new IllegalArgumentException("The avatar is dead!");
         if (this.getSquare() != null)
             this.getSquare().setAvatar(null);
         this.square = square;
         square.setAvatar(this);
     }
 
     /**
      * Moves the avatar in a certain direction.
      *
      * @param   dir
      *          The given direction.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the given direction is invalid.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if the avatar is dead.
      * @throws  IllegalArgumentException
      *          Throws an illegal argument exception if there's no square in the given direction.
      */
     public void move(Direction dir) {
         if (dir == null)
             throw new IllegalArgumentException("Invalid direction!");
         if (!this.isAlive())
             throw new IllegalArgumentException("The avatar is dead!");
         if (this.getSquare().getBorder(dir).getAdjacent() == null)
             throw new IllegalArgumentException("There's no other square in the given direction!");
         this.getSquare().setAvatar(null);
        this.getSquare().getBorder(dir).getAdjacent().getSquare().setAvatar(this);
     }
 
 }
