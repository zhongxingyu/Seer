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
 
 package org.h2o.db.query;
 
 import java.io.Serializable;
 import java.rmi.RemoteException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2.table.Table;
 import org.h2o.autonomic.settings.Settings;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.DatabaseInstanceRemote;
 import org.h2o.db.interfaces.TableManagerRemote;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.manager.recovery.LocatorException;
 import org.h2o.db.manager.recovery.SystemTableAccessException;
 import org.h2o.db.query.asynchronous.AsynchronousQueryExecutor;
 import org.h2o.db.query.locking.LockRequest;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.test.fixture.H2OTest;
 import org.h2o.util.exceptions.MovedException;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * A proxy class used to make sending queries to multiple replicas easier. The query only needs to be sent to the query proxy, which handles
  * the rest of the transaction.
  * 
  * <p>
  * Query proxies are created by the Table Manager for a given table, and indicate permission to perform a given query. The level of this
  * permission is indicated by the type of lock granted (see lockGranted attribute).
  * 
  * @author Angus Macdonald (angus@cs.st-andrews.ac.uk)
  */
 public class TableProxy implements Serializable {
 
     /**
      * Generated serial version.
      */
     private static final long serialVersionUID = -31853777345527026L;
 
     private LockType lockGranted;
 
     private final TableInfo tableName;
 
     private Map<DatabaseInstanceWrapper, Integer> allReplicas;
 
     /**
      * Proxy for the Table Manager of this table. Used to release any locks held at the end of the transaction.
      */
     private TableManagerRemote tableManager; // changed by al - this is either a local guy or an RMI reference
 
     /**
      * The database instance making the request. This is used to request the lock (i.e. the lock for the given query is taken out in the
      * name of this database instance.
      */
     private final LockRequest requestingDatabase;
 
     /**
      * ID assigned to this update by the Table Manager. This is returned to inform the Table Manager what replicas were updated.
      */
     private int updateID;
 
     /**
      * The type of lock that was requested. It may not have been granted
      * 
      * @see #lockGranted
      */
     private LockType lockRequested;
 
     /**
      * @param lockGranted the type of lock that has been granted
      * @param tableName name of the table that is being used in the query
      * @param allReplicas proxies for each of the replicas used in the query.
      * @param tableManager proxy for the Table Manager of the table involved in the query (i.e. tableName).
      * @param updateID ID given to this update.
      */
     public TableProxy(final LockType lockGranted, final TableInfo tableName, final Map<DatabaseInstanceWrapper, Integer> allReplicas, final TableManagerRemote tableManager, final LockRequest requestingMachine, final int updateID, final LockType lockRequested) {
 
         this.lockGranted = lockGranted;
         this.lockRequested = lockRequested;
         this.tableName = tableName;
         this.allReplicas = allReplicas;
         this.tableManager = tableManager;
         requestingDatabase = requestingMachine;
         this.updateID = updateID;
     }
 
     /**
      * Creates a dummy query proxy.
      * 
      * @param lockRequest
      */
     public TableProxy(final LockRequest lockRequest) {
 
         lockGranted = LockType.WRITE;
         allReplicas = new HashMap<DatabaseInstanceWrapper, Integer>();
         if (lockRequest != null) { // true when the DB hasn't started yet (management DB, etc.)
             allReplicas.put(lockRequest.getRequestLocation(), 0);
         }
         requestingDatabase = lockRequest;
         tableName = new TableInfo("Dummy", "Table");
     }
 
     /**
      * Executes the given SQL update.
      * 
      * @param query the query to be executed
      * @throws SQLException
      */
     public int executeUpdate(final String query, final String transactionNameForQuery, final Session session) throws SQLException {
 
         if ((allReplicas == null || allReplicas.size() == 0) && lockRequested == LockType.CREATE) {
 
             // If we don't know of any replicas and this is a CREATE TABLE statement then we just run the query on the local DB instance.
 
             allReplicas = new HashMap<DatabaseInstanceWrapper, Integer>();
             allReplicas.put(requestingDatabase.getRequestLocation(), 0);
 
         }
 
         // Execute the query. Send the query to each DB instance holding a replica.
 
         final AsynchronousQueryExecutor queryExecutor = new AsynchronousQueryExecutor(session.getDatabase());
         final int returnValue = queryExecutor.executeQuery(query, transactionNameForQuery, allReplicas, tableName, session, false);
 
         H2OTest.rmiFailure(); // Test code to simulate the failure of DB instances at this point.
 
         if (returnValue < 0) { throw new SQLException("Commit failed on one or more replicas. The query will be rolled back."); }
 
         return returnValue;
     }
 
     /**
      * Obtain a query proxy for the given table.
      * 
      * @param table table involved in query.
      * @param lockType lock required for query
      * @param db local database instance - needed to inform DM of: the identity of the requesting machine, and to obtain the Table Manager
      *            for the given table.
      * @return
      * @throws SQLException
      */
     public static TableProxy getTableProxyAndLock(final Table table, final LockType lockType, final LockRequest lockRequest, final Database db) throws SQLException {
 
         // If the table is temporary it only exists as part of this transaction - i.e. no lock is needed.
         // If one of the reserved table names is used (SYSTEM_RANGE, for example) it isn't a proper table so won't have a Table Manager.
 
         if (table != null && !table.getTemporary() && !Settings.reservedTableNames.contains(table.getName())) { return getTableProxyAndLock(lockRequest, table.getFullName(), lockType, db); }
         return getDummyQueryProxy(lockRequest);
     }
 
     /**
      * Returns a dummy query proxy which indicates that it is possible to execute the query and lists the local (requesting) database as the
      * only replica. Used in cases where a query won't have to be propagated, or where no particular table is specified in the query (e.g.
      * create schema), and so it isn't possible to lock on a particular table.
      * 
      * @param lockRequest
      * @return
      */
     public static TableProxy getDummyQueryProxy(final LockRequest lockRequest) {
 
         return new TableProxy(lockRequest);
     }
 
     /**
      * Obtain a proxy for the given table.
      * 
      * @param tableManager
      * @param lockRequest DB making the request.
      * @return Query proxy for a specific table within H20.
      * @throws SQLException
      */
     public static TableProxy getTableProxyAndLock(TableManagerRemote tableManager, final String tableName, final LockRequest lockRequest, final LockType lockType, final Database db, final boolean alreadyCalled) throws SQLException {
 
         assert lockRequest != null : "A requesting database must be specified.";
 
         try {
             try {
                 return tableManager.getTableProxy(lockType, lockRequest);
             }
             catch (final MovedException e) {
                 // Get an uncached Table Manager from the System Table
                 tableManager = db.getSystemTableReference().lookup(tableName, false);
 
                 return tableManager.getTableProxy(lockType, lockRequest);
             }
         }
         catch (final java.rmi.NoSuchObjectException e) {
             e.printStackTrace();
             throw new SQLException("Table Manager could not be accessed. It may not have been exported to RMI correctly.");
         }
         catch (final RemoteException e) {
 
             if (!alreadyCalled) {
 
                 final ISystemTable systemTable = db.getSystemTable();
 
                 boolean systemTableActive = true;
 
                 if (systemTable == null) {
                     // reInstantiateSystemTable
                     try {
                         db.getSystemTableReference().failureRecovery();
                     }
                     catch (final LocatorException e1) {
                         ErrorHandling.exceptionError(e1, "Failed to contact locator servers.");
                     }
                     catch (final SystemTableAccessException e1) {
                         systemTableActive = false;
                         throw new SQLException("The System Table could not be re-instantiated.");
                     }
                 }
 
                 if (systemTableActive) {
                     try {
                         final TableManagerRemote newTableManager = db.getSystemTable().recreateTableManager(new TableInfo(tableName));
                         if (newTableManager != null) { return getTableProxyAndLock(newTableManager, tableName, lockRequest, lockType, db, true); }
                     }
                     catch (final RemoteException e1) {
                         e1.printStackTrace();
                     }
                     catch (final MovedException e1) {
                         e1.printStackTrace();
                     }
                 }
             }
 
             throw new SQLException("Table Manager could not be accessed. The table is unavailable until the Table Manager is reactivated.");
         }
         catch (final MovedException e) {
             throw new SQLException("Table Manager has moved and can't be accessed at this location.");
         }
     }
 
     public static TableProxy getTableProxyAndLock(final LockRequest lockRequest, final String tableName, final LockType lockType, final Database db) throws SQLException {
 
         final TableManagerRemote tableManager = db.getSystemTableReference().lookup(tableName, true);
 
         if (tableManager == null) {
             Diagnostic.trace(DiagnosticLevel.FULL, "Table Manager proxy was null when requesting table.");
             db.getSystemTableReference();
             throw new SQLException("Table Manager not found for table.");
         }
 
         return getTableProxyAndLock(tableManager, tableName, lockRequest, lockType, db, false);
     }
 
     public LockType getLockGranted() {
 
         return lockGranted;
     }
 
     @Override
     public String toString() {
 
         if (tableManager == null) {
 
             // This is a dummy proxy.
 
             return "Dummy Query Proxy: Used for local query involving a system table or another entity entirely.";
         }
 
         final String plural = allReplicas.size() == 1 ? "" : "s";
         return tableName + " (" + allReplicas.size() + " replica" + plural + "), with lock '" + lockGranted + "', request from " + requestingDatabase;
     }
 
     public Map<DatabaseInstanceWrapper, Integer> getReplicaLocations() {
 
         return allReplicas;
     }
 
     /**
      * Add a new replica location. This should be used carefully because locks (and replica locations) have already been acquired
      * when the TableProxy is created. Currently this is only used to 
      * @param databaseInstanceWrapper
      */
     public void addReplicaLocation(final DatabaseInstanceWrapper databaseInstanceWrapper) {
 
         allReplicas.put(databaseInstanceWrapper, updateID);
     }
 
     public Map<DatabaseInstanceWrapper, Integer> getRemoteReplicaLocations() {
 
         final Map<DatabaseInstanceWrapper, Integer> remoteReplicas = new HashMap<DatabaseInstanceWrapper, Integer>(allReplicas);
         final Integer removedItem = remoteReplicas.remove(requestingDatabase.getRequestLocation());
 
         assert removedItem != null : "Tried to remove the local replica from the set of all replicas, but failed. Possibly equality check problem.";
 
         return remoteReplicas;
     }
 
     public TableManagerRemote getTableManager() {
 
         return tableManager;
     }
 
     public int getUpdateID() {
 
         return updateID;
     }
 
     protected void setLockType(final LockType lockGranted) {
 
         this.lockGranted = lockGranted;
     }
 
     /**
      * @return the requestingDatabase
      */
     public LockRequest getRequestingDatabase() {
 
         return requestingDatabase;
     }
 
     /**
      * Checks whether there is only one database in the system. Currently used in CreateReplica to ensure the system doesn't try to create a
      * replica of something on the same instance.
      */
     public boolean isSingleDatabase(final DatabaseInstanceRemote localDatabase) {
 
         return allReplicas != null && allReplicas.size() == 1 && getRequestingDatabase().getRequestLocation().getDatabaseInstance() == localDatabase;
     }
 
     /**
      * Name of the table this proxy holds locks for.
      */
     public TableInfo getTableName() {
 
         return tableName;
     }
 
     public int getNumberOfReplicas() {
 
         return allReplicas == null ? 0 : allReplicas.size();
     }

    /**
     * Clear this Table Proxy of all information relating to a table. This is done when a transaction can return successfully despite
     * having done nothing. 
     * 
     * <p>For example, CREATE TABLE IF NOT EXISTS will return 0 if the table exists, so it should not be possible
     * for this Table Proxy to 'commit' the creation of this table a second time - the table proxy should effectively be discarded.
     */
    public void clear() {

        tableManager = null;
    }
 }
