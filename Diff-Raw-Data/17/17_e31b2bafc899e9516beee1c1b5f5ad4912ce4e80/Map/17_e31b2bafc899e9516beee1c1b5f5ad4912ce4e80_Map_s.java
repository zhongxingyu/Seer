 package btlshp.entities;
 
 import java.util.ArrayList;
 
 import btlshp.enums.Direction;
 
public class Map {
 	private static final int MAPWIDTH = 30;
 	private static final int MAPHEIGHT = 30;
 	ArrayList<Ship> ships;
 	MapNode   nodes [][];
 	Player    leftPlayer, rightPlayer;
 	
 	private void createNodes() {
 		nodes = new MapNode[MAPWIDTH][MAPHEIGHT];
 		
 		for(int y = 0; y < MAPWIDTH; ++y) {
 			for(int x = 0; x < MAPHEIGHT; ++x) {
 				nodes[y][x] = new MapNode();
 			}
 		}
 	}
 	
 	private void createShips() {
 		// TODO: Support for creating ship inventory for each player.
 	}
 	
 	private void createReefs() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	private void createBases() {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	/**
 	* Constructor to create a map object which will be the map used by two players to play a game.
 	* Any random elements will be generated.
 	* @param playerOne     Player 1 (left side)   	
 	* @param playerTwo     Player 2 (right side)
 	*/
 	public Map(Player playerOne, Player playerTwo) {
 		leftPlayer = playerOne;
 		rightPlayer = playerTwo;
 		
 		createNodes();
 		createBases();
 		createShips();
 		createReefs();
 	}
 	        	
 	/**
 	* Constructor to create a map object which will be the map used to continue a game between two players.
 	* The given StoredMap object will be used to restore the previously saved map state.
 	* @param map                               	StoredMap used to restore the map state
 	* @param playerOne     Player 1 (left side)   	
 	* @param playerTwo     Player 2 (right side)
 	*/
 	public Map(StoredMap map, Player playerOne, Player playerTwo) {
 		// TODO: Support for loading a stored map.
 		leftPlayer = playerOne;
 		rightPlayer = playerTwo;
 		
 		createNodes();
 	}
 	        	
 	/**
 	* Returns a StoredMap object representing the current state of the map.
 	* @return
 	*/
 	public StoredMap makeStoredMap() {
 		// TODO: This
 		return null;
 	}
 
 	        	
 	/**
 	* Performs the initial placement of a ship, either at the start of a game or on save load.
 	* @param s     The ship to be placed
 	* @param tail  The location of the tail of the ship
 	* @param dir   The direction the ship should be facing.
 	*/
 	public void addShip(Ship s, Location tail, Direction dir) {
 		
 	}
 	
 	/**
 	 * Removes a ship from the map.
 	 * @param s    The ship to be removed.
 	 */
 	public void removeShip(Ship s) {
 		
 	}
 	        	
 	/**
 	* This method cleans up any visibility data from the previous turn and updates the map with the
 	* radar/sonar of the current ship locations.
 	*/
 	public void updateFrame() {
 		
 	}
 
 	/**
 	* Returns a MapBlock object representing the state of the map at a given location.
 	* @param loc   Location to get the map block from
 	* @returns The map block at the given location
 	*/
 	public MapNode getMapNode(Location loc) {
 		return nodes[loc.gety()][loc.getx()];
 	}
 	        	
 	/**
 	* Checks to see if a ship movement can be carried out.
 	* @param s          Ship moving
 	* @param dir        direction of movement.
 	* @param blocks  number of blocks to move.
 	* @return true if the ship movement can be carried out.
 	*/
 	public boolean canMove(Ship s, Direction dir, int blocks) {
 		return false;
 		
 	}
 	        	
 	/**
 	* Moves a ship either entirely or partially the number of blocks in the given direction.
 	* @param s             	Ship moving
 	* @param dir          	Direction to move the ship
 	* @param blocks    	Number of blocks to be moved
 	* @returns Number of blocks actually used.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public int move(Ship s, Direction dir, int blocks) {
 		return 0;
 		
 	}
 	        	
 	/**
 	* Checks whether a ship can rotate from it's current direction to a new given direction. This method only
 	* considers physical barriers that can be see by radar.
 	*
 	* @param s 	          Ship to turn
 	* @param newDir    New direction
 	* @returns True if the ship can rotate in the new direction.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public boolean canShipRotate(Ship s, Direction newDir) {
 		return false;
 		
 	}
 	        	
 	/**
 	* Attempts to turn a ship from it's current direction to a new direction.
 	* @param s       Ship to turn
 	* @param newDir  Direction ship should face by the end of the turn.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public void rotateShip(Ship s, Direction newDir) {
 		
 	}
 
 	/**
 	* Attempts to place a mine via the given ship in the given location
 	* @param s       Ship that places the mine
 	* @param loc     Map location to place the mine
 	* @returns true if the mine was placed, or false if it could not be placed.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public boolean placeMine(Ship s, Location loc) {
 		return false;
 		
 	}
 
 	/**
 	* Attempts to pick up a mine via the given ship.
 	* @param s       Ship that picks up the mine
 	* @param loc     Location of the mine.
 	* @return true if the mine was picked up, or false if it was not.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public boolean pickupMine(Ship s, Location loc) {
 		return false;
 		
 	}
 	        	
 	/**
 	* Fires the torpedo of the given ship. The torpedo will start from the front of the ship and will
 	* travel in the direction of the ship.
 	* @param s       Ship to fire torpedo from.
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public void fireTorpedo(Ship s) {
 		
 	}
 	        	
 	/**
 	* Fires the guns of a ship at a specific location
 	* @param s             	Ship to fire guns from
 	* @param loc  Map location of the target of the fire
 	* @throws IllegalStateException If a move has already been made since the last generateTurn method call.
 	*/
 	public void fireGuns(Ship s, Location loc) {
 		
 	}
 }
