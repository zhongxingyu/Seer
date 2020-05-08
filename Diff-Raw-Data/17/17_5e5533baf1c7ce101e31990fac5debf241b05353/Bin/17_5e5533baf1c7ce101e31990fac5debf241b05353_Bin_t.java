 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package firstfitdecreasing;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * Represents a bin in a single dimension bin packing problem.
  *
  * @author Marcel
  */
 class Bin {
 
     private final int capacity;
     private final List<Integer> pieces;
     private int usedCapacity;
 
     public Bin(int capacity) {
         assert(capacity > 0);
         this.capacity = capacity;
         this.pieces = new ArrayList<>();
     }
 
     /**
      * Adds a piece of size [piece] to the Bin
      *
      * @param piece size to add
      */
     public void addPiece(int piece) {
         if (piece > this.getFreeCapacity()) {
             throw new IllegalArgumentException(
                     "Piece is too big for this bin's free capacity.");
         }
 
         this.usedCapacity += piece;
         this.pieces.add(piece);
     }
 
     /**
      * Gets the free capacity in this bin.
      *
      * @return the free capacity
      */
     public int getFreeCapacity() {
         return this.capacity - this.usedCapacity;
     }
 
     /**
      * Gets the pieces within this Bin.
      *
      * @return the pieces in this bin.
      */
     public Integer[] getPieces() {
         return this.pieces.toArray(new Integer[]{});
     }
 
     /**
      * Formats the output of this bin, displaying it's pieces.
      *
      * @return the Bin in formatted text
      */
     @Override
     public String toString() {
         String msg = "{\n";
         msg += "  freeCapacity: " + this.getFreeCapacity() + ",\n";
         msg += "  pieces: [ ";
         for (int i = 0; i < this.pieces.size(); i++) {
             msg += "" + pieces.get(i);
             if (i < this.pieces.size() - 1) {
                 msg += ", ";
            } else {
                msg += " ";
             }
         }
         
         msg += "]\n";
         
         return msg + "}";
     }
 }
