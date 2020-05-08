 package com.archmageinc.RealStore;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Hashtable;
 import java.util.Iterator;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.World;
 import org.bukkit.block.Chest;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.material.MaterialData;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class RealStore extends JavaPlugin {
 	
 	private Logger log;
 	private Hashtable<Chest,OfflinePlayer> stores						=	new Hashtable<Chest,OfflinePlayer>();
 	private Hashtable<Chest,OfflinePlayer> coffers						=	new Hashtable<Chest,OfflinePlayer>();
 	private Hashtable<Chest,Hashtable<MaterialData,Integer>> prices		=	new Hashtable<Chest,Hashtable<MaterialData,Integer>>();
 	private Hashtable<Chest,Integer> defaultPrices						=	new Hashtable<Chest,Integer>();
 	private HashSet<Player> setting										=	new HashSet<Player>();
 	private HashSet<Player> removeSetting								=	new HashSet<Player>();
 	private final String storeFileName									=	"stores";
 	private final String cofferFileName									=	"coffers";
 	private boolean debug;
 	
 	@Override
 	public void onEnable(){
 		log	=	getLogger();
 		initialConfigCheck();
 		loadCoffers();
 		loadStores();
 		debug	=	getConfig().getBoolean("debug");
 		getCommand("RealStore").setExecutor(new RSExecutor(this));
 		getServer().getPluginManager().registerEvents(new StoreListener(this), this);
 		logMessage("Enabled");
 	}
 	
 	@Override
 	public void onDisable(){
 		logMessage("Disabled");
 	}
 	
 	/**
 	 * Checks to see if we've written the initial config
 	 * Write it if we haven't
 	 */
 	private void initialConfigCheck(){
 		getConfig().options().copyDefaults(true);
 		logMessage("Saving default configuration file.");
		saveDefaultConfig();
 	}
 	
 	/**
 	 * Saves the current coffer list to a file
 	 */
 	private void saveCoffers(){
 		File cofferFile		=	new File(getDataFolder(),cofferFileName);
 		if(cofferFile.exists())
 			cofferFile.delete();
 		if(coffers.size()==0)
 			return;
 		try {
 			cofferFile.createNewFile();
 		} catch (IOException e) {
 			logWarning("Unable to create coffers file!");
 			return;
 		}
 		
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(cofferFile);
 		Iterator<Chest> citr		=	coffers.keySet().iterator();
 		while(citr.hasNext()){
 			Chest chest					=	citr.next();
 			OfflinePlayer owner			=	coffers.get(chest);
 			String world				=	chest.getWorld().getName();
 			//We must use an 'x' at the beginning of location or YAML will think it is a list entry (which would be bad)
 			String location				=	"x"+chest.getLocation().getBlockX()+"x"+chest.getLocation().getBlockY()+"x"+chest.getLocation().getBlockZ();
 			String player				=	owner.getName();
 			config.set("coffers."+location+".world", world);
 			config.set("coffers."+location+".player", player);
 			try {
 				config.save(cofferFile);
 			} catch (IOException e) {
 				logWarning("Unable to save coffers file!");
 			}
 		}
 	}
 	
 	/**
 	 * Saves the current stores and prices to a file
 	 */
 	private void saveStores(){
 		File storeFile		=	new File(getDataFolder(),storeFileName);
 
 		if(storeFile.exists())
 			storeFile.delete();
 		if(stores.size()==0)
 			return;
 		try {
 			storeFile.createNewFile();
 		} catch (IOException e) {
 			logWarning("Unable to create stores file!");
 			return;
 		}
 		
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(storeFile);
 		
 		Iterator<Chest> citr		=	stores.keySet().iterator();
 		while(citr.hasNext()){
 			Chest chest				=	citr.next();
 			OfflinePlayer owner		=	stores.get(chest);
 			String world			=	chest.getWorld().getName();
 			//We must use an 'x' at the beginning of location or YAML will think it is a list entry (which would be bad)
 			String location			=	"x"+chest.getLocation().getBlockX()+"x"+chest.getLocation().getBlockY()+"x"+chest.getLocation().getBlockZ();
 			String player			=	owner.getName();
 			Integer defPrice		=	defaultPrices.contains(chest) ? defaultPrices.get(chest) : 1;
 			config.set("stores."+location+".world", world);
 			config.set("stores."+location+".player", player);
 			config.set("stores."+location+".default-price", defPrice);
 			if(prices.containsKey(chest)){
 				Iterator<MaterialData> pitr	=	prices.get(chest).keySet().iterator();
 				while(pitr.hasNext()){
 					MaterialData mdata	=	pitr.next();
 					Integer price		=	prices.get(chest).get(mdata);
 					String material		=	mdata.getItemTypeId()+"-"+mdata.getData();
 					config.set("stores."+location+".prices."+material, price);
 				}
 			}
 			
 		}
 		try {
 			config.save(storeFile);
 		} catch (IOException e) {
 			logWarning("Unable to save stores file!");
 		}
 		
 	}
 	
 	/**
 	 * Loads the coffers from a file
 	 */
 	private void loadCoffers(){
 		File cofferFile		=	new File(getDataFolder(),cofferFileName);
 		if(!cofferFile.exists()){
 			logMessage("Coffer file does not exist");
 			return;
 		}
 		
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(cofferFile);
 		Iterator<String> citr		=	config.getConfigurationSection("coffers").getKeys(false).iterator();
 		while(citr.hasNext()){
 			try{
 				String key			=	citr.next();
 				String wName		=	config.getString("coffers."+key+".world");
 				World world			=	getServer().getWorld(wName);
 				String pName		=	config.getString("coffers."+key+".player");
 				Location chestLoc	=	new Location(world,Double.parseDouble(key.split("x")[1]),Double.parseDouble(key.split("x")[2]),Double.parseDouble(key.split("x")[3]));
 				//The chest was some how missing since we last loaded, don't add it
 				if(!(world.getBlockAt(chestLoc).getState() instanceof Chest)){
 					logMessage("A coffer belonging to "+pName+" does not appear to exist in the world!");
 					continue;
 				}
 				Chest chest				=	(Chest) world.getBlockAt(chestLoc).getState();
 				OfflinePlayer player	=	getServer().getOfflinePlayer(pName);
 				//No offline player by that name, can't add it
 				if(player==null){
 					logMessage("The player "+pName+" does not appear to exist, though they have an assigned coffer!");
 					continue;
 				}
 				if(!addCoffer(player,chest,false))
 					logWarning("Unable to add a coffer to the list for "+player.getName()+"!");
 			}catch(NullPointerException e){
 				logWarning("The coffer file has been improperly modified!");
 				continue;
 			}
 			
 		}
 		saveCoffers();
 	}
 	
 	/**
 	 * Loads the stores and prices from a file
 	 */
 	private void loadStores(){
 		File storeFile		=	new File(getDataFolder(),storeFileName);
 		if(!storeFile.exists()){
 			logMessage("Store file does not exist");
 			return;
 		}
 		FileConfiguration config	=	YamlConfiguration.loadConfiguration(storeFile);
 		Iterator<String> citr		=	config.getConfigurationSection("stores").getKeys(false).iterator();
 		while(citr.hasNext()){
 			try{
 				String key				=	citr.next();
 				String wName			=	config.getString("stores."+key+".world");
 				World world				=	getServer().getWorld(wName);
 				String pName			=	config.getString("stores."+key+".player");
 				Integer defPrice		=	config.getInt("stores."+key+".default-price");
 				Location chestLoc		=	new Location(world,Double.parseDouble(key.split("x")[1]),Double.parseDouble(key.split("x")[2]),Double.parseDouble(key.split("x")[3]));
 				//The chest was somehow missing since we last loaded, don't add it
 				if(!(world.getBlockAt(chestLoc).getState() instanceof Chest)){
 					logMessage("A store belonging to "+pName+" does not appear to exist in the world!");
 					continue;
 				}
 				
 				Chest chest				=	(Chest) world.getBlockAt(chestLoc).getState();
 				OfflinePlayer player	=	getServer().getOfflinePlayer(pName);
 				//No offline player by that name, can't add it
 				if(player==null){
 					logMessage("The player by "+pName+" does not appear to exist, though they have a store assigned!");
 					continue;
 				}
 				
 				if(!addStore(player,chest,false)){
 					logWarning("Unable to add a store to the list for "+player.getName()+"!");
 					continue;
 				}
 				setDefaultPrice(player,chest,defPrice);
 				if(config.isConfigurationSection("stores."+key+".prices")){
 					Iterator<String> pitr	=	config.getConfigurationSection("stores."+key+".prices").getKeys(false).iterator();
 					while(pitr.hasNext()){
 						String mName		=	pitr.next();
 						Integer price		=	config.getInt("stores."+key+".prices."+mName);
 						MaterialData mData	=	new MaterialData(Integer.parseInt(mName.split("-")[0]),Byte.parseByte(mName.split("-")[1]));
 						setPrice(player,chest,mData,price);
 					}
 				}
 			}catch(NullPointerException e){
 				logWarning("The store file has been improperly modified!");
 				continue;
 			}
 			
 			
 		}
 		saveStores();
 	}
 	
 	/**
 	 * Sends the command help to a player
 	 * 
 	 * @param player Player the player who should receive the help
 	 * @param rsCommand String The sub command to help with (null for main help)
 	 */
 	public void sendHelpInfo(CommandSender player,String rsCommand){
 		(new HelpMessage(this,player,rsCommand)).send();
 	}
 	
 	/**
 	 * Sends a message to the player from the system
 	 * 
 	 * @param player Player the player who should receive the message
 	 * @param msg String the message to send
 	 */
 	public void sendPlayerMessage(Player player,String msg){
 		if(player!=null){
 			player.sendMessage(ChatColor.GOLD+"[RealStore] "+ChatColor.WHITE+msg);
 		}
 	}
 	
 	/**
 	 * Sends messages to the player from the system
 	 * 
 	 * @param player Player the player who should receive the message
 	 * @param msg String[] An array of messages to send to the player
 	 */
 	public void sendPlayerMessage(Player player,String[] msgs){
 		if(player!=null){
 			for(String msg : msgs){
 				sendPlayerMessage(player,msg);
 			}
 		}
 	}
 	
 	/**
 	 * Logs an informational message to the console
 	 * 
 	 * @param msg String the message to log
 	 */
 	public void logMessage(String msg){
 		PluginDescriptionFile pdFile	=	this.getDescription();
 		log.info("["+pdFile.getName()+" "+pdFile.getVersion()+"]: "+msg);
 	}
 	
 	/**
 	 * Logs a warning message to the console
 	 * 
 	 * @param msg String the warning to log
 	 */
 	public void logWarning(String msg){
 		PluginDescriptionFile pdFile	=	this.getDescription();
 		log.warning("["+pdFile.getName()+" "+pdFile.getVersion()+"]: "+msg);
 	}
 	
 	/**
 	 * A check to see if the player is setting a coffer, store, or price
 	 * This is used as a way to not spam the player that a chest may
 	 * not be broken
 	 * 
 	 * @param player Player the player to check if they are setting something
 	 * @return boolean True if they are setting something up, false otherwise
 	 */
 	public boolean isSetting(Player player){
 		return setting.contains(player);
 	}
 	
 	/**
 	 * Adds a player to the setting list so that isSetting will return true
 	 * 
 	 * @param player Player the player to add to the list
 	 */
 	public void addSetting(Player player){
 		setting.add(player);
 	}
 	
 	/**
 	 * Marks a player for removal from the setting list so that the next
 	 * BlockDamageEvent will clear them. It is done this way because
 	 * BlockDamageEvent fires after PlayerInteractEvent.
 	 * 
 	 * @param player Player the player to remove
 	 */
 	public void removeSetting(Player player){
 		removeSetting.add(player);
 	}
 	
 	/**
 	 * Clears the specified player from the setting list as they are finished
 	 * 
 	 * @param player Player the player to clear
 	 */
 	public void clearSetting(Player player){
 		setting.remove(player);
 		removeSetting.remove(player);
 	}
 	
 	/**
 	 * Checks to see if the specified Chest is a store
 	 * 
 	 * @param chest Chest the chest to check
 	 * @return boolean True if it is a store, false otherwise
 	 */
 	public boolean isStore(Chest chest){
 		if(chest==null)
 			return false;
 		
 		return stores.containsKey(chest);
 	}
 	
 	/**
 	 * Adds a chest to the set of stores for the specified player
 	 * 
 	 * @param player Player the player who will own the store
 	 * @param chest Chest the chest that will be the store
 	 * @return boolean True if adding the store worked, false otherwise
 	 */
 	public boolean addStore(OfflinePlayer player,Chest chest){
 		return addStore(player,chest,true);
 	}
 	
 	public boolean addStore(OfflinePlayer player,Chest chest, Boolean save){
 		if(player==null || chest==null)
 			return false;
 		if(isStore(chest))
 			return false;		
 		if(isCoffer(chest))
 			return false;
 		
 		stores.put(chest, player);
 		if(save)
 			saveStores();
 		return true;
 	}
 	
 	/**
 	 * Removes the specified Chest as a store
 	 * 
 	 * @param chest Chest the chest to remove
 	 * @return boolean True if the removal worked, false otherwise
 	 */
 	public boolean removeStore(Chest chest){
 		return removeStore(chest,true);
 	}
 	
 	public boolean removeStore(Chest chest,Boolean save){
 		if(chest==null)
 			return false;
 		if(!isStore(chest))
 			return false;
 		
 		prices.remove(chest);
 		stores.remove(chest);
 		if(save)
 			saveStores();
 		return true;
 	}
 	
 	/**
 	 * Gets the owner of the store
 	 * 
 	 * @param chest Chest the chest store to check against
 	 * @return Player the owning player
 	 */
 	public OfflinePlayer getStoreOwner(Chest chest){
 		if(chest==null)
 			return null;
 		
 		return stores.get(chest);
 	}
 	
 	/**
 	 * Checks to see if the player has a store
 	 * 
 	 * @param player Player the player to check
 	 * @return boolean True if the player has a store, false otherwise
 	 */
 	public boolean hasStore(Player player){
 		if(player==null)
 			return false;
 		return stores.contains(player);
 	}
 	
 	/**
 	 * Checks to see if the specified Chest is a coffer
 	 * 
 	 * @param chest Chest the chest to check
 	 * @return boolean True if the chest is a coffer, false otherwise
 	 */
 	public boolean isCoffer(Chest chest){
 		return coffers.containsKey(chest);
 	}
 	
 	/**
 	 * Adds the specified chest as a coffer for the player
 	 * 
 	 * @param player Player the player owning the coffer
 	 * @param chest Chest the chest that will become a coffer
 	 * @return boolean True if adding the coffer worked, false otherwise
 	 */
 	public boolean addCoffer(OfflinePlayer player,Chest chest){
 		return addCoffer(player,chest,true);
 	}
 	
 	public boolean addCoffer(OfflinePlayer player,Chest chest,Boolean save){
 		if(player==null || chest==null)
 			return false;
 		if(isStore(chest))
 			return false;
 		if(isCoffer(chest))
 			return false;
 		
 		coffers.put(chest, player);
 		if(save)
 			saveCoffers();
 		return true;
 	}
 	
 	/**
 	 * Removes a coffer from the list of coffers
 	 * 
 	 * @param chest Chest the chest to remove from the list of coffers
 	 * @return boolean True if the removal worked, false otherwise
 	 */
 	public boolean removeCoffer(Chest chest){
 		return removeCoffer(chest,true);
 	}
 	
 	public boolean removeCoffer(Chest chest, Boolean save){
 		if(chest==null)
 			return false;
 		if(!isCoffer(chest))
 			return false;
 		coffers.remove(chest);
 		if(save)
 			saveCoffers();
 		return true;
 	}
 	
 	/**
 	 * Gets the owner of the specified coffer
 	 * 
 	 * @param chest Chest the chest to get the owner
 	 * @return Player the owning player of the coffer (null if the chest is not a coffer)
 	 */
 	public OfflinePlayer getCofferOwner(Chest chest){
 		if(chest==null)
 			return null;
 		
 		return coffers.get(chest);
 	}
 	
 	/**
 	 * Checks to see if the player has a coffer
 	 * 
 	 * @param player Player the player to check
 	 * @return boolean True if the player has a coffer, false otherwise
 	 */
 	public boolean hasCoffer(Player player){
 		if(player==null)
 			return false;
 		
 		return coffers.contains(player);
 	}
 	
 	/**
 	 * Gets the total number of coffers a player has setup
 	 * @param player Player the player who's coffers to check
 	 * @return Integer The total number of coffers the player has
 	 */
 	public Integer getTotalCoffers(Player player){
 		if(player==null)
 			return null;
 		
 		Integer total					=	0;
 		Iterator<OfflinePlayer> itr		=	coffers.values().iterator();
 		while(itr.hasNext()){
 			if(itr.next().equals(player))
 				total++;
 		}
 		
 		return total;
 	}
 	
 	/**
 	 * Checks to see if the player has only one coffer
 	 * @param player Player the player to check
 	 * @return Boolean True if the player has only one coffer, false otherwise
 	 */
 	public Boolean isLastCoffer(Player player){
 		if(player==null)
 			return null;
 		
 		return (getTotalCoffers(player)==1);
 		
 	}
 	
 	/**
 	 * Sets a price of an item for a store. This will include the item data
 	 * 
 	 * @param player Player the owning player of the store
 	 * @param chest Chest the chest store where the price will be set
 	 * @param material MaterialData the type and data of the item to set the price
 	 * @param price Integer the total price in nuggets of the item
 	 * @return boolean True if the set price worked, false otherwise
 	 */
 	public boolean setPrice(OfflinePlayer player,Chest chest,MaterialData material,Integer price){
 		if(player==null || chest==null || material==null || price==null)
 			return false;
 		
 		if(price<=0)
 			return false;
 		
 		if(!isStore(chest))
 			return false;
 		
 		if(!getStoreOwner(chest).equals(player))
 			return false;
 		
 		if(prices.get(chest)==null){
 			prices.put(chest, new Hashtable<MaterialData,Integer>());
 		}
 		
 		prices.get(chest).put(material, price);
 		saveStores();
 		return true;
 	}
 	
 	/**
 	 * Sets the default price of items not specified for a store
 	 * 
 	 * @param player Player the owning player of a store
 	 * @param chest Chest the chest store where the default price will be set
 	 * @param price Integer the total price in nuggets for unspecified items
 	 * @return boolean True if the set price worked, false otherwise
 	 */
 	public boolean setDefaultPrice(OfflinePlayer player, Chest chest,Integer price){
 		if(player==null || chest==null || price==null)
 			return false;
 		
 		if(price<=0)
 			return false;
 		
 		if(!isStore(chest))
 			return false;
 		
 		if(!getStoreOwner(chest).equals(player))
 			return false;
 		
 		defaultPrices.put(chest, price);
 		saveStores();
 		return true;
 	}
 	
 	/**
 	 * Gets the price of the specified item in a store
 	 * 
 	 * @param chest Chest the chest store that contains the item
 	 * @param data MaterialData the item type to get the price
 	 * @return Integer returns the price of the item type or null if it couldn't be found
 	 */
 	public Integer getPrice(Chest chest,MaterialData data){
 		if(chest==null || data==null)
 			return null;
 		
 		if(prices.containsKey(chest) && prices.get(chest).containsKey(data))
 			return prices.get(chest).get(data);
 		
 		if(defaultPrices.containsKey(chest))
 			return defaultPrices.get(chest);
 		
 		return 1;
 	}
 	
 	/**
 	 * Deposits the amount into a player's coffers. If a player's coffers are full
 	 * or the player doesn't have any, the items will spill out somewhere
 	 * 
 	 * @param player Player the player receiving the deposit
 	 * @param amount Integer the total amount in nuggets of the deposit
 	 */
 	public void deposit(OfflinePlayer player,Integer amount){
 		if(player==null)
 			return;
 		if(amount<=0)
 			return;
 		
 		if(debug)
 			logMessage("Performing a deposit for "+player.getName()+" in the amount of "+amount.toString()+".");
 		
 		Iterator<Chest> itr				=	coffers.keySet().iterator();
 		HashSet<ItemStack> currency		=	Currency.colorUpSet(amount, false);
 		if(debug)
 			logMessage("There are "+currency.size()+" items in the deposit.");
 		
 		while(itr.hasNext()){
 			Chest chest	=	itr.next();
 			if(coffers.get(chest).equals(player)){
 				
 				HashSet<ItemStack> left		=	new HashSet<ItemStack>();
 				Iterator<ItemStack> citr	=	currency.iterator();
 				while(citr.hasNext()){
 					ItemStack item					=	citr.next();
 					if(debug)
 						logMessage("Depositing "+item.toString()+" in "+player.getName()+"'s coffer at "+chest.getLocation().toString()+".");
 					Collection<ItemStack> remainder	=	chest.getInventory().addItem(item).values();
 					if(debug)
 						logMessage("There were "+remainder.size()+" stacks left over");
 					citr.remove();
 					if(remainder.size()>0){
 						left.addAll(remainder);
 					}
 				}
 				currency.addAll(left);
 			}
 			
 			if(currency.size()==0)
 				break;
 		}
 		if(currency.size()>0){
 			/**
 			 * We couldn't fit all of the money in their coffers
 			 */
 			
 			if(debug)
 				logMessage("Not all of the deposit would fit in "+player.getName()+"'s coffer.");
 			
 			if(player.isOnline()){
 				Player owner	=	player.getPlayer();
 				if(debug)
 					logMessage("Player "+player.getName()+" is online, dropping it in the world at "+owner.getLocation().toString()+".");
 				
 				sendPlayerMessage(owner, ChatColor.BLUE+"Warning: "+ChatColor.WHITE+"Your coffers are full! Sending the money directly to you!");
 				Iterator<ItemStack> citr	=	currency.iterator();
 				while(citr.hasNext()){
 					owner.getWorld().dropItemNaturally(owner.getLocation(), citr.next());
 				}
 			}else{
 				logMessage("The owner was not online so we have nowhere to put the money!");
 			}
 		}
 	}
 	
 	public boolean debug(){
 		return debug;
 	}
 }
