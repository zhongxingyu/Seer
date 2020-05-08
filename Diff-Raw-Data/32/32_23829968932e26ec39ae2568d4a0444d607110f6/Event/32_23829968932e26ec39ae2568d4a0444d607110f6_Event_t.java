 package fm.audiobox.core.observables;
 
 import java.util.EventObject;
import java.util.HashMap;
import java.util.Map;
 
 public class Event extends EventObject {
 
   
   private static final long serialVersionUID = 1L;
  
  private Map<String, Object> properties = new HashMap<String, Object>();
 
   public int detail = -1;
 
   public static enum States {
     ENTITY_ADDED,
     ENTITY_REMOVED,
     COLLECTION_CLEARED,
     ENTITY_REFRESHED
   };
   
   
   public States state;
   
   public Event(Object source) {
     super(source);
   }
 
   
   public Event(Object source, States state) {
     super(source);
     this.state = state;
   }
   
   
  public void addProperty(String key, Object value){
    this.properties.put( key, value );
  }
  
  public Object getProperty(String key){
    
    if ( this.properties.containsKey( key ) ){
      return this.properties.get( key );
    }
    return null;
    
  }
  
 }
