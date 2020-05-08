 package com.tacoid.puyopuyo.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.GL20;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.tacoid.puyopuyo.I18nManager;
 import com.tacoid.puyopuyo.PuyoPuyo;
 import com.tacoid.puyopuyo.I18nManager.Language;
 import com.tacoid.puyopuyo.PuyoPuyo.ScreenOrientation;
 import com.tacoid.puyopuyo.actors.BackgroundActor;
 
 public class LanguageScreen implements Screen {
 	
 	private static final int VIRTUAL_WIDTH = 1280;
 	private static final int VIRTUAL_HEIGHT = 768;
 	private static LanguageScreen instance = null;
 	
 	private Stage stage;
 	private PuyoPuyo puyopuyo = null;
 	
 	private class LangButton extends Button implements ClickListener{
 
 		I18nManager.Language lang;
 		
 		public LangButton(TextureRegion region, I18nManager.Language lang) {
 			super(region);
 			this.lang = lang;
 			setClickListener(this);
 		}
 
 		@Override
 		public void click(Actor arg0, float arg1, float arg2) {
 			I18nManager.getInstance().setLanguage(lang);
 			puyopuyo.loadLocalizedAssets();
 			MainMenuScreen.getInstance().init();
			GameVersusScreen.getInstance().initGraphics();
			GameSoloScreen.getInstance().initGraphics();
			GameTimeAttackScreen.getInstance().initGraphics();
 			puyopuyo.setScreen(MainMenuScreen.getInstance());
 		}
 		
 	}
 	
 	public static LanguageScreen getInstance() {
 		if(instance == null) {
 			instance = new LanguageScreen();
 		}
 		return instance;
 	}
 	
 	private LanguageScreen() {
 		
 		this.puyopuyo = PuyoPuyo.getInstance();
 		TextureRegion enRegion = new TextureRegion( puyopuyo.manager.get("images/menu/flag-en.png", Texture.class));
 		TextureRegion frRegion = new TextureRegion( puyopuyo.manager.get("images/menu/flag-fr.png", Texture.class));
 		LangButton enButton = new LangButton(enRegion, Language.ENGLISH);
 		LangButton frButton = new LangButton(frRegion, Language.FRENCH);
 		
 		this.puyopuyo = PuyoPuyo.getInstance();
 		stage = new Stage(Gdx.graphics.getWidth(), Gdx.graphics.getHeight(),
 				false);
 		
 		/* Positionnement des bouttons aux premier tier et deuxieme tier de l'�cran */
 		enButton.x = 2*VIRTUAL_WIDTH/3 - enRegion.getRegionWidth()/2;
 		enButton.y = VIRTUAL_HEIGHT/3 - enRegion.getRegionHeight()/2;
 		frButton.x = VIRTUAL_WIDTH/3 - frRegion.getRegionWidth()/2;
 		frButton.y = VIRTUAL_HEIGHT/3 - enRegion.getRegionHeight()/2;
 		
 		stage.addActor(new BackgroundActor(ScreenOrientation.LANDSCAPE));
 		stage.addActor(enButton);
 		stage.addActor(frButton);
 		
 		//stage.addActor(new loadingActor());
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render(float arg0) {
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
 		
 		stage.draw();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 	}
 
 	@Override
 	public void resize(int arg0, int arg1) {
 		stage.setViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, false);
 		stage.getCamera().position.set(VIRTUAL_WIDTH / 2, VIRTUAL_HEIGHT / 2, 0);
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void show() {
 		Gdx.input.setInputProcessor(stage);
 	}
 
 }
