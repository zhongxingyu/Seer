 package ca.couchware.wezzle2d;
 
 import ca.couchware.wezzle2d.animation.AnimationManager;
 import static ca.couchware.wezzle2d.animation.FadeAnimation.FadeType;
 import ca.couchware.wezzle2d.graphics.GraphicEntity;
 import ca.couchware.wezzle2d.animation.*;
 import ca.couchware.wezzle2d.graphics.AbstractEntity;
 import ca.couchware.wezzle2d.graphics.EntityGroup;
 import ca.couchware.wezzle2d.tile.TileColor;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.util.*;
 import java.lang.reflect.Constructor;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Set;
 
 /**
  * Manages the game board.  A replacement for the GameBoard class from
  * the SVG-based Wezzle.
  *  
  * @author cdmckay
  * 
  */
 
 public class BoardManager implements IManager
 {	
     //--------------------------------------------------------------------------
     // Static Members
     //--------------------------------------------------------------------------      
     
     /**
      * An enumeration representing the four directions.
      * 
      * @author cdmckay
      */
     public static enum Direction 
     {
         UP, DOWN, LEFT, RIGHT
     }
     
     /**
      * The types of board animations.
      */
     public static enum AnimationType
     {
         ROW_FADE, SLIDE_FADE
     
     }
     
     /**
      * The path to the board background graphic.
      */
     final private String PATH = Game.SPRITES_PATH + "/Board.png";        
         
     /**
      * The default number of colours.
      */
     final private int DEFAULT_NUMBER_OF_COLORS = 5;
     
     /**
      * The animation manager.
      */
     final private AnimationManager animationMan;
     
     /**
      * The layer manager.
      */
     final private LayerManager layerMan;
     
 	/**
 	 * The x-coordiante of the top left corner of the board.
 	 */
 	final private int x;
 	
 	/**
 	 * The y-coordinate of the top left corner of the board.
 	 */
 	final private int y;
 	
 	/**
 	 * The number of columns in the game board.
 	 */
 	final private int columns;
 	
 	/**
 	 * The number of rows in the game board.
 	 */
 	final private int rows;
 	
 	/**
 	 * The total number of cells.
 	 */
 	final private int cells;
 	
 	/**
 	 * The minimum number of tiles in a match.
 	 */
 	final private int minimumMatch;
 	
 	/**
 	 * The width of the board.
 	 */
 	final private int width;
 	
 	/**
 	 * The height of the board.
 	 */
 	final private int height;
     
     /**
 	 * The width of a grid cell.
 	 */
 	final private int cellWidth;
 	
 	/**
 	 * The height of a grid cell.
 	 */
 	final private int cellHeight;        
     
     /**
      * The hash map keys for storing the score manager state.
      */
     private static enum Keys
     {
         NUMBER_OF_COLORS,
         NUMBER_OF_TILES,
         NUMBER_OF_ITEMS,
         GRAVITY,
         BOARD,
         SCRATCH_BOARD
     }
     
     /**
      * The hash map used to save the score manager's state.
      */
     final private EnumMap<Keys, Object> managerState = 
             new EnumMap<Keys, Object>(Keys.class);
     
     //--------------------------------------------------------------------------
     // Instance Members
     //--------------------------------------------------------------------------        
     
     /**
      * Whether or not this is visible.
      */
     private boolean visible;
     
     /**
      * Whether or not the board needs to be drawn.
      */
     private boolean dirty;
     
     /**
      * The number of colours.
      */
     private int numberOfColors;
     
     /**
      * The number of tiles.
      */
     private int numberOfTiles;
     
     /**
      * The number of items.
      */
     private int numberOfItems;		
     
     /**
      * The gravity corner.
      */
     private EnumSet<Direction> gravity;
 	
 	/**
 	 * The array representing the game board.
 	 */
 	private TileEntity[] board;
     
     /**
      * An array representing the scratch board.
      */
     private TileEntity[] scratchBoard;
 	
     //--------------------------------------------------------------------------
     // Constructor
     //--------------------------------------------------------------------------
     
 	/**
 	 * The constructor.
 	 */
 	public BoardManager(
             final AnimationManager animationMan,
             final LayerManager layerMan,
             final int x, final int y, 
             final int columns, final int rows)
 	{
         // Board is initially visible.
         this.visible = true;
         
         // Keep reference to managers.
         this.animationMan = animationMan;
         this.layerMan = layerMan;
         
 		// Set the cell width and height. Hard-coded to 32x32 for now.
 		this.cellWidth = 32;
 		this.cellHeight = 32;
 		
 		// Set the x and y coordinates.
 		this.x = x;
 		this.y = y;
 		
 		// Set columns and rows.
 		this.columns = columns;
 		this.rows = rows;
 		this.cells = columns * rows;
 		
 		// Set the minimum match length.
 		this.minimumMatch = 3;
 		
 		// Set the board width and height.
 		this.width = columns * cellWidth;
 		this.height = rows * cellHeight;
         
         // Set the number of colours.
         this.numberOfColors = 5;
                 
         // Set the number of tiles.
         this.numberOfTiles = 0;
         
         // Set the number of items.
         this.numberOfItems = 0;
         
         // Set the gravity to be to the bottom left by default.
         this.gravity = EnumSet.of(Direction.DOWN, Direction.LEFT);
 		
 		// Initialize board.
 		board = new TileEntity[cells];
         scratchBoard = new TileEntity[cells];
         
         // Create the board background graphic.
         GraphicEntity entity = new GraphicEntity.Builder(x - 12, y - 12, PATH)
                 .opacity(90).end();        
         layerMan.add(entity, Game.LAYER_BACKGROUND);
 	}
     
     //--------------------------------------------------------------------------
     // Instance Methods
     //--------------------------------------------------------------------------
 	
     /**
      * Set the board to the passed in array of tile entities.
      * 
      * @param newBoard
      */
     public void loadBoard(TileEntity[] newBoard)
     {
         // Make sure the array is the right size.        
         assert newBoard.length == cells;
         
         // Set the current board to the passed board.
         board = newBoard;
     }
     
 	/**
 	 * Generates a random game board with a linked list of item descriptors.
      * 
 	 * @param items A linked list of Item Descriptors.
 	 */
 	public void generateBoard(LinkedList<Item> itemList)
 	{
         // Make sure the board is clean.
         this.clearBoard();
         assert(itemList.get(0) instanceof Item);
         
         int count = 0;
         for (int i = 0; i < itemList.size(); i++)
         {
             for (int j = 0; 
                 j < itemList.get(i).getInitialAmount(); j++)
             {
                 this.createTile(count, itemList.get(i).getItemClass());
                 count++;
             }
         }      
 		
 		shuffleBoard();
 		refactorBoard();
 		
 		HashSet<Integer> set = new HashSet<Integer>();		
 
 		while (true)
 		{
 			findXMatch(set);
 			findYMatch(set);
 			
 			if (set.size() > 0)
 			{
 				for (Iterator it = set.iterator(); it.hasNext(); )
 				{
 					Integer n = (Integer) it.next();
 					this.createTile(n.intValue(), 
                             getTile(n.intValue()).getClass());
 				}
 				
 				set.clear();
 			}
 			else
 				break;
 		} // end while	
 	}
 	    
     /**
      * Clear the board of all tiles.
      */
     public void clearBoard()
     {
         for (int i = 0; i < cells; i++)
         {
             if (this.getTile(i) != null)
                 this.removeTile(i);
         }
     }       
         
 	/**
 	 * Shuffles the board randomly.
 	 */
 	private void shuffleBoard()
 	{
 		for (int i = 0; i < cells; i++)
 			swapTile(i, Util.random.nextInt(cells));
 	}
 	
 	/**
 	 * An instant refactor used for generating boards.
 	 */
 	private void refactorBoard()
 	{      
         // Check the gravity vertical direction.  It is in if-else format
         // because having a gravity of both up and down would not make sense.                        
         if (gravity.contains(Direction.DOWN))
         {
             startShift(Direction.DOWN, 200);
             for (int i = 0; i < cells; i++)
                 if (board[i] != null)
                     board[i].setYMovement(rows * cellHeight);
         }
         else
         {
             startShift(Direction.UP, 200);
             for (int i = 0; i < cells; i++)
                 if (board[i] != null)
                     board[i].setYMovement(-rows * cellHeight);
         }		
         
 		moveAll(1000);		
 		synchronize();
 		
         // Check the gravity horizontal direction.
         if (gravity.contains(Direction.LEFT))
         {
             startShift(Direction.LEFT, 200);
             for (int i = 0; i < cells; i++)
                 if (board[i] != null)
                     board[i].setXMovement(-columns * cellWidth);
         }
         else
         {
             startShift(Direction.RIGHT, 200);
             for (int i = 0; i < cells; i++)
                 if (board[i] != null)
                     board[i].setXMovement(columns * cellWidth);
         }
 		        
 		moveAll(1000);		
 		synchronize();
 	}
 	
 	/**
 	 * Searches for all matches in the X-direction and returns a linked list
 	 * with the indices of the matches.
      * 
 	 * @param set The linked list that will be filled with indices.
      * @return The number of matches found.
 	 */
 	public int findXMatch(Set<Integer> set)
 	{
         // The line count.
         int lineCount = 0;
         
 		// Cycle through the board looking for a match in the X-direction.
 		for (int i = 0; i < cells; i++)
 		{
 			// Check to see if there's even enough room for an X-match.
 			if (columns - (i % columns) < minimumMatch)
 				continue;
 			
 			// Make sure there's a tile here.
 			if (board[i] == null)
 				continue;
 			
 			// Get the color of this tile.
 			TileColor color = board[i].getColor();
 			
 			// See how long we have a match for.
 			int j;
 			for (j = 1; j < (columns - (i % columns)); j++)
 			{
 				if (board[i + j] == null || board[i + j].getColor() != color)
 					break;
 			}
 			
 			// Check if we have a match.
 			if (j >= minimumMatch)
 			{
 				Util.handleMessage("XMatch of length " + j + " found.",
                         "BoardManager#findXMatch");
                 
                 lineCount++;
 				
 				// Copy all matched locations to the linked list.
 				for (int k = i; k < i + j; k++)				
 					set.add(new Integer(k));				
 				
 				i += j - 1;
 			}
 		} // end for
         
         // Return the line count.
         return lineCount;
 	}
 	
 	/**
 	 * Searches for all matches in the Y-direction and returns a set
 	 * with the indices of the matches.
      * 
 	 * @param set The linked list that will be filled with indices.
      * @return The number of matches found.
 	 */
 	public int findYMatch(Set<Integer> set)
 	{
         // The number of matches found.
         int lineCount = 0;
         
 		// Cycle through the board looking for a match in the Y-direction.
 		for (int i = 0; i < cells; i++)
 		{
 			// Transpose i.
 			int ti = Util.pseudoTranspose(i, columns, rows);
 			
 			// Check to see if there's even enough room for an Y-match.
 			if (rows - (ti / columns) < minimumMatch)
 				continue;
 			
 			// Make sure there's a tile here.
 			if (board[ti] == null)
 				continue;
 			
 			// Get the color of this tile.
 			TileColor color = board[ti].getColor();
 			
 			// See how long we have a match for.
 			int j;
 			for (j = 1; j < (rows - (ti / columns)); j++)
 			{
 				// Transpose i + j.
 				int tij = Util.pseudoTranspose(i + j, columns, rows);
 				
 				if (board[tij] == null 
 						|| board[tij].getColor() != color)
 					break;
 			}
 			
 			// Check if we have a match.
 			if (j >= minimumMatch)
 			{
 				Util.handleMessage("YMatch of length " + j + " found.", 
                         "BoardManager#findYMatch");
 				
                 lineCount++;
                 
 				// Copy all matched locations to the linked list.
 				for (int k = i; k < i + j; k++)				
 					set.add(new Integer(Util.pseudoTranspose(k, columns, rows)));				
 				
 				i += j - 1;
 			}
 		}
         
         // Return the number of matches found.
         return lineCount;
 	}
 	
 	/**
 	 * Synchronizes the current board array with where the tiles are current
 	 * are on the board.  Usually called after a refactor so that the board
 	 * array will accurately reflect the board.
 	 */
 	public void synchronize()
 	{				
         Arrays.fill(scratchBoard, null);
 		
 		for (int i = 0; i < cells; i++)
 		{			
 			if (board[i] != null)
 			{
 				TileEntity t = board[i];
 				int column = (t.getX() - x) / cellWidth;
 				int row = (t.getY() - y) / cellHeight;
 												
     			scratchBoard[column + (row * columns)] = board[i];
 			}
 		}
         
         // The new number of tiles.
         int newNumberOfTiles = 0;
         
         // Count the number of tiles on the new board.
         for (int i = 0; i < cells; i++)        
             if (scratchBoard[i] != null)
                 newNumberOfTiles++;        
 		
         // Make sure the tile count hasn't changed.
         if (newNumberOfTiles != numberOfTiles)
             throw new IllegalStateException("Expected " + numberOfTiles + ", "
                     + "Found " + newNumberOfTiles + ".");
         
         // Trade-sies!
         TileEntity[] swapBoard = board;
 		board = scratchBoard;
         scratchBoard = swapBoard;
 	}
     
     /**
      * This method is used to re-add all the tiles to the layer manager if they
      * are not already there.  It is principally used for restoring an old board
      * in the manager <pre>loadState()</pre> method.
      */
     private void layerize()
     {
         for (TileEntity tile : board)            
             if (tile != null && layerMan.exists(tile, Game.LAYER_TILE) == false)
             {
                 tile.setVisible(visible);
                 layerMan.add(tile, Game.LAYER_TILE);
             }
     }
 	
     /**
      * TODO Documentation.
      * 
      * @param direction
      * @param speed
      */
     public void startShift(final Direction direction, final int speed)
     {
         switch (direction)
         {
             case UP:
                 
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
                         board[i].setYMovement(-speed);
                         board[i].calculateBound(direction,
                                 countTilesInDirection(direction, i));
                     }
                 }  
                 
                 break;
                 
             case DOWN:
                 
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
                         board[i].setYMovement(+speed);
                         board[i].calculateBound(direction,
                                 countTilesInDirection(direction, i));
                     }
                 }  
                 
                 break;
                 
             case LEFT:
                 
                 // Start them moving left.
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
                         board[i].setXMovement(-speed);
                         board[i].calculateBound(direction,
                                 countTilesInDirection(direction, i));
                     }
                 }
                 
                 break;
                 
             case RIGHT:
                 
                 // Start them moving left.
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
                         board[i].setXMovement(+speed);
                         board[i].calculateBound(direction,
                                 countTilesInDirection(direction, i));
                     }
                 }
                 
                 break;
                 
             default:
                 throw new IllegalStateException("Unknown direction.");
         }              
     }    	
     
     /**
      * A convience method for starting a shift in the vertical direction of the
      * currently set gravity.
      * 
      * @param speed
      */
     public void startVerticalShift(final int speed)
     {
         if (gravity.contains(Direction.DOWN))
             startShift(Direction.DOWN, speed);
         else
             startShift(Direction.UP, speed);
     }
     
     /**
      * A convience method for starting a horizontal shift in the direction of 
      * the currently set gravity.
      * 
      * @param speed
      */
     public void startHorizontalShift(final int speed)
     {
         if (gravity.contains(Direction.LEFT))
             startShift(Direction.LEFT, speed);
         else
             startShift(Direction.RIGHT, speed);
     }
     
 	/**
 	 * Moves all currently moving tiles.
 	 * @returns True if there is still more moving to happen.
 	 */
 	public boolean moveAll(long delta)
 	{
 		// Set to true if there are more movement to happen.
 		boolean moreMovement = false;
 		
 		for (int i = 0; i < cells; i++)		
 			if (board[i] != null)
 			{
 				board[i].move(delta);
 				if (board[i].getXMovement() != 0 
 						|| board[i].getYMovement() != 0)
 					moreMovement = true;
 			}
 		
         // Dirty board.
         setDirty(true);
         
 		return moreMovement;
 	}    
 	
 	/**
 	 * Counts all the tiles that are under the tile at the specified
 	 * index.
 	 * 
 	 * For example, if we had a 3x3 board like this:
 	 * 
 	 * 012 .X.
 	 * 345 XX.
 	 * 678 .XX
 	 * 
 	 * where "X" is a tile and "." is an empty space, then calling
 	 * this method on index 1 would return 2.
 	 * 
 	 * @param index
 	 * @return
 	 */
     public int countTilesInDirection(Direction direction, int index)
     {
         // Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// The current column and row.
 		int column = index % columns;
 		int row = index / columns;
         
         // The tile count.
         int count = 0;
         
         switch (direction)
         {
             case UP:
                 
                 // If we're at the top row, return 0.
                 if (row == 0)
                     return 0;
                 
                 // Cycle through the column rows, counting tiles.
                 for (int j = row - 1; j >= 0; j--)
                     if (getTile(column, j) != null)
                         count++;
                 
                 break;
            
             case DOWN:
                 
                 // If we're at the bottom row, return 0.
                 if (row == rows - 1)
                     return 0;
               
                 // Cycle through the column rows, counting tiles.
                 for (int j = row + 1; j < rows; j++)
                     if (getTile(column, j) != null)
                         count++;
                 
                 break;
                 
             case LEFT:
                 
                 // If we're at the bottom row, return 0.
                 if (column == 0)
                     return 0;
                 
                 // Cycle through the column rows, counting tiles.
                 for (int i = column - 1; i >= 0; i--)
                     if (getTile(i, row) != null)
                         count++;
                 
                 break;
                 
             case RIGHT:
                 
                 // If we're at the bottom row, return 0.
                 if (column == columns - 1)
                     return 0;
 
                 // Cycle through the column rows, counting tiles.
                 for (int i = column + 1; i < columns; i++)
                     if (getTile(i, row) != null)
                         count++;
                 
                 break;
                 
             default:
                 throw new IllegalStateException("Unknown direction.");
         }
         
         // Return the count.
         return count;
     }    
     
     public void addTile(final int index, final TileEntity t)
     {
         // Sanity check.
         assert (index >= 0 && index < cells);   
         
         // Make sure the tile is located properly.
         t.setXYPosition(x + (index % columns) * cellWidth, 
                 y + (index / columns) * cellHeight);
         t.resetDrawRect();
         
         // If this is an item, increment the count.
         if (t.getClass() != TileEntity.class)
         {
             Util.handleMessage("Item added.", "BoardManager#addTile");
             this.incrementNumberOfItems();                
         }
                        
         // If we're overwriting a tile, remove it first.
         if (getTile(index) != null)
             removeTile(index);
         
 		// Set the tile.
 		board[index] = t;
         
         // Increment tile count.
         numberOfTiles++;
 
         // Set the tile visibility to that of the board.
         t.setVisible(visible);
         
         // Add the tile to the bottom layer too.        
         layerMan.add(t, Game.LAYER_TILE);               
         
         // Dirty board.
         setDirty(true);                
     }
     
     public void addTile(final int column, final int row, final TileEntity t)
     {
          addTile(row * columns + column, t);
     }
 	        
     /**
      * Create a new a tile at the specified index using the given class and
      * color.  The new tile is also returned.
      * 
      * @param index
      * @param c
      * @param color
      * @return
      */
 	public TileEntity createTile(final int index, final Class c, 
             final TileColor color)
 	{
         // Sanity check.
         assert (index >= 0 && index < cells);
         assert (c != null);      
         
         // The new tile.
         TileEntity t = null;
         
         try
         {           
             // Get the constructor.  All tiles must have the same constructor.
             Constructor con = c.getConstructor(new Class[] { BoardManager.class, 
                 TileColor.class, Integer.TYPE, Integer.TYPE });
 
             t = (TileEntity) con.newInstance(this, color, 
 				x + (index % columns) * cellWidth,
 				y + (index / columns) * cellHeight);          
             
             con = null;
         }
         catch (Exception ex)
         {
             Util.handleException(ex);
             return null;
         }        
         
         // Add the tile.
         addTile(index, t);
         
         // Return the tile.
         return t;
 	}
     
     public TileEntity createTile(final int column, final int row, final Class c,
             final TileColor color)
     {
         return createTile(row * columns + column, c, color);
     }
     
     public TileEntity createTile(final int index, final Class c)
     {
         return createTile(index, c, 
                 TileColor.getRandomColor(getNumberOfColors()));
     }
     
     public TileEntity createTile(final int column, final int row, final Class c)
     {
         return createTile(row * columns + column, c);
     }
     
     public void removeTile(final int index)
     {
         // Sanity check.
 		assert(index >= 0 && index < cells);
         
         // Get the tile.
         TileEntity t = getTile(index);
         
         // If the tile does not exist, throw an exception.
         if (t == null)
             throw new NullPointerException("No tile at that index.");        
         
         // If this is an item, decrement the item count.
         if (t.getClass() != TileEntity.class)
             this.decrementNumberOfItems();
         
         // Remove from layer manager.
         if (layerMan.exists(t, Game.LAYER_TILE))
             layerMan.remove(t, Game.LAYER_TILE);
         
         // Remove the tile.
         board[index] = null;
         
         // Remove the animation.        
         animationMan.remove(t.getAnimation());
         
         // Decrement tile counter.
         numberOfTiles--;
         
         // Dirty board.
         setDirty(true);
     }
     
     public void removeTile(final int column, final int row)
     {
         // Sanity check.
 		assert(column >= 0 && column < columns);
         assert(row >= 0 && row < rows);
         
         // Passthrough.
         removeTile(column + (row * columns));
     }
     
     public void removeTiles(final Set indexSet)
     {
         for (Iterator it = indexSet.iterator(); it.hasNext(); )        
             removeTile((Integer) it.next());        
     }
 	
 	public TileEntity getTile(int index)
 	{
 		// Sanity check.
 		assert(index >= 0 && index < cells);
 		
 		// Set the tile.
 		return board[index];
 	}
 	
 	public TileEntity getTile(int column, int row)
 	{
 		// Make sure we're within parameters.
 		assert column >= 0 && column < columns;
         assert row >= 0 && row < rows;
 		
 		return getTile(column + (row * columns));
 	}
     
     public EntityGroup getTiles(int index1, int index2)
     {
         assert index1 >= 0 && index1 < cells;
         assert index2 >= index1 && index2 < cells;               
         
         // Calculate the number of possible tiles in the range.
         int length = index2 - index1 + 1;
         
         // Count the number of tiles in that range.
         int count = 0;
         
         for (int i = 0; i < length; i++)
             if (getTile(index1 + i) != null)
                 count++;
         
         AbstractEntity[] entities = new AbstractEntity[count];
         
         // The array index.
         int index = 0;
         
         for (int i = 0; i < length; i++)
         {
             AbstractEntity e = getTile(index1 + i);
             
             if (e != null)
                 entities[index++] = e;
         }
         
         return new EntityGroup(entities);
     }	
 
 	public void swapTile(int index1, int index2)
 	{
 		// Validate parameters.
 		assert(index1 >= 0 && index1 < cells);
 		assert(index2 >= 0 && index2 < cells);
 		
 		TileEntity t = board[index1];
 		board[index1] = board[index2];
 		board[index2] = t;
 		
 		if (board[index1] != null)
 		{
 			board[index1].setX(x + (index1 % columns) * cellWidth);
 			board[index1].setY(y + (index1 / columns) * cellHeight);
 		}
 		
 		if (board[index2] != null)
 		{
 			board[index2].setX(x + (index2 % columns) * cellWidth);
 			board[index2].setY(y + (index2 / columns) * cellHeight);
 		}
 	}        
 
     /**
      * Finds all the tiles between the rocket and the wall that is in the
      * direction the rocket is pointing.
      * 
      * @param rocketSet
      * @param affectedSet
      */
     public void processRockets(Set<Integer> rocketSet, Set<Integer> affectedSet)
     {        
         // Clear the set.
         affectedSet.clear();
                 
         int column;
         int row;        
         
         for (Integer rocketIndex : rocketSet)
         {
             // Extract column and row.
             column = rocketIndex % columns;
             row = rocketIndex / columns;
             
             // Depending on the direction, collect the appropriate tiles.                                           
             RocketTileEntity.Direction dir = 
                     ((RocketTileEntity) getTile(rocketIndex.intValue()))
                     .getDirection();
             
             int index;
 
             switch (dir)
             {
                 case UP:
                         
                     //Util.handleWarning("Dir is up!", Thread.currentThread());
                     
                     for (int j = 0; j <= row; j++)
                     {
                         index = column + j * columns;
                         
                         if (getTile(index) != null)
                             affectedSet.add(index);
                     }
 
                     break;
 
                 case DOWN:
                     
                     //Util.handleWarning("Dir is down!", Thread.currentThread());
 
                     for (int j = row; j < rows; j++)
                     {
                         index = column + j * columns;
                         
                         if (getTile(index) != null)
                             affectedSet.add(index);
                     }
 
                     break;
 
                 case LEFT:
                     
                     //Util.handleWarning("Dir is left!", Thread.currentThread());
 
                     for (int j = 0; j <= column; j++)
                     {
                         index = j + row * columns;
                         
                         if (getTile(index) != null)
                             affectedSet.add(index);
                     }   
 
                     break;
 
                 case RIGHT:
                     
                     //Util.handleWarning("Dir is right!", Thread.currentThread());
                     
                     for (int j = column; j < columns; j++)
                     {
                         index = j + row * columns;
                         
                         if (getTile(index) != null)
                             affectedSet.add(index);
                     }  
 
                     break;
             }                  
         }               
     } 
     
     /**
      * Finds all the tiles that are the same color as the star tile.
      * 
      * @param starSet
      * @param affectedSet
      */
     public void processStars(Set<Integer> starSet, Set<Integer> affectedSet)
     {        
         // Clear the set.
         affectedSet.clear();
         
         for (Integer starIndex : starSet)
         {
             // Determine the colour of the star.
             TileColor color = getTile(starIndex).getColor();
             
             // Look for that colour and add it to the affected set.
             for (int i = 0; i < cells; i++)
             {
                 if (getTile(i) == null)
                     continue;
                 
                 if (getTile(i).getColor() == color)
                     affectedSet.add(i);
             }
         }               
     } 
     
 	/**
 	 * Feeds all the bombs in the bomb processor and then returns those results
      * in the cleared out affected set parameter.
      * 
 	 * @param bombTileSet
      * @param affectedSet
 	 */
 	public void processBombs(Set<Integer> bombSet, Set<Integer> affectedSet)
 	{				
 		// A list of tiles affected by the blast.
 		affectedSet.clear();
 		
 		// Gather affected tiles.
 		for (Iterator<Integer> it = bombSet.iterator(); it.hasNext(); )		
 			affectedSet.addAll(this.processBomb(it.next()));			
 	}
     
     /**
 	 * Determines where tiles are affected by the bomb explosion.
      * 
 	 * @param bombIndex     
      * @return The set of indices (including the bomb) affected by the bomb.
 	 */
 	private Set<Integer> processBomb(final int bombIndex)
 	{	                
 		// List of additional bomb tiles.
 		Set<Integer> affectedSet = new HashSet<Integer>();		
 		
 		// Return if bomb is null.
 		if (getTile(bombIndex) == null)
 			return null;				
 		
 		// Determine affected tiles.
 		for (int j = -1; j < 2; j++)
 		{
 			for (int i = -1; i < 2; i++)
 			{
 				if ((bombIndex % columns) + i >= 0
 						&& (bombIndex % columns) + i < this.getColumns()
 						&& (bombIndex / columns) + j >= 0
 						&& (bombIndex / columns) + j < this.getRows())
 				{
 					if (getTile(bombIndex % columns + i, bombIndex / columns + j) != null)
 						affectedSet.add(new Integer(bombIndex + i + (j * columns)));														
 				}
 			} // end for i
 		} // end for j
 				
 		// Pass back affected tiles.
 		return affectedSet;
 	}
                     
     /**
      * Scans the tile set for specified item and places them in a passed item 
      * set.
      * 
      * @param itemClass
      * @param tileSet
      * @param itemSet
      */
     public void scanFor(Class itemClass, 
             Set<Integer> tileSet, Set<Integer> itemSet)
     {        
         for (Integer index : tileSet)
         {            
             if (getTile(index).getClass() == itemClass)            
                 itemSet.add(index);                            
         } // end for
     }           
     
     /**
      * Animates the showing of the board.
      *      
      * @param type The type of animation to use.
      * @return An animation that can be checked for doneness.
      */
     public IAnimation animateShow(AnimationType type)
     {                
          // Animate based on the type.
         switch (type)
         {
             case ROW_FADE:
                 return animateRowFadeIn();                
                 
             case SLIDE_FADE:
                 return animateSlideFadeIn();
                 
             default:
                 throw new AssertionError();
         }      
     }
     
     private IAnimation animateRowFadeIn()
     {
         // The amount of delay between each row.
         int delay = 0;
         int deltaDelay = 200;
         
         // True if a tile was found this row.
         boolean tileFound = false;
         
         // Count the number of tiles.
         int tileCount = 0;
         
         // Add the animations.
         for (int i = 0; i < cells; i++)
 		{
 			TileEntity t = getTile(i);
 			
 			if (t != null)		
 			{	
                 //Animation a = new FadeAnimation(FadeType.IN, 0, 700, t);
                 IAnimation a = new FadeAnimation.Builder(FadeType.IN, t)
                         .wait(delay).duration(700).end();                
                 t.setAnimation(a);
                 animationMan.add(a);
                 
                 tileFound = true;
                 tileCount++;
 			}
 			
 			if (tileFound == true && (i + 1) % columns == 0)
             {
                 tileFound = false;
 				delay += deltaDelay;
             }
         }
         
         // If there are any tiles, there at least must be a tile in the bottom
         // left corner.                
         if (tileCount > 0)
         {
             for (int i = cells - 1; i >= 0; i--)
                 if (getTile(i) != null)
                     return getTile(i).getAnimation();
             throw new IllegalStateException("There are no tiles on the board.");
         }
         else
             return null;
     }
     
     /**
      * Slides the board on the screen and fades it in as it does so.
      * 
      * @return An animation object that can be tested for doneness.
      */
     private IAnimation animateSlideFadeIn()
     {
         // Count the number of tiles.
         int tileCount = 0;
         
         // The animation variables that will be used.
         IAnimation a1 = null;
         IAnimation a2 = null;
         
         // Get all the tiles on the board.
         for (int i = 0; i < cells; i++)
         {
             // Get the tile.
             final TileEntity tile = getTile(i);                        
             
             // Get the row.
             int row = i / columns;
             
             if (tile != null)		
 			{
                 // Make a copy and hide the original.                
                 final TileEntity t = new TileEntity(tile);
                 tile.setVisible(false);
                 layerMan.add(t, Game.LAYER_TILE);
                 
                 // Count it.
                 tileCount++;
                 
                 // Create the animation.
                 a1 = new FadeAnimation.Builder(FadeType.IN, t)
                         .wait(0).duration(500).end();
                 
                 // Make the animation remove itself.                
                 a1.setFinishAction(new Runnable()
                 {
                    public void run()
                    {
                        layerMan.remove(t, Game.LAYER_TILE);
                        tile.setVisible(true);
                    }
                 });
 
                 // Determine the theta.
                 int theta = 180 * ((row + 1) % 2);
                 
                 // The min and max x values.
                 int minX = Integer.MIN_VALUE;
                 int maxX = Integer.MAX_VALUE;
                 
                 // If the theta is facing right, move the copy that direction.
                 if (theta == 0)
                 {
                     t.translate(-(int) (500.0 * 0.15), 0);
                     maxX = x + width;
                 }
                 else if (theta == 180)
                 {
                     t.translate((int) (500.0 * 0.15), 0);
                     minX = x;
                 }
                 else
                     throw new AssertionError("Angle should only be 0 or 180.");
                 
                 a2 = new MoveAnimation.Builder(t).minX(minX).maxX(maxX)
                         .wait(0).duration(500).theta(theta).v(0.15).end();                                
                 
                 // Add them to the animation manager.
                 t.setAnimation(a1);
                 animationMan.add(a1);
                 animationMan.add(a2);
             }
         }
                 
         // If there are any tiles, there at least must be a tile in the bottom
         // left corner.
         if (tileCount > 0)
         {
             return a1;
         }
         else
             return null;                
     }
     
     /**
      * Animates the hiding of the board.
      *      
      * @param type The type of animation to use.
      * @return An animation that can be checked for doneness.
      */
     public IAnimation animateHide(AnimationType type)
     {       
         // Animate based on the type.
         switch (type)
         {
             case ROW_FADE:
                 return animateRowFadeOut();                
                 
             case SLIDE_FADE:
                 return animateSlideFadeOut();
                 
             default:
                 throw new AssertionError();
         }                
     }
     
     /**
      * Create an animation that fades each row slowly, starting from the
      * top to the bottom.
      * 
      * @return An animation object that can be tested for doneness.
      */
     private IAnimation animateRowFadeOut()
     {
         // The amount of delay between each row.
         int wait = 0;
         int deltaWait = 200;
         
         // True if a tile was found this row.
         boolean tileFound = false;
         
         // Count the number of tiles.
         int tileCount = 0;
         
         // Add the animations.
         for (int i = 0; i < cells; i++)
 		{
 			TileEntity t = getTile(i);
 			
 			if (t != null)		
 			{	                
                 IAnimation a = new FadeAnimation.Builder(FadeType.OUT, t)
                         .wait(wait).duration(700).end();                 
                 t.setAnimation(a);
                 animationMan.add(a);
                 
                 tileFound = true;
                 tileCount++;
 			}
 			
 			if (tileFound == true && (i + 1) % columns == 0)
             {
                 tileFound = false;
 				wait += deltaWait;
             }
         }
         
         // If there are any tiles, there at least must be a tile in the bottom
         // left corner.
         if (tileCount > 0)
         {
             for (int i = cells - 1; i >= 0; i--)
                 if (getTile(i) != null)
                     return getTile(i).getAnimation();            
             throw new IllegalStateException("There are no tiles on the board.");
         }
         else
             return null;
     }
     
     /**
      * Slides the board off the screen and fades it as it does so.
      * 
      * ---->
      * <----
      * ---->
      * <----
      * 
      * @return An animation object that can be tested for doneness.
      */
     private IAnimation animateSlideFadeOut()
     {
         // Count the number of tiles.
         int tileCount = 0;
         
         // The animation variables that will be used.
         IAnimation a1 = null;
         IAnimation a2 = null;
         
         // Get all the tiles on the board.
         for (int i = 0; i < cells; i++)
         {
             // Get the tile.
             TileEntity tile = getTile(i);                        
             
             // Get the row.
             int row = i / columns;
             
             if (tile != null)		
 			{
                 // Make a copy and hide the original.                
                 final TileEntity t = new TileEntity(tile);
                 tile.setVisible(false);
                 layerMan.add(t, Game.LAYER_TILE);
                 
                 // Count it.
                 tileCount++;
                 
                 // Create the animation.
                 a1 = new FadeAnimation.Builder(FadeType.OUT, t)
                         .wait(0).duration(500).end();
 
                 a2 = new MoveAnimation.Builder(t).wait(0)
                         .duration(500).theta(180 * (row % 2)).v(0.15).end();
                 
                 // Make the animation remove itself.                
                 a1.setFinishAction(new Runnable()
                 {
                    public void run()
                    {
                        layerMan.remove(t, Game.LAYER_TILE);
                    }
                 });
                 
                 // Add them to the animation manager.
                 t.setAnimation(a1);
                 animationMan.add(a1);
                 animationMan.add(a2);
             }
         }
                 
         // If there are any tiles, there at least must be a tile in the bottom
         // left corner.
         if (tileCount > 0)
         {
             return a1;
         }
         else
             return null;                
     }
     
     /**
      * Determines the centrepoint of a group of tiles.  Used for determine
      * position of SCT.
      * 
      * @param indexSet
      * @return
      */
     public WPosition determineCenterPoint(final Set<Integer> indexSet)
     {
         // The furthest left, right, up and down locations.
         int l = Integer.MAX_VALUE;
         int r = 0;
         int u = Integer.MAX_VALUE;
         int d = 0;
 
         // The x and y coordinate of the centre of the tiles.
         int cx, cy;
 
         // Determine centre of tiles.
         for (Integer index : indexSet)            
         {     
             TileEntity t = getTile(index); 
             
             if (t == null)
                 Util.handleWarning("Tile was null: " + index , 
                         "BoardManager#determineCenterPoint");
 
             if (t.getX() < l) 
                 l = t.getX();
 
             if (t.getX() + t.getWidth() > r) 
                 r = t.getX() + t.getWidth();
 
             if (t.getY() < u)
                 u = t.getY();
 
             if (t.getY() + t.getHeight() > d)
                 d = t.getY() + t.getHeight();
         }
 
         // Assigned centre.
         cx = l + (r - l) / 2;
         cy = u + (d - u) / 2;
         
         // Return centerpoint.
         return new WPosition(cx, cy);
     }
     
 	/**
 	 * Prints board to console (for debugging purposes).
 	 */
 	public void print()
 	{
 		for (int i = 0; i < board.length; i++)
 		{
 			if (board[i] == null)
 				System.out.print(".");
 			else
 				System.out.print("X");
 			
 			if (i % columns == columns - 1)
 				System.out.println();
 		}
 		
 		System.out.println();
 	}
     
     /**
      * Restarts the board manager to appropriate settings for the first level.
      */
     public void restart()
     {
         // Reset the number of colours.
         setNumberOfColors(DEFAULT_NUMBER_OF_COLORS);
     }    
 
     //--------------------------------------------------------------------------
     // Getters and Setters
     //--------------------------------------------------------------------------
     
 	/**
 	 * @return The cellWidth.
 	 */
 	public int getCellWidth()
 	{
 		return cellWidth;
 	}
 
 	/**
 	 * @return The cellHeight.
 	 */
 	public int getCellHeight()
 	{
 		return cellHeight;
 	}			
 	
 	/**
 	 * Gets the width.
 	 * @return The width.
 	 */
 	public int getWidth()
 	{
 		return width;
 	}
 
 	/**
 	 * Gets the height.
 	 * @return The height.
 	 */
 	public int getHeight()
 	{
 		return height;
 	}
 
 	/**
 	 * Gets the x.
 	 * @return The x.
 	 */
 	public int getX()
 	{
 		return x;
 	}
 
 	/**
 	 * Gets the y.
 	 * @return The y.
 	 */
 	public int getY()
 	{
 		return y;
 	}
 
     public int getColumns()
     {
         return columns;
     }
 
     public int getRows()
     {
         return rows;
     }
 
     public int getCells()
     {
         return cells;
     }
             
     public int getNumberOfItems()
     {
         return this.numberOfItems;
     }
     
     public void setNumberOfItems(final int numberOfItems)
     {
         this.numberOfItems = numberOfItems;
     }
     
     public void decrementNumberOfItems()
     {
         this.numberOfItems--;
     }
     
     public void incrementNumberOfItems()
     {
         this.numberOfItems++;
     }    
 
     public int getNumberOfTiles()
     {
         int counter = 0;
         for(int i = 0; i < this.cells; i++)
         {
             if (this.getTile(i) != null)
                 counter++;
         }
         
         return counter;
     }
 
     public int getNumberOfColors()
     {
         return numberOfColors;
     }
 
     public void setNumberOfColors(int numberOfColors)
     {
         this.numberOfColors = numberOfColors;
     }
 
     public EnumSet<Direction> getGravity()
     {
         return gravity;
     }      
 
     public void setGravity(EnumSet<Direction> gravity)
     {
         this.gravity = gravity;
     }            
     
     public boolean isVisible()
     {
         return visible;
     }
     
     public void setVisible(boolean visible)
     {        
         Util.handleMessage("Board visible: " + visible + ".", 
                 "BoardManager#setVisible");
        
        for (TileEntity tile : board)
            if (tile != null)
                tile.setVisible(visible);
            
         this.visible = visible;        
     }   
     
     public void setDirty(boolean dirty)
     {
         this.dirty = dirty;
     }
 
     public boolean isDirty()
     {
         return dirty;
     }
 
     public void saveState()
     {
         managerState.put(Keys.NUMBER_OF_COLORS, numberOfColors);
         managerState.put(Keys.NUMBER_OF_ITEMS, numberOfItems);
         managerState.put(Keys.NUMBER_OF_TILES, numberOfTiles);
         managerState.put(Keys.GRAVITY, gravity);
         managerState.put(Keys.BOARD, board.clone());
         managerState.put(Keys.SCRATCH_BOARD, scratchBoard.clone());
         
         Util.handleMessage("Saved " + numberOfTiles + " tiles.");  
         Util.handleMessage("Saved " + numberOfItems + " items.");
     }
 
     @SuppressWarnings("unchecked") 
     public void loadState()
     {
         // Clear the board.
         clearBoard();
                 
         numberOfColors = (Integer) managerState.get(Keys.NUMBER_OF_COLORS);
         numberOfItems = (Integer) managerState.get(Keys.NUMBER_OF_ITEMS);
         numberOfTiles = (Integer) managerState.get(Keys.NUMBER_OF_TILES);        
         gravity = (EnumSet<Direction>) managerState.get(Keys.GRAVITY);
         scratchBoard = (TileEntity[]) managerState.get(Keys.SCRATCH_BOARD);   
         board = (TileEntity[]) managerState.get(Keys.BOARD);        
         
         // Make sure that this board is in the layer manager.
         layerize();
         
         Util.handleMessage("Loaded " + numberOfTiles + " tiles.");
         Util.handleMessage("Loaded " + numberOfItems + " items.");
     }
 
 }
