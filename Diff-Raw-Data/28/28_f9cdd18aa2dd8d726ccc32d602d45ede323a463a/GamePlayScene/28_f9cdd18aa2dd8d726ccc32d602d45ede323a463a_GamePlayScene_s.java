 package com.secondhand.view.scene;
 
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.anddev.andengine.engine.Engine;
 import org.anddev.andengine.engine.camera.hud.HUD;
 import org.anddev.andengine.extension.physics.box2d.PhysicsWorld;
 
 import android.content.Context;
 
 import com.badlogic.gdx.math.Vector2;
 import com.secondhand.debug.MyDebug;
 import com.secondhand.model.BlackHole;
 import com.secondhand.model.Entity;
 import com.secondhand.model.GameWorld;
 import com.secondhand.model.IPhysicsWorld;
 import com.secondhand.model.Player;
 import com.secondhand.view.entities.BlackHoleView;
 import com.secondhand.view.entities.EntityView;
 import com.secondhand.view.entity.FadingNotifierText;
 import com.secondhand.view.entity.ScoreLivesText;
 import com.secondhand.view.opengl.StarsBackground;
 import com.secondhand.view.physics.MyPhysicsEntity;
 import com.secondhand.view.physics.MyPhysicsWorld;
 import com.secondhand.view.resource.Sounds;
 
 public class GamePlayScene extends GameScene{
 
 	private HUD hud;
 
 	private ScoreLivesText scoreLivesText;
 	
 	private List<EntityView> entityViewList;
 
 	private GameWorld gameWorld;
 	
 	private IPhysicsWorld physics;
 	private PhysicsWorld physicsWorld;
 	
 	public void setPhysics(final IPhysicsWorld physics) {
 		this.physics = physics;
 	}
 
 	public GamePlayScene(final Engine engine, final Context context) {
 		super(engine, context);
 		physicsWorld = new PhysicsWorld(new Vector2(),true);
 		physics = new MyPhysicsWorld(physicsWorld);
 	}
 
 	public GameWorld getGameWorld() {
 		return this.gameWorld;
 	}
 	
 	public PhysicsWorld getPhysicsWorld(){
 		return this.physicsWorld;
 	}
 
 	public void registerNewLevel() {
 		
 		entityViewList = new ArrayList();
 		
 
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		new BlackHoleView(gameWorld.getPlayer());
 
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
 			if (!entity.getShape().hasParent()){
 				
 				if(entity instanceof BlackHole){
 				//entityViewList.add(new BlackHoleView((BlackHole) entity));
 				//MyDebug.d("new BlackHOleView created!");
 				}
 				
 				attachChild(entity.getShape());
 			}
 		}
 	}
 
 	private void setupView() {
 		MyDebug.i("In setupView gameplayscene");
 		final float width = gameWorld.getLevelWidth();
 		final float height = gameWorld.getLevelHeight();
 		MyDebug.i("In setupView gameplayscene");
 		this.smoothCamera.setBounds(0, width, 0, height);
 		this.smoothCamera.setBoundsEnabled(true);
 		// setup the player
 		MyDebug.i("In setupView gameplayscene");
 		final Player player = gameWorld.getPlayer();
 		MyDebug.i("In setupView gameplayscene");
		player.getShape().detachSelf();
 		MyDebug.i("In setupView gameplayscene");
 		attachChild(player.getShape());
 		MyDebug.i("In setupView gameplayscene");
 		engine.getCamera().setChaseEntity(player.getShape());//playerView
 		MyDebug.i("In setupView gameplayscene");
 
 		registerUpdateHandler(physicsWorld);
 
 		// setup the HUD
 		hud = new HUD();
 		this.scoreLivesText = new ScoreLivesText(new Vector2(10, 10),
 				player.getScore(), player.getLives());
 		hud.attachChild(scoreLivesText);
 	}
 
 	@Override
 	public void loadScene() {
 		super.loadScene();
 	
 		MyDebug.i("creating game world");
 		
 		this.gameWorld = new GameWorld(physics);
 		MyDebug.i("Done game world");
 		gameWorld.getPlayer().setPhysics(new MyPhysicsEntity(physicsWorld));
 		gameWorld.generateNewLevelEntities();
 		MyDebug.i("Done set physics for player");
 		// we'll need to be able to restore the camera when returning to the
 		// menu.
 		
 		gameWorld.setCameraPos(smoothCamera.getCenterX(),
 				smoothCamera.getCenterY());
 		MyDebug.i("done setCamera");
 
 		setupView();
 		MyDebug.i("done setupView");
 		registerNewLevel();
 		MyDebug.i("done registernewLevel");
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
 		smoothCamera.setCenter(gameWorld.getCameraX(),
 				gameWorld.getCameraY());
 		// smoothCamera.setBoundsEnabled(true);
 
 		// don't show the HUD in the menu.
 		hud.setCamera(null);
 	}
 	
 	@Override
 	public void onSwitchScene() {
 		super.onSwitchScene();
 		resetCamera();
 
 	}
 
 	@Override
 	public AllScenes getParentScene() {
 		return AllScenes.MAIN_MENU_SCENE;
 	}
 
 	// zoom out when player grows.
 	// I think we may need to reset sizes when going to new level, otherwise it
 	// will look bad
 	public void apaptCameraToGrowingPlayer(final float newRadius,
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
 		Sounds.getInstance().winSound.play();
 
 		smoothCamera.setZoomFactorDirect(1.0f);
 		
 		registerNewLevel();
 	}
 
 	public void updateScore(final int newScore) {
 		this.scoreLivesText.setScore(newScore);
 	}
 
 	public void updateLives(final int newLives) {
 		this.scoreLivesText.setLives(newLives);
 	}
 
 	public void onPlayerWallCollision() {
 		Sounds.getInstance().obstacleCollisionSound.play();	
 	}
 }
