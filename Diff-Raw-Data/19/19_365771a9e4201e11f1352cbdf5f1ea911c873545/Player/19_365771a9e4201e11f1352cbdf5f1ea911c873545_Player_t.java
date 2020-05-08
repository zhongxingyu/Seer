 import java.io.Serializable;
 
 /**
  * Created with IntelliJ IDEA.
  * User: oscka063
  * Date: 2012-10-04
  * Time: 11:13
  * To change this template use File | Settings | File Templates.
  */
 public class Player implements Serializable{
     private int size;
     private Board myBoard;
     private int xPosition;
     private int yPosition;
     private int homeX;
     private int homeY;
 
     public Player(int yPosition, int xPosition, int boardSize, Board board) {
         this.size = size;
         this.yPosition = yPosition;
         this.xPosition = xPosition;
         this.myBoard = board;
     }
 
     public int getXPosition() {
         return xPosition;
       }
     public int getYPosition() {
         return yPosition;
     }
 
     public void setHome(int x, int y) {
         this.homeX = x;
         this.xPosition = x;
         this.homeY = y;
         this.yPosition = y;
     }
 
     public void setPosition(int x, int y) {
         xPosition = x;
         yPosition = y;
     }
 
     public void moveNorth() {
        if (yPosition != 0) {
             if (myBoard.myBoard[xPosition][yPosition].north && myBoard.myBoard[xPosition][yPosition - 1].south) {
                 yPosition--;
             }
         }
     }
     public void moveEast() {
        if (xPosition != size - 1) {
             if (myBoard.myBoard[xPosition][yPosition].east && myBoard.myBoard[xPosition + 1][yPosition].west) {
             xPosition++;
             }
         }
     }
 
     public void moveSouth() {
        if (yPosition != size - 1) {
             if (myBoard.myBoard[xPosition][yPosition].south && myBoard.myBoard[xPosition][yPosition + 1].north) {
             yPosition++;
             }
         }
     }
     public void moveWest() {
        if (xPosition != 0) {
             if (myBoard.myBoard[xPosition][yPosition].west && myBoard.myBoard[xPosition -1][yPosition].east) {
             xPosition--;
             }
         }
     }
 }
