 package client;
 
 import java.io.*;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JOptionPane;
 
 import data.Database;
 
 import ui.isometric.IsoInterface;
 
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
 		boolean debugMode = false;
 		String host = "localhost";
 		String uid = "";
 		int port = 32765;
 		//can now take command line server and info
 		if (args.length == 3) {
 			host = args[0];
 			port = Integer.parseInt(args[1]);
 			uid = args[2];
 		} else {
 			String server = JOptionPane.showInputDialog("Please enter a server ( [hostname]:[port] or [hostname] )");
 			uid = JOptionPane.showInputDialog("Please pick a username (if you have previously connected, please use the same name)");
 			if (server.length() > 0) {
 				String[] split = server.split(":");
 				host = split[0];
 				if (split.length == 2)
 					port = Integer.parseInt(split[1]);
 
 			}
 		}
 		if (debugMode)
 			System.out.println(host + ", " + port);
 		Client client = new Client(host, port, uid, debugMode);
 
 	}
 
 	/**
 	 * Network client for the game
 	 * 
 	 * @param host
 	 *            server hostname
 	 * @param port
 	 *            server port
 	 */
 	public Client(String host, int port, String uid, boolean debugMode) {
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
 			writer.write("uid " + uid + "\n");
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
 			System.out.print("Sent: " + send);
 			writer.write(send);
 			writer.flush();
 		} catch (IOException e) {
 			System.out.println("Bother, the BufferedWriter broke");
 		}
 
 	}
 
 }
