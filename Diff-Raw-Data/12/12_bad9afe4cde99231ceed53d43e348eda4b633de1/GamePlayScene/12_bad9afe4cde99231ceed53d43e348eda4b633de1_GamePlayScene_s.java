 package com.secondhand.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
 import org.anddev.andengine.engine.camera.hud.HUD;
 
 import android.content.Context;
 import android.view.KeyEvent;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.controller.CollisionContactListener;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
import com.secondhand.model.powerup.ExtraLife;
 import com.secondhand.model.powerup.PowerUp;
import com.secondhand.model.powerup.ScoreUp;
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
 		
 		
 		// I do believe this belong here
 			getGameWorld().getPhysicsWorld().setContactListener(
 						new CollisionContactListener(getGameWorld()));
 				
 	}
 	
 	
 	@Override
 	public void loadScene() {
 		super.loadScene();
 		
 		// get rid the entities from the previous game.
 		//this.detachChildren();
 		
 		
 	
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
 				isLoaded = false;
 				resetCamera();
 				
 				setScene(parent);
 				return true;
 			} else
 				return false;
 		} else {
 			return false;
 		}
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
 			resetCamera();
 			setScene(AllScenes.GAME_OVER_SCENE);
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
 		this.attachChild(new FadingNotifierText(str, position));
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
 	
	public void playerMoveAnimation(Vector2 touch) {
		Vector2 playerPosition = new Vector2(gameWorld.getPlayer().getCenterX(),gameWorld.getPlayer().getCenterY());
		Vector2 touchPosition = new Vector2(touch);
 		
 		// Fire some sort of particles relative to the players and touched position
 	}
 	
 }
