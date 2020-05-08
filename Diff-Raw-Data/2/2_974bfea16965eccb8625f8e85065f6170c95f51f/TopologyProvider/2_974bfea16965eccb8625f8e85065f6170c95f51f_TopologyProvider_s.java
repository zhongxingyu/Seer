 package cmf.bus.pubsub.transport;
 
 import java.util.Collection;
 import java.util.LinkedList;
 
 import cmf.bus.core.DeliveryOutcome;
 import cmf.bus.core.event.IEventHandler;
 import cmf.bus.pubsub.Envelope;
 import cmf.bus.pubsub.Registration;
 import cmf.bus.pubsub.event.EventBus;
 
 public class TopologyProvider {
 
     public class TopologyUpdateResponseHandler implements IEventHandler<TopologyUpdateResponse> {
 
         @Override
         public DeliveryOutcome receive(TopologyUpdateResponse topologyUpdateResponse) {
             setTopologyRegistry(topologyUpdateResponse.getTopologyRegistry());
 
             return DeliveryOutcome.Acknowledge;
         }
 
     }
 
     private String profile;
 
     private TopologyRegistry topologyRegistry;
 
     public String getProfile() {
         return profile;
     }
 
     public Collection<Route> getReceiveRouteCollection(Registration registration) {
         Collection<Route> receiveRouteCollection = null;
         try {
             receiveRouteCollection = topologyRegistry.getReceiveRouteCollection(registration.getTopic());
         } catch (Exception e) {
             receiveRouteCollection = new LinkedList<Route>();
         }
 
         return receiveRouteCollection;
     }
 
     public Collection<Route> getSendRouteCollection(Envelope envelope) {
         Collection<Route> sendRouteCollection = null;
         try {
            sendRouteCollection = topologyRegistry.getReceiveRouteCollection(envelope.getTopic());
         } catch (Exception e) {
             sendRouteCollection = new LinkedList<Route>();
         }
 
         return sendRouteCollection;
     }
 
     public void setEventBus(EventBus eventBus) {
         eventBus.register(new TopologyUpdateResponseHandler(), TopologyUpdateResponse.class);
     }
 
     public void setProfile(String profile) {
         this.profile = profile;
     }
 
     public void setTopologyRegistry(TopologyRegistry topologyRegistry) {
         this.topologyRegistry = topologyRegistry;
     }
 
 }
