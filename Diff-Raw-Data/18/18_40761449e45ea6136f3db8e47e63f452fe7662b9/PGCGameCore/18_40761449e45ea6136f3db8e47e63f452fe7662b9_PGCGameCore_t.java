 package com.buildcoolrobots.games.pgcgame;
 
 import me.pagekite.glen3b.gjlib.SpriteManager;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BGSprite;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.BaseScreen;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.StateManager;
 import com.buildcoolrobots.games.pgcgame.CoreTypes.Enums.*;
 import com.buildcoolrobots.games.pgcgame.Screens.MainMenu;
 import com.buildcoolrobots.games.pgcgame.Screens.TitleScreen;
 
 public class PGCGameCore implements ApplicationListener {
 	private OrthographicCamera camera;
 	private SpriteBatch batch;
 	
 	@Override
 	public void create() {		
 		float width = Gdx.graphics.getWidth();
 		float height = Gdx.graphics.getHeight();
 		
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, width, height);
 		batch = new SpriteBatch();
 		
 		loadContent();
 				
 		
 		
 		TitleScreen titleScreen = new TitleScreen(new SpriteManager(), batch, ScreenType.TITLESCREEN);
 		MainMenu mainMenuScreen = new MainMenu(new SpriteManager(), batch, ScreenType.MAINMENU);
 
 		mainMenuScreen.hide();
 		
 	}
 
 	@SuppressWarnings("unused")
 	private void loadContent() {
 		//Pre-load images
 		for(IGameObject gameObject : ShipTypes.values()) {
 			//Looping through the enum values causes type instantiation and texture load
 		}
 		for(IGameScreen gameScreen : ScreenType.values()) {
 			//Looping through the enum values causes type instantiation and texture load
 		}
 		for(IGameLevel gameLevel : GameLevel.values()) {
 			//Looping through the enum values causes type instantiation and texture load
 		}
 	}
 	
 	@Override
 	public void dispose() {
 		batch.dispose();
 	}
 
 	private void update() {
 		//Update all created screens; if screen is not active, it is the screen's responsibility to not run update unnecessarily
 		for(BaseScreen screen : StateManager.AllScreens.getAllScreens()) {
 			screen.update(Gdx.graphics.getDeltaTime());
 		}
 				
 		if(StateManager.DebugData.AllowScreenSwitching) {
 			if(Gdx.input.isKeyPressed(Input.Keys.F1)) {
 				StateManager.SwitchScreen(ScreenType.TITLESCREEN);
			} else if (Gdx.input.isKeyPressed(Input.Keys.F2)) {
 				StateManager.SwitchScreen(ScreenType.MAINMENU);
 			}				
 		}		
 	}
 	
 	private void draw() {
 		Gdx.gl.glClearColor(1, 1, 1, 1);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);	
 		
 		batch.setProjectionMatrix(camera.combined);		
 			
 		batch.begin();		
 
 		//Update all created screens; if screen is not active, it is the screen's responsibility to not run update unnecessarily
 		for(BaseScreen screen : StateManager.AllScreens.getAllScreens()) {
 			screen.draw();
 		}
 		
 		batch.end();
 		
 	}
 	
 	@Override
 	public void render() {
 		update();
 		draw();
 		
 		BGSprite.scrollingBackgroundUpdate();
 	}
 	
 	
 	
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() {
 	}
 }
