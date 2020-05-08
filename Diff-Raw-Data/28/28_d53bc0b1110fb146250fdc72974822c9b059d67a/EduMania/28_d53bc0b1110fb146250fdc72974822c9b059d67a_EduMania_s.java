 /*
     EduMania: Bukkit plugin for educational users.
     Copyright (C) 2012  korikisulda
 
     This program is free software: you can redistribute it and/or modify
     it under the terms of the GNU General Public License as published by
     the Free Software Foundation, either version 3 of the License, or
     (at your option) any later version.
 
     This program is distributed in the hope that it will be useful,
     but WITHOUT ANY WARRANTY; without even the implied warranty of
     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
     GNU General Public License for more details.
 
     You should have received a copy of the GNU General Public License
     along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package info.plugmania.EduMania;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class EduMania extends JavaPlugin {
 	public Logger log = Logger.getLogger("Minecraft");
 	public Map<String, String> authKeys = new HashMap<String, String>();
 	public List<Player> authedPlayers = new ArrayList<Player>();
 	public util util = new util(this);
 
 	public void onEnable() {
 		if (!getConfig().isSet("TexturePack.enable"))
 			getConfig().set("TexturePack.enable", false);
 		
 		if (!getConfig().isSet("TexturePack.URL"))
 			getConfig().set("TexturePack.URL", "");
 		
 		if (!getConfig().isSet("TexturePack.fileName"))
 			getConfig().set("TexturePack.fileName", "texture.zip");
 		
 		if (!getConfig().isSet("Authentication.enable"))
 			getConfig().set("Authentication.enable", true);
 		
 		
 		///////////////////////////////////////////////////////////////////////////////////
 		if(!getConfig().isSet("keys")) getConfig().createSection("keys", authKeys);
 		for (String s:getConfig().getConfigurationSection("keys").getKeys(false)){
 			authKeys.put(s, getConfig().getConfigurationSection("keys").getString(s));
 		}
 		//////////////////////////////////////////////////////////////////////////////////
 		PluginManager pm = getServer().getPluginManager();
 		pm.registerEvents(new PlayerListener(this), this);
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command command,
 			String label, String[] args) {
 		if (command.getName().equalsIgnoreCase("auth")) {
 		}
 		else if (command.getName().equalsIgnoreCase("txdl")) {
 			sender.sendMessage("[EduMania][TXDL]" + args[0]);
 		}else if (command.getName().equalsIgnoreCase("txrl")) {
 			sender.sendMessage("[EduMania][TXRL].");
 		}
 		return true;
 
 	}
 
 	public void onDisable() {
 		getConfig().createSection("keys", authKeys);
 		saveConfig();
 	}
 }
