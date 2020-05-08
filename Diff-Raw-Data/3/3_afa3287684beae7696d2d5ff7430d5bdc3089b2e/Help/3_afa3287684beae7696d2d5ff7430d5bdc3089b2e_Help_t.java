 package qs.swornshop;
 
 import java.util.HashMap;
 
 import org.bukkit.command.CommandSender;
 
 /**
  * Help stores all help-related information, such as help for each
  * command and context-sensitive index topics.
  */
 public class Help {
 	
 	public static final CommandHelp help = new CommandHelp("shop help", "h", "[action]", "show help with shops",
 			CommandHelp.arg("action", "get help on a /shop action, e.g. /shop h create"));
 	public static final CommandHelp create = new CommandHelp("shop create", "mk", "<owner> [inf=no]", "create a new shop", 
 			CommandHelp.args(
 				"owner", "the owner of the shop",
 				"inf", "whether the shop is infinite"));
 	public static final CommandHelp delete = new CommandHelp("shop delete", "del", null, "removes this shop");
 	public static final CommandHelp save = new CommandHelp("shop save", null, null, "saves all shops");
 	public static final CommandHelp backup = new CommandHelp("shop backup", null, null, "backs up shops");
 	
 	public static final CommandHelp notifications = new CommandHelp("shop notifications", "n,pending,p", null, "view notifications", 
 			"Shows a list of notifications to sell items to your shops",
 			"These can be offers (e.g., someone wishes to sell you an item)",
 			"or messages (e.g., an offer was accepted).",
 			"Use /shop accept and /shop reject on offers.");
 	
 	public static final CommandHelp accept = new CommandHelp("shop accept", "yes,a", null, "accept your most recent notification");
 	public static final CommandHelp claim = new CommandHelp("shop claim", "c", null, "claim your most recent notification");
 	public static final CommandHelp reject = new CommandHelp("shop reject", "no", null, "reject your most recent notification");
 	public static final CommandHelp skip = new CommandHelp("shop skip", "sk", null, "skip your most recent notification",
 			"Moves your most recent notification to the end of the list");
 	
 	public static final CommandHelp buy = new CommandHelp("shop buy", "b", "[item] <amount>", "buy an item from this shop", 
 			CommandHelp.args(
 				"item", "the name of the item or an entry number in the shop.    §LNote:§R enchanted items must be bought with an entry number",
 				"quantity", "the quantity you wish to buy"
 			));
 	public static final CommandHelp sell = new CommandHelp("shop sell", "s", "<item> <quantity> [price=auto]", "request to sell an item to this shop",
 			CommandHelp.args(
 				"item", "the name of the item",
 				"quantity", "the quantity you wish to sell",
 				"price", "the price (for the entire quantity); defaults to the store's price times the quantity"
 			));
 	
 	public static final CommandHelp add = new CommandHelp("shop add", "+,ad", "<$buy> [$sell=no]", "add held item to this shop",
 			concat(CommandHelp.args(
 				"buy-price", "the price of a single item in the stack",
 				"sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
 			), new String[] {
 				"§BWarning:§F Once you add an item to a shop, you cannot remove it."
 			}));
	public static final CommandHelp remove = new CommandHelp("shop remove", "rm", "<item>", "remove an item and get 2/3 back",
			CommandHelp.arg("item", "the name or entry number of the item"));
 	public static final CommandHelp restock = new CommandHelp("shop restock", "r", null, "restock this shop with your held item");
 	public static final CommandHelp set = new CommandHelp("shop set", null, "<item> <$buy> <$sell>", "change an item's price",
 			CommandHelp.args(
 				"item", "the ID or name of the item to modify",
 				"buy-price", "the new price of a single item in the stack",
 				"sell-price", "the selling price of a single item in the stack (by default the item cannot be sold)"
 			));
 	public static final CommandHelp sign = new CommandHelp("shop sign", null, "<line1>|<line2>…", "changes a shop's sign",
 			CommandHelp.arg("text", "the new text of the sign, separated by |'s"));
 	
 	public static final CommandHelp lookup = new CommandHelp("shop lookup", null, "<item-name>", "look up an item's ID and damage value",
 			CommandHelp.arg("item-name", "the name of an alias for an item"));
 	
 	/**
 	 * The general index of commands
 	 */
 	public static final String[] index = {
 		CommandHelp.header("Shop Help"),
 		help.toIndexString(),
 		notifications.toIndexString(),
 		accept.toIndexString(),
 		reject.toIndexString(),
 		claim.toIndexString(),
 		skip.toIndexString()
 	};
 	/**
 	 * An index of commands only usable when a shop is selected
 	 */
 	public static final String[] indexSelected = {
 		"§ELeft- and right-click to browse this shop's items"
 	};
 	/**
 	 * An index of commands only usable by an admin (a player 
 	 * with the swornshop.admin permission)
 	 */
 	public static final String[] indexAdmin = {
 		create.toIndexString(),
 		save.toIndexString(),
 		backup.toIndexString()
 	};
 	/**
 	 * An index of commands only usable by an admin (a player
 	 * with the swornshop.admin permission) who has selected
 	 * a shop
 	 */
 	public static final String[] indexSelectedAdmin = {
 		delete.toIndexString()
 	};
 	/**
 	 * An index of commands only usable by a player who has
 	 * selected a shop which he/she does not own
 	 */
 	public static final String[] indexNotOwner = {
 		buy.toIndexString(),
 		sell.toIndexString()
 	};
 	/**
 	 * An index of commands only usable by a player who has
 	 * selected a shop which he/she owns
 	 */
 	public static final String[] indexOwner = {
 		add.toIndexString(),
 		restock.toIndexString(),
 		set.toIndexString(),
 		remove.toIndexString(),
 		sign.toIndexString()
 	};
 
 	/**
 	 * Shows generic shop help to a player.
 	 * @param sender the player
 	 */
 	public static void showHelp(CommandSender sender) {
 		sender.sendMessage(Help.index);
 	}
 	
 	/**
 	 * Shows context-sensitive help to a player based on that player's selection.
 	 * @param sender the player
 	 * @param selection the player's shop selection, or null if the player has no selection
 	 */
 	public static void showHelp(CommandSender sender, ShopSelection selection) {
 		sender.sendMessage(Help.index);
 		if (sender.hasPermission("shops.admin"))
 			sender.sendMessage(Help.indexAdmin);
 		if (selection != null) {
 			if (sender.hasPermission("shops.admin"))
 				sender.sendMessage(Help.indexSelectedAdmin);
 			if (selection.isOwner)
 				sender.sendMessage(Help.indexOwner);
 			else
 				sender.sendMessage(Help.indexNotOwner);
 			sender.sendMessage(Help.indexSelected);
 		}
 	}
 
 	private static final HashMap<String, CommandHelp> commands = new HashMap<String, CommandHelp>();
 	
 	/**
 	 * Gets help for a specific action.
 	 * @param action the action
 	 * @return the action's help
 	 */
 	public static final CommandHelp getHelpFor(String action) {
 		return commands.get(action);
 	}
 	
 	static {
 		commands.put("help", help);
 		commands.put("notifications", notifications);
 		commands.put("pending", notifications);
 		commands.put("accept", accept);
 		commands.put("reject", reject);
 		commands.put("claim", claim);
 		commands.put("skip", skip);
 		
 		commands.put("buy", buy);
 		commands.put("sell", sell);
 		
 		commands.put("add", add);
 		commands.put("remove", remove);
 		commands.put("restock", restock);
 		commands.put("set", set);
 		commands.put("sign", sign);
 
 		commands.put("create", create);
 		commands.put("delete", delete);
 		commands.put("save", save);
 		commands.put("backup", backup);
 		
 		commands.put("h", help);
 		commands.put("n", notifications);
 		commands.put("p", notifications);
 		commands.put("a", accept);
 		commands.put("yes", accept);
 		commands.put("no", reject);
 		commands.put("c", claim);
 		commands.put("sk", skip);
 		
 		commands.put("b", buy);
 		commands.put("s", sell);
 		
 		commands.put("ad", add);
 		commands.put("rm", remove);
 		commands.put("+", add);
 		commands.put("r", restock);
 
 		commands.put("c", create);
 		commands.put("mk", create);
 		commands.put("del", delete);
 	}
 
 	/**
 	 * Concatenate two String arrays
 	 * @param a the first String array
 	 * @param b the second String array
 	 * @return a String array with the elements of a followed by the
 	 * elements of b
 	 */
 	private static final String[] concat(String[] a, String[] b) {
 		String[] result = new String[a.length + b.length];
 		for (int i = 0; i < a.length; ++i)
 			result[i] = a[i];
 		for (int i = 0; i < b.length; ++i)
 			result[i + a.length] = b[i];
 		return result;
 	}
 }
