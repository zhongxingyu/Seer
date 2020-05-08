 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.thedeadlybutter.ldgame;
 
 import java.awt.Color;
 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 
 /**
  *
  * @author Butters
  */
 public class GUIGame implements GUI{
 
     @Override
     public void init(Main game) {
         
     }
 
     @Override
     public void render(Main game, Graphics2D g) {
         g.setColor(Color.gray);
         g.fillRect(0, 0, game.getWidth(), game.getHeight());
         game.renderCity(g);
     }
 
     @Override
     public void handleClick(Main game, int x, int y) {
         Building b = game.getBuildingAt(x, y);
         if(b != null){
             game.getSound().playClick();
             game.switchGUI(new GUIBuilding(b));
         }
     }
 
     @Override
     public void update(Main game) {
        if(!defeatedCity(game)){
            game.switchGUI(new GUIWon());
        }
         if(game.getVirusGun().getMoney() < 0){
             game.switchGUI(new GUILostGame());
         }
         if(game.getInput().isKeyDown(KeyEvent.VK_SPACE)){
             game.switchGUI(new GUIVirusGun());
         }
     }
     
     private boolean defeatedCity(Main game){
         for(Building b : game.getBuildings()){
             if(b.isDefeated()){
                 return false;
             }
         }
         return true;
     }
     
 }
