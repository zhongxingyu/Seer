 import java.awt.Image;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 
 public abstract class Piece {
   public static final int BLACK = 1;
   public static final int WHITE = 2;
   protected int _color;
   protected boolean _hasMoved;
   Image _img;
   
   public Piece(int color) throws IOException {
     _color = color;
     _img = ImageIO.read(new File(getIconFilename()));
   }
   
   public boolean validMove(int currentX, int currentY, int requestedX, int requestedY, Board board) {
     
     int i;
     ArrayList<int[]> moves = allPossibleMoves(currentX, currentY, board);
     
     for (i = 0; i < moves.size(); i++) {
       if (requestedX == moves.get(i)[0] && requestedY == moves.get(i)[1]) {
         return true;
       }
     }
     
     return false;
   }
   
   // returns an ArrayList of coordinates. Coordinates are 2 length int arrays.
   protected abstract ArrayList<int[]> allPossibleMoves(int currentX, int currentY, Board board);
 
   public int getColor() {
     return _color;
   }
   
   public void setMoved(boolean moved) {
     _hasMoved = moved;
     
   }
   
   public Image getImage() {
     return _img;
   }
   
   private String getIconFilename() {
     String result = "";
     if (getColor() == Piece.BLACK) {
       result += "b";
     } else if (getColor() == Piece.WHITE) {
       result += "w";
     }
     
     result += this.toString().toLowerCase();
     
    return "../img/" + result + ".png";
   }
   
 }
