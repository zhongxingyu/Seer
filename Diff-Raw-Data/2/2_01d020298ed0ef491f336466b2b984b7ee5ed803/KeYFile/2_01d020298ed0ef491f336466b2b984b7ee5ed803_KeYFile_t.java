 /**
  * Created on 07.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.io.OutputStream;
 import java.io.PrintWriter;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import sun.reflect.generics.reflectiveObjects.NotImplementedException;
 
 import edu.kit.asa.alloy2key.modules.KeYModule;
 import edu.kit.asa.alloy2key.util.Util;
 import edu.mit.csail.sdg.alloy4.ast.Sig;
 
 /**
  * capturing output to an .SMT file
  * 
  * @author Ulrich Geilmann
  * @author Jonny
  * @author Aboubakr Achraf El Ghazi
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
 		lemmas = new LinkedList<Term>();
 		axioms = new LinkedList<Term>();
 		cmdasserts = new LinkedList<Term>();
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
 		// we don't need to assert the expression "TRUE"
 		if (!term.equals(Term.TRUE)) {
 			asserts.add(term);
 		}
 	}	
 	
 	/** 
 	 * Add an SMT assertion (assert expression)
 	 * @param term 
 	 * Expression to be asserted (declare in SMT syntax)
 	 */
 	public void addCmdAssertion(Term term) {
 		cmdasserts.add(term);		
 	}	
 	
 	/**
 	 * Add a predicate declaration
 	 * @param pred
 	 * declaration in smt syntax, e.g. (declare-fun p (Rel1) bool)
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
 	
 	/** 
 	 * Add an SMT assertion as "lemma" (assert expression)
 	 * @param term 
 	 * Expression to be asserted (declare in SMT syntax)
 	 * @comment 
 	 * there is no difference in SMT between lemmas and normal assertions. 
 	 * lemmas are added by the converter to help the SMT solver find a solution.
 	 */
 	public void addLemma(Term term){
 		lemmas.add(term);
 	}
 	
 	private Collection<String> sorts;
 	private Collection<Term> asserts;
 	private Collection<String> includes;
 	private Collection<String> funcs;
 	private Collection<String> preds;
 	private Collection<Taclet> rules;
 	private Collection<Term> assump;
 	private Collection<Term> concl;
 	private Collection<Term> lemmas;
 	private Collection<Term> axioms;
 	private Collection<Term> cmdasserts;
 	
 	public void output(OutputStream os) {
 		PrintWriter out = new PrintWriter(os);
 		//printTheory(out);
 //		out.println ("\\include \"theory/alloyHeader.key\";");
 //		for (String s : includes)
 //			out.println ("\\include \""+s+"\";");
 //		for (KeYModule m: modules) {
 //			out.println ("\\include \"theory/"+m.filename()+"\";");
 //		}
 		out.println ("(set-logic AUFLIA)\n(set-option :macro-finder true)");
 		
 		// Only for sanity checks
 		out.println ("(set-option :produce-unsat-cores true)");
 		
 		//
 		out.println (";; sorts");
 		out.println (Util.join(sorts, "\n"));
 		out.println (";; --end sorts\n");
 		out.println (";; functions");
 		out.println (Util.join(funcs, "\n"));
 		out.println (";; --end functions\n");
 		out.println (";; axioms");
 		
 		int i = 0;
 		for (Term a : axioms) {
 			out.println (String.format("(assert \n (! \n  %s \n %s \n ) \n )", a.toString(), ":named ax" + i++));
 		}
 		
 		out.println (";; --end axioms\n");
 		out.println (";; assertions");
 		
 		int j = 0;
 		for (Term a : asserts) {
 			out.println (String.format("(assert \n (! \n  %s \n %s \n ) \n )", a.toString(), ":named a" + j++));
 		}
 		
 		out.println (";; --end assertions\n");
 		out.println (";; command");
 		
 		int k = 0;
 		for (Term a : cmdasserts) {
 			out.println (String.format("(assert \n (! \n  %s \n %s \n ) \n )", a.toString(), ":named c" + k++));
 		}
 		
 		out.println (";; --end command\n");
 		out.println (";; lemmas");
 		
 		int l = 0;
 		for (Term a : lemmas) {
 			out.println (String.format("(assert\n (! \n  %s \n %s \n ) \n )", a.toString(), ":named l" + l++));
 		}
 		
 		out.println (";; --end lemmas\n");
 		
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
 		
 		// Only for sanity checks
 		out.println ("(get-unsat-core)");
 		
 		out.close();
 	}
 
 	/** adds declaration and theory for Atom */
 	public void declareAtom() {
 		this.addSort("(declare-sort Atom)");
 	}
 	
 	/** adds a declaration and theory for disjoint 
 	 * @param arity arity of the expression
 	 * @throws ModelException 
 	 */
 	public void declareDisjoint(int ar) throws ModelException {
 		declareAtom();
 		declareRel(ar);
 		String relAr = "Rel" + ar;
 		this.addFunction("Bool", "disjoint_"  +ar, relAr, relAr);
 		
 		// forall a in A, b in B | not a in B.
 		// or maybe: forall a in A, b in B | not a in B and not b in A...?
 		TermVar A, B;
 		TermVar[] a = makeTuple(ar, "a");
 		A = TermVar.var(relAr, "A");
 		B = TermVar.var(relAr, "B");
 		Term notAInB = Term.reverseIn(B, a).not();
 		Term aInA = Term.reverseIn(A, a);
 		Term exclusive = aInA.implies(notAInB);
 		Term axiom = Term.call("disjoint_" + ar, A, B).iff(exclusive.forall(a)).forall(A, B);
 		this.addAxiom(axiom); // add this axiom to the list of assertions
 	}
 
 	/** adds a declaration and theory for subset and subrel 
 	 * @param arity of the set expression
 	 * @throws ModelException 
 	 */
 	public void declareSubset(int ar) throws ModelException {
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
 			// declare variables
 			TermVar x = TermVar.var(relSort, "x");	// a Set or Relation
 			TermVar y = TermVar.var(relSort, "y");	// another Set or Relation
 			Term subset = Term.call("subset_"+ar, x, y);  // x is subset of y
 			Term inImpliesIn = Term.in(atomVars, x).implies(Term.in(atomVars, y));  // if an atom is in x, it is also in y 
 			// now quantify the two expressions for all x, y and atoms
 			Term axiom = subset.equal(inImpliesIn.forall(atomVars)).forall(x, y);
 			this.addAxiom(axiom);  // add this axiom to the list of assertions
 		}
 	}
 
 	private void declareRel(int ar) {
 		this.addSort("(declare-sort Rel" + ar + ")");
 	}
 
 	/** adds a declaration and theory for the converter function 
 	 * @param arity arity of the expression
 	 * @throws ModelException 
 	 */
 	public void declareA2r(int ar) throws ModelException {
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
 		String name = "a2r_" + ar;
 		if(this.addFunction("Rel" + ar, name, params)){
 			// axiom 
 			Term xInX, inImpliesEqual;
 			TermVar[] x = makeTuple(ar, "x"),
 				y = makeTuple(ar, "y");
 			
 			xInX = Term.reverseIn(Term.call(name, x), x);
 			inImpliesEqual = Term.reverseIn(Term.call(name, x), y).implies(TermBinOp.equals(x, y));
 			Term axiom = Term.forall("Atom", x, xInX.and(Term.forall("Atom", y, inImpliesEqual)));
 					
 			this.addAxiom(axiom);
 		}	
 	}
 
 	private TermVar[] makeTuple(int ar, String basename) {
 		TermVar[] x = new TermVar[ar];
 		for (int i = 0; i < x.length; i++) {
 				x[i] = TermVar.var("Atom", basename+i); // the atoms are named "a0" "a1" etc
 		}
 		return x;
 	}
 
 	/** adds a declaration for in (no axiom) 
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
 
 	/** adds a declaration and theory for "none" (the empty set)
 	 * @param arity arity of none
 	 * @throws ModelException 
 	 */
 	public void declareNone(int ar) throws ModelException {
 		declareAtom();
 		declareIn(ar);
 		if(this.addFunction("Bool", "none_" + ar, "Rel" + ar))
 		{
 			// forall tuples of this arity, tuple is not member of none
 			TermVar[] a = makeTuple(ar, "a");
 			Term none = Term.call("none_"+ar);
 			Term memb = Term.reverseIn(none, a);
 			Term axiom = memb.not().forall(a);
 			this.addAxiom(axiom);
 		}
 	}
 
 	public void declareProduct(int lar, int rar) throws ModelException {
 		if(lar < 1 || rar < 1)
 			throw new ModelException("The product is not defined for arguments of arity 0.");
 		declareAtom();
 		declareRel(rar);
 		declareRel(lar);
 		declareRel(lar + rar);
 		declareIn(rar);
 		declareIn(lar);
 		declareIn(lar + rar);
 		// careful with overloaded + operator
 		String name = String.format("prod_%dx%d", lar, rar);
 		String leftRelar = "Rel"+lar;
 		String rightRelar = "Rel"+rar;
 		if(this.addFunction("Rel" + /*concatenation*/ (lar + /*sum*/ rar), name, leftRelar, rightRelar))
 		{
 			// add axiom
 			// these are the two parameters for the product function
 			TermVar A = TermVar.var(leftRelar, "A"),
 					B = TermVar.var(rightRelar, "B"); 
 			TermVar[] x = makeTuple(lar, "x"), 
 					y = makeTuple(rar, "y"); // these are two elements, one of A and one of B
 
 			Term somethingIsInProduct = Term.reverseIn(Term.call(name, A, B), Util.concat(x, y));
 			Term xInAandYInB = Term.reverseIn(A, x).and(Term.reverseIn(B, y));
 			List<TermVar> arglist = new LinkedList<TermVar>();
 			arglist.add(A);
 			arglist.add(B);
 			arglist.addAll(Arrays.asList(x));
 			arglist.addAll(Arrays.asList(y));
 			Term axiom = somethingIsInProduct.iff(xInAandYInB).forall(arglist);
 			this.addAxiom(axiom);
 		}
 	}
 
 	public void declareJoin(int lar, int rar) throws ModelException {
 		if(lar < 1 || rar < 1)
 			throw new ModelException("The join is not defined for arguments of arity 0.");
 		if(lar + rar < 3)
 			throw new ModelException("The dot-join is not defined for two arguments of arity 1.");
 		declareAtom();      // we work with atoms in this axiom
 		declareRel(lar);    // declare left relation
 		declareRel(rar);    // declare right relation
 		int resultArity = rar + lar - 2; // this is the arity of the resulting relation
 		declareRel(resultArity); // declare result relation
 		declareSubset(rar); // declare subset
 		declareIn(rar);     // declare the in-function for the right hand argument
 		declareIn(lar);     // declare the in-function for the right hand argument
 		declareIn(resultArity);  // declare the in-function for the result
 		String leftRelar = "Rel"+lar;  // shorthand for Rel+lar
 		String rightRelar = "Rel"+rar; // shorthand for Rel+lar
 		String name = "join_" + lar + "x" + rar; // this function name
 		if(this.addFunction("Rel"+resultArity, name, leftRelar, rightRelar))
 		{
 			// add axiom
 			/* For the expression (join_1x2 A B) this axiom should read (infix):
 			 * \forall A Rel1, B Rel2, y Atom| (y \elem join_1x2) 
 			 *     iff [(\exist x Atom| x \elem A) and (a2r_2(x y) \subset B)]
 			 */
 			TermVar A, B; // relation symbols for our two arguments			
 			A = TermVar.var(leftRelar, "A");            // the left relation
 			B = TermVar.var(rightRelar, "B");           // the right relation
 			TermVar[] y = makeTuple(resultArity, "y");  // y is a tuple of the resulting relation
 			
 			Term somethingInTheJoin = Term.reverseIn(Term.call(name, A, B), y); // a call to "join"
 			
 			TermVar x = TermVar.var("Atom", "x");	// x is the row that gets removed by the join
 			TermVar[] amembers = Arrays.copyOf(y, lar);
 			amembers[lar - 1] = x;
 			TermVar[] bmembers = new TermVar[rar]; // this is a tuple to join on (element of B)
 			bmembers[0] = x; // add the last atom in x as the first atom in the match
 			for (int i = 0; i < bmembers.length - 1; i++) {
 				bmembers[i + 1] = y[(lar-1)+i];
 			}
 			
 			Term xInA = Term.reverseIn(A, amembers);  // this means: (y0 y1 .. x) \elem A
 
 			Term xyInB = Term.reverseIn(B, bmembers); // this means: (x y2 y3 ...) \elem B
 			List<TermVar> arglist = new LinkedList<TermVar>();	// universally quantified vars for this axiom
 			arglist.add(A);                 					// quantify over A 
 			arglist.add(B);    									// quantify over B
 			arglist.addAll(Arrays.asList(y));  // quantify over all atoms in the y-tuple 
 			Term axiom = somethingInTheJoin.iff((xInA.and(xyInB)).exists(x)).forall(arglist);
 			axiom.setComment("axiom for " + name);
 			this.addAxiom(axiom);
 			// to work properly, we also add some neccessary lemmas
 			assertLemmasJoin(lar, rar);
 		}
 	}
 
 	private void assertLemmasJoin(int lar, int rar) throws ModelException {
 		// lar <= rar is default case
 		int uniquerows = lar + rar -1;
 		int largerarity = lar > rar ? lar : rar;
 		TermVar r = TermVar.var("Rel"+largerarity, "r");
 		TermVar[] atoms = makeTuple(uniquerows, "a");
 		TermVar[] relmembers = Arrays.copyOf(atoms, largerarity);
 		TermVar[] singles = Arrays.copyOfRange(atoms, largerarity - 1, uniquerows);
 		TermVar[] result = new TermVar[uniquerows - 1];
 		for (int i = 0; i < relmembers.length -1; i++) {
 			result[i] = relmembers[i];
 		}
 		for (int i = 1; i < singles.length; i++) {
 			result[relmembers.length - 2 + i] = singles[i];
 		}
 		Term lhs, rhs;
 		if (lar > rar) {
 			lhs = r;
 			rhs = a2r(singles);
 		}
 		else {
 			// interestingly, the lemma holds for both cases if we just reverse the arrays
 			atoms = Util.reverse(atoms); // not really needed, but looks nicer
 			relmembers = Util.reverse(relmembers);
 			singles = Util.reverse(singles);
 			result = Util.reverse(result);
 			declareA2r(singles.length);
 			lhs = a2r(singles);
 			lhs.comment = "(swapped)";
 			rhs = r;
 		}
 		Term aInR = Term.reverseIn(r, relmembers);
 		String name = "join_" + lar + "x" + rar;
 		Term resultInJoin = Term.reverseIn(Term.call(name, lhs, rhs), result);
 		Term lemma = resultInJoin.implies(aInR).forall(Util.concat(atoms, r));
 		lemma.setComment("1. lemma for "+name+". direction: join to in");
 		this.addLemma(lemma);
 		lemma = aInR.implies(resultInJoin).forall(Util.concat(atoms, r));
 		lemma.setComment("2. lemma for "+name+". direction: in to join");
 		this.addLemma(lemma);
 	}
 
 	private Term a2r(TermVar[] vars) throws ModelException {
 		return a2r(vars.length, vars);
 	}
 
 	public void declareUnion(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel" + ar;
 		String name = "union_" + ar;
 		if(this.addFunction(relar, name, relar, relar))
 		{
 			// x in union A B = { x in A or x in B }
 			List<TermVar> arglist = new LinkedList<TermVar>();
 			TermVar[] x = makeTuple(ar, "x");
 			TermVar A = TermVar.var(relar, "A");
 			TermVar B = TermVar.var(relar, "B");
 			Term xInA = Term.reverseIn(A, x);
 			Term xInB = Term.reverseIn(B, x);
 			Term orTerm = xInA.or(xInB);
 			Term call = Term.call(name, A, B);
 			arglist.addAll(Arrays.asList(x));
 			arglist.add(A);
 			arglist.add(B);
 			this.addAxiom(Term.reverseIn(call, x).iff(orTerm).forall(arglist));
 		}
 	}
 
 	/** declares and defines the operator "one" for arity = 1.
 	 *  "one" means "one and only one"
 	 * @throws ModelException 
 	 */
 	public void declareOne() throws ModelException  {
 		List<TermVar> argList = new LinkedList<TermVar>();
 		declareRel(1);
 		String name = "one_1";
 		String relar = "Rel1";
 		if(this.addFunction("Bool", name, relar))
 		{
 			// add axiom
 			TermVar X = TermVar.var(relar, "X");
 			
 			TermVar[] aTuple = makeTuple(1, "a");
 			argList.addAll(Arrays.asList(aTuple));
 			
 			Term notEmpty = Term.exists(aTuple, Term.in(argList, X));
 			
 			TermVar[] bTuple = makeTuple(1, "b");
 			argList.addAll(Arrays.asList(bTuple));
 			
 			Term one = Term.call(name, X);			
 			
 			// make the a == b expression 
 			Term aEqualsBTerm = TermBinOp.equals(aTuple, bTuple);
 			
 			Term aInX = Term.reverseIn(X, aTuple);
 			Term bInX = Term.reverseIn(X, bTuple);
 			Term aAndBinX = aInX.and(bInX);
 			Term existanceImpliesEqual = aAndBinX.implies(aEqualsBTerm).forall(argList);
 			
 			Term axiom = (one.iff(notEmpty.and(existanceImpliesEqual))).forall(X);
 			this.addAxiom(axiom);
 		}
 	}
 
 	/** declares and defines the operator "lone" for arity 1.
 	 *  "lone" means "at most one"
 	 * @throws ModelException 
 	 */
 	public void declareLone() throws ModelException {
 		List<TermVar> argList = new LinkedList<TermVar>();
 		declareRel(1);
 		String name = "lone_1";
 		String relar = "Rel1";
 		if(this.addFunction("Bool", name, relar))
 		{
 			// add axiom
 			TermVar X = TermVar.var(relar, "X");
 			
 			TermVar[] aTuple = makeTuple(1, "a");
 			argList.addAll(Arrays.asList(aTuple));
 			TermVar[] bTuple = makeTuple(1, "b");
 			argList.addAll(Arrays.asList(bTuple));
 			
 			Term lone = Term.call(name, X);
 			
 			Term aEqualsBTerm = TermBinOp.equals(aTuple, bTuple);
 			
 			Term aInX = Term.reverseIn(X, aTuple);
 			Term bInX = Term.reverseIn(X, bTuple);
 			Term aAndBinX = aInX.and(bInX);
 			Term existanceImpliesEqual = aAndBinX.implies(aEqualsBTerm).forall(argList);
 			
 			Term axiom = (lone.iff(existanceImpliesEqual)).forall(X);
 			this.addAxiom(axiom);
 		}
 	}
 
 	/** declares the multiplicity constraint "some". 
 	 *  Does not take any parameters because multiplicity constraints are always of arity = 1  
 	 * 
 	 * @throws ModelException
 	 */
 	public void declareSome() throws ModelException {
 		declareRel(1);
 		String name = "some_1";
 		String relar = "Rel1";
 		if(this.addFunction("Bool", name, relar))
 		{
 			// axiom: some means that there is any Atom/Tuple inside the argument expression
 			TermVar A = TermVar.var(relar, "A");
 			
 			TermVar[] aTuple = makeTuple(1, "a");
 			
 			Term some = Term.call(name, A);
 			Term xInA = Term.reverseIn(A, aTuple);
 			Term axiom = some.iff(xInA.exists(aTuple)).forall(A);
 			this.addAxiom(axiom);
 		}
 	}
 
 	// arity is always 2
 	public void declareTranspose() throws ModelException {
 		declareAtom();
 		declareIn(2);
 		declareRel(2);
 		String name = "transp";
 		if(this.addFunction("Rel2", name, "Rel2"))
 		{
 			// ∀r: Rel2 , a1 , a2 : Atom | in2 (a1 , a2 , transpose 2 (r)) ⇔ in2 (a2 , a1 , r)
 			TermVar R = TermVar.var("Rel2", "R");
 			TermVar[] a = makeTuple(2, "a");
 			
 			Term axiom = Term.reverseIn(Term.call(name, R), a).equal(Term.reverseIn(R, Util.reverse(a))).forall(Util.concat(a, R));
 			this.addAxiom(axiom);
 		}
 	}
 
 	// arity is always 2
 	public void declareTransitiveClosure() throws ModelException {
 		declareAtom();
 		declareIn(2);
 		declareRel(2);
 		// helper function to make transitive closure more readable and less redundant
 		declareTrans();
 		String name = "transClos";
 		if(this.addFunction("Rel2", name, "Rel2"))
 		{
 			/// add axiom
 			// we define what a transitive closure is
 			// this is split into 3 assertions
 			// 1. assert r in trans(r)
 			TermVar r = TermVar.var("Rel2", "r");
 			this.addAxiom(Term.call("subset_2", r, Term.call(name, r)).forall(r));
 			// 2. assert that the transitive closure is -in fact- transitive
 			this.addAxiom(Term.call("trans", Term.call(name, r)).forall(r));
 			// 3. assert that tcl is minimal
 			TermVar r1 = TermVar.var("Rel2", "r1");
 			TermVar r2 = TermVar.var("Rel2", "r2");
 			Term subsetAndTrans = Term.call("subset_2", r1, r2).and(Term.call("trans", r2));
 			Term minimalaxiom = subsetAndTrans.implies(Term.call("subset_2", Term.call(name, r1), r2)).forall(r1, r2);
 			this.addAxiom(minimalaxiom);
 			// also, add some lemma about in_2 and the transCl
 			assertLemmasTCL("transClos");
 		}
 	}
 
 	/**
 	 * Adds lemmas for transitive closure to work properly. 
 	 * These lemmas are the result of experimentation with the z3 solver. 
 	 * @param name
 	 * @throws ModelException
 	 */
 	private void assertLemmasTCL(String name) throws ModelException {
 		TermVar a1 = TermVar.var("Atom", "a1");
 		TermVar a2 = TermVar.var("Atom", "a2");		
 		TermVar r = TermVar.var("Rel2", "r");
 		Term tCl = Term.call(name, r);
 		
 		Term a12inTCL = Term.reverseIn(tCl, a1, a2);
 		
 		TermVar a3 = TermVar.var("Atom", "a3");
 		Term a13inR = Term.reverseIn(r, a1, a3);		
 		Term a32inTCL = Term.reverseIn(tCl, a3, a2);
 		
 		this.addLemma(a12inTCL.implies(a13inR.and(a32inTCL).exists(a3)).forall(a1, a2, r));
 		
 		Term a13inTCL = Term.reverseIn(tCl, a1, a3);
 		Term a32inR = Term.reverseIn(r, a3, a2);
 		
 		this.addLemma(a12inTCL.implies(a13inTCL.and(a32inR).exists(a3)).forall(a1, a2, r));
 	}
 
 	private void declareTrans() throws ModelException {
 		declareAtom();
 		declareRel(2);
 		declareIn(2);
 		if(this.addFunction("Bool", "trans", "Rel2")){
 			// 
 			TermVar a1 = TermVar.var("Atom", "a1");
 			TermVar a2 = TermVar.var("Atom", "a2");
 			TermVar a3 = TermVar.var("Atom", "a3");
 			TermVar r = TermVar.var("Rel2", "r");
 			//
 			Term a12reachR = Term.reverseIn(r, a1, a2);
 			Term a23reachR = Term.reverseIn(r, a2, a3);
 			Term a13reachR = Term.reverseIn(r, a1, a3);
 			Term meaning = (a12reachR.and(a23reachR)).implies(a13reachR);
 			Term fun = Term.call("trans", r);
 			Term axiomtransitivity = fun.iff(meaning.forall(a1, a2, a3)).forall(r);
 			this.addAxiom(axiomtransitivity);
 		}
 	}
 
 	public void declareReflexiveTransitiveClosure() throws ModelException {
 		declareAtom();
 		declareIn(2);
 		declareRel(2);
 		declareTrans();
 		declareIdentity();
 		String name = "reflTransClos";
 		if(this.addFunction("Rel2", name, "Rel2"))
 		{
 			/// add axiom
 			// first we define what transitive means
 			declareTrans();
 			// then we define what a transitive closure is
 			// this is split into 3 assertions
 			// 1. assert r is in trans(r)
 			TermVar r = TermVar.var("Rel2", "r");
 			this.addAxiom(Term.call("subset_2", r, Term.call(name, r)).forall(r));
 			// 2. assert that the transitive closure is -in fact- transitive
 			this.addAxiom(Term.call("trans", Term.call(name, r)).forall(r));
 			// 3. assert iden in TCL
 			Term idenInTCL = Term.call("subset_2", Term.call("iden"), Term.call(name, r)).forall(r);
 			this.addAxiom(idenInTCL);
 			// 4. assert that tcl is minimal
 			TermVar r1 = TermVar.var("Rel2", "r1");
 			TermVar r2 = TermVar.var("Rel2", "r2");
 			
 //			Term subsetAndTrans = Term.call("subset_2", r1, r2).and(Term.call("trans", r2));
 			Term subsetAndTransAndRefl = Term.call("subset_2", r1, r2).and(Term.call("trans", r2)).and(Term.call("subset_2", Term.call("iden"), r2));
 			
 			Term minimalaxiom = subsetAndTransAndRefl.implies(Term.call("subset_2", Term.call(name, r1), r2)).forall(r1, r2);
 			this.addAxiom(minimalaxiom);
 			// don't forget our lemmas
 			assertLemmasTCL(name);
 		}
 	}
 
 	public void declareCardinality(int ar) throws ModelException {
 		declareRel(ar);
 		declareFinite();
 		declareOrd();
 		
 		String name = "card_" + ar;
 		String relar = "Rel" + ar;
 		if (this.addFunction("Int", name, relar)) {
 			TermVar R = TermVar.var(relar, "R");
 			TermVar[] a = makeTuple(ar, "a");
 			Term one = Term.call("1");
 			Term cardR = Term.call(name, R);
 			Term finR = Term.call("finite", R);
 			Term ordA = Term.call("ord", Util.reverse(Util.concat(a, R)));
 			Term validCard = cardR.gte(ordA).gte(one);
 			// ∀r: Reln , a1:n : Atom | (finite n (r) ∧ inn (a1:n , r)) ⇒ 1 ≤ ordn (r, a1:n ) ≤ card n (r)
 			{
 				Term guard = finR.and(Term.reverseIn(R, a));
 				Term axiom = guard.implies(validCard).forall(Util.concat(a, R));
 				this.addAxiom(axiom);
 			}	
 			// ∀r: Reln , i: int | (finite n (r) ∧ 1 ≤ i ≤ card n (r)) 
 			//		⇒ ∃a1:n : Atom | inn (a1:n , r) ∧ ordn (r, a1:n ) = i
 			{
 				TermVar i = TermVar.var("Int", "i");
 				Term guard = finR.and(validCard);
 				Term axiom = guard.implies(Term.reverseIn(R, a).and(ordA.equal(i)).exists(i)).forall(R, i);
 				this.addAxiom(axiom);
 			}			
 		}
 	}
 	
 	/** Declares all the axioms needed for ordering a given signature.  
 	 * Will only produce either finite or infinite axioms.
 	 * 
 	 * @param suffix : the name of the signature on which the ordering is defined  
 	 * @param finite : if true, the signature is considered finite. 
 	 * 	If false, the signature is considered infinite. This affects special operations like "last element" and "cardinality" 
 	 * @throws ModelException
 	 */
 	public void declareOrdering(String suffix, boolean finite) throws ModelException {
 		/* We begin by declaring the common operators finite, and ord.
 		 * Orderings are only valid for signature, so arity is always assumed to be 1.
 		 * The iterator function "elem" is implicitly declared inside declareOrd().  
 		 */
 		declareFinite();
 		declareI2a();
 		declareOrd();
 		declareNext(suffix);
 		declareFirst(suffix);
 		declareLast(suffix, finite);			
 	}
 
 	/** Implements "last" for a given relation. The name of the relation must be a proper suffix
 	 * for a SMT function.
 	 * 
 	 * @param suffix
 	 * @param finite
 	 * @throws ModelException
 	 */
 	private void declareLast(String suffix, boolean finite) throws ModelException {
 		declareNone(1);
 		// implement Last
 		String name = "last"+suffix;
 		this.addFunction("Rel1", name);
 		
 		// this branch is important because card_1 may not be defined (due to S being infite). SMT does not handle partial functions, so undefined values are not allowed 
 		TermVar N = TermVar.var(suffix); // not actually a variable but a constant
 		Term finN = Term.call("finite", N);
 		Term lastS = Term.call(name);
 		if (!finite) {
 			declareNone(1);
 			// ¬finite 1 (N [S]) ⇒ lastS = none 1
 			Term none = Term.call("none_1");
 			Term axiom = finN.not().implies(lastS.equal(none));
 			axiom.setComment("infinite axiom for " + name);
 			this.addAxiom(axiom);
 		}
 		else {
 			declareCardinality(1);
 			declareOrd();
 			declareA2r(1);
 			// finite 1 (N [S]) ⇒ (lastS = sin 1 (ordInv 1 (N [S], card 1 (N [S]))))
 			Term cardN = Term.call("card_1", N);
 			Term ordInv = Term.call("at", N, cardN);
 			Term a2r = Term.call("a2r_1", ordInv);
 			Term axiom = finN.implies(lastS.equal(a2r));
 			axiom.setComment("finite axiom for " + name);
 			this.addAxiom(axiom);
 		}
 	}
 
 	private void declareFirst(String suffix) throws ModelException {
 		// implementing "first"
 		declareA2r(1);
 		declareOrd();
 		String name = "first"+suffix;
 		// firstS = sin 1 (ordInv 1 (N [S], 1))
 		if (this.addFunction("Rel1",name)) {
 			Term first = Term.call("name");
 			TermVar N = TermVar.var(suffix); // not actually a variable but a constant
 			TermVar one = TermVar.var("Int", "1"); // not a variable but a constant
 			Term ordInv = Term.call("at", N, one);
 			Term a2r = Term.call("a2r_1", ordInv);
 			Term axiom = first.equal(a2r);
 			axiom.setComment("axiom for " + name);
 			this.addAxiom(axiom);
 		}
 	}
 
 	/** Declares the constant relations Next for a given relation S
 	 *  (nextS : Rel2)
 	 * @param suffix the name of the relation S
 	 * @throws ModelException
 	 */
 	private void declareNext(String suffix) throws ModelException {		
 		// declarations
 		declareAtom();
 		declareIn(1);
 		declareIn(2);
 		declareRel(1);
 		declareRel(2);
 		declareOrd();
 		declareI2a();
 		declareFinite();
 		declareNone(1);
 		
 		String nextSname = "next"+suffix;
 		if (this.addFunction("Rel2", nextSname)) {
 			TermVar a = TermVar.var("Atom", "a");
 			TermVar b = TermVar.var("Atom", "b");
 			TermVar N = TermVar.var(suffix); // actually constant
 			TermVar one = TermVar.var("1"); // actually constant
 			
 			// ∀a, b: Atom |(in 1 (a, N [S]) ∧ in 1 (b, N [S]))
 			//	⇒ (in 2 (a, b, nextS ) ⇔ ord 1 (N [S], b) = ord 1 (N [S], a) + 1)
 			{
 				Term guard = Term.reverseIn(N, a).and(Term.reverseIn(N, b));
 				Term inS = Term.reverseIn(Term.call(nextSname), a, b);
 				Term follows = Term.call("ord", N, b).equal(Term.call("ord", a).plus(one));
 				Term axiom = guard.implies(inS.equal(follows));
 				axiom.setComment("axiom for " + nextSname);
 				this.addAxiom(axiom);
 			}
 			// ¬(N [S] = none 1 )
 			{
 				Term axiom = N.not().equal(Term.call("none_1"));
 				axiom.setComment("another axiom for " + nextSname);
 				this.addAxiom(axiom);
 			}
 		}
 	}
 
 	private void declareInt() {
 		this.addFunction("Rel1", "N_Int");	
 	}
 
 	private void declareI2a() throws ModelException {
 		declareInt();
 
 		Term N = Term.call("N_Int");
 		String namei2a = "i2a";
 		String namea2i = "a2i";
 		if (this.addFunction("Atom", namei2a, "Int")) {
 			TermVar a = TermVar.var("Atom", "a");
 			Term guard = Term.reverseIn(N, a);
 			// i2a(a2i(a))
 			Term thereAndBack = Term.call(namei2a, Term.call(namea2i, a));
 			// ∀a: Atom | in1 (a, N [Int]) ⇒ i2a(a2i(a)) = a
 			Term axiom = guard.implies(thereAndBack.equal(a)).forall(a);
 			this.addAxiom(axiom);
 		}
 		if (this.addFunction("Int", namea2i, "Atom")) {
 			TermVar i = TermVar.var("Int", "i");
 			// in1 (i2a(i), N [Int])
 			Term inN = Term.call("in_1", Term.call(namei2a, i), N);
 			// a2i(i2a(i))
 			Term thereAndBack = Term.call(namea2i, Term.call(namei2a, i));
 			// ∀i: int | in1 (i2a(i), N [Int]) ∧ a2i(i2a(i)) = i
 			Term axiom = inN.implies(thereAndBack.equal(i)).forall(i);
 			axiom.setComment("axiom for i2a and a2i");
 			this.addAxiom(axiom);
 		}
 	}
 
 	private void declareFinite() throws ModelException {
 		declareRel(1);
		this.addFunction("Bool", "finite", "Rel1");
 	}
 		
 	private void declareOrd() throws ModelException{
 		declareAtom();
 		declareRel(1);
 		declareFinite();
 		
 		if(this.addFunction("Int", "ord", "Rel1"))
 		{
 			// ∀r: Reln , a1:n , b1:n : Atom | (finite n (r) ∧ inn (a1:n , r) ∧ inn (b1:n , r) 
 			//	∧ ordn (r, a1:n ) = ordn (r, b1:n )) ⇒ (a1 = b1 ∧ … ∧ an = bn )
 			TermVar R = TermVar.var("Rel1", "R");
 			TermVar a = TermVar.var("Atom", "a");
 			TermVar b = TermVar.var("Atom", "b");
 			
 			Term guard = Term.call("finite", R).and(Term.reverseIn(R, a)).and(Term.reverseIn(R, b));
 			Term ordeq = Term.call("ord", R, a).and(Term.call("ord", R, b));
 			Term axiom = guard.and(ordeq).implies(a.equal(b)).forall(R,a,b);
 			axiom.setComment("axiom for ord");
 			this.addAxiom(axiom);
 		}
 
 		if(this.addFunction("Atom", "at", "Rel1", "Int"))
 		{
 			// ∀r: Rel1 , a: Atom | in 1 (a, r) ⇒ at 1 (r, ord1 (r, a)) = a
 			TermVar R = TermVar.var("Rel1", "R");
 			TermVar a = TermVar.var("Atom", "a");
 			Term guard = Term.reverseIn(R, a);
 			Term ord = Term.call("ord", R, a);
 			Term axiom = guard.implies(Term.call("at", R, ord).equal(a)).forall(R, a);
 			axiom.setComment("axiom for at (the reverse of ord)");
 			this.addAxiom(axiom);
 		}
 	}
 
 	/** Declares the domain restriction operator
 	 * @param rar arity of the right-hand parameter
 	 * @throws ModelException 
 	 * @rem left-hand arity is required to be 1 (type:set)
 	 */
 	public void declareDomainRestriction(int rar) throws ModelException {
 		declareRel(1);
 		declareRel(rar);
 		String relar = "Rel"+rar;
 		this.addFunction(relar, "domRestr_" + rar, "Rel1", relar);
 		// TODO: add axiom for domain Restriction
 		throw new ModelException("None has not yet been implemented.");
 	}
 
 	public void declareDifference(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		String name = "diff_" + ar;
 		if(this.addFunction(relar, name, relar, relar))
 		{
 			// add axiom
 			List<TermVar> argList = new LinkedList<TermVar>(); // this is a list of all quantified variables in the forall expression
 			
 			TermVar A, B;     // these are the left and right arg in "(diff_x A B)"
 			A = TermVar.var(relar, "A");
 			B = TermVar.var(relar, "B");
 			argList.add(A);
 			argList.add(B);
 			
 			TermVar[] aTuple = makeTuple(ar, "a"); // this is a "tuple". it consists of one atom per rank
 			argList.addAll(Arrays.asList(aTuple)); // we will also quantify over those
 			
 			
 			Term somethingIsInTheCallToDiff = Term.in(Arrays.asList(aTuple), Term.call(name, A, B));
 			Term inAandNotB = Term.in(Arrays.asList(aTuple), A).and(Term.in(Arrays.asList(aTuple), B).not());
 			Term axiom = somethingIsInTheCallToDiff.iff(inAandNotB).forall(argList);
 			this.addAxiom(axiom);
 		}
 	}
 
 	public void declareOverride(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "overr_" + ar, relar, relar);
 		//TODO: add axiom
 		throw new ModelException("Domain Override has not yet been implemented.");
 	}
 
 	public void declareIntersection(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "inter_" + ar, relar, relar);
 		//TODO: add axiom
 		throw new ModelException("Intersection has not yet been implemented.");
 	}
 
 	/** Declares the range restriction operator
 	 * @param lar arity of the left-hand parameter
 	 * @throws ModelException 
 	 * @rem right-hand arity is required to be 1 (type:set)
 	 */
 	public void declareRangeRestriction(int lar) throws ModelException {
 		declareRel(1);
 		declareRel(lar);
 		String relar = "Rel"+lar;
 		this.addFunction(relar, "rangeRestr_" + lar, relar, "Rel1");
 		//TODO: add axiom
 		throw new ModelException("Range restriction has not yet been implemented.");
 	}
 
 	public void declareIdentity() throws ModelException {
 		declareRel(2);		
 		if(this.addFunction("Rel"+2, "iden"))
 		{
 			TermVar	a0 = TermVar.var("Atom", "a0");
 			Term axiom = Term.reverseIn(Term.call("iden"), a0, a0);			
 			this.addAxiom(axiom.forall(a0));
 		}
 	}
 
 	private void addAxiom(Term term) {
 		// we don't need to assert the expression "TRUE"
 		if (!term.equals(Term.TRUE)) {
 			axioms.add(term);
 		}
 	}
 
 	public Term a2r(int ar, Term ... sub) throws ModelException {
 		declareA2r(ar);
 		return Term.call("a2r_"+ar, sub);
 	}
 }
