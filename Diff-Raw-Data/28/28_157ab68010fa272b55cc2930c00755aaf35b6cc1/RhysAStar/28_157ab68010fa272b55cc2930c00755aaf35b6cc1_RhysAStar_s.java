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
 
 public class RhysAStar implements PlanningAgent {
 	// plan to execute
 	private ComputedPlan plan;
 
 	MapInfo mapinfo;
 
 	private int stepNo = 0;
 
 	private GridCell lastGoal = null;
 
 	@Override
 	public GridCell getNextMove(GridDomain map, GridCell start, GridCell goal,
 			int stepLeft, long stepTime, long timeLeft) {
 
 		try {
 			Trace.Enable(false);
 			GridCell nextStep = null;
 
 			boolean bReplan =
 				plan == null ||			// no last path stored, have yet notr planned before?
 				map.getChangedEdges().size() > 0 ||	// map has had changes
 				!lastGoal.equals(goal) || // Goal has changed (equals not implemented?)
 				!plan.contains(start); // sNode is not in the path (sNode out of track)
 
 			if (bReplan) {
 				plan = generatePlan(map, start, goal);
 				stepNo = 0;
 				lastGoal = goal;
 			}
 
 			if (plan == null) {
 				System.out.println("Could not find goal");
 				return start;
 			}
 
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
 
 	private ComputedPlan generatePlan(GridDomain map, GridCell start,
 			GridCell goal) {
 
 		mapinfo = new MapInfo(map);
 
 		// initialize open set with start node
 		mapinfo.add(start, 0f, map.hCost(start, goal));
 
 		// repeat while states are left in open set
 		while (mapinfo.isOpenEmpty() == false) {
 			GridCell current = mapinfo.closeCheapestOpen();
 
 			Trace.print(current);
 
 			// if goal has been reached, return path
 			if (current == goal) {
 				Trace.print("found goal!");
 				return mapinfo.computePlan(goal);
 			}
 
 			// iterate through neighboring nodes
 			for (State neighbor : map.getSuccessors(current)) {
 
 				// consider node if it can be entered and is not in closed list
 				if (map.isBlocked(neighbor) == false &&
 						mapinfo.isClosed((GridCell) neighbor) == false) {
 
 					// get g cost of neighbor
 					float gCost = mapinfo.getGCost(current) + map.cost(current, neighbor);
 
 					if (mapinfo.isOpen((GridCell) neighbor) == false) {
 						// node not previously encountered, add it to the open set
 						mapinfo.add((GridCell) neighbor, gCost, map.hCost(neighbor, goal), current);
 					} else if (gCost < mapinfo.getGCost((GridCell) neighbor)) {
 						// more direct route to node found, update it
 						// NOTE: this can never happen with an admissible heuristic!
						System.out.println("failing now...");
 						//mapInfo.setGCost(neighbor, gCost);
 						//mapInfo.setParent(neighbor, current);
 					}
 				}
 			}
 		}
 		// no goal found
 		return null;
 	}
 
 	// Do we want to show extra info? (e.g., close and open nodes, current path)
 	@Override
 	public Boolean showInfo() {
 		return true;
 	}
 
 	@Override
 	public ArrayList<GridCell> expandedNodes() {
 
 		ArrayList<GridCell> open = new ArrayList<GridCell>();
 		ArrayList<GridCell> closed = new ArrayList<GridCell>();
 
 		if (mapinfo != null)
 		{
 			Trace.print("Open Set has" + mapinfo.openCount() + " entries");
 			// Need to make this open/closed query into singleton style - it iterates through every tile twice, due
 			// to being called at unexpanded nodes too!!
 			mapinfo.GetSearchSetsAsArrayList(open, closed);
 		}
 
 		return(closed);
 	}
 
 	@Override
 	public ArrayList<GridCell> unexpandedNodes() {
 		ArrayList<GridCell> open = new ArrayList<GridCell>();
 		ArrayList<GridCell> closed = new ArrayList<GridCell>();
 
 		if (mapinfo != null)
 		{
 			Trace.print("Closed Set has" + mapinfo.closedCount() + " entries");
 			// Need to make this open/closed query into singleton style - it iterates through every tile twice, due
 			// to being called at expanded nodes too!!
 			mapinfo.GetSearchSetsAsArrayList(open, closed);
 
 
 		}
 
 		return(open);
 
 	}
 
 
 	@Override
 	public ComputedPlan getPath() {
 		return plan;
 	}
 
 }
