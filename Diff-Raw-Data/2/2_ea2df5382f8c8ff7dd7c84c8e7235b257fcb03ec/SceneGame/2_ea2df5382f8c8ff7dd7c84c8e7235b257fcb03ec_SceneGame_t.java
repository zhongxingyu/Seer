 /*
  * Copyright (C) 2013 Andrew Tulay
  * @mail: mhyhre@gmail.com
  * 
  * This work is licensed under a Creative Commons 
  * Attribution-NonCommercial-NoDerivs 3.0 Unported License.
  * You may obtain a copy of the License at
  *
  *		http://creativecommons.org/licenses/by-nc-nd/3.0/legalcode
  *
  */
 
 package mhyhre.lightrabbit.Scenes;
 
 import java.util.ArrayList;
 import mhyhre.lightrabbit.GameState;
 import mhyhre.lightrabbit.MainActivity;
 import mhyhre.lightrabbit.MhyhreScene;
 
 import org.andengine.entity.scene.background.Background;
 import org.andengine.entity.sprite.Sprite;
 import org.andengine.input.touch.TouchEvent;
 import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
 import org.andengine.opengl.texture.region.ITextureRegion;
 import org.andengine.opengl.texture.region.TextureRegionFactory;
 
 import android.util.Log;
 
 public class SceneGame extends MhyhreScene {
 
 	private Background mBackground;
 
 	private static GameState mode = GameState.Ready;
 	private boolean loaded = false;
 
 	Sprite spriteNext, boat;
 
 	private WaterPolygon water;
 
 	// Resources
 	public static final String uiAtlasName = "User_Interface";
 
 	public static ArrayList<ITextureRegion> TextureRegions;
 
 	public SceneGame() {
 
 		mBackground = new Background(0.40f, 0.88f, 0.99f);
 		setBackground(mBackground);
 		setBackgroundEnabled(true);
 
 		CreateTextureRegions();
 
 		water = new WaterPolygon(16, MainActivity.Me.getVertexBufferObjectManager());
 		this.attachChild(water);
 		
 		
 		boat = new Sprite(100, 100, MainActivity.Res.getTextureRegion("boat_body"), MainActivity.Me.getVertexBufferObjectManager());
 		attachChild(boat);
 
 		spriteNext = new Sprite(MainActivity.getWidth() - (10 + TextureRegions.get(3).getWidth()), 10, TextureRegions.get(3), MainActivity.Me.getVertexBufferObjectManager()) {
 			@Override
 			public boolean onAreaTouched(TouchEvent pSceneTouchEvent, float pTouchAreaLocalX, float pTouchAreaLocalY) {
 
 				if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_UP) {
 
 					MainActivity.vibrate(30);
 
 					switch (mode) {
 
 					case Ready:
 						setGameState(GameState.Memorize);
 						break;
 
 					case Memorize:
 						setGameState(GameState.Recollect);
 						break;
 
 					case Recollect:
 						setGameState(GameState.Result);
 						break;
 
 					case Result:
 						setGameState(GameState.Ready);
 						break;
 
 					case Loss:
 						SceneRoot.SetState(SceneStates.MainMenu);
 						break;
 					}
 				}
 				return true;
 			}
 		};
 		spriteNext.setVisible(true);
 		attachChild(spriteNext);
 
 		loaded = true;
 
 		setGameState(GameState.Ready);
 
 		Log.i(MainActivity.DebugID, "Scene game created");
 	}
 
 	private static void CreateTextureRegions() {
 
 		TextureRegions = new ArrayList<ITextureRegion>();
 		BitmapTextureAtlas atlas = MainActivity.Res.getTextureAtlas(uiAtlasName);
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 0, 0, 310, 70, false));
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 325, 0, 45, 70, false));
 
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 0, 70, 74, 74, false));
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 80, 70, 74, 74, false));
 
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 160, 70, 74, 74, false));
 
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 390, 0, 120, 384, false));
 		TextureRegions.add(TextureRegionFactory.extractFromTexture(atlas, 460, 0, 4, 384, false));
 	}
 
 	@Override
 	public boolean onSceneTouchEvent(final TouchEvent pSceneTouchEvent) {
 
 		if (loaded == true) {
 
 			switch (mode) {
 
 			case Ready:
 
 				break;
 
 			case Memorize:
 
 				break;
 
 			default:
 				break;
 			}
 		}
 
 		return super.onSceneTouchEvent(pSceneTouchEvent);
 	}
 
 	@Override
 	protected void onManagedUpdate(final float pSecondsElapsed) {
 
 		
		boat.setY(water.getYPositionOnWave(boat.getX() + boat.getWidth()/2)-boat.getHeight()/2 - 5);
 		boat.setRotation(water.getAngleOnWave(boat.getX())/2.0f);
 		
 		super.onManagedUpdate(pSecondsElapsed);
 	}
 
 	public void setGameState(GameState mode) {
 
 		if (loaded == false) {
 			return;
 		}
 
 		Log.i(MainActivity.DebugID, "SceneGame::setGameState: " + mode.name());
 
 		SceneGame.mode = mode;
 
 		switch (mode) {
 
 		case Ready:
 			break;
 
 		case Memorize:
 			break;
 
 		default:
 			break;
 		}
 	}
 
 	public static GameState getMode() {
 		return mode;
 	}
 
 }
