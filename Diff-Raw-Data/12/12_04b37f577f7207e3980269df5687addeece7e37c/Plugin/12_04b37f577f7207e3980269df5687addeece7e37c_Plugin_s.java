 /* cBorder
  * Copyright (C) 2013 Norbert Kawinski (norbert.kawinski@gmail.com)
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
 
 package castro.cBorder;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.scheduler.BukkitScheduler;
 
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 
 import castro.base.plugin.CPlugin;
 import castro.base.plugin.CPluginSettings;
 
 
 // TODO: Store unloaded chunks in array and then reload them after limit change
 public class Plugin extends CPlugin
 {
 	protected static Plugin instance;
 	
 	
 	WorldEditPlugin getWorldEdit()
 	{
 		return (WorldEditPlugin)getServer().getPluginManager().getPlugin("WorldEdit");
 	}
 	
 	
 	@Override
 	protected CPluginSettings getSettings()
 	{
 		instance = this;
 		
 		CPluginSettings settings = new CPluginSettings();
 		
 		settings.useConfig = true;
 		settings.commandMgr = new CommandMgr();
 		
 		return settings;
 	}
 	
 	
 	@Override
 	public void init()
 	{
 		new Config();
 		
 		BorderMgr.init();
 		
 		final int second = 20;
 		BukkitScheduler scheduler = getServer().getScheduler();
 		scheduler.scheduleSyncRepeatingTask(this, new EntitiesCleaner(), second, 1*second);
 		
 		PluginManager PM = getServer().getPluginManager();
		PM.registerEvents(new BorderMgr(), this);
 		PM.registerEvents(new ProtectionListener(), this);
 	}
 	
 	
 	public static Plugin get()
 	{
 		return instance;
 	}
 }
