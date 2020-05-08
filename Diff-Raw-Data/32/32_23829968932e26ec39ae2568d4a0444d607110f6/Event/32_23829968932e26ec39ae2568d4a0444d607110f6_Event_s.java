 package fm.audiobox.core.observables;
 
 import java.util.EventObject;
 
 public class Event extends EventObject {
 
   
   private static final long serialVersionUID = 1L;
 
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
   
   
 }
