 package client.signin;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.rmi.RemoteException;
 
 import javax.swing.*;
 
 import client.model.NwbRemoteModel;
 import client.model.room.NwbClientConnector;
 import client.model.room.NwbClientRoom;
 import client.model.NwbRemoteModelObserverImpl;
 
 import server.NwbServerGate;
 import server.NwbServerGateObserver;
 import server.NwbUserDataSecure;
 
 public class NwbClientSignIn extends JFrame {
 
 	private JLabel jLabel = null;
 	private JTextField userField = null;
 	private JLabel jLabel1 = null;
 	private JTextField IPField = null;
 	private JLabel jLabel2 = null;
 	private JTextField portField = null;
 	private JButton connectButton = null;
 	private static String userStr;
 	private NwbClientSignInPanel signinPanel = null;
 	private int width;
 	private int height;
 	
 	private NwbClientRoom roomModel;
 	private NwbServerGate server;
 	private NwbUserDataSecure user;
 
 	public NwbClientSignIn(NwbClientRoom roomModel) {
 		this.roomModel = roomModel;
 		try {
 			initialize();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private NwbClientSignInPanel getLoginPanel() {
 		if (signinPanel == null) {
 			jLabel = new JLabel("Username");
 			jLabel.setBounds(new Rectangle(110, 50, 100, 18));
 			jLabel1 = new JLabel("Server IP address");
 			jLabel1.setBounds(new Rectangle(110, 110, 150, 18));
 			jLabel2 = new JLabel("Port");
 			jLabel2.setBounds(new Rectangle(270, 110, 150, 18));
 
 			signinPanel = new NwbClientSignInPanel();
 			signinPanel.setLayout(null);
 			signinPanel.setBackground(Color.white);
 			signinPanel.add(jLabel, null);
 			signinPanel.add(getUserField(), null);
 			signinPanel.add(jLabel1, null);
 			signinPanel.add(getIPField(), null);
 			signinPanel.add(jLabel2, null);
 			signinPanel.add(getPortField(), null);
 			signinPanel.add(getConnectButton(), null);
 		}
 		return signinPanel;
 	}
 
 	private JTextField getUserField() {
 		if (userField == null) {
 			userField = new JTextField();
 			userField.setBounds(new Rectangle(110, 70, 220, 28));
 		}
 		return userField;
 	}
 
 	private JTextField getIPField() {
 		if (IPField == null) {
 			IPField = new JTextField();
 			IPField.setBounds(new Rectangle(110, 130, 150, 28));
 		}
 		return IPField;
 	}
 	
 	private JTextField getPortField() {
 		if (portField == null) {
 			portField = new JTextField();
 			portField.setBounds(new Rectangle(270, 130, 60, 28));
 		}
 		return portField;
 	}
 
 	private JButton getConnectButton() {
 		if (connectButton == null) {
 			connectButton = new JButton("Connect");
 			connectButton.setBounds(new Rectangle(175, 176, 90, 25));
 			connectButton.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					try {
 						userStr = userField.getText();
 						if (userStr.equals("")) {
 							JOptionPane.showMessageDialog(NwbClientSignIn.this,
 									"Error: Username cannot be null",
 									"No username", JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						if (IPField.getText().equals("")) {
 							JOptionPane
 									.showMessageDialog(
 											NwbClientSignIn.this,
 											"Error: Server IP address cannot cannot be null",
 											"No IP address",
 											JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						if (portField.getText().equals("")) {
 							JOptionPane
 									.showMessageDialog(
 											NwbClientSignIn.this,
 											"Error: Server port number cannot cannot be null",
 											"No port",
 											JOptionPane.ERROR_MESSAGE);
 							return;
 						}
 						InetAddress.getByName(IPField.getText());
 						
 						String hostname = IPField.getText()+":"+portField.getText();
 						System.out.println(hostname);
 						
 						if(connectServer(hostname, userStr) == false)
 						{
 							return;
 						}
 						
 					} catch (IOException e1) {
 						JOptionPane.showMessageDialog(NwbClientSignIn.this,
 								"Error: Incorrect IP address format.",
 								"IP address incorrect",
 								JOptionPane.ERROR_MESSAGE);
 						e1.printStackTrace();
 						return;
 					} catch (Exception e1) {
 						e1.printStackTrace();
 					}
 					setVisible(false);
 				}
 			});
 		}
 		return connectButton;
 	}
 
 	private boolean connectServer(String hostname, String username)
 	{
 		server = NwbClientConnector.connectServer(hostname);
 		if(server == null)
 		{
 			JOptionPane.showMessageDialog(NwbClientSignIn.this,
 					"Error: Server is invalid. Try another server or port",
 					"Error",
 					JOptionPane.ERROR_MESSAGE);
 			return false;
 		}
 		
 		NwbClientSignInObserver observer = null;
 		try {
 			observer = new NwbClientSignInObserver();
 		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(NwbClientSignIn.this,
					"Error: cannot create observer. ",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
 		}
 		user = null;
 		
 		try {
 			user = server.signIn(username, observer);
 			
 			if(user == null)
 			{
 				JOptionPane.showMessageDialog(NwbClientSignIn.this,
 						"Error: Username is duplicated, try another name",
 						"Error",
 						JOptionPane.ERROR_MESSAGE);
 				return false;
 			}
 		} catch (RemoteException e) {
			JOptionPane.showMessageDialog(NwbClientSignIn.this,
					"Error: cannot connect to server. Check host and port again.",
					"Error",
					JOptionPane.ERROR_MESSAGE);
			return false;
 		}
 		
 		NwbClientConnect connectDialog = new NwbClientConnect(server, user, roomModel);
 		observer.setClientConnect(connectDialog);
 		connectDialog.setVisible(true);
 		
 		return true;
 	}
 	
 	private void initialize() {
 		Dimension size = getToolkit().getScreenSize();
 		this.width = 458;
 		this.height = 396;
 		setLocation((size.width - this.width) / 2,
 				(size.height - this.height) / 2);
 		setSize(this.width, this.height);
 		this.setTitle("Sign in");
 		this.setResizable(false);
 		setContentPane(getLoginPanel());
 		//this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 		this.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
 	}
 	
 	public void signOut()
 	{
 		try {
 			if(server != null && user != null)
 				server.signOut(user);
 		} catch (RemoteException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
