 /*
  * Copyright (c) 2013, Timothy Stack
  *
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  *
  *  Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer.
  *  Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution.
  *  Neither the name of Timothy Stack nor the names of its contributors
  * may be used to endorse or promote products derived from this software
  * without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE AUTHORS AND CONTRIBUTORS ''AS IS'' AND ANY
  * EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
  * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.sqlite.jdbcng;
 
 import org.bridj.Pointer;
 import org.sqlite.jdbcng.bridj.Sqlite3;
 import org.sqlite.jdbcng.internal.TimeoutProgressCallback;
 
 import java.sql.*;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import static java.util.logging.Level.WARNING;
 
 public class SqliteStatement extends SqliteCommon implements Statement {
     private static final Logger LOGGER = Logger.getLogger(SqliteConnection.class.getName());
 
     protected final SqliteConnection conn;
     protected final List<String> batchList = new ArrayList<>();
     protected int queryTimeoutSeconds;
     protected boolean closeOnCompletion;
     protected String lastQuery;
     protected int maxRows;
     protected SqliteResultSet lastResult;
     protected int lastUpdateCount;
     protected boolean closed;
     protected final TimeoutProgressCallback timeoutCallback;
 
     public SqliteStatement(SqliteConnection conn) {
         this.conn = conn;
         this.timeoutCallback = new TimeoutProgressCallback(conn);
     }
 
     Pointer<Sqlite3.Sqlite3Db> getDbHandle() {
         return this.conn.getHandle();
     }
 
     void requireOpened() throws SQLException {
         if (this.isClosed()) {
             throw new SQLNonTransientException("Statement is closed for business");
         }
     }
 
     void resultSetClosed() throws SQLException {
         if (this.closeOnCompletion) {
             this.close();
         }
     }
 
     void replaceResultSet(SqliteResultSet rs) throws SQLException {
         if (this.lastResult != null) {
             this.lastResult.close();
         }
         this.lastResult = rs;
     }
 
     @Override
     public ResultSet executeQuery(String s) throws SQLException {
         requireOpened();
 
         Pointer<Pointer<Sqlite3.Statement>> stmt_out = Pointer.allocatePointer(Sqlite3.Statement.class);
 
         this.clearWarnings();
 
         this.lastQuery = s;
 
         Sqlite3.checkOk(Sqlite3.sqlite3_prepare_v2(this.conn.getHandle(),
                 Pointer.pointerToCString(s), -1, stmt_out, Pointer.NULL),
                 this.conn.getHandle());
 
         Pointer<Sqlite3.Statement> stmt = Sqlite3.withReleaser(stmt_out.get());
 
         if (Sqlite3.sqlite3_stmt_readonly(stmt) == 0)
             throw new SQLNonTransientException("SQL statement is not a query");
 
         this.replaceResultSet(new SqliteResultSet(this, stmt, this.maxRows));
 
         return this.lastResult;
     }
 
     @Override
     public int executeUpdate(String s) throws SQLException {
         if (this.execute(s)) {
             try (ResultSet rs = this.getResultSet()) {
                 /*
                  * Certain statements are read-only, but are not SELECT
                  * queries.  For example, adding another database to a
                  * connection with "ATTACH".  This means we need to get
                  * the result set and execute it at least once.
                  */
                 if (rs.next()) {
                     LOGGER.log(Level.WARNING,
                             "executeUpdate used with a statement that is returning results -- {0}",
                             new Object[] { s });
                 }
             }
         }
 
         return this.lastUpdateCount;
     }
 
     @Override
     public void close() throws SQLException {
         if (this.lastResult != null) {
             Pointer<Sqlite3.Statement> stmt = this.lastResult.getHandle();
 
             this.lastResult.close();
             this.lastResult = null;
             stmt.release();
             this.conn.statementClosed(this);
         }
 
         this.closed = true;
     }
 
     @Override
     public int getMaxFieldSize() throws SQLException {
         return 0;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void setMaxFieldSize(int i) throws SQLException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public int getMaxRows() throws SQLException {
         requireOpened();
 
         return this.maxRows;
     }
 
     @Override
     public void setMaxRows(int i) throws SQLException {
         requireOpened();
 
         if (i < 0)
             throw new SQLNonTransientException("maxRows must be greater than or equal to zero");
 
         this.maxRows = i;
     }
 
     @Override
     public void setEscapeProcessing(boolean b) throws SQLException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public int getQueryTimeout() throws SQLException {
         requireOpened();
 
         return this.queryTimeoutSeconds;
     }
 
     /**
      * Sets a timeout for any executed statements.
      *
      * Implementation details:
      * - The timeout is implemented through the progress callback
      *   mechanism in SQLite, so it applies to all statements concurrently
      *   running on a connection.  However, connections should not be shared
      *   across threads, so this shouldn't be a problem.
      * - For queries executed through executeQuery(), the statement is not
      *   executed until ResultSet.next() is run and the timeout is applied
      *   at that time.  The next() method will also be the one throwing the
      *   exception.
      * - If a timeout is applied to a statement and cancel() is called, the
      *   driver will throw a SQLTimeoutException since the SQLite library
      *   does not distinguish between sqlite3_interrupt() being called and
      *   a progress handler returning a non-zero return code.
      *
      * {@inheritDoc}
      */
     @Override
     public void setQueryTimeout(int seconds) throws SQLException {
         requireOpened();
 
         if (seconds < 0)
             throw new SQLNonTransientException("Timeout must be greater than or equal to zero");
 
         this.queryTimeoutSeconds = seconds;
     }
 
     @Override
     public void cancel() throws SQLException {
         requireOpened();
 
         Sqlite3.sqlite3_interrupt(this.conn.getHandle());
     }
 
     @Override
     public void setCursorName(String s) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support named cursors");
     }
 
     @Override
     public boolean execute(String s) throws SQLException {
         requireOpened();
 
         Pointer<Pointer<Sqlite3.Statement>> stmt_out = Pointer.allocatePointer(Sqlite3.Statement.class);
 
         this.clearWarnings();
 
         this.lastQuery = s;
         Sqlite3.checkOk(Sqlite3.sqlite3_prepare_v2(this.conn.getHandle(),
                 Pointer.pointerToCString(s), -1, stmt_out, Pointer.NULL),
                 this.conn.getHandle());
 
         Pointer<Sqlite3.Statement> stmt = Sqlite3.withReleaser(stmt_out.get());
 
         try {
             if (Sqlite3.sqlite3_stmt_readonly(stmt) != 0) {
                 this.replaceResultSet(new SqliteResultSet(this, stmt, this.maxRows));
                 stmt = null;
             }
             else {
                 int rc;
 
                 try (TimeoutProgressCallback cb = this.timeoutCallback.setExpiration(
                         this.getQueryTimeout() * 1000)) {
                     rc = Sqlite3.sqlite3_step(stmt);
                     if (cb != null && rc == Sqlite3.ReturnCodes.SQLITE_INTERRUPT.value()) {
                         throw new SQLTimeoutException("Query timeout reached");
                     }
                 }
 
                 switch (Sqlite3.ReturnCodes.valueOf(rc)) {
                     case SQLITE_OK:
                     case SQLITE_DONE:
                         break;
                     default:
                         Sqlite3.checkOk(rc, this.conn.getHandle());
                         break;
                 }
                 this.replaceResultSet(null);
             }
         }
         finally {
             Pointer.release(stmt);
         }
 
         this.lastUpdateCount = Sqlite3.sqlite3_changes(this.conn.getHandle());
 
         return this.lastResult != null;
     }
 
     @Override
     public ResultSet getResultSet() throws SQLException {
         requireOpened();
 
         return this.lastResult;
     }
 
     @Override
     public int getUpdateCount() throws SQLException {
         requireOpened();
 
         return this.lastUpdateCount;
     }
 
     @Override
     public boolean getMoreResults() throws SQLException {
         requireOpened();
 
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void setFetchDirection(int direction) throws SQLException {
         requireOpened();
 
         if (direction != ResultSet.FETCH_FORWARD)
             throw new SQLFeatureNotSupportedException("SQLite result sets only support forward fetching");
     }
 
     @Override
     public int getFetchDirection() throws SQLException {
         requireOpened();
 
         return ResultSet.FETCH_FORWARD;
     }
 
     @Override
     public void setFetchSize(int i) throws SQLException {
         requireOpened();
     }
 
     @Override
     public int getFetchSize() throws SQLException {
         requireOpened();
 
         return 0;
     }
 
     @Override
     public int getResultSetConcurrency() throws SQLException {
         requireOpened();
 
         return ResultSet.CONCUR_READ_ONLY;
     }
 
     @Override
     public int getResultSetType() throws SQLException {
         requireOpened();
 
         return ResultSet.TYPE_FORWARD_ONLY;
     }
 
     @Override
     public void addBatch(String s) throws SQLException {
         requireOpened();
 
         this.batchList.add(s);
     }
 
     @Override
     public void clearBatch() throws SQLException {
         requireOpened();
 
         this.batchList.clear();
     }
 
     @Override
     public int[] executeBatch() throws SQLException {
         String[] batchCopy = this.batchList.toArray(new String[this.batchList.size()]);
         int[] retval = new int[batchCopy.length];
         int index = 0;
 
         this.batchList.clear();
 
         for (String sql : batchCopy) {
             try {
                 if (this.execute(sql)) {
                     retval[index] = SUCCESS_NO_INFO;
                 }
                 else {
                     retval[index] = this.lastUpdateCount;
                 }
             }
             catch (SQLException e) {
                 throw new BatchUpdateException(e);
             }
             index += 1;
         }
 
         return retval;
     }
 
     @Override
     public Connection getConnection() throws SQLException {
         requireOpened();
 
         return this.conn;
     }
 
     @Override
     public boolean getMoreResults(int i) throws SQLException {
         requireOpened();
 
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public ResultSet getGeneratedKeys() throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support generated keys");
     }
 
     @Override
     public int executeUpdate(String s, int i) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public int executeUpdate(String s, int[] ints) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public int executeUpdate(String s, String[] strings) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public boolean execute(String s, int i) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public boolean execute(String s, int[] ints) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public boolean execute(String s, String[] strings) throws SQLException {
         throw new SQLFeatureNotSupportedException("SQLite does not support returning generated keys");
     }
 
     @Override
     public int getResultSetHoldability() throws SQLException {
         requireOpened();
 
         return ResultSet.CLOSE_CURSORS_AT_COMMIT;
     }
 
     @Override
     public boolean isClosed() throws SQLException {
         return this.closed;
     }
 
     @Override
     public void setPoolable(boolean b) throws SQLException {
         //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public boolean isPoolable() throws SQLException {
         return false;  //To change body of implemented methods use File | Settings | File Templates.
     }
 
     @Override
     public void closeOnCompletion() throws SQLException {
         requireOpened();
 
         this.closeOnCompletion = true;
     }
 
     @Override
     public boolean isCloseOnCompletion() throws SQLException {
         requireOpened();
 
         return this.closeOnCompletion;
     }
 
     @Override
     protected void finalize() throws Throwable {
         super.finalize();
 
         if (!this.closed) {
             LOGGER.log(WARNING,
                     "SQLite database statement was not explicitly closed -- {0}",
                     this.conn.getURL());
 
             this.close();
         }
     }
 
     @Override
     public String toString() {
         return this.lastQuery;
     }
 }
