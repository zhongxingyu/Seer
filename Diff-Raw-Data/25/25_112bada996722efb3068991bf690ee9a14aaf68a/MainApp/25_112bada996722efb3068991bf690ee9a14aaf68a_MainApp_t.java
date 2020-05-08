 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package game;
 
 
 import com.jme3.app.Application;
 import com.jme3.renderer.ViewPort;
 import com.jme3.system.AppSettings;
 import com.jme3.system.JmeSystem;
 import com.jme3.system.Timer;
 
 public class MainApp extends Application {
 
    private GameState te = null;
    private MenuState ms = null;
    private AppSettings s;
    private InputState is = null;
    
    public MainApp() {
       
    }
 
     @Override
     public void start(){
         // set some default settings in-case
         // settings dialog is not shown
       
         
         if (settings == null)
         {
             s= new AppSettings(true);
         s.setSettingsDialogImage("/Pictures/uk.png");
             setSettings(s);
         }
 
         // show settings dialog
         if (!JmeSystem.showSettingsDialog(settings,true))
            return;
 
         super.start();
     }
    
    @Override
     public void initialize() {
       // initialize the standard environment first
       super.initialize();
       
       // Create the States
       ms = new MenuState(this);
       te = new GameState(this);
       is = new InputState(this);
       
       // Attach MenuState
       getStateManager().attach(ms);
     }
    
    
    @Override
     public void update() {
         super.update();
         float tpf = timer.getTimePerFrame() * speed;
 
         // update states
         stateManager.update(tpf);
 
         // render states
         stateManager.render(renderManager);
 
         renderManager.render(tpf,true);
     }
    
 
    public void loadMenu() {
       getStateManager().detach(te);
       getStateManager().attach(ms);
    }
    
    public void loadInput() {
        
       getStateManager().detach(ms);
       getStateManager().attach(is);
    }
      public void loadMenu2() {
      getStateManager().detach(is);
      getStateManager().attach(ms);
   }
    
    public AppSettings getSettings()
    {
        
        return settings;
    }
    
    public void loadGame() {
       getStateManager().detach(ms);
       getStateManager().attach(te);
    }
    
    
    
    public ViewPort getViewPort() {
       return viewPort;
    }
    
    public ViewPort getGUIViewPort() {
       return guiViewPort;
    }
    
    
    public Timer getTimer() {
       return timer;
    }
    
    
    public static void main(String... args) {
       new MainApp().start();
    }
 
    
 
 }
 
 
