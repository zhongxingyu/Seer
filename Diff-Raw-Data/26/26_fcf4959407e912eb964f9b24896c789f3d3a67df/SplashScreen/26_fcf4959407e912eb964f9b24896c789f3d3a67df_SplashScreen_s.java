 package com.pix.mind.screens;
 
 import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.pix.mind.PixMindGame;
import com.pix.mind.levels.FirstLevel;
import com.pix.mind.levels.Level01;
 
 public class SplashScreen implements  Screen{
 	private PixMindGame game;
 	private Skin splashSkin;
 	private AssetManager assetManagerSplash;
 	private Stage stageSplash;
 	
 	public SplashScreen(PixMindGame game) {
		// TODO Auto-generated constructor stub
 		this.game = game;

 	}
 	
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 
 		Gdx.gl.glClearColor(0, 0, 0, 1); 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); 
 
 		stageSplash.draw();
 		
 		// waiting for it to finish loading the game atlas
 		if (game.getAssetManager().update()) {
 	
 			PixMindGame.setSkin(new Skin(game.getAssetManager().get(
 				"data/textureatlas/PixmindTextureAtlas.pack", TextureAtlas.class)));
 
 			PixMindGame.setMusic(game.getAssetManager().get("data/music/smlo.mp3", Music.class));
 			PixMindGame.setBoing(game.getAssetManager().get("data/sounds/boing.wav", Sound.class));
 			PixMindGame.setGettingActivator(game.getAssetManager().get("data/sounds/gettingactivator.wav", Sound.class));
 			PixMindGame.setWinning(game.getAssetManager().get("data/sounds/winning.wav", Sound.class));			
 			PixMindGame.setLosing(game.getAssetManager().get("data/sounds/losing.wav", Sound.class));
 			PixMindGame.setLosing(game.getAssetManager().get("data/sounds/losing.wav", Sound.class));
 			PixMindGame.setMenuClick(game.getAssetManager().get("data/sounds/menuclick.mp3", Sound.class));
 			PixMindGame.setFont(game.getAssetManager().get("data/fonts/sweetmindfont.fnt", BitmapFont.class));
 
             game.setScreen(game.getMainMenuScreen());
//			game.setScreen(game.getLevel11());
 		}
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 	@Override
 	public void show() {
 		// TODO Auto-generated method stub	
 		assetManagerSplash = new AssetManager();
 		assetManagerSplash.load("data/textureatlas/SplashTextureAtlas.pack", TextureAtlas.class);
 		assetManagerSplash.finishLoading();
 
 		splashSkin = new Skin(assetManagerSplash.get("data/textureatlas/SplashTextureAtlas.pack", TextureAtlas.class));
 		
 		// loading game atlas , music, etc...
 		game.getAssetManager().load("data/textureatlas/PixmindTextureAtlas.pack", TextureAtlas.class);
 		game.getAssetManager().load("data/music/smlo.mp3", Music.class);
 		game.getAssetManager().load("data/sounds/boing.wav", Sound.class);
 		game.getAssetManager().load("data/sounds/gettingactivator.wav", Sound.class);
 		game.getAssetManager().load("data/sounds/winning.wav", Sound.class);
 		game.getAssetManager().load("data/sounds/losing.wav", Sound.class);
 		game.getAssetManager().load("data/sounds/menuclick.mp3", Sound.class);
 		game.getAssetManager().load("data/fonts/sweetmindfont.fnt", BitmapFont.class);
 		
 		
 		
 		
 		stageSplash = new Stage(PixMindGame.w, PixMindGame.h, true);
 		
 		Image modelsheep = new Image(splashSkin.getDrawable("modelsheep"));	
 		Image g3 = new Image(splashSkin.getDrawable("g3"));		
 		modelsheep.setPosition((PixMindGame.w)/2 - modelsheep.getWidth()/2 -150, 240-modelsheep.getHeight()/2);
 		g3.setPosition((PixMindGame.w)/2 - g3.getWidth()/2 +150, 240-g3.getHeight()/2);
 		
 		stageSplash.addActor(g3);
 		stageSplash.addActor(modelsheep);
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
 	public void resume() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 		assetManagerSplash.dispose();	
 		splashSkin.dispose();
 		stageSplash.dispose();
 	  
 	}
 
 }
