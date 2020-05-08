 package uk.badger.bConomy;
 
 import java.text.DecimalFormat;
 
 import n3wton.me.BukkitDatabaseManager.Database.BukkitDatabase;
 import net.milkbowl.vault.permission.Permission;
 
 import org.bukkit.ChatColor;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.Server;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.badger.bConomy.account.Account;
 import uk.badger.bConomy.account.PlayerAccounts;
 import uk.badger.bConomy.config.Config;
 
 /**
  * The Class Global.
  */
 public class Global {
 	
 	/** The m_plugin. */
 	private static JavaPlugin m_plugin = null;
 	
 	/** The m_accounts. */
 	private static PlayerAccounts m_accounts = null;
 	
 	/** The m_database. */
 	public static BukkitDatabase m_database = null;
 	
 	/** The permission. */
 	private static Permission m_permission = null;
 
 	/**
 	 * get the JavaPlugin instance.
 	 *
 	 * @return the plugin instance
 	 */
 	public static JavaPlugin getPlugin() {
 		return m_plugin;
 	}
 	
 	/**
 	 * Set the plugin instance.
 	 *
 	 * @param plugin - the plugin instance to use
 	 */
 	public static void setPlugin(JavaPlugin plugin) {
 		m_plugin = plugin;
 		m_accounts = new PlayerAccounts();
 	}
 	
 	/**
 	 * Output a message to console.
 	 *
 	 * @param message to output
 	 */
 	public static void outputToConsole(String message) {
 		
 		m_plugin.getLogger().info(message);		
 	}
 
 	/**
 	 * Get the accounts array.
 	 *
 	 * @return the accounts array
 	 */
 	public static PlayerAccounts getAccounts() {
 		return m_accounts;
 	}
 
 	/**
 	 * Set the accounts array.
 	 *
 	 * @param m_accounts - the array to set it to
 	 */
 	public static void setAccounts(PlayerAccounts m_accounts) {
 		Global.m_accounts = m_accounts;
 	}
 
 	/**
 	 * Add a account to the array.
 	 *
 	 * @param account to add
 	 */
 	public static void addAccout(Account account) {
 		m_accounts.add(account);	
 	}
 	
 	/**
 	 * Format a number to a usable string.
 	 *
 	 * @param amount the amount
 	 * @return the string
 	 */
 	public static String format(double amount) {
 		DecimalFormat format = new DecimalFormat(Config.m_currency.format);
 		String formatted = format.format(amount);
 		
 		if (formatted.endsWith("."))
 			formatted = formatted.substring(0, formatted.length() - 1);
 		
 		return formatted;
 	}
 	
 	/**
 	 * Gets the server.
 	 *
 	 * @return the server
 	 */
 	public static Server getServer() {
 		return m_plugin.getServer();
 	}
 
 	/**
 	 * Output a formatted message to a sender (console or player).
 	 *
 	 * @param sender the sender
 	 * @param message the message
 	 */
 	public static void output(CommandSender sender, String message) {
 		sender.sendMessage(ChatColor.GOLD + "[bConomy] " + ChatColor.WHITE + message);
 	}
 	
 	/**
 	 * Output.
 	 *
 	 * @param player the player
 	 * @param message the message
 	 */
 	public static void output(OfflinePlayer player, String message) {
 		if (player.isOnline()) {
 			player.getPlayer().sendMessage(ChatColor.GOLD + "[bConomy] " + ChatColor.WHITE + message);
 		}
 	}
 	
 	
 	/**
 	 * Checks for permission.
 	 *
 	 * @param sender the sender
 	 * @param perm the perm
 	 * @param verbose the verbose
 	 * @return true, if successful
 	 */
 	public static boolean hasPermission(CommandSender sender, String perm, boolean verbose) {
 		
 		// Are we console?
 		if (!(sender instanceof Player))
 			return true;
 		
 		// Are we an op?
 		Player player = (Player)sender;
 		if (player.isOp() == true)
 			return true;
 		
 		// use vault to check perms
 		if (m_permission.has(sender, perm)){
 			return true;
 		}
 		
 		// by now they don't have perms so see if we are outputting a message
 		if (verbose) {
			Global.output(sender, "You do not have the required permissions - " + perm);
 		}
 		
 		return false;		
 	}	
 	
 	/**
 	 * setup the vault permissions
 	 * 
 	 * @return whether it was successful
 	 */
 	public static boolean setupPermissions() {
 		if (getServer().getPluginManager().getPlugin("Vault") == null)
 			return false;
 		
         RegisteredServiceProvider<Permission> rsp = getServer().getServicesManager().getRegistration(Permission.class);
         m_permission = rsp.getProvider();
         return m_permission != null;
     }
 	
 }
