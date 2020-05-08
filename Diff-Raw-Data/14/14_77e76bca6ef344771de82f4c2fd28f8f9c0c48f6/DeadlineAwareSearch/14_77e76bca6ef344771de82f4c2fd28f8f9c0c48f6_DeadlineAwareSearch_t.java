 package agents;
 
 import java.util.ArrayList;
 import java.util.PriorityQueue;
 import java.util.Map;
 import java.util.HashMap;
 import java.util.Collection;
 import java.util.Set;
 import java.util.HashSet;
 
 import au.rmit.ract.planning.pathplanning.entity.ComputedPlan;
 import au.rmit.ract.planning.pathplanning.entity.State;
 import au.rmit.ract.planning.pathplanning.entity.Plan;
 import au.rmit.ract.planning.pathplanning.entity.SearchDomain;
 import pplanning.interfaces.PlanningAgent;
 import pplanning.simviewer.model.GridCell;
 import pplanning.simviewer.model.GridDomain;
 
 
 public class DeadlineAwareSearch implements PlanningAgent 
 {
 	private ComputedPlan plan;
 
 	DasMapInfo mapInfo;
 	
 	// number of steps taken in current plan
 	private int stepNo = 0;
 
 	// goal state from previous call to getNextMove()
 	private GridCell lastGoal = null;
 
 	final private long MS_TO_NS_CONV_FACT = 1000000;
 	
 	@Override
 	public GridCell getNextMove(GridDomain map, GridCell start, GridCell goal,
 			int stepLeft, long stepTime, long timeLeft) {
 
 		try {
 			Trace.Enable(true);
 			GridCell nextStep = null;
 
 			// recalculate plan if:
 			//  - no plan yet
 			//  - environment has changed
 			//  - goal has changed             TODO: check that equals actually works
 			//  - current node is not in path  TODO: is this a linear search?
 			//
 			// NOTE: I think Sebastian explicitly said that the goal wont move and the
 			// map wont change, so we could probably drop the last two conditions
 			boolean bReplan =
 				plan == null ||
 				map.getChangedEdges().size() > 0 ||
 				!lastGoal.equals(goal) ||
 				!plan.contains(start);
 
 
 			if (bReplan) 
 			{
 				long timeCurrent = System.nanoTime();
 				// TODO: Should we provide a buffer, i.e. 1% of the time, for provision of the plan to the recipient
 				long timeDeadline = timeCurrent + timeLeft * MS_TO_NS_CONV_FACT;
 				Trace.print("current time (ns): " + timeCurrent);
 				Trace.print("deadline: " + timeDeadline);
 				plan = generatePlan(map, start, goal, timeDeadline);
 				stepNo = 0;
 				lastGoal = goal;
 			}
 
 			// take next step in plan
 			if (stepNo < plan.getLength())
 			{
 				nextStep = (GridCell) plan.getStep(stepNo++);
 
 				if (nextStep == null) 
 				{
 					Trace.print("next step is null!");
 				}
 			}
 
 			return nextStep;
 
 		} 
 		catch (Exception e) 
 		{
 			e.printStackTrace();
 			return start;
 		}
 	}
 
 	/*
 	 * Algorithm description
 	 * 1) 	Initialise Open with starting state
 	 * 2) 	Initialise Pruned with empty structure
 	 * 3) 	Initialise Incumbent plan with NULL
 	 * 4) 	while (current time < deadline)
 	 * 		{
 	 * 5)		if Open is not empty
 	 * 			{
 	 * 6)			max_reachable_depth = calculate_d_bound()
 	 * 7)			state s = open.pop()
 	 * 8) 			if goal(s) && s > incumbent
 	 *  			{
 	 * 9) 				incumbent = s
 	 *   			}
 	 * 10) 			else if cheapest_solution_depth < max_reachable_depth
 	 *   			{
 	 * 11) 				s' = for each child of s
 	 *   				{
 	 * 12) 					open.push(s') 
 	 *   				}
 	 *   			}
 	 * 13) 			else
 	 *   			{
 	 * 14) 				pruned.push(s)
 	 *   			}
 	 *   		}
 	 * 15) 		else
 	 *   		{
 	 * 16) 			recover_pruned_states(open, pruned)
 	 *   		}
 	 *   	}
 	 *   
 	 * 17) 	return incumbent	
 	 */
 	private ComputedPlan generatePlan(GridDomain map, GridCell start,
 			GridCell goal, long timeDeadline) {
 
 		ComputedPlan planIncumbent = null;
 		int nMaxReachableDepth = Integer.MAX_VALUE;
 		int nEstimatedDepth = Integer.MAX_VALUE;
 		mapInfo = new DasMapInfo(map);
 		
 		float fHCost = map.hCost(start, goal);
 		
 		//TODO: Again, this is assuming manhattan world - see #Issue 11
 		int nDCost = dCostManhattan((GridCell)start, goal);
 
 		// initialize open set with start node
 		mapInfo.add(start, 0f, fHCost, nDCost, 0);
 
 		while (System.nanoTime() < timeDeadline)
 		{
 		
 			if (!mapInfo.isOpenEmpty())
 			{
 				
 				nMaxReachableDepth = calculateMaxReachableDepth(timeDeadline);
				Trace.print("just calced d_max: " + nMaxReachableDepth);
 				GridCell current = mapInfo.closeCheapestOpen();
 				
 				// If the current state is a goal state, and the cost to get there was cheaper
 				// than that of the incumbent solution
 				if ( current == goal)
 				{
 					Trace.print("found path to goal!");
 					if ( (mapInfo.GetIncumbentPlan() == null) || 
 						 (mapInfo.getGCost(goal) < mapInfo.GetIncumbentPlan().getCost()) )
 					{
 						Trace.print("new path to goal is an improvement!");
 						mapInfo.computePlan(goal);
 					}
 					else
 					{
 						Trace.print("new path to goal is worse than incumbent!");
 					}
 						
 				}
 				else if (estimateGoalDepth(current) < nMaxReachableDepth)
 				{
					Trace.print("(reachable) d_cheapest: " + estimateGoalDepth(current) + " d_max: " + nMaxReachableDepth);
 					for (State neighbor : map.getSuccessors(current)) 
 					{
 						// consider node if it can be entered and is not in closed or pruned list
 						if (map.isBlocked(neighbor) == false &&
 						    mapInfo.isClosed((GridCell) neighbor) == false &&
 						    mapInfo.isPruned((GridCell) neighbor) == false) 
 						{
 							float fNeighborGCost = mapInfo.getGCost(current) + map.cost(current, neighbor);
 							float fNeighborHCost = map.hCost(neighbor, goal);
 							
 							// TODO: this is currently assuming manhattan grid world. See Issue #11
 							int nNeighbourDCost = (int) dCostManhattan((GridCell)neighbor, goal);
 							
 							if (mapInfo.isOpen((GridCell) neighbor) == false)
 							{
 								// Add the neighbor to the open set!
 								mapInfo.add((GridCell) neighbor, fNeighborGCost, fNeighborHCost, 
 										nNeighbourDCost, current);
 							}
 							// Do we need the following case handling? Is the above enough to add s' to open? Step 12 of algorithm
 							/* else if (gCost < mapInfo.getGCost((GridCell) neighbor)) */
 						}
 					}
 				}
 				else
 				{
					Trace.print("(unreachable) d_cheapest: " + estimateGoalDepth(current) + " d_max: " + nMaxReachableDepth);
 					mapInfo.pruneCell(current);
 				}
 			}
 			else
 			{
 				// Open list is empty, so we need to repopulate it.
 				mapInfo.recoverPrunedStates(0.0f);
 			}
 		}
 		
 		return(mapInfo.GetIncumbentPlan());
 			
 	}
 	
 
 
 
 	/**
 	 * Estimate the number of expansions required to move from one state to
 	 * another in a gridworld where only four directional movement is permitted. 
 	 * @param from the starting state
 	 * @param to the goal state
 	 */
 	private int dCostManhattan(GridCell from, GridCell to) {
 		return Math.abs(to.getCoord().getX() - from.getCoord().getX()) +
 		       Math.abs(to.getCoord().getY() - from.getCoord().getY());
 	}
 
 	/**
 	 * Estimate the number of expansions required to move from one state to
 	 * another in a gridworld where diagonal movement is permitted. 
 	 * @param from the starting state
 	 * @param to the goal state
 	 */
 	private int dCostEuclidean(GridCell from, GridCell to) {
 		return Math.max(Math.abs(to.getCoord().getX() - from.getCoord().getX()),
 		                Math.abs(to.getCoord().getY() - from.getCoord().getY()));
 	}
 
 	@Override
 	public Boolean showInfo() {
 		return mapInfo != null;
 	}
 
 	@Override
 	public ArrayList<GridCell> expandedNodes() {
 		return mapInfo.getClosedArrayList();
 	}
 
 	@Override
 	public ArrayList<GridCell> unexpandedNodes() {
 		return mapInfo.getOpenArrayList();
 	}
 
 
 	@Override
 	public ComputedPlan getPath() {
 		return plan;
 	}
 	
 	/**
 	 * Calculate dMax
 	 * @return
 	 */
 	public int calculateMaxReachableDepth(long timeDeadline)
 	{
 		int nMaxDepth = Integer.MAX_VALUE;
 		
 		double fAvgExpansionDelay = mapInfo.calculateAvgExpansionDelay();
 		
 		nMaxDepth = (int) (calculateExpansionsRemaining(timeDeadline) / fAvgExpansionDelay);

		Trace.print(nMaxDepth + " maximum reachable depth");
 		return(nMaxDepth);
 	}
 	
 	/**
 	 * exp = t.r
 	 * t = time remaining
 	 * r = sliding window average of the delta times between expansions
 	 * @return
 	 */
 	public int calculateExpansionsRemaining(long timeDeadline)
 	{
 		int nExpansionsRemaining = 0;
 		
 		long timeCurrent = System.nanoTime();
 		long timeRemaining = timeDeadline - timeCurrent;
 		
 		double nAvgExpansionRate = 1/mapInfo.calculateAvgExpansionInterval();
		Trace.print(nAvgExpansionRate  + " avg expansion rate");
 		nExpansionsRemaining = (int) (timeRemaining * nAvgExpansionRate);
		Trace.print(nExpansionsRemaining + " expansions remaining (estimated)");
 		
 		return(nExpansionsRemaining);
 	}
 	
 	/**
 	 * Calculate the estimated depth of goal under this cell
 	 * For the given cell, we want to get it's dCheapest
 	 * We can use this dCheapest, and that of it's parent, as well as the mean single step error
 	 * to compute a dCheapestWithError.
 	 * @param _cell
 	 * @return
 	 */
 	public float estimateGoalDepth(GridCell _cell)
 	{
 		float fEstimatedGoalDepth = Integer.MAX_VALUE;
 		
 		fEstimatedGoalDepth = mapInfo.getDCheapestWithError(_cell);
 		
 		return(fEstimatedGoalDepth);
 	}
 	
 
 	
 
 }
