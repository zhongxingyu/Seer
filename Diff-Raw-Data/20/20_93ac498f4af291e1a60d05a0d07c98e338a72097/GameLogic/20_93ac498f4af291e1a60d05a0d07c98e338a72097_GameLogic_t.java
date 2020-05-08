 import java.util.Calendar;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.concurrent.TimeoutException;
 
 
 public class GameLogic implements IGameLogic {
     private int n = 0;
     private int m = 0;
     private int playerID;
     private GameState state;
     
     private int cutoff;
     private Date deadline;
     private int timeLimit = 5; // Seconds
     
     private boolean debugPrint = true;
     
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
         int result = 0;
         cutoff = 1;
     	
     	// Calculate deadline
     	Calendar cal = new GregorianCalendar();
     	cal.add(Calendar.SECOND, timeLimit);
     	deadline = cal.getTime();
     	
     	// Start search with increasing cutoff
    	System.out.format("Starting search with %d seconds timelimit...%n", timeLimit);
     	long latestStart = new Date().getTime();
     	while(new Date().before(deadline))
     	{
     		long now = new Date().getTime();
     		long latestElapsed = now - latestStart;
     		long remaining =  cal.getTimeInMillis() - now;
    		System.out.format("Evaluation took %dms (Remaining time: %dms)%n", latestElapsed, remaining);
     		
     		if(remaining > latestElapsed)
     		{
    			if(debugPrint) System.out.format("Searching with cutoff %d%n", cutoff);
     			try
         		{
     				latestStart = now;
         			result = minmaxDecision(this.state);
     	    		cutoff++;
         		}
         		catch(TimeoutException e) 
         		{
        			if(debugPrint) System.out.format("Evaluation with cutoff %d timed out%n", cutoff);
         			cutoff--;
         		}
     		}
     		else
     		{
    			if(debugPrint) System.out.format("Evaluation with cutoff %d not persued because too little time remaining%n", cutoff);
     			break;
     		}
     	}
     	
    	System.out.format("Result from search with cutoff: %d%n", cutoff);
     	return result;
     }
     
     private int minmaxDecision(GameState state) throws TimeoutException
     {
     	// Loop through potential actions (columns)
     	double resultUtil = Double.NEGATIVE_INFINITY;
     	int resultAction = -1;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
     			double val = minValue(state.result(i), Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 0);
    			if(debugPrint) System.out.format("Column %d has utility %f%n", i, val);
     			if(resultUtil < val)
 				{
     				resultAction = i;
     				resultUtil = val;
 				}
     		}
     	}
     	return resultAction;
     }
     
     private double minValue(GameState state, double a, double b, int depth) throws TimeoutException
     {
     	if(new Date().after(deadline)) throw new TimeoutException("Out of time!");
     	
     	Winner winner = stateGameFinished(state);
     	if(winner != Winner.NOT_FINISHED)
     	{
     		return utility(winner);
     	}
     	depth++;
     	
     	double result = Double.POSITIVE_INFINITY;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
     			if (depth > cutoff) return h(state);
 	    		result = Math.min(maxValue(state.result(i), a, b, depth), result);
 				if (result <= a) return result;
 				b = Math.min(b, result);
     		}
     	}
     	
     	return result;
     }
     
     private double maxValue(GameState state, double a, double b, int depth) throws TimeoutException
     {
     	if(new Date().after(deadline)) throw new TimeoutException("Out of time!");
     	
     	Winner winner = stateGameFinished(state);
     	if(winner != Winner.NOT_FINISHED)
     	{
     		return utility(winner);
     	}
     	
     	depth++;
     	
     	double result = Double.NEGATIVE_INFINITY;
     	for(int i = 0; i < n; i++)
     	{
     		if(state.coinsInColumn(i) < m)
     		{
     			if (depth > cutoff) return h(state);
     			result = Math.max(minValue(state.result(i), a, b, depth), result);
         		if (result >= b) return result;
     			a = Math.max(a, result);
     		}
     	}
     	
     	return result;
     }
     
     private double h(GameState state) throws TimeoutException
     {
     	double result = 0;
     	
     	for(int i = 0; i < n; i++)
     	{
     		for(int j = 0; j < m; j++)
     		{
     			if(deadline.before(new Date())) throw new TimeoutException();
     			if(state.ps(i,j) != -1)
 				{
     				result += friendlyNeighbours(i, j, state);
 				}
     		}
     	}
     	
     	return result/1000.0;
     }
     
     private int friendlyNeighbours(int x, int y, GameState state) {
     	int[][] neighbours = new int[][] { {x, y-1}, {x, y+1}, {x-1, y}, {x+1, y}, {x+1,y+1}, {x-1,y-1}, {x+1, y-1}, {x-1, y+1} };
     	int result = 0;
     	int fieldPlayer = state.ps(x, y);
     	int currentPlayer = state.getCurrentPlayer();
     	
     	for(int[] nb : neighbours)
     	{
 			if (neighbourIsFriendly(nb[0], nb[1], fieldPlayer)) {
 				if (fieldPlayer == currentPlayer) result++;
 				else result--;
 			}
     	}
     	return result;
     }
     
     private boolean neighbourIsFriendly(int x, int y, int playerID) {
     	return state.ps(x, y) == playerID;
     }
     
     private int utility(Winner winner)
     {
     	if(winner == Winner.PLAYER1 && this.playerID == 1) return 1;
     	else if(winner == Winner.PLAYER2 && this.playerID == 2) return 1;
     	else if(winner == Winner.TIE) return 0;
     	return -1;
     }
     
 }
