 package net.todd.games.balls.client.ui;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Graphics;
 import java.awt.Point;
 
 import net.todd.games.balls.common.Ball;
 
 public class BouncyBallComponent extends Component {
 	private static final long serialVersionUID = 3146348058907303750L;
 	private static final int BALL_SIZE = 20;
 	private Ball[] ballData;
 	private final CoordinateConverter coordinateConverter;
 
 	public BouncyBallComponent() {
 		coordinateConverter = new CoordinateConverter();
 	}
 
 	@Override
 	public void paint(Graphics g) {
 		drawGrid(g);
 		drawBalls(g);
 	}
 
 	private void drawBalls(Graphics g) {
 		for (Ball ball : getBallData()) {
 			g.setColor(new Color(ball.getColorRed(), ball.getColorGreen(), ball
 			        .getColorBlue()));
 
 			Point point = coordinateConverter.toXY(ball.getPositionX(), ball
 			        .getPositionY(), ball.getPositionZ());
 
 			point = coordinateConverter.scale(point, getSize());
 
 			g.fillOval(point.x, point.y, BALL_SIZE, BALL_SIZE);
 		}
 	}
 
 	private void drawGrid(Graphics g) {
		for (int i = 0; i < 20; i++) {
 			Point startingPoint = coordinateConverter.scale(new Point(0, i), getSize());
 			Point endingPoint = coordinateConverter.scale(new Point(20, i), getSize());
 
 			g.drawLine(startingPoint.x, startingPoint.y, endingPoint.x, endingPoint.y);
 		}
 
		for (int i = 0; i < 20; i++) {
 			Point startingPoint = coordinateConverter.scale(new Point(i, 0), getSize());
 			Point endingPoint = coordinateConverter.scale(new Point(i, 20), getSize());
 
 			g.drawLine(startingPoint.x, startingPoint.y, endingPoint.x, endingPoint.y);
 		}
 	}
 
 	public void setBallPositions(Ball[] ballData) {
 		this.ballData = ballData;
 	}
 
 	private Ball[] getBallData() {
 		return ballData == null ? new Ball[0] : ballData;
 	}
 }
