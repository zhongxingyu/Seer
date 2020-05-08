 package essentials.objects;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlTransient;
 
import essentials.core.ScrabbleLogger;
 import essentials.enums.LetterEnum;
import essentials.enums.LogLevel;
 import essentials.enums.OrientationEnum;
 import essentials.interfaces.Cachable;
 import essentials.interfaces.Cloneable;
 
 
 public class ScrabbleMap implements Cloneable, Cachable {
 
 	protected Boolean cache_isClear = null;
 	protected String cache_toString = null;
 	
 	/**
 	 * Number of rows of the map
 	 */
 	protected int rows = 11;
 	
 	/**
 	 * Number of columns of the map
 	 */
 	protected int columns = 11;
 	
 	/**
 	 * Map array
 	 * [rows][columns]
 	 * Starting with [0][0] as top left corner of map
 	 */
 	@XmlElement
 	protected Brick[][] map;
 	
 	/**
 	 * Constructor
 	 */
 	public ScrabbleMap() {
 	}
 	
 	/**
 	 * Constructor
 	 * @param aColumns Number of columns of the map
 	 * @param aRows Number of rows of the map
 	 */
 	public ScrabbleMap(int aRows, int aColumns){
 		rows = aRows;
 		columns = aColumns;
 		initMap();
 	}
 	
 	/**
 	 * Adds a list of bricks
 	 * @param aBrickList Bricklist
 	 * @return List of new words
 	 */
 	public List<BrickList> addBricks(BrickList aBrickList){
 		
 		BrickList word = new BrickList();
 		List<BrickList> words = new ArrayList<BrickList>();
 		
 		int aRow = aBrickList.getRow();
 		int aCol = aBrickList.getColumn();
 		OrientationEnum aOrientation = aBrickList.getOrientation();
 		
 		if( aOrientation == OrientationEnum.HORIZONTAL ){			
 			word = addBricksHorizontal( aBrickList );
 			words = getWordsByLength(aRow, aCol, aBrickList.size(), aOrientation);
 		}
 		else if( aOrientation == OrientationEnum.VERTICAL ){
 			word = addBricksVertical( aBrickList );	
 			words = getWordsByLength(aRow, aCol, aBrickList.size(), aOrientation);
 		}
 
 		if( word.size() > 0 ){
 			words.add(word);
 		}
 		else {
 			words = new ArrayList<BrickList>();
 		}
 		
 		resetCache();
 		
 		return words;
 	}
 	
 	/**
 	 * Adds bricks in horizontal direction
 	 * @param aBrickList
 	 * @param aRow
 	 * @param aCol
 	 * @return Complete new horizontal word
 	 * Cache is not reseted!
 	 */
 	protected BrickList addBricksHorizontal( BrickList aBrickList ){
 		
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		int x = col;
 		BrickList word = new BrickList();
 		
 		ScrabbleLogger.log( "\tisWordPlaceable: " + isWordPlaceable(aBrickList), LogLevel.DEBUG );
 		
 		if( isWordPlaceable(aBrickList) ){
 			for( Brick b : aBrickList.getBricks() ){
 				
 				if( isEmpty(row, x) ){
 					map[row][x] = b.clone();
 				}				
 				x++;
 					
 			}
 			
 			word = getWordHorizontal(row, col);
 		}
 		
 		return word;
 	}
 	
 	/**
 	 * Adds bricks in vertical direction
 	 * @param aBrickList
 	 * @param aRow
 	 * @param aCol
 	 * @return Complete new verticla word
 	 * Cache is not reseted!
 	 */
 	protected BrickList addBricksVertical( BrickList aBrickList ){
 		
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		int y = row;
 		BrickList word = new BrickList();
 		
 		if( isWordPlaceable(aBrickList) ){
 			for( Brick b : aBrickList.getBricks() ){
 				
 				if( isEmpty(y, col) ){
 					map[y][col] = b.clone();
 				}
 				y++;
 				
 			}
 			
 			word = getWordVertical(row, col);
 		}
 		
 		return word;
 	}
 	
 	
 	/**
 	 * Gets all words from one starting point into one direction by a specific length
 	 * @param aRow	Start row
 	 * @param aCol	Start column
 	 * @param aLength Length to iterate over
 	 * @param aOrientation	Orientation of iteration
 	 * @return List of words
 	 */
 	protected List<BrickList> getWordsByLength(int aRow, int aCol, int aLength, OrientationEnum aOrientation){
 		List<BrickList> rWordList = new ArrayList<BrickList>();
 		BrickList word = new BrickList();
 		
 		if( aOrientation == OrientationEnum.HORIZONTAL ){
 			
 			for(int x = aCol; x <= (aCol + aLength - 1); x++ ){
 				word = getWordVertical(aRow, x);
 				if( word.size() > 0 )
 					rWordList.add(word);
 			}
 				
 		}
 		else if( aOrientation == OrientationEnum.VERTICAL ){
 			
 			for( int y = aRow; y <= (aRow + aLength - 1); y++ ){
 				word = getWordHorizontal(y, aCol);
 				if( word.size() > 0 )
 					rWordList.add(word);
 			}
 			
 		}
 		
 		return rWordList;
 	}
 	
 	/**
 	 * Gets the complete word in horizonal direction
 	 * @param aRow Start row
 	 * @param aCol Start column
 	 * @return String of the horizontal word
 	 */
 	public BrickList getWordHorizontal(int aRow, int aCol){
 		BrickList word = new BrickList();
 		
 		// find right and left border
 		int borderLeft = aCol;
 		int borderRight = aCol;
 		
 		if( (aRow >= rows) || (aRow < 0) ){
 			return word;
 		}
 		
 		while( (borderLeft-1 > 0) && (map[aRow][borderLeft-1] != null) )
 			borderLeft--;
 		while( (borderRight+1 < columns) && (map[aRow][borderRight+1] != null) )
 			borderRight++;
 		
 		if( borderRight - borderLeft > 0 ){		
 			// slice word
 			for(int i = borderLeft; i <= borderRight; i++){
 				word.add( map[aRow][i] );
 			}
 		}
 		
 		return word;
 	}
 	
 	
 	
 	/**
 	 * Gets the complete word in vertical direction
 	 * @param aRow Start row
 	 * @param aCol Start column
 	 * @return String of the horizontal word
 	 */
 	public BrickList getWordVertical(int aRow, int aCol){
 		BrickList word = new BrickList();
 		
 		// find top and bottom border
 		int borderBottom = aRow;
 		int borderTop = aRow;
 		
 		if( (aCol >= columns) || (aCol < 0) ){
 			return word;
 		}
 		
 		while( (borderBottom-1 > 0) && (map[borderBottom-1][aCol] != null) )
 			borderBottom--;
 		while( (borderTop+1 < rows) && (map[borderTop+1][aCol] != null) )
 			borderTop++;
 		
 		if( borderTop - borderBottom > 0 ){		
 			// slice word
 			for(int i = borderBottom; i <= borderTop; i++){
 				word.add( map[i][aCol] );
 			}
 		}
 		
 		return word;
 	}
 	
 	
 	/**
 	 * Removes bricks from map
 	 * @param aBrickList
 	 * @return true if successfull, otherwise false
 	 */
 	public boolean removeBricks(BrickList aBrickList) {
 		OrientationEnum orientation = aBrickList.getOrientation();
 		boolean ret = false;
 		
 		if( orientation == OrientationEnum.HORIZONTAL ){
 			ret = removeBricksHoriontal( aBrickList );
 		}
 		else if( orientation == OrientationEnum.VERTICAL ){
 			ret = removeBricksVertical( aBrickList );
 		}
 		
 		resetCache();
 		return ret;
 	}
 	
 	
 	/**
 	 * Removes bricks in horizontal direction
 	 * @param aBrickList
 	 * @return true if successfull, otherwise false
 	 * Cache is not reseted!
 	 */
 	protected boolean removeBricksHoriontal(BrickList aBrickList){
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		int length = aBrickList.size();
 
 		if( (col + length <= columns) && (row < rows) && 
 				(col >= 0) && (row >= 0) ){
 			
 			for( Brick b : aBrickList.getBricks() ){
 				if( (b.getWeight() >= 0)
 						&& (map[row][col] != null && map[row][col].getLetter() == b.getLetter()) ){
 					map[row][col] = null;
 				}
 				col++;
 			}
 			return true;
 			
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Removes bricks in vertical direction
 	 * @param aBrickList
 	 * @return true if successfull, otherwise false
 	 * Cache is not reseted!
 	 */
 	protected boolean removeBricksVertical(BrickList aBrickList){
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		int length = aBrickList.size();
 		
 		if( (col < columns) && (row + length <= rows) && 
 				(col >= 0) && (row >= 0) ){
 			
 			for( Brick b : aBrickList.getBricks() ){
 				if( b.getWeight() >= 0
 						&& (map[row][col] != null && map[row][col].getLetter() == b.getLetter()) ){
 					map[row][col] = null;
 				}
 				row++;
 			}			
 			return true;
 			
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * @param aRow
 	 * @param aCol
 	 * @return True if position is empty, otherwise false
 	 */
 	protected boolean isEmpty(int aRow, int aCol){
 		if( aRow < rows && aCol < columns && map[aRow][aCol] == null ){
 			return true;
 		}
 		return false;
 	}
 	
 	
 	/**
 	 * Checks if there's enough space for a bricklist
 	 * @param aBrickList
 	 * @return true if there's enough space
 	 */
 	public boolean isEnoughSpace(BrickList aBrickList){
 		if( aBrickList.getOrientation() == OrientationEnum.HORIZONTAL ){
 			return isEnoughSpaceHorizontal( aBrickList );
 		}
 		return isEnoughSpaceVertical( aBrickList );
 	}
 	
 	/**
 	 * Checks if there's enough space for a bricklist in vertical direction
 	 * @param aBrickList
 	 * @return true if there's enough space
 	 */
 	protected boolean isEnoughSpaceVertical(BrickList aBrickList){
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		
 		if( row + aBrickList.size() <= rows ){
 			for( Brick b : aBrickList.getBricks() ){
 				if( !isBrickPlaceable(row, col, b) ){
 					return false;
 				}
 				row++;
 			}
 		}
 		else{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Checks if ther's enough space for a bricklist in horizontal direction
 	 * @param aBrickList
 	 * @return
 	 */
 	protected boolean isEnoughSpaceHorizontal(BrickList aBrickList){
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		
 		if( col + aBrickList.size() <= columns ){
 			for( Brick b : aBrickList.getBricks() ){
 				if( !isBrickPlaceable(row, col, b) ){
 					return false;
 				}
 				col++;
 			}
 		}
 		else{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Checks if a brick could be placed at the specified position
 	 * @param aRow
 	 * @param aCol
 	 * @param aBrick
 	 * @return true if brick is placeable, otherwise false
 	 */
 	protected boolean isBrickPlaceable(int aRow, int aCol, Brick aBrick){
 		boolean A = (map[aRow][aCol] != null);
 		boolean B = (A && aBrick.getLetter() != LetterEnum.NULL
 					&& map[aRow][aCol].getLetter() == aBrick.getLetter() && aBrick.getWeight() < 0);
 		return ( !A || B );
 	}
 	
 	
 	/**
 	 * Checks if a word could placed at a specific position
 	 * @param aBrickList bricklist to place
 	 * @return true if everything is ok, otherwise false
 	 */
 	public boolean isWordPlaceable(BrickList aBrickList){
 		boolean ret = false;
 		int row = aBrickList.getRow();
 		int col = aBrickList.getColumn();
 		int length = aBrickList.size();
 		OrientationEnum orientation = aBrickList.getOrientation();
 		
 		// check if word start position is out of bounds
 		if( row < 0 || col < 0 || row >= rows || col >= columns ){
 			return false;
 		}
 		
 		if( isClear() ){
 			// first word placed
 			if( (orientation == OrientationEnum.HORIZONTAL)
 					&& (col <= columns/2) && ( col+length >= columns/2 )
 					&& (row == rows/2) )
 				ret = true;
 			if( (orientation == OrientationEnum.VERTICAL)
 					&& (row <= rows/2) && ( row+length >= rows/2 )
 					&& (col == columns/2) )
 				ret = true;
 		}
 		else{
 			// check if word is added to other words
 			ret = isEnoughSpace(aBrickList);
 		}
 		
 		return ret;
 	}
 	
 	
 	/**
 	 * Returns if a brick is on the map or not
 	 * @return true if no bricks are on the map
 	 */
 	public boolean isClear() {
 		if( cache_isClear == null ){
 			
 			if( map != null ){
 		
 				for(int row = 0; row < rows; row++){
 					for(int col = 0; col < columns; col++){				
 						if(map[row][col] != null){
 							cache_isClear = false;
 							return cache_isClear;
 						}
 					}
 				}
 				
 				cache_isClear = true;
 			}
 			else{
 				cache_isClear = true;
 			}
 			
 		}
 		
 		return Boolean.valueOf( cache_isClear );
 	}
 	
 	
 	/**
 	 * Initiates map (clears existing map)
 	 */
 	protected void initMap(){
 		map = new Brick[rows][columns];
 		resetCache();
 	}
 
 	/**
 	 * @return the rows
 	 */
 	@XmlTransient
 	public int getRows() {
 		return rows;
 	}
 
 	/**
 	 * @param rows the rows to set (clears existing map)
 	 */
 	public void setRows(int rows) {
 		this.rows = rows;
 		initMap();
 	}
 
 	/**
 	 * @return the columns
 	 */
 	@XmlTransient
 	public int getColumns() {
 		return columns;
 	}
 
 	/**
 	 * @param columns the columns to set (clears existing map)
 	 */
 	public void setColumns(int columns) {
 		this.columns = columns;
 		initMap();
 	}
 
 	/**
 	 * @return the map
 	 */
 	@XmlTransient
 	public Brick[][] getMap() {
 		return map;
 	}
 
 	/**
 	 * @param aMap the map to set
 	 */
 	public void setMap(Brick[][] aMap) {
 		map = aMap;
 		rows = aMap.length;
 		columns = aMap[0].length;
 		resetCache();
 	}
 	
 	
 	/**
 	 * Returns Map as String
 	 */
 	public String toString(){
 		if( cache_toString == null ){
 			cache_toString = getMapAsString();
 		}
 		return cache_toString;
 	}
 	
 	
 	/**
 	 * Returns Map as String
 	 */
 	private String getMapAsString(){
 		String rString = "  |";
 		int y = 0;
 		
 		if( map == null ){
 			return "-no map-";
 		}
 		
 		for( int x = 0; x < columns; x++ ){
 			rString = rString.concat( String.format("%1$" + 5 + "s", Integer.toString(x) + "|") );
 		}
 		rString = rString.concat("\n");
 		
 		for( Brick[] row : map ){
 			
 			rString = rString.concat( String.format("%1$" + 3 + "s", Integer.toString(y) + "|") );
 			
 			for( Brick col : row ){
 				
 				if( col != null ){
 					rString = rString.concat("[" + col.getLetter() + "_" + col.getWeight() + "]");
 				}
 				else{
 					rString = rString.concat("[ - ]");
 				}
 			}
 			rString = rString.concat("\n");
 			y++;
 		}
 		
 		return rString;
 	}
 
 	@Override
 	public void resetCache() {
 		cache_isClear = null;
 		cache_toString = null;
 	}
 	
 	
 	/**
 	 * Clones this object
 	 */
 	public ScrabbleMap clone(){
 		ScrabbleMap newScrabbleMap = new ScrabbleMap();
 		Brick[][] newMap = new Brick[rows][columns];
 		
 		for( int row=0; row < rows; row++ )
 		{
 			for( int col=0; col < columns; col++ ){
 				Brick b = map[row][col];
 				if( b != null )
 					newMap[row][col] = b.clone();
 			}
 		}
 		
 		newScrabbleMap.setMap(newMap);		
 		return newScrabbleMap;
 	}
 	
 }
