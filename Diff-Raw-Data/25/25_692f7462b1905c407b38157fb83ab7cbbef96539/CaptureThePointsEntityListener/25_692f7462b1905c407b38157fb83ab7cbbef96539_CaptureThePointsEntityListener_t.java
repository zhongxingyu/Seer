 package me.dalton.capturethepoints.listeners;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import me.dalton.capturethepoints.CTPPoints;
 import me.dalton.capturethepoints.CaptureThePoints;
 import me.dalton.capturethepoints.Items;
 import me.dalton.capturethepoints.Lobby;
 import me.dalton.capturethepoints.Util;
 import org.bukkit.ChatColor;
 import org.bukkit.DyeColor;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Projectile;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.material.Wool;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.util.config.Configuration;
 
 public class CaptureThePointsEntityListener extends EntityListener {
 
     private CaptureThePoints ctp;
 
     public double loadDouble(String s) {
         Configuration config = this.ctp.load();
         return config.getDouble(s, 0.0D);
     }
 
     public Integer loadInt(String s) {
         Configuration config = this.ctp.load();
         return Integer.valueOf(config.getInt(s, 0));
     }
 
     public boolean loadBoolean(String s) {
         Configuration config = this.ctp.load();
         return config.getBoolean(s, false);
     }
 
     public String loadString(String s) {
         Configuration config = this.ctp.load();
         return config.getString(s, "");
     }
 
     public CaptureThePointsEntityListener(CaptureThePoints plugin) {
         this.ctp = plugin;
     }
 
     Plugin checkPlugin(String pluginname) {
         return this.ctp.getServer().getPluginManager().getPlugin(pluginname);
     }
 
     private boolean checkForPlayerEvent(EntityDamageEvent event) {
         if (!(event instanceof EntityDamageByEntityEvent)) {
             return false;
         }
         // You now know the player getting damaged was damaged by another entity
         if (!(((EntityDamageByEntityEvent) event).getDamager() instanceof Player)) {
             return false;
         }
         // You now know the entity that is attacking is a player
         return true;
     }
 
     @Override
     public void onEntityDamage(EntityDamageEvent event)
     {
         if (!(event.getEntity() instanceof Player))
         {
             // Kj -- Didn't involve a player. So we don't care.
             return;
         }
         //Only check if game is running
         if (ctp.isGameRunning())
         {
             Player attacker = null;
             if ((this.ctp.playerData.get((Player) event.getEntity()) != null))
             {
                 
                 // for melee
                 if (checkForPlayerEvent(event))
                 {
                     attacker = ((Player) ((EntityDamageByEntityEvent) event).getDamager());
                 }
 
                 // for arrows
                 if ((event.getCause() == EntityDamageEvent.DamageCause.PROJECTILE) && (((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter() instanceof Player))
                 {
                     attacker = (Player) ((Projectile) ((EntityDamageByEntityEvent) event).getDamager()).getShooter();
                 }
 
                 Player playa = (Player) event.getEntity();
 
                 // Kj -- helmet checker
                 /*boolean helmetRemoved = ctp.playerListener.checkHelmet(attacker);
                 if (helmetRemoved)
                 {
                     ctp.playerListener.fixHelmet(attacker);
                     event.setCancelled(true);
                     return;
                 }*/
         
                 // lobby damage check
                if (attacker != null && this.ctp.playerData.get(attacker) != null && this.ctp.playerData.get(playa).isInLobby)
                 {
                     event.setCancelled(true);
                     return;
                 }
 
                 if (isProtected(playa)) {
                     // If you damage yourself
                     if (attacker != null) {
                         attacker.sendMessage(ChatColor.LIGHT_PURPLE + "You can't damage enemy in their spawn!");
                     }
                     event.setCancelled(true);
                     return;
                 }
 
                 //disable pvp damage
                 if (attacker != null) {
                     if ((this.ctp.playerData.get(playa) != null) && (this.ctp.playerData.get(attacker) != null)) {
                         if (this.ctp.playerData.get(playa).color.equalsIgnoreCase(this.ctp.playerData.get(attacker).color)) {
                             attacker.sendMessage(ctp.playerData.get(playa).team.chatcolor + playa.getName() + ChatColor.LIGHT_PURPLE + " is on your team!");
                             event.setCancelled(true);
                             return;
                         } else {   // This is if there exists something like factions group protection
                             if (event.isCancelled()) {
                                 event.setCancelled(false);
                             }
                         }
                     }
                 }
 
                 //Player has "died"
                 if ((this.ctp.playerData.get(playa) != null) && (playa.getHealth() - event.getDamage() <= 0)) {
                     event.setCancelled(true);
                     //Send message to all players
                     if (attacker != null) {
                         Util.sendMessageToPlayers(ctp, ctp.playerData.get(playa).team.chatcolor + playa.getName() + ChatColor.WHITE
                                 + " was killed by " + ctp.playerData.get(attacker).team.chatcolor + attacker.getName());
                         dropWool(playa);
                         ctp.playerData.get(attacker).money += ctp.configOptions.moneyForKill;
                         attacker.sendMessage("Money: " + ChatColor.GREEN + ctp.playerData.get(attacker).money);
                         ctp.checkForKillMSG(attacker, false);
                         ctp.checkForKillMSG(playa, true);
                     }
 
                     playa.setHealth(ctp.configOptions.maxPlayerHealth);
                     playa.setFoodLevel(20);
                     CTPPoints point = ctp.mainArena.teamSpawns.get(ctp.playerData.get(playa).color);
                     if (ctp.configOptions.giveNewRoleItemsOnRespawn) {
                         giveRoleItemsAfterDeath(playa);
                     }
                     Location loc = new Location(ctp.getServer().getWorld(ctp.mainArena.world), point.x, point.y, point.z);
                     loc.setYaw((float) point.dir);
                     loc.getWorld().loadChunk(loc.getBlockX(), loc.getBlockZ());
                     playa.teleport(loc);
                 }
             }
         }
         if (ctp.playerData.get((Player) event.getEntity()) != null && ctp.playerData.get((Player) event.getEntity()).isInLobby)
         {
             event.setCancelled(true);
         }
     }
 
 //    public boolean hasLobbyProtection(Player player) {
 //        Lobby lobby = ctp.mainArena.lobby;
 //        Location protectionPoint = new Location(ctp.getServer().getWorld(ctp.mainArena.world), lobby.x, lobby.y, lobby.z);
 //        double distance = player.getLocation().distance(protectionPoint);
 //
 //        return distance <= ctp.configOptions.protectionDistance;
 //    }
 
     public boolean isProtected(Player player) {
         if (ctp.mainArena == null && player == null) // Kj -- null check
         {
             return false;
         }
 
         CTPPoints point = ctp.mainArena.teamSpawns.get(ctp.playerData.get(player).color);
         Location protectionPoint = new Location(ctp.getServer().getWorld(ctp.mainArena.world), point.x, point.y, point.z);
         double distance = player.getLocation().distance(protectionPoint);
 
         return distance <= ctp.configOptions.protectionDistance;
     }
 
     public void giveRoleItemsAfterDeath(Player player) {
         PlayerInventory inv = player.getInventory();
         for (Items item : ctp.roles.get(ctp.playerData.get(player).role)) {
             if (inv.contains(item.item)) {
                 if (!Util.ARMORS_TYPE.contains(item.item)/* && (!Util.WEAPONS_TYPE.contains(item.getType()))*/) {
                     HashMap<Integer, ? extends ItemStack> slots = inv.all(item.item);
                     int amount = 0;
                     for (int slotNum : slots.keySet()) {
                         amount = amount + slots.get(slotNum).getAmount();
                     }
                     // nzn apie sita
 //                    for (Iterator<Integer> i$ = slots.keySet().iterator(); i$.hasNext();)
 //                    {
 //                        int slotNum = i$.next().intValue();
 //                        amount += ((ItemStack) slots.get(Integer.valueOf(slotNum))).getAmount();
 //                    }
                     if (amount < item.amount) {
                         inv.remove(item.item);
                         inv.addItem(new ItemStack[]{new ItemStack(item.item, item.amount)});
                     }
                 } //Its armor
 //                else
 //                {
 //                    //Check if its equiped
 //                    if(Util.BOOTS_TYPE.contains(item.item))
 //                    {
 //                        //inv.remove(item.item);
 //                        if(inv.getBoots().getType() == item.item)
 //                            inv.setBoots(new ItemStack(item.item, 1));
 //                        else
 //                            inv.addItem(new ItemStack[] { new ItemStack(item.item, item.amount) });
 //
 //                    }
 //                    else if(Util.CHESTPLATES_TYPE.contains(item.item))
 //                    {
 //                        //inv.remove(item.item);
 //                        if(inv.getChestplate().getType() == item.item)
 //                            inv.setChestplate(new ItemStack(item.item, 1));
 //                        else
 //                            inv.addItem(new ItemStack[] { new ItemStack(item.item, item.amount) });
 //                    }
 //                    else if(Util.LEGGINGS_TYPE.contains(item.item))
 //                    {
 //                        if(inv.getLeggings().getType() == item.item)
 //                            inv.setLeggings(new ItemStack(item.item, 1));
 //                        else
 //                            inv.addItem(new ItemStack[] { new ItemStack(item.item, item.amount) });
 //                    }
 //                }
             } else {
                 if (!Util.ARMORS_TYPE.contains(item.item)) {
                     inv.addItem(new ItemStack[]{new ItemStack(item.item, item.amount)});
                 } else {// find if there is somethig equiped
                     if (Util.BOOTS_TYPE.contains(item.item)) {
                         if (inv.getBoots().getType() == item.item) {
                             inv.setBoots(new ItemStack(item.item, 1));
                         } else {
                             inv.addItem(new ItemStack[]{new ItemStack(item.item, item.amount)});
                         }
                     } else if (Util.LEGGINGS_TYPE.contains(item.item)) {
                         if (inv.getLeggings().getType() == item.item) {
                             inv.setLeggings(new ItemStack(item.item, 1));
                         } else {
                             inv.addItem(new ItemStack[]{new ItemStack(item.item, item.amount)});
                         }
                     } else if (Util.CHESTPLATES_TYPE.contains(item.item)) {
                         if (inv.getChestplate().getType() == item.item) {
                             inv.setChestplate(new ItemStack(item.item, 1));
                         } else {
                             inv.addItem(new ItemStack[]{new ItemStack(item.item, item.amount)});
                         }
                     }
                 }
             }
 
         }
     }
 
     private boolean dropWool(Player player) {
         if (!ctp.configOptions.dropWoolOnDeath) {
             return false;
         }
 
         PlayerInventory inv = player.getInventory();
         int ownedWool = 0;
         for (ItemStack item : inv.getContents()) {
             if (item != null && item.getTypeId() == 35) {
                 if (!((Wool) item.getData()).getColor().toString().equalsIgnoreCase(ctp.playerData.get(player).color)) {
                     inv.remove(35);
                     ItemStack tmp = new ItemStack(item.getType(), item.getAmount(), (short) ((Wool) item.getData()).getColor().getData());
                     player.getWorld().dropItem(player.getLocation(), tmp);
                 } else {
                     ownedWool = ownedWool + item.getAmount();
                 }
             }
         }
         inv.remove(Material.WOOL);
         if (ownedWool != 0) {
             DyeColor color = DyeColor.valueOf(ctp.playerData.get(player).color.toUpperCase());
             ItemStack wool = new ItemStack(35, ownedWool, color.getData());
             player.getInventory().addItem(new ItemStack[]{wool});
             player.updateInventory();
         }
         return true;
     }
 }
