 import java.io.FileInputStream;
 import java.lang.Math;
 import java.net.*;
 import java.util.Properties;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 /**
  * 
  * @author Jenna
  * 
  * Client has both a Sender and Receiver and connects them to
  * 		  a DataIn and DataOut object that has been initialized
  * 		  correctly for the machine the client is on
  * 
  *
  */
 public class Client// implements ActionListener
 {
     // Input and output threads to send and receive voice data
 	private static DataInThread input;
 	private static DataOutThread output;
 	
 	// constant port number that our program will broadcast to.
 	private static final int PORT = 8000;
 	private static final int MAXBUFSIZE = 1024;
 	
     // Client and Server InetAddress
 	private static InetAddress clientIP;
 	private static InetAddress serverIP;
 
 	// GUI Stuff
 	private static JTextField textField;
 	
 	
 	
 	private static class ClientDisplay extends JPanel implements ActionListener {
 
       public void actionPerformed(ActionEvent e) {
       	String com = e.getActionCommand();
 		if (com.equals("Connect")) {
 			System.out.println("Found connecting button.");
 			try {
 				setServerIPInfo();
 				runVOIP();
 			} catch (Exception excep) {
 				// do stuff
 			}
 		} else {
 			System.out.println("Couldn't find button.");
 		}
       }
     }
 
    public static void setServerIPInfo() {
    		try {
 			String textIP = textField.getText();
 			serverIP = InetAddress.getByName(textIP);
 		} catch (UnknownHostException e) {

 		}
    }
 
    public static void setClientIPInfo() {
 	   	InetAddress ip;
 		  try {
 			ip = InetAddress.getLocalHost();
 			System.out.println("Current IP address : " + ip.getHostAddress());
 			clientIP = ip;
 	 
 		  } catch (UnknownHostException e) {
 		  	e.printStackTrace();
 		  }
    }
    public static void readConfig() throws Exception {
    		// Get all information from a config file
 		// config file contains: SERVER_IP, CLIENT_IP
 		Properties prop = new Properties();
 		prop.load(new FileInputStream("src/config.properties"));
 		clientIP = InetAddress.getByName(prop.getProperty("CLIENT_IP"));
 		serverIP = InetAddress.getByName(prop.getProperty("SERVER_IP"));
    }
    
    public static void runVOIP() throws Exception {
    	    // Create an outbound socket with the client IP to connect to
 		DatagramSocket outSocket = new DatagramSocket(PORT,clientIP);
 		
         // Create an inbound socket that will connect to us
 		DatagramSocket inSocket = new DatagramSocket();
 		inSocket.connect(serverIP,PORT);
 		
 		// Configure data in/out first
 		output = new DataOutThread(outSocket);
 		input = new DataInThread(inSocket);
 		
 		output.startSpeakers();
 		input.startMic();
    }
 
 	public static void main(String[] args) {
 		setupGUI();
 		
 		try {
 			setClientIPInfo();
 		} catch (Exception e) {
 			// do stuff
 		}
 		
 
         // Comments
 //		input.stopMic();
 //		output.stopSpeakers();
 	}
 
 	public static void setupGUI() {
 		// Modified Swing code based on http://math.hws.edu/javanotes/c6/s1.html
 	
       ClientDisplay listener = new ClientDisplay();
       JButton connectButton = new JButton("Connect");
       textField = new JTextField(20);
      JLabel label1 = new JLabel("Friend's IP Address", JLabel.CENTER);

 
       // ButtonHandler listener = new ButtonHandler();
       connectButton.addActionListener(listener);
       textField.addActionListener(listener);
 
       JPanel content = new JPanel();
       content.setLayout(new BorderLayout());
      content.add(listener);
      content.add(label1, BorderLayout.PAGE_START);
      content.add(textField, BorderLayout.CENTER);
      content.add(connectButton, BorderLayout.PAGE_END);


 
       JFrame window = new JFrame("Lean VoIP");
       window.setContentPane(content);
       window.setSize(250,100);
       window.setLocation(100,100);
       window.setVisible(true);
      window.setResizable(false);
       
 // 		JFrame gui = new JFrame("Lean VoIP");
 // 		/*
 // 		 * JFrame.
 // 		 */
 // 		gui.setSize(600,600);//Size of JFrame
 // 		gui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 // 		gui.setVisible(true);//Sets if its visible.
 // 		/*
 // 		 * JButton.
 // 		 */
 // 		JButton startButton = new JButton("Start");//The JButton name.
 // 		//startButton.addActionListener(this);//Reads the action.
 // 		gui.add(startButton);//Add the button to the JFrame.
 
 	}
 
 }
