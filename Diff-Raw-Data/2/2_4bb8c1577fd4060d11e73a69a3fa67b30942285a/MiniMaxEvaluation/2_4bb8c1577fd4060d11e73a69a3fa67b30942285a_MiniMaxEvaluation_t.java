 package brown.games.algos;
 
 import java.util.Collection;
 import brown.games.Evaluation;
 import brown.games.GameMove;
 import brown.games.GameState;
 import brown.games.Player;
 
 /**
  * Implementation of MiniMax algorithm.
  * <p>
  * Not threadsafe.
  * 
  * @author Matt Brown msbcode@gmail.com
  * @date Jun 3, 2010
  */
 public class MiniMaxEvaluation implements Evaluation {
 
 	private Player original;
 
 	private GameState state;
 
 	private int ply;
 
 	public MiniMaxEvaluation(int ply) {
 		this.ply = ply;
 	}
 
 	@Override
 	public GameMove bestMove(GameState s, Player player, Player opponent) {
 
 		this.original = player;
 		this.state = s.copy();
 
 		MoveEvaluation best = minimax(state, ply, player, opponent);
 
 		return best.move;
 	}
 
 	private MoveEvaluation minimax(GameState s, int p, Player player, Player opponent) {
 
 		Collection<GameMove> moves = player.validMoves(s);
 
 		if (p == 0 || moves.isEmpty()) {
 			return new MoveEvaluation(original.eval(state));
 		}
 
 		// set a lower bound for best, which we will try to improve on
 		MoveEvaluation best = new MoveEvaluation(player == original ? Integer.MIN_VALUE
 				: Integer.MAX_VALUE);
 
 		for (GameMove move : moves) {
 			move.execute(s);
 
 			MoveEvaluation me = minimax(s, p - 1, opponent, player);
 
 			move.undo(s);
 
 			// if we improved, keep track of new score and move
 			if (player == original) {
 				if (me.score > best.score) best = new MoveEvaluation(move, me.score);
 			}
 			else {
 				if (me.score < best.score) best = new MoveEvaluation(move, me.score);
 			}
 		}
 		return best;
 	}
 
	private static class MoveEvaluation {
 
 		protected final GameMove move;
 
 		protected final int score;
 
 		protected MoveEvaluation(int score) {
 			this(null, score);
 		}
 
 		protected MoveEvaluation(GameMove move, int score) {
 			this.move = move;
 			this.score = score;
 		}
 
 	}
 }
