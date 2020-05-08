 import java.util.ArrayList;
 
 public class UndoInformation
 {
     private int score;
     private int level;
     private Shape shape;
     private ArrayList<Integer> removedRows;
 
    public UndoInformation(int currentScore, int currentLevel, Shape landedShape, ArrayList<Integer> toBeRemovedRows)
     {
         score = currentScore;
         level = currentLevel;
         shape = landedShape;
        removedRows = toBeRemovedRows;
     }
 
     public int getPreviousScore() // read-only access
     {
         return score;
     }
 
     public int getPreviousLevel()
     {
         return level;
     }
 
     public Shape getPreviousShape()
     {
         return shape;
     }
 
     public ArrayList<Integer> getRemovedRows()
     {
         return removedRows;
     }
 }
