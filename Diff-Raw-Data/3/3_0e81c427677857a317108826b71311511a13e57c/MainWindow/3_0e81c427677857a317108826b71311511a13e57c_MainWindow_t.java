 package mcgill.ui;
 
 import javax.swing.JFrame;
 
 import mcgill.fiveCardStud.EndOfRound;
 import mcgill.game.Chat;
 import mcgill.game.Client;
 import mcgill.game.ClientEvent;
 import mcgill.game.ClientEventListener;
 import mcgill.game.Config;
 import mcgill.game.Message;
 import mcgill.game.Table;
 import mcgill.game.User;
 import mcgill.poker.Card;
 import mcgill.poker.Hand;
 
 import org.eclipse.wb.swing.FocusTraversalOnArray;
 import java.awt.Component;
 
 import javax.swing.DefaultListModel;
 import javax.swing.JOptionPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.ListSelectionModel;
 
 import javax.swing.JPanel;
 import java.awt.GridBagLayout;
 import java.awt.GridBagConstraints;
 
 import javax.swing.JTextField;
 import javax.swing.JButton;
 
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.RowSpec;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 import javax.swing.JLabel;
 import javax.swing.ImageIcon;
 import java.awt.Font;
 import java.awt.Color;
 import javax.swing.border.LineBorder;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import java.awt.Insets;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import com.jgoodies.forms.factories.FormFactory;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.JList;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import javax.swing.SwingConstants;
 import java.awt.Toolkit;
 
 
 public class MainWindow {
 	
 	private static final String OPEN_SEAT = "OPEN SEAT";
 	
 	private JFrame frame;
 	private JTextField txtGame;
 	private JTable table_1;
 	
 	private Client client;
 	private JTextField txtChatHere;
 	private JTextField txtBetAmt;
 	private JTextField textFriendName;
 
 	private int MIN;
 	private int MAX;
 	private Map<String, Hand> HANDS;
 	
 	/**
 	 * Create the application.
 	 */
 	public MainWindow(Client client) {
 		HANDS = new HashMap<String, Hand>();
 		
 		this.client = client;
 		initialize();
 	}
 	
 	public void open() {
 		frame.setVisible(true);
 	}
 	
 	public DefaultListModel getGameList() {
 		Table[] tables = client.getTables();
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < tables.length; i++) {
 			String display = tables[i].getName() + " --- " + tables[i].getUsers().size() + "/5 players";
 			DisplayWithId display_obj = new DisplayWithId(display, tables[i].getId());
 			listModel.addElement(display_obj);
 		}
 		
 		return listModel;
 	}
 	
 	public DefaultListModel getFriendList() {
 		User[] friends = client.getFriends(client.getUser().getUsername());
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < friends.length; i++) {
 			listModel.addElement(friends[i].getUsername());
 		}
 		
 		return listModel;
 	}
 	
 	public DefaultListModel getChatList() {
 		String username = client.getUser().getUsername();
 		Chat[] chats = client.getChats(username);
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < chats.length; i++) {
 			String chat_str = "";
 			Set<User> users = chats[i].getUsers();
 			for (User user : users) {
 				if (!user.getUsername().equals(username)) {
 					chat_str += user.getUsername() + " ";
 				}
 			}
 			
 			DisplayWithId display = new DisplayWithId(chat_str, chats[i].getId());
 			listModel.addElement(display);
 		}
 		
 		return listModel;
 	}
 	
 	public DefaultListModel getChatMessages(Chat chat) {
 		List<Message> messages = chat.getMessages();
 		DefaultListModel listModel = new DefaultListModel();
 		
 		for (int i = 0; i < messages.size(); i++) {
 			listModel.addElement(messages.get(i).getUsername() + ": " + messages.get(i).getMessage());
 		}
 		
 		return listModel;
 	}
 	
 	public void setTableLabels(Map<String, Integer> creditMap, JLabel[] nameLabels, JLabel[] cashLabels) {
 		for (int i = 0; i < nameLabels.length; i++) {
 			String username = nameLabels[i].getText();
 			Integer amount = creditMap.get(username);
 			
 			if (amount != null) {
 				cashLabels[i].setText(amount + "$");
 			}
 		}
 	}
 	
 	public void setTableLabels(User[] users, JLabel[] nameLabels, JLabel[] cashLabels) {
 		int i = 0;
 		
 		for (; i < users.length; i++) {
 			nameLabels[i].setText(users[i].getUsername());
 			cashLabels[i].setText(users[i].getCredits() + "$");
 		}
 		
 		for (; i < Config.MAX_PLAYERS; i++) {
 			nameLabels[i].setText(OPEN_SEAT);
 			cashLabels[i].setText("N/A");
 		}
 	}
 	
 	public void setCardLabels(Map<String, Hand> hands, JLabel[] nameLabels, JLabel[][] cardLabels) {
 		for (int i = 0; i < nameLabels.length; i++) {
 			if (!nameLabels[i].getText().equals(OPEN_SEAT)) {
 				int j = 0;
 				Hand hand = hands.get(nameLabels[i].getText());
 				if (hand == null) {
 					continue;
 				}
 				
 				for (Card card : hand) {
 					if (j == 0 && !nameLabels[i].getText().equals(client.getUser().getUsername())) {
						j++;
 						continue;
 					}
 					
 					cardLabels[i][j].setText(card.toString());
 					j++;
 				}
 				
 				for (; j < Hand.MAX_SIZE; j++) {
 					cardLabels[i][j].setText("");
 				}
 			}
 		}
 	}
 
 	/**
 	 * Initialize the contents of the frame.
 	 */
 	private void initialize() {
 		frame = new JFrame();
 		frame.setIconImage(Toolkit.getDefaultToolkit().getImage(MainWindow.class.getResource("/images/icon.png")));
 		frame.addWindowListener(new WindowAdapter() {
 			@Override
 			public void windowClosing(WindowEvent e) {
 				client.logout();
 				System.exit(0);
 			}
 		});
 		frame.setResizable(false);
 		frame.setBounds(100, 100, 900, 700);
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.getContentPane().setLayout(new FormLayout(new ColumnSpec[] {
 				ColumnSpec.decode("max(74dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("308px:grow"),
 				ColumnSpec.decode("max(90dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(9dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("71px"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(54dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,},
 			new RowSpec[] {
 				RowSpec.decode("488px"),
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("15px"),
 				RowSpec.decode("max(16dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("26px"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(13dlu;default)"),}));
 		
 		JButton btnJoinFriendsGame = new JButton("Join Friend's Game");
 		frame.getContentPane().add(btnJoinFriendsGame, "10, 2, 3, 1, right, default");
 		
 		JLabel lblChat = new JLabel("Chat");
 		lblChat.setFont(new Font("Tahoma", Font.BOLD, 14));
 		frame.getContentPane().add(lblChat, "1, 4, center, bottom");
 		
 		final JTabbedPane main = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(main, "1, 1, 4, 1, fill, fill");
 		
 		final JScrollPane allGames = new JScrollPane();
 		main.addTab("All Games", null, allGames, null);
 		allGames.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		
 		final JList listAllGames = new JList(getGameList());
 		allGames.setViewportView(listAllGames);
 		
 		JPanel createGame = new JPanel();
 		main.addTab("Create Game", null, createGame, null);
 		GridBagLayout gbl_createGame = new GridBagLayout();
 		gbl_createGame.columnWidths = new int[]{94, 181, 0, 0};
 		gbl_createGame.rowHeights = new int[]{27, 0, 0, 0, 0, 0, 0, 0, 0};
 		gbl_createGame.columnWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_createGame.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		createGame.setLayout(gbl_createGame);
 		
 		JButton btnCreate = new JButton("Create");
 		btnCreate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				client.createTable(txtGame.getText());
 				allGames.setViewportView(new JList(getGameList()));
 				main.setSelectedIndex(0);
 			}
 		});
 		
 		JLabel lblGameCreationOptions = new JLabel("  Game Creation Options:");
 		lblGameCreationOptions.setFont(new Font("Tahoma", Font.BOLD, 18));
 		GridBagConstraints gbc_lblGameCreationOptions = new GridBagConstraints();
 		gbc_lblGameCreationOptions.gridwidth = 2;
 		gbc_lblGameCreationOptions.insets = new Insets(0, 0, 5, 5);
 		gbc_lblGameCreationOptions.anchor = GridBagConstraints.NORTHWEST;
 		gbc_lblGameCreationOptions.gridx = 0;
 		gbc_lblGameCreationOptions.gridy = 1;
 		createGame.add(lblGameCreationOptions, gbc_lblGameCreationOptions);
 		
 		JLabel lblGameName = new JLabel("Game Name:");
 		GridBagConstraints gbc_lblGameName = new GridBagConstraints();
 		gbc_lblGameName.anchor = GridBagConstraints.EAST;
 		gbc_lblGameName.insets = new Insets(0, 0, 5, 5);
 		gbc_lblGameName.gridx = 0;
 		gbc_lblGameName.gridy = 3;
 		createGame.add(lblGameName, gbc_lblGameName);
 		
 		txtGame = new JTextField();
 		txtGame.setText("Game1");
 		GridBagConstraints gbc_txtGame = new GridBagConstraints();
 		gbc_txtGame.insets = new Insets(0, 0, 5, 5);
 		gbc_txtGame.fill = GridBagConstraints.HORIZONTAL;
 		gbc_txtGame.gridx = 1;
 		gbc_txtGame.gridy = 3;
 		createGame.add(txtGame, gbc_txtGame);
 		txtGame.setColumns(10);
 		GridBagConstraints gbc_btnCreate = new GridBagConstraints();
 		gbc_btnCreate.insets = new Insets(0, 0, 5, 0);
 		gbc_btnCreate.gridx = 2;
 		gbc_btnCreate.gridy = 3;
 		createGame.add(btnCreate, gbc_btnCreate);
 		
 		JPanel currentGame = new JPanel();
 		currentGame.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 				
 			}
 		});
 		main.addTab("Current Game", null, currentGame, null);
 		currentGame.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(3dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("max(10dlu;default)"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("25dlu"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("30dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(5dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("10dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("30dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("max(10dlu;default)"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("13dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("13dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				RowSpec.decode("12dlu"),
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel pWhite = new JLabel("");
 		pWhite.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar white.png")));
 		currentGame.add(pWhite, "4, 2, 1, 5, center, bottom");
 		
 		final JLabel pWhiteName = new JLabel(OPEN_SEAT);
 		currentGame.add(pWhiteName, "6, 4, 3, 1");
 		
 		final JLabel pGreyName = new JLabel(OPEN_SEAT);
 		currentGame.add(pGreyName, "20, 4, 3, 1");
 		
 		JLabel pGrey = new JLabel("");
 		pGrey.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar grey.png")));
 		currentGame.add(pGrey, "24, 2, 1, 5, center, bottom");
 		
 		final JLabel pWhiteCash = new JLabel("N/A");
 		currentGame.add(pWhiteCash, "6, 6, 3, 1");
 		
 		final JLabel pGreyCash = new JLabel("N/A");
 		currentGame.add(pGreyCash, "20, 6, 3, 1");
 		
 		JLabel pWhiteCard1 = new JLabel("");
 		pWhiteCard1.setOpaque(true);
 		pWhiteCard1.setAlignmentX(Component.CENTER_ALIGNMENT);
 		pWhiteCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard1.setBackground(Color.GRAY);
 		pWhiteCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		currentGame.add(pWhiteCard1, "4, 8, fill, fill");
 		
 		JLabel pWhiteCard2 = new JLabel("");
 		pWhiteCard2.setOpaque(true);
 		pWhiteCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard2.setBackground(Color.WHITE);
 		pWhiteCard2.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard2, "6, 8, fill, fill");
 		
 		JLabel pWhiteCard3 = new JLabel("");
 		pWhiteCard3.setOpaque(true);
 		pWhiteCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard3.setBackground(Color.WHITE);
 		pWhiteCard3.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard3, "8, 8, fill, fill");
 		
 		JLabel pWhiteCard4 = new JLabel("");
 		pWhiteCard4.setOpaque(true);
 		pWhiteCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard4.setBackground(Color.WHITE);
 		pWhiteCard4.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard4, "10, 8, fill, fill");
 		
 		JLabel pWhiteCard5 = new JLabel("");
 		pWhiteCard5.setOpaque(true);
 		pWhiteCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pWhiteCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pWhiteCard5.setBackground(Color.WHITE);
 		pWhiteCard5.setAlignmentX(0.5f);
 		currentGame.add(pWhiteCard5, "12, 8, fill, fill");
 		
 		JLabel pGreyCard5 = new JLabel("");
 		pGreyCard5.setOpaque(true);
 		pGreyCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard5.setBackground(Color.WHITE);
 		pGreyCard5.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard5, "16, 8, fill, fill");
 		
 		JLabel pGreyCard4 = new JLabel("");
 		pGreyCard4.setOpaque(true);
 		pGreyCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard4.setBackground(Color.WHITE);
 		pGreyCard4.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard4, "18, 8, fill, fill");
 		
 		JLabel pGreyCard3 = new JLabel("");
 		pGreyCard3.setOpaque(true);
 		pGreyCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard3.setBackground(Color.WHITE);
 		pGreyCard3.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard3, "20, 8, fill, fill");
 		
 		JLabel pGreyCard2 = new JLabel("");
 		pGreyCard2.setOpaque(true);
 		pGreyCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard2.setBackground(Color.WHITE);
 		pGreyCard2.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard2, "22, 8, fill, fill");
 		
 		JLabel pGreyCard1 = new JLabel("");
 		pGreyCard1.setOpaque(true);
 		pGreyCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pGreyCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pGreyCard1.setBackground(Color.GRAY);
 		pGreyCard1.setAlignmentX(0.5f);
 		currentGame.add(pGreyCard1, "24, 8, fill, fill");
 		
 		JLabel pRed = new JLabel("");
 		pRed.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar.png")));
 		currentGame.add(pRed, "4, 12, 1, 5, center, bottom");
 		
 		JLabel pYellow = new JLabel("");
 		pYellow.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar yellow.png")));
 		currentGame.add(pYellow, "24, 12, 1, 5, center, bottom");
 		
 		JLabel pRedName = new JLabel(OPEN_SEAT);
 		currentGame.add(pRedName, "6, 14, 3, 1");
 		
 		JLabel pYellowName = new JLabel(OPEN_SEAT);
 		currentGame.add(pYellowName, "20, 14, 3, 1");
 		
 		JLabel pRedCash = new JLabel("N/A");
 		currentGame.add(pRedCash, "6, 16, 3, 1");
 		
 		JLabel pYellowCash = new JLabel("N/A");
 		currentGame.add(pYellowCash, "20, 16, 3, 1");
 		
 		JLabel pRedCard1 = new JLabel("");
 		pRedCard1.setOpaque(true);
 		pRedCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard1.setBackground(Color.GRAY);
 		pRedCard1.setAlignmentX(0.5f);
 		currentGame.add(pRedCard1, "4, 18, fill, fill");
 		
 		JLabel pRedCard2 = new JLabel("");
 		pRedCard2.setOpaque(true);
 		pRedCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard2.setBackground(Color.WHITE);
 		pRedCard2.setAlignmentX(0.5f);
 		currentGame.add(pRedCard2, "6, 18, fill, fill");
 		
 		JLabel pRedCard3 = new JLabel("");
 		pRedCard3.setOpaque(true);
 		pRedCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard3.setBackground(Color.WHITE);
 		pRedCard3.setAlignmentX(0.5f);
 		currentGame.add(pRedCard3, "8, 18, fill, fill");
 		
 		JLabel pRedCard4 = new JLabel("");
 		pRedCard4.setOpaque(true);
 		pRedCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard4.setBackground(Color.WHITE);
 		pRedCard4.setAlignmentX(0.5f);
 		currentGame.add(pRedCard4, "10, 18, fill, fill");
 		
 		JLabel pRedCard5 = new JLabel("");
 		pRedCard5.setOpaque(true);
 		pRedCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pRedCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pRedCard5.setBackground(Color.WHITE);
 		pRedCard5.setAlignmentX(0.5f);
 		currentGame.add(pRedCard5, "12, 18, fill, fill");
 		
 		JLabel pYellowCard5 = new JLabel("");
 		pYellowCard5.setOpaque(true);
 		pYellowCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard5.setBackground(Color.WHITE);
 		pYellowCard5.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard5, "16, 18, fill, fill");
 		
 		JLabel pYellowCard4 = new JLabel("");
 		pYellowCard4.setOpaque(true);
 		pYellowCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard4.setBackground(Color.WHITE);
 		pYellowCard4.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard4, "18, 18, fill, fill");
 		
 		JLabel pYellowCard3 = new JLabel("");
 		pYellowCard3.setOpaque(true);
 		pYellowCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard3.setBackground(Color.WHITE);
 		pYellowCard3.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard3, "20, 18, fill, fill");
 		
 		JLabel pYellowCard2 = new JLabel("");
 		pYellowCard2.setOpaque(true);
 		pYellowCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard2.setBackground(Color.WHITE);
 		pYellowCard2.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard2, "22, 18, fill, fill");
 		
 		JLabel pYellowCard1 = new JLabel("");
 		pYellowCard1.setOpaque(true);
 		pYellowCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pYellowCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pYellowCard1.setBackground(Color.GRAY);
 		pYellowCard1.setAlignmentX(0.5f);
 		currentGame.add(pYellowCard1, "24, 18, fill, fill");
 		
 		JLabel pBlue = new JLabel("");
 		pBlue.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar blue.png")));
 		currentGame.add(pBlue, "10, 24, 1, 3, center, center");
 		
 		JLabel pBlueName = new JLabel(OPEN_SEAT);
 		currentGame.add(pBlueName, "12, 24, 3, 1");
 		
 		JLabel pBlueCard1 = new JLabel("");
 		pBlueCard1.setOpaque(true);
 		pBlueCard1.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard1.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard1.setBackground(Color.GRAY);
 		pBlueCard1.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard1, "16, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard2 = new JLabel("");
 		pBlueCard2.setOpaque(true);
 		pBlueCard2.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard2.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard2.setBackground(Color.WHITE);
 		pBlueCard2.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard2, "18, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard3 = new JLabel("");
 		pBlueCard3.setOpaque(true);
 		pBlueCard3.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard3.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard3.setBackground(Color.WHITE);
 		pBlueCard3.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard3, "20, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard4 = new JLabel("");
 		pBlueCard4.setOpaque(true);
 		pBlueCard4.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard4.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard4.setBackground(Color.WHITE);
 		pBlueCard4.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard4, "22, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCard5 = new JLabel("");
 		pBlueCard5.setOpaque(true);
 		pBlueCard5.setForeground(Color.BLACK);
 		pBlueCard5.setFont(new Font("Arial", Font.BOLD, 20));
 		pBlueCard5.setBorder(new LineBorder(new Color(0, 0, 0)));
 		pBlueCard5.setBackground(Color.WHITE);
 		pBlueCard5.setAlignmentX(0.5f);
 		currentGame.add(pBlueCard5, "24, 24, 1, 3, fill, fill");
 		
 		JLabel pBlueCash = new JLabel("N/A");
 		currentGame.add(pBlueCash, "12, 26, 3, 1");
 		
 		final JLabel[] nameLabels = { pWhiteName, pGreyName, pRedName, pYellowName, pBlueName };
 		final JLabel[] cashLabels = { pWhiteCash, pGreyCash, pRedCash, pYellowCash, pBlueCash };
 		final JLabel[][] cardLabels = { {pWhiteCard1, pWhiteCard2, pWhiteCard3, pWhiteCard4, pWhiteCard5}
 									  , {pGreyCard1, pGreyCard2, pGreyCard3, pGreyCard4, pGreyCard5}
 									  , {pRedCard1, pRedCard2, pRedCard3, pRedCard4, pRedCard5}
 									  , {pYellowCard1, pYellowCard2, pYellowCard3, pYellowCard4, pYellowCard5}
 									  , {pBlueCard1, pBlueCard2, pBlueCard3, pBlueCard4, pBlueCard5}};
 		
 		final JButton btnFold = new JButton("Fold");
 		btnFold.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				ClientEvent event = new ClientEvent(new Object());
 				event.setType(ClientEvent.ACTION_REC);
 				event.setAction(-1);
 				
 				client.fireEvent(event);
 			}
 		});
 		
 		final JButton btnBet = new JButton("Bet");
 		btnBet.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				int amount = Integer.parseInt(txtBetAmt.getText());
 				
 				if (amount < MIN) {
 					JOptionPane.showMessageDialog(frame, "You need to meet the call amount of " + MIN + "!");
 					return;
 				} else if (amount > MAX) {
 					JOptionPane.showMessageDialog(frame, "You cannot bet anymore than " + MAX + "!");
 					return;
 				}
 				
 				ClientEvent event = new ClientEvent(new Object());
 				event.setType(ClientEvent.ACTION_REC);
 				
 				event.setAction(amount);
 				
 				client.fireEvent(event);
 			}
 		});
 		currentGame.add(btnBet, "10, 30, 3, 1");
 		currentGame.add(btnFold, "16, 30, 3, 1");
 		
 		JLabel lblPot = new JLabel("Pot:");
 		lblPot.setHorizontalAlignment(SwingConstants.CENTER);
 		lblPot.setFont(new Font("Lucida Grande", Font.BOLD, 13));
 		currentGame.add(lblPot, "4, 32");
 		
 		final JLabel lblPotCash = new JLabel("N/A");
 		currentGame.add(lblPotCash, "6, 32");
 		
 		txtBetAmt = new JTextField();
 		txtBetAmt.setText("0");
 		currentGame.add(txtBetAmt, "10, 32, 3, 1, fill, default");
 		txtBetAmt.setColumns(10);
 		
 		JButton btnStart = new JButton("Start");
 		btnStart.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				client.startRound(client.getTable().getId());
 			}
 		});
 		currentGame.add(btnStart, "4, 30, 3, 1");
 		
 		final JButton btnCheck = new JButton("Check");
 		btnCheck.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				
 				if (0 < MIN) {
 					JOptionPane.showMessageDialog(frame, "You need to call or fold!");
 					return;
 				}
 				
 				ClientEvent event = new ClientEvent(new Object());
 				event.setType(ClientEvent.ACTION_REC);
 				event.setAction(0);
 				
 				client.fireEvent(event);
 			}
 		});
 		currentGame.add(btnCheck, "16, 32, 3, 1");
 		
 		final JTabbedPane friends = new JTabbedPane(JTabbedPane.TOP);
 		frame.getContentPane().add(friends, "6, 1, 7, 1, fill, fill");	
 	
 		final JScrollPane allFriends = new JScrollPane();
 		allFriends.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		allFriends.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		friends.addTab("Friends", null, allFriends, null);
 
 		allFriends.setViewportView(table_1);		
 		
 		final JList listFriends = new JList(getFriendList());
 		allFriends.setViewportView(listFriends);
 		
 		JPanel addFriend = new JPanel();
 		friends.addTab("Add Friend", null, addFriend, null);
 		addFriend.setLayout(new FormLayout(new ColumnSpec[] {
 				FormFactory.RELATED_GAP_COLSPEC,
 				FormFactory.DEFAULT_COLSPEC,
 				FormFactory.RELATED_GAP_COLSPEC,
 				ColumnSpec.decode("default:grow"),},
 			new RowSpec[] {
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,
 				FormFactory.RELATED_GAP_ROWSPEC,
 				FormFactory.DEFAULT_ROWSPEC,}));
 		
 		JLabel lblFriend = new JLabel("Friend:");
 		addFriend.add(lblFriend, "2, 2, right, default");
 		
 		textFriendName = new JTextField();
 		addFriend.add(textFriendName, "4, 2, fill, default");
 		textFriendName.setColumns(10);
 		
 		JButton btnAddFriend = new JButton("Add Friend");
 		btnAddFriend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				Boolean result = client.addFriend(client.getUser().getUsername(), textFriendName.getText());
 				
 				if (result) {
 					allFriends.setViewportView(new JList(getFriendList()));
 					friends.setSelectedIndex(0);
 				} else {
 					JOptionPane.showMessageDialog(frame, "Friend not found :(");
 				}
 			}
 		});
 		addFriend.add(btnAddFriend, "4, 4");
 		
 		final JScrollPane scrollPane = new JScrollPane();
 		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.getContentPane().add(scrollPane, "1, 5, 1, 7, fill, fill");
 		
 		final JScrollPane chatContainer = new JScrollPane();
 		chatContainer.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.getContentPane().add(chatContainer, "3, 5, 2, 5, fill, fill");
 		
 		final JList listChatArea = new JList();
 		chatContainer.setViewportView(listChatArea);
 		
 		final JList listChats = new JList(getChatList());
 		ListSelectionModel chatSelection = listChats.getSelectionModel();
 		chatSelection.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				JList chats = (JList) scrollPane.getViewport().getView();
 				DisplayWithId chat_display = (DisplayWithId) chats.getSelectedValue();
 				if (chat_display == null) {
 					return;
 				}
 				
 				String chat_id = chat_display.getId();
 				
 				Chat chat = client.getChat(chat_id);
 				JList chat_messages = new JList(getChatMessages(chat));
 				chatContainer.setViewportView(chat_messages);
 
 				chatContainer.getVerticalScrollBar().setValue(chat_messages.getHeight());
 			}
 		});
 		scrollPane.setViewportView(listChats);
 		
 		JLabel label = new JLabel("");
 		label.setIcon(new ImageIcon(MainWindow.class.getResource("/images/avatar main.png")));
 		frame.getContentPane().add(label, "8, 5, 1, 3, center, bottom");
 		
 		final JLabel cash = new JLabel(client.getUser().getCredits() + "$");
 		frame.getContentPane().add(cash, "8, 11, center, top");
 		
 		JButton btnAddCredit = new JButton("Add Credit");
 		btnAddCredit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				User user = client.addCredits(client.getUser().getUsername());
 				client.setUser(user);
 				cash.setText(user.getCredits() + "$");
 			}
 		});
 		frame.getContentPane().add(btnAddCredit, "12, 7");
 		
 		JLabel lblScreenName = new JLabel(this.client.getUser().getUsername());
 		frame.getContentPane().add(lblScreenName, "8, 9, center, center");
 		
 		JButton btnLogOut = new JButton("Log Out");
 		btnLogOut.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				client.logout();
 				LoginPage login = new LoginPage();
 				login.open(client);
 				frame.setVisible(false);
 				
 			}
 		});
 		frame.getContentPane().add(btnLogOut, "12, 9");
 		
 		txtChatHere = new JTextField();
 		txtChatHere.setText("Chat here");
 		frame.getContentPane().add(txtChatHere, "3, 11, fill, fill");
 		txtChatHere.setColumns(10);
 		
 		JButton btnSend = new JButton("Send");
 		btnSend.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JList chatList = (JList) scrollPane.getViewport().getView();
 				DisplayWithId chat_display = (DisplayWithId) chatList.getSelectedValue();
 				
 				if (chat_display == null) {
 					JOptionPane.showMessageDialog(frame, "No Chat Selected");
 					return;
 				}
 				
 				String chat_id = chat_display.getId();
 				String message = txtChatHere.getText();
 				
 				client.sendMessage(client.getUser().getUsername(), message, chat_id);
 	
 			}
 		});
 		frame.getContentPane().add(btnSend, "4, 11");
 				
 		JButton btnQuit = new JButton("Quit");
 		btnQuit.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				client.logout();
 				System.exit(0);
 			}
 		});
 		frame.getContentPane().add(btnQuit, "12, 11");
 		frame.setFocusTraversalPolicy(new FocusTraversalOnArray(new Component[]{frame.getContentPane()}));
 		
 		JButton btnRefresh = new JButton("Refresh");
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				allGames.setViewportView(new JList(getGameList()));
 				allFriends.setViewportView(new JList(getFriendList()));
 				
 				// HACKY TODO Remove
 				JList chatList = new JList(getChatList());
 				
 				ListSelectionModel chatSelection = chatList.getSelectionModel();
 				chatSelection.addListSelectionListener(new ListSelectionListener() {
 					public void valueChanged(ListSelectionEvent e) {
 						JList chats = (JList) scrollPane.getViewport().getView();
 						DisplayWithId chat_display = (DisplayWithId) chats.getSelectedValue();
 						if (chat_display == null) {
 							return;
 						}
 						
 						String chat_id = chat_display.getId();
 						
 						Chat chat = client.getChat(chat_id);
 						JList chat_messages = new JList(getChatMessages(chat));
 						chatContainer.setViewportView(chat_messages);
 
 						chatContainer.getVerticalScrollBar().setValue(chat_messages.getHeight());}
 				});
 				
 				scrollPane.setViewportView(chatList);
 			}
 		});
 		frame.getContentPane().add(btnRefresh, "1, 2, left, default");
 		
 		JButton btnChat = new JButton("Chat");
 		btnChat.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JList friendList = (JList) allFriends.getViewport().getView();
 				String username = (String) friendList.getSelectedValue();
 				
 				if (username == null) {
 					JOptionPane.showMessageDialog(frame, "No Friend Selected");
 					return;
 				}
 				
 				client.createChat(client.getUser().getUsername(), username);
 				scrollPane.setViewportView(new JList(getChatList()));
 			}
 		});
 		frame.getContentPane().add(btnChat, "6, 2, 3, 1, left, default");
 		
 		JButton btnJoinGame = new JButton("Join Game");
 		btnJoinGame.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				JList games = (JList) allGames.getViewport().getView();
 				
 				DisplayWithId table_display = (DisplayWithId) games.getSelectedValue();
 				if (table_display == null) {
 					JOptionPane.showMessageDialog(frame, "No Game Selected");
 					return;
 				}
 				
 				String table_id = table_display.getId();
 				
 				Boolean result = client.joinTable(client.getUser().getUsername(), table_id);
 				if (!result) {
 					JOptionPane.showMessageDialog(frame, "Error Joining Table");
 				}
 				
 				main.setSelectedIndex(2);
 				
 				setTableLabels(client.getTable().getUsers().toArray(new User[0]), nameLabels, cashLabels);
 			}
 		});
 		frame.getContentPane().add(btnJoinGame, "4, 2");
 		
 		client.addEventListener(new ClientEventListener() {			
 			public void eventOccured(ClientEvent e) {
 				if (e.getType() == ClientEvent.ACTION_GET) {
 					JOptionPane.showMessageDialog(frame, "It's your turn, Min bet: " + e.getLimits()[0] + ", Max bet: " + e.getLimits()[1]);
 					MIN = e.getLimits()[0];
 					MAX = e.getLimits()[1];
 				}
 				
 				if (e.getType() == ClientEvent.HAND) {
 					HANDS = e.getHands();
 					setCardLabels(e.getHands(), nameLabels, cardLabels);
 				}
 				
 				if (e.getType() == ClientEvent.USER) {
 					User[] users = e.getUsers();
 					setTableLabels(users, nameLabels, cashLabels);
 					setCardLabels(HANDS, nameLabels, cardLabels);
 				}
 				
 				if (e.getType() == ClientEvent.POT_STATUS) {
 					int[] current = e.getPotStatus();
 					lblPotCash.setText(current[0] + "$");
 				}
 				
 				if (e.getType() == ClientEvent.MESSAGE) {
 					JList chatList = (JList) scrollPane.getViewport().getView();
 					DisplayWithId chat_display = (DisplayWithId) chatList.getSelectedValue();
 					
 					if (chat_display == null) {
 						scrollPane.setViewportView(new JList(getChatList()));
 						return;
 					}
 					
 					String selected_chat = chat_display.getId();
 					
 					if (selected_chat.equals(e.getChatId())) {
 						Chat chat = client.getChat(selected_chat);
 						chatContainer.setViewportView(new JList(getChatMessages(chat)));
 					}
 					
 					scrollPane.setViewportView(new JList(getChatList()));
 					
 					
 					String chat_id = chat_display.getId();
 					Chat chat = client.getChat(chat_id);
 					JList chat_messages = new JList(getChatMessages(chat));
 					chatContainer.getVerticalScrollBar().setValue(chat_messages.getHeight());
 				}
 				
 				if (e.getType() == ClientEvent.END_OF_ROUND) {
 					EndOfRound end = e.getEndOfRound();
 					setTableLabels(end.getCreditMap(), nameLabels, cashLabels);
 					cash.setText(end.getCreditMap().get(client.getUser().getUsername()) + "$");
 					JOptionPane.showMessageDialog(frame, "Round Over & The winner is: " + end.getWinner());
 				}
 			}
 		});
 	}
 }
