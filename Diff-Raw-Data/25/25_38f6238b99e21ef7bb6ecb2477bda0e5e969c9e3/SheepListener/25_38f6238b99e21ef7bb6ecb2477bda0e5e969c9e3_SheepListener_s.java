 package com.chrisgward.bukkit.plugins.Sheep;
 
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.CreatureType;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Sheep;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
 import org.bukkit.event.block.BlockBreakEvent;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.inventory.ItemStack;
 
 import java.awt.*;
 import java.rmi.MarshalledObject;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;
 
 public class SheepListener implements Listener {
     HashMap<UUID, Collection<ItemStack>> sheepMaterialHashMap = new HashMap<UUID, Collection<ItemStack>>();
     SheepPlugin inst;
     public SheepListener(SheepPlugin instance)
     {
         inst = instance;
     }
 
     @EventHandler
     public void onBlockBreak(BlockBreakEvent e)
     {
         Location loc  = e.getBlock().getLocation();
         Sheep sheep = (Sheep)loc.getWorld().spawnCreature(loc, CreatureType.SHEEP);
         sheep.setColor(DyeColor.values()[(new Random()).nextInt() ^ 2 % DyeColor.values().length]);
         sheepMaterialHashMap.put(sheep.getUniqueId(), e.getBlock().getDrops());
        e.getDrops().clear();
     }
 
     @EventHandler
     public void onEntityDeath(EntityDeathEvent e)
     {
         if(e.getEntity() instanceof Sheep)
         {
             if(sheepMaterialHashMap.containsKey(e.getEntity().getUniqueId()))
             {
                e.getDrops().clear();
                e.getDrops().addAll(sheepMaterialHashMap.get(e.getEntity().getUniqueId()));
             }
         }
     }
 
 }
