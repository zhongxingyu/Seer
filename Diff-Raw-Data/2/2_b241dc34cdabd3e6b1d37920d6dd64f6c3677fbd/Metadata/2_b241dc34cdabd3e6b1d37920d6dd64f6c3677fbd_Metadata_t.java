 package me.limebyte.battlenight.core.util;
 
 import java.util.List;
 
 import me.limebyte.battlenight.api.battle.PlayerClass;
 import me.limebyte.battlenight.core.BattleNight;
 import me.limebyte.battlenight.core.managers.ClassManager;
 
 import org.bukkit.entity.Player;
 import org.bukkit.metadata.FixedMetadataValue;
 import org.bukkit.metadata.MetadataValue;
 
 public class Metadata {
 
     public static void set(Player player, String key, Object value) {
         player.setMetadata(key, new FixedMetadataValue(BattleNight.instance, value));
     }
 
     public static void remove(Player player, String key) {
         player.removeMetadata(key, BattleNight.instance);
     }
 
     public static boolean getBoolean(Player player, String key) {
         MetadataValue value = getValue(player, key);
         if (value != null) return value.asBoolean();
         return false;
     }
 
     public static int getInt(Player player, String key) {
         MetadataValue value = getValue(player, key);
         if (value != null) return value.asInt();
         return 0;
     }
 
     public static String getString(Player player, String key) {
         MetadataValue value = getValue(player, key);
         if (value != null) return value.asString();
         return null;
     }
 
     public static PlayerClass getBattleClass(Player player) {
         String result = getString(player, "class");
        if (result == null) return null;
         return ClassManager.getClassNames().get(result);
     }
 
     private static MetadataValue getValue(Player player, String key) {
         List<MetadataValue> values = player.getMetadata(key);
         String bnName = BattleNight.instance.getDescription().getName();
 
         for (MetadataValue value : values) {
             String owner = value.getOwningPlugin().getDescription().getName();
             if (owner == bnName) return value;
         }
 
         return null;
     }
 
 }
