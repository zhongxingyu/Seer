 package kodkod.engine.bool;
 
 import static kodkod.engine.bool.BooleanConstant.FALSE;
 import static kodkod.engine.bool.BooleanConstant.TRUE;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import kodkod.engine.bool.MultiGate.Operator;
 import kodkod.util.LinkedStack;
 import kodkod.util.Stack;
 
 
 /**
  * A factory for creating and assembling {@link kodkod.engine.bool.BooleanValue boolean values}.
  * Non-constant are not status among factories.
  * 
  * @specfield components: set BooleanValue
  * @invariant no f1, f2: BooleanFactory | f1 != f2 => f1.components & f2.components = BooleanConstant
  * @author Emina Torlak
  */
 public final class BooleanFactory {
 	/**
 	 * Stores input variables.
 	 * @invariant all i: [1..iLits.size()] | vars[i-1].positive.literal = i
 	 */
 	private BooleanVariable[] vars;
 	
 	/**
 	 * Stores gate caches; e.g. the map that caches OR gates is in gates[OR].
 	 * @invariant all map: gates[int] | all g1, g2: map.values() | g1.op = g2.op
 	 * @invariant all map: gates[int] | all entry: map | entry.key = entry.value.digest(value.op)
 	 */
 	private final Map<Integer, Stack<ImmutableMultiGate>>[] gates;
 	
 	private int eqDepth;
 	private int nextLiteral;
 	
 	/**
 	 * Constructs a circuit factory with the given number of input variables.  Gates are
 	 * checked for semantic equality down to the given depth.
 	 * @requires 0 <= numInputVariables < Integer.MAX_VALUE 
 	 * @requires checkToDepth >= 0
 	 * @effects #this.components' = numInputVariables && this.components' in BooleanVariable &&
 	 *          (all i: [1..numInputVariables] | one this.components'.literal & i }
 	 */
 	@SuppressWarnings("unchecked")
 	private BooleanFactory(int numInputVariables, int checkToDepth) {
 		eqDepth = checkToDepth;
 		vars = new BooleanVariable[numInputVariables];
 		for(int i = 0; i < numInputVariables; i++) {
 			vars[i]= new BooleanVariable(i+1);                                                                        
 		}
 		nextLiteral = numInputVariables + 1;
 		gates = new HashMap[2];
 		gates[0] = new HashMap<Integer,Stack<ImmutableMultiGate>>();
 		gates[1] = new HashMap<Integer,Stack<ImmutableMultiGate>>();
 	}
 	
 	/**
 	 * Returns a circuit factory, initialized to contain the given number
 	 * of input variables that can be used in circuit construction.  The
 	 * integer representations of the initial variables are the literals
 	 * [1..numInputVariables].  Gates are checked for semantic equality 
 	 * down to depth 5, when composing them using BooleanFactory#compose
 	 * method.  The effect of this method is the same as calling BooleanFactory.factory(numInputVariables, 5).
 	 * @return {f: BooleanFactory | #f.components = numInputVariables && f.components in BooleanVariable &&
 	 *                              (all v: f.components | v.generator = v) &&
 	 *                              (all i: [1..numInputVariables] | one f.components.literal & i }}
 	 * @throws IllegalArgumentException - numInputVariables < 0 || numInputVariables > Integer.MAX_VALUE - 1
 	 */
 	public static BooleanFactory factory(int numInputVariables) {
 		return factory(numInputVariables, 5);
 	}
 	
 	/**
 	 * Returns a circuit factory, initialized to contain the given number
 	 * of input variables that can be used in circuit construction.  The
 	 * integer representations of the initial variables are the literals
 	 * [1..numInputVariables].  Gates are checked for semantic equality 
 	 * down to the given depth when composing them using BooleanFactory#compose
 	 * method.  In general,  setting the
 	 * comparison depth to a high value will result in more 
 	 * subcomponents being shared.  However, it will also slow down
 	 * gate construction.  
 	 * @return {f: BooleanFactory | #f.components = numInputVariables && f.components in BooleanVariable &&
 	 *                              (all v: f.components | v.generator = v) &&
 	 *                              (all i: [1..numInputVariables] | one f.components.literal & i }}
 	 * @throws IllegalArgumentException - numInputVariables < 0 || numInputVariables > Integer.MAX_VALUE - 1
 	 * @throws IllegalArgumentException - compDepth < 0
 	 */
 	public static BooleanFactory factory(int numInputVariables, int compDepth) {
 		if (numInputVariables < 0 || numInputVariables == Integer.MAX_VALUE) 
 			throw new IllegalArgumentException("numInputVariables < 0 || numInputVariables > Integer.MAX_VALUE - 1");
 		if (compDepth < 0) throw new IllegalArgumentException("checkToDepth < 0");
 		return new BooleanFactory(numInputVariables, compDepth);
 	}
 	
 	/**
 	 * Returns the depth (from the root) to which components are checked for 
 	 * semantic equality during gate construction.
 	 * @return maximum depth to which components are checked for equality
 	 */
 	public int comparisonDepth() { return eqDepth; }
 	
 	/**
 	 * Sets the comparison depth to the given value.  Setting the
 	 * comparison depth to a high value will result in more 
 	 * subcomponents being shared.  However, it will also slow down
 	 * gate construction.
 	 * @effects sets the comparison depth to the given value
 	 * @throws IllegalArgumentException - newDepth < 0
 	 */
 	public void setComparisonDepth(int newDepth) {
 		if (newDepth < 0)
 			throw new IllegalArgumentException("newDepth < 0: " + newDepth);
 		eqDepth = newDepth;
 	}
 	
 	/**
 	 * Returns the largest literal corresponding to a formula created by this factory.
 	 * @return max((BooleanFormula & this.components).literal)
 	 */
 	public int maxFormulaLiteral() { return nextLiteral-1; }
 	
 	/**
 	 * Returns the variable with the given literal, if it has already been produced
 	 * by this factory.  If not, null is returned.
 	 * @return (this.components & BooleanVariable).literal
 	 */
 	public BooleanVariable variable(int literal) {
 		return (literal > 0 && literal <= vars.length ? 
 				vars[literal - 1] : null);
 	}
 	
 	/**
 	 * Returns the largest literal corresponding to a variable created by this factory.
 	 * @return max((BooleanVariable & this.components).literal)
 	 */
 	public int maxVariableLiteral() { return vars.length; }
 	
 	/**
 	 * Returns the negation of the given boolean value.
 	 * @return {n: BooleanValue | n.literal = -v.literal && [[n]] = ![[v]] }
 	 * @effects (components.v).components' = (components.v).components + n 
 	 * @throws NullPointerException - v = null                             
 	 */
 	public BooleanValue not(BooleanValue v) {
 		return v.negation();
 	}
 	
 	/**
 	 * Returns a boolean value that represents the conjunction of the input components.  
 	 * The effect of this method is the same as calling this.compose(MultiGate.Operator.AND, v0, v1).
 	 * @return {v: BooleanValue | [[v]] = [[v0]] AND [[v1]] }
 	 * @effects this.components' = this.components + v 
 	 * @throws NullPointerException - any of the arguments are null
 	 * @throws IllegalArgumentException - v0 + v1 !in this.components
 	 */
 	public final BooleanValue and(BooleanValue v0, BooleanValue v1) {
 		return compose(MultiGate.Operator.AND, v0, v1);
 	}
 	
 	/**
 	 * Returns a boolean value that represents the disjunction of the input components.  
 	 * The effect of this method is the same as calling this.compose(MultiGate.Operator.OR, v0, v1).
 	 * @return {v: BooleanValue | [[v]] = [[v0]] OR [[v1]] }
 	 * @effects this.components' = this.components + v 
 	 * @throws NullPointerException - any of the arguments are null
 	 * @throws IllegalArgumentException - v0 + v1 !in this.components
 	 */
 	public final BooleanValue or(BooleanValue v0, BooleanValue v1) {
 		return compose(MultiGate.Operator.OR, v0, v1);
 	}
 	
 	/**
 	 * Returns a boolean value that represents the formula v0 => v1.  The effect
 	 * of this method is the same as calling this.compose(OR, this.not(v0), v1).
 	 * @return { v: BooleanValue | [[v]] = [[v0]] => [[v1]] }
 	 * @effects this.components' = this.components + v
 	 * @throws NullPointerException - any of the arguments are null
 	 * @throws IllegalArgumentException - v0 + v1 !in this.components
 	 */
 	public final BooleanValue implies(BooleanValue v0, BooleanValue v1) {
 		return compose(MultiGate.Operator.OR, v0.negation(), v1);
 	}
 	
 	/**
 	 * Returns a boolean value that represents the formula v0 = v1.  The
 	 * effect of this method is the same as calling 
 	 * this.and(this.implies(v0, v1), this.implies(v1, v0)).
 	 * @return { v: BooleanValue | [[v]] = [[v0]] iff [[v1]] }
 	 * @effects this.components' = this.components + v
 	 * @throws NullPointerException - any of the arguments are null
 	 * @throws IllegalArgumentException - v0 + v1 !in this.components
 	 */
 	public final BooleanValue iff(BooleanValue v0, BooleanValue v1) {
 		return compose(MultiGate.Operator.AND, implies(v0,v1), implies(v1,v0));
 	}
 	
 	/**
 	 * Returns a boolean value that represents
 	 * the composition of the inputs using the given operator.
 	 * @return {v: BooleanValue | [[v]] = [[v0]] op [[v1]]  }
 	 * @effects this.components' = this.components + v
 	 * @throws NullPointerException - any of the arguments are null
 	 * @throws IllegalArgumentException - v0 + v1 !in this.components
 	 */
 	public BooleanValue compose(MultiGate.Operator op, BooleanValue v0, BooleanValue v1) {
 		if (!contains(v0) || !contains(v1))
 			throw new IllegalArgumentException("v0 + v1 !in this.components");
 		return fastCompose(op, v0, v1);
 	}
 	
 	/**
 	 * Converts the given mutable gate into an immutable value and adds it to this.components.
 	 * This method requires that all of g's inputs are in this.components.  If g has no inputs,
 	 * its operator's identity constant is returned.  If g has one input, that input is returned.
 	 * Otherwise, an immutable value that is semantically equivalent to g is returned.
 	 * @return no g.inputs => g.op.identity(), 
 	 *         one g.inputs => g.inputs, 
 	 *         {g' : BooleanValue - MutableMultiGate | [[g']] = [[g]] }
 	 * @effects this.components' = this.components + g'
 	 * @throws IllegalArgumentException - g.inputs !in this.components
 	 */
 	public BooleanValue toImmutableValue(MutableMultiGate g) {
 		for(Iterator<BooleanValue> inputs = g.inputs(); inputs.hasNext();) {
 			if (!contains(inputs.next())) throw new IllegalArgumentException();
 		}
 		return makeImmutable(g);
 	}
 	
 	/**
 	 * Returns the map used for caching the multigates with the given operator.
 	 */
 	private Map<Integer,Stack<ImmutableMultiGate>> cacheFor(Operator op) {
 		return gates[op.ordinal()];
 	}
 	
 	/**
 	 * Returns the stack in which the multigates with the given operator and
 	 * digest (with respect to that operator) are cached.  If such a stack does not exist, it 
 	 * is created, added to the cache, and returned.
 	 * @return some gates[op][digest] => gates[op][digest], s: LinkedStack | s = new LinkedStack()
 	 * @effects gates[op].map' = gates[op].map + digest->s 
 	 */
 	private Stack<ImmutableMultiGate> stackFor(Operator op, int digest) {
 		Stack<ImmutableMultiGate> gates = cacheFor(op).get(digest);
 		if (gates==null) {
 			gates = new LinkedStack<ImmutableMultiGate>();
 			cacheFor(op).put(digest, gates);
 		}
 		return gates;
 	}
 	
 	/**
 	 * Returns true if v is in this.components.
 	 * @return v in this.components
 	 * @throws NullPointerException - v = null
 	 */
 	public boolean contains(BooleanValue v) {
 		if (v==TRUE || v==FALSE) return true;
 		if (v.literal()==0) return false;
 		if (v.literal() < 0) v = v.negation();
 		final int absLit = v.literal();
 		if (absLit <= vars.length) {
 			return v == vars[absLit-1];
 		} else {
 			final MultiGate g = (MultiGate) v;
 			final Stack<ImmutableMultiGate> s = cacheFor(g.op).get(g.digest(g.op));
 		    if (s!=null) {
 		    	for (Iterator<ImmutableMultiGate> gates = s.iterator(); gates.hasNext();) {
 		    		if (gates.next()==g) return true;
 		    	}
 		    }
 			return false;
 		}
 	}
 	
 	/**
 	 * Makes the given mutable gate into an immutable formula and adds it to this.components.
 	 * This method requires that all of g's inputs are in this.components.  If g has no inputs,
 	 * its operator's identity constant is returned.  If g has one input, that input is returned.
 	 * Otherwise, an immutable value that is semantically equivalent to g is returned.
 	 * @requires g.inputs in this.components 
 	 * @return no g.inputs => g.op.identity(), 
 	 *         one g.inputs => g.inputs, 
 	 *         {g' : BooleanValue - MutableMultiGate | [[g']] = [[g]] }
 	 * @effects this.components' = this.components + g'
 	 */
 	BooleanValue makeImmutable(MutableMultiGate g) {
 		final int gsize = g.numInputs();
 		if (gsize==0) return g.op.identity();
 		else if (gsize==1) return g.inputs().next();
 		else if (gsize==2) {
 			final Iterator<BooleanValue> inputs = g.inputs();
 			return fastCompose(g.op, inputs.next(), inputs.next());
 		} else { // g.numInputs > 2
 			final Stack<ImmutableMultiGate> s = stackFor(g.op, g.digest(g.op));
 			final int parts = g.numAtomicParts(g.op);
 			
 			for(Iterator<ImmutableMultiGate> gates = s.iterator(); gates.hasNext();) {
 				ImmutableMultiGate gate = gates.next();
 				if (gate.numAtomicParts(g.op)==parts) {
 					boolean same = true;
 					for(Iterator<BooleanValue> inputs = g.inputs(); inputs.hasNext(); ) {
 						if (!((BooleanFormula) inputs.next()).isPartOf(g.op, gate, eqDepth, eqDepth)) {
 							same = false;
 							break;
 						}
 					}
 					if (same) return gate;
 				}
 			}
 			return s.push(ImmutableMultiGate.make(g, nextLiteral++));
 		}
 	}
 	
 	/**
 	 * Returns a multigate with the given operator and children.  If such a gate
 	 * has already been created, it is returned.  Otherwise, it is created, cached,
 	 * and returned.
 	 * @requires f0 and f1 have already been reduced with respect to the given operator
 	 * (i.e. f0!=f1 && f0 != !f1, etc.)
 	 * @return {m: MultiGate | m.op = op && m.inputs = f0 + f1
 	 * @effects this.gates[op.ordinal]' = this.gates[op.ordinal] + m.digest(op)->m
 	 */
 	private ImmutableMultiGate cache(MultiGate.Operator op, BooleanFormula f0, BooleanFormula f1) {
 		final int digest = f0.digest(op) + f1.digest(op), parts = f0.numAtomicParts(op) + f1.numAtomicParts(op);
 		final Stack<ImmutableMultiGate> s = stackFor(op, digest);
 		
 		for(Iterator<ImmutableMultiGate> gates = s.iterator(); gates.hasNext();) {
 			ImmutableMultiGate gate = gates.next();
 			if (gate.numAtomicParts(op)==parts && 
 				f0.isPartOf(op, gate, eqDepth, eqDepth) &&
 				f1.isPartOf(op, gate, eqDepth, eqDepth)) {
 				return gate;
 			}
 		}
 		return s.push(ImmutableMultiGate.make(op, nextLiteral++, f0, f1));
 	}
 	
 	/**
 	 * Returns a boolean value that represents
 	 * the composition of the inputs using the given operator, 
 	 * without checking that v0 and v1 are in this components
 	 * @return {v: BooleanValue | [[v]] = [[v0]] op [[v1]]  }
 	 * @effects this.components' = this.components + v
 	 * @throws NullPointerException - any of the arguments are null
 	 */
 	BooleanValue fastCompose(MultiGate.Operator op, BooleanValue v0, BooleanValue v1) {
 		if (v0==TRUE || v0==FALSE) {
 			return v0==op.shortCircuit() ? v0 : v1;
 		} else if (v1==TRUE || v1==FALSE) {
 			return v1==op.shortCircuit() ? v1 : v0;
 		} else {
 			
 			final int l0 = v0.literal(), l1 = v1.literal();
 			
 			if (l0==l1) return v0; // a op a = a
 			else if (l0==-l1) return op.shortCircuit(); // a op !a = op.shortCircuit
 			else {
 				final BooleanFormula f0 = (BooleanFormula) v0, f1 = (BooleanFormula) v1;
 
 				if (f1.isPartOf(op, f0, eqDepth, eqDepth) ||              // (a op b) op a = (a op b)
 					f0.isPartOf(op.complement(), f1, eqDepth, eqDepth)) { // (a op.complement b) op a = a
 					return f0; 
 				} else if (f0.isPartOf(op, f1, eqDepth, eqDepth) ||             
 						   f1.isPartOf(op.complement(), f0, eqDepth, eqDepth)) {
 					return f1; 
 				} else {
 					return cache(op, f0, f1);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Removes all formulas whose literal is higher
 	 * than the specified value from this.components.
 	 * @effects this.componets' = 
 	 *     this.components - { f: BooleanFormula | |f.literal| > |maxLit| }
 	 */
 	public void clear(int maxLit) {
 		maxLit = StrictMath.abs(maxLit);
 		if (maxLit<=vars.length) {
 			this.gates[0].clear();
 			this.gates[1].clear();
 			BooleanVariable[] temp = new BooleanVariable[maxLit];
 			System.arraycopy(this.vars, 0, temp, 0, maxLit);
 			this.vars = temp;
 			this.nextLiteral = vars.length + 1;
 		} else if (maxLit < this.nextLiteral-1) {
 			for(int i = 0; i < 2; i++) {
 				for(Iterator<Map.Entry<Integer, Stack<ImmutableMultiGate>>> entries = gates[i].entrySet().iterator(); entries.hasNext(); ) {
 					Map.Entry<Integer, Stack<ImmutableMultiGate>> entry = entries.next();
 					Stack<ImmutableMultiGate> s = entry.getValue();
 					for(Iterator<ImmutableMultiGate> sIter = s.iterator(); sIter.hasNext();) {
 						ImmutableMultiGate gate = sIter.next();
 						if (gate.literal() > maxLit)
 							sIter.remove();
 					}
 					if (s.empty())
 						entries.remove();
 				}
 			}
 			this.nextLiteral = maxLit + 1;
 		}
 	}
 	
 	/**
 	 * Returns a BooleanMatrix with the given dimensions, zero, and this 
 	 * as the factory for its non-zero components.  
 	 * @throws NullPointerException - any of the arguments are null 
 	 * @return { m: BooleanMatrix | no m.elements && m.factory = this && m.dimensions = dims && m.zero = zero }
 	 */
 	public BooleanMatrix matrix(Dimensions d, BooleanConstant c) {
		if (d == null || c == null) throw new NullPointerException();
 		return new BooleanMatrix(d, c, this);
 	}
 }
