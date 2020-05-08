 /*
  * LabPlugin
  * Copyright (C) 2013 beaucoralk <http://www.liveatblocks.fr> and contributors
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>.
  */
 package com.beaucoralk.bukkit.labplugin;
 
import com.beaucoralk.bukkit.labplugin.commands.CpAddBlock;
 import com.beaucoralk.bukkit.labplugin.commands.CpEnchant;
 import com.beaucoralk.bukkit.labplugin.commands.CpPlayers;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * This class is the main entry point for Live at Blocks Plugin.
  *
  * @author beaucoralk
  */
 public class LabPlugin extends JavaPlugin implements Listener {
     
     public final static String TAG_CHAT = "[LABPlugin]";
     
     @Override
     public void onEnable(){
         LoadCommands();
         logInfo("has been enabled !");
     }
  
     @Override
     public void onDisable() {
         logInfo("has been disabled !");
     }
     
     public void LoadCommands() {
         getCommand("cpenchant").setExecutor(new CpEnchant(this));
         getCommand("cpplayers").setExecutor(new CpPlayers(this));
        getCommand("cpaddblock").setExecutor(new CpAddBlock(this));
     }
     
     public void logInfo(String _info) {
         getLogger().info(new StringBuilder(TAG_CHAT + " " + _info).toString());
     }
 }
