 import java.io.EOFException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 import java.io.IOException;
 import java.net.ServerSocket;
 import java.net.Socket;
 import javax.swing.JFrame;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.SwingUtilities;
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 public class Server extends JFrame{
 	
 	private JTextField enterField;
 	private JTextArea displayArea;
 
 	//Output Stream to Client
 	private ObjectOutputStream output;
 	//Input Stream from Client
 	private ObjectInputStream input;
 	//ServerSocket
 	private ServerSocket server;
 	//Connection to Client
 	private Socket connection;
 	private int counter = 1;
 	
 	//Default Constructor
 	public Server(){
 		
 		super("Server Application");
 		
 		enterField = new JTextField();
 		enterField.setEditable(false);
 		enterField.addActionListener(
 				
 			//Anonymous Class
 			new ActionListener(){
 				
 				public void actionPerformed(ActionEvent event){
 					
 					sendData(event.getActionCommand());
 					enterField.setText("");
 				}
 			});
 		
 		add(enterField, BorderLayout.NORTH);
 		displayArea = new JTextArea();
 		add(new JScrollPane(displayArea),BorderLayout.CENTER);
 		
 		//Setting Up Window
 		setSize(350,150);
 		setVisible(true);
 		
 	}//End Default Constructor
 	
 	public void runServer(){
 		
 		try{
 			
 			server = new ServerSocket(12345, 100);
 		
 			while(true)
 			{
 				try{
 					
 					waitForConnection();
 					getStream();
 					processConnection();
 					
 				}
 				finally{
 					
 					closeConnection();
 					++counter;
 				}
 			}
 			
 		}
 		catch(IOException e){
 			e.printStackTrace();
 		}
 	
 	}//End runServer
 	
 	
 	private void closeConnection() {
 		// TODO Auto-generated method stub
 		displayMessage("\nTerminating Connection\n");
 		
 		setTextFieldEditable(false);
 		
 		try{
 			
 			output.close();
 			input.close();
 			connection.close();
 		}
 		catch(IOException e){
 			
 			e.printStackTrace();
 		}
 		
 	}
 
 	private void displayMessage(final String message) {
 		// TODO Auto-generated method stub
 		
 		SwingUtilities.invokeLater(
 				
 				new Runnable(){
 					
 					public void run(){
 						
 						displayArea.append(message);
 					}
 				}
 				
 		);		
 	}
 
 	private void processConnection() throws IOException{
 		// TODO Auto-generated method stub
 		String message = "Connection successful";
 		
 		sendData(message);
 		
 		setTextFieldEditable(true);
 		
 		do{
 
 			try{
 				
 				message = (String) input.readObject();
 				displayMessage("\n" + message);
 			}
 			catch(ClassNotFoundException e){
 				
 				displayMessage("\nUnknown object type received");
 			}
 		}
 		while(!message.equals("CLIENT>>>TERMINATE"));
 	}
 
 	private void setTextFieldEditable(final boolean isEditable) {
 		// TODO Auto-generated method stub
 		
 		SwingUtilities.invokeLater(
 				
 				new Runnable(){
 					
 					public void run(){
 						enterField.setEditable(isEditable);
 					}
 				}		
 		);
 	}
 
 	private void getStream() throws IOException{
 		// TODO Auto-generated method stub
 		
 		output = new ObjectOutputStream(connection.getOutputStream());
 		output.flush();
 		
 		input = new ObjectInputStream(connection.getInputStream());
 		
 		displayMessage("\n Got I/O Streams");
 		
 	}
 
 	private void waitForConnection() throws IOException{
 		// TODO Auto-generated method stub
 		displayMessage("Waiting for Connection\n");
 		
 		connection = server.accept();
 		
 		displayMessage("Connection " + counter + " received from: " + 
 						connection.getInetAddress().getHostName());
 		
 	}
 
 	private void sendData(String message) {
 		// TODO Auto-generated method stub
 		try{
 			
 			output.writeObject("SERVER>>> " + message);
 			output.flush();
 			
 			displayMessage("\nSERVER>>> " + message);
 			
 		}
 		catch(IOException e){
 			
 			displayArea.append("\nError writing object");
 		}
 		
 	}
 
 }//End Class
