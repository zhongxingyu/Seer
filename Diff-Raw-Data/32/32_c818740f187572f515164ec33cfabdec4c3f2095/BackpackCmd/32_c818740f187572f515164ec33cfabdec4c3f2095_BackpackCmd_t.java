 package Commands;
 
 import java.io.*;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.HashMap;
 import java.util.List;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.logging.Level;
 import SQLib.SQLite;
 import Main.EtriaCommands;
 import Main.Strings;
 import Util.CommandHandle;
 import Util.ItemStackSerialize;
 import Util.Utils;
 import org.bukkit.Bukkit;
 import org.bukkit.OfflinePlayer;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 
 public class BackpackCmd implements Listener {
 	
 	private EtriaCommands main;
 	public static SQLite sql;
 	
 	//Backpacks
 	public static HashSet<String> backpackusers = new HashSet();
 	public static HashMap<String, ItemStack[]> backpacks = new HashMap();
 	
 	// Snooping
 	public static HashSet<String> snoopers = new HashSet();
 	
 	private static final int MAX_INVENTORY_ROWS = 6;
 	private static final int INVENTORY_ROW_SIZE = 9;
 
 	public BackpackCmd() {
 		main = EtriaCommands.getInstance();
 		
 		if (sql == null || sql.getConnection() == null) {
 			setupDatabase();
 		}
 		
 		if  (!sql.tableExists("packs")) {
 			Utils.log.log(Level.INFO, Strings.PREFIX + "Creating backpacks table..");
 			String query = "CREATE TABLE `packs` (`player` VARCHAR(32) PRIMARY KEY, `backpack` BLOB);";
 			sql.modifyQuery(query);
 		}
 		
 		loadPackDb();
 		
 		EtriaCommands.getInstance().getServer().getPluginManager().registerEvents(this, EtriaCommands.getInstance());
 	}
 	
 	private void setupDatabase() {
 		sql = new SQLite(Utils.log, Strings.PREFIX.toString(), "backpacks.db", main.getDataFolder().getAbsolutePath());
 		sql.open();
 	}
 	
 	private void loadPackDb() {
 		try {
 			Statement stmt = sql.getConnection().createStatement();
 			ResultSet rs = stmt.executeQuery("SELECT * FROM `packs`");
 			
 			while(rs.next()) {
 				byte[] buf = rs.getBytes("backpack");
 				String player = rs.getString("player");
 				
 				ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(buf));
 				Object o = ois.readObject();
 				
 				ItemStack[] iss = ItemStackSerialize.fromMapList((List) o);
 				
 				backpacks.put(player, iss);
 			}
 		} catch (SQLException | IOException | ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	private static void closePack(String player, Inventory inv) {
 		backpackusers.remove(player);
 		updatePack(player, inv.getContents());
 	}
 	
 	// Saves backpacks
 	private static void updatePack(String player, ItemStack[] iss) {
 		backpacks.put(player, iss);
 		List<Map<String, Object>> list = ItemStackSerialize.toMapList(iss);
 		
 		writeToDb(list, player);
 		
 		Utils.log.info(Strings.PREFIX + "Saved backpack for " + player);
 	}
 	
 	// Serializes and writes to table
 	private static void writeToDb(List list, String player) {
 		try {
 			final PreparedStatement stmt = sql.getConnection().prepareStatement("INSERT OR REPLACE INTO `packs` (`player`, `backpack`) VALUES (?, ?);");
 			final ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			final ObjectOutputStream oos = new ObjectOutputStream(baos);
 			
 			oos.writeObject(list);
 			oos.close();
 			
 			stmt.setString(1, player);
 			stmt.setBytes(2, baos.toByteArray());
 			
 			stmt.execute();
 			stmt.close();
 		} catch (SQLException | IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	// Command Time!
 	@CommandHandle(name = "backpack", perms = "ec.backpack")
 	public static boolean pack(CommandSender s, String[] args) {
 		if (!(s instanceof Player)) return false;
 		
 		final Player p = (Player) s;
 		final int packSize = getPackSize(p);
 		String packOwner = s.getName();
 		
 		final OfflinePlayer op;
 		if (args.length >= 1 && s.hasPermission("ec.backpack.other")) {
 			snoopers.add(s.getName());
 			op = Bukkit.getOfflinePlayer(args[0]);
 			
 			if (!op.hasPlayedBefore()) {
 				s.sendMessage("cPlayer has not played before, therefore they do not have a backpack");
 				return true;
 			}
 			
 			packOwner = op.getName();
 		}
 		
 		final Inventory i = Bukkit.createInventory(p, packOwner.equals(s.getName())? packSize : MAX_INVENTORY_ROWS * INVENTORY_ROW_SIZE, packSize > 9? "Backpack" : "Satchel");
 		
 		if (backpacks.containsKey(packOwner)) {
 			setInventoryContents(i, backpacks.get(packOwner));
 		} else {
 			Utils.log.log(Level.INFO, Strings.PREFIX + "Creating a new backpack for " + packOwner);
 		}
 		p.openInventory(i);
 		
 		backpackusers.add(p.getName());
 		return true;
 	}
 	
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent e) {
 		if (backpackusers.contains(e.getPlayer().getName()) && !snoopers.contains(e.getPlayer().getName())) {
 			final String invTitle = e.getInventory().getTitle();
 			if (!invTitle.equalsIgnoreCase("backpack") && invTitle.equalsIgnoreCase("satchel")) return; //Double Sure
 			closePack(e.getPlayer().getName(), e.getInventory());
 		}
 		
 		// Closed Inventory, snoopers can click again!
 		if (snoopers.contains(e.getPlayer().getName())) {
 			snoopers.remove(e.getPlayer().getName());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerQuit(PlayerQuitEvent e) {
 		if (backpackusers.contains(e.getPlayer().getName())) {
 			final String invTitle = e.getPlayer().getOpenInventory().getTitle();
 			if (!invTitle.equalsIgnoreCase("backpack") && !invTitle.equalsIgnoreCase("satchel")) return;
 			closePack(e.getPlayer().getName(), e.getPlayer().getOpenInventory().getTopInventory());
 		}
 		
 		if (snoopers.contains(e.getPlayer().getName())) {
 			snoopers.remove(e.getPlayer().getName());
 		}
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent e) {
 		final Player p = e.getEntity();
 		
 		if (backpackusers.contains(p.getName())) {
 			final String invTitle = p.getOpenInventory().getTitle();
 			if (!invTitle.equalsIgnoreCase("backpack") && !invTitle.equalsIgnoreCase("satchel")) return;
 			closePack(e.getEntity().getName(), p.getOpenInventory().getTopInventory());
 		}
 		
 		if (snoopers.contains(p.getName())) {
 			snoopers.remove(p.getName());
 		}
 		
 		if (!getPackIsProtected(p)) {
 			final ItemStack[] iss = backpacks.get(p.getName());
 			for (int i = 0; i < iss.length; i++) {
 				if (iss[i] == null) continue;
 				p.getWorld().dropItemNaturally(p.getLocation(), iss[i]);
 			}
 			
 			clearPack(p.getName());
 		}
 	}
 	
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent e) {
 		
 		if (snoopers.contains(e.getWhoClicked().getName())) {
 			e.setCancelled(true);
 		}
 	}
 	
 	
 	private static int getPackSize(Player p) {
 		int size = 0;
 		for (int i = 1; i <= MAX_INVENTORY_ROWS; i++) {
 			if (p.hasPermission("ec.backpack.size." + i)) size = i * INVENTORY_ROW_SIZE;
 		}
 		return size;
 	}
 	
 	private static boolean getPackIsProtected(Player p) {
 		return p.hasPermission("ec.backpack.protected");
 	}
 	
 	private static void clearPack(String p) {
 		if (backpackusers.contains(p)) {
 			Bukkit.getPlayerExact(p).closeInventory();
 		}
 		
 		updatePack(p, new ItemStack[0]);
 	}
 	
 	private static void setInventoryContents(Inventory inv, ItemStack[] iss) {
 		if (inv.getSize() >= iss.length) {
 			inv.setContents(iss);
 		} else {
 			int index = 0;
 			for (int i = 0; i < iss.length; i++) {
 				if (iss[i] != null) {
 					inv.setItem(i, iss[i]);
 					index++;
 				}
 				
 				if (index == inv.getSize()) break;
 			}
 		}
 	}
	
	public static void shutdown() {
		for (Player p : Bukkit.getOnlinePlayers()) {
			if (backpackusers.contains(p.getName())) {
				if (p.getOpenInventory().getTitle().equalsIgnoreCase("Satchel")
					|| p.getOpenInventory().getTitle().equalsIgnoreCase("Backpack")) {
						closePack(p.getName(), p.getOpenInventory().getTopInventory());
						p.closeInventory();
					}
			}
		}
		sql.close();
	}
 }
