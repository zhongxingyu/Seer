 package com.secondhand.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.engine.handler.timer.ITimerCallback;
 import org.anddev.andengine.engine.handler.timer.TimerHandler;
 import org.anddev.andengine.entity.particle.ParticleSystem;
 import org.anddev.andengine.entity.particle.emitter.PointParticleEmitter;
 import org.anddev.andengine.opengl.texture.TextureOptions;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
 import org.anddev.andengine.opengl.texture.region.TextureRegion;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.controller.CollisionContactListener;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.powerup.PowerUp;
 import com.secondhand.opengl.StarsBackground;
 import com.secondhand.resource.Sounds;
 
 public class GamePlayScene extends GameScene implements PropertyChangeListener,
 		IGamePlaySceneView {
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 
 	private GameWorld gameWorld;
 	
 	private Vector2 cachedCameraCenter;
 
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
 
 		MyDebug.i("creating game world");
 		//Have to create gameWorld here, because else it is null when I need it!
 		this.gameWorld = new GameWorld();
 	}
 
 	public GameWorld getGameWorld() {
 		return this.gameWorld;
 	}
 	
 	public void registerNewLevel() {
 		
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 
 		
 		// TODO: get this background to work.
 		/*
 		 * final List<TextureRegion> starsTextures = new
 		 * ArrayList<TextureRegion>();
 		 * starsTextures.add(TextureRegions.getInstance().starsTexture);
 		 * this.attachChild(new RandomRepeatingBackground(starsTextures, width,
 		 * height));
 		 */
 
 		// starry sky
 		attachChild(new StarsBackground(50, 5.0f, width, height));
 		attachChild(new StarsBackground(100, 3.0f, width, height));
 		this.attachChild(new StarsBackground(130, 1.0f, width, height));
 		
 		this.smoothCamera.setBounds(0, width, 0, height);
 
 		for (final Entity entity : gameWorld.getEntityManager().getEntityList()) {
 			if(!entity.getShape().hasParent())
 				attachChild( entity.getShape());
 		}
 	}
 
 	private void setupView() {
 
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 
 		this.smoothCamera.setBounds(0, width, 0, height);
 		this.smoothCamera.setBoundsEnabled(true);
 		// setup the player
 
 		final Player player = gameWorld.getPlayer();
 		player.getShape().detachSelf();
 		attachChild(player.getShape());
 		gameWorld.addListener(this);
 		engine.getCamera().setChaseEntity(player.getShape());
 
 		// setup the physicsworld the
 		registerUpdateHandler(gameWorld.getPhysicsWorld());
 		//gameWorld.setView(this);
 
 		// setup the HUD
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10, 10),
 				player.getScore(), player.getLives());
 		hud.attachChild(scoreLivesText);
 		
 		
 	//	hud.attachChild(new FadingNotifierText("hello!", new Vector2(100,100)));
 		
 		
 		// I do believe this belong here
 			getGameWorld().getPhysicsWorld().setContactListener(
 						new CollisionContactListener(getGameWorld()));
 				
 	}
 	
 	
 	@Override
 	public void loadScene() {
 		super.loadScene();
 		
 		// get rid the entities from the previous game.
 		//this.detachChildren();
 		
 		MyDebug.i("creating game world");
 		
 		this.gameWorld = new GameWorld();
 	
 		// we'll need to be able to restore the camera when returning to the menu.
 		cachedCameraCenter = new Vector2(smoothCamera.getCenterX(), smoothCamera.getCenterY());
 		
 		
 		
 		MyDebug.d("loading game play sceme");
 		
 		setupView();
 		registerNewLevel();
 		// we set this as late as possible, to make sure it doesn't show up in the loading scene. 
 		engine.getCamera().setHUD(hud);
 		
 	}
 
 	// reset camera before the menu is shown
 	public void resetCamera() {
 		// stopping chasing player.
 		smoothCamera.setChaseEntity(null);
 		// reset zoom
 		smoothCamera.setZoomFactor(1.0f);
 		
 		//camera.setCenter(camera.getWidth()/2, camera.getHeight()/2);
 		
 		smoothCamera.setBoundsEnabled(false);
 		this.smoothCamera.setBounds(0, this.smoothCamera.getWidth(), 0, this.smoothCamera.getHeight());
 		smoothCamera.setCenterDirectThatActuallyFuckingWorks(this.cachedCameraCenter.x, this.cachedCameraCenter.y);
 		//smoothCamera.setBoundsEnabled(true);
 		
 		
 
 		// don't show the HUD in the menu.
 		hud.setCamera(null);
 	}
 
 	@Override
 	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
 		if (pKeyCode == KeyEvent.KEYCODE_BACK
 				&& pEvent.getAction() == KeyEvent.ACTION_DOWN) {
 			final AllScenes parent = getParentScene();
 			if (parent != null) {
 				this.switchScene(parent);
 				return true;
 			} else
 				return false;
 		} else {
 			return false;
 		}
 	}
 	
	public void switchScene(final AllScenes scene) {
 		isLoaded = false;
 		resetCamera();
 		
 		setScene(scene);
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 		super.onManagedUpdate(pSecondsElapsed);
 		if (gameWorld.isGameOver()) {
 			MyDebug.d("GameOver");
 			switchScene(AllScenes.GAME_OVER_SCENE);
 		}
 		gameWorld.onManagedUpdate(pSecondsElapsed);
 
 	}
 
 	// not a very good solution bellow but it can do for now 
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final String eventName = event.getPropertyName();
 		if (eventName.equals("ADD")) {
 			
 			final Player player = gameWorld.getPlayer();
 			final PowerUp powerUp = ((PowerUp) event.getNewValue());
 			engine.registerUpdateHandler(powerUp.getTimer(player));
 			
 			if (powerUp.hasText()) {
 				showFadingTextNotifier(powerUp.getText(),
 						new Vector2(player.getX(), player.getY()));
 			} 
 		} else if (eventName.equals("Score")) {
 			updateScore((Integer) event.getNewValue());
 		} else if (eventName.equals("Life")) {
 			updateLives((Integer) event.getNewValue());
 		} else if (eventName.equals("PlayerRadius")) {
 			final float newRadius = (Float)event.getNewValue();
 			MyDebug.d("new radius: " + newRadius);
 			apaptCameraToGrowingPlayer( (Float)event.getNewValue(),  (Float)event.getOldValue());
 		} else if(eventName.equals("NextLevel")){
 			newLevelStarted();
 		} else if (eventName.equals("PlayerMove")) {
 			playerMoveAnimation((Vector2) event.getNewValue());
 		}
 	}
 	
 	
 	// zoom out when player grows.
 	private void apaptCameraToGrowingPlayer(final float newRadius, final float oldRadius) {
 		this.smoothCamera.setZoomFactor(this.smoothCamera.getZoomFactor() - 0.05f * oldRadius/newRadius);
 		/*if(this.smoothCamera.getZoomFactor() < 0.0) {
 			this.smoothCamera.setZoomFactor(0);
 		}*/
 	}
 	
 	@Override
 	public void showFadingTextNotifier(final String str, final Vector2 position) {
 		
 		// convert positon to camera coordinates.
 		final Vector2 cameraPosition = new Vector2( 
 				position.x - this.smoothCamera.getMinX(),
 						position.y - this.smoothCamera.getMinY());
 		
 		this.hud.attachChild(new FadingNotifierText(str, cameraPosition));
 	}
 
 	@Override
 	public void newLevelStarted() {
 		MyDebug.d("new level!");
 		registerNewLevel();
 		Sounds.getInstance().winSound.play();
 		setScene(AllScenes.CHANGE_LEVEL_SCENE);
 		
 		
 	}
 
 	@Override
 	public void updateScore(final int newScore) {
 		this.scoreLivesText.setScore(newScore);
 	}
 
 	@Override
 	public void updateLives(final int newLives) {
 		this.scoreLivesText.setLives(newLives);
 	}
 	
 	public void playerMoveAnimation(final Vector2 touch) {
 
 		final Vector2 playerPosition = new Vector2(gameWorld.getPlayer().getCenterX(),gameWorld.getPlayer().getCenterY());
 		final Vector2 touchPosition = new Vector2(touch);
 
 		Player player = gameWorld.getPlayer();
 		
 		// TODO: Will use TextureLoader, this is just for testing
 		final BitmapTextureAtlas texture = new BitmapTextureAtlas(16, 16, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
 		final TextureRegion particleTexture = BitmapTextureAtlasTextureRegionFactory.createFromAsset(texture, context, "gfx/particle.png", 0, 0);
 		
 		final Vector2 surfacePosition = getRelativeSurfacePosition(player, touch);
 		
 		final PointParticleEmitter movementEmitter = new PointParticleEmitter(surfacePosition.x, surfacePosition.y);
 		final ParticleSystem particleSystem = new ParticleSystem(movementEmitter, 60, 60, 10, particleTexture);
 		
 		attachChild(particleSystem);
 		
 		final float duration = 2; 
 		engine.registerUpdateHandler(new TimerHandler(duration, new ITimerCallback() {
 			@Override
 			public void onTimePassed(TimerHandler pTimerHandler) {
 				GamePlayScene.this.detachChild(particleSystem);
 			}
 		}));
 	}
 	
 	// Calculate the surface position of object relative to given position
 	public Vector2 getRelativeSurfacePosition(Entity object, Vector2 position) {
 		// Vector from object position to given position
 		Vector2 surfacePosition = new Vector2(object.getCenterX() - position.x, object.getCenterY() - position.y);
 		// Length of new vector increased/decreased to length of radius
 		surfacePosition.mul(object.getRadius() / surfacePosition.len());
 		return surfacePosition;
 	}
 }
