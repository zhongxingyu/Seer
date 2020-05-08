 package devhood.im.sim.service;
 
 import java.io.IOException;
 import java.io.ObjectOutputStream;
 import java.net.ServerSocket;
 import java.net.Socket;
 import java.net.UnknownHostException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import devhood.im.sim.Sim;
 import devhood.im.sim.event.EventDispatcher;
 import devhood.im.sim.event.EventObserver;
 import devhood.im.sim.event.Events;
 import devhood.im.sim.model.Message;
 import devhood.im.sim.model.User;
 import devhood.im.sim.service.interfaces.MessageService;
 
 /**
  * Peer to Peer Implementierung des {@link MessageService}.
  * 
  * @author flo
  * 
  */
 public class PeerToPeerMessageService implements EventObserver, Runnable {
 
 	private Logger log = Logger.getLogger(PeerToPeerMessageService.class
 			.toString());
 
 	/**
 	 * Socket fr den Server
 	 */
 	private ServerSocket serverSocket;
 
 	/**
 	 * Server Thread
 	 */
 	private Thread thread;
 
 	/**
 	 * Startet Message Server in neuem Thread.
 	 * 
 	 * @throws IOException
 	 *             Exception wenn ServerSocket nicht erzeugt werden kann
 	 */
 	public PeerToPeerMessageService() throws IOException {
		serverSocket = new ServerSocket();
 		Sim.setPort(serverSocket.getLocalPort());
 		thread = new Thread(this);
 		thread.start();
 	}
 
 	/**
 	 * {@inheritDoc}
 	 */
 	@Override
 	public void eventReceived(Events event, Object o) {
 		if (Events.MESSAGE_SENT.equals(event)) {
 			Message m = (Message) o;
 			String receiver = m.getReceiver();
 			User user = ServiceLocator.getInstance().getRegistryService().getUser(receiver);
 			
 			Socket socket = null;
 			try {
 				socket = new Socket(user.getAddress(), user.getPort());
 				ObjectOutputStream os = new ObjectOutputStream(socket.getOutputStream());
 				os.writeObject(m);
 				socket.close();
 			} catch (UnknownHostException e) {
				log.log(Level.SEVERE, "client socket Verbindung Fehler: "+e.getMessage());
 			} catch (IOException e) {
				log.log(Level.SEVERE, "IOException: "+e.getMessage());
 			}
 			
 		} else if (Events.LOGOUT.equals(event)) {
 			thread.interrupt();
 		}
 	}
 
 	/**
 	 * Startet Server und empfngt neue Nachrichten
 	 */
 	@Override
 	public void run() {
 		while (!Thread.interrupted()) {
 			try {
 				Socket clientSocket = serverSocket.accept();
 				Thread worker = new Thread(
 						new PeerToPeerMessageServiceReceiver(clientSocket));
 				worker.start();
 			} catch (IOException e) {
				log.log(Level.SEVERE, "client socket connection error");
 			}
 		}
 	}
 
 
 }
