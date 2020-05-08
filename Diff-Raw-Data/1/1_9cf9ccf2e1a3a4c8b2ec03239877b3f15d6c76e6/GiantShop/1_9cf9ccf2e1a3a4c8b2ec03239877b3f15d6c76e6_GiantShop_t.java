 package nl.giantit.minecraft.GiantShop;
 
 import nl.giantit.minecraft.GiantShop.Executors.chat;
 import nl.giantit.minecraft.GiantShop.Executors.console;
 import nl.giantit.minecraft.GiantShop.Locationer.Locationer;
 import nl.giantit.minecraft.GiantShop.Misc.Messages;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Database.Database;
 import nl.giantit.minecraft.GiantShop.core.Eco.Eco;
 import nl.giantit.minecraft.GiantShop.core.Items.Items;
 import nl.giantit.minecraft.GiantShop.core.Metrics.MetricsHandler;
 import nl.giantit.minecraft.GiantShop.core.Tools.Discount.Discounter;
 import nl.giantit.minecraft.GiantShop.core.Tools.dbInit.dbInit;
 import nl.giantit.minecraft.GiantShop.core.Updater.Updater;
 import nl.giantit.minecraft.GiantShop.core.perms.PermHandler;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author Giant
  */
 public class GiantShop extends JavaPlugin {
 
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	private static GiantShop plugin;
 	private static Server Server;
 	private Database db;
 	private PermHandler permHandler;
 	private chat chat;
 	private console console;
 	private Items itemHandler;
 	private Eco econHandler;
 	private Messages msgHandler;
 	private Locationer locHandler;
 	private Updater updater;
 	private Discounter discounter;
 	private MetricsHandler metrics;
 	private int tID;
 	private String name, dir, pubName;
 	private String bName = "Forniphilia";
 	
 	private boolean useLoc = false;
 	public List<String> cmds;
 	
 	private void setPlugin() {
 		GiantShop.plugin = this;
 	}
 	
 	public GiantShop() {
 		this.setPlugin();
 	}
 	
 	@Override
 	public void onEnable() {
 		Server = this.getServer();
 		
 		this.name = getDescription().getName();
 		this.dir = getDataFolder().toString();
 		
 		File configFile = new File(getDataFolder(), "conf.yml");
 		if(!configFile.exists()) {
 			getDataFolder().mkdir();
 			getDataFolder().setWritable(true);
 			getDataFolder().setExecutable(true);
 			
 			extractDefaultFile("conf.yml");
 		}
 		
 		config conf = config.Obtain(this);
 		try {
			this.updater = new Updater(this); // Dirty fix for NPE
 			conf.loadConfig(configFile);
 			HashMap<String, String> db = (HashMap<String, String>) conf.getMap(this.name + ".db");
 			db.put("debug", conf.getString(this.name + ".global.debug"));
 			
 			this.db = Database.Obtain(this, null, db);
 			new dbInit(this);
 			
 			if(conf.getBoolean(this.name + ".permissions.usePermissions")) {
 				permHandler = new PermHandler(this, conf.getString(this.name + ".permissions.Engine"), conf.getBoolean(this.name + ".permissions.opHasPerms"));
 			}else{
 				permHandler = new PermHandler(this, "NOPERM", true);
 			}
 			
 			if(conf.getBoolean(this.name + ".Location.useGiantShopLocation")) {
 				useLoc = true;
 				locHandler = new Locationer(this);
 				cmds = conf.getStringList(this.name + ".Location.protect.Commands");
 				
 				if(conf.getBoolean(this.name + ".Location.showPlayerEnteredShop"))
 					getServer().getPluginManager().registerEvents(new nl.giantit.minecraft.GiantShop.Locationer.Listeners.PlayerListener(this), this);
 				
 			}
 			
 			if(conf.getBoolean(this.name + ".Updater.checkForUpdates")) {
 				getServer().getPluginManager().registerEvents(new nl.giantit.minecraft.GiantShop.Listeners.PlayerListener(this), this);
 			}
 			
 			this.updater = new Updater(this);
 			
 			pubName = conf.getString(this.name + ".global.name");
 			chat = new chat(this);
 			console = new console(this);
 			itemHandler = new Items(this);
 			econHandler = new Eco(this);
 			msgHandler = new Messages(this);
 			
 			discounter = new Discounter(this);
 			
 			if(econHandler.isLoaded()) {
 				log.log(Level.INFO, "[" + this.name + "](" + this.bName + ") Was successfully enabled!");
 			}else{
 				log.log(Level.WARNING, "[" + this.name + "] Could not load economy engine yet!");
 				log.log(Level.WARNING, "[" + this.name + "] Errors might occur if you do not see '[GiantShop]Successfully hooked into (whichever) Engine!' after this message!");
 			}
 			
 			if(conf.getBoolean(this.name + ".metrics.useMetrics")) {
 				this.metrics = new MetricsHandler(this);
 			}
 		}catch(Exception e) {
 			log.log(Level.SEVERE, "[" + this.name + "](" + this.bName + ") Failed to load!");
 			if(conf.getBoolean(this.name + ".global.debug")) {
 				log.log(Level.INFO, "" + e);
 				e.printStackTrace();
 			}
 			Server.getPluginManager().disablePlugin(this);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 		if(null != this.updater)
 			this.updater.stop();
 		
 		this.db.getEngine().close();
 		
 		log.log(Level.INFO, "[" + this.name + "] Was successfully dissabled!");
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args) {
 		if (Misc.isEitherIgnoreCase(cmd.getName(), "shop", "s")) {
 			if(!(sender instanceof Player)){
 				return console.exec(sender, args);
 			}
 			
 			return chat.exec(sender, args);
 		}else if (cmd.getName().equalsIgnoreCase("loc")) {
 			return locHandler.onCommand(sender, cmd, commandLabel, args);
 		}
 		
 		return false;
 	}
 	
 	public int scheduleAsyncDelayedTask(final Runnable run) {
 		return getServer().getScheduler().scheduleAsyncDelayedTask(this, run, 20L);
 	}
 	
 	public int scheduleAsyncRepeatingTask(final Runnable run, Long init, Long delay) {
 		return getServer().getScheduler().scheduleAsyncRepeatingTask(this, run, init, delay);
 	}
 	
 	public String getPubName() {
 		return this.pubName;
 	}
 	
 	public String getDir() {
 		return this.dir;
 	}
 	
 	public String getSeparator() {
 		return File.separator;
 	}
 	
 	public Boolean isOutOfDate() {
 		return this.updater.isOutOfDate();
 	}
 	
 	public Boolean useLocation() {
 		return this.useLoc;
 	}
 	
 	public String getNewVersion() {
 		return this.updater.getNewVersion();
 	}
 	
 	public Database getDB() {
 		return this.db;
 	}
 	
 	public PermHandler getPermHandler() {
 		return this.permHandler;
 	}
 	
 	public Server getSrvr() {
 		return getServer();
 	}
 	
 	public Items getItemHandler() {
 		return this.itemHandler;
 	}
 	
 	public Eco getEcoHandler() {
 		return this.econHandler;
 	}
 	
 	public Messages getMsgHandler() {
 		return this.msgHandler;
 	}
 	
 	public Discounter getDiscounter() {
 		return this.discounter;
 	}
 	
 	public Locationer getLocHandler() {
 		return this.locHandler;
 	}
 	
 	public Updater getUpdater() {
 		return this.updater;
 	}
 	
 	public void extract(String file) {
 		extractDefaultFile(file);
 	}
 	
 	public void extract(File file, InputStream input) {
 		extractDefaultFile(file, input);
 	}
 	
 	public static GiantShop getPlugin() {
 		return GiantShop.plugin;
 	}
 	
 	private void extractDefaultFile(String file) {
 		File configFile = new File(getDataFolder(), file);
 		if (!configFile.exists()) {
 			InputStream input = this.getClass().getResourceAsStream("/nl/giantit/minecraft/" + name + "/core/Default/" + file);
 			if (input != null) {
 				FileOutputStream output = null;
 
 				try {
 					output = new FileOutputStream(configFile);
 					byte[] buf = new byte[8192];
 					int length = 0;
 
 					while ((length = input.read(buf)) > 0) {
 						output.write(buf, 0, length);
 					}
 
 					log.log(Level.INFO, "[" + name + "] copied default file: " + file);
 					output.close();
 				} catch (Exception e) {
 					Server.getPluginManager().disablePlugin(this);
 					log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Can't extract the requested file!!", e);
 				} finally {
 					try {
 						input.close();
 					} catch (Exception e) {
 						Server.getPluginManager().disablePlugin(this);
 						log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Severe error!!", e);	
 					}
 					try {
 						output.close();
 					} catch (Exception e) {
 						Server.getPluginManager().disablePlugin(this);
 						log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Severe error!!", e);
 					}
 				}
 			}
 		}
 	}
 	
 	private void extractDefaultFile(File file, InputStream input) {
 		if (!file.exists()) {
 			try {
 			 file.createNewFile();
 			}catch(IOException e) {
 				log.log(Level.SEVERE, "[" + name + "] Can't extract the requested file!!", e);
 			}
 		}
 		if (input != null) {
 			FileOutputStream output = null;
 
 			try {
 				output = new FileOutputStream(file);
 				byte[] buf = new byte[8192];
 				int length = 0;
 
 				while ((length = input.read(buf)) > 0) {
 					output.write(buf, 0, length);
 				}
 
 				log.log(Level.INFO, "[" + name + "] copied default file: " + file);
 				output.close();
 			} catch (Exception e) {
 				Server.getPluginManager().disablePlugin(this);
 				log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Can't extract the requested file!!", e);
 			} finally {
 				try {
 					input.close();
 				} catch (Exception e) {
 					Server.getPluginManager().disablePlugin(this);
 					log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Severe error!!", e);	
 				}
 				try {
 					output.close();
 				} catch (Exception e) {
 					Server.getPluginManager().disablePlugin(this);
 					log.log(Level.SEVERE, "[" + name + "] AAAAAAH!!! Severe error!!", e);
 				}
 			}
 		}
 	}
 }
