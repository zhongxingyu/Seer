 package de.flower.common.ui.ajax.updatebehavior.events;
 
import java.io.Serializable;

 /**
  * @author oblume
  */
public abstract class Event implements Serializable {
 
     private Class<?> clazz;
 
     protected Event(Class<?> clazz) {
         this.clazz = clazz;
     }
 
     public static Event EntityUpdated(Class<?> clazz) {
         return new EntityUpdatedEvent(clazz);
     }
 
     public static Event EntityCreated(Class<?> clazz) {
         return new EntityCreatedEvent(clazz);
     }
 
     public static Event EntityDeleted(Class<?> clazz) {
         return new EntityDeletedEvent(clazz);
     }
 
     public static Event EntityAll(Class<?> clazz) {
         return new EntityAllEvent(clazz);
     }
 
     /**
      * Check if this event matches the triggeredEvent.
      * <p/>
      * First compare clazz field. If not equal no match.
      * If same clazz then check type of Event (All, Created, Changed, Deleted).
      * ALL matches Created, Changed, Deleted.
      * Created matches Created, ...
      *
      * @param triggeredEvent
      * @return
      */
     public boolean matches(Event triggeredEvent) {
         if (!this.clazz.equals(triggeredEvent.getClazz())) {
             return false;
         }
         if (this.getClass().isInstance(triggeredEvent)) {
             return true;
         }
         return false;
     }
 
     public Class<?> getClazz() {
         return clazz;
     }
 }
