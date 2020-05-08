 import java.util.ArrayList;
 
 public class UndoInformation
 {
     private int score;
     private int level;
     private Shape shape;
     private ArrayList<Integer> removedRows;
 
    public UndoInformation(int currentScore, int currentLevel, Shape landedShape)
     {
         score = currentScore;
         level = currentLevel;
         shape = landedShape;
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
 
    public void setRemovedRows(ArrayList<Integer> newlyRemovedRows)
    {
        if(removedRows == null)
        {
            removedRows = newlyRemovedRows;
        }
        else
        {
            throw new RuntimeException("Removed rows being set more than once!");
        }
    }

     public ArrayList<Integer> getRemovedRows()
     {
         return removedRows;
     }
 }
