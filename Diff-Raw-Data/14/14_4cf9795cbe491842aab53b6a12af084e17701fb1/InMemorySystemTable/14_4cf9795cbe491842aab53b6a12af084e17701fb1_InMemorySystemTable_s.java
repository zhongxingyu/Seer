 /*
  * Copyright (C) 2009-2010 School of Computer Science, University of St Andrews. All rights reserved. Project Homepage:
  * http://blogs.cs.st-andrews.ac.uk/h2o H2O is free software: you can redistribute it and/or modify it under the terms of the GNU General
  * Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version. H2O
  * is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details. You should have received a copy of the GNU General
  * Public License along with H2O. If not, see <http://www.gnu.org/licenses/>.
  */
 package org.h2o.db.manager;
 
 import java.sql.SQLException;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Set;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.h2.engine.Database;
 import org.h2o.db.DatabaseInstanceProxy;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.manager.monitorthreads.TableManagerLivenessCheckerThread;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.db.wrappers.TableManagerWrapper;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.util.filter.CollectionFilter;
 import org.h2o.util.filter.PredicateWithParameter;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 public final class InMemorySystemTable implements ISystemTable {
 
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
     private Map<TableInfo, Set<DatabaseID>> tmReplicaLocations;
 
     private Map<DatabaseID, DatabaseInstanceWrapper> databasesInSystem = new HashMap<DatabaseID, DatabaseInstanceWrapper>();
 
     /**
      * The next valid table set number which can be assigned by the System Table.
      */
     private int tableSetNumber = 1;
 
     /**
      * Where table managers are located.
      * XXX this duplicates the {@link #tableManagers} field, so it should probably be removed to prevent inconsistencies.
      */
     private Map<TableInfo, DatabaseID> primaryLocations;
 
     /**
      * A thread which periodically checks that Table Managers are still alive.
      */
     private final TableManagerLivenessCheckerThread tableManagerPingerThread;
 
     /**
      * The set of table managers that are currently being recreated via the {@link #recreateTableManagerIfNotAlive(TableManagerWrapper)} method. This prevents
      * concurrent calls to this method attempting to recreate the same table manager multiple times.
      */
     private final Set<TableManagerWrapper> managersBeingRecreated = Collections.synchronizedSet(new HashSet<TableManagerWrapper>());
 
     private boolean started = false;
 
     private final Set<DatabaseInstanceWrapper> noReplicateInstances = new HashSet<DatabaseInstanceWrapper>();
 
     public InMemorySystemTable(final Database database) throws Exception {
 
         this.database = database;
         databasesInSystem = Collections.synchronizedMap(new HashMap<DatabaseID, DatabaseInstanceWrapper>());
         tableManagers = Collections.synchronizedMap(new HashMap<TableInfo, TableManagerWrapper>());
         tmReplicaLocations = Collections.synchronizedMap(new HashMap<TableInfo, Set<DatabaseID>>());
 
         primaryLocations = new HashMap<TableInfo, DatabaseID>();
 
         final int replicationThreadSleepTime = Integer.parseInt(database.getDatabaseSettings().get("TABLE_MANAGER_LIVENESS_CHECKER_THREAD_SLEEP_TIME"));
 
         tableManagerPingerThread = new TableManagerLivenessCheckerThread(this, replicationThreadSleepTime);
         tableManagerPingerThread.setName("TableManagerLivenessCheckerThread");
         tableManagerPingerThread.start();
 
         started = true;
 
         H2OEventBus.publish(new H2OEvent(database.getID().getURL(), DatabaseStates.SYSTEM_TABLE_CREATION));
     }
 
     /******************************************************************
      **** Methods which involve updating the System Table's state.
      ******************************************************************/
 
     @Override
     public boolean addTableInformation(final ITableManagerRemote tableManager, final TableInfo tableDetails, final Set<DatabaseInstanceWrapper> replicaLocations) throws RPCException {
 
         final TableInfo basicTableInfo = tableDetails.getGenericTableInfo();
 
         final TableManagerWrapper tableManagerWrapper = new TableManagerWrapper(basicTableInfo, tableManager, tableDetails.getDatabaseID());
 
         //        if (tableManagers.containsKey(basicTableInfo)) {
         //            ErrorHandling.errorNoEvent("Table " + tableDetails + " already exists.");
         //            return false; // this table already exists.
         //        }
 
         tableManagers.put(basicTableInfo, tableManagerWrapper);
 
         primaryLocations.put(basicTableInfo, tableDetails.getDatabaseID());
 
         Set<DatabaseID> replicas = tmReplicaLocations.get(basicTableInfo);
 
         if (replicas == null) {
             replicas = new HashSet<DatabaseID>();
         }
 
         for (final DatabaseInstanceWrapper wrapper : replicaLocations) {
             replicas.add(wrapper.getURL());
         }
 
         tmReplicaLocations.put(basicTableInfo, replicas);
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "New table successfully created: " + tableDetails);
 
         return true;
     }
 
     @Override
     public boolean removeTableInformation(final TableInfo ti) throws RPCException {
 
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
      * @throws RPCException
      */
     private void setTableManagerAsShutdown(final TableManagerWrapper tmw) throws RPCException {
 
         if (tmw != null && tmw.getTableManager() != null) {
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
 
     @Override
     public int addConnectionInformation(final DatabaseID databaseURL, final DatabaseInstanceWrapper databaseInstanceRemote) throws RPCException {
 
         databasesInSystem.remove(databaseURL);
         databasesInSystem.put(databaseURL, databaseInstanceRemote);
 
         informTableManagersOfMachineStartupOrFailure(null);
 
         return 1;
     }
 
     /******************************************************************
      **** Methods which involve querying the System Table.
      ******************************************************************/
 
     @Override
     public TableManagerWrapper lookup(TableInfo ti) throws RPCException {
 
         if (ti == null) { throw new RPCException("The table information provided was null."); }
 
         ti = ti.getGenericTableInfo();
         TableManagerWrapper tableManagerWrapper = tableManagers.get(ti);
         ITableManagerRemote tm = null;
 
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
 
         if (tableManagerWrapper != null && database.getID().equals(tableManagerWrapper.getURL())) {
             /*
              * It is okay to re-instantiate the Table Manager here.
              */
             try {
                 tm = new TableManager(ti, database, true);
                 tm.recreateReplicaManagerState(tableManagerWrapper.getURL().sanitizedLocation());
                 H2OEventBus.publish(new H2OEvent(database.getID().getURL(), DatabaseStates.TABLE_MANAGER_CREATION, ti.getFullTableName()));
 
             }
             catch (final SQLException e) {
                 e.printStackTrace();
             }
             catch (final Exception e) {
                 e.printStackTrace();
             }
 
             database.getTableManagerServer().exportObject(tm);
 
         }
         else if (tableManagerWrapper != null) {
             // Try to create the data manager at whereever it is meant to be. It
             // may already be active.
             // RECREATE TABLEMANAGER <tableName>
             try {
                 IDatabaseInstanceRemote dir = getDatabaseInstance(tableManagerWrapper.getURL());
                 final DatabaseID url = tableManagerWrapper.getURL();
                 ti = tableManagerWrapper.getTableInfo();
                 if (dir != null) {
                     dir.executeUpdate("RECREATE TABLEMANAGER " + ti.getFullTableName() + " FROM '" + url.sanitizedLocation() + "';", false);
                 }
                 else {
                     // Remove location we know isn't active, then try to
                     // instantiate the table manager elsewhere.
                     final Set<DatabaseID> replicaLocations = tmReplicaLocations.get(tableManagerWrapper.getTableInfo());
                     replicaLocations.remove(tableManagerWrapper.getURL());
 
                     for (final DatabaseID replicaLocation : replicaLocations) {
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
 
     @Override
     public boolean exists(final TableInfo ti) throws RPCException {
 
         return tableManagers.containsKey(ti);
     }
 
     @Override
     public Set<String> getAllTablesInSchema(final String schemaName) throws RPCException {
 
         final Set<String> tableNames = new HashSet<String>();
 
         for (final TableInfo ti : tableManagers.keySet()) {
             if (ti.getSchemaName().equals(schemaName)) {
                 tableNames.add(ti.getFullTableName());
             }
         }
 
         return tableNames;
     }
 
     @Override
     public int getNewTableSetNumber() throws RPCException {
 
         return tableSetNumber++;
     }
 
     @Override
     public void recreateSystemTable(final ISystemTable otherSystemTable) throws RPCException, MovedException, SQLException {
 
         started = false;
         /*
          * Obtain references to connected machines.
          */
         final Map<DatabaseID, DatabaseInstanceWrapper> connectedMachines = otherSystemTable.getConnectionInformation();
 
         if (connectedMachines == null || connectedMachines.size() == 0) { throw new RPCException("No connected machines were found in the remote (old) System Table. There should be at least two."); }
 
         databasesInSystem = new HashMap<DatabaseID, DatabaseInstanceWrapper>();
 
         // Make sure this contains remote references for each URL
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Obtaining references to database instances.");
 
         for (final Entry<DatabaseID, DatabaseInstanceWrapper> remoteDB : connectedMachines.entrySet()) {
             final DatabaseInstanceWrapper wrapper = remoteDB.getValue();
 
             IDatabaseInstanceRemote dir = null;
 
             if (wrapper != null) {
                 wrapper.getDatabaseInstance();
             }
 
             boolean active = remoteDB.getValue() == null ? true : remoteDB.getValue().isActive();
             boolean isReplicating = true;
             if (dir == null) {
                 if (remoteDB.getKey().equals(database.getID())) {
                     // Local machine.
                     dir = database.getLocalDatabaseInstance();
                 }
                 else {
                     // Look for a remote reference.
                     try {
 
                         //dir = database.getRemoteInterface().getDatabaseInstanceAt(remoteDB.getKey()); //TODO Replace with method call to call database instance server directly...
                         dir = DatabaseInstanceProxy.getProxy(remoteDB.getKey());
                         if (dir != null) {
                             isReplicating = dir.isReplicating();
                             active = true;
                         }
                     }
                     catch (final Exception e) {
                         // Couldn't find reference to this database instance.
                         active = false;
                     }
                 }
             }
 
             final DatabaseInstanceWrapper da = new DatabaseInstanceWrapper(remoteDB.getKey(), dir, active);
             databasesInSystem.put(remoteDB.getKey(), da);
 
             if (!isReplicating) {
                 noReplicateInstances.add(da);
             }
 
         }
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Obtaining references to Table Managers.");
         /*
          * Obtain references to Table Managers, though not necessarily references to active TM proxies.
          */
         tableManagers = otherSystemTable.getTableManagers();
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Received Table Managers");
 
         tmReplicaLocations = otherSystemTable.getReplicaLocations();
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Received Replica Locations");
 
         primaryLocations = otherSystemTable.getPrimaryLocations();
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Received Primary Locations");
 
         /*
          * At this point some of the Table Manager references will be null if the Table Managers could not be found at their old location.
          * BUT, a new Table Manager cannot be created at this point because it would require contact with the System Table, which is not yet
          * active.
          */
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "New System Table started.");
 
         started = true;
     }
 
     public void removeTableManagerCheckerThread() {
 
         tableManagerPingerThread.setRunning(false);
     }
 
     /**
      * Check that Table Managers are still alive.
      */
     @Override
     public boolean checkTableManagerAccessibility() {
 
         return checkTableManagerAccessibility(null);
     }
 
     /**
      * Check whether table managers are accessible on the given database instance.
      * @param databaseID if 'null' this will perform a check for all table managers.
      * @return
      */
     public boolean checkTableManagerAccessibility(final DatabaseID databaseID) {
 
         boolean anyTableManagerRecreated = false;
         if (started) {
 
             final TableManagerWrapper[] tableManagerArray = tableManagers.values().toArray(new TableManagerWrapper[0]); // Note: done this way to avoid concurrent modification exceptions when a table manager entry is updated.
             for (final TableManagerWrapper tableManagerWrapper : tableManagerArray) {
                 if (databaseID == null || tableManagerWrapper.isLocalTo(databaseID)) {
                     final boolean thisTableManagerRecreated = recreateTableManagerIfNotAlive(tableManagerWrapper);
 
                     if (thisTableManagerRecreated) {
                         anyTableManagerRecreated = true;
                     }
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
     private static boolean isAlive(final ITableManagerRemote tableManager) {
 
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
     public ITableManagerRemote recreateTableManager(final TableInfo tableInfo) {
 
         final TableManagerWrapper tableManager = tableManagers.get(tableInfo);
 
         final DatabaseID oldLocation = tableManager.getURL();
 
         recreateTableManagerIfNotAlive(tableManager);
 
         try {
             suspectInstanceOfFailure(oldLocation);
         }
         catch (final Exception e) {
             //Won't throw an exception because this is a local call.
             e.printStackTrace();
         }
 
         return tableManagers.get(tableInfo).getTableManager();
     }
 
     public synchronized boolean recreateTableManagerIfNotAlive(final TableManagerWrapper tableManagerWrapper) {
 
         if (isAlive(tableManagerWrapper.getTableManager())) { return false; // check that it isn't already active.
         }
 
         //Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Beginning attempt to recreate Table Manager for " + tableManagerWrapper.getTableInfo() + " on " + database.getID());
 
         if (!managersBeingRecreated.contains(tableManagerWrapper)) { //Prevent this being called in quick succession on a single table manager.
 
             try {
                 managersBeingRecreated.add(tableManagerWrapper);
 
                 final Set<DatabaseID> tableManagerReplicaLocations = tmReplicaLocations.get(tableManagerWrapper.getTableInfo());
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, database.getID() + ": To recreate " + tableManagerWrapper.getTableInfo() + ", replicas exist at " + tableManagerReplicaLocations);
 
                 for (final DatabaseID replicaLocation : tableManagerReplicaLocations) {
 
                     final DatabaseInstanceWrapper instance = databasesInSystem.get(replicaLocation);
 
                     try {
 
                         if (instance != null && instance.getDatabaseInstance() != null) {
                             final boolean success = instance.getDatabaseInstance().recreateTableManager(tableManagerWrapper.getTableInfo(), tableManagerWrapper.getURL());
 
                             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, (success ? "Successfully " : "Unsuccessfully ") + "recreated " + tableManagerWrapper.getTableInfo() + " Table Manager on " + instance.getURL() + ".");
 
                             return success;
                         }
                         else {
                             if (instance != null) {
                                 instance.setActive(false);
                             }
                         }
                     }
                     catch (final RPCException e) {
                         // May fail on some nodes.
                         instance.setActive(false);
                     }
                 }
 
                 ErrorHandling.errorNoEvent(DiagnosticLevel.INIT,
                                 "Failed to recreate Table Manager for " + tableManagerWrapper.getTableInfo() + ". There were " + tableManagerReplicaLocations.size() + " replicas available (including the failed machine) at " + PrettyPrinter.toString(tableManagerReplicaLocations) + ".");
 
             }
             finally {
                 managersBeingRecreated.remove(tableManagerWrapper);
             }
 
         }
         else {
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Will not attempt to recreate the table manager for " + tableManagerWrapper.getTableInfo() + ", because it is being recreated elsewhere.");
         }
 
         return false;
     }
 
     @Override
     public Map<DatabaseID, DatabaseInstanceWrapper> getConnectionInformation() throws RPCException {
 
         return databasesInSystem;
     }
 
     @Override
     public Map<TableInfo, TableManagerWrapper> getTableManagers() {
 
         return tableManagers;
     }
 
     @Override
     public Map<TableInfo, Set<DatabaseID>> getReplicaLocations() {
 
         return tmReplicaLocations;
     }
 
     @Override
     public void recreateInMemorySystemTableFromLocalPersistedState() throws RPCException {
 
         // TODO Auto-generated method stub
     }
 
     @Override
     public void removeAllTableInformation() throws RPCException {
 
         for (final TableManagerWrapper dmw : tableManagers.values()) {
             try {
 
                 ITableManagerRemote dm = null;
 
                 if (dmw != null) {
                     dm = dmw.getTableManager();
                 }
 
                 if (dm != null) {
                     dm.remove(true);
 
                 }
             }
             catch (final Exception e) {
             }
         }
 
         tableManagers.clear();
     }
 
     @Override
     public IDatabaseInstanceRemote getDatabaseInstance(final DatabaseID databaseURL) throws RPCException, MovedException {
 
         final DatabaseInstanceWrapper wrapper = databasesInSystem.get(databaseURL);
         if (wrapper == null) { return null; }
         return wrapper.getDatabaseInstance();
     }
 
     @Override
     public Set<DatabaseInstanceWrapper> getDatabaseInstances() throws RPCException, MovedException {
 
         return new HashSet<DatabaseInstanceWrapper>(databasesInSystem.values());
     }
 
     @Override
     public void removeConnectionInformation(final IDatabaseInstanceRemote localDatabaseInstance) throws RPCException, MovedException {
 
         final DatabaseInstanceWrapper wrapper = databasesInSystem.get(localDatabaseInstance.getURL());
 
         assert wrapper != null;
 
         wrapper.setActive(false);
     }
 
     @Override
     public void changeTableManagerLocation(final ITableManagerRemote stub, final TableInfo tableInfo) {
 
         final Object result = tableManagers.remove(tableInfo.getGenericTableInfo());
 
         if (result == null) {
             ErrorHandling.errorNoEvent("There is an inconsistency in the storage of Table Managers which has caused inconsistencies in the set of managers.");
             assert false;
         }
 
         final TableManagerWrapper tableManagerWrapper = new TableManagerWrapper(tableInfo, stub, tableInfo.getDatabaseID());
 
         tableManagers.put(tableInfo.getGenericTableInfo(), tableManagerWrapper);
     }
 
     @Override
     public Set<TableManagerWrapper> getLocalTableManagers(final DatabaseID databaseInstance) throws RPCException, MovedException {
 
         /*
          * Create an interator to go through and check whether a given Table Manager is local to the specified machine.
          */
         final PredicateWithParameter<TableManagerWrapper, DatabaseID> isLocal = new PredicateWithParameter<TableManagerWrapper, DatabaseID>() {
 
             @Override
             public boolean apply(final TableManagerWrapper wrapper, final DatabaseID databaseInstance) {
 
                 return wrapper.isLocalTo(databaseInstance);
             }
         };
 
         final Set<TableManagerWrapper> localManagers = CollectionFilter.filter(tableManagers.values(), isLocal, databaseInstance);
 
         return localManagers;
     }
 
     @Override
     public void addTableManagerStateReplica(final TableInfo table, final DatabaseID replicaLocation, final DatabaseID primaryLocation, final boolean active) throws RPCException, MovedException {
 
         Set<DatabaseID> replicas = tmReplicaLocations.get(table.getGenericTableInfo());
 
         primaryLocations.put(table.getGenericTableInfo(), primaryLocation);
 
         if (replicas == null) {
             replicas = new HashSet<DatabaseID>();
         }
 
         replicas.add(replicaLocation);
 
         tmReplicaLocations.put(table.getGenericTableInfo(), replicas);
     }
 
     @Override
     public void removeTableManagerStateReplica(final TableInfo table, final DatabaseID replicaLocation) throws RPCException, MovedException {
 
         final Set<DatabaseID> replicas = tmReplicaLocations.get(table.getGenericTableInfo());
 
         if (replicas == null) {
             ErrorHandling.errorNoEvent("Failed to remove Table Manager Replica state for a replica because it wasn't recorded. Table " + table + ".");
         }
 
         final boolean removed = replicas.remove(replicaLocation);
 
         if (!removed) {
             ErrorHandling.errorNoEvent("Failed to remove Table Manager Replica state for a replica because it wasn't recorded. Table " + table + " at " + replicaLocation);
         }
     }
 
     @Override
     public Map<TableInfo, DatabaseID> getPrimaryLocations() {
 
         return primaryLocations;
     }
 
     @Override
     public void suspectInstanceOfFailure(final DatabaseID suspectedDbURL) throws RPCException, MovedException {
 
         //Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Instance at " + suspectedDbURL + " is suspected of failure. Checking whether this is the case.");
 
         final DatabaseInstanceWrapper suspectedDb = databasesInSystem.get(suspectedDbURL);
 
         if (suspectedDb != null) {
             try {
                 suspectedDb.getDatabaseInstance().isAlive();
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Instance at " + suspectedDbURL + " was suspected of failure, but is still alive.");
 
             }
             catch (final RPCException e) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "The database instance " + suspectedDbURL + " is no longer active. Removing from membership set.");
                 databasesInSystem.remove(suspectedDbURL);
                 checkTableManagerAccessibility(suspectedDbURL);
                 informTableManagersOfMachineStartupOrFailure(suspectedDbURL);
             }
         }
 
     }
 
     /**
      * Send a message to every table manager informing them that a machine (which possibly holds one of their replicas) has failed.
      * @param failedMachine The machine that has failed. null if a new machine has started.
      */
     private void informTableManagersOfMachineStartupOrFailure(final DatabaseID failedMachine) {
 
         final List<NotifyTableManagersOfFailureAsync> asyncRequests = new LinkedList<NotifyTableManagersOfFailureAsync>();
 
         for (final TableManagerWrapper tableManagerWrapper : tableManagers.values()) {
             asyncRequests.add(new NotifyTableManagersOfFailureAsync(tableManagerWrapper.getTableManager(), failedMachine));
 
         }
 
         final ExecutorService exec = Executors.newFixedThreadPool(2);
         while (asyncRequests.size() > 0) {
             System.out.println("job submitted.");
             exec.submit(asyncRequests.remove(0));
         }
         exec.shutdown();
         System.out.println("moving on.");
 
     }
 
     @Override
     public int getCurrentSystemTableReplication() throws RPCException, MovedException {
 
         //Not implemented by the Inmemory System Table.
         return 0;
     }
 
     @Override
     public Set<DatabaseInstanceWrapper> getNoReplicateInstances() {
 
         return noReplicateInstances;
     }
 }
