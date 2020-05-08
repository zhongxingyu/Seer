 
 package com.ninjacannon.quantum;
 
 import javax.swing.JOptionPane;
 import org.newdawn.slick.AppGameContainer;
 import org.newdawn.slick.GameContainer;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.state.StateBasedGame;
 
 /**
  *
  * @author Dan Cannon
  */
 public class QuantumGame extends StateBasedGame
 {
     public static final int GAMEWIDTH = 1024;
     public static final int GAMEHEIGHT = 768;
     
     public static final int MAINMENUSTATE = 0;
     public static final int GAMEPLAYSTATE = 1;
     public static final int OVERMAPSTATE = 2;
     
    public static final String version = "0.2.00a";
     
     public QuantumGame()
     {
         super("Quantum AST");
     }
     
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) throws SlickException
     { 
       boolean windowed = false;
       int n = JOptionPane.showConfirmDialog(null, "Would you like to run this game fullscreen?",
               "QuantumAST", JOptionPane.YES_NO_OPTION);
       if(n == 0){
           windowed = true;
       }
       AppGameContainer app = new AppGameContainer(new QuantumGame());
       app.setMaximumLogicUpdateInterval(20);
       app.setMinimumLogicUpdateInterval(20);
       app.setDisplayMode(GAMEWIDTH, GAMEHEIGHT, windowed);
       app.setResizable(false);
       app.setShowFPS(true);
       app.start();
     }
 
     @Override
     public void initStatesList(GameContainer gc) throws SlickException
     {
         this.addState(new MainMenuState(MAINMENUSTATE));
         this.addState(new OvermapState(OVERMAPSTATE));
     }
 }
