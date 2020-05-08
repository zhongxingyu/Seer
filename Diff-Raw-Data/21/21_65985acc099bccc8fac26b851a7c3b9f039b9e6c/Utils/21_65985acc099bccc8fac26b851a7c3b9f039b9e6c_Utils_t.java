 /*
  The MIT License
 
  Copyright (c) 2012 Zloteanu Nichita (ZNickq) and Andre Mohren (IceReaper)
 
  Permission is hereby granted, free of charge, to any person obtaining a copy
  of this software and associated documentation files (the "Software"), to deal
  in the Software without restriction, including without limitation the rights
  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  copies of the Software, and to permit persons to whom the Software is
  furnished to do so, subject to the following conditions:
 
  The above copyright notice and this permission notice shall be included in
  all copies or substantial portions of the Software.
 
  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
  THE SOFTWARE.
  */
 
 package net.morematerials.manager;
 
 import java.util.logging.Level;
import java.util.logging.Logger;

import jline.internal.Log;
 
 import net.morematerials.Main;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Utils {
 	private Main plugin;
 
 	protected Utils(Main plugin) {
 		this.plugin = plugin;
 	}
 
 	public boolean hasPermission(CommandSender sender, String perm, boolean verbose) {
 		// Allow console
 		if (!(sender instanceof Player)) {
 			return true;
 		// Or players with this permission
 		} else if (sender.hasPermission("morematerials.*")) {
 			return true;
 		// Or players with this permission
 		} else if (((Player) sender).hasPermission(perm)) {
 			return true;
 		}
 		
 		// Display message in verbose mode.
 		if (verbose) {
 			sender.sendMessage(getMessage("You do not have permission to do that! You need " + perm + "!", Level.SEVERE));
 		}
 		return false;
 	}
 
 	// Generalize all chat output!
 	public String getMessage(String logMessage) {
 		return this.getMessage(logMessage, Level.INFO);
 	}
 
 	public String getMessage(String logMessage, Level level) {
 		if (level == Level.WARNING) {
 			return ChatColor.GREEN + "[" + this.plugin.getDescription().getName() + "] " + ChatColor.YELLOW + logMessage;
 		} else if (level == Level.SEVERE) {
 			return ChatColor.GREEN + "[" + this.plugin.getDescription().getName() + "] " + ChatColor.RED + logMessage;
 		}
 		return ChatColor.GREEN + "[" + this.plugin.getDescription().getName() + "] " + ChatColor.WHITE + logMessage;
 	}
 
 	// Generalize all console output!
 	public void log(String logMessage) {
 		this.log(logMessage, Level.INFO);
 	}
 
 	public void log(String logMessage, Level level) {
		Logger loglevel = Logger.getLogger("Minecraft");
		if (level == Level.WARNING) {			
			loglevel.warning("[" + this.plugin.getDescription().getName() + "] Warning: " + logMessage);
		} else if (level == Level.SEVERE) {			
			loglevel.severe("[" + this.plugin.getDescription().getName() + "] ERROR: " + logMessage);			
 		} else {
			loglevel.info("[" + this.plugin.getDescription().getName() + "] " + logMessage);
 		}
 	}
 }
