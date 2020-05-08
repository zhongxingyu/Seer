 package org.racenet.racesow;
 
 import java.io.File;
 
 import org.racenet.framework.GLGame;
 import org.racenet.framework.interfaces.FileIO;
 import org.racenet.framework.interfaces.Screen;
 
 import android.os.Bundle;
 
 public class Racesow extends GLGame {	
 	
 	public static boolean LOOPER_PREPARED = false;
 	
 	public void onCreate(Bundle savedInstanceState) {
 		
 		super.onCreate(savedInstanceState);
 		
 		FileIO fileIO = this.getFileIO();
 		fileIO.createDirectory("racesow" + File.separator + "maps");
 		fileIO.createDirectory("racesow" + File.separator + "textures");
 	}
 	
     public Screen getStartScreen() {
     	
         return new MenuScreen(this);
     }
     
     public void onBackPressed() {
     	
     	Screen screen = this.getCurrentScreen();
     	String screenName = screen.getClass().getName();
     	
     	if (screenName.endsWith("GameScreen")) {
     		
     		GameScreen gameScreen = (GameScreen)screen;
     		if (gameScreen.map.inRace() || gameScreen.map.raceFinished()) {
     		
    			gameScreen.map.restartRace(gameScreen.player);
     			
     		} else {
     			
     			this.glView.queueEvent(new Runnable() {
 
                     public void run() {
                        
                     	Racesow.this.setScreen(new MapsScreen(Racesow.this));
                     }
                 });
     		}
     	
     	} else if (screenName.endsWith("MapsScreen")) {
     		
 			this.glView.queueEvent(new Runnable() {
 
                 public void run() {
                    
                 	Racesow.this.setScreen(Racesow.this.getStartScreen());
                 }
             });
     	
     	} else {
     		
     		LOOPER_PREPARED = false; // just to be sure...
     		super.onBackPressed();
     	}
     }
 }
