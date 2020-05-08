 package bb;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 public class Simplex {
 
 	public static final int EQUALS = 0x00;
 	public static final int LTE = 0x01;
 	public static final int GTE = 0x02;
 
 	public static final int MINIMIZE = 0x00;
 	public static final int MAXIMIZE = 0x01;
 
 	private double[][] mA;
 	private double[] mb;
 	private double[] mc;
 	private double mv;
 	private int[] mB;
 	private int[] mN;
 
 	private ArrayList<Constraint> mConstraints;
 	private int mObjective;
 
 	private boolean mIsNormalized;
 
 	public Simplex() {
 		mConstraints = new ArrayList<Constraint>();
 		mIsNormalized = false;
 	}
 
 	/**
 	 * Adds the constraint
 	 * 
 	 * a[0]x[1] + a[1]x[2] + ... + a[n-1]x[n] rel b
 	 * 
 	 * Where "rel" is either EQUALS, LTE or GTE.
 	 * 
 	 * @param a
 	 * @param rel
 	 * @param b
 	 */
 	public void addConstraint(double[] a, int rel, double b) {
 		mConstraints.add(new Constraint(a, rel, b));
 		mIsNormalized = false;
 	}
 
 	/**
 	 * Sets the objective function to:
 	 * 
 	 * f(x_0, x_1, ..., x_{n-1}) = objectiveFunction[0] +
 	 * objectiveFunction[1]*x[0] + objectiveFunction[2]*x[1] + ... +
 	 * objectiveFunction[n]*x[n-1]
 	 * 
 	 * @param objective
 	 *            Function The objective function coefficients.
 	 * @param objective
 	 *            Either MINIMIZE or MAXIMIZE.
 	 */
 	public void setObjectiveFuntion(double[] objectiveFunction, int objective) {
 		mv = objectiveFunction[0];
 		mc = Arrays.copyOfRange(objectiveFunction, 1, objectiveFunction.length);
 		mObjective = objective;
 		mIsNormalized &= (objective == MAXIMIZE);
 	}
 
 	/**
 	 * Puts the current linear problem in the slack form.
 	 */
 	private void normalize() {
 		/* This must be a maximization problem. */
 		if (mObjective == MINIMIZE) {
 			for (int i = 0; i < mc.length; i++) {
 				mc[i] = -mc[i];
 			}
 			mv = -mv;
 			mObjective = MAXIMIZE;
 		}
 
 		/* We must set the basic and non-basic index sets. */
 		mN = new int[mConstraints.get(0).a.length];
 		for (int i = 1; i <= mN.length; i++) {
 			mN[i - 1] = i;
 		}
 
 		mB = new int[mConstraints.size()];
 		for (int i = 1; i <= mB.length; i++) {
 			mB[i - 1] = mN.length + i;
 		}
 
 		/*
 		 * We must alloc space for the new coefficients in the objective
 		 * function.
 		 */
 		double[] newC = new double[mc.length + mB.length];
 		System.arraycopy(mc, 0, newC, 0, mc.length);
 		mc = newC;
 
 		/* All the constraints must be equalities. */
 		mA = new double[mConstraints.size() + mB.length][mConstraints.get(0).a.length
 				+ mB.length];
 		mb = new double[mConstraints.size() + mB.length];
 
 		for (int i = 0; i < mConstraints.size(); i++) {
 			switch (mConstraints.get(i).rel) {
 			case EQUALS:
 				throw new NotImplementedException();
 			case GTE:
 				mb[mB[i] - 1] = -mConstraints.get(i).b;
 				for (int j = 0; j < mConstraints.get(i).a.length; j++) {
 					mA[mB[i] - 1][j] = -mConstraints.get(i).a[j];
 				}
 				break;
 			case LTE:
 				mb[mB[i] - 1] = mConstraints.get(i).b;
 				for (int j = 0; j < mConstraints.get(i).a.length; j++) {
 					mA[mB[i] - 1][j] = mConstraints.get(i).a[j];
 				}
 				break;
 			default:
 				break;
 			}
 		}
 
 		mIsNormalized = true;
 	}
 
 	private void pivot(int leaving, int entering) {
 		/* Compute the coefficients of the equation for new variable xe. */
 		mb[entering - 1] = mb[leaving - 1] / mA[leaving - 1][entering - 1];
 		for (int j : mN) {
 			if (j != entering) {
 				mA[entering - 1][j - 1] = mA[leaving - 1][j - 1]
 						/ mA[leaving - 1][entering - 1];
 			}
 		}
 		mA[entering - 1][leaving - 1] = 1.0 / mA[leaving - 1][entering - 1];
 
 		/* Compute the coefficients of the remaining constraints. */
 		for (int i : mB) {
 			if (i != leaving) {
 				mb[i - 1] -= mA[i - 1][entering - 1] * mb[entering - 1];
 				for (int j : mN) {
 					if (j != entering) {
 						mA[i - 1][j - 1] -= mA[i - 1][entering - 1]
 								* mA[entering - 1][j - 1];
 					}
 				}
 				mA[i - 1][leaving - 1] = -mA[i - 1][entering - 1]
 						* mA[entering - 1][leaving - 1];
 			}
 		}
 
 		/* Compute the objective function. */
 		mv += mc[entering - 1] * mb[entering - 1];
 		for (int j : mN) {
 			if (j != entering) {
				mc[j - 1] -= (mc[entering - 1] * mA[entering - 1][j - 1]);
 			}
 		}
		mc[leaving - 1] = -mc[entering - 1] * mA[entering - 1][leaving - 1];
 
 		/* Compute the new set of basic and nonbasic variables. */
 		for (int i = 0; i < mN.length; i++) {
 			if (mN[i] == entering) {
 				mN[i] = leaving;
 				break;
 			}
 		}
 		
 		for (int i = 0; i < mB.length; i++) {
 			if (mB[i] == leaving) {
 				mB[i] = entering;
 				break;
 			}
 		}
 	}
 
 	public void solve() {
 
 	}
 
 	public int[] getSolution() {
 		// TODO: Unimplemented.
 		return null;
 	}
 
 	@Override
 	public String toString() {
 		StringBuilder b = new StringBuilder();
 
 		if (mIsNormalized) {
			Arrays.sort(mB);
			Arrays.sort(mN);
			
 			b.append("z\t=\t" + String.format("%+6.3g", mv));
 			for (int j : mN) {
 				b.append("\t" + String.format("%+6.3g", mc[j - 1]) + " x" + j);
 			}
 			b.append("\n");
 
 			for (int i : mB) {
 				b.append("x" + i + "\t=\t"
 						+ String.format("%+6.3g", mb[i - 1]));
 				for (int j : mN) {
 					b.append("\t" + String.format("%+6.3g", -mA[i - 1][j - 1])
 							+ " x" + j);
 				}
 				b.append("\n");
 			}
 		} else {
 			b.append(mObjective == MINIMIZE ? "Minimize:\n\t" : "Maximize:\n\n");
 			b.append("z = " + String.format("%+6.3g", mv));
 			for (int j = 0; j < mc.length; j++) {
 				b.append("\t" + String.format("%+6.3g", mc[j]) + " x" + (j + 1));
 			}
 
 			b.append("\n\nSubject to:\n");
 			for (Constraint constraint : mConstraints) {
 				b.append("\t");
 				for (int j = 0; j < constraint.a.length; j++) {
 					b.append("\t" + String.format("%+6.3g", constraint.a[j])
 							+ " x" + (j + 1));
 				}
 				b.append("\t");
 				switch (constraint.rel) {
 				case EQUALS:
 					b.append("= \t");
 					break;
 				case GTE:
 					b.append(">=\t");
 					break;
 				case LTE:
 					b.append("<=\t");
 					break;
 				default:
 					break;
 				}
 				b.append(String.format("%+6.3g", constraint.b) + "\n");
 			}
 		}
 
 		return b.toString();
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Simplex s = new Simplex();
 
 		s.setObjectiveFuntion(new double[] { 0, 3, 1, 2 }, MAXIMIZE);
 
 		s.addConstraint(new double[] { 1, 1, 3 }, LTE, 30);
 		s.addConstraint(new double[] { 2, 2, 5 }, LTE, 24);
 		s.addConstraint(new double[] { 4, 1, 2 }, LTE, 36);
 
 		System.out.println(s);
 
 		s.normalize();
 
 		System.out.println(s);
 
 		s.pivot(6, 1);
 		System.out.println(s);
		
		s.pivot(5, 3);
		System.out.println(s);
		
		s.pivot(3, 2);
		System.out.println(s);
 	}
 
 }
