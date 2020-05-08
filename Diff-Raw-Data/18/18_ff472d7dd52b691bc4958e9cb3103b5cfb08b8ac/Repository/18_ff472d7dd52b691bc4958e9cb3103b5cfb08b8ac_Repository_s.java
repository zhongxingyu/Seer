 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package be.mira.adastra3.server.business;
 
 import be.mira.adastra3.server.exceptions.RepositoryException;
 import be.mira.adastra3.server.repository.configuration.Configuration;
 import be.mira.adastra3.server.repository.connection.Connection;
 import be.mira.adastra3.server.repository.presentation.Presentation;
 import be.mira.adastra3.spring.Logger;
 import be.mira.adastra3.server.events.RepositoryConfigurationEvent;
 import be.mira.adastra3.server.events.RepositoryConnectionEvent;
 import be.mira.adastra3.server.events.RepositoryEvent;
 import be.mira.adastra3.server.events.RepositoryEvent.RepositoryEventType;
 import be.mira.adastra3.server.events.RepositoryPresentationEvent;
 import java.util.HashMap;
 import java.util.Map;
 import javax.annotation.PostConstruct;
 import javax.annotation.PreDestroy;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElementWrapper;
 import javax.xml.bind.annotation.XmlRootElement;
 import org.apache.commons.logging.Log;
 import org.springframework.context.ApplicationEventPublisher;
 import org.springframework.context.ApplicationEventPublisherAware;
 
 /**
  *
  * @author tim
  */
 @XmlRootElement(name="repository")
 public final class Repository implements ApplicationEventPublisherAware {
     //
     // Member data
     //
     
     @Logger
     private Log mLogger;
     
     private ApplicationEventPublisher mPublisher;
     
     private final Map<String, Configuration> mConfigurations;
     private final Map<String, Presentation> mPresentations;
     private final Map<String, Connection> mConnections;
     private String mServer;
 
 
     //
     // Construction and destruction
     //
 
     public Repository() {
         mConfigurations = new HashMap<String, Configuration>();
         mPresentations = new HashMap<String, Presentation>();
         mConnections = new HashMap<String, Connection>();
     }
     
     @Override
     public void setApplicationEventPublisher(final ApplicationEventPublisher iPublisher) {
         mPublisher = iPublisher;
     }
     
     @PostConstruct
     public void init() {
         
     }
     
     @PreDestroy
     public void destroy() {
         mConfigurations.clear();
         mPresentations.clear();
         mConnections.clear();
     }
 
 
     //
     // Getters and setters
     //
     
     // TODO: Remove the quite identical Connection/Configuration/Presentation setters
     //       somehow make it using the RepositoryEntity interface
     
     @XmlElement(nillable=true)
     public String getServer() {
         synchronized (mServer) {
             return mServer;
         }
     }
     
    public synchronized void setServer(final String iServer) {
         synchronized (mServer) {
             mServer = iServer;
         }
     }
      
     @XmlElementWrapper(name="presentations")
     @XmlElement(name="presentation")
     public Map<String, Presentation> getPresentations() {
         synchronized (mPresentations) {
             return mPresentations;
         }
     }
 
     public Presentation getPresentation(final String iId) {
         synchronized (mPresentations) {
             return mPresentations.get(iId);
         }
     }
 
     public void addPresentation(final String iId, final Presentation iPresentation) throws RepositoryException {
         synchronized (mPresentations) {
             if (mPresentations.containsKey(iId)) {
                 throw new RepositoryException("presentation " + iId + " already present in repository");
             }
             mPresentations.put(iId, iPresentation);
         }
         
         RepositoryEvent tEvent = new RepositoryPresentationEvent(this, RepositoryEventType.ADDED, iId, iPresentation);
         mPublisher.publishEvent(tEvent);
     }
     
     public void updatePresentation(final String iId, final Presentation iPresentation) throws RepositoryException {
         Presentation tOldPresentation;
         synchronized (mPresentations) {
             if (! mPresentations.containsKey(iId)) {
                 throw new RepositoryException("presentation " + iId + " not present in repository");
             }
             tOldPresentation = mPresentations.put(iId, iPresentation);
         }
         
         RepositoryEvent tEvent = new RepositoryPresentationEvent(this, RepositoryEventType.UPDATED, iId, iPresentation, tOldPresentation);
         mPublisher.publishEvent(tEvent);
     }
 
     public void removePresentation(final String iId, final Presentation iPresentation) throws RepositoryException {
         synchronized (mPresentations) {
             if (! mPresentations.containsKey(iId)) {
                 throw new RepositoryException("presentation " + iId + " not present in repository");
             }
             mPresentations.remove(iId);
         }
         
         RepositoryEvent tEvent = new RepositoryPresentationEvent(this, RepositoryEventType.REMOVED, iId, iPresentation);
         mPublisher.publishEvent(tEvent);
     }
     
     @XmlElementWrapper(name="configurations")
     @XmlElement(name="configuration")
     public Map<String, Configuration> getConfigurations() {
         synchronized (mConfigurations) {
             return mConfigurations;
         }
     }
 
     public Configuration getConfiguration(final String iId) {
         synchronized (mConfigurations) {
             return mConfigurations.get(iId);
         }
     }
 
     public void addConfiguration(final String iId, final Configuration iConfiguration) throws RepositoryException {
         synchronized (mConfigurations) {
             if (mConfigurations.containsKey(iId)) {
                 throw new RepositoryException("configuration " + iId + " already present in repository");
             }
             mConfigurations.put(iId, iConfiguration);
         }
         
         RepositoryEvent tEvent = new RepositoryConfigurationEvent(this, RepositoryEventType.ADDED, iId, iConfiguration);
         mPublisher.publishEvent(tEvent);
     }
     
     public void updateConfiguration(final String iId, final Configuration iConfiguration) throws RepositoryException {
         Configuration tOldConfiguration;
         synchronized (mConfigurations) {
             if (! mConfigurations.containsKey(iId)) {
                 throw new RepositoryException("configuration " + iId + " not present in repository");
             }
             tOldConfiguration = mConfigurations.put(iId, iConfiguration);
         }
         
         RepositoryEvent tEvent = new RepositoryConfigurationEvent(this, RepositoryEventType.UPDATED, iId, iConfiguration, tOldConfiguration);
         mPublisher.publishEvent(tEvent);
     }
 
     public void removeConfiguration(final String iId, final Configuration iConfiguration) throws RepositoryException {
         synchronized (mConfigurations) {
             if (! mConfigurations.containsKey(iId)) {
                 throw new RepositoryException("configuration " + iId + " not present in repository");
             }
             mConfigurations.remove(iId);
         }
         
         RepositoryEvent tEvent = new RepositoryConfigurationEvent(this, RepositoryEventType.REMOVED, iId, iConfiguration);
         mPublisher.publishEvent(tEvent);
     }
     
     @XmlElementWrapper(name="connections")
     @XmlElement(name="connection")
     public Map<String, Connection> getConnections() {
         synchronized (mConnections) {
             return mConnections;
         }
     }
 
     public Connection getConnection(final String iId) {
         synchronized (mConnections) {
             return mConnections.get(iId);
         }
     }
 
     public void addConnection(final String iId, final Connection iConnection) throws RepositoryException {
         synchronized (mConnections) {
             if (mConnections.containsKey(iId)) {
                 throw new RepositoryException("connection " + iId + " already present in repository");
             }
             mConnections.put(iId, iConnection);
         }
         
         RepositoryEvent tEvent = new RepositoryConnectionEvent(this, RepositoryEventType.ADDED, iId, iConnection);
         mPublisher.publishEvent(tEvent);
     }
     
     public void updateConnection(final String iId, final Connection iConnection) throws RepositoryException {
         Connection tOldConnection;
         synchronized (mConnections) {
             if (! mConnections.containsKey(iId)) {
                 throw new RepositoryException("connection " + iId + " not present in repository");
             }
             tOldConnection = mConnections.put(iId, iConnection);
         }
         
         RepositoryEvent tEvent = new RepositoryConnectionEvent(this, RepositoryEventType.UPDATED, iId, iConnection, tOldConnection);
         mPublisher.publishEvent(tEvent);
     }
 
     public void removeConnection(final String iId, final Connection iConnection) throws RepositoryException {
         synchronized (mConnections) {
             if (! mConnections.containsKey(iId)) {
                 throw new RepositoryException("connection " + iId + " not present in repository");
             }
             mConnections.remove(iId);
         }
         
         RepositoryEvent tEvent = new RepositoryConnectionEvent(this, RepositoryEventType.REMOVED, iId, iConnection);
         mPublisher.publishEvent(tEvent);
     }
 }
