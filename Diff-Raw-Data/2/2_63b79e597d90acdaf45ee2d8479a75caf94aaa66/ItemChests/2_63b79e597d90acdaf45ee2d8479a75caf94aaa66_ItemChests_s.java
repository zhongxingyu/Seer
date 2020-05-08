 package com.hawkfalcon.ItemChests;
 
 import org.bukkit.Bukkit;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Listener;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitTask;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 
 public class ItemChests extends JavaPlugin {
     private ChestType chestType = ChestType.INFINITE;
     private int limit = 1;
     private HashMap<String, Integer> usesLeft = new HashMap<String, Integer>();
     private int minutes = 24 * 60;
 
     public CommandExecutor Commands = new Commands(this);
     public Listener InventoryListener = new InventoryListener(this);
 
     public void onEnable() {
         final File f = new File(getDataFolder(), "config.yml");
         if (!f.exists()) {
             saveDefaultConfig();
         }
         try {
             MetricsLite metrics = new MetricsLite(this);
             metrics.start();
         } catch (IOException e) {
             System.out.println("Error Submitting stats!");
         }
         getServer().getPluginManager().registerEvents(InventoryListener, this);
         getCommand("ic").setExecutor(Commands);
         getCommand("itemchest").setExecutor(Commands);
        chestType = ChestType.valueOf(getConfig().getString("chestType"));
         limit = getConfig().getInt("limit");
         if (chestType == ChestType.TIMELIMIT) {
         startTimer();
         }
     }
 
     private void startTimer() {
         @SuppressWarnings("unused")
         BukkitTask task = Bukkit.getScheduler().runTaskTimer(this, new Runnable() {
             public void run() {
                 usesLeft.clear();
             }
         }, 0, 24 * 60 * this.minutes);
     }
 
     public void setChestType(ChestType chestType) {
         this.chestType = chestType;
     }
 
     public ChestType getChestType() {
         return this.chestType;
     }
 
     public int getLimit() {
         return this.limit;
     }
 
     public int setLimit(int limit) {
        return this.limit = limit;
     }
 
     public void addLimitedUses(Player player) {
        this.usesLeft.put(player.getName(), limit);
     }
 
     public int getPlayerUses(Player player) {
         return this.usesLeft.get(player.getName());
     }
 
     public void decreasePlayerUses(Player player) {
        this.usesLeft.put(player.getName(), this.getPlayerUses(player) - 1);
     }
 
     public boolean canUse(Player player) {
         return (this.getPlayerUses(player) > 0);
     }
 
     public boolean playerHasLimitedUses(Player player) {
         return this.usesLeft.containsKey(player.getName());
     }
 }
