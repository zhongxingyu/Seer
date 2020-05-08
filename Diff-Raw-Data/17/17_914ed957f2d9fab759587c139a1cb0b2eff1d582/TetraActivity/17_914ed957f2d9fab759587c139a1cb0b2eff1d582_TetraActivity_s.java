 package ddmp.projecttetra;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.engine.handler.IUpdateHandler;
 import org.andengine.engine.options.EngineOptions;
 import org.andengine.engine.options.ScreenOrientation;
 import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.util.FPSLogger;
 import org.andengine.extension.physics.box2d.PhysicsFactory;
 import org.andengine.extension.physics.box2d.PhysicsWorld;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.TextureOptions;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
 import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
 import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
 import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.ui.activity.SimpleBaseGameActivity;
 import org.andengine.util.debug.Debug;
 
 import android.util.Log;
 
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.physics.box2d.Body;
 import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
 import com.badlogic.gdx.physics.box2d.FixtureDef;
 
 public class TetraActivity extends SimpleBaseGameActivity {
 
 	private static final int CAMERA_WIDTH = 480;
 	private static final int CAMERA_HEIGHT = 720;
 	private float CAMERA_SLOWNESS = 15;
 
 	private PhysicsWorld mPhysicsWorld;
 	private Camera mCamera;
 	private BuildableBitmapTextureAtlas mTextureAtlas;
 	private ITextureRegion mCometTextureRegion;
 	private ITextureRegion mPlanetTextureRegion;
 	private Comet comet;
 	private boolean cameraUpdates = true;
 
 	@Override
 	public EngineOptions onCreateEngineOptions() {
 		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
 		return new EngineOptions(true, ScreenOrientation.PORTRAIT_FIXED,
 				new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), mCamera);
 	}
 
 	@Override
 	protected void onCreateResources() {
 		mTextureAtlas = new BuildableBitmapTextureAtlas(
 				this.getTextureManager(), 1024, 1024,
 				TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
 
 		mCometTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mTextureAtlas, this, "comet.png");
 		mPlanetTextureRegion = BitmapTextureAtlasTextureRegionFactory
 				.createFromAsset(mTextureAtlas, this, "planet_earthlike1.png");
 
 		try {
 			this.mTextureAtlas
 			.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(
 					0, 1, 0));
 		} catch (final TextureAtlasBuilderException e) {
 			Debug.e(e);
 		}
 
 		this.mTextureAtlas.load();
 	}
 
 	@Override
 	protected Scene onCreateScene() {
 		this.mEngine.registerUpdateHandler(new FPSLogger());
 
 		final Scene scene = new Scene();
 
 		scene.setBackground(new Background(0f, 0f, 0f));
 		scene.setOnSceneTouchListener(new IOnSceneTouchListener() {
 
 			@Override
 			public boolean onSceneTouchEvent(Scene pScene,
 					TouchEvent pSceneTouchEvent) {
 				mCamera.convertSceneToSurfaceTouchEvent(pSceneTouchEvent, 
 						mEngine.getSurfaceWidth(), mEngine.getSurfaceHeight());
 				if(pSceneTouchEvent.isActionDown()) {
 					touchDown(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 				}
 				if(pSceneTouchEvent.isActionUp()) {
 					touchUp(pSceneTouchEvent.getX(), pSceneTouchEvent.getY());
 				}
 
 				return true;
 			}
 		});
 
 
 		this.mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
 
 		/*
 		 * Calculate the coordinates for the comet, so its centered on the
 		 * camera.
 		 */
		final float centerX = (CAMERA_WIDTH - this.mCometTextureRegion
				.getWidth()) / 2;
		final float centerY = (CAMERA_HEIGHT - this.mCometTextureRegion
				.getHeight()) / 2;

		/* Create the comet sprite and add it to the scene. */
		final Sprite cometSprite = new Sprite(centerX, centerY,
				0.12f * CAMERA_HEIGHT, 0.12f * CAMERA_HEIGHT,
				this.mCometTextureRegion, this.getVertexBufferObjectManager());
 		scene.attachChild(cometSprite);
 
 		/* Create the comet body. */
 		FixtureDef cometFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
 				0.5f);
 		Body cometBody = PhysicsFactory.createCircleBody(this.mPhysicsWorld,
 				cometSprite, BodyType.DynamicBody, cometFixtureDef);
 		cometBody.setLinearVelocity(0, -2);
 		this.comet = new Comet(cometSprite, cometBody);
 		this.mPhysicsWorld.registerPhysicsConnector(comet);
 		scene.registerUpdateHandler(mPhysicsWorld);
 
 		this.mCamera.setChaseEntity(cometSprite);
 
 		PlanetManager pManager = new PlanetManager(this.mEngine,
 				this.mPhysicsWorld);
 		scene.registerUpdateHandler(pManager);
 
 		PlanetSpawner pSpawner = new PlanetSpawner(this.mEngine,
 				this.mPhysicsWorld, pManager, comet, this.mPlanetTextureRegion);
 
 		scene.registerUpdateHandler(pSpawner);
 
 		scene.registerUpdateHandler(new IUpdateHandler() {
 
 			@Override
 			public void onUpdate(float pSecondsElapsed) {
 				if(cameraUpdates) {
 					Vector2 tmpVel = comet.getBody().getLinearVelocity();
 					float goalAngle = (float) -(Math.atan2(tmpVel.y, tmpVel.x) * 180/Math.PI + 90);
 					float camAngle = mCamera.getRotation();
 					Log.d("Angles", "Cam: " + camAngle + " Goal: " + goalAngle);
 					if (camAngle<=-180 && goalAngle>=0) {
 						float newAngle = (CAMERA_SLOWNESS*camAngle+(goalAngle-360))/(CAMERA_SLOWNESS+1);
 						if (newAngle < -270) { // keep angles between -270 and 90
 							newAngle += 360;
 						}
 						mCamera.setRotation(newAngle);
 					} else if (camAngle>=0 && goalAngle<=-180) {
 						float newAngle = (CAMERA_SLOWNESS*camAngle+(goalAngle+360))/(CAMERA_SLOWNESS+1);
 						if (newAngle > 90) {
 							newAngle -= 360;
 						}
 						mCamera.setRotation(newAngle);
 					} else {
 						mCamera.setRotation((float) (CAMERA_SLOWNESS*camAngle+goalAngle)/(CAMERA_SLOWNESS+1));
 					}
 				}
 			}
 
 			@Override
 			public void reset() {}
 
 		});
 
 		return scene;
 	}
 
 	private void touchDown(float x, float y) {
 		cameraUpdates = false;
 		if (x > mEngine.getSurfaceWidth() / 2) {
 			comet.setTurnRight(true);
 		} else {
 			comet.setTurnLeft(true);
 		}
 	}
 
 	private void touchUp(float x, float y) {
 		cameraUpdates = true;
 		if (x > mEngine.getSurfaceWidth() / 2) {
 			comet.setTurnRight(false);
 		} else {
 			comet.setTurnLeft(false);
 		}
 	}
 
 }
