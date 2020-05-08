 /*
  * Copyright (c) 2012, someone All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 
  * 1.Redistributions of source code must retain the above copyright notice, this
  * list of conditions and the following disclaimer. 2.Redistributions in binary
  * form must reproduce the above copyright notice, this list of conditions and
  * the following disclaimer in the documentation and/or other materials provided
  * with the distribution. 3.Neither the name of the Happyelements Ltd. nor the
  * names of its contributors may be used to endorse or promote products derived
  * from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
  * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
  * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
  * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
  * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
  * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
  * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
  * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
  * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
  * POSSIBILITY OF SUCH DAMAGE.
  */
 package com.happyelements.zero.nio;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Iterator;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.happyelements.zero.async.Asyncs;
 
 /**
  * a utility to create NIO sockets
  * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
  */
 public class NIO {
 
 	private static final Log LOGGER = LogFactory.getLog(NIO.class);
 
 	/**
 	 * listener trigger by NIO runtime,include read event and connected event
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	public static interface ConnectedListener {
 		/**
 		 * notify when exception occurs
 		 * @param channel
 		 * 		the exception channel
 		 * @throws Exception
 		 * 		any exception,expected of unexpected
 		 */
 		public void notifyException(SocketChannel channel, Exception exception);
 
 		/**
 		 * notify when the channel is readable
 		 * @param channel
 		 * 		the readable channel
 		 * @throws Exception
 		 * 		any exception,expected of unexpected
 		 */
 		public void notifyReadable(SocketChannel channel) throws Exception;
 
 		/**
 		 * notify when the channel is connected
 		 * @param channel
 		 * 		the connected channel
 		 * @throws Exception
 		 * 		any exception,expected of unexpected
 		 */
 		public void notifyConnected(SocketChannel channel) throws Exception;
 	}
 
 	/**
 	 * internal listener. to simplify state transfer
 	 * @author <a href="mailto:zhizhong.qiu@happyelements.com">kevin</a>
 	 */
 	protected static abstract class InternelListener {
 
 		/**
 		 * notify some specific listeners
 		 * @param key
 		 * 		the ready select key
 		 */
 		public abstract void notifyListener(SelectionKey key) throws Exception;
 
 		/**
 		 * chain the exception notification
 		 * @param listener
 		 * 		the listener to handle the exception
 		 * @param key
 		 * 		the selection key
 		 * @param exception
 		 * 		the exception
 		 */
 		public void failListenerNotify(ConnectedListener listener,
 				SelectionKey key, Exception exception) {
 			NIO.LOGGER.error("fail to notify listern:" + listener
 					+ " clean up resources including channel", exception);
 
 			// null check
 			if (key == null) {
 				return;
 			}
 
 			// close channel
 			try {
 				key.channel().close();
 			} catch (IOException e) {
 				NIO.LOGGER.error("fail to close channel of key:" + key, e);
 			}
 
 			// clean
 			key.cancel();
 			key.attach(null);
 		}
 	}
 
 	private final Selector selector;
 	private final Asyncs asyncs;
 
 	/**
 	 * create a selector
 	 * @param asyncs
 	 * 		the asyncs work agents
 	 * @throws IOException
 	 */
	public NIO() throws IOException {
 		this.selector = Selector.open();
		this.asyncs = new Asyncs();
 	}
 
 	/**
 	 * start the NIO,doing select actually
 	 * @return
 	 * 		this NIO
 	 */
 	public NIO start() {
 		this.asyncs.offer(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					for (;;) {
 						// do blocking select
 						if (NIO.this.selector.select() <= 0) {
 							// may be unblock by wake up,so just ignore
 							continue;
 						}
 
 						// loop each ready keys
 						Iterator<SelectionKey> iterator = NIO.this.selector
 								.selectedKeys().iterator();
 						while (iterator.hasNext()) {
 							// retrieve the key
 							SelectionKey key = iterator.next();
 							iterator.remove();
 
 							// notify listener
 							InternelListener listener = ((InternelListener) key
 									.attachment());
 							if (listener != null) {
 								try {
 									// notify the real listener
 									listener.notifyListener(key);
 								} catch (Exception e) {
 									// exception,notify it
 									listener.failListenerNotify(null, key, e);
 								}
 							}
 						}
 					}
 				} catch (Exception e) {
 					NIO.LOGGER.error(
 							"unexpected exception in nio select,restart it", e);
 				} finally {
 					// this should never end,so queue it again
 					NIO.this.asyncs.offer(this);
 				}
 			}
 		});
 
 		return this;
 	}
 
 	/**
 	 * connect to address,with the listener attach to the channel
 	 * @param address
 	 * 		the address to connect to
 	 * @param listener
 	 * 		the listener
 	 * @param timeout
 	 * 		the time out value for the connection
 	 * @return
 	 * 		the this NIO
 	 * @throws IOException
 	 * 		throw when channel initialize fail
 	 */
 	public NIO connect(InetSocketAddress address, ConnectedListener listener,
 			long timeout) throws IOException {
 		SocketChannel channel = SocketChannel.open();
 
 		// set up tunable parameters
 		channel = this.tuneSocketChannel(channel);
 
 		// connect
 		channel.connect(address);
 
 		// should lock the selector in this way,as register require the selector
 		// synchronized point
 		synchronized (this.selector.wakeup()) {
 			// register the connect key
 			channel.register(this.selector, SelectionKey.OP_CONNECT,
 					this.makeConnectedInternelListener(listener));
 		}
 
 		return this;
 	}
 
 	/**
 	 * listen on the address,wait for incoming connection
 	 * @param address
 	 * 		the remote address
 	 * @param listener
 	 * 		the listener that will call by the NIO runtime when connected or readable
 	 * @return
 	 * @throws IOException
 	 */
 	public NIO listen(InetSocketAddress address, ConnectedListener listener)
 			throws IOException {
 		ServerSocketChannel channel = ServerSocketChannel.open();
 
 		// set up tunable parameters
 		channel = this.tuneServerSocketChannel(channel);
 
 		// bind address
 		channel.socket().bind(address);
 
 		// should lock the selector in this way,as register require the selector
 		// synchronized point
 		synchronized (this.selector.wakeup()) {
 			// register the accept key
 			channel.register(this.selector, SelectionKey.OP_ACCEPT,
 					this.makeAcceptInternelListener(listener));
 		}
 		return this;
 	}
 
 	/**
 	 * tunable parameter for socket channel
 	 * @param channel
 	 * 		the socket channel
 	 * @return
 	 * 		the tuned socket channel 
 	 * @throws IOException
 	 * 		when tune fail
 	 */
 	protected ServerSocketChannel tuneServerSocketChannel(
 			ServerSocketChannel channel) throws IOException {
 		channel.socket().setReuseAddress(true);
 		channel.configureBlocking(false);
 		return channel;
 	}
 
 	/**
 	 * tunable parameter for socket channel
 	 * @param channel
 	 * 		the socket channel
 	 * @return
 	 * 		the tuned socket channel 
 	 * @throws IOException
 	 * 		when tune fail
 	 */
 	protected SocketChannel tuneSocketChannel(SocketChannel channel)
 			throws IOException {
 		channel.socket().setReuseAddress(true);
 		channel.configureBlocking(false);
 		return channel;
 	}
 
 	/**
 	 * internal readable listener trigger by NIO runtime when a channel is readable 
 	 * @param listener
 	 * 		the real listener
 	 * @return
 	 * 		the internal listener 
 	 */
 	protected InternelListener makeReadableInternelListener(
 			final ConnectedListener listener) {
 		return new InternelListener() {
 			@Override
 			public void notifyListener(final SelectionKey key) throws Exception {
 				// temporary ignore the selection
 				key.interestOps(key.interestOps() ^ SelectionKey.OP_READ);
 
 				// async forward
 				NIO.this.asyncs.offer(new Runnable() {
 					@Override
 					public void run() {
 						// forward listener
 						if (listener != null) {
 							try {
 								// forward the real read
 								listener.notifyReadable((SocketChannel) key
 										.channel());
 							} catch (Exception e) {
 								// notify when exception
 								listener.notifyException(
 										(SocketChannel) key.channel(), e);
 							}
 						}
 
 						// check key
 						if (key.isValid()) {
 							// still valid , register the read key
 							key.interestOps(key.interestOps()
 									| SelectionKey.OP_READ);
 						} else {
 							// clean reference
 							key.attach(null);
 						}
 					}
 				});
 			}
 		};
 	}
 
 	/**
 	 * internal readable listener trigger by NIO runtime when a channel become conneted 
 	 * @param listener
 	 * 		the real listener
 	 * @return
 	 * 		the internal listener 
 	 */
 	protected InternelListener makeConnectedInternelListener(
 			final ConnectedListener listener) {
 		return new InternelListener() {
 			@Override
 			public void notifyListener(final SelectionKey key) {
 				// temporary ignore the selection
 				key.interestOps(key.interestOps() ^ SelectionKey.OP_CONNECT
 						| SelectionKey.OP_READ);
 
 				// async forward
 				final InternelListener self = this;
 				NIO.this.asyncs.offer(new Runnable() {
 					@Override
 					public void run() {
 						try {
 							// finished connect
 							((SocketChannel) key.channel()).finishConnect();
 						} catch (Exception e) {
 							// finished connect fail,cancel it
 							listener.notifyException(
 									(SocketChannel) key.channel(), e);
 
 							// should notify self
 							self.failListenerNotify(listener, key, e);
 							return;
 						}
 
 						// switch to readable listener
 						key.attach(NIO.this
 								.makeReadableInternelListener(listener));
 
 						// notify
 						try {
 							listener.notifyConnected((SocketChannel) key
 									.channel());
 						} catch (Exception e) {
 							self.failListenerNotify(listener, key, e);
 						}
 					}
 				});
 			}
 		};
 	}
 
 	/**
 	 * internal readable listener trigger by NIO runtime when a channel accepted
 	 * @param listener
 	 * 		the real listener
 	 * @return
 	 * 		the internal listener 
 	 */
 	protected InternelListener makeAcceptInternelListener(
 			final ConnectedListener listener) {
 		return new InternelListener() {
 			@Override
 			public void notifyListener(final SelectionKey key) throws Exception {
 				// temporary ignore the selection
 				key.interestOps(key.interestOps() ^ SelectionKey.OP_ACCEPT);
 
 				// accept
 				// get the server socket
 				ServerSocketChannel server = (ServerSocketChannel) key
 						.channel();
 
 				// find connected channels
 				try {
 					SocketChannel channel;
 					synchronized (NIO.this.selector.wakeup()) {
 						while ((channel = server.accept()) != null) {
 							try {
 								// make it final for anonymous function
 								final SocketChannel final_channel = NIO.this
 										.tuneSocketChannel(channel);
 
 								// register it
 								final_channel
 										.register(
 												NIO.this.selector,
 												SelectionKey.OP_READ,
 												NIO.this.makeReadableInternelListener(listener));
 
 								// notify connected
 								NIO.this.asyncs.offer(new Runnable() {
 									@Override
 									public void run() {
 										// notify connection from remote
 										try {
 											listener.notifyConnected(final_channel);
 										} catch (Exception e) {
 											listener.notifyException(
 													final_channel, e);
 										}
 									}
 								});
 							} catch (Exception e) {
 								listener.notifyException(channel, e);
 							}
 						}
 					}
 				} catch (IOException e) {
 					this.failListenerNotify(listener, key, e);
 					return;
 				}
 
 				// re attach
 				key.interestOps(key.interestOps() | SelectionKey.OP_ACCEPT);
 			}
 		};
 	}
 }
