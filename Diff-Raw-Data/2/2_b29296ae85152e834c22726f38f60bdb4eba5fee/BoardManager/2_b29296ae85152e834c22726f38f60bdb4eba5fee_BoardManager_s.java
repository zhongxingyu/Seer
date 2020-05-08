 package ca.couchware.wezzle2d.manager;
 
 import ca.couchware.wezzle2d.*;
 import ca.couchware.wezzle2d.manager.LayerManager.Layer;
 import ca.couchware.wezzle2d.graphics.GraphicEntity;
 import ca.couchware.wezzle2d.animation.*;
 import ca.couchware.wezzle2d.graphics.AbstractEntity;
 import ca.couchware.wezzle2d.graphics.EntityGroup;
 import ca.couchware.wezzle2d.manager.Settings.Key;
 import ca.couchware.wezzle2d.tile.TileColor;
 import ca.couchware.wezzle2d.tile.*;
 import ca.couchware.wezzle2d.util.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.EnumMap;
 import java.util.EnumSet;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
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
         NONE(0), 
         UP(-1), 
         DOWN(1), 
         LEFT(-1), 
         RIGHT(1);
                 
         private int dir;
         
         Direction(int dir)
         { this.dir = dir; }
                 
         public int asInteger()
         { return dir; }
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
     final private String PATH = Settings.getSpriteResourcesPath() + "/Board.png";        
         
     /**
      * The default number of colours.
      */
     final private int DEFAULT_NUMBER_OF_COLORS = 5;
     
     /**
      * The animation manager.
      */
     final private AnimationManager animationMan;
     
     /**
      * The world Manager
      */
     final private WorldManager worldMan;
     
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
      * The shape of the board.
      */
     final private ImmutableRectangle shape;
     
     /**
      * The hash map keys for storing the score manager state.
      */
     private static enum Keys
     {
         NUMBER_OF_COLORS,
         NUMBER_OF_TILES,
         NUMBER_OF_ITEMS,
         NUMBER_OF_MULTS,
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
      * The number of mults.
      */
     private int numberOfMults;
     
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
 	private BoardManager(
             final AnimationManager animationMan,
             final LayerManager layerMan,
             final WorldManager worldMan,
             final int x, final int y, 
             final int columns, final int rows)
 	{
         // Board is initially visible.
         this.visible = true;
         
         // Keep reference to managers.
         this.animationMan = animationMan;
         this.layerMan = layerMan;
         this.worldMan = worldMan;
         
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
         
         // Create the shape.
         this.shape = new ImmutableRectangle(x, y, width, height);
         
         // Set the number of colours.
         this.numberOfColors = 5;
                 
         // Set the number of tiles.
         this.numberOfTiles = 0;
         
         // Set the number of items.
         this.numberOfItems = 0;
         
         this.numberOfMults = 0;
         
         // Set the gravity to be to the bottom left by default.
         this.gravity = EnumSet.of(Direction.DOWN, Direction.LEFT);
 		
 		// Initialize board.
 		board = new TileEntity[cells];
         scratchBoard = new TileEntity[cells];
         
         // Create the board background graphic.
         GraphicEntity entity = new GraphicEntity.Builder(x - 12, y - 12, PATH)
                 .opacity(90).end();        
         layerMan.add(entity, Layer.BACKGROUND);        
 	}
             
     /**
      * Public API.
      * 
      * @param animationMan
      * @param layerMan
      * @param x
      * @param y
      * @param columns
      * @param rows
      * @return
      */    
     public static BoardManager newInstance(final AnimationManager animationMan,
         final LayerManager layerMan,
         final WorldManager worldMan,
         final int x, final int y, 
         final int columns, final int rows)
     {
         return new BoardManager(animationMan, layerMan, worldMan, x, y, columns, rows);
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
 	public void generateBoard(List<Item> itemList)
 	{
         // Make sure the board is clean.
         this.clearBoard(); 
         assert(itemList.get(0) instanceof Item);
         
        System.out.println("******* " + worldMan.getLevel());
        
         int count = 0;
         for (int i = 0; i < itemList.size(); i++)
         {
             int offset = 0;
             
             // Handle the case where the normal tiles are different. This occurs
             // When players start on a level other than level 1.
             if(i == 0)
                 offset = worldMan.getLevel()-1;
             
             for (int j = 0; 
                 j < itemList.get(i).getInitialAmount() + offset; j++)
             {
                 this.createTile(count, itemList.get(i).getTileType());
                 count++;
             }
         }      
 		
 		shuffleBoard();
 		instantRefactorBoard();
 		
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
                             getTile(n.intValue()).getType());
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
         
         // Ensure the item counts are set to 0
         for (Item item : worldMan.getItemList())
             item.setCurrentAmount(0);
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
 	private void instantRefactorBoard()
 	{
         // Check the vertical direction.
         if (gravity.contains(Direction.DOWN) == true)
         {
             for (int i = 0; i < cells; i++)
             {
                 TileEntity tile = board[i];
                 if (tile == null) continue;
                 
                 int tiles = countTilesInDirection(Direction.DOWN, i);
                 int bound = calculateBound(Direction.DOWN, tiles);
                 
                 if (bound != board[i].getY())
                 {
                     board[i].setY(bound);
                     //synchronizeTile(board[i]);
                     //board[i] = null;
                 }                             
             }
         }
         else
         {
             for (int i = 0; i < cells; i++)
             {
                 TileEntity tile = board[i];
                 if (tile == null) continue;
                 
                 int tiles = countTilesInDirection(Direction.UP, i);
                 int bound = calculateBound(Direction.UP, tiles);
                 
                 if (bound != board[i].getY())
                 {
                     board[i].setY(bound);
                     //synchronizeTile(board[i]);
                     //board[i] = null;
                 }
             }
         }     
                 
         synchronize();
         
         // Check the horizontal direction.
         if (gravity.contains(Direction.LEFT) == true)
         {
             for (int i = 0; i < cells; i++)
             {
                 TileEntity tile = board[i];
                 if (tile == null) continue;
                 
                 int tiles = countTilesInDirection(Direction.LEFT, i);
                 int bound = calculateBound(Direction.LEFT, tiles);
                 
                 if (bound != board[i].getX())
                 {
                     board[i].setX(bound);
                     //synchronizeTile(board[i]);
                     //board[i] = null;
                 }
             }
         }
         else
         {
             for (int i = 0; i < cells; i++)
             {
                 TileEntity tile = board[i];
                 if (tile == null) continue;
                 
                 int tiles = countTilesInDirection(Direction.RIGHT, i);
                 int bound = calculateBound(Direction.RIGHT, tiles);                
                 if (bound != board[i].getX())
                 {
                     board[i].setX(bound);
                     //synchronizeTile(board[i]);
                     //board[i] = null;
                 }
             }
         } 
         
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
 				LogManager.recordMessage("XMatch of length " + j + " found.",
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
 				LogManager.recordMessage("YMatch of length " + j + " found.", 
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
             if (tile != null && layerMan.exists(tile, Layer.TILE) == false)
             {
                 tile.setVisible(visible);
                 layerMan.add(tile, Layer.TILE);
             }
     }
 	
     /**
      * TODO Documentation.
      * 
      * @param direction
      * @param speed
      */
     public List<IAnimation> startShift(final Direction direction, final int speed)
     {
         // The list of new animations made.
         List<IAnimation> animationList = new ArrayList<IAnimation>();
         
         // The new animation.
         IAnimation a;
         int bound;
         
         // The v.
         int v = speed;
         
         switch (direction)
         {
             case UP:
                 
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {                                               
 //                        board[i].setYMovement(-speed);
 //                        board[i].calculateBound(direction,
 //                                countTilesInDirection(direction, i));
                         bound = calculateBound(direction,
                                 countTilesInDirection(direction, i));
                         
                         a = new MoveAnimation.Builder(board[i]).speed(v)
                                 .minY(bound).theta(90).end();                        
                         animationList.add(a);
                     }
                 }  
                 
                 break;
                 
             case DOWN:
                 
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
 //                        board[i].setYMovement(+speed);
 //                        board[i].calculateBound(direction,
 //                                countTilesInDirection(direction, i));
                         bound = calculateBound(direction,
                                 countTilesInDirection(direction, i));
                         
                         a = new MoveAnimation.Builder(board[i]).speed(v)
                                 .maxY(bound).theta(-90).end();                        
                         animationList.add(a);
                     }
                 }  
                 
                 break;
                 
             case LEFT:
                 
                 // Start them moving left.
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
 //                        board[i].setXMovement(-speed);
 //                        board[i].calculateBound(direction,
 //                                countTilesInDirection(direction, i));
                         bound = calculateBound(direction,
                                 countTilesInDirection(direction, i));
                         
                         a = new MoveAnimation.Builder(board[i]).speed(v)
                                 .minX(bound).theta(180).end();                        
                         animationList.add(a);
                     }
                 }
                 
                 break;
                 
             case RIGHT:
                 
                 // Start them moving left.
                 for (int i = 0; i < cells; i++)
                 {
                     if (board[i] != null)
                     {
 //                        board[i].setXMovement(+speed);
 //                        board[i].calculateBound(direction,
 //                                countTilesInDirection(direction, i));
                         bound = calculateBound(direction,
                                 countTilesInDirection(direction, i));
                         
                         a = new MoveAnimation.Builder(board[i]).speed(v)
                                 .maxX(bound).theta(0).end();                        
                         animationList.add(a);
                     }
                 }
                 
                 break;
                 
             default:
                 throw new AssertionError();                           
         }              
         
         return animationList;
     }    	
     
     /**
      * A convience method for starting a shift in the vertical direction of the
      * currently set gravity.
      * 
      * @param speed
      */
     public List<IAnimation> startVerticalShift(final int speed)
     {
         assert speed != 0;
         
         if (gravity.contains(Direction.DOWN))
             return startShift(Direction.DOWN, speed);
         else
             return startShift(Direction.UP, speed);
     }
     
     /**
      * A convience method for starting a horizontal shift in the direction of 
      * the currently set gravity.
      * 
      * @param speed
      */
     public List<IAnimation> startHorizontalShift(final int speed)
     {
         assert speed != 0;
         
         if (gravity.contains(Direction.LEFT))
             return startShift(Direction.LEFT, speed);
         else
             return startShift(Direction.RIGHT, speed);
     }
     
 	/**
 	 * Moves all currently moving tiles.
 	 * @returns True if there is still more moving to happen.
 	 */
 //	public boolean moveAll(long delta)
 //	{
 //		// Set to true if there are more movement to happen.
 //		boolean moreMovement = false;
 //		
 //		for (int i = 0; i < cells; i++)		
 //			if (board[i] != null)
 //			{
 //				board[i].move(delta);
 //				if (board[i].getXMovement() != 0 
 //						|| board[i].getYMovement() != 0)
 //					moreMovement = true;
 //			}
 //		
 //        // Dirty board.
 //        setDirty(true);
 //        
 //		return moreMovement;
 //	}    
 	
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
     
     public int calculateBound(Direction direction, int tileCount)
     {
         switch (direction)
         {
             case UP:
                 
                 return tileCount * getCellHeight();                
                 
             case DOWN:
                 
                 return getY() + getHeight() - ((tileCount + 1) * getCellHeight());
                 
             case LEFT:
                 
                 return getX() + (tileCount * getCellWidth());                
                 
             case RIGHT:
                 
                 return getX() + getWidth() - ((tileCount + 1) * getCellWidth());
                 
             default: throw new AssertionError();
         }
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
 //        if (t.getClass() != TileEntity.class)
 //        {
 //            if (t.getClass() == X2TileEntity.class || t.getClass() == X3TileEntity.class 
 //                    || t.getClass() == X4TileEntity.class)
 //            {
 //                 
 //                 LogManager.recordMessage("Mult added.", "BoardManager#addMult");
 //                this.incrementNumberOfMults();
 //            }
 //            else
 //            {
 //                LogManager.recordMessage("Item added.", "BoardManager#addTile");
 //                this.incrementNumberOfItems();
 //            }    
 //        }
                        
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
         layerMan.add(t, Layer.TILE);               
         
         // Dirty board.
         setDirty(true);                
     }
     
     public void addTile(final int column, final int row, final TileEntity t)
     {
          addTile(row * columns + column, t);
     }
 	     
     public TileEntity makeTile(TileType type, TileColor color, int x, int y)
     {
         TileEntity t;
         
         switch (type)
         {
             case NORMAL:
                 t = new TileEntity(this, color, x, y);
                 break;
                 
             case X2:
                 t = new X2TileEntity(this, color, x, y);
                 break;
                 
             case X3:
                 t = new X3TileEntity(this, color, x, y);
                 break;
                 
             case X4:
                 t = new X4TileEntity(this, color, x, y);
                 break;
 
             case ROCKET:
                 t = new RocketTileEntity(this, color, x, y);
                 break;
 
             case BOMB:
                 t = new BombTileEntity(this, color, x, y);
                 break;
 
             case STAR:
                 t = new StarTileEntity(this, color, x, y);
                 break;
                 
             case GRAVITY:
                 t = new GravityTileEntity(this, color, x, y);
                 break;
                 
             default: throw new AssertionError("Unknown type.");
         }
         
         return t;
     }
     
     /**
      * Create a new a tile at the specified index using the given class and
      * color.  The new tile is also returned.
      * 
      * @param index
      * @param type
      * @param color
      * @return
      */
 	public TileEntity createTile(final int index, final TileType type, 
             final TileColor color)
 	{
         // Sanity check.
         assert (index >= 0 && index < cells);
         assert (type != null);      
         
         // The new tile.
         int tx = x + (index % columns) * cellWidth;
         int ty = y + (index / columns) * cellHeight;
         TileEntity t = makeTile(type, color, tx, ty);        
                         
         //Increment the item count.
         if (t.getType() != TileType.NORMAL)
         {
             for (Item item : worldMan.getItemList())
             {
                 if(item.getTileType() == t.getType())
                 {
                     item.incrementCurrentAmount();
                     LogManager.recordMessage(item.getTileType() + " has " + item.getCurrentAmount() + " instances.");
                     break;
                 }
             }
         }
         
         // Add the tile.
         addTile(index, t);
         if (t.getClass() != TileEntity.class)
         {
           if (t.getClass() == X2TileEntity.class || t.getClass() == X3TileEntity.class 
                     || t.getClass() == X4TileEntity.class)
             {
                  
                  LogManager.recordMessage("Mult added.", "BoardManager#addMult");
                 this.incrementNumberOfMults();
             }
             else
             {
                 LogManager.recordMessage("Item added.", "BoardManager#addTile");
                 this.incrementNumberOfItems();
             }    
         }
         
         // Return the tile.
         return t;
 	}
     
     public TileEntity createTile(final int column, final int row, 
             final TileType type, final TileColor color)
     {
         return createTile(row * columns + column, type, color);
     }
     
     public TileEntity createTile(final int index, final TileType type)
     {
         return createTile(index, type, 
                 TileColor.getRandomColor(getNumberOfColors()));
     }        
     
     public TileEntity createTile(final int column, final int row, 
             final TileType type)
     {
         return createTile(row * columns + column, type);
     }
     
     public TileEntity cloneTile(TileEntity tile)
     {
         assert tile != null;
         
         TileType type = tile.getType();
         TileEntity t = makeTile(type, tile.getColor(), tile.getX(), tile.getY());
         
         switch(type)
         {
             case ROCKET:
                 RocketTileEntity rocketTile = (RocketTileEntity) tile;
                 RocketTileEntity rt = (RocketTileEntity) t;                
                 rt.setDirection(rocketTile.getDirection());                
                 break;                                
         }        
         
         return t;
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
         
         // Decrement the item count.
         if (t.getType() != TileType.NORMAL)
         {
             for (Item item : worldMan.getItemList())
             {
                 if(item.getTileType() == t.getType())
                 {
                     item.decrementCurrentAmount();
                     LogManager.recordMessage(item.getTileType() + " has " + item.getCurrentAmount() + " instances.");
                     break;
                 }
             }
         }
         
         // If this is an item, decrement the item count.
         if (t.getClass() != TileEntity.class)
         {
             //check the class
             if (t.getClass() == X2TileEntity.class || t.getClass() == X3TileEntity.class 
                     || t.getClass() == X4TileEntity.class)
             {
                 this.decrementNumberOfMults();
             }
             else
                 this.decrementNumberOfItems();
         }
         
         // Remove from layer manager.
         if (layerMan.exists(t, Layer.TILE))
             layerMan.remove(t, Layer.TILE);
         
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
 		if (column < 0 || column >= columns)
             throw new IndexOutOfBoundsException(
                     "Column out of bounds: " + column + ".");
         
         if (row < 0 || row >= rows)
             throw new IndexOutOfBoundsException(
                     "Row out of bounds: " + row + ".");
 		
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
             row    = rocketIndex / columns;
             
             // Depending on the direction, collect the appropriate tiles.                                           
             RocketTileEntity.Direction dir = 
                     ((RocketTileEntity) getTile(rocketIndex))
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
      * Scans the tile set for specified tile and places them in a passed item 
      * set.
      * 
      * @param tileType The type of tile to scan for.
      * @param tileSet  The tile set to scan in.
      * @param foundSet The set to store the found tiles in (may be null).
      * @return The number of tiles of that type found.
      */
     public int scanFor(TileType tileType, 
             Set<Integer> tileSet, 
             Set<Integer> foundSet)
     {        
         assert tileType != null;
         assert tileSet  != null;        
         
         // The number of items found.
         int count = 0;
         
         for (Integer index : tileSet)
         {            
             if (getTile(index).getType() == tileType)            
             {
                 count++;                
                 if (foundSet != null) foundSet.add(index);                            
             }
         } // end for
         
         return count;
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
         int wait = 0;
         int deltaWait = SettingsManager.get().getInt(Key.ANIMATION_ROWFADE_WAIT);
         int duration  = SettingsManager.get().getInt(Key.ANIMATION_ROWFADE_DURATION);
         
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
                 IAnimation a = new FadeAnimation.Builder(FadeAnimation.Type.IN, t)
                         .wait(wait).duration(duration).end();  
                 
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
      * Slides the board on the screen and fades it in as it does so.
      * 
      * @return An animation object that can be tested for doneness.
      */
     private IAnimation animateSlideFadeIn()
     {
         // The settings manager.
         SettingsManager settingsMan = SettingsManager.get();
         
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
                 layerMan.add(t, Layer.TILE);
                 
                 // Count it.
                 tileCount++;
                 
                 int fadeWait     = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_FADE_WAIT);
                 int fadeDuration = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_FADE_DURATION);
                 
                 // Create the animation.
                 a1 = new FadeAnimation.Builder(FadeAnimation.Type.IN, t)
                         .wait(fadeWait).duration(fadeDuration).end();
                 
                 // Make the animation remove itself.                
                 a1.setFinishRunnable(new Runnable()
                 {
                    public void run()
                    {
                        layerMan.remove(t, Layer.TILE);
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
                     maxX = tile.getX();
                 }
                 else if (theta == 180)
                 {
                     t.translate((int) (500.0 * 0.15), 0);
                     minX = tile.getX();
                 }
                 else
                     throw new AssertionError("Angle should only be 0 or 180.");
                 
                 int moveWait     = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_MOVE_WAIT);                
                 int moveSpeed    = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_MOVE_SPEED);
                 
                 a2 = new MoveAnimation.Builder(t)
                         .minX(minX).maxX(maxX)
                         .wait(moveWait)
                         .theta(theta).speed(moveSpeed).end();                                
                 
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
         int deltaWait = SettingsManager.get().getInt(Key.ANIMATION_ROWFADE_WAIT);
         int duration  = SettingsManager.get().getInt(Key.ANIMATION_ROWFADE_DURATION);
         
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
                 IAnimation a = new FadeAnimation.Builder(FadeAnimation.Type.OUT, t)
                         .wait(wait).duration(duration).end();                 
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
                 layerMan.add(t, Layer.TILE);
                 
                 // Count it.
                 tileCount++;
                 
                 // The settings manager.
                 SettingsManager settingsMan = SettingsManager.get();
                 
                 int fadeWait     = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_FADE_WAIT);
                 int fadeDuration = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_FADE_DURATION);
                 int moveWait     = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_MOVE_WAIT);    
                 int moveDuration = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_MOVE_DURATION);    
                 int moveSpeed    = settingsMan.getInt(Key.ANIMATION_SLIDEFADE_MOVE_SPEED);
                 
                 // Create the animation.
                 a1 = new FadeAnimation.Builder(FadeAnimation.Type.OUT, t)
                         .wait(fadeWait).duration(fadeDuration).end();               
                                 
                 a2 = new MoveAnimation.Builder(t).wait(moveWait)
                         .duration(moveDuration)
                         .theta(180 * (row % 2))
                         .speed(moveSpeed).end();
                 
                 // Make the animation remove itself.                
                 a1.setFinishRunnable(new Runnable()
                 {
                    public void run()
                    {
                        layerMan.remove(t, Layer.TILE);
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
     public ImmutablePosition determineCenterPoint(final Set<Integer> indexSet)
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
                 LogManager.recordWarning("Tile was null: " + index , 
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
         return new ImmutablePosition(cx, cy);
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
     
     public void insertItemRandomly(TileType type)
     {
         // Get a random tile location.
         int [] locations = this.getTileLocations();
         
         if(locations == null)
             return;
         
         int random = Util.random.nextInt(locations.length);
         
         int index = locations[random];
         // Remove the old, insert the new.
         TileColor color = getTile(index).getColor();
         this.removeTile(index);
         this.createTile(index, type, color);
         
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
 
     public int getNumberOfMults()
     {
         return this.numberOfMults;
     }
     
     public void setNumberOfMults(final int numberOfmults)
     {
         this.numberOfMults = numberOfmults;
     }
     
     public void decrementNumberOfMults()
     {
         this.numberOfMults--;
     }
     
     public void incrementNumberOfMults()
     {
         this.numberOfMults++;
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
 
     private int[] getTileLocations()
     {
         int size = this.getNumberOfTiles()-this.getNumberOfItems()
                 -this.getNumberOfMults();
         
         if(size <= 0)
             return null;
         
         int[] locations = new int[size];  
         TileEntity temp;
         int count = 0;
         
         for(int i = 0; i < this.cells; i++)
         {
             temp = this.getTile(i);
             if (temp != null && temp.getType() == TileType.NORMAL)
             {
                 locations[count++] = i;
             }
         }
         
         return locations;
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
         LogManager.recordMessage("Board visible: " + visible + ".", 
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
 
     public ImmutableRectangle getShape()
     {
         return shape;
     }        
 
     public void saveState()
     {
         managerState.put(Keys.NUMBER_OF_COLORS, numberOfColors);
         managerState.put(Keys.NUMBER_OF_ITEMS, numberOfItems);
         managerState.put(Keys.NUMBER_OF_MULTS, numberOfMults);
         managerState.put(Keys.NUMBER_OF_TILES, numberOfTiles);
         managerState.put(Keys.GRAVITY, gravity);
         managerState.put(Keys.BOARD, board.clone());
         managerState.put(Keys.SCRATCH_BOARD, scratchBoard.clone());
         
         LogManager.recordMessage("Saved " + numberOfTiles + " tiles.");  
         LogManager.recordMessage("Saved " + numberOfItems + " items.");
         LogManager.recordMessage("Saved " + numberOfMults + " mults.");
     }
 
     @SuppressWarnings("unchecked") 
     public void loadState()
     {
         // Clear the board.
         clearBoard();
                 
         numberOfColors = (Integer) managerState.get(Keys.NUMBER_OF_COLORS);
         numberOfItems = (Integer) managerState.get(Keys.NUMBER_OF_ITEMS);
         numberOfTiles = (Integer) managerState.get(Keys.NUMBER_OF_TILES);  
         numberOfMults = (Integer) managerState.get(Keys.NUMBER_OF_MULTS);
         gravity = (EnumSet<Direction>) managerState.get(Keys.GRAVITY);
         scratchBoard = (TileEntity[]) managerState.get(Keys.SCRATCH_BOARD);   
         board = (TileEntity[]) managerState.get(Keys.BOARD);     
                        
         // Make sure that this board is in the layer manager.
         layerize();
         
          // readd the item counts.
         for (TileEntity t : board)
         {
             if (t == null)
                 continue;
             
             if (t.getType() != TileType.NORMAL)
             {
                 for (Item item : worldMan.getItemList())
                 {
                     if(item.getTileType() == t.getType())
                     {
                         item.incrementCurrentAmount();
                         LogManager.recordMessage(item.getTileType() + " has " + item.getCurrentAmount() + " instances.");
                         break;
                     }
                 } // end for
             } // end if
         }
         
         LogManager.recordMessage("Loaded " + numberOfTiles + " tiles.");
         LogManager.recordMessage("Loaded " + numberOfItems + " items.");
         LogManager.recordMessage("Loaded " + numberOfMults + " mults.");
     }
 
     public void resetState()
     {
         // Reset the number of colours.
         setNumberOfColors(DEFAULT_NUMBER_OF_COLORS);
     }
     
     public int asColumn(int index)
     {
         return (index % columns);
     }
     
     public int asRow(int index)
     {
         return (index / columns);
     }
     
     /**
      * Returns the relative position of the two tiles, relative to a.
      * For instance, if Direction.LEFT was returned, then that would mean
      * that "a is left of b".
      * 
      * @param a
      * @param b
      * @return
      */
     public Direction relativeColumnPosition(int a, int b)
     {                
         if (asColumn(a) > asColumn(b))
             return Direction.RIGHT;
         else if (asColumn(a) < asColumn(b))
             return Direction.LEFT;
         else
             return Direction.NONE;
     }
     
     /**
      * Returns the relative position of the two tiles, relative to a.
      * For instance, if Direction.UP was returned, then that would mean
      * that "a is above b".
      * 
      * @param a
      * @param b
      * @return
      */
     public Direction relativeRowPosition(int a, int b)
     {
         if (asRow(a) > asRow(b))
             return Direction.DOWN;
         else if (asRow(a) < asRow(b))
             return Direction.UP;
         else
             return Direction.NONE;
     }
 
 }
