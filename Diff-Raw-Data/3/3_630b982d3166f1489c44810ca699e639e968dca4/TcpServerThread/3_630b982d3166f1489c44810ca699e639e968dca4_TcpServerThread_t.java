 /*
  * Copyright 2004-2009 H2 Group. Multiple-Licensed under the H2 License, Version 1.0, and under the Eclipse Public License, Version 1.0
  * (http://h2database.com/html/license.html). Initial Developer: H2 Group
  */
 package org.h2.server;
 
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringWriter;
 import java.net.Socket;
 import java.sql.SQLException;
 
 import org.h2.command.Command;
 import org.h2.constant.ErrorCode;
 import org.h2.constant.SysProperties;
 import org.h2.engine.ConnectionInfo;
 import org.h2.engine.Constants;
 import org.h2.engine.Engine;
 import org.h2.engine.Session;
 import org.h2.engine.SessionRemote;
 import org.h2.expression.Parameter;
 import org.h2.expression.ParameterRemote;
 import org.h2.jdbc.JdbcSQLException;
 import org.h2.message.Message;
 import org.h2.result.LocalResult;
 import org.h2.result.ResultColumn;
 import org.h2.util.ObjectArray;
 import org.h2.util.SmallMap;
 import org.h2.util.StringUtils;
 import org.h2.value.Transfer;
 import org.h2.value.Value;
 
 import uk.ac.standrews.cs.nds.util.Diagnostic;
 import uk.ac.standrews.cs.nds.util.DiagnosticLevel;
 
 /**
  * One server thread is opened per client connection.
  */
 public class TcpServerThread implements Runnable {
 
     private final TcpServer server;
 
     private Session session;
 
     private boolean stop;
 
     private Thread thread;
 
     private final Transfer transfer;
 
     private Command commit;
 
     private final SmallMap cache = new SmallMap(SysProperties.SERVER_CACHED_OBJECTS);
 
     private final int id;
 
     private int clientVersion;
 
     private String sessionId;
 
     TcpServerThread(final Socket socket, final TcpServer server, final int id) {
 
         this.server = server;
         this.id = id;
         transfer = new Transfer(null);
         transfer.setSocket(socket);
     }
 
     private void trace(final String s) {
 
         server.trace(this + " " + s);
     }
 
     @Override
     public void run() {
 
         try {
             transfer.init();
             trace("Connect");
             // TODO server: should support a list of allowed databases
             // and a list of allowed clients
             try {
                 clientVersion = transfer.readInt();
                 if (!server.allow(transfer.getSocket())) { throw Message.getSQLException(ErrorCode.REMOTE_CONNECTION_NOT_ALLOWED); }
                 if (clientVersion >= Constants.TCP_PROTOCOL_VERSION_6) {
                     // version 6 and newer: read max version (currently not
                     // used)
                     transfer.readInt();
                 }
                 else if (clientVersion != Constants.TCP_PROTOCOL_VERSION_5) { throw Message.getSQLException(ErrorCode.DRIVER_VERSION_ERROR_2, new String[]{"" + clientVersion, "" + Constants.TCP_PROTOCOL_VERSION_5}); }
                 String db = transfer.readString();
                 final String originalURL = transfer.readString();
                 if (db == null && originalURL == null) {
                     final String sessionId = transfer.readString();
                     final int command = transfer.readInt();
                     stop = true;
                     if (command == SessionRemote.SESSION_CANCEL_STATEMENT) {
                         // cancel a running statement
                         final int statementId = transfer.readInt();
                         server.cancelStatement(sessionId, statementId);
                     }
                     else if (command == SessionRemote.SESSION_CHECK_KEY) {
                         // check if this is the correct server
                         db = server.checkKeyAndGetDatabaseName(sessionId);
                         if (!sessionId.equals(db)) {
                             transfer.writeInt(SessionRemote.STATUS_OK);
                         }
                         else {
                             transfer.writeInt(SessionRemote.STATUS_ERROR);
                         }
                     }
                 }
                 String baseDir = server.getBaseDir();
                 if (baseDir == null) {
                     baseDir = SysProperties.getBaseDir();
                 }
                 db = server.checkKeyAndGetDatabaseName(db);
                 final ConnectionInfo ci = new ConnectionInfo(db, server.getPort(), server.getSystemTableLocation());
                 if (baseDir != null) {
                     ci.setBaseDir(baseDir);
                 }
                 if (server.getIfExists()) {
                     ci.setProperty("IFEXISTS", "TRUE");
                 }
                 ci.setOriginalURL(originalURL);
                 ci.setUserName(transfer.readString());
                 ci.setUserPasswordHash(transfer.readBytes());
                 ci.setFilePasswordHash(transfer.readBytes());
                 final int len = transfer.readInt();
                 for (int i = 0; i < len; i++) {
                     ci.setProperty(transfer.readString(), transfer.readString());
                 }
                 final Engine engine = Engine.getInstance();
                 session = engine.getSession(ci);
                 transfer.setSession(session);
                 transfer.writeInt(SessionRemote.STATUS_OK);
                 if (clientVersion >= Constants.TCP_PROTOCOL_VERSION_6) {
                     // version 6: reply what version to use
                     transfer.writeInt(Constants.TCP_PROTOCOL_VERSION_6);
                 }
                 transfer.flush();
                 server.addConnection(id, originalURL, ci.getUserName());
                 trace("Connected");
             }
             catch (final Throwable e) {
                 sendError(e);
                 stop = true;
             }
             while (!stop) {
                 try {
                     process();
                 }
                 catch (final Throwable e) {
                     sendError(e);
                 }
             }
             trace("Disconnect");
         }
         catch (final Throwable e) {
             server.traceError(e);
         }
         finally {
             close();
         }
     }
 
     private void closeSession() {
 
         if (session != null) {
             try {
                 final Command rollback = session.prepareLocal("ROLLBACK");
                 rollback.executeUpdate();
                 session.close();
                 server.removeConnection(id);
             }
             catch (final Exception e) {
                 server.traceError(e);
             }
             finally {
                 session = null;
             }
         }
     }
 
     /**
      * Close a connection.
      */
     void close() {
 
         try {
             stop = true;
             closeSession();
             transfer.close();
             trace("Close");
         }
         catch (final Exception e) {
             server.traceError(e);
         }
         server.remove(this);
     }
 
     private void sendError(final Throwable e) {
 
         try {
             final SQLException s = Message.convert(e);
             final StringWriter writer = new StringWriter();
             e.printStackTrace(new PrintWriter(writer));
             final String trace = writer.toString();
             String message;
             String sql;
             if (e instanceof JdbcSQLException) {
                 final JdbcSQLException j = (JdbcSQLException) e;
                 message = j.getOriginalMessage();
                 sql = j.getSQL();
             }
             else {
                 message = e.getMessage();
                 sql = null;
             }
             transfer.writeInt(SessionRemote.STATUS_ERROR).writeString(s.getSQLState()).writeString(message).writeString(sql).writeInt(s.getErrorCode()).writeString(trace).flush();
         }
         catch (final IOException e2) {
             server.traceError(e2);
             // if writing the error does not work, close the connection
             stop = true;
         }
     }
 
     private void setParameters(final Command command) throws IOException, SQLException {
 
         final int len = transfer.readInt();
         final ObjectArray params = command.getParameters();
         for (int i = 0; i < len; i++) {
             final Parameter p = (Parameter) params.get(i);
             p.setValue(transfer.readValue());
         }
     }
 
     private void process() throws IOException, SQLException {
 
         final int operation = transfer.readInt();
         switch (operation) {
             case SessionRemote.SESSION_PREPARE_READ_PARAMS:
             case SessionRemote.SESSION_PREPARE: {
                 final int id = transfer.readInt();
                 final String sql = transfer.readString();
 
                 Diagnostic.traceNoEvent(DiagnosticLevel.FULL, "Incoming Query from TCP Server: " + sql);
 
                 final int old = session.getModificationId();
                 final Command command = session.prepareLocal(sql);
                 final boolean readonly = command.isReadOnly();
                 cache.addObject(id, command);
                 final boolean isQuery = command.isQuery();
                 final ObjectArray params = command.getParameters();
                 final int paramCount = params.size();
                 transfer.writeInt(getState(old)).writeBoolean(isQuery).writeBoolean(readonly).writeInt(paramCount);
                 if (operation == SessionRemote.SESSION_PREPARE_READ_PARAMS) {
                     for (int i = 0; i < paramCount; i++) {
                         final Parameter p = (Parameter) params.get(i);
                         ParameterRemote.writeMetaData(transfer, p);
                     }
                 }
                 transfer.flush();
                 break;
             }
             case SessionRemote.SESSION_CLOSE: {
                 closeSession();
                 transfer.writeInt(SessionRemote.STATUS_OK).flush();
                 close();
                 break;
             }
             case SessionRemote.COMMAND_COMMIT: {
                 if (commit == null) {
                     commit = session.prepareLocal("COMMIT");
                 }
                 final int old = session.getModificationId();
                 commit.executeUpdate();
                 transfer.writeInt(getState(old)).flush();
                 break;
             }
             case SessionRemote.COMMAND_GET_META_DATA: {
                 final int id = transfer.readInt();
                 final int objectId = transfer.readInt();
                 final Command command = (Command) cache.getObject(id, false);
 
                 final LocalResult result = command.getMetaDataLocal();
                 cache.addObject(objectId, result);
                 final int columnCount = result.getVisibleColumnCount();
                 transfer.writeInt(SessionRemote.STATUS_OK).writeInt(columnCount).writeInt(0);
                 for (int i = 0; i < columnCount; i++) {
                     ResultColumn.writeColumn(transfer, result, i);
                 }
                 transfer.flush();
                 break;
             }
             case SessionRemote.COMMAND_EXECUTE_QUERY: {
                 final int id = transfer.readInt();
                 final int objectId = transfer.readInt();
                 final int maxRows = transfer.readInt();
                 final int fetchSize = transfer.readInt();
                 final Command command = (Command) cache.getObject(id, false);
                 setParameters(command);
                 final int old = session.getModificationId();
                 final LocalResult result = command.executeQueryLocal(maxRows);
                 cache.addObject(objectId, result);
                 final int columnCount = result.getVisibleColumnCount();
                 final int state = getState(old);
                 transfer.writeInt(state).writeInt(columnCount);
                 final int rowCount = result.getRowCount();
                 transfer.writeInt(rowCount);
                 for (int i = 0; i < columnCount; i++) {
                     ResultColumn.writeColumn(transfer, result, i);
                 }
                 final int fetch = Math.min(rowCount, fetchSize);
                 for (int i = 0; i < fetch; i++) {
                     sendRow(result);
                 }
                 transfer.flush();
                 break;
             }
             case SessionRemote.COMMAND_EXECUTE_UPDATE: {
                 final int id = transfer.readInt();
                 final Command command = (Command) cache.getObject(id, false);
 
                 setParameters(command);
                 final int old = session.getModificationId();
                 final int updateCount = command.update();
                 int status;
                 if (session.isClosed()) {
                     status = SessionRemote.STATUS_CLOSED;
                 }
                 else {
                     status = getState(old);
                 }
                 transfer.writeInt(status).writeInt(updateCount).writeBoolean(session.getApplicationAutoCommit());
                 transfer.flush();
                 break;
             }
             case SessionRemote.COMMAND_CLOSE: {
                 final int id = transfer.readInt();
                 final Command command = (Command) cache.getObject(id, true);
                 if (command != null) {
                     command.close();
                     cache.freeObject(id);
                 }
                 break;
             }
             case SessionRemote.RESULT_FETCH_ROWS: {
                 final int id = transfer.readInt();
                 final int count = transfer.readInt();
                 final LocalResult result = (LocalResult) cache.getObject(id, false);
                 transfer.writeInt(SessionRemote.STATUS_OK);
                 for (int i = 0; i < count; i++) {
                     sendRow(result);
                 }
                 transfer.flush();
                 break;
             }
             case SessionRemote.RESULT_RESET: {
                 final int id = transfer.readInt();
                 final LocalResult result = (LocalResult) cache.getObject(id, false);
                 result.reset();
                 break;
             }
             case SessionRemote.RESULT_CLOSE: {
                 final int id = transfer.readInt();
                 final LocalResult result = (LocalResult) cache.getObject(id, true);
                 if (result != null) {
                     result.close();
                     cache.freeObject(id);
                 }
                 break;
             }
             case SessionRemote.CHANGE_ID: {
                 final int oldId = transfer.readInt();
                 final int newId = transfer.readInt();
                 final Object obj = cache.getObject(oldId, false);
                 cache.freeObject(oldId);
                 cache.addObject(newId, obj);
                 break;
             }
             case SessionRemote.SESSION_SET_ID: {
                 sessionId = transfer.readString();
                 transfer.writeInt(SessionRemote.STATUS_OK).flush();
                 break;
             }
             default:
                 trace("Unknown operation: " + operation);
                 closeSession();
                 close();
         }
     }
 
     private int getState(final int oldModificationId) {
 
         if (session.getModificationId() == oldModificationId) { return SessionRemote.STATUS_OK; }
         return SessionRemote.STATUS_OK_STATE_CHANGED;
     }
 
     private void sendRow(final LocalResult result) throws IOException, SQLException {
 
         if (result.next()) {
             transfer.writeBoolean(true);
             final Value[] v = result.currentRow();
             for (int i = 0; i < result.getVisibleColumnCount(); i++) {
                 transfer.writeValue(v[i]);
             }
         }
         else {
             transfer.writeBoolean(false);
         }
     }
 
     void setThread(final Thread thread) {
 
         this.thread = thread;
     }
 
     Thread getThread() {
 
         return thread;
     }
 
     /**
      * Cancel a running statement.
      * 
      * @param sessionId
      *            the session id
      * @param statementId
      *            the statement to cancel
      */
     void cancelStatement(final String sessionId, final int statementId) throws SQLException {
 
         if (StringUtils.equals(sessionId, this.sessionId)) {
             final Command cmd = (Command) cache.getObject(statementId, false);
             cmd.cancel();
         }
     }
 
 }
