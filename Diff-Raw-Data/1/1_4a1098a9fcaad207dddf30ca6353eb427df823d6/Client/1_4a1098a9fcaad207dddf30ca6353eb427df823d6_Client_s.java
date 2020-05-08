 package pl.edu.agh.two.mud.client;
 
 import java.io.IOException;
 
 import org.apache.log4j.Logger;
 
 import pl.edu.agh.two.mud.client.configuration.ApplicationContext;
import pl.edu.agh.two.mud.client.connection.impl.Connection;
 import pl.edu.agh.two.mud.client.ui.MainWindow;
 
 public class Client {
 
 	private static final String DEFAULT_HOST = "149.156.205.250";
 	private static final int DEFULT_PORT = 13933;
 
 	private Connection connection;
 
 	private MainWindow mainWindow;
 
 	private Logger logger = Logger.getLogger(Client.class);
 
 	public Client() {
 		mainWindow = new MainWindow();
 	}
 
 	public Client(MainWindow mainWindow) {
 		this.mainWindow = mainWindow;
 	}
 
 	public void start(String host, int port) {
 		try {
 			mainWindow.open();
 			// connection.connect(host, port);
 			// gui.setLabel(connection.read().toString());
 		} catch (Exception e) {
 			logger.error("Connection with \"" + host + ":" + port
 					+ "\" Error: " + e.getMessage());
 			// gui.setLabel("Connection with \"" + host + ":" + port +
 			// "\" Error: " + e.getMessage());
 		}
 
 	}
 
 	public void setConnection(Connection connection) {
 		this.connection = connection;
 	}
 
 	public static void main(String[] args) throws IOException,
 			ClassNotFoundException {
 		Client client = (Client) ApplicationContext.getBean("client");
 		String host = getHostFromArgsOrDefault(args);
 		int port = getPortFromArgsOrDefault(args);
 		client.start(host, port);
 	}
 
 	private static int getPortFromArgsOrDefault(String[] args) {
 		int port;
 		try {
 			port = (args.length != 2 ? DEFULT_PORT : Integer.parseInt(args[1]));
 		} catch (NumberFormatException ex) {
 			port = DEFULT_PORT;
 		}
 		return port;
 	}
 
 	private static String getHostFromArgsOrDefault(String[] args) {
 		return args.length != 2 ? DEFAULT_HOST : args[0];
 	}
 
 }
