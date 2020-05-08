 package com.multimedia.slidepuzzel.solver;
 
 public final class PuzzleSolver {
 
    public PuzzleSolver(int [][]puzzleF) {
         int algorithm = PuzzleConfiguration.ALGORITHM_IDASTAR;
         int heuristic = PuzzleConfiguration.HEURISTIC_LC;
         byte[] tiles = null;
         int puzzleSize = 16;
         final int numOfThreads = 1;
        tiles = Utility.getRandomArray(int [][]puzzleF,puzzleSize,true);
         final int puzzleType = tiles.length == 16 ? PuzzleConfiguration.PUZZLE_15 :
                                                         PuzzleConfiguration.PUZZLE_8;
         PuzzleConfiguration.setVerbose(true);
         PuzzleConfiguration.initialize(puzzleType, algorithm, heuristic, numOfThreads);
         PuzzleConfiguration.getAlgorithm().solve(
             Utility.arrayToLong(tiles), numOfThreads);
         Utility.displayStats(tiles);
     }
 
 } 
