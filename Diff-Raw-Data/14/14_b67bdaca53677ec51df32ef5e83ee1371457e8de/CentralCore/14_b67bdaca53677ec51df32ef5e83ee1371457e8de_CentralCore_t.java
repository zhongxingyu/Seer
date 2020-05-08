 package net.shiroumi.central;
 
 import java.io.File;
 import java.sql.SQLException;
 import java.util.logging.Logger;
 
 import net.shiroumi.central.Command.CommandRegister;
 import net.shiroumi.central.Command.Player.CmdAFK;
 import net.shiroumi.central.Command.Player.CmdClear;
 import net.shiroumi.central.Command.Player.CmdGamemode;
 import net.shiroumi.central.Command.Player.CmdGive;
 import net.shiroumi.central.Command.Player.CmdHome;
 import net.shiroumi.central.Command.Player.CmdInvisible;
 import net.shiroumi.central.Command.Player.CmdItem;
 import net.shiroumi.central.Command.Player.CmdNoPickup;
 import net.shiroumi.central.Command.Player.CmdSpawn;
 import net.shiroumi.central.Command.Player.CmdThor;
 import net.shiroumi.central.Command.Server.CmdBan;
 import net.shiroumi.central.Command.Server.CmdBroadCast;
 import net.shiroumi.central.Command.Server.CmdKick;
 import net.shiroumi.central.Command.Server.CmdLockDown;
 import net.shiroumi.central.Command.Server.CmdObservation;
 import net.shiroumi.central.Command.Server.CmdOnlinePlayer;
 import net.shiroumi.central.Command.Server.CmdTime;
 import net.shiroumi.central.Command.Server.CmdWeather;
 import net.shiroumi.central.Databases.DatabaseManager;
 import net.shiroumi.central.Databases.DatabaseManager.SQLType;
 import net.shiroumi.central.Databases.SQL;
 import net.shiroumi.central.Databases.SQLiteManager;
 import net.shiroumi.central.Listener.AFKListener;
 import net.shiroumi.central.Listener.LockdownListener;
 import net.shiroumi.central.Listener.PlayerListener;
 import net.shiroumi.central.Util.PluginFeatures;
 import net.shiroumi.central.Util.Util;
 import net.shiroumi.central.Util.i18n;
 import net.shiroumi.central.Worker.AFKWorker;
 import net.shiroumi.central.Worker.NopickupWorker;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import ru.tehkode.permissions.PermissionUser;
 import ru.tehkode.permissions.bukkit.PermissionsEx;
 
 import com.mcbans.firestar.mcbans.MCBans;
 
 public class CentralCore extends JavaPlugin {
 	private static CentralCore Instance;
 	public static Logger log;
 	private static PluginFeatures<MCBans> mcbansFeatures;
 	private static PluginFeatures<PermissionsEx> pexFeatures;
 
 	public CentralCore() {
 		Instance = this;
 	}
 
 	@Override
 	public void onEnable() {
 		log = this.getLogger();
 		CTServer.initialize(this);
 		// Player Commands
 		CommandRegister.Register(new CmdAFK(this));
 		CommandRegister.Register(new CmdClear(this));
 		CommandRegister.Register(new CmdGamemode(this));
 		CommandRegister.Register(new CmdGive(this));
 		CommandRegister.Register(new CmdHome(this));
 		CommandRegister.Register(new CmdInvisible(this));
 		CommandRegister.Register(new CmdItem(this));
 		CommandRegister.Register(new CmdNoPickup(this));
 		CommandRegister.Register(new CmdSpawn(this));
 		CommandRegister.Register(new CmdThor(this));
 		// Server Commands
 		CommandRegister.Register(new CmdBan(this));
 		CommandRegister.Register(new CmdBroadCast(this));
 		CommandRegister.Register(new CmdKick(this));
 		CommandRegister.Register(new CmdLockDown(this));
 		CommandRegister.Register(new CmdObservation(this));
 		CommandRegister.Register(new CmdOnlinePlayer(this));
 		CommandRegister.Register(new CmdTime(this));
 		CommandRegister.Register(new CmdWeather(this));
 
 		new AFKListener(this);
 		new LockdownListener(this);
 		new PlayerListener(this);
 		AFKWorker
				.setAFKTime(CTServer.getConfiguration().getInteger("afktime") * 1000);
 		AFKWorker.setKickTime(CTServer.getConfiguration().getInteger(
				"afkkicktime") * 1000);
 		AFKWorker.setKick(CTServer.getConfiguration().getBoolean("afkkick"));
 		NopickupWorker.getPlayerNopickupMap().clear();
 		checkFeatures();
 		this.getServer()
 				.getScheduler()
 				.scheduleSyncRepeatingTask(this, AFKWorker.getAFKChecker(), 0L,
 						20L);
 		this.getServer()
 				.getScheduler()
 				.scheduleSyncRepeatingTask(this, AFKWorker.getKickChecker(),
 						0L, 20L);
 
 		try {
 			SQLiteManager.connect(this.getDataFolder().getAbsolutePath() + "/log.db");
 			DatabaseManager.executeUpdate(SQL.CREATE_TABLE_BLOCK_DATA);
 			DatabaseManager.executeUpdate(SQL.CREATE_TABLE_PLAYERS);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		log.info("Enabled " + this.getDescription().getName() + "!");
 	}
 
 	@Override
 	public void onDisable() {
 		try {
 			DatabaseManager.closeConnection();
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		this.getServer().getScheduler().cancelTasks(this);
 		log.info("Disabled " + this.getDescription().getName() + "!");
 	}
 
 	public static CentralCore getInstance() {
 		return Instance;
 	}
 
 	public File getPluginJarFile() {
 		return this.getFile();
 	}
 
 	public static String getLang() {
 		return CTServer.getConfiguration().getString("lang");
 	}
 
 	private void checkFeatures() {
 		mcbansFeatures = PluginFeatures.register("MCBans");
 		Util.broadcastMessage(i18n._((mcbansFeatures != null ? "en" : "dis")
 				+ "ablefeatures"), new String[][] { { "%plugin", "MCBans" } });
 		pexFeatures = PluginFeatures.register("PermissionsEx");
 		Util.broadcastMessage(
 				i18n._((pexFeatures != null ? "en" : "dis") + "ablefeatures"),
 				new String[][] { { "%plugin", "PermissionsEx" } });
 	}
 
 	public static String getPrefix() {
 		return getPrefix(null);
 	}
 
 	public static String getPrefix(Player par1Player) {
 		if (!CTServer.getConfiguration().getBoolean("isuseprefix"))
 			return "";
 		if (par1Player != null && pexFeatures.isEnable()) {
 			PermissionUser user = PermissionsEx.getUser(par1Player);
 			return user.getPrefix(par1Player.getWorld().getName());
 		}
 		return CTServer.getConfiguration().getString("prefix");
 	}
 }
