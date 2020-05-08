 package application_states;
 
 import base.Runner;
 
 import com.jme3.app.Application;
 import com.jme3.app.state.AbstractAppState;
 import com.jme3.app.state.AppStateManager;
 import com.jme3.input.InputManager;
 
 import de.lessvoid.nifty.Nifty;
 import de.lessvoid.nifty.elements.render.TextRenderer;
 import de.lessvoid.nifty.screen.Screen;
 import de.lessvoid.nifty.screen.ScreenController;
 
 public class PlayerStats extends AbstractAppState implements ScreenController{
     private Runner instance;
     private Nifty niftyGui;
     private InputManager inputManager;
 
     public static float
     	kills = 0,
     	shots = 0,
     	hits = 0,
     	damageTaken = 0,
     	damageDealt = 0;
     
     @Override
     public void initialize(AppStateManager stateManager, Application app) {
         super.initialize(stateManager, app);
         instance = (Runner) app;
         niftyGui = Runner.getNifty();
         inputManager = instance.getInputManager();
         
         setEnabled(false);
     }
 
     @Override
     public void setEnabled(boolean enabled) {
         super.setEnabled(enabled);
 
         if (enabled) {
             if (!inputManager.isCursorVisible())
                 inputManager.setCursorVisible(true);
             
             niftyGui.gotoScreen("stats");
             
             Screen screen = niftyGui.getScreen("stats");
             
             screen.findElementByName("kills").getRenderer(TextRenderer.class).setText("Kills: " + (int)kills);
             screen.findElementByName("shots").getRenderer(TextRenderer.class).setText("Shots: " + (int)shots);
             screen.findElementByName("hits").getRenderer(TextRenderer.class).setText("Hits: " + (int)hits);
            String accuracy = shots>0? (int)(100*(hits/shots))+"%": "N/A "; 
             screen.findElementByName("accuracy").getRenderer(TextRenderer.class).setText("Accuracy: " + accuracy);
             screen.findElementByName("damage_taken").getRenderer(TextRenderer.class).setText("Damage Taken: " + (int)damageTaken);
             screen.findElementByName("damage_dealt").getRenderer(TextRenderer.class).setText("Damage Dealt: " + (int)damageDealt);
         }
         else {
             //inputManager.setCursorVisible(false);
         }
     }
 
     public void bind(Nifty nifty, Screen screen) {
         System.out.println("bind(" + screen.getScreenId() + ")");
     }
 
     public void onStartScreen() {
         System.out.println("onStartScreen");
     }
 
     public void onEndScreen() {
         System.out.println("onEndScreen");
     }
     
     public void exit(){
     	setEnabled(false);
         Runner.getState(PauseState.class).setEnabled(true);
     }
 }
