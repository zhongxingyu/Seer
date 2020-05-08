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
 
import java.util.ArrayList;
import java.util.Arrays;

 import org.bukkit.craftbukkit.v1_4_6.inventory.CraftItemStack;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class AuthenticItem extends JavaPlugin {
 
 	//ClassListeners
 	private AuthenticItemCommandExecutor commandExecutor;
 	private AuthenticItemEventListener eventListener;
 	//ClassListeners
	
	public ArrayList<Integer> changeDura;
 
 	public void onDisable() {
 		// add any code you want to be executed when your plugin is disabled
 	}
 
 	public void onEnable() { 
 		
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		eventListener = new AuthenticItemEventListener(this);
 		commandExecutor = new AuthenticItemCommandExecutor(this);
 
 		getCommand("authentic").setExecutor(commandExecutor);
 
 		pm.registerEvents(eventListener, this);
		
		changeDura = (ArrayList<Integer>) Arrays.asList(256,257,258,259,261,262,267,268,269,270,271,272,273,274,275,276,277,278,279,283,284,285,286,290,291,292,293,294,298,299,300,301,302,303,304,305,306,307,308,309,310,311,312,313,314,315,316,317;
 
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
