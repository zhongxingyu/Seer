 import javax.swing.*; 
 import java.awt.*; 
 import java.awt.event.*; 
 import java.io.*; 
 import java.util.*; 
 
 public class FanoronaGame implements ActionListener {
 	JFrame window = new JFrame("Fanorona Game");
 
 	// Game Menu variables
 	JMenuBar mainMenu = new JMenuBar();
 	JMenuItem newGame = new JMenuItem("New Game"), 
 				about = new JMenuItem("About"),
 				instructions = new JMenuItem("Instructions"),
 				exit = new JMenuItem("Exit");
 	// Buttons for Menus
 	JButton pve = new JButton("Player vs CPU"),
 			back = new JButton("exit");
 	JButton buttonArray[] = new JButton[32];
 
 	// Panels for Graphic interface
 	JPanel	newGamePanel = new JPanel(),
 			northPanel = new JPanel(),
 			southPanel = new JPanel(),
 			topPanel = new JPanel(),
 			bottomPanel = new JPanel(),
 			playingFieldPanel = new JPanel();
 
 	JLabel gameTitle = new JLabel("Fanorona");
 	JTextArea text = new JTextArea();
 
 	// set window size and default color
 	final int windowX = 900, windowY = 500, color = 190; 
 
 	public FanoronaGame() {
 		// Game window formatting
 		window.setSize(windowX, windowY);
 		window.setLocation(400, 400);
 		window.setResizable(false);
 		window.setLayout(new BorderLayout());
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 
 		//Panel properties
 		newGamePanel.setLayout(new GridLayout(2, 1, 2, 10));
 		northPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		southPanel.setLayout( new FlowLayout(FlowLayout.CENTER));
 		topPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		bottomPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 
 		northPanel.setBackground(new Color(color-20, color-20, color-20));
 		southPanel.setBackground(new Color(color, color, color));
 		bottomPanel.setBackground(new Color(color, color, color));
 		topPanel.setBackground(new Color(color, color, color));
 
 		// Create Menu Bars
 		mainMenu.add(newGame);
 		mainMenu.add(about);
 		mainMenu.add(instructions);
 		mainMenu.add(exit);
 
 		newGamePanel.add(pve);
 
 		// Add action listeners
 		newGame.addActionListener(this);
 		exit.addActionListener(this);
 		instructions.addActionListener(this);
 		about.addActionListener(this);
 		back.addActionListener(this);
 		pve.addActionListener(this);
 
 		//Game board setup
 		playingFieldPanel.setLayout(new GridLayout(4, 8, 2, 2));
 		playingFieldPanel.setBackground(Color.black);
 		for (int i = 0; i < 32; i++) {
 			buttonArray[i] = new JButton();
 			buttonArray[i].setBackground(new Color(220, 220, 220));
 			buttonArray[i].addActionListener(this);
 			playingFieldPanel.add(buttonArray[i]);
 		}
 
 		northPanel.add(mainMenu);
 		southPanel.add(gameTitle);
 
 		window.add(northPanel, BorderLayout.NORTH);
 		window.add(southPanel, BorderLayout.CENTER);
 		window.setVisible(true);
 	}
 
 	public void actionPerformed(ActionEvent click) {
 		Object actionSource = click.getSource();
 		if (actionSource == newGame) {
 			int option = askMessage("Are you prepared to start a new game vs CPU?" +
 				" WARNING: All current progress will be lost.\n", 
 				"Quit Game?", JOptionPane.YES_NO_OPTION);
 			if(option == JOptionPane.YES_OPTION) {
 				// do stuff
 			}
 		}
 		else if (actionSource == exit) {
 			int option = askMessage("Are you sure you want to exit?", "Exit Game", JOptionPane.YES_NO_OPTION); 
 			if(option == JOptionPane.YES_OPTION) 
 				System.exit(0);
 		}
 		else if (actionSource == about) {
			JOptionPane.showMessageDialog(null, "This Game was created by Megan Kerins, Patrick Casey, and Matt Hacker.\n" +
 					"Current Version: 0.1\n" +
 					"Team 04, CSCE 315-501\n",
 					"About", JOptionPane.ERROR_MESSAGE);
 		}
 		else if (actionSource == instructions) {
 			JOptionPane.showMessageDialog(null, "Move your piece toward or way from an enemy piece to capture it.\n" +
 					"You may only move a piece that will cause the capture of an enemy piece.\n" +
 					"The Game will continue as long as there are valid move to be made.\n" +
 					"More instructions to follow...", "Instructions", JOptionPane.ERROR_MESSAGE);
 		}
 	}
 
 	public int askMessage(String message, String title, int option) { 
 		return JOptionPane.showConfirmDialog(null, message, title, option); 
 	} 
 
 	public static void main(String[] args) {
 		 new FanoronaGame();
 	}
 }
