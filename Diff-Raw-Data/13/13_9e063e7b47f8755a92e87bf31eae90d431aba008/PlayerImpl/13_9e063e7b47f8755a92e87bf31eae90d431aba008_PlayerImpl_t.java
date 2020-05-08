 import java.util.Random;
 
 public class PlayerImpl extends Player {
 	private Color color;
 	private Position bestSoFar;
 
 	@Override
 	public void color(Color color) {
 		this.color = color;
 	}
 
 	@Override
 	public void move(Color[][] board) {
 		// Delete everything in this method and include your implementation
 		// here.
 		double highestAlpha = Double.NEGATIVE_INFINITY;
 		double lowestBeta = Double.POSITIVE_INFINITY;
 		for (int i = 0;; i++) {
 			int depth = i;
 			Position[] positions = getLegalMoves(board, color);
 			for (int j = 0; j < positions.length; j++) {
 				GameState gs = makeMove(board, positions[j], color);
                visited(gs.getBoard());
 				if (color.equals(color.WHITE)) {
 					System.out.println("Enter white");
 					MaxValue(gs, highestAlpha, lowestBeta, depth);
 					if (highestAlpha < getScore(gs.getBoard())) {
 						System.out.println("Enter score updated white;");
 						highestAlpha = getScore(gs.getBoard());
 						bestSoFar = positions[i];
 					}
 				} else if (color.equals(color.BLACK)) {
 					System.out.println("Enter black");
 					MinValue(gs, highestAlpha, lowestBeta, depth);
 					System.out.println("lowest beta is " + lowestBeta);
 					if (lowestBeta > getScore(gs.getBoard())) {
 						System.out.println("Enter score updated black;");
 						lowestBeta = getScore(gs.getBoard());
 						bestSoFar = positions[i];
 					}
 				}
 			}
 		}
 	}
 
 	public double MaxValue(GameState gs, double alpha, double beta, int depth) {
 		if (gs.getMove() == Move.GAME_OVER || depth == 0) {
 			return getScore(gs.getBoard());
 		} else {
 			Color c = null;
 			if (gs.getMove() == Move.BLACK) {
 				c = c.BLACK;
 			} else {
 				c = c.WHITE;
 			}
 
 			Position[] positions = getLegalMoves(gs.getBoard(), c);
 			Position bestPosition = null;
 			for (int i = 0; i < positions.length; i++) {
 				GameState gamestate = makeMove(gs.getBoard(), positions[i], c);
                visited(gs.getBoard());
 				if (gs.getMove() != gamestate.getMove()) {
 					alpha = Math.max(alpha,
 							MinValue(gamestate, alpha, beta, depth--));
 				} else {
 					alpha = Math.max(alpha,
 							MaxValue(gamestate, alpha, beta, depth));
 				}
 			}
 			if (alpha >= beta) {
 				return beta;
 			}
 		}
 		return alpha;
 	}
 
 	public double MinValue(GameState gs, double alpha, double beta, int depth) {
 		if (gs.getMove() == Move.GAME_OVER || depth == 0) {
 			return getScore(gs.getBoard());
 		} else {
 			Color c = null;
 			if (gs.getMove() == Move.BLACK) {
 				c = c.BLACK;
 			} else {
 				c = c.WHITE;
 			}
 
 			Position[] positions = getLegalMoves(gs.getBoard(), c);
 			Position bestPosition = null;
 			for (int i = 0; i < positions.length; i++) {
				GameState gamestate = makeMove(gs.getBoard(), positions[i], c);               
                visited(gs.getBoard());
 
 				if (gs.getMove() != gamestate.getMove()) {
					beta = Math.min(alpha,
 							MaxValue(gamestate, alpha, beta, depth--));
 				} else {
					beta = Math.min(alpha,
 							MinValue(gamestate, alpha, beta, depth));
 				}
 			}
 			if (alpha >= beta) {
 				return alpha;
 			}
 		}
 		return beta;
 	}
 
 	@Override
 	public Position bestSoFar() {
 		return bestSoFar;
 	}
 }
