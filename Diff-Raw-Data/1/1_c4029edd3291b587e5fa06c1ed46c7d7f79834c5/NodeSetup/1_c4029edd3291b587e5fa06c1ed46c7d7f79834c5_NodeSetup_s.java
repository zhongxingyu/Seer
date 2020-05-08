 package net.mayateck.BuyCommand;
 
 import java.util.logging.Level;
 
 public class NodeSetup {
 	public BuyCommand plugin;
 	int hash = 0;
 	
 	public NodeSetup(BuyCommand plugin){
 		this.plugin = plugin;
 		int currentHash = plugin.getConfig().getInt("node.hash");
 		try {
 			hash = plugin.getConfig().hashCode();
 		} catch (Exception e) {
 			plugin.getLogger().log(Level.SEVERE, "BuyCommand encountered an error while checking hash.");
 			e.printStackTrace();
 		}
 		if (hash!=0){
 			if (hash==currentHash){
 				plugin.getLogger().info(" - Hash matches. Continuing with set-up...");
 			} else {
 				plugin.getLogger().info(" - Hash does not match! Re-configuring permissions to match config...");
 				reconfigure();
 			}
 		}
 	}
 	
 	public void reconfigure(){
 		//FileConfiguration f = YamlConfiguration.loadConfiguration(plugin.getResource("plugin.yml"));
 	}
 }
