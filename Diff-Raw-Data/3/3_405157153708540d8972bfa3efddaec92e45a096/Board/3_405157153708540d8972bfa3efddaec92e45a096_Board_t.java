 /**
  * Board class that represents the current state of a Catan board.
  * Uses a 3-dimensional coordinate system where each point is represented as
  * the ordered tuple (x, y, z). x and y represent the position of the tile, and
  * z represents the point on the tile. Because each point on the board has
  * three possible coordinate representations, points are normalized so that
  * each point stored on the map has a coordinate that either represents
  * the top right point of a tile of the right point of a tile. 
  */
 package cs283.catan;
 
 import java.awt.Point;
 import java.io.*;
 import java.util.*;
 
 
 public final class Board implements Serializable {
     /**
      * 
      */
     private static final long serialVersionUID = 1070958368003158458L;
 
 
     /**TEMPORARY METHOD!!!*/
 public static void main(String args[]) {
     Board b = new Board();
     
     b.loadBoardGraphFromResource("cs283/catan/resources/board.csv");
     b.loadBoardTilesFromResource("cs283/catan/resources/tiles.csv");
 
     for (Map.Entry<Coordinate, Node> x : b.nodeSet.entrySet()) {
         System.out.println(x.getKey());
     }
     
     for (Map.Entry<Coordinate, Tile> x : b.tileSet.entrySet()) {
         System.out.println("Tile: " + x.getKey() + "    roll: " + x.getValue().getRollNumber() + "\ttype: " + x.getValue().getTileType());
     }
     
     Scanner in = new Scanner(System.in);
     
     Player bob = new Player("Bob", 0);
     Player joe = new Player("Joe", 1);
     
     System.out.println(b.addSettlement(new Coordinate(0,0,2), joe, false, false));
     System.out.println(b.addSettlement(new Coordinate(0,0,4), bob, false, false));
     System.out.println(b.addSettlement(new Coordinate(1,-1,5), joe, false, false));
     System.out.println(b.addSettlement(new Coordinate(1,-1,1), joe, false, false));
     
     System.out.println(b.upgradeSettlement(new Coordinate(0, 0, 2), joe));
     
     System.out.println(b.addRoad(new Coordinate(0,0,4), new Coordinate(0, 0, 3), bob, false));
     System.out.println(b.addRoad(new Coordinate(0, 0, 3), new Coordinate(0, 0, 2), joe, false));
     System.out.println(b.addRoad(new Coordinate(0, 0, 2), new Coordinate(0, 0, 1), joe, false));
     
     
     System.out.println(b.addSettlement(new Coordinate(-2,2,0), bob, false, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,0), new Coordinate(-2, 2, 1), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,1), new Coordinate(-2, 2, 2), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,3), new Coordinate(-2, 2, 2), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,3), new Coordinate(-2, 2, 4), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,4), new Coordinate(-2, 2, 5), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2,2,0), new Coordinate(-2, 2, 5), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2, 1, 2), new Coordinate(-2, 1, 3), bob, false));
     System.out.println(b.addRoad(new Coordinate(-2, 1, 3), new Coordinate(-2, 1, 4), bob, false));
     
     System.out.println(b.getResourceCardsEarned(5, joe));
     System.out.println(b.getResourceCardsEarned(5, bob));
     
     System.out.println(b.moveRobber(0, 0));
     
     System.out.println(b.getResourceCardsEarned(5, joe));
     System.out.println(b.getResourceCardsEarned(5, bob));
     
     System.out.println(b.moveRobber(0, 0));
     System.out.println(b.moveRobber(0, 0));
     System.out.println(b.moveRobber(1, -1));
     System.out.println(b.moveRobber(2, 0));
     
    // System.out.println(b.whoHasLongestRoad());
     
     System.out.println(b.addSettlement(new Coordinate(-2,1,3), joe, false, false));
 
    // System.out.println(b.whoHasLongestRoad());
     
     while (true) {
      // Extract the coordinate of the node from the node name
         int x = 0; int y = 0; int z = 0;
         int start = 0;
         int finish = 1;
         String input = in.nextLine();
         if (input.charAt(start) == '-') {
             finish++;
         }
         
         x = Integer.parseInt(input.substring(start, finish));
         
         start = finish;
         finish = start + 1;
         
         if (input.charAt(start) == '-') {
             finish++;
         }
         
         y = Integer.parseInt(input.substring(start, finish));
         
         z = Integer.parseInt(input.substring(finish, 
                                                 input.length()));
         
         // Add the node to the set of nodes
         Coordinate coord = new Coordinate(x, y, z);
         
         
         start = 0;
          finish = 1;
         input = in.nextLine();
         if (input.charAt(start) == '-') {
             finish++;
         }
         
         x = Integer.parseInt(input.substring(start, finish));
         
         start = finish;
         finish = start + 1;
         
         if (input.charAt(start) == '-') {
             finish++;
         }
         
         y = Integer.parseInt(input.substring(start, finish));
         
         z = Integer.parseInt(input.substring(finish, 
                                                 input.length()));
         
         // Add the node to the set of nodes
         Coordinate coord2 = new Coordinate(x, y, z);
         
         System.out.printf("%s is adjacent to %s: ", coord, coord2);
         System.out.printf("Normalized: %s %s\n", coord.normalizeCoordinate(), coord2.normalizeCoordinate());
         
         Node test = b.nodeSet.get(coord.normalizeCoordinate());
         System.out.println(test.isAdjacent(coord2.normalizeCoordinate()));
         
         break;
         
     }
     in.close();
 } /** End of Temporary Method!!!!*/
    
     
     /**
      * Map of the coordinates to the nodes in the graph. The key is the 
      * coordinate of the node and the value is the node.
      */
     private final Map<Coordinate, Node> nodeSet = 
                                                 new HashMap<Coordinate, Node>();
     
     /**
      * Running list of all of the settlements on the board. This
      * list is used for GUI drawing purposes only.
      */
     private final List<Settlement> settlementList = 
                                                    new LinkedList<Settlement>();
     
     /**
      * Map of players to road node sets in the graph. The key is the player name
      * and the value is the map of coordinates to nodes for all of the roads 
      * owned by the player.
      */
     private final Map<String, Map<Coordinate, Node>> roadSet = 
                                    new HashMap<String, Map<Coordinate, Node>>();
     
     /**
      * Running list of all of the roads on the board. This list
      * is used for GUI drawing purposes only.
      */
     private final List<Road> roadList = new LinkedList<Road>();
     
     /**
      * Map of coordinates to the tiles on the board. The key is the coordinate 
      * of the tile (Coordinate object, but the z coordinate is always 0) and the
      * value is the Tile object representing the tile.
      */
     private final Map<Coordinate, Tile> tileSet = 
                                                 new HashMap<Coordinate, Tile>();
     
     /**
      * Map of coordinates to pixels
      */
     private final Map<Coordinate, Point> pixelMap= 
                                                new HashMap<Coordinate, Point>();
     
     /**
      * Object representing the robber
      */
 	private final Robber robber = new Robber();
 	
 	
 	
 	/*
 	 * Constructor
 	 */
 	public Board() {
 	    // Nothing to do!
 	}  
 	
 	
 	/**
 	 * Initializes the board graph from a csv file that contains an adjacency
 	 * list representation of the graph
 	 * @param resourceName
 	 * @return whether or not the board was successfully loaded.
 	 */
 	public boolean loadBoardGraphFromResource(String resourceName) {
 	    boolean isSuccessful = false;
 	    
 	    // Attempt to read the data from the file
 	    InputStream resourceStream = Thread.currentThread().
 	                                 getContextClassLoader().
 	                                 getResourceAsStream(resourceName);
 	    
 	    if (resourceStream != null) {
 	        try {
 	            Scanner fileInput = new Scanner(resourceStream);
 	            
 	            // Read each line of the file. Each line represents a vertex
 	            // and its adjacency list
 	            String line;
 	            while (fileInput.hasNextLine()) {
 	            
 	                line = fileInput.nextLine();
 	                
 	                String split[] = line.split(",");
 	                
 	                // Create a new node
 	                Node newNode = new Node();
 	                
 	                // Create the list of neighbors of the node if the node
 	                // has neighbors
 	                if (split.length > 1) {
 	                    
 	                    for (int i = 1; i < split.length; i++) {
 	                        newNode.addNeighbor(new Coordinate(split[i]));
 	                    }
 	                    
 	                }
 	                
 	                // Extract the coordinate of the node from the node name
 	                // and add the node to the set of nodes
 	                Coordinate coord = new Coordinate(split[0]);
 	                nodeSet.put(coord.normalizeCoordinate(), newNode);
 	            }
 	            
 	            fileInput.close();
 	            
 	            
 	            isSuccessful = true;
 	            
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	        }
 	    }
 	    
 	    return isSuccessful;
 	}
 	
 	/**
 	 * Initializes the board tiles from a csv file that contains the x and y
 	 * coordinates, the roll number, and the tile type for each tile.
 	 * @param resourceName
 	 * @return whether or not the tiles were successfully loaded from the file.
 	 */
 	public boolean loadBoardTilesFromResource(String resourceName) {
 	    boolean isSuccessful = false;
 	    
 	    // Attempt to read the data from the file
 	    InputStream resourceStream = Thread.currentThread().
                                      getContextClassLoader().
                                      getResourceAsStream(resourceName);
 
         if (resourceStream != null) {
             try {
                 Scanner fileInput = new Scanner(resourceStream);
                 fileInput.useDelimiter("\\s|,|;");
                 
                 // Read each line of the file. Each line represents a vertex
                 // and its adjacency list
                 while (fileInput.hasNext()) {
                     
                     // Obtain the tile information
                     int x = fileInput.nextInt();
                     int y = fileInput.nextInt();
                     int rollNumber = fileInput.nextInt();
                     String cardTypeString = fileInput.next();
                     ResourceCard cardType;
                     
                     switch (cardTypeString.toLowerCase()) {
                     case "lumber":
                         cardType = ResourceCard.LUMBER;
                         break;
                     case "wool":
                         cardType = ResourceCard.WOOL;
                         break;
                     case "wheat":
                         cardType = ResourceCard.WHEAT;
                         break;
                     case "brick":
                         cardType = ResourceCard.BRICK;
                         break;
                     case "ore":
                         cardType = ResourceCard.ORE;
                         break;
                     case "desert":
                         cardType = ResourceCard.DESERT;
                     default:
                         cardType = ResourceCard.DESERT;
                     }
                     
                     // Add the tile to the set of tiles. Note that a tile
                     // coordinate always has a z value of 0
                     tileSet.put(new Coordinate(x, y, 0),  
                                 new Tile(x, y, rollNumber, cardType));
                 }
                 
                 fileInput.close();
                 
                 
                 isSuccessful = true;
                 
             } catch (Exception e) {
                 e.printStackTrace();
             }
         }
 	    
 	    return isSuccessful;
 	}
 	
 	/**
      * Loads a pixel mapping.
      * @parameter coordinate
      * @parameter pixel
      */
     public boolean loadPixelMappingsFromResource(String resourceName) {
         boolean isSuccessful = false;
         
         if (pixelMap.size() == 0) {
             // Attempt to read the data from the file
             InputStream resourceStream = Thread.currentThread().
                                          getContextClassLoader().
                                          getResourceAsStream(resourceName);
             
             if (resourceStream != null) {
                 try {
                     Scanner fileInput = new Scanner(resourceStream);
                     
                     // Read each line of the file. Each line represents a vertex
                     // and its associated pixel
                     String line;
                     while (fileInput.hasNextLine()) {
                     
                         line = fileInput.nextLine();
                         
                         String split[] = line.split(",");
                         
                         // Create a new point
                         Point point = new Point();
                         
                         // Create the list of neighbors of the node if the node
                         // has neighbors
                         if (split.length == 3) {
                             point.x = Integer.parseInt(split[1]);
                             point.y = Integer.parseInt(split[2]);
                         }
                         
                         // Extract the coordinate of the node from the node name
                         // and add the node to the set of nodes
                         Coordinate coord = new Coordinate(split[0]);
                         pixelMap.put(coord.normalizeCoordinate(), point);
                     }
                     
                     fileInput.close();
                     
                     
                     isSuccessful = true;
                     
                 } catch (Exception e) {
                     e.printStackTrace();
                 }
             }
         } else {
             isSuccessful = true;
         }
         
         return isSuccessful;
     }
     
     /**
      * Returns the pixel coordinate associated with the coordinate.
      * @return a point representing the pixel coordinate, or null if no such
      *         pixel mapping exists.
      */
     public Point getPixel(Coordinate coord) {
         return pixelMap.get(coord.normalizeCoordinate());
     }
 	
 	/**
 	 * Add the specified settlement to the board, assuming that there is not
 	 * already another settlement within a distance of 1.
 	 * @param location
 	 * @param owner
      * @param checkCards - whether or not the method checks to make sure the
      *                     player has appropriate resources.
      * @param checkRoads - whether or not the method checks whether the player
      *                     has an adjacent road to the settlement location.                     
 	 * @return whether or not the settlement was successfully added.
 	 */
 	public boolean addSettlement(Coordinate location, Player owner, 
 	                             boolean checkCards, boolean checkRoads) {
 	    boolean isSettlementAdded = false;
 	    
 	    location = location.normalizeCoordinate();
 	    Node locationNode = nodeSet.get(location);
 	    
 	    // Make sure user has proper hand
 	    boolean hasSheep = owner.getNumCards(ResourceCard.WOOL.toString()) > 0;
 	    boolean hasLumber = 
 	                      owner.getNumCards(ResourceCard.LUMBER.toString()) > 0;
 	    boolean hasBrick = owner.getNumCards(ResourceCard.BRICK.toString()) > 0;
 	    boolean hasWheat = owner.getNumCards(ResourceCard.WHEAT.toString()) > 0;
 	    
 	    if (locationNode != null && ((hasSheep && hasLumber && hasBrick &&
             hasWheat) || !checkCards) && !locationNode.hasSettlement()) {
 	        
 	        boolean safeToAdd = true;
 	        List<Coordinate> neighbors = locationNode.getNeighbors();
 	    
 	        // Make sure that there are no settlements directly adjacent to the
 	        // location
             for (Coordinate neighborCoordinate : neighbors) {
                 Node neighbor = nodeSet.get(neighborCoordinate);
 
                 if (neighbor.hasSettlement()) {
                     safeToAdd = false;
                     break;
                 }
             }
             
             // If performing a road check, make sure the player has a road
             // adjacent to the settlement location
             if (checkRoads && safeToAdd) {
                safeToAdd = roadSet.get(owner.getUsername())
                                   .containsKey(location);
             }
             
             // Add the settlement if there are not settlements directly adjacent
             // to the location (and if performing a road check, there is a road
             // adjacent to the settlement)
             if (safeToAdd) {
                 Settlement newSettlement = new Settlement(location, owner);
                
                 locationNode.setSettlement(newSettlement);
                 
                 // Add the settlement to the settlement list for GUI drawing
                 // purposes
                 settlementList.add(newSettlement);
                 
                 isSettlementAdded = true;
                 
                 //makes new coordinates for tradeports
                 
                 Coordinate wheatPort1 = new Coordinate(1, -2, 5);
                 Coordinate wheatPort2 = new Coordinate(1, -2, 4);
                 Coordinate threePort1 = new Coordinate(2, -2, 0);
                 Coordinate threePort2 = new Coordinate(2, -2, 5);
                 Coordinate brickPort1 = new Coordinate(2, -1, 0);
                 Coordinate brickPort2 = new Coordinate(2, -1, 1);
                 Coordinate threePort3 = new Coordinate(1, 1, 0);
                 Coordinate threePort4 = new Coordinate(1, 1, 1);
                 Coordinate orePort1 = new Coordinate(0, 2, 1);
                 Coordinate orePort2 = new Coordinate(0, 2, 2);
                 Coordinate threePort5 = new Coordinate(-1, 2, 2);
                 Coordinate threePort6 = new Coordinate(-1, 2, 3);
                 Coordinate lumberPort1 = new Coordinate(-2, 1, 2);
                 Coordinate lumberPort2 = new Coordinate(-2, 1, 3);
                 Coordinate threePort7 = new Coordinate(-2, 0, 3);
                 Coordinate threePort8 = new Coordinate(-2, 0, 4);
                 Coordinate woolPort1 = new Coordinate(-1, -1, 5);
                 Coordinate woolPort2 = new Coordinate(-1, -1, 4);
                 
                 wheatPort1 = wheatPort1.normalizeCoordinate();
                 wheatPort2 = wheatPort2.normalizeCoordinate();
                 threePort1 = threePort1.normalizeCoordinate();
                 threePort2 = threePort2.normalizeCoordinate();
                 brickPort1 = brickPort1.normalizeCoordinate();
                 brickPort2 = brickPort2.normalizeCoordinate();
                 threePort3 = threePort3.normalizeCoordinate();
                 threePort4 = threePort4.normalizeCoordinate();
                 orePort1 = orePort1.normalizeCoordinate();
                 orePort2 = orePort2.normalizeCoordinate();
                 threePort5 = threePort5.normalizeCoordinate();
                 threePort6 = threePort6.normalizeCoordinate();
                 lumberPort1 = lumberPort1.normalizeCoordinate();
                 lumberPort2 = lumberPort2.normalizeCoordinate();
                 threePort7 = threePort7.normalizeCoordinate();
                 threePort8 = threePort8.normalizeCoordinate();
                 woolPort1 = woolPort1.normalizeCoordinate();
                 woolPort2 = woolPort2.normalizeCoordinate();
                 
                 
                 if (checkCards) {
                     owner.doSettlementPurchase();
                     if (location.normalizeCoordinate().equals(wheatPort1) || location.normalizeCoordinate().equals(wheatPort2) )
                     {
                     	owner.has2WheatPort = true;
                     }else if (location.normalizeCoordinate().equals(orePort1) || location.normalizeCoordinate().equals(orePort2) )
                     {
                     	owner.has2OrePort = true;
                     }else if (location.normalizeCoordinate().equals(brickPort1) || location.normalizeCoordinate().equals(brickPort2) )
                     {
                     	owner.has2BrickPort = true;
                     }else if (location.normalizeCoordinate().equals(lumberPort1) || location.normalizeCoordinate().equals(lumberPort2) )
                     {
                     	owner.has2LumberPort = true;
                     }else if (location.normalizeCoordinate().equals(woolPort1) || location.normalizeCoordinate().equals(woolPort2) )
                     {
                     	owner.has2WoolPort = true;
                     }else if (location.normalizeCoordinate().equals(threePort1) || location.normalizeCoordinate().equals(threePort2) 
                     		|| location.normalizeCoordinate().equals(threePort3) || location.normalizeCoordinate().equals(threePort4) 
                     		|| location.normalizeCoordinate().equals(threePort5) || location.normalizeCoordinate().equals(threePort6) 
                     		|| location.normalizeCoordinate().equals(threePort7) || location.normalizeCoordinate().equals(threePort8) )
                     {
                     	owner.has3To1Port = true;
                     }
                     
                 }
             }
 	    }
 	    
 	    return isSettlementAdded;
 	}
 	
 public boolean freeAddSettlement(Coordinate location, Player owner, 
                                  boolean addPlacementCards) {
 	boolean isSettlementAdded = false;
 
 	location = location.normalizeCoordinate();
 	Node locationNode = nodeSet.get(location);
 
 
 	boolean safeToAdd = true;
 	List<Coordinate> neighbors = locationNode.getNeighbors();
 
 	// Make sure that there are no settlements directly adjacent to the
 	// location
 	for (Coordinate neighborCoordinate : neighbors) {
 		Node neighbor = nodeSet.get(neighborCoordinate);
 
 		if (neighbor.hasSettlement()) {
 			safeToAdd = false;
 			break;
 		}
 	}
 
 
 	// Add the settlement if there are not settlements directly adjacent
 	// to the location (and if performing a road check, there is a road
 	// adjacent to the settlement)
 	if (safeToAdd && !locationNode.hasSettlement()) {
 		Settlement newSettlement = new Settlement(location, owner);
 
 		locationNode.setSettlement(newSettlement);
 
 		// Add the settlement to the settlement list for GUI drawing
 		// purposes
 		settlementList.add(newSettlement);
 
 		isSettlementAdded = true;
 
 		//makes new coordinates for tradeports
 
 		Coordinate wheatPort1 = new Coordinate(1, -2, 5);
 		Coordinate wheatPort2 = new Coordinate(1, -2, 4);
 		Coordinate threePort1 = new Coordinate(2, -2, 0);
 		Coordinate threePort2 = new Coordinate(2, -2, 5);
 		Coordinate brickPort1 = new Coordinate(2, -1, 0);
 		Coordinate brickPort2 = new Coordinate(2, -1, 1);
 		Coordinate threePort3 = new Coordinate(1, 1, 0);
 		Coordinate threePort4 = new Coordinate(1, 1, 1);	
 		Coordinate orePort1 = new Coordinate(0, 2, 1);	
 		Coordinate orePort2 = new Coordinate(0, 2, 2);
 		Coordinate threePort5 = new Coordinate(-1, 2, 2);
 		Coordinate threePort6 = new Coordinate(-1, 2, 3);
 		Coordinate lumberPort1 = new Coordinate(-2, 1, 2);
 		Coordinate lumberPort2 = new Coordinate(-2, 1, 3);
 		Coordinate threePort7 = new Coordinate(-2, 0, 3);
 		Coordinate threePort8 = new Coordinate(-2, 0, 4);
 		Coordinate woolPort1 = new Coordinate(-1, -1, 5);
 		Coordinate woolPort2 = new Coordinate(-1, -1, 4);
 
 		wheatPort1 = wheatPort1.normalizeCoordinate();
 		wheatPort2 = wheatPort2.normalizeCoordinate();
 		threePort1 = threePort1.normalizeCoordinate();
 		threePort2 = threePort2.normalizeCoordinate();
 		brickPort1 = brickPort1.normalizeCoordinate();
 		brickPort2 = brickPort2.normalizeCoordinate();
 		threePort3 = threePort3.normalizeCoordinate();
 		threePort4 = threePort4.normalizeCoordinate();
 		orePort1 = orePort1.normalizeCoordinate();
 		orePort2 = orePort2.normalizeCoordinate();
 		threePort5 = threePort5.normalizeCoordinate();
 		threePort6 = threePort6.normalizeCoordinate();
 		lumberPort1 = lumberPort1.normalizeCoordinate();
 		lumberPort2 = lumberPort2.normalizeCoordinate();
 		threePort7 = threePort7.normalizeCoordinate();
 		threePort8 = threePort8.normalizeCoordinate();
 		woolPort1 = woolPort1.normalizeCoordinate();
 		woolPort2 = woolPort2.normalizeCoordinate();
 
 
 		
 		if (location.normalizeCoordinate().equals(wheatPort1) || location.normalizeCoordinate().equals(wheatPort2) )
 		{
 			owner.has2WheatPort = true;
 		}else if (location.normalizeCoordinate().equals(orePort1) || location.normalizeCoordinate().equals(orePort2) )
 		{
 			owner.has2OrePort = true;
 		}else if (location.normalizeCoordinate().equals(brickPort1) || location.normalizeCoordinate().equals(brickPort2) )
 		{
 			owner.has2BrickPort = true;
 		}else if (location.normalizeCoordinate().equals(lumberPort1) || location.normalizeCoordinate().equals(lumberPort2) )
 		{
 			owner.has2LumberPort = true;
 		}else if (location.normalizeCoordinate().equals(woolPort1) || location.normalizeCoordinate().equals(woolPort2) )
 		{
 			owner.has2WoolPort = true;
 		}else if (location.normalizeCoordinate().equals(threePort1) || location.normalizeCoordinate().equals(threePort2) 
 				|| location.normalizeCoordinate().equals(threePort3) || location.normalizeCoordinate().equals(threePort4) 
 				|| location.normalizeCoordinate().equals(threePort5) || location.normalizeCoordinate().equals(threePort6) 
 				|| location.normalizeCoordinate().equals(threePort7) || location.normalizeCoordinate().equals(threePort8) )
 		{
 			owner.has3To1Port = true;
 		}
 		
 		// Add the cards the player gets when placing a settlement during
 		// the last settlement placing phase
 		if (addPlacementCards) {
 		    owner.addArrayOfCards(getPlacementResourceCards(location));
 		}
 		
 	}
 
 
 return isSettlementAdded;
 }
 	
 	/**
 	 * Add the specified road to the board, assuming that the road is not
 	 * already taken. The road must be of length 1, as well as adjacent to
 	 * another road or settlement owned by the player.
 	 * @param start
 	 * @param finish
 	 * @param owner
 	 * @param checkCards - whether or not the method checks to make sure the
 	 *                     player has appropriate resources.
 	 * @return whether or not the road was successfully added.
 	 */
 	public boolean addRoad(Coordinate start, Coordinate finish, Player owner,
 	                       boolean checkCards) {
 	    boolean isRoadAdded = false;
 	    
 	    // Normalize the coordinates
 	    start = start.normalizeCoordinate();
 	    finish = finish.normalizeCoordinate();
 	    
 	    boolean canAddRoad = false;
 	    
 	    // Make sure the start and finish points of the road are adjacent
 	    Node nodeStartFromBoard = nodeSet.get(start);
 	    Node nodeFinishFromBoard = nodeSet.get(finish);
 	    
 	    canAddRoad = nodeStartFromBoard.isAdjacent(finish); 
 	    
 	    // Make sure road does not already exist
 	    for (Map<Coordinate, Node> roadNodeSet : roadSet.values()) {
 	        if (!canAddRoad) {
 	            break;
 	        }
 	        
 	        Node possibleRoadNode = roadNodeSet.get(start);
 	        
 	        if (possibleRoadNode != null) {
 	            if (possibleRoadNode.isAdjacent(finish)) {
 	                // Road already exists!
 	                canAddRoad = false;
 	            }
 	        }
 	    }
 	    
         Map<Coordinate, Node> playerRoadNodeSet = 
                                               roadSet.get(owner.getUsername());
 	    
         
         // Make sure user has proper hand
         boolean hasLumber = 
                           owner.getNumCards(ResourceCard.LUMBER.toString()) > 0;
         boolean hasBrick = 
                            owner.getNumCards(ResourceCard.BRICK.toString()) > 0;
         
         canAddRoad = canAddRoad && ((hasLumber && hasBrick) || !checkCards);
         
 	    // Make sure either road is adjacent to a settlement owned by the player
 	    // or adjacent to a road owned by the player
 	    if (canAddRoad && !((nodeStartFromBoard.hasSettlement() && 
 	        (nodeStartFromBoard.getSettlement().getOwner() == owner)) ||
 	        (nodeFinishFromBoard.hasSettlement() &&
 	        (nodeFinishFromBoard.getSettlement().getOwner() == owner)))) {
 	        
 	        canAddRoad = false;
 	        
 	        // Because the road is not adjacent to any settlements owned by
 	        // the owner, it must be adjacent to another road owned by the user
 	        if (playerRoadNodeSet != null) {
 	            if (playerRoadNodeSet.get(start) != null ||
 	                playerRoadNodeSet.get(finish) != null) {
 	                
 	                canAddRoad = true;
 	            }
 	        }
 	        
 	    }
 	    
 	    
 	    if (canAddRoad) {
 	        // Add the road to the player's set of roads. Create a new set for
 	        // the player if the player currently has no roads.
 	        
 	        if (playerRoadNodeSet == null) {
 	            playerRoadNodeSet = new HashMap<Coordinate, Node>();
 	            
 	            roadSet.put(owner.getUsername(), playerRoadNodeSet);
 	        }
 	        
 	        // Add the nodes if they do not exist
 	        Node startNode = playerRoadNodeSet.get(start);
 	        if (startNode == null) {
 	            startNode = new Node();
 	            playerRoadNodeSet.put(start, startNode);
 	        }
 	        
 	        Node finishNode = playerRoadNodeSet.get(finish);
 	        if (finishNode == null) {
 	            finishNode = new Node();
 	            playerRoadNodeSet.put(finish, finishNode);
 	        }
 	        
 	        // Add the edge
 	        startNode.addNeighbor(finish);
 	        finishNode.addNeighbor(start);
 	        
 	        // Add a road object to the list for GUI drawing purposes
 	        roadList.add(new Road(start,finish, owner));
 	        
 	        isRoadAdded = true;
 	        
 	        if (checkCards) {
 	            owner.doRoadPurchase();
 	        }
 	    }
 	    
 	    return isRoadAdded;
 	}
 	
 	public boolean freeAddRoad(Coordinate start, Coordinate finish, Player owner)
 	{
 		  boolean isRoadAdded = false;
 		    
 		    // Normalize the coordinates
 		    start = start.normalizeCoordinate();
 		    finish = finish.normalizeCoordinate();
 		    
 		    boolean canAddRoad = false;
 		    
 		    // Make sure the start and finish points of the road are adjacent
 		    Node nodeStartFromBoard = nodeSet.get(start);
 		    Node nodeFinishFromBoard = nodeSet.get(finish);
 		    
 		    canAddRoad = nodeStartFromBoard.isAdjacent(finish); 
 		    
 		    // Make sure road does not already exist
 		    for (Map<Coordinate, Node> roadNodeSet : roadSet.values()) {
 		        if (!canAddRoad) {
 		            break;
 		        }
 		        
 		        Node possibleRoadNode = roadNodeSet.get(start);
 		        
 		        if (possibleRoadNode != null) {
 		            if (possibleRoadNode.isAdjacent(finish)) {
 		                // Road already exists!
 		                canAddRoad = false;
 		            }
 		        }
 		    }
 		    
 	        Map<Coordinate, Node> playerRoadNodeSet = 
 	                                              roadSet.get(owner.getUsername());
 		    
 	        
 	  
 	        
 		    // Make sure either road is adjacent to a settlement owned by the player
 		    // or adjacent to a road owned by the player
 		    if (canAddRoad && !((nodeStartFromBoard.hasSettlement() && 
 		        (nodeStartFromBoard.getSettlement().getOwner() == owner)) ||
 		        (nodeFinishFromBoard.hasSettlement() &&
 		        (nodeFinishFromBoard.getSettlement().getOwner() == owner)))) {
 		        
 		        canAddRoad = false;
 		        
 		        // Because the road is not adjacent to any settlements owned by
 		        // the owner, it must be adjacent to another road owned by the user
 		        if (playerRoadNodeSet != null) {
 		            if (playerRoadNodeSet.get(start) != null ||
 		                playerRoadNodeSet.get(finish) != null) {
 		                
 		                canAddRoad = true;
 		            }
 		        }
 		        
 		    }
 		    
 		    
 		    if (canAddRoad) {
 		        // Add the road to the player's set of roads. Create a new set for
 		        // the player if the player currently has no roads.
 		        
 		        if (playerRoadNodeSet == null) {
 		            playerRoadNodeSet = new HashMap<Coordinate, Node>();
 		            
 		            roadSet.put(owner.getUsername(), playerRoadNodeSet);
 		        }
 		        
 		        // Add the nodes if they do not exist
 		        Node startNode = playerRoadNodeSet.get(start);
 		        if (startNode == null) {
 		            startNode = new Node();
 		            playerRoadNodeSet.put(start, startNode);
 		        }
 		        
 		        Node finishNode = playerRoadNodeSet.get(finish);
 		        if (finishNode == null) {
 		            finishNode = new Node();
 		            playerRoadNodeSet.put(finish, finishNode);
 		        }
 		        
 		        // Add the edge
 		        startNode.addNeighbor(finish);
 		        finishNode.addNeighbor(start);
 		        
 		        // Add a road object to the list for GUI drawing purposes
 		        roadList.add(new Road(start,finish, owner));
 		        
 		        isRoadAdded = true;
 		        
 		        
 		    }
 		    
 		    return isRoadAdded;
 	}
 	/**
 	 * Adds a tile to the board if it does not already exist.
 	 * @param x
 	 * @param y
 	 * @param rollNumber
 	 * @param tileType
 	 * @return whether or not the tile was added.
 	 */
 	public boolean addTile(int x, int y, int rollNumber,
 	                       ResourceCard tileType) {
 	    boolean isTileAdded = false;
 	    
 	    Coordinate tileCoordinate = new Coordinate(x, y, 0);
 	    
 	    if (!tileSet.containsKey(tileCoordinate)) {
 	        tileSet.put(tileCoordinate, new Tile(x, y, rollNumber, 
 	                                             tileType));
 	        
 	        isTileAdded = true;
 	    }
 	    
 	    return isTileAdded;
 	}
 	
 	/**
 	 * Upgrades a settlement to a city. Always checks whether a player has
 	 * enough cards.
 	 * @param location
 	 * @param owner
 	 * @return whether or not the upgrade was successful. Returns true if the
 	 *         settlement is already a city.
 	 */
 	public boolean upgradeSettlement(Coordinate location, Player owner) {
 	    boolean isUpgraded = false;
 	    
 	    location = location.normalizeCoordinate();
 	    
 	    // Attempt to find the settlement and upgrade it
 	    Node locationNode = nodeSet.get(location);
 	    
 	    // Make sure user has proper hand
         int numOre = owner.getNumCards(ResourceCard.ORE.toString());
         int numWheat = owner.getNumCards(ResourceCard.WHEAT.toString());
         
 	    if (locationNode != null && locationNode.hasSettlement() &&
 	        numOre >= 3 && numWheat >= 2) {
 	        
 	        Settlement settlement = locationNode.getSettlement();
 	        
 	        if (settlement.getOwner() == owner) {
 	            settlement.upgradeToCity();
 	            isUpgraded = true;
 	            
 	            owner.doCityPurchase();
 	        }
 	    }
 	    
 	    return isUpgraded;
 	}
 	
 	/**
 	 * Attempts to move the robber. Returns set of players adjacent to the
 	 * robber.
 	 * @param x
 	 * @param y
 	 * @return a set of the players adjacent to the robber. If the robber could
 	 *         not be moved, either because the robber is already on the tile
 	 *         or the tile does not exist, the return value will be null.
 	 */
 	public Set<Player> moveRobber(int x, int y) {
 	    Set<Player> adjacentPlayers = null;
 	    
 	    Tile tile = tileSet.get(new Coordinate(x, y, 0));
 	    
 	    // Check if the tile exists and the robber is successfully moved
 	    if (tile != null) {
 	        if (robber.setLocation(x, y)) {
 	            adjacentPlayers = new HashSet<Player>();
 	            
 	            // Look for all players with settlements adjacent to robber
 	            for (Coordinate coordinate : tile.getNormalizedCoordinates()) {
 	                Settlement settlement = 
 	                                    nodeSet.get(coordinate).getSettlement();
 	                
 	                if (settlement != null) {
 	                    adjacentPlayers.add(settlement.getOwner());
 	                }
 	            }
 	        }
 	    }
 
 	    return adjacentPlayers;
 	}
 	
 	
 	/**
 	 * Calculates the longest road for a specific player. Returns 0 if the
 	 * player has no roads or the player does not exist. The algorithm works
 	 * by running a depth-first search starting from each node in the players
 	 * road graph. Each depth-first search returns the length of the longest
 	 * path or cycle from the resulting depth-first search tree. The maximum of 
 	 * all of these values is equal to the player's longest road.
 	 * @param playerName
 	 * @return the length of the player's longest road, or 0 if the player
 	 *         has no roads or does not exist.
 	 */
 	public int getPlayersLongestRoad(String playerName) {
 	    int longestRoad = 0;
 	    
 	    // Make sure the road set contains the player
 	    if (roadSet.containsKey(playerName)) {
 	        // Get the map of coordinates and nodes that make up the player's
 	        // roads
 	        Map<Coordinate, Node> nodeMap = roadSet.get(playerName);
 	        
 	        // Run DFS starting with each of the nodes
 	        for (Node startNode : nodeMap.values()) {
 	            // Set of nodes that have already been visited in the DFS
 	            Set<Node> visitedNodes = new HashSet<Node>();
 	            
 	            longestRoad = Math.max(longestRoad, 
 	                                   longestRoadDFS(startNode, null,
 	                                                  visitedNodes, 
 	                                                  nodeMap, playerName));
 	        }
 	    }
 	    
 	    return longestRoad;
 	}
 	
 	/**
 	 * Performs a depth-first search using the given node as the current
 	 * node and the set as a set of vertices that have been visited already.
 	 * Returns 1 plus the length of the longest path branching out from the
 	 * current node. If the current node is a leaf in the DFS tree, return
 	 * 0 if the leaf does not have a back edge, and 1 if the leaf does have
 	 * a back edge. The parent node is the previous node visited, or null
 	 * if the current node is the root of the DFS tree.
 	 * @param current
 	 * @param visitedNodes
 	 * @param nodeMap
 	 * @param playerName
 	 * @return 0 if the leaf does not have a back edge, and 1 if the leaf does
 	 *           have a back edge.
 	 */
 	private int longestRoadDFS(Node current, Node parent,
 	                           Set<Node> visitedNodes,
 	                           Map<Coordinate, Node> nodeMap,
 	                           String playerName) {
 	    int maxLength = 0;
 	    	    
 	    // Add the current node to the set of visited nodes
 	    visitedNodes.add(current);
 	    
 	    // Look at all of the unvisited neighbors of the current node
 	    for (Coordinate neighborCoord : current.getNeighbors()) {
 	        Node neighbor = nodeMap.get(neighborCoord);
 	        
 	        // Make sure the neighbor does not have a settlement from
             // another player
             Settlement settlement = nodeSet.get(neighborCoord).getSettlement();
             
             if (settlement != null && 
                 !settlement.getOwner().getUsername().equals(playerName)) {
                 
                 maxLength = Math.max(maxLength, 1);
                 
             } else if (!visitedNodes.contains(neighbor)) {
 	            maxLength = Math.max(maxLength,
 	                                 longestRoadDFS(neighbor, current, 
 	                                                visitedNodes,
 	                                                nodeMap, playerName) + 1);
 	        } else if (neighbor != parent) {
 	            // The neighbor must be connected to the current node by a
 	            // back edge, so the maximum length is at least 1
 	            maxLength = Math.max(maxLength,  1);
 	        }
 	    }
 	    
 	    return maxLength;
 	}
 	
 	/**
 	 * Returns an array of the resource cards a player would earn if a certain 
 	 * number was rolled.
 	 * @param rollNumber
 	 * @param player
 	 * @return an array of the earned resource cards.
 	 */
 	public int[] getResourceCardsEarned(int rollNumber, Player player) {
 	    int cardsEarned[] = new int[5];
 	    for (int i = 0; i < cardsEarned.length; i++) {
 	        cardsEarned[i] = 0;
 	    }
 	    
 	    // For each tile that has the roll number, look at all of its nodes
 	    // and see if the player has a settlement on any of them
 	    for (Tile tile : tileSet.values()) {
 	        if (tile.getRollNumber() == rollNumber) {
 	            for (Coordinate tileCoord : tile.getNormalizedCoordinates()) {
 	                
 	                Node tileNode = nodeSet.get(tileCoord);
 	                
 	                if (tileNode.hasSettlement() && 
 	                    !robber.getLocation().equals(tile.getLocation())) {
 	                    
 	                    Settlement settlement = tileNode.getSettlement();
 	                    
 	                    if (settlement.getOwner() == player) {
 	                        
 	                        // If the settlement is a city, add 2 cards,
 	                        // otherwise add 1 card
 	                        cardsEarned[tile.getTileType().getIndex()] += 
 	                                                settlement.isCity() ? 2 : 1;
 	                    }
 	                }
 	            }
 	        }
 	    }
 	    
 	    return cardsEarned;
 	}
 	
 	/**
 	 * Returns an array of the resource cards earned by placing a settlement.
 	 * @param coord
 	 * @return a list of resource cards.
 	 */
 	public int[] getPlacementResourceCards(Coordinate coord) {
 	    coord = coord.normalizeCoordinate();
 	    
 	    int cardsEarned[] = new int[5];
 	    for (int i = 0; i < cardsEarned.length; i++) {
 	        cardsEarned[i] = 0;
 	    }
 	    
 	    // Get the node at the coordinate
 	    Node settlementLocation = nodeSet.get(coord);
 	    
 	    if (settlementLocation != null && settlementLocation.hasSettlement()) {
 	        // Obtain the three bordering tiles
 	        Coordinate neighboringTiles[] = new Coordinate[3];
 	        
 	        if (coord.z == 0) {
 	            neighboringTiles[0] = new Coordinate(coord.x, coord.y, 0);
 	            neighboringTiles[1] = new Coordinate(coord.x + 1, coord.y, 0);
 	            neighboringTiles[2] = new Coordinate(coord.x + 1, coord.y - 1,
 	                                                 0);
 	        } else {
 	            neighboringTiles[0] = new Coordinate(coord.x, coord.y, 0);
                 neighboringTiles[1] = new Coordinate(coord.x, coord.y + 1, 0);
                 neighboringTiles[2] = new Coordinate(coord.x + 1, coord.y, 0);
 	        }
 	        
 	        // Add the resources, if any
 	        for (Coordinate tileCoord : neighboringTiles) {
                 
                 Tile tile = tileSet.get(tileCoord);
                 if (tile != null) {
                     cardsEarned[tile.getTileType().getIndex()]++;
                 }
             }
 	    }
 	    
 	    return cardsEarned;
 	}
 		
 	/**
 	 * Returns a list of all of the settlements on the board.
 	 * @return a list of settlements.
 	 */
 	public List<Settlement> getSettlementList() {
 	    return settlementList;
 	}
 	
 	/**
 	 * Returns a list of all of the roads on the board
 	 * @return a list of roads.
 	 */
 	public List<Road> getRoadList() {
 	    return roadList;
 	}
 
 
 	public String tradeport(int tradeNumber, String resourceOne,
 			String resourceTwo, Player player) {
 		if (tradeNumber == 3)
 		{
 			if (player.has3To1Port)
 			{
 				if (player.removeCards(resourceOne, 3))
 				{
 					player.addCards(resourceTwo, 1);
 				}else
 				{
 					return new String ("chat* Server: you don't have the right number of cards");
 				}
 			}else
 			{
 				return new String("chat* Server: you don't have a 3 to 1 port");
 			}
 		}else if (tradeNumber == 2)
 		{
 			if (resourceOne == "ORE")
 			{
 				if(player.has2OrePort)
 				{
 					if (player.removeCards(resourceOne, 2))
 					{
 						player.addCards(resourceTwo, 1);
 					}else
 					{
 						return new String ("chat* Server: you don't have the right number of cards");
 					}
 				}else
 				{
 					return new String("chat* Server: you don't have that 2 to one port");
 				}
 			}
 			if (resourceOne == "LUMBER")
 			{
 				if(player.has2LumberPort)
 				{
 					if (player.removeCards(resourceOne, 2))
 					{
 						player.addCards(resourceTwo, 1);
 					}else
 					{
 						return new String ("chat* Server: you don't have the right number of cards");
 					}
 				}else
 				{
 					return new String("chat* Server: you don't have that 2 to one port");
 				}
 			}
 			if (resourceOne == "WHEAT")
 			{
 				if(player.has2WheatPort)
 				{
 					if (player.removeCards(resourceOne, 2))
 					{
 						player.addCards(resourceTwo, 1);
 					}else
 					{
 						return new String ("chat* Server: you don't have the right number of cards");
 					}
 				}else
 				{
 					return new String("chat* Server: you don't have that 2 to one port");
 				}
 			}else if (resourceOne == "BRICK")
 			{
 				if(player.has2BrickPort)
 				{
 					if (player.removeCards(resourceOne, 2))
 					{
 						player.addCards(resourceTwo, 1);
 					}else
 					{
 						return new String ("chat* Server: you don't have the right number of cards");
 					}
 				}else
 				{
 					return new String("chat* Server: you don't have that 2 to one port");
 				}
 			}else if (resourceOne == "WOOL")
 			{
 				if(player.has2WoolPort)
 				{
 					if (player.removeCards(resourceOne, 2))
 					{
 						player.addCards(resourceTwo, 1);
 					}else
 					{
 						return new String ("chat* Server: you don't have the right number of cards");
 					}
 				}else
 				{
 					return new String("chat* Server: you don't have that 2 to one port");
 				}
 			}else
 			{
 				return new String ("chat* Server: spell your resources correctly");
 			}
 		}else if (tradeNumber == 4)
 		{
 			if (player.removeCards(resourceOne, 4))
 			{
 				player.addCards(resourceTwo, 1);
 			}else
 			{
 				return new String ("chat* Server: you don't have the right number of cards");
 			}
 		}else
 		{
 			return new String ("chat* Wrong amount to trade.");
 		}
 		
 		return null;
 	}
 	
 }
