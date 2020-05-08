 package students.ramusivich;
 
 import edu.drexel.cs.ai.othello.*;
 import edu.drexel.cs.ai.othello.GameState.Player;
 
 import java.util.AbstractSet;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Date;
 
 public class StevesOthelloPlayer extends OthelloPlayer {
 	
 	private MiniMax tree;
 
 	private static final int CORNER_SCORE = 1000;
 	private static final int FRONTIER_PENALTY = -20;
 	private static final int NEXT_TO_CORNER_PENALTY = -500;
 	
 	private int STONE_PENALTY;
 
 	public StevesOthelloPlayer(String name)
 	{
 		super(name);
 		tree = null;
    STONE_PENALTY = -20;
 	}
 	
 	//heuristic ideas taken from: http://radagast.se/othello/howto.html
 	private double heuristic(GameState state, GameState previousState)
 	{
 		double score = 0;
    int numberOfStones = state.getScore(state.getCurrentPlayer());

    //Minimize in start game
    if(numberOfStones < 32)
      STONE_PENALTY = -20;
    else //Maximize in end game
      STONE_PENALTY = 10;
 
 		score += (numberOfStones * STONE_PENALTY);
 		
 		Square previousMove = state.getPreviousMove();
 		
 		//Favor corner squares
 		if(IsCorner(previousMove))
 			score += CORNER_SCORE;
 		
 		//Avoid giving your opponent the corner square
 		//Only avoid if the corner is empty
 		if(isNextToCornerPenalty(state, previousMove))
 			score += NEXT_TO_CORNER_PENALTY;
 		
 		//Want to minimize frontier
 		int newFrontiers = getFrontiers(state) - getFrontiers(previousState);
 		score += (newFrontiers * FRONTIER_PENALTY);
 		
 		return score;
 	}
 
 	protected final long howMuchTimesLeft(Date currentDeadline) {
 		if(currentDeadline == null)
 			return 0;
 		else
 			return currentDeadline.getTime() - (new Date()).getTime();
 	}
 
 	public Date getDefaultTime() {
 		return new Date(System.currentTimeMillis() + 30000);
 	}
 
 	public Square getMove(GameState currentState, Date deadline) {
 		try {
 			if (deadline == null) {
 				deadline = getDefaultTime();
 			}
 
 		} catch (NullPointerException e) {
 			deadline = getDefaultTime();
 		}
 		
 		if(tree == null)
 			tree = new MiniMax(new MiniMaxNode(currentState, currentState.getScore(currentState.getCurrentPlayer())));
 		else
 		{
 			try{
 				//BufferedWriter out = new BufferedWriter(new FileWriter("output.txt", true));
 				
 				//out.write("\n---------------------------------------------------------------");
 				//out.write("\ncount before setting a new head: " + tree.Count());
 				
 				try{
 					tree.SetNewHead(new MiniMaxNode(currentState, 0));
 				}
 				catch(Exception e)
 				{
 					tree = new MiniMax(new MiniMaxNode(currentState, currentState.getScore(currentState.getCurrentPlayer())));
 					//out.write("\n\nFailed to update head to: " + currentState.toString());
 				}
 
 				//out.write("\ncount after setting a new head: " + tree.Count());
 				
 				//out.close();
 			} catch (Exception e)
 			{
 				System.out.println("CAUGHT ERROR: " + e.getMessage());
 			}
 		}
 
 		Square nextMove = null;
 		MiniMaxNode node = null;
 		GameState bestBoard = null;
 		
 		//NEED TO FIND A BETTER WAY TO DO THIS!
 		while(this.howMuchTimesLeft(deadline) > 500 && tree.GetEmptyParents().size() > 0)
 		{
 			node = tree.GetEmptyParents().get(0);
 			
 			AbstractSet<GameState> successors = ((GameState)node.GetObject()).getSuccessors();
 			List<MiniMaxNode> children = new ArrayList<MiniMaxNode>();
 			
 			for(GameState state : successors)
 			{
 				children.add(new MiniMaxNode(state, heuristic(state, currentState)));
 			}
 			
 			tree.SetChildren(node, children);
 
 			bestBoard = ((GameState)tree.AlphaBetaSearch().GetObject());
 			nextMove = bestBoard.getPreviousMove();
 			//log("setting current best move: " + nextMove.toString());
 			//log("time left: " + this.howMuchTimesLeft());
 			this.registerCurrentBestMove(nextMove);
 		}
 		
 		/* return the move that we have chosen */
 		log("Steve's player is moving to " + nextMove + " with a cost of: " + heuristic(bestBoard, bestBoard));
 		return nextMove;
 	}
 	
 	public boolean IsCorner(Square square)
 	{
 		int row = square.getRow();
 		int col = square.getCol();
 		
 		if(row == 0)
 		{
 			if(col == 0)
 				return true;
 			else if(col == 7)
 				return true;
 			else
 				return false;
 		}
 
 		else if(row == 7)
 		{
 			if(col == 0)
 				return true;
 			else if(col == 7)
 				return true;
 			else
 				return false;
 		}
 		else
 			return false;
 	}
 	
 	public boolean isNextToCorner(Square square)
 	{
 		int row = square.getRow();
 		int col = square.getCol();
 
 		if(row == 0)
 		{
 			if(col == 1 || col == 6)
 				return true;
 			else
 				return false;
 		}
 		else if(row == 1)
 		{
 			if(col == 0 || col == 1 || col == 6 || col ==7)
 				return true;
 			else
 				return false;
 		}
 		else if(row == 6)
 		{
 			if(col == 0 || col == 1 || col == 6 || col == 7)
 				return true;
 			else
 				return false;
 		}
 		else if(row == 7)
 		{
 			if(col == 1 || col == 6)
 				return true;
 			else
 				return false;
 		}
 		else
 		{
 			return false;
 		}
 	}
 	
 	public boolean IsSquareEmpty(GameState gameState, int row, int col)
 	{
 		Player cornerOwner = gameState.getSquare(row, col);
 		if(cornerOwner == Player.EMPTY)
 			return true;
 		else
 			return false;
 	}
 	
 	public boolean isCornerEmpty(GameState gameState, Square square)
 	{
 		int row = square.getRow();
 		int col = square.getCol();
 		
 		if(row == 0)
 		{
 			if(col == 1)
 			{
 				return IsSquareEmpty(gameState, 0, 0);
 			}
 			else if(col == 6)
 			{
 				return IsSquareEmpty(gameState, 0, 7);
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else if(row == 1)
 		{
 			if(col == 0 || col == 1)
 			{
 				return IsSquareEmpty(gameState, 0, 0);
 			}
 			else if(col == 6 || col == 7)
 			{
 				return IsSquareEmpty(gameState, 0, 7);
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else if(row == 6)
 		{
 			if(col == 0 || col == 1)
 			{
 				return IsSquareEmpty(gameState, 7, 0);
 			}
 			else if(col == 6 || col == 7)
 			{
 				return IsSquareEmpty(gameState, 7, 7);
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else if(row == 7)
 		{
 			if(col == 1)
 			{
 				return IsSquareEmpty(gameState, 7, 0);
 			}
 			else if(col == 6)
 			{
 				return IsSquareEmpty(gameState, 7, 7);
 			}
 			else
 			{
 				return false;
 			}
 		}
 		else 
 		{
 			return false;
 		}
 	}
 
 	public boolean isNextToCornerPenalty(GameState gameState, Square square) {
 		return isNextToCorner(square) && isCornerEmpty(gameState, square);
 	}
 
 	public int getFrontiers(GameState gameState) {
 		int fontierCount = 0;
 		
 		for(int row = 0; row < 8; row++) {
 			for(int col = 0; col < 8; col++) {
 				if(gameState.getSquare(row, col) == gameState.getCurrentPlayer()) {
 					if(isFrontier(gameState, row, col))
 						fontierCount++;
 				}
 			}
 		}
 			
 		return fontierCount;
 	}
 
 	public boolean isFrontier(GameState gameState, int row, int col) {
 		if(isUpperEmpty(gameState, row, col) || isRightEmpty(gameState, row, col) || isLeftEmpty(gameState, row, col) || isLowerEmpty(gameState, row, col))
 			return true;
 		else
 			return false;
 	}
 
 	public boolean isUpperEmpty(GameState gameState, int row, int col) {
 		if(row == 0)
 			return false;
 		else
 		{
 			return gameState.getSquare(row-1, col) == Player.EMPTY;
 		}
 	}
 
 	public boolean isRightEmpty(GameState gameState, int row, int col) {
 		if(col == 7)
 			return false;
 		else
 		{
 			return gameState.getSquare(row, col+1) == Player.EMPTY;
 		}
 	}
 
 	public boolean isLeftEmpty(GameState gameState, int row, int col) {
 		if(col == 0)
 			return false;
 		else
 		{
 			return gameState.getSquare(row, col-1) == Player.EMPTY;
 		}
 	}
 
 	public boolean isLowerEmpty(GameState gameState, int row, int col) {
 		if(row == 7)
 			return false;
 		else
 		{
 			return gameState.getSquare(row+1, col) == Player.EMPTY;
 		}
 	}
 }
