 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package dab.gui.application;
 
 import com.fasterxml.jackson.core.JsonProcessingException;
 import dab.engine.newsim.AbstractSimulator;
 import dab.engine.newsim.SinglePlayerSimulator;
 import dab.engine.newsim.TwoPlayerSimulator;
 import dab.gui.intro.DaIntro;
 import dab.gui.mainpanels.DaMMenu;
 import dab.gui.mainpanels.GameInterface;
 import dab.gui.mainpanels.MenuHandler;
 import dab.gui.mainpanels.SinglePlayerInterface;
 import dab.gui.mainpanels.TwoPlayerInterface;
 import java.awt.Component;
 import javax.swing.JFrame;
 import javax.swing.JLayeredPane;
 import javax.swing.JPanel;
 
 
 /**
  *
  * @author eduard
  */
 public class MainWindow extends JFrame {
     private final static int DEFAULT_DIFFICULTY = 1;    //to check with the one in options
     
     public static void main(String[] args) {
         java.awt.EventQueue.invokeLater(new Runnable() {
 
             @Override
             public void run() {
                 MainWindow mw = new MainWindow();
                 mw.setExtendedState(MainWindow.MAXIMIZED_BOTH);
                 mw.setVisible(true);
                 //mw.showMenu();
                //mw.showIntro();
                mw.startSinglePlayerGame(new SinglePlayerSimulator("edd"));
                //mw.startTwoPlayerGame(new TwoPlayerSimulator("edd", "miniedd"));
             }
         });
     }
    // private DaMMenu menu;
     private Component currentComponent = null;
     private int difficulty;
     private boolean music;
     private GameInterface gameInterface = null;
     private JPanel currentMenu = null;
     private String oldUserName=null; 
     private String oldUserName2=null;
     private boolean gameOver;
     private AbstractSimulator currentSimulator;
     
     public MainWindow() {
         setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         setSize(1366, 768);
         gameOver = false;
         difficulty = DEFAULT_DIFFICULTY;
         currentSimulator = null;
         // create the menu
         
         music = true;      
     }
     
     public void showIntro() {
         DaIntro intro = new DaIntro(this);
         changeToPanel(intro);
     }
 
     public void showMenu() {
         DaMMenu menu = new DaMMenu(this);
         changeToPanel(menu);      
     }
     
     /*public void startGame(AbstractSimulator sim, boolean onePlayerMode) {
         currentSimulator = sim;
         oldUserName = currentSimulator.getUsername();
 
         if(onePlayerMode){
             gameInterface = new SinglePlayerInterface(this, sim);
         } else {
             gameInterface = new TwoPlayerInterface(this, sim);
         }
         simulator.setDifficulty(difficulty);
         //simulator.setPlayerMode(onePlayerMode);
         changeToPanel(gameInterface);
     }*/
 
     public void startSinglePlayerGame(SinglePlayerSimulator sim) {
         currentSimulator = sim;
         sim.setDifficulty(difficulty);
         oldUserName = sim.getUsername();
         
         gameInterface = new SinglePlayerInterface(this, sim);
         changeToPanel(gameInterface);
     }
     
     public void startTwoPlayerGame(TwoPlayerSimulator sim) {
         currentSimulator = sim;
         sim.setDifficulty(difficulty);
         oldUserName = sim.getUsername1();
         oldUserName2 = sim.getUsername2();
         
         gameInterface = new TwoPlayerInterface(this, sim);
         changeToPanel(gameInterface);
     }
     
     public void close() {
         setVisible(false);
         dispose();
     }
 
     public void changeToPanel(Component p) {
         if (currentComponent != null) {
             currentComponent.setVisible(false);
             getContentPane().remove(currentComponent);
         }
         getContentPane().add(p);
         currentComponent = p;
         p.setVisible(true);
         
         // put the new component in focus
         p.requestFocusInWindow();
     }
     
     public void changeMenu(MenuHandler p){
         if (currentMenu != null) {
             currentMenu.setVisible(false);
             p.getInvoker().remove(currentMenu);
         }
       
         
        //p.setLayout(new BoxLayout(invoker,BoxLayout.Y_AXIS)); 
         p.getInvoker().add(p, JLayeredPane.POPUP_LAYER);
         currentMenu = p;
         p.setVisible(true);         
         p.requestFocus();
     }
     
     public void removeMenu(JPanel p, JLayeredPane invoker){
         p.setVisible(false);
         invoker.remove(p);
         currentMenu = null;
     }
     
     public void setDifficulty (int i) {      
         if(currentSimulator != null){               
             currentSimulator.setDifficulty(i);
         }
         difficulty = i;       
     }
     
     public int getDifficulty(){
         return difficulty;
     }
     
     public void setMusic() {
         music = !music;
         if(gameInterface!=null) {
             gameInterface.handleMusic();
         }
     }
     
     public boolean getMusic(){
         return music;
     }
     
     public void stopMusic(){
         if(gameInterface != null) {
             gameInterface.stopMusic();
         }
     }
     
     public void resume() {
         gameInterface.resume();
     }
     
     public String getUserName(){
         return oldUserName;
     }
     
     public String getUserName2(){
         return oldUserName2;
     }
     
     public void setGameOver(boolean gameOver){
         this.gameOver = gameOver;
     }
     
     public boolean getGameOver(){
         return gameOver;
     }
     
     public void saveGame() throws JsonProcessingException {
         if (currentSimulator instanceof SinglePlayerSimulator) {
             ((SinglePlayerSimulator)currentSimulator).saveGame();
         }
     }
 }
