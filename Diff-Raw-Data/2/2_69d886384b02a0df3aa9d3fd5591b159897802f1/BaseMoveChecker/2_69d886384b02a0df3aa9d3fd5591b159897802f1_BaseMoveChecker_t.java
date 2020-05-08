 package edu.fmi.ai.reversi.move;
 
 import edu.fmi.ai.reversi.model.Board;
 import edu.fmi.ai.reversi.model.Cell;
 import edu.fmi.ai.reversi.model.Player;
 
 public abstract class BaseMoveChecker {
 
 	protected Board board;
 
 	protected BaseMoveChecker(final Board board) {
 		this.board = board;
 	}
 
 	protected boolean isStoppingSearch(final Player forPlayer, int currentNeighbour,
 			final Cell currentCell) {
 		return currentCell.isEmpty() || (currentCell.isOwnedBy(forPlayer) && currentNeighbour == 1);
 	}
 
 	protected boolean isClosestNeighbour(final Player forPlayer, int currentNeighbour,
 			final Cell currentCell) {
		return currentCell.isOwnedBy(forPlayer) && (currentNeighbour > 1 || forPlayer == Player.UNKNOWN);
 	}
 
 	public boolean isMovePermitted(final Cell cell, final Player player) {
 		return getNeighbourIndex(cell, player) > 0;
 	}
 
 	protected boolean isStableCell(final Cell cell, final Player player,
 			final boolean isMinusDirection) {
 		final Player otherPlayer = Player.getOpponent(player);
 		return getNeighbourIndex(cell, otherPlayer, isMinusDirection, false) < 0
 				&& getNeighbourIndex(cell, Player.UNKNOWN, isMinusDirection, false) < 0;
 	}
 
 	protected abstract int getNeighbourIndex(final Cell cell, final Player player);
 
 	protected abstract int getNeighbourIndex(final Cell cell, final Player player,
 			final boolean isMinusDirection, final boolean isStoppingSearch);
 
 	protected abstract int incrementIndex(final int cellIndex, final boolean isMinusDirection);
 
 }
