 package com.redshape.net.connection;
 
 import com.redshape.net.IServer;
 import com.redshape.net.ServerType;
 import com.redshape.net.connection.auth.IConnectionAuthenticator;
 import com.redshape.net.connection.auth.IConnectionAuthenticatorsProvider;
 import com.redshape.utils.Commons;
 
 import java.lang.reflect.Constructor;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: cyril
  * Date: 3/19/12
  * Time: 4:16 PM
  * To change this template use File | Settings | File Templates.
  */
 public class StandardServerConnectionsFactory implements IServerConnectionFactory {
     private Map<ServerType, Class<? extends IServerConnection>> registry
             = new HashMap<ServerType, Class<? extends IServerConnection>>();
 
     private IConnectionAuthenticatorsProvider authenticatorsProvider;
 
     public StandardServerConnectionsFactory(Map<ServerType, Class<? extends IServerConnection>> registry,
                                             IConnectionAuthenticatorsProvider provider ) {
         Commons.checkNotNull(registry);
         Commons.checkNotNull(provider);
 
         this.registry = registry;
         this.authenticatorsProvider = provider;
     }
 
     protected IConnectionAuthenticatorsProvider getAuthenticatorsProvider() {
         return this.authenticatorsProvider;
     }
 
     @Override
     public void registerSupport(ServerType type, Class<? extends IServerConnection> connectionSupport) {
         this.registry.put( type, connectionSupport );
     }
 
     @Override
     public IServerConnection createConnection(IServer server) {
         Class<? extends IServerConnection> connection = this.registry.get(server.getType());
         if ( connection == null ) {
             throw new IllegalArgumentException("Server type not supported");
         }
 
         IServerConnection conn;
         try {
             Constructor<? extends IServerConnection> constructor = connection.getConstructor( IServer.class,
                     IConnectionAuthenticator.class );
             conn = constructor.newInstance(server, this.getAuthenticatorsProvider().provide(server) );
         } catch ( Throwable e ) {
             throw new IllegalArgumentException( e.getMessage(), e );
         }
 
         return conn;
     }
 }
