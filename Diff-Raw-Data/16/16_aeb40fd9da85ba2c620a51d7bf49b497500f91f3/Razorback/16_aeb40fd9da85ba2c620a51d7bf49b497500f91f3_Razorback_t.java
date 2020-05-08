 package com.team14;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.World;
 import com.badlogic.gdx.physics.box2d.PolygonShape;
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
 	private static Razorback instance = null;
     private Body body;
     private Sprite sprite;
     private Texture texture;
 
 	// Razorback states
 	private static final int RUNNING = 0;
 	private static final int JUMP = 1;
 	private static final int DOUBLEJUMP = 2;
 	private static final int DEAD = 3;
 	private int state = RUNNING;
     private boolean dash = false;
 	private int lives;
 
     private static final float normalXVelocity = 10.0f;
     private static final float dashXVelocity = 20.0f;
 	public static final float PIXELS_PER_METER = 60.0f;
 
     private Timer jumpTimer = new Timer();
     private Timer dashTimer = new Timer();
 
 	protected Razorback(World world, int livesLeft)
     {
         super();
         lives = livesLeft;
 
         /**
          * Load up the overall texture, create sprite from it.
          */
         if (world!=null){
         texture = new Texture(Gdx.files.internal("assets/razorback.png"));    
         texture.setFilter(TextureFilter.Linear, TextureFilter.Linear);
         sprite = new Sprite(texture, 0, 0, 99, 63);
 
         // Now initialize 
         BodyDef bodyDef = new BodyDef();
         bodyDef.type = BodyDef.BodyType.DynamicBody;
         /* Default start position */
         bodyDef.position.set(1.0f, 7.0f);
 
         body = world.createBody(bodyDef);
 
         /**
          * Boxes are defined by their "half width" and "half height", hence the 2 multiplier.
          */
         PolygonShape shape = new PolygonShape();
         shape.setAsBox(sprite.getWidth() / (2 * PIXELS_PER_METER), sprite.getHeight() / (2 * PIXELS_PER_METER));
 
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
 		fixtureDef.friction = 0.0f; // always moving on ground... // 5.0f;
 
 		body.createFixture(fixtureDef);
 		shape.dispose();
 
 		body.setLinearVelocity(new Vector2(normalXVelocity, 0.0f));
         }
 	}
 
 	/*
 	 * TODO:
 	 * add parameter for map in which we are placed
 	 * add parameters for initial x and y coordinates
 	 * 
 	 * pass these parameters to constructor
 	 */
 	public static Razorback getInstance(World world, int livesLeft)
 	{
 		if (instance == null)
 		{
 			instance = new Razorback(world, livesLeft);
 		}
 		return instance;
 	}
 
 	public void move(SpriteBatch spriteBatch)
 	{
         if (grounded())
             state = RUNNING;
 
 		sprite.setPosition(
 				PIXELS_PER_METER * body.getPosition().x	- sprite.getWidth() / 2,
 				PIXELS_PER_METER * body.getPosition().y - sprite.getHeight() / 2
                 );
 		sprite.draw(spriteBatch);
     }
 	
 	/**
 	 * jump(): Attempts to perform a jump or double jump.
 	 */
     public boolean jump(){
         long now = System.nanoTime();
 //        if ((now - jumpTimer) > 40000000) // 40ms delay, to prevent accidental double jump
         
         {
             switch (state)
             {
                 case RUNNING: 
                 case JUMP:
                     state++;
                    body.applyLinearImpulse(new Vector2(0.0f, 4.0f), body.getWorldCenter());
  //                   jumpTimer = System.nanoTime();
                 	return true;
             
                 default:
                 	return false;
 
             }
         }
 //        return false;
    
     }
 
     /**
      * dash(): Attempts to perform a dash. A dash is 1.5 seconds long.
      *         Ensure that we're not already dashing.
      *         TODO: Method to return dash time left?
      */
     public void dash()
     {
         if (!dash)
         {
         	setXVelocity(dashXVelocity);
         	dash = true;
         	dashTimer = new Timer();
         	dashTimer.schedule(new DashTimerTask(), 1500);
         }
     }
 
     /**
      * grounded(): Returns true if y velocity is below and unchanging below
      *             an accepted threshold.
      */
     public boolean grounded()
     {
         if (Math.abs(body.getLinearVelocity().y) < 1e-9)
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
     	body.setLinearVelocity(new Vector2(xvel, 0.0f /*getYVelocity()*/));
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
   
 	/**
      * Methods related to lives.
      */
     public void loseLife()
 	{
 		lives--;
 	}
 
     public void oneUp()
     {
         lives++;
     }
 
 	public int getLives()
 	{
 		return lives;
 	}
 	
 	class DashTimerTask extends TimerTask
 	{
 		public void run()
 		{
 			setXVelocity(normalXVelocity);
 			dash = false;
 			dashTimer.cancel();
 		}
 	}
 }
