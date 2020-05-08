 /**
  * This file, MineQuest.java, is part of MineQuest:
  * A full featured and customizable quest/mission system.
  * Copyright (C) 2012 The MineQuest Team
  * 
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
  **/
 package com.theminequest.MineQuest;
 
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.theminequest.MQCoreEvents.RegisterEvents;
 import com.theminequest.MineQuest.AbilityAPI.AbilityManager;
 import com.theminequest.MineQuest.Editable.EditableManager;
 import com.theminequest.MineQuest.EventsAPI.EventManager;
 import com.theminequest.MineQuest.Frontend.Command.CommandListener;
 import com.theminequest.MineQuest.Frontend.QuestSign.SignFrontend;
 import com.theminequest.MineQuest.Player.PlayerManager;
 import com.theminequest.MineQuest.Quest.QuestManager;
 import com.theminequest.MineQuest.Tasks.TaskManager;
 import com.theminequest.MineQuest.Team.TeamManager;
 import com.theminequest.MineQuest.Utils.UtilManager;
 
 public class MineQuest extends JavaPlugin {
 
 	/*
 	 * I NEED IT >_< But we could use
 	 * Bukkit.getPluginManager().getPlugin("MineQuest")...
 	 */
 	public static Permission permission = null;
 	public static Economy economy = null;
 	public static MineQuest activePlugin = null;
 	public static AbilityManager abilityManager = null;
 	public static EventManager eventManager = null;
 	public static EditableManager editableManager = null;
 	public static TaskManager taskManager = null;
 	public static QuestManager questManager = null;
 	public static PlayerManager playerManager = null;
 	public static TeamManager teamManager = null;
 	public static UtilManager utilManager = null;
 	public static QuestConfig configuration = null;
 	public static SQLExecutor sqlstorage = null;
 	private static PluginDescriptionFile description = null;
 	
 	public static SignFrontend signFrontend = null;
 	public static CommandListener commandFrontend = null;
 
 	public static void log(String msg) {
 		log(Level.INFO, msg);
 	}
 
 	public static void log(Level level, String msg) {
 		Logger.getLogger("Minecraft").log(level, "[MineQuest] " + msg);
 	}
 
 	public static String getVersion() {
 		return description.getVersion();
 	}
 
 	public static String getPluginName() {
 		return description.getName();
 	}
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> permissionProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null) {
 			permission = permissionProvider.getProvider();
 		}
 		return (permission != null);
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 		return (economy != null);
 	}
 
 	@Override
 	public void onEnable() {
 		if (!getDataFolder().exists())
 			getDataFolder().mkdirs();
 		description = this.getDescription();
 		activePlugin = this;
 		if (description.getVersion().equals("unofficialDev")){
 			MineQuest.log(Level.SEVERE,"[Core] You're using an unofficial dev build!");
 			MineQuest.log(Level.SEVERE,"[Core] I CANNOT keep track of changes with this.");
 		}
 		configuration = new QuestConfig();
 		sqlstorage = new SQLExecutor();
 		abilityManager = new AbilityManager();
 		getServer().getPluginManager().registerEvents(abilityManager, this);
 		eventManager = new EventManager();
 		getServer().getPluginManager().registerEvents(eventManager, this);
 		editableManager = new EditableManager();
 		getServer().getPluginManager().registerEvents(editableManager, this);
 		taskManager = new TaskManager();
 		getServer().getPluginManager().registerEvents(taskManager, this);
 		questManager = new QuestManager();
 		getServer().getPluginManager().registerEvents(questManager, this);
 		playerManager = new PlayerManager();
 		getServer().getPluginManager().registerEvents(playerManager, this);
 		teamManager = new TeamManager();
 		getServer().getPluginManager().registerEvents(teamManager, this);
 		utilManager = new UtilManager();
 		getServer().getPluginManager().registerEvents(utilManager, this);
 		if (!setupPermissions())
 			log(Level.SEVERE,"Permissions could not be setup!");
 		if (!setupEconomy())
 			log(Level.SEVERE,"Economy could not be setup!");
 		RegisterEvents.registerEvents();
 		
 		// core frontends
 		signFrontend = new SignFrontend();
 		getServer().getPluginManager().registerEvents(signFrontend, this);
 		commandFrontend = new CommandListener();
 		getCommand("minequest").setExecutor(commandFrontend);
 		getCommand("quest").setExecutor(commandFrontend);
 		getCommand("spell").setExecutor(commandFrontend);
 		getCommand("npc").setExecutor(commandFrontend);
 		getCommand("class").setExecutor(commandFrontend);
 	}
 
 	@Override
 	public void onDisable() {
 		playerManager.saveAll();
 		description = null;
 		activePlugin = null;
 		abilityManager = null;
 		eventManager = null;
 		editableManager = null;
 		taskManager = null;
 		questManager = null;
 		playerManager = null;
 		teamManager = null;
 		utilManager = null;
 		configuration = null;
 		sqlstorage = null;
 		permission = null;
 		economy = null;
 	}
 }
