 package chronos;
 
 import client.ClientController;
 import events.AuthEvent;
 import events.NetworkEvent.EventType;
 import server.ServerController;
 
 /**
  * Main class. Starts either a server or a client, depending on args input. ALL
  * HAIL THE CHRONOS.
  * 
  * @author anon
  * 
  */
 public class Chronos {
 	public static void main(String[] args) {
 		new Chronos().run(args);
 
 	}
 
 	private void run(String[] args) {
 		if (args.length == 1)
 			if (args[0].equals("-s"))
 				startServer();
			else {
 
				Singleton.getInstance().enableLog();
				Singleton.getInstance().enableLogin();
				Singleton.getInstance().enableNetwork();
 
				if (Singleton.getInstance().networkEnabled())
					// startServer();
					startClient();
				// stressTestNetwork();
			}
 
 	}
 
 	private void startClient() {
 		ClientController clientController = new ClientController();
 		Thread clientControllerThread = new Thread(clientController);
 		clientControllerThread.run();
 	}
 
 	private void startServer() {
 		ServerController serverController = new ServerController();
 		serverController.run();
 	}
 
 	/**
 	 * used primarily for debugging
 	 */
 	private void stressTestNetwork() {
 		startClient(new AuthEvent(EventType.LOGIN, new Person("kyra"), "derp"));
 		startClient(new AuthEvent(EventType.LOGIN, new Person("pat"), "derp"));
 		startClient(new AuthEvent(EventType.LOGIN, new Person("boytoy"), "derp"));
 		startClient(new AuthEvent(EventType.LOGIN, new Person("root"), ""));
 
 	}
 
 	private void startClient(AuthEvent event) {
 
 		ClientController clientController = new ClientController(event);
 		Thread clientControllerThread = new Thread(clientController);
 		clientControllerThread.start();
 	}
 }
