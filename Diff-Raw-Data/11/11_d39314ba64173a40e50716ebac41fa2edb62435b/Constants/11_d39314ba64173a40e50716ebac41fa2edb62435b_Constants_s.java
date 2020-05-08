 package musician101.itembank.lib;
 
 import org.bukkit.ChatColor;
  
 /**
  * List of strings used throughout the plugin.
  * 
  * @author Musician101
  */
 public class Constants 
 {
 	/** Formatting. */
 	public static String PREFIX = ChatColor.DARK_RED + "[ItemBank] ";
 	
 	/** Error messages. */
 	public static final String AIR_BLOCK = PREFIX + "Silly player, you can't use air. :3";
 	public static final String FULL_INV = PREFIX + "Sorry, but you're invenotry is to full to accept any more items.";
 	public static final String NO_PERMISSION = PREFIX + "You do not have permission for this command.";
 	public static final String NOT_ENOUGH_ARGUMENTS = PREFIX + "Error: Not enough arguments.";
 	public static final String PLAYER_COMMAND_ONLY = PREFIX + "Error: This is a player command only.";
 	
 	public static String getAliasError(String alias)
 	{
 		return PREFIX + "Error: " + alias.toUpperCase() + " is not a valid alias.";
 	}
 	
 	public static String getCustomItemWithdrawError(String name)
 	{
 		return PREFIX + "Error getting " + name + ". Please contact an administrator.";
 	}
 	
 	/** Exception messages. */
 	public static final String FILE_NOT_FOUND = "Error: The file does not exist. Please contact an admin.";
 	public static final String IO_EXCEPTION = "An internal server error has occured. Please alert an admin.";
 	public static final String NULL_POINTER = "Error: The ItemTranslator failed to load. Please contact an admin.";
 	public static final String NUMBER_FORMAT = "Error: The amount you entered is not a number.";
 	public static final String YAML_EXCEPTION = "Error: Improper file format. Please check for TABs.";
 	
 	/** ItemBank command */
 	public static final String BASE_CMD = "itembank";
 	
 	/** Admin command */
 	public static final String ADMIN_CMD = "admin";
 	public static final String ADMIN_PERM = BASE_CMD + "." + ADMIN_CMD;
 	public static final String ADMIN_DESC = "Add/Remove blocks/items from a specified account.";
 	
 	/** Deposit command */
 	public static final String DEPOSIT_CMD = "deposit";
 	public static final String DEPOSIT_PERM = BASE_CMD + "." + DEPOSIT_CMD;
 	public static final String DEPOSIT_DESC = "Deposit blocks/items into your account.";
	public static final String[] DEPOSIT_USAGE = {"/deposit [item] <amount>", "/deposit admin [player] [item] <amount>"};
 	public static final String[] DEPOSIT_HELP = {"--------" + ChatColor.DARK_RED + "ItemBank" + ChatColor.WHITE + "--------",
 		"Help for the " + ChatColor.DARK_RED + "Deposit" + ChatColor.WHITE + " command.", "[] are required and <> are optional.",
 		ChatColor.DARK_RED + "Description: " + ChatColor.WHITE + DEPOSIT_DESC, ChatColor.DARK_RED + "Player Usage: " + ChatColor.WHITE + DEPOSIT_USAGE[0],
		ChatColor.DARK_RED + "Admin Usage: " + ChatColor.WHITE + DEPOSIT_USAGE[1]};
 	
 	/** Purge command */
 	public static final String PURGE_CMD = "purge";
 	public static final String PURGE_PERM = BASE_CMD + "." + PURGE_CMD;
 	public static final String PURGE_DESC = "Delete all or a specified account.";
 	public static final String PURGE_USAGE = "/itembank purge <player>";
 	public static final String[] PURGE_HELP = {"--------" + ChatColor.DARK_RED + "ItemBank" + ChatColor.WHITE + "--------",
 		"Help for the " + ChatColor.DARK_RED + "Purge" + ChatColor.WHITE + " command.", "[] are required and <> are optional.",
 		ChatColor.DARK_RED + "Description: " + ChatColor.WHITE + PURGE_DESC, ChatColor.DARK_RED + "Usage: " + ChatColor.WHITE + PURGE_USAGE};
 	
 	/** Withdraw command */
 	public static final String WITHDRAW_CMD = "withdraw";
 	public static final String WITHDRAW_PERM = BASE_CMD + "." + WITHDRAW_CMD;
 	public static final String WITHDRAW_DESC = "Withdraw blocks/items from your account";
	public static final String[] WITHDRAW_USAGE = {"/withdraw [item] <amount>", "/withdraw admin [player] [item] <amount>"};
 	public static final String[] WITHDRAW_HELP = {"--------" + ChatColor.DARK_RED + "ItemBank" + ChatColor.WHITE + "--------",
 		"Help for the " + ChatColor.DARK_RED + "Withdraw" + ChatColor.WHITE + " command.", "[] are required and <> are optional.",
 		ChatColor.DARK_RED + "Description: " + ChatColor.WHITE + WITHDRAW_DESC, ChatColor.DARK_RED + "Player Usage: " + ChatColor.WHITE + WITHDRAW_USAGE[0],
		ChatColor.DARK_RED + "Admin Usage: " + ChatColor.WHITE + WITHDRAW_USAGE[1]};
 	
 	/** Help command */
 	public static final String HELP_CMD = "help";
 	public static final String HELP_DESC = "Gives a list of commands for the plugin, how they're used and what they do.";
 	public static final String[] HELP_LIST = {"--------" + ChatColor.DARK_RED + "ItemBank" + ChatColor.WHITE + "--------",
 		"Type " + ChatColor.DARK_RED + "/itembank help <command> " + ChatColor.WHITE + "for specific info.",
 		"Deposit: " + DEPOSIT_DESC, "Purge: " + PURGE_DESC, "Withdraw: " + WITHDRAW_DESC};
 	
 	/** Other */
 	public static final String CUSTOM_ITEM = "customItem";
 }
