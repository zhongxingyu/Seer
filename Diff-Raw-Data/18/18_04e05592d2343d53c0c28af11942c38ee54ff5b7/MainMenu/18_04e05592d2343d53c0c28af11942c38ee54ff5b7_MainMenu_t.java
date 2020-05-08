 package de.propra12.gruppe04.dynamiteboy.Menu;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 import de.propra12.gruppe04.dynamiteboy.Game.Game;
 
 public class MainMenu extends JPanel {
 	private JButton ButtonStart1p;
 	private JButton ButtonStart2p;
 	private JButton ButtonNetworkPlay;
 	private JButton ButtonMapEditor;
 	private JFrame frame;
 	private TitlePanel title = new TitlePanel();
 
 	private JPanel panelButton = new JPanel(new GridLayout(1, 3));
 
 	/**
 	 * Constructor sets up MainMenu with buttons for: <br>
 	 * - Starting a singleplayer game <br>
 	 * - Starting a multiplayer game <br>
 	 * - Going to the NetworkMenu <br>
 	 * - Starting the MapEditor <br>
 	 * 
 	 */
 	public MainMenu(final JFrame frame) {
 		this.frame = frame;
 		frame.setTitle("DynamiteBoy - Hauptmenü");
 		ButtonStart1p = new JButton("1 Spieler");
 		ButtonStart2p = new JButton("2 Spieler");
 		ButtonNetworkPlay = new JButton("Netzwerkspiel");
 		ButtonMapEditor = new JButton("Karten Editor");
 		panelButton.add(ButtonStart1p);
 		panelButton.add(ButtonStart2p);
 		panelButton.add(ButtonNetworkPlay);
 		panelButton.add(ButtonMapEditor);
 
 		/**
 		 * Sets up a single player game
 		 */
 		ButtonStart1p.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ButtonStart1p.setText("Spiel wird gestartet...");
 				String map = askForMap();
 				loadGame(1, map);
 			}
 
 		});
 		/**
 		 * Sets up a two player game
 		 */
 		ButtonStart2p.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				ButtonStart2p.setText("Spiel wird gestartet...");
 				loadGame(2, "Maze.xml");
 			}
 		});
 
 		/**
 		 * Go to Network Menu for setting up a network game
 		 * 
 		 */
 		ButtonNetworkPlay.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				title.setVisible(false);
 				panelButton.setVisible(false);
 				NetworkMenu nMenu = new NetworkMenu(frame);
 			}
 
 		});
 
 		/**
 		 * Go to Editor Menu and start editor
 		 */
 		ButtonMapEditor.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				title.setVisible(false);
 				panelButton.setVisible(false);
 				EditorMenu eMenu = new EditorMenu(frame);
 			}
 
 		});
 
 		frame.getContentPane().add(BorderLayout.CENTER, this.title);
 		ButtonStart1p.setPreferredSize(new Dimension(100, 80));
 		ButtonStart2p.setPreferredSize(new Dimension(100, 80));
 		frame.getContentPane().add(BorderLayout.SOUTH, panelButton);
 	}
 
 	/**
 	 * Asks for the Player to choose a Map and returns it
 	 * 
 	 * @return chosen map
 	 */
 	public String askForMap() {
 		String map = "";
 		Object[] maps = { "Standardmap", "Spaßmap", "Multiplayermap" };
 		map = (String) JOptionPane.showInputDialog(frame,
 				"Bitte Map wählen: \n", "Map", JOptionPane.PLAIN_MESSAGE, null,
 				maps, "");
 		if ((map != null) && (map.length() > 0)) {
 			if (map.equals(maps[0])) {
 				map = "Map1.xml";
 				return map;
 			}
 			if (map.equals(maps[1])) {
 				map = "fghfdgh.xml";
 				return map;
 			}
 			if (map.equals(maps[2])) {
 				map = "Maze.xml";
 				return map;
 			}
		} else if (map == null) {
			map = "Map1.xml";
 		}
 		return map;
 	}
 
 	/**
 	 * Loads a game with passed number of players and map
 	 * 
 	 * @param numberOfPlayers
 	 *            Number of players to create the game with ( currently max. 2
 	 *            players)
 	 * @param mapName
 	 *            Name of the XML file
 	 */
 	public void loadGame(int numberOfPlayers, String mapName) {
 		Game game = new Game(frame, numberOfPlayers, mapName);
 		frame.getContentPane().add(game);
 		title.setVisible(false);
 		panelButton.setVisible(false);
 		game.setVisible(true);
 	}
 
 	/**
 	 * Class to paint the title image
 	 * 
 	 */
 	static class TitlePanel extends JPanel {
 		private String titleScreenImage = "../images/db_menu_titlescreen.png";
 		ImageIcon img = new ImageIcon(this.getClass().getResource(
 				titleScreenImage));
 
 		public void paintComponent(Graphics g) {
 			g.drawImage(img.getImage(), 0, 0, this);
 
 		}
 
 	}
 
 }
