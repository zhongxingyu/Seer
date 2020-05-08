 package src.ui;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BorderFactory;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.SwingWorker;
 import javax.swing.event.ListSelectionEvent;
 
 import src.FilePaths;
 import src.net.AvailableGame;
 import src.net.LobbyManager;
 import src.ui.controller.MultiplayerController;
 
 public class Lobby extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private static final String lobbyText = "Multiplayer Lobby";
 	private static final String[] columnHeaders = {"Game Name", "Host", "Map Name"};
 	
 	private JLabel lobbyLabel;
 	private JLabel usernameLabel;
 	
 	private JTextField usernameField;
 	private JScrollPane gameTableScrollPane;
 	private JTable gameTable;
 	
 	private JButton refreshButton;
 	private JButton exitButton;
 	private JButton createGameButton;
 	private JButton joinButton;
 	
 	private MultiplayerController controller;
 	
 	private ImageIcon background;
 	
 	private ImageIcon mainMenuIcon;
 	private ImageIcon mainMenuPressedIcon;
 	private ImageIcon mainMenuHoverIcon;
 
 	private ImageIcon refreshIcon;
 	private ImageIcon refreshPressedIcon;
 	private ImageIcon refreshHoverIcon;
 	
 	private ImageIcon createIcon;
 	private ImageIcon createPressedIcon;
 	private ImageIcon createHoverIcon;
 	private ImageIcon createDisabledIcon;
 	
 	private ImageIcon joinIcon;
 	private ImageIcon joinPressedIcon;
 	private ImageIcon joinHoverIcon;
 	private ImageIcon joinDisabledIcon;
 	
 	public Lobby(MultiplayerController multiController) {
 		super(new GridBagLayout());
 		setSize(800, 600);
 		
 		background = new ImageIcon(FilePaths.bgPath + "GenericBGRDsmall.png");
 		
 		mainMenuIcon = new ImageIcon(FilePaths.buttonPath + "MPMenuButton.png");
 		mainMenuPressedIcon = new ImageIcon(FilePaths.buttonPath + "MPMenuButtonDown.png");
 		mainMenuHoverIcon = new ImageIcon(FilePaths.buttonPath + "MPMenuButtonHover.png");
 
 		refreshIcon = new ImageIcon(FilePaths.buttonPath + "RefreshButton.png");
 		refreshPressedIcon = new ImageIcon(FilePaths.buttonPath + "RefreshButtonDown.png");
 		refreshHoverIcon = new ImageIcon(FilePaths.buttonPath + "RefreshButtonHover.png");
 		
 		createIcon = new ImageIcon(FilePaths.buttonPath + "CreateGameButton.png");
 		createPressedIcon = new ImageIcon(FilePaths.buttonPath + "CreateGameButtonDown.png");
 		createHoverIcon = new ImageIcon(FilePaths.buttonPath + "CreateGameButtonHover.png");
 		createDisabledIcon = new ImageIcon(FilePaths.buttonPath + "CreateGameButtonDisabled.png");
 		
 		joinIcon = new ImageIcon(FilePaths.buttonPath + "JoinGameButton.png");
 		joinPressedIcon = new ImageIcon(FilePaths.buttonPath + "JoinGameButtonDown.png");
 		joinHoverIcon = new ImageIcon(FilePaths.buttonPath + "JoinGameButtonHover.png");
 		joinDisabledIcon = new ImageIcon(FilePaths.buttonPath + "JoinGameButtonDisabled.png");
 		
 		this.controller = multiController;
 		
 		gameTableScrollPane = new JScrollPane();
 		
 		lobbyLabel = new JLabel(lobbyText);
 		lobbyLabel.setForeground(Color.WHITE);
 		lobbyLabel.setFont(new Font("lobbyFont", Font.BOLD, 16));
 		
 		usernameLabel = new JLabel("Username: ");
 		usernameLabel.setForeground(Color.WHITE);
 		usernameLabel.setFont(new Font("lobbyFont", Font.BOLD, 12));
 
 		usernameField = new JTextField(13);
 		updateGameListPane();
 		
 		usernameField.addKeyListener(new KeyAdapter() {
 			public void keyTyped(KeyEvent e) {
 				char[] key = {e.getKeyChar()};
 				String charString = new String(key);
 				
 				if (charString.matches("[^a-zA-Z0-9]") && e.getKeyChar() != KeyEvent.VK_BACK_SPACE) {
 					e.consume();
 				} else {
 					JTextField field = (JTextField) e.getSource();
 					String uname = "";
 
 					if (e.getKeyChar() == KeyEvent.VK_BACK_SPACE && field.getText().length() > 0) {
 						uname = field.getText();
 					} else {
 						uname = field.getText() + e.getKeyChar();
 					}
 
 					controller.setUsername(uname);
 					updateAllowedButtons(uname);
 				}
 			}
 		});
 		
 		refreshButton = new JButton(refreshIcon);
 		refreshButton.setBorder(BorderFactory.createEmptyBorder());
 		refreshButton.setContentAreaFilled(false);
 		refreshButton.setPressedIcon(refreshPressedIcon);
 		refreshButton.setRolloverIcon(refreshHoverIcon);
 		
 		refreshButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				updateGameListPane();
 			}
 		});
 		
 		exitButton = new JButton(mainMenuIcon); 
 		exitButton.setBorder(BorderFactory.createEmptyBorder());
 		exitButton.setContentAreaFilled(false);
 		exitButton.setPressedIcon(mainMenuPressedIcon);
 		exitButton.setRolloverIcon(mainMenuHoverIcon);
 		
 		exitButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.exitLobby();
 			}
 		});
 		
 		createGameButton = new  JButton(createIcon);
 		createGameButton.setBorder(BorderFactory.createEmptyBorder());
 		createGameButton.setContentAreaFilled(false);
 		createGameButton.setPressedIcon(createPressedIcon);
 		createGameButton.setRolloverIcon(createHoverIcon);
 		createGameButton.setDisabledIcon(createDisabledIcon);
 		createGameButton.setEnabled(false);
 		createGameButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				controller.beginGameCreation();
 			}
 		});
 		
 		joinButton = new JButton(joinIcon);
 		joinButton.setBorder(BorderFactory.createEmptyBorder());
 		joinButton.setContentAreaFilled(false);
 		joinButton.setPressedIcon(joinPressedIcon);
 		joinButton.setRolloverIcon(joinHoverIcon);
 		joinButton.setDisabledIcon(joinDisabledIcon);
 		joinButton.setEnabled(false);
 		joinButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 					controller.joinGame(gameTable.getSelectedRow());
 			}
 		});
 
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(20, 0, 0, 0);	
 		c.gridx = 0;
 		c.gridy = 0;
 		c.anchor = GridBagConstraints.LINE_START;
 		c.fill = GridBagConstraints.NONE;
 		add(lobbyLabel, c);
 		
 		c.gridx = 0;
 		c.gridy = 1;
 		c.fill = GridBagConstraints.NONE;
 		add(usernameLabel, c);
 
 		c.gridx = 1;
 		c.gridy = 1;
 		c.gridwidth = 1;
 		c.insets = new Insets(20, -20, 0, 0);	
 		c.fill = GridBagConstraints.HORIZONTAL;
 		add(usernameField, c);
 		
 		c.gridx = 3;
 		c.gridy = 0;
 		c.insets = new Insets(20, -150, 0, 0);	
 		c.fill = GridBagConstraints.NONE;
 		add(refreshButton, c);
 			
 		c.gridx = 0;
 		c.gridy = 2;
 		c.gridwidth = 6;
 		c.ipadx = 100;
 		c.ipady = 300;
 		c.insets = new Insets(20, 0, 0, 0);	
 		c.anchor = GridBagConstraints.LINE_START;
 		c.fill = GridBagConstraints.BOTH;
 		add(gameTableScrollPane, c);
 		
 		c.gridx = 0;
 		c.gridy = 3;
 		c.gridwidth = 2;
 		c.ipadx = 0;
 		c.ipady = 0;
 		c.insets = new Insets(20, 0, 0, 0);	
 		c.fill = GridBagConstraints.NONE;
 		add(exitButton, c);
 		
 		c.gridx = 1;
 		c.gridy = 3;
 		c.insets = new Insets(20, 150, 0, 0);	
 		c.gridwidth = 1;
 		c.fill = GridBagConstraints.NONE;
 		add(createGameButton, c);				
 
 		c.gridx = 2;
 		c.gridy = 3;
 		c.insets = new Insets(20, 50, 0, 0);	
 		c.fill = GridBagConstraints.NONE;
 		add(joinButton, c);	
 	}
 	
 	private void updateAllowedButtons(String username) {
 	    if (gameTable != null) {
 	    	if (gameTable.getSelectedRow() < 0 || username.trim().length() <= 0) {
 				joinButton.setEnabled(false);
 			} else {
 				joinButton.setEnabled(true);	
 			}
 	
 			if (username.trim().length() <= 0) {
 				createGameButton.setEnabled(false);
 			} else {
 				createGameButton.setEnabled(true);
 			}
 		}
 	}
 	
 	public void updateGameListPane() {
 		final LobbyManager lm = controller.getLobbyManager();
 		
 		SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
 			public Void doInBackground() {
 				lm.refreshGameList();
 				return null;
 			}
 			
 			protected void done() {
 				String[][] data = new String[lm.getAvailableGames().size()][3];
 				
 				int i = 0;
 				for (AvailableGame ag : lm.getAvailableGames()) {
 					data[i][0] = ag.getGameName();
 					data[i][1] = ag.getHostName();
 					data[i][2] = ag.getMapName();
 					i++;
 				}
 				
 				gameTable = new JTable(data, columnHeaders) {
 					private static final long serialVersionUID = 1L;
 
 					public boolean isCellEditable(int row, int column) {
 						return false;
 					}
 
 					public void valueChanged(ListSelectionEvent e) {
 						updateAllowedButtons(usernameField.getText());
 					}
 				};
 
 				gameTable.setColumnSelectionAllowed(false);
 				gameTable.getTableHeader().setReorderingAllowed(false);
 				gameTableScrollPane.setViewportView(gameTable);
 				gameTable.setFillsViewportHeight(true);
 			}
 		};
 		
 		worker.execute();
 	}
 	
 	public void paintComponent(Graphics g) {
 		g.drawImage(background.getImage(), 0 ,0, null);
 	}
 }
