 package edgruberman.bukkit.simpledeathnotices;
 
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.event.entity.EntityDamageEvent;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.entity.EntityDeathEvent;
 import org.bukkit.event.entity.EntityEvent;
 
 import edgruberman.bukkit.simpledeathnotices.Communicator.MessageLevel;
 
 public class Main extends org.bukkit.plugin.java.JavaPlugin {
 
     public Communicator communicator = new Communicator(this);
     
     private static final String DEFAULT_MESSAGE_LEVEL = Integer.toString(MessageLevel.DEATH.level.intValue());
 	
     public void onEnable() {
         this.communicator.log("Version " + this.getDescription().getVersion());
         
         Configuration.load(this);
         this.communicator.setLogLevel(Level.parse(this.getConfiguration().getString("logLevel", "CONFIG")));
         this.communicator.setMessageLevel(Level.parse(this.getConfiguration().getString("messageLevel", DEFAULT_MESSAGE_LEVEL)));
         this.communicator.log(Level.CONFIG,
             "timestamp: " + this.getConfiguration().getString("timestamp")
             + "; format: " + this.getConfiguration().getString("format")
         );
         
         this.registerEvents();
         
         this.communicator.log("Plugin Enabled");
     }
     
     public void onDisable() {
         this.communicator.log("Plugin Disabled");
     }
     
     private void registerEvents() {
         EntityListener entityListener = new EntityListener(this);
         
         org.bukkit.plugin.PluginManager pluginManager = this.getServer().getPluginManager();
         pluginManager.registerEvent(Event.Type.ENTITY_DAMAGE, entityListener, Event.Priority.Monitor, this);
         pluginManager.registerEvent(Event.Type.ENTITY_DEATH, entityListener, Event.Priority.Monitor, this);
     }
     
     public void describeEvent(EntityEvent event) {
         Entity damager = null;
        String damagerName = "";
         
         if (event instanceof EntityDamageByEntityEvent){
             damager = ((EntityDamageByEntityEvent) event).getDamager();
             if (damager instanceof Player) {
                 damagerName = " " + ((Player) damager).getDisplayName();
             } else {
                 String[] damagerClass = damager.getClass().getName().split("\\.");
                 damagerName = " a " + damagerClass[damagerClass.length - 1].substring("Craft".length());
             }
         }
         
         String deathCause;
         if (event instanceof EntityDeathEvent) {
             deathCause = this.getCause(null);
         } else {
             deathCause = this.getCause(((EntityDamageEvent) event).getCause());
         }
         
         String deathNotice = this.getConfiguration().getString("format")
             .replace(
                   "%TIMESTAMP%"
                 , (new SimpleDateFormat(this.getConfiguration().getString("timestamp")).format(new Date()))
             )
             .replace(
                   "%VICTIM%"
                 , ((Player) event.getEntity()).getDisplayName()
             )
             .replace(
                   "%CAUSE%"
                 , deathCause
             )
             .replace(
                   "%KILLER%"
                 , damagerName
             )
         ;
         
         this.communicator.log(MessageLevel.DEATH.level, deathNotice);
         this.communicator.broadcastMessage(MessageLevel.DEATH, deathNotice);
     }
     
     public String getCause(DamageCause damageCause) {
         String deathCause;
         switch (damageCause) {
             case ENTITY_ATTACK:    deathCause = "being hit by";           break;
             case ENTITY_EXPLOSION: deathCause = "an explosion from";      break;
             case CONTACT:          deathCause = "contact";                break;
             case SUFFOCATION:      deathCause = "suffocation";            break;
             case FALL:             deathCause = "falling";                break;
             case FIRE:             deathCause = "fire";                   break;
             case FIRE_TICK:        deathCause = "burning";                break;
             case LAVA:             deathCause = "lava";                   break;
             case DROWNING:         deathCause = "drowning";               break;
             case BLOCK_EXPLOSION:  deathCause = "an explosion";           break;
             case VOID:             deathCause = "falling in to the void"; break;
             case CUSTOM:           deathCause = "something custom";       break;
             default:               deathCause = "something";              break;
         }
         return deathCause;
     }
 
 }
