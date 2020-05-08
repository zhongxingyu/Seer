 package obstacles;
 import game.GameOverException;
 import game.SnakeModel;
 
 import java.awt.Graphics2D;
 import java.awt.geom.Rectangle2D;
 
 
 
 import java.awt.geom.Ellipse2D;
 
 
 public class RectObstacle extends Obstacle{
 	private final int FRAMES_PER_SECOND = 4;
 	public Rectangle2D rect;
 	public SnakeModel mySnake;
 	public RectObstacle(int duration, double xloc, double yloc, double d, SnakeModel snake){
 		rect = new Rectangle2D.Double(xloc, yloc, d, d);
 		origTimer = duration;
 		timer = 0;
 		mySnake = snake;
 	}
 
 	@Override
 	public void update() throws GameOverException {
 		timer++;
		if (timer == origTimer * FRAMES_PER_SECOND){
 			if (haveCollided())
 				throw new GameOverException();
 			rect.setFrame(0,0,0,0);
 		}
 		else {
			double alpha = 1 + (timer / (origTimer * FRAMES_PER_SECOND));
 			double x = rect.getX();
 			double y = rect.getY();
 			double d = rect.getWidth();
 			//double h = rect.getHeight();
 			double centerX = x + d / 2;
 			double centerY = y + d / 2;
			d = d * alpha;
 			x = centerX - d / 2;
 			y = centerY - d / 2;
 			rect.setRect(x, y, d, d);
 		}
 	}
 
 	@Override
 	public boolean haveCollided() {
 		boolean tempresult = false;
 
 		// get dimensions of snake
 		double r1 = mySnake.getInnerRadius();
 		double r2 = mySnake.getOuterRadius();
 		double cx = mySnake.getOrigin().getX();
 		double cy = mySnake.getOrigin().getY();
 
 		Ellipse2D e = new Ellipse2D.Double(cx-r1, cy-r1, 2*r1, 2*r1);
 		Ellipse2D e2 = new Ellipse2D.Double(cx-r2, cy-r2, 2*r2, 2*r2);
 		Rectangle2D s2 = e2.getBounds2D();
 
 		if (e.contains(rect))
 			return false;
 		if (s2.intersects(rect))
 			tempresult = true;
 		if (tempresult) {
 			 for (int i = 0; i < 360; i += 5)
 				{
 					double testX = cx + (Math.cos(Math.toRadians(i)) *r2);
 					double testY = cy + (Math.sin(Math.toRadians(i)) *r2);
 					if (rect.contains(testX, testY))
 						return true;
 				}
 		}
 
 		return false;
 	}
 
 	@Override
 	public void draw(Graphics2D g) {
 		g.draw(rect);
 	}
 
 }
