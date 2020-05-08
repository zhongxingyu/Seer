 package edu.kit.asa.alloy2relsmt.smt;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 
 import edu.kit.asa.alloy2relsmt.util.Util;
 
 /**
  * Implements the relational theory for SMT. Includes axioms, declarations and lemmas
  * @author Jonny
  *
  */
 public final class RelTheory {
 	
 	private SMTFile file;
 	
 	public RelTheory (SMTFile target){
 		file = target;
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
 		if(file.addFunction("Rel" + ar, name, params)){
 			// axiom 
 			Term xInX, inImpliesEqual;
 			TermVar[] x = makeTuple(ar, "x"),
 				y = makeTuple(ar, "y");
 			
 			xInX = Term.reverseIn(Term.call(name, x), x);
 			inImpliesEqual = Term.reverseIn(Term.call(name, x), y).implies(TermBinOp.equals(x, y));
 			Term axiom = Term.forall("Atom", x, xInX.and(Term.forall("Atom", y, inImpliesEqual)));
 			
 			axiom.setComment("axiom for the conversion function Atom -> Relation");
 			file.addAxiom(axiom);
 		}	
 	}
 
 	/** adds declaration and theory for Atom */
 	public void declareAtom() {
 		file.addSort("(declare-sort Atom)");
 	}
 
 	public void declareCardinality(int ar) throws ModelException {
 		declareRel(ar);
 		declareFinite();
 		declareOrd();
 		
 		String name = "card_" + ar;
 		String relar = "Rel" + ar;
 		Term one = Term.call("1");
 		Term zero = Term.call("0");
 		TermVar[] a = makeTuple(ar, "a");
 		if (file.addFunction("Int", name, relar)) {
 			TermVar R = TermVar.var(relar, "R");
 			Term cardR = Term.call(name, R);
 			Term finR = Term.call("finite", R);
 			Term ordA = Term.call("ord", Util.reverse(Util.concat(a, R)));
 			Term validCard = cardR.gte(ordA).and(ordA.gte(one));
 			// ∀r: Reln , a1:n : Atom | (finite n (r) ∧ inn (a1:n , r)) ⇒ 1 ≤ ordn (r, a1:n ) ≤ card n (r)
 			{
 				Term guard = finR.and(Term.reverseIn(R, a));
 				Term axiom = guard.implies(validCard).forall(Util.concat(a, R));
 				axiom.setComment("axiom about finite Relations having a card > ord > 1");
 				file.addAxiom(axiom);
 			}	
 			// ∀r: Reln , i: int | (finite n (r) ∧ 1 ≤ i ≤ card n (r)) 
 			//		⇒ ∃a1:n : Atom | inn (a1:n , r) ∧ ordn (r, a1:n ) = i
 			{
 				TermVar i = TermVar.var("Int", "i");
 				Term guard = finR.and(validCard);
 				Term axiom = guard.implies(Term.reverseIn(R, a).and(ordA.equal(i)).exists(i)).forall(R, i).forall(a);
 				axiom.setComment("axiom about finite Relations having atoms for certain numbers in ord");
 				file.addAxiom(axiom);
 			}
 			{
 				// lemma about a2r_1 having card_1				
 				Term call = Term.call("a2r_" + ar, a);
 				Term lemma = Term.call(name, call).equal(one).forall(a);
 				lemma.setComment("lemma about a2r_x having card_x");
 				file.addLemma(lemma);
 			}
 			{
 				// lemma about some having card_1 > 0				
 				Term call = Term.call("some_" + ar, R);
 				Term lemma = call.implies(Term.call(name, R).gt(zero)).forall(R);
 				lemma.setComment("lemma about some_x having card_x > 0");
 				file.addLemma(lemma);
 			}
 		}
 	}
 
 	public void declareDifference(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		String name = "diff_" + ar;
 		if(file.addFunction(relar, name, relar, relar))
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
 			file.addAxiom(axiom);
 		}
 	}
 
 	/** adds a declaration and theory for disjoint 
 	 * @param arity arity of the expression
 	 * @throws ModelException 
 	 */
 	public void declareDisjoint(int ar) throws ModelException {
 		declareAtom();
 		declareRel(ar);
 		String relAr = "Rel" + ar;
 		if( file.addFunction("Bool", "disjoint_"  +ar, relAr, relAr) )
 		{
 			// forall a in A, b in B | not a in B.
 			// or maybe: forall a in A, b in B | not a in B and not b in A...?
 			TermVar A, B;
 			TermVar[] a = makeTuple(ar, "a");
 			A = TermVar.var(relAr, "A");
 			B = TermVar.var(relAr, "B");
 			Term aInB = Term.reverseIn(B, a);
 			Term aInA = Term.reverseIn(A, a);
 			Term exclusive = aInA.and(aInB).not(); // aInA.implies(aInB.not());
 			Term axiom = Term.call("disjoint_" + ar, A, B).iff(exclusive.forall(a)).forall(A, B);
 			axiom.comment = Term.call("disjoint_" + ar, A, B).iff(aInA.implies(aInB.not()).forall(a)).forall(A, B).toString() + "; alternative";
 			file.addAxiom(axiom); // add this axiom to the list of assertions
 		}
 	}
 
 	/** Declares the binary domain restriction operator <:. The resulting relation (S <: R) contains 
 	 * all tuples of R that start with an element in S 
 	 * @param rar arity of the right-hand parameter
 	 * @throws ModelException 
 	 * @rem left-hand arity is required to be 1 (type:set)
 	 */
 	public void declareDomainRestriction(int rar) throws ModelException {
 		declareRel(1);
 		declareRel(rar);
 		String relar = "Rel"+rar;
 		if(file.addFunction(relar, "domRestr_" + rar, "Rel1", relar))
 		{
 			// for all (a1 a2) in R and b in S, ((a1 a2) in (S <: R)) => b = a1
 			TermVar[] a = makeTuple(rar, "a");
 			TermVar b = TermVar.var("Atom", "b");
 			TermVar R = TermVar.var(relar, "R");
 			TermVar S = TermVar.var("Rel1", "S");
 			Term guard = Term.reverseIn(R, a).and(Term.reverseIn(S, b)).and(Term.reverseIn(Term.call("domRestr_" + rar, S, R), a));
 			Term axiom = guard.implies(a[0].equal(b)).forall(R, S, b).forall(a);
 			axiom.setComment("Axiom for domain restriction of arity " + rar);
 			file.addAxiom(axiom);
 		}
 	}
 
 	private void declareFinite() throws ModelException {
 		declareRel(1);
 		file.addFunction("Bool", "finite", "Rel1");
 	}
 
 	private void declareFirst(String suffix) throws ModelException {
 		// implementing "first"
 		declareA2r(1);
 		declareOrd();
 		String name = "first"+suffix;
 		// firstS = sin 1 (ordInv 1 (N [S], 1))
 		if (file.addFunction("Rel1",name)) {
 			Term first = Term.call(name);
 			TermVar N = TermVar.var(suffix); // not actually a variable but a constant
 			TermVar one = TermVar.var("Int", "1"); // not a variable but a constant
 			Term ordInv = Term.call("at", N, one);
 			Term a2r = Term.call("a2r_1", ordInv);
 			Term axiom = first.equal(a2r);
 			axiom.setComment("axiom for " + name);
 			file.addAxiom(axiom);
 		}
 	}
 
 	private void declareI2a() throws ModelException {
 		declareInt();
 	
 		Term N = Term.call("N_Int");
 		String namei2a = "i2a";
 		String namea2i = "a2i";
 		if (file.addFunction("Atom", namei2a, "Int")) {
 			TermVar a = TermVar.var("Atom", "a");
 			Term guard = Term.reverseIn(N, a);
 			// i2a(a2i(a))
 			Term thereAndBack = Term.call(namei2a, Term.call(namea2i, a));
 			// ∀a: Atom | in1 (a, N [Int]) ⇒ i2a(a2i(a)) = a
 			Term axiom = guard.implies(thereAndBack.equal(a)).forall(a);
 			file.addAxiom(axiom);
 		}
 		if (file.addFunction("Int", namea2i, "Atom")) {
 			TermVar i = TermVar.var("Int", "i");
 			// in1 (i2a(i), N [Int])
 			Term inN = Term.call("in_1", Term.call(namei2a, i), N);
 			// a2i(i2a(i))
 			Term thereAndBack = Term.call(namea2i, Term.call(namei2a, i));
 			// ∀i: int | in1 (i2a(i), N [Int]) ∧ a2i(i2a(i)) = i
 			Term axiom = inN.implies(thereAndBack.equal(i)).forall(i);
 			axiom.setComment("axiom for i2a and a2i");
 			file.addAxiom(axiom);
 		}
 	}
 
 	public void declareIdentity() throws ModelException {
 		declareRel(2);		
 		if(file.addFunction("Rel"+2, "iden"))
 		{
 			TermVar	a0 = TermVar.var("Atom", "a0");
 			Term axiom = Term.reverseIn(Term.call("iden"), a0, a0);			
 			file.addAxiom(axiom.forall(a0));
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
 		file.addFunction("Bool", "in_" + ar, params);
 		// axiom omitted: "in" does not have an axiom (uninterpreted)
 	}
 
 	private void declareInt() {
 		file.addFunction("Rel1", "N_Int");	
 	}
 
 	public void declareIntersection(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		if(file.addFunction(relar, "inter_" + ar, relar, relar))
 		{
 			TermVar[] a = makeTuple(ar, "a");
 			TermVar R = TermVar.var(relar, "R");
 			TermVar S = TermVar.var(relar, "S");
 			Term tInIntersec = Term.reverseIn(Term.call("inter_" + ar, R, S), a);
 			Term meaning = Term.reverseIn(R, a).or(Term.reverseIn(S, a));
 			Term axiom = tInIntersec.implies(meaning).forall(R, S).forall(a);
 			axiom.setComment("axiom for intersection " + ar);
 			file.addAxiom(axiom);
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
 		if(file.addFunction("Rel"+resultArity, name, leftRelar, rightRelar))
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
 			file.addAxiom(axiom);
 			// to work properly, we also add some neccessary lemmas
 			assertLemmasJoin(lar, rar);
 		}
 	}
 
 	/** Implements "last" for a given relation. The name of the relation must be a proper suffix
 	 * for a SMT function.
 	 * 
 	 * @param suffix
 	 * @param finite
 	 * @throws ModelException
 	 */
 	private void declareLast(String suffix, boolean finite) throws ModelException {
 		declareNone();
 		// implement Last
 		String name = "last"+suffix;
 		file.addFunction("Rel1", name);
 		
 		// this branch is important because card_1 may not be defined (due to S being infite). SMT does not handle partial functions, so undefined values are not allowed 
 		TermVar N = TermVar.var(suffix); // not actually a variable but a constant
 		Term lastS = Term.call(name);
 		if (!finite) {
 			declareNone();
 			// ¬finite 1 (N [S]) ⇒ lastS = none 1
 			Term none = Term.call("none");
 			Term axiom = Term.call("finite", N).not().implies(lastS.equal(none));
 			axiom.setComment("infinite axiom for " + name);
 			file.addAxiom(axiom);
 		}
 		else {
 			declareCardinality(1);
 			declareOrd();
 			declareA2r(1);
 			// finite 1 (N [S]) ⇒ (lastS = sin 1 (ordInv 1 (N [S], card 1 (N [S]))))
 			Term cardN = Term.call("card_1", N);
 			Term ordInv = Term.call("at", N, cardN);
 			Term a2r = Term.call("a2r_1", ordInv);
 			Term axiom = lastS.equal(a2r);
 			axiom.setComment("finite axiom for " + name);
 			file.addAxiom(axiom);
 			
 			{
 				// lemma about cardinality being the ord of lastX
 				TermVar x = TermVar.var("Atom", "x");
 				Term xInLastS = Term.reverseIn(lastS, x);
 				Term ordX = Term.call("ord", N, x);
 				Term lemma = xInLastS.implies(cardN.equal(ordX)).forall(x);
 				lemma.setComment("lemma about cardinality being the ord of " + lastS);
 				file.addLemma(lemma);
 			}
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
 		if(file.addFunction("Bool", name, relar))
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
 			file.addAxiom(axiom);
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
 		declareFinite();
 		declareNone();		
 		
 		String nextSname = "next"+suffix;
 		if (file.addFunction("Rel2", nextSname)) {
 			TermVar a = TermVar.var("Atom", "a");
 			TermVar b = TermVar.var("Atom", "b");
 			TermVar N = TermVar.var(suffix); // actually constant
 			TermVar one = TermVar.var("1"); // actually constant
 			
 			// ∀a, b: Atom |(in 1 (a, N [S]) ∧ in 1 (b, N [S]))
 			//	⇒ (in 2 (a, b, nextS ) ⇔ ord 1 (N [S], b) = ord 1 (N [S], a) + 1)
 			{
 				Term guard = Term.reverseIn(N, a).and(Term.reverseIn(N, b));
 				Term inS = Term.reverseIn(Term.call(nextSname), a, b);
 				Term follows = Term.call("ord", N, b).equal(Term.call("ord", N, a).plus(one));
 				Term axiom = guard.implies(inS.equal(follows)).forall(a, b);
 				axiom.setComment("axiom for " + nextSname);
 				file.addAxiom(axiom);
 			}
 			// ¬(N [S] = none 1 )
 			{
 				Term axiom = Term.call("none").equal(N).not();
 				axiom.setComment("'there is no empty ordered relation' axiom for " + nextSname);
 				file.addAxiom(axiom);
 			}
 		}
 	}
 
 	private void declareNexts(String suffix) throws ModelException {
 		declareNext(suffix); // "next"+suffix
 		declareTransitiveClosure(); // transClos
 		declareDomainRestriction(2); // domRestr_2
 		declareJoin(1, 2);
 		
 		if (file.addFunction("Rel1", "nexts"+suffix, "Rel1")) {
 			// so/nexts = e . ^ so/Ord . (so/Ord <: Next)
 			// I didn't implement so/Ord, so I'll try it like this: e.^next
 			//Term nexts = Term.call("nexts", params)
 			TermVar e = TermVar.var("Rel1", "e"); // parameter to the function
 			TermVar sig = TermVar.var("Rel1", suffix); // constant
 			Term nextX = Term.call("next" + suffix);
 			Term nexts = Term.call("nexts" + suffix, e);
 			Term tcl = Term.call("transClos", nextX);
 			Term guard = Term.call("subset_1", e, sig);
 			Term axiom = guard.implies(nexts.equal(Term.call("join_1x2", e, tcl))).forall(e);
 			axiom.setComment("axiom for the function 'nexts' of " + suffix);
 			file.addAxiom(axiom);
 		}
 	}
 
 	/** adds a declaration and theory for "no" (the empty function)
 	 * @param arity arity of none
 	 * @throws ModelException
 	 */
 	public void declareNo(int ar) throws ModelException {
 		declareAtom();
 		declareIn(ar);
 		String relar = "Rel" + ar;
 		if(file.addFunction("Bool", "no_" + ar, relar))
 		{
 			// forall tuples of this arity, tuple is not member of none
 			TermVar[] a = makeTuple(ar, "a");
 			TermVar R = TermVar.var(relar, "R");
 			Term no = Term.call("no_"+ar, R);
 			Term memb = Term.reverseIn(R, a);
 			Term axiom = no.implies(memb.not()).forall(R).forall(a);
 			axiom.setComment("axiom for 'the expression is empty'");
 			file.addAxiom(axiom);
 		}
 	}
 
 	public void declareNone() throws ModelException {
 		declareAtom();
 		declareIn(1);
 		
 		if(file.addFunction("Rel1", "none")){
 			// forall tuples, there exists no in None
 			TermVar a = TermVar.var("Atom", "a");
 			Term axiom = Term.reverseIn(Term.call("none"), a).not().forall(a);
 			axiom.setComment("axiom for empty set");
 			file.addAxiom(axiom);
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
 		if(file.addFunction("Bool", name, relar))
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
 			file.addAxiom(axiom);
 		}
 	}
 
 	private void declareOrd() throws ModelException{
 		declareAtom();
 		declareRel(1);
 		
 		if(file.addFunction("Int", "ord", "Rel1", "Atom"))
 		{
 			// ∀r: Reln , a1:n , b1:n : Atom | (finite n (r) ∧ inn (a1:n , r) ∧ inn (b1:n , r) 
 			//	∧ ordn (r, a1:n ) = ordn (r, b1:n )) ⇒ (a1 = b1 ∧ … ∧ an = bn )
 			TermVar R = TermVar.var("Rel1", "R");
 			TermVar a = TermVar.var("Atom", "a");
 			TermVar b = TermVar.var("Atom", "b");
 			
 			Term guard = Term.reverseIn(R, a).and(Term.reverseIn(R, b));
 			Term ordeq = Term.call("ord", R, a).equal(Term.call("ord", R, b));
 			Term axiom = guard.and(ordeq).implies(a.equal(b)).forall(R,a,b);
 			axiom.setComment("axiom for ord");
 			file.addAxiom(axiom);
 		}
 	
 		if(file.addFunction("Atom", "at", "Rel1", "Int"))
 		{
 			// ∀r: Rel1 , a: Atom | in 1 (a, r) ⇒ at 1 (r, ord1 (r, a)) = a
 			TermVar R = TermVar.var("Rel1", "R");
 			TermVar a = TermVar.var("Atom", "a");
 			Term guard = Term.reverseIn(R, a);
 			Term ord = Term.call("ord", R, a);
 			Term axiom = guard.implies(Term.call("at", R, ord).equal(a)).forall(R, a);
 			axiom.setComment("axiom for at (the reverse of ord)");
 			file.addAxiom(axiom);
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
 		//declareI2a();
 		declareOrd();
 		declareNext(suffix);
 		declareNexts(suffix);
 		declareFirst(suffix);
 		declareLast(suffix, finite);
 	}
 
 	/**
 	 * Declares the relational override
 	 * @param ar
 	 * @throws ModelException
 	 * @rem The relational override works like a union of p and q, 
 	 * except that the tuples of q can replaces tuples in p
 	 */
 	public void declareOverride(int ar) throws ModelException {
 		if (ar < 2) {
 			throw new ModelException("Arity of an override expression 'P ++ Q' must be 2 or higher.");
 		}
 		declareRel(ar);
 		String relar = "Rel"+ar;
 		if(file.addFunction(relar, "overr_" + ar, relar, relar))
 		{
 			// forall a=(a0 a1), b=(b0 b1), P and Q, the tuple a is in P++Q if 
 			// 		a is in Q or a is in P and there is no b in Q with b0 = a0
 			TermVar[] a = makeTuple(ar, "a");
 			TermVar[] b = makeTuple(ar, "b");
 			TermVar P = TermVar.var(relar, "P");
 			TermVar Q = TermVar.var(relar, "Q");
 			Term guard = Term.reverseIn(Term.call("overr_"+ar, P, Q), a);
 			Term inQ = Term.reverseIn(Q, a);
 			Term inP = Term.reverseIn(P, a);
 			Term bEqA = a[0].equal(b[0]);
 			Term noB = Term.reverseIn(Q, b).and(bEqA).not();
 			Term axiom = guard.implies(inQ.or(inP.and(noB.exists(b)))).forall(P, Q).forall(a);
 			axiom.setComment("axiom for override, arity " + ar);
 			file.addAxiom(axiom);
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
 		int resultArity = lar + /*sum*/ rar;
 		String resultRelar = "Rel" + /*concatenation*/ resultArity;
 		if(file.addFunction(resultRelar, name, leftRelar, rightRelar))
 		{
 			// add axiom
 			// these are the two parameters for the product function
 			TermVar A = TermVar.var(leftRelar, "A"),
 					B = TermVar.var(rightRelar, "B"); 
 			TermVar[] x = makeTuple(lar, "x"), 
 					y = makeTuple(rar, "y"); // these are two elements, one of A and one of B
 	
 			Term fn = Term.call(name, A, B);
 			Term somethingIsInProduct = Term.reverseIn(fn, Util.concat(x, y));
 			Term xInAandYInB = Term.reverseIn(A, x).and(Term.reverseIn(B, y));
 			Term axiom = somethingIsInProduct.iff(xInAandYInB).forall(A, B).forall(x).forall(y);
 			file.addAxiom(axiom);
 			
 			if(lar == 1 && rar == 1)
 			{
 				/* something like:
 				 *  (assert
 					(forall ((R Rel2)(A Rel1)(B Rel1)) 
 						(=>
 							(subset_2 R (prod_1x1 A B))  ; R=(i.qi) A=IID B=Interface	; guard
 							(forall ((a0 Atom)) 										; body
 							(=>
 								(in_1 a0 A)												; a in A
 								(= 	(not (in_1 a0 (join_2x1 R B)))						; exclusionB
									(no_2 (join_1x2 (a2r_1 a0) R))))))))				; exclusionA
 				 */
 				// lemma for subset and product
 				
 				declareJoin(lar, resultArity);
 				declareJoin(resultArity, rar);
 				declareSubset(resultArity);
 				// declareIn?
 				declareNo((lar + resultArity - 2));
 				
 				TermVar R = TermVar.var(resultRelar, "R");
 				TermVar[] a = makeTuple(lar, "a");
 				
 				Term guard = Term.call("subset_" + resultArity , R, fn);
 				Term ainA = Term.reverseIn(A, a);
 				Term joinA = Term.call("join_" + lar + "x" + resultArity, Term.call("a2r_" + lar, a), R);
 				Term joinB = Term.call("join_" + resultArity + "x" + rar, R, B);
 				Term nojoinA = Term.call("no_" + (lar + resultArity - 2), joinA);
 				Term exclusionA = nojoinA;
 				Term exclusionB = Term.reverseIn(joinB, a).not();
 				Term body = ainA.implies((exclusionB.equal(exclusionA))).forall(a);
 				Term lemma = guard.implies(body).forall(R, A, B);
 				lemma.setComment("lemma about subset " + resultArity + " and product "+ lar + "x" + rar + " , using join");
 				file.addLemma(lemma);
 			}
 		}
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
 		if(file.addFunction(relar, "rangeRestr_" + lar, relar, "Rel1"))
 		{
 			// for all (b1 b2) in R and a in S, ((b1 b2) in (R :> S)) => b2 = a
 			TermVar[] b = makeTuple(lar, "b");
 			TermVar a = TermVar.var("Atom", "a");
 			TermVar R = TermVar.var(relar, "R");
 			TermVar S = TermVar.var("Rel1", "S");
 			Term guard = Term.reverseIn(R, b).and(Term.reverseIn(S, a)).and(Term.reverseIn(Term.call("rangeRestr_" + lar, R, S), b));
 			Term axiom = guard.implies(b[0].equal(a));
 			axiom.setComment("Axiom for range restriction of arity " + lar);
 			file.addAxiom(axiom);
 		}
 	}
 
 	public void declareReflexiveTransitiveClosure() throws ModelException {
 			declareAtom();
 			declareIn(2);
 			declareRel(2);
 			declareTrans();
 			declareIdentity();
 			String name = "reflTransClos";
 			if(file.addFunction("Rel2", name, "Rel2"))
 			{
 				/// add axiom
 				// first we define what transitive means
 				declareTrans();
 				// then we define what a transitive closure is
 				// this is split into 3 assertions
 				// 1. assert r is in trans(r)
 				TermVar r = TermVar.var("Rel2", "r");
 				file.addAxiom(Term.call("subset_2", r, Term.call(name, r)).forall(r));
 				// 2. assert that the transitive closure is -in fact- transitive
 				file.addAxiom(Term.call("trans", Term.call(name, r)).forall(r));
 				// 3. assert iden in TCL
 				Term idenInTCL = Term.call("subset_2", Term.call("iden"), Term.call(name, r)).forall(r);
 				file.addAxiom(idenInTCL);
 				// 4. assert that tcl is minimal
 				TermVar r1 = TermVar.var("Rel2", "r1");
 				TermVar r2 = TermVar.var("Rel2", "r2");
 				
 	//			Term subsetAndTrans = Term.call("subset_2", r1, r2).and(Term.call("trans", r2));
 				Term subsetAndTransAndRefl = Term.call("subset_2", r1, r2).and(Term.call("trans", r2)).and(Term.call("subset_2", Term.call("iden"), r2));
 				
 				Term minimalaxiom = subsetAndTransAndRefl.implies(Term.call("subset_2", Term.call(name, r1), r2)).forall(r1, r2);
 				file.addAxiom(minimalaxiom);
 				// don't forget our lemmas
 				assertLemmasTCL(name);
 			}
 		}
 
 	private void declareRel(int ar) {
 		file.addSort("(declare-sort Rel" + ar + ")");
 	}
 
 	/** declares the multiplicity constraint "some". 
 	 *  Does not take any parameters because multiplicity constraints are always of arity = 1  
 	 * 
 	 * @throws ModelException
 	 */
 	public void declareSome(int ar) throws ModelException {
 		declareRel(1);
 		String name = "some_" + ar;
 		String relar = "Rel" + ar;
 		if(file.addFunction("Bool", name, relar))
 		{
 			// axiom: some means that there is any Atom/Tuple inside the argument expression
 			TermVar A = TermVar.var(relar, "A");
 			
 			TermVar[] aTuple = makeTuple(ar, "a");
 			
 			Term some = Term.call(name, A);
 			Term xInA = Term.reverseIn(A, aTuple);
 			Term axiom = some.iff(xInA.exists(aTuple)).forall(A);
 			file.addAxiom(axiom);
 		}
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
 		if(file.addFunction("Bool", "subset_" + ar, relSort, relSort)){			
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
 			axiom.setComment("subset axiom for " + relSort);
 			file.addAxiom(axiom);  // add this axiom to the list of assertions
 		}
 	}
 
 	private void declareTrans() throws ModelException {
 		declareAtom();
 		declareRel(2);
 		declareIn(2);
 		if(file.addFunction("Bool", "trans", "Rel2")){
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
 			axiomtransitivity.setComment("this axiom defines transitivity");
 			file.addAxiom(axiomtransitivity);
 		}
 	}
 
 	// arity is always 2
 	public void declareTransitiveClosure() throws ModelException {
 		/* This should be something like here https://en.wikipedia.org/wiki/Closure_operator
 		 * so we need 4 axioms:
 		 * 1 transitive
 		 * 2 extensive
 		 * 3 increasing
 		 * 4 idempotent
 		 * 	| <math>X \subseteq \operatorname{cl}(X)</math>
 			| (cl is ''extensive'')
 			|-
 			| <math>X\subseteq Y \Rightarrow \operatorname{cl}(X) \subseteq \operatorname{cl}(Y)</math>
 			| (cl is ''increasing'')
 			|-
 			| <math> \operatorname{cl}(\operatorname{cl}(X))=\operatorname{cl}(X)</math>
 			| (cl is ''idempotent'')
 		 * */
 		declareAtom();
 		declareIn(2);
 		declareRel(2);
 		// helper function to make transitive closure more readable and less redundant
 		declareTrans();
 		String name = "transClos";
 		if(file.addFunction("Rel2", name, "Rel2"))
 		{
 			/// add axiom
 			// we define what a transitive closure is
 			// this is split into 3 assertions
 			// 1. assert that the transitive closure is -in fact- transitive
 			TermVar r1 = TermVar.var("Rel2", "r1");
 			Term tcl = Term.call(name, r1);
 			Term transitive = Term.call("trans", tcl).forall(r1);
 			transitive.setComment("this axiom satisfies transitivity for transclos");
 			file.addAxiom(transitive);
 			// 2. assert that transcl(r) is extensive
 			Term extensive = Term.call("subset_2", r1, Term.call(name, r1)).forall(r1);
 			extensive.setComment("this axioms satisfies that tcl is extensive");
 			file.addAxiom(extensive);
 			// 3. assert that tcl is increasing
 			TermVar r2 = TermVar.var("Rel2", "r2");
 			Term subset = Term.call("subset_2", r1, r2);
 			Term increasing = subset.implies(Term.call("subset_2", tcl, r2)).forall(r1, r2);
 			increasing.setComment("this axiom satisfies that transclos is increasing");
 			file.addAxiom(increasing);
 			// 4. assert that tcl is idempotent
 			Term closclos = Term.call(name, tcl);
 			Term idempotent = closclos.equal(tcl).forall(r1);
 			idempotent.setComment("this axiom satisfies that tcl should be idempotent");
 			file.addAxiom(idempotent);
 			// also, add some lemma about in_2 and the transCl
 			assertLemmasTCL("transClos");
 		}
 	}
 
 	// arity is always 2
 	public void declareTranspose() throws ModelException {
 		declareAtom();
 		declareIn(2);
 		declareRel(2);
 		String name = "transp";
 		if(file.addFunction("Rel2", name, "Rel2"))
 		{
 			// ∀r: Rel2 , a1 , a2 : Atom | in2 (a1 , a2 , transpose 2 (r)) ⇔ in2 (a2 , a1 , r)
 			TermVar R = TermVar.var("Rel2", "R");
 			TermVar[] a = makeTuple(2, "a");
 			
 			Term axiom = Term.reverseIn(Term.call(name, R), a).equal(Term.reverseIn(R, Util.reverse(a))).forall(Util.concat(a, R));
 			axiom.setComment("axiom for transposition");
 			file.addAxiom(axiom);
 		}
 	}
 
 	public void declareUnion(int ar) throws ModelException {
 		declareRel(ar);
 		String relar = "Rel" + ar;
 		String name = "union_" + ar;
 		if(file.addFunction(relar, name, relar, relar))
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
 			Term axiom = Term.reverseIn(call, x).iff(orTerm).forall(arglist);
 			axiom.setComment("axiom for union of " + relar);
 			file.addAxiom(axiom);
 		}
 	}
 
 	private TermVar[] makeTuple(int ar, String basename) {
 		TermVar[] x = new TermVar[ar];
 		for (int i = 0; i < x.length; i++) {
 				x[i] = TermVar.var("Atom", basename+i); // the atoms are named "a0" "a1" etc
 		}
 		return x;
 	}
 
 	public Term a2r(int ar, Term ... sub) throws ModelException {
 		declareA2r(ar);
 		return Term.call("a2r_"+ar, sub);
 	}
 
 	private Term a2r(TermVar[] vars) throws ModelException {
 		return a2r(vars.length, vars);
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
 		file.addLemma(lemma);
 		lemma = aInR.implies(resultInJoin).forall(Util.concat(atoms, r));
 		lemma.setComment("2. lemma for "+name+". direction: in to join");
 		file.addLemma(lemma);
 		
 		if(lar == 1 && rar == 2){
 			/* lemma for the com-theorem1
 			 * ; R=(i.qi) S=iids
 				(assert (! (forall ((R Rel2)(S Rel2)(x Atom))
 					(=>
 						(no_2 (join_1x2 (a2r_1 x) R)) 
 						(no_2 (join_1x2 (a2r_1 x) (join_2x2 R S))))))
 						:named step21
 				)
 			*/
 			declareJoin(2, 2);
 			declareA2r(1);
 			declareNo(1);
 			TermVar R = TermVar.var("Rel2", "R");
 			TermVar S = TermVar.var("Rel2", "S");
 			TermVar x = TermVar.var("Atom", "x");
 			Term Xrel = Term.call("a2r_1", x);
 			Term joinRS = Term.call("join_2x2", R, S);
 			Term guard = Term.call("no_1", Term.call(name, Xrel, R));
 			Term body = Term.call("no_1", Term.call(name, Xrel, joinRS));
 			lemma = guard.implies(body).forall(R, S, x);
 			lemma.setComment("lemma for step 21 of the com-theorem1 for join_1x2: R=(i.qi) S=iids");
 			file.addLemma(lemma);
 		}
 	}
 
 	/**
 	 * Adds lemmas for transitive closure to work properly. 
 	 * These lemmas are the result of experimentation with the z3 solver. 
 	 * @param name
 	 * @throws ModelException
 	 */
 	private void assertLemmasTCL(String name) throws ModelException {		
 		/* lemma 1 about the "second-last element": 
 		 * (forall ((a1 Atom)(a3 Atom)(r Rel2)) 
 		 * 	(=> 
 		 * 		(in_2 a1 a3 (transClos r))	; guard
 		 * 	(exists ((a2 Atom)) 
 		 * 	(or 							; body
 		 * 		(not (in_2 a1 a2 r)) 		; not inR
 		 * 		(and (in_2 a1 a2 r) (in_2 a2 a3 (transClos r))))))) ; inR && middleInTCL 
 		 */
 		TermVar a1 = TermVar.var("Atom", "a1");
 		TermVar a2 = TermVar.var("Atom", "a2"); // "middle element"
 		TermVar a3 = TermVar.var("Atom", "a3");
 		
 		TermVar R = TermVar.var("Rel2", "R");
 		Term tCl = Term.call(name, R);
 				
 		Term guard = Term.reverseIn(tCl, a1, a3);
 		{
 			Term inR = Term.reverseIn(R, a1, a2);
 			Term middleInTCL = Term.reverseIn(tCl, a2, a3);
 			Term body = inR.not().or(inR.and(middleInTCL)).forall(a2);
 			Term lemma1 = guard.implies(body).forall(a1, a3, R);
 			lemma1.setComment("lemma 1 for " + name + " about the second-last 'middle element'");
 			file.addLemma(lemma1);
 		}
 		/* lemma 2 about the "second element":
 		 * (forall ((a1 Atom)(a3 Atom)(r Rel2)) (=> 
 		 * (in_2 a1 a3 (transClos r))							; guard 
 		 * (exists ((a2 Atom)) (or 
 		 * 	(not (in_2 a2 a3 r)) 								; not inR
 		 * 	(and (in_2 a2 a3 r) (in_2 a1 a2 (transClos r)))))) 
 		 */
 		{
 			Term inR = Term.reverseIn(R, a2, a3);
 			Term middleInTCL = Term.reverseIn(tCl, a1, a2);
 			Term body = inR.not().or(inR.and(middleInTCL)).forall(a2);
 			Term lemma2 = guard.implies(body).forall(a1, a3, R);
 			lemma2.setComment("lemma 1 for " + name + " about the second 'middle element'");
 			file.addLemma(lemma2);
 		}
 	}
 
 }
