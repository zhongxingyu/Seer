 package VK;
 
 import VK.main.Main;
 import VK.utils.pictures.SplashScreen;
 import VK.utils.sound.SoundPlayer;
 
 /**
  * Een klasse om de applicatie te testen/runnen
  * 
  * @author Hyke de Vries
  * @version 1.0
  */
 public class Starter {
 	
	public static Main main;
 
 	public static void main(String[] args){
 			
		@SuppressWarnings("unused")
 		SoundPlayer startSound = new SoundPlayer("src/VK/utils/sound/system-starting-up.wav", 1);
 
 		SplashScreen splash = new SplashScreen(5000);
 
 		splash.showSplashAndExit();
 
 		try {
			main = new Main();
 		}
 		catch (Exception e) {
 			e.getStackTrace();
 		}
 	}
 }
