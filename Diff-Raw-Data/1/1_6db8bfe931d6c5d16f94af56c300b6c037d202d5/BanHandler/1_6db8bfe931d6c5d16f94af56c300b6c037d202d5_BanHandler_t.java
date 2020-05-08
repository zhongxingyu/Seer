 package com.cole2sworld.ColeBans.handlers;
 
 import java.util.HashMap;
 import java.util.Vector;
 
 import org.bukkit.ChatColor;
 
 import com.cole2sworld.ColeBans.GlobalConf;
 import com.cole2sworld.ColeBans.framework.PlayerAlreadyBannedException;
 import com.cole2sworld.ColeBans.framework.PlayerNotBannedException;
 /**
  * Handles banning and unbanning of players.
  */
 public abstract class BanHandler {
 	/**
 	 * Types of bans
 	 *
 	 */
 	public static enum Type {
 		/**
 		 * Represents a temporary ban.
 		 */
 		TEMPORARY,
 		/**
 		 * Represents a permanent ban.
 		 */
 		PERMANENT,
 		/**
 		 * Represents a player not being banned.
 		 */
 		NOT_BANNED
 	};
 	/**
 	 * The preferred name for the admin name when an action is initiated without player intervention
 	 */
 	public static final String SYSTEM_ADMIN_NAME = "[System]";
 	/**
 	 * Do stuff related to getting ready, and then return a new instance of the BanHandler.
 	 * @param data EnableData that contains SQL credentials, files for yaml/json, etc.
 	 * @return
 	 */
 	public abstract BanHandler onEnable(HashMap<String, String> data);
 	/**
 	 * Permanently bans a player.
 	 * @param player The player to ban.
 	 * @param reason The ban reason
 	 * @param admin The admin that initiated this action
 	 * @throws PlayerAlreadyBannedException if the player is already banned
 	 */
 	public abstract void banPlayer(String player, String reason, String admin) throws PlayerAlreadyBannedException;
 	/**
 	 * Temporarily bans a player.
 	 * @param player The player to ban
 	 * @param time Amount of time to stay banned, in minutes.
 	 * @param admin The admin that initiated this action
 	 * @throws PlayerAlreadyBannedException if the player is already banned
 	 * @throws MethodNotSupportedException if temp bans are disabled
 	 */
 	public abstract void tempBanPlayer(String player, long time, String admin) throws PlayerAlreadyBannedException, UnsupportedOperationException;
 	/**
 	 * Unbans a player, whether they have been temp banned or perm banned
 	 * @param player The player to unban
 	 * @param admin The admin that initiated this action
 	 * @throws PlayerNotBannedException If the player is not banned
 	 */
 	public abstract void unbanPlayer(String player, String admin) throws PlayerNotBannedException;
 	/**
 	 * Gets whether a player is banned.
 	 * @param player The player to check
 	 * @param admin The admin that initiated this action
 	 * @return If the player is banned (whether it be temp or permanent)
 	 */
 	public abstract boolean isPlayerBanned(String player, String admin);
 	/**
 	 * @param player The name of the player to check
 	 * @param admin The admin that initiated this action
 	 * @return BanData object containing details about the ban.
 	 */
 	public abstract BanData getBanData(String player, String admin);
 	/**
 	 * Gets the formatted ban reason for "reason"
 	 * @param banReason The reason for the ban
 	 * @param banType The type of ban
 	 * @param tempBanTime The amount of time in minutes until the ban expires
 	 * @return The fancy formatted ban reason, with colors and everything
 	 */
 	public static String getFormattedBanReason(String banReason, Type banType, Long tempBanTime) {
 		if (banType == Type.PERMANENT) {
 			return ChatColor.valueOf(GlobalConf.banColor)+GlobalConf.banMessage.replace("%reason", banReason).replace("%time", "infinite");
 		}
 		else if (banType == Type.TEMPORARY) {
 			return ChatColor.valueOf(GlobalConf.tempBanColor)+GlobalConf.tempBanMessage.replace("%reason", "Temporary Ban").replace("%time", tempBanTime.toString());
 		}
 		return "";
 	}
 	/**
 	 * Do stuff needed when disabling. Closing SQL connections, flushing caches, etc.
 	 */
 	public abstract void onDisable();
 	/**
 	 * Convert over to a new ban handler.
 	 * @param handler The handler to dump data into.
 	 */
 	public abstract void convert(BanHandler handler);
 	/**
 	 * Does a full dump of the data for this ban handler.
 	 */
 	public abstract Vector<BanData> dump(String admin);
 	/**
 	 * Gets a simple list of the banned players, with no reasons.
 	 */
 	public abstract Vector<String> listBannedPlayers(String admin);
 }
