 package net.illusiononline.factioncore;
 
 import java.util.logging.Logger;
 import org.bukkit.plugin.java.JavaPlugin;
 import net.illusiononline.EmeraldEconomy.EmeraldEconomy;
 import net.illusiononline.factioncore.backends.MySQLManager;
 
 public class FactionCore extends JavaPlugin{
 	
 	private Logger log = Logger.getLogger("Minecraft");
 	private static MySQLManager sqlmanager;
 	private boolean economy_is_present = false;
 	private static FactionManager factionmanager;
 	private net.illusiononline.EmeraldEconomy.MySQLManager economy_sqlmanager;
 	
 	@Override
 	public void onEnable() { 
 		economy_sqlmanager = EmeraldEconomy.getSQLManager();
 		if (economy_sqlmanager != null) {
 			log.info("Economy plugin is present: Allowing advanced features!");
 			economy_is_present = true;
 		} else {
 			log.info("Economy plugin is not present: Disallowing advanced features!");
 		}
 		sqlmanager = new MySQLManager(this);
 		factionmanager = new FactionManager(this);
 		getCommand("faction").setExecutor(new FactionCommandExecutor());
 	}
 	 @Override
 	public void onDisable() {  
 	}
 
 	public static MySQLManager getSqlManager(){return sqlmanager;}
 	public static FactionManager getFactionManager(){return factionmanager;}
 	 
 	public boolean getEconomyIsPresent(){return economy_is_present;}
 	public net.illusiononline.EmeraldEconomy.MySQLManager getEconomySQLManager(){return economy_sqlmanager;}
 }
 
