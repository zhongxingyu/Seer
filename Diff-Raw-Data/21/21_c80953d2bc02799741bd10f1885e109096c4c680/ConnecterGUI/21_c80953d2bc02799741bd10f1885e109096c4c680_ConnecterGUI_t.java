 package seg.project.checkers.gui;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.ListModel;
 
 import ocsf.server.ConnectionToClient;
 
 import seg.project.checkers.CheckerGame;
 import seg.project.checkers.CheckersClient;
 import seg.project.checkers.CheckersServer;
 /**
  * This acts as the point of entry for the game. It displays a small GUI for connecting, and it sets up the server and starts listening, it also allows the user to connect to another
  * 
  *
  */
 public class ConnecterGUI extends JFrame implements ActionListener, Observer{
 	private static final long serialVersionUID = 4L;
 	private CheckersServer server;
 	private JList<String> connectionRequests;
 	private JFrame makeConnectionFrame;
 	private JTextArea connectHost;
 	private JTextArea connectPort;
 	private JLabel messageLabel;
 	/**
 	 * This creates the frame that will be displayed, but it will not make it visible, you need to use the setVisible(true) method of JFrame for that. \n
 	 * The constructor will also set up the server, and start listening for connections.
 	 * @param port The port to use to start the server
 	 * @throws IOException If hosting the server fails
 	 */
 	public ConnecterGUI(int port) throws IOException{
 		// Call the super
 		super("Connection creater");
 		// Make the game's server
 		CheckerGame.getInstance().setServer(new CheckersServer(port));
 		server = CheckerGame.getInstance().getServer();
 		// I need to get actions from the Checkers Server, so I observe the CheckerGame
 		CheckerGame.getInstance().addObserver(this);
 		//Button for making a connection to another player
 		JButton makeConnection = new JButton("Connect to other player");
 		makeConnection.addActionListener(this);
 		// Setting up the list that will display connections one can accept to the user
 		connectionRequests = new JList<String>();
 		connectionRequests.setToolTipText("This shows all the players who want to start a game with you");
 		// Button for accepting a connection
 		JButton accept = new JButton("Accept selected player");
 		accept.addActionListener(this);
 		// Adding all the buttons and list and arranging it using BorderLayout
 		this.add(connectionRequests, BorderLayout.CENTER);
 		this.add(makeConnection, BorderLayout.NORTH);
 		this.add(accept, BorderLayout.SOUTH);
 		this.setSize(200, 200);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE); // So the program ends when it is pressed
 		this.setLocationRelativeTo(null);
 		
 	}
 	/**
 	 * This is the what the action listener attached to the buttons will call when a button is pressed
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();// get the button's message 
 		// Check first button
 		if(command.equals("Accept selected player")){
 			String req = connectionRequests.getSelectedValue(); // Get the value of the list the user selected
 			if(req == null) // if they did not yet select one
 				return;
 			if(!server.setToGameMode(req)) // If the server already is running in game mode
 				return;
 			this.setVisible(false); // make the frame invisible
 			if(makeConnectionFrame != null)
 				makeConnectionFrame.setVisible(false); // if the connectionFrame is running, make it invisible
 			// If the user tried to connect to some one before accepting a connection
 			if(CheckerGame.getInstance().getClient() !=null){ 
 				try {
 					// Do everything that needs to be done to remove the client
 					CheckerGame.getInstance().getClient().closeConnection();
 					CheckerGame.getInstance().setClient(null);
 					CheckerGame.getInstance().setBlack(true);
 					CheckerGame.getInstance().setTurn(false);
 				} catch (IOException e1) { // If closing the connection had a problem I don't care, but I still need to set the rest of the values
 					CheckerGame.getInstance().setClient(null);
 					CheckerGame.getInstance().setBlack(true);
 					CheckerGame.getInstance().setTurn(false);
 				}
 			}
 			// Make a new Checker GUI
 			new CheckersFrame();
 			// Remove the observer, and notify the other player that the user accepted their connection
 			CheckerGame.getInstance().deleteObserver(this);
 			CheckerGame.getInstance().sendCommand("accept");
 		}
 		
 		else if(command.equals("Connect to other player")){
 			setupFrame(); // create a GUI for connecting to another player
 		}
 		// If the want to try and connect to another player
 		else if(command.equals("Connect")){
 			String host = connectHost.getText(); // Get the host name
			host = host.trim();  // just in case the added extra spaces
 			int port; // the port
 			if(!host.equals("")){ // If they didn't leave it empty
 				String portString = connectPort.getText(); // Get the port
				portString = portString.trim();	 
 				if(portString.equals("")){ // If they left it blank
 					port = CheckersServer.DEFAULT_PORT;
 					
 				}
 				else{
 					try{
 						port = Integer.parseInt(portString);
 					}
 					catch(NumberFormatException ex){ // Just in case the tried to make the port a name
 						messageLabel.setText("The port is a integer"); // Tell them what they just tried
 						return;
 					}
 				}
 			}
 			else{
 				messageLabel.setText("You need to enter a host name"); // Tell them what to do
 				
 				return;
 			}
 			if(host.equalsIgnoreCase("localhost")){
 				if(port == server.getPort())
 				{
 					messageLabel.setText("You can't connect to yourself"); // Tell them what they just tried
 					return;
 				}
 			}
 			try { // Check for errors
 				CheckerGame.getInstance().setClient(new CheckersClient(host, port)); // try and connect to the host and port provided
 				messageLabel.setText("Connected, please wait for response"); // If it worked, tell them to wait
 				((JButton)e.getSource()).setEnabled(false);	 // So they can't connect to some one else at the same time
 			}
 			catch( java.lang.IllegalArgumentException e2){ // If the number was out of range
 				messageLabel.setText("Port is not in correct range");
 			}
 			catch (IOException e1) { // if the connection failed
 				messageLabel.setText("Connection Failed");
 			}
 			
 		}
 	}
 	
 	@Override
 	public void update(Observable arg0, Object conn) {
 		
 		if(conn instanceof ConnectionToClient){// If the object is a connection to add to the list
 			addConnection(((ConnectionToClient)conn).getInetAddress().toString());
 		}
 		 // If the request to connect was accepted
 		else if(CheckerGame.getInstance().getClient()!= null){	
 			// Do everything  required to end the server and prep the client for a game
 			server.killServer(); // end server
 			server = null;
 			this.setVisible(false);
 			makeConnectionFrame.setVisible(false); // no need to do null check, it will have been created if it is this case
 			new CheckersFrame(); // start game
 			CheckerGame.getInstance().deleteObserver(this);
 		}
 	}
 	// Private method for making a connection frame
 	private void setupFrame(){
 		if(makeConnectionFrame != null){ // if it was already made, all I need to do is make is visible
 			makeConnectionFrame.setVisible(true);
 			return;
 		}
 		// Make a new jframe and set up the GUI 
 		makeConnectionFrame =new JFrame("Connect to other player");
 		makeConnectionFrame.setSize(600, 120);
 		makeConnectionFrame.setLocationRelativeTo(null);
 		// Add instruction and error text
 		messageLabel = new JLabel("Make your connection");
 		makeConnectionFrame.add(messageLabel, BorderLayout.NORTH);
 		// So the user knows where to enter their host name
 		JLabel label1 = new JLabel("Enter host name here");
 		connectHost = new JTextArea("");
 		connectHost.setColumns(10);
 		connectHost.setToolTipText("Enter host name here");
		connectHost.getDocument().putProperty("filterNewlines", Boolean.TRUE);// Code for disallowing the return key to be pressed
 		// So the user knows what to do with the port field, and they can leave it blank if they want
 		JLabel label2 = new JLabel("Enter port here, leave empty for default");
 		connectPort = new JTextArea("");
 		connectPort.setToolTipText("Enter port here, leave empty for default");
 		connectPort.setColumns(10);
		connectHost.getDocument().putProperty("filterNewlines", Boolean.TRUE);
 		// Add all the panels to the frame, so everything renders properly
 		JPanel pane1 = new JPanel();
 		pane1.add(label1,BorderLayout.EAST);
 		pane1.add(connectHost, BorderLayout.WEST);
 		JPanel pane2 = new JPanel();
 		pane2.add(label2,BorderLayout.EAST);
 		pane2.add(connectPort, BorderLayout.WEST);
 		// The button to press once the user is done
 		JButton done = new JButton("Connect");
 		done.addActionListener(this);
 		
 		makeConnectionFrame.add(done, BorderLayout.SOUTH);
 		makeConnectionFrame.add(pane2, BorderLayout.EAST);
 		makeConnectionFrame.add(pane1, BorderLayout.WEST);
 		makeConnectionFrame.setVisible(true);
 	}
 	// Private method to add a new ip to the list
 	private void addConnection(String name){
 		String [] connections;// the string array to be inputed into the list
 		ListModel<String> temp = connectionRequests.getModel(); // Get the list of current ip's in the list
 		connections = new String[temp.getSize()+1]; // initialize the array to be one bigger then the current list size
 		// add the old ip's to the array
 		for(int i =0; i < temp.getSize();i++ ){
 			connections[i] = temp.getElementAt(i);// only way to get the value, no iterator 
 		}
 		// add the last element to the one to add
 		connections[connections.length-1] = name;
 		// put them into the list
 		connectionRequests.setListData(connections);
 	}
 	// Main method used to run
 	public static void main(String[] args) throws IOException{
 		ConnecterGUI s;
 		try{ // Start up the GUI
 		if(args.length != 0) // If a port was inputed into the args
 			s= new ConnecterGUI(Integer.parseInt(args[0]));
 		else
 			s = new ConnecterGUI(CheckersServer.DEFAULT_PORT);
 		s.setVisible(true);
 		}
 		// If a port is already in use, allow the user to start their server on another one.
 		catch(java.net.BindException e){
 			String input = JOptionPane.showInputDialog("Port is in use please enter another port");
 			if(input == null)
 				System.exit(0);
 			int port = Integer.parseInt(input);
 			s = new ConnecterGUI(port);
 			s.setVisible(true);
 		}
 	}
 
 }
