 package gamedev.scenes;
 
 import gamedev.game.ResourcesManager;
 import gamedev.game.SceneManager;
 
 import java.util.Iterator;
 
 import org.andengine.engine.handler.timer.ITimerCallback;
 import org.andengine.engine.handler.timer.TimerHandler;
 import org.andengine.entity.scene.CameraScene;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.util.HorizontalAlign;
 
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class GameOverScene extends CameraScene {
 
 	private static GameOverScene instance;
 	private ResourcesManager resourcesManager = ResourcesManager.getInstance();
 
 	private Sprite background;
 
 	private GameOverScene() {
 		super(ResourcesManager.getInstance().camera);
 		this.setBackgroundEnabled(false);
 
 		// TODO: Add game over picture.
 		// background = new Sprite(0, 0, resourcesManager.shopRegion,
 		// resourcesManager.vbom) {
 		// @Override
 		// protected void preDraw(GLState pGLState, Camera pCamera) {
 		// super.preDraw(pGLState, pCamera);
 		// pGLState.enableDither();
 		// }
 		// };
 		// centerShapeInCamera(background);
 		// attachChild(background);
 
 		String gameOverString = "GAME OVER!" + "\n\n" + "Tap...";
 		Text text = new Text(0, 0, resourcesManager.font, gameOverString,
 				resourcesManager.vbom);
 		text.setHorizontalAlign(HorizontalAlign.CENTER);
 		text.setScale(2f);
 		centerShapeInCamera(text);
 		attachChild(text);
 
 		// Wait 3 seconds until the onSceneTouchListener is added.
 		resourcesManager.engine.registerUpdateHandler(new TimerHandler(3,
 				new ITimerCallback() {
 					public void onTimePassed(final TimerHandler pTimerHandler) {
 						setOnSceneTouchListener(new IOnSceneTouchListener() {
 							@Override
 							public boolean onSceneTouchEvent(Scene pScene,
 									TouchEvent pSceneTouchEvent) {
 								if (pSceneTouchEvent.isActionDown()) {
 									closeGameOverScene();
 								}
 								return true;
 							}
 						});
 					}
 				}));
 	}
 
 	public void openGameOverScene() {
 		resourcesManager.unloadHUDResources();
 		Scene childScene = SceneManager.getInstance().getCurrentGameMapScene()
 				.getChildScene();
		childScene.detachSelf();
		if (!childScene.isDisposed()) {
			childScene.dispose();
 		}
 		SceneManager.getInstance().getCurrentGameMapScene().clearChildScene();
 		SceneManager.getInstance().getCurrentGameMapScene().setChildScene(this);
 	}
 
 	public void closeGameOverScene() {
 		this.detachSelf();
 		if (!this.isDisposed()) {
 			this.dispose();
 		}
 		SceneManager.getInstance().loadMenuScene(resourcesManager.engine);
 
 		GameMapScene mapScene = SceneManager.getInstance()
 				.getCurrentGameMapScene();
 		mapScene.disposeScene();
 		Iterator<Body> it = resourcesManager.physicsWorld.getBodies();
 		while (it.hasNext()) {
 			Body body = it.next();
 			if (body != null)
 				resourcesManager.physicsWorld.destroyBody(body);
 		}
 		resourcesManager.physicsWorld.clearForces();
 		resourcesManager.physicsWorld.clearPhysicsConnectors();
 		resourcesManager.physicsWorld.reset();
 		resourcesManager.physicsWorld.dispose();
 		resourcesManager.physicsWorld = null;
 		resourcesManager.avatar = null;
 		SceneManager.getInstance().deleteCurrentGameMapScene();
 	}
 
 	public static GameOverScene getInstance() {
 		if (instance == null) {
 			instance = new GameOverScene();
 		}
 		return instance;
 	}
 
 }
