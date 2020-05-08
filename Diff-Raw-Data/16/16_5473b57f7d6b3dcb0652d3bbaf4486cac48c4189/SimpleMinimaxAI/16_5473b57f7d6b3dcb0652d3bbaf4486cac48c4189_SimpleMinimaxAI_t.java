 package othello.ai;
 import java.awt.Point;
 
 import othello.model.Board;
 
 // very simple AI - return the board reflecting the move that captures the most pieces
 
 public class SimpleMinimaxAI implements ReversiAI
 {
   private int size;
   private int maxDepth;
   private Point bestMove;
   
   public SimpleMinimaxAI(int depth) { maxDepth = depth; }
   public void setSize(int boardsize) { size = boardsize; }
   
   private int minMove(Board prev, int depth)
   {
   	if(depth > maxDepth) return prev.getScore(); // exceeded maximum depth
   	
   	int minScore = MAX_SCORE;
   	Board b = new Board(prev);
   	b.turn(); // min player's turn
   	
   	for(int j = 0; j < size; j++)
   	{
   		for(int i = 0; i < size; i++)
   		{
   			if(b.move(i, j))
   			{
   				b.turn(); // max player's turn
   				int score = maxMove(b, depth + 1);
   				
   				if(score < minScore) minScore = score;
   				
   				b = new Board(prev);
   				b.turn();
   			}
   		}
   	}
   	
   	if(minScore == MAX_SCORE) // min player can't make a move
   	{
   		b.turn();
   		if(b.canMove()) return maxMove(b, depth + 1); // max player can make a move
   		else return prev.getScore(); // max player can't make a move either - game over
   	}
   	
 		return minScore;
   }
   
   private int maxMove(Board prev, int depth)
   {
   	if(depth > maxDepth) return prev.getScore(); // exceeded maximum depth
   	
   	int maxScore = MIN_SCORE;
   	Board b = new Board(prev);
   	
   	for(int j = 0; j < size; j++)
   	{
   		for(int i = 0; i < size; i++)
   		{
   			if(b.move(i, j)) // try move
   			{
   				int score = minMove(b, depth + 1);
   				
   				if(score > maxScore)	maxScore = score;
   				
   				b = new Board(prev);
   			}
   		}
   	}
   	
   	if(maxScore == MIN_SCORE) // no moves found
   	{
   		b.turn();
   		if(b.canMove()) { b.turn(); return minMove(b, depth + 1); }
   		else return prev.getScore();
   	}
   	
 		return maxScore;
   }
 
   public Board nextMove(Board prev, int lastx, int lasty)
   {
  	int maxScore = MIN_SCORE;
   	Board best = null, b = new Board(prev);
   	bestMove = null;
   	
   	for(int j = 0; j < size; j++)
   	{
   		for(int i = 0; i < size; i++)
   		{
   			if(b.move(i, j))
   			{
   				int score = minMove(b, 1);
   				
   				if(score > maxScore)
   				{
   					bestMove = new Point(i,j);
   					maxScore = score;
   					best = b;
   				}
   				b = new Board(prev);
   			}
   		}
   	}
   	
   	return best;
   }
   
 	public Point getMove() { return bestMove; }
 }
