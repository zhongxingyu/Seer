 package Server;
 
 import java.net.*;
 import java.util.ArrayList;
 import javax.swing.*;

 import java.awt.FlowLayout;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.*;
 
 public class Server {
 	private ServerSocket serverSocket;
 	private JFrame window;
 	private JTextArea text;
 	private JScrollPane scroll;
 	private ArrayList<ServerClient> clients = new ArrayList<ServerClient>(20);
 
 	public Server() {
 		window = new JFrame("JIMmy Server");
 		window.setLayout(new FlowLayout(FlowLayout.LEADING));
 		window.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		window.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				shutdownServer();
 			}
 		});
 		text = new JTextArea();
 		text.setRows(20);
 		text.setColumns(55);
 		text.setEditable(false);
 		scroll = new JScrollPane(text);
 		text.append("Server started.");
 		window.add(scroll);
 		window.pack();
 		window.setVisible(true);
 	}
 
 	public void listen() {
 		try {
 			serverSocket = new ServerSocket(25568);
 		} catch (IOException e) {
 			addText("Could not bind port 25568, server already running?");
 		}
 		while (true) {
 			ServerClient client;
 			try {
 				client = new ServerClient(serverSocket.accept(), this);
 				boolean exists = false;
 				for (ServerClient c : clients) {
 					if (client.getNickname().equals(c.getNickname())) {
 						exists = true;
 					}
 				}
 				if (exists) {
 					client.out.writeUTF("Nickname " + client.getNickname() + " already taken!\nPlease reconnect with a new one!");
 				} else if (!exists) {
 					clients.add(client);
 					sendText(client.getNickname() + " at " + client.socket.getInetAddress().toString().replace("/", "") + " connected.");
 					Thread t = new Thread(client);
 					t.start();
 				}
 
 			} catch (IOException e) {
				addText("Connection failed.");
 			}
 		}
 
 	}
 
 	public void addText(String text) {
 		this.text.append("\n" + text);
 		this.text.setCaretPosition(this.text.getDocument().getLength());
 	}
 
 	public void sendText(String text) {
 		addText(text);
 		for (ServerClient c : clients) {
 			c.sendText(text);
 		}
 	}
 
 	public void removeClient(ServerClient c) {
 		clients.remove(c);
 	}
 
 	public void shutdownServer() {
 		sendText("$SERVERSHUTDOWN");
 		System.exit(0);
 	}
 
 	public static void main(String[] args) {
 		Server server = new Server();
 		server.listen();
 	}
 }
