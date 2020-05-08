 /**
  * 
  */
 package org.iplantc.de.client.events;
 
 import com.google.gwt.event.shared.EventHandler;
 import com.google.gwt.event.shared.GwtEvent;
 
import org.iplantc.de.client.events.WindowCloseRequestEvent.WindowCloseRequestEventHandler;

 /**
  * @author sriram
  * 
  */
 public class WindowCloseRequestEvent extends GwtEvent<WindowCloseRequestEventHandler> {
 
     public interface WindowCloseRequestEventHandler extends EventHandler {
 
         void onWindowCloseRequest(WindowCloseRequestEvent event);
 
     }
 
     public static final GwtEvent.Type<WindowCloseRequestEventHandler> TYPE = new GwtEvent.Type<WindowCloseRequestEventHandler>();
 
     public WindowCloseRequestEvent() {
     }
 
     @Override
     public GwtEvent.Type<WindowCloseRequestEventHandler> getAssociatedType() {
         return TYPE;
     }
 
     @Override
     protected void dispatch(WindowCloseRequestEventHandler handler) {
         handler.onWindowCloseRequest(this);
     }
 
 }
