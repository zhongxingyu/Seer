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
 		
		Node temp = new Node(nodes.get(index));
 		TreeSet<Double> val = new TreeSet<Double>(); //Sorts them automatically :)
 		
 		//Creates the list of values for that node
		Iterator<Entry<Node, Double>> itr = temp.neighbors.entrySet().iterator();
 		while(itr.hasNext()){
 			Map.Entry<Node, Double> pairs = (Map.Entry<Node, Double>)itr.next();
 			val.add(pairs.getValue());
			itr.remove();
 		}
 		
 		Double value = val.first(); //returns the smallest double
 		
 		//Return the corresponding key
		Node nextStep = getKeyfromVal(index , temp.neighbors , value);
 		
 		
 		if(nextStep == null){
 			System.out.println("Step Unsuccessful"); //Big Problem!
 		}else{
 			System.out.println("Node Found!"); 
 			int newIndex = nodes.indexOf(nextStep);
 			System.out.println("Old Index: " + index + " and object: " + nodes.get(index).toString());
 			System.out.println("New Index: " + newIndex + " and object: " + nodes.get(newIndex).toString());
 		}
 		
 	}
 	
 	private Node getKeyfromVal(int index, Map<Node,Double> map, Double value){
 		
 		/*
 		 * Error - returns null here for some reason.
 		 */
 		
		Node temp = new Node(nodes.get(index));
		for(Entry<Node, Double> entry: temp.neighbors.entrySet()){
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
 
 		nodes.get(0).neighbors.put(nodes.get(1), Math.random() * 12);
 		nodes.get(0).neighbors.put(nodes.get(3), Math.random() * 12);
 		nodes.get(1).neighbors.put(nodes.get(0), Math.random() * 12);
 		nodes.get(1).neighbors.put(nodes.get(2), Math.random() * 12);
 		nodes.get(1).neighbors.put(nodes.get(4), Math.random() * 12);
 		nodes.get(2).neighbors.put(nodes.get(1), Math.random() * 12);
 		nodes.get(2).neighbors.put(nodes.get(5), Math.random() * 12);
 		nodes.get(3).neighbors.put(nodes.get(0), Math.random() * 12);
 		nodes.get(3).neighbors.put(nodes.get(4), Math.random() * 12);
 		nodes.get(3).neighbors.put(nodes.get(6), Math.random() * 12);
 		nodes.get(4).neighbors.put(nodes.get(1), Math.random() * 12);
 		nodes.get(4).neighbors.put(nodes.get(3), Math.random() * 12);
 		nodes.get(4).neighbors.put(nodes.get(5), Math.random() * 12);
 		nodes.get(4).neighbors.put(nodes.get(7), Math.random() * 12);
 		nodes.get(5).neighbors.put(nodes.get(2), Math.random() * 12);
 		nodes.get(5).neighbors.put(nodes.get(4), Math.random() * 12);
 		nodes.get(5).neighbors.put(nodes.get(8), Math.random() * 12);
 		nodes.get(6).neighbors.put(nodes.get(3), Math.random() * 12);
 		nodes.get(6).neighbors.put(nodes.get(7), Math.random() * 12);
 		nodes.get(7).neighbors.put(nodes.get(4), Math.random() * 12);
 		nodes.get(7).neighbors.put(nodes.get(6), Math.random() * 12);
 		nodes.get(7).neighbors.put(nodes.get(8), Math.random() * 12);
 		nodes.get(8).neighbors.put(nodes.get(5), Math.random() * 12);
 		nodes.get(8).neighbors.put(nodes.get(7), Math.random() * 12);
 	}
 	
 	/**
 	 * isWinner() - checks to see if agent is at goal state
 	 * @param index - index of the node we are checking
 	 * @return - true or false
 	 */
 	public boolean isWinner(int index){
 		return true;
 	}
 	
 	/**
 	 * findAgent() - scans the arraylist for the agent location
 	 * @return - index of the agent's locations
 	 */
 	public int findAgent(){
 		return 0;
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
