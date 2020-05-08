 /**
 * AutoCraft - a Bukkit plugin
 * Copyright (C) 2011-2013 MineSworn
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
 package net.dmulloy2.autocraft;
 
 import java.util.MissingResourceException;
 import java.util.logging.Level;
 
 import lombok.Getter;
 import net.dmulloy2.autocraft.commands.CommandHandler;
 import net.dmulloy2.autocraft.legacy.FileConverter;
 import net.dmulloy2.autocraft.listeners.PlayerListener;
 import net.dmulloy2.autocraft.permissions.PermissionHandler;
 import net.dmulloy2.autocraft.ships.ShipManager;
 import net.dmulloy2.autocraft.util.FormatUtil;
 import net.dmulloy2.autocraft.util.LogHandler;
 import net.dmulloy2.autocraft.util.ResourceHandler;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AutoCraft extends JavaPlugin {
 	private @Getter PermissionHandler permissionHandler;
 	private @Getter ResourceHandler resourceHandler;
 	private @Getter CommandHandler commandHandler;
 	private @Getter LogHandler logHandler;
 
 	private @Getter DataHandler dataHandler;
 	private @Getter ShipManager shipManager;
 	private @Getter boolean factionsEnabled;
 	
 	private @Getter String prefix = FormatUtil.format("&4[&6&lAC&4]&6 ");
 	
 	@Override
 	public void onEnable() {
 		long start = System.currentTimeMillis();
 		
 		saveDefaultConfig();
 		
		new FileConverter(this).run();
		
 		permissionHandler = new PermissionHandler(this);
 		commandHandler = new CommandHandler(this);
 		logHandler = new LogHandler(this);
 		
 		saveResource("messages.properties", true);
 		resourceHandler = new ResourceHandler(this, getClassLoader());

 		shipManager = new ShipManager();
 		
 		dataHandler = new DataHandler(this);
 		dataHandler.load();
 		
 		if (getConfig().getBoolean("factionsProtectionsEnabled") && 
 				(getServer().getPluginManager().isPluginEnabled("SwornNations") || 
 						getServer().getPluginManager().isPluginEnabled("Factions"))) {
 			
 			factionsEnabled = true;
 			
 		}
 		
 		if (factionsEnabled) {
 			logHandler.log("Factions plugin found. Enabling integration.");
 		} else  {
 			logHandler.log("Factions plugin not found. Disabling integration.");
 		}
 
 		getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
 		
 		long finish = System.currentTimeMillis();
 		
 		logHandler.log("{0} has been enabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 	
 	@Override
 	public void onDisable() {
 		long start = System.currentTimeMillis();
 		
 		dataHandler.clearMemory();
 		shipManager.clearMemory();
 		
 		long finish = System.currentTimeMillis();
 		
 		logHandler.log("{0} has been disabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 
 	public String getMessage(String string) {
 		try {
 			return resourceHandler.getMessages().getString(string);
 		} catch (MissingResourceException ex) {
 			logHandler.log(Level.WARNING, "Messages locale is missing key for: {0}", string);
 			return null;
 		}
 	}
 }
