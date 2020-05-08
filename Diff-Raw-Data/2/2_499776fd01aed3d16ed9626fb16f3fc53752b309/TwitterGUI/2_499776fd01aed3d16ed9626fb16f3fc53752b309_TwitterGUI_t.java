 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Event;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.datatransfer.StringSelection;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.util.ArrayList;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 import javax.swing.KeyStroke;
 import javax.swing.border.LineBorder;
 
 import model.Users;
 import twitter4j.User;
 import controller.TwitterController;
 
 /**********************************************************************
  * Twitter GUI
  * 
  * @author Nick, Vincenzo, Corey, Russ
  *********************************************************************/
 public class TwitterGUI extends JFrame implements ActionListener, KeyListener {
 
 	private static final long serialVersionUID = 1L;
 
 	private final int FRAME_HEIGHT /* 450 */= 450;
 	private final int FRAME_WIDTH /* 700 */= 700;
 
 	private TwitterController controller;
 	private JFrame frame;
 	private JPanel profilePanel, tweetPanel, followingPanel, followersPanel;
 
 	private JMenuBar menuBar;
 	private JMenu fileMenu, tweetMenu, aboutMenu;
 	private JMenuItem exit, newTweet, delete, about;
 
 	private JTabbedPane tabbedPane;
 
 	// Profile Panel
 	private String displayName, twitterName, description, location, website;
 	private JButton followersBtn, followingBtn, tweetsBtn;
 	private ImageIcon profileImage;
 	private Image profileBanner, backgroundImage;
 
 	// Tweet Panel
 	private GridBagConstraints gbc;
 	private int remaining = 140;
 	private JButton cancel, tweetSubmit, tweetShow, tweetTotal;
 	private JLabel charsRemaining;
 	private JTextArea tweetText;
 
 	/****************************************************
 	 * GUI
 	 ***************************************************/
 	public TwitterGUI() {
 		frame = new JFrame();
 		setTitle("Desktop Tweets");
 		setBackground(Color.WHITE);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		setMaximumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		setMinimumSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		setResizable(false);
 		// setUndecorated(true); //Something cool we might want to look into?
 		// (No Title Menu on Frame)
 		setLocationRelativeTo(null);
 
 		setUpController();
 
 		// Create components
 		createProfilePanel();
 		createTweetPanel();
 		createFollowingPanel();
 		createFollowersPanel();
 		createMenu();
 		createTabbedPane();
 
 		setVisible(true);
 	}
 
 	private void setUpController() {
 		controller = new TwitterController();
 
 		if (!controller.getIsSetUp()) {
 			String authUrl = controller.getAuthUrl();
 			// copy to clipboard
 			Toolkit.getDefaultToolkit().getSystemClipboard()
 					.setContents(new StringSelection(authUrl), null);
 			String pin = JOptionPane
 					.showInputDialog(
 							"Please follow this link to authenticate this App.\nEnter Pin:",
 							authUrl);
 			controller.setUp(pin);
 		}
 	}
 
 	private void createFollowersPanel() {
 		followersPanel = new JPanel();
 		followersPanel.setBackground(Color.WHITE);
 
 		followersPanel.setLayout(new BorderLayout());
 
 		ArrayList<User> userList = new ArrayList<User>();
 
 		long[] list = controller.getFollowersIDs();
 
 		for (long l : list) {
 			userList.add(controller.showUser(l));
 		}
 
 		Users followers = new Users(userList);
 
 		JList<String> jlist = new JList<String>(followers);
 
 		JScrollPane scrollpane = new JScrollPane(jlist);
 
		followersPanel.add(scrollpane);
 	}
 
 	private void createFollowingPanel() {
 		followingPanel = new JPanel();
 		followingPanel.setBackground(Color.WHITE);
 
 		followingPanel.setLayout(new BorderLayout());
 
 		JList<String> jlist = new JList<String>(new Users(
 				controller.getFollowing()));
 
 		JScrollPane scrollpane = new JScrollPane(jlist);
 
 		followingPanel.add(scrollpane);
 	}
 
 	private void createTweetPanel() {
 		tweetPanel = new JPanel();
 		tweetPanel.setBackground(Color.WHITE);
 		tweetPanel.setLayout(new GridBagLayout());
 		gbc = new GridBagConstraints();
 
 		// Instantiate vars
 		cancel = new JButton("Cancel");
 		cancel.addActionListener(this);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 0;
 		gbc.gridy = 0;
 		tweetPanel.add(cancel, gbc);
 
 		tweetSubmit = new JButton("Send Tweet");
 		tweetSubmit.addActionListener(this);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 1;
 		gbc.ipady = 0;
 		tweetPanel.add(tweetSubmit, gbc);
 
 		tweetText = new JTextArea();
 		tweetText.addKeyListener(this);
 		tweetText.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 		tweetText.setFocusable(true);
 		tweetText.setColumns(30);
 		tweetText.setRows(8);
 		tweetText.setLineWrap(true);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 0;
 		gbc.gridy = 1;
 		gbc.gridwidth = 3;
 		tweetPanel.add(tweetText, gbc);
 
 		charsRemaining = new JLabel("" + remaining + "", JLabel.RIGHT);
 		gbc.gridx = 2;
 		gbc.gridy = 2;
 		tweetPanel.add(charsRemaining, gbc);
 
 		tweetTotal = new JButton(controller.getTweetCount() + " Tweets");
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 0;
 		gbc.gridy = 3;
 		gbc.gridwidth = 1;
 		tweetPanel.add(tweetTotal, gbc);
 
 		tweetShow = new JButton("Show Tweets");
 		tweetShow.addActionListener(this);
 		gbc.fill = GridBagConstraints.HORIZONTAL;
 		gbc.gridx = 1;
 		gbc.gridy = 3;
 		gbc.gridwidth = 1;
 		gbc.anchor = GridBagConstraints.PAGE_END;
 		tweetPanel.add(tweetShow, gbc);
 	}
 
 	@SuppressWarnings("serial")
 	private void createProfilePanel() {
 		// Get User Information
 		displayName = controller.getDisplayName();
 		twitterName = controller.getTwitterName();
 		description = controller.getDescription();
 		location = controller.getLocation();
 		website = controller.getWebsite();
 		profileImage = controller.getProfileImage();
 		profileBanner = controller.getProfileBanner();
 		backgroundImage = controller.getBackgroundImage();
 
 		/** INFO PANEL */
 		JPanel infoPanel = new JPanel() {
 			protected void paintComponent(Graphics g) {
 				g.drawImage(profileBanner,
 						(FRAME_WIDTH / 2) - profileBanner.getWidth(null) / 2
 								- 8,
 						FRAME_HEIGHT / 3 + 10 - profileBanner.getHeight(null)
 								/ 2, null);
 			}
 		};
 		infoPanel.setOpaque(false);
 		infoPanel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		infoPanel.paintComponents(null);
 		c.fill = GridBagConstraints.NONE;
 
 		// Profile Image
 		ImageIcon img = profileImage;
 		c.gridy = 0;
 		JButton profImgBtn = getPlainButton(null, null);
 		profImgBtn.setPreferredSize(new Dimension(img.getIconWidth() + 2, img
 				.getIconHeight() + 2));
 		profImgBtn.setBorder(new LineBorder(Color.WHITE, 4));
 		profImgBtn.setIcon(img);
 		infoPanel.add(profImgBtn, c);
 
 		// Display Name
 		c.ipady = 5;
 		c.gridy = 1;
 		JLabel displayNameLbl = new JLabel(displayName);
 		displayNameLbl.setFont(new Font("arial", Font.BOLD, 24));
 		displayNameLbl.setForeground(Color.WHITE);
 		infoPanel.add(displayNameLbl, c);
 
 		// Twitter Name
 		c.gridy = 2;
 		JLabel twitterNameLbl = new JLabel(twitterName);
 		twitterNameLbl.setFont(new Font("arial", Font.PLAIN, 15));
 		twitterNameLbl.setForeground(Color.WHITE);
 		infoPanel.add(twitterNameLbl, c);
 		c.ipady = 30; // more padding
 
 		// Description
 		c.gridy = 3;
 		c.ipady = 0;
 		createDescriptionPanel(infoPanel, c);
 
 		// Location
 		c.ipady = 5;
 		c.gridy = 6;
 		JLabel locationLbl = new JLabel(location);
 		locationLbl.setFont(new Font("arial", Font.BOLD, 15));
 		locationLbl.setForeground(Color.WHITE);
 		infoPanel.add(locationLbl, c);
 
 		// Website
 		c.gridy = 7;
 		JLabel websiteLbl = new JLabel(website);
 		websiteLbl.setFont(new Font("arial", Font.PLAIN, 14));
 		websiteLbl.setForeground(Color.WHITE);
 		infoPanel.add(websiteLbl, c);
 
 		/** COUNT PANEL */
 		JPanel countPanel = new JPanel();
 		countPanel.setLayout(new GridBagLayout());
 		GridBagConstraints cpc = new GridBagConstraints();
 		cpc.fill = GridBagConstraints.NONE;
 		cpc.ipadx = 30;
 		countPanel.setOpaque(false);
 		// countPanel.setBackground(Color.WHITE);
 
 		followersBtn = getPlainButton("" + controller.getFollowersCount(),
 				"Followers");
 		followersBtn.addActionListener(this);
 		followingBtn = getPlainButton("" + controller.getFriendsCount(),
 				"Following");
 		followingBtn.addActionListener(this);
 		tweetsBtn = getPlainButton("" + controller.getTweetCount(), "Tweets");
 		tweetsBtn.addActionListener(this);
 
 		cpc.gridy = 0;
 		JButton blankBtn1 = getPlainButton(null, null);
 		blankBtn1.setBorderPainted(false);
 		JButton blankBtn2 = getPlainButton(null, null);
 		blankBtn2.setBorderPainted(false);
 		countPanel.add(followersBtn, cpc);
 		countPanel.add(new JLabel(" "), cpc);
 		countPanel.add(followingBtn, cpc);
 		countPanel.add(new JLabel(" "), cpc);
 		countPanel.add(tweetsBtn, cpc);
 		cpc.gridy = 1;
 		countPanel.add(new JLabel(" "), cpc);
 
 		/** PROFILE PANEL */
 		profilePanel = new JPanel() {
 			protected void paintComponent(Graphics g) {
 				g.drawImage(backgroundImage, 0, 0, null);
 				super.paintComponent(g);
 			}
 		};
 		profilePanel.setOpaque(false);
 		profilePanel.setLayout(new BorderLayout());
 		profilePanel.add(infoPanel, BorderLayout.CENTER);
 		profilePanel.add(countPanel, BorderLayout.SOUTH);
 	}
 
 	private JButton getPlainButton(String line1, String line2) {
 		String text = line1;
 		if (line2 != null)
 			text = ("<html><center><font size=6>" + line1 + "</font><br><i>"
 					+ line2 + "</i></center></html>");
 		JButton tmp = new JButton();
 		tmp.setPreferredSize(new Dimension(120, 50));
 		tmp.setFont(new Font("Arial", Font.BOLD, 15));
 		tmp.setText(text);
 		tmp.setBackground(Color.WHITE);
 		tmp.setForeground(Color.DARK_GRAY);
 		tmp.setBorder(new LineBorder(Color.GRAY, 1, true));
 		tmp.setFocusable(false);
 		tmp.setFocusPainted(false);
 		return tmp;
 	}
 
 	private void setPlainButton(String line1, String line2, JButton j) {
 		String text = line1;
 		if (line2 != null)
 			text = ("<html><center><font size=6>" + line1 + "</font><br><i>"
 					+ line2 + "</i></center></html>");
 		j.setPreferredSize(new Dimension(120, 50));
 		j.setFont(new Font("Arial", Font.BOLD, 15));
 		j.setText(text);
 		j.setBackground(Color.WHITE);
 		j.setForeground(Color.DARK_GRAY);
 		j.setBorder(new LineBorder(Color.GRAY, 1, true));
 		j.setFocusable(false);
 		j.setFocusPainted(false);
 	}
 
 	private void createDescriptionPanel(JPanel panel, GridBagConstraints c) {
 
 		if (description.length() < 55) {
 			JLabel descriptionLbl = new JLabel(description);
 			descriptionLbl.setFont(new Font("arial", Font.PLAIN, 14));
 			descriptionLbl.setForeground(Color.WHITE);
 			panel.add(descriptionLbl, c);
 		} else if (description.length() < 110) {
 			String d1, d2;
 
 			String[] w = description.split(" ");
 			int wMid = (w.length / 2) + 1;
 			System.out.println("" + wMid);
 			int dMid = description.length() / 2;
 			System.out.println("" + dMid);
 			d1 = description.substring(0,
 					description.indexOf(w[wMid], dMid - 1));
 			d2 = description.substring(description.indexOf(w[wMid], dMid - 1));
 
 			JLabel description1Lbl = new JLabel(d1);
 			JLabel description2Lbl = new JLabel(d2);
 			description1Lbl.setFont(new Font("arial", Font.PLAIN, 14));
 			description1Lbl.setForeground(Color.WHITE);
 			description2Lbl.setFont(new Font("arial", Font.PLAIN, 14));
 			description2Lbl.setForeground(Color.WHITE);
 			panel.add(description1Lbl, c);
 			c.gridy = 4;
 			panel.add(description2Lbl, c);
 		} else {
 			String d1, d2, d3;
 			String[] w = description.split(" ");
 			int wThird = (w.length / 3) + 1;
 			int dThird = description.length() / 3;
 
 			d1 = description.substring(0,
 					description.indexOf(w[wThird], dThird));
 			d2 = description.substring(description.indexOf(w[wThird], dThird),
 					description.indexOf(w[wThird * 2], dThird * 2));
 			d3 = description.substring(description.indexOf(w[wThird * 2],
 					dThird * 2));
 
 			JLabel description1Lbl = new JLabel(d1);
 			description1Lbl.setFont(new Font("arial", Font.PLAIN, 14));
 			description1Lbl.setForeground(Color.WHITE);
 			JLabel description2Lbl = new JLabel(d2);
 			description2Lbl.setFont(new Font("arial", Font.PLAIN, 14));
 			description2Lbl.setForeground(Color.WHITE);
 			JLabel description3Lbl = new JLabel(d3);
 			description3Lbl.setFont(new Font("arial", Font.PLAIN, 14));
 			description3Lbl.setForeground(Color.WHITE);
 
 			panel.add(description1Lbl, c);
 			c.gridy = 4;
 			panel.add(description2Lbl, c);
 			c.gridy = 5;
 			panel.add(description3Lbl, c);
 		}
 	}
 
 	private void createMenu() {
 		menuBar = new JMenuBar();
 
 		// File Menu
 		fileMenu = new JMenu("File");
 		exit = new JMenuItem("Exit");
 		exit.addActionListener(this);
 		exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
 				Event.CTRL_MASK));
 		fileMenu.add(exit);
 
 		// Tweet Menu
 		tweetMenu = new JMenu("Tweet");
 		newTweet = new JMenuItem("New Tweet");
 		newTweet.addActionListener(this);
 		newTweet.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
 				Event.CTRL_MASK));
 		delete = new JMenuItem("Delete");
 		delete.addActionListener(this);
 		delete.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
 				Event.CTRL_MASK));
 		tweetMenu.add(newTweet);
 		tweetMenu.add(delete);
 
 		// About Menu
 		aboutMenu = new JMenu("About");
 		about = new JMenuItem("About Desktop Tweets");
 		about.addActionListener(this);
 		about.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
 				Event.CTRL_MASK));
 		aboutMenu.add(about);
 
 		// Add To MenuBar
 		menuBar.add(fileMenu);
 		menuBar.add(tweetMenu);
 		menuBar.add(aboutMenu);
 		setJMenuBar(menuBar);
 	}
 
 	private void createTabbedPane() {
 		// UIManager.put("TabbedPane.selected", Color.WHITE);
 		// UIManager.put("TabbedPane.contentAreaColor", Color.WHITE);
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setFocusable(false);
 		tabbedPane.setBackground(Color.WHITE);
 		tabbedPane.setForeground(Color.BLACK);
 		tabbedPane.setFont(new Font("arial", Font.BOLD, 14));
 		tabbedPane.setTabLayoutPolicy(JTabbedPane.CENTER);
 		tabbedPane.addTab("Profile", profilePanel);
 		tabbedPane.addTab("Tweet", tweetPanel);
 		tabbedPane.addTab("Followers", followersPanel);
 		tabbedPane.addTab("Following", followingPanel);
 		add(tabbedPane);
 	}
 
 	/**
 	 * This is where all actions should be delegated and sent to the controller
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		Object source = e.getSource();
 
 		if (source == exit)
 			System.exit(0);
 
 		if (source == newTweet || source == tweetsBtn)
 			tabbedPane.setSelectedComponent(tweetPanel);
 
 		if (source == tweetSubmit) {
 			if (controller.tweet(tweetText.getText())) {
 				updateTweetCount();
 				JOptionPane.showMessageDialog(null, "Status sent.");
 			} else {
 				JOptionPane
 						.showMessageDialog(null, "Status could not be sent.");
 			}
 		}
 
 		if (source == about)
 			JOptionPane.showMessageDialog(null,
 					"This is a desktop twitter application");
 
 		if (source == followersBtn)
 			tabbedPane.setSelectedComponent(followersPanel);
 
 		if (source == followingBtn) {
 			DialogFollowing x = new DialogFollowing(this, new Users(
 					controller.getFollowing()));
 			for (long l : x.getRemoveList()) {
 				controller.unfollow(l);
 			}
 			updateFollowingCount();
 		}
 		// tabbedPane.setSelectedComponent(followingPanel);
 
 		if (source == cancel)
 			tweetText.setText("");
 
 		if (source == tweetShow) {
 			DialogTweets x = new DialogTweets(this,
 					controller.getUserTimeline());
 			for (long l : x.getRemoveList()) {
 				controller.destroyStatus(l);
 			}
 			updateTweetCount();
 
 		}
 	}
 
 	private void updateFollowingCount() {
 		setPlainButton("" + controller.getFriendsCount(), "Following",
 				followingBtn);
 
 	}
 
 	private void updateTweetCount() {
 		setPlainButton("" + controller.getTweetCount(), "Tweets", tweetsBtn);
 		tweetTotal.setText(controller.getTweetCount() + " Tweets");
 
 	}
 
 	public static void main(String[] args) {
 		new TwitterGUI();
 	}
 
 	@Override
 	public void keyPressed(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyReleased(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void keyTyped(KeyEvent e) {
 		// TODO Auto-generated method stub
 
 	}
 }
