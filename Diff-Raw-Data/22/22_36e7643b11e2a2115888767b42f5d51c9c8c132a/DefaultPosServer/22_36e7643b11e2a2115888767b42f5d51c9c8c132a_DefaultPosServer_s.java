 /**
  * 
  */
 package com.chinarewards.qqgbvpn.main.impl;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Iterator;
 
 import org.apache.commons.configuration.Configuration;
 import org.apache.mina.core.service.IoAcceptor;
 import org.apache.mina.core.session.IdleStatus;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.logging.LoggingFilter;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.chinarewards.qqgbvpn.config.PosNetworkProperties;
 import com.chinarewards.qqgbvpn.main.PosServer;
 import com.chinarewards.qqgbvpn.main.PosServerException;
 import com.chinarewards.qqgbvpn.main.protocol.filter.BodyMessageFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.LoginFilter;
 import com.chinarewards.qqgbvpn.main.protocol.filter.TransactionFilter;
 import com.chinarewards.qqgbvpn.main.protocol.hander.ServerSessionHandler;
 import com.chinarewards.qqgbvpn.main.protocol.socket.mina.encoder.MessageCoderFactory;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistService;
 
 /**
  * 
  * 
  * @author Cyril
  * @since 0.1.0
  */
 public class DefaultPosServer implements PosServer {
 
 	protected final Configuration configuration;
 
 	protected final Injector injector;
 
 	protected Logger log = LoggerFactory.getLogger(PosServer.class);
 
 	/**
 	 * socket server address
 	 */
 	InetSocketAddress serverAddr;
 
 	protected int port;
 
 	/**
 	 * acceptor
 	 */
 	IoAcceptor acceptor;
 
 	@Inject
 	public DefaultPosServer(Configuration configuration, Injector injector) {
 		this.configuration = configuration;
 		this.injector = injector;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see com.chinarewards.qqgbvpn.main.PosServer#start()
 	 */
 	@Override
 	public void start() throws PosServerException {
 
 		printConfigValues();
 		
 		// start the JPA persistence service
 		startPersistenceService();
 		
 
 		// setup Apache Mina server.
 
 		port = configuration.getInt("server.port");
 		serverAddr = new InetSocketAddress(port);
 		
 		
 		// the TCP port to listen
 		int port = new PosNetworkProperties().getSearverPort();
 
 		// =============== server side ===================
 		serverAddr = new InetSocketAddress(port);
 
 		acceptor = new NioSocketAcceptor();
 		acceptor.getFilterChain().addLast("logger", new LoggingFilter());
 
 		// not this
 		acceptor.getFilterChain().addLast("codec",
 				new ProtocolCodecFilter(new MessageCoderFactory(injector)));
 
 		//bodyMessage filter
 		acceptor.getFilterChain().addLast("bodyMessage",
 				new BodyMessageFilter());
 		
 		// Transaction filter.
 		acceptor.getFilterChain().addLast("transaction",
 				injector.getInstance(TransactionFilter.class));
 
 		// Login filter.
 		acceptor.getFilterChain().addLast("login", new LoginFilter());
 
 		acceptor.setHandler(new ServerSessionHandler(injector));
 		acceptor.setCloseOnDeactivation(true);
 
 		// acceptor.getSessionConfig().setReadBufferSize(2048);
 		acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
 		try {
 			acceptor.bind(serverAddr);
 		} catch (IOException e) {
 			throw new PosServerException("Error binding server port", e);
 		}
 
 		log.info("Server running, listening on {}", getLocalPort());
 
 	}
 
 	/**
 	 * Print configuration.
 	 */
 	private void printConfigValues() {
 		// get system configuration
 		Iterator iter = configuration.getKeys();
 		if (configuration.isEmpty()) {
 			log.debug("No configuration values");
 		} else {
 			log.debug("System configuration:");
 			while (iter.hasNext()) {
 				String key = (String) iter.next();
 				log.debug("- {}: {}", key, configuration.getProperty(key));
 			}
 		}
 
 		// TODO print command mapping
 	}
 
 	protected void startPersistenceService() {
 		PersistService ps = injector.getInstance(PersistService.class);
 		ps.start();
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
 
		PersistService ps = injector.getInstance(PersistService.class);
		ps.stop();
 
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
