 package tk.tyzoid.plugins.colors;
 
 import java.io.IOException;
 import java.util.HashMap;
 
 import org.bukkit.plugin.PluginDescriptionFile;
 
 import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 import tk.tyzoid.plugins.colors.lib.Metrics;
 
 import tk.tyzoid.plugins.colors.lib.Names;
 import tk.tyzoid.plugins.colors.lib.Perms;
 import tk.tyzoid.plugins.colors.lib.SaveData;
 import tk.tyzoid.plugins.colors.lib.settings;
 import tk.tyzoid.plugins.colors.listeners.colorsPListener;
 
 /**
  * Chat plugin for Bukkit
  * 
  * @author tyzoid
  */
 public class Colors extends JavaPlugin {
 	public String pluginname = "Colors";
 	public char rainbowColor = 'z';
 	
 	public settings colorSettings = new settings();
 	private final colorsPListener playerListener = new colorsPListener(this);
 	private final HashMap<Player, Boolean> colorify = new HashMap<Player, Boolean>();
 	public Perms permissionHandler;
 	public Names PSnames;
 	public SaveData data;
 	
 	public void onDisable() {
 		System.out.println("[" + pluginname + "] " + pluginname + " is closing...");
 		PSnames.pluginClosing(true);
 		data.pluginClosing(true);
 	}
 	
 	public void onEnable() {
 		try {
 		    Metrics metrics = new Metrics(this);
 		    metrics.start();
 		} catch (IOException e) {
 		    // Failed to submit the stats :(
 		}
 		PluginManager pm = getServer().getPluginManager();
 		
 		pm.registerEvents(playerListener, this);
 		
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println("[" + pluginname + "] Starting " + pluginname + " v" + pdfFile.getVersion() + "...");
 		
 		setupPermissions();
 		
 		PSnames = new Names(this);
 		PSnames.loadNames();
 		
 		data = new SaveData(this);
 		data.loadData();
 		
 		colorSettings.readSettings();
 		playerListener.plugin_init();

		if(this.getServer().getOnlinePlayers().length > 0){
			System.out.println("[" + pluginname + "] " + pluginname + " is reloading...");
			Player[] players = this.getServer().getOnlinePlayers();
			for(Player player : players){
				playerListener.onPlayerJoin(new PlayerJoinEvent(player, ""));
			}
		}
 	}
 	
 	private void setupPermissions() {
 		permissionHandler = new Perms(this);
 	}
 	
 	public boolean hasPermission(Player p, String node, boolean defaultValue) {
 		return permissionHandler.hasPermission(p, node, defaultValue);
 	}
 	
 	public boolean isChatColoring(final Player player) {
 		if (colorify.containsKey(player)) {
 			return colorify.get(player);
 		} else {
 			return false;
 		}
 	}
 	
 	public void toggleChatColoring(final Player player) {
 		boolean value = !isChatColoring(player);
 		if (value) {
 			player.sendMessage("§1C§2o§3l§4o§5r§6s §fare now enabled");
 		} else {
 			player.sendMessage("§1C§2o§3l§4o§5r§6s §fare now disabled");
 		}
 		colorify.put(player, value);
 	}
 	
 	public String colorsChat(String message){
 		return colorsChat(message, new boolean[]{false, false, false, false, false})[0];
 	}
 	
 	public String[] colorsChat(String message, boolean[] format) {
 		char[] charMessage = message.toCharArray();
 		String finalMessage = "";
 		// k (scramble), l (bold), m (strike-through), n(underline), o (italic)
 		int color = 1;
 		for (int i = 0; i < charMessage.length; i++) {
 			if ((i + 1) < charMessage.length && charMessage[i] == '§' && isColorNumber(charMessage[i + 1])) {
 				i++;
 				continue;
 			}
 			if ((i + 1) < charMessage.length && isColorChar(charMessage[i]) && isFormattingChar(charMessage[i + 1])) {
 				String tmp = Character.toString(Character.toUpperCase(charMessage[i+1]));
 				int index = formattingChars.valueOf(tmp).ordinal();
 				format[index] = true;
 				i++;
 				continue;
 			}
 			finalMessage += "§" + Integer.toHexString(color);
 			if (format[0]) {
 				finalMessage += "§k";
 			}
 			if (format[1]) {
 				finalMessage += "§l";
 			}
 			if (format[2]) {
 				finalMessage += "§m";
 			}
 			if (format[3]) {
 				finalMessage += "§n";
 			}
 			if (format[4]) {
 				finalMessage += "§o";
 			}
 			finalMessage += charMessage[i];
 			color++;
 			if (color >= 16) {
 				color = 1;
 			}
 		}
 		
 		return new String[]{
 				finalMessage, 
 				(format[0]?"0":"1"),
 				(format[1]?"0":"1"),
 				(format[2]?"0":"1"),
 				(format[3]?"0":"1"),
 				(format[4]?"0":"1")
 		};
 	}
 	
 	public String convertToColor(String withoutColor, boolean rainbowAllowed) {
 		boolean[] format = {false, false, false, false, false};
 		int count = withoutColor.length();
 		char[] colorless = withoutColor.toCharArray();
 		char rcc = Character.toLowerCase(rainbowColor); // rainbow color char
 		String withColor = "";
 		for (int i = 0; i < count; i++) {
 			if ((i + 1) < count && isColorChar(colorless[i]) && isFormattingChar(colorless[i + 1])) {
 				String tmp = Character.toString(Character.toUpperCase(colorless[i+1]));
 				int index = formattingChars.valueOf(tmp).ordinal();
 				if(index < 5)
 					format[index] = true;
 				else
 					format = new boolean[]{false, false, false, false, false};
 			}
 			if ((isColorChar(colorless[i])) && (i + 1) < count) {
 				if (isColorNumber(colorless[i + 1]) || isFormattingChar(colorless[i + 1])) {
 					withColor += "§";
 				} else if (rainbowAllowed && Character.toLowerCase(colorless[i + 1]) == rcc) {
 					boolean found = false;
 					int indexOfColorChar = i + 2;
 					String rainbowString = new String(colorless);
 					
 					while (indexOfColorChar < count && !found) {
 						found = (isColorChar(colorless[indexOfColorChar]) &&
 								(isColorNumber(colorless[indexOfColorChar + 1]) ||
 										Character.toLowerCase(colorless[indexOfColorChar + 1]) == rcc ||
 										Character.toLowerCase(colorless[indexOfColorChar + 1]) == 'r'));
 						indexOfColorChar++;
 					}
 					
 					if (found) {
 						indexOfColorChar--;
 					}
 					String[] result = colorsChat(rainbowString.substring(i + 2, indexOfColorChar), format);
 					rainbowString = result[0];
 					for(int j = 1; j < result.length; j++){
 						format[j-1] = result[j].equalsIgnoreCase("1");
 					}
 					withColor += rainbowString;
 					
 					i = indexOfColorChar - 1;
 					
 				} else {
 					withColor += colorless[i];
 				}
 			} else {
 				withColor += colorless[i];
 			}
 		}
 		return withColor;
 	}
 	
 	public boolean isColorChar(char c) {
 		String[] chars = colorSettings.getProperty("color-chars").split(",");
 		boolean charUsed = false;
 		for (int i = 0; (i < chars.length && !charUsed); i++) {
 			if (c == chars[i].toCharArray()[0]) {
 				charUsed = true;
 			}
 		}
 		return charUsed;
 	}
 	
 	public boolean isColorNumber(char c) {
 		c = Character.toLowerCase(c);
 		return ((c == '0') || (c == '1') || (c == '2') || (c == '3') || (c == '4') || (c == '5') || (c == '6') || (c == '7') || (c == '8') || (c == '9') || (c == 'a') || (c == 'b') || (c == 'c') || (c == 'd') || (c == 'e') || (c == 'f'));
 	}
 	
 	public boolean isFormattingChar(char c) {
 		c = Character.toLowerCase(c);
 		return ((c == 'k') || (c == 'l') || (c == 'm') || (c == 'n') || (c == 'o') || (c == 'r'));
 	}
 	
 	private enum formattingChars {
 		K, L, M, N, O, R
 	}
 }
