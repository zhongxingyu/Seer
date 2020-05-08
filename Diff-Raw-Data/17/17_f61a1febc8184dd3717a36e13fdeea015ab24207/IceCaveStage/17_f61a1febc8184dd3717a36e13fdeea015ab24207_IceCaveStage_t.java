 package com.android.icecave.mapLogic;
 
 import java.io.Serializable;
 import java.util.LinkedList;
 import java.util.Random;
 
 import com.android.icecave.utils.Point;
 
 import com.android.icecave.general.EDifficulty;
 import com.android.icecave.general.EDirection;
 import com.android.icecave.general.GeneralServiceProvider;
 import com.android.icecave.mapLogic.collision.ICollisionable;
 import com.android.icecave.mapLogic.tiles.BoulderTile;
 import com.android.icecave.mapLogic.tiles.EmptyTile;
 import com.android.icecave.mapLogic.tiles.FlagTile;
 import com.android.icecave.mapLogic.tiles.IBlockingTile;
 import com.android.icecave.mapLogic.tiles.ITile;
 import com.android.icecave.mapLogic.tiles.WallTile;
 import com.android.icecave.mapLogic.tiles.validators.TileValidatorFactory;
 
 /**
  * Class representing a stage in the game.
  * @author Tom
  *
  */
 @SuppressWarnings("serial")
 public class IceCaveStage implements Serializable
 {
 	/**
 	 * The tiles of the current stage board.
 	 */
 	private ITile[][] mTiles;
 	
 	/**
 	 * Number of moves for the current stage.
 	 */
 	private int mMoves;
 	
 	/**
 	 * Find a location on the board to place the flag in.
 	 * 
	 * @param colsNumber 	- Number of columns in the board.
	 * @param rowsNumber 	- Number of rows in the board.
 	 * @param playerLoc - Location of the player.
 	 * 
 	 * @return Location to place the flag.
 	 */
	private Point createExit(int colsNumber, int rowsNumber, Point playerLoc)
 	{
 		// Get a random number
 		Random rand = GeneralServiceProvider.getInstance().getRandom();
 		
		int flagXposition = rand.nextInt(rowsNumber - 2) + 1;
		int flagYposition = rand.nextInt(colsNumber - 2) + 1;
 		
 		while(playerLoc.equals(flagXposition, flagYposition)){
			flagXposition = rand.nextInt(rowsNumber - 2) + 1;
			flagYposition = rand.nextInt(colsNumber - 2) + 1;
 		}
 			
 		return new Point(flagXposition, flagYposition);
 	}
 
 	/**
 	 * Validate a point on the board.
 	 * @param toCheck - Point to validate.
 	 * @return true if point is valid.
 	 */
 	public boolean validatePoint(Point toCheck)
 	{
 		return ((toCheck.x > 0 			    || 
 				 toCheck.x < mTiles[0].length - 1) && 
 				(toCheck.y > 0 				|| 
 				 toCheck.y < mTiles.length - 1));
 	}
 	
 	/**
 	 * Move the player one tile in a direction.
 	 * @param playerLocation - Current player location.
 	 * @param direction - Direction to move the player in.
 	 */
 	public Point movePlayerOneTile(Point playerLocation, EDirection direction) {
 		// Get the next index on the board for the player.
 		Point nextPoint = new Point(playerLocation);
 		nextPoint.offset(direction.getDirection().x,direction.getDirection().y);
 		
 		ICollisionable collisionable = (ICollisionable)
 													mTiles[nextPoint.y]
 														  [nextPoint.x];
 		
 		// Call the tile that the player will meet.
 		MapLogicServiceProvider.getInstance().
 								getCollisionManager().
 								handleCollision(collisionable);
 		
 		return nextPoint;
 	}
 	
 	/**
 	 * Get the number of minimal moves for this stage.
 	 * @return Minimal number of moves.
 	 */
 	public int getMoves() {
 		return mMoves;
 	}
 	
 	/**
 	 * Get the board of the stage.
 	 * @return The current stage's board.
 	 */
 	public ITile[][] getBoard() {
 		return mTiles;
 	}
 	
 	/**
 	 * Initialize the board.
 	 * @param colsNumber - Number of rows in board.
 	 * @param rowsNumber - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 */
 	private void initializeBoard(int colsNumber, int rowsNumber, int wallWidth)
 	{
 		createEmptyBoard(colsNumber, rowsNumber);
 
 		fillWithEmptyTles(colsNumber, rowsNumber, wallWidth);
 	}
 
 	/**
 	 * Fill the initialized board with empty tiles.
 	 * 
 	 * @param colsNumber - Number of rows in board.
 	 * @param rowsNumber - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 */
 	private void fillWithEmptyTles(int colsNumber, int rowsNumber, int wallWidth) {
 		for (int i = wallWidth; i < colsNumber - wallWidth; i++)
 		{
 			for (int j = wallWidth; j < rowsNumber - wallWidth; j++)
 			{
 				// Initializing board.
 				mTiles[i][j] = new EmptyTile(j,i);
 			}
 		}
 	}
 
 	/**
 	 * Creates an empty board, all tiles are walls.
 	 * @param colsNumber - Number of rows in board.
 	 * @param rowsNumber - Number of columns in board.
 	 */
 	private void createEmptyBoard(int colsNumber, int rowsNumber) {
 		// Initializing walls
 		for (int i = 0; i < colsNumber; i++)
 		{
 			for (int j = 0; j < rowsNumber; j++)
 			{
 				mTiles[i][j] = new WallTile(j,i);
 			}
 		}
 	}
 
 	/**
 	 * Generating a possible to beat map
 	 * 
 	 * @param difficulty - Difficulty for the stage.
 	 * @param rowsNumber - Number of rows in board.
 	 * @param colsNumber - Number of columns in board.
 	 * @param wallWidth - Width of the wall in tiles.
 	 * @param playerLoc - Starting location for the player.
 	 * @param boulderNum - Number of boulders in the board.
 	 * @param startingMove - First move of the player to do (while building the board).
 	 */
 	public void buildBoard(EDifficulty difficulty, 
 						   int 		   rowsNumber, 
 						   int 		   colsNumber, 
 						   int 		   wallWidth, 
 						   Point 	   playerLoc, 
 						   int 	       boulderNum,
 						   EDirection  startingMove)
 	{
 		// Initialize members.
 		mTiles = new ITile[colsNumber][rowsNumber];
 		
 		// Place tiles in the board.
 		placeTiles(colsNumber, rowsNumber, wallWidth, playerLoc, boulderNum);			
 	
 		//printBoard(mTiles);
 		
 		// Validating the matrix path
 		// If the validate turns false
 		while (!validate(startingMove, playerLoc, difficulty)){
 
 			// Re-initializing map
 			placeTiles(colsNumber, rowsNumber, wallWidth, playerLoc, boulderNum);			
 			
 //			printBoard(mTiles);
 		}
 	}
 
 	/**
 	 * 
 	 */
 //	private void printBoard(ITile[][] board)
 //	{
 //		String result = "";
 //		int nRowIndex = 0;
 //		for (ITile[] rowTiles : board)
 //		{
 //			result += nRowIndex;
 //			result += ")   ";
 //			nRowIndex++;
 //			for (ITile iTile : rowTiles)
 //			{
 //				if(iTile instanceof FlagTile)
 //				{
 //					result += "F";
 //				}
 //				else if(iTile instanceof BoulderTile)
 //				{
 //					result += "B";
 //				}
 //				else if(iTile instanceof WallTile)
 //				{
 //					result += "W";
 //				}
 //				else if(iTile instanceof EmptyTile)
 //				{
 //					result += ".";
 //				}
 //				else
 //				{
 //					result += "!";
 //				}
 //				result += "    ";
 //			}
 //			result += "\n";
 //		}
 //		System.out.println(result);
 //	}
 
 	
 	/**
 	 * Place all tiles on the board.
 	 * 
 	 * @param colsNumber - Board row length in tiles. 
 	 * @param rowsNumber - Board column length in tiles.
 	 * @param wallWidth - Width of the wall in tiles.
 	 * @param playerLoc - Player location.
 	 * @param boulderNum - Number of boulders to place.
 	 */
 	private void placeTiles(int colsNumber, int rowsNumber, int wallWidth,
 			Point playerLoc, int boulderNum) {
 		// Creating the board
 		initializeBoard(colsNumber, rowsNumber, wallWidth);
 
 		// Creating exit point
 		Point flagLocation = 
 				createExit(colsNumber, rowsNumber, playerLoc);
 		
 		mTiles[flagLocation.y][flagLocation.x] = new FlagTile(flagLocation);
 
 		// Place the boulders on the board.
 		placeBoulders(colsNumber,
 					  rowsNumber, 
 					  playerLoc,
 					  boulderNum);
 	}
 
 	/**
 	 * Place boulders on the board.
 	 * @param colsNumber - Row length of the board in tiles.
 	 * @param rowsNumber - Column length of the board in tiles.
 	 * @param playerLoc - Player location.
 	 * @param boulderNum - Number of boulders to place on board.
 	 */
 	private void placeBoulders(int colsNumber,
 							   int rowsNumber,
 							   Point playerLoc,
 							   int boulderNum) {
 		Random rand = 
 				GeneralServiceProvider.getInstance().getRandom();
 		
 		int boulderColRand, boulderRowRand, boulderCounter = 0;
 		
 		// Place boulders.
 		// TODO: Might be an infinite loop.
 		int retryCounter = 0;
 		
 		// Check if we placed all boulders, 
 		// or failed placing one for ten times in a row.
 		while (retryCounter < 10 && boulderCounter < boulderNum)
 		{
 			// Making random points
 			boulderRowRand = rand.nextInt(rowsNumber - 2) + 1;
 			boulderColRand = rand.nextInt(colsNumber - 2) + 1;
 
 			// Validate the position.
 			TileValidatorFactory tileValidatorFactory =
 					MapLogicServiceProvider.getInstance().getTileValidatorFactory();
 			
 			// If the location is not valid.
 			if(!tileValidatorFactory.validate(BoulderTile.class, 
 											 boulderRowRand, 
 											 boulderColRand, 
 											 playerLoc.x, 
 											 playerLoc.y, 
 											 mTiles)){
 				retryCounter++;
 				continue;
 			}
 			
 			mTiles[boulderColRand][boulderRowRand] = 
 					new BoulderTile(boulderRowRand, boulderColRand);
 			
 			// Increase counter after creating a boulder
 			boulderCounter++; 
 		}
 	}
 
 	/**
 	 * Validate that the map is solvable in a specific number of moves.
 	 * @param defaultMoveDirection - The first direction the player moves in.
 	 * @param playerPoint - The starting location of the player.
 	 * @param difficulty - The difficulty of the stage.
 	 * @return true if valid.
 	 */
 	private boolean validate(EDirection  defaultMoveDirection,
 							 Point 	     playerPoint,
 							 EDifficulty difficulty)
 	{
 		// TODO: Validate the number of steps is good.
 		// Create the root node
 		MapNode root = new MapNode(null, mTiles[playerPoint.y][playerPoint.x]);
 
 		// Clear the previous stuff
 		root.clear();
 
 		// Add the root to the stack
 		root.addRoot();
 
 		boolean[][] visited = 
 				new boolean[mTiles.length][mTiles[0].length];
 //						[EDirection.values().length];
 		
 		LinkedList<MapNode> queue = new LinkedList<MapNode>();
 		
 		// Add the root.
 		queue.add(root);
 		
 		MapNode mapNode = null;
 		MapNode flagNode = null; 
 		
 		// While the queue is not empty.
 		while(!queue.isEmpty()){
 			// Pop the node.
 			mapNode = queue.remove();
 			
 			if(mapNode.getValue() instanceof FlagTile){
 				flagNode = mapNode;
 				break;
 			}
 			
 			for (EDirection direction : EDirection.values()) {
 				Point currPoint = new Point(mapNode.getValue().getLocation());
 				getMove(direction, currPoint);
 				if(!visited[currPoint.y][currPoint.x]){
 					visited[currPoint.y][currPoint.x] = true;
 					MapNode newNode = new MapNode(mapNode, mTiles[currPoint.y][currPoint.x]);
 					// Push the node.
 					queue.add(newNode);
 					mapNode.push(newNode);
 				}
 			}
 		}
 		
 //		ArrayList<MapNode> flags = new ArrayList<MapNode>();
 //		getTheFlagNode(root, flags);
 //		
 //		MapNode flagNode = null;
 //		int lowestSteps = Integer.MAX_VALUE;
 //		for (MapNode mapNode : flags)
 //		{
 //			if(mapNode.getLevel() < lowestSteps){
 //				lowestSteps = mapNode.getLevel();				
 //				flagNode = mapNode;
 //			}
 //		}
 
 		if(flagNode == null){
 			return false;
 		}
 		
 		mMoves = flagNode.getLevel();
 		
 		// Check if it's OK.
 		if (mMoves >= difficulty.getMinMoves() &&
 			mMoves <= difficulty.getMaxMoves())
 		{
 			System.out.println("Flag level : " + flagNode.getLevel());
 			MapNode flagParent = flagNode.getParent();
 			while(flagParent != null){
 				System.out.println(flagParent.getValue().getLocation());
 				flagParent = flagParent.getParent();
 			}
 			return true;
 //			// Get the number of steps.
 //			m_nStepsTaken = nCurMinSteps;
 //
 //			if (nBreakableObjects == 0)
 //			{
 //				return true;
 //			}
 //			else
 //			{
 //				// Check if breakable nodes exist.
 //				int nRemoveableNum = GetBreakable();
 //
 //				// Check the number of objects.
 //				if (nRemoveableNum < nBreakableObjects)
 //				{
 //					// TODO: What to do?
 //				}
 //
 //				// Just choose the breakable object's you would like.
 //
 //				// Check if succeeded
 //				return (node.IsFlag() && node.GetLevel() >= m_nMinimumNumberOfSteps);
 //			}
 		}
 
 		return false;
 	}
 
 
 	/**
 	 * Get the flag node from the children of the root node.
 	 * @param root - Root node.
 	 * @param flags - Array list of flags.
 	 */
 //	private void getTheFlagNode(MapNode root, ArrayList<MapNode> flags)
 //	{
 //		if(root.getValue() instanceof FlagTile){
 //			flags.add(root);
 //		}
 //		
 //		// Get the flag.
 //		ArrayList<MapNode> nodes = root.getChildren();
 //		for (MapNode mapNode : nodes){
 //			getTheFlagNode(mapNode, flags);
 //			
 //		}
 //	}
 
 	/**
 	 * Change the types matrix boulders that are breakable to breakable.
 	 * 
 	 * @return number of breakable objects.
 	 */
 //	private int GetBreakable()
 //	{
 //		// The current array list of breakable nodes.
 //		ArrayList<Point> lstRowBreakable = new ArrayList<Point>();
 //		ArrayList<Point> lstColBreakable = new ArrayList<Point>();
 //		int nNumberOFBreakable = 0;
 //
 //		for (int i = 0; i < nRowMaxSize; i++)
 //		{
 //			boolean fFoundVisited = false;
 //			lstRowBreakable.clear();
 //			lstColBreakable.clear();
 //
 //			for (int j = 0; j < nColMaxSize; j++)
 //			{
 //				if (fFoundVisited)
 //				{
 //					// Check if we have found another visited place.
 //					if (TempMatrix[i][j])
 //					{
 //						// Go through all the boulders in the current row.
 //						for (SerPoint serPoint : lstRowBreakable)
 //						{
 //							if (TypesMatrix[serPoint.x][serPoint.y] != Types.Breakable)
 //							{
 //								nNumberOFBreakable++;
 //							}
 //
 //							// Set as breakable.
 //							TypesMatrix[serPoint.x][serPoint.y] = Types.Breakable;
 //						}
 //						lstRowBreakable.clear();
 //					}
 //					// Check if we have found another visited place.
 //					if (TempMatrix[j][i])
 //					{
 //						// Go through all the boulders in the current col.
 //						for (SerPoint serPoint : lstColBreakable)
 //						{
 //							if (TypesMatrix[serPoint.x][serPoint.y] != Types.Breakable)
 //							{
 //								nNumberOFBreakable++;
 //							}
 //
 //							// Set as breakable.
 //							TypesMatrix[serPoint.x][serPoint.y] = Types.Breakable;
 //						}
 //						lstColBreakable.clear();
 //					}
 //
 //					if (TypesMatrix[i][j] == Types.Boulder)
 //					{
 //						lstRowBreakable.add(new SerPoint(i, j));
 //					}
 //					if (TypesMatrix[j][i] == Types.Boulder)
 //					{
 //						lstColBreakable.add(new SerPoint(j, i));
 //					}
 //				}
 //				else if (TempMatrix[i][j])
 //				{
 //					fFoundVisited = true;
 //				}
 //			}
 //		}
 //
 //		return nNumberOFBreakable;
 //	}
 
 	/**
 	 * Fill nodes in the board map, return the end node.
 	 * @param curNode - Current received node.
 	 * @param lastDirection - Direction to move the player in.
 	 * @param playerPoint - The current location of the player.
 	 * @return
 	 */
 //	private MapNode fillNodes(MapNode      curNode,
 //							  EDirection   lastDirection, 
 //							  Point        playerPoint,
 //							  boolean[][][] visited)
 //	{
 //		// Check if root.
 //		if (curNode == null)
 //		{
 //			return curNode;
 //		}
 //		
 //		// Checking if the current place has been checked before from that direction.
 //		if (!visited[playerPoint.y][playerPoint.x][lastDirection.ordinal()])
 //		{
 //			// Making the currently visited place false, to not
 //			// visit it again
 //			visited[playerPoint.y][playerPoint.x][lastDirection.ordinal()] = true;
 //
 //			// Go through the available directions and fill the map.
 //			for (EDirection direction : EDirection.values()) {
 //				fillNodesInDirection(curNode,
 //								     direction, 
 //								     lastDirection, 
 //								     playerPoint,
 //								     visited);
 //			}
 //		}
 //
 //		return curNode.peek();
 //	}
 
 	/**
 	 * @param src
 	 * @param dst
 	 */
 //	private void copy3dimentionalArray(boolean[][][] src,
 //										boolean[][][] dst)
 //	{
 //		for (int i = 0; i < dst.length; i++)
 //		{
 //			for (int j = 0; j < dst[0].length; j++)
 //			{
 //				for (int k = 0; k < dst[0][0].length; k++)
 //				{
 //					dst[i][j][k] = src[i][j][k];
 //				}
 //			}
 //		}
 //	}
 
 	/**
 	 * Fill the nodes in a specific direction. 
 	 * @param curNode - Current node to be added.
 	 * @param toMove - The direction to move the player in.
 	 * @param toMove - The last direction the player moved in.
 	 * @param playerPoint - The location of the player.
 	 */
 //	private void fillNodesInDirection(MapNode    curNode, 
 //									  EDirection toMove, 
 //									  EDirection lastMove,
 //									  Point playerPoint,
 //									  boolean[][][] visited) {
 //		MapNode newNode;
 //		Point pntNewPoint;
 //
 //		pntNewPoint = new Point(playerPoint);
 //		
 //		// Validate the point.
 //		if(!validateFillNodesInDirection(curNode, toMove, lastMove, playerPoint)){
 //			return;
 //		}
 //		getMove(toMove, pntNewPoint);
 //		
 //		newNode = 
 //				curNode.push(mTiles[pntNewPoint.y][pntNewPoint.x]);
 //
 //		boolean[][][] newVisited = 	
 //				new boolean[mTiles.length][mTiles[0].length][EDirection.values().length];
 //
 //		copy3dimentionalArray(visited, newVisited);
 //
 //		
 //		fillNodes(newNode, toMove, pntNewPoint, newVisited);
 //	}
 
 	/**
 	 * Validate the parameters for the fillNodesInDirection function.
 	 * @param curNode - The current map node checked.
 	 * @param toMove - The direction to move the player in.
 	 * @param lastMove - The last move that was taken out.
 	 * @param playerPoint - The position of the player.
 	 */
 //	private boolean validateFillNodesInDirection(MapNode    curNode,
 //	                                             EDirection toMove,
 //	                                             EDirection lastMove,
 //	                                             Point      playerPoint)
 //	{
 //		if(stopFillingNodes(curNode)){
 //			System.out.println("Stop filling nodes");
 //			return false;
 //		}
 //		
 //		if(mTiles[playerPoint.y + toMove.getDirection().y]
 //				 [playerPoint.x + toMove.getDirection().x] instanceof IBlockingTile){
 //			return false;
 //	    }
 //			
 //		if(lastMove.equals(toMove.getOpositeDirection())){
 //			return false;
 //		}
 //		
 //		return true;
 //	}
 
 	/**
 	 * Check if to stop filling nodes.
 	 * @param curNode - Current node.
 	 * @return true if stop filling node.
 	 */
 //	private boolean stopFillingNodes(MapNode curNode)
 //	{
 //		return curNode.getValue() instanceof FlagTile;
 //	}
 
 	/**
 	 * Get the new location after making move.
 	 * @param toMove - Direction to move.
 	 * @param currPoint - The current point of the player.
 	 * @return Location of the player after making the move.
 	 */
 	private Point getMove(EDirection toMove, Point currPoint)
 	{
 		// Getting the current point
 		ITile tileCurr = 
 				mTiles[currPoint.y + toMove.getDirection().y]
 					  [currPoint.x + toMove.getDirection().x];
 
 		// While not blocked
 		while (!(tileCurr instanceof IBlockingTile))
 		{
 			currPoint.x += toMove.getDirection().x;
 			currPoint.y += toMove.getDirection().y;
 
 			// Stopping if reached exit.
 			// TODO: make this more generic.
 			if (tileCurr instanceof FlagTile)
 			{
 				break;
 			}
 
 			// Advance
 			tileCurr =
 				mTiles[currPoint.y + toMove.getDirection().y]
 					  [currPoint.x + toMove.getDirection().x];
 		}
 		
 		return (currPoint);
 	}
 }
 
