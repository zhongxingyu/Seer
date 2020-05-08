 
 public class GameLogic implements IGameLogic {
     private int n = 0;
     private int m = 0;
     private int playerID;
     private GameState state;
     
     public GameLogic() {
         //TODO Write your implementation for this method
     }
 	
     public void initializeGame(int n, int m, int playerID) {
         this.n = n;
         this.m = m;
         this.playerID = playerID;
         this.state = new GameState(n, m);
     }
 	
     public Winner gameFinished() 
     {
         return stateGameFinished(this.state);
     }
     
     private Winner stateGameFinished(GameState state)
     {
     	// Did anyone win? (3)
     	int[] move = state.getLastMove();
 		int x = move[0];
 		int y = move[1];
 		int p = state.ps(x, y);
 		if (p > 0) {
 			if (exploreField(x, y, 4, state)) return GameState.winnerFromInt(p);
 		}
 		
 		// Is it a tie?
     	boolean tie = true;
     	for(int t = 0; t < n; t++) {
     		tie = tie && !(state.coinsInColumn(t) < m);
     	}
     	if(tie) return Winner.TIE;
     	
         return Winner.NOT_FINISHED;
     }
     
     private boolean exploreField(int x, int y, int max, GameState state) 
     {    	
     	if (1 + explore(x, y, -1, -1, 0, state) + explore(x, y, 1, 1, 0, state) >= max ||
     		1 + explore(x, y, 0, -1, 0, state) >= max ||
     		1 + explore(x, y, -1, 0, 0, state) + explore (x, y, 1, 0, 0, state) >= max ||
     		1 + explore(x, y, -1, 1, 0, state) + explore(x, y, 1, -1, 0, state) >= max) return true;
     	
     	return false;
     }
     
     private int explore(int x, int y, int dx, int dy, int level, GameState state)
     {
     	if (state.ps(x + dx, y + dy) == GameState.opponent(state.getCurrentPlayer())) return explore(x+dx, y+dy, dx, dy, level+1, state);
     	else return level;
     }
 
     public void insertCoin(int column, int playerID) {
     	state.madeMove(column);
     }
 
     public int decideNextMove() {
         // Put coin in left-most column, that isn't full already.
     	/*for(int i = 0; i < n; i++) {
     		if(state.coinsInColumn(i) < m) {
     			return i;
     		}
     	}
     	return -1;*/
     	return minmaxDecision(this.state);
     }
     
     private int minmaxDecision(GameState state)
     {
     	// Loop through potential actions (columns)
     	int resultUtil = Integer.MIN_VALUE;
     	int resultAction = -1;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
     			int val = minValue(state.result(i));
     			System.out.println("Column " + i + " has utility " + val);
     			if(resultUtil < val)
 				{
     				resultAction = i;
     				resultUtil = val;
 				}
     		}
     	}
     	return resultAction;
     }
     
     private int minValue(GameState state)
     {
     	Winner winner = stateGameFinished(state);
     	if(winner != Winner.NOT_FINISHED)
     	{
     		return utility(winner);
     	}
     	
     	int result = Integer.MAX_VALUE;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
 	    		int val = maxValue(state.result(i));
 				if(result > val) result = val;
     		}
     	}
     	
     	return result;
     }
     
     private int maxValue(GameState state)
     {
     	Winner winner = stateGameFinished(state);
     	if(winner != Winner.NOT_FINISHED)
     	{
     		return utility(winner);
     	}
     	
     	int result = Integer.MIN_VALUE;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
         		int val = minValue(state.result(i));
     			if(result < val) result = val;
     		}
     	}
     	
     	return result;
     }
     
     private int utility(Winner winner)
     {
     	if(winner == Winner.PLAYER1 && this.playerID == 1) return 1;
     	else if(winner == Winner.PLAYER2 && this.playerID == 2) return 1;
     	return -1;
     }
     
 }
