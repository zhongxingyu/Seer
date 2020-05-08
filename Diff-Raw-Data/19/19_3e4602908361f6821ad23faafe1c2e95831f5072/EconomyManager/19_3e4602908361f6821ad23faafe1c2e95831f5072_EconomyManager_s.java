 package com.m0pt0pmatt.bettereconomy;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Bukkit;
 import org.bukkit.DyeColor;
 import org.bukkit.Material;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.Dye;
 
 import com.m0pt0pmatt.bettereconomy.accounts.Account;
 import com.m0pt0pmatt.bettereconomy.banks.Bank;
 import com.m0pt0pmatt.bettereconomy.currency.Currency;
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.bukkit.selections.Selection;
 import com.sk89q.worldguard.domains.DefaultDomain;
 import com.sk89q.worldguard.protection.flags.DefaultFlag;
 import com.sk89q.worldguard.protection.flags.StateFlag.State;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 import com.sk89q.worldguard.protection.regions.ProtectedCuboidRegion;
 import com.sk89q.worldguard.protection.regions.ProtectedRegion;
 
 import net.milkbowl.vault.economy.Economy;
 import net.milkbowl.vault.economy.EconomyResponse;
 
 public class EconomyManager implements Economy{
 	
 	//constant variables
 	private static final String economyName = "BetterEconomy";
 	private static final String currencyName = "Dollar";
 	private static final int decimalPlaces = 2;
 	private static final double startingAmount = 50.0;
 	
 	//internal data structures
 	private final Map<String, Currency> currencies = new HashMap<String, Currency>();
 	private final Map<String, Account> accounts = new HashMap<String, Account>();
 	private final Map<String, Bank> banks = new HashMap<String, Bank>();
 
 	private YamlConfiguration config;
 	
 	public EconomyManager(File file){
 		config = YamlConfiguration.loadConfiguration(file);
 		setupCurrencies();
 	}
 	
 	//----------------------
 	//START OF FILE-BASED METHODS
 	//----------------------
 	
 	/**
 	 * Save economy data to file
 	 */
 	public void save(){
 		
 		//write the accounts
 		ConfigurationSection accountsSection = config.createSection("accounts");
 		for (Account account: accounts.values()){
 			accountsSection.set(account.getOwner(), account.getBalance());
 			
 		}
 		
 		//save the file
 		try {
 			BetterEconomy.config.save(BetterEconomy.configFile);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * load economy data from file
 	 */
 	public void load(){
 		
 		//make sure the config section has an accounts section to load
 		if (!config.contains("accounts")){
 			config.createSection("accounts");
 		}
 		
 		//get the accounts
 		ConfigurationSection accountsSection = config.getConfigurationSection("accounts");
 		
 		//clear the currently kept accounts
 		accounts.clear();
 		
 		//read the new accounts
 		for (String accountName: accountsSection.getKeys(false)){
 			accounts.put(accountName, new Account(accountName, accountsSection.getDouble(accountName)));
 		}
 	}
 	
 	/**
 	 * Sets up the known physical currencies for the economy.
 	 * Normal currencies are added via economy.addCurrency
 	 * Ores are not tradable, but they still need to be removed from player inventories
 	 * for special cases, like deducting certain values from a player (Wilderness)
 	 */
 	private void setupCurrencies() {
 		
 		//clear existing currencies
 		clearCurrencies();
 		
 		//add coal
 		addCurrency(new Currency("coal", new ItemStack(Material.COAL), 10, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("coal_block", new ItemStack(Material.COAL_BLOCK), 90, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("coal_ore", new ItemStack(Material.COAL_ORE), 22, Currency.CurrencyType.OTHER));
 		
 		//add iron
 		addCurrency(new Currency("iron", new ItemStack(Material.IRON_INGOT), 20, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("iron_block", new ItemStack(Material.IRON_BLOCK), 180, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("iron_ore", new ItemStack(Material.IRON_ORE), 20, Currency.CurrencyType.OTHER));
 		
 		//add gold
 		addCurrency(new Currency("gold",  new ItemStack(Material.GOLD_INGOT), 200, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("gold_block", new ItemStack(Material.GOLD_BLOCK), 1800, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("gold_ore", new ItemStack(Material.GOLD_ORE), 200, Currency.CurrencyType.OTHER));
 		addCurrency(new Currency("gold_nugget",  new ItemStack(Material.GOLD_NUGGET), (int)Math.floor(200/9), Currency.CurrencyType.OTHER));
 		
 		//add diamond
 		addCurrency(new Currency("diamond", new ItemStack(Material.DIAMOND), 500, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("diamond_block", new ItemStack(Material.DIAMOND_BLOCK), 4500, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("diamond_ore", new ItemStack(Material.DIAMOND_ORE), 1100, Currency.CurrencyType.OTHER));
 		
 		//add lapis
 		Dye lapis = new Dye();
 		lapis.setColor(DyeColor.BLUE);
 		ItemStack inc = new ItemStack(Material.INK_SACK);
 		inc.setData(lapis);
 		addCurrency(new Currency("lapis", new ItemStack(inc), 55, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("lapis_block", new ItemStack(Material.LAPIS_BLOCK), 495, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("lapis_ore", new ItemStack(Material.LAPIS_ORE), 120, Currency.CurrencyType.OTHER));
 		
 		//add redstone
 		addCurrency(new Currency("redstone", new ItemStack(Material.REDSTONE), 15, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("redstone_block", new ItemStack(Material.REDSTONE_BLOCK), 135, Currency.CurrencyType.BLOCK));
 		addCurrency(new Currency("redstone_ore", new ItemStack(Material.REDSTONE_ORE), 33, Currency.CurrencyType.OTHER));
 		
 		//add quartz
 		addCurrency(new Currency("quartz", new ItemStack(Material.QUARTZ), 25, Currency.CurrencyType.BASE));
 		addCurrency(new Currency("quartz_ore", new ItemStack(Material.QUARTZ_ORE), 55, Currency.CurrencyType.OTHER));
 	}
 	
 	//----------------------
 	//END OF FILE-BASED METHODS
 	//----------------------
 	
 	//----------------------
 	//START OF PLAYER COMMAND METHODS
 	//----------------------
 	
 	/**
 	 * Shows a player his or her current balance
 	 * @param sender The player executing the command
 	 */
 	public boolean showBalance(CommandSender sender) {
 		if (!(hasAccount(sender.getName()))){
 			sender.sendMessage("Error: for some reason you do not have an account");
 			return false;
 		}
 		
 		sender.sendMessage("Your current balance is: $" + ((int)getBalance(sender.getName())));
 		return true;
 	}
 	
 	private boolean canWithdraw(CommandSender sender, Currency currency, Inventory playerInventory, int amount, int bankAmount){
 		
 		//make sure the amount specified was a positive number
 		if (amount <= 0){
 			sender.sendMessage("Please enter a positive amount");
 			return false;
 		}
 		
 		//make sure its a player
 		if (!(sender instanceof Player)){
 			sender.sendMessage("Sorry, only players can execute this command");
 			return false;
 		}
 		
 		//make sure the player is in the right world
 		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
 			sender.sendMessage("Sorry, you have to be on the HomeWorld to withdraw");
 			return false;
 		}
 		
 		if (currency == null){
 			sender.sendMessage("Bad currency name");
 			return false;
 		}
 		
 		//make sure the currency is tradable
 		if (currency.getType().equals(Currency.CurrencyType.OTHER)){
 			sender.sendMessage("I'm sorry, but this currency cannot be withdrawn.");
 			sender.sendMessage("Convert the currency to its regular form to withdraw it");
 			return false;
 		}
 		
 		//make sure player has enough funds
 		if (getBalance(sender.getName()) < currency.getValue(amount)){
 			sender.sendMessage("Sorry, you dont have enough money");
 			return false;
 		}
 		
 		playerInventory = ((Player) sender).getInventory();
 		
 		//checks for enough space to withdraw
 		if (countRoomForCurrency(playerInventory, currency) < amount){
 			sender.sendMessage("Sorry, there is not enough space in your inventory.");
 			return false;
 		}
 		
 		//if not enough currency in the bank
 		if(bankAmount < amount){
 			sender.sendMessage("Not enough " + currency.getName() + " in the bank");
 			return false;
 		}
 		
 		return true;
 	}
 	
 	/**
 	 * Allows a player to withdraw an amount of physical currency into his or her inventory, given he or she has the needed funds
 	 * @param sender The player executing the command
 	 * @param currency The name of the physical currency specified
 	 * @param amount The amount of physical currency to be withdrawn
 	 */
 	public boolean withdraw(CommandSender sender, String currencyName, int amount) {
 		
 		Currency currency = currencies.get(currencyName);
 		Inventory playerInventory = ((Player) sender).getInventory();
 		int bankAmount = BetterEconomy.bank.getCurrencyAmount(currency);
 		
 		if(!canWithdraw(sender, currency, playerInventory, amount, bankAmount)){
 			return false;
 		}
 		
 		//add items to inventory
 		if(addCurrency(playerInventory, currency, amount) != 0){
 			sender.sendMessage("Something bad happened!");
 			return false;
 		}
 		
 		//convert to base item
 		if(currency.getType().equals(Currency.CurrencyType.BLOCK)){
 			currency = blockToBase(currency);
 			amount *= 9;
 		}
 		
 		//take items from bank
 		BetterEconomy.bank.updateAmount(currency, bankAmount - amount);
 		
 		//remove funds
 		this.withdrawPlayer(sender.getName(), currency.getValue(amount));
 		sender.sendMessage(amount + " " + currency.getName() + " was withdrawn.");
 		
 		return true;
 	}
 	
 	/** 
 	 *  Allows a player to withdraw the max amount of currency that they can afford
 	 *  @param sender The player executing the command
 	 *  @return true on success
 	 */
 	public boolean withdrawEverything(CommandSender sender){
 		
 		ArrayList<Currency> unsorted = new ArrayList<Currency>();
 		ArrayList<Currency> sorted = new ArrayList<Currency>();
 		
 		for(Entry<String, Currency> e: currencies.entrySet()){
 			unsorted.add(e.getValue());
 		}
 		
 		int i = 0;
 		sorted.add(unsorted.get(0));
 		for(Currency c: unsorted){
 			while(i < sorted.size()){
 				if(c.getValue() > sorted.get(i).getValue()){
 					sorted.add(i,c);
 					break;
 				}
 				i++;
 			}
 			sorted.add(i,c);
 			i = 0;
 		}
 		
 		for(Currency c: sorted){
 			if (withdrawAll(sender, c.getName()) == false){
 				return false;
 			}
 		}
 	
 		return true; 
 	}
 	 
 	/**
 	 * Withdraws as much of a given currency as the sender can afford
 	 * @param sender The individual sending the command
 	 * @param currency The currency to be deposited
 	 */
 	public boolean withdrawAll(CommandSender sender, String currencyName){
 		
 		//make sure its a player
 		if (!(sender instanceof Player)){
 			sender.sendMessage("Sorry, only players can execute this command");
 			return false;
 		}
 		
 		//make sure the player is in the right world
 		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
 			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
 			return false;
 		}
 		
 		Currency c; 
 		int i = (int)Math.floor(getBalance(sender.getName()) / (c = currencies.get(currencyName)).getValue());
 		Inventory playerInventory = ((Player) sender).getInventory();
 		
 		return withdraw(sender, currencyName, Math.min(countRoomForCurrency(playerInventory, c), i));
 	}
 	
 	private boolean canDeposit(CommandSender sender, Currency currency, Inventory playerInventory, int amount, int inventoryAmount){
 			
 		//make sure amount specified was a positive number
 		if (amount <= 0){
 			sender.sendMessage("Please enter a positive amount");
 			return false;
 		}
 		
 		//make sure its a player
 		if (!(sender instanceof Player)){
 			sender.sendMessage("Sorry, only players can execute this command");
 			return false;
 		}
 		
 		//make sure the player is in the right world
 		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
 			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
 			return false;
 		}
 
 		if (currency == null){
 			sender.sendMessage("Bad currency name");
 			return false;
 		}
 		
 		//make sure the currency is tradable
 		if (currency.getType().equals(Currency.CurrencyType.OTHER)){
 			sender.sendMessage("I'm sorry, but this currency cannot be deposited.");
 			sender.sendMessage("Convert the currency to its regular form to deposit it");
 			return false;
 		}
 
 		//make sure enough was found
 		if (inventoryAmount < amount){
 			sender.sendMessage("Sorry, you dont have enough " + currency.getName());
 			return false;
 		}
 		
 		return true;
 	}
 	 
 	/**
 	 * Allows a player to deposit an amount of physical currency into his or her account, given he or she has the currency
 	 * @param sender The player executing the command
 	 * @param currency The name of the physical currency specified
 	 * @param amount The amount of physical currency to be deposited
 	 */
 	public boolean deposit(CommandSender sender, String currencyName, int amount){
 		
 		Currency currency = this.getCurrency(currencyName);
 		Inventory playerInventory = ((Player) sender).getInventory();
 		int inventoryAmount = countCurrency(playerInventory, currency);
 
 		if(!canDeposit(sender, currency, playerInventory, amount, inventoryAmount)){
 			return false;
 		}
 		
 		//remove items
 		removeCurrency(playerInventory, currency, amount);
 		
 		//convert to base item
 		if(currency.getType().equals(Currency.CurrencyType.BLOCK)){
 			currency = blockToBase(currency);
 			amount *= 9;
 		}
 		
 		//add items to bank
 		BetterEconomy.bank.updateAmount(currency, BetterEconomy.bank.getCurrencyAmount(currency) + amount);
 		
 		//add funds
 		this.depositPlayer(sender.getName(), currency.getValue(amount));
 		sender.sendMessage(amount + " " + currency.getName() + " was deposited.");
 		return true;	
 	}
 	
 	/** 
 	 *  @Author Timmy Miles
 	 *  Allows a player to deposit all currency in his/her inventory to his/her account
 	 *  Derivation of Lucas's addition using an arraylist
 	 *  @param sender The player executing the command
 	 *  @return void
 	 */
 	 public boolean depositEverything(CommandSender sender){	 	
 	 	for(String currencyName: currencies.keySet()){
 	 		if (depositAll(sender, currencyName) == false){
 	 			return false;
 	 		}
 	 	}
 	 
 	 	return true; 
 	 }
 	 
 	 /**
 	  * Deposits all instances of a currency in the sender's inventory
 	  * @param sender The individual sending the command
 	  * @param currency The currency to be deposited
 	  */
 	 public boolean depositAll(CommandSender sender, String currencyName){
 		
 		//make sure its a player
 		if (!(sender instanceof Player)){
 			sender.sendMessage("Sorry, only players can execute this command");
 			return false;
 		}
 		
 		//make sure the player is in the right world
 		if (!(Bukkit.getWorld("HomeWorld").getPlayers().contains(sender))){
 			sender.sendMessage("Sorry, you have to be on the HomeWorld to deposit items");
 			return false;
 		}
 		
 		//get the players inventory
 		Inventory playerInventory = ((Player) sender).getInventory();
 		
 		Currency currency = getCurrency(currencyName);
 		
 		//check inventory
 		int amount = countCurrency(playerInventory, currency);
 		
 		return deposit(sender, currencyName, amount);
 	}
 	
 	private Currency blockToBase(Currency block){
 		String [] name = block.getName().split("_");
 		return currencies.get(name[0]);
 	}
 	
 	private Currency baseToBlock(Currency base){
 		return currencies.get(base.getName() + "_block");
 	}
 	
 	/**
 	 * Checks the value of a given amount of a given currency
 	 * @param sender The player executing the command
 	 * @param name The name of the currency
 	 * @param amount The amount specified
 	 */
 	public boolean checkValue(CommandSender sender, String currencyName, int amount){
 		Currency currency = getCurrency(currencyName);
 		if (currency == null){
 			sender.sendMessage("Invalid currency");
 			return false;
 		}
 		
 		String message = amount + " " + currencyName + " is worth $" + (currency.getValue(amount));
 		sender.sendMessage(message);
 		return true;
 	}
 	
 	public boolean calculateWealth(CommandSender sender){
 		
 		//make sure its a player
 		if (!(sender instanceof Player)){
 			sender.sendMessage("Sorry, only players can execute this command");
 			return false;
 		}
 		
 		//get the players inventory
 		Inventory inv = ((Player) sender).getInventory();
 		
 		double wealth = 0;
 		for (Currency currency: currencies.values()){
 			wealth += currency.getValue(this.countCurrency(inv, currency));
 		}
 		
 		//tell the player
 		sender.sendMessage("You are carrying $" + wealth + " worth in materials");
 		return true;
 	}
 	/**
 	 * Sets a players balance. Can only be executed by the server
 	 * @param server Server
 	 * @param player Player to set balance
 	 * @param amount amount to be set
 	 */
 	public boolean setBalance(CommandSender server, String playerName, double amount) {
 
 		//make sure its the server
 		if (!(server instanceof ConsoleCommandSender)){
 			server.sendMessage("Sorry, only the server can execute this command");
 			return false;
 		}
 		
 		Account account = accounts.get(playerName);
 		if (account == null){
 			server.sendMessage("No account for " + playerName);
 			return false;
 		}
 		
 		double previousBalance = account.getBalance();
 		account.setBalance(amount);
 		
 		server.sendMessage("Account was set to $" + amount + ". Previous amount was $" + previousBalance + ".");
 		return true;
 	}
 	
 	public boolean createBank(CommandSender sender, String bankName) {
 		//get the region manager for the homeworld
 		RegionManager rm = BetterEconomy.wgplugin.getRegionManager(Bukkit.getWorld("HomeWorld"));
 		if (rm == null){
 			sender.sendMessage("No region manager for the homeworld");
 			return false;
 		}
 		
 		String name = "__bank__" + bankName;
 		//make sure name isn't already used
 		if (rm.getRegion(name) != null){
 			sender.sendMessage("I'm sorry, but the name " + bankName + " is already in use.");
 			return false;
 		}
 		
 		//get the WorldEdit selection
 		Selection selection = BetterEconomy.weplugin.getSelection((Player) sender);
 		if (selection == null){
 			sender.sendMessage("Please select an area for the bank before running this command.");
 			return false;
 		}
 		
 		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
 		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
 		
 		//create WorldGuard Region
 		ProtectedRegion region = new ProtectedCuboidRegion(name, b1, b2);
 		region.setFlag(BetterEconomy.isBank, State.ALLOW);
 		region.setFlag(DefaultFlag.GREET_MESSAGE, "Welcome to the bank");
 		
 		//add the new region to WorldGuard
 		rm.addRegion(region);
 		
 		//add player to the owner of the new region
 		DefaultDomain newDomain = new DefaultDomain();
 		newDomain.addPlayer("__Server");
 		rm.getRegion(name).setOwners(newDomain);
 		
 		//save WorldGuard
 		try {
 			rm.save();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		sender.sendMessage("Bank \"" + bankName + "\" was created.");
 		return true;
 	}
 	
 	public boolean showBankValues(CommandSender sender){
 		HashMap<Currency,Integer> map = BetterEconomy.bank.getMap();
 		for(Currency c: BetterEconomy.economy.getCurrencies().values()){
 			if (c.getType().equals(Currency.CurrencyType.BASE)){
 				int value = 0;
 				if (map.get(c) != null) value = map.get(c);
 				sender.sendMessage(c.getName() + ": " + value);
 			}
 		}
 		return true;
 	}
 
 	public boolean pay(CommandSender sender, String receiver, double amount) {
 
 		if (!hasAccount(sender.getName())){
 			sender.sendMessage("I'm sorry, but you do not have an account");
 			return false;
 		}
 		
 		if (!hasAccount(receiver)){
 			sender.sendMessage("I'm sorry, but " + receiver + " does not have an account");
 			return false;
 		}
 		
 		//make sure sender has enough money
 		Account sendersAccount = accounts.get(sender.getName());
 		if (sendersAccount.getBalance() < amount){
 			sender.sendMessage("I'm sorry, but you do not have that much money in your account");
 			return false;
 		}
 		
 		//take money from the sender
 		sendersAccount.withdraw(amount);
 		
 		//pay the player
 		Account receiverAccount = accounts.get(receiver);
 		receiverAccount.deposit(amount);
 		
 		//notify both players
 		sender.sendMessage("You have payed " + receiver + " $" + amount + " dollars");
 		Bukkit.getPlayer(receiver).sendMessage("" + sender.getName() + " has payed you $" + amount + " dollars");
 		return true;
 	}
 
 	public boolean top(CommandSender sender, int number) {
 
 		sender.sendMessage("here are the top " + number + " accounts on the server:");
 		
 		List<Account> sortedList = new LinkedList<Account>();
 		sortedList.addAll(accounts.values());
 		
 		java.util.Collections.sort(sortedList, new BalanceComparator());
 		java.util.Collections.reverse(sortedList);
 		
 		int i = 0;
 		for (Account a: sortedList){
 			sender.sendMessage(a.getOwner() + ": " + a.getBalance());
 			i++;
 			if (i == number){
 				return true;
 			}
 		}
 		
 		return true;
 	}
 
 	/**
 	 * Counts all currencies in a region and prints the result as a percentage
 	 * of all blocks in a selection
 	 * @param sender the player who issued the command that calls this function
 	 * @return 
 	 */
 	public boolean evaluateCurrencies(CommandSender sender){
 		
 		Map<Currency,Integer> map = new HashMap<Currency,Integer>();
 		
 		for(Currency c: currencies.values()){
 			map.put(c,0);
 		}
 		
 		//get the WorldEdit selection
 		Selection selection = BetterEconomy.weplugin.getSelection((Player) sender);
 		BlockVector b1 = new BlockVector(selection.getMinimumPoint().getX(), selection.getMinimumPoint().getY(), selection.getMinimumPoint().getZ());
 		BlockVector b2 = new BlockVector(selection.getMaximumPoint().getX(), selection.getMaximumPoint().getY(), selection.getMaximumPoint().getZ());
 		
 		//calculate volume of selection
 		double height = selection.getMaximumPoint().getY() - selection.getMinimumPoint().getY() + 1;
 		double length = selection.getMaximumPoint().getX() - selection.getMinimumPoint().getX() + 1;
 		double width = selection.getMaximumPoint().getZ() - selection.getMinimumPoint().getZ() + 1;
 		double volume = height * length * width;
 		
 		//build the map by checking all blocks in the selection
 		for (int x = b1.getBlockX(); x < b2.getBlockX() + 1; x++){
 			for (int y = b1.getBlockY(); y < b2.getBlockY() + 1; y++){
 				for (int z = b1.getBlockZ(); z < b2.getBlockZ() + 1; z++){
 					Material b = Bukkit.getWorld(selection.getWorld().getName()).getBlockAt(x,y,z).getType();
 
 					for(Currency c: currencies.values()){
 						if(c.getItem().getType().equals(b)){
 							map.put(c, map.get(c)+1);
 						}
 					}
 				}
 			}
 		}
 		
 		for(Currency c: currencies.values()){
 			System.out.println(c.getName() + ": " + map.get(c) + "/" + volume);
 			sender.sendMessage(c.getName() + ": " + map.get(c) + "/" + volume);
 		}
 		
 		return true;
 	}
 	
 	//----------------------
 	//END OF PLAYER COMMAND METHODS
 	//----------------------
 	
 	//----------------------
 	//START OF CURRENCY METHODS
 	//----------------------
 	
 	/**
 	 * Add a physical currency to the list of valid currencies
 	 * @param newCurrency the new Currency type to be added
 	 */
 	public void addCurrency(Currency newCurrency){
 		currencies.put(newCurrency.getName(), newCurrency);
 	}
 	
 	/**
 	 * Gets a physical currency from the economy
 	 * @param currencyName the name of the currency
 	 * @return
 	 */
 	public Currency getCurrency(String currencyName){
 		return currencies.get(currencyName);
 	}
 	
 	/**
 	 * Returns all of the currencies
 	 * @return all of the currencies
 	 */
 	public Map<String, Currency> getCurrencies(){
 		return currencies;
 	}
 	
 	/**
 	 * Checks if an ItemStack is a specified currency
 	 * @param stack ItemStack
 	 * @param currencyName Currency name
 	 * @return true if so, false if not
 	 */
 	public boolean isCurrency(ItemStack stack, String currencyName){
		if (stack.getData().equals(currencies.get(currencyName).getItem().getData()) 
			&& stack.getItemMeta().equals(currencies.get(currencyName).getItem().getItemMeta())){
 				return true;
 		}
 		return false;
 	}
 	
 	/**
 	 * Checks if an ItemStack is a currency
 	 * @param item
 	 * @return
 	 */
 	public boolean isCurrency(ItemStack item) {
 		for (Currency currency: currencies.values()){
 			if (isCurrency(item, currency.getName())){
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void clearCurrencies(){
 		currencies.clear();
 	}
 	
 	//----------------------
 	//END OF CURRENCY METHODS
 	//----------------------
 	
 	//----------------------
 	//START OF INVENTORY METHODS
 	//----------------------
 	
 	/**
 	 * Counts the number of a currency is in a given inventory
 	 * @param inv The inventory to be checked
 	 * @param currency The currency to be counted
 	 * @return The amount of currency in the inventory
 	 */
 	public int countCurrency(Inventory inv, Currency currency){
 		
 		int total = 0;
 		
 		//for the inventory spots
 		for (ItemStack stack : inv.getContents()){
 			
 			//make sure the stack is non-empty
 			if (stack != null){
 				
 				//check if the stack is the given currency type
 				if (isCurrency(stack, currency.getName())){
 					
 					//add the amount of currency
 					total += stack.getAmount();
 				}
 			}
 		}
 		
 		return total;
 	}
 	
 	/**
 	 * Counts the room available in a given inventory for a given currency
 	 * @param inv The inventory to be checked
 	 * @param currency for which space is being checked
 	 * @return
 	 */
 	public int countRoomForCurrency(Inventory inv, Currency currency){
 	
 		int total = 0;
 		
 		//for the inventory spots
 		for (ItemStack stack : inv.getContents()){
 			
 			//if the spot is empty, add 64
 			if (stack == null){
 				total += 64;
 			}
 			else{
 				
 				//if the spot if of the currency type, see if we can top off the stack
 				if (isCurrency(stack, currency.getName())){
 					
 					//add the remainder
 					total += (64 - stack.getAmount());
 				}
 			}
 		}
 		
 		return total;
 	}
 	
 	/**
 	 * Adds a number of a physical currency to an inventory
 	 * @param inv The inventory to be added to
 	 * @param currency The currency to be added
 	 * @param amount The amount of currency to add
 	 */
 	public int addCurrency(Inventory inv, Currency currency, int amount){
 		
 		int remaining = amount;
 		
 		//for the inventory spots
 	 	for (ItemStack stack : inv.getContents()){
 	 		
 	 		//if there is an empty spot, add as much currency we can
 	 		if (stack == null){
 	 			
 	 			//add a full stack, or the remaining if not a full stack remains
 	 			int amountToAdd = java.lang.Math.min(remaining, 64);
 	 			
 	 			//add a new ItemStack
 	 			ItemStack item = currency.getItem().clone();
 	 			item.setAmount(amountToAdd);
 	 			inv.addItem(item);
 	 			
 	 			//update remaining counter
 	 			remaining -= amountToAdd;
 	 		}
 	 		
 	 		//stack is not empty, but check if it can be toped off
 	 		else{
 				if (isCurrency(stack, currency.getName())){
 					
 					//either top off the stack, or add the remaining if it won't top off a stack
 					int amountToAdd = java.lang.Math.min(64 - stack.getAmount(), remaining);
 					
 					//add a new ItemStack
 		 			stack.setAmount(stack.getAmount() + amountToAdd);
 					
 		 			//update remaining counter
 		 			remaining -= amountToAdd;
 				}
 	 		}
 	 		
 			//stop checking spots if nothing remains
 			if (remaining <= 0){
 				break;
 			}
 			
 		}
 	 	
 	 	//returns the amount of currency that was not put into the inventory
 	 	//0 on success, negative means too much of the currency was added (should never happen)
 	 	return remaining;
 	 }
 	 
 	/**
  	* Removes an amount of physical currency from a given inventory
  	* @param inv The inventory from which the currency is removed
  	* @param currency The currency to be removed
  	* @amount The ammount of currency to be removed
  	*/
 	public int removeCurrency(Inventory inv, Currency currency, int amount){
 		
 		//a little barbaric, but it works
 		int inventorySpot = 0;
 		
 		int remaining = amount;
 		
 		//for the inventory spots
 		for (ItemStack stack : inv.getContents()){
 			
 			//make sure the stack is not null
 			if (stack != null){
 				
 				//if the stack is of the same type as the currency being removed
 				if (isCurrency(stack, currency.getName())){
 					
 					int amountToRemove;
 					
 					//only remove the remaining
 					if (remaining < stack.getAmount()){
 						amountToRemove = remaining;
 						stack.setAmount(stack.getAmount() - remaining);
 					}
 					
 					//remove the whole stack
 					else{
 						amountToRemove = stack.getAmount();
 						inv.setItem(inventorySpot, null);	
 					}
 					
 					//update remaining counter
 					remaining -= amountToRemove;
 				}
 			}
 			
 			if (remaining <= 0){
 				break;
 			}
 			
 			inventorySpot++;
 		}
 		
 		return remaining;
 	}
 	
 	/**
 	 * Calculates the sum of all physical currencies in the player's inventory
 	 * @param sender The player executing the command
 	 */
 	public int calculateValue(Inventory inv){
 		
 		//calculate wealth
 		int value = 0;
 		for (ItemStack stack : inv.getContents()){
 			if (stack != null){
 				for (Currency c: currencies.values()){
 					if (isCurrency(stack, c.getName())){
 						value += c.getValue(stack.getAmount());
 					}
 				}
 			}
 		}
 		
 		return value;
 	}
 	
 	//----------------------
 	//END OF INVENTORY METHODS
 	//----------------------
 	
 	//----------------------
 	//START OF VAULT MATHODS
 	//----------------------
 	/**
      * Returns the amount the bank has
      * @param economyName
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse bankBalance(String arg0) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Deposit an amount into a bank account - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param economyName
      * @param amount
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse bankDeposit(String arg0, double arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Returns true or false whether the bank has the amount specified - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param economyName
      * @param amount
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse bankHas(String arg0, double arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Withdraw an amount from a bank account - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param economyName
      * @param amount
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse bankWithdraw(String arg0, double arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Creates a bank account with the specified name and the player as the owner
      * @param economyName
      * @param player
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse createBank(String arg0, String arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Attempts to create a player account for the given player
      * @return if the account creation was successful
      */
 	@Override
 	public boolean createPlayerAccount(String playerName) {
 		
 		//make sure player doesn't already have an account
 		if (this.hasAccount(playerName)){
 			return false;
 		}
 		
 		//create a new account
 		accounts.put(playerName, new Account(playerName, startingAmount));
 		return true;
 	}
 
 	/**
      * Attempts to create a player account for the given player on the specified world
      * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
      * @return if the account creation was successful
      */
 	@Override
 	public boolean createPlayerAccount(String playerName, String world) {
 		return createPlayerAccount(playerName);
 	}
 
 	/**
      * Returns the name of the currency in plural form.
      * If the economy being used does not support currency names then an empty string will be returned.
      * 
      * @return name of the currency (plural)
      */
 	@Override
 	public String currencyNamePlural() {
 		return currencyName + "s";
 	}
 
 	/**
      * Returns the name of the currency in singular form.
      * If the economy being used does not support currency names then an empty string will be returned.
      * 
      * @return name of the currency (singular)
      */
 	@Override
 	public String currencyNameSingular() {
 		return currencyName;
 	}
 
 	/**
      * Deletes a bank account with the specified name.
      * @param economyName
      * @return if the operation completed successfully
      */
 	@Override
 	public EconomyResponse deleteBank(String arg0) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param playerName Name of player
      * @param amount Amount to deposit
      * @return Detailed response of transaction
      */
 	@Override
 	public EconomyResponse depositPlayer(String playerName, double amount) {
 		
 		//make sure the player has an account
 		if (!(hasAccount(playerName))){
 			return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "No account exists for the player " + playerName);
 		}
 		
 		//get the account
 		Account account = accounts.get(playerName);
 		
 		//deposit from the account
 		account.deposit(amount);
 		
 		//return success
 		return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "Successfully removed "+ amount +" from " + playerName + "'s account");
 	}
 
 	/**
      * Deposit an amount to a player - DO NOT USE NEGATIVE AMOUNTS
      * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
      * @param playerName Name of player
      * @param amount Amount to deposit
      * @return Detailed response of transaction
      */
 	@Override
 	public EconomyResponse depositPlayer(String playerName, String world, double amount) {
 		return depositPlayer(playerName, amount);
 	}
 
 	/**
      * Format amount into a human readable String This provides translation into
      * economy specific formatting to improve consistency between plugins.  
      *
      * For now, we just add a dollar sign. I don't know if we should
      *
      * @param amount
      * @return Human readable string describing amount
      */
 	@Override
 	public String format(double arg0) {
 		return "$" + arg0;
 	}
 
 	/**
      * Some economy plugins round off after a certain number of digits.
      * This function returns the number of digits the plugin keeps
      * or -1 if no rounding occurs.
      * 
      * We round after decimalPlaces decimal places
      * 
      * @return decimalPlaces
      */
 	@Override
 	public int fractionalDigits() {
 		return decimalPlaces;
 	}
 
 	/**
      * Gets balance of a player
      * @param playerName
      * @return Amount currently held in players account
      */
 	@Override
 	public double getBalance(String playerName) {
 		return accounts.get(playerName).getBalance();
 	}
 
 	/**
      * Gets balance of a player on the specified world.
      * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
      * @param playerName
      * @param world name of the world
      * @return Amount currently held in players account
      */
 	@Override
 	public double getBalance(String playerName, String world) {
 		return getBalance(playerName);
 	}
 
 	/**
      * Gets the list of banks
      * @return the List of Banks
      */
 	@Override
 	public List<String> getBanks() {
 		List<String> list = new LinkedList<String>();
 		list.addAll(banks.keySet());
 		return list;
 	}
 
 	/**
      * Gets name of economy method
      * @return Name of Economy Method
      */
 	@Override
 	public String getName() {
 		return economyName;
 	}
 
 	/**
      * Checks if the player account has the amount - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param playerName
      * @param amount
      * @return True if <b>playerName</b> has <b>amount</b>, False else wise
      */
 	@Override
 	public boolean has(String playerName, double amount) {
 		return accounts.get(playerName).getBalance() >= amount;
 	}
 
 	/**
      * Checks if the player account has the amount in a given world - DO NOT USE NEGATIVE AMOUNTS
      * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
      * @param playerName
      * @param worldName
      * @param amount
      * @return True if <b>playerName</b> has <b>amount</b>, False else wise
      */
 	@Override
 	public boolean has(String playerName, String world, double amount) {
 		return has(playerName, amount);
 	}
 
 	/**
      * Checks if this player has an account on the server yet
      * This will always return true if the player has joined the server at least once
      * as all major economy plugins auto-generate a player account when the player joins the server
      * @param playerName
      * @return if the player has an account
      */
 	@Override
 	public boolean hasAccount(String playerName) {
 		return accounts.containsKey(playerName);
 	}
 
 	/**
      * Checks if this player has an account on the server yet on the given world
      * This will always return true if the player has joined the server at least once
      * as all major economy plugins auto-generate a player account when the player joins the server
      * 
      * Note that we don't support multi-world accounts yet.
      * 
      * @param playerName
      * @return if the player has an account
      */
 	@Override
 	public boolean hasAccount(String playerName, String world) {
 		return hasAccount(playerName);
 	}
 
 	/**
      * Returns true if the given implementation supports banks.
      * 
      * Right now, banks are not supported.
      * 
      * @return false
      */
 	@Override
 	public boolean hasBankSupport() {
 		return false;
 	}
 
 	/**
      * Check if the player is a member of the bank account
      * @param economyName
      * @param playerName
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse isBankMember(String arg0, String arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Check if a player is the owner of a bank account
      * @param economyName
      * @param playerName
      * @return EconomyResponse Object
      */
 	@Override
 	public EconomyResponse isBankOwner(String arg0, String arg1) {
 		return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Not Implemented");
 	}
 
 	/**
      * Checks if economy method is enabled.
      * @return true
      */
 	@Override
 	public boolean isEnabled() {
 		return true;
 	}
 
 	 /**
      * Withdraw an amount from a player - DO NOT USE NEGATIVE AMOUNTS
      * 
      * @param playerName Name of player
      * @param amount Amount to withdraw
      * @return Detailed response of transaction
      */
 	@Override
 	public EconomyResponse withdrawPlayer(String playerName, double amount) {
 		
 		//make sure the player has an account
 		if (!(hasAccount(playerName))){
 			return new EconomyResponse(amount, 0, EconomyResponse.ResponseType.FAILURE, "No account exists for the player " + playerName);
 		}
 		
 		//get the account
 		Account account = accounts.get(playerName);
 		
 		//withdraw from the account
 		account.withdraw(amount);
 		
 		//return success
 		return new EconomyResponse(amount, account.getBalance(), EconomyResponse.ResponseType.SUCCESS, "Successfully took "+ amount + " from " + playerName + "'s account");
 	}
 
 	/**
      * Withdraw an amount from a player on a given world - DO NOT USE NEGATIVE AMOUNTS
      * IMPLEMENTATION SPECIFIC - if an economy plugin does not support this the global balance will be returned.
      * @param playerName Name of player
      * @param worldName - name of the world
      * @param amount Amount to withdraw
      * @return Detailed response of transaction
      */
 	@Override
 	public EconomyResponse withdrawPlayer(String playerName, String world, double amount) {
 		return withdrawPlayer(playerName, amount);
 	}
 
 	//----------------------
 	//END OF VAULT MATHODS
 	//----------------------
 }
