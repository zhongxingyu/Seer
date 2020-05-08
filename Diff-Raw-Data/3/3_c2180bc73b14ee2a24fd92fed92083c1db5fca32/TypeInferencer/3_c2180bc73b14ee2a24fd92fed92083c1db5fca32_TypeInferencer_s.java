 package org.eclipse.dltk.ddp;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.core.runtime.Platform;
 import org.eclipse.dltk.evaluation.types.IEvaluatedType;
 import org.eclipse.dltk.evaluation.types.RecursionTypeCall;
 
 public final class TypeInferencer implements ITypeInferencer {
 	
 	private final static boolean TRACE_GOALS = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.core/typeInferencing/traceGoals"))
 			.booleanValue();
 	
 	private final static boolean TRACE_TIME_LIMIT_RECOVERY = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.core/typeInferencing/traceTimeLimit"))
 			.booleanValue();
 	
 	private final static boolean SHOW_STATISTICS = Boolean.valueOf(
 			Platform.getDebugOption("org.eclipse.dltk.core/typeInferencing/showStatistics"))
 			.booleanValue();
 	
 	private final IGoalEvaluatorFactory evaluatorFactory;
 	
 	private final Map answersCache = new HashMap();
 	
 	private int totalGoalsCalculated, totalGoalsRequested, totalRecursiveRequests, cacheHits, maxStackSize;
 	
 	private GoalEvaluator[] stack = new GoalEvaluator[6];
 	
 	private int stackSize = 0;
 
 	public TypeInferencer(IGoalEvaluatorFactory evaluatorFactory) {
 		this.evaluatorFactory = evaluatorFactory;
 	}
 	
 	private void pushToStack(GoalEvaluator goal) {
 		Assert.isTrue(stackSize <= stack.length);
 		if (stack.length == stackSize) {
 			GoalEvaluator[] oldStack = stack;
 			stack = new GoalEvaluator[oldStack.length * 2];
 			System.arraycopy(oldStack, 0, stack, 0, stackSize);
 		}
 		stack[stackSize++] = goal;
 		if (stackSize > maxStackSize)
 			maxStackSize = stackSize;
 	}
 	
 	private boolean isInStack(IGoal goal) {
 		for (int i = stackSize - 1; i >= 0; i--)
 			if (stack[i].getGoal().equals(goal))
 				return true;
 		return false;
 	}
 	
 	private GoalEvaluator popStack() {
 		Assert.isTrue(stackSize > 0);
 		return stack[--stackSize];
 	}
 	
 	private GoalEvaluator peekStack() {
 		Assert.isTrue(stackSize > 0);
 		return stack[stackSize - 1];
 	}
 
 	public IEvaluatedType evaluateGoal(IGoal rootGoal, long timeLimit) {
		rootGoal = evaluatorFactory.translateGoal(rootGoal);
 		if (rootGoal == null)
 			return null;
 		
 		totalGoalsRequested++;
 		
 		if (isInStack(rootGoal)) {
 			totalRecursiveRequests++;
 			return RecursionTypeCall.INSTANCE;
 		}
 		IEvaluatedType lastObtainedSubgoalResult = (IEvaluatedType) answersCache.get(rootGoal);
 		if (lastObtainedSubgoalResult != null) {
 			if (TRACE_GOALS)
 				System.out.println("DDP root goal cache hit: " + rootGoal.toString());
 			cacheHits++;
 			return lastObtainedSubgoalResult;
 		}
 
 		if (TRACE_GOALS)
 			System.out.println("DDP root goal evaluation: " + rootGoal.toString() + 
 					(timeLimit > 0 ? "  time limit: " + timeLimit : ""));
 	
 		long endTime = System.currentTimeMillis() + timeLimit;
 		GoalEvaluator rootEvaluator = evaluatorFactory.createEvaluator(rootGoal);
 		int emptyStackSize = stackSize; // there might already be some elements there
 		pushToStack(rootEvaluator);
 		IGoal lastFullyEvaluatedGoal = null;
 		while(stackSize > emptyStackSize && (timeLimit == 0 || System.currentTimeMillis() < endTime)) {
 			GoalEvaluator goal = peekStack();
 			IGoal subgoal = goal.produceNextSubgoal(lastFullyEvaluatedGoal, lastObtainedSubgoalResult);
 
 			if (subgoal == null) {
 				lastObtainedSubgoalResult = goal.produceType();
 				lastFullyEvaluatedGoal = goal.getGoal();
 				popStack();
 				totalGoalsCalculated++;
 			} else {
 				subgoal = evaluatorFactory.translateGoal(subgoal);
 				totalGoalsRequested++;
 				if (isInStack(subgoal)) {
 					totalRecursiveRequests++;
 					lastFullyEvaluatedGoal = subgoal;
 					lastObtainedSubgoalResult = RecursionTypeCall.INSTANCE;
 				} else {
 					lastObtainedSubgoalResult = (IEvaluatedType) answersCache.get(subgoal);
 					if (lastObtainedSubgoalResult != null) {
 						cacheHits++;
 						lastFullyEvaluatedGoal = subgoal;
 					} else {
 						lastFullyEvaluatedGoal = null;
 						GoalEvaluator evaluator = evaluatorFactory.createEvaluator(subgoal);
 						if (evaluator == null) {
 							if (TRACE_GOALS)
 								System.out.println("DDP no evaluator for goal: " + subgoal);
 							lastFullyEvaluatedGoal = subgoal;
 						} else {
 							pushToStack(evaluator);
 						}
 					}
 				}
 			}
 		}
 		
 		boolean timeLimitCondition = stackSize > emptyStackSize;
 		while (stackSize > emptyStackSize) {
 			GoalEvaluator goal = popStack();
 			// tell the 
 			IGoal subgoal = goal.produceNextSubgoal(lastFullyEvaluatedGoal, lastObtainedSubgoalResult);
 			if (TRACE_TIME_LIMIT_RECOVERY && subgoal != null)
 				System.out.println("DDP goal ignored due to time limit: " + subgoal.toString());
 			lastObtainedSubgoalResult = goal.produceType();
 			lastFullyEvaluatedGoal = goal.getGoal();
 		}
 		
 		if (TRACE_GOALS || SHOW_STATISTICS)
 			System.out.println("DDP root goal done: " + rootGoal.toString() + ", answer is " + 
 					String.valueOf(lastObtainedSubgoalResult) + 
 					(timeLimitCondition ? " [time limit exceeded]" : ""));
 		if (SHOW_STATISTICS) {
 			System.out.println("Time spent:             " + (System.currentTimeMillis() - endTime + timeLimit) + " ms");
 			System.out.println("Total goals requested:  " + totalGoalsRequested);
 			System.out.println("Total goals calculated: " + totalGoalsCalculated);
 			System.out.println("Total cache hits:       " + cacheHits);
 			System.out.println("Maximal stack size:     " + maxStackSize);
 			System.out.println("Cached goal answers:    " + answersCache.size());
 			System.out.println();
 		}
 		
 		return lastObtainedSubgoalResult;
 	}
 	
 }
