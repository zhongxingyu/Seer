 package spaceshooters.main;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 import spaceshooters.api.plugins.loading.PluginLoader;
 import spaceshooters.config.Configuration;
 import spaceshooters.gfx.TextureAtlasManager;
 import spaceshooters.gfx.texturepacks.AlternativeTexturePack;
 import spaceshooters.gfx.texturepacks.CustomTexturePack;
 import spaceshooters.gfx.texturepacks.DefaultTexturePack;
 import spaceshooters.gfx.texturepacks.ITexturePack;
 import spaceshooters.gui.Font;
 import spaceshooters.save.SaveData;
 import spaceshooters.sfx.SoundEngine;
 import spaceshooters.states.GameplayState;
 import spaceshooters.states.MainMenuState;
 import spaceshooters.states.OptionsState;
 import spaceshooters.states.PluginsState;
 import spaceshooters.states.TestLevelState;
 
 /**
  * The main game class. All states are initialized here.
  * 
  * @author Mat
  * 
  */
 public class Spaceshooters extends StateBasedGame {
 	
 	private static AppGameContainer container;
 	private static ITexturePack texturePack;
	public static String VERSION = "Alpha 1.2.1";
 	public static final int WIDTH = 800;
 	public static final int HEIGHT = 600;
 	
 	private Configuration config = Configuration.getConfiguration();
 	
 	Spaceshooters() throws SlickException {
 		super("Spaceshooters 2 " + VERSION);
 		System.setProperty("org.lwjgl.librarypath", Spaceshooters.getPath() + "bin/libs/natives");
 		this.checkVersion();
 		container = new AppGameContainer(this, WIDTH, HEIGHT, Boolean.parseBoolean(config.getLauncher(Configuration.LAUNCHER_FULLSCREEN)));
 		container.setVerbose(false);
 		container.setShowFPS(false);
 		if (Boolean.parseBoolean(config.getLauncher(Configuration.LAUNCHER_LIMIT_FPS))) {
 			container.setTargetFrameRate(75);
 		}
 		if (Boolean.parseBoolean(config.getLauncher(Configuration.LAUNCHER_VSYNC))) {
 			container.setVSync(true);
 		}
 		container.setMinimumLogicUpdateInterval(20); // 50 ticks per second.
 		container.setMaximumLogicUpdateInterval(20);
 		container.start();
 	}
 	
 	@Override
 	public void initStatesList(GameContainer container) throws SlickException {
 		this.init();
 		this.addState(new MainMenuState());
 		this.addState(new GameplayState());
 		this.addState(new PluginsState());
 		this.addState(new OptionsState());
 		this.addState(new TestLevelState());
 	}
 	
 	public void init() throws SlickException {
 		new Thread(new Runnable() {
 			@Override
 			public void run() {
 				SoundEngine.init();
 				PluginLoader.findPlugins();
 			}
 		}, "Sound & Plugin Loader Thread").start();
 		
 		SaveData.init();
 		TextureAtlasManager.getTextureManager().init();
 	}
 	
 	@Override
 	public void render(GameContainer container, Graphics g) throws SlickException {
 		g.setFont(Font.getFont(16));
 		super.render(container, g);
 	}
 	
 	private void checkVersion() {
 		if (new File("dev.txt").exists())
 			return;
 		
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
 	
 	public static ITexturePack getCurrentTexturePack() {
 		String tp = Configuration.getConfiguration().getLauncher(Configuration.LAUNCHER_TEXTURE_PACK);
 		if (texturePack == null) {
 			if (tp.equals("Default")) {
 				texturePack = DefaultTexturePack.getInstance();
 			} else if (tp.equals("Alternative")) {
 				texturePack = AlternativeTexturePack.getInstance();
 			} else {
 				File pack = new File(getPath() + "texturepacks/" + tp);
 				if (pack.exists()) {
 					try {
 						texturePack = new CustomTexturePack(pack);
 					} catch (IOException e) {
 						e.printStackTrace();
 						texturePack = DefaultTexturePack.getInstance();
 					}
 				} else {
 					texturePack = DefaultTexturePack.getInstance();
 				}
 			}
 		}
 		
 		return texturePack;
 	}
 	
 	public static final GameContainer getGameContainer() {
 		return container;
 	}
 	
 	public static String getPath() {
 		String os = System.getProperty("os.name").toLowerCase();
 		if (os.indexOf("win") >= 0)
 			return System.getenv("APPDATA") + File.separator + "Spaceshooters 2" + File.separator;
 		else if (os.indexOf("mac") >= 0)
 			return System.getProperty("user.home") + File.separator + "Library" + File.separator + "Application Support" + File.separator + "Spaceshooters 2" + File.separator;
 		else
 			return System.getProperty("user.home") + File.separator + "Spaceshooters 2" + File.separator;
 	}
 	
 	public static String getPluginsPath() {
 		new File(getPath() + "plugins" + File.separator).mkdirs();
 		return getPath() + "plugins" + File.separator;
 	}
 	
 	public static void main(String[] args) {
 		try {
 			new Spaceshooters();
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 	}
 }
