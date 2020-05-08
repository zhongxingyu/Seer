 package bb;
 
 import general.DoubleComparator;
 
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.PriorityQueue;
 
 /**
  * This class implements the Branch and Bound algorithm.
  * 
  * @author Andre Luiz Verucci da Cunha
  * 
  */
 public class BranchAndBound {
 
 	/**
 	 * The queue containing the active nodes.
 	 */
 	private PriorityQueue<Simplex> mNodesQueue;
 
 	/**
 	 * The best solution found.
 	 */
 	private Simplex mBestSolution;
 
 	/**
 	 * The base problem, without the integer constraints.
 	 */
 	private Simplex mRelaxedBaseProblem;
 
 	/**
 	 * The variables that must be integer.
 	 */
 	private boolean[] mIntegerVariables;
 
 	/**
 	 * A variable used to compare two Simplex instances.
 	 */
 	private SimplexComparator mSimplexComparator;
 
 	/**
 	 * A variable used to reversely compare two Simplex instances. Used in the
 	 * piority queue only.
 	 */
 	private ReverseSimplexComparator mReverseSimplexComparator;
 
 	/**
 	 * Whether this problem has already been solved.
 	 */
 	private boolean mIsSolved;
 
 	/**
 	 * Whether or not to produce verbose output while running.
 	 */
 	private boolean mVerbose;
 
 	/**
 	 * Creates a BB instance.
 	 * 
 	 * @param baseRelaxedProblem
 	 *            The base problem.
 	 * @param integerVariables
 	 *            The variables that should be integer.
 	 */
 	public BranchAndBound(Simplex baseRelaxedProblem, boolean[] integerVariables) {
 		mSimplexComparator = new SimplexComparator();
 		mReverseSimplexComparator = new ReverseSimplexComparator();
 		mNodesQueue = new PriorityQueue<Simplex>(10, mReverseSimplexComparator);
 		mRelaxedBaseProblem = baseRelaxedProblem;
 		mIntegerVariables = integerVariables;
 	}
 
 	/**
 	 * Chooses which non-integer variable should be branched, using strong
 	 * branching.
 	 * 
 	 * @return The variable to be branched, or -1 if all constrained variables
 	 *         are integer.
 	 */
 	private int chooseVariableToBranch(Simplex problem) {
 		int bestVariable = -1;
 		double[] solution = problem.getSolution();
 		double[] objectiveFunction = problem.getOriginalObjectiveFunction();
 
 		for (int i = 1; i <= solution.length; i++) {
 			if (DoubleComparator.compare(solution[i - 1],
 					(double) Math.round(solution[i - 1])) != 0
 					&& mIntegerVariables[i - 1]) {
 				if (bestVariable == -1) {
 					bestVariable = i;
 				} else if (objectiveFunction[i] > objectiveFunction[bestVariable]) {
 					bestVariable = i;
 				}
 			}
 		}
 		return bestVariable;
 	}
 
 	/**
 	 * Solves this MIP problem.
 	 * 
 	 * @return True if problem is feasible and bounded; false otherwise.
 	 */
 	public boolean solve() {
 		if (mVerbose) {
 			System.out.println("### B&B ### Starting Branch and Bound.");
 		}
 		mIsSolved = true;
 
 		if (!mRelaxedBaseProblem.solve()) {
 			/* Relaxed base problem is infeasible. */
 			if (mVerbose) {
 				System.out
 						.println("### B&B ### Relaxed base problem is infeasible; returning.");
 			}
 			return false;
 		}
 
 		if (mVerbose) {
 			System.out
 					.println("### B&B ### Adding initial problem to the queue.");
 		}
 		mNodesQueue.add(mRelaxedBaseProblem);
 
 		while (!mNodesQueue.isEmpty()) {
 			Simplex currentProblem = mNodesQueue.remove();
 
 			if (mVerbose) {
 				System.out.println("### B&B ### Analyzing next problem:\n"
 						+ currentProblem.getStandardFormDescription());
 			}
 
 			if (mBestSolution != null
 					&& mSimplexComparator
 							.compare(currentProblem, mBestSolution) <= 0) {
 				/*
 				 * Problem is feasible, but we already have a better solution.
 				 * Prune by quality.
 				 */
 				if (mVerbose) {
 					System.out
 							.println("### B&B ### Problem is feasible, but we already have a better or equal solution. Prune by quality.");
 				}
 
 				continue;
 			}
 
 			int var = chooseVariableToBranch(currentProblem);
 
 			if (var == -1) {
 				/* Solution is integer. */
 				if (mVerbose) {
 					System.out.println("### B&B ### Solution is integer.");
 				}
 				if (mBestSolution == null) {
 					mBestSolution = currentProblem;
 					if (mVerbose) {
 						System.out
 								.println("### B&B ### First integer solution found.");
 					}
 				} else if (mSimplexComparator.compare(currentProblem,
 						mBestSolution) > 0) {
 					/*
 					 * Current solution is better than the best one found
 					 * previously.
 					 */
 					mBestSolution = currentProblem;
 
 					if (mVerbose) {
 						System.out
 								.println("### B&B ### Found a new best integer solution.");
 					}
 				}
 			} else {
 				/* Solution has non-integer variables. We need to branch. */
 				if (mVerbose) {
 					System.out
 							.println("### B&B ### Solution has non-integer variables. Branching on variable x"
 									+ var + ".");
 				}
 
 				Simplex leftChild = new Simplex(currentProblem);
 				Simplex rightChild = new Simplex(currentProblem);
 
 				double value = currentProblem.getSolution()[var - 1];
 				double[] a = new double[currentProblem.getOriginalNoVariables()];
 				a[var - 1] = 1;
 
 				leftChild.addConstraint(a, Simplex.LTE, Math.floor(value));
 				rightChild.addConstraint(a, Simplex.GTE, Math.ceil(value));
 
 				if (leftChild.solve()) {
 					if (mVerbose) {
 						System.out.println("### B&B ### Left child (@ "
 								+ leftChild
 										.getObjectiveValueOfOptimalSolution()
 								+ ") is feasible. Adding to the queue.");
 					}
 					mNodesQueue.add(leftChild);
 				}
 
 				if (rightChild.solve()) {
 					if (mVerbose) {
 						System.out.println("### B&B ### Right child (@ "
 								+ rightChild
 										.getObjectiveValueOfOptimalSolution()
 								+ ") is feasible. Adding to the queue.");
 					}
 					mNodesQueue.add(rightChild);
 				}
 			}
 		}
 
 		if (mVerbose) {
 			System.out.println("### B&B ### Finished Branch and Bound.");
 		}
 
 		return (mBestSolution != null);
 	}
 
 	/**
 	 * Returns the solution found, or null if the problem is infeasible or
 	 * unbounded, raising an exception if it was not solved yet.
 	 * 
 	 * @return The solution found, or null if the problem is infeasible or
 	 *         unbounded.
 	 */
 	public double[] getSolution() {
 		if (!mIsSolved) {
 			throw new IllegalStateException("Problem not solved.");
 		}
 
 		if (mBestSolution == null) {
 			return null;
 		}
 		return mBestSolution.getSolution();
 	}
 
 	/**
 	 * Returns the objective value associated with the optimal solution, raising
 	 * an exception if the problem wasn't solved, or if it's infeasible or
 	 * unbounded.
 	 * 
 	 * @return The objective value of the optimal solution.
 	 */
 	public double getObjectiveValueOfOptimalSolution() {
 		if (!mIsSolved) {
 			throw new IllegalStateException("Problem not solved.");
 		}
 		if (mBestSolution == null) {
 			throw new IllegalStateException(
 					"Problem is either infeasible or unbounded.");
 		}
 
 		return mBestSolution.getObjectiveValueOfOptimalSolution();
 	}
 
 	/**
 	 * Returns the relaxed base problem.
 	 * 
 	 * @return the relaxed base problem.
 	 */
 	public Simplex getRelaxedProblem() {
 		return mRelaxedBaseProblem;
 	}
 
 	/**
 	 * Activates or deactivates the verbose mode.
 	 * 
 	 * @param verbose
 	 *            if true, activates the verbose mode; if false, deactivates it.
 	 */
 	public void setVerbose(boolean verbose) {
 		mVerbose = verbose;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 
 		b.append(mRelaxedBaseProblem.toString());
 		b.append("\n\nInteger variables: ");
 
 		for (int i = 0; i < mIntegerVariables.length; i++) {
 			if (mIntegerVariables[i]) {
 				b.append("x" + (i + 1) + " ");
 			}
 		}
 
 		return b.toString().trim();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Simplex baseRelaxedProblem = new Simplex();
 
 		/*
 		 * baseRelaxedProblem.setObjectiveFuntion(new double[] { 0, 8, 11, 6, 4
 		 * }, Simplex.MAXIMIZE); baseRelaxedProblem.addConstraint(new double[] {
 		 * 5, 7, 4, 3 }, Simplex.LTE, 14);
 		 * baseRelaxedProblem.addBinaryVariableConstraint(1);
 		 * baseRelaxedProblem.addBinaryVariableConstraint(2);
 		 * baseRelaxedProblem.addBinaryVariableConstraint(3);
 		 * baseRelaxedProblem.addBinaryVariableConstraint(4);
 		 */
 
 		baseRelaxedProblem.setObjectiveFuntion(new double[] { 0, 1, 1.5 },
 				Simplex.MAXIMIZE);
 		baseRelaxedProblem
 				.addConstraint(new double[] { 3, 5 }, Simplex.LTE, 16);
 		baseRelaxedProblem
 				.addConstraint(new double[] { 6, 5 }, Simplex.LTE, 30);
 
 		/*
 		 * baseRelaxedProblem.setObjectiveFuntion(new double[] { 0, 10, 6, 4 },
 		 * Simplex.MAXIMIZE); baseRelaxedProblem.addConstraint(new double[] { 1,
 		 * 1, 1 }, Simplex.LTE, 100); baseRelaxedProblem.addConstraint(new
 		 * double[] { 10, 4, 5 }, Simplex.LTE, 600);
 		 * baseRelaxedProblem.addConstraint(new double[] { 2, 2, 6 },
 		 * Simplex.LTE, 300);
 		 */
 
 		/*
 		 * System.out.println(baseRelaxedProblem); baseRelaxedProblem.solve();
 		 * System.out.println(baseRelaxedProblem); baseRelaxedProblem.solve();
 		 * System.out.println(baseRelaxedProblem);
 		 */
 
 		boolean[] integerVariables = new boolean[3];
 		Arrays.fill(integerVariables, true);
 
 		BranchAndBound bb = new BranchAndBound(baseRelaxedProblem,
 				integerVariables);
 		bb.setVerbose(true);
 		bb.solve();
 		// baseRelaxedProblem.solve();
 
 		// System.out.println(Arrays.toString(baseRelaxedProblem.getSolution()));
 		System.out.println(Arrays.toString(bb.getSolution()));
 	}
 }
 
 /**
  * A class used to compare two linear programs.
  * 
  * @author Andre Luiz Verucci da Cunha
  * 
  */
 class SimplexComparator implements Comparator<Simplex> {
 
 	@Override
 	public int compare(Simplex o1, Simplex o2) {
 		double obj1 = o1.getObjectiveValueOfOptimalSolution();
 		double obj2 = o2.getObjectiveValueOfOptimalSolution();
 
 		return DoubleComparator.compare(obj1, obj2);
 	}
 }
 
 /**
  * A class used to compare two linear programs, so that we can create a reverse
  * priority queue of them.
  * 
  * @author Andre Luiz Verucci da Cunha
  * 
  */
 class ReverseSimplexComparator implements Comparator<Simplex> {
 
 	@Override
 	public int compare(Simplex o1, Simplex o2) {
 		double obj1 = o1.getObjectiveValueOfOptimalSolution();
 		double obj2 = o2.getObjectiveValueOfOptimalSolution();
 
 		/*
 		 * We return the opposite of the comparison so that the priority queue
 		 * is reverse-ordered.
 		 */
 		return -DoubleComparator.compare(obj1, obj2);
 	}
}
