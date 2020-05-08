 package com.herocraftonline.dev.heroes.skill.skills;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.LivingEntity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event.Type;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityListener;
 import org.bukkit.util.config.ConfigurationNode;
 
 import com.herocraftonline.dev.heroes.Heroes;
 import com.herocraftonline.dev.heroes.persistence.Hero;
 import com.herocraftonline.dev.heroes.skill.TargettedSkill;
 import com.herocraftonline.dev.heroes.util.Messaging;
 
 public class SkillRoot extends TargettedSkill {
 
     private Map<String, Integer> teleporterTasks = new HashMap<String, Integer>();
 
     private String expireText;
 
     public SkillRoot(Heroes plugin) {
         super(plugin);
         setName("Root");
         setDescription("Roots your target in place");
         setUsage("/skill root <target>");
         setMinArgs(0);
         setMaxArgs(1);
         getIdentifiers().add("skill root");
 
         registerEvent(Type.ENTITY_DAMAGE, new SkillEntityListener(), Priority.Normal);
     }
 
     @Override
     public ConfigurationNode getDefaultConfig() {
         ConfigurationNode node = super.getDefaultConfig();
         node.setProperty("duration", 5000);
         node.setProperty("expire-text", "%skill% faded from %hero%!");
         return node;
     }
 
     @Override
     public void init() {
         super.init();
         expireText = getSetting(null, "expire-text", "%hero% lost %skill%!");
         expireText = expireText.replace("%hero%", "$1").replace("%skill%", "$2");
     }
 
     @Override
     public boolean use(Hero hero, LivingEntity target, String[] args) {
         Player player = hero.getPlayer();
         if (!(target instanceof Player)) {
             Messaging.send(player, "You need a target!");
         }
 
         Player targetPlayer = (Player) target;
         Hero targetHero = plugin.getHeroManager().getHero(targetPlayer);
         if (targetHero.equals(hero)) {
             Messaging.send(player, "You need a target!");
             return false;
         }
 
         long duration = getSetting(hero.getHeroClass(), "duration", 5000);
         TimedTeleporter task = new TimedTeleporter(targetPlayer, duration);
         task.setTaskID(plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, task, 0, 5));
         targetHero.applyEffect(getName(), duration);
         notifyNearbyPlayers(player.getLocation(), getUseText(), player.getName(), getName(), getEntityName(target));
         return true;
     }
 
     public class SkillEntityListener extends EntityListener {
 
         @Override
         public void onEntityDamage(EntityDamageEvent event) {
             if (event.isCancelled() || event.getCause() != DamageCause.FALL) {
                 return;
             }
 
             Entity defender = event.getEntity();
             if (defender instanceof Player) {
                 Player player = (Player) defender;
                 Hero hero = plugin.getHeroManager().getHero(player);
                 if (hero.hasEffect(getName())) {
                     hero.expireEffect(getName());
                     Integer taskID = teleporterTasks.get(player.getName());
                     if (taskID != null) {
                         teleporterTasks.remove(taskID);
                         plugin.getServer().getScheduler().cancelTask(taskID);
                     }
                 }
             }
         }
     }
 
     public class TimedTeleporter implements Runnable {
 
         private final String target;
         private final long endTime;
         private final double x, y, z;
         private int taskID = 0;
 
         public TimedTeleporter(Player target, long duration) {
             this.target = target.getName();
             Location location = target.getLocation();
             x = location.getX();
             y = location.getY();
             z = location.getZ();
             this.endTime = System.currentTimeMillis() + duration;
         }
 
         @Override
         public void run() {
             Player targetPlayer = Bukkit.getServer().getPlayer(target);
             if (targetPlayer != null) {
                 Location location = targetPlayer.getLocation();
                 if (location.getX() != x || location.getY() != y || location.getZ() != z) {
                     location.setX(x);
                     location.setY(y);
                     location.setZ(z);
                     targetPlayer.teleport(location);
                 }
             }
 
             if (System.currentTimeMillis() >= endTime) {
                 teleporterTasks.remove(taskID);
                 Bukkit.getServer().getScheduler().cancelTask(taskID);
             }
         }
 
         public void setTaskID(int taskID) {
             this.taskID = taskID;
         }
 
     }
 }
