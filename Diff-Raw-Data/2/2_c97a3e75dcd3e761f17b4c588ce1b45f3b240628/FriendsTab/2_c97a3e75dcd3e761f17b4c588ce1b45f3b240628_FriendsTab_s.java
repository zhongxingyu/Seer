 package com.tranek.chivalryserverbrowser;
 import java.awt.BorderLayout;
 import java.awt.Cursor;
 import java.awt.Toolkit;
 import java.awt.datatransfer.Clipboard;
 import java.awt.datatransfer.ClipboardOwner;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.StringSelection;
 import java.awt.datatransfer.Transferable;
 import java.awt.datatransfer.UnsupportedFlavorException;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseMotionAdapter;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.ScrollPaneConstants;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 /**
  * 
  * The tab that contains the Steam friends list.
  *
  */
 @SuppressWarnings("serial")
 public class FriendsTab extends JPanel {
 	
 	/** A reference to the MainWindow. */
 	protected final MainWindow mw;
 	/** A reference to itself to pass to the {@link FriendQuery}. */
 	protected final FriendsTab ft;
 	/** The JTable containing the user's Steam friends. */
 	private JTable playerListTable;
 	/** The URL to the user's Steam Community page. */
 	protected JTextField urlField;
 	/** Data model for the Steam friend table. */
 	protected TableModel dataModel;
 	/** Column headers for the Steam friend table. */
 	private final String[] playerListColumnHeaders = {"Player Name", "Status", "Server Name", "IP Address:Port", "Players", "Ping", "Password"};
 	/** The user's {@link SteamProfile}. */
 	protected SteamProfile steamProfile = null;
 	/** Label for the user's Steam nickname. */
 	protected JLabel lblPlayerName;
 	/** Whether or not the user only wants to show Steam friends currently in Chivalry: Medieval Warfare. */
 	protected JCheckBox chckbxInChiv;
 	/** The FriendQuery that parses and queries all of the user's Steam friends from his/her Steam Community profile page. */
 	protected FriendQuery fq;
 	
 	/**
 	 * Creates a new FriendsTab.
 	 * 
 	 * @param mw the MainWindow with utility methods
 	 */
 	public FriendsTab(MainWindow mw) {
 		super();
 		this.mw = mw;
 		ft = this;
 		initialize();
 	}
 	
 	/**
 	 * Initializes the JPanel and adds all children components to it.
 	 */
 	public void initialize() {
 		setLayout(new BorderLayout(0, 0));
 		
 		JPanel settingsPanel = new JPanel();
 		add(settingsPanel, BorderLayout.NORTH);
 		settingsPanel.setLayout(new BorderLayout(0, 0));
 		
 		JPanel urlPanel = new JPanel();
 		settingsPanel.add(urlPanel, BorderLayout.NORTH);
 		
 		JLabel lblSteamCommunityUrl = new JLabel("Steam Community URL:");
 		urlPanel.add(lblSteamCommunityUrl);
 		
 		urlField = new JTextField();
 		urlPanel.add(urlField);
 		urlField.setToolTipText("Enter your Steam Community URL such as http://steamcommunity.com/id/ReMixx or http://steamcommunity.com/profiles/76561197970611950. Your profile must be set to PUBLIC!");
 		urlField.setColumns(40);
 		urlField.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				if ( e.getButton() == MouseEvent.BUTTON3 ) {
 					JPopupMenu rmbURLPopup = new JPopupMenu();
 					JMenuItem popPaste = new JMenuItem("Paste");
 					popPaste.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 							Transferable contents = clipboard.getContents(null);
 						    boolean hasTransferableText =
 						      (contents != null) &&
 						      contents.isDataFlavorSupported(DataFlavor.stringFlavor);
 						    if ( hasTransferableText ) {
 						    	try {
 									String result = (String)contents.getTransferData(DataFlavor.stringFlavor);
 									urlField.setText(result);
 								} catch (UnsupportedFlavorException
 										| IOException e1) {
 									e1.printStackTrace();
 								} 
 						    }
 						}
 					});
 					rmbURLPopup.add(popPaste);
 					rmbURLPopup.show(urlField, e.getPoint().x, e.getPoint().y);
 				}
 			}
 		});
 		
 		JButton btnRefresh = new JButton("Refresh");
 		urlPanel.add(btnRefresh);
 		
 		JLabel lblPlayer = new JLabel("Player:");
 		urlPanel.add(lblPlayer);
 		
 		lblPlayerName = new JLabel();
 		urlPanel.add(lblPlayerName);
 		
 		JPanel settingsPanel2 = new JPanel();
 		settingsPanel.add(settingsPanel2, BorderLayout.SOUTH);
 		
 		//TODO add beta
 		chckbxInChiv = new JCheckBox("Show friends only in Chivalry: Medieval Warfare");
 		chckbxInChiv.setSelected(true);
 		settingsPanel2.add(chckbxInChiv);
 		
 		btnRefresh.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				refreshFriends();
 			}
 		});
 		
 		dataModel = new DefaultTableModel(playerListColumnHeaders, 0) {
 			public boolean isCellEditable(int row, int column){  
 				return false;  
 			}
 			public Class<?> getColumnClass(int columnIndex) {
 				if ( columnIndex == 4 || columnIndex == 5 ) {
 					return Integer.class;
 				}
 				return super.getColumnClass(columnIndex);
 			}
 		};
 		
 		playerListTable = new JTable(dataModel);
 		playerListTable.setAutoCreateRowSorter(true);
 		TableRowSorter<?> trs = (TableRowSorter<?>) playerListTable.getRowSorter();
 		trs.setComparator(4, new PlayerComparator());
 		trs.toggleSortOrder(2);
 		trs.toggleSortOrder(2);
 		playerListTable.getColumnModel().getColumn(3).setMaxWidth(400); // IP address
 		playerListTable.getColumnModel().getColumn(3).setPreferredWidth(130); // IP address
 		playerListTable.getColumnModel().getColumn(4).setMaxWidth(70); // Player count
 		playerListTable.getColumnModel().getColumn(5).setMaxWidth(70); // Ping
 		playerListTable.getColumnModel().getColumn(6).setMaxWidth(80); // Password
 		playerListTable.addMouseMotionListener(new MouseMotionAdapter() {
 			@Override
 			public void mouseMoved(MouseEvent e) {
 				int row = playerListTable.rowAtPoint(e.getPoint());
 				int col = playerListTable.columnAtPoint(e.getPoint());
 				
 				if ( col == 3 && ((String)dataModel.getValueAt(row, col)) != null && 
 						!((String)dataModel.getValueAt(row, col)).equals("") ) {
 					setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
 				} else {
 					setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
 				}
 			}
 		});
 		playerListTable.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 				final int tableRow = playerListTable.rowAtPoint(e.getPoint());
 				final int tableCol = playerListTable.columnAtPoint(e.getPoint());
 				
 				// Fix by gregcau http://www.chivalrythegame.com/forums/viewtopic.php?f=69&t=10664&start=30#p99728
 				final int row = playerListTable.convertRowIndexToModel(tableRow);
 	            final int col = playerListTable.convertColumnIndexToModel(tableCol);
 				
 				if ( e.getButton() == MouseEvent.BUTTON1 && col == 3 &&
 						((String)dataModel.getValueAt(row, col)) != null ) {
 					String stripped = ((String)dataModel.getValueAt(row, 3)).substring(26);
 					stripped = stripped.split("<")[0];
 					String[] ipaddress = stripped.split(":");
 					String ip = ipaddress[0];
 					String port = ipaddress[1];
 					String urlstring = "steam://run/219640/en/" + ip + ":" + port;
 					String friendName = (String)dataModel.getValueAt(row, 0);
 					mw.joinServer(urlstring, ip, port, friendName);
 				}
 				
 				// Double click server name
 				if ( e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 2 && col == 2 ) {
 					String stripped = ((String)dataModel.getValueAt(row, 3)).substring(26);
 					stripped = stripped.split("<")[0];
 					System.out.println("IP_port = " + stripped);
 					mw.addServerTab(stripped, true);
 				}
 				
 				// Right click row
 				if ( e.getButton() == MouseEvent.BUTTON3 && !((String)dataModel.getValueAt(row, 2)).equals("") ) {
 					playerListTable.setRowSelectionInterval(tableRow, tableRow);
 					JPopupMenu rmbServerPopup = new JPopupMenu();
 					String stripped = ((String)dataModel.getValueAt(row, 3)).substring(26);
 					stripped = stripped.split("<")[0];
 					String[] ipaddress = stripped.split(":");
 					final String ip = ipaddress[0];
 					final String port = ipaddress[1];
 					JMenuItem popFav = new JMenuItem("Add to favorites");
 					popFav.addActionListener(new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							ChivServer cs = mw.findChivServer(ip, port, mw.serversFriends);
 							if (cs == null) {
 								cs = mw.getServerFromDB(ip, port);
 							}
 							mw.addFavorite(cs);
 						}
 					});
 					rmbServerPopup.add(popFav);
 					JMenuItem popInfo = new JMenuItem("Server details");
 					popInfo.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							ChivServer cs = mw.findChivServer(ip, port, mw.serversFriends);
 							if (cs == null) {
 								cs = mw.getServerFromDB(ip, port);
 							}
 							mw.addServerTab(cs, true);
 						}
 					});
 					rmbServerPopup.add(popInfo);
 					JMenuItem popCopy = new JMenuItem("Copy server name to Clipboard");
 					popCopy.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
 							StringSelection stringSelection = new StringSelection( (String)dataModel.getValueAt(row, 2) );
 							clipboard.setContents(stringSelection, new ClipboardOwner() {
 								@Override
 								public void lostOwnership(Clipboard clipboard, Transferable contents) {}
 							});
 						}
 					});
 					rmbServerPopup.add(popCopy);
 					JMenuItem popShowInMap = new JMenuItem("Show server in map");
 					popShowInMap.addActionListener(new ActionListener() {
 						@Override
 						public void actionPerformed(ActionEvent e) {
 							ChivServer cs = mw.findChivServer(ip, port, mw.serversFriends);
 							if (cs == null) {
 								cs = mw.getServerFromDB(ip, port);
 							}
 							mw.showInMap(cs);
 						}
 					});
 					rmbServerPopup.add(popShowInMap);
 					rmbServerPopup.show(playerListTable, e.getPoint().x, e.getPoint().y);
 				}
 			
 			}
 		});		
 		JScrollPane playerListScrollPane = new JScrollPane(playerListTable); // Adds table directly to scrollpane
 		playerListScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 		playerListScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		add(playerListScrollPane, BorderLayout.CENTER);
 	}
 	
 	/**
 	 * Refreshes the user's list of Steam friends. This is called whenever the
 	 * refresh button is clicked. This needs to be called after the JCheckBox
 	 * for displaying only friends in Chivalry versus all friends is checked or
 	 * unchecked to refresh the list of Steam friends.
 	 */
 	public void refreshFriends() {
		if ( fq != null && !fq.pool.isShutdown() ) {
 			fq.pool.shutdownNow();
 		}
 		mw.serversFriends = new Vector<ChivServer>();
 		String url = urlField.getText();
 		((DefaultTableModel)dataModel).setRowCount(0);
 		fq = new FriendQuery(mw, ft, url);
 		fq.start();
 		mw.settingsTab.tfSteamCommunityUrl.setText(url);
 	}
 	
 }
