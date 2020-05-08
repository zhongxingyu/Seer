 package me.corriekay.pppopp3.chat;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 
 import me.corriekay.pppopp3.Mane;
 import me.corriekay.pppopp3.events.JoinEvent;
 import me.corriekay.pppopp3.events.PrivateMessageEvent;
 import me.corriekay.pppopp3.events.QuitEvent;
 import me.corriekay.pppopp3.ponymanager.PonyManager;
 import me.corriekay.pppopp3.ponyville.Pony;
 import me.corriekay.pppopp3.ponyville.Ponyville;
 import me.corriekay.pppopp3.utils.PSCmdExe;
 import me.corriekay.pppopp3.utils.PonyLogger;
 import me.corriekay.pppopp3.utils.Utils;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.AsyncPlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 
 public class ChatHandler extends PSCmdExe{
 
 	private final HashMap<String,Channel> channels = new HashMap<String,Channel>();
 	private final HashMap<String,Channel> chatting = new HashMap<String,Channel>();
 	private final HashMap<String,String> pmTargets = new HashMap<String,String>();
 	private final HashSet<String> muted = new HashSet<String>();
 	protected final static HashMap<String,String> lastSeenMessage = new HashMap<String,String>();
 	protected final static HashMap<String,String> lastChannel = new HashMap<String,String>();
 	public static ChatHandler ch;
 	protected static String regex;
 	protected static HashMap<String,HashSet<String>> silenced = new HashMap<String,HashSet<String>>();
 
 	public ChatHandler() throws Exception{
 		super("ChatHandler", "join", "leave", "channel", "say", "me", "mute", "pm", "r", "silence", "silenced", "channelcolor");
 		//methodMap.put(ChannelMessageEvent.class, this.getClass().getDeclaredMethod("messageRecieveEvent", PlayerRecieveMessageEvent.class));
 		ch = this;
 		FileConfiguration config = getNamedConfig("channels.yml");
 		regex = "^(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]";
 		for(String channelName : config.getKeys(false)) {
 			ChatColor color = ChatColor.valueOf(config.getString(channelName + ".color"));
 			String icon = config.getString(channelName + ".icon");
 			String permission = config.getString(channelName + ".permission");
 			String quick = config.getString(channelName + ".quick");
 			Channel channel = new Channel(channelName, icon, permission, color, quick);
 			channels.put(channelName, channel);
 		}
 		channels.put("world", new WorldChannel("world", "[WORLD]", "pppopp3.chat.world", ChatColor.DARK_AQUA, "world"));
 		for(Player player : Bukkit.getOnlinePlayers()) {
 			initiatePlayerChannels(player);
 			pmTargets.put(player.getName(), null);
 			Pony p = Ponyville.getPony(player);
 			HashSet<String> mute = p.getSilenced();
 			initiateSilenceList(player.getName(), mute);
 		}
 	}
 
 	private void initiatePlayerChannels(Player player){
 		Pony p = Ponyville.getPony(player);
 		if(p.isMuted()) {
 			muted.add(player.getName());
 		}
 		Channel chatChannel = channels.get(p.getChatChannel());
 		if(chatChannel != null) {
 			if(checkCanJoin(chatChannel, player)) {
 				joinChannel(chatChannel, player, true, false, false, false);
 				chatting.put(player.getName(), chatChannel);
 			}
 		}
 		for(String channel : p.getListeningChannels()) {
 			Channel listenChan = channels.get(channel);
 			if(listenChan == null) {
 				continue;
 			} else {
 				if(checkCanJoin(listenChan, player)) {
 					joinChannel(listenChan, player, false, false, false, false);
 				}
 			}
 		}
 	}
 
 	private void initiateSilenceList(String name, HashSet<String> muted){
 		HashSet<String> m = new HashSet<String>();
 		m.addAll(muted);
 		silenced.put(name, m);
 	}
 
 	public boolean handleCommand(CommandSender sender, Command cmd, String label, String[] args){
 		if(cmd.getName().equals("mute")) {
 			if(args.length < 1) {
 				sendMessage(sender, notEnoughArgs);
 				return true;
 			}
 			Player player = getOnlinePlayer(args[0], sender);
 			if(player == null) {
 				return true;
 			}
 			Pony p = Ponyville.getPony(player);
 			if(muted.contains(player.getName())) {
 				p.setMuted(false);
 				muted.remove(player.getName());
 				sendMessage(player, "Youre a good pony! You've been unmuted!");
 				sendMessage(sender, "Yay! you unmuted " + player.getDisplayName() + ChatColor.LIGHT_PURPLE + "!");
 				logAdmin(sender, "Unmuted " + player.getName());
 			} else {
 				p.setMuted(false);
 				muted.add(player.getName());
 				sendMessage(player, "Naughty naughty! You've been muted!");
 				sendMessage(sender, "Awh, You muted " + player.getDisplayName() + ChatColor.LIGHT_PURPLE + "...");
 				logAdmin(sender, "Muted " + player.getName());
 			}
 			p.save();
 			return true;
 		}
 		if(cmd.getName().equals("me")) {
 			if(args.length < 1) {
 				sendMessage(sender, notEnoughArgs);
 				return true;
 			}
 			String emote = "";
 			for(String word : args) {
 				emote += word + " ";
 			}
 			if(sender instanceof Player) {
 				Player player = (Player)sender;
 				Channel channel = chatting.get(player.getName());
 				if(channel == null) {
 					sendMessage(player, "Hey! Youre not in a channel!");
 					return true;
 				} else {
 					channel.broadcastRawMessage(channel.color() + "* " + player.getDisplayName() + " " + channel.color() + emote, true);
 					return true;
 				}
 			} else {
 				channels.get("equestria").broadcastRawMessage(ChatColor.AQUA + "* Server " + emote, true);
 				return true;
 			}
 		}
 		if(cmd.getName().equals("channel") || cmd.getName().equals("say")) {
 			if(!(sender instanceof Player)) {
 				Channel channel = getChannel(label);
 				if(channel == null) {
 					sendMessage(sender, ">_o wat. Channel not found! D:");
 					return true;
 				}
 				if(args.length < 1) {
 					sendMessage(sender, notEnoughArgs);
 					return true;
 				}
 				String message = "";
 				for(String word : args) {
 					message += word + " ";
 				}
 				channel.broadcastToChannel("Server", "Server", message, true);
 				return true;
 			}
 			Player player = (Player)sender;
 			String chans = "";
 			for(String s : channels.keySet()) {
 				chans += " " + channels.get(s).color() + s;
 			}
 			sendMessage(player, "heres a list of the channels!:" + chans);
 			return true;
 		}
 		Player player;
 		if(sender instanceof Player) {
 			player = (Player)sender;
 		} else {
 			sendMessage(sender, notPlayer);
 			return true;
 		}
 		if(cmd.getName().equals("silence")) {
 			if(args.length < 1) {
 				sendMessage(player, notEnoughArgs);
 				return true;
 			}
 			String sop = getOfflinePlayer(args[0], player);
 			if(sop == null) {
 				return true;
 			}
 			OfflinePlayer op = Bukkit.getOfflinePlayer(sop);
 			{
 				String g = PonyManager.getGroup(op.getName());
 				if(g.equalsIgnoreCase("admin") || g.equalsIgnoreCase("opony")) {
 					sendMessage(player, "You cant silence that pony!");
 					return true;
 				}
 			}
 			Pony pony = Ponyville.getPony(player);
 			HashSet<String> sl = pony.getSilenced();
 			if(sl.contains(op.getName())) {
 				sendMessage(player, "Unsilencing player " + op.getName() + "!");
 				sl.remove(op.getName());
 			} else {
 				sendMessage(player, "Silencing player " + op.getName());
 				sl.add(op.getName());
 			}
 			pony.setSilenced(sl);
 			silenced.put(player.getName(), new HashSet<String>(sl));
 			pony.save();
 			return true;
 		}
 		if(cmd.getName().equals("silenced")) {
 			HashSet<String> sl = Ponyville.getPony(player).getSilenced();
 			if(sl.isEmpty()) {
 				sendMessage(player, "Silly willy, you havnt silenced anyone!");
 				return true;
 			}
 			Iterator<String> s = sl.iterator();
 			StringBuilder sb = new StringBuilder();
 			String p = null;
 			while(s.hasNext()) {
 				p = s.next();
 				sb.append(p);
 				if(s.hasNext()) {
 					sb.append(", ");
 				}
 			}
 			sendMessage(player, "These are the players you have silenced: " + sb);
 			return true;
 		}
 		if(cmd.getName().equals("join")) {
 			if(args.length < 1) {
 				sendMessage(player, notEnoughArgs);
 				return true;
 			}
 			Channel channel = getChannel(args[0]);
 			if(channel == null) {
 				sendMessage(player, ">_o wat. Channel not found! D:");
 				return true;
 			}
 			if(channel.isChatting(player)) {
 				sendMessage(player, "Pfft, youre already chatting there silly!");
 				return true;
 			}
 			if(player.hasPermission(channel.permission())) {
 				try {
 					chatting.get(player.getName()).leaveChatters(player, false);
 				} catch(NullPointerException e) {}
 				channel.joinChatters(player, true);
 				chatting.put(player.getName(), channel);
 				if(!channel.isListening(player)) {
 					channel.joinListeners(player, false);
 				}
 				updateChannels(player);
 				return true;
 			} else {
 				sendMessage(player, "You cant join that channel!");
 				return true;
 			}
 		}
 		if(cmd.getName().equals("leave")) {
 			Channel channel;
 			if(args.length == 0) {
 				channel = getChattingChannel(player);
 			} else {
 				channel = getChannel(args[0]);
 			}
 			if(channel == null) {
 				sendMessage(player, "Silly, youre not in a channel!");
 				return true;
 			}
 			boolean chatteng = channel.isChatting(player);
 			if(chatteng) {
 				channel.leaveChatters(player, true);
 				channel.leaveListeners(player, false);
 				ArrayList<Channel> chans = getListeningChannels(player);
 				if(chans.size() == 0) {
 					sendMessage(player, "Youre no longer in any chat channels! Ah... sweet silence~");
 					chatting.put(player.getName(), null);
 					updateChannels(player);
 					return true;
 				}
 				Channel chan = chans.get(0);
 				chan.joinChatters(player, true);
 				chatting.put(player.getName(), chan);
 			} else {
 				channel.leaveListeners(player, true);
 			}
 			updateChannels(player);
 			return true;
 		}
 		if(cmd.getName().equals("channel")) {
 			if(label.equals("channel")) {
 				return true;
 			} else {
 				Channel chan = getChannel(label);
 				if(chan == null) {
 					sendMessage(player, ">_o wat. Channel not found! D:");
 					return true;
 				} else {
 					if(chan.isListening(player)) {
 						String msg = "";
 						for(String word : args) {
 							msg += word + " ";
 						}
 						chan.broadcastToChannel(player.getDisplayName(), player.getName(), msg, true);
 						return true;
 					}
 					sendMessage(player, "Youre not in that channel, please join the channel first!");
 					return true;
 				}
 			}
 		}
 		if(cmd.getName().equals("pm")) {
 			if(args.length < 2) {
 				sendMessage(player, notEnoughArgs);
 				return true;
 			}
 			Player target = getOnlinePlayer(args[0], player);
 			if(target == null) {
 				return true;
 			}
 			String msg = "";
 			for(int i = 1; i < args.length; i++) {
 				msg += args[i] + " ";
 			}
 			PrivateMessageEvent pme = new PrivateMessageEvent(player, target, msg.trim());
 			Bukkit.getPluginManager().callEvent(pme);
 			if(!pme.isCancelled()) {
 				player.sendMessage(ChatColor.RED + "[PM][You > " + target.getDisplayName() + ChatColor.RED + "]" + ChatColor.GRAY + ": " + pme.getMsg());
 				target.sendMessage(ChatColor.RED + "[PM][" + player.getDisplayName() + ChatColor.RED + " > You]" + ChatColor.GRAY + ": " + pme.getMsg());
 				pmTargets.put(player.getName(), target.getName());
 				pmTargets.put(target.getName(), player.getName());
 			}
 			return true;
 		}
 		if(cmd.getName().equals("r")) {
 			if(args.length < 1) {
 				sendMessage(player, notEnoughArgs);
 				return true;
 			}
 			String targetS = pmTargets.get(player.getName());
 			if(targetS == null) {
 				sendMessage(player, "I looked high and low, but I couldnt find that pony! :C");
 				return true;
 			}
 			Player target = Bukkit.getPlayerExact(targetS);
 			if(target == null) {
 				sendMessage(player, "I looked high and low, but I couldnt find that pony! :C");
 				return true;
 			}
 			String msg = "";
 			for(String word : args) {
 				msg += word + " ";
 			}
 			PrivateMessageEvent pme = new PrivateMessageEvent(player, target, msg.trim());
 			Bukkit.getPluginManager().callEvent(pme);
 			if(!pme.isCancelled()) {
 				player.sendMessage(ChatColor.RED + "[PM][You > " + target.getDisplayName() + ChatColor.RED + "]" + ChatColor.GRAY + ": " + pme.getMsg());
 				target.sendMessage(ChatColor.RED + "[PM][" + player.getDisplayName() + ChatColor.RED + " > You]" + ChatColor.GRAY + ": " + pme.getMsg());
 				pmTargets.put(player.getName(), target.getName());
 				pmTargets.put(target.getName(), player.getName());
 			}
 			return true;
 		}
 		if(cmd.getName().equals("channelcolor")) {
 			if(args.length < 1) {
 				sendMessage(player, "This command changes a chat channels color to a different color! If you dont specify a channel, it will default to the channel you are currently chatting in. If you type \"default\" for the color, it will set the color to the channels default color!");
 				return true;
 			}
 			Channel chan;
 			ChatColor color;
 			if(args.length > 1) {
 				chan = getChannel(args[1]);
 			} else {
 				chan = getChattingChannel(player);
 			}
 			if(chan == null) {
 				sendMessage(player, "Either that channel cannot be found, or you aren't even talking in a channel! Please specify a correct channel!");
 				return true;
 			}
 			if(!checkCanJoin(chan, player)) {
 				sendMessage(player, "Uh oh, you cant modify that channel!");
 				return true;
 			}
			try {
				if(args[0].equals("default")) {
					color = chan.cc;
				} else {
					color = ChatColor.valueOf(args[0].toUpperCase());
				}
			} catch(Exception e) {
 				sendMessage(player, "Color not found!");
 				return true;
 			}
 			Pony pony = Ponyville.getPony(player);
 			pony.setChannelColor(chan.name, color);
 			pony.save();
 			sendMessage(player, "Yay! you just set " + chan.name + "'s color to " + color + color.name() + "!");
 			return true;
 		}
 		return true;
 	}
 
 	private void joinChannel(Channel channel, Player player, boolean chat, boolean notifyCant, boolean notifyJoin, boolean notifyChat){
 		if(checkCanJoin(channel, player)) {
 			channel.joinListeners(player, notifyJoin);
 			if(chat) {
 				channel.joinChatters(player, notifyChat);
 			}
 		} else {
 			if(notifyCant) {
 				sendMessage(player, "You cant join that channel!");
 			}
 			return;
 		}
 	}
 
 	private boolean checkCanJoin(Channel channel, Player player){
 		return player.hasPermission(channel.permission());
 	}
 
 	private Channel getChattingChannel(Player player){
 		for(Channel c : channels.values()) {
 			if(c.isChatting(player)) {
 				return c;
 			}
 		}
 		return null;
 	}
 
 	public Channel getChannel(String channelName){
 		for(Channel chan : channels.values()) {
 			if(chan.getName().equalsIgnoreCase(channelName) || chan.getQuick().equalsIgnoreCase(channelName)) {
 				return chan;
 			}
 		}
 		return null;
 	}
 
 	private ArrayList<Channel> getListeningChannels(Player player){
 		ArrayList<Channel> channelz = new ArrayList<Channel>();
 		for(Channel channel : channels.values()) {
 			if(channel.isListening(player)) {
 				channelz.add(channel);
 			}
 		}
 		return channelz;
 	}
 
 	private void updateChannels(Player player){
 		Pony pony = Ponyville.getPony(player);
 		Channel chan = chatting.get(player.getName());
 		if(chan != null) {
 			pony.setChatChannel(chan.getName());
 		}
 		HashSet<String> channels = new HashSet<String>();
 		for(Channel chann : getListeningChannels(player)) {
 			channels.add(chann.getName());
 		}
 		pony.setListenChannels(channels);
 		pony.save();
 	}
 
 	@EventHandler (priority = EventPriority.LOW, ignoreCancelled = false)
 	public void onChat(AsyncPlayerChatEvent event){
 		if(event.isCancelled()) {
 			return;
 		}
 		Player player = event.getPlayer();
 		if(muted.contains(player.getName())) {
 			event.setCancelled(true);
 			sendMessage(player, "Naughty naughty! You're muted!");
 			return;
 		}
 		Channel channel = chatting.get(player.getName());
 		if(channel == null) {
 			sendMessage(event.getPlayer(), "Youre not chatting in a channel! You gotta join a channel by typing /join <channel>!");
 			event.setCancelled(true);
 			return;
 		}
 		channel.broadcastToChannel(player.getDisplayName(), player.getName(), event.getMessage(), true);
 		event.setCancelled(true);
 	}
 
 	@EventHandler
 	public void onJoin(JoinEvent event){
 		if(event.isJoining()) {
 			initiatePlayerChannels(event.getPlayer());
 			pmTargets.put(event.getPlayer().getName(), null);
 			initiateSilenceList(event.getPlayer().getName(), event.getPony().getSilenced());
 		}
 	}
 
 	@EventHandler
 	public void onQuit(QuitEvent event){
 		pmTargets.remove(event.getPlayer().getName());
 		for(String key : pmTargets.keySet()) {
 			String value = pmTargets.get(key);
 			if(value == null) {
 				continue;
 			}
 			if(pmTargets.get(key).equals(event.getPlayer().getName())) {
 				pmTargets.put(key, null);
 			}
 		}
 		if(event.isQuitting()) {
 			pmTargets.remove(event.getPlayer().getName());
 			silenced.remove(event.getPlayer().getName());
 			for(Channel chan : channels.values()) {
 				chan.leaveChatters(event.getPlayer(), false);
 				chan.leaveListeners(event.getPlayer(), false);
 			}
 		}
 	}
 
 	@EventHandler
 	public void onCommand(PlayerCommandPreprocessEvent event){
 		String msg = "[" + Utils.getTimeStamp(System.currentTimeMillis()) + "]" + event.getPlayer().getName() + ": " + event.getMessage();
 		PonyLogger.logCommand(Mane.getInstance().getDataFolder() + File.separator + "ChatLogs" + File.separator + Utils.getFileDate(System.currentTimeMillis()), "commandlog", msg);
 	}
 
 	/*
 	@EventHandler
 	public void messageRecieveEvent(PlayerRecieveMessageEvent event){
 		if(event instanceof ChannelMessageEvent){
 			ChannelMessageEvent cme = (ChannelMessageEvent)event;
 			String lastsent = lastSeenMessage.get(cme.getName());
 			String lastChan = lastChannel.get(cme.getName());
 			lastSeenMessage.put(cme.getName(), cme.getWhosent());
 			lastChannel.put(cme.getName(), cme.getChanname());
 			if(lastsent != null&&lastChan != null){
 				if(lastsent.equals(cme.getWhosent())){
 					if (lastChan.equals(cme.getChanname())) {
 						cme.setWhosent(null);
 					}
 				}
 			}
 			return;
 		} else {
 			lastSeenMessage.put(event.getName(), null);
 			lastChannel.put(event.getName(), null);
 		}
 	}*/
 	public String[] getChannelNames(){
 		String[] chans = new String[channels.keySet().size()];
 		int i = 0;
 		for(String chan : channels.keySet()) {
 			chans[i] = chan;
 			i++;
 		}
 		return chans;
 	}
 }
