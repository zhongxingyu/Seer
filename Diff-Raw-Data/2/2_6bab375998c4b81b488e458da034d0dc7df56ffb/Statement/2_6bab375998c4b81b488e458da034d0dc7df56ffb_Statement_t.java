 package mepk.kernel;
 
 import static mepk.kernel.Expression.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 
 /**
  * This class represents an statement. Statements are values: they cannot be
  * modified after they have been created. They are {@link #equals(Object) equal}
  * if (and only if) they have the same structure. It is only possible to create
  * an instance using the static methods in this class.
  */
public final class Statement {
 
 	/**
 	 * Create a new instance with an empty set of DVRs.
 	 * 
 	 * @param hypotheses
 	 *            the hypotheses
 	 * @param conclusion
 	 *            the conclusion
 	 * @return the created statement
 	 */
 	public static Statement Stat(List<Expression> hypotheses, Expression conclusion) {
 		return Stat(DVRSet.EMPTY, hypotheses, conclusion);
 	}
 
 	/**
 	 * Create a new instance.
 	 * 
 	 * @param dvrs
 	 *            the DVRs
 	 * @param hypotheses
 	 *            the hypotheses
 	 * @param conclusion
 	 *            the conclusion
 	 * 
 	 * @return the created statement
 	 */
 	public static Statement Stat(DVRSet dvrs, List<Expression> hypotheses, Expression conclusion) {
 		return new Statement(dvrs, new HashSet<Expression>(hypotheses), conclusion);
 	}
 
 	private final DVRSet dvrs;
 	private final Set<Expression> hypotheses;
 	private final Expression conclusion;
 
 	/*
 	 * The constructor is not exposed, to allow future performance improvements
 	 * without breaking the interface.
 	 */
 	private Statement(DVRSet dvrs, Set<Expression> hypotheses, Expression conclusion) {
 		this.dvrs = dvrs;
 		this.hypotheses = hypotheses;
 		this.conclusion = conclusion;
 
 		// check that every var has _at least_ one type
 		Set<String> typedVarNames = new HashSet<String>();
 		for (Expression hyp : hypotheses) {
 			App f = hyp.asApp();
 			if (f != null && f.getConstName().equals("")) {
 				List<Expression> args = f.getSubexpressions();
 				if (args.size() > 0) {
 					Var tv = args.get(0).asVar();
 					if (tv != null) {
 						typedVarNames.add(tv.getVarName());
 					}
 				}
 			}
 		}
 		if (!typedVarNames.containsAll(getVarNames())) {
 			throw new MEPKException(String.format("Ill-typed statement: all of %s should be typed, but only %s are.",
 					getVarNames(), typedVarNames));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#hashCode()
 	 */
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((conclusion == null) ? 0 : conclusion.hashCode());
 		result = prime * result + ((dvrs == null) ? 0 : dvrs.hashCode());
 		result = prime * result + ((hypotheses == null) ? 0 : hypotheses.hashCode());
 		return result;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#equals(java.lang.Object)
 	 */
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj) {
 			return true;
 		}
 		if (obj == null) {
 			return false;
 		}
 		if (!(obj instanceof Statement)) {
 			return false;
 		}
 		Statement other = (Statement) obj;
 		if (conclusion == null) {
 			if (other.conclusion != null) {
 				return false;
 			}
 		} else if (!conclusion.equals(other.conclusion)) {
 			return false;
 		}
 		if (dvrs == null) {
 			if (other.dvrs != null) {
 				return false;
 			}
 		} else if (!dvrs.equals(other.dvrs)) {
 			return false;
 		}
 		if (hypotheses == null) {
 			if (other.hypotheses != null) {
 				return false;
 			}
 		} else if (!hypotheses.equals(other.hypotheses)) {
 			return false;
 		}
 		return true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return "Statement [hypotheses=" + hypotheses + ", conclusion=" + conclusion + ", dvrs=" + dvrs + "]";
 	}
 
 	/**
 	 * Create a new statement, by replacing one variable in this statement by an
 	 * expression, and optionally adding type expressions (to make resulting
 	 * statement type-correct).
 	 * 
 	 * @param varName
 	 *            the variable name
 	 * @param replacement
 	 *            the replacement expression
 	 * @param typesOfNewVars
 	 *            the additional type expressions
 	 * @return the new statement
 	 */
 	public Statement substitute(String varName, Expression replacement, Map<String, Expression> typesOfNewVars) {
 		List<Expression> allHyps = new ArrayList<Expression>();
 		for (Entry<String, Expression> e : typesOfNewVars.entrySet()) {
 			Expression typeHyp = App("", Var(e.getKey()), e.getValue());
 			allHyps.add(typeHyp);
 		}
 		for (Expression hyp : hypotheses) {
 			allHyps.add(hyp.substitute(varName, replacement));
 		}
 		Expression c = conclusion.substitute(varName, replacement);
 		DVRSet d = dvrs.substitute(varName, replacement.getVarNames());
 		return Stat(d, allHyps, c);
 	}
 
 	/**
 	 * Create a new statement, by adding hypotheses and DVRs to this statement.
 	 * 
 	 * @param addedDVRs
 	 *            the added DVRs
 	 * @param addedHypotheses
 	 *            the added hypotheses
 	 * 
 	 * @return the new statement
 	 */
 	public Statement weaken(DVRSet addedDVRs, Expression... addedHypotheses) {
 		List<Expression> allHyps = new ArrayList<Expression>(hypotheses);
 		allHyps.addAll(Arrays.asList(addedHypotheses));
 		DVRSet d = dvrs.add(addedDVRs);
 		return Stat(d, allHyps, conclusion);
 	}
 
 	private Set<String> getVarNames() {
 		Set<String> result = new HashSet<String>();
 		for (Expression h : hypotheses) {
 			h.addVarNamesTo(result);
 		}
 		conclusion.addVarNamesTo(result);
 		return result;
 	}
 }
