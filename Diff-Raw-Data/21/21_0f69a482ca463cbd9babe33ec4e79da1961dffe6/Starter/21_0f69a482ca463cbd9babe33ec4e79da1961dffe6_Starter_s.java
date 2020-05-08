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
 	
	
 
	@SuppressWarnings("unused")
 	public static void main(String[] args){
 			
 		SoundPlayer startSound = new SoundPlayer("src/VK/utils/sound/system-starting-up.wav", 1);
 
 		SplashScreen splash = new SplashScreen(5000);
 
 		splash.showSplashAndExit();
 
 		try {
			new Main();
 		}
 		catch (Exception e) {
 			e.getStackTrace();
 		}
 	}
 }
