 package me.asofold.bukkit.contextmanager;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedHashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.configuration.MemoryConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import asofold.pluginlib.shared.Messaging;
 import asofold.pluginlib.shared.permissions.pex.PexUtil;
 
 public class ContextManager extends JavaPlugin implements Listener{
 	
 	public static final String plgLabel = "[ContextManager]";
 	
 	String msgCol = ChatColor.WHITE.toString();
 	String partyNameCol = ChatColor.GRAY.toString();
 	String partyBracketCol = ChatColor.GREEN.toString();
 	String partyMsgCol = ChatColor.GRAY.toString();
 	String broadCastCol = ChatColor.YELLOW.toString();
 	
 	Set<String> mutePreventCommands = new HashSet<String>();
 	
 	/**
 	 * Lower case -> correct case.
 	 */
 	Map<String, String> channels = new LinkedHashMap<String, String>();
 	
 	ArrayList<String> channelsOrdered = new ArrayList<String>();
 	
 	/**
 	 * muted players
 	 */
 
     Map<String, Long> muted = new LinkedHashMap<String, Long>();
     
     public Map<String, PlayerData> playerData = new HashMap<String, PlayerData>();
 	
 	boolean useEvent = true;
 	
 	public ContextManager(){
 		channelsOrdered.add(ContextManager.defaultChannelName);
 	}
 	
 	CMCommand cmdExe = new CMCommand(this);
 	
 	List<HistoryElement> history = new LinkedList<HistoryElement>(); 
 	
 	int histSize = 100;
 	
 	/**
 	 * TODO: is a hack ?
 	 */
 	static String defaultChannelName = "global";
 	
 	@Override
 	public void onEnable() {
 		loadSettings();
 		getServer().getPluginManager().registerEvents(this, this);
 		for ( String cmd : new String[]{
 			"cmreload",	"cmmute", "cmunmute", "mute", "unmute", "demute", "muted",
 			"context", "cxc", "cxch", "cxr", "cxrec", "cxign", "cxcl", "cxinf",
 		}){
 			// TODO: Most probably unnecessary !
 			getCommand(cmd).setExecutor(cmdExe);
 		}
 		System.out.println(plgLabel+getDescription().getFullName()+ "enabled.");
 	}
 	
 	public static final boolean hasPermission(CommandSender sender, String perm){
 		return sender.isOp() || sender.hasPermission(perm);
 	}
 	
 	public static final void send(CommandSender sender, String message){
 		if ( sender instanceof Player) sender.sendMessage(message);
 		else sender.sendMessage(ChatColor.stripColor(message));
 	}
 	
 	/**
 	 * With failure message.
 	 * @param sender
 	 * @param perm
 	 * @return
 	 */
 	public static final boolean checkPerm(CommandSender sender, String perm){
 		if (hasPermission(sender, perm)) return true;
 		send(sender, ChatColor.DARK_RED+"Permission not present: "+perm);
 		return false;
 	}
 	
 	public static boolean checkPlayer(CommandSender sender){
 		if (sender instanceof Player) return true;
 		sender.sendMessage(plgLabel+" This is only available to players!");
 		return false;
 	}
 	
 	public static final boolean isPlayerKnown(String name){
 		if ( Bukkit.getPlayerExact(name)!=null) return true;
 		OfflinePlayer player = Bukkit.getOfflinePlayer(name);
 		if ( player == null) return false;
 		if ( player.hasPlayedBefore()) return true;
 		return false;
 	}
 	
 	/**
 	 * Obtain player data.
 	 * @param playerName
 	 * @param create
 	 * @return
 	 */
 	public PlayerData getPlayerData(String playerName, boolean create){
 		String lcName = playerName.toLowerCase(); 
 		PlayerData data = playerData.get(lcName);
 		if (data != null) return data;
 		else if (!create) return null;
 		else{
 			data = new PlayerData(lcName);
 			playerData.put(lcName, data);
 			return data;
 		}
 	}
 	
 	/**
 	 * Obtain PalyerData, create if not existent.
 	 * @param playerName
 	 * @return
 	 */
 	public PlayerData getPlayerData(String playerName){
 		return getPlayerData(playerName, true);
 	}
 	
 	/**
 	 * 
 	 * @param parts
 	 * @param link can be null
 	 * @return
 	 */
 	public static final String join(Collection<String> parts, String link){
 		StringBuilder builder = new StringBuilder();
 		int i = 0;
 		int max = parts.size();
 		for ( String part : parts){
 			builder.append(part);
 			i++;
 			if ( i<max && link!=null ) builder.append(link);
 		}
 		return builder.toString();
 	}
 	
 
 
 
 
 	/**
 	 * Light checks to be performed now and then.
 	 * (such as cleanup muted).
 	 */
 	void lightChecks(){
 		checkMuted();
 	}
 	
 	/**
 	 * Remove expired from muted.
 	 */
 	void checkMuted(){
 		List<String> rem = new LinkedList<String>();
 		long ts = System.currentTimeMillis();
 		for (String n : muted.keySet()){
 			long nts = muted.get(n);
 			if ( nts == 0 ) continue;
 			else if (nts<ts) rem.add(n);
 		}
 		for ( String n : rem){
 			muted.remove(n);
 		}
 	}
 
 	public void loadSettings() {
 		reloadConfig();
 		Configuration cfg = getConfig();
 		cfg.setDefaults(getDefaultSettings());
 		cfg.options().copyDefaults(true);
 		saveConfig(); // if ( !new File(getDataFolder(), "plugin.yml").exists()) 
 		useEvent = cfg.getBoolean("chat.use-event");
 		broadCastCol = Messaging.withChatColors(cfg.getString("chat.color.announce"));
 		msgCol = Messaging.withChatColors(cfg.getString("chat.color.normal"));
 		partyBracketCol = Messaging.withChatColors(cfg.getString("chat.color.party.brackets"));
 		partyNameCol = Messaging.withChatColors(cfg.getString("chat.color.party.name"));
 		partyMsgCol = Messaging.withChatColors(cfg.getString("chat.color.party.message"));
		ContextManager.defaultChannelName = cfg.getString("channels.default-channel-name", "default").trim();
 		histSize = cfg.getInt("history.size");
 		mutePreventCommands.clear();
 		channels.clear();
 		channelsOrdered.clear();
 		channelsOrdered.add(ContextManager.defaultChannelName);
 		List<String> ch = cfg.getStringList("contexts.channels.names");
 		if (ch != null){
 			for (String c : ch){
				c = c.trim();
 				channels.put(c.trim().toLowerCase(), c);
 				channelsOrdered.add(c);
 			}
 		}
 		for (PlayerData data: playerData.values()){
 			if (data.channel != null) data.setChannel(getAvailableChannel(data.channel));
 		}
 		List<String> cmds = cfg.getStringList("mute.prevent-commands");
 		if (cmds!= null){
 			for (String cmd : cmds){
 				cmd = cmd.trim().toLowerCase();
 				if (!cmd.isEmpty()) mutePreventCommands.add(cmd);
 			}
 		}
 	}
 	
 	public MemoryConfiguration getDefaultSettings(){
 		MemoryConfiguration cfg = new MemoryConfiguration();
 		cfg.set("chat.use-event", true);
 		cfg.set("chat.color.normal", "&f");
 		cfg.set("chat.color.announce", "&e");
 		cfg.set("chat.color.party.brackets", "&a");
 		cfg.set("chat.color.party.name", "&7");
 		cfg.set("chat.color.party.message", "&7");
 		cfg.set("mute.prevent-commands", new LinkedList<String>());
 		cfg.set("contexts.channels.names", new LinkedList<String>());
 		cfg.set("history.size", 100);
 		cfg.set("channels.default-channel-name", "default");
 //		List<String> load = new LinkedList<String>();
 //		for ( String plg : new String[]{
 //				"PermissionsEx", "mcMMO"
 //		}){
 //			load.add(plg);
 //		}
 		return cfg;
 	}
 	
 	@EventHandler(priority=EventPriority.HIGHEST)
 	void onPlayerCommand(PlayerCommandPreprocessEvent event){
 		if (event.isCancelled()) return;
 		final String msg = event.getMessage().trim();
 		final String[] split = msg.split(" ");
 		String cmd = split[0].toLowerCase();
 		if (cmd.length()>1) cmd = cmd.substring(1);
 		// TODO: check set of others ?
 		final Player player = event.getPlayer();
 		if (cmd.equals("tell")){
 			// TODO: maybe log and maybe message player self.
 			event.setCancelled(true);
 			if (split.length > 1){
 				final String playerName =  player.getName(); 
 				String lcName = playerName.toLowerCase();
 				String recipient = split[1].trim().toLowerCase();
 				if (lcName.equals(recipient)) return;
 				if (recipient.isEmpty()){
 					// TODO: maybe better command parsing (stripping of space only parts).
 					player.sendMessage(ChatColor.DARK_RED+plgLabel+" Bad tell message.");
 					return;
 				}
 				PlayerData otherData = getPlayerData(recipient, false);
 				if (otherData != null && otherData.ignored.contains(lcName) && !hasPermission(player, "contextmanager.bypass.tell")){
 					player.sendMessage(ChatColor.DARK_RED+plgLabel+" You are ignored by this player.");
 					return;
 				}
 				Player other = getServer().getPlayerExact(recipient);
 				if (other != null && !player.canSee(other) && !hasPermission(player, "contextmanager.bypass.tell")){
 					if (!otherData.recipients.contains(lcName)) other = null;
 				}
 				if (other != null){
 					final String tellMsg = "[Tell] ("+player.getName()+" -> "+other.getName()+") "+join(getCollection(split, 2), " ");
 					System.out.println(tellMsg);
 					player.sendMessage(ChatColor.DARK_GRAY + tellMsg);
 					other.sendMessage(ChatColor.GRAY+ tellMsg);
 					addToHistory(new HistoryElement(ContextType.PRIVATE, playerName, "", tellMsg, false));
 				}
 				else{
 					player.sendMessage(ChatColor.DARK_RED+"[Tell] "+recipient+" is not available.");
 				}
 				return; // TODO: consider cancelling tell always
 			}
 		}
 		if (!cmd.equals("me") && !mutePreventCommands.contains(cmd)) return;
 		// TODO: maybe find the fastest way to do this !
 		if (isMuted(player)) event.setCancelled(true);
 	}
 	
 	public static Collection<String> getCollection(String[] args, int  startIndex){
 		List<String> out = new LinkedList<String>();
 		for ( int i = startIndex; i<args.length; i++){
 			out.add(args[i]);
 		}
 		return out;
 	}
 
 	@EventHandler(priority=EventPriority.LOW)
 	void onPlayerChat(PlayerChatEvent event){
 		if (event.isCancelled()) return;
 		String message = event.getMessage();
 		Player player = event.getPlayer();
 		
 		// Tell shortcut
 		if (message.startsWith("@")){
 			// tell shortcut
 			event.setCancelled(true);
 			final String cmd;
 			if (message.startsWith("@ ")) cmd = "/tell "+message.substring(2);
 			else cmd = "/tell "+message.substring(1);
 			final PlayerCommandPreprocessEvent tellEvent = new PlayerCommandPreprocessEvent(player, cmd);
 			getServer().getPluginManager().callEvent(tellEvent); // will be replaced by THIS plugin.
 			return;
 		}
 		
 		
 		// Muted
 		if (isMuted(player)){
 			event.setCancelled(true);
 			return;
 		}
 		
 		// Announcements
 		String msgCol = null; // this.msgCol;
 		boolean forceBroadcast = false;
 		boolean isAnnounce = false;
 		if ( message.startsWith("!") && player.hasPermission("contextmanager.chat.announce")){
 			isAnnounce = true;
 			msgCol = broadCastCol;
 			int n = 1;
 			if ( message.startsWith("!!")){
 				forceBroadcast = true;
 				n =2;
 			}
 			message = message.substring(n,message.length());
 			if ( useEvent) event.setMessage(message);
 		}
 		
 		// History:
 		final String playerName = player.getName();
 		PlayerData data = getPlayerData(playerName);
 		boolean isParty = isPartyChat(player);
 		ContextType type;
 		if (forceBroadcast) type = ContextType.BROADCAST;
 		else if (isAnnounce){
			if (data.channel == null) type = ContextType.DEFAULT;
 			else type = ContextType.CHANNEL;
 		}
 		else if (isParty) type = ContextType.PARTY;
 		else if (!data.recipients.isEmpty()) type = ContextType.PRIVATE;
		else if (data.channel == null) type = ContextType.DEFAULT;
 		else type = ContextType.CHANNEL;
 		addToHistory(new HistoryElement(type, playerName, data.extraFormat, message, isAnnounce));
 		
 		// recipients
 		Set<Player> recipients;
 		boolean useEvent = this.useEvent;
 		if (isAnnounce){
 			// force recipients to prevent other plugins messing around.
 			recipients = new HashSet<Player>();
 			for (Player other :  getServer().getOnlinePlayers()){
 				recipients.add(other);
 			}
 			useEvent = false;
 		}
 		else recipients = event.getRecipients();
 		if ( !forceBroadcast) adjustRecipients(player, recipients, isAnnounce);
 		
 		// TODO: filters for who wants to hear (or must) and who should hear / forces to hear
 		
 		// Assemble message
 		String format;
 		if (isAnnounce) format = getNormalFormat(playerName, msgCol, isAnnounce);
 		else format = getFormat(player, msgCol, isAnnounce);
 		if (useEvent) event.setFormat(format);
 		else {
 			event.setCancelled(true);
 			String sendMsg = format.replace("%1$s", playerName).replace("%2$s", message);
 			if (forceBroadcast) getServer().broadcastMessage(sendMsg);
 			else{
 				for ( Player other : recipients){
 					other.sendMessage(sendMsg);
 				}
 				System.out.println(sendMsg);
 			}
 		}
 	}
 	
 	/**
 	 * Also messages the player or removes the timestamp from the map, if expired.
 	 * @param player
 	 * @return
 	 */
 	public boolean isMuted(Player player) {
 		String lcn = player.getName().toLowerCase();
 		Long tsMute = muted.get(lcn);
 		if ( tsMute !=null){
 			if ( ( tsMute!=0L) && (System.currentTimeMillis() > muted.get(lcn))){
 				muted.remove(lcn);
 			}
 			else if ( hasPermission(player, "contextmanager.bypass.mute")){
 				send(player, ChatColor.YELLOW+plgLabel+" Removed you from muted (permission present).");
 				muted.remove(lcn);
 			}
 			else if ( isGlobalChat(player) ){
 				// TODO: add time descr.
 				send(player, ChatColor.RED+plgLabel+" You are currently muted.");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	boolean isPartyChat(Player player){
 		try{
 			if (com.gmail.nossr50.Users.getProfile(player).getPartyChatMode()) return true;
 		} catch( Throwable t){
 		}
 		return false;
 	}
 	
 	private boolean inSameParty( Player p1, Player p2){
 		try{
 			return com.gmail.nossr50.party.Party.getInstance().inSameParty(p1, p1);
 		} catch( Throwable t){
 			
 		}
 		return false;
 	}
 	
 	private boolean isGlobalChat(Player player) {
 		if ( isPartyChat(player)) return false;
 		return true;
 	}
 
 	public final String getFormat(final Player player, final String msgCol, boolean isAnnounce){
 		final String playerName = player.getName();
 		// TODO: context dependent ...
 		if ( !isAnnounce && isPartyChat(player)) return getPartyFormat(playerName, msgCol);
 		else return getNormalFormat(playerName, msgCol, isAnnounce);
 	}
 	
 	public final String getNormalFormat(String playerName, String msgCol, boolean isAnnounce){
 		if (msgCol == null) msgCol = this.msgCol;
 		String[] decorated = PexUtil.findDecoration(playerName);
 		if (decorated[0] == null) decorated[0] = "";
 		else decorated[0] = Messaging.withChatColors(decorated[0]);
 		if (decorated[1] == null) decorated[1] = "";
 		else decorated[1] = Messaging.withChatColors(decorated[1]);
 		return msgCol+"<"+decorated[0]+"%1$s"+decorated[1]+msgCol+">"+(isAnnounce?"":getPlayerData(playerName).extraFormat)+msgCol+" %2$s";
 	}
 	
 	public final String getPartyFormat(String playerName, String msgCol){
 		if (msgCol == null) msgCol = partyMsgCol;
 		return partyBracketCol+"("+partyNameCol+"%1$s"+partyBracketCol+")"+msgCol+" %2$s";
 	}
 	/**
 	 * Adjust the list of recipients according to context.
 	 * @param player
 	 * @param recipients
 	 */
 	public final void adjustRecipients(Player player, Set<Player> recipients, boolean isAnnounce) {
 		PlayerData data = getPlayerData(player.getName());
 		if ( !isAnnounce && isPartyChat(player)){
 			// TODO: these might be already set by mcmmo, on the other hand this allows for others.
 			List<Player> add = new LinkedList<Player>();
 			for ( Player rec : recipients){ // TODO: get directly from mcMMO
 				if (inSameParty(player, rec)) add.add(rec);
 			}
 			recipients.clear(); 
 			recipients.addAll(add);
 		}
 		else{
 			List<Player> rem = new LinkedList<Player>();
 			for (Player other : recipients){
 				PlayerData otherData = getPlayerData(other.getName());
 				if (!otherData.canHear(data, isAnnounce)) rem.add(other);
 			}
 			if (!rem.isEmpty()) recipients.removeAll(rem);
 		}
 	}
 
 	/**
 	 * Send if online.
 	 * @param playerName
 	 * @param msg
 	 */
 	public static final void tryMessage(final String playerName, String msg) {
 		Player player = Bukkit.getServer().getPlayerExact(playerName);
 		if (player != null) player.sendMessage(msg);
 	}
 
 	public String getAvailableChannel(String name) {
 		if (name == null) return null;
 		String chan = channels.get(name.trim().toLowerCase());
 		if (chan == null){
 			try{
 				int i = Integer.parseInt(name);
 				if (i>=0 && i<channelsOrdered.size()) chan = channelsOrdered.get(i);
 			} catch (NumberFormatException e){		
 			}
 		}
 		if (name.equalsIgnoreCase("global") || (name.equalsIgnoreCase("default") || name.equalsIgnoreCase(ContextManager.defaultChannelName))) return null;
 		else return chan;
 	}
 	
 	public void addToHistory(HistoryElement element){
 		history.add(element);
 		while (history.size() > histSize && !history.isEmpty()) history.remove(0);
 	}
 
 	public String getChannesString() {
 		// TODO: generate on load settings.
 		StringBuilder b = new StringBuilder();
 		int i = -1;
 		for (String n : channelsOrdered){
 			i ++;
 			b.append(n + " ("+i+") | ");
 		}
 		return b.toString();
 	}
 }
