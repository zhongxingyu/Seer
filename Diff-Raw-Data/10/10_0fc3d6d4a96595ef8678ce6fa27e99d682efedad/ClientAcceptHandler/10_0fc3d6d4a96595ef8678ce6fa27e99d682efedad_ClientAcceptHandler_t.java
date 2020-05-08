 package de.me.networking.server;
 
import java.io.IOException;
 import java.nio.channels.SocketChannel;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import de.me.networking.server.listener.ClientAcceptListener;
 
 
 /**
  * Internal class to handle an accepted client in an executor execution.
  */
 class ClientAcceptHandler implements Runnable {
 
 	private final Logger log = LoggerFactory.getLogger(getClass());
 
 	private final SocketChannel client;
 	private final int capacity;
 	private final ClientAcceptListener listener;
 
 
 	ClientAcceptHandler(SocketChannel client, ClientAcceptListener listener, int capacity) {
 		this.client = client;
 		this.listener = listener;
 		this.capacity = capacity;
 	}
 
 
 	@Override
 	public void run() {
 		final ClientHandler handler = new ClientHandler(client, capacity);
 
 		try {
 			// Let listener register on events
 			listener.onIncomingClient(handler);
 
 			// Watch client
 			handler.execute();
 		} catch (Throwable e) {
 			log.error("Client Handler error", e);
 
 			// Inform listeners for error
 			try {
 				handler.onError(e);
 			}
 			catch (Throwable ee) {
 				log.error("Error in client handler onError", ee);
 			}
			finally {
				try {
					client.close();
				}
				catch (IOException eee) {
					log.error("Cannot close client connection", eee);
				}
			}
 		}
 	}
 
 }
