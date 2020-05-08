 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package me.olloth.lordned.seasons.listener;
 
 import me.olloth.lordned.seasons.Seasons;
 import me.olloth.lordned.seasons.util.Enums;
 import org.getspout.spoutapi.event.spout.SpoutCraftEnableEvent;
 import org.getspout.spoutapi.event.spout.SpoutListener;
 
 /**
  *
  * @author Olloth
  */
 public class SpoutCraftListener extends SpoutListener {
     
     Seasons plugin;
     
     public SpoutCraftListener(Seasons plugin) {
         this.plugin = plugin;
     }
 
     @Override
     public void onSpoutCraftEnable(SpoutCraftEnableEvent event) {
 
         if(!event.getPlayer().isSpoutCraftEnabled()) {
             event.getPlayer().sendMessage("This server uses SpoutCraft for the Seasons plugin.");
             event.getPlayer().sendMessage("Install SpoutCraft from http://goo.gl/UbjS1 to see it.");
         }
         event.getPlayer().setTexturePack(Enums.getSeasonUrl());
     }
     
     
     
 }
