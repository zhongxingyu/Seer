 /*
  * This file is part of libror.
  * libror is distributed from http://smaa.fi/libror.php.
  * Copyright (C) 2011 Tommi Tervonen.
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package fi.smaa.libror;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 import org.apache.commons.math.optimization.GoalType;
 import org.apache.commons.math.optimization.OptimizationException;
 import org.apache.commons.math.optimization.RealPointValuePair;
 import org.apache.commons.math.optimization.linear.LinearConstraint;
 import org.apache.commons.math.optimization.linear.LinearObjectiveFunction;
 import org.apache.commons.math.optimization.linear.NoFeasibleSolutionException;
 import org.apache.commons.math.optimization.linear.Relationship;
 import org.apache.commons.math.optimization.linear.SimplexSolver;
 
 
 @SuppressWarnings("deprecation")
 public class UTAGMSSolver extends RORModel {
 
 	private RealMatrix necessaryRelation = null;
 	private RealMatrix possibleRelation = null;
 	private SimplexSolver solver = new SimplexSolver();
 	private boolean strictValueFunctions = false;
 	private static final int MAX_SIMPLEX_ITERATIONS = 100000;
 	
 	public enum RelationsType {
 		BOTH,
 		NECESSARY,
 		POSSIBLE
 	}
 
 	public UTAGMSSolver(RealMatrix perfMatrix) {
 		super(perfMatrix);
 		solver.setMaxIterations(MAX_SIMPLEX_ITERATIONS);
 	}
 	
 	public void solve() throws InfeasibleConstraintsException {
 		solve(RelationsType.BOTH);
 	}
 
 	public void solve(RelationsType rel) throws InfeasibleConstraintsException {
 		necessaryRelation = new Array2DRowRealMatrix(getNrAlternatives(), getNrAlternatives());
 		possibleRelation = new Array2DRowRealMatrix(getNrAlternatives(), getNrAlternatives());
 		List<LinearConstraint> baseConstraints = buildRORConstraints();
 		
 		// check that the set of constraints is feasible
 		try {
 			RealPointValuePair res = solver.optimize(buildObjectiveFunction(), baseConstraints, GoalType.MAXIMIZE, true);
 			if (res.getValue() <= 0.0) {
 				throw new InfeasibleConstraintsException("Preference information leading to infeasible constraints, epsilon <= 0.0");
 			}
 		} catch (OptimizationException e) {
 			throw new InfeasibleConstraintsException("Preference information leading to infeasible constraints: " + e.getMessage());
 		}
 		
 		for (int i=0;i<getNrAlternatives();i++) {
 			for (int j=0;j<getNrAlternatives();j++) {
 				boolean necHolds = false;
 				if (rel.equals(RelationsType.NECESSARY) || rel.equals(RelationsType.BOTH)) {
 					necHolds = solveRelation(i, j, baseConstraints, true);
 					necessaryRelation.setEntry(i, j, necHolds ? 1.0 : 0.0);
 				}
 				if (rel.equals(RelationsType.POSSIBLE) || rel.equals(RelationsType.BOTH)) {
 					if (necHolds) {
 						possibleRelation.setEntry(i, j, 1.0);
 					} else {
 						possibleRelation.setEntry(i, j,
 							solveRelation(i, j, baseConstraints, false)? 1.0 : 0.0);
 					}
 				}
 			}
 		}
 	}
 		
 	public void printModel(boolean necessary, int a, int b) {
 		List<LinearConstraint> constraints = buildRORConstraints();
 		addNecOrPrefConstraint(a, b, necessary, constraints);
 		LinearConstraintHelper.printConstraints(constraints);
 	}
 
 	List<LinearConstraint> buildRORConstraints() {
 		List<LinearConstraint> c = new ArrayList<LinearConstraint>();
 		for (PrefPair p : prefPairs) {
 			c.add(buildStrictlyPreferredConstraint(p.a, p.b));
 		}
 		for (int i=0;i<getNrCriteria();i++) {
 			c.addAll(buildMonotonousConstraints(i));
 		}
 		for (int i=0;i<getNrCriteria();i++) {
 			c.add(buildFirstLevelZeroConstraint(i));
 		}
 		c.add(buildBestLevelsAddToUnityConstraint());
 		c.addAll(buildAllVariablesLessThan1Constraint());
 		
 		return c;
 	}
 
 	private List<LinearConstraint> buildAllVariablesLessThan1Constraint() {
 		List<LinearConstraint> con = new ArrayList<LinearConstraint>();
 		for (int i=0;i<getNrLPVariables();i++) {
 			double[] lhsVars = new double[getNrLPVariables()];		
 			lhsVars[i] = 1.0;
 			con.add(new LinearConstraint(lhsVars, Relationship.LEQ, 1.0));
 		}
 		return con;
 	}
 
 	private LinearConstraint buildBestLevelsAddToUnityConstraint() {
 		double[] vars = new double[getNrLPVariables()];		
 		for (int i=0;i<getNrCriteria();i++) {
 			vars[getConstraintOffset(i) + getLevels()[i].getDimension() - 1] = 1.0;
 		}
 		return new LinearConstraint(vars, Relationship.EQ, 1.0);		
 	}
 
 	private LinearConstraint buildFirstLevelZeroConstraint(int critIndex) {
 		double[] lhsVars = new double[getNrLPVariables()];		
 		lhsVars[getConstraintOffset(critIndex)] = 1.0;
 		return new LinearConstraint(lhsVars, Relationship.EQ, 0.0);
 	}
 
 	private List<LinearConstraint> buildMonotonousConstraints(int critIndex) {
 		List<LinearConstraint> constList = new ArrayList<LinearConstraint>();
 		RealVector levels = getLevels()[critIndex];
 		for (int i=0;i<levels.getDimension()-1;i++) {
 			double[] lhs = new double[getNrLPVariables()];
 			double[] rhs = new double[getNrLPVariables()];
 			lhs[getConstraintOffset(critIndex)+i] = 1.0;
 			rhs[getConstraintOffset(critIndex)+i+1] = 1.0;
 			if (strictValueFunctions) {
 				lhs[lhs.length-1] = 1.0; // epsilon
 			}
 			constList.add(new LinearConstraint(lhs, 0.0, Relationship.LEQ, rhs, 0.0));
 		}
 		return constList;
 	}
 
 	/**
 	 * a is strictly preferred to b (v(a) >= v(b) + epsilon)
 	 * @param a
 	 * @param b
 	 * @return
 	 */
 	private LinearConstraint buildStrictlyPreferredConstraint(int a, int b) {
 		double[] lhsVars = new double[getNrLPVariables()];
 		double[] rhsVars = new double[getNrLPVariables()];
 		setVarsPositive(lhsVars, a);
 		setVarsPositive(rhsVars, b);
 		// set epsilon
 		rhsVars[rhsVars.length-1] = 1.0;
 		return new LinearConstraint(lhsVars, 0.0, Relationship.GEQ, rhsVars, 0.0);
 	}
 
 	private void setVarsPositive(double[] vars, int altIndex) {
 		for (int i=0;i<getNrCriteria();i++) {
 			vars[getConstraintIndex(i, altIndex)] = 1.0;
 		}
 	}
 
 	private int getNrLPVariables() {
 		// utility of all alts + epsilon
 		int sum = 0;
 		for(RealVector vec : getLevels()) {
 			sum += vec.getDimension();
 		}
 		return sum + 1;
 	}
 	
 	private int getConstraintIndex(int critIndex, int altIndex) {
 		assert(critIndex >= 0 && altIndex >= 0);
 		
 		int offset = getConstraintOffset(critIndex);
 		int index = Arrays.binarySearch(getLevels()[critIndex].getData(), perfMatrix.getEntry(altIndex, critIndex));
 		assert(index >= 0); // sanity check
 		
 		return offset + index;
 	}
 
 	private int getConstraintOffset(int critIndex) {
 		int offset = 0;
 		for (int i=0;i<critIndex;i++) {
 			offset += getLevels()[i].getDimension();
 		}
 		return offset;
 	}
 
 	/**
 	 * Check whether relation holds.
 	 * 
 	 * @param i index of first alternative
 	 * @param j index of the second alternative
 	 * @param rorConstraints base constraints E_{ROR}^{A^R}
 	 * @param necessary true if the relation solved is the necessary one, false otherwise
 	 * @return
 	 */
 	private boolean solveRelation(int i, int j, List<LinearConstraint> rorConstraints, boolean necessary) {
 		assert (i >= 0 && j >= 0);
 		
 		if (i==j) {
 			return true;
 		}
 		
 		List<LinearConstraint> constraints = new ArrayList<LinearConstraint>(rorConstraints);
 		addNecOrPrefConstraint(i, j, necessary, constraints);
 		LinearObjectiveFunction goalFunction = buildObjectiveFunction();
 		
 		try {
 			RealPointValuePair res = solver.optimize(goalFunction, constraints, GoalType.MAXIMIZE, true);
 			if (necessary) {
 				return res.getValue() <= 0.0;
 			} else { // possible
 				return res.getValue() > 0.0;
 			}
 			
 		} catch (NoFeasibleSolutionException e) {
 			if (necessary) {
 				return true;
 			} else { // possible
 				return false;
 			}
 		} catch (OptimizationException e) {
 			throw new IllegalStateException("Invalid OptimizationException: " + e.getMessage());
 		}
 	}
 
 	private LinearObjectiveFunction buildObjectiveFunction() {
 		double[] coeff = new double[getNrLPVariables()];
 		coeff[coeff.length-1] = 1.0;
 		LinearObjectiveFunction goalFunction = new LinearObjectiveFunction(coeff, 0.0);
 		return goalFunction;
 	}
 
 	private void addNecOrPrefConstraint(int i, int j, boolean necessary,
 			List<LinearConstraint> constraints) {
 		if (necessary) {
 			constraints.add(buildStrictlyPreferredConstraint(j, i));
 		} else { // possible
 			constraints.add(buildWeaklyPreferredConstraint(i, j));
 		}
 	}
 
 	private LinearConstraint buildWeaklyPreferredConstraint(int a, int b) {
 		double[] lhsVars = new double[getNrLPVariables()];
 		double[] rhsVars = new double[getNrLPVariables()];
 		setVarsPositive(lhsVars, a);
 		setVarsPositive(rhsVars, b);
 		return new LinearConstraint(lhsVars, 0.0, Relationship.GEQ, rhsVars, 0.0);		
 	}
 
 	/**
 	 * PRECOND: solve() executed and returned true
 	 * @return a matrix where >0.0 (1.0) signifies that the relation holds
 	 * @throws IllegalStateException if solve() hasn't been succesfully executed 
 	 */
 	public RealMatrix getNecessaryRelation() throws IllegalStateException {
 		if (necessaryRelation == null) {
 			throw new IllegalStateException("violating PRECOND");
 		}
 		return necessaryRelation;
 	}
 
 	/**
 	 * PRECOND: solve() executed and returned true
 	 * @return a matrix where >0.0 (1.0) signifies that the relation holds
 	 * @throws IllegalStateException if solve() hasn't been succesfully executed 
 	 */
 	public RealMatrix getPossibleRelation() {
 		if (possibleRelation == null) {
 			throw new IllegalStateException("violating PRECOND");
 		}
 		return possibleRelation;
 	}
 
 	public void setStrictValueFunctions(boolean b) {
 		this.strictValueFunctions = b;
 	}
 }
