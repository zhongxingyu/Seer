 package gamedev.scenes;
 
 import gamedev.game.SceneManager;
 import gamedev.game.SceneManager.SceneType;
 
 import java.util.ArrayList;
 
 import org.andengine.engine.camera.Camera;
 import org.andengine.entity.IEntity;
 import org.andengine.entity.modifier.FadeInModifier;
 import org.andengine.entity.modifier.FadeOutModifier;
 import org.andengine.entity.primitive.Rectangle;
 import org.andengine.entity.scene.IOnAreaTouchListener;
 import org.andengine.entity.scene.IOnSceneTouchListener;
 import org.andengine.entity.scene.ITouchArea;
 import org.andengine.entity.scene.Scene;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.entity.text.Text;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.util.GLState;
 import org.andengine.util.HorizontalAlign;
 import org.andengine.util.color.Color;
 
 import android.widget.Toast;
 
 public class GameIntroScene extends BaseScene {
 
 	private static final float FADE_IN_DURATION = 1f;
 	private static final float FADE_OUT_DURATION = 0.5f;
 
 	private static final String skipIntroString = "Tap here to skip the intro.";
 
 	private static final String string0 = "Once a beautiful day in our modern world...";
 	private static final String string1 = "a bad accident messed up your life...";
 	private static final String string2 = "With a big flash and a terrible noise,\nyou were thrown back to the stone age...";
 	private static final String string3 = "Fortunately, some kindly cavemen\nhave taken you to their living cave.\n They gave you food and water until\nfinally you were fully recovered...";
 	private static final String string4 = "But life in stone age is boring...\n You miss your wife and friends back home...\nAfter some weeks, you saw a fascinating\n mural painting and recognized something...";
 	private static final String string5 = "There has to be a portal somewhere,\nwhich could bring you back home...";
 	private static final String string6 = "On a small map you discovered where this portal could be!\nWithout fear and armed with a spear,\nyou walk out of the cave...";
 
 	private static final String[] gameIntroStrings = { string0, string1,
 			string2, string3, string4, string5, string6 };
 
 	private ArrayList<Text> gameIntroTexts;
 	private ArrayList<Sprite> gameIntroSprites;
 	private ArrayList<IEntity> gameIntroEntities;
 
 	private Rectangle skipIntroButton;
 	private Text skipIntroText;
 
 	private int nextEntity = 1;
 
 	@Override
 	public void createScene() {
 		// Create the text list.
 		gameIntroTexts = new ArrayList<Text>();
 		for (String string : gameIntroStrings) {
 			Text text = new Text(0, 0, resourcesManager.font, string, vbom);
 			text.setPosition(camera.getCenterX() - text.getWidth() / 2,
 					camera.getCenterY() - text.getHeight() / 2);
 			text.setHorizontalAlign(HorizontalAlign.CENTER);
 			gameIntroTexts.add(text);
 		}
 
 		// Create the picture list.
 		float spriteCenterX = camera.getCenterX()
 				- resourcesManager.game_intro_region.get(0).getWidth() / 2;
 		float spriteCenterY = camera.getCenterY()
 				- resourcesManager.game_intro_region.get(0).getHeight() / 2;
 
 		gameIntroSprites = new ArrayList<Sprite>();
 		for (ITextureRegion region : resourcesManager.game_intro_region) {
 			Sprite sprite = new Sprite(spriteCenterX, spriteCenterY,
 					region.getWidth(), region.getHeight(), region, vbom) {
 				@Override
 				protected void preDraw(GLState pGLState, Camera pCamera) {
 					super.preDraw(pGLState, pCamera);
 					pGLState.enableDither();
 				}
 			};
 			gameIntroSprites.add(sprite);
 		}
 
 		gameIntroEntities = new ArrayList<IEntity>();
 		// Add the entities alternately, beginning with the pictures.
 		for (int i = 0; i < 7; i++) {
 			gameIntroEntities.add(gameIntroSprites.get(i));
 			gameIntroEntities.add(gameIntroTexts.get(i));
 		}
 		// Attach the first entity to the scene.
 		attachChild(gameIntroEntities.get(0));
 		// Add skip intro button. Remove it after the first entity.
 		addSkipIntroButton();
 
 		// Tell how to proceed.
 		resourcesManager.activity.toastOnUIThread(
 				"Tap on the screen to continue...", Toast.LENGTH_LONG);
 
 		// This call has to be at the end.
 		registerSceneTouchListener();
 	}
 
 	private void addSkipIntroButton() {
		skipIntroButton = new Rectangle(camera.getCenterX() + 80,
				camera.getCenterY() + 170, 300, 50, vbom);
 		skipIntroButton.setColor(Color.BLACK);
 		skipIntroText = new Text(10, 8, resourcesManager.font, skipIntroString,
 				vbom);
 		skipIntroButton.attachChild(skipIntroText);
 
 		this.attachChild(skipIntroButton);
 		this.registerTouchArea(skipIntroButton);
 		this.setOnAreaTouchListener(new IOnAreaTouchListener() {
 
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
 					ITouchArea pTouchArea, float pTouchAreaLocalX,
 					float pTouchAreaLocalY) {
 				if (pSceneTouchEvent.isActionDown()) {
 					SceneManager.getInstance().loadGameMapScene(engine);
 					return true;
 				}
 				return false;
 			}
 
 		});
 	}
 
 	private void removeSkipIntroButton() {
 		skipIntroText.detachSelf();
 		if (!skipIntroText.isDisposed()) {
 			skipIntroText.dispose();
 		}
 
 		skipIntroButton.detachSelf();
 		if (!skipIntroButton.isDisposed()) {
 			skipIntroButton.dispose();
 		}
 	}
 
 	private void registerSceneTouchListener() {
 		this.setOnSceneTouchListener(new IOnSceneTouchListener() {
 
 			@Override
 			public boolean onSceneTouchEvent(Scene pScene,
 					TouchEvent pSceneTouchEvent) {
 				if (pSceneTouchEvent.isActionDown()) {
 					// Show the GameMapScene if last entity was touched, else
 					// show the next one.
 					if (gameIntroEntities.get(gameIntroEntities.size() - 1)
 							.hasParent()) {
 						IEntity oldEntity = gameIntroEntities
 								.get(gameIntroEntities.size() - 1);
 						oldEntity.registerEntityModifier(new FadeOutModifier(
 								FADE_OUT_DURATION));
 						detachViaUpdateHandlerAfterTime(oldEntity,
 								FADE_OUT_DURATION);
 
 						SceneManager.getInstance().loadGameMapScene(engine);
 						return true;
 					} else if (gameIntroEntities.get(nextEntity - 1)
 							.hasParent()) {
 						IEntity oldEntity = gameIntroEntities
 								.get(nextEntity - 1);
 						oldEntity.registerEntityModifier(new FadeOutModifier(
 								FADE_OUT_DURATION));
 						detachViaUpdateHandlerAfterTime(oldEntity,
 								FADE_OUT_DURATION);
 
 						if (nextEntity == 1) {
 							removeSkipIntroButton();
 						}
 
 						IEntity newEntity = gameIntroEntities.get(nextEntity);
 						attachChild(newEntity);
 						newEntity.registerEntityModifier(new FadeInModifier(
 								FADE_IN_DURATION));
 						nextEntity++;
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
 		return SceneType.SCENE_INTRO;
 	}
 
 	@Override
 	public void disposeScene() {
 		for (IEntity entity : gameIntroEntities) {
 			if (entity.hasParent()) {
 				entity.detachSelf();
 			}
 			entity.clearEntityModifiers();
 			if (!entity.isDisposed()) {
 				entity.dispose();
 			}
 		}
 
 		removeSkipIntroButton();
 
 		this.detachSelf();
 		if (!this.isDisposed()) {
 			this.dispose();
 		}
 	}
 
 }
