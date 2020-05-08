 package vooga.rts.player;
 
 import java.awt.Graphics2D;
 import vooga.rts.IGameLoop;
 import vooga.rts.commands.Command;
 import vooga.rts.controller.Controller;
 import vooga.rts.gamedesign.sprite.gamesprites.interactive.InteractiveEntity;
 import vooga.rts.manager.Manager;
 

 /**
  * Sends commands to its unit manager from either the human input, the network
  * input or the ai input.]
  * 
  * @author challenherzberg-brovold
 * 
  */
 public class Player implements Controller, IGameLoop {
 
    protected Manager myManager;
     private int myTeamID;
 
     public Player (int id) {
         myManager = new Manager();
         myTeamID = id;
     }
 
     @Override
     public void sendCommand (Command command) {
         // TODO Auto-generated method stub
     }
 
     public Manager getManager () {
         return myManager;
     }
 
     public void add (InteractiveEntity unit) {
         myManager.add(unit);
     }
 
     @Override
     public void paint (Graphics2D pen) {
         myManager.paint(pen);
     }
 
     @Override
     public void update (double elapsedTime) {
         myManager.update(elapsedTime);
     }
 }
