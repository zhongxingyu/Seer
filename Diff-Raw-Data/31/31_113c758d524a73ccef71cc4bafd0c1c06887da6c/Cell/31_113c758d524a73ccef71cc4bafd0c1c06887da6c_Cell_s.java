 package com.faziz.playarea;
 
 /**
  * A particular location on the play area.
  * 
  * @author faziz
  */
 public class Cell {
     private Player player = null;
     private PlayArea playArea = null;
     private int row;
     private int column;
 
     public Cell(int row, int column, PlayArea playArea) {
         this.row = row;
         this.column = column;
         this.playArea = playArea;
     }
 
     /**
      * @return the row
      */
     public int getRow() {
         return row;
     }
 
     /**
      * @return the column
      */
     public int getColumn() {
         return column;
     }
 
     /**
      * @return the player
      */
     public Player getPlayer() {
         return player;
     }
     
     public boolean isTopLeft(){
         return (row == 0 && column == 0) ? true: false;
     }
     public boolean isTopRight(){
         return (row == 0 && column == PlayArea.MATRIX_SIZE - 1) ? true: false;
     }
     public boolean isBottomLeft(){
         return (row == PlayArea.MATRIX_SIZE - 1 && column == 0) ? true: false;
     }
     public boolean isBottomRight(){
         return ( row == PlayArea.MATRIX_SIZE - 1 && column == PlayArea.MATRIX_SIZE - 1) ? true: false;
     }
     
     public boolean isLeftEdge(){
         return (column == 0 && row > 0 && row < PlayArea.MATRIX_SIZE -1) ? true: false;
     }
     
     public boolean isTopEdge(){
         return (row == 0 && column > 0 && column < PlayArea.MATRIX_SIZE -1) ? true: false;
     }
     
     public boolean isRightEdge(){
         return (column == PlayArea.MATRIX_SIZE -1 && row > 0 && row < PlayArea.MATRIX_SIZE -1) ? true: false;
     }
     
     public boolean isBottomEdge(){
         return (row == PlayArea.MATRIX_SIZE -1 && column > 0 && column < PlayArea.MATRIX_SIZE -1) ? true: false;
     }
 
     /**
      * @return the leftCell
      */
     public Cell getLeftCell() {
         return playArea.getCell(row, column -1);
     }
 
     /**
      * @return the topCell
      */
     public Cell getTopCell() {
         return playArea.getCell(row -1, column);
     }
 
     /**
      * @return the rightCell
      */
     public Cell getRightCell() {
         return playArea.getCell(row, column +1);
     }
 
     /**
      * @return the bottomCell
      */
     public Cell getBottomCell() {
         return playArea.getCell(row +1, column);
     }
 
     /**
      * @param player the player to set
      */
     public void setPlayer(Player player) {
         this.player = player;
     }
     
     public boolean isOccupied(){
         return (player == null) ? false: true;
     }
     
     public boolean isEmpty(){
         return !isOccupied();
     }
     
     public Cell getCellInDirection(MovementDirection direction){
         Cell cell = null;
         
         if( MovementDirection.LEFT == direction){
             cell = getLeftCell();
         }
         if( MovementDirection.TOP == direction){
             cell = getTopCell();
         }
         if( MovementDirection.RIGHT == direction){
             cell = getRightCell();
         }
         if( MovementDirection.BOTTOM == direction){
             cell = getBottomCell();
         }
         
         return cell;
     }
 
     @Override
     public String toString() {
         return "Row: " + row + ", Column: " + column + ", Player: " + player;
    }
    
    
 }
