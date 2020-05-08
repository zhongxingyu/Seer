 package client.gui.menu;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import client.event.Event;
 
 import model.player.*;
 
 import database.*;
 
 /**
  * The StatisticsPanel has information about the logged in user. The information is retrieved from the DB.
  * @author forssenm
  *
  */
 @SuppressWarnings("serial")
 public class StatisticsPanel extends JPanel implements ActionListener,
 		ListSelectionListener, client.event.EventHandler, IDBGame {
 	DatabaseCommunicator dbc = DatabaseCommunicator.getInstance();
 
 	private JLabel setThisUserName;
 	private JLabel setThisFirstName;
 	private JLabel setThisLastName;
 	private JLabel setThisPlayedGames;
 	private JLabel setThisWonGames;
 	
 	/*
 	private JList<Integer> listWithGames;
 	private DefaultListModel<Integer> listWithGamesModel;
 	
 	private JList<String> selectedGame;
 	private DefaultListModel<String> selectedGameModel;
 	 */	
 	// TODO Modified the following part from the above Code.
 	// Couldnt compile on mac. Also see below TODOS
 
 	
 	private JList listWithGames;
 	private DefaultListModel listWithGamesModel;
 	
 	private JList selectedGame;
 	private DefaultListModel selectedGameModel;
 	
 	private int frameHeight = P.INSTANCE.getFrameHeight();
 	private int margin = P.INSTANCE.getMarginSize();
 	private int buttonHeight = P.INSTANCE.getButtonHeight();
 	private int buttonWidth = P.INSTANCE.getButtonWidth();
 
 	private JButton statisticsBackButton;
 	
 	/**
 	 * Creates the application.
 	 */
 	public StatisticsPanel() {
 		init();
 		client.event.EventBus.register(this);
 	}
 
 	@Override
 	public void onEvent(client.event.Event evt) {
 		if(evt.getTag().equals(Event.Tag.PUBLISH_ACCOUNT_INFORMATION)) {
 			Account acc = (Account)evt.getValue();
 			setThisUserName.setText(acc.getUserName());
 			setThisFirstName.setText(acc.getFirstName());
 			setThisLastName.setText(acc.getLastName());
 			setThisWonGames.setText(Integer.toString(loadNbrOfWonGames(acc.getUserName())));
 			setThisPlayedGames.setText(Integer.toString(loadNbrOfPlayedGames(acc.getUserName())));
 		}
 
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getSource() == statisticsBackButton) {
 			client.event.EventBus.publish(new client.event.Event(client.event.Event.Tag.GO_TO_MAIN, 1));
 		}
 	}
 
 	private void init() {
 		this.setLayout(null);
 		this.setBackground(P.INSTANCE.getBackground());
 
 		JLabel userName = new JLabel("User name");
 		userName.setBounds(200, 102, 200, 20);
 		userName.setFont(P.INSTANCE.getBoldLabelFont());
 		this.add(userName);
 		
 		//TODO set the different values, get them from where?
 		setThisUserName = new JLabel("Get the name from where?");
 		setThisUserName.setBounds(200, 127, 200, 20);
 		setThisUserName.setFont(P.INSTANCE.getLabelFont());
 		this.add(setThisUserName);
 		
 		JLabel firstName = new JLabel("First name");
 		firstName.setBounds(200, 152, 200, 20);
 		firstName.setFont(P.INSTANCE.getBoldLabelFont());
 		this.add(firstName);
 		
 		setThisFirstName = new JLabel("Get the name from where?");
 		setThisFirstName.setBounds(200, 177, 200, 20);
 		setThisFirstName.setFont(P.INSTANCE.getLabelFont());
 		this.add(setThisFirstName);
 		
 		JLabel lastName = new JLabel("Last name");
 		lastName.setBounds(200, 202, 200, 20);
 		lastName.setFont(P.INSTANCE.getBoldLabelFont());
 		this.add(lastName);
 		
 		setThisLastName = new JLabel("Get the name from where?");
 		setThisLastName.setBounds(200, 227, 200, 20);
 		setThisLastName.setFont(P.INSTANCE.getLabelFont());
 		this.add(setThisLastName);
 		
 		JLabel playedGames = new JLabel("Number of played games");
 		playedGames.setBounds(200, 252, 230, 20);
 		playedGames.setFont(P.INSTANCE.getBoldLabelFont());
 		this.add(playedGames);
 		
 		setThisPlayedGames = new JLabel("Get the number of games from where?");
 		setThisPlayedGames.setBounds(200, 277, 200, 20);
 		setThisPlayedGames.setFont(P.INSTANCE.getLabelFont());
 		this.add(setThisPlayedGames);
 		
 		JLabel wonGames = new JLabel("Number of won games");
 		wonGames.setBounds(200, 302, 200, 20);
 		wonGames.setFont(P.INSTANCE.getBoldLabelFont());
 		this.add(wonGames);
 		
 		setThisWonGames = new JLabel(("Get number of won games from where?"));
 		setThisWonGames.setBounds(200, 327, 200, 20);
 		setThisWonGames.setFont(P.INSTANCE.getLabelFont());
 		this.add(setThisWonGames);
 		
		listWithGamesModel = new DefaultListModel();
 		listWithGames = new JList(listWithGamesModel);
 		listWithGames.setBounds(500, 102, 400, 245);
 		listWithGames.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		List<Integer> tmp = loadPlayedGames(setThisUserName.getText());
 		for(Integer i : tmp) {
 			listWithGamesModel.addElement(i);
 		}
 		listWithGames.addListSelectionListener(this);
 		this.add(listWithGames);
 		
		selectedGameModel = new DefaultListModel();
 		selectedGame = new JList(selectedGameModel);
 		selectedGame.setBounds(500, 372, 400, 245);
 		this.add(selectedGame);
 		
 		statisticsBackButton = new JButton("Back");
 		statisticsBackButton.setFont(P.INSTANCE.getLabelFont());
 		statisticsBackButton.setBounds(margin, frameHeight - buttonHeight 
 				- 2*margin, buttonWidth, buttonHeight);
 		statisticsBackButton.addActionListener(this);
 		this.add(statisticsBackButton);
 	}
 
 	@Override
 	public void saveGame(int gameID, String date) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void savePlacement(String gameID, Player player, int placement) {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public Map<Integer, String> loadGamePlacements(String gameID) {
 		Connection conn = dbc.getConnection();
 		Statement myStmt;
 		try {
 			myStmt = conn.createStatement();
 			ResultSet rs = myStmt
 					.executeQuery("SELECT * FROM PlayedGames WHERE gameID = '"
 							+ gameID + "'");
 			Map<Integer, String> placements = new TreeMap<Integer, String>();
 			while (rs.next()) {
 				String placement = rs.getString(3);
 				int tmp = Integer.parseInt(placement);
 				String player = rs.getString(2);
 
 				placements.put(tmp, player);
 			}
 			return placements;
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public int loadNbrOfWonGames(String userName) {
 		Connection conn = dbc.getConnection();
 		Statement myStmt;
 		try {
 			myStmt = conn.createStatement();
 			ResultSet rs = myStmt
 					.executeQuery("SELECT * FROM WonGames WHERE player = '"
 							+ userName + "'");
 			if (rs.next()) {
 				String nbrOfWonGames = rs.getString(2);
 				int tmp = Integer.parseInt(nbrOfWonGames);
 				return tmp;
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	@Override
 	public int calculateGameID() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public int loadNbrOfPlayedGames(String userName) {
 		Connection conn = dbc.getConnection();
 		Statement myStmt;
 		try {
 			myStmt = conn.createStatement();
 			ResultSet rs = myStmt
 					.executeQuery("SELECT player, COUNT(*) AS " +
 							"nbrOfPlayedGames FROM PlayedGames WHERE player = '"
 							+ userName + "' GROUP BY player");
 			if (rs.next()) {
 				String nbrOfPlayedGames = rs.getString(2);
 				int tmp = Integer.parseInt(nbrOfPlayedGames);
 				return tmp;
 			}
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		return 0;
 	}
 
 	@Override
 	public List<Integer> loadPlayedGames(String userName) {
 		Connection conn = dbc.getConnection();
 		Statement myStmt;
 		try {
 			myStmt = conn.createStatement();
 			ResultSet rs = myStmt
 					.executeQuery("SELECT gameID FROM PlayedGames WHERE player = '"
 							+ userName + "'");
 			List<Integer> playedGames = new LinkedList<Integer>();
 			while (rs.next()) {
 				String gameID = rs.getString(1);
 				int tmp = Integer.parseInt(gameID);
 				playedGames.add(tmp);
 			}
 			return playedGames;
 		} catch (SQLException e) {
 			System.out.println(e.getMessage());
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	@Override
 	public void valueChanged(ListSelectionEvent e) {
 		String tmp = listWithGames.getSelectedValue().toString();
 		Map<Integer, String> othertmp = loadGamePlacements(tmp);
 		Set<Integer> s = othertmp.keySet();
 		for(Integer i : s) {
 			selectedGameModel.addElement(i + ". " + othertmp.get(i));
 		}
 	}
 }
