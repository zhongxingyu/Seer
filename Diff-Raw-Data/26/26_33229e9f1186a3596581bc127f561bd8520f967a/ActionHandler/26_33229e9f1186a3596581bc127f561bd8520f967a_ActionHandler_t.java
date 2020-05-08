 package ch.gibb.project.controller;
 
 import android.graphics.PointF;
 import android.graphics.RectF;
 import ch.gibb.project.activity.Level;
 import ch.gibb.project.elements.Ball;
 import ch.gibb.project.elements.Maze;
 import ch.gibb.project.elements.Point;
 import ch.gibb.project.elements.Text;
 import ch.gibb.project.elements.Wall;
 import ch.gibb.project.enums.Dimension;
 
 public class ActionHandler {
 	private Ball ballElement;
 	private Maze mazeElement;
 	private Wall wallElement;
 	private Point pointElement;
 	private Text textElement;
 
 	public ActionHandler(Level levelActivity) {
 		ballElement = levelActivity.getBallElement();
 		mazeElement = levelActivity.getMazeElement();
 		wallElement = levelActivity.getWallElement();
 		pointElement = levelActivity.getPointElement();
 		textElement = levelActivity.getTextElement();
 	}
 
 	public boolean containsBallX(float accelX) {
 		if ((ballElement.getCoordinates().x - (accelX * 2)) > mazeElement
 				.getPlayGround().left
 				&& (ballElement.getCoordinates().x - (accelX * 2)) < (mazeElement
 						.getPlayGround().right - (ballElement.getDiameter()))) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean containsBallY(float accelY) {
 		if ((ballElement.getCoordinates().y + (accelY * 2)) > mazeElement
 				.getPlayGround().top
 				&& (ballElement.getCoordinates().y + (accelY * 2)) < mazeElement
 						.getPlayGround().bottom - (ballElement.getDiameter())) {
 			return true;
 		}
 		return false;
 	}
 
 	public boolean touchOnWall(Dimension ort) {
 		switch (ort) {
 		case LEFT:
 			if ((ballElement.getCoordinates().x - mazeElement.getPlayGround().left) < (mazeElement
 					.getPlayGround().right - ballElement.getCoordinates().x)) {
 				return true;
 			}
 			break;
 		case TOP:
 			if ((ballElement.getCoordinates().y - mazeElement.getPlayGround().top) < (mazeElement
 					.getPlayGround().bottom - ballElement.getCoordinates().y)) {
 				return true;
 			}
 			break;
 		case RIGHT:
 			if ((ballElement.getCoordinates().x - mazeElement.getPlayGround().right) < (mazeElement
 					.getPlayGround().left - ballElement.getCoordinates().x)) {
 				return true;
 			}
 			break;
 		case BOTTOM:
 			if ((ballElement.getCoordinates().y - mazeElement.getPlayGround().bottom) < (mazeElement
 					.getPlayGround().top - ballElement.getCoordinates().y)) {
 				return true;
 			}
 			break;
 		}
 		return false;
 	}
 
 	public boolean isBallInHole() {
 		for (PointF hole : mazeElement.getHoles()) {
 			float dx = Math.abs(ballElement.getCoordinates().x
 					- wallElement.xCompare(hole.x));
 			float dy = Math.abs(ballElement.getCoordinates().y
 					- wallElement.yCompare(hole.y));
			if (Math.sqrt(mazeElement.xCompare(dx * dx)
					+ mazeElement.yCompare(dy * dy)) < 33) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void checkStarCollision() {
 		for (PointF point : pointElement.getPoints()) {
 			float dx = Math.abs(ballElement.getCoordinates().x
 					- wallElement.xCompare(point.x));
 			float dy = Math.abs(ballElement.getCoordinates().y
 					- wallElement.yCompare(point.y));
 
 			if (Math.sqrt(dx * dx + dy * dy) < 33) {
 				removeStar(point);
 				break;
 			}
 		}
 	}
 
 	private void removeStar(PointF toRemove) {
 		pointElement.getPoints().remove(toRemove);
 		pointElement.postInvalidate((int) toRemove.x - 1, (int) toRemove.y + 1,
 				(int) toRemove.x + pointElement.getImageWidth() + 1,
 				(int) toRemove.y - pointElement.getImageHeight() + 1);
 		Text.pointcount += Text.stage;
 		textElement.postInvalidate();
 	}
 
 	public boolean checkWallTouch(float x, float y) {
 		for (RectF rect : wallElement.getWalls()) {
 			RectF wallRect = new RectF(wallElement.xCompare(rect.left),
 					wallElement.yCompare(rect.top),
 					wallElement.xCompare(rect.right),
 					wallElement.yCompare(rect.bottom));
 			RectF ballAsRect = ballElement.generateRect(x, y);
 			if (ballAsRect.intersect(wallRect)) {
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public void moveAndCheckX(float accelX) {
 		if (containsBallX(accelX)) {
 			float newX = ballElement.getCoordinates().x
 					- ballElement.xCompare(accelX * 2);
 			if (!checkWallTouch(newX, ballElement.getCoordinates().y)) {
 				ballElement.getCoordinates().x = newX;
 			}
 		} else if (touchOnWall(Dimension.LEFT)) {
 			ballElement.getCoordinates().x = mazeElement.getPlayGround().left;
 		} else if (touchOnWall(Dimension.RIGHT)) {
 			ballElement.getCoordinates().x = (mazeElement.getPlayGround().right - (ballElement
 					.xCompare(ballElement.getRadius() * 2)));
 		}
 	}
 
 	public void moveAndCheckY(float accelY) {
 		if (containsBallY(accelY)) {
 			float newY = ballElement.getCoordinates().y
 					+ ballElement.yCompare(accelY * 2);
 			if (!checkWallTouch(ballElement.getCoordinates().x, newY)) {
 				ballElement.getCoordinates().y = newY;
 			}
 
 		} else if (touchOnWall(Dimension.TOP)) {
 			ballElement.getCoordinates().y = mazeElement.getPlayGround().top;
 		} else if (touchOnWall(Dimension.BOTTOM)) {
 			ballElement.getCoordinates().y = (mazeElement.getPlayGround().bottom - (ballElement
 					.yCompare(ballElement.getRadius() * 2)));
 		}
 	}
 
 	public boolean isGameFinished() {
 		if (mazeElement.getFinishRect().contains(ballElement.getBallRect())) {
 			return true;
 		}
 		return false;
 	}
 }
