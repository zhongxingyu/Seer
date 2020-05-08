 package nz.ac.otago.oosg;
 
 import com.jme3.app.SimpleApplication;
 import com.jme3.niftygui.NiftyJmeDisplay;
 import com.jme3.system.AppSettings;
 import de.lessvoid.nifty.Nifty;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nz.ac.otago.oosg.gui.GuiController;
 import nz.ac.otago.oosg.states.GameState;
 
 /**
  * Welcome to the Beginning of the Otago Open Source Game!
  *
  * I have put this class in the package nz.ac.otago.oosg. Which might be too
  * long or others may suggest a better one.
  *
  * Modified by Tim Sullivan. 
  * Added to by Kevin Weatherall
  *
  */
 public class Game extends SimpleApplication {
     public static void main(String[] args) {
         //Custom settings to 'brand' the game launcher
         AppSettings settings = new AppSettings(true);
         settings.setTitle("Otago Open Source Game");
         settings.setSettingsDialogImage("Interface/oosgsplash.png"); //temp image
         settings.setResolution(800, 600);
 
         Game app = new Game(); //create instance of this Game class
         app.setSettings(settings); //apply the settings above
         app.start();
     }
 
     /**
      * This method is called first when the game starts. It gives us the change
      * to initilise objects and add them to the scene in the game. It is only
      * called once.
      */
     @Override
     public void simpleInitApp() {
         GameState gs = new GameState(this);
         stateManager.attach(gs);
         initGui(); //simpy comment out to disable entire gui.
     }
     
     /**
      * Load the welcome GUI menu system.
      * Registers GuiController is responsible for all gui menu events.
      */
     private void initGui() {
         
         NiftyJmeDisplay niftyDisplay = new NiftyJmeDisplay(assetManager,
                 inputManager,
                 audioRenderer,
                 guiViewPort);
 
         Nifty nifty = niftyDisplay.getNifty();
         //nifty.setDebugOptionPanelColors(true); //DEBUG
         nifty.fromXml("Interface/gamegui.xml", "start", new GuiController(nifty, this));
         //get insane console spam if the logging level isn't set for niftygui
         Logger.getLogger("de.lessvoid.nifty").setLevel(Level.SEVERE);
         Logger.getLogger("NiftyInputEventHandlingLog").setLevel(Level.SEVERE);
         // attach the nifty display to the gui view port as a processor
         guiViewPort.addProcessor(niftyDisplay);
 
         //must disable the flycamera for gui to 'get' the mouse.
         flyCam.setEnabled(false);
         inputManager.setCursorVisible(true);
         //pause the game
         stateManager.getState(GameState.class).setEnabled(false);
     }
 }
