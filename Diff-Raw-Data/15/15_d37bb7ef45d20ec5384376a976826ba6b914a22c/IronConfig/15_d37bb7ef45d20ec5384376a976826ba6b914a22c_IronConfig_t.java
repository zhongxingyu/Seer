 package fr.frozen.iron.util;
 
 import java.io.File;
 import java.io.IOException;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.FileAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import fr.frozen.game.SoundManager;
 import fr.frozen.util.XMLParser;
 
 public class IronConfig {
 	
 	protected static XMLParser xmlinstance = new XMLParser("Data/iron.cfg");
 	protected static IronConfig instance = new IronConfig();
 	
 	protected static String CLIENT_LOG_FILE = "client.log";
 	protected static String SERVER_LOG_FILE = "server.log";
 	
 	protected static Level CLIENT_LOG_LEVEL = Level.INFO;
 	protected static Level SERVER_LOG_LEVEL = Level.ALL;
 	
 	protected static String CONSOLE_LAYOUT = "[%5p] - %m%n";
 	protected static String FILE_LAYOUT = "[%d{dd/MM/yy-HH:mm:ss,SSS}][%5p][%t] (%F:%L) - %m%n";
 	
 	protected String username;
 	protected boolean showGrid;
 	
 	protected IronConfig() {
	}
	
	public static IronConfig getInstance() {
		return instance;
	}
	
	//if i put in constructor, the server will need the jogg/jorbis jars
	//so only the client initialises this
	public void initClientConfig() {
 		username = IronUtil.findName();
 		
 		String showGridOption = IronUtil.findOptionValue("showgrid");
 		showGrid = IronConst.showGridDefault;
 		if (showGridOption != null) {
 			if (showGridOption.equals("true")) {
 				showGrid = true;
 			} else if (showGridOption.equals("false")) {
 				showGrid = false;
 			}
 		}
 		
 		float volume = 1.f;
 		String volumeOption = IronUtil.findOptionValue("volume");
 		if (volumeOption != null) {
 			volume = Float.parseFloat(volumeOption);
 		}
 		setVolumeAux(volume);
 	}
 	
 	public static void configClientLogger() {
 		configLogger(CLIENT_LOG_LEVEL, CLIENT_LOG_FILE, false);	
 	}
 	
 	public static void configServerLogger() {
 		configLogger(SERVER_LOG_LEVEL, SERVER_LOG_FILE, true);
 	}
 	
 	public static void configLogger(Level level, String logFileName, boolean append) {
 		Logger.getRootLogger().setLevel(level);
 		Logger.getRootLogger().addAppender(new ConsoleAppender(new PatternLayout(CONSOLE_LAYOUT)));
 		
 		
 		if (logFileName == null || logFileName.equals("")) return;
 		
 		String dirPath = IronUtil.getIronDirPath();
 		String filePath = dirPath + System.getProperty("file.separator") + logFileName;
 		
 		File dir = new File(dirPath);
 		if (!dir.exists()) {
 			dir.mkdir();
 		}
 
 		try {
 			Logger.getRootLogger().addAppender(new FileAppender(new PatternLayout(FILE_LAYOUT), filePath, append));
 		} catch (IOException e) {
 			Logger.getRootLogger().warn("log file could not be appended");
 		}
 	}
 	
 	public static String getUserName() {
 		return instance.username;
 	}
 	
 	public static void setUserName(String name) {
 		instance.username = name;
 		IronUtil.saveName(name);
 	}
 	
 	public static boolean isShowGrid() {
 		return instance.showGrid;
 	}
 	
 	public static void setShowGrid(boolean value) {
 		instance.showGrid = value;
 		IronUtil.saveOptionValue("showgrid", value ? "true":"false");
 	}
 
 	public static float getVolume() {
 		return SoundManager.getInstance().getMusicVolume();
 	}
 	
 	private static void setVolumeAux(float val) {
 		SoundManager.getInstance().setMusicVolume(val);
 		SoundManager.getInstance().setSoundVolume(val);
 		
 		SoundManager.getInstance().setMusicOn(val > 0);
 		SoundManager.getInstance().setSoundsOn(val > 0);
 	}
 	
 	public static void setVolume(float val) {
 		setVolumeAux(val);
 		IronUtil.saveOptionValue("volume", String.valueOf(val));
 	}
 	
 	public static XMLParser getIronXMLParser() {
 		return xmlinstance;
 	}
 	
 	/*public static void main(String []args) {
 		IronConfig ic = new IronConfig();
 		System.out.println("---------------");
 		String orcMaxHp = ic.getAttributeValue("unitstats/footsoldier/orc", "maxhp");
 		String elfweaponid = ic.getAttributeValue("unitstats/footsoldier/elf", "weaponid");
 		System.out.println("orc max hp = "+orcMaxHp);
 		System.out.println("elf weapon id = "+elfweaponid);
 	}*/
 }
