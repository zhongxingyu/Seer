 /**
  * Created on 07.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import edu.kit.asa.alloy2key.key.TermBinOp.Op;
 import edu.kit.asa.alloy2key.modules.KeYModule;
 import edu.kit.asa.alloy2key.util.Util;
 
 /**
  * capturing output to a .key file
  * 
  * @author Ulrich Geilmann
  *
  */
 public class KeYFile {
 
 	public KeYFile () {
 		includes = new LinkedList<String>();
 		sorts = new LinkedList<String>();
 		funcs  =  new LinkedList<String>();
 		preds  = new LinkedList<String>();
 		rules  = new LinkedList<Taclet>();
 		assump = new LinkedList<Term>();
 		concl  = new LinkedList<Term>();
 		asserts = new LinkedList<Term>();
 	}
 	
 	/** referred modules */
 	public Queue<KeYModule> modules = KeYModule.NIL;
 	
 	/**
 	 * Add a function declaration (free text)
 	 * @param fun
 	 * declaration in SMT syntax, e.g. (declare-fun MyFun bool);
 	 */
 	public void addFunction (String fun) {
 		funcs.add(fun);
 	}
 	
 	/**
 	 * Add a sort declaration (free text)
 	 * @param sortDeclaration
 	 * declaration in SMT/Z3 syntax, e.g. (declare-sort MySort);
 	 * @return TRUE if successfully added, FALSE if omitted
 	 */
 	public boolean addSort (String sortDeclaration) {
 		if(!sorts.contains(sortDeclaration)){
 			sorts.add(sortDeclaration);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Add a typed function declaration with parameters
 	 * @return TRUE if successfully added, FALSE if omitted
 	 * @throws ModelException 
 	 */
 	public boolean addFunction(String type, String name, String...params) {		
 		String checkedParams = params == null ? "" : Util.join(params, " ");
 		String fun = String.format("(declare-fun %s (%s) %s)", name, checkedParams, type);
 		if(!funcs.contains(fun)){
 			funcs.add(fun);
 			return true;
 		}
 		else {
 			return false;
 		}
 	}
 	
 	/** 
 	 * Add an SMT assertion (assert expression)
 	 * @param term 
 	 * Expression to be asserted (declare in SMT syntax)
 	 */
 	public void addAssertion(Term term) {
 		asserts.add(term);
 	}	
 	
 	/**
 	 * Add a predicate declaration
 	 * @param pred
 	 * declaration in KeY syntax, e.g. p(Rel1);
 	 */
 	public void addPredicate (String pred) {
 		preds.add(pred);
 	}
 	
 	/**
 	 * Add a rule definition
 	 * @param rule
 	 * the taclet object
 	 */
 	public void addRule (Taclet rule) {
 		rules.add(rule);
 	}
 	
 	/**
 	 * Add an assumption
 	 * @param f
 	 * formula in KeY syntax to be added as assumption
 	 */
 	public void addAssumption (Term f) {
 		if (f != Term.TRUE)
 			assump.add(f);
 	}
 	
 	/**
 	 * Add a conclusion
 	 * @param f
 	 * formula in KeY syntax to be added as conclusion
 	 */
 	public void addConclusion (Term f) {
 		concl.add(f);
 	}
 	
 	/**
 	 * Add an include statement
 	 * @param f
 	 * the filename to include
 	 */
 	public void addInclude (String f) {
 		includes.add(f);
 	}
 	
 	private Collection<String> sorts;
 	private Collection<Term> asserts;
 	private Collection<String> includes;
 	private Collection<String> funcs;
 	private Collection<String> preds;
 	private Collection<Taclet> rules;
 	private Collection<Term> assump;
 	private Collection<Term> concl;
 	
 	public void output(OutputStream os) {
 		PrintStream out = new PrintStream(os);
 		//printTheory(out);
 //		out.println ("\\include \"theory/alloyHeader.key\";");
 //		for (String s : includes)
 //			out.println ("\\include \""+s+"\";");
 //		for (KeYModule m: modules) {
 //			out.println ("\\include \"theory/"+m.filename()+"\";");
 //		}
 		out.println (";; sorts");
 		out.println (Util.join(sorts, "\n"));
 		out.println (";; --end sorts\n");
 		out.println (";; functions");
 		out.println (Util.join(funcs, "\n"));
 		out.println (";; --end functions\n");
 		out.println (";; assertions");
 		for (Term a : asserts) {
 			out.println (String.format("(assert\n  %s\n)", a.toString()));
 		}
 		out.println (";; --end assertions\n");
 		
 		
 		out.println (";; -- key stuff for debugging --");
 //		out.println ("\\rules {");
 //		out.println (Util.join(rules, "\n"));
 //		out.println ("}\n");
 		out.println (";\\problem {(");
 		out.print(";");
 		out.println (Util.join(assump, " &\n"));
 		out.println (";)-> (");
 		out.print(";");
 		out.println (Util.join(concl, " &\n"));
 		out.println (";;\\predicates {");
 		out.println (Util.join(preds, "\n"));
 		out.println (";;}\n");
 		out.println (";; -- END key stuff --");
 		
 		out.println ("(check-sat)");
 		out.close();
 	}
 
 	/** adds declaration and theory for Atom */
 	public void declareAtom() {
 		this.addSort("(declare-sort Atom)");
 	}
 	
 	/** adds a declaration and theory for disjoint 
 	 * @param arity arity of the expression
 	 */
 	public void declareDisjoint(int ar) {
 		// TODO add declaration
 		// TODO: add axiom		
 	}
 
 	/** adds a declaration and theory for subset and subrel 
 	 * @param arity of the set expression
 	 */
 	public void declareSubset(int ar) {
 		// declare prerequisite sorts
 		declareAtom();
 		declareRel(ar);
 		// declare subset function, e.g. (declare-fun subset_2 (Rel2, Rel2) bool)
 		// add declaration
 		String relSort = "Rel"+ar;
 		if(this.addFunction("Bool", "subset_" + ar, relSort, relSort)){			
 			// if successfully added; add axiom(s) as well
 			// prepare parameter list for in()
 			List<TermVar> atomVars = new LinkedList<TermVar>();
 			for(int i = 0; i < ar; i++){
 				atomVars.add(TermVar.var("Atom", "a" + i));
 			}
 			//TODO: add axiom for subset
 			TermVar x = TermVar.var(relSort, "x");	// a Set or Relation
 			TermVar y = TermVar.var(relSort, "y");	// another Set or Relation
 			Term subset = Term.call("subset_"+ar, x, y);
 			Term inImpliesIn = new TermBinOp(Term.in(x, atomVars), Op.IMPLIES, Term.in(y, atomVars));
 			List<TermVar> quantVars = new LinkedList<TermVar>(atomVars);
 			quantVars.add(x);
 			quantVars.add(y);
 			Term axiom = Term.forall(quantVars, (Term)new TermBinOp(subset, Op.IFF, inImpliesIn));
 		}
 	}
 
 	private void declareRel(int ar) {
 		this.addSort("(declare-sort Rel" + ar + ")");
 	}
 
 	/** adds a declaration and theory for the converter function 
 	 * @param arity arity of the expression
 	 */
 	public void declareA2r(int ar) {
 		// declare prerequisite sorts and functions
 		declareAtom();
 		declareRel(ar);
 		declareIn(ar);
 		// prepare declaration
 		String[] params = new String[ar];
 		for(int i = 0; i < ar; i++){
 			params[i] = "Atom";
 		}
 		// declaration		
 		if(this.addFunction("Rel" + ar, "a2r_" + ar, params)){
 			// TODO: axiom of higher arity
 			// axiom 
 			Term xInX, inImpliesEqual;
 			Term x = TermVar.var("x");
 			Term y = TermVar.var("y");
 			
 			xInX = Term.call("in_1", x, Term.call("a2r_" + ar, x));
 			inImpliesEqual = new TermBinOp(Term.call("in_1", y, Term.call("a2r_" + ar, x)), Op.IMPLIES, new TermBinOp(x, Op.EQUALS, y));
 			Term axiom = Term.forall("Atom", "x", xInX.and(Term.forall("Atom", "y", inImpliesEqual)));
 					
 			this.addAssertion(axiom);
 		}	
 	}
 
 	/** adds a declaration for in (no theory) 
 	 * @param arity arity of the expression
 	 * @throws ModelException couldn't declare this add this function because of "addFunction"
 	 */
 	public void declareIn(int ar)   {
 		declareAtom();
 		declareRel(ar);
 		String[] params = new String[ar + 1];		
 		for(int i = 0; i < ar; i++)
 			params[i] = "Atom";
 		params[ar]  = "Rel" + ar;
 		this.addFunction("Bool", "in_" + ar, params);
 		// axiom omitted: "in" does not have an axiom (uninterpreted)
 	}
 
 	/** adds a declaration and theory for "none" 
 	 * @param arity arity of none
 	 */
 	public void declareNone(int ar) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareProduct(int i, int j) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareJoin(int lar, int rar) {
 		this.addFunction("Bool", "join_" + lar + "x" + rar);
 		// TODO: add axiom
 	}
 
 	public void declareUnion(int ar) {
 		declareRel(ar);
 		this.addFunction("Rel" + ar, "union_" + ar);	
 		// TODO: add axiom
 	}
 
 	public void declareOne(int ar)  {		
 		declareRel(ar);
 		this.addFunction("Bool", "one_" + ar, "Rel" + ar);
 		// TODO: add axiom
 	}
 
 	public void declareLone(int ar) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareSome(int ar) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	// arity is always 2
 	public void declareTranspose() {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareTransitiveClosure() {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareReflexiveTransitiveClosure() {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareCardinality(int ar) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	/** Declares the domain restriction operator
 	 * @param rar arity of the right-hand parameter
 	 */
 	public void declareDomainRestriction(int rar) {
 		// left-hand arity is required to be 1 (type:set)
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void declareDifference(int ar1) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareOverride(int ar1) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareIntersection(int ar1) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareRangeRestriction(int ar1) {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 
 	public void declareIdentity() {
 		// TODO add declaration
 		//TODO: add axiom
 	}
 }
