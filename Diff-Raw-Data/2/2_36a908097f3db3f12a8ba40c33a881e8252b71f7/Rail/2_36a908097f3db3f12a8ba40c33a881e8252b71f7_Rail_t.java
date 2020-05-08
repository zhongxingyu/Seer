 package edu.wctc.java.demo.tictactoe.domain;
 
 /**
  * This class represents a winning combination of Tiles. To be a winner, a 
  * Rail must contain three Tile objects with the same mark ("X" or "0").
  * 
  * @author   Jim Lombardo, Lead Java Instructor, jlombardo@wctc.edu
  * @version  1.01
  */
 public class Rail {
     private Tile[] tiles;
 
     /**
      * Constructs a Rail with three tiles.
      * 
      * @param tiles - three unmarked Tile objects.
      */
     public Rail(final Tile[] tiles) {
         this.tiles = tiles;
     }
 
     public final Tile[] getTiles() {
         return tiles;
     }
     
     /**
      * Determines winning state of this rail by comparing the marks on
      * the three enclosed Tile objects. If they match, it's a winner.
      * 
      * @return the winning state of this Rail
      */
     public final boolean isWinner() {
         int xCount = 0;
         int oCount = 0;
         boolean result = false;
         
         for(Tile tile : tiles) {
             if(tile.getText().equals("X")) {
                 xCount++;
             } else if(tile.getText().equals("0")) {
                 oCount++;
             }
         }
        return xCount == 3 || oCount == 3 ? true : false;
         
     }
     
     
 }
