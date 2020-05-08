 package template;
 
 /* import table */
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.PriorityQueue;
 
 import logist.simulation.Vehicle;
 import logist.agent.Agent;
 import logist.behavior.DeliberativeBehavior;
 import logist.plan.Plan;
 import logist.task.Task;
 import logist.task.TaskDistribution;
 import logist.task.TaskSet;
 import logist.topology.Topology;
 import logist.topology.Topology.City;
 
 /**
  * An optimal planner for one vehicle.
  */
 public class DeliberativeTemplate implements DeliberativeBehavior {
 
 	enum Algorithm { BFS, ASTAR }
 
 	/* Environment */
 	Topology topology;
 	TaskDistribution td;
 
 	/* the properties of the agent */
 	Agent agent;
 	int capacity;
 
 	private static final int INITSTATE = 0;
 	private static final int PICKEDUP = 1;
 	private static final int DELIVERED = 2;
 
 	TaskSet carriedTasks;			//used to save carried task when i have to recompute the plan
 	ArrayList<ArrayList<Object>> startState= new ArrayList<ArrayList<Object>>();
 	ArrayList<ArrayList<Object>> goalState= new ArrayList<ArrayList<Object>>();
 	/* the planning class */
 	Algorithm algorithm;
 
 	public void setup(Topology topology, TaskDistribution td, Agent agent) {
 		this.topology = topology;
 		this.td = td;
 		this.agent = agent;
 		// initialize the planner
 		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
 		// Throws IllegalArgumentException if algorithm is unknown
 		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
 	}
 
 	public Plan plan(Vehicle vehicle, TaskSet tasks) {
 		Plan plan = null;
 		planeNode goalNode;
 		planeNode currentNode;
 
 		System.out.println( vehicle.name() +" is computing plan");
 
 		generateStartGoalNode(tasks);
 		carriedTasks = null;
 		System.out.println(startState);
 		System.out.println(goalState);
 
 		planeNode startNode = createStartNode(vehicle, startState, tasks);
 		ArrayList<ArrayList<Object>> currentState = startState;
 		// Compute the plan with the selected algorithm.
 
 		switch (algorithm) {
 		case ASTAR:
 			System.out.println("ASTAR");
 			Comparator<planeNode> comparator = new planNodeComparator();
 			PriorityQueue<planeNode> nodeQueue = new PriorityQueue<planeNode> (1000, comparator);
 			ArrayList<planeNode> visitedNodes = new ArrayList<planeNode>();
 			currentNode = startNode;
 			int i = 0;
 			while(!checkGoalState(currentState)) {
 				ArrayList<planeNode> childQueue = currentNode.expandNodes();
 				nodeQueue.addAll(childQueue);
				System.out.println("*****"+ nodeQueue.size());
 				try{
 					currentNode = nodeQueue.remove();
 					System.out.println(currentNode.getState());
 					System.out.println("Estimated total cost: "+currentNode.getCosts()+planNodeComparator.getHeuristicCost(currentNode.getState(), vehicle.costPerKm(), currentNode.getCapacity(), currentNode.getCity()));
 				} catch (Exception e) {
 					break;
 				}
 				
 				visitedNodes.add(currentNode);
 				currentState = currentNode.getState();
 				i++;
 			}
 			System.out.println("Iteration: "+i);
 			
 			if(nodeQueue.size() > 0) {
 				System.out.println("ASTAR: GOAL NODE REACHED!");
 				goalNode = currentNode;
 				
 				// Do backtracking from goal node and create plan
 				plan = backtrackingPlan(goalNode);
 			} else {
 				System.out.println("Node Queue is empty and we haven't found a solution");
 			}
 
 			break;
 
 		case BFS:
 			System.out.println("BFS");
 			currentNode = startNode;
 			ArrayList<planeNode> nodeQueueList = new ArrayList<planeNode>();
 			i = 0;
 			while(!checkGoalState(currentState)) {
 				ArrayList<planeNode> Queue = currentNode.expandNodes();
 				nodeQueueList.addAll(Queue);
 				currentNode = nodeQueueList.remove(0);
 				currentNode.printState();
 				currentState = currentNode.getState();
 				i++;	
 			}
 			System.out.println("Iteration: "+ i);
 			System.out.println("BFS: GOAL NODE REACHED!");
 			goalNode = currentNode;
 			plan = backtrackingPlan(goalNode);
 			break;
 		default:
 			throw new AssertionError("Should not happen!!!");
 		}		
 		return plan;
 	}
 
 	public void planCancelled(TaskSet _carriedTasks) {
 		if (!_carriedTasks.isEmpty()) {
 			carriedTasks = _carriedTasks;
 		}
 		else{
 			carriedTasks = _carriedTasks;
 		}
 	}
 
 	public static boolean checkGoalState(ArrayList<ArrayList<Object>> state) {
 		int stateSize = state.size();
 		for(int i = 0; i < stateSize; i++) {
 			if(!state.get(i).get(1).equals(DELIVERED)) {
 				return false;
 			}
 		}
 		return true;
 	}
 	public void generateStartGoalNode(TaskSet tasks){
 		startState.clear();
 		goalState.clear();
 		Iterator<Task> itr = tasks.iterator();
 		while(itr.hasNext()){			
 			startState.add(new ArrayList<Object>());
 			goalState.add(new ArrayList<Object>());
 
 			Task currentTask = itr.next();
 			startState.get(startState.size()-1).add(currentTask);
 			startState.get(startState.size()-1).add(INITSTATE);
 
 			goalState.get(goalState.size()-1).add(currentTask);
 			goalState.get(goalState.size()-1).add(DELIVERED);
 		}
 
 		if(carriedTasks != null){
 			Iterator<Task> c_itr= carriedTasks.iterator();
 			while(c_itr.hasNext()){
 				startState.add(new ArrayList<Object>());
 				goalState.add(new ArrayList<Object>());
 				
 				Task currentTask = c_itr.next();
 				goalState.get(goalState.size()-1).add(currentTask);
 				goalState.get(goalState.size()-1).add(DELIVERED);
 				startState.get(startState.size()-1).add(currentTask);
 				startState.get(startState.size()-1).add(PICKEDUP);
 			}
 		}
 	}
 
 	private planeNode createStartNode(Vehicle vehicle, ArrayList<ArrayList<Object>> startState, TaskSet tasks) {
 		ArrayList<ArrayList<Object>> stateHash= new ArrayList<ArrayList<Object>>();
 		planeNode startNode = new planeNode(vehicle, vehicle.getCurrentCity(), startState, vehicle.capacity(), 0, null, stateHash, algorithm.name());
 		return startNode;
 	}
 
 	private Plan backtrackingPlan(planeNode goalNode) {
 		planeNode currentNode = goalNode;
 		ArrayList<planeNode> path = new ArrayList<planeNode>();
 
 		while(currentNode != null) {
 			path.add(currentNode);
 			currentNode = currentNode.getParent();
 		}
 		
 		Collections.reverse(path);
 		Plan optimalPlan = new Plan(path.get(0).getCity());
 		for(int i = 1; i < path.size() ; i++) {
 			for(City city : path.get(i-1).getCity().pathTo(path.get(i).getCity())){
 				optimalPlan.appendMove(city);
 			}
 			int stateSize = path.get(i).getState().size();
 			for(int j=0; j< stateSize; j++) {
 				if(!path.get(i).getState().get(j).get(1).equals(path.get(i-1).getState().get(j).get(1))) {
 					Object action = path.get(i).getState().get(j).get(1);			// switch pickup or delivery
 					Task currentTask = (Task) path.get(i).getState().get(j).get(0);
 
 					switch((Integer)action) {
 					case PICKEDUP:
 						optimalPlan.appendPickup(currentTask);
 						break;
 					case DELIVERED:
 						optimalPlan.appendDelivery(currentTask);
 						break;
 					default:
 						System.out.println("_OO_");
 					}
 				}
 			}
 		}
 		return optimalPlan;
 	}
 }
