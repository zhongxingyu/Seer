 package reversi.game.controller.input.ai;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import reversi.game.board.ReversiMoveFinder;
 import reversi.game.board.ReversiMoveMaker;
 import reversi.models.ReversiBoard;
 import reversi.models.ReversiPlayer;
 import reversi.models.game.ReversiInput;
 import base.models.Position;
 
 public class MinMaxAi extends ReversiAi {
 
 	public MinMaxAi(Integer difficulty) {
 		super(difficulty);
 	}
 
 	/**
 	 * This AI works by recursively trying all available moves, then comparing
 	 * the amount of damage the AI does from that move versus the amount of
 	 * damage the opponent will do.
 	 * 
 	 * The AI will check the cause/effect of all its moves, and return the
 	 * 
 	 * @param intelligence
 	 * @param board
 	 * @return
 	 */
 	public ReversiInput inputForIntelligence(ReversiPlayer intelligence, ReversiBoard board) {
 
 		ReversiInput input = null;
 		MoveSimulation simulation = new MoveSimulation(intelligence, board, difficulty);
 		MoveResult bestMove = simulation.findBestMove();
 
 		if (bestMove != null) {
 			Position move = bestMove.getMove();
 			input = new ReversiInput(intelligence, move);
 		}
 
 		return input;
 	}
 
 	/**
 	 * Can simulate moves on a given board.
 	 * 
 	 * @author dereekb
 	 */
 	private static class MoveSimulation {
 		private final ReversiPlayer player;
 		private final ReversiBoard board;
 		private final Integer recursiveSimulations;
 
 		private boolean simulateOpponentMoves = true;
 
 		public MoveSimulation(ReversiPlayer player, ReversiBoard board, Integer recursiveSteps) {
 			this.player = player;
 			this.board = board;
 			this.recursiveSimulations = recursiveSteps;
 		}
 
 		public MoveResult findBestMove() {
 			List<MoveResult> results = this.simulateMoves();
 			MoveResult bestMove = null;
 
 			if (results.size() > 0) {
 				Collections.sort(results); // Sort least to greatest.
 				Collections.reverse(results); // Reverse list.
 				bestMove = results.get(0);
 			}
 
 			return bestMove;
 		}
 
 		public List<MoveResult> simulateMoves() {
 			List<MoveResult> results = new ArrayList<MoveResult>();
 
 			ReversiMoveFinder finder = new ReversiMoveFinder(board, player);
 			Set<Position> moves = finder.findMoves();
 
 			// Simulate running moves on a board for each of those moves
 			for (Position move : moves) {
 				MoveResult result = this.simulateMove(move);
 				results.add(result);
 			}
 
 			return results;
 		}
 
 		public MoveResult simulateMove(Position position) {
 
 			MoveResult result = new MoveResult(position);
 
 			ReversiBoard simulationBoard = this.board.cloneBoard();
 
 			// Count number of pieces that are owned by the AI at the end of
 			// that move.
 			Integer initialPiecesCount = simulationBoard.countElementsOwnedByPlayer(player);
 			ReversiMoveMaker moveMaker = new ReversiMoveMaker(simulationBoard, player);
 			moveMaker.playAtPosition(position);
 
 			Integer postMovePiecesCount = simulationBoard.countElementsOwnedByPlayer(player);
 			Integer gain = postMovePiecesCount - initialPiecesCount;
 			result.setPiecesCaptured(gain);
 
 			if (this.simulateOpponentMoves) {
 
 				/*
 				 * Simulate opponents turn for each of their available moves.
 				 * 
 				 * (Only 1 in Reversi, unless they have no piece after this
 				 * turn!)
 				 */
 				Set<ReversiPlayer> opponents = simulationBoard.getOpponentsOfPlayerOnBoard(player);
 				for (ReversiPlayer opponent : opponents) {
 
 					// Use this simulator, but only run their moves.
 					MoveSimulation opponentSimulation = new MoveSimulation(opponent, simulationBoard, 0);
 					opponentSimulation.setSimulateOpponentMoves(false);
 
 					/*
 					 * Take that best short-term move and apply it to the board.
 					 * 
 					 * Only consider this move here, since we'll check the
 					 * others during recursion. That said, we can easily set
 					 * opponent move simulation on, and recurse a few times.
 					 * 
 					 * Something to test. (Although I think that might also
 					 * cause an infinite loop)
 					 */
 					MoveResult bestMove = opponentSimulation.findBestMove();
 
 					// Make sure they have any move. Null would be returned when
 					// they have none.
 					if (bestMove != null) {
 						Position bestMovePosition = bestMove.getMove();
 
 						// Apply that best move to our board.
 						ReversiMoveMaker opponentMoveMaker = new ReversiMoveMaker(simulationBoard, opponent);
 						opponentMoveMaker.playAtPosition(bestMovePosition);
 					}
 				}
 			}
 
 			/*
 			 * Pieces lost is the amount we ended with minus the amount we
 			 * gained earlier. The AI wants this to be negative.
 			 */
 			Integer postOpponentPlaysPieceCount = simulationBoard.countElementsOwnedByPlayer(player);
 			Integer piecesLost = postOpponentPlaysPieceCount - postMovePiecesCount;
 			result.setPiecesLost(piecesLost);
 
 			/*
 			 * TODO: At this point, further recursion will happen using more
 			 * simulations.
 			 */
 			if(this.recursiveSimulations > 0) {
 				
 				Integer futureSimulationRecursions = this.recursiveSimulations - 1;				
 				MoveSimulation nextStepsSimulation = new MoveSimulation(player, simulationBoard, futureSimulationRecursions);
 				MoveResult futureResults = nextStepsSimulation.findBestMove();
 
				if(futureResults != null){
					Integer predictedPiecesGained = futureResults.getPiecesCaptured() + result.getPiecesCaptured();
					Integer predictedPiecesLost = futureResults.getPiecesLost() + result.getPiecesLost();
					
					result.setPiecesCaptured(predictedPiecesGained);
					result.setPiecesLost(predictedPiecesLost);
				}
 			}
 
 			return result;
 		}
 
 		public boolean isSimulateOpponentMoves() {
 			return simulateOpponentMoves;
 		}
 
 		public void setSimulateOpponentMoves(boolean simulateOpponentMoves) {
 			this.simulateOpponentMoves = simulateOpponentMoves;
 		}
 	}
 
 	/**
 	 * Result of making a move at a given position. Used by the MoveSimulator to
 	 * convey results.
 	 * 
 	 * @author dereekb
 	 */
 	private static class MoveResult implements Comparable<MoveResult> {
 		private final Position move;
 
 		/**
 		 * Pieces that would be captured from making this move.
 		 */
 		private Integer piecesCaptured = 0;
 
 		/**
 		 * Max potential amount of pieces lost from making this move.
 		 */
 		private Integer piecesLost = 0;
 
 		public MoveResult(Position move) {
 			this.move = move;
 		}
 
 		public Integer getPiecesCaptured() {
 			return piecesCaptured;
 		}
 
 		public void setPiecesCaptured(Integer piecesCaptured) {
 			this.piecesCaptured = piecesCaptured;
 		}
 
 		public Integer getPiecesLost() {
 			return piecesLost;
 		}
 
 		public void setPiecesLost(Integer piecesLost) {
 			this.piecesLost = piecesLost;
 		}
 
 		public Position getMove() {
 			return move;
 		}
 
 		// TODO: This is where we can apply a heuristic. A greedy AI, a cautious
 		// AI, and a min/max AI heuristic. Just for fun anyways.
 		@Override
 		public int compareTo(MoveResult move) {
 			Integer moveCaptured = move.getPiecesCaptured();
 			Integer moveLost = move.getPiecesLost();
 
 			Integer capturedCompare = this.piecesCaptured.compareTo(moveCaptured);
 			Integer lostCompare = this.piecesLost.compareTo(moveLost);
 
 			// Less pieces lost is better than move pieces gained.
 			Integer result = 0;
 
 			if (lostCompare == 0) {
 				/*
 				 * Equal amount of loss. The one with the greater pieces
 				 * captured wins.
 				 */
 				result = capturedCompare;
 			} else if (lostCompare == 1) {
 				/*
 				 * More Lost, automatically lesser.
 				 */
 
 				result = 1;
 			} else {
 				/*
 				 * Less Lost, automatically greater.
 				 */
 
 				result = -1;
 			}
 
 			return result;
 		}
 
 		@Override
 		public String toString() {
 			return "MoveResult [move=" + move + ", captured=" + piecesCaptured + ", lost=" + piecesLost + "]";
 		}
 
 	}
 }
