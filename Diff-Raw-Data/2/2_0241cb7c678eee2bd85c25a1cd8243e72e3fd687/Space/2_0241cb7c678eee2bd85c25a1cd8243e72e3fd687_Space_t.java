 package internal.space;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import internal.piece.PieceFactory;
 import internal.piece.TileInterfaceType;
 import internal.tree.IWorldTree;
 import internal.tree.IWorldTree.ITile;
 import internal.tree.WorldTreeFactory.Tile;
 
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
 	protected Coordinates current;
 	protected ITile[][] matrix;
 	private List<String> stringRepresentation;
 	
 	public Space(int x, int y) {
 		super(y, x);
 		matrix = new Tile[y][x];
 		current = new Coordinates(true, 0, yDimension - 1);
 		stringRepresentation = new ArrayList<String>();
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
 		return matrix[coordinates.x][coordinates.y];
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
 		matrix[indices.y][indices.x] = tile;
 		updateStringRepresentation(indices.x, indices.y);
 	}
 	
 	/**
 	 * Set a given tile specified by array indices
 	 * @param {@code Coordinates} containing the necessary location coordinates
 	 * @param tile {@code ITile} object that is to be set in the given coordinates
 	 */
 	public void setByArray(Coordinates coordinates, ITile tile) {
 		assert(coordinates.cartesian == false);
 		matrix[coordinates.x][coordinates.y] = tile;
 		updateStringRepresentation(coordinates.x, coordinates.y);
 	}
 
 	/**
 	 * Obtain a collection of all {@code ITile} objects used in this Space
 	 * @return {@code List<IWorldTree} containing every {@code ITile} from this Space
 	 */
 	public List<IWorldTree> collection() {
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
 		else if(matrix[yIndex + 1][xIndex].piece().hasInterface(TileInterfaceType.U))
 			mandatoryInterfaces.append("D");
 		else
 			invalidInterfaces.append("D");
 		
 		if(xIndex - 1 < 0)
 			invalidInterfaces.append("L");
 		else if(matrix[yIndex][xIndex - 1] == null);
 		else if(matrix[yIndex][xIndex - 1].piece().hasInterface(TileInterfaceType.R))
 			mandatoryInterfaces.append("L");
 		else
 			invalidInterfaces.append("L");
 		
 		
 		if(xIndex + 1 == xDimension)
 			invalidInterfaces.append("R");
 		else if(matrix[yIndex][xIndex + 1] == null);
 		else if(matrix[yIndex][xIndex + 1].piece().hasInterface(TileInterfaceType.L))
 			mandatoryInterfaces.append("R");
 		else
 			invalidInterfaces.append("R");
 		
 		
 		if(yIndex - 1 < 0)
 			invalidInterfaces.append("U");
 		else if(matrix[yIndex - 1][xIndex] == null);
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
 		stringRepresentation.removeAll(stringRepresentation);
 		List<List<String>> listStringList = new ArrayList<List<String>>();
 		for(int i = 0; i < getYDimension(); i++) {
 			List<String> stringList = new ArrayList<String>();
 			for(int j = 0; j < getXDimension(); j++) {
 				Coordinates currentCoords = new Coordinates(false, i, j);
 				if(getByArray(currentCoords) != null)
 					stringList.add(getByArray(currentCoords).piece().toString());
 				else
 					stringList.add(PieceFactory.newPiece("").toString());
 			}
 			listStringList.add(stringList);
 		}
 		
 //		We use one instance of a piece's toString() to test for number of lines.
 		int lineCount = listStringList.get(0).get(0).split("\n").length;
 		
 		for(int yIndex = 0; yIndex < listStringList.size(); yIndex++) {
 			List<String> stringList = listStringList.get(yIndex);
 			for(int lineIndex = 0; lineIndex < lineCount; lineIndex++) {
 				StringBuffer fullLine = new StringBuffer(); 
 				for(int xIndex = 0; xIndex < stringList.size(); xIndex++) {
 					String[] stringArray = null;
 					try {
 						 stringArray = stringList.get(xIndex).split("\n");
 						fullLine.append(stringArray[lineIndex] + " ");
 					} catch(ArrayIndexOutOfBoundsException e) {
 						System.err.println("size :" + stringList.size() + "\n" + stringList.get(xIndex));
 						e.printStackTrace();
 					}
 					
 				}
 //				if(!stringRepresentation.contains(fullLine.toString()))
 					stringRepresentation.add(fullLine.toString());
 			}
 		}
 		return stringRepresentation;
 	}
 	
 	private void updateStringRepresentation(int xIndex, int yIndex) {
 //		TODO: First we find the 'line' that holds this tile (using yIndex)
 		getStringRepresentation();
 	}
 
 	/**
 	 * Set current coordinates
 	 * @param {@code Coordinates} containing the new current coordinates
 	 */
 	public void setCurrentCoordinates(Coordinates current) {
 		assert(current.cartesian == true);
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
 //		coordinates.cartesian = true;
 //		int xCoord = coordinates.x;
 //		int yCoord = coordinates.y;
 //		coordinates.y = xCoord;
 //		coordinates.x = (yDimension - yCoord - 1);
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
 //		int xCoord = coordinates.x;
 //		int yCoord = coordinates.y;
 //		coordinates.cartesian = false;
 //		coordinates.x = yCoord;
 //		coordinates.y = yDimension - xCoord - 1; 
 		return newCoordinates;
 	}
 }
