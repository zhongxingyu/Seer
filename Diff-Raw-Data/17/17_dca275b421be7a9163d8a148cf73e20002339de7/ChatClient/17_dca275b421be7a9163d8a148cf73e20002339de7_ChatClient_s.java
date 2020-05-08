 /**
  * Chat system - Client
  * 
  * Communication Systems, HI1032
  * Lab assignment 3 - Client-Server programming
  * 
  * Simon Kers      skers@kth.se
  * Sakib Pathan    sakibp@kth.se
  *                                 KTH STH 2012
  */
 
 package chat_client;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.net.*;
 import java.io.*;
 
 /** 
 * This class creates the gui and handle outgoing packets (on the event 
  * dispatch thread).
  * Incoming packets are handled on a separate thread in the class 
  * Receiver.
  */
 public class ChatClient extends JFrame {
 	private static final long serialVersionUID = -7591669216369282173L;
 
 	public static final int SERVER_PORT = 3490;
 	
 	private InetAddress iaddr;
 	private int port;
 	private Socket so = null;
 	private DataOutputStream sout = null;
 	private Receiver receiver;
 	private JTextArea text = new JTextArea();
 	private JTextField input = new JTextField(25); 
 	
 	/**
 	 * Constructor. Creates the socket, the receiver object and the GUI.
 	 */
 	public ChatClient(String server, int port) throws IOException {
 		iaddr = InetAddress.getByName(server);
 		this.port = port;
 		this.so = new Socket(iaddr, port);
 
 		sout = new DataOutputStream(so.getOutputStream());
 
 		// Listener for window closing
 		this.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				send("/quit"); // Message to server...
 				cleanUpAndExit();
 			}	
 		});
 		// Called when user types ENTER in the text field.
 		input.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				send(input.getText());
 				if (input.getText().equalsIgnoreCase("/quit"))
 					cleanUpAndExit();
 				input.setText("");
 			}	
 		});	
 
 		// Layout stuff...
 		setTitle("TCP chat client");
 		text.setEditable(false);
 		getContentPane().setLayout(new BorderLayout());
 		getContentPane().add(new JScrollPane(text), BorderLayout.CENTER);
 		JPanel inputPanel = new JPanel();
 		inputPanel.add(new JLabel("Input"));
 		inputPanel.add(input);
 		getContentPane().add(inputPanel, BorderLayout.SOUTH);
 		input.requestFocus();
 
 		setSize(400, 300);
 		setVisible(true);
 		
 		// Create a receiver object with a separate thread.
 		receiver = new Receiver(so, text);
 		receiver.start();

 	}
 
	/** Send a datagram packet.
 	 */
 	public void send(String msg) {
 		try {
 			sout.writeBytes(msg + "\n");
 			sout.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	private void cleanUpAndExit() {
 		try {
 			receiver.stop();
 			so.close();
 			dispose(); // Dispose window
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			System.exit(0);
 		}
 	}
 
 	/**
	 * The main method
 	 */
 	public static void main(String[] args) throws IOException {
 
 		if(args.length != 1) {
 			System.out.println("usage: java ChatClient servername");
 			System.exit(0);
 		}		
 
 		// If exceptions occur during initialization, clean up and exit.
 		ChatClient client = null;
 		try {
 			client = new ChatClient(args[0], SERVER_PORT);
 		} catch (Exception e) {
 			e.printStackTrace();
 			if (client != null) {
 				client.cleanUpAndExit();
 			}
 		}
 	}
 }
