 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package be.mira.adastra3.server.controller;
 
 import be.mira.adastra3.server.Service;
 import be.mira.adastra3.server.exceptions.DeviceException;
 import be.mira.adastra3.server.exceptions.NetworkException;
 import be.mira.adastra3.server.exceptions.RepositoryException;
 import be.mira.adastra3.server.exceptions.ServiceRunException;
 import be.mira.adastra3.server.exceptions.ServiceSetupException;
 import be.mira.adastra3.server.network.INetworkListener;
 import be.mira.adastra3.server.network.Network;
 import be.mira.adastra3.server.network.entities.Entity;
 import be.mira.adastra3.server.network.entities.Kiosk;
 import be.mira.adastra3.server.repository.IRepositoryListener;
 import be.mira.adastra3.server.repository.Repository;
 import be.mira.adastra3.server.repository.configurations.Configuration;
 import be.mira.adastra3.server.repository.configurations.KioskConfiguration;
 import be.mira.adastra3.server.repository.media.Media;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author tim
  */
 public class Controller extends Service implements INetworkListener, IRepositoryListener {
     //
     // Data members
     //
     
     private Network mNetwork;
     private Repository mRepository;
     
     
     //
     // Construction and destruction
     //
     
     public Controller() throws ServiceSetupException {
         mNetwork = Network.getInstance();
         mRepository = Repository.getInstance();
         
         mNetwork.addListener(this);
         mRepository.addListener(this);
     }
     
     
     //
     // Service interface
     //
 
     @Override
     public final void run() throws ServiceRunException {
     }
 
     @Override
     public final void stop() throws ServiceRunException {
         mNetwork.removeListener(this);
         mRepository.removeListener(this);
     }
     
     
     //
     // Repository listener interface
     //
     
     // TODO: the doRepository as well as doMedia(Update|Added) functions are
    //       quite similar, it would be nice to deduplicate this code
 
     @Override
     public final void doRepositoryWarning(final String iMessage) {
         getLogger().warn("Repository warning: " + iMessage);
     }
 
     @Override
     public final void doRepositoryError(final String iMessage, final RepositoryException iException) {
         getLogger().error("Repository error: " + iMessage, iException);
     }
     
     @Override
     public final void doConfigurationAdded(final Configuration iConfiguration) {
         getLogger().info("Configuration added: " + iConfiguration.getId());
         
         // Check if there is a valid target device, and if so push the configuration
         Entity tDevice = null;
         try {
             tDevice = Network.getInstance().getDevice(iConfiguration.getTarget());
             if (tDevice != null) {
                 tDevice.setConfiguration(iConfiguration);
             } else {
                 getLogger().warn("Configuration " + iConfiguration.getId() + " does not target a valid device");
             }
         } catch (DeviceException tException) {
             getLogger().error("Could not push configuration " + iConfiguration.getId() + " to target device '" + tDevice.getUuid() + "'", tException);
         }
     }
 
     @Override
     public final void doConfigurationUpdated(final Configuration iOldConfiguration, final Configuration iConfiguration) {
         getLogger().info("Configuration updated: " + iConfiguration.getId());
         
         // Check if there is a valid target device, and if so update the configuration
         Entity tDevice = null;
         try {
             tDevice = Network.getInstance().getDevice(iConfiguration.getTarget());
             if (tDevice != null) {
                 tDevice.setConfiguration(iConfiguration);
             } else {
                 getLogger().warn("Configuration " + iConfiguration.getId() + " does not target a valid device");
             }
         } catch (DeviceException tException) {
             getLogger().error("Could not update configuration " + iConfiguration.getId() + " on target device '" + tDevice.getUuid() + "'", tException);
         }
     }
     
     @Override
     public final void doConfigurationRemoved(final Configuration iConfiguration) {
         getLogger().info("Configuration removed: " + iConfiguration.getId());
         
         // TODO: Erase the configuration on the device
     }
     
     @Override
     public final void doMediaAdded(final Media iMedia) {
         getLogger().info("Media added: " + iMedia.getId());
         
         List<KioskConfiguration> tKioskConfigurations = findKioskConfigurationsByMedia(iMedia);
         if (tKioskConfigurations.size() == 0) {
             getLogger().warn("Media " + iMedia.getId() + " is not referred to by any configuration");
         }
         for (KioskConfiguration tKioskConfiguration: tKioskConfigurations) {
             Entity tEntity = Network.getInstance().getDevice(tKioskConfiguration.getTarget());
             if (tEntity != null) {
                 try {
                     getLogger().debug("Media "
                             + iMedia.getId()
                             + " is in use by network entity "
                             + tEntity.getName() +
                             ", resubmitting");
                     if (tEntity instanceof Kiosk) {
                         ((Kiosk)tEntity).setMedia(iMedia);
                     } else {
                         getLogger().error("Cannot update media, unknown network entity");
                     }
                 } catch (DeviceException tException) {
                     getLogger().error("Could not set media "
                             + iMedia.getId()
                             + " on device "
                             + tEntity.getName());
                 }
             }
         }
     }
 
     @Override
     public final void doMediaUpdated(final Media iOldMedia, final Media iMedia) {
         getLogger().info("Media updated: " + iMedia.getId());
         
         List<KioskConfiguration> tKioskConfigurations = findKioskConfigurationsByMedia(iMedia);
         if (tKioskConfigurations.size() == 0) {
             getLogger().warn("Media " + iMedia.getId() + " is not referred to by any configuration");
         }
         for (KioskConfiguration tKioskConfiguration: tKioskConfigurations) {
             Entity tEntity = Network.getInstance().getDevice(tKioskConfiguration.getTarget());
             if (tEntity != null) {
                 try {
                     getLogger().debug("Media "
                             + iMedia.getId()
                             + " is in use by network entity "
                             + tEntity.getName() +
                             ", resubmitting");
                     if (tEntity instanceof Kiosk) {
                         ((Kiosk)tEntity).setMedia(iMedia);
                     } else {
                         getLogger().error("Cannot update media, unknown network entity");
                     }
                 } catch (DeviceException tException) {
                     getLogger().error("Could not set media "
                             + iMedia.getId()
                             + " on device "
                             + tEntity.getName());
                 }
             }
         }
     }
     
     @Override
     public final void doMediaRemoved(final Media iMedia) {
         getLogger().info("Media removed: " + iMedia.getId());
         
         // TODO
     }
     
     private List<KioskConfiguration> findKioskConfigurationsByMedia(final Media iMedia) {
         List<KioskConfiguration> tKioskConfigurations = new ArrayList<KioskConfiguration>();
         
         for (Configuration tConfiguration: Repository.getInstance().getAllConfigurations()) {
             if (tConfiguration instanceof KioskConfiguration) {
                 KioskConfiguration tKioskConfiguration = (KioskConfiguration) tConfiguration;
                 if (tKioskConfiguration.getMediaConfiguration().getName().equals(iMedia.getId())) {
                     tKioskConfigurations.add(tKioskConfiguration);
                 }
             }
         }
         
         return tKioskConfigurations;
     }
     
     
     //
     // Network listener interface
     //
 
     @Override
     public final void doNetworkWarning(final String iMessage) {
         getLogger().warn("Network warning: " + iMessage);
     }
 
     @Override
     public final void doNetworkError(final String iMessage, final NetworkException iException) {
         getLogger().error("Network error: " + iMessage, iException);
     }
 
     @Override
     public final void doEntityAdded(final Entity iEntity) {
         getLogger().info("MIRA network entity added: " + iEntity.getUuid());
         Repository tRepository = Repository.getInstance();
             
         // Check if there is a configuration for this entity
         try {
             Configuration tConfiguration = null;
             for (Configuration tAvailableConfiguration: tRepository.getAllConfigurations()) {
                 if (tAvailableConfiguration.getTarget() == iEntity.getUuid()) {
                     tConfiguration = tAvailableConfiguration;
                     break;
                 }
             }
             if (tConfiguration == null) {
                 getLogger().warn("Couldn't find any configuration for device " + iEntity.getUuid() + ", it'll remain unconfigured");
             } else {
                 getLogger().debug("Loading configuration " + tConfiguration.getId() + " onto device " + iEntity.getUuid());
                 iEntity.setConfiguration(tConfiguration);
             }
         } catch (DeviceException tException) {
             getLogger().error("could not configure device", tException);
         }
     }
 
     @Override
     public final void doEntityRemoved(final Entity iEntity) {
         getLogger().info("MIRA network entity removed: " + iEntity.getUuid());
     }
 }
