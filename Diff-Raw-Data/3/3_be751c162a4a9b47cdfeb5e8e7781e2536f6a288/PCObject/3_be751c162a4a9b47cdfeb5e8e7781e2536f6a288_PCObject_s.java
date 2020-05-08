 import java.io.Serializable;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 public class PCObject implements Serializable 
 {
     private final HashMap<String, Object> _props = new HashMap<String, Object>();
     private final static String _base = "050FCAC0-610C-4CF6-9CC2-5EA5A40C3155";
 
     public PCObject() {
         set(_base, this);
     }
     public PCObject(double num) {
         set(_base, num);
     }
     public PCObject(boolean b) {
         set(_base, b);
     }
 
     public PCObject(char c) {
         set(_base, c);
     }
     public boolean contains(String key) {
         return _props.containsKey(key);
     }
 
     public PCObject set(String key, Object value) {
         _props.put(key, value);
         return this;
     }
 
     private Object getObj(String key) {
         return _props.containsKey(key) ? _props.get(key) : null;
     }
 
     @SuppressWarnings("unchecked")
     public <T> T get(String key) {
         // this is an unchecked cast and that's ok, it might fail at run time
         return (T) getObj(key);
     }
 
     public <T> T getBase() {
         return get(_base);
     }
 
     //we know from type inference these must be of the same type
     //so we only ever have to check one
     public boolean equals(PCObject other) {
         //first let's check if these are primitives
         if(_props.containsKey(_base)){
             if(_props.get(_base) instanceof Double){
                 return this.<Double>getBase() == other.<Double>getBase();
             }
             else if (_props.get(_base) instanceof Boolean){
                 return this.<Boolean>getBase() == other.<Boolean>getBase();
             }
             else if (_props.get(_base) instanceof Character){
                 return this.<Character>getBase() == other.<Character>getBase();
             }
             else return false; //hopefully never see this guy
         }
         else{
             //else let's check each guy
             Iterator it = _props.entrySet().iterator(); 
             while (it.hasNext())
             {
                 Map.Entry entry = (Map.Entry) it.next();
                 String key = (String)entry.getKey(); 
                 PCObject val = (PCObject)entry.getValue(); 
                 if(!other.contains(key)){
                     return false;
                 }
                 else{
                     PCObject mine =val;
                     PCObject his = other.<PCObject>get(key);
                     if(!mine.equals(his)){
                         return false;
                     }
                 }
             }
             return true;
         }
 
 
     }
 
 }
