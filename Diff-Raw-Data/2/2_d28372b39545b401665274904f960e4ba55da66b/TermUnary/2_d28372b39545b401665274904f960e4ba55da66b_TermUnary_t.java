 /**
  * Created on 13.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.util.List;
 
 import edu.mit.csail.sdg.alloy4.Pair;
 
 /**
  * term constructed from a unary operator 
  * 
  * @author Ulrich Geilmann
  *
  */
 public class TermUnary extends Term {
 	
 	public enum Op {
 		NOT
 	};
 	
 	// the operator
 	private Op operator;
 	// the sub term
 	private Term sub;
 	
 	public TermUnary (Op op, Term sub) {
 		this.operator = op;
 		this.sub = sub;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public List<Pair<String,String>> getQuantVars() {
 		return sub.getQuantVars();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toString() {
 		switch (operator) {
 		case NOT:
			return (comment == null ? "" : "; " + this.comment + "\n") +
 					"(not "+sub.toString()+")";    // smt ok
 		default:
 			return "";
 		}
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toStringTaclet() {    // todo: remove taclets
 		switch (operator) {
 		case NOT:
 			return "!("+sub.toStringTaclet()+")";
 		default:
 			return "";
 		}
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean occurs (String id) {
 		return sub.occurs(id);
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Term substitute (String a, String b) {
 		return new TermUnary(operator,sub.substitute(a,b));
 	}
 	
 	/** {@inheritDoc} 
 	 * @throws ModelException */
 	@Override
 	public Term fill(Term t) throws ModelException {
 		return new TermUnary(operator,sub.fill(t));
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean isInt() {
 		return false;
 	}
 	
 }
