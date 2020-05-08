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
 
 package org.h2.engine;
 
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.Random;
 import java.util.Set;
 
 import org.h2.command.Command;
 import org.h2.command.CommandInterface;
 import org.h2.command.Parser;
 import org.h2.command.Prepared;
 import org.h2.command.dml.SetTypes;
 import org.h2.constant.ErrorCode;
 import org.h2.constant.SysProperties;
 import org.h2.constraint.Constraint;
 import org.h2.index.Index;
 import org.h2.jdbc.JdbcConnection;
 import org.h2.log.InDoubtTransaction;
 import org.h2.log.LogSystem;
 import org.h2.log.UndoLog;
 import org.h2.log.UndoLogRecord;
 import org.h2.message.Message;
 import org.h2.message.Trace;
 import org.h2.message.TraceSystem;
 import org.h2.result.LocalResult;
 import org.h2.result.Row;
 import org.h2.schema.Schema;
 import org.h2.store.DataHandler;
 import org.h2.table.Table;
 import org.h2.util.ObjectArray;
 import org.h2.util.ObjectUtils;
 import org.h2.value.Value;
 import org.h2.value.ValueLob;
 import org.h2.value.ValueLong;
 import org.h2.value.ValueNull;
 import org.h2.value.ValueString;
 import org.h2o.db.query.TableProxyManager;
 import org.h2o.db.remote.IDatabaseRemote;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 /**
  * A session represents an embedded database connection. When using the server mode, this object resides on the server side and communicates
  * with a SessionRemote object on the client side.
  */
 public class Session extends SessionWithState {
 
     /**
      * The prefix of generated identifiers. It may not have letters, because they are case sensitive.
      */
     private static final String SYSTEM_IDENTIFIER_PREFIX = "_";
 
     private static int nextSerialId;
 
    private final int serialId = nextSerialId++;
 
     private final Database database;
 
     private ConnectionInfo connectionInfo;
 
     private final User user;
 
     private final int id;
 
     private final ObjectArray locks = new ObjectArray();
 
     private TableProxyManager proxyManagerForCurrentTransaction = null;
 
     private final UndoLog undoLog;
 
     private Random random;
 
     private final LogSystem logSystem;
 
     private int lockTimeout;
 
     private Value lastIdentity = ValueLong.get(0);
 
     private int firstUncommittedLog = LogSystem.LOG_WRITTEN;
 
     private int firstUncommittedPos = LogSystem.LOG_WRITTEN;
 
     private HashMap<String, Integer> savepoints;
 
     private final Exception stackTrace = new Exception();
 
     private Map<String, Table> localTempTables;
 
     private Map<String, Index> localTempTableIndexes;
 
     private Map<String, Constraint> localTempTableConstraints;
 
     private int throttle;
 
     private long lastThrottle;
 
     private Command currentCommand;
 
     private boolean allowLiterals;
 
     private String currentSchemaName;
 
     private String[] schemaSearchPath;
 
     private String traceModuleName;
 
     private Map<String, ValueLob> unlinkMap;
 
     private int systemIdentifier;
 
     private Map<String, Procedure> procedures;
 
     private boolean undoLogEnabled = true;
 
     private boolean autoCommitAtTransactionEnd;
 
     private String currentTransactionName;
 
     private volatile long cancelAt;
 
     private boolean closed;
 
     private final long sessionStart = System.currentTimeMillis();
 
     private long currentCommandStart;
 
     private Map<String, Value> variables;
 
     private Set<LocalResult> temporaryResults;
 
     private int queryTimeout = SysProperties.getMaxQueryTimeout();
 
     private int lastUncommittedDelete;
 
     private boolean commitOrRollbackDisabled;
 
     private Table waitForLock;
 
     private int modificationId;
 
     private int modificationIdState;
 
     /*
      * The auto commit field that an external application can actually control in H2O.
      */
     private boolean applicationAutoCommit = true;
 
     public Session(final Database database, final User user, final int id) {
 
         // TODO remove public identifier - only needed for RMI tests.
         this.database = database;
         undoLog = new UndoLog(this);
         this.user = user;
         this.user.sessions++;
         this.id = id;
         logSystem = database.getLog();
         final Setting setting = database.findSetting(SetTypes.getTypeName(SetTypes.DEFAULT_LOCK_TIMEOUT));
         lockTimeout = setting == null ? Constants.INITIAL_LOCK_TIMEOUT : setting.getIntValue();
         currentSchemaName = Constants.SCHEMA_MAIN;
     }
 
     public boolean setCommitOrRollbackDisabled(final boolean x) {
 
         final boolean old = commitOrRollbackDisabled;
         commitOrRollbackDisabled = x;
         return old;
     }
 
     private void initVariables() {
 
         if (variables == null) {
             variables = new HashMap<String, Value>();
         }
     }
 
     /**
      * Set the value of the given variable for this session.
      * 
      * @param name
      *            the name of the variable (may not be null)
      * @param value
      *            the new value (may not be null)
      */
     public void setVariable(final String name, Value value) throws SQLException {
 
         initVariables();
         modificationId++;
         Value old;
         if (value == ValueNull.INSTANCE) {
             old = variables.remove(name);
         }
         else {
             if (value instanceof ValueLob) {
                 // link it, to make sure we have our own file
                 value = value.link(database, ValueLob.TABLE_ID_SESSION_VARIABLE);
             }
             old = variables.put(name, value);
         }
         if (old != null) {
             // close the old value (in case it is a lob)
             old.unlink();
             old.close();
         }
     }
 
     /**
      * Get the value of the specified user defined variable. This method always returns a value; it returns ValueNull.INSTANCE if the
      * variable doesn't exist.
      * 
      * @param name
      *            the variable name
      * @return the value, or NULL
      */
     public Value getVariable(final String name) {
 
         initVariables();
         final Value v = variables.get(name);
         return v == null ? ValueNull.INSTANCE : v;
     }
 
     /**
      * Get the list of variable names that are set for this session.
      * 
      * @return the list of names
      */
     public String[] getVariableNames() {
 
         if (variables == null) { return new String[0]; }
         final String[] list = new String[variables.size()];
         variables.keySet().toArray(list);
         return list;
     }
 
     /**
      * Get the local temporary table if one exists with that name, or null if not.
      * 
      * @param name
      *            the table name
      * @return the table, or null
      */
     public Table findLocalTempTable(final String name) {
 
         if (localTempTables == null) { return null; }
         return localTempTables.get(name);
     }
 
     public ObjectArray getLocalTempTables() {
 
         if (localTempTables == null) { return new ObjectArray(); }
         return new ObjectArray(localTempTables.values());
     }
 
     /**
      * Add a local temporary table to this session.
      * 
      * @param table
      *            the table to add
      * @throws SQLException
      *             if a table with this name already exists
      */
     public void addLocalTempTable(final Table table) throws SQLException {
 
         if (localTempTables == null) {
             localTempTables = new HashMap<String, Table>();
         }
         if (localTempTables.get(table.getName()) != null) { throw Message.getSQLException(ErrorCode.TABLE_OR_VIEW_ALREADY_EXISTS_1, table.getSQL()); }
         modificationId++;
         localTempTables.put(table.getName(), table);
     }
 
     /**
      * Drop and remove the given local temporary table from this session.
      * 
      * @param table
      *            the table
      */
     public void removeLocalTempTable(final Table table) throws SQLException {
 
         modificationId++;
         localTempTables.remove(table.getName());
         table.removeChildrenAndResources(this);
     }
 
     /**
      * Get the local temporary index if one exists with that name, or null if not.
      * 
      * @param name
      *            the table name
      * @return the table, or null
      */
     public Index findLocalTempTableIndex(final String name) {
 
         if (localTempTableIndexes == null) { return null; }
         return localTempTableIndexes.get(name);
     }
 
     public Map<String, Index> getLocalTempTableIndexes() {
 
         if (localTempTableIndexes == null) { return new HashMap<String, Index>(); }
         return localTempTableIndexes;
     }
 
     /**
      * Add a local temporary index to this session.
      * 
      * @param index
      *            the index to add
      * @throws SQLException
      *             if a index with this name already exists
      */
     public void addLocalTempTableIndex(final Index index) throws SQLException {
 
         if (localTempTableIndexes == null) {
             localTempTableIndexes = new HashMap<String, Index>();
         }
         if (localTempTableIndexes.get(index.getName()) != null) { throw Message.getSQLException(ErrorCode.INDEX_ALREADY_EXISTS_1, index.getSQL()); }
         localTempTableIndexes.put(index.getName(), index);
     }
 
     /**
      * Drop and remove the given local temporary index from this session.
      * 
      * @param index
      *            the index
      */
     public void removeLocalTempTableIndex(final Index index) throws SQLException {
 
         if (localTempTableIndexes != null) {
             localTempTableIndexes.remove(index.getName());
             index.removeChildrenAndResources(this);
         }
     }
 
     /**
      * Get the local temporary constraint if one exists with that name, or null if not.
      * 
      * @param name
      *            the constraint name
      * @return the constraint, or null
      */
     public Constraint findLocalTempTableConstraint(final String name) {
 
         if (localTempTableConstraints == null) { return null; }
         return localTempTableConstraints.get(name);
     }
 
     /**
      * Get the map of constraints for all constraints on local, temporary tables, if any. The map's keys are the constraints' names.
      * 
      * @return the map of constraints, or null
      */
     public Map<String, Constraint> getLocalTempTableConstraints() {
 
         if (localTempTableConstraints == null) { return new HashMap<String, Constraint>(); }
         return localTempTableConstraints;
     }
 
     /**
      * Add a local temporary constraint to this session.
      * 
      * @param constraint
      *            the constraint to add
      * @throws SQLException
      *             if a constraint with the same name already exists
      */
     public void addLocalTempTableConstraint(final Constraint constraint) throws SQLException {
 
         if (localTempTableConstraints == null) {
             localTempTableConstraints = new HashMap<String, Constraint>();
         }
         final String name = constraint.getName();
         if (localTempTableConstraints.get(name) != null) { throw Message.getSQLException(ErrorCode.CONSTRAINT_ALREADY_EXISTS_1, constraint.getSQL()); }
         localTempTableConstraints.put(name, constraint);
     }
 
     /**
      * Drop and remove the given local temporary constraint from this session.
      * 
      * @param constraint
      *            the constraint
      */
     public void removeLocalTempTableConstraint(final Constraint constraint) throws SQLException {
 
         if (localTempTableConstraints != null) {
             localTempTableConstraints.remove(constraint.getName());
             constraint.removeChildrenAndResources(this);
         }
     }
 
     @Override
     protected void finalize() {
 
         if (!SysProperties.runFinalize) { return; }
         if (!closed) { throw Message.getInternalError("not closed", stackTrace); }
     }
 
     public User getUser() {
 
         return user;
     }
 
     public int getLockTimeout() {
 
         return lockTimeout;
     }
 
     public void setLockTimeout(final int lockTimeout) {
 
         this.lockTimeout = lockTimeout;
     }
 
     @Override
     public CommandInterface prepareCommand(final String sql, final int fetchSize) throws SQLException {
 
         return prepareLocal(sql);
     }
 
     /**
      * Parse and prepare the given SQL statement. This method also checks the rights.
      * 
      * @param sql
      *            the SQL statement
      * @return the prepared statement
      */
     public Prepared prepare(final String sql) throws SQLException {
 
         return prepare(sql, false);
     }
 
     /**
      * Parse and prepare the given SQL statement.
      * 
      * @param sql
      *            the SQL statement
      * @param rightsChecked
      *            true if the rights have already been checked
      * @return the prepared statement
      */
     public Prepared prepare(final String sql, final boolean rightsChecked) throws SQLException {
 
         final Parser parser = new Parser(this, true);
         parser.setRightsChecked(rightsChecked);
         return parser.prepare(sql);
     }
 
     /**
      * Parse and prepare the given SQL statement. This method also checks if the connection has been closed.
      * 
      * @param sql
      *            the SQL statement
      * @return the prepared statement
      */
     public Command prepareLocal(final String sql) throws SQLException {
 
         if (closed) { throw Message.getSQLException(ErrorCode.CONNECTION_BROKEN); }
         final Parser parser = new Parser(this, false);
         return parser.prepareCommand(sql);
     }
 
     public Database getDatabase() {
 
         return database;
     }
 
     @Override
     public int getPowerOffCount() {
 
         return database.getPowerOffCount();
     }
 
     @Override
     public void setPowerOffCount(final int count) {
 
         database.setPowerOffCount(count);
     }
 
     public int getLastUncommittedDelete() {
 
         return lastUncommittedDelete;
     }
 
     public void setLastUncommittedDelete(final int deleteId) {
 
         lastUncommittedDelete = deleteId;
     }
 
     /**
      * Commit the current transaction. If the statement was not a data definition statement, and if there are temporary tables that should
      * be dropped or truncated at commit, this is done as well.
      * 
      * @param ddl
      *            if the statement was a data definition statement
      */
     public void commit(final boolean ddl) throws SQLException {
 
         commit(ddl, false);
     }
 
     /**
      * Commit the current transaction. If the statement was not a data definition statement, and if there are temporary tables that should
      * be dropped or truncated at commit, this is done as well.
      * 
      * @param ddl
      *            if the statement was a data definition statement
      * @param hasAlreadyCommittedQueryProxy
      *            true if the calling command/method has already called commit on the transactions queryProxyManager object, meaning it
      *            shouldn't be called again.
      */
     public void commit(final boolean ddl, final boolean hasAlreadyCommittedQueryProxy) throws SQLException {
 
         checkCommitRollback();
         lastUncommittedDelete = 0;
         currentTransactionName = null;
         if (containsUncommitted()) {
             // need to commit even if rollback is not possible
             // (create/drop table and so on)
             logSystem.commit(this);
         }
         if (undoLog.size() > 0) {
             undoLog.clear();
         }
         if (!ddl) {
             // do not clean the temp tables if the last command was a
             // create/drop
             cleanTempTables(false);
             if (autoCommitAtTransactionEnd) {
                 setApplicationAutoCommit(true);
                 autoCommitAtTransactionEnd = false;
             }
         }
         if (unlinkMap != null && unlinkMap.size() > 0) {
             // need to flush the log file, because we can't unlink lobs if the
             // commit record is not written
             logSystem.flush();
             final Iterator<ValueLob> it = unlinkMap.values().iterator();
             while (it.hasNext()) {
                 final Value v = it.next();
                 v.unlink();
             }
             unlinkMap = null;
         }
 
         logSystem.flush();
 
         unlockAllH2Locks();
 
         if (proxyManagerForCurrentTransaction != null && !ddl && !hasAlreadyCommittedQueryProxy) {
             proxyManagerForCurrentTransaction.finishTransaction(true, true, getDatabase());
             proxyManagerForCurrentTransaction = null;
         }
     }
 
     private void checkCommitRollback() throws SQLException {
 
         if (commitOrRollbackDisabled && locks.size() > 0) { throw Message.getSQLException(ErrorCode.COMMIT_ROLLBACK_NOT_ALLOWED); }
     }
 
     /**
      * Fully roll back the current transaction.
      */
     public void rollback() throws SQLException {
 
         checkCommitRollback();
         currentTransactionName = null;
         boolean needCommit = false;
         if (undoLog.size() > 0) {
             rollbackTo(0, false);
             needCommit = true;
         }
         if (locks.size() > 0 || needCommit) {
             logSystem.commit(this);
         }
         cleanTempTables(false);
         unlockAllH2Locks();
         if (autoCommitAtTransactionEnd) {
             setApplicationAutoCommit(true);
             autoCommitAtTransactionEnd = false;
         }
     }
 
     /**
      * Partially roll back the current transaction.
      * 
      * @param index
      *            the position to which should be rolled back
      * @param trimToSize
      *            if the list should be trimmed
      */
     public void rollbackTo(final int index, final boolean trimToSize) throws SQLException {
 
         while (undoLog.size() > index) {
             final UndoLogRecord entry = undoLog.getLast();
             entry.undo(this);
             undoLog.removeLast(trimToSize);
         }
         if (savepoints != null) {
             final String[] names = new String[savepoints.size()];
             savepoints.keySet().toArray(names);
             for (final String name : names) {
                 final Integer id = savepoints.get(name);
                 if (id.intValue() > index) {
                     savepoints.remove(name);
                 }
             }
         }
     }
 
     public int getLogId() {
 
         return undoLog.size();
     }
 
     public int getId() {
 
         return id;
     }
 
     @Override
     public void cancel() {
 
         cancelAt = System.currentTimeMillis();
     }
 
     @Override
     public void close() throws SQLException {
 
         if (!closed) {
             try {
                 cleanTempTables(true);
                 user.sessions--;
 
                 if (user.sessions == 0 && (Constants.IS_NON_SM_TEST || getDatabase().getSystemSession().getUser().sessions == 0)) {
                     final IDatabaseRemote cr = database.getRemoteInterface();
                     cr.shutdown();
                     database.removeSession(this);
                 }
             }
             finally {
                 closed = true;
             }
         }
     }
 
     /**
      * Add a lock for the given table. The object is unlocked on commit or rollback.
      * 
      * @param table
      *            the table that is locked
      */
     public void addLock(final Table table) {
 
         if (SysProperties.CHECK) {
             if (locks.indexOf(table) >= 0) {
                 Message.throwInternalError();
             }
         }
         locks.add(table);
     }
 
     /**
      * Add an undo log entry to this session.
      * 
      * @param table
      *            the table
      * @param type
      *            the operation type (see {@link UndoLogRecord})
      * @param row
      *            the row
      */
     public void log(final Table table, final short type, final Row row) throws SQLException {
 
         log(new UndoLogRecord(table, type, row));
     }
 
     private void log(final UndoLogRecord log) throws SQLException {
 
         // TODO don't understand comment below - why the reference to row insertion?
 
         // called _after_ the row was inserted successfully into the table,
         // otherwise rollback will try to rollback a not-inserted row
 
         // XXX because of exclusive locking at the H2O level, it is assumed that this is not needed.
         if (SysProperties.CHECK) {
             final int lockMode = database.getLockMode();
             if (lockMode != Constants.LOCK_MODE_OFF) {
                 if (locks.indexOf(log.getTable()) < 0 && !Table.TABLE_LINK.equals(log.getTable().getTableType())) {
 
                     /*
                      * Thrown if we try to log something, but a lock isn't held.
                      */
 
                     Message.throwInternalError();
                 }
             }
         }
         // end of check
 
         if (undoLogEnabled) {
             undoLog.add(log);
         }
         else {
             log.commit();
             log.getRow().commit();
         }
     }
 
     /**
      * Unlock all read locks. This is done if the transaction isolation mode is READ_COMMITTED.
      */
     public void unlockH2ReadLocks() {
 
         for (int i = 0; i < locks.size(); i++) {
             final Table t = (Table) locks.get(i);
             if (!t.isLockedExclusively()) {
                 synchronized (database) {
                     t.unlock(this);
                     locks.remove(i);
                 }
                 i--;
             }
         }
     }
 
     private void unlockAllH2Locks() throws SQLException {
 
         if (SysProperties.CHECK) {
             if (undoLog.size() > 0) {
                 Message.throwInternalError();
             }
         }
         database.afterWriting();
         if (locks.size() > 0) {
             synchronized (database) {
                 for (int i = 0; i < locks.size(); i++) {
                     final Table t = (Table) locks.get(i);
                     t.unlock(this);
                 }
                 locks.clear();
             }
         }
         savepoints = null;
 
         if (modificationIdState != modificationId) {
             sessionStateChanged = true;
         }
     }
 
     private void cleanTempTables(final boolean closeSession) throws SQLException {
 
         if (localTempTables != null && localTempTables.size() > 0) {
             final ObjectArray list = new ObjectArray(localTempTables.values());
             for (int i = 0; i < list.size(); i++) {
                 final Table table = (Table) list.get(i);
                 if (closeSession || table.getOnCommitDrop()) {
                     modificationId++;
                     table.setModified();
                     localTempTables.remove(table.getName());
                     table.removeChildrenAndResources(this);
                 }
                 else if (table.getOnCommitTruncate()) {
                     table.truncate(this);
                 }
             }
         }
     }
 
     public Random getRandom() {
 
         if (random == null) {
             random = new Random();
         }
         return random;
     }
 
     @Override
     public Trace getTrace() {
 
         if (traceModuleName == null) {
             traceModuleName = Trace.JDBC + "[" + id + "]";
         }
         if (closed) { return new TraceSystem(null, false).getTrace(traceModuleName); }
         return database.getTrace(traceModuleName);
     }
 
     public void setLastIdentity(final Value last) {
 
         lastIdentity = last;
     }
 
     public Value getLastIdentity() {
 
         return lastIdentity;
     }
 
     /**
      * Called when a log entry for this session is added. The session keeps track of the first entry in the log file that is not yet
      * committed.
      * 
      * @param logId
      *            the log file id
      * @param pos
      *            the position of the log entry in the log file
      */
     public void addLogPos(final int logId, final int pos) {
 
         if (firstUncommittedLog == LogSystem.LOG_WRITTEN) {
             firstUncommittedLog = logId;
             firstUncommittedPos = pos;
         }
     }
 
     public int getFirstUncommittedLog() {
 
         return firstUncommittedLog;
     }
 
     public int getFirstUncommittedPos() {
 
         return firstUncommittedPos;
     }
 
     /**
      * This method is called after the log file has committed this session.
      */
     public void setAllCommitted() {
 
         firstUncommittedLog = LogSystem.LOG_WRITTEN;
         firstUncommittedPos = LogSystem.LOG_WRITTEN;
     }
 
     private boolean containsUncommitted() {
 
         return firstUncommittedLog != LogSystem.LOG_WRITTEN;
     }
 
     /**
      * Create a savepoint that is linked to the current log position.
      * 
      * @param name
      *            the savepoint name
      */
     public void addSavepoint(final String name) {
 
         if (savepoints == null) {
             savepoints = new HashMap<String, Integer>();
         }
         savepoints.put(name, ObjectUtils.getInteger(getLogId()));
     }
 
     /**
      * Undo all operations back to the log position of the given savepoint.
      * 
      * @param name
      *            the savepoint name
      */
     public void rollbackToSavepoint(final String name) throws SQLException {
 
         checkCommitRollback();
         if (savepoints == null) { throw Message.getSQLException(ErrorCode.SAVEPOINT_IS_INVALID_1, name); }
         final Integer id = savepoints.get(name);
         if (id == null) { throw Message.getSQLException(ErrorCode.SAVEPOINT_IS_INVALID_1, name); }
         final int i = id.intValue();
         rollbackTo(i, false);
     }
 
     /**
      * Prepare the given transaction.
      * 
      * @param transactionName
      *            the name of the transaction
      */
     public void prepareCommit(final String transactionName) throws SQLException {
 
         if (containsUncommitted()) {
             // need to commit even if rollback is not possible (create/drop
             // table and so on)
             logSystem.prepareCommit(this, transactionName);
         }
         currentTransactionName = transactionName;
     }
 
     /**
      * Commit or roll back the given transaction.
      * 
      * @param transactionName
      *            the name of the transaction
      * @param commit
      *            true for commit, false for rollback
      */
     public void setPreparedTransaction(final String transactionName, final boolean commit) throws SQLException {
 
         if (currentTransactionName != null && currentTransactionName.equals(transactionName)) {
             if (commit) {
                 commit(false, true);
             }
             else {
                 rollback();
             }
         }
         else {
             final ObjectArray list = logSystem.getInDoubtTransactions();
             final int state = commit ? InDoubtTransaction.COMMIT : InDoubtTransaction.ROLLBACK;
             boolean found = false;
             for (int i = 0; list != null && i < list.size(); i++) {
                 final InDoubtTransaction p = (InDoubtTransaction) list.get(i);
                 if (p.getTransaction().equals(transactionName)) {
                     p.setState(state);
                     found = true;
                     break;
                 }
             }
             if (!found && commit) { // only called on commit because of the way ROLLBACKS could be sent to machines unaware of a problem.
                 throw Message.getSQLException(ErrorCode.TRANSACTION_NOT_FOUND_1, transactionName);
             }
         }
     }
 
     @Override
     public boolean isClosed() {
 
         return closed;
     }
 
     public void setThrottle(final int throttle) {
 
         this.throttle = throttle;
     }
 
     /**
      * Wait for some time if this session is throttled (slowed down).
      */
     public void throttle() {
 
         if (throttle == 0) { return; }
         final long time = System.currentTimeMillis();
         if (lastThrottle + Constants.THROTTLE_DELAY > time) { return; }
         lastThrottle = time + throttle;
         try {
             Thread.sleep(throttle);
         }
         catch (final Exception e) {
             // ignore
         }
     }
 
     /**
      * Set the current command of this session. This is done just before executing the statement.
      * 
      * @param command
      *            the command
      * @param startTime
      *            the time execution has been started
      */
     public void setCurrentCommand(final Command command, final long startTime) {
 
         currentCommand = command;
         currentCommandStart = startTime;
         if (queryTimeout > 0 && startTime != 0) {
             cancelAt = startTime + queryTimeout;
         }
     }
 
     /**
      * Check if the current transaction is canceled by calling Statement.cancel() or because a session timeout was set and expired.
      * 
      * @throws SQLException
      *             if the transaction is canceled
      */
     public void checkCanceled() throws SQLException {
 
         throttle();
         if (cancelAt == 0) { return; }
         final long time = System.currentTimeMillis();
         if (time >= cancelAt) {
             cancelAt = 0;
             throw Message.getSQLException(ErrorCode.STATEMENT_WAS_CANCELED);
         }
     }
 
     public Command getCurrentCommand() {
 
         return currentCommand;
     }
 
     public long getCurrentCommandStart() {
 
         return currentCommandStart;
     }
 
     public boolean getAllowLiterals() {
 
         return allowLiterals;
     }
 
     public void setAllowLiterals(final boolean b) {
 
         allowLiterals = b;
     }
 
     public void setCurrentSchema(final Schema schema) {
 
         modificationId++;
         currentSchemaName = schema.getName();
     }
 
     public String getCurrentSchemaName() {
 
         return currentSchemaName;
     }
 
     /**
      * Create an internal connection. This connection is used when initializing triggers, and when calling user defined functions.
      * 
      * @param columnList
      *            if the url should be 'jdbc:columnlist:connection'
      * @return the internal connection
      */
     public JdbcConnection createConnection(final boolean columnList) {
 
         String url;
         if (columnList) {
             url = Constants.CONN_URL_COLUMNLIST;
         }
         else {
             url = Constants.CONN_URL_INTERNAL;
         }
         return new JdbcConnection(this, getUser().getName(), url);
     }
 
     @Override
     public DataHandler getDataHandler() {
 
         return database;
     }
 
     /**
      * Remember that the given LOB value must be un-linked (disconnected from the table) at commit.
      * 
      * @param v
      *            the value
      */
     public void unlinkAtCommit(final ValueLob v) {
 
         if (SysProperties.CHECK && !v.isLinked()) {
             Message.throwInternalError();
         }
         if (unlinkMap == null) {
             unlinkMap = new HashMap<String, ValueLob>();
         }
         unlinkMap.put(v.toString(), v);
     }
 
     /**
      * Do not unlink this LOB value at commit any longer.
      * 
      * @param v
      *            the value
      */
     public void unlinkAtCommitStop(final Value v) {
 
         if (unlinkMap != null) {
             unlinkMap.remove(v.toString());
         }
     }
 
     /**
      * Get the next system generated identifiers. The identifier returned does not occur within the given SQL statement.
      * 
      * @param sql
      *            the SQL statement
      * @return the new identifier
      */
     public String getNextSystemIdentifier(final String sql) {
 
         String id;
         do {
             id = SYSTEM_IDENTIFIER_PREFIX + systemIdentifier++;
         }
         while (sql.indexOf(id) >= 0);
         return id;
     }
 
     /**
      * Add a procedure to this session.
      * 
      * @param procedure
      *            the procedure to add
      */
     public void addProcedure(final Procedure procedure) {
 
         if (procedures == null) {
             procedures = new HashMap<String, Procedure>();
         }
         procedures.put(procedure.getName(), procedure);
     }
 
     /**
      * Remove a procedure from this session.
      * 
      * @param name
      *            the name of the procedure to remove
      */
     public void removeProcedure(final String name) {
 
         if (procedures != null) {
             procedures.remove(name);
         }
     }
 
     /**
      * Get the procedure with the given name, or null if none exists.
      * 
      * @param name
      *            the procedure name
      * @return the procedure or null
      */
     public Procedure getProcedure(final String name) {
 
         if (procedures == null) { return null; }
         return procedures.get(name);
     }
 
     public void setSchemaSearchPath(final String[] schemas) {
 
         modificationId++;
         schemaSearchPath = schemas;
     }
 
     public String[] getSchemaSearchPath() {
 
         return schemaSearchPath;
     }
 
     @Override
     public int hashCode() {
 
        return user.getName().hashCode();
     }
 
     @Override
    public boolean equals(final Object obj) {
 
        Diagnostic.trace(">>>>>>>>>>> Session.equals() called");
        Diagnostic.printStackTrace();
 
        return user.getName().equals(((Session) obj).getUser().getName());
     }
 
     @Override
     public String toString() {
 
         return "#" + serialId + " (user: " + user.getName() + ")";
     }
 
     public void setUndoLogEnabled(final boolean b) {
 
         undoLogEnabled = b;
     }
 
     public boolean getUndoLogEnabled() {
 
         return undoLogEnabled;
     }
 
     /**
      * Begin a transaction.
      */
     public void begin() {
 
         autoCommitAtTransactionEnd = getApplicationAutoCommit();
         setApplicationAutoCommit(false);
     }
 
     public long getSessionStart() {
 
         return sessionStart;
     }
 
     public Table[] getLocks() {
 
         synchronized (database) {
             final Table[] list = new Table[locks.size()];
             locks.toArray(list);
             return list;
         }
     }
 
     /**
      * Wait if the exclusive mode has been enabled for another session. This method returns as soon as the exclusive mode has been disabled.
      */
     public void waitIfExclusiveModeEnabled() {
 
         while (true) {
             final Session exclusive = database.getExclusiveSession();
             if (exclusive == null || exclusive == this) {
                 break;
             }
             try {
                 Thread.sleep(100);
             }
             catch (final InterruptedException e) {
                 // ignore
             }
         }
     }
 
     /**
      * Remember the result set and close it as soon as the transaction is committed (if it needs to be closed). This is done to delete
      * temporary files as soon as possible.
      * 
      * @param result
      *            the temporary result set
      */
     public void addTemporaryResult(final LocalResult result) {
 
         if (!result.needToClose()) { return; }
         if (temporaryResults == null) {
             temporaryResults = new HashSet<LocalResult>();
         }
         if (temporaryResults.size() < 100) {
             // reference at most 100 result sets to avoid memory problems
             temporaryResults.add(result);
         }
     }
 
     /**
      * Close all temporary result set. This also deletes all temporary files held by the result sets.
      */
     public void closeTemporaryResults() {
 
         if (temporaryResults != null) {
             for (final LocalResult result : temporaryResults) {
                 result.close();
             }
             temporaryResults = null;
         }
     }
 
     public void setQueryTimeout(int queryTimeout) {
 
         final int max = SysProperties.getMaxQueryTimeout();
         if (max != 0 && (max < queryTimeout || queryTimeout == 0)) {
             // the value must be at most max
             queryTimeout = max;
         }
         this.queryTimeout = queryTimeout;
         // must reset the cancel at here,
         // otherwise it is still used
         cancelAt = 0;
     }
 
     public int getQueryTimeout() {
 
         return queryTimeout;
     }
 
     public void setWaitForLock(final Table table) {
 
         waitForLock = table;
     }
 
     public Table getWaitForLock() {
 
         return waitForLock;
     }
 
     public int getModificationId() {
 
         return modificationId;
     }
 
     @Override
     public boolean isReconnectNeeded() {
 
         return database.isReconnectNeeded();
     }
 
     @Override
     public SessionInterface reconnect() throws SQLException {
 
         readSessionState();
         close();
         final Session newSession = Engine.getInstance().getSession(connectionInfo);
         newSession.sessionState = sessionState;
         newSession.recreateSessionState();
         return newSession;
     }
 
     public void setConnectionInfo(final ConnectionInfo ci) {
 
         connectionInfo = ci;
     }
 
     public Value getTransactionId() {
 
         if (undoLog.size() == 0 || !database.isPersistent()) { return ValueNull.INSTANCE; }
         return ValueString.get(firstUncommittedLog + "-" + firstUncommittedPos + "-" + id);
     }
 
     public void setApplicationAutoCommit(final boolean applicationAutoCommit) {
 
         this.applicationAutoCommit = applicationAutoCommit;
     }
 
     public boolean getApplicationAutoCommit() {
 
         return applicationAutoCommit;
     }
 
     /**
      * @return the currentTransactionLocks
      */
     public TableProxyManager getProxyManager() {
 
         return proxyManagerForCurrentTransaction;
     }
 
     /**
      * Returns a proxy manager for a transaction. If there is an active transaction an existing proxy manager will be returned. Otherwise a
      * new proxy manager will be created, indicating the start of a new transaction.
      * 
      * @return
      */
     public TableProxyManager getProxyManagerForTransaction() {
 
         if (proxyManagerForCurrentTransaction == null) {
             proxyManagerForCurrentTransaction = new TableProxyManager(getDatabase(), this);
             Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "New transaction started: " + proxyManagerForCurrentTransaction.getTransactionName());
         }
         return proxyManagerForCurrentTransaction;
     }
 
     /**
      * Completes the transaction by resetting the TableProxyManager for this session.
      */
     public void completeTransaction() {
 
         proxyManagerForCurrentTransaction = new TableProxyManager(getDatabase(), this);
     }
 
     public int getSerialID() {
 
         return serialId;
     }
 }
