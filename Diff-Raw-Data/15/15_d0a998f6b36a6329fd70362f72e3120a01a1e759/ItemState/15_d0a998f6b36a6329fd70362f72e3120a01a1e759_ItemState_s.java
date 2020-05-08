 package ch.x42.terye.persistence;
 
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.bson.BSONObject;
 
 import com.mongodb.DBObject;
 
 public abstract class ItemState implements DBObject {
 
    public static enum ItemType {
         NODE, PROPERTY
     }
 
     private ItemType type;
     private String path;
     private String parent;
     private boolean isPartial;
 
     public ItemState(ItemType type) {
         this.type = type;
     }
 
     @Override
     public boolean containsField(String s) {
         boolean ret = false;
         if (s.equals("type") && type != null) {
             ret = true;
         } else if (s.equals("path") && path != null) {
             ret = true;
         } else if (s.equals("parent") && parent != null) {
             ret = true;
         }
         return ret;
     }
 
     @Override
     @Deprecated
     public boolean containsKey(String s) {
         return this.containsField(s);
     }
 
     @Override
     public Object get(String key) {
         Object value = null;
         if (key.equals("type")) {
            value = type;
         } else if (key.equals("path")) {
             value = path;
         } else if (key.equals("parent")) {
             value = parent;
         }
         return value;
     }
 
     @Override
     public Set<String> keySet() {
         Set<String> set = new HashSet<String>();
         set.add("type");
         set.add("path");
         set.add("parent");
         return set;
     }
 
     @Override
     public Object put(String key, Object v) {
         if (key.equals("type")) {
             type = ItemType.values()[(Integer) v];
         } else if (key.equals("path")) {
             path = (String) v;
         } else if (key.equals("parent")) {
             parent = (String) v;
         }
         return v;
     }
 
     @Override
     public void putAll(BSONObject o) {
         putAll(o.toMap());
     }
 
     @Override
     @SuppressWarnings({ "unchecked", "rawtypes" })
     public void putAll(Map m) {
         Set<String> keySet = m.keySet();
         for (String key : keySet) {
             put(key, m.get(key));
         }
     }
 
     @Override
     public Object removeField(String key) {
         Object ret = null;
         if (key.equals("type")) {
             ret = type;
             type = null;
         } else if (key.equals("path")) {
             ret = path;
             path = null;
         } else if (key.equals("parent")) {
             ret = parent;
             parent = null;
         }
         return ret;
     }
 
     @Override
     @SuppressWarnings("rawtypes")
     public Map toMap() {
         Map<String, Object> ret = new HashMap<String, Object>();
         ret.put("type", type);
         ret.put("path", path);
         ret.put("parent", parent);
         return ret;
     }
 
     @Override
     public boolean isPartialObject() {
         return isPartial;
     }
 
     @Override
     public void markAsPartialObject() {
         isPartial = true;
     }
 
     public String getPath() {
         return path;
     }
 
     public String getParent() {
         return parent;
     }
 
 }
