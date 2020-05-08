 package app.flycatfly.object;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.opengl.vbo.VertexBufferObjectManager;
 import java.lang.Math;
 
 import android.util.Log;
 import app.flycatfly.manager.ResourcesManager;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 
 public abstract class Player extends AnimatedSprite
 {
 	// ---------------------------------------------
 	// VARIABLES
 	// ---------------------------------------------
 	
 	private Body body;
 	
 	private boolean canRun = false;
 	
 	private int footContacts = 0;
 	
 	public float speed = 6;
 	
 	public double distance = 0.000;
 	
 	public double resistance = 0.01;
 
	private float initX, initY;
 	
 	// ---------------------------------------------
 	// CONSTRUCTOR
 	// ---------------------------------------------
 	
 	public Player(float pX, float pY, VertexBufferObjectManager vbo, Camera camera, PhysicsWorld physicsWorld)
 	{
 		super(pX, pY, ResourcesManager.getInstance().player_region, vbo);
		initX=pX;
		initY=pY;
 		createPhysics(camera, physicsWorld);
 		camera.setChaseEntity(this);
 	}
 	
 	// ---------------------------------------------
 	// CLASS LOGIC
 	// ---------------------------------------------
 	
 	private void createPhysics(final Camera camera, PhysicsWorld physicsWorld)
 	{		
 		body = PhysicsFactory.createBoxBody(physicsWorld, this, BodyType.DynamicBody, PhysicsFactory.createFixtureDef(0, 0, 0));
 		body.setUserData("player");
 		body.setFixedRotation(true);
 		
 		physicsWorld.registerPhysicsConnector(new PhysicsConnector(this, body, true, true)
 		{
 			@Override
 	        public void onUpdate(float pSecondsElapsed)
 	        {
 				super.onUpdate(pSecondsElapsed);
 				camera.onUpdate(0.1f);
 				
 				if (canRun)
 				{
 					if (speed > 0.3)
 					{
 						speed -= resistance; // while the character has speed, decrease constantly by resistance
 					}
 					else if (speed > 0)
 					{
 						speed-= resistance;
 						stopAnimation(); //stopping the animation once the character is almost stopped
 					}
 					else
 					{
 						speed = 0; //putting the character to a complete stop
 						onDie();
 					}
 				
 					if (getY() <= 0)
 					{					
 						onDie();
 					}
					calculateDistance();
					Log.d("mine", "distance:"+distance);
 					body.setLinearVelocity(new Vector2(speed, body.getLinearVelocity().y)); 
 				}
 	        }
 		});
 	}
 	
 	
 	public void setRunning()
 	{
 		canRun = true;
 		
 		final long[] PLAYER_ANIMATE = new long[] { 100, 100, 100 };
 		
 		animate(PLAYER_ANIMATE, 0, 2, true);
 	}
 	
 	public void fly(float xDiff, float yDiff)
 	{
 		float c1= (float) Math.sqrt(xDiff*xDiff+yDiff*yDiff);
 		float c2=speed;
 		float ratio = c2/c1;
 		float x2 = xDiff*ratio;
 		float y2 = yDiff*ratio;
 		body.setLinearVelocity(x2, y2);
 	}
 	
 	public void increaseFootContacts()
 	{
 		footContacts++;
 	}
 	
 	public void decreaseFootContacts()
 	{
 		footContacts--;
 	}
 	
	public void calculateDistance(){
		Log.d("mine", "getX:"+getX()+" initX:"+initX);
		distance= (getX()-initX)/10;
	}
	
 	public abstract void onDie();
 }
