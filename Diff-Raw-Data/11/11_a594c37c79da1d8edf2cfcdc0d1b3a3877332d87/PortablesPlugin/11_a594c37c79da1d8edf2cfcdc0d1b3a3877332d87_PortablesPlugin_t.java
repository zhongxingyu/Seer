 /*
  * This file is part of Portables.
  *
  * Copyright (c) 2012, AlmuraDev <http://www.almuramc.com/>
  * Portables is licensed under the Almura Development License.
  *
  * Portables is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * As an exception, all classes which do not reference GPL licensed code
  * are hereby licensed under the GNU Lesser Public License, as described
  * in Almura Development License.
  *
  * Portables is distributed in the hope that it will be useful,
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
 package com.almuramc.portables.bukkit;
 
 import com.almuramc.portables.bukkit.command.PortablesCommands;
 import com.almuramc.portables.bukkit.configuration.PortablesConfiguration;
 import com.almuramc.portables.bukkit.input.PortablesEnchantmentTableDelegate;
 import com.almuramc.portables.bukkit.input.PortablesWorkbenchDelegate;
 import com.almuramc.portables.bukkit.util.Dependency;
 
 import org.getspout.spoutapi.SpoutManager;
 import org.getspout.spoutapi.keyboard.Keyboard;
 
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class PortablesPlugin extends JavaPlugin {
 	private static Dependency hooks;
 	private static PortablesConfiguration cached;
 
 	@Override
 	public void onLoad() {
 		cached = new PortablesConfiguration(this);
 		cached.load();
 		hooks = new Dependency(this);
 	}
 
 	@Override
 	public void onEnable() {
 		System.out.println(cached.useSpout());
 		System.out.println(hooks.isSpoutPluginEnabled());
		try {
			//Dynamic bindings means we need to gurrantee that SpoutPlugin is there in the classloader else logs get spammed
			Class.forName("org.getspout.spoutapi.Spout");
			if (cached.useSpout() && hooks.isSpoutPluginEnabled()) {
 				SpoutManager.getKeyBindingManager().registerBinding("Enchantment Table", Keyboard.valueOf(cached.getEnchantmentTableHotkey()), "Opens the portable enchantment table", new PortablesEnchantmentTableDelegate(), this);
 				SpoutManager.getKeyBindingManager().registerBinding("Workbench", Keyboard.valueOf(cached.getWorkbenchHotkey()), "Opens the portable workbench", new PortablesWorkbenchDelegate(), this);
			}
		} catch (ClassNotFoundException ignore) {}
 
 		if (hooks.isVaultPluginEnabled()) {
 			if (cached.useEconomy()) {
 				hooks.setupVaultEconomy();
 			}
 			hooks.setupVaultPermissions();
 		}
 		getCommand("portables").setExecutor(new PortablesCommands(this));
 	}
 
 	public static Dependency getHooks() {
 		return hooks;
 	}
 
 	public static PortablesConfiguration getCached() {
 		return cached;
 	}
 }
