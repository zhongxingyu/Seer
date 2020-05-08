 package to.joe.strangeweapons.datastorage;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.logging.Level;
 import java.util.Set;
 
 import org.bukkit.configuration.ConfigurationSection;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.inventory.ItemStack;
 
 import to.joe.strangeweapons.Part;
 import to.joe.strangeweapons.StrangeWeapons;
 
 public class YamlDataStorage implements DataStorageInterface {
 
     private StrangeWeapons plugin;
     private File weaponConfigFile;
     private YamlConfiguration weaponConfig;
     private int lastWeaponId;
     private File dropsConfigFile;
     private YamlConfiguration dropsConfig;
 
     public YamlDataStorage(final StrangeWeapons plugin) throws IOException {
         this.plugin = plugin;
 
         this.weaponConfigFile = new File(plugin.getDataFolder(), "weapons.yml");
         if (!(weaponConfigFile.exists() && !weaponConfigFile.isDirectory())) {
             weaponConfigFile.createNewFile();
         }
         this.weaponConfig = YamlConfiguration.loadConfiguration(weaponConfigFile);
         if (!weaponConfig.isConfigurationSection("weapons")) {
             weaponConfig.createSection("weapons");
         }
 
         lastWeaponId = 0;
         Set<String> keys = this.weaponConfig.getConfigurationSection("weapons").getKeys(false);
         for (String key : keys) {
             try {
                 int value = Integer.valueOf(key);
                 if (value < 0) {
                     this.plugin.getLogger().warning("Picked up an invalid id at weapons." + key);
                 }
                 if (value > lastWeaponId) {
                     this.lastWeaponId = value;
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid id at weapons." + key);
             }
         }
 
         this.dropsConfigFile = new File(plugin.getDataFolder(), "drops.yml");
         if (!(dropsConfigFile.exists() && !dropsConfigFile.isDirectory())) {
             dropsConfigFile.createNewFile();
         }
         this.dropsConfig = YamlConfiguration.loadConfiguration(dropsConfigFile);
         if (!dropsConfig.isConfigurationSection("players")) {
             dropsConfig.createSection("players");
         }
         
         plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
             @Override
             public void run() {
                 try {
                     weaponConfig.save(weaponConfigFile);
                     dropsConfig.save(dropsConfigFile);
                 } catch (IOException e) {
                     plugin.getServer().getLogger().log(Level.SEVERE, "Error writing file!", e);
                 }
             }
         }, 1200, 1200);
     }
 
     @Override
     public WeaponData getWeaponData(int id) throws DataStorageException {
         if (this.weaponConfig.getConfigurationSection("weapons").getConfigurationSection(Integer.toString(id)) == null) {
             throw new DataStorageException("Tried to read a weapon whose id didn't exist!");
         }
         return WeaponData.fromConfigurationSection(id, this.weaponConfig.getConfigurationSection("weapons").getConfigurationSection(Integer.toString(id)));
     }
 
     @Override
     public WeaponData saveNewWeaponData(WeaponData data) throws DataStorageException {
         int newId = lastWeaponId + 1;
         this.weaponConfig.getConfigurationSection("weapons").createSection(Integer.toString(newId));
         ConfigurationSection section = this.weaponConfig.getConfigurationSection("weapons").getConfigurationSection(Integer.toString(newId));
         section.set("quality", data.getQuality().toString());
         if (data.getCustomName() != null) {
             section.set("customname", data.getCustomName());
         }
         if (data.getDescription() != null) {
             section.set("description", data.getDescription());
         }
         List<String> rawParts = new ArrayList<String>();
         for (Entry<Part, Integer> part : data.getParts().entrySet()) {
             rawParts.add(part.getKey() + "," + part.getValue());
         }
         section.set("parts", rawParts);
         data.setWeaponId(newId);
         lastWeaponId = newId;
         return data;
     }
 
     @Override
     public void updateWeaponData(WeaponData data) throws DataStorageException {
         if (this.weaponConfig.getConfigurationSection("weapons").getConfigurationSection(Integer.toString(data.getWeaponId())) == null) {
             throw new DataStorageException("Tried to update a weapon whose id didn't exist!");
         }
         ConfigurationSection section = this.weaponConfig.getConfigurationSection("weapons").getConfigurationSection(Integer.toString(data.getWeaponId()));
         section.set("quality", data.getQuality().toString());
         if (data.getCustomName() != null) {
             section.set("customname", data.getCustomName());
         }
         if (data.getDescription() != null) {
             section.set("description", data.getDescription());
         }
         List<String> rawParts = new ArrayList<String>();
         for (Entry<Part, Integer> part : data.getParts().entrySet()) {
             rawParts.add(part.getKey() + "," + part.getValue());
         }
         section.set("parts", rawParts);
     }
 
     @Override
     public PlayerDropData getPlayerDropData(String player) throws DataStorageException {
         Set<String> keys = this.dropsConfig.getConfigurationSection("players").getKeys(false);
         for (String key : keys) {
             if (key.equalsIgnoreCase(player)) {
                 player = key;
                 break;
             }
         }
         if (this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player) == null) {
             this.dropsConfig.getConfigurationSection("players").createSection(player);
             ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player);
             PlayerDropData data = new PlayerDropData(player, 0, 0, 0);
             section.set("playtime", data.getPlayTime());
             section.set("nextitemdrop", data.getNextCrateDrop());
             section.set("nextcratedrop", data.getNextCrateDrop());
             return data;
         } else {
             ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player);
             return new PlayerDropData(player, section.getInt("playtime"), section.getInt("nextitemdrop"), section.getInt("nextcratedrop"));
         }
     }
 
     @Override
     public boolean playerDropDataExists(String player) throws DataStorageException {
         Set<String> keys = this.dropsConfig.getConfigurationSection("players").getKeys(false);
         for (String key : keys) {
             if (key.equalsIgnoreCase(player)) {
                 player = key;
                 break;
             }
         }
         return this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player) != null;
     }
 
     @Override
     public void updatePlayerDropData(PlayerDropData data) throws DataStorageException {
         if (this.dropsConfig.getConfigurationSection("players").getConfigurationSection(data.getPlayer()) == null) {
             throw new DataStorageException("Tried to update a player who didn't exist!");
         }
         ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(data.getPlayer());
         section.set("playtime", data.getPlayTime());
         section.set("nextitemdrop", data.getNextItemDrop());
         section.set("nextcratedrop", data.getNextCrateDrop());
     }
 
     @Override
     public boolean itemCanDrop(PlayerDropData data) throws DataStorageException {
         ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(data.getPlayer()).getConfigurationSection("drops");
         if (section == null) {
             return true;
         } else {
             int numDrops = 0;
             Set<String> keys = section.getKeys(false);
             for (String key : keys) {
                 if (!section.getConfigurationSection(key).getBoolean("iscrate") && section.getConfigurationSection(key).getLong("time") > (new Date().getTime() / 1000) - (plugin.itemDropReset * 120)) {
                     numDrops++;
                 }
             }
             if (numDrops < plugin.itemDropLimit) {
                 return true;
             }
             return false;
         }
     }
 
     @Override
     public boolean crateCanDrop(PlayerDropData data) throws DataStorageException {
         ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(data.getPlayer()).getConfigurationSection("drops");
         if (section == null) {
             return true;
         } else {
             int numDrops = 0;
             Set<String> keys = section.getKeys(false);
             for (String key : keys) {
                 if (section.getConfigurationSection(key).getBoolean("iscrate") && section.getConfigurationSection(key).getLong("time") > (new Date().getTime() / 1000) - (plugin.crateDropReset * 120)) {
                     numDrops++;
                 }
             }
             if (numDrops < plugin.crateDropLimit) {
                 return true;
             }
             return false;
         }
     }
 
     @Override
     public void recordDrop(String player, ItemStack item, boolean isCrate) throws DataStorageException {
         if (this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player) == null) {
             throw new DataStorageException("Tried to record a drop for a player who didn't exist!");
         }
         ConfigurationSection section = this.dropsConfig.getConfigurationSection("players").getConfigurationSection(player);
         if (section.getConfigurationSection("drops") == null) {
             section.createSection("drops");
         }
         section = section.getConfigurationSection("drops");
         int lastDropId = 0;
         Set<String> keys = section.getKeys(false);
         for (String key : keys) {
             try {
                 int value = Integer.valueOf(key);
                 if (value < 0) {
                     this.plugin.getLogger().warning("Picked up an invalid id at players." + player + ".drops." + key);
                 }
                 if (value > lastDropId) {
                     lastDropId = value;
                 }
             } catch (NumberFormatException e) {
                 this.plugin.getLogger().warning("Picked up an invalid id at players." + player + ".drops." + key);
             }
         }
         lastDropId++;
         section = section.createSection(Integer.toString(lastDropId));
         section.set("iscrate", isCrate);
         section.set("item", item);
         section.set("time", new Date().getTime() / 1000);
     }
 }
