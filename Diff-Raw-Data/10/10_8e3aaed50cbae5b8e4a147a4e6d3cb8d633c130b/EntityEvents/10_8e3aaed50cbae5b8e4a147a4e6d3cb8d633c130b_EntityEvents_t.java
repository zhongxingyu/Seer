 /*
 
 This file is part of EpicZones
 
 Copyright (C) 2011 by Team ESO
 
 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:
 
 The above copyright notice and this permission notice shall be included in
 all copies or substantial portions of the Software.
 
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 THE SOFTWARE.
 
  */
 
 /**
  * @author jblaske@gmail.com
  * @license MIT License
  */
 
 package com.randomappdev.EpicZones.listeners;
 
 import com.randomappdev.EpicZones.General;
 import com.randomappdev.EpicZones.Log;
 import com.randomappdev.EpicZones.objects.EpicZone;
 import com.randomappdev.EpicZones.objects.EpicZonePlayer;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.*;
 import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
 
 import java.awt.*;
 import java.util.HashSet;
 
 public class EntityEvents implements Listener
 {
 
     private HashSet<SpawnReason> spawnReasonsToBlock = new HashSet<SpawnReason>();
 
     public EntityEvents()
     {
         spawnReasonsToBlock.add(SpawnReason.NATURAL);
     }
 
     @EventHandler
     public void onEntityExplode(EntityExplodeEvent event)
     {
         try
         {
             //Log.Write("Entity Explode Event! [" + event.getEntity().getType().toString() + "]");
             EpicZone zone = General.GetZoneForPlayer(null, event.getLocation().getWorld().getName(), event.getLocation().getBlockY(), new Point(event.getLocation().getBlockX(), event.getLocation().getBlockZ()));
             if (zone != null)
             {
                 if (event.getEntity() == null)
                 {
                     if (!zone.getExplode().getOther())
                     {
                         event.setYield(0);
                         event.setCancelled(true);
                     }
                 } else
                 {
                     if (event.getEntity().toString().equalsIgnoreCase("CraftTNTPrimed"))
                     {
                         if (!zone.getExplode().getTNT())
                         {
                             event.setYield(0);
                             event.setCancelled(true);
                         }
                     } else if (event.getEntity().toString().equalsIgnoreCase("CraftCreeper"))
                     {
                         if (!zone.getExplode().getCreeper())
                         {
                             event.setYield(0);
                             event.setCancelled(true);
                         }
                     } else if (event.getEntity().toString().equalsIgnoreCase("CraftFireball"))
                     {
                         if (!zone.getExplode().getGhast())
                         {
                             event.setYield(0);
                             event.setCancelled(true);
                         }
                     } else
                     {
                         if (!zone.getExplode().getGhast())
                         {
                             event.setYield(0);
                             event.setCancelled(true);
                         }
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
     @EventHandler
     public void onEntityCombust(EntityCombustEvent event)
     {
         try
         {
             //Log.Write("Entity Combust Event! [" + event.getEntity().getType().toString() + "]");
             if (!event.isCancelled())
             {
                 Entity e = event.getEntity();
                 EpicZone zone = General.GetZoneForPlayer(null, e.getLocation().getWorld().getName(), e.getLocation().getBlockY(), new Point(e.getLocation().getBlockX(), e.getLocation().getBlockZ()));
                 if (zone != null)
                 {
                     if (!zone.getFire().getIgnite())
                     {
                         if (isPlayer(e))
                         {
                             e.setFireTicks(0);
                             event.setCancelled(true);
                         } else if (!zone.getFireBurnsMobs())
                         {
                             e.setFireTicks(0);
                             event.setCancelled(true);
                         }
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
     @EventHandler
     public void onEntityDamage(EntityDamageEvent event)
     {
         try
         {
             //Log.Write("Entity Damage Event! [" + event.getEntity().getType().toString() + "]");
             if (!event.isCancelled())
             {
                 Entity e = event.getEntity();
                 EpicZone sancZone = General.GetZoneForPlayer(null, e.getLocation().getWorld().getName(), e.getLocation().getBlockY(), new Point(e.getLocation().getBlockX(), e.getLocation().getBlockZ()));
                 if ((sancZone != null && !sancZone.getSanctuary()) || sancZone == null)
                 {
                    if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_ATTACK || event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE)
                     {
                         if (event instanceof EntityDamageByEntityEvent)
                         {
                             EntityDamageByEntityEvent sub = (EntityDamageByEntityEvent) event;
                             if (isPlayer(sub.getEntity()) && isPlayer(sub.getDamager()))
                             {
                                 Player player = (Player) sub.getEntity();
                                 EpicZonePlayer ezp = General.getPlayer(player.getName());
                                 EpicZone zone = ezp.getCurrentZone();
                                 if (zone != null)
                                 {
                                     if (!zone.getPVP())
                                     {
                                         event.setCancelled(true);
                                     }
                                 } else
                                 {
                                     if (!General.myGlobalZones.get(e.getWorld().getName().toLowerCase()).getPVP())
                                     {
                                         event.setCancelled(true);
                                     }
                                 }
                             } else if (sub.getDamager().toString().equalsIgnoreCase("CraftGhast"))
                             {
                                 if (sancZone != null)
                                 {
                                     if (!sancZone.getExplode().getGhast())
                                     {
                                         event.setCancelled(true);
                                     }
                                 }
                             }
                         }
                     } else if (event.getCause() == EntityDamageEvent.DamageCause.BLOCK_EXPLOSION)
                     {
                         if (sancZone != null)
                         {
                             if (!sancZone.getExplode().getTNT())
                             {
                                 event.setCancelled(true);
                             }
                         }
                     } else if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION)
                     {
                         if (sancZone != null)
                         {
                             if (!sancZone.getExplode().getCreeper())
                             {
                                 event.setCancelled(true);
                             }
                         }
                     } else if (event.getCause() == EntityDamageEvent.DamageCause.FIRE || event.getCause() == EntityDamageEvent.DamageCause.FIRE_TICK)
                     {
                         if (sancZone != null)
                         {
                             if (!sancZone.getFire().getIgnite())
                             {
                                 if (isPlayer(e))
                                 {
                                     e.setFireTicks(0);
                                     event.setCancelled(true);
                                 } else if (!sancZone.getFireBurnsMobs())
                                 {
                                     e.setFireTicks(0);
                                     event.setCancelled(true);
                                 }
                             }
                         }
                     }
                 } else //This is a sanctuary zone, no damage allowed to players.
                 {
                     if (isPlayer(e))
                     {
                         e.setFireTicks(0);
                         event.setCancelled(true);
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
     @EventHandler
     public void onCreatureSpawn(CreatureSpawnEvent event)
     {
         try
         {
             //Log.Write("Entity Creature Spawn Event! [" + event.getEntity().getType().toString() + "]");
             if (!event.isCancelled() && spawnReasonsToBlock.contains(event.getSpawnReason()))
             {
                 EpicZone zone = General.GetZoneForPlayer(null, event.getLocation().getWorld().getName(), event.getLocation().getBlockY(), new Point(event.getLocation().getBlockX(), event.getLocation().getBlockZ()));
                 if (zone != null)
                 {
                     if (zone.getMobs() != null) //If null assume all
                     {
                         if (zone.getMobs().size() > 0) //If size = 0 assume all
                         {
                             if (!zone.getMobs().contains("ALL"))
                             {
                                 if (zone.getMobs().contains("NONE") || !zone.getMobs().contains(event.getEntityType().toString()))
                                 {
                                     event.setCancelled(true);
                                 }
                             }
                         }
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
     @EventHandler
     public void onEntityChangeBlock(EntityChangeBlockEvent event)
     {
         try
         {
             //Log.Write("Entity Change Block Event! [" + event.getEntity().getType().toString() + "]");
             if (!event.isCancelled())
             {
                 if (event.getEntity().getType() == EntityType.ENDERMAN)
                 {
                     EpicZone zone = General.GetZoneForPlayer(null, event.getBlock().getLocation().getWorld().getName(), event.getBlock().getLocation().getBlockY(), new Point(event.getBlock().getLocation().getBlockX(), event.getBlock().getLocation().getBlockZ()));
                     if (zone != null)
                     {
                         if (!zone.getAllowEndermenPick())
                         {
                             event.setCancelled(true);
                         }
                     }
                 }
             }
         } catch (Exception e)
         {
             Log.Write(e.getMessage());
         }
     }
 
 //    @EventHandler
 //    public void onSheepRegrowWool(SheepRegrowWoolEvent event)
 //    {
 //        Log.Write("Wool Grow!");
 //    }
 
     private boolean isPlayer(Entity entity)
     {
 
         boolean result = false;
 
        if (entity instanceof Projectile)
        {
            result = isPlayer(((Projectile) entity).getShooter());
        } else if (entity instanceof Player)
         {
             Player player = (Player) entity;
             if (General.getPlayer(player.getName()) != null)
             {
                 result = true;
             }
         }
 
         return result;
 
     }
 
 }
