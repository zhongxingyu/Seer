 package me.limebyte.battlenight.api.battle;
 
 import java.util.ArrayList;
 import java.util.HashMap;
import java.util.List;
 import java.util.Map;
 import java.util.Random;
 
 import org.bukkit.configuration.serialization.ConfigurationSerializable;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 @SerializableAs("Arena")
 public class Arena implements ConfigurationSerializable {
 
     private String name;
     private String displayName;
    private List<Waypoint> spawnPoints = new ArrayList<Waypoint>();
     private boolean enabled = true;
     private final Random RANDOM = new Random();
 
     public Arena(String name) {
         this.name = name.toLowerCase();
         this.displayName = name;
     }
 
     public String getName() {
         return name;
     }
 
     public String getDisplayName() {
         return displayName;
     }
 
     public void setDisplayName(String displayName) {
         this.displayName = displayName;
     }
 
    public List<Waypoint> getSpawnPoints() {
         return spawnPoints;
     }
 
     public Waypoint getRandomSpawnPoint() {
         return getSpawnPoints().get(RANDOM.nextInt(getSpawnPoints().size()));
     }
 
     public void addSpawnPoint(Waypoint waypoint) {
         spawnPoints.add(waypoint);
     }
 
     public void removeSpawnPoint(Waypoint waypoint) {
         spawnPoints.remove(waypoint);
     }
 
     public void enable() {
         setEnabled(true);
     }
 
     public void disable() {
         setEnabled(false);
     }
 
     public void setEnabled(boolean enabled) {
         this.enabled = enabled;
     }
 
     public boolean isEnabled() {
         return enabled;
     }
 
     public boolean isSetup(int minSpawnPoints) {
         return spawnPoints.size() >= minSpawnPoints;
     }
 
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> map = new HashMap<String, Object>();
         map.put("Name", name);
         map.put("DisplayName", displayName);
         map.put("SpawnPoints", spawnPoints);
         map.put("Enabled", enabled);
         return map;
     }
 
     @SuppressWarnings("unchecked")
     public static Arena deserialize(Map<String, Object> map) {
         Arena arena = new Arena((String) map.get("Name"));
         arena.displayName = (String) map.get("DisplayName");
        arena.spawnPoints = (List<Waypoint>) map.get("SpawnPoints");
         arena.enabled = (Boolean) map.get("Enabled");
 
         return arena;
     }
 
 }
