 package net.LoadingChunks.SyncingFeeling;
 
 /*
     This file is part of SyncingFeeling
 
     Foobar is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     Foobar is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 import net.LoadingChunks.SyncingFeeling.util.SQLWrapper;
 
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SyncingFeeling extends JavaPlugin {
 
 	//ClassListeners
 	private final SyncingFeelingCommandExecutor commandExecutor = new SyncingFeelingCommandExecutor(this);
 	private final SyncingFeelingEventListener eventListener = new SyncingFeelingEventListener(this);
	private PluginManager pm;
 	//ClassListeners
 
 	public void onDisable() {
 		// add any code you want to be executed when your plugin is disabled
 	}
 
 	public void onEnable() { 
		pm = this.getServer().getPluginManager();
 		getCommand("sync").setExecutor(commandExecutor);
 
 		// you can register multiple classes to handle events if you want
 		// just call pm.registerEvents() on an instance of each class
 		pm.registerEvents(eventListener, this);
 
 		getConfig().options().copyDefaults(true);
 		getConfig().addDefault("db.user", "");
 		getConfig().addDefault("db.pass", "");
 		getConfig().addDefault("db.host", "");
 		getConfig().addDefault("db.name", "");
 		getConfig().addDefault("general.server.name", "");
 		
 		saveConfig();
 		reloadConfig();
 	}
 	
 	@Override
 	public void reloadConfig() {
 		super.reloadConfig();
 		dbConnect();
 	}
 	
 	public void dbConnect() {
 		SQLWrapper.setPlugin(this);
 		SQLWrapper.setConfig(getConfig().getString("db.user"), getConfig().getString("db.pass"), getConfig().getString("db.host"), getConfig().getString("db.name"));
 		if(!SQLWrapper.connect()) {
 			getLogger().severe("Could not connect to SQL Server! Ohhhh boy! Let's disable yeah?");
 			pm.disablePlugin(this);
 		}
 	}
 }
