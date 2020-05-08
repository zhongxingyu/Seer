 package model;
 
 import drawnobjects.DrawnObject3D;
 import drawnobjects.PhysicObject3D;
 import listeners.CollisionListener;
 import listeners.KeyListener;
 import motivaatiovalaspeli.HelpMath;
 import motivaatiovalaspeli.MotivaatiovalasPeli;
 import processing.core.PConstants;
 import saito.objloader.OBJModel;
 import scrolling.Scrollable;
 
 /**
  * Valas is the playable character of MotivaatiovalasPeli. Valas can be controlled 
  * using the keyboard (or mouse?) and is drawn as a 3D object.
  *
  * @author Gandalf.
  *         Created 2.12.2012.
  */
 public class Valas extends PhysicObject3D implements KeyListener, Scrollable, CollisionListener
 {
 	// ATTRIBUTES	------------------------------------------------------
 	
 	// TODO: Add speed / motion
 	private int tillMovement, movementInterval, minX, maxX, minY, maxY;
 	private double movementForce, maxSPeed;
 	private OBJModel model;
 	
 	
 	// CONSTRUCTOR	------------------------------------------------------
 
 	/**
 	 * Creates a new valas to the given coordinates with the given name. 
 	 * Valas also needs some information for its movement. Valas moves in 
 	 * bursts with the given interval and force. Valas also collides with borders 
 	 * of the 'screen' or area of play that is given with maxX and maxY. The border's 
 	 * left top is at (0, 0) at default.
 	 *
 	 * @param x Object's new position's x-coordinate in game world (Pxl)
 	 * @param y Object's new position's y-coordinate in game world (Pxl)
 	 * @param z Object's new position's z-coordinate in game world (Pxl)
 	 * @param maxX How large the position's x-coordinate can become before the valas "collides" 
 	 * with the wall and bounces back (Pxl)
 	 * @param maxY How large the position's x-coordinate can become before the valas "collides" 
 	 * with the wall and bounces back (Pxl)
 	 * @param movementInterval How often is the valas moved (steps)
 	 * @param movementForce How much accelration is added to the object at each movement
 	 * @param maxSpeed How fast can the object move
 	 * @param parent The applet that will draw the valas
 	 */
 	public Valas(int x, int y, int z, int maxX, int maxY, int movementInterval,
 			double movementForce, double maxSpeed, MotivaatiovalasPeli parent)
 	{
 		super(x, y, z);
 		
		this.model = new OBJModel(parent, "valas1.obj", "relative", 
 				PConstants.POLYGON);
 		
 		setRotationFriction(1);
 		setFriction(0.15);
 		this.movementForce = movementForce;
 		this.maxSPeed = maxSpeed;
 		this.maxX = maxX;
 		this.maxY = maxY;
 		this.minX = 0;
 		this.minY = 0;
 		
 		if (movementInterval > 0)
 			this.movementInterval = movementInterval;
 		else
 			this.movementInterval = 1;
 		this.tillMovement = this.movementInterval;
 		
 		// Valas's model is so small that we need to scale it a bit
 		setScale(8, 8, 8);
 	}
 	
 	
 	// IMPLEMENTED METHODS	---------------------------------------------
 
 	@Override
 	public void drawSelf3D(MotivaatiovalasPeli applet)
 	{
 	    this.model.draw();
 	    
 		// TODO Add cool valas models
 		// Changes origin
 		//applet.translate(-16, -16, 0);
 		// Changes colour
 	    /*
 		applet.fill(255, 0, 0);
 		applet.stroke(0);
 		// Draws
 		applet.box(32, 32, 64);
 		// Resets
 		applet.noFill();
 		applet.noStroke();
 		*/
 	}
 
 	@Override
 	public boolean pointCollides(int x, int y, int z)
 	{
 		// TODO Think of somehting here...
 		return false;
 	}
 
 	@Override
 	public void onKeyDown(int key, int keyCode, boolean coded)
 	{	
 		// TODO Add controlls
 		// Case up
 		if (coded && keyCode == PConstants.UP)
 			setRotation(-5, getYRotation(), getZRotation());
 		// Case down
 		else if (coded && keyCode == PConstants.DOWN)
 			setRotation(5, getYRotation(), getZRotation());
 		// Case left
 		else if (coded && keyCode == PConstants.LEFT)
 			setRotation(getXRotation(), 5, getZRotation());
 		// Case Right
 		else if (coded && keyCode == PConstants.RIGHT)
 			setRotation(getXRotation(), -5, getZRotation());
 	}
 
 	@Override
 	public void onKeyPressed(int key, int keyCode, boolean coded)
 	{
 		// Does nothing yet
 	}
 
 	@Override
 	public void onKeyReleased(int key, int keyCode, boolean coded)
 	{
 		// Does nothing yet
 	}
 
 	@Override
 	public int getOriginX()
 	{
 		return 16;
 	}
 
 	@Override
 	public int getOriginY()
 	{
 		return 16;
 	}
 
 	@Override
 	public int getOriginZ()
 	{
 		return 0;
 	}
 	
 	@Override
 	public void act()
 	{
 		//System.out.println(getY() - getCollisionY());
 		
 		//System.out.println(HelpMath.checkDirection(HelpMath.pointDirection((int) getX(), (int) getY(), 
 		//		this.maxX/2, this.maxY/2)));
 		//System.out.println(HelpMath.pointDistance((int) getX(), (int) getY(), this.maxX/2, this.maxY/2));
 		
 		// Adds valas' own movement if needed and checks the angles
 		checkAngle(50);
 		
 		this.tillMovement --;
 		if (this.tillMovement == 0)
 		{
 			this.tillMovement = this.movementInterval;
 			addMotion3D((int) getXAngle(), -(int) getYAngle() -90, this.movementForce);
 			
 			//System.out.println(getZDirection());
 			
 			if (getSpeed() > this.maxSPeed)
 			{
 				//System.out.println("Slows");
 				setSpeed3D(10, false);
 			}
 			
 			// CHecks if the valas is colliding with the borders
 			checkBorders();
 			
 			//setMotion3D((int) getXAngle(), (int) -getYAngle() - 90, this.movementForce);
 			
 			//System.out.println(getVspeed());
 			//System.out.println(getXAngle());
 			//System.out.println(getYAngle());
 			//System.out.println("Speed: " + getHspeed() + ", " + getVspeed() + ", " + getZspeed());
 			//System.out.println("Direction " + getXAngle() + ", " + getYAngle() + ", " + getZAngle());
 		}
 		
 		super.act();
 	}
 	
 	@Override
 	public void setZ(double z)
 	{
 		setPosition(getX(), getY(), z);
 	}
 
 
 	@Override
 	public int getMaxZ()
 	{
 		return 5;
 	}
 
 
 	@Override
 	public int getMinZ()
 	{
 		return -5;
 	}
 
 
 	@Override
 	public boolean isReturned()
 	{
 		return false;
 	}
 
 
 	@Override
 	public void onOutOfRange()
 	{
 		// Does nothing
 	}
 	
 	@Override
 	public int getCollisionX()
 	{
 		return (int) getX() + getOriginX() 
 				+ (int) HelpMath.lendirX(32, (int) getYAngle() + 90);
 	}
 
 
 	@Override
 	public int getCollisionY()
 	{
 		return (int) getY() +  getOriginY() 
 				- (int) HelpMath.lendirY(32, (int) -getXAngle());
 	}
 
 
 	@Override
 	public int getCollisionZ()
 	{
 		return (int) getZ() + getOriginZ() 
 				- (int) HelpMath.lendirX(32, (int) getXAngle());
 	}
 
 
 	@Override
 	public void onCollision(DrawnObject3D collidedObject)
 	{
 		if (!isActive())
 			return;
 		
 			//setSpeed3D(-5, false);
 			//setSpeed3D(-getSpeed(), false);
 		//System.out.println(getSpeed());
 		
 		int ydir = HelpMath.PointYDirection((int) collidedObject.getX(), 
 				(int) collidedObject.getZ(), (int) getX(), (int) getZ());
 		int zdir = HelpMath.PointZDirection((int) collidedObject.getX(), 
 				(int) collidedObject.getY(), (int) getX(), (int) getY());
 		double hspeed = HelpMath.lendirX(4, ydir);
 		double vspeed = HelpMath.lendirY(4, zdir);
 		double zspeed = HelpMath.lendirY(4, ydir);
 		setVelocity(hspeed, vspeed, zspeed);
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	// Returns the x and y angles to a area
 	private void checkAngle(int maxAngle)
 	{
 		if (getXAngle() > 180 && getXAngle() < 360 - maxAngle)
 			setAngle(-50, getYAngle(), getZAngle());
 		
 		if (getXAngle() > maxAngle && getXAngle() < 180)
 			setAngle(50, getYAngle(), getZAngle());
 		
 		if (getYAngle() > maxAngle && getYAngle() < 180)
 			setAngle(getXAngle(), 50, getZAngle());
 		
 		if (getYAngle() < 310 && getYAngle() > 180)
 			setAngle(getXAngle(), -50, getZAngle());
 	}
 	
 	// Checks if the valas has gone too far on the x or y axis and returns it back
 	private void checkBorders()
 	{
 		if (getX() - 35 < this.minX)
 			setVelocity(Math.abs(getHspeed())*0.75, getVspeed(), getZspeed());
 		else if (getX() + 32 > this.maxX)
 			setVelocity(-Math.abs(getHspeed())*0.75, getVspeed(), getZspeed());
 		
 		if (getY() - 35 < this.minY)
 			setVelocity(getHspeed(), Math.abs(getVspeed())*0.75, getZspeed());
 		if (getY() + 35 > this.maxY)
 			setVelocity(getHspeed(), -Math.abs(getVspeed())*0.75, getZspeed());
 	}
 }
