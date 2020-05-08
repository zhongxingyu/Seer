 package net.loadingchunks.plugins.AuthenticItem.AuthenticItem;
 
 /*
     This file is part of AuthenticItem
 
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
 
 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AuthenticItem extends JavaPlugin {
 
 	//ClassListeners
 	private AuthenticItemCommandExecutor commandExecutor;
 	private AuthenticItemEventListener eventListener;
 	//ClassListeners
 
 	public void onDisable() {
 		// add any code you want to be executed when your plugin is disabled
 	}
 
 	public void onEnable() { 
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		eventListener = new AuthenticItemEventListener(this);
 		commandExecutor = new AuthenticItemCommandExecutor(this);
 
 		getCommand("authentic").setExecutor(commandExecutor);
 
 		pm.registerEvents(eventListener, this);
 
 	}
 	
 	public String formatText(String text, CraftItemStack cstack)
 	{
 		AItem item = new AItem(cstack.clone());
 		item.setPlugin(this);
 		
 		String displayName = item.getDisplayName();
 		String name = "";
 				
 		if(displayName == null || displayName.isEmpty())
 			name = cstack.getType().name();
 		else
 			name = displayName;
 		
 		return text.replace("{hand}", name);
 	}
 }
