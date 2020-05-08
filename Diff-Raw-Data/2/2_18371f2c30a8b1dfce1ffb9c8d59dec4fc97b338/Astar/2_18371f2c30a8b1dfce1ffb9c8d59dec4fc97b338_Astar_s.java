 package dinaBOT.navigation;
 
 import java.lang.Math;
 
 /**
  * The Astar class is an implementation of the A* pathfinding algorithm mostly following the tutorial found at {@link http://www.policyalmanac.org/games/aStarTutorial.htm}.
  * <p>
  * It is implemented using only multi-dimensional arrays of integers (as opposed to linked-lists of objects) for the purposes of reducing the memory footprint of the algorithm on the NXT and because not all the built-in abstract collection classes (eg: ArrayList) in leJOS seem to be bug-free yet.
  * <p>
  * The algorithm is first given a resolution so it can define a square grid.  The total number of nodes (or squares) is the resolution squared.  The grid is then populated with obstacles.  Finally it is given an inital position and heading on the grid and a destination.  It then finds the shortest Manhattan path (ie: only moving parallel to the x or y axis of the grid; no diagonal movement allowed) from the initial position to the destination.  Consecutive nodes in the path are always adjacent; the path is not allowed to go over obstacles with a certain value (thus potentially no obstacles); changing direction (ie: turning) to go form one node to another costs more than continuing in the same direction; continuing in the same direction when going form one node to another always costs the same. If no path is possible, a null pointer is returned.
  * <p>
  * NOTE: currently you should create a new Astar object and feed it the obstacle information each you wish to generate a path.  Generating a path more than once from the same Astar object will give undefined results.
  * 
  * @author Stepan Salenikovich
  * @see Pathing
  * @see Map
  * @version 1
  */
 public class Astar {
 	/* -- Class Variables -- */
 	
 	/* algo critical arrays */
 	
 	/**
 	 * Stores all the information the algorithm needs about each node when finding a path.  Except for number of nodes, the information is always specific to the path currently being found and changes throughout the algorithm.
 	 * <p>
 	 * Note: this array basically replaces the need for having to create a node class and thus an object for every node.  See the source code comments to see what information is stored at where.
 	 */
 	
 	int[][][] pathInfo;	// array containing all information for pathfinding
 				/*
 				pathInfo[x][y][0] = status:
 							-1 = closed
 							0 = open
 							1 = default
 							>1 = obstacle
 				
 				pathInfo[x][y][1] = G: travel cost from starting square
 				pathInfo[x][y][2] = H: estimated cost to destination
 				pathInfo[x][y][3] = x pos of parent
 				pathInfo[x][y][4] = y pos of parent
 				pathInfo[x][y][5] = direction of robot at this tile
 				
 				simple, eh?
 				*/
 	
 	
 	int[][] open;	// array used to store the list of nodes currently "open"
 	int open_idx;	// index (of first dimension of open) of the next node to be stored
 					// implicitly indicates the number of nodes currently store in open or if open is empty
 					
 	int open_limit;	// max number of nodes which can be stored in open
 	
 
 	int rez;	// resolution of map
 	
 
 	
 	/* constants ... should probably be put in MechConstants or something similar */
 	
 	static final int OBSTACLE = 2;	// value which is considered unpassable
 	static final int NORTH = 90;
 	static final int SOUTH = 270;
 	static final int EAST = 0;
 	static final int WEST = 180;
 
 
 	/**
 	 * creates a new Astar path
 	 *
 	 * @param resolution the number of nodes on each axis; total number of nodes is the resolution squared.
 	*/
 	public Astar(int resolution) {
 		int i, j;
 		this.rez = resolution;
 		
 		pathInfo = new int [rez][rez][6];
 		open_limit = rez*rez;
 		open = new int[open_limit][2];
 		open_idx = 0;
 
 		
 		for(i = 0; i < rez; i++) {
 			for(j = 0; j < rez; j++) {
 				pathInfo[i][j][0] = 1;
 			}
 		}
 		
 		for(i = 0; i < open_limit; i++) {
 			open[i][0] = -1;
 			open[i][1] = -1;
 		}
 		
 	}
 
 	/**
 	 * Calculates and returns the shortest path from the start node to the end node.
 	 *
 	 * @param start the start node coordinates
 	 * @param direction the initial direciton (NORTH, SOUTH, EAST, or WEST)
 	 * @param end the end node coordinates
 	*/
 	public int[][] getPath( int[] start, int direction, int[] end ) {
 		int[] startInfo, nodeInfo;
 		int[] curr;
 		boolean done = false;
 		boolean path = true;
 		int[][] result;
 		int i;
 		
 		// check that end isn't start
 		if( (start[0] == end[0]) && (start[1] == end[1]) ) {
 			result = new int[1][2];
 			result[0][0] = start[0];
 			result[0][1] = start[1];
 
 			return result;
 		}
 		
 		startInfo = pathInfo[start[0]][start[1]];
 		startInfo[5] = direction;	// set direction
 		addOpen(start);	// add to open list
 		
 		startInfo[1] = 0;	// set G to 0
 		heuristicCost(start, end);	// calc H.. not really necessary
 		
 		// set parent to itself
 		startInfo[3] = start[0];
 		startInfo[4] = start[1];
 
 		// start algo
 		curr = firstOpen();
 
 		// do algo
 		while ( !done ) {
 			//System.out.println("(" + curr[0] + "," + curr[1] + ")");
 			done = astar( curr, end );
 			if( done ); // then you're done
 			// if curr == null, no path
 			else if( this.isOpenEmpty() ) {
 				done = true;
 				path = false;
 			} else 	curr = firstOpen();
 		}
 
 		// generate path if it exists
 		if( path ) {
 			// get number of nodes in paths
 			int[] parent = new int[] {end[0], end[1]};
 			
			i = 0;
 			while( !( (start[0] == parent[0]) && (start[1] == parent[1]) ) ) {
 				i++;
 				//System.out.println("node: " + parent[0] + "," + parent[1]);
 				
 				nodeInfo = pathInfo[parent[0]][parent[1]];
 				parent[0] = nodeInfo[3];
 				parent[1] = nodeInfo[4];								
 			}
 			
 			parent = new int[] {end[0], end[1]};
 			
 			result = new int[i+1][2];
 			for( ; i >= 0; i--) {
 				result[i][0] = parent[0];
 				result[i][1] = parent[1];
 
 				nodeInfo = pathInfo[parent[0]][parent[1]];
 				parent[0] = nodeInfo[3];
 				parent[1] = nodeInfo[4];
 			}
 
 
 		} else result = null;
 
 		return result;
 		
 	}
 
 	/**
 	 * Adds the given obstacle value to the given node.
 	 * 
 	 * @param obstacle coordinates of the node with the obstacle.
 	 * @param value value of the obstacle (should be at least 2)
 	 */
 	void addObstacle( int[] obstacle, int value ) {
 		pathInfo[obstacle[0]][obstacle[1]][0] = value;
 	}
 
 
 	// ------ open list methods ------- //
 
 	private void addOpen( int[] node ) {
 		
 		if(open_idx >= open_limit) {	//something is wrong
 			//LCD.drawString("Astar error: void addOpen()", 0, 0);
 			return;
 		}
 		
 		open[open_idx][0] = node[0];
 		open[open_idx][1] = node[1];
 				
 		open_idx++;
 		
 		
 		// set status of node to open
 		pathInfo[node[0]][node[1]][0] = 0;
 
 		/*
 		// add to open list
 		open.add( 0, new int[] {node[0], node[1]} );
 		*/
 
 	}
 
 	private int[] firstOpen() {
 		int[] node;
 		int leastF;	// F = H + G
 		int currF;
 		int[] firstNode = new int[2];	// node with smallest F
 		int firstNode_idx, i;
 		
 		firstNode[0] = open[0][0];
 		firstNode[1] = open[0][1];
 		firstNode_idx = 0;
 		leastF = Integer.MAX_VALUE;
 		
 		//check that there are still nodes left in open list
 		if( open_idx == 0) return new int[] {-1, -1};
 		
 		// find firstNode
 		for( i = 0; i < open_idx; i++) {
 			node = open[i];
 			currF = pathInfo[node[0]][node[1]][1] + pathInfo[node[0]][node[1]][2];
 			if( currF < leastF ) {
 				firstNode[0] = node[0];
 				firstNode[1] = node[1];
 				leastF = currF;
 				firstNode_idx = i;
 			}
 		}
 		
 		// remove firstNode from open list by shifting everything over
 		for(i = firstNode_idx; i < open_idx; i++ ) {
 			open[i][0] = open[i + 1][0];
 			open[i][1] = open[i + 1][1];
 		}
 		
 		// mark firstNode as closed
 		pathInfo[firstNode[0]][firstNode[1]][0] = -1;
 		
 		// decrease open_idx since one node has been removed
 		open_idx--;
 		
 		return firstNode;
 		
 		/*
 		//find in open list
 		node = searchOpen();
 
 		//remove from open list
 		open.remove(node);
 
 		//set status to closed
 		pathInfo[node[0]][node[1]][0] = -1;
 		
 		return node;
 		*/
 	}
 
 	//depreciated, for now
 	/*
 	private int[] searchOpen() {
 		int leastF;	// F = H + G
 		int currF;
 		int[] firstNode;	// node with smallest F
 
 		firstNode = open.get(0);
 		leastF = pathInfo[firstNode[0]][firstNode[1]][1] + pathInfo[firstNode[0]][firstNode[1]][2];
 		// find firstNode
 		for( int[] node : open) {
 			currF = pathInfo[node[0]][node[1]][1] + pathInfo[node[0]][node[1]][2];
 			if( currF < leastF ) {
 				firstNode = node;
 				leastF = currF;
 			}
 		}
 
 		return firstNode;
 	}
 	*/
 	
 	private boolean isOpenEmpty() {
 		if( open_idx == 0 ) return true;
 		else return false;
 	}
 
 
 
 	// ----- algo methods ------ //
 
 	//main algo
 	private boolean astar( int[] curr, int[] end ) {
 		int[] adj = new int[2];
 
 		//check that end has not been reached yet
 		if( (curr[0] == end[0]) && (curr[1] == end[1]) ) {
 			return true;
 		}
 
 
 		// north
 		if( curr[1] + 1 < rez ) {
 			adj[0] = curr[0];
 			adj[1] = curr[1] + 1;
 
 			adjCheck( adj, curr, end );
 		}
 
 		// south
 		if( curr[1] - 1 >= 0 ) {
 			adj[0] = curr[0];
 			adj[1] = curr[1] - 1;
 
 			adjCheck( adj, curr, end );
 		}
 
 		// east
 		if( curr[0] + 1 < rez ) {
 			adj[0] = curr[0] + 1;
 			adj[1] = curr[1];
 
 			adjCheck( adj, curr, end );
 		}
 
 		// west
 		if( curr[0] - 1 >= 0 ) {
 			adj[0] = curr[0] -1;
 			adj[1] = curr[1];
 
 			adjCheck( adj, curr, end );
 		}
 
 		return false;
 			
 	}
 
 	//checks valid adjacent nodes
 	private void adjCheck( int[] adj, int[] curr, int[] end ) {
 		int[] adjInfo;
 				
 		adjInfo = pathInfo[adj[0]][adj[1]];
 		// check if no obstacle and not on closed list
 		if(  (adjInfo[0] < OBSTACLE) && (adjInfo[0] > -1)  ) {
 			
 			// if not on open list already
 			if( adjInfo[0] != 0 ) {
 
 				addOpen(adj);	// add to open list
 			
 				// make curr the parent of adj				
 				adjInfo[3] = curr[0];
 				adjInfo[4] = curr[1];
 
 				// calculate G and H
 				movementCost(curr, adj);
 				heuristicCost(adj, end);
 			} else {
 				int currG, newG;	// current and new G costs
 				int currDir;	// current direction
 
 				// save current G and dir
 				currG = adjInfo[1];
 				currDir = adjInfo[5];
 
 				// calc and save new G and dir
 				movementCost(curr, adj);
 				newG = adjInfo[1];
 				
 				//compare Gs
 				if( newG < currG ) {
 					// change parent to curr
 					adjInfo[3] = curr[0];
 					adjInfo[4] = curr[1];
 				} else {	// else revert to previous G and dir
 					adjInfo[1] = currG;
 					adjInfo[5] = currDir;
 				}
 
 			}
 		}
 	}
 
 	// assumes nodes are adjacent
 	private void movementCost(int[] start, int[] end) {
 		int startDir;
 		int endDir;
 		int cost = 1;
 
 		startDir = pathInfo[start[0]][start[1]][5];
 		
 		endDir = NORTH;
 		if( end[1] - start[1] < 0) endDir = SOUTH;
 		else if( end[0] - start[0] > 0) endDir = EAST;
 		else if( end[1] - start[1] < 0) endDir = WEST;
 
 		// set direction of end
 		pathInfo[end[0]][end[1]][5] = endDir;
 
 		// if not the same dir, add cost
 		if( endDir != startDir ) {
 			cost = 2;
 		}
 
 		// add local cost, to total cost
 		cost += pathInfo[start[0]][start[1]][1];
 		// set end cost (G)
 		pathInfo[end[0]][end[1]][1] = cost;
 	}
 
 	// does manhattan distance from start to end, stores in H of start
 	// no turnin penalty currently
 	private void heuristicCost(int[] start, int[] end) {
 		int cost;
 		
 		cost = Math.abs(start[0] - end[0]);
 		cost += Math.abs(start[1] - end[1]);
 
 		// set H cost of start
 		pathInfo[start[0]][start[1]][2] = cost;
 	}
 	
 }
