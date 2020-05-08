 /*
  * This file is part of Aqualock.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Aqualock is licensed under the Almura Development License.
  *
  * Aqualock is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Aqualock is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License,
  * the GNU Lesser Public License (for classes that fulfill the exception)
  * and the Almura Development License along with this program. If not, see
  * <http://www.gnu.org/licenses/> for the GNU General Public License and
  * the GNU Lesser Public License.
  */
 package com.almuramc.aqualock.bukkit;
 
 import com.almuramc.aqualock.bukkit.command.AqualockCommands;
 import com.almuramc.aqualock.bukkit.configuration.AqualockConfiguration;
 import com.almuramc.bolt.lock.Lock;
 import com.almuramc.bolt.registry.CommonRegistry;
 import com.almuramc.bolt.storage.SqlStorage;
 import com.almuramc.bolt.storage.Storage;
 import com.alta189.simplesave.sqlite.SQLiteConfiguration;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AqualockPlugin extends JavaPlugin {
 	private static final CommonRegistry registry;
 	private static Storage backend;
 	private static Permission permission;
 	private static Economy economy;
 	private static AqualockConfiguration configuration;
 	private static AqualockPlugin instance;
 
 	static {
 		registry = new CommonRegistry();
 	}
 
 	@Override
 	public void onDisable() {
 		backend.onUnLoad();
 	}
 
 	@Override
 	public void onEnable() {
 		if (!setupPermissions()) {
 			throw new RuntimeException("Failed to intialize Vault for permissions!");
 		}
 		if (!setupEconomy()) {
 			Bukkit.getLogger().info(getPrefix() + "Failed to initialize Vault for economy, skipping...");
 		}
 		backend = new SqlStorage(new SQLiteConfiguration(), getDataFolder());
 		backend.onLoad();
 		for (Lock lock : backend.getAll()) {
 			registry.addLock(lock);
 		}
 		this.getCommand("aqualock").setExecutor(new AqualockCommands(this));
 		this.getServer().getPluginManager().registerEvents(new AqualockListener(this), this);
 		configuration = new AqualockConfiguration(this);
 	}
 
 	@Override
 	public void onLoad() {
 		instance = this;
 	}
 
 	public static AqualockPlugin getInstance() {
 		return instance;
 	}
 
 	public static CommonRegistry getRegistry() {
 		return registry;
 	}
 
 	public static Storage getBackend() {
 		return backend;
 	}
 
 	public static AqualockConfiguration getConfiguration() {
 		return configuration;
 	}
 
 	public static String getPrefix() {
 		return "[" + ChatColor.AQUA + "Aqualock" + ChatColor.WHITE + "] ";
 	}
 
 	public static Permission getPermissions() {
 		return permission;
 	}
 
 	public static Economy getEconomies() {
 		return economy;
 	}
 
 	///////////////////////////////////////////////////////////////////////////////////////////////////////////////////
 
 	private boolean setupPermissions() {
 		RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
 		permission = rsp.getProvider();
 		return permission != null;
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
 		economy = rsp == null ? null : rsp.getProvider() == null ? null : rsp.getProvider();
 		return economy != null;
 	}
 }
