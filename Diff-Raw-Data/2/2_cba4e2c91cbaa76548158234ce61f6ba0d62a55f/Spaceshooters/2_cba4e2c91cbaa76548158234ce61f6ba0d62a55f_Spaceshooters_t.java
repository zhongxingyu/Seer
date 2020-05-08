 package spaceshooters.main;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import spaceshooters.api.plugins.loading.PluginLoader;
 import spaceshooters.exceptions.OSNotSupportedException;
 import spaceshooters.gfx.RenderEngine;
 import spaceshooters.gui.font.Font;
 import spaceshooters.sfx.SoundEngine;
 import spaceshooters.states.ClientMultiplayerState;
 import spaceshooters.states.GameOverState;
 import spaceshooters.states.GameplayState;
 import spaceshooters.states.HostMultiplayerClass;
 import spaceshooters.states.MainMenuState;
 import spaceshooters.states.OptionsState;
 import spaceshooters.states.PluginsState;
 import spaceshooters.states.PreMultiplayerState;
 import spaceshooters.states.StoreState;
 import spaceshooters.states.TestLevelState;
 import spaceshooters.util.ErrorFrame;
 import spaceshooters.util.config.Configuration;
 import sun.misc.Launcher;
 
 /**
  * The main game class. All states are initialized here. Game starting point is in {@link Launcher}, though.
  * 
  * @author Mat
  * 
  */
 public class Spaceshooters extends StateBasedGame {
 	
	public static String VERSION = "Alpha 1.1.3";
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
 	private Configuration config = Configuration.getConfiguration();
 	private static Font font;
 	
 	Spaceshooters() throws SlickException {
 		super("Spaceshooters 2 " + VERSION);
 		System.setProperty("org.lwjgl.librarypath", Spaceshooters.getPath() + "bin/libs/natives");
 		this.checkVersion();
 		AppGameContainer game = new AppGameContainer(this, WIDTH, HEIGHT, config.getLauncher(Configuration.FULLSCREEN));
 		game.setVerbose(false);
 		game.setShowFPS(false);
 		if (config.getLauncher(Configuration.LIMIT_FPS)) {
 			game.setTargetFrameRate(75);
 		}
 		if (config.getLauncher(Configuration.VSYNC)) {
 			game.setVSync(true);
 		}
 		game.setMinimumLogicUpdateInterval(20); // 50 ticks per second.
 		game.setMaximumLogicUpdateInterval(20);
 		game.setAlwaysRender(true);
 		game.setUpdateOnlyWhenVisible(false);
 		game.start();
 		
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.init();
 		this.addState(new MainMenuState());
 		this.addState(new GameplayState());
 		this.addState(new StoreState());
 		this.addState(new PreMultiplayerState());
 		this.addState(new HostMultiplayerClass());
 		this.addState(new ClientMultiplayerState());
 		this.addState(new PluginsState());
 		this.addState(new OptionsState());
 		this.addState(new TestLevelState());
 		this.addState(new GameOverState());
 	}
 	
 	public void init() throws SlickException {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				SoundEngine.init();
 			}
 		}, "Sound Loader Thread").start();
 		
 		font = new Font("gfx/fonts/default.ttf", 16);
 		
 		PluginLoader.findPlugins();
 		RenderEngine.registerImages(false);
 		
 		/*
 		try {
 			Field f = ClassLoader.class.getDeclaredField("classes");
 			f.setAccessible(true);
 			Vector<Class> classes = (Vector<Class>) f.get(ClassLoader.getSystemClassLoader());
 			for (Class c : classes) {
 				Logger.info(c.getName());
 			}
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		*/
 	}
 	
 	private void checkVersion() {
 		try {
 			File version = new File(Spaceshooters.getPath() + "bin" + File.separator + "version");
 			if (!version.exists()) {
 				version.createNewFile();
 				BufferedWriter wr = new BufferedWriter(new FileWriter(version));
 				wr.write(VERSION);
 				wr.close();
 			} else {
 				BufferedReader r = new BufferedReader(new FileReader(version));
 				if (VERSION != r.readLine()) {
 					version.delete();
 					version.createNewFile();
 					BufferedWriter wr = new BufferedWriter(new FileWriter(version));
 					wr.write(VERSION);
 					wr.close();
 				}
 				r.close();
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static Font getFont() {
 		return font;
 	}
 	
 	public static String getPath() {
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.indexOf("win") >= 0) {
 			return System.getenv("APPDATA") + File.separator + "Spaceshooters 2" + File.separator;
 		} else if (os.indexOf("mac") >= 0) {
 			return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "Spaceshooters 2" + File.separator;
 		} else if (os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0 || os.indexOf("aix") > 0) {
 			return System.getProperty("user.home") + File.separator + "Spaceshooters 2" + File.separator;
 		} else {
 			throw new ErrorFrame(new OSNotSupportedException(os), true);
 		}
 	}
 	
 	public static String getPluginsPath() {
 		return getPath() + "plugins" + File.separator;
 	}
 	
 	public static String getGraphicsFolder() {
 		return Configuration.getConfiguration().getLauncher(Configuration.OLD_GFX) ? "gfx/org/" : "gfx/new/";
 	}
 	
 	public static void main(String[] args) {
 		try {
 			new Spaceshooters();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }
