 package chris;
 
 import java.util.ArrayList;
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
			if (!agentGoals.get(i).completed()) {
 				chosenGoal = agentGoals.get(i);
 				System.err.println("chosenGoal: "+chosenGoal.id);
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
 		/*System.err.println("chosenGoal "+chosenGoal);
 		for (Character ch : boxesByID.keySet()) 
 		{
 			ArrayList<Box> boxes = boxesByID.get(ch);
 			System.err.println(""+ch+": "+boxes.size());
 		}*/
 //		System.err.println("Agent is " + agent.getId());
 //		System.err.println("Box is " + box.getId());
 
 		// Move Task
 //		System.err.println("MOve task with box " + box.getId());
 		
 //<<<<<<< HEAD
 		if(box == null){ //Should probably not be needed
 			chosenGoal.reserved = false;
 			taskQueue.clear();
 			taskQueue.add(new WaitTask(1));
 			return taskQueue;
 		}
 //=======
 //		// test
 //		//Box bgh = boxesByID.get('i').get(0);
 //		//System.err.println("Box I is at "+bgh.atField);
 //>>>>>>> 84652967095663cf0438c11b1ae89d18a4f7df58
 		
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
