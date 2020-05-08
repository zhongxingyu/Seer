 package racing;
 
 import java.util.ArrayList;
 
 import processing.core.PConstants;
 import graphic.SpriteBank;
 import handleds.Collidable;
 import handlers.ActorHandler;
 import handlers.DrawableHandler;
 import handlers.KeyListenerHandler;
 import helpAndEnums.DoublePoint;
 import helpAndEnums.HelpMath;
 import drawnobjects.MaskedSpriteObject;
 
 /**
  * Car is the playable car that races around the stages
  *
  * @author Gandalf.
  *         Created 15.6.2013.
  */
 public class Car extends MaskedSpriteObject implements listeners.KeyListener
 {	
 	// ATTRIBUTES	-----------------------------------------------------
 	
 	private double maxdrivespeed, acceleration, turning, maxturning;
 	private double turningfriction, turnrate, brakepower, maxreversespeed;
 	private double slidepower, rotfriction, slideturnmodifier, turbopower; 
 	private double turbospeed;
 	private boolean sliding;
 	
 	
 	// CONSTRUCTOR	-----------------------------------------------------
 	
 	/**
 	 * Creates a new car
 	 *
 	 * @param x The new x-coordinate of the car (ingame pxl)
 	 * @param y The new y-coordinate of the car (ingame pxl)
 	 * @param drawer The drawablehandler that draws the car (optional)
 	 * @param actorhandler The actorhandler that moves the car (optional)
 	 * @param keyhandler The keylistenerhandler that informs the car of the 
 	 * keypresses (optional)
 	 * @param carspritebank The Spritebank that holds the car's sprite
 	 */
 	public Car(int x, int y, DrawableHandler drawer,
 			ActorHandler actorhandler, KeyListenerHandler keyhandler, 
 			SpriteBank carspritebank)
 	{
 		super(x, y, drawer, actorhandler, carspritebank, "test", "testcarmask");
 		
 		// Adds the car to the keyhandler (if possible)
 		if (keyhandler != null)
 			keyhandler.addKeyListener(this);
 		
 		// Initializes attributes
 		this.sliding = false;
 		
 		this.maxdrivespeed = 10;		// How fast the car can drive (> 0)
 		this.turning = 0.01;			// How fast the car changes its direction (> 0)
 		this.acceleration = 0.05;		// How fast the car starts moving (> 0)
 		this.maxturning = 0.4;			// How much the car can turn (> 0)
 		this.turningfriction = 0.03;	// How much turning affects the car's movement (>= 0)
 		this.turnrate = 1.5;			// How much speed is kept while turning (0+)
		this.brakepower = 0.01;			// How effectively the car brakes (>= 0)
 		this.maxreversespeed = 4;		// How fast the car can move backwards (> 0)
 		this.slidepower = 0.7;			// How effective the slide is (0 - 1)
 		this.rotfriction = 0.7;			// How fast the rotation diminishes (>= 0)
 		this.slideturnmodifier = 1;		// How much the slide affects the turning (0 - 1)
 		this.turbopower = 1;			// What is the accelration of the turbos
 		this.turbospeed = 15;			// What is the maximum speed with the turbo
 		
 		// Initializes some stats
 		setMaxRotation(20);				// How much the car can possibly spin (> maxturning)
 		setMaxSpeed(25);				// How fast the car can possibly go (> maxdrivespeed)
 		setFriction(0.05);				// How much speed diminishes over time
 		setRotationFriction(this.rotfriction);
 	}
 	
 	
 	// IMPLEMENTED METHODS	---------------------------------------------
 
 	@Override
 	public void onKeyDown(int key, int keyCode, boolean coded)
 	{
 		if (coded)
 		{
 			// Turns with left / right arrowkey
 			if (keyCode == PConstants.LEFT)
 			{
 				//System.out.println(calculateTurning());
 				turn(calculateTurning());
 			}
 			else if (keyCode == PConstants.RIGHT)
 				turn(-calculateTurning());
 			
 			// Goes forward with up arrowkey
 			else if (keyCode == PConstants.UP)
 			{
 				addCheckedBoost(getAngle(), getFriction() + this.acceleration, 
 						this.maxdrivespeed);
 			}
 			// Goes backwards with bottomkey
 			else if (keyCode == PConstants.DOWN)
 				addCheckedBoost(HelpMath.checkDirection(getAngle() + 180), 
 						getFriction() + this.brakepower, this.maxreversespeed);
 		}
 		else
 		{
 			// If C was pressed, turbos
 			if (key == 'c')
 			{
 				addCheckedBoost(getAngle(), this.turbopower, this.turbospeed);
 			}
 		}
 	}
 
 	@Override
 	public void onKeyPressed(int key, int keyCode, boolean coded)
 	{
 		if (!coded)
 		{
 			// Adds turbo if C was pressed
 			/*
 			if (key == 'c')
 				addTurboBoost(10);
 			// Slides around if X was pressed
 			else */
 			if (key == 'x')
 			{
 				//System.out.println("Sliding!");
 				this.sliding = true;
 				// Also negates some of the rotationfriction
 				setRotationFriction(this.rotfriction * (1 - this.slidepower));
 			}
 		}
 	}
 
 	@Override
 	public void onKeyReleased(int key, int keyCode, boolean coded)
 	{
 		if (!coded)
 		{
 			// If V was released, sstops sliding
 			if (key == 'x')
 			{
 				this.sliding = false;
 				setRotationFriction(this.rotfriction);
 			}
 		}
 	}
 	
 	@Override
 	public void act()
 	{
 		super.act();
 		
 		// Also implies the turning friction
 		applyTurningFriction();
 		// And the turnboost too
 		addTurnBoost();
 		//System.out.println(getDirection());
 	}
 	
 	@Override
 	public void onCollision(ArrayList<DoublePoint> collisionpoints, Collidable collided)
 	{
 		// Does nothing
 	}
 	
 	
 	// OTHER METHODS	--------------------------------------------------
 	
 	private void addCheckedBoost(double direction, double force, double maxspeed)
 	{
 		// TODO: Make boost dependent on the friction?
 		
 		// Remembers the last speed
 		double lastspeed = getSpeed();
 		// Adds the boost
 		addMotion(direction, force);
 		// Checks if the car is going too fast and does the necessary repairs
 		if (getSpeed() > maxspeed)
 		{
 			// If the car was already going too fast, boost only affects direction
			if (lastspeed < getSpeed() && lastspeed > maxspeed)
 				setSpeed(lastspeed);
 			// Otherwise, caps the speed to the max
 			else
 				setSpeed(maxspeed);
 		}
 	}
 	
 	/*
 	private void addTurboBoost(double amount)
 	{
 		// Adds the boost
 		addMotion(getAngle(), amount);
 	}
 	*/
 	
 	private void turn(double amount)
 	{	
 		// Remembers the last rotation
 		double lastrotation = getRotation();
 		// Adds the turn
 		addRotation(amount);
 		
 		double maxturn = Math.abs(this.maxturning * getSpeed());
 		
 		// Sliding affects the maxrotation (makes turning easier)
 		if (this.sliding)
 			maxturn *= 1  + this.slidepower * this.slideturnmodifier;
 		
 		// Checks if the car is rotating too fast and does the necessary repairs
 		if (Math.abs(getRotation()) > maxturn)
 		{
 			// If the car was already going too fast, doens't increase the turning
 			if (Math.abs(lastrotation) > maxturn)
 				setRotation(lastrotation);
 			// Otherwise, caps the speed to the max
 			else if (getRotation() > 0)
 				setRotation(this.maxturning * getSpeed());
 			else
 				setRotation(-this.maxturning * getSpeed());
 		}
 	}
 	
 	private void applyTurningFriction()
 	{
 		// Sliding affects turningfriction
 		double modifier = 1;
 		
 		if (this.sliding)
 			modifier = 1 - this.slidepower;
 		
 		diminishSpeed(modifier * getTurningFriction());
 	}
 	
 	private double getAngleDifference180()
 	{
 		double angledifference = Math.abs(getAngle() - getDirection());
 		
 		// > 180 = < 180
 		if (angledifference > 180)
 			angledifference = 360 - angledifference;
 		
 		return angledifference;
 	}
 	
 	private double getAngleDifference90()
 	{
 		double angledifference = getAngleDifference180();
 		
 		// > 90 < 90
 		if (angledifference > 90)
 			angledifference = 180 - angledifference;
 		
 		return angledifference;
 	}
 	
 	// Makes the car's direction change when the car is turned
 	private void addTurnBoost()
 	{
 		// Calculates the turnboost (a certain amount out of turningfriction)
 		double turnboost = getTurningFriction();
 		turnboost *= this.turnrate;
 		
 		// The larger the angledifference (up to 90) the smalller the turnboost
 		turnboost *= HelpMath.getDirectionalForce(getDirection(), 1, getAngle());
 		
 		/*
 		// If the car is driving backwards, the boost is reversed
 		if (getAngleDifference180() > 90)
 			turnboost *= -1;
 		*/
 		
 		// High slidingpower diminishes turnboost
 		if (this.sliding)
 			turnboost *= 1 - this.slidepower;
 	
 		addMotion(getAngle(), turnboost);
 	}
 	
 	private double calculateTurning()
 	{
 		return getRotationFriction() + this.turning * getSpeed();
 	}
 	
 	private double getTurningFriction()
 	{
 		// Reduces the object's speed depending on how much the object is turning
 		double angledifference = getAngleDifference90();
 		// If there's no difference or no speed, doesn't imply friction
 		if (angledifference < 1 || getSpeed() == 0)
 			return 0;
 		
 		return this.turningfriction * Math.log(angledifference);
 	}
 }
