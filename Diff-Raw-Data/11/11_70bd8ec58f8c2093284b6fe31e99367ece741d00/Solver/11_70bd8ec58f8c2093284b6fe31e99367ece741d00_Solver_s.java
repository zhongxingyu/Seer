 import java.util.LinkedList;
 
 public class Solver {
     private Board main;
     private Board twin;
     private int minMoves = -1;
     private boolean solvable = false;
     private LinkedList<Board> list = null;
     private MinPQ<PQKey> mainMinPQ;
     private MinPQ<PQKey> twinMinPQ;
     
     private class PQKey implements Comparable<PQKey>
     {
         private Board state;
         private PQKey parent;
         private int moves;
         
         
         public int compareTo(PQKey that) {
             return (this.moves + this.state.manhattan())
                     - (that.moves + that.state.manhattan());
         }
     }
     
        
     
     // find a solution to the initial board (using the A* algorithm)
     public Solver(Board initial) {
         
         if (initial == null) {
             //StdOut.println("Input is null");
             return;
         }
             
         this.main = initial;
         list = new LinkedList<Board>();
         
         if (main.isGoal()) {
             solvable = true;
             minMoves = 0;
             list.add(main);
             return;
         }
         
         twin = initial.twin();
         if (twin.isGoal()) {
             //StdOut.println("Initial twin is goal");
             solvable = false;
             return;
         }
         
 
         mainMinPQ = new MinPQ<PQKey>();
         PQKey mainPQKey = new PQKey();
         mainPQKey.state = initial;
         mainPQKey.moves = 0;
         mainPQKey.parent = null;
         mainMinPQ.insert(mainPQKey);
         
         twinMinPQ = new MinPQ<PQKey>();
         PQKey twinPQKey = new PQKey();
         twinPQKey.state = twin;
         twinPQKey.moves = 0;
         twinPQKey.parent = null;
         twinMinPQ.insert(twinPQKey);
 
         Board goal = null;
         while (!solvable) {
             
             // out of choice
             if (mainMinPQ.isEmpty()) {
                // StdOut.println("Out of choice");
                 return;
             }
                 
             
             // get the min key from PQ
             mainPQKey = mainMinPQ.delMin();
             // get the next moves
             for (Board board : mainPQKey.state.neighbors()) {
                 // find the goal
                 if (board.isGoal()) {
                     solvable = true;
                     goal = board;
                     minMoves = mainPQKey.moves + 1;
                     break;
                 }
                 
                 // check if it is duplicated with its grand-parent
                 if (mainPQKey.parent == null 
                     || (!mainPQKey.parent.state.equals(board))) {
                     PQKey newKey = new PQKey();
                     newKey.moves = mainPQKey.moves + 1;
                     newKey.state = board;
                     newKey.parent = mainPQKey;
                     mainMinPQ.insert(newKey);
                 }
            } // end of main solution
             
             // Find the next move in twin solution
             if (!solvable) {
                 twinPQKey = twinMinPQ.delMin();
                 // get the next moves
                 for (Board board : twinPQKey.state.neighbors()) {
                     // find the goal
                     if (board.isGoal()) {
                         solvable = false;
                         //StdOut.println("Twin solution found");
                         return;
                     }
                     
                     // check if it is duplicated with its grand-parent
                     if (twinPQKey.parent == null 
                         || (!twinPQKey.parent.state.equals(board))) {
                         PQKey newKey = new PQKey();
                         newKey.moves = twinPQKey.moves + 1;
                         newKey.state = board;
                         newKey.parent = twinPQKey;
                         twinMinPQ.insert(newKey);
                     }
                }
             }  // end of twin solution
         }  // end of main search
         
         list.addFirst(goal);
         list.addFirst(mainPQKey.state);
         while (mainPQKey.parent != null) {
             mainPQKey = mainPQKey.parent;
             list.addFirst(mainPQKey.state);
         }
 
         
 
     }
     
     // is the initial board solvable? 
     public boolean isSolvable() {
         return solvable;
         
     }
     
     // min number of moves to solve initial board; -1 if no solution
     public int moves() {
         return minMoves;
     }
     
     // sequence of boards in a shortest solution; null if no solution
     public Iterable<Board> solution()  {
         return list;
     }
     
     public static void main(String[] args) {
         // create initial board from file
         In in = new In(args[0]);
         int N = in.readInt();
         int[][] blocks = new int[N][N];
         for (int i = 0; i < N; i++)
             for (int j = 0; j < N; j++)
                 blocks[i][j] = in.readInt();
         Board initial = new Board(blocks);
 
         // solve the puzzle
         Solver solver = new Solver(initial);
 
         // print solution to standard output
         if (!solver.isSolvable())
             StdOut.println("No solution possible");
         else {
             StdOut.println("Minimum number of moves = " + solver.moves());
             for (Board board : solver.solution())
                 StdOut.println(board);
         }
     }
 }
