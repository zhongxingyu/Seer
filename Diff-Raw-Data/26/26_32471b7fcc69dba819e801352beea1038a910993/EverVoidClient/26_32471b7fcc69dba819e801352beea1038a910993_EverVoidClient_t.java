 package com.evervoid.client;
 
 import java.awt.Toolkit;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import com.evervoid.client.graphics.EverNode;
 import com.evervoid.client.graphics.FrameUpdate;
 import com.evervoid.client.graphics.GraphicManager;
 import com.evervoid.client.sound.EVSoundEngine;
 import com.evervoid.state.geometry.Dimension;
 import com.jme3.input.MouseInput;
 import com.jme3.input.controls.ActionListener;
 import com.jme3.input.controls.AnalogListener;
 import com.jme3.input.controls.MouseAxisTrigger;
 import com.jme3.input.controls.MouseButtonTrigger;
 import com.jme3.input.controls.Trigger;
 import com.jme3.math.Ray;
 import com.jme3.math.Vector2f;
 import com.jme3.math.Vector3f;
 import com.jme3.post.FilterPostProcessor;
 import com.jme3.post.filters.BloomFilter;
 import com.jme3.post.filters.BloomFilter.GlowMode;
 import com.jme3.scene.Node;
 import com.jme3.system.AppSettings;
 
 /**
  * everVoid game client providing the user with a user interface to play the game.
  */
 public class EverVoidClient extends EverJMEApp implements ActionListener, AnalogListener
 {
 	public enum NodeType
 	{
 		THREEDIMENSION, TWODIMENSION;
 		public Node getNode(final EverVoidClient client)
 		{
 			switch (this) {
 				case TWODIMENSION:
 					return client.getGuiNode();
 				case THREEDIMENSION:
 					return client.getRootNode();
 			}
 			return null;
 		}
 	}
 
 	/**
 	 * Instance of the everVoidClient
 	 */
 	private static EverVoidClient sClient;
 	public static Vector2f sCursorPosition = new Vector2f();
 	private static final EVInputManager sInputManager = EVInputManager.getInstance();
 	private static int sScreenHeight = 0;
 	private static int sScreenWidth = 0;
 
 	/**
 	 * Add a root node to the main window
 	 * 
 	 * @param type
 	 *            Type of node to add
 	 * @param node
 	 *            The node to add
 	 */
 	public static void addRootNode(final NodeType type, final EverNode node)
 	{
 		type.getNode(sClient).attachChild(node);
 	}
 
 	/**
 	 * Delete a root node from the main window
 	 * 
 	 * @param node
 	 *            The node to delete (type doesn't matter)
 	 */
 	public static void delRootNode(final EverNode node)
 	{
 		// Try detaching from both; no side-effects
 		sClient.getGuiNode().detachChild(node);
 		sClient.getRootNode().detachChild(node);
 	}
 
 	public static Logger getLogger()
 	{
 		return Logger.getLogger(EverVoidClient.class.getName());
 	}
 
 	/**
 	 * Creates a 3D Ray covered the area directly under point on the screen designated by the vector. This should be used when
 	 * in a 3D view to convert from 2D to 3D.
 	 * 
 	 * @param vector
 	 *            The screen location under which the ray should be formed.
 	 * @return The Ray projecting for the click location going straight in to the camera.
 	 */
 	public static Ray getRayFromVector(final Vector2f vector)
 	{
 		// calculates the point you clicked and it's projection at the z = 10 level
 		final Vector3f clickPoint = sClient.cam.getWorldCoordinates(vector, 10f);
 		final Vector3f projectedPoint = sClient.cam.getWorldCoordinates(vector, -10f);
 		// creates a ray from the original point and the projection
 		return new Ray(clickPoint, projectedPoint.subtractLocal(clickPoint).normalizeLocal());
 	}
 
 	/**
 	 * @return The user's client settings
 	 */
 	public static EVClientSettings getSettings()
 	{
 		return sClient.aClientSettings;
 	}
 
 	/**
 	 * Creates a Dimension containing the window width and height in x and y respectively. More specifically represents the
 	 * number of pixels of the window in that particular dimension.
 	 * 
 	 * @return The Dimensions of the window at this instant.
 	 */
 	public static Dimension getWindowDimension()
 	{
 		return new Dimension(sScreenWidth, sScreenHeight);
 	}
 
 	public static Vector3f getWorldCoordiante(final Vector2f screenPos, final float zPos)
 	{
 		return sClient.cam.getWorldCoordinates(screenPos, zPos);
 	}
 
 	/**
 	 * everVoid Client program
 	 * 
 	 * @param args
 	 *            Arguments passed to the program.
 	 */
 	public static void main(final String[] args)
 	{
 		Logger.getLogger("").setLevel(Level.SEVERE);
 		System.setProperty("org.lwjgl.opengl.Window.undecorated", "false"); // Change to true for undecorated
 		sClient = new EverVoidClient();
 		sClient.setShowSettings(false);
 		final AppSettings options = new AppSettings(true);
 		final Dimension screenSize = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
 		options.setResolution((int) (screenSize.width * 0.8), (int) (screenSize.height * 0.8));
 		options.setFullscreen(false);
 		options.setSamples(4);
 		options.setVSync(true);
 		options.setTitle("everVoid");
 		sClient.setSettings(options);
 		sClient.start();
 	}
 
 	public static void quit()
 	{
 		sClient.requestClose(false);
 	}
 
 	private final EVClientSettings aClientSettings;
 	private EVViewManager aViewManager;
 
 	/**
 	 * Private constructor for the everVoidClient
 	 */
 	private EverVoidClient()
 	{
 		sClient = this;
 		aClientSettings = new EVClientSettings();
 		getLogger().setLevel(Level.INFO);
 	}
 
 	private void createAllMappings()
 	{
 		inputManager.addMapping("Mouse move", new MouseAxisTrigger(MouseInput.AXIS_X, false), new MouseAxisTrigger(
 				MouseInput.AXIS_X, true), new MouseAxisTrigger(MouseInput.AXIS_Y, false), new MouseAxisTrigger(
 				MouseInput.AXIS_Y, true));
 		inputManager.addListener(this, "Mouse move");
 		createMapping("Mouse wheel up", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, false));
 		createMapping("Mouse wheel down", new MouseAxisTrigger(MouseInput.AXIS_WHEEL, true));
 		createMapping("Mouse left", new MouseButtonTrigger(MouseInput.BUTTON_LEFT));
 		createMapping("Mouse right", new MouseButtonTrigger(MouseInput.BUTTON_RIGHT));
 		KeyboardKey.setMappings(inputManager, this);
 	}
 
 	private void createMapping(final String pMappingName, final Trigger pTrigger)
 	{
 		inputManager.addListener(this, pMappingName);
 		inputManager.addMapping(pMappingName, pTrigger);
 	}
 
 	@Override
 	public void onAction(final String name, final boolean isPressed, final float tpf)
 	{
 		sInputManager.onAction(name, isPressed, tpf, sCursorPosition);
 	}
 
 	@Override
 	public void onAnalog(final String name, final float delta, final float tpf)
 	{
 		sCursorPosition = inputManager.getCursorPosition();
 		sInputManager.onAnalog(name, delta, tpf, sCursorPosition);
 	}
 
 	@Override
 	public void requestClose(final boolean esc)
 	{
 		super.requestClose(esc);
 		EVClientEngine.disconnect();
 		EVClientEngine.stopLocalServer(); // Kill it, if any
 	}
 
 	@Override
 	public void simpleInitApp()
 	{
 		GraphicManager.setAssetManager(assetManager);
 		sScreenHeight = cam.getHeight();
 		sScreenWidth = cam.getWidth();
 		final FilterPostProcessor fpp = new FilterPostProcessor(assetManager);
 		final BloomFilter bloom = new BloomFilter(GlowMode.Objects);
 		bloom.setDownSamplingFactor(4);
 		bloom.setBloomIntensity(1.2f);
 		fpp.addFilter(bloom);
 		getRootViewPort().addProcessor(fpp);
 		createAllMappings();
 		aViewManager = EVViewManager.getInstance();
		// start the client engine
		EVClientEngine.getInstance();
		// register the gui as a listener
 		EVClientEngine.registerGlobalListener(aViewManager);
 		EVSoundEngine.init(assetManager, audioRenderer);
 	}
 
 	@Override
 	public void simpleUpdate(final float tpf)
 	{
 		EVFrameManager.tick(new FrameUpdate(tpf));
 	}
 }
