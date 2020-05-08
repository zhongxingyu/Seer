 package com.redshape.daemon;
 
 import com.redshape.daemon.traits.IBindableDaemon;
 import com.redshape.daemon.traits.IConfigurableDaemon;
 import com.redshape.daemon.traits.ISpringContextAwareDaemon;
 import com.redshape.utils.config.ConfigException;
 import com.redshape.utils.config.IConfig;
 import com.redshape.utils.net.Utils;
 import org.apache.log4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.AbstractXmlApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 import org.springframework.context.support.FileSystemXmlApplicationContext;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URI;
 import java.rmi.AlreadyBoundException;
 import java.rmi.RMISecurityManager;
 import java.rmi.Remote;
 import java.rmi.RemoteException;
 import java.rmi.registry.LocateRegistry;
 import java.rmi.registry.Registry;
 import java.rmi.server.RMIClientSocketFactory;
 import java.rmi.server.RMIServerSocketFactory;
 import java.rmi.server.UnicastRemoteObject;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 public abstract class AbstractRMIDaemon<T extends IDaemonAttributes>
 						extends AbstractDaemon<T>
 					    implements IConfigurableDaemon<T>,
 					    		   IBindableDaemon<T>,
 					       		   ISpringContextAwareDaemon<T> {
 	private static final Logger log = Logger.getLogger( AbstractRMIDaemon.class );
 	
 	public static class Attributes extends DaemonAttributes {
 		protected Attributes( String code ) {
 			super(code);
 		}
 
 		public static final Attributes SERVER_PORT = new Attributes("SERVER_PORT");
 		public static final Attributes SERVER_HOST = new Attributes("SERVER_HOST");
 		public static final Attributes SERVER_SERVICE = new Attributes("SERVER_SERVICE");
 		public static final Attributes THREADS_COUNT = new Attributes("THREADS_COUNTER");
 	}
 
 	/**
 	 * RMI server thread
 	 */
 	private Registry registry;
 	
 	/**
 	 * Configuration state flag
 	 */
 	private boolean configured;
    
    private static Remote service;
    private static Remote remote;
 	
 	/**
 	 * Current spring application context
 	 */
 	private ApplicationContext context;
 	
 	private RMIClientSocketFactory clientsFactory;
 	
 	private RMIServerSocketFactory serverFactory;
 
     private ExecutorService threadExecutor = Executors.newSingleThreadExecutor();
 
     public AbstractRMIDaemon(String context ) throws DaemonException, ConfigException {
         this( loadContext(context) );
     }
     
 	public AbstractRMIDaemon( ApplicationContext context ) throws DaemonException, ConfigException {
 		super();
 
         this.setContext(context);
 		this.changeState( DaemonState.INITIALIZED );
 	}
 
     protected ExecutorService getThreadExecutor() {
         return this.threadExecutor;
     }
 
 	public void setClientsFactory( RMIClientSocketFactory factory ) {
 		this.clientsFactory = factory;
 	}
 	
 	public void setServerFactory( RMIServerSocketFactory factory ) {
 		this.serverFactory = factory;
 	}
 	
 	protected Registry getRegistry() {
 		return this.registry;
 	}
 	
 	/**
 	 * Spring context loader
 	 * @param contextPath
 	 * @return
 	 * @throws DaemonException
 	 */
     public static ApplicationContext loadContext( String contextPath ) throws DaemonException {
         log.info("Loading context location: " + contextPath );
 
         File file = new File(contextPath);
         AbstractXmlApplicationContext result;
         if (file.exists()) {
             result = new FileSystemXmlApplicationContext(contextPath);
         } else {
             result = new ClassPathXmlApplicationContext(contextPath);
         }
         
         result.setValidating(false);
         
         return result;
     }
 
 	@Override
 	public void setMaxConnections(Integer connections) {
 		this.setAttribute( (T) Attributes.MAX_CONNECTIONS, connections );
 	}
 
 	@Override
 	public Integer getMaxConnections() {
 		return this.getAttribute( (T) Attributes.MAX_CONNECTIONS );
 	}
 
     @Override
     public void setConfigured(boolean value) {
         this.configured = value;
     }
 
     @Override
 	public void setPath(String path) {
 		this.setAttribute( (T) Attributes.SERVICE_NAME, path );
 	}
 
 	@Override
 	public String getPath() {
 		return this.getAttribute( (T) Attributes.SERVICE_NAME );
 	}
 
 	@Override
 	public void setPort(Integer port) {
 		this.setAttribute( (T) Attributes.PORT, port );
 	}
 
 	@Override
 	public Integer getPort() {
 		return this.getAttribute( (T) Attributes.PORT );
 	}
 
 	@Override
 	public Integer getMaxAttempts() {
 		return this.<Integer>getAttribute( (T) Attributes.MAX_ATTEMPTS );
 	}
 
 	@Override
 	public void setHost(String host) {
 		this.setAttribute( (T) Attributes.HOST, host );
 	}
 
 	@Override
 	public String getHost() {
 		return this.getAttribute( (T) Attributes.HOST );
 	}
 
 	@Override
     public ApplicationContext getContext() {
     	return this.context;
     }
     
     @Override
     public void setContext( ApplicationContext context ) throws DaemonException, ConfigException {
     	this.setContext( context, true );
     }
     
     @Override
     public void setContext( ApplicationContext context, boolean reloadConfiguration ) throws DaemonException, ConfigException {
     	this.context = context;
     	
     	if ( reloadConfiguration ) {
     		this.loadConfiguration();
     	}
     }
     
     @Override
     public boolean isConfigured() {
     	return this.configured;
     }
     
     abstract protected void onStarted() throws DaemonException;
 	
 	@Override
 	public void start() throws DaemonException {
 		try {
             if ( System.getSecurityManager() == null ) {
                 System.setSecurityManager( new RMISecurityManager() );
             }
 
 			if ( this.getState().equals( DaemonState.STARTED ) ){
 				this.stop();
 				this.changeState( DaemonState.INITIALIZED );
 			}
 			
 			if ( !this.isConfigured() ) {
 				try {
 					this.loadConfiguration();
 				} catch ( ConfigException e ) {
 					throw new DaemonException("Daemon configuration exception", e );
 				}
 			}
 
 			if ( this.clientsFactory == null ) {
 				throw new DaemonException("Client sockets factory not set");
 			}
 
 			if ( this.serverFactory == null ) {
 				throw new DaemonException("Server sockets factory not set");
 			}
 
             AbstractRMIDaemon.this.startRegistry();
             AbstractRMIDaemon.this.onStarted();
 		} catch ( Throwable e ) {
             log.error( e.getMessage(), e );
             this.changeState( DaemonState.ERROR );
         } finally {
 			if ( this.getState() == null || this.getState().equals( DaemonState.ERROR ) ) {
 				this.stop();
 			}
 		}
 	}
 	
 	protected Integer getUnusedPort() throws AlreadyBoundException, IOException {
 		return Utils.getAvailable( this.getPort() );
 	}
 	
 	@SuppressWarnings("unchecked")
 	protected <E extends Remote> E exportService( IRemoteService service ) throws RemoteException, AlreadyBoundException, IOException {
 		final Integer port = this.getUnusedPort();
 
		Remote stub = AbstractRMIDaemon.remote = UnicastRemoteObject.exportObject(
			AbstractRMIDaemon.service = service,
 			port,
 			this.clientsFactory,
 			this.serverFactory
 		);
 		
 		log.info("Starting service: " + service.getServiceName() + " on port " + port );
 		
 		this.getRegistry().bind( service.getServiceName(), stub);
 		
		return (E) AbstractRMIDaemon.service;
 	}
 	
 	
 	protected void startRegistry() throws RemoteException, DaemonException  {
 		this.registry = LocateRegistry.createRegistry( this.getPort(),
 				this.clientsFactory,
 				this.serverFactory);
 		
 		log.info( "RMI Daemon Started!" );
 		this.changeState( DaemonState.STARTED );
 	}
 
 	protected URI prepareServiceLocation() {
 		return this.prepareServiceLocation( this.getHost(), this.getPort(), this.getPath() );
 	}
 
 	protected URI prepareServiceLocation( String host, Integer port, String serviceName ) {
 		try {
 			return new URI( "rmi://" + host + ":" + port + "/" + serviceName );
 		} catch ( Throwable e ) {
 			throw new RuntimeException();
 		}
 	}
 	
 	@Override
 	public void loadConfiguration() throws ConfigException, DaemonException {
 		this.loadConfiguration( this.getInitialConfiguration() );
 	}
 	
 	public IConfig getInitialConfiguration() {
 		if ( this.getContext() != null ) {
 			return this.getContext().getBean( "config", IConfig.class );
 		}
 		
 		return null;
 	}
 
 	@Override
 	public void reloadConfiguration( IConfig configLocation ) {
 		throw new RuntimeException("Not implemented yet");
 	}
 
 	@Override
 	public void stop() throws DaemonException {
 		try {
 			if ( this.registry != null ) {
 				UnicastRemoteObject.unexportObject(this.registry, true);
 			}
 		} catch ( Throwable e ) {
 			throw new DaemonException("Cannot gracefully stop registry", e);
 		}
 	}
 
 }
