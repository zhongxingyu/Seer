 package zauberstuhl.BukkitUpdater;
 
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.URL;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.craftbukkit.CraftServer;
 import org.bukkit.craftbukkit.command.ColouredConsoleSender;
 import org.bukkit.entity.Player;
 
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
 
 public class ThreadHelper {
	public ColouredConsoleSender console = new ColouredConsoleSender(
			(CraftServer) Bukkit.getServer());
 	// current working directory
 	public String cwd = System.getProperty("user.dir");
 	// token for adding static links
 	public File token = new File(cwd+"/plugins/BukkitUpdater/token.txt");
 	// you can hide plugins with that list
 	public File blacklist = new File(cwd+"/plugins/BukkitUpdater/blacklist.txt");
 	// main folder
 	public File folder = new File(cwd +"/plugins/BukkitUpdater/");
 	// backup folder
 	public File backupFolder = new File(cwd +"/plugins/BukkitUpdater/backup/");
 	
 	public String sendData(String send) throws IOException {
 		String token = "";
 		String received = "";
 		String inputLine;
 		
 		if (this.token.exists()) {
 			token = readFile(this.token);
 		}
 		// ADD SOME SERVER FILE for dudes who want to add there own thinks blabal
 		URL adress = new URL( "http://bukkit.zauberstuhl.de/lookup.pl?s="+send+"&t="+token );
 		BufferedReader in = new BufferedReader(
 				new InputStreamReader(
 						adress.openStream()));
 		while ((inputLine = in.readLine()) != null)
 			received = received + inputLine;
 		in.close();
 		
 		return received;
 	}
 	
 	public void helper(Player player) {
 		sendTo(player, "RED", "Bukkit Updater Commands:");
 		sendTo(player, "WHITE", "");
 		sendTo(player, "GOLD", "/u2d - Shows outdated plugins");
 		sendTo(player, "GOLD", "/u2d update <PluginName> - Update the plugin if there is following tag behind the name '(L)'");
 		sendTo(player, "GOLD", "/u2d reload <PluginName> - Reload the plugin e.g. after a update");
 		sendTo(player, "GOLD", "/u2d ignore <PluginName> - Add/Remove a plugin from the blacklist");
 		sendTo(player, "GOLD", "/u2d ignore list - List all ignored plugins");
 		sendTo(player, "GOLD", "/u2d unsupported - Shows unsupported plugins");
 		sendTo(player, "GOLD", "/u2d help - Display this help-text");
 	}
 
 	public String readFile(File file) throws IOException {
 	    byte[] buffer = new byte[(int) file.length()];
 	    BufferedInputStream f = null;
 	    try {
 	        f = new BufferedInputStream(new FileInputStream(file));
 	        f.read(buffer);
 	    } finally {
 	        if (f != null) try { f.close(); } catch (IOException ignored) { }
 	    }
 	    return new String(buffer);
 	}
 
 	public void writeToFile(File file, String input) throws IOException {
 		BufferedWriter out = new BufferedWriter(new FileWriter(file));
 		out.write(input);
 		out.close();
 	}
 	
 	public void sendTo(Player player, String color, String string) {
 		if (player == null) {
 			if (!string.equalsIgnoreCase(""))
 				console.sendMessage(ChatColor.valueOf(color)+string);
 		} else {
 			player.sendMessage(ChatColor.valueOf(color)+string);
 		}
 	}
 }
