 package de.consistec.syncframework.common;
 
 /*
  * #%L
  * Project - doppelganger
  * File - SyncContext.java
  * %%
  * Copyright (C) 2011 - 2013 consistec GmbH
  * %%
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as
  * published by the Free Software Foundation, either version 3 of the 
  * License, or (at your option) any later version.
  * 
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public 
  * License along with this program.  If not, see
  * <http://www.gnu.org/licenses/gpl-3.0.html>.
  * #L%
  */
 
 import static de.consistec.syncframework.common.i18n.MessageReader.read;
 import static de.consistec.syncframework.common.util.Preconditions.checkGlobalSyncDirectionAndConflictStrategyState;
 import static de.consistec.syncframework.common.util.Preconditions.checkNotNull;
 import static de.consistec.syncframework.common.util.Preconditions.checkState;
 
 import de.consistec.syncframework.common.adapter.DatabaseAdapterFactory;
 import de.consistec.syncframework.common.adapter.IDatabaseAdapter;
 import de.consistec.syncframework.common.client.ClientSyncProvider;
 import de.consistec.syncframework.common.client.IClientSyncProvider;
 import de.consistec.syncframework.common.client.SyncAgent;
 import de.consistec.syncframework.common.data.schema.Schema;
 import de.consistec.syncframework.common.exception.ContextException;
 import de.consistec.syncframework.common.exception.SyncException;
 import de.consistec.syncframework.common.exception.SyncProviderInstantiationException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterException;
 import de.consistec.syncframework.common.exception.database_adapter.DatabaseAdapterInstantiationException;
 import de.consistec.syncframework.common.i18n.Errors;
 import de.consistec.syncframework.common.i18n.Infos;
 import de.consistec.syncframework.common.server.IServerSyncProvider;
 import de.consistec.syncframework.common.server.ServerSyncProvider;
 import de.consistec.syncframework.common.util.LoggingUtil;
 
 import java.sql.Connection;
 import java.sql.SQLException;
 import javax.sql.DataSource;
 import org.slf4j.cal10n.LocLogger;
 
 /**
  * Represents synchronization context.
  * <br/>Both for the server and the client site of synchronization process.<br/>
  * <p>
  * This class is a Facade to interact with the framework. <br/>
  * <span style="color: red;">You should use always use this class to interact with the framework!</span><br/>
  * Attempts to invoke methods directly on others frameworks objects can lead to undefined behavior,
  * because database structures could be not prepared or other requirements could be not fulfilled.
  * When sub contexts are created, all needed initialization is performed. It is thus important to create as little
  * context objects as possible. Context object can be used for more than one synchronization process.<br/>
  * <span style="color: red;">One only needs to create a new context object if the framework configuration has changed.
  * </span>
  * </p>
  * <p>
  * Functions of the framework are separated in three sub-contexts:
  * <ul>
  * <li>{@link de.consistec.syncframework.common.SyncContext.ClientContext},</li>
  * <li>{@link de.consistec.syncframework.common.SyncContext.ServerContext},</li>
  * <li>{@link de.consistec.syncframework.common.SyncContext.LocalContext}</li>
  * </ul>
  * Each sub-context can be created with static factory methods of its class.<br/>
  * Simplest examples:<br/>
  * <ul>
  * <li>On client - <br/><pre>SyncContext.ClientContext.create().synchronize();</pre></li>
  * <li>On server -
  * <pre>
  *  int nextServerRevisionSendToClient;
  *  List<Change> changes;
  *  Schema dbSchema;
  *  SyncContext.ServerContext serverContext = SyncContext.ServerContext.create();
  *  switch(action) {
  *      case(APPLY_CHANGES):
  *           nextServerRevisionSendToClient = serverContext.applyChanges(deserializedChanges,clientRevision);
  *           break;
  *      case(GET_CHANGES):
  *          serverContext.getChanges(clientRev);
  *          break;
  *      case(GET_SCHEMA):
  *          dbSchema = serverContext.getSchema();
  *          break;
  * }</pre></li>
  * <li>When client and synchronization server are used by the same app (LocalContext) -
  * <pre>
  *  SyncContext.LocalContext.create().synchronize();
  * </pre>
  * </li>
  * </ul>
  * </p>
  * <h1>Remote servers</h1>
  * <p>
  * Under the hood context use synchronization providers and agents. To synchronize with remote server,
  * one has to provide server proxy which implements
  * {@link de.consistec.syncframework.common.server.IServerSyncProvider} interface.
  * This proxy class has to be specified in framework configuration in order to use it.
  * If it is not, then the synchronization will be carried out with local instance of the server
  * synchronization provider.<br/>
  * </p>
  * <h1>Connection pooling</h1>
  * <p>
  * Framework can connect to synchronized databases by itself but it does not use any pooling mechanism
  * to manage the connections.<br/>
  * If you are using database connection pooling or just want to use yours connections,
  * just use the factory methods which accepts {@link java.sql.DataSource} object,
  * to create instances of synchronization context.<br/>
  * If you want framework do use its own connections, provide appropriate connection data for database adapter through
  * framework's {@link de.consistec.syncframework.common.Config configuration class}.
  * </p>
  * <p/>
  * <h1>Configuration</h1>
  * <p>
  * Before creating the context objects, <b style="color: red;">remember</b> to populate the framework's
  * {@link de.consistec.syncframework.common.Config configuration class} with the desired values
  * (either loading from file or manually through mutators).
  * </p>
  *
  * @author Piotr Wieczorek
  * @company consistec Engineering and Consulting GmbH
  * @date 14.11.2012 16:18:27
  * @since 0.0.1-SNAPSHOT
  */
 public final class SyncContext {
 
 //<editor-fold defaultstate="expanded" desc=" Class fields " >
     /**
      * Default local server provider class.
      * <p/>
      * value: {@value}
      */
 //    private static final Class<? extends IServerSyncProvider> DEFAULT_SERVER_PROVIDER = ServerSyncProvider.class;
     private static final LocLogger LOGGER = LoggingUtil.createLogger(SyncContext.class);
     private static final Config CONF = Config.getInstance();
     private TableSyncStrategies strategies = new TableSyncStrategies();
 
 //</editor-fold>
 //<editor-fold defaultstate="expanded" desc=" Class constructors " >
 
     /**
      * Do not allow direct creation of new instance.
      */
     private SyncContext() {
     }
 
 //</editor-fold>
 
     //<editor-fold defaultstate="expanded" desc=" Class methods " >
     private void initServer(Connection connection) throws ContextException {
 
         IDatabaseAdapter adapter = null;
 
         try {
             if (connection == null) {
                 adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER);
             } else {
                 adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER,
                     connection);
             }
 
             adapter.getConnection().setAutoCommit(true);
             adapter.createMDSchemaOnServer();
             LOGGER.info(Infos.COMMON_FRAMEWORK_INITIALIZED_SERVER);
 
         } catch (SQLException ex) {
             throw new ContextException(read(Errors.COMMON_CANT_INIT_FRAMEWORK), ex);
         } catch (DatabaseAdapterException ex) {
             throw new ContextException(read(Errors.COMMON_CANT_INIT_FRAMEWORK), ex);
         } finally {
             if (adapter != null) {
                 try {
                     adapter.getConnection().close();
                 } catch (SQLException ex) {
                     throw new ContextException(read(Errors.DATA_CLOSE_CONNECTION_FAILED), ex);
                 }
             }
         }
     }
 
     private IServerSyncProvider createServer(DataSource ds) throws ContextException {
 
         ServerSyncProvider provider = null;
 
         try {
             if (ds == null) {
                 initServer(null);
                 provider = new ServerSyncProvider(strategies);
             } else {
                 initServer(ds.getConnection());
                 provider = new ServerSyncProvider(strategies, ds);
             }
         } catch (SQLException ex) {
             throw new ContextException(ex);
         } catch (DatabaseAdapterException ex) {
             throw new ContextException(ex);
         }
 
         return provider;
     }
 
     private IClientSyncProvider createClient(DataSource ds, IServerSyncProvider serProvider) throws ContextException,
         SyncException {
 
         ClientSyncProvider clientProvider = null;
         try {
             if (ds == null) {
                 clientProvider = new ClientSyncProvider(strategies);
             } else {
                 clientProvider = new ClientSyncProvider(strategies, ds);
             }
         } catch (DatabaseAdapterInstantiationException ex) {
             throw new ContextException(ex);
         }
         initClient(serProvider, clientProvider);
 
         return clientProvider;
     }
 
     private void initClient(IServerSyncProvider serverProvider, IClientSyncProvider clientProvider) throws
         ContextException, SyncException {
 
         validateSettings(serverProvider, clientProvider);
         prepareClientSchema(serverProvider, clientProvider);
         LOGGER.info(Infos.COMMON_FRAMEWORK_INITIALIZED_CLIENT);
     }
 
     private void prepareClientSchema(IServerSyncProvider serverProvider, IClientSyncProvider clientProvider)
         throws SyncException {
 
         if (clientProvider.hasSchema()) {
             LOGGER.info(Infos.COMMON_SCHEMA_IS_UP_TO_DATE);
         } else {
             LOGGER.info(Infos.COMMON_DOWNLOADING_DB_SCHEMA_FROM_SERVER);
             Schema schema = serverProvider.getSchema();
             LOGGER.info(Infos.COMMON_APPLYING_DB_SCHEMA);
             clientProvider.applySchema(schema);
         }
     }
 
     private void validateSettings(IServerSyncProvider serverProvider, IClientSyncProvider clientProvider) throws
         SyncException {
         LOGGER.info(Infos.COMMON_SETTINGS_VALIDATION);
 
         SyncSettings clientSettings = new SyncSettings(CONF.getSyncTables(), this.strategies);
         serverProvider.validate(clientSettings);
     }
 
     /**
      * Creates client context with database connection managed by framework.
      * <p>
      * While creating the context, framework will be prepared for client operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      * <p/>
      *
      * @return Client synchronization context
      * @throws ContextException When creation of database adapter or sync providers fails.
      * @throws SyncException When request for database schema fails.
      */
     public static ClientContext client() throws ContextException, SyncException {
 
         checkState(CONF.getServerProxy() != null, read(Errors.CONFIG_NO_SERVER_PROXY_SPECIFIED));
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new ClientContext(null);
     }
 
     /**
      * Creates client context which will use provided database connection.
      * <p>
      * While creating the context, framework will be prepared for client operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      * <p/>
      *
      * @param ds External data source.
      * @return Client synchronization context
      * @throws ContextException When creation of database adapter or sync providers fails.
      * @throws SyncException When request for database schema fails.
      */
     public static ClientContext client(DataSource ds) throws ContextException, SyncException {
 
         checkState(CONF.getServerProxy() != null, read(Errors.CONFIG_NO_SERVER_PROXY_SPECIFIED));
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new ClientContext(ds);
     }
 
     /**
      * Creates client context with database connection managed by framework.
      * <p>
      * While creating the context, framework will be prepared for client operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      * <p/>
      *
      * @param strategies Synchronization strategies for monitored tables.
      * @return Client synchronization context
      * @throws ContextException When creation of database adapter or sync providers fails.
      * @throws SyncException When request for database schema fails.
      */
     public static ClientContext client(TableSyncStrategies strategies) throws ContextException, SyncException {
 
         checkState(CONF.getServerProxy() != null, read(Errors.CONFIG_NO_SERVER_PROXY_SPECIFIED));
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new ClientContext(null);
     }
 
     /**
      * Creates client context with database connection managed by framework.
      * <p>
      * While creating the context, framework will be prepared for client operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param strategies Synchronization strategies for monitored tables.
      * @param ds External datasource.
      * @return Client synchronization context
      * @throws ContextException When creation of database adapter or sync providers fails.
      * @throws SyncException When request for database schema fails.
      */
     public static ClientContext client(DataSource ds, TableSyncStrategies strategies) throws ContextException,
         SyncException {
 
         checkState(CONF.getServerProxy() != null, read(Errors.CONFIG_NO_SERVER_PROXY_SPECIFIED));
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new ClientContext(ds);
     }
 
     /**
      * Creates server synchronization context.
      * Context will use its own database connection.
      * <p>
      * While creating the context, framework will be prepared for server operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @return Server synchronization context
      * @throws ContextException When creation of database adapter or server provider fails.
      */
     public static ServerContext server() throws ContextException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new ServerContext(null);
     }
 
     /**
      * Creates server synchronization context.
      * context will use provided <i>connection</i> object.
      * <p>
      * While creating the context, framework will be prepared for server operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param ds SQL data source for server provider.
      * @return Server synchronization context.
      * @throws ContextException When creation of database adapter or server provider fails.
      */
     public static ServerContext server(DataSource ds) throws ContextException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new ServerContext(ds);
     }
 
     /**
      * Creates server synchronization context.
      * Context will use its own database connection.
      * <p>
      * While creating the context, framework will be prepared for server operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param strategies Synchronization strategies for monitored tables.
      * @return Server synchronization context
      * @throws ContextException When creation of database adapter or server provider fails.
      */
     public static ServerContext server(TableSyncStrategies strategies) throws ContextException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new ServerContext(null);
     }
 
     /**
      * Creates server synchronization context.
      * Context will use its own database connection.
      * <p>
      * While creating the context, framework will be prepared for server operations.<br/>
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param ds SQL data source for server provider.
      * @param strategies Synchronization strategies for monitored tables.
      * @return Server synchronization context
      * @throws ContextException When creation of database adapter or server provider fails.
      */
     public static ServerContext server(DataSource ds, TableSyncStrategies strategies) throws ContextException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new ServerContext(ds);
     }
 
     private static void initServerContext(DataSource ds) throws ContextException {
         IDatabaseAdapter adapter = null;
 
         try {
             if (ds == null) {
                 adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER);
             } else {
                 adapter = DatabaseAdapterFactory.newInstance(DatabaseAdapterFactory.AdapterPurpose.SERVER,
                     ds.getConnection());
             }
 
             adapter.getConnection().setAutoCommit(true);
             adapter.createMDSchemaOnServer();
             LOGGER.info(Infos.COMMON_FRAMEWORK_INITIALIZED_SERVER);
 
         } catch (SQLException ex) {
             throw new ContextException(read(Errors.COMMON_CANT_INIT_FRAMEWORK), ex);
         } catch (DatabaseAdapterException ex) {
             throw new ContextException(read(Errors.COMMON_CANT_INIT_FRAMEWORK), ex);
         } finally {
             if (adapter != null) {
                 try {
                     adapter.getConnection().close();
                 } catch (SQLException ex) {
                     throw new ContextException(read(Errors.DATA_CLOSE_CONNECTION_FAILED), ex);
                 }
             }
         }
     }
 
     /**
      * Creates LocalContext object for scenario where both client and server provider use
      * their own database connection.
      * <p>
      * While creating the context, framework will be prepared for client and server operations.
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @return LocalContext Context instance.
      * @throws ContextException When creation of database adapter or server provider fails.
      * @throws SyncException When request for database schema fails.
      */
     public static LocalContext local() throws ContextException, SyncException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new LocalContext(null, null);
     }
 
     /**
      * Creates LocalContext object for scenario where both client and server provider use
      * their own database connection.
      * <p>
      * While creating the context, framework will be prepared for client and server operations.
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param strategies Synchronization strategies for synchronized tables.
      * @return LocalContext Context instance.
      * @throws ContextException When creation of database adapter or server provider fails.
      * @throws SyncException When request for database schema fails.
      */
     public static LocalContext local(TableSyncStrategies strategies) throws ContextException, SyncException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new LocalContext(null, null);
     }
 
     /**
      * Creates LocalContext object for scenario where both client and server provider can use external database
      * connection.
      * <p>
      * While creating the context, framework will be prepared for client and server operations.
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>.
      *
      * @param serverDs DataSource for server sync provider. If <i>null</i>, <b>internal</b> connection will be used.
      * @param clientDs DataSource for client sync provider. If <i>null</i>, <b>internal</b> connection will be used.
      * @return instance of LocalContext
      * @throws ContextException When creation of database adapter or server provider fails.
      * @throws SyncException When request for database schema fails.
      */
     public static LocalContext local(DataSource serverDs, DataSource clientDs) throws ContextException,
         SyncException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         return mainCtx.new LocalContext(serverDs, clientDs);
     }
 
     /**
      * Creates LocalContext object for scenario where both client and server provider can use external database
      * connection.
      * <p>
      * While creating the context, framework will be prepared for client and server operations.
      * <b style="color: red;">Warning!</b> framework's
      * {@link de.consistec.syncframework.common.Config configuration} has to be populated with desired values.
      * </p>
      *
      * @param serverDs DataSource for server sync provider. If <i>null</i>, <b>internal</b> connection will be used.
      * @param clientDs DataSource for client sync provider. If <i>null</i>, <b>internal</b> connection will be used.
      * @param strategies Synchronization strategies for synchronized tables.
      * @return instance of LocalContext
      * @throws ContextException When creation of database adapter or server provider fails.
      * @throws SyncException When request for database schema fails.
      */
     public static LocalContext local(DataSource serverDs, DataSource clientDs, TableSyncStrategies strategies)
         throws ContextException, SyncException {
         checkGlobalSyncDirectionAndConflictStrategyState();
         SyncContext mainCtx = new SyncContext();
         mainCtx.strategies.addAll(strategies);
         return mainCtx.new LocalContext(serverDs, clientDs);
     }
 
 //</editor-fold>
 //<editor-fold defaultstate="expanded" desc=" Inner classes " >
 
     /**
      * Represents client side of the synchronization process.
      */
     public final class ClientContext {
 
         private SyncAgent agent;
 
         /**
          * Instance creation only allowed through static factory methods.
          */
         private ClientContext(DataSource ds) throws ContextException, SyncException {
 
             try {
 
                 IServerSyncProvider serProvider = ServerProxyFactory.newInstance();
                 IClientSyncProvider clientProvider = createClient(ds, serProvider);
                 agent = new SyncAgent(serProvider, clientProvider);
 
             } catch (SyncProviderInstantiationException ex) {
                 throw new ContextException(read(Errors.COMMON_SYNC_PROVIDER_INSTANTIATION_FAILED), ex);
             }
 
         }
 
         /**
          * Starts the synchronization request.
          * Invokes the chain of methods on server provider instance (or its proxy).
          */
         public void synchronize() throws SyncException {
             agent.synchronize();
         }
 
         /**
          * Sets the conflict listener.
          * <p/>
          *
          * @param listener Conflict listener
          */
         public void setConflictListener(IConflictListener listener) {
             checkNotNull(listener, read(Errors.COMMON_PROVIDED_CONFLICT_LISTENER_NOT_INITIALIZED));
             agent.setConflictListener(listener);
         }
 
         /**
          * @return Conflict listener
          */
         public IConflictListener getConflictListener() {
             return agent.getConflictListener();
         }
 
         /**
          * Add progress listener to listeners collection.
          * <p/>
          *
          * @param listener Progress listener
          */
         public void addProgressListener(ISyncProgressListener listener) {
             checkNotNull(listener, read(Errors.COMMON_PROVIDED_PROGRESS_LISTENER_NOT_INITIALIZED));
             agent.addProgressListener(listener);
         }
 
         /**
          * Removes progress listener from listeners collection.
          * <p/>
          *
          * @param listener Progress listener
          */
         public void removeProgressListener(ISyncProgressListener listener) {
             agent.removeProgressListener(listener);
         }
     }
 
     /**
      * Represents server side of the synchronization process.
      */
     public final class ServerContext {
 
         private IServerSyncProvider serverProvider;
         private TableSyncStrategies strategies;
 
         /**
          * Instance creation only allowed through static factory methods of outer class.
          */
         private ServerContext(DataSource ds) throws ContextException {
             this.serverProvider = createServer(ds);
         }
 
         /**
          * Apply changes from client to server.
          * <p/>
          *
          * @param clientData client data which contains the client's revision and the changes to apply.
          * @return New revision created by the server.
          * @throws SyncException
          */
         public int applyChanges(SyncData clientData) throws SyncException {
             return serverProvider.applyChanges(clientData);
         }
 
         /**
          * Get changes from server for the given revision.
          * <p/>
          *
          * @param rev Data revision.
          * @return List of changes from server.
          * @throws SyncException
          */
         public SyncData getChanges(int rev) throws SyncException {
             return serverProvider.getChanges(rev);
         }
 
         /**
          * Get database schema definition from server.
          * <p/>
          *
          * @return Database schema definition.
          * @throws SyncException
          */
         public Schema getSchema() throws SyncException {
             return serverProvider.getSchema();
         }
 
         /**
          * Validates the passed client settings and throws a SyncException if necessary.
          *
          * @param syncSettings client settings
          * @throws SyncException
          */
         public void validate(SyncSettings syncSettings) throws SyncException {
             serverProvider.validate(syncSettings);
         }
 
         /**
          * Sets the optional sync strategies for each table to the server.
          *
          * @param tableSyncStrategies optional sync strategies for configured table
          */
         public void setTableSyncStrategies(final TableSyncStrategies tableSyncStrategies) {
             AbstractSyncProvider abstractSyncProvider = (AbstractSyncProvider) serverProvider;
             abstractSyncProvider.getStrategies().addAll(tableSyncStrategies);
         }
     }
 
     /**
      * Represents <i>"local"</i> case of synchronization process.
      * In this case both, the client synchronization provider and the server synchronization provider
      * are used by the same application, and they can invoke theirs methods directly.<br>
      * <b>Warning!</b><br/>To use this context, the {@link Config#serverProxy }
      * property should be set to <b><i>null</i></b>, because client provider has to able to <i>talk</i> directly
      * to server provider object, not to it's proxy object.
      */
     public final class LocalContext {
 
         private SyncAgent agent;
         private IServerSyncProvider serverProvider;
 
         /**
          * Instance creation only allowed through static factory methods of outer class.
          */
         private LocalContext(DataSource serverDs, DataSource clientDs) throws ContextException, SyncException {
 
             // just one time initialization
//            initServerContext(serverDs);
             this.serverProvider = createServer(serverDs);
 
             IClientSyncProvider clientProvider = createClient(clientDs, serverProvider);
             agent = new SyncAgent(this.serverProvider, clientProvider);
         }
 
         /**
          * Returns the configured sync strategies passed through the factory method local() of SyncContext.
          *
          * @return the configured sync strategies
          */
         public TableSyncStrategies getStrategies() {
             return strategies;
         }
 
         /**
          * Apply changes from client to server.
          * <p/>
          *
          * @param clientData client data which contains the client's revision and the changes to apply.
          * @return New revision created by the server.
          * @throws SyncException
          */
         public int applyChanges(SyncData clientData) throws SyncException {
             return serverProvider.applyChanges(clientData);
         }
 
         /**
          * Get changes from server for the given revision.
          * <p/>
          *
          * @param rev Data revision.
          * @return List of changes from server.
          * @throws SyncException
          */
         public SyncData getChanges(int rev) throws SyncException {
             return serverProvider.getChanges(rev);
         }
 
         /**
          * Get database schema definition from server.
          * <p/>
          *
          * @return Database schema definition.
          * @throws SyncException
          */
         public Schema getSchema() throws SyncException {
             return serverProvider.getSchema();
         }
 
         /**
          * Starts the synchronization request.
          * Invokes the chain of methods on server provider instance (or its proxy).
          */
         public void synchronize() throws SyncException {
             agent.synchronize();
         }
 
         /**
          * Sets the conflict listener.
          * <p/>
          *
          * @param listener Conflict listener
          */
         public void setConflictListener(IConflictListener listener) {
             checkNotNull(listener, read(Errors.COMMON_PROVIDED_CONFLICT_LISTENER_NOT_INITIALIZED));
             agent.setConflictListener(listener);
         }
 
         /**
          * @return Conflict listener
          */
         public IConflictListener getConflictListener() {
             return agent.getConflictListener();
         }
 
         /**
          * Add progress listener to listeners collection.
          * <p/>
          *
          * @param listener Progress listener.
          */
         public void addProgressListener(ISyncProgressListener listener) {
             checkNotNull(listener, read(Errors.COMMON_PROVIDED_PROGRESS_LISTENER_NOT_INITIALIZED));
             agent.addProgressListener(listener);
         }
 
         /**
          * Removes progress listener from listeners collection.
          * <p/>
          *
          * @param listener Progress listener
          */
         public void removeProgressListener(ISyncProgressListener listener) {
             agent.removeProgressListener(listener);
         }
 
         private IServerSyncProvider createServer(DataSource ds) throws ContextException {
 
             ServerSyncProvider provider = null;
 
             try {
                 if (ds == null) {
                     provider = new ServerSyncProvider(strategies);
                 } else {
                     provider = new ServerSyncProvider(strategies, ds);
                 }
             } catch (DatabaseAdapterException ex) {
                 throw new ContextException(ex);
             }
 
             return provider;
         }
     }
 
     /**
      * This class fabricates ServerSyncProvider instances to proxy the <i>real</i> server.
      */
     private static final class ServerProxyFactory {
 
         /**
          * Do not allow direct instantiation.
          */
         private ServerProxyFactory() {
             throw new AssertionError("No instance allowed");
         }
 
         /**
          * Create an instance of Server Provider or its proxy object.
          * When Server Proxy implementation is specified in framework configuration (see {@link Config#getServerProxy()}),
          * then instance of its class is produced, if not, then the {@link IllegalStateException} is thrown.
          * <p/>
          *
          * @param <T> type of provider object
          * @return IServerSyncProvider or default IServerSyncProvider implementation
          */
         public static <T extends IServerSyncProvider> T newInstance() throws SyncProviderInstantiationException {
 
             Class<? extends IServerSyncProvider> proxyClass = CONF.getServerProxy();
             LOGGER.info("instantiate following proxy class: {}",
                 proxyClass == null ? "null" : proxyClass.getCanonicalName());
             checkState(proxyClass != null, read(Errors.CONFIG_NO_SERVER_PROXY_SPECIFIED));
             T instance;
             try {
                 instance = (T) proxyClass.newInstance();
             } catch (Exception ex) {
                 throw new SyncProviderInstantiationException(ex);
             }
             return instance;
         }
     }
 
     //</editor-fold>
 }
