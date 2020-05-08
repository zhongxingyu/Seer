 package de.dustplanet.silkspawnersecoaddon;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.io.FileOutputStream;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.RegisteredServiceProvider;
 import org.bukkit.plugin.java.JavaPlugin;
 // Metrics
 import org.mcstats.Metrics;
 //Vault
 import net.milkbowl.vault.Vault;
 import net.milkbowl.vault.economy.Economy;
 
 /**
  * General stuff
  * @author xGhOsTkiLLeRx
  */
 
 public class SilkSpawnersEcoAddon extends JavaPlugin {
 	public FileConfiguration config;
 	private File configFile;
 	public Economy economy;
 	public double defaultPrice = 10.5;
 
 	public void onEnable() {	
 		// Check for Vault
 		Plugin vault = getServer().getPluginManager().getPlugin("Vault");
 		if (vault != null && vault instanceof Vault) {
 			// If Vault is enabled, load the economy
 			getLogger().info("Loaded Vault successfully");
 			setupEconomy();
 		}
 		else {
 			// Else tell the admin about the missing of Vault
 			getLogger().severe("Vault was not found! Disabling...");
 			setEnabled(false);
 			return;
 		}
 		
 		// Config
 		configFile = new File(getDataFolder(), "config.yml");
 		if(!configFile.exists()){
 			if (configFile.getParentFile().mkdirs()) copy(getResource("config.yml"), configFile);
 		}
 		config = getConfig();
 		loadConfig();
 
 		// Listeners
 		getServer().getPluginManager().registerEvents(new SilkSpawnersEcoAddonListener(this), this);
 
 		// Metrics
 		try {
 			Metrics metrics = new Metrics(this);
 			metrics.start();
 		}
 		catch (IOException e) {
 			getLogger().info("Couldn't start Metrics, please report this!");
 			e.printStackTrace();
 		}
 	}
 
 	private void loadConfig() {
 		config.options().header("You can configure every entityID or a default!");
 		config.addDefault("cantAfford", "&e[SilkSpawnersEco] &4Sorry, but you can't change the mob of this spawner, because you have not enough money!");
		config.addDefault("afford", "&e[SilkSpawnersEco] &2This action costed &e%money%");
 		config.addDefault("default", 10.5);
 		config.addDefault("pig", 7.25);
 		config.addDefault("cow", 0.00);
 		config.options().copyDefaults(true);
 		saveConfig();
 		defaultPrice = config.getDouble("default");
 	}
 
 	// Initialized to work with Vault
 	private Boolean setupEconomy() {
 		RegisteredServiceProvider<Economy> economyProvider = getServer().getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
 		if (economyProvider != null) {
 			economy = economyProvider.getProvider();
 		}
 		return (economy != null);
 	}
 	
 	// If no config is found, copy the default one(s)!
 	private void copy(InputStream in, File file) {
 		try {
 			OutputStream out = new FileOutputStream(file);
 			byte[] buf = new byte[1024];
 			int len;
 			while ((len = in.read(buf)) > 0) {
 				out.write(buf, 0, len);
 			}
 			out.close();
 			in.close();
 		} catch (FileNotFoundException e) {
 			getLogger().warning("Failed to copy the default config! (FileNotFound)");
 		} catch (IOException e) {
 			getLogger().warning("Failed to copy the default config! (I/O)");
 		}
 	}
 }
