 package edu.ua.cs.pbvs;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.microedition.khronos.opengles.GL10;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.BoundCamera;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
 import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl.IOnScreenControlListener;
 import org.anddev.andengine.engine.camera.hud.controls.DigitalOnScreenControl;
 import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
 import org.anddev.andengine.engine.options.EngineOptions;
 import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
 import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.anddev.andengine.entity.IEntity;
 import org.anddev.andengine.entity.scene.Scene;
 import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
 import org.anddev.andengine.entity.scene.background.ParallaxBackground;
 import org.anddev.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
 import org.anddev.andengine.entity.shape.Shape;
 import org.anddev.andengine.entity.sprite.Sprite;
 import org.anddev.andengine.entity.util.FPSLogger;
 import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
 import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
 import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
 import org.anddev.andengine.extension.physics.box2d.PhysicsConnector;
 import org.anddev.andengine.extension.physics.box2d.PhysicsFactory;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 import org.anddev.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.anddev.andengine.input.touch.TouchEvent;
 import org.anddev.andengine.opengl.texture.Texture;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
 import org.anddev.andengine.sensor.accelerometer.AccelerometerData;
 import org.anddev.andengine.sensor.accelerometer.IAccelerometerListener;
 import org.anddev.andengine.ui.activity.BaseGameActivity;
 import org.anddev.andengine.util.Debug;
 
 import android.hardware.SensorManager;
 import android.widget.Toast;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.Fixture;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 
 public class pbvs extends BaseGameActivity implements IAccelerometerListener, IOnSceneTouchListener {
 
 	// ===========================================================
 	// Constants
 	// ===========================================================
 
 	private static final int CAMERA_WIDTH = 800;
 	private static final int CAMERA_HEIGHT = 480;
 
 	// ===========================================================
 	// Fields
 	// ===========================================================
 
 	private BoundCamera mCamera;
 
 	private ArrayList<PhysicsAnimatedSprite> characters;
 	
 	public Texture scaffoldTexture;  
 	public TextureRegion metalBoxTextureRegion;
 	
 	public Texture block1X4Texture;
 	public TextureRegion block1X4TextureRegion;
 	
 	public Texture block4X1Texture;
 	public TextureRegion block4X1TextureRegion;
 	
 	public Texture endBlockTexture;
 	public TextureRegion endBlockTextureRegion;
 	
 	public Texture scientistTexture;
 	public TiledTextureRegion scientistTextureRegion;
 	
 	public Texture ninjaTexture;
 	public TiledTextureRegion ninjaTextureRegion;
 	
 	public Texture stockBrokerTexture;
 	public TiledTextureRegion stockBrokerTextureRegion;
 	
 	public Texture racerTexture;
 	public TiledTextureRegion racerTextureRegion;
 	
 	public Texture playerTexture;  //I think this is like a texture container.  or something.
 	public TiledTextureRegion mPlayerTextureRegion; // player texture
 	public Texture bulletTexture;
 	public TiledTextureRegion bulletTextureRegion;
 
 	public Texture mAutoParallaxBackgroundTexture;
 
 	public TextureRegion mParallaxLayerBack;  //parallax textures
 	public TextureRegion mParallaxLayerMid;
 	public TextureRegion mParallaxLayerFront;
 	
 	public Texture mOnScreenControlTexture;  //button thing
 	public TextureRegion mOnScreenControlBaseTextureRegion;
 	public TextureRegion mOnScreenControlKnobTextureRegion;
 	
 	public Texture mOnScreenButtonTexture;  //button thing
 	public TextureRegion mOnScreenButtonBaseTextureRegion;
 	public TextureRegion mOnScreenButtonKnobTextureRegion;
 	
 	public PhysicsWorld mPhysicsWorld;
 	
 	
 	public Shape ground;
 	public Shape roof;
 	public Shape left;
 	public Shape right;
 	
 	private Player player;
 	private Enemy ninja;
 	
 	
 	private float mGravityX;
 	private float mGravityY;
 	
 	private int playerX;
 	private int playerY;
 	Scene scene;
 	
 	pbvs THIS = this;
 
 	
 	
 	/*
 	 * Here are the xml attributes.
 	 */
 	
 	/*
 	 * Here are the XML object types
 	 */
 
 	// ===========================================================
 	// Constructors
 	// ===========================================================
 
 	// ===========================================================
 	// Getter & Setter
 	// ===========================================================
 
 	// ===========================================================
 	// Methods for/from SuperClass/Interfaces
 	// ===========================================================
 
 	public Engine onLoadEngine() {
 			this.mCamera = new BoundCamera(0, 30, CAMERA_WIDTH, CAMERA_HEIGHT+30); 
 			final Engine engine = new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
 			 try {
                  if(MultiTouch.isSupported(this)) {
                          engine.setTouchController(new MultiTouchController());
                          if(MultiTouch.isSupportedDistinct(this)) {
                                  //Toast.makeText(this, "MultiTouch detected --> Both controls will work properly!", Toast.LENGTH_SHORT).show();
                          } 
                          else {
                          }
                  } else {
                          Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
                  }
          } catch (final MultiTouchException e) {
                  Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(Falling back to SingleTouch.)\n\nControls are placed at different vertical locations.", Toast.LENGTH_LONG).show();
          }
 
          return engine;
 	}
 
 	public void onLoadResources() {
 			this.characters = new ArrayList<PhysicsAnimatedSprite>();
 	        this.enableAccelerometerSensor(this);	
 			TextureRegionFactory.setAssetBasePath("gfx/");
 			this.prepSpriteTextures();
 			this.prepParaBackground();
 			this.prepControlTextures();
 			this.mEngine.getTextureManager().loadTextures(this.scaffoldTexture, this.playerTexture, this.ninjaTexture,
 					this.mOnScreenControlTexture , this.mAutoParallaxBackgroundTexture, this.mOnScreenButtonTexture, bulletTexture, this.block1X4Texture, this.block4X1Texture );
 	}
 
 	public Scene onLoadScene() {
 			this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, SensorManager.GRAVITY_EARTH), false);
 			mPhysicsWorld.setContactListener(new ContactDetector());
 			
 			this.mEngine.registerUpdateHandler(new FPSLogger());
 			
 
 			scene = new Scene(1);
 			
 			final LevelLoaderWrapper levelLoaderObj = new LevelLoaderWrapper(this, scene);
 			
 			/* Calculate the coordinates for the face(do you mean player?), so its centered on the camera. */
 			playerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getTileWidth()) / 2;
 			playerY = 0;
 			
 			player = new Player(playerX, playerY, this.mPlayerTextureRegion, mPhysicsWorld, this);
 			characters.add(player);
 			
 			scene.getLastChild().attachChild(player);
 			this.mCamera.setChaseEntity(player);
 			
 			
 			
 			final PhysicsHandler controlHandler = new PhysicsHandler(player);
 			player.registerUpdateHandler(controlHandler);  //this is the thing that the controls control.
			//ninja.registerUpdateHandler(controlHandler);
 			
 			
 			final ParallaxBackground paraBack = this.loadManualParallax();
 			scene.setBackground( paraBack );  
 			
 			
 			final DigitalOnScreenControl digitalOnScreenControl = this.loadControl(controlHandler, paraBack, player);
 			//makes and configures the onscreen controls.
 
 			scene.setChildScene(digitalOnScreenControl);
 			//adds them to the scene
 			
 			try {
				levelLoaderObj.loadLevelFromAsset(this, "test2.lvl");
 			} catch (final IOException e) {
 				Debug.e(e);
 			}
 			
 			final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0, 0f, 0.5f);
             PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, wallFixtureDef);
             PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, wallFixtureDef);
             PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, wallFixtureDef);
             PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, wallFixtureDef);
            
             scene.registerUpdateHandler(this.mPhysicsWorld);
             
             //create enemy
             ninja = new Enemy(player.getX()+100, player.getY(), this.ninjaTextureRegion, mPhysicsWorld, this);
             characters.add(ninja);
 			scene.getLastChild().attachChild(ninja);
 			final PhysicsHandler ch = new PhysicsHandler(ninja);
 			ninja.registerUpdateHandler(ch);
             
 			
 			return scene;
 	}
 
 	public void onLoadComplete() {
 
 	}
 
 	// ===========================================================
 	// Methods
 	// ===========================================================
 	
 	private ParallaxBackground loadManualParallax()
 	{
 		final ParallaxBackground ParallaxBackground = new ParallaxBackground(0, 0, 0);
 		ParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerBack.getHeight(), this.mParallaxLayerBack)));
 		ParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f, new Sprite(0, 80, this.mParallaxLayerMid)));
 		ParallaxBackground.attachParallaxEntity(new ParallaxEntity(-10.0f, new Sprite(0, CAMERA_HEIGHT - this.mParallaxLayerFront.getHeight(), this.mParallaxLayerFront)));
 		//I dont know the specifics here.
 		return ParallaxBackground;
 	}
 	
 	private DigitalOnScreenControl loadControl( final PhysicsHandler physicsHandler, final ParallaxBackground paraBack, final Player player)
 	{
 		
 		
 		//TODO I vote we keep this in here until
 
 		final DigitalOnScreenControl digitalOnScreenControl = new DigitalOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
 			float count = 0;
 			int runCount = 0;
 			int dir = 0;
 			boolean run = true;
 			
 			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float controlXVal, final float controlYVal ) {
 				//physicsHandler.setVelocity(controlXVal * 100, controlYVal * 100); //when controls are idle the values = 0
 				
 		        Vector2 playerMove = Vector2Pool.obtain(15*controlXVal,0*controlYVal);
 				if(controlXVal > 0)
 				{
 					if (!(dir > 0))
 					{
 						player.stopAnimation();
 					}
 					if (run)
 					{
 						dir = 1;
 						player.dir = dir;
 						player.animate(new long[]{150, 150, 150}, 3, 5, 1);
 						
 						runCount = 0;
 						
 						run = false;
 					}
 					else
 					{
 						if (runCount > 2)
 						{
 							run = true;
 						}
 						runCount++;
 					}
 					this.count+=1.50f;
 				}
 				else if (controlXVal < 0)
 				{
 					if (!(dir < 0))
 					{
 						player.stopAnimation();
 					}
 					if (run)
 					{
 						dir = -1;
 						player.dir = dir;
 						player.animate(new long[]{150, 150, 150}, 9, 11, 1);
 						runCount = 0;
 						run = false;
 					}
 					else
 					{
 						if (runCount > 2)
 						{
 							dir = 0;
 							run = true;
 						}
 						runCount++;
 					}
 					this.count-=1.50f;
 					/*
 					if(dir<0) {
 						player.dir = -1;
 					}
 					else {
 						player.dir = 1;
 					}
 					*/
 				}
 				else
 				{
 					player.stopAnimation();
 				}
 				//paraBack.setParallaxValue((float)this.count*4);
 				Body playerBody = mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player);
 				playerBody.setLinearVelocity(playerMove);
 			}
 		});
 		final DigitalOnScreenControl rightControl = new DigitalOnScreenControl(CAMERA_WIDTH - (this.mOnScreenButtonBaseTextureRegion.getWidth()+135), CAMERA_HEIGHT - this.mOnScreenButtonBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenButtonBaseTextureRegion, this.mOnScreenButtonKnobTextureRegion, 0.1f, new IOnScreenControlListener() {
 			public void onControlChange(final BaseOnScreenControl pBaseOnScreenControl, final float controlXVal, final float controlYVal ) {
 				if (controlXVal < 0f)
 				{
 					player.jump();
 				}
 				if (player.collidesWith(ground))
 				{
 					player.setJump();
 				}
 				if(controlXVal > 0f) {
 					player.shoot();
 				}
 				
 			}
 		});
 		rightControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		rightControl.getControlBase().setAlpha(0.75f);
 		rightControl.getControlBase().setScaleCenter(0, 48);
 		rightControl.getControlBase().setScale(2.0f);
 		rightControl.getControlKnob().setScale(00f);
 		rightControl.refreshControlKnobPosition();	
 		
 		digitalOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
 		digitalOnScreenControl.getControlBase().setAlpha(0.75f);
 		digitalOnScreenControl.getControlBase().setScaleCenter(0, 48);
 		digitalOnScreenControl.getControlBase().setScale(2.0f);
 		digitalOnScreenControl.getControlKnob().setScale(00f);
 		digitalOnScreenControl.refreshControlKnobPosition();
 		
 		digitalOnScreenControl.setChildScene(rightControl);
 		
 		return digitalOnScreenControl;
 			
 	}
 	
 	private void prepSpriteTextures()
 	{
 		this.playerTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.scientistTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.ninjaTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.stockBrokerTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.racerTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		
 		this.ninjaTexture = new Texture(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.scaffoldTexture = new Texture(32, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.block1X4Texture = new Texture(128, 32, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.block4X1Texture = new Texture(32, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.bulletTexture = new Texture(32, 64, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		
 		//this.block1X4Texture = new Texture(32, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA); //inits the texture
 		this.scientistTextureRegion = TextureRegionFactory.createTiledFromAsset(this.scientistTexture, this, "player_possible.png", 0, 0, 3, 4);
 		this.ninjaTextureRegion = TextureRegionFactory.createTiledFromAsset(this.ninjaTexture, this, "player_possible.png", 0, 0, 3, 4);
 		this.stockBrokerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.stockBrokerTexture, this, "player_possible.png", 0, 0, 3, 4);
 		this.racerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.racerTexture, this, "player_possible.png", 0, 0, 3, 4);
 		
 		this.mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.playerTexture, this, "player_possible.png", 0, 0, 3, 4);
 		this.ninjaTextureRegion = TextureRegionFactory.createTiledFromAsset(this.ninjaTexture, this, "ninja.png", 0, 0, 3, 4);
 		this.metalBoxTextureRegion = TextureRegionFactory.createFromAsset(this.scaffoldTexture, this, "metal_block.png", 0, 0);
 		this.block1X4TextureRegion = TextureRegionFactory.createFromAsset(this.block1X4Texture, this, "block4x1.png", 0, 0);
 		this.block4X1TextureRegion = TextureRegionFactory.createFromAsset(this.block4X1Texture, this, "block1x4.png", 0, 0);
 		this.bulletTextureRegion = TextureRegionFactory.createTiledFromAsset(this.bulletTexture, this, "newbullet.png", 0, 0, 1, 2);
 	}
 	
 	private void prepParaBackground()
 	{
 		this.mAutoParallaxBackgroundTexture = new Texture(1024, 1024, TextureOptions.DEFAULT);  
 		this.mParallaxLayerFront = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_front.png", 0, 0);
 		this.mParallaxLayerBack = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "newparallax_background_layer_back.png", 0, 188);
 		this.mParallaxLayerMid = TextureRegionFactory.createFromAsset(this.mAutoParallaxBackgroundTexture, this, "parallax_background_layer_mid.png", 0, 669);
 	}
 	
 	private void prepControlTextures()
 	{
 		this.mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "digital_control_base.png", 0, 0);
 		this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "onscreen_control_knob.png", 128, 0);
 		
 		this.mOnScreenButtonTexture = new Texture(256, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		this.mOnScreenButtonBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenButtonTexture, this, "control_button.png", 0, 0);
 		this.mOnScreenButtonKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenButtonTexture, this, "onscreen_control_knob.png", 128, 0);
 	}
 	
 	
 	// ===========================================================
 	// Inner and Anonymous Classes
 	// ===========================================================	
 
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	public void onAccelerometerChanged(AccelerometerData pAccelerometerData) {
 		// TODO Auto-generated method stub
 		player.onUpdate(1);
 		 this.mGravityX = pAccelerometerData.getY();
          this.mGravityY = pAccelerometerData.getX();
 
          final Vector2 gravity = Vector2Pool.obtain(this.mGravityX*2, this.mGravityY*2);
          final Vector2 playerGravity = Vector2Pool.obtain(0, SensorManager.GRAVITY_EARTH);
          this.mPhysicsWorld.setGravity(gravity);
          
          
          /*
 		 final Body playerBody = this.mPhysicsWorld.getPhysicsConnectorManager().findBodyByShape(player);
          playerBody.applyLinearImpulse(playerGravity, new Vector2( 0, 0 ));
          */
          
 		 setCharacterGravity(playerGravity);
          Vector2Pool.recycle(gravity);
          Vector2Pool.recycle(playerGravity);
 		
 	}
 	
 	public void setCharacterGravity(Vector2 charGravity)
 	{
 		for (int i = 0; i < characters.size(); i++)
 		{
 			this.setBodyForce(characters.get(i).body, charGravity);
 		}
 	}
 
 	public void setBodyForce( Body temp, Vector2 charGravity)
 	{
 		temp.applyLinearImpulse( charGravity, new Vector2(0,0) );
 	}
 	
 	class ContactDetector implements ContactListener {
 
 		@Override
 		public void beginContact(Contact contact) {
 			try {
 				collision(contact);
 			}
 			catch(NullPointerException e) {
 				return;
 			}
 		}
 
 		@Override
 		public void endContact(Contact contact) {
 			// TODO Auto-generated method stub
 			
 		}
 			
 	}
 	
 	private void collision(Contact contact) throws NullPointerException {
 		Body bodyA = contact.getFixtureA().getBody();
 		Body bodyB = contact.getFixtureB().getBody();
 		Object objA = bodyA.getUserData();
 		Object objB = bodyB.getUserData();
 		PhysicsData dataA = null;
 		PhysicsData dataB = null;
 		PhysicsAnimatedSprite spriteA = null;
 		PhysicsAnimatedSprite spriteB = null;
 		if(objA instanceof PhysicsData) {
 			dataA = (PhysicsData)objA;
 			spriteA = dataA.sprite;
 		}
 		if(objB instanceof PhysicsData) {
 			dataB = (PhysicsData)objB;
 			spriteB = dataB.sprite;
 		}
 		if(spriteA instanceof Bullet) {
 			removePhysicsSprite(spriteA);
 			if(spriteB instanceof PhysicsAnimatedSprite) {
 				spriteB.hit();
 			}
 		}
 		if(spriteB instanceof Bullet) {
 			removePhysicsSprite(spriteB);
 			if(spriteA instanceof PhysicsAnimatedSprite) {
 				spriteA.hit();
 			}
 		}
 	}
 	
 	public void removePhysicsSprite(PhysicsAnimatedSprite sp) {
 		this.runOnUpdateThread(new RemovePhysicsSprite(sp));
 	}
 	
 	public class RemovePhysicsSprite implements Runnable {
 		private PhysicsAnimatedSprite sp;
 		public RemovePhysicsSprite(PhysicsAnimatedSprite sp) {
 			this.sp = sp;
 		}
 		public void run() {
 			try {
 			PhysicsConnector connect = mPhysicsWorld.getPhysicsConnectorManager().findPhysicsConnectorByShape(sp);
 			mPhysicsWorld.unregisterPhysicsConnector(connect);
 			mPhysicsWorld.destroyBody(connect.getBody());
 			scene.getLastChild().detachChild(sp);
 			}
 			catch(NullPointerException e) {
 				return;
 			}
 		}
 	}
 	
 }
