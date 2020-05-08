 //BEGIN FILE Lobby.java
 package UI;
 
 import javax.swing.*;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import controller.User;
 import view.LobbyClient;
 
 import java.awt.*;
 import java.awt.event.*;
 import java.util.*;
 import java.util.List;
 
 /**
  * A Network Lobby for TicTacToe games
  * Displays information from the server and allows users to place requests to the servlet
  * such as setting up games with other players.
  * @author Mustache Cash Stash
  * @version 1.0
  */
 //BEGIN CLASS Lobby
 public class Lobby extends JPanel implements ActionListener, ListSelectionListener 
 {
 	public static JFrame masterFrame = new JFrame("Lobby"); // the window container
 	static final long serialVersionUID = 0L; // just because :]
 	public static Color backgroundColor = new Color(0, 0, 0); // color of the background
 	protected static boolean statsWindowIsPopulated = false; //monitor the stats window
 	//private JTextArea display = new JTextArea();
 	private JLabel stats = new JLabel();
 	private JList onlineUsersList;
 	private JList requestedGamesList;
 	private LobbyClient lobbyClient;
 	private List<User> userList;
 
 	/**
 	 * Creates and shows all lobby elements
 	 * @param frame The Frame this lobby is contained in.
 	 */
 	//BEGIN Constructor public Lobby(JFrame frame)
 	public Lobby(JFrame frame)
 	{
 		promptLobbyClient();
 
 		// create a master panel that holds all the elements
 		JPanel masterPanel = new JPanel();
 
 		//create a header, body, and footer
 		JPanel header = new JPanel();
 		JPanel middle = new JPanel();
 		JPanel footer = new JPanel();
 
 		// set background color to black for all elements
 		header.setBackground(backgroundColor);
 		middle.setBackground(backgroundColor);
 		footer.setBackground(backgroundColor);
 		masterPanel.setBackground(backgroundColor);
 		this.setBackground(backgroundColor);
 
 		//treat master panel as a grid that has 3 rows and one column
 		masterPanel.setLayout(new GridLayout(3, 1));
 
 		header.setLayout(new GridLayout(1, 1));
 		middle.setLayout(new GridLayout(1, 1));
 
 		// temporary for testing layout
 
 		//create a list of users for the lobby
 
 		onlineUsersList = new JList();
 		onlineUsersList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		onlineUsersList.addListSelectionListener(this);
 
 		requestedGamesList = new JList();
 		requestedGamesList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		requestedGamesList.addListSelectionListener(this);
 
 		stats = new JLabel("Ready to play!");
 		stats.setForeground(Color.CYAN);
 		stats.setBorder(new EmptyBorder(10, 10, 10, 10));
 
 		// add the list to the header
 		header.add(onlineUsersList);
 		header.add(requestedGamesList);
 
 		middle.add(stats);
 		//treat footer as a grid with 1 column and 3 rows
 		footer.setLayout(new GridLayout(1, 3));
 
 		//create 3 buttons
 		JButton button1 = new JButton("Request Game");
 		JButton button2 = new JButton("Refresh Lobby");
 		JButton button3 = new JButton("Quit Game");
 
 		// set action command to buttons
 		button1.setActionCommand("Request Game");
 		button2.setActionCommand("Refresh Lobby");
 		button3.setActionCommand("Quit Game");
 
 		// set action listeners to button
 		button1.addActionListener(this); 
 		button2.addActionListener(this);
 		button3.addActionListener(this);
 
 		// add the three buttons to the footer
 		footer.add(button1);
 		footer.add(button2);
 		footer.add(button3);
 
 		// add the header, body and footer to the window
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
 		if (frameSize.height > screenSize.height) 
 		{
 			frameSize.height = screenSize.height;
 		}
 		if (frameSize.width > screenSize.width) 
 		{
 			frameSize.width = screenSize.width;
 		}
 		frame.setLocation((screenSize.width - frameSize.width) / 2,
 				(screenSize.height - frameSize.height) / 2); 
 	}
 	//END CONSTRUCTOR public Lobby(JFrame frame)
 
 	@Override
 	//BEGIN METHOD public void actionPerformed(ActionEvent event) 
 	public void actionPerformed(ActionEvent event) 
 	{
 		// TODO Auto-generated method stub
 
 		// get the action of the button
 		String command = event.getActionCommand();
 
 		// if Start Game is clicked
 		if(command.equalsIgnoreCase("Request Game")) 
 		{
 			User otherPlayer = (User) onlineUsersList.getSelectedValue();
 			if(otherPlayer != null)
 			{
 				if(lobbyClient.getUser().equals(otherPlayer))
 				{
 					stats.setText("You cannot challenge yourself.");
 				}
 				else
 				{
 					lobbyClient.requestGame(otherPlayer);
 				}
 			}
 			else
 			{
 				stats.setText("Please select a player if you wish to request a game.");
 			}
 		}
 
 		// if Refresh Lobby is clicked
 		else if (command.equalsIgnoreCase("Refresh Lobby")) 
 		{
 			List<User> newUserList = lobbyClient.getUserList();
 			lobbyClient.getRequestList();
 			if(newUserList != null) 
 			{
 				this.userList = newUserList;
 				onlineUsersList.setListData(newUserList.toArray());
 				stats.setText("Lobby refreshed!");
 			}
 			else
 			{
 				stats.setText("THERE IS NOTHING TO REFRESH");
 			}
 		}
 
 		// if Quit Game is clicked
 		else if (command.equalsIgnoreCase("Quit Game")) 
 		{
 			//quitGame();
 			stats.setText("GET OUTTA HERE");
 		}
 
 		//none of the other buttons have been called
 		else 
 		{
 			//nothing
 		}
 	}
 
 	/**
 	 * 
 	 * @param userList
 	 */
 	//BEGIN METHOD private void refreshLobby(List<User> userList) 
 	private void refreshLobby(List<User> userList) 
 	{
 		// TODO Auto-generated method stub
 
 	}
 	//END METHOD private void refreshLobby(List<User> userList) 
 
 	/**
 	 * Display a message in the Lobby
 	 * @param prefix
 	 * @param e
 	 */
 	//BEGIN METHOD void displayMessage(String prefix, ListSelectionEvent e) 
 	void displayMessage(String prefix, ListSelectionEvent e) 
 	{
 	}
 	//END METHOD void displayMessage(String prefix, ListSelectionEvent e) 
 
 	@Override
 	//BEGIN METHOD public void valueChanged(ListSelectionEvent event) 
 	public void valueChanged(ListSelectionEvent event) 
 	{
 		//String statistics = event.toString();
 		//list.getSelectedValue();
 		stats.setText(((User)onlineUsersList.getSelectedValue()).toString());
 	}
 	//END METHOD public void valueChanged(ListSelectionEvent event) 
 
 	/**
 	 * Checks whether a userName is valid.  Less than 25 characters, no punctuation.
	 * @param userName
 	 * @return if the username is valid.
 	 */
 	//BEGIN METHOD private boolean isAValidPlayerName(String input)  
 	private boolean isAValidPlayerName(String input) 
 	{
 		int nameLength = input.length();
 		if (nameLength > 0 && nameLength < 26)
 		{
 			return !input.contains(".");
 		}
 		return false;
 	}
 	//END METHOD private boolean isAValidPlayerName(String input) 
 
 	/**
 	 * Before the lobby is setup, prompt the player for a Username to request from the servlet
 	 * @return The string the user entered.
 	 */
 	//BEGIN METHOD private String promptPlayerName()
 	private String promptPlayerName()
 	{
 		boolean isValid = false;
 		String name;
 		do
 		{
 			// show a dialog to get the username
 			name = JOptionPane.showInputDialog(null, "Enter the name you would like to use:");
 
 			// if the user clicks on cancel
 			if(name == null)
 			{
 				//We need a username to continue. End the program.
 				System.exit(-1);
 			}
 			else
 			{
 				// If the username is valid, accept it.
 				isValid = isAValidPlayerName(name);
 			}
 		}
 		while(!isValid);
 		return name;
 
 	}
 	//END METHOD private String promptPlayerName()
 
 	/**
 	 * Creates the LobbyClient after asking the user for a server address and a User
 	 */
 	//BEGIN METHOD private LobbyClient promptLobbyClient() 
 	private void promptLobbyClient() 
 	{
 
 		String userName = "";
 		String serverName = "";
 
 		boolean isValid = false;
 
 		do
 		{
 			// show a dialog to get the username
 			serverName = JOptionPane.showInputDialog(null, "Enter the server name:");
 
 			// if the user clicks on cancel
 			if(serverName == null)
 			{
 				//We need a Server to continue. End the program.
 				System.exit(-1);
 			}
 			else
 			{
 				isValid = true;
 			}
 		}
 		while(!isValid);
 
 		int needsAUserName = JOptionPane.showConfirmDialog(null,"Do you have a username on this server?", "Question", JOptionPane.YES_NO_OPTION);
 
 		if (needsAUserName == 1)
 		{	
 			userName = promptPlayerName();
 			this.lobbyClient = new LobbyClient(serverName, userName);
 			boolean serverTest = this.lobbyClient.registerUser();
 			if (!serverTest)
 			{
 				System.exit(-1);
 			}
 			this.lobbyClient.keepAlive();
 		}
 		else
 		{
 			userName = promptPlayerName();
 			isValid = false;
 			String cCode;
 			do
 			{
 				// show a dialog to get the username
 				cCode = JOptionPane.showInputDialog(null, "Enter your Character Code:");
 
 				// if the user clicks on cancel
 				if(cCode == null)
 				{
 					//We need a Character Code to continue. End the program.
 					System.exit(-1);
 				}
 				else
 				{
 					// If the username is valid, accept it.
 					isValid = cCode.matches("\\d{3}");
 				}
 			}
 			while(!isValid);
 			lobbyClient = new LobbyClient(serverName, new User(userName, Integer.parseInt(cCode)));
 
 		}
 	}
 	//END METHOD private LobbyClient promptLobbyClient() 
 
 
 }
 //END CLASS Lobby
 //END FILE Lobby.java
