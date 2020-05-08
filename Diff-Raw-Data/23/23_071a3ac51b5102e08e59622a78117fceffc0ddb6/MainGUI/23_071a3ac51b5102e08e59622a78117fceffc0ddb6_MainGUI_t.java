 package GUI;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.GridBagLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyListener;
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.util.Scanner;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.UIManager;
 import javax.swing.UIManager.LookAndFeelInfo;
 import javax.swing.border.LineBorder;
 
 import main.*;
 
 public class MainGUI extends JFrame {
 
 	private KeyListener reader; 
 	private AccessTracker tracker;
 	private User currentUser;
 	private JPanel mainPanel; // Basically The Entire Panel
 	private JPanel headerBar;
 	private JPanel footerBar;
 	private JPanel mainContentPanel; // The content Panel (between header and footer)
 	private Clock time;
 	private Font headerFont;
 	private GridBagConstraints c = new GridBagConstraints();
 	private static int MAX_ERROR_COUNT = 3;
 	private JButton homeButton;
 	private JButton helpButton;
 	private JButton aboutButton;
 	private JButton close;
 	private JFrame frame;
 	private ButtonListener buttonListener;
 	private final String homeButtonPath = "/Images/homeButton.jpg";
 	private final String mPath = "/Images/minesM.jpg";
 	private final String bannerPath = "/Images/banner.jpg";
 	private JLabel minesMlabel;
 	private JLabel bannerLabel;
 	
 	
 	private Color borderColor = Color.black;
 	private Font miniFont = new Font("SansSerif", Font.BOLD, 18);
 
 	public MainGUI() {
 		
 		this.getContentPane().setBackground(Color.white);
 		reader = new InputReader(this);
 		mainPanel = new SwipeCardPanel();
 		buttonListener = new ButtonListener();
 		tracker = Driver.getAccessTracker();
 
 		addKeyListener(reader);
 		setFocusable(true);
 		
 		setLayout(new GridBagLayout());
 		setup();
 
 		ImageIcon mIcon = GUI.createImageIcon(mPath);
 		minesMlabel = new JLabel(mIcon);
 
 		ImageIcon bannerIcon = GUI.createImageIcon(bannerPath);
 		bannerLabel = new JLabel(bannerIcon);
 		
 		ImageIcon homeButtonIcon = GUI.createImageIcon(homeButtonPath);
 		homeButton = new JButton(homeButtonIcon);
 		homeButton.setBackground(null);
 		homeButton.setBorder(new LineBorder(Color.white, 4));
         
 		homeButton.addActionListener(buttonListener);
 
 		helpButton = new JButton("Help");
 		helpButton.addActionListener(buttonListener);
 		helpButton.setFont(miniFont);
 		
 		aboutButton = new JButton("About");
 		aboutButton.addActionListener(buttonListener);
 		aboutButton.setFont(miniFont);
 		
 		add(mainPanel);
 		setVisible(true);
 	}
 
 	public void setup() {
 		
 //		TODO sets the cursor to invisible (For touch-screen purposes)
 //		Toolkit tk = Toolkit.getDefaultToolkit();
 //		BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
 //		setCursor(tk.createCustomCursor(image, new Point(0,0), "blank"));
 
 		//sets the screen to full screen		
 		setExtendedState(MAXIMIZED_BOTH);
 		setUndecorated(true);
 		
 		if (Validator.isUnixOS()){
 			setResizable(true);
 		} else {
 			setResizable(false);
 		}
 
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// TODO disable Alt+F4
 		//setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		// Set up the look and feel of the frame/panels
 		try {
 			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
 				if ("Nimbus".equals(info.getName())) {
 					UIManager.setLookAndFeel(info.getClassName());
 					break;
 				}
 			}
 		} catch (Exception e) {
 			// use default look and feel
 		}
 	}
 	
 	public void dispose() {
 		this.dispose();
 	}
 	
 	public void close() {
 		this.close();
 	}
 
 	// Returns the user to the "Swipe Blastercard" screen
 	public void restart() {
 
 		Driver.isLogInScreen = true;
 		remove(headerBar);
 		remove(footerBar);
 		remove(mainContentPanel);
 		add(mainPanel);
 		setVisible(true);
 		repaint();
 
 	}
 	
 	// This function does most of the work to set up the homescreen for whichever
 	// user is currently logged in (whether they are an Admin, SystemAdmin, or User)
 	public void processHomeScreen(User currentUser) {
 		
 		// Create header and footer bars
 		headerFont = new Font("SansSerif", Font.BOLD, 34);
		headerBar = new JPanel(new GridLayout(1, 3));
 		headerBar.setBorder(new LineBorder(borderColor, 4));
 		headerBar.setBackground(Color.white);
 		
 		footerBar = new JPanel(new GridBagLayout());
 		footerBar.setBackground(Color.white);
 		footerBar.setBorder(new LineBorder(borderColor, 4));
 		
 		JPanel aboutButtonPanel = new JPanel(new GridBagLayout());
 		aboutButtonPanel.setBackground(Color.WHITE);
 		aboutButtonPanel.add(aboutButton);
 		
 		JPanel helpButtonPanel = new JPanel(new GridBagLayout());
 		helpButtonPanel.setBackground(Color.WHITE);
 		helpButtonPanel.add(helpButton);
 
 		GridBagConstraints footerC = new GridBagConstraints();
 		
 		footerC.fill = GridBagConstraints.NONE;
 		footerC.gridx = 0;
 		footerC.gridy = 0;
 		footerC.weightx = 0.05;
 		footerBar.add(aboutButtonPanel, footerC);
 		
 		footerC.fill = GridBagConstraints.HORIZONTAL;
 		footerC.gridx = 1;
 		footerC.weightx = 0.9;
 		footerBar.add(bannerLabel, footerC);
 		
 		footerC.fill = GridBagConstraints.NONE;
 		footerC.gridx = 2;
 		footerC.weightx = 0.05;
 		footerBar.add(helpButtonPanel, footerC);
 		
 		String userName = currentUser.getFirstName() + " " + currentUser.getLastName();
 		
 		// Add the user's name and the clock
 		JLabel nameLabel = new JLabel(userName);
 		nameLabel.setBackground(Color.white);
		
 		time = new Clock(headerFont);
 
 		nameLabel.setHorizontalAlignment(JLabel.LEFT);
 		time.setHorizontalAlignment(JLabel.RIGHT);
 
 		nameLabel.setFont(headerFont);
 
 		JPanel leftPanel = new JPanel(new GridBagLayout());
 		leftPanel.setBackground(Color.white);
 		
 		// Set up the constraints to format everything correctly
 		c.ipadx = 5;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 0.2;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		leftPanel.add(homeButton, c);
 		
 		c.gridx = 2;
 		c.gridy = 0;
 		c.weightx = 0.8;
 		c.fill = GridBagConstraints.HORIZONTAL;
 		leftPanel.add(nameLabel, c);
 
		headerBar.add(leftPanel);
		headerBar.add(minesMlabel);
		headerBar.add(time);
 		
 		c.ipadx = 0;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1;
 		c.weighty = 0.05;
 		c.fill = GridBagConstraints.BOTH;
 		add(headerBar, c);
 		
 		// This is where we create and add the main panel with all the relevant information.
 		// This panel will be switched in and out for other panels all throughout the runtime
 		// of the program. These panels will be stored in a stack so that the back button will
 		// function correctly.		
 		mainContentPanel = new JPanel();
 		mainContentPanel.setBackground(Color.white);
 
 		if ( currentUser.isAdmin() ) {
 			if ( ((Administrator) currentUser).isSystemAdmin() ) {
 				mainContentPanel = new SystemAdminGUI(currentUser);
 			} else {
 				mainContentPanel = new AdminGUI(currentUser);
 			}
 		} 	else {
 			mainContentPanel = new UserGUI(currentUser);
 		}
 		
 		mainContentPanel.setBorder(new LineBorder(borderColor, 4));
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weighty = 0.9;
 		c.fill = GridBagConstraints.BOTH;
 		add(mainContentPanel, c);
 		
 		c.ipady = 20;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weighty = 0.05;
 		c.fill = GridBagConstraints.BOTH;
 		add(footerBar, c);
 
 		setVisible(true);
 		c.ipady = 0;
 
 	}
 
 	public void handleInput() {
 
 		InputReader inReader = (InputReader) reader;
 
 		try {
 			inReader.strip();			
 			if (inReader.getCWID() == null) {
 				return;
 			}
 			if (Validator.isValidCWID(inReader.getCWID())) {
 				login(inReader.getCWID());
 			}
 
 		} catch (InputReaderException e) {
 
 			if (InputReader.getErrorCount() < MAX_ERROR_COUNT) {
 				JOptionPane.showMessageDialog(this, e.getMessage());
 				return;
 			}
 
 			String input;
 
 			if (InputReader.getErrorCount() == 999) {
 				input = JOptionPane.showInputDialog("Enter CWID.");
 			} else {
 				input = JOptionPane.showInputDialog("An Error has occurred. Please enter your CWID.");
 			}
 			if (input == null) {
 				InputReader.resetErrorCount();
 				return;
 			}
 
 			int tries = 0;
 			while (!Validator.isValidCWID(input) && tries < MAX_ERROR_COUNT) {
 				input = JOptionPane.showInputDialog("Not a valid CWID. Please try again. ");
 				if (input == null){
 					InputReader.resetErrorCount();
 					return;
 				}
 				++tries;
 			}
 
 			InputReader.resetErrorCount();
 			if (Validator.isValidCWID(input)){
 				login(input);
 			}
 		}
 	}
 
 	public void login(String CWID){
 		currentUser = tracker.processLogIn(CWID);
 		if ( currentUser != null ) {
 			remove(mainPanel);
 			processHomeScreen(currentUser);
 		}
 	}
 
 	public void enterPressed() {
 		handleInput();
 	}
 
 	public User getCurrentUser() {
 		return currentUser;
 	}
 
 	private void displayTextFile(String title, String file) {
 		if (frame != null) 
 			frame.dispose();
 		frame = new JFrame();
 		frame.setUndecorated(true);
 		frame.setTitle(title);
 		frame.setSize(getMinimumSize());
 		frame.setLocationRelativeTo(null);
 		JPanel all = new JPanel();
 		all.setLayout(new GridBagLayout());
 		all.setBorder(new LineBorder(borderColor, 4));
 		JPanel panel = new JPanel();
 		panel.setLayout(new GridLayout(0, 1));
 		JScrollPane scroller = new JScrollPane(panel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
 		Scanner in;
 		try {
 			in = new Scanner(new FileReader(file));
 			while (in.hasNextLine()) {
 				JLabel label = new JLabel();
 				label.setText(in.nextLine());
 				label.setFont(miniFont);
 				panel.add(label);
 			}			
 		} catch (FileNotFoundException e) {
 			System.out.println("Documentation Could Not Be Found");
 		}
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weighty = 0.9;
 		c.fill = GridBagConstraints.BOTH;
 		all.add(scroller, c);
 		
 		close = new JButton("Close");
 		close.addActionListener(buttonListener);
 		close.setFont(miniFont);
 
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weighty = 0.1;
 		c.fill = GridBagConstraints.NONE;
 		all.add(close, c);
 		
 		frame.add(all);
 		frame.setVisible(true);
 	}
 	
 	public static void showMessage(String message) {
 		JOptionPane.showMessageDialog(Driver.getMainGui(), message);
 	}
 	
 	public static boolean confirm(String message) {
 		if ( JOptionPane.showConfirmDialog(Driver.getMainGui(), message) == JOptionPane.YES_OPTION ) {
 			return true;
 		}
 		return false;
 	}
 
 	private class ButtonListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if ( e.getSource() == homeButton ) {
 				remove(mainContentPanel);
 				if ( currentUser.isAdmin() ) {
 					if ( ((Administrator) currentUser).isSystemAdmin() ) {
 						mainContentPanel = new SystemAdminGUI(currentUser);
 					} else {
 						mainContentPanel = new AdminGUI(currentUser);
 					}
 				} 	else {
 					mainContentPanel = new UserGUI(currentUser);
 				}
 				mainContentPanel.setBorder(new LineBorder(borderColor, 4));
 				c.gridx = 0;
 				c.gridy = 1;
 				c.weighty = 0.9;
 				c.fill = GridBagConstraints.BOTH;
 				add(mainContentPanel, c);
 				
 			} else if (e.getSource() == helpButton) {
 				displayTextFile("Help", "help.txt");
 			} else if (e.getSource() == aboutButton) {
 				displayTextFile("About", "about.txt");
 			} else if (e.getSource() == close) {
 				frame.setVisible(false);
 				frame.dispose();
 				repaint();
 			}
 		}
 	}
 	
 }
