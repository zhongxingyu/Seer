 package com.team14;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Animation;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
 import java.util.BitSet;
 import java.util.Timer;
 import java.util.TimerTask;
 
 /**
  *  Razorback.java
  * 
  * Create an instance with getInstance() instead of constructor.
  * Using singleton only as an excuse for another JUnit test.
  */
 public class Razorback
 {
 	private World world;
     private Body body;
 
     /* Razorback sprite animation variables */
     private Animation walkAnimation;
     private Animation jumpAnimation;
     private Animation dashAnimation;
     private Animation deathAnimation;
     private Texture walkSheet;
     private Texture dashSheet;
     private TextureRegion currentFrame;
     private float stateTime;
 
 	/* Razorback states */
     private BitSet state; 
 
 	public static final int RUNNING = 0;
 	public static final int JUMP = 1;
 	public static final int DOUBLEJUMP = 2;
 	public static final int DASH = 3;
 	public static final int DEAD = 4;
 
    public static final float normalXVelocity = 8.0f;
     public static final float dashXVelocity = 13.0f;
 	public static final float PIXELS_PER_METER = 60.0f;
 
     private Timer dashTimer;
 
 	protected Razorback(World w)
     {
         super();
 
         /**
          * Load up the texture sheets, create sprites from it.
          */
         walkSheet = new Texture(Gdx.files.internal("assets/animation_sheet.png"));
         walkAnimation = new Animation(0.075f, //25f,
                 new TextureRegion(walkSheet, 0, 0, 69, 56),
                 new TextureRegion(walkSheet, 70, 0, 69, 56),
                 new TextureRegion(walkSheet, 139, 0, 69, 56),
                 new TextureRegion(walkSheet, 208, 0, 69, 56),
                 new TextureRegion(walkSheet, 277, 0, 65, 56));
         jumpAnimation = new Animation(0.075f, //25f,
                 new TextureRegion(walkSheet, 0, 0, 69, 56),
                 new TextureRegion(walkSheet, 70, 0, 69, 56),
                 new TextureRegion(walkSheet, 139, 0, 69, 56),
                 new TextureRegion(walkSheet, 208, 0, 69, 56),
                 new TextureRegion(walkSheet, 277, 0, 65, 56));
         dashSheet = new Texture(Gdx.files.internal("assets/dash_sheet.png"));
         dashAnimation = new Animation(0.150f, //25f,
                 new TextureRegion(dashSheet, 0, 0, 60, 70),
                 new TextureRegion(dashSheet, 60, 0, 68, 70),
                 new TextureRegion(dashSheet, 128, 0, 68, 70),
                 new TextureRegion(dashSheet, 196, 0, 70, 72));
         deathAnimation = new Animation(0.075f, //25f,
                 new TextureRegion(walkSheet, 0, 0, 69, 56),
                 new TextureRegion(walkSheet, 70, 0, 138, 56),
                 new TextureRegion(walkSheet, 139, 0, 207, 56),
                 new TextureRegion(walkSheet, 208, 0, 276, 56),
                 new TextureRegion(walkSheet, 277, 0, 341, 56));
         stateTime = 0f;
         
         /* Now initialize */ 
         BodyDef bodyDef = new BodyDef();
         bodyDef.type = BodyDef.BodyType.DynamicBody;
         
         /* Default start position */
         bodyDef.position.set(1.0f, 7.0f);
 
         body = w.createBody(bodyDef);
 
         /**
          * Boxes are defined by their "half width" and "half height", hence the 2 multiplier.
          */
         PolygonShape shape = new PolygonShape();
         shape.setAsBox(69 / (2 * PIXELS_PER_METER), 56 / (2 * PIXELS_PER_METER));
 
         /**
          * The character should not ever spin around on impact.
          */
         body.setFixedRotation(true);
 
 		/**
 		 * The density and friction of the jumper were found experimentally.
 		 * Play with the numbers and watch how the character moves faster or
 		 * slower.
 		 */
 		FixtureDef fixtureDef = new FixtureDef();
 		fixtureDef.shape = shape;
 		fixtureDef.density = 0.8f;
 		fixtureDef.friction = 0.0f; // always moving on ground
 
 		body.createFixture(fixtureDef);
 		shape.dispose();
 
 		world = w;
 		state = new BitSet();
 		state.set(RUNNING);
 		
         dashTimer = new Timer();
         /* Set default start velocity */
         setXVelocity(normalXVelocity);
 	}
 
 
 	public void move(SpriteBatch spriteBatch)
 	{
     	if ((grounded()) && (!state.get(DASH)))
         {
             state.set(RUNNING);
             state.set(JUMP, false);
             state.set(DOUBLEJUMP, false);
         }
 
 //        if (grounded())
 //        {
 //            state.set(RUNNING);
 //            System.out.println("Grounded");
 //        }
 //        else
 //        	System.out.println("Not Grounded");
         stateTime += Gdx.graphics.getDeltaTime();
 
         /* Determine animation based on Razorback state */        
         if (state.get(DASH))
         {
     		currentFrame = dashAnimation.getKeyFrame(stateTime, true);
         }
         else if ((state.get(JUMP)) || (state.get(DOUBLEJUMP)))
         {
             currentFrame = walkAnimation.getKeyFrame(stateTime, true);
         }
         else if (state.get(RUNNING))
         {
             currentFrame = walkAnimation.getKeyFrame(stateTime, true);        	
         }
         else if (state.get(DEAD))
         {
     		currentFrame = deathAnimation.getKeyFrame(stateTime, true);
         }
         spriteBatch.draw(currentFrame, PIXELS_PER_METER * body.getPosition().x	- 69 / 2,
         		PIXELS_PER_METER * body.getPosition().y - 56 / 2);
     }
 	
 	/**
 	 * jump(): Attempts to perform a jump or double jump.
 	 */
     public boolean jump()
     {
     	boolean didJump = false;
         
     	/** 
     	 * Check if we've just landed
     	 * Our grounding mechanism is based on  
     	 */
 //		THIS IS CURRENTLY IN move(), STILL NOT PERFECTED.
 //    	if ((grounded()) && (!state.get(DASH)))
 //        {
 //            state.set(RUNNING);
 //            state.set(JUMP, false);
 //            state.set(DOUBLEJUMP, false);
 //        }
 
     	System.out.println("JUUUUUUUUUUMP!");
     	System.out.println("Before: " + state.toString());
     	/* Handle the cases for each state */
     	if (state.get(DOUBLEJUMP))
     	{
     		// Do nothing - we aren't allowed to jump again.
     	}
     	else if (state.get(JUMP))
     	{
     		if (state.get(DASH))
     			endDash();
     		state.set(DOUBLEJUMP);
     		setYVelocity(7.0f);
     		didJump = true;
     	}
     	else if (state.get(DASH))
     	{
     		System.out.println("!!! Jumping in dash mode");
     		if (!state.get(DOUBLEJUMP))
     		{
     			endDash();
     			state.set(DOUBLEJUMP);
     			setYVelocity(7.0f);
     			didJump = true;
     		}
     	}
     	else if (state.get(RUNNING))
         {
         	state.set(RUNNING, false);
         	state.set(JUMP);
     		setYVelocity(7.0f);
         	didJump = true;
         }
     	System.out.println("After: " + state.toString());
         return didJump;
     }
 
     /**
      * dash(): Attempts to perform a dash. A dash is 0.5 seconds long.
      *         Ensure that we're not already dashing.
      *         TODO: Method to return dash time left?
      */
     public boolean dash()
     {
     	boolean didDash = false;
     	
         if (!state.get(DASH))
         {
         	setXVelocity(dashXVelocity);
         	setYVelocity(0.0f);
         	world.setGravity(new Vector2(0.0f, 0.0f));
         	state.set(DASH);
         	dashTimer = new Timer();
         	dashTimer.schedule(new DashTimerTask(), 1000);
         	didDash = true;
         }
         return didDash;
     }
 
     /**
      * endDash(): called when dash timer is up or we wish to jump out of a dash
      */
     public void endDash()
     {
     	world.setGravity(new Vector2(0.0f, -10.0f));
 
		state.set(DASH, false);
 		dashTimer.cancel();
     }
 
     /**
      * grounded(): Returns true if y velocity is below and unchanging below
      *             an accepted threshold.
      */
     public boolean grounded()
     {
         if (Math.abs(body.getLinearVelocity().y) < 1e-3)
             return true;
         else
             return false;
     }
 
     /**
      * Methods to get and set X and Y velocity
      */
     public float getXVelocity()
     {
     	return body.getLinearVelocity().x;
     }
 
     public float getYVelocity()
     {
     	return body.getLinearVelocity().y;
     }
 
     public void setXVelocity(float xvel)
     {
     	body.setLinearVelocity(new Vector2(xvel, /*0.0f */getYVelocity()));
     }
 
     public void setYVelocity(float yvel)
     {
     	body.setLinearVelocity(new Vector2 (getXVelocity(), yvel));
     }
     
     /**
      * Methods to get and set X and Y position
      */
     public float getXPosition()
     {
     	return body.getPosition().x;
     }
 
     public float getYPosition()
     {
     	return body.getPosition().y;
     }
   
 	public BitSet getState()
 	{
 		return state;
 	}
 	
 	class DashTimerTask extends TimerTask
 	{
 		public void run()
 		{
 			endDash();
 		}
 	}
 }
