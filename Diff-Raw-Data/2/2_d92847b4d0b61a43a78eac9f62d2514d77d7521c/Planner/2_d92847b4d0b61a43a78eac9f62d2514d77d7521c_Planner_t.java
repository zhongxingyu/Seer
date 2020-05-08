 package at.ac.tuwien.lsdc.mape;
 
 import at.ac.tuwien.lsdc.actions.Action;
 
 public abstract class Planner {
 
	public abstract Action selectAction(Problem problem);
 }
