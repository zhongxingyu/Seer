 /**
  * Copyright (C) 2011-2012 Tim Besard <tim.besard@gmail.com>
  *
  * All rights reserved.
  */
 package be.mira.codri.server.bo;
 
 import be.mira.codri.server.events.NetworkKioskEvent;
 import be.mira.codri.server.events.RepositoryPresentationEvent;
 import be.mira.codri.server.events.RepositoryConnectionEvent;
 import be.mira.codri.server.events.RepositoryEvent;
 import be.mira.codri.server.events.NetworkEvent;
 import be.mira.codri.server.events.RepositoryConfigurationEvent;
 import be.mira.codri.server.exceptions.DeviceException;
 import be.mira.codri.server.bo.network.entities.Kiosk;
 import be.mira.codri.server.bo.repository.entities.Configuration;
 import be.mira.codri.server.bo.repository.entities.Connection;
 import be.mira.codri.server.bo.repository.entities.Presentation;
 import be.mira.codri.server.spring.Slf4jLogger;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import org.slf4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationEvent;
 import org.springframework.context.ApplicationListener;
 
 /**
 *
 * @author tim
 */
 public class Coordinator implements ApplicationListener<ApplicationEvent> {
     //
     // Data members
     //
     
     @Slf4jLogger
     private Logger mLogger;
     
     private final Network mNetwork;
     private final Repository mRepository;
     
     
     //
     // Construction and destruction
     //
     
     @Autowired
     public Coordinator(final Network iNetwork, final Repository iRepository) {
         mNetwork = iNetwork;
         mRepository = iRepository;
     }
     
     
     //
     // ApplicationEvent handler
     //
     
     @Override
     public final void onApplicationEvent(final ApplicationEvent iEvent) {
         // TODO: this is ugly
         if (iEvent instanceof NetworkEvent) {
             if (iEvent instanceof NetworkKioskEvent) {
                 onNetworkKioskEvent((NetworkKioskEvent) iEvent);
             }
         } else if (iEvent instanceof RepositoryEvent) {
             if (iEvent instanceof RepositoryPresentationEvent) {
                 onRepositoryPresentationEvent((RepositoryPresentationEvent) iEvent);
             } else if (iEvent instanceof RepositoryConfigurationEvent) {
                 onRepositoryConfigurationEvent((RepositoryConfigurationEvent) iEvent);
             } else if (iEvent instanceof RepositoryConnectionEvent) {
                 onRepositoryConnectionEvent((RepositoryConnectionEvent) iEvent);
             }
         }
     }
     
     
     //
     // NetworkEvent handler
     //
     
     public final void onNetworkKioskEvent(final NetworkKioskEvent iEvent) {   
         mLogger.trace("Received network kiosk event");
         
         switch (iEvent.getType()) {
             case ADDED:
                 mLogger.info("Kiosk {} has been added", iEvent.getId());
 
                 // Find the connections this device is a part of
                 Map<String, Connection> tRelevantConnections = new HashMap<String, Connection>();
                 for (Map.Entry<String, Connection> tEntry : mRepository.getConnections().entrySet()) {
                     if (tEntry.getValue().getKiosk().equals(iEvent.getId())) {
                         tRelevantConnections.put(tEntry.getKey(), tEntry.getValue());
                     }
                 }
 
                 // Check the connections
                 if (tRelevantConnections.isEmpty()) {
                     mLogger.warn("Could not find any connection for kiosk {}", iEvent.getId());
                 } else if (tRelevantConnections.size() > 1) {
                     mLogger.warn("Ambiguous connections found for kiosk {}", iEvent.getId());
                 } else {
                     Map.Entry<String, Connection> tEntry = tRelevantConnections.entrySet().iterator().next();
                     pushConnection(tEntry.getKey(), tEntry.getValue());
                 }
                 
                 break;
                 
             case REFRESHED:
                 mLogger.debug("Received heartbeat from kiosk {}", iEvent.getId());
                 
                 break;
                 
             case EXPIRED:
                 mLogger.warn("Kiosk {} has been expired", iEvent.getId());
                 
                 break;
                 
             case REMOVED:
                 mLogger.info("Kiosk {} has been removed", iEvent.getId());
                 
                 break;
         }
     }
     
     
     //
     // RepositoryPresentationEvent handler
     //
     
     // TODO: the doRepository as well as doPresentation(Update|Added) functions are
     // quite similar, it would be nice to deduplicate this code&
     
     public final void onRepositoryPresentationEvent(final RepositoryPresentationEvent iEvent) {
         mLogger.trace("Received repository presentation event");
         
         Presentation tPresentation = (Presentation) iEvent.getEntity();        
         switch (iEvent.getType()) {
             case ADDED:
                 mLogger.info("Presentation {} has been added", iEvent.getId());
                 pushPresentation(iEvent.getId(), tPresentation);
                 
                 break; 
                 
             case UPDATED:
                 // TODO: do something with old one
                 
                 mLogger.info("Presentation {} has been updated", iEvent.getId());
                 pushPresentation(iEvent.getId(), tPresentation);
                 
                 break;
                 
             case REMOVED:
                 mLogger.info("Presentation {} has been removed", iEvent.getId());
 
                 // TODO
                 
                 break;
         }
     }
         
     private void pushPresentation(final String iId, final Presentation iPresentation) {
         mLogger.debug("Pushing presentation {}", iId);
         
         // Find the connections this presentation is a part of
         List<Map.Entry<String, Connection>> tRelevantConnections = new ArrayList<Map.Entry<String, Connection>>();
         for (Map.Entry<String, Connection> tConnection : mRepository.getConnections().entrySet()) {
             if (tConnection.getValue().getPresentation().equals(iId)) {
                 tRelevantConnections.add(tConnection);
             }
         }
         
         // Push to the devices
         for (Map.Entry<String, Connection> tEntry : tRelevantConnections) {
             Kiosk tKiosk = mNetwork.getKiosk(tEntry.getValue().getKiosk());
             // No need to display too many errors here, this should already have
             // have happened when the connection was initially added to the
             // repository
             if (tKiosk != null) {
                 mLogger.debug("Pushing presentation {} to kiosk {} (part of connection {})", new Object[] {iId, tEntry.getValue().getKiosk(), tEntry});
                 try {
                    tKiosk.setPresentation(iId, iPresentation);
                 } catch (DeviceException tException) {
                     mLogger.error("Could not push presentation {} to kiosk {}", new Object[] {iId, tEntry.getValue().getKiosk()}, tException);
                 }
             }
         }
     }
     
     
     //
     // RepositoryConfigurationEvent handler
     //
     
     public final void onRepositoryConfigurationEvent(final RepositoryConfigurationEvent iEvent) {
         mLogger.trace("Received repository configuration event");
         
         Configuration tConfiguration = (Configuration) iEvent.getEntity();        
         switch (iEvent.getType()) {
             case ADDED:
                 mLogger.info("Configuration {} has been added", iEvent.getId());
                 pushConfiguration(iEvent.getId(), tConfiguration);
                 
                 break;
                 
             case UPDATED:
                 // TODO: do something with old one
                 
                 mLogger.info("Configuration {} has been updated", iEvent.getId());
                 pushConfiguration(iEvent.getId(), tConfiguration);
                 
                 break;
                 
             case REMOVED:
                 mLogger.info("Configuration {} has been removed", iEvent.getId());
 
                 // TODO: Erase the configuration on the device
                 
                 break;
         }
     }
     
     private void pushConfiguration(final String iId, final Configuration iConfiguration) {
         mLogger.debug("Pushing configuration {}", iId);
         
         // Find the connections this configuration is a part of
         List<Map.Entry<String, Connection>> tRelevantConnections = new ArrayList<Map.Entry<String, Connection>>();
         for (Map.Entry<String, Connection> tConnection : mRepository.getConnections().entrySet()) {
             if (tConnection.getValue().getConfiguration().equals(iId)) {
                 tRelevantConnections.add(tConnection);
             }
         }
         
         // Push to the devices
         for (Map.Entry<String, Connection> tEntry : tRelevantConnections) {
             Kiosk tKiosk = mNetwork.getKiosk(tEntry.getValue().getKiosk());
             // No need to display too many errors here, this should already have
             // have happened when the connection was initially added to the
             // repository
             if (tKiosk != null) {
                 mLogger.debug("Pushing configuration {} to kiosk {} (part of connection {})", new Object[] {iId, tEntry.getValue().getKiosk(), tEntry});
                 try {
                    tKiosk.setConfiguration(iId, iConfiguration);
                 } catch (DeviceException tException) {
                     mLogger.error("Could not push configuration {} to kiosk {}", new Object[] {iId, tEntry.getValue().getKiosk()}, tException);
                 }
             }
         }
     }
     
     //
     // RepositoryConnectionEvent handler
     //
     
     public final void onRepositoryConnectionEvent(final RepositoryConnectionEvent iEvent) {
         mLogger.trace("Received repository connection event");
         
         Connection tConnection = (Connection) iEvent.getEntity();        
         switch (iEvent.getType()) {
             case ADDED:
                 mLogger.info("Connection {} has been added", iEvent.getId());
                 pushConnection(iEvent.getId(), tConnection);
                 
                 break;
                 
             case UPDATED:
                 // TODO: do something with old one
                 
                 mLogger.info("Connection {} has been updated", iEvent.getId());
                 pushConnection(iEvent.getId(), tConnection);
                 
                 break;
                 
             case REMOVED:
                 mLogger.info("Connection {} has been removed", iEvent.getId());
 
                 // TODO: Erase the connection on the device
                 
                 break;
         }
     }
     
     private void pushConnection(final String iId, final Connection iConnection) {
         mLogger.debug("Pushing connection {}", iId);
         
         // Check if there is a valid target device
         Kiosk tKiosk = mNetwork.getKiosk(iConnection.getKiosk());
         if (tKiosk == null) {
             mLogger.warn("Connection {} does not point to a valid kiosk", iId);
             return;
         }
         
         // Push the configuration
         Configuration tConfiguration = mRepository.getConfiguration(iConnection.getConfiguration());
         if (tConfiguration != null) {
             try {
                tKiosk.setConfiguration(iId, tConfiguration);
             } catch (DeviceException tException) {
                 mLogger.error("Could not upload configuration {} ", iId, tException);
             }
         } else {
             mLogger.warn("Connection {} does not point to a valid configuration", iId);
         }
 
         // Push the presentation
         Presentation tPresentation = mRepository.getPresentation(iConnection.getPresentation());
         if (tPresentation != null) {
             try {
                tKiosk.setPresentation(iId, tPresentation);
             } catch (DeviceException tException) {
                 mLogger.error("Could not upload presentation {}", iId, tException);
             }
         } else {
             mLogger.warn("Connection {} does not point to a valid presentation", iId);
         }
     }
 }
