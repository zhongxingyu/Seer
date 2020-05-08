 import java.util.Vector;
 
 public class GameState {
 	//TODO Create game state board. Array with strings.
 	//Functions for isLegalMove/isMovable(C.box) etc. PossibleGameStates function
 	public char[][] state;
 	private Position positionNow;
 	private boolean lastMovePushMove;
 
 	public GameState(Vector<String> board){
 		//Check longest column size
 		int col = 0;
 		for (int i = 0; i < board.size(); i++) {
 			if(col< board.get(i).length()){
 				col = board.get(i).length();
 			}
 		}
 		//create the board
 		state = new char[board.size()][];
 		for (int i = 0; i < board.size(); i++) {
 			state[i] = board.get(i).toCharArray();
 		}
 		for (int i = 0; i < state.length; i++) {
 			for (int j = 0; j < state[0].length; j++) {
 				if(state[i][j] == C.player ||state[i][j] == C.playerOnGoal){
 					positionNow = new Position(i, j);
 				}
 			}
 		}
 	}
 	public GameState(char[][] firstState){
 		state = firstState;
 
 		for (int i = 0; i < firstState.length; i++) {
 			for (int j = 0; j < firstState[0].length; j++) {
 				if(firstState[i][j] == C.player ||firstState[i][j] == C.playerOnGoal){
 					positionNow = new Position(i, j);
 				}
 			}
 		}
 	}
 	
 	public char[][] getGameBoard()
 	{
 		return state; 
 	}
 	public Position getPositionNow()
 	{
 		return positionNow;
 	}
 
 	public char getCharAt(int row, int col){
 		return state[row][col];
 	}
 	public int getRows(){
 		return state.length;
 	}
 	public int getColumns(){
 		return state[0].length;
 	}
 
 	private boolean canPush(int x, int y) //x and y must be either (-1, 0, +1)
 	{
 		int py = positionNow.row;
 		int px = positionNow.column;
 		//check the cell that are going to be pushed
 		if(py+y< getRows() && px+x < getColumns()){
 			char c = state[py+y][px+x];
 			if(c == C.wall) {
 				return false;
 			}
 			else if (c == C.empty|| c == C.goal){
 				return true;
 			}
 		} else {
 			return false;
 		}			
 
 		//check the cell behind the pushed object
 		if(py+y*2< state.length && px+x*2 < state[0].length){
 			char c = state[py+y*2][px+x*2];
 			if(c == C.wall || c == C.box || c == C.boxOnGoal){
 				return false;
 			}
 			else if(c==C.empty|| c ==C.goal){
 				return true;
 			}			
 		}
 		
 		return false;
 	}
 
 	public boolean canPushUp(){
 		return canPush(0, -1);
 	}
 	public boolean canPushDown(){
 		return canPush(0, +1);
 	}
 	public boolean canPushLeft(){
 		return canPush(-1, 0);
 	}
 	public boolean canPushRight(){
 		return canPush(+1, 0);
 	}
 
 	private GameState push(int x, int y) //x and y must be either (-1, 0, +1)
 	{
 		int px = positionNow.column;
 		int py = positionNow.row;
 //		System.err.println("Pushing to row = " + (py + y) + " and col " + (px+x));
 		char[][] childState = copyMatrix(state);
 
 		//update the cell on the players current postion
 		{
 			char c = childState[py][px];
 			if(c == C.player) 
 				childState[py][px] = C.empty;
 			else if(c == C.playerOnGoal ) 
 				childState[py][px] = C.goal;
 		}
 
 		//update the cell where the pushed object goes, if there is a pushed object
 		{
 			char c = childState[py+y][px+x];
 			if(c == C.box || c == C.boxOnGoal){
 				lastMovePushMove = true;
 				char cc = childState[py+y*2][px+x*2];
 				if(cc == C.empty){
 					childState[py+y*2][px+x*2] = C.box;
 					
 				}
 				else if(cc == C.goal){
 					childState[py+y*2][px+x*2] = C.boxOnGoal;
 					
 				}
 				if(c == C.boxOnGoal){
 					childState[py+y][px+x] = C.goal;
 				} else if(c== C.box){
 					childState[py+y][px+x] = C.empty;
 				}else{
 					System.err.println("Wrong in push1, gamestate");
 				}
 				//else throw exception invalid push
 			}
 			else{
 				lastMovePushMove = false;
 			}
 		}
 
 		//update the pushed cell
 		{
 			char c = childState[py+y][px+x];
 			if(c == C.goal) {
 				childState[py+y][px+x] = C.playerOnGoal;
 			}
 			else if (c== C.empty){
 				childState[py+y][px+x] = C.player;
 			}
 			else{
 				System.err.println("Wrong in push2, gamestate");
 			}
 		}
 		GameState newState = new GameState(childState);
 		newState.positionNow.row = py + y;
 		newState.positionNow.column = px + x;
 		return newState;
 	}
 
 	public GameState pushUp(){
 		return push(0,-1);
 	}
 	public GameState pushDown(){
 		return push(0,1);
 	}
 	public GameState pushLeft(){
 		return push(-1,0);
 	}
 	public GameState pushRight(){
 		return push(1,0);
 	}
 
 	public boolean isWinning(){
 		for(int iy=0; iy<state.length; iy++)
 		{
 			for(int ix=0; ix<state[iy].length; ix++)
 			{
 				if(state[iy][ix] == C.box) return false;
 			}
 		}
 		return true;
 	}
 
 	public static char[][] copyMatrix(char[][] array){
 		char[][] copy = new char[array.length][];
 		for (int i = 0; i < array.length; i++) {
 			copy[i] = array[i].clone();
 		}
 		return copy;
 	}
 	public boolean isLastMovePush(){
 		return lastMovePushMove;
 	}
 	@Override
 	public String toString()
 	{
		String ret = "";
		for(int i = 0 ; i < state.length; i++)
 		{
			for (int j = 0 ; j < state[0].length; j++)
 			{
				ret += state[i][j];
 			}
			ret += "\n";
 		}
		return ret;
 	}
 }
