 package com.pix.mind.screens;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.pix.mind.PixMindGame;
 
 public class SplashScreen implements  Screen{
 	private PixMindGame game;
 	private Skin splashSkin;
 	private AssetManager assetManagerSplash;
 	private Stage stageSplash;
 	
 	public SplashScreen(PixMindGame game) {
 		this.game = game;
 	}
 	float time =0;
 	@Override
 	public void render(float delta) {
 		// TODO Auto-generated method stub
 		time = time + delta;
 		Gdx.gl.glClearColor(0, 0, 0, 1); 
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT); 
 
 		stageSplash.draw();
 		
 		// waiting for it to finish loading the game atlas
		if (game.getAssetManager().update() && time>0) {
 	
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
 			PixMindGame.setFontLevels(game.getAssetManager().get("data/fonts/sweetmindfont1.fnt", BitmapFont.class));
 
             game.setScreen(game.getMainMenuScreen());
 
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
 		game.getAssetManager().load("data/fonts/sweetmindfont1.fnt", BitmapFont.class);
 		
 		
 		
 		
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
