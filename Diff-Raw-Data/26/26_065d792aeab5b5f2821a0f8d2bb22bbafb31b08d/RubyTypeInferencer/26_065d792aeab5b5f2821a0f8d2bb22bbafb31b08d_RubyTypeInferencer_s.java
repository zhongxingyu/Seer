 package org.eclipse.dltk.ruby.typeinference;
 
 import org.eclipse.dltk.ti.DefaultTypeInferencer;
 import org.eclipse.dltk.ti.EvaluatorStatistics;
 import org.eclipse.dltk.ti.IPruner;
 import org.eclipse.dltk.ti.goals.AbstractTypeGoal;
 import org.eclipse.dltk.ti.goals.IGoal;
 import org.eclipse.dltk.ti.types.IEvaluatedType;
 
 public class RubyTypeInferencer extends DefaultTypeInferencer {
 	
 	private class SimplestRubyPruner implements IPruner {
 		
 		private long timeStart;
 		private final long timeLimit;
 		
 		
 
 		public SimplestRubyPruner(long timeLimit) {
 			super();
 			this.timeLimit = timeLimit;
 		}
 
 		public void init() {
 			this.timeStart = System.currentTimeMillis();			
 		}
 
 		public boolean prune(IGoal goal, EvaluatorStatistics stat) {
 			long currentTime = System.currentTimeMillis();
 			if (stat != null) {
 //				if (stat.getSubGoalsDoneSuccessful() > 5)
 //					return true;
 			}
 //			if (currentTime - timeStart > timeLimit)
 //				return true;
 			return false;
 		}
 		
 	}
 	
 	public RubyTypeInferencer() {
 		super( new RubyEvaluatorFactory());
 	}
 
 	public IEvaluatedType evaluateType(AbstractTypeGoal goal, int timeLimit) {
 		IEvaluatedType type = super.evaluateType(goal, null); //TODO: add pruner
		if (type == null) { 
 			type = new RubyClassType("Object"); //anyway, all things in ruby are objects
 		}
 		return type;
 	}
 	
 
 }
