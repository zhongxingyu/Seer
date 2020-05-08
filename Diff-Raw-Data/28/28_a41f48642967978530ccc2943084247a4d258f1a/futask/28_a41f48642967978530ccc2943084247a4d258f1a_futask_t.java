 package com.comze_instancelabs.boatgame;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.scheduler.BukkitRunnable;
  
 public class futask extends BukkitRunnable {
 
 	private Player player;
 	private Location locat;
 	private boolean update = true;
 	private List<Integer> itemid = new ArrayList<Integer>();
 	private int amount = 0;
 	private int snowballstacks = 0;
	private ItemStack[] savedinv = null;
 	
     public futask(Player pla, Location loc, boolean t, int snowballstacks_amount) {
     	player = pla;
     	locat = loc;
     	update = t;
     	snowballstacks = snowballstacks_amount;
     }
     
     public futask(Player pla, Location loc, boolean t, List<Integer> itemreward_itemid, int itemreward_amount, int snowballstacks_amount) {
     	player = pla;
     	locat = loc;
     	update = t;
     	itemid = itemreward_itemid;
     	amount = itemreward_amount;
     	snowballstacks = snowballstacks_amount;
     }
    
    public futask(Player pla, Location loc, boolean t, int snowballstacks_amount, ItemStack[] pinv) {
    	player = pla;
    	locat = loc;
    	update = t;
    	snowballstacks = snowballstacks_amount;
    	savedinv = pinv;
    }
  
     public void run() {
         player.updateInventory();
         player.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));
         player.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));
         player.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));
         if(update){
         	player.getInventory().setContents(Main.pinv.get(player));
         }
         player.updateInventory();
 
 		if(itemid.size() > 0){
 			for(int id : itemid){
 				player.getInventory().addItem(new ItemStack(Material.getMaterial(id), amount));
 	        	player.updateInventory();
 			}
         }
     	
         tpaway(player, locat);
         
         player.updateInventory();
         player.getInventory().addItem(new ItemStack(Material.SNOW_BALL, 1));
         player.updateInventory();
         for(int i_ = 0; i_ < snowballstacks + 1; i_++){
 			player.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 64));	
 		}
         player.getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 1));
         player.updateInventory();
        
        if(savedinv != null){
        	player.getInventory().setContents(savedinv);
        }
     }
  
     
     public void tpaway(Player pla, Location loc){
     	pla.teleport(loc);
     }
 }
