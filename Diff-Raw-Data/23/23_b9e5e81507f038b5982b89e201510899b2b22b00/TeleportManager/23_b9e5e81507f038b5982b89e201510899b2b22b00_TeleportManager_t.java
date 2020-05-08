 package net.sacredlabyrinth.Phaed.TelePlusPlus.managers;
 
 import net.sacredlabyrinth.Phaed.TelePlusPlus.TeleHistory;
 import net.sacredlabyrinth.Phaed.TelePlusPlus.TelePlusPlus;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.block.Block;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 
 import java.util.ArrayList;
 
 public class TeleportManager
 {
     private TelePlusPlus plugin;
 
     public TeleportManager(TelePlusPlus plugin)
     {
         this.plugin = plugin;
     }
 
     public boolean teleport(Entity entity, Player player)
     {
         ArrayList<Entity> entities = new ArrayList<Entity>();
         entities.add(entity);
 
         Location smartLocation = calculateSmartLocation(player);
         return teleport(entities, smartLocation);
     }
 
     public boolean teleport(Entity entity, Location destination)
     {
         ArrayList<Entity> entities = new ArrayList<Entity>();
         entities.add(entity);
         return teleport(entities, destination);
     }
 
     public boolean teleport(ArrayList<Entity> entities, Player player)
     {
         Location smartLocation = calculateSmartLocation(player);
         return teleport(entities, smartLocation);
     }
 
     public boolean teleport(ArrayList<Entity> entities, Location destination)
     {
         World world = destination.getWorld();
         double x = destination.getBlockX();
         double y = destination.getBlockY();
         double z = destination.getBlockZ();
 
         x = x + .5D;
         z = z + .5D;
 
         if (y < 1)
         {
             y = 1;
         }
 
         if (!world.isChunkLoaded(destination.getBlockX() >> 4, destination.getBlockZ() >> 4))
         {
             world.loadChunk(destination.getBlockX() >> 4, destination.getBlockZ() >> 4);
         }
 
         while (!blockIsSafe(world, x, y, z))
         {
             y += 1;
 
             if (y >= 255)
             {
                 return false;
             }
         }
 
         for (Entity entity : entities)
         {
             Location loc = new Location(world, x, y, z, destination.getYaw(), destination.getPitch());
 
             boolean sneaking = false;
 
             if (entity instanceof Player)
             {
                 Player player = (Player) entity;
 
                 if (player.isSneaking())
                 {
                     sneaking = true;
                 }
             }
 
             if (entity instanceof Player)
             {
                 if (plugin.sm.explosionEffect)
                 {
                    if (!plugin.pm.isVanished((Player) entity) && !sneaking)
                     {
                         world.createExplosion(entity.getLocation(), -1);
                     }
                 }
             }
 
            if (entity instanceof Player)
            {
                TeleHistory.pushLocation((Player) entity, entity.getLocation());
            }

             entity.teleport(loc);
 
             if (plugin.sm.explosionEffect)
             {
                if (entity instanceof Player)
                 {
                    if (!plugin.pm.isVanished((Player) entity) && !sneaking)
                    {
                        world.createExplosion(loc, -1);
                    }
                 }
             }
         }
 
         return true;
     }
 
     public boolean blockIsSafe(Block block)
     {
         return blockIsSafe(block.getWorld(), block.getX(), block.getY(), block.getZ());
     }
 
     public boolean blockIsSafe(World world, double x, double y, double z)
     {
         int id1 = world.getBlockTypeIdAt((int) Math.floor(x), (int) Math.floor(y), (int) Math.floor(z));
         int id2 = world.getBlockTypeIdAt((int) Math.floor(x), (int) Math.floor(y + 1), (int) Math.floor(z));
 
         return (plugin.sm.isSeeThrough(id1)) && (plugin.sm.isSeeThrough(id2));
     }
 
     public Location calculateSmartLocation(Player player)
     {
         return player.getLocation();
     }
 }
