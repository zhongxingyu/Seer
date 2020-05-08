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
 
 package org.h2o.db.manager;
 
 import java.net.InetSocketAddress;
 import java.sql.SQLException;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Queue;
 import java.util.Set;
 
 import org.h2.command.Command;
 import org.h2.command.Parser;
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2.result.LocalResult;
 import org.h2o.autonomic.numonic.ThresholdChecker;
 import org.h2o.autonomic.numonic.metric.CreateReplicaMetric;
 import org.h2o.autonomic.numonic.metric.IMetric;
 import org.h2o.autonomic.numonic.ranking.Requirements;
 import org.h2o.autonomic.numonic.threshold.Threshold;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.db.id.DatabaseID;
 import org.h2o.db.id.DatabaseURL;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.IDatabaseInstanceRemote;
 import org.h2o.db.interfaces.ITableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTableMigratable;
 import org.h2o.db.manager.monitoring.tablemanager.ITableManagerMonitor;
 import org.h2o.db.manager.monitoring.tablemanager.TableManagerMonitor;
 import org.h2o.db.query.TableProxy;
 import org.h2o.db.query.asynchronous.CommitResult;
 import org.h2o.db.query.locking.ILockingTable;
 import org.h2o.db.query.locking.LockRequest;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.query.locking.LockingTable;
 import org.h2o.db.replication.ReplicaManager;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.util.exceptions.MigrationException;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.util.exceptions.StartupException;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 import uk.ac.standrews.cs.nds.util.PrettyPrinter;
 import uk.ac.standrews.cs.numonic.data.ResourceType;
 import uk.ac.standrews.cs.stachord.interfaces.IChordRemoteReference;
 
 /**
  * <p>
  * The Table Manager represents a user table in H2O, and is responsible for storing information on replicas for that table, and handing out
  * locks to access those replicas.
  * </p>
  * 
  * <p>
  * There is one Table Manager for every user table in the system.
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class TableManager extends PersistentManager implements ITableManagerRemote, Observer {
 
     private static final long serialVersionUID = 3347740231310946286L;
 
     /**
      * Name of the schema used to store Table Manager tables.
      */
     private static final String SCHEMA = "H2O.";
 
     /**
      * Name of tables' table in Table Manager.
      */
     public static final String TABLES = "H2O_TM_TABLE";
 
     /**
      * Name of replicas' table in Table Manager.
      */
     public static final String REPLICAS = "H2O_TM_REPLICA";
 
     /**
      * Name of connections' table in Table Manager.
      */
     public static final String CONNECTIONS = "H2O_TM_CONNECTION";
 
     public static final String TABLEMANAGERSTATE = "H2O_TM_TABLEMANAGERS";
 
     /**
      * The name of the table that this Table Manager is responsible for.
      */
     private final String tableName;
 
     /**
      * Name of the schema in which the table is located.
      */
     private String schemaName;
 
     private ReplicaManager replicaManager;
 
     /**
      * Stores locks held by various databases for accessing this table (all replicas).
      */
     private final ILockingTable lockingTable;
 
     private boolean shutdown = false;
 
     /*
      * MIGRATION RELATED CODE.
      */
     /**
      * If this System Table has been moved to another location (i.e. its state has been transferred to another machine and it is no longer
      * active) this field will not be null, and will note the new location of the System Table.
      */
 
     private String movedLocation = null;
 
     /**
      * Whether the System Table is in the process of being migrated. If this is true the System Table will be 'locked', unable to service
      * requests.
      */
     private boolean inMigration;
 
     /**
      * Whether the System Table has been moved to another location.
      */
     private boolean hasMoved = false;
 
     /**
      * The amount of time which has elapsed since migration began. Used to timeout requests which take too long.
      */
     private long migrationTime = 0l;
 
     /**
      * The timeout period for migrating the System Table.
      */
     private static final int MIGRATION_TIMEOUT = 10000;
 
     private final IChordRemoteReference location;
 
     private final String fullName;
 
     private final int desiredRelationReplicationFactor;
 
     private final TableInfo tableInfo;
 
     private final ITableManagerMonitor queryMonitor;
 
     /**
      * True if the table has already been created and this new instance is being created as part of a Table
      * Manager migration or recreation. False if this is being created as part of a CREATE TABLE operation.
      */
     private boolean tableAlreadyExists;
 
     /**
      * Temporarily stores the set of replicas that were created as part of the CREATE TABLE command for this
      * table. This information is persisted (and this field is emptied) when the transaction containing the CREATE TABLE
      * operation commits. 
      */
     private final Set<TableInfo> temporaryInitialReplicas = new HashSet<TableInfo>();
 
     /**
      * Metric used to query the System Table when asking where to create a replica.
      */
     private static IMetric createReplicaMetric = new CreateReplicaMetric();
 
     /**
      * A new Table Manager object is created when acquiring locks during a CREATE TABLE operation and when recreating or moving
      * an existing Table Manager.
      * @param tableAlreadyExists    True if the table has already been created and this new instance is being created as part of a Table
      * Manager migration or recreation. False if this is being created as part of a CREATE TABLE operation.
      */
     public TableManager(final TableInfo tableDetails, final Database database, final boolean tableAlreadyExists) {
 
         super(database);
 
         this.tableAlreadyExists = tableAlreadyExists;
 
         final String dbName = database.getID().sanitizedLocation();
         setMetaDataTableNames(getMetaTableName(dbName, TABLES), getMetaTableName(dbName, REPLICAS), getMetaTableName(dbName, CONNECTIONS), getMetaTableName(dbName, TABLEMANAGERSTATE));
 
         tableName = tableDetails.getTableName();
 
         schemaName = tableDetails.getSchemaName();
 
         if (schemaName == null || schemaName.equals("")) {
             schemaName = "PUBLIC";
         }
 
         fullName = schemaName + "." + tableName;
         tableInfo = tableDetails.getGenericTableInfo();
 
         replicaManager = new ReplicaManager();
         replicaManager.add(database.getLocalDatabaseInstanceInWrapper()); // the first replica will be created here.
 
         lockingTable = new LockingTable(schemaName, tableName);
 
         location = database.getChordInterface().getLocalChordReference();
 
         desiredRelationReplicationFactor = Integer.parseInt(database.getDatabaseSettings().get("RELATION_REPLICATION_FACTOR"));
 
         queryMonitor = new TableManagerMonitor();
 
         getDB().getTableManagerServer().exportObject(this);
 
         if (getDB().getNumonic() != null) {
             getDB().getNumonic().addObserver(this);
         }
     }
 
     public static String getMetaTableName(final String databaseName, final String tablePostfix) {
 
         return SCHEMA + "H2O_" + databaseName + "_" + tablePostfix;
     }
 
     @Override
     public boolean addTableInformation(final DatabaseID tableManagerURL, final TableInfo tableDetails) throws RPCException, MovedException, SQLException {
 
         final int result = super.addConnectionInformation(tableManagerURL, true);
 
         final boolean added = result != -1;
         if (!added) { return false; }
 
         return super.addTableInformation(tableManagerURL, tableDetails, true);
 
         /*
          * The System Table isn't contacted here, but in the Create Table class. This is because the Table isn't officially created until
          * the end of CreateTable.update().
          */
     }
 
     @Override
     public void addReplicaInformation(final TableInfo tableDetails) throws RPCException, MovedException, SQLException {
 
         preMethodTest();
 
         super.addConnectionInformation(tableDetails.getDatabaseID(), true);
         super.addReplicaInformation(tableDetails);
         replicaManager.add(getDatabaseInstance(tableDetails.getDatabaseID()));
     }
 
     @Override
     public void removeReplicaInformation(final TableInfo ti) throws RPCException, MovedException {
 
         super.removeReplicaInformation(ti);
 
         IDatabaseInstanceRemote dbInstance = getDB().getDatabaseInstance(ti.getDatabaseID());
         if (dbInstance == null) {
             dbInstance = getDB().getDatabaseInstance(ti.getDatabaseID());
             if (dbInstance == null) {
                 ErrorHandling.errorNoEvent("Couldn't remove replica location.");
             }
         }
 
         replicaManager.remove(new DatabaseInstanceWrapper(null, dbInstance, true));
 
     }
 
     @Override
     public boolean removeTableInformation() throws RPCException, SQLException, MovedException {
 
         return removeTableInformation(getTableInfo(), true);
     }
 
     @Override
     public boolean removeTableInformation(final TableInfo tableInfo, final boolean removeReplicaInfo) {
 
         return super.removeTableInformation(getTableInfo(), removeReplicaInfo);
     }
 
     /**
      * Creates the set of tables used by the Table Manager.
      * 
      * @return Result of the update.
      * @throws SQLException
      */
     public static int createTableManagerTables(final Session session) throws SQLException {
 
         Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Creating Table Manager tables.");
 
         final StringBuilder builder = new StringBuilder();
 
         final String databaseName = session.getDatabase().getID().sanitizedLocation().toUpperCase();
         String sql = createSQL(getMetaTableName(databaseName, TableManager.TABLES), getMetaTableName(databaseName, TableManager.CONNECTIONS));
 
         builder.append(sql);
         builder.append("\n\nCREATE TABLE IF NOT EXISTS ");
         builder.append(getMetaTableName(databaseName, TableManager.REPLICAS));
         builder.append("(replica_id INTEGER NOT NULL auto_increment(1,1), table_id INTEGER NOT NULL, connection_id INTEGER NOT NULL, storage_type VARCHAR(255), active boolean NOT NULL, ");
         builder.append("table_set INT NOT NULL, PRIMARY KEY (replica_id), FOREIGN KEY (table_id) REFERENCES ");
         builder.append(getMetaTableName(databaseName, TableManager.TABLES));
         builder.append(" (table_id) ON DELETE CASCADE ,  FOREIGN KEY (connection_id) REFERENCES ");
         builder.append(getMetaTableName(databaseName, TableManager.CONNECTIONS));
         builder.append(" (connection_id));");
         sql += builder.toString();
 
         final Parser parser = new Parser(session, true);
 
         final Command query = parser.prepareCommand(sql);
         try {
             return query.update();
         }
         catch (final RPCException e) {
             e.printStackTrace();
             return -1;
         }
     }
 
     /**
      * Get the database instance at the specified database URL.
      * 
      * @param dbID
      *            location of the database instance.
      * @return null if the instance wasn't found (including if it wasn't active).
      */
     private DatabaseInstanceWrapper getDatabaseInstance(final DatabaseID dbID) {
 
         final ISystemTableMigratable systemTable = getDB().getSystemTableReference().getSystemTable();
 
         IDatabaseInstanceRemote dir = null;
 
         if (systemTable != null) {
             try {
                 dir = systemTable.getDatabaseInstance(dbID);
             }
             catch (final RPCException e1) {
                 e1.printStackTrace();
             }
             catch (final MovedException e1) {
                 try {
                     getDB().getSystemTableReference().handleMovedException(e1);
                     dir = systemTable.getDatabaseInstance(dbID);
                 }
                 catch (final Exception e) {
                     e.printStackTrace();
                 }
             }
         }
 
         if (dir == null) {
             try {
                 // The System Table doesn't contain a proper reference for the
                 // remote database instance. Try and find one,
                 // then update the System Table if successful.
                 dir = getDB().getRemoteInterface().getDatabaseInstanceAt(dbID);
 
                 if (dir == null) {
                     ErrorHandling.errorNoEvent("DatabaseInstanceRemote wasn't found.");
                 }
                 else {
 
                     getDB().getSystemTable().addConnectionInformation(dbID, new DatabaseInstanceWrapper(dbID, dir, true));
                 }
             }
             catch (final Exception e) {
                 // e.printStackTrace();
             }
         }
 
         return new DatabaseInstanceWrapper(dbID, dir, true);
     }
 
     @Override
     public synchronized TableProxy getTableProxy(LockType lockTypeRequested, final LockRequest lockRequest) throws RPCException, SQLException, MovedException {
 
         preMethodTest();
 
         if (replicaManager.allReplicasSize() == 0 && !lockTypeRequested.equals(LockType.CREATE)) { throw new SQLException("Illegal State. There must be at least one replica"); }
 
         int currentUpdateID = replicaManager.getCurrentUpdateID();
         boolean isDrop = false;
 
         if (lockTypeRequested == LockType.DROP) {
             /*
              * If a table is dropped then created again with auto-commit turned off the update ID given here is higher than it is expected
              * to be on the create table operation. This just resets the update ID on the preceding drop. The LockType of DROP is not used
              * anywhere else, so the request is processed in the locking table as a write.
              */
             currentUpdateID = 0;
             isDrop = true;
             lockTypeRequested = LockType.WRITE;
         }
 
         if (Settings.QUERY_MONITORING_ENABLED) {
             queryMonitor.addQueryInformation(lockRequest, lockTypeRequested); //Query Monitoring.
         }
 
         final LockType lockGranted = lockingTable.requestLock(lockTypeRequested, lockRequest);
 
         return new TableProxy(lockGranted, tableInfo, selectReplicaLocations(lockTypeRequested, lockRequest, isDrop), this, lockRequest, currentUpdateID, lockTypeRequested);
     }
 
     /**
      * <p>
      * Selects a set of replica locations on which replicas will be created for a given table or schema.
      * 
      * <p>
      * This decision is currently based on the DESIRED_REPLICATION_FACTOR variable (if the query is a create), the SYNCHRONOUS_UPDATE
      * variable if the query is another form of update, and the database instance where the request was initiated.
      * 
      * @param primaryLocation
      *            The location of the primary copy - also the location of the Table Manager. This location will NOT be returned in the list
      *            of replica locations (because the primary copy already exists there).
      * @param lockType
      * @param isDrop
      * @param databaseInstanceRemote
      *            Requesting machine.
      * @return The set of database instances that should host a replica for the given table/schema. The return value will be NULL if no more
      *         replicas need to be created.
      */
     private Map<DatabaseInstanceWrapper, Integer> selectReplicaLocations(final LockType lockType, final LockRequest lockRequest, final boolean isDrop) {
 
         if (lockType == LockType.READ || lockType == LockType.NONE) { return replicaManager.getAllReplicasOnActiveMachines(); }// else, a more informed decision is needed.
 
         /*
          * The set of machines onto which new replicas will be added.
          */
         final Map<DatabaseInstanceWrapper, Integer> newReplicaLocations = new HashMap<DatabaseInstanceWrapper, Integer>();
 
         /*
          * The set of all replica locations that could be involved in the query.
          */
         Queue<DatabaseInstanceWrapper> potentialReplicaLocations = null;
 
         if (lockType == LockType.CREATE) {
             /*
              * We know that the CREATE operation has been executed on the machine on which this Table Manager has been created, because it
              * is the create operation that initializes the Table Manager in the first place.
              */
             newReplicaLocations.put(lockRequest.getRequestLocation(), 0);
             if (desiredRelationReplicationFactor == 1) { return newReplicaLocations; // No more replicas are needed currently.
             }
 
             try {
                 // the update could be sent to any or all machines in the system.
 
                 potentialReplicaLocations = getDB().getSystemTableReference().getRankedListOfInstances(createReplicaMetric, Requirements.NO_FILTERING);
             }
             catch (final RPCException e) {
                 e.printStackTrace();
             }
             catch (final MovedException e) {
                 e.printStackTrace();
             }
 
             int currentReplicationFactor = 1; // currently one copy of the table.
 
             /*
              * Loop through all potential replica locations, selecting enough to satisfy the system's replication factor. The location of the
              * primary copy cannot be re-used.
              */
             if (potentialReplicaLocations != null && potentialReplicaLocations.size() > 0) {
 
                 for (final DatabaseInstanceWrapper dbInstance : potentialReplicaLocations) {
                     // This includes the location of the primary copy.
                     final Integer nullIfNoPrevious = newReplicaLocations.put(dbInstance, 0);
                     if (nullIfNoPrevious == null) {
                         currentReplicationFactor++;
                     }
 
                     /*
                      * Do we have enough replicas yet?
                      */
                     if (currentReplicationFactor == desiredRelationReplicationFactor) {
                         break;
                     }
                 }
             }
 
             if (currentReplicationFactor < desiredRelationReplicationFactor) {
                 // Couldn't replicate to enough machines.
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Insufficient number of machines available to reach a replication factor of " + desiredRelationReplicationFactor + ". The table will be replicated on " + currentReplicationFactor + " instances.");
             }
         }
         else if (lockType == LockType.WRITE) {
             Map<DatabaseInstanceWrapper, Integer> replicaLocations;
 
             if (isDrop) {
                 /*
                  * If this is a drop table request we return a hashmap where the update IDs are all zero. This is due to a problem where
                  * AUTO COMMIT is off, and a new table is created after a table has been dropped. This results in the update ID being higher
                  * than expected unless we reset them on DROP.
                  */
                 replicaLocations = new HashMap<DatabaseInstanceWrapper, Integer>();
                 for (final Entry<DatabaseInstanceWrapper, Integer> entry : replicaManager.getAllReplicas().entrySet()) {
                     replicaLocations.put(entry.getKey(), 0);
                 }
             }
             else {
                 replicaLocations = replicaManager.getAllReplicasOnActiveMachines(); // The update could be sent to any or all machines holding the given table.
 
                 //Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Replicas on active machines for " + fullName + ": " + PrettyPrinter.toString(replicaLocations));
             }
 
             return replicaLocations;
         }
 
         return newReplicaLocations;
     }
 
     @Override
     public DatabaseID getLocation() throws RPCException, MovedException {
 
         preMethodTest();
 
         return getDB().getID();
     }
 
     @Override
     public String getTableName() {
 
         return tableName;
     }
 
     @Override
     public boolean isAlive() throws RPCException, MovedException {
 
         if (shutdown) { return false; }
 
         preMethodTest();
 
         return true;
     }
 
     @Override
     public void releaseLockAndUpdateReplicaState(final boolean commit, final LockRequest lockRequest, final Collection<CommitResult> committedQueries, final boolean asynchronousCommit) throws RPCException, MovedException, SQLException {
 
         try {
 
             if (tableNewlyCreated(commit)) {
                 completeCreationByUpdatingSystemTable();
                 tableAlreadyExists = true;
             }
 
             // Find the type of lock that was taken out.
             final LockType lockType = lockingTable.peekAtLockGranted(lockRequest);
 
             // Update the set of 'active replicas' and their update IDs.
             if (commit) {
                 //Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Query committed. Replica set will be updated.");
                 //The method call below changes update IDs which is why rollbacks don't call it.
                 updateActiveReplicaSet(commit, committedQueries, asynchronousCommit, lockType);
             }
             else {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Query was not committed. Some replicas may be on inactive instances.");
                 replicaManager.markNonCommittingReplicasAsInactive(committedQueries, tableInfo);
             }
 
         }
         finally {
             // Release locks regardless whether the previous operations were successful.
             if (!asynchronousCommit && tableAlreadyExists) { // if the table doesn't already exist, an exception was thrown calling completeCreationByUpdatingSystemTable(), and no locks are held.
                 lockingTable.releaseLock(lockRequest);
             }
         }
     }
 
     /**
      * Whether this is a create table request.
      * 
      * If it's not a commit on a CREATE TABLE request nothing needs to be persisted.
      * @param commit
      * @return
      */
     public boolean tableNewlyCreated(final boolean commit) {
 
         return !tableAlreadyExists && commit;
     }
 
     private void completeCreationByUpdatingSystemTable() throws RPCException, MovedException, SQLException {
 
         // Add Basic Table Information to the System Table.
         final Set<DatabaseInstanceWrapper> replicaLocations = db.getMetaDataReplicaManager().getTableManagerReplicaLocations();
         replicaLocations.add(db.getLocalDatabaseInstanceInWrapper());
         final TableInfo tableInfo = new TableInfo(tableName, schemaName, db.getID());
         final boolean successful = db.getSystemTableReference().addTableInformation(this, tableInfo, replicaLocations);
 
         if (!successful) { throw new SQLException("Failed to add Table Manager reference to System Table."); }
 
         try {
             persistToCompleteStartup(tableInfo);
 
             H2OEventBus.publish(new H2OEvent(db.getID().getURL(), DatabaseStates.TABLE_CREATION, tableInfo.getFullTableName()));
         }
         catch (final StartupException e) {
             throw new SQLException("Failed to create table. Couldn't persist table manager meta-data [" + e.getMessage() + "].");
         }
     }
 
     /**
      * Update the set of 'active replicas' and their update IDs.
      * @param commit
      * @param committedQueries
      * @param asynchronousCommit
      * @param lockType
      * @throws SQLException 
      */
     private void updateActiveReplicaSet(final boolean commit, final Collection<CommitResult> committedQueries, final boolean asynchronousCommit, final LockType lockType) throws SQLException {
 
         // Reads don't change the set of active replicas.
         if (lockType == LockType.WRITE || asynchronousCommit) { // LockType.WRITE == LockType.CREATE in the locking table.
             final Set<DatabaseInstanceWrapper> changed = replicaManager.completeUpdate(commit, committedQueries, tableInfo, !asynchronousCommit);
 
             if (!asynchronousCommit && changed.size() < replicaManager.getActiveReplicas().size() && changed.size() > 1) {
                 // This is the first part of a query. Some replicas will be made inactive.
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Newly inactive replicas: " + PrettyPrinter.toString(changed));
                 persistInactiveInformation(tableInfo, changed);
             }
             else {
                 // This is the asynchronous part of the query. Some replicas will be made active.
                 persistActiveInformation(tableInfo, changed);
             }
 
             printCurrentActiveReplicas();
         }
     }
 
     private void printCurrentActiveReplicas() {
 
         if (Diagnostic.getLevel().equals(DiagnosticLevel.INIT)) {
 
             final String databaseName = db.getID().sanitizedLocation();
             final String sql = "SELECT LOCAL ONLY " + "connection_type, machine_name, db_location, connection_port, chord_port, " + getMetaTableName(databaseName, REPLICAS) + ".table_id, " + getMetaTableName(databaseName, REPLICAS) + ".connection_id FROM " + getMetaTableName(databaseName, REPLICAS)
                             + ", " + getMetaTableName(databaseName, TABLES) + ", " + getMetaTableName(databaseName, CONNECTIONS) + " WHERE tablename = '" + tableName + "' AND schemaname='" + schemaName + "' AND " + getMetaTableName(databaseName, REPLICAS) + ".active='true' AND "
                             + getMetaTableName(databaseName, TABLES) + ".table_id=" + getMetaTableName(databaseName, REPLICAS) + ".table_id AND " + getMetaTableName(databaseName, REPLICAS) + ".connection_id=" + getMetaTableName(databaseName, CONNECTIONS) + ".connection_id;";
 
             try {
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Current Active Replicas for table: " + tableName);
                 final LocalResult rs = executeQuery(sql);
 
                 while (rs.next()) {
 
                     final DatabaseID dbID = new DatabaseID(new DatabaseURL(rs.currentRow()[0].getString(), rs.currentRow()[1].getString(), rs.currentRow()[3].getInt(), rs.currentRow()[2].getString(), false, rs.currentRow()[4].getInt()));
 
                     Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "\tLocation: " + dbID + "; tableID = " + rs.currentRow()[5].getString() + "; connectionID = " + rs.currentRow()[6].getString());
                 }
             }
             catch (final SQLException e) {
                 e.printStackTrace();
             }
         }
     }
 
     @Override
     public TableInfo getTableInfo() {
 
         return new TableInfo(tableName, schemaName, getDB().getID());
     }
 
     @Override
     public void remove(final boolean dropCommand) {
 
         // Remove all persisted information
         removeTableInformation(getTableInfo(), true);
 
         shutdown = true;
 
         H2OEventBus.publish(new H2OEvent(db.getID().getURL(), DatabaseStates.TABLE_MANAGER_SHUTDOWN));
 
     }
 
     /*******************************************************
      * Methods implementing the Migrate interface.
      ***********************************************************/
 
     private void preMethodTest() throws RPCException, MovedException {
 
         if (hasMoved) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Table Manager " + fullName + " has moved. Throwing MovedException.");
             throw new MovedException(movedLocation);
         }
         else if (shutdown) {
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Table Manager " + fullName + " has shutdown. An old reference was probably cached somewhere.");
             throw new MovedException(null);
         }
         /*
          * If the manager is being migrated, and has been migrated for less than 10 seconds (timeout period, throw an execption.
          */
         if (inMigration) {
             // If it hasn't moved, but is in the process of migration an
             // exception will be thrown.
             final long currentTimeOfMigration = System.currentTimeMillis() - migrationTime;
 
             if (currentTimeOfMigration > MIGRATION_TIMEOUT) {
                 inMigration = false; // Timeout request.
                 migrationTime = 0l;
 
                 throw new RPCException("Timeout exception. Migration took too long. Current time :" + currentTimeOfMigration + ", TIMEOUT time: " + MIGRATION_TIMEOUT);
             }
         }
     }
 
     @Override
     public void checkConnection() throws RPCException, MovedException {
 
         preMethodTest();
 
     }
 
     @Override
     public void completeMigration() throws RPCException, MovedException, MigrationException {
 
         if (!inMigration) { // the migration process has timed out.
             throw new MigrationException("Migration process has timed-out. Took too long to migrate (timeout: " + MIGRATION_TIMEOUT + "ms)");
         }
 
         hasMoved = true;
         inMigration = false;
 
     }
 
     @Override
     public void prepareForMigration(final String newLocation) throws RPCException, MigrationException, MovedException {
 
         preMethodTest();
 
         movedLocation = newLocation;
 
         inMigration = true;
 
         migrationTime = System.currentTimeMillis();
     }
 
     @Override
     public void buildTableManagerState(final ITableManagerRemote otherTableManager) throws RPCException, MovedException {
 
         preMethodTest();
 
         /*
          * Table name, schema name, and other infor are already obtained when the table manager instance is created.
          */
 
         /*
          * Obtain replica manager.
          */
         replicaManager = ReplicaManager.recreateReplicaManager(otherTableManager);
     }
 
     @Override
     public String getSchemaName() throws RPCException {
 
         return schemaName;
     }
 
     @Override
     public int getTableSet() throws RPCException {
 
         return 1; // TODO implement
     }
 
     @Override
     public DatabaseID getDatabaseURL() throws RPCException {
 
         return getDB().getID();
     }
 
     @Override
     public void shutdown(final boolean shutdown) throws RPCException, MovedException {
 
         this.shutdown = shutdown;
     }
 
     @Override
     public IChordRemoteReference getChordReference() throws RPCException {
 
         return location;
     }
 
     @Override
     public void recreateReplicaManagerState(final String oldPrimaryDatabaseName) throws RPCException, SQLException {
 
         final ReplicaManager rm = new ReplicaManager();
 
         /*
          * Get Replica information from persisted state.
          */
 
         final String oldTableRelation = getMetaTableName(oldPrimaryDatabaseName, TABLES);
         final String oldconnectionRelation = getMetaTableName(oldPrimaryDatabaseName, CONNECTIONS);
         final String oldReplicaRelation = getMetaTableName(oldPrimaryDatabaseName, REPLICAS);
 
         final String sql = "SELECT LOCAL ONLY connection_type, machine_name, db_location, connection_port, chord_port FROM " + oldReplicaRelation + ", " + oldTableRelation + ", " + oldconnectionRelation + " WHERE tablename = '" + tableName + "' AND schemaname='" + schemaName + "' AND "
                         + oldReplicaRelation + ".active='true' AND " + oldTableRelation + ".table_id=" + oldReplicaRelation + ".table_id AND " + oldconnectionRelation + ".connection_id=" + oldReplicaRelation + ".connection_id;";
 
         LocalResult rs = null;
         try {
             rs = executeQuery(sql);
         }
         catch (final SQLException e) {
             ErrorHandling.errorNoEvent(db.getID() + ": Error replicating table manager state.");
             throw e;
         }
 
         final List<DatabaseInstanceWrapper> replicaLocations = new LinkedList<DatabaseInstanceWrapper>();
         while (rs.next()) {
 
             final DatabaseID dbID = new DatabaseID(new DatabaseURL(rs.currentRow()[0].getString(), rs.currentRow()[1].getString(), rs.currentRow()[3].getInt(), rs.currentRow()[2].getString(), false, rs.currentRow()[4].getInt()));
 
             // Don't include the URL of the old instance unless it is still running.
             final DatabaseInstanceWrapper replicaLocation = getDatabaseInstance(dbID);
 
             boolean alive = true;
             try {
                 alive = replicaLocation.getDatabaseInstance().isAlive();
                 Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Active replica for " + tableName + " found on " + dbID);
 
             }
             catch (final Exception e) {
                 alive = false;
             }
 
             replicaLocation.setActive(alive); // even dead replicas must be recorded.
             replicaLocations.add(replicaLocation);
 
         }
 
         if (replicaLocations.size() == 0) { throw new SQLException("No replicas were listed for this table (" + fullName + "). An internal error has occured."); }
 
         rm.add(replicaLocations);
 
         replicaManager = rm;
     }
 
     @Override
     public int getNumberofReplicas() throws RPCException {
 
         return replicaManager.getNumberOfReplicas();
     }
 
     @Override
     public void persistToCompleteStartup(final TableInfo tableInfo) throws RPCException, StartupException {
 
         try {
             addTableInformation(getDB().getID(), tableInfo);
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Replicas of " + fullName + "[started on " + getDB().getID() + "] have been created on: ");
             for (final TableInfo ti : temporaryInitialReplicas) {
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "\t" + ti.getDatabaseID());
             }
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "All other replicas on Table Manager creation: " + PrettyPrinter.toString(replicaManager.getAllReplicasOnActiveMachines()));
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Set of all replicas known to the table manager: " + PrettyPrinter.toString(replicaManager.getAllReplicas()));
 
             for (final TableInfo replica : temporaryInitialReplicas) {
                 addReplicaInformation(replica);
             }
             temporaryInitialReplicas.clear();
 
             //Table replica replication:
            if (replicaManager.getAllReplicasOnActiveMachines().size() < desiredRelationReplicationFactor) {
                 createNewReplicas();
             }
 
             //Meta-data replication:
             db.getMetaDataReplicaManager().replicateMetaDataIfPossible(db.getSystemTableReference(), false);
         }
         catch (final MovedException e) {
             throw new StartupException("Newly created Table Manager throws a MovedException. This should never happen - serious internal error.");
         }
         catch (final SQLException e) {
             throw new StartupException("Failed to persist table manager meta-data to disk: " + e.getMessage());
         }
 
     }
 
     /**
      * Create new replicas to reach the required replication factor.
      * @throws MovedException 
      * @throws RPCException 
      */
     private void createNewReplicas() throws RPCException, MovedException {
 
         final int currentReplicationFactor = replicaManager.getAllReplicasOnActiveMachines().size();
         final int newReplicasNeeded = desiredRelationReplicationFactor - currentReplicationFactor;
 
         Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Deciding whether to create new replicas. Current replication factor is " + currentReplicationFactor + " and the desired replication factor is " + desiredRelationReplicationFactor + ".");
 
         if (newReplicasNeeded > 0) {
             final Queue<DatabaseInstanceWrapper> potentialReplicaLocations = getDB().getSystemTableReference().getRankedListOfInstances(createReplicaMetric, Requirements.NO_FILTERING);
 
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Table Manager for " + fullName + " (on " + db.getID() + ") will attempt to replicate to " + newReplicasNeeded + " of these machines: " + PrettyPrinter.toString(potentialReplicaLocations));
 
             final String createReplicaSQL = "CREATE  REPLICA " + fullName + " FROM '" + replicaManager.getPrimaryLocation().getURL().getURLwithRMIPort() + "'";
 
             final Set<DatabaseInstanceWrapper> locationOfNewReplicas = new HashSet<DatabaseInstanceWrapper>();
 
             DatabaseInstanceWrapper wrapper = null;
             while ((wrapper = potentialReplicaLocations.poll()) != null) {
 
                 if (!replicaManager.contains(wrapper)) {
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Attempting to replicate table state of " + fullName + " to " + wrapper.getURL());
 
                     final IDatabaseInstanceRemote instance = wrapper.getDatabaseInstance();
 
                     try {
                         final int result = instance.executeUpdate(createReplicaSQL, true);
 
                         if (result == 0) {
                             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Successfully replicated " + tableInfo.getFullTableName() + " onto " + wrapper.getURL());
                             locationOfNewReplicas.add(wrapper);
                         }
                     }
                     catch (final RPCException e) {
                         ErrorHandling.errorNoEvent("Tried to create replica of " + tableInfo.getFullTableName() + " onto " + wrapper.getURL() + ", but couldn't connnect: " + e.getMessage());
 
                         db.getSystemTable().suspectInstanceOfFailure(wrapper.getURL());
                     }
                     catch (final SQLException e) {
                         ErrorHandling.errorNoEvent("Tried to create replica of " + tableInfo.getFullTableName() + " onto " + wrapper.getURL() + ", but couldn't connnect: " + e.getMessage());
 
                     }
                 }
                 else {
                     Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "There is already a replica on " + wrapper.getURL() + ", so we won't replicate here.");
                 }
             } //end of while loop attempting replication.
 
             // Update meta-data to reflect new replica locations.
 
             for (final DatabaseInstanceWrapper databaseInstanceWrapper : locationOfNewReplicas) {
                 // Update Table Manager meta-data.
 
                 final TableInfo tableDetails = new TableInfo(tableInfo, databaseInstanceWrapper.getURL());
                 try {
                     addReplicaInformation(tableDetails);
                 }
                 catch (final SQLException e) {
                     ErrorHandling.errorNoEvent("Failed to add information regarding new replicas for " + tableInfo.getFullTableName() + " on " + db.getID() + ".");
 
                 }
 
             }
 
         }
 
     }
 
     public void persistReplicaInformation() {
 
         for (final DatabaseInstanceWrapper dir : replicaManager.getActiveReplicas().keySet()) {
             final TableInfo ti = new TableInfo(getTableInfo());
             ti.setURL(dir.getURL());
             try {
                 super.addConnectionInformation(ti.getDatabaseID(), true);
                 super.addReplicaInformation(ti);
             }
             catch (final Exception e) {
                 ErrorHandling.exceptionError(e, "Trying to persist replica information.");
             }
         }
     }
 
     @Override
     public String toString() {
 
         return "TableManager [fullName=" + fullName + ", lockingTable=" + lockingTable + "]";
     }
 
     /**
      * Add information on the set of replicas initially created for this table (as part of the CREATE TABLE operation). This information
      * must be persisted later once the CREATE TABLE operation has succeeded.
      * @param tableDetails
      */
     public void addInitialReplicaInformation(final TableInfo tableDetails) {
 
         temporaryInitialReplicas.add(tableDetails);
     }
 
     @Override
     public Map<DatabaseInstanceWrapper, Integer> getActiveReplicas() throws RPCException, MovedException {
 
         return replicaManager.getActiveReplicas();
     }
 
     @Override
     public Map<DatabaseInstanceWrapper, Integer> getAllReplicas() throws RPCException, MovedException {
 
         return replicaManager.getAllReplicas();
     }
 
     @Override
     public DatabaseInstanceWrapper getDatabaseLocation() throws RPCException, MovedException {
 
         return replicaManager.getManagerLocation();
     }
 
     @Override
     public InetSocketAddress getAddress() throws RPCException {
 
         return getDB().getTableManagerServer().getAddress();
     }
 
     @Override
     public void update(final Observable o, final Object arg) {
 
         final Threshold threshold = ThresholdChecker.getThresholdObject(arg);
 
         if (threshold.resourceName == ResourceType.CPU_USER && threshold.above) {
             //CPU utilization has been exceeded.
 
             //Act on this.
         }
     }
 
     @Override
     public String getFullTableName() {
 
         return tableInfo.getFullTableName();
     }
 
     @Override
     public void notifyOfFailure(final DatabaseID failedMachine) throws RPCException {
 
         replicaManager.markMachineAsFailed(failedMachine);
 
         /*
          * Contact the meta-data replica manager for this machine as well.
          */
 
         db.getMetaDataReplicaManager().notifyOfFailure(failedMachine);
 
         /*
          * Check whether any new replicas are needed.
          */
 
         try {
             createNewReplicas();
         }
         catch (final MovedException e) {
             ErrorHandling.exceptionError(e, "Error checking whether new replicas need to be created on the Table Manager for " + db.getID());
         }
 
     }
 
     @Override
     public Map<DatabaseInstanceWrapper, Integer> getReplicasOnActiveMachines() {
 
         return replicaManager.getAllReplicasOnActiveMachines();
     }
 
     @Override
     public DatabaseID getLocalDatabaseID() throws RPCException {
 
         return db.getID();
     }
 }
