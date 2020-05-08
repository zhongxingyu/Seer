 package edu.cwru.SimpleRTS.agent;
 
 import java.io.*;
 import java.util.*;
 
 import edu.cwru.SimpleRTS.action.*;
 import edu.cwru.SimpleRTS.environment.State.StateView;
 import edu.cwru.SimpleRTS.model.Direction;
 import edu.cwru.SimpleRTS.model.resource.ResourceNode.Type;
 import edu.cwru.SimpleRTS.model.resource.ResourceType;
 import edu.cwru.SimpleRTS.model.unit.Unit;
 import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
 import edu.cwru.SimpleRTS.model.unit.UnitTemplate;
 import edu.cwru.SimpleRTS.util.DistanceMetrics;
 
 
 public class Planner {
 
 	private static final long serialVersionUID = 1L;
 	static int playernum = 0;
 	static String townHall = "TownHall";
 	static String peasant = "Peasant";
 	static String farm = "Farm";
 	static String barracks = "Barracks";
 	static String footman = "Footman";
 	static String lumber = "lumber";
 	static String gold = "gold";
 	private int finalGoldTally = 200;
 	private int finalWoodTally = 200;
 	private boolean canBuildPeasant = false;
 	private String planFileName = "plan.txt";
 	private ArrayList<String> plan;
 	
 	public Planner(StateView startState, int finalGoldAmount, int finalWoodAmount, boolean canBuildP)
 	{
 		finalGoldTally = finalGoldAmount;
 		finalWoodTally = finalWoodAmount;
 		canBuildPeasant = canBuildP;
 		
 		Map<Integer, Action> actions = new HashMap<Integer, Action>();
 		List<Integer> allUnitIds = startState.getAllUnitIds();
 		List<Integer> peasantIds = findUnitType(allUnitIds, startState, peasant);
 		List<Integer> townHallIds = findUnitType(allUnitIds, startState, townHall);
 		
 		ArrayList<ResourceInfo> gold = new ArrayList<ResourceInfo>();
 		ArrayList<ResourceInfo> lumber = new ArrayList<ResourceInfo>();
 		
 		addResources(Type.GOLD_MINE, gold, startState);
 		addResources(Type.TREE, gold, startState);
 		
 	}
 	
 	
 	public void addResources(Type resourceType, ArrayList<ResourceInfo> resources, StateView state)
 	{
 		List<Integer> resourceIds = state.getResourceNodeIds(resourceType);
 		
 		for (Integer resourceId: resourceIds)
 		{
 			ResourceInfo resource = new ResourceInfo();
 			
 			resource.x = state.getResourceNode(resourceId).getXPosition();
 			resource.y = state.getResourceNode(resourceId).getYPosition();
 			
 			resources.add(resource);
 		}
 		
 		//JEFF ADD totalAvailable TO EACH RESOURCE SHOULD BE BASED ON DISTANCE FROM TOWNHALL
 		/*
 		 * 
 		 * ADDD ME
 		 * 
 		 */
 	}
 	
 	//originally A* search
 	public Map<Integer, Action> generatePlan(Integer startId, Integer goalId, StateView state)	{
 		
 		Map<Integer, Action> actions = new HashMap<Integer, Action>();
 		STRIP startSpace = new STRIP(); 
 		startSpace.unit = state.getUnit(startId); //starting space
 		
		UnitView goalSpace = state.getUnit(goalId); //end space //NEEDS TO JUST BE GOAL OF TALLY
 		
 		ArrayList<STRIP> openList = new ArrayList<STRIP>(); //the open list, will hold items to be searched
 		ArrayList<STRIP> closedList = new ArrayList<STRIP>(); //spaces all ready searched
 		
 		HashMap<STRIP, STRIP> parentNodes = new HashMap<STRIP, STRIP>(); //Parent node, i.e. the node you came from hashed by the UnitView
 		HashMap<STRIP, Integer> gCost = new HashMap<STRIP, Integer>(); //gCost hashed by the UnitView
 		HashMap<STRIP, Integer> fCost = new HashMap<STRIP, Integer>(); //fCost hashed by the UnitView
 		HashMap<STRIP, Integer> hCost = new HashMap<STRIP, Integer>(); //hCost hashed by the UnitView
 		
 		Integer tempHCost = heuristicCostCalculator(startSpace, goalSpace); //get the costs of the starting node
 		Integer tempGCost = 0; //see above
 		Integer tempFCost = tempHCost + tempGCost; //see above
 		
 		openList.add(startSpace); //start out with the first space
 		hCost.put(startSpace, tempHCost); //add the hCost to the HashMap
 		gCost.put(startSpace, tempGCost); //add the gCost to the HashMap
 		fCost.put(startSpace, tempFCost); //add the fCost to the HashMap
 		
 		System.out.println("Start space: " + startSpace.getXPosition()  + ", " + startSpace.getYPosition());
 		System.out.println("Goal space: " + goalSpace.getXPosition()  + ", " + goalSpace.getYPosition());
 		
 		while (openList.size() > 0) //loop till we exhaust the openList
 		{
 			UnitView currentParent = getLowestCostF(openList, fCost); //finds the UnitView with the lowest fCost
 
 			System.out.println("Searching.. " + currentParent.getXPosition() + ", " + currentParent.getYPosition() + " There are " + openList.size() + " items on the OL");
 			if (checkGoal(currentParent, goalSpace, state)) //success
 			{
 				System.out.println("Woot, found the goal");
 				
 				if(currentParent.equals(startSpace)) //The starting space is the final space, attack the townHall
 				{
 					Action attack = Action.createPrimitiveAttack(startSpace.getID(), goalSpace.getID());
 					actions.put(startSpace.getID(), attack);
 				}
 				else //not quite there
 				{
 					actions = rebuildPath(parentNodes, currentParent, startSpace); 
 				}
 				return actions; 
 			}
 			else //keep on searching
 			{
 				openList.remove(currentParent); //remove the object from the openList and add it to the closed list
 				closedList.add(currentParent);
 				
 				ArrayList<UnitView> neighbors = getNeighbors(currentParent, state, false); //We need to implement neighbor checking and only return valid neighbor types.. i.e. movable squares
 				System.out.println("Found " + neighbors.size() + " neighbors");
 				for (UnitView neighbor : neighbors) //loop for all neighbors
 				{
 					System.out.println("Searching neighbor at : " + neighbor.getXPosition() + ", " + neighbor.getYPosition());
 					
 					if (checkXYList(closedList, neighbor) == (null)) //only go if the neighbor isn't all ready checked
 					{
 						tempGCost = gCostCalculator(neighbor, currentParent, gCost); //grab it's gCost
 						
 						boolean better = true; //used to check if we found a better gCost in the case of the node all ready being in the openList
 						UnitView tempNeighbor = neighbor; //temp used in case the neighbor isn't in the openList yet
 						
 						neighbor = checkXYList(openList, neighbor); //check if the neighbor is in the openList
 						
 						if (neighbor == (null)) //If the openList doesn't contain this neighbor
 						{
 							neighbor = tempNeighbor;
 							tempHCost = heuristicCostCalculator(neighbor, goalSpace); //get the costs of the starting node
 							hCost.put(neighbor, tempHCost); 
 							openList.add(neighbor); //add it to the openList
 						}
 						else if (tempGCost >= gCost.get(neighbor)) //See if we found a better gCost.. if so we're awesome
 						{
 							better = false;
 						}
 						
 						if (better)
 						{
 							gCost.put(neighbor, tempGCost); //add the gCost to our hash
 							parentNodes.put(neighbor, currentParent); //add the parent reference
 
 							tempFCost = hCost.get(neighbor) + tempGCost; //calculate our fCost
 							
 							fCost.put(neighbor, tempFCost); //add the value to our hash
 						} 
 					}					
 				}
 			}
 		}		
 		System.out.println("No path from search space to goal...");
 		return null; //returns null if we don't find anything
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
 	
 	/*
 	 * REWORK: NEEDS TO CHECK GOAL BASED ON IF NEIGHBOR = DEPOSIT && RESOURCE TALLY = GOAL TALLY THEN WIN
 	 * 
 	 * 
 	 * 
 	 */	
 	public boolean checkGoal(UnitView neighbor, UnitView goal, StateView state) //checks if we have reached the goal based on if we neighbor the goalSpace
 	{
 		
 		ArrayList<UnitView> units = getNeighbors(neighbor, state, true);
 		
 		Integer x = goal.getXPosition();
 		Integer y = goal.getYPosition();
 		
 		for (UnitView unit : units) //for all neighbors
 		{
 			Integer unitX = unit.getXPosition();
 			Integer unitY = unit.getYPosition();
 			
 			if (x == unitX && y == unitY) //if it's the same as the goal x, y
 			{
 				return true; //we found it!
 			}
 		}
 		
 		return false;
 	}
 	
 	
 	/*
 	 * NEEDS TO USE gCOST of DEPTH NO LONGER DISTANCE
 	 * EDITED: cjg28
 	 * SHOULD NOW JUST add one to indicate it's depth
 	 * 
 	 */
 	
 	//this calculates the distance between neighbor and currentParent + the g_score of currentParent
 	public Integer gCostCalculator(UnitView neighbor, UnitView currentParent, HashMap<UnitView, Integer> gCost)
 	{
 		Integer cost = gCost.get(currentParent); //currentParent's gCost
 		
 		cost += 1; //heuristicCostCalculator(currentParent, neighbor); //just uses chubeycasdyasi for(neighor, parent) + parent's cost
 		
 		return cost;
 		
 	}
 	
 	
 	/*
 	 * I BELIEVE THIS WILL BE USELESS
 	 * 
 	 * 
 	 */
 	public UnitView checkXYList(ArrayList<UnitView> list, UnitView unit) //Used for checking based on whether or not we all ready have the space of values: x, y
 	{
 		Integer x = unit.getXPosition();
 		Integer y = unit.getYPosition();
 		
 		for (UnitView item : list) //for every item in the list
 		{
 			if (item.getXPosition() == (x) && item.getYPosition() == (y)) //if it's there
 				return item; //return it
 		}
 		return null; //otherwise return nothing
 	}
 	
 	//Goes through oList and checks against Hashmap fCost to find the UnitView with the lowest fCost
 	public UnitView getLowestCostF(ArrayList<UnitView> oList, HashMap<UnitView, Integer> fCost)
 	{
 		UnitView lowestCostF = oList.get(0); // set the first node as the lowest case
 		
 		for(int i = 0; i < oList.size(); i++) // for every item within the list
 		{
 			if(fCost.get(oList.get(i)) < fCost.get(lowestCostF)) //if the new node is lower than the previous
 			{
 				lowestCostF = oList.get(i); //set it
 			}
 		}
 		
 		return lowestCostF; //return our lowest cost
 	}
 	
 	
 	/*
 	 * 
 	 * 
 	 * MOVE TO peAGENT
 	 */
 	//returns the path from start to goal
 	public Map<Integer, Action> rebuildPath(HashMap<UnitView, UnitView> parentNodes, UnitView goalParent, UnitView startParent)
 	{
 		ArrayList<UnitView> backwardsPath = new ArrayList<UnitView>(); //The path backwards
 		Map<Integer, Action> path = new HashMap<Integer, Action>(); //The return set of actions
 		backwardsPath.add(goalParent); //add the goal as our first action
 		
 		UnitView parentNode = parentNodes.get(goalParent);
 		backwardsPath.add(parentNode);
 		
 		while (!parentNode.equals(startParent)) //run till we find the starting node
 		{
 			parentNode = parentNodes.get(parentNode);
 			backwardsPath.add(parentNode);
 		}
 		
 		for(int i = (backwardsPath.size()-1); i > 0; i--)
 		{
 			int xDiff = backwardsPath.get(i).getXPosition() - backwardsPath.get(i-1).getXPosition();
 			int yDiff = backwardsPath.get(i).getYPosition() - backwardsPath.get(i-1).getYPosition();
 			
 			Direction d = Direction.EAST; //default value
 			
 			if(xDiff < 0 && yDiff > 0) //NW
 				d = Direction.NORTHEAST;
 			else if(xDiff == 0 && yDiff > 0) //N
 				d = Direction.NORTH;
 			else if(xDiff > 0 && yDiff > 0) //NE
 				d = Direction.NORTHWEST;
 			else if(xDiff < 0 && yDiff == 0) //E
 				d = Direction.EAST;
 			else if(xDiff < 0 && yDiff < 0) //SE
 				d = Direction.SOUTHEAST;
 			else if(xDiff == 0 && yDiff < 0) //S
 				d = Direction.SOUTH;
 			else if(xDiff > 0 && yDiff < 0) //SW
 				d = Direction.SOUTHWEST;
 			else if(xDiff > 0 && yDiff == 0) //W
 				d = Direction.WEST;
 			if (i == backwardsPath.size()-1) //only put on the first action
 			{
 				path.put(backwardsPath.get(i).getID(), Action.createPrimitiveMove(backwardsPath.get(i).getID(), d));
 			}
 			System.out.println("Path action: " + backwardsPath.get(i).getXPosition() + ", " + backwardsPath.get(i).getYPosition() + " Direction: " + d.toString());
 		}
 		
 		return path;
 		
 	}
 	
 	/*
 	 * 
 	 * 
 	 * NEEDS TO TAKE IN STATE AND CREATE OPEN NODE FOR RESOURCE... IF RESOURCE THEN NEEDS TO SET NAME OF SPACE TO RESOURCE
 	 * FIXED: cjg28 10:01AM
 	 * NEEDS TESTED
 	 */
 	public UnitView createOpenSpace(Integer x, Integer y, StateView state) //creates a dummy UnitView at the requested space
 	{
 		UnitTemplate template = new UnitTemplate(0); //The template, ID 0 is used because we don't care what type it is
 		
 		if (state.isResourceAt(x, y))
 		{
 			String resourceName = getResourceType(x, y, state);
 			
 			if (resourceName != null)
 			{
 				template.setUnitName(resourceName);
 			}
 		}
 		
 
 		Unit unit = new Unit(template, y);	//The actual Unit
 		
 		unit.setxPosition(x); //set its x
 		unit.setyPosition(y); //set its y
 		
 		UnitView openSpace = new UnitView(unit); //make a UnitView from it
 		
 		return openSpace; //return the UnitView
 	}
 	
 	/*
 	 * Returns type of resource.. if no resource there returns null
 	 * 
 	 */
 	public String getResourceType(Integer x, Integer y, StateView state)
 	{
 		Type goldType = Type.GOLD_MINE;
 		Type lumberType = Type.TREE;
 		
 		List<Integer> goldNodes = state.getResourceNodeIds(goldType);
 		List<Integer> lumberNodes = state.getResourceNodeIds(lumberType);
 		
 		for (Integer goldNode: goldNodes)
 		{
 			if (state.getResourceNode(goldNode).getXPosition() == x && state.getResourceNode(goldNode).getYPosition() == y)
 			{
 				return gold;
 			}
 		}
 		for (Integer lumberNode: lumberNodes)
 		{
 			if (state.getResourceNode(lumberNode).getXPosition() == x && state.getResourceNode(lumberNode).getYPosition() == y)
 			{
 				return lumber;
 			}
 		}
 		
 		return null;
 	}
 	
 	/*
 	 * NEEDS TO PASS IN STATEVIEW TO SPACE MAKER FOR RESOURCE POSSIBILITY
 	 * 
 	 * 
 	 */
 	public ArrayList<UnitView> getNeighbors(UnitView currentParent, StateView state, boolean unitDoesntMatter) //returns neighbors 
 	{
 		//NOTE: boolean unitDoesntMatter tells it whether we care about whether or not the space is occupied
 		//		It should ONLY be set to true if we are checking goals or cheating...
 		
 		ArrayList<UnitView> neighbors = new ArrayList<UnitView>(); //The return list of all neighbors
 		
 		Integer x = currentParent.getXPosition();
 		Integer y = currentParent.getYPosition();
 		Integer xPlusOne = x + 1;
 		Integer xMinusOne = x - 1;
 		Integer yPlusOne = y + 1;
 		Integer yMinusOne = y - 1;		
 		
 		Integer tempX = 0, tempY = 0;
 		
 		for (int j = 0; j < 8; j++) //go through all possible 8 squares
 		{
 			switch(j) //Could use something better but it's too much thinking right now
 			{
 				case 0: //x + 1, y
 					tempX = xPlusOne;
 					tempY = y;
 					break;
 				case 1: //x + 1, y + 1
 					tempX = xPlusOne;
 					tempY = yPlusOne;
 					break;
 				case 2: //x + 1, y - 1
 					tempX = xPlusOne;
 					tempY = yMinusOne;
 					break;
 				case 3: //x, y + 1
 					tempX = x;
 					tempY = yPlusOne;
 					break;
 				case 4: //x, y - 1
 					tempX = x;
 					tempY = yMinusOne;
 					break;
 				case 5: //x - 1, y
 					tempX = xMinusOne;
 					tempY = y;
 					break;
 				case 6: //x - 1, y + 1
 					tempX = xMinusOne;
 					tempY = yPlusOne;
 					break;
 				case 7: //x - 1, y - 1
 					tempX = xMinusOne;
 					tempY = yMinusOne;
 					break;
 				default:
 					break;
 			}
 			
 			UnitView neighbor = createOpenSpace(tempX, tempY, state); //make a dummy space
 			
 			if(checkValidNeighbor(tempX, tempY, state, unitDoesntMatter)) //check if it's a valid space
 			{
 				neighbors.add(neighbor);
 			}
 		}		
 		
 		return neighbors;
 	}
 	
 	/*
 	 * NEEDS TO CALCULATE WHICH MOVE IS BEST
 	 * IF b == NODE AND WE ARE EMPTY THEN hCOST = 0
 	 * 
 	 */
 	public Integer heuristicCostCalculator(STRIP a, STRIP b)	{ 
 		
 		ArrayList<ResourceInfo> gold = a.gold;
 		ArrayList<ResourceInfo> wood = a.lumber;
 		
 		if (b.unit.getTemplateView().getUnitName().equals(gold))
 		{
 			;//do gold
 		}
 		else if(b.unit.getTemplateView().getUnitName().equals(wood))
 		{
 			;//do wood
 		}
 		else
 		{
 			;//do move
 		}
 		
 		return 0;
 	}
 	
 	/*
 	 * 
 	 * NEEDS TO CHANGE AND BE MODIFIED FOR RESOURCE.. MAYBE EVEN DELETED
 	 * 
 	 */
 	public boolean checkValidNeighbor(Integer x, Integer y, StateView state, boolean unitDoesntMatter) //returns if a space is empty and valid
 	{
 		boolean isResource = state.isResourceAt(x, y); //check if there is a resource here
 		boolean isUnit = state.isUnitAt(x, y); //check if there is a unit here
 		boolean isValid = state.inBounds(x, y); //check if the square is valid
 		
 		boolean isNotTaken = !isResource && !isUnit; //if it is not an occupied square
 		
 		if ((isNotTaken || unitDoesntMatter) && isValid) //if there is no resource here and no unit and it's valid it means it's an empty square
 		{
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public void writeListToFile(ArrayList<String> a, String fileName)
 	{
 		try //create file edit and write
 		{
 			FileWriter fstream = new FileWriter(fileName);
 			BufferedWriter planFile = new BufferedWriter(fstream);
 			for(int i = 0; i < a.size(); i++)
 			{
 				planFile.write(a.get(i));
 				planFile.newLine();
 			}
 			planFile.close();
 		}
 		catch (Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 		}
 	}	
 }
