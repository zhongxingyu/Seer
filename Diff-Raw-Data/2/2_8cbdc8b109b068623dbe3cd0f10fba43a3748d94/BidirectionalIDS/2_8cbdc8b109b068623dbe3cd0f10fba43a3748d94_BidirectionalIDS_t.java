 package sokoban.solvers;
 
 import java.util.HashMap;
 import java.util.HashSet;
 
 import sokoban.Board;
 import sokoban.SearchInfo;
 import sokoban.SearchStatus;
 
 /**
  * This solver performs a bidirectional search using the IDSPusher and IDSPuller.
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
 
         pusher = new IDSPusher(startBoard, failedBoardsPuller, pusherStatesMap,
                 pullerStatesMap);
         puller = new IDSPuller(startBoard, failedBoardsPusher, pullerStatesMap,
                 pusherStatesMap);
 
         boolean runPuller = true;
         int lowerBound = IDSCommon.lowerBound(startBoard);
         SearchInfo result;
 
         // IDS loop
         boolean pullerFailed = false;
         boolean pusherFailed = false;
 
         int pullerDepth = lowerBound;
         int pusherDepth = lowerBound;
 
         while (true) {
             result = SearchInfo.Failed;
 
             // Puller
             if (runPuller && pullerDepth < IDSCommon.DEPTH_LIMIT) {
                 System.out.print("puller (depth " + pullerDepth + "): ");
                 result = puller.dfs(pullerDepth);
                 pullerDepth = puller.nextDepth(lowerBound);
                 System.out.println(result.status);
             }
 
             // Pusher
             if (!runPuller && pullerDepth < IDSCommon.DEPTH_LIMIT) {
                 System.out.print("pusher (depth " + pusherDepth + "): ");
                 result = pusher.dfs(pusherDepth);
                 pusherDepth = pusher.nextDepth(lowerBound);
                 System.out.println(result.status);
             }
 
             if (result.solution != null) {
                 System.out.println();
                 return Board.solutionToString(result.solution);
             }
            else if (pusherDepth >= IDSCommon.DEPTH_LIMIT
                     && pullerDepth >= IDSCommon.DEPTH_LIMIT) {
                 System.out.println("Maximum depth reached!");
                 return null;
             }
             else if (result.status == SearchStatus.Failed) {
                 if (runPuller)
                     pullerFailed = true;
                 if (!runPuller)
                     pusherFailed = true;
             }
 
             if (pullerFailed && pusherFailed) {
                 System.out.println("no solution!");
                 return null;
             }
 
             // Run the other solver if only one of them failed
             // in case it failed because of a bug or hash collision
             if (pullerFailed) {
                 runPuller = false;
             }
             else if (pusherFailed) {
                 runPuller = true;
             }
             else if (runPuller && 2*pusher.numLeafNodes < puller.numLeafNodes) {
                 runPuller = false;
             }
             else {
                 runPuller = true;
             }
         }
     }
 
     @Override
     public int getIterationsCount()
     {
         // TODO
         return pusher.getIterationsCount() + puller.getIterationsCount();
     }
 
 }
