 package com.intetics;
 
 import java.math.BigDecimal;
 import java.util.*;
 
 import static org.springframework.util.Assert.isTrue;
 import static org.springframework.util.Assert.notNull;
 
 public class HexicSolver {
 
     public final static Integer NUMBER_OF_CELLS = 85;
     public final static Integer COLOR_INDEX = 6;
     public final static Integer BOTTOM_CELL_INDEX = 3;
     public final static Integer MIN_CELLS_IN_CLUSTER = 3;
     public final static Integer NUMBER_OF_COLORS = 9;
     public final static Integer NO_COLOR = -1;
     public final static Integer NO_CELL = -1;
 
     private final static Random RANDOM = new Random(System.nanoTime());
 
     private final static String BOARD_TEMPLATE = "   _   _   _   _   _\n" +
             " _/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "/%s\\_/%s\\_/%s\\_/%s\\_/%s\\_/\n" +
             "\\_/%s\\_/%s\\_/%s\\_/%s\\_/%s\\\n" +
             "  \\_/ \\_/ \\_/ \\_/ \\_/";
 
     //Links section. Value - 0-based number of connected cell. -1 means 'no connections for that direction'.
     //[0] - link #0
     //[1] - link #1
     //[2] - link #2
     //[3] - link #3
     //[4] - link #4
     //[5] - link #5
     //[6] - color. -1 means 'no color'
     private int[][] cells = new int[][]{
             // 0   1   2   3   4   5   6
             { -1, -1,  6, 10,  5, -1, -1}, // 0
             { -1, -1,  7, 11,  6, -1, -1}, // 1
             { -1, -1,  8, 12,  7, -1, -1}, // 2
             { -1, -1,  9, 13,  8, -1, -1}, // 3
             { -1, -1, -1, 14,  9, -1, -1}, // 4
             { -1,  0, 10, 15, -1, -1, -1}, // 5
             { -1,  1, 11, 16, 10,  0, -1}, // 6
             { -1,  2, 12, 17, 11,  1, -1}, // 7
             { -1,  3, 13, 18, 12,  2, -1}, // 8
             { -1,  4, 14, 19, 13,  3, -1}, // 9
             {  0,  6, 16, 20, 15,  5, -1}, // 10
             {  1,  7, 17, 21, 16,  6, -1}, // 11
             {  2,  8, 18, 22, 17,  7, -1}, // 12
             {  3,  9, 19, 23, 18,  8, -1}, // 13
             {  4, -1, -1, 24, 19,  9, -1}, // 14
             {  5, 10, 20, 25, -1, -1, -1}, // 15
             {  6, 11, 21, 26, 20, 10, -1}, // 16
             {  7, 12, 22, 27, 21, 11, -1}, // 17
             {  8, 13, 23, 28, 22, 12, -1}, // 18
             {  9, 14, 24, 29, 23, 13, -1}, // 19
             { 10, 16, 26, 30, 25, 15, -1}, // 20
             { 11, 17, 27, 31, 26, 16, -1}, // 21
             { 12, 18, 28, 32, 27, 17, -1}, // 22
             { 13, 19, 29, 33, 28, 18, -1}, // 23
             { 14, -1, -1, 34, 29, 19, -1}, // 24
             { 15, 20, 30, 35, -1, -1, -1}, // 25
             { 16, 21, 31, 36, 30, 20, -1}, // 26
             { 17, 22, 32, 37, 31, 21, -1}, // 27
             { 18, 23, 33, 38, 32, 22, -1}, // 28
             { 19, 24, 34, 39, 33, 23, -1}, // 29
             { 20, 26, 36, 40, 35, 25, -1}, // 30
             { 21, 27, 37, 41, 36, 26, -1}, // 31
             { 22, 28, 38, 42, 37, 27, -1}, // 32
             { 23, 29, 39, 43, 38, 28, -1}, // 33
             { 24, -1, -1, 44, 39, 29, -1}, // 34
             { 25, 30, 40, 45, -1, -1, -1}, // 35
             { 26, 31, 41, 46, 40, 30, -1}, // 36
             { 27, 32, 42, 47, 41, 31, -1}, // 37
             { 28, 33, 43, 48, 42, 32, -1}, // 38
             { 29, 34, 44, 49, 43, 33, -1}, // 39
             { 30, 36, 46, 50, 45, 35, -1}, // 40
             { 31, 37, 47, 51, 46, 36, -1}, // 41
             { 32, 38, 48, 52, 47, 37, -1}, // 42
             { 33, 39, 49, 53, 48, 38, -1}, // 43
             { 34, -1, -1, 54, 49, 39, -1}, // 44
             { 35, 40, 50, 55, -1, -1, -1}, // 45
             { 36, 41, 51, 56, 50, 40, -1}, // 46
             { 37, 42, 52, 57, 51, 41, -1}, // 47
             { 38, 43, 53, 58, 52, 42, -1}, // 48
             { 39, 44, 54, 59, 53, 43, -1}, // 49
             { 40, 46, 56, 60, 55, 45, -1}, // 50
             { 41, 47, 57, 61, 56, 46, -1}, // 51
             { 42, 48, 58, 62, 57, 47, -1}, // 52
             { 43, 49, 59, 63, 58, 48, -1}, // 53
             { 44, -1, -1, 64, 59, 49, -1}, // 54
             { 45, 50, 60, 65, -1, -1, -1}, // 55
             { 46, 51, 61, 66, 60, 50, -1}, // 56
             { 47, 52, 62, 67, 61, 51, -1}, // 57
             { 48, 53, 63, 68, 62, 52, -1}, // 58
             { 49, 54, 64, 69, 63, 53, -1}, // 59
             { 50, 56, 66, 70, 65, 55, -1}, // 60
             { 51, 57, 67, 71, 66, 56, -1}, // 61
             { 52, 58, 68, 72, 67, 57, -1}, // 62
             { 53, 59, 69, 73, 68, 58, -1}, // 63
             { 54, -1, -1, 74, 69, 59, -1}, // 64
             { 55, 60, 70, 75, -1, -1, -1}, // 65
             { 56, 61, 71, 76, 70, 60, -1}, // 66
             { 57, 62, 72, 77, 71, 61, -1}, // 67
             { 58, 63, 73, 78, 72, 62, -1}, // 68
             { 59, 64, 74, 79, 73, 63, -1}, // 69
             { 60, 66, 76, 80, 75, 65, -1}, // 70
             { 61, 67, 77, 81, 76, 66, -1}, // 71
             { 62, 68, 78, 82, 77, 67, -1}, // 72
             { 63, 69, 79, 83, 78, 68, -1}, // 73
             { 64, -1, -1, 84, 79, 69, -1}, // 74
             { 65, 70, 80, -1, -1, -1, -1}, // 75
             { 66, 71, 81, -1, 80, 70, -1}, // 76
             { 67, 72, 82, -1, 81, 71, -1}, // 77
             { 68, 73, 83, -1, 82, 72, -1}, // 78
             { 69, 74, 84, -1, 83, 73, -1}, // 79
             { 70, 76, -1, -1, -1, 75, -1}, // 80
             { 71, 77, -1, -1, -1, 76, -1}, // 81
             { 72, 78, -1, -1, -1, 77, -1}, // 82
             { 73, 79, -1, -1, -1, 78, -1}, // 83
             { 74, -1, -1, -1, -1, 79, -1}, // 84
     };
 
     // Rotation points
     // [0] - cell #1
     // [1] - cell #2
     // [3] - cell #3
     private final static int[][] ROTATION_POINTS = new int[][] {
             {  0, 10,  5}, {  0,  6, 10}, {  1, 11,  6}, {  1,  7, 11}, {  2, 12,  7}, {  2,  8, 12}, {  3, 13,  8}, {  3,  9, 13}, {  4, 14,  9},
             {  5, 10, 15}, {  6, 16, 10}, {  6, 11, 16}, {  7, 17, 11}, {  7, 12, 17}, {  8, 18, 12}, {  8, 13, 18}, {  9, 19, 13}, {  9, 14, 19},
             { 10, 20, 15}, { 10, 16, 20}, { 11, 21, 16}, { 11, 17, 21}, { 12, 22, 17}, { 12, 18, 22}, { 13, 23, 18}, { 13, 19, 23}, { 14, 24, 19},
             { 15, 20, 25}, { 16, 26, 20}, { 16, 21, 26}, { 17, 27, 21}, { 17, 22, 27}, { 18, 28, 22}, { 18, 23, 28}, { 19, 29, 23}, { 19, 24, 29},
             { 20, 30, 25}, { 20, 26, 30}, { 21, 31, 26}, { 21, 27, 31}, { 22, 32, 27}, { 22, 28, 32}, { 23, 33, 28}, { 23, 29, 33}, { 24, 34, 29},
             { 25, 30, 35}, { 26, 36, 30}, { 26, 31, 36}, { 27, 37, 31}, { 27, 32, 37}, { 28, 38, 32}, { 28, 33, 38}, { 29, 39, 33}, { 29, 34, 39},
             { 30, 40, 35}, { 30, 36, 40}, { 31, 41, 36}, { 31, 37, 41}, { 32, 42, 37}, { 32, 38, 42}, { 33, 43, 38}, { 33, 39, 43}, { 34, 44, 39},
             { 35, 40, 45}, { 36, 46, 40}, { 36, 41, 46}, { 37, 47, 41}, { 37, 42, 47}, { 38, 48, 42}, { 38, 43, 48}, { 39, 49, 43}, { 39, 44, 49},
             { 40, 50, 45}, { 40, 46, 50}, { 41, 51, 46}, { 41, 47, 51}, { 42, 52, 47}, { 42, 48, 52}, { 43, 53, 48}, { 43, 49, 53}, { 44, 54, 49},
             { 45, 50, 55}, { 46, 56, 50}, { 46, 51, 56}, { 47, 57, 51}, { 47, 52, 57}, { 48, 58, 52}, { 48, 53, 58}, { 49, 59, 53}, { 49, 54, 59},
             { 50, 60, 55}, { 50, 56, 60}, { 51, 61, 56}, { 51, 57, 61}, { 52, 62, 57}, { 52, 58, 62}, { 53, 63, 58}, { 53, 59, 63}, { 54, 64, 59},
             { 55, 60, 65}, { 56, 66, 60}, { 56, 61, 66}, { 57, 67, 61}, { 57, 62, 67}, { 58, 68, 62}, { 58, 63, 68}, { 59, 69, 63}, { 59, 64, 69},
             { 60, 70, 65}, { 60, 66, 70}, { 61, 71, 66}, { 61, 67, 71}, { 62, 72, 67}, { 62, 68, 72}, { 63, 73, 68}, { 63, 69, 73}, { 64, 74, 69},
            { 65, 70, 75}, { 66, 76, 70}, { 66, 71, 76}, { 67, 77, 71}, { 67, 72, 77}, { 68, 78, 72}, { 68, 73, 78}, { 69, 79, 73}, { 69, 74, 79},
             { 70, 80, 75}, { 70, 76, 80}, { 71, 81, 76}, { 71, 77, 81}, { 72, 82, 77}, { 72, 78, 82}, { 73, 83, 78}, { 73, 79, 83}, { 74, 84, 79},
     };
 
     public String getGameBoard() {
         return String.format(BOARD_TEMPLATE, getColors(getCells()));
     }
 
     public int[][] getCells() {
         notNull(cells);
         return cells;
     }
 
     public long calculatePoints(int numberOfConnectedCells) {
         if (numberOfConnectedCells < MIN_CELLS_IN_CLUSTER) {
             return 0;
         }
 
         return 3 * new BigDecimal(3).pow(numberOfConnectedCells - MIN_CELLS_IN_CLUSTER).longValueExact();
     }
 
     public long calculateTotal(ArrayList<Set<Integer>> clusters) {
         notNull(clusters);
         isTrue(!clusters.isEmpty());
 
         long result = 0;
         for (Set<Integer> cluster : clusters) {
             result += calculatePoints(cluster.size());
         }
 
         isTrue(result > 0);
         return result;
     }
 
     public int[][] fillRandomColors(int[][] cells) {
         notNull(cells);
         do {
             for (int[] cell : cells) {
                 notNull(cell);
                 cell[COLOR_INDEX] = RANDOM.nextInt(NUMBER_OF_COLORS + 1);
             }
         } while (hasClusters(cells));
 
         notNull(cells);
         isTrue(!hasClusters(cells));
         return cells;
     }
 
     public List<Set<Integer>> findClusters(int[][] cells) {
         notNull(cells);
         List<Set<Integer>> clusters = new ArrayList<Set<Integer>>();
 
         for (int cellNumber = 0; cellNumber < NUMBER_OF_CELLS; cellNumber++) {
             Set<Integer> cluster = makeCluster(cells, cellNumber, new HashSet<Integer>());
             if (cluster.size() >= MIN_CELLS_IN_CLUSTER) {
                 clusters.add(cluster);
             }
         }
 
         notNull(clusters);
         return distinct(clusters);
     }
 
     public boolean hasClusters(int[][] cells) {
         notNull(cells);
         return !findClusters(cells).isEmpty();
     }
 
     public void clearClusters(int[][] cells) {
         notNull(cells);
         if (!hasClusters(cells)) {
             return;
         }
 
         List<Set<Integer>> clusters = findClusters(cells);
         for (Set<Integer> cluster : clusters) {
             for (Integer cellNumber : cluster) {
                 cells[cellNumber][COLOR_INDEX] = NO_COLOR;
             }
         }
 
         isTrue(!hasClusters(cells));
     }
 
     public void fallDown(int[][] cells) {
         notNull(cells);
 
         for (int cellNumber = NUMBER_OF_CELLS - 10 - 1 /* 74 */; cellNumber >= 0; cellNumber--) {
             int[] cell = cells[cellNumber];
             int[] bottomCell = cells[cell[BOTTOM_CELL_INDEX]];
 
             if (cell[COLOR_INDEX] == NO_COLOR) {
                 continue;
             }
 
             if (bottomCell[COLOR_INDEX] != NO_COLOR) {
                 continue;
             }
 
             int[] targetCell = bottomCell;
             int nextTargetCellNumber = targetCell[BOTTOM_CELL_INDEX];
             while (nextTargetCellNumber != NO_CELL && cells[nextTargetCellNumber][COLOR_INDEX] == NO_COLOR) {
                 targetCell = cells[nextTargetCellNumber];
                 nextTargetCellNumber = targetCell[BOTTOM_CELL_INDEX];
             }
 
             isTrue(targetCell[COLOR_INDEX] == NO_COLOR);
             targetCell[COLOR_INDEX] = cell[COLOR_INDEX];
             cell[COLOR_INDEX] = NO_COLOR;
         }
     }
 
     private Set<Integer> makeCluster(int[][] cells, int cellNumber, Set<Integer> cluster) {
         notNull(cells);
         isTrue(cellNumber > -1);
         isTrue(cellNumber < NUMBER_OF_CELLS);
         notNull(cluster);
 
         int color = cells[cellNumber][COLOR_INDEX];
         if (color == NO_COLOR) {
             notNull(cluster);
             return cluster; // EARLY EXIT
         }
 
         cluster.add(cellNumber);
 
         for (int linkIndex = 0; linkIndex < 6; linkIndex++) {
             int nextCell = cells[cellNumber][linkIndex];
             if (nextCell == -1 || cluster.contains(nextCell) || cells[nextCell][COLOR_INDEX] != color) {
                 continue;
             }
 
             makeCluster(cells, nextCell, cluster);
         }
 
         notNull(cluster);
         return cluster;
     }
 
     private List<Set<Integer>> distinct(List<Set<Integer>> clusters) {
         notNull(clusters);
         if (clusters.isEmpty()) {
             return new ArrayList<Set<Integer>>(); // EARLY EXIT
         }
 
         List<Set<Integer>> distinctClusters = new ArrayList<Set<Integer>>();
         for (Set<Integer> cluster : clusters) {
             if (present(cluster, distinctClusters)) {
                 continue;
             }
             distinctClusters.add(cluster);
         }
 
         notNull(distinctClusters);
         isTrue(!distinctClusters.isEmpty());
         return distinctClusters;
     }
 
     private boolean present(Set<Integer> cluster, List<Set<Integer>> distinctClusters) {
         notNull(cluster);
         isTrue(!cluster.isEmpty());
 
         Integer anyCellFromCluster = cluster.iterator().next();
         for (Set<Integer> distinctCluster : distinctClusters) {
             if (distinctCluster.contains(anyCellFromCluster)) {
                 return true;
             }
         }
 
         return false;
     }
 
     private String[] getColors(int[][] cells) {
         notNull(cells);
 
         String[] result = new String[NUMBER_OF_CELLS];
         for (int i = 0; i < NUMBER_OF_CELLS; i++) {
             result[i] = cells[i][COLOR_INDEX] == NO_COLOR ? " " : String.valueOf(cells[i][COLOR_INDEX]);
         }
 
         notNull(result);
         return result;
     }
 }
