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
 
 package org.apache.tomcat.util.net;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.BindException;
 import java.net.StandardSocketOptions;
 import java.nio.ByteBuffer;
 import java.nio.channels.AsynchronousChannelGroup;
 import java.nio.channels.AsynchronousServerSocketChannel;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.CompletionHandler;
 import java.nio.file.StandardOpenOption;
 import java.util.concurrent.ConcurrentHashMap;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ThreadFactory;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.atomic.AtomicInteger;
 
 import javax.net.ssl.SSLContext;
 import javax.net.ssl.SSLHandshakeException;
 
 import org.apache.tomcat.util.net.NioEndpoint.Handler.SocketState;
 import org.apache.tomcat.util.net.jsse.NioJSSESocketChannelFactory;
 import org.jboss.logging.Logger;
 
 /**
  * {@code NioEndpoint} NIO2 endpoint, providing the following services:
  * <ul>
  * <li>Socket channel acceptor thread</li>
  * <li>Simple Worker thread pool, with possible use of executors</li>
  * </ul>
  * 
  * Created on Dec 13, 2011 at 9:41:53 AM
  * 
  * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
  */
 public class NioEndpoint extends AbstractEndpoint {
 
 	protected static Logger logger = Logger.getLogger(NioEndpoint.class);
 
 	private AsynchronousServerSocketChannel listener;
 	private ThreadFactory threadFactory;
 	private ConcurrentHashMap<Long, NioChannel> connections;
 	private ConcurrentLinkedQueue<ChannelProcessor> recycledChannelProcessors;
 
 	/**
 	 * Handling of accepted sockets.
 	 */
 	protected Handler handler = null;
 
 	private EventPoller eventPoller;
 
 	protected NioServerSocketChannelFactory serverSocketChannelFactory = null;
 
 	/**
 	 * SSL context.
 	 */
 	protected SSLContext sslContext;
 
 	/**
 	 * The static file sender.
 	 */
 	protected Sendfile sendfile;
 
 	/**
 	 * Create a new instance of {@code NioEndpoint}
 	 */
 	public NioEndpoint() {
 		super();
 	}
 
 	/**
 	 * @param handler
 	 */
 	public void setHandler(Handler handler) {
 		this.handler = handler;
 	}
 
 	/**
 	 * @return the handler
 	 */
 	public Handler getHandler() {
 		return handler;
 	}
 
 	/**
 	 * Number of keep-alive channels.
 	 * 
 	 * @return the number of connection
 	 */
 	public int getKeepAliveCount() {
 		return this.eventPoller.channelList.size();
 	}
 
 	/**
 	 * Return the amount of threads that are managed by the pool.
 	 * 
 	 * @return the amount of threads that are managed by the pool
 	 */
 	public int getCurrentThreadCount() {
 		return curThreads;
 	}
 
 	/**
 	 * Return the amount of threads currently busy.
 	 * 
 	 * @return the amount of threads currently busy
 	 */
 	public int getCurrentThreadsBusy() {
 		return curThreadsBusy;
 	}
 
 	/**
 	 * Getter for sslContext
 	 * 
 	 * @return the sslContext
 	 */
 	public SSLContext getSslContext() {
 		return this.sslContext;
 	}
 
 	/**
 	 * Setter for the sslContext
 	 * 
 	 * @param sslContext
 	 *            the sslContext to set
 	 */
 	public void setSslContext(SSLContext sslContext) {
 		this.sslContext = sslContext;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.tomcat.util.net.AbstractEndpoint#init()
 	 */
 	@Override
 	public void init() throws Exception {
 		if (initialized) {
 			return;
 		}
 
 		if (this.soTimeout < 0) {
 			this.soTimeout = DEFAULT_SO_TIMEOUT;
 		}
 
 		if (this.keepAliveTimeout < 0) {
 			this.keepAliveTimeout = this.soTimeout;
 		}
 
 		// Initialize thread count defaults for acceptor
 		if (acceptorThreadCount <= 0) {
 			acceptorThreadCount = 1;
 		}
 
 		// Create the thread factory
 		if (this.threadFactory == null) {
 			this.threadFactory = new DefaultThreadFactory(getName() + "-", threadPriority);
 		}
 
 		if (this.connections == null) {
 			this.connections = new ConcurrentHashMap<>();
 		}
 
 		if (this.recycledChannelProcessors == null) {
 			this.recycledChannelProcessors = new ConcurrentLinkedQueue<>();
 		}
 
 		// If the executor is not set, create it with a fixed thread pool
 		if (this.executor == null) {
 			this.executor = Executors.newFixedThreadPool(this.maxThreads, this.threadFactory);
 		}
 
 		ExecutorService executorService = (ExecutorService) this.executor;
 		AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup
 				.withThreadPool(executorService);
 
 		if (this.serverSocketChannelFactory == null) {
 			this.serverSocketChannelFactory = NioServerSocketChannelFactory
 					.createServerSocketChannelFactory(threadGroup, SSLEnabled);
 		} else {
 			this.serverSocketChannelFactory.threadGroup = threadGroup;
 		}
 
 		// Initialize the SSL context if the SSL mode is enabled
 		if (SSLEnabled) {
 			NioJSSESocketChannelFactory factory = (NioJSSESocketChannelFactory) this.serverSocketChannelFactory;
 			sslContext = factory.getSslContext();
 		}
 
 		// Initialize the channel factory
 		this.serverSocketChannelFactory.init();
 
 		if (listener == null) {
 			try {
 				if (address == null) {
 					listener = this.serverSocketChannelFactory.createServerChannel(port, backlog);
 				} else {
 					listener = this.serverSocketChannelFactory.createServerChannel(port, backlog,
 							address);
 				}
 
 				listener.setOption(StandardSocketOptions.SO_REUSEADDR, this.reuseAddress);
 			} catch (BindException be) {
 				logger.fatal(be.getMessage(), be);
 				throw new BindException(be.getMessage() + " "
 						+ (address == null ? "<null>" : address.toString()) + ":" + port);
 			}
 		}
 
 		initialized = true;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.tomcat.util.net.AbstractEndpoint#start()
 	 */
 	@Override
 	public void start() throws Exception {
 		// Initialize channel if not done before
 		if (!initialized) {
 			init();
 		}
 		if (!running) {
 			running = true;
 			paused = false;
 
 			// Start acceptor threads
 			for (int i = 0; i < acceptorThreadCount; i++) {
 				Thread acceptorThread = this.threadFactory.newThread(new Acceptor());
 				acceptorThread.setDaemon(daemon);
 				acceptorThread.start();
 			}
 
 			// Start sendfile thread
 			if (useSendfile) {
 				sendfile = new Sendfile();
 				sendfile.init();
 				Thread sendfileThread = this.threadFactory.newThread(sendfile);
 				sendfileThread.setName(getName() + "-SendFile");
 				sendfileThread.setPriority(threadPriority);
 				sendfileThread.setDaemon(true);
 				sendfileThread.start();
 			}
 
 			// Starting the event poller
 			this.eventPoller = new EventPoller(this.maxThreads);
 			this.eventPoller.init();
 			Thread eventPollerThread = this.threadFactory.newThread(this.eventPoller);
 			eventPollerThread.setName(getName() + "-EventPoller");
 			eventPollerThread.setDaemon(true);
 			eventPollerThread.start();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.tomcat.util.net.AbstractEndpoint#stop()
 	 */
 	@Override
 	public void stop() {
 		if (running) {
 			running = false;
 			unlockAccept();
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.apache.tomcat.util.net.AbstractEndpoint#destroy()
 	 */
 	@Override
 	public void destroy() throws Exception {
 		if (running) {
 			stop();
 		}
 		if (listener != null) {
 			try {
 				listener.close();
 			} catch (IOException e) {
 				logger.error(sm.getString("endpoint.err.close"), e);
 			} finally {
 				listener = null;
 			}
 		}
 
 		if (this.sendfile != null) {
 			sendfile.destroy();
 		}
 
 		for (NioChannel ch : this.connections.values()) {
 			try {
 				ch.close();
 			} catch (Throwable t) {
 				// Nothing to do
 			}
 		}
 
 		this.connections.clear();
 
 		this.serverSocketChannelFactory.destroy();
 		this.serverSocketChannelFactory = null;
 		this.recycledChannelProcessors = null;
 
 		initialized = false;
 	}
 
 	/**
 	 * Configure the channel options before being processed
 	 */
 	protected boolean setChannelOptions(NioChannel channel) {
 		// Process the connection
 		try {
 			// Set channel options: timeout, linger, etc
 			if (keepAliveTimeout > 0) {
 				channel.setOption(StandardSocketOptions.SO_KEEPALIVE, Boolean.TRUE);
 			}
 			if (soLinger >= 0) {
 				channel.setOption(StandardSocketOptions.SO_LINGER, soLinger);
 			}
 			if (tcpNoDelay) {
 				channel.setOption(StandardSocketOptions.TCP_NODELAY, tcpNoDelay);
 			}
 
 			channel.setOption(StandardSocketOptions.SO_REUSEADDR, Boolean.TRUE);
 
 			// Initialize the channel
 			serverSocketChannelFactory.initChannel(channel);
 			// Start SSL handshake if SSL is enabled
 			serverSocketChannelFactory.handshake(channel);
 			return true;
 		} catch (Throwable t) {
 			// logger.error(t.getMessage(), t);
 			if (logger.isDebugEnabled()) {
 				if (t instanceof SSLHandshakeException) {
 					logger.debug(sm.getString("endpoint.err.handshake"), t);
 				} else {
 					logger.debug(sm.getString("endpoint.err.unexpected"), t);
 				}
 			}
 			// Tell to close the channel
 			return false;
 		}
 	}
 
 	/**
 	 * Add specified channel and associated pool to the poller. The added will
 	 * be added to a temporary array, and polled first after a maximum amount of
 	 * time equal to pollTime (in most cases, latency will be much lower,
 	 * however). Note: If both read and write are false, the socket will only be
 	 * checked for timeout; if the socket was already present in the poller, a
 	 * callback event will be generated and the socket will be removed from the
 	 * poller.
 	 * 
 	 * @param channel
 	 *            to add to the poller
 	 * @param timeout
 	 *            to use for this connection
 	 * @param read
 	 *            to do read polling
 	 * @param write
 	 *            to do write polling
 	 * @param resume
 	 *            to send a callback event
 	 * @param wakeup
 	 */
 	public void addEventChannel(NioChannel channel, long timeout, boolean read, boolean write,
 			boolean resume, boolean wakeup) {
 
 		int flags = (read ? ChannelInfo.READ : 0) | (write ? ChannelInfo.WRITE : 0)
 				| (resume ? ChannelInfo.RESUME : 0) | (wakeup ? ChannelInfo.WAKEUP : 0);
 
 		addEventChannel(channel, timeout, flags);
 	}
 
 	/**
 	 * 
 	 * 
 	 * @param channel
 	 * @param timeout
 	 * @param flags
 	 */
 	public void addEventChannel(NioChannel channel, long timeout, int flags) {
 
 		long eventTimeout = timeout <= 0 ? keepAliveTimeout : timeout;
 
 		if (eventTimeout <= 0) {
 			// Always put a timeout in
 			eventTimeout = soTimeout > 0 ? soTimeout : Integer.MAX_VALUE;
 		}
 
 		if (!this.eventPoller.add(channel, eventTimeout, flags)) {
 			closeChannel(channel);
 		}
 	}
 
 	/**
 	 * Remove the channel from the list of venet channels
 	 * 
 	 * @param channel
 	 */
 	public void removeEventChannel(NioChannel channel) {
 		if (channel != null) {
 			this.eventPoller.remove(channel);
 		}
 	}
 
 	/**
 	 * Add a send file data to the queue of static files
 	 * 
 	 * @param data
 	 * @return <tt>TRUE</tt> if the object is added successfully to the list of
 	 *         {@code SendfileData}, else <tt>FALSE</tt>
 	 */
 	public boolean addSendfileData(SendfileData data) {
 		if (this.sendfile != null) {
 			return this.sendfile.add(data);
 		}
 
 		return false;
 	}
 
 	/**
 	 * Process given channel.
 	 */
 	protected boolean processChannelWithOptions(NioChannel channel) {
 		try {
 			executor.execute(new ChannelWithOptionsProcessor(channel));
 		} catch (Throwable t) {
 			// This means we got an OOM or similar creating a thread, or that
 			// the pool and its queue are full
 			logger.error(sm.getString("endpoint.process.fail"), t);
 			return false;
 		}
 		return true;
 	}
 
 	/**
 	 * Process given channel for an event.
 	 * 
 	 * @param channel
 	 * @param status
 	 * @return <tt>true</tt> if the processing of the channel finish
 	 *         successfully else <tt>false</tt>
 	 */
 	public boolean processChannel(NioChannel channel, SocketStatus status) {
 		try {
 			ChannelProcessor processor = getChannelProcessor(channel, status);
 			this.executor.execute(processor);
 			return true;
 		} catch (Throwable t) {
 			// This means we got an OOM or similar creating a thread, or that
 			// the pool and its queue are full
 			logger.error(sm.getString("endpoint.process.fail"), t);
 			return false;
 		}
 	}
 
 	/**
 	 * @return peek a processor from the recycled processors list
 	 */
 	private ChannelProcessor getChannelProcessor(NioChannel channel, SocketStatus status) {
 		ChannelProcessor processor = this.recycledChannelProcessors.poll();
 		if (processor == null) {
 			processor = new ChannelProcessor(channel, status);
 		} else {
 			processor.setChannel(channel);
 			processor.setStatus(status);
 		}
 		return processor;
 	}
 
 	/**
 	 * Try to add the specified channel to the list of connections.
 	 * 
 	 * @param channel
 	 *            the channel to be added
 	 * @return <tt>true</tt> if the channel is added successfully, else
 	 *         <tt>false</tt>
 	 */
 	private boolean addChannel(NioChannel channel) {
 		if (this.counter.get() < this.maxConnections && channel.isOpen()) {
 			if (this.connections.get(channel.getId()) == null
 					|| this.connections.get(channel.getId()).isClosed()) {
 				this.connections.put(channel.getId(), channel);
 				this.counter.incrementAndGet();
 				return true;
 			}
 		}
 
 		return false;
 	}
 
 	/**
 	 * Getter for serverSocketChannelFactory
 	 * 
 	 * @return the serverSocketChannelFactory
 	 */
 	public NioServerSocketChannelFactory getServerSocketChannelFactory() {
 		return this.serverSocketChannelFactory;
 	}
 
 	/**
 	 * Setter for the serverSocketChannelFactory
 	 * 
 	 * @param serverSocketChannelFactory
 	 *            the serverSocketChannelFactory to set
 	 */
 	public void setServerSocketChannelFactory(
 			NioServerSocketChannelFactory serverSocketChannelFactory) {
 		this.serverSocketChannelFactory = serverSocketChannelFactory;
 	}
 
 	/**
 	 * Close the specified channel and remove it from the list of open
 	 * connections
 	 * 
 	 * @param channel
 	 *            the channel to be closed
 	 */
 	public void closeChannel(NioChannel channel) {
 		if (channel != null) {
 			try {
 				channel.close();
 			} catch (IOException e) {
 				logger.error(e.getMessage(), e);
 			} finally {
 				if (this.connections.remove(channel.getId()) != null) {
 					this.counter.decrementAndGet();
 				}
 			}
 		}
 	}
 
 	/**
 	 * @return if the send file is supported, peek up a {@link SendfileData}
 	 *         from the pool, else <tt>null</tt>
 	 */
 	public SendfileData getSendfileData() {
 		return this.sendfile != null ? this.sendfile.getSendfileData() : new SendfileData();
 	}
 
 	/**
 	 * {@code Acceptor}
 	 * 
 	 * <p>
 	 * Server socket acceptor thread.
 	 * </p>
 	 * Created on Mar 6, 2012 at 9:13:34 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected class Acceptor implements Runnable {
 
 		/**
 		 * The background thread that listens for incoming TCP/IP connections
 		 * and hands them off to an appropriate processor.
 		 */
 		public void run() {
 
 			// Loop until we receive a shutdown command
 			while (running) {
 				// Loop if end point is paused
 				while (paused) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// Ignore
 					}
 				}
 
 				// Accept the next incoming connection from the server channel
 				try {
 					final NioChannel channel = serverSocketChannelFactory.acceptChannel(listener);
 					// Using the short-circuit AND operator
 					if (!(addChannel(channel) && setChannelOptions(channel) && channel.isOpen() && processChannel(
 							channel, null))) {
 						logger.info("Fail processing the channel");
 						closeChannel(channel);
 					}
 				} catch (Exception exp) {
 					if (running) {
 						logger.error(sm.getString("endpoint.accept.fail"), exp);
 					}
 				} catch (Throwable t) {
 					logger.error(sm.getString("endpoint.accept.fail"), t);
 				}
 			}
 		}
 	}
 
 	/**
 	 * {@code ChannelInfo}
 	 * <p>
 	 * Channel list class, used to avoid using a possibly large amount of
 	 * objects with very little actual use.
 	 * </p>
 	 * Created on Apr 13, 2012 at 11:13:13 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	public static class ChannelInfo {
 		/**
 		 * 
 		 */
 		public static final int READ = 1;
 		/**
 		 * 
 		 */
 		public static final int WRITE = 2;
 		/**
 		 * 
 		 */
 		public static final int RESUME = 4;
 		/**
 		 * 
 		 */
 		public static final int WAKEUP = 8;
 
 		protected NioChannel channel;
 		protected long timeout;
 		protected int flags;
 
 		/**
 		 * Create a new instance of {@code ChannelInfo}
 		 */
 		public ChannelInfo() {
 			this(null, 0, 0);
 		}
 
 		/**
 		 * Create a new instance of {@code ChannelInfo}
 		 * 
 		 * @param channel
 		 *            the channel
 		 * @param timeout
 		 *            the channel timeout. The default time unit is
 		 *            {@code java.util.concurrent.TimeUnit.MILLISECONDS}
 		 * @param flags
 		 */
 		public ChannelInfo(NioChannel channel, long timeout, int flags) {
 			this.channel = channel;
 			this.timeout = timeout;
 			this.flags = flags;
 		}
 
 		/**
 		 * Create a new instance of {@code ChannelInfo}
 		 * 
 		 * @param channel
 		 * @param timeout
 		 * @param unit
 		 * @param flags
 		 */
 		public ChannelInfo(NioChannel channel, long timeout, TimeUnit unit, int flags) {
 			this(channel, TimeUnit.MILLISECONDS.convert(timeout, unit), flags);
 		}
 
 		/**
 		 * Recycle this channel info for next use
 		 */
 		public void recycle() {
 			this.channel = null;
 			this.timeout = 0;
 			this.flags = 0;
 		}
 
 		/**
 		 * @return the read flag
 		 */
 		public boolean read() {
 			return (flags & READ) == READ;
 		}
 
 		/**
 		 * Set the <code>read</code> flag. If the parameter is true, the read
 		 * flag will have the value 1 else 0.
 		 * 
 		 * @param read
 		 */
 		public void read(boolean read) {
 			this.flags = (read ? (this.flags | READ) : (this.flags & 0xE));
 		}
 
 		/**
 		 * @return the write flag
 		 */
 		public boolean write() {
 			return (flags & WRITE) == WRITE;
 		}
 
 		/**
 		 * Set the <code>write</code> flag. If the parameter is true, the write
 		 * flag will have the value 1 else 0.
 		 * 
 		 * @param write
 		 */
 		public void write(boolean write) {
 			this.flags = (write ? (this.flags | WRITE) : (this.flags & 0xD));
 		}
 
 		/**
 		 * @return the resume flag
 		 */
 		public boolean resume() {
 			return (flags & RESUME) == RESUME;
 		}
 
 		/**
 		 * Set the <code>resume</code> flag. If the parameter is true, the
 		 * resume flag will have the value 1 else 0.
 		 * 
 		 * @param resume
 		 */
 		public void resume(boolean resume) {
 			this.flags = (resume ? (this.flags | RESUME) : (this.flags & 0xB));
 		}
 
 		/**
 		 * @return the wake up flag
 		 */
 		public boolean wakeup() {
 			return (flags & WAKEUP) == WAKEUP;
 		}
 
 		/**
 		 * Set the <code>wakeup</code> flag. If the parameter is true, the
 		 * wakeup flag will have the value 1 else 0.
 		 * 
 		 * @param wakeup
 		 */
 		public void wakeup(boolean wakeup) {
 			this.flags = (wakeup ? (this.flags | WAKEUP) : (this.flags & 0x7));
 		}
 
 		/**
 		 * Merge the tow flags
 		 * 
 		 * @param flag1
 		 * @param flag2
 		 * @return the result of merging the tow flags
 		 */
 		public static int merge(int flag1, int flag2) {
 			return ((flag1 & READ) | (flag2 & READ)) | ((flag1 & WRITE) | (flag2 & WRITE))
 					| ((flag1 & RESUME) | (flag2 & RESUME)) | ((flag1 & WAKEUP) & (flag2 & WAKEUP));
 		}
 	}
 
 	/**
 	 * {@code Handler}
 	 * 
 	 * <p>
 	 * Bare bones interface used for socket processing. Per thread data is to be
 	 * stored in the ThreadWithAttributes extra folders, or alternately in
 	 * thread local fields.
 	 * </p>
 	 * 
 	 * Created on Mar 6, 2012 at 9:13:07 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	public interface Handler {
 		/**
 		 * {@code ChannelState}
 		 * 
 		 * Created on Dec 12, 2011 at 9:41:06 AM
 		 * 
 		 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 		 */
 		public enum SocketState {
 			/**
 			 * 
 			 */
 			OPEN,
 			/**
 			 * 
 			 */
 			CLOSED,
 			/**
 			 * 
 			 */
 			LONG
 		}
 
 		/**
 		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
 		 * 
 		 * @param channel
 		 *            the {@code org.apache.tomcat.util.net.NioChannel}
 		 * @return a channel state
 		 */
 		public SocketState process(NioChannel channel);
 
 		/**
 		 * Process the specified {@code org.apache.tomcat.util.net.NioChannel}
 		 * 
 		 * @param channel
 		 * @param status
 		 * @return a channel state
 		 */
 		public SocketState event(NioChannel channel, SocketStatus status);
 
 	}
 
 	/**
 	 * {@code ChannelWithOptionsProcessor}
 	 * <p>
 	 * This class is the equivalent of the Worker, but will simply use in an
 	 * external Executor thread pool. This will also set the channel options and
 	 * do the handshake.
 	 * </p>
 	 * Created on Mar 6, 2012 at 9:09:43 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected class ChannelWithOptionsProcessor extends ChannelProcessor {
 
 		/**
 		 * Create a new instance of {@code ChannelWithOptionsProcessor}
 		 * 
 		 * @param channel
 		 */
 		public ChannelWithOptionsProcessor(NioChannel channel) {
 			super(channel);
 		}
 
 		@Override
 		public void run() {
 			boolean ok = true;
 
 			if (!deferAccept) {
 				ok = setChannelOptions(channel);
 			} else {
 				// Process the request from this channel
 				ok = setChannelOptions(channel)
 						&& handler.process(channel) != Handler.SocketState.CLOSED;
 			}
 
 			if (!ok) {
 				// Close the channel
 				closeChannel(channel);
 			}
 
 			channel = null;
 		}
 	}
 
 	/**
 	 * {@code ChannelProcessor}
 	 * <p>
 	 * This class is the equivalent of the Worker, but will simply use in an
 	 * external Executor thread pool.
 	 * </p>
 	 * Created on Mar 6, 2012 at 9:10:06 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected class ChannelProcessor implements Runnable {
 
 		protected NioChannel channel;
 		protected SocketStatus status = null;
 
 		/**
 		 * Create a new instance of {@code ChannelProcessor}
 		 * 
 		 * @param channel
 		 */
 		public ChannelProcessor(NioChannel channel) {
 			this.channel = channel;
 		}
 
 		/**
 		 * Create a new instance of {@code ChannelProcessor}
 		 * 
 		 * @param channel
 		 * @param status
 		 */
 		public ChannelProcessor(NioChannel channel, SocketStatus status) {
 			this(channel);
 			this.status = status;
 		}
 
 		@Override
 		public void run() {
 			try {
 				Handler.SocketState state = ((status == null) ? handler.process(channel) : handler
 						.event(channel, status));
 
 				if (state == SocketState.CLOSED) {
 					closeChannel(channel);
 				}
 			} catch (Throwable th) {
 				if (logger.isDebugEnabled()) {
 					logger.debug(th.getMessage(), th);
 				}
 			} finally {
 				this.recycle();
 			}
 		}
 
 		/**
 		 * Reset this channel processor
 		 */
 		protected void recycle() {
 			this.channel = null;
 			this.status = null;
 			if (recycledChannelProcessors != null) {
 				recycledChannelProcessors.offer(this);
 			}
 		}
 
 		/**
 		 * 
 		 * @param channel
 		 * @param status
 		 */
 		protected void setup(NioChannel channel, SocketStatus status) {
 			this.channel = channel;
 			this.status = status;
 		}
 
 		/**
 		 * @param status
 		 */
 		public void setStatus(SocketStatus status) {
 			this.status = status;
 		}
 
 		/**
 		 * @param channel
 		 */
 		public void setChannel(NioChannel channel) {
 			this.channel = channel;
 		}
 	}
 
 	/**
 	 * {@code EventPoller}
 	 * 
 	 * Created on Mar 26, 2012 at 12:51:53 PM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	public class EventPoller implements Runnable {
 
 		/**
 		 * Last run of maintain. Maintain will run usually every 5s.
 		 */
 		protected long lastMaintain = System.currentTimeMillis();
 
 		protected ConcurrentHashMap<Long, ChannelInfo> channelList;
 		protected ConcurrentLinkedQueue<ChannelInfo> recycledChannelList;
 		private ConcurrentLinkedQueue<CompletionHandler<Integer, NioChannel>> recycledCompletionHandlers;
 		private Object mutex;
 		private int size;
 
 		/**
 		 * Create a new instance of {@code EventPoller}
 		 * 
 		 * @param size
 		 */
 		public EventPoller(int size) {
 			this.size = size;
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		@Override
 		public void run() {
 			while (running) {
 				// Loop if endpoint is paused
 				while (paused) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// Ignore
 					}
 				}
 
 				while (this.channelList.size() < 1 && running) {
 					synchronized (this.mutex) {
 						try {
 							this.mutex.wait(10000);
 						} catch (InterruptedException e) {
 							// NOPE
 						}
 					}
 				}
 
 				while (this.channelList.size() > 0 && running) {
 					maintain();
 					try {
 						Thread.sleep(5000);
 					} catch (InterruptedException e) {
 						// NOPE
 					}
 				}
 
 			}
 		}
 
 		/**
 		 * Check timeouts and raise timeout event
 		 */
 		public void maintain() {
 			long date = System.currentTimeMillis();
 			// Maintain runs at most once every 5s, although it will likely get
 			// called more
 			if ((date - lastMaintain) < 5000L) {
 				return;
 			}
 
 			// Update the last maintain time
 			lastMaintain = date;
 
 			for (ChannelInfo info : this.channelList.values()) {
 				if (date >= info.timeout) {
 					NioChannel ch = info.channel;
 					remove(info);
 					if (!processChannel(ch, SocketStatus.TIMEOUT)) {
 						closeChannel(ch);
 					}
 				}
 			}
 		}
 
 		/**
 		 * Remove the channel having the specified id
 		 * 
 		 * @param id
 		 */
 		protected void remove(long id) {
 			ChannelInfo info = this.channelList.remove(id);
 			offer(info);
 		}
 
 		/**
 		 * @param channel
 		 */
 		public void remove(NioChannel channel) {
 			if (channel != null) {
 				remove(channel.getId());
 			}
 		}
 
 		/**
 		 * @param info
 		 */
 		public void remove(ChannelInfo info) {
 			if (info != null) {
 				remove(info.channel);
 			}
 		}
 
 		/**
 		 * Initialize the event poller
 		 */
 		public void init() {
 			this.mutex = new Object();
 			this.channelList = new ConcurrentHashMap<>(this.size);
 			this.recycledChannelList = new ConcurrentLinkedQueue<>();
 			this.recycledCompletionHandlers = new ConcurrentLinkedQueue<>();
 		}
 
 		/**
 		 * Destroy the event poller
 		 */
 		public void destroy() {
 			synchronized (this.mutex) {
 				this.channelList.clear();
 				this.recycledChannelList.clear();
 				this.recycledCompletionHandlers.clear();
 				this.mutex.notifyAll();
 			}
 		}
 
 		/**
 		 * 
 		 * @return
 		 */
 		protected ChannelInfo poll() {
 			ChannelInfo info = this.recycledChannelList.poll();
 			if (info == null) {
 				info = new ChannelInfo();
 			}
 			return info;
 		}
 
 		/**
 		 * Recycle the the {@link ChannelInfo}
 		 * 
 		 * @param info
 		 */
 		protected void offer(ChannelInfo info) {
 			if (info != null) {
 				info.recycle();
 				this.recycledChannelList.offer(info);
 			}
 		}
 
 		/**
 		 * Peek a {@link java.nio.CompletionHandler} from the list of recycled
 		 * handlers. If the list is empty, create a new one and return it.
 		 * 
 		 * @return a reference of {@link java.nio.CompletionHandler}
 		 */
 		private CompletionHandler<Integer, NioChannel> getCompletionHandler() {
 			CompletionHandler<Integer, NioChannel> handler = this.recycledCompletionHandlers.poll();
 			if (handler == null) {
 				handler = new CompletionHandler<Integer, NioChannel>() {
 
 					@Override
 					public void completed(Integer nBytes, NioChannel attach) {
 						if (nBytes < 0) {
 							failed(new ClosedChannelException(), attach);
 						} else {
 							remove(attach);
 							if (!processChannel(attach, SocketStatus.OPEN_READ)) {
 								closeChannel(attach);
 							}
 							// Recycle the completion handler
 							recycleHanlder(this);
 						}
 					}
 
 					@Override
 					public void failed(Throwable exc, NioChannel attach) {
 						remove(attach);
 						SocketStatus status = (exc instanceof ClosedChannelException) ? SocketStatus.DISCONNECT
 								: SocketStatus.ERROR;
 						if (!processChannel(attach, status)) {
 							closeChannel(attach);
 						}
 						// Recycle the completion handler
 						recycleHanlder(this);
 					}
 				};
 			}
 
 			return handler;
 		}
 
 		/**
 		 * Recycle the {@link java.nio.CompletionHandler}
 		 * 
 		 * @param handler
 		 */
 		private void recycleHanlder(CompletionHandler<Integer, NioChannel> handler) {
 			this.recycledCompletionHandlers.offer(handler);
 		}
 
 		/**
 		 * Add the channel to the list of channels
 		 * 
 		 * @param channel
 		 * @param timeout
 		 * @param flag
 		 * @return <tt>true</tt> if the channel is added successfully else
 		 *         <tt>false</tt>
 		 */
 		public boolean add(final NioChannel channel, long timeout, int flag) {
 			if (this.channelList.size() > this.size) {
 				return false;
 			}
 
 			long date = timeout + System.currentTimeMillis();
 			ChannelInfo info = this.channelList.get(channel.getId());
 
 			if (info == null) {
 				info = poll();
 				info.channel = channel;
 				info.flags = flag;
 				this.channelList.put(channel.getId(), info);
 			} else {
 				info.flags = ChannelInfo.merge(info.flags, flag);
 			}
 			// Setting the channel timeout
 			info.timeout = date;
 
 			final NioChannel ch = channel;
 
 			if (info.resume()) {
 				remove(info);
 				if (!processChannel(ch, SocketStatus.OPEN_CALLBACK)) {
 					closeChannel(ch);
 				}
 			} else if (info.wakeup()) {
 				remove(info);
 				// TODO
 			} else if (info.read()) {
 				if (ch.isReadReady()) {
 					ch.awaitRead(ch, getCompletionHandler());
 				}
 			} else if (info.write()) {
 				remove(info);
 				if (!processChannel(ch, SocketStatus.OPEN_WRITE)) {
 					closeChannel(ch);
 				}
 			} else {
 				remove(info);
 				processChannel(ch, SocketStatus.ERROR);
 			}
 
 			// Wake up all waiting threads
 			synchronized (this.mutex) {
 				this.mutex.notifyAll();
 			}
 			return true;
 		}
 	}
 
 	/**
 	 * {@code DefaultThreadFactory}
 	 * 
 	 * The default thread factory
 	 * 
 	 * Created on Mar 6, 2012 at 9:11:20 AM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	protected static class DefaultThreadFactory implements ThreadFactory {
 		private static final AtomicInteger poolNumber = new AtomicInteger(1);
 		private final ThreadGroup group;
 		private final AtomicInteger threadNumber = new AtomicInteger(1);
 		private final String namePrefix;
 		private final int threadPriority;
 
 		/**
 		 * Create a new instance of {@code DefaultThreadFactory}
 		 * 
 		 * @param namePrefix
 		 * @param threadPriority
 		 */
 		public DefaultThreadFactory(String namePrefix, int threadPriority) {
 			SecurityManager s = System.getSecurityManager();
 			group = (s != null) ? s.getThreadGroup() : Thread.currentThread().getThreadGroup();
 			this.namePrefix = namePrefix;
 			this.threadPriority = threadPriority;
 		}
 
 		/**
 		 * 
 		 * Create a new instance of {@code DefaultThreadFactory}
 		 * 
 		 * @param threadPriority
 		 */
 		public DefaultThreadFactory(int threadPriority) {
 			this("pool-" + poolNumber.getAndIncrement() + "-thread-", threadPriority);
 		}
 
 		/**
 		 * Create and return a new thread
 		 */
 		public Thread newThread(Runnable r) {
 			Thread thread = new Thread(group, r, namePrefix + threadNumber.getAndIncrement(), 0);
 			if (thread.isDaemon())
 				thread.setDaemon(false);
 
 			if (thread.getPriority() != this.threadPriority)
 				thread.setPriority(this.threadPriority);
 			return thread;
 		}
 	}
 
 	/**
 	 * SendfileData class.
 	 */
 	public static class SendfileData {
 		// File
 		protected String fileName;
 		// Range information
 		protected long start;
 		protected long end;
 		// The channel
 		protected NioChannel channel;
 		// The file channel
 		protected java.nio.channels.FileChannel fileChannel;
 		// Position
 		protected long pos;
 		// KeepAlive flag
 		protected boolean keepAlive;
 
 		/**
 		 * Prepare the {@code SendfileData}
 		 * 
 		 * @throws IOException
 		 * 
 		 * @throws Exception
 		 */
 		protected void setup() throws IOException {
 			this.pos = this.start;
 			java.nio.file.Path path = new File(this.fileName).toPath();
 			this.fileChannel = java.nio.channels.FileChannel.open(path, StandardOpenOption.READ)
 					.position(this.pos);
 		}
 
 		/**
 		 * Recycle this {@code SendfileData}
 		 */
 		protected void recycle() {
 			this.start = 0;
 			this.end = 0;
 			this.pos = 0;
 			this.channel = null;
 			this.keepAlive = false;
 			if (this.fileChannel != null && this.fileChannel.isOpen()) {
 				try {
 					this.fileChannel.close();
 				} catch (IOException e) {
 					// Ignore
 				}
 			}
 			this.fileChannel = null;
 		}
 
 		/**
 		 * Getter for fileName
 		 * 
 		 * @return the fileName
 		 */
 		public String getFileName() {
 			return this.fileName;
 		}
 
 		/**
 		 * Setter for the fileName
 		 * 
 		 * @param fileName
 		 *            the fileName to set
 		 */
 		public void setFileName(String fileName) {
 			this.fileName = fileName;
 		}
 
 		/**
 		 * Getter for start
 		 * 
 		 * @return the start
 		 */
 		public long getStart() {
 			return this.start;
 		}
 
 		/**
 		 * Setter for the start
 		 * 
 		 * @param start
 		 *            the start to set
 		 */
 		public void setStart(long start) {
 			this.start = start;
 		}
 
 		/**
 		 * Getter for end
 		 * 
 		 * @return the end
 		 */
 		public long getEnd() {
 			return this.end;
 		}
 
 		/**
 		 * Setter for the end
 		 * 
 		 * @param end
 		 *            the end to set
 		 */
 		public void setEnd(long end) {
 			this.end = end;
 		}
 
 		/**
 		 * Getter for channel
 		 * 
 		 * @return the channel
 		 */
 		public NioChannel getChannel() {
 			return this.channel;
 		}
 
 		/**
 		 * Setter for the channel
 		 * 
 		 * @param channel
 		 *            the channel to set
 		 */
 		public void setChannel(NioChannel channel) {
 			this.channel = channel;
 		}
 
 		/**
 		 * Getter for pos
 		 * 
 		 * @return the pos
 		 */
 		public long getPos() {
 			return this.pos;
 		}
 
 		/**
 		 * Setter for the pos
 		 * 
 		 * @param pos
 		 *            the pos to set
 		 */
 		public void setPos(long pos) {
 			this.pos = pos;
 		}
 
 		/**
 		 * Getter for keepAlive
 		 * 
 		 * @return the keepAlive
 		 */
 		public boolean isKeepAlive() {
 			return this.keepAlive;
 		}
 
 		/**
 		 * Setter for the keepAlive
 		 * 
 		 * @param keepAlive
 		 *            the keepAlive to set
 		 */
 		public void setKeepAlive(boolean keepAlive) {
 			this.keepAlive = keepAlive;
 		}
 	}
 
 	/**
 	 * {@code Sendfile}
 	 * 
 	 * Created on Mar 7, 2012 at 4:04:59 PM
 	 * 
 	 * @author <a href="mailto:nbenothm@redhat.com">Nabil Benothman</a>
 	 */
 	public class Sendfile implements Runnable {
 
 		protected int size;
 		protected ConcurrentLinkedQueue<SendfileData> fileDatas;
 		protected ConcurrentLinkedQueue<SendfileData> recycledFileDatas;
 		protected AtomicInteger counter;
 		private Object mutex;
 
 		/**
 		 * @return the number of send file
 		 */
 		public int getSendfileCount() {
 			return this.counter.get();
 		}
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.lang.Runnable#run()
 		 */
 		@Override
 		public void run() {
 
 			while (running) {
 				// Loop if endpoint is paused
 				while (paused) {
 					try {
 						Thread.sleep(1000);
 					} catch (InterruptedException e) {
 						// Ignore
 					}
 				}
 				// Loop while poller is empty
 				while (this.counter.get() < 1 && running && !paused) {
 					try {
 						synchronized (this.mutex) {
 							this.mutex.wait();
 						}
 					} catch (InterruptedException e) {
 						// Ignore
 					}
 				}
 
 				if (running && !paused) {
 					try {
 						SendfileData data = this.poll();
 						if (data != null) {
 							sendFile(data);
 						}
 					} catch (Throwable th) {
 						// Ignore
 					}
 				}
 			}
 		}
 
 		/**
 		 * Initialize the {@code Sendfile}
 		 */
 		protected void init() {
 			this.size = maxThreads;
 			this.mutex = new Object();
 			this.counter = new AtomicInteger(0);
 			this.fileDatas = new ConcurrentLinkedQueue<>();
 			this.recycledFileDatas = new ConcurrentLinkedQueue<>();
 		}
 
 		/**
 		 * Destroy the SendFile
 		 */
 		protected void destroy() {
 			synchronized (this.mutex) {
 				// To unlock the
 				this.counter.incrementAndGet();
 				this.fileDatas.clear();
 				this.recycledFileDatas.clear();
 				// Unlock threads waiting for this monitor
 				this.mutex.notifyAll();
 			}
 		}
 
 		/**
 		 * @param data
 		 */
 		public void recycleSendfileData(SendfileData data) {
 			data.recycle();
 			this.recycledFileDatas.offer(data);
 		}
 
 		/**
 		 * Poll the head of the recycled object list if it is not empty, else
 		 * create a new one.
 		 * 
 		 * @return a {@code SendfileData}
 		 */
 		public SendfileData getSendfileData() {
 			SendfileData data = this.recycledFileDatas.poll();
 			return data == null ? new SendfileData() : data;
 		}
 
 		/**
 		 * 
 		 * @param data
 		 * @throws Exception
 		 */
 		private void sendFile(final SendfileData data) throws Exception {
 			if (data.channel.isWritePending()) {
 				add(data);
 				return;
 			}
 			// Configure the send file data
 			data.setup();
 
 			final NioChannel channel = data.channel;
 			final int BUFFER_SIZE = channel.getOption(StandardSocketOptions.SO_SNDBUF);
 			final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
 			int nr = data.fileChannel.read(buffer);
 
 			if (nr >= 0) {
 				buffer.flip();
 				channel.write(buffer, data, new CompletionHandler<Integer, SendfileData>() {
 
 					@Override
 					public void completed(Integer nw, SendfileData attachment) {
 						if (nw < 0) { // Reach the end of stream
 							closeChannel(channel);
 							closeFile(attachment.fileChannel);
 							return;
 						}
 
 						attachment.pos += nw;
 
 						if (attachment.pos >= attachment.end) {
 							// All requested bytes were sent, recycle it
 							recycleSendfileData(attachment);
 							return;
 						}
 
 						boolean ok = true;
 
 						if (!buffer.hasRemaining()) {
 							// This means that all data in the buffer has been
 							// written => Empty the buffer and read again
 							buffer.clear();
 							try {
 								if (attachment.fileChannel.read(buffer) >= 0) {
 									buffer.flip();
 								} else {
 									// Reach the EOF
 									ok = false;
 								}
 							} catch (Throwable th) {
 								ok = false;
 							}
 						}
 
 						if (ok) {
 							channel.write(buffer, attachment, this);
 						} else {
 							closeFile(attachment.fileChannel);
 						}
 					}
 
 					@Override
 					public void failed(Throwable exc, SendfileData attachment) {
 						// Closing channels
 						closeChannel(channel);
 						closeFile(data.fileChannel);
 					}
 
 					/**
 					 * 
 					 * @param closeable
 					 */
 					private void closeFile(java.io.Closeable closeable) {
 						try {
 							closeable.close();
 						} catch (IOException e) {
 							// NOPE
 						}
 					}
 				});
 			} else {
 				recycleSendfileData(data);
 			}
 		}
 
 		/**
 		 * Add the sendfile data to the sendfile poller. Note that in most
 		 * cases, the initial non blocking calls to sendfile will return right
 		 * away, and will be handled asynchronously inside the kernel. As a
 		 * result, the poller will never be used.
 		 * 
 		 * @param data
 		 *            containing the reference to the data which should be sent
 		 * @return true if all the data has been sent right away, and false
 		 *         otherwise
 		 */
 		public boolean add(SendfileData data) {
 			if (data != null && this.counter.get() < this.size) {
 				synchronized (this.mutex) {
 					if (this.fileDatas.offer(data)) {
 						this.counter.incrementAndGet();
 						this.mutex.notifyAll();
 						return true;
 					}
 				}
 			}
 
 			return false;
 		}
 
 		/**
 		 * Retrieves and removes the head of this queue, or returns
 		 * <tt>null</tt> if this queue is empty.
 		 * 
 		 * @return the head of this queue, or <tt>null</tt> if this queue is
 		 *         empty
 		 */
 		protected SendfileData poll() {
 			SendfileData data = this.fileDatas.poll();
 			if (data != null) {
 				this.counter.decrementAndGet();
 			}
 			return data;
 		}
 
 		/**
 		 * Remove socket from the poller.
 		 * 
 		 * @param data
 		 *            the sendfile data which should be removed
 		 */
 		protected void remove(SendfileData data) {
 			if (this.fileDatas.remove(data)) {
 				this.counter.decrementAndGet();
 			}
 		}
 	}
 }
