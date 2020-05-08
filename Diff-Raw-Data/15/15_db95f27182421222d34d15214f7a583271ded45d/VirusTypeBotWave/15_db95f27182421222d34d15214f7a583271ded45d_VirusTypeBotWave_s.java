 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.thedeadlybutter.ldgame;
 
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import javax.imageio.ImageIO;
 
 /**
  *
  * @author Butters
  */
 public class VirusTypeBotWave extends Virus{
 
     public VirusTypeBotWave(){
         super("BotWave", 150);
         try{
            this.setSprite(ImageIO.read(this.getClass().getResource("res/BotWaveIcon.png")));
         } catch (IOException ex) {
             ex.printStackTrace();
         }          
     }
     
     @Override
     public void handleSystem(Main game, AntiSystem system, Building building, VirusGun gun) {
         if(system.getName().equalsIgnoreCase("firewall")){
             if(system.doAction()){
                 building.destoryCurrentSystem();
                 return;
             }        
         }
         else if(system.getName().equalsIgnoreCase("Quarantine")){
             building.destoryCurrentSystem(); //Bot Wave only way to destroy this
         }
         else if(system.getName().equalsIgnoreCase("SearchDestory")){
             if(system.doAction()){
                 return; //Was destroyed, no data sent back
             }
             else{
                 building.foundSystemButDidNotDestory(system);
                 return;
             }
         }
         else if(system.getName().equalsIgnoreCase("packetreroute")){
             if(system.doAction()){
                 building.foundSystemButDidNotDestory(system);
                 return;
             }
             else{
                 building.destoryCurrentSystem();
                 return;
             }
         }
         else if(system.getName().equalsIgnoreCase("backup")){
             if(system.doAction()){
                 building.destroyCurrentSystemAndReplace();
                 return;
             }
         }
         else if(system.getName().equalsIgnoreCase("virusscan")){
             if(system.doAction()){
                 building.foundSystemButDidNotDestory(system);
                 return;
             }
             else{
                 building.destoryCurrentSystem();
                 return;
             }
         }
         else if(system.getName().equalsIgnoreCase("encryption")){
             building.foundSystemButDidNotDestory(system);
             return; //Bot can't do anything about this
         }       
         else if(system.getName().equalsIgnoreCase("monitor")){
             building.foundSystemButDidNotDestory(system);
         }        
     }
     
 }
