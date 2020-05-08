 package uk.badger.bConomy;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import uk.badger.bConomy.account.Account;
 import uk.badger.bConomy.account.PlayerAccounts;
 import uk.badger.bConomy.config.Config;
 import uk.badger.bConomy.config.DatabaseManager;
 
 public class bConomy extends JavaPlugin {
 	
 	/** The player listener. */
 	private PlayerListener m_playerListener = new PlayerListener();
 	
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.java.JavaPlugin#onEnable()
 	 */
 	public void onEnable() {
 		
 		Global.setPlugin(this);
 		Global.setAccounts(new PlayerAccounts());
 		Global.outputToConsole("Initialising bConomy");
 		Global.getServer().getPluginManager().registerEvents(m_playerListener, this);
 
 		if (Global.setupPermissions()) {
 			Global.outputToConsole("Vault found, setup permissions through it");
 		}
 		
 		// setup config and database
 		Config.setupConfig();
 		DatabaseManager.setupDatabase(this);
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.bukkit.plugin.java.JavaPlugin#onCommand(org.bukkit.command.CommandSender, org.bukkit.command.Command, java.lang.String, java.lang.String[])
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 
 		if (commandLabel.equalsIgnoreCase("money")) {
 						
 			// show the help
 			if (args.length != 0 && (args[0].equalsIgnoreCase("?") || args[0].equalsIgnoreCase("help"))) {
 				showHelp(sender);
 				return true;
 			}
 
 			// handle /money pay
 			if (args.length != 0 && args[0].equalsIgnoreCase("pay")) {
 				handlePay(sender, args);
 				return true;
 			}			
 			
 			// Admin cmds
 			// handle /money grant
 			if (args.length != 0 && args[0].equalsIgnoreCase("grant")) {
 				handleGrant(sender, args);
 				return true;
 			}
 			
 			// handle /money withdraw
 			if (args.length != 0 && args[0].equalsIgnoreCase("withdraw")) {
 				handleWithdraw(sender, args);
 				return true;
 			}
 
 			// handle /money set
 			if (args.length != 0 && args[0].equalsIgnoreCase("set")) {
 				handleSet(sender, args);
 				return true;
 			}
 			
 			// handle /money reset
 			if (args.length != 0 && args[0].equalsIgnoreCase("reset")) {
 				handleReset(sender, args);
 				return true;
 			}
 			
 			// handle /money top
 			if (args.length != 0 && args[0].equalsIgnoreCase("top")) {
 				handleTop(sender, args);
 				return true;
 			}
 
 			// handle /money last
 			if (args.length <= 1) {
 				handleMoney(sender, args);
 				return true;
 			}
 
 			return true;
 		}
 		
 		return false;
 	}
 
 	/**
 	 * Handle top.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleTop(CommandSender sender, String[] args) {
 		
 		if (!Global.hasPermission(sender, "bconomy.top", true))
 			return;
 		
 		int amount = 5;
 		
 		if (args.length == 2) {
 			try {
 				amount = Integer.parseInt(args[1]);
 			} catch(Exception ex) {
 				amount = 5;
 			}
 		}
 		
 		ArrayList<Account> topPlayers = Global.getAccounts().getTop(amount);
 		
 		for (int i = 0; i < topPlayers.size(); ++i) {
			Global.output(sender, (i+1) + " - " + topPlayers.get(i).getPlayer().getName() + " - " + topPlayers.get(i).getBalance() );
 		}
 		
 	}
 
 	/**
 	 * Handle reset.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleReset(CommandSender sender, String[] args) {
 		
 		if (!Global.hasPermission(sender, "bconomy.admin.reset", true))
 			return;
 		
 		if (args.length != 2) {
 			Global.output(sender, "Invalid usage. /money reset <player>");
 			return;
 		}
 
 		Account playerAccount = Global.getAccounts().get(args[1]);
 		
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for the player " + args[1]);
 			return;
 		}
 		
 		double amount = Config.m_startingBalance;
 		
 		playerAccount.setBalance(amount);
 		Global.output(sender, "You have reset '" + playerAccount.getPlayer().getName() + "s' balence");
 	}
 
 	/**
 	 * Handle set.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleSet(CommandSender sender, String[] args) {
 		
 		if (!Global.hasPermission(sender, "bconomy.admin.set", true))
 			return;
 		
 		if (args.length != 3) {
 			Global.output(sender, "Invalid usage. /money set <player> <amount>");
 			return;
 		}
 		
 		Account playerAccount = Global.getAccounts().get(args[1]);
 		
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for the player " + args[1]);
 			return;
 		}
 		
 		double amount = 0;
 		try {
 			amount = Double.parseDouble(args[2]);
 		} catch(Exception ex) {
 			Global.output(sender, "Could not understand the amount " + args[2]);
 			return;
 		}
 		
 		if (amount <= 0) {
 			Global.output(sender, "You cannot set someone to a negative amount");
 			return;
 		}
 		
 		playerAccount.setBalance(amount);
 		Global.output(sender, "You have set " + playerAccount.getPlayer().getName() + "s balence to " + Global.format(amount));
 	}
 
 	/**
 	 * Handle withdraw.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleWithdraw(CommandSender sender, String[] args) {
 
 		if (!Global.hasPermission(sender, "bconomy.admin.withdraw", true))
 			return;
 		
 		if (args.length != 3) {
 			Global.output(sender, "Invalid usage. /money withdraw <player> <amount>");
 			return;
 		}
 		
 		Account playerAccount = Global.getAccounts().get(args[1]);
 		
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for the player " + args[1]);
 			return;
 		}
 		
 		double amount = 0;
 		try {
 			amount = Double.parseDouble(args[2]);
 		} catch(Exception ex) {
 			Global.output(sender, "Could not understand the amount " + args[2]);
 			return;
 		}
 		
 		if (amount <= 0) {
 			Global.output(sender, "You cannot withdraw someone a negative amount");
 			return;
 		}
 		
 		if (!playerAccount.has(amount)) {
 			Global.output(sender, args[1] + " does not have that much money");
 			return;
 		}
 		
 		playerAccount.withdraw(amount);
 		Global.output(sender, "You have taken " + Global.format(amount) + " from "+ playerAccount.getPlayer().getName());
 		
 	}
 	
 	/**
 	 * Handle grant.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleGrant(CommandSender sender, String[] args) {
 
 		if (!Global.hasPermission(sender, "bconomy.admin.grant", true))
 			return;
 		
 		if (args.length != 3) {
 			Global.output(sender, "Invalid usage. /money grant <player> <amount>");
 			return;
 		}
 		
 		Account playerAccount = Global.getAccounts().get(args[1]);
 		
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for the player " + args[1]);
 			return;
 		}
 		
 		double amount = 0;
 		try {
 			amount = Double.parseDouble(args[2]);
 		} catch(Exception ex) {
 			Global.output(sender, "Could not understand the amount " + args[2]);
 			return;
 		}
 		
 		if (amount <= 0) {
 			Global.output(sender, "You cannot pay someone a negative amount");
 			return;
 		}
 		
 		playerAccount.deposit(amount);
 		Global.output(sender, "You have given " + playerAccount.getPlayer().getName() + " " + Global.format(amount));
 		
 	}
 
 	/**
 	 * Handle pay.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handlePay(CommandSender sender, String[] args) {
 		
 		if (!Global.hasPermission(sender, "bconomy.pay", true)) {
 			return;
 		}
 		
 		if (!(sender instanceof Player)) {
 			Global.output(sender, "The console can not use the pay command, use grant instead");
 			return;
 		}
 		
 		if (args.length != 3) {
 			Global.output(sender, "Invalid usage. /money pay <player> <ammount>");
 			return;
 		}
 		
 		Account playerAccount = Global.getAccounts().get(args[1]);
 		
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for the player " + args[1]);
 			return;
 		}
 		
 		double amount = 0;
 		try {
 			amount = Double.parseDouble(args[2]);
 		} catch(Exception ex) {
 			Global.output(sender, "Could not understand the amount " + args[2]);
 			return;
 		}
 		
 		if (amount <= 0) {
 			Global.output(sender, "You cannot pay someone a negative amount");
 			return;
 		}
 		
 		Account myAccount = Global.getAccounts().get((Player)sender);
 		
 		if (!myAccount.has(amount)) {
 			Global.output(sender, "You don't have enough money to pay that");
 			return;
 		}
 		
 		myAccount.withdraw(amount);
 		playerAccount.deposit(amount);
 		
 		Global.output(sender, "You have paid " + args[1] + " " + Global.format(amount));
 		Global.output(playerAccount.getPlayer(), "You have been paid " + Global.format(amount) + " by " + myAccount.getPlayer().getName());
 		
 	}
 
 	/**
 	 * Handle money.
 	 *
 	 * @param sender the sender
 	 * @param args the args
 	 */
 	private void handleMoney(CommandSender sender, String[] args) {
 		
 		if (!Global.hasPermission(sender, "bconomy.money", true)) {
 			return;
 		}
 			
 		Account playerAccount = null;
 		
 		if (args.length == 1) {
 			playerAccount = Global.getAccounts().get(args[0]);
 		}
 		
 		// not looking for an account and command is from console get out of here
 		if (playerAccount == null && !(sender instanceof Player)) {
 			Global.output(sender, "The console does not have an account.");
 			Global.output(sender, "Use /money <name>.");
 			return;
 		} else if (playerAccount == null) {
 			Player player = (Player)sender;
 			playerAccount = Global.getAccounts().get(player.getName());
 		}
 		
 		// could not find an account
 		if (playerAccount == null) {
 			Global.output(sender, "Could not find an account for that player.");
 			return;
 		}
 		
 		Global.output(sender, playerAccount.getPlayer().getName() + " has " + Global.format(playerAccount.getBalance()) );
 	}
 
 	/**
 	 * Show help.
 	 *
 	 * @param sender the sender
 	 */
 	private void showHelp(CommandSender sender) {
 
 		sender.sendMessage(ChatColor.GOLD + "-- bConomy --");
 
 		sender.sendMessage("/money [name] - Displays the amount of money in an account");
 		sender.sendMessage("/money pay <name> <amount> - Pays a player");
 		sender.sendMessage("/money top [amount] - Shows the top player balances");
 		
 		if (Global.hasPermission(sender, "bconomy.admin.grant", false))
 			sender.sendMessage("/money grant <name> <amount> - Grants a player money");
 		
 		if (Global.hasPermission(sender, "bconomy.admin.withdraw", false))
 			sender.sendMessage("/money withdraw <name> <amount> - Withdraws from an account");
 		
 		if (Global.hasPermission(sender, "bconomy.admin.set", false))
 			sender.sendMessage("/money set <name> <amount> - Sets a players balance");
 		
 		if (Global.hasPermission(sender, "bconomy.admin.reset", false))
 			sender.sendMessage("/money reset (name) - Resets a players balance");
 	}
 	
 }
