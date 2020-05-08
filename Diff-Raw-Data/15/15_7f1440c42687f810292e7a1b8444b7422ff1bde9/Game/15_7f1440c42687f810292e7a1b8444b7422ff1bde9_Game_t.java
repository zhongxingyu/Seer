 package game;
 
 import input.Input;
 import manager.SoundManager;
 import physics.OdePhysics;
 import rendering.DeferredRenderer;
 import rendering.GiRenderer;
 import rendering.NiceRenderer;
 import rendering.OpenGLRendering;
 import rendering.SimpleRenderer;
 import rendering.TestSkinningRenderer;
 import script.JavaScript;
 import settings.Settings;
 import util.Factory;
 import util.FolderWatcher;
 import util.Log;
 import util.Stoppable;
 import util.Util;
 import util.XMLToObjectParser;
 import vr.VRFactory;
 import world.Camera;
 import world.World;
 
 public class Game {
 	public static Game INSTANCE;
 	public GameLoop loop = new GameLoop();
 	public World world;
 	public Factory factory = Factory.INSTANCE;
 	public Input input = new Input();
 	public static boolean DEBUG = false;
 	public Camera cam = new Camera();
 	public boolean exitFlag = false;
	public boolean fullscreenFlag = Settings.USE_FULL_SCREEN;
 	public static VRFactory.VR vr;
 
 	public Game() {
 		INSTANCE = this;
 		if (Settings.VR)
 			vr = VRFactory.createVR();
 		FolderWatcher f = new FolderWatcher(Settings.RESSOURCE_FOLDER);
 		f.addFolderListener(new GameWatcher(this));
 		f.start();
 		FolderWatcher f2 = new FolderWatcher(Settings.ENGINE_FOLDER);
 		f2.addFolderListener(new GameWatcher(this));
 		f2.start();
 		loop.startPause();
 		loop.start();
 	}
 
 	public void parseXML() {
 		new XMLToObjectParser().parse();
 	}
 
 	public void restart() {
 		Log.log(this, "Restarting!");
 		// TODO restart the whole shit
 		Util.sleep(10);
 		loop.startPause();
 		loop.exit();
 		JavaScript.reset();
 		Util.sleep(100);
 		start();
 	}
 
 	public void start() {
 		world = new World();
 		world.add(cam);
		JavaScript.execute(Settings.MAIN_SCRIPT, true);
 		if (Settings.LOW_GRAPHICS)
 			Util.sleep(1000);
 		loop.endPause();
 	}
 
 	public void addComponent(String c) {
 		c = c.toLowerCase();
 		if (c.contains("renderer")) {
 			if (c.equals("renderer") || Settings.LOW_GRAPHICS) {
 				loop.renderer = new SimpleRenderer();
 			} else if (c.equals("deferredrenderer")) {
 				loop.renderer = new DeferredRenderer();
 			} else if (c.equals("skinrenderer")) {
 				loop.renderer = new TestSkinningRenderer();
 			} else if (c.equals("nicerenderer")) {
 				loop.renderer = new NiceRenderer();
 			} else if (c.equals("girenderer")) {
 				loop.renderer = new GiRenderer();
 			} else {
 				System.err.println("Can't add component: " + c);
 			}
 		} else {
 			if (c.equals("gamemechanics")) {
 				loop.mechanics = new GameMechanics();
 			} else if (c.equals("sound")) {
 				loop.sound = new SoundManager();
 			} else if (c.equals("physics")) {
 				loop.mechanics.physics = new OdePhysics();
 			} else {
 				System.err.println("Can't add component: " + c);
 			}
 		}
 	}
 
 	public void exit() {
 		Log.log(this, "game exit");
 		// loop.startPause();
 		loop.exit();
 		Stoppable.stopAll();
 	}
 
 	public void hideMouse(boolean b) {
 		OpenGLRendering.hideMouse(b);
 	}
 
 	public void centerMouse() {
 		OpenGLRendering.centerMouse();
 	}
 
 	public void log(Object o) {
 		Log.log(this, o);
 	}
 
 	public int getWidth() {
 		return loop.renderer.width;
 	}
 
 	public int getHeight() {
 		return loop.renderer.height;
 	}
 
 	public static Game getInstance() {
 		return INSTANCE;
 	}
 
 	public void jsTest() {
 		Log.log(this, "jsTest");
 	}
 }
