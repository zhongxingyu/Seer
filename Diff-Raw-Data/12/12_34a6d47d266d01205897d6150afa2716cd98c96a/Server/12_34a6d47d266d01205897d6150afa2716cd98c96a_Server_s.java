 /**
  * Copyright 2012 NetDigital Sweden AB
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
 */
 
 package com.nginious.http.server;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import java.util.Set;
 import java.util.concurrent.ConcurrentLinkedQueue;
 import java.util.concurrent.LinkedBlockingQueue;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 /**
  * A generic event based I/O server that accepts incoming connection requests. A new {@link Connection}
  * instance is assigned to incoming connections for handling reading and writing of data. A connection typically
  * implements a specific network protocol, for example HTTP.
  * 
  * <p>
  * Subclasses must implement the {@link #createConnection(SocketChannel, SelectionKey)} method to create
  * a connection capable of handling protocol specific functionality.
  * </p>
  * 
  * <p>
  * A single thread handles operations for all connections which are informed once data is available for reading or
  * channels are ready for writing. Calls to connections for reading and writing are handed over to a thread pool
  * The thread pool contains as many threads as there are available processor cores.
  * 
  * Connections can interact with this server by queuing operations for execution. By queuing a write
  * operation and last a close operation a connection can instruct the server to write all pending
  * data and then close the underlying socket channel for the connection.
  * 
  * <ul>
  * <li>Queue read - instruct the server to call the {@link Connection#read(SocketChannel)} method when data
  * is available for reading.</li>
  * <li>Queue write - instruct the server to call the connections {@link Connection#write()} method when
  * the connections socket channel is ready to accept data for writing.</li>
  * <li>Queue close - instruct the server to close the connections underlying socket channel.</li>
  * </ul>
  * </p>
  *  
  * @author Bojan Pisler, NetDigital Sweden AB
  *
  */
 public abstract class Server implements Runnable {
 	
 	protected String name;
 	
 	protected MessageLog log;
 	
 	private int port;
 	
 	private ServerSocketChannel channel;
 	
 	private Thread thread;
 	
 	private boolean started;
 	
 	private ThreadPoolExecutor executor;
 	
 	private ConcurrentLinkedQueue<OperationEvent> events;
 	
 	private Selector selector;
 	
 	/**
 	 * Constructs a new server with the specified name.
 	 * 
 	 * @param name server name
 	 */
 	public Server(String name) {
 		super();
 		this.name = name;
 		this.started = false;
 		this.log = MessageLog.getInstance();
 	}
 	
 	/**
 	 * Sets listen port to the specified port for this server.
 	 * 
 	 * @param port the listen port
 	 */
 	protected void setPort(int port) {
 		this.port = port;
 	}
 	
 	/**
 	 * Starts this server. Incoming connection requests are accepted once the server is started.
 	 * 
 	 * @return <code>true</code> if the server was started, <code>false</code> if the server is already started
 	 * @throws IOException if unable to start server
 	 */
 	public boolean start() throws IOException {
 		boolean done = false;
 		
 		try {
			log.open();
 			log.info(this.name, "starting...");
 			
 			if(this.started) {
 				return false;
 			}
 			
 			int numThreads = Runtime.getRuntime().availableProcessors();
 			this.executor = new ThreadPoolExecutor(numThreads, numThreads, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
 			this.events = new ConcurrentLinkedQueue<OperationEvent>();
 			
 			this.channel = ServerSocketChannel.open();
 			InetSocketAddress address = new InetSocketAddress(port);
 			channel.socket().bind(address);
 			channel.configureBlocking(false);
 			log.info("Http", "listening " + address.getAddress().getHostAddress() + ":" + address.getPort());
 			
 			this.started = true;
 			this.thread = new Thread(this);
 			thread.start();
 			
 			log.info(this.name, "started, ready for connections");
 			done = true;
 			return true;
 		} catch(IOException e) {
 			log.error("Http", e);
 			throw e;
 		} finally {
 			if(!done) {
 				log.close();
 			}
 		}
 	}
 	
 	/**
 	 * Stops this server. Incoming connection requests are not accepted after stop. Processing of queued operations
 	 * is also stopped.
 	 * 
 	 * @return <code>true</code> if the server was stopped, <code>false</code> if the server is already stopped
 	 * @throws IOException if unable to stop server
 	 */
 	public boolean stop() throws IOException {
 		log.info(this.name, "stopping...");
 		
 		if(!this.started) {
 			return false;
 		}
 		
 		this.started = false;
 		executor.shutdownNow();
 		this.executor = null;
 		
 		channel.close();
 		
 		log.info(this.name, "stopped");
		log.close();
 		return true;
 	}
 	
 	/**
 	 * Queues a read request for the specified connection. Any read data from the socket channel is
 	 * forwarded to the connection.
 	 * 
 	 * @param conn the connection to read data for
 	 * @return <code>true</code> if the read was queued, <code>false</code> otherwise
 	 */
 	public boolean queueRead(Connection conn) {
 		return queueEvent(new OperationEvent(conn, OperationEvent.Type.OPERATION_READ));
 	}
 	
 	/**
 	 * Queues a write request for the specified connection. Once the request is executed the
 	 * connection is called to provide the data to write.
 	 * 
 	 * @param conn the connection to write data for
 	 * @return <code>true</code> if the write was queued, <code>false</code> otherwise
 	 */
 	public boolean queueWrite(Connection conn) {
 		return queueEvent(new OperationEvent(conn, OperationEvent.Type.OPERATION_WRITE));
 	}
 	
 	/**
 	 * Queues a close request for the specified connection. The socket channel is closed when
 	 * the request is executed.
 	 *  
 	 * @param conn the connection to close socket channel for
 	 * @return <code>true</code> if the close was queued, <code>false</code> otherwise
 	 */
 	public boolean queueClose(Connection conn) {
 		return queueEvent(new OperationEvent(conn, OperationEvent.Type.OPERATION_CLOSE));
 	}
 	
 	/**
 	 * Queues the specified for execution. Wakes up the internal selector thread to process
 	 * queued events.
 	 * 
 	 * @param event the event to queue
 	 * @return <code>true</code> if the event was queued, <code>false</code> otherwise.
 	 */
 	private boolean queueEvent(OperationEvent event) {
 		if(this.selector != null) {
 			events.add(event);
 			selector.wakeup();
 			return true;
 		}
 		
 		return false;
 	}
 	
 	/**
 	 *  Executed continuously by a single thread until the server is stopped. Uses one selector
 	 *  to accept connection requests and data. Also handles queued operations and executes
 	 *  them.
 	 */
 	public void run() {
 		long connectionTimeoutMillis = getConnectionTimeoutMillis();
 		boolean timeoutHandling = connectionTimeoutMillis > 0L;
 		long lastTimeoutMillis = System.currentTimeMillis();
 		
 		try {
 			selector = Selector.open();
 			channel.register(selector, SelectionKey.OP_ACCEPT);
 			
 			while(this.started) {
 				while(!events.isEmpty()) {
 					OperationEvent event = events.poll();
 					Connection conn = event.getConnection();
 					SelectionKey key = conn.getSelectionKey();
 					OperationEvent.Type operation = event.getOperation();
 					
 					switch(operation) {
 					case OPERATION_READ:
 						if(key.isValid()) {
 							key.interestOps(SelectionKey.OP_READ);
 						}
 						break;
 					
 					case OPERATION_WRITE:
 						if(key.isValid()) {
 							key.interestOps(SelectionKey.OP_WRITE);
 						}
 						break;
 					
 					case OPERATION_CLOSE:
 						if(key.isValid()) {
 							key.interestOps(0);
 							key.cancel();
 						}
 						
 						SocketChannel channel = conn.getChannel();
 						channel.close();
 						break;
 					}
 				}
 				
 				int numKeys = 0;
 				
 				if(!timeoutHandling) {
 					numKeys = selector.select();
 				} else {
 					long timeoutMillis = connectionTimeoutMillis - (System.currentTimeMillis() - lastTimeoutMillis);
 					numKeys = selector.select(timeoutMillis);
 				}
 				
 				if(!this.started) {
 					return;
 				}
 				
 				if(numKeys > 0) {
 					Set<SelectionKey> keys = selector.selectedKeys();
 					Iterator<SelectionKey> it = keys.iterator();
 					
 					while(it.hasNext()) {	
 						SelectionKey key = it.next();
 						it.remove();
 						
 						if(!key.isValid()) {
 							continue;
 						}
 						
 						if(key.isAcceptable()) {
 							SocketChannel socket = channel.accept();
 							socket.configureBlocking(false);
 							socket.socket().setTcpNoDelay(true);
 							
 							SelectionKey connKey = socket.register(key.selector(), SelectionKey.OP_READ, this);
 							connKey.selector().wakeup();
 							
 							Connection conn = createConnection(socket, connKey);
 							connKey.attach(conn);
 						} else if(key.isReadable()) {
 							key.interestOps(0);
 							Connection connector = (Connection)key.attachment();
 							
 							if(this.started) {
 								executor.execute(new Processor(connector));
 							}
 						} else if(key.isWritable()) {
 							write(key);
 						}
 					}
 				}
 				
 				if(timeoutHandling && System.currentTimeMillis() - lastTimeoutMillis >= connectionTimeoutMillis) {
 					lastTimeoutMillis = System.currentTimeMillis();
 					timeoutHandling = lastTimeoutMillis > 0L;
 					Set<SelectionKey> keys = selector.keys();
 					
 					for(SelectionKey key : keys) {
 						Connection conn = (Connection)key.attachment();
 						
 						if(conn != null && conn.isTimedOut()) {
 							if(key.isValid()) {
 								key.interestOps(0);
 								key.cancel();
 							}
 							
 							SocketChannel channel = conn.getChannel();
 							channel.close();							
 						}
 					}
 				}
 			}
 		} catch(IOException e) {
 			log.error("Http", e);
 		} finally {
 			if(selector != null) {
 				try { 
 					selector.close();
 					this.selector = null;
 				} catch(IOException e) {}
 			}
 		}
 	}
 	
 	/**
 	 * Creates connection for the specified socket channel and key. Subclasses must implement this method to
 	 * create connections capable of handling data for the intended protocol.
 	 * 
 	 * @param socket the socket channel for the accepted connection
 	 * @param connKey the selection key
 	 * @return the created connection
 	 * @throws IOException if unable to create connection
 	 */
 	protected abstract Connection createConnection(SocketChannel socket, SelectionKey connKey) throws IOException;
 	
 	/**
 	 * Returns the number of milliseconds a connection is allowed to be idle before closed. Subclasses must
 	 * implement this method to provide protocol specific timeouts.
 	 * 
 	 * @return the maximum number of milliseconds a connection is allowed to be idle
 	 */
 	public abstract long getConnectionTimeoutMillis();
 	
 	/**
 	 * Calls connection for the specified selection key to write data on the corresponding socket channel.
 	 * 
 	 * @param key the selection key
 	 * @throws IOException if unable to write data
 	 */
 	private void write(SelectionKey key) throws IOException {
 		Connection connector = (Connection)key.attachment();
 		connector.write();
 	}
 	
 	/**
 	 * Handles read data from socket channels. Calls connections {@link Connection#read()} method
 	 * to process data. Instances of this class are maintained in a thread pool to handle several
 	 * reads simultaneously.
 	 * 
 	 * @author Bojan Pisler, NetDigital Sweden aB
 	 *
 	 */
 	private class Processor implements Runnable {
 		
 		private Connection connector;
 		
 		/**
 		 * Constructs a new processor for the specified connection.
 		 * 
 		 * @param connector the connection
 		 */
 		Processor(Connection connector) {
 			this.connector = connector;
 		}
 		
 		/**
 		 * Calls connection to handle read data.
 		 */
 		public void run() {
 			try {
 				connector.read();
 			} catch(IOException e) {
 				// TODO, Key should be canceled here and connection closed.
 			}
 		}
 	}
 }
