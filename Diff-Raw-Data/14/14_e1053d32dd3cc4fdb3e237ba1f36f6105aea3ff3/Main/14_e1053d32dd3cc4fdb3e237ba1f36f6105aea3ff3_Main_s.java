 package edgruberman.bukkit.doorman;
 
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.TimeUnit;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.doorman.commands.Change;
 import edgruberman.bukkit.doorman.commands.History;
 import edgruberman.bukkit.doorman.commands.Reload;
 import edgruberman.bukkit.doorman.commands.Show;
 import edgruberman.bukkit.doorman.messaging.ConfigurationCourier;
 import edgruberman.bukkit.doorman.util.CustomPlugin;
 
 public final class Main extends CustomPlugin {
 
     public static ConfigurationCourier courier;
 
     @Override
     public void onLoad() {
        this.putConfigMinimum(CustomPlugin.CONFIGURATION_FILE, "1.2.1");
         this.setPathSeparator('|');
     }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = ConfigurationCourier.Factory.create(this).setPath("language").setColorCode("+color-code").build();
 
         Long grace = this.getConfig().getLong("declaration-grace", -1);
         if (grace != -1) grace = TimeUnit.SECONDS.toMillis(grace);
 
         final Map<String, Object> switches = new HashMap<String, Object>();
         final ConfigurationSection section = this.getConfig().getConfigurationSection("switches");
         if (section != null) switches.putAll(section.getValues(false));
 
         final RecordKeeper records = new RecordKeeper(this);
         final Doorman doorman = new Doorman(this, records, grace, switches
                 , this.parseGreetingSwitches(switches.keySet(), "greeting|headers"), this.parseGreetingSwitches(switches.keySet(), "greeting|arguments"));
 
         this.getCommand("doorman:history").setExecutor(new History(records));
         this.getCommand("doorman:show").setExecutor(new Show(doorman, records));
         this.getCommand("doorman:change").setExecutor(new Change(doorman, records));
         this.getCommand("doorman:reload").setExecutor(new Reload(this));
     }
 
     private List<String> parseGreetingSwitches(final Collection<String> recognized, final String path) {
         final List<String> values = this.getConfig().getStringList(path);
         if (values == null) return Collections.emptyList();
 
         final Iterator<String> it = values.iterator();
         while (it.hasNext()) {
             final String name = it.next();
             if (recognized.contains(name)) continue;
             this.getLogger().warning("Unrecognized switch specified in " + path + ": " + name);
             it.remove();
         }
         return values;
     }
 
     @Override
     public void onDisable() {
         Main.courier = null;
         HandlerList.unregisterAll(this);
     }
 
 }
