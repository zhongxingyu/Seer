 package chris;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Queue;
 import java.util.Random;
 
 import client.Command;
 
 import chris.TaskDispenser.Node;
 import utils.Utils; 
 
 import levelObjects.*;
 
 
 public class TaskDispenser 
 {
 	// Example: boxesByColor.get(Color.cyan.ordinal()) gives an array of all cyan boxes
 	public ArrayList<ArrayList<Box>>   boxesByColor  = new ArrayList<ArrayList<Box>>(); // consider using simple arrays here instead
 	public ArrayList<ArrayList<Agent>> agentsByColor = new ArrayList<ArrayList<Agent>>();
 	public ArrayList<ArrayList<Goal>>  goalsByColor  = new ArrayList<ArrayList<Goal>>();
 	
 	// Example: boxesByID.get('a') gives an array of all boxes with A as ID
 	public Hashtable<Character,ArrayList<Box>>  boxesByID = new Hashtable<Character,ArrayList<Box>>();
 	public Hashtable<Character,ArrayList<Goal>> goalsByID = new Hashtable<Character,ArrayList<Goal>>();
 	
 	// Example: boxesByNode.get(node) gives an array of all boxes in 'node'
 	public Hashtable<Node,ArrayList<Box>>   boxesByNode  = new Hashtable<Node,ArrayList<Box>>();
 	public Hashtable<Node,ArrayList<Agent>> agentsByNode = new Hashtable<Node,ArrayList<Agent>>();
 	
 	public ArrayList<Agent> agents = new ArrayList<Agent>();
 	public ArrayList<Box> boxes = new ArrayList<Box>();
 	public ArrayList<Field> fields = new ArrayList<Field>();
 	public ArrayList<Goal> goals = new ArrayList<Goal>();
 
 	public ArrayList<Node> graph = new ArrayList<Node>();
 	public Level level;
 	
 	Random rand = new Random();
 	
 	public ArrayList<AreaNode> closingAreas = new ArrayList<AreaNode>();
 
 	
 	public TaskDispenser(Goal goal, Level level)
 	{
 		this.level = level;
 		goals.add(goal);
 		goal.td = this;
 		
 		// create graph from goal
 		GoalNode n = new GoalNode(goal);
 		graph.add(n);
 		GoalDFS(n);
 
 		fillDataStructures();
 	}
 	
 	
 	public LinkedList<Task> simpleDispenseTaskForAgent(Agent agent)
 	{
 		LinkedList<Task> taskQueue = new LinkedList<Task>();
 		// find goal
 		ArrayList<Goal> agentGoals = goalsByColor.get(agent.color.ordinal());
 		Goal chosenGoal = null;
 		for (int i=agentGoals.size()-1; i>=0; i--) {
 			if (!agentGoals.get(i).completed() && !agentGoals.get(i).reserved) {
 				chosenGoal = agentGoals.get(i);
 //				System.err.println("chosenGoal: "+chosenGoal.id);
 				break;
 			}
 		}
 		// if there are no goals, let agent wait
 		if (chosenGoal == null) {
 			taskQueue.add(new WaitTask(5));
 			return taskQueue;
 		}
 		
 		// find box for goal
 		ArrayList<Box> goalBoxes = boxesByID.get(chosenGoal.id);
 		Box box = null;
 		for (Box gb : goalBoxes) {
 //			System.err.println(gb.getId() + " is reserved " + gb.reserved);
 			if (!gb.reserved) {
 				box = gb;
 //				System.err.println("Reserving box " + box.id);
 				box.reserved = true;
 				break;
 			}
 		}
 
 		chosenGoal.reserved = true;
 		
 		// test
 		//System.err.println("chosenGoal "+chosenGoal);
 		/*for (Character ch : boxesByID.keySet()) 
 		{
 			ArrayList<Box> boxes = boxesByID.get(ch);
 			System.err.println(""+ch+": "+boxes.size());
 		}*/
 //		System.err.println("Agent is " + agent.getId());
 //		System.err.println("Box is " + box.getId());
 
 		// Move Task
 //		System.err.println("MOve task with box " + box.getId());
 		
 		if(box == null){ //Should probably not be needed
 			chosenGoal.reserved = false;
 			taskQueue.clear();
 			taskQueue.add(new WaitTask(1));
 			return taskQueue;
 		}
 
 //		// test
 //		//Box bgh = boxesByID.get('i').get(0);
 //		//System.err.println("Box I is at "+bgh.atField);
 		
 		MoveTask moveTask = new MoveTask(box.atField, box, chosenGoal);
 		moveTask.path = Pathfinding.AStar(level, agent, agent.atField, moveTask.moveTo);
 		moveTask.path.remove(moveTask.path.size()-1); moveTask.moveTo = moveTask.path.get(moveTask.path.size()-1);
 		moveTask.commandQueue = Pathfinding.commandsForAgentToField(agent, moveTask);
 		agent.taskQueue.add(moveTask);
 		
 		//System.err.println("is box I in path? "+moveTask.path.contains(bgh.atField));
 
 		
 		// Dock Task
 		DockTask dockTask = new DockTask(box, chosenGoal, null);
 		
 		Field agentTo = Pathfinding.findFreeNeighbour(dockTask.moveBoxTo);
 
 //		System.err.println("Dock with agent to " + agentTo.toString() + " box to " + dockTask.moveBoxTo);
 		
 		Object[] pathInfo = Pathfinding.findGoalRoute(level, agent, box, moveTask.moveTo, agentTo, box.atField, dockTask.moveBoxTo);
 		
 		dockTask.path = (ArrayList<Field>) pathInfo[1];//Pathfinding.AStar(level, agent, dockTask.box.atField, dockTask.moveBoxTo);
 		dockTask.commandQueue = (LinkedList<Command>) pathInfo[0];//Pathfinding.findGoalRoute(level, agent, box, moveTask.moveTo, agentTo, box.atField, dockTask.moveBoxTo);
 		
 
 		//commandsForAgentAndBoxToField(level, agent, box, moveTask.moveTo, dockTask.moveAgentTo, dockTask.moveBoxTo, null, dockTask);
 		agent.taskQueue.add(dockTask);
 		
 		// Give Wait Task
 		if (moveTask.commandQueue == null || dockTask.commandQueue == null) {
 			taskQueue.clear();
 			taskQueue.add(new WaitTask(1));
 
 			chosenGoal.reserved = false;
 			box.reserved = false;
 
 		}
 		
 		return taskQueue;
 	}
 	
 	public LinkedList<Task> newDispenseTaskForAgent(Agent agent)
 	{
 		//System.err.println("newDispenseTaskForAgent: "+agent.id);
 		
 		// if agent can help take out boxes from closing node it should
 		if (closingAreas.size() > 0) {
 			
 		}
 		
 		// Find goals that the agent can complete, are not already completed, or currently being completed
 		ArrayList<Goal> openGoals = new ArrayList<Goal>();
 		for (Goal goal : goalsByColor.get(agent.color.ordinal())) {
 			if (goal.reserved || goal.completed()) continue;
 			openGoals.add(goal);
 		}
 		
 		// Rank the goals by how near they are to the agent (BFS)
 		ArrayList<Goal> goalsByRank = new ArrayList<Goal>();
 		Queue<Field> queue = new LinkedList<Field>(); queue.add(agent.atField);
 		HashSet<Field> visited = new HashSet<Field>(); visited.add(agent.atField);
 		
 		Field current;
 		while (queue.size() != 0 && goalsByRank.size() != openGoals.size())
 		{
 			current = queue.poll();
 			if (current instanceof Goal) {
 				Goal goal = (Goal)current;
 				if (!goal.reserved && !goal.completed()) goalsByRank.add(goal);
 			}
 			for (Field neighbor : current.getNeighbors()) {
 				if (!visited.contains(neighbor)) {
 					visited.add(neighbor);
 					queue.add(neighbor);
 				}
 			}
 		}
 		
 		// Try goals from goalsByRank until we find a good candidate
 		for (Goal goal : goalsByRank)
 		{
 			// Dijkstra from goal finding the best/nearest box
 			Box box = Pathfinding.findBestBoxForGoal(goal, agent, level);
 			
 			// if we keep at most one section active
 			// then return a task to do this goal
 			// else try next goal
 			Object[] info = isGoalGoodForAgentAndBox(goal, agent, box);
 			boolean goalIsGood = (Boolean)info[0];
 			ArrayList<Box> boxesToMove = (ArrayList<Box>)info[1];
 			ArrayList<Node> agentEndNodes = (ArrayList<Node>)info[2];
 			
 			if (goalIsGood) {
 				//if (boxesToMove == null) {
 					LinkedList<Task> taskQueue = tasksForAgent(agent,goal,box,agentEndNodes);
 					return taskQueue;
 				//} else {
 					// find a way to move all boxesToMove
 					// CLOSE AREAS
 				//}
 			}
 		}
 		
 		
 		// if we get to this point, then we found no task for agent -> let it wait
 		Task task = new WaitTask(1);
 		LinkedList<Task> taskQueue = new LinkedList<Task>();
 		taskQueue.add(task);
 		return taskQueue;
 	}
 	
 	
 	public LinkedList<Task> tasksForAgent(Agent agent, Goal goal, Box box, ArrayList<Node> agentEndNodes)
 	{
 		//System.err.println("tasksForAgent | agent: "+agent.id+", goal: "+goal.id+", box: "+box.id);
 		
 		LinkedList<Task> taskQueue = new LinkedList<Task>();
 		
 		MoveTask moveTask = new MoveTask(box.atField, box, goal);
 		moveTask.path = Pathfinding.AStar(level, agent, agent.atField, moveTask.moveTo);
 		moveTask.path.remove(moveTask.path.size()-1); moveTask.moveTo = moveTask.path.get(moveTask.path.size()-1);
 		moveTask.commandQueue = Pathfinding.commandsForAgentToField(agent, moveTask);
 		taskQueue.add(moveTask);
 		
 		DockTask dockTask = new DockTask(box, goal, agentEndNodes);
 		dockTask.path = Pathfinding.AStar(level, agent, dockTask.box.atField, dockTask.moveBoxTo);
 		Object[] info = Utils.commandsForAgentAndBoxToField(level, agent, box, moveTask.moveTo, dockTask.moveAgentTo, dockTask.moveBoxTo, null, dockTask);
 		@SuppressWarnings("unchecked")
 		LinkedList<Command> cmdQueue = (LinkedList<Command>)info[0];
 		@SuppressWarnings("unchecked")
 		ArrayList<Field> fieldsUsed = (ArrayList<Field>)info[1];
 		dockTask.commandQueue = cmdQueue;
 		taskQueue.add(dockTask);
 		
 		// Check for boxes in the way
 		ArrayList<Box> boxesInTheWay = new ArrayList<Box>();
 		
 		// first the path from agent to box
 		for (Field field : moveTask.path) {
 			if (field.object != null && field.object instanceof Box) {
 				if (field.object != box && !boxesInTheWay.contains((Box)field.object)) {
 					boxesInTheWay.add((Box)field.object);
 				}
 			}
 		}
 		// then the path from box to goal
 		for (Field field : fieldsUsed) {
 			if (field.object != null && field.object instanceof Box) {
 				if (field.object != box && !boxesInTheWay.contains((Box)field.object)) {
 					boxesInTheWay.add((Box)field.object);
 				}
 			}
 		}
 		
 		// TEST
 		/*System.err.print("tasksForAgent | boxesInTheWay: ");
 		for (Box box2 : boxesInTheWay) System.err.print(box2.id);
 		System.err.println();
 		
 		System.err.print("fieldsUsed: ");
 		for (Field df : fieldsUsed) System.err.print(df+" ");
 		System.err.println();
 		// END TEST*/
 		
 		if (boxesInTheWay.size() > 0) {
 			HashSet<Field> pathFields = new HashSet<Field>();
 			pathFields.addAll(moveTask.path);
 			pathFields.addAll(fieldsUsed);
 			return tasksToClearWay(agent, goal, box, boxesInTheWay, pathFields);
 		}
 		
 		
 		//taskQueue.clear();
 		//taskQueue.add(new WaitTask(1));
 		return taskQueue;
 	}
 	
 
 
 	private LinkedList<Task> tasksToClearWay(Agent agent, Goal goal, Box box, ArrayList<Box> boxesInTheWay, HashSet<Field> pathFields)
 	{
 		ArrayList<Task> taskQueue = new ArrayList<Task>();
 		
 		// let the last box we have to move find the first field
 		HashSet<Field> reserved = new HashSet<Field>();
 		for (int i=boxesInTheWay.size()-1; i>=0; i--)
 		{
 			Box b = boxesInTheWay.get(i);
 			
 			// Do BFS from b
 			// ignoring the earlier boxes in boxesInTheWay
 			// and can't take field that is already reserved
 			LinkedList<Field> queue = new LinkedList<Field>();
 			queue.add(b.atField);
 			Field moveBoxTo = null;
 			while (queue.size() > 0) {
 				Field t = queue.poll();
 				for (Field field : t.neighbors) {
 					if (field==null) continue;
 					
 					if (field.object != null && field.object instanceof Box) {
 						Box bg = (Box)field.object;
 						int indexOfBox = boxesInTheWay.indexOf(bg);
 						if (indexOfBox == -1 || indexOfBox>i) {
 							continue;
 						}
 					}
 					if (!reserved.contains(field) && !pathFields.contains(field)) {
 						reserved.add(field);
 						moveBoxTo = field;
 						queue.clear();
 						break;
 					}
 					queue.add(field);
 				}
 			}
 			
 			
 			//System.err.println("Move box "+b.id+" to "+moveBoxTo);
 			
 			Task moveTask = new MoveTask(b.atField);
 			Task dockTask = new DockTask(b, moveBoxTo, null);
 			taskQueue.add(dockTask); taskQueue.add(moveTask);
 		}
 		Collections.reverse(taskQueue);
 		
 		// PLAN THE TASKS
 		for (int i=0; i<taskQueue.size(); i+=2)
 		{
 			Field agentAtField = null;
 			if (i==0) {
 				agentAtField = agent.atField;
 			} else {
 				agentAtField = ((DockTask)taskQueue.get(i-1)).agentWillBeMovedTo;
 			}
 			
 			MoveTask moveTask = (MoveTask)taskQueue.get(i);
 			DockTask dockTask = (DockTask)taskQueue.get(i+1);
 			
 			
 			// Move
 			moveTask.path = Pathfinding.AStar(level, agent, agentAtField, moveTask.moveTo); // could ignore some boxes
 			moveTask.path.remove(moveTask.path.size()-1); moveTask.moveTo = moveTask.path.get(moveTask.path.size()-1);
 			moveTask.commandQueue = Pathfinding.commandsForAgentToField(agent, moveTask);
 			
 			//System.err.println("moveTask.moveTo: "+moveTask.moveTo);
 			//System.err.println("dock box: "+dockTask.box.id);
 			
 			// Dock
 			dockTask.path = Pathfinding.AStar(level, agent, dockTask.box.atField, dockTask.moveBoxTo); // could ignore some boxes
 			Object[] info = Utils.commandsForAgentAndBoxToField(level, agent, dockTask.box, moveTask.moveTo, dockTask.moveAgentTo, dockTask.moveBoxTo, null, dockTask);
 			@SuppressWarnings("unchecked")
 			LinkedList<Command> cmdQueue = (LinkedList<Command>)info[0];
 			dockTask.commandQueue = cmdQueue;
 			dockTask.agentWillBeMovedTo = (Field)info[2];
 		}
 		
 		// end with move + dock for last goal
 		Field agentAtField = ((DockTask)taskQueue.get(taskQueue.size()-1)).agentWillBeMovedTo;
 		
 		MoveTask moveTask = new MoveTask(box.atField, box, goal);
 		moveTask.path = Pathfinding.AStar(level, agent, agentAtField, moveTask.moveTo);
 		moveTask.path.remove(moveTask.path.size()-1); moveTask.moveTo = moveTask.path.get(moveTask.path.size()-1);
 		moveTask.commandQueue = Pathfinding.commandsForAgentToField(agent, moveTask);
 		taskQueue.add(moveTask);
 		
 		DockTask dockTask = new DockTask(box, goal, null);
 		dockTask.path = Pathfinding.AStar(level, agent, dockTask.box.atField, dockTask.moveBoxTo);
 		Object[] info = Utils.commandsForAgentAndBoxToField(level, agent, box, moveTask.moveTo, dockTask.moveAgentTo, dockTask.moveBoxTo, null, dockTask);
 		@SuppressWarnings("unchecked")
 		LinkedList<Command> cmdQueue = (LinkedList<Command>)info[0];
 		dockTask.commandQueue = cmdQueue;
 		taskQueue.add(dockTask);
 		
 		return new LinkedList<Task>(taskQueue);
 	}
 
 
 	private Object[] isGoalGoodForAgentAndBox(Goal goal, Agent agent, Box box)
 	{
 		GoalNode gn = (GoalNode)goal.node;
 		
 		// BFS for all new graphs created by taking away goal.
 		// Usually a fast solution, but in graphs with _many_ nodes can be bad
 		Queue<Node> queue = new LinkedList<Node>();
 		HashSet<Node> allVisited = new HashSet<Node>(); allVisited.add(gn); // we don't want to traverse through original node
 		ArrayList<HashSet<Node>> graphs = new ArrayList<HashSet<Node>>();
 		
 		Iterator<Node> iterator = gn.neighbors.iterator();
 		
 		while (iterator.hasNext())
 		{
 			Node next = iterator.next();
			//if (goal.id == 'd' && next instanceof GoalNode) System.err.println("r: found goal " +((Goal)node).goal.id);
 			if (allVisited.contains(next)) continue;
 			allVisited.add(next);
 			if (next instanceof GoalNode) {
 				Goal g = ((GoalNode)next).goal;
 				if (g.completed()) continue;
 			}
 			queue.add(next);
 			
 			HashSet<Node> visited = new HashSet<Node>();
 			graphs.add(visited);
			visited.add(next); // TODO
 			
 			while (queue.size() > 0) {
 				Node n = queue.poll();
 				for (Node neighbor : n.neighbors) {
 					if (!allVisited.contains(neighbor))
 					{
 						allVisited.add(neighbor);
 						if (neighbor instanceof GoalNode) {
 							Goal g = ((GoalNode)neighbor).goal;
 							if (g.completed()) continue;
 						}
 						visited.add(neighbor);
 						queue.add(neighbor);
 					}
 				}
 			}
 		}
 		
 		// if, at this point, there was only 1 graph found from goal, then this is a good goal as it doesn't disconnect
 		if (graphs.size() == 1) {
 			return new Object[]{new Boolean(true),null,null};
 		}
 		
 		
 		// 'graphs' now contains 2-4 graphs (sets of nodes), all disconnected from each other by the goal
 		
 		// if we're already closing an area, then we should not close another area
 		if (closingAreas.size() > 0) {
 			return new Object[]{new Boolean(false),null,null};
 		}
 		
 		// Gather data from new graphs     // also agents?
 		ArrayList<HashSet<Goal>> incompleteGoalsInGraphs = new ArrayList<HashSet<Goal>>();
 		ArrayList<HashSet<Box>>  unusedBoxesInGraphs = new ArrayList<HashSet<Box>>();
 		for (HashSet<Node> graph : graphs)
 		{
 			HashSet<Goal> goalInGraph = new HashSet<Goal>();
 			HashSet<Box>  boxInGraph  = new HashSet<Box>();
 			incompleteGoalsInGraphs.add(goalInGraph);
 			unusedBoxesInGraphs.add(boxInGraph);
 			
 			for (Node node : graph) {
 				if (node instanceof GoalNode) {
 					GoalNode goalNode = (GoalNode)node;
					//if (goal.id == 'd') System.err.println("r: found goal " +goalNode.goal.id);  // TODO
 					if (!goalNode.goal.completed()) {
 						boxInGraph.addAll(boxesByNode.get(goalNode));
 						goalInGraph.add(goalNode.goal);
 					}
 				} else { // AreaNode
 					boxInGraph.addAll(boxesByNode.get(node));
 				}
 			}
 			boxInGraph.remove(box); // we don't care about the box we're going to move to a goal
 		}
 		
 		int graphsWithGoals = 0;
 		int indexOfMainGraph = -1; // the graph we don't 'close'
 		for (int i=0; i<incompleteGoalsInGraphs.size(); i++) {
 			HashSet<Goal> igig = incompleteGoalsInGraphs.get(i);
 			if (igig.size() > 0) {
 				graphsWithGoals++;
 				indexOfMainGraph = i;
 				
 			}
 		}
 		// if there is no main graph, then the goal is the last one and it's ok to complete
 		if (indexOfMainGraph == -1) return new Object[]{new Boolean(true),null,null};
 		
 		if (graphsWithGoals <= 1) {
 			HashSet<Node> mainGraph = graphs.get(indexOfMainGraph);
 			HashSet<Goal> incompleteGoalsInMainGraph = incompleteGoalsInGraphs.get(indexOfMainGraph);
 			
 			// Find all unused boxes in closing graphs/areas
 			HashSet<Box> unusedBoxesInClosingAreas = new HashSet<Box>();
 			for (int i=0; i<graphs.size(); i++)
 			{
 				if (i == indexOfMainGraph) continue; // skip main graph
 				unusedBoxesInClosingAreas.addAll(unusedBoxesInGraphs.get(i));
 			}
 			
 			// what boxes from the closing graph(s) should be moved to main graph?
 			ArrayList<Box> boxesWeNeedToMoveOutOfClosingArea = new ArrayList<Box>();
 			for (Goal iGoal : incompleteGoalsInMainGraph)
 			{
 				for (Box uBox : unusedBoxesInClosingAreas) {
 					if (uBox.id == iGoal.id) {
 						boxesWeNeedToMoveOutOfClosingArea.add(uBox);
 						break;
 					}
 				}
 			}
 			
 			// make sure we have enough space in main graph. This approach is a bit naive, as the fields may be in a corridor
 			// get size of all area nodes
 			int space = 0;
 			for (Node node : mainGraph) {
 				if (node instanceof AreaNode) {
 					AreaNode areaNode = (AreaNode)node;
 					space += areaNode.fields.size() - boxesByNode.get(areaNode).size();
 				}
 			}
 			
 			// find neighbor node of goal node that agent should end in
 			ArrayList<Node> agentEndNodes = new ArrayList<Node>();
 			for (Node node : gn.neighbors) {
 				if (mainGraph.contains(node)) agentEndNodes.add(node);
 			}
 			//System.err.println("size of agentEndNodes: "+agentEndNodes.size());
 			if (agentEndNodes.size() == 0) agentEndNodes = null;
 			
			
 			// if we have enough space
 			if (space > boxesWeNeedToMoveOutOfClosingArea.size()*3) {
				return new Object[]{new Boolean(true),boxesWeNeedToMoveOutOfClosingArea,agentEndNodes};
 			} else {
 				return new Object[]{new Boolean(false),null,null};
 			}
 		}
 		
 		// goal splits graph with incomplete goals on more than one side
 		return new Object[]{new Boolean(false),null,null};
 	}
 	
 	
 	public LinkedList<Task> dispenseTaskForAgent(Agent agent)
 	{
 		// Find goals that the agent can complete, are not already completed, or currently being completed
 		ArrayList<Goal> openGoals = new ArrayList<Goal>();
 		for (Goal goal : goalsByColor.get(agent.color.ordinal())) {
 			if (goal.reserved || goal.completed()) continue;
 			openGoals.add(goal);
 		}
 		
 		
 		
 		// Give all openGoals a rating
 		// factors:
 		// few edges is good (disconnects less)
 		// for all (nodes with 1 edge) we pick the fastest to complete from agent.. also have to pick box...
 		// with >1 edge we have to check if we disconnect anything important
 		
 		// Try to find goal nodes with only 1 edge. These always have priority over nodes with more edges
 		ArrayList<GoalNode> desiredGoalNodes = new ArrayList<GoalNode>();
 		for (Goal goal : openGoals) {
 			GoalNode gn = (GoalNode)goal.node;
 			if (gn.neighbors.size() == 1) desiredGoalNodes.add(gn);
 		}
 		
 		// If we don't find any one edged goal nodes, we try to find ones that do not disconnect vital parts
 		if (desiredGoalNodes.size() == 0)
 		{
 			for (Goal goal : openGoals)
 			{
 				GoalNode gn = (GoalNode)goal.node;
 				
 				// BFS for all new graphs created by taking away goal.
 				// Usually a fast solution, but in graphs with _many_ nodes can be bad
 				Queue<Node> queue = new LinkedList<Node>();
 				HashSet<Node> allVisited = new HashSet<Node>(); allVisited.add(gn); // we don't want to traverse through original node
 				ArrayList<HashSet<Node>> graphs = new ArrayList<HashSet<Node>>();
 				
 				Iterator<Node> iterator = gn.neighbors.iterator();
 				
 				while (allVisited.size() != graph.size())
 				{
 					Node next = iterator.next();
 					if (allVisited.contains(next)) continue;
 					allVisited.add(next);
 					queue.add(next);
 					
 					HashSet<Node> visited = new HashSet<Node>();
 					graphs.add(visited);
 					
 					while (queue.size() > 0) {
 						Node n = queue.poll();
 						for (Node neighbor : n.neighbors) {
 							if (!allVisited.contains(neighbor)) {
 								allVisited.add(neighbor);
 								visited.add(neighbor);
 								queue.add(neighbor);
 							}
 						}
 					}
 				}
 				
 				// if, at this point, the first BFS found all nodes, then this is a good goal as it doesn't disconnect
 				if (graphs.get(0).size() == graph.size()-1) {
 					desiredGoalNodes.add(gn);
 					continue;
 				}
 				
 				
 				// 'graphs' now contains 2-4 sets of nodes, all disconnected from each other by the goal
 				
 				// Gather data from new graphs     // also agents?
 				ArrayList<HashSet<Goal>> incompleteGoalsInGraphs = new ArrayList<HashSet<Goal>>();
 				ArrayList<HashSet<Box>>  unusedBoxesInGraphs = new ArrayList<HashSet<Box>>();
 				for (HashSet<Node> graph : graphs)
 				{
 					HashSet<Goal> goalInGraph = new HashSet<Goal>();
 					HashSet<Box>  boxInGraph  = new HashSet<Box>();
 					incompleteGoalsInGraphs.add(goalInGraph);
 					unusedBoxesInGraphs.add(boxInGraph);
 					
 					for (Node node : graph) {
 						if (node instanceof GoalNode) {
 							GoalNode goalNode = (GoalNode)node;
 							if (!goalNode.goal.completed()) {
 								boxInGraph.addAll(boxesByNode.get(goalNode));
 								goalInGraph.add(goalNode.goal);
 							}
 						} else { // AreaNode
 							boxInGraph.addAll(boxesByNode.get(node));
 						}
 					}
 				}
 				
 				int graphsWithGoals = 0;
 				int indexOfMainGraph = -1; // the graph we don't 'close'
 				for (int i=0; i<incompleteGoalsInGraphs.size(); i++) {
 					HashSet<Goal> igig = incompleteGoalsInGraphs.get(i);
 					if (igig.size() > 0) {
 						graphsWithGoals++;
 						indexOfMainGraph = i;
 						
 					}
 				}
 				if (graphsWithGoals <= 1) {
 					desiredGoalNodes.add(gn);  // how do we signal what nodes/graph we need the agent to end at?
 				} else {
 					// goal splits graph with incomplete goals on more than one side
 					continue;
 				}
 				
 				// TODO check that the 'main graph' has all the boxes it needs.
 				
 				
 				
 			}
 		}
 		
 		// Estimate how fast/hard they are to solve by agent
 		if (desiredGoalNodes.size() == 0)
 		{
 			// use AStar...
 		}
 		
 		// Return task
 		if (desiredGoalNodes.size() == 0) {
 			Task task = new WaitTask(5);
 			LinkedList<Task> taskQueue = new LinkedList<Task>();
 			taskQueue.add(task);
 			return taskQueue;
 		} else {
 			//return new TaskMove() + TaskDock()
 		}
 		
 		//Task test = new TaskWait(5);
 		return null;
 	}
 	
 
 	private void GoalDFS(GoalNode n) 
 	{
 		for (Field f : n.goal.neighbors) {
 			if (f==null) continue;
 			if (f.node == null) {
 				// we haven't seen this field before so we call DFS on it
 				if (f instanceof Goal) {
 					Goal g = (Goal)f;
 					goals.add(g); g.td = this;
 					GoalNode m = new GoalNode(g); graph.add(m);
 					GoalDFS(m);
 				} else { // Field
 					fields.add(f); f.td = this;
 					AreaNode m = new AreaNode(f); graph.add(m);
 					AreaDFS(m);
 				}
 			} else {
 				// already explored so we just link nodes
 				f.node.neighbors.add(n);
 				n.neighbors.add(f.node);
 			}
 		}
 	}
 	
 	private void AreaDFS(AreaNode n)
 	{
 		ArrayList<Goal> neighborGoals = new ArrayList<Goal>();
 		
 		// BFS adding all fields to AreaNode
 		Queue<Field> queue = new LinkedList<Field>();
 		queue.add(n.fields.get(0));
 		while (queue.size() > 0) {
 			Field t = queue.poll();
 			for (Field field : t.neighbors) {
 				if (field==null) continue;
 				if (field instanceof Goal) {     // goals we explore later
 					neighborGoals.add((Goal)field);
 				} else if (field.node == null) { // add all unexplored neighbors to queue
 					queue.add(field);
 					fields.add(field);
 					field.td = this;
 					n.addField(field);
 				}
 			}
 		}
 		
 		// Explore found goals
 		for (Goal g : neighborGoals) {
 			if (g.node == null) {
 				// we haven't seen this goal before so we call DFS on it
 				goals.add(g); g.td = this;
 				GoalNode m = new GoalNode(g); graph.add(m);
 				GoalDFS(m);
 			} else {
 				// already explored so we just link nodes
 				g.node.neighbors.add(n);
 				n.neighbors.add(g.node);
 			}
 		}
 	}
 
 	private void fillDataStructures()
 	{
 		// add agents + boxes to this task dispenser
 		for (Field field : fields) {
 			if (field.object != null) {
 				Object o = field.object;
 				if (o instanceof Agent) {
 					agents.add((Agent)o);
 					((Agent) o).td = this;
 				} else { // Box
 					boxes.add((Box)o);
 					((Box) o).td = this;
 				}
 			}
 		}
 		
 		
 		// sort boxes and agents by color
 		for (int i=0; i<8; i++) {
 			boxesByColor.add(new ArrayList<Box>());
 			agentsByColor.add(new ArrayList<Agent>());
 			goalsByColor.add(new ArrayList<Goal>());
 		}
 		for (Box box : boxes) {
 			boxesByColor.get(box.color.ordinal()).add(box);
 		}
 		for (Agent agent : agents) {
 			agentsByColor.get(agent.color.ordinal()).add(agent);
 		}
 		for (Goal goal : goals) {
 			goalsByColor.get(goal.color.ordinal()).add(goal);
 		}
 
 		
 		// sort boxes and goals by ID
 		for (Box box : boxes) {
 			if (boxesByID.get(box.id) == null) {
 				boxesByID.put(box.id,new ArrayList<Box>());
 			}
 			boxesByID.get(box.id).add(box);
 		}
 		for (Goal goal : goals) {
 			if (goalsByID.get(goal.id) == null) {
 				goalsByID.put(goal.id,new ArrayList<Goal>());
 			}
 			goalsByID.get(goal.id).add(goal);
 		}
 
 		
 		// sort boxes and agents by Node
 		for (Node node : graph) {
 			boxesByNode.put(node, new ArrayList<Box>());
 			agentsByNode.put(node,new ArrayList<Agent>());
 		}
 		for (Box box : boxes) {
 			Node node = box.atField.node;
 			boxesByNode.get(node).add(box);
 		}
 		for (Agent agent : agents) {
 			Node node = agent.atField.node;
 			agentsByNode.get(node).add(agent);
 		}
 	}
 
 
 	public static abstract class Node
 	{
 		HashSet<Node> neighbors = new HashSet<Node>(); //ArrayList<Node> neighbors = new ArrayList<Node>();
 		//boolean open = true; // used to close off areas and minimize search space ?
 		
 		public String toString() {
 			String string = "";
 			for (Node n : neighbors) {
 				string += n.shortName()+" ";
 			}
 			return "Neighbours: "+string;
 		}
 		public String shortName() {return "";}
 	}
 	
 	public static class GoalNode extends Node
 	{
 		Goal goal;
 
 		public GoalNode(Goal goal){
 			this.goal = goal;
 			goal.node = this;
 		}
 		
 		public String toString() {
 			return "GoalNode: "+goal.id+", "+super.toString();
 		}
 		public String shortName() {return ""+goal;}
 	}
 	
 	public static class AreaNode extends Node
 	{
 		ArrayList<Field> fields = new ArrayList<Field>();
 
 		public AreaNode(Field field) {
 			fields.add(field);
 			field.node = this;
 		}
 		
 		public void addField(Field field) {
 			fields.add(field);
 			field.node = this;
 		}
 		
 		public String toString() {
 			String string = "";
 			for (Field field : fields) {
 				string += field+" ";
 			}
 			return "AreaNode: "+string+", "+super.toString();
 		}
 		public String shortName() {return ""+fields.get(0);}
 	}
 	
 	
 	public static abstract class Task
 	{
 		public ArrayList<Field> path; // path we want task follow. E.g. move along path, or move box along path. Path is usually given by TD, or another agent that needs your help
 		public LinkedList<Command> commandQueue = new LinkedList<Command>(); // cmds that follow path
 		public boolean highPriority = false; // true means it was given by another agent, and thus has higher priority than anything else
 	}
 	
 	public static class WaitTask extends Task
 	{
 		int waitSteps;
 		
 		public WaitTask(int waitSteps) {
 			this.waitSteps = waitSteps;
 			for (int i=0; i<waitSteps; i++) {
 				commandQueue.add(null);
 			}
 		}
 	}
 	
 	public static class MoveTask extends Task
 	{
 		public Field moveTo;
 		public Box box;
 		public Field goal;
 		
 		public MoveTask(Field moveTo) {
 			this.moveTo = moveTo;
 		}
 		
 		public MoveTask(Field moveTo, Box b, Field futureGoal) {
 			this.moveTo = moveTo;
 			this.box = b;
 			this.goal = futureGoal;
 		}
 	}
 	
 	public static class DockTask extends Task
 	{
 		public Box box;
 		public Field moveBoxTo;
 		public ArrayList<Node> moveAgentTo; // may be null if it's not important
 		public Field agentWillBeMovedTo;		
 		
 		public DockTask(Box box, Field moveBoxTo, ArrayList<Node> moveAgentTo) { // pass null if agent pos doesn't matter
 			this.box = box;
 			this.moveBoxTo = moveBoxTo;
 			this.moveAgentTo = moveAgentTo;
 		}
 	}
 	
 	
 	public String toString() {
 		String string = 
 		"Boxes:  "+boxes.size() +
 		"\nAgents: "+agents.size() +
 		"\nGoals:  "+goals.size() +
 		"\nFields: "+fields.size() +
 		"\nboxesByID: "+goalsByID.size() +
 		"\n\n";
 		
 		for (Node node : graph) {
 			string += node+"\n";
 		}
 		return string;
 	}
 }
