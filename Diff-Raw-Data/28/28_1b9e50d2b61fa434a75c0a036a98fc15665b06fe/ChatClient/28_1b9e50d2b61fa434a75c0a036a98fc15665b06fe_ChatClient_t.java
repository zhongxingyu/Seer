 package pw.svn.client;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.net.Socket;
 
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 /**
  * A client for connecting to the chat server.
  * @author Theneva
  * @version 1.0
  */
 public class ChatClient {
 
 	private Socket serverConnection;
 	
 	private ObjectOutputStream output;
 	private ObjectInputStream input;
 	
 	/** UI stuff */
 	private JFrame ui;
 	private JTextArea textAreaMessages;
 	
 	public ChatClient(String serverAddress, int serverPort) {
 
 		this.initializeUI();
 		this.startClient(serverAddress, serverPort);
 	}
 	
 	private void initializeUI() {
 
 		this.ui = new JFrame("Useless chat client");
 		
 		this.ui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		this.textAreaMessages = new JTextArea();
 		this.textAreaMessages.setEditable(false);
 		this.textAreaMessages.setLineWrap(true);
 		this.textAreaMessages.setWrapStyleWord(true);
 		
 		this.ui.add(new JScrollPane(this.textAreaMessages));
 
 		final JTextField textFieldMessage = new JTextField();
 		textFieldMessage.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				String message = textFieldMessage.getText();
 				sendMessage(message);
 				textFieldMessage.setText("");
 			}
 		});
 
 		this.ui.add(textFieldMessage, BorderLayout.SOUTH);
 		
 		this.ui.setLocationRelativeTo(null);
 		this.ui.setSize(600, 400);
 		this.ui.setVisible(true);
 	}
 	
 	private void startClient(String serverAddress, int serverPort) {
 		
 		try {
 			this.serverConnection = new Socket(serverAddress, serverPort);
 			this.output = new ObjectOutputStream(this.serverConnection.getOutputStream());
 			this.input = new ObjectInputStream(this.serverConnection.getInputStream());
 			
 			for (;;) {
 				this.displayMessage(this.readMessage());
 			}
 			
 		} catch (IOException e) {
 			e.printStackTrace();
 		} finally {
 			
 			this.displayMessage("Shutting down."); // TODO internationalization
 			try {
 				Thread.sleep(5000);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 			
 			System.exit(0);
 		}
 	}
 	
 	/**
 	 * Displays a message to the client.
 	 */
 	private void displayMessage(String message) {
 		this.textAreaMessages.append(message.concat(System.getProperty("line.separator")));
 		this.textAreaMessages.setCaretPosition(this.textAreaMessages.getText().length());
 	}
 	
 	/**
 	 * Reads a message (String) from the server.
 	 * @return the message from the server.
 	 */
 	private String readMessage() {
 		
 		String message = null;
 		
 		try {
 			message = (String) this.input.readObject();
 		} catch (ClassNotFoundException | IOException e) {
 			e.printStackTrace();
 		}
 
 		return message;
 	}
 	
 	public void sendMessage(String message) {
 		try {
 			this.output.writeObject(message.concat(" "));
 			this.output.flush();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 }
