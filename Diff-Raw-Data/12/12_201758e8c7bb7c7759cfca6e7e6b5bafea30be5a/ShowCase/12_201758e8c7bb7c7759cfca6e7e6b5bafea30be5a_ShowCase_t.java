 package it.qubixic.showcase;
 
 import javax.microedition.lcdui.Display;
 import javax.microedition.midlet.*;
 
 public class ShowCase extends MIDlet {    
    
     private FlipSplashScreen flipSplashScreen ;
     private Grids grids ;
 
     public ShowCase() {        
         flipSplashScreen = new FlipSplashScreen();
         grids = new Grids(this) ;
     }
     
     public void startApp() {
         loadSplashScreen(Page.SPLASH);        
     }
     
     public void loadSplashScreen(int screen) {       
         Display.getDisplay(this).setCurrent(flipSplashScreen);
         final MIDlet midlet = this ;
         new Thread(new Runnable() {
             public void run() {
                while (flipSplashScreen.isLoading()) {
                 }
                 Display.getDisplay(midlet).setCurrent(grids) ;
             }
         }).start();
      }
     
     public void pauseApp() {
     
     }
     
     public void destroyApp(boolean unconditional) {
     
     }
 }
