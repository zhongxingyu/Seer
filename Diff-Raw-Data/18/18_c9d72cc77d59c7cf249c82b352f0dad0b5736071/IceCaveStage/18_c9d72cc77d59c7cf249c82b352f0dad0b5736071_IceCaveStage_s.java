 package com.tas.icecaveLibrary.mapLogic;
 
 import java.io.Serializable;
 
 import com.tas.icecaveLibrary.general.EDifficulty;
 import com.tas.icecaveLibrary.general.EDirection;
 import com.tas.icecaveLibrary.mapLogic.collision.ICollisionable;
 import com.tas.icecaveLibrary.mapLogic.tiles.EmptyTile;
 import com.tas.icecaveLibrary.mapLogic.tiles.ITile;
 import com.tas.icecaveLibrary.utils.Point;
 
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
 	private IceCaveBoard mBoard;
 	
 	/**
 	 * The tiles of the current stage board.
 	 */
 	private IceCaveBoard mTempBoard;
 	
 	/**
 	 * Reset the stage.
 	 * @throws CloneNotSupportedException 
 	 */
 	public void reset() throws CloneNotSupportedException{
 		mBoard = (IceCaveBoard) mTempBoard.clone();
 		mTempBoard = (IceCaveBoard) mBoard.clone();
 	}
 	
 	/**
 	 * Validate a point on the board.
 	 * @param toCheck - Point to validate.
 	 * @return true if point is valid.
 	 */
 	public boolean validatePoint(Point toCheck)
 	{
 		return ((toCheck.x > 0 			    || 
 				 toCheck.x < mBoard.getColumnNum() - 1) && 
 				(toCheck.y > 0 				|| 
 				 toCheck.y < mBoard.getRowsNum() - 1));
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
 		
 		ICollisionable collisionable = 
 				(ICollisionable)mBoard.getTile(nextPoint);
 		
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
 		return mBoard.getMinMoves();
 	}
 	
 	/**
 	 * Get the board of the stage.
 	 * @return The current stage's board.
 	 */
 	public IceCaveBoard getBoard() {
 		return mBoard;
 	}
 	
 	/**
 	 * Generating a a stage from a fixed map.
 	 * 
 	 * @param board - The fixed board to initialize the stage with.
 	 * @throws CloneNotSupportedException 
 	 */
 	public void buildBoard(IceCaveBoard board) throws CloneNotSupportedException
 	{
 		// Initialize members.
 		mBoard = (IceCaveBoard) board.clone();
 		mTempBoard = (IceCaveBoard) board.clone();
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
 	 * @throws CloneNotSupportedException 
 	 */
 	public void buildBoard(EDifficulty difficulty, 
 						   int 		   rowsNumber, 
 						   int 		   colsNumber, 
 						   int 		   wallWidth, 
 						   Point 	   playerLoc, 
 						   int 	       boulderNum,
 						   EDirection  startingMove) throws CloneNotSupportedException
 	{
 		mBoard = 
 				new IceCaveBoard(rowsNumber, 
 								 colsNumber, 
 								 playerLoc, 
 								 startingMove,
 								 boulderNum,
 								 wallWidth,
 								 difficulty);
		mTempBoard = (IceCaveBoard) mBoard.clone();
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
 	 * Get a tile from a location of the board.
 	 * @param location - Location on the board to get a tile from.
 	 * @return ITile from the requested location or null if error.
 	 */
 	public ITile getTile(Point location)
 	{
 		return (ITile) mBoard.getTile(location);
 	}
 
 	/**
 	 * Remove the tile in the location, 
 	 * replacing it with an empty tile.
 	 * @param collisionPoint - Location on the board.
 	 * @return true if successful.
 	 */
 	public boolean removeTile(Point location)
 	{
 		return mBoard.setTile(location, new EmptyTile(location));
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
 
 }
 
