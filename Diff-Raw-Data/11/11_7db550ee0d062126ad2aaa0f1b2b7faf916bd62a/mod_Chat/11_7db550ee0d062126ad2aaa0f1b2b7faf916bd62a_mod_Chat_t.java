 package btwmod.chat;
 
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import btwmods.CommandsAPI;
 import btwmods.IMod;
 import btwmods.PlayerAPI;
 import btwmods.Util;
 import btwmods.io.Settings;
 import btwmods.player.IPlayerChatListener;
 import btwmods.player.PlayerChatEvent;
 
 public class mod_Chat implements IMod, IPlayerChatListener {
 
 	public String globalMessageFormat = "<%1$s> %2$s";
 	public Set<String> bannedColors = new HashSet<String>();
 	public Set<String> bannedUsers = new HashSet<String>();
 	
 	private Settings data = null;
 	private Map<String, String> colorLookup = new HashMap<String, String>();
 	private CommandChatColor commandChatColor;
 	
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
 	}
 
 	@Override
 	public IMod getMod() {
 		return this;
 	}
 	
 	public boolean setPlayerColor(String username, String color) throws IOException {
 		if (color.equalsIgnoreCase("off") || color.equalsIgnoreCase("white") || isBannedUser(username)) {
 			data.removeKey(username.toLowerCase(), "color");
 			return true;
 		}
 		else if (isValidColor(color)) {
 			data.set(username.toLowerCase(), "color", color.toLowerCase());
 			data.saveSettings();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	public String getPlayerColor(String username) {
 		return data.get(username.toLowerCase(), "color");
 	}
 	
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
 
 	@Override
 	public void onPlayerChatAction(PlayerChatEvent event) {
 		if (event.type == PlayerChatEvent.TYPE.GLOBAL) {
 			// Attempt to get the user's setting.
 			String color = data.get(event.player.username.toLowerCase(), "color");
 			
 			// Convert the setting to a color.
 			if (color != null)
 				color = getColorChar(color);
 			
 			event.setMessage(String.format(globalMessageFormat,
 				color == null
 					? event.player.username
 					: color + event.player.username + Util.COLOR_WHITE,
 				event.getMessage()
 			));
 			
 			event.sendAsGlobalMessage();
 		}
 	}
 }
