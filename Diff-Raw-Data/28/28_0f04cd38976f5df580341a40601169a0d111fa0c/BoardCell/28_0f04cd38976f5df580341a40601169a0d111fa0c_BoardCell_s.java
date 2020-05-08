 package edu.victone.scrabblah.logic.game;
 
 import edu.victone.scrabblah.logic.common.Tile;
 
 /**
  * Created with IntelliJ IDEA.
  * User: vwilson
  * Date: 9/12/13
  * Time: 3:47 PM
  */
 public class BoardCell {
   private int multiplier;
   private boolean isWordMultiplier;
  private boolean used;
   private Tile tile;
 
   public BoardCell(int multiplier, boolean isWordMultiplier) {
     this.multiplier = multiplier;   //single, double or triple
     this.isWordMultiplier = isWordMultiplier; //letter or word
   }
 
   public void setTile(Tile t) {
     if (isEmpty()) {
       tile = t;
     } else {
       throw new IllegalStateException("Cell already contains Tile");
     }
   }
 
   public boolean isEmpty() {
     return tile == null;
   }
 
  public boolean isUsed() {
    return used;
   }
 
  public void setUsed(boolean used) {
    this.used = used;
   }
 
   public int getMultiplier() {
     return multiplier;
   }
 
   public boolean isWordMultiplier() {
     return isWordMultiplier;
   }
 
   public Tile getTile() {
     return tile;
   }
 
   @Override
   public String toString() {
     String output = "";
     switch (multiplier) {
       case 1:
         output = "  ";
         break;
       case 2:
         output = (isWordMultiplier ? "DW" : "DL");
         break;
       case 3:
         output = (isWordMultiplier ? "TW" : "TL");
         break;
     }
     return output;
   }
 }
