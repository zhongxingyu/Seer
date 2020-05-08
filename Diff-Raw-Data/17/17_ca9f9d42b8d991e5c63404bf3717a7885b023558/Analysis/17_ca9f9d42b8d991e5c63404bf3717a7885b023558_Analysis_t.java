 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.log4j.Logger;
 
 import soot.BooleanType;
 import soot.IntType;
 import soot.SootMethod;
 import soot.Type;
 import soot.Unit;
 import soot.Value;
 import soot.jimple.AddExpr;
 import soot.jimple.AndExpr;
 import soot.jimple.BinopExpr;
 import soot.jimple.CaughtExceptionRef;
 import soot.jimple.ClassConstant;
 import soot.jimple.ConditionExpr;
 import soot.jimple.DefinitionStmt;
 import soot.jimple.DivExpr;
 import soot.jimple.IfStmt;
 import soot.jimple.InstanceFieldRef;
 import soot.jimple.IntConstant;
 import soot.jimple.InvokeExpr;
 import soot.jimple.LengthExpr;
 import soot.jimple.MulExpr;
 import soot.jimple.NegExpr;
 import soot.jimple.NewArrayExpr;
 import soot.jimple.NewExpr;
 import soot.jimple.OrExpr;
 import soot.jimple.ParameterRef;
 import soot.jimple.RemExpr;
 import soot.jimple.ShlExpr;
 import soot.jimple.ShrExpr;
 import soot.jimple.StaticFieldRef;
 import soot.jimple.Stmt;
 import soot.jimple.SubExpr;
 import soot.jimple.ThisRef;
 import soot.jimple.UshrExpr;
 import soot.jimple.XorExpr;
 import soot.jimple.internal.JArrayRef;
 import soot.jimple.internal.JInstanceFieldRef;
 import soot.jimple.internal.JNewArrayExpr;
 import soot.jimple.internal.JimpleLocal;
 import soot.jimple.toolkits.annotation.logic.Loop;
 import soot.toolkits.graph.BriefUnitGraph;
 import soot.toolkits.graph.DirectedGraph;
 import soot.toolkits.graph.LoopNestTree;
 import soot.toolkits.scalar.ForwardBranchedFlowAnalysis;
 
 /**
  * Computes a fix point of the abstract variable states in an individual method
  * using the interval domain.
  * 
  * Besides mapping integer and boolean variables to an interval, it also maps an
  * array allocated in the method to an interval, indicating its possible size.
  */
 public class Analysis extends ForwardBranchedFlowAnalysis<IntervalPerVar> {
 
 	/**
 	 * The maximum number of times that a back-jump statement of any loop in the
 	 * method is allowed be handled by the method
 	 * {@link #flowThrough(IntervalPerVar, Unit, List, List)} before widening
 	 * kicks in.
 	 */
 	private static final int LOOP_BACK_JUMP_COUNT_THRESHOLD = 100;
 
 	private static final Logger LOG = Logger.getLogger(Analysis.class);
 
 	private final Map<NewArrayExpr, Interval> allocationNodeMap;
 
 	/**
 	 * Keeps track of how many times the back-jump statement of any loop in the
 	 * method was handled in the
 	 * {@link #flowThrough(IntervalPerVar, Unit, List, List)} method.
 	 */
 	private final Map<Stmt, Integer> loopBackJumpCountMap;
 
 	private final LoopNestTree loopNestTree;
 
 	public Analysis(SootMethod method) {
 		super(new BriefUnitGraph(method.retrieveActiveBody()));
 		allocationNodeMap = new HashMap<NewArrayExpr, Interval>();
 		loopNestTree = new LoopNestTree(method.retrieveActiveBody());
 		loopBackJumpCountMap = new HashMap<Stmt, Integer>();
 
 		// Set the counter for every back-jump statement to zero
 		for (Loop loop : loopNestTree) {
 			loopBackJumpCountMap.put(loop.getBackJumpStmt(), 0);
 		}
 
 		LOG.debug(getGraph().toString());
 	}
 
 	protected DirectedGraph<Unit> getGraph() {
 		return graph;
 	}
 
 	protected LoopNestTree getLoopNestTree() {
 		return loopNestTree;
 	}
 
 	void run() {
 		doAnalysis();
 	}
 
 	/**
 	 * Returns the first potentially unsafe statement in the method associated
 	 * with this {@link Analysis}, or <code>null</code> if there is no unsafe
 	 * statement.
 	 * 
 	 * @param context
 	 *            may contain additional array size intervals that the analysis
 	 *            itself cannot determine, i.e., because it requires a points-to
 	 *            analysis
 	 * @return the first unsafe statement or <code>null</code> if there is none
 	 */
 	Stmt getFirstUnsafeStatement(IntervalPerVar context) {
 		for (Unit unit : getGraph()) {
 			Stmt stmt = (Stmt) unit;
 			if (stmt instanceof DefinitionStmt) {
 				DefinitionStmt defStmt = (DefinitionStmt) stmt;
 				Value left = defStmt.getLeftOp();
 				Value right = defStmt.getRightOp();
 				IntervalPerVar intervalPerVar = getFlowBefore(unit);
 				if (intervalPerVar.isInDeadCode()) {
 					continue;
 				}
 				IntervalPerVar merged = new IntervalPerVar();
 				merged.mergeFrom(intervalPerVar, context);
 				if (!merged.isSafe(left) || !merged.isSafe(right)) {
 					return stmt;
 				}
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Stops the analysis because it cannot or does not need to handle a certain
 	 * feature.
 	 * 
 	 * @param what
 	 *            a description of the feature not handled
 	 */
 	static void unhandled(String what) {
 		LOG.error("Can't handle " + what);
 		System.exit(1); // TODO: is System.exit(1) a good idea for code that we
 						// want to unit-test?
 	}
 
 	@Override
 	protected void flowThrough(IntervalPerVar current, Unit op,
 			List<IntervalPerVar> fallOut, List<IntervalPerVar> branchOuts) {
 		LOG.debug(op.getClass().getName() + ": " + op);
 
 		Stmt stmt = (Stmt) op;
 		IntervalPerVar fallState = new IntervalPerVar();
 		fallState.copyFrom(current);
 		IntervalPerVar branchState = new IntervalPerVar();
 		branchState.copyFrom(current);
 
 		if (stmt instanceof DefinitionStmt) {
 			DefinitionStmt defStmt = (DefinitionStmt) stmt;
 			Value left = defStmt.getLeftOp();
 			Value right = defStmt.getRightOp();
 			LOG.debug("\tLeft: " + left.getClass().getName());
 			LOG.debug("\tRight: " + right.getClass().getName());
 
 			// Only handle cases where the left value is either a
 			// StaticFieldRef, JimpleLoca, JArrayRef or JInstanceFieldRef
 			if ((!(left instanceof StaticFieldRef))
 					&& (!(left instanceof JimpleLocal))
 					&& (!(left instanceof JArrayRef))
 					&& (!(left instanceof JInstanceFieldRef))) {
 				unhandled("assignment to non-variables is not handled.");
 			} else if ((left instanceof JArrayRef)
 					&& (!((((JArrayRef) left).getBase()) instanceof JimpleLocal))) {
 				unhandled("assignment to a non-local array variable is not handled.");
 			}
 
 			if (left instanceof JimpleLocal) {
 				String varName = ((JimpleLocal) left).getName();
 				Interval newInterval = null;
 
 				if (right instanceof IntConstant
 						|| right instanceof JimpleLocal
 						|| right instanceof LengthExpr) {
 					Interval interval = current.tryGetIntervalForValue(right);
 					fallState.putIntervalForVar(varName, interval);
 				} else if (right instanceof BinopExpr) {
 					Value firstValue = ((BinopExpr) right).getOp1();
 					Value secondValue = ((BinopExpr) right).getOp2();
 
 					Interval first = current.tryGetIntervalForValue(firstValue);
 					Interval second = current
 							.tryGetIntervalForValue(secondValue);
 
 					if (first != null && second != null) {
 						if (right instanceof AddExpr) {
 							newInterval = Interval.plus(first, second);
 						} else if (right instanceof SubExpr) {
 							newInterval = Interval.sub(first, second);
 						} else if (right instanceof MulExpr) {
 							newInterval = Interval.mul(first, second);
 						} else if (right instanceof DivExpr) {
 							newInterval = Interval.div(first, second);
 						} else if (right instanceof RemExpr) {
 							newInterval = Interval.rem(first, second);
 						} else if (right instanceof ShlExpr) {
 							newInterval = Interval.shl(first, second);
 						} else if (right instanceof ShrExpr) {
 							newInterval = Interval.shr(first, second);
 						} else if (right instanceof UshrExpr) {
 							newInterval = Interval.ushr(first, second);
 						} else if (right instanceof AndExpr) {
 							newInterval = Interval.and(first, second);
 						} else if (right instanceof OrExpr) {
 							newInterval = Interval.or(first, second);
 						} else if (right instanceof XorExpr) {
 							newInterval = Interval.xor(first, second);
 						} else {
 							LOG.warn("loss of precision due to unsupported binary expression of type "
 									+ right.getClass().getName());
 							if (first.isBottom() || second.isBottom()) {
 								newInterval = Interval.BOTTOM;
 							} else {
 								newInterval = Interval.TOP;
 							}
 						}
 
 					}
 				} else if (right instanceof NegExpr) {
 					NegExpr negExpr = (NegExpr) right;
 					Value value = negExpr.getOp();
 					newInterval = Interval.neg(current
 							.tryGetIntervalForValue(value));
 				} else if (right instanceof NewArrayExpr) {
 					// This statement allocates a new array
 					// Store the array size interval in the state
 					NewArrayExpr newArrayExpr = (JNewArrayExpr) right;
 					Value size = newArrayExpr.getSize();
 					newInterval = current.tryGetIntervalForValue(size);
 					// Also associate allocation site with size interval
 					// for use from other methods (via pointer-analysis)
 					getAllocationNodeMap().put(newArrayExpr, newInterval);
 				} else if (right instanceof InstanceFieldRef) {
 					InstanceFieldRef instFieldRef = (InstanceFieldRef) right;
 					if (isBooleanOrIntType(instFieldRef.getType())) {
 						newInterval = Interval.TOP;
 					}
 				} else if (right instanceof StaticFieldRef) {
 					StaticFieldRef staticFieldRef = (StaticFieldRef) right;
 					if (isBooleanOrIntType(staticFieldRef.getType())) {
 						newInterval = Interval.TOP;
 					}
 				} else if (right instanceof JArrayRef) {
 					JArrayRef arrayRef = (JArrayRef) right;
 					if (isBooleanOrIntType(arrayRef.getType())) {
 						newInterval = Interval.TOP;
 					}
 				} else if (right instanceof NewExpr) {
 					// Do nothing
 				} else if (right instanceof InvokeExpr) {
 					// When calling a method that returns an integer, assume
 					// that the return value could be anything
 					InvokeExpr invokeExpr = (InvokeExpr) right;
 					SootMethod method = invokeExpr.getMethod();
 					if (isBooleanOrIntType(method.getReturnType())) {
 						newInterval = Interval.TOP;
 					}
 				} else if (right instanceof ParameterRef) {
 					ParameterRef parameterRef = (ParameterRef) right;
 					Type parameterType = parameterRef.getType();
 					if (isBooleanOrIntType(parameterType)) {
 						newInterval = Interval.TOP;
 					} else {
 						// Do nothing
 					}
 				} else if (right instanceof ThisRef) {
 					// nothing to do
 				} else if (right instanceof CaughtExceptionRef) {
 					// nothing to do
 				} else if (right instanceof ClassConstant) {
 					// a reference to a class object, not relevant for us
 				} else {
 					if (isBooleanOrIntType(left.getType())) {
 						LOG.warn("cannot handle right-hand side of assignment of type "
 								+ right.getClass().getName()
 								+ ", assuming TOP to be on the safe side");
 						newInterval = Interval.TOP;
 					}
 				}
 				if (newInterval != null) {
 					Interval oldInterval = fallState.getIntervalForVar(varName);
 					fallState.putIntervalForVar(varName, newInterval);
 					LOG.debug("\t" + varName + ": " + oldInterval + " -> "
 							+ newInterval);
 				}
 			} else if (left instanceof JArrayRef) {
 				// Do nothing
 				// The array access is relevant at the end of the analysis
 			} else if (left instanceof StaticFieldRef
 					|| left instanceof InstanceFieldRef) {
 				// Do nothing
 			} else {
 				// This branch will never be taken due to the earlier check
 				// regarding the type of 'left'
 				unhandled("left-hand side of assignment");
 			}
 		} else if (op instanceof IfStmt) {
 			IfStmt ifStmt = (IfStmt) op;
 			Value condition = ifStmt.getCondition();
 			LOG.debug("\tCondition: " + condition.getClass().getName());
 
 			if (condition instanceof ConditionExpr) {
 				ConditionExpr conditionExpr = (ConditionExpr) condition;
 				ConditionExprEnum conditionExprEnum = ConditionExprEnum
 						.fromConditionExprClass(conditionExpr.getClass());
 
 				Value left = conditionExpr.getOp1();
 				Value right = conditionExpr.getOp2();
 				Interval leftInterval = current.tryGetIntervalForValue(left);
 				Interval rightInterval = current.tryGetIntervalForValue(right);
 
 				LOG.debug("\tLeft Interval: " + leftInterval);
 				LOG.debug("\tRight Interval: " + rightInterval);
 
 				// The constrained interval of the left value when the condition
 				// holds. It is Interval.BOTTOM if the left value cannot
 				// possibly satisfy the condition
 				Interval leftBranchInterval = Interval.cond(leftInterval,
 						rightInterval, conditionExprEnum);
 
 				// The constrained interval of the left value when the condition
 				// does not hold. It is Interval.BOTTOM if the left value
 				// certainly satisfies the condition
 				Interval leftFallInterval = Interval.cond(leftInterval,
 						rightInterval, conditionExprEnum.getNegation());
 
 				// The constrained interval of the right value when the
 				// condition holds. It is Interval.BOTTOM if the right value
 				// cannot possibly satisfy the condition
 				Interval rightBranchInterval = Interval.cond(rightInterval,
 						leftInterval, conditionExprEnum.getSwapped());
 
 				// The constrained interval of the right value when the
 				// condition does not hold. It is Interval.BOTTOM if the right
 				// value certainly satisfies the condition
 				Interval rightFallInterval = Interval.cond(rightInterval,
 						leftInterval, conditionExprEnum.getSwapped()
 								.getNegation());
 
 				if (leftBranchInterval.isBottom()
 						&& rightBranchInterval.isBottom()) {
 					// If no values of the left and right intervals can satisfy
 					// the condition, mark the branch state as dead
 					// TODO: Replace this by a disjunction?
 					branchState.setInDeadCode();
 				} else if (leftFallInterval.isBottom()
 						&& rightFallInterval.isBottom()) {
 					// If no values of the left and right intervals can satisfy
 					// the negation of the condition, mark the fall through
 					// state as dead
 					// TODO: Replace this by disjunction?
 					fallState.setInDeadCode();
 				}
 
 				if (left instanceof JimpleLocal) {
 					JimpleLocal leftLocal = (JimpleLocal) left;
 					String varName = leftLocal.getName();
 
 					LOG.debug("\tLeft Branch Interval: "
 							+ branchState.getIntervalForVar(varName) + " -> "
 							+ leftBranchInterval);
 					LOG.debug("\tLeft Fall Interval: "
 							+ fallState.getIntervalForVar(varName) + " -> "
 							+ leftFallInterval);
 
 					branchState.putIntervalForVar(varName, leftBranchInterval);
 					fallState.putIntervalForVar(varName, leftFallInterval);
 				}
 
 				if (right instanceof JimpleLocal) {
 					JimpleLocal rightLocal = (JimpleLocal) right;
 					String varName = rightLocal.getName();
 
 					LOG.debug("\tRight Branch Interval: "
 							+ branchState.getIntervalForVar(varName) + " -> "
 							+ rightBranchInterval);
 					LOG.debug("\tRight Fall Interval: "
 							+ fallState.getIntervalForVar(varName) + " -> "
 							+ rightFallInterval);
 
 					branchState.putIntervalForVar(varName, rightBranchInterval);
 					fallState.putIntervalForVar(varName, rightFallInterval);
 				}
 			} else {
 				unhandled("condition");
 			}
 		}
 
 		// If the statement under inspection is a back-jump statement of a loop,
 		// apply widening if it has already been handled at least
 		// LOOP_BACK_JUMP_COUNT_THRESHOLD times.
 		boolean isWideningNeeded = false;
 		if (loopBackJumpCountMap.containsKey(stmt)) {
 			int newCount = loopBackJumpCountMap.get(stmt) + 1;
 			loopBackJumpCountMap.put(stmt, newCount);
 			if (newCount > LOOP_BACK_JUMP_COUNT_THRESHOLD) {
 				isWideningNeeded = true;

				// When there are nested loop and the threshold has been
				// reached in the case of the inner loop, the intervals
				// of the inner loop may change when the symbolic execution
				// moves on to the second element of the outer loop.
				// Because the threshold of the inner loop is still reached,
				// another widening is performed, which accidentally affects
				// the outer loop as well, even though the execution has only
				// reached its second element.
				// As a simple measure, reset the counter back to zero.
				loopBackJumpCountMap.put(stmt, 0);
 			}
 		}
 
 		for (IntervalPerVar fnext : fallOut) {
 			if (fallState != null) {
 				if (isWideningNeeded) {
 					fnext.copyAndWidenFrom(fallState);
 				} else {
 					fnext.copyFrom(fallState);
 				}
 			}
 		}
 		for (IntervalPerVar fnext : branchOuts) {
 			if (branchState != null) {
 				if (isWideningNeeded) {
 					fnext.copyAndWidenFrom(branchState);
 				} else {
 					fnext.copyFrom(branchState);
 				}
 			}
 		}
 	}
 
 	@Override
 	protected void copy(IntervalPerVar source, IntervalPerVar target) {
 		target.copyFrom(source);
 	}
 
 	@Override
 	protected IntervalPerVar entryInitialFlow() {
 		// TODO: How do you model the entry point?
 		return new IntervalPerVar();
 	}
 
 	@Override
 	protected void merge(IntervalPerVar firstSource,
 			IntervalPerVar secondSource, IntervalPerVar target) {
 		target.mergeFrom(firstSource, secondSource);
 		LOG.debug("Merge:");
 		LOG.debug("\tSource: " + firstSource);
 		LOG.debug("\tSource: " + secondSource);
 		LOG.debug("\tResult: " + target);
 	}
 
 	@Override
 	protected IntervalPerVar newInitialFlow() {
 		return new IntervalPerVar();
 	}
 
 	public Map<NewArrayExpr, Interval> getAllocationNodeMap() {
 		return allocationNodeMap;
 	}
 
 	protected boolean isBooleanOrIntType(Type type) {
 		// TODO: In the case of booleans, one might also return [0, 1] rather
 		// than Interval.TOP.
 		return type.equals(IntType.v()) || type.equals(BooleanType.v());
 	}
 
 }
