 package com.untamedears.recivchat;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import com.untamedears.citadel.Citadel;
 import com.untamedears.citadel.GroupManager;
 import com.untamedears.citadel.entity.Faction;
 import com.untamedears.recivchat.mode.ChatMode;
 import com.untamedears.recivchat.mode.GroupChat;
 import com.untamedears.recivchat.mode.MessageChat;
 import com.untamedears.recivchat.mode.NormalChat;
 import com.untamedears.recivchat.mode.Replyable;
 
 public class CivChat extends JavaPlugin implements Listener {
 	public static CivChat instance;
 	
 	private static int chatRange = 1000;
 	private static int shoutRange = 1500;
 	private static float shoutExhaustion = 5.0F;
 	private static int whisperRange = 50;
 	
 	private PlayerManager playerManager = new PlayerManager();
 	
 	private PrintWriter logWriter = null;
 	
 	public void onEnable() {
 		instance = this;
 		
 		if(!this.getDataFolder().isDirectory())
 			this.getDataFolder().mkdir();
 		
 		File logFolder = new File(this.getDataFolder(), "logs");
 		
 		try {
 			if(!logFolder.isDirectory())
 				logFolder.mkdir();
 			
 			String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
 			
 			File logFile = new File(logFolder, date + ".txt");
 			
 			if(!logFile.isFile())
 				logFile.createNewFile();
 			
 			logWriter = new PrintWriter(new FileWriter(logFile, true));
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		
 		Bukkit.getPluginManager().registerEvents(this, this);
 		
 		this.getLogger().info("Loading ignore lists...");
 		int ignores = loadIgnoreLists();
 		this.getLogger().info("Loaded " + ignores + " entries.");
 		
 		chatRange = this.getConfig().getInt("chatRange", 1000);
 		shoutRange = this.getConfig().getInt("shoutRange", 1500);
 		shoutExhaustion = (float) this.getConfig().getDouble("shoutExhaustion", 5.0F);
 		whisperRange = this.getConfig().getInt("whisperRange", 50);
 		
 		this.getConfig().set("chatRange", chatRange);
 		this.getConfig().set("shoutRange", shoutRange);
 		this.getConfig().set("shoutExhaustion", (double) shoutExhaustion);
 		this.getConfig().set("whisperRange", whisperRange);
 		
 		this.saveConfig();
 	}
 	
 	public void onDisable() {
 		this.getLogger().info("Saving ignore lists...");
 		int ignores = saveIgnoreLists();
 		this.getLogger().info("Saved " + ignores + " entries.");
 	}
 	
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		if(command.getName().equals("msg")) {
 			if(args.length == 0) {
 				ChatMode mode = playerManager.getChatMode(sender.getName());
 				
 				if(!(mode instanceof NormalChat)) {
 					playerManager.removePlayerChatMode(sender.getName());
 					
 					sender.sendMessage(ChatColor.YELLOW + "You have been moved to normal chat.");
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 				}
 			}
 			else if(args.length == 1) {
 				Player player = Bukkit.getPlayer(args[0]);
 				
 				if(player != null) {
					playerManager.setChatMode(sender.getName(), new MessageChat(player.getName()));
 					
 					sender.sendMessage(ChatColor.YELLOW + "You are now in a conversation with " + player.getName() + ".");
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "The specified player is not online.");
 				}
 			}
 			else if(args.length > 1) {
 				Player player = Bukkit.getPlayer(args[0]);
 				
 				if(player != null) {
					MessageChat chat = new MessageChat(player.getName());
 					
 					String message = "";
 					
 					for(int i = 1; i < args.length; i++) {
 						message += args[i];
 						
 						if(i < args.length - 1) {
 							message += " ";
 						}
 					}
 					
 					chat.directMessage(sender, message);
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "The specified player is not online.");
 				}
 			}
 		}
 		else if(command.getName().equals("groupchat")) {
 			if(args.length == 0) {
 				ChatMode mode = playerManager.getChatMode(sender.getName());
 				
 				if(!(mode instanceof NormalChat)) {
 					playerManager.removePlayerChatMode(sender.getName());
 					
 					sender.sendMessage(ChatColor.YELLOW + "You have been moved to normal chat.");
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 				}
 			}
 			else if(args.length == 1) {
 				GroupManager groupManager = Citadel.getGroupManager();
 				
 				if(groupManager.isGroup(args[0])) {
 					Faction group = groupManager.getGroup(args[0]);
 					
 					playerManager.setChatMode(sender.getName(), new GroupChat(group.getName()));
 					
 					sender.sendMessage(ChatColor.YELLOW + "You are now in a conversation with " + group.getName() + ".");
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "The specified group does not exist.");
 				}
 			}
 			else if(args.length > 1) {
 				GroupManager groupManager = Citadel.getGroupManager();
 				
 				if(groupManager.isGroup(args[0])) {
 					Faction group = groupManager.getGroup(args[0]);
 					
					MessageChat chat = new MessageChat(group.getName());
 					
 					String message = "";
 					
 					for(int i = 1; i < args.length; i++) {
 						message += args[i];
 						
 						if(i < args.length - 1) {
 							message += " ";
 						}
 					}
 					
 					chat.directMessage(sender, message);
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "The specified group does not exist.");
 				}
 			}
 		}
 		else if(command.getName().equals("reply")) {
 			Replyable replyable = playerManager.getLastReplyable(sender.getName());
 			
 			if(replyable != null) {
 				String message = "";
 				
 				for(int i = 0; i < args.length; i++) {
 					message += args[i];
 					
 					if(i < args.length - 1) {
 						message += " ";
 					}
 				}
 				
 				replyable.getReplyChat().directMessage(sender, message);
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "You do not have anything to reply to!");
 			}
 		}
 		else if(command.getName().equals("ignore")) {
 			if(args.length == 0) {
 				ArrayList<String> ignoreList = playerManager.getIgnoreList(sender.getName());
 				
 				if(ignoreList.size() == 0) {
 					sender.sendMessage(ChatColor.YELLOW + "You are currently ignoring no one.");
 				}
 				else {
 					String all = "";
 					
 					for(int i = 0; i < ignoreList.size(); i++) {
 						all += ignoreList.get(i);
 						
 						if(i < ignoreList.size() - 1) {
 							all += " ";
 						}
 					}
 					
 					sender.sendMessage(ChatColor.YELLOW + "You are currently ignoring the following player(s): " + all);
 				}
 			}
 			else {
 				OfflinePlayer player = Bukkit.getOfflinePlayer(args[0]);
 				
 				if(playerManager.isIgnoring(sender.getName(), player.getName())) {
 					sender.sendMessage(ChatColor.YELLOW + "You are no longer ignoring " + player.getName() + ".");
 					
 					playerManager.removeIgnored(sender.getName(), player.getName());
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "You are now ignoring " + player.getName() + ".");
 					
 					playerManager.addIgnore(sender.getName(), player.getName());
 				}
 			}
 		}
 		else if(command.getName().equals("shout")) {
 			if(!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.YELLOW + "This command cannot be run from the console.");
 				
 				return true;
 			}
 			
 			Player player = (Player) sender;
 			
 			if(args.length < 1) {
 				sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 				
 				return true;
 			}
 			
 			if(player.getFoodLevel() <= 6) {
 				player.sendMessage(ChatColor.YELLOW + "You are too hungry to shout!");
 				
 				return true;
 			}
 			
 			player.setExhaustion(player.getExhaustion() + shoutExhaustion);
 			
 			String message = "";
 			
 			for(int i = 0; i < args.length; i++) {
 				message += args[i];
 				
 				if(i < args.length - 1) {
 					message += " ";
 				}
 			}
 			
 			Location playerLoc = player.getLocation();
 			
 			ArrayList<Player> recipients = new ArrayList<Player>();
 			
 			for(Player recipient : player.getWorld().getPlayers()) {
 				if(CivChat.instance.getPlayerManager().isIgnoring(recipient.getName(), player.getName())) {
 					continue;
 				}
 				
 				double dist = playerLoc.distance(recipient.getLocation());
 				
 				if(dist <= whisperRange) {
 					recipients.add(recipient);
 				}
 			}
 			
 			for(Player recipient : recipients) {
 				recipient.sendMessage(player.getName() + ": " + message);
 			}
 		}
 		else if(command.getName().equals("whisper")) {
 			if(!(sender instanceof Player)) {
 				sender.sendMessage(ChatColor.YELLOW + "This command cannot be run from the console.");
 				
 				return true;
 			}
 			
 			Player player = (Player) sender;
 			
 			if(args.length < 1) {
 				sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 				
 				return true;
 			}
 			
 			String message = "";
 			
 			for(int i = 0; i < args.length; i++) {
 				message += args[i];
 				
 				if(i < args.length - 1) {
 					message += " ";
 				}
 			}
 			
 			Location playerLoc = player.getLocation();
 			
 			ArrayList<Player> recipients = new ArrayList<Player>();
 			
 			for(Player recipient : player.getWorld().getPlayers()) {
 				if(CivChat.instance.getPlayerManager().isIgnoring(recipient.getName(), player.getName())) {
 					continue;
 				}
 				
 				double dist = playerLoc.distance(recipient.getLocation());
 				
 				if(dist <= whisperRange) {
 					recipients.add(recipient);
 				}
 			}
 			
 			for(Player recipient : recipients) {
 				recipient.sendMessage(player.getName() + ": " + message);
 			}
 		}
 		else if(command.getName().equals("civchat")) {
 			if(args.length == 1) {
 				if(args[0].equals("load")) {
 					this.reloadConfig();
 				}
 				else {
 					sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 				}
 			}
 			else {
 				sender.sendMessage(ChatColor.YELLOW + "Usage: " + command.getUsage());
 			}
 		}
 		
 		return true;
 	}
 	
 	@EventHandler
 	public void onPlayerChat(AsyncPlayerChatEvent event) {
 		ChatMode chatMode = playerManager.getChatMode(event.getPlayer().getName());
 		
 		chatMode.onPlayerChat(event);
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent event) {
 		playerManager.removePlayerChatMode(event.getPlayer().getName());
 		playerManager.removeLastReplyable(event.getPlayer().getName());
 	}
 	
 	public void logChat(String log) {
 		logWriter.print(log);
 		logWriter.flush();
 	}
 	
 	public void logChatLine(String log) {
 		logWriter.println(log);
 		logWriter.flush();
 	}
 	
 	public PlayerManager getPlayerManager() {
 		return playerManager;
 	}
 	
 	public static int getChatRange() {
 		return chatRange;
 	}
 	
 	private int loadIgnoreLists() {
 		File file = new File(this.getDataFolder(), "ignoreList.txt");
 		
 		if(file.isFile()) {
 			try {
 				BufferedReader reader = new BufferedReader(new FileReader(file));
 				
 				int ignores = 0;
 				String line;
 				
 				while((line = reader.readLine()) != null) {
 					String[] parts = line.split(" ");
 					
 					if(parts.length == 2) {
 						playerManager.addIgnore(parts[0], parts[1]);
 						
 						ignores++;
 					}
 				}
 				
 				reader.close();
 				
 				return ignores;
 			}
 			catch(IOException e) {
 				e.printStackTrace();
 			}
 		}
 		
 		return 0;
 	}
 	
 	private int saveIgnoreLists() {
 		File file = new File(this.getDataFolder(), "ignoreList.txt");
 
 		try {
 			if(!file.isFile())
 				file.createNewFile();
 			
 			BufferedWriter writer = new BufferedWriter(new FileWriter(file));
 			
 			int ignores = 0;
 			
 			for(String player : playerManager.getIgnoringPlayers()) {
 				for(String ignored : playerManager.getIgnoreList(player)) {
 					writer.write(player + " " + ignored);
 					writer.newLine();
 					
 					ignores++;
 				}
 			}
 			
 			writer.close();
 			
 			return ignores;
 		}
 		catch(IOException e) {
 			e.printStackTrace();
 		}
 		
 		return 0;
 	}
 }
