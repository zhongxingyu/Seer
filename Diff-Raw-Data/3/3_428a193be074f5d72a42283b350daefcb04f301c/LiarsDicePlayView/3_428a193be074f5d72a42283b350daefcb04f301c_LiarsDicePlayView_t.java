 package gui;
 
 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 import java.util.concurrent.TimeUnit;
 
 import javax.swing.*;
 import javax.swing.border.Border;
 import javax.swing.border.TitledBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import model.Facade;
 import model.Game;
 import model.Player;
 import model.liarsDice.HumanController;
 import model.liarsDice.LiarsDiceGameFactory;
 import model.liarsDice.LiarsDiceView;
 import model.liarsDice.gameInfo.GameHistory;
 import model.liarsDice.gameInfo.GameInfo;
 import model.liarsDice.gameInfo.PlayerInfo;
 import model.liarsDice.gameInfo.Result;
 import model.liarsDice.gameInfo.Round;
 import model.liarsDice.gameInfo.Turn;
 import model.liarsDice.gameLogic.Bid;
 import model.liarsDice.gameLogic.Challenge;
 import model.liarsDice.gameLogic.Decision;
 import model.liarsDice.gameLogic.Die;
 import model.liarsDice.gameLogic.LiarsDiceGame;
 import model.liarsDice.gameLogic.LiarsDicePlayer;
 
 @SuppressWarnings("serial")
 public class LiarsDicePlayView extends JPanel implements LiarsDiceView {
 
     private Facade facade;
     private HumanController humanController; 
     private Vector<Player> players;
     private int numPlayers;
     
     private Thread gameThread;
     
     private GameInfo oldGameInfo; 
     
     private GridLayout layout;
     private PlayerPanel playerPanel1, playerPanel2, playerPanel3, humanPanel;
     private JPanel player1InfoPanel, player2InfoPanel, player3InfoPanel, humanInputPanel;
     private JLabel player1InfoLabel, player2InfoLabel, player3InfoLabel, player1Decision, player2Decision, player3Decision;
     private JButton startGame, nextRound, humanBid, humanChallenge;
     private JTextField bidQuantity;
     private JRadioButton rb2, rb3, rb4, rb5, rb6;
 
     private JTextArea history; // Text area
     private JScrollPane scrollPane; // Scroll pane for text area
     
     private JComboBox[] botPickers;
     
     private BidListener bidListener;
     
     private Color tablegreen = new Color(80, 200, 120); //paris green
     //private Color tablegreen = new Color(8, 138, 75)); //internet poker table
     private boolean coloredGUI = true, nimbus = false; //set to false if don't want color
     
 	private boolean viewDisabled = false;
 
 	public LiarsDicePlayView(Facade f){
 		facade = f;
 
 		oldGameInfo = new GameInfo();
 		oldGameInfo.getGameHistory().addNewRound();
 		
 		botPickers = new JComboBox[3];
 
 		layout = new GridLayout(3,3);
 		setLayout(layout);
 		
 		player2InfoPanel = new JPanel();
 		if(coloredGUI) player2InfoPanel.setBackground(tablegreen);
 		player2InfoPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		player2InfoLabel = new JLabel("Last Decision:  ");
 		player2Decision = new JLabel();
 		player2InfoPanel.add(player2InfoLabel);
 		player2InfoPanel.add(player2Decision);
 		add(player2InfoPanel, 0);
 
 		playerPanel2 = new PlayerPanel(1);
 		if(coloredGUI) playerPanel2.setBackground(tablegreen);
 		add(playerPanel2, 1);
 
 		player3InfoPanel = new JPanel();
 		if(coloredGUI) player3InfoPanel.setBackground(tablegreen);
 		player3InfoPanel.setLayout(new BorderLayout());
 		player3InfoLabel = new JLabel("Last Decision:  ");
 		player3Decision = new JLabel();
 		JPanel p3Container = new JPanel();
 		if(coloredGUI) p3Container.setBackground(tablegreen);
 		p3Container.setLayout(new FlowLayout(FlowLayout.RIGHT));
 		p3Container.add(player3InfoLabel);
 		p3Container.add(player3Decision);
 		player3InfoPanel.add(p3Container, BorderLayout.SOUTH);
 		add(player3InfoPanel, 2);
 		
 		playerPanel1 = new PlayerPanel(0);
 		if(coloredGUI) playerPanel1.setBackground(tablegreen);
 		add(playerPanel1, 3);
 		
 		history = new JTextArea();
 		if(coloredGUI) history.setBackground(tablegreen);
 		history.setLineWrap(true);
 		history.setEditable(false);
 		scrollPane = new JScrollPane(history);
 //		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		add(scrollPane, 4);
 
 		playerPanel3 = new PlayerPanel(2);
 		if(coloredGUI) playerPanel3.setBackground(tablegreen);
 		add(playerPanel3, 5);
 		
 		player1InfoPanel = new JPanel();
 		if(coloredGUI) player1InfoPanel.setBackground(tablegreen);
 		player1InfoPanel.setLayout(new FlowLayout(FlowLayout.LEFT));
 		player1InfoLabel = new JLabel("Last Decision:  ");
 		player1Decision = new JLabel();
 		player1InfoPanel.add(player1InfoLabel);
 		player1InfoPanel.add(player1Decision);
 		add(player1InfoPanel, 6);
 
 		humanPanel = new PlayerPanel(-1);
 		if(coloredGUI) humanPanel.setBackground(tablegreen);
 		add(humanPanel, 7);
 
 		bidListener = new BidListener();
 		humanInputPanel = new JPanel();
 		if(coloredGUI) humanInputPanel.setBackground(tablegreen);
 		GridLayout g = new GridLayout(2,1);
 		humanInputPanel.setLayout(g);
 		JPanel wrapperPanel = new JPanel();
 		if(coloredGUI) wrapperPanel.setBackground(tablegreen);
 		wrapperPanel.setLayout(new BorderLayout());
 		JPanel bidPanel = new JPanel();
 		if(coloredGUI) bidPanel.setBackground(tablegreen);
 		bidQuantity = new JTextField("", 4);
 		bidQuantity.getDocument().addDocumentListener(bidListener);
 		bidPanel.add(bidQuantity);
 		//add radio buttons, etc
 		rb2 = new JRadioButton(new ImageIcon("images/small/die2.png"));
 		rb2.addActionListener(new RadioButtonListener());
 		if(coloredGUI) rb2.setBackground(tablegreen);
 //		JLabel rbp2 = new JLabel(new ImageIcon("images/small/die2.png"));
 		rb3 = new JRadioButton(new ImageIcon("images/small/die3.png"));
 		rb3.addActionListener(new RadioButtonListener());
 		if(coloredGUI) rb3.setBackground(tablegreen);
 //		JLabel rbp3 = new JLabel(new ImageIcon("images/small/die3.png"));
 		rb4 = new JRadioButton(new ImageIcon("images/small/die4.png"));
 		rb4.addActionListener(new RadioButtonListener());
 		if(coloredGUI) rb4.setBackground(tablegreen);
 //		JLabel rbp4 = new JLabel(new ImageIcon("images/small/die4.png"));
 		rb5 = new JRadioButton(new ImageIcon("images/small/die5.png"));
 		rb5.addActionListener(new RadioButtonListener());
 		if(coloredGUI) rb5.setBackground(tablegreen);
 //		JLabel rbp5 = new JLabel(new ImageIcon("images/small/die5.png"));
 		rb6 = new JRadioButton(new ImageIcon("images/small/die6.png"));
 		rb6.addActionListener(new RadioButtonListener());
 		if(coloredGUI) rb6.setBackground(tablegreen);
 		//JLabel rbp6 = new JLabel(new ImageIcon("images/small/die6.png"));
 		rb2.addActionListener(bidListener);
 		rb3.addActionListener(bidListener);
 		rb4.addActionListener(bidListener);
 		rb5.addActionListener(bidListener);
 		rb6.addActionListener(bidListener);
 	    //Group the radio buttons.
 	    ButtonGroup group = new ButtonGroup();
 	    group.add(rb2);
 	    group.add(rb3);
 	    group.add(rb4);
 	    group.add(rb5);
 	    group.add(rb6);
 	    JPanel radioPanel = new JPanel();
 	    if(coloredGUI) radioPanel.setBackground(tablegreen);
 	    radioPanel.setLayout(new GridLayout(1, 10));
 	    radioPanel.add(rb2);
 //	    radioPanel.add(rbp2);
 	    radioPanel.add(rb3);
 //	    radioPanel.add(rbp3);
 	    radioPanel.add(rb4);
 //	    radioPanel.add(rbp4);
 	    radioPanel.add(rb5);
 //	    radioPanel.add(rbp5);
 	    radioPanel.add(rb6);
 //	    radioPanel.add(rbp6);
 	    bidPanel.add(radioPanel);
 	    wrapperPanel.add(bidPanel, BorderLayout.SOUTH);
 		humanInputPanel.add(wrapperPanel, 0);
 		JPanel buttonPanel1 = new JPanel();
 		if(coloredGUI) buttonPanel1.setBackground(tablegreen);
 		buttonPanel1.setLayout(new FlowLayout(FlowLayout.CENTER));
 		humanBid = new JButton(" Submit Bid ");
 		humanBid.addActionListener(new ButtonListener());
 		humanChallenge = new JButton(" Challenge ");
 		humanChallenge.addActionListener(new ButtonListener());
 		
 		startGame = new JButton("New Game");
 		startGame.addActionListener(new ButtonListener());
 		nextRound = new JButton("Next Round");
 		nextRound.addActionListener(new ButtonListener());
 		
 		buttonPanel1.add(humanBid);
 		buttonPanel1.add(humanChallenge);
 		
 		JPanel buttonPanel2 = new JPanel();
 		if(coloredGUI) buttonPanel2.setBackground(tablegreen);
 		buttonPanel2.add(startGame);
 		buttonPanel2.add(nextRound);
 		
 		JPanel buttonPanel = new JPanel();
 		if(coloredGUI) buttonPanel.setBackground(tablegreen);
 		buttonPanel.setLayout(new BorderLayout());
 		
 		buttonPanel.add(buttonPanel1, BorderLayout.NORTH);
 		buttonPanel.add(buttonPanel2, BorderLayout.SOUTH);
 		
 		humanInputPanel.add(buttonPanel, 1);
 		add(humanInputPanel, 8);
 
 		LiarsDiceGameFactory factory = new LiarsDiceGameFactory();
     	List<Player> allPlayers = factory.getPlayers();
 		players = new Vector<Player>();
 		numPlayers = 4; //TODO this should go elsewhere, I think
 		players.setSize(numPlayers);
 		
 		Random rand = new Random();
 		int index = rand.nextInt(allPlayers.size());
 		players.set(0, allPlayers.get(index));
 		botPickers[0].setSelectedIndex(index);
 		allPlayers = factory.getPlayers();
 		index = rand.nextInt(allPlayers.size());
 		players.set(1, allPlayers.get(index));
 		botPickers[1].setSelectedIndex(index);
 		allPlayers = factory.getPlayers();
 		index = rand.nextInt(allPlayers.size());
 		players.set(2, allPlayers.get(index));
 		botPickers[2].setSelectedIndex(index);
 		humanController = new HumanController();
 		humanController.getViewCommunication().registerView(this);
 		players.set(3, new LiarsDicePlayer(humanController, allPlayers.size()));
 		
 		setupPlayers();
 		initializeEnables();
 		writeMessage("Select opponent bots, then click \"New Game\" to get started.");
 
         this.setMinimumSize(new Dimension(600,400));
         
         //grey out everything for now - remove when playview is working again
         if (viewDisabled)
         {
 	        startGame.setEnabled(false);
 	        nextRound.setEnabled(false);
 	        humanBid.setEnabled(false);
 	        humanChallenge.setEnabled(false);
 	        bidQuantity.setEnabled(false);
 	        rb2.setEnabled(false);
 	        rb3.setEnabled(false);
 	        rb4.setEnabled(false);
 	        rb5.setEnabled(false);
 	        rb6.setEnabled(false);
 	        for(JComboBox cb : botPickers){
 	        	cb.setEnabled(false);
 	        }
 	        history.setText("This view is a work in progress. Please check back to the competition website after April 1st for updated code which includes this view.");
         }
 	}
 
 	public void setDice(Player p){
 		if(p == null){
 			return;
 		}
 		if(p == playerPanel1.player){
 			playerPanel1.updateDicePanel(false);
 		}
 		else if(p == playerPanel2.player){
 			playerPanel2.updateDicePanel(false);
 		}
 		else if(p == playerPanel1.player){
 			playerPanel3.updateDicePanel(false);
 		}
 		else if(p == humanPanel.player){
 			humanPanel.updateDicePanel(true);
 		}
 		else{
 			System.out.println("non-existent player: " + p.getID() + " " + p.getName());
 		}
 	}
 
 	private void setupPlayers() {
     			
 		playerPanel1.setPlayer((LiarsDicePlayer)players.get(0));
 		playerPanel2.setPlayer((LiarsDicePlayer)players.get(1));
 		playerPanel3.setPlayer((LiarsDicePlayer)players.get(2));
 		humanPanel.setPlayer((LiarsDicePlayer)players.get(3));
 		
 		playerPanel1.updateDicePanel(false);
 		playerPanel2.updateDicePanel(false);
 		playerPanel3.updateDicePanel(false);
 		humanPanel.updateDicePanel(true);
 	}
 
 	private void initializeEnables() {
 		startGame.setEnabled(true);
 		nextRound.setEnabled(false);
 		bidListener.setEnabled(false);
 		humanChallenge.setEnabled(false);
 		for (int i=0; i<botPickers.length; i++) {
 			botPickers[i].setEnabled(true);
 		}
 	}
 //	Reminders?
 //	Notifications?
 	
     private class PlayerPanel extends JPanel 
     {
     	public LiarsDicePlayer player;
     	public GridLayout layout;
     	public JLabel[][] diceLabels;
     	public Dimension[] diceIndices;
     	public JPanel dicePanel;
     	public ImageIcon die1, die2, die3, die4, die5, die6, dieq, blank;
     	
     	public PlayerPanel(int index) {
     		blank = new ImageIcon("images/blank.png");
     		if(coloredGUI) blank = new ImageIcon("images/blank-green.png");
     		if(nimbus){
     			blank = new ImageIcon("images/blank-nimbus.png");
     		}
     		die1 = new ImageIcon("images/die1.png");
     		die2 = new ImageIcon("images/die2.png");
     		die3 = new ImageIcon("images/die3.png");
     		die4 = new ImageIcon("images/die4.png");
     		die5 = new ImageIcon("images/die5.png");
     		die6 = new ImageIcon("images/die6.png");
     		dieq = new ImageIcon("images/diequestion.png");
     		
     		this.setLayout(new BorderLayout());
     		if(index > -1){
 	    		List<Player> players = facade.getPlayers();
 	    		List<String> playerNames = new ArrayList<String>();
 	    		for(Player p : players){
 	    			playerNames.add(p.getName());
 	    		}
 	    		Object[] botStrings = playerNames.toArray();
 	    		//Create the combo box, select item at index 4.
 	    		//Indices start at 0, so 4 specifies the pig.
 	    		botPickers[index] = new JComboBox(botStrings);
 	    		botPickers[index].addActionListener(new ComboBoxListener());
 	    		this.add(botPickers[index], BorderLayout.NORTH);
     		}
     		dicePanel = new JPanel();
     		if(coloredGUI) dicePanel.setBackground(tablegreen);
     		layout = new GridLayout(5,5);
     		dicePanel.setLayout(layout);
     		diceLabels = new JLabel[5][5];
     		for(int i = 0; i < 5; i++){
     			for(int j = 0; j < 5; j++){
     				diceLabels[i][j] = new JLabel(blank/*" " + i + "," + j + " "*/);
     				dicePanel.add(diceLabels[i][j]);
     			}
     		}
     		diceIndices = new Dimension[5];
     		diceIndices[0] = new Dimension(0,2);
     		diceIndices[1] = new Dimension(2,0);
     		diceIndices[2] = new Dimension(4,1);
     		diceIndices[3] = new Dimension(4,3);
     		diceIndices[4] = new Dimension(2,4);
 //    		for(Dimension d : diceIndices){
 //    			//diceLabels[d.width][d.height].setText("  1  ");
 //    			diceLabels[d.width][d.height].setIcon(dieq);
 //    		}
     		this.add(dicePanel, BorderLayout.CENTER);
     	}
 
 		public void updateDicePanel(boolean show) {
 			Dimension d;
 			int i = 0;
 			while(i < player.getDice().size()){
 				d = diceIndices[i];
 				if(show){
 					diceLabels[d.width][d.height].setIcon(chooseDie(player.getDice().get(i)));
 				}
 				else{
 					diceLabels[d.width][d.height].setIcon(dieq);
 				}
 				i++;
 			}
 			while(i < 5){
 				d = diceIndices[i];
     			diceLabels[d.width][d.height].setIcon(blank);
     			i++;
 			}
 		}
 
 		private Icon chooseDie(Die die) {
 			switch(die.getValue()){
 				case 1:
 					return die1;
 				case 2:
 					return die2;
 				case 3:
 					return die3;
 				case 4:
 					return die4;
 				case 5:
 					return die5;
 				case 6:
 					return die6;
 				default: //shouldn't happen
 					writeMessage("default");
 					return dieq;
 			}
 		}
 
 		public void setPlayer(LiarsDicePlayer player) {
 			this.player = player;
 			reload();
 		}
 		
 		private void reload() {
 			//nameLabel.setText(player.getName()); //TODO assumes getName will be fast and exception-free
 		}
     	
     }
     
     private void runGame() {
     	if (gameThread != null)
     		gameThread.interrupt();
     	setupPlayers(); 
     	Game game = facade.getGame("LiarsDice", players, Long.MAX_VALUE);
     	gameThread = new GameThread(game);
     	gameThread.start();
     	writeMessage("Round 1:");
     }
     
     private class GameThread extends Thread {
     	Game game;
     	public GameThread(Game game) {
     		this.game = game;
     	}
     	public void run() {
     		game.runGame();
     	}
     }
 
 	@Override
 	public void requestDecision(GameInfo gameInfo) {
 		updateView(gameInfo);
 		playerPanel1.updateDicePanel(false);
 		playerPanel2.updateDicePanel(false);
 		playerPanel3.updateDicePanel(false);
 		humanPanel.updateDicePanel(true);
 		bidListener.setEnabled(true);
 		humanChallenge.setEnabled(true);
 		writeMessage("Please enter your bid.");
 	}
 
 	/**
 	 * Updates the message box with all messages for the turns completed, and updates 
 	 * the last-decision displays for all bots, up to the round end or the current turn.
 	 * @param gameInfo A GameInfo object representing the current state of the game.
 	 */
 	private void updateView(GameInfo gameInfo) {
 		Round oldLastRound = oldGameInfo.getGameHistory().getRounds().get(
 				oldGameInfo.getGameHistory().getRounds().size() - 1);
 		Round latestLastRound = gameInfo.getGameHistory().getRounds().get(
 				gameInfo.getGameHistory().getRounds().size() - 1);
 		
 		//for each turn, update the message box and last decisions and add the turn to oldGameInfo
 		for (int turnIndex = oldLastRound.getTurns().size(); 
 				turnIndex < latestLastRound.getTurns().size();
 				turnIndex++) {
 			Turn turn = latestLastRound.getTurns().get(turnIndex);
 			
 			String msg = "";
 			Player currentPlayer = getPlayerFromID(turn.getPlayerID());
 			msg += currentPlayer.getName() + " ";
 			if (turn.getDecision() instanceof Challenge)
 				msg += "challenged.";
 			else if (turn.getDecision() instanceof Bid){
 				msg += "bid ";
 				msg += ((Bid) turn.getDecision()).getFrequency() + " ";
 				msg += ((Bid) turn.getDecision()).getFaceValue() + "s.";
 			}
 			else {
 				msg += "failed to make a decision.";
 			}
 			writeMessage(msg);
 			
 			updateLastDecision(currentPlayer.getID(), (turn.getDecision() != null?
 					turn.getDecision().toString():
 						"Failed Decision"));
 			oldLastRound.addTurn(turn);
 		}
 		
 		if (latestLastRound.isOver())
 		{
 			//add the round result to oldGameInfo
 			oldLastRound.end(latestLastRound.getResult());
 			
 			//report the round result to the user
 			Result roundResult = oldLastRound.getResult();
 			Turn lastTurnInRound = oldLastRound.getTurns().get(oldLastRound.getTurns().size() - 1);
 			Player lastPlayer = getPlayerFromID(lastTurnInRound.getPlayerID());
 			String msg = "Round ended: " + lastPlayer.getName() + " ";
 			if (roundResult == Result.EXCEPTION)
 				msg += "threw an exception and lost a die.";
 			else if (roundResult == Result.INVALIDDECISION)
 				msg += "made an invalid decision and lost a die.";
 			else if (roundResult == Result.LOSING_CHALLENGE)
 				msg += "lost the challenge.";
 			else if (roundResult == Result.TIMEOUT)
 				msg += "timed out and lost a die.";
 			else if (roundResult == Result.WINNING_CHALLENGE) {
 				msg += "won the challenge. ";
 				Turn nextToLastTurnInRound = oldLastRound.getTurns().get(oldLastRound.getTurns().size() - 2);
 				Player nextToLastPlayer = getPlayerFromID(nextToLastTurnInRound.getPlayerID());
 				msg += nextToLastPlayer.getName() + " lost a die.";
 			}
 			writeMessage(msg);
 		}
 	}
 
 	private void updateLastDecision(int playerID, String decisionString) {
 		if (players.get(0).getID() == playerID)
 			player1Decision.setText(decisionString);
 		else if (players.get(1).getID() == playerID)
 			player2Decision.setText(decisionString);
 		else if (players.get(2).getID() == playerID)
 			player3Decision.setText(decisionString);
 		else
 			; //playerID is for the human player, so do nothing.
 	}
 
 	private Player getPlayerFromID(int playerID) {
 		for (Player p : players) {
 			if (p.getID() == playerID)
 				return p;
 		}
 		System.err.println("Player not found for ID " + playerID);
 		return null;
 	}
 
 	@Override
 	public void reportRoundResults(GameInfo gameInfo) {
 		updateView(gameInfo);
 		if (gameInfo.isGameOver())
 			displayGameOver();
 		else {
     		nextRound.setEnabled(true);
     		bidListener.setEnabled(false);
     		humanChallenge.setEnabled(false);
 		}
 	}
 	
 	private void displayGameOver() {
 		writeMessage("Game over. ____ won."); //TODO declare who won
 		playerPanel1.updateDicePanel(true);
 		playerPanel2.updateDicePanel(true);
 		playerPanel3.updateDicePanel(true);
 		humanPanel.updateDicePanel(true);
 		
 		startGame.setText("New Game");
 		for (int i=0; i<botPickers.length; i++) {
 			botPickers[i].setEnabled(true);
 		}
 		startGame.setEnabled(true);
 		bidListener.setEnabled(false);
 		humanChallenge.setEnabled(false);
 		nextRound.setEnabled(false);
 	}
 
 	private void writeMessage(String msg) {
 		history.setText(history.getText() + "\n" + msg);
 		//scrollPane.
 //XXX		history.setText(history.getText() + "max: " + scrollPane.getVerticalScrollBar().getMaximum() + "\n");
 //XXX		scrollPane.getVerticalScrollBar().setValue(50);
 //XXX		history.setText(history.getText() + "current: " + scrollPane.getVerticalScrollBar().getValue() + "\n");
 	}
 
 	private Decision getHumanBid() {
 		int quantity;
 		int face = checkRadioButtons();
 		try{
 			quantity = Integer.parseInt(bidQuantity.getText());
 		}catch(NumberFormatException e){
 			quantity = 0;
 		}
 		return new Bid(quantity, face);
 	}
 
 	private int checkRadioButtons() {
 		if(rb2.isSelected()){
 			return 2;
 		}
 		else if(rb3.isSelected()){
 			return 3;
 		}
 		else if(rb4.isSelected()){
 			return 4;
 		}
 		else if(rb5.isSelected()){
 			return 5;
 		}
 		else if(rb6.isSelected()){
 			return 6;
 		}
 			return -1;
 	}
 	
 	private class BidListener implements ActionListener, DocumentListener
 	{
 		private boolean enabled = false;
 
 		public void setEnabled(boolean b) {
 			enabled = b;
 			updateBidButtonEnabled();
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			updateBidButtonEnabled();
 		}
 
 		public void insertUpdate(DocumentEvent e) {
 			updateBidButtonEnabled();
 		}
 
 		public void removeUpdate(DocumentEvent e) {
 			updateBidButtonEnabled();
 		}
 
 		public void changedUpdate(DocumentEvent e) {
 			updateBidButtonEnabled();
 		}
 		
 		private void updateBidButtonEnabled() {
 			if (enabled && LiarsDiceGame.isValidDecision(getHumanBid(), oldGameInfo))
 				humanBid.setEnabled(true);
 			else
 				humanBid.setEnabled(false);
 		}
 	}
 	
 	private class ButtonListener implements ActionListener
     {
         public void actionPerformed(ActionEvent e){
         	if(e.getSource() == startGame) {
         		if (startGame.getText().equals("New Game"))
         		{
         			startGame.setText("End Game");
             		for (int i=0; i<botPickers.length; i++) {
             			botPickers[i].setEnabled(false);
             		}
             		runGame();
         		}
         		else
         		{
         			startGame.setText("New Game");
             		for (int i=0; i<botPickers.length; i++) {
             			botPickers[i].setEnabled(true);
             		}
         			startGame.setEnabled(true);
         			bidListener.setEnabled(false);
         			humanChallenge.setEnabled(false);
         			nextRound.setEnabled(false);
         			
         			writeMessage("Game over.");
         			
         			gameThread.interrupt();
         		}
         	}
         	else if(e.getSource() == nextRound) {
     			writeMessage("Round " + (oldGameInfo.getGameHistory().getRounds().size() + 1) + ":");
     			oldGameInfo.getGameHistory().addNewRound();
     			nextRound.setEnabled(false);
         		humanController.getViewCommunication().continueNextRound();
     		}
         	else if(e.getSource() == humanBid){
         		bidListener.setEnabled(false);
         		humanChallenge.setEnabled(false);
         		Decision decision = getHumanBid();
         		humanController.getViewCommunication().setDecision(decision);
         	}
         	else if(e.getSource() == humanChallenge){
         		//writeMessage(""+scrollPane.getVerticalScrollBar().getValue());
         		Decision decision = new Challenge();
         		humanController.getViewCommunication().setDecision(decision);
         	}
         }
     }
 	
 	private class ComboBoxListener implements ActionListener{
 		
 		public ComboBoxListener(){
 			
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			LiarsDiceGameFactory factory = new LiarsDiceGameFactory();
 	    	List<Player> allPlayers = factory.getPlayers();
 			if(e.getSource() == botPickers[0]) {
 				players.set(0, allPlayers.get(botPickers[0].getSelectedIndex()));
 			}
 			else if(e.getSource() == botPickers[1]) {
 				players.set(1, allPlayers.get(botPickers[1].getSelectedIndex()));
 			}
 			else if(e.getSource() == botPickers[2]) {
 				players.set(2, allPlayers.get(botPickers[2].getSelectedIndex()));
 			}
 		}
 	}
 	
 	private class RadioButtonListener implements ActionListener{
 		
 		public RadioButtonListener(){
 			
 		}
 		
 		public void actionPerformed(ActionEvent e) {
 			if(e.getSource() == rb2){
 				rb2.setIcon(new ImageIcon("images/small/die2-black.png"));
 			}
 			else{
 				rb2.setIcon(new ImageIcon("images/small/die2.png"));
 			}
 			
 			if(e.getSource() == rb3){
 				rb3.setIcon(new ImageIcon("images/small/die3-black.png"));
 			}
 			else{
 				rb3.setIcon(new ImageIcon("images/small/die3.png"));
 			}
 			
 			if(e.getSource() == rb4){
 				rb4.setIcon(new ImageIcon("images/small/die4-black.png"));
 			}
 			else{
 				rb4.setIcon(new ImageIcon("images/small/die4.png"));
 			}
 			
 			if(e.getSource() == rb5){
 				rb5.setIcon(new ImageIcon("images/small/die5-black.png"));
 			}
 			else{
 				rb5.setIcon(new ImageIcon("images/small/die5.png"));
 			}
 			
 			if(e.getSource() == rb6){
 				rb6.setIcon(new ImageIcon("images/small/die6-black.png"));
 			}
 			else{
 				rb6.setIcon(new ImageIcon("images/small/die6.png"));
 			}
 			
 		}
 		
 	}
 }
