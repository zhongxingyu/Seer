 package client;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.io.*;
 import java.lang.reflect.Field;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import javax.swing.JDialog;
 import javax.swing.JOptionPane;
 
 import data.Database;
 
 import ui.isometric.IsoInterface;
 import util.GUI;
 
 import game.*;
 
 /**
  * Main class for the client.
  * 
  * @author greenwthom (300122757)
  * 
  */
 public class Client implements ClientMessageHandler {
 	private Socket skt;
 	private String userName;
 	private String characterName = null;
 	private long userGID = -1;
 	private InputStreamReader in;
 	private BufferedReader reader;
 	private OutputStreamWriter out;
 	private BufferedWriter writer;
 	private IsoInterface view;
 	private GameWorld world = new GameWorld();
 	private boolean debugMode;
 	private Color chatTextColor = Color.getHSBColor((float) Math.random(), 1, 1);
 
 	public static void main(String[] args) {
 		boolean debugMode = false;
 		String host = "localhost";
 		String username = "";
 		
 		int port = 32765;
 		// take command line server and info
 		if (args.length == 3) {
 			host = args[0];
 			port = Integer.parseInt(args[1]);
 			username = args[2];
 		} else { // user input dialogs to get server and player information
 			String server = JOptionPane
 					.showInputDialog("Please enter a server ( [hostname]:[port] or [hostname] )");
 			if (server == null)
 				System.exit(0); // closes if cancel is pressed
 			username = JOptionPane
 					.showInputDialog("Please pick a username (if you have previously connected, please use the same name)");
 			if (username == null)
 				System.exit(0);// closes if cancel is pressed
 			if (username.equals(""))
 				username = "Player" + (int) (Math.random() * 1000);
 			if (server.length() > 0) {
 				String[] split = server.split(":");
 				host = split[0];
 				if (split.length == 2)
 					port = Integer.parseInt(split[1]);
 
 			}
 		}
 		if (debugMode)
 			System.out.println(host + ", " + port);
 		new Client(host, port, username, debugMode);
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
 		this.userName = uid;
 		this.debugMode = debugMode;
 		try {
 
 			// creating socket and readers/writers
 			skt = new Socket(host, port);
 			if (debugMode)
 				System.out.println("connected to " + host + " on " + port);
 			in = new InputStreamReader(skt.getInputStream());
 			reader = new BufferedReader(in);
 			out = new OutputStreamWriter(skt.getOutputStream());
 			writer = new BufferedWriter(out);
 
 			// creating GUI
 			
 			NetworkListenerThread updater = new NetworkListenerThread(reader, this, world);
 
 			
 			writer.write("uid " + uid + "\n");
 			updater.start();
 			writer.flush();
 
 		} catch (UnknownHostException e) {
 			Client.exit("Unknown host name");
 		} catch (IOException e) {
 			Client.exit("Connection to server lost");
 		}
 
 	}
 
 	public void sendMessage(ClientMessage message) {
 		try {
 			String send = "cmg " + Database.escapeNewLines(Database.treeToString(ClientMessage.serializer(world, 0).write(message))) + "\n";
 			if (debugMode)
 				System.out.print("Sent: " + send);
 			writer.write(send);
 			writer.flush();
 		} catch (IOException e) {
 			Client.exit("Connection to server lost");
 		}
 
 	}
 
 
 	public void sendChat(String chatText) {
 		if (debugMode)
 			System.out.print("Sent chat: " + chatText);
 		if (chatText.startsWith("/me"))
			if (chatText.length() > 4) chatText = "*" + userName + " " + chatText.substring(4);
 		else if (chatText.startsWith("/color")) {
 			Color newColor = null;
 			if (chatText.substring(7).startsWith("#")) 
 					newColor = Color.decode("0x"+chatText.substring(8));
 			else {
 				//code from http://www.java-forums.org/advanced-java/27084-rgb-color-name.html.
 				//This allows a color to be selected with 
 				Field field;
 				try {
 					field = Class.forName("java.awt.Color").getField(chatText.substring(7));
 					int rgb = ((Color)field.get(null)).getRGB();
 					newColor = new Color(rgb);
 				} catch (NoSuchFieldException e) {
 					view.incomingChat("GAME: Color \"" + chatText.substring(7) + "\" not found", Color.RED);
 				} catch (Exception e) {
 					if (debugMode) {
 						System.err.println("That's what you get for using reflection");
 						e.printStackTrace();
 					}
 				}
 				
 			}
 			if (newColor != null) chatTextColor = newColor;
 			return;
 		} else if (chatText.startsWith("/resetcolor")) {
 			chatTextColor = Color.getHSBColor((float) Math.random(), 1, 1);
 			return;
 		} else
 			chatText = userName + ": " + chatText;
 			String send = "cts " + chatTextColor.getRGB() + "::::" + chatText + "\n";
 			try {
 				writer.write(send);
 				writer.flush();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 	}
 
 	/**
 	 * Exits the application with a Message Dialog explaining what happened
 	 * 
 	 * @param message
 	 *            message to be printed
 	 */
 	public static void exit(String message) {
 		System.out.println(message);
 		System.out.flush();
 		JOptionPane.showMessageDialog(null, message);
 		System.exit(0);
 	}
 	
 	/**
 	 * Creates the UI with the player's GID
 	 * @param uid the GID of the player for the UI
 	 */
 	public void receivedUID(long uid) {
 		if(userGID == -1) {
 			userGID = uid;
 
 			// showing GUI
 			view = new IsoInterface("Run, Escape!", world, this, userGID);
 			view.show();
 		}
 	}
 
 	/**
 	 * calls the logMessage on the UI
 	 * @param message message to be logged
 	 */
 	public void logMessage(String message) {
 		if(view != null) {
 			view.logMessage(message);
 		}
 	}
 
 	/**
 	 * gives a chat message to the UI
 	 * @param message message to be shown
 	 * @param color color of the message
 	 */
 	public void incomingChat(String message, Color color) {
 		if(view != null) {
 			view.incomingChat(message, color);
 		}
 	}
 	
 	/**
 	 * gets the character's name
 	 */
 	public String getCharacterName() {
 		return characterName;
 	}
 
 	
 	/**
 	 * sets the character's name, and sends it to the server
 	 * @param charName
 	 */
 	public void setAndSendCharacterName(String charName) {
 		try {
 			writer.write("cid " + charName+"\n");
 			writer.flush();
 		} catch (IOException e) {
 			Client.exit("Connection to server lost, you can reconnect using the same using name to return where you were at");
 		}
 		characterName = charName;
 		
 	}
 	
 	/**
 	 * sets the character's name without sending it to the server
 	 * @param charName
 	 */
 	public void setCharacterName(String charName) {
 		characterName = charName;
 		
 	}
 }
