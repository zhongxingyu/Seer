 package mancala;
 
 import java.awt.GridLayout;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  * Mancala GUI class.
  */
 public class Gui extends JFrame {
 	private static final long serialVersionUID = -1049958103353244632L;
 	
 	private static int NUM_PLAYERS = 2;
 	private static int NUM_HOUSES_PER_PLAYER = 6;
 	
 	// menu specific variables
 	JMenuBar menuBar;
 	JMenu mainMenu, helpMenu;
 	JMenuItem newGameMenuItem;
 	JMenuItem restartGameMenuItem;
 	JMenuItem playerNamesMenuItem;
 	JMenuItem highscoresMenuItem;
 	JMenuItem quitMenuItem;
 	JMenuItem helpMenuItem;
 	
 	// stores & mancalas
 	JButton[] stores;
 	JButton[][] houses;
 	
 	// grid for empty buttons in GridLayout
 	JButton[][] buttonGrid;
 	private static int GRID_WIDTH = 8;
 	private static int GRID_HEIGHT = 3;
 	
 	/**
 	 * Setup menus.
 	 */
 	private void setupMenus() {
 		menuBar = new JMenuBar();
 		
 		// main menu with items
 		mainMenu = new JMenu("Mancala");
		newGameMenuItem = new JMenuItem("Play Player vs Player");
 		restartGameMenuItem = new JMenuItem("Restart game");
 		playerNamesMenuItem = new JMenuItem("Edit player names");
 		highscoresMenuItem = new JMenuItem("Show highscores");
 		quitMenuItem = new JMenuItem("Quit Mancala");
 		
 		mainMenu.add(newGameMenuItem);
 		mainMenu.add(restartGameMenuItem);
 		mainMenu.add(playerNamesMenuItem);
 		mainMenu.add(highscoresMenuItem);
 		mainMenu.addSeparator();
 		mainMenu.add(quitMenuItem);
 		
 		// help menu
 		helpMenu = new JMenu("Help");
 		helpMenuItem = new JMenuItem("Show manual");
 		
 		helpMenu.add(helpMenuItem);
 		
 		// add menus to menubar
 		menuBar.add(mainMenu);
 		menuBar.add(helpMenu);
 		
 		setJMenuBar(menuBar);
 	}
 	
 	/**
 	 * Put the stores and houses onto layout and initialize them.
 	 */
 	private void setupStoresAndHouses() {
 		GridLayout layout = new GridLayout(Gui.GRID_HEIGHT, Gui.GRID_WIDTH);
 		setLayout(layout);
 		
 		// create the buttongrid
 		buttonGrid = new JButton[Gui.GRID_WIDTH][Gui.GRID_HEIGHT];
 		for (int j=0 ; j<Gui.GRID_HEIGHT ; ++j) {
 			for (int i=0 ; i<Gui.GRID_WIDTH ; ++i) {
 				buttonGrid[i][j] = new JButton(i + " " + j);
 				buttonGrid[i][j].setVisible(false);
 			}
 		}
 		
 		// setup player name buttons
 		buttonGrid[0][0].setText("Alice");
 		buttonGrid[7][2].setText("Bob");
 		buttonGrid[0][0].setVisible(true);
 		buttonGrid[7][2].setVisible(true);
 		buttonGrid[0][0].setEnabled(false);
 		buttonGrid[7][2].setEnabled(false);
 		
 		// creates stores
 		stores = new JButton[Gui.NUM_PLAYERS];
 		for (int i=0 ; i<Gui.NUM_PLAYERS ; ++i) {
 			stores[i] = new JButton("0");
 		}
 		
 		// put them into buttongrid
 		buttonGrid[0][1] = stores[0];
 		buttonGrid[7][1] = stores[1];
 		
 		// create houses
 		houses = new JButton[Gui.NUM_PLAYERS][Gui.NUM_HOUSES_PER_PLAYER];
 		
 		for (int i=0 ; i<Gui.NUM_HOUSES_PER_PLAYER ; ++i) {
 			houses[0][i] = new JButton("0");
 			houses[1][i] = new JButton("0");
 			buttonGrid[i+1][0] = houses[0][i];
 			buttonGrid[i+1][2] = houses[1][i];
 		}
 		
 		// add buttongrid to layout
 		for (int j=0 ; j<Gui.GRID_HEIGHT ; ++j) {
 			for (int i=0 ; i<Gui.GRID_WIDTH ; ++i) {
 				add(buttonGrid[i][j]);
 			}
 		}
 	}
 	
 	/**
 	 * Initializes necessary components of Mancala GUI.
 	 */
 	public Gui() {
 		super();
 		this.setDefaultCloseOperation(EXIT_ON_CLOSE);
 		this.setupStoresAndHouses();
 		this.setupMenus();
 		this.setSize(600, 400);
 		this.setTitle("Mancala");
 	}
 	
 	/**
 	 * Main function for testing purposes
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		Gui gui = new Gui();
 		gui.setVisible(true);
 	}
 }
