 import java.awt.*;
 import java.awt.event.*;
 import java.io.IOException;
 import java.net.URL;
 import java.util.Timer;
 import java.util.TimerTask;
 
 import javax.swing.*;
 
 /**
  * The GameInterface class creates a GUI to represent a sudoku board and 
  * allows the user to interact with the board.
  * @author Aaron Balsara, Nicholas Figueira, David Loyzaga
  *
  */
 public class GameInterface implements FocusListener {
 	/**
 	 * The board representing the current sudoku game.
 	 */
 	private Board currentGame;
 	/**
 	 * An array of text fields used to represent the sudoku board.
 	 */
 	private JTextField[][] sudokuBoard;
 	/**
 	 * The game player class which is used to interface with the board.
 	 */
 	private GamePlayer gamePlayer;
 	/**
 	 * A label to provide feedback to the player.
 	 */
 	private final JLabel statusIndicator;
 	/**
 	 * The difficulty of the current game.
 	 */
 	private int difficulty;
 	/**
 	 * The text field that most recently had the focus.
 	 */
 	private JTextField lastSelected;
 	/**
 	 * The clock used to measure the length of sudoku games.
 	 */
 	private Timer clock;
 	/**
 	 * The button pressed to give a hint.
 	 */
 	private JButton hintButton;
 	/**
 	 * A variable that keeps track of whether the help frame is open or closed
 	 */
 	private boolean isHelpOpen = false;
 	/**
 	 * The button to be pressed to solve a sudoku board.
 	 */
 	private JButton solveButton;
 	/**
 	 * The button used to reset the board to it's intial state.
 	 */
 	private JButton resetButton;
 	
 	private final static String MENU_TIP = "Returns to the menu selection window";
 	private final static String NEW_GAME_TIP = "Starts a new game in the current difficulty";
 	private final static String HINT_TIP = "Gives a random number in the sudoku";
 	private final static String RESET_TIP = "Resets the sudoku to the orginal numbers";
 	private final static String SOLVE_TIP = "Solves the sudoku puzzle";
 	private final static String HELP_TIP = "Launches the help dialog that provides instructions on how to play sudoku";
 	private final static String EASY_TIP = "Creates a new game with easy difficulty";
 	private final static String MEDIUM_TIP = "Creates a new game with medium difficulty";
 	private final static String HARD_TIP = "Creates a new game with hard difficulty";
 	private final static String SOLVER_TIP = "Creates a solver mode where you provide your own sudoku and the program gives you a solution";
 	
 	private final static int SOLVER_MODE = 3;
 	
 	private final static Color YELLOW = new Color (254, 255, 210);
 	private final static Color GREEN = new Color (200, 255, 200);
 	private final static Color RED = new Color (255, 210, 210);
 	private final static Color GREY = new Color (220, 220, 220);
 	
 	/**
 	 * The constructor for GameInterface requires a GamePlayer to be made.
 	 * @param gamePlayer	The gamePlayer that will receive input from this
 	 * class.
 	 */
 	public GameInterface (GamePlayer gamePlayer) {
 
 		sudokuBoard = new JTextField[9][9];
 		this.gamePlayer = gamePlayer;
 		difficulty = -1;
 		statusIndicator = new JLabel(" ");
 
 		KeyboardFocusManager.getCurrentKeyboardFocusManager()
 		.addKeyEventDispatcher(new KeyEventDispatcher() {
 			@Override
 			public boolean dispatchKeyEvent(KeyEvent e) {
 				if (e.getID() == KeyEvent.KEY_TYPED) {
 					if (e.getKeyChar() == 'h') {
 						if(!(difficulty == SOLVER_MODE)) {
 							hint ();
 						}
 					}
 				}
 				return false;
 			}
 		});
 		menuSelector ();
 	}
 
 	/**
 	 * Makes the Sudoku board using 9 3x3 grids within each 3x3 grid
 	 * @param difficulty	The selected difficulty of the game
 	 */
 	private void makeSudokuBoard (String difficulty) {
 
 		JFrame frame = new JFrame();
 		if (difficulty == null) {
			frame.setTitle("Sudoku Solver");
 		} else {
 			frame.setTitle("Difficulty: " + difficulty);	
 		}
 
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		frame.setResizable(false);
 		
 		JPanel sudokuPanel = make3x3Grid();
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				JPanel smallPanel = make3x3Grid();
 				if (difficulty != null) {
 					makeSudokuCell (smallPanel, i, j);	
 				} else {
 					currentGame = new Board (Board.EMPTYBOARD);
 					makeSudokuCell (smallPanel, i, j);
 				}
 				sudokuPanel.add(smallPanel);
 			}
 		}
 		updateBoard();
 		sudokuPanel.setBorder(BorderFactory.createMatteBorder(4,4,4,4, Color.BLACK));
 
 		for (int i = 0; i < 81; i++) {
 			int row = i % 9;
 			int col = i / 9;
 			JTextField field = sudokuBoard[row][col];
 			field.addFocusListener(this);
 		}
 
 		frame.add(sudokuPanel, BorderLayout.CENTER);
 
 		if (difficulty != null) {
 			frame.add(makeSideButtons(frame), BorderLayout.EAST);	
 		} else {
 			frame.add(makeSolverSideButtons(frame), BorderLayout.EAST);
 		}
 
 		frame.pack();
 		frame.setSize(761, 654);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 
 	/**
 	 * Used for solver mode, creates a sudoku board with no cells filled in.
 	 */
 	private void makeEmptyBoard () {
 		makeSudokuBoard(null);
 	}
 
 	/**
 	 * Creates a cell in the Sudoku Board
 	 * @param panel 		The 3x3 grid
 	 * @param startRow 		The row in which the 3x3 grid is in
 	 * @param startColumn 	The column in which the 3x3 grid is in
 	 */
 	private void makeSudokuCell (JPanel panel, int startRow, int startColumn) {
 		for (int i = 0; i < 3; i++) {
 			for (int j = 0; j < 3; j++) {
 				final int row = startRow * 3 + i;
 				final int column = startColumn * 3 + j;
 				final String value = String.valueOf(currentGame.getCellValue(row, column));
 				JTextField field = new JTextField();
 
 				Font font = new Font("Arial", Font.PLAIN, 30);
 				field.setFont(font);
 				field.setHorizontalAlignment(JLabel.CENTER);
 				field.setBorder(BorderFactory.createLineBorder(Color.black));
 				if (!value.equals("0")) {
 					field.setEditable(false);
 					field.setBackground(GREY);
 					field.setFocusable(false);
 				}
 
 				field.addActionListener(new ActionListener() {
 					@Override
 					public void actionPerformed(ActionEvent event) {
 						JTextField me = (JTextField) event.getSource();
 						editTextField(me);
 						
 						int row = getFieldCoordinates(me);
 						int col = row % 10;
 						row = row / 10;
 						boolean done = false;
 						for (int i = row; i < 9 && !done; i++) {
 							for (int j = col; j < 9 && !done; j++) {
 								if (sudokuBoard[i][j].getText().equals("")) {
 									sudokuBoard[i][j].requestFocus();
 									done = true;
 								}
 							}
 						}
 					}
 				});
 				
 				sudokuBoard[row][column] = field;
 				panel.add(field);
 			}
 		}
 	}
 
 
 
 	/**
 	 * Updates the board based on the changes in this text field. 
 	 * @param me	The text field that has the recorded changes which needs to be added to the board.
 	 */
 	private void editTextField (JTextField me) {
 		if (me.getBackground().equals(GREEN)) {
 			me.setBackground(Color.WHITE);	
 		}
 		
 		int row = getFieldCoordinates(me);
 		int col = row % 10;
 		row = row / 10;
 		String text = me.getText();
 
 		if (text.equals("")) {
 			gamePlayer.clearCell(row, col);
 			return;
 		}
 		if (text.length() > 1) {
 			updateBoard();
 			text = me.getText();
 			if (text.equals("")) {
 				gamePlayer.clearCell(row, col);
 				return;
 			}
 		}
 
 		char input = text.charAt(0);
 		if (!Character.isDigit(input) || input == '0') {
 			text = "";
 			me.setText(text);
 			return;
 		}
 
 		int num = Character.getNumericValue(input);
 		if (currentGame.getSet(row, col)) {
 			if (currentGame.getCellValue(row, col) == num) {
 				return;
 			} else {
 				gamePlayer.clearCell(row, col);
 				System.out.println("Right place");
 				if (!gamePlayer.assign(row, col, num)) {
 					wrongNumber(row, col);
 					gamePlayer.clearCell(row, col);
 					updateBoard();
 					return;
 				}
 			}
 		} else {
 			if (!gamePlayer.assign(row, col, num)) {
 				wrongNumber(row, col);
 				updateBoard();
 				return;
 			}
 		}
 		updateBoard();
 	}
 	
 	/**
 	 * If the user inputs a number that violates the constraints of sudoku,
 	 * this method handles that by clearing the cell and flashing it red.
 	 * @param row	The row of the cell.
 	 * @param col	The column of the cell.
 	 */
 	private void wrongNumber (int row, int col) {
 		sudokuBoard[row][col].setBackground(RED);
		updateStatus("Invalid number");
 		final int myRow = row;
 		final int myCol = col;
 		TimerTask task = new TimerTask() {
 			public void run() {
 				if (sudokuBoard[myRow][myCol].getBackground().equals(RED)) {
 					if (sudokuBoard[myRow][myCol].hasFocus()) {
 						sudokuBoard[myRow][myCol].setBackground(GREEN);
 					} else {
 						sudokuBoard[myRow][myCol].setBackground(Color.WHITE);
 					}
 				}
 			}
 		};
 		clock.schedule(task, 1500);
 	}
 	
 	/**
 	 * The actions to be performed in the event an object gains focus.
 	 */
 	public void focusGained(FocusEvent e) {
 		if (e.getComponent() instanceof JTextField) {
 			JTextField eventTrigger = (JTextField) e.getComponent();
 			if (eventTrigger.getBackground().equals(Color.WHITE)) {
 				eventTrigger.setBackground(GREEN);	
 			}
 			lastSelected = eventTrigger;
 		}
 	}
 	
 	/**
 	 * The actions to be performed in the event an object loses focus.
 	 */
 	public void focusLost(FocusEvent e) {
 		JTextField eventTrigger = (JTextField) e.getComponent();
 		editTextField(eventTrigger);
 	}
 
 	/**
 	 * Creates a small 3x3 grid and places it on a panel.
 	 * @return JPanel	A JPanel containing the 3x3 grid.
 	 */
 	private JPanel make3x3Grid () {
 		final GridLayout gridLayout = new GridLayout(3, 3);
 		JPanel panel = new JPanel(gridLayout);
 		panel.setBorder(BorderFactory.createLineBorder(Color.black));
 		return panel;
 	}
 	
 	/**
 	 * Makes the buttons for the different sudoku difficulties and
 	 * places them on a panel
 	 * @param frame	The frame that will be using the buttons.
 	 * @return		The JPanel with the components on it.
 	 */
 	private JPanel makeSideButtons (final JFrame frame) {
 		JPanel sideButtons = new JPanel();
 		sideButtons.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		sideButtons.setPreferredSize(new Dimension(120, 600));
 		
 		JButton menuButton = initButton("Menu", MENU_TIP);
 		menuButton.setMnemonic(KeyEvent.VK_M);
 		menuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				updateStatus(" ");
 				menuSelector();
 			}
 		});
 
 		JButton newGameButton = initButton("New Game", NEW_GAME_TIP);
 		newGameButton.setMnemonic(KeyEvent.VK_N);
 		newGameButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				newGame();
 			}
 		});
 		hintButton = initButton("Hint", HINT_TIP);
 		if (difficulty == BoardGenerator.EASY) {
 			hintButton.setText("Hint");
 		} else {
 			hintButton.setText("Hint: " + gamePlayer.hintsLeft());	
 		}
 		hintButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				hint ();
 			}
 		});
 		resetButton = initButton("Reset", RESET_TIP);
 		resetButton.setMnemonic(KeyEvent.VK_R);
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				resetGame();			
 			}
 		});
 		solveButton = initButton("Solve", SOLVE_TIP);
 		solveButton.setMnemonic(KeyEvent.VK_S);
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				solveCurrentBoard();
 				resetButton.setEnabled(false);
 				solveButton.setEnabled(false);
 			}
 		});
 		
 		final JLabel timerLabel = new JLabel("");
 		timerLabel.setHorizontalAlignment(JLabel.CENTER);
 		timerLabel.setPreferredSize(new Dimension(110, 20));
 		clock = new Timer();
 		TimerTask task = new TimerTask() {
 			int seconds = 0;
 			int mins = 0;
 			int hours = 0;
 			public void run() {
 				seconds++;
 				if (seconds >= 60) {
 					seconds = 0;
 					mins++;
 				}
 				if (mins >= 60) {
 					mins = 0;
 					hours++;
 				}
 				String timerText = "Time: ";
 				if (hours > 0) {
 					timerText = timerText.concat(hours + ":");
 				}
 				if (mins < 10 && hours > 0) {
 					timerText = timerText.concat("0");
 				}
 				timerText = timerText.concat(mins + ":");
 				if (seconds < 10) {
 					timerText = timerText.concat("0");
 				}
 				timerText = timerText.concat(String.valueOf(seconds));
 				
 				timerLabel.setText(timerText);
 	        }
 		};
 		clock.schedule(task, 0, 1000);
 
 		JButton helpButton = initButton("Help", HELP_TIP);
 		helpButton.setMnemonic(KeyEvent.VK_F1);
 		helpButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				helpAction ();
 			}
 		});
 
 		JLabel blankLabel = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 1;
 		sideButtons.add(menuButton, c);
 		c.weighty = 0.25;
 		c.gridy = 2;
 		sideButtons.add(statusIndicator, c);
 		c.gridy = 3;
 		sideButtons.add(newGameButton, c);
 		c.gridy = 4;
 		sideButtons.add(hintButton, c);
 		c.gridy = 5;
 		sideButtons.add(resetButton, c);
 		c.gridy = 6;
 		sideButtons.add(solveButton, c);
 		c.gridy = 7;
 		sideButtons.add(timerLabel, c);
 		c.gridy = 8;
 		sideButtons.add(helpButton, c);
 		c.weighty = 1;
 		c.gridy = 9;
 		sideButtons.add(blankLabel, c);
 		return sideButtons;
 	}
 	
 	/**
 	 * Starts a new game of sudoku with the current difficulty.
 	 */
 	private void newGame () {
 		updateStatus(" ");
 		gamePlayer.newGame(difficulty);
 		if (difficulty == BoardGenerator.EASY){
 			makeSudokuBoard ("Easy");	
 		} else if (difficulty == BoardGenerator.MEDIUM) {
 			makeSudokuBoard("Medium");
 		} else {
 			makeSudokuBoard("Hard");
 		}
 	}
 	
 	/**
 	 * Resets the current game of sudoku to it's inital state.
 	 */
 	private void resetGame () {
 		gamePlayer.resetGame();
 		updateStatus("Board reset");
 		updateBoard();
 		if (difficulty == BoardGenerator.EASY) {
 			hintButton.setText("Hint");
 		} else {
 			hintButton.setText("Hint: " + gamePlayer.hintsLeft());	
 		}
 	}
 	
 	/**
 	 * Solves the current game of sudoku for the player. Marks the cells they
 	 * had not filled in correctly.
 	 */
 	private void solveCurrentBoard () {
 		Board current = currentGame.clone();
 		Board solution = gamePlayer.getSolution();
 		updateStatus("Board solved");
 		setBoard(solution);
 		updateBoard();
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				if (current.getCellValue(i, j) != solution.getCellValue(i, j)) {
 					if (sudokuBoard[i][j].isEditable()) {
 						sudokuBoard[i][j].setBackground(RED);
 					}
 				}
 			}
 		}
 		stopTimer();
 	}
 	
 	/**
 	 * Permanently reveals a cell to the player and paints it yellow.
 	 */
 	public void hint () {
 		if (lastSelected == null) {
 			return;
 		}
 		if (!lastSelected.getText().equals("")) {
 			return;
 		}
 		int row = getFieldCoordinates(lastSelected);
 		int col = row % 10;
 		row = row / 10;
 		if (gamePlayer.hint(row, col)) {
 			sudokuBoard[row][col].setBackground(YELLOW);
 			sudokuBoard[row][col].setEditable(false);
 		}
 		updateBoard();
 		if (gamePlayer.hintsLeft() == 0) {
 			hintButton.setEnabled(false);
 		}
 		if (difficulty == BoardGenerator.EASY) {
 			hintButton.setText("Hint");
 		} else {
 			hintButton.setText("Hint: " + gamePlayer.hintsLeft());	
 		}
 	}
 	
 	/**
 	 * Stops the game timer. Activated when the sudoku game is complete.
 	 */
 	public void stopTimer () {
 		if (clock != null) {
 			clock.cancel();	
 		}
 	}
 
 	/**
 	 * Creates the side buttons for solver mode and places them on a panel.
 	 * @param frame	The frame that will be using the buttons.
 	 * @return		The JPanel with the components on it.
 	 */
 	private JPanel makeSolverSideButtons (final JFrame frame) {
 		JPanel sideButtons = new JPanel();
 		sideButtons.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		sideButtons.setPreferredSize(new Dimension(120, 600));
 
 		JButton menuButton = initButton("Menu", MENU_TIP );
 		menuButton.setMnemonic(KeyEvent.VK_M);
 		menuButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				updateStatus(" ");
 				menuSelector();
 			}
 		});
 		resetButton = initButton("Reset", RESET_TIP);
 		resetButton.setMnemonic(KeyEvent.VK_R);
 		resetButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.solverMode();
 				updateStatus("Board reset");
 				updateBoard();
 				sudokuBoard[0][0].requestFocus();
 			}
 		});
 		solveButton = initButton("Solve", SOLVE_TIP);
 		solveButton.setMnemonic(KeyEvent.VK_S);
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				gamePlayer.solveBoard();
 				updateStatus("Board solved");
 				updateBoard();
 			}
 		});
 		JButton helpButton = initButton("Help", HELP_TIP);
 		helpButton.setMnemonic(KeyEvent.VK_F1);
 		helpButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				helpAction ();
 			}
 		});
 		JLabel blankLabel = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 1;
 		sideButtons.add(menuButton, c);
 		c.weighty = 0.25;
 		c.gridy = 2;
 		sideButtons.add(statusIndicator, c);
 		c.gridy = 3;
 		sideButtons.add(resetButton, c);
 		c.gridy = 4;
 		sideButtons.add(solveButton, c);
 		c.gridy = 5;
 		sideButtons.add(helpButton, c);
 		c.weighty = 1;
 		c.gridy = 6;
 		sideButtons.add(blankLabel, c);
 		return sideButtons;
 	}
 
 	/**
 	 * Shows a help frame to help the user play sudoku
 	 * and use the interface.
 	 */
 	private void helpAction () {
 		final JFrame frame = new JFrame();
 		frame.addWindowListener(new WindowAdapter() {
 			public void windowClosing(WindowEvent e) {
 				isHelpOpen = false;
 		        frame.dispose();
 			}
 		});
 		URL helpURL = GameInterface.class.getResource("Help.html");
 		JEditorPane editorPane = new JEditorPane();
 		editorPane.setEditable(false);
 		try {
 			editorPane.setPage(helpURL);
 		} catch (IOException e) {
 			System.err.println("Attempted to read a bad URL: " + helpURL);
 		}
 		JScrollPane editorScrollPane = new JScrollPane(editorPane);
 		editorScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 		frame.add(editorScrollPane);
 		frame.setTitle("Sudoku Help");
 		frame.pack();
 		frame.setSize(400, 400);
 		frame.setLocationRelativeTo(null);
 		if (isHelpOpen == false) {
 			isHelpOpen = true;
 			frame.setVisible(true);
 		} else {
 			JOptionPane.showMessageDialog(null, "This window is already open");
 		}
 	}
 
 	/**
 	 * Pops up the initial window that asks the user to select a
 	 * difficulty. It then closes and runs the game with the
 	 * selected difficulty.
 	 */
 	public void menuSelector () {
 		final JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 		JLabel instructions = new JLabel("Select a mode:", JLabel.CENTER);
 		instructions.setPreferredSize(new Dimension(110, 20));
 		
 		JButton easy = initButton("Easy", EASY_TIP);
 		easy.setMnemonic(KeyEvent.VK_E);
 		easy.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.EASY);
 				difficulty = BoardGenerator.EASY;
 				makeSudokuBoard ("Easy");
 			}
 		});
 		JButton medium = initButton("Medium", MEDIUM_TIP);
 		medium.setMnemonic(KeyEvent.VK_M);
 		medium.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.MEDIUM);
 				difficulty = BoardGenerator.MEDIUM;
 				makeSudokuBoard ("Medium");
 			}
 		});
 		JButton hard = initButton("Hard", HARD_TIP);
 		hard.setMnemonic(KeyEvent.VK_H);
 		hard.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				gamePlayer.newGame(BoardGenerator.HARD);
 				difficulty = BoardGenerator.HARD;
 				makeSudokuBoard ("Hard");
 			}
 		});
 		solveButton = initButton("Solver", SOLVER_TIP);
 		solveButton.setMnemonic(KeyEvent.VK_S);
 		solveButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				frame.dispose();
 				difficulty = SOLVER_MODE;
				clock = new Timer();
 				makeEmptyBoard ();
 				gamePlayer.solverMode();
 			}
 		});
 		
 		JPanel difficulties = new JPanel (new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		JLabel blankLabel = new JLabel(" ");
 		c.weighty = 1;
 		c.gridy = 1;
 		difficulties.add(instructions, c);
 		c.weighty = 0.25;
 		c.gridy = 2;
 		difficulties.add(easy, c);
 		c.gridy = 3;
 		difficulties.add(medium, c);
 		c.gridy = 4;
 		difficulties.add(hard, c);
 		c.gridy = 5;
 		difficulties.add(solveButton, c);
 		c.weighty = 1;
 		c.gridy = 6;
 		difficulties.add(blankLabel, c);
 
 		frame.add(difficulties);
 		frame.pack();
 		frame.setSize(150, 250);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Creates a JButton with a given name and roll over tool tip
 	 * @param buttonName 	The String the JButton is called
 	 * @param toolTip 		The String of the tool tip
 	 * @return 				The JButton with all the properties
 	 */
 	private JButton initButton (String buttonName, String toolTip) {
 		JButton button = new JButton (buttonName);
 		button.setToolTipText(toolTip);
 		button.setHorizontalAlignment(JButton.CENTER);
 		button.setPreferredSize(new Dimension(110, 20));
 		return button;
 	}
 	
 	/**
 	 * Shows a window letting the user they have won the game.
 	 */
 	private void gameWon () {
		

 		final JFrame frame = new JFrame();
 		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
         JLabel congratsLabel = new JLabel("Congratulations!", JLabel.CENTER);
 		JLabel wonLabel = new JLabel("You won", JLabel.CENTER);
         JLabel wonImage = new JLabel(new ImageIcon("win.gif"), SwingConstants.CENTER);
         // http://www.netanimations.net/Animated-fireworks-changing-colors.gif
         JPanel panel = new JPanel (new GridBagLayout());
         GridBagConstraints c = new GridBagConstraints();
         c.weighty = 0.25;
         c.gridy = 1;
         panel.add(congratsLabel, c);
         c.weighty = 1;
         c.gridy = 2;
         panel.add(wonImage, c);
         c.weighty = 0.25;
         c.gridy = 3;
         panel.add(wonLabel, c);
         panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
         frame.add(panel);
 		frame.pack();
 		frame.setSize(300, 250);
 		frame.setLocationRelativeTo(null);
 		frame.setVisible(true);
 	}
 	
 	/**
 	 * Updates the GUI to display the board's new values.
 	 */
 	public void updateBoard () {
 		for (int i = 0; i < 9; i++) {
 			for (int j = 0; j < 9; j++) {
 				JTextField field = sudokuBoard[i][j];
 				int num = currentGame.getCellValue(i, j);
 				if (num == 0) {
 					if (field.isEditable()) {
 						field.setText("");	
 					}
 				} else {
 					field.setText(String.valueOf(num));
 				}
 			}
 		}
 		if (currentGame.isComplete()) {
 			stopTimer();
 			if (difficulty != SOLVER_MODE) {
 				solveButton.setEnabled(false);
 				hintButton.setEnabled(false);
 				gameWon();
				for (int i = 0; i < Board.NUMROWS; i++) {
					for (int j = 0; j < Board.NUMCOLS; j++) {
						sudokuBoard[i][j].setFocusable(false);
					}
				}
 			}
 		}

 	}
 
 	/**
 	 * Updates the value of the label on the GUI. Used to give feedback.
 	 * @param newStatus	The message to be displayed by the label.
 	 */
 	public void updateStatus (String newStatus) {
 		statusIndicator.setText(newStatus);
 		Timer timer = new Timer();
 		timer.schedule(new TimerTask() {
 			@Override
 			public void run() {
 				statusIndicator.setText(" ");
 			}
 		}, 2000); // Timer for 2 secs. Is a little buggy when you spam click a number.
 	}
 	
 	private int getFieldCoordinates (JTextField field) {
 		int row = -1;
 		int col = -1;
 		boolean found = false;
 		for (int i = 0; i < 9 && !found; i++) {
 			for (int j = 0; j < 9 && !found; j++) {
 				if (sudokuBoard[i][j].equals(field)) {
 					row = i;
 					col = j;
 					found = true;
 				}
 			}
 		}
 		if (found) {
 			return 10 * row + col;
 		} else {
 			return -1;
 		}
 	}
 
 	/**
 	 * Sets the current board to be displayed by the interface to the given one.
 	 * @param board	The board to be displayed.
 	 */
 	public void setBoard (Board board) {
 		currentGame = board;
 	}
 }
