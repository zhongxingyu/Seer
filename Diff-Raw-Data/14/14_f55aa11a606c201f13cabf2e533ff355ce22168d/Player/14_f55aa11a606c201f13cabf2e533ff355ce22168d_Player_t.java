 /**
  * @file Player.java
  * @author Jia Chen
  * @date Sept 06, 2011
  * @description 
  * 		Player.java is used to keep track of player information in the
  * 		game. It also keeps track of the player records.
  */
 
/**
 * if user does not have deck, prompt them to make a deck
 */

 package Game;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 import java.io.Serializable;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 import javax.swing.Box;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextArea;
 
 import CardAssociation.Card;
 import CardAssociation.Deck;
 import DeckBuilder.BuilderGUI;
 import Field.Hand;
 import Field.NewMainField;
 
 public class Player implements Serializable {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8938420254001640004L;
 	public Phase currentPhase;
 	private Hand hand;
 	public NewMainField field;
 	private int playerID;
 
 	private ArrayList<String> playerDecks;
 	public String selectedDeck;
 	public Deck currentDeck;
 	public HashMap<String, Card> dictionary;
 
 	public JFrame userFrame;
 	public JTabbedPane tabbedPane;
 	public JPanel deckPane;
 	public JButton playGame;
 
 	private BuilderGUI builderGui = null;
 	private int sessionID;
 	private boolean inGame;
 
 	public JFrame f;
 	private boolean loggedOut;
 	private Game currentGame = null;
 
 	// private Connector connector;
 
 	// Player Constructor initializes all UI fields
 	public Player() {
 		playerID = (int) (Math.random() * 10000);
 		userFrame = new JFrame("Testificate #" + playerID);
 		tabbedPane = new JTabbedPane();
 		deckPane = new JPanel();
 		loggedOut = true;
 
 		selectedDeck = "";
 
 		playGame = new JButton("Launch Solo");
 		playGame.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (!selectedDeck.isEmpty()) {
 					if (findGame())
 						buildGame();
 					// setReady();
 				}
 			}
 
 		});
 
 		playGame.setEnabled(false);
 
 		userFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		sessionID = -1;
 
 		playerDecks = new ArrayList<String>();
 		// connector = new Connector("Cyber", 5000);
 		deserializer();
 	}
 
 	// for Game.java to specify what game phase the player is at
 	public void setCurrentPhase(Phase nextPhase) {
 		currentPhase = nextPhase;
 	}
 
 	// to retrieve the current phase information of the player
 	public Phase getCurrentPhase() {
 		return currentPhase;
 	}
 
 	// set the player's hand
 	public void setHand(Hand hand) {
 		this.hand = hand;
 	}
 
 	// get the player's hand information
 	public Hand getHand() {
 		return hand;
 	}
 
 	// display and choose all the valid decks that the player has build
 	public void displayDecks() {
 		// display all decks in the "Deck" directory
 		File file = new File("Deck");
 		if (file.isDirectory()) {
 			String titles[] = file.list();
 			for (int i = 0; i < titles.length; i++) {
 				playerDecks.add(titles[i]);
 			}
 		} else {
 			System.out.println("Not A Directory");
 		}
		
		if(playerDecks.isEmpty()) {
			return ;
		}
		
 		// selecting a deck
 		for (String deckTitle : playerDecks) {
 			final JButton deckSelect = new JButton(deckTitle);
 			deckSelect.addActionListener(new ActionListener() {
 
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					selectedDeck = deckSelect.getText();
 					initField();
 					if (isReady())
 						playGame.setEnabled(true);
 					else
 						playGame.setEnabled(false);
 
 					ready = false;
 					
 					int reset = deckPane.getComponentCount();
 
 					for (int i = 0; i < reset; i++) {
 						deckPane.getComponent(i).setBackground(Color.CYAN);
 					}
 					
 					deckSelect.setBackground(Color.RED);
 					// System.out.println("Deck Selected : " + selectedDeck);
 					// setReady();
 					// startDeckEdit();
 				}
 
 			});
 			deckSelect.setBackground(Color.CYAN);
 
 			deckPane.add(deckSelect);
 			// System.out.println(playerID + " : " + deckTitle);
 		}
 
 		/*
 		 * final JButton newDeck = new JButton("New_Deck");
 		 * newDeck.addActionListener(new ActionListener() {
 		 * 
 		 * @Override public void actionPerformed(ActionEvent e) {
 		 * startDeckEdit(); }
 		 * 
 		 * }); deckPane.add(newDeck);
 		 */
 	}
 
 	private boolean findGame() {
 		// TODO
 		return true;
 	}
 
 	public void initField() {
 		// displayDecks();
 
 		System.err.println("loading....");
 
 		// loading the deck
 		currentDeck = new Deck();
 		currentDeck.loadRaw(new File("Deck/" + selectedDeck), dictionary);
 		System.out.println(selectedDeck + " has " + currentDeck.getPlayingDeck().size() + " cards");
 
 		field = new NewMainField(this);
 
 		boolean readyDeck = field.prepare(currentDeck);
 		// Create Field End
 
 		// Create Hand given Field information
 		System.out.println("Deck Size = " + currentDeck.getCards().size());
 		hand = new Hand("Vertical.png", 50, 850, this);
 		// userFrame.add(hand);
 		// System.out.println("should say match game " + sessionID);
 		//
 		// Runner.connector.messenger("match game " + sessionID, this);
 
 		// Allow field to see hand
 
 		// Create Game given player information
 		if (readyDeck)
 			setReady();
 		else
 			System.out.println("The deck is not ready...");
 		System.out.println(playerID + " is ready");
 	}
 
 	private boolean ready = false;
 	private Box displayInfo;
 	private Box leftPanel;
 	private Box statsInfo;
 
 	public boolean isReady() {
 		return ready;
 	}
 
 	public void setReady() {
 		ready = true;
 	}
 
 	public void buildGame() {
 		System.out.println("Building game....");
 		f = new JFrame("Weiss Schwarz " + playerID);
 		// if (currentGame == null) {
 		currentGame = new Game(this);
 		initField();
 		currentGame.testGame();
 
 	}
 
 	public void drawField() {
 		displayInfo = Box.createHorizontalBox();
 		displayInfo.setPreferredSize(new Dimension(200, 500));
 		JPanel gamePanel = new JPanel();
 		gamePanel.add(currentGame);
 
 		statsInfo = Box.createVerticalBox();
 		updateStatsBox();
 
 		leftPanel = Box.createVerticalBox();
 		leftPanel.add(displayInfo);
 		leftPanel.add(Box.createVerticalGlue());
 		leftPanel.add(statsInfo);
 		leftPanel.add(Box.createVerticalStrut(10));
 
 		f.add(gamePanel, BorderLayout.EAST);
 		f.add(leftPanel, BorderLayout.WEST);
 		f.pack();
 		f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		f.setLocationRelativeTo(null);
 		f.setVisible(true);
 		// currentGame = null;
 
 		currentGame.startGame();
 	}
 
 	public void updateStatsBox() {
 		statsInfo.removeAll();
 		statsInfo.validate();
 		statsInfo.add(new JLabel("Cards remain: " + this.getField().getDeckZone().getCount()));
 		statsInfo.add(new JLabel("Waiting room: " + this.getField().getWaitingRoom().getCount()));
 		statsInfo.add(new JLabel("Clock damage: " + this.getField().getClockZone().getCount()));
 		statsInfo.add(new JLabel("Level count : " + this.getField().getLevelZone().getCount()));
 		statsInfo.add(new JLabel("Stock size  : " + this.getField().getStockZone().getCount()));
 		statsInfo.add(new JLabel("Memory count: " + this.getField().getMemoryZone().getCount()));
 		f.setVisible(true);
 	}
 
 	public void retreiveCardInfo(Card selectedCard) {
 		displayInfo.removeAll();
 		displayInfo.validate();
 		Dimension dim = new Dimension(200, 400);
 		Box displayArea = Box.createVerticalBox();
 		GridBagConstraints gbc = new GridBagConstraints();
 		gbc.fill = GridBagConstraints.NONE;
 
 		if (selectedCard == null) {
 			displayArea.setPreferredSize(dim);
 		}
 
 		int height = 100, width = 50;
 
 		displayArea.setPreferredSize(dim);
 		displayArea.setMinimumSize(dim);
 		displayArea.setMaximumSize(dim);
 
 		try {
 			height = selectedCard.getCardImage().getHeight(null);
 			width = selectedCard.getCardImage().getWidth(null);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		JPanel image = selectedCard.displayImage(width, height);
 		displayArea.add(image);
 
 		JTextArea cardTitle = new JTextArea(selectedCard.getCardName());
 		cardTitle.setLineWrap(true);
 		cardTitle.setWrapStyleWord(true);
 		cardTitle.setEditable(false);
 
 		String areaContext = "";
 		areaContext += "LEVEL: " + (selectedCard.getLevel() >= 0 ? selectedCard.getLevel() : "-") + " ";
 		areaContext += "COST: " + (selectedCard.getCost() >= 0 ? selectedCard.getCost() : "-") + " ";
 		areaContext += "TRIGGER: " + selectedCard.getTrigger();
 
 		JTextArea cardNumber = new JTextArea(areaContext);
 		cardNumber.setLineWrap(true);
 		cardNumber.setWrapStyleWord(true);
 		cardNumber.setEditable(false);
 
 		areaContext = "";
 		areaContext += "POWER: " + (selectedCard.getPower() >= 0 ? selectedCard.getPower() : "-") + " ";
 		areaContext += "SOUL: " + (selectedCard.getSoul() >= 0 ? selectedCard.getSoul() : "-");
 		JTextArea power = new JTextArea(areaContext);
 		power.setLineWrap(true);
 		power.setWrapStyleWord(true);
 		power.setEditable(false);
 
 		JTextArea text = new JTextArea(selectedCard.getEffects());
 		text.setLineWrap(true);
 		text.setWrapStyleWord(true);
 		text.setEditable(false);
 
 		JScrollPane effect = new JScrollPane(text);
 		displayArea.add(cardTitle);
 		displayArea.add(cardNumber);
 		displayArea.add(power);
 		displayArea.add(effect);
 
 		displayArea.setAlignmentY(Component.LEFT_ALIGNMENT);
 
 		displayInfo.add(displayArea);
 		displayInfo.setAlignmentX(Component.TOP_ALIGNMENT);
 
 		// displayInfo.setAlignmentY(BoxLayout.PAGE_AXIS);
 		// displayInfo.setAlignmentY(BoxLayout.Y_AXIS);
 
 		f.setVisible(true);
 
 	}
 
 	public NewMainField getField() {
 		return field;
 	}
 
 	public String getSelectedDeck() {
 		return selectedDeck;
 	}
 
 	@SuppressWarnings("unchecked")
 	private void deserializer() {
 		InputStream fileInput;
 		ObjectInputStream objectInput;
 
 		try {
 			fileInput = getClass().getResourceAsStream("/resources/CardDatav2");
 			objectInput = new ObjectInputStream(fileInput);
 
 			objectInput.readObject();
 			dictionary = (HashMap<String, Card>) objectInput.readObject();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	public void buildAndDisplay() {
 		// deckPane.setSize(new Dimension(500, 500));
 		displayDecks();
 
 		int dimension = (int) Math.ceil(Math.sqrt(playerDecks.size()));
		if(dimension <= 0 )
			dimension = 1;
 		deckPane.setLayout(new GridLayout(dimension, dimension));
 
 		deckPane.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				refresh();
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 			}
 
 		});
 
 		if (builderGui == null) {
 			builderGui = new BuilderGUI();
 			builderGui.init();
 		}
 
 		tabbedPane.addTab("Builder", builderGui.getContentPane());
 		tabbedPane.addTab("Game", playGame);
 		tabbedPane.addTab("Deck", deckPane);
 
 		tabbedPane.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				refresh();
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 			}
 
 		});
 
 		// Box box = Box.createVerticalBox();
 		// box.setAlignmentX(Box.CENTER_ALIGNMENT);
 		// box.add(tabbedPane);
 
 		userFrame.setContentPane(tabbedPane);
 
 		Dimension playerDim = new Dimension(builderGui.getWidth() + 10, builderGui.getHeight() + 30);
 
 		userFrame.setMinimumSize(playerDim);
 		userFrame.setMaximumSize(playerDim);
 		// userFrame.pack();
 		userFrame.setLocationRelativeTo(null);
 		userFrame.setVisible(true);
 	}
 
 	public void setSessionID(int playerCount) {
 		sessionID = playerCount;
 		System.out.println("my session id is " + sessionID);
 	}
 
 	// get the player's session id
 	public int getSessionID() {
 		return sessionID;
 	}
 
 	// check to see if the player is logged in
 	public boolean getLogedOut() {
 		return loggedOut;
 	}
 
 	public void setLogedOut() {
 		loggedOut = !loggedOut;
 	}
 
 	// check to see if player is already playing a game
 	public boolean isInGame() {
 		return inGame;
 	}
 
 	public void setInGame(boolean b) {
 		inGame = b;
 	}
 
 	public Player clone() {
 		return this;
 	}
 
 	public void setGame(Game game) {
 		currentGame = game;
 		// currentGame.setFirst(this);
 		System.out.println("set game into game");
 		refresh();
 	}
 
 	private void refresh() {
 
 		playerDecks.clear();
 		deckPane.removeAll();
 		deckPane.revalidate();
 
 		displayDecks();
 
 		deckPane.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent arg0) {
 				refresh();
 			}
 
 			@Override
 			public void focusLost(FocusEvent arg0) {
 			}
 
 		});
 
 	}
 
 	public void setPlayerID(int playerID) {
 		this.playerID = playerID;
 	}
 
 	public int getPlayerID() {
 		return playerID;
 	}
 
 }
