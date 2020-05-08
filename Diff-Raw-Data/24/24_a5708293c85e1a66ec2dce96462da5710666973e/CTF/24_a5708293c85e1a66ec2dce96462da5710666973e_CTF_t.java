 package com.Top_Cat.CODMW.gamemodes;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 
 import com.Top_Cat.CODMW.main;
 import com.Top_Cat.CODMW.team;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.gamemodes.ECTF.flag;
 
 public class CTF extends team_gm {
 
     int diam, gold;
     flag f1, f2;
     boolean swap = false;
     HashMap<player, Integer> scores = new HashMap<player, Integer>();
     
     public CTF(main instance) {
         super(instance);
     }
     
     @Override
     public void setup() {
         spawns.add(0, plugin.currentMap.getSpawns(0));
         spawns.add(1, plugin.currentMap.getSpawns(1));
         spawns.add(2, plugin.currentMap.getSpawns(2));
         f1 = new flag(plugin, plugin.currentMap.getSpawns(3).get(0), Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.FENCE, team.DIAMOND, team.GOLD);
         f2 = new flag(plugin, plugin.currentMap.getSpawns(4).get(0), Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.FENCE, team.GOLD, team.DIAMOND);
     }
     
     @Override
     public void startGame() {
         super.startGame();
         diam = 0;
         gold = 0;
     }
     
     @Override
     public void destroy() {
         super.destroy();
         f1.destroy();
         f2.destroy();
     }
     
     @Override
     public void onKill(player attacker, player defender, Location l) {
         super.onKill(attacker, defender, l);
         attacker.addPoints(3);
         defender.addPoints(-1);
         
         if (defender == plugin.p(f1.p)) {
            f1.drop = l;
         } else if (defender == plugin.p(f2.p)) {
            f2.drop = l;
         }
     }
     
     @Override
     public void onRespawn(Player p) {
         if (f1.drop != null) {
             f1.p = null;
             f1.drop_i = plugin.currentWorld.dropItem(f1.drop, new ItemStack(Material.GOLD_BLOCK, 1));
             f1.ret = new Date().getTime() + 20000;
             f1.drop = null;
             f1.toret = true;
         } else if (f2.drop != null) {
             f2.p = null;
             f2.drop_i = plugin.currentWorld.dropItem(f2.drop, new ItemStack(Material.DIAMOND_BLOCK, 1));
             f2.ret = new Date().getTime() + 20000;
             f2.drop = null;
             f2.toret = true;
         }
     }
     
     @Override
     public void tick() {
         super.tick();
         if (f1.toret == true && f1.ret < new Date().getTime()) {
             f1.drop_i.remove();
             f1.toret = false;
             f1.returnFlag(null, false);
         } else if (f2.toret == true && f2.ret < new Date().getTime()) {
             f2.drop_i.remove();
             f2.toret = false;
             f2.returnFlag(null, false);
         }
         if (time > 300 && swap == false) {
             sendMessage(team.BOTH, "Switching sides!");
             Location temp = f1.l;
             Location temp2 = f2.l;
             f1.destroy();
             f2.destroy();
             f1 = new flag(plugin, temp2, Material.DIAMOND_BLOCK, Material.GOLD_BLOCK, Material.FENCE, team.DIAMOND, team.GOLD);
             f2 = new flag(plugin, temp, Material.GOLD_BLOCK, Material.DIAMOND_BLOCK, Material.FENCE, team.GOLD, team.DIAMOND);
             swap = true;
             for (Player i : plugin.players.keySet()) {
                 plugin.p(i).setStreaks();
                 spawnPlayer(i, true);
             }
         } else if (time > 600) {
             team d = team.GOLD;
             if (diam > gold) {
                 d = team.DIAMOND;
             } else if (diam == gold) {
                 d = team.BOTH;
             }
             onWin(d, null, null);
         }
     }
     
     @Override
     public void playerpickup(PlayerPickupItemEvent event, Material pickedup) {
         if (pickedup == Material.DIAMOND_BLOCK) {
             event.setCancelled(true);
             event.getItem().remove();
             if (plugin.p(event.getPlayer()).getTeam() == team.DIAMOND) {
                 f2.takeFlag(event.getPlayer());
             } else {
                 f2.returnFlag(null, false);
             }
         } else if (pickedup == Material.GOLD_BLOCK) {
             event.setCancelled(true);
             event.getItem().remove();
             if (plugin.p(event.getPlayer()).getTeam() == team.GOLD) {
                 f1.takeFlag(event.getPlayer());
             } else {
                 f1.returnFlag(null, false);
             }
         }
     }
     
     @Override
     public void playermove(PlayerMoveEvent event) {
         if (event.getTo().distance(f1.l) < 2) {
             if (plugin.p(event.getPlayer()).getTeam() != f1.t) {
                 f1.takeFlag(event.getPlayer());
                 plugin.p(event.getPlayer()).addPoints(2);
             } else if (event.getPlayer() == f2.p) {
                 f2.returnFlag(plugin.p(event.getPlayer()).getTeam() == team.DIAMOND ? Material.DIAMOND_HELMET : Material.GOLD_HELMET, true);
                 if (f2.t == team.GOLD) {
                     diam++;
                 } else {
                     gold++;
                 }
                 int score = scores.containsKey(plugin.p(event.getPlayer())) ? scores.get(plugin.p(event.getPlayer())) : 0;
                 scores.put(plugin.p(event.getPlayer()), score + 1);
                 plugin.p(event.getPlayer()).addPoints(7);
             }
         } else if (event.getTo().distance(f2.l) < 2) {
             if (plugin.p(event.getPlayer()).getTeam() != f2.t) {
                 f2.takeFlag(event.getPlayer());
                 plugin.p(event.getPlayer()).addPoints(2);
             } else if (event.getPlayer() == f1.p) {
                 f1.returnFlag(plugin.p(event.getPlayer()).getTeam() == team.DIAMOND ? Material.DIAMOND_HELMET : Material.GOLD_HELMET, true);
                 if (f2.t == team.GOLD) {
                     gold++;
                 } else {
                     diam++;
                 }
                 int score = scores.containsKey(plugin.p(event.getPlayer())) ? scores.get(plugin.p(event.getPlayer())) : 0;
                 scores.put(plugin.p(event.getPlayer()), score + 1);
                 plugin.p(event.getPlayer()).addPoints(7);
             }
         }
     }
     
     @Override
     public Location spawnTele(player _p, Player p, boolean start) {
         Location g = null;
         ArrayList<Player> alivePlayers = new ArrayList<Player>();
         for (player i : plugin.players.values()) {
             if (i.dead == false && i.getTeam() == _p.getTeam() && i.stime < new Date().getTime() && spawnCheck(i.p.getLocation())) {
                 alivePlayers.add(i.p);
             }
         }
         if (alivePlayers.size() <= 1 || start) {
             if (start) {
                 team t = _p.getTeam();
                 if (swap == true) {
                     if (t == team.DIAMOND) {
                         t = team.GOLD;
                     } else {
                         t = team.DIAMOND;
                     }
                 }
                 switch (t) {
                     case DIAMOND: g = spawns.get(0).get(generator.nextInt(spawns.get(0).size())); break;
                     case GOLD: g = spawns.get(1).get(generator.nextInt(spawns.get(1).size())); break;
                 }
             } else {
                 ArrayList<Location> spawn = new ArrayList<Location>();
                 for (Location i : spawns.get(2)) {
                     if (spawnCheck(i)) { spawn.add(i); }
                 }
                 g = spawn.get(generator.nextInt(spawn.size()));
             }
         } else {
             g = alivePlayers.get(generator.nextInt(alivePlayers.size())).getLocation();
         }
         _p.stime = new Date().getTime() + 5000;
         p.teleport(g);
         if (start) {
             p.sendMessage(plugin.d + _p.getTeam().getColour() + _p.getTeam().toString() + " team go!");
         }
         return g;
     }
     
     @Override
     public void printScore(Player p, team t) {
         if (t == team.BOTH) {
             p.sendMessage(plugin.d + "6Gold: " + gold + plugin.d + "f  " + plugin.d + "bDiamond: " + diam);
         } else if (t == team.GOLD || t == team.DIAMOND) {
             String c = "6";
             if (t == team.DIAMOND) {
                 c = "b";
             }
             
             p.sendMessage(plugin.d + c + "Player Name | Ki | A | D |");
             for (player i : plugin.players.values()) {
                 if (i.getTeam() == t) {
                     p.sendMessage(plugin.d + c + i.nick + " | " + i.kill + " | " + i.assists + " | " + i.death + " |");
                 }
             }
             p.sendMessage(plugin.d + c + (t == team.DIAMOND ? diam : gold));
         }
     }
     
     @Override
     public player getTopPlayer(team t) {
         player out = null;
         int mcaps = -1;
         for (player i : plugin.players.values()) {
             if (i.getTeam() == t) {
                 if (scores.containsKey(i) && scores.get(i) > mcaps) {
                     out = i;
                     mcaps = scores.get(i);
                 }
             }
         }
         return out;
     }
 
 }
