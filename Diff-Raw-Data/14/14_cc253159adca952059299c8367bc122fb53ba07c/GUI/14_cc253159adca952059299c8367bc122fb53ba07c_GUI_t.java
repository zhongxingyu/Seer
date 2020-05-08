 package client;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectOutput;
 import java.io.ObjectOutputStream;
 import java.util.ArrayList;
 
 import javax.swing.*;
 
 //TODO Make Message History Box Scrollable
 public class GUI implements KeyListener, WindowListener, ActionListener, MouseListener{
 	private JFrame mainFrame;
 	private JTextArea messageInputArea;
 	private JTextArea messageHistoryArea;
 	private JList buddyListArea;
 	private JButton addBuddyButton, removeBuddyButton;
 	private JPanel leftPanel, topPanel, bottomPanel, middlePanel;
 	private final DefaultListModel model = new DefaultListModel();
 	
 	private String selectedBuddy;
 	
 	public GUI( boolean exists ){
 		// If first time running program, prompt for user name
 		if(Client_Driver.getCurrentUser() == null){
 			String userName = JOptionPane.showInputDialog(null, "Username:", null);
 			
 			if( !exists ){
 				Client_Driver.createUser( userName );				
			}else{
				Client_Driver.initComm();
 			}
 			
 			// Valid user name check
 			if(userName == null || userName.equals("")){
 				System.exit(0);
 			}
 		}
 		
 		// Initial frame creation
 		mainFrame = new JFrame("Secure IM");
 		mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		mainFrame.setSize(700, 700);
 		mainFrame.setLayout(new BorderLayout());
 		mainFrame.setResizable(false);
 		
 		// Set screen location
 		int width = (int) Toolkit.getDefaultToolkit().getScreenSize().getWidth();
 		int height = (int) Toolkit.getDefaultToolkit().getScreenSize().getHeight();
 		mainFrame.setLocation(width/4, height/5);
 		
 		// Bottom panel creation
 		bottomPanel = new JPanel();
 		messageInputArea = new JTextArea(10,61);
 		messageInputArea.setLineWrap(true);
 		messageInputArea.addKeyListener(this);
 		messageInputArea.setBackground(Color.lightGray);
 		bottomPanel.add(messageInputArea, BorderLayout.SOUTH);
 		
 		// Top panel creation
 		topPanel = new JPanel();
 		addBuddyButton = new JButton("Add Buddy");
 		removeBuddyButton = new JButton("Remove Buddy");
 		
 		addBuddyButton.addActionListener(this);
 		removeBuddyButton.addActionListener(this);
 		
 		topPanel.setLayout(new GridLayout());
 		JLabel buddyLabel = new JLabel("Buddy List");
 		topPanel.add(buddyLabel, BorderLayout.WEST);
 		topPanel.add(addBuddyButton);
 		topPanel.add(removeBuddyButton);
 		
 		// Center panel creation
 		middlePanel = new JPanel();
 		messageHistoryArea = new JTextArea(30, 54);
 		messageHistoryArea.setBackground(Color.lightGray);
 		messageHistoryArea.setEditable(false);
 		
 		JScrollPane scroll = new JScrollPane (messageHistoryArea);
 		middlePanel.add(scroll);
 		middlePanel.add(messageHistoryArea);
 		
 		// Left panel creation
 		leftPanel = new JPanel();
 		leftPanel.setSize(50, 600);
 		
 		buddyListArea = new JList(model);
 		ArrayList<String> buddies =  Client_Driver.getCurrentUser().getBuddies();
 		for(String curBuddy : buddies){
 			model.addElement(curBuddy);
 		}
 		
 		buddyListArea.setLayoutOrientation(JList.VERTICAL);
 		buddyListArea.setBackground(Color.white);
 		buddyListArea.addMouseListener(this);
 		leftPanel.add(buddyListArea, BorderLayout.WEST);
 		
 		// Add panels to frame and display frame
 		mainFrame.add(bottomPanel, BorderLayout.SOUTH);
 		mainFrame.add(topPanel, BorderLayout.NORTH);
 		mainFrame.add(leftPanel, BorderLayout.WEST);
 		mainFrame.add(middlePanel, BorderLayout.CENTER);
 		mainFrame.addWindowListener(this);
 		mainFrame.setVisible(true);
 	}
 	
 	// KeyListener Methods
 	@Override
 	public void keyTyped(KeyEvent arg0) {
 		if(arg0.getKeyChar() == '\n'){
 			String receiver = (String) buddyListArea.getSelectedValue();
 			
 			if(receiver != null){
 				// Get message and clear area
 				String message = messageInputArea.getText();
 				message = message.trim();
 				
 				if(!message.equals("") || message == null){
 					// Send Message
 					Client_Driver.getCurrentUser().addSentMessage(receiver, message);
 					messageHistoryArea.setText(Client_Driver.getCurrentUser().getMessageHistory(receiver));
 				}
 			}
 			
 			messageInputArea.setText("");
 		}
 	}
 	
 	@Override
 	public void keyPressed(KeyEvent arg0) { }
 
 	@Override
 	public void keyReleased(KeyEvent arg0) { }
 	
 	// WindowListener Methods
 	@Override
 	public void windowClosing(WindowEvent arg0) {
 		// Serialize data object to a file
 		try {
 			ObjectOutput out = new ObjectOutputStream(new FileOutputStream("user.ser"));
 			out.writeObject(Client_Driver.getCurrentUser());
 			out.close();
 			Comm.stopServerSocket();
 			System.exit(0);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public void windowActivated(WindowEvent arg0) { }
 
 	@Override
 	public void windowClosed(WindowEvent arg0) { }
 
 	@Override
 	public void windowDeactivated(WindowEvent arg0) { }
 
 	@Override
 	public void windowDeiconified(WindowEvent arg0) { }
 
 	@Override
 	public void windowIconified(WindowEvent arg0) { }
 
 	@Override
 	public void windowOpened(WindowEvent arg0) { }
 
 	// ActionListener Methods
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		if(arg0.getActionCommand().equals("Add Buddy")){
 			String buddyName = JOptionPane.showInputDialog(null, "Buddy Name:", null);
 			try {
 				Client_Driver.getCurrentUser().addBuddy(buddyName);
 			} catch (NoInternetException e) {
 				System.err.println( e.getMessage() );
 			}
 			
 			model.addElement(buddyName);
 			mainFrame.validate();
 		}else if(arg0.getActionCommand().equals("Remove Buddy")){
 			String buddyName = (String) buddyListArea.getSelectedValue();
 			
 			if(buddyName != null){
 				try{
 					Client_Driver.getCurrentUser().removeBuddy(buddyName);
 				} catch (NoInternetException e) {
 					System.err.println( e.getMessage() );
 				}
 
 				Client_Driver.getCurrentUser().deleteMessageHistory(buddyName);
 				
 				model.remove(model.indexOf(buddyName));
 				mainFrame.validate();	
 				messageHistoryArea.setText("");
 			}
 		}
 	}
 	
 	@Override
 	public void mouseReleased(MouseEvent arg0) {
 		selectedBuddy = (String) buddyListArea.getSelectedValue();
 		
 		if(selectedBuddy != null){
 			messageHistoryArea.setText(Client_Driver.getCurrentUser().getMessageHistory(selectedBuddy));
 			Client_Driver.getCurrentUser().setCurrentBuddy(selectedBuddy);
 		}
 	}
 	
 	@Override
 	public void mouseClicked(MouseEvent arg0) { }
 
 	@Override
 	public void mouseEntered(MouseEvent arg0) { }
 
 	@Override
 	public void mouseExited(MouseEvent arg0) { }
 
 	@Override
 	public void mousePressed(MouseEvent arg0) { }
 	
 	public void refreshBuddy( String buddy, boolean enable ){
 		int i = model.indexOf( buddy );
 		buddyListArea.getComponent( i ).setEnabled( enable );
 	}
 }
