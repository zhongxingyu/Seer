 package twitter.app;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Graphics;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.LinkedHashSet;
 import java.util.LinkedList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.concurrent.ExecutionException;
 
 import javax.imageio.ImageIO;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.DefaultListModel;
 import javax.swing.GroupLayout;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JProgressBar;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.SwingConstants;
 import javax.swing.SwingWorker;
 import javax.swing.border.BevelBorder;
 import javax.swing.text.Caret;
 import javax.swing.text.DefaultCaret;
 
 import twitter4j.Twitter;
 import twitter4j.TwitterException;
 import twitter4j.internal.logging.Logger;
 
 public class MainFrame extends JFrame{
 	
 	private int screenWidth;
 	private int screenHeight;
 	private JPanel buttonPanel;		
 	private JPanel panelTwo;
 	private JPanel timeLinePanel;	
 	public static final Logger LOG = Logger.getLogger(MainFrame.class);
 	private Twitter twitter;
 	private TwitterInit twitterInit;
 	private FriendList fl;	
 	private UserDirectMessage udm;
 	private UserStatus us;	
 	private TimeLine tl;
 	private AutoUpdate au;
 	private String currentName;
 	MainFrame(TwitterInit ti, Twitter twitter){		
 		this.twitterInit = ti;
 		this.twitter = twitter;
 		this.fl = ti.getFl();		
 		this.udm = ti.getUdm();
 		this.us = ti.getUs();
 		this.tl = ti.getTlu();
 		Toolkit kit = Toolkit.getDefaultToolkit();
 		Dimension screenSize = kit.getScreenSize();
 		screenHeight = screenSize.height;
 		screenWidth = screenSize.width;
 		setSize(screenWidth/2, screenHeight/2);
 		setMinimumSize(new Dimension(screenWidth/2, screenHeight/2));
 		setLocationRelativeTo(null);		
 		setTitle("Twitter Application");
 		setResizable(true);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		getContentPane().setBackground(Color.WHITE);
 		
 		buttonPanel = createPreferredSizePanel(Color.WHITE,  new Dimension(screenWidth/4, screenHeight/16));		
 		panelTwo = createPreferredSizePanel(Color.WHITE,  new Dimension(screenWidth/4, 7*screenHeight/16));		
 		timeLinePanel = createPreferredSizePanel(Color.WHITE, new Dimension(screenWidth/4, screenHeight/2));
 						
 		buttonPanel.setBorder(BorderFactory.createEtchedBorder());
 		panelTwo.setBorder(BorderFactory.createEtchedBorder());
 		timeLinePanel.setBorder(BorderFactory.createEtchedBorder());
 		timeLinePanel.setName("null");
 		
 		createMenuBar();
 		createButtonPanel();
 		createEmptyPanel();		
 		
 		Timer t = new Timer(true);
 		t.schedule(new TimerTask(){
 			@Override
 			public void run() {
 				LOG.info("Time Line Updating...");				
 				au = new AutoUpdate();
 				au.execute();				
 			}}, 0, 180000);
 	
 		
 		
 		GroupLayout layout = new GroupLayout(getContentPane());
 		getContentPane().setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 						.addComponent(buttonPanel)
 						.addComponent(panelTwo))
 				.addComponent(timeLinePanel));				
 		
 		layout.linkSize(SwingConstants.HORIZONTAL,buttonPanel, panelTwo);
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addGroup(layout.createSequentialGroup()
 						.addComponent(buttonPanel)
 						.addComponent(panelTwo))
 				.addComponent(timeLinePanel)));
 	}
 	
 	public void createMenuBar(){
 		JMenuBar menuBar = new JMenuBar();
 		JMenu menu = new JMenu("Menu");				
 		JMenuItem logoutItem = new JMenuItem("Logout");
 		JMenuItem exit = new JMenuItem("Exit");
 		menuBar.add(menu);
 		menu.add(logoutItem);
 		menu.add(exit);
 		setJMenuBar(menuBar);
 		
 		logoutItem.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();				
 				LogOut logout = new LogOut();
 				logout.doLogout();
 			}});
 		exit.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				dispose();
 				if(au.getState() == SwingWorker.StateValue.STARTED){
 					do{
 						
 					}
 					while(au.getState() == SwingWorker.StateValue.DONE);
 				}
 				System.exit(0);
 				
 			}});
 	}
 
 	public void createButtonPanel(){		
 		buttonPanel.setName("buttonPanel");
 		LOG.info(buttonPanel.getName());
 		GroupLayout layout = new GroupLayout(buttonPanel);
 		buttonPanel.setLayout(layout);
 		
 		layout.setAutoCreateContainerGaps(true);
 		layout.setAutoCreateGaps(true);
 		
 		JButton tweet = new JButton("Tweet");
 		JButton friendList = new JButton("Friend List");
 		final JButton update = new JButton("Update");
 		JButton directMessages = new JButton("DirectMessages");
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addComponent(tweet)
 				.addComponent(friendList)
 				.addComponent(directMessages)
 				
 				);
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addComponent(tweet)
 						.addComponent(friendList)
 						.addComponent(directMessages)
 						)
 				);
 	
 		
 		tweet.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				panelTwo.removeAll();
 				createTweetPanel();
 			}	
 		});
 		
 		friendList.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				panelTwo.removeAll();
 				createFriendPanel();
 			}
 		});
 		/*
 		update.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 						
 				
 		});
 		*/
 		directMessages.addActionListener(new ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				panelTwo.removeAll();
 				createConversationsListPanel();
 				
 			}
 			
 		});
 	}
 	
 	public void createEmptyPanel(){		
 		panelTwo.setName("emptyPanel");
 		LOG.info(panelTwo.getName());
 		panelTwo.setLayout(new BorderLayout());		
 		final ImageIcon icon = new ImageIcon("F:\\git\\TwitterApp\\twitter\\images.jpg");		
 		JLabel label = new JLabel();
 		label.setIcon(icon);
 		label.setHorizontalAlignment(SwingConstants.CENTER);
 		panelTwo.add(BorderLayout.CENTER,label);		
 	}
 	
 	public void createTweetPanel(){
 		panelTwo.setName("tweetPanel");
 		LOG.info(panelTwo.getName());
 		GroupLayout layout = new GroupLayout(panelTwo);		
 		panelTwo.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		
 		final JTextArea textArea = new JTextArea();
 		JScrollPane scrollPane = new JScrollPane(textArea);			
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		final JLabel label = new JLabel();
 		label.setVisible(false);
 		JButton send = new JButton("Update your status!");
 		send.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {				
 				boolean updated =us.update(textArea.getText());
 				if(updated){
 					LOG.info("Twitter status update correctly");
 					label.setText("Status updated!!!");
 					label.setVisible(true);
 				}
 				if(!updated){
 					LOG.info("Twitter status has not been updated ");
 					label.setText("An erros has occurred");
 					label.setVisible(true);
 				}
 			}
 		});
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addComponent(scrollPane)
 						.addComponent(send)
 						.addComponent(label))
 						);
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addComponent(scrollPane))
 			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 			    		.addComponent(send))
 			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 			    		.addComponent(label))		
 				);
 	}
 	
 	public void createDirectMessageToPanel(final String name){
 		panelTwo.setName("DirectMessageTo");
 		LOG.info(panelTwo.getName());
 		GroupLayout layout = new GroupLayout(panelTwo);		
 		panelTwo.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		final JTextArea textArea = new JTextArea();
 		JScrollPane scrollPane = new JScrollPane(textArea);			
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		final JLabel sendStatus = new JLabel();
 		sendStatus.setVisible(false);
 		JButton send = new JButton("Send Direct Message");		
 		send.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent arg0) {				
				boolean sended = udm.sentDirectMessageTo(name, textArea.getText());
 				if(sended){
 					LOG.info("Direct Message sended");
 					sendStatus.setText("Message sended");					
 					sendStatus.setVisible(true);
 				}
 				if(!sended){
 					LOG.info("Direct Message not sended");
 					sendStatus.setText("Message not sended");
 					sendStatus.setVisible(true);
 				}
 			}
 			
 		});
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addComponent(scrollPane)
 						.addComponent(send)
 						.addComponent(sendStatus))
 						);
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addComponent(scrollPane))
 			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 			    		.addComponent(send))
 			    .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 			    		.addComponent(sendStatus))
 				);
 	}
 	
 	public void createFriendPanel(){		
 		panelTwo.setName("listOfFriends");
 		LOG.info(panelTwo.getName());
 		GroupLayout layout = new GroupLayout(panelTwo);
 		panelTwo.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		
 		LinkedHashSet<Friend> friendList = fl.getFriendList();
 		DefaultListModel<String> dlm = new DefaultListModel<String>();
 		for(Friend f : friendList){
 			dlm.addElement(f.getName()+" "+"@"+f.getScreenName());
 		}
 		final JList<String> list = new JList<String>(dlm);
 		
 		JScrollPane scrollPane = new JScrollPane(list);		
 		scrollPane.setBorder(BorderFactory.createEtchedBorder());		
 		
 		JButton directMassage = new JButton("Direct Massege");
 		JButton deleteFriend = new JButton("Dell Friend");
 		JButton addFriend = new JButton("Add Friend");
 		JLabel lable = new JLabel("Enter friend twitter accaunt @:");
 		final JLabel errorLabel = new JLabel();
 		errorLabel.setVisible(false);
 		final JTextField textField = new JTextField(1);
 		textField.setMaximumSize(new Dimension(120,10));
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(scrollPane)					
 								.addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
 										.addComponent(lable)
 										.addComponent(textField)
 										.addComponent(addFriend)
 										.addComponent(directMassage)
 										.addComponent(deleteFriend)))
 					
 						.addComponent(errorLabel))
 								
 				);
 				
 				
 		layout.linkSize(SwingConstants.HORIZONTAL,directMassage, deleteFriend, addFriend );		
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
 						.addComponent(scrollPane)
 						.addGroup(layout.createSequentialGroup()
 								.addComponent(lable)
 								.addComponent(textField)
 								.addComponent(addFriend)
 								.addComponent(directMassage)
 								.addComponent(deleteFriend)))
 				.addComponent(errorLabel)				
 				);
 		
 		directMassage.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				panelTwo.removeAll();
 				createDirectMessageToPanel(list.getSelectedValue());
 				
 			}
 		});
 		
 		deleteFriend.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				fl.deleteFriend(list.getSelectedValue());
 				panelTwo.removeAll();
 				createFriendPanel();
 			}
 		});
 		
 		addFriend.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				try {
 					fl.addFriend(textField.getText().trim());
 					panelTwo.removeAll();
 					createFriendPanel();
 					errorLabel.setText("Friend is added!");
 					errorLabel.setVisible(true);
 				} catch (TwitterException e1) {
 					LOG.info("Sorry this page doesn't exist");
 					errorLabel.setText("Sorry this page doesn't exist");
 					errorLabel.setVisible(true);
 				}
 				
 			}
 		});
 	}
 	
 	public void createConversationsListPanel(){
 		panelTwo.setName("conversationsList");
 		LOG.info(panelTwo.getName());
 		GroupLayout layout = new GroupLayout(panelTwo);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		panelTwo.setLayout(layout);	
 		DefaultListModel<String> dlm = new DefaultListModel<String>();
 		for(String s : udm.conversationsList()){
 			dlm.addElement(s);
 		}
 		final JList<String> nameList = new JList<String>(dlm);
 		JScrollPane scrollPane = new JScrollPane(nameList);		
 		scrollPane.setBorder(BorderFactory.createEtchedBorder());		
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addComponent(scrollPane));
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addComponent(scrollPane));
 		nameList.addMouseListener(new MouseListener(){
 
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				if(arg0.getButton() == MouseEvent.BUTTON1){
 					panelTwo.removeAll();
 					createInternalConversationPanel(nameList.getSelectedValue());
 				}
 				
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				// TODO Auto-generated method stub
 				
 			}});
 		
 	}
 	
 	public String createInternalConversationPanel(String n){
 		String name = n;
 		setCurrentName(name);
 		panelTwo.setName("internalConversation");
 		LOG.info(panelTwo.getName());
 		GroupLayout layout = new GroupLayout(panelTwo);
 		layout.setAutoCreateContainerGaps(true);
 		layout.setAutoCreateGaps(true);
 		panelTwo.setLayout(layout);
 		
 		JPanel container = new JPanel();
 		BoxLayout containerLayout = new BoxLayout(container, BoxLayout.PAGE_AXIS);
 		container.setLayout(containerLayout);
 		LinkedList<Conversation> conv = udm.setConversationMessages(name);		
 		for(Conversation c : conv){			
 			JPanel panel = new JPanel();
 			panel.setBorder(BorderFactory.createEtchedBorder());
 			JLabel label = new JLabel();
 			JTextArea textArea = new JTextArea();
 			BorderLayout panelLayout = new BorderLayout();
 			panel.setLayout(panelLayout);	
 			DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");				
 			String dateTime = dateFormat.format(c.getDate());
 			if(c.isSent()){				
 				label.setText(dateTime);
 				textArea.setBorder(BorderFactory.createEtchedBorder());
 				textArea.setBackground(Color.YELLOW);
 				textArea.setWrapStyleWord(true);
 				textArea.setLineWrap(true);
 				textArea.setEditable(false);
 				textArea.setText(c.getText());				
 				panel.add(textArea, BorderLayout.WEST);
 				panel.add(label, BorderLayout.CENTER);
 				
 			}
 			if(!c.isSent()){							
 				label.setText(dateTime);
 				textArea.setBorder(BorderFactory.createEtchedBorder());
 				textArea.setBackground(Color.LIGHT_GRAY);
 				textArea.setWrapStyleWord(true);
 				textArea.setLineWrap(true);
 				textArea.setEditable(false);
 				textArea.setText(c.getText());
 				panel.add(textArea, BorderLayout.WEST);
 				panel.add(label, BorderLayout.CENTER);
 				
 			}
 			container.add(panel);
 		}
 		
 		JScrollPane scrollPane = new JScrollPane(container);
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setBorder(BorderFactory.createEtchedBorder());
 		
 		JButton backButton = new JButton("Back");
 		backButton.addActionListener(new ActionListener(){
 
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				panelTwo.removeAll();
 				createConversationsListPanel();
 				
 			}
 			
 		});
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addComponent(backButton)
 						.addComponent(scrollPane)));
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addComponent(backButton)
 				.addComponent(scrollPane)
 				);
 		return name;
 	}
 	
 	public void createHomeTimeLinePanel(){		
 		timeLinePanel.setName("homeTimeLine");
 		LOG.info(timeLinePanel.getName());
 		timeLinePanel.setLayout(new BorderLayout());
 		JPanel container = new JPanel();
 		BoxLayout layout = new BoxLayout(container, BoxLayout.PAGE_AXIS);		
 		container.setLayout(layout);		
 			for(Tweets t : tl.getTimeLineList()){			
 				JPanel panel = new JPanel();
 				BorderLayout panelLayout = new BorderLayout();
 				panel.setBackground(Color.GRAY);				
 				panel.setLayout(panelLayout);
 				panel.setBorder(BorderFactory.createEtchedBorder());	
 				
 				final JTextArea textArea = new JTextArea();
 				DefaultCaret caret = (DefaultCaret) textArea.getCaret();
 				caret.setUpdatePolicy(DefaultCaret.NEVER_UPDATE);
 				textArea.append(t.getName()+"\n"+t.getText());
 				textArea.setEditable(false);
 				textArea.setWrapStyleWord(true);
 				textArea.setLineWrap(true);
 				
 				textArea.addMouseListener(new MouseListener(){
 					public void mouseClicked(MouseEvent arg0) {
 						timeLinePanel.removeAll();					
 						createInternaHomeTimeLinelPanel(textArea.getText());
 						timeLinePanel.repaint();
 						timeLinePanel.revalidate();
 					
 					}
 
 					public void mouseEntered(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					public void mouseExited(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					public void mousePressed(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 						
 					}
 
 					public void mouseReleased(MouseEvent arg0) {
 						// TODO Auto-generated method stub
 						
 					}});
 				
 				
 							
 				panel.add(textArea, BorderLayout.CENTER);			
 				container.add(panel);			
 			}
 			
 		JScrollPane timeLineScrollPane = new JScrollPane(container);
 		timeLineScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);		
 		timeLinePanel.add(BorderLayout.CENTER, timeLineScrollPane);
 							
 	}
 	
 	public void createInternaHomeTimeLinelPanel(String text){		
 		timeLinePanel.setName("internalHomeTimeLine");
 		LOG.info(timeLinePanel.getName());
 		GroupLayout layout = new GroupLayout(timeLinePanel);
 		timeLinePanel.setLayout(layout);
 		timeLinePanel.setBackground(Color.WHITE);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		JButton backButton = new JButton("back");		
 		JTextArea textArea = new JTextArea(text);			
 		textArea.setEditable(false);
 		textArea.setWrapStyleWord(true);
 		textArea.setLineWrap(true);
 		textArea.setBorder(BorderFactory.createEtchedBorder());
 		
 		layout.setHorizontalGroup(layout.createSequentialGroup()
 				.addGroup(layout.createParallelGroup(GroupLayout.Alignment.CENTER)
 						.addComponent(backButton)
 						.addComponent(textArea)
 						));
 		
 		layout.setVerticalGroup(layout.createSequentialGroup()
 				.addComponent(backButton)
 				.addComponent(textArea));
 		
 		
 		
 		backButton.addActionListener(new ActionListener(){
 
 			public void actionPerformed(ActionEvent e) {
 				// TODO Auto-generated method stub
 				timeLinePanel.removeAll();				
 				createHomeTimeLinePanel();
 				timeLinePanel.repaint();
 				timeLinePanel.revalidate();
 			}
 			
 		});
 		
 	
 	}
 			
 	public JPanel createPreferredSizePanel(Color color, Dimension dimension) {
 		JPanel panel = new JPanel();
 		panel.setBackground(color);
 		panel.setPreferredSize(dimension);
 		return panel;		
 	}
 			
 	class AutoUpdate extends SwingWorker<Object,Object>{
 
 		@Override
 		protected Object doInBackground() throws Exception {			
 			tl.setTimeLineList();
 			LOG.info("Time Line has been updated");
 			udm.setSent();
 			udm.setRecieved();
 			return null;
 		}
 		@Override
 		protected void done(){
 			if(timeLinePanel.getName().equals("null")|timeLinePanel.getName().equals("homeTimeLine")){
 				timeLinePanel.removeAll();
 				createHomeTimeLinePanel();
 				timeLinePanel.repaint();
 				timeLinePanel.revalidate();
 			}
 			if(panelTwo.getName().equals("conversationsList")){
 				panelTwo.removeAll();
 				createConversationsListPanel();
 				panelTwo.repaint();
 				panelTwo.revalidate();
 			}
 			if(panelTwo.getName().equals("internalConversation")){
 				panelTwo.removeAll();	
 				createInternalConversationPanel(getCurrentName());
 				panelTwo.repaint();
 				panelTwo.revalidate();
 			}
 		}
 	}
 
 	public String getCurrentName() {
 		return currentName;
 	}
 
 	public void setCurrentName(String currentName) {
 		this.currentName = currentName;
 	}
 }
