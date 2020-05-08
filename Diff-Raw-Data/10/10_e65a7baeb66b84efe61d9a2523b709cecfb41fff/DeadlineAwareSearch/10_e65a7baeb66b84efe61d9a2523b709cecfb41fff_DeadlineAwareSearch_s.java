 package agents;
 
 import java.util.ArrayList;
 
 import au.rmit.ract.planning.pathplanning.entity.ComputedPlan;
 import au.rmit.ract.planning.pathplanning.entity.State;
 import au.rmit.ract.planning.pathplanning.entity.Plan;
 import au.rmit.ract.planning.pathplanning.entity.SearchDomain;
 import pplanning.interfaces.PlanningAgent;
 import pplanning.simviewer.model.GridCell;
 import pplanning.simviewer.model.GridDomain;
 import java.lang.management.ManagementFactory;
 import java.lang.management.ThreadMXBean;
 
 public class DeadlineAwareSearch implements PlanningAgent
 {
 	private ComputedPlan plan;
 
 	//DasMapInfo mapInfo;
 	FastDasMapInfo mapInfo;
 
 	// number of steps taken in current plan
 	private int stepNo = 0;
 
 	final private long MS_TO_NS_CONV_FACT = 1000000;
 
 	// Percentage of deadline to be used generating plan. (As opposed to moving
 	// along the plan afterwards.)
 	final private float SEARCH_TIME_FRACTION = 0.95f;
 
 	// Should the open and closed sets be regenerated?
 	boolean shouldUpdateOpen = false;
 	boolean shouldUpdateClosed = false;
 
 	// These values needs tuning!
 
 	// r_default. Used before conExpansionIntervals has settled.
 	// This is the number of expansions to perform before the sliding window is deemed 'settled'
 	final private int SETTLING_EXPANSION_COUNT = 200;
 
 	// Updating count that needs to be reached to indicate that we are settled.
 	private int expansionCountForSettling;
 
 
 	// This is the size of the sliding window, in entries.
 	final private int EXPANSION_DELAY_WINDOW_LENGTH = 10;
 
 	// Sliding window to calculate average single step error.
 	private SlidingWindow expansionDelayWindow = new SlidingWindow(
 			EXPANSION_DELAY_WINDOW_LENGTH);
 
 	// For timing
 	final ThreadMXBean threadMX = ManagementFactory.getThreadMXBean();
 
 	long timeAtLastDifferentMeasurement;
 	long countExpansionsAtLastDifferentMeasurement;
 	long timePerExpansion;
 
 	@Override
 	public GridCell getNextMove(GridDomain map, GridCell start, GridCell goal,
 			int stepLeft, long stepTime, long timeLeft) {
 
 		try {
 			Trace.Enable(false);
 
 			// If there is no plan, generate one.
 			if (plan == null)
 			{
 
 				// TODO: base search buffer on the length of the solution.
 				long timeCurrent = threadMX.getCurrentThreadCpuTime();
 				long searchTime = (long) ((timeLeft * MS_TO_NS_CONV_FACT) * SEARCH_TIME_FRACTION);
 				long timeDeadline = timeCurrent + searchTime;
 
 				System.out.println("current time (ns): " + timeCurrent);
 				System.out.println("deadline: " + timeDeadline);
 				Trace.Enable(true);
 
 				// a new plan has been generated, update open and closed debug sets.
 				shouldUpdateOpen = true;
 				shouldUpdateClosed = true;
 
 
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
 
 
 
 		//System.out.println("Generating a new plan");
 
 		// Map info exists outside of this function so that its open and closed
 		// sets for debug display.
 		//mapInfo = new DasMapInfo(map);
 		mapInfo = new FastDasMapInfo(map);
 
 		// Track the number of expansions performed -  e_curr value
 		// TODO: investigate refactoring this to long to avoid potential truncactions in operations
 		int expansionCount = 0;
 
 		// Initialize open set with start node.
 		float hCost = map.hCost(start, goal);
 		int dCost = dCostManhattan((GridCell)start, goal);
 
 
 		ComputedPlan incumbentPlan = null;
 		//incumbentPlan = speedierSearch(map, start,goal);
 
 
 		assert threadMX.isCurrentThreadCpuTimeSupported();
 		threadMX.setThreadCpuTimeEnabled(true);
 
 		long timeAfterGreedy = threadMX.getCurrentThreadCpuTime();
 		long timeUntilDeadline = timeDeadline - timeAfterGreedy;
 		System.out.println("time after greedy: " + timeAfterGreedy);
 		System.out.println("time left: " + timeUntilDeadline);
 
 
 
 		mapInfo.addStartCell(start, hCost, dCost);
 
 		timeAtLastDifferentMeasurement = threadMX.getCurrentThreadCpuTime();
 		// Continue until time has run out
 		while (threadMX.getCurrentThreadCpuTime() < timeDeadline)
 		{
 			//System.out.println("expansionCount:" + expansionCount);
 			// TODO: is this a good inital value?
 
 
 			if (!mapInfo.isOpenEmpty()) {
 				GridCell current = mapInfo.closeCheapestOpen();
 
 				// If this node has a higher g cost than the incumbent plan, discard it.
 				if (incumbentPlan != null
 						&& mapInfo.getGCost(current) > incumbentPlan.getCost()) {
 					continue;
 				}
 
 				//System.out.println(current.getCoord() +" " +  mapInfo.getFCost(current));
 				// If the current state is a goal state, and the cost to get there was cheaper
 				// than that of the incumbent solution
 				if (current == goal)
 				{
 					System.out.println("Found path to goal! cost = " + mapInfo.getGCost(current));
 					//TODO: this is a potentially expensive operation!
 					incumbentPlan = mapInfo.computePlan(goal);
 					// The below hack is to test finding the first goal!
 					//return incumbentPlan;
 				}
 				else if (expansionCount <= expansionCountForSettling ||
 						(mapInfo.getDCheapestWithError(current) < calculateMaxReachableDepth(timeDeadline)))
 				{
 					//Trace.print("(reachable) d_cheapest: " + estimateGoalDepth(current) + " d_max: " + dMax);
 					int count = 0;
 
 					// Expand current node. TODO: move this into its own method.
 					for (State stateIter : map.getSuccessors(current))
 					{
 						GridCell neighbor = (GridCell) stateIter;
 						//System.out.println("Generating Child " + count++);
 						// consider node if it can be entered and is not in closed or pruned list
 						if (map.isBlocked(neighbor) == false)
 						{
 							float neighborGCost = mapInfo.getGCost(current) + map.cost(current, neighbor);
 							float neighborHCost = map.hCost(neighbor, goal);
 
 							// NOTE: Using map's h cost estimate as d cheapest estimate.
 							int neighborDCheapestRaw = (int) map.hCost(neighbor, goal);
 
 							if (!mapInfo.cellExists(neighbor))
 							{
 								// Node has not been seen before, add it to the open set.
 								mapInfo.add(neighbor, neighborGCost, neighborHCost,
 										neighborDCheapestRaw, expansionCount, current);
 							}
 							else if (neighborGCost < mapInfo.getGCost(neighbor))
 							{
 								// Shorter path to node found, update gCost.
 								mapInfo.setGCost(neighbor, neighborGCost);
 
 								// If node was closed, put it back into the open list. The new
 								// cost might make it viable.
 								if (mapInfo.isClosed(neighbor) == true)
 								{
 									mapInfo.reopenCell((GridCell) neighbor, expansionCount, current);
 								}
 							}
 						}
 					} // end expansion
 
 					// Increment number of expansions.
 					expansionCount++;
 
 					// Insert expansion delay into sliding window.
 					int expansionDelay = expansionCount - mapInfo.getExpansionNumber(current);
 					expansionDelayWindow.push(expansionDelay);
 
 					// Calculate expansion interval.
 					long timeCurrent = threadMX.getCurrentThreadCpuTime();
 					if (timeCurrent != timeAtLastDifferentMeasurement)
 					{
 						long expansionTimeDelta = timeCurrent - timeAtLastDifferentMeasurement;
 						long expansionCountDelta = expansionCount - countExpansionsAtLastDifferentMeasurement;
 						timePerExpansion = expansionTimeDelta / expansionCountDelta;
 						timeAtLastDifferentMeasurement = timeCurrent;
 						countExpansionsAtLastDifferentMeasurement = expansionCount;
 						/*
 						System.out.println(
 								"\n expansionTimeDelta " + expansionTimeDelta +
 								"\n expansionCountDelta " + expansionCountDelta +
 								"\n timePerExpansion: " + timePerExpansion);
 								*/
 					}
 
 
 					// Insert expansion interval into sliding window.
 					//expansionIntervalWindow.push(expansionInterval);
 				}
 				else /* expansionCount > settlingCount && dCheapest > dMax */
 				{
 					//System.out.println("Pruning " + current.getCoord());
 					System.out.println("Pruning cell, expansion count = " + expansionCount);
 					mapInfo.pruneCell(current);
 				}
 			}
 			else
 			{
 				// Open list is empty, so we need to repopulate it.
 				if (!mapInfo.isPrunedEmpty())
 				{
 					int exp = calculateExpansionsRemaining(timeDeadline);
 					return null;
 					/*
 					mapInfo.recoverPrunedStates(exp);
 					expansionDelayWindow.reset();
 					//expansionIntervalWindow.reset();
 					expansionCountForSettling = expansionCount + SETTLING_EXPANSION_COUNT;
 					*/
 				}
 				else
 				{
 					System.out.println("Pruned and open are empty");
 					break;
 				}
 
 			}
 		}
 		//System.out.println("Returning solution with " + incumbentPlan.getLength() + " nodes");
 		return incumbentPlan;
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
 	 * Estimate the number of expansions that can be performed before the deadline (dMax).
 	 * @param timeDeadline the time that a solution must be found by (ns)
 	 * @return estimated number of expansions or dMax
 	 */
 	public int calculateMaxReachableDepth(long timeDeadline)
 	{
 		double avgExpansionDelay = expansionDelayWindow.getAvg();
 
 		int dMax = (int) (calculateExpansionsRemaining(timeDeadline) / avgExpansionDelay);
 
 //		System.out.println(dMax + " maximum reachable depth");
 		return dMax;
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
 		long timeRemaining = timeDeadline - threadMX.getCurrentThreadCpuTime();
 		//float averageInterval = expansionIntervalWindow.getAvg();
 		//float averageRate = 1.0f / averageInterval;
 		float averageRate = 1.0f / timePerExpansion;
 		int exp = (int) (timeRemaining * averageRate);
 
 //		System.out.println("Calculating expansions remaining:" +
 //				"\nexpansions remaining: " + exp +
 //				"\ntime remaining: " + timeRemaining +
 //				"\naverage expansion rate: " + averageRate);
 
 		return exp;
 	}
 
 	private ComputedPlan speedierSearch(GridDomain map, GridCell start, GridCell goal)
 	{
 
 		GreedyMapInfo mapInfo = new GreedyMapInfo(map);
 		float hCost = map.hCost(start, goal);
 		mapInfo.add(start, 0, hCost);
 
 		ComputedPlan incumbentPlan = null;
 
 		while (mapInfo.isOpenEmpty() == false)
 		{
 			GridCell current = mapInfo.closeCheapestOpen();
 			if (current == goal)
 			{
 				System.out.println("Goal found with speedier search, GCost" + mapInfo.getGCost(current));
 				incumbentPlan = mapInfo.computePlan(current);
 				break;
 			}
 
 			for (State neighbor : map.getSuccessors(current))
 			{
 				if (map.isBlocked(neighbor) == false &&
 						mapInfo.isClosed((GridCell) neighbor) == false &&
 						mapInfo.isOpen((GridCell)neighbor) == false)
 				{
 					float hNeighbor = map.hCost(neighbor, goal);
 					float gNeighbor = mapInfo.getGCost((GridCell)current) + map.cost(current, neighbor);
 					mapInfo.add((GridCell) neighbor, gNeighbor, hNeighbor, current);
 				}
 			}
 		}
 
 		return(incumbentPlan);
 	}
 
 
 
 	// -- Apparate Debug Output --
 
 	private ArrayList<GridCell> closedNodes;
 	private ArrayList<GridCell> prunedNodes;
 
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
 			prunedNodes = mapInfo.getPrunedArrayList();
 		}
 		return prunedNodes;
 	}
 
 
 	@Override
 	public ComputedPlan getPath() {
 		return plan;
 	}
 }
