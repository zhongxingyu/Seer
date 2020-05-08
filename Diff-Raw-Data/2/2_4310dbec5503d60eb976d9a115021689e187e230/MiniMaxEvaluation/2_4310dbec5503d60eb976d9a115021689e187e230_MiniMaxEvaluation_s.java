 package brown.games.algos;
 
 import java.util.Collection;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
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
 
 	private static final Logger log = LoggerFactory.getLogger(MiniMaxEvaluation.class);
 
 	private Player original;
 
 	private int ply;
 
 	public MiniMaxEvaluation(int ply) {
 		this.ply = ply;
 	}
 
 	@Override
 	public GameMove bestMove(GameState s, Player player, Player opponent) {
 
 		if (log.isInfoEnabled())
 			log.info("bestMove: calculating best move for player {} state {}", player, s);
 
 		this.original = player;
 		MoveEvaluation best = minimax(s.copy(), ply, player, opponent);
 
 		if (log.isInfoEnabled()) log.info("bestMove: the best possible move is [{}]", best);
 
 		GameMove bestMove = best.move;
 
 		if (best.move != null) {
 			return bestMove;
 		}
 		// we reached a point where all moves lead to a loss - if any moves
 		// at all exist, just return an arbitrary move
 		Collection<GameMove> moves = player.validMoves(s);
 		return moves.isEmpty() ? null : moves.iterator().next();
 	}
 
 	private MoveEvaluation minimax(GameState s, int p, Player player, Player opponent) {
 
 		if (log.isDebugEnabled())
 			log.debug("minimax: evaluating player {} opponent {} at ply {} state {}", new Object[]{
 					player, opponent, p, s });
 
 		Collection<GameMove> moves = player.validMoves(s);
 
 		// if no more moves or we've reached the end of the analysis, return the
 		// state score
 		if (p == 0 || moves.isEmpty()) {
 
			final int score = original.eval(s);
 			final MoveEvaluation e = new MoveEvaluation(score);
 			if (log.isDebugEnabled()) log.debug("minimax: at end of tree, returning [{}]", e);
 			return e;
 		}
 
 		final boolean isPlayer = player == original;
 
 		// set a lower bound for best, which we will try to improve on
 		MoveEvaluation best = new MoveEvaluation(isPlayer ? Integer.MIN_VALUE : Integer.MAX_VALUE);
 
 		for (GameMove move : moves) {
 			move.execute(s);
 
 			MoveEvaluation me = minimax(s, p - 1, opponent, player);
 
 			move.undo(s);
 
 			// if we improved, keep track of new score and move
 			if (isPlayer) {
 				if (me.score > best.score) {
 					if (log.isDebugEnabled())
 						log.debug("minimax: isPlayer and move {} is better than previous best {}",
 							me, best);
 					best = new MoveEvaluation(move, me.score);
 				}
 			}
 			else {
 				if (me.score < best.score) {
 					if (log.isDebugEnabled())
 						log.debug("minimax: isOpponent and move {} is worse than previous best {}",
 							me, best);
 					best = new MoveEvaluation(move, me.score);
 				}
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
 
 		@Override
 		public String toString() {
 			return super.toString() + "[move=" + move + ",score=" + score + "]";
 		}
 	}
 }
