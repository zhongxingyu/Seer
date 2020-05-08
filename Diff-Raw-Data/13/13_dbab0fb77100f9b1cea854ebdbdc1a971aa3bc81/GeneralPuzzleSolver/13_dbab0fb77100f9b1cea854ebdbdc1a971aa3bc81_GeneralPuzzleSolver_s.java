 package gps;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collections;
import java.util.logging.Level;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
 
 import gps.solver.*;
 import gps.statemanager.*;
 import gnu.getopt.*;
 
 public class GeneralPuzzleSolver {
 	// name of program
 	final private static String progname = "GeneralPuzzleSolver";
 
 	private static Puzzle puzzle = Puzzle.GraphColoring;
 	private static Method method = Method.MinimumConflicts;
 	private static int rounds = 20;
 	private static String puzzleLevelS;
 	private static int puzzleLevelInt;
 	private static int MAX_STEPS = 100000;
 
 	public static void main(String[] args) throws Exception {
 		// TODO - delete - just test
 		// KQueensMC kqMC = new KQueensMC(10, new Range(5, 12));
 		// kqMC.solve(1000);
 		// kqMC.printVariables();
 
 		if (!parseCmdParams(args)) {
 			usage(progname);
 			return;
 		}
 
 		ConstraintBasedLocalSearch solver = chooseSolver();
 
 		solve(solver, rounds);
 	}
 
 	public static ConstraintBasedLocalSearch chooseSolver() throws Exception {
 		if (puzzle == Puzzle.GraphColoring) {
 			return (method == Method.SimulatedAnnealing) ? new GraphColorSA(
 					puzzleLevelS) : new GraphColorMC(puzzleLevelInt, new Range(
 					0, puzzleLevelInt));
 		} else if (puzzle == Puzzle.KQueens) { // TODO
 			// return (method == METHOD_SA) ? new KQueensSA(puzzleLevelS)
 			// : new GraphColorMC(puzzleLevelInt, new Range(0,
 			// puzzleLevelInt));
 		} else { // PUZZLE_XX TODO
 
 		}
 		return null;
 
 	}
 
 	public static void solve(ConstraintBasedLocalSearch solver, int rounds) {
 		ArrayList<Integer> steps = new ArrayList<Integer>();
 		ArrayList<Double> qualities = new ArrayList<Double>();
 		for (int i = 0; i < rounds; i++) {
 			solver.reset();
 			ArrayList<Integer> state = solver.solve(MAX_STEPS);
 			steps.add(solver.getStep());
 			qualities.add(solver.evaluateState(state));
 			System.out
 					.println("Step = " + solver.getStep()
 							+ " and solution quality is "
 							+ solver.evaluateState(state));
 		}
 
 		printStatistics(steps, qualities);
 
 	}
 
 	private static int getQuickestSolution(ArrayList<Integer> steps,
 			ArrayList<Double> qualities) {
 		int bestStep = Integer.MAX_VALUE;
 		for (int i = 0; i < qualities.size(); ++i) {
 			if (qualities.get(i) == 0.0 && steps.get(i) < bestStep)
 				bestStep = steps.get(i);
 		}
 		return bestStep;
 	}
 
 	private static void printStatistics(ArrayList<Integer> steps,
 			ArrayList<Double> qualities) {
 		DecimalFormat df = new DecimalFormat("#.##");
 
 		double meanOfQualities = getMean(qualities);
 		double meanOfSteps = getMean(steps);
 		double bestEvaluation = Collections.min(qualities);
 		double stddevOfQualities = getStandardDeviation(qualities);
 		double stddevOfSteps = getStandardDeviation(steps);
 		int quickestSolution = getQuickestSolution(steps, qualities);
 
 		System.out.println("--------------------------------------");
 		System.out.println(rounds + " runs");
 		System.out.println("Best evaluation: " + bestEvaluation);
 
 		System.out.println("Quickest solution: "
 				+ ((bestEvaluation == 0.0) ? quickestSolution
 						: "no optimal solution found"));
 		System.out.println("Avarage evaluation of result: "
 				+ df.format(meanOfQualities) + " +-"
 				+ df.format(stddevOfQualities));
 		System.out.println("Avarage number of steps: " + df.format(meanOfSteps)
 				+ " +-" + df.format(stddevOfSteps));
 		System.out.println("--------------------------------------");
 
 	}
 
 	private static Boolean parseCmdParams(String[] argv) {
 		Boolean retval = true;
 		int c;
 		String arg;
		String level = new String();
 
 		Getopt g = new Getopt(progname, argv, "hp:l:r:m:");
 		g.setOpterr(false); // We'll do our own error handling
 		//
 
 		while ((c = g.getopt()) != -1)
 			switch (c) {
 			case 'p':
 				arg = g.getOptarg();
 				if (arg == null) {
 					retval = false;
 				} else if (arg.compareTo(Puzzle.KQueens.getKey()) == 0) {
 					puzzle = Puzzle.KQueens;
 				} else if (arg.compareTo(Puzzle.GraphColoring.getKey()) == 0) {
 					puzzle = Puzzle.GraphColoring;
 				} else if (arg.compareTo(Puzzle.XX.getKey()) == 0) {
 					puzzle = Puzzle.XX;
 				}
 				break;
 			//
 			case 'l':
 				arg = g.getOptarg();
 				if (arg == null) {
 					retval = false;
 				}
 				level = new String(arg);
 				break;
 			case 'r':
 				arg = g.getOptarg();
 				if (arg == null) {
 					retval = false;
 				}
 				rounds = Integer.parseInt(arg);
 				break;
 			//
 			case 'h':
 				retval = false;
 				break;
 			//
 			case 'm':
 				arg = g.getOptarg();
 				if (arg == null) {
 					retval = false;
 				} else if (arg.compareTo(Method.MinimumConflicts.getKey()) == 0) {
 					method = Method.MinimumConflicts;
 				} else if (arg.compareTo(Method.SimulatedAnnealing.getKey()) == 0) {
 					method = Method.SimulatedAnnealing;
 				} else {
 					retval = false;
 				}
 				break;
 			case '?':
 				System.out.println("The option '" + (char) g.getOptopt()
 						+ "' is not valid");
 				break;
 			//
 			default:
 				System.out.println("getopt() returned " + c);
 				break;
 			}
 		//
 		for (int i = g.getOptind(); i < argv.length; i++)
 			System.out.println("Non option argv element: " + argv[i] + "\n");
 
 		// parse puzzle level;
 		if (level == null) {
 			return false;
 		} else if (puzzle == Puzzle.GraphColoring) {
 			puzzleLevelS = level;
 		} else if (puzzle == Puzzle.KQueens) {
 			puzzleLevelInt = Integer.parseInt(level);
 		} else if (puzzle == Puzzle.XX) {
 			puzzleLevelInt = Integer.parseInt(level);
 		}
 
 		return retval;
 
 	}
 
 	private static void usage(String progname) {
 		System.out.println("USAGE: " + progname + " [-p PUZZLE] "
 				+ "-l LEVEL [-r ROUNDS] [-m METHOD]");
 		System.out.println();
 		System.out.println("\tPUZZLE = [KQ|GC|XX] where");
 		System.out.println("\t         \tKQ is KQueens");
 		System.out.println("\t         \tGC is GraphColoring, this is implicit");
 		System.out.println("\t         \tXX is YYY");
 		System.out.println();
 		System.out.println("\tLEVEL  = [INT|FILENAME] where");
 		System.out
 				.println("\t         \tINT is number of queens in case of KQ");
 		System.out
 				.println("\t         \tFILENAME is path to source file in case of GC");
 		System.out.println();
 		System.out
 				.println("\tROUNDS  express count of how many times puzzle is being solved.");
 		System.out.println("\t        is implicit 20");
 		System.out.println();
 		System.out.println("\tMETHOD = [SA|MC] where");
 		System.out.println("\t          \tSA is Simulated Annealing");
 		System.out
 				.println("\t          \tMC is Minimum Conflicts, this is implicit value");
 
 	}
 
 	private static <T extends Number> double getMean(ArrayList<T> list) {
 		double mean = 0;
 		for (T i : list) {
 			mean += i.doubleValue();
 		}
 		return mean / list.size();
 	}
 
 	private static <T extends Number> double getStandardDeviation(
 			ArrayList<T> list) {
 		double mean = getMean(list);
 		double stddev = 0;
 		for (T i : list) {
 			stddev += Math.pow(i.doubleValue() - mean, 2);
 		}
 		return Math.sqrt(stddev / list.size());
 	}
 }
