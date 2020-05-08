 package net.robinjam.bukkit.eternalwolf;
 
 import java.util.ArrayList;
 import java.util.List;
 import net.robinjam.bukkit.eternalwolf.commands.CallWolves;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.entity.Wolf;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 /**
  *
  * @author robinjam
  */
 public class EternalWolf extends JavaPlugin {
 
     private EntityListener entityListener = new EntityListener();
     private PlayerListener playerListener = new PlayerListener(this);
 
     @Override
     public void onEnable() {
         // Load config.yml
         getConfig().options().copyDefaults(true);
         saveConfig();
 
         // Register events
         PluginManager pm = getServer().getPluginManager();
         pm.registerEvents(entityListener, this);
         pm.registerEvents(playerListener, this);
 
         // Register commands
        this.getCommand("call-wolves").setExecutor(new CallWolves(this));
 
         System.out.println(this + " is enabled!");
     }
 
     @Override
     public void onDisable() {
         // Do nothing
     }
     
     public static List<Wolf> getWolves(Player player) {
         List<Wolf> wolves = new ArrayList<Wolf>();
         
         for (LivingEntity entity : player.getWorld().getLivingEntities()) {
             if (entity instanceof Wolf) {
                 Wolf wolf = (Wolf) entity;
 
                 if (wolf.isTamed() && player.equals(wolf.getOwner()))
                     wolves.add(wolf);
             }
         }
 
         return wolves;
     }
     
 }
