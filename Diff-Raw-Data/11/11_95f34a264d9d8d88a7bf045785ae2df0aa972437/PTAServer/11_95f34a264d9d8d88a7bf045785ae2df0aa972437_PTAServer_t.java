 /*
  * Document confidentiel - Diffusion interdite 
  */
 package fr.pokerfan.pta.server;
 
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 
 import org.apache.commons.logging.impl.Log4JLogger;
 
 /**
  * 
  * Point d'entre de l'application. Dmarrage du server
  * 
  * @author pierre.kerichard
  * 
  */
 public class PTAServer {
 
 	private static final Log4JLogger LOGGER = new Log4JLogger(PTAServer.class
			.getCanonicalName());
 	private static final int DEFAULT_PORT = 54504;
 
 	/**
 	 * Dmrrage du serveur et initialisation du contexte Spring
 	 * 
 	 * @param args
 	 *            args[0] : port
 	 * 
 	 * @author pierre.kerichard
 	 */
 	public static void main(final String[] args) {
 		final ServerSocket serverSocket = PTAServer.initSocket(args);
 		if (serverSocket == null) {
 			PTAServer.LOGGER.fatal("serverSocket non cr. Exit");
 			return;
 		}
 
 		while (true) {
 			try {
 				// on attend les connections et on les spares dans des threads
 				// diffrents
 				final Socket socketConnection = serverSocket.accept();
 				final PTAThread ptaThread = new PTAThread(socketConnection);
 				final Thread thread = new Thread(ptaThread);
 				thread.start();
 			} catch (final IOException e) {
 				PTAServer.LOGGER.warn("dconnection du client");
 			}
 		}
 
 	}
 
 	/**
 	 * Initialisation du web socket avec les arguments donns lors du lancement
 	 * de l'appli.<br>
 	 * Si pas d'arguments, utilisation des valeurs par defaut.
 	 * 
 	 * @param args
 	 *            arguments
 	 * 
 	 * @return {@link ServerSocket} initialis <br>
 	 *         <code>null</code> si une erreur survient lors de la cration
 	 * 
 	 * @author pierre.kerichard
 	 */
 	private static ServerSocket initSocket(final String[] args) {
 		PTAServer.LOGGER.debug("Initialisation du ServerSocket");
 		int port = PTAServer.DEFAULT_PORT;
 		try {
 			if (args != null && args.length > 0) {
 				port = Integer.valueOf(args[0]);
 				PTAServer.LOGGER.debug("Utilisation du port : " + port);
 			} else {
 				PTAServer.LOGGER.debug("Utilisation du port par defaut : "
 						+ port);
 			}
 
 			return new ServerSocket(port);
 		} catch (final IOException e) {
 			PTAServer.LOGGER.error(
 					"Erreur lors de la cration du socket sur le port "
 							+ String.valueOf(port), e);
 			return null;
 		}
 	}
 
 }
