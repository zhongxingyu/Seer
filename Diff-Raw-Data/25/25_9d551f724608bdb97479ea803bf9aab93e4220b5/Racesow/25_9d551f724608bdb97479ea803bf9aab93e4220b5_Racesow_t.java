 package org.racenet.racesow;
 
 import org.racenet.framework.GLGame;
 import org.racenet.framework.interfaces.Screen;
 
 public class Racesow extends GLGame {	
 	
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
                        
                     	Racesow.this.setScreen(Racesow.this.getStartScreen());
                     }
                 });
     		}
     	
     	} else {
     		
     		super.onBackPressed();
     	}
     }
 }
