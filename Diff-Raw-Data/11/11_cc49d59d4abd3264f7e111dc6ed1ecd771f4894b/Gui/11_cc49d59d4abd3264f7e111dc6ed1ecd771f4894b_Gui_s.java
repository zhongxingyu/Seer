 package gamesincommon;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Desktop;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Handler;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.SwingWorker;
 
 import net.miginfocom.swing.MigLayout;
 
 import com.github.koraktor.steamcondenser.exceptions.SteamCondenserException;
 import com.github.koraktor.steamcondenser.steam.community.SteamGame;
 import com.github.koraktor.steamcondenser.steam.community.SteamId;
 
 public class Gui {
 
 	final static Font font = new Font("Tahoma", Font.PLAIN, 18);
 
 	private JFrame gamesInCommonFrame;
 	
 	private JPanel consolePanel;
 	private JPanel optionsPanel;
 	private JPanel outputPanel;
 	private JPanel playerPanel;
 	private JPanel scanPanel;
 	private FilterPanel filterPanel;
 	
 	private JScrollPane consoleScrollPane;
 	private JScrollPane outputScrollPane;
 	private JScrollPane playerListScrollPane;
 	
 	private JButton addButton;
 	private JButton removeButton;
 	private JButton scanButton;
 	private JButton cancelButton;
 	
 	private JTextPane consoleText;
 	private JTextField addPlayerText;
 
 	private JList<SteamGameWrapper> outputList;
   private JList<SteamId> playerList;
 
 	private DefaultListModel<SteamGameWrapper> outputListModel;
 	private DefaultListModel<SteamId> playerListModel;
 
 	private ArrayList<SteamId> playerIdList;
 
 	private GamesInCommon gamesInCommon;
 
	private Logger logger;
 
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				try {
 					Gui window = new Gui();
 					window.initialize();
 					window.gamesInCommonFrame.setVisible(true);
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	/**
 	 * Create the application.
 	 */
 	public Gui() {
 		initialize();
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		// create GamesInCommon
 		gamesInCommon = new GamesInCommon();
 		// logger initialisation
 		logger = gamesInCommon.getLogger();
 		// remove handlers
 		removeHandlers(logger.getParent());
 		removeHandlers(logger);
 		// console logging
 		ConsoleHandler cHandler = new ConsoleHandler();
 		cHandler.setLevel(Level.FINEST);
 		// logging to GUI
 		TextPaneHandler tpHandler = new TextPaneHandler(font.getSize());
 		tpHandler.setLevel(Level.INFO);
 		// attach handlers
 		logger.addHandler(cHandler);
 		logger.addHandler(tpHandler);
 		// set formats of handlers to be unified
 		LogFormatter formatter = new LogFormatter();
 		Handler[] handlers = logger.getHandlers();
 		for (Handler handler : handlers) {
 			handler.setFormatter(formatter);
 		}
 
 		gamesInCommonFrame = new JFrame();
 		gamesInCommonFrame.setTitle("Games in Common");
 		gamesInCommonFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		playerPanel = new JPanel();
 		playerPanel.setBorder(BorderFactory.createTitledBorder("Players"));
 
 		optionsPanel = new JPanel();
 		optionsPanel.setBorder(BorderFactory.createTitledBorder("Options"));
 
 		consolePanel = new JPanel();
 		consolePanel.setBorder(BorderFactory.createTitledBorder("Console"));
 		
 		scanPanel = new JPanel();
 		scanPanel.setBorder(BorderFactory.createTitledBorder("Scan"));
 
 		outputPanel = new JPanel();
 		outputPanel.setBorder(BorderFactory.createTitledBorder("Output"));
 		
 		outputScrollPane = new JScrollPane();
 		outputScrollPane.setMinimumSize(new Dimension(450, outputScrollPane.getMinimumSize().height));
 		outputListModel = new DefaultListModel<SteamGameWrapper>();
 		outputList = new JList<SteamGameWrapper>(outputListModel);
 		outputList.setFont(font);
 		outputScrollPane.setViewportView(outputList);
 
 		// launches the selected steam game on double click
 		outputList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					SteamGame launchGame = outputList.getSelectedValue().getGame();
 					logger.log(Level.INFO, "Launching " + launchGame.getName() + " (" + launchGame.getAppId() + ")");
 					try {
 						Desktop.getDesktop().browse(new URI("steam://run/" + launchGame.getAppId()));
 					} catch (IOException | URISyntaxException e1) {
 						logger.log(Level.SEVERE, e1.getMessage());
 					}
 				}
 			}
 		});
 
 		consoleScrollPane = new JScrollPane();
 
 		consoleText = tpHandler.getTextPane();
 		consoleScrollPane.setViewportView(consoleText);
 		consoleScrollPane.setMinimumSize(new Dimension(consoleScrollPane.getMinimumSize().width, 100));
 
 		filterPanel = new FilterPanel();
 
 		scanButton = new JButton("Scan");
 		scanButton.setFont(font);
 		
 		scanButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 			  ((CardLayout) scanPanel.getLayout()).last(scanPanel);
				new Scanner<Void, Void>().execute();	
 			}
 		});
 
 		cancelButton = new JButton("Cancel");
 		cancelButton.setFont(font);
 		
 		cancelButton.addActionListener(new ActionListener() {
 		  @Override
 		  public void actionPerformed(ActionEvent e) {
		    new Scanner<Void, Void>().cancel(true);
 		  }
 		});
 
 		addButton = new JButton("Add");
 		addButton.setFont(font);
 
 		addButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				addName();
 			}
 		});
 
 		removeButton = new JButton("Remove");
 		removeButton.setFont(font);
 
 		removeButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				removeName();
 			}
 		});
 
 		addPlayerText = new JTextField();
 		addPlayerText.setColumns(10);
 		addPlayerText.setFont(font);
 
 		addPlayerText.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent e) {
 				addPlayerText.setText("");
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				if (addPlayerText.getText().equals("")) {
 					addPlayerText.setText("Enter player name...");
 				}
 			}
 		});
 
 		addPlayerText.addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyReleased(KeyEvent e) {
   			char c = e.getKeyChar();
 				if (c == KeyEvent.VK_ENTER) {
 					addName();
 				}
 			}
 		});
 
 		playerListScrollPane = new JScrollPane();
 		playerListScrollPane.setMinimumSize(new Dimension(playerListScrollPane.getMinimumSize().width, 105));
 
 		// initialise data model for playerList, which tells the JList about the data it expects
 		playerListModel = new DefaultListModel<SteamId>();
 		// initialise ArrayList for player list
 		playerIdList = new ArrayList<SteamId>(10);
 		// initialise JList for player list display
 		playerList = new JList<SteamId>(playerListModel);
 		playerList.setFont(font);
 		playerList.setCellRenderer(new PlayerListRenderer());
 		playerList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
 		playerList.setLayoutOrientation(JList.VERTICAL);
 		playerList.setVisibleRowCount(-1);
 		playerListScrollPane.setViewportView(playerList);
 
 		// messages the selected user on double click
 		playerList.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if (e.getClickCount() == 2) {
 					SteamId playerId = playerIdList.get(playerList.getSelectedIndex());
 					//logger.log(Level.INFO, "Messaging " + playerId.getNickname() + " (" + playerId.getSteamId64() + ")");
 					try {
 						Desktop.getDesktop().browse(new URI("steam://friends/message/" + playerId.getSteamId64()));
 					} catch (IOException | URISyntaxException e1) {
 						logger.log(Level.SEVERE, e1.getMessage());
 					}
 				}
 			}
 		});
 		
 		playerPanel.setLayout(new MigLayout("", "grow", "grow"));
 		playerPanel.add(addPlayerText, "cell 0 0, grow");
 		playerPanel.add(playerListScrollPane, "cell 1 0, span 0 2, grow");
 		playerPanel.add(addButton, "cell 0 1, split 2, grow");
 		playerPanel.add(removeButton, "grow");
 		
 		outputPanel.setLayout(new MigLayout("", "grow", "grow"));
 		outputPanel.add(outputScrollPane, "grow");
 		
 		optionsPanel.setLayout(new MigLayout("", "grow", "grow"));
 		optionsPanel.add(filterPanel, "grow");
 		
 		scanPanel.setLayout(new CardLayout(10, 10));
     scanPanel.add(scanButton);
     scanPanel.add(cancelButton);
     
     consolePanel.setLayout(new MigLayout("", "grow", "grow"));
     consolePanel.add(consoleScrollPane, "grow");
 		
 		gamesInCommonFrame.getContentPane().setLayout(new MigLayout("", "grow", "grow"));		
 		gamesInCommonFrame.getContentPane().add(playerPanel, "grow");
 		gamesInCommonFrame.getContentPane().add(outputPanel, "grow, wrap, span 0 2");
 		gamesInCommonFrame.getContentPane().add(optionsPanel, "grow, split 2");
 		gamesInCommonFrame.getContentPane().add(scanPanel, "grow");
 		gamesInCommonFrame.getContentPane().add(consolePanel, "south");
 		
 		gamesInCommonFrame.pack();
 
 	}
 	
 	class PlayerListRenderer extends DefaultListCellRenderer {
 	  
     private static final long serialVersionUID = 1L;
 
     @Override
     public Component getListCellRendererComponent(
         JList<?> list, Object value, int index,
         boolean isSelected, boolean cellHasFocus) {
       
       super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
       
       if (value instanceof SteamId) {
         SteamId id = (SteamId) value;
         setText(id.getNickname());
         try {
           ImageIcon icon = new ImageIcon(new URL(id.getAvatarIconUrl())); 
           setIcon(icon);
         } catch (MalformedURLException e) {
           e.printStackTrace();
         }
       }
        
       return this;
     }
 
 	}
 
 	class Scanner<T,V> extends SwingWorker<T,V> {
     @Override
     protected T doInBackground() throws Exception {
       scan();
       return null;
     }
     
     @Override
     protected void done() {
       ((CardLayout) scanPanel.getLayout()).first(scanPanel);
     }
   };
 
 	/**
 	 * Removes all handlers for the given logger
 	 * 
 	 * @param logger
 	 *            to clear handlers for
 	 */
 	private void removeHandlers(Logger loggerToClear) {
 		Handler[] handlers = loggerToClear.getHandlers();
 		for (Handler handler : handlers) {
 			loggerToClear.removeHandler(handler);
 		}
 	}
 
 	/**
 	 * Displays all games from a collection in a JList format.
 	 * 
 	 * @param games
 	 *            Games to show.
 	 */
 	public void showCommonGames(final Collection<SteamGame> games) {
 
 		if (games == null) {
 			return;
 		}
 		
 		List<SteamGameWrapper> gameList = new ArrayList<SteamGameWrapper>();
 
 		for (SteamGame steamGame : games) {
 			gameList.add(new SteamGameWrapper(steamGame));
 		}
 
 		Collections.sort(gameList);
 
 		// Final count.
 		logger.log(Level.INFO, "Total games in common: " + games.size());
 
 		for (final SteamGameWrapper str : gameList) {
 		  outputListModel.addElement(str);
 		}
 
 		outputList.setSelectedIndex(0);
 		
 	}
 
 	/**
 	 * Get a list of SteamIds parallel to the playerIDList
 	 * 
 	 * @return A list of SteamIds in the same order as playerList
 	 */
 	private List<SteamId> getPlayerIDs() {
 		return playerIdList;
 	}
 
 	/**
 	 * Verifies and retrieves information on the user indicated by addPlayerText, then adds them to playerList.
 	 */
 	private void addName() {
 		// If text field not empty then add player to list.
 		// verify with Steam that the entry is a valid Steam ID and throw an error if not
 		String name = addPlayerText.getText();
 		if ((!name.isEmpty()) && (!name.equals("Enter player name..."))) {
 			try {
 				SteamId temp = gamesInCommon.checkSteamId(name);
 				// add to the list of SteamIds
 				playerIdList.add(temp);
 				// then add to the JList
 				playerListModel.addElement(temp);
 			} catch (SteamCondenserException e) {
 				logger.log(Level.SEVERE, "\"" + name + "\": " + e.getMessage());
 			}
 
 			// Afterwards clear the text field.
 			addPlayerText.setText("");
 		}
 	}
 
 	/**
 	 * Removes the currently selected player from playerList. Has no effect if nothing selected.
 	 */
 	private void removeName() {
 		if (playerList.getSelectedIndex() > -1) {
 			// remove the selected player from the SteamId list
 			playerIdList.remove(playerList.getSelectedIndex());
 			// and also from the JList
 			playerListModel.removeElement(playerList.getSelectedValue());
 		}
 	}
 
 	/**
 	 * Finds common games within the users of playerList, then applies any selected filters and displays the result
 	 */
 	private void scan() {
 		logger.log(Level.INFO, "Starting scan.");
 
 		outputListModel.clear();
 
 		// Find common games.
 		Collection<SteamGame> commonGames = gamesInCommon.findCommonGames(getPlayerIDs());
 
 		// Apply filters, if applicable.
 		List<FilterType> filters = filterPanel.getFilters();
 		if (!filters.isEmpty()) {
 			logger.log(Level.INFO, "Filtering games... ");
 			commonGames = gamesInCommon.filterGames(commonGames, filters);
 			logger.log(Level.INFO, "Filtering complete.");
 		}
 
 		// Display filtered list of common games.
 		showCommonGames(commonGames);
 	}
 }
