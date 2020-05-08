 package darkknightcz.InviteEm;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.entity.Player;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 import darkknightcz.InviteEm.commands.PlayerCommands;
 import darkknightcz.InviteEm.economy.RegisterMoney;
 import darkknightcz.InviteEm.listeners.InviteEmPlayerListener;
 
 public class InviteEm extends JavaPlugin {
 	private MySQL db;
 	private Settings settings;
 	public Economy economy;
 	Logger log;
 
 	public void onDisable() {
 		PluginDescriptionFile pdfFile = getDescription();
 		log.info("[" + pdfFile.getName() + "] Disabled");
 	}
 
 	public void onEnable() {
 		log = this.getLogger();
 		PluginDescriptionFile pdfFile = getDescription();
 		PluginManager pm = getServer().getPluginManager();
 		log.info("[" + pdfFile.getName() + "] Loading");
 		/* methods */
 		settings = new Settings(this);
 		settings.load();
 		setupEconomy();
 
 		try {
 			db = new MySQL();
 		} catch (Exception e) {
 			log.warning("[" + pdfFile.getName()
 					+ "] Database error, shutting down");
 			getServer().getPluginManager().disablePlugin(this);
 			return;
 		}
 
 		db.loadIps();
 		pm.registerEvents(new InviteEmPlayerListener(this), this);
 
 		PlayerCommands playerCommandsExecutor = new PlayerCommands(this, db);
 
 		this.getCommand("inv").setExecutor(playerCommandsExecutor);
 
 		log.info("[" + pdfFile.getName() + "] Loaded");
 	}
 
 	private boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer()
 				.getServicesManager().getRegistration(
 						net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 
 		return (economy != null);
 	}
 
 	public void tryRegisterMoney(Player player, int type) {
 		this.getServer()
 				.getScheduler()
 				.scheduleSyncDelayedTask(this,
 						new RegisterMoney(this, player, type), 1800L);
 	}
 
 	public MySQL getDb() {
 		return db;
 	}
 
 	public void setRewarded(String nick) {
 		db.setRewarded(nick);
 	}
 
 
 }
