 package com.Top_Cat.CODMW.gamemodes;
 
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Random;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import net.minecraft.server.EntityItem;
 
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.block.Sign;
 import org.bukkit.craftbukkit.entity.CraftEntity;
 import org.bukkit.craftbukkit.entity.CraftWolf;
 import org.bukkit.entity.Arrow;
 import org.bukkit.entity.Creature;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Item;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.event.player.PlayerMoveEvent;
 import org.bukkit.event.player.PlayerPickupItemEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkitcontrib.BukkitContrib;
 
 import com.Top_Cat.CODMW.main;
 import com.Top_Cat.CODMW.team;
 import com.Top_Cat.CODMW.objects.chopper;
 import com.Top_Cat.CODMW.objects.claymore;
 import com.Top_Cat.CODMW.objects.player;
 import com.Top_Cat.CODMW.objects.sentry;
 import com.Top_Cat.CODMW.sql.Achievement;
 import com.Top_Cat.CODMW.sql.Stat;
 
 public class gamemode {
     
     main plugin;
     ArrayList<ArrayList<Location>> spawns = new ArrayList<ArrayList<Location>>();
     HashMap<Arrow, Location> ploc = new HashMap<Arrow, Location>();
     ArrayList<String> lossmesssages = new ArrayList<String>();
     ArrayList<String> winmesssages = new ArrayList<String>();
     public Random generator = new Random();
     Location d1, d2, d3, d4;
     boolean dl = false;
     int time = 0;
     Timer t = new Timer();
     
     public gamemode(main instance) {
         plugin = instance;
         
         setup();
         
         lossmesssages.add("They have won the battle but not the war!");
         lossmesssages.add("We have made a lot of mistakes you are going to regret");
         lossmesssages.add("God. You're such Gigs");
         winmesssages.add("Good job seals");
         winmesssages.add("You will receive cake for your victory here!");
         
         scheduleGame();
     }
     
     public void sendMessage(team t, String s, Player exclude) {
         for (Player p : plugin.players.keySet()) {
             if ((t == team.BOTH || t == plugin.p(p).getTeam()) && p != exclude) {
                 p.sendMessage(s);
             }
         }
     }
     
     public void sendMessage(team t, String s) {
         sendMessage(t, s, null);
     }
     
     public class tick implements Runnable {
         @Override
         public void run() { try { tick(); } catch (Exception e) { e.printStackTrace(); } }
     }
     
     public class tickone implements Runnable {
         @Override
         public void run() { time++; }
     }
     
     public class tickfast implements Runnable {
         @Override
         public void run() { try { tickfast(); } catch (Exception e) { e.printStackTrace(); } }
     }
     
     public boolean spawnCheck(Location i) {
         return ((d1 == null || i.distance(d1) > 7) && (d2 == null || i.distance(d2) > 7) && (d3 == null || i.distance(d3) > 7) && (d4 == null || i.distance(d4) > 7));
     }
     
     public void scheduleGame() {
         t.schedule(new startgame(), 55000);
     }
 
     public class startgame extends TimerTask {
 
         @Override
         public void run() {
             beginGame();
         }
 
     }
     
     public void beginGame() {
         if (plugin.activeGame == false) {
             if (plugin.players.size() >= plugin.minplayers) {
                 sendMessage(team.BOTH, plugin.d + "9Game starting in 5 seconds!");
                 plugin.r.countdowns();
                 plugin.activeGame = true;
             } else {
                 scheduleGame();
             }
         }
     }
     
     // BELOW ARE INTERFACES
     
     public void setup() {};
     
     public void spawnPlayer(Player p, boolean start) {
         player _p = plugin.p(p);
         _p.spawn = new Date().getTime();
         _p.clearinv();
         _p.setinv();
         
        if (!start) {
             _p.giveItem(2, new ItemStack(Material.WALL_SIGN, _p.last.clays));
             _p.giveItem(3, new ItemStack(Material.APPLE, _p.last.apples));
             _p.giveItem(4, new ItemStack(Material.BONE, _p.last.dogs));
             _p.giveItem(5, new ItemStack(Material.DISPENSER, _p.last.sentry));
             _p.giveItem(6, new ItemStack(Material.DIAMOND, _p.last.chop));
         }
         
         spawnTele(_p, p, start);
         _p.dead = false;
     }
     
     public Location spawnTele(player _p, Player p, boolean start) { return null; };
     
     public void startGame() {
         sendMessage(team.BOTH, plugin.d + "9Game starting now!");
         for (Player p : plugin.players.keySet()) {
             plugin.p(p).resetScore();
             spawnPlayer(p, true);
         }
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tick(), 40L, 40L);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tickone(), 20L, 20L);
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new tickfast(), 2L, 2L);
     }
     
     public void onWin(team winners, player lastkill, player lastdeath) {
         destroy();
         plugin.loadmap();
         
         for (player i : plugin.players.values()) {
             i.dead = false;
             i.clearinv();
         }
         
         if (lastdeath != null) {
             lastdeath.p.getInventory().setHelmet(new ItemStack(Material.WOOD, 1));
             lastdeath.s.incStat(Stat.LAST_DEATH);
         }
         if (lastkill != null) {
             lastkill.s.incStat(Stat.LAST_KILL);
         }
         
         sendMessage(team.BOTH, plugin.d + "9Game ended, game will resume on '" + plugin.currentMap.name + "' in 60 seconds");
     }
     
     public void onKill(player attacker, player defender, Location l) {
         if (dl) {
             d1 = l;
             d3 = attacker.p.getLocation();
         } else {
             d2 = l;
             d4 = attacker.p.getLocation();
         }
         dl = !dl;    
     }
     
     public void destroy() {
         plugin.getServer().getScheduler().cancelTasks(plugin);
         BukkitContrib.getAppearanceManager().resetAllCloaks();
         for (claymore i : plugin.clays) {
             i.b.setType(Material.AIR);
         }
         for (Wolf i : plugin.wolves.keySet()) {
             i.remove();
         }
         for (sentry i : plugin.sentries) {
             i.destroy();
         }
         for (chopper i : plugin.choppers) {
             i.destroy();
         }
         plugin.clays.clear();
         plugin.wolves.clear();
         plugin.sentries.clear();
         plugin.choppers.clear();
         plugin.activeGame = false;
         t.cancel();
     }
     
     public void onRespawn(Player p) {}
     
     public void tick() {
         for (player p : plugin.players.values()) {
             if (p.vtime < new Date().getTime()) {
                 p.p.getInventory().clear(38);
                 p.p.getInventory().clear(37);
                 p.p.getInventory().clear(36);
                 p.inv = false;
             }
             int life = (int) (new Date().getTime() - p.spawn);
             if (life > 300000) {
                 p.s.awardAchievement(Achievement.BEAR_GRYLLS);
             } else if (life > 180000) {
                 p.s.awardAchievement(Achievement.CAMPER);
             } else if (life > 120000) {
                 p.s.awardAchievement(Achievement.HIDING);
             }
             if (p.dead) {
                 onRespawn(p.p);
                 if (p.todrop > 0) {
                     plugin.currentWorld.dropItem(p.dropl, new ItemStack(Material.FEATHER, p.todrop));
                     p.todrop = 0;
                 }
                 spawnPlayer(p.p, false);
             }
             if (p.htime < new Date().getTime()) {
                 p.incHealth(-1, null, 0, null);
             }
             p.p.setHealth(p.h * 10);
         }
         List<Wolf> r = new ArrayList<Wolf>();
         for (Wolf i : plugin.wolves.keySet()) {
             if (((CraftWolf) i).getHandle().pathEntity.b()) {
                 System.out.println("Wolf b");
             }
             if (plugin.wolves.get(i).expire < new Date().getTime()) {
                 i.remove();
                 r.add(i);
             }
         }
         for (Wolf j : r) {
             plugin.wolves.remove(j);
         }
     }
     
     public void tickfast() {
         List<claymore> r = new ArrayList<claymore>();
         List<Entity> r2 = new ArrayList<Entity>();
         for (Entity i : plugin.currentWorld.getEntities()) {
             if (i instanceof Arrow) {
                 Location l = i.getLocation();
                 for (claymore j : plugin.clays) {
                     if (j.b.getLocation().add(0.5, 0, 0.5).distance(l) < 1) {
                         j.setOwner((Player) ((Arrow)i).getShooter(), plugin.p((Player) ((Arrow)i).getShooter()));
                         j.kill();
                         r.add(j);
                     }
                 }
                 if (ploc.containsKey(i)) {
                     if (l.distance(ploc.get(i)) < 0.1) {
                         r2.add(i);
                         
                         for (chopper j : plugin.choppers) {
                             if (j.l.distance(l) < 1.5) {
                                 j.arrowhit();
                             }
                         }
                         
                     }
                 }
                 ploc.put((Arrow) i, l);
             } else if (i instanceof Item) {
                 int itemId = ((EntityItem)((CraftEntity)i).getHandle()).itemStack.id;
                 if (!plugin.playerListener.allowed_pickup.contains(Material.getMaterial(itemId))) {
                     r2.add(i);
                 }
             } else if (i instanceof Creature && !(i instanceof Wolf)) {
                 r2.add(i);
             }
             for (Entity j : r2) {
                 j.remove();
             }
         }
         for (claymore i : plugin.clays) {
             if (i.init < new Date().getTime() && i.b.getType() != Material.WALL_SIGN) {
                 i.b.setType(Material.WALL_SIGN);
                 
                 switch (i.r) {
                     case 1: i.b.setData((byte) 4); break;
                     case 2: i.b.setData((byte) 2); break;
                     case 3: i.b.setData((byte) 5); break;
                     case 4: i.b.setData((byte) 3); break;
                 }
                 
                 if (i.b.getState() instanceof Sign) {
                     Sign s = (Sign) i.b.getState();
                     s.setLine(0, getClaymoreText(i.getOwner()));
                     s.setLine(3, getClaymoreText(i.getOwner()));
                     
                     s.setLine(1, "This side");
                     s.setLine(2, "towards enemy");
                     s.update();
                 }
             }
             if (i.exploded && i.explode < new Date().getTime()) {
                 i.kill();
                 r.add(i);
             }
         }
         plugin.clays.removeAll(r);
     }
     
     public void printScore(Player p, team t) {};
     
     public void playermove(PlayerMoveEvent event) {};
     
     public void playerpickup(PlayerPickupItemEvent event, Material pickedup) {};
     
     public class tele extends TimerTask {
         
         @Override
         public void run() {
             for (Player i : plugin.totele) {
                 jointele(i);
             }
             plugin.totele.clear();
         }
         
     }
     
     public void playerjoin(PlayerJoinEvent event) {
         plugin.setDoors();
         plugin.totele.add(event.getPlayer());
         t.schedule(new tele(), 200);
         plugin.clearinv(event.getPlayer());    
         String nick = event.getPlayer().getDisplayName();
         ResultSet r = plugin.sql.query("SELECT * FROM cod_players WHERE username = '" + event.getPlayer().getDisplayName() + "'");
         try {
             if (r.next()) {
                 nick = r.getString("nick");
             } else {
                 int id = plugin.sql.update("INSERT INTO cod_players VALUES (NULL, '" + event.getPlayer().getDisplayName() + "', '" + event.getPlayer().getDisplayName() + "')");
                 plugin.sql.update("INSERT INTO cod_stats VALUES (NULL, '" + id + "', '0', '1000')");
             }
         } catch (SQLException e) {
             e.printStackTrace();
         }
         
         event.setJoinMessage(plugin.d + "9" + nick + " has joined the fray");
         event.getPlayer().sendMessage(plugin.d + "9Welcome to The Gigcast's MineCod Server!");
         event.getPlayer().setHealth(20);
     }
     
     public void jointele(Player p) {};
     
     public boolean canHit(LivingEntity a, LivingEntity d) { return false; };
     
     public String getClaymoreText(Player p) { return "^^ CLAYMORE ^^"; };
     
     public player getTopPlayer(team t) { return null; }
     
 }
