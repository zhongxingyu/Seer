 package de.newsarea.homecockpit.connector.facade.registration.eventhandler;
 
import de.newsarea.homecockpit.connector.Connector;
 import de.newsarea.homecockpit.connector.event.ConnectorEventHandlerListener;
 import de.newsarea.homecockpit.connector.facade.eventhandler.AbstractEventHandler;
 import de.newsarea.homecockpit.connector.facade.eventhandler.ConnectorEventHandler;
 import de.newsarea.homecockpit.connector.facade.eventhandler.InboundEventHandler;
 
 import java.util.Map;
 
 public class TestEventHandler extends AbstractEventHandler implements InboundEventHandler, ConnectorEventHandler {
 
    public TestEventHandler(Connector connector, Map<String, String> parameters) {
         super(connector, parameters);
     }
 
     @Override
     public void addConnectorEventHandlerListener(ConnectorEventHandlerListener<Object> connectorEventHandlerListener) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void handleInboundEvent(Object value) {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void queueLastEvent() {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
 }
