 package me.rotzloch.Classes;
 
 import java.io.File;
 import java.sql.ResultSet;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.World;
 import org.bukkit.command.CommandException;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 
 import com.sk89q.worldedit.BlockVector;
 import com.sk89q.worldedit.bukkit.WorldEditPlugin;
 import com.sk89q.worldguard.LocalPlayer;
 import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
 import com.sk89q.worldguard.protection.managers.RegionManager;
 
 import lib.PatPeter.SQLibrary.MySQL;
 import lib.PatPeter.SQLibrary.SQLite;
 import me.rotzloch.main.MaRoCraft;
 import net.milkbowl.vault.economy.Economy;
 
 
 public class Helper {
 	private static Logger _mcLog;
 	private static Economy _econ;
 	private static PluginManager _pm;
 	private static MaRoCraft _plugin;
 	private static String _serverAccount = "_MaRoCraft-Bank_";
 	private static Tax _tax;
 	public static MySQL _farmonatorMysql;
 	public static MySQL _rewardMysql;
 	public static SQLite _farmonatorSQLLite;
 	public static SQLite _rewardSQLLite;
 	public static Map<String, Integer> _blocksBreaked;
 	public static Integer _blocksBreakTaskId;
 	private static Map<String, Farm> _regions;
 	private static List<RewardLock> _rewardLocks;
 	private static File pFolder = new File("plugins" + File.separator + "MaRo-Craft");
 
 	//Public Methods
 	public static boolean setPlugin(MaRoCraft plugin) {
 		Helper._plugin = plugin;
 		Helper._pm = Helper._plugin.getServer().getPluginManager();
 		Helper._mcLog = Helper._plugin.getLogger();
 		Helper._blocksBreaked = new HashMap<String, Integer>();
 		return Helper.setupEconomy();
 	}
 	public static void LoadConfig() {
 		Helper.Config().addDefault("config.Land.Enabled", true);
 		Helper.Config().addDefault("config.Land.BuyPrice", 200);
 		Helper.Config().addDefault("config.Land.AddBuyPricePerGS", 50);
 		Helper.Config().addDefault("config.Land.TaxEnabled", true);
 		Helper.Config().addDefault("config.Land.TaxPerGS", 0.1);
 		Helper.Config().addDefault("config.Land.TaxTimeSeconds", 900);
 		Helper.Config().addDefault("config.Land.SellBySign", true);
 		Helper.Config().addDefault("config.MoneyPerBlock.Enabled", true);
 		Helper.Config().addDefault("config.MoneyPerBlock.Amount", 0.05);
 		Helper.Config().addDefault("config.MoneyPerBlock.Minutes", 5);
 		Helper.Config().addDefault("config.ItemStacker.Enabled", true);
 		Helper.Config().addDefault("config.ItemStacker.Radius", 5);
 		Helper.Config().addDefault("config.ItemStacker.Normal", true);
 		Helper.Config().addDefault("config.ItemStacker.ItemPerStack",256);
 		Helper.Config().addDefault("config.AutoReplant.Enabled", true);
 		Helper.Config().addDefault("config.AutoReplant.ReplantTicksWait", 40);
 		Helper.Config().addDefault("config.Reward.Enabled", false);
 		Helper.Config().addDefault("config.Reward.db.MySql", false);
 		Helper.Config().addDefault("config.Reward.db.Hostname", "localhost");
 		Helper.Config().addDefault("config.Reward.db.Database", "marocraft");
 		Helper.Config().addDefault("config.Reward.db.Port", "3306");
 		Helper.Config().addDefault("config.Reward.db.User", "USERNAME");
 		Helper.Config().addDefault("config.Reward.db.Password", "PASSWORD");
 		Helper.Config().addDefault("config.Farmonator.Enabled", false);
 		Helper.Config().addDefault("config.Farmonator.db.MySql", false);
 		Helper.Config().addDefault("config.Farmonator.db.Hostname", "localhost");
 		Helper.Config().addDefault("config.Farmonator.db.Database", "marocraft");
 		Helper.Config().addDefault("config.Farmonator.db.Port", "3306");
 		Helper.Config().addDefault("config.Farmonator.db.User", "USERNAME");
 		Helper.Config().addDefault("config.Farmonator.db.Password", "PASSWORD");
		List<String> emptyList = new ArrayList<String>();
		Helper.Config().addDefault("config.Land.IgnoreWorlds", emptyList);
 		Helper.Config().options().copyDefaults(true);
 		Helper._plugin.saveConfig();
 	}
 	public static FileConfiguration Config() {
 		return Helper._plugin.getConfig();
 	}
 	public static int StartAsyncTask(Runnable run, long timeTicks){
 		return Helper._plugin.getServer().getScheduler().runTaskTimerAsynchronously(Helper._plugin, run, timeTicks, timeTicks).getTaskId();
 	}
 	public static void StartDelayedTask(Runnable run, long timeTicks) {
 		Helper._plugin.getServer().getScheduler().runTaskLater(Helper._plugin, run, timeTicks);
 	}
 	public static void StopAsyncTask(int TaskId) {
 		Helper._plugin.getServer().getScheduler().cancelTask(TaskId);
 	}
 	public static List<World> GetWorlds() {
 		return Helper._plugin.getServer().getWorlds();
 	}
 	public static void BroadcastMessage(String message) {
 		Helper._plugin.getServer().broadcastMessage(message);
 	}
 	public static void RegisterListener(Listener listener) {
 		Helper._pm.registerEvents(listener, Helper._plugin);
 	}
 	public static World GetWorld(String name) {
 		return Helper._plugin.getServer().getWorld(name);
 	}
 	
 	public static void StartMoneyPerBlockPay(){
 		long ticks = Helper.Config().getLong("config.MoneyPerBlock.Minutes") * 60;
 		ticks = ticks * 20;
 		MoneyPerPlayer mpp = new MoneyPerPlayer();
 		Helper._blocksBreakTaskId = Helper.StartAsyncTask(mpp,ticks);
 	}
 	public static void CalcMoney(Map<String, Integer> blocksBreaked){
 		for (Map.Entry<String, Integer> entry : blocksBreaked.entrySet()) {
 			double pay = Helper.BlockBreakMoney()*entry.getValue();
 			pay = pay * 100;
 			pay = Math.round(pay);
 			pay = pay / 100;
 			
 			if(Helper.HasPlayerAccountAndEnoughBalance(entry.getKey(), 0)) {
 				Helper.PayToTarget(null, entry.getKey(), pay);
 			}
 			Player player = Helper.GetPlayer(entry.getKey());
 			if(player != null) {
 				Helper.SendMessageInfo(player, "Du hast in den letzten " +Helper.Config().getLong("config.MoneyPerBlock.Minutes")+ " Minuten, "+
 						pay +" "+Helper.GetCurrency()+ " verdient.");
 			}
 		}
 	}
 	
 	//Logger
 	public static void LogInfo(String message) {
 		Helper._mcLog.log(Level.INFO, message);
 	}
 	public static void LogError(String message) {
 		Helper._mcLog.log(Level.SEVERE, message);
 	}
 	
 	//Economy
 	public static String GetCurrency(){
 		if(Helper._econ != null){
 			return Helper._econ.currencyNamePlural();
 		}
 		return "";
 	}
 	private static boolean setupEconomy() {
 		if (Helper._pm.getPlugin("Vault") == null) {
 			Helper._pm.disablePlugin(Helper._plugin);
 			return false;
 		}
 		RegisteredServiceProvider<Economy> rsp = Helper._plugin.getServer()
 				.getServicesManager().getRegistration(Economy.class);
 		if (rsp == null) {
 			return false;
 		}
 		Helper._econ = rsp.getProvider();
 		return Helper._econ != null;
 	}
 	public static boolean HasPlayerAccountAndEnoughBalance(String name,
 			double price) {
 		boolean returnvalue = false;
 
 		if (!Helper._econ.hasAccount(name)) {
 			Helper._econ.createPlayerAccount(name);
 		}
 		if (!Helper._econ.hasAccount(Helper._serverAccount)) {
 			Helper._econ.createPlayerAccount(Helper._serverAccount);
 		}
 		if (Helper._econ.getBalance(name) >= price) {
 			returnvalue = true;
 		}
 
 		return returnvalue;
 	}
 	public static void PayToTarget(String name, String targetName, double price) {
 		if(name != null){
 			Helper._econ.bankWithdraw(name, price);
 		}
 		if(targetName != null) {
 			Helper._econ.bankDeposit(targetName, price);
 		}
 	}
 	
 	//WorldEdit/WorldGuard
 	public static WorldEditPlugin getWorldEdit() throws CommandException {
 		Plugin worldEdit = Helper._pm.getPlugin("WorldEdit");
 		if (worldEdit == null) {
 			throw new CommandException(
 					"WorldEdit does not appear to be installed.");
 		}
 
 		if ((worldEdit instanceof WorldEditPlugin)) {
 			return (WorldEditPlugin) worldEdit;
 		}
 		throw new CommandException("WorldEdit detection failed (report error).");
 	}
 	public static WorldGuardPlugin getWorldGuard() throws CommandException {
 		Plugin worldGuard = Helper._pm.getPlugin("WorldGuard");
 		if (worldGuard == null) {
 			throw new CommandException(
 					"WorldGuard does not appear to be installed.");
 		}
 
 		if ((worldGuard instanceof WorldGuardPlugin)) {
 			return (WorldGuardPlugin) worldGuard;
 		}
 		throw new CommandException(
 				"WorldGuard detection failed (report error).");
 	}
 	public static BlockVector getMinimumWedit(Player player) {
 		if (Helper.getWorldEdit().getSelection(player) != null) {
 			return Helper.getWorldEdit().getSelection(player)
 					.getNativeMinimumPoint().toBlockVector();
 		}
 		return null;
 	}
 	public static BlockVector getMaximumWedit(Player player) {
 		if (Helper.getWorldEdit().getSelection(player) != null) {
 			return Helper.getWorldEdit().getSelection(player)
 					.getNativeMaximumPoint().toBlockVector();
 		}
 		return null;
 	}
 	
 	//SendMessage To Player
 	public static Player GetPlayer(String name){
 		return Helper._plugin.getServer().getPlayer(name);
 	}
 	private static boolean isPlayerOnline(Player player) {
 		if(Helper.GetPlayer(player.getName()) != null) {
 			return true;
 		}
 		return false;
 	}
 	public static void SendMessageInfo(Player player, String message) {
 		if(Helper.isPlayerOnline(player)) {
 			player.sendMessage(ChatColor.GREEN + message);
 		}
 	}
 	public static void SendMessageError(Player player, String message) {
 		if(Helper.isPlayerOnline(player)) {
 			player.sendMessage(ChatColor.RED + message);
 		}
 	}
 	public static void SendMessageNoPermission(Player player) {
 		if(Helper.isPlayerOnline(player)) {
 			player.sendMessage(ChatColor.RED +Text.NoPermission);
 		}
 	}
 	
 	//Help
 	public static void Help(Player player, String command) {
 		if(command.equalsIgnoreCase("land")) {
 			player.sendMessage(Text.LandHelp());
 		}
 		else if(command.equalsIgnoreCase("farm")) {
 			player.sendMessage(Text.FarmHelp());
 		}
 	}
 	
 	//TaxHelper
 	public static void StartTax() {
 		Helper._tax = new Tax();
 		Helper._tax.StartTax();
 	}
 	public static Player[] GetOnlinePlayers(){
 		return Helper._plugin.getServer().getOnlinePlayers();
 	}
 	public static int CountGS(Player player) {
 		int count = 0;
 		LocalPlayer localPlayer = Helper.getWorldGuard().wrapPlayer(player);
 		for (World world : Helper._plugin.getServer().getWorlds()) {
 			RegionManager regionManager = Helper.getWorldGuard()
 					.getRegionManager(world);
 				count += regionManager.getRegionCountOfPlayer(localPlayer);
 		}
 		return count;
 	}
 	public static String ServerAccount() {
 		return Helper._serverAccount;
 	}
 	
 	//ItemStacker
 	public static int ItemStackerRadius() {
 		return Helper.Config().getInt("config.ItemStacker.Radius");
 	}
 	public static int ItemStackerItemsPerStack() {
 		return Helper.Config().getInt("config.ItemStacker.ItemPerStack");
 	}
 	public static boolean ItemStackerNormal() {
 		return Helper.Config().getBoolean("config.ItemStacker.Normal");
 	}
 
 	//BlockBreakMoney
 	public static double BlockBreakMoney() {
 		return Helper.Config().getDouble("config.MoneyPerBlock.Amount");
 	}
 
 	//Farmonator
 	public static Map<String, Farm> FarmDB() {
 		return Helper._regions;
 	}
 	public static void LoadFarmonatorFromDB() {
 		ResultSet result = null;
 		String query = "SELECT * FROM farmonator";
 		Helper._regions = new HashMap<String, Farm>();
 		try {
 				if(Helper._farmonatorMysql != null) {
 					result = Helper._farmonatorMysql.query(query);
 				} else if(Helper._farmonatorSQLLite != null){
 					result = Helper._farmonatorSQLLite.query(query);
 				}
 				
 				if(result != null){
 		
 					while(result.next()) {
 						Helper._regions.put(result.getString(1), new Farm(result));
 					}
 					result.close();
 				}
 			} catch(Exception e){
 			Helper.LogError(e.getMessage());
 		}
 	}
 	public static void setFarm(String id, Farm region) {
         Helper._regions.put(id, region);
     }
 
 	//MySQL or SQLite
 	public static boolean MySQLInit(String host, int port, String data, String user, String pass, String TableName) {		
 		String MySQLTable = TableName.toLowerCase();
 		if(TableName.equalsIgnoreCase("Farmonator")) {
 			String query = "CREATE TABLE IF NOT EXISTS `"+MySQLTable+"` (`id` varchar(50) NOT NULL,`world` varchar(30) NOT NULL,";
 			query += "`player` varchar(50) NOT NULL,`itemId` int(4) NOT NULL,`itemDamage` int(2) NOT NULL,`minPointX` double NOT NULL,";
 			query += "`minPointY` double NOT NULL,`minPointZ` double NOT NULL,`maxPointX` double NOT NULL,`maxPointY` double NOT NULL,";
 			query += "`maxPointZ` double NOT NULL,`truheX` double NOT NULL,`truheY` double NOT NULL,`truheZ` double NOT NULL,";
 			query += "PRIMARY KEY (`id`));";
 			if(Helper.Config().getBoolean("config.Farmonator.db.MySql")){
 				Helper._farmonatorMysql = new MySQL(Helper._mcLog,"",host,port,data,user,pass);
 				
 				try{
 					Helper._farmonatorMysql.open();
 					if(Helper._farmonatorMysql.isOpen()){
 						if(!Helper._farmonatorMysql.isTable(MySQLTable)){
 							Helper.LogInfo("Creating "+MySQLTable+" Table");
 							Helper._farmonatorMysql.query(query);
 						}
 					} else {
 						Helper.LogError("MySql Connection failed!");
 					}
 				} catch(Exception e){
 					Helper.LogError(e.getMessage());
 					return false;
 				}
 			}
 			else{
 				Helper._farmonatorSQLLite = new SQLite(Helper._mcLog,MySQLTable,MySQLTable,pFolder.getPath());
 				try{
 					Helper._farmonatorSQLLite.open();
 					if(!Helper._farmonatorSQLLite.isTable(MySQLTable)){
 						Helper.LogInfo("Creating "+MySQLTable+" Table");
 						Helper._farmonatorSQLLite.query(query);
 					}
 				} catch(Exception e){
 					Helper.LogError(e.getMessage());
 					return false;
 				}
 				
 				
 			}
 			return true;
 			
 		} else if(TableName.equalsIgnoreCase("Reward")) {
 			String query = "CREATE TABLE IF NOT EXISTS `"+MySQLTable+"` (`id` varchar(255) NOT NULL,";
 			query += "`playername` varchar(255) NOT NULL,`timelock` varchar(255) NOT NULL";
 			query += ");";
 			if(Helper.Config().getBoolean("config.Reward.db.MySql")){
 				Helper._rewardMysql = new MySQL(Helper._mcLog,MySQLTable,host,port,data,user,pass);
 				try{
 					Helper._rewardMysql.open();
 					if(Helper._rewardMysql.isOpen()){
 						if(!Helper._rewardMysql.isTable(MySQLTable)){
 							Helper.LogInfo("Creating "+MySQLTable+" Table");
 							Helper._rewardMysql.query(query);
 						}
 					} else {
 						Helper.LogError("MySql Connection failed!");
 						return false;
 					}
 				} catch(Exception e){
 					Helper.LogError(e.getMessage());
 					return false;
 				}
 				
 			}
 			else{
 				Helper._rewardSQLLite = new SQLite(Helper._mcLog,MySQLTable,MySQLTable,pFolder.getPath());
 				try{
 					Helper._rewardSQLLite.open();
 					if(!Helper._rewardSQLLite.isTable(MySQLTable)){
 						Helper.LogInfo("Creating "+MySQLTable+" Table");
 						Helper._rewardSQLLite.query(query);
 					}
 				} catch(Exception e){
 					Helper.LogError(e.getMessage());
 					return false;
 				}
 				
 				
 			}
 			return true;
 		}
 		return false;
 	}	
 	
 	//Reward
 	public static RewardLock RewardLocks(String ID, String playername) {
 		List<RewardLock> list = Helper._rewardLocks;
 		RewardLock lock = null;
 		for(RewardLock rewardLock : list) {
 			if(rewardLock.RewardID().equalsIgnoreCase(ID) && rewardLock.RewardPlayer().equalsIgnoreCase(playername)) {
 				lock = rewardLock;
 				break;
 			}
 		}
 		if(lock != null) {
 			Date date = new Date();
 			Date dateLock = new Date(lock.RewardLockTill());
 			if(date.after(dateLock)) {
 				Helper.RemoveRewardLock(lock);
 			} else  {
 				return lock;
 			}
 		}
 		return null;
 	}
 	public static void AddRewardLock(RewardLock lock) {
 		Helper._rewardLocks.add(lock);
 		String[] columns = {"(id,","playername,","timelock)"};
 		String[] values = {"","",""};
 		String table = "reward";
 		
 		values[0] = "('"+lock.RewardID()+"',";
 		values[1] = "'"+lock.RewardPlayer()+"',";
 		values[2] = "'"+lock.RewardLockTill()+"')";
 		
 		String query = "INSERT INTO " + table;
 		for(String column:columns){
 			query += column;
 		}
 		query += "VALUES ";
 		
 		for(String value:values){
 			query += value;
 		}
 		
 		try {
 			ResultSet result = null;
 			if(Helper._rewardMysql != null){
 				result = Helper._rewardMysql.query(query);
 			} else if (Helper._rewardSQLLite != null) {
 				result = Helper._rewardSQLLite.query(query);
 			}
 			if(result != null){
 				result.close();
 			}
 		} catch(Exception e){
 			Helper.LogError(e.getMessage());
 		}
 	}
 	public static void RemoveRewardLock(RewardLock lock) {
 		Helper._rewardLocks.remove(lock);
 		String query = "DELETE FROM reward WHERE ";
 		query += "id = '"+lock.RewardID()+"' AND ";
 		query += "playername = '"+lock.RewardPlayer() + "' AND ";
 		query += "timelock = '" +lock.RewardLockTill()+ "'";
 		
 		try {
 			ResultSet result = null;
 			if(Helper._rewardMysql != null){
 				result = Helper._rewardMysql.query(query);
 			} else if (Helper._rewardSQLLite != null) {
 				result = Helper._rewardSQLLite.query(query);
 			}
 			if(result != null){
 				result.close();
 			}
 		} catch(Exception e){
 			Helper.LogError(e.getMessage());
 		}
 	}
 	public static void LoadRewardLocks() {
 		ResultSet result = null;
 		String query = "SELECT playername,timelock,id FROM reward";
 		Helper._rewardLocks = new ArrayList<RewardLock>();
 		try {
 				if(Helper._rewardMysql != null) {
 					result = Helper._rewardMysql.query(query);
 				} else if(Helper._rewardSQLLite != null){
 					result = Helper._rewardSQLLite.query(query);
 				}
 				
 				if(result != null){
 		
 					while(result.next()) {
 						Helper._rewardLocks.add(new RewardLock(result.getString(1), result.getString(2), result.getString(3)));
 					}
 					result.close();
 				}
 			} catch(Exception e){
 			Helper.LogError(e.getMessage());
 		}
 	}
 }
