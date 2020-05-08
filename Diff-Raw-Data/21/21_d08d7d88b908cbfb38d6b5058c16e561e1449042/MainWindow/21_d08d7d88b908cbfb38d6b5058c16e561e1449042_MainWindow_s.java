 package game;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JDialog;
 import javax.swing.JFrame;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame {
 	public static void main(String[] args) {
 		try {
 			System.setProperty("apple.laf.useScreenMenuBar", "true");
 		} catch (Exception e) {
 			// the program isn't running on a Mac, too bad for them
 		}
 
 		MainWindow window = new MainWindow();
 		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		window.setSize(new Dimension(900, 700));
 		window.setVisible(true);
 	}
 
 	private GameController game;
 	private String[] helpMessage;
 	private PlayerPanel playerPanel;
 
 	private SceneComponent sceneComponent;
 
 	public MainWindow() {
 		setLayout(new BorderLayout());
 
 		sceneComponent = new SceneComponent();
 		add(sceneComponent, BorderLayout.CENTER);
 		playerPanel = new PlayerPanel();
 		add(playerPanel, BorderLayout.EAST);
 
 		this.newGame();
 
 		setHelpMessage();
 
 		JMenuBar menuBar = new JMenuBar();
 		setJMenuBar(menuBar);
 		menuBar.add(createFileMenu());
 	}
 
 	public PlayerPanel getPlayerPanel() {
 		return playerPanel;
 	}
 
 	public void newGame() {
 		int playerCount = 0;
 
 		while (true) {
 			JOptionPane pane = new JOptionPane("Number of players:", JOptionPane.PLAIN_MESSAGE);
 			pane.setInitialSelectionValue("4");
 			pane.setWantsInput(true);
 			JDialog dialog = pane.createDialog(this, "Players");
 			dialog.setVisible(true);
 
 			try {
 				playerCount = Integer.parseInt((String) pane.getInputValue());
 			} catch (NumberFormatException ex) {
 				JOptionPane.showMessageDialog(this, "Invalid number", "Error", JOptionPane.ERROR_MESSAGE);
 				continue;
 			}
 
 			if (playerCount < 2) {
 				JOptionPane.showMessageDialog(this, "There must be at least two players", "Error", JOptionPane.ERROR_MESSAGE);
 				continue;
 			}
 
 			break;
 		}
 
 		game = new GameController(playerCount, sceneComponent, playerPanel);
 		playerPanel.updatePlayerListInfo();
 	}
 
 	private JMenuItem createFileExitItem() {
 
 		JMenuItem item = new JMenuItem("Exit");
 		class MenuItemListener implements ActionListener {
 			public void actionPerformed(ActionEvent e) {
 				System.exit(0);
 			}
 		}
 		item.addActionListener(new MenuItemListener());
 		return item;
 	}
 
 	private JMenuItem createFileHelpItem() {
 		JMenuItem item = new JMenuItem("Help");
 
 		class MenuItemListener implements ActionListener {
 			int count;
 			String[] helpMessage;
 			MainWindow mw;
 			String[] options;
 
 			public MenuItemListener(MainWindow mw) {
 				this.mw = mw;
 				helpMessage = mw.getHelpMessage();
				options = new String[3];
				options[0] = "Close";
				options[1] = "Next";
				options[2] = "Previous";
 
 				count = 0;
 			}
 
 			public void actionPerformed(ActionEvent e) {
 				int change;
 				do {
					change = JOptionPane.showOptionDialog(mw, helpMessage[count], "Game Directions", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[2]);
					;
					if (change == JOptionPane.YES_OPTION + 1)
 						count = (count + 1) % helpMessage.length;
					if (change == JOptionPane.NO_OPTION + 1)
 						if (count == 0)
 							count = helpMessage.length - 1;
 						else
 							count = (count - 1) % helpMessage.length;
				} while (change >= JOptionPane.CLOSED_OPTION + 1);
 			}
 		}
 		item.addActionListener(new MenuItemListener(this));
 		return item;
 	}
 
 	private JMenu createFileMenu() {
 
 		JMenu menu = new JMenu("File");
 		menu.add(createFileExitItem());
 		menu.add(createFileHelpItem());
 		return menu;
 	}
 
 	private String[] getHelpMessage() {
 		return helpMessage;
 	}
 
 	private void setHelpMessage() {
 		helpMessage = new String[14];
 		helpMessage[0] = "Each Lander is controlled by a seperate player.";
 		helpMessage[1] = "The current players Lander is seen by the angle arc" +
 				" above the current players Lander.";
 		helpMessage[2] = "All of the current players info is on the right of" +
 				" the screen.";
 		helpMessage[3] = "To change the angle, use the box on the right labeled" +
 				" angle, by pressing up or down to change the angle, or typing in" +
 				" a new angle";
 		helpMessage[4] = "To change the power, use the box on the right labeled" +
 				" power, by pressing up or down to change the power, or typing in" +
 				" a new power";
 		helpMessage[5] = "Use integers (regular numbers, no decimals) for the" +
 				" angle and power!";
 		helpMessage[6] = "You can fire either a missile or a transporter!";
 		helpMessage[7] = "A missile will create an explosion that will damage a" +
 				" Lander that is too close to the missile when it hits a planet.";
 		helpMessage[8] = "A teleporter will move your Lander to a new location," +
 				" wherever it hits a planet (you will not move if it fails to hit" +
 				" a planet.";
 		helpMessage[9] = "After you have adjusted the angle and power to what you" +
 				" would like, and chosen your projectile type, end your turn by" +
 				" pressing the fire button.";
 		helpMessage[10] = "The rockets will not fire until ever player has set their" +
 				" angle, power and projectile type, and after the last player has pressed the fire" +
 				" button, all the missiles will fire at once.";
 		helpMessage[11] = "Points are scored based on damage done to another players" +
 				" lander, and extra points are rewarded for finishing that players Lander";
 		helpMessage[12] = "The game ends when their is only one player left, or if" +
 				" all players have been destroyed";
 		helpMessage[13] = "The last player alive wins! If there is no one left," +
 				" the player with the most points wins! Good luck!";
 			
 	}
 }
