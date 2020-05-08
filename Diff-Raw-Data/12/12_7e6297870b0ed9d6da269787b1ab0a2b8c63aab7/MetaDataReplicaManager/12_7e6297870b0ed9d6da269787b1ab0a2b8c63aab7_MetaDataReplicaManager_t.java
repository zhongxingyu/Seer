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
 
 package org.h2o.db.replication;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2.command.Command;
 import org.h2.command.Parser;
 import org.h2.engine.Database;
 import org.h2.result.LocalResult;
 import org.h2o.autonomic.numonic.metric.CreateMetaDataReplicaMetric;
 import org.h2o.autonomic.numonic.metric.IMetric;
 import org.h2o.autonomic.numonic.ranking.Requirements;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.manager.PersistentSystemTable;
 import org.h2o.db.manager.TableManager;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.manager.interfaces.ISystemTableReference;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.locator.client.H2OLocatorInterface;
 import org.h2o.util.H2OPropertiesWrapper;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 
 /**
  * One instance of this class exists per database instance. This instance is responsible for managing the replication of meta-data for 
  * H2O processes on this instance - Table Manager and System Table state.
  *
  * @author Angus Macdonald (angus AT cs.st-andrews.ac.uk)
  */
 public class MetaDataReplicaManager {
 
     /*
      * TABLE MANAGER STATE
      */
     /**
      * Manages the location of Table Manager replicas.
      */
     private final ReplicaManager tableManagerReplicas;
 
     /*
      * SYSTEM TABLE STATE.
      */
     /**
      * Managers the location of System Table replicas.
      */
     private final ReplicaManager systemTableReplicas;
 
     /*
      * QUERIES - standard set of queries used to create new replicas
      * and drop old replicas.
      */
     private final String addNewReplicaLocationQuery;
 
     private final String addNewSystemTableQuery;
 
     private final String dropOldTableManagerReplica;
 
     private final String dropOldSystemTableReplica;
 
     /*
      * CONFIGURATION OPTIONS.
      */
     /**
      * Whether the System is replicating meta-data at all.
      */
     private final boolean metaDataReplicationEnabled;
 
     /**
      * The number of replicas required of System Table meta-data.
      */
     private final int systemTableReplicationFactor;
 
     /**
      * The number of replicas required of Table Manager meta-data.
      */
     private final int tableManagerReplicationFactor;
 
     /**
      * The metric used when asking the System Table where a meta data replica should be created.
      */
     private static IMetric createMetaDataReplicaMetric = new CreateMetaDataReplicaMetric();
 
     /*
      * DATABASE STATE.
      */
     private final Parser parser;
 
     /**
      * Location of the local database instance.
      */
     private final DatabaseInstanceWrapper localDatabase;
 
     private final Database db;
 
     public MetaDataReplicaManager(final boolean metaDataReplicationEnabled, final int systemTableReplicationFactor, final int tableManagerReplicationFactor, final DatabaseInstanceWrapper localDatabase, final Database db) {
 
         /*
          * Replica Locations
          */
         tableManagerReplicas = new ReplicaManager();
         systemTableReplicas = new ReplicaManager();
 
         tableManagerReplicas.add(localDatabase);
         systemTableReplicas.add(localDatabase);
 
         /*
          * Configuration options.
          */
         this.metaDataReplicationEnabled = metaDataReplicationEnabled;
         this.systemTableReplicationFactor = systemTableReplicationFactor;
         this.tableManagerReplicationFactor = tableManagerReplicationFactor;
 
         /*
          * Local Machine Details.
          */
         this.db = db;
         this.localDatabase = localDatabase;
         parser = new Parser(db.getSystemSession(), true);
 
         /*
          * Queries.
          */
         final String databaseName = db.getID().sanitizedLocation().toUpperCase();
 
         addNewReplicaLocationQuery = "CREATE REPLICA IF NOT EXISTS " + TableManager.getMetaTableName(databaseName, TableManager.TABLES) + ", " + TableManager.getMetaTableName(databaseName, TableManager.REPLICAS) + ", " + TableManager.getMetaTableName(databaseName, TableManager.CONNECTIONS)
                         + " FROM '" + db.getID().getOriginalURL() + "';";
 
         addNewSystemTableQuery = "CREATE REPLICA IF NOT EXISTS " + PersistentSystemTable.TABLES + ", " + PersistentSystemTable.CONNECTIONS + ", " + PersistentSystemTable.TABLEMANAGERSTATE + " FROM '" + db.getID().getOriginalURL() + "';";
 
         dropOldSystemTableReplica = "DROP REPLICA IF EXISTS " + PersistentSystemTable.TABLES + ", " + PersistentSystemTable.CONNECTIONS + ", " + PersistentSystemTable.TABLEMANAGERSTATE + ";";
 
         dropOldTableManagerReplica = "DROP REPLICA IF EXISTS " + TableManager.getMetaTableName(databaseName, TableManager.TABLES) + ", " + TableManager.getMetaTableName(databaseName, TableManager.CONNECTIONS) + ", " + TableManager.getMetaTableName(databaseName, TableManager.REPLICAS) + ";";
     }
 
     /**
      * Attempts to replicate local meta-data to the machine provided by the parameter.
      */
     public synchronized void replicateMetaDataToRemoteInstance(final ISystemTableReference systemTableRef, final boolean isSystemTable, final DatabaseInstanceWrapper databaseInstance) {
 
         if (isSystemTable && !systemTableRef.isSystemTableLocal()) { return; }
 
         final ReplicaManager replicaManager = isSystemTable ? systemTableReplicas : tableManagerReplicas;
         final int managerStateReplicationFactor = isSystemTable ? systemTableReplicationFactor : tableManagerReplicationFactor;
 
         // Check that replication is enabled and replication factor has not already been reached.
         if (metaDataReplicationEnabled && replicaManager.allReplicasSize() < managerStateReplicationFactor) {
 
             if (!isLocal(databaseInstance) && databaseInstance.isActive()) {
                 try {
                     addReplicaLocation(databaseInstance, isSystemTable);
                 }
                 catch (final RPCException e) {
                     Diagnostic.trace(DiagnosticLevel.FULL, "failed to add replica location: databaseInstance: " + databaseInstance);
                 }
             }
         }
     }
 
     /**
      * Attempts to replicate local meta-data to any available machines, until the desired replication factor is reached.
      * @param systemTableRef    Reference to the System Table.
      * @param isSystemTable     True if System Table state is to be replicated, False if Table Manager state is to be replicated.
      */
     public synchronized void replicateMetaDataIfPossible(final ISystemTableReference systemTableRef, final boolean isSystemTable) {
 
         if (isSystemTable && !systemTableRef.isSystemTableLocal()) { return; }
 
         final ReplicaManager replicaManager = isSystemTable ? systemTableReplicas : tableManagerReplicas;
         final int managerStateReplicationFactor = isSystemTable ? systemTableReplicationFactor : tableManagerReplicationFactor;
 
         // Check that replication is enabled and replication factor has not already been reached.
         if (metaDataReplicationEnabled && replicaManager.allReplicasSize() < managerStateReplicationFactor) {
 
             Queue<DatabaseInstanceWrapper> databaseInstances = null;
 
             try {
                 final ISystemTableMigratable systemTableRemote = systemTableRef.getSystemTable();
 
                 if (systemTableRemote == null) {
                     Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "System table was NULL so the meta-data manager is unable to replicate.");
                     return;
                 }
                 databaseInstances = systemTableRef.getRankedListOfInstances(createMetaDataReplicaMetric, Requirements.NO_FILTERING);
             }
             catch (final Exception e) {
                 Diagnostic.trace(DiagnosticLevel.FULL, "Error trying to contact system table (for the purposes of discovering new instances on which to replicate data). This is not a fatal error. No recovery is attempted.");
                 return; // just return, the system will attempt to replicate later on.
             }
 
             if (databaseInstances.size() > replicaManager.allReplicasSize()) {
                 //Don't display diagnostic message when it isn't possible to replicate further anyway.
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL,
                                 "Does " + (isSystemTable ? "system table" : "table manager") + " meta-data need to be replicated? Currently " + replicaManager.allReplicasSize() + ", and there needs to be " + managerStateReplicationFactor + "(" + databaseInstances.size() + " machines available).");
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Meta-data on " + db.getID() + " currently replicated to : " + PrettyPrinter.toString(replicaManager.getAllReplicas()));
                 Diagnostic.traceNoEvent(DiagnosticLevel.FINAL, "Data can be replicated to the following machines: " + PrettyPrinter.toString(databaseInstances));
             }
 
             if (databaseInstances.size() != 1) {
 
                 for (final DatabaseInstanceWrapper databaseInstance : databaseInstances) {
 
                     if (!isLocal(databaseInstance) && databaseInstance.isActive() && !replicaManager.getAllReplicas().keySet().contains(databaseInstance)) {
                         try {
 
                             addReplicaLocation(databaseInstance, isSystemTable);
 
                             if (replicaManager.allReplicasSize() >= managerStateReplicationFactor) {
                                 break;
                             }
                         }
                         catch (final RPCException e) {
                             // May fail. Try next database.
                         }
                     }
                 }
             }
             else {
                 //Diagnostic.trace(DiagnosticLevel.FULL, "Only one database instance was found (the local one), so no meta-data can be replicated.");
 
             }
         }
     }
 
     /**
      * Replicate meta-tables to the location specified.
      * @param newReplicaLocation    The location on which replicas will be added.
      * @param isSystemTable         True if System Table state is to be replicated, False if Table Manager state is to be replicated.
      * @return  True if the replica was created successfully.
      * @throws RPCException      Thrown if the new replica location couldn't be contacted.
      */
     private boolean addReplicaLocation(final DatabaseInstanceWrapper newReplicaLocation, final boolean isSystemTable) throws RPCException {
 
         if (newReplicaLocation.getURL().equals(db.getID())) { return false; // can't replicate to the local machine
         }
 
         return addReplicaLocation(newReplicaLocation, isSystemTable, 0);
     }
 
     /**
      * Replicate meta-tables to the location specified.
      * 
      * @param newReplicaLocation the location on which replicas will be added
      * @param isSystemTable true if System Table state is to be replicated, false if Table Manager state is to be replicated
      * @param numberOfPreviousAttempts the number of attempts made so far to replicate to this machine. This method will try 5 times to replicate
      * state before giving up.
      * @return true if the replica was created successfully
      * @throws RPCException
      */
     private boolean addReplicaLocation(final DatabaseInstanceWrapper newReplicaLocation, final boolean isSystemTable, final int numberOfPreviousAttempts) throws RPCException {
 
         final ReplicaManager replicaManager = isSystemTable ? systemTableReplicas : tableManagerReplicas;
         final int managerStateReplicationFactor = isSystemTable ? systemTableReplicationFactor : tableManagerReplicationFactor;
 
         final Set<TableInfo> localTableManagers = db.getSystemTableReference().getLocalTableManagers().keySet();
 
         if (metaDataReplicationEnabled) {
             if (replicaManager.allReplicasSize() < managerStateReplicationFactor) {
 
                 // now replica state here.
                 try {
                     /*
                      * Remove existing entries for this System Table / Table Manager.
                      */
                     String deleteOldEntries = null;
                     if (isSystemTable) {
                         deleteOldEntries = dropOldSystemTableReplica;
                     }
                     else if (localTableManagers.size() > 0) {
                         // if there are any local Table Managers clear old meta data on them from remote machines.
                         deleteOldEntries = dropOldTableManagerReplica;
                     }
 
                     if (deleteOldEntries != null) {
                         newReplicaLocation.getDatabaseInstance().executeUpdate(deleteOldEntries, true);
                     }
 
                     /*
                      * Create new replica if needed, then replicate state.
                      */
                     final String createQuery = isSystemTable ? addNewSystemTableQuery : addNewReplicaLocationQuery;
 
                     final int result = newReplicaLocation.getDatabaseInstance().executeUpdate(createQuery, true);
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "H2O " + (isSystemTable ? "System Table" : "Table Manager") + " tables on " + localDatabase.getURL() + " replicated onto new node: " + newReplicaLocation.getURL().getDbLocation() + "[result=" + result + "]");
 
                     publishReplicaCreationToEventBus(newReplicaLocation, isSystemTable);
 
                     replicaManager.add(newReplicaLocation);
 
                     if (isSystemTable) {
                         try {
                             updateLocatorFiles(isSystemTable);
                         }
                         catch (final Exception e) {
                             throw new RPCException(e.getMessage());
                         }
                     }
                     else {
                         for (final TableInfo ti : localTableManagers) {
                             db.getSystemTableReference().getSystemTable().addTableManagerStateReplica(ti, newReplicaLocation.getURL(), localDatabase.getURL(), true);
                         }
                     }
 
                     return true;
                 }
                 catch (final SQLException e) {
                     /*
                      * Usually thrown if the CREATE REPLICA command couldn't connect back to this database. This often happens when the
                      * database has only recently started and hasn't fully initialized, so this code attempts the operation again.
                      */
                     if (numberOfPreviousAttempts < 5) {
                         try {
                             Thread.sleep(10);
                         }
                         catch (final InterruptedException e1) {
                             // Ignore.
                         }
                         return addReplicaLocation(newReplicaLocation, isSystemTable, numberOfPreviousAttempts + 1);
                     }
                     Diagnostic.trace(DiagnosticLevel.FULL, localDatabase.getURL() + " failed to replicate " + (isSystemTable ? "System Table" : "Table Manager") + " state onto: " + newReplicaLocation.getURL().getDbLocation() + ".");
                 }
                 catch (final Exception e) {
                     /*
                      * Usually thrown if this database couldn't connect to the remote instance.
                      */
                     throw new RPCException(e.getMessage());
                 }
             }
         }
         return false;
     }
 
     private void publishReplicaCreationToEventBus(final DatabaseInstanceWrapper newReplicaLocation, final boolean isSystemTable) {
 
         // Publish H2O event: database is the location of the replica. Value is the database it is for.
         if (isSystemTable) {
             H2OEventBus.publish(new H2OEvent(newReplicaLocation.getURL().getURL(), DatabaseStates.META_TABLE_REPLICA_CREATION, "System Table State: " + db.getID().getDbLocation()));
         }
         else {
             H2OEventBus.publish(new H2OEvent(newReplicaLocation.getURL().getURL(), DatabaseStates.META_TABLE_REPLICA_CREATION, "Table Manager State: " + db.getID().getDbLocation()));
         }
     }
 
     public synchronized int executeUpdate(final String query, final boolean isSystemTable, final TableInfo tableInfo) throws SQLException {
 
         // Loop through replicas
         // TODO Parallelise sending of update.
 
         final ReplicaManager replicaManager = isSystemTable ? systemTableReplicas : tableManagerReplicas;
         final int managerStateReplicationFactor = isSystemTable ? systemTableReplicationFactor : tableManagerReplicationFactor;
 
         final Map<DatabaseInstanceWrapper, Integer> replicas = replicaManager.getAllReplicasOnActiveMachines();
         final Map<DatabaseInstanceWrapper, Integer> failed = new HashMap<DatabaseInstanceWrapper, Integer>();
 
         Diagnostic.traceNoEvent(DiagnosticLevel.NONE, "About to execute meta-data update (isSystemTable:" + isSystemTable + "[ query: " + query + "] onto " + PrettyPrinter.toString(replicas));
 
         int result = -1;
         for (final Entry<DatabaseInstanceWrapper, Integer> replica : replicas.entrySet()) {
 
             if (isLocal(replica.getKey())) {
                 final Command sqlQuery = parser.prepareCommand(query);
 
                 try {
                     result = sqlQuery.update();
 
                     sqlQuery.close();
                 }
                 catch (final RPCException e) {
                     System.err.println("Error in meta-data query: " + query);
                     ErrorHandling.exceptionError(e, "Query: " + query);
                 }
                catch (final SQLException e) {
                    ErrorHandling.exceptionError(e, "Failed to execute query on " + replica.getKey().getURL());
                    failed.put(replica.getKey(), replica.getValue());
                }
             }
             else {
                 try {
                     Diagnostic.traceNoEvent(DiagnosticLevel.NONE, "Executing meta-data update on " + replica.getKey().getURL() + "[ query: " + query + "]");
                     result = replica.getKey().getDatabaseInstance().executeUpdate(query, true);
                 }
                 catch (final RPCException e) {
                     ErrorHandling.exceptionError(e, "Failed to execute query on " + replica.getKey().getURL());
                     failed.put(replica.getKey(), replica.getValue());
                 }
                catch (final SQLException e) {
                    ErrorHandling.exceptionError(e, "Failed to execute query on " + replica.getKey().getURL());
                    failed.put(replica.getKey(), replica.getValue());
                }
             }
         }
 
         final boolean hasRemoved = failed.size() > 0;
 
        ErrorHandling.errorNoEvent("List of failed machines: " + PrettyPrinter.toString(failed));
 
         if (hasRemoved) {
             if (!isSystemTable) {
                 // Remove table replica information from the system table.
                 for (final DatabaseInstanceWrapper replica : failed.keySet()) {
                     try {
                         db.getSystemTable().removeTableManagerStateReplica(tableInfo, replica.getURL());
                     }
                     catch (final RPCException e1) {
                         e1.printStackTrace();
                     }
                     catch (final MovedException e1) {
                         e1.printStackTrace();
                     }
                 }
             }
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, db.getID() + ": Removed one or more replica locations because they couldn't be contacted for the last update: " + PrettyPrinter.toString(failed));
         }
 
         replicaManager.remove(failed.keySet());
 
         // Check that there is a sufficient replication factor.
         if (!isSystemTable && metaDataReplicationEnabled && replicas.size() < managerStateReplicationFactor) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Insufficient replication factor (" + replicas.size() + "<" + managerStateReplicationFactor + ") of Table Manager State on " + db.getID());
             replicateMetaDataIfPossible(db.getSystemTableReference(), isSystemTable);
         }
 
         return result;
     }
 
     /**
      * 
      */
     public void updateLocatorFiles(final boolean isSystemTable) throws Exception {
 
         final H2OPropertiesWrapper persistedInstanceInformation = H2OPropertiesWrapper.getWrapper(db.getID());
         persistedInstanceInformation.loadProperties();
 
         final String descriptorLocation = persistedInstanceInformation.getProperty("descriptor");
         final String databaseName = persistedInstanceInformation.getProperty("databaseName");
 
         if (descriptorLocation == null || databaseName == null) { throw new Exception("The location of the database descriptor must be specifed (it was not found). The database will now terminate."); }
         final H2OLocatorInterface dl = new H2OLocatorInterface(descriptorLocation);
 
         final boolean successful = dl.setLocations(getReplicaLocations(isSystemTable));
 
         if (!successful) {
             ErrorHandling.errorNoEvent("Didn't successfully write new System Replica locations to a majority of locator servers.");
         }
     }
 
     /**
      * Called when a new replica location is added by the local meta data replica manager.
      * 
      * 
      * 
      * @param tableID
      * @param connectionID
      * @param primaryLocationConnectionID
      * @param active
      * @return
      * @throws SQLException
      */
     public int addTableManagerReplicaInformation(final int tableID, final int connectionID, final int primaryLocationConnectionID, final boolean active) throws SQLException {
 
         final String sql = "INSERT INTO " + PersistentSystemTable.TABLEMANAGERSTATE + " VALUES (" + tableID + ", " + connectionID + ", " + primaryLocationConnectionID + ", " + active + ");";
 
         return executeUpdate(sql, true, null);
     }
 
     /**
      * When a table is created this must be called, and all replica locations should be added (there may be more than one replica location
      * when the table is created).
      * 
      * @param tableID
      * @param connectionID
      * @param primaryLocationConnectionID
      * @param active
      * @param tableDetails
      * @return
      */
     public Set<DatabaseInstanceWrapper> getTableManagerReplicaLocations() {
 
         return new HashSet<DatabaseInstanceWrapper>(tableManagerReplicas.getAllReplicasOnActiveMachines().keySet());
     }
 
     private boolean isLocal(final DatabaseInstanceWrapper replica) {
 
         return replica.equals(localDatabase);
     }
 
     public String[] getReplicaLocations(final boolean isSystemTable) {
 
         if (isSystemTable) {
             return systemTableReplicas.getReplicaLocationsAsStrings();
         }
         else {
             return tableManagerReplicas.getReplicaLocationsAsStrings();
         }
     }
 
     public void remove(final IDatabaseInstanceRemote databaseInstance, final boolean isSystemTable) {
 
         final ReplicaManager replicaManager = isSystemTable ? systemTableReplicas : tableManagerReplicas;
 
         replicaManager.remove(new DatabaseInstanceWrapper(null, databaseInstance, isSystemTable));
     }
 
     /**
      * Called when a new Table Manager replica location is added, or a new Table Manager is added.
      * 
      * This deletes any outdated information held by remote machines on local Table Managers.
      * 
      * @return
      * @throws SQLException
      */
     public String constructRemoveReplicaQuery() throws SQLException {
 
         final String databaseName = localDatabase.getURL().sanitizedLocation();
         final String replicaRelation = TableManager.getMetaTableName(databaseName, TableManager.REPLICAS);
         final String tableRelation = TableManager.getMetaTableName(databaseName, TableManager.TABLES);
 
         final StringBuilder deleteReplica = new StringBuilder();
         deleteReplica.append("DELETE FROM ");
         deleteReplica.append(tableRelation);
         deleteReplica.append(" WHERE ");
 
         final StringBuilder deleteTable = new StringBuilder();
         deleteTable.append("DELETE FROM ");
         deleteTable.append(replicaRelation);
         deleteTable.append(" WHERE ");
 
         boolean includeAnd = false;
 
         final Set<TableInfo> localTableManagers = db.getSystemTableReference().getLocalTableManagers().keySet();
 
         for (final TableInfo ti : localTableManagers) {
 
             final int tableID = getTableID(ti, false);
 
             if (includeAnd) {
                 deleteReplica.append(" AND ");
                 deleteTable.append(" AND ");
             }
             else {
                 includeAnd = true;
             }
 
             deleteReplica.append("table_id=" + tableID + " ");
 
             deleteTable.append("table_id=");
             deleteTable.append(tableID);
             deleteTable.append(" ");
 
         }
 
         deleteReplica.append(";");
         deleteTable.append(";");
 
         return deleteReplica.append(deleteTable).toString();
     }
 
     public String constructRemoveReplicaQuery(final TableInfo ti, final boolean removeReplicaInfo, final boolean isSystemTable) throws SQLException {
 
         final String databaseName = localDatabase.getURL().sanitizedLocation();
         final String replicaRelation = isSystemTable ? null : TableManager.getMetaTableName(databaseName, TableManager.REPLICAS);
         final String tableRelation = isSystemTable ? PersistentSystemTable.TABLES : TableManager.getMetaTableName(databaseName, TableManager.TABLES);
 
         StringBuilder sql = new StringBuilder();
 
         if (ti == null || ti.getTableName() == null && ti.getSchemaName() == null) {
             /*
              * Deleting everything
              */
 
             if (removeReplicaInfo) {
                 sql.append("DELETE FROM " + replicaRelation + ";");
             }
 
             sql.append("\nDELETE FROM " + tableRelation + ";");
         }
         else if (ti.getTableName() == null) {
             /*
              * Deleting the entire schema.
              */
             Integer[] tableIDs;
 
             tableIDs = getTableIDs(ti.getSchemaName(), isSystemTable);
 
             if (tableIDs.length > 0 && removeReplicaInfo) {
                 sql = new StringBuilder("DELETE FROM " + replicaRelation);
                 for (int i = 0; i < tableIDs.length; i++) {
                     if (i == 0) {
                         sql.append(" WHERE table_id=" + tableIDs[i]);
                     }
                     else {
                         sql.append(" OR table_id=" + tableIDs[i]);
                     }
                 }
                 sql.append(";");
             }
             else {
                 sql = new StringBuilder();
             }
 
             sql.append("\nDELETE FROM " + tableRelation + " WHERE schemaname='" + ti.getSchemaName() + "'; ");
         }
         else {
 
             final int tableID = getTableID(ti, isSystemTable);
 
             sql = new StringBuilder();
 
             if (removeReplicaInfo) {
                 sql.append("DELETE FROM " + replicaRelation + " WHERE table_id=" + tableID + ";");
             }
             sql.append("\nDELETE FROM " + tableRelation + " WHERE table_id=" + tableID + "; ");
 
         }
         return sql.toString();
     }
 
     /**
      * Gets the table ID for a given database table if it is present in the database. If not, an exception is thrown.
      * 
      * @param tableName
      *            Name of the table
      * @param schemaName
      *            Schema the table is in.
      * @return
      */
     public int getTableID(final TableInfo ti, final boolean isSystemTable) throws SQLException {
 
         final String databaseName = localDatabase.getURL().sanitizedLocation();
 
         final String tableRelation = isSystemTable ? PersistentSystemTable.TABLES : TableManager.getMetaTableName(databaseName, TableManager.TABLES);
 
         final String sql = "SELECT table_id FROM " + tableRelation + " WHERE tablename='" + ti.getTableName() + "' AND schemaname='" + ti.getSchemaName() + "';";
 
         LocalResult result = null;
 
         final Command sqlQuery = parser.prepareCommand(sql);
 
         result = sqlQuery.executeQueryLocal(1);
 
         if (result.next()) {
             return result.currentRow()[0].getInt();
         }
         else {
             throw new SQLException("Internal problem: tableID not found in System Table.");
         }
     }
 
     /**
      * Gets all the table IDs for tables in a schema.
      * 
      * @param schemaName
      * @return
      * @throws SQLException
      */
     protected Integer[] getTableIDs(final String schemaName, final boolean isSystemTable) throws SQLException {
 
         final String databaseName = localDatabase.getURL().sanitizedLocation();
 
         final String tableRelation = isSystemTable ? PersistentSystemTable.TABLES : TableManager.getMetaTableName(databaseName, TableManager.TABLES);
 
         final String sql = "SELECT table_id FROM " + tableRelation + " WHERE schemaname='" + schemaName + "';";
 
         LocalResult result = null;
 
         final Command sqlQuery = parser.prepareCommand(sql);
 
         result = sqlQuery.executeQueryLocal(0);
 
         final Set<Integer> ids = new HashSet<Integer>();
         while (result.next()) {
             ids.add(result.currentRow()[0].getInt());
         }
 
         return ids.toArray(new Integer[0]);
     }
 
     /**
      * The replica manager is notified that a particular machine is now inactive.
      * @param failedMachine
      */
     public void notifyOfFailure(final DatabaseID failedMachine) {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.NONE, "The meta-data replication manager on " + db.getID() + " has been notified of the failure of " + failedMachine);
 
         //        systemTableReplicas.markMachineAsFailed(failedMachine);
         //        tableManagerReplicas.markMachineAsFailed(failedMachine);
 
         systemTableReplicas.remove(new DatabaseInstanceWrapper(failedMachine, null, false));
         tableManagerReplicas.remove(new DatabaseInstanceWrapper(failedMachine, null, false));
 
         Diagnostic.traceNoEvent(DiagnosticLevel.NONE, "The remaining actie table manager replicas are: " + PrettyPrinter.toString(tableManagerReplicas));
     }
 
 }
