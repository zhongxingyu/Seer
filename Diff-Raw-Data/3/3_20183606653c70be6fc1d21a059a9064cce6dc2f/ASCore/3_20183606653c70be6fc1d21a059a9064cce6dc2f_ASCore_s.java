 /*
  * Copyright (C) 2011 MineStar.de 
  * 
  * This file is part of 'AdminStuff'.
  * 
  * 'AdminStuff' is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, version 3 of the License.
  * 
  * 'AdminStuff' is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License
  * along with 'AdminStuff'.  If not, see <http://www.gnu.org/licenses/>.
  * 
  * AUTHOR: GeMoschen
  * 
  */
 
 package com.bukkit.Souli.AdminStuff;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 import com.bukkit.Souli.AdminStuff.Listener.ASBlockListener;
 import com.bukkit.Souli.AdminStuff.Listener.ASEntityListener;
 import com.bukkit.Souli.AdminStuff.Listener.ASPlayerListener;
 import com.bukkit.Souli.AdminStuff.commands.CommandList;
 
 public class ASCore extends JavaPlugin {
     /** GLOBAL VARS */
     public static LogUnit log = null;
     private String pluginName = "";
     private String version = "";
     private static Server server = null;
 
     /** LISTENERS */
     private ASBlockListener bListener;
     private ASEntityListener eListener;
     private ASPlayerListener pListener;
     
     public static HashMap<String, ASKit> kitList = new HashMap<String, ASKit>();
 
     /**
      * ON DISABLE
      */
     @Override
     public void onDisable() {
 	log.printInfo("v" + this.version + " disabled!");
     }
 
     /**
      * ON ENABLE
      */
     @Override
     public void onEnable() {
 	ASCore.setMCServer(getServer());
 	this.pluginName = this.getDescription().getName();
 	this.version = this.getDescription().getVersion();
 	log = LogUnit.getInstance(pluginName);
 
 	boolean error = false;
 	try {
 	    ASSpawn.loadAllSpawns();
 
 	    bListener = new ASBlockListener();
 	    eListener = new ASEntityListener();
 	    pListener = new ASPlayerListener();
 	    new CommandList(getServer());
 
 	    for (Player player : getServer().getOnlinePlayers()) {
 		ASPlayerListener.playerMap
 			.put(player.getName(), new ASPlayer());
 		ASPlayerListener.playerMap.get(player.getName()).loadConfig(
 			player.getName());
 	    }
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.BLOCK_PLACE, bListener, Event.Priority.Monitor,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.ENTITY_DAMAGE, eListener, Event.Priority.Normal,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_CHAT, pListener, Event.Priority.Normal,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_INTERACT, pListener,
 		    Event.Priority.Normal, this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_JOIN, pListener, Event.Priority.Monitor,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_KICK, pListener, Event.Priority.Monitor,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_MOVE, pListener, Event.Priority.Normal,
 		    this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_RESPAWN, pListener,
 		    Event.Priority.Normal, this);
 	    getServer().getPluginManager().registerEvent(
 		    Event.Type.PLAYER_QUIT, pListener, Event.Priority.Monitor,
 		    this);
 
 	    loadConfig();
 	    error = false;
 	} catch (Exception e) {
 	    error = true;
 	    log.printError("ERROR while enabling " + pluginName + "!", e);
 	}
 
 	if (!error)
 	    log.printInfo("v" + this.version + " enabled!");
     }
 
     /**
      * ON COMMAND
      */
     @Override
     public boolean onCommand(CommandSender sender, Command command,
 	    String label, String[] args) {
 	CommandList.handleCommand(sender, label, args);
 	return true;
     }
 
     /**
      * LOAD CONFIG
      */
     @SuppressWarnings("unchecked")
     public void loadConfig() {
 	new File("plugins/AdminStuff/").mkdirs();
 	Configuration config = new Configuration(new File(
 		"plugins/AdminStuff/config.yml"));
 	config.load();
 
 	Map<String, ArrayList<String>> nodeList = (Map<String, ArrayList<String>>) config
 		.getProperty("kits");
 
 	for (Map.Entry<String, ArrayList<String>> entry : nodeList.entrySet()) {
   	    ASKit thisKit = new ASKit();	        
 	    for (String part : entry.getValue()) {
 		try {		    
 		    String[] split = part.split(" ");
 		    
 		    int TypeID 	= 0;
 		    byte Data   = 0;
 		    int Amount	= 1;
 		    
 		    String[] itemSplit = split[0].split(":");		    
 		    if(split.length > 1)
 		    {
 			Amount = Integer.valueOf(split[1]);
 		    }
 		    
 		    TypeID = Integer.valueOf(itemSplit[0]);
 		    if(itemSplit.length > 1)
 		    {
 			Data = Byte.valueOf(itemSplit[1]);
 		    }
 		    
 		    if(ASItem.isValid(TypeID, Data))			
 		    {
 			ItemStack item = new ItemStack(TypeID);
 			item.setAmount(Amount);
 			item.setDurability(Data);
 			thisKit.addItem(item);
 		    }
 		} catch (Exception e) {
 		}
 	    }
 	    kitList.put(entry.getKey().toLowerCase(), thisKit);
 	    }
     }
 
     /**
      * 
      * GET PLAYER
      * 
      * @param name
      *            the playername to find
      * @return the player
      */
     public static Player getPlayer(String name) {
 	if (name == null)
 	    return null;
 	List<Player> matchedPlayers = server.matchPlayer(name);
 	if (matchedPlayers != null)
 	    if (matchedPlayers.size() > 0)
 		return matchedPlayers.get(0);
 	return null;
     }
 
     /**
      * 
      * GET PLAYER
      * 
      * @param name
      *            the playername to find
      * @return the player
      */
     public static Player getDirectPlayer(String name) {
 	if (name == null)
 	    return null;
 	return server.getPlayer(name);
     }
 
     /**
      * 
      * GET THE PLAYERS NAME
      * 
      * @param player
      *            the player
      * @return the players name
      */
     public static String getPlayerName(Player player) {
 	if (player == null)
 	    return "PLAYER NOT FOUND";
 	String nick = player.getName();
 	if (player.getDisplayName() != null)
 	    nick = player.getDisplayName();
 
 	nick = nick.replace("[AFK] ", "").replace(" was fished!", "");
 	return nick;
     }
 
     /**
      * 
      * SET MC SERVER
      * 
      * @param server
      *            the serverinstance
      */
     public static void setMCServer(Server server) {
 	ASCore.server = server;
     }
 
     /**
      * 
      * GET MC SERVER
      * 
      * @return the serverinstance
      */
     public static Server getMCServer() {
 	return ASCore.server;
     }
 }
