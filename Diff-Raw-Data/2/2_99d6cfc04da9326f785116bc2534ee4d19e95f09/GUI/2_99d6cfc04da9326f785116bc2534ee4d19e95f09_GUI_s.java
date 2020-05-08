 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 import javax.swing.ButtonGroup;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JRadioButtonMenuItem;
 
 
 public class GUI {
 
 	public JFrame frame;
 	private final int FRAME_WIDTH = 1280;
 	private final int FRAME_HEIGHT = 900;
 	private final int BUT_HEIGHT = 100;
 	private final int BUT_WIDTH = 100;
 	public JPanel panel;
 	public JPanel gemPileStuff;
 	public JPanel playerStuff;
 	public Game game;
 	public Color trans = new Color(255, 255, 255, 0);
 	private int quickBuyVal;
 	private int quickBuyNum;
 	public boolean buyPhase = false;
 	public boolean chooseCharPhase = false;
 	public int playerNum;
 	public int selectedChar = 0;
 	public String lang = "English";
 	public JPanel charChoices = new JPanel();
 	public ArrayList<Integer> charsSoFar = new ArrayList<Integer>();
 
 	public GUI() {
 		this.game = new Game(1);
 		this.game.setLocale("English");
 		this.frame = new JFrame(this.game.names.getString("Title"));
 		this.frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setResizable(false);
 		this.frame.setVisible(true);
 		this.frame.setResizable(false);
 
 		this.panel = new JBackgroundPanel();
 		this.frame.setContentPane(this.panel);
 		updateFrame();
 //		StartGUI();
 		titleScreen();
 	}
 	
 	public GUI(int i, ArrayList<Integer> chtrs) {
 		this.playerNum = i;
 		this.charsSoFar = chtrs;
 		this.game = new Game(1);
 		this.frame = new JFrame(this.game.names.getString("Title"));
 		this.frame.setSize(FRAME_WIDTH, FRAME_HEIGHT);
 		this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.frame.setResizable(false);
 		this.frame.setVisible(true);
 		this.frame.setResizable(false);
 
 		this.panel = new JBackgroundPanel();
 		this.frame.setContentPane(this.panel);
 		updateFrame();
 		this.startGameWithPlaysAndChars();
 	}
 
 	public void StartGUI() {
 
 		Icon icon = new ImageIcon();
 
 		Object[] options = { 2, 3, 4 };
 		Integer n = JOptionPane
 				.showOptionDialog(
 						this.frame,
 						"How many players will be playing?\nCombien de joueurs vont jouer?",
 						"New Game", JOptionPane.OK_OPTION,
 						JOptionPane.QUESTION_MESSAGE, icon, options, options[0]);
 
 		Object[] options2 = { "English", "French" };
 		this.lang = (String) JOptionPane.showInputDialog(this.frame,
 				"Choose Default Language\nChoisir la Langue par Dfaut",
 				"Language", JOptionPane.OK_OPTION, icon, options2, options2[0]);
 		this.game.setLocale(this.lang);
 		this.frame.setTitle(this.game.names.getString("Title"));
 
 		this.playerNum = n + 2;
 		
 		setUpCharChoicePanel();
 		
 		chooseCharsScreen();
 
 	}
 
 	private void setUpCharChoicePanel() {
 		class ChangeCharListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				changeSelectedChar((JButton) e.getSource());
 			}
 
 		}
 		
 		this.charChoices = new JPanel();
 		this.charChoices.setPreferredSize(new Dimension(FRAME_WIDTH, 200));
 		this.charChoices.setBackground(this.trans);
 
 		for (int i = 0; i < this.game.Characters.length; i++) {
 			JButton character = new JBackgroundButton();
 			character.setName("" + i);
 			character.add(new JLabel((String) this.game.Characters[i]));
 			character.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 			character.addActionListener(new ChangeCharListener());
 			this.charChoices.add(character);
 		}
 		
 	}
 
 	public void titleScreen() {
 
 		JPanel titleStuff = new JPanel();
 		titleStuff.setBackground(this.trans);
 		titleStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				FRAME_HEIGHT));
 		JLabel picLabel = new JLabel(new ImageIcon("titlescreen.png"));
 		picLabel.setBackground(this.trans);
 		titleStuff.add(picLabel);
 		JButton startGameUp = new JButton("Start Game");
 		class StartListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				StartGUI();
 			}
 		}
 
 		startGameUp.addActionListener(new StartListener());
 
 		titleStuff.add(startGameUp);
 
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		titleStuff.setName("Title");
 		this.panel.add(titleStuff);
 		updateFrame();
 	}
 	
 	public void chooseCharsScreen() {
 		this.chooseCharPhase = true;
 		addMenuBar();
 
 		JPanel chardsStuff = new JPanel();
 		chardsStuff.setBackground(this.trans);
 		chardsStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				FRAME_HEIGHT / 3));
 		JPanel hand = new JPanel();
 		hand.setPreferredSize(new Dimension(FRAME_WIDTH, 500));
 		hand.setBackground(this.trans);
 		for (int i = 0; i < 3; i++) {
 			Card card = this.game.getPlayerCards(this.selectedChar).get(i);
 			JLabel picLabel = new JLabel(new ImageIcon(
 					this.game.names.getString("Path") + card.imagePath));
 			picLabel.setBackground(this.trans);
 			hand.add(picLabel);
 		}
 		JButton selectBut = new JButton(this.game.names.getString("Select"));
 		class SelectListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				selectChar((JButton) e.getSource());
 			}
 		}
 
 		selectBut.addActionListener(new SelectListener());
 		JPanel screen = new JPanel();
 		screen.setBackground(this.trans);
 		screen.add(hand);
 
 
 		JPanel mostPhases = new JPanel();
 		mostPhases.setBackground(this.trans);
 		mostPhases.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		mostPhases.add(screen);
 		mostPhases.add(selectBut);
 		mostPhases.add(this.charChoices);
 
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		mostPhases.setName("Choosing");
 		this.panel.add(mostPhases);
 		updateFrame();
 	}
 
 	public void startGameWithPlaysAndChars() {
 
 		this.game = new Game(this.playerNum);
 		this.game.setLocale(this.lang);
 		chooseCharacters(this.playerNum, this.charsSoFar);
 
 		firstSetUp();
 		newTurn();
 	}
 
 	public void changeGameLanguage(String lang) {
 		this.game.setLocale(lang);
 		JOptionPane.setDefaultLocale(this.game.currentLocale);
 		this.frame.setTitle(this.game.names.getString("Title"));
 		
 		if (this.chooseCharPhase) {
 			this.chooseCharsScreen();
 			return;
 		}
 
 		if (buyPhase) {
 			newTurn();
 			setUp();
 		} else {
 			setUp();
 			newTurn();
 		}
 
 		if (this.game.getNumber > 0) {
 			quickBuy(this.game.underVal, this.game.getNumber);
 			updateFrame();
 		}
 	}
 
 	private void changeSelectedChar(JButton chars) {
 		this.selectedChar = Integer.parseInt(chars.getName());
 		chooseCharsScreen();
 	}
 
 	public class JBackgroundPanel extends JPanel {
 		private BufferedImage img;
 
 		public JBackgroundPanel() {
 			// load the background image
 			try {
 				img = ImageIO.read(new File("./backgroundpanels.png"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			// paint the background image and scale it to fill the entire space
 			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 
 	public class JBackgroundButton extends JButton {
 		private BufferedImage img;
 
 		public JBackgroundButton() {
 			// load the background image
 			try {
 				img = ImageIO.read(new File("./cardbg.png"));
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 
 		@Override
 		protected void paintComponent(Graphics g) {
 			super.paintComponent(g);
 			// paint the background image and scale it to fill the entire space
 			g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
 		}
 	}
 
 	public void firstSetUp() {
 		JPanel shopPhase = new JPanel();
 		shopPhase.setName("Shopping");
 		JPanel shopCards = new JPanel();
 
 		class CardShopListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cardShopInfo((JButton) e.getSource());
 			}
 		}
 		shopPhase.setBackground(this.trans);
 		shopPhase.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		shopCards.setBackground(this.trans);
 		shopCards.setPreferredSize(new Dimension(FRAME_WIDTH,
 				14 * FRAME_HEIGHT / 16));
 		for (int i = 0; i < this.game.bank.size(); i++) {
 			JButton card = new JBackgroundButton();
 			card.setName("" + i);
 			card.add(new JLabel(this.game.bank.get(i).getName(this.game)));
 			card.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 			card.addActionListener(new CardShopListener());
 			shopCards.add(card);
 		}
 
 		class EndShopListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				endShopPhase();
 			}
 		}
 
 		JButton endPhase = new JButton(this.game.names.getString("EndPhase"));
 		endPhase.addActionListener(new EndShopListener());
 		shopPhase.add(shopCards);
 		shopPhase.add(endPhase);
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		this.panel.add(shopPhase);
 	}
 
 	public void setUp() {
 		firstSetUp();
 		updateFrame();
 
 	}
 
 	public void newTurn() {
 
 		addMenuBar();
 
 		class CardListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				cardInfo((JButton) e.getSource());
 			}
 		}
 		this.playerStuff = new JPanel();
 		this.playerStuff.setBackground(this.trans);
 		this.playerStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				FRAME_HEIGHT / 3));
 		JPanel hand = new JPanel();
 		hand.setBackground(this.trans);
 		hand.setPreferredSize(new Dimension(FRAME_WIDTH, 2 * FRAME_HEIGHT / 8));
 		for (int i = 0; i < this.game.players.get(this.game.turn).hand.size(); i++) {
 			JButton card = new JBackgroundButton();
 			card.add(new JLabel(this.game.getCurrentPlayer().hand.get(i)
 					.getName(this.game)));
 			card.setName("" + i);
 			card.addActionListener(new CardListener());
 			card.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 			hand.add(card);
 		}
 		JButton endPhase = new JButton(this.game.names.getString("EndPhase"));
 		class EndListener implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				newShopPhase();
 			}
 		}
 
 		endPhase.addActionListener(new EndListener());
 		this.playerStuff.add(hand);
 		this.playerStuff.add(endPhase);
 
 		this.gemPileStuff = new JPanel();
 
 		for (int i = 0; i < this.game.playerNum; i++) {
 			JLabel name = new JLabel(this.game.names.getString("Player") + " "
 					+ (i + 1));
 			JPanel gemPileMargins = new JPanel();
 			JPanel gemPile = new JPanel();
 			gemPileMargins.setPreferredSize(new Dimension(FRAME_WIDTH
 					/ (this.game.playerNum + 1), 400));
 			gemPileMargins.setBackground(this.trans);
 			gemPile.setPreferredSize(new Dimension(100, 400));
 			gemPile.setBackground(this.trans);
 
 			addGemPile(i, gemPile);
 
 			gemPileMargins.add(gemPile);
 			if (this.game.turn == i) {
 				name.setForeground(Color.YELLOW);
 			} else {
 				name.setForeground(Color.WHITE);
 			}
 			JPanel player = new JPanel();
 			player.setPreferredSize(new Dimension(FRAME_WIDTH
 					/ (this.game.playerNum + 1), FRAME_HEIGHT / 2));
 			player.setBackground(this.trans);
 			player.add(name);
 			player.add(gemPileMargins);
 			this.gemPileStuff.add(player);
 		}
 
 		this.gemPileStuff.setBackground(this.trans);
 		this.gemPileStuff.setPreferredSize(new Dimension(FRAME_WIDTH,
 				3 * FRAME_HEIGHT / 5));
 
 		JPanel mostPhases = new JPanel();
 		mostPhases.setBackground(this.trans);
 		mostPhases.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 		mostPhases.add(this.gemPileStuff);
 		mostPhases.add(this.playerStuff);
 
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		mostPhases.setName("Playing");
 		this.panel.add(mostPhases);
 		updateFrame();
 	}
 
 	public void endQuickBuy() {
 		this.game.clearMiniBuy();
 		newTurn();
 	}
 
 	public void selectChar(JButton source) {
 		JButton selected = findSelectedChar();
 		this.charChoices.remove(selected);
 		this.charsSoFar.add(Integer.parseInt(selected.getName()));
 		if (this.charsSoFar.size() == this.playerNum) {
 			this.chooseCharPhase = false;
 			startGameWithPlaysAndChars();
 		} else {
 			chooseCharsScreen();
 		}
 	}
 	
 	public JButton findSelectedChar(){
 		Component[] things = this.charChoices.getComponents();
 		for (int i = 0; i < things.length; i++){
 			if (Integer.parseInt(things[i].getName()) == this.selectedChar){
 				return (JButton) things[i];
 			}
 		}
 		return null;
 	}
 
 	public void endShopPhase() {
 		if (this.game.boughtSomething) {
 			this.buyPhase = false;
 			this.game.getCurrentPlayer().endTurn();
 			this.game.newTurn();
 			newTurn();
 		}
 	}
 
 	public void newShopPhase() {
 		this.buyPhase = true;
 		this.game.totalMoney();
 		if (this.panel.getComponentCount() != 0) {
 			this.panel.removeAll();
 		}
 		setUp();
 	}
 
 	private void useCard(Card clicked) {
 		ChoiceGroup choices = clicked.getChoice(this.game);
 		Icon icon = new ImageIcon();
 		Choice current = choices.getNextChoice();
 
 		boolean completeSoFar = cycleChoices(choices, current, clicked,
 				this.game.turn);
 
 		if (completeSoFar && clicked.opposing) {
 			// if targets opponent(s)
 			boolean reacted = reactToPlay(clicked, choices);
 			// Nobody reacted to card
 			if (!reacted) {
 				clicked.use(choices.getChoiceList(), this.game);
 			}
 			// No target
 		} else if (completeSoFar) {
 			clicked.use(choices.getChoiceList(), this.game);
 		}
 
 		if (this.game.getNumber > 0) {
 			this.game.useCard(clicked);
 			quickBuy(this.game.underVal, this.game.getNumber);
 		} else {
 			this.game.useCard(clicked);
 			newTurn();
 		}
 
 	}
 
 	private void updateFrame() {
 		this.frame.validate();
 		this.frame.setVisible(true);
 		this.frame.repaint();
 	}
 
 	private void quickBuy(int val, int num) {
 		this.panel = new JBackgroundPanel();
 		this.frame.setContentPane(this.panel);
 		updateFrame();
 		this.quickBuyVal = val;
 		this.quickBuyNum = num;
 		JPanel quickBuy = new JPanel();
 		JPanel quickCards = new JPanel();
 
 		if (num > 0) {
 
 			class CardShopListener implements ActionListener {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					cardGetInfo((JButton) e.getSource());
 				}
 			}
 			quickBuy.setBackground(this.trans);
 			quickBuy.setPreferredSize(new Dimension(FRAME_WIDTH, FRAME_HEIGHT));
 			quickCards.setBackground(this.trans);
 			quickCards.setPreferredSize(new Dimension(FRAME_WIDTH,
 					14 * FRAME_HEIGHT / 16));
 			for (int i = 0; i < this.game.bank.size(); i++) {
 				if (this.game.bank.get(i).cost <= val) {
 					JButton card = new JBackgroundButton();
 					card.setName("" + i);
 					card.add(new JLabel(this.game.bank.get(i)
 							.getName(this.game)));
 					card.setPreferredSize(new Dimension(BUT_WIDTH, BUT_HEIGHT));
 					card.addActionListener(new CardShopListener());
 					quickCards.add(card);
 				}
 			}
 
 			class EndShopListener implements ActionListener {
 				@Override
 				public void actionPerformed(ActionEvent e) {
 					endQuickBuy();
 				}
 			}
 
 			JButton endPhase = new JButton(
 					this.game.names.getString("EndPhase"));
 			endPhase.addActionListener(new EndShopListener());
 			quickBuy.add(quickCards);
 			quickBuy.add(endPhase);
 
 			if (this.panel.getComponentCount() != 0) {
 				this.panel.removeAll();
 			}
 			this.panel.add(quickBuy);
 
 			updateFrame();
 		} else {
 			this.game.clearMiniBuy();
 			newTurn();
 		}
 	}
 
 	public void cardInfo(JButton card) {
 		String numString = card.getName();
 		int num = Integer.parseInt(numString);
 		Card clicked = this.game.getCurrentPlayer().hand.get(num);
 
 		Object[] options = { this.game.names.getString("Use") };
 
 		int decision = cardMakeInfo(
 				this.game.getCurrentPlayer().canUseCard(clicked), "", clicked,
 				options);
 
 		if (decision == 0) {
 			newTurn();
 			useCard(clicked);
 		}
 		updateFrame();
 	}
 
 	public int cardMakeInfo(Boolean condition, String description, Card card,
 			Object[] options) {
 		Card clicked = card;
 		Icon icon = new ImageIcon();
 		if (clicked.imagePath != null) {
 			icon = new ImageIcon(this.game.names.getString("Path")
 					+ clicked.imagePath);
 		}
 		if (condition) {
 
 			Integer n = JOptionPane.showOptionDialog(this.frame, description,
 					clicked.getName(this.game), JOptionPane.OK_OPTION,
 					JOptionPane.QUESTION_MESSAGE, icon, options, options[0]);
 
 			if (n == 0) {
 				updateFrame();
 				return n;
 			} else {
 				updateFrame();
 				return -1;
 			}
 		} else {
 			Object[] noOptions = {};
 			JOptionPane.showOptionDialog(this.frame, description,
 					clicked.getName(this.game), JOptionPane.DEFAULT_OPTION,
 					JOptionPane.INFORMATION_MESSAGE, icon, noOptions, null);
 			updateFrame();
 			return -1;
 		}
 	}
 
 	public void cardShopInfo(JButton card) {
 
 		String numString = card.getName();
 		int num = Integer.parseInt(numString);
 		Card clicked = this.game.bank.get(num);
 
 		Object[] options = { this.game.names.getString("Buy") };
 
 		int decision = cardMakeInfo(this.game.canBuy(clicked),
 				this.game.names.getString("Amount") + " " + clicked.amount,
 				clicked, options);
 
 		if (decision == 0) {
 			this.game.playerBuyCard(this.game.getCurrentPlayer(), clicked);
 		}
 
 		updateFrame();
 	}
 
 	public void cardGetInfo(JButton card) {
 		String numString = card.getName();
 		int num = Integer.parseInt(numString);
 		Card clicked = this.game.bank.get(num);
 
 		Object[] options = { this.game.names.getString("Get") };
 
 		int decision = cardMakeInfo(true, this.game.names.getString("Amount")
 				+ " " + clicked.amount, clicked, options);
 
 		if (decision == 0) {
 			this.game.playerGetCard(this.game.getCurrentPlayer(), clicked);
 			quickBuy(this.quickBuyVal, this.quickBuyNum - 1);
 		}
 
 		updateFrame();
 	}
 
 	public boolean cardReactInfo(Card card) {
 		Card clicked = card;
 
 		Object[] options = { this.game.names.getString("React"),
 				this.game.names.getString("Dont") };
 
 		int decision = cardMakeInfo(true, "", clicked, options);
 
 		if (decision == 0) {
 			return true;
 		} else {
 			return false;
 		}
 
 	}
 
 	public void addGemPile(int i, JPanel pane) {
 		for (int j = 0; j < 4; j++) {
 			JLabel gem = new JLabel("" + (j + 1) + " "
 					+ this.game.names.getString("Gems") + ": "
 					+ this.game.players.get(i).gemPile[j]);
 			pane.add(gem);
 		}
 
 		JLabel totalVal = new JLabel(this.game.names.getString("Total") + ": "
 				+ this.game.players.get(i).totalGemValue());
 		pane.add(totalVal);
 	}
 
 	public boolean cycleChoices(ChoiceGroup choices, Choice current,
 			Card clicked, int player) {
 		Icon icon = new ImageIcon();
 		while (current != null) {
 			while (current.nextChoice()) {
 				Object[] options = current.getOptions().toArray();
 				String n = (String) JOptionPane.showInputDialog(
 						this.frame,
 						current.getInstructions(),
 						this.game.names.getString("Player") + " "
 								+ (player + 1) + ":  "
 								+ clicked.getName(this.game),
 						JOptionPane.OK_OPTION, icon, options, options[0]);
 				if (n != null) {
 					current.addChoice(n);
 				} else {
 					return false;
 				}
 				current = choices.getNextChoice();
 				if (current == null) {
 					return true;
 				}
 			}
 
 			current = choices.getNextChoice();
 		}
 		return true;
 	}
 
 	public void makeLangOptions(String[] langs, String[] counts, String[] keys,
 			JMenu menu) {
 		ButtonGroup group = new ButtonGroup();
 
 		class ChangeLanguage implements ActionListener {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				changeGameLanguage(((JRadioButtonMenuItem) e.getSource())
 						.getName());
 			}
 		}
 
 		for (int i = 0; i < langs.length; i++) {
 			JRadioButtonMenuItem rbMenuItem = new JRadioButtonMenuItem(
 					this.game.names.getString(langs[i]));
 			if (this.game.currentLocale.getCountry().equals(counts[i])) {
 				rbMenuItem.setSelected(true);
 			}
 			rbMenuItem.setName(keys[i]);
 			rbMenuItem.addActionListener(new ChangeLanguage());
 			group.add(rbMenuItem);
 			menu.add(rbMenuItem);
 		}
 
 	}
 
 	public ArrayList<ArrayList<Card>> getDefensiveCards(
 			ArrayList<Player> targets, Card clicked) {
 		ArrayList<ArrayList<Card>> playersReacts = new ArrayList<ArrayList<Card>>();
 		// cycles through targets
 		for (int i = 0; i < targets.size(); i++) {
 			ArrayList<Card> defends = new ArrayList<Card>();
 			ArrayList<Card> hand = targets.get(i).hand;
 			// cycles through hand
 			for (int j = 0; j < hand.size(); j++) {
 				Card card = hand.get(j);
 				// only runs if card is defensive
 				if (card.defense) {
 					if (((ReactionCard) card).canReactTo(clicked)) {
 						defends.add(card);
 					}
 				}
 			}
 			playersReacts.add(defends);
 		}
 		return playersReacts;
 	}
 
 	public boolean reactToPlay(Card clicked, ChoiceGroup choices) {
 		clicked.prepare(choices.getChoiceList(), this.game);
 		ArrayList<Player> targets = clicked.targets;
 		ArrayList<ArrayList<Card>> defends = getDefensiveCards(targets, clicked);
 		// cycles through targets
 		for (int i = 0; i < targets.size(); i++) {
 			ArrayList<Card> playersDefs = defends.get(i);
 			// cycles through hand
 			for (int j = 0; j < playersDefs.size(); j++) {
 				Card card = playersDefs.get(j);
 				// only runs if card is defensive
 				ReactionCard react = (ReactionCard) card;
 				boolean wantReact = cardReactInfo(card);
 				if (wantReact) {
 					ChoiceGroup reactChoices = react.getReactChoices(this.game);
 
 					Choice currents = reactChoices.getNextChoice();
 
 					boolean completeSoFarReact = cycleChoices(reactChoices,
 							currents, react,
 							this.game.players.indexOf(targets.get(i)));
 
 					// Uses card if you filled things out
 					if (completeSoFarReact) {
 						react.react(clicked, targets.get(i),
 								reactChoices.getChoiceList(), this.game);
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	public void chooseCharacters(int num, ArrayList<Integer> chtrs) {
 
 		for (int i = 0; i < num; i++) {
 			int m = chtrs.get(i);
 			this.game.setCharacter(i, m);
 		}
 
 	}
 
 	public void addMenuBar() {
 		// Create the menu bar.
 		JMenuBar menuBar = new JMenuBar();
 
 		// Build the first menu.
 		JMenu menu = new JMenu(this.game.names.getString("LangOption"));
 		menu.getAccessibleContext().setAccessibleDescription(
 				"The only menu in this program that has menu items");
 		menuBar.add(menu);
 		String[] langs = { "English", "French" };
 		String[] country = { "US", "FR" };
 		String[] keys = { "english", "french" };
 		makeLangOptions(langs, country, keys, menu);
 		frame.setJMenuBar(menuBar);
 	}
 
 }
