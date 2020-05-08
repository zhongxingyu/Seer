 /**
  * Created on 13.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.mit.csail.sdg.alloy4.Pair;
 
 /**
  * A quantified formula (existential/universal)
  * 
  * @author Ulrich Geilmann
  * @author Jonny
  *
  */
 public class TermQuant extends Term {
 
 	public enum Quant {
 		FORALL, EXISTS
 	};
 	
 	private Quant quant;
 	
 	private Term sub;
 
 	/** contains the bound variables for this expression as 
 	 * value pairs in format var/sort.
 	 * a-value = var name / b-value = sort
 	 */
 	private List<Pair<String, String>> vars;
 	
 	/**
 	 * construct a quantified formula 
 	 * @param quantifier
 	 * the quantifier
 	 * @param sort
 	 * the quantification variable's sort
 	 * @param variable
 	 * the quantification variable
 	 * @param sub
 	 * the quantification formula
 	 */
 	public TermQuant (Quant quantifier, String sort, String variable, Term sub) {
 		this.quant = quantifier;
 		vars = new LinkedList<Pair<String, String>>();
 		vars.add(new Pair<String, String>(variable, sort));
 		this.sub = sub;
 	}
 	
 	/**
 	 * construct a quantified formula 
 	 * @param quantifier
 	 * the quantifier
 	 * @param typedVars
 	 * the quantified variables as a list of name/type pairs. 
 	 * @param sub
 	 * the quantification formula
 	 */
 	public TermQuant (Quant quantifier, List<Pair<String, String>> typedVars, Term sub) {
 		this.quant = quantifier;
 		this.vars = typedVars;
 		this.sub = sub;
 	}
 
 	@Override
 	public String toString() {
 		StringBuffer buf = new StringBuffer();
 		buf.append(getFormattedComment());
 		switch (quant) {
 		case FORALL:
 			buf.append ("(forall (");
 			break;
 		case EXISTS:
 			buf.append ("(exists (");
 			break;
 		}
 		String var, sort;
 		for(Pair<String, String> typedVar : vars){
 			var = typedVar.a;
 			sort = typedVar.b;
 			buf.append("(");
 			buf.append(var).append(" ");
 			buf.append(sort);
 			buf.append(")");
 		}
 		buf.append(") ");
 		buf.append(sub.toString()).append(")");
 		return buf.toString();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public String toStringTaclet() {	// TODO: make smt-ready => delete
 		StringBuffer buf = new StringBuffer();
 		switch (quant) {
 		case FORALL:
 			buf.append ("\\forall ");
 			break;
 		case EXISTS:
 			buf.append ("\\exists ");
 			break;
 		}
 //		buf.append(var).append("; (").append(sub.toStringTaclet()).append(")");
 		return buf.toString();
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public List<Pair<String,String>> getQuantVars() {
 		List<Pair<String,String>> decls = sub.getQuantVars();
 		decls.addAll(vars);
 		return decls;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public boolean occurs (String id) {		
 		for(Pair<String, String> typedVar : vars){
 			if (id.equals(typedVar.a))
 				return true;
 		}
 		return sub.occurs(id);
 	}
 	
 	/** {@inheritDoc}
 	 *  (will not substitute bound variables) 
 	 **/
 	@Override
 	public Term substitute (String a, String b) {
 		if (occurs(a))
 			return this;
 		// return a copy
 		return new TermQuant(quant, new LinkedList<Pair<String,String>>(vars) ,sub.substitute(a,b));
 	}
 	
 	/** {@inheritDoc} 
 	 * @throws ModelException */
 	@Override
 	public Term fill(Term t) throws ModelException {
 		// return a copy
 		return new TermQuant(quant,new LinkedList<Pair<String,String>>(vars),sub.fill(t));
 	}
 
 	/** {@inheritDoc} */
 	@Override
 	public boolean isInt() {
 		return false;
 	}
 
 	public static Term createSortedTerm(Quant quantifier, List<TermVar> vars, Term sub) {
 		if(sub == TRUE)
 			return sub;
 		else {
 			List<Pair<String, String>> typedVars = new LinkedList<Pair<String,String>>();
 			for(TermVar var : vars){
 				typedVars.add(new Pair<String, String>(var.getName(), var.getSort()));
 			}
 			if (sub instanceof TermQuant && ((TermQuant) sub).quant == quantifier) {
 				// if the quantifiers are equal, just extend the variable list
 				TermQuant q = (TermQuant) sub;
 				typedVars.addAll(q.vars);
 				return new TermQuant(quantifier, typedVars, q.sub);
 			}
 			else {
 				return new TermQuant(quantifier, typedVars, sub);
 			}
 		}
 	}	
 }
