 package m0rjc.ax25.generator.simulatorBuilder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SimulatedInstructionBlock extends SimulatedAction
 {
 	private List<SimulatedAction> m_actions = new ArrayList<SimulatedAction>();
 	
 	public void addAction(SimulatedAction a)
 	{
 		m_actions.add(a);
 	}
 	
 	/**
 	 * RunFromId must recurse into all children
 	 */
 	@Override
 	public ActionResult runFromId(int id) throws SimulationException
 	{
 		for(SimulatedAction action : m_actions)
 		{
 			ActionResult result = action.runFromId(id);
 			switch(result)
 			{
 			case POP:
				return result;
 			case RETURN_FROM_STATE_ENGINE:
 				return result;
 			}
 		}
 	
 		return ActionResult.CONTINUE_TO_NEXT_ACTION;
 	}
 
 	@Override
 	public ActionResult run() throws SimulationException
 	{
 		return runFromId(-1);
 	}
 }
