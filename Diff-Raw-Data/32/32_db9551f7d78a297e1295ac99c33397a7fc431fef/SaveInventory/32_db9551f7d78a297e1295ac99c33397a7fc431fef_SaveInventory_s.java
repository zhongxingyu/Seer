 package saveinventory;
 
 import java.io.BufferedOutputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.HashMap;
 import java.util.zip.GZIPOutputStream;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Material;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.inventory.InventoryClickEvent;
 import org.bukkit.event.inventory.InventoryCloseEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class SaveInventory extends JavaPlugin implements Listener {
 
 	private static SaveInventory instance;
 	private File folderPlayerData;
 	private HashMap<String, PlayerInfo> currentViewers;
 	private int interval;
 	private String resetAfter;
 	private GregorianCalendar lastReset;
 	private GregorianCalendar nextReset;
 	private SimpleDateFormat fileDateFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm");
 	private SimpleDateFormat resetDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
 
 	@Override
 	public void onDisable() {
 		this.getLogger().info("disabled.");
 	}
 
 	@Override
 	public void onEnable() {
 		instance = this;
 
 		this.currentViewers = new HashMap<String, PlayerInfo>();
 		if (!this.getDataFolder().exists()) {
 			this.getDataFolder().mkdir();
 		}
 
 		this.folderPlayerData = new File(this.getDataFolder(), "playerdata");
 		if (!this.folderPlayerData.exists()) {
 			this.folderPlayerData.mkdir();
 		}
 
 		this.loadConfig();
 
 		this.getServer().getPluginManager().registerEvents(this, this);
 
 		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 			@Override
 			public void run() {
 				saveInventories();
 				removeOldInventories();
 				Bukkit.getServer().broadcastMessage("Inventories saved.");
 			}
 		}, 60, this.interval);
 
 		this.getLogger().info("enabled.");
 	}
 
 	public boolean removeViewer(String admin) {
 		if (this.currentViewers.containsKey(admin)) {
 			this.currentViewers.remove(admin);
 			return true;
 		}
 		return false;
 	}
 
 	public File getPlayerDataFolder() {
 		return this.folderPlayerData;
 	}
 
 	public static SaveInventory getInstance() {
 		return instance;
 	}
 
 	public void saveItemStackArray(Player player, String dateTime) throws FileNotFoundException, IOException {
 		File filePlayerFolder = new File(this.folderPlayerData, player.getName());
 		if (!filePlayerFolder.exists()) {
 			filePlayerFolder.mkdir();
 		}
 
		File fileInv = new File(filePlayerFolder, dateTime + "\\.inv");
 		BufferedOutputStream out = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(fileInv)));
 
 		YamlConfiguration yamlInventory = new YamlConfiguration();
 		yamlInventory.set("inventory", ItemParser.getHashMapFromItemStackArray(player.getInventory().getContents()));
 		yamlInventory.set("armor", ItemParser.getHashMapFromItemStackArray(player.getInventory().getArmorContents()));
 
 		out.write(yamlInventory.saveToString().getBytes());
 		out.flush();
 		out.close();
 	}
 
 	public void saveInventories() {
 		Date date = new Date();
 
 		for (Player player : this.getServer().getOnlinePlayers()) {
 			try {
 				this.saveItemStackArray(player, this.fileDateFormat.format(date));
 			} catch (FileNotFoundException e) {
 				e.printStackTrace();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	public void removeOldInventories(){
 		// check if time has passed
 		GregorianCalendar g = new GregorianCalendar();
 		if (g.before(this.nextReset))
 			return;
 		for (String playername : this.folderPlayerData.list()){
 			File playerfolder = new File(this.folderPlayerData,playername);
 			if (!playerfolder.isDirectory())
 				continue;
 			for (String inventory : playerfolder.list()){
 				if (!inventory.endsWith("\\.inv"))
 					continue;
 				// check if older than reset time
 				GregorianCalendar fileDate = new GregorianCalendar();
 				try {
 					fileDate.setTime(this.fileDateFormat.parse(inventory.replace("\\.inv","")));
 				} catch (ParseException e) {
 					continue;
 				}
 				
 				if (fileDate.after(this.nextReset)){
 					File invFile = new File(playerfolder,inventory);
 					invFile.delete();
 				}
 			}
 			
 			// check, if folder is empty
 			if (playerfolder.list().length == 0){
 				playerfolder.delete();
 			}
 		
 		}
 		
 	}
 
 	public void loadConfig() {
 
 		YamlConfiguration yml = new YamlConfiguration();
 		File configFile = new File(this.getDataFolder(), "config.yml");
 		if (!configFile.exists()) {
 			this.interval = 6000;
 			String deleteAfterString = "3d";
 			this.lastReset = new GregorianCalendar();
 			this.nextReset = new GregorianCalendar();
 			this.nextReset.add(GregorianCalendar.DAY_OF_YEAR, 3);
 			try {
 				yml.set("interval", this.interval);
 				yml.set("deleteAfter", deleteAfterString);
				yml.set("lastReset", this.resetDateFormat.format(this.lastReset));
 				yml.save(configFile);
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 			return;
 		}
 
 		try {
 			yml.load(configFile);
 			
 			this.interval = yml.getInt("interval");
 			if (this.interval < 1200){
 				this.interval = 1200;
 				yml.set("interval", this.interval);
 				yml.save(configFile);
 				this.getLogger().warning("Interval for saving should be at least 1 min. Changed accordingly.");
 			}
 			
 			this.resetAfter = yml.getString("deleteAfter");
 			if (!this.resetAfter.matches("\\d+(h|d|w|M)")){
 				this.resetAfter = "3d";
 				yml.save(configFile);
 				this.getLogger().warning("Could not interpret how long to keep files. Use format <Number><Duration>");
 				this.getLogger().warning("use only h (hours), d (days), w (weeks) and M (Month)");
 			}
 
 			try {
 				this.lastReset = new GregorianCalendar();
 				this.lastReset.setTime(this.resetDateFormat.parse(yml.getString("lastReset")));
 			} catch (ParseException e) {
 				this.lastReset = new GregorianCalendar();
				yml.set("lastReset", this.resetDateFormat.format(this.lastReset));
 				yml.save(configFile);
 			}
 			this.nextReset = calculateNextReset(this.resetAfter);
 
 			
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (InvalidConfigurationException e) {
 			e.printStackTrace();
 		}
 
 	}
 	
 	public GregorianCalendar calculateNextReset(String s){
 		int i = Integer.parseInt(s.substring(0, s.length()-1));
 		String t = s.substring(s.length()-1, s.length());
 		GregorianCalendar g = new GregorianCalendar();
 		if (t.equals("h")){
 			g.add(GregorianCalendar.HOUR_OF_DAY, i);
 		} else if(t.equals("d")){
 			g.add(GregorianCalendar.DAY_OF_YEAR, i);
 		} else if(t.equals("w")){
 			g.add(GregorianCalendar.WEEK_OF_YEAR, i);
 		} else if(t.equals("M")){
 			g.add(GregorianCalendar.MONTH, i);
 		}
 		
 		return g;
 	}
 
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		if (!(sender instanceof Player)) {
 			sender.sendMessage("Only a player can use that command.");
 			return true;
 		}
 
 		Player player = (Player) sender;
 
 		if (cmd.getName().equalsIgnoreCase("show")) {
 			if (args.length == 0) {
 				player.sendMessage("/show inv <player name>");
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("inv")) {
 
 				String admin = player.getName();
 				String inventoryOwner = args[1];
 				PlayerInfo adminInfo;
 				if (!this.currentViewers.containsKey(admin)) {
 					adminInfo = new PlayerInfo(admin, inventoryOwner);
 					if (!adminInfo.getPlayerFolder().exists())
 						return true;
 					this.currentViewers.put(admin, adminInfo);
 				} else {
 					adminInfo = this.currentViewers.get(admin);
 				}
 
 				Inventory inv = adminInfo.loadInventory();
 				if (inv == null)
 					return true;
 				player.openInventory(inv);
 				return true;
 			}
 
 			if (args[0].equalsIgnoreCase("logout")) {
 				if (!this.currentViewers.containsKey(player.getName())) {
 					return true;
 				}
 				this.currentViewers.remove(player.getName());
 			}
 
 			if (args[0].equalsIgnoreCase("give")) {
 				if (!this.currentViewers.containsKey(player.getName())) {
 					return true;
 				}
 
 				PlayerInfo adminInfo = this.currentViewers.get(player.getName());
 				// check, if player is online
 				Player inventoryOwner = Bukkit.getPlayer(adminInfo.getInventoryOwner());
 				if (inventoryOwner == null) {
 					player.sendMessage("Spieler momentan nicht online.");
 					return true;
 				}
 				// check if empty inventory
 				boolean isempty = true;
 				for (ItemStack item : inventoryOwner.getInventory().getContents()) {
 					if (item != null && item.getType() != Material.AIR) {
 						isempty = false;
 						break;
 					}
 				}
 				for (ItemStack item : inventoryOwner.getInventory().getArmorContents()) {
 					if (item != null && item.getType() != Material.AIR) {
 						isempty = false;
 						break;
 					}
 				}
 
 				if (!isempty) {
 					player.sendMessage("Spielerinventar ist nicht leer, kann es nicht ersetzen. Bitte leeren.");
 					inventoryOwner.sendMessage("Ein Admin mchte dir ein altes Inventar zurckgeben, bitte leere dein momentanes Inventar.");
 					return true;
 				}
 				if (adminInfo.getLastArmor() == null || adminInfo.getLastInventory() == null) {
 					player.sendMessage("Kein Spielerinventar ausgewhlt.");
 					return true;
 				}
 				inventoryOwner.getInventory().setArmorContents(adminInfo.getLastArmor());
 				inventoryOwner.getInventory().setContents(adminInfo.getLastInventory());
 				player.sendMessage("Spielerinventar von " + inventoryOwner.getName() + " wurde gsetzt.");
 				inventoryOwner.sendMessage("Dein altes Inventar wurde dir von " + adminInfo.getAdmin() + " zurckgegeben.");
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	@EventHandler
 	public void onInventoryClose(InventoryCloseEvent event) {
 		if (!(event.getPlayer() instanceof Player)) {
 			return;
 		}
 
 		Player player = (Player) event.getPlayer();
 		if (!this.currentViewers.containsKey(player.getName())) {
 			return;
 		}
 
 		if (!event.getInventory().getTitle().contains("SaveInventory:")) {
 			return;
 		}
 
 		// ask if the last viewed inventory should be given to the player
 		player.sendMessage("Zum Ausloggen bitte /show logout eingeben.");
 		player.sendMessage("Falls der Spieler das letzte angesehene Inventar erhalten soll bitte /show give eingeben.");
 	}
 
 	@EventHandler
 	public void onInventoryClick(InventoryClickEvent event) {
 		if (!(event.getWhoClicked() instanceof Player)) {
 			return;
 		}
 
 		Player player = (Player) event.getWhoClicked();
 		// check if player is a viewer
 		if (!this.currentViewers.containsKey(player.getName())) {
 			return;
 		}
 
 		if (!event.getInventory().getTitle().contains("SaveInventory:")) {
 			return;
 		}
 
 		PlayerInfo admin = this.currentViewers.get(player.getName());
 
 		event.setCancelled(true);
 
 		// check if klicked on slotnr 7 or 8
 		if (event.getSlot() == 6 && admin.hasPreviousInventory()) {
 			admin.setPreviousInventory();
 		} else if (event.getSlot() == 8 && admin.hasNextInventory()) {
 			admin.setNextInventory();
 		} else {
 			return;
 		}
 
 		Inventory inv = admin.loadInventory();
 		if (inv == null) {
 			return;
 		}
 		event.getInventory().setContents(inv.getContents());
 	}
 }
