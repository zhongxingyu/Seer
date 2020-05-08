 import java.util.*;;
 
 public class PeopleCircle {
     public String order(int numMales, int numFemales, int K) {
         List<Integer> positions = new ArrayList<Integer>();
         List<Integer> circle = new ArrayList<Integer>();
         int total = numMales + numFemales;
         for(int i=0;i<total;i++) {
             circle.add(i);
         }
 
         char[] ans = new char[total];
         for(int i=0;i<total;i++) {
             ans[i] = 'M';
         }
 
         int rmposition = 0;
        int j = 0;
         for(int i=1;i<=numFemales;i++) {
            rmposition = (j + K - 1)%circle.size();
             positions.add(circle.get(rmposition));
            if (circle.size() > 1) {
                j = (rmposition)%(circle.size() - 1);
            }
             circle.remove(rmposition);
         }
 
         for(int i: positions) {
             ans[i] = 'F';
         }
     return String.valueOf(ans);
     }
 
 
 
 // BEGIN CUT HERE
 public static void main(String[] args) {
     if (args.length == 0) {
         PeopleCircleHarness.run_test(-1);
     } else {
         for (int i=0; i<args.length; ++i)
             PeopleCircleHarness.run_test(Integer.valueOf(args[i]));
     }
 }
 // END CUT HERE
 }
 
 // BEGIN CUT HERE
 class PeopleCircleHarness {
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
 
     static boolean compareOutput(String expected, String result) { return expected.equals(result); }
     static String formatResult(String res) {
         return String.format("\"%s\"", res);
     }
 
     static int verifyCase(int casenum, String expected, String received) {
         System.err.print("Example " + casenum + "... ");
         if (compareOutput(expected, received)) {
             System.err.println("PASSED");
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
                         int numMales              = 5;
                         int numFemales            = 3;
                         int K                     = 2;
                         String expected__         = "MFMFMFMM";
 
                         return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
             }
             case 1: {
                         int numMales              = 7;
                         int numFemales            = 3;
                         int K                     = 1;
                         String expected__         = "FFFMMMMMMM";
 
                         return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
             }
             case 2: {
                         int numMales              = 25;
                         int numFemales            = 25;
                         int K                     = 1000;
                         String expected__         = "MMMMMFFFFFFMFMFMMMFFMFFFFFFFFFMMMMMMMFFMFMMMFMFMMF";
 
                         return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
             }
             case 3: {
                         int numMales              = 5;
                         int numFemales            = 5;
                         int K                     = 3;
                         String expected__         = "MFFMMFFMFM";
 
                         return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
             }
             case 4: {
                         int numMales              = 1;
                         int numFemales            = 0;
                         int K                     = 245;
                         String expected__         = "M";
 
                         return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
             }
 
             // custom cases
 
             /*      case 5: {
                     int numMales              = ;
                     int numFemales            = ;
                     int K                     = ;
                     String expected__         = ;
 
                     return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
                     }*/
             /*      case 6: {
                     int numMales              = ;
                     int numFemales            = ;
                     int K                     = ;
                     String expected__         = ;
 
                     return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
                     }*/
             /*      case 7: {
                     int numMales              = ;
                     int numFemales            = ;
                     int K                     = ;
                     String expected__         = ;
 
                     return verifyCase(casenum__, expected__, new PeopleCircle().order(numMales, numFemales, K));
                     }*/
             default:
                     return -1;
         }
     }
 }
 
 // END CUT HERE
