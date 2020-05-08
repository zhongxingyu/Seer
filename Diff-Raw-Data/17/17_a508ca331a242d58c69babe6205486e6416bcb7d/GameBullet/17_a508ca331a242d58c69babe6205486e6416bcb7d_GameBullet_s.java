 /**
  *  @author max <maxdeliso@gmail.com>
  */
 
 package game;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.Polygon;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 
 public class GameBullet extends GameObject implements GamePhysicsInterface {
 	
 	public static int BULLET_XPOINTS[] = 
		{0, 0, 1, 1 }; 
 	public static int BULLET_YPOINTS[] = 
		{0, 1, 0, 1 };
	public static final int BULLET_VEL = 4;
 
 	public GameBullet(Point start, double vel, double heading) {
 		super(new Area(new Polygon(BULLET_XPOINTS, BULLET_YPOINTS, 4)), 
 				start.x, start.y, vel, 0.0, heading  );
 	}
 
 	@Override
 	public void drawObject(Graphics2D g) {
 		g.setColor(Color.RED);
 
 		/* store transformation matrix  */
 		AffineTransform at  = g.getTransform();
 	
 		/* translate and draw */
 		g.setTransform( AffineTransform.getTranslateInstance( (double) x, (double)y) );
 		g.draw(objectArea);
 		
 		/* restore transformation matrix */
 		g.setTransform(at);
 	}
 
 	@Override
 	public void update(Dimension frameDimension) {
 		updatePosition();
 		
 		if( x > frameDimension.width )
 			isValid = false;
 		
 		if( x < 0 )
 			isValid = false;
 		
 		if( y > frameDimension.height )
 			isValid = false;
 		
 		if( y < 0 )
 			isValid = false;
 	}
 
 }
