 package quoridor;
 
 import java.util.HashSet;
 
 public class BoardImpl implements Board {
     
     protected Pair <Pawn> pawns;
     protected HashSet <Wall> walls;
 
     //bec can you write this please
    @Override 
     public String toString() {
         return null;
     }
 
     //shotgun --stevebob
     public boolean pathToGoal(Pawn pawn) {
         //make sure the pawn is on the board
         assert(pawns.contains(pawn));  
 
         return false;
     }
}
