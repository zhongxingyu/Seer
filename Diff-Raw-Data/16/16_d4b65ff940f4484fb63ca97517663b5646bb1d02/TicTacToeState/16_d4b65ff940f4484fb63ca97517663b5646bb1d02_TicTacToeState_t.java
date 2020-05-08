 package ticTacToe;
 import gamePlayer.Action;
 import gamePlayer.State;
 
import java.util.LinkedList;
import java.util.List;

 /**
  * This is a State of the game Tic Tac Toe.
  * @author Ashoat Tevosyan
  * @since Mon April 18 2011
  * @version CSE 473
  */
 public class TicTacToeState implements State {
 	
 	// Whose turn is it?
 	private boolean turn;
 	// The dimension of the board.
 	private int size;
 	
 	/**
 	 * An two-dimensional array representing the board.
 	 * Each byte has one of three possible values:
 	 * 1, meaning that the first player has moved here.
 	 * -1, meaning that the second player has moved here.
 	 * 0, meaning that no player has yet moved here.
 	 */
 	private byte[][] board;
 	
 	TicTacToeState parent;
 	
 	/**
 	 * Instantiate this TicTacToeState.
 	 * @param size The dimension of the board.
 	 */
 	public TicTacToeState(int size) {
 		this.turn = true;
 		this.size = size;
 		this.board = new byte[size][size];
 	}
 
 	/** {@inheritDoc} */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	@Override
 	public List<Action> getActions() {		
 		LinkedList<Action> actions = new LinkedList<Action>();
 		// Add all Actions blindly
 		for (int i = 0; i < this.size; i++) {
 			for (int j = 0; j < this.size; j++) {
 				Action cast = new TicTacToeAction(this.turn, i, j);
 				actions.add(cast);
 			}
 		}
 		// Remove as necessary
 		Action action = actions.poll();
 		for (int i = 0; i < this.size * this.size; i++) {
 			if (action.validOn(this)) actions.add(action);
 			action = actions.poll();
 		}
 		if (action != null) actions.add(action);
 		// Return all the valid actions
 		return actions;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public float heuristic() {
 		Status status = this.getStatus();
 		if (status == Status.Draw) return 0;
 		else if (status == Status.PlayerOneWon) return Float.POSITIVE_INFINITY;
 		else if (status == Status.PlayerTwoWon) return Float.NEGATIVE_INFINITY;
 		float heuristic = 0;
 		for (int i = 0; i < this.size; i++) {
 			for (int j = 0; j < this.size; j++) {
 				if (this.board[i][j] == 0) continue;
 				int points = 2;
 				// On either diagonals?
 				if ((this.size * i + j) % (this.size - 1) == 0 &&
 						(this.size * i + j) != 0
 						&& (this.size * i + j) != this.size * this.size - 1)
 					points++;
 				if ((this.size * i + j) % (this.size + 1) == 0) points++;
 				heuristic += this.board[i][j] * points; 
 			}
 		}
 		return heuristic;
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public Status getStatus() {
 		// Definitions
 		boolean full = true;
 		byte[] verticals = new byte[this.size];
 		byte[] diagonals = new byte[2];
 		// Get values
 		for (int i = 0; i < this.size; i++) {
 			byte horizontal = 0;
 			for (int j = 0; j < this.size; j++) {
 				// Horizontal/vertical win
 				horizontal += this.board[i][j]; 
 				verticals[j] += this.board[i][j];
 				// Diagonal win
 				if ((this.size * i + j) % (this.size - 1) == 0 &&
 					(this.size * i + j) != 0
 					&& (this.size * i + j) != this.size * this.size - 1)
 					diagonals[0] += this.board[i][j];
 				if ((this.size * i + j) % (this.size + 1) == 0) diagonals[1] += this.board[i][j];
 				// Fullness
 				if (this.board[i][j] == 0) full = false;
 			}
 			// Horizontal winner?
 			if (horizontal == this.size) return Status.PlayerOneWon;
 			else if (horizontal == -1 * this.size) return Status.PlayerTwoWon;
 		}
 		// Vertical winner?
 		for (int i = 0; i < this.size; i++) {
 			if (verticals[i] == this.size) return Status.PlayerOneWon;
 			else if (verticals[i] == -1 * this.size) return Status.PlayerTwoWon;
 		}
 		// Diagonal winner?
 		if (diagonals[0] == this.size || diagonals[1] == this.size) return Status.PlayerOneWon;  
 		else if (diagonals[0] == -1 * this.size || diagonals[1] == -1 * this.size) return Status.PlayerTwoWon;
 		// Full?
 		if (full) return Status.Draw;
 		// Not done!
 		return Status.Ongoing;
 	}
 	
 	/**
 	 * Check the byte value at a particular spot on the board.
 	 * TicTacToeAction uses this to check if it's valid on a State.
 	 * @param i The x-coordinate of a spot on the board.
 	 * @param j The y-coordinate of a spot on the board.
 	 * @return A byte that represents a spot on the board.
 	 * @see this.board
 	 */
 	public byte getValue(int i, int j) {
 		return this.board[i][j];
 	}
 	
 	/**
 	 * "Execute" a move on this State.
 	 * Will effectively mutate this State by setting a value at a spot and changing whose turn it is.
 	 * @param value The value to set a point to.
 	 * @param i The x-coordinate of a spot on the board.
 	 * @param j The y-coordinate of a spot on the board.
 	 * @see this.board
 	 */
 	public void executeMove(byte value, int i, int j) {
 		this.board[i][j] = value;
 		this.turn = !this.turn;
 	}
 	
 	/**
 	 * We want to override since Actions might want to copy us to make a transition State.
 	 * @return A deep copy of this State.
 	 */
 	@Override
 	public Object clone() {
 		TicTacToeState state = new TicTacToeState(this.size);
 		state.turn = this.turn;
 		state.board = new byte[this.size][this.size];
 		for (int i = 0; i < this.size; i++) {
 			for (int j = 0; j < this.size; j++) {
 				state.board[i][j] = this.board[i][j];
 			}
 		}
 		return state;
 	}
 	
 	/**
 	 * Necessary for checking duplicate States.
 	 * @param object An object to compare to this one.
 	 * @return True if the parameter is a State of the same type that is deeply equivalent to us.
 	 */
 	@Override
 	public boolean equals(Object object) {
 		if (object != null && this.getClass() == object.getClass()) {
 			TicTacToeState state = (TicTacToeState)object;
 			return this.hashCode() == state.hashCode();
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * We want to hash identical States into the same bucket.
 	 * This way, we can use a HashSet/Map to avoid duplicate States in searches.
 	 * @return A hash code for this State. 
 	 */
 	@Override
 	public int hashCode() {
 		int code = 0;
 		for (int i = 0; i < this.size; i++) {
 			for (int j = 0; j < this.size; j++) {
 				int additive = this.board[i][j] + 1;
 				for (int k = 0; k < i * this.size + j; k++) additive *= 3;
 				code += additive;
 			}
 		}
 		return code;
 	}
 	
 	/**
 	 * We want to be able to compare States accurately.
 	 * This way, we can use a TreeSet/Map to avoid duplicate States in searched.
 	 * @param state A State to compare ourselves to.
 	 * @return A positive integer if we are greater than the parameter; negative if we are lesser; zero otherwise. 
 	 */
 	@Override
 	public int compareTo(State state) {
 		float heuristic = this.heuristic() - state.heuristic();
 		return heuristic >= 0 ? (int)Math.ceil(heuristic) : (int)Math.floor(heuristic); 
 	}
 	
 	/** {@inheritDoc} */
 	@Override
 	public String toString() {
 		StringBuilder builder = new StringBuilder();
 		for (byte[] array : this.board) {
 			boolean first = true; 
 			for (byte item : array) {
 				if (!first) builder.append("|");
 				else first = false;
 				if (item < 0) builder.append(" O ");
 				else if (item > 0) builder.append(" X ");
 				else builder.append("   ");
 			}
 			builder.append("\n");
 		}
 		return builder.toString();
 	}
 	
 	@Override
 	public State getParentState() {
 		return parent;
 	}
 
 	@Override
 	public float heuristic2() {
 		return heuristic();
 	}

	@Override
	public String identifier() {
		// TODO Auto-generated method stub
		return null;
	}
 	
 }
