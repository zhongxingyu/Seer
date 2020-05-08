 package com.tacoid.puyopuyo;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 
 public class PuyoPuyo extends Game {
 
 	private static PuyoPuyo instance = null;
 	public AssetManager manager;
 	public TextureAtlas atlasPuyo;
 	public TextureAtlas atlasControls;
 	public TextureAtlas atlasPlank;
 	public TextureAtlas atlasPanelsLandscape;
 	public TextureAtlas atlasPanelsPortrait;
 	public TextureAtlas atlasBouttons;
 	private boolean desktopMode;
 	public static final int VIRTUAL_WIDTH = 1280;
 	public static final int VIRTUAL_HEIGHT = 768;
 	private LoadingScreen loadingScreen;
 	private boolean loaded = false;
 	
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
 		
 	public void render() {
 		/* Si update renvoi true, c'est que tout est chargé, on a plus qu'à afficher le screen qu'on veut. Sinon, on affiche le screen de chargement */
 		if (manager.update()) {
			if (getScreen() == null) {
 				setScreen(LanguageScreen.getInstance());
 			} else if (!loaded){
 				getScreen().show();
 			}
 			loaded = true;
 			super.render();
 		} else {
 			if (loadingScreen != null) loadingScreen.render(Gdx.graphics.getDeltaTime());
 			loaded = false;
 		}
 	}
 	
 	@Override
 	public void create() {		
 		loadingScreen = LoadingScreen.getInstance();
 		manager = new AssetManager();
 	
 		Gdx.input.setCatchBackKey(true);
 		
 		loadAssets();
 	}
 	
 	private void loadAssets() {
 
 		/* fonts */
 		manager.load("images/font_score.fnt", BitmapFont.class);
 		manager.load("images/font_level.fnt", BitmapFont.class);
 		
 		/* Textures du menu */
 		manager.load("images/menu/flag-fr.png", Texture.class);
 		manager.load("images/menu/flag-en.png", Texture.class);
 		manager.load("images/menu/sky.png", Texture.class);
 		manager.load("images/menu/sky-portrait.png", Texture.class);
 		manager.load("images/menu/hills.png", Texture.class);
 		manager.load("images/menu/logo.png", Texture.class);
 		
 		atlasPuyo = new TextureAtlas(Gdx.files.internal("images/puyos/pages.atlas"));
 		atlasControls = new TextureAtlas(Gdx.files.internal("images/controls/pages.atlas"));
 		atlasPanelsLandscape = new TextureAtlas(Gdx.files.internal("images/panels/landscape/pages.atlas"));
 		atlasPanelsPortrait = new TextureAtlas(Gdx.files.internal("images/panels/portrait/pages.atlas"));
 		atlasBouttons = new TextureAtlas(Gdx.files.internal("images/bouttons/pages.atlas"));
 		
 		/*** Son ***/
 		manager.load("sounds/bleep.wav", Sound.class);
 		manager.load("sounds/bleep2.wav", Sound.class);
 		manager.load("sounds/explode.wav", Sound.class);
 		manager.load("sounds/click.wav", Sound.class);
 		
 		manager.load("sounds/AnoyingMusic.mp3", Music.class);
 		
 	}
 
 	public void loadLocalizedAssets() {
 		atlasPlank = new TextureAtlas(Gdx.files.internal("images/menu/plank-fr/pages.atlas"));
 	}
 
 	public void setDesktopMode(boolean b) {
 		this.desktopMode = b;
 	}
 	
 	public boolean getDesktopMode() {
 		return this.desktopMode;
 	}
 }
