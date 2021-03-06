 //***Main entry of the libgdx-project****
 package it.chalmers.tendu;
 
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.defaults.Constants.Difficulty;
 import it.chalmers.tendu.gamemodel.numbergame.NumberGame;
 import it.chalmers.tendu.gamemodel.shapesgame.ShapesGame;
 import it.chalmers.tendu.network.INetworkHandler;
 import it.chalmers.tendu.network.NetworkState;
 import it.chalmers.tendu.screens.GameScreen;
 import it.chalmers.tendu.screens.MainMenuScreen;
 import it.chalmers.tendu.screens.NumberGameScreen;
 import it.chalmers.tendu.screens.ShapesGameScreen;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 
 public class Tendu implements ApplicationListener {
 	private GameScreen screen; //contains whats shown on device screen in any given moment. Changes depending current minigame or if in a menu etc
 	private float accum = 0; //used to help lock frame rate in 60 frames per second
 	private InputController input; //used for handling input (obviously)
 	private OrthographicCamera camera; //The use of a camera helps us to work on one screen size no matter the actual screen sizes of different devices
 	
 	private INetworkHandler networkHandler; //handle to all network related stuff (Android specific, at least for now)
 	
 	public Tendu(INetworkHandler netCom) {
 		setNetworkHandler(netCom);
 	}
 
 	@Override
 	public void create() {
 		//here we should load the start screen of the game
 		//setScreenByNetworkState();
 		//setScreen(new MainMenuScreen(this, null));
		setScreen(new NumberGameScreen(this, new NumberGame(0, Constants.Difficulty.ONE)));
 		//setScreen(new ShapesGameScreen(this, new ShapesGame(30000, Constants.Difficulty.ONE)));
 		
 		//create an inputController and register it with Gdx
 		input = new InputController();
 		Gdx.input.setInputProcessor(input);
 
 		//setup the camera
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, Constants.SCREEN_WIDTH,
 				Constants.SCREEN_HEIGHT);
 	}
 
 	//clean up
 	@Override
 	public void dispose() {
 		networkHandler.destroy();
 	}
 	
 	//**The games main loop, everything but early setup happens here
 	@Override
 	public void render() {
 		//setScreenByNetworkState(); //changes to some error screen if connections is lost?
 		//clear the entire screen
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		//makes sure the game runs in 60 fps
 		accum += Gdx.graphics.getDeltaTime();	
 		while (accum > 1.0f / 60.0f) {
 			screen.tick(input); //runs tick in the current screen witch should handle all input and game logic for that specific minigame/menu
 			input.tick(); //updates input
 			accum -= 1.0f / 60.0f;
 		}
 		
 		camera.update();
 		screen.render(); //draw all graphic for the current frame
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
 	
 	//sets a new screen and cleans up the previous one
 	public void setScreen(GameScreen newScreen) {
 		if (screen != null) {
 			screen.removed();
 		}
 		screen = newScreen;
 	}
 	
 	//the screens need access to the camera to handle translations between actual screen pixels and our defined in game pixels
 	public OrthographicCamera getCamera() {
 		return camera;
 	}
 
 	//screens need access to the network
 	public INetworkHandler getNetworkHandler() {
 		return networkHandler;
 	}
 
 	private void setNetworkHandler(INetworkHandler networkHandler) {
 		this.networkHandler = networkHandler;
 	}
 	
 	//TODO unsure sure about this
 	private void setScreenByNetworkState() {
 		int state = networkHandler.pollNetworkState();
 		// Change screen depending on network state (Maybe not the proper place for this)
 		switch (state) {
 		case NetworkState.STATE_NONE: 
 			if (screen instanceof NumberGameScreen) {
 				setScreen(new MainMenuScreen(this, null));
 			}
 			break;
 		case NetworkState.STATE_CONNECTED: 
 			if (screen instanceof MainMenuScreen) {
 				setScreen(new NumberGameScreen(this, new NumberGame(0, Constants.Difficulty.ONE)));
 		}
 			break;
 		}
 		
 					
 	}
 }
