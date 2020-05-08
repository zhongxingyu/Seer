 /*
  * Document confidentiel - Diffusion interdite 
  */
 package fr.pokerfan.pta.server;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.net.Socket;
 
 import org.apache.commons.logging.impl.Log4JLogger;
 
 /**
  * 1 Thread = 1 connection
  * 
  * @author pierre.kerichard
  * 
  */
 public class PTAThread implements Runnable {
 
 	private static final Log4JLogger LOGGER = new Log4JLogger(PTAThread.class
			.getCanonicalName());
 
 	private Socket socket;
 
 	public PTAThread(final Socket socket) {
 		this.socket = socket;
 	}
 
 	/**
 	 * 
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void run() {
 		PTAThread.LOGGER.debug("Dmarrage du thread");
 		try {
 			final BufferedReader reader = new BufferedReader(
 					new InputStreamReader(socket.getInputStream()));
 
 			while (true) {
 				System.out.println(reader.readLine());
 			}
 		} catch (final IOException e) {
 			PTAThread.LOGGER.warn("Dconnection du socket");
 			try {
 				socket.close();
 			} catch (final IOException e1) {
 				PTAThread.LOGGER.error(
 						"Erreur lors de la fermeture du socket ", e1);
 			}
 		}
 
 	}
 
 	/**
 	 * Retourne socket
 	 * 
 	 * @return socket
 	 */
 	public Socket getSocket() {
 		return socket;
 	}
 
 	/**
 	 * Modifie socket
 	 * 
 	 * @param socket
 	 *            socket  modifier
 	 */
 	public void setSocket(final Socket socket) {
 		this.socket = socket;
 	}
 
 }
