 // Copyright (C) 2009 Google Inc.
 //
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 // http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 
 package com.google.enterprise.connector.instantiator;
 
 import com.google.enterprise.connector.common.PropertiesUtils;
 import com.google.enterprise.connector.common.StringUtils;
 import com.google.enterprise.connector.manager.Context;
 import com.google.enterprise.connector.persist.ConnectorExistsException;
 import com.google.enterprise.connector.persist.ConnectorNotFoundException;
 import com.google.enterprise.connector.pusher.PusherFactory;
 import com.google.enterprise.connector.scheduler.LoadManager;
 import com.google.enterprise.connector.scheduler.LoadManagerFactory;
 import com.google.enterprise.connector.scheduler.Schedule;
 import com.google.enterprise.connector.scheduler.ScheduleTimeInterval;
 import com.google.enterprise.connector.spi.AuthenticationManager;
 import com.google.enterprise.connector.spi.AuthorizationManager;
 import com.google.enterprise.connector.spi.ConfigureResponse;
 import com.google.enterprise.connector.spi.Connector;
 import com.google.enterprise.connector.spi.ConnectorShutdownAware;
 import com.google.enterprise.connector.spi.ConnectorType;
 import com.google.enterprise.connector.spi.TraversalManager;
 import com.google.enterprise.connector.traversal.BatchResult;
 import com.google.enterprise.connector.traversal.BatchResultRecorder;
 import com.google.enterprise.connector.traversal.BatchSize;
 import com.google.enterprise.connector.traversal.QueryTraverser;
 import com.google.enterprise.connector.traversal.TraversalDelayPolicy;
 import com.google.enterprise.connector.traversal.Traverser;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Calendar;
 import java.util.HashMap;
 import java.util.Locale;
 import java.util.Map;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * ConnectorCoordinator that supports Spring based connector instantiation and
  * persistent storage of connector configuration, schedule and traversal state.
  */
 class ConnectorCoordinatorImpl implements
     ConnectorCoordinator, BatchResultRecorder {
 
   private static final Logger LOGGER =
       Logger.getLogger(ConnectorCoordinatorImpl.class.getName());
 
   /**
    * Invariant context.
    */
   private final String name;
   private final PusherFactory pusherFactory;
   private final ThreadPool threadPool;
 
   /**
    * Context set when an instance is created or configured and cleared when the
    * instance is removed. It is an invariant that either both of these are null
    * or neither is.
    */
   private TypeInfo typeInfo;
   private InstanceInfo instanceInfo;
 
   /**
    * Context that is filled in on first use. Requires instanceInfo.
    */
   private ConnectorInterfaces interfaces;
 
   /**
    * LoadManager controls throughput to avoid overtaxing the Repository
    * or the GSA.
    */
   private LoadManager loadManager;
 
   /**
    * The current traversal Schedule.
    */
   private Schedule traversalSchedule;
 
   /**
    * The finish time for delay of next traversal.  Used to postpone
    * starting another traversal for a short period of time, as dictated
    * by a {@link TraversalDelayPolicy}.
    */
   private long traversalDelayEnd;
 
   /**
    * Context set when a batch is run. This must be cleared and any
    * running batch must be canceled when interfaces is reset.
    */
   private TaskHandle taskHandle;
   Object currentBatchKey;
 
    /**
    * Constructs a ConnectorCoordinator for the named {@link Connector}.
    * The {@code Connector} may not yet have a concrete instance.
    *
    * @param name The name of the Connector.
    * @param pusherFactory creates instances of
    *        {@link com.google.enterprise.connector.pusher.Pusher Pusher}
    *        for pushing documents to the GSA.
    * @param loadManagerFactory the used to create instances of
    *        {@link LoadManager} for controlling feed rate.
    * @param threadPool the {@link ThreadPool} for running traversals.
    */
   ConnectorCoordinatorImpl(String name, PusherFactory pusherFactory,
         LoadManagerFactory loadManagerFactory, ThreadPool threadPool) {
     this.name = name;
     this.threadPool = threadPool;
     this.pusherFactory = pusherFactory;
     this.loadManager = loadManagerFactory.newLoadManager(name);
   }
 
   /**
    * Constructs a ConnectorCoordinator for the named {@link Connector}
    * that wraps an existing Connector instance.
    *
    * @param instanceInfo A Connector instance.
    * @param pusherFactory creates instances of
    *        {@link com.google.enterprise.connector.pusher.Pusher Pusher}
    *        for pushing documents to the GSA.
    * @param loadManagerFactory the used to create instances of
    *        {@link LoadManager} for controlling feed rate.
    * @param threadPool the {@link ThreadPool} for running traversals.
    */
   ConnectorCoordinatorImpl(InstanceInfo instanceInfo,
         PusherFactory pusherFactory, LoadManagerFactory loadManagerFactory,
         ThreadPool threadPool) {
     this(instanceInfo.getName(), pusherFactory, loadManagerFactory, threadPool);
     this.instanceInfo = instanceInfo;
     this.typeInfo = instanceInfo.getTypeInfo();
     this.loadManager.setLoad(getSchedule().getLoad());
   }
 
   /**
    * Returns the name of this {@link Connector}.
    *
    * @return The name of this Connector.
    */
   //@Override
   public String getConnectorName() {
     return name;
   }
 
   /**
    * Returns {@code true} if an instance of this {@link Connector} exists.
    */
   //@Override
   public synchronized boolean exists() {
     return (instanceInfo != null);
   }
 
   /**
    * Removes this {@link Connector} instance.  Halts traversals,
    * removes the Connector instance from the known connectors,
    * and removes the Connector's on-disk representation.
    */
   //@Override
   public synchronized void removeConnector() {
     LOGGER.info("Dropping connector: " + name);
     try {
       resetBatch();
       if (instanceInfo != null) {
         File connectorDir = instanceInfo.getConnectorDir();
         shutdownConnector(true);
         instanceInfo.removeConnector();
         removeConnectorDirectory(name, connectorDir, typeInfo);
       }
     } finally {
       instanceInfo = null;
       typeInfo = null;
     }
   }
 
   /**
    * Returns the {@link AuthenticationManager} for the {@link Connector}
    * instance.
    *
    * @return an AuthenticationManager
    * @throws InstantiatorException
    */
   //@Override
   public synchronized AuthenticationManager getAuthenticationManager()
       throws ConnectorNotFoundException, InstantiatorException {
     return getConnectorInterfaces().getAuthenticationManager();
   }
 
   /**
    * Returns the {@link AuthorizationManager} for the {@link Connector}
    * instance.
    *
    * @return an AuthorizationManager
    * @throws InstantiatorException
    */
   //@Override
   public synchronized AuthorizationManager getAuthorizationManager()
       throws ConnectorNotFoundException, InstantiatorException {
     return getConnectorInterfaces().getAuthorizationManager();
   }
 
   /**
    * Returns the {@link TraversalManager} for the {@link Connector}
    * instance.
    *
    * @return a TraversalManager
    * @throws InstantiatorException
    */
   //@Override
   public synchronized TraversalManager getTraversalManager()
       throws ConnectorNotFoundException, InstantiatorException {
     return getConnectorInterfaces().getTraversalManager();
   }
 
   /**
    * Get populated configuration form snippet for the {@link Connector}
    * instance.
    *
    * @param locale A java.util.Locale which the implementation may use to
    *        produce appropriate descriptions and messages
    * @return a ConfigureResponse object. The form must be prepopulated with the
    *         supplied data in the map.
    * @see ConnectorType#getPopulatedConfigForm(Map, Locale)
    */
   //@Override
   public synchronized ConfigureResponse getConfigForm(Locale locale)
       throws ConnectorNotFoundException, InstantiatorException {
     Map<String, String> configMap = getInstanceInfo().getConnectorConfig();
     ConnectorType connectorType = typeInfo.getConnectorType();
     try {
       return connectorType.getPopulatedConfigForm(configMap, locale);
     } catch (Exception e) {
       throw new InstantiatorException("Failed to get configuration form", e);
     }
   }
 
   /**
    * Retraverses the {@link Connector}'s content from scratch.
    * Halts any traversal in progress and removes any saved traversal state,
    * forcing the Connector to retraverse the Repository from its start.
    */
   //@Override
   public synchronized void restartConnectorTraversal()
       throws ConnectorNotFoundException {
     // Halt any traversal in progress.
     resetBatch();
 
     // Remove any remembered traversal state.  This forces traversal to start
     // at the beginning of the repository.
     setConnectorState(null);
 
     // If Schedule was 'run-once', re-enable it to run again.  But watch out -
     // empty disabled Schedules could look a bit like a run-once Schedule.
     Schedule schedule = getSchedule();
     if (schedule.isDisabled() && schedule.getRetryDelayMillis() == -1
         && !schedule.getTimeIntervals().isEmpty()) {
       schedule.setDisabled(false);
       setConnectorSchedule(schedule.toString());
     } else {
       // Kick off a restart immediately.
       delayTraversal(TraversalDelayPolicy.IMMEDIATE);
     }
   }
 
   /**
    * Returns a traversal {@link Schedule} for the {@link Connector} instance.
    */
   private synchronized Schedule getSchedule() {
     if (traversalSchedule == null) {
       try {
         traversalSchedule = new Schedule(getConnectorSchedule());
       } catch (ConnectorNotFoundException e) {
         return new Schedule();
       }
     }
     return traversalSchedule;
   }
 
   /**
    * Sets the stringified version of traversal {@link Schedule} for the
    * {@link Connector}.
    *
    * @param connectorSchedule String to store or null unset any existing
    *        schedule.
    * @throws ConnectorNotFoundException if the connector is not found
    */
   //@Override
   public synchronized void setConnectorSchedule(String connectorSchedule)
       throws ConnectorNotFoundException {
     // Discard the cached Schedule.
     traversalSchedule = null;
 
     // Persistently store the new schedule.
     getInstanceInfo().setConnectorSchedule(connectorSchedule);
 
     // Update the LoadManager with the new load.
     loadManager.setLoad(getSchedule().getLoad());
 
     // New Schedule may alter DelayPolicy.
     delayTraversal(TraversalDelayPolicy.IMMEDIATE);
   }
 
   /**
    * Fetches the stringified version of traversal {@link Schedule} for the
    * {@link Connector}.
    *
    * @return the schedule String, or null if there is no stored schedule
    *         for this connector.
    * @throws ConnectorNotFoundException if the connector is not found
    */
   //@Override
   public synchronized String getConnectorSchedule()
       throws ConnectorNotFoundException {
     return getInstanceInfo().getConnectorSchedule();
   }
 
   /**
    * Set the Connector's traversal state.
    *
    * @param state a String representation of the state to store.
    *        If null, any previous stored state is discarded.
    * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
    *         does not exist.
    */
   //@Override
   public synchronized void setConnectorState(String state)
       throws ConnectorNotFoundException {
     getInstanceInfo().setConnectorState(state);
   }
 
   /**
    * Returns the Connector's traversal state.
    *
    * @return String representation of the stored state, or
    *         null if no state is stored.
    * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
    *         does not exist.
    */
   //@Override
   public synchronized String getConnectorState()
       throws ConnectorNotFoundException {
     return getInstanceInfo().getConnectorState();
   }
 
   /**
    * Returns the name of the {@link ConnectorType} for this {@link Connector}
    * instance.
    */
   //@Override
   public synchronized String getConnectorTypeName()
       throws ConnectorNotFoundException {
     return getInstanceInfo().getTypeInfo().getConnectorTypeName();
   }
 
   /**
    * Sets the configuration for this {@link ConnectorCoordinator}. If this
    * {@link ConnectorCoordinator} supports persistence this will persist the new
    * configuration.
    */
   //@Override
   public synchronized ConfigureResponse setConnectorConfig(
       TypeInfo newTypeInfo, Map<String, String> configMap, Locale locale,
       boolean update) throws ConnectorNotFoundException,
       ConnectorExistsException, InstantiatorException {
     LOGGER.info("Configuring connector " + name);
     resetBatch();
 
     ConfigureResponse response = null;
     if (instanceInfo != null) {
       if (!update) {
         throw new ConnectorExistsException();
       }
       if (newTypeInfo.getConnectorTypeName().equals(
           typeInfo.getConnectorTypeName())) {
         File connectorDir = instanceInfo.getConnectorDir();
         response = resetConfig(connectorDir, typeInfo, configMap, locale);
       } else {
         // An existing connector is being given a new type - drop then add.
         removeConnector();
         response = createNewConnector(newTypeInfo, configMap, locale);
         if (response != null) {
           // TODO: We need to restore original Connector config. This is
           // necessary once we allow update a Connector with new ConnectorType.
           LOGGER.severe("Failed to update Connector configuration."
               + " Restoring original Connector configuration.");
         }
       }
     } else {
       if (update) {
         throw new ConnectorNotFoundException();
       }
       response = createNewConnector(newTypeInfo, configMap, locale);
     }
     return response;
   }
 
   //@Override
   public synchronized Map<String, String> getConnectorConfig()
       throws ConnectorNotFoundException {
     return getInstanceInfo().getConnectorConfig();
   }
 
   /**
    * Delay future traversals for a short period of time, as dictated by the
    * {@link TraversalDelayPolicy}.
    *
    * @param delayPolicy a TraversalDelayPolicy
    */
   public synchronized void delayTraversal(TraversalDelayPolicy delayPolicy) {
     switch (delayPolicy) {
       case IMMEDIATE:
         traversalDelayEnd = 0;  // No delay.
         break;
 
       case POLL:
         try {
           Schedule schedule = getSchedule();
           int retryDelayMillis = schedule.getRetryDelayMillis();
           if (retryDelayMillis == Schedule.POLLING_DISABLED) {
             if (!schedule.isDisabled()) {
               // We reached then end of the repository, but aren't allowed
               // to poll looking for new content to arrive.  Disable the
               // traversal schedule.
               traversalDelayEnd = 0;
               schedule.setDisabled(true);
              getInstanceInfo().setConnectorSchedule(schedule.toString());
               LOGGER.info("Traversal complete. Automatically pausing "
                   + "traversal for connector " + name);
             }
           } else if (retryDelayMillis > 0) {
             traversalDelayEnd = System.currentTimeMillis() + retryDelayMillis;
           }
         } catch (ConnectorNotFoundException cnfe) {
           // Connector was deleted while processing the batch.  Don't take any
           // action at the moment, as we may be in the middle of a reconfig.
         }
         break;
 
       case ERROR:
         traversalDelayEnd =
             System.currentTimeMillis() + Traverser.ERROR_WAIT_MILLIS;
         break;
     }
   }
 
   /**
    * Returns {@code true} if it is OK to start a traversal,
    * {@code false} otherwise.
    */
   // Package access because this is called by tests.
   synchronized boolean shouldRun() {
     // Are we already running? If so, we shouldn't run again.
     if (taskHandle != null && !taskHandle.isDone()) {
       return false;
     }
 
     // Don't run if we have postponed traversals.
     if (System.currentTimeMillis() < traversalDelayEnd) {
       return false;
     }
 
     Schedule schedule = getSchedule();
 
     // Don't run if traversals are disabled.
     if (schedule.isDisabled()) {
       return false;
     }
 
     // Don't run if we have exceeded our configured host load.
     if (loadManager.shouldDelay()) {
       return false;
     }
 
     // OK to run if we are within one of the Schedule's traversal intervals.
     Calendar now = Calendar.getInstance();
     int hour = now.get(Calendar.HOUR_OF_DAY);
     for (ScheduleTimeInterval interval : schedule.getTimeIntervals()) {
       int startHour = interval.getStartTime().getHour();
       int endHour = interval.getEndTime().getHour();
       if (0 == endHour) {
         endHour = 24;
       }
       if ((hour >= startHour) && (hour < endHour)) {
         return true;
       }
     }
 
     return false;
   }
 
   /**
    * Starts running a batch for this {@link ConnectorCoordinator} if a batch is
    * not already running.
    *
    * @return true if this call started a batch
    * @throws ConnectorNotFoundException if this {@link ConnectorCoordinator}
    *         does not exist.
    */
   //@Override
   public synchronized boolean startBatch() throws ConnectorNotFoundException {
     verifyConnectorInstanceAvailable();
     if (!shouldRun()) {
       return false;
     }
 
     BatchSize batchSize = loadManager.determineBatchSize();
     if (batchSize.getMaximum() == 0) {
       return false;
     }
     taskHandle = null;
     currentBatchKey = new Object();
 
     try {
       BatchCoordinator batchCoordinator = new BatchCoordinator(this);
       TraversalManager traversalManager =
           getConnectorInterfaces().getTraversalManager();
       Traverser traverser = new QueryTraverser(pusherFactory,
           traversalManager, batchCoordinator, name,
           Context.getInstance().getTraversalContext());
       TimedCancelable batch =  new CancelableBatch(traverser, name,
           batchCoordinator, batchCoordinator, batchSize);
       taskHandle = threadPool.submit(batch);
       return true;
     } catch (ConnectorNotFoundException cnfe) {
       LOGGER.log(Level.WARNING, "Connector not found - this is normal if you "
           + " recently reconfigured your connector instance." + cnfe);
     } catch (InstantiatorException ie) {
       LOGGER.log(Level.WARNING, "Connector not found - this is normal if you "
           + " recently reconfigured your connector instance." + ie);
     }
     return false;
   }
 
   /**
    * Records the supplied traversal batch results.  Updates the
    * {@link LoadManager} with number of documents traversed,
    * and implements the requested {@link TraversalDelayPolicy}.
    *
    * @param result a BatchResult
    */
   //@Override
   public synchronized void recordResult(BatchResult result) {
     loadManager.recordResult(result);
     delayTraversal(result.getDelayPolicy());
   }
 
   /**
    * Shuts down this {@link Connector} instance.  Halts any in-progress
    * traversals, instructs the Connector that it is being shut down,
    * and discards the Connector instance.  Any on-disk representation of
    * the connector remains.
    */
   public synchronized void shutdown() {
     resetBatch();
     shutdownConnector(false);
     instanceInfo = null;
   }
 
   /**
    * Halts any in-progess traversals for this {@link Connector} instance.
    * Some or all of the information collected during the current traversal
    * may be discarded.
    */
   synchronized void resetBatch() {
     if (taskHandle != null) {
       taskHandle.cancel();
     }
     taskHandle = null;
     currentBatchKey = null;
     interfaces = null;
   }
 
   /**
    * Informs the Connector instance that it will be shut down
    * and possibly deleted.
    *
    * @param delete {@code true} if the {@code Connector} will be deleted.
    */
   private void shutdownConnector(boolean delete) {
     if (instanceInfo != null
         && instanceInfo.getConnector() instanceof ConnectorShutdownAware) {
       ConnectorShutdownAware csa =
           (ConnectorShutdownAware)(instanceInfo.getConnector());
       try {
         LOGGER.fine("Shutting down connector " + name);
         csa.shutdown();
       } catch (Exception e) {
         LOGGER.log(Level.WARNING, "Problem shutting down connector " + name
             + " during configuration update.", e);
       }
 
       if (delete) {
         try {
           LOGGER.fine("Removing connector " + name);
           csa.delete();
         } catch (Exception e) {
           LOGGER.log(Level.WARNING, "Failed to remove connector " + name, e);
         }
       }
     }
   }
 
   /**
    * Returns the {@link InstanceInfo} representing the associated
    * {@link Connector} instance.
    *
    * @throws ConnectorNotFoundException if there is no associated Connector
    *         instance.
    */
   // Visible for testing.
   InstanceInfo getInstanceInfo() throws ConnectorNotFoundException {
     verifyConnectorInstanceAvailable();
     return instanceInfo;
   }
 
   /**
    * Checks if this {@code ConnectorCoordinator} is associated
    * with an active {@link Connector} instance.
    *
    * @throws ConnectorNotFoundException if there is no associated Connector
    *         instance.
    */
   private void verifyConnectorInstanceAvailable()
       throws ConnectorNotFoundException {
     if (instanceInfo == null) {
       throw new ConnectorNotFoundException("Connector instance " + name
           + " not available.");
     }
   }
 
   /**
    * Returns a {@link ConnectorInterfaces} object that exposes the public
    * interfaces of the associated {@link Connector} instance.
    *
    * @throws ConnectorNotFoundException if there is no associated Connector
    *         instance.
    */
   private ConnectorInterfaces getConnectorInterfaces()
       throws ConnectorNotFoundException {
     if (interfaces == null) {
       InstanceInfo info = getInstanceInfo();
       interfaces = new ConnectorInterfaces(name, info.getConnector());
     }
     return interfaces;
   }
 
   private ConfigureResponse createNewConnector(TypeInfo newTypeInfo,
       Map<String, String> config, Locale locale) throws InstantiatorException {
     if (typeInfo != null) {
       throw new IllegalStateException("Create new connector with type set");
     }
     if (instanceInfo != null) {
       throw new IllegalStateException("Create new connector with existing set");
     }
     File connectorDir = makeConnectorDirectory(name, newTypeInfo);
     try {
       ConfigureResponse result = null;
       result = resetConfig(connectorDir, newTypeInfo, config, locale);
       if (result != null && result.getMessage() != null) {
         removeConnectorDirectory(name, connectorDir, newTypeInfo);
       }
       return result;
     } catch (InstantiatorException ie) {
       removeConnectorDirectory(name, connectorDir, newTypeInfo);
       throw (ie);
     }
   }
 
   private ConfigureResponse resetConfig(File connectorDir,
       TypeInfo newTypeInfo, Map<String, String> proposedConfig, Locale locale)
       throws InstantiatorException {
 
     // Copy the configuration map, adding a couple of additional
     // context properties. validateConfig() may also alter this map.
     Map<String, String> newConfig = new HashMap<String, String>();
     newConfig.putAll(proposedConfig);
     newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_NAME, name);
     newConfig.put(PropertiesUtils.GOOGLE_CONNECTOR_WORK_DIR, connectorDir
         .getPath());
     newConfig.put(PropertiesUtils.GOOGLE_WORK_DIR, Context.getInstance()
         .getCommonDirPath());
 
     // Validate the configuration.
     ConfigureResponse response =
         validateConfig(name, connectorDir, newTypeInfo, newConfig, locale);
     if (response != null) {
       return response;
     }
 
     // We have an apparently valid configuration. Create a connector instance
     // with that configuration.
     InstanceInfo newInstanceInfo =
         InstanceInfo.fromNewConfig(name, connectorDir, newTypeInfo, newConfig);
     if (newInstanceInfo == null) {
       // We don't expect this, because an InstantiatorException should have
       // been thrown, but just in case.
       throw new InstantiatorException("Failed to create connector " + name);
     }
 
     // Tell old connector instance to shut down, as it is being replaced.
     shutdownConnector(false);
 
     // Only after validateConfig and instantiation succeeds do we
     // save the new configuration to persistent store.
     newInstanceInfo.setConnectorConfig(newConfig);
     instanceInfo = newInstanceInfo;
     typeInfo = newTypeInfo;
 
     // The load value in a Schedule is docs/minute.
     loadManager.setLoad(getSchedule().getLoad());
 
     // Allow newly modified connector to resume traversals immediately.
     delayTraversal(TraversalDelayPolicy.IMMEDIATE);
 
     return null;
   }
 
   private static ConfigureResponse validateConfig(String name,
       File connectorDir, TypeInfo newTypeInfo, Map<String, String> config,
       Locale locale) throws InstantiatorException {
     ConnectorInstanceFactory factory =
         new ConnectorInstanceFactory(name, connectorDir, newTypeInfo, config);
     ConfigureResponse response;
     try {
       response =
           newTypeInfo.getConnectorType()
               .validateConfig(config, locale, factory);
     } catch (Exception e) {
       throw new InstantiatorException("Unexpected validateConfig failure.", e);
     } finally {
       factory.shutdown();
     }
 
     if (response != null) {
       // If validateConfig() returns a non-null response with an error message.
       // or populated config form, then consider it an invalid config that
       // needs to be corrected. Return the response so that the config form
       // may be redisplayed.
       if ((response.getMessage() != null)
           || (response.getFormSnippet() != null)) {
         LOGGER.warning("A rejected configuration for connector " + name
             + " was returned.");
         return response;
       }
 
       // If validateConfig() returns a response with no message or formSnippet,
       // but does include a configuration Map; then consider it a valid,
       // but possibly altered configuration and use it.
       if (response.getConfigData() != null) {
         LOGGER.config("A modified configuration for connector " + name
             + " was returned.");
         config.clear();
         config.putAll(response.getConfigData());
       }
     }
     return null;
   }
 
   private static File makeConnectorDirectory(String name, TypeInfo typeInfo)
       throws InstantiatorException {
     File connectorDir = new File(typeInfo.getConnectorTypeDir(), name);
     if (connectorDir.exists()) {
       if (connectorDir.isDirectory()) {
         // we don't know why this directory already exists, but we're ok with it
         LOGGER.warning("Connector directory " + connectorDir.getAbsolutePath()
             + "; already exists for connector " + name);
       } else {
         throw new InstantiatorException("Existing file blocks creation of "
             + "connector directory at " + connectorDir.getAbsolutePath()
             + " for connector " + name);
       }
     } else {
       if (!connectorDir.mkdirs()) {
         throw new InstantiatorException("Can not create "
             + "connector directory at " + connectorDir.getAbsolutePath()
             + " for connector " + name);
       }
     }
 
     // If connectorInstance.xml file does not exist, copy it out of the
     // Connector's jar file.
     File configXml = new File(connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
     if (!configXml.exists()) {
       try {
         InputStream in =
             typeInfo.getConnectorInstancePrototype().getInputStream();
         String config = StringUtils.streamToStringAndThrow(in);
         FileOutputStream out = new FileOutputStream(configXml);
         out.write(config.getBytes("UTF-8"));
         out.close();
       } catch (IOException ioe) {
         LOGGER.log(Level.WARNING, "Failed to extract connectorInstance.xml "
             + " to connector directory at " + connectorDir.getAbsolutePath()
             + " for connector " + name, ioe);
       }
     }
     return connectorDir;
   }
 
   /**
    * Remove the on-disk {@link Connector} representation.  This removes
    * many or all files in the {@code Connector}'s directory.  As a convenience,
    * modified {@code connnectorInstance.xml} files are preserved at this time.
    *
    */
   // TODO: Issue 87: Should we force the removal of files created by the
   // Connector implementation? ConnectorShutdownAware.delete() gives the
   // Connector an opportunity to delete these files in a cleaner fashion.
   private static void removeConnectorDirectory(String name, File connectorDir,
       TypeInfo typeInfo) {
     // Remove the extracted connectorInstance.xml file, but only
     // if it is unmodified.
     // TODO: Remove this when fixing CM Issue 87?
     File configXml = new File(connectorDir, TypeInfo.CONNECTOR_INSTANCE_XML);
     if (configXml.exists()) {
       try {
         InputStream in1 =
             typeInfo.getConnectorInstancePrototype().getInputStream();
         FileInputStream in2 = new FileInputStream(configXml);
         String conf1 = StringUtils.streamToStringAndThrow(in1);
         String conf2 = StringUtils.streamToStringAndThrow(in2);
         if (conf1.equals(conf2) && !configXml.delete()) {
           LOGGER.log(Level.WARNING, "Failed to delete connectorInstance.xml"
               + " from connector directory at "
               + connectorDir.getAbsolutePath() + " for connector " + name);
         }
       } catch (IOException ioe) {
         LOGGER.log(Level.WARNING, "Failed to delete connectorInstance.xml"
             + " from connector directory at " + connectorDir.getAbsolutePath()
             + " for connector " + name, ioe);
       }
     }
 
     if (connectorDir.exists()) {
       if (!connectorDir.delete()) {
         LOGGER.warning("Failed to delete connector directory "
             + connectorDir.getPath()
             + "; this connector may be difficult to delete.");
       }
     }
   }
 }
