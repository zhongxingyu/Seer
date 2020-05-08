 package template;
 
 /* import table */
 import java.util.ArrayList;
 
 import logist.simulation.Vehicle;
 import logist.task.Task;
 import logist.topology.Topology.City;
 
 public class planeNode {
 
 	private planeNode parent;
 	private ArrayList<planeNode> children;
 	private ArrayList<ArrayList<Object>> nodeState;
 	private int capacity;
 	private double costs;
 	private Vehicle vehicle;
 	private City nodeCity;
 	private ArrayList<ArrayList<Object>> hashTable;
 	private enum Algorithm { BFS, ASTAR };
 	private Algorithm algorithm;
 
 	private static final int INITSTATE = 0;
 	private static final int PICKEDUP = 1;
 	private static final int DELIVERED = 2;
 
 	/**
 	 * Constructor of a planeNode
 	 * 
 	 * @param _vehicle
 	 * @param _nodeCity
 	 * @param _nodeState
 	 * @param _capacity
 	 * @param _costs
 	 * @param _parent
 	 * @param _hashTable
 	 * @param alg
 	 */
 	public planeNode(Vehicle _vehicle, City _nodeCity, ArrayList<ArrayList<Object>> _nodeState, int _capacity, double _costs, planeNode _parent, ArrayList<ArrayList<Object>> _hashTable, String alg){
 		algorithm= Algorithm.valueOf(alg.toUpperCase());
 		hashTable= _hashTable;
 		nodeState = _nodeState;
 		vehicle = _vehicle;
 		capacity = _capacity;
 		costs = _costs;
 		parent = _parent;
 		nodeCity = _nodeCity;
 		children= new ArrayList<planeNode>();
 	}
 
 	/**
 	 * Used for Breath first search: We don't care about the best solution,
 	 * Therefore, if we arrive at a state, where the same tasks are delivered and picked up,
 	 * we don't expand the node anymore.
 	 * 
 	 * @param state
 	 * @param newCost
 	 * @return true / false
 	 */
 	public boolean computeHash(ArrayList<ArrayList<Object>> state, double newCost){
 		int hashCode = state.hashCode();
 		boolean present = false;
 		boolean shouldAddNode = true;
 
 		for(int i=0; i< hashTable.size(); i++){
 			if(hashTable.get(i).get(0).equals(hashCode)){
 				present = true;
 				if(algorithm.equals(Algorithm.BFS)) {
 					shouldAddNode = false;
 				}
 			}
 		}	
 		if(present == false) {
 			hashTable.add(new ArrayList<Object>());
 			hashTable.get(hashTable.size()-1).add(state.hashCode());
 			hashTable.get(hashTable.size()-1).add(this);
 		}
 
 		return shouldAddNode;
 	}
 
 	/**
 	 * Expands the current node
 	 * 
 	 * @return ArrayList<planeNode> with all its children nodes
 	 */
 	public ArrayList<planeNode> expandNodes() {
 		ArrayList<planeNode> childNodes = new ArrayList<planeNode>();
 		ArrayList<Integer> subState = createSubState(nodeState);
 
 		for (int i=0; i< subState.size(); i++){
 			planeNode newState = createNewState(subState.get(i));
 			if(newState != null){
 				childNodes.add(newState);
 			}
 		}
 
 		return childNodes;
 	}
 
 	/**
 	 * All tasks, that are on the current city (vehicle position)
 	 * and haven't been moved yet, are extracted from the currentState
 	 * into a subState. Based on the substate, we calculate the possible
 	 * next global states
 	 * 
 	 * @param currentState
 	 * @return subState
 	 */
 
 	/**
 	 * Computes all the possible children states of the current node
 	 * 
 	 * @param currentState
 	 * @return ArrayList<Integer>
 	 */
 	private ArrayList<Integer> createSubState(ArrayList<ArrayList<Object>> currentState) {
 		ArrayList<Integer> subState = new ArrayList<Integer>();
 
 		for(int i = 0; i < currentState.size(); i++) {
 			Integer pos = new Integer(i);
 			if(currentState.get(i).get(1).equals(INITSTATE) || currentState.get(i).get(1).equals(PICKEDUP)) {
 				subState.add(pos);
 			}
 		}
 
 		return subState;
 	}
 
 	/**
 	 * Creates a new state
 	 * 
 	 * @param selectedTaskIndex
 	 * @return
 	 */
 	private planeNode createNewState(Integer selectedTaskIndex) {
 		ArrayList<ArrayList<Object>> newState= new ArrayList<ArrayList<Object>>();
 		planeNode child = null;
 		ArrayList<Object> currentTaskNode = nodeState.get(selectedTaskIndex);
 
 		for(int i=0; i<nodeState.size(); i++){
 			newState.add(new ArrayList<Object>());
 			newState.get(i).add(nodeState.get(i).get(0));
 			newState.get(i).add(nodeState.get(i).get(1));			
 		}
 
 		if(currentTaskNode.get(1).equals(PICKEDUP)) {		 //selected task is PICKEDUP
 			child = calculateNewStateParameters(newState.get(selectedTaskIndex), newState);
 		} else if(capacity >= ((Task) currentTaskNode.get(0)).weight) {
 			child = calculateNewStateParameters(newState.get(selectedTaskIndex), newState);
 		}
 		return child;
 	}
 	/**
 	 * Generates a new child based on the newState and the node we modify.
 	 * 
 	 * @param currentTaskNode
 	 * @param newState
 	 * @return planNode
 	 */
 
 	private planeNode calculateNewStateParameters(ArrayList<Object> currentTaskNode, ArrayList<ArrayList<Object>> newState) {
 		planeNode child = null;
 		Task currentTaskNodeTask = (Task) currentTaskNode.get(0);
 		double newCost = calculateCost(currentTaskNode, currentTaskNodeTask, currentTaskNode.get(1));
 		int newCapacity = calculateCapacity(capacity, currentTaskNodeTask, currentTaskNode.get(1));
 		
 		if(computeHash(newState, newCost)){
 			if(currentTaskNode.get(1).equals(PICKEDUP)) {
 				child = new planeNode(vehicle, currentTaskNodeTask.pickupCity, newState, newCapacity, newCost, this, hashTable, algorithm.name());
 			}
 			else {
 				child = new planeNode(vehicle, currentTaskNodeTask.deliveryCity, newState, newCapacity, newCost, this, hashTable, algorithm.name());
 			}
 			children.add(child);
 		}
 		return child;
 	}
 
 	/**
 	 * Computes the costs for reaching a child node. This depends if we deliver a task or pick one up.
 	 * Delivering a task returns a rewards.
 	 * 
 	 * @param currentTaskNode
 	 * @param currentTaskNodeTask
 	 * @param taskState
 	 * @return
 	 */
 	private double calculateCost(ArrayList<Object> currentTaskNode, Task currentTaskNodeTask, Object taskState) {
 		double newCost = 0;
 		if(taskState.equals(PICKEDUP)) {
 			deliverTask(currentTaskNode, currentTaskNodeTask.deliveryCity);
 			newCost = costs + (nodeCity.distanceTo(currentTaskNodeTask.deliveryCity) * vehicle.costPerKm());
 		} else if (taskState.equals(INITSTATE)){
 			currentTaskNode.set(1, PICKEDUP);
 			newCost = costs + (nodeCity.distanceTo(currentTaskNodeTask.pickupCity) * vehicle.costPerKm());
 		}	
 		return newCost;
 	}
 
 	/**
 	 * Computes the current capacity.
 	 * Delivering a task gives more capacity, picking up a task reduces the capacity.
 	 * 
 	 * @param capacity
 	 * @param currentTaskNodeTask
 	 * @param taskState
 	 * @return int
 	 */
 	private int calculateCapacity(int capacity, Task currentTaskNodeTask, Object taskState) {
 		int newCapacity = 0;
 		if(taskState.equals(PICKEDUP)) {
 			newCapacity = capacity - currentTaskNodeTask.weight;
		} else {
			newCapacity = capacity + currentTaskNodeTask.weight;
 		}
 		return newCapacity;
 	}
 
 	/**
 	 * Sets the current task as delivered
 	 * 
 	 * @param currentTaskNode
 	 * @param city
 	 */
 	private void deliverTask(ArrayList<Object> currentTaskNode, City city) {
 		if( ((Task) currentTaskNode.get(0)).deliveryCity == city && currentTaskNode.get(1).equals(PICKEDUP)) {
 			currentTaskNode.set(1, DELIVERED);
 		}
 	}
 
 	/**
 	 * Getter and Setter methods
 	 */
 
 	public planeNode getParent() {
 		return parent;
 	}
 
 	public double getCosts() {
 		return costs;
 	}
 
 	public int getCostsPerKm() {
 		return vehicle.costPerKm();
 	}
 
 	public City getCity() {
 		return nodeCity;
 	}
 
 	public void printState() {
 		//System.out.println(nodeState);
 	}
 	
 	public int getCapacity() {
 		return capacity;
 	}
 
 	public ArrayList<ArrayList<Object>> getState() {
 		return nodeState;
 	}
 }
