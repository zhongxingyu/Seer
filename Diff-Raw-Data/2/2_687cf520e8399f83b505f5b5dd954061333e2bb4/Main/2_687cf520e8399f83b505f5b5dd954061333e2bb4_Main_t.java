 package main;
 
 import com.jme3.app.DebugKeysAppState;
 import com.jme3.app.SimpleApplication;
 import com.jme3.app.StatsAppState;
 import com.jme3.renderer.RenderManager;
 import com.jme3.system.AppSettings;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import state.InGameState;
 import state.InMainMenuState;
 import variables.P;
 
 /**
  * The main application class. This sets up the relevant AppStates, starting
  * the game.
  * @author normenhansen
  */
 public class Main extends SimpleApplication {
     
     public static void main(String[] args) {
        AppSettings appSettings = new AppSettings(true);
         appSettings.setSamples(2);
         appSettings.setVSync(true);
         Main app = new Main();
         app.setSettings(appSettings); 
         app.start();
     }
 
     public Main(){
         /* This call to super makes sure to not load the flyCam. */
         super(new StatsAppState(), new DebugKeysAppState());
         Logger.getLogger("").setLevel(Level.SEVERE);
         Logger.getLogger("Kandidat").setLevel(Level.FINE);
     }
     
     @Override
     public void simpleInitApp() {
         P.screenWidth = settings.getWidth();
         P.screenHeight = settings.getHeight();
         stateManager.attach(new InGameState());
         //stateManager.attach(new InMainMenuState());
     }
 
     @Override
     public void simpleUpdate(float tpf) {
     }
 
     @Override
     public void simpleRender(RenderManager rm) {
         //TODO: add render code
     }
     
     // BAra skriver en kommentar för att testa push
 }
