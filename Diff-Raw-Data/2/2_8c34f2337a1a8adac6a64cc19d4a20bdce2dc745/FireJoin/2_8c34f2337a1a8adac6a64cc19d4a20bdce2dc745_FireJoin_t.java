 package at.junction.firejoin;
 
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.Color;
 import org.bukkit.FireworkEffect;
 import org.bukkit.FireworkEffect.Type;
 import org.bukkit.entity.EntityType;
 import org.bukkit.entity.Firework;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.player.PlayerJoinEvent;
 import org.bukkit.inventory.meta.FireworkMeta;
 import org.bukkit.plugin.java.JavaPlugin;
 
 public class FireJoin extends JavaPlugin implements Listener {
     public void onEnable(){
         getServer().getPluginManager().registerEvents(this, this);
     }
     
     public void onDisable(){
     }
     
     @EventHandler
     public void onPlayerJoin(PlayerJoinEvent event) {               
         Player p = event.getPlayer();
         Firework fw = (Firework) p.getWorld().spawnEntity(p.getLocation(), EntityType.FIREWORK);
         FireworkMeta fwm = fw.getFireworkMeta();
        FireworkEffect effect = FireworkEffect.builder().with(Type.BALL_LARGE).withFlicker().withTrail().withColor(Color.ORANGE).withFade(Color.WHITE).build();
         fwm.addEffect(effect);
         fwm.setPower(2);
         fw.setFireworkMeta(fwm);           
     }
 }
