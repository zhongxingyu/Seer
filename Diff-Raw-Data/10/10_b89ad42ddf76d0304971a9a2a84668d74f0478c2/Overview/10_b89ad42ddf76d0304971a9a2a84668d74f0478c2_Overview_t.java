 package zauberstuhl.BukkitUpdater.Async;
 
 import java.io.IOException;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.Plugin;
 
 import zauberstuhl.BukkitUpdater.ThreadHelper;
 
 /**
 * BukkitUpdater 0.2.x
 * Copyright (C) 2011 Lukas 'zauberstuhl y33' Matt <lukas@zauberstuhl.de>
 * and many thanks to V10lator for your support.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Permissions Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Permissions Public License for more details.
 *
 * You should have received a copy of the GNU Permissions Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
 
 /**
 * This plugin was written for Craftbukkit
 * @author zauberstuhl
 */
 
 public class Overview extends Thread{
 	private final ThreadHelper th = new ThreadHelper();
 	private Player player;
 	private Plugin[] plugins;
 	private String action;
 	
 	public String supportedPlugins = "";
 	public String unsupportedPlugins = "";
 		
 	public Overview(Player player, Plugin[] plugins, String action) {
 		this.player = player;
 		this.plugins = plugins;
 		this.action = action;
 	}
 	
 	public void run() {
 		String[] supported;
 		String[] unsupported;
 
 		try {
 			// start a lookup
 			boolean u2d = u2d(player);
 			
 			if (action.equalsIgnoreCase("info")) {
 				if (u2d) th.sendTo(player, "RED", "New Updates available!! /u2d for details");
 			} else if (action.equalsIgnoreCase("unsupported")) {
 				if (unsupportedPlugins.matches(".*;.*")) {
 					unsupported = unsupportedPlugins.split(";");
 					th.sendTo(player, "WHITE", "Following plugins are not supported by BukkitUpdater:");
 					for(int i = 0; unsupported.length > i; i++)
 						th.sendTo(player, "RED", unsupported[i]);
 				} else
 					th.sendTo(player, "GREEN", "All your plugins are supported by BukkitUpdater :)");
 			} else {
 				if (u2d) {
 					supported = supportedPlugins.split(";");
 					th.sendTo(player, "GOLD", "New Updates are available for:");
 					for(int i = 0; supported.length > i; i++)
 						th.sendTo(player, "GREEN", supported[i]);
 				} else
 					th.sendTo(player, "GREEN", "Currently there are no new updates available.");
 				if (unsupportedPlugins.matches(".*;.*")) {
 					unsupported = unsupportedPlugins.split(";");
 					th.sendTo(player, "RED", "There is/are "+unsupported.length+" unspported plugin(s). For more info: /u2d unsupported");
 				}			
 			}
 		} catch (IOException e) {
 			new Debugger(player, e.getMessage(), "(Something went wrong)");
 		}
 	}
 	
 	public boolean u2d(Player player) throws IOException{
 		String allVersions = "";
 		String supported = "";
 		String unsupported = "";
 		String buffer;
 		
 		for(int i = 0; i < plugins.length; i++){
 			String version = plugins[i].getDescription().getVersion();
 			String name = plugins[i].getDescription().getName();
 			buffer = name+"::"+version;
 			allVersions += name+"::"+version+",";
 			buffer = th.sendData(buffer);			
 			
 			if(!buffer.equals("false") &&
 					!buffer.equals("unsupported") &&
 					blacklist(name)) {
 				
 				supported += buffer+";";
 			} else if (buffer.equals("unsupported"))
 				unsupported += plugins[i]+";";
 		}
 		supportedPlugins = supported;
 		unsupportedPlugins = unsupported;
 		
 		buffer = th.sendData(allVersions);
 		if(!buffer.equals("false"))
 			return true;
 		return false;
 	}
 	
 	public boolean blacklist(String plugin) throws IOException {
 		// read the blacklist and separate
 		String blacklist = th.readFile(th.blacklist);
		if (blacklist.indexOf(plugin) > 0)
 			return false;
 		return true;
 	}
 }
