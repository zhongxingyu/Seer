 package com.thegoodsirs.moogle.nohunger;
 
 import org.bukkit.Bukkit;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
 import org.bukkit.scheduler.BukkitTask;
 
 public final class NoHunger extends JavaPlugin {
 	public void onEnable(){
 		getLogger().info("NoHunger onEnable has been invoked!");
		PluginManager pm = this.getServer().getPluginManager();
        NoHungerListener nhl = new NoHungerListener(this);
 	}
  
 	public void onDisable(){
 		getLogger().info("NoHunger onDisable has been invoked!");
 	}
 }
 
 class NoHungerListener implements Listener {
 	 
     private final NoHunger plugin;
  
     public NoHungerListener(NoHunger plugin) {
         this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
        NoHungerTask nht = new NoHungerTask(this.plugin);
        nht.runTask(plugin);
     }
 }
 
 class NoHungerTask extends BukkitRunnable {
 	 
     private final JavaPlugin plugin;
  
     public NoHungerTask(JavaPlugin plugin) {
         this.plugin = plugin;
     }
  
     public void run() {
    	Player[] onlinePlayerList = plugin.getServer().getOnlinePlayers();
    	for (Player p : onlinePlayerList) {
     		p.setFoodLevel(20);
    	}
    	
    	this.runTaskLater(plugin,  20);
     }
  
 }
