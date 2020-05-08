 /*
  *     Copyright (C) 2013  Nodin Chan <nodinchan@live.com>
  *     
  *     This program is free software: you can redistribute it and/or modify
  *     it under the terms of the GNU General Public License as published by
  *     the Free Software Foundation, either version 3 of the License, or
  *     (at your option) any later version.
  *     
  *     This program is distributed in the hope that it will be useful,
  *     but WITHOUT ANY WARRANTY; without even the implied warranty of
  *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *     GNU General Public License for more details.
  *     
  *     You should have received a copy of the GNU General Public License
  *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package com.titankingdoms.dev.titanchat;
 
 import java.io.File;
 import java.util.Arrays;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.command.CommandSender;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.titankingdoms.dev.titanchat.addon.AddonManager;
 import com.titankingdoms.dev.titanchat.command.Command;
 import com.titankingdoms.dev.titanchat.command.CommandManager;
 import com.titankingdoms.dev.titanchat.core.channel.Channel;
 import com.titankingdoms.dev.titanchat.core.channel.ChannelManager;
 import com.titankingdoms.dev.titanchat.core.channel.info.Status;
 import com.titankingdoms.dev.titanchat.core.participant.ParticipantManager;
 import com.titankingdoms.dev.titanchat.format.tag.TagManager;
 import com.titankingdoms.dev.titanchat.metrics.Metrics;
 import com.titankingdoms.dev.titanchat.topic.TopicManager;
 import com.titankingdoms.dev.titanchat.util.Debugger;
 import com.titankingdoms.dev.titanchat.util.Messaging;
 import com.titankingdoms.dev.titanchat.util.Permissions;
 import com.titankingdoms.dev.titanchat.util.vault.Vault;
 
 /**
  * {@link TitanChat} - Main Class of TitanChat
  * 
  * @author NodinChan
  * 
  */
 public final class TitanChat extends JavaPlugin {
 	
 	private static TitanChat instance;
 	
 	private final Logger log = Logger.getLogger("TitanLog");
 	
 	private final Debugger db = new Debugger(0, "TitanChat");
 	
 	private AddonManager addon;
 	private ChannelManager channel;
 	private CommandManager command;
 	private TopicManager topic;
 	private ParticipantManager participant;
 	private TagManager tag;
 	
 	/**
 	 * Gets the {@link AddonManager}
 	 * 
 	 * @return The {@link AddonManager}
 	 */
 	public AddonManager getAddonManager() {
 		return addon;
 	}
 	
 	/**
 	 * Gets the {@link ChannelManager}
 	 * 
 	 * @return The {@link ChannelManager}
 	 */
 	public ChannelManager getChannelManager() {
 		return channel;
 	}
 	
 	/**
 	 * Gets the {@link CommandManager}
 	 * 
 	 * @return The {@link CommandManager}
 	 */
 	public CommandManager getCommandManager() {
 		return command;
 	}
 	
 	/**
 	 * Gets the instance of {@link TitanChat}
 	 * 
 	 * @return The instance of {@link TitanChat}
 	 */
 	public static TitanChat getInstance() {
 		return instance;
 	}
 	
 	/**
 	 * Gets the {@link ParticipantManager}
 	 * 
 	 * @return The {@link ParticipantManager}
 	 */
 	public ParticipantManager getParticipantManager() {
 		return participant;
 	}
 	
 	/**
 	 * Gets the {@link TagManager}
 	 * 
 	 * @return The {@link TagManager}
 	 */
 	public TagManager getTagManager() {
 		return tag;
 	}
 	
 	/**
 	 * Gets the {@link TopicManager}
 	 * 
 	 * @return The {@link TopicManager}
 	 */
 	public TopicManager getTopicManager() {
 		return topic;
 	}
 	
 	/**
 	 * Initialises {@link Metrics} for {@link TitanChat}
 	 * 
 	 * @return True if no errors occured
 	 */
 	private boolean initMetrics() {
 		if (!getConfig().getBoolean("metrics-statistics", true)) {
 			log(Level.INFO, "Metrics Disabled");
 			return true;
 		}
 		
 		try {
 			Metrics metrics = new Metrics(this);
 			
 			if (metrics.isOptOut())
 				return true;
 			
 			return metrics.start();
 			
 		} catch (Exception e) { return false; }
 	}
 	
 	/**
 	 * Sends the message with a prefix to the console
 	 * 
 	 * @param level The {@link Level} of importance
 	 * 
 	 * @param message The message to send
 	 */
 	public void log(Level level, String message) {
 		if (message != null && !message.isEmpty())
 			log.log((level != null) ? level : Level.INFO, "[TitanChat v4.1] " + message);
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String lbl, String[] args) {
 		if (sender == null || cmd == null || lbl == null || args == null)
 			return false;
 		
 		if (cmd.getName().equals("titanchat")) {
 			db.debug(Level.INFO, "Command by " + sender.getName() + ":");
 			db.debug(Level.INFO, lbl + " " + StringUtils.join(args, " "));
 			
 			if (args.length < 1) {
 				Messaging.sendMessage(sender, "&5You are running &6" + this);
 				Messaging.sendMessage(sender, "&6\"/titanchat help [topic/page]\" for help");
 				return true;
 			}
 			
 			String cmdName = "";
 			String chName = "";
 			
 			if (args[0].startsWith("@")) {
 				if (args.length < 2) {
 					cmdName = "join";
 					chName = args[0].substring(1);
 					args = new String[] { args[0].substring(1) };
 					
 				} else {
 					cmdName = args[1];
 					chName = args[0].substring(1);
 					args = Arrays.copyOfRange(args, 2, args.length);
 				}
 				
 			} else {
 				cmdName = args[0];
 				args = Arrays.copyOfRange(args, 1, args.length);
 			}
 			
			args = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").split(StringUtils.join(args, " "));
 			
 			db.debug(Level.INFO, "Command: " + cmdName);
 			db.debug(Level.INFO, "Channel:" + ((chName.isEmpty()) ? "" : " " + chName));
 			db.debug(Level.INFO, "Arguments:" + ((args != null) ? " " + StringUtils.join(args, ", ") : ""));
 			
 			Channel ch = null;
 			
 			if (!chName.isEmpty()) {
 				if (!channel.hasChannel(chName)) {
 					Messaging.sendMessage(sender, "&4Channel does not exist");
 					return true;
 				}
 				
 				ch = channel.getChannel(chName);
 				
 			} else { ch = participant.getParticipant(sender).getCurrentChannel(); }
 			
 			onCommand(sender, ch, cmdName, args);
 			return true;
 			
 		} else if (cmd.getName().equalsIgnoreCase("chat")) {
 			onCommand(sender, participant.getParticipant(sender).getCurrentChannel(), "chat", args);
 			return true;
 			
 		} else if (cmd.getName().equalsIgnoreCase("pm")) {
 			onCommand(sender, (Channel) null, "pm", args);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 * Processes the command
 	 * 
 	 * @param sender The {@link CommandSender}
 	 * 
 	 * @param ch The targetted {@link Channel}
 	 * 
 	 * @param label The label of the {@link Command} used
 	 * 
 	 * @param args The arguments of the command
 	 */
 	private void onCommand(CommandSender sender, Channel ch, String label, String[] args) {
 		if (command.hasLabel(label)) {
 			Command cmd = command.getCommand(label);
 			
 			if (args.length < cmd.getMinArguments() || args.length > cmd.getMaxArguments()) {
 				Messaging.sendMessage(sender, "&4Invalid argument length");
 				
 				String pre = "/titanchat [@<channel>] " + cmd.getName();
 				String suf = (!cmd.getUsage().isEmpty()) ? " " + cmd.getUsage() : "";
 				Messaging.sendMessage(sender, "&6" + pre + suf);
 				return;
 			}
 			
 			if (!cmd.permissionCheck(sender, ch)) {
 				Messaging.sendMessage(sender, "&4You do not have permission");
 				return;
 			}
 			
 			cmd.execute(sender, ch, args);
 			return;
 		}
 		
 		Messaging.sendMessage(sender, "&4Invalid command", "&6\"/titanchat help [topic/page]\" for help");
 	}
 	
 	@Override
 	public void onDisable() {
 		log(Level.INFO, "TitanChat is now disabling...");
 		
 		log(Level.INFO, "Unloading managers...");
 		
 		addon.unload();
 		channel.unload();
 		command.unload();
 		topic.unload();
 		participant.unload();
 		tag.unload();
 		
 		log(Level.INFO, "TitanChat is now disabled");
 	}
 	
 	@Override
 	public void onEnable() {
 		log(Level.INFO, "TitanChat is now enabling...");
 		
 		if (instance == null)
 			instance = this;
 		
 		log(Level.INFO, "Attempting to hook into Vault...");
 		
 		if (!Vault.initialise(getServer()))
 			log(Level.WARNING, "Failed to hook into Vault");
 		
 		log(Level.INFO, "Registering permissions...");
 		
 		Permissions.load();
 		
 		if (getConfig().get("logging.debug", null) != null) {
 			for (int id : getConfig().getIntegerList("logging.debug"))
 				Debugger.startDebug(id);
 		}
 		
 		getServer().getPluginManager().registerEvents(new TitanChatListener(), this);
 		
 		log(Level.INFO, "Registered events");
 		
 		log(Level.INFO, "Attempting to hook into Metrics...");
 		
 		if (!initMetrics())
 			log(Level.WARNING, "Failed to hook into Metrics");
 		
 		log(Level.INFO, "Loading managers...");
 		
 		addon.load();
 		channel.load();
 		command.load();
 		topic.load();
 		participant.load();
 		tag.load();
 		
 		if (channel.getChannels(Status.DEFAULT).isEmpty())
 			log(Level.WARNING, "No default channel has been defined");
 		
 		log(Level.INFO, "TitanChat is now enabled");
 	}
 	
 	@Override
 	public void onLoad() {
 		log(Level.INFO, "TitanChat is now loading...");
 		
 		instance = this;
 		
 		log(Level.INFO, "Initialising managers...");
 		
 		this.addon = new AddonManager();
 		this.channel = new ChannelManager();
 		this.command = new CommandManager();
 		this.topic = new TopicManager();
 		this.participant = new ParticipantManager();
 		this.tag = new TagManager();
 		
 		if (!new File(getDataFolder(), "config.yml").exists())
 			log(Level.INFO, "Generating default config.yml...");
 		
 		getConfig().options().copyDefaults(true);
 		saveConfig();
 		
 		if (channel.getChannelDirectory().listFiles().length == 0) {
 			log(Level.INFO, "Generating default channel configs...");
 			saveResource("channels/Default.yml", false);
 			saveResource("channels/Global.yml", false);
 			saveResource("channels/Local.yml", false);
 			saveResource("channels/Staff.yml", false);
 			saveResource("channels/World.yml", false);
 			saveResource("channels/README.txt", false);
 		}
 		
 		if (!new File(getDataFolder(), "participants.yml").exists()) {
 			log(Level.INFO, "Generating default participants.yml...");
 			saveResource("participants.yml", false);
 		}
 		
 		participant.getConfig().options().copyDefaults(true);
 		participant.saveConfig();
 		
 		if (!new File(getDataFolder(), "topic.yml").exists()) {
 			log(Level.INFO, "Generating default topic.yml...");
 			saveResource("topic.yml", false);
 		}
 		
 		topic.getConfig().options().copyDefaults(true);
 		topic.saveConfig();
 		
 		log(Level.INFO, "TitanChat is now loaded");
 	}
 	
 	/**
 	 * Called when {@link TitanChat} is to be reloaded
 	 */
 	public void onReload() {
 		log(Level.INFO, "TitanChat is now reloading...");
 		
 		log(Level.INFO, "Reloading managers...");
 		
 		addon.reload();
 		channel.reload();
 		command.reload();
 		topic.reload();
 		participant.reload();
 		
 		log(Level.INFO, "TitanChat is now reloaded");
 	}
 }
