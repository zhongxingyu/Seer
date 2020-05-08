 package JPushy.Core;
 
 import JPushy.Settings.Settings;
 
 /**
  * 
  * @author Marcel Benning
  * 
  */
 public class Core {
 
 	Game			game;
 	static Settings	settings;
 
 	public Core(String[] args, Settings settings) {
 		Core.settings = settings;
 		game = new Game(args);
 	}
 
 	public static Settings getSettings() {
 		return settings;
 	}
 
 	public static boolean checkDebugMode(){
 		return settings.getSettings(settings.debug);
 	}
 	
 }
