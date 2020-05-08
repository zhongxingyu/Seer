 package btwmod.chat;
 
 import java.util.ArrayDeque;
 import java.util.ArrayList;
 import java.util.Deque;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.Packet3Chat;
 
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.PlayerAPI;
 import btwmods.Util;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerChatListener;
 import btwmods.player.IPlayerInstanceListener;
 import btwmods.player.PlayerChatEvent;
 import btwmods.player.PlayerInstanceEvent;
 import btwmods.util.CaselessKey;
 import btwmods.util.ValuePair;
 
 public class mod_Chat implements IMod, IPlayerChatListener, IPlayerInstanceListener {
 
 	public String globalMessageFormat = "<%1$s> %2$s";
 	public String emoteMessageFormat = "* %1$s %2$s";
 	public Set<String> bannedColors = new HashSet<String>();
 	public Set<String> bannedUsers = new HashSet<String>();
 	
 	private Settings data = null;
 	private Map<String, String> colorLookup = new HashMap<String, String>();
 	private CommandChatColor commandChatColor;
 	
 	public int chatRestoreLines = 30;
 	private long chatRestoreTimeout = 20L;
 	private Deque<ValuePair<String, Long>> chatRestoreBuffer = new ArrayDeque<ValuePair<String, Long>>();
 	private Map<String, Long> loginTime = new HashMap<String, Long>();
 	private Map<String, Long> logoutTime = new HashMap<String, Long>();
 	
 	private CommandChatAlias commandChatAlias;
 	
 	private CommandIgnore commandIgnore;
 	private CommandUnignore commandUnignore;
 	public static final String IGNORE_PREFIX = "ignore_";
 	public int defaultIgnoreMinutes = 30;
 	public int maxIgnoreMinutes = 120;
 	
 	@Override
 	public String getName() {
 		return "Chat";
 	}
 
 	@Override
 	public void init(Settings settings, Settings data) throws Exception {
 		this.data = data;
 
 		String bannedUsers = settings.get("bannedUsers");
 		if (bannedUsers != null)
 			for (String bannedUser : bannedUsers.split("[,; ]+"))
 				this.bannedUsers.add(bannedUser.toLowerCase().trim());
 		
 		String bannedColors = settings.get("bannedColors");
 		if (bannedColors != null)
 			for (String bannedColor : bannedColors.split("[,; ]+"))
 				this.bannedColors.add(bannedColor.toLowerCase().trim());
 		
 		chatRestoreLines = settings.getInt("chatRestoreLines", chatRestoreLines);
 		chatRestoreTimeout = settings.getLong("chatRestoreTimeout", chatRestoreTimeout);
 		defaultIgnoreMinutes = settings.getInt("defaultIgnoreMinutes", defaultIgnoreMinutes);
 		maxIgnoreMinutes = settings.getInt("maxIgnoreMinutes", maxIgnoreMinutes);
 		
 		//addColor("black", Util.COLOR_BLACK);
 		//addColor("navy", Util.COLOR_NAVY);
 		addColor("green", Util.COLOR_GREEN);
 		addColor("teal", Util.COLOR_TEAL);
 		addColor("maroon", Util.COLOR_MAROON);
 		addColor("purple", Util.COLOR_PURPLE);
 		addColor("gold", Util.COLOR_GOLD);
 		addColor("silver", Util.COLOR_SILVER);
 		addColor("grey", Util.COLOR_GREY);
 		addColor("blue", Util.COLOR_BLUE);
 		addColor("lime", Util.COLOR_LIME);
 		addColor("aqua", Util.COLOR_AQUA);
 		addColor("red", Util.COLOR_RED);
 		addColor("pink", Util.COLOR_PINK);
 		addColor("yellow", Util.COLOR_YELLOW);
 		addColor("white", Util.COLOR_WHITE);
 		
 		PlayerAPI.addListener(this);
 		CommandsAPI.registerCommand(commandChatColor = new CommandChatColor(this), this);
 		CommandsAPI.registerCommand(commandChatAlias = new CommandChatAlias(this), this);
 		CommandsAPI.registerCommand(commandIgnore = new CommandIgnore(this), this);
 		CommandsAPI.registerCommand(commandUnignore = new CommandUnignore(this), this);
 	}
 	
 	private void addColor(String color, String colorCode) {
 		if (!bannedColors.contains(color)) {
 			colorLookup.put(color, colorCode);
 		}
 	}
 
 	@Override
 	public void unload() throws Exception {
 		PlayerAPI.removeListener(this);
 		CommandsAPI.unregisterCommand(commandChatColor);
 		CommandsAPI.unregisterCommand(commandChatAlias);
 		CommandsAPI.unregisterCommand(commandIgnore);
 		CommandsAPI.unregisterCommand(commandUnignore);
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 	
 	public boolean setPlayerColor(String username, String color) {
 		if (username == null | color == null) {
 			return false;
 		}
 		else if (color.equalsIgnoreCase("off") || color.equalsIgnoreCase("white") || isBannedUser(username)) {
 			if (data.removeKey(username.toLowerCase(), "color")) {
 				data.saveSettings(this);
 				return true;
 			}
 		}
 		else if (isValidColor(color)) {
 			data.set(username.toLowerCase(), "color", color.toLowerCase());
 			data.saveSettings(this);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public String getPlayerColor(String username) {
 		return username == null ? null : data.get(username.toLowerCase(), "color");
 	}
 	
 	/**
 	 * Get the vMC color code for a named color.
 	 * 
 	 * @param color The named color.
 	 * @return The two characters that represent this color in vMC.
 	 */
 	public String getColorChar(String color) {
 		return colorLookup.get(color.toLowerCase());
 	}
 	
 	public boolean isValidColor(String color) {
 		return color.equalsIgnoreCase("off") || colorLookup.containsKey(color.toLowerCase());
 	}
 
 	public String[] getColors() {
 		return colorLookup.keySet().toArray(new String[colorLookup.size()]);
 	}
 	
 	public boolean isBannedUser(String username) {
 		return bannedUsers.contains(username.toLowerCase().trim());
 	}
 	
 	public String getAlias(String username) {
 		return username == null ? null : data.get(username.toLowerCase().trim(), "alias");
 	}
 	
 	public boolean setAlias(String username, String alias) {
 		alias = alias.trim();
 		
 		if (alias.length() < 1 || alias.length() > 16)
 			return false;
 		
 		MinecraftServer.getServer().logger.info("Set alias for " + username + " to " + alias);
 		data.set(username.toLowerCase().trim(), "alias", alias);
 		data.saveSettings(this);
 		return true;
 	}
 	
 	public boolean removeAlias(String username) {
 		if (data.removeKey(username.toLowerCase().trim(), "alias")) {
 			MinecraftServer.getServer().logger.info("Removed alias for " + username);
 			data.saveSettings(this);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public boolean hasAlias(String username) {
 		return getAlias(username) != null;
 	}
 	
 	public boolean addIgnore(String username, String ignoredUsername, long minutes) {
 		if (minutes <= 0)
 			return false;
 		
		data.setLong(username.toLowerCase().trim(), IGNORE_PREFIX + ignoredUsername.toLowerCase().trim(), System.currentTimeMillis() + (minutes * 60 * 1000));
 		data.saveSettings(this);
 		return true;
 	}
 	
 	public boolean isIgnoring(String username, String ignoredUsername) {
 		long time = getIgnoreTime(username, ignoredUsername);
 		return time > 0 && time > System.currentTimeMillis();
 	}
 	
 	public long getIgnoreTime(String username, String ignoredUsername) {
 		return data.getLong(username.toLowerCase().trim(), IGNORE_PREFIX + ignoredUsername.toLowerCase().trim(), -1L);
 	}
 	
 	public boolean removeIgnore(String username, String ignoredUsername) {
 		if (data.removeKey(username.toLowerCase().trim(), IGNORE_PREFIX + ignoredUsername.toLowerCase().trim())) {
 			data.saveSettings(this);
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public List<String> getIgnores(String username) {
 		Set<CaselessKey> keys = data.getSectionKeys(username.toLowerCase().trim());
 		ArrayList<String> ignoredUsers = new ArrayList<String>();
 		
 		if (keys != null) {
 			for (CaselessKey key : keys) {
 				if (key.key.startsWith(IGNORE_PREFIX)) {
 					String ignored = key.key.substring(IGNORE_PREFIX.length());
 					if (isIgnoring(username, ignored))
 						ignoredUsers.add(ignored);
 				}
 			}
 		}
 		
 		return ignoredUsers;
 	}
 	
 	public void sendIgnoreList(EntityPlayer player, boolean showWhenEmpty) {
 		List<String> ignored = getIgnores(player.username);
 		if (ignored.size() > 0) {
 			String header = Util.COLOR_YELLOW + "You are ignoring: " + Util.COLOR_WHITE;
 			
 			List<String> messages = Util.combineIntoMaxLengthMessages(ignored, Packet3Chat.maxChatLength, ", ", true);
 			
 			if (messages.size() == 1 && messages.get(0).length() + header.length() <= Packet3Chat.maxChatLength) {
 				player.sendChatToPlayer(header + messages.get(0));
 			}
 			else {
 				player.sendChatToPlayer(header);
 				for (String message : messages) {
 					player.sendChatToPlayer(message);
 				}
 			}
 		}
 		else if (showWhenEmpty) {
 			player.sendChatToPlayer(Util.COLOR_YELLOW + "You are not ignoring any players.");
 		}
 	}
 
 	@Override
 	public void onPlayerChatAction(PlayerChatEvent event) {
 		if (event.type == PlayerChatEvent.TYPE.HANDLE_GLOBAL || event.type == PlayerChatEvent.TYPE.HANDLE_EMOTE) {
 			
 			String username = getAlias(event.player.username);
 			if (username == null)
 				username = event.player.username;
 			
 			// Attempt to get the user's setting.
 			String color = data.get(event.player.username.toLowerCase(), "color");
 			
 			if (color != null)
 				color = getColorChar(color);
 			
 			event.setMessage(String.format(event.type == PlayerChatEvent.TYPE.HANDLE_GLOBAL ? globalMessageFormat : emoteMessageFormat,
 				color == null
 					? username
 					: color + username + Util.COLOR_WHITE,
 				event.getMessage()
 			));
 			
 			event.sendAsGlobalMessage();
 		}
 		else if (event.type == PlayerChatEvent.TYPE.GLOBAL) {
 			chatRestoreBuffer.add(new ValuePair(event.originalMessage, new Long(System.currentTimeMillis())));
 			
 			if (chatRestoreBuffer.size() > chatRestoreLines)
 				chatRestoreBuffer.pollFirst();
 		}
 		else if (event.type == PlayerChatEvent.TYPE.SEND_TO_PLAYER_ATTEMPT) {
 			if (isIgnoring(event.getTargetPlayer().username, event.player.username)) {
 				event.markNotAllowed();
 			}
 		}
 	}
 
 	@Override
 	public void onPlayerInstanceAction(PlayerInstanceEvent event) {
 		long currentTimeMillis = System.currentTimeMillis();
 		
 		if (event.getType() == PlayerInstanceEvent.TYPE.LOGIN) {
 			String userKey = event.getPlayerInstance().username.toLowerCase();
 			
 			Long loginSessionStart = loginTime.get(userKey);
 			Long lastLogout = logoutTime.get(userKey);
 			
 			// Reset the login time if they've been logged out longer than the restore timeout.
 			if (loginTime == null || lastLogout == null || lastLogout.longValue() < currentTimeMillis - chatRestoreTimeout * 1000L) {
 				loginTime.put(userKey, loginSessionStart = new Long(currentTimeMillis));
 			}
 			else {
 				for (ValuePair<String, Long> message : chatRestoreBuffer) {
 					if (message.value.longValue() >= loginSessionStart)
 						event.getPlayerInstance().sendChatToPlayer(message.key);
 				}
 			}
 			
 			sendIgnoreList(event.getPlayerInstance(), false);
 				
 		}
 		else if (event.getType() == PlayerInstanceEvent.TYPE.LOGOUT) {
 			logoutTime.put(event.getPlayerInstance().username.toLowerCase(), new Long(System.currentTimeMillis()));
 		}
 	}
 }
