 package danger_zone;
 import java.sql.Timestamp;
 import java.sql.*;
 import javax.sql.*;
 import java.util.Stack;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.lang.Math;
 
 /**
 *@author Ethan Eldridge <ejayeldridge @ gmail.com>
 *@author Garth Fritz <gfritz @ uvm.edu>
 *@version 0.1
 *@since 2012-10-2
 *
 * The DangerNode class is a node of a K-d Tree of dimensionality 2. It contains an id for reference to an outside database as well as
 * a timestamp identified with the time the Danger Zone took place or was entered into the database. The Node is sorted by longitude and
 * latitude which are stored in the coordinates array in their respective order.
 *
 * 
 */
 public class DangerNode{
 	private final float LAT_DUMMY = 100.0f;
 	private final float LONG_DUMMY = 200.0f;
 
 	private static int numNodes = 0;
 	private int nodeNumber = -1;
 	/**
 	*Identifier corresponding to an integer database key.
 	*/
 	private int id;
 	/**
 	*Time the DangerNode took place at (Unix milliseconds)
 	*/
 	private long timestamp;
 	/**
 	*Left child of  DangerNode node
 	*/
 	private DangerNode left = null;
 	/**
 	*Right child of this DangerNode node
 	*/
 	private DangerNode right = null;
 	/**
 	*Tuple for holding the longitude and latitude of the Danger Zone referenced by id. Indicies to the array correspond to 0 => longitude and 1=> latitude.  Latitudes range [-90,90], and Longitudes range [-180,180].
 	*/
 	private float[] coordinates = new float[2];
 
 	/**
 	*Creates an DUMMY instance of the DangerNode.g
 	*/
 	public DangerNode(){
 		this.id = -1;
 		this.timestamp = -1;
 		this.left = null;
 		this.right = null;
 		this.coordinates[0] = 100.0f;
 		this.coordinates[1] = 200.0f;
 		nodeNumber = numNodes;
 		numNodes++;
 	}
 
 	/**
 	*Creates an instance of the DangerNode.g
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*/
 	public DangerNode(float longitude,float latitude, int id){
 		this(longitude,latitude,id,null,null);
 	}
 
 		/**
 	*Creates an instance of the DangerNode.g
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@param tStamp Timestamp in Long
 	*/
 	public DangerNode(float longitude,float latitude, int id, long tStamp){
 		this(longitude,latitude,id,null,null);
 		this.timestamp = tStamp;
 	}
 
 	/**
 	*Creates an instance of the DangerNode. Nulls may be passed for lChild and rChild if there are no children to this node
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@param lChild the left child of the DangerNode
 	*@param rChild the right child of the DangerNode
 	*/
 	public DangerNode(float longitude,float latitude,int id,DangerNode lChild,DangerNode rChild){
 		this.id = id;
 		this.timestamp = System.currentTimeMillis();
 		this.coordinates[0] = longitude;
 		this.coordinates[1] = latitude;
 		this.left = lChild;
 		this.right = rChild;
 		nodeNumber = numNodes;
 		numNodes++;
 	}
 
 
 	/**
 	*Creates an instance of the DangerNode. Nulls may be passed for lChild and rChild if there are no children to this node
 	*@param longitude The longitude coordinate of the danger zone associated with id
 	*@param latitude The latitude coordinate of the danger zone associated with id
 	*@param id The integer id for the database entry associated with the danger zone
 	*@param lChild the left child of the DangerNode
 	*@param rChild the right child of the DangerNode
 	*@param tStamp Timestamp in SQL Format
 	*/
 	public DangerNode(float longitude,float latitude,int id,DangerNode lChild, DangerNode rChild, long tStamp){
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
 	*Returns if this node is a leaf or not.
 	*@return True if this node is a leaf and False if not.
 	*/
 	public boolean isLeaf(){	
 		return this.right == null && this.left == null;
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
 	*Overloaded nearestNeighbor that returns nodes within a radius of the searchTuple, not necessarily the best first.
 	*@param searchTuple The tuple to find neighbors for
 	*@param numOfNeighbors How many neighbors one would like maximum from the tree, the function will not neccesary return this number of nodes
 	*@param radius Radius to search in from the searchTuple, note that we use squared distance in comparisons so squaring the radius may be a good idea before passing it into the function
 	*@return Returns a Stack of DangerNodes of nearest neighbors to the searchTuple, can return an empty stack if radius is too small
 	*/
 	public Stack<DangerNode> nearestNeighbor(float[] searchTuple, int numOfNeighbors, float radius){
 		Stack<DangerNode> neighborNodes = new Stack<DangerNode>();
 		neighborNodes =  this.innerNN(searchTuple,0,neighborNodes,radius);
 		if(neighborNodes.size() > numOfNeighbors){
 			neighborNodes.setSize(numOfNeighbors);
 		}
 		return neighborNodes;
 	}
 
 	/**
 	*Helper function to perform the nearest neighbor algorithm
 	*@param searchTuple The tuple to find neighbors for
 	*@param depth Depth of the current node
 	*@param bests Stack to store current best neighbors in
 	*@return Returns stack of DangerNodes of nearest neighbors to the searchTuple
 	*/
 	public Stack<DangerNode> innerNN(float[] searchTuple, int depth,Stack<DangerNode> bests,float radius){
 		//Iteratively search the tree using a stack to maintain links between trees and such
 		DangerNode curNode = this;
 		Stack<DangerNode> treeStack = new Stack<DangerNode>();
 
 		//Move down the tree until we hit a leaf node
 		while(curNode != null){
 			treeStack.push(curNode);
 			if(curNode.getCoordinate(depth % coordinates.length) > searchTuple[depth % searchTuple.length]){
 				if(curNode.left != null){
 					curNode = curNode.left;
 				}else{
 					//leaf node 
 					curNode = null;
 				}
 			}else{
 				if(curNode.right != null){
 					curNode = curNode.right;
 				}else{
 					curNode = null;
 				}
 			}
 			depth++;
 		}
 
 		//Now unwind our 'recursion' checking children along the way up for better distances
 		final DangerNode searchNode = new DangerNode(searchTuple[0],searchTuple[1],-1);
 		float currentBest = treeStack.peek().sqDistance(searchNode);
 		bests.push(treeStack.peek());
 		while(depth != 0){
 			curNode = treeStack.pop();
 			//Check if this node is better
 			if(curNode.sqDistance(searchNode) < radius){
 				currentBest = curNode.sqDistance(searchNode);
 				bests.push(curNode);
 				//Check its child to see if we need to expand the search to the other branch of the tree
 				if(curNode.left != null){
 					if(curNode.sqDistance(curNode.left) < radius){
 						//Must search that tree
 						bests = curNode.left.innerNN(searchTuple,depth-1,bests,radius);
 					}
 				}
 				if(curNode.right != null){
 					if(curNode.sqDistance(curNode.right) < radius){
 						//Must also search that
 						bests = curNode.right.innerNN(searchTuple,depth-1,bests,radius);
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
 	*Nearest neighbor search on this DangerNode. Returns up to numOfNeighbors neighbors. 
 	*@param searchTuple The tuple we'd like to find neighbors for.
 	*@param numOfNeighbors How many neighbors one would like maximum from the tree, the function will not neccesary return this number of nodes
 	*@return Returns a Stack of DangerNodes of nearest neighbors to the searchTuple
 	*/
 	public Stack<DangerNode> nearestNeighbor(float[] searchTuple,int numOfNeighbors){
 		//ArrayList of DangerNodes or id's?
 		Stack<DangerNode> neighborNodes = new Stack<DangerNode>();
 		neighborNodes =  this.innerNN(searchTuple,0,neighborNodes);
 		if(neighborNodes.size() > numOfNeighbors){
 			neighborNodes.setSize(numOfNeighbors);
 		}
 		return neighborNodes;
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
 				if(curNode.left != null){
 					curNode = curNode.left;
 				}else{
 					//leaf node 
 					curNode = null;
 				}
 			}else{
 				if(curNode.right != null){
 					curNode = curNode.right;
 				}else{
 					curNode = null;
 				}
 			}
 			depth++;
 		}
 
 		//Now unwind our 'recursion' checking children along the way up for better distances
 		final DangerNode searchNode = new DangerNode(searchTuple[0],searchTuple[1],-1);
 		float currentBest = treeStack.peek().sqDistance(searchNode);
 		bests.push(treeStack.peek());
 		while(depth > 0){
 			if(!treeStack.empty()){
 				curNode = treeStack.pop();	
 			}else{
 				//We have no more tree to search so just return
 				return bests;
 			}
 			//Check if this node is better
 			if(curNode.sqDistance(searchNode) < currentBest){
 				currentBest = curNode.sqDistance(searchNode);
 				bests.push(curNode);
 				//Check its child to see if we need to expand the search to the other branch of the tree
 				if(curNode.left != null){
 					if(curNode.sqDistance(curNode.left) < currentBest){
 						//Must search that tree
 						bests = curNode.left.innerNN(searchTuple,depth-1,bests);
 					}
 				}
 				if(curNode.right != null){
 					if(curNode.sqDistance(curNode.right) < currentBest){
 						//Must also search that
 						bests = curNode.right.innerNN(searchTuple,depth-1,bests);
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
 	public float sqDistance(DangerNode other){
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
 		return "\"id" + nodeNumber + "\" :" + getID();
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
 
 	/**
 	*Gets the K-d Tree in the form of an ArrayList
 	*@param list The current list of nodes being built
 	*@return The k-d tree in an ArrayList
 	*/
 	private ArrayList<DangerNode> getList(ArrayList<DangerNode> list){
 		if(this.left != null){
 			list =  this.left.getList(list);
 		}
 		if(this.right != null){
 			list = this.right.getList(list);
 		}
 		list.add(this);
 		return list;
 	}
 
 	/**
 	*Balances the k-d Tree by reconstructing the entire tree
 	*@param root The root of the tree to reconstruct
 	*@return The root of the newly balanced tree.
 	*/
 	static final public DangerNode reBalanceTree(DangerNode root){
 		//Get whole tree into a single list
 		ArrayList<DangerNode> list = root.getList(new ArrayList<DangerNode>());
 		return root.innerBalance(list,0);
 	}
 
 	/**
 	*Helper function to create balance tree
 	*@param nodes The list of nodes to be used in constructing the balanced tree.
 	*@param depth The current depth of recursion
 	*@return The root of the newly balanced tree.
 	*/
 	final private DangerNode innerBalance(ArrayList<DangerNode> nodes,int depth){
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
 
 	/**
 	*Wrapper function that uses reBalance and a list of DangerNodes to construct a balanced KD tree.
 	*@param nodes The list of nodes to be used in constructing the balanced tree.
 	*@return The root of the newly built and balanced tree.
 	*/
 	final public static DangerNode makeTree(ArrayList<DangerNode> nodes){
 		//Check if the list is empty
 		if(nodes.isEmpty()){return null;}
 
 		DangerNode root = nodes.get(0);
 		nodes.remove(0);
 
 		//Load up a DangerNode root with the DangerNodes from the ArrayList.
 		//So we make add every node in the ArrayList to the first node in the list.
 		for (DangerNode dangernode : nodes) {
 			root.addNode(dangernode);
 		}
 
 		//Now rebalance the tree and return it.
 		return DangerNode.reBalanceTree(root);
 	}
 
 	/**
 	*Function to fetch all records
 	*@param user The username to connect to the database. From command line argument.
 	*@param password The password to connect to the database. From command line argument.
 	*@return The root of the newly built and balanced tree.
 	*/
 	static final public ArrayList<DangerNode> fetchDangers(String user, String password) throws Exception{
 		// Guide: http://www.java-samples.com/showtutorial.php?tutorialid=9
 
 		String dbUrl = "jdbc:mysql://dangerzone.cems.uvm.edu/DangerZone";
 		String dbClass = "com.mysql.jdbc.Driver";
 		String query = "SELECT * FROM tbl_danger_zone";
 
 		ArrayList<DangerNode> fetchedList = new ArrayList<DangerNode>();
 
 		try {
 
 			System.out.println("Connecting...");
 			Class.forName("com.mysql.jdbc.Driver").newInstance();
 			Connection con = DriverManager.getConnection (dbUrl, user, password);
 
 			System.out.println(con);
 
 			Statement stmt = con.createStatement();
 			ResultSet rs = stmt.executeQuery(query);
 
 			// Fetch each row from the result set.
 			//float longitude,float latitude, int id, long tStamp
 			float longitude, latitude;
 			int id;
 			long tStamp;
 
 			//Process each record.
 			while (rs.next()) {
 				// Get the data from the row using the column name
 				longitude = rs.getFloat("fld_longitude");
 				latitude = rs.getFloat("fld_latitude");
 				id = rs.getInt("pk_id");
 				tStamp = rs.getLong("fld_time_stamp");
 				//Create a DangerNode from this data
 				DangerNode node = new DangerNode(longitude,latitude,id,tStamp);
 				//Append this node to the ArrayList<DangerNode>
 				fetchedList.add(node);
 			}//end while
 
 			stmt.close();
 			con.close();
 			System.out.println("Closing connection for Tree");
 
 			return fetchedList;
 
 		}//end try
 
 		catch(ClassNotFoundException e) {
 			e.printStackTrace();
 			return fetchedList;
 		}
 
 		catch(SQLException e) {
 			e.printStackTrace();
 			return fetchedList;
 		}
 
 	}
 
 	private Region convertGeoPoint(float lat, float lon, double radius){
 		double theta = lat;
 		double phi = lon;
 		double left = theta - (radius/Region.EARTH_RADIUS)*(180/Region.PI);
 		double right = theta + (radius/Region.EARTH_RADIUS)*(180/Region.PI);
 		double top = phi + (radius/(Region.EARTH_RADIUS*Math.cos(theta)))*(180/Region.PI);
 		double bottom = phi - (radius/(Region.EARTH_RADIUS*Math.cos(theta)))*(180/Region.PI);
 		return new Region(left,right,top,bottom);
 	}
 
 	private Region getRegionFor(float x, float y, double radius){
 		return new Region(x-radius,x+radius,y+radius,y-radius);
 	}
 
 	public ArrayList<DangerNode> boundedSearch(float lat, float lon, double radius){
 		//Create the region to search in:
 		
 		Region searchRegion = getRegionFor(lat,lon,radius);//convertGeoPoint(lat,lon,radius);
 
 		return innerBoundedSearch(searchRegion,new ArrayList<DangerNode>(),0,new Region());
 	}
 
 	private ArrayList<DangerNode> innerBoundedSearch(Region sRegion,ArrayList<DangerNode> results,int depth,Region bRegion){
 		int axis = depth % coordinates.length;
 		if(this.isLeaf()){
 			if(sRegion.fullyContains(this)){
 				results.add(this);
 			}
 		}else{
 			//Subtree we need to redefine our bounding region bRegion
 			if(axis == 0){
 				//We are splitting on the x axis
 				if(sRegion.fullyContains(bRegion)){
 					//We are in the region so we report ourselves
 					results.add(this);
 				}else{
 					if(sRegion.fullyContains(this)){
 						results.add(this);
 					}
 				}
 				if(sRegion.intersects(bRegion)){
 					if(this.left != null){
 						//Search down the left with our splitting line as a bound on the right
 						return this.left.innerBoundedSearch(sRegion,results,depth+1,bRegion.setRight(this.getCoordinate(axis)));
 					}else{
 						//Null link return results
 						return results;
 					}
 				}
 				if(sRegion.intersects(bRegion)){
 					if(this.right != null){
 						//Search down the left with a splitting line as a bound on the left
 						return this.right.innerBoundedSearch(sRegion,results,depth+1,bRegion.setLeft(this.getCoordinate(axis)));
 					}
 				}
 			}else{
 				//We are splitting on the y  axis
 				if(sRegion.fullyContains(bRegion)){
 					//We are in the region so we report ourselves
 					results.add(this);
 				}else{
 					if(sRegion.fullyContains(this)){
 						results.add(this);
 					}
 				}
 				if(sRegion.intersects(bRegion)){
 					if(this.left != null){
 						//Search down the left with our splitting line as a bound on the right
 						return this.left.innerBoundedSearch(sRegion,results,depth+1,bRegion.setTop(this.getCoordinate(axis)));
 					}else{
 						//Null link return results
 						return results;
 					}
 				}
 				if(sRegion.intersects(bRegion)){
 					if(this.right != null){
 						//Search down the left with a splitting line as a bound on the left
 						return this.right.innerBoundedSearch(sRegion,results,depth+1,bRegion.setBottom(this.getCoordinate(axis)));
 					}
 				}
 			}
 		}
 		return results;
 	}
 
 	/**
 	*
 	*/
 	private static ArrayList<DangerNode> flatten(ArrayList<DangerNode> nodes){
 		//Flatten each nodes subtree out.
 		ArrayList<DangerNode> temp = new ArrayList<DangerNode>();
 		for(DangerNode node : nodes){
 			temp = node.collectTree(temp);
 		}
 		return temp;
 	}
 
 	public ArrayList<DangerNode> collectTree(){
 		return collectTree(new ArrayList<DangerNode>());
 	}
 
 	public  ArrayList<DangerNode> collectTree(ArrayList<DangerNode> results){
 		//Traverse the tree and put all children in there
 		if(this.left!= null){
 			results = this.left.collectTree(results);
 		}
 		if(this.right!=null){
 			results = this.right.collectTree(results);
 		}
 		if(!results.contains(this)){
 			results.add(this);
 		}
 		return results;
 	}	
 
 	//Test function
 	public static void main(String args[]) throws Exception
 	{
 		if (args.length != 2) {
 	        System.out.println("Required arguments: username password");
 	        return;
       	}
 
       	DangerNode d = new DangerNode();
 		
 		// ArrayList of DangerNodes to hold the nodes collected from the database.
 		// Fetch the DangerNodes
 		ArrayList<DangerNode> nodes = fetchDangers(args[0],args[1]);
 
 		System.out.println("Printing fetched nodes.");
 
 		for (int i=0; i<nodes.size(); i++){
 			//System.out.println(nodes.get(i).toString());
 
 			//Load DangerNodes from arraylist into a root DangerNode
 			d.addNode(nodes.get(i));
 		}//print out contents of nodes list
 
 		System.out.println("Printing tree before balance...");
 		d.printTree();
 
 		System.out.println("Printing tree after balance...");
 		d = DangerNode.reBalanceTree(d);
 		d.printTree();
 
 
 		
 	
 		System.out.println("Creating Tree");
 		DangerNode p = new DangerNode(9,6,2);
 		//(2,3), (5,4), (9,6), (4,7), (8,1), (7,2).
 		p.addNode(new DangerNode(7,2,1));
 		p.addNode(new DangerNode(2,3,3));
 		p.addNode(new DangerNode(5,4,2));
 		p.addNode(new DangerNode(8,1,3));
 		p.addNode(new DangerNode(4,7,3));
 		p.printTree();
 		System.out.println("Re-Balancing Tree");
 		p= DangerNode.reBalanceTree(p);
 		p.printTree();
 		
 		float [] s = new float[2];
 		System.out.println("Nearest Neighbor Search on 13,9");
 		s[0] = 13;
 		s[1] = 9;
 		Stack<DangerNode> bests = p.nearestNeighbor(s,2);
 		while(!bests.empty()){
 			System.out.println("Bests: "+ bests.pop());
 		}
 		
 
 		System.out.println("Testing Bounded Tree Search Algorithm");
 		ArrayList<DangerNode> results =  p.boundedSearch(1,1,4);
 		for(DangerNode result : DangerNode.flatten(results)){
 			System.out.println("Result: " + result.getLongitude() + "," + result.getLatitude());
 		}
 
 		System.out.println("Testing collect tree");
 		results = p.collectTree();
 		for(DangerNode result : results){
 			System.out.println("Result: " + result.getLongitude() + "," + result.getLatitude());
 		}
 
//
 
 	}// end main	
 
 }
