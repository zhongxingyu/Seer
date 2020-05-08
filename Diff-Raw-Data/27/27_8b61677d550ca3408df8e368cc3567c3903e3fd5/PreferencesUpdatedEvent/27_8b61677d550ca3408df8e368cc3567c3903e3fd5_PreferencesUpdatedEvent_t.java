 /**
  * 
  */
 package org.iplantc.de.client.events;
 
import org.iplantc.de.client.events.PreferencesUpdatedEvent.PreferencesUpdatedEventHandler;

 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 
 /**
  * @author sriram
  * 
  */
 public class PreferencesUpdatedEvent extends GwtEvent<PreferencesUpdatedEventHandler> {
 
     public interface PreferencesUpdatedEventHandler extends EventHandler {
 
         void onUpdate(PreferencesUpdatedEvent event);
 
     }
 
     public static final GwtEvent.Type<PreferencesUpdatedEventHandler> TYPE = new GwtEvent.Type<PreferencesUpdatedEventHandler>();
 
     @Override
     public com.google.gwt.event.shared.GwtEvent.Type<PreferencesUpdatedEventHandler> getAssociatedType() {
         return TYPE;
     }
 
     @Override
     protected void dispatch(PreferencesUpdatedEventHandler handler) {
         handler.onUpdate(this);
 
     }
 
 }
