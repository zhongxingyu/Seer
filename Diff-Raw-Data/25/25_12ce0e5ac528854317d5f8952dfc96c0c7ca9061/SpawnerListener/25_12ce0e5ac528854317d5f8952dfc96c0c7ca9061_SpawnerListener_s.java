 package com.jordanneil23.SpawnMob;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Player;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.block.BlockListener;
 import org.bukkit.event.block.BlockRightClickEvent;
 import org.bukkit.inventory.ItemStack;
 
 /**
  * SpawnMob for Bukkit
  *
  * @author jordanneil23
  */
 public class SpawnerListener extends BlockListener{
 	 public static SpawnMob plugin;
 	 public SpawnerListener(SpawnMob instance) {
     	 plugin = instance;
     }
 	 public void onBlockRightClick(BlockRightClickEvent event) 
 	 {
 		 Block block = event.getBlock();
 	 		if(block.getType() == Material.MOB_SPAWNER)
 	 		{
 	 			Player p = event.getPlayer();
 	 			if (SpawnMob.Permissions.has(p, "spawnmob.mspawn.check"))
 	 			{
 	 				CreatureType mob = ((org.bukkit.block.CreatureSpawner) block.getState()).getCreatureType();
          			int del = ((org.bukkit.block.CreatureSpawner) block.getState()).getDelay();
         			event.getPlayer().sendMessage("This spawners mob type is " + mob + ".");
         			event.getPlayer().sendMessage("This spawners delay is set to " + del + ".");
 	 			} else {return;}
 	 		}
 	 		return;
 	}
 	 public void onBlockBreak(BlockBreakEvent event) {
 		 Block block = event.getBlock();
 		 if(block.getType() == Material.MOB_SPAWNER)
 	 		{
 			Location pos = new Location(event.getPlayer().getWorld(), event.getBlock().getX(), event.getBlock().getY(), event.getBlock().getZ());
      		event.getPlayer().getWorld().dropItemNaturally(pos, new ItemStack(event.getBlock().getTypeId(),1));
      		event.getBlock().setTypeId(0);
      		event.setCancelled(true);
      		return;
 	 		}
		 return;
 	 }
 }
