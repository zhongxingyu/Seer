 package edu.cwru.SimpleRTS.agent;
 
 import java.io.*;
 import java.util.*;
 
 import edu.cwru.SimpleRTS.environment.State.StateView;
 import edu.cwru.SimpleRTS.model.resource.ResourceNode.Type;
 import edu.cwru.SimpleRTS.model.unit.Unit;
 import edu.cwru.SimpleRTS.model.unit.Unit.UnitView;
 import edu.cwru.SimpleRTS.model.unit.UnitTemplate;
 import edu.cwru.SimpleRTS.util.DistanceMetrics;
 
 
 public class Planner {
 
 	private static final long serialVersionUID = 1L;
 	static String townHall = "TownHall";
 	static String peasant = "Peasant";
 	static String farm = "Farm";
 	static String barracks = "Barracks";
 	static String footman = "Footman";
 	static String lumber = "lumber";
 	static String gold = "gold";
 	static String move = "move";
 	static String gather = "gather";
 	static String deposit = "deposit";
 	static int goldWeCanCarry = 50;
 	static int woodWeCanCarry = 20;
 	static int playernum = 0;
 	
 	private boolean canBuildPeasant = false;
 	private String planFileName = "plan.txt";
 	private ArrayList<String> plan = new ArrayList<String>();
 	private int finalGoldTally = 1000;
 	private int finalWoodTally = 1000;
 	
 	public ArrayList<ResourceInfo> goldList = new ArrayList<ResourceInfo>();
 	public ArrayList<ResourceInfo> lumberList = new ArrayList<ResourceInfo>();
 	
 	public Planner(StateView startState, int finalGoldAmount, int finalWoodAmount, boolean canBuildP)
 	{
 		finalGoldTally = finalGoldAmount;
 		finalWoodTally = finalWoodAmount;
 		canBuildPeasant = canBuildP;		
 		
 		
 		addResources(Type.GOLD_MINE, goldList, startState);
 		addResources(Type.TREE, lumberList, startState);		
 	}
 	
 	
 	public void addResources(Type resourceType, ArrayList<ResourceInfo> resources, StateView state)
 	{
 		List<Integer> resourceIds = state.getResourceNodeIds(resourceType);
 		for (Integer resourceId: resourceIds)
 		{
 			ResourceInfo resource = new ResourceInfo();
 			resource.x = state.getResourceNode(resourceId).getXPosition();
 			resource.y = state.getResourceNode(resourceId).getYPosition();
 			resource.totalAvailable = state.getResourceNode(resourceId).getAmountRemaining();
 			resource.type = resourceType;
 			resources.add(resource);
 		}
 	}
 	
 	//originally A* search
 	public ArrayList<STRIP> generatePlan(Integer startId, Integer goalId, StateView state)	
 	{		
 		STRIP startSpace = new STRIP(); 
 		startSpace.unit = state.getUnit(startId); //starting space
 		startSpace.gold.addAll(goldList);
 		startSpace.lumber.addAll(lumberList);
 		
 		STRIP goalSpace = new STRIP(); //end space //NEEDS TO JUST BE GOAL OF TALLY
 		goalSpace.unit = state.getUnit(goalId);
 		goalSpace.goldCollected = finalGoldTally;
 		goalSpace.woodCollected = finalWoodTally;
 		
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
 		
 		while (openList.size() > 0) //loop till we exhaust the openList
 		{
 			STRIP currentParent = getLowestCostF(openList, fCost); //finds the STRIP with the lowest fCost
 
 			System.out.println("Searching.. " + currentParent.unit.getTemplateView().getUnitName() + " " + currentParent.unit.getXPosition() + ", " 
 					+ currentParent.unit.getYPosition() + 
 					" There are " + openList.size() + " items on the OL");
 			
 			if (checkGoal(currentParent, goalSpace, state)) //success
 			{
 				System.out.println("Woot, found the goal");					
 				
 				return rebuildPath(parentNodes, currentParent, startSpace);
 			}
 			else //keep on searching
 			{
 				openList.remove(currentParent); //remove the object from the openList and add it to the closed list
 				closedList.add(currentParent);
 				
 				ArrayList<STRIP> neighbors = getActions(currentParent, state); //We need to implement neighbor checking and only return valid neighbor types.. i.e. movable squares
 				
 				System.out.println("Found " + neighbors.size() + " neighbors");
 				
 				for (STRIP neighbor : neighbors) //loop for all neighbors
 				{
 					System.out.println("Searching neighbor at : " + neighbor.unit.getTemplateView().getUnitName() + " to " +
 							 neighbor.unit.getXPosition() + ", " + neighbor.unit.getYPosition());
 					
 					if (checkXYList(closedList, neighbor) == (null)) //only go if the neighbor isn't all ready checked
 					{
 						tempGCost = gCostCalculator(neighbor, currentParent, gCost); //grab it's gCost
 						
 						boolean better = true; //used to check if we found a better gCost in the case of the node all ready being in the openList
 						STRIP tempNeighbor = neighbor; //temp used in case the neighbor isn't in the openList yet
 						neighbor = checkXYList(openList, neighbor);
 						
 						if (neighbor == null) //If the openList doesn't contain this neighbor
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
 					else
 					{
 						//System.out.println("")
 					}
 				}
 			}
 		}		
 		System.out.println("No path from search space to goal...");
 		return null; //returns null if we don't find anything
 	}
 	
 	/*
 	 * Returns nearest resource. If none harvestable it returns null
 	 * 
 	 * 
 	 */
 	public ResourceInfo findNearestResource(STRIP node, ArrayList<ResourceInfo> resources)
 	{
 		ResourceInfo returnResource = null;
 		int minDistance = -1;
 		
 		for (ResourceInfo resource: resources)
 		{
 			if (resource.totalAvailable > 0) //ensure it's not a dead node
 			{
 				Integer distance = DistanceMetrics.chebyshevDistance(node.unit.getXPosition(), node.unit.getYPosition(),
 						resource.x, resource.y);
 				if (minDistance == -1 || distance < minDistance)
 				{
 					minDistance = distance; 
 					returnResource = resource;
 				}
 			}
 		}
 		
 		return returnResource;
 	}
 	
 	public UnitView checkValidDeposit(STRIP node, StateView state)
 	{		
 		List<Integer> townHallIds = findUnitType(state.getAllUnitIds(), state, townHall);
 		
 		for (Integer townHallId: townHallIds)
 		{
 			UnitView townHall = state.getUnit(townHallId);
 			
 			if (townHall.getXPosition() == node.unit.getXPosition()
 					&& townHall.getYPosition() == node.unit.getYPosition())
 			{
 				return townHall;
 			}
 		}
 		
 		return null;
 	}
 	
 	public ResourceInfo checkValidGather(STRIP node, StateView state)
 	{	
 		ArrayList<ResourceInfo> resources = new ArrayList<ResourceInfo>();
 		resources.addAll(node.gold);
 		resources.addAll(node.lumber);
 		
 		for (ResourceInfo resource: resources)
 		{			
 			if (resource.x == node.unit.getXPosition()
 					&& resource.y == node.unit.getYPosition() && resource.totalAvailable > 0)
 			{
 				return resource;
 			}
 		}
 		
 		return null;
 	}
 	
 	
 	public ArrayList<STRIP> getActions(STRIP node, StateView state)
 	{		
 		ArrayList<STRIP> returnActions = new ArrayList<STRIP>();
 		
 		STRIP moveMove =  new STRIP();
 		STRIP depositMove = new STRIP();
 		STRIP gatherMove = new STRIP();
 		
 		ResourceInfo nearestLumber = findNearestResource(node, node.lumber);
 		ResourceInfo nearestGold = findNearestResource(node, node.gold);
 		
 		if ((!node.hasGold && !node.hasWood && (nearestGold != null || nearestLumber != null)) )
 		{
 			if (node.goldCollected < finalGoldTally && nearestGold != null)
 			{
 				UnitView temp = createOpenSpace(nearestGold.x, nearestGold.y, move);
 				moveMove.unit = temp;
 			}
 			else if (node.woodCollected < finalWoodTally && nearestLumber != null)
 			{
 				moveMove.unit = createOpenSpace(nearestLumber.x, nearestLumber.y, move);
 			}
 			else //we have collected enough supplies don't move
 			{
 				moveMove.unit = createOpenSpace(node.unit.getXPosition(), node.unit.getYPosition(), move);			
 			}
 		}
 		else //move to townhall
 		{
 			List<Integer> townHallIds = findUnitType(state.getAllUnitIds(), state, townHall);
 			moveMove.unit = createOpenSpace(state.getUnit(townHallIds.get(0)).getXPosition(), state.getUnit(townHallIds.get(0)).getYPosition(), move);
 			moveMove.hasGold = node.hasGold;
 			moveMove.hasWood = node.hasWood;
 		}
 		
 		depositMove.unit = createOpenSpace(node.unit.getXPosition(), node.unit.getYPosition(), deposit);
 		gatherMove.unit = createOpenSpace(node.unit.getXPosition(), node.unit.getYPosition(), gather);
 		
 		moveMove.gold.addAll(node.gold);
 		moveMove.lumber.addAll(node.lumber);
 		moveMove.goldCollected = node.goldCollected;
 		moveMove.woodCollected = node.woodCollected;
 		
 		if (checkValidDeposit(node, state) != null)
 		{
 			depositMove.gold.addAll(node.gold);
 			depositMove.lumber.addAll(node.lumber);
 			depositMove.goldCollected = node.goldCollected;
 			depositMove.woodCollected = node.woodCollected;
 			
 			if (node.hasGold)
 			{
 				depositMove.goldCollected += goldWeCanCarry;
 				depositMove.hasGold = false;
 				returnActions.add(depositMove);
 			}
 			else if (node.hasWood)
 			{
 				depositMove.woodCollected += woodWeCanCarry;
 				depositMove.hasWood = false;
 				returnActions.add(depositMove);
 			}
 			
 		}
 
 		gatherMove.gold.addAll(node.gold);
 		gatherMove.lumber.addAll(node.lumber);
 		gatherMove.goldCollected = node.goldCollected;
 		gatherMove.woodCollected = node.woodCollected;
 		ResourceInfo validResource = checkValidGather(node, state);
 		
 		if (validResource != null)
 		{
 			if (validResource.type == Type.GOLD_MINE)
 			{
 				validResource.totalAvailable -= goldWeCanCarry;
 				validResource.collected += goldWeCanCarry;
 				gatherMove.hasGold = true;
 				returnActions.add(gatherMove);
 			}
 			else if (validResource.type == Type.TREE)
 			{
 				validResource.totalAvailable -= woodWeCanCarry;
 				validResource.collected += woodWeCanCarry;
 				gatherMove.hasWood = true;
 				returnActions.add(gatherMove);
 			}
 		}
 		
 		returnActions.add(moveMove);
 		
 		return returnActions;		
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
 	public boolean checkGoal(STRIP neighbor, STRIP goal, StateView state) //checks if we have reached the goal based on if we neighbor the goalSpace
 	{
 		
 		if (neighbor.goldCollected >= goal.goldCollected &&
 				neighbor.woodCollected >= goal.woodCollected )
 		{
 			return true;
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
 	public Integer gCostCalculator(STRIP neighbor, STRIP currentParent, HashMap<STRIP, Integer> gCost)
 	{
 		Integer cost = gCost.get(currentParent); //currentParent's gCost
 		
 		cost += DistanceMetrics.chebyshevDistance(neighbor.unit.getXPosition(), neighbor.unit.getYPosition(),
 				currentParent.unit.getXPosition(), currentParent.unit.getYPosition());
 		
 		return cost;
 		
 	}
 	
 	
 	/*
 	 * I BELIEVE THIS WILL BE USELESS
 	 * 
 	 * 
 	 */
 	public STRIP checkXYList(ArrayList<STRIP> list, STRIP unit) //Used for checking based on whether or not we all ready have the space of values: x, y
 	{
 		Integer x = unit.unit.getXPosition();
 		Integer y = unit.unit.getYPosition();
 		
 		for (STRIP item : list) //for every item in the list
 		{
 			if (item.unit.getXPosition() == (x) && item.unit.getYPosition() == (y) && 
 					item.goldCollected == unit.goldCollected && item.woodCollected == unit.woodCollected &&
 					item.hasWood == unit.hasWood && item.hasGold == unit.hasGold &&
 					item.unit.getTemplateView().getUnitName().equals(unit.unit.getTemplateView().getUnitName())) //if it's there
 				return item; //return it
 		}
 		return null; //otherwise return nothing
 	}
 	
 	//Goes through oList and checks against Hashmap fCost to find the UnitView with the lowest fCost
 	public STRIP getLowestCostF(ArrayList<STRIP> oList, HashMap<STRIP, Integer> fCost)
 	{
 		STRIP lowestCostF = oList.get(0); // set the first node as the lowest case
 		
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
 	public ArrayList<STRIP> rebuildPath(HashMap<STRIP, STRIP> parentNodes, STRIP goalParent, STRIP startParent)
 	{
 		ArrayList<STRIP> backwardsPath = new ArrayList<STRIP>(); //The path backwards
 		backwardsPath.add(goalParent); //add the goal as our first action
 		ArrayList<STRIP> returnPath = new ArrayList<STRIP>();
 		
 		STRIP parentNode = parentNodes.get(goalParent);
 		backwardsPath.add(parentNode);
 		
 		while (!parentNode.equals(startParent)) //run till we find the starting node
 		{
 			parentNode = parentNodes.get(parentNode);
 			backwardsPath.add(parentNode);
 		}
 		
 		for(int i = (backwardsPath.size()-1); i > 0; i--)
 		{
 			returnPath.add(backwardsPath.get(i));
 			STRIP action = backwardsPath.get(i);
 			String output = "Path action: " + action.unit.getTemplateView().getUnitName() + " FROM: "
 					+ action.unit.getXPosition() + ", " + action.unit.getYPosition() + "\n";
 			plan.add(output);
 			System.out.print(output);
 		}
 		writeListToFile(plan, planFileName);
 		return returnPath;		
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
 	
 	public UnitView createOpenSpace(Integer x, Integer y, String name) //creates a dummy UnitView at the requested space
 	{
 		UnitTemplate template = new UnitTemplate(0); //The template, ID 0 is used because we don't care what type it is
 		
 		template.setUnitName(name);
 		
 
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
 				
 		Integer hCost = b.goldCollected + b.woodCollected - a.goldCollected - b.goldCollected;
 		
 		if (a.unit.getTemplateView().getUnitName() == gather)
 		{
 			if (a.hasGold)
 			{
 				hCost -= (int)(goldWeCanCarry * .5);
 			}
 			else if (a.hasWood)
 			{
 				hCost -= (int)(woodWeCanCarry * .5);
 			}
 		}
 		else if (a.unit.getTemplateView().getUnitName() == move)
 		{
 			hCost -= (int)DistanceMetrics.chebyshevDistance(a.unit.getXPosition(), a.unit.getYPosition(), b.unit.getXPosition(), b.unit.getYPosition());
 		}
 		return hCost;
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
 			}
 			planFile.close();
 		}
 		catch (Exception e)
 		{
 			System.err.println("Error: " + e.getMessage());
 		}
 	}	
 }
