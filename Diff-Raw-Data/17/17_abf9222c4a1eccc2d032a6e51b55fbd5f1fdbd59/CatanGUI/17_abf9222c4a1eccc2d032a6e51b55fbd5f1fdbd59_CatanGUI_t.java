 package cs283.catan;
 
 import java.awt.EventQueue;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFrame;
 import java.awt.GridBagLayout;
 import javax.swing.BoxLayout;
 import java.awt.BorderLayout;
 import javax.swing.JCheckBox;
 import java.awt.GridBagConstraints;
 import javax.swing.JSplitPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import java.awt.Dimension;
 import javax.swing.JLayeredPane;
 import javax.swing.JLabel;
 import javax.swing.JTextPane;
 import java.awt.Insets;
 import java.awt.Component;
 
 import javax.swing.ImageIcon;
 import javax.swing.JOptionPane;
 import javax.swing.JRadioButton;
 import javax.swing.JEditorPane;
 import javax.swing.JButton;
 import javax.swing.JSeparator;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JList;
 import javax.swing.JTable;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import java.awt.GridLayout;
 import javax.swing.JScrollPane;
 import javax.swing.ListSelectionModel;
 
 import cs283.catan.ClientMain.LobbyMessageType;
 
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.IOException;
 import java.util.*;
 import javax.swing.JTextField;
 import javax.swing.JScrollBar;
 import java.awt.ScrollPane;
 
 
 public class CatanGUI {
 
 	private JFrame frame;
 	private JTable lobby_table;
 	private JTabbedPane gameAndMatch;
 	private JTextField textField;
 	private JTextField textField_1;
 	private JTextField largestArmyField;
 	private JTextField longestRoadField;
 	private JTextField victoryPointTotalField;
 	private JTextField wheatField;
 	private JTextField woolField;
 	private JTextField oreField;
 	private JTextField lumberField;
 	private JTextField brickField;
 	private JTextField monField;
 	private JTextField plentyField;
 	private JTextField knightField;
 	private JTextField victoryField;
 	private JTextField roadBuildField;
 	private JTextField gameCommandField;
 	private JTextField chatInputField;
 	private JEditorPane chatOutputPane;
 	private JButton sendMessageButton;
 	private JButton sendCommandButton;
 	private JButton endTurnButton;
 	private GamePanel gamePanel;
 	
 	private JButton btnCreateGame;
     private JButton btnJoinGame;
     private JButton btnLeaveGame;
     private String username;
     private JTabbedPane tabbedPane;
     private JPanel panel;
     private JScrollPane scrollPane_2;
     private JPanel panel_3;
     
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					CatanGUI window = new CatanGUI();
 					window.frame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public CatanGUI() {
 		initialize();
 	}
 	
 	/**
      * Set the username
      * @param username
      */
     public void setUsername(String username) {
         this.username = username;
         this.frame.setTitle(username);
     }
 	
 	/**
 	 * @return the GUI frame.
 	 */
 	public JFrame getFrame() {
 	    return frame;
 	}
 	
 
 	/**
      * Update the lobby table.
      * @param lobbyGames
      */
     public void updateLobby(Map<String, String[]> lobbyGames) {
         // Keep track of whether the user is currently in a game        
         boolean isAlreadyInGame = false;
         
         DefaultTableModel model = (DefaultTableModel) lobby_table.getModel();
         
         // Delete previous data
         model.setRowCount(0);
         
         for (Map.Entry<String, String[]> game : lobbyGames.entrySet()) {
             String rowData[] = new String[5];
             rowData[0] = game.getKey();
             
             String playerNames[] = game.getValue();
             
             for (int i = 0; i < playerNames.length; i++) {
                 if (playerNames[i] != null && playerNames[i].equals(username)) {
                     isAlreadyInGame = true;
                 }
                 rowData[i + 1] = playerNames[i];
             }
             
             // Add the game to the lobby table
             model.addRow(rowData);
         }
         
         btnCreateGame.setEnabled(!isAlreadyInGame);
         btnJoinGame.setEnabled(!isAlreadyInGame && 
                                lobby_table.getSelectedRow() != -1);
         btnLeaveGame.setEnabled(isAlreadyInGame &&
                                 lobby_table.getSelectedRow() != -1);
     }
 	
 	/**
 	 * Switch to the game mode
 	 */
 	public void switchToGameMode() {
 	    // Switch the tab to game mode and disable the lobby
 	    gameAndMatch.setEnabledAt(1, true);
 	    gameAndMatch.setSelectedIndex(1);
 	    gameAndMatch.setEnabledAt(0, false);
 	    
 	    // Enable the chat and game command inputs
 	    chatInputField.setEnabled(true);
 	    sendMessageButton.setEnabled(true);
 	    
 	    gameCommandField.setEnabled(true);
 	    sendCommandButton.setEnabled(true);
 	    
 	    // Make sure the end turn button is initially disabled
 	    endTurnButton.setEnabled(false);
 	}
 	
 	/**
 	 * Send a chat message.
 	 */
 	private void sendChatMessage() {
 	    String message = chatInputField.getText().trim();
 	    
 	    if (!message.equals("")) {
 	        
 	        // Attempt to send the chat message
     	    try {
     	        ClientMain.sendChatMsg(message);
     	        chatInputField.setText("");
     	    } catch (StringIndexOutOfBoundsException e) {
     	        JOptionPane.showMessageDialog(this.frame, 
     	                                      "Invalid message format!");
     	    } catch (Exception e) {
     	        e.printStackTrace();
     	    }
 	    }
 	}
 	
 	/**
 	 * Send a game command.
 	 */
 	private void sendGameCommand() {
 	    String message = gameCommandField.getText().trim();
 	    
 	    if (!message.equals("")) {
 	        
 	        // Attempt to send the game command
 	        try {
 	            ClientMain.sendGameCommand(gameCommandField.getText());
 	            gameCommandField.setText("");
 	        } catch (Exception e) {
 	            e.printStackTrace();
 	            JOptionPane.showMessageDialog(this.frame, 
 	                                          "Connection problem, game over!");
 	            frame.dispose();
 	        }
 	    }
 	}
 	
 	/**
 	 * Receive a chat message and update the chat window.
 	 * @param message
 	 */
 	public void receiveChatMessage(String message) {
 	    chatOutputPane.setText(chatOutputPane.getText() + "\n" +
 	                           message);
 	}
 	
 	
 	/**
 	 * Draws the catan board.
 	 * @param game
 	 */
 	public void drawBoard(ServerCatanGame game) {
 	    Board board = game.getBoard();
 	    
   
	    
 	    // Draw all of the settlements
 	    List<Settlement> settlements = board.getSettlementList();
 	    
 	    gamePanel.clearboard();
 	    
 	    for (Settlement settlement : settlements) {
 	        gamePanel.drawsettle(board.getPixel(settlement.getLocation()), 
 	                             settlement.isCity(),
 	                             settlement.getOwner().getColorIndex());
 	        
 	        System.out.format("Drawing settlement:\nLocation: %s\nOwner: " +
 	                          "%s\nIs City %s\n", settlement.getLocation(), 
 	                          settlement.getOwner(), settlement.isCity());
 	    }
 	    
 	    // Draw all of the roads
 	    List<Road> roads = board.getRoadList();
 	    
 	    for (Road road : roads) {
 	        gamePanel.addroad(board.getPixel(road.getStart()),
 	                          board.getPixel(road.getFinish()),
 	                          road.getOwner().getColorIndex());
 	        
 	        System.out.format("Drawing road:\nStart: %s\nFinish: %s\n" +
 	                          "Owner: %s\n", road.getStart(), road.getFinish(),
 	                          road.getOwner());
 	    }
 	    

	    // Draw the robber
	    gamePanel.drawrobber(board.getRobberLocation());
	    	    
 	    // Update the counts for the cards
 	    Player playerArray[] = game.getPlayerArray();
 	    for (int i = 0; i < playerArray.length; i++) {
 	        if (playerArray[i].getUsername().equals(username)) {
 	            updateCardCount(playerArray[i]);
 	            break;
 	        }
 	    }
 	    
 	    // Update the owner of the longest road
 	    String longestRoadOwner = game.whoHasLongestRoad();
 	    if (longestRoadOwner == null) {
 	        longestRoadOwner = "N/A";
 	    }
 	    
 	    longestRoadField.setText(longestRoadOwner);
 	    
 	    // Update the owner of the largest army
 	    String largestArmyOwner = game.whoHasLargestArmy();
 	    if (largestArmyOwner == null) {
 	        largestArmyOwner = "N/A";
 	    }
 	    

	    
 	    largestArmyField.setText(largestArmyOwner);
 	    
 	    gamePanel.view().repaint();
 	}
 	
 	/**
 	 * Handles a new roll.
 	 * @roll
 	 * @currentPlayer
 	 * @playerArray
 	 */
 	public void newRoll(int roll, int currentPlayer, Player playerArray[]) {
 	    if (roll > 0) {
 	        JOptionPane.showMessageDialog(frame, 
 	                                 String.format("'%s' rolls a %d!", 
 	                                 playerArray[currentPlayer].getUsername(), 
 	                                 roll));
 	    } else {
 	        JOptionPane.showMessageDialog(frame,
 	                                 String.format("'%s' will now make a " +
 	                                                 "settlement and road " +
 	                                                 "placement!",
 	                                 playerArray[currentPlayer].getUsername()));
 	    }
 	    
 	    // Update the current player's info
 	    for (int i = 0; i < playerArray.length; i++) {
 	        if (playerArray[i].getUsername().equals(username)) {
 	            if (i == currentPlayer) {
     	            // Enable the end turn button
 	                endTurnButton.setEnabled(true);
 	            } else {
 	                // Disable the end turn button
 	                endTurnButton.setEnabled(false);
 	            }
 	            
 	            // Update the counts for the cards
 	            updateCardCount(playerArray[i]);
 	            
 	            break;
 	        }
 	    }
 	}
 	
 	/**
 	 * Updates the counts for the cards
 	 * @param player
 	 */
 	private void updateCardCount(Player player) {
 	    int woolCount = player.getNumCards(ResourceCard.WOOL.toString());;
 	    int lumberCount = player.getNumCards(ResourceCard.LUMBER.toString());
 	    int brickCount = player.getNumCards(ResourceCard.BRICK.toString());
 	    int wheatCount = player.getNumCards(ResourceCard.WHEAT.toString());
 	    int oreCount = player.getNumCards(ResourceCard.ORE.toString());
 	    
 	    // Set the resource card counts
 	    woolField.setText(String.valueOf(woolCount));
 	    lumberField.setText(String.valueOf(lumberCount));
 	    brickField.setText(String.valueOf(brickCount));
 	    wheatField.setText(String.valueOf(wheatCount));
 	    oreField.setText(String.valueOf(oreCount));
 	    
 	    int monCount = 0;
 	    int plentyCount = 0;
 	    int knightCount = 0;
 	    int victoryCount = 0;
 	    int roadBuildCount = 0;
 	    
 	    for (DevelopmentCard card : player.getDevelopmentCards()) {
 	        switch (card.getDevCardType()) {
 	        case MONOPOLY:
 	            monCount++;
 	            break;
 	        case YEAR_OF_PLENTY:
 	            plentyCount++;
 	            break;
 	        case KNIGHT:
 	            knightCount++;
 	            break;
 	        case VICTORY_POINTS:
 	            victoryCount++;
 	            break;
 	        case ROAD_BUILDING:
 	            roadBuildCount++;
 	            break;
 	        }
 	    }
 	    
 	    // Set the development card counts
 	    monField.setText(String.valueOf(monCount));
 	    plentyField.setText(String.valueOf(plentyCount));
 	    knightField.setText(String.valueOf(knightCount));
 	    victoryField.setText(String.valueOf(victoryCount));
 	    roadBuildField.setText(String.valueOf(roadBuildCount));
 	    
 	    // Update the victory point counts
         victoryPointTotalField.setText(String
                             .valueOf(player.getVictoryPoints()));
 	}
 	
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setPreferredSize(new Dimension(1000, 900));
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 		frame.addWindowListener(new WindowAdapter() {
 		    @Override
 		    public void windowClosed(WindowEvent e) {
 		        synchronized (ClientMain.waitForGuiDone) {
 		            // Notify the main thread that the client program is exiting
 		            ClientMain.waitForGuiDone.notifyAll();
 		        }
 		    }
 		});
 		frame.setMinimumSize(new Dimension(1000, 768));
 		frame.setBounds(100, 100, 450, 300);
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[]{500, 233, 0};
 		gridBagLayout.rowHeights = new int[]{350, 45, 100, 0};
 		gridBagLayout.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		gridBagLayout.rowWeights = new double[]{1.0, 0.0, 1.0, Double.MIN_VALUE};
 		frame.getContentPane().setLayout(gridBagLayout);
 		
 		gameAndMatch = new JTabbedPane(JTabbedPane.TOP);
 		gameAndMatch.setOpaque(true);
 		gameAndMatch.setMaximumSize(new Dimension(650, 550));
 		GridBagConstraints gbc_gameAndMatch = new GridBagConstraints();
 		gbc_gameAndMatch.fill = GridBagConstraints.BOTH;
 		gbc_gameAndMatch.insets = new Insets(0, 0, 5, 5);
 		gbc_gameAndMatch.gridx = 0;
 		gbc_gameAndMatch.gridy = 0;
 		frame.getContentPane().add(gameAndMatch, gbc_gameAndMatch);
 		
 		JPanel matchmaking = new JPanel();
 		matchmaking.setPreferredSize(new Dimension(650, 350));
 		matchmaking.setMaximumSize(new Dimension(650, 550));
 		gameAndMatch.addTab("Lobby", null, matchmaking, null);
 		gameAndMatch.setEnabledAt(0, true);
 		matchmaking.setLayout(new BoxLayout(matchmaking, BoxLayout.Y_AXIS));
 		
 		JScrollPane scrollPane = new JScrollPane();
 		matchmaking.add(scrollPane);
 		
 		lobby_table = new JTable();
 		lobby_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		scrollPane.setViewportView(lobby_table);
 		lobby_table.setModel(new DefaultTableModel(
 		    new Object[][] {
 		    },
 		    new String[] {
 		        "Game Name", "Player 1", "Player 2", "Player 3", "Player 4"
 		    }
 		) {
 		    Class[] columnTypes = new Class[] {
 		        String.class, String.class, String.class, String.class, String.class
 		    };
 		    public Class getColumnClass(int columnIndex) {
 		        return columnTypes[columnIndex];
 		    }
 		    
 		    boolean[] columnEditables = new boolean[] {
 	                false, false, false, false, false
 	            };
             public boolean isCellEditable(int row, int column) {
                 return columnEditables[column];
             }
 		});
 		
 		lobby_table.getSelectionModel().addListSelectionListener(
                 new ListSelectionListener() {
             @Override
             public void valueChanged(ListSelectionEvent arg0) {
                 int row = lobby_table.getSelectedRow();
                 if (btnCreateGame.isEnabled() && row != -1) {
                     btnJoinGame.setEnabled(true);
                 } else if (!btnCreateGame.isEnabled() && row != -1) {
                     Vector data = ((Vector) ((DefaultTableModel) lobby_table
                                    .getModel()).getDataVector().get(row));
                     
                     btnLeaveGame.setEnabled(false);
                     for (int i = 0; i < data.size(); i++) {
                         String currentString = (String) data.get(i);
                         if (currentString != null && 
                             currentString.equals(username)) {
                             
                             btnLeaveGame.setEnabled(true);
                             break;
                         }
                     }
                 }
                 
             }
         });
 		
 		JPanel panel_1 = new JPanel();
 		matchmaking.add(panel_1);
 		
 		btnCreateGame = new JButton("Create Game");
 		btnCreateGame.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent arg0) {
 		        String gameName = JOptionPane.showInputDialog(frame, 
 		                                          "Enter the name of the game");
 		        
 		        if (gameName != null) {
 		            gameName = gameName.trim();
 		        
 		            if (!gameName.equals("")) {
 		                try {
 		                    ClientMain.sendLobbyMsg(ClientMain.LobbyMessageType
 		                                            .CreateGame, gameName);
 		                } catch (Exception e) {
 		                    e.printStackTrace();
 		                    frame.dispose();
 		                }
 		            }
 		        }
 		    }
 		});
 		panel_1.add(btnCreateGame);
 		
 		btnJoinGame = new JButton("Join Game");
 		btnJoinGame.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent arg0) {
 		        int row = lobby_table.getSelectedRow();
 		        
 		        if (row != -1) {
 		            String gameName = (String) ((Vector) 
 		                              ((DefaultTableModel) lobby_table
 		                                .getModel()).getDataVector().get(row))
 		                                .get(0);
 		            
                     if (gameName != null) {
                         try {
                             ClientMain.sendLobbyMsg(ClientMain.LobbyMessageType
                                                     .AddUserToGame, gameName);
                         } catch (Exception e) {
                             e.printStackTrace();
                             frame.dispose();
                         }
                     }
                 }
 		    }
 		});
 		panel_1.add(btnJoinGame);
 		
 		btnLeaveGame = new JButton("Leave Game");
 		btnLeaveGame.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent arg0) {
 		        int row = lobby_table.getSelectedRow();
 		        
 		        if (row != -1) {
 		            String gameName = (String) ((Vector)
 		                              ((DefaultTableModel) lobby_table
 		                                .getModel()).getDataVector().get(row))
 		                                .get(0);
 		            
 		            if (gameName != null) {
 		                try {
 		                    ClientMain.sendLobbyMsg(ClientMain.LobbyMessageType
 		                                            .RemoveUserFromGame,
 		                                            gameName);
 		                } catch (Exception e) {
 		                    e.printStackTrace();
 		                    frame.dispose();
 		                }
 		            }
 		        }
 		    }
 		});
 		panel_1.add(btnLeaveGame);
 		lobby_table.getColumnModel().getColumn(0).setResizable(false);
 		lobby_table.getColumnModel().getColumn(1).setResizable(false);
 		lobby_table.getColumnModel().getColumn(2).setResizable(false);
 		lobby_table.getColumnModel().getColumn(3).setResizable(false);
 		lobby_table.getColumnModel().getColumn(4).setResizable(false);
 		
 		lobby_table.getTableHeader().setReorderingAllowed(false);
 		
 		
 		gamePanel = new GamePanel();
 		
 		JScrollPane gameplay = new JScrollPane();
 		gameplay.setPreferredSize(new Dimension(650, 550));
 		gameplay.setMaximumSize(new Dimension(650, 550));
 		gameAndMatch.addTab("Game", null, gameplay, null);
 		gameAndMatch.setEnabledAt(1, false);
 		//gameplay.add(gamePanel.view());
 		
 		panel_3 = new JPanel();
 		gameplay.setViewportView(panel_3);
 		panel_3.add(gamePanel.view());
 		
 		panel = new JPanel();
 		panel.setMaximumSize(new Dimension(32767, 550));
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.BOTH;
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.gridx = 1;
 		gbc_panel.gridy = 0;
 		frame.getContentPane().add(panel, gbc_panel);
 		GridBagLayout gbl_panel = new GridBagLayout();
 		gbl_panel.columnWidths = new int[]{332, 0};
 		gbl_panel.rowHeights = new int[]{15, 300, 0};
 		gbl_panel.columnWeights = new double[]{1.0, Double.MIN_VALUE};
 		gbl_panel.rowWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
 		panel.setLayout(gbl_panel);
 		
 		JLabel lblNewLabel_7 = new JLabel("Smack-talkin");
 		GridBagConstraints gbc_lblNewLabel_7 = new GridBagConstraints();
 		gbc_lblNewLabel_7.insets = new Insets(0, 0, 5, 0);
 		gbc_lblNewLabel_7.gridx = 0;
 		gbc_lblNewLabel_7.gridy = 0;
 		panel.add(lblNewLabel_7, gbc_lblNewLabel_7);
 		
 		JScrollPane scrollPane_1 = new JScrollPane();
 		scrollPane_1.setMaximumSize(new Dimension(32767, 530));
 		GridBagConstraints gbc_scrollPane_1 = new GridBagConstraints();
 		gbc_scrollPane_1.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_1.gridx = 0;
 		gbc_scrollPane_1.gridy = 1;
 		panel.add(scrollPane_1, gbc_scrollPane_1);
 		
 		chatOutputPane = new JEditorPane();
 		chatOutputPane.setPreferredSize(new Dimension(233, 300));
 		chatOutputPane.setEditable(false);
 		scrollPane_1.setViewportView(chatOutputPane);
 		
 		JPanel commandPanel = new JPanel();
 		commandPanel.setMaximumSize(new Dimension(32767, 45));
 		GridBagConstraints gbc_commandPanel = new GridBagConstraints();
 		gbc_commandPanel.insets = new Insets(0, 0, 5, 5);
 		gbc_commandPanel.anchor = GridBagConstraints.NORTH;
 		gbc_commandPanel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_commandPanel.gridx = 0;
 		gbc_commandPanel.gridy = 1;
 		frame.getContentPane().add(commandPanel, gbc_commandPanel);
 		GridBagLayout gbl_commandPanel = new GridBagLayout();
 		gbl_commandPanel.columnWidths = new int[]{0, 0, 0};
 		gbl_commandPanel.rowHeights = new int[]{0, 0, 0, 0};
 		gbl_commandPanel.columnWeights = new double[]{1.0, 1.0, Double.MIN_VALUE};
 		gbl_commandPanel.rowWeights = new double[]{0.0, 0.0, 1.0, Double.MIN_VALUE};
 		commandPanel.setLayout(gbl_commandPanel);
 		
 		gameCommandField = new JTextField();
 		gameCommandField.setEnabled(false);
 		gameCommandField.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 		        sendGameCommand();
 		    }
 		});
 		GridBagConstraints gbc_gameCommandField = new GridBagConstraints();
 		gbc_gameCommandField.gridwidth = 2;
 		gbc_gameCommandField.insets = new Insets(0, 0, 5, 0);
 		gbc_gameCommandField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_gameCommandField.gridx = 0;
 		gbc_gameCommandField.gridy = 0;
 		commandPanel.add(gameCommandField, gbc_gameCommandField);
 		gameCommandField.setColumns(10);
 		
 		sendCommandButton = new JButton("Send Command");
 		sendCommandButton.setEnabled(false);
 		sendCommandButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			    sendGameCommand();
 			}
 		});
 		
 		JTextField ErrorText = new JTextField();
 		ErrorText.setEditable(false);
 		ErrorText.setText("Error Messages");
 		ErrorText.setSize(new Dimension(100, 22));
 		ErrorText.setMinimumSize(new Dimension(100, 22));
 		ErrorText.setToolTipText("Displays error messages");
 		GridBagConstraints gbc_ErrorText = new GridBagConstraints();
 		gbc_ErrorText.insets = new Insets(0, 0, 5, 0);
 		gbc_ErrorText.gridwidth = 2;
 		gbc_ErrorText.anchor = GridBagConstraints.WEST;
 		gbc_ErrorText.fill = GridBagConstraints.VERTICAL;
 		gbc_ErrorText.gridx = 0;
 		gbc_ErrorText.gridy = 1;
 		commandPanel.add(ErrorText, gbc_ErrorText);
 		GridBagConstraints gbc_sendCommandButton = new GridBagConstraints();
 		gbc_sendCommandButton.insets = new Insets(0, 0, 5, 0);
 		gbc_sendCommandButton.anchor = GridBagConstraints.EAST;
 		gbc_sendCommandButton.gridx = 1;
 		gbc_sendCommandButton.gridy = 1;
 		commandPanel.add(sendCommandButton, gbc_sendCommandButton);
 		
 		JPanel panel_2 = new JPanel();
 		panel_2.setMaximumSize(new Dimension(32767, 145));
 		GridBagConstraints gbc_panel_2 = new GridBagConstraints();
 		gbc_panel_2.gridheight = 2;
 		gbc_panel_2.fill = GridBagConstraints.BOTH;
 		gbc_panel_2.gridx = 1;
 		gbc_panel_2.gridy = 1;
 		frame.getContentPane().add(panel_2, gbc_panel_2);
 		GridBagLayout gbl_panel_2 = new GridBagLayout();
 		gbl_panel_2.columnWidths = new int[]{143, 189, 0};
 		gbl_panel_2.rowHeights = new int[]{32, 43, 0, 22, 0};
 		gbl_panel_2.columnWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gbl_panel_2.rowWeights = new double[]{0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		panel_2.setLayout(gbl_panel_2);
 		
 		JLabel lblNewLabel_8 = new JLabel("Chat Here!");
 		GridBagConstraints gbc_lblNewLabel_8 = new GridBagConstraints();
 		gbc_lblNewLabel_8.gridwidth = 2;
 		gbc_lblNewLabel_8.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_8.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_8.gridx = 0;
 		gbc_lblNewLabel_8.gridy = 0;
 		panel_2.add(lblNewLabel_8, gbc_lblNewLabel_8);
 		
 		chatInputField = new JTextField();
 		chatInputField.setEnabled(false);
 		chatInputField.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent arg0) {
 		        sendChatMessage();
 		    }
 		});
 		chatInputField.setColumns(10);
 		GridBagConstraints gbc_chatInputField = new GridBagConstraints();
 		gbc_chatInputField.gridwidth = 2;
 		gbc_chatInputField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_chatInputField.insets = new Insets(0, 0, 5, 0);
 		gbc_chatInputField.gridx = 0;
 		gbc_chatInputField.gridy = 1;
 		panel_2.add(chatInputField, gbc_chatInputField);
 		
 		sendMessageButton = new JButton("Send Message");
 		sendMessageButton.setEnabled(false);
 		sendMessageButton.addActionListener(new ActionListener() {
 		    public void actionPerformed(ActionEvent e) {
 		        sendChatMessage();
 		    }
 		});
 		GridBagConstraints gbc_sendMessageButton = new GridBagConstraints();
 		gbc_sendMessageButton.gridwidth = 2;
 		gbc_sendMessageButton.anchor = GridBagConstraints.EAST;
 		gbc_sendMessageButton.gridx = 0;
 		gbc_sendMessageButton.gridy = 2;
 		panel_2.add(sendMessageButton, gbc_sendMessageButton);
 		
 		
 
 		
 		scrollPane_2 = new JScrollPane();
 		scrollPane_2.setMinimumSize(new Dimension(27, 100));
 		GridBagConstraints gbc_scrollPane_2 = new GridBagConstraints();
 		gbc_scrollPane_2.fill = GridBagConstraints.BOTH;
 		gbc_scrollPane_2.insets = new Insets(0, 0, 0, 5);
 		gbc_scrollPane_2.gridx = 0;
 		gbc_scrollPane_2.gridy = 2;
 		frame.getContentPane().add(scrollPane_2, gbc_scrollPane_2);
 		
 		tabbedPane = new JTabbedPane(JTabbedPane.TOP);
 		tabbedPane.setMinimumSize(new Dimension(5, 50));
 		scrollPane_2.setViewportView(tabbedPane);
 		
 		JPanel resourcePanel = new JPanel();
 		tabbedPane.addTab("Resources", null, resourcePanel, null);
 		GridBagLayout gbl_resourcePanel = new GridBagLayout();
 		gbl_resourcePanel.columnWidths = new int[]{100, 100, 100, 100, 200, 0};
 		gbl_resourcePanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0};
 		gbl_resourcePanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_resourcePanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		resourcePanel.setLayout(gbl_resourcePanel);
 		
 		JLabel lblNewLabel_2 = new JLabel("Wheat");
 		GridBagConstraints gbc_lblNewLabel_2 = new GridBagConstraints();
 		gbc_lblNewLabel_2.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_2.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_2.gridx = 0;
 		gbc_lblNewLabel_2.gridy = 0;
 		resourcePanel.add(lblNewLabel_2, gbc_lblNewLabel_2);
 		
 		wheatField = new JTextField();
 		wheatField.setEditable(false);
 		wheatField.setColumns(10);
 		GridBagConstraints gbc_wheatField = new GridBagConstraints();
 		gbc_wheatField.insets = new Insets(0, 0, 5, 5);
 		gbc_wheatField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_wheatField.gridx = 1;
 		gbc_wheatField.gridy = 0;
 		resourcePanel.add(wheatField, gbc_wheatField);
 		
 		JLabel lblMonopoly = new JLabel("Monopoly");
 		GridBagConstraints gbc_lblMonopoly = new GridBagConstraints();
 		gbc_lblMonopoly.anchor = GridBagConstraints.WEST;
 		gbc_lblMonopoly.insets = new Insets(0, 0, 5, 5);
 		gbc_lblMonopoly.gridx = 2;
 		gbc_lblMonopoly.gridy = 0;
 		resourcePanel.add(lblMonopoly, gbc_lblMonopoly);
 		
 		endTurnButton = new JButton("End Turn");
 		endTurnButton.setEnabled(false);
 		endTurnButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			    try {
 			        ClientMain.sendEndTurn();
 			    } catch (Exception e) {
 			        e.printStackTrace();
 			        frame.dispose();
 			    }
 			}
 		});
 		
 		monField = new JTextField();
 		monField.setEditable(false);
 		monField.setColumns(10);
 		GridBagConstraints gbc_monField = new GridBagConstraints();
 		gbc_monField.insets = new Insets(0, 0, 5, 5);
 		gbc_monField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_monField.gridx = 3;
 		gbc_monField.gridy = 0;
 		resourcePanel.add(monField, gbc_monField);
 		GridBagConstraints gbc_endTurnButton = new GridBagConstraints();
 		gbc_endTurnButton.gridheight = 5;
 		gbc_endTurnButton.gridx = 4;
 		gbc_endTurnButton.gridy = 0;
 		resourcePanel.add(endTurnButton, gbc_endTurnButton);
 		
 		JLabel lblNewLabel_3 = new JLabel("Wool");
 		GridBagConstraints gbc_lblNewLabel_3 = new GridBagConstraints();
 		gbc_lblNewLabel_3.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_3.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_3.gridx = 0;
 		gbc_lblNewLabel_3.gridy = 1;
 		resourcePanel.add(lblNewLabel_3, gbc_lblNewLabel_3);
 		
 		woolField = new JTextField();
 		woolField.setEditable(false);
 		woolField.setColumns(10);
 		GridBagConstraints gbc_woolField = new GridBagConstraints();
 		gbc_woolField.insets = new Insets(0, 0, 5, 5);
 		gbc_woolField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_woolField.gridx = 1;
 		gbc_woolField.gridy = 1;
 		resourcePanel.add(woolField, gbc_woolField);
 		
 		JLabel lblYearOfPlenty = new JLabel("Year of Plenty");
 		GridBagConstraints gbc_lblYearOfPlenty = new GridBagConstraints();
 		gbc_lblYearOfPlenty.anchor = GridBagConstraints.WEST;
 		gbc_lblYearOfPlenty.insets = new Insets(0, 0, 5, 5);
 		gbc_lblYearOfPlenty.gridx = 2;
 		gbc_lblYearOfPlenty.gridy = 1;
 		resourcePanel.add(lblYearOfPlenty, gbc_lblYearOfPlenty);
 		
 		plentyField = new JTextField();
 		plentyField.setEditable(false);
 		plentyField.setColumns(10);
 		GridBagConstraints gbc_plentyField = new GridBagConstraints();
 		gbc_plentyField.insets = new Insets(0, 0, 5, 5);
 		gbc_plentyField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_plentyField.gridx = 3;
 		gbc_plentyField.gridy = 1;
 		resourcePanel.add(plentyField, gbc_plentyField);
 		
 		JLabel lblNewLabel_4 = new JLabel("Ore");
 		GridBagConstraints gbc_lblNewLabel_4 = new GridBagConstraints();
 		gbc_lblNewLabel_4.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_4.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_4.gridx = 0;
 		gbc_lblNewLabel_4.gridy = 2;
 		resourcePanel.add(lblNewLabel_4, gbc_lblNewLabel_4);
 		
 		oreField = new JTextField();
 		oreField.setEditable(false);
 		oreField.setColumns(10);
 		GridBagConstraints gbc_oreField = new GridBagConstraints();
 		gbc_oreField.insets = new Insets(0, 0, 5, 5);
 		gbc_oreField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_oreField.gridx = 1;
 		gbc_oreField.gridy = 2;
 		resourcePanel.add(oreField, gbc_oreField);
 		
 		JLabel lblKnights = new JLabel("Knights");
 		GridBagConstraints gbc_lblKnights = new GridBagConstraints();
 		gbc_lblKnights.anchor = GridBagConstraints.WEST;
 		gbc_lblKnights.insets = new Insets(0, 0, 5, 5);
 		gbc_lblKnights.gridx = 2;
 		gbc_lblKnights.gridy = 2;
 		resourcePanel.add(lblKnights, gbc_lblKnights);
 		
 		knightField = new JTextField();
 		knightField.setEditable(false);
 		knightField.setColumns(10);
 		GridBagConstraints gbc_knightField = new GridBagConstraints();
 		gbc_knightField.insets = new Insets(0, 0, 5, 5);
 		gbc_knightField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_knightField.gridx = 3;
 		gbc_knightField.gridy = 2;
 		resourcePanel.add(knightField, gbc_knightField);
 		
 		JLabel lblNewLabel_5 = new JLabel("Lumber");
 		GridBagConstraints gbc_lblNewLabel_5 = new GridBagConstraints();
 		gbc_lblNewLabel_5.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_5.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel_5.gridx = 0;
 		gbc_lblNewLabel_5.gridy = 3;
 		resourcePanel.add(lblNewLabel_5, gbc_lblNewLabel_5);
 		
 		lumberField = new JTextField();
 		lumberField.setEditable(false);
 		lumberField.setColumns(10);
 		GridBagConstraints gbc_lumberField = new GridBagConstraints();
 		gbc_lumberField.insets = new Insets(0, 0, 5, 5);
 		gbc_lumberField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_lumberField.gridx = 1;
 		gbc_lumberField.gridy = 3;
 		resourcePanel.add(lumberField, gbc_lumberField);
 		
 		JLabel lblArmy = new JLabel("Victory Points");
 		GridBagConstraints gbc_lblArmy = new GridBagConstraints();
 		gbc_lblArmy.anchor = GridBagConstraints.WEST;
 		gbc_lblArmy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblArmy.gridx = 2;
 		gbc_lblArmy.gridy = 3;
 		resourcePanel.add(lblArmy, gbc_lblArmy);
 		
 		victoryField = new JTextField();
 		victoryField.setEditable(false);
 		victoryField.setColumns(10);
 		GridBagConstraints gbc_victoryField = new GridBagConstraints();
 		gbc_victoryField.insets = new Insets(0, 0, 5, 5);
 		gbc_victoryField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_victoryField.gridx = 3;
 		gbc_victoryField.gridy = 3;
 		resourcePanel.add(victoryField, gbc_victoryField);
 		
 		JLabel lblNewLabel_6 = new JLabel("Brick");
 		GridBagConstraints gbc_lblNewLabel_6 = new GridBagConstraints();
 		gbc_lblNewLabel_6.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_6.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNewLabel_6.gridx = 0;
 		gbc_lblNewLabel_6.gridy = 4;
 		resourcePanel.add(lblNewLabel_6, gbc_lblNewLabel_6);
 		
 		brickField = new JTextField();
 		brickField.setEditable(false);
 		brickField.setColumns(10);
 		GridBagConstraints gbc_brickField = new GridBagConstraints();
 		gbc_brickField.insets = new Insets(0, 0, 0, 5);
 		gbc_brickField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_brickField.gridx = 1;
 		gbc_brickField.gridy = 4;
 		resourcePanel.add(brickField, gbc_brickField);
 		
 		JLabel lblRoadBuilder = new JLabel("Road Builder");
 		GridBagConstraints gbc_lblRoadBuilder = new GridBagConstraints();
 		gbc_lblRoadBuilder.insets = new Insets(0, 0, 0, 5);
 		gbc_lblRoadBuilder.anchor = GridBagConstraints.WEST;
 		gbc_lblRoadBuilder.gridx = 2;
 		gbc_lblRoadBuilder.gridy = 4;
 		resourcePanel.add(lblRoadBuilder, gbc_lblRoadBuilder);
 		
 		roadBuildField = new JTextField();
 		roadBuildField.setEditable(false);
 		roadBuildField.setColumns(10);
 		GridBagConstraints gbc_roadBuildField = new GridBagConstraints();
 		gbc_roadBuildField.insets = new Insets(0, 0, 0, 5);
 		gbc_roadBuildField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_roadBuildField.gridx = 3;
 		gbc_roadBuildField.gridy = 4;
 		resourcePanel.add(roadBuildField, gbc_roadBuildField);
 		
 		JPanel gameInfoTop = new JPanel();
 		tabbedPane.addTab("Victory Conditions", null, gameInfoTop, null);
 		GridBagLayout gbl_gameInfoTop = new GridBagLayout();
 		gbl_gameInfoTop.columnWidths = new int[]{100, 100, 100, 100, 100, 100, 0};
 		gbl_gameInfoTop.rowHeights = new int[]{0, 0, 0};
 		gbl_gameInfoTop.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
 		gbl_gameInfoTop.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		gameInfoTop.setLayout(gbl_gameInfoTop);
 		
 		JLabel lblNewLabel = new JLabel("Settlements");
 		GridBagConstraints gbc_lblNewLabel = new GridBagConstraints();
 		gbc_lblNewLabel.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel.insets = new Insets(0, 0, 5, 5);
 		gbc_lblNewLabel.gridx = 0;
 		gbc_lblNewLabel.gridy = 0;
 		gameInfoTop.add(lblNewLabel, gbc_lblNewLabel);
 		
 		textField = new JTextField();
 		textField.setEditable(false);
 		GridBagConstraints gbc_textField = new GridBagConstraints();
 		gbc_textField.insets = new Insets(0, 0, 5, 5);
 		gbc_textField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField.gridx = 1;
 		gbc_textField.gridy = 0;
 		gameInfoTop.add(textField, gbc_textField);
 		textField.setColumns(10);
 		
 		JLabel lblCities = new JLabel("Biggest Army");
 		GridBagConstraints gbc_lblCities = new GridBagConstraints();
 		gbc_lblCities.anchor = GridBagConstraints.WEST;
 		gbc_lblCities.insets = new Insets(0, 0, 5, 5);
 		gbc_lblCities.gridx = 2;
 		gbc_lblCities.gridy = 0;
 		gameInfoTop.add(lblCities, gbc_lblCities);
 		
 		largestArmyField = new JTextField();
 		largestArmyField.setEditable(false);
 		largestArmyField.setColumns(10);
 		GridBagConstraints gbc_largestArmyField = new GridBagConstraints();
 		gbc_largestArmyField.insets = new Insets(0, 0, 5, 5);
 		gbc_largestArmyField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_largestArmyField.gridx = 3;
 		gbc_largestArmyField.gridy = 0;
 		gameInfoTop.add(largestArmyField, gbc_largestArmyField);
 		
 		JLabel lblBiggestArmy = new JLabel("Victory Points");
 		GridBagConstraints gbc_lblBiggestArmy = new GridBagConstraints();
 		gbc_lblBiggestArmy.anchor = GridBagConstraints.WEST;
 		gbc_lblBiggestArmy.insets = new Insets(0, 0, 5, 5);
 		gbc_lblBiggestArmy.gridx = 4;
 		gbc_lblBiggestArmy.gridy = 0;
 		gameInfoTop.add(lblBiggestArmy, gbc_lblBiggestArmy);
 		
 		victoryPointTotalField = new JTextField();
 		victoryPointTotalField.setEditable(false);
 		victoryPointTotalField.setColumns(10);
 		GridBagConstraints gbc_victoryPointTotalField = new GridBagConstraints();
 		gbc_victoryPointTotalField.insets = new Insets(0, 0, 5, 0);
 		gbc_victoryPointTotalField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_victoryPointTotalField.gridx = 5;
 		gbc_victoryPointTotalField.gridy = 0;
 		gameInfoTop.add(victoryPointTotalField, gbc_victoryPointTotalField);
 		
 		JLabel lblNewLabel_1 = new JLabel("Cities");
 		GridBagConstraints gbc_lblNewLabel_1 = new GridBagConstraints();
 		gbc_lblNewLabel_1.anchor = GridBagConstraints.WEST;
 		gbc_lblNewLabel_1.insets = new Insets(0, 0, 0, 5);
 		gbc_lblNewLabel_1.gridx = 0;
 		gbc_lblNewLabel_1.gridy = 1;
 		gameInfoTop.add(lblNewLabel_1, gbc_lblNewLabel_1);
 		
 		textField_1 = new JTextField();
 		textField_1.setEditable(false);
 		textField_1.setColumns(10);
 		GridBagConstraints gbc_textField_1 = new GridBagConstraints();
 		gbc_textField_1.insets = new Insets(0, 0, 0, 5);
 		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
 		gbc_textField_1.gridx = 1;
 		gbc_textField_1.gridy = 1;
 		gameInfoTop.add(textField_1, gbc_textField_1);
 		
 		JLabel lblLongestRoad = new JLabel("Longest Road");
 		GridBagConstraints gbc_lblLongestRoad = new GridBagConstraints();
 		gbc_lblLongestRoad.anchor = GridBagConstraints.WEST;
 		gbc_lblLongestRoad.insets = new Insets(0, 0, 0, 5);
 		gbc_lblLongestRoad.gridx = 2;
 		gbc_lblLongestRoad.gridy = 1;
 		gameInfoTop.add(lblLongestRoad, gbc_lblLongestRoad);
 		
 		longestRoadField = new JTextField();
 		longestRoadField.setEditable(false);
 		longestRoadField.setColumns(10);
 		GridBagConstraints gbc_longestRoadField = new GridBagConstraints();
 		gbc_longestRoadField.insets = new Insets(0, 0, 0, 5);
 		gbc_longestRoadField.fill = GridBagConstraints.HORIZONTAL;
 		gbc_longestRoadField.gridx = 3;
 		gbc_longestRoadField.gridy = 1;
 		gameInfoTop.add(longestRoadField, gbc_longestRoadField);
 	}
 
 }
