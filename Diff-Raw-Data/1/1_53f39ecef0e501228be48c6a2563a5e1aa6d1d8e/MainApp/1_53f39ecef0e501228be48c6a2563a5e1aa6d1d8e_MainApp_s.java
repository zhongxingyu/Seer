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
 import database.Player;
 import states.GameOverState;
 import states.GameState;
 import states.InputState;
 import states.LogInState;
 import states.LogInState2;
 import states.MenuState;
 import states.MenuStateGame;
 import states.StatisticsState;
 
 public class MainApp extends Application {
 
     private GameState te = null;
     private MenuState ms = null;
     private MenuStateGame ms2 = null;
     private AppSettings s;
     private InputState is = null;
     private StatisticsState ss = null;
     private LogInState ls = null;
     private LogInState2 ls2 = null;
     private GameOverState gos=null;
     private Player player;
 
     public MainApp() {
     }
 
     public void start() {
         // set some default settings in-case
         // settings dialog is not shown
 
         if (settings == null) {
             s = new AppSettings(true);
             s.setSettingsDialogImage("/Pictures/Portada.jpg");
 
             setSettings(s);
         }
 
         // show settings dialog
         if (!JmeSystem.showSettingsDialog(settings, true)) {
             return;
         }
 
         super.start();
     }
 
     public void initialize() {
         // initialize the standard environment first
         super.initialize();
 
         // Create the States
         ms = new MenuState(this);
         te = new GameState(this);
         is = new InputState(this);
         ss = new StatisticsState(this);
         ls = new LogInState(this);
         ls2 = new LogInState2(this);
         ms2 = new MenuStateGame(this);
         gos= new GameOverState(this);
 
         // Attach the fisrt screen to be shown
         getStateManager().attach(ls);
     }
 
     public void update() {
         super.update();
         float tpf = timer.getTimePerFrame() * speed;
 
         // update states
         stateManager.update(tpf);
 
         // render states
         stateManager.render(renderManager);
 
         renderManager.render(tpf, true);
     }
     
     public void reLoadMenu() {
         getStateManager().detach(ms);
         getStateManager().attach(ms);
     }
 
     public void loadMenu() {
         getStateManager().detach(te);
         getStateManager().attach(ms);
     }
 
     public void loadInput() {
         getStateManager().detach(ms);
         getStateManager().attach(is);
     }
 
     public void loadMenuFromInput() {
         getStateManager().detach(is);
         getStateManager().attach(ms);
     }
 
     public void loadMenuFromStatistics(){
         getStateManager().detach(ss);
         getStateManager().attach(ms);
     }
     
     public void loadMenuFromLogIn(){
         getStateManager().detach(ls);
         getStateManager().attach(ms);
     }
     public void loadLogInFromMenu(){
         getStateManager().detach(ms);
         getStateManager().attach(ls);
     }
     
     public void loadMenuFromLogIn2(){
         getStateManager().detach(ls2);
         getStateManager().attach(ms);
     }
     public void loadLogIn2FromMenu(){
         getStateManager().detach(ms);
         getStateManager().attach(ls2);
     }
 
     public AppSettings getSettings() {
         return settings;
     }
 
     public void loadGame() {
         getStateManager().detach(ms);
         getStateManager().attach(te);
     }
     
     public void loadGameFromMenuGame() {
          getStateManager().detach(ms2);
         //getStateManager().attach(te);
         te.setEnabled(true);
         inputManager.setCursorVisible(false);
         
     }
     
     public void loadMenuGameFromGame() {
         //getStateManager().detach(te);
         getStateManager().attach(ms2);
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
 
     public void loadStatistics() {
 
         getStateManager().detach(ms);
         getStateManager().attach(ss);
     }
     
 
     public void loadMenuFromGaveOver() {
 
         getStateManager().detach(gos);
         getStateManager().attach(ms);
     }
     
     public void  loadGameFromGameOver() {
 
         getStateManager().detach(gos);
          te = new GameState(this);
         getStateManager().attach(te);
     }
      
     public void  loadGameOverFromGame() {
 
         getStateManager().detach(te);
         te.cleanup();
         getStateManager().attach(gos);
     }
     
     public void setPlayer(Player p) {
         this.player = p;
     }
 
     public Player getPlayer() {
         return player;
     }
     
     
 }
