 package com.oakonell.chaotictactoe.model.solver;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import com.oakonell.chaotictactoe.model.Board;
 import com.oakonell.chaotictactoe.model.Cell;
 import com.oakonell.chaotictactoe.model.Marker;
 import com.oakonell.chaotictactoe.model.MarkerChance;
 import com.oakonell.chaotictactoe.model.State;
 
 /**
  * MiniMax solving algorithm with Alpha/Beta pruning
  */
 public class MiniMaxAlg {
 	private final Marker player;
 	private final int depth;
 	private final MarkerChance chance;
 
 	public MiniMaxAlg(Marker player, int depth, MarkerChance chance) {
 		this.player = player;
 		this.chance = chance;
 		if (depth < 0)
 			throw new RuntimeException("Search-tree depth cannot be negative");
 		this.depth = depth;
 	}
 
 	public int getDepth() {
 		return depth;
 	}
 
 	public Cell solve(Board board, Marker toPlay) {
 		Board copy = board.copy();
 		MoveAndScore solve = solve(copy, depth, player, toPlay,
 				Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
 		if (solve.move == null) {
 			throw new RuntimeException("Move should not be null!");
 		}
 		return solve.move;
 	}
 
 	private MoveAndScore solve(Board board, int depth, Marker currentPlayer,
 			Marker toPlay, double theAlpha, double theBeta) {
 		State state = board.getState();
 		if (state.isOver()) {
 			// how can moves be empty is state is not over?!
 			// game is over
 			Marker winner = state.getWinner();
 			if (winner != null) {
 				// someone won, give the heuristic score
 				return new MoveAndScore(null, getHeuristicScore(board));
 			}
 			// game was a draw, score is 0
 			return new MoveAndScore(null, 0);
 		}
 		// reached the search depth
 		if (depth == 0) {
 			return new MoveAndScore(null, getHeuristicScore(board));
 		}
 
 		Cell bestMove = null;
 		double alpha = theAlpha;
 		double beta = theBeta;
 
 		List<MoveAndWeight> moves = getValidMoves(board, currentPlayer, toPlay);
 		for (MoveAndWeight move : moves) {
 			Marker original = null;
 			if (move.marker == Marker.EMPTY) {
 				original = board.getCell(move.move.getX(), move.move.getY());
 				board.removeMarker(move.move, move.marker);
 			} else {
 				board.placeMarker(move.move, move.marker);
 			}
 			double currentScore;
 			if (currentPlayer == player) {
 				currentScore = solve(board, depth - 1,
 						currentPlayer.opponent(), null, alpha, beta).score
 						* move.weight;
 				if (currentScore > alpha) {
 					alpha = currentScore;
 					bestMove = move.move;
 				}
 			} else {
 				currentScore = solve(board, depth - 1,
 						currentPlayer.opponent(), null, alpha, beta).score
 						* move.weight;
 				if (currentScore < beta) {
 					beta = currentScore;
 					bestMove = move.move;
 				}
 			}
 			if (move.marker == Marker.EMPTY) {
 				board.placeMarker(move.move, original);
 			} else {
 				board.clearMarker(move.move, move.marker);
 			}
 			if (alpha >= beta)
 				break;
 		}
 		double bestScore = currentPlayer == player ? alpha : beta;
 		return new MoveAndScore(bestMove, bestScore);
 	}
 
 	private List<MoveAndWeight> getValidMoves(Board board, Marker player,
 			Marker toPlay) {
 		List<MoveAndWeight> result = new ArrayList<MoveAndWeight>();
 		if (toPlay != null) {
 			addMoves(result, board, toPlay, 1);
 			return result;
 		}
 		if (chance.getMyMarker() != 0) {
 			addMoves(result, board, player, chance.getMyMarkerPercentage());
 		}
 		if (chance.getOpponentMarker() != 0) {
 			addMoves(result, board, player.opponent(),
					chance.getOpponentMarkerPercentage());
 		}
 		if (chance.getRemoveMarker() != 0) {
 			addMoves(result, board, Marker.EMPTY,
 					chance.getRemoveMarkerPercentage());
 		}
 		return result;
 	}
 
 	private void addMoves(List<MoveAndWeight> result, Board board,
 			Marker marker, double weight) {
 		int size = board.getSize();
 		for (int x = 0; x < size; ++x) {
 			for (int y = 0; y < size; ++y) {
 				Marker boardMarker = board.getCell(x, y);
 				Cell cell = new Cell(x, y);
 				MoveAndWeight move = new MoveAndWeight(cell, marker, weight);
 				if (boardMarker == Marker.EMPTY && marker != Marker.EMPTY) {
 					result.add(move);
 				} else if (boardMarker != Marker.EMPTY
 						&& marker == Marker.EMPTY) {
 					result.add(move);
 				}
 			}
 		}
 	}
 
 	private int scoreLine(int numMine, int numOpponent) {
 		if (numMine == 3) {
 			return 1000;
 		}
 		if (numOpponent == 3) {
 			return -1000;
 		}
 
 		if (numMine == 2 && numOpponent == 0)
 			return 100;
 		if (numMine == 1 && numOpponent == 0)
 			return 10;
 
 		if (numMine == 0 && numOpponent == 2)
 			return -100;
 		if (numMine == 0 && numOpponent == 1)
 			return -10;
 
 		return 0;
 	}
 
 	private int getHeuristicScore(Board board) {
 		int size = board.getSize();
 		int score = 0;
 		Marker opponent = player.opponent();
 
 		// Inspect the columns
 		for (int x = 0; x < size; ++x) {
 			int numMine = 0;
 			int numOpponent = 0;
 
 			for (int y = 0; y < size; ++y) {
 				Marker cell = board.getCell(x, y);
 				if (cell == player) {
 					numMine++;
 				}
 				if (cell == opponent) {
 					numOpponent++;
 				}
 			}
 
 			score += scoreLine(numMine, numOpponent);
 		}
 
 		// Inspect the rows
 		for (int y = 0; y < size; ++y) {
 			int numMine = 0;
 			int numOpponent = 0;
 
 			for (int x = 0; x < size; ++x) {
 				Marker cell = board.getCell(x, y);
 				if (cell == player) {
 					numMine++;
 				}
 				if (cell == opponent) {
 					numOpponent++;
 				}
 			}
 
 			score += scoreLine(numMine, numOpponent);
 		}
 
 		// Inspect the top-left/bottom-right diagonal
 		int numMine = 0;
 		int numOpponent = 0;
 		for (int x = 0; x < size; ++x) {
 			Marker cell = board.getCell(x, x);
 			if (cell == player) {
 				numMine++;
 			}
 			if (cell == opponent) {
 				numOpponent++;
 			}
 		}
 
 		score += scoreLine(numMine, numOpponent);
 
 		numMine = 0;
 		numOpponent = 0;
 		// Inspect the bottom-right/top-right
 		for (int x = 0; x < size; ++x) {
 			Marker cell = board.getCell(x, size - x - 1);
 			if (cell == player) {
 				numMine++;
 			}
 			if (cell == opponent) {
 				numOpponent++;
 			}
 		}
 
 		score += scoreLine(numMine, numOpponent);
 
 		return score;
 	}
 
 	public static class MoveAndWeight {
 		Cell move;
 		Marker marker;
 		double weight;
 
 		public MoveAndWeight(Cell move, Marker marker, double weight) {
 			this.move = move;
 			this.marker = marker;
 			this.weight = weight;
 		}
 	}
 
 	public static class MoveAndScore {
 		Cell move;
 		double score;
 
 		MoveAndScore(Cell move, double score) {
 			this.score = score;
 			this.move = move;
 		}
 	}
 
 }
