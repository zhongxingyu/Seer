 package com.ProjectTeam.SideKick;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.util.Vector;

import com.ProjectTeam.API.Back;
 public class PL implements Listener{
 	public static boolean Tools = false;
     public static boolean game = false;
     public static boolean Quit = false;
     public static boolean Join = false;
     public static boolean TheKit = false;
     public static boolean Sneak = false;
     public static boolean fly = false;
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerInteract(PlayerInteractEvent event){
 		Player p = event.getPlayer(); 
         ItemStack item = p.getItemInHand();
         if(Tools){
         if (event.getAction() == Action.RIGHT_CLICK_AIR) {
             if (item.getType() == Material.STONE_PICKAXE) {
             	if(item !=null);
              p.getItemInHand().setDurability((short) 15);
             }
             if (item.getType() == Material.WOOD_PICKAXE);
             p.getItemInHand().setDurability((short) 15);
         	}
         	if (item.getType() == Material.DIAMOND_AXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_BOOTS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_CHESTPLATE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_HELMET);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_HOE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_LEGGINGS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_PICKAXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_SPADE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.DIAMOND_SWORD);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_AXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_BOOTS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_CHESTPLATE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_HELMET);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_HOE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_LEGGINGS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_PICKAXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_SPADE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.GOLD_SWORD);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_AXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_BOOTS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_CHESTPLATE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_HELMET);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_HOE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_LEGGINGS);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_PICKAXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_SPADE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.IRON_SWORD);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.STONE_AXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.STONE_HOE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.STONE_SPADE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.STONE_SWORD);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.WOOD_AXE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.WOOD_HOE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.WOOD_SPADE);
         	p.getItemInHand().setDurability((short) 15);
         	if (item.getType() == Material.WOOD_SWORD);
         	p.getItemInHand().setDurability((short) 15);
         }
         if(fly){
         	if(p.getItemInHand().getType()==Material.SIGN && p.hasPermission("Sidekick.fly")){
     	    Player player = event.getPlayer();
     	 Vector dir = player.getLocation().getDirection();
           Vector vec = new Vector(dir.getX() * 0.8D, 0.8D, dir.getZ() * 0.8D);
           player.setVelocity(vec);
           player.setFallDistance(-100.0F);
         }
         }
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerQuit(PlayerQuitEvent event){
 		if(Quit){
 		Player p = event.getPlayer();
 		Server s = p.getServer();
 		String playername = p.getName();
 		s.broadcastMessage(ChatColor.AQUA + playername + " Has Left The Server");
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerJoin(PlayerJoinEvent event){
 		if(Join){
 			Player p = event.getPlayer();
 			Server s = p.getServer();
 			String playername = p.getName();
 			s.broadcastMessage(ChatColor.LIGHT_PURPLE + playername + " Has Joined The Server!");
 		} 
 		if(TheKit){
 		    Player p = event.getPlayer();
 			ItemStack i = new ItemStack(Material.GRASS, 10);
 			ItemStack i2 = new ItemStack(Material.WOOD_PICKAXE, 1);
 			ItemStack i3 = new ItemStack(Material.WOOD, 3);
 			p.getInventory().addItem(i);
 			p.getInventory().addItem(i2);
 			p.getInventory().addItem(i3);
 			Economy.economy.put(event.getPlayer().getName(), (double) 1);
 		}
 		if (!Economy.economy.containsKey(event.getPlayer().getName())) {
 		    Economy.economy.put(event.getPlayer().getName(), (double) 30);
 		}
 	}
 	@EventHandler(priority = EventPriority.NORMAL)
 	public void onPlayerToggleSneak(PlayerToggleSneakEvent event){
 		if(Sneak){
 			event.setCancelled(true);
 		}
 	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerDeath(PlayerDeathEvent event){
		Back.setBack(event.getEntity().getName(), event.getEntity().getLocation());
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onPlayerTeleport(PlayerTeleportEvent event){
		Back.setBack(event.getPlayer().getName(), event.getPlayer().getLocation());
	}
 }
 		
