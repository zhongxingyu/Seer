 package com.Top_Cat.CODMW.listeners;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 import java.util.TimerTask;
 
 import net.minecraft.server.EntityItem;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Block;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Result;
 import org.bukkit.event.block.Action;
 import org.bukkit.event.player.PlayerBedEnterEvent;
 import org.bukkit.event.player.PlayerChatEvent;
 import org.bukkit.event.player.PlayerDropItemEvent;
 import org.bukkit.event.player.PlayerInteractEvent;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.event.player.PlayerQuitEvent;
 import org.bukkit.event.player.PlayerToggleSneakEvent;
 import org.bukkit.inventory.Inventory;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 
 import com.Top_Cat.CODMW.main;
 import com.Top_Cat.CODMW.team;
 import com.Top_Cat.CODMW.Killstreaks.Killstreaks;
 import com.Top_Cat.CODMW.Killstreaks.killstreak;
 import com.Top_Cat.CODMW.Killstreaks.useable;
 import com.Top_Cat.CODMW.objects.grenade;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.sql.Stat;
 
 public class CODPlayerListener extends PlayerListener {
     
     main plugin;
     public ArrayList<Material> allowed_pickup = new ArrayList<Material>();
     Random generator = new Random();
     
     public CODPlayerListener(main instance) {
         plugin = instance;
         allowed_pickup.add(Material.FEATHER);
         allowed_pickup.add(Material.DIAMOND_BLOCK);
         allowed_pickup.add(Material.GOLD_BLOCK);
         allowed_pickup.add(Material.RAW_FISH);
         allowed_pickup.add(Material.SULPHUR);
         allowed_pickup.addAll(Killstreaks.table2.keySet());
     }
     
     @Override
     public void onPlayerDropItem(PlayerDropItemEvent event) {
         CraftEntity item = (CraftEntity)event.getItemDrop();
         int itemId = ((EntityItem)item.getHandle()).itemStack.id;
         if (!allowed_pickup.contains(Material.getMaterial(itemId))) {
             event.setCancelled(true);
         } else {
             player p = plugin.p(event.getPlayer());
             if (p != null) {
                 p.s.incStat(Stat.ITEMS_THROWN, event.getItemDrop().getItemStack().getAmount());
             }
         }
     }
     
     @Override
     public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
         if (event.isSneaking()) {
             event.setCancelled(true);
         }
     }
     
     @Override
     public void onPlayerJoin(PlayerJoinEvent event) {
         plugin.game.playerjoin(event);
     }
     
     @Override
     public void onPlayerQuit(PlayerQuitEvent event) {
         if (plugin.players.containsKey(event.getPlayer())) {
             plugin.p(event.getPlayer()).destroy();
             plugin.players.remove(event.getPlayer());
             
             int d = (int) Math.floor(Math.abs(plugin.diam - plugin.gold) / 2);
             if (d > 0) {
                 nextbalance = new Date().getTime() + 10000;
                 
             }
         }
         for (player i : plugin.players.values()) {
             if (i.assist == event.getPlayer()) {
                 i.assist = null;
             }
         }
     }
     
     long nextbalance = 0;
     
     public class balanceteams extends TimerTask {
 
         @Override
         public void run() {
             if (nextbalance < new Date().getTime()) {
                 int d = (int) Math.floor(Math.abs(plugin.diam - plugin.gold) / 2);
                 team b = team.DIAMOND;
                 if (plugin.gold > plugin.diam) {
                     b = team.GOLD;
                 }
                 
                 if (d > 0) {
                     plugin.game.sendMessage(team.BOTH, "Balancing teams...");
                     ArrayList<player> bigteam = new ArrayList<player>();
                     for (player i : plugin.players.values()) {
                         if (i.getTeam() == b) {
                             bigteam.add(i);
                         }
                     }
                     for (int i = 0; i < d; i++) {
                         plugin.switchplayer(bigteam.remove(generator.nextInt(bigteam.size())).p);
                     }
                 }
             }
         }
         
     }
     
     Block lastB;
 
     @Override
     public void onPlayerMove(PlayerMoveEvent event) {
         plugin.game.playermove(event);
         Location t = event.getTo();
         for (killstreak i : plugin.ks) {
             i.onMove(event, t.getBlock() != lastB);
         }
         if (t.getBlock() != lastB) {
             lastB = t.getBlock();
             team e;
             
             player p = plugin.p(event.getPlayer());
             if (p != null) {
                 p.s.incStat(Stat.BLOCKS_MOVED);
             }
             
             if (t.getX() > -10 && t.getX() < -8 && t.getZ() > 14 && t.getZ() < 16 && t.getBlockY() == 64) {
                 e = team.GOLD;
             } else if (t.getX() > -10 && t.getX() < -8 && t.getZ() > 10 && t.getZ() < 12 && t.getBlockY() == 64) {
                 e = team.DIAMOND;
             } else if (t.getX() > -8 && t.getX() < -6 && t.getZ() > 12 && t.getZ() < 14 && t.getBlockY() == 64) {
                 if (plugin.diam > plugin.gold) {
                     e = team.GOLD;
                 } else if (plugin.gold > plugin.diam) {
                     e = team.DIAMOND;
                 } else if (generator.nextInt(2) > 0) {
                     e = team.DIAMOND;
                 } else {
                     e = team.GOLD;
                 }
             } else {
                 return;
             }
             if (!plugin.players.containsKey(event.getPlayer())) {
                 new player(plugin, event.getPlayer(), e);
             } else {
                 plugin.players.get(event.getPlayer()).setTeam(e);
                 if (plugin.activeGame) {
                     plugin.game.spawnTele(plugin.p(event.getPlayer()), event.getPlayer(), false);
                 } else {
                     plugin.players.get(event.getPlayer()).dead = false;
                 }
                 recount();
             }
             plugin.setDoors();
         }
     }
     
     public void recount() {
         plugin.diam = 0;
         plugin.gold = 0;
         plugin.tot = plugin.players.size();
         for (player i : plugin.players.values()) {
             if (i.getTeam() == team.DIAMOND) {
                 plugin.diam++;
             } else {
                 plugin.gold++;
             }
         }
     }
     
     private int inv_count(Inventory in, List<Material> m) {
     	int out = 0;
         for (ItemStack i : in.getContents()) {
             if (i != null) {
                 if (m.contains(i.getType())) {
                     out += i.getAmount();
                 }
             }
         }
         return out;
     }
     
     private int inv_count(Inventory in, Material m) {
     	return inv_count(in, Arrays.asList(m));
     }
     
     @Override
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         int itemId = event.getItem().getItemStack().getType().getId();
         int amm = event.getItem().getItemStack().getAmount();
         Material um = Material.getMaterial(itemId);
         if (!allowed_pickup.contains(um)) {
             event.setCancelled(true);
         } else if (um == Material.FEATHER) {
             int ammo = inv_count(event.getPlayer().getInventory(), Material.FEATHER);
             event.getPlayer().getInventory().remove(Material.FEATHER);
             if (ammo < 99) {
                 event.getItem().remove();
                 amm += ammo;
             } else {
                 amm = ammo;
             }
             if (amm > 99) { amm = 99; }
             if (plugin.players.containsKey(event.getPlayer())) {
                 plugin.p(event.getPlayer()).s.incStat(Stat.AMMO_PICKED_UP, amm - ammo);
             }
             PlayerInventory i = event.getPlayer().getInventory();
             if (i.getItem(7) != null && i.getItem(7).getType() != Material.FEATHER && i.getItem(7).getType() != Material.AIR) {
                 i.addItem(new ItemStack(Material.FEATHER, amm));
             } else {
                 i.setItem(7, new ItemStack(Material.FEATHER, amm));
             }
             event.setCancelled(true);
         }
         plugin.game.playerpickup(event, Material.getMaterial(itemId));
     }
     
     @Override
     public void onPlayerChat(PlayerChatEvent event) {
         if (plugin.players.containsKey(event.getPlayer())) {
         	event.setCancelled(true);
         	for (Player i : plugin.getServer().getOnlinePlayers()) {
         		i.sendMessage("<" + plugin.p(event.getPlayer()).nick + "> " + plugin.d + plugin.p(event.getPlayer()).getTeam().getColour() + event.getMessage());
         	}
         }
     }
     
     @SuppressWarnings("unchecked")
     @Override
     public void onPlayerInteract(PlayerInteractEvent event) {
         event.setUseInteractedBlock(Result.DENY);
         event.setUseItemInHand(Result.ALLOW);
         if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
             Killstreaks s = Killstreaks.fromMaterial(event.getPlayer().getItemInHand().getType());
             if (s != null && useable.class.isAssignableFrom(s.getkClass())) {
                 event.setUseItemInHand(Result.DENY);
                 event.getPlayer().getInventory().removeItem(new ItemStack(event.getPlayer().getItemInHand().getType(), 1));
                 s.callIn(plugin, event.getPlayer(), new Object[] {});
             } else if (event.getPlayer().getItemInHand().getType() == Material.BOW) {
                 if (plugin.p(event.getPlayer()).rtime < new Date().getTime()) {
                     plugin.p(event.getPlayer()).stime = new Date().getTime() + 3000;
                     if (event.getPlayer().getInventory().contains(Material.ARROW)) {
                         plugin.p(event.getPlayer()).s.incStat(Stat.ARROWS_FIRED);
                     }
                 } else {
                     event.setUseItemInHand(Result.DENY);
                     event.getPlayer().updateInventory();
                 }
             } else if (event.getPlayer().getItemInHand().getType() == Material.SNOW_BALL) {
             	new grenade(plugin, event.getPlayer());
             	event.getPlayer().getInventory().removeItem(new ItemStack(Material.SNOW_BALL, 1));
             	event.setUseItemInHand(Result.DENY);
             } else if (event.getPlayer().getItemInHand().getType() == Material.RAW_FISH) {
                 event.setUseItemInHand(Result.DENY);
             }
         }
         for (killstreak i : (ArrayList<killstreak>) plugin.ks.clone()) {
             i.onInteract(event);
         }
     }
     
     @Override
     public void onPlayerBedEnter(PlayerBedEnterEvent event) {
         event.setCancelled(true);
     }
 }
