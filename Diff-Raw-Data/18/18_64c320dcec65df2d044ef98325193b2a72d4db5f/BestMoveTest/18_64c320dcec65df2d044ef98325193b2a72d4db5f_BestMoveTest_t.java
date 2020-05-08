 package sf.pnr.base;
 
 import sf.pnr.io.UncloseableOutputStream;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.UndeclaredThrowableException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.ExecutionException;
 
 public class BestMoveTest {
     public static void main(final String[] args) throws IOException, ExecutionException, InterruptedException {
         final UciRunner[] players = GamePlayTest.getPlayers();
         System.out.println("Running best move test with the following engines:");
         for (UciRunner player: players) {
             System.out.println("  - " + player.getName());
         }
         final UciRunner refEngine = getReferenceEngine();
         System.out.println("against reference engine " + refEngine.getName());
         FileOutputStream debugOs = null;
         final String debugFile = System.getProperty("searchTask.debugFile");
         if (debugFile != null) {
             debugOs = new FileOutputStream(debugFile);
             final UncloseableOutputStream os = new UncloseableOutputStream(debugOs);
             for (UciRunner player: players) {
                 player.setDebugOutputStream(os, player.getName() + " ");
             }
         }
         final int depth = Integer.parseInt(System.getProperty("searchTask.depth", "8"));
         final int printInterval = Integer.parseInt(System.getProperty("searchTask.printInterval", "10"));
         final int maxScore = Integer.parseInt(System.getProperty("searchTask.maxScore", "20000"));
         final long rndSeed = Long.parseLong(System.getProperty("searchTask.rndSeed", "-1"));
 
         final List<String> testFiles = new ArrayList<String>();
         testFiles.add("pos.epd");
         testFiles.add("best7.epd");
         testFiles.add("wnperm.epd");
         testFiles.add("qtest_easy.epd");
         testFiles.add("en passant.epd");
         testFiles.add("ans.epd");
 
         new EpdProcessor().process(testFiles, new BestMoveTask(players, refEngine, depth, printInterval, maxScore), rndSeed);
 
         if (debugOs != null) {
             debugOs.close();
         }
         for (UciRunner player: players) {
             player.close();
         }
         refEngine.close();
     }
 
     public static UciRunner getReferenceEngine() throws IOException {
         final File executable = new File(System.getProperty("searchTask.referenceEngine"));
         return new UciRunner(GamePlayTest.getPlayerName(executable),
             new ExternalUciProcess(executable.getAbsolutePath()));
     }
 
     private static class BestMoveTask implements EpdProcessorTask {
 
         private final UciRunner[] engines;
         private final long[] scores;
         private final long[] scoreDiffs;
         private final long[] scoreDiffsSquared;
         private final long[] nodeCounts;
         private final long[] moveTimes;
         private final int depth;
         private final int printInterval;
         private final int maxScore;
         private int testCount;
         private long startTime;
         private int maxNameLen;
 
         private BestMoveTask(final UciRunner[] engines, final UciRunner referenceEngine, final int depth,
                              final int printInterval, final int maxScore) {
             this.maxScore = maxScore;
             this.engines = new UciRunner[engines.length + 1];
             this.engines[0] = referenceEngine;
             System.arraycopy(engines, 0, this.engines, 1, engines.length);
             this.printInterval = printInterval;
             scores = new long[this.engines.length];
             scoreDiffs = new long[this.engines.length];
             scoreDiffsSquared = new long[this.engines.length];
             nodeCounts = new long[this.engines.length];
             moveTimes = new long[this.engines.length];
             this.depth = depth;
             startTime = System.currentTimeMillis();
             maxNameLen = 0;
             for (UciRunner engine: engines) {
                 final int nameLen = engine.getName().length();
                 if (nameLen > maxNameLen) {
                     maxNameLen = nameLen;
                 }
             }
         }
 
         @Override
         public void run(final String fileName, final Board board, final Map<String, String> commands) {
             testCount++;
             compute(engines[0], board, depth);
             final String referenceBestMove = engines[0].getBestMove();
             final int referenceScore = engines[0].getScore();
             scores[0] += engines[0].getScore();
             nodeCounts[0] += engines[0].getNodeCount();
             moveTimes[0] += engines[0].getMoveTime();
             System.out.printf("Reference engine '%s' moved '%s', score: %d\r\n", engines[0].getName(),
                 referenceBestMove, referenceScore);
 
            final Map<String, Integer> scoresMap = new HashMap<String, Integer>();
            scoresMap.put(referenceBestMove, referenceScore);
             for (int i = 1, enginesLength = engines.length; i < enginesLength; i++) {
                 final UciRunner engine = engines[i];
                 compute(engine, board, depth);
                 final String bestMoveStr = engine.getBestMove();
                 final int score;
                if (scoresMap.containsKey(bestMoveStr)) {
                    score = scoresMap.get(bestMoveStr);
                 } else {
                     score = getScore(board, bestMoveStr);
                    scoresMap.put(bestMoveStr, score);
                 }
                final int scoreDiff = limitScore(score) - limitScore(referenceScore);
                 System.out.printf("  - engine '%s' moved '%s', score: %d\r\n", engine.getName(), bestMoveStr, score);
                 scores[i] += score;
                 scoreDiffs[i] += scoreDiff;
                 scoreDiffsSquared[i] += scoreDiff * scoreDiff;
                 nodeCounts[i] += engine.getNodeCount();
                 moveTimes[i] += engine.getMoveTime();
             }
 
             if (testCount % printInterval == 0) {
                 printStats();
             }
         }
 
         private void printStats() {
             System.out.printf("Statistics after %d tests (elapsed time: %2.1fs):\r\n",
                 testCount, ((double) System.currentTimeMillis() - startTime) / 1000);
             System.out.printf("%" + (maxNameLen + 4) + "s\t%7s\t%7s\t%7s\t%9s\t%6s\t%9s\r\n",
                     "Engine name", "cp", "diff", "stddev", "nodes", "ms", "nodes/sec");
             for (int i = 0; i < engines.length; i++) {
                 final UciRunner engine = engines[i];
                 final double avgScore = ((double) scores[i]) / testCount;
                 final double avgScoreDiff = ((double) scoreDiffs[i]) / testCount;
                 final double avgNodeCount = ((double) nodeCounts[i]) / testCount;
                 final double avgMoveTime = ((double) moveTimes[i]) / testCount;
                 final double stdDevScoreDiff = Math.sqrt(((double) scoreDiffsSquared[i]) / testCount - avgScoreDiff * avgScoreDiff);
                 System.out.printf("%" + (maxNameLen + 4) + "s\t%7.1f\t%7.1f\t%7.1f\t%9.1f\t%6.0f\t%9.1f\r\n",
                     engine.getName(), avgScore, avgScoreDiff, stdDevScoreDiff, avgNodeCount, avgMoveTime, (avgNodeCount * 1000) / avgMoveTime);
             }
         }
 
         private void compute(final UciRunner engine, final Board board, final int depth) {
             try {
                 engine.uciNewGame();
                 engine.position(board);
                 engine.go(depth, 0);
             } catch (IOException e) {
                 try {
                     engine.close();
                 } catch (IOException e1) {
                     // do nothing, propagate the outer exception
                     e1.printStackTrace(System.out);
                 }
                 throw new UndeclaredThrowableException(e, String.format("Engine '%s' failed on test FEN '%s'",
                     engine.getName(), StringUtils.toFen(board)));
             }
         }
 
         private int getScore(final Board board, final String moveStr) {
             final int move = StringUtils.fromLong(board, moveStr);
             final long undo = board.move(move);
             try {
                 compute(engines[0], board, depth - 1);
             } finally {
                 board.takeBack(undo);
             }
             return -engines[0].getScore();
         }
 
         private int limitScore(final int score) {
             if (score < -maxScore) {
                 return -maxScore;
             } else if (score > maxScore) {
                 return maxScore;
             } else {
                 return score;
             }
         }
 
         @Override
         public void completed() {
             if (testCount % printInterval != 0) {
                 printStats();
             }
         }
     }
 }
