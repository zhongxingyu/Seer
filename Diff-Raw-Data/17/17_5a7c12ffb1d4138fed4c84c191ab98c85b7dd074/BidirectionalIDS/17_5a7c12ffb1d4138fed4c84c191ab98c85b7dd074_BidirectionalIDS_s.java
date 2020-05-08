 package sokoban.solvers;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import sokoban.Board;
 import sokoban.SearchInfo;
 import sokoban.SearchStatus;
 
 /**
  * This solver performs a bidirectional (TODO iterative deepening DFS?) search.
  */
 public class BidirectionalIDS implements Solver
 {
     private IDSPuller puller;
     private IDSPusher pusher;
 
     @Override
     public String solve(final Board startBoard)
     {
         HashSet<Long> failedBoardsPuller = new HashSet<Long>();
         HashSet<Long> failedBoardsPusher = new HashSet<Long>();
         HashMap<Long, BoxPosDir> pullerStatesMap = new HashMap<Long, BoxPosDir>(); 
         HashMap<Long, BoxPosDir> pusherStatesMap = new HashMap<Long, BoxPosDir>();
 
         pusher = new IDSPusher(startBoard, failedBoardsPuller, pusherStatesMap, pullerStatesMap);
         puller = new IDSPuller(startBoard, failedBoardsPusher, pullerStatesMap, pusherStatesMap);
 
         boolean runPuller = true;
         int lowerBound = IDSCommon.lowerBound(startBoard);
         // TODO implement collision check in pusher and update accordingly here (remove line)
         SearchInfo result;
                 
         // IDS loop
         boolean pullerFailed = false;
         boolean pusherFailed = false;
         for (int maxDepth = lowerBound; maxDepth < IDSCommon.DEPTH_LIMIT; maxDepth += 3) {
 
             // Puller
             if (runPuller) {
                 result = puller.dfs(maxDepth);
                 System.out.println("puller: "+result.status);
             }
             // Pusher
             else {
                 result = pusher.dfs(maxDepth);
                 System.out.println("pusher: "+result.status);
             }
             
             if (result.solution != null) {
                 System.out.println();
                 return Board.solutionToString(result.solution);
             }
             else if (result.status == SearchStatus.Failed) {
                 if (runPuller) pullerFailed = true;
                 if (!runPuller) pusherFailed = true;
                System.out.println("\nSolver failed: "+(runPuller ? "Puller" : "Pusher"));
             }
 
             if (pullerFailed && pusherFailed) {
                 System.out.println("no solution!");
                 return null;
             }
 
            // TODO: implement collision check in pusher and activate this line
            runPuller = !runPuller;
         }
 
         System.out.println("Maximum depth reached!");
         return null;
     }
 
     @Override
     public int getIterationsCount()
     {
         // TODO
         return pusher.getIterationsCount() + puller.getIterationsCount();
     }
 
 }
