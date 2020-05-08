 package aidancbrady.server.gui;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.Timer;
 
 import aidancbrady.server.FileHandler;
 import aidancbrady.server.ServerCore;
 import aidancbrady.server.User;
 
 public class GuiCacheInfo extends JFrame implements WindowListener
 {
 	private static final long serialVersionUID = 1L;
 	
 	private String username;
 	
 	private int onlineUserID = -1;
 	
 	private Timer timer;
 	
 	public JList messageList;
 	
 	public JLabel usernameLabel;
 	
 	public JLabel idLabel;
 	
 	public JLabel onlineLabel;
 	
 	public JButton kickButton;
 	
 	public JButton removeButton;
 	
 	public GuiCacheInfo(String user)
 	{
 		super("Cached User Information");
 		
 		username = user;
 		setBackground(Color.LIGHT_GRAY);
 		setResizable(false);
 		setSize(300, 400);
 		setPreferredSize(new Dimension(300, 400));
 		setVisible(true);
 		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
 		getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));
 		
 		writeLabel(new JLabel("Cached User Information"), new Font("Arial", Font.BOLD, 14));
 		
 		if(getUser().isOnline())
 		{
 			idLabel = writeLabel(new JLabel("User ID: " + getUser().getConnection().getUserID()), new Font("Arial", Font.PLAIN, 14));
 		}
 		else {
 			onlineUserID = -1;
 			idLabel = writeLabel(new JLabel("User ID: N/A"), new Font("Arial", Font.PLAIN, 14));
 		}
 		
 		onlineLabel = writeLabel(new JLabel("Online: " + getUser().isOnline()), new Font("Arial", Font.PLAIN, 14));
 		
 		usernameLabel = writeLabel(new JLabel("Username: " + getUser().username), new Font("Arial", Font.PLAIN, 14));
 		usernameLabel.setVisible(true);
 		
 		messageList = new JList();
 		JScrollPane scroll = new JScrollPane(messageList);
 		scroll.setAlignmentX(Component.CENTER_ALIGNMENT);
 		scroll.setPreferredSize(new Dimension(200, 300));
 		scroll.setSize(new Dimension(200, 300));
 		getContentPane().add(scroll);
 		
 		JPanel buttonPanel = new JPanel(new BorderLayout());
 		
 		JPanel rightButtons = new JPanel();
 		rightButtons.setLayout(new BoxLayout(rightButtons, BoxLayout.Y_AXIS));
 		
 		removeButton = new JButton("Remove");
 		removeButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if(getUser().isOnline())
 				{
 					getUser().getConnection().socketConnection.kick();
 				}
 				
 				ServerCore.instance().cachedUsers.remove(username);
 				FileHandler.saveCaches();
 				
 				timer.stop();
 				
 				try {
 					dispose();
 					setVisible(false);
 				} catch(Throwable t) {}
 			}
 		});
 		rightButtons.add(removeButton);
 		
 		buttonPanel.add(rightButtons, "East");
 		
 		JPanel leftButtons = new JPanel();
 		leftButtons.setLayout(new BoxLayout(leftButtons, BoxLayout.Y_AXIS));
 		
 		JButton clearButton = new JButton("Clear");
 		clearButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				getUser().messages.clear();
 				
 				FileHandler.saveCaches();
 			}
 		});
 		leftButtons.add(clearButton);
 		
 		kickButton = new JButton("Kick");
 		kickButton.addActionListener(new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent arg0)
 			{
 				if(!getUser().isOnline())
 				{
 					return;
 				}
 				
 				getUser().getConnection().socketConnection.kick();
 			}
 		});
 		leftButtons.add(kickButton);
 		
 		buttonPanel.add(leftButtons, "West");
 		
 		getContentPane().add(buttonPanel);
 		
 		pack();
 		
 		timer = new Timer(100, new ActionListener()
 		{
 			@Override
 			public void actionPerformed(ActionEvent event)
 			{
 				if(ServerCore.instance().cachedUsers.get(username) == null)
 				{
 					if(onlineUserID != -1)
 					{
 						username = ServerCore.instance().connections.get(onlineUserID).user.username;
 						return;
 					}
 				}
 				
 				if(getUser() == null)
 				{
 					try {
 						dispose();
 						setVisible(false);
 						return;
 					} catch(Throwable t) {}
 				}
 				
 				int index = messageList.getSelectedIndex();
 				Vector<String> messages = new Vector<String>();
 				
 				for(String s : getUser().messages)
 				{
 					messages.add(s);
 				}
 				
 				messageList.setListData(messages);
 				messageList.setSelectedIndex(index);
 				
 				onlineLabel.setText("Online: " + getUser().isOnline());
 				
 				if(getUser().isOnline())
 				{
 					onlineUserID = getUser().getConnection().getUserID();
 					idLabel.setText("User ID: " + getUser().getConnection().getUserID());
 				}
 				else {
 					onlineUserID = -1;
 				}
 				
 				usernameLabel.setText("Username: " + getUser().username);
 				usernameLabel.setVisible(true);
 				
 				if(getUser().isOnline())
 				{
 					kickButton.setEnabled(true);
 				}
 				else {
 					kickButton.setEnabled(false);
 				}
 			}
 		});
 		
 		timer.start();
 	}
 	
 	public JLabel writeLabel(JLabel label, Font font)
 	{
 		label.setFont(font);
 		label.setAlignmentX(Component.CENTER_ALIGNMENT);
 		getContentPane().add(label, "North");
 		return label;
 	}
 	
 	public User getUser()
 	{
 		if(onlineUserID != -1 && ServerCore.instance().connections.get(onlineUserID) != null)
 		{
 			return ServerCore.instance().connections.get(onlineUserID).user;
 		}
 		else {
 			return ServerCore.instance().cachedUsers.get(username);
 		}
 	}
 	
 	@Override
 	public void windowClosing(WindowEvent e)
 	{
 		timer.stop();
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
