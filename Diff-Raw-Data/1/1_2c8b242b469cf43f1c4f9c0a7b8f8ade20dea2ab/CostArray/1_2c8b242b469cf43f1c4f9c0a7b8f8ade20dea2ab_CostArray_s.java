 //TODO FIX THIS WHOLE MESS
 package ai;
 import java.awt.Point;
 import java.util.LinkedList;
 
 class CostArray {
 	private static boolean[][] rigid; //initially false
 	private static boolean[][] initRigid; // to print more useful array
 	private static int[][] cost; //used by bfs, is returned by getCostArray
 	//static Point hero;
 	private static LinkedList<Point> Nodes = new LinkedList<Point>();
 	private static Point nextOne;
 	private static int[] generationOffspring;
 	private static int hY, hX;//for printMap() hero-coor.
 	
 
 	/**
 	 * Returns cost-array when passed a boolean "map" for walk-able tiles (false)
 	 * and non-walk-able (true). Hence called /rigid/ array.
 	 * Use the test method /print map/ for debugging/understanding quickly what the 
 	 * cost-array looks like.
 	 * @param notWalkAble the map--rigid array--used for cost-array calculation
 	 * @param heroX x-coor of hero
 	 * @param heroY y-coor of hero
 	 * @return cost-array of steps needed by the /hero/ to reach the tile. Hero position
 	 * is 0, and so are all other unreachable fields.
 	 */
 	public static int[][] getCostArray(boolean[][] notWalkAble, int heroX, int heroY) {
 		makeMap(notWalkAble);
 		hX = heroX;
 		hY = heroY;//for printMap();
 		bfs(heroX, heroY, 0, true); //heroStartPosition must be inital bfs!!!!
 		
 		return cost;
 	}
 	private static void bfs(int x, int y, int g, boolean next) {
 		check(x, y + 1, cost[x][y], g); 	//NORTH
 		check(x + 1, y + 1, cost[x][y], g);	//NORTHEAST
 		check(x + 1, y, cost[x][y], g);		//EAST
 		check(x + 1, y - 1, cost[x][y], g);	//SOUTHEAST
 		check(x, y - 1, cost[x][y], g);		//SOUTH
 		check(x - 1, y - 1, cost[x][y], g);	//SOUTHWEST
 		check(x - 1, y, cost[x][y], g);		//WEST
 		check(x - 1, y + 1, cost[x][y], g);	//NORTHWEST
 		rigid[x][y] = true;
 		if (next) { // is it time for progeny?
 			//Debugging info:
 			//System.out.println("reached with g:"+g+ " go:"+generationOffspring[g]);
 			next = false;
 			if (generationOffspring[g] != 0) {//no offspring?
 				for (int i = 0; i < generationOffspring[g]; i++) {
 					nextOne = Nodes.removeFirst();
 					bfs(nextOne.x, nextOne.y, g + 1, false);
 				}
 				if(!Nodes.isEmpty()) {
 					nextOne = Nodes.removeFirst();
 					//kill record of "Adam"
 					generationOffspring[g + 1] -= 1;
 					//this marks the beginning of world regeneration:
 					bfs(nextOne.x, nextOne.y, g + 1, true);
 				}
 			}
 		}
 	}
 	/*
 	 * checks coordinates a and b and applies bfs to them,
 	 *  only to be used within bfs.
 	 */
 	private static void check(int a, int b, int costOfCaller, int g) { 
 		/*
 		 * only alternated Nodes get to procreate further children
 		 */
 		if (!outsideArrayOrRigid(a, b)) {			
 			if (cost[a][b] != 0) {
 				if(costOfCaller + 1 < cost[a][b]) {
 					cost[a][b] = costOfCaller + 1;
 					//add to Nodes stack to follow later:
 					Nodes.add(new Point(a, b));
 					//every "add" is a "children" in the current generation!
 					generationOffspring[g]++;
 				}
 			} else {
 				//add to Nodes stack to follow later:
 				Nodes.add(new Point(a, b));
 				//every "add" is a "children" in the current generation!
 				generationOffspring[g]++;
 				cost[a][b] = costOfCaller + 1;
 			}
 		}
 	}
 	private static boolean outsideArrayOrRigid(int x, int y) {
 		try {
 			return rigid[x][y]; //in array, either "walk-able" or not
 		} catch (ArrayIndexOutOfBoundsException ex) {
 			return true; //not in array
 		}
 	}
 	private static void makeMap(boolean[][] toRigid) {
 		// ok arrayname.length is first outer array, arrayname[0].length nested
 		cost = new int[toRigid.length][toRigid[0].length];
 		rigid = toRigid;
 		// create copy for printMap(), only used for printMap()
 		initRigid = new boolean [toRigid.length][toRigid[0].length];
 		for(int i=0;i<initRigid.length;i++)
 			for(int j=0;j<initRigid[0].length;j++)
 				initRigid[i][j] = toRigid[i][j];
		rigid[2][0] = true;
 		//TODO: too small?
 		generationOffspring = new int[toRigid.length * toRigid[0].length];
 	}
 	//for debugging:
 	/**
 	 * prints rigid/cost maps _intuitively_: 
 	 * "bottom left" is x, y = 0; 
 	 * "up right" is x, y = their respective max value
 	 */
 	public static void printMap() {
 		//map.length is "x" value!  
 		//map.length for first dim, map[0].length for second, also map[3].length ...)
 		System.out.println("cost-array/rigid-map:");
 		for (int i = cost[0].length - 1; i > -1; i--) {
 			for (int n = 0; n < cost.length; n++) {			
 					System.out.print(cost[n][i] + " "); //read from cost array
 				}
 			System.out.print("   ");
 			for (int n = 0; n < initRigid.length; n++) {
 				if (initRigid[n][i]) { // the n/i reversing WILL be the final bug fix
 					System.out.print('#' + " "); //read from cost array
 				} else if (i == hY & n == hX) {
 					System.out.print('@' + " ");
 				} else {
 					System.out.print('.' + " ");
 				}
 			}	
 			System.out.println();
 		}
 	}
 }
