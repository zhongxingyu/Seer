 package agents;
 
 import java.util.ArrayList;
 
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
 
 	final private long MS_TO_NS_CONV_FACT = 1000000;
 
 	// Percentage of deadline to be used generating plan. (As opposed to moving
 	// along the plan afterwards.)
 	final private float SEARCH_TIME_FRACTION = 0.97f;
 
 	// Should the open and closed sets be regenerated?
 	boolean shouldUpdateOpen = false;
 	boolean shouldUpdateClosed = false;
 
 	@Override
 	public GridCell getNextMove(GridDomain map, GridCell start, GridCell goal,
 			int stepLeft, long stepTime, long timeLeft) {
 
 		try {
 			Trace.Enable(true);
 
 			// If there is no plan, generate one.
 			if (plan == null)
 			{
 				// a new plan has been generated, update open and closed debug sets.
 				shouldUpdateOpen = true;
 				shouldUpdateClosed = true;
 
 				// TODO: base search buffer on the length of the solution.
 				long timeCurrent = System.nanoTime();
 				long searchTime = (long) ((timeLeft * MS_TO_NS_CONV_FACT) * SEARCH_TIME_FRACTION);
 				long timeDeadline = timeCurrent + searchTime;
 
 				Trace.print("current time (ns): " + timeCurrent);
 				Trace.print("deadline: " + timeDeadline);
 				Trace.Enable(false);
 				plan = generatePlan(map, start, goal, timeDeadline);
 
 				// If plan was not found, return start node.
 				if (plan == null)
 				{
 					Trace.print("No plan found within deadline");
 					return start;
 				}
 
 				// Plan was found, reset step count.
 				stepNo = 0;
 			}
 
 			// Check if path has been exhausted.
 			if (stepNo >= plan.getLength()) {
 				return start;
 			}
 
 			// Return the next step in the path.
 			return (GridCell) plan.getStep(stepNo++);
 		}
 		catch (Exception e)
 		{
 			// Catch all exceptions before the propagate into Apparate.
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
 
 		System.out.println("Generating a new plan");
 		int nMaxReachableDepth = Integer.MAX_VALUE;
 		mapInfo = new DasMapInfo(map);
 
 		// initialize open set with start node
 		float hCost = map.hCost(start, goal);
 		int dCost = dCostManhattan((GridCell)start, goal);
 		mapInfo.add(start, 0f, hCost, dCost, 0);
 
 		// Continue until time has run out
 		while (System.nanoTime() < timeDeadline)
 		{
 			if (!mapInfo.isOpenEmpty())
 			{
 				// TODO: Per last section of the pruning section of the DAS paper,
 				// dMax should not be calculated while initially settling, or settling after
 				// a repopulation of the open set from the pruned set.
 				if (mapInfo.getSettled())
 				{
 					nMaxReachableDepth = calculateMaxReachableDepth(timeDeadline);
 				}
 
 				//Trace.print("just calced d_max: " + nMaxReachableDepth);
 				GridCell current = mapInfo.closeCheapestOpen();

 				// If the current state is a goal state, and the cost to get there was cheaper
 				// than that of the incumbent solution
 				if ( current == goal)
 				{
 					System.out.println("found path to goal!");
 					if ( (mapInfo.GetIncumbentPlan() == null) ||
 						 (mapInfo.getGCost(goal) < mapInfo.GetIncumbentPlan().getCost()) )
 					{
 						System.out.println("new path to goal is an improvement!");
 						//TODO: this is a potentially expensive operation!
 						mapInfo.computePlan(goal);
 						// The below hack is to test finding the first goal!
 						//return(mapInfo.GetIncumbentPlan());
 					}
 					else
 					{
 						System.out.println("new path to goal is worse than incumbent!");
 					}
 
 				}
 				else if (!mapInfo.getSettled() ||
 						(estimateGoalDepth(current) < nMaxReachableDepth) )
 				{
 					//Trace.print("(reachable) d_cheapest: " + estimateGoalDepth(current) + " d_max: " + nMaxReachableDepth);
 					for (State neighbor : map.getSuccessors(current))
 					{
 						// consider node if it can be entered and is not in closed or pruned list
 						if (map.isBlocked(neighbor) == false &&
 						    mapInfo.isClosed((GridCell) neighbor) == false &&
 						    mapInfo.isPruned((GridCell) neighbor) == false)
 						{
 							if (mapInfo.isOpen((GridCell) neighbor) == false)
 							{
 								float fNeighborGCost = mapInfo.getGCost(current) + map.cost(current, neighbor);
 								float fNeighborHCost = map.hCost(neighbor, goal);
 
 								//System.out.println("g: " + fNeighborGCost + " h" + fNeighborHCost);
 								// TODO: this is currently assuming manhattan grid world. See Issue #11
 								int nNeighbourDCost = (int) dCostManhattan((GridCell)neighbor, goal);
 								//System.out.println("d" + nNeighbourDCost);
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
 					//Trace.print("(unreachable) d_cheapest: " + estimateGoalDepth(current) + " d_max: " + nMaxReachableDepth);
					if (mapInfo.getSettled())
 					{
 						mapInfo.pruneCell(current);
 					}
 				}
 			}
 			else
 			{
 				// Open list is empty, so we need to repopulate it.
 				if (!mapInfo.isPrunedEmpty())
 				{
 					int exp = calculateExpansionsRemaining(timeDeadline);
 					mapInfo.recoverPrunedStates(exp);
 				}
 				else
 				{
 					break;
 				}
 				//System.out.print(exp);
 
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
 	 * Get the predicted number of expansions that can be performed in the
 	 * specified time. The estimate is based on the current average expansion rate
 	 * and the time remaining.
 	 *
 	 *       exp = t * r
 	 *
 	 *       where t is time remaining
 	 *         and r is the current average expansion rate
 	 *
 	 * @param timeDeadline the time of the deadline, in nanoseconds
 	 * @return expansions remaining
 	 */
 	public int calculateExpansionsRemaining(long timeDeadline)
 	{
 		long timeRemaining = timeDeadline - System.nanoTime();
 		float averageInterval = mapInfo.calculateAvgExpansionInterval();
 		float averageRate = 1 / averageInterval;
 
 		int exp = (int) (timeRemaining * averageRate);
 
 		Trace.print("Calculating expansions remaining:" +
 				"\nexpansions remaining: " + exp +
 				"\naverage expansion rate: " + averageRate);
 
 		return exp;
 	}
 
 	/**
 	 * Calculate the estimated depth of goal under this cell
 	 * For the given cell, we want to get its dCheapest
 	 * We can use this dCheapest, and that of its parent, as well as the mean single step error
 	 * to compute a dCheapestWithError.
 	 *
 	 * @param cell the cell from which to estimate
 	 * @return estimated number of expansions from this cell to the goal
 	 */
 	public float estimateGoalDepth(GridCell cell)
 	{
 		return mapInfo.getDCheapestWithError(cell);
 	}
 
 	// -- Apparate Debug Output --
 
 	private ArrayList<GridCell> closedNodes;
 	private ArrayList<GridCell> openNodes;
 
 	@Override
 	public Boolean showInfo() {
 		//return false;
 		return mapInfo != null;
 	}
 
 	@Override
 	public ArrayList<GridCell> expandedNodes() {
 		if (shouldUpdateClosed) {
 			shouldUpdateClosed = false;
 			closedNodes = mapInfo.getClosedArrayList();
 		}
 		return closedNodes;
 	}
 
 	@Override
 	public ArrayList<GridCell> unexpandedNodes() {
 		if (shouldUpdateOpen) {
 			shouldUpdateOpen = false;
 			openNodes = mapInfo.getOpenArrayList();
 		}
 		return openNodes;
 	}
 
 
 	@Override
 	public ComputedPlan getPath() {
 		return plan;
 	}
 }
