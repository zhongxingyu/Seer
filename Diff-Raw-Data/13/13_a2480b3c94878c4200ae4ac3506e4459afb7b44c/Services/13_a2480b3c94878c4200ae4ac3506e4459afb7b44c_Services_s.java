 package org.gethydrated.hydra.core.service;
 
 import org.gethydrated.hydra.actors.Actor;
 import org.gethydrated.hydra.actors.ActorFactory;
 import org.gethydrated.hydra.actors.ActorRef;
 import org.gethydrated.hydra.api.configuration.Configuration;
 import org.gethydrated.hydra.core.messages.StartService;
 import org.gethydrated.hydra.core.service.locator.ServiceLocator;
 import org.gethydrated.hydra.core.service.locator.SystemServiceLocator;
 import org.gethydrated.hydra.core.util.IDGenerator;
 import org.slf4j.Logger;
 
 import java.io.IOException;
 
 /**
  * Manages running services.
  * 
  * @author Christian Kulpa
  * @since 0.1.0
  * 
  */
 public class Services extends Actor {
     /**
      * Logger.
      */
     private final Logger logger = getLogger(Services.class);
 
     /**
      * Service locator.
      */
     private final ServiceLocator sl;
 
     private final IDGenerator idGen;
 
     /**
      * Configuration.
      */
     private final Configuration cfg;
     
     /**
      * Constructor.
      * 
      * @param cfg
      *            Configuration.
      */
     public Services(final Configuration cfg) {
         sl = new SystemServiceLocator(cfg);
         idGen = new IDGenerator();
         this.cfg = cfg;
     }
 
     @Override
     public void onReceive(Object message) throws Exception {
         if(message instanceof StartService) {
             startService(((StartService) message).getServiceName());
         }
     }
 
     private void startService(String serviceName) {
         try {
             Long id = idGen.getId();
             final ServiceInfo si = sl.locate(serviceName);
             if(si != null) {
                 ActorRef service = getContext().spawnActor(new ActorFactory() {
                     @Override
                     public Actor create() throws Exception {
                         return new ServiceImpl(si, cfg);
                     }
                 }, id.toString());
                 getSender().tell(new SIDImpl(service), getSelf());
             }
        } catch (IOException e) {
            e.printStackTrace();
             getSender().tell(e, getSelf());
         }
     }
 
     @Override
     public void onStart() {
         logger.info("Starting service managing.");
     }
 }
