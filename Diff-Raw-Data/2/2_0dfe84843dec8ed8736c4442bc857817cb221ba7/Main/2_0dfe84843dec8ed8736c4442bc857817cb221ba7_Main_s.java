 package edgruberman.bukkit.simpledeathnotices;
 
 import org.bukkit.Material;
 import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
 import org.bukkit.material.MaterialData;
 import org.bukkit.util.config.Configuration;
 
 import edgruberman.bukkit.messagemanager.MessageLevel;
 import edgruberman.bukkit.messagemanager.MessageManager;
 
 public final class Main extends org.bukkit.plugin.java.JavaPlugin {
     
     static ConfigurationFile configurationFile;
     static MessageManager messageManager;
     
     public void onLoad() {
         Main.messageManager = new MessageManager(this);
         Main.messageManager.log("Version " + this.getDescription().getVersion());
         
         Main.configurationFile = new ConfigurationFile(this);
         this.loadConfiguration();
     }
     
     public void onEnable() {
         new DeathMonitor(this);
         
         Main.messageManager.log("Plugin Enabled");
     }
     
     public void onDisable() {
         DamageReport.last.clear();
         
         Main.messageManager.log("Plugin Disabled");
     }
     
     private void loadConfiguration() {
         Configuration cfg = Main.configurationFile.getConfiguration();
         
         // Load default format.
         DeathMonitor.causeFormats.clear();
         DeathMonitor.causeFormats.put(null, cfg.getString("default", DeathMonitor.DEFAULT_FORMAT));
         
         // Load damage cause specific formats.
         for (String name: cfg.getKeys("DamageCause")) {
             DamageCause cause = DamageCause.valueOf(name);
             if (cause == null) continue;
             
             DeathMonitor.causeFormats.put(cause, cfg.getString("DamageCause." + cause.name(), DeathMonitor.DEFAULT_FORMAT));
         }
         Main.messageManager.log(DeathMonitor.causeFormats.size() + " cause formats loaded.", MessageLevel.CONFIG);
         
         // weapon
         DeathMonitor.weaponFormat = cfg.getString("weapon", DeathMonitor.DEFAULT_WEAPON_FORMAT);
         Main.messageManager.log("Weapon Format: " + DeathMonitor.weaponFormat, MessageLevel.CONFIG);
         
         // hand
         DeathMonitor.weaponFormat = cfg.getString("hand", DeathMonitor.DEFAULT_HAND);
         Main.messageManager.log("Hand: " + DeathMonitor.hand, MessageLevel.CONFIG);
         
         // owners
         DeathMonitor.ownerFormats.clear();
         for (String name: cfg.getKeys("owners")) {
             DeathMonitor.ownerFormats.put(name, cfg.getString("owners." + name));
            Main.messageManager.log("Owner Format for" + name + ": " + DeathMonitor.ownerFormats.get(name), MessageLevel.CONFIG);
         }
 
         // Entity
         DeathMonitor.entityNames.clear();
         for (String name: cfg.getKeys("Entity"))
             DeathMonitor.entityNames.put(name, cfg.getString("Entity." + name, name.toLowerCase()));
         
         Main.messageManager.log(DeathMonitor.entityNames.size() + " entity names loaded.", MessageLevel.CONFIG);
         
         // Material
         DeathMonitor.materialNames.clear();
         for (String name: cfg.getKeys("Material")) {
             Material material = Material.valueOf(name);
             if (material == null) continue;
             
             DeathMonitor.materialNames.put(material, cfg.getString("Material." + material.name(), material.name().toLowerCase()));
         }
         Main.messageManager.log(DeathMonitor.materialNames.size() + " material names loaded.", MessageLevel.CONFIG);
         
         // MaterialData
         DeathMonitor.materialDataNames.clear();
         for (String entry: cfg.getKeys("MaterialData")) {
             Material material = Material.valueOf(entry.split(":")[0]);
             Byte data = Byte.parseByte(entry.split(":")[1]);
             DeathMonitor.materialDataNames.put(new MaterialData(material, data), cfg.getString("MaterialData." + entry));
         }
         Main.messageManager.log(DeathMonitor.materialDataNames.size() + " material data names loaded.", MessageLevel.CONFIG);
     }
 }
