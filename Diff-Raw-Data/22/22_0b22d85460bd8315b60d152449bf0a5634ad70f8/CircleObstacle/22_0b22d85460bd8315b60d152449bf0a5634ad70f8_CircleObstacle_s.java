 package obstacles;
 
 import game.GameOverException;
 import game.SnakeBoundary;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.geom.Ellipse2D;
 import java.util.Random;
 
 
 public class CircleObstacle extends Obstacle{
 
 	private final int FRAMES_PER_SECOND = 50;
 	private float originalDiameter;
 
 	SnakeBoundary mySnake;
 
 	public Ellipse2D.Float circle = new Ellipse2D.Float();
 	
 	public CircleObstacle(int duration, float xloc, float yloc, float d, SnakeBoundary snake){
 		circle = new Ellipse2D.Float(xloc, yloc, d, d);
 		origTimer = duration;
 		timer = 0;
 		mySnake = snake;
 		originalDiameter = d;
 	}
 
 
 	///////////////////////////////////////////////////////
 	// NOTE: this now returns bool in order to communicate
 	//		with Game Dialog (i.e. to notify the wall that
 	//		it has passed the snake and should be removed
 	//		from collection of obstacles
 	///////////////////////////////////////////////////////
 	@Override
 	public boolean update() throws GameOverException {
 		float x, y;
 		timer++;
 
 		double centerXdouble = circle.getCenterX();
 		float centerX = (float) centerXdouble;
 
 		double centerYdouble = circle.getCenterY();
 		float centerY = (float) centerYdouble;
 
 		if (timer >= origTimer * FRAMES_PER_SECOND) {
 			if (haveCollided())
 				throw new GameOverException();
 
 			circle.setFrame(0,0,0,0);
 			return false;
 		}
 		else
 		{
 			float alpha = 1 + (timer * 2.0f / (origTimer * FRAMES_PER_SECOND));
 			float d = originalDiameter * alpha;
 			x = centerX - (float) d / 2;
 			y = centerY - (float) d / 2;
 			circle.setFrame(x, y, d, d);
 		}
 		return true;
 	}
 
 	@Override
 	public boolean haveCollided() {
 
 		// centre of snake
 		double snakeX = mySnake.getOrigin().getX() + mySnake.getOuterRadius();
 		double snakeY = mySnake.getOrigin().getY() + mySnake.getOuterRadius();
 
 		/// centre of circle obstacle
 		//double topx = circle.getX();
 		//double topy = circle.getY();
 		double d = circle.getWidth();
 
 		//double circleX = topx + d / 2;
 		//double circleY = topy - d / 2;
 		double circleX = circle.getCenterX();
 		double circleY = circle.getCenterY();
 
 		// smaller and larger radii of snake
 		Dimension[][] dimArray = mySnake.getDimArray();
		int counter = mySnake.getCounter() - 1;
 		int INNER = 0, OUTER = 1;
 		double r1 = dimArray[counter][INNER].height; 
 		double r2 = dimArray[counter][OUTER].height;
 
 		double distance = Math.sqrt( Math.pow( circleX - snakeX, 2 ) + Math.pow( circleY - snakeY, 2 ) );
 		if(distance >= r1 && distance <= r2)
 			return true;
 		else if(distance < r1)
 			return (distance + d / 2) >= r1;
 		else 
 			return (distance - d / 2) <= r2;
 
 	}
 
 	@Override
 	public void draw(Graphics g) {
 		Graphics2D g2d = (Graphics2D)g;
 		Color color = g2d.getColor();
 		g2d.setColor(Color.red);
 		g2d.draw(circle);
 	}
 }
