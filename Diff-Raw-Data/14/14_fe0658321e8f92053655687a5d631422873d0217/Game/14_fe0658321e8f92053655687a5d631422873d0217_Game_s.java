 package game;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 
 
 import javax.imageio.ImageIO;
 import javax.swing.*;
 
 import game.Board.Difficulty;
 
 @SuppressWarnings("serial")
 public class Game extends JFrame {
 	Difficulty gameDifficulty;
 	Board board;
 	int mineCount;
 	int time;
 	OperatingPanel operatingPanel;
 	private boolean firstTime = true;
 	public boolean gameOver;
 
 	public Game(){
 		createGUI();
 	}
 
 	private void createGUI() {
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setTitle("Minesweeper");
 
 		// Sets initial condition. By default, an easy game is generated
 		gameDifficulty = Difficulty.EASY;
 
 		// Sets up the Operating panel
 		// This Panel contains the file menu and the other controls
 		operatingPanel = new OperatingPanel();
 
 		// Creates a new board setup with that difficulty.
 		newGame(gameDifficulty);
 
 		// If this is the first time the program is running, the operating panel is created.
 		if (firstTime) {
 			add(operatingPanel, BorderLayout.NORTH);
 		}
 
 		// Makes the board non-resizeable by the user
 		setResizable(false);
 
 		// Sets the flag to signify that the first game creation is over.
 		firstTime = false;
 	}
 
 	public void newGame(Difficulty difficulty){
 
 		// If this isn't the first time through, the board is removed.
 		if (!firstTime) {
 			remove(board);
 		}
 
 		// Creates a new board based on the game difficulty that was passed in
 		gameDifficulty = difficulty;
 		board = new Board(gameDifficulty, this);
 
 		// Sets up and initializes the mineCounterField
 		mineCount = board.getMineCount();
 		operatingPanel.mineCounter.update(mineCount);
 
 		// Resets the time field
 		time = 0;
 		operatingPanel.timer.update(time);
 
 		// Sets the size of the JFrame based on the board
 		setSize(new Dimension(board.getWidth()+10, board.getHeight() + 150));
 
 		// Adds the board to the JFrame
 		add(board);
 	}
 
 	// Display panel for the game
 	class OperatingPanel extends JPanel{
 		public JButton reset;
 		public TimerCounterField timer;
 		public MineCounterField mineCounter;
 
 		OperatingPanel() {	
 
 			// Step one: Make the menubar have a File option
 			this.setLayout(new GridLayout(0,1));
 			JMenuBar menuBar = new JMenuBar();
 			JMenu fileMenu = new JMenu("File");
 
 			// Populate the File menu
 			makeFileMenu(fileMenu);
 
 			// Step two: Make the menubar have a Difficulty option
 			JMenu difficultyMenu=new JMenu("Difficulty");
 
 			// Populate the difficulty
 			makeDifficultyMenu(difficultyMenu);	
 
 			// Add the two menus to the menubar
 			menuBar.add(fileMenu);
 			menuBar.add(difficultyMenu);
 
 			// Add the menubar to the Operating Panel
 			add(menuBar);	
 
 			// Create the buttonPanel that contains the timer, minecount and the reset button
 			JPanel buttonPanel=new JPanel();
 			buildButtonPanel(buttonPanel);
 
 			// Add that to the operating panel
 			add(buttonPanel);
 		}
 
 		private void buildButtonPanel(JPanel buttonPanel) {
 
 			buttonPanel.setLayout(new GridLayout(1,5));
 
 			//make minecounter
 			mineCounter=new MineCounterField ("Mines:");
 			mineCounter.update(mineCount);
 			buttonPanel.add(mineCounter);
 
 			//make reset button
 			reset = new JButton();
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/fine.jpg"));
 				reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 			//reset.setIcon);
 			reset.setSize(new Dimension(50,50));
 			reset.addActionListener(new ResetListener());
 			buttonPanel.add(reset);
 
 			//make timer
 			timer=new TimerCounterField("Time:");
 			timer.update(time);
 			buttonPanel.add(timer);
 
 		}
 
 		private void makeFileMenu(JMenu fileMenu) {
 			JMenuItem newGame=new JMenuItem("New Game");
 			newGame.addActionListener(new ResetListener());
 			fileMenu.add(newGame);
 			JMenuItem exit=new JMenuItem("Exit");
 			exit.addActionListener(new ExitListener());
 			fileMenu.add(exit);
 
 		}
 
 		private void makeDifficultyMenu(JMenu difficultyMenu) {
 			JMenuItem easy = new JMenuItem("Easy");
 			easy.addActionListener(new DifficultySetterListenerEasy ());
 			difficultyMenu.add(easy);
 			JMenuItem medium = new JMenuItem("Medium");
 			medium.addActionListener(new DifficultySetterListenerMedium ());
 			difficultyMenu.add(medium);
 			JMenuItem hard = new JMenuItem("Hard");
 			hard.addActionListener(new DifficultySetterListenerHard ());
 			difficultyMenu.add(hard);
 
 		}
 	}
 
 	// Creates the timer field
 	private class TimerCounterField extends JTextField{
 		private JTextField timerCount;	
 
 		public TimerCounterField(String textLabel) {
 			setLayout(new GridLayout(1, 0));
 			setEditable(false);
 			add(new JLabel(textLabel));
 			timerCount = new JTextField(10);
 			timerCount.setEditable(false);
 			add(timerCount);
 		}
 
 		// Allows the timer to be updated
 		public void update(int time){
 			timerCount.setText(String.valueOf(time));
 		}
 	}
 	public void incrementMineCount() {
		operatingPanel.mineCounter.update(++mineCount);
 	}
 
 	public void decrementMineCount() {
 		operatingPanel.mineCounter.update(--mineCount);
 	}
 
 	// Creates the mine counter field
 	private class MineCounterField extends JTextField{
 		public JTextField minecount;	
 
 		public MineCounterField(String textLabel) {
 			setLayout(new GridLayout(1, 0));
 			setEditable(false);
 			add(new JLabel(textLabel));
 			minecount = new JTextField(10);
 			minecount.setEditable(false);
 			add(minecount);
 		}
 
 		// Allows the counter to be updated
 		public void update(int mine){
 			minecount.setText(String.valueOf(mine));
 		}
 	}
 
 	// Creates a new game of the current difficulty
 	// This is called by both the New Game menu option and the reset button
 	private class ResetListener implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			resetGame();
 		}
 	}
 
 	protected void resetGame() {
 		gameOver = false;
 		// Due to how the Board's are constructing themselves, this is the best solution.
 		Difficulty temp = gameDifficulty;
 		if (temp.equals(Difficulty.EASY)) {
 			newGame(Difficulty.MEDIUM);
 			newGame(temp);
 		} else {
 			newGame(Difficulty.EASY);
 			newGame(temp);
 		}
 	}
 	// Listeners designed to create boards of the specified difficulty
 	private class DifficultySetterListenerEasy implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			newGame(Difficulty.EASY);
 		}
 	}
 	private class DifficultySetterListenerMedium implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			newGame(Difficulty.MEDIUM);
 		}
 	}
 	private class DifficultySetterListenerHard implements ActionListener {
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			newGame(Difficulty.HARD);
 		}
 	}
 	// Listener for the exit button
 	private class ExitListener implements ActionListener{
 
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			System.exit(0);
 		}
 
 	}
 	public void holdingMouse() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/holdingMouse.jpg"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 	}
 
 	public void fine() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/fine.jpg"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 	}
 
 	public void gameOver() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/lose.jpg"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 		gameOver = true;
 	}
 }
 
