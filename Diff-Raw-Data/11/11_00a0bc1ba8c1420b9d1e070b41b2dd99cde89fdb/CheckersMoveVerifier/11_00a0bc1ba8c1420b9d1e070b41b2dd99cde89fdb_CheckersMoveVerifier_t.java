 package verifiers;
 
 import game.CheckersGame;
 
 import java.awt.Point;
 
 import piece.BlankPiece;
 import piece.CheckerPiece;
 
 public class CheckersMoveVerifier implements MoveVerifyer<CheckersGame> {
 
 	/**
 	 * Sorry about the length of the method!
 	 * 
 	 * @Override
 	 */
 	public boolean legalMove(CheckersGame game, Point originalSpot, Point newSpot) {
 		// TODO don't let pieces move side to side by one space
 		/*
 		 * first we verify that the move points are legal
 		 */
 		// if the first point is out of bounds
 		// return false
 		if (originalSpot.x >= game.getDimension().width
 				|| originalSpot.y >= game.getDimension().height)
 			return false;
 		if (originalSpot.x < 0 || originalSpot.y < 0)
 			return false;
 		// if the second point is out of bounds
 		// return false
 		if (newSpot.x >= game.getDimension().width
 				|| newSpot.y >= game.getDimension().height)
 			return false;
 		if (newSpot.x < 0 || newSpot.y < 0)
 			return false;
 
 		// if the first point is the same as the second point
 		// return false
 		if (originalSpot.equals(newSpot))
 			return false;
 
 		/*
 		 * Now we verify that the piece is making a move in the right direction
 		 */
 
 		// the first piece must be a CheckerPiece..otherwise
 		// the move cannot be valid
 		CheckerPiece pieceAtFirstPoint = null;
 		try {
 			pieceAtFirstPoint = (CheckerPiece) game.getPiece(originalSpot);
 		} catch (ClassCastException e) {
 			return false;
 		}
 
 		if (pieceAtFirstPoint.equals(CheckerPiece.RED())) {
 			// ie from bottom to top, or top to bottom
 			if (game.getSide(0).equals(CheckersGame.RED_SIDE)) {
 				// The row in the new point better be higher than the row in
 				// the second point
 				if (originalSpot.y > newSpot.y) {
 					return false;
 				}
 			} else {
 				// the row in the new point better be less than
 				// the row in the old point
 				if (originalSpot.y < newSpot.y) {
 					return false;
 				}
 			}
 		} else if (pieceAtFirstPoint.equals(CheckerPiece.BLACK())) {
 			// ie from bottom to top, or top to bottom
 			if (game.getSide(0).equals(CheckersGame.BLACK_SIDE)) {
 				// The row in the new point better be higher than the row in
 				// the second point
 				if (originalSpot.y > newSpot.y) {
 					return false;
 				}
 			} else {
 				// the row in the new point better be less than
 				// the row in the old point
 				if (originalSpot.y < newSpot.y) {
 					return false;
 				}
 			}
 		} else {
 			// if its a king then it can go either way
 		}
 
 		/*
 		 * The second piece better be a BlankPiece.BLANK
 		 */
		@SuppressWarnings("unused")
 		BlankPiece pieceAtSecondPoint = null;
 		try {
 			pieceAtSecondPoint = (BlankPiece) game.getPiece(newSpot);
 		} catch (ClassCastException e) {
 			return false;
 		}
 
 		/*
 		 * Make sure the piece is moving diagonally
 		 */
 		if (originalSpot.x == newSpot.x || originalSpot.y == newSpot.y)
 			return false;
 		// diagonal implies same dx and dy
 		if (Math.abs(newSpot.x - originalSpot.x) != Math.abs(newSpot.y - originalSpot.y))
 			return false;
 
 		/*
 		 * Check for jump case if the y distance is greater than 1 if there is no jump,
 		 * then the move should be valid
 		 */
 		if ((Math.abs(originalSpot.y - newSpot.y) > 1)
 				|| Math.abs(originalSpot.x - newSpot.x) > 1) {
 			// TODO jump case stuff
 			// TODO no jumping w/out removing pieces, or no jumping yourself
 			// if the y or x dist is too far..then we have an illegal move
 			if (Math.abs(originalSpot.y - newSpot.y) > 2
 					|| (Math.abs(originalSpot.x - newSpot.x) > 2)) {
 				return false;
 			}
 
 			// TODO fire a piece jumped at the correct point if a legal jumped happened
 		}
 
 		return true;
 	}
 }
