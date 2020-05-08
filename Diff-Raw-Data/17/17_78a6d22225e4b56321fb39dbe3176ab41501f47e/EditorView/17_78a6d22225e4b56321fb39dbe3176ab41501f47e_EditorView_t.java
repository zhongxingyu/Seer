 package editor.view;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.HashMap;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.Set;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.JTextField;
 
 import com.jgoodies.forms.factories.FormFactory;
 import com.jgoodies.forms.layout.ColumnSpec;
 import com.jgoodies.forms.layout.FormLayout;
 import com.jgoodies.forms.layout.RowSpec;
 
 import model.Room;
 import model.object.Player;
 import view.FirstPersonRoom;
 import editor.controller.EditorListener;
 import editor.controller.EditorUpdateObject;
 
 public class EditorView extends Observable implements Observer {
 
 	private static final String SOUTH = "south";
 	private static final String EAST = "east";
 	private static final String WEST = "west";
 	private static final String NORTH = "north";
 
 	private static final String REMOVE_ITEM = "removeItem";
 	private static final String ADD_ITEM = "addItem";
 	private static final String REMOVE_MONSTER = "removeMonster";
 	private static final String ADD_MONSTER = "addMonster";
 	private static final String REMOVE_EXIT = "removeExit";
 	private static final String ADD_EXIT = "addExit";
 	private static final String REMOVE_ROOM = "removeRoom";
 	private static final String ADD_ROOM = "addRoom";
 	private static final String LOOK = "look";
 	private static final String SAVE = "save";
 	private static final String PLAY = "play";
 
 	private GridLayout gridLayout;
 	private BorderLayout borderLayout;
 	private EditorMap map;
 	private FirstPersonRoom currentRoom;
 	private JPanel gamePanel;
 	private JFrame mainFrame;
 
 	private String[][] roomsArray;
 	private HashMap<String, FirstPersonRoom> rooms;
 	private int x;
 	private int y;
 
 	private int maxX;
 	private int maxY;
 
 	private EditorToolsPanel toolsPanel;
 
 	private Player player;
 
 	private Set<String> items;
 	private Set<String> monsters;
 
 	public EditorView(String name, int maxX, int maxY,
 			EditorListener mouseListener, Set<String> items,
 			Set<String> monsters) {
 		gamePanel = new JPanel();
 		mainFrame = new JFrame(name);
 
 		// Initialize the layout
 		gridLayout = new GridLayout(1, 2);
 		borderLayout = new BorderLayout();
 		gamePanel.setLayout(gridLayout);
 		mainFrame.getContentPane().setLayout(borderLayout);
 		mainFrame.add(gamePanel, BorderLayout.CENTER);
 
 		// Set the size of the game
 		this.maxX = maxX;
 		this.maxY = maxY;
 
 		// Initialize the starting position to (0,0)
 		x = 0;
 		y = 0;
 
 		// Initialize the map
 		map = new EditorMap("Map", maxX, maxY, mouseListener);
 
 		// Setup the window
 		mainFrame.setSize(1505, 600);
 		mainFrame.setResizable(false);
 		// mainFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		// mainFrame.setBackground(Color.BLACK);
 
 		currentRoom = null;
 
 		this.items = items;
 		this.monsters = monsters;
 
 		toolsPanel = new EditorToolsPanel();
 
 		roomsArray = new String[maxX][maxY];
 		rooms = new HashMap<String, FirstPersonRoom>();
 
 		player = new Player(null, 0, 0);
 	}
 
 	public void update(Observable arg0, Object arg1) {
 		if (arg1 instanceof String) {
 			// A command was passed through by the toolPane
 		}
 
 		if (arg1 instanceof EditorUpdateObject) {
 			EditorUpdateObject update = (EditorUpdateObject) arg1;
 
 			x = update.getSelectedX();
 			y = update.getSelectedY();
 
 			roomsArray = update.getRoomsArray();
 			rooms = update.getRooms();
 			player = update.getPlayer();
 		}
 
 		if (x >= 0 && x < maxX && y >= 0 && y < maxY) {
 			if (roomsArray[x][y] != null) {
 				currentRoom = (FirstPersonRoom) rooms.get(roomsArray[x][y]);
 			} else {
 				currentRoom = null;
 			}
 		}
 		map.update(this, arg1);
 		refreshView();
 
 	}
 
 	private void refreshView() {
 		// Remove everything from the gamePanel and mainFrame
 		gamePanel.removeAll();
 		mainFrame.getContentPane().removeAll();
 
 		// Remove everything from the glass pane
 		JPanel glassPane = (JPanel) mainFrame.getGlassPane();
 		glassPane.removeAll();
 		glassPane.setLayout(null);
 
 		if (roomsArray[x][y] != null) {
 			JLabel arrow = new JLabel("");
 			if (player.getLookingDirection().equals(NORTH)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/north.png")));
 			} else if (player.getLookingDirection().equals(SOUTH)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/south.png")));
 			} else if (player.getLookingDirection().equals(EAST)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/east.png")));
 			} else if (player.getLookingDirection().equals(WEST)) {
 				arrow.setIcon(new ImageIcon(FirstPersonRoom.class
 						.getResource("/img/firstperson/arrow/west.png")));
 			}
 			arrow.setBounds(1180, 300, 40, 40);
 			glassPane.add(arrow);
 			glassPane.setVisible(true);
 
 		}
 
 		// Add the 3D perspective and the map perspective
 		if (currentRoom != null) {
 			gamePanel.add(currentRoom.getView(player.getLookingDirection()));
 		} else {
 			JPanel blank = new JPanel();
 			blank.setSize(600, 600);
 			blank.add(new JLabel("Please select a room"));
 			gamePanel.add(blank);
 		}
 		gamePanel.add(map.getContentPane());
 
 		// Repaint the gamePanel
 		gamePanel.validate();
 		gamePanel.repaint();
 
 		// Add the gamePanel to mainFrame
 		mainFrame.add(gamePanel, BorderLayout.CENTER);
 		mainFrame.add(toolsPanel, BorderLayout.WEST);
 
 		// Repaint the mainFrame
 		mainFrame.validate();
 		mainFrame.repaint();
 
 	}
 
 	public void show() {
 		refreshView();
 		mainFrame.setVisible(true);
 	}
 
 	public class EditorToolsPanel extends JPanel {
 
 		private static final long serialVersionUID = 1L;
 		private JTextField xmlName;
 		private JTextField startingRoom;
 
 		/**
 		 * Create the panel.
 		 */
 		public EditorToolsPanel() {
 			setLayout(new FormLayout(new ColumnSpec[] {
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC,
 					ColumnSpec.decode("default:grow"),
 					FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] {
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC,
 					FormFactory.RELATED_GAP_ROWSPEC,
 					FormFactory.DEFAULT_ROWSPEC, }));
 
 			JButton btnCreateRoom = new JButton("Create Room");
 			btnCreateRoom.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					setChanged();
 					notifyObservers(ADD_ROOM);
 				}
 			});
 			add(btnCreateRoom, "2, 2, 3, 1");
 
 			final JComboBox directionBox = new JComboBox();
 			directionBox.setModel(new DefaultComboBoxModel(new String[] {
 					"Select One", "north", "south", "east", "west" }));
 
 			JButton btnDeleteRoom = new JButton("Delete Room");
 			btnDeleteRoom.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(REMOVE_ROOM);
 				}
 			});
 			add(btnDeleteRoom, "2, 4, 3, 1");
 
 			JSeparator separator = new JSeparator();
 			add(separator, "2, 6, 3, 1");
 
 			JLabel lblDirection_1 = new JLabel("Exit Direction:");
 			add(directionBox, "4, 8, fill, default");
 
 			final JComboBox monstersBox = new JComboBox();
 			monstersBox.setModel(new DefaultComboBoxModel(monsters.toArray()));
 
 			JButton btnDeleteExit = new JButton("Delete Exit");
 			btnDeleteExit.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(REMOVE_EXIT + ","
 							+ directionBox.getSelectedItem());
 				}
 			});
 			add(btnDeleteExit, "2, 10");
 
 			JButton btnAddExit = new JButton("Add Exit");
 			btnAddExit.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(ADD_EXIT + ","
 							+ directionBox.getSelectedItem());
 				}
 			});
 			add(btnAddExit, "4, 10");
 
 			JSeparator separator_1 = new JSeparator();
 			add(separator_1, "2, 12, 3, 1");
 
 			JLabel lblMonster = new JLabel("Monster:");
 			add(lblMonster, "2, 14, right, default");
 			monstersBox.setToolTipText("monster");
 			add(monstersBox, "4, 14, fill, default");
 
 			JButton btnAddMonster = new JButton("Add Monster");
 			btnAddMonster.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(ADD_MONSTER + ","
 							+ monstersBox.getSelectedItem());
 				}
 			});
 			add(btnAddMonster, "2, 16");
 
 			JButton btnDeleteMonster = new JButton("Delete Monster");
 			btnDeleteMonster.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(REMOVE_MONSTER + ","
 							+ monstersBox.getSelectedItem());
 				}
 			});
 			add(btnDeleteMonster, "4, 16");
 
 			JSeparator separator_2 = new JSeparator();
 			add(separator_2, "2, 18, 3, 1");
 
 			JLabel lblItem = new JLabel("Item");
 			add(lblItem, "2, 20, right, default");
 
 			final JComboBox itemBox = new JComboBox();
 			itemBox.setModel(new DefaultComboBoxModel(items.toArray()));
 			add(itemBox, "4, 20, fill, default");
 
 			JButton btnDeleteItem = new JButton("Delete Item");
 			btnDeleteItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(REMOVE_ITEM + ","
 							+ itemBox.getSelectedItem());
 				}
 			});
 			add(btnDeleteItem, "2, 22");
 
 			JButton btnAddItem = new JButton("Add Item");
 			btnAddItem.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(ADD_ITEM + "," + itemBox.getSelectedItem());
 				}
 			});
 			add(btnAddItem, "4, 22");
 
 			JSeparator separator_3 = new JSeparator();
 			add(separator_3, "2, 24, 3, 1");
 
 			JButton btnTurnLeft = new JButton("Turn Left");
 			btnTurnLeft.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					String lookingDirection = "";
 
 					if (player.getLookingDirection().equals(NORTH)) {
 						lookingDirection = WEST;
 					} else if (player.getLookingDirection().equals(SOUTH)) {
 						lookingDirection = EAST;
 					} else if (player.getLookingDirection().equals(EAST)) {
 						lookingDirection = NORTH;
 					} else if (player.getLookingDirection().equals(WEST)) {
 						lookingDirection = SOUTH;
 					}
 
 					setChanged();
 					notifyObservers(LOOK + "," + lookingDirection);
 				}
 			});
 			add(btnTurnLeft, "2, 26");
 
 			JButton btnTurnRight = new JButton("Turn Right");
 			btnTurnRight.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent arg0) {
 					String lookingDirection = "";
 
 					if (player.getLookingDirection().equals(NORTH)) {
 						lookingDirection = EAST;
 					} else if (player.getLookingDirection().equals(SOUTH)) {
 						lookingDirection = WEST;
 					} else if (player.getLookingDirection().equals(EAST)) {
 						lookingDirection = SOUTH;
 					} else if (player.getLookingDirection().equals(WEST)) {
 						lookingDirection = NORTH;
 					}
 
 					setChanged();
 					notifyObservers(LOOK + "," + lookingDirection);
 				}
 			});
 			btnTurnRight.setToolTipText("right");
 			add(btnTurnRight, "4, 26");
 
 			JSeparator separator_4 = new JSeparator();
 			add(separator_4, "2, 28, 3, 1");
 
 			JLabel lblStartingRoom_1 = new JLabel("Starting Room:");
 			add(lblStartingRoom_1, "2, 30, right, default");
 
 			startingRoom = new JTextField();
 			add(startingRoom, "4, 30, fill, default");
 			startingRoom.setColumns(10);
 
 			JButton btnSaveLevel = new JButton("Save Level");
 			btnSaveLevel.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					setChanged();
 					notifyObservers(SAVE + "," + startingRoom.getText() + ","
 							+ xmlName.getText());
 				}
 			});
 
 			JLabel lblLevelName = new JLabel("Level Name:");
 			add(lblLevelName, "2, 32, right, default");
 
 			xmlName = new JTextField();
 			add(xmlName, "4, 32, fill, default");
 			xmlName.setColumns(10);
 			add(btnSaveLevel, "2, 34");
 
 			JButton btnGeneratePlayableGamesave = new JButton("Play");
 			btnGeneratePlayableGamesave.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
					
					if (startingRoom.getText() == null || startingRoom.getText().equals("")) {
						JOptionPane.showMessageDialog(mainFrame, "Error: Please enter a valid name of an exisiting room");
						return;
					}
					
 					setChanged();
 					notifyObservers(PLAY + "," + startingRoom.getText());
 				}
 			});
 			add(btnGeneratePlayableGamesave, "4, 34");
 
 		}
 
 	}
 }
