 package bb;
 
 import general.DoubleComparator;
 import general.QoSAttribute;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 /**
  * This class adapts a given Web Service composition problem, possibly with
  * constraints over some attributes, into a MIP problem to be solved by Branch
  * and Bound.
  * 
  * @author Andre Luiz Verucci da Cunha
  * 
  */
 public class WscHelper {
 
 	/**
 	 * The QoS attributes to be considered.
 	 */
 	private QoSAttribute[] mQoSAttributes;
 
 	/**
 	 * The constraints over the aggregated QoS values.
 	 */
 	private ArrayList<Constraint> mConstraints;
 
 	/**
 	 * The number of abstract services.
 	 */
 	private int mNoAbstractServices;
 
 	/**
 	 * The number of concrete services.
 	 */
 	private int mNoConcreteServices;
 
 	/**
 	 * Whether this problem has already been solved.
 	 */
 	private boolean mIsSolved;
 
 	/**
 	 * The solution found by the last call to solve().
 	 */
 	private double[] mLastSolution;
 
 	public WscHelper(QoSAttribute[] attributes) {
 		mQoSAttributes = attributes;
 		mConstraints = new ArrayList<Constraint>();
 
 		mNoAbstractServices = attributes[0].getValues().length;
 		for (int i = 0; i < mNoAbstractServices; i++) {
 			mNoConcreteServices += attributes[0].getValues()[i].length;
 		}
 	}
 
 	/**
 	 * Adds a constraint over the aggregated value of an attribute.
 	 * 
 	 * @param attributeIndex
 	 *            The index of the attribute.
 	 * @param rel
 	 *            One of Simplex.GTE, Simplex.EQUALS and Simplex.LTE.
 	 * @param b
 	 *            The right-hand term of the constraint.
 	 */
 	public void addConstraintOnAttribute(int attributeIndex, int rel, double b) {
 		double[] a = new double[mNoConcreteServices];
 
 		int accum = 0;
 		switch (mQoSAttributes[attributeIndex].getAggregationMethod()) {
 		case QoSAttribute.AGGREGATE_BY_PRODUCT:
 			throw new IllegalArgumentException(
 					"Non-linear aggregation function.");
 		case QoSAttribute.AGGREGATE_BY_AVERAGE:
 			for (int i = 0; i < mNoAbstractServices; i++) {
 				for (int j = 0; j < mQoSAttributes[attributeIndex].getValues()[i].length; j++) {
 					a[accum] = mQoSAttributes[attributeIndex].getValues()[i][j]
 							/ mNoAbstractServices;
 					accum++;
 				}
 			}
 			mConstraints.add(new Constraint(a, rel, b));
 			break;
 		case QoSAttribute.AGGREGATE_BY_SUM:
 			for (int i = 0; i < mNoAbstractServices; i++) {
 				for (int j = 0; j < mQoSAttributes[attributeIndex].getValues()[i].length; j++) {
 					a[accum] = mQoSAttributes[attributeIndex].getValues()[i][j];
 					accum++;
 				}
 			}
 			mConstraints.add(new Constraint(a, rel, b));
 			break;
 		default:
 			break;
 		}
 
 		mIsSolved = false;
 	}
 
 	/**
 	 * Manually adds a constraint.
 	 * 
 	 * @param c
 	 *            The constraint to be added.
 	 */
 	public void addConstraint(Constraint c) {
 		mConstraints.add(c);
 		mIsSolved = false;
 	}
 
 	/**
 	 * Manually adds a constraint.
 	 * 
 	 * @param a
 	 *            The constraint's coefficients.
 	 * @param rel
 	 *            One of Simplex.GTE, Simplex.EQUALS and Simplex.LTE.
 	 * @param b
 	 *            The right-hand term of the constraint.
 	 */
 	public void addConstraint(double[] a, int rel, double b) {
 		mConstraints.add(new Constraint(a, rel, b));
 		mIsSolved = false;
 	}
 
 	/**
 	 * Returns the MIP problem corresponding to the given QoS attributes and
 	 * constraints.
 	 * 
 	 * @return the MIP problem corresponding to the given QoS attributes and
 	 *         constraints.
 	 */
 	public BranchAndBound getProblem() {
 		Simplex s = new Simplex();
 
 		double[][] totalQoS = QoSAttribute.calculateTotalQoS(mQoSAttributes);
 
 		double[] objectiveFunction = new double[mNoConcreteServices + 1];
 		int accum = 0;
 		for (int i = 0; i < mNoAbstractServices; i++) {
 			for (int j = 0; j < totalQoS[i].length; j++) {
 				objectiveFunction[accum + 1] = totalQoS[i][j];
 				accum++;
 			}
 		}
 
 		s.setObjectiveFuntion(objectiveFunction, Simplex.MAXIMIZE);
 
 		/* Adding the common constraints. */
 		accum = 0;
 		for (int i = 0; i < mNoAbstractServices; i++) {
 			double[] a = new double[mNoConcreteServices];
 			for (int j = 0; j < totalQoS[i].length; j++) {
 				s.addBinaryVariableConstraint(accum + 1);
 				a[accum] = 1;
 				accum++;
 			}
 
 			s.addConstraint(a, Simplex.EQUALS, 1);
 		}
 
 		/* Adding the user-defined constraints. */
 		for (Constraint c : mConstraints) {
 			s.addConstraint(c);
 		}
 
 		boolean[] integerVariables = new boolean[mNoConcreteServices];
 		Arrays.fill(integerVariables, true);
 
 		BranchAndBound bb = new BranchAndBound(s, integerVariables);
 
 		return bb;
 	}
 
 	/**
 	 * Generates and solves the corresponding problem.
 	 * 
 	 * @return The return value of BranchAndBound.solve() when applied to the
 	 *         generated problem.
 	 */
 	public boolean solveProblem() {
 		mIsSolved = true;
 
 		BranchAndBound bb = getProblem();
 		boolean result = bb.solve();
 		mLastSolution = bb.getSolution();
 
 		return result;
 	}
 
 	/**
 	 * Returns the solution in terms of which concrete service must be chosen
 	 * for each abstract service.
 	 * 
	 * @return A vector containing which concrete service should be selected for
	 *         each abstract service, or null if the problem is infeasible.
 	 */
 	public int[] getSolution() {
 		if (!mIsSolved) {
 			throw new IllegalStateException("Problem not solved.");
 		}
 
		if (mLastSolution == null) {
			return null;
		}

 		int[] solution = new int[mNoAbstractServices];
 
 		double[][] values = mQoSAttributes[0].getValues();
 
 		int next = 0;
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[i].length; j++) {
 				if (DoubleComparator.compare(mLastSolution[next], 0d) == 0) {
 					next++;
 				} else if (DoubleComparator.compare(mLastSolution[next], 1d) == 0) {
 					solution[i] = j;
 					next += (values[i].length - j);
					break;
 				}
 			}
 		}
 
 		return solution;
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		double[][] values = { { 1.0, 0.5, 0.5 }, { 0.5, 1.0 },
 				{ 0.2, 0.5, 1.0 } };
 
 		QoSAttribute attrSum = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_SUM, 0.2);
 		QoSAttribute attrProd = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_PRODUCT, 0.2);
 		QoSAttribute attrAvg = new QoSAttribute(values,
 				QoSAttribute.AGGREGATE_BY_AVERAGE, 0.6);
 
 		QoSAttribute[] attrs = new QoSAttribute[] { attrSum, attrProd, attrAvg };
 
 		WscHelper h = new WscHelper(attrs);
 		// h.addConstraintOnAttribute(0, Simplex.LTE, 2.6); //<<<<<<<<
 		// h.addConstraintOnAttribute(0, Simplex.LTE, 0.8); //<<<<<<<<
 		// h.addConstraintOnAttribute(0, Simplex.LTE, 1.2);
		// h.addConstraintOnAttribute(0, Simplex.LTE, 1.5);
 
		h.solveProblem();
 
		System.out.println(Arrays.toString(h.getSolution()));
 	}
 }
