 package afleveringsopgave4.opgave3.Lauszus;
 
 import java.util.Random;
 
 /**
  * This is the Game of Life class which will keep track of the current state and generate the next state.
  * @author Simon Patrzalek, Mads Bornebusch and Kristian Lauszus.
  */
 public class GameOfLife {
 	private static Random random = new Random();
 	private static int[][] state;	
 	private static int gridSize;	
 	
 	/**
 	 * Constructor used to set a random state.
 	 * @param n Size of the n x n array.
 	 */
 	public GameOfLife(int n) {
 		gridSize = n;
 		state = new int[n][n];
 		for(int y = 0; y < gridSize; y++) {
 			for(int x = 0; x < gridSize; x++)
 				state[y][x] = random.nextInt(2); // Returns a value from 0-1
 		}
 	}
 	
 	/**
 	 * Constructor used to set the initial state.
 	 * @param initialState A n x n array containing the initial state.   
 	 */
 	public GameOfLife(int[][] initialState) {
		gridSize = initialState[0].length; // We will assume that it's square 
 		state = initialState.clone();
 	}
 	
 	/**
 	 * Call this to get the next state in the game.
 	 */
 	public void nextState() {
 		int[][] newState = new int[gridSize][gridSize];
 		for(int y = 0; y < gridSize; y++) {
 			for(int x = 0; x < gridSize; x++) {
 				int liveNeighbours = liveNeighbours(x,y);
 				if(liveNeighbours < 2 || liveNeighbours > 3)
 					newState[y][x] = 0;
 				else if(liveNeighbours == 3)
 					newState[y][x] = 1;
 				else
 					newState[y][x] = state[y][x];
 			}
 		}
 		state = newState.clone();
 	}
 	
 	/**
 	 * Return the state at the specific x,y-coordinate.
 	 * @param x x-coordinate.
 	 * @param y y-coordinate.
 	 * @return The state with y-coordinates starting at the bottom for easy use with StdDraw.
 	 */
 	public int getState(int x, int y) {
 		y = gridSize-1 - y;	
 		return state[y][x];		
 	}
 	
 	/**
 	 * Function used to count how many neighbours a point has.
 	 * @param x-coordinate of the point.
 	 * @param y-coordinate of the point.
 	 * @return Returns the numbers of neighbours.
 	 */
 	public int liveNeighbours(int x, int y) {
 		int liveNeighbours = 0;
 		
 		for(int i = -1; i <= 1; i++) {
 			int corY = y+i;
 			for(int j = -1; j <= 1; j++) {
 				int corX = x+j;
 				if(corX >= 0 && corY >= 0 && corX < gridSize && corY < gridSize && (corX != x || corY != y)) {
 					if(state[corY][corX] == 1)
 						liveNeighbours++;
 				}
 			}
 		}
 		return liveNeighbours;
 	}
 	
 	/**
 	 * Converts the current state into a printable matrix.
 	 */
 	public String toString() {
 		String output = "";
 		for(int y = 0; y < gridSize; y++) {
 			for(int x = 0; x < gridSize; x++) {
 				output += state[y][x] + " ";
 			}
 			output += "\n";
 		}
 		return output;
 	}
 }
