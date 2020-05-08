 public class BombSweeper {
    public int surround(int x, int y, String[] board) {
        int height = board.length;
        int width = board[0].length();
         int[] p = {-1, 0, 1};
 
         if (board[y].charAt(x) == 'B')
             return -1;
 
        for(int dy : p){
             for(int dx : p) {
                 if( x+dx < 0 || x+dx >= width ||
                     y+dy < 0 || y+dy >= height) continue;
                 if(board[y+dy].charAt(x+dx) == 'B')
                     return 0;
             }
        }
         return 1;
     }
 
     public double winPercentage(String[] board) {
         int height = board.length;
         int width = board[0].length();
         int safe = 0;
         int bad = 0;
        for(int i=0;i<height;i++) {
            for(int j=0;j<width;j++) {
                int f = surround(j, i, board);
                if(f==1)
                    safe++;
                else if(f==-1)
                    bad++;
            }
         }
        return 100.0 * (double)safe/((double)safe+(double)bad);
 
     }
 
 
     // BEGIN CUT HERE
     public static void main(String[] args) {
         if (args.length == 0) {
             BombSweeperHarness.run_test(-1);
         } else {
             for (int i=0; i<args.length; ++i)
                 BombSweeperHarness.run_test(Integer.valueOf(args[i]));
         }
     }
     // END CUT HERE
 }
 
 // BEGIN CUT HERE
 class BombSweeperHarness {
     public static void run_test(int casenum) {
         if (casenum != -1) {
             if (runTestCase(casenum) == -1)
                 System.err.println("Illegal input! Test case " + casenum + " does not exist.");
             return;
         }
 
         int correct = 0, total = 0;
         for (int i=0;; ++i) {
             int x = runTestCase(i);
             if (x == -1) {
                 if (i >= 100) break;
                 continue;
             }
             correct += x;
             ++total;
         }
 
         if (total == 0) {
             System.err.println("No test cases run.");
         } else if (correct < total) {
             System.err.println("Some cases FAILED (passed " + correct + " of " + total + ").");
         } else {
             System.err.println("All " + total + " tests passed!");
         }
     }
 
     static final double MAX_DOUBLE_ERROR = 1E-9;
     static boolean compareOutput(double expected, double result){ if(Double.isNaN(expected)){ return Double.isNaN(result); }else if(Double.isInfinite(expected)){ if(expected > 0){ return result > 0 && Double.isInfinite(result); }else{ return result < 0 && Double.isInfinite(result); } }else if(Double.isNaN(result) || Double.isInfinite(result)){ return false; }else if(Math.abs(result - expected) < MAX_DOUBLE_ERROR){ return true; }else{ double min = Math.min(expected * (1.0 - MAX_DOUBLE_ERROR), expected * (1.0 + MAX_DOUBLE_ERROR)); double max = Math.max(expected * (1.0 - MAX_DOUBLE_ERROR), expected * (1.0 + MAX_DOUBLE_ERROR)); return result > min && result < max; } }
     static double relativeError(double expected, double result) { if (Double.isNaN(expected) || Double.isInfinite(expected) || Double.isNaN(result) || Double.isInfinite(result) || expected == 0) return 0; return Math.abs(result-expected) / Math.abs(expected); }
 
     static String formatResult(double res) {
         return String.format("%.10g", res);
     }
 
     static int verifyCase(int casenum, double expected, double received) {
         System.err.print("Example " + casenum + "... ");
         if (compareOutput(expected, received)) {
             System.err.print("PASSED");
             double rerr = relativeError(expected, received);
             if (rerr > 0) System.err.printf(" (relative error %g)", rerr);
             System.err.println();
             return 1;
         } else {
             System.err.println("FAILED");
             System.err.println("    Expected: " + formatResult(expected));
             System.err.println("    Received: " + formatResult(received));
             return 0;
         }
     }
 
     static int runTestCase(int casenum__) {
         switch(casenum__) {
             case 0: {
                         String[] board            = {".....",
                             ".....",
                             "..B..",
                             ".....",
                             "....."};
                         double expected__         = 94.11764705882354;
 
                         return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
             }
             case 1: {
                         String[] board            = {"BBBBB",
                             "B...B",
                             "B...B",
                             "B...B",
                             "BBBBB"};
                         double expected__         = 5.882352941176471;
 
                         return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
             }
             case 2: {
                         String[] board            = {".........",
                             ".B..B..B.",
                             ".........",
                             ".........",
                             ".B..B..B.",
                             ".........",
                             ".........",
                             ".B..B..B.",
                             "........."};
                         double expected__         = 0.0;
 
                         return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
             }
             case 3: {
                         String[] board            = {".........................",
                             ".........................",
                             ".........................",
                             "........................."};
                         double expected__         = 100.0;
 
                         return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
             }
             case 4: {
                         String[] board            = {"......B.......B..B...........................B....",
                             "..............B..................BB..B............",
                             "B.B.B.............B.....B..............B..........",
                             "...................B...B....................BB....",
                             "...B.....B.........................B.......B.....B",
                             "B.B.........B.....B.......B..B......B.B...B.BB....",
                             "..B...................BB...............B..........",
                             ".........B...B................B..B................",
                             ".......BB.......B....B................B.....BBB...",
                             ".......BB..........B..............B......BB.......",
                             "...................BB.....................B.......",
                             "...B.B.B......B..............B...B......B.........",
                             "B................B................................",
                             "....B..........B.....B..BB....B...............BB..",
                             "..B....B.....B.............B.....B............B...",
                             "...................B.B........B..B.........B.B....",
                             ".....B.....B......................................",
                             "...........BB......BB...B.B........B...B..........",
                             ".....BBB..........................................",
                             ".B...........B....B...BB......B......B...B.B......",
                             "..................B........BB................B....",
                             "...............................B..B....BBB.B....B.",
                             "..........B.......................................",
                             ".....B..........B....BB......B.B......B......B....",
                             ".....B..................B........B................",
                             "............B.....B..B....BB...B....BB........B...",
                             "..B.................B.........B...................",
                             ".BB..............B................................",
                             "...B....B..................B.................B....",
                             "......B...B......B......................B.B.......",
                             "..............B..................B.......B........",
                             ".....B........BB...B.....B........................",
                             ".......B......B.B..B..........B...........B....B..",
                             "B...B...........B...B...B......B.B...B..B......B..",
                             "....B..B.....B.B.......BB..B............B.B....B..",
                             "B.......B..........B.........B...B.BB......B......",
                             "....B...............................B.............",
                             ".....B.B..........................................",
                             "..........B............B......B.B..B....B.........",
                             "....B...B.......................B.................",
                             "B.................B...........B..B....B...........",
                             "...B.....B........................................",
                             "...B..B......................................BBB..",
                             ".B...B....................................B....B..",
                             "...B...B..........B...B.B.........................",
                             ".....B.............B...BB..........B..BBB.BB......",
                             "....................B.....B.......................",
                             "........B..BB..........B.B....B...........B......B",
                             ".........B.....BB..B.............B....BB..........",
                             "....B..B..............B...B..B..........B........."};
                         double expected__         = 77.67558528428094;
 
                         return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
             }
 
             // custom cases
 
             /*      case 5: {
                     String[] board            = ;
                     double expected__         = ;
 
                     return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
                     }*/
             /*      case 6: {
                     String[] board            = ;
                     double expected__         = ;
 
                     return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
                     }*/
             /*      case 7: {
                     String[] board            = ;
                     double expected__         = ;
 
                     return verifyCase(casenum__, expected__, new BombSweeper().winPercentage(board));
                     }*/
             default:
                     return -1;
         }
     }
 }
 
 // END CUT HERE
