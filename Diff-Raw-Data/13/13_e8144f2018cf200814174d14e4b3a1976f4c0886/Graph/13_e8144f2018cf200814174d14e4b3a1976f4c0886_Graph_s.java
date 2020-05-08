 package Main;
 
 import java.util.*;
 import java.util.Map.Entry;
 /**
  * 
  * @author Robot-Subgoaling
  * Class Details:
  * 	This is the actual Graph API. Using the nodes, 
  * 	it will create a graph Data Structure to be able to
  * 	simulate the agent moving through the environment
  * Attributes:
  * 	environment - an Arraylist of Nodes containing all 
  * 	the available nodes in the 
  */
 
 public class Graph {
 	
 	private ArrayList<Node> nodes = new ArrayList<Node>();
 	
 	public Graph(){
 		/*
 		 * 9 different nodes - hand coded?
 		 * call setNeighbors()
 		 * Add the nodes to the array list
 		 */
 		Node s0 = new Node("s0");
 		nodes.add(s0);
 		Node s1 = new Node("s1");
 		nodes.add(s1);
 		Node s2 = new Node("s2");
 		nodes.add(s2);
 		Node s3 = new Node("s3");
 		nodes.add(s3);
 		Node s4 = new Node("s4");
 		nodes.add(s4);
 		Node s5 = new Node("s5");
 		nodes.add(s5);
 		Node s6 = new Node("s6");
 		nodes.add(s6);
 		Node s7 = new Node("s7");
 		nodes.add(s7);
 		Node s8 = new Node("s8");
 		nodes.add(s8);
 		
 		//sets up the start state and the goal state 
 		nodes.get(0).setHere(true);
 		nodes.get(nodes.size() - 1).setGoal(true);
 		setNeighbors();
 		
 	}
 	
 	/**
 	 * move() - Moves the agent from one state to the next
 	 * by simply collecting the state from each of the it
 	 */
 	public void move(int index){
 		/*
 		 * access the node on the hashmap
 		 * looks for the smallest value in the map (behavior) - collects the node's index/state
 		 * sets the agent to false in old node, and true in the new node
 		 * print out the node
 		 */
 		
 		nodes.get(index).neighbors.entrySet();
 		Node temp = nodes.get(index);
 		TreeSet<Double> val = new TreeSet<Double>(); //Sorts them automatically :)
 		
 		//Creates the list of values for that node
 		Iterator<Entry<Node, Double>> itr = nodes.get(index).neighbors.entrySet().iterator();
 		while(itr.hasNext()){
 			Map.Entry<Node, Double> pairs = (Map.Entry<Node, Double>)itr.next();
 			val.add(pairs.getValue());
 		}
 		
 		Double value = val.first(); //returns the smallest double
 		
 		//Return the corresponding key
 		Node nextStep = getKeyfromVal(index , nodes.get(index).neighbors , value);
 		
 		
 		if(nextStep == null){
 			System.out.println("Step Unsuccessful"); //Big Problem!
 		}else{ 
 			int newIndex = nodes.indexOf(nextStep);
 			nodes.get(index).setHere(false);
 			nodes.get(newIndex).setHere(true); //Moves the Agent
 			
 
 			System.out.print("Agent Moved from Here " + nodes.get(index).toString());
 			System.out.println(" to Here" + nodes.get(newIndex).toString());
 		}
 		
 	}
 	
 	private Node getKeyfromVal(int index, Map<Node,Double> map, Double value){
 		
 		/*
 		 * Error - returns null here for some reason.
 		 */
 		
 		for(Entry<Node, Double> entry: nodes.get(index).neighbors.entrySet()){
 			if(value.equals(entry.getValue()))
 				return entry.getKey();
 		}
 		return null;
 	}
 	
 	/**
 	 * setNeighbors() - sets up the neighborhood of all the
 	 * node elements
 	 */
 	private void setNeighbors(){
 		/*
 		 * 	for each node in nodes
 		 * 		add HashMap entries of neighboring Nodes and their corresponding Q-Values
 		 * 			note: could add random number generator for the random numbers.
 		 */
 
 		nodes.get(0).neighbors.put(nodes.get(1), 3.0);
 		nodes.get(0).neighbors.put(nodes.get(3), 1.0);
 		nodes.get(1).neighbors.put(nodes.get(0), 3.0);
 		nodes.get(1).neighbors.put(nodes.get(2), 7.0);
 		nodes.get(1).neighbors.put(nodes.get(4), 5.0);
 		nodes.get(2).neighbors.put(nodes.get(1), 7.0);
 		nodes.get(2).neighbors.put(nodes.get(5), 6.0);
 		nodes.get(3).neighbors.put(nodes.get(0), 1.0);
 		nodes.get(3).neighbors.put(nodes.get(4), 8.0);
 		nodes.get(3).neighbors.put(nodes.get(6), 0.9);
 		nodes.get(4).neighbors.put(nodes.get(1), 5.0);
 		nodes.get(4).neighbors.put(nodes.get(3), 8.0);
 		nodes.get(4).neighbors.put(nodes.get(5), 7.0);
 		nodes.get(4).neighbors.put(nodes.get(7), 6.0);
 		nodes.get(5).neighbors.put(nodes.get(2), 6.0);
 		nodes.get(5).neighbors.put(nodes.get(4), 7.0);
 		nodes.get(5).neighbors.put(nodes.get(8), 10.0);
 		nodes.get(6).neighbors.put(nodes.get(3), 0.9);
 		nodes.get(6).neighbors.put(nodes.get(7), 0.8);
 		nodes.get(7).neighbors.put(nodes.get(4), 6.0);
 		nodes.get(7).neighbors.put(nodes.get(6), 0.8);
 		nodes.get(7).neighbors.put(nodes.get(8), 0.7);
 		nodes.get(8).neighbors.put(nodes.get(5), 10.0);
 		nodes.get(8).neighbors.put(nodes.get(7), 0.7);
 	}
 	
 	/**
 	 * isWinner() - checks to see if agent is at goal state
 	 * @param index - index of the node we are checking
 	 * @return - true or false
 	 */
 	public boolean isWinner(int index){
 		return nodes.get(index).isHere() && nodes.get(index).isGoal();
 	}
 	
 	/**
 	 * findAgent() - scans the arraylist for the agent location
 	 * @return - index of the agent's locations
 	 */
 	public int findAgent(){
<<<<<<< HEAD
		int index = -3;
		for(int i = 0; i < nodes.size(); i++){
			if(nodes.get(i).isHere()){
				index = i;
			}
		}
 		
		return index;
=======
 		for(int i = 0; i < nodes.size(); i++){
 			if(nodes.get(i).isHere())
 				return i;
 		}
 		return -1;
>>>>>>> origin/Tenji
 	}
 	
 	/**
 	 * toString() - returns the toString of the entire Graph
 	 * @return - tostring of the entire graph
 	 */
 	public String toString(){
 		StringBuilder str = new StringBuilder();
 		
 		for(int i = 0; i < nodes.size(); i++)
 			str.append(printNode(i));
 		
 		return str.toString();
 	}
 	
 	/**
 	 * printNode() - returns the toString of just a single node element
 	 * @param index - index of the node
 	 * @return - toString variable
 	 */
 	public String printNode(int index){
 		return nodes.get(index).toString();
 	}
 }
