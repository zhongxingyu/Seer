 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Container;
 import java.awt.Dimension;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentEvent;
 import java.awt.event.ComponentListener;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 import java.util.Iterator;
 
 import javax.swing.JCheckBox;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.KeyStroke;
 
 public class CheckersGUI extends Frame implements ActionListener, ItemListener,
 		ComponentListener, GameStateListener {
 
 	private static Dimension DEFAULT_DIMENSION = new Dimension(1024, 1024);
 	private static final long serialVersionUID = 1L;
 
 	
 	private Container GUIContainer;
 	private DrawPanel boardPanel;
 	private DrawPanel scoreBoardPanel;
 	
 	private CheckersSettingsManager settingsManager; 
 	private boolean showStartPanel;
 
 	private GraphicCanvas boardCanvas;
 	private ScoreBoard scoreBoard;
 
 	private JMenuBar menuBar;
 	private JMenuItem undoAction;
 	private JCheckBox guideButton;
 	private JCheckBox forceJumpButton;
 	
 	private double guiRatio;
 	
 	private CheckersGame currentGame;
 
 	public CheckersGUI() throws FileIOException {
 		super(DEFAULT_DIMENSION, "Checkers");
 		this.setResizable(false);
 		
 		this.settingsManager = new CheckersSettingsManager();
 		
 		this.boardPanel = new DrawPanel();
 		this.boardCanvas = new GraphicCanvas();
 		this.boardPanel.setDrawable(boardCanvas);
 
 		this.scoreBoardPanel = new DrawPanel();
 		this.scoreBoard = new ScoreBoard();
 		GraphicCanvas bannerCanvas = new GraphicCanvas();
 		this.scoreBoardPanel.setDrawable(bannerCanvas);
 
 		this.addComponentListener(this);
 
 		GUIContainer = this.getContentPane();
 		GUIContainer.add(boardPanel, BorderLayout.CENTER);
 		GUIContainer.add(scoreBoardPanel, BorderLayout.NORTH);
 		
 		buildMenuBar();
 		
 		setVisible(true);
 		this.setLocation(50, 50);
 		new GUIMouseEventListener(this, boardPanel);
<<<<<<< HEAD
		rescale(.80);
=======
		
 		showStartPanel = true;
 		BufferedImage image = ThemeManager.getThemeManager().getImage("title");
 		boardCanvas.addDrawable("title", new Graphic(image, 0, 0, new Dimension(image.getWidth(), image.getHeight())));
 		image = ThemeManager.getThemeManager().getImage("barebanner");
 		bannerCanvas.addDrawable("barebanner", new Graphic(image, 0, 0, new Dimension(image.getWidth(), image.getHeight())));
 		
 		
		
		rescale(.50);
>>>>>>> Checkpoint - Bug Fixes, Title Graphics Added ...
 	}
 	
 	public boolean isShowingStart(){
 		return showStartPanel;
 	}
 	
 	public void itemStateChanged(ItemEvent e) {
 		Object source = e.getItemSelectable();
 
 		if (source == guideButton) {
 			if (e.getStateChange() == ItemEvent.DESELECTED) {
 				settingsManager.setMoveGuides(false);
 				if(currentGame != null){
 					currentGame.clearMoveHighlights();
 				}
 			} else if (e.getStateChange() == ItemEvent.SELECTED) {
 				settingsManager.setMoveGuides(true);
 				if(currentGame != null){
 					currentGame.updateAvailableMoves();
 				}
 			}
 		}
 		if(source == forceJumpButton){
 			if (e.getStateChange() == ItemEvent.DESELECTED) {
 				settingsManager.setForceJumps(false);
 				if(currentGame != null){
 					newGame(currentGame.getGameType());
 				}
 			} else if (e.getStateChange() == ItemEvent.SELECTED) {
 				settingsManager.setForceJumps(true);
 				if(currentGame != null){
 					newGame(currentGame.getGameType());
 				}
 			}
 			if(currentGame != null){
 				currentGame.updateAvailableMoves();
 			}
 		}
 		repaint();
 	}
 
 	public void actionPerformed(ActionEvent ae) {
 		String[] parse = ae.getActionCommand().split(":");
 		if (parse[0].equals("TwoPlayers")) {
 			newGame(GameType.TWO_PLAYERS);
 		} else if (parse[0].equals("aiEasy")) {
 			newGame(GameType.AI_EASY);
 		} else if (parse[0].equals("aiMedium")) {
 			newGame(GameType.AI_MEDIUM);
 		} else if (parse[0].equals("aiHard")) {
 			newGame(GameType.AI_HARD);
 		} else if (parse[0].equals("THEMES")) {
 			try {
 				updateTheme(parse[1]);
 			} catch (InvalidThemeException e) {
 				// Invalid Theme
 			}
 		} else if (parse[0].equals("Undo")) {
 			undoMove();
 		} else if (parse[0].equals("Exit")) {
 			System.exit(0);
 		}
 
 		repaint();
 	}
 
 	public CheckersGame getCurrentGame() {
 		return currentGame;
 	}
 
 	public GraphicCanvas getBoardCanvas() {
 		return boardCanvas;
 	}
 
 	public ScoreBoard getScoreBoard() {
 		return scoreBoard;
 	}
 
 	public double getRatio() {
 		return guiRatio;
 	}
 
 	public void componentHidden(ComponentEvent arg0) {
 	}
 
 	public void componentMoved(ComponentEvent arg0) {
 		repaint();
 	}
 
 	public void componentResized(ComponentEvent arg0) {
 		repaint();
 	}
 
 	public void componentShown(ComponentEvent arg0) {
 		repaint();
 	}
 
 	public void adjustScale(double offset) {
 		rescale(guiRatio + offset);
 	}
 
 	public void rescale(double ratio) {
 		this.guiRatio = ratio;
 		boardPanel.setRatio(ratio);
 		scoreBoardPanel.setRatio(ratio);
 		
 		int scaledScoreBoardWidth = (int) (Math.round(scoreBoard
 				.getOriginalSize().width * scoreBoardPanel.getRatio()));
 		int scaledScoreBoardHeight = (int) (Math.round(scoreBoard
 				.getOriginalSize().height * scoreBoardPanel.getRatio()));
 		scoreBoardPanel.setPreferredSize(new Dimension(scaledScoreBoardWidth,
 				scaledScoreBoardHeight));
 
 		Dimension windowBezelOffset = new Dimension(this.getWidth()
 				- boardPanel.getSize().width, this.getHeight()
 				- boardPanel.getSize().height
 				- scoreBoardPanel.getSize().height);
 
 		int newWidth = (int) (Math.round((DEFAULT_DIMENSION.width * boardPanel
 				.getRatio()) + windowBezelOffset.getWidth()));
 		int newHeight = (int) (Math
 				.round((DEFAULT_DIMENSION.height * boardPanel.getRatio())
 						+ scaledScoreBoardHeight
 						+ windowBezelOffset.getHeight()));
 		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
 
 		if ((newHeight > dim.getSize().height || newWidth > dim.getSize().width)) {
 			if (guiRatio > 0.20) {
 				rescale(ratio - 0.05);
 			}
 		} else if(guiRatio < 0.15){
 			rescale(0.15);
 		}
 		else {
 			this.setSize(newWidth, newHeight);
 		}
 	}
 
 	public void undoMove() {
 		if(currentGame != null){
 			currentGame.undo();
 			boardCanvas.addDrawable("board", getCurrentGame().getGameBoard());
 		}
 		this.repaint();
 	}
 
 	private void buildMenuBar() {
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		menuBar.add(buildGameMenu());
 		menuBar.add(buildOptionMenu());
 	}
 
 	private JMenu buildGameMenu() {
 		JMenu fileMenu = new JMenu("Game");
 		fileMenu.setMnemonic(KeyEvent.VK_G);
 		fileMenu.add(buildGameTypeSubMenu());
 
 		undoAction = new JMenuItem("Undo");
 		undoAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z,
 				ActionEvent.CTRL_MASK));
 		undoAction.addActionListener(this);
 		undoAction.setActionCommand("Undo");
 		undoAction.setEnabled(false);
 		undoAction.setForeground(Color.GRAY);
 		fileMenu.add(undoAction);
 
 		JMenuItem exitAction = new JMenuItem("Exit");
 		exitAction.addActionListener(this);
 		exitAction.setActionCommand("Exit");
 		exitAction.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q,
 				ActionEvent.CTRL_MASK));
 		fileMenu.addSeparator();
 		fileMenu.add(exitAction);
 
 		return fileMenu;
 	}
 
 	private JMenu buildGameTypeSubMenu() {
 		JMenu newAction = new JMenu("New");
 		newAction.setMnemonic(KeyEvent.VK_N);
 		JMenu onePlayer = new JMenu("One Player");
 		JMenuItem twoPlayer = new JMenuItem("Two Players");
 		twoPlayer.addActionListener(this);
 		twoPlayer.setActionCommand("TwoPlayers");
 
 		JMenuItem aiEasy = new JMenuItem("Easy");
 		aiEasy.addActionListener(this);
 		aiEasy.setActionCommand("aiEasy");
 		onePlayer.add(aiEasy);
 
 		JMenuItem aiMedium = new JMenuItem("Moderate");
 		aiMedium.addActionListener(this);
 		aiMedium.setActionCommand("aiMedium");
 		onePlayer.add(aiMedium);
 
 		JMenuItem aiHard = new JMenuItem("Hard");
 		aiHard.addActionListener(this);
 		aiHard.setActionCommand("aiHard");
 		onePlayer.add(aiHard);
 
 		newAction.add(onePlayer);
 		newAction.add(twoPlayer);
 		return newAction;
 
 	}
 
 	private JMenu buildOptionMenu() {
 		JMenu optionMenu = new JMenu("Options");
 		optionMenu.setMnemonic(KeyEvent.VK_O);
 		
 
 		guideButton = new JCheckBox("Show Guide");
 		guideButton.addItemListener(this);
 		guideButton.setMnemonic(KeyEvent.VK_G);
 		guideButton.setSelected(false);
 		optionMenu.add(guideButton);
 		
 		forceJumpButton = new JCheckBox("Force Jumps");
 		forceJumpButton.addItemListener(this);
 		forceJumpButton.setMnemonic(KeyEvent.VK_F);
 		forceJumpButton.setToolTipText("Requires jumps to be made. Restarts game!");
 		forceJumpButton.setSelected(false);
 		optionMenu.add(forceJumpButton);
 		
 		optionMenu.addSeparator();
 		optionMenu.add(buildThemeMenu());
 	
 		return optionMenu;
 
 	}
 
 	private JMenu buildThemeMenu() {
 		JMenu themeMenu = new JMenu("Themes");
 		themeMenu.setMnemonic(KeyEvent.VK_T);
 
 		Iterator<String> themeIterator = ThemeManager.getThemeManager()
 				.iterator();
 		while (themeIterator.hasNext()) {
 			String theme = themeIterator.next();
 			JMenuItem item = new JMenuItem(theme);
 			themeMenu.add(item);
 			item.addActionListener(this);
 			item.setActionCommand("THEME:" + theme);
 		}
 		try {
 			updateTheme(ThemeManager.getThemeManager().getThemes().get(0));
 		} catch (InvalidThemeException e) {
 			System.exit(0);
 		}
 		return themeMenu;
 	}
 
 	public void newGame(GameType type) {
 
 		if(showStartPanel){
 			this.scoreBoardPanel.setDrawable(scoreBoard);
 			rescale(guiRatio);
 			boardCanvas.removeDrawable("title");
 			showStartPanel = false;
 			
 		}
 		currentGame = new CheckersGame(type, settingsManager);
 		boardCanvas.addDrawable("board", getCurrentGame().getGameBoard());
 		currentGame.addStateListener(this.scoreBoard);
 		currentGame.addStateListener(this);
 		currentGame.updateAvailableMoves();
 
 	}
 
 	private void updateTheme(String themeID) throws InvalidThemeException {
 		String selectedTheme = themeID;
 		if (selectedTheme != null) {
 			ThemeManager.getThemeManager().setTheme(selectedTheme);
 			ThemeManager.getThemeManager().updateTheme();
 		}
 		repaint();
 	}
 
 	@Override
 	public void boardChange(CheckersGame game) {
 		if(currentGame == null){
 			return;
 		}
 		
 		if(currentGame.getGameHistory().isEmpty()){
 			undoAction.setEnabled(false);
 			undoAction.setForeground(Color.GRAY);
 		} else {
 			undoAction.setEnabled(true);
 			undoAction.setForeground(Color.DARK_GRAY);
 		}
 		
 	}
 
 	@Override
 	public void gameOver(BlockOccupant player) {
 		// TODO Auto-generated method stub
 		
 	}
 }
