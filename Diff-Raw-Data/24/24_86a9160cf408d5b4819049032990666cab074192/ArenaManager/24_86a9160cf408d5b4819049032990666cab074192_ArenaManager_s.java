 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package redcastlemedia.multitallented.bukkit.heromatchmaking.managers;
 
 import com.herocraftonline.heroes.Heroes;
 import com.herocraftonline.heroes.characters.Hero;
 import com.herocraftonline.heroes.characters.classes.HeroClass;
 import java.util.HashMap;
 import java.util.HashSet;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.inventory.InventoryType;
 import org.bukkit.inventory.Inventory;
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
     private HashMap<Integer, ArenaBuilder> arenas = new HashMap<Integer, ArenaBuilder>();
     private final World world;
     private HashMap<Player, ArenaBuilder> existingArenas = new HashMap<Player, ArenaBuilder>();
     private HashMap<Player, Location> previousLocation = new HashMap<Player, Location>();
     private HashMap<Player, Inventory> previousInventory = new HashMap<Player, Inventory>();
     private HashMap<Player, HeroClass> previousClass = new HashMap<Player, HeroClass>();
     private HashMap<Player, Integer> previousHealth = new HashMap<Player, Integer>();
     
     public ArenaManager() {
         YMLProxy config = (YMLProxy) Controller.getInstance("config");
         String worldName = config.getString("world", "world");
         System.out.println(worldName);
         World w = Bukkit.getWorld(worldName);
         this.world = w;
         if (w == null) {
             System.out.println("[HeroMatchMaking] invalid world name in config");
             return;
         }
     }
     
     public ArenaBuilder getExistingArena(Player p) {
         return existingArenas.get(p);
     }
     
     public void scheduleMatch(final HashSet<Player> players, ArenaBuilder ab) {
         for (Player p : players) {
             p.sendMessage(ChatColor.GOLD + "[HeroMatchMaking] Your match will begin in 5s");
         }
         
         Bukkit.getScheduler().scheduleSyncDelayedTask((JavaPlugin) Controller.getInstance("plugin"), 
                 new Runnable() {
 
                 @Override
                 public void run() {
                     //TODO put players in a match
                     setupNextArena(new RTSArenaBuilder(), players);
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
     }
     
     public void endMatch(Player p) {
         
     }
     
     private void setupNextArena(ArenaBuilder arena, HashSet<Player> players) {
         int j=0;
         for (Integer i : arenas.keySet()) {
             if (j != i) {
                 break;
             }
             j++;
         }
         Location l = new Location(world, 0 + (20*j), 0, 0);
         arena.build(l);
         arenas.put(j, arena);
         Object h = Controller.getInstance("heroes");
         Heroes heroes = null;
         if (h!=null) {
             heroes = (Heroes) h;
         }
         int i=0;
         for (Player p : players) {
             existingArenas.put(p, arena);
             previousLocation.put(p, p.getLocation());
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
             previousInventory.put(p, p.getInventory());
             Bukkit.createInventory(p, InventoryType.PLAYER);
             p.updateInventory();
             
             p.teleport(arena.getStartPoint(i));
             p.sendMessage(ChatColor.GOLD + "[HeroMatchMaking] Your match has begun!");
             i++;
         }
         
         
     }
 }
