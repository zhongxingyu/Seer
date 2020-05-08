 package edu.kit.asa.alloy2relsmt.smt;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.kit.asa.alloy2relsmt.smt.TermQuant.Quant;
 import edu.mit.csail.sdg.alloy4.Pair;
 
 /** represents a SMT expression **/
 public abstract class Term implements Comparable<Term> {
 	
 	@Override
 	public int compareTo(Term o) {
		return this.hashCode() - o.hashCode();
 	}
 	
 	@Override
 	public int hashCode() {
 		return this.toString().hashCode();
 	}
 
 	protected String comment = null;
 	
 	protected String getFormattedComment()
 	{
 		if (comment != null) {
 			return "; " + comment + "\n";
 		}
 		else {
 			return "";
 		}
 	}
 	
 	/**
 	 * check whether a variable occurs in this term
 	 * @param id
 	 * the identifier to search for
 	 * @return
 	 * true iff a variable of name <code>id</code> occurs
 	 * in the term.
 	 */
 	public abstract boolean occurs (String id);
 	
 	/**
 	 * perform substitution
 	 * @param a
 	 * the variable to be replaced
 	 * @param b
 	 * substitute a with b
 	 * @return
 	 * the term with a replaced with b
 	 */
 	public abstract Term substitute (String a, String b);
 	
 	/**
 	 * get all quantification variables used in this term.
 	 * @return
 	 * a list of pairs of sort and name of the quantification variables.
 	 */
 	public List<Pair<String,String>> getQuantVars() {
 		return new LinkedList<Pair<String,String>>();
 	}
 	
 	/**
 	 * prints this term (uses <code>Object.toString()</code>)
 	 * @return
 	 * String representation of the term
 	 */
 	@Override
 	public abstract String toString(); // printing make children print themselves
 
 	/**
 	 * @return
 	 * String representation of the term, for use
 	 * within a taclet. i.e. quantification variables
 	 * have to be declared as schema variables
 	 */
 	public abstract String toStringTaclet();
 	
 	/*
 	 * Convenience methods for building complex terms
 	 * ----------------------------------------------
 	 */
 	
 	/**
 	 * @return
 	 * formula representing <code>this | right</code>
 	 */
 	public Term or (Term right) {
 		if (right == FALSE)
 			return this;
 		return new TermBinOp (this,TermBinOp.Op.OR,right);
 	}
 
 	/**
 	 * @return
 	 * formula representing <code>this & right</code>
 	 */
 	public Term and (Term right) {
 		if (right == TRUE)
 			return this;
 		return new TermBinOp (this,TermBinOp.Op.AND,right);
 	}
 
 	/**
 	 * @return
 	 * formula representing <code>this -> right</code>
 	 */
 	public Term implies (Term right) {
 		if (right == TRUE)
 			return TRUE;
 		return new TermBinOp (this,TermBinOp.Op.IMPLIES,right);
 	}
 
 	/**
 	 * @return
 	 * formula representing <code>this <-> right</code>
 	 */
 	public Term iff (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.IFF,right);
 	}
 	
 	/**
 	 * @return
 	 * formula representing <code>this = right</code>
 	 */
 	public Term equal (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.EQUALS,right);
 	}
 
 	/**
 	 * @return formula representing <code>this < right</code>
 	 */
 	public Term lt (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.LT,right);
 	}
 	
 	/**
 	 * @return formula representing <code>this > right</code>
 	 */
 	public Term gt (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.GT,right);
 	}
 
 	/**
 	 * @return formula representing <code>this <= right</code>
 	 */
 	public Term lte (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.LTE,right);
 	}
 
 	/**
 	 * @return formula representing <code>this >= right</code>
 	 */
 	public Term gte (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.GTE,right);
 	}
 
 	/**
 	 * @return formula representing <code>this - right</code>
 	 */
 	public Term minus (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.MINUS,right);
 	}
 	
 	/**
 	 * @return formula representing <code>this + right</code>
 	 */
 	public Term plus (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.PLUS,right);
 	}
 	
 	/**
 	 * @return formula representing <code>this * right</code>
 	 */
 	public Term mul (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.MUL,right);
 	}
 	
 	/**
 	 * @return formula representing <code>this / right</code>
 	 */
 	public Term div (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.DIV,right);
 	}
 	
 	/**
 	 * @return formula representing <code>this % right</code>
 	 */
 	public Term rem (Term right) {
 		return new TermBinOp (this,TermBinOp.Op.REM,right);
 	}
 
 	//TODO missing binops
 	
 	/**
 	 * @return
 	 * formula representing <code>\forall sort var; this</code>
 	 */
 	public Term forall (String sort, String var) {
 		return forall(sort, var, this);
 	}
 	
 	/**
 	 * @return
 	 * formula representing <code>\exists sort var; this</code>
 	 */
 	public Term exists (String sort, String var) {
 		return exists(sort, var, this);
 	}
 
 	/**
 	 * @return
 	 * formula representing <code>!this</code>
 	 */
 	public Term not() {
 		return new TermUnary(TermUnary.Op.NOT,this);
 	}
 	
 	/**
 	 * @return
 	 * term representing <code>(ite this sub1 sub2)</code>
 	 */
 	public Term ite(Term sub1, Term sub2) {
 		return new TermITE(this,sub1,sub2);
 	}
 	
 	/**
 	 * @return
 	 * formula representing <code>compr{vars[0];}(bind{vars[1];}...(this))</code>
 	 */
 	public Term compr(String... vars) {
 		return new TermCompr(this,vars);
 	}
 	
 	/*
 	 * Convenience methods for initial creation of terms
 	 */
 	public static final Term FALSE = new Term.False();
 	public static final Term TRUE = new Term.True();
 	public static final Term HOLE = new Hole();
 	
 	/**
 	 * make this name into a (fun/pred) call
 	 * @param name of the function
 	 * @param params one or more parameters for this function
 	 * @throws ModelException 
 	 */
 	public static Term call(String name, Term... params) throws ModelException {
 		if (Arrays.asList(params).contains(null)) {
 			throw new ModelException("Something went wrong creating a call. Parameters cannot be null.");
 		}
 		return new TermCall (name, params);
 	}
 	
 	/** make a variable **/
 	public static TermVar var(String name) {
 		return new TermVar (name);
 	}
 	
 	/** make a "for all" expression **/
 	public static Term forall(String sort, String var, Term sub) {
 		TermVar sortedVar = TermVar.var(sort, var);
 		List<TermVar> varlist = new LinkedList<TermVar>();
 		varlist.add(sortedVar);
 		return TermQuant.createSortedTerm(Quant.FORALL, varlist, sub);
 	}
 	
 	/** make an "exists" expression **/
 	public static Term exists(String sort, String var, Term sub) {
 		TermVar sortedVar = TermVar.var(sort, var);
 		List<TermVar> varlist = new LinkedList<TermVar>();
 		varlist.add(sortedVar);
 		return TermQuant.createSortedTerm(Quant.EXISTS, varlist, sub);
 	}
 	
 	/** make an expression which represents an integer **/
 	public static Term number(int n) {
 		return new TermNumber(n);
 	}
 	
 	/**
 	 * @return <code>true</code> iff this term is of type int
 	 */
 	public abstract boolean isInt();
 	
 	/**
 	 * fill all occurrences of <code>HOLE</code> with <code>t</code>
 	 * @throws ModelException 
 	 */
 	public abstract Term fill(Term t) throws ModelException;
 	
 	/**
 	 * Placeholder in a Term. Can later be replaced with
 	 * any term using <code>fill</code>
 	 */
 	public static final class Hole extends Term {
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean occurs(String id) {
 			return false;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term substitute(String a, String b) {
 			return this;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public String toStringTaclet() {
 			throw new RuntimeException ("Unexpected operation! This term is a hole. Holes cannot be expressed as taclet.");
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term fill(Term t) {
 			return t;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public boolean isInt() {
 			return false;
 		}
 
 		@Override
 		public String toString() {
 			// holes can probably not be printed in SMT syntax
 			throw new RuntimeException ("Unexpected operation! This term is a hole. Holes cannot be expressed as strings.");
 		}
 
 	}
 	
 	/** representation of a TRUE literal **/
 	public static final class True extends Term {
 
 		/** {@inheritDoc} */
 		@Override
 		public String toStringTaclet() {
 			return "true";
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public String toString() {
 			return "true";
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term or(Term right) {
 			return this;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public Term implies(Term right) {
 			return right;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term and(Term right) {
 			return right;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public boolean occurs (String id) {
 			return false;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term substitute (String a, String b) {
 			return this;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public Term fill(Term t) {
 			return this;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public Term forall (String sort, String var) {
 			return this;
 		}
 	
 		/** {@inheritDoc} */
 		@Override
 		public boolean isInt() {
 			return false;
 		}
 		
 	}
 	
 	/** representation of a FALSE literal **/
 	public static final class False extends Term {
 
 		/** {@inheritDoc} */
 		@Override
 		public String toStringTaclet() {
 			return "false";
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public String toString() {
 			return "false";
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term or(Term right) {
 			return right;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term and(Term right) {
 			return this;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public boolean occurs (String id) {
 			return false;
 		}
 
 		/** {@inheritDoc} */
 		@Override
 		public Term substitute (String a, String b) {
 			return this;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public Term fill(Term t) {
 			return this;
 		}
 		
 		/** {@inheritDoc} */
 		@Override
 		public boolean isInt() {
 			return false;
 		}
 		
 	}
 
 	/** wrap this expression in a single sorted "forall" expression
 	 * 
 	 * @param sort the bounding sort
 	 * @param vars the variables, elements of the bounding sort
 	 * @return this expression, quantified with respect to sort and vars
 	 */
 	public Term forall(String sort, TermVar[] vars) {
 		return forall(sort, vars, this);
 	}
 
 	/** create a single sorted "forall" expression
 	 * 
 	 * @param sort the bounding sort
 	 * @param vars the variables, elements of the bounding sort
 	 * @param sub expression to quantify
 	 * @return the sub expression, quantified with respect to sort and vars
 	 */
 	public static Term forall(String sort, TermVar[] vars, Term sub) {
 		List<TermVar> varList = new LinkedList<TermVar>();
 		for (TermVar termVar : vars) {
 			varList.add(TermVar.var(sort, termVar.getName()));
 		}
 		return TermQuant.createSortedTerm(Quant.FORALL, varList, sub);
 	}	
 
 	/** Creates an "in()" expression. 
 	 * @param bound The bounding expression which contains the tuple 
 	 * @param atoms Any number of atom variables, or an array of atom variables (aka tuple)
 	 * @return an expression of the form (in atoms[0] atoms[1] atoms[2..etc] bound)
 	 * @throws ModelException 
 	 * @remark It's called "reverse" because the parameter order is the other way around. 
 	 * This is a technical neccessity, since I wanted to use varargs.  
 	 */
 	public static Term reverseIn(Term bound, TermVar... atoms) throws ModelException {
 		int arity = atoms.length;
 		Term[] params = new Term[arity + 1];
 		for(int i = 0; i < arity; i++){
 			params[i] = atoms[i];			
 		}
 		params[arity] = bound;
 		return call("in_" + arity, params);
 	}
 
 	/** Creates an "in()" expression.
 	 * @param atoms a list of atom variables (aka tuple)
 	 * @param bound The bounding expression which contains the tuple 
 	 * @return an expression of the form (in atoms[0] atoms[1] atoms[2..etc] bound)
 	 * @throws ModelException 
 	 */
 	public static Term in(List<TermVar> atoms, Term bound) throws ModelException {
 		return reverseIn(bound, (TermVar[]) atoms.toArray(new TermVar[atoms.size()]));
 	}
 
 	/** creates a multi sorted "forall" expression. requires all vars to be well-sorted
 	 * 
 	 * @param vars a list of well-sorted variables
 	 * @param sub term to bind to
 	 * @return
 	 */
 	public static Term forall(List<TermVar> vars, Term sub) {
 		return TermQuant.createSortedTerm(Quant.FORALL, vars, sub);
 	}
 
 	/** wrap this expression in a "forall" expression
 	 * 
 	 * @param vars well-sorted variables to bind this to
 	 * @return an expression representing "forall (vars) this"
 	 */
 	public Term forall(TermVar... vars) {				
 		return forall(Arrays.asList(vars), this);
 	}
 
 	/** wrap this expression in an "exists" expression
 	 * 
 	 * @param vars well-sorted variables to bind this to
 	 * @return an expression representing "exists (vars) this"
 	 */
 	public Term exists(TermVar... vars) {
 		return exists(Arrays.asList(vars), this);
 	}
 
 	/** creates a multi sorted "exists" expression. requires all vars to be well-sorted
 	 * 
 	 * @param vars a list of well-sorted variables
 	 * @param sub term to bind to
 	 * @return
 	 */
 	private Term exists(List<TermVar> vars, Term sub) {
 		return TermQuant.createSortedTerm(Quant.EXISTS, vars, sub);
 	}
 
 	public Term exists(List<TermVar> vars) {
 		return exists(vars, this);
 	}
 
 	public Term forall(List<TermVar> vars) {
 		return forall(vars, this);
 	}
 
 	public static Term exists(TermVar[] vars, Term sub) {
 		return TermQuant.createSortedTerm(Quant.EXISTS, Arrays.asList(vars), sub);
 	}
 
 	public static Term call(String name, List<TermVar> params) throws ModelException {
 		TermVar[] paramArray = new TermVar[params.size()];
 		return call(name, params.toArray(paramArray));
 	}
 	
 	public void setComment(String comment){
 		this.comment = comment;
 	}
 }
