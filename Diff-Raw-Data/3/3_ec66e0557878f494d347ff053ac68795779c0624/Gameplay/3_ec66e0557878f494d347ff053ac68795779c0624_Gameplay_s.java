 package Alex;
 
 import javax.swing.JOptionPane;
 
 import upietz.Feld;
 import upietz.Spielfeld;
 import anika.Player;
 import axel.Draw;
 import axel.Sound;
 import controller.Controller;
 
 /**
  * Gameplay, responsible for the communication between player, Spielfeld, view
  * and controller
  * 
  * @author Volo
  * 
  */
 public class Gameplay {
 	/**
 	 * Instanzvariablen
 	 */
 	private Spielfeld board; // Board
 	private int playerCount; // Amount of players
 	private Player[] player; // Array with all players
 	private Draw screen; // View
 	private Controller control; // Controller
 
 	/**
 	 * Constructor
 	 * 
 	 * Creates a (standard) board and the wished amount of players.
 	 * 
 	 * @param int player
 	 */
 	public Gameplay(int player, Controller control) {
 		this.control = control;
 		// Saves the amount of players
 		// this.playerCount = player;
 		this.playerCount = player;
 
 		// Initialize a Draw Object
 		this.screen = new Draw(control);
		new Sound(System.getProperty("user.dir") +"/graphics/musik.wav").loop();
 
 		// Create the board, at first with hardcoded values
 		try {
 			this.board = new Spielfeld(15, 15, null, this.playerCount,
 					this.screen, this, control);
 		} catch (Exception e) {
 			control.print("Spielfeld erstellen gescheitert: " + e.getMessage());
 		}
 
 		// Create as many player instances as wished
 		this.player = new Player[this.playerCount];
 		for (int id = 0; id < this.playerCount; id++) {
 			createPlayer(id);
 		}
 
 	}
 
 	/**
 	 * Constructs a Gameplay object from saved data
 	 * 
 	 * @param playerInfo
 	 *            Strings containing the player data
 	 * @param board
 	 *            Strings containing the serialized board
 	 * @param c
 	 *            The controller
 	 * @return
 	 */
 	public static Gameplay restore(String[] playerInfo, String[] board,
 			Controller c) {
 		Gameplay gp = new Gameplay(playerInfo.length - 1, c);
 		Draw d = gp.screen;
 		Controller control = c;
 
 		int width = Integer.valueOf(board[0].split(",")[0]);
 		int height = Integer.valueOf(board[0].split(",")[1]);
 		Feld[][] feld = new Feld[width][height];
 		for (int i = 1; i < board.length; i++) {
 			int row = (int) ((i - 1) / width);
 			int column = i - 1 - row * width;
 			Feld f = new Feld();
 			String[] info = board[i].split(",");
 			f.typ = Integer.valueOf(info[0]);
 			f.belegt = Integer.valueOf(info[1]);
 			f.hasBomb = (info[2].equals("1") ? true : false);
 			f.isExit = (info[3].equals("1") ? true : false);
 			feld[row][column] = f;
 		}
 
 		Spielfeld gameboard = new Spielfeld(height, width, d, gp, control, feld);
 		gp.board = gameboard;
 		Player[] players = new Player[playerInfo.length - 1];
 		for (int i = 1; i < playerInfo.length; i++) {
 			String[] info = playerInfo[i].split(",");
 			int[] coordinates = { Integer.valueOf(info[0]),
 					Integer.valueOf(info[1]) };
 			Player p = new Player(i - 1, gp.board, d, gp, coordinates);
 			players[i - 1] = p;
 		}
 		gp.player = players;
 		d.drawBoard(gp.board.getStructure(), height, width);
 		return gp;
 	}
 
 	/**
 	 * Constructor without integer parameter initializes game for 1 player as
 	 * default
 	 */
 	public Gameplay(Controller control) {
 		this(1, control);
 	}
 
 	/**
 	 * createPlayer
 	 * 
 	 * Creates instance of Player with transmitted it und saves it in array
 	 * this.player
 	 * 
 	 * @param int id
 	 */
 	private void createPlayer(int id) {
 		this.player[id] = new Player(id, this.board, this.screen, this);
 	}
 
 	/**
 	 * controls
 	 * 
 	 * Receives key inputs and sends them to the relevant spaces
 	 * 
 	 * @param String
 	 *            key
 	 */
 	public void controls(String key) {
 
 		if (key.equals("pause")) {
 			System.out.println("Pause...");
 		}
 
 		if (key.equals("left")) {
 			this.player[0].moveLeft();
 		} else if (key.equals("right"))
 			this.player[0].moveRight();
 		else if (key.equals("up"))
 			this.player[0].moveUp();
 		else if (key.equals("down"))
 			this.player[0].moveDown();
 		else if (key.equals("bomb"))
 			this.player[0].dropBomb();
 
 		if (key.equals("a"))
 			this.player[1].moveLeft();
 		else if (key.equals("d"))
 			this.player[1].moveRight();
 		else if (key.equals("w"))
 			this.player[1].moveUp();
 		else if (key.equals("s"))
 			this.player[1].moveDown();
 		else if (key.equals("y"))
 			this.player[1].dropBomb();
 
 	}
 
 	/**
 	 * gameWon
 	 * 
 	 * Gets player's id as parameter. This one has won the game.
 	 * 
 	 * @param int id
 	 */
 	public void gameWon(int id) {
 		// ?
 		System.out.println("And the winner is: Player " + id);
 		gameOver();
 		// System.exit(0);
 	}
 
 	/**
 	 * gameOver
 	 * 
 	 * If every player is dead, game is lost.
 	 */
 	public void gameOver() {
 		// ?
 		control.print("Game Over!");
 		JOptionPane.showMessageDialog(null, "Das Spiel ist zu Ende!",
 				"Spielstand", JOptionPane.OK_CANCEL_OPTION);
 		// System.exit(0);
 	}
 
 	/**
 	 * deregisterPlayer
 	 * 
 	 * If a player is dead, he deregisters with this method.
 	 * 
 	 * ToDo: Im Moment gibt es ja nur einen Spieler, also wird das Spiel sofort
 	 * beendet. In Zukunft sollte hier eine Abfrage stehen die nachsieht, ob
 	 * noch ein Player aktiv ist. Sind es mehr als einer geht das Spiel weiter,
 	 * ist es genau einer hat dieser gewonnen, ist keiner mehr aktiv ist das
 	 * Spiel vorbei.
 	 * 
 	 * @param int id
 	 */
 	public void deregisterPlayer(int id) {
 		// Informs player, that he is dead.
 		this.player[id].die();
 		gameOver();
 	}
 
 	/**
 	 * Provides access to the game board
 	 * 
 	 * @return the current game board value
 	 */
 	public Spielfeld getBoard() {
 		return board;
 	}
 
 	/**
 	 * This method allows access to the array of Player objects
 	 * 
 	 * @param index
 	 *            the index of the desired Player object in the array
 	 * @return The indicated Player object
 	 */
 	public Player getPlayer(int index) {
 		if (index < 0 || index > player.length)
 			return null;
 		return player[index];
 	}
 
 	public int getNumOfPlayers() {
 		return playerCount;
 	}
 }
