 package org.oobium.app;
 
 import java.net.InetSocketAddress;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.group.ChannelGroup;
 import org.jboss.netty.channel.group.ChannelGroupFuture;
 import org.jboss.netty.channel.group.DefaultChannelGroup;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.oobium.app.handlers.Gateway;
 import org.oobium.app.handlers.HttpRequestHandler;
 import org.oobium.app.server.RequestHandlerTrackers;
 import org.oobium.app.server.RequestHandlers;
 import org.oobium.app.server.ServerPipelineFactory;
 import org.oobium.logging.LogProvider;
 import org.oobium.logging.Logger;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 public class AppServer implements BundleActivator {
 
 	private static AppServer instance;
 	
 	public static BundleContext getContext() {
 		return instance.context;
 	}
 	
 	private Logger logger;
 	private BundleContext context;
 	
 	private RequestHandlers handlers;
 	private RequestHandlerTrackers trackers;
 	
 	private ServerBootstrap server;
 	private ChannelGroup channels;
 	private ExecutorService executors;
 
 	private Thread shutdownHook;
 	
 	public AppServer() {
 		this(LogProvider.getLogger(AppServer.class));
 	}
 	
 	public AppServer(Logger logger) {
 		instance = this;
 		this.logger = logger;
 		this.handlers = new RequestHandlers();
 	}
 	
 	private void addShutdownHook() {
 		shutdownHook = new Thread() {
 			@Override
 			public void run() {
 				disposeServer();
 			}
 		};
 		Runtime.getRuntime().addShutdownHook(shutdownHook);
 	}
 
 	private void removeShutdownHook() {
 		if(shutdownHook != null) {
 			Thread hook;
 			synchronized(AppServer.class) {
 				hook = shutdownHook;
 				shutdownHook = null;
 			}
 			if(hook != null) {
 				try {
					Runtime.getRuntime().removeShutdownHook(shutdownHook);
 				} catch(IllegalStateException e) {
 					// discard - virtual machine is shutting down anyway
 				}
 			}
 		}
 	}
 	
 	private ServerBootstrap createServer() {
 		channels = new DefaultChannelGroup();
 
 		server = new ServerBootstrap(
 				new NioServerSocketChannelFactory(
 						Executors.newCachedThreadPool(),
 						Executors.newCachedThreadPool() ));
 
 		executors = Executors.newCachedThreadPool();
 		
 		server.setPipelineFactory(new ServerPipelineFactory(logger, handlers, executors));
 
         server.setOption("child.tcpNoDelay", true);
         server.setOption("child.keepAlive", true);
 
         // TODO accept options from the application's config file
         
 		addShutdownHook();
 		
 		return server;
 	}
 	
 	private synchronized void disposeServer() {
 		if(server != null) {
 			removeShutdownHook();
 			
 			ChannelGroupFuture future = channels.close();
 			future.awaitUninterruptibly();
 
 			server.releaseExternalResources();
 			
 			server = null;
 			channels = null;
 		}
 	}
 	
 	private void startServer(int port) {
 		logger.info("starting server on port " + port);
 		
 		if(server == null) {
 			server = createServer();
 		}
 		
 		Channel channel = server.bind(new InetSocketAddress(port));
 		channels.add(channel);
 	}
 	
 	private void stopServer(int port) {
 		if(server != null) {
 			logger.info("server stopped on port " + port);
 			if(!handlers.hasPorts()) {
 				disposeServer();
 			}
 		}
 	}
 	
 	public HttpRequestHandler addHandler(HttpRequestHandler handler) {
 		return (HttpRequestHandler) addHandler(handler, handler.getPort());
 	}
 	
 	public Gateway addHandler(Gateway handler) {
 		return (Gateway) addHandler(handler, handler.getPort());
 	}
 	
 	public Object addHandler(Object handler, int port) {
 		handlers.addHandler(handler, port);
 		int count = handlers.size(port);
 		if(count == 1) {
 			startServer(port);
 		} else {
 			logger.info("incremented count of port " + port + " to " + count);
 		}
 		return handler;
 	}
 	
 	public void removeHandler(HttpRequestHandler handler) {
 		int port = handler.getPort();
 		if(handlers.removeHandler(handler, port)) {
 			int count = handlers.size(port);
 			if(count == 0) {
 				stopServer(port);
 			} else {
 				logger.info("decremented count of port " + handler.getPort() + " to " + count);
 			}
 		}
 	}
 	
 	public void start(final BundleContext context) throws Exception {
 		this.context = context;
 		
 		logger.setTag(context.getBundle().getSymbolicName());
 		logger.info("Starting server");
 
 		trackers = new RequestHandlerTrackers();
 		trackers.open(this, context, handlers);
 		
 		logger.info("Server started");
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		logger.info("Stopping server");
 		trackers.close();
 		trackers = null;
 		handlers.clear();
 		handlers = null;
 		disposeServer();
 		logger.info("Server stopped");
 		logger.setTag(null);
 		logger = null;
 		
 		this.context = null;
 		instance = null;
 	}
 
 	public int[] getPorts() {
 		return (handlers != null) ? handlers.getPorts() : new int[0];
 	}
 
 }
