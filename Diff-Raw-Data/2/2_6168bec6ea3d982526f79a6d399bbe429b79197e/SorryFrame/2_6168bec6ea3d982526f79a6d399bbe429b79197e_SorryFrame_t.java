 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 /**
  * Extension of JFrame used to contain necessary menus and the Sorry board game.
  * 
  * @author sturgedl. Created Mar 25, 2013.
  */
 public class SorryFrame extends JFrame implements ActionListener {
 	private static final int BOARD_WIDTH = 1000;
 	private static final int BOARD_HEIGHT = 1000;
 	private static final int BOARD_ROWS = 16;
 	private static final int BOARD_COLS = 16;
 	private static final double CELL_WIDTH = ((double) BOARD_WIDTH / BOARD_COLS);
 	private static final double CELL_HEIGHT = ((double) BOARD_HEIGHT / BOARD_ROWS);
 	private static final int FRAME_X_PAD = 10;
 	private static final int FRAME_Y_PAD = 30;
 
 	private volatile int clickCount = 0;
 	private volatile ArrayList<Coordinate> clicks = new ArrayList<Coordinate>();
 	private static final long serialVersionUID = 1L;
 	protected BoardList board;
 	protected EngineInterface engine;
 	protected Card currentCard;
 	private FileReader fr;
 	private String[] userMessages;
 	private UIComponent gui;
 
 	/*
 	 * Indices 0-3 are red, Indices 4-7 are blue, Indices 8-11 are Yellow,
 	 * Indices 12-15 are green
 	 */
 	/**
 	 * Basic constructor. Does what it does.
 	 * 
 	 * @param board
 	 * @param engine
 	 */
 	public SorryFrame(String lang, EngineInterface eng) {
 		super("Sorry!");
 		this.setEnabled(true);
 		this.board = new BoardList();
 		try {
 			fr = new FileReader(lang + ".txt");
 		} catch (FileNotFoundException e) {
 		}
 		this.engine = eng;
 		this.engine.newGame();
 		Scanner in = new Scanner(fr);
 		for (int x = 0; x < 12; x++)
 			in.nextLine();
 		userMessages = new String[10];
 		for (int x = 0; x < 9; x++)
 			userMessages[x] = in.nextLine();
 		this.setSize(1330, 1040);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JComponent displayBoard = new DisplayableBoard(eng);
 		this.add(displayBoard, BorderLayout.CENTER);
 		gui = new UIComponent(300, 1000, this, lang);
 		this.add(gui, BorderLayout.EAST);
 	}
 
 	public SorryFrame(String lang) {
 		super("Sorry!");
 		this.setEnabled(true);
 		this.board = new BoardList();
 		try {
 			fr = new FileReader(lang + ".txt");
 		} catch (FileNotFoundException e) {
 		}
 		this.engine = new Engine(this.board, lang);
 		this.engine.newGame();
 		Scanner in = new Scanner(fr);
 		for (int x = 0; x < 12; x++)
 			in.nextLine();
 		userMessages = new String[9];
 		for (int x = 0; x < 9; x++)
 			userMessages[x] = in.nextLine();
 		this.setSize(1330, 1040);
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		JComponent displayBoard = new DisplayableBoard(this.engine);
 		this.add(displayBoard, BorderLayout.CENTER);
 		gui = new UIComponent(300, 1000, this, lang);
 		this.add(gui, BorderLayout.EAST);
 	}
 
 	public void start() {
 		this.engine.rotatePlayers();
 		this.engine.getNextCard();
 		gui.repaint();
 		this.setVisible(true);
 		this.repaint();
 		this.addMouseListener(new BoardMouseListener(this));
 		this.initiateTurn();
 		updateThread t = (new updateThread());
 		t.e = this.engine;
 		t.s = this;
 		t.start();
 
 	}
 
 	public void load(String filename) {
 		File file = new File(filename);
 		Scanner reader;
 		try {
 			reader = new Scanner(file);
 			ArrayList<String> players = new ArrayList<String>();
 			while (reader.hasNext()) {
 				players.add(reader.nextLine());
 			}
 			int a = passPlayers(players);
 			this.board = new BoardList((players.get(a + 1)));
 			this.engine.load(this.board, this.board.clone(),
 					this.board.pieceList);
 		} catch (FileNotFoundException e) {
 			e.printStackTrace();
 		}
 	}
 
 	public int passPlayers(ArrayList<String> str) {
 		int ret = 0;
 		for (String string : str) {
 			if (string == "") {
 				break;
 			}
 			String[] player = string.split("\\|");
 			if (player.length != 2) {
 				break;
 			}
 			switch (player[1]) {
 			case "red":
 				this.engine
 						.insertPlayer(new Player(Piece.COLOR.red, player[0]));
 				break;
 			case "green":
 				this.engine.insertPlayer(new Player(Piece.COLOR.green,
 						player[0]));
 				break;
 			case "blue":
 				this.engine
 						.insertPlayer(new Player(Piece.COLOR.blue, player[0]));
 				break;
 			case "yellow":
 				this.engine.insertPlayer(new Player(Piece.COLOR.yellow,
 						player[0]));
 				break;
 			}
 			ret++;
 		}
 		return ret;
 	}
 
 	/**
 	 * Given an (x, y) tuple of doubles, will return an appropriate board grid
 	 * coordinate of integers.
 	 * 
 	 * @param x
 	 * @param y
 	 * 
 	 * @return Coordinate, position on board corresponding to x and y
 	 */
 	public static Coordinate convertClickToCoordinate(double x, double y) {
 		if (x > BOARD_WIDTH || x < 0)
 			throw new CoordinateOffOfBoardException("Bad location: x = " + x
 					+ " y = " + y);
 
 		if (y > BOARD_HEIGHT || y < 0)
 			throw new CoordinateOffOfBoardException("Bad location: x = " + x
 					+ " y = " + y);
 
 		int xCoord = (int) Math.floor((x / CELL_WIDTH));
 		int yCoord = (int) Math.floor((y / CELL_HEIGHT));
 
 		return new Coordinate(xCoord, yCoord);
 	}
 
 	/**
 	 * 
 	 * Informs the player that something bad has happened and let's them fix
 	 * their problem. Stupid users always screwing stuff up, no wonder Clue
 	 * wanted them dead.
 	 * 
 	 * @param msg
 	 */
 	private void informPlayerError(String msg) {
 		this.notifyPlayer(msg);
 	}
 
 	/**
 	 * 
 	 * Asks engine for a card, displays that card. Instructs engine to swap
 	 * active player. Begins listening to mouse input.
 	 * 
 	 */
 	protected void initiateTurn() {
 		this.engine.getUpdatedInfo();
 		this.currentCard = this.engine.getCurrentCard();
 		// System.out.println(this.currentCard.toString());
 		// this.engine.rotatePlayers();
 		Player pl;
		if(this.engine.getOwner() != null){
 			pl = this.engine.getOwner();
 		}
 		else{
 			pl = this.engine.getActivePlayer();
 		}
 		if (pl.getColor() == Piece.COLOR.blue) {
 			gui.playerInformation.setBackground(Color.CYAN);
 		} else if (pl.getColor() == Piece.COLOR.green)
 			gui.playerInformation.setBackground(Color.GREEN);
 		else if (pl.getColor() == Piece.COLOR.yellow)
 			gui.playerInformation.setBackground(Color.YELLOW);
 		else
 			gui.playerInformation.setBackground(Color.RED);
 		this.gui.playerNameText
 				.setText(pl.getName());
 		this.gui.update();
 		this.repaint();
 		// this.notifyPlayer(userMessages[0]);
 
 	}
 
 	/**
 	 * 
 	 * Waits for mouse input, converts them to coords. Relays coords to engine,
 	 * checking if the move was legal and if the turn should end. Checks for
 	 * turn forfeit, reverts the board if so. If turn is done, finalizes turn in
 	 * engine. Initiates next player's turn.
 	 * 
 	 */
 	private void performTurn() {
 		int result = this.engine.pawnMove(this.clicks.get(0),
 				this.clicks.get(1));
 		this.repaint();
 		if (result == Engine.SAME_NODE_SELECTED) {
 			this.informPlayerError(userMessages[1]);
 		} else if (result == Engine.INVALID_MOVE) {
 			this.informPlayerError(userMessages[2]);
 		} else if (result == Engine.NODE_NOT_FOUND) {
 			this.informPlayerError(userMessages[3]);
 		} else if (result == Engine.NO_PIECE_SELECTED) {
 			this.informPlayerError(userMessages[4]);
 		} else if (result == Engine.VALID_MOVE_NO_FINALIZE) {
 			// just wait for another turn
 		} else if (result == Engine.INACTIVE_PLAYER){
 			this.informPlayerError(userMessages[9]);
 		}
 		
 		else {
 			// turn is over, rotate
 			if (this.engine.finalizeTurn()) {
 				this.notifyPlayer(userMessages[5]);
 				this.markGameComplete();
 			} else {
 				this.initiateTurn();
 			}
 		}
 	}
 
 	/**
 	 * Eventually decides what to do upon victory. For now, nothing really.
 	 * 
 	 */
 	private void markGameComplete() {
 		// TODO Auto-generated method stub.
 
 	}
 
 	private void resetClickDetection() {
 		this.clicks.clear();
 		this.clickCount = 0;
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg0) {
 		// TODO Auto-generated method stub
 
 	}
 
 	private void registerMouseClick(Coordinate coord) {
 		this.clickCount++;
 		this.clicks.add(coord);
 	}
 
 	private void notifyPlayer(String message) {
 		JOptionPane.showMessageDialog(this, this.engine.getActivePlayer()
 				.getName() + message, userMessages[8],
 				JOptionPane.PLAIN_MESSAGE);
 	}
 
 	/**
 	 * TODO Put here a description of what this method does.
 	 * 
 	 */
 	public void quitGame() {
 		this.saveGame();
 		System.exit(0);
 
 	}
 
 	/**
 	 * TODO Put here a description of what this method does.
 	 * 
 	 */
 	public void saveGame() {
 		File save = new File("save.txt");
 		try {
 			this.engine.save(save);
 		} catch (IOException e) {
 			System.out.println("IT SPLODED");
 			e.printStackTrace();
 		}
 	}
 
 	/**
 	 * Forfeit the current player's turn.
 	 * 
 	 */
 	public void forfeitTurn() {
 		this.resetClickDetection();
 		this.engine.forfeit();
 		this.initiateTurn();
 	}
 
 	/**
 	 * Container class for mouse-click coordinates. Really just to provide
 	 * convenience, because Java is really horrible at dealing with multiple3
 	 * return values. If this was a nice language like Python or Scheme or
 	 * really almost anything else then I could just return a tuple but because
 	 * it's Java and whatnot I have to write an entire freaking class just to
 	 * conveniently return 2 integers. So yeah. It contains 2 integers.
 	 * 
 	 * @author sturgedl. Created Mar 24, 2013.
 	 */
 	protected static class Coordinate {
 		private static final int HASH_BROWNS = 17;
 		private static final int SALT = 113;
 		private int x;
 		private int y;
 
 		/**
 		 * Makes a coordinate. Don't be stupid.
 		 * 
 		 * @param x
 		 * @param y
 		 */
 		public Coordinate(int x, int y) {
 			this.x = x;
 			this.y = y;
 		}
 
 		@SuppressWarnings("javadoc")
 		public int getX() {
 			return this.x;
 		}
 
 		@SuppressWarnings("javadoc")
 		public int getY() {
 			return this.y;
 		}
 
 		@SuppressWarnings("javadoc")
 		public void setX(int x) {
 			this.x = x;
 		}
 
 		@SuppressWarnings("javadoc")
 		public void setY(int y) {
 			this.y = y;
 		}
 
 		@Override
 		public boolean equals(Object o) {
 			if (o instanceof Coordinate)
 				return this.equals((Coordinate) o);
 			return false;
 		}
 
 		@Override
 		public int hashCode() {
 			return this.x * HASH_BROWNS + this.y * SALT;
 		}
 
 		/**
 		 * Checks if 2 coordinates are equal, based on their x and y values.
 		 * 
 		 * @param c
 		 * @return true if equal... duh
 		 */
 		public boolean equals(Coordinate c) {
 			return this.x == c.x && this.y == c.y;
 		}
 
 	}
 
 	/**
 	 * Class for the Mouse listener used on the game board.
 	 * 
 	 * @author sturgedl. Created Mar 25, 2013.
 	 */
 	private class updateThread extends Thread {
 		EngineInterface e;
 		SorryFrame s;
 
 		public void run() {
 			e.getUpdatedInfo();
 			// s.repaint();
 			s.initiateTurn();
 			try {
 				sleep(1000);
 			} catch (InterruptedException e1) {
 				// TODO Auto-generated catch block
 				e1.printStackTrace();
 			}
 			run();
 		}
 	}
 
 	protected class BoardMouseListener implements MouseListener {
 		private SorryFrame myFrame;
 
 		/**
 		 * Basic MouseListener constructor, takes a frame to interact with. Uses
 		 * the frame to register mouse clicks upon.
 		 * 
 		 * @param frame
 		 */
 		public BoardMouseListener(SorryFrame frame) {
 			this.myFrame = frame;
 		}
 
 		@Override
 		public void mouseClicked(MouseEvent click) {
 			Coordinate coord = null;
 			try {
 				coord = SorryFrame.convertClickToCoordinate(click.getX()
 						- FRAME_X_PAD, click.getY() - FRAME_Y_PAD);
 			} catch (CoordinateOffOfBoardException e) {
 				System.out.println("Clicked off board, probably ok.");
 			}
 			if (coord != null)
 				this.myFrame.registerMouseClick(coord);
 
 			if (this.myFrame.clickCount >= 2) {
 				// have enough clicks to make a turn
 				this.myFrame.performTurn();
 				this.myFrame.resetClickDetection();
 			}
 		}
 
 		@Override
 		public void mouseEntered(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mouseExited(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mousePressed(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 		@Override
 		public void mouseReleased(MouseEvent click) {
 			// NOT NEEDED (YET)
 
 		}
 
 	}
 }
 
