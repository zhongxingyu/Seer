 package uk.co.jacekk.bukkit.simpleirc;
 
 import java.io.IOException;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.concurrent.Callable;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.NickAlreadyInUseException;
 import org.jibble.pircbot.PircBot;
 
 public class SimpleIRCBot extends PircBot implements Listener {
 	
 	private SimpleIRC plugin;
 	
 	public SimpleIRCBot(SimpleIRC plugin, String nick, String password, boolean verbose){
 		this.plugin = plugin;
 		
 		this.setVerbose(verbose);
 		this.setAutoNickChange(false);
 		this.setName(nick);
 		this.setLogin(nick);
 		this.setVersion(plugin.getName() + " " + plugin.getDescription().getVersion());
 		
 		String serverPassword = plugin.config.getString(Config.IRC_SERVER_PASSWORD);
 		
 		try{
 			if (serverPassword.isEmpty()){
 				this.connect(plugin.config.getString(Config.IRC_SERVER_ADDRESS), plugin.config.getInt(Config.IRC_SERVER_PORT));
 			}else{
 				this.connect(plugin.config.getString(Config.IRC_SERVER_ADDRESS), plugin.config.getInt(Config.IRC_SERVER_PORT), serverPassword);
 			}
 		}catch (NickAlreadyInUseException e){
 			plugin.log.fatal("The IRC nick you chose is already in use, it's probably a good idea to pick a unique one and register it with NickServ if the server allows it.");
 		}catch (IOException e){
 			e.printStackTrace();
 		}catch (IrcException e){
 			e.printStackTrace();
 		}
 		
 		if (!password.isEmpty()){
 			this.identify(password);
 		}
 		
 		for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 			this.joinChannel(channel);
 		}
 		
 		plugin.pluginManager.registerEvents(this, plugin);
 	}
 	
 	@Override
 	public void onMessage(String channel, String sender, String login, String hostname, String message){
 		if (!sender.equals(this.getNick())){
 			String senderLower = sender.toLowerCase();
 			String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : sender;  
 			
 			if (message.startsWith("!") && plugin.ircOps.contains(playerName) && !plugin.gameAliases.containsKey(senderLower)){
 				String command = message.substring(1);
 				
 				plugin.commandSender.setMessageTarget(channel);
 				plugin.server.dispatchCommand(plugin.commandSender, command);
 			}else{
 				RemotePlayerChatEvent event = new RemotePlayerChatEvent(playerName, message, new HashSet<Player>(Arrays.asList(plugin.server.getOnlinePlayers())));
 				
 				plugin.pluginManager.callEvent(event);
 				
 				if (!event.isCancelled()){
 					String chatMessage = String.format(event.getFormat(), playerName, ChatColorHelper.convertIRCtoMC(message));
 					
 					for (Player recipient : event.getRecipients()){
 						recipient.sendMessage(chatMessage);
 					}
 					
 					plugin.server.getConsoleSender().sendMessage(chatMessage);
 				}
 			}
 		}
 	}
 	
 	@Override
 	public void onPrivateMessage(String sender, String login, String hostname, String message){
 		String senderLower = sender.toLowerCase();
 		String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : sender; 
 		
 		if (plugin.ircOps.contains(playerName) && !plugin.gameAliases.containsKey(senderLower)){
 			plugin.commandSender.setMessageTarget(sender);
 			plugin.server.dispatchCommand(plugin.commandSender, message);
 		}
 	}
 	
 	@Override
 	public void onAction(String sender, String login, String hostname, String target, String action){
 		if (!sender.equals(this.getNick())){
 			String senderLower = sender.toLowerCase();
 			String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : sender; 
 			
 			plugin.server.broadcastMessage("* " + playerName + " " + action);
 		}
 	}
 	
 	@Override
 	public void onNickChange(String oldNick, String login, String hostname, String newNick){
 		if (plugin.gameAliases.containsKey(newNick.toLowerCase())){
 			for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 				this.kick(channel, newNick, "Attempting to impersonate an operator");
 			}
 		}
 	}
 	
 	@Override
 	public void onJoin(String channel, String sender, String login, String hostname){
 		if (!sender.equals(this.getNick())){
 			if (plugin.gameAliases.containsKey(sender.toLowerCase())){
 				this.kick(channel, sender, "Attempting to impersonate an operator");
 			}
 			
 			String playerName = (plugin.ircAliases.containsKey(sender)) ? plugin.ircAliases.get(sender) : sender;
 			
 			plugin.server.broadcastMessage(ChatColor.YELLOW + playerName + " has joined the chat");
 		}
 	}
 	
 	@Override
 	public void onPart(String channel, String sender, String login, String hostname){
 		if (!sender.equals(this.getNick())){
 			String senderLower = sender.toLowerCase();
 			String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : sender; 
 			
 			plugin.server.broadcastMessage(ChatColor.YELLOW + playerName + " has left the chat");
 		}
 	}
 	
 	@Override
 	public void onQuit(String sourceNick, String sourceLogin, String sourceHostname, String reason){
 		if (!sourceNick.equals(this.getNick())){
 			String senderLower = sourceNick.toLowerCase();
 			String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : sourceNick; 
 			
 			plugin.server.broadcastMessage(ChatColor.YELLOW + playerName + " has left the chat");
 		}
 	}
 	
 	@Override
 	public void onKick(String channel, String kickerNick, String kickerLogin, String kickerHostname, String recipientNick, String reason){
 		if (recipientNick.equals(this.getNick())){
 			this.joinChannel(channel);
 		}else{
 			String senderLower = recipientNick.toLowerCase();
 			String playerName = (plugin.ircAliases.containsKey(senderLower)) ? plugin.ircAliases.get(senderLower) : recipientNick; 
 			
 			plugin.server.broadcastMessage(ChatColor.YELLOW + playerName + " has left the chat");
 		}
 	}
 	
 	@Override
 	public void onDisconnect(){
 		plugin.log.warn("Disconnected from IRC, will reconnect in 5 seconds.");
 		
 		plugin.scheduler.scheduleSyncDelayedTask(plugin, new Runnable(){
 			
 			public void run(){
 				try{
 					SimpleIRCBot.this.reconnect();
 				}catch (Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 		}, 100L);
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerChat(final AsyncPlayerChatEvent event){
 		if (event.isAsynchronous()){
 			plugin.scheduler.callSyncMethod(plugin, new Callable<Boolean>(){
 				
 				public Boolean call() throws Exception{
 					String playerName = event.getPlayer().getName();
 					
 					if (plugin.gameAliases.containsKey(playerName)){
 						playerName = plugin.gameAliases.get(playerName);
 					}
 					
 					String message = ChatColorHelper.convertMCtoIRC(String.format(event.getFormat(), playerName, event.getMessage()));
 					
 					for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 						SimpleIRCBot.this.sendMessage(channel, message);
 					}
 					
 					return true;
 				}
 				
 			});
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		String playerName = event.getPlayer().getName();
 		String[] parts = event.getMessage().split(" ", 2);
 		
 		if (parts[0].equalsIgnoreCase("/me")){
 			if (plugin.gameAliases.containsKey(playerName)){
 				playerName = plugin.gameAliases.get(playerName);
 			}
 			
 			for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 				SimpleIRCBot.this.sendMessage(channel, "* " + playerName + " " + parts[1]);
 			}
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		String playerName = event.getPlayer().getName();
 		String message = event.getJoinMessage();
 		
 		if (plugin.gameAliases.containsKey(playerName)){
 			message = message.replaceAll(playerName, plugin.gameAliases.get(playerName));
 		}
 		
 		for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 			SimpleIRCBot.this.sendMessage(channel, ChatColorHelper.convertMCtoIRC(message));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerQuit(PlayerQuitEvent event){
 		String playerName = event.getPlayer().getName();
 		String message = event.getQuitMessage();
 		
 		if (plugin.gameAliases.containsKey(playerName)){
 			message = message.replaceAll(playerName, plugin.gameAliases.get(playerName));
 		}
 		
 		for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 			SimpleIRCBot.this.sendMessage(channel, ChatColorHelper.convertMCtoIRC(message));
 		}
 	}
 	
 	@EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
 	public void onPlayerDeath(PlayerDeathEvent event){
 		String playerName = event.getEntity().getName();
 		String message = event.getDeathMessage();
 		
 		if (plugin.gameAliases.containsKey(playerName)){
 			message = message.replaceAll(playerName, plugin.gameAliases.get(playerName));
 		}
 		
 		for (String channel : plugin.config.getStringList(Config.IRC_BOT_CHANNELS)){
 			SimpleIRCBot.this.sendMessage(channel, ChatColorHelper.convertMCtoIRC(ChatColor.GRAY + message));
 		}
 	}
 	
 }
