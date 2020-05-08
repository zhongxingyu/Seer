 package com.gmail.scyntrus.dotaminecraft;
 
 import net.minecraft.server.v1_5_R2.EntityMonster;
 import net.minecraft.server.v1_5_R2.Item;
 import net.minecraft.server.v1_5_R2.ItemStack;
 
 import org.bukkit.Material;
 import org.bukkit.craftbukkit.v1_5_R2.entity.CraftEntity;
 import org.bukkit.entity.Entity;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockDamageEvent;
 import org.bukkit.event.block.BlockDispenseEvent;
 import org.bukkit.event.block.BlockPlaceEvent;
 import org.bukkit.material.Dispenser;
 import org.bukkit.material.SpawnEgg;
 
 public class BlockListener implements Listener {
 	DotaMinecraft plugin = null;
 	
 	public BlockListener(DotaMinecraft plugin){
 		this.plugin = plugin;
 	}
 
 	@EventHandler
     public void onBlockBreak(BlockBreakEvent event) {
 		if (event.getBlock().getWorld().getName().equals(plugin.WorldName)){
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
     public void onBlockDamage(BlockDamageEvent event) {
 		if (event.getBlock().getWorld().getName().equals(plugin.WorldName)){
 			event.setCancelled(true);
 		}
 	}
 
 	@EventHandler
     public void onBlockPlace(BlockPlaceEvent event) {
 		if (event.getBlock().getWorld().getName().equals(plugin.WorldName)){
 			event.setCancelled(true);
 		}
 	}
 	
 	@EventHandler
     public void onBlockDispenseEvent(BlockDispenseEvent event) {
 		if (event.getBlock().getWorld().getName().equals(plugin.WorldName)){
 			if (event.getItem().getType()==Material.MONSTER_EGG) {
 				Dispenser disp = (Dispenser) event.getBlock().getState().getData();
 				SpawnEgg e = (SpawnEgg) event.getItem().getData();
 				Entity newEntity = (Entity) event.getBlock().getWorld().spawnEntity(
						event.getBlock().getRelative(disp.getFacing()).getLocation().add(.5,0.,.5), 
 						e.getSpawnedType());
 				if (plugin.goodVersion) {
 					net.minecraft.server.v1_5_R2.Entity newMob = ((CraftEntity) newEntity).getHandle();
 					if (plugin.removeMobArmor) {
 						newMob.setEquipment(1, null);
 						newMob.setEquipment(2, null);
 						newMob.setEquipment(3, null);
 						newMob.setEquipment(4, null);
 					}
 					if (plugin.giveMobsHelmet) {
 						if (!(newMob instanceof EntityMonster && ((EntityMonster) newMob).getEquipment(4) != null)) {
 							newMob.setEquipment(4, new ItemStack(Item.LEATHER_HELMET));
 						}
 					}
 				}
 				event.setCancelled(true);
 			}
 		}
 	}
 }
