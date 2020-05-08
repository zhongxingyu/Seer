 package net.krinsoft.orbsuite.listeners;
 
import net.krinsoft.orbsuite.OrbCore;
import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
 
 /**
  * @author krinsdeath
  */
 public class OrbListener implements Listener {
    private OrbCore plugin;

    public OrbListener(OrbCore instance) {
        plugin = instance;
    }

    @EventHandler
    void playerDeath(PlayerDeathEvent event) {
    }

    @EventHandler
    void playerKill(EntityDeathEvent event) {
    }
 
 }
