 package com.tacoid.puyopuyo;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 
 public class PuyoPuyo extends Game {
 
 	private static PuyoPuyo instance = null;
 	public AssetManager manager;
 	public TextureAtlas atlasPuyo;
 	public TextureAtlas atlasControls;
 	public TextureAtlas atlasPlank;
 	public TextureAtlas atlasPanelsLandscape;
 	public TextureAtlas atlasPanelsPortrait;
 	public static final int VIRTUAL_WIDTH = 1280;
 	public static final int VIRTUAL_HEIGHT = 768;
 	
 	public enum ScreenOrientation {
 		LANDSCAPE,
 		PORTRAIT
 	};
 	
 	private PuyoPuyo() {}
 	
 	public static PuyoPuyo getInstance() {
 		if (instance == null) {
 			instance = new PuyoPuyo();
 		}
 		return instance;
 	}
 	
 	@Override
 	public void create() {
 		setScreen(LoadingScreen.getInstance());
 	
 		Gdx.input.setCatchBackKey(true);
 		
 		loadAssets();
 	}
 
 	private void loadAssets() {
 		
 		/*** Textures ***/
 		
 		manager = new AssetManager();
 
 		manager.load("images/fond_solo.png", Texture.class);
 		
 		manager.load("images/perdu-fr.png", Texture.class);
 		manager.load("images/gagne-fr.png", Texture.class);
 		manager.load("images/continuer-fr.png", Texture.class);
 		manager.load("images/quitter-fr.png", Texture.class);
 		
 		manager.load("images/pause_button.png", Texture.class);
 		manager.load("images/music-on.png", Texture.class);
 		manager.load("images/music-off.png", Texture.class);
 		
 		/* Textures du menu */
 		manager.load("images/menu/sky.png", Texture.class);
		manager.load("images/menu/sky-portrait.png", Texture.class);
 		manager.load("images/menu/hills.png", Texture.class);
 		manager.load("images/menu/foreground.png", Texture.class);
 		
 		atlasPuyo = new TextureAtlas(Gdx.files.internal("images/puyos/pages.atlas"));
 		atlasControls = new TextureAtlas(Gdx.files.internal("images/controls/pages.atlas"));
 		atlasPlank = new TextureAtlas(Gdx.files.internal("images/menu/plank-fr/pages.atlas"));
 		atlasPanelsLandscape = new TextureAtlas(Gdx.files.internal("images/panels/landscape/pages.atlas"));
 		atlasPanelsPortrait = new TextureAtlas(Gdx.files.internal("images/panels/portrait/pages.atlas"));
 		
 		/*** Son ***/
 		manager.load("sounds/bleep.wav",Sound.class);
 	}
 }
