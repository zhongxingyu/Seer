 package edu.cwru.sepia.agent;
 
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.prefs.Preferences;
 
 import edu.cwru.sepia.action.Action;
 import edu.cwru.sepia.action.ActionFeedback;
 import edu.cwru.sepia.action.ActionResult;
 import edu.cwru.sepia.action.ActionType;
 import edu.cwru.sepia.agent.action.BaseAction;
 import edu.cwru.sepia.agent.action.CollectAction;
 import edu.cwru.sepia.agent.action.CollectGoldAction;
 import edu.cwru.sepia.agent.action.CollectWoodAction;
 import edu.cwru.sepia.environment.model.history.BirthLog;
 import edu.cwru.sepia.environment.model.history.History.HistoryView;
 import edu.cwru.sepia.environment.model.history.ResourceNodeExhaustionLog;
 import edu.cwru.sepia.environment.model.state.ResourceNode;
 import edu.cwru.sepia.environment.model.state.ResourceNode.ResourceView;
 import edu.cwru.sepia.environment.model.state.ResourceType;
 import edu.cwru.sepia.environment.model.state.State.StateView;
 import edu.cwru.sepia.experiment.Configuration;
 import edu.cwru.sepia.experiment.ConfigurationValues;
 
 /*
  * This agent uses an instance of the SRS class to do online planning for 
  * resource collection.
  */
 
 public class ResourceCollectionAgent extends Agent {
 
 	private static final long serialVersionUID = 1596635469778909387L;
 	
 	private SRS planner;
 	private List<BaseAction> plan = null;
 	
 	// this number controls how often the agent will replan
 	private int replanTime = 1000;
 	
 	private Condition goal;
 	
 	/* Keeps track of actions in progress
 	 * The first key is the list the action came from
 	 * The second key is the total duration so far of that action
 	 */
 	private List<Pair<BaseAction,Integer>> inProgress;
 	
 	// stores the townhall's id
 	private Integer townhall = null;
 	private boolean busyTownhall = false;
 	
 	// stores the peasants that are available for work
 	private List<Integer> freePeasants;
 
 	public ResourceCollectionAgent(int playernum) {
 		super(playernum);
 		
 		// this will be used by the convertId's method
 		inProgress = new ArrayList<Pair<BaseAction, Integer>>();
 		
 		freePeasants = new LinkedList<Integer>();
 	}
 
 	@Override
 	public Map<Integer, Action> initialStep(StateView newstate,
 			HistoryView statehistory) {
 		
 		System.out.println("In initial step");
 		
 		// Extract the Goal conditions from the xml
 		
 		goal = new Condition();
 		//goal.gold=ConfigurationValues.MODEL_REQUIRED_GOLD.getIntValue(configuration);
 		//goal.wood=Preferences.userRoot().node("edu").node("cwru").node("sepia").node("environment").node("model").getInt("RequiredWood", 0);
 
 		goal.gold = 50000;
 		goal.wood = 50000;
 		// find out the townhall and add all of the peasants to the free list
 		List<Integer> unitIds = newstate.getUnitIds(this.playernum);
 		for(Integer id : unitIds)
 		{
 			String unitName = newstate.getUnit(id).getTemplateView().getName();
 			if(unitName.equals("TownHall"))
 			{
 				townhall = id;
 			}
 			else if(unitName.equals("Peasant"))
 			{
 				freePeasants.add(id);
 			}
 		}
 		
 		// add the resources to the Collect Actions
 		int[] spaces = new int[2];
 		spaces[0] = newstate.getUnit(townhall).getXPosition();
 		spaces[1] = newstate.getUnit(townhall).getYPosition();
 		CollectGoldAction goldAction = new CollectGoldAction();
 		CollectWoodAction woodAction = new CollectWoodAction();
 		List<Integer> resourceIds = newstate.getAllResourceIds();
 		for(Integer id : resourceIds)
 		{
 			ResourceView resource = newstate.getResourceNode(id);
 			if(resource.getType() == ResourceNode.Type.GOLD_MINE)
 			{
 				goldAction.addResource(id, Math.max(Math.abs(resource.getXPosition()-spaces[0]), Math.abs(resource.getYPosition()-spaces[1])));
 			}
 			else
 			{
 				woodAction.addResource(id, Math.max(Math.abs(resource.getXPosition()-spaces[0]), Math.abs(resource.getYPosition()-spaces[1])));
 			}
 		}
 
 		// get initial plan
 		plan = SRS.getPlan(newstate, goal, this.playernum);
 		System.out.println("# of actions in plan: " + plan.size());
 		
 		// increment the number of steps;
 		Map<Integer, Action> executableActions = convertPlan2Actions(newstate, new HashMap<Integer, Action>());
 		System.out.println("executableActions: " + executableActions);
 		return executableActions;
 	}
 
 	@Override
 	public Map<Integer, Action> middleStep(StateView newstate,
 			HistoryView statehistory) {
 		CollectGoldAction.clearTempGather();
 		CollectWoodAction.clearTempGather();
 		
 		System.out.println("In middleStep");
 		System.out.println("# of actions in plan: " + plan.size());
 		System.out.println("\n");
 		if(newstate.getTurnNumber() % replanTime == 0)
 		{
 			plan = SRS.getPlan(newstate, goal, this.playernum);
 		}
 
 		Map<Integer, ActionResult> commandLog = statehistory.getCommandFeedback(playernum, newstate.getTurnNumber()-1);
 		System.out.println("commandLog: " + commandLog);
 
 		// remove nodes that no longer have any resources
 		List<ResourceNodeExhaustionLog> depletedNodes = statehistory.getResourceNodeExhaustionLogs(newstate.getTurnNumber()-1);
 		updateResources(depletedNodes);
 		
 		// update the duration counts
 		// also if any compoundgather moves have completed add
 		// appropriate compound deposit actions
 		Map<Integer, Action> executableActions = updateDurations(commandLog);
 		
 		// see if any new peasants were created
 		// if they were then add their id's to the free peasant list
 		List<BirthLog> births = statehistory.getBirthLogs(newstate.getTurnNumber()-1);
 		if(!births.isEmpty())
 			System.out.println("births: " + births);
 		System.out.println("");
 		System.out.println("");
		addNewPeasants(births, newstate);
 		
 		executableActions = convertPlan2Actions(newstate, executableActions);
 		
 		//System.out.println("executableActions: " + executableActions);
 		return executableActions;
 	}
 
 	@Override
 	public void terminalStep(StateView newstate, HistoryView statehistory) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void savePlayerData(OutputStream os) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void loadPlayerData(InputStream is) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	/*
 	 * This method takes the plan from the SRS and converts it to
 	 * SEPIA's actions
 	 */
 	private Map<Integer, Action> convertPlan2Actions(StateView state, Map<Integer, Action> actionMap)
 	{
 		System.out.println("In convertPlan2Actions");
 
 		while(!plan.isEmpty())
 		{
 			System.out.println("Action " + plan.get(0).getClass().getName());
 			//System.out.println("\tPreconditions Met: " + preSat(plan.get(0), state));
 			if(preSat(plan.get(0), state))
 			{
 				System.out.println("\tPreconditions Met: True");
 				System.out.println("\tUnit Type: " + plan.get(0).getUnitType());
 				if(plan.get(0).getUnitType().equals("Peasant"))
 				{
 					// grab the id of the peasant this will be assigned to
 					Integer id = getAvailPeasant();
 					// remove this action from the plan
 					BaseAction action = plan.remove(0);
 					// set the unitID in the action
 					action.setUnitId(id);
 					// add the action to the actions to be executed this turn
 					actionMap.put(id, action.getAction(playernum, state));
 					// add the action to the list of actions in progress
 					inProgress.add(new Pair<BaseAction, Integer>(action, 0));
 					System.out.println("actionMap: " + actionMap);
 				}
 				else
 				{
 					BaseAction action = plan.remove(0);
 					action.setUnitId(townhall);
 					actionMap.put(townhall, action.getAction(playernum, state));
 					inProgress.add(new Pair<BaseAction, Integer>(action, 0));
 					busyTownhall = true;
 					System.out.println("actionMap: " + actionMap);
 				}
 			}
 			else // as soon as a single bad action is reached then quit trying to make a plan
 			{
 				break;
 			}
 		}
 		return actionMap;
 	}
 	
 	private boolean preSat(BaseAction action, StateView state)
 	{
 		Condition pre = action.getPreConditions();
 		boolean result = true;
 		result = result && (state.getResourceAmount(playernum, ResourceType.GOLD) >= pre.gold); // is there enough gold?
 		result = result && (state.getResourceAmount(playernum, ResourceType.WOOD) >= pre.wood); // is there enough wood?
 		result = result && (freePeasants.size() >= pre.peasant); // are there enough peasants?
 		// might have to subtract the number of peasants currently created
 		int sup = state.getSupplyCap(playernum);
 		result = result && (state.getSupplyCap(playernum) >= pre.supply); // are there enough farms?
 		if(pre.townhall == 1)
 			result = result && !busyTownhall;
 		return result;
 	}
 	
 	private Integer getAvailPeasant()
 	{
 		if(!freePeasants.isEmpty())
 			return freePeasants.remove(0);
 		return null;
 	}
 	
 	/*
 	 * This method will either update the duration counter for actions in progress
 	 * It will also remove an action from inProgress and free up its resources if
 	 * the action is incomplete
 	 */
 	private Map<Integer, Action> updateDurations(Map<Integer, ActionResult> log)
 	{
 		Map<Integer, Action> actionMap = new HashMap<Integer, Action>();
 		int index = -1;
 		List<Pair<BaseAction, Integer>> newInProgress = new ArrayList<Pair<BaseAction, Integer>>(); 
 		for(Pair<BaseAction, Integer> pairing : inProgress)
 		{
 			index++;
 			int unitid = pairing.first.getUnitId();
 			ActionFeedback result = log.get(unitid).getFeedback();
 			// if still in progress
 			if(result.compareTo(ActionFeedback.INCOMPLETE) == 0)
 			{
 				pairing.second++;
 				newInProgress.add(pairing);
 			}
 			// finished last turn
 			else if(result.compareTo(ActionFeedback.COMPLETED) == 0)
 			{
 				// update the duration for that resourceID
 				pairing.second++;
 				// don't want to remove collection actions because they need to be continued
 				// with a COMPOUND_DEPOSIT move
 				if(log.get(unitid).getAction().getType().compareTo(ActionType.COMPOUNDGATHER) == 0)
 				{
 					actionMap.put(unitid, Action.createCompoundDeposit(unitid, townhall));
 					newInProgress.add(pairing);
 					continue;
 				}
 				try {
 					pairing.first.updateDuration(pairing.second);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 				
 				// remove the object from the inProgress list
 				Pair<BaseAction, Integer> completed = inProgress.get(index);
 				if(unitid != townhall)
 					freePeasants.add(unitid);
 				else
 					busyTownhall = false;
 			}
 			// error or something else
 			else
 			{
 				// retry the action
 				// should probably make this replan
 				// deposits must suceed or a peasant becomes useless
 				// because the peasant will be unable to pickup any more resources without first depositing
 				if(log.get(unitid).getAction().getType().compareTo(ActionType.COMPOUNDDEPOSIT) == 0)
 				{
 					actionMap.put(unitid, log.get(unitid).getAction());
 					newInProgress.add(pairing);
 				}
 				// any other action can simply be dropped and will be readded with the new plan
 				else
 				{
 					Pair<BaseAction, Integer> completed = inProgress.get(index);
 					if(unitid != townhall)
 						freePeasants.add(unitid);
 					else
 						busyTownhall = false;
 				}
 			}
 		}
 		inProgress = newInProgress;
 		return actionMap;
 	}
 	
	private void addNewPeasants(List<BirthLog> births, StateView state)
 	{	
 		for(BirthLog log : births)
 		{
			String unitName = state.getUnit(log.getNewUnitID()).getTemplateView().getName();
			if(unitName.equals("Peasant"))
				freePeasants.add(log.getNewUnitID());
 		}
 	}
 	
 	/*
 	 * This method removes depleted trees and gold mines from the duration
 	 * tracking map inside of the wood and gold action classes
 	 */
 	private void updateResources(List<ResourceNodeExhaustionLog> log)
 	{
 		CollectAction wood = new CollectWoodAction();
 		CollectAction gold = new CollectGoldAction();
 		
 		for(ResourceNodeExhaustionLog nodeLog : log)
 		{
 			if(nodeLog.getResourceNodeType() == ResourceNode.Type.GOLD_MINE)
 			{
 				gold.deleteResource(nodeLog.getExhaustedNodeID());
 			}
 			else
 			{
 				wood.deleteResource(nodeLog.getExhaustedNodeID());
 			}
 		}
 	}
 }
