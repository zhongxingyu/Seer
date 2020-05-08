 package eu.icecraft.xrayalerter;
 
 import java.io.File;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.util.config.Configuration;
 
 import com.ensifera.animosity.craftirc.CraftIRC;
 import com.nijikokun.bukkit.Permissions.Permissions;
 
 public class XrayAlerter extends JavaPlugin {
 
 	public Map<String, XRAPlayerData> playerData = new HashMap<String, XRAPlayerData>();
 	private Permissions perm;
 	private CraftIRC craftircHandle;
 	private boolean hasCraftIrc = false;
 	private boolean UseOP = false;
 	private Configuration log;
 	private Configuration conf;
 	private List<Integer> watchOres;
 
 	@Override
 	public void onDisable() {
 		log.save();
 		System.out.println(this.getDescription().getName() + " " + this.getDescription().getVersion() + " was disabled!");
 	}
 
 	@Override
 	public void onEnable() {
 
 		Plugin p = this.getServer().getPluginManager().getPlugin("Permissions");
 
 		if (p != null) {
 			if (!this.getServer().getPluginManager().isPluginEnabled(p)) {
 				this.getServer().getPluginManager().enablePlugin(p);
 			}
 			perm = (Permissions) p;
 		} else {
 			UseOP  = true;
 		}
 
 		Plugin checkplugin = this.getServer().getPluginManager().getPlugin("CraftIRC");
 		if (checkplugin == null || !checkplugin.isEnabled()) {
 			System.out.println("[IceXRA] CraftIRC not found");
 		} else {
 			System.out.println("[IceXRA] CraftIRC found");
 			craftircHandle = (CraftIRC) checkplugin;
 			hasCraftIrc = true;
 		}
 
 		XRABlockListener listener = new XRABlockListener(this);
 		this.getServer().getPluginManager().registerEvent(Type.BLOCK_BREAK, listener, Priority.Monitor, this);
 
 		XRAPlayerListener plListener = new XRAPlayerListener(this);
 		this.getServer().getPluginManager().registerEvent(Type.PLAYER_QUIT, plListener, Priority.Monitor, this);
 		
 		if(!this.getDataFolder().exists()) this.getDataFolder().mkdir();
 		File logFile = new File(this.getDataFolder(), "log.yml");
 		log = new Configuration(logFile);
 		if(!logFile.exists()) log.save();
 		log.load();
 
 		File confFile = new File(this.getDataFolder(), "config.yml");
 		conf = new Configuration(confFile);
 		if(!confFile.exists()) {
 			conf.setProperty("minY", 50);
 			conf.setProperty("minLightLevel", 4);
 			conf.setProperty("warnAfter", 6);
 			conf.setProperty("watchMinutes", 10);
 			conf.setProperty("craftIRCtag", "minecraft");
 			
			int[] oreIDs = new int[5];
			oreIDs[0] = Material.IRON_ORE.getId();
			oreIDs[1] = Material.GOLD_ORE.getId();
			oreIDs[2] = Material.DIAMOND_ORE.getId();
			oreIDs[3] = Material.LAPIS_ORE.getId();
			oreIDs[4] = Material.REDSTONE_ORE.getId();
 			conf.setProperty("watchOres", oreIDs);
 			
 			conf.save();
 		}
 		
 		log.load();
 		conf.load();
 		
 		this.watchOres = conf.getIntList("watchOres", null);
 		
 		System.out.println(this.getDescription().getName() + " " + this.getDescription().getVersion() + " was loaded sucessfully!");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
 		boolean hasPerm = false;
 		label = command.getName().toLowerCase();
 
 		if (sender instanceof ConsoleCommandSender) {
 			hasPerm = true;
 		} else if (sender instanceof Player) {
 			Player sending = (Player) sender;
 			if(UseOP) hasPerm = sending.isOp();
 			else hasPerm = ((Permissions) perm).getHandler().permission(sending, "icexra.warn");
 		}
 
 		if (hasPerm) {
 			int i = 0;
 			List<String> keys = log.getKeys();
 			for(String pl : keys) {
 				i++;
 				if(keys.size() - i <= 5 || args.length > 0) sender.sendMessage(ChatColor.AQUA + pl + " : " + log.getString(pl));
 			}
 
 		} else {
 			sender.sendMessage(ChatColor.RED + "You don't have permission to do that!");
 		}
 		return true;
 	}
 
 	public void onBreak(BlockBreakEvent event) {
 
 		Block block = event.getBlock();
 		int typeID = block.getTypeId();
 		Player player = event.getPlayer();
 		XRAPlayerData xp = getXRAPlayer(player);
 		
 		int lightlevel = 10;
 
 		ArrayList<Block> target = (ArrayList<Block>) player.getLastTwoTargetBlocks(null, 50);
 		if (target.size() >= 2 &&!target.get(1).getType().equals(Material.matchMaterial("AIR"))) {
 			lightlevel = target.get(0).getLightLevel();
 		}
 
 		if(player.getLocation().getY() < conf.getInt("minY", 50) && lightlevel <= conf.getInt("minLightLevel", 50) && this.watchOres.contains(typeID)) {
 
 			if(xp != null) {
 				xp = getXRAPlayer(player);
 
 				if(xp.getBlockBroken() > conf.getInt("warnAfter", 6) && ((int) (System.currentTimeMillis() / 1000L) - xp.getFirstBreak() < conf.getInt("watchMinutes", 10)*60)) {
 					reportPlayer(player);
 
 					xp.clearBlockBroken();
 					xp.setFirstBreak();
 
 				} else {	
 					xp.incBlockBroken();
 				}
 
 			} else {
 				xp = new XRAPlayerData();
 				playerData.put(player.getName(), xp);
 			}
 
 		}
 
 	}
 
 	private void reportPlayer(Player player) {
 		
 		XRAPlayerData xp = getXRAPlayer(player);
 		int lastWarn = xp.getWarnTime();
 
 		if(lastWarn - (int) (System.currentTimeMillis() / 1000L) > 2*60 || lastWarn == 0) {
 			xp.setWarned();
 
 			logWarn(player);
 
 			String xmsg = ChatColor.RED + "POSSIBLE XRAY: " + ChatColor.AQUA + player.getName() + " at "+ player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ();
 
 			for(Player p : this.getServer().getOnlinePlayers()) {
 
 				boolean hasPerm = false;
 				if(UseOP) hasPerm  = p.isOp();
 				else hasPerm = ((Permissions) perm).getHandler().permission(p, "icexra.warn");
 
 				if(hasPerm) {
 					p.sendMessage(xmsg);
 				}
 			}
 
 			if(hasCraftIrc) craftircHandle.sendMessageToTag(org.jibble.pircbot.Colors.RED + ChatColor.stripColor(xmsg), conf.getString("craftIRCtag", "minecraft"));
 
 		}
 
 	}
 
 	public void logWarn(Player player) {
 
 		Calendar cal = Calendar.getInstance();
 		SimpleDateFormat sdf = new SimpleDateFormat("dd.MM.yyyy HH:mm");
 
 		log.setProperty(player.getName(), player.getLocation().getBlockX() + "," + player.getLocation().getBlockY() + "," + player.getLocation().getBlockZ() + " (" + sdf.format(cal.getTime()) + ")");
 		log.save();
 	}
 
 	public void onQuit(Player player) {
 		playerData.remove(player.getName());
 	}
 	
 	public XRAPlayerData getXRAPlayer(Player player) {
 		if(this.playerData.containsKey(player.getName())) {
 			return this.playerData.get(player.getName());
 		} else {
 			return null;
 		}
 	}
 	
 	public class XRAPlayerData {
 		
 		private int breakTime = 0;
 		private int blockBroken = 0;
 		private int warned = 0;
 
 		public XRAPlayerData() {
 			this.setFirstBreak();
 			this.incBlockBroken();
 		}
 
 		public int getFirstBreak() {
 			return breakTime;
 		}
 
 		public void setFirstBreak() {
 			this.breakTime = (int) (System.currentTimeMillis() / 1000L);
 		}
 		
 		public int getBlockBroken() {
 			return this.blockBroken;
 		}
 		
 		public void incBlockBroken() {
 			this.blockBroken++;
 		}
 		
 		public void clearBlockBroken() {
 			this.blockBroken = 0;
 		}
 		
 		public void setWarned() {
 			this.warned = (int) (System.currentTimeMillis() / 1000L);
 		}
 		
 		public void unsetWarned() {
 			this.warned = 0;
 		}
 		
 		public boolean isWarned() {
 			return this.warned > 0;
 		}
 		
 		public int getWarnTime() {
 			return this.warned;
 		}
 	}
 	
 }
