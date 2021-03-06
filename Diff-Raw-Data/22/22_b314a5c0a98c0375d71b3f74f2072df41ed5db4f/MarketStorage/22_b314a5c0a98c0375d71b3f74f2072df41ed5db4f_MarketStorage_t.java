 package com.survivorserver.GlobalMarket;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.enchantments.Enchantment;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.meta.BookMeta;
 import org.yaml.snakeyaml.Yaml;
 import org.yaml.snakeyaml.constructor.CustomClassLoaderConstructor;
 
 import com.google.common.base.Predicate;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.survivorserver.GlobalMarket.Lib.SearchResult;
 import com.survivorserver.GlobalMarket.SQL.Database;
 import com.survivorserver.GlobalMarket.SQL.AsyncDatabase;
 import com.survivorserver.GlobalMarket.SQL.MarketResult;
 import com.survivorserver.GlobalMarket.SQL.QueuedStatement;
 import com.survivorserver.GlobalMarket.SQL.StorageMethod;
 
 public class MarketStorage {
 
 	private Market market;
 	private AsyncDatabase asyncDb;
 	private Map<Integer, ItemStack> items;
 	private LinkedHashMap<Integer, Listing> listings;
 	private HashMap<String, List<Listing>> worldListings;
 	private LinkedHashMap<Integer, Mail> mail;
 	private HashMap<String, List<Mail>> worldMail;
 	private LinkedHashMap<Integer, QueueItem> queue;
 	private int itemIndex;
 	private int listingIndex;
 	private int mailIndex;
 	private int queueIndex;
 	
 	public MarketStorage(Market market, AsyncDatabase asyncDb) {
 		this.market = market;
 		this.asyncDb = asyncDb;
 		items = new HashMap<Integer, ItemStack>();
 		listings = new LinkedHashMap<Integer, Listing>();
 		worldListings = new HashMap<String, List<Listing>>();
 		mail = new LinkedHashMap<Integer, Mail>();
 		worldMail = new HashMap<String, List<Mail>>();
 		queue = new LinkedHashMap<Integer, QueueItem>();
 	}
 	
 	public void loadSchema(Database db) {
 		boolean sqlite = market.getConfigHandler().getStorageMethod() == StorageMethod.SQLITE;
 		try {
 			// Create items table
 			db.createStatement("CREATE TABLE IF NOT EXISTS items ("
 					+ (sqlite ? "id INTEGER NOT NULL PRIMARY KEY, " : "id int NOT NULL PRIMARY KEY AUTO_INCREMENT, ")
 					+ (sqlite ? "item MEDIUMTEXT" : "item MEDIUMTEXT CHARACTER SET utf8 COLLATE utf8_general_ci")
 					+ ")").execute();
 			// Create listings table
 			db.createStatement("CREATE TABLE IF NOT EXISTS listings ("
 					+ (sqlite ? "id INTEGER NOT NULL PRIMARY KEY, " : "id int NOT NULL PRIMARY KEY AUTO_INCREMENT, ") 
 					+ "seller TINYTEXT, "
 					+ "item int, "
 					+ "amount int, "
 					+ "price DOUBLE, "
 					+ "world TINYTEXT, "
 					+ "time BIGINT)").execute();
 			// Create mail table
 			db.createStatement("CREATE TABLE IF NOT EXISTS mail ("
 					+ (sqlite ? "id INTEGER NOT NULL PRIMARY KEY, " : "id int NOT NULL PRIMARY KEY AUTO_INCREMENT, ")
 					+ "owner TINYTEXT, "
 					+ "item int, "
 					+ "amount int, "
 					+ "sender TINYTEXT, "
 					+ "world TINYTEXT, "
 					+ "pickup DOUBLE)").execute();
 			// Create queue table
 			db.createStatement("CREATE TABLE IF NOT EXISTS queue ("
 					+ (sqlite ? "id INTEGER NOT NULL PRIMARY KEY, " : "id int NOT NULL PRIMARY KEY AUTO_INCREMENT, ")
 					+ "data MEDIUMTEXT)").execute();
 			// Create users metadata table
 			db.createStatement("CREATE TABLE IF NOT EXISTS users ("
 					+ "name varchar(16) NOT NULL UNIQUE, "
 					+ "earned DOUBLE, "
 					+ "spent DOUBLE)").execute();
 			// Create history table
 			db.createStatement("CREATE TABLE IF NOT EXISTS history ("
 					+ (sqlite ? "id INTEGER NOT NULL PRIMARY KEY, " : "id int NOT NULL PRIMARY KEY AUTO_INCREMENT, ")
 					+ "player TINYTEXT, "
 					+ "action TINYTEXT, "
 					+ "who TINYTEXT, "
 					+ "item int, "
 					+ "amount int, "
 					+ "price DOUBLE, "
 					+ "time BIGINT)").execute();
 		} catch(Exception e) {
 			market.log.severe("Error while preparing database:");
 			e.printStackTrace();
 		}
 	}
 	
 	public void load(Database db) {
 		// Items we should cache in memory
 		List<Integer> itemIds = new ArrayList<Integer>();
 		try {
 			/*
 			 * Synchronize the listing index with the database
 			 */
 			listings.clear();
 			listingIndex = 1;
 			MarketResult res = db.createStatement("SELECT id FROM listings ORDER BY id DESC LIMIT 1").query();
 			if (res.next()) {
 				listingIndex = res.getInt(1) + 1;
 			}
 			res = db.createStatement("SELECT * FROM listings ORDER BY id ASC").query();
 			while(res.next()) {
 				Listing listing = res.constructListing(this);
 				int id = listing.getItemId();
 				if (!itemIds.contains(id)) {
 					itemIds.add(id);
 				}
 				listings.put(listing.getId(), listing);
 				addWorldItem(listing);
 			}
 			/*
 			 * Synchronize the mail index with the database
 			 */
 			mail.clear();
 			res = db.createStatement("SELECT * FROM mail ORDER BY id ASC").query();
 			while(res.next()) {
 				Mail m = res.constructMail(this);
 				int id = m.getItemId();
 				if (!itemIds.contains(id)) {
 					itemIds.add(id);
 				}
 				mail.put(m.getId(), m);
 				addWorldItem(m);
 			}
 			mailIndex = 1;
 			res = db.createStatement("SELECT id FROM mail ORDER BY id DESC LIMIT 1").query();
 			if (res.next()) {
 				mailIndex = res.getInt(1) + 1;
 			}
 			/*
 			 * Queue
 			 */
 			queue.clear();
 			res = db.createStatement("SELECT * FROM queue ORDER BY id ASC").query();
 			Yaml yaml = new Yaml(new CustomClassLoaderConstructor(Market.class.getClassLoader()));
 			while(res.next()) {
 				QueueItem item = yaml.loadAs(res.getString(2), QueueItem.class);
 				queue.put(item.getId(), item);
 				int itemId;
 				if (item.getMail() != null) {
 					itemId = item.getMail().getItemId();
 				} else {
 					itemId = item.getListing().getItemId();
 				}
 				if (!itemIds.contains(itemId)) {
 					itemIds.add(itemId);
 				}
 			}
 			queueIndex = 1;
 			res = db.createStatement("SELECT id FROM queue ORDER BY id DESC LIMIT 1").query();
 			if (res.next()) {
 				queueIndex = res.getInt(1) + 1;
 			}
 			/*
 			 * Synchronize needed items
 			 */
 			items.clear();
 			if (itemIds.size() > 0) {
 				StringBuilder query = new StringBuilder();
 				query.append("SELECT * FROM items WHERE id IN (");
 				for (int i = 0; i < itemIds.size(); i++) {
 					query.append(itemIds.get(i));
 					if (i + 1 == itemIds.size()) {
 						query.append(")");
 					} else {
 						query.append(", ");
 					}
 				}
 				res = db.createStatement(query.toString()).query();
 				while(res.next()) {
 					items.put(res.getInt(1), res.getItemStack(2));
 				}
 			}
 			itemIndex = 1;
 			res = db.createStatement("SELECT id FROM items ORDER BY id DESC LIMIT 1").query();
 			if (res.next()) {
 				itemIndex = res.getInt(1) + 1;
 			}
 			asyncDb.startTask();
 		} catch(Exception e) {
 			market.log.severe("Error while loading:");
 			e.printStackTrace();
 		}
 	}
 	
 	private void addWorldItem(Listing listing) {
 		String world = listing.getWorld();
 		if (!worldListings.containsKey(world)) {
 			worldListings.put(world, new ArrayList<Listing>());
 		}
 		worldListings.get(world).add(listing);
 	}
 	
 	private void addWorldItem(Mail mailItem) {
 		String world = mailItem.getWorld();
 		if (!worldMail.containsKey(world)) {
 			worldMail.put(world, new ArrayList<Mail>());
 		}
 		worldMail.get(world).add(mailItem);
 	}
 	
 	private List<Listing> getListingsForWorld(String world) {
 		List<Listing> toReturn = new ArrayList<Listing>();
 		if (worldListings.containsKey(world)) {
 			toReturn.addAll(worldListings.get(world));
 		}
 		for (String w : market.getLinkedWorlds(world)) {
 			if (worldListings.containsKey(w)) {
 				toReturn.addAll(worldListings.get(w));
 			}
 		}
 		return toReturn;
 	}
 	
 	private List<Mail> getMailForWorld(String world) {
 		List<Mail> toReturn = new ArrayList<Mail>();
 		if (worldMail.containsKey(world)) {
 			toReturn.addAll(worldMail.get(world));
 		}
 		for (String w : market.getLinkedWorlds(world)) {
 			if (worldMail.containsKey(w)) {
 				toReturn.addAll(worldMail.get(w));
 			}
 		}
 		return toReturn;
 	}
 
 	public AsyncDatabase getAsyncDb() {
 		return asyncDb;
 	}
 	
 	public Listing queueListing(String seller, ItemStack itemStack, double price, String world) {
 		int itemId = storeItem(itemStack);
 		long time = System.currentTimeMillis();
 		Listing listing = new Listing(listingIndex++, seller, itemId, itemStack.getAmount(), price, world, time);
 		QueueItem item = new QueueItem(queueIndex++, time, listing);
 		queue.put(item.getId(), item);
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO queue (data) VALUES (?)")
 		.setValue(new Yaml().dump(item)));
 		return listing;
 	}
 	
 	public Mail queueMail(String owner, String from, ItemStack itemStack, String world) {
 		int itemId = storeItem(itemStack);
 		Mail mail = new Mail(owner, mailIndex++, itemId, itemStack.getAmount(), 0, from, world);
 		QueueItem item = new QueueItem(queueIndex++, System.currentTimeMillis(), mail);
 		queue.put(item.getId(), item);
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO queue (data) VALUES (?)")
 		.setValue(new Yaml().dump(item)));
 		return mail;
 	}
 	
 	public Mail queueMail(String owner, String from, int itemId, int amount, String world) {
 		Mail mail = new Mail(owner, mailIndex++, itemId, amount, 0, from, world);
 		QueueItem item = new QueueItem(queueIndex++, System.currentTimeMillis(), mail);
 		queue.put(item.getId(), item);
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO queue (data) VALUES (?)")
 		.setValue(new Yaml().dump(item)));
 		return mail;
 	}
 	
 	public synchronized List<QueueItem> getQueue() {
 		return new ArrayList<QueueItem>(queue.values());
 	}
 	
 	public synchronized void removeItemFromQueue(int id) {
 		QueueItem item = queue.get(new Integer(id));
 		if (item.getMail() != null) {
 			storeMail(item.getMail());
 		} else {
 			storeListing(item.getListing());
 		}
 		asyncDb.addStatement(new QueuedStatement("DELETE FROM queue WHERE id=?").setValue(id));
 		queue.remove(id);
 	}
 	
 	public static String itemStackToString(ItemStack item) {
 		YamlConfiguration conf = new YamlConfiguration();
 		ItemStack toSave = item.clone();
 		toSave.setAmount(1);
 		conf.set("item", toSave);
 		return conf.saveToString();
 	}
 	
 	public static ItemStack itemStackFromString(String item) {
 		YamlConfiguration conf = new YamlConfiguration();
 		try {
 			conf.loadFromString(item);
 			return new ItemStack(conf.getItemStack("item"));
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static ItemStack itemStackFromString(String item, int amount) {
 		YamlConfiguration conf = new YamlConfiguration();
 		try {
 			conf.loadFromString(item);
 			ItemStack itemStack = conf.getItemStack("item");
 			itemStack.setAmount(amount);
 			return itemStack;
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public int storeItem(ItemStack item) {
 		for (Entry<Integer, ItemStack> ent : items.entrySet()) {
 			if (ent.getValue().equals(item)) {
 				return ent.getKey();
 			}
 		}
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO items (item) VALUES (?)")
 		.setValue(item));
 		items.put(itemIndex, item);
 		return itemIndex++;
 	}
 	
 	public ItemStack getItem(int id, int amount) {
 		if (!items.containsKey(new Integer(id))) {
 			market.log.severe("Couldn't find an item with ID " + id);
 		}
 		ItemStack item = items.get(new Integer(id)).clone();
 		item.setAmount(amount);
 		return item;
 	}
 	
 	public Listing createListing(String seller, ItemStack item, double price, String world) {
 		int itemId = storeItem(item);
 		Long time = System.currentTimeMillis();
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO listings (seller, item, amount, price, world, time) VALUES (?, ?, ?, ?, ?, ?)")
 		.setValue(seller)
 		.setValue(itemId)
 		.setValue(item.getAmount())
 		.setValue(price)
 		.setValue(world)
 		.setValue(time));
 		Listing listing = new Listing(listingIndex++, seller, itemId, item.getAmount(), price, world, time);
 		listings.put(listing.getId(), listing);
 		addWorldItem(listing);
 		market.getInterfaceHandler().updateAllViewers();
 		return listing;
 	}
 	
 	public void storeListing(Listing listing) {
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO listings (id, seller, item, amount, price, world, time) VALUES (?, ?, ?, ?, ?, ?, ?)")
 		.setValue(listing.getId())
 		.setValue(listing.getSeller())
 		.setValue(listing.getItemId())
 		.setValue(listing.getAmount())
 		.setValue(listing.getPrice())
 		.setValue(listing.getWorld())
 		.setValue(listing.getTime()));
 		listings.put(listing.getId(), listing);
 		addWorldItem(listing);
 		market.notifyPlayer(listing.getSeller(), market.getLocale().get("your_listing_has_been_added", market.getItemName(getItem(listing.getItemId(), listing.getAmount()))));
 		market.getInterfaceHandler().updateAllViewers();
 	}
 	
 	public void storeMail(Mail m) {
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO mail (id, owner, item, amount, sender, world, pickup) VALUES (?, ?, ?, ?, ?, ?, ?)")
 		.setValue(m.getId())
 		.setValue(m.getOwner())
 		.setValue(m.getItemId())
 		.setValue(m.getAmount())
 		.setValue(m.getSender())
 		.setValue(m.getWorld())
 		.setValue(m.getPickup()));
 		mail.put(m.getId(), m);
 		addWorldItem(m);
 		market.notifyPlayer(m.getOwner(), market.getLocale().get("you_have_new_mail"));
 		market.getInterfaceHandler().refreshViewer(m.getOwner());
 	}
 	
 	public synchronized Listing getListing(int id) {
 		if (listings.containsKey(id)) {
 			return listings.get(id);
 		}
 		return null;
 	}
 	
 	public List<Listing> getListings(int page, int pageSize, String world) {
 		List<Listing> toReturn = new ArrayList<Listing>();
 		int index = (pageSize * page) - pageSize;
 		List<Listing> list = Lists.reverse(market.enableMultiworld() ? getListingsForWorld(world) : new ArrayList<Listing>(listings.values()));
 		while (list.size() > index && toReturn.size() < pageSize) {
 			toReturn.add(list.get(index));
 			index++;
 		}
 		return toReturn;
 	}
 	
 	public synchronized List<Listing> getAllListings() {
 		return new ArrayList<Listing>(listings.values());
 	}
 	
 	@SuppressWarnings("deprecation")
 	public SearchResult getListings(int page, int pageSize, String search, String world) {
 		List<Listing> found = new ArrayList<Listing>();
 		List<Listing> list = Lists.reverse(market.enableMultiworld() ? getListingsForWorld(world) : new ArrayList<Listing>(listings.values()));
 		for (Listing listing : list) {
 			ItemStack item = getItem(listing.getItemId(), listing.getAmount());
 			String itemName = market.getItemName(item);
 			if (itemName.toLowerCase().contains(search.toLowerCase())
 					|| isItemId(search, item.getTypeId())
 					|| isInDisplayName(search.toLowerCase(), item)
 					|| isInEnchants(search.toLowerCase(), item)
 					|| isInLore(search.toLowerCase(), item)) {
 				found.add(listing);
 			}
 		}
 		list.clear();
 		int index = (pageSize * page) - pageSize;
 		List<Listing> toReturn = new ArrayList<Listing>();
 		while (found.size() > index && toReturn.size() < pageSize) {
 			toReturn.add(found.get(index));
 			index++;
 		}
 		return new SearchResult(found.size(), toReturn);
 	}
 	
 	public synchronized void removeListing(int id) {
 		Listing listing = listings.get(id);
 		listings.remove(id);
 		worldListings.get(listing.getWorld()).remove(listing);
 		asyncDb.addStatement(new QueuedStatement("DELETE FROM listings WHERE id=?")
 		.setValue(id));
 	}
 	
 	public int getNumListings(String world) {
 		return market.enableMultiworld() ? getListingsForWorld(world).size() : listings.size();
 	}
 	
 	public int getNumListingsFor(String name, String world) {
 		int amount = 0;
 		for (Listing listing : market.enableMultiworld() ? getListingsForWorld(world) : listings.values()) {
 			if (listing.getSeller().equalsIgnoreCase(name)) {
 				amount++;
 			}
 		}
 		return amount;
 	}
 	
 	public LinkedHashMap<Integer, Listing> getCachedListingIndex() {
 		return listings;
 	}
 	
 	public Mail createMail(String owner, String from, int itemId, int amount, String world) {
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO mail (owner, item, amount, sender, world, pickup) VALUES (?, ?, ?, ?, ?, ?)")
 		.setValue(owner)
 		.setValue(itemId)
 		.setValue(amount)
 		.setValue(from)
 		.setValue(world)
 		.setValue(0));
 		Mail m = new Mail(owner, mailIndex++, itemId, amount, 0, from, world);
 		mail.put(m.getId(), m);
 		addWorldItem(m);
 		market.getInterfaceHandler().refreshViewer(m.getOwner());
 		return m;
 	}
 	
 	public Mail createMail(String owner, String from, ItemStack item, double pickup, String world) {
 		int itemId = storeItem(item);
 		asyncDb.addStatement(new QueuedStatement("INSERT INTO mail (owner, item, amount, sender, world, pickup) VALUES (?, ?, ?, ?, ?, ?)")
 		.setValue(owner)
 		.setValue(itemId)
 		.setValue(item.getAmount())
 		.setValue(from)
 		.setValue(world)
 		.setValue(pickup));
 		Mail m = new Mail(owner, mailIndex++, itemId, item.getAmount(), pickup, from, world);
 		mail.put(m.getId(), m);
 		addWorldItem(m);
 		market.getInterfaceHandler().refreshViewer(m.getOwner());
 		return m;
 	}
 	
 	public void storePayment(ItemStack item, String player, String buyer, double amount, String world) {
 		ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
 		BookMeta meta = (BookMeta) book.getItemMeta();
 		if (meta == null) {
 			meta = (BookMeta) market.getServer().getItemFactory().getItemMeta(book.getType());
 		}
 		meta.setTitle(market.getLocale().get("transaction_log.item_name"));
 		double cut = market.getCut(amount, player, world);
 		String itemName = market.getItemName(item);
 		String logStr = market.getLocale().get("transaction_log.title") + "\n\n" +
 						market.getLocale().get("transaction_log.item_sold", itemName) + "\n\n" +
 						market.getLocale().get("transaction_log.sale_price", amount) + "\n\n" +
 						market.getLocale().get("transaction_log.market_cut", cut) +  "\n\n" +
 						market.getLocale().get("transaction_log.amount_recieved", (amount-cut));
 		meta.setPages(logStr);
 		book.setItemMeta(meta);
 		createMail(player, buyer, book, amount - cut, world);
 	}
 	
 	public Mail getMail(int id) {
 		if (mail.containsKey(id)) {
 			return mail.get(id);
 		}
 		return null;
 	}
 	
 	public List<Mail> getMail(final String owner, int page, int pageSize, final String world) {
 		Collection<Mail> ownedMail = Collections2.filter(market.enableMultiworld() ? getMailForWorld(world) : mail.values(), new Predicate<Mail>() {
 			public boolean apply(Mail mail) {
 				return mail.getOwner().equals(owner);
 			}
 		});
 		List<Mail> toReturn = new ArrayList<Mail>();
 		int index = (pageSize * page) - pageSize;
 		List<Mail> list = Lists.reverse(new ArrayList<Mail>(ownedMail));
 		while (ownedMail.size() > index && toReturn.size() < pageSize) {
 			toReturn.add(list.get(index));
 			index++;
 		}
 		return toReturn;
 	}
 	
 	public void nullifyMailPayment(int id) {
 		asyncDb.addStatement(new QueuedStatement("UPDATE mail SET pickup=? WHERE id=?")
 		.setValue(0)
 		.setValue(id));
 		if (mail.containsKey(id)) {
			Mail m = mail.get(id);
			m.setPickup(0);
			market.getInterfaceHandler().refreshViewer(m.getOwner());
 		}
 	}
 	
 	public void removeMail(int id) {
 		Mail m = mail.get(id);
 		mail.remove(id);
 		worldMail.get(m.getWorld()).remove(m);
 		asyncDb.addStatement(new QueuedStatement("DELETE FROM mail WHERE id=?")
 		.setValue(id));
		market.getInterfaceHandler().refreshViewer(m.getOwner());
 	}
 	
 	public int getNumMail(final String player, final String world) {
 		Collection<Mail> ownedMail = Collections2.filter(market.enableMultiworld() ? getMailForWorld(world) : mail.values(), new Predicate<Mail>() {
 				public boolean apply(Mail mail) {
 					return mail.getOwner().equals(player);
 				}
 			});
 		return ownedMail.size();
 	}
 
 	/*
 	 * Basic search method
 	 */
 	public boolean isItemId(String search, int typeId) {
 		if (search.equalsIgnoreCase(Integer.toString(typeId))) {
 			return true;
 		}
 		return false;
 	}
 	
 	/*
 	 * Basic search method
 	 */
 	public boolean isInDisplayName(String search, ItemStack item) {
 		if (item.hasItemMeta() && item.getItemMeta().hasDisplayName()) {
 			return item.getItemMeta().getDisplayName().toLowerCase().contains(search);
 		}
 		return false;
 	}
 	
 	/*
 	 * Basic search method
 	 */
 	public boolean isInEnchants(String search, ItemStack item) {
 		if (item.hasItemMeta() && item.getItemMeta().hasEnchants()) {
 			for (Entry<Enchantment, Integer> entry : item.getItemMeta().getEnchants().entrySet()) {
 				if (entry.getKey().getName().toLowerCase().contains(search)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 	
 	/*
 	 * Basic search method
 	 */
 	public boolean isInLore(String search, ItemStack item) {
 		if (item.hasItemMeta() && item.getItemMeta().hasLore()) {
 			for (String l : item.getItemMeta().getLore()) {
 				if (l.toLowerCase().contains(search)) {
 					return true;
 				}
 			}
 		}
 		return false;
 	}
 }
