 package pylos.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import pylos.exception.PylosError;
 import pylos.view.PlayerGraphics;
 
 public class Player {
 	public enum Action {
 		/** place a new ball or remove one to mount */
 		PLACE,
 		/** place the removed ball on a higher level */
 		RISE,
 		/** choose 1 or 2 balls to remove from board */
 		REMOVE,
 		/** Wait for other player */
 		WAIT
 	}
 
 	public static final int nbBalls = Model.BALLS / 2;
 	public final Ball[] balls = new Ball[nbBalls];
 	public final int side;
 	public PlayerGraphics graphics;
 	public Action action = Action.PLACE;
 
 	public Player(int side) {
 		this.side = side;
 		for (int i = 0; i < balls.length; i++) {
 			balls[i] = new Ball(this);
 		}
 		graphics = new PlayerGraphics(this);
 	}
 
 	@Override
 	public String toString() {
 		return "Player " + (this == Model.player1 ? 1 : 2);
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
 
 	public boolean checkIfAnyLineOrSquare(int level) {
 		if (checkIfAnyLine(level))
 			return true;
 		if (checkIfAnySquare(level))
 			return true;
 		return false;
 	}
 
 	public boolean checkIfAnyLine(int level) { // Is there a point knowing which line is it ?
 		List<List<Position>> lines = new LinkedList<List<Position>>();
 		for (int i = 0; i < Model.LEVELS - level; i++) {
 			lines.add(Model.getAllPositionsForLevelLineByLine(level, i));
 		}
 		for (List<Position> line : lines) {
 			if (checkIfThisLineOrSquare(line))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean checkIfAnySquare(int level) {
 		List<List<Position>> squares = Model.getAllPositionForLevelSquareBySquare(level);
 		for (List<Position> square : squares) {
 			if (checkIfThisLineOrSquare(square))
 				return true;
 		}
 		return false;
 	}
 
 	public boolean checkIfThisLineOrSquare(List<Position> lineOrSquare) {
 		for (Position position : lineOrSquare) {
 			if (!isBallOnThisPosition(position))
 				return false;
 		}
 		return true;
 	}
 
 	public boolean isBallOnThisPosition(Position position) { // does ball owned by this player be on this position ?
 		for (Ball ball : balls) {
 			if (ball.position == position)
 				return true;
 		}
 		return false;
 	}
 
 	public void riseBall(Ball ball) {
 		ball.removeFromBoard();
 		action = Action.RISE;
 	}
 
 	// Action methods
 	public void resetAction() {
 		action = Action.PLACE;
 	}

	public boolean canRise() {
		return action == Action.PLACE;
	}

	public boolean allBallsOnBoard() {
		for (Ball ball : balls) {
			if (!ball.onBoard)
				return false;
		}
		return true;
	}

 }
