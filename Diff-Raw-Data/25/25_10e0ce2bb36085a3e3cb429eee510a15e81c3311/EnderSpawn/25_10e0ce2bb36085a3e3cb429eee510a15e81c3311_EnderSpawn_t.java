 /*
  * Copyright (c) 2012 Sean Porter <glitchkey@gmail.com>
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
 
 package org.enderspawn;
 
 //* IMPORTS: JDK/JRE
 	import java.io.File;
 	import java.io.FileInputStream;
 	import java.io.FileOutputStream;
 	import java.io.InputStream;
 	import java.io.ObjectInputStream;
 	import java.io.ObjectOutputStream;
 	import java.io.OutputStream;
 	import java.lang.String;
 	import java.util.Date;
 //* IMPORTS: BUKKIT
 	import org.bukkit.command.CommandSender;
 	import org.bukkit.entity.Player;
 	import org.bukkit.plugin.java.JavaPlugin;
 	import java.util.logging.Logger;
 //* IMPORTS: SPOUT
 	//* NOT NEEDED
 //* IMPORTS: OTHER
 	//* NOT NEEDED
 
 public class EnderSpawn extends JavaPlugin
 {
 	public static String pluginName = "EnderSpawn";
 
 	public	Configuration		config;
 	public	Data			data;
 	public	Spawner			spawner;
 	private	EnderSpawnListener	listener;
 	private	EnderSpawnCommand	command;
 	public	Logger			log;
 
 	public void onLoad()
 	{
 		this.log	= this.getLogger();
 		this.data	= new Data();
 		loadData();
 
 		copyConfig();
		File configurationFile = new File(getDataFolder(), "config.yml");
 		this.config	= new Configuration(configurationFile, log, this);
 		this.listener	= new EnderSpawnListener(this);
 		this.spawner	= new Spawner(this);
 		this.command	= new EnderSpawnCommand(this);
 	}
 
 	public void onEnable()
 	{
 		config.load();
 		listener.register();
 		spawner.start();
 
 		getCommand("enderspawn").setExecutor(command);
 	}
 	
 	public void onDisable()
 	{
 		spawner.stop();
 		saveData(false);
 	}
 
 	public void reload()
 	{
 		spawner.stop();
 		saveData(false);
 		config.load();
 		spawner.start();
 
 		log.info(getDescription().getVersion() + " reloaded.");
 	}
 
 	public boolean hasPermission(CommandSender sender, String perm)
 	{
 		return hasPermission(sender, perm, true);
 	}
 
 	public boolean hasPermission(Player sender, String perm)
 	{
 		return hasPermission(sender, perm, true);
 	}
 
 	public boolean hasPermission(CommandSender sender, String perm, boolean reply)
 	{
 		if(!(sender instanceof Player))
 			return true;
 		
 		return hasPermission((Player) sender, perm, reply);
 	}
 
 	public boolean hasPermission(Player sender, String perm, boolean reply)
 	{		
 		if(sender.hasPermission(perm))
 			return true;
 
 		if(reply)
 		{
 			String message = "You don't have permission to do that!";
 			Message.severe((CommandSender) sender, message);
 		}
 
 		return false;
 	}
 
 	public boolean status(CommandSender sender, long time, String name)
 	{
 		String pronoun = "You";
 		String pronoun2 = "you";
 		boolean other = false;
 
 		if(name != null)
 		{
 			pronoun = name;
 			pronoun2 = "they";
 			other = true;
 		}
 
 		String playerName = (name == null) ? sender.getName() : name;
 
 		if(data.bannedPlayers.get(playerName) != null)
 		{
 			String message = pronoun + (other ? " is " : " are ");
 			message += "not allowed to receive experience from the Ender Dragon.";
 			Message.info(sender, message);
 			return true;
 		}
 
 		if(!(hasPermission(sender, "enderspawn.exp", false)))
 		{
 			String message = pronoun + (other ? " is " : " are ");
 			message += "not allowed to receive experience from the Ender Dragon.";
 			Message.info(sender, message);
 			return true;
 		}
 
 		long currentTime = new Date().getTime();
 		long timeRemaining = (time + (config.expResetMinutes * 60000));
 		timeRemaining -= currentTime;
 
 		if(timeRemaining <= 0)
 		{
 			String message = pronoun + " can receive experience from the Ender Dragon.";
 			Message.info(sender, message);
 			return true;
 		}
 
 		long hours	= timeRemaining / 3600000;
 		long minutes	= (timeRemaining % 3600000) / 60000;
 		long seconds	= ((timeRemaining % 3600000) % 60000) / 1000;
 
 		if(hours < 1 && minutes < 1 && seconds < 1)
 		{
 			String message = pronoun + " can receive experience from the Ender Dragon.";
 			Message.info(sender, message);
 			return true;
 		}
 		else if(hours < 1 && minutes < 1 && seconds >= 1)
 		{
 			String message = pronoun + (other ? " has " : " have ");
 			message += "%d seconds until " + pronoun2 + " can receive experience from ";
 			message += "the Ender Dragon.";
 			Message.info(sender, message, seconds);
 			return true;
 		}
 		else if(hours < 1 && minutes >= 1)
 		{
 			String message = pronoun + (other ? " has " : " have ");
 			message += "%d minutes and %d seconds until " + pronoun2 + " can receive ";
 			message += "experience from the Ender Dragon.";
 			Message.info(sender, message, minutes, seconds);
 			return true;
 		}
 		else if(hours >= 1)
 		{
 			String message = pronoun + (other ? " has " : " have ");
 			message += "%d hours, %d minutes, and %d seconds until " + pronoun2;
 			message += " can receive experience from the Ender Dragon.";
 			Message.info(sender, message, hours, minutes, seconds);
 			return true;
 		}
 
 		return true;
 	}
 
 	public boolean status(Player sender, long time, String name)
 	{		
 		return status((CommandSender) sender, time, name);
 	}
 
 	public boolean showStatus(Player player, String name)
 	{
 		String playerName = (name == null) ? player.getName() : name;
 		String caselessPlayerName = playerName.toUpperCase().toLowerCase();
 
 		long time = 0;
 
 		if(data.players.get(caselessPlayerName) != null)
 			time = data.players.get(caselessPlayerName).getTime();
 
 		return status(player, time, name);
 	}
 
 	public void loadData()
 	{
 		File dataFile;
 
 		try
 		{
 			dataFile = new File(getDataFolder(), "Data.bin");
 
 			if(!dataFile.exists())
 				return;
 
 			FileInputStream fis = new FileInputStream(dataFile);
 			ObjectInputStream in = new ObjectInputStream(fis);
 			data = (Data) in.readObject();
 			in.close();
 			log.info("Successfully loaded all data.");
 		}
 		catch(Exception e)
 		{
 			log.info("Unable to read the data file. It may be corrupt.");
 		}
 
 		saveData();
 	}
 
 	public void saveData(boolean silent)
 	{
 		try {
 			File dataFile = new File(getDataFolder(), "Data.bin");
 			FileOutputStream fos = new FileOutputStream(dataFile);
 			ObjectOutputStream out = new ObjectOutputStream(fos);
 			out.writeObject(data);
 			out.close();
 
 			if(silent)
 				return;
 
 			log.info("Successfully saved all data.");
 		} catch (Exception e) {
 			log.info("Unable to save the data file. It may be corrupt.");
 		}
 	}
 
 	public void saveData()
 	{
 		this.saveData(true);
 	}
 
 	public boolean copyConfig()
 	{
 		File sourceFile;
 		File destinationFile;
 		try
 		{
 			destinationFile = new File(getDataFolder(), "config.yml");
 
			if(!destinationFile.createNewFile())
 				return false;
 
 			InputStream inputStream = getClass().getResourceAsStream("/config.yml");
 			OutputStream out = new FileOutputStream(destinationFile);
 			byte buffer[] = new byte[1024];
 			int length;
 
 			while((length = inputStream.read(buffer)) > 0)
 				out.write(buffer, 0, length);
 
 			out.close();
 			inputStream.close();
 			return true;
 		}
 		catch(Exception e)
 		{
			log.warning("Unable to copy configuration to the plugin directory.");
 			return false;
 		}
 	}
 }
