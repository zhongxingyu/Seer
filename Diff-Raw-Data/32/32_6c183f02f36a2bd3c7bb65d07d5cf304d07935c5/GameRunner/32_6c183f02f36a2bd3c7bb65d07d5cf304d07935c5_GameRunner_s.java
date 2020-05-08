 package com.vyorkin.engine;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.assets.loaders.resolvers.InternalFileHandleResolver;
 import com.badlogic.gdx.graphics.FPSLogger;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.maps.tiled.TiledMap;
 import com.badlogic.gdx.maps.tiled.TmxMapLoader;
 import com.vyorkin.engine.common.MouseCursor;
 import com.vyorkin.engine.renderers.DiagnosticsRenderer;
 import com.vyorkin.engine.renderers.LoadingRenderer;
 import com.vyorkin.engine.renderers.TextLoadingRenderer;
 import com.vyorkin.engine.screens.GameScreen;
 import com.vyorkin.engine.screens.LoadingScreen;
 import com.vyorkin.engine.services.LocaleManager;
 import com.vyorkin.engine.services.MusicManager;
 import com.vyorkin.engine.services.PreferencesManager;
 import com.vyorkin.engine.services.SoundManager;
 
 public abstract class GameRunner extends Game {
 	private FPSLogger fpsLogger;
 	private LoadingRenderer loading;
 	private DiagnosticsRenderer diagnostics;
 	private MouseCursor cursor;
 	private GameScreen nextScreen;
 	
 	@Override
 	public void create() {
 		this.nextScreen = null;
 		
 		E.settings = getSettings();
 		
 		E.preferences = new PreferencesManager();
 		E.assets = new AssetManager();
 		E.font = new BitmapFont();
 		E.batch = new SpriteBatch();
 		E.locales = new LocaleManager();
 		E.music = new MusicManager();
 		E.sounds = new SoundManager();
 		
 		this.loading = new TextLoadingRenderer(
 			new Runnable() {
 				@Override
 				public void run() {
 					setScreen(nextScreen);
 				}
 			}
 		);
 		
 		this.diagnostics = new DiagnosticsRenderer();
 		this.fpsLogger = new FPSLogger();
 
 		E.assets.setLoader(TiledMap.class, 
 			new TmxMapLoader(new InternalFileHandleResolver()));
 		Texture.setAssetManager(E.assets);
 		
 		initialize();
 		
		this.cursor = new MouseCursor();
 		
 		this.nextScreen = getNextScreen(null);
 		this.nextScreen.load();
 	}
 	
 	@Override
 	public void setScreen(Screen screen) {
 		super.setScreen(screen);
 		
 		E.log("Setting screen: " + screen.getClass().getSimpleName());
 	}
 	
 	@Override
 	public void resize(int width, int height) {
 		super.resize(width, height);
 		
 		E.log(String.format(
 			"Resizing to: %dx%d", width, height));
 	}
 	
 	@Override
 	public void render() {
 		float delta = Gdx.graphics.getDeltaTime();
 		GameScreen currentScreen = (GameScreen)getScreen();
 		
 		if (loading.isDone()) {
 			if (currentScreen.isDone()) {
 				currentScreen.dispose();
 				
 				nextScreen = getNextScreen(currentScreen);
 				nextScreen.load();
 				if (E.assets.getQueuedAssets() == 0) {
 					setScreen(nextScreen);
 				}
 			} else {
 				currentScreen.render(delta);
 			}
 		} else if (currentScreen instanceof LoadingScreen) {
 			currentScreen.render(delta);
 		} else {
 			loading.render(delta);
 		}
 		
 		if (E.preferences.isDeveloperMode()) {
 			diagnostics.render(delta);
 			fpsLogger.log();
 		}
 		
		if (currentScreen != null) {
			cursor.draw(currentScreen.getCamera());
		}
 	}
 
 	@Override
 	public void pause() {
 		super.pause();
 		E.log("pausing");
 		E.preferences.save();
 	}
 	
 	@Override
 	public void resume() {
 		super.resume();
 		E.log("Resuming");
 	}
 	
 	@Override 
 	public void dispose() {
 		super.dispose();
 		
 		E.log("Disposing");
 		E.dispose();
 	}
 	
 	public abstract GameSettings getSettings();
 	protected abstract void initialize();
 	protected abstract GameScreen getNextScreen(GameScreen screen);
 }
