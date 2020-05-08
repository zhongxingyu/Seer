 package aidancbrady.client;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.Timer;
 import javax.swing.UIManager;
 import javax.swing.border.EtchedBorder;
 import javax.swing.border.TitledBorder;
 
 import aidancbrady.client.ClientCore.ConnectionState;
 
 public class GuiClient extends JFrame implements WindowListener
 {
 	private static final long serialVersionUID = 1L;
 	
	public JTextArea chatBox;
 	
	public JList statistics;
 	
 	public JList onlineUsersList;
 	
 	public JButton setPortButton;
 	
 	public JButton connectButton;
 	
 	public JButton disconnectButton;
 	
 	public JTextField connectionField;
 	
 	public JLabel portLabel;
 	
 	public JLabel connectedLabel;
 	
 	public JTextField chatField;
 	
 	public JLabel usernameLabel;
 	
 	public JTextField usernameField;
 	
 	public boolean isOpen = true;
 	
 	public Timer timer;
 	
 	public GuiClient()
 	{
 		super("DynamicServer (Client)");
 		
 		timer = new Timer(100, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent event)
 			{
 				int onlineIndex = onlineUsersList.getSelectedIndex();
 				
 				Vector<String> userVector = new Vector<String>();
 				
 				for(ClientUser user : ClientCore.instance().usersOnline)
 				{
 					userVector.add(user.username);
 				}
 				
 				if(userVector.isEmpty())
 				{
 					userVector.add("No users online.");
 				}
 				
 				onlineUsersList.setListData(userVector);
 				onlineUsersList.setSelectedIndex(onlineIndex);
 				
 				if(userVector.size() == 1)
 				{
 					onlineUsersList.setSelectedIndex(0);
 				}
 				
 				Vector<String> statsVector = new Vector<String>();
 				statsVector.add("Connected: " + ClientCore.instance().state.description);
 				statsVector.add("Online count: " + ClientCore.instance().usersOnline.size());
 				statsVector.add("Active threads: " + Thread.activeCount());
 				statsVector.add("Active memory: " + (int)((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())/1000000) + "MB");
 				statsVector.add("Total memory: " + (int)(Runtime.getRuntime().totalMemory()/1000000) + "MB");
 				
 				statistics.setListData(statsVector);
 			}
 		});
 		
 		timer.start();
 		
 		try {
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 	    } catch (Exception localException) {}
 		
 		JPanel completePanel = new JPanel(new BorderLayout());
 		
 		JPanel leftInfoPanel = new JPanel(new BorderLayout());
 		leftInfoPanel.setPreferredSize(new Dimension(206, 800));
 		
 		JPanel rightInfoPanel = new JPanel(new BorderLayout());
 		rightInfoPanel.setPreferredSize(new Dimension(206, 800));
 		
 		setBackground(Color.LIGHT_GRAY);
 		setResizable(false);
 		
 		//Start user list panel
 		onlineUsersList = new JList();
 		
 		onlineUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		onlineUsersList.setBorder(new TitledBorder(new EtchedBorder(), "Online Users"));
 		onlineUsersList.setVisible(true);
 		onlineUsersList.setFocusable(true);
 		onlineUsersList.setEnabled(true);
 		onlineUsersList.setSelectionInterval(1, 1);
 		onlineUsersList.setBackground(Color.GRAY);
 		onlineUsersList.setToolTipText("The users currently connected to this server.");
		rightInfoPanel.add(new JScrollPane(onlineUsersList));
 		//End user list panel
 		
 		//Start port setter panel
 		JPanel serverControlPanel = new JPanel();
 		serverControlPanel.setBorder(new TitledBorder(new EtchedBorder(), "Server Control"));
 		serverControlPanel.setVisible(true);
 		serverControlPanel.setBackground(Color.GRAY);
 		serverControlPanel.setFocusable(false);
 		serverControlPanel.setPreferredSize(new Dimension(206-15, 180));
 		serverControlPanel.setToolTipText("Set this server's active port to a new value.");
 		
 		connectedLabel = Util.getWithFont(new JLabel("Idle -"), new Font("Arial", Font.BOLD, 14));
 		serverControlPanel.add(connectedLabel);
 		
 		portLabel = new JLabel("N/A");
 		serverControlPanel.add(portLabel);
 		
 		connectionField = new JTextField();
 		connectionField.setFocusable(true);
 		connectionField.setText("");
 		connectionField.setPreferredSize(new Dimension(140, 20));
 		connectionField.addActionListener(new SetPortListener());
 		serverControlPanel.add(connectionField, "North");
 		
 		setPortButton = new JButton("Confirm");
 		setPortButton.setFocusable(true);
 		setPortButton.setPreferredSize(new Dimension(120, 25));
 		setPortButton.addActionListener(new SetPortListener());
 		serverControlPanel.add(setPortButton, "Center");
 		
 		connectButton = new JButton("Connect");
 		connectButton.setFocusable(true);
 		connectButton.setPreferredSize(new Dimension(80, 25));
 		connectButton.setEnabled(true);
 		connectButton.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				ClientCore.instance().connect();
 			}
 		});
 		serverControlPanel.add(connectButton, "South");
 		
 		disconnectButton = new JButton("Disconnect");
 		disconnectButton.setFocusable(true);
 		disconnectButton.setPreferredSize(new Dimension(100, 25));
 		disconnectButton.setEnabled(false);
 		disconnectButton.addActionListener(new ActionListener() 
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				ClientCore.instance().disconnect();
 			}
 		});
 		serverControlPanel.add(disconnectButton, "South");
 		
 		leftInfoPanel.add(serverControlPanel, "North");
 		//End port setter panel
 		
 		//Start username panel
 		JPanel usernamePanel = new JPanel();
 		usernamePanel.setBorder(new TitledBorder(new EtchedBorder(), "Username"));
 		usernamePanel.setVisible(true);
 		usernamePanel.setBackground(Color.GRAY);
 		usernamePanel.setFocusable(false);
 		usernamePanel.setToolTipText("Set your username to a new value.");
 		
 		JLabel sideLabel = new JLabel("Username:");
 		usernamePanel.add(sideLabel, "North");
 		
 		usernameLabel = Util.getWithFont(new JLabel("Undefined"), new Font("Arial", Font.BOLD, 14));
 		usernamePanel.add(usernameLabel, "North");
 		
 		usernameField = new JTextField();
 		usernameField.setFocusable(true);
 		usernameField.setText("");
 		usernameField.setPreferredSize(new Dimension(140, 20));
 		usernameField.addActionListener(new UsernameListener());
 		usernamePanel.add(usernameField, "Center");
 		
 		JButton usernameButton = new JButton("Confirm");
 		usernameButton.setFocusable(true);
 		usernameButton.setPreferredSize(new Dimension(80, 25));
 		usernameButton.addActionListener(new UsernameListener());
 		usernamePanel.add(usernameButton, "South");
 		
 		leftInfoPanel.add(usernamePanel);
 		//End username panel
 		
 		//Start statistics panel
 		statistics = new JList();
 		
 		for(MouseListener listener : statistics.getMouseListeners())
 		{
 			statistics.removeMouseListener(listener);
 		}
 		
 		statistics.setBorder(new TitledBorder(new EtchedBorder(), "Statistics"));
 		statistics.setVisible(true);
 		statistics.setBackground(Color.GRAY);
 		statistics.setFocusable(false);
 		statistics.setToolTipText("Statistics regarding this server.");
		JScrollPane statScroll = new JScrollPane(statistics);
		statScroll.setPreferredSize(new Dimension(206-15, 180));
		leftInfoPanel.add(statScroll, "South");
 		//End statistics panel
 		
 		completePanel.add(leftInfoPanel, "West");
 		completePanel.add(rightInfoPanel, "East");
 		
 		JPanel mainPanel = new JPanel(new BorderLayout());
 		
 		//Start chat box panel
 		chatBox = new JTextArea();
 		chatBox.setEditable(false);
 		chatBox.setBorder(new TitledBorder(new EtchedBorder(), "Chatbox"));
 		chatBox.setAutoscrolls(true);
 		chatBox.setBackground(Color.LIGHT_GRAY);
 		mainPanel.add(new JScrollPane(chatBox), "Center");
 		//End chat box panel
 		
 		JPanel chatEntryPanel = new JPanel(new BorderLayout());
 		chatEntryPanel.setBackground(Color.WHITE);
 		
 		//Start chat field panel
 		chatField = new JTextField();
 		chatField.setFocusable(true);
 		chatField.setText("");
 		chatField.setPreferredSize(new Dimension(120, 40));
 		chatField.addActionListener(new ChatBoxListener());
 		chatField.setBorder(new TitledBorder(new EtchedBorder(), "Type here to Chat"));
 		chatEntryPanel.add(chatField, "Center");
 		//End chat field panel
 		
 		JButton clearChatButton = new JButton("Clear");
 		clearChatButton.setVisible(true);
 		clearChatButton.setBackground(Color.WHITE);
 		clearChatButton.setFocusable(true);
 		clearChatButton.setPreferredSize(new Dimension(60, 40));
 		clearChatButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				chatBox.setText("Chat cleared.");
 				appendChat("");
 			}
 		});
 		chatEntryPanel.add(clearChatButton, "East");
 		
 		mainPanel.add(chatEntryPanel, "South");
 		
 		completePanel.add(mainPanel, "Center");
 		add(completePanel);
 		
 		addWindowListener(this);
 		setSize(854, 580);
 		setVisible(true);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 	}		
 
 	public void appendChat(String str) 
 	{	
 		chatBox.append(str+"\n");	
 		chatBox.setCaretPosition(chatBox.getText().length() - 1);
 	}
 	
 	public class UsernameListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent arg0)
 		{
 			if(usernameField.getText() == null || usernameField.getText().equals(""))
 			{
 				return;
 			}
 			
 			if(!Util.isValidUsername(usernameField.getText()))
 			{
 				JOptionPane.showMessageDialog(GuiClient.this, "Invalid username.\nA username must be at most 16 characters long,\nand only alphabetic characters or digits may be used.", "Warning", JOptionPane.WARNING_MESSAGE);
 				usernameField.setText("");
 				return;
 			}
 			
 			if(usernameField.getText().equals(ClientCore.instance().username))
 			{
 				return;
 			}
 			
 			if(ClientCore.instance().state != ConnectionState.CONNECTED)
 			{
 				ClientCore.instance().setUsername(usernameField.getText());
 			}
 			
 			if(ClientCore.instance().state == ConnectionState.CONNECTED)
 			{
 				ClientCore.instance().activeConnection.printWriter.println("/user:" + usernameField.getText());
 			}
 			
 			usernameField.setText("");
 		}
 	}
 	
 	public class SetPortListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent arg0) 
 		{
 			if(ClientCore.instance().state == ConnectionState.DISCONNECTED)
 			{
 				String command = connectionField.getText().trim().toLowerCase();
 				
 				if(command == null || command.equals(""))
 				{
 					return;
 				}
 				
 				try {
 					String[] split = command.split(":");
 					
 					if(split.length != 2 || !Util.isVaildIP(split[0]))
 					{
 						JOptionPane.showMessageDialog(GuiClient.this, "Please enter a valid IPv4 address.\nX.X.X.X:X");
 						connectionField.setText("");
 						return;
 					}
 					
 					ClientCore.instance().serverIP = split[0];
 					ClientCore.instance().serverPort = Integer.parseInt(split[1]);
 					portLabel.setText(ClientCore.instance().serverIP + ":" + ClientCore.instance().serverPort);
 					connectionField.setText("");
 				} catch(Exception e) {
 					JOptionPane.showMessageDialog(GuiClient.this, "Invalid characters.", "Warning", JOptionPane.WARNING_MESSAGE);
 					connectionField.setText("");
 				}
 			}
 		}
 	}
 
 	public class ChatBoxListener implements ActionListener
 	{
 		@Override
 		public void actionPerformed(ActionEvent arg0) 
 		{
 			try {
 				chatField.setText("");
 				
				String command = arg0.getActionCommand().trim();
 				
 				if(command == null || command.equals(""))
 				{
 					return;
 				}
 				
 				if(!Util.isValidMessage(command))
 				{
 					JOptionPane.showMessageDialog(GuiClient.this, "Invalid message.\nA message must be at most 500 characters.", "Warning", JOptionPane.WARNING_MESSAGE);
 					return;
 				}
 				
 				command = Util.trimMessage(command);
 				
 				if(ClientCore.instance().state == ConnectionState.CONNECTED && ClientCore.instance().activeConnection.printWriter != null)
 				{
 					ClientCore.instance().activeConnection.printWriter.println("/msg:" + command);
 				}
 				else {
 					appendChat("[Disconnected]: " + command);
 				}
 			} catch(Exception e) {
 				appendChat("Error: " + e.getMessage());
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	@Override
 	public void windowClosing(WindowEvent e)
 	{
 		timer.stop();
 		isOpen = false;
 		
 		synchronized(ClientCore.instance())
 		{
 			ClientCore.instance().notify();
 		}
 	}
 	
 	@Override
 	public void windowClosed(WindowEvent e) {}
 	
 	@Override
 	public void windowOpened(WindowEvent e) {}
 	
 	@Override
 	public void windowIconified(WindowEvent e) {}
 	
 	@Override
 	public void windowDeiconified(WindowEvent e) {}
 	
 	@Override
 	public void windowActivated(WindowEvent e) {}
 	
 	@Override
 	public void windowDeactivated(WindowEvent e) {}
 }
