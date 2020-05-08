 /**
  * JBoss, Home of Professional Open Source. Copyright 2011, Red Hat, Inc., and
  * individual contributors as indicated by the @author tags. See the
  * copyright.txt file in the distribution for a full listing of individual
  * contributors.
  * 
  * This is free software; you can redistribute it and/or modify it under the
  * terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  * 
  * This software is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  * 
  * You should have received a copy of the GNU Lesser General Public License
  * along with this software; if not, write to the Free Software Foundation,
  * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA, or see the FSF
  * site: http://www.fsf.org.
  */
 package org.apache.coyote.http11;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.InetAddress;
 import java.net.URLEncoder;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.CompletionHandler;
 import java.util.Iterator;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.Executor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import javax.management.ObjectName;
 
 import org.apache.coyote.RequestGroupInfo;
 import org.apache.coyote.RequestInfo;
 import org.apache.tomcat.util.modeler.Registry;
 import org.apache.tomcat.util.net.NioChannel;
 import org.apache.tomcat.util.net.NioEndpoint;
 import org.apache.tomcat.util.net.SSLImplementation;
 import org.apache.tomcat.util.net.SocketStatus;
 import org.apache.tomcat.util.net.jsse.NioJSSEImplementation;
 import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;
 
 /**
  * {@code Http11NioProtocol}
  * 
  * Created on Jan 10, 2012 at 3:14:49 PM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class Http11NioProtocol extends Http11AbstractProtocol {
 
 	protected NioEndpoint endpoint = new NioEndpoint();
 	private Http11ConnectionHandler cHandler = new Http11ConnectionHandler(this);
 	protected NioJSSESocketChannelFactory socketFactory = null;
 
 	/**
 	 * Create a new instance of {@code Http11NioProtocol}
 	 */
 	public Http11NioProtocol() {
 		setSoLinger(Constants.DEFAULT_CONNECTION_LINGER);
 		setSoTimeout(Constants.DEFAULT_CONNECTION_TIMEOUT);
 		setTcpNoDelay(Constants.DEFAULT_TCP_NO_DELAY);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#getRequestGroupInfo()
 	 */
 	@Override
 	public RequestGroupInfo getRequestGroupInfo() {
 		return cHandler.global;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#init()
 	 */
 	@Override
 	public void init() throws Exception {
 		endpoint.setName(getName());
 		endpoint.setHandler(cHandler);
 
 		// Verify the validity of the configured socket factory
 		try {
 			if (isSSLEnabled()) {
 				sslImplementation = SSLImplementation.getInstance(NioJSSEImplementation.class
 						.getName());
 				socketFactory = sslImplementation.getServerSocketChannelFactory();
 				endpoint.setServerSocketChannelFactory(socketFactory);
 			}
 		} catch (Exception ex) {
 			log.error(sm.getString("http11protocol.socketfactory.initerror"), ex);
 			throw ex;
 		}
 
 		if (socketFactory != null) {
 			Iterator<String> attE = attributes.keySet().iterator();
 			while (attE.hasNext()) {
 				String key = attE.next();
 				Object v = attributes.get(key);
 				socketFactory.setAttribute(key, v);
 			}
 		}
 
 		try {
 			// endpoint.setKeepAliveTimeout(this.timeout);
 			endpoint.init();
 		} catch (Exception ex) {
 			log.error(sm.getString("http11protocol.endpoint.initerror"), ex);
 			throw ex;
 		}
 		//
 		log.info(sm.getString("http11protocol.init", getName()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#start()
 	 */
 	@Override
 	public void start() throws Exception {
 		if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
 			if (this.domain != null) {
 				try {
 					tpOname = new ObjectName(domain + ":" + "type=ThreadPool,name=" + getName());
 					Registry.getRegistry(null, null).registerComponent(endpoint, tpOname, null);
 				} catch (Exception e) {
 					log.error("Can't register threadpool");
 				}
 				rgOname = new ObjectName(domain + ":type=GlobalRequestProcessor,name=" + getName());
 				Registry.getRegistry(null, null).registerComponent(cHandler.global, rgOname, null);
 			}
 		}
 		try {
 			endpoint.start();
 		} catch (Exception ex) {
 			log.error(sm.getString("http11protocol.endpoint.starterror"), ex);
 			throw ex;
 		}
 
 		log.info(sm.getString("http11protocol.start", getName()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#pause()
 	 */
 	@Override
 	public void pause() throws Exception {
 		try {
 			endpoint.pause();
 		} catch (Exception ex) {
 			log.error(sm.getString("http11protocol.endpoint.pauseerror"), ex);
 			throw ex;
 		}
 		canDestroy = false;
 		// Wait for a while until all the processors are idle
 		RequestInfo[] states = cHandler.global.getRequestProcessors();
 		int retry = 0;
 		boolean done = false;
 		while (!done && retry < org.apache.coyote.Constants.MAX_PAUSE_WAIT) {
 			retry++;
 			done = true;
 			for (int i = 0; i < states.length; i++) {
 				if (states[i].getStage() == org.apache.coyote.Constants.STAGE_SERVICE) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// NOTHING TO DO
 					}
 					done = false;
 					break;
 				}
 			}
 			if (done) {
 				canDestroy = true;
 			}
 		}
 
 		log.info(sm.getString("http11protocol.pause", getName()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#resume()
 	 */
 	@Override
 	public void resume() throws Exception {
 		try {
 			endpoint.resume();
 		} catch (Exception ex) {
 			log.error(sm.getString("http11protocol.endpoint.resumeerror"), ex);
 			throw ex;
 		}
 
 		log.info(sm.getString("http11protocol.resume", getName()));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.ProtocolHandler#destroy()
 	 */
 	@Override
 	public void destroy() throws Exception {
 		log.info(sm.getString("http11protocol.stop", getName()));
 		if (canDestroy) {
 			endpoint.destroy();
 		} else {
 			log.warn(sm.getString("http11protocol.cannotDestroy", getName()));
 			try {
 				RequestInfo[] states = cHandler.global.getRequestProcessors();
 				for (int i = 0; i < states.length; i++) {
 					if (states[i].getStage() == org.apache.coyote.Constants.STAGE_SERVICE) {
 						// FIXME: Log RequestInfo content
 					}
 				}
 			} catch (Exception ex) {
 				log.error(sm.getString("http11protocol.cannotDestroy", getName()), ex);
 				throw ex;
 			}
 		}
 		if (org.apache.tomcat.util.Constants.ENABLE_MODELER) {
 			if (tpOname != null)
 				Registry.getRegistry(null, null).unregisterComponent(tpOname);
 			if (rgOname != null)
 				Registry.getRegistry(null, null).unregisterComponent(rgOname);
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.coyote.http11.Http11AbstractProtocol#getName()
 	 */
 	public String getName() {
 		String encodedAddr = "";
 		if (getAddress() != null) {
 			encodedAddr = "" + getAddress();
 			try {
 				encodedAddr = URLEncoder.encode(encodedAddr.replace('/', '-'), "UTF-8") + "-";
 			} catch (UnsupportedEncodingException e) {
 				log.warn("UTF-8 encoding is not supported");
 			}
 		}
 		return ("http-" + encodedAddr + endpoint.getPort());
 	}
 
 	/**
 	 * @return the executor
 	 */
 	public Executor getExecutor() {
 		return endpoint.getExecutor();
 	}
 
 	/**
 	 * Setter for the executor
 	 * 
 	 * @param executor
 	 *            the executor to set
 	 */
 	public void setExecutor(Executor executor) {
 		endpoint.setExecutor(executor);
 	}
 
 	/**
 	 * @return the maximum number of threads
 	 */
 	public int getMaxThreads() {
 		return endpoint.getMaxThreads();
 	}
 
 	/**
 	 * Setter for the maximum number of threads
 	 * 
 	 * @param maxThreads
 	 *            the maximum number of threads to set
 	 */
 	public void setMaxThreads(int maxThreads) {
 		endpoint.setMaxThreads(maxThreads);
 	}
 
 	/**
 	 * @param size
 	 */
 	public void setPollerSize(int size) {
 		this.endpoint.setMaxConnections(size);
 	}
 
 	/**
 	 * @return The poller size
 	 */
 	public int getPollerSize() {
 		return this.endpoint.getMaxConnections();
 	}
 
 	/**
 	 * @return the thread priority
 	 */
 	public int getThreadPriority() {
 		return endpoint.getThreadPriority();
 	}
 
 	/**
 	 * Setter for the thread priority
 	 * 
 	 * @param threadPriority
 	 *            the thread priority to set
 	 */
 	public void setThreadPriority(int threadPriority) {
 		endpoint.setThreadPriority(threadPriority);
 	}
 
 	/**
 	 * @return the backlog
 	 */
 	public int getBacklog() {
 		return endpoint.getBacklog();
 	}
 
 	/**
 	 * Setter for the backlog
 	 * 
 	 * @param backlog
 	 *            the backlog to set
 	 */
 	public void setBacklog(int backlog) {
 		endpoint.setBacklog(backlog);
 	}
 
 	/**
 	 * @return the port number
 	 */
 	public int getPort() {
 		return endpoint.getPort();
 	}
 
 	/**
 	 * Setter for the port number
 	 * 
 	 * @param port
 	 *            the port number to set
 	 */
 	public void setPort(int port) {
 		endpoint.setPort(port);
 	}
 
 	/**
 	 * @return the IP address
 	 */
 	public InetAddress getAddress() {
 		return endpoint.getAddress();
 	}
 
 	/**
 	 * Setter for the IP address
 	 * 
 	 * @param ia
 	 *            the IP address to set
 	 */
 	public void setAddress(InetAddress ia) {
 		endpoint.setAddress(ia);
 	}
 
 	/**
 	 * @return TCP NO DELAY
 	 */
 	public boolean getTcpNoDelay() {
 		return endpoint.getTcpNoDelay();
 	}
 
 	/**
 	 * @param tcpNoDelay
 	 */
 	public void setTcpNoDelay(boolean tcpNoDelay) {
 		endpoint.setTcpNoDelay(tcpNoDelay);
 	}
 
 	/**
 	 * @return the soLinger
 	 */
 	public int getSoLinger() {
 		return endpoint.getSoLinger();
 	}
 
 	/**
 	 * @param soLinger
 	 *            the soLinger to set
 	 */
 	public void setSoLinger(int soLinger) {
 		endpoint.setSoLinger(soLinger);
 	}
 
 	/**
 	 * @return the socket timeout
 	 */
 	public int getSoTimeout() {
 		return endpoint.getSoTimeout();
 	}
 
 	/**
 	 * Setter for the socket timeout
 	 * 
 	 * @param soTimeout
 	 */
 	public void setSoTimeout(int soTimeout) {
 		endpoint.setSoTimeout(soTimeout);
 	}
 
 	/**
 	 * @return <tt>TRUE</tt> if the reverse connection is enabled, else
 	 *         <tt>FALSE</tt>
 	 */
 	public boolean getReverseConnection() {
 		return endpoint.isReverseConnection();
 	}
 
 	/**
 	 * Set the reverse connection
 	 * 
 	 * @param reverseConnection
 	 */
 	public void setReverseConnection(boolean reverseConnection) {
 		endpoint.setReverseConnection(reverseConnection);
 	}
 
 	/**
 	 * @return <tt>TRUE</tt> if the defer accept is enabled, else <tt>FALSE</tt>
 	 */
 	public boolean getDeferAccept() {
 		return endpoint.getDeferAccept();
 	}
 
 	/**
 	 * Set the defer accept
 	 * 
 	 * @param deferAccept
 	 */
 	public void setDeferAccept(boolean deferAccept) {
 		endpoint.setDeferAccept(deferAccept);
 	}
 
 	/**
 	 * The number of seconds Tomcat will wait for a subsequent request before
 	 * closing the connection.
 	 * 
 	 * @return the keep alive timeout value
 	 */
 	public int getKeepAliveTimeout() {
 		return endpoint.getKeepAliveTimeout();
 	}
 
 	/**
 	 * Set the keep alive timeout value
 	 * 
 	 * @param timeout
 	 */
 	public void setKeepAliveTimeout(int timeout) {
 		endpoint.setKeepAliveTimeout(timeout);
 	}
 
 	/**
 	 * @return the user send file boolean value
 	 */
 	public boolean getUseSendfile() {
 		return endpoint.getUseSendfile();
 	}
 
 	/**
 	 * Set the user send file
 	 * 
 	 * @param useSendfile
 	 */
 	public void setUseSendfile(boolean useSendfile) {
 		endpoint.setUseSendfile(useSendfile);
 	}
 
 	/**
 	 * @return the send file size
 	 */
 	public int getSendfileSize() {
 		return endpoint.getSendfileSize();
 	}
 
 	/**
 	 * @param sendfileSize
 	 */
 	public void setSendfileSize(int sendfileSize) {
 		endpoint.setSendfileSize(sendfileSize);
 	}
 
 	/**
 	 * Return the Keep-Alive policy for the connection.
 	 * 
 	 * @return keep-alive
 	 */
 	public boolean getKeepAlive() {
 		return ((maxKeepAliveRequests != 0) && (maxKeepAliveRequests != 1));
 	}
 
 	/**
 	 * Set the keep-alive policy for this connection.
 	 * 
 	 * @param keepAlive
 	 */
 	public void setKeepAlive(boolean keepAlive) {
 		if (!keepAlive) {
 			setMaxKeepAliveRequests(1);
 		}
 	}
 
 	// -------------------- Various implementation classes --------------------
 
 	// -------------------- SSL related properties --------------------
 
 	/**
 	 * SSL engine.
 	 * 
 	 * @return <tt>true</tt> if the SSL is enabled, else <tt>false</tt>
 	 */
 	public boolean isSSLEnabled() {
 		return endpoint.getSSLEnabled();
 	}
 
 	/**
 	 * @param SSLEnabled
 	 */
 	public void setSSLEnabled(boolean SSLEnabled) {
 		endpoint.setSSLEnabled(SSLEnabled);
 	}
 
 	/**
 	 * SSL protocol.
 	 * 
 	 * @return the SSL protocol
 	 */
 	public String getSSLProtocol() {
 		return endpoint.getSSLProtocol();
 	}
 
 	/**
 	 * @param SSLProtocol
 	 */
 	public void setSSLProtocol(String SSLProtocol) {
 		endpoint.setSSLProtocol(SSLProtocol);
 	}
 
 	/**
 	 * SSL password (if a cert is encrypted, and no password has been provided,
 	 * a callback will ask for a password).
 	 * 
 	 * @return the SSL password
 	 */
 	public String getSSLPassword() {
 		return endpoint.getSSLPassword();
 	}
 
 	/**
 	 * @param SSLPassword
 	 */
 	public void setSSLPassword(String SSLPassword) {
 		endpoint.setSSLPassword(SSLPassword);
 	}
 
 	/**
 	 * SSL cipher suite.
 	 * 
 	 * @return the SSL cipher suite
 	 */
 	public String getSSLCipherSuite() {
 		return endpoint.getSSLCipherSuite();
 	}
 
 	/**
 	 * @param SSLCipherSuite
 	 */
 	public void setSSLCipherSuite(String SSLCipherSuite) {
 		endpoint.setSSLCipherSuite(SSLCipherSuite);
 	}
 
 	/**
 	 * SSL certificate file.
 	 * 
 	 * @return SSL certificate file
 	 */
 	public String getSSLCertificateFile() {
 		return endpoint.getSSLCertificateFile();
 	}
 
 	/**
 	 * @param SSLCertificateFile
 	 */
 	public void setSSLCertificateFile(String SSLCertificateFile) {
 		endpoint.setSSLCertificateFile(SSLCertificateFile);
 	}
 
 	/**
 	 * SSL certificate key file.
 	 * 
 	 * @return SSL certificate key file
 	 */
 	public String getSSLCertificateKeyFile() {
 		return endpoint.getSSLCertificateKeyFile();
 	}
 
 	/**
 	 * @param SSLCertificateKeyFile
 	 */
 	public void setSSLCertificateKeyFile(String SSLCertificateKeyFile) {
 		endpoint.setSSLCertificateKeyFile(SSLCertificateKeyFile);
 	}
 
 	/**
 	 * SSL certificate chain file.
 	 * 
 	 * @return SSL certificate chain file
 	 */
 	public String getSSLCertificateChainFile() {
 		return endpoint.getSSLCertificateChainFile();
 	}
 
 	/**
 	 * @param SSLCertificateChainFile
 	 */
 	public void setSSLCertificateChainFile(String SSLCertificateChainFile) {
 		endpoint.setSSLCertificateChainFile(SSLCertificateChainFile);
 	}
 
 	/**
 	 * SSL CA certificate path.
 	 * 
 	 * @return SSL CA certificate path
 	 */
 	public String getSSLCACertificatePath() {
 		return endpoint.getSSLCACertificatePath();
 	}
 
 	/**
 	 * @param SSLCACertificatePath
 	 */
 	public void setSSLCACertificatePath(String SSLCACertificatePath) {
 		endpoint.setSSLCACertificatePath(SSLCACertificatePath);
 	}
 
 	/**
 	 * SSL CA certificate file.
 	 * 
 	 * @return SSL CA certificate file
 	 */
 	public String getSSLCACertificateFile() {
 		return endpoint.getSSLCACertificateFile();
 	}
 
 	/**
 	 * @param SSLCACertificateFile
 	 */
 	public void setSSLCACertificateFile(String SSLCACertificateFile) {
 		endpoint.setSSLCACertificateFile(SSLCACertificateFile);
 	}
 
 	/**
 	 * SSL CA revocation path.
 	 * 
 	 * @return SSL CA revocation path
 	 */
 	public String getSSLCARevocationPath() {
 		return endpoint.getSSLCARevocationPath();
 	}
 
 	/**
 	 * @param SSLCARevocationPath
 	 */
 	public void setSSLCARevocationPath(String SSLCARevocationPath) {
 		endpoint.setSSLCARevocationPath(SSLCARevocationPath);
 	}
 
 	/**
 	 * SSL CA revocation file.
 	 * 
 	 * @return the SSL CA revocation file
 	 */
 	public String getSSLCARevocationFile() {
 		return endpoint.getSSLCARevocationFile();
 	}
 
 	/**
 	 * @param SSLCARevocationFile
 	 */
 	public void setSSLCARevocationFile(String SSLCARevocationFile) {
 		endpoint.setSSLCARevocationFile(SSLCARevocationFile);
 	}
 
 	/**
 	 * SSL verify client.
 	 * 
 	 * @return SSLVerifyClient
 	 */
 	public String getSSLVerifyClient() {
 		return endpoint.getSSLVerifyClient();
 	}
 
 	/**
 	 * @param SSLVerifyClient
 	 */
 	public void setSSLVerifyClient(String SSLVerifyClient) {
 		endpoint.setSSLVerifyClient(SSLVerifyClient);
 	}
 
 	/**
 	 * SSL verify depth.
 	 * 
 	 * @return the SSL verify depth
 	 */
 	public int getSSLVerifyDepth() {
 		return endpoint.getSSLVerifyDepth();
 	}
 
 	/**
 	 * @param SSLVerifyDepth
 	 *            the SSL verify depth
 	 */
 	public void setSSLVerifyDepth(int SSLVerifyDepth) {
 		endpoint.setSSLVerifyDepth(SSLVerifyDepth);
 	}
 
 	// -------------------- Connection handler --------------------
 
 	/**
 	 * {@code Http11ConnectionHandler}
 	 * 
 	 * Created on Jan 13, 2012 at 10:45:44 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	static class Http11ConnectionHandler implements NioEndpoint.Handler {
 
 		protected Http11NioProtocol proto;
 		protected AtomicLong registerCount = new AtomicLong(0);
 		protected RequestGroupInfo global = new RequestGroupInfo();
 
 		protected ConcurrentHashMap<Long, Http11NioProcessor> connections = new ConcurrentHashMap<Long, Http11NioProcessor>();
 		protected ConcurrentLinkedQueue<Http11NioProcessor> recycledProcessors = new ConcurrentLinkedQueue<Http11NioProcessor>() {
 			/**
 			 * 
 			 */
 			private static final long serialVersionUID = 1L;
 			protected AtomicInteger size = new AtomicInteger(0);
 
 			@Override
 			public boolean offer(Http11NioProcessor processor) {
 				boolean offer = (proto.processorCache == -1) ? true
 						: (size.get() < proto.processorCache);
 				// avoid over growing our cache or add after we have stopped
 				boolean result = false;
 				if (offer) {
 					result = super.offer(processor);
 					if (result) {
 						size.incrementAndGet();
 					}
 				}
 				if (!result)
 					unregister(processor);
 				return result;
 			}
 
 			@Override
 			public Http11NioProcessor poll() {
 				Http11NioProcessor result = super.poll();
 				if (result != null) {
 					size.decrementAndGet();
 				}
 				return result;
 			}
 
 			@Override
 			public void clear() {
 				Http11NioProcessor next = poll();
 				while (next != null) {
 					unregister(next);
 					next = poll();
 				}
 				super.clear();
 				size.set(0);
 			}
 		};
 
 		/**
 		 * Create a new instance of {@code Http11ConnectionHandler}
 		 * 
 		 * @param proto
 		 */
 		Http11ConnectionHandler(Http11NioProtocol proto) {
 			this.proto = proto;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.apache.tomcat.util.net.NioEndpoint.Handler#event(java.nio.channels
 		 * .AsynchronousSocketChannel, org.apache.tomcat.util.net.ChannelStatus)
 		 */
 		@Override
 		public SocketState event(NioChannel channel, SocketStatus status) {

 			Http11NioProcessor processor = connections.get(channel.getId());
 
 			SocketState state = SocketState.CLOSED;
 			if (processor != null) {
 				processor.startProcessing();
 				// Call the appropriate event
 				try {
 					state = processor.event(status);
 				} catch (java.net.SocketException e) {
 					// SocketExceptions are normal
 					Http11NioProtocol.log.debug(
 							sm.getString("http11protocol.proto.socketexception.debug"), e);
 				} catch (java.io.IOException e) {
 					// IOExceptions are normal
 					Http11NioProtocol.log.debug(
 							sm.getString("http11protocol.proto.ioexception.debug"), e);
 				}
 				// Future developers: if you discover any other
 				// rare-but-nonfatal exceptions, catch them here, and log as
 				// above.
 				catch (Throwable e) {
 					// any other exception or error is odd. Here we log it
 					// with "ERROR" level, so it will show up even on
 					// less-than-verbose logs.
 					Http11NioProtocol.log.error(sm.getString("http11protocol.proto.error"), e);
 				} finally {
 					if (state != SocketState.LONG) {
 						connections.remove(channel.getId());
 						recycledProcessors.offer(processor);
 						if (proto.endpoint.isRunning() && state == SocketState.OPEN) {
 							final NioChannel ch = channel;
 							proto.endpoint.removeEventChannel(channel);
 							if (ch.isReadReady()) {
 								ch.awaitRead(proto.getKeepAliveTimeout(), TimeUnit.MILLISECONDS,
 										proto.endpoint,
 										new CompletionHandler<Integer, NioEndpoint>() {
 
 											@Override
 											public void completed(Integer nBytes,
 													NioEndpoint endpoint) {
 												if (nBytes < 0) {
 													failed(new ClosedChannelException(), endpoint);
 												} else {
 													endpoint.processChannel(ch, null);
 												}
 											}
 
 											@Override
 											public void failed(Throwable exc, NioEndpoint endpoint) {
 												endpoint.closeChannel(ch);
 											}
 										});
 							}
 						}
 					} else {
 						if (proto.endpoint.isRunning()) {
 							proto.endpoint.addEventChannel(channel, processor.getTimeout(),
 									processor.getReadNotifications(),
 									processor.getWriteNotification(),
 									processor.getResumeNotification(), false);
 						}
 					}
 					processor.endProcessing();
 				}
 			}
 
 			return state;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see
 		 * org.apache.tomcat.util.net.NioEndpoint.Handler#process(java.nio.channels
 		 * .AsynchronousSocketChannel)
 		 */
 		@Override
 		public SocketState process(NioChannel channel) {
 			Http11NioProcessor processor = recycledProcessors.poll();
 			try {
 				if (processor == null) {
 					processor = createProcessor();
 				}
 
 				if (proto.secure && (proto.sslImplementation != null)) {
 					processor.setSSLSupport(proto.sslImplementation.getSSLSupport(channel));
 				} else {
 					processor.setSSLSupport(null);
 				}
 
 				SocketState state = processor.process(channel);
 
 				if (state == SocketState.LONG) {
 					// Associate the connection with the processor. The next
 					// request
 					// processed by this thread will use either a new or a
 					// recycled
 					// processor.
 					connections.put(channel.getId(), processor);
					if (processor.isAvailable() && processor.getReadNotifications()) {
 						// Call a read event right away
 						state = event(channel, SocketStatus.OPEN_READ);
 					} else {
 						proto.endpoint.addEventChannel(channel, processor.getTimeout(),
 								processor.getReadNotifications(), false,
 								processor.getResumeNotification(), false);
 					}
 				} else {
 					recycledProcessors.offer(processor);
 				}
 				return state;
 
 			} catch (IOException e) {
 				if (e instanceof java.net.SocketException) {
 					// SocketExceptions are normal
 					Http11NioProtocol.log.debug(
 							sm.getString("http11protocol.proto.socketexception.debug"), e);
 				} else {
 					// IOExceptions are normal
 					Http11NioProtocol.log.debug(
 							sm.getString("http11protocol.proto.ioexception.debug"), e);
 				}
 			}
 			// Future developers: if you discover any other
 			// rare-but-non-fatal exceptions, catch them here, and log as
 			// above.
 			catch (Throwable e) {
 				// any other exception or error is odd. Here we log it
 				// with "ERROR" level, so it will show up even on
 				// less-than-verbose logs.
 				Http11NioProtocol.log.error(sm.getString("http11protocol.proto.error"), e);
 			}
 			recycledProcessors.offer(processor);
 			return SocketState.CLOSED;
 		}
 
 		/**
 		 * @return
 		 */
 		protected Http11NioProcessor createProcessor() {
 			Http11NioProcessor processor = new Http11NioProcessor(proto.maxHttpHeaderSize,
 					proto.endpoint);
 			processor.setAdapter(proto.adapter);
 			processor.setMaxKeepAliveRequests(proto.maxKeepAliveRequests);
 			processor.setTimeout(proto.timeout);
 			processor.setDisableUploadTimeout(proto.disableUploadTimeout);
 			processor.setCompressionMinSize(proto.compressionMinSize);
 			processor.setCompression(proto.compression);
 			processor.setNoCompressionUserAgents(proto.noCompressionUserAgents);
 			processor.setCompressableMimeTypes(proto.compressableMimeTypes);
 			processor.setRestrictedUserAgents(proto.restrictedUserAgents);
 			processor.setMaxSavePostSize(proto.maxSavePostSize);
 			processor.setServer(proto.server);
 			register(processor);
 			return processor;
 		}
 
 		/**
 		 * @param processor
 		 */
 		protected void register(Http11NioProcessor processor) {
 			RequestInfo rp = processor.getRequest().getRequestProcessor();
 			rp.setGlobalProcessor(global);
 			if (org.apache.tomcat.util.Constants.ENABLE_MODELER && proto.getDomain() != null) {
 				synchronized (this) {
 					try {
 						long count = registerCount.incrementAndGet();
 						ObjectName rpName = new ObjectName(proto.getDomain()
 								+ ":type=RequestProcessor,worker=" + proto.getName()
 								+ ",name=HttpRequest" + count);
 						if (log.isDebugEnabled()) {
 							log.debug("Register " + rpName);
 						}
 						Registry.getRegistry(null, null).registerComponent(rp, rpName, null);
 						rp.setRpName(rpName);
 					} catch (Exception e) {
 						e.printStackTrace();
 						log.warn("Error registering request");
 					}
 				}
 			}
 		}
 
 		/**
 		 * @param processor
 		 */
 		protected void unregister(Http11NioProcessor processor) {
 			RequestInfo rp = processor.getRequest().getRequestProcessor();
 			rp.setGlobalProcessor(null);
 			if (org.apache.tomcat.util.Constants.ENABLE_MODELER && proto.getDomain() != null) {
 				synchronized (this) {
 					try {
 						ObjectName rpName = rp.getRpName();
 						if (log.isDebugEnabled()) {
 							log.debug("Unregister " + rpName);
 						}
 						Registry.getRegistry(null, null).unregisterComponent(rpName);
 						rp.setRpName(null);
 					} catch (Exception e) {
 						e.printStackTrace();
 						log.warn("Error unregistering request", e);
 					}
 				}
 			}
 		}
 	}
 
 }
