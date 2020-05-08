 /**
  *  @author max <maxdeliso@gmail.com>
  */
 
 package game;
 
 import java.awt.Dimension;
 import java.awt.Graphics2D;
 import java.awt.Point;
 import java.awt.geom.AffineTransform;
 import java.awt.geom.Area;
 import java.awt.geom.GeneralPath;
 import java.awt.geom.PathIterator;
 
 public abstract class GameObject {
 	protected Area objectArea;
 	protected double x, y;
 	protected double vel;
 	protected double accel;
 	protected double heading;
 	protected double headingDelta;
 	protected boolean isValid;
 
 	public GameObject( Area objectArea )
 	{
 		this( objectArea,0,0);
 	}
 
 	public GameObject( Area objectArea, int x, int y )
 	{
 		this( objectArea,x,y,0,0);
 	}
 
 	public GameObject( Area objectArea, Point p )
 	{
 		this( objectArea,p.x,p.y,0,0);
 	}
 
 	public GameObject( Area objectArea, int x, int y, double vel )
 	{
 		this( objectArea,x,y,vel,0.0);
 	}
 
 	public GameObject( Area objectArea, int x, int y, double vel, double accel )
 	{
 		this( objectArea,x,y,vel,accel,0.0);
 	}
 
 	public GameObject( Area objectArea, int x, int y, double vel, double accel, double heading )
 	{
 		this( objectArea,x,y,vel,accel,heading,0.0);
 	}
 
 	public GameObject( Area objectArea, int x, int y, double vel, double accel, double heading, double headingDelta )
 	{
 		this.objectArea = objectArea;
 		this.x = x;
 		this.y = y;
 		this.vel = vel;
 		this.accel = accel;
 		this.heading = heading;
 		this.headingDelta = headingDelta;
 		isValid = true;

 	}
 	public final Area getArea()
 	{
 		return objectArea;
 	}
 
 	public Point getPosition()
 	{
 		return new Point((int)x,(int)y);
 	}
 
 	/**
 	 * This function performs collision detection between two different game objects.
 	 * The detection is only guaranteed to work if both objects are convex.
 	 * @param gameObject the object to compare this one to
 	 * @return whether the two objects are touching
 	 */
 	public boolean isTouching(GameObject gameObject) {
 		Point otherPosition = gameObject.getPosition();
 		Area thisModifiedArea = (Area) objectArea.clone();
 		Area otherArea = gameObject.getArea();
 		PathIterator i;
 		double currentCoordinates[] = new double[6];
 		boolean collisionDetected = false;
 		GeneralPath otherPath = new GeneralPath( otherArea );
 
 		/* translate a copy of the current area to the correct position */
 		thisModifiedArea.transform( AffineTransform.getTranslateInstance(x,y));
 
 		/* create path iterator and translate it to the correct position */
 		i = otherPath.getPathIterator(
 				AffineTransform.getTranslateInstance(otherPosition.x, otherPosition.y));
 
 		collisionDetected = false;
 		while( !i.isDone() && !collisionDetected)
 		{
 			i.currentSegment(currentCoordinates);
 
 			if (thisModifiedArea.contains(currentCoordinates[0],currentCoordinates[1]))
 			{

 				collisionDetected = true;
 			}
 
 			i.next();
 		}
 
 		return collisionDetected;
 	}
 
 	public void updatePosition() {
 		heading += headingDelta;
 		vel += accel;
 		x += vel * Math.cos( heading );
 		y += vel * Math.sin( heading );
 
 		objectArea.transform( 	
 				AffineTransform.getRotateInstance(
 						headingDelta, 
 						objectArea.getBounds().width/2, 
 						objectArea.getBounds().height/2));
 	}
 
 	public abstract void drawObject( Graphics2D g );
 	public abstract void update( Dimension screenDimension );
 
 }
