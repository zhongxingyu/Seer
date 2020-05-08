 package edu.wheaton.simulator.expression;
 
import edu.wheaton.simulator.entity.Agent;
import edu.wheaton.simulator.simulation.Grid;
 import net.sourceforge.jeval.EvaluationException;
 
 public class GetFieldOfAgentAt extends AbstractExpressionFunction {
 
 	@Override
 	public String getName() {
 		return "getFieldOfAgentAt";
 	}
 
 	@Override
 	public int getResultType() {
 		return AbstractExpressionFunction.RESULT_TYPE_NUMERIC;
 	}
 
 	@Override
 	public String execute(String[] args) throws EvaluationException {
 		Double x = Double.valueOf(args[0]);
 		Double y = Double.valueOf(args[1]);
		
		Agent xThis = resolveAgent("this");
		Grid grid = xThis.getGrid();
		
		if(grid.isValidCoord(x.intValue(), y.intValue())==false){
			System.err.println("invalid coord sent as param to 'getFieldOfAgentAt(...)': [" + x + "," + y + "]");
		}
		
		Agent target = grid.getAgent(x.intValue(), y.intValue());
		
		if(target==null){
			System.err.println("grid.getAgent(x,y) returned null therefore no field can be retrieved");
		}
		
 		String fieldName = args[2].replaceAll("'", "");
		
		
		Object fieldValue = target.getFieldValue(fieldName);
 		return fieldValue.toString();
 		}
 }
