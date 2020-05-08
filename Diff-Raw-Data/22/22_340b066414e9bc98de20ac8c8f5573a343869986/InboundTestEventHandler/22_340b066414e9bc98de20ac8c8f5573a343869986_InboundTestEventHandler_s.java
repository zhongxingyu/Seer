 package de.newsarea.homecockpit.connector.facade.registration.eventhandler;
 
 import de.newsarea.homecockpit.connector.facade.eventhandler.AbstractEventHandler;
 import de.newsarea.homecockpit.connector.facade.eventhandler.InboundEventHandler;
 
 import java.util.Map;
 
 public class InboundTestEventHandler extends AbstractEventHandler implements InboundEventHandler {
 
    public InboundTestEventHandler(Object connector, Map<String, String> parameters) {
         super(connector, parameters);
     }
 
     @Override
     public void handleInboundEvent(Object value) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 }
