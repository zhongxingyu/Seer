 package n.queens;
 
 /**
  *
  * @author Rumal
  */
 abstract class NQueen {
 
     protected int boardSize;
     protected State currentState, nextState;
     protected int tollerenceCost;
 
     public NQueen(int boardSize, int tollrence) {
         this.boardSize = boardSize;
         this.tollerenceCost = tollrence;
     }
 
     abstract public void solve();
 
     public void show() {
         System.out.println("Total Cost of " + currentState.getCost());
         int temp = 0;
         Queen q[] = currentState.getQueens();
         boolean queen = false;
         System.out.println();
 
         for (int i = 0; i < boardSize; i++) {
             for (int j = 0; j < boardSize; j++) {
                 for (int k = 0; k < boardSize; k++) {
                     if (i == q[k].getIndexOfX() && j == q[k].getIndexOfY()) {
                         queen = true;
                         temp = k;
                         break;
 
                     }
                 }
 
                 if (queen) {
                     System.out.print("" + temp + "\t");
                     queen = false;
                 } else {
                     System.out.print("*\t");
                 }
             }
 
             System.out.println();
         }
     }
 
     protected boolean isSolvedPossition(State s) {
         if (s.getCost() <= tollerenceCost) {
             return true;
         }
         return false;
     }
 }
