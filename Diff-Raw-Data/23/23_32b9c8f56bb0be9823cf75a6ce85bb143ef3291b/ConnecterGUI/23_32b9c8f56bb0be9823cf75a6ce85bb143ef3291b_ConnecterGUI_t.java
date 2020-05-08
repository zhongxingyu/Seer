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
 
 public class ConnecterGUI extends JFrame implements ActionListener, Observer{
 	private static final long serialVersionUID = 4L;
 	private CheckersServer server;
 	private JList<String> connectionRequests;
 	private JFrame makeConnectionFrame;
 	private JTextArea connectHost;
 	private JTextArea connectPort;
 	private JLabel messageLabel;
 	public ConnecterGUI(int port) throws IOException{
 		super("Connection creater");
 		CheckerGame.getInstance().setServer(new CheckersServer(port));
 		server = CheckerGame.getInstance().getServer();
 		CheckerGame.getInstance().addObserver(this);
 		JButton makeConnection = new JButton("Connect to other player");
 		makeConnection.addActionListener(this);
 		connectionRequests = new JList<String>();
 		connectionRequests.setToolTipText("This shows all the players who want to start a game with you");
 		JButton accept = new JButton("Accept selected player");
 		accept.addActionListener(this);
 		this.add(connectionRequests, BorderLayout.CENTER);
 		this.add(makeConnection, BorderLayout.NORTH);
 		this.add(accept, BorderLayout.SOUTH);
 		this.setSize(200, 200);
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setLocationRelativeTo(null);
 		
 	}
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		String command = e.getActionCommand();
 		if(command.equals("Accept selected player")){
 			String req = connectionRequests.getSelectedValue();
 			if(req == null)
 				return;
 			
 			
 			if(!server.setToGameMode(req))
 				return;
 			this.setVisible(false);
 			if(makeConnectionFrame != null)
 				makeConnectionFrame.setVisible(false);
 			new CheckersFrame();
 			CheckerGame.getInstance().deleteObserver(this);
 			CheckerGame.getInstance().sendCommand("accept");
 		}
 		else if(command.equals("Connect to other player")){
 			setupFrame();
 		}
 		else if(command.equals("Connect")){
 			String host = connectHost.getText();
 			host.trim();
 			int port;
 			if(!host.equals("")){
 				String portString = connectPort.getText();
 				portString.trim();	
 				if(portString.equals("")){
 					port = CheckersServer.DEFAULT_PORT;
 				}
 				else{
 					try{
 						port = Integer.parseInt(portString);
 					}
 					catch(NumberFormatException ex){
 						messageLabel.setText("The port is a integer");
 						return;
 					}
 				}
 			}
 			else{
 				messageLabel.setText("You need to enter a host name");
 				
 				return;
 			}
 			try {
 				CheckerGame.getInstance().setClient(new CheckersClient(host, port));
 				messageLabel.setText("Connected, waiting for response");
 			} catch (IOException e1) {
 				// TODO Auto-generated catch block
 				messageLabel.setText("Connection Failed");
 		
 			}
 		}
 	}
 	@Override
 	public void update(Observable arg0, Object conn) {
 		
 		if(conn instanceof ConnectionToClient){
 			addConnection(((ConnectionToClient)conn).getInetAddress().toString());
 		}
 		else if(CheckerGame.getInstance().getClient()!= null){
 			server.killServer();
 			server = null;
 			this.setVisible(false);
 			makeConnectionFrame.setVisible(false);
 			new CheckersFrame();
 			CheckerGame.getInstance().deleteObserver(this);
 		}
 	}
 	private void setupFrame(){
 		makeConnectionFrame =new JFrame("Connect to other player");
 		makeConnectionFrame.setSize(600, 120);
 		makeConnectionFrame.setLocationRelativeTo(null);
 		messageLabel = new JLabel("Make your connection");
 		makeConnectionFrame.add(messageLabel, BorderLayout.NORTH);
 		JLabel label1 = new JLabel("Enter host name here");
 		connectHost = new JTextArea("");
 		connectHost.setColumns(10);
 		connectHost.setToolTipText("Enter host name here");
 		JLabel label2 = new JLabel("Enter port here, leave empty for default");
 		connectPort = new JTextArea("");
 		connectPort.setToolTipText("Enter port here, leave empty for default");
 		connectPort.setColumns(10);
 		JPanel pane1 = new JPanel();
 		pane1.add(label1,BorderLayout.EAST);
 		pane1.add(connectHost, BorderLayout.WEST);
 		
 		JPanel pane2 = new JPanel();
 		pane2.add(label2,BorderLayout.EAST);
 		pane2.add(connectPort, BorderLayout.WEST);
 		
 		JButton done = new JButton("Connect");
 		done.addActionListener(this);
 		makeConnectionFrame.add(done, BorderLayout.SOUTH);
 		makeConnectionFrame.add(pane2, BorderLayout.EAST);
 		makeConnectionFrame.add(pane1, BorderLayout.WEST);
 		makeConnectionFrame.setVisible(true);
 	}
 	public void addConnection(String name){
 		String [] connections;
 		ListModel<String> temp = connectionRequests.getModel();
 		connections = new String[temp.getSize()+1];
 		for(int i =0; i < temp.getSize();i++ ){
 			connections[i] = temp.getElementAt(i);// only way to get the value, no iterator 
 		}
 		connections[connections.length-1] = name;
 		connectionRequests.setListData(connections);
 	}
 	public static void main(String[] args) throws IOException{
 		ConnecterGUI s;
		try{
 		if(args.length != 0)
 			s= new ConnecterGUI(Integer.parseInt(args[0]));
 		else
 			s = new ConnecterGUI(CheckersServer.DEFAULT_PORT);
 		s.setVisible(true);
		}
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
