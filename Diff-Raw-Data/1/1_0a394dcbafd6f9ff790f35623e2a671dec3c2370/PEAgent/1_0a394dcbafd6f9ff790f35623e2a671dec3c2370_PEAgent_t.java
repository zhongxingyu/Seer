 package edu.cwru.SimpleRTS.agent;
 
 import java.util.*;
 
 import edu.cwru.SimpleRTS.action.*;
 import edu.cwru.SimpleRTS.environment.State.StateView;
 import edu.cwru.SimpleRTS.model.resource.ResourceNode.Type;
 import edu.cwru.SimpleRTS.model.resource.ResourceType;
 import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
 
 public class PEAgent extends Agent {
 
 	private static final long serialVersionUID = 1L;
 	static int playernum = 0;
 	static String townHall = "TownHall";
 	static String peasant = "Peasant";
 	static String move = "move";
 	static String gather = "gather";
 	static String deposit = "deposit";
 	private int finalGoldTally = 200;
 	private int finalWoodTally = 200;
 	private boolean canBuildPeasant = false;
 	private ArrayList<STRIP> actionsList = new ArrayList<STRIP>();
 	private int peasantID;
 	private String fileName = "pln.txt";
 	public STRIP currentAction = null;
 	
 	public PEAgent(int playernum, String[] args) 
 	{
 		super(playernum);
 		finalGoldTally = Integer.parseInt(args[0]);
 		finalWoodTally = Integer.parseInt(args[1]);
 		if(args[2].equals("true")) //check to see if we can build peasants
 		{
 			canBuildPeasant = true;
 		}
 		fileName = args[3];
 	}
 
 	@Override
 	public Map<Integer, Action> initialStep(StateView state) 
 	{
 		List<Integer> allUnitIds = state.getAllUnitIds();
 		List<Integer> peasantIds = findUnitType(allUnitIds, state, peasant);
 		List<Integer> townHallIds = findUnitType(allUnitIds, state, townHall);
 		
 		//create Planner
 		Planner planner = new Planner(state, finalGoldTally, finalWoodTally, canBuildPeasant, fileName);
 		if	(townHallIds.size() > 0) //TownHall Exists. Check if resources available in here too?
 		{
 			actionsList = planner.generatePlan(peasantIds.get(0), townHallIds.get(0), state);
 			peasantID = actionsList.get(0).unit.getID();
 		}	
 		else
 		{
 			System.out.println("TownHall.size() <= 0. Where is it?!!");
 		}
 		return middleStep(state);
 	}
 
 	@Override
 	public Map<Integer, Action> middleStep(StateView state) 
 	{
 		Map<Integer, Action> actions = new HashMap<Integer, Action>();
 
 		List<Integer> allUnitIds = state.getAllUnitIds();
 		List<Integer> peasantIds = findUnitType(allUnitIds, state, peasant);
 		actions = convertToMap(actionsList, peasantID, state);
 		System.out.println(actions);
 		if(actions == null)
 		{
 			actions = new HashMap<Integer, Action>();
 		}
 		
 		System.out.println("GOLD: " + state.getResourceAmount(playernum, ResourceType.GOLD) + " WOOD: " + state.getResourceAmount(playernum, ResourceType.WOOD));
 		System.out.println("PEASANT HAS " + state.getUnit(peasantIds.get(0)).getCargoAmount() + " gold");
 		
 		return actions;
 	}
 
 	@Override
 	public void terminalStep(StateView state) {
 
 	}
 	
 	public Map<Integer, Action> convertToMap(ArrayList<STRIP> actionsIn, int pID, StateView state)
 	{
 		Map<Integer, Action> actionsOut = new HashMap<Integer, Action>();
 		
 		STRIP tempAction = actionsIn.get(0);
 		
 		if (currentAction == null) //grab the first move
 		{
 			System.out.println(tempAction.unit.getTemplateView().getUnitName());
 			actionsIn.remove(0);
 			currentAction = actionsIn.get(0);
 		}
 		
 		if(currentAction.unit.getTemplateView().getUnitName().equals(move))
 		{
 			actionsIn.remove(0);
 			currentAction = actionsIn.get(0); //grab the next move
 		}
 		
 		if(currentAction.unit.getTemplateView().getUnitName().equals(deposit))
 		{
 			if (state.getUnit(pID).getCargoType() != null)
 				actionsOut.put(pID, Action.createCompoundDeposit(pID, state.unitAt(currentAction.unit.getXPosition(), currentAction.unit.getYPosition())));
 			else
 			{
 				actionsIn.remove(0);
 				currentAction = actionsIn.get(0);
 			}
 		}
 		else if(currentAction.unit.getTemplateView().getUnitName().equals(gather))
 		{
 			if (state.getUnit(pID).getCargoType() == null)
 				actionsOut.put(pID, Action.createCompoundGather(pID, state.resourceAt(currentAction.unit.getXPosition(), currentAction.unit.getYPosition())));
 			else
 			{
 				actionsIn.remove(0);
 				currentAction = actionsIn.get(0);
 			}
 		}
 		return actionsOut;
 	}
 	
 	public List<Integer> findUnitType(List<Integer> ids, StateView state, String name)
 	{
 		List<Integer> unitIds = new ArrayList<Integer>();
 		for (int x = 0; x < ids.size(); x++)
 		{
 			Integer unitId = ids.get(x);
 			UnitView unit = state.getUnit(unitId);
 			
 			if(unit.getTemplateView().getUnitName().equals(name))
 			{
 				unitIds.add(unitId);
 			}
 		}
 		return unitIds;
 	}
 }
