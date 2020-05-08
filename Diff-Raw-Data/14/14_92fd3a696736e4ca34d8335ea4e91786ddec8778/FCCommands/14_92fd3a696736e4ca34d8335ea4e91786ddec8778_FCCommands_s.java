 /*
  * FC Commands - Basic commands for a Minecraft CraftBukkit server.
  * Copyright (C) 2011, 2012 Joel Green <jgreen@itechsolutions.biz>
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package biz.itechsolutions.bukkitplugin.FCCommands;
 
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  * @author Joel Green
  * @version 0.0.1
  * 
  * The FCCommands class is the main class for the plugin.
  *
  */
 public class FCCommands extends JavaPlugin {
     Logger log = Logger.getLogger("Minecraft.FCCommands");
 
     public void onDisable() {
     	saveConfig();
     	consoleMsg("Plugin Disabled");
     }
 
     public void onEnable() {
     	getConfig();
         consoleMsg("Plugin Version " + this.getDescription().getVersion() + " Enabled");
     }
     
     private void consoleMsg(String msg) {
     	log.info("[" + this.getDescription().getName() + "] " + msg);
     }
     
     public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
     	Player player = null;
 
     	if (sender instanceof Player) {
     		player = (Player) sender;
     	}
     	
    	if(cmd.getName().equalsIgnoreCase("fcc")) {
     		if (args[0].equalsIgnoreCase("reload")) {
     			reloadConfig();
     			consoleMsg("Config reloaded");
     			if (player != null) {
     				sender.sendMessage("FCCommands configureation reloaded.");
     			}
     			return true;
      		} else {
    			if (player == null) {
    				consoleMsg(cmd.getName() + " with no arguments was run from the console!");
	    		} else {
	    			consoleMsg(cmd.getName() + " was run with no arguments by " + sender.getName() + "!");
	    		}
	    		sender.sendMessage("You ran the " + cmd.getName() + " command with no arguments!!");
 	    		return false;
     		}
     	}
     	return false;
     }
 }
