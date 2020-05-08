 package edgruberman.bukkit.obituaries;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.Configuration;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.event.player.PlayerJoinEvent;
 
 import edgruberman.bukkit.obituaries.commands.Reload;
 import edgruberman.bukkit.obituaries.damages.BlockExplosion;
 import edgruberman.bukkit.obituaries.damages.Fall;
 import edgruberman.bukkit.obituaries.damages.FireTick;
 import edgruberman.bukkit.obituaries.damages.Magic;
 import edgruberman.bukkit.obituaries.damages.Suffocation;
 import edgruberman.bukkit.obituaries.damages.Thorns;
 import edgruberman.bukkit.obituaries.messaging.ConfigurationCourier;
 import edgruberman.bukkit.obituaries.util.CustomPlugin;
 
 public final class Main extends CustomPlugin implements Listener {
 
     public static ConfigurationCourier courier;
 
     @Override
     public void onLoad() {
         this.putConfigMinimum("3.0.0b0");
        this.putConfigMinimum("en_US.yml", "3.0.0b0");
 
         Damage.register(DamageCause.BLOCK_EXPLOSION, BlockExplosion.class);
         Damage.register(DamageCause.CONTACT, Accident.class);
         Damage.register(DamageCause.ENTITY_ATTACK, Attack.class);
         Damage.register(DamageCause.ENTITY_EXPLOSION, Attack.class);
         Damage.register(DamageCause.FALL, Fall.class);
         Damage.register(DamageCause.VOID, Fall.class);
         Damage.register(DamageCause.FIRE_TICK, FireTick.class);
         Damage.register(DamageCause.MAGIC, Magic.class);
         Damage.register(DamageCause.PROJECTILE, Attack.class);
         Damage.register(DamageCause.SUFFOCATION, Suffocation.class);
         Damage.register(DamageCause.FALLING_BLOCK, Attack.class);
         Damage.register(DamageCause.THORNS, Thorns.class);
     }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         final Configuration language = this.loadConfig(this.getConfig().getString("language") + ".yml");
         Main.courier = ConfigurationCourier.Factory.create(this).setBase(language).setFormatCode("format-code").build();
 
         this.getServer().getPluginManager().registerEvents(this, this);
 
         this.getCommand("obituaries:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         Main.courier = null;
     }
 
     @EventHandler(priority = EventPriority.MONITOR)
     public void onPlayerJoin(final PlayerJoinEvent join) {
         Bukkit.getPluginManager().registerEvents(new Coroner(join.getPlayer()), this);
     }
 
 }
