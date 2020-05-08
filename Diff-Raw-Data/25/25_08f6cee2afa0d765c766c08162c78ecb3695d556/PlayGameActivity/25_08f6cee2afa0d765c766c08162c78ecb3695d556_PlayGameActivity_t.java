 package edu.asu.tankgame;
 
 import java.util.ArrayList;
 
 import org.andengine.engine.Engine;
 import org.andengine.engine.FixedStepEngine;
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.WakeLockOptions;
 import org.andengine.engine.options.resolutionpolicy.FixedResolutionPolicy;
 
 import org.andengine.entity.Entity;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.AnimatedSprite;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.extension.physics.box2d.FixedStepPhysicsWorld;
 import org.andengine.extension.physics.box2d.PhysicsConnector;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.util.Vector2Pool;
 import org.andengine.input.sensor.acceleration.AccelerationData;
 import org.andengine.input.sensor.acceleration.IAccelerationListener;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.BaseGameActivity;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.Contact;
 import com.badlogic.gdx.physics.box2d.ContactImpulse;
 import com.badlogic.gdx.physics.box2d.ContactListener;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 import com.badlogic.gdx.physics.box2d.Manifold;
 import com.badlogic.gdx.physics.box2d.joints.WeldJointDef;
 
 import android.hardware.SensorManager;
 import android.util.DisplayMetrics;
 import android.util.Log;
 
 public class PlayGameActivity extends BaseGameActivity implements IAccelerationListener, IOnSceneTouchListener {
 	
 	private Camera mCamera;
 	private Scene mScene;
 	public FixedStepPhysicsWorld mPhysicsWorld;
 	public Body groundWallBody;
 	public Body roofWallBody;
 	public Body leftWallBody;
 	public Body rightWallBody;
 	private int Height;
 	private int Width;
 	public Sprite[][] mLevelSprites;
 	public Body [][] mLevelBody;
 	public Sprite[][] mPlayerSprites;
 	public Body [] mPlayerBody;
 	public AnimatedSprite mExplosion;
 	
 	public Sprite [] PowerBar;
 	public Sprite [] AngleBar;
 	public Sprite fireButton;
 	
 	public boolean isPowerTouch;
 	public boolean isAngleTouch;
 	public boolean isFireTouch;
 	public float lastTouchX;
 	public float lastTouchY;
 	
 	public Sprite shellSprite;
 	public Body shellBody;
 	
 	public ArrayList <Sprite> SpritesToDetach;
 	public ArrayList <Body> BodiesToDestroy;
 	
 	@Override
 	public Engine onCreateEngine(final EngineOptions pEngineOptions)
 	{
 		return new FixedStepEngine(pEngineOptions, 60);
 	}
 	
 	@Override
 	public EngineOptions onCreateEngineOptions()
 	{
 		DisplayMetrics dm = new DisplayMetrics();
 		getWindowManager().getDefaultDisplay().getMetrics(dm);
 
 		Height = dm.heightPixels;
 		Width = dm.widthPixels;	
 		// Define our mCamera object
 		mCamera = new Camera(0,0, Width, Height);
 		
 		EngineOptions engineOptions = new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED, new FixedResolutionPolicy(Width, Height), mCamera);		
 		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
 
 		engineOptions.getAudioOptions().setNeedsMusic(true);
 		engineOptions.getAudioOptions().setNeedsSound(true); // *** Mike testing
 		
 		isAngleTouch = false;
 		isPowerTouch = false;
 		isFireTouch = false;
 		return engineOptions;
 	}
 	
 	@Override
 	public void onCreateResources( OnCreateResourcesCallback pOnCreateResourcesCallback)
 	{
 		ResourceManager.getInstance().loadGameTextures(mEngine, this);
 		ResourceManager.getInstance().loadSounds(mEngine, this);
 		
 		GameManager.getInstance().generateLevel(Height, Width);
 		// Notify call back that we've finished.	
 		pOnCreateResourcesCallback.onCreateResourcesFinished();
 	}
 	
 	@Override
 	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
 	{
 		// Create the scene object and background roughly sky colored
 		mScene = new Scene();
 		Background background = new Background(0.45f,0.69f,0.85f,1f);
 		mScene.setBackground(background);
 		mScene.setBackgroundEnabled(true);
 		SpritesToDetach = new ArrayList <Sprite>();
 		BodiesToDestroy = new ArrayList <Body>();
 
 		// parameters are StepsPerSecond, Gravity, AllowSleep, VelocityIterations, PositionIterations)
 		mPhysicsWorld = new FixedStepPhysicsWorld(30, new Vector2(0.0f, SensorManager.GRAVITY_EARTH/4), false, 3, 8);
 		mScene.registerUpdateHandler(mPhysicsWorld); 
 		//SensorManager.GRAVITY_EARTH
 		//parameters are Density, Elasticity, Friction
 		final FixtureDef WALL_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.0f, 0.0f, 0.0f);
 		final FixtureDef TILE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.75f, 0.0f, 1.0f);
 		final Rectangle ground = new Rectangle(0, Height, Width, 1f, this.getVertexBufferObjectManager());
 		final Rectangle roof = new Rectangle(0, -Height, Width, 1f, this.getVertexBufferObjectManager());
 		final Rectangle left = new Rectangle(0, -Height, 1f, Height*2, this.getVertexBufferObjectManager());
 		final Rectangle right = new Rectangle(Width, -Height, 1f, Height*2, this.getVertexBufferObjectManager());
 		ground.setColor(0f,0f,0f);
 		roof.setColor(0f,0f,0f);
 		left.setColor(0f,0f,0f);
 		right.setColor(0f,0f,0f);
 		
 		groundWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		roofWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		leftWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, left, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		rightWallBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, right, BodyType.StaticBody, WALL_FIXTURE_DEF);
 		
 		this.mScene.attachChild(ground);
 		this.mScene.attachChild(roof);
 		this.mScene.attachChild(left);
 		this.mScene.attachChild(right);
 		
 		ITextureRegion temp = null;
 		boolean [][] level = GameManager.getInstance().getLevel();
 		
 		mLevelSprites =  new Sprite[level.length][level[0].length];
 		mLevelBody =  new Body[level.length][level[0].length];
 		GameManager.getInstance();
 		mPlayerSprites = new Sprite[3][GameManager.maxPlayers];
 		mPlayerBody = new Body[GameManager.maxPlayers];
 		PowerBar = new Sprite[3];
 		AngleBar = new Sprite[3];
 				
 		for(int i = 0; i < mLevelSprites.length; i++)
 			for(int j = 0; j < mLevelSprites[i].length; j++)
 			{				
 				if(level[i][j] == true)
 				{
 					temp = querryTile(i,j);
 					if(temp != null)
 					{
 						mLevelSprites[i][j] = new Sprite((i*48),Height - (j*48), temp, mEngine.getVertexBufferObjectManager());
 						mScene.attachChild(mLevelSprites[i][j]);
 						mLevelBody[i][j] = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mLevelSprites[i][j], BodyType.StaticBody, TILE_FIXTURE_DEF);
 						mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mLevelSprites[i][j], mLevelBody[i][j], true, true));
 					}
 				}
 				else
 				{
 					mLevelSprites[i][j] = null;
 					mLevelBody[i][j] = null;
 				}
 			}
 /*
 		for(int i = 1; i < mLevelSprites.length - 1; i++)
 			for(int j = 1; j < mLevelSprites[i].length - 1; j++)
 			{
 				if(mLevelBody[i][j] != null)
 				{
 					if(mLevelBody[i-1][j] != null)
 					{
 						final WeldJointDef weldJointDef = new WeldJointDef();
 						weldJointDef.initialize(mLevelBody[i-1][j] , mLevelBody[i][j], mLevelBody[i][j].getWorldCenter());
 						mPhysicsWorld.createJoint(weldJointDef);
 					}
 					if(mLevelBody[i+1][j] != null)
 					{
 						final WeldJointDef weldJointDef = new WeldJointDef();
 						weldJointDef.initialize(mLevelBody[i+1][j] , mLevelBody[i][j], mLevelBody[i][j].getWorldCenter());
 						mPhysicsWorld.createJoint(weldJointDef);
 					}
 					if(mLevelBody[i][j-1] != null)
 					{
 						final WeldJointDef weldJointDef = new WeldJointDef();
 						weldJointDef.initialize(mLevelBody[i][j-1] , mLevelBody[i][j], mLevelBody[i][j].getWorldCenter());
 						mPhysicsWorld.createJoint(weldJointDef);
 					}		
 					if(mLevelBody[i][j+1] != null)
 					{
 						final WeldJointDef weldJointDef = new WeldJointDef();
 						weldJointDef.initialize(mLevelBody[i][j+1] , mLevelBody[i][j], mLevelBody[i][j].getWorldCenter());
 						mPhysicsWorld.createJoint(weldJointDef);
 					}	
 				}
 			}
 */		
 			for(int i = 0; i < GameManager.maxPlayers; i++)
 			{
 				int pp = GameManager.getInstance().getPlayerPosition(i+1);
 				for(int j = mLevelSprites[pp].length-1; j >= 0; j--)
 					if(mLevelSprites[pp][j] == null)
 					{
 						if(GameManager.getInstance().getPlayerAngle(i+1) <= 90)
 						{
 							mPlayerSprites[0][i] = new Sprite((pp*48),Height - (j*48) + 20, ResourceManager.getInstance().mHaloTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[1][i] = new Sprite(0,0, ResourceManager.getInstance().mTankTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[2][i] = new Sprite(20,4, ResourceManager.getInstance().mBarrelTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[2][i].setRotationCenter(4,4);
 							mPlayerSprites[2][i].setRotation(-GameManager.getInstance().getPlayerAngle(i+1));
 						}
 						else
 						{
 							mPlayerSprites[0][i] = new Sprite((pp*48),Height - (j*48) + 20, ResourceManager.getInstance().mHaloTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[1][i] = new Sprite(0,0, ResourceManager.getInstance().mTankTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[2][i] = new Sprite(0,4, ResourceManager.getInstance().mBarrelTextureRegion, mEngine.getVertexBufferObjectManager());
 							mPlayerSprites[2][i].setRotationCenter(20,4);
 							mPlayerSprites[2][i].setRotation(GameManager.getInstance().getPlayerAngle(i+1)%90);
 							mPlayerSprites[1][i].setFlippedHorizontal(true);
 							mPlayerSprites[2][i].setFlippedHorizontal(true);
 						}
 						if(i == 0)
 							mPlayerSprites[0][0].setColor(0.0f, 1.0f, 0.0f, GameManager.getInstance().getPlayerHealth(i+1)/100f);
 						else if(i == 1)
 							mPlayerSprites[0][1].setColor(1.0f, 0.0f, 0.0f, GameManager.getInstance().getPlayerHealth(i+1)/100f);
 					} 
 				if(mPlayerSprites[1][i] != null && mPlayerSprites[0][i] != null)
 				{
 					mPlayerBody[i] = PhysicsFactory.createBoxBody(this.mPhysicsWorld, mPlayerSprites[0][i], BodyType.DynamicBody, TILE_FIXTURE_DEF);
 					mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(mPlayerSprites[0][i], mPlayerBody[i], true, true));
 					mPlayerSprites[0][i].attachChild(mPlayerSprites[1][i]);
 					mPlayerSprites[0][i].attachChild(mPlayerSprites[2][i]);
 					mScene.attachChild(mPlayerSprites[0][i]);
 					mPlayerSprites[1][i].setZIndex(2);
 					mPlayerSprites[2][i].setZIndex(1);
 					mPlayerSprites[0][i].sortChildren();
 				}
 			}
 			
 		PowerBar[0] = new Sprite(0,Height - 256, ResourceManager.getInstance().mBarBGTextureRegion, mEngine.getVertexBufferObjectManager());
 		PowerBar[1] = new Sprite(0,0, ResourceManager.getInstance().mBarLensTextureRegion, mEngine.getVertexBufferObjectManager());
 		PowerBar[2] = new Sprite(16,16 + 208 * (1 - GameManager.getInstance().getPlayerPower(GameManager.getInstance().getCurrentPlayer())/100), ResourceManager.getInstance().mBarLineTextureRegion, mEngine.getVertexBufferObjectManager());
 		PowerBar[0].setColor(1,1,1,0.5f);
 		PowerBar[0].attachChild(PowerBar[1]);
 		PowerBar[1].attachChild(PowerBar[2]);
 		mScene.attachChild(PowerBar[0]);
 		
 		AngleBar[0] = new Sprite(Width - 162,Height - 162, ResourceManager.getInstance().mBarBGTextureRegion, mEngine.getVertexBufferObjectManager());
 		AngleBar[1] = new Sprite(0,0, ResourceManager.getInstance().mBarLensTextureRegion, mEngine.getVertexBufferObjectManager());
 		AngleBar[2] = new Sprite(16, 16 + 208 * (1 - GameManager.getInstance().getPlayerAngle(GameManager.getInstance().getCurrentPlayer())/180), ResourceManager.getInstance().mBarLineTextureRegion, mEngine.getVertexBufferObjectManager());
 		AngleBar[0].setColor(1,1,1,0.5f);
 		AngleBar[0].setRotationCenter(AngleBar[0].getWidth()/2, AngleBar[0].getHeight()/2);
 		AngleBar[0].setRotation(90);
 		AngleBar[0].attachChild(AngleBar[1]);
 		AngleBar[1].attachChild(AngleBar[2]);
 		mScene.attachChild(AngleBar[0]);
 
 		fireButton = new Sprite((Width/2) - 64,Height - 128, ResourceManager.getInstance().mFireTextureRegion, mEngine.getVertexBufferObjectManager());
 		fireButton.setColor(1,1,1,0.5f);
 		mScene.attachChild(fireButton);
 		
 		// Notify the callback that we've finished creating the scene
 		ResourceManager.getInstance().mMusic.play();
 		mScene.setOnSceneTouchListener(this);
 		pOnCreateSceneCallback.onCreateSceneFinished(mScene);
 	}
 	
 	public ITextureRegion querryTile(int x, int y)
 	{
 		ITextureRegion temp = null;
 		boolean above;
 		boolean left;
 		boolean right;
 		boolean [][] level = GameManager.getInstance().getLevel();
 
 		// Check tile above
 		if(y+1 == level[x].length)
 			above = false;
 		else
 			above = level[x][y+1];
 		// Check tile to the left
 		if(x == 0)
 			left = false;
 		else
 			left = level[x-1][y];
 		// Check tile to the right
 		if(x+1 == level.length)
 			right = false;
 		else
 			right = level[x+1][y];
 		
 		if(above == false && left == true && right == true)
 			temp = ResourceManager.getInstance().mCenterTextureRegion;
 		else if(above == false && left == false && right == true)
 			temp = ResourceManager.getInstance().mLeftCornerTextureRegion;
 		else if(above == false && left == true && right == false)
 			temp = ResourceManager.getInstance().mRightCornerTextureRegion;
 		else if(above == true && left == false && right == true)
 			temp = ResourceManager.getInstance().mLeftTextureRegion;
 		else if(above == true && left == true && right == false)
 			temp = ResourceManager.getInstance().mRightTextureRegion;
 		else if(above == true && left == false && right == false)
 			temp = ResourceManager.getInstance().mPillarTextureRegion;
 		else if(above == true && left == true && right == true)
 			temp = ResourceManager.getInstance().mBlankTextureRegion;
 		else if(above == false && left == false && right == false)
 			temp = ResourceManager.getInstance().mCenterCornerTextureRegion;
 		
 		return temp;
 	}
 	
 	@Override
 	public void onPopulateScene(Scene pScene, OnPopulateSceneCallback pOnPopulateSceneCallback)
 	{
 		// Inform callback we've finished
 		ContactListener contactListener = new ContactListener()
 		{
 			@Override
 			public void beginContact(Contact contact)
 			{
 				if(isBodyContacted(shellBody, contact))
 				{
 					Log.w("Shell" , "impacted");
 					ResourceManager.getInstance().mHitSound.play();
 					// explosion code
 					mExplosion = new AnimatedSprite(shellSprite.getX(), shellSprite.getY(), ResourceManager.getInstance().mExplosionTextureRegion, mEngine.getVertexBufferObjectManager());
 					mExplosion.animate(100, false);
 					mScene.attachChild(mExplosion);
 					BodiesToDestroy.add(shellBody);
 					SpritesToDetach.add(shellSprite);
 					shellBody = null;
 					shellSprite = null;
 				}
 			}
 			
 			@Override
 			public void endContact(Contact contact)
 			{
 
 			}
 			
 			@Override
 			public void preSolve(Contact contact, Manifold oldManifold)
 			{
 				Vector2 impactForce;
 				if(isBodyContacted(mPlayerBody[0], contact))
 				{
 					impactForce = mPlayerBody[0].getLinearVelocity();
 					GameManager.getInstance().damageByImpact(1, impactForce.x, impactForce.y);
 				}
 				else if(isBodyContacted(mPlayerBody[1], contact))
 				{
 					impactForce = mPlayerBody[1].getLinearVelocity();
 					GameManager.getInstance().damageByImpact(2, impactForce.x, impactForce.y);
 				}				
 			}
 			
 			@Override
 			public void postSolve(Contact contact, ContactImpulse impulse)
 			{
 
 			}
 		};
 		mPhysicsWorld.setContactListener(contactListener);
 		mScene.registerUpdateHandler(new IUpdateHandler() {
 			@Override
 			public void onUpdate(float pSecondsElapsed)
 			{
 				mPlayerSprites[0][0].setColor(0.0f, 1.0f, 0.0f, GameManager.getInstance().getPlayerHealth(1)/100f);
 				mPlayerSprites[0][1].setColor(1.0f, 0.0f, 0.0f, GameManager.getInstance().getPlayerHealth(2)/100f);
 				while(!SpritesToDetach.isEmpty())
 				{
 					Log.w("Update", "Delete Sprite " + SpritesToDetach.size());
 					mScene.detachChild(SpritesToDetach.remove(0));
 				}
 				while(!BodiesToDestroy.isEmpty())
 				{
 					Log.w("Update", "Destroy Body " + BodiesToDestroy.size());
 					mPhysicsWorld.destroyBody(BodiesToDestroy.remove(0));			
 				}
 			}
 			
 			@Override
 			public void reset() {};
 		});
 		
 		pOnPopulateSceneCallback.onPopulateSceneFinished();
 	}
 
 	@Override
 	public void onAccelerationAccuracyChanged(AccelerationData pAccelerationData) {
 		
 	}
 
 	@Override
 	public void onAccelerationChanged(AccelerationData pAccelerationData) {
 //		final Vector2 gravity = Vector2Pool.obtain(pAccelerationData.getX(), pAccelerationData.getY());
 //		this.mPhysicsWorld.setGravity(gravity);
 //		Vector2Pool.recycle(gravity);
 	}
 	
 	@Override
 	public void onResumeGame()
 	{
 		super.onResumeGame();
 		this.enableAccelerationSensor(this);
 		ResourceManager.getInstance().mMusic.play();
 	}
 	
 	@Override
 	public void onPauseGame()
 	{
 		super.onPauseGame();
 		ResourceManager.getInstance().mMusic.pause();
 		this.disableAccelerationSensor();
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent)
 	{
 		float tempX = pSceneTouchEvent.getX();
 		float tempY = pSceneTouchEvent.getY();
 //		Log.w("Touch", "X:" + tempX + " Y:" + tempY );
 //		if(pSceneTouchEvent.isActionDown())
 //			Log.w("TouchType", "Down");
 //		else if(pSceneTouchEvent.isActionUp())
 //			Log.w("TouchType", "Up");
 //		else if(pSceneTouchEvent.isActionMove())
 //			Log.w("TouchType", "Move");
 //		else if(pSceneTouchEvent.isActionCancel())
 //			Log.w("TouchType", "Cancel");
 //		else if(pSceneTouchEvent.isActionOutside())
 //			Log.w("TouchType", "Outside");
 		GameManager gm = GameManager.getInstance();
 		
 		//////////////////////////////////////////////////////////////////////////////
 		// Detect WHAT the user is touching (Power Bar, Angle Bar, or Fire Button). //
 		//////////////////////////////////////////////////////////////////////////////
 		if(pSceneTouchEvent.isActionDown())
 		{
 			// PowerBar Touched
 			if(tempX > 0 && tempX < 70 && tempY > Height - 256 && tempY < Height)
 			{
 				isPowerTouch = true;
 				lastTouchX = tempX;
 				lastTouchY = tempY;
 			}
 			// AngleBar Touched
 			if(tempX > Width - 256 && tempX < Width && tempY > Height - 70 && tempY < Height)
 			{
 				isAngleTouch = true;
 				lastTouchX = tempX;
 				lastTouchY = tempY;
 			}
 			fireButton = new Sprite((Width/2) - 64,Height - 128, ResourceManager.getInstance().mFireTextureRegion, mEngine.getVertexBufferObjectManager());
 
 			if(tempX > ((Width/2) - 64) && tempX < ((Width/2) + 64)&& tempY > Height - 128 && tempY < Height)
 			{
 				isFireTouch = true;
 			}
 		}
 		
 		////////////////////////////////////////////////
 		// If we're sliding our finger or we release. //
 		////////////////////////////////////////////////
 		if(pSceneTouchEvent.isActionMove() || pSceneTouchEvent.isActionUp())
 		{
 			if(isPowerTouch)
 			{
 				gm.changePlayerPower(gm.getCurrentPlayer(), (tempY - lastTouchY)/-20);
 				PowerBar[2].setY(16 + 208 * ((100 - gm.getPlayerPower(gm.getCurrentPlayer()))/100));
 			}
 			if(isAngleTouch)
 			{
				//Log.w("isAngleTouch", "P" + gm.getCurrentPlayer() + " angle is (" + tempX + " - " + lastTouchX + ") / 20 = " + ((tempX-lastTouchX)/20) + "r.");
 				gm.changePlayerAngle(gm.getCurrentPlayer(), (tempX - lastTouchX)/20);	
				//AngleBar[2].setY(16 + 208 * ((180 - gm.getPlayerAngle(gm.getCurrentPlayer()))/180));
				AngleBar[2].setY(16 + 208 * (( gm.getPlayerAngle(gm.getCurrentPlayer()))/180)); // Mike's tweak (2/2) to fix inverted angle bar
 				if(gm.getPlayerAngle(gm.getCurrentPlayer()) <= 90) // If facing left...
 				{
					Sprite tT = mPlayerSprites[1][(gm.getCurrentPlayer()-1)];	// T for Tank
					Sprite tB = mPlayerSprites[2][(gm.getCurrentPlayer()-1)];	// B for Barrel
 					tB.setX(20);
 					tB.setRotationCenter(4,4);
 					tB.setRotation(-gm.getPlayerAngle(gm.getCurrentPlayer()));
					//Log.w("isAngleTouch","P" + gm.getCurrentPlayer() + " angle is " + gm.getPlayerAngle(gm.getCurrentPlayer()) + ", barrel angle is " + tB.getRotation());
					//tB.setRotation(180+gm.getPlayerAngle(gm.getCurrentPlayer()));
 					tB.setFlippedHorizontal(false);
 					tT.setFlippedHorizontal(false);
 				}
 				else // If the tank is facing right....
 				{
 					Sprite tT = mPlayerSprites[1][(gm.getCurrentPlayer()-1)];
 					Sprite tB = mPlayerSprites[2][(gm.getCurrentPlayer()-1)];
 					tB.setX(0);
 					tB.setRotationCenter(20,4);
 					tB.setRotation(180- gm.getPlayerAngle(gm.getCurrentPlayer()));
					//Log.w("isAngleTouch","P" + gm.getCurrentPlayer() + " angle is " + gm.getPlayerAngle(gm.getCurrentPlayer()) + ", barrel angle is " + tB.getRotation());
					//tB.setRotation(- gm.getPlayerAngle(gm.getCurrentPlayer()));
 					tB.setFlippedHorizontal(true);
 					tT.setFlippedHorizontal(true);
 				}
 			}
 			if(pSceneTouchEvent.isActionUp())
 			{
 				isPowerTouch = false;
 				isAngleTouch = false;
 				if(isFireTouch && shellSprite == null)
 				{
 					isFireTouch = false;
 					fireBullet();
 				}
 			}
 		}
 				
 		return false;
 	}
 	
 	public void fireBullet()
 	{
 		final FixtureDef TILE_FIXTURE_DEF = PhysicsFactory.createFixtureDef(0.50f, 0.0f, 1.0f);		
 		GameManager gm = GameManager.getInstance();
 		// Sin(angle) * power = Y
 		// Cos(angle) * power = X
 		float firedAngle = GameManager.getInstance().getPlayerAngle();
 		float firedForce = GameManager.getInstance().getPlayerPower();
 		float scalarX;
 		float scalarY;
 		
 		scalarX = (float) Math.cos(firedAngle);
 		scalarY = (float) Math.sin(firedAngle);
 		// Quadrants where operations are positive
 		// 0-90:    All
 		// 90-180:  Sin
 		// 180-270: Tan
 		// 270-360: Cos
 		if (firedAngle > 90 && firedAngle < 180)
 		{
 			scalarX = -scalarX;
 			scalarY = -scalarY;
 		}
 		
 		float positionX = mPlayerSprites[0][gm.getCurrentPlayer() - 1].getX() + 23;
 		float positionY = mPlayerSprites[0][gm.getCurrentPlayer() - 1].getY() + 14;
 		
 		gm.togglePlayer();
 
 		
 		shellSprite = new Sprite( positionX + scalarX * 41, positionY + scalarY * 41, ResourceManager.getInstance().mShellTextureRegion, mEngine.getVertexBufferObjectManager());
 		Log.w("firebullet", "P" + GameManager.getInstance().getCurrentPlayer() + " center is at (" + positionX + "," + positionY + ").  Putting shell (angle "+firedAngle+") top-left at (" + shellSprite.getX() + "," + shellSprite.getY() + ")");
 		shellSprite.setRotationCenter((float) (shellSprite.getWidth()/2.0f), (float)(shellSprite.getHeight()/2.0f));
 		shellSprite.setRotation(-firedAngle);
 		mScene.attachChild(shellSprite);
 		shellBody = PhysicsFactory.createBoxBody(this.mPhysicsWorld, shellSprite, BodyType.DynamicBody, TILE_FIXTURE_DEF);
 		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(shellSprite, shellBody, true, true));
 //		shellBody.setWorldCenter(15,3);
 
 		shellBody.applyForce(new Vector2(scalarX * firedForce/2,scalarY * firedForce/2), new Vector2(shellBody.getWorldCenter().x, shellBody.getWorldCenter().y));
 
 		PowerBar[2].setY(16 + 208 * ((100 - gm.getPlayerPower())/100));
 		AngleBar[2].setY(16 + 208 * ((180 - gm.getPlayerAngle())/180));
 		ResourceManager.getInstance().mFiringSound.play();		// "boom"
 		if(mExplosion != null)
 			SpritesToDetach.add(mExplosion);
 	}
 	
 	public boolean isBodyContacted(Body pBody, Contact pContact)
 	{
 		if(pContact.getFixtureA().getBody().equals(pBody))
 			return true;
 		else if(pContact.getFixtureB().getBody().equals(pBody))
 			return true;
 		return false;
 	}
 }
 
 
