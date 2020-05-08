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
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.ServicesManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import castro.base.GenericCommandMgr;
 
 public abstract class CPluginBase extends JavaPlugin
 {
 	public static CPlugin baseinstance;
 	
 	private GenericCommandMgr commandMgr;
 	public FileConfiguration con; // config
 	public static Economy economy;
 	public static Permission permissions;
 	
 	
 	protected abstract CPluginSettings getSettings();
 	protected abstract CPlugin         getBaseInstance();
 	
 	
 	protected void initBase()
 	{
 		baseinstance = getBaseInstance();
 		
 		ServicesManager services = getServer().getServicesManager();
 		RegisteredServiceProvider<Economy> economyProvider = services.getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null && economy == null)
 			economy = economyProvider.getProvider();
 		RegisteredServiceProvider<Permission> permissionProvider = services.getRegistration(net.milkbowl.vault.permission.Permission.class);
 		if (permissionProvider != null && permissions == null)
 			permissions = permissionProvider.getProvider();
 		
 		initBase(getSettings());
 	}
 	
 	
 	private final void initBase(CPluginSettings settings)
 	{		
 		if(settings.useConfig)
 		{
 			saveDefaultConfig();
 			con = getConfig();
 		}
 		
 		commandMgr = settings.commandMgr;
 		
 		for(Listener listener : settings.listeners)
 			getServer().getPluginManager().registerEvents(listener, this);
 	}
 	
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
 	{
 		if(commandMgr != null)
 			return commandMgr.onCommand(sender, command, args);
 		return true;
 	}
 	
 	
 	public String getPdf()
 	{
		return "&f[&9" + getDescription().getName() + "&f]&7 ";
 	}
 }
