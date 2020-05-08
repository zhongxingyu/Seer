 package lure.lang;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 /* TODO make this a java list via hella delegation. */
 public class List implements LureObject {
   private ArrayList<Object> list;
   private HashMap<String, Object> map;
 
   public List() {
     list = new ArrayList<Object>();
     map = new HashMap<String, Object>();
     map.put("add", new Function() {
       public Object call(Object arg) {
         list.add(arg);
         return arg;
       }
     });
     map.put("remove", new Function() {
       public Object call(Object arg) {
         return list.remove((int)((Integer)arg));
       }
     });
     map.put("get", new Function() {
       public Object call(Object arg) {
         return list.get((int)((Integer)arg));
       }
     });
     map.put("size", new Function() {
      public Object call(Object arg) {
         return list.size();
       }
     });
   }
 
   public String toString() {
     return list.toString();
   }
 
   public Object _fieldGet(Object key) {
     return map.get((String)key);
   }
 
   public Object _fieldSet(Object key, Object val) {
     map.put((String)key, val);
     return val;
   }
 
   public ArrayList getArrayList() {
     return list;
   }
 }
