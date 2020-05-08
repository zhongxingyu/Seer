 /**
 * NioServer.java
  *
  * Copyright 2012 Niolex, Inc.
  *
  * Niolex licenses this file to you under the Apache License, version 2.0
  * (the "License"); you may not use this file except in compliance with the
  * License.  You may obtain a copy of the License at:
  *
  *    http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
  * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
  * License for the specific language governing permissions and limitations
  * under the License.
  */
 package org.apache.niolex.network;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.nio.channels.CancelledKeyException;
 import java.nio.channels.ClosedChannelException;
 import java.nio.channels.SelectionKey;
 import java.nio.channels.Selector;
 import java.nio.channels.ServerSocketChannel;
 import java.nio.channels.SocketChannel;
 import java.util.Set;
 
 import org.apache.niolex.network.server.ClientHandler;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The base Nonblocking implementation of IServer.
  *
  * @author <a href="mailto:xiejiyun@gmail.com">Xie, Jiyun</a>
  * @version 1.0.0
  * @Date: 2012-6-11
  */
 public class NioServer implements IServer {
     private static final Logger LOG = LoggerFactory.getLogger(NioServer.class);
 
     /**
      * The server socket channel, which is there the server listening.
      */
     private ServerSocketChannel ss;
 
     /**
      * The server accept and read selector, which is the main selector.
      */
     private Selector mainSelector;
 
     /**
      * The listen thread.
      */
     private Thread mainThread;
 
     /**
      * The packet handler.
      */
     protected IPacketHandler packetHandler;
 
     /**
      * The current server status.
      */
     protected volatile boolean isListening = false;
 
     /**
      * The accept timeout, which is not important.
      */
     protected int acceptTimeOut = Config.SERVER_ACCEPT_TIMEOUT;
 
     /**
      * The current server port number.
      */
     protected int port = Config.SERVER_DEFAULT_PORT;
 
     /**
      * Start this Server and listen to the port specified.
      * It run the main selector internally to handle accept request.
      *
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#start()
 	 */
     @Override
 	public boolean start() {
         try {
             ss = ServerSocketChannel.open();
             ss.socket().bind(new InetSocketAddress(this.getPort()));
             ss.configureBlocking(false);
             mainSelector = Selector.open();
             ss.register(mainSelector, SelectionKey.OP_ACCEPT);
             isListening = true;
             run();
             LOG.info("Server started at {}", this.getPort());
             return true;
         } catch (Exception e) {
             LOG.error("Failed to start server.", e);
         }
         return false;
     }
 
     /**
      * This method will start a new thread.
      * Run selector internally.
      */
     private void run() {
         try {
         	mainThread = new Thread(new Runnable() {
         		public void run() {
                     // Listen the main loop.
         			try {
 						listenMain();
 					} catch (Exception e) {
 			            LOG.error("Error occured while server is listening. The server will now shutdown.", e);
 			            stop();
 					}
                 }
         	});
         	mainThread.start();
         } catch (Exception e) {
             LOG.error("Error occured while server is listening. The server will now shutdown.", e);
             stop();
         }
     }
 
     /**
      * This method will never return after this server stop or IOException.
      * Call stop() to shutdown this server.
      * @throws IOException
      */
     private void listenMain() throws IOException {
         while (isListening) {
             // Setting the timeout for accept method. Avoid that this server can not be shut
             // down when this thread is waiting to accept.
             mainSelector.select(acceptTimeOut);
             Set<SelectionKey> selectionKeys = mainSelector.selectedKeys();
             for (SelectionKey selectionKey: selectionKeys) {
                 handleKey(selectionKey);
             }
             selectionKeys.clear();
         }
     }
 
     /**
      * handleKey use This method to register the newly created SocketChannel to a worker selector.
      * This method will use the main selector to register READ operation.
      *
      * Any Sub class can override this method to change the default behavior.
      * Sub class can override this method to use there own selector.
      * The default client handler will just process in the main thread.
      * @param client
      * @return
      */
     protected void registerClient(SocketChannel client) throws IOException {
     	client.register(mainSelector, SelectionKey.OP_READ,
     			new ClientHandler(packetHandler, mainSelector, client));
     }
 
     /**
      * Process all the IO requests.
      * Handle accept, read, write. Please do not override this method.
      */
     protected void handleKey(SelectionKey selectionKey) throws IOException {
         SocketChannel client = null;
         try {
             if (selectionKey.isAcceptable()) {
                 ServerSocketChannel server = (ServerSocketChannel) selectionKey.channel();
                 client = server.accept();
                 client.configureBlocking(false);
                 // Register this client to a selector.
                 registerClient(client);
                 return;
             }
             if (selectionKey.isReadable()) {
                 ClientHandler clientHandler = (ClientHandler) selectionKey.attachment();
                 if (clientHandler != null) {
                 	handleRead(clientHandler);
                 }
             }
             if (selectionKey.isWritable()) {
             	ClientHandler clientHandler = (ClientHandler) selectionKey.attachment();
                 if (clientHandler != null) {
                 	handleWrite(clientHandler);
                 }
             }
         } catch (Exception e) {
         	if (e instanceof CancelledKeyException || e instanceof ClosedChannelException) {
         		return;
         	}
             LOG.info("Failed to handle client socket: {}", e.toString());
         }
     }
 
     /**
      * Any Sub class can override this method to change the behavior of ClientHandler.
      * The default client handler will just process in the main thread.
      * @param clientHandler
      */
     protected void handleRead(ClientHandler clientHandler) {
     	// Call clientHandler.handleRead one time will just generate one Packet.
     	// If there are more Packet, we need to call it multiple times.
     	while (clientHandler.handleRead()) {
     		continue;
     	}
     }
 
     /**
      * Any Sub class can override this method to change the behavior of ClientHandler.
      * The default client handler will just process in the main thread.
      * @param clientHandler
      */
     protected void handleWrite(ClientHandler clientHandler) {
     	// Call clientHandler.handleWrite will just send one buffer.
     	// If that buffer is sent immediately, we need to call it again to send more data.
     	while (clientHandler.handleWrite()) {
     		continue;
     	}
     }
 
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#stop()
 	 */
     @Override
 	public void stop() {
     	if (!isListening) {
     		return;
     	}
     	// Mark the server as stopped, so main thread will return.
         isListening = false;
         try {
         	mainThread.interrupt();
         	mainThread.join();
         } catch (Exception e) {
         	LOG.error("Failed to stop server main thread.", e);
         }
         try {
             ss.socket().close();
             ss.close();
             mainSelector.close();
         } catch (Exception e) {
             LOG.error("Failed to close server port.", e);
         }
         LOG.info("Server stoped.");
     }
 
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#getPort()
 	 */
     @Override
 	public int getPort() {
         return this.port;
     }
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#setPort(int)
 	 */
     @Override
 	public void setPort(int port) {
         this.port = port;
     }
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#getPacketHandler()
 	 */
     @Override
 	public IPacketHandler getPacketHandler() {
         return packetHandler;
     }
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#setPacketHandler(org.apache.niolex.network.IPacketHandler)
 	 */
     @Override
 	public void setPacketHandler(IPacketHandler packetHandler) {
         this.packetHandler = packetHandler;
     }
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#getAcceptTimeOut()
 	 */
     @Override
 	public int getAcceptTimeOut() {
         return acceptTimeOut;
     }
 
     /**
 	 * Override super method
 	 * @see org.apache.niolex.network.IServer#setAcceptTimeOut(int)
 	 */
     @Override
 	public void setAcceptTimeOut(int acceptTimeOut) {
         this.acceptTimeOut = acceptTimeOut;
     }
 
 }
