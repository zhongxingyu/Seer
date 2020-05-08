 package client;
 
 import game.PlayerMessage;
 
 import java.io.*;
 import java.net.InetAddress;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JOptionPane;
 
 import data.Database;
 
 import ui.isometric.IsoInterface;
 import ui.isometric.mock.ClientMessageHandlerMock;
 import util.*;
 
 import game.*;
 
 /**
  * Main class for the client.
  * 
  * @author greenwthom
  * 
  */
 public class Client implements ClientMessageHandler {
 	private Socket skt;
 	private InputStreamReader in;
 	private BufferedReader reader;
 	private OutputStreamWriter out;
 	private BufferedWriter writer;
 	private IsoInterface view;
 	private GameWorld world = new GameWorld();
 	private boolean debugMode;
 
 	public static void main(String[] args) {
		private boolean debugMode = false;
 		String host = "localhost";
 		int port = 32765;
 		String server = JOptionPane.showInputDialog("Please enter a server ( [hostname]:[port] or [hostname] )");
 		if (server.length() > 0) {
 			String[] split = server.split(":");
 			host = split[0];
 			if (split.length == 2)
 				port = Integer.parseInt(split[1]);
 
 		}
 		if (debugMode) System.out.println(host + ", " + port);
 		Client client = new Client(host, port, debugMode);
 
 	}
 
 	/**
 	 * Network client for the game
 	 * 
 	 * @param host
 	 *            server hostname
 	 * @param port
 	 *            server port
 	 */
 	public Client(String host, int port, boolean debugMode) {
 		this.debugMode = debugMode;
 		boolean debug = true;
 		try {
 
 			// creating socket and readers/writers
 			skt = new Socket(host, port);
 			if (debug)
 				System.out.println("connected to " + host + " on 32768");
 			in = new InputStreamReader(skt.getInputStream());
 			reader = new BufferedReader(in);
 			out = new OutputStreamWriter(skt.getOutputStream());
 			writer = new BufferedWriter(out);
 
 			UpdateThread updater = new UpdateThread(reader, view, world);
 
 			// sending name
 			writer.write("uid Bob\n");
 			updater.start();
 			writer.flush();
 
 			// creating GUI
 			view = new IsoInterface("IsoTest", world, this);
 			view.show();
 
 		} catch (UnknownHostException e) {
 			System.out.println("Unknown Host");
 		} catch (IOException e) {
 			System.out.println("IO Error");
 		}
 
 	}
 
 	public void sendMessage(ClientMessage message) {
 		try {
 			String send = Database.escapeNewLines(Database.treeToXML(ClientMessage.serializer(world, 0).write(message)));
 			System.out.print(send);
 			writer.write(send);
 			writer.flush();
 		} catch (IOException e) {
 			System.out.println("Bother, the BufferedWriter broke");
 		}
 
 	}
 
 }
