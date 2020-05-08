 package org.weiqi;
 
 import java.util.HashSet;
 
 import org.weiqi.Board.MoveType;
 import org.weiqi.Weiqi.Occupation;
 
 public class GameSet {
 
 	private final Board board;
 	private Occupation nextPlayer;
 	private HashSet<Integer> previousStates = new HashSet<>();
 
 	public GameSet() {
 		this(new Board(), Occupation.BLACK);
 	}
 
 	@SuppressWarnings("unchecked")
 	public GameSet(GameSet gameSet) {
 		this(gameSet.board, gameSet.nextPlayer,
 				(HashSet<Integer>) gameSet.previousStates.clone());
 	}
 
 	/**
 	 * Constructs a "left-over" game. Note that the previous state information
 	 * will be empty, not suitable for real-play scenario.
 	 */
 	public GameSet(Board board, Occupation nextPlayer) {
 		this(board, nextPlayer, new HashSet<Integer>());
 		previousStates.add(board.hashCode());
 	}
 
 	private GameSet(Board board, Occupation nextPlayer,
 			HashSet<Integer> previousStates) {
 		this.board = new Board(board);
 		this.nextPlayer = nextPlayer;
 		this.previousStates = previousStates;
 	}
 
 	/**
 	 * Move that can be played or un-played.
 	 */
 	public static class Move {
 		public Coordinate position;
 		public MoveType type;
 		public Occupation neighborColors[] = new Occupation[4];
 
 		public Move() {
 		}
 
 		public Move(Coordinate position) {
 			this.position = position;
 		}
 	}
 
 	public Move play(Coordinate c) {
 		Move move = new Move(c);
 		play(move);
 		return move;
 	}
 
 	private void play(Move move) {
 		if (!playIfValid(move))
 			throw new RuntimeException("Invalid move " + move.position
 					+ " for " + nextPlayer + "\n" + this);
 	}
 
	public void undo(Move move) {
		nextPlayer = nextPlayer.opponent();
		unplay(move);
	}

 	public boolean isValidMove(Move move) {
 		return playIfValid(move, true);
 	}
 
 	public boolean playIfValid(Move move) {
 		return playIfValid(move, false);
 	}
 
 	/**
 	 * Plays a move on the Weiqi board. Ensure no repeats in game state history.
 	 */
 	private boolean playIfValid(Move move, boolean rollBack) {
 		Occupation opponent = nextPlayer.opponent();
 		int i = 0;
 
 		for (Coordinate c1 : move.position.neighbors())
 			move.neighborColors[i++] = board.get(c1);
 
 		move.type = board.playIfSeemsPossible(move.position, nextPlayer);
 		boolean success = move.type != MoveType.INVALID;
 
 		if (success) {
 			int newHashCode = board.hashCode();
 			success &= !previousStates.contains(newHashCode);
 
 			if (success && !rollBack) {
 				nextPlayer = opponent;
 				previousStates.add(newHashCode);
 			} else
 				unplay(move);
 		}
 
 		return success;
 	}
 
 	public void pass() {
 		nextPlayer = nextPlayer.opponent();
 	}
 
 	/**
 	 * Roll back board status; rejuvenate the pieces being eaten.
 	 */
 	public void unplay(Move move) {
 		Occupation opponent = nextPlayer.opponent();
 		previousStates.remove(board.hashCode());
 
 		if (move.type == MoveType.CAPTURE) {
 			int i = 0;
 
 			for (Coordinate c1 : move.position.neighbors())
 				if (move.neighborColors[i++] != board.get(c1))
 					for (Coordinate c2 : board.findGroup(c1))
 						board.set(c2, opponent);
 		}
 
 		board.set(move.position, Occupation.EMPTY);
 	}
 
 	@Override
 	public int hashCode() {
 		return board.hashCode() //
 				^ nextPlayer.hashCode() //
 				^ previousStates.hashCode();
 	}
 
 	@Override
 	public boolean equals(Object object) {
 		if (object instanceof GameSet) {
 			GameSet other = (GameSet) object;
 			return board.equals(other.board) //
 					&& nextPlayer == other.nextPlayer //
 					&& previousStates.equals(other.previousStates);
 
 		} else
 			return false;
 	}
 
 	public Board getBoard() {
 		return board;
 	}
 
 	public Occupation getNextPlayer() {
 		return nextPlayer;
 	}
 
 }
