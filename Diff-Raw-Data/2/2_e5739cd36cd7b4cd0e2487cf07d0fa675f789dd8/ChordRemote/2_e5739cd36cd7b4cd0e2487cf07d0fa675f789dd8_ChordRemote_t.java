 /***************************************************************************
  *                                                                         *
  * H2O                                                                     *
  * Copyright (C) 2010 Distributed Systems Architecture Research Group      *
  * University of St Andrews, Scotland                                      *
  * http://blogs.cs.st-andrews.ac.uk/h2o/                                   *
  *                                                                         *
  * This file is part of H2O, a distributed database based on the open      *
  * source database H2 (www.h2database.com).                                *
  *                                                                         *
  * H2O is free software: you can redistribute it and/or                    *
  * modify it under the terms of the GNU General Public License as          *
  * published by the Free Software Foundation, either version 3 of the      *
  * License, or (at your option) any later version.                         *
  *                                                                         *
  * H2O is distributed in the hope that it will be useful,                  *
  * but WITHOUT ANY WARRANTY; without even the implied warranty of          *
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the           *
  * GNU General Public License for more details.                            *
  *                                                                         *
  * You should have received a copy of the GNU General Public License       *
  * along with H2O.  If not, see <http://www.gnu.org/licenses/>.            *
  *                                                                         *
  ***************************************************************************/
 
 package org.h2o.db.remote;
 
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.InetSocketAddress;
 import java.net.UnknownHostException;
 import java.sql.SQLException;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Random;
 import java.util.Set;
 
 import org.h2.engine.Constants;
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.db.DatabaseInstance;
 import org.h2o.db.DatabaseInstanceProxy;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.manager.interfaces.ISystemTableReference;
 import org.h2o.db.manager.recovery.LocatorException;
 import org.h2o.db.replication.MetaDataReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.h2o.locator.client.H2OLocatorInterface;
 import org.h2o.util.H2ONetUtils;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.util.exceptions.StartupException;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.p2p.interfaces.IKey;
 import uk.ac.standrews.cs.nds.registry.IRegistry;
 import uk.ac.standrews.cs.nds.registry.RegistryUnavailableException;
 import uk.ac.standrews.cs.nds.registry.stream.RegistryFactory;
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 import uk.ac.standrews.cs.stachord.impl.ChordNodeFactory;
 import uk.ac.standrews.cs.stachord.impl.KeyUnknownException;
 import uk.ac.standrews.cs.stachord.impl.RemoteChordException;
 import uk.ac.standrews.cs.stachord.interfaces.IChordNode;
 import uk.ac.standrews.cs.stachord.interfaces.IChordRemoteReference;
 
 /**
  * The interface between a local database instance and the rest of the database system.
  *
  * <p>
  * Methods defined in IChordInterface relate to the database's interface and interactions with Chord.
  *
  * <p>
  * Methods defined in IDatabaseRemote represent the rest of the databases distributed state such as remote references to the local
  * databases, database lookup capabilities.
  *
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class ChordRemote implements IDatabaseRemote, IChordInterface, Observer {
 
     /**
      * The remote interface of the local database instance.
      */
     private IDatabaseInstanceRemote localInstance;
 
     /**
      * Location information for the local database and chord instance.
      */
     private final DatabaseID localMachineLocation;
 
     /**
      * Local wrapper for the System Table.
      */
     private final ISystemTableReference systemTableRef;
 
     /**
      * Name under which the local database instance is bound to its RMI registry.
      */
     private static final String LOCAL_DATABASE_INSTANCE = "LOCAL_INSTANCE";
 
     /**
      * The timeout interval for contacting locator servers.
      */
     private static final long LOCATOR_CONTACT_RETRY_TIMEOUT = 30000;
 
     /**
      * The retry interval between successive attempts to contact locator servers.
      */
     private static final long LOCATOR_CONTACT_RETRY_WAIT = 2000;
 
     /**
      * The local chord node for this database instance.
      */
     private IChordNode chordNode;
 
     /**
      * Used to cache the location of the System Table by asking the known node where it is on startup. This is only ever really used for
      * this initial lookup. The rest of the System Table functionality is hidden behind the SystemTableReference object.
      */
     private DatabaseID actualSystemTableLocation = null;
 
     /**
      * This chord nodes predecessor in the ring. When the predecessor changes this is used to determine if the System Table was located on
      * the old predecessor, and to check whether it has failed.
      */
     private IChordRemoteReference predecessor;
 
     private boolean inShutdown = false;
 
     private H2OLocatorInterface locatorInterface;
 
     private Settings databaseSettings;
 
     private MetaDataReplicaManager metaDataReplicaManager = null;
 
     private DatabaseID predecessorURL;
 
     private final Database db;
 
     public static final String REGISTRY_PREFIX = "H2O_DATABASE_INSTANCE_";
 
     /**
      * Port to be used for the next database instance. Currently used for testing.
      */
     private static int currentPort = 30000;
 
     public static synchronized int getCurrentPort() {
 
         return currentPort++;
     }
 
     public static synchronized void setCurrentPort(final int port) {
 
         currentPort = port;
     }
 
     public ChordRemote(final DatabaseID localMachineLocation, final ISystemTableReference systemTableRef, final Database db) {
 
         this.systemTableRef = systemTableRef;
         this.localMachineLocation = localMachineLocation;
         this.db = db;
     }
 
     @Override
     public DatabaseID connectToDatabaseSystem(final Session session, final Settings databaseSettings) throws StartupException {
 
         this.databaseSettings = databaseSettings;
 
         establishChordConnection(localMachineLocation, session);
 
         /*
          * The System Table location must be known at this point, otherwise the database instance will not start.
          */
         if (systemTableRef.getSystemTableURL() == null) {
             ErrorHandling.hardError("System Table not known. This can be fixed by creating a known hosts file (called " + localMachineLocation.sanitizedLocation() + ".instances.properties) and adding the location of a known host.");
         }
 
         return systemTableRef.getSystemTableURL();
     }
 
     /**
      * Attempt to establish a new Chord connection by trying to connect to a number of known hosts.
      *
      * If no established ring is found a new Chord ring will be created.
      *
      * @param databaseSettings
      */
     private DatabaseID establishChordConnection(final DatabaseID localMachineLocation, final Session session) throws StartupException {
 
         boolean connected = false;
 
         int attempts = 1; // attempts at connected
 
         DatabaseID newSMLocation = null;
 
         /*
          * Contact descriptor for SM locations.
          */
 
         /*
          * Try to connect repeatedly until successful. There is a back-off mechanism to ensure this doesn't fail repeatedly in a short space
          * of time.
          */
 
         List<String> databaseInstances = null;
 
         final int maxNumberOfAttempts = Integer.parseInt(databaseSettings.get("ATTEMPTS_TO_CREATE_OR_JOIN_SYSTEM"));
 
         while (!connected && attempts < maxNumberOfAttempts) {
             try {
                 databaseInstances = locatorInterface.getLocations();
             }
             catch (final Exception e) {
                 throw new StartupException(e.getMessage());
             }
 
             /*
              * If this is the first time DB to be run the set of DB instance will be empty and this node should become the schema manager.
              * If there is a list of DB instances this instance should attempt to connect to one of them (but not to itself). If none exist
              * but for itself then it can start as the schema manager. If none exist and it isn't on the list either, just shutdown the
              * database.
              */
 
             if (databaseInstances != null && databaseInstances.size() > 0) {
                 /*
                  * There may be a number of database instances already in the ring. Try to connect.
                  */
                 connected = attemptToJoinChordRing(databaseSettings.getLocalSettings(), localMachineLocation, databaseInstances);
             }
 
             if (connected) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Successfully connected to existing chord ring.");
             }
             else {
                 /*
                  * Check whether the local machines URL is included on the list of possible schema managers.
                  */
                 boolean localMachineIncluded = false;
                 if (databaseInstances != null) {
                     for (final String instance : databaseInstances) {
                         if (instance.contains(localMachineLocation.getURL())) {
                             localMachineIncluded = true;
                             break;
                         }
                     }
                 }
 
                 if (!connected && (databaseInstances == null || databaseInstances.size() == 0 || localMachineIncluded)) {
                     /*
                      * Either because there are no known hosts, or because none are still alive. Create a new chord ring.
                      */
 
                     // Obtain a lock on the locator server first.
 
                     final H2OPropertiesWrapper localSettings = databaseSettings.getLocalSettings();
 
                     boolean locked = false;
                     try {
                         locked = locatorInterface.lockLocators(this.localMachineLocation.getDbLocation());
                     }
                     catch (final IOException e) {
                         throw new StartupException("Couldn't obtain a lock to create a new System Table. " + "An IO Exception was thrown trying to contact the locator server (" + e.getMessage() + ").");
                     }
 
                     if (locked) {
                         final String chordPort = localSettings.getProperty("chordPort");
 
                         int portToUse = getCurrentPort();
                         if (chordPort != null) { //TODO remove
                             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Obtained chord port from disk: " + chordPort);
                             portToUse = Integer.parseInt(chordPort);
                         }
 
                         connected = startChordRing(localMachineLocation.getHostname(), portToUse, localMachineLocation);
 
                         if (connected) {
                             localSettings.setProperty("chordPort", portToUse + "");
                             try {
                                 localSettings.saveAndClose();
                             }
                             catch (final IOException e) {
                                 throw new StartupException("Couldn't save Chord port to properties file");
                             }
                         }
 
                         newSMLocation = localMachineLocation;
 
                         systemTableRef.setSystemTableURL(newSMLocation);
                     }
                 }
 
                 if (!connected) {
                     /*
                      * Back-off then try to connect again up to n times. If this fails, throw an exception.
                      */
 
                     final Random r = new Random();
                     try {
                         final int backoffTime = (1000 + r.nextInt(100) * 10) * attempts;
                         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Trying to connect to Chord ring. Attempt number " + attempts + " of " + databaseSettings.get("ATTEMPTS_TO_CREATE_OR_JOIN_SYSTEM") + ". Instance at " + localMachineLocation + " is about to back-off for " + backoffTime + " ms.");
 
                         Thread.sleep(backoffTime);
                     }
                     catch (final InterruptedException e) {
                         e.printStackTrace();
                     }
                     attempts++;
                 }
             }
         }
 
         /*
          * If still not connected after many attempts, throw an exception.
          */
         if (!connected) {
             final StringBuilder instances = new StringBuilder();
             for (final String instance : databaseInstances) {
                 instances.append(instance + "\n");
             }
 
             throw new StartupException("\n\nAfter " + attempts + " the H2O instance at " + localMachineLocation + " couldn't find an active instance with System Table state, so it cannot connect to the database system.\n\n" + "Please re-instantiate one of the following database instances:\n\n"
                             + instances + "\n\n");
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Database at " + localMachineLocation + " successful created/connected to chord ring.");
 
         try {
             final DatabaseID dbID = systemTableRef.getSystemTableURL();
 
             if (dbID == null) {
                 systemTableRef.setSystemTableURL(getSystemTableLocation());
             }
         }
         catch (final RPCException e) {
             ErrorHandling.exceptionError(e, "Failed to get the System Table URL.");
         }
 
         /*
          * Create the local database instance remote interface and register it. This must be done before meta-records are executed.
          */
         localInstance = new DatabaseInstance(localMachineLocation, session);
 
         assert systemTableRef.getSystemTableURL() != null;
 
         return systemTableRef.getSystemTableURL();
     }
 
     @Override
     public H2OLocatorInterface getLocatorInterface() throws LocatorException {
 
         // Obtain a reference to the locator servers if one is not already held.
         if (locatorInterface == null) {
             establishLocatorInterface();
         }
 
         return locatorInterface;
     }
 
     private void establishLocatorInterface() throws LocatorException {
 
         try {
             final H2OPropertiesWrapper persistedInstanceInformation = H2OPropertiesWrapper.getWrapper(localMachineLocation);
             persistedInstanceInformation.loadProperties();
             establishLocatorInterface(persistedInstanceInformation);
         }
         catch (final IOException e) {
             throw new LocatorException(e.getMessage());
         }
     }
 
     /**
      * Get a reference to the locator servers for this database system.
      *
      * @param localDatabaseProperties a properties file containing the location of the database descriptor and the name of the database
      * @return
      * @throws StartupException if the descriptor file couldn't be found.
      */
     private void establishLocatorInterface(final H2OPropertiesWrapper localDatabaseProperties) throws LocatorException {
 
         final String descriptorLocation = localDatabaseProperties.getProperty("descriptor");
 
         if (descriptorLocation == null) { throw new LocatorException("The location of the database descriptor was not specified. The database will now exit."); }
 
         final long startTime = System.currentTimeMillis();
         while (true) {
             try {
                 locatorInterface = new H2OLocatorInterface(descriptorLocation);
                 return;
             }
             catch (final IOException e) {
                 // Wait and try again if timeout has not been exceeded.
 
                 if (System.currentTimeMillis() - startTime > LOCATOR_CONTACT_RETRY_TIMEOUT) { throw new LocatorException(e.getMessage()); }
                 try {
                     Thread.sleep(LOCATOR_CONTACT_RETRY_WAIT);
                 }
                 catch (final InterruptedException e1) {
                     // Ignore and carry on.
                 }
             }
         }
     }
 
     /**
      * Try to join an existing chord ring.
      *
      * @return true if a connection was successful
      * @throws StartupException
      */
     private boolean attemptToJoinChordRing(final H2OPropertiesWrapper persistedInstanceInformation, final DatabaseID localMachineLocation, final List<String> databaseInstances) throws StartupException {
 
         /*
          * Try to connect via each of the database instances that are known.
          */
         for (final String url : databaseInstances) {
             final DatabaseID instanceURL = DatabaseID.parseURL(url);
 
             /*
              * Check first that the location isn't the local database instance (currently running).
              */
             if (instanceURL.equals(localMachineLocation)) {
                 continue;
             }
 
             // Attempt to connect to a Chord node at this location.
             final String chordPort = persistedInstanceInformation.getProperty("chordPort");
 
             int localChordPort = 0;
             if (chordPort != null) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Obtained chord port from disk: " + chordPort);
                 localChordPort = Integer.parseInt(chordPort);
             }
             else {
                 localChordPort = getCurrentPort();
             }
 
             if (instanceURL.getRMIPort() == localChordPort) {
                 localChordPort++;
             }
 
             boolean connected = false;
             try {
                 connected = joinChordRing(localMachineLocation.getHostname(), localChordPort, instanceURL.getHostname(), instanceURL.getRMIPort(), localMachineLocation.sanitizedLocation());
             }
             catch (final RPCException e1) {
                 connected = false;
             }
 
             if (connected) {
                 try {
                     persistedInstanceInformation.saveAndClose();
                 }
                 catch (final IOException e) {
                     throw new StartupException("Couldn't save Chord port to properties file");
                 }
                 chordNode.addObserver(this);
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Successfully connected to an existing chord ring at " + url);
                 return true;
             }
         }
 
         return false;
     }
 
     @Override
     public IDatabaseInstanceRemote getLocalDatabaseInstance() {
 
         return localInstance;
     }
 
     @Override
     public DatabaseID getLocalMachineLocation() {
 
         return localMachineLocation;
     }
 
     @Override
     public IDatabaseInstanceRemote getDatabaseInstanceAt(final IChordRemoteReference lookupLocation) throws RPCException, RPCException {
 
         return getDatabaseInstanceAt(lookupLocation, false);
     }
 
     private IDatabaseInstanceRemote getDatabaseInstanceAt(final IChordRemoteReference lookupLocation, final boolean inShutdown) throws RPCException, RPCException {
 
         final InetSocketAddress address = lookupLocation.getRemote().getAddress();
 
         final String hostname = address.getHostName();
 
         return getDatabaseInstanceAt(hostname, inShutdown);
     }
 
     public IDatabaseInstanceRemote getDatabaseInstanceAt(final String hostname, final boolean inShutdown) throws RPCException, RPCException {
 
         try {
             final IRegistry registry = RegistryFactory.FACTORY.getRegistry(InetAddress.getByName(hostname));
 
             final Map<String, Integer> serverLocations = registry.getEntries();
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, PrettyPrinter.toString("Registry contents: " + serverLocations.keySet()));
 
             for (final Entry<String, Integer> applicationRegistryMap : serverLocations.entrySet()) {
 
                 if (applicationRegistryMap.getKey().startsWith(REGISTRY_PREFIX) && !applicationRegistryMap.getKey().equals(getApplicationRegistryIDForLocalDatabase())) {
 
                     //If this is not the local machine being listed.
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Looking for another active database ID in application registry. Currently looking at: " + applicationRegistryMap.getKey());
 
                     final IDatabaseInstanceRemote instanceReference = DatabaseInstanceProxy.getProxy(new InetSocketAddress(hostname, applicationRegistryMap.getValue()));
 
                     try {
                         instanceReference.isAlive();
 
                         return instanceReference; // return a reference if this database instance is active.
                     }
                     catch (final Exception e) {
 
                         ErrorHandling.exceptionError(e, "Couldn't connect to database instance with ID: " + applicationRegistryMap.getKey() + " on server port " + applicationRegistryMap.getValue());
                         //Try again if the database is not active.
                     }
                 }
             }
 
            Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, localMachineLocation + ": Failed to find an active instance on the machine specified: " + hostname + ". Number of application registry entries: " + serverLocations.size());
 
             return null;
         }
         catch (final UnknownHostException e) {
             ErrorHandling.exceptionError(e, "Failed to find the host specified: " + hostname);
         }
         catch (final RegistryUnavailableException e) {
 
             if (!inShutdown) {
                 ErrorHandling.errorNoEvent("Couldn't find an active registry on this machine (" + hostname + "). Restarting registry locally.");
                 recreateRegistryIfItHasFailed();
                 return getDatabaseInstanceAt(hostname, true);
             }
             else {
                 ErrorHandling.errorNoEvent("Couldn't find an active registry on this machine (" + hostname + "). Not attempting to restart registry.");
             }
         }
 
         return null;
     }
 
     @Override
     public void recreateRegistryIfItHasFailed() {
 
         try {
             final IRegistry registry = RegistryFactory.FACTORY.getRegistry(true);
             registry.rebind(getApplicationRegistryIDForLocalDatabase(), db.getDatabaseInstanceServer().getAddress().getPort());
         }
         catch (final Exception e) {
             ErrorHandling.exceptionError(e, "Error trying to recreate registry and adding the local database instance port.");
         }
     }
 
     /**
      * Produces the key used to store connection information (port of the database instance server) for the
      * database given as a parameter.
      * @param databaseID    Database whose key is to be created.
      * @return Key to find this databases entry in the application registry.
      */
     public static String getApplicationRegistryID(final String databaseID) {
 
         return REGISTRY_PREFIX + databaseID;
     }
 
     @Override
     public String getApplicationRegistryIDForLocalDatabase() {
 
         return REGISTRY_PREFIX + localMachineLocation.getID();
     }
 
     @Override
     public IDatabaseInstanceRemote getDatabaseInstanceAt(final DatabaseID dbID) throws RPCException {
 
         if (dbID.equals(localMachineLocation)) { return getLocalDatabaseInstance(); }
 
         return getDatabaseInstanceAt(dbID.getHostname(), dbID.getID());
 
     }
 
     @Override
     public IDatabaseInstanceRemote getDatabaseInstanceAt(final String hostname, final String databaseName) throws RPCException {
 
         try {
             final IRegistry registry = RegistryFactory.FACTORY.getRegistry(InetAddress.getByName(hostname));
 
             final int port = registry.lookup(getApplicationRegistryID(databaseName));
 
             return DatabaseInstanceProxy.getProxy(new InetSocketAddress(hostname, port));
         }
         catch (final UnknownHostException e) {
             ErrorHandling.exceptionError(e, "Failed to find the host specified: " + hostname);
         }
         catch (final RegistryUnavailableException e) {
             recreateRegistryIfItHasFailed(); //this call recreates only the local registry, which may not be the registry involved in this case.
             ErrorHandling.errorNoEvent("Couldn't find an active registry on " + hostname + ". If the machine is still active the registry will eventually be recreated.");
         }
 
         return null;
     }
 
     /**
      * Start a new Chord ring at the specified location.
      *
      * @param hostname the hostname on which the Chord ring will be started. This must be a local address to the machine on which this process is running.
      * @param port the port on which the Chord node will listen.
      * @return true if the chord ring was started successfully; otherwise false.
      */
     private boolean startChordRing(final String hostname, final int port, final DatabaseID databaseURL) {
 
         final InetSocketAddress localChordAddress = new InetSocketAddress(hostname, port);
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, localMachineLocation + ": Deploying new Chord ring on " + localChordAddress);
 
         /*
          * Create a new Chord Ring.
          */
         try {
             chordNode = new ChordNodeFactory().createNode(localChordAddress);
         }
         catch (final Exception e) {
             ErrorHandling.exceptionError(e, "Failed to create new Chord ring.");
             return false;
         }
 
         systemTableRef.setLookupLocation(chordNode.getSelfReference());
 
         actualSystemTableLocation = databaseURL;
 
         systemTableRef.setInKeyRange(true);
         chordNode.addObserver(this);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Started local Chord node on : ", databaseURL.sanitizedLocation(), " : ", hostname, ":", port, " : initialized with key :", chordNode.getKey().toString(10), " : ", chordNode.getKey(), " : System Table at ", systemTableRef.getLookupLocation(),
                         " : ");
 
         return true;
     }
 
     /**
      * Join an existing chord ring.
      *
      * @param localHostname
      *            The hostname on which this node will start. This must be a local address to the machine on which this process is running.
      * @param localPort
      *            The port on which this node will listen. The RMI server will run on this port.
      * @param remoteHostname
      *            The hostname of a known host in the existing Chord ring.
      * @param remotePort
      *            The port on which a known host (database instance server) is listening.
      * @param databaseName
      *            The name of the database instance starting this Chord ring. This information is used purely for diagnostic output, so can
      *            be left null.
      * @return true if a node was successfully created and joined an existing Chord ring; otherwise false.
      * @throws RemoteChordException
      */
     private boolean joinChordRing(final String localHostname, int localPort, final String remoteHostname, final int remotePort, final String databaseName) throws RPCException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Trying to connect to existing database on " + remoteHostname + ":" + remotePort);
 
         /*
          * Connect to the database running at this port. From this database get the address/port of its local chord node.
          */
         final DatabaseInstanceProxy remoteInstance = DatabaseInstanceProxy.getProxy(new InetSocketAddress(remoteHostname, remotePort));
 
         final int remoteChordPort = remoteInstance.getChordPort();
 
         localPort = H2ONetUtils.getInactiveTCPPort(localPort);
 
         InetSocketAddress localChordAddress = new InetSocketAddress(localHostname, localPort);
         final InetSocketAddress knownHostAddress = new InetSocketAddress(remoteHostname, remoteChordPort);
 
         boolean connected = false;
         int attempts = 0;
 
         final int maxNumberOfAttempts = Integer.parseInt(databaseSettings.get("ATTEMPTS_AFTER_BIND_EXCEPTIONS"));
 
         final ChordNodeFactory factory = new ChordNodeFactory();
 
         while (!connected && attempts < maxNumberOfAttempts) { // only have multiple attempts if there is a bind exception.
 
             try {
                 chordNode = factory.createNode(localChordAddress);
                 chordNode.join(factory.bindToNode(knownHostAddress));
 
                 Diagnostic.trace(localMachineLocation + ": Created chord node on: " + localChordAddress + ". Known host: " + knownHostAddress);
             }
             catch (final RemoteChordException e) { // database instance we're trying to connect to doesn't exist.
 
                 ErrorHandling.errorNoEvent("RCE: Failed to connect to chord node on + " + localHostname + ":" + localPort + " known host: " + remoteHostname + ":" + remoteChordPort);
                 return false;
             }
             catch (final KeyUnknownException e) {
                 ErrorHandling.errorNoEvent("Couldn't perform Chord lookup because predecessor key was not known.");
                 connected = false;
             }
             catch (final RPCException e) {
                 ErrorHandling.errorNoEvent("An RPCException was thrown connecting to + " + localHostname + ":" + localPort + " known host: " + remoteHostname + ":" + remoteChordPort);
                 connected = false;
             }
             catch (final Exception e) {
                 ErrorHandling.errorNoEvent("E: Failed to create new chord node on + " + localHostname + ":" + localPort + " known host: " + remoteHostname + ":" + remoteChordPort);
                 connected = false;
             }
 
             if (chordNode != null) {
                 connected = true;
             }
             if (!connected) {
                 localChordAddress = new InetSocketAddress(localHostname, localPort++);
             }
 
             attempts++;
         }
 
         if (!connected) { return false; }
 
         systemTableRef.setInKeyRange(false);
 
         try {
             final IDatabaseInstanceRemote lookupInstance = getDatabaseInstanceAt(remoteHostname, false);
 
             if (lookupInstance != null) {
                 actualSystemTableLocation = lookupInstance.getSystemTableURL();
                 systemTableRef.setSystemTableURL(actualSystemTableLocation);
             }
             else {
                 ErrorHandling.hardError(localMachineLocation + ": Couldn't find another lookup instance on startup. Looking for remote host at: " + remoteHostname);
             }
         }
         catch (final RPCException e) {
             e.printStackTrace();
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Started local Chord node on : " + databaseName + " : " + localHostname + " : " + localPort + " : initialized with key :" + chordNode.getKey().toString(10) + " : " + chordNode.getKey() + " : System Table at " + systemTableRef.getLookupLocation()
                         + " : " + chordNode.getSuccessor().getCachedKey());
 
         return true;
     }
 
     /**
      * Called by various chord functions in {@link ChordNodeImpl} which are being observed. Of particular interest to this class is the case
      * where the predecessor of a node changes. This is used to assess whether the System Tables location has changed.
      *
      * <p>
      * If changing this method please note that it is called synchronously by the Observable class, ChordNodeImpl. This means that if you
      * try and do something such as chordNode.stabilize() you will possibly introduce some form of deadlock into Chord. This is difficult to
      * debug, but is the most likely cause of a ring failing to close properly (i.e. not stablizing even after an extended period).
      *
      * @see java.util.Observer#update(java.util.Observable, java.lang.Object)
      */
     @Override
     public void update(final Observable o, final Object arg) {
 
         /*
          * If the predecessor of this node has changed.
          */
         if (arg.equals(IChordNode.PREDECESSOR_CHANGE_EVENT)) {
             predecessorChangeEvent();
         }
         else if (arg.equals(IChordNode.SUCCESSOR_CHANGE_EVENT)) {
             successorChangeEvent();
         }
         else if (arg.equals(IChordNode.OWN_ADDRESS_CHANGE_EVENT)) {
             ownAddressChangeEvent();
         }
     }
 
     /**
      * Called when this H2O instances primary IP changes. This will happen when, for example, a laptop switches from a wired connection
      * to a wireless connection, or if a machine is set to hibernate/sleep and it comes online in a different network.
      *
      * <p>H2O must treat an IP change as if the H2O instance had failed and restarted (because other instances will no longer be able to
      * access this instance at the known address). Consequently, any running System Table or Table Managers on this machine must be stopped
      * until the database can successfully obtain permission from the locator servers to restart the System Table.
      */
     private void ownAddressChangeEvent() {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "The local machines IP address has changed.");
 
         //TODO Terminate local System Table / Table Manager Processes.
 
         //TODO Attempt to make connection with active System Table via a locator server.
 
         //TODO If attempt fails (but it is possible to connect to the locator server) request to recrate the System Table locally (if possible).
     }
 
     /**
      * Called when the successor has changed. Used to check whether the System Table was on the predecessor, and if it was (and has failed)
      * restart the System Table elsewhere.
      */
     private void predecessorChangeEvent() {
 
         try {
             if (predecessor == null) {
                 predecessor = chordNode.getPredecessor();
                 if (predecessorURL == null) {
                     getPredecessorUrl();
                 }
                 return;
             }
             predecessor.getRemote().getPredecessor();
 
             if (predecessorURL == null) {
                 getPredecessorUrl();
             }
             return; // the old predecessor has not failed, so nothing needs to be recovered.
         }
         catch (final RPCException e1) {
             // If the old predecessor is no longer available it has failed - try to recover processes.
             if (predecessorURL != null) {
                 H2OEventBus.publish(new H2OEvent(predecessorURL.getURL(), DatabaseStates.DATABASE_FAILURE, null));
             }
         }
 
         final boolean systemTableWasOnPredecessor = systemTableRef.isThisSystemTableNode(predecessor);
 
         if (!systemTableWasOnPredecessor && predecessorURL != null) {
             try {
                 systemTableRef.suspectInstanceOfFailure(predecessorURL);
             }
             catch (final Exception e) {
                 try {
                     systemTableRef.failureRecovery();
                 }
                 catch (final Exception e1) {
                     //Don't do anything else if this fails here.
                 }
             }
         }
 
         predecessor = chordNode.getPredecessor();
 
         /*
          * This will often be null at this point because it hasn't stabilized.
          */
         if (predecessor != null) {
             getPredecessorUrl();
 
         }
         else {
             predecessorURL = null;
         }
 
         boolean systemTableAlive = true;
         ISystemTableMigratable newSystemTable = null;
         if (systemTableWasOnPredecessor) {
             try {
                 systemTableAlive = isSystemTableActive();
             }
             catch (final Exception e1) {
                 systemTableAlive = false;
             }
 
             if (!systemTableAlive) {
                 try {
                     newSystemTable = systemTableRef.findSystemTable();
 
                     // Now try to recreate any Table Managers that were on the failed machine.
                     try {
                         newSystemTable.checkTableManagerAccessibility();
                     }
                     catch (final Exception e) {
                         e.printStackTrace();
                     }
                 }
                 catch (final SQLException e) {
                     // If this fails it doesn't matter at this point.
                 }
             }
         }
     }
 
     public void getPredecessorUrl() {
 
         try {
             final IDatabaseInstanceRemote remoteInstance = getDatabaseInstanceAt(predecessor);
             if (remoteInstance != null) {
                 predecessorURL = remoteInstance.getURL();
             }
         }
         catch (final RPCException e) {
             e.printStackTrace();
         }
 
     }
 
     private boolean isSystemTableActive() {
 
         boolean systemTableAlive;
         try {
             systemTableRef.getSystemTable().checkConnection();
             systemTableAlive = true;
         }
         catch (final Exception e) {
             systemTableAlive = false;
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "The System Table is no longer accessible.");
         }
         return systemTableAlive;
     }
 
     /**
      * The successor has changed. Make sure the System Table is replicated to the new successor if this instance is controlling the schema
      * manager.
      */
     private void successorChangeEvent() {
 
         if (Constants.IS_NON_SM_TEST) { return; // Don't do this if we're testing something that isn't to do with this replication.
         }
 
         /*
          * Check whether there are any table managers running locally.
          */
         Set<TableManagerWrapper> localTableManagers = null;
         try {
 
             final ISystemTableMigratable systemTable = systemTableRef.getSystemTable();
 
             /*
              * If systemTable is null then the previous successor has failed and it was the System Table, so no System Table exists currently. It is not the
              * responsibility of this node (the System Table's predecessor) to restart the System Table.
              */
             if (systemTable != null) {
                 localTableManagers = systemTable.getLocalTableManagers(localMachineLocation);
             }
         }
         catch (final RPCException e) {
             ErrorHandling.errorNoEvent("Remote exception thrown. Happens when successor has very recently changed and chord ring hasn't stabilized.");
         }
         catch (final MovedException e) {
             try {
                 systemTableRef.handleMovedException(e);
             }
             catch (final SQLException e1) {
                 e1.printStackTrace();
             }
         }
 
         final IChordRemoteReference successor = chordNode.getSuccessor();
 
         /*
          * If table managers running locally or the System Table is located locally then get a reference to the successor instance so that
          * we can replicate meta-data onto it. If not, don't go to the effort of looking up the successor.
          */
         try {
 
             final InetSocketAddress address = successor.getRemote().getAddress();
             final String hostname = address.getHostName();
 
             final IDatabaseInstanceRemote successorInstance = getDatabaseInstanceAt(hostname, false);
 
             if (successorInstance != null) {
 
                 if (systemTableRef.isSystemTableLocal() || localTableManagers != null && localTableManagers.size() > 0) {
 
                     final DatabaseInstanceWrapper successorInstanceWrapper = new DatabaseInstanceWrapper(successorInstance.getURL(), successorInstance, true);
 
                     metaDataReplicaManager.replicateMetaDataToRemoteInstance(systemTableRef, true, successorInstanceWrapper);
                 }
 
                 /*
                  * Now do the same thing for table manager replication.
                  */
 
                 if (localTableManagers != null && metaDataReplicaManager != null) {
 
                     // delete query must remove entries for all table managers
                     // replicated on this machine.
 
                     final DatabaseInstanceWrapper successorInstanceWrapper = new DatabaseInstanceWrapper(successorInstance.getURL(), successorInstance, true);
 
                     metaDataReplicaManager.replicateMetaDataToRemoteInstance(systemTableRef, false, successorInstanceWrapper);
                 }
             }
             else {
                 Diagnostic.trace("Not yet able to find the successor instance of this database, so unable to replicate any more state onto it.");
             }
         }
         catch (final RPCException e) {
             ErrorHandling.errorNoEvent("Successor not known: " + e.getMessage());
         }
     }
 
     @Override
     public void shutdown() {
 
         if (inShutdown) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Chord node is already shutting down: " + chordNode);
             return;
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Shutting down node: " + chordNode);
 
         inShutdown = true;
 
         if (chordNode == null) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Chord node was not initialized so the system is shutting down without transferring any active tables or managers.");
             return;
         }
 
         final IChordRemoteReference successor = chordNode.getSuccessor();
 
         boolean successorIsDifferentMachine = false;
         try {
             successorIsDifferentMachine = successor != null && !chordNode.getKey().equals(successor.getCachedKey());
         }
         catch (final RPCException e1) {
             successorIsDifferentMachine = true;
         }
         final boolean thisIsntATestThatShouldPreventThis = !Constants.IS_NON_SM_TEST && !Constants.IS_TEAR_DOWN;
         final boolean systemTableHeldLocally = systemTableRef.isSystemTableLocal();
 
         IDatabaseInstanceRemote successorDB = null;
 
         if (successorIsDifferentMachine && thisIsntATestThatShouldPreventThis) {
 
             /*
              * Migrate any local Table Managers.
              */
             try {
                 successorDB = getDatabaseInstanceAt(successor, true);
 
                 final Set<TableManagerWrapper> localManagers = systemTableRef.getSystemTable().getLocalTableManagers(getLocalMachineLocation());
 
                 /*
                  * Create replicas if needed.
                  */
                 for (final TableManagerWrapper wrapper : localManagers) {
 
                     final ITableManagerRemote dmr = wrapper.getTableManager();
                     if (dmr.getActiveReplicas().containsKey(new DatabaseInstanceWrapper(localMachineLocation, localInstance, true)) && dmr.getActiveReplicas().size() == 1) {
                         // This machine holds the only replica - replicate on the successor as well.
                         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Replicating table [" + wrapper.getTableInfo().getFullTableName() + "] to successor: " + successor);
 
                         successorDB.executeUpdate("CREATE REPLICA " + wrapper.getTableInfo().getFullTableName() + ";", false);
                     }
                 }
 
                 /*
                  * Migrate Table Managers.
                  */
                 for (final TableManagerWrapper wrapper : localManagers) {
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Migrating Table Manager [" + wrapper.getTableInfo().getFullTableName() + "] to successor: " + successor);
 
                     successorDB.executeUpdate("MIGRATE TABLEMANAGER " + wrapper.getTableInfo().getFullTableName(), false);
                 }
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionError(e, "(Error during shutdown on " + db.getID() + ") " + e.getMessage());
             }
 
             /*
              * Migrate the System Table if needed.
              */
             if (systemTableHeldLocally) {
 
                 // Migrate the System Table to this node before shutdown.
                 try {
                     Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Migrating System Table to successor: " + successor);
                     successorDB = getDatabaseInstanceAt(successor);
 
                     successorDB.executeUpdate("MIGRATE SYSTEMTABLE", false);
 
                 }
                 catch (final Exception e) {
                     ErrorHandling.errorNoEvent("Failed to migrate System Table to successor: " + successor);
                 }
             }
         }
 
         chordNode.shutDown();
 
     }
 
     @Override
     public IChordNode getChordNode() {
 
         return chordNode;
     }
 
     @Override
     public DatabaseID getSystemTableLocation() throws RPCException, RPCException {
 
         if (actualSystemTableLocation != null) { return actualSystemTableLocation; }
 
         final IChordRemoteReference lookupLocation = null;
         final DatabaseID stLocation = lookupSystemTableNodeLocation();
         systemTableRef.setLookupLocation(lookupLocation);
 
         final String lookupHostname = stLocation.getHostname();
         final String databaseName = stLocation.getID();
         IDatabaseInstanceRemote lookupInstance;
 
         lookupInstance = getDatabaseInstanceAt(lookupHostname, databaseName);
 
         actualSystemTableLocation = lookupInstance.getSystemTableURL();
         systemTableRef.setSystemTableURL(actualSystemTableLocation);
 
         return actualSystemTableLocation;
     }
 
     @Override
     public DatabaseID lookupSystemTableNodeLocation() throws RPCException {
 
         try {
             final List<String> locations = locatorInterface.getLocations();
 
             return DatabaseID.parseURL(locations.get(0));
         }
         catch (final IOException e) {
             ErrorHandling.exceptionError(e, "Failed to contact locator servers, so unable to get the location of a System Table.");
             return null;
         }
     }
 
     @Override
     public IChordRemoteReference getLocalChordReference() {
 
         return chordNode.getSelfReference();
     }
 
     @Override
     public IChordRemoteReference getLookupLocation(final IKey systemTableKey) throws RPCException {
 
         return chordNode.lookup(systemTableKey);
     }
 
     @Override
     public boolean inShutdown() {
 
         return inShutdown;
     }
 
     /**
      * Called when the local database has been created, has started an ST, and is ready to receive requests.
      *
      * <p>
      * The system will start throwing errors about meta-tables not existing if this is called too soon.
      */
     public boolean commitSystemTableCreation() {
 
         boolean successful = false;
 
         try {
             successful = locatorInterface.commitLocators(localMachineLocation.getDbLocation());
         }
         catch (final Exception e) {
             e.printStackTrace();
             successful = false;
         }
 
         if (successful) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Committed system table creation on " + localMachineLocation);
         }
         else {
             ErrorHandling.errorNoEvent("Failed to unlock database locator servers after creating the system table.");
         }
 
         return successful;
     }
 
     /**
      * Called when the database is ready to replicate meta-data (i.e. it has created the local H2O meta-data tables at this point.
      *
      * <p>
      * This is called by the database object at the end of startup, so it is limited in what it can do. Anything involving querying the
      * local database may have to be run asynchronously.
      *
      * @param metaDataReplicaManager
      *            The replica manager for this databases meta-data.
      */
     public void setAsReadyToReplicateMetaData(final MetaDataReplicaManager metaDataReplicaManager) {
 
         this.metaDataReplicaManager = metaDataReplicaManager;
     }
 
     /**
      * Set the port on which the local database instance server is being run on.
      * @param databaseInstancePort Port number.
      */
     public void setDatabaseInstanceServerPort(final int databaseInstancePort) {
 
         localMachineLocation.setRMIPort(databaseInstancePort);
     }
 
     @Override
     public int getChordPort() {
 
         return chordNode.getSelfReference().getCachedAddress().getPort();
     }
 }
