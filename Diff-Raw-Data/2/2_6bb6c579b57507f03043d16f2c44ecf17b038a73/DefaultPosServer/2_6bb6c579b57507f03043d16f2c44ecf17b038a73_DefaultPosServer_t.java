 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.impl;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.logging.LogLevel;
 import org.apache.mina.filter.logging.LoggingFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.main.ConfigKey;
 import com.chinarewards.qqgbvpn.main.PosServer;
 import com.chinarewards.qqgbvpn.main.PosServerException;
 import com.chinarewards.qqgbvpn.main.SessionStore;
 import com.chinarewards.qqgbvpn.main.protocol.CmdCodecFactory;
 import com.chinarewards.qqgbvpn.main.protocol.CmdMapping;
 import com.chinarewards.qqgbvpn.main.protocol.CodecMappingConfigBuilder;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceDispatcher;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceHandlerObjectFactory;
 import com.chinarewards.qqgbvpn.main.protocol.ServiceMapping;
 import com.chinarewards.qqgbvpn.main.protocol.SimpleCmdCodecFactory;
 import com.chinarewards.qqgbvpn.main.protocol.filter.BodyMessageFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.ErrorConnectionKillerFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.IdleConnectionKillerFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.LoginFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.SessionKeyMessageFilter;
 import com.chinarewards.qqgbvpn.main.protocol.handler.ServerSessionHandler;
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.codec.MessageCoderFactory;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistService;
 
 /**
  * Concrete implementation of <code>PosServer</code>.
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class DefaultPosServer implements PosServer {
 	
 	/**
 	 * Default timeout, in seconds, which the server will disconnect a client.
 	 */
 	public static final int DEFAULT_SERVER_CLIENTMAXIDLETIME = 1800;
 
 	protected final Configuration configuration;
 
 	protected final Injector injector;
 
 	protected Logger log = LoggerFactory.getLogger(PosServer.class);
 
 	protected ServiceMapping mapping;
 
 	protected CmdMapping cmdMapping;
 
 	protected CmdCodecFactory cmdCodecFactory;
 
 	protected final ServiceHandlerObjectFactory serviceHandlerObjectFactory;
 
 	protected final ServiceDispatcher serviceDispatcher;
 
 	/**
 	 * socket server address
 	 */
 	InetSocketAddress serverAddr;
 
 	/**
 	 * The configured port to use.
 	 */
 	protected int port;
 
 	/**
 	 * Whether the PersistService of Guice has been initialized, i.e. the
 	 * .start() method has been called. We need to remember this state since it
 	 * cannot be called twice (strange!).
 	 */
 	protected boolean isPersistServiceInited = false;
 
 	/**
 	 * acceptor
 	 */
 	NioSocketAcceptor acceptor;
 
 	boolean persistenceServiceInited = false;
 
 	@Inject
 	public DefaultPosServer(Configuration configuration, Injector injector,
 			ServiceDispatcher serviceDispatcher,
 			ServiceHandlerObjectFactory serviceHandlerObjectFactory) {
 		this.configuration = configuration;
 		this.injector = injector;
 		this.serviceDispatcher = serviceDispatcher;
 		this.serviceHandlerObjectFactory = serviceHandlerObjectFactory;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgbvpn.main.PosServer#start()
 	 */
 	@Override
 	public void start() throws PosServerException {
 
 		buildCodecMapping();
 
 		// start the JPA persistence service
 		startPersistenceService();
 
 		// setup Apache Mina server.
 		startMinaService();
 
 		log.info("Server running, listening on {}", getLocalPort());
 
 	}
 
 	protected void buildCodecMapping() {
 
 		CodecMappingConfigBuilder builder = new CodecMappingConfigBuilder();
 		CmdMapping cmdMapping = builder.buildMapping(configuration);
 
 		// and then the factory
 		this.cmdCodecFactory = new SimpleCmdCodecFactory(cmdMapping);
 		this.cmdMapping = cmdMapping;
 
 	}
 
 	/**
 	 * Start the Apache Mina service.
 	 * 
 	 * @throws PosServerException
 	 */
 	protected void startMinaService() throws PosServerException {
 		
 		// default  1800 seconds
 		int idleTime = configuration.getInt(ConfigKey.SERVER_CLIENTMAXIDLETIME,
 				DEFAULT_SERVER_CLIENTMAXIDLETIME);
 		port = configuration.getInt("server.port");
 		serverAddr = new InetSocketAddress(port);
 
 		// =============== server side ===================
 
 		// Programmers: You MUST consult your team before rearranging the 
 		// order of the following Mina filters, as the ordering may affects
 		// the behaviour of the application which may lead to unpredictable
 		// result.
 		
 		acceptor = new NioSocketAcceptor();
 		
 		// ManageIoSessionConnect filter if idle server will not close any idle IoSession
 		acceptor.getFilterChain().addLast("ManageIoSessionConnect",
 				new IdleConnectionKillerFilter());
 		
 		// our logging filter
 		acceptor.getFilterChain()
 				.addLast(
 						"cr-logger",
 						new com.chinarewards.qqgbvpn.main.protocol.filter.LoggingFilter());
 		
 		acceptor.getFilterChain().addLast("logger", buildLoggingFilter());
 
 		// decode message
 		// TODO config MessageCoderFactory to allow setting the maximum message size 
 		acceptor.getFilterChain().addLast(
 				"codec",
 				new ProtocolCodecFilter(new MessageCoderFactory(cmdCodecFactory)));
 
 		// kills error connection if too many.
 		acceptor.getFilterChain().addLast("errorConnectionKiller",
 				new ErrorConnectionKillerFilter());
 
 		// bodyMessage filter - short-circuit if error message is received.
 		acceptor.getFilterChain().addLast("bodyMessage",
 				new BodyMessageFilter());
 		
 		// bodyMessage filter - short-circuit if error message is received.
 		acceptor.getFilterChain().addLast(
 				"sessionKeyFilter",
 				new SessionKeyMessageFilter(injector.getInstance(SessionStore.class),
 						new UuidSessionIdGenerator()));
 		
 		// Login filter.
 		acceptor.getFilterChain().addLast("login",
 				injector.getInstance(LoginFilter.class));
 
 		// the handler class
 		acceptor.setHandler(new ServerSessionHandler(serviceDispatcher,
				mapping, configuration, injector.getInstance(SessionStore.class)));
 		
 		// additional configuration
 		acceptor.setCloseOnDeactivation(true);
 		acceptor.setReuseAddress(true);
 
 		// acceptor.getSessionConfig().setReadBufferSize(2048);
 		if (idleTime > 0) {
 			log.info("Client idle timeout set to {} seconds", idleTime);
 		} else {
 			log.info("Client idle timeout set to {} seconds, will be disabled",
 					idleTime);
 		}
 		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, idleTime);
 		
 		// start the acceptor and listen to incomming connection!
 		try {
 			acceptor.bind(serverAddr);
 		} catch (IOException e) {
 			throw new PosServerException("Error binding server port", e);
 		}
 	}
 	
 	/**
 	 * Build an new instance of LoggingFilter with sane logging level. The
 	 * principle is to hide unnecessary logging under INFO level.
 	 * 
 	 * @return
 	 */
 	protected LoggingFilter buildLoggingFilter() {
 		LoggingFilter loggingFilter = new LoggingFilter();
 		loggingFilter.setMessageReceivedLogLevel(LogLevel.DEBUG);
 		loggingFilter.setMessageSentLogLevel(LogLevel.DEBUG);
 		loggingFilter.setSessionIdleLogLevel(LogLevel.TRACE);
 		return loggingFilter;
 	}
 
 	protected void startPersistenceService() {
 
 		// the guice-persist's PersistService can only be started once.
 		if (persistenceServiceInited) {
 			return;
 		}
 
 		// start the PersistService.
 		PersistService ps = injector.getInstance(PersistService.class);
 		ps.start();
 		persistenceServiceInited = true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgbvpn.main.PosServer#stop()
 	 */
 	@Override
 	public void stop() {
 
 		acceptor.unbind(serverAddr);
 		acceptor.dispose();
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgbvpn.main.PosServer#shutdown()
 	 */
 	@Override
 	public void shutdown() {
 		try {
 			PersistService ps = injector.getInstance(PersistService.class);
 			ps.stop();
 		} catch (Throwable t) {
 			// mute
 			log.warn("An error occurred when stopping persistence service", t);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgbvpn.main.PosServer#isStopped()
 	 */
 	@Override
 	public boolean isStopped() {
 		if (acceptor == null)
 			return true;
 		if (!acceptor.isActive() || acceptor.isDisposed())
 			return true;
 		return false;
 	}
 
 	@Override
 	public int getLocalPort() {
 
 		if (acceptor != null) {
 			InetSocketAddress d = (InetSocketAddress) acceptor
 					.getLocalAddress();
 			return d.getPort();
 		}
 
 		return port;
 	}
 
 }
