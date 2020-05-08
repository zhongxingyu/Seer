 package pylos.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import pylos.Config;
 import pylos.exception.PylosError;
 import pylos.view.PlayerGraphics;
 
 public class Player {
 	public enum Action {
 		/** place a new ball or remove one to mount */
 		PLACE,
 		/** place the removed ball on a higher level */
 		MOUNT,
 		/** choose 1 or 2 balls to remove from board */
 		REMOVE,
 		/** Wait for other player */
 		WAIT
 	}
 
 	public enum Location {
 		UNDEFINED,
 		LOCAL,
 		AI,
 		REMOTE
 	}
 
 	public static final int nbBalls = Model.BALLS / 2;
 	public final Ball[] balls = new Ball[nbBalls];
 	public final int side;
 	public PlayerGraphics graphics;
 	public Action action = Action.PLACE;
 	Location location = Location.UNDEFINED;
 
 	public Player(int side) {
 		this.side = side;
 		for (int i = 0; i < balls.length; i++) {
 			balls[i] = new Ball(this);
 		}
 		graphics = new PlayerGraphics(this);
 	}
 
 	public static Player fromByte(byte player) {
 		switch (player) {
 		case 1:
 			return Model.player1;
 
 		case 2:
 			return Model.player2;
 
 		default:
 			System.err.println("Invalid player number: " + player);
 			return null;
 		}
 	}
 
 	public byte toByte() {
 		return (byte) (this == Model.player1 ? 1 : 2);
 	}
 
 	@Override
 	public String toString() {
 		return "Player " + toByte();
 	}
 
 	public String toStringWithLocation() {
 		return this + " (" + location + ")";
 	}
 
 	public Player other() {
 		return Model.player1 == this ? Model.player2 : Model.player1;
 	}
 
 	public LinkedList<Ball> partitionBalls(LinkedList<Ball> ballsOnBoard) {
 		LinkedList<Ball> ballsOnSide = new LinkedList<Ball>();
 		for (Ball ball : balls) {
 			if (ball.onBoard) {
 				ballsOnBoard.add(ball);
 			} else {
 				ballsOnSide.add(ball);
 			}
 		}
 		return ballsOnSide;
 	}
 
 	public void putBallOnBoard(Position position) {
 		Ball last = lastBallOnSide();
 		last.placeAt(position);
 	}
 
 	private Ball lastBallOnSide() {
 		for (int i = nbBalls - 1; i >= 0; i--) {
 			if (!balls[i].onBoard) {
 				return balls[i];
 			}
 		}
 		throw new PylosError("Could not find ball in the side to put on the pyramid");
 	}
 
 	/**
 	 * Balls which can be removed from the board and be placed back on the side
 	 */
 	public List<Ball> getRemovableBalls() {
 		List<Ball> list = new LinkedList<Ball>();
 		for (Ball ball : balls) {
 			if (ball.isRemovable())
 				list.add(ball);
 		}
 		return list;
 	}
 
 	public List<Ball> getMountableBalls() {
 		List<Ball> list = new LinkedList<Ball>();
 		for (Ball ball : balls) {
 			if (ball.isMountable())
 				list.add(ball);
 		}
 		return list;
 	}
 
 	public boolean anyLineOrSquare(Position position) {
 		return anyLine(position) || anySquare(position);
 	}
 
 	private boolean anyLine(Position position) {
 		if (position.z >= 2)
 			return false;
 		for (List<Position> line : position.lines()) {
 			boolean validLine = true;
 			for (Position pos : line) {
 				if (!Model.board.anyBallAt(pos) || Model.board.ballAt(pos).owner != this) {
 					validLine = false;
 					break;
 				}
 			}
 			if (validLine)
 				return true;
 		}
 		return false;
 	}
 
 	private boolean anySquare(Position position) {
 		if (position.z >= 2)
 			return false;
 		for (List<Position> list : position.fourSquare()) {
 			boolean validSquare = true;
 			for (Position pos : list) {
 				if (!Model.board.anyBallAt(pos) || Model.board.ballAt(pos).owner != this) {
 					validSquare = false;
 					break;
 				}
 			}
 			if (validSquare)
 				return true;
 		}
 		return false;
 	}
 
 	public void mountBall(Ball ball) {
 		ball.removeFromBoard();
 		if (canMove())
 			action = Action.MOUNT;
 	}
 
 	public void removeBall(Ball ball) {
 		ball.removeFromBoard();
 	}
 
 	public boolean allBallsOnBoard() {
 		for (Ball ball : balls) {
 			if (!ball.onBoard)
 				return false;
 		}
 		return true;
 	}
 
 	// Action methods
 	public void resetAction() {
 		if (canMove()) {
 			action = Action.PLACE;
 		} else {
 			action = Action.WAIT;
 		}
 	}
 
 	public boolean isMounting() {
 		return action == Action.PLACE;
 	}
 
 	public boolean isPlacing() {
 		return action == Action.PLACE || action == Action.MOUNT;
 	}
 
 	public boolean isRemoving() {
 		return action == Action.REMOVE;
 	}
 
 	public void removeBalls() {
 		if (canMove())
 			action = Action.REMOVE;
 	}
 
 	// Location methods
 	public void isLocal() {
 		location = Location.LOCAL;
 		resetAction();
 		System.out.println(toStringWithLocation());
 	}
 
 	public void isRemote() {
 		location = Location.REMOTE;
 		resetAction();
 		System.out.println(toStringWithLocation());
 	}
 
 	public void isAi() {
 		location = Location.AI;
 		resetAction();
 		System.out.println(toStringWithLocation());
 	}
 
 	public boolean canMove() {
		// FIXME: should not be UNDEFINED
		return Config.CAN_MOVE_OTHER || location == Location.LOCAL || location == Location.UNDEFINED;
 	}
 
 	public boolean isUndefined() {
 		return location == Location.UNDEFINED;
 	}
 
 	public boolean local() {
 		return location != Location.REMOTE;
 	}
 
 	public boolean Ai() {
 		return location == Location.AI;
 	}
 }
