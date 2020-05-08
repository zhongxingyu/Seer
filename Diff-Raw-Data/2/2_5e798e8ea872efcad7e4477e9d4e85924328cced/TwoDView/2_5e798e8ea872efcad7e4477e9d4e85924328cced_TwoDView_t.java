 /**
  * 
  * This is the view as well as the controller.
  * 
  * @author Joint programming between Ryan, Jarred and Vinayak
  * For more details on who made what change, please refer to the 
  * change sets associated with this file on GitHub
  */
 package View;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
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
 import javax.swing.JTextArea;
 
 import zuul.Command;
 import zuul.CommandWords;
 import zuul.Direction;
 import zuul.Item;
 import zuul.Monster;
 import zuul.Player;
 import zuul.Room;
 
 @SuppressWarnings("serial")
 public class TwoDView extends JFrame implements IView, ActionListener
 {
 	private JMenuItem resetGame, objective, hint, quit; //commands
 	private JMenuBar menuBar;
 	private JButton undo, redo, northRoom, southRoom, eastRoom, westRoom, pickup, fight, eat, drop, inspect;
 	private JLabel currentRoom, mapLabel;
 	private JTextArea consoleField;
 	private JPanel consolePanel, inventoryPanel, centralPanel, undoRedoPanel, mapPanel;
 	private Player p;
 	private JList inventoryList;
 	private DefaultListModel inventoryModel;
 	private boolean unlocked = false;
 
 	@SuppressWarnings("static-access")
 	public TwoDView (Player p) {
 		this.p = p;
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		menuBar = new JMenuBar( );
 	    setJMenuBar( menuBar );
 	    setLayout(new GridLayout(3,3));
 	    this.setExtendedState(this.MAXIMIZED_BOTH);
 
 	    undo = new JButton("UNDO");
 	    redo = new JButton("REDO");
 	    northRoom = new JButton("Go North");
 	    eastRoom = new JButton("Go East");
 	    westRoom = new JButton("Go West");
 	    southRoom = new JButton("Go South");
 	    pickup = new JButton("Pickup");
 	    fight = new JButton ("Fight");
 	    eat = new JButton ("Eat");
 	    drop = new JButton ("Drop");
 	    inspect = new JButton ("Inspect");
 
 
 	    undo.addActionListener(this);
 	    redo.addActionListener(this);
 	    northRoom.addActionListener(this);
 	    eastRoom.addActionListener(this);
 	    westRoom.addActionListener(this);
 	    southRoom.addActionListener(this);
 	    pickup.addActionListener(this);
 	    fight.addActionListener(this);
 	    drop.addActionListener(this);
 	    eat.addActionListener(this);
 	    inspect.addActionListener(this);
 
 	    consolePanel = new JPanel();
 	    consolePanel.setLayout(new GridLayout(3, 2));
 	    inventoryPanel = new JPanel();
 	    inventoryPanel.setLayout(new GridLayout(1, 2));
 	    centralPanel = new JPanel();
 	    undoRedoPanel = new JPanel();
 	    mapPanel = new JPanel();
 	    mapLabel = new JLabel(new ImageIcon("rooms_startroom.png"));
 	    mapPanel.add(mapLabel);
 
 	    currentRoom = new JLabel("Current Room Actions:");
 	    centralPanel.add(currentRoom);
 	    centralPanel.add(pickup);
 	    centralPanel.add(fight);
 	    centralPanel.setBackground(new Color(255, 249, 206));
 	    inventoryModel = new DefaultListModel();
 	    inventoryList = new JList(inventoryModel);
 
 	    JScrollPane pane = new JScrollPane(inventoryList);
 	    
 	    inventoryList.addMouseListener(new MouseListener() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {}
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 			@Override
 			public void mouseExited(MouseEvent arg0) {}
 			@Override
 			public void mousePressed(MouseEvent arg0) {}
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				Item selectedItem = (Item) inventoryList.getSelectedValue();
 				if(selectedItem == null) {
 					drop.setEnabled(false);
 					eat.setEnabled(false);
 					inspect.setEnabled(false);
 				}
 				else {
 					drop.setEnabled(true);
 					inspect.setEnabled(true);
 					if(!selectedItem.isWeapon()) {
 						eat.setEnabled(true);
 					}
 					else eat.setEnabled(false);
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
 
 	    undoRedoPanel.add(undo);
 	    undoRedoPanel.add(redo);
 	    consoleField = new JTextArea();
 	    consoleField.setEditable(false);
 		consolePanel.add(consoleField);
 
 
 	    add(undoRedoPanel);
 	    add(northRoom);
 	    add(mapPanel);
 	    add(westRoom);
 	    add(centralPanel);
 	    add(eastRoom);
 	    add(consolePanel);
 	    add(southRoom);
 	    add(inventoryPanel);
 
 	    JMenu addressMenu = new JMenu( "File" );
 	    menuBar.add( addressMenu );
 
 	    resetGame = new JMenuItem ( "Reset" );
 	    addressMenu.add( resetGame );
 	    resetGame.addActionListener(this);
 
 	    quit = new JMenuItem ( "Quit" );
 	    addressMenu.add( quit );
 	    quit.addActionListener(this);
 
 	    JMenu helpMenu = new JMenu( "Help" );
 	    menuBar.add(helpMenu);
 
 	    objective = new JMenuItem ( "Objective" );
 	    helpMenu.add( objective );
 	    objective.addActionListener(this);
 
 	    hint = new JMenuItem("Hint");
 	    helpMenu.add(hint);
 	    hint.addActionListener(this);
 
 	    update();
 	}
 
 	/**
 	 * This is the overarching method used to update everything in the class.
 	 * It will change buttons as required and enables/disables them.
 	 * It updates all of the panels as well.
 	 */
 	@Override
 	public void update() {
 		Room currentRoom = p.getCurrentRoom();
 		if (currentRoom.getExit(Direction.NORTH) == null) {
 			northRoom.setEnabled(false);
 		} else {
 			northRoom.setEnabled(true);
 		}
 		if (currentRoom.getExit(Direction.SOUTH) == null) {
 			southRoom.setEnabled(false);
 		} else {
 			southRoom.setEnabled(true);
 		}
 		if (currentRoom.getExit(Direction.EAST)== null) {
 			eastRoom.setEnabled(false);
 		} else {
 			eastRoom.setEnabled(true);
 		}
 		if (currentRoom.getExit(Direction.WEST) == null) {
 			westRoom.setEnabled(false);
 		} else {
 			westRoom.setEnabled(true);
 		}
 
 		if (p.canUndo()) {
 			undo.setEnabled(true);
 		} else {
 			undo.setEnabled(false);
 		}
 		if (p.canRedo()) {
 			redo.setEnabled(true);
 		} else {
 			redo.setEnabled(false);
 		}
 
 
 		if(!p.getCurrentRoom().getItems().isEmpty()){
 			pickup.setEnabled(true);
 		} else {
 			pickup.setEnabled(false);
 		}
 
 		inventoryModel.removeAllElements();
 		for (Item i :p.getInventory())
 			inventoryModel.addElement(i);
 
 		if(p.getCurrentRoom().hasMonsters()) {
 			fight.setEnabled(true);
 		} else {
 			fight.setEnabled(false);
 		}
 		drop.setEnabled(false);
 		eat.setEnabled(false);
 		inspect.setEnabled(false);
 		consoleField.setText(updateConsole());
 		updateMapPanel();
 
 	}
 
 	/**
 	 * This method updates the mapPanel with the images of the minimap.
 	 * If there is no map in the players inventory, 
 	 * then a picture message is shown telling the player to find the map.
 	 */
     private void updateMapPanel(){
 		String s = p.getCurrentRoom().getRoomName();
 		   mapPanel.remove(mapLabel);
 	   if(p.getInventory().contains(new Item("Map", true))){
 		if(s.equals("NorthRoom1")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_northroom1.png"));
 		} else if (s.equals("EastRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_eastRoom.png"));
 		} else if (s.equals("StartRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_startRoom.png"));
 		} else if (s.equals("EastRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_eastRoom.png"));
 		} else if (s.equals("WestRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_westRoom.png"));
 		} else if (s.equals("SouthRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_southRoom.png"));
 		} else if (s.equals("NorthRoom2")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_northroom2.png"));
 		} else if (s.equals("NorthWestRoom")){
 		    mapLabel = new JLabel(new ImageIcon("rooms_northWestRoom.png"));
 		}
 
 	   } else {
 		   mapPanel.remove(mapLabel);
 		   mapLabel = new JLabel(new ImageIcon("rooms_noMap.png"));
 	   }
 
 	   mapPanel.add(mapLabel);
 	}
 
 	/**
 	 * This method is not used in this milestone.
 	 */
 	@Override
 	public void displayHelp() {/* String is needed, so getObjective and 
 	and getHint methods is used instead.*/}
 
 
 	/**
 	 * This method prints out the objective of the game.
 	 * @return : Returns a string informing the player of the game objective.
 	 */
 	private String getObjective(){
 		String str = "";
 		str+="Welcome to the World of Zuul.\nCan you conquer the monsters and find the long lost treasure of Zuul?\n";
 		return str;
 	}
 
 
 	/*
 	 * This method is not used in this milestone.
 	 */
 //	public String getCommands(){
 //		String str="Your command words are:\n";
 //		for (CommandWords commandWord : CommandWords.values()) {
 //			str+= commandWord + " ";
 //		}
 //		return str;
 //	}
 
 	/**
 	 * This method pops up a dialog that informs the player of the damage done to and from the player.
 	 */
 	private void fightPopUp(){
 		Monster m = p.getCurrentRoom().getMonster();
 		JOptionPane.showMessageDialog(this, "" + p.getName() + " attacked " + m.getName() + " and did " + p.getBestItem().getValue() + " Damage\n"
 				 + m.getName() + " attacked " + p.getName() + " and did " + m.getBestItem().getValue()*m.getLevel()  + " Damage\n");
 	}
 
 
 	/**
 	 * This method pops up a dialog that gives the player a hint as to what to do next.
 	 */
 	private void getHint(){
 		if(!p.getInventory().contains(new Item("Map", true))){
			JOptionPane.showMessageDialog(this, "Find the map!\nTry the room east of the startroom!");
 		} else if(!p.getInventory().contains(new Item("Key", true))){
 			JOptionPane.showMessageDialog(this, "Find the key!\nPerhaps the boss in the southroom has it!");
 		} else {
 			JOptionPane.showMessageDialog(this, "Locate the treasure, the game is yours!");
 		}
 	}
 
 	/**
 	 * This method pops up a dialog that informs the player that they have been defeated by a monster.
 	 */
 	@Override
 	public void gameDone() {
 		JOptionPane.showMessageDialog(this, "You have been defeated!");
 		quit();
 	}
 
 	/**
 	 * This method pops up a dialog that congratulates the player on winning the game.
 	 */
 	private void win(){
 		JOptionPane.showMessageDialog(this, "Congratulations!\nYou recovered the long lost treasure of Zuul and bested all the monsters!\nYou win!");
 		quit();
 	}
 
 
 	/**
 	 * If the monster dies, A message should be printed accordingly.
 	 * This method creates a popup that shows the items that the monster dropped.
 	 * @param m : The monster that has died.
 	 */
 	@Override
 	public void monsterDead(Monster m) {
 		String s = ("You defeated " + m.getName() + "!\n");
 		if(!m.getInventory().isEmpty()){
 			s+= m.getName() + " dropped the following item(s):\n" ;
 			for(Item i: m.getInventory()){
 				s+= i.getName() + "\n";
 			}
 		}
 		JOptionPane.showMessageDialog(this, s);
 	}
 
 
 	/**
 	 * These methods are implemented from the IView interface, but are not used here.
 	 */
 	@Override	public void monsterMissing() {/* Checked by disabling the button, this will never be called*/}
 	@Override	public void garbageCommand() {/* checked by using buttons, no way to enter garbage*/}
 	@Override	public void invalidRoom() {	/* Checked by disabling buttons*/}
 	@Override	public void eatingWeapon(Item i) {/*// Checked by disabling buttons*/}
 	@Override	public void noItem(Item i) {/* Checked by disabling button*/}
 	@Override	public void itemInvalid(Item i) {/* Checked by disabling button*/}
 	@Override	public void itemError(Item i) {/* Checked by disabling button*/}
 	@Override	public void inCompleteCommand() {/* Impossible with GUI*/}
 	@Override	public void undoRedoUnavailable(CommandWords commandWord) {/* Disabling buttons when appropriate*/}
 
 	/**
 	 * This method is used to update the console showing the player and monster health.
 	 * @return : The string that should be placed onto the console.
 	 */
 	private String updateConsole(){
 		String s = "";
 		s += ("Player Health: " + p.getHealth() + "\n");
 		if(p.getCurrentRoom().getMonster()!=null){
 			s+= ("Monster Health: " + p.getCurrentRoom().getMonster().getHealth());
 		}
 		return s;
 	}
 	
 	/**
 	 * Used when the inspect button is clicked.
 	 * This method returns the image icon associated with the item when inspect is clicked.
 	 * @param i : The item that is selected.
 	 * @return : The corresponding image that represents the item.
 	 */
     private ImageIcon getImageIcon(Item i){
 		ImageIcon icon = null;;
 		if(i.equals(new Item("Sword", true))){
 			icon  = new ImageIcon("sword.png");
 		} else if(i.equals(new Item("Bread", true))){
 			icon  = new ImageIcon("bread.gif");
 		} else if(i.equals(new Item("Apple", true))){
 			icon  = new ImageIcon("apple.png");
 		} else if(i.equals(new Item("Pear", true))){
 			icon  = new ImageIcon("pear.png");
 		} else if(i.equals(new Item("Orange", true))){
 			icon  = new ImageIcon("orange.png");
 		} else if(i.equals(new Item("Map", true))){
 			icon  = new ImageIcon("map.jpg");
 		} else if(i.equals(new Item("Hatchet", true))){
 			icon  = new ImageIcon("hatchet.png");
 		} else if(i.equals(new Item("Flamethrower", true))){
 			icon  = new ImageIcon("flamethrower.jpg");
 		} else if(i.equals(new Item("Key", true))){
 			icon  = new ImageIcon("key.png");
 		} //can't inspect treasure since game is already won if it is picked up
 			return icon;
 	}
 
 	/**
 	 * Quit method, used to exit the game.
 	 */
 	@Override
 	public void quit() {
 		System.exit(0);
 	}
 
 	/**
 	 * Reset method, used to start the game anew.
 	 */
 	private void reset(){
 		while(p.canUndo()){
 			p.doCommand(Command.parse("Undo"));
 		}
 		p.reset();
 		unlocked = false;
 		resetInitialize();
 	}
 
 	/**
 	 * Initialize the game with the original monsters and items
 	 * in the case that monsters were defeated, or items were eaten.
 	 */
 	private void resetInitialize() {
 		Room west = p.getCurrentRoom().getExit(Direction.WEST);
 		Item r1 = new Item("Apple", 10, 0, false);
 		if (!west.getItems().contains(r1)) {
 			west.addItem(r1);
 		}
 		Item r2 = new Item("Orange", 15, 0, false);
 		if (!west.getItems().contains(r2)) {
 			west.addItem(r2);
 		}
 		Item r3 = new Item("Pear", 20, 0, false);
 		if (!west.getItems().contains(r3)) {
 			west.addItem(r3);
 		}
 		Room north1 = p.getCurrentRoom().getExit(Direction.NORTH);
 		Item r4 = new Item("Bread", 30, 0, false);
 		if (!north1.getItems().contains(r4)) {
 			north1.addItem(r4);
 		}
 		Room east = p.getCurrentRoom().getExit(Direction.EAST);
 		Monster monster1 = new Monster(Monster.MAX_HEALTH,
 				Monster.DEFAULT_LEVEL, "Monster1", east);
 		if (!east.getItems().contains(monster1)) {
 			west.addMonster(monster1);
 			east.addMonster(monster1);
 			monster1.addItem(new Item("Map", 0, 0, true));
 			monster1.addItem(new Item("Hatchet", 10, 0, true));
 		}
 		Room south = p.getCurrentRoom().getExit(Direction.SOUTH);
 		Monster boss = new Monster(100, 2, "Boss", south);
 		if (!east.getItems().contains(boss)) {
 			west.addMonster(boss);
 			south.addMonster(boss);
 			boss.addItem(new Item("Flamethrower", 30, 0, true));
 			boss.addItem(new Item("Key", 0, 0, true));
 		}
 	}
 
 
 	/**
 	 * This method gets an action from a button press and reacts accordingly.
 	 * @param e : The actionEvent when a button or menu item is clicked.
 	 */
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Reset")) {
 			reset();
 		}
 		else if (e.getActionCommand().equals("Objective")) {
 			JOptionPane.showMessageDialog(this, getObjective());
 		}
 		else if(e.getActionCommand().equals("Hint")){
 			getHint();
 		}
 		else if (e.getActionCommand().equals("Go North")) {
 			p.doCommand(Command.parse("Go North"));
 		}
 		else if (e.getActionCommand().equals("Go East")) {
 			p.doCommand(Command.parse("Go East"));
 		}
 		else if (e.getActionCommand().equals("Go West")) {
 			if(p.getCurrentRoom().getExit(Direction.WEST).getLocked()!=true || unlocked==true){
 				p.doCommand(Command.parse("Go West"));
 			} else {
 				if(!p.getInventory().contains(new Item("Key", true))){
 					JOptionPane.showMessageDialog(this, "The Door is locked!\nYou are sure the treasure is just beyond.\nIf only you had a Key..");
 				} else {
 					JOptionPane.showMessageDialog(this, "You have opened the door!\nYou see the treasure in front of you!");
 					unlocked = true;
 					p.doCommand(Command.parse("Go West"));
 				}
 			}
 		}
 		else if (e.getActionCommand().equals("Go South")) {
 			p.doCommand(Command.parse("Go South"));
 		}
 		else if (e.getActionCommand().equals("Pickup")) {
 			pickup.setEnabled(true);
 				int popup = JOptionPane.showOptionDialog(this, "You are in the current room", "Current Room", JOptionPane.YES_NO_CANCEL_OPTION,
 					JOptionPane.INFORMATION_MESSAGE, null, p.getCurrentRoom().getItems().toArray(), null);
 				if (popup != JOptionPane.CLOSED_OPTION) {
 					p.doCommand(new Command(CommandWords.PICKUP, p.getCurrentRoom().getItems().get(popup)));
 				}
 				if(p.getInventory().contains(new Item("Treasure", true))){
 					win();
 				}
 		}
 		else if (e.getActionCommand().equals("Drop")) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if(selectedItem != null) {
 				p.doCommand(new Command(CommandWords.DROP, selectedItem));
 				updateMapPanel();
 			}
 		}
 		else if (e.getActionCommand().equals("Fight")) {
 			Monster m = p.getCurrentRoom().getMonster();
 			p.doCommand(Command.parse("Fight"));
 			if(p.getHealth()<=0){
 				this.gameDone();
 			} else {
 				if(p.getCurrentRoom().hasMonsters()){
 					m.setHealth(p.getCurrentRoom().getMonster().getHealth());
 				} else {
 					m.setHealth(0);
 				}
 				if(m.getHealth()<=0){
 					monsterDead(m);
 				} else {
 					fightPopUp();
 				}
 			}
 		}
 		else if (e.getActionCommand().equals("Eat")) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if(selectedItem != null) {
 				p.doCommand(new Command(CommandWords.EAT, selectedItem));
 			}
 		}
 		else if (e.getActionCommand().equals("Inspect")) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if(selectedItem != null) {
 		   		JOptionPane.showMessageDialog(this, selectedItem.getDescription(), "Item", getDefaultCloseOperation(), getImageIcon(selectedItem));
 			}
 		}
 		else if (e.getActionCommand().equals("UNDO")) {
 			p.doCommand(Command.parse("UNDO"));
 		}
 		else if (e.getActionCommand().equals("REDO")) {
 			p.doCommand(Command.parse("REDO"));
 		}
 		else if (e.getActionCommand().equals("Quit")) {
 			quit();
 		}
 		update();
 	}
 }
