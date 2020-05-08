 package com.coffeejawa.LootChests;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.block.Chest;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class LootChestsMain extends JavaPlugin  {
     public final Logger logger = Logger.getLogger("Minecraft");
     public PlayerInteractionListener  rightClickListener;
     
     private HashMap<CustomChest,Player> ChestPlayerMap;
     
     public HashMap<CustomChest, Player> getChestPlayerMap() {
         return ChestPlayerMap;
     }
     
     public Boolean hasChest(Block b){
         for( CustomChest c : getChestPlayerMap().keySet() ){
             if(c.getChestBlock().getLocation().equals(b.getLocation())){
                 return true;
             }
         }
         return false;
     }
     
     public Player getChestOwner(Block b){
         for( CustomChest c : getChestPlayerMap().keySet() ){
             if(c.getChestBlock().getLocation().equals(b.getLocation())){
                 return getChestPlayerMap().get(c);
             }
         }
         return null;
     }
 
     @Override
     public void onEnable() 
     {        
         ChestPlayerMap = new HashMap<CustomChest, Player>();
         
         this.reloadConfig();
         
         rightClickListener = new PlayerInteractionListener();
         
         getServer().getPluginManager().registerEvents(new ChestListener(this), this);
         getServer().getPluginManager().registerEvents(rightClickListener, this);
         
         logger.info("[" + this.getDescription().getName() +  "] enabled.");
         
         
         getServer().getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
 
             public void run() {
                 for( CustomChest cc : ChestPlayerMap.keySet()){
                     if(cc.hasTimeoutExpired()){
                         cc.tearDown();
                         ChestPlayerMap.remove(cc);
                     }
                 }
             }
         }, 0L, 600L);
     }
     
     @Override
     public void onDisable() 
     {
         logger.info("[" + this.getDescription().getName() +  "] disabled.");  
         this.saveConfig();
     }
 
     public Chest CreateChest(Player player, Location location, ArrayList<ItemStack> items, float timeout)
     {
         // if there are no items, why bother
         if(items.size() == 0)
             return null;
         
         // attempt to create a block 1 tile above
         location.setY(location.getY());
         
         Block b = location.getWorld().getBlockAt(location);
         if( b.getTypeId() != Material.AIR.getId() ){
             // not air, we can't create a block here
             return null;
         }
         
         // otherwise change block type from air to chest
         b.setType(Material.CHEST);
         
         // insert items
         Chest chest = (Chest) b.getState();
         for( ItemStack item : items ){
             chest.getBlockInventory().addItem(item);
         }
         
         // insert in our map
         ChestPlayerMap.put(new CustomChest(chest,timeout), player);
         
         return chest;
     }
     
 }
 
 
 /* 
  * Requirements:
 *   -CreateChest
 *   -Remove spawned chests when empty
 *   -Remove spawned non-empty chests when timeout expires
 *   -Prevent unauthorized players from opening chests
 */
 
