 package internal.space;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import internal.piece.PieceFactory;
 import internal.piece.TileInterfaceType;
 import internal.tree.IWorldTree;
 import internal.tree.IWorldTree.ITile;
 import internal.tree.WorldTreeFactory.Tile;
 import static internal.Helper.multiLine;
 
 /**
  * The Space class aims to provide methods to traverse the space easily.
  * The Space class consists of Tiles in a 2-Dimensional array.
  * <p>
  * This implementation of Space tries to emulate Cartesian behavior.
  * All methods of access - read/write can be passed in with Cartesian coordinates instead of array indices
  * @author Guru
  *
  */
 public class Space extends Dimension {
 	private static final long serialVersionUID = -3788398250352166358L;
 	
 	protected Coordinates current;
 	protected ITile[][] matrix;
 	private List<String> stringRepresentation;
 	private List<String> columnRepresentation;
 	
 	public Space() {
 		super(0, 0);
 	}
 	public Space(int x, int y) {
 		super(y, x);
 		matrix = new Tile[y][x];
 		current = new Coordinates(true, 0, 0);
 		stringRepresentation = new ArrayList<String>();
 		columnRepresentation = new ArrayList<String>();
 	}
 	
 	/**
 	 * Returns the next Tile to the left
 	 * @return {@code Tile} object to the left of the current Tile
 	 */
 	public ITile nextLeft() {
 		assert(current.x - 1 != -1);
 		return matrix[current.y][current.x - 1];
 	}
 	
 	/**
 	 * Returns the next Tile to the right
 	 * @return {@code Tile} object to the right of the current Tile
 	 */
 	public ITile nextRight() {
 		assert(current.x + 1 != yDimension);
 		return matrix[current.y][current.x + 1];
 	}
 	
 	/**
 	 * Returns the next Tile on top of the current Tile
 	 * @return {@code Tile} object to the top of the current Tile
 	 */
 	public ITile nextUp() {
 		assert(current.y - 1 != -1);
 		return matrix[current.y - 1][current.x];
 	}
 	
 	/**
 	 * Returns the next Tile below the current Tile
 	 * @return {@code Tile} object below the current Tile
 	 */
 	public ITile nextDown() {
 		assert(current.y + 1 != xDimension);
 		return matrix[current.y + 1][current.x];
 	}
 
 	/**
 	 * Returns the current Tile
 	 * @return {@code Tile} object referencing the current Tile
 	 */
 	public ITile currentTile() {
 		return matrix[current.x][current.y];
 	}
 	
 	/**
 	 * Obtain a copy of the current coordinates
 	 * @return {@code Coordinates} object containing the values of the current coordinates
 	 */
 	public Coordinates currentCoordinates() {
 		assert(current.cartesian == true);
 		return new Coordinates(current);
 	}
 	
 	/**
 	 * Retrieve the Cell that is being represented by the given set of Cartesian coordinates
 	 * @param {@code Coordinates} representing the desired ITile
 	 * @return {@code ITile} representing the given set of coordinates
 	 */
 	public ITile getByCoord(Coordinates coordinates) {
 		assert (coordinates.cartesian == true);
 		if(!validate(coordinates))
 			return null;
 		return matrix[yDimension - coordinates.y - 1][coordinates.x];
 	}
 	
 	/**
 	 * Retrieve the Cell that is being represented by the given set of array indices
 	 * @param {@code Coordinates} representing the desired ITile 
 	 * @return {@code ITile} representing the given set of coordinates
 	 */
 	public ITile getByArray(Coordinates coordinates) {
 		assert(coordinates.cartesian == false);
 		return matrix[coordinates.y][coordinates.x];
 	}
 
 	/**
 	 * Current x-coordinate
 	 * @return {@code Integer} holding the current x-coordinate
 	 */
 	public int xCoord() {
 		return current.x;
 	}
 	
 	/**
 	 * Current y-coordinate
 	 * @return {@code Integer} holding the current y-coordinate
 	 */
 	public int yCoord() {
 		return current.y;
 	}
 	
 	/**
 	 * Set a given tile specified by Cartesian coordinates
 	 * @param {@code Coordinates} containing the necessary location coordinates
 	 * @param tile {@code ITile} object that is to be set in the given coordinates
 	 */
 	public void setByCoord(Coordinates coordinates, ITile tile) {
 		assert(coordinates.cartesian == true);
 		Coordinates indices = coordToArray(coordinates);
 		setByArray(indices, tile);
 	}
 	
 	/**
 	 * Set a given tile specified by array indices
 	 * @param {@code Coordinates} containing the necessary location coordinates
 	 * @param tile {@code ITile} object that is to be set in the given coordinates
 	 */
 	public void setByArray(Coordinates coordinates, ITile tile) {
 		assert(coordinates.cartesian == false);
 		matrix[coordinates.y][coordinates.x] = tile;
 		updateStringRepresentation(coordinates);
 	}
 
 	/**
 	 * Obtain a collection of all {@code ITile} objects used in this Space
 	 * @return {@code List<IWorldTree} containing every {@code ITile} from this Space
 	 */
 	public Collection<IWorldTree> collection() {
 		List<IWorldTree> returnList = new ArrayList<IWorldTree>();
 		for(int i = 0; i < getYDimension(); i++) {
 			for(int j = 0; j < getXDimension(); j++) {
 				Coordinates coordinates = new Coordinates(true, j, i);
 				ITile tile = getByCoord(coordinates); 
 				if(tile != null)
 					returnList.add(tile);
 			}
 		}
 		return returnList;
 	}
 
 	/**
 	 * Check surrounding entries in the space to find out valid interfaces. Also check for corner cases (literally)
 	 * @param {@code Coordinates} containing the necessary location coordinates
 	 * @return {@code String} containing set of valid interfaces
 	 */
 	public Map<String, String> getValidInterfaces(Coordinates coordinates) {
 		Coordinates indices = coordinates;
 		if(coordinates.cartesian)
 			indices = coordToArray(coordinates);
 		
 		int xIndex = indices.x;
 		int yIndex = indices.y;
 		
 		StringBuffer mandatoryInterfaces = new StringBuffer();
 		StringBuffer invalidInterfaces = new StringBuffer();
 		if(yIndex + 1 == yDimension)
 			invalidInterfaces.append("D");
 		else if(matrix[yIndex + 1][xIndex] == null);
 		else if(matrix[yIndex + 1][xIndex].piece().getText().equals(""));
 		else if(matrix[yIndex + 1][xIndex].piece().hasInterface(TileInterfaceType.U))
 			mandatoryInterfaces.append("D");
 		else
 			invalidInterfaces.append("D");
 		
 		if(xIndex - 1 < 0)
 			invalidInterfaces.append("L");
 		else if(matrix[yIndex][xIndex - 1] == null);
 		else if(matrix[yIndex][xIndex - 1].piece().getText().equals(""));
 		else if(matrix[yIndex][xIndex - 1].piece().hasInterface(TileInterfaceType.R))
 			mandatoryInterfaces.append("L");
 		else
 			invalidInterfaces.append("L");
 		
 		
 		if(xIndex + 1 == xDimension)
 			invalidInterfaces.append("R");
 		else if(matrix[yIndex][xIndex + 1] == null);
 		else if(matrix[yIndex][xIndex + 1].piece().getText().equals(""));
 		else if(matrix[yIndex][xIndex + 1].piece().hasInterface(TileInterfaceType.L))
 			mandatoryInterfaces.append("R");
 		else
 			invalidInterfaces.append("R");
 		
 		
 		if(yIndex - 1 < 0)
 			invalidInterfaces.append("U");
 		else if(matrix[yIndex - 1][xIndex] == null);
 		else if(matrix[yIndex - 1][xIndex].piece().getText().equals(""));
 		else if(matrix[yIndex - 1][xIndex].piece().hasInterface(TileInterfaceType.D))
 			mandatoryInterfaces.append("U");
 		else
 			invalidInterfaces.append("U");
 		
 		Map<String, String> interfaceMap = new HashMap<String, String>();
 		interfaceMap.put("mandatoryInterfaces", mandatoryInterfaces.toString());
 		interfaceMap.put("invalidInterfaces", invalidInterfaces.toString());
 		return interfaceMap;
 	}
 	
 	public List<String> getStringRepresentation() {
 //		Initial settings
 		if(columnRepresentation.size() == 0) {
 			for(int i = 0; i < xDimension; i++) {
 				StringBuffer column = new StringBuffer();
 				for(int j = 0; j < yDimension; j++) {
 					Coordinates currentCoords = new Coordinates(false, i, j);
 					ITile tile = getByArray(currentCoords);
 					if(tile == null)
 						column.append(PieceFactory.newPiece("").toString());
 					else {
 						for(String s : tile.getStringRepresentation()) {
 							column.append(s + "\n");
 						}
 					}
 				}
 				columnRepresentation.add(column.toString());
 			}
 		}
 		return stringRepresentation;
 	}
 	
 	private void updateStringRepresentation(Coordinates coordinates) {
 		if(columnRepresentation.size() == 0) {
 			getStringRepresentation();
 			return;
 		}
 		
 //		TODO: First we find the 'line' that holds this tile (using coordinates.y)
 		if(coordinates.cartesian())
 			coordinates = coordToArray(coordinates);
 		
 		String visualToReplace = columnRepresentation.get(coordinates.x());
 		
 		List<String> subStrings = new ArrayList<String>(Arrays.asList(visualToReplace.split("\n")));
//		FIXME: Should not be hard-coded as length 7
		int replaceIndex = coordinates.y() * 7;
 		
 		ITile tile = getByArray(coordinates);
 		
 		StringBuffer sb = new StringBuffer();
 		for(String s : tile.getStringRepresentation())
 			sb.append(s + "\n");
 		
 		String[] newVisual = sb.toString().split("\n");
 		for(int i = 0; i < 5; i++) {
 			subStrings.remove(replaceIndex + i);
 			subStrings.add(replaceIndex + i, newVisual[i]);
 		}
 		
 		sb.delete(0, sb.length());
 		
 		for(String s : subStrings)
 			sb.append(s + "\n");
 		
 		columnRepresentation.remove(coordinates.x());
 		columnRepresentation.add(coordinates.x(), sb.toString());
 		
 		stringRepresentation.removeAll(stringRepresentation);
 
 		for(String s : multiLine(columnRepresentation).split("\n")) {
 			stringRepresentation.add(s);
 		}
 
 	}
 
 	/**
 	 * Set current coordinates
 	 * @param {@code Coordinates} containing the new current coordinates
 	 */
 	public void setCurrentCoordinates(Coordinates current) {
 		assert(current.cartesian == true);
 		updateCurrentVisual(current);
 		this.current = current;
 		
 	}
 	
 	/**
 	 * Get a {@code Collection} of neighbouring tiles
 	 * @param {@code Coordinates} containing the necessary location coordinates
 	 * @return {@code Collection<ITile>} containing neighbouring tiles
 	 */
 	public Collection<ITile> getNeighbours(Coordinates coordinates) {
 		
 //		pre-processing
 		Coordinates indices = coordToArray(coordinates);
 		int x = indices.x;
 		int y = indices.y;
 		
 		Collection<ITile> returnCollection = new ArrayList<ITile>();
 		
 		Coordinates oldCurrent = currentCoordinates();
 		setCurrentCoordinates(new Coordinates(false, x, y));
 		returnCollection.add(nextUp());
 		returnCollection.add(nextDown());
 		returnCollection.add(nextLeft());
 		returnCollection.add(nextRight());
 		
 		setCurrentCoordinates(oldCurrent);
 		
 		return returnCollection;
 	}
 	
 	/**
 	 * Get a {@code Collection} of neighboring tiles from current tile
 	 * @return {@code Collection<ITile>} containing neighboring tiles
 	 */
 	public Collection<ITile> getNeighbours() {
 		return getNeighbours(current);
 	}
 	
 	/**
 	 * Get all the valid directions allowed by this Space
 	 * @return {@code Collection<Direction>} containing the valid directions as defined by the private enum {@code Direction}
 	 */
 	public static Collection<Direction> listDirections() {
 		return Direction.listDirections();
 	}
 	
 	/**
 	 * The Direction enum is to be used while specifying directions related to the matrix represented by Space.
 	 * @author guru
 	 *
 	 */
 	public enum Direction {
 		NW("NW"),
 		N("N"),
 		NE("NE"),
 		E("E"),
 		SE("SE"),
 		S("S"),
 		SW("SW"),
 		W("W")
 		;
 		
 		private String choice;
 		private Direction(String choice) {
 			this.choice = choice;
 		}
 		
 		/**
 		 * List all the directions in this enum
 		 * @return {@code Collection<Direction>} containing all the directions of this enum
 		 */
 		public static Collection<Direction> listDirections() {
 			Collection<Direction> collection = new ArrayList<Direction>();
 			for(Direction d : values()) {
 				collection.add(d);
 			}
 			
 			return collection;
 		}
 		
 		/**
 		 * Get the Direction corresponding to the specified parameter
 		 * @param choice {@code String} containing the textual representation of a Direction
 		 * @return {@code Direction} represented by the parameter<br>
 		 * <b>null</b> if there is no Direction corresponding to the parameter
 		 */
 		public static Direction getDirection(String choice) {
 			for(Direction d : values()) {
 				if(d.choice.equals(choice))
 					return d;
 			}
 			
 			throw new IllegalArgumentException("No such direction " + choice + "\n" +
 					"Valid choices are :" + listDirections().toString() );
 		}
 		
 		@Override public String toString() {
 			return choice;
 		}
 	}
 
 	/**
 	 * Validates the given coordinates against the defined space. Allows <b>only</b> Cartesian coordinates
 	 * @param {@code Coordinates} containing the location coordinates that needs to be validated
 	 * @return {@code true} if valid, {@code false} otherwise
 	 */
 	public boolean validate(Coordinates coordinates) {
 		assert(coordinates.cartesian == true);
 		if(coordinates.x >= 0 && coordinates.x < xDimension && coordinates.y >= 0 && coordinates.y < yDimension)
 			return true;
 		else
 			return false;
 	}
 	
 	/**
 	 * Convert a Coordinates object from array format to Cartesian format
 	 * @param coordinates {@code Coordinates} object representing the array format
 	 * @return {@code Coordinates} object representing the Cartesian format
 	 */
 	public Coordinates arrayToCoord(Coordinates coordinates) {
 		assert(coordinates.cartesian == false);
 		Coordinates newCoordinates = new Coordinates(true, coordinates.x, yDimension - coordinates.y - 1);
 		return newCoordinates;
 	}
 	
 	/**
 	 * Convert a Coordinates object from Cartesian format to array format
 	 * @param coordinates {@code Coordinates} object representing the Cartesian format
 	 * @return {@code Coordinates} object representing the array format
 	 */
 	public Coordinates coordToArray(Coordinates coordinates) {
 		assert(coordinates.cartesian == true);
 		Coordinates newCoordinates = new Coordinates(false, coordinates.x, yDimension - coordinates.y - 1);
 		return newCoordinates;
 	}
 
 	/**
 	 * Update the visual of current tile to explicitly represent the current tile <br>
 	 * This helps while running the UI debug suite
 	 * @param coordinates {@code Coordinates} containing the tile whose visual is to be modified
 	 */
 	public void updateCurrentVisual(Coordinates coordinates) {
 		StringBuffer sb = new StringBuffer();
 		
 //		First remove it from the old tile
 		ITile currentTile = getByCoord(current);
 		
 //		This is required as this.current is pointing to C(0,0) upon initialization 
 		if(currentTile != null) {
 			currentTile.removeFromVisual("CT");
 			updateStringRepresentation(current);
 		}
 		
 //		Now do the reverse in the new tile
 		sb.delete(0, sb.length());
 		ITile newCurrentTile = getByCoord(coordinates);
 		newCurrentTile.addToVisual("CT");
 		
 		updateStringRepresentation(coordinates);
 	}
 }
