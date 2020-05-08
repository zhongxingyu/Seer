 package me.limebyte.deathstrike;
 
 import java.util.logging.Logger;
 
 import me.limebyte.battlenight.api.BattleNightAPI;
 import me.limebyte.battlenight.api.BattleNightPlugin;
 import me.limebyte.battlenight.api.battle.Battle;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.PlayerDeathEvent;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class DeathStrike extends JavaPlugin implements Listener {
 
     private BattleNightAPI api;
 
     @Override
     public void onEnable() {
         Logger log = getLogger();
         PluginManager pm = getServer().getPluginManager();
 
         try {
             BattleNightPlugin plugin = (BattleNightPlugin) pm.getPlugin("BattleNight");
             api = plugin.getAPI();
        } catch (Exception e) {
             // Unsupported version
            e.printStackTrace();
             pm.disablePlugin(this);
             return;
         }
 
         pm.registerEvents(this, this);
         log.info("Version " + getDescription().getVersion() + " enabled successfully.");
     }
 
     @EventHandler
     public void onPlayerDeath(PlayerDeathEvent event) {
         Player player = event.getEntity();
         Battle battle = api.getBattleManager().getActiveBattle();
 
         if (battle.containsPlayer(player)) {
             player.getWorld().strikeLightningEffect(player.getLocation());
         }
     }
 
 }
