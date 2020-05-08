 /**
  * Created on 07.02.2011
  */
 package edu.kit.asa.alloy2key.key;
 
 import java.io.OutputStream;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import edu.kit.asa.alloy2key.key.TermBinOp.Op;
 import edu.kit.asa.alloy2key.modules.KeYModule;
 import edu.kit.asa.alloy2key.util.Util;
 
 /**
  * capturing output to an .SMT file
  * 
  * @author Ulrich Geilmann
  * @author Jonny
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
 		declareAtom();
 		declareRel(ar);
 		String relAr = "Rel" + ar;
 		this.addFunction("Bool", "disjoint_"  +ar, relAr, relAr);
 		
 		// forall a in A, b in B | not a in B.
 		// or maybe: forall a in A, b in B | not a in B and not b in A...?
 		TermVar a, b, A, B;
 		a = TermVar.var("Atom", "a");
 		b = TermVar.var("Atom", "b");
 		A = TermVar.var(relAr, "A");
 		B = TermVar.var(relAr, "B");
 		Term aInA = Term.reverseIn(B, b);
 		Term bInB = Term.reverseIn(A, a);
 		Term typeRestriction = aInA.and(bInB);		
 		Term notAinB = new TermUnary(TermUnary.Op.NOT, Term.reverseIn(B, a));		
 		Term axiom = Term.call("disjoint_" + ar, A, B).iff(typeRestriction.and(notAinB)).forall(a, A, b, B);;
 		this.addAssertion(axiom); // add this axiom to the list of assertions
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
 			// declare variables
 			TermVar x = TermVar.var(relSort, "x");	// a Set or Relation
 			TermVar y = TermVar.var(relSort, "y");	// another Set or Relation
 			Term subset = Term.call("subset_"+ar, x, y);  // x is subset of y
 			Term inImpliesIn = new TermBinOp(Term.in(atomVars, x), Op.IMPLIES, Term.in(atomVars, y));  // if an atom is in x, it is also in y 
 			// now quantify the two expressions for all x, y and atoms
 			List<TermVar> quantVars = new LinkedList<TermVar>(atomVars);
 			quantVars.add(x);
 			quantVars.add(y);
 			Term axiom = Term.forall(quantVars, (Term)new TermBinOp(subset, Op.IFF, inImpliesIn));
 			this.addAssertion(axiom);  // add this axiom to the list of assertions
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
 
 	/** adds a declaration and theory for "none" 
 	 * @param arity arity of none
 	 */
 	public void declareNone(int ar) {
 		this.addFunction("Bool", "none_" + ar, "Rel" + ar);
 		//TODO: add axiom
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
 			TermVar[] x = new TermVar[lar], 
 					y = new TermVar[rar]; // these are two elements, one of A and one of B
 			for (int i = 0; i < x.length; i++) {
 				x[i] = TermVar.var("Atom", "x"+i);
 			}
 			for (int i = 0; i < y.length; i++) {
 				y[i] = TermVar.var("Atom", "y"+i);
 			}
 			Term somethingIsInProduct = Term.reverseIn(Term.call(name, A, B), Util.concat(x, y));
 			Term xInAandYInB = new TermBinOp(Term.reverseIn(A, x), Op.AND, Term.reverseIn(B, y));
 			List<TermVar> arglist = new LinkedList<TermVar>();
 			arglist.add(A);
 			arglist.add(B);
 			arglist.addAll(Arrays.asList(x));
 			arglist.addAll(Arrays.asList(y));
 			Term axiom = somethingIsInProduct.iff(xInAandYInB).forall(arglist);
 			this.addAssertion(axiom);
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
 		declareA2r(rar);    // declare the conversion function
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
 			List<TermVar> y = new ArrayList<TermVar>(); // the tuple for the "y" variable
 			for(int i = 0; i < resultArity; i++)
 			{
 				y.add(TermVar.var("Atom", "y"+i));      // y is actually a tuple
 			}
 			
 			Term somethingInTheJoin = Term.in(y, Term.call(name, A, B)); // a call to "join"
 			
 			TermVar[] x = new TermVar[lar];                          // the left hand tuple
 			for(int i = 0; i < lar; i++){               
 					x[i] = TermVar.var("Atom", "x"+i);               // x is actually a tuple, too
 			}
 			TermVar xLast = x[lar-1];                                // the atom to join on is the last atom in the x-tuple
 			
 			Term xInA = Term.reverseIn(A, x);                        // this means: x \elem A
 			List<TermVar> matchingtuple = new LinkedList<TermVar>(); // this is a tuple to join on (element of B)
 			matchingtuple.add(xLast);                                // add the last atom in x as the first atom in the match
 			matchingtuple.addAll(y);                                 // then follow up with the remaining atoms from the result
 			// xyInB means, for B of Rel2: (subset_2 (a2r_2 x y) B)
 			Term xyInB = Term.call("subset_" + rar, Term.call("a2r_" + rar, matchingtuple), B);
 			List<TermVar> arglist = new LinkedList<TermVar>();	// universally quantified vars for this axiom
 			arglist.add(A);                 					// quantify over A 
 			arglist.add(B);    									// quantify over B
 			arglist.addAll(y); 									// quantify over all atoms in the y-tuple 
 			Term axiom = somethingInTheJoin.iff((xInA.and(xyInB)).exists(x)).forall(arglist);
 			this.addAssertion(axiom);
 		}
 	}
 
 	public void declareUnion(int ar) {
 		declareRel(ar);
 		this.addFunction("Rel" + ar, "union_" + ar);	
 		// TODO: add axiom
 	}
 
 	/** declares and defines the operator "one" for a given arity.
 	 *  "one" means "one and only one"
 	 * @param ar arity of the expression
 	 */
 	public void declareOne(int ar)  {		
 		List<TermVar> argList = new LinkedList<TermVar>();
 		declareRel(ar);
 		String name = "one_" + ar;
 		String relar = "Rel" + ar;
 		if(this.addFunction("Bool", name, relar))
 		{
 			// add axiom
 			TermVar X = TermVar.var(relar, "X");
 			
 			TermVar[] aTuple = new TermVar[ar];
 			for(int i = 0; i < ar; i++)
 			{
 				aTuple[i] = TermVar.var("Atom", "a"+i);
 				argList.add(aTuple[i]);
 			}
 			
 			Term notEmpty = Term.exists(aTuple, Term.in(argList, X));
 			
 			TermVar[] bTuple = new TermVar[ar];
 			for(int i = 0; i < ar; i++)
 			{
 				bTuple[i] = TermVar.var("Atom", "b"+i);
 				argList.add(bTuple[i]);
 			}
 			
 			Term one = Term.call(name, X);			
 			
 			// make the a == b expression 
 			Term aEqualsBTerm = Term.TRUE;
 			for(int i = 0; i < aTuple.length; i++)
 			{
 				aEqualsBTerm = aEqualsBTerm.and(aTuple[i].equal(bTuple[i]));
 			}
 			
 			Term aInX = Term.reverseIn(X, aTuple);
 			Term bInX = Term.reverseIn(X, bTuple);
 			Term aAndBinX = aInX.and(bInX);
 			Term existanceImpliesEqual = aAndBinX.implies(aEqualsBTerm).forall(argList);
 			
 			Term axiom = (one.iff(notEmpty.and(existanceImpliesEqual))).forall(X);
 			this.addAssertion(axiom);
 		}
 	}
 
 	/** declares and defines the operator "lone" for a given arity.
 	 *  "lone" means "at most one"
 	 * @param ar arity of the expression
 	 */
 	public void declareLone(int ar) {
 		List<TermVar> argList = new LinkedList<TermVar>();
 		declareRel(ar);
 		String name = "lone_" + ar;
 		String relar = "Rel" + ar;
 		if(this.addFunction("Bool", name, relar))
 		{
 			// add axiom
 			TermVar X = TermVar.var(relar, "X");
 			
 			TermVar[] aTuple = new TermVar[ar];
 			for(int i = 0; i < ar; i++)
 			{
 				aTuple[i] = TermVar.var("Atom", "a"+i);
 				argList.add(aTuple[i]);
 			}
 			
 			TermVar[] bTuple = new TermVar[ar];
 			for(int i = 0; i < ar; i++)
 			{
 				bTuple[i] = TermVar.var("Atom", "b"+i);
 				argList.add(bTuple[i]);
 			}
 			
 			Term lone = Term.call(name, X);
 			
 			// make the a == b expression 
 			Term aEqualsBTerm = Term.TRUE;
 			for(int i = 0; i < aTuple.length; i++)
 			{
 				aEqualsBTerm = aEqualsBTerm.and(aTuple[i].equal(bTuple[i]));
 			}
 			
 			Term aInX = Term.reverseIn(X, aTuple);
 			Term bInX = Term.reverseIn(X, bTuple);
 			Term aAndBinX = aInX.and(bInX);
 			Term existanceImpliesEqual = aAndBinX.implies(aEqualsBTerm).forall(argList);
 			
 			Term axiom = (lone.iff(existanceImpliesEqual)).forall(X);
 			this.addAssertion(axiom);
 		}
 	}
 
 	public void declareSome(int ar) {
 		declareRel(ar);
 		String name = "some_" + ar;
 		String relar = "Rel" + ar;
 		if(this.addFunction("Bool", name, relar))
 		{
 			// axiom: some means that there is any Atom/Tuple inside the argument expression
 			TermVar A = TermVar.var(relar, "A");
 			
 			TermVar[] aTuple = new TermVar[ar];
 			for(int i = 0; i < ar; i++)
 			{
 				aTuple[i] = TermVar.var("Atom", "a"+i);
 			}
 			
 			Term some = Term.call(name, A);
 			Term xInA = Term.reverseIn(A, aTuple);
 			Term axiom = some.iff(xInA.exists(aTuple)).forall(A);
 			this.addAssertion(axiom);
 		}
 	}
 
 	// arity is always 2
 	public void declareTranspose() {
 		declareRel(2);
 		this.addFunction("Rel2", "transp", "Rel2");
 		//TODO: add axiom
 	}
 
 	// arity is always 2
 	public void declareTransitiveClosure() {
 		declareRel(2);
 		this.addFunction("Rel2", "transClos", "Rel2");
 		//TODO: add axiom
 	}
 
 	public void declareReflexiveTransitiveClosure() {
 		declareRel(2);
 		this.addFunction("Rel2", "reflTransClos", "Rel2");
 		//TODO: add axiom
 	}
 
 	public void declareCardinality(int ar) {
 		declareRel(ar);
 		this.addFunction("Int", "card_" + ar, "Rel" + ar);
 		//TODO: add axiom
 	}
 
 	/** Declares the domain restriction operator
 	 * @param rar arity of the right-hand parameter
 	 * @rem left-hand arity is required to be 1 (type:set)
 	 */
 	public void declareDomainRestriction(int rar) {
 		declareRel(1);
 		declareRel(rar);
 		String relar = "Rel"+rar;
 		this.addFunction(relar, "domRestr_" + rar, "Rel1", relar);
 		// TODO Auto-generated method stub
 	}
 
 	public void declareDifference(int ar) {
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
 			
 			TermVar[] aTuple = new TermVar[ar]; // this is a "tuple". it consists of
 			for(int i = 0; i < ar; i++)         // one atom per rank
 			{
 				aTuple[i] = TermVar.var("Atom", "a"+i); // the atoms are named "a0" "a1" etc
 				
 				argList.add(aTuple[i]);         // we will also quantify over those
 			}
 			
 			Term somethingIsInTheCallToDiff = Term.in(Arrays.asList(aTuple), Term.call(name, A, B));
 			Term inAandNotB = Term.in(Arrays.asList(aTuple), A).and(Term.in(Arrays.asList(aTuple), B).not());
 			Term axiom = somethingIsInTheCallToDiff.iff(inAandNotB).forall(argList);
 			this.addAssertion(axiom);
 		}
 	}
 
 	public void declareOverride(int ar) {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "overr_" + ar, relar, relar);
 		//TODO: add axiom
 	}
 
 	public void declareIntersection(int ar) {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		this.addFunction(relar, "inter_" + ar, relar, relar);
 		//TODO: add axiom
 	}
 
 	/** Declares the range restriction operator
 	 * @param lar arity of the left-hand parameter
 	 * @rem right-hand arity is required to be 1 (type:set)
 	 */
 	public void declareRangeRestriction(int lar) {
 		declareRel(1);
 		declareRel(lar);
 		String relar = "Rel"+lar;
 		this.addFunction(relar, "rangeRestr_" + lar, relar, "Rel1");
 		//TODO: add axiom
 	}
 
 	public void declareIdentity() {
 		declareRel(2);		
 		this.addFunction("Rel"+2, "iden");
 		//TODO: add axiom
 	}
 }
