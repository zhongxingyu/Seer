 //***Main entry of the libgdx-project****
 package it.chalmers.tendu;
 
 import it.chalmers.tendu.controllers.InputController;
 import it.chalmers.tendu.defaults.Constants;
 import it.chalmers.tendu.gamemodel.MiniGame;
 import it.chalmers.tendu.gamemodel.Player;
 import it.chalmers.tendu.gamemodel.SessionResult;
 import it.chalmers.tendu.network.INetworkHandler;
 import it.chalmers.tendu.screens.GameOverScreen;
 import it.chalmers.tendu.screens.InterimScreen;
 import it.chalmers.tendu.screens.MainMenuScreen;
 import it.chalmers.tendu.screens.MiniGameScreenFactory;
 import it.chalmers.tendu.screens.Screen;
 import it.chalmers.tendu.tbd.C;
 import it.chalmers.tendu.tbd.EventBus;
 import it.chalmers.tendu.tbd.EventMessage;
 import it.chalmers.tendu.tbd.Listener;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 
 
 public class Tendu implements ApplicationListener, Listener {
 
 	public static final String TAG = "Tendu"; // Tag for logging
 
 	private Screen screen; // contains whats shown on device screen in any
 							// given moment. Changes depending current
 							// minigame or if in a menu etc
 	private float accum = 0; // used to help lock frame rate in 60 frames per
 								// second
 	private InputController input; // used for handling input (obviously)
 	private OrthographicCamera camera; // The use of a camera helps us to work
 										// on one screen size no matter the
 										// actual screen sizes of different
 										// devices
 
 	private INetworkHandler networkHandler; // handle to all network related
 											// stuff (Android specific, at least
 											// for now)
 	public SpriteBatch spriteBatch; // used for drawing of graphics
 
 	public Tendu(INetworkHandler networkHandler) {
 		setNetworkHandler(networkHandler);
 		EventBus.INSTANCE.addListener(this);
 	}
 
 	@Override
 	public void create() {
 		String mac = networkHandler.getMacAddress();
 		Player.getInstance().setMac(mac);
 		Gdx.app.log(TAG, Player.getInstance().getMac());
 
 		spriteBatch = new SpriteBatch();
 
 		 setScreen(new MainMenuScreen(this));
 
 
 		// setup the camera
 		camera = new OrthographicCamera();
 		camera.setToOrtho(false, Constants.SCREEN_WIDTH,
 				Constants.SCREEN_HEIGHT);
 
 		// create an inputController and register it with Gdx
 		input = new InputController(camera);
 		Gdx.input.setInputProcessor(input);
 	}
 
 	// clean up
 	@Override
 	public void dispose() {
 		spriteBatch.dispose();
 		networkHandler.destroy();
 	}
 
 	// **The games main loop, everything but early setup happens here
 	@Override
 	public void render() {
 
 		// clear the entire screen
 		// setScreenByNetworkState(); //changes to some error screen if
 		// connections is lost?
 		// clear the entire screen
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 	    //Gdx.gl.glClearColor(0.12f, 0.6f, 0.98f, 1);
 	    //Gdx.gl.glClearColor(1f, 1f, 0f, 1);
 	    //Gdx.gl.glClearColor(1f, 1f, 1f, 1);
 
 		// makes sure the game runs in 60 fps
 		accum += Gdx.graphics.getDeltaTime();
 		while (accum > 1.0f / 60.0f) {
 			screen.tick(input); // runs tick in the current screen witch should
 								// handle all input and game logic for that
 								// specific minigame/menu
 			input.tick(); // updates input
 			accum -= 1.0f / 60.0f;
 		}
 
 		camera.update();
 		spriteBatch.setProjectionMatrix(camera.combined);
 		spriteBatch.begin();
 		screen.render(); // draw all graphic for the current frame
 		spriteBatch.end();
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
 
 	// sets a new screen and cleans up the previous one
 	public void setScreen(Screen newScreen) {
 		if (screen != null) {
 			screen.removed();
 		}
 		screen = newScreen;
 	}
 
 	// the screens need access to the camera to handle translations between
 	// actual screen pixels and our defined in game pixels
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
 
 	@Override
 	public void onBroadcast(EventMessage message) {
 		if(message.tag == C.Tag.TO_SELF){
 			
 		
 		if (message.msg == C.Msg.CREATE_SCREEN) {
 			MiniGame game = (MiniGame) message.content;
 			Screen screen = MiniGameScreenFactory.createMiniGameScreen(this,
 					game);
 			setScreen(screen);
 
 			EventMessage msg = new EventMessage(C.Tag.TO_SELF,
 					C.Msg.WAITING_TO_START_GAME, Player.getInstance().getMac());
 			EventBus.INSTANCE.broadcast(msg);
 			
 		} else if (message.msg == C.Msg.SHOW_INTERIM_SCREEN) {
 			SessionResult sessionResult = (SessionResult)message.content;
 			Screen screen = new InterimScreen(this, sessionResult);
 			setScreen(screen);
 			
 		} else if (message.msg == C.Msg.SHOW_GAME_OVER_SCREEN){
 			SessionResult sessionResult = (SessionResult)message.content;
 			Screen screen = new GameOverScreen(this, sessionResult);
 			setScreen(screen);
			networkHandler.resetNetwork();
 			
 		} else if (message.msg == C.Msg.RESTART){
 			// TODO: Unregister network
 			Screen screen = new MainMenuScreen(this);
 			setScreen(screen);
 			
 		} else if (message.msg == C.Msg.STOP_ACCEPTING_CONNECTIONS);
 			networkHandler.stopAcceptingConnections();
 		}
 	}
 
 	@Override
 	public void unregister() {
 		// TODO: Will this ever be called? ( maybe on dispose() )
 		EventBus.INSTANCE.removeListener(this);
 	}
 }
