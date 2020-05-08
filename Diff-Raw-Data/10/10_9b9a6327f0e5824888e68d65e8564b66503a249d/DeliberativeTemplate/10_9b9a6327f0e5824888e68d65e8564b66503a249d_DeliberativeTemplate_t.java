 package template;
 
 /* import table */
 import java.util.ArrayList;
 import java.util.Iterator;
 
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
 
 	ArrayList<ArrayList<Object>> startState= new ArrayList<ArrayList<Object>>();
 	ArrayList<ArrayList<Object>> goalState= new ArrayList<ArrayList<Object>>();
 	/* the planning class */
 	Algorithm algorithm;
 	
 	@Override
 	public void setup(Topology topology, TaskDistribution td, Agent agent) {
 		this.topology = topology;
 		this.td = td;
 		this.agent = agent;
 
 		// initialize the planner
 		int capacity = agent.vehicles().get(0).capacity();
 		String algorithmName = agent.readProperty("algorithm", String.class, "ASTAR");
 		
 		// Throws IllegalArgumentException if algorithm is unknown
 		algorithm = Algorithm.valueOf(algorithmName.toUpperCase());
 		
 		// ...
 	}
 	
 	@Override
 	public Plan plan(Vehicle vehicle, TaskSet tasks) {
 		Plan plan;
 		
 		
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
 		
 		// Compute the plan with the selected algorithm.
 		switch (algorithm) {
 		case ASTAR:
 			// ...
 			plan = naivePlan(vehicle, tasks);
 			break;
 		case BFS:
 			// ...
 			plan = naivePlan(vehicle, tasks);
 			break;
 		default:
 			throw new AssertionError("Should not happen.");
 		}		
 		return plan;
 	}
 	
 	private Plan naivePlan(Vehicle vehicle, TaskSet tasks) {
 		City current = vehicle.getCurrentCity();
 		Plan plan = new Plan(current);
 
 		for (Task task : tasks) {
 			// move: current city => pickup location
 			for (City city : current.pathTo(task.pickupCity))
 				plan.appendMove(city);
 
 			plan.appendPickup(task);
 
 			// move: pickup location => delivery location
 			for (City city : task.path())
 				plan.appendMove(city);
 
 			plan.appendDelivery(task);
 
 			// set current city
 			current = task.deliveryCity;
 		}
 		return plan;
 	}
 
 	@Override
 	public void planCancelled(TaskSet carriedTasks) {
 		
 		if (!carriedTasks.isEmpty()) {
 			// This cannot happen for this simple agent, but typically
 			// you will need to consider the carriedTasks when the next
 			// plan is computed.
 		}
 	}
 }
