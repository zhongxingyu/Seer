 package com.evervoid.client;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.client.ViewManager.ViewTypes;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.GraphicManager;
 import com.evervoid.client.views.galaxy.GalaxyView;
 import com.evervoid.network.connection.ServerConnection;
 import com.evervoid.network.server.EverVoidServer;
 import com.evervoid.state.EverVoidGameState;
 import com.jme3.app.SimpleApplication;
 import com.jme3.input.KeyInput;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.KeyTrigger;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.math.Vector2f;
 import com.jme3.scene.Spatial;
 import com.jme3.system.AppSettings;
 
 /**
  * everVoid game client providing the user with a user interface to play the game.
  */
 public class EverVoidClient extends SimpleApplication implements ActionListener, AnalogListener
 {
 	/**
 	 * Instance of the everVoidClient
 	 */
 	private static EverVoidClient sClient;
 	public static Vector2f sCursorPosition = new Vector2f();
 	protected static EverVoidGameState sGameState;
 	private static final ClientInput sInputManager = new ClientInput();
 	public static int sScreenHeight = 0;
 	public static int sScreenWidth = 0;
 	private static final ViewManager sViewManager = new ViewManager();
 
 	/**
 	 * Attaches the passed Spatial node to the guiNode, which becomes the node's new parent.
 	 * 
 	 * @param node
 	 *            The node to attach to guiNode
 	 * @see Spatial
 	 */
 	public static void addRootNode(final ClientView node)
 	{
 		sViewManager.currentGameView = node;
 		if (node instanceof GalaxyView) {
 			sClient.rootNode.attachChild(node);
 		}
 		else {
 			sClient.guiNode.attachChild(node);
 		}
 	}
 
 	public static void changeView(final ViewTypes type, final Object arg)
 	{
 		addRootNode(sViewManager.getView(type, arg));
 	}
 
 	/**
 	 * everVoid Client program
 	 * 
 	 * @param args
 	 *            Arguments passed to the program.
 	 */
 	public static void main(final String[] args)
 	{
 		sClient = new EverVoidClient();
 		sClient.setShowSettings(false);
 		final AppSettings options = new AppSettings(true);
 		final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		options.setResolution((int) (screenSize.width * .8), (int) (screenSize.height * .8));
 		options.setFullscreen(false);
 		options.setSamples(4);
 		options.setVSync(true);
 		sClient.setSettings(options);
 		sClient.start();
 	}
 
 	public static void setGameState(final EverVoidGameState pState)
 	{
 		sGameState = pState.clone();
 	}
 
	private EverVoidServer aTestServer;

 	/**
 	 * Private constructor for the everVoidClient
 	 */
 	private EverVoidClient()
 	{
 		super();
 		sClient = this;
 	}
 
 	private void init()
 	{
 		sGameState = new EverVoidGameState();
 		sViewManager.createViews(sGameState);
 		addRootNode(sViewManager.currentGameView);
 	}
 
 	@Override
 	public void onAction(final String name, final boolean isPressed, final float tpf)
 	{
 		sInputManager.onAction(sViewManager.currentGameView, name, isPressed, tpf, sCursorPosition);
 	}
 
 	@Override
 	public void onAnalog(final String name, final float delta, final float tpf)
 	{
 		sCursorPosition = inputManager.getCursorPosition();
 		sInputManager.onAnalog(sViewManager.currentGameView, name, delta, tpf, sCursorPosition);
 	}
 
	@Override
	public void requestClose(final boolean esc)
	{
		super.requestClose(esc);
		aTestServer.stop();
	}

 	/**
 	 * Temporary; delete once engine is done.
 	 */
 	void sampleGame()
 	{
 		inputManager.addMapping("Mouse move", new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(
 				MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_Y, false), new MouseAxisTrigger(
 				MouseInput.AXIS_Y, true));
 		inputManager.addMapping("Mouse wheel up", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
 		inputManager.addMapping("Mouse wheel down", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
 		inputManager.addListener(this, "Mouse move");
 		inputManager.addListener(this, "Mouse wheel up");
 		inputManager.addListener(this, "Mouse wheel down");
 		inputManager.addMapping("Mouse click", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
 		inputManager.addListener(this, "Mouse click");
 		inputManager.addMapping("Click g", new KeyTrigger(KeyInput.KEY_G));
 		inputManager.addListener(this, "Click g");
 		inputManager.addMapping("Click s", new KeyTrigger(KeyInput.KEY_S));
 		inputManager.addListener(this, "Click s");
 		init();
 	}
 
 	@Override
 	public void simpleInitApp()
 	{
 		Logger.getLogger("").setLevel(Level.SEVERE);
 		flyCam.setEnabled(false);
 		GraphicManager.setAssetManager(assetManager);
 		sScreenHeight = cam.getHeight();
 		sScreenWidth = cam.getWidth();
		// Network connection test START
		aTestServer = new EverVoidServer();
		final ServerConnection testConnecton = new ServerConnection("localhost");
		aTestServer.start();
		testConnecton.start();
		// Network connection test END
 		sampleGame();
 	}
 
 	@Override
 	public void simpleUpdate(final float tpf)
 	{
 		FrameManager.tick(new FrameUpdate(tpf));
 	}
 }
