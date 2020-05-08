 /*
  * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License, Version 1.0, and under the Eclipse Public License, Version 1.0
  * (http://h2database.com/html/license.html). Initial Developer: H2 Group
  */
 package org.h2.command.ddl;
 
 import java.rmi.RemoteException;
 import java.rmi.server.UnicastRemoteObject;
 import java.sql.SQLException;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 
 import org.h2.command.Prepared;
 import org.h2.command.dml.Insert;
 import org.h2.command.dml.Query;
 import org.h2.constant.ErrorCode;
 import org.h2.constant.LocationPreference;
 import org.h2.constraint.Constraint;
 import org.h2.constraint.ConstraintReferential;
 import org.h2.engine.Database;
 import org.h2.engine.Session;
 import org.h2.expression.Expression;
 import org.h2.message.Message;
 import org.h2.schema.Schema;
 import org.h2.schema.Sequence;
 import org.h2.table.Column;
 import org.h2.table.IndexColumn;
 import org.h2.table.Table;
 import org.h2.table.TableData;
 import org.h2.util.ObjectArray;
 import org.h2.value.DataType;
 import org.h2o.db.id.TableInfo;
 import org.h2o.db.interfaces.DatabaseInstanceRemote;
 import org.h2o.db.interfaces.TableManagerRemote;
 import org.h2o.db.manager.TableManager;
 import org.h2o.db.manager.interfaces.ISystemTable;
 import org.h2o.db.query.TableProxy;
 import org.h2o.db.query.TableProxyManager;
 import org.h2o.db.query.locking.LockRequest;
 import org.h2o.db.query.locking.LockType;
 import org.h2o.db.wrappers.DatabaseInstanceWrapper;
 import org.h2o.util.TransactionNameGenerator;
 import org.h2o.util.exceptions.MovedException;
 import org.h2o.viewer.H2OEventBus;
 import org.h2o.viewer.gwt.client.DatabaseStates;
 import org.h2o.viewer.gwt.client.H2OEvent;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 /**
  * This class represents the statement CREATE TABLE
  */
 public class CreateTable extends SchemaCommand {
 
     private String tableName;
 
     private final ObjectArray constraintCommands = new ObjectArray();
 
     private ObjectArray columns = new ObjectArray();
 
     private IndexColumn[] pkColumns;
 
     private boolean ifNotExists;
 
     private boolean persistent = true;
 
     private boolean temporary;
 
     private boolean globalTemporary;
 
     private boolean onCommitDrop;
 
     private boolean onCommitTruncate;
 
     private Query asQuery;
 
     private String comment;
 
     private boolean clustered;
 
     private TableManager tableManager = null;
 
     public CreateTable(final Session session, final Schema schema) {
 
         super(session, schema);
     }
 
     public void setQuery(final Query query) {
 
         asQuery = query;
     }
 
     public void setTemporary(final boolean temporary) {
 
         this.temporary = temporary;
     }
 
     public void setTableName(final String tableName) {
 
         this.tableName = tableName;
     }
 
     /**
      * Add a column to this table.
      * 
      * @param column
      *            the column to add
      */
     public void addColumn(final Column column) {
 
         if (columns == null) {
             columns = new ObjectArray();
         }
         columns.add(column);
     }
 
     /**
      * Add a constraint statement to this statement. The primary key definition is one possible constraint statement.
      * 
      * @param command
      *            the statement to add
      */
     public void addConstraintCommand(final Prepared command) throws SQLException {
 
         if (command instanceof CreateIndex) {
             constraintCommands.add(command);
         }
         else {
             final AlterTableAddConstraint con = (AlterTableAddConstraint) command;
             boolean alreadySet;
             if (con.getType() == AlterTableAddConstraint.PRIMARY_KEY) {
                 alreadySet = setPrimaryKeyColumns(con.getIndexColumns());
             }
             else {
                 alreadySet = false;
             }
             if (!alreadySet) {
                 constraintCommands.add(command);
             }
         }
     }
 
     public void setIfNotExists(final boolean ifNotExists) {
 
         this.ifNotExists = ifNotExists;
     }
 
     @Override
     public int update() throws SQLException, RemoteException {
 
         /*
          * The only time this is called is when a CreateTable command is replayed at database startup. This differs from the normal
          * CreateTable execution because a TableManager for the table may exist somewhere. Instead of creating a new Table Manager this
          * command should look for an existing one somewhere. The command to create the Table Manager tables should have already been
          * replayed, so the
          */
         return update(TransactionNameGenerator.generateName("NULLCREATION"));
     }
 
     @Override
     public int update(final String transactionName) throws SQLException, RemoteException {
 
         // TODO rights: what rights are required to create a table?
         session.commit(true);
         final Database db = session.getDatabase();
         if (!db.isPersistent()) {
             persistent = false;
         }
 
         if (getSchema().findTableOrView(session, tableName, LocationPreference.NO_PREFERENCE) != null && !isStartup() || getSchema().findLocalTableOrView(session, tableName) != null) {
            if (ifNotExists) {
                tableProxy.clear();
                return 0;

            }

            if (isStartup()) { return 0; }

             throw Message.getSQLException(ErrorCode.TABLE_OR_VIEW_ALREADY_EXISTS_1, tableName);
         }
 
         if (asQuery != null) {
             asQuery.prepare();
             if (columns.size() == 0) {
                 generateColumnsFromQuery();
             }
             else if (columns.size() != asQuery.getColumnCount()) { throw Message.getSQLException(ErrorCode.COLUMN_COUNT_DOES_NOT_MATCH); }
         }
         if (pkColumns != null) {
             final int len = pkColumns.length;
             for (int i = 0; i < columns.size(); i++) {
                 final Column c = (Column) columns.get(i);
                 for (int j = 0; j < len; j++) {
                     if (c.getName().equals(pkColumns[j].columnName)) {
                         c.setNullable(false);
                     }
                 }
             }
         }
         final ObjectArray sequences = new ObjectArray();
         for (int i = 0; i < columns.size(); i++) {
             final Column c = (Column) columns.get(i);
             if (c.getAutoIncrement()) {
                 final int objId = getObjectId(true, true);
                 c.convertAutoIncrementToSequence(session, getSchema(), objId, temporary);
             }
             final Sequence seq = c.getSequence();
             if (seq != null) {
                 sequences.add(seq);
             }
         }
         final int id = getObjectId(true, true);
 
         final TableData table = getSchema().createTable(tableName, id, columns, persistent, clustered, headPos);
         table.setComment(comment);
         table.setTemporary(temporary);
         table.setGlobalTemporary(globalTemporary);
         if (temporary && !globalTemporary) {
             if (onCommitDrop) {
                 table.setOnCommitDrop(true);
             }
             if (onCommitTruncate) {
                 table.setOnCommitTruncate(true);
             }
             session.addLocalTempTable(table);
         }
         else {
             db.addSchemaObject(session, table);
         }
         try {
             for (int i = 0; i < columns.size(); i++) {
                 final Column c = (Column) columns.get(i);
                 c.prepareExpression(session);
             }
             for (int i = 0; i < sequences.size(); i++) {
                 final Sequence sequence = (Sequence) sequences.get(i);
                 table.addSequence(sequence);
             }
             for (int i = 0; i < constraintCommands.size(); i++) {
                 final Prepared command = (Prepared) constraintCommands.get(i);
                 command.update();
             }
             if (asQuery != null) {
                 final boolean old = session.getUndoLogEnabled();
                 try {
                     session.setUndoLogEnabled(false);
                     Insert insert = null;
                     insert = new Insert(session, true);
                     insert.setQuery(asQuery);
                     insert.setTable(table);
                     insert.prepare();
                     insert.update();
                 }
                 finally {
                     session.setUndoLogEnabled(old);
                 }
             }
 
             /*
              * ################################################################## ####### Create a System Table entry.
              * ################################################################## #######
              */
 
             final boolean localTable = db.isTableLocal(getSchema());
             if (!db.isManagementDB() && !localTable && !isStartup()) {
                 final ISystemTable systemTable = db.getSystemTable();
                 assert systemTable != null;
 
                 int tableSet = -1;
                 boolean thisTableReferencesAnExistingTable = false;
 
                 try {
 
                     if (table.getConstraints() != null) {
                         final Constraint[] constraints = new Constraint[table.getConstraints().size()];
                         table.getConstraints().toArray(constraints);
 
                         final Set<Table> referencedTables = new HashSet<Table>();
                         for (final Constraint con : constraints) {
                             if (con instanceof ConstraintReferential) {
                                 thisTableReferencesAnExistingTable = true;
                                 referencedTables.add(con.getRefTable());
                             }
                         }
 
                         if (thisTableReferencesAnExistingTable) {
                             if (referencedTables.size() > 1) {
                                 System.err.println("Unexpected. Test that this still works.");
                             }
                             for (final Table tab : referencedTables) {
                                 tableSet = tab.getTableSet();
                             }
                         }
                         else {
                             tableSet = systemTable.getNewTableSetNumber();
                         }
                     }
                     else {
                         tableSet = systemTable.getNewTableSetNumber();
                     }
 
                     final TableInfo tableInfo = new TableInfo(tableName, getSchema().getName(), table.getModificationId(), tableSet, table.getTableType(), db.getURL());
 
                     createReplicas(tableInfo, transactionName);
 
                 }
                 catch (final MovedException e) {
                     throw new RemoteException("System Table has moved. Cannot complete query.");
                 }
                 table.setTableSet(tableSet);
             }
 
             if (!db.isManagementDB()) {
                 prepareTransaction(transactionName);
             }
 
         }
         catch (final SQLException e) {
             db.checkPowerOff();
             db.removeSchemaObject(session, table);
             throw e;
         }
 
         return 0;
     }
 
     /**
      * Create replicas if required by the system configuration (Settings.RELATION_REPLICATION_FACTOR).
      * 
      * @param tableInfo
      *            The name of the table being created.
      * @param tableManager 
      * @throws RemoteException
      * @throws SQLException
      * @throws MovedException
      */
     private void createReplicas(final TableInfo tableInfo, final String transactionName) throws RemoteException, SQLException, MovedException {
 
         final Map<DatabaseInstanceWrapper, Integer> replicaLocations = tableProxy.getRemoteReplicaLocations();
 
         if (replicaLocations != null && replicaLocations.size() > 0) {
             /*
              * If true, this table should be immediately replicated onto a number of other instances.
              */
             Diagnostic.traceNoEvent(DiagnosticLevel.INIT, "Creating replica of table " + tableInfo.getFullTableName() + " onto " + (tableProxy.getReplicaLocations().size() - 1) + " other instances.");
 
             String sql = sqlStatement.substring("CREATE TABLE".length());
             sql = "CREATE EMPTY REPLICA" + sql + " FROM '" + getSchema().getDatabase().getURL().getURLwithRMIPort() + "'";
 
             final Set<DatabaseInstanceWrapper> successfulUpdates = new HashSet<DatabaseInstanceWrapper>();
 
             //Execute create replica operations.
             for (final DatabaseInstanceWrapper replicaLocation : replicaLocations.keySet()) {
                 final DatabaseInstanceRemote instance = replicaLocation.getDatabaseInstance();
 
                 final int result = instance.execute(sql, transactionName, false);
 
                 if (result == 0) {
 
                     successfulUpdates.add(replicaLocation);
                 }
             }
 
             // Update meta-data to reflect replica locations.
 
             for (final DatabaseInstanceWrapper databaseInstanceWrapper : successfulUpdates) {
                 // Update Table Manager meta-data.
 
                 final TableInfo tableDetails = new TableInfo(tableInfo, databaseInstanceWrapper.getURL());
                 tableManager.addInitialReplicaInformation(tableDetails);
 
                 // Update query proxy.
 
                 tableProxy.addReplicaLocation(databaseInstanceWrapper);
             }
         }
     }
 
     private void generateColumnsFromQuery() {
 
         final int columnCount = asQuery.getColumnCount();
         final ObjectArray expressions = asQuery.getExpressions();
         for (int i = 0; i < columnCount; i++) {
             final Expression expr = (Expression) expressions.get(i);
             final int type = expr.getType();
             final String name = expr.getAlias();
             long precision = expr.getPrecision();
             final int displaySize = expr.getDisplaySize();
             final DataType dt = DataType.getDataType(type);
             if (precision > 0 && (dt.defaultPrecision == 0 || dt.defaultPrecision > precision && dt.defaultPrecision < Byte.MAX_VALUE)) {
                 // don't set precision to MAX_VALUE if this is the default
                 precision = dt.defaultPrecision;
             }
             int scale = expr.getScale();
             if (scale > 0 && (dt.defaultScale == 0 || dt.defaultScale > scale)) {
                 scale = dt.defaultScale;
             }
             final Column col = new Column(name, type, precision, scale, displaySize);
             addColumn(col);
         }
     }
 
     /**
      * Sets the primary key columns, but also check if a primary key with different columns is already defined.
      * 
      * @param columns
      *            the primary key columns
      * @return true if the same primary key columns where already set
      */
     private boolean setPrimaryKeyColumns(final IndexColumn[] columns) throws SQLException {
 
         if (pkColumns != null) {
             if (columns.length != pkColumns.length) { throw Message.getSQLException(ErrorCode.SECOND_PRIMARY_KEY); }
             for (int i = 0; i < columns.length; i++) {
                 if (!columns[i].columnName.equals(pkColumns[i].columnName)) { throw Message.getSQLException(ErrorCode.SECOND_PRIMARY_KEY); }
             }
             return true;
         }
         pkColumns = columns;
         return false;
     }
 
     public void setPersistent(final boolean persistent) {
 
         this.persistent = persistent;
     }
 
     public void setGlobalTemporary(final boolean globalTemporary) {
 
         this.globalTemporary = globalTemporary;
     }
 
     /**
      * This temporary table is dropped on commit.
      */
     public void setOnCommitDrop() {
 
         onCommitDrop = true;
     }
 
     /**
      * This temporary table is truncated on commit.
      */
     public void setOnCommitTruncate() {
 
         onCommitTruncate = true;
     }
 
     public void setComment(final String comment) {
 
         this.comment = comment;
     }
 
     public void setClustered(final boolean clustered) {
 
         this.clustered = clustered;
     }
 
     @Override
     public boolean shouldBePropagated() {
 
         /*
          * If this is not a regular table (i.e. it is a meta-data table, then it will not be propagated regardless.
          */
         return isRegularTable();
     }
 
     @Override
     protected boolean isRegularTable() {
 
         final boolean isLocal = session.getDatabase().isTableLocal(getSchema());
         return !session.getDatabase().isManagementDB() && !internalQuery && !isLocal;
     }
 
     /*
      * The queryProxyManager variable isn't used in create table, because it can't have a proxy
      * for something which hasn't yet been created.
      */
     @Override
     public void acquireLocks(final TableProxyManager tableProxyManager) throws SQLException {
 
         System.out.println("acquiring locks to create table: " + tableName);
         final Database db = session.getDatabase();
 
         assert tableProxyManager.getTableProxy(tableName) == null; // should never exist.
 
         /*
          * ### H2O. Check that the table doesn't already exist elsewhere. ###
          */
 
         final boolean tableLocal = db.isTableLocal(getSchema());
         final boolean managementDB = db.isManagementDB();
         final boolean startup = isStartup();
 
         if (!db.getSystemTableReference().isSystemTableLocal() && !managementDB && !tableLocal && !startup) {
 
             final TableManagerRemote tableManager = db.getSystemTableReference().lookup(getSchema().getName() + "." + tableName, false);
 
             if (tableManager != null) {
                 try {
                     if (tableManager.isAlive()) { throw Message.getSQLException(ErrorCode.TABLE_OR_VIEW_ALREADY_EXISTS_1, tableName); }
                 }
                 catch (final Exception e) {
                     // The TableManager is not alive.
                 }
             }
         }
 
         System.out.println("tableLocal: " + tableLocal);
         System.out.println("managementDB: " + managementDB);
         System.out.println("startup: " + startup);
         tableProxy = null;
 
         if (!tableLocal && !managementDB && !startup) { // if it is startup then we don't want to create a table manager yet.
 
             System.out.println("ctal3");
             final TableInfo ti = new TableInfo(tableName, getSchema().getName(), 0l, 0, "TABLE", db.getURL());
             try {
                 System.out.println("ctal4");
                 tableManager = new TableManager(ti, db, false);
                 System.out.println("ctal5");
             }
             catch (final Exception e1) {
                 e1.printStackTrace();
             }
 
             try {
                 // Make Table Manager exportable first.
                 UnicastRemoteObject.exportObject(tableManager, 0);
             }
             catch (final Exception e) {
                 // May already be exported.
             }
             H2OEventBus.publish(new H2OEvent(db.getURL().getURL(), DatabaseStates.TABLE_MANAGER_CREATION, ti.getFullTableName()));
 
             tableProxy = TableProxy.getTableProxyAndLock(tableManager, ti.getFullTableName(), new LockRequest(session), LockType.CREATE, db, false);
 
         }
         else {
             /*
              * This is a system table meta-table, but it still needs a TableProxy to indicate that it is acceptable to execute the query.
              */
             tableProxy = TableProxy.getTableProxyAndLock(table, LockType.CREATE, new LockRequest(session), db);
 
         }
 
         tableProxyManager.addProxy(tableProxy);
     }
 }
