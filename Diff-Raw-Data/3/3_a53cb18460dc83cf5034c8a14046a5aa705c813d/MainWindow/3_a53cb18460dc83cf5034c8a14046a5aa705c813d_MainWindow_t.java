 //BEGIN FILE MainWindow.java
 package UI;
 
 import javax.swing.*;
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 
 /**
  * A total clusterfuck
  * @author Mustache Cash Stash
  * @version 1.0
  */
 //BEGIN CLASS MainWindow
 public class MainWindow extends JPanel implements ActionListener
 {
 	public static JFrame masterFrame = new JFrame("TicTacToe"); // the window it self
 	public static ArrayList<String> myscores = new ArrayList<String>(); // an arrayList for scores
 	static final long serialVersionUID = 0L; // to shut up the stupid Eclipse
 	public static Color backgroundColor = new Color(0,0,0); // the black color of background
 	protected static boolean scoresWindowIsUp = false; // monitor the scores window
 
 	/*
 	 *  MainWindow
 	 *  
 	 *  Create and show all elements
 	 */
 	public MainWindow(JFrame frame)
 	{
 		// create a master panel that holds all elements
 		JPanel masterPanel = new JPanel();
 
 		// create a header, body and footer
 		JPanel header = new JPanel();
 		JPanel middle = new JPanel();
 		JPanel footer = new JPanel();
 
 		// set background color to black for all elements
 		header.setBackground(backgroundColor);
 		middle.setBackground(backgroundColor);
 		footer.setBackground(backgroundColor);
 		masterPanel.setBackground(backgroundColor);
 		this.setBackground(backgroundColor);
 
 		// treat master panel as a grid that has 3 rows and one column
 		masterPanel.setLayout(new GridLayout(3,1));
 
 		// treat the body as a grid that has 3 rows and one column
 		middle.setLayout(new GridLayout(3,1));
 
 		// create 2 images and place them in header and footer
 		header.add(new JLabel(new ImageIcon("images/header.png")));
 		footer.add(new JLabel(new ImageIcon("images/footer.png")));
 
 		// create 3 buttons
 		JButton button1 = new JButton("Network Game");
 		JButton button2 = new JButton("Local Game");
 		JButton button3 = new JButton("Quit");
 
 		// set action command to buttons
 		button1.setActionCommand("Network Game");
 		button2.setActionCommand("Local Game");
 		button3.setActionCommand("Quit");
 
 		// set action listeners to button
 		button1.addActionListener(this); 
 		button2.addActionListener(this);
 		button3.addActionListener(this);
 
 		// add the 3 buttons to body
 		middle.add(button1);
 		middle.add(button2);
 		middle.add(button3);
 
 		// add the header, body and footer to master panel
 		masterPanel.add(header);
 		masterPanel.add(middle);
 		masterPanel.add(footer);
 
 		// add masterPanel to the window
 		add(masterPanel);
 
 		// set the size of the window
 		frame.setSize(400, 460);	 	
 
 		// Place the window in the middle of the screen
 		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
 		Dimension frameSize = frame.getSize();
 		if (frameSize.height > screenSize.height) {
 			frameSize.height = screenSize.height;
 		}
 		if (frameSize.width > screenSize.width) {
 			frameSize.width = screenSize.width;
 		}
 		frame.setLocation((screenSize.width - frameSize.width) / 2,
 				(screenSize.height - frameSize.height) / 2);         
 
 	}
 
 	/**
 	 * actionPerformed
 	 * 
 	 * Listens to actions and act accordingly
 	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
 	 */
 	@Override
 	public void actionPerformed(ActionEvent event)
 	{
 		// get the action of the button
 		String command = event.getActionCommand();
 
 		// If the Network Game button is clicked.
 		if(command.equalsIgnoreCase("Network Game")) 
 		{
 			networkGamePrompts();
 		}
 
 		// If the Local Game button is clicked.
 		else if (command.equalsIgnoreCase("Local Game")) 
 		{
 			localGamePrompts();
 		}
 
 		//None of the above; must have clicked quit.
 		else
 		{
 			// ask user for confirmation
 			int confirmation = JOptionPane.showConfirmDialog(null,"Do you want to Quit?", "input", JOptionPane.YES_NO_OPTION);
 
 			// if confirmed, quit the application
 			if(confirmation==0)
 			{
 				System.exit(0);
 			}
 
 		}
 	}
 
 	private void localGamePrompts() 
 	{
 		String user1 = "";
 		String user2 = "";
 
 		// we assume the username is valid 
 		boolean isValid = false;
 
 		// keep asking the user for a valid username
 		while(!isValid)
 		{
 			// show a dialog to get the username
 			user1 = JOptionPane.showInputDialog(null, "Player 1 Name:");
 
 
 			// if the user clicks on cancel
 			if(user1 == null)
 			{
 				// simply stop asking for username and break the while loop 
 				isValid = false;
 				break;
 			}
 			else
 			{
 				// If the username is valid, accept it.
 				isValid = isAValidUserName(user1);
 			}
 		}
		
		isValid = false;
		
 		while(!isValid)
 		{
 			// show a dialog to get the username
 			user2 = JOptionPane.showInputDialog(null, "Player 2 Name:");
 
 			// if the user click on cancel
 			if(user2 == null)
 			{
 				// simply stop asking for username and break the while loop 
 				isValid = false;
 				break;
 			}
 			else
 			{
 				// If the username is valid, accept it.
 				isValid = isAValidUserName(user2);
 
 				// check if username is already used
 				if(user2.equals(user1))
 				{
 					isValid = false;							
 					JOptionPane.showMessageDialog(null, "The name you have choosen is already being used by another player");
 				}
 			}
 		}
 		// show game board if allowed
 		if(isValid) {
 			new GameBoardDisplay(user1, user2, "Dual");
 			masterFrame.dispose();
 		}
 
 	}
 
 	/**
 	 * Checks whether a userName is valid.  Less than 25 characters, no punctuation.
 	 * @param userName
 	 * @return if the username is valid.
 	 */
 	//BEGIN METHOD private boolean isAValidUserName(String input) 
 	private boolean isAValidUserName(String input) 
 	{
 		int nameLength = input.length();
 		if (nameLength > 0 && nameLength < 26)
 		{
 			return !input.contains(".");
 		}
 		return false;
 	}
 	//END METHOD private boolean isAValidUserName(String input) 
 
 	//BEGIN METHOD private void networkGamePrompts() 
 	private void networkGamePrompts() 
 	{
 
 		// create a content pane
 		final Lobby contentPane = new Lobby(masterFrame);
 
 		// set closing option for this window
 		masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// set opaque
 		contentPane.setOpaque(true);
 
 		// attach content pane to the master frame
 		masterFrame.setContentPane(contentPane);
 
 		// this window will be not resizable
 		masterFrame.setResizable(false);
 
 		// pack the window (styling issue)
 		masterFrame.pack();
 
 		// show the window
 		masterFrame.setVisible(true);
 	}
 	//END METHOD private void networkGamePrompts() 
 
 	/**
 	 * Notifies the main window as soon as the score window is closed.
 	 */
 	//BEGIN METHOD public static void scoresWindowIsClosed()
 	public static void scoresWindowIsClosed()
 	{
 		scoresWindowIsUp = false;
 	}
 	//END METHOD public static void scoresWindowIsClosed()
 
 	/**
 	 * Called by the main function to display the main window.
 	 */
 	//BEGIN METHOD public static void showMainWindow() 
 	public static void showMainWindow() 
 	{
 		// create a content pane
 		final MainWindow contentPane = new MainWindow(masterFrame);
 
 		// set closing option for this window
 		masterFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		// set opaque
 		contentPane.setOpaque(true);
 
 		// attach content pane to the master frame
 		masterFrame.setContentPane(contentPane);
 
 		// this window will be not resizable
 		masterFrame.setResizable(false);
 
 		// pack the window (styling issue)
 		masterFrame.pack();
 
 		// show the window
 		masterFrame.setVisible(true);
 	}
 	//END METHOD public static void showMainWindow() 
 
 }
 //END CLASS MainWindow
 //END FILE MainWindow.java
