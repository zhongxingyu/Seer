 package me.bibo38.DeathPay;
 
 import java.util.logging.Logger;
 
 import net.milkbowl.vault.economy.Economy;
 
 import org.bukkit.ChatColor;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DeathPay extends JavaPlugin implements Listener
 {
 	private Logger log;
 	private PluginDescriptionFile pdFile;
 	private Economy eco;
 	private FileConfiguration cfg;
 	private CustomConfig msgs;
 	
 	private boolean setupEconomy() // Vault
 	{
 		if(this.getServer().getPluginManager().getPlugin("Vault") == null)
 		{
 			return false;
 		}
 		
 		RegisteredServiceProvider<Economy> rsp = this.getServer().getServicesManager().getRegistration(Economy.class);
 		if(rsp == null)
 		{
 			return false;
 		}
 		
 		if((eco = rsp.getProvider()) == null)
 		{
 			return false;
 		}
 		
 		return true;
 	}
 	
 	@Override
 	public void onEnable()
 	{
 		log = this.getLogger();
 		pdFile = this.getDescription();
 		
 		// Setup Economy (Vault)
 		if(!setupEconomy())
 		{
 			log.warning("!!! Error setting up Vault Economy !!!");
 			return; // Nichts registrieren lassen
 		}
 		
 		// Register Event Handlers
 		PluginManager pm = this.getServer().getPluginManager();
 		pm.registerEvents(this, this);
 		
 		// Konfiguration einrichten
 		cfg = this.getConfig();
 		cfg.options().copyDefaults(true);
 		this.saveConfig();
 		
 		msgs = new CustomConfig("messages.txt", this);
		msgs.save();
 		
 		log.info("DeathPay Version " + pdFile.getVersion() + " by bibo38 was activated!");
 	}
 	
 	@EventHandler
 	public void onPlayerDeath(PlayerDeathEvent evt)
 	{
 		// Abziehen des Geldbetrages vom Konto
 		if(evt.getEntity() instanceof Player)
 		{
 			this.reloadConfig();
			msgs.reload();
 			double betrag = cfg.getDouble("amount"); // Betrag zum abziehen
 			
 			if(betrag > 0)
 			{
 				eco.withdrawPlayer(((Player) evt.getEntity()).getName(), betrag);
 				// ((Player) evt.getEntity()).sendMessage(ChatColor.RED + "" + String.format(msgs.getCfg().getString("death-debit"), betrag));
 				((Player) evt.getEntity()).sendMessage(ChatColor.RED + "" +
 						msgs.getCfg().getString("death-debit").replaceAll("%amount", String.valueOf(betrag)));
 			} else
 			{
 				log.warning("!!! Error in Configuration: amount is negative or 0 !!!");
 			}
 		}
 	}
 	
 	@Override
 	public void onDisable()
 	{
 		log.info("DeathPay Version " + pdFile.getVersion() + " was deactivated!");
 	}
 }
