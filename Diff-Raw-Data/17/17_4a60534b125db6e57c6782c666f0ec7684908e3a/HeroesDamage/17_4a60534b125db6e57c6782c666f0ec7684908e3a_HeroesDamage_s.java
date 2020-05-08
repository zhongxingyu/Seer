 package com.herocraftonline.dev.heroes.damage;
 
 import java.util.HashMap;
 
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.util.Properties;
 
 public class HeroesDamage {
     public Heroes plugin;
     private HashMap<Integer, Double> mobHealthValues = new HashMap<Integer, Double>();
    private HeroesPlayerDamage heroesPlayerListener = new HeroesPlayerDamage(plugin, this);
     private Properties prop = plugin.getConfigManager().getProperties();
 
     public HeroesDamage(Heroes plugin) {
         this.plugin = plugin;
     }
 
     /**
      * @return the mobHealthValues
      */
     public HashMap<Integer, Double> getMobHealthValues() {
         return mobHealthValues;
     }
 
     /**
      * Register the events for the damage system
      */
     public void registerEvents() {
         if(prop.damageSystem) {
             plugin.getServer().getPluginManager().registerEvent(Type.ENTITY_DAMAGE, heroesPlayerListener, Priority.Highest, plugin);
         }
     }
 
 }
