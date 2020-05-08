 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved. Project Homepage:
  * http://blogs.cs.st-andrews.ac.uk/h2o H2O is free software: you can redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. H2O
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
  * Public License along with H2O. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.db.manager;
 
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.PriorityQueue;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2.engine.Database;
 import org.h2o.autonomic.decision.ranker.metric.ActionRequest;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.DatabaseInstanceRemote;
 import org.h2o.db.interfaces.TableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.manager.monitorthreads.TableManagerLivenessCheckerThread;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.util.filter.CollectionFilter;
 import org.h2o.util.filter.Predicate;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
public class InMemorySystemTable implements ISystemTable, Remote {
 
     private final Database database;
 
     /**
      * References to every Table Manager in the database system.
      * 
      * <p>
      * <ul>
      * <li>Key: Full table name (incl. schema name)</li>
      * <li>Value: reference to the table's Table Manager</li>
      * </ul>
      */
     private Map<TableInfo, TableManagerWrapper> tableManagers;
 
     /**
      * Where replicas for table manager state are stored in the database system.
      * 
      * <p>
      * <ul>
      * <li>Key: Full table name (inc. schema name)</li>
      * <li>Value: reference to the location of a table manager state replica for that table.</li>
      * </ul>
      */
     private Map<TableInfo, Set<DatabaseURL>> tmReplicaLocations;
 
     private Map<DatabaseURL, DatabaseInstanceWrapper> databasesInSystem = new HashMap<DatabaseURL, DatabaseInstanceWrapper>();
 
     /**
      * The next valid table set number which can be assigned by the System Table.
      */
     private int tableSetNumber = 1;
 
     private Map<TableInfo, DatabaseURL> primaryLocations;
 
     /**
      * A thread which periodically checks that Table Managers are still alive.
      */
     private final TableManagerLivenessCheckerThread tableManagerPingerThread;
 
     private boolean started = false;
 
     /**
      * Maintained because RMI registry uses weak references so it's possible for otherwise unreferenced
      * exposed objects to be garbage collected.
      * 
      * http://download.oracle.com/javase/6/docs/platform/rmi/spec/rmi-arch4.html
      * 
      * See http://stackoverflow.com/questions/645208/java-rmi-nosuchobjectexception-no-such-object-in-table/854097#854097.
      */
     public final static HashSet<TableManagerRemote> tableManagerReferences = new HashSet<TableManagerRemote>();
 
     public InMemorySystemTable(final Database database) throws Exception {
 
         this.database = database;
         databasesInSystem = Collections.synchronizedMap(new HashMap<DatabaseURL, DatabaseInstanceWrapper>());
         tableManagers = Collections.synchronizedMap(new HashMap<TableInfo, TableManagerWrapper>());
         tmReplicaLocations = Collections.synchronizedMap(new HashMap<TableInfo, Set<DatabaseURL>>());
 
         primaryLocations = new HashMap<TableInfo, DatabaseURL>();
 
         final int replicationThreadSleepTime = Integer.parseInt(database.getDatabaseSettings().get("TABLE_MANAGER_LIVENESS_CHECKER_THREAD_SLEEP_TIME"));
 
         tableManagerPingerThread = new TableManagerLivenessCheckerThread(this, replicationThreadSleepTime);
         tableManagerPingerThread.setName("TableManagerLivenessCheckerThread");
         tableManagerPingerThread.start();
 
         started = true;
 
         H2OEventBus.publish(new H2OEvent(database.getURL().getURL(), DatabaseStates.SYSTEM_TABLE_CREATION));
     }
 
     /******************************************************************
      **** Methods which involve updating the System Table's state.
      ******************************************************************/
 
     @Override
     public boolean addTableInformation(final TableManagerRemote tableManager, final TableInfo tableDetails, final Set<DatabaseInstanceWrapper> replicaLocations) throws RemoteException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "New table successfully created: " + tableDetails);
 
         final TableInfo basicTableInfo = tableDetails.getGenericTableInfo();
 
         final TableManagerWrapper tableManagerWrapper = new TableManagerWrapper(basicTableInfo, tableManager, tableDetails.getURL());
 
         if (tableManagers.containsKey(basicTableInfo)) {
             ErrorHandling.errorNoEvent("Table " + tableDetails + " already exists.");
             return false; // this table already exists.
         }
 
         tableManagerReferences.add(tableManager);
         tableManagers.put(basicTableInfo, tableManagerWrapper);
 
         primaryLocations.put(basicTableInfo, tableDetails.getURL());
 
         Set<DatabaseURL> replicas = tmReplicaLocations.get(basicTableInfo);
 
         if (replicas == null) {
             replicas = new HashSet<DatabaseURL>();
         }
 
         for (final DatabaseInstanceWrapper wrapper : replicaLocations) {
             replicas.add(wrapper.getURL());
         }
 
         tmReplicaLocations.put(basicTableInfo, replicas);
 
         return true;
     }
 
     @Override
     public boolean removeTableInformation(final TableInfo ti) throws RemoteException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Request to completely drop table '" + ti.getFullTableName() + "' from the system.");
 
         final Set<TableInfo> toRemove = new HashSet<TableInfo>();
 
         if (ti.getTableName() == null) {
             /*
              * Drop the entire schema.
              */
 
             for (final TableInfo info : tableManagers.keySet()) {
                 if (info.getSchemaName().equals(ti.getSchemaName())) {
                     toRemove.add(info);
                 }
             }
 
             for (final TableInfo key : toRemove) {
                 final TableManagerWrapper tmw = tableManagers.remove(key);
 
                 setTableManagerAsShutdown(tmw);
             }
 
         }
         else { // Just remove the single table.
 
             final TableManagerWrapper tmw = tableManagers.remove(ti.getGenericTableInfo());
             setTableManagerAsShutdown(tmw);
         }
 
         return true;
     }
 
     /**
      * Specify that the Table Manager is no longer in use. This ensures that if any remote instances have cached references of the manager,
      * they will become aware that it is no longer active.
      * 
      * @param tmw
      * @throws RemoteException
      */
     private void setTableManagerAsShutdown(final TableManagerWrapper tmw) throws RemoteException {
 
         if (tmw.getTableManager() != null) {
             try {
                 tmw.getTableManager().shutdown(true);
             }
             catch (final MovedException e) {
                 // This should never happen - the System Table should always
                 // know the current location.
                 e.printStackTrace();
             }
         }
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.ISystemTable#addConnectionInformation(org.h2.h2o.util.DatabaseURL )
      */
     @Override
     public int addConnectionInformation(final DatabaseURL databaseURL, final DatabaseInstanceWrapper databaseInstanceRemote) throws RemoteException {
 
         databasesInSystem.remove(databaseURL);
         databasesInSystem.put(databaseURL, databaseInstanceRemote);
 
         return 1;
     }
 
     // /* (non-Javadoc)
     // * @see
     // org.h2.h2o.ISystemTable#addReplicaInformation(org.h2.h2o.TableInfo)
     // */
     // @Override
     // public void addReplicaInformation(TableInfo ti) throws RemoteException {
     //
     // Set<TableInfo> replicas = replicaLocations.get(ti.getFullTableName());
     //
     // if (replicas == null){
     // replicas = new HashSet<TableInfo>();
     // }
     //
     // replicas.add(ti);
     //
     // replicaLocations.put(ti.getFullTableName(), replicas);
     // }
     //
     // /* (non-Javadoc)
     // * @see org.h2.h2o.ISystemTable#removeReplica(java.lang.String,
     // org.h2.h2o.TableInfo)
     // */
     // @Override
     // public void removeReplicaInformation(TableInfo ti) throws RemoteException
     // {
     // Diagnostic.traceNoEvent(DiagnosticLevel.FINAL,
     // "Request to drop a single replica of '" + ti.getFullTableName() +
     // "' from the system.");
     //
     // Set<TableInfo> replicas = replicaLocations.get(ti.getFullTableName());
     //
     // if (replicas == null){
     // return;
     // }
     //
     // replicas.remove(ti);
     //
     // }
 
     /******************************************************************
      **** Methods which involve querying the System Table.
      ******************************************************************/
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.ISystemTable#lookup(java.lang.String)
      */
     @Override
     public TableManagerWrapper lookup(TableInfo ti) throws RemoteException {
 
         if (ti == null) { throw new RemoteException("The table information provided was null."); }
 
         ti = ti.getGenericTableInfo();
         TableManagerWrapper tableManagerWrapper = tableManagers.get(ti);
         TableManagerRemote tm = null;
 
         if (tableManagerWrapper != null) {
             tm = tableManagerWrapper.getTableManager();
         }
         /*
          * If there is a null reference to a Table Manager we can try to reinstantiate it, but if there is no reference at all just return
          * null for the lookup.
          */
         final boolean containsTableManager = tableManagers.containsKey(ti);
         if (tm != null || !containsTableManager) {
             if (!containsTableManager) { return null; }
 
             return tableManagerWrapper;
         }
 
         /*
          * The DM reference is null so we must look to create a new DM. XXX is it possible that a data manager is running and the SM doesn't
          * know of it?
          */
 
         if (tableManagerWrapper != null && database.getURL().equals(tableManagerWrapper.getURL())) {
             /*
              * It is okay to re-instantiate the Table Manager here.
              */
             // TableManager dm =
             // TableManager.createTableManagerFromPersistentStore(ti.getSchemaName(),
             // ti.getSchemaName());
             try {
                 tm = new TableManager(ti, database);
                 tm.recreateReplicaManagerState(tableManagerWrapper.getURL().sanitizedLocation());
                 H2OEventBus.publish(new H2OEvent(database.getURL().getURL(), DatabaseStates.TABLE_MANAGER_CREATION, ti.getFullTableName()));
 
             }
             catch (final SQLException e) {
                 e.printStackTrace();
             }
             catch (final Exception e) {
                 e.printStackTrace();
             }
 
             /*
              * Make Table Manager serializable first.
              */
             try {
                 tm = (TableManagerRemote) UnicastRemoteObject.exportObject(tm, 0);
             }
             catch (final RemoteException e) {
                 e.printStackTrace();
             }
 
             database.getChordInterface().bind(ti.getFullTableName(), tm);
 
         }
         else if (tableManagerWrapper != null) {
             // Try to create the data manager at whereever it is meant to be. It
             // may already be active.
             // RECREATE TABLEMANAGER <tableName>
             try {
                 DatabaseInstanceRemote dir = getDatabaseInstance(tableManagerWrapper.getURL());
                 final DatabaseURL url = tableManagerWrapper.getURL();
                 ti = tableManagerWrapper.getTableInfo();
                 if (dir != null) {
                     dir.executeUpdate("RECREATE TABLEMANAGER " + ti.getFullTableName() + " FROM '" + url.sanitizedLocation() + "';", false);
                 }
                 else {
                     // Remove location we know isn't active, then try to
                     // instantiate the table manager elsewhere.
                     final Set<DatabaseURL> replicaLocations = tmReplicaLocations.get(tableManagerWrapper.getTableInfo());
                     replicaLocations.remove(tableManagerWrapper.getURL());
 
                     for (final DatabaseURL replicaLocation : replicaLocations) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Attempting to recreate table manager for " + tableManagerWrapper.getTableInfo() + " on " + replicaLocation);
 
                         dir = getDatabaseInstance(replicaLocation);
                         if (dir != null) {
                             dir.executeUpdate("RECREATE TABLEMANAGER " + ti.getFullTableName() + " FROM '" + url.sanitizedLocation() + "';", false);
                         }
                     }
                 }
             }
             catch (final SQLException e) {
                 e.printStackTrace();
             }
             catch (final MovedException e) {
                 e.printStackTrace();
             }
 
             tableManagerWrapper = tableManagers.get(ti);
             tm = tableManagerWrapper.getTableManager();
 
         }
         else {
             // Table Manager location is not known.
             ErrorHandling.errorNoEvent("Couldn't find the location of the table manager for table " + ti + ". This should never happen - the relevant information" + " should be found in persisted state.");
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, ti.getFullTableName() + "'s table manager has been recreated on " + tableManagerWrapper.getURL() + ".");
 
         tableManagerWrapper.setTableManager(tm);
         tableManagers.put(ti, tableManagerWrapper);
 
         return tableManagerWrapper;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.ISystemTable#exists(java.lang.String)
      */
     @Override
     public boolean exists(final TableInfo ti) throws RemoteException {
 
         return tableManagers.containsKey(ti);
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.ISystemTable#getAllTablesInSchema(java.lang.String)
      */
     @Override
     public Set<String> getAllTablesInSchema(final String schemaName) throws RemoteException {
 
         final Set<String> tableNames = new HashSet<String>();
 
         for (final TableInfo ti : tableManagers.keySet()) {
             if (ti.getSchemaName().equals(schemaName)) {
                 tableNames.add(ti.getFullTableName());
             }
         }
 
         return tableNames;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.ISystemTable#getNewTableSetNumber()
      */
     @Override
     public int getNewTableSetNumber() throws RemoteException {
 
         return tableSetNumber++;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#buildSystemTableState(org.h2.h2o.manager .ISystemTable)
      */
     @Override
     public void buildSystemTableState(final ISystemTable otherSystemTable) throws RemoteException, MovedException, SQLException {
 
         started = false;
         /*
          * Obtain references to connected machines.
          */
         final Map<DatabaseURL, DatabaseInstanceWrapper> connectedMachines = otherSystemTable.getConnectionInformation();
 
         databasesInSystem = new HashMap<DatabaseURL, DatabaseInstanceWrapper>();
 
         // Make sure this contains remote references for each URL
 
         for (final Entry<DatabaseURL, DatabaseInstanceWrapper> remoteDB : connectedMachines.entrySet()) {
             final DatabaseInstanceWrapper wrapper = remoteDB.getValue();
 
             DatabaseInstanceRemote dir = null;
 
             if (wrapper != null) {
                 wrapper.getDatabaseInstance();
             }
 
             boolean active = remoteDB.getValue() == null ? true : remoteDB.getValue().isActive();
 
             if (dir == null) {
                 if (remoteDB.getKey().equals(database.getURL())) {
                     // Local machine.
                     dir = database.getLocalDatabaseInstance();
                 }
                 else {
                     // Look for a remote reference.
                     try {
                         dir = database.getRemoteInterface().getDatabaseInstanceAt(remoteDB.getKey());
 
                         if (dir != null) {
                             active = true;
                         }
                     }
                     catch (final Exception e) {
                         // Couldn't find reference to this database instance.
                         active = false;
                     }
                 }
 
             }
 
             databasesInSystem.put(remoteDB.getKey(), new DatabaseInstanceWrapper(remoteDB.getKey(), dir, active));
         }
 
         /*
          * Obtain references to Table Managers, though not necessarily references to active TM proxies.
          */
         tableManagers = otherSystemTable.getTableManagers();
 
         tmReplicaLocations = otherSystemTable.getReplicaLocations();
         primaryLocations = otherSystemTable.getPrimaryLocations();
 
         /*
          * At this point some of the Table Manager references will be null if the Table Managers could not be found at their old location.
          * BUT, a new Table Manager cannot be created at this point because it would require contact with the System Table, which is not yet
          * active.
          */
 
         started = true;
 
     }
 
     public void removeTableManagerCheckerThread() {
 
         tableManagerPingerThread.setRunning(false);
     }
 
     /**
      * Check that Table Managers are still alive.
      * 
      * @return
      */
     @Override
     public boolean checkTableManagerAccessibility() {
 
         boolean anyTableManagerRecreated = false;
         if (started) {
 
             // Note: done this way to avoid concurrent modification exceptions
             // when a table manager entry is updated.
             final TableManagerWrapper[] tableManagerArray = tableManagers.values().toArray(new TableManagerWrapper[0]);
             for (final TableManagerWrapper tableManagerWrapper : tableManagerArray) {
                 final boolean thisTableManagerRecreated = recreateTableManagerIfNotAlive(tableManagerWrapper);
 
                 if (thisTableManagerRecreated) {
                     anyTableManagerRecreated = true;
                 }
             }
 
         }
         return anyTableManagerRecreated;
     }
 
     /**
      * Checks whether a table manager is currently active.
      * 
      * @param tableManager
      * @return
      */
     private static boolean isAlive(final TableManagerRemote tableManager) {
 
         boolean alive = true;
 
         if (tableManager == null) {
             alive = false;
         }
         else {
             try {
                 tableManager.checkConnection();
             }
             catch (final Exception e) {
                 alive = false;
             }
         }
         return alive;
     }
 
     @Override
     public TableManagerRemote recreateTableManager(final TableInfo tableInfo) {
 
         final TableManagerWrapper tableManager = tableManagers.get(tableInfo);
 
         recreateTableManagerIfNotAlive(tableManager);
 
         return tableManagers.get(tableInfo).getTableManager();
     }
 
     public synchronized boolean recreateTableManagerIfNotAlive(final TableManagerWrapper tableManagerWrapper) {
 
         if (isAlive(tableManagerWrapper.getTableManager())) { return false; // check that it isn't already active.
         }
 
         for (final DatabaseURL replicaLocation : tmReplicaLocations.get(tableManagerWrapper.getTableInfo())) {
             try {
                 final DatabaseInstanceWrapper instance = databasesInSystem.get(replicaLocation);
 
                 if (instance != null && instance.getDatabaseInstance() != null) {
                     final boolean success = instance.getDatabaseInstance().recreateTableManager(tableManagerWrapper.getTableInfo(), tableManagerWrapper.getURL());
 
                     if (success) {
                         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Table Manager for " + tableManagerWrapper.getTableInfo() + " recreated on " + instance.getURL());
 
                         return true;
                     }
                 }
                 else {
                     if (instance != null) {
                         instance.setActive(false);
                     }
                 }
             }
             catch (final RemoteException e) {
                 // May fail on some nodes.
 
                 // TODO mark these instances as inactive.
             }
         }
 
         ErrorHandling.errorNoEvent("Failed to recreate Table Manager for " + tableManagerWrapper.getTableInfo() + ". There were " + tmReplicaLocations.get(tableManagerWrapper.getTableInfo()).size() + " replicas available (including the failed machine) at "
                         + PrettyPrinter.toString(tmReplicaLocations.get(tableManagerWrapper.getTableInfo())) + ".");
         return false;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getConnectionInformation()
      */
     @Override
     public Map<DatabaseURL, DatabaseInstanceWrapper> getConnectionInformation() throws RemoteException {
 
         return databasesInSystem;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getTableManagers()
      */
     @Override
     public Map<TableInfo, TableManagerWrapper> getTableManagers() {
 
         return tableManagers;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getReplicaLocations()
      */
     @Override
     public Map<TableInfo, Set<DatabaseURL>> getReplicaLocations() {
 
         return tmReplicaLocations;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#buildSystemTableState()
      */
     @Override
     public void buildSystemTableState() throws RemoteException {
 
         // TODO Auto-generated method stub
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#removeAllTableInformation()
      */
     @Override
     public void removeAllTableInformation() throws RemoteException {
 
         for (final TableManagerWrapper dmw : tableManagers.values()) {
             try {
 
                 TableManagerRemote dm = null;
 
                 if (dmw != null) {
                     dm = dmw.getTableManager();
                 }
 
                 if (dm != null) {
                     dm.remove(true);
 
                     UnicastRemoteObject.unexportObject(dm, true);
                 }
             }
             catch (final Exception e) {
             }
         }
 
         tableManagers.clear();
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getDatabaseInstance(org.h2.h2o.util. DatabaseURL)
      */
     @Override
     public DatabaseInstanceRemote getDatabaseInstance(final DatabaseURL databaseURL) throws RemoteException, MovedException {
 
         final DatabaseInstanceWrapper wrapper = databasesInSystem.get(databaseURL);
         if (wrapper == null) { return null; }
         return wrapper.getDatabaseInstance();
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getDatabaseInstances()
      */
     @Override
     public Set<DatabaseInstanceWrapper> getDatabaseInstances() throws RemoteException, MovedException {
 
         return new HashSet<DatabaseInstanceWrapper>(databasesInSystem.values());
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#removeDatabaseInstance(org.h2.h2o.comms .remote.DatabaseInstanceRemote)
      */
     @Override
     public void removeConnectionInformation(final DatabaseInstanceRemote localDatabaseInstance) throws RemoteException, MovedException {
 
         final DatabaseInstanceWrapper wrapper = databasesInSystem.get(localDatabaseInstance.getURL());
 
         assert wrapper != null;
 
         wrapper.setActive(false);
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#changeTableManagerLocation(org.h2.h2o .comms.remote.TableManagerRemote)
      */
     @Override
     public void changeTableManagerLocation(final TableManagerRemote stub, final TableInfo tableInfo) {
 
         final Object result = tableManagers.remove(tableInfo.getGenericTableInfo());
 
         if (result == null) {
             ErrorHandling.errorNoEvent("There is an inconsistency in the storage of Table Managers which has caused inconsistencies in the set of managers.");
             assert false;
         }
 
         final TableManagerWrapper dmw = new TableManagerWrapper(tableInfo, stub, tableInfo.getURL());
 
         tableManagers.put(tableInfo.getGenericTableInfo(), dmw);
         tableManagerReferences.add(stub);
         database.getChordInterface().bind(tableInfo.getFullTableName(), stub);
 
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#getLocalDatabaseInstances(org.h2.h2o. util.DatabaseURL)
      */
     @Override
     public Set<TableManagerWrapper> getLocalDatabaseInstances(final DatabaseURL databaseInstance) throws RemoteException, MovedException {
 
         /*
          * Create an interator to go through and chec whether a given Table Manager is local to the specified machine.
          */
         final Predicate<TableManagerWrapper, DatabaseURL> isLocal = new Predicate<TableManagerWrapper, DatabaseURL>() {
 
             @Override
             public boolean apply(final TableManagerWrapper wrapper, final DatabaseURL databaseInstance) {
 
                 try {
                     return wrapper.isLocalTo(databaseInstance);
                 }
                 catch (final RemoteException e) {
                     return false;
                 }
             }
 
         };
 
         final Set<TableManagerWrapper> localManagers = CollectionFilter.filter(tableManagers.values(), isLocal, databaseInstance);
 
         return localManagers;
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#addTableManagerStateReplica(org.h2.h2o .util.TableInfo, org.h2.h2o.util.DatabaseURL)
      */
     @Override
     public void addTableManagerStateReplica(final TableInfo table, final DatabaseURL replicaLocation, final DatabaseURL primaryLocation, final boolean active) throws RemoteException, MovedException {
 
         Set<DatabaseURL> replicas = tmReplicaLocations.get(table.getGenericTableInfo());
 
         primaryLocations.put(table.getGenericTableInfo(), primaryLocation);
 
         if (replicas == null) {
             replicas = new HashSet<DatabaseURL>();
         }
 
         replicas.add(replicaLocation);
 
         tmReplicaLocations.put(table.getGenericTableInfo(), replicas);
 
     }
 
     /*
      * (non-Javadoc)
      * @see org.h2.h2o.manager.ISystemTable#removeTableManagerStateReplica(org.h2 .h2o.util.TableInfo, org.h2.h2o.util.DatabaseURL)
      */
     @Override
     public void removeTableManagerStateReplica(final TableInfo table, final DatabaseURL replicaLocation) throws RemoteException, MovedException {
 
         final Set<DatabaseURL> replicas = tmReplicaLocations.get(table.getGenericTableInfo());
 
         if (replicas == null) {
             ErrorHandling.errorNoEvent("Failed to remove Table Manager Replica state for a replica because it wasn't recorded. Table " + table + ".");
         }
 
         final boolean removed = replicas.remove(replicaLocation);
 
         if (!removed) {
             ErrorHandling.errorNoEvent("Failed to remove Table Manager Replica state for a replica because it wasn't recorded. Table " + table + " at " + replicaLocation);
         }
 
     }
 
     @Override
     public Map<TableInfo, DatabaseURL> getPrimaryLocations() {
 
         return primaryLocations;
     }
 
     @Override
     public Queue<DatabaseInstanceWrapper> getAvailableMachines(final ActionRequest typeOfRequest) {
 
         final Queue<DatabaseInstanceWrapper> sortedMachines = new PriorityQueue<DatabaseInstanceWrapper>();
 
         // TODO make use of action request.
         try {
             sortedMachines.addAll(getDatabaseInstances());
         }
         catch (final Exception e) {
             // Local call - won't happen.
         }
 
         return sortedMachines;
     }
 
 }
