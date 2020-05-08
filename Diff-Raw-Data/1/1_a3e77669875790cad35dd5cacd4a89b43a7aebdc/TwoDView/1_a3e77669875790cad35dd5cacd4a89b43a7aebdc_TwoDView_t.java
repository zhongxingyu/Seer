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
 
 
 
 
 public class TwoDView extends JFrame implements IView, ActionListener
 {
 	private JMenuItem resetGame, objective, hint, quit; //commands
 	private JMenuBar menuBar;
 	private JButton undo, redo, northRoom, southRoom, eastRoom, westRoom, pickup, fight, eat, drop, inspect;
 	private JLabel currentRoom, mapLabel;
 	private JTextArea consoleField;
 	private JPanel consolePanel, inventoryPanel, centralPanel, undoRedoPanel, mapPanel;
 	private Player p, reset;
 	private JList inventoryList;
 	private DefaultListModel inventoryModel;
 	private boolean unlocked = false;
 
 	public TwoDView (Player p) {
 		this.p = p;
 		reset = p;
 		setDefaultCloseOperation(EXIT_ON_CLOSE);
 		menuBar = new JMenuBar( );
 	    setJMenuBar( menuBar );
 	    setLayout(new GridLayout(3,3));
 	    this.setExtendedState(this.MAXIMIZED_BOTH);
 
 	    undo = new JButton("UNDO");
 	    redo = new JButton("REDO");
 	    northRoom = new JButton("North Room");
 	    eastRoom = new JButton("East Room");
 	    westRoom = new JButton("West Room");
 	    southRoom = new JButton("South Room");
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
 	    
 //	    commands = new JMenuItem("Commands");
 //	    helpMenu.add(commands);
 //	    commands.addActionListener(this);
 	    
 	    hint = new JMenuItem("Hint");
 	    helpMenu.add(hint);
 	    hint.addActionListener(this);
 
 	    update();
 	}
 
 
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
 	
 	public void updateMapPanel(){
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
 	
 	
 	@Override
 	public void displayHelp() {/* String is needed, so getHelp method is used instead.*/}
 	
 	public String getObjective(){
 		String str = "";
 		str+="Welcome to the World of Zuul.\nCan you conquer the monsters and find the long lost treasure of Zuul?\n";
 		return str;
 	}
 	
 //	public String getCommands(){
 //		String str="Your command words are:\n";
 //		for (CommandWords commandWord : CommandWords.values()) {
 //			str+= commandWord + " ";
 //		}
 //		return str;
 //	}
 	
 	public void fightPopUp(){
 		Monster m = p.getCurrentRoom().getMonster();
 		JOptionPane.showMessageDialog(this, "" + p.getName() + " attacked " + m.getName() + " and did " + p.getBestItem().getValue() + " Damage\n"
 				 + m.getName() + " attacked " + p.getName() + " and did " + m.getBestItem().getValue()*m.getLevel()  + " Damage\n"); 
 	}
 	
 	public void getHint(){
 		if(!p.getInventory().contains(new Item("Map", true))){
 			JOptionPane.showMessageDialog(this, "Find the map!\nTry the room west of the startroom!");
 		} else if(!p.getInventory().contains(new Item("Key", true))){
 			JOptionPane.showMessageDialog(this, "Find the key!\nPerhaps the boss in the southroom has it!");
 		} else {
 			JOptionPane.showMessageDialog(this, "Locate the treasure, the game is yours!");
 		}
 	}
 
 	@Override
 	public void gameDone() {
 		JOptionPane.showMessageDialog(this, "You have been defeated!");
 		quit();
 	}
 
 	public void win(){
 		JOptionPane.showMessageDialog(this, "Congratulations!\nYou recovered the long lost treasure of Zuul and bested all the monsters!\nYou win!");
 		quit();		
 	}
 	
 	
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
 	
 
 	@Override	public void monsterMissing() {/* Checked by disabling the button, this will never be called*/}
 	@Override	public void garbageCommand() {/* checked by using buttons, no way to enter garbage*/}
 	@Override	public void invalidRoom() {	/* Checked by disabling buttons*/}
 	@Override	public void eatingWeapon(Item i) {/*// Checked by disabling buttons*/}
 	@Override	public void noItem(Item i) {/* Checked by disabling button*/}
 	@Override	public void itemInvalid(Item i) {/* Checked by disabling button*/}
 	@Override	public void itemError(Item i) {/* Checked by disabling button*/}
 	@Override	public void inCompleteCommand() {/* Impossible with GUI*/}
 	@Override	public void undoRedoUnavailable(CommandWords commandWord) {/* Disabling buttons when appropriate*/}
 	
 	public String updateConsole(){
 		String s = "";
 		s += ("Player Health: " + p.getHealth() + "\n");
 		if(p.getCurrentRoom().getMonster()!=null){
 			s+= ("Monster Health: " + p.getCurrentRoom().getMonster().getHealth());
 		}
 		return s;
 	}
 	
 	@Override
 	public void quit() {
 		System.exit(0);
 	}
 	
 	public void reset(){
 		while(p.canUndo()){
 			p.doCommand(Command.parse("Undo"));
 		}
 		p = reset;
 		p.setHealth(p.MAX_HEALTH);
 		p.getPlayerHistory().clear();
		unlocked = false;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if (e.getActionCommand().equals("Reset")) {
 			reset();
 		}
 		else if (e.getActionCommand().equals("Objective")) {
 			JOptionPane.showMessageDialog(this, getObjective());
 		} 
 //		else if (e.getActionCommand().equals("Commands")){
 //			JOptionPane.showMessageDialog(this, getCommands());
 //		}
 		else if(e.getActionCommand().equals("Hint")){
 			getHint();
 		}
 		else if (e.getActionCommand().equals("North Room")) {
 			p.doCommand(Command.parse("Go North"));
 		}
 		else if (e.getActionCommand().equals("East Room")) {
 			p.doCommand(Command.parse("Go East"));
 		}
 		else if (e.getActionCommand().equals("West Room")) {
 			
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
 		else if (e.getActionCommand().equals("South Room")) {
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
 				JOptionPane.showMessageDialog(this, selectedItem.getDescription());
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
