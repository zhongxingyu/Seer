 /*
  * Copyright (c) 2012-2013 Sean Porter <glitchkey@gmail.com>
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
 
 package org.stacked;
 
 //* IMPORTS: JDK/JRE
 	import java.io.File;
 	import java.io.FileOutputStream;
 	import java.io.InputStream;
 	import java.io.OutputStream;
 //* IMPORTS: BUKKIT
 	import org.bukkit.command.Command;
 	import org.bukkit.command.CommandExecutor;
 	import org.bukkit.command.CommandSender;
 	import org.bukkit.entity.Player;
 	import org.bukkit.plugin.java.JavaPlugin;
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class Stacked extends JavaPlugin implements CommandExecutor {
 	public static Configuration config;
	//private StackedListener listener;
	//private ItemChecker itemChecker;
 
 	public boolean copyConfig(String filename) {
 		File dest;
 		InputStream is = null;
 		OutputStream out = null;
 
 		try {
 			if (!getDataFolder().exists())
 				getDataFolder().mkdirs();
 
 			dest = new File(getDataFolder(), filename);
 
 			if (!dest.createNewFile())
 				return false;
 
 			is = getClass().getResourceAsStream("/" +  filename);
 			out = new FileOutputStream(dest);
 			byte buffer[] = new byte[1024];
 			int length;
 
 			while ((length = is.read(buffer)) > 0)
 				out.write(buffer, 0, length);
 		}
 		catch(Exception e) {
 			String msg = "Unable to copy " + filename;
 			msg += " to the plugin directory.";
 			getLogger().warning(msg);
 			return false;
 		}
 		finally {
 			try {
 				is.close();
 			}
 			catch(Exception e) {}
 
 			try {
 				out.close();
 			}
 			catch(Exception e) {}
 		}
 
 		return true;
 	}
 
 	public void loadConfig() {
 		copyConfig("config.yml");
 		File configFile	= new File(getDataFolder(), "config.yml");
 		this.config	= new Configuration(configFile, this);
 	}
 
 	@Override
 	public boolean onCommand(
 		CommandSender sender,
 		Command command,
 		String label,
 		String[] args) {
 		if (sender == null || command == null)
 			return false;
 		else if (label == null || args == null)
 			return false;
 
 		if (!(sender instanceof Player)) {
 			String msg = "You have to be a player to do that!";
 			Message.severe(this, sender, msg);
 			return true;
 		}
 
 		Player player = (Player) sender;
 		StackInventory stacker = new StackInventory(this, player, true);
 		return stacker.stack();
 	}
 
 	public void onDisable() {
		//itemChecker.stop();
 		getLogger().info("Successfully disabled.");
 	}
 
 	public void onEnable() {
 		loadConfig();
 		getCommand("stack").setExecutor(this);
		//listener = new StackedListener(this);
		//itemChecker = new ItemChecker(this);
 		getLogger().info("Successfully enabled.");
 	}
 
 	public void reload() {
 		if (this.config != null)
 			this.config.load();
 		else
 			loadConfig();
 
		//itemChecker.stop();
		//itemChecker.start();
 
 		getLogger().info("Successfully reloaded!");
 	}
 }
