 public class ExerciseMachine {
    public int getPercentages(String time) {
       String[] s = time.split(":");
       int H = Integer.parseInt(s[0]);
       int M = Integer.parseInt(s[1]);
       int S = Integer.parseInt(s[2]);
 
        double sum = H*60*60 + M*60 + S;

        int cnt=0;
        for (int i=1;i<100;i++) {
           if((sum*i%100) == 0)
                cnt++;
        }
        return cnt;
    }
 
 
 // BEGIN CUT HERE
    public static void main(String[] args) {
 		if (args.length == 0) {
 			ExerciseMachineHarness.run_test(-1);
 		} else {
 			for (int i=0; i<args.length; ++i)
 				ExerciseMachineHarness.run_test(Integer.valueOf(args[i]));
 		}
 	}
 // END CUT HERE
 }
 
 // BEGIN CUT HERE
 class ExerciseMachineHarness {
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
 
 	static boolean compareOutput(int expected, int result) { return expected == result; }
 	static String formatResult(int res) {
 		return String.format("%d", res);
 	}
 
 	static int verifyCase(int casenum, int expected, int received) {
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
 			String time               = "00:30:00";
 			int expected__            = 99;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}
 		case 1: {
 			String time               = "00:28:00";
 			int expected__            = 19;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}
 		case 2: {
 			String time               = "23:59:59";
 			int expected__            = 0;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}
 		case 3: {
 			String time               = "00:14:10";
 			int expected__            = 49;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}
 		case 4: {
 			String time               = "00:19:16";
 			int expected__            = 3;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}
 
 		// custom cases
 
 /*      case 5: {
 			String time               = ;
 			int expected__            = ;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}*/
 /*      case 6: {
 			String time               = ;
 			int expected__            = ;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}*/
 /*      case 7: {
 			String time               = ;
 			int expected__            = ;
 
 			return verifyCase(casenum__, expected__, new ExerciseMachine().getPercentages(time));
 		}*/
 		default:
 			return -1;
 		}
 	}
 }
 
 // END CUT HERE
