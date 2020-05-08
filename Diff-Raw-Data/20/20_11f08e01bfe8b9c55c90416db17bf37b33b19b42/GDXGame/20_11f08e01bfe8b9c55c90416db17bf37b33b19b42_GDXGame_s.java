 package se.chalmers.dat255.risk;
 
 import se.chalmers.dat255.risk.controller.ScreenManager;
 
 import com.badlogic.gdx.Game;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.FPSLogger;
 
 public class GDXGame extends Game {
 	FPSLogger logger;
 	ScreenManager manager;
 
 	@Override
 	public void create() {
 	
 		Gdx.app.log("Risk", "creating game");
 	
 		logger = new FPSLogger();
 		manager = ScreenManager.getInstance();
 		manager.instantiate(this);
 		manager.changeScreen();
 		
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void render() {
 		super.render();
 		// Gdx.app.log("Risk", "RenderingGame");
		logger.log();
 	}
 
 	@Override
 	public void pause() {
 		Gdx.app.log("Risk", "Pausing game");
 	}
 
 	@Override
 	public void resume() {
 		Gdx.app.log("Risk", "Resuming game");
 
 	}
 
 	@Override
 	public void dispose() {
 		Gdx.app.log("Risk", "Destroying game");
 		manager.dispose();
 	}
 
 }
