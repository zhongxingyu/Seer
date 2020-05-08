 package org.eclipse.dltk.ti;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.dltk.ti.goals.GoalEvaluator;
 import org.eclipse.dltk.ti.goals.IGoal;
 
 /**
  * Main working class for type inference. Purpose of this class is simple:
  * evaluate goals and manage their dependencies of subgoals. Also this
  * class allows pruning: before evaluating every goal(except root goal) could
  * be pruned by provided prunner. 
  */
 public class GoalEngine {
 	
 //	private final static boolean TRACE_GOALS = Boolean
 //	.valueOf(
 //			Platform
 //					.getDebugOption("org.eclipse.dltk.core/typeInferencing/traceGoals"))
 //	.booleanValue();
 //	
 //	private final static boolean SHOW_STATISTICS = Boolean
 //	.valueOf(
 //			Platform
 //					.getDebugOption("org.eclipse.dltk.core/typeInferencing/showStatistics"))
 //	.booleanValue();
 	
 	private final IGoalEvaluatorFactory evaluatorFactory;
 	
 	private final LinkedList workingQueue = new LinkedList();
 	private final HashMap goalStates = new HashMap ();
 	private final HashMap evaluatorStates = new HashMap();
 		
 	private class EvalutorState {
 		public int subgoalsLeft;
 
 		public EvalutorState(int subgoalsLeft) {
 			this.subgoalsLeft = subgoalsLeft;
 		}		
 	}
 	
 	private class WorkingPair {
 		private IGoal goal;
 		private GoalEvaluator creator;
 		public WorkingPair(IGoal goal, GoalEvaluator parent) {
 			this.goal = goal;
 			this.creator = parent;
 		}
 		
 	}
 	
 	private class GoalEvaluationState {
 		public GoalEvaluator creator;
 		public GoalState state;
 		public Object result;		
 	}
 		
 	public GoalEngine(IGoalEvaluatorFactory evaluatorFactory) {
 		this.evaluatorFactory = evaluatorFactory;
 	}
 	
 	private void storeGoal (IGoal goal, GoalState state, Object result, GoalEvaluator creator) {
 		GoalEvaluationState es = new GoalEvaluationState();
 		es.result = result;
 		es.state = state;
 		es.creator = creator;
 		goalStates.put(goal, es);
 	}
 	
 	private void notifyEvaluator (GoalEvaluator evaluator, IGoal subGoal) {
 		GoalEvaluationState subGoalState = (GoalEvaluationState) goalStates.get(subGoal);
 		Object result = subGoalState.result;
 		GoalState state = subGoalState.state;
 		
 		if (state == GoalState.WAITING)
 			state = GoalState.RECURSIVE;
 		
 		IGoal[] newGoals = evaluator.subGoalDone(subGoal, result, state);										
 		for (int i = 0; i < newGoals.length; i++) {
 			workingQueue.add(new WorkingPair(newGoals[i], evaluator));
 		}
 		EvalutorState ev = (EvalutorState) evaluatorStates.get(evaluator);
 		ev.subgoalsLeft--;
 		ev.subgoalsLeft += newGoals.length;				
 		if (ev.subgoalsLeft == 0) {
 			Object newRes = evaluator.produceResult();
 			GoalEvaluationState st = (GoalEvaluationState) goalStates.get(evaluator.getGoal());
 			Assert.isNotNull(st);
 			st.state = GoalState.DONE;
 			st.result = newRes;
 			if (st.creator != null)
 				notifyEvaluator(st.creator, evaluator.getGoal());			
 		}
 	}
 	
 	public Object evaluateGoal(IGoal rootGoal, IPruner pruner) {
 		reset();
 		if (pruner != null)
 			pruner.init();
 		workingQueue.add(new WorkingPair(rootGoal, null));
 		while (!workingQueue.isEmpty()) {
 			WorkingPair pair = (WorkingPair) workingQueue.getFirst();
 			workingQueue.removeFirst();
 			GoalEvaluationState state = (GoalEvaluationState) goalStates.get(pair.goal);
 			if (state != null && pair.creator != null) {
 				notifyEvaluator(pair.creator, pair.goal);														
 			} else {
 				boolean prune = false;
 				if (pruner != null && pair.creator != null)
 					prune = pruner.prune(pair.goal);
 				if (prune) {
 					storeGoal (pair.goal, GoalState.PRUNED, null, pair.creator);
 					notifyEvaluator(pair.creator, pair.goal);
 				} else {
 					GoalEvaluator evaluator = evaluatorFactory.createEvaluator(pair.goal);
 					Assert.isNotNull(evaluator);
 					IGoal[] newGoals = evaluator.init();
 					if (newGoals.length > 0) {
 						for (int i = 0; i < newGoals.length; i++) {
 							workingQueue.add(new WorkingPair(newGoals[i], evaluator));
 						}
 						evaluatorStates.put(evaluator, new EvalutorState(newGoals.length));
 						storeGoal(pair.goal, GoalState.WAITING, null, pair.creator);
 					} else {
 						Object result = evaluator.produceResult();
 						storeGoal(pair.goal, GoalState.DONE, result, pair.creator);
 						if (pair.creator != null)
 							notifyEvaluator(pair.creator, pair.goal);
 					}
 				}
 			}			
 		}		
 		GoalEvaluationState s = (GoalEvaluationState) goalStates.get(rootGoal);
 //		
 //		if (TRACE_GOALS || SHOW_STATISTICS)
 //			System.out.println("Root goal done: " + rootGoal.toString()
 //					+ ", answer is "
 //					+ String.valueOf(lastObtainedSubgoalResult)
 //					+ (timeLimitCondition ? " [time limit exceeded]" : ""));
 //		if (SHOW_STATISTICS) {
 //			System.out.println("Time spent:             "
 //					+ (System.currentTimeMillis() - endTime + timeLimit)
 //					+ " ms");
 //			System.out
 //					.println("Total goals requested:  " + totalGoalsRequested);
 //			System.out.println("Total goals calculated: "
 //					+ totalGoalsCalculated);
 //			System.out.println("Total cache hits:       " + cacheHits);
 //			System.out.println("Maximal stack size:     " + maxStackSize);
 //			System.out
 //					.println("Cached goal answers:    " + answersCache.size());
 //			System.out.println();
 //		}
 //		
 		Assert.isTrue(s.state == GoalState.DONE);
 		return s.result;
 	}
 
 	private void reset() {
 		workingQueue.clear();
 		goalStates.clear();
 		evaluatorStates.clear();
 	}
 	
 }
