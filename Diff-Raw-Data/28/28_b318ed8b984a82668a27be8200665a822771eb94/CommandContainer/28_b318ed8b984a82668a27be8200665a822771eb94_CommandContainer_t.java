 /*
  * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License, Version 1.0, and under the Eclipse Public License, Version 1.0
  * (http://h2database.com/html/license.html). Initial Developer: H2 Group
  */
 package org.h2.command;
 
 import java.sql.SQLException;
 
 import org.h2.command.dml.Select;
 import org.h2.engine.Constants;
 import org.h2.expression.Parameter;
 import org.h2.result.LocalResult;
 import org.h2.util.ObjectArray;
 import org.h2.value.Value;
 import org.h2o.db.query.TableProxyManager;
 import org.h2o.db.query.locking.LockException;
 import org.h2o.test.fixture.H2OTest;
 
 import uk.ac.standrews.cs.nds.rpc.RPCException;
 import uk.ac.standrews.cs.nds.util.ErrorHandling;
 
 /**
  * Represents a single SQL statements. It wraps a prepared statement.
  */
 public class CommandContainer extends Command {
 
     private Prepared prepared;
 
     CommandContainer(final Parser parser, final String sql, final Prepared prepared) {
 
         super(parser, sql);
         prepared.setCommand(this);
         this.prepared = prepared;
 
     }
 
     @Override
     public ObjectArray getParameters() {
 
         return prepared.getParameters();
     }
 
     @Override
     public boolean isTransactional() {
 
         return prepared.isTransactional();
     }
 
     @Override
     public boolean isQuery() {
 
         return prepared.isQuery();
     }
 
     @Override
     public int executeUpdate() throws SQLException {
 
         return executeUpdate(false);
     }
 
     private void recompileIfRequired() throws SQLException {
 
         if (prepared.needRecompile()) {
             // TODO test with 'always recompile'
             prepared.setModificationMetaId(0);
             final String sql = prepared.getSQL();
             final ObjectArray oldParams = prepared.getParameters();
             final Parser parser = new Parser(session, false);
             prepared = parser.parseOnly(sql);
             final long mod = prepared.getModificationMetaId();
             prepared.setModificationMetaId(0);
             final ObjectArray newParams = prepared.getParameters();
             for (int i = 0; i < newParams.size(); i++) {
                 final Parameter old = (Parameter) oldParams.get(i);
                 if (old.isValueSet()) {
                     final Value v = old.getValue(session);
                     final Parameter p = (Parameter) newParams.get(i);
                     p.setValue(v);
                 }
             }
             prepared.prepare();
             prepared.setModificationMetaId(mod);
         }
     }
 
     @Override
     public LocalResult query(final int maxrows) throws SQLException {
 
         return query(maxrows, false);
     }
 
     @Override
     protected LocalResult query(final int maxrows, final boolean partOfMultiQueryTransaction) throws SQLException {
 
         recompileIfRequired();
 
         start();
         prepared.checkParameters();
 
         /*
          * If this is a SELECT query that does not target any meta-tables then locks must be acquired. If it is something else then no locks
          * are needed.
          */
         if (!prepared.sqlStatement.contains("H2O.") && !prepared.sqlStatement.contains("INFORMATION_SCHEMA.") && !prepared.sqlStatement.contains("SYSTEM_RANGE") && !prepared.sqlStatement.contains("information_schema.") && !prepared.sqlStatement.contains("CALL DATABASE()")
                         && prepared instanceof Select) {
 
             doLock();
         }
 
         final TableProxyManager currentProxyManager = session.getProxyManagerForTransaction();
 
         if (Constants.LOG_INCOMING_UPDATES) {
             System.out.println("> > " + prepared.getSQLIncludingParameters().replace("\n", " "));
         }
 
         try {
             final LocalResult result = prepared.query(maxrows);
             prepared.trace(startTime, result.getRowCount());
             if (session.getApplicationAutoCommit()) {
                 currentProxyManager.releaseLocksAndUpdateReplicaState(null, true);
 
                 session.completeTransaction();
 
             }
             return result;
         }
         catch (final SQLException e) {
             currentProxyManager.releaseLocksAndUpdateReplicaState(null, false);
             throw e;
         }
 
     }
 
     @Override
     public int update() throws SQLException, RPCException {
 
         return update(false);
     }
 
     @Override
     protected int update(final boolean partOfMultiQueryTransaction) throws SQLException, RPCException {
 
         recompileIfRequired();
         start();
         prepared.checkParameters();
         int updateCount;
 
         final boolean singleQuery = !partOfMultiQueryTransaction;
         final boolean transactionCommand = prepared.isTransactionCommand();
 
         if (!transactionCommand) { // Not a prepare or commit.
 
             final TableProxyManager currentProxyManager = session.getProxyManagerForTransaction();
 
             assert currentProxyManager != null;
 
             if (Constants.LOG_INCOMING_UPDATES) {
                 currentProxyManager.addSQL(prepared.getSQLIncludingParameters());
                 System.out.println("> " + prepared.getSQLIncludingParameters().replace("\n", " "));
             }
 
             doLock(); // This throws an SQLException if no lock is found.
 
             try {
 
                 updateCount = prepared.update(currentProxyManager.getTransactionName());
 
                 final boolean commit = true; // An exception would already have been thrown if it should have been a rollback.
 
                 H2OTest.createTableFailure();
 
                 if (singleQuery && session.getApplicationAutoCommit()) {
 
                     // If it is one of a number of queries in the transaction then we must wait for the entire transaction to finish.
                     currentProxyManager.finishTransaction(commit, true, session.getDatabase());
                 }
 
             }
             catch (final SQLException e) {
                 currentProxyManager.finishTransaction(false, true, session.getDatabase());
                 session.rollback();
 
                 throw e;
             }
         }
         else {
             // This is a transaction command. No need to commit such a query.
 
             try {
                 if (Constants.LOG_INCOMING_UPDATES) {
                     System.out.println("> " + prepared.getSQLIncludingParameters());
                 }
 
                 /*
                  * If this is a rollback and there is a proxy manager, release locks on all affected table managers first. The commit
                  * command will execute the ROLLBACK / COMMIT later on.
                  */
                 if (prepared.getSQL().contains("ROLLBACK") && session.getProxyManager() != null) {
                     session.getProxyManager().finishTransaction(false, false, session.getDatabase());
                     updateCount = 0;
                 }
                 else {
                     updateCount = prepared.update();
                 }
 
             }
             catch (final SQLException e) {
                ErrorHandling.errorNoEvent("COMMIT failed to commit to all replicas.");
                //This could also happen because a transaction isn't found (which would happen if there was a bug in H2O.
                //But in the regular non-buggy case it can only happen when a transaction couldn't commit to all replicas.

                 throw e;
             }
         }
 
         prepared.trace(startTime, updateCount);
         return updateCount;
     }
 
     @Override
     public boolean isReadOnly() {
 
         return prepared.isReadOnly();
     }
 
     @Override
     public LocalResult queryMeta() throws SQLException {
 
         return prepared.queryMeta();
     }
 
     @Override
     public void acquireLocks() throws SQLException {
 
         prepared.acquireLocks(session.getProxyManagerForTransaction());
     }
 
     public String getTableName() {
 
         return prepared.table.getFullName();
     }
 
     @Override
     public boolean shouldBePropagated() {
 
         return prepared.shouldBePropagated();
     }
 
     @Override
     public void setIsPreparedStatement(final boolean preparedStatement) {
 
         prepared.setPreparedStatement(preparedStatement);
     }
 
     private void doLock() throws SQLException {
 
         final long max = System.currentTimeMillis() + session.getLockTimeout();
 
         while (true) {
 
             /*
              * Check if lock has been obtained.
              */
             acquireLocks();
 
             if (session.getProxyManager().hasAllLocks()) { return; }
 
             /*
              * Check current time.. wait.
              */
             final long now = System.currentTimeMillis();
             if (now >= max) { throw new LockException("Couldn't obtain locks for all tables involved in query."); }
 
             try {
                 // TODO al says: WTF
                 for (int i = 0; i < 20; i++) {
                     final long free = Runtime.getRuntime().freeMemory();
                     System.gc();
                     final long free2 = Runtime.getRuntime().freeMemory();
                     if (free == free2) {
                         break;
                     }
                 }
 
                 // don't wait too long so that deadlocks are detected early
                 long sleep = Math.min(Constants.DEADLOCK_CHECK, max - now);
                 if (sleep <= 0) {
                     sleep = 1;
                 }
                 Thread.sleep(sleep);
             }
             catch (final InterruptedException e) {
                 // ignore
             }
         }
     }
 }
