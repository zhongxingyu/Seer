 package obstacles;
 
import game.GameOverException;

 import java.awt.geom.Ellipse2D;
 
 
 public class CircleObstacle extends Obstacle{
 	private final int FRAMES_PER_SECOND = 4;
 	public CircleObstacle(int duration, double xloc, double yloc, double r){
 		circle = new Ellipse2D.Double(xloc, yloc, r, r);
 		origTimer = duration;
 		timer = 0;
 	}
 	
 	public Ellipse2D.Double circle;
 	@Override
	public void update() throws GameOverException {
 		timer++;
 		if (timer == origTimer * FRAMES_PER_SECOND){
 			if (haveCollided())
 				throw new GameOverException();
 			circle.setFrame(0,0,0,0);
 		}
 		else {
 			double alpha = 1 + (timer / origTimer * FRAMES_PER_SECOND);
 			double x = circle.getX();
 			double y = circle.getY();
 			double r = circle.getWidth();
 			double centerX = x + r / 2;
 			double centerY = y - r / 2;
 			r = r * alpha;
 			x = centerX - r;
 			y = centerY + r;
 			circle.setFrame(x, y, r, r);
 		}
 	}
 
 	@Override
 	public void draw() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean haveCollided() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 }
