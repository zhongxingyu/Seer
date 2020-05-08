 package com.imjake9.simplejail.cells.data;
 
 import java.util.HashMap;
 import java.util.Map;
 import org.bukkit.configuration.serialization.SerializableAs;
 
 @SerializableAs("Jail")
 public class Jail extends JailCell {
     
     public Map<String, JailCell> cells;
     
     public Jail() {
         cells = new HashMap<String, JailCell>();
     }
     
     public static JailCell deserialize(Map<String, Object> data) {
        Jail jail = (Jail) JailCell.deserialize(data);
         jail.cells = (Map<String, JailCell>) data.get("cells");
         return jail;
     }
     
     @Override
     public Map<String, Object> serialize() {
         Map<String, Object> jail = super.serialize();
         jail.put("cells", cells);
         return jail;
     }
     
 }
