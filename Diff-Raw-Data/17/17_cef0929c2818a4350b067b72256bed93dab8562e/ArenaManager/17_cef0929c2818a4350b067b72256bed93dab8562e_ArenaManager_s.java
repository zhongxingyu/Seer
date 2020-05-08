 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package redcastlemedia.multitallented.bukkit.heromatchmaking.managers;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerRespawnEvent;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.inventory.PlayerInventory;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.parts.redcastlemedia.multitallented.controllers.Controller;
 import org.parts.redcastlemedia.multitallented.models.YMLProxy;
 import redcastlemedia.multitallented.bukkit.heromatchmaking.builders.ArenaBuilder;
 import redcastlemedia.multitallented.bukkit.heromatchmaking.builders.RTSArenaBuilder;
 
 /**
  *
  * @author Multitallented
  */
 public class ArenaManager {
     private ArrayList<ArenaBuilder> arenas = new ArrayList<ArenaBuilder>();
     private final World world;
     private HashMap<Player, ArenaBuilder> existingArenas = new HashMap<Player, ArenaBuilder>();
     private HashMap<Player, Location> previousLocation = new HashMap<Player, Location>();
     private HashMap<Player, ArrayList<ItemStack>> previousInventory = new HashMap<Player, ArrayList<ItemStack>>();
     private HashMap<Player, HeroClass> previousClass = new HashMap<Player, HeroClass>();
     private HashMap<Player, Integer> previousHealth = new HashMap<Player, Integer>();
     private HashMap<Player, Integer> previousFood = new HashMap<Player, Integer>();
     private HashMap<Player, Integer> previousExp = new HashMap<Player, Integer>();
     
     public ArenaManager() {
         YMLProxy config = (YMLProxy) Controller.getInstance("config");
         String worldName = config.getString("world", "world");
         World w = Bukkit.getWorld(worldName);
         this.world = w;
         Logger log = Bukkit.getLogger();
         if (w == null) {
             log.warning("[HeroMatchMaking] invalid world name in config.");
             Bukkit.getPluginManager().disablePlugin(Bukkit.getPluginManager().getPlugin("HeroMatchMaking"));
             return;
         } else {
           log.info("[HeroMatchMaking] Using " + worldName + " for instanced matches.");
           log.warning("[HeroMatchMaking] Make sure there is nothing valuable in " + worldName);
         }
     }
     
     public ArenaBuilder getExistingArena(Player p) {
         return existingArenas.get(p);
     }
     
     public void scheduleMatch(final HashSet<Player> players, final ArenaBuilder ab) {
         for (Player p : players) {
             p.sendMessage(ChatColor.GOLD + "[HeroMatchMaking] Your match will begin in 5s");
         }
         
         Bukkit.getScheduler().scheduleSyncDelayedTask((JavaPlugin) Controller.getInstance("plugin"), 
                 new Runnable() {
 
                 @Override
                 public void run() {
                     //TODO put players in a match
                     setupNextArena(ab, players);
                 }
                     
                 }, 100l);
         
     }
     
     public void returnPlayer(Player p) {
         Hero hero = null;
         if (previousLocation.containsKey(p)) {
             p.teleport(previousLocation.get(p));
         }
         if (previousClass.containsKey(p)) {
             hero = ((Heroes) Controller.getInstance("heroes")).getCharacterManager().getHero(p);
             hero.changeHeroClass(previousClass.get(p), false);
         }
         if (previousHealth.containsKey(p)) {
             if (hero != null) {
                 hero.setHealth(previousHealth.get(p));
             } else {
                 p.setHealth(previousHealth.get(p));
             }
         }
         PlayerInventory pInv = p.getInventory();
         ArrayList<ItemStack> inv = previousInventory.get(p);
         if (inv.get(3) != null) {
             pInv.setBoots(inv.get(3));
         }
         if (inv.get(0) != null) {
             pInv.setHelmet(inv.get(0));
         }
         
         if (inv.get(2) != null) {
             pInv.setLeggings(inv.get(2));
         }
         if (inv.get(1) != null) {
             pInv.setChestplate(inv.get(1));
         }
         for (int k = 4; k<inv.size(); k++) {
             if (inv.get(k) != null) {
                 pInv.addItem(inv.get(k));
             }
         }
     }
     
     public void checkEndMatch(Player p) {
         PlayerManager pm = (PlayerManager) Controller.getInstance("playermanager");
         if (pm.hasFightingPlayer(p)) {
             pm.removePlayerLocation(p);
             pm.addRespawningPlayer(p);
             
         } else {
             return;
         }
         
         ArenaBuilder ab = existingArenas.get(p);
         existingArenas.remove(p);
         HashSet<HashSet<Player>> tempPlayers = ab.getPlayers();
         HashSet<HashSet<Player>> destroyThese = new HashSet<HashSet<Player>>();
         for (HashSet<Player> playerSet : tempPlayers) {
             if (playerSet.contains(p)) {
                 //TODO Record stats
                 if (playerSet.size() < 2) {
                     destroyThese.add(playerSet);
                     break;
                 } else {
                     playerSet.remove(p);
                     return;
                 }
             }
         }
         for (HashSet<Player> destroy : destroyThese) {
             tempPlayers.remove(destroy);
         }
         if (tempPlayers.size() > 1) {
             return;
         }
         
         //TODO record stats
         for (HashSet<Player> playerSet : tempPlayers) {
             for (Player pl : playerSet) {
                 pm.removePlayerLocation(pl);
                 if (existingArenas.containsKey(pl)) {
                     existingArenas.remove(pl);
                 }
                 returnPlayer(pl);
             }
         }
         
         
         //remove arena from list and make the arena available
        
        
        //TODO end the match
         //Clear all itemstacks in arena
     }
     
     public void setLocation(ArenaBuilder ab) {
         int j=0;
         Location prevLocation = new Location(world, 0, 0, 0);
         int prevSize = 999999999;
         int size = ab.getSize();
         if (arenas.isEmpty()) {
             ab.setLocation(0, new Location(world, 0, 0, 0));
         } else {
             int index = -1;
             for (ArenaBuilder arenaBuilder : arenas) {
                 if (arenas.size() > j+1) {
                     if (arenas.get(j+1).getLocation().getX() - size > prevSize + prevLocation.getX()) {
                         ab.setLocation(j, new Location(world, prevLocation.getX() + prevSize, 0, 0));
                         index = j;
                         break;
                     } else {
                         prevSize = arenaBuilder.getSize();
                         prevLocation = arenaBuilder.getLocation();
                         j++;
                         continue;
                     }
                 } else {
                    ab.setLocation(j+1, new Location(world, prevLocation.getX() + prevSize, 0, 0));
                     index = -2;
                 }
             }
             if (index == -2) {
                 arenas.add(ab);
             } else if (index != -1) {
                 arenas.add(j, ab);
             }
             
         }
     }
     
     private void setupNextArena(ArenaBuilder arena, HashSet<Player> players) {
         arena.build();
         arenas.add(arena.getID(), arena);
         
         HashSet<HashSet<Player>> play = new HashSet<HashSet<Player>>();
         Object h = Controller.getInstance("heroes");
         Heroes heroes = null;
         if (h!=null) {
             heroes = (Heroes) h;
         }
         int i=0;
         PlayerManager pm = (PlayerManager) Controller.getInstance("playermanager");
         for (Player p : players) {
             //TODO make this dynamic
             HashSet<Player> tempPlayers = new HashSet<Player>();
             tempPlayers.add(p);
             play.add(tempPlayers);
             //////////////////
             
             //TODO previous food
             //TODO previous exp
             existingArenas.put(p, arena);
             previousLocation.put(p, p.getLocation());
             pm.putPlayerLocation(p, arena);
             if (heroes != null) {
                 Hero hero = heroes.getCharacterManager().getHero(p);
                 previousHealth.put(p, hero.getHealth());
                 hero.setHealth(hero.getMaxHealth());
                 previousClass.put(p, hero.getHeroClass());
                 hero.changeHeroClass(heroes.getClassManager().getClass(((YMLProxy) Controller.getInstance("config")).getString("default-class")), false);
             } else {
                 previousHealth.put(p, p.getHealth());
                 p.setHealth(20);
             }
             previousInventory.put(p, copyInventory(p));
             p.getInventory().clear();
             p.updateInventory();
             
             p.teleport(arena.getStartPoint(i));
             p.sendMessage(ChatColor.GOLD + "[HeroMatchMaking] Your match has begun!");
             i++;
         }
         arena.setPlayers(play);
        
     }
     
     private ArrayList<ItemStack> copyInventory(Player p) {
         PlayerInventory inv = p.getInventory();
         ArrayList<ItemStack> pInv = new ArrayList<ItemStack>();
         
         pInv.add(inv.getHelmet());
         pInv.add(inv.getChestplate());
         pInv.add(inv.getLeggings());
         pInv.add(inv.getBoots());
         
         pInv.addAll(Arrays.asList(inv.getContents()));
         return pInv;
     }
 
     public void playerRespawned(PlayerRespawnEvent event) {
         Player p = event.getPlayer();
         event.setRespawnLocation(previousLocation.get(p));
         PlayerInventory pInv = p.getInventory();
         ArrayList<ItemStack> inv = previousInventory.get(p);
         if (inv.get(3) != null) {
             pInv.setBoots(inv.get(3));
         }
         if (inv.get(0) != null) {
             pInv.setHelmet(inv.get(0));
         }
         if (inv.get(2) != null) {
             pInv.setLeggings(inv.get(2));
         }
         if (inv.get(1) != null) {
             pInv.setChestplate(inv.get(1));
         }
         for (int k = 4; k<inv.size(); k++) {
             if (inv.get(k) != null) {
                 pInv.addItem(inv.get(k));
             }
         }
         Hero hero = null;
         if (previousClass.containsKey(p)) {
             hero = ((Heroes) Controller.getInstance("heroes")).getCharacterManager().getHero(p);
             if (hero != null) {
                 hero.changeHeroClass(previousClass.get(p), false);
             }
         }
         if (previousHealth.containsKey(p)) {
             if (hero != null) {
                 hero.setHealth(previousHealth.get(p));
             } else {
                 p.setHealth(previousHealth.get(p));
             }
         }
     }
 }
