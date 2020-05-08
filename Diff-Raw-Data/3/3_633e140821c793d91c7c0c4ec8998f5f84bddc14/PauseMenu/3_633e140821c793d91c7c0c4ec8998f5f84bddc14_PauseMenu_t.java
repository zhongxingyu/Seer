 package com.tacoid.pweek.actors;
 
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.InputEvent;
 import com.badlogic.gdx.scenes.scene2d.InputListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 import com.tacoid.pweek.IActivityRequestHandler;
 import com.tacoid.pweek.SoundPlayer;
 import com.tacoid.pweek.Pweek.ScreenOrientation;
 import com.tacoid.pweek.SoundPlayer.SoundType;
 import com.tacoid.pweek.screens.GameScreen;
 
 public class PauseMenu extends Group {
 	
 	private IActivityRequestHandler handler;
 	private GameScreen gameScreen;
 	
 	private SwingMenu menu;
 	
 	private class ContinueButton extends Button {
 		public ContinueButton(TextureRegion region) {
 			super(new TextureRegionDrawable(region));
 			setX(100);
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					hide();
 				}
 			});
 		}
 	}
 	
 	private class QuitButton extends Button{
 		public QuitButton(TextureRegion region) {
 			super(new TextureRegionDrawable(region));
 			setX(800);
 			addListener(new InputListener() {
 				@Override
 				public boolean touchDown(InputEvent event, float x, float y,
 						int pointer, int button) {
 					SoundPlayer.getInstance().playSound(SoundType.TOUCH_MENU, 0.5f, true);
 					return true;
 				}
 				
 				@Override
 				public void touchUp(InputEvent event, float x, float y,
 						int pointer, int button) {
 					gameScreen.quit();
 				}
 			});
 		}
 	}
 	
 	public PauseMenu(TextureAtlas atlasPlank, GameScreen gameScreen, IActivityRequestHandler handler, ScreenOrientation orientation, boolean hidden) {
 		TextureRegion continueRegion = atlasPlank.findRegion("continuer");
 		TextureRegion quitRegion = atlasPlank.findRegion("quitter");
 		
 		this.handler = handler;
 		menu = new SwingMenu(orientation);
 		
 		menu.initBegin("pause");
 		menu.addButton(new ContinueButton(continueRegion));
 		menu.addButton(new QuitButton(quitRegion));
 		menu.initEnd();
 		if (hidden) {
 			menu.hide();
 		}
 		
 		this.addActor(menu);
 		
 		this.gameScreen = gameScreen;
 		setVisible(true);
 	}
 	
 	public void show() {
 		handler.showAds(true);
 		gameScreen.gamePause();
 		menu.show("pause");
 	}
 	
 	public void hide() {
		handler.showAds(false);
 		gameScreen.gameResume();
 		menu.hideInstant();
 	}
 }
