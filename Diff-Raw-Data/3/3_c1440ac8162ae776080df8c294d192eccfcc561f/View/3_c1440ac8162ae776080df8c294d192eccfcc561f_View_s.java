 package View;
 import Builders.*;
 
 import java.awt.FileDialog;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.ObjectInputStream;
 import java.io.ObjectOutputStream;
 
 import javax.imageio.ImageIO;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JList;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 
 import zuul.Command;
 import zuul.CommandWords;
 import zuul.Game;
 import zuul.Humanoid;
 import zuul.Item;
 import zuul.Monster;
 import zuul.Player;
 
 /**
  * This class is intended to display the current state of the model in different
  * ways. For all views to successfully display the model and changes to it, it
  * must implement the methods listed in this interface.
  * 
  * @author Vinayak Bansal
  * @version 2012.10.26
  * @param <ItemBuilder>
  * @param <MapBuilder>
  * @param <RoomBuilder>
  */
 
 @SuppressWarnings("serial")
 public abstract class View extends JFrame implements ActionListener {
 	public static final String REDO = "Redo";
 	public static final String UNDO = "Undo";
 	public static final String HINT = "Hint";
 	public static final String FIGHT = "Fight";
 	public static final String TREASURE = "Treasure";
 	public static final String OBJECTIVE = "Objective";
 	public static final String QUIT = "Quit";
 	private static final String RESET = "Reset";
 	private static final String INSPECT = "Inspect";
 	private static final String DROP = "Drop";
 	public static final String EAT = "Eat";
 	public static final String GO_SOUTH = "Go South";
 	public static final String GO_NORTH = "Go North";
 	public static final String GO_EAST = "Go East";
 	public static final String GO_WEST = "Go West";
 	protected Player p;
 	protected boolean unlocked;
 	protected JMenuBar menuBar;
 	protected JList inventoryList;
 	protected DefaultListModel inventoryModel;
 	protected JMenuItem resetGame, objective, hint, quit, save, open;
 	protected JButton eat, drop, inspect;
 	protected JPanel inventoryPanel, mapPanel;
 	protected JLabel mapLabel;
 	protected JMenu addressMenu, helpMenu;
 	protected int currentMapRoom = 9;
 	protected Builder b;
 	File path = new File("Images");
 
 	public View(Builder b) {
 		
 		this.b = b;
 		
 		p = new Player(Humanoid.MAX_HEALTH, Game.initialize(b), "Player");
 		saveGame("default.txt");
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		setExtendedState(MAXIMIZED_BOTH);
 		eat = new JButton(EAT);
 		drop = new JButton(DROP);
 		inspect = new JButton(INSPECT);
 		drop.addActionListener(this);
 		eat.addActionListener(this);
 		inspect.addActionListener(this);
 		inventoryPanel = new JPanel();
 		mapPanel = new JPanel();
 		//mapLabel = new JLabel(new ImageIcon("Images/rooms_startroom.png"));
 		//smapPanel.add(mapLabel);
 
 		inventoryModel = new DefaultListModel();
 		inventoryList = new JList(inventoryModel);
 
 		JScrollPane pane = new JScrollPane(inventoryList);
 
 		inventoryList.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseEntered(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseExited(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mousePressed(MouseEvent arg0) {
 			}
 
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				Item selectedItem = (Item) inventoryList.getSelectedValue();
 				if (selectedItem == null) {
 					drop.setEnabled(false);
 					eat.setEnabled(false);
 					inspect.setEnabled(false);
 				} else {
 					drop.setEnabled(true);
 					inspect.setEnabled(true);
 					if (!selectedItem.isWeapon())
 						eat.setEnabled(true);
 					else
 						eat.setEnabled(false);
 				}
 			}
 
 		});
 
 		JPanel inventoryLeftPanel = new JPanel();
 		inventoryLeftPanel.setLayout(new GridLayout(1, 1));
 		JPanel inventoryRightPanel = new JPanel();
 		inventoryRightPanel.setLayout(new GridLayout(3, 1));
 		inventoryPanel.add(inventoryLeftPanel);
 		inventoryPanel.add(inventoryRightPanel);
 		inventoryLeftPanel.add(pane);
 		inventoryRightPanel.add(eat);
 		inventoryRightPanel.add(drop);
 		inventoryRightPanel.add(inspect);
 
 		addressMenu = new JMenu("File");
 		helpMenu = new JMenu("Help");
 		menuBar.add(addressMenu);
 		menuBar.add(helpMenu);
 
 		resetGame = new JMenuItem(RESET);
 		resetGame.addActionListener(this);
 		save = new JMenuItem("Save");
 		save.addActionListener(this);
 		open = new JMenuItem("Open");
 		open.addActionListener(this);
 		quit = new JMenuItem(QUIT);
 		quit.addActionListener(this);
 		objective = new JMenuItem(OBJECTIVE);
 		objective.addActionListener(this);
 		hint = new JMenuItem(HINT);
 		hint.addActionListener(this);
 		addressMenu.add(resetGame);
 		addressMenu.add(open);
 		addressMenu.add(save);
 		helpMenu.add(objective);
 		helpMenu.add(hint);
 
 	}
 
 	/**
 	 * This is the overarching method used to update everything in the class. It
 	 * will change buttons as required and enables/disables them. It updates all
 	 * of the panels as well.
 	 */
 	public void update() {
 		inventoryModel.removeAllElements();
 		for (Item i : p.getInventory())
 			inventoryModel.addElement(i);
 		drop.setEnabled(false);
 		eat.setEnabled(false);
 		inspect.setEnabled(false);
 		updateMapPanel();
 	}
 
 	/**
 	 * This method pops up a dialog that informs the player that they have been
 	 * defeated by a monster.
 	 */
 	protected void gameDone() {
 		JOptionPane.showMessageDialog(this, "You have been defeated!");
 		quit();
 	}
 
 	/**
 	 * If the monster dies, A message should be printed accordingly. This method
 	 * creates a popup that shows the items that the monster dropped.
 	 * 
 	 * @param m
 	 *            : The monster that has died.
 	 */
 	protected void monsterDead(Monster m) {
 		String s = ("You defeated " + m.getName() + "!\n");
 		if (!m.getInventory().isEmpty()) {
 			s += m.getName() + " dropped the following item(s):\n";
 			for (Item i : m.getInventory())
 				s += i.getName() + "\n";
 		}
 		m.dropItems();
 		JOptionPane.showMessageDialog(this, s);
 	}
 
 	/**
 	 * Quit method, used to exit the game.
 	 */
 	protected void quit() {
 		System.exit(0);
 	}
 
 	/**
 	 * This method gets an action from a button press and reacts accordingly.
 	 * 
 	 * @param e
 	 *            : The actionEvent when a button or menu item is clicked.
 	 */
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals(RESET)) {
 			load("default.txt");
 			currentMapRoom = 9;
 		} else if (e.getActionCommand().equals(OBJECTIVE)) {
 			JOptionPane.showMessageDialog(this, getObjective());
 		} else if (e.getActionCommand().equals(HINT)) {
 			getHint();
 		} else if (e.getActionCommand().equals(DROP)) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if (selectedItem != null) {
 				p.doCommand(new Command(CommandWords.DROP, selectedItem));
 				updateMapPanel();
 			}
 		} else if (e.getActionCommand().equals(EAT)) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if (selectedItem != null)
 				p.doCommand(new Command(CommandWords.EAT, selectedItem));
 		} else if (e.getActionCommand().equals(INSPECT)) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if (selectedItem != null) {
 				JOptionPane.showMessageDialog(this,
 						selectedItem.getDescription(), "Item",
 						getDefaultCloseOperation(), getImageIcon(selectedItem));
 			}
 		} else if (e.getActionCommand().equals(UNDO)) {
 			p.doCommand(Command.parse(UNDO));
 		} else if (e.getActionCommand().equals(REDO)) {
 			p.doCommand(Command.parse(REDO));
 		} else if (e.getActionCommand().equals(QUIT)) {
 			quit();
 		} else if (e.getActionCommand().equals("Save")) {
 			saveGame(getFileName());
 		} else if (e.getActionCommand().equals("Open")) {
 			load(getFileNameForLoad());
 		}
 		update();
 	}
 
 	/**
 	 * TODO
 	 */
 	private void load(String fileName) {
 		if (fileName == null) {
 			return;
 		}
 		ObjectInputStream in;
 		try {
 			in = new ObjectInputStream(new FileInputStream(fileName));
 			p = (Player) in.readObject();
 			unlocked = in.readBoolean();
 			in.close();
 			JOptionPane.showMessageDialog(this,
 					"Your game was successfully retrieved!", "Confirmation",
 					JOptionPane.INFORMATION_MESSAGE);
 		} catch (Exception e1) {
 			JOptionPane.showMessageDialog(this,
 					"There was a problem retrieving your game!", "Error",
 					JOptionPane.ERROR_MESSAGE);
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private String getFileNameForLoad() {
 		FileDialog dialog = new FileDialog(this, "Open", FileDialog.LOAD);
 		dialog.setVisible(true);
 		String fileName = dialog.getFile();
 		return fileName;
 	}
 
 	/**
 	 * TODO
 	 */
 	private void saveGame(String fileName) {
 
 		if (fileName == null){
 			return;
 		}
 		FileOutputStream fos;
 		ObjectOutputStream oos;
 		try {
 			fos = new FileOutputStream(fileName);
 			oos = new ObjectOutputStream(fos);
 			oos.writeObject(p);
 			oos.writeBoolean(unlocked);
 			oos.close();
 			JOptionPane.showMessageDialog(this,
 					"Your game was successfully saved!", "Confirmation",
 					JOptionPane.INFORMATION_MESSAGE);
 		} catch (Exception e1) {
 			JOptionPane.showMessageDialog(this,
 					"There was a problem saving your game!", "Error",
 					JOptionPane.ERROR_MESSAGE);
 			e1.printStackTrace();
 		}
 	}
 
 	/**
 	 * @return
 	 */
 	private String getFileName() {
 		FileDialog dialog = new FileDialog(this, "Save As", FileDialog.SAVE);
 		dialog.setVisible(true);
 		String fileName = dialog.getFile();
 		if (fileName == null) {
 			return null; // the user did not want to save
 		}
 		return fileName;
 	}
 
 	/**
 	 * This method updates the mapPanel with the images of the minimap. If there
 	 * is no map in the players inventory, then a picture message is shown
 	 * telling the player to find the map.
 	 */
 	protected void updateMapPanel() {
 		mapPanel.removeAll();
 		BufferedImage map = null;
 		b.getRoomBuilder().getMapBuilder().drawPlayer(currentMapRoom);
 		try {
 			map  = ImageIO.read(new File(path, "combined_with_player.png"));
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		if(p.getInventory().contains(new Item("Map", true))){
 			mapLabel = new JLabel(new ImageIcon(map));
 			validate();
 		} else {
 			mapLabel = new JLabel(new ImageIcon("Images/rooms_noMap.png"));
 		}
 			mapPanel.add(mapLabel);
 			mapPanel.validate();
 	}
 
 	/**
 	 * This method prints out the objective of the game.
 	 * 
 	 * @return : Returns a string informing the player of the game objective.
 	 */
 	protected String getObjective() {
 		String str = "";
 		str += "Welcome to the World of Zuul.\nCan you conquer the monsters and find the long lost treasure of Zuul?\n";
 		return str;
 	}
 
 	/**
 	 * This method pops up a dialog that gives the player a hint as to what to
 	 * do next.
 	 */
 	protected void getHint() {
 		if (!p.getInventory().contains(new Item("Map", true)))
 			JOptionPane.showMessageDialog(this,
 					"Find the map!\nTry the room east of the startroom!");
 		else if (!p.getInventory().contains(new Item("Key", true)))
 			JOptionPane.showMessageDialog(this,
 					"Find the key!\nPerhaps the boss in the southroom has it!");
 		else
 			JOptionPane.showMessageDialog(this,
 					"Locate the treasure, the game is yours!");
 	}
 
 	/**
 	 * This method pops up a dialog that congratulates the player on winning the
 	 * game.
 	 */
 	protected void win() {
 		JOptionPane
 				.showMessageDialog(
 						this,
 						"Congratulations!\nYou recovered the long lost treasure of Zuul and bested the monsters!\nYou win!");
 		quit();
 	}
 
 	/**
 	 * Used when the inspect button is clicked. This method returns the image
 	 * icon associated with the item when inspect is clicked.
 	 * 
 	 * @param i
 	 *            : The item that is selected.
 	 * @return : The corresponding image that represents the item.
 	 */
 	protected ImageIcon getImageIcon(Item i) {
 		ImageIcon icon = null;
 		;
 		if (i.equals(new Item("Sword", true)))
 			icon = new ImageIcon("Images/sword.png");
 		else if (i.equals(new Item("Bread", true)))
 			icon = new ImageIcon("Images/bread.gif");
 		else if (i.equals(new Item("Apple", true)))
 			icon = new ImageIcon("Images/apple.png");
 		else if (i.equals(new Item("Pear", true)))
 			icon = new ImageIcon("Images/pear.png");
 		else if (i.equals(new Item("Orange", true)))
 			icon = new ImageIcon("Images/orange.png");
 		else if (i.equals(new Item("Map", true)))
 			icon = new ImageIcon("Images/map.jpg");
 		else if (i.equals(new Item("Claws", true)))
 			icon = new ImageIcon("Images/Claws.png");
 		else if (i.equals(new Item("Flamethrower", true)))
 			icon = new ImageIcon("Images/flamethrower.jpg");
 		else if (i.equals(new Item("Key", true)))
 			icon = new ImageIcon("Images/key.png");
 		return icon;
 	}
 
 	/**
 	 * Reset method, used to start the game anew.
 	 */
 	protected void reset() {
 		p.reset();
 		unlocked = false;
 	}
 
 	/**
 	 * This method pops up a dialog that informs the player of the damage done
 	 * to and from the player.
 	 */
 	protected void fightPopUp() {
 		Monster m = p.getCurrentRoom().getMonster();
 		JOptionPane.showMessageDialog(
 				this,
 				"" + p.getName() + " attacked " + m.getName() + " and did "
 						+ p.getBestItem().getValue() + " Damage\n"
 						+ m.getName() + " attacked " + p.getName()
 						+ " and did "
 						+ (m.getBestItem().getValue() * m.getLevel())
 						+ " Damage\n");
 	}
 
 	/**
 	 * asks the user what kind of a view he wants
 	 * @param <ItemBuilder>
 	 * @param <MapBuilder>
 	 * @param <RoomBuilder>
 	 * 
 	 * @return: a 2D or a 3D view depending on what the user wanted.
 	 */
 	public static  View getInstance(Builder b) {
 		String[] s = new String[2];
 		s[0] = "2D";
 		s[1] = "3D";
 		int popup = JOptionPane.showOptionDialog(null, "Please select a view:",
 				"View Selection", JOptionPane.YES_NO_CANCEL_OPTION,
 				JOptionPane.INFORMATION_MESSAGE, null, s, null);
 		View view;
 		if (popup == 0) {
 			view = new TwoDView(b);
 		} else {
 			view = new ThreeDView(b);
 		}
 		return view;
 
 	}
 
 }
