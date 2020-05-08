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
 	protected Difficulty gameDifficulty;
 	protected Board board;
 	protected int mineCount;
 	protected int tempMineCount;
 	private final int UPDATE_TIME_PANEL_DELAY=1000;
 	protected OperatingPanel operatingPanel;
 	protected Timer timer;
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
 		
 		// If this isn't the first time through, the board is removed and operating panel are removed
 		if (!firstTime) {
 			remove(board);
 			timer.stop();
 			operatingPanel.timer.time = 0;
 		}
 			
 		fine();
 		
 		// Creates a new board based on the game difficulty that was passed in
 		gameDifficulty = difficulty;
 		board = new Board(gameDifficulty, this);
 
 		// Sets up and initializes the mineCounterField
 		mineCount = board.getMineCount();
 		tempMineCount = mineCount;
 		operatingPanel.mineCounter.update(mineCount);
 		
 		// Creates a new timer, the timepanel is also an ActionListener.
 		// The timer is started in the board, which detects the first click.
 		timer = new Timer(UPDATE_TIME_PANEL_DELAY, operatingPanel.timer);
 		
 		
 		// Sets the size of the JFrame based on the board
		setSize(new Dimension(board.getWidth()+6, board.getHeight() + 148));
 
 		// Adds the board to the JFrame
 		add(board, BorderLayout.CENTER);
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
 				Image img = ImageIO.read(getClass().getResource("/image/faces/fine.png"));
 				reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 			//reset.setIcon);
 			reset.setSize(new Dimension(50,50));
 			reset.addActionListener(new ResetListener());
 			buttonPanel.add(reset);
 
 			//make timer
 			timer=new TimerCounterField("Time:");
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
 	private class TimerCounterField extends JTextField implements ActionListener {
 		private JTextField timerCount;	
 		private long time;
 		private final long TIME_LIMIT = 999;
 		public TimerCounterField(String textLabel) {
 			setLayout(new GridLayout(1, 0));
 			setEditable(false);
 			add(new JLabel(textLabel));
 			timerCount = new JTextField(10);
 			timerCount.setEditable(false);
 			timerCount.setText(String.valueOf(0));
 			add(timerCount);
 			time=0;
 		}
 
 		@Override
 		public void actionPerformed(ActionEvent e) {
 			if(time < TIME_LIMIT)
 				time++;
 			timerCount.setText(String.valueOf(time));
 		}
 	}
 	public void incrementMineCount() {
 		++tempMineCount;
 		if (tempMineCount > 0) {
 			operatingPanel.mineCounter.update(++mineCount);
 		}
 	}
 
 	public void decrementMineCount() {
 		--tempMineCount;
 		if (tempMineCount < 0) {
 			return;
 		}
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
 		fine();
 		newGame(gameDifficulty);
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
 	
 	// These functions here change the face on the reset button
 	public void holdingMouse() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/holdingMouse.png"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 	}
 
 	public void fine() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/fine.png"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 	}
 
 	public void gameOver() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/lose.png"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 		gameOver = true;
 	}
 
 	public void win() {
 		if(!gameOver){
 			try {
 				Image img = ImageIO.read(getClass().getResource("/image/faces/win.png"));
 				operatingPanel.reset.setIcon(new ImageIcon(img));
 			} catch (IOException ex) {
 			}
 		}
 	}
 }
 
