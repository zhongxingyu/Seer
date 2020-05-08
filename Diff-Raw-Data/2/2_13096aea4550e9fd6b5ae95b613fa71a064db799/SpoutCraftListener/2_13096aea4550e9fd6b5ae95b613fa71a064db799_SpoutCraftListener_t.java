 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.spiceking.plugins.spoutwallet.listeners;
 
 import java.util.HashMap;
 import me.spiceking.plugins.spoutwallet.SpoutWallet;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  *
  * @author Kyle
  */
 public class SpoutCraftListener extends SpoutListener {
     
     SpoutWallet plugin;
     
     public SpoutCraftListener(SpoutWallet plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
         
         // Should not happen
         if(!event.getPlayer().isSpoutCraftEnabled()) {
            event.getPlayer().sendMessage("This server uses SpoutCraft to display your balance.");
             event.getPlayer().sendMessage("Install SpoutCraft from http://goo.gl/UbjS1 to see it.");
             return;
         }
         
         drawGUI(event.getPlayer());
     }
 
     private void drawGUI(SpoutPlayer sp) {
         
         //This is the code to start the funds lable
         GenericLabel fundsLabel = new GenericLabel("");
         // Todo: fundsLable: config the location and colour
         fundsLabel.setHexColor(Integer.parseInt("FFFFFF", 16)).setX(10).setY(plugin.ySetting);
         HashMap fundsLabels = plugin.getFundsLabels();
         fundsLabels.put(sp.getName(), fundsLabel.getId());
         sp.getMainScreen().attachWidget(fundsLabel);
         
         if (plugin.showRank){
             //This is the code to start the rank lable
             GenericLabel rankLabel = new GenericLabel("");
             // Todo: rankLable: config the location and colour
             rankLabel.setHexColor(Integer.parseInt("FFFFFF", 16)).setX(10).setY(plugin.ySetting+10);
             HashMap rankLabels = plugin.getRankLabels();
             rankLabels.put(sp.getName(), rankLabel.getId());
             sp.getMainScreen().attachWidget(rankLabel);
         }
     }
 }
