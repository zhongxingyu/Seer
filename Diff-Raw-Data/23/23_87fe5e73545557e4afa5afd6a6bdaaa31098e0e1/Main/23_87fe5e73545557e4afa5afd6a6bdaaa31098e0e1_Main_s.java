 package edgruberman.bukkit.borderpatrol;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 
 import org.bukkit.Material;
 import org.bukkit.World;
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.event.HandlerList;
 
 import edgruberman.bukkit.borderpatrol.commands.Reload;
 import edgruberman.bukkit.borderpatrol.messaging.ConfigurationCourier;
 import edgruberman.bukkit.borderpatrol.messaging.Courier;
 import edgruberman.bukkit.borderpatrol.util.CustomPlugin;
 
 public final class Main extends CustomPlugin {
 
     public static Courier courier;
 
     @Override
     public void onLoad() {
         this.putConfigMinimum("config.yml", "2.4.0");
        this.putConfigMinimum("safety.yml", "2.2.0");
     }
 
     @Override
     public void onEnable() {
         this.reloadConfig();
         Main.courier = new ConfigurationCourier(this);
 
         this.loadSafety(this.loadConfig("safety.yml"));
         final List<Border> borders = this.loadBorders(this.getConfig().getConfigurationSection("borders"));
         final CivilEngineer engineer = new CivilEngineer(borders);
         new BorderAgent(this, engineer);
         new ImmigrationInspector(this, engineer);
 
         this.getCommand("borderpatrol:reload").setExecutor(new Reload(this));
     }
 
     @Override
     public void onDisable() {
         HandlerList.unregisterAll(this);
     }
 
     private void loadSafety(final ConfigurationSection safety) {
         this.loadMaterials(safety, "safeContainers", SafetyOfficer.safeContainers);
         this.loadMaterials(safety, "safeMaterials", SafetyOfficer.safeMaterials);
         SafetyOfficer.safeContainers.addAll(SafetyOfficer.safeMaterials);
         this.loadMaterials(safety, "unsafeSupports", SafetyOfficer.unsafeSupports);
     }
 
     private void loadMaterials(final ConfigurationSection type, final String entry, final Set<Integer> materials) {
         if (!type.isSet(entry)) return;
 
         for (final String name : type.getStringList(entry)) {
             final Material material = Material.valueOf(name);
             if (material == null) {
                 this.getLogger().warning("Unable to determine material: " + name);
                 continue;
             }
 
             materials.add(material.getId());
         }
     }
 
     private List<Border> loadBorders(final ConfigurationSection sectionBorders) {
         if (sectionBorders == null) return Collections.emptyList();
 
         final List<Border> borders = new ArrayList<Border>();
 
         for (final String worldName : sectionBorders.getKeys(false)) {
             final World world = this.getServer().getWorld(worldName);
             if (world == null) {
                 this.getLogger().warning("Unable to define border; World not found: " +  worldName);
                 continue;
             }
 
             final ConfigurationSection worldBorder = sectionBorders.getConfigurationSection(worldName);
             final Border border = new Border(
                       world
                     , worldBorder.getInt("minX", 0)
                     , worldBorder.getInt("maxX", 0)
                     , worldBorder.getInt("minZ", 0)
                     , worldBorder.getInt("maxZ", 0)
                     , worldBorder.getInt("safe.x", 0)
                     , worldBorder.getInt("safe.y", 0)
                     , worldBorder.getInt("safe.z", 0)
             );
             borders.add(border);
             this.getLogger().config(border.description());
         }
         return borders;
     }
 
 }
