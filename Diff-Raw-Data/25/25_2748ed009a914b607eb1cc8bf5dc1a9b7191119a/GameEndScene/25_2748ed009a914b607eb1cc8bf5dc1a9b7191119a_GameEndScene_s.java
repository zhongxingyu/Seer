 package gamedev.scenes;
 
 import gamedev.game.SceneManager;
 import gamedev.game.SceneManager.SceneType;
 
 import java.util.Iterator;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.modifier.FadeInModifier;
 import org.andengine.entity.modifier.FadeOutModifier;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.HorizontalAlign;
 
 import com.badlogic.gdx.physics.box2d.Body;
 
 public class GameEndScene extends BaseScene {
 
 	private static final float FADE_IN_DURATION = 1f;
 	private static final float FADE_OUT_DURATION = 0.5f;
 
 	private static final String gameEndString = "Congratulations! " + "\n"
 			+ "The portal was working properly and you returned to your era."
 			+ "\n\n\n" + "Tap...";
 	private static final String gameDevelopersString = "This game was designed and developed by"
 			+ "\n"
 			+ "Stefan Wanzenried"
 			+ "\n"
 			+ "David Wettstein"
 			+ "\n\n\n"
 			+ "Tap...";
 
 	private Sprite gameEndSprite;
 	private Text gameEndText;
 	private Text gameDevelopersText;
 
 	@Override
 	public void createScene() {
 		float centerX = camera.getCenterX()
 				- resourcesManager.game_end_region.getWidth() / 2;
 		float centerY = camera.getCenterY()
 				- resourcesManager.game_end_region.getHeight() / 2;
 
 		gameEndText = new Text(0, 0, resourcesManager.font, gameEndString,
 				gameEndString.length(), vbom);
 		gameEndText.setPosition(camera.getCenterX() - gameEndText.getWidth()
 				/ 2, camera.getCenterY() - gameEndText.getHeight() / 2);
 		gameEndText.setHorizontalAlign(HorizontalAlign.CENTER);
 		attachChild(gameEndText);
 		gameEndText
 				.registerEntityModifier(new FadeInModifier(FADE_IN_DURATION));
 
 		gameEndSprite = new Sprite(centerX, centerY,
 				resourcesManager.game_end_region.getWidth(),
 				resourcesManager.game_end_region.getHeight(),
 				resourcesManager.game_end_region, vbom) {
 			@Override
 			protected void preDraw(GLState pGLState, Camera pCamera) {
 				super.preDraw(pGLState, pCamera);
 				pGLState.enableDither();
 			}
 		};
 
 		gameDevelopersText = new Text(0, 0, resourcesManager.font,
 				gameDevelopersString, gameDevelopersString.length(), vbom);
 		gameDevelopersText.setPosition(
 				camera.getCenterX() - gameDevelopersText.getWidth() / 2,
 				camera.getCenterY() - gameDevelopersText.getHeight() / 2);
 		gameDevelopersText.setHorizontalAlign(HorizontalAlign.CENTER);
 
 		// This call has to be at the end.
 		registerSceneTouchListener();
 	}
 
 	private void registerSceneTouchListener() {
 		this.setOnSceneTouchListener(new IOnSceneTouchListener() {
 			@Override
 			public boolean onSceneTouchEvent(Scene pScene,
 					TouchEvent pSceneTouchEvent) {
 				if (pSceneTouchEvent.isActionDown()) {
 
 					if (gameEndText.hasParent()) {
 						gameEndText.registerEntityModifier(new FadeOutModifier(
 								FADE_OUT_DURATION));
 						detachViaUpdateHandlerAfterTime(gameEndText,
 								FADE_OUT_DURATION);
 
 						if (!gameEndSprite.hasParent()) {
 							attachChild(gameEndSprite);
 						}
 						gameEndSprite
 								.registerEntityModifier(new FadeInModifier(
 										FADE_IN_DURATION));
 						return true;
 					}
 
 					if (gameEndSprite.hasParent()) {
 						gameEndSprite
 								.registerEntityModifier(new FadeOutModifier(
 										FADE_OUT_DURATION));
 						detachViaUpdateHandlerAfterTime(gameEndSprite,
 								FADE_OUT_DURATION);
 
 						if (!gameDevelopersText.hasParent()) {
 							attachChild(gameDevelopersText);
 						}
 						gameDevelopersText
 								.registerEntityModifier(new FadeInModifier(
 										FADE_IN_DURATION));
 						return true;
 					}
 
 					if (gameDevelopersText.hasParent()) {
 						gameDevelopersText
 								.registerEntityModifier(new FadeOutModifier(
 										FADE_OUT_DURATION));
 						detachViaUpdateHandlerAfterTime(gameDevelopersText,
 								FADE_OUT_DURATION);
 
 						SceneManager.getInstance().loadMenuScene(engine);
 
 						GameMapScene mapScene = SceneManager.getInstance()
 								.getCurrentGameMapScene();
 						mapScene.disposeScene();
 
 						Iterator<Body> it = resourcesManager.physicsWorld
 								.getBodies();
 						while (it.hasNext()) {
 							Body body = it.next();
 							resourcesManager.physicsWorld.destroyBody(body);
 						}
 
 						resourcesManager.physicsWorld.clearForces();
 						resourcesManager.physicsWorld.clearPhysicsConnectors();
 						resourcesManager.physicsWorld.reset();
 						resourcesManager.physicsWorld.dispose();
 						resourcesManager.physicsWorld = null;
 						resourcesManager.avatar = null;
 						SceneManager.getInstance().deleteCurrentGameMapScene();
 
 						return true;
 					}
 				}
 				return false;
 			}
 
 		});
 	}
 
 	@Override
 	public void onBackKeyPressed() {
 		SceneManager.getInstance().loadMenuScene(engine);
 	}
 
 	@Override
 	public SceneType getSceneType() {
 		return SceneType.SCENE_GAME_END;
 	}
 
 	@Override
 	public void disposeScene() {
 		if (!gameEndText.isDisposed()) {
 			gameEndText.detachSelf();
 			gameEndText.dispose();
 		}
 
 		if (!gameEndSprite.isDisposed()) {
 			gameEndSprite.detachSelf();
 			gameEndSprite.dispose();
 		}
 
 		if (!gameDevelopersText.isDisposed()) {
 			gameDevelopersText.detachSelf();
 			gameDevelopersText.dispose();
 		}
 
 		this.detachSelf();
 		if (!this.isDisposed()) {
 			this.dispose();
 		}
 	}
 }
