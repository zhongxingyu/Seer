 //package danger_zone;
 import java.sql.Timestamp;
 import java.util.Stack;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.lang.Math;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@version 0.0
 *@since 2012-10-2
 *
 * The DangerNode class is a node of a K-d Tree of dimensionality 2. It contains an id for reference to an outside database as well as
 * a timestamp identified with the time the Danger Zone took place or was entered into the database. The Node is sorted by longitude and
 * latitude which are stored in the coordinates array in their respective order.
 */
 public class DangerNode{
 	/**
 	*Identifier corresponding to an integer database key.
 	*/
 	private int id;
 	/**
 	*Time the DangerNode took place at
 	*/
 	private Timestamp timestamp;
 	/**
 	*Left child of  DangerNode node
 	*/
 	private DangerNode left = null;
 	/**
 	*Right child of this DangerNode node
 	*/
 	private DangerNode right = null;
 	/**
 	*Tuple for holding the longitude and latitude of the Danger Zone referenced by id. Indicies to the array correspond to 0 => longitude and 1=> latitude
 	*/
 	private float[] coordinates = new float[2];
 
 	/**
 	*Creates an instance of the DangerNode.g
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@return the created object instance of DangerNode
 	*/
 	public DangerNode(float longitude,float latitude, int id){
 		this(longitude,latitude,id,null,null);
 	}
 
 	/**
 	*Creates an instance of the DangerNode. Nulls may be passed for lChild and rChild if there are no children to this node
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@param lChild the left child of the DangerNode
 	*@param rChild the right child of the DangerNode
 	*@return the created object instance of DangerNode
 	*/
 	public DangerNode(float longitude,float latitude,int id,DangerNode lChild,DangerNode rChild){
 		this.id = id;
 		this.timestamp = new java.sql.Timestamp(System.currentTimeMillis());
 		this.coordinates[0] = longitude;
 		this.coordinates[1] = latitude;
 		this.left = lChild;
 		this.right = rChild;
 	}
 
 
 	/**
 	*Creates an instance of the DangerNode. Nulls may be passed for lChild and rChild if there are no children to this node
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@param lChild the left child of the DangerNode
 	*@param rChild the right child of the DangerNode
 	*@param tStamp Timestamp in SQL Format
 	*@return the created object instance of DangerNode
 	*/
 	public DangerNode(float longitude,float latitude,int id,DangerNode lChild, DangerNode rChild, java.sql.Timestamp tStamp){
 		this(longitude,latitude,id,lChild,rChild);
 		this.timestamp = tStamp;
 	}
 
 	/**
 	*Searches the tree for the searchTuple given, returns null if not found and a DangerNode if found. Note this search is note the nearest neighbor search
 	*@param searchTuple The tuple of floats we're searching for.
 	*@return null if not found, the DangerNode if found.
 	*/
 	public DangerNode search(float[] searchTuple){
 		//Possibly thinking about having this return the id for the danger zone instead, will have to talk with group about this
 		return this.innerSearch(searchTuple,2);
 	}
 
 	/**
 	*Search used by search function to search along the correct axis given the depth of the tree
 	*@param searchTuple the tuple of floats we're searching for.
 	*@param depth The depth of the tree, required to compute the proper sorting axis of the tree
 	*@return null if not found, the DangerNode if found.
 	*/
 	private DangerNode innerSearch(float[] searchTuple, int depth){
 		//Check for match
 		if(coordinates[(depth % coordinates.length)] == searchTuple[depth % searchTuple.length]){
 			if(coordinates[(depth +1)% coordinates.length] == searchTuple[(depth +1)% searchTuple.length]){
 					return this;
 			}
 		}
 		//To the left, to the left...
 		if(coordinates[depth % coordinates.length] > searchTuple[depth % searchTuple.length]){
 			if(this.left != null){
 				return this.left.innerSearch(searchTuple,depth+1);
 			}
 		}else{
 			if(this.right != null){
 				return this.right.innerSearch(searchTuple,depth+1);
 			}
 		}
 		return null;
 	}
 
 	/**
 	*Gets the coordinate at the specified index. 
 	*@param index Since coordinates are a 2-Tuple, should be either 0 or 1
 	*@return The coordinate at that index.
 	*/
 	public float getCoordinate(int index){
 		return coordinates[index];
 	}
 
 	/**
 	*Returns the longitude of this DangerNode
 	*@return The longitude of this DangerNode
 	*/
 	public float getLongitude(){
 		return coordinates[0];
 	}
 
 	/**
 	*Returns the latitude of this DangerNode
 	*@return the latitude of this DangerNode
 	*/
 	public float getLatitude(){
 		return coordinates[1];
 	}
 
 	/**
 	*Adds a node to the K-d Tree
 	*@param newNode The node to be added.
 	*/
 	public void addNode(DangerNode newNode){
 		this.innerAddNode(newNode, 2);
 	}
 
 	/**
 	*Returns the database identifier associated with this DangerNode
 	*@return The database id for this DangerNode
 	*/
 	public int getID(){
 		return id;
 	}
 
 	/**
 	*Private helping function for adding a node, required to sort properly on the depth without exposing to user
 	*@param newNode the node to be added
 	*@param depth The depth of the current node we're on. Used for sorting by an axis
 	*/
 	public void innerAddNode(DangerNode newNode, int depth){
 		if(coordinates[depth % coordinates.length] > newNode.getCoordinate(depth % coordinates.length)){
 			if(this.left != null){
 				this.left.innerAddNode(newNode,depth+1);
 			}else{
 				this.left = newNode;
 			}
 		}else{
 			if(this.right != null){
 				this.right.innerAddNode(newNode,depth+1);
 			}else{
 				this.right = newNode;
 			}
 		}
 	}
 
 	/**
 	*Nearest neighbor search on this DangerNode. Returns up to numOfNeighbors neighbors. 
 	*@param searchTuple The tuple we'd like to find neighbors for.
 	*@int numOfNeighbors How many neighbors one would like maximum from the tree, the function will not neccesary return this number of nodes
 	*@return Returns a Stack of DangerNodes of nearest neighbors to the searchTuple
 	*/
 	public Stack<DangerNode> nearestNeighbor(float[] searchTuple,int numOfNeighbors){
 		//ArrayList of DangerNodes or id's?
 		Stack<DangerNode> neighborNodes = new Stack<DangerNode>();
 		neighborNodes =  this.innerNN(searchTuple,2,neighborNodes);
 		Stack<DangerNode> returnNeighbors = new Stack<DangerNode>();
 		for(; numOfNeighbors > 0; numOfNeighbors--){
 			if(!neighborNodes.empty()){
 				returnNeighbors.push(neighborNodes.pop());
 			}
 		}
 		return returnNeighbors;
 	}
 
 	/**
 	*Helper function to perform the nearest neighbor algorithm
 	*@param searchTuple The tuple to find neighbors for
 	*@param depth Depth of the current node
 	*@param bests Stack to store current best neighbors in
 	*@return Returns stack of DangerNodes of nearest neighbors to the searchTuple
 	*/
 	public Stack<DangerNode> innerNN(float[] searchTuple, int depth,Stack<DangerNode> bests){
 		//Iteratively search the tree using a stack to maintain links between trees and such
 		DangerNode curNode = this;
 		Stack<DangerNode> treeStack = new Stack<DangerNode>();
 
 		//Move down the tree until we hit a leaf node
 		while(curNode != null){
 			treeStack.push(curNode);
 			if(curNode.getCoordinate(depth % coordinates.length) > searchTuple[depth % searchTuple.length]){
 				System.out.println("Traverse Left");
 				System.out.println(curNode);
 				if(curNode.left != null){
 					curNode = curNode.left;
 				}else{
 					//leaf node 
 					curNode = null;
 				}
 			}else{
 				System.out.println("Traverse Right");
 				System.out.println(curNode);
 				if(curNode.right != null){
 					curNode = curNode.left;
 				}else{
 					curNode = null;
 				}
 			}
 			depth++;
 		}
 
 		//Now unwind our 'recursion' checking children along the way up for better distances
 		DangerNode searchNode = new DangerNode(searchTuple[0],searchTuple[1],-1);
 		float currentBest = treeStack.peek().euDistance(searchNode);
 		bests.push(treeStack.peek());
 		while(!treeStack.empty()){
 			System.out.println(currentBest);
 			curNode = treeStack.pop();
 			//Check if this node is better
 			if(currentBest > curNode.euDistance(searchNode)){
 				currentBest = curNode.euDistance(searchNode);
 				bests.push(curNode);
 				//Check its child to see if we need to expand the search to the other branch of the tree
 				if(curNode.left != null){
 					if(curNode.euDistance(curNode.left) < currentBest){
 						//Must search that tree
 						bests = curNode.left.innerNN(searchTuple,depth,bests);
 					}
 				}
 				if(curNode.right != null){
 					if(curNode.euDistance(curNode.right) < currentBest){
 						//Must also search that
 						bests = curNode.right.innerNN(searchTuple,depth,bests);
 					}
 				}
 			}
 			//move up a depth
 			depth--;
 		}
 		//Now we have a Stack of the best nodes we found. 
 
 		return bests;
 	}
 
 	/**
 	*Computes the euclidean distance between a node and another Node.
 	*@param other The node to compute the distance to
 	*@return The euclidean distance between the node and passed in and this one.
 	*/
 	public float euDistance(DangerNode other){
 		return square((getLatitude() - other.getLatitude())) + square(getLongitude() - other.getLongitude());
 	}
 
 	/**
 	*Squares a floating point number
 	*@param number Number to square
 	*@return The number squared
 	*/
 	public float square(float number){
 		return number * number;
 	}
 
 	/**
 	*Prints the tree to the system console, starting with leaves
 	*/
 	public void printTree(){
 		this.innerPrintTree(2,"O");
 	}
 
 	/**
 	*Returns the longitude, latitude and id of the DangerNode
 	*/
 	public String toString() {
 		return "(" + getLongitude() + "," + getLatitude() + ")  id: " + getID();
 	}
 
 	/**
 	*Helper function to assist with printTree function
 	*@param depth Depth of the current node
 	*@param traversel Prints L for Left, R for Right or O for Root depending on traversel of tree
 	*/
 	public void innerPrintTree(int depth,String traversel){
 		if(this.left != null){
 			this.left.innerPrintTree(depth + 1,"L");
 		}
 		if(this.right != null){
 			this.right.innerPrintTree(depth +1,"R");
 		}
 		System.out.print(traversel + (depth-1));
 		for(int i=0; i < depth; i++){
 			System.out.print(" ");
 		}
 		System.out.println("(" + getLongitude() + "," + getLatitude() +")");
 	}
 
 	private ArrayList<DangerNode> getList(ArrayList<DangerNode> list){
 		if(this.left != null){
			return this.left.getList(list);
 		}
 		if(this.right != null){
			return this.right.getList(list);
 		}
 		list.add(this);
 		return list;
 	}
 
 	/**
 	*
 	*/
 	static final public DangerNode reBalanceTree(DangerNode root){
 		//Get whole tree into a single list
 		ArrayList<DangerNode> list = root.getList(new ArrayList<DangerNode>());
 		return root.innerBalance(list,2);
 	}
 
 	/**
 	*Helper function to create balance tree
 	*/
 	public DangerNode innerBalance(ArrayList<DangerNode> nodes,int depth){
 		//if the list is empty then we can just ignore it
 		if(nodes.isEmpty()){return null;}
 
 		//Sort the list of points by the axis
 		final int axis = depth % coordinates.length;
 		Collections.sort(nodes, new Comparator<DangerNode>(){
 			public int compare(DangerNode first, DangerNode second){
 				if(first.getCoordinate(axis) < second.getCoordinate(axis)){
 					return -1;
 				}else if(first.getCoordinate(axis) > second.getCoordinate(axis)){
 					return 1;
 				}else{
 					return 0;
 				}
 			}
 		});
 		//Choose a median point:
 		int median = (int)Math.floor((double)nodes.size() / 2);
 
 		//Create Node
 		DangerNode node = nodes.get(median);
 
 		//Split the list into two pieces by median
 		ArrayList<DangerNode> partialList = new ArrayList<DangerNode>();
 		for(int l = 0; l < median; l++){
 			partialList.add(nodes.get(l));
 		}
 		node.left = innerBalance(partialList,depth+1);
 		//let garbage collection deal with the other half
 		partialList = new ArrayList<DangerNode>();
 		for(int r = (median+1); r < nodes.size(); r++){
 			partialList.add(nodes.get(r));
 		}
 		node.right = innerBalance(partialList,depth+1);
 
 		return node;
 
 	}
 
 	//Test function
 	public static void main(String argv[]) throws Exception
 	{
 		DangerNode p = new DangerNode(7,2,1);
 		//(2,3), (5,4), (9,6), (4,7), (8,1), (7,2).
 		p.addNode(new DangerNode(2,3,4));
 		p.addNode(new DangerNode(5,4,5));
 		p.addNode(new DangerNode(9,6,6));
 		p.addNode(new DangerNode(4,7,7));
 		p.addNode(new DangerNode(8,1,8));
 		p.printTree();
		DangerNode.reBalanceTree(p);
 		p.printTree();
 		float [] s = new float[2];
 		System.out.println("Nearest Neighbor Search on 0,0");
 		s[0] = 0;
 		s[1] = 0;
 		System.out.println(p.nearestNeighbor(s,1));
 	}	
 
 }
