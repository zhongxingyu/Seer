 package battlechallenge.bot;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import battlechallenge.ActionResult;
 import battlechallenge.Coordinate;
 import battlechallenge.ShipAction;
 import battlechallenge.ship.Ship;
 import battlechallenge.ship.Ship.Direction;
 
 /**
  * The Class ClientPlayer.
  */
 public class ClientPlayer {
 	
 	/** The player name. */
 	protected final String playerName;
 	
 	/** The id to identify the player. */
 	protected int networkID;
 
 	/** The board width. */
 	protected int boardWidth;
 
 	/** The board height. */
 	protected int boardHeight;
 	
 	/**
 	 * Save the width of the game board.
 	 *
 	 * @param boardWidth the width of the game board
 	 */
 	public void setBoardWidth(int boardWidth) {
 		this.boardWidth = boardWidth;
 	}
 	
 	/**
 	 * Save the height of the game board.
 	 *
 	 * @param boardHeight the height of the game board
 	 */
 	public void setBoardHeight(int boardHeight) {
 		this.boardHeight = boardHeight;
 	}
 	
 	/**
 	 * Sets the network id.
 	 *
 	 * @param networkID the new network id
 	 */
 	public void setNetworkID(int networkID) {
 		this.networkID = networkID;
 	}
 	
 	/**
 	 * Instantiates a new client player.
 	 *
 	 * @param playerName the player name
 	 */
 	public ClientPlayer(final String playerName) {
 		this(playerName, 0, 0, 0);
 	}
 	
 	/**
 	 * Instantiates a new client player.
 	 *
 	 * @param playerName the player name
 	 * @param mapWidth the map width
 	 * @param mapHeight the map height
 	 * @param networkID the network id
 	 */
 	public ClientPlayer(final String playerName, final int mapWidth, final int mapHeight, final int networkID) {
 		this.playerName = playerName;
 		this.boardWidth = mapWidth;
 		this.boardHeight = mapHeight;
 		this.networkID = networkID;
 	}
 	/**
 	 * This method is called at the beginning of the game to determine
 	 * where the player wants to place his ships. For each ship set the
 	 * starting position and the direction in which the ship's length will extend. Make sure
 	 * ships are not overlapping and are within the defined bounds of the game map.
 	 * 
 	 * @param shipList A list of ships with all null attributes
 	 * @return the shipList with updated values for the starting position
 	 * and direction the ship is facing
 	 */
 	public List<Ship> placeShips(List<Ship> shipList) {
 		List<Integer> shipRow = new ArrayList<Integer>();
 		int row = 0;
 		for (Ship ship: shipList) {
 			while (shipRow.contains(row)) {
 				row = (int) (Math.random() * (boardHeight-1));
 			}
 			shipRow.add(row);
 			ship.setStartPosition(new Coordinate(row,0));
 			ship.setDirection(Ship.Direction.EAST);
 			System.out.println("Ship : " + ship.getStartPosition());
 		}
 		System.out.println("placed ships");
 		
 		return shipList;
 	}
 	
 	/**
 	 * This class will be filled in by the player. All logic regarding in game decisions to be
 	 * made by your bot should be put in here. This class will be called every turn until the
 	 * end of the game
 	 *
 	 * @param myShips List of ships belonging to player
 	 * @param actionResults the action results
 	 * @return a List of coordinates corresponding to where you wish to fire
 	 */
 	public List<ShipAction> doTurn(Map<Integer, List<Ship>> ships, Map<Integer, List<ActionResult>> actionResults) {
 		List<ShipAction> actions = new LinkedList<ShipAction>();
 		List<Ship> myShips = ships.get(networkID);
 		for (Ship s : myShips) {
 			List<Coordinate> shotCoordinates = new ArrayList<Coordinate>();
 			List<Direction> moves = new LinkedList<Direction>();
 			shotCoordinates.add(new Coordinate((int) (Math.random() * (boardHeight -1)), (int) (Math.random() * (boardWidth - 1))));
 			while (s.distanceFromCenter(shotCoordinates.get(0)) > s.getRange()) {
 				shotCoordinates.remove(0);
 				shotCoordinates.add(new Coordinate((int) (Math.random() * (boardHeight -1)), (int) (Math.random() * (boardWidth - 1))));
 			}
 				
 			if (Math.random() > 0.5)
 				moves.add(Direction.EAST);
 			else
 				moves.add(Direction.WEST);
 			actions.add(new ShipAction(s.getIdentifier(), shotCoordinates, moves));
 		}
 		System.out.println("Actions : " + actions);
 		System.out.println("Ships : " + ships);
		try {
		Thread.sleep(1500);
		} catch (InterruptedException e) {}
 		return actions;
 	}
 }
 
