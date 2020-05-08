 package tictactoe;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.List;
 
 import tictactoe.Game.SquareState;
 
 public class UnbeatableStrategy implements MoveChoosingStrategy {
 
 	@Override
 	public Location chooseMove(Game gameState) {
 		// Square state representing player about to move
 		final SquareState me = getMe(gameState);
 		// Square state representing opponent of player about to move
 		final SquareState opponent = getOpponentOf(me);
 
 		// List of possible moves
 		List<Location> emptyLocations = new ArrayList<>();
 		for (int column = 0 ; column < 3; column++) {
 			for (int row = 0 ; row < 3; row++) {
 				SquareState square = gameState.getSquare(column, row);
 				if (square == SquareState.EMPTY) {
 					emptyLocations.add(new Location(column, row));
 				}
 			}
 		}
 		// shuffle so we get some variety in the play:
 		Collections.shuffle(emptyLocations);
 		
 		// look for winning move:
 		List<Location> winningLine = findWinningLine(gameState, me, opponent);
 		if (winningLine != null) {
 			// There's a winning line for me; find the empty square on it and return that square
 			for (Location loc : winningLine) {
 				if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
 					return loc ;
 				}
 			}
 		}
 		
 		// look to block opponent:
 		List<Location> opponentsWinningLine = findWinningLine(gameState, opponent, me) ;
 		if (opponentsWinningLine != null) {
 			// There's a winning line for my opponent; find the empty square on it and return that square 
 			// (blocks opponent)
 			for (Location loc : opponentsWinningLine) {
 				if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
 					return loc ;
 				}
 			}
 		}
 		
 		// look for "fork" (i.e. a move that will give me two places to win on my next move)
 		
 		for (Location location : emptyLocations) {
 			if (createsFork(location, gameState, me)) {
 				return location ;
 			}
 		}
 		
 		// Now look to force a move away from the opponent being able to create a fork...
 		
 		for (Location location : emptyLocations) {
 			List<List<Location>> intersectingLines = getIntersectingLines(location);
 			for (List<Location> line : intersectingLines) {
 				// check to see if I can threaten to win on this line next move
 				if (count(line, me, gameState)==1 && count(line, opponent, gameState)==0) {
 					// Find the other empty square on this line; this is where we force our opponent to play
 					for (Location loc : line) {
 						if (!loc.equals(location) && gameState.squareProperty(loc).get()==SquareState.EMPTY) {
 							// Make sure we don't force our opponent to create a fork!
 							if (! createsFork(loc, gameState, opponent)) {
								return location ;
 							}
 						}
 					}
 				}
 			}
 		}
 
 		// Just choose "best" general square...
 		
 		// default order of squares...
 		List<Location> orderedLocs = Arrays.asList(
 			new Location(1, 1),
 			new Location(0, 0),
 			new Location(2, 0),
 			new Location(0, 2), 
 			new Location(2, 2),
 			new Location(1, 0), 
 			new Location(0, 1), 
 			new Location(2, 1), 
 			new Location(1, 2)
 		);
 		for (Location loc : orderedLocs) {
 			if (gameState.squareProperty(loc).get() == SquareState.EMPTY) {
 				return loc ;
 			}
 		}
 		// Only get here if board is full....
 		return null ;
 	}
 	
 	// Checks to see if playing at location creates a fork for the player represented by playerMark
 	private boolean createsFork(Location location, Game game, SquareState playerMark) {
 		SquareState otherMark = getOpponentOf(playerMark);
 		List<List<Location>> intersectingLines = getIntersectingLines(location);
 		int countPossibleWinningLines = 0 ;
 		for (List<Location> line : intersectingLines) {
 			if (count(line, playerMark, game) == 1 && count(line, otherMark, game) == 0) {
 				countPossibleWinningLines++ ;
 			}
 		}
 		return countPossibleWinningLines >= 2 ;
 	}
 
 	// Just return the mark for the opponent of the player represented by playerMark
 	// i.e. if O is provided, return X, and vice versa
 	private SquareState getOpponentOf(SquareState playerMark) {
 		SquareState otherMark ;
 		if (playerMark == SquareState.O) {
 			otherMark = SquareState.X ;
 		} else if (playerMark == SquareState.X) {
 			otherMark = SquareState.O ;
 		} else {
 			otherMark = null ;
 		}
 		return otherMark;
 	}
 
 	// Return a list of all lines intersecting the given location
 	private List<List<Location>> getIntersectingLines(Location location) {
 		List<List<Location>> intersectingLines = new ArrayList<>();
 		for (List<Location> line : Game.LINES) {
 			if (line.contains(location)) {
 				intersectingLines.add(line);
 			}
 		}
 		return intersectingLines;
 	}
 
 	// If there is a line for which the specified player can win, return it
 	private List<Location> findWinningLine(Game game, SquareState player, SquareState opponent) {
 		for (List<Location> line : Game.LINES) {
 			if (count(line, player, game) == 2 && count(line, opponent, game) == 0) {
 				return line ;
 			}
 		}
 		return null ;
 	}
 	
 	// Return the mark (O or X) represented by the current player in the game
 	private SquareState getMe(Game game) {
 		if (game.getCurrentPlayer() == game.getOPlayer()) {
 			return SquareState.O ;
 		} else if (game.getCurrentPlayer() == game.getXPlayer()) {
 			return SquareState.X ;
 		} else {
 			return null ;
 		}
 	}
 	
 	// Count the number of marks equal to target in the specified line
 	private int count(List<Location> line, SquareState target, Game game) {
 		int count = 0 ;
 		for (Location loc : line ) {
 			SquareState square = game.squareProperty(loc).get();
 			if (square == target) {
 				count ++ ;
 			}
 		}
 		return count ;
 	}
 	
 }
