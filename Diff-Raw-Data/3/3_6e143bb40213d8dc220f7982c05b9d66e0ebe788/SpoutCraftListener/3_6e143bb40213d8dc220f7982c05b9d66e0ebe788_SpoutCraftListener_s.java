 /*
  * This file is part of SpoutWallet.
  * 
  * SpoutWallet is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  
  * SpoutWallet is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
 
  * You should have received a copy of the GNU General Public License
  * along with SpoutWallet.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package me.spiceking.plugins.spoutwallet.listeners;
 
 import java.util.HashMap;
 import me.spiceking.plugins.spoutwallet.SpoutWallet;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 import org.getspout.spoutapi.player.SpoutPlayer;
 
 /**
  *
  * @author Kyle
  */
 public class SpoutCraftListener extends SpoutListener {
     
     SpoutWallet plugin;
     
     public Integer fundsY;
     public Integer rankY;
     
     public SpoutCraftListener(SpoutWallet plugin) {
         this.plugin = plugin;
         //Position offset for the new anchor system
         switch(plugin.location) {
             case TOP_LEFT:
             case TOP_CENTER:
             case TOP_RIGHT:
                 fundsY = plugin.ySetting;
                 rankY = plugin.ySetting + 10;
                 break;
             case CENTER_LEFT:
             case CENTER_CENTER:
             case CENTER_RIGHT:
                 fundsY = plugin.ySetting - 5;
                 rankY = plugin.ySetting + 5;
                 break;
             case BOTTOM_LEFT:
             case BOTTOM_CENTER:
             case BOTTOM_RIGHT:
                 fundsY = plugin.ySetting - 10;
                 rankY = plugin.ySetting;
                 break;
             default:
                 System.out.print("[SpoutWallet] Somehow, I can't see the offset needed!");
                 try {
                     plugin.location = WidgetAnchor.TOP_LEFT;
                 } catch (java.lang.IllegalArgumentException e){
                     System.err.print("[SpoutWallet] Uh oh, Spout broke something! Tell Spice_King that WidgetAnchor enum has changed!");
                 }
                 break;
         }
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
         //Perms, yay!
         if (sp.hasPermission("SpoutWallet.Use")){
             plugin.setWallet(sp, true);
         } else {
             plugin.setWallet(sp, false);
         }
         //This is the code to start the funds lable
         GenericLabel fundsLabel = new GenericLabel("");
         // Todo: fundsLable: config the location and colour
         fundsLabel.setTextColor(plugin.colorFunds).setAnchor(plugin.location);
         fundsLabel.setAlign(plugin.location);
         fundsLabel.setX(plugin.xSetting).setY(fundsY);
         HashMap fundsLabels = plugin.getFundsLabels();
         fundsLabels.put(sp.getName(), fundsLabel.getId());
         sp.getMainScreen().attachWidget(plugin, fundsLabel);
         
         if (plugin.showRank){
             //This is the code to start the rank lable
             GenericLabel rankLabel = new GenericLabel("");
             // Todo: rankLable: config the location and colour
             rankLabel.setTextColor(plugin.colorRank).setAnchor(plugin.location);
             rankLabel.setAlign(plugin.location);
             rankLabel.setX(plugin.xSetting).setY(rankY);
             HashMap rankLabels = plugin.getRankLabels();
             rankLabels.put(sp.getName(), rankLabel.getId());
             sp.getMainScreen().attachWidget(plugin, rankLabel);
         }
         plugin.setWallet(sp, true);
     }
 }
