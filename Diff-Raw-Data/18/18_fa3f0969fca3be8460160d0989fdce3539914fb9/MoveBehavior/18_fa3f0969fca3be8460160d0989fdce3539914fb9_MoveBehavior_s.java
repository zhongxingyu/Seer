 /**
  * MoveBehavior.java
  * 
  * @author Agent Team
  */
 
 package edu.wheaton.simulator.behavior;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.expression.Expression;
 
 public class MoveBehavior extends AbstractBehavior {
 
 	/**
 	 * Main constructor
 	 * 
 	 */
 	@Override
 	public String getName() {
 		return "move";
 	}
 
 	@Override
 	public Integer numArgs() {
 		return 2;
 	}
 
 	/**
 	 * Attempts to move the target Agent to the (x, y) position found by
 	 * evaluating the expressions. If the slot it attempts to move to is
 	 * already full, it returns false instead of true.
 	 * arg1: x position
 	 * arg2: y position
 	 */
 	@Override
 	public String execute(String[] args) throws EvaluationException {
 		Agent target = resolveAgent("this");
 		int oldX = target.getPosX();
 		int oldY = target.getPosY();
		
 		Integer x = Double.valueOf(args[0]).intValue();
 		Integer y = Double.valueOf(args[1]).intValue();
 		
 		Grid grid = target.getGrid();
 		if(grid.addAgent(target, x, y)) {
 			grid.removeAgent(oldX, oldY);
 			return Expression.TRUE;
 		}
 		return Expression.FALSE;
 	}
 
 }
