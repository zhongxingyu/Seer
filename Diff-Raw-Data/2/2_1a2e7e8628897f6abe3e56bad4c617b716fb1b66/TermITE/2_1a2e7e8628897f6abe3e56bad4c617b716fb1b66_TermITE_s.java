 /**
  * Created on 14.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.util.List;
 
 import edu.mit.csail.sdg.alloy4.Pair;
 
 /**
  * an if-then-else term
  * 
  * @author Ulrich Geilmann
  *
  */
 public class TermITE extends Term {
 
 	// the if constraint
 	private Term c;
 	
 	// the then branch
 	private Term t1;
 	// the else branch
 	private Term t2;
 	
 	public TermITE (Term c, Term t1, Term t2) {
 		this.c = c;
 		this.t1 = t1;
 		this.t2 = t2;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public List<Pair<String,String>> getQuantVars() {
 		List<Pair<String,String>> decls = c.getQuantVars();
 		decls.addAll(t1.getQuantVars());
 		decls.addAll(t2.getQuantVars());
 		return decls;
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toString() {
		return "\\if("+c+") \\then("+t1+") \\else("+t2+")";
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toStringTaclet() {
 		return "\\if("+c.toStringTaclet()+") \\then("+t1.toStringTaclet()+") \\else("+t2.toStringTaclet()+")";
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean occurs (String id) {
 		return c.occurs(id) || t1.occurs(id) || t2.occurs(id);
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public Term substitute (String a, String b) {
 		return c.substitute(a,b).ite(t1.substitute(a,b), t2.substitute(a,b));
 	}
 	
 	/** {@inheritDoc} 
 	 * @throws ModelException */
 	@Override
 	public Term fill(Term t) throws ModelException {
 		return c.fill(t).ite(t1.fill(t), t2.fill(t));
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean isInt() {
 		return t1.isInt();
 	}
 	
 }
