 package obstacles;

 import java.awt.geom.Rectangle2D;
 
 
 
 
 public class RectObstacle extends Obstacle{
 	private final int FRAMES_PER_SECOND = 4;
 	public Rectangle2D rect;
 	public RectObstacle(int duration, double xloc, double yloc, double r){
 		rect = new Rectangle2D.Double(xloc, yloc, r, r);
 		origTimer = duration;
 		timer = 0;
 	}
 
 	@Override
	public void update() {
 		timer++;
 		if (timer == origTimer * FRAMES_PER_SECOND){
 			if (haveCollided())
 				throw new GameOverException();
 			rect.setFrame(0,0,0,0);
 		}
 		else {
 			double alpha = 1 + (timer / origTimer * FRAMES_PER_SECOND);
 			double x = rect.getX();
 			double y = rect.getY();
 			double r = rect.getWidth();
 			//double h = rect.getHeight();
 			double centerX = x + r / 2;
 			double centerY = y - r / 2;
 			r = r * alpha;
 			x = centerX - r;
 			y = centerY + r;
 			rect.setRect(x, y, r, r);
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
