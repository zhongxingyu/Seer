 package nl.giantit.minecraft.GiantShop;
 
 import nl.giantit.minecraft.giantcore.GiantCore;
 import nl.giantit.minecraft.giantcore.Database.Database;
 import nl.giantit.minecraft.giantcore.GiantPlugin;
 import nl.giantit.minecraft.giantcore.Misc.Messages;
 import nl.giantit.minecraft.giantcore.core.Eco.Eco;
 import nl.giantit.minecraft.giantcore.perms.PermHandler;
 
 import nl.giantit.minecraft.GiantShop.Locationer.Locationer;
 import nl.giantit.minecraft.GiantShop.Misc.Misc;
 import nl.giantit.minecraft.GiantShop.core.config;
 import nl.giantit.minecraft.GiantShop.core.Commands.ChatExecutor;
 import nl.giantit.minecraft.GiantShop.core.Commands.ConsoleExecutor;
 import nl.giantit.minecraft.GiantShop.core.Items.Items;
 import nl.giantit.minecraft.GiantShop.core.Metrics.MetricsHandler;
 import nl.giantit.minecraft.GiantShop.core.Tools.Discount.Discounter;
 import nl.giantit.minecraft.GiantShop.core.Tools.dbInit.dbInit;
 import nl.giantit.minecraft.GiantShop.core.Updater.Updater;
 
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nl.giantit.minecraft.GiantShop.API.GiantShopAPI;
 
 /**
  *
  * @author Giant
  */
 public class GiantShop extends GiantPlugin {
 
 	public static final Logger log = Logger.getLogger("Minecraft");
 	
 	private static GiantShop plugin;
 	private static Server Server;
 	
 	private GiantCore gc;
 	private Database db;
 	private PermHandler permHandler;
 	private ChatExecutor chat;
 	private ConsoleExecutor console;
 	private Items itemHandler;
 	private Eco econHandler;
 	private Messages msgHandler;
 	private Locationer locHandler;
 	private Updater updater;
 	private Discounter discounter;
 	private MetricsHandler metrics;
 	private String name, dir, pubName;
 	private String bName = "Cacti Powered";
 	
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
 		this.gc = GiantCore.getInstance();
 		if(this.gc == null) {
 			getLogger().severe("Failed to hook into required GiantCore!");
 			this.getPluginLoader().disablePlugin(this);
 			return;
 		}
 		
		if(this.gc.getProtocolVersion() < 0.3) {
 			getLogger().severe("The GiantCore version you are using it not made for this plugin!");
 			this.getPluginLoader().disablePlugin(this);
 			return;
 		}
 		
 		Server = this.getServer();
 		
 		this.name = getDescription().getName();
 		this.dir = getDataFolder().toString();
 		
 		File configFile = new File(getDataFolder(), "conf.yml");
 		if(!configFile.exists()) {
 			getDataFolder().mkdir();
 			getDataFolder().setWritable(true);
 			getDataFolder().setExecutable(true);
 			
 			this.extract("conf.yml");
 			if(!configFile.exists()) {
 				getLogger().severe("Failed to extract configuration file!");
 				this.getPluginLoader().disablePlugin(this);
 				return;
 			}
 		}
 		
 		config conf = config.Obtain(this);
 		try {
 			this.updater = new Updater(this); // Dirty fix for NPE
 			conf.loadConfig(configFile);
 			if(!conf.isLoaded()) {
 				getLogger().severe("Failed to load configuration file!");
 				this.getPluginLoader().disablePlugin(this);
 				return;
 			}
 			
 			HashMap<String, String> db = conf.getMap(this.name + ".db");
 			db.put("debug", conf.getString(this.name + ".global.debug"));
 			
 			this.db = this.gc.getDB(this, null, db);
 			new dbInit(this);
 			
 			if(conf.getBoolean(this.name + ".permissions.usePermissions")) {
 				permHandler = this.gc.getPermHandler(PermHandler.findEngine(conf.getString(this.name + ".permissions.Engine")), conf.getBoolean(this.name + ".permissions.opHasPerms"));
 			}else{
 				permHandler = this.gc.getPermHandler(PermHandler.findEngine("NOPERM"), true);
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
 			chat = new ChatExecutor(this);
 			console = new ConsoleExecutor(this);
 			itemHandler = new Items(this);
 			econHandler = this.gc.getEcoHandler(Eco.findEngine(conf.getString("GiantShop.Economy.Engine")));
 			msgHandler = new Messages(this, 1.4);
 			
 			discounter = new Discounter(this);
 			
 			if(conf.getBoolean(this.name + ".metrics.useMetrics")) {
 				this.metrics = new MetricsHandler(this);
 			}
 			
 			GiantShopAPI.Obtain();
 			
 			if(econHandler.isLoaded()) {
 				log.log(Level.INFO, "[" + this.name + "](" + this.bName + ") Was successfully enabled!");
 			}else{
 				log.log(Level.WARNING, "[" + this.name + "] Could not load economy engine yet!");
 				log.log(Level.WARNING, "[" + this.name + "] Errors might occur if you do not see '[GiantShop]Successfully hooked into (whichever) Engine!' after this message!");
 			}
 		}catch(Exception e) {
 			log.log(Level.SEVERE, "[" + this.name + "](" + this.bName + ") Failed to load!");
 			if(conf.getBoolean(this.name + ".global.debug")) {
 				log.log(Level.INFO, e.getMessage(), e);
 			}
 			Server.getPluginManager().disablePlugin(this);
 		}
 	}
 	
 	@Override
 	public void onDisable() {
 		if(null != this.updater)
 			this.updater.stop();
 		
 		GiantShopAPI.Obtain().stop();
 		
 		if(null != this.db) {
 			this.db.getEngine().close();
 		}
 		
 		log.log(Level.INFO, "[" + this.name + "] Was successfully disabled!");
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
 	
 	@Override
 	public GiantCore getGiantCore() {
 		return this.gc;
 	}
 	
 	@Override
 	public String getPubName() {
 		return this.pubName;
 	}
 	
 	@Override
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
 
 	public String getVersion() {
 		return getDescription().getVersion();
 	}
 	
 	public String getNewVersion() {
 		return this.updater.getNewVersion();
 	}
 	
 	@Override
 	public Database getDB() {
 		return this.db;
 	}
 	
 	@Override
 	public PermHandler getPermHandler() {
 		return this.permHandler;
 	}
 	
 	public Server getSrvr() {
 		return getServer();
 	}
 	
 	public Items getItemHandler() {
 		return this.itemHandler;
 	}
 	
 	@Override
 	public Eco getEcoHandler() {
 		return this.econHandler;
 	}
 	
 	@Override
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
 	
 	public static GiantShop getPlugin() {
 		return GiantShop.plugin;
 	}
 }
