 package edu.wheaton.simulator.behavior;
 
 import net.sourceforge.jeval.EvaluationException;
 import edu.wheaton.simulator.datastructure.Grid;
 import edu.wheaton.simulator.entity.Agent;
 import edu.wheaton.simulator.expression.Expression;
 
 public class MoveUpBehavior extends AbstractBehavior {
 
 	/**
 	 * Main constructor
 	 * 
 	 */
 	@Override
 	public String getName() {
		return "moveUp";
 	}
 
 	@Override
 	public Integer numArgs() {
 		return 1;
 	}
 
 	/**
 	 * Attempts to move the target Agent to the up by the number of squares
 	 * input as the function's argument. If the spot is invalid/already full,
 	 * it does not move the Agent and returns false instead of true as the result.
 	 * arg1: number of spaces it moves to the right
 	 */
 	@Override
 	public String execute(String[] args) throws EvaluationException {
 		Agent target = resolveAgent("this");
 		int oldX = target.getPosX();
 		int oldY = target.getPosY();
 		
 		Integer x = oldX;
 		Integer y = Double.valueOf(args[0]).intValue() + oldY;
 		
 		Grid grid = target.getGrid();
 		if(grid.addAgent(target, x, y)) {
 			grid.removeAgent(oldX, oldY);
 			return Expression.TRUE;
 		}
 		return Expression.FALSE;
 	}
 	
 }
