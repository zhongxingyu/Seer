 package org.iplantc.core.uicommons.client.info;
 
 import org.iplantc.core.uicommons.client.info.AnnouncementRemovedEvent.Handler;
 
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 
 /**
  * This event takes place when an announcement has been removed from the announcer.
  */
 public final class AnnouncementRemovedEvent extends GwtEvent<Handler> {
 
     /**
      * Objects that wish to listen for this event need to implement this interface.
      */
     public interface Handler extends EventHandler {
         /**
          * this method is called when the event occurs.
          * 
          * @param event the event
          */
         void onRemove(AnnouncementRemovedEvent event);
     }
 
     /**
      * The single type object associated with this event
      */
     public static final Type<Handler> TYPE = new Type<Handler>();
 
     private final AnnouncementId announcement;
 
     /**
      * the constructor
      * 
      * @param announcement the id of the announcement that was removed
      */
    AnnouncementRemovedEvent(final AnnouncementId announcement) {
         this.announcement = announcement;
     }
 
     /**
      * @see GwtEvent#getAssociatedType()
      */
     @Override
     public Type<Handler> getAssociatedType() {
         return TYPE;
     }
 
     /**
      * the id of the announcement that was removed
      */
     public AnnouncementId getAnnouncement() {
         return announcement;
     }
 
     /**
      * @see GwtEvent<T>#dispatch(T)
      */
     @Override
     protected void dispatch(final Handler handler) {
         handler.onRemove(this);
     }
 
 }
