 package no.runsafe.nchat;
 
 import org.bukkit.ChatColor;
 
 public class Constants
 {
 	public static String COLOR_CHARACTER = "&%s";
 
 	public static String FORMAT_PLAYER_NAME = "#player";
 	public static String FORMAT_WORLD = "#world";
 	public static String FORMAT_GROUP = "#group";
 	public static String FORMAT_TAG = "#tags";
 	public static String FORMAT_MESSAGE = "#message";
 	public static String FORMAT_OP = "#op";
 	public static String FORMAT_CHANNEL = "#channel";
 
 	public static ChatColor DEFAULT_MESSAGE_COLOR = ChatColor.AQUA;
 
 	// TODO: Remove this shit.
 	public static String CHAT_CHANNEL_NODE = "nChat.channel.%s";
 
 	public static String CHANNEL_NOT_EXIST = "The specified channel does not exist.";
 	public static String CHANNEL_NO_PERMISSION = "You do not have permission to speak in this channel.";
 
 	public static String CHAT_MUTED = "You cannot broadcast messages right now.";
 
 	public static String COMMAND_CHAT_MUTED = "Global chat has now been muted.";
 	public static String COMMAND_CHAT_UNMUTED = "Global chat has now been unmuted";
 
 	public static String COMMAND_ENTER_PLAYER = "Please enter a player name";
 	public static String COMMAND_NO_PERMISSION = ChatColor.RED + "You do not have permission to do that.";
 	public static String COMMAND_TARGET_EXEMPT = ChatColor.RED + "You cannot use that on the specified player.";
 	public static String COMMAND_TARGET_NO_EXISTS = ChatColor.RED + "That player does not exist.";
 
	public static String WHISPER_NO_TARGET = ChatColor.RED + "The player %s " + ChatColor.RED + " does not exist.";
	public static String WHISPER_TARGET_OFFLINE = ChatColor.RED + "The player %s " + ChatColor.RED + " is currently offline.";
 	public static String WHISPER_NO_REPLY_TARGET = ChatColor.RED + "You have nothing to reply to.";
 }
