 package kauhsa.sokoban.level.yaml;
 
 import java.util.Map;
 
 /**
  * Helper class for SnakeYAML library, which will use this to load YAML data to
  * memory.
  */
class YAMLLevelData {
     private Map<String, String> metadata;
     private String world;
 
     public String getMetadata(String key) {
         return metadata.get(key);
     }
 
     public String getWorld() {
         return world;
     }
 
     public void setMetadata(Map<String, String> metadata) {
         this.metadata = metadata;
     }
 
     public void setWorld(String world) {
         this.world = world;
     }
 }
