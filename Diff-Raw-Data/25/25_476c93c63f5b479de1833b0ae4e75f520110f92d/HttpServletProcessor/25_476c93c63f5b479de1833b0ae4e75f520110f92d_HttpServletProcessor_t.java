 package de.consistec.syncframework.impl.proxy.http_servlet;
 
 import static de.consistec.syncframework.common.i18n.MessageReader.read;
 import static de.consistec.syncframework.common.util.CollectionsUtil.newSyncMap;
 import static de.consistec.syncframework.impl.proxy.http_servlet.SyncRequestHttpParams.ACTION;
 import static de.consistec.syncframework.impl.proxy.http_servlet.SyncRequestHttpParams.CHANGES;
 import static de.consistec.syncframework.impl.proxy.http_servlet.SyncRequestHttpParams.REVISION;
 import static de.consistec.syncframework.impl.proxy.http_servlet.SyncRequestHttpParams.SETTINGS;
 import static de.consistec.syncframework.impl.proxy.http_servlet.SyncRequestHttpParams.THREAD_ID;
 
 import de.consistec.syncframework.common.SyncContext;
 import de.consistec.syncframework.common.TableSyncStrategies;
 import de.consistec.syncframework.common.exception.ContextException;
 import de.consistec.syncframework.common.exception.SerializationException;
 import de.consistec.syncframework.common.exception.ServerStatusException;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.server.ServerStatus;
 import de.consistec.syncframework.common.util.LoggingUtil;
 import de.consistec.syncframework.common.util.StringUtil;
 import de.consistec.syncframework.impl.adapter.ISerializationAdapter;
 import de.consistec.syncframework.impl.adapter.JSONSerializationAdapter;
 import de.consistec.syncframework.impl.commands.ApplyChangesCommand;
 import de.consistec.syncframework.impl.commands.GetChangesCommand;
 import de.consistec.syncframework.impl.commands.GetSchemaCommand;
 import de.consistec.syncframework.impl.commands.RequestCommand;
 import de.consistec.syncframework.impl.commands.ValidateSettingsCommand;
 import de.consistec.syncframework.impl.i18n.Errors;
 import de.consistec.syncframework.impl.i18n.Warnings;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.URLEncoder;
 import java.util.Map;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.sql.DataSource;
 import org.slf4j.MDC;
 import org.slf4j.cal10n.LocLogger;
 
 /**
  * Servlet processor takes received http servlet request and pass the control farther to server sync provider.
  * <p/>
  * In it {@link #execute(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}
  * method, the requests parameters are read, parsed and then appropriate method of
  * ServerContext is invoked. The result is parsed JSON String, append to
  * servlet response object and returned to client.
  *
  * @author Markus Backes
  * @company Consistec Engineering and Consulting GmbH
  * @date 23.07.12 13:15
  * @since 0.0.1-SNAPSHOT
  */
 public class HttpServletProcessor {
 
     //<editor-fold defaultstate="expanded" desc=" Class fields " >
     private static final LocLogger LOGGER = LoggingUtil.createLogger(HttpServletProcessor.class.getCanonicalName());
 
     /**
      * Map that contains command objects to execute when requested.
      */
     protected Map<String, RequestCommand> actionCommands = newSyncMap();
    /**
     * Flag to represent the process running status.
     */
    protected boolean isDebugEnabled = false;
 
     private ISerializationAdapter serializationAdapter;
     private final SyncContext.ServerContext serverContext;
 
 
     //</editor-fold>
 
     //<editor-fold defaultstate="collapsed" desc=" Class constructor " >
 
     /**
      * Creates new instance of servlet processor.
      * <p/>
      * This instance will be using
      * {@link de.consistec.syncframework.common.SyncContext.ServerContext synchronization context}
      * with it's internal database connection.
      *
      * @param isDebugEnabled is process in debugging mode
      * @throws ContextException
      */
     public HttpServletProcessor(boolean isDebugEnabled) throws ContextException {
         serverContext = SyncContext.server();
         serializationAdapter = new JSONSerializationAdapter();
         this.isDebugEnabled = isDebugEnabled;
         initializeActionCommands();
     }
 
     /**
      * Creates new instance of servlet processor.
      * <p/>
      * This instance will be using
      * {@link de.consistec.syncframework.common.SyncContext.ServerContext synchronization context}
      * with provided database connection (e.g. from server pool).
      *
      * @param ds External sql data source.
      * @param isDebugEnabled is process in debugging mode
      * @throws IOException
      * @throws ContextException
      */
     public HttpServletProcessor(DataSource ds, boolean isDebugEnabled) throws ContextException {
         serverContext = SyncContext.server(ds);
         serializationAdapter = new JSONSerializationAdapter();
         this.isDebugEnabled = isDebugEnabled;
         initializeActionCommands();
     }
 
     /**
      * Creates new instance of servlet processor.
      * <p/>
      * This instance will be using
      * {@link de.consistec.syncframework.common.SyncContext.ServerContext synchronization context}
      * with provided database connection (e.g. from server pool).
      *
      * @param tableSyncStrategies syncstrategies for configured tables
      * @param isDebugEnabled is process in debugging mode
      * @throws IOException
      * @throws ContextException
      */
     public HttpServletProcessor(TableSyncStrategies tableSyncStrategies, boolean isDebugEnabled) throws
         ContextException {
         serverContext = SyncContext.server(tableSyncStrategies);
         serializationAdapter = new JSONSerializationAdapter();
         this.isDebugEnabled = isDebugEnabled;
         initializeActionCommands();
     }
 
     /**
      * Puts the supported action commands into actionCommands hashMap.
      */
     private void initializeActionCommands() {
         actionCommands.put(SyncAction.GET_SCHEMA.getStringName(), new GetSchemaCommand());
         actionCommands.put(SyncAction.GET_CHANGES.getStringName(), new GetChangesCommand());
         actionCommands.put(SyncAction.APPLY_CHANGES.getStringName(), new ApplyChangesCommand());
         actionCommands.put(SyncAction.VALIDATE_SETTINGS.getStringName(), new ValidateSettingsCommand());
     }
 
    /**
     * Sets the optional sync strategies for configured server tables.
     *
     * @param tableSyncStrategies optional sync strategies for tables
     */
    public void setTableSyncStrategies(TableSyncStrategies tableSyncStrategies) {
         serverContext.setTableSyncStrategies(tableSyncStrategies);
     }
 
     //</editor-fold>
     //<editor-fold defaultstate="expanded" desc=" Class fmethods " >
 
     /**
      * Parses the request, invokes
      * {@link de.consistec.syncframework.common.SyncContext.ServerContext synchronization context}
      * and returns http response to client.
      *
      * @param req Http request received from client.
      * @param resp Http response from invoking servlet.
      * @throws java.io.IOException Signals that an exception has occurred while getting the response writer.
      * @throws DatabaseAdapterException
      * @throws SerializationException
      */
     public void execute(HttpServletRequest req, HttpServletResponse resp) throws IOException, DatabaseAdapterException,
         SerializationException {
 
         if (!StringUtil.isNullOrEmpty(req.getParameter(THREAD_ID.name()))) {
             MDC.put("thread-id", req.getParameter(THREAD_ID.name()));
         }
 
         if (!StringUtil.isNullOrEmpty(req.getParameter(ACTION.name()))) {
 
             RequestCommand command = actionCommands.get(req.getParameter(ACTION.name()));
             if (command == null) {
                 throw new UnsupportedOperationException(read(Errors.SERVER_UNSUPPORTED_ACTION));
             } else {
                 try {
 
                     HttpRequestParamValues paramValues = new HttpRequestParamValues(serverContext, serializationAdapter,
                         req.getParameter(REVISION.name()), req.getParameter(CHANGES.name()),
                         req.getParameter(SETTINGS.name()));
                     String response = command.execute(paramValues);
                     if (response != null) {
                         String encodedResponse = URLEncoder.encode(response, "UTF-8");
                         resp.getWriter().print(encodedResponse);
                         resp.getWriter().flush();
                     }
                 } catch (SyncException e) {
 
                     if (e instanceof ServerStatusException) {
                         ServerStatusException ex = (ServerStatusException) e;
                         if (ex.getStatus().equals(ServerStatus.CLIENT_NOT_UPTODATE)) {
                             LOGGER.warn(read(Warnings.CANT_APPLY_CHANGES_CLIENT_NOT_UP_TO_DATE));
                             resp.addHeader(HttpServerSyncProxy.HEADER_NAME_SERVER_EXCEPTION,
                                 String.valueOf(ServerStatus.CLIENT_NOT_UPTODATE.getCode()));
                         } else {
                             LOGGER.warn(read(Errors.CANT_APPLY_CHANGES), e);
                         }
                     }
                     writeExceptionOut(resp, e);
                 } catch (SerializationException e) {
                     writeExceptionOut(resp, e);
                 }
             }
         }
     }
 
     private void writeExceptionOut(HttpServletResponse resp, Throwable th) throws IOException {
         if (isDebugEnabled) {
             writeExceptionToHttpOutputStream(resp, th);
         } else {
             String msg = th.getLocalizedMessage();
             Throwable cause = th.getCause();
             if (cause != null) {
                 msg = cause.getLocalizedMessage();
             }
             LOGGER.error(Errors.CANT_EXECUTE_SERVER_COMMAND, msg);
             resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, msg);
         }
     }
 
     private void writeExceptionToHttpOutputStream(HttpServletResponse resp, Throwable th) throws IOException {
 
         resp.addHeader(HttpServerSyncProxy.HEADER_NAME_EXCEPTION, String.valueOf(true));
 
         ObjectOutputStream oos = null;
         try {
             oos = new ObjectOutputStream(resp.getOutputStream());
             oos.writeObject(th);
             oos.flush();
         } catch (IOException e) {
             // try to send IOException
             try {
                 oos = new ObjectOutputStream(resp.getOutputStream());
                 oos.writeObject(e);
                 oos.flush();
             } catch (IOException ex) {
                 LOGGER.error(Errors.CANT_WRITE_TO_OUTPUTSTREAM, e.getLocalizedMessage());
                 resp.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, e.getLocalizedMessage());
 
             }
         }
     }
 
     /**
      * Puts this object values in a String format.
      *
      * @return String representation of this object.
      */
     @Override
     public String toString() {
         return "HttpServletProcessor{"
             + "serializationAdapter=" + serializationAdapter
             + ", serverContext=" + serverContext
             + ", isDebugEnabled=" + isDebugEnabled
             + ", actionCommands=" + actionCommands
             + '}';
     }
     //</editor-fold>
 }
