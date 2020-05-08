 package sudoku.strategy;
 
import java.util.Stack;
 
 import sudoku.Board;
import sudoku.SquareCandidate;
 /**
  * The 'FirstAvailable' strategy always selects the first valid choice
  * it finds in any situation
  * @author neiljohnson
  *
  */
 public class FirstAvailable implements Strategy {
    Stack<SquareCandidate> suggestionStack = new Stack<SquareCandidate>();
     public void solve(Board board) {
     	long startTime= System.nanoTime();
         final int[] initialState = board.retrieveState().clone();
         int[] workingState = board.retrieveState();
         //loop through all
         //find lowest number that is sound, 
        //if find a sqaure where no sound exists
         //roll back to previous initial start as 0
         //find next sound and proceed.
         for(int i=0;i<initialState.length;i++){
            //System.out.println("i is "+i);
             boolean updatedBoard = false;
             if (initialState[i]==0){
                 for(int j=workingState[i]+1;j<=9;j++){
                     if(board.isSound(i,j)) {
                         workingState[i] = j;
                         updatedBoard=true;
                        //System.out.println("Setting "+i+" to "+j);
                         break;
                     }
                 }
                 
                 //found nothing, so must roll back
                 if(updatedBoard==false){
                     workingState[i]=0;
                     int k=1;
                     
                     while (initialState[i-k]!=0){
                         k++;
                         
                    }
                    //System.out.print("i was "+i);
                    i-=(k+1);
                    //System.out.println(" rolling i back to "+i);
                 }
             }   
         }
 
         System.out.println("First Available solve time is "+(System.nanoTime()-startTime));
     }
 
 }
