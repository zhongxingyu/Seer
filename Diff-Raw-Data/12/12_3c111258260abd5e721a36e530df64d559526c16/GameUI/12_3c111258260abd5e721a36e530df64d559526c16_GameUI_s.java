 package ui;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Point;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 
 import javax.swing.AbstractButton;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSlider;
 import javax.swing.JTextField;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 
 import model.Game;
 import model.GameState;
 import model.Meeple;
 import model.Player;
 import model.Tile;
 
 /**
  * @author Andrew Wylie <andrew.dale.wylie@gmail.com>
  * @version 1.0
  * @since 2012-06-28
  */
 public class GameUI extends JFrame implements ActionListener, MouseListener {
 	private static final long serialVersionUID = 1L;
 	private Game game = null;
 
 	// Each menu screen (and the game screen) is contained within their own
 	// JPanel's, which are set as the frame content pane when appropriate.
 
 	// UI settings.
 	private String title = "Carcassonne";
	private Image icon = Toolkit.getDefaultToolkit().getImage("icon.png");
 
 	private String[] windowedResolutions = { "800 x 600", "1024 x 768",
 			"1280 x 720", "1280 x 960", "1280 x 1024", "1360 x 768",
 			"1600 x 900", "1680 x 1050", "1920 x 1080" };
 
 	private String[] windowedSettings = { "Fullscreen", "Windowed",
 			"Borderless Window" };
 
 	private String currentWindowedMode = "Windowed";
 	private Dimension currentWindowedResolution = new Dimension(800, 600);
 	private int currentVolume = 25;
 	private boolean currentSoundEnabled = false;
 
 	// Main JPanels which are to be swapped as the frame content.
 	private JPanel titleScreenContentPane;
 	private JPanel optionsScreenContentPane;
 	private JPanel gameLobbyContentPane;
 	private JPanel gameContentPane;
 
 	// And other ui elements which need to be declared globally.
 	private JComboBox windowedResolutionDropDown;
 	private JLabel volumeSliderLabel;
 	private JCanvas gameBoardWindow;
 	private JCanvas currentTilePanel;
 	private JTextField numPlayersTextField;
 	private JButton endTurnButton;
 
 	public GameUI() {
 		// Initialize game menus.
 		// The game screen is initialized after we get the game parameters from
 		// the user as we can then create the players list for scoring.
 		this.initTitleScreen();
 		this.initOptionsScreen();
 		this.initLobbyScreen();
 
 		// Set the title bar and icon.
 		this.setTitle(title);
		this.setIconImage(icon);
 
 		// Show the title screen to begin with.
 		this.setContentPane(this.titleScreenContentPane);
 
 		// Other frame/program settings.
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		this.setResizable(false);
 		this.setPreferredSize(this.currentWindowedResolution);
 		this.pack();
 		this.setVisible(true);
 
 		// TODO: begin to play game music if enabled.
 
 		// Set the frame to center on-screen.
 		this.setLocationRelativeTo(null);
 	}
 
 	// State machine for menus:
 	//
 	// title
 	// - options
 	// - lobby (play)
 	// - quit
 	//
 	// options
 	// - title
 	// - change music
 	// - change video
 	// - change sound
 	//
 	// lobby
 	// - title
 	// - game options
 	// - play
 	//
 	// play
 	// -title
 	// -quit
 	//
 	private void initTitleScreen() {
 		this.titleScreenContentPane = new JPanel(new BorderLayout());
 
 		JLabel titleLabel = new JLabel("Carcassonne");
 		titleLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		titleLabel.setHorizontalTextPosition(JLabel.CENTER);
 		titleLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		JButton playButton = new JButton("Multiplayer");
 		playButton.setVerticalTextPosition(AbstractButton.CENTER);
 		playButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		playButton.setMnemonic(KeyEvent.VK_M);
 		playButton.setActionCommand("startLobby");
 		playButton.addActionListener(this);
 
 		JButton optionsButton = new JButton("Options");
 		optionsButton.setVerticalTextPosition(AbstractButton.CENTER);
 		optionsButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		optionsButton.setMnemonic(KeyEvent.VK_O);
 		optionsButton.setActionCommand("showOptionsScreen");
 		optionsButton.addActionListener(this);
 
 		JButton exitButton = new JButton("Quit Game");
 		exitButton.setVerticalTextPosition(AbstractButton.CENTER);
 		exitButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		exitButton.setMnemonic(KeyEvent.VK_Q);
 		exitButton.setActionCommand("exitGame");
 		exitButton.addActionListener(this);
 
 		// After creating the components, place them on the title screen.
 		JPanel titleContainer = new JPanel(new GridBagLayout());
 
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 
 		gc.gridx = 0;
 		gc.gridy = 0;
 		gc.gridwidth = 2;
 		gc.gridheight = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		titleContainer.add(playButton, gc);
 		gc.gridy = 1;
 		gc.gridwidth = 1;
 		gc.fill = GridBagConstraints.NONE;
 		gc.insets = new Insets(2, 2, 20, 2);
 		titleContainer.add(optionsButton, gc);
 		gc.gridx = 1;
 		titleContainer.add(exitButton, gc);
 
 		this.titleScreenContentPane.add(titleLabel, BorderLayout.PAGE_START);
 		this.titleScreenContentPane.add(titleContainer, BorderLayout.PAGE_END);
 	}
 
 	private void initOptionsScreen() {
 		this.optionsScreenContentPane = new JPanel(new BorderLayout());
 
 		JLabel optionsLabel = new JLabel("Options");
 		optionsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		optionsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		optionsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		JLabel videoSettingsLabel = new JLabel("Video");
 		videoSettingsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		videoSettingsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		videoSettingsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		JLabel audioSettingsLabel = new JLabel("Sound");
 		audioSettingsLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		audioSettingsLabel.setHorizontalTextPosition(JLabel.CENTER);
 		audioSettingsLabel.setHorizontalAlignment(JLabel.CENTER);
 
 		windowedResolutionDropDown = new JComboBox(this.windowedResolutions);
 		windowedResolutionDropDown.setSelectedItem("800 x 600");
 		windowedResolutionDropDown.setActionCommand("changeResolution");
 		windowedResolutionDropDown.addActionListener(this);
 
 		JComboBox windowedModeDropDown = new JComboBox(this.windowedSettings);
 		windowedModeDropDown.setSelectedItem("Windowed");
 		windowedModeDropDown.setActionCommand("changeWindowedMode");
 		windowedModeDropDown.addActionListener(this);
 
 		// TODO MUSIC
 		JPanel volumeContainer = new JPanel(new BorderLayout());
 		volumeContainer.setPreferredSize(new Dimension(240, 20));
 
 		volumeSliderLabel = new JLabel(this.currentVolume + "%");
 		volumeSliderLabel.setVerticalTextPosition(JLabel.BOTTOM);
 		volumeSliderLabel.setHorizontalTextPosition(JLabel.CENTER);
 		volumeSliderLabel.setHorizontalAlignment(JLabel.RIGHT);
 
 		JSlider volumeSlider = new JSlider(JSlider.HORIZONTAL, 0, 100,
 				this.currentVolume);
 
 		volumeSlider.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				JSlider s = (JSlider) e.getSource();
 				volumeSliderLabel.setText(s.getValue() + "%");
 			}
 		});
 
 		// Sub-placement of volume slider and label in the volume container.
 		volumeContainer.add(volumeSlider, BorderLayout.WEST);
 		volumeContainer.add(volumeSliderLabel, BorderLayout.EAST);
 
 		JButton soundToggleButton = new JButton("Music: "
 				+ (this.currentSoundEnabled ? "ON" : "OFF"));
 
 		soundToggleButton.setVerticalTextPosition(AbstractButton.CENTER);
 		soundToggleButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		soundToggleButton.setMnemonic(KeyEvent.VK_M);
 		soundToggleButton.setActionCommand("toggleSound");
 		soundToggleButton.addActionListener(this);
 
 		JButton backButton = new JButton("Back");
 		backButton.setVerticalTextPosition(AbstractButton.CENTER);
 		backButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		backButton.setMnemonic(KeyEvent.VK_B);
 		backButton.setActionCommand("showTitleScreen");
 		backButton.addActionListener(this);
 
 		// Now, place the created components in the container.
 		JPanel optionsContainer = new JPanel(new GridBagLayout());
 
 		GridBagConstraints gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 
 		gc.gridx = 0;
 		gc.gridy = 0;
 		gc.gridwidth = 1;
 		gc.gridheight = 1;
 		optionsContainer.add(videoSettingsLabel);
 		gc.gridx = 1;
 		optionsContainer.add(audioSettingsLabel);
 		gc.gridx = 0;
 		gc.gridy = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		optionsContainer.add(windowedResolutionDropDown, gc);
 		gc.gridy = 2;
 		optionsContainer.add(windowedModeDropDown, gc);
 		gc.gridx = 1;
 		gc.gridy = 1;
 		optionsContainer.add(volumeContainer, gc);
 		gc.gridy = 2;
 		optionsContainer.add(soundToggleButton, gc);
 		gc.gridy = 3;
 		gc.insets = new Insets(2, 2, 20, 2);
 		optionsContainer.add(backButton, gc);
 
 		optionsScreenContentPane.add(optionsLabel, BorderLayout.PAGE_START);
 		optionsScreenContentPane.add(optionsContainer, BorderLayout.PAGE_END);
 	}
 
 	// Allow the players to choose colors, pick how many of them are playing.
 	private void initLobbyScreen() {
 		// TODO: improve this screen!
 		this.gameLobbyContentPane = new JPanel(new BorderLayout());
 
 		// number of players input
 		numPlayersTextField = new JTextField("3", 2);
 		numPlayersTextField.addActionListener(this);
 
 		// Back button
 		JButton backButton = new JButton("Back");
 		backButton.setVerticalTextPosition(AbstractButton.CENTER);
 		backButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		backButton.setMnemonic(KeyEvent.VK_B);
 		backButton.setActionCommand("showTitleScreen");
 		backButton.addActionListener(this);
 
 		// Start game button
 		JButton startButton = new JButton("Start Game");
 		startButton.setVerticalTextPosition(AbstractButton.CENTER);
 		startButton.setHorizontalTextPosition(AbstractButton.CENTER);
 		startButton.setMnemonic(KeyEvent.VK_S);
 		startButton.setActionCommand("startGame");
 		startButton.addActionListener(this);
 
 		this.gameLobbyContentPane.add(backButton, BorderLayout.PAGE_START);
 		this.gameLobbyContentPane.add(numPlayersTextField, BorderLayout.CENTER);
 		this.gameLobbyContentPane.add(startButton, BorderLayout.PAGE_END);
 	}
 
 	private ArrayList<JLabel> playerScoreArray = new ArrayList<JLabel>();
 	private ArrayList<JLabel> playerLabelArray = new ArrayList<JLabel>();
 
 	private void initGameScreen() {
 		this.gameContentPane = new JPanel(new BorderLayout());
 
 		int tileSize = Tile.tileSize * Tile.tileTypeSize;
 		int gameBoardWidth = game.getBoardWidth() * tileSize;
 		int gameBoardHeight = game.getBoardHeight() * tileSize;
 		Point scrollPos = new Point(gameBoardWidth / 2, gameBoardHeight / 2);
 
 		gameBoardWindow = new JCanvas(gameBoardWidth, gameBoardHeight);
 		JScrollPane gameBoardWindowScrollPane = new JScrollPane(gameBoardWindow);
 		gameBoardWindowScrollPane.getViewport().setViewPosition(scrollPos);
 
 		// Add mouse listener for game window.
 		this.gameBoardWindow.addMouseListener(this);
 
 		JPanel infoContainer = new JPanel(new BorderLayout());
 		GridBagConstraints gc;
 
 		// Top part of the info window has the score information.
 		JPanel scoreInfoWindow = new JPanel(new GridBagLayout());
 
 		// Create the player scoring list.
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 		gc.gridx = 0;
 		gc.gridy = 0;
 
 		// TODO factor out a jlabel playerscore class?
 
 		for (int i = 0; i < this.game.getNumPlayers(); i++) {
 
 			JLabel playerName = new JLabel("Player " + (i + 1) + ":");
 			JLabel playerScore = new JLabel(""
 					+ this.game.getPlayers()[i].getScore());
 			playerLabelArray.add(playerName);
 			playerScoreArray.add(playerScore);
 
 			scoreInfoWindow.add(playerName, gc);
 			gc.gridx++;
 			scoreInfoWindow.add(playerScore, gc);
 			gc.gridy++;
 			gc.gridx = 0;
 		}
 
 		// Bottom part of the info window has the controls.
 		JPanel controlsWindow = new JPanel(new GridBagLayout());
 
 		// Zooming.
 		JButton zoomInButton = new JButton("+");
 		zoomInButton.setActionCommand("zoomIn");
 		zoomInButton.addActionListener(this);
 
 		JButton zoomOutButton = new JButton("-");
 		zoomOutButton.setActionCommand("zoomOut");
 		zoomOutButton.addActionListener(this);
 
 		// Tile Rotation.
 		JButton rotateCWButton = new JButton("--\\");
 		rotateCWButton.setActionCommand("rotateCW");
 		rotateCWButton.addActionListener(this);
 
 		JButton rotateCCWButton = new JButton("/--");
 		rotateCCWButton.setActionCommand("rotateCCW");
 		rotateCCWButton.addActionListener(this);
 
 		// Draw Pile.
		ImageIcon drawTileImageIcon = new ImageIcon("tile-back.jpg");
 		JButton drawTileButton = new JButton(drawTileImageIcon);
 		drawTileButton.setActionCommand("drawTile");
 		drawTileButton.addActionListener(this);
 
 		// Current Tile.
 		currentTilePanel = new JCanvas(tileSize, tileSize);
 
 		// End turn button.
 		// Action for this button should only be allowed to let the player
 		// skip placing their meeple.
 		endTurnButton = new JButton("End my turn");
 		endTurnButton.setActionCommand("endTurn");
 		endTurnButton.addActionListener(this);
 
 		// Fill in the info window controls.
 		gc = new GridBagConstraints();
 		gc.insets = new Insets(2, 2, 2, 2);
 
 		gc.gridx = 0;
 		gc.gridy = 0;
 		gc.gridwidth = 2;
 		gc.gridheight = 2;
 		gc.fill = GridBagConstraints.NONE;
 		controlsWindow.add(drawTileButton, gc);
 		gc.gridx = 2;
 		gc.gridy = 0;
 		gc.gridwidth = 1;
 		gc.gridheight = 1;
 		controlsWindow.add(zoomInButton, gc);
 		gc.gridy = 1;
 		controlsWindow.add(zoomOutButton, gc);
 		gc.gridx = 0;
 		gc.gridy = 2;
 		controlsWindow.add(rotateCCWButton, gc);
 		gc.gridx = 2;
 		controlsWindow.add(rotateCWButton, gc);
 		gc.gridx = 0;
 		gc.gridy = 3;
 		gc.gridwidth = 3;
 		gc.gridheight = 3;
 		controlsWindow.add(currentTilePanel, gc);
 		gc.gridx = 0;
 		gc.gridy = 6;
 		gc.gridwidth = 3;
 		gc.gridheight = 1;
 		gc.fill = GridBagConstraints.HORIZONTAL;
 		controlsWindow.add(endTurnButton, gc);
 
 		// Add everything to everything in the info container.
 		gameContentPane.add(gameBoardWindowScrollPane, BorderLayout.CENTER);
 		gameContentPane.add(infoContainer, BorderLayout.EAST);
 		infoContainer.add(scoreInfoWindow, BorderLayout.NORTH);
 		infoContainer.add(controlsWindow, BorderLayout.SOUTH);
 	}
 
 	private GameState gameState = null;
 	private Player currentPlayer;
 	private int currentPlayerIdx = 0;
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 
 		// Actions for gameplay.
 		if (this.gameState != null) {
 
 			// Event handlers for tile rotation.
 			if ("rotateCW".equals(e.getActionCommand())
 					&& currentPlayer.getCurrentTile() != null) {
 
 				currentPlayer.getCurrentTile().rotateClockwise();
 				this.currentTilePanel.repaint();
 			}
 
 			if ("rotateCCW".equals(e.getActionCommand())
 					&& currentPlayer.getCurrentTile() != null) {
 
 				currentPlayer.getCurrentTile().rotateCounterClockwise();
 				this.currentTilePanel.repaint();
 			}
 
 			// Allow a player to end their turn if they don't want to place a
 			// meeple.
 			if ("endTurn".equals(e.getActionCommand())
 					&& this.gameState == GameState.PLACE_MEEPLE) {
 				this.gameState = GameState.DRAW_TILE;
 				this.endTurn();
 			}
 
 			// Each turn begins with a player drawing a tile from the draw
 			// pile. Here we allow a player to draw the tile, and after they
 			// have we draw it to the screen on the current tile panel.
 			if ("drawTile".equals(e.getActionCommand())
 					&& gameState == GameState.DRAW_TILE) {
 
 				game.drawTile(currentPlayer);
 				Tile tileToPlace = currentPlayer.getCurrentTile();
 
 				tileToPlace.setx(0);
 				tileToPlace.sety(0);
 				this.currentTilePanel.add(tileToPlace);
 				this.currentTilePanel.repaint();
 
 				gameState = GameState.PLACE_TILE;
 
 			}
 
 		}
 
 		// Main screen.
 		if ("showOptionsScreen".equals(e.getActionCommand())) {
 			this.setContentPane(this.optionsScreenContentPane);
 			this.validate();
 			this.repaint();
 
 		} else if ("exitGame".equals(e.getActionCommand())) {
 			System.exit(0);
 
 		} else if ("startLobby".equals(e.getActionCommand())) {
 			this.setContentPane(this.gameLobbyContentPane);
 			this.validate();
 			this.repaint();
 		}
 
 		// Lobby screen.
 		if ("startGame".equals(e.getActionCommand())) {
 
 			// Get number of players
 			int numPlayers = 0;
 
 			try {
 				numPlayers = Integer.parseInt(numPlayersTextField.getText());
 
 			} catch (Exception ex) {
 				return;
 			}
 
 			if (numPlayers > 5 || numPlayers < 1) {
 				// TODO: better error handling
 				// TODO: format numplayers.
 				JOptionPane.showMessageDialog(this, "Can't have a game with "
 						+ numPlayers + " players.");
 
 				return;
 			}
 
 			// Start the game.
 			this.game = new Game(numPlayers);
 
 			// Now that we know the number of players we can create the game
 			// screen.
 			this.initGameScreen();
 
 			// If the game has just started then transition to the first 'real'
 			// game state. We choose the first player to play and start.
 			this.gameState = GameState.DRAW_TILE;
 			currentPlayer = game.getPlayers()[currentPlayerIdx];
 			this.showCurrentPlayer(currentPlayerIdx);
 			this.endTurnButton.setEnabled(false);
 
 			// And switch to the correct content pane.
 			this.setContentPane(this.gameContentPane);
 			this.validate();
 			this.repaint();
 		}
 
 		// Options screen.
 		if ("showTitleScreen".equals(e.getActionCommand())) {
 			this.setContentPane(this.titleScreenContentPane);
 			this.validate();
 			this.repaint();
 
 		} else if ("changeWindowedMode".equals(e.getActionCommand())) {
 			// Get the users selection.
 			JComboBox cb = (JComboBox) e.getSource();
 			String mode = (String) cb.getSelectedItem();
 
 			if ("Fullscreen".equals(mode)
 					&& !"Fullscreen".equals(this.currentWindowedMode)) {
 				// Fullscreen the window by removing the title bar, resizing
 				// to the screen size, and moving to fill the screen.
 				this.dispose();
 				this.setUndecorated(true);
 				this.setSize(Toolkit.getDefaultToolkit().getScreenSize());
 				this.setLocation(0, 0);
 				this.setVisible(true);
 
 				// Disable the resolution setting.
 				this.windowedResolutionDropDown.setEnabled(false);
 
 			} else if (!"Fullscreen".equals(mode)) {
 				this.dispose();
 				this.setUndecorated("Borderless Window".equals(mode));
 				this.setVisible(true);
 
 				// Change the resolution to match the current selection.
 				this.setSize(this.currentWindowedResolution);
 
 				if ("Fullscreen".equals(this.currentWindowedMode)) {
 					this.setLocationRelativeTo(null);
 
 					// Re-enable the resolution setting.
 					this.windowedResolutionDropDown.setEnabled(true);
 				}
 			}
 
 			this.currentWindowedMode = mode;
 
 		} else if ("changeResolution".equals(e.getActionCommand())
 				&& this.currentWindowedMode != "Fullscreen") {
 			JComboBox cb = (JComboBox) e.getSource();
 			String res = (String) cb.getSelectedItem();
 
 			String[] resArray = res.split(" ");
 
 			int width = Integer.parseInt(resArray[0]);
 			int height = Integer.parseInt(resArray[2]);
 
 			this.setSize(width, height);
 			this.currentWindowedResolution = new Dimension(width, height);
 
 		} else if ("toggleSound".equals(e.getActionCommand())) {
 			JButton b = (JButton) e.getSource();
 
 			if (this.currentSoundEnabled) {
 				b.setText("Music: OFF");
 				this.currentSoundEnabled = false;
 			} else {
 				b.setText("Music: ON");
 				this.currentSoundEnabled = true;
 				// Code to play song file; looped.
 			}
 
 		}
 
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 
 		// Detect if there has been a mouse click on the board canvas object.
 		if (e.getComponent() == this.gameBoardWindow) {
 
 			if (gameState == null) {
 				return;
 			}
 
 			int err = 0;
 
 			// We'll do some click calculations outside of the state-specific
 			// checks to prevent code duplication. First get the clicked
 			// position, and then convert it to a tile location.
 			int xPos = e.getX();
 			int yPos = e.getY();
 
 			// Example:
 			// x click is 52, with each tile having 5 TileType's at size
 			// 10 each. Then the player has clicked 52 / 5*10 = 1st tile.
 			// Our array is zero-indexed, so this is correct. Otherwise we'd
 			// get the mathematical ceiling.
 			int xBoard = xPos / (Tile.tileTypeSize * Tile.tileSize);
 			int yBoard = yPos / (Tile.tileTypeSize * Tile.tileSize);
 
 			int xTile = (xPos % (Tile.tileTypeSize * Tile.tileSize))
 					/ Tile.tileTypeSize;
 			int yTile = (yPos % (Tile.tileTypeSize * Tile.tileSize))
 					/ Tile.tileTypeSize;
 
 			// Check that the proper game state is selected. Here we are
 			// looking for the tile placement state.
 			if (gameState == GameState.PLACE_TILE) {
 
 				// If this is the first turn we want to force the position so
 				// that the tile is placed in the center of the game board.
 				if (!this.game.hasGameStarted()) {
 
 					// Set the click point.
 					xBoard = 76;
 					yBoard = 76;
 				}
 
 				// Place the tile.
 				Tile tileToPlace = currentPlayer.getCurrentTile();
 				err = game.placeTile(currentPlayer, xBoard, yBoard);
 
 				// If no error draw the tile on the gameboard and remove it
 				// from the currentTile area.
 				if (err == 0) {
 					// UI code.
 					int tileSize = Tile.tileTypeSize * Tile.tileSize;
 					tileToPlace.setx(xBoard * tileSize);
 					tileToPlace.sety(yBoard * tileSize);
 
 					this.gameBoardWindow.add(tileToPlace);
 					this.currentTilePanel.clear();
 
 					this.gameBoardWindow.repaint();
 					this.currentTilePanel.repaint();
 
 					gameState = GameState.PLACE_MEEPLE;
 				} else {
 					// TODO: better error handling
 					JOptionPane.showMessageDialog(this,
 							"Can't place tile there.");
 				}
 
 				// Check if the player can place any meeples.
 				// If not, end their turn.
 				if (this.game.getNumMeeplesPlaced(currentPlayer) == 7) {
 
 					gameState = GameState.SCORE_PLAYERS;
 					this.endTurn();
 					gameState = GameState.DRAW_TILE;
 				} else {
 					this.endTurnButton.setEnabled(true);
 				}
 
 				return;
 			}
 
 			// Here we are looking for the meeple placement state.
 			if (gameState == GameState.PLACE_MEEPLE) {
 
 				// Place the meeple.
 				err = game.placeMeeple(currentPlayer, xBoard, yBoard, xTile,
 						yTile);
 
 				if (err == 0) {
 					Meeple m = game.getMeeple(xBoard, yBoard, xTile, yTile);
 
 					// UI code.
 					int tileSize = Tile.tileTypeSize * Tile.tileSize;
 					m.setx((xBoard * tileSize) + (xTile * Tile.tileTypeSize));
 					m.sety((yBoard * tileSize) + (yTile * Tile.tileTypeSize));
 
 					this.gameBoardWindow.add(m);
 					this.gameBoardWindow.repaint();
 
 					gameState = GameState.SCORE_PLAYERS;
 
 					this.endTurn();
 
 					gameState = GameState.DRAW_TILE;
 
 				} else {
 					// TODO: better error handling
 					JOptionPane.showMessageDialog(this,
 							"Can't place meeple there");
 				}
 
 				return;
 			}
 
 		}
 
 	}
 
 	private void scoreGame(boolean hasGameEnded) {
 
 		ArrayList<Meeple> removedMeeples = game.score(hasGameEnded);
 
 		for (int i = 0; i < removedMeeples.size(); i++) {
 			gameBoardWindow.remove(removedMeeples.get(i));
 		}
 
 		this.gameBoardWindow.repaint();
 
 		// Update player scores on the ui.
 		for (int i = 0; i < game.getNumPlayers(); i++) {
 
 			int playerScore = game.getPlayers()[i].getScore();
 			playerScoreArray.get(i).setText("" + playerScore);
 			playerScoreArray.get(i).repaint();
 		}
 
 	}
 
 	private void endTurn() {
 
 		scoreGame(false);
 
 		// After scoring in-game events, check if the draw pile is
 		// empty. If so then we want to do end game scoring, and
 		// then end the game.
 		if (this.game.isDrawPileEmpty()) {
 
 			gameState = GameState.GAME_END;
 			scoreGame(true);
 			// TODO: some shiny end-game notification or what-have-you
 		}
 
 		currentPlayerIdx = (currentPlayerIdx + 1) % game.getNumPlayers();
 		currentPlayer = game.getPlayers()[currentPlayerIdx];
 		this.showCurrentPlayer(currentPlayerIdx);
 		this.endTurnButton.setEnabled(false);
 	}
 
 	private void showCurrentPlayer(int playerIndex) {
 		// un-bold all players
 		for (int i = 0; i < playerLabelArray.size(); i++) {
 			playerLabelArray.get(i).setFont(getFont().deriveFont(Font.PLAIN));
 		}
 
 		// bold the player whose turn it currently is.
 		playerLabelArray.get(playerIndex).setFont(
 				getFont().deriveFont(Font.BOLD));
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 	}
 
 	public static void main(String[] args) {
 		// Schedule a job for the event-dispatching thread:
 		// creating and showing this application's GUI.
 		javax.swing.SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				GameUI game = new GameUI();
 			}
 		});
 	}
 
 }
