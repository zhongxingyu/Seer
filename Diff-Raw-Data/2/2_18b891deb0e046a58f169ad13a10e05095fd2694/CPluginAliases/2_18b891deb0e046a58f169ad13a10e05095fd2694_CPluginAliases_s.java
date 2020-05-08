 /* cPlugin
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
 
 package castro.base.plugin;
 
 
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.scheduler.BukkitScheduler;
 
 
 public abstract class CPluginAliases extends CPluginBase
 {
 	public  static Server server;
 	private PluginManager PM;
 	private BukkitScheduler scheduler;
 	
 	
 	@Override
 	protected void initBase()
 	{
 		super.initBase();
 		server = getServer();
 		PM = server.getPluginManager();
 		scheduler = server.getScheduler();
 	}
 	
 	
 	public static Player getPlayer(String playername)
 	{
 		return server.getPlayer(playername);
 	}
 	
 	
 	public static Player getPlayerExact(String playername)
 	{
 		return server.getPlayerExact(playername);
 	}
 	
 	
 	public static boolean dispatchConsoleCommand(String command)
 	{
 		return dispatchCommand(server.getConsoleSender(), command);
 	}
 	
 	
 	public static boolean dispatchCommand(CommandSender sender, String command)
 	{
 		server.dispatchCommand(sender, command);
 		return true;
 	}
 	
 	
 	public void registerEvents(Listener eventListener)
 	{
 		PM.registerEvents(eventListener, this);
 	}
 	
 	
 	public void scheduleSyncDelayedTask(Runnable runnable)
 	{
 		scheduler.scheduleSyncDelayedTask(this, runnable);
 	}
 	
 	
 	public void scheduleSyncDelayedTask(Runnable runnable, long delay)
 	{
 		scheduler.scheduleSyncDelayedTask(this, runnable, delay);
 	}
 	
 	
	public void schedulerSyncRepeatingTask(Runnable runnable, long delay, long repeat)
 	{
 		scheduler.scheduleSyncRepeatingTask(this, runnable, delay, repeat);
 	}
 }
