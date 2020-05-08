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
 package com.minesworn.autocraft;
 
 import org.bukkit.command.CommandSender;
 
 import com.minesworn.autocraft.commands.ACCommandRoot;
 import com.minesworn.autocraft.core.SPlugin;
 import com.minesworn.autocraft.core.commands.CmdHelp;
 import com.minesworn.autocraft.core.util.SLang;
 import com.minesworn.autocraft.listeners.PlayerListener;
 import com.minesworn.autocraft.ships.ACPropertiesManager;
 import com.minesworn.autocraft.ships.ACShipManager;
 
 public class Autocraft extends SPlugin {
 
 	public static Autocraft p;
 	public static ACPropertiesManager propertiesmanager;
 	public static ACShipManager shipmanager;
 	public static boolean factionsEnabled;
 	
 	@Override
 	public void onEnable() {
 		long start = System.currentTimeMillis();
 		
 		p = this;
 		
 		preEnable();
 		
 		lang = new SLang(this);
 		lang.load();
 		
 		Config.load();
 		
 		shipmanager = new ACShipManager();
 		propertiesmanager = new ACPropertiesManager(p);
 		commandRoot = new ACCommandRoot(p);
 		
 		if (Config.factionsProtectionsEnabled && 
 				(getServer().getPluginManager().isPluginEnabled("SwornNations") || 
 						getServer().getPluginManager().isPluginEnabled("Factions"))) {
 			
 			factionsEnabled = true;
 			
 		}
 		
 		if (factionsEnabled) {
 			log("Factions plugin found. Enabling integration.");
 		} else  {
 			log("Factions plugin not found. Disabling integration.");
 		}
 
 		registerEvents();
 		
 		long finish = System.currentTimeMillis();
 		
 		log("{0} has been enabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 	
 	@Override
 	public void onDisable() {
 		long start = System.currentTimeMillis();
 		
 		preDisable();
 		
 		long finish = System.currentTimeMillis();
 		
 		log("{0} has been disabled ({1}ms)", getDescription().getFullName(), finish - start);
 	}
 	
 	@Override
 	public void newHelpCommand(CommandSender sender, String[] args) {
 		 new CmdHelp<Autocraft>().execute(sender, args);
 	}
 	
 	public void registerEvents() {
		getServer().getPluginManager().registerEvents(new PlayerListener(), p);
 	}
 	
 }
