 package jchazelcast;
 
import java.util.ArrayList;

 
 public class Event<K,V>  extends JCConnection.Response {
     String type;
     String name;
     String structure;
     boolean includeValue;
     K key;
     V newvalue;
     V oldvalue;
 
     public Event(String type,String name,String structure, boolean iV,  K key) {
        super("EVENT",new ArrayList());
         this.type = type;
         this.includeValue = iV;
         this.name = name;
         this.structure=structure;
         this.key = key;
     }
 
     public Event(String type, String name,String structure,boolean iV,  K key, V value) {
        super("EVENT",new ArrayList());
         this.type = type;
         this.includeValue = iV;
         this.name = name;
         this.key = key;
         this.newvalue= value;
         this.structure=structure;
     }
 
     public Event(String type,String name,String structure, boolean iV,  K key, V newvalue, V oldvalue) {
        super("EVENT",new ArrayList());
         this.type = type;
         this.includeValue = iV;
         this.name = name;
         this.key = key;
         this.newvalue = newvalue;
         this.oldvalue = oldvalue;
         this.structure=structure;
     }
 }
