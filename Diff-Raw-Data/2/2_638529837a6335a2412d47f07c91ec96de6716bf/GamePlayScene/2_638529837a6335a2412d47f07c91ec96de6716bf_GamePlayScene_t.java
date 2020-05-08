 package com.secondhand.view.scene;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 
 import android.content.Context;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.controller.CollisionContactListener;
 import com.secondhand.controller.InputDialogManager;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.Player;
 import com.secondhand.model.resource.HighScoreList;
 import com.secondhand.view.entity.FadingNotifierText;
 import com.secondhand.view.entity.ScoreLivesText;
 import com.secondhand.view.opengl.StarsBackground;
 import com.secondhand.view.resource.Sounds;
 
 public class GamePlayScene extends GameScene implements PropertyChangeListener {
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 
 	private GameWorld gameWorld;
 
 	private Vector2 cachedCameraCenter;
 
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
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
 			if (!entity.getShape().hasParent())
 				attachChild(entity.getShape());
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
 		// gameWorld.setView(this);
 
 		// setup the HUD
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10, 10),
 				player.getScore(), player.getLives());
 		hud.attachChild(scoreLivesText);
 
 		// I do believe this belong here
 		getGameWorld().getPhysicsWorld().setContactListener(
				new CollisionContactListener(getGameWorld().getPhysics()));
 
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 	
 		// get rid the entities from the previous game.
 		// this.detachChildren();
 
 		MyDebug.i("creating game world");
 
 		this.gameWorld = new GameWorld();
 
 		// we'll need to be able to restore the camera when returning to the
 		// menu.
 		cachedCameraCenter = new Vector2(smoothCamera.getCenterX(),
 				smoothCamera.getCenterY());
 
 		setupView();
 		registerNewLevel();
 		// we set this as late as possible, to make sure it doesn't show up in
 		// the loading scene.
 		engine.getCamera().setHUD(hud);
 
 	}
 
 	// reset camera before the menu is shown
 	public void resetCamera() {
 		// stopping chasing player.
 		smoothCamera.setChaseEntity(null);
 		// reset zoom
 		smoothCamera.setZoomFactor(1.0f);
 
 		// camera.setCenter(camera.getWidth()/2, camera.getHeight()/2);
 
 		smoothCamera.setBoundsEnabled(false);
 		this.smoothCamera.setBounds(0, this.smoothCamera.getWidth(), 0,
 				this.smoothCamera.getHeight());
 		smoothCamera.setCenter(this.cachedCameraCenter.x,
 				this.cachedCameraCenter.y);
 		// smoothCamera.setBoundsEnabled(true);
 
 		// don't show the HUD in the menu.
 		hud.setCamera(null);
 	}
 	
 	public void onSwitchScene() {
 		super.onSwitchScene();
 		resetCamera();
 
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 	
 	// not a very good solution bellow but it can do for now
 	@Override
 	public void propertyChange(final PropertyChangeEvent event) {
 		final String eventName = event.getPropertyName();
 		if (eventName.equals(Player.INCREASE_SCORE)) {
 			updateScore((Integer) event.getNewValue());
 		} else if (eventName.equals(Player.INCREASE_LIFE)) {
 			updateLives((Integer) event.getNewValue());
 		} else if (eventName.equals("PlayerRadius")) {
 			apaptCameraToGrowingPlayer((Float) event.getNewValue(),
 					(Float) event.getOldValue());
 		} else if (eventName.equals("NextLevel")) {
 			smoothCamera.setZoomFactorDirect(1.0f);
 			newLevelStarted();
 		}
 	}
 
 	// zoom out when player grows.
 	// I think we may need to reset sizes when going to new level, otherwise it
 	// will look bad
 	private void apaptCameraToGrowingPlayer(final float newRadius,
 			final float oldRadius) {
 		this.smoothCamera.setZoomFactor(this.smoothCamera.getZoomFactor()
 				- 0.05f * oldRadius / newRadius);
 		/*
 		 * if(this.smoothCamera.getZoomFactor() < 0.0) {
 		 * this.smoothCamera.setZoomFactor(0); }
 		 */
 	}
 
 	public void showFadingTextNotifier(final String str, final Vector2 position) {
 
 		// convert positon to camera coordinates.
 		final Vector2 cameraPosition = new Vector2(position.x
 				- this.smoothCamera.getMinX(), position.y
 				- this.smoothCamera.getMinY());
 
 		this.hud.attachChild(new FadingNotifierText(str, cameraPosition));
 	}
 
 	public void newLevelStarted() {
 		MyDebug.d("new level!");
 
 		gameWorld.getPlayer().setRadius(30);
 		registerNewLevel();
 
 		Sounds.getInstance().winSound.play();
 
 	}
 
 	public void updateScore(final int newScore) {
 		this.scoreLivesText.setScore(newScore);
 	}
 
 	public void updateLives(final int newLives) {
 		this.scoreLivesText.setLives(newLives);
 	}
 
 }
