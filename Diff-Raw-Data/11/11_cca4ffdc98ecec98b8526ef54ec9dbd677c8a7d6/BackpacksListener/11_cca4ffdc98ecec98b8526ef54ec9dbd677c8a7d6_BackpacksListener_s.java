 package edu.unca.atjones.Backpacks;
 
 import java.text.MessageFormat;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Random;
 import java.util.Set;
 
 import net.minecraft.server.EntityItem;
 import net.minecraft.server.EntityPlayer;
 import net.minecraft.server.EntityTracker;
 import net.minecraft.server.Packet22Collect;
 import net.minecraft.server.WorldServer;
 
 import org.bukkit.Bukkit;
 import org.bukkit.craftbukkit.CraftWorld;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.player.PlayerInteractEntityEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerLoginEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 
 import edu.unca.atjones.Backpacks.BackpacksInventory.MinecraftInventory;
 
 public class BackpacksListener implements Listener {
     private final Backpacks plugin;
 
     public BackpacksListener(Backpacks plugin) {
         // Register the listener
         plugin.getServer().getPluginManager().registerEvents(this, plugin);
         
         this.plugin = plugin;
     }
     
     /*@EventHandler
     public void onPlayerQuit(PlayerQuitEvent event) {
     	Player player = event.getPlayer();
     	String playerName = player.getName();
     	if(plugin.backpacks.containsKey(playerName)){
     		HashMap<String,BackpacksInventory> playerBackpacks = plugin.backpacks.get(playerName);
     		Set<String> keys = playerBackpacks.keySet();
     		Iterator<String> keyIterator = keys.iterator();
     		while(keyIterator.hasNext()) {
     			BackpacksInventory backpack = playerBackpacks.get(keyIterator.next());
     			plugin.database.storeBackpack(player, backpack);
     		}
     	}
     }*/
     
     /*@EventHandler
     public void onPlayerLogin(PlayerLoginEvent event) {
     	
     }*/
     
     @EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
     	//increment the player's broken block count
     	Player p = event.getPlayer();
     	String playerName = p.getName();
     	if(p.hasPermission("backpacks.autoreward")) {
     		if(plugin.blocks.containsKey(playerName)) {
         		int count = plugin.blocks.get(playerName) + 1;
         		double log10 = Math.log10(count);
        		if((int)log10 == (int)Math.floor(log10)) {
        			plugin.blocks.put(playerName, 0);
         			plugin.grantReward(p,1);
        			p.sendMessage("%d blocks broken! You can upgrade of add an inventory");
        		}
        		else {
        			plugin.blocks.put(playerName, count);
         		}
         	} else {
         		plugin.blocks.put(playerName, 1);
         	}
     	}
     }
     
     @EventHandler
     public void onPickupItem(PlayerPickupItemEvent event) {
     	Player p = event.getPlayer();
     	String playerName = p.getName();
     	
     	Item item = event.getItem();
     	int typeId = item.getItemStack().getTypeId();
     	
     	if(plugin.backpacks.containsKey(playerName)) {
     		if(plugin.routes.containsKey(playerName)) {
     			HashMap<Integer,String> playerRoutes = plugin.routes.get(playerName);
         		HashMap<String,BackpacksInventory> playerInventories = plugin.backpacks.get(playerName);
         		if(playerRoutes.containsKey(typeId)) {
         			String destName = playerRoutes.get(typeId);
         			if(playerInventories.containsKey(destName)) {
     			    	String worldName = item.getWorld().getName();
     			    	CraftWorld w = (CraftWorld) Bukkit.getServer().getWorld(worldName);
     			    	WorldServer worldserver = w.getHandle();
     					EntityItem entity = (EntityItem) worldserver.getEntity(item.getEntityId());
     					EntityPlayer player = (EntityPlayer) worldserver.getEntity(p.getEntityId());
     							
     					entity.pickupDelay = 0;
     					BackpacksInventory dest = playerInventories.get(destName);
     					if ( ( (MinecraftInventory) dest.getInventory()).pickup(entity.itemStack) ) {
     						event.setCancelled(true);
     						Random random = new Random();
     						entity.world.makeSound(entity, "random.pop", 0.2F, ((random.nextFloat() - random.nextFloat()) * 0.7F + 1.0F) * 2.0F);
     						EntityTracker entitytracker = worldserver.getTracker();
     						entitytracker.a(entity, new Packet22Collect(entity.id, player.id));
     			            if (entity.itemStack.count <= 0) {
     			            	entity.die();
     			            }
     			        }
         			}
         		};
     		}
     	}	
     }
 }
