 package be.uclouvain.jail.algo.fa.rand;
 
 import be.uclouvain.jail.algo.graph.rand.DefaultRandomGraphInput;
 import be.uclouvain.jail.algo.graph.rand.IRandomGraphInput;
 import be.uclouvain.jail.fa.IAlphabet;
 import be.uclouvain.jail.fa.IDFA;
 import be.uclouvain.jail.fa.constraints.AccessibleDFAGraphConstraint;
 import be.uclouvain.jail.fa.constraints.NoDeadlockGraphConstraint;
 import be.uclouvain.jail.fa.impl.GraphDFA;
 import be.uclouvain.jail.fa.utils.DFAQueryable;
 import be.uclouvain.jail.fa.utils.IntegerAlphabet;
 import be.uclouvain.jail.graph.IDirectedGraph;
 import be.uclouvain.jail.graph.IGraphPredicate;
 import be.uclouvain.jail.graph.constraints.AbstractGraphPredicate;
 
 /**
  * Provides an {@link IRandomGraphInput} implementation to generate
  * DFAs.
  * 
  * @author blambeau
  */
 public class RandomDFAInput extends DefaultRandomGraphInput {
 
 	/** State count multiplication factor. */
 	private double stateMultFactor = 1.62;
 	
 	/** Accepting probability. */
 	protected double accepting = 0.5;
 	
 	/** Maximal out degree. */
 	protected double maxOutDegree = 4;
 	
 	/** Tolerance on stateCount. */
 	protected double tolerance = 0.1;
 	
 	/** Make a depth control? */
 	protected boolean depthControl = false;
 	
 	/** No deadlock ? */
 	protected boolean noDeadlock = true;
 	
 	/** Alphabet size to generate. */
 	protected IAlphabet<?> alphabet = new IntegerAlphabet(2);
 	
 	/** Installs the options. */
 	@Override
 	protected void installOptions() {
 		super.installOptions();
 		super.addOption("stateMultFactor", false, Double.class, null);
 		super.addOption("accepting", false, Double.class, null);
 		super.addOption("maxOutDegree", false, Double.class, null);
 		super.addOption("tolerance", false, Double.class, null);
 		super.addOption("alphabetSize", false, Integer.class, null);
 		super.addOption("depthControl", false, Boolean.class, null);
		super.addOption("noDeadlock", false, Boolean.class, null);
 	}
 
 	/** Sets state count multiplication factor. */
 	public void setStateMultFactor(double stateMultFactor) {
 		this.stateMultFactor = stateMultFactor;
 	}
 	
 	/** Sets the probability to get an accepting state. */
 	public void setAccepting(double accepting) {
 		this.accepting = accepting;
 	}
 
 	/** Sets the tolerance on number of states. */
 	public void setTolerance(double tolerance) {
 		this.tolerance = tolerance;
 	}
 
 	/** Sets a user-defined alphabet size. */
 	public void setAlphabetSize(int size) {
 		this.alphabet = new IntegerAlphabet(size);
 	}
 	
 	/** Sets maximal output degree of states. */
 	public void setMaxOutDegree(double max) {
 		this.maxOutDegree = max;
 	}
 	
 	/** Sets depth control. */
 	public void setDepthControl(boolean depthControl) {
 		this.depthControl = depthControl;
 	}
 	
 	/** Rejects deadlock states?. */
 	public void setNoDeadLock(boolean noDeadlock) {
 		this.noDeadlock = noDeadlock;
 	}
 
 	/** Accepts the DFA if DFAGraphConstraint accepts it. */
 	public IGraphPredicate getAcceptPredicate() {
 		return new AbstractGraphPredicate() {
 			@Override
 			public boolean evaluate(IDirectedGraph graph) {
 				if (graph.getVerticesTotalOrder().size() == 0) {
 					//System.out.println("Empty DFA.");
 					return false;
 				}
 				
 				// check that it's a DFA
 				if (!new AccessibleDFAGraphConstraint().isRespectedBy(graph)) {
 					//System.out.println("Not a DFA.");
 					return false;
 				}
 				
 				// check no deadlock
 				if (noDeadlock) {
 					if (!new NoDeadlockGraphConstraint().isRespectedBy(graph)) {
 						//System.out.println("No deadlock.");
 						return false;
 					}
 				}
 				
 				// check tolerance if set
 				if (tolerance != -1.0) {
 					double wish = vertexCount;
 					double actual = graph.getVerticesTotalOrder().size();
 					double diff = Math.abs(wish-actual)/wish;
 					if (diff >= tolerance) { 
 						//System.out.println("Tolerance: " + wish + " " + actual);
 						return false; 
 					}
 				}
 				
 				// check depth if required
 				IDFA dfa = new GraphDFA(graph);
 				DFAQueryable queried = new DFAQueryable(dfa);
 				int depth = queried.getDepth();
 				if (depthControl) {
 					long expected = Math.round((2*Math.log(vertexCount)/Math.log(2)) - 2);
 					if (depth != expected) {
 						//System.out.println("Depth: " + expected + " " + depth);
 						return false;
 					}
 				} else if (depth == 0) {
 					return false;
 				}
 				
 				// seems ok
 				return true;
 			}
 		};
 	}
 
 	/** Returns true when created states reach stateCount. */
 	public IGraphPredicate getVertexStopPredicate() {
 		return new AbstractGraphPredicate() {
 			public boolean evaluate(IDirectedGraph graph) {
 				return graph.getVerticesTotalOrder().size() == 
 				   new Double(vertexCount*stateMultFactor).intValue();
 			}
 		};
 	}
 
 	/** Stops edge creation when... */
 	public IGraphPredicate getEdgeStopPredicate() {
 		return new AbstractGraphPredicate() {
 			public boolean evaluate(IDirectedGraph g) {
 				// actual state count
 				double st = new Double(vertexCount*stateMultFactor);
 				// alphabet size
 				double as = alphabet.getLetters().size();
 				// maximal out degree
 				double maxd = Math.min(as, maxOutDegree);
 				// current number of edges
 				double ec = g.getEdgesTotalOrder().size();
 				// maximal number of edges
 				double maxe = (st * maxd);
 				// at least one half of all possible edges reached?
 				if (ec >= maxe) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		};
 	}
 
 }
