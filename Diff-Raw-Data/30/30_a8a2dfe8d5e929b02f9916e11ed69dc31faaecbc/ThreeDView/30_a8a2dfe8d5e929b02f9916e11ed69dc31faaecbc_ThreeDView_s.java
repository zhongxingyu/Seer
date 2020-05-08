 /**
  * 
  * This is the view as well as the controller.
  * 
  * @author Joint programming between Ryan, Jarred and Vinayak
  * For more details on who made what change, please refer to the 
  * change sets associated with this file on GitHub
  */
 package View;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.GridLayout;
 import java.awt.Polygon;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JLayeredPane;
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
 public class ThreeDView extends JFrame implements IView, ActionListener
 {
	private JMenuItem resetGame, objective, hint, quit; //commands
 	private JMenuBar menuBar;
 	private JButton /*undo, redo, northRoom, southRoom, eastRoom, westRoom,*/ pickup,/* fight,*/ eat, drop, inspect;
 	private JLabel currentRoom, mapLabel;
 	private JTextArea healthField;
 	private JPanel /*consolePanel,*/ inventoryPanel,/* centralPanel, undoRedoPanel, */mapPanel;
 	private Player p;
 	private JList inventoryList;
 	private DefaultListModel inventoryModel;
 	private boolean unlocked = false;
 
 	private Polygon doorWest, doorEast, doorNorth, chest, monster, doorSouth, treasure;
 	private JLayeredPane consolePanel;
 	private JLabel backgroundLabel;
 	
     private JPanel backgroundPanel = new JPanel();
     private JPanel westPanel = new JPanel();
     private JPanel eastPanel = new JPanel();
     private JPanel northPanel = new JPanel();
     private JPanel southPanel = new JPanel();
 	private JPanel monsterPanel = new JPanel();
 	private JPanel chestPanel = new JPanel();
 	private JPanel treasurePanel = new JPanel();
 	private JPanel bossPanel = new JPanel();
 	private JPanel bossPanel2 = new JPanel();
 	private JPanel scene = new JPanel();
 	private JPanel fixPanel = new JPanel();
 	private JPanel fixPanel2 = new JPanel();
 	private JPanel fixPanel3 = new JPanel();
 
 	
 	@SuppressWarnings("static-access")
 	public ThreeDView (Player p) {
 		this.p = p;
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		menuBar = new JMenuBar( );
 	    setJMenuBar( menuBar );
 	    this.setExtendedState(this.MAXIMIZED_BOTH);
 	    
 	    this.setLayout(new BorderLayout());
 	    scene.setLayout(new BorderLayout());
 	    consolePanel = new JLayeredPane();
 	    scene.add(consolePanel, BorderLayout.CENTER);
 	    consolePanel.setBounds(0,0,600,400);
 	    scene.setBounds(0,0,600,400);
 	    this.add(scene, BorderLayout.CENTER);
 	    
 	    backgroundLabel = new JLabel(new ImageIcon("background_plain.png"));
 	    backgroundPanel.add(backgroundLabel);
 	    backgroundPanel.setBounds(0, 0, 850, 900);
 	    backgroundPanel.setOpaque(true);
 
 	    consolePanel.add(backgroundPanel, new Integer(0), 0);
 
 	
 //	    undo = new JButton("UNDO");
 //	    redo = new JButton("REDO");
 //	    northRoom = new JButton("Go North");
 //	    eastRoom = new JButton("Go East");
 //	    westRoom = new JButton("Go West");
 //	    southRoom = new JButton("Go South");
 	    pickup = new JButton("Pickup");
 //	    fight = new JButton ("Fight");
 	    eat = new JButton ("Eat");
 	    drop = new JButton ("Drop");
 	    inspect = new JButton ("Inspect");
 
 
 //	    undo.addActionListener(this);
 //	    redo.addActionListener(this);
 //	    northRoom.addActionListener(this);
 //	    eastRoom.addActionListener(this);
 //	    westRoom.addActionListener(this);
 //	    southRoom.addActionListener(this);
 //	    pickup.addActionListener(this);
 //	    fight.addActionListener(this);
 	    drop.addActionListener(this);
 	    eat.addActionListener(this);
 	    inspect.addActionListener(this);
 
 	    
 	    setupView();
 	    doorWest = new Polygon();
 	    doorEast = new Polygon();
 	    doorNorth = new Polygon();
 	    doorSouth = new Polygon();
 	    chest = new Polygon();
 	    monster = new Polygon();
 	    treasure = new Polygon();
 	    //still some strange instances where mouse is not found inside polygon
 	    
 	    doorWest.addPoint(103, 247);
 	    doorWest.addPoint(103, 458);
 	    doorWest.addPoint(174, 166);
 	    doorWest.addPoint(174, 374);
 	    
 	    doorEast.addPoint(632, 166);
 	    doorEast.addPoint(632, 374);
 	    doorEast.addPoint(705, 247);
 	    doorEast.addPoint(705, 459);
 	    
 	    doorNorth.addPoint(366, 152);
 	    doorNorth.addPoint(366, 314);
 	    doorNorth.addPoint(458, 152);
 	    doorNorth.addPoint(458, 314);
 	    
 	    doorSouth.addPoint(363, 495);
 	    doorSouth.addPoint(450, 495);
 	    doorSouth.addPoint(450, 573);
 	    doorSouth.addPoint(363, 573);
 	    
 	    chest.addPoint(197, 280);
 	    chest.addPoint(351, 280);
 	    chest.addPoint(351, 430);
 	    chest.addPoint(197, 430);
 	    
 	    monster.addPoint(472, 170);
 	    monster.addPoint(609, 170);
 	    monster.addPoint(609, 487);
 	    monster.addPoint(472, 487);
 	    
 	    treasure.addPoint(0, 0);
 	    treasure.addPoint(600, 0);
 	    treasure.addPoint(0, 400);
 	    treasure.addPoint(600, 400);
 	    
 	    backgroundLabel.addMouseListener(new MouseListener() {
 			
 			@Override
 			public void mouseReleased(MouseEvent arg0) {
 				//System.out.println("" + arg0.getX() + " " + arg0.getY());
 				handleCoordinates(arg0.getX(), arg0.getY());
 			}
 			@Override
 			public void mousePressed(MouseEvent arg0) {}
 			@Override 
 			public void mouseExited(MouseEvent arg0) {}
 			@Override
 			public void mouseEntered(MouseEvent arg0) {}
 			@Override
 			public void mouseClicked(MouseEvent arg0) {}
 		});
 	    inventoryPanel = new JPanel();
 	    inventoryPanel.setLayout(new GridLayout(2, 1));
 	    
 //	    centralPanel = new JPanel();
 //	    undoRedoPanel = new JPanel();
 	    mapPanel = new JPanel();
 	    mapLabel = new JLabel(new ImageIcon("rooms_startroom.png"));
 	    mapPanel.add(mapLabel);
 
 	    currentRoom = new JLabel("Current Room Actions:");
 //	    centralPanel.add(currentRoom);
 //	    centralPanel.add(pickup);
 //	    centralPanel.add(fight);
 //	    centralPanel.setBackground(new Color(255, 249, 206));
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
 
 	    JPanel interfacePanel = new JPanel();
 	    interfacePanel.setLayout(new GridLayout(3, 1));
 
 	    healthField = new JTextArea("Player Health: " + p.getHealth());
 	    healthField.setEditable(false);
 	    
 	    interfacePanel.add(mapPanel);
 	    interfacePanel.add(healthField);
 	    interfacePanel.add(inventoryPanel);
 //	    undoRedoPanel.add(undo);
 //	    undoRedoPanel.add(redo);
 //	    consoleField = new JTextArea();
 //	    consoleField.setEditable(false);
 //		consolePanel.add(consoleField);
 
 
 //	    add(undoRedoPanel);
 //	    add(northRoom);
 //	    add(mapPanel);
 //	    add(westRoom);
 //	    add(centralPanel);
 //	    add(eastRoom);
 //	    add(consolePanel);
 //	    add(southRoom);
 //	    add(inventoryPanel);
 	    this.add(interfacePanel, BorderLayout.EAST);//, c);
 
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
 //		if (currentRoom.getExit(Direction.NORTH) == null) {
 //			//northRoom.setEnabled(false);
 //		} else {
 //			//northRoom.setEnabled(true);
 //		}
 //		if (currentRoom.getExit(Direction.SOUTH) == null) {
 //			//southRoom.setEnabled(false);
 //		} else {
 //			//southRoom.setEnabled(true);
 //		}
 //		if (currentRoom.getExit(Direction.EAST)== null) {
 //			//eastRoom.setEnabled(false);
 //		} else {
 //			//eastRoom.setEnabled(true);
 //		}
 //		if (currentRoom.getExit(Direction.WEST) == null) {
 //			//westRoom.setEnabled(false);
 //		} else {
 //			//westRoom.setEnabled(true);
 //		}
 //
 //		if (p.canUndo()) {
 //			//undo.setEnabled(true);
 //		} else {
 //			//undo.setEnabled(false);
 //		}
 //		if (p.canRedo()) {
 //			//redo.setEnabled(true);
 //		} else {
 //			//redo.setEnabled(false);
 //		}
 
 
 		if(!p.getCurrentRoom().getItems().isEmpty()){
 			pickup.setEnabled(true);
 		} else {
 			pickup.setEnabled(false);
 		}
 
 		inventoryModel.removeAllElements();
 		for (Item i :p.getInventory())
 			inventoryModel.addElement(i);
 
 //		if(p.getCurrentRoom().hasMonsters()) {
 //			//fight.setEnabled(true);
 //		} else {
 //			//fight.setEnabled(false);
 //		}
 		drop.setEnabled(false);
 		eat.setEnabled(false);
 		inspect.setEnabled(false);
 	//	consoleField.setText(updateConsole());
 		updateMapPanel();
 		mapPanel.validate();
 		updateHealthField();
 	}
 
 	/**
 	 * This method updates the mapPanel with the images of the minimap.
 	 * If there is no map in the players inventory, 
 	 * then a picture message is shown telling the player to find the map.
 	 */
     private void updateMapPanel(){
 		String s = p.getCurrentRoom().getRoomName();
 		   mapPanel.removeAll();
 
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
 		JOptionPane.showMessageDialog(this, "Congratulations!\nYou recovered the long lost treasure of Zuul and bested the monsters!\nYou win!");
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
 	private void updateHealthField(){
 		String s = "Player Health: " + p.getHealth();
 		if (p.getCurrentRoom().hasMonsters()) {
 			s += "\nMonster Health: "
 					+ p.getCurrentRoom().getMonster().getHealth();
 		}
 		healthField.setText(s);
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
 		setupView();
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
 		east.removeItem(new Item("Claws", 10, 0, true));
 		east.removeItem(new Item("Map", 0, 0, true));
 		if (!east.hasMonsters()) {
 			east.addMonster(monster1);
 			monster1.addItem(new Item("Map", 0, 0, true));
 			monster1.addItem(new Item("Claws", 10, 0, true));
 		}
 		Room south = p.getCurrentRoom().getExit(Direction.SOUTH);
 		Monster boss = new Monster(100, 2, "Boss", south);
 		south.removeItem(new Item("Key", 0, 0, true));
 		south.removeItem(new Item("Flamethrower", 30, 0, true));
 		if (!south.hasMonsters()) {
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
 		else if (e.getActionCommand().equals("Drop")) {
 			Item selectedItem = ((Item) inventoryList.getSelectedValue());
 			if(selectedItem != null) {
 				p.doCommand(new Command(CommandWords.DROP, selectedItem));
 				updateMapPanel();
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
 	
 	private void handleCoordinates(int x, int y){
 		
 		if(p.getCurrentRoom().hasItem(new Item("Treasure", 100, 0, true))){
 			if(treasure.contains(x, y)){
 				win();
 			} 
 		}
 		
 		if(doorEast.contains(x, y)){
 			p.doCommand(Command.parse("Go East"));
 		} else if(doorWest.contains(x, y)){
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
 		} else if(doorNorth.contains(x, y)){
 			p.doCommand(Command.parse("Go North"));
 		} else if(doorSouth.contains(x, y)){
 			p.doCommand(Command.parse("Go South"));
 		} else if(chest.contains(x, y)){
 			pickup.setEnabled(true);
 			int popup;
 			if(!p.getCurrentRoom().getItems().isEmpty()){
 				popup = JOptionPane.showOptionDialog(this, "This Room has the following items:", "Current Room", JOptionPane.YES_NO_CANCEL_OPTION,
 						JOptionPane.INFORMATION_MESSAGE, null, p.getCurrentRoom().getItems().toArray(), null);
 					
 			} else {
 				popup = JOptionPane.showOptionDialog(this, "This room does not have any items in it!", "Current Room", JOptionPane.YES_NO_CANCEL_OPTION,
 						JOptionPane.INFORMATION_MESSAGE, null, p.getCurrentRoom().getItems().toArray(), null);
 					
 			}
 			if (popup != JOptionPane.CLOSED_OPTION) {
 				p.doCommand(new Command(CommandWords.PICKUP, p.getCurrentRoom().getItems().get(popup)));
 			}
 			if(p.getInventory().contains(new Item("Treasure", true))){
 				win();
 			}
 		} else if(monster.contains(x, y)){
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
 		update();
 		setupView();
 	}
 	
 	private void setupView(){
 		consolePanel.removeAll();
 		consolePanel.add(backgroundPanel, new Integer(0), 0);
 		
 		JLabel fixLabel = new JLabel(new ImageIcon("fix.png"));
 		
 		JLabel chestLabel = new JLabel(new ImageIcon("chest_in_room.png"));
 		if(!p.getCurrentRoom().hasItem(new Item("Treasure", 100, 0, true))){
 			chestPanel.add(chestLabel);
 			chestPanel.setBounds(210,278, 165, 160);
 			chestPanel.setBackground(new Color(185, 122, 87));
 			consolePanel.add(chestPanel, new Integer(1), 0);
 			fixPanel.add(fixLabel);
 			fixPanel.setBackground(Color.black);
 			fixPanel.setBounds(249, 278, 5, 5);
 			consolePanel.add(fixPanel, new Integer(1), 0);
 		} else {
 			JLabel treasureLabel = new JLabel(new ImageIcon("treasure_in_room.png"));
 			treasurePanel.add(treasureLabel);
 			treasurePanel.setBounds(75, 0, 700, 500);
 			consolePanel.add(treasurePanel, new Integer(1), 0);
 		}
 		if(p.getCurrentRoom().hasMonsters()){
 			if(p.getCurrentRoom().getMonster().getName().equals("Boss")){
 				JLabel bossLabel = new JLabel(new ImageIcon("boss1_in_room.png"));
 				JLabel bossLabel2 = new JLabel(new ImageIcon("boss2_in_room.png"));
 				bossPanel.add(bossLabel);
 				bossPanel2.add(bossLabel2);
 				bossPanel.setBackground(new Color(185, 122, 87));
 				bossPanel2.setBackground(new Color(69, 43, 29));
 				bossPanel.setBounds(487,143, 180, 300);
 				bossPanel2.setBounds(380,330, 280, 180);
 			    consolePanel.add(bossPanel2, new Integer(1), 0);
 			    consolePanel.add(bossPanel, new Integer(1), 0);
 				fixPanel2.add(fixLabel);
 				fixPanel2.setBackground(Color.black);
 				fixPanel2.setBounds(596, 143, 5, 5);
 				consolePanel.add(fixPanel2, new Integer(1), 0);
 			} else {
 				JLabel monsterLabel = new JLabel(new ImageIcon("monster_in_room.png"));
 				monsterPanel.add(monsterLabel);
 				monsterPanel.setBackground(new Color(185, 122, 87));
 				monsterPanel.setBounds(488,210, 180, 300);
 			    consolePanel.add(monsterPanel, new Integer(1), 0);   
 				fixPanel3.add(fixLabel);
 				fixPanel3.setBackground(Color.black);
 				fixPanel3.setBounds(596, 210, 5, 5);
 				consolePanel.add(fixPanel3, new Integer(1), 0);
 			}
 		} 
 		if(p.getCurrentRoom().getExit(Direction.NORTH)!=null){
 			JLabel northLabel = new JLabel(new ImageIcon("north_door.png"));
 		    northPanel.add(northLabel);
 		    northPanel.setBackground(new Color(185, 122, 87));
 		    northPanel.setBounds(385,146, 100, 180);
 		    consolePanel.add(northPanel, new Integer(1), 0);
 		}
 		if(p.getCurrentRoom().getExit(Direction.EAST)!=null){
 			JLabel eastLabel = new JLabel(new ImageIcon("east_door.png"));
 		    eastPanel.add(eastLabel);
 		    eastPanel.setBackground(new Color(185, 122, 87));
 		    eastPanel.setBounds(650,158, 80, 310);
 		    consolePanel.add(eastPanel, new Integer(1), 1);
 		}
 		if(p.getCurrentRoom().getExit(Direction.WEST)!=null){
 			JLabel westLabel = new JLabel(new ImageIcon("west_door.png"));
 		    westPanel.add(westLabel);
 		    westPanel.setBackground(new Color(185, 122, 87));
 		    westPanel.setBounds(120,158, 80, 310);
 		    consolePanel.add(westPanel, new Integer(1), 0);
 		}
 		if(p.getCurrentRoom().getExit(Direction.SOUTH)!=null){
 			JLabel southLabel = new JLabel(new ImageIcon("south_door.png"));
 		    southPanel.add(southLabel);
 		    southPanel.setBackground(new Color(69, 43, 29));
 		    southPanel.setBounds(385,490, 90, 90);
 		    consolePanel.add(southPanel, new Integer(1), 0);
 		}
 	}
 	
 	
 }
