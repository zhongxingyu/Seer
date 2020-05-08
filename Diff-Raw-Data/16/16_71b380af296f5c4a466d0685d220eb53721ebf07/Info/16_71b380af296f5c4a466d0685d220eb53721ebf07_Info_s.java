 package rageteam.cookieslap.main;
 
 import java.io.File;
 
 public class Info {
 	public static String name = "CookieSlap";
 	public static String description = "A Reloaded version of CookieSlap";
 	public static String version = "0.0.1a";
 	public static String author = "RageTeam";
	public static String[] authors = {"Xquiset", "RageMaster14", "TheMinecraftDomain"};
 	public static String prefix = "CS";
 	
 	public static File logFile = new File("CS-Log.log");
 	public static File errorlogFile = new File("CS-ErrorLog.log");
 	
 	public static File baseDir = new File("plugins/CookieSlap");
 	public static File configFile = new File(baseDir + "/config.yml");
 	public static File playersFile = new File(baseDir + "/players.yml");
 	public static File arenasFile = new File(baseDir + "/arenas.yml");
 	
 	public static String worldsPath = "arenas/";
 }
