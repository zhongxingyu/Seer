 import java.awt.*;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.util.Random;
 
 import javax.swing.BorderFactory;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 
 /**
  * The board gui that contains the sudoku
  * It contains all 81 cells and buttons from 1-9 to 
  * be used as input for the cells
  * Buttons to prevent users from entering invalid inputs
  *
  */
 public class BoardGUI extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	Cell[][] board;
 	private Cell nothingCell;
 	private Cell chosenCell;
 	private SudokuGenerator generator;
 	private SudokuBoard sudokuBoard;
 	private SudokuBoard solution;
 	private int difficulty;
 
 	public BoardGUI(int difficulty) {
 		board = new Cell[9][9];
 		nothingCell = new Cell(-1, -1);
 		chosenCell = nothingCell;
 		this.difficulty = difficulty;
 		generator = new SudokuGenerator(difficulty);
 		generator.GenerateSolvableSudoku();
 		sudokuBoard = generator.getBoard();
 		solution = generator.solution;
 		sudokuBoard.printBoard();
 		System.out.print("\n");
 
 		JPanel squares = createSquares();	
 		add(squares);
 		JPanel buttons = createNumButtons();
 		add(buttons);	
 
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
 		populateBoard(sudokuBoard);
 		setVisible(true);
 	}
 
 	public BoardGUI(int[][] boardArray, int difficulty, int[][] editableArray) {
 		board = new Cell[9][9];
 		nothingCell = new Cell(-1, -1);
 		chosenCell = nothingCell;
 		this.difficulty = difficulty;
 		generator = null;
 		sudokuBoard = new SudokuBoard(boardArray);
 		sudokuBoard.printBoard();
 		SudokuSolver solver = new SudokuSolver(sudokuBoard);
 		solver.runSolve();
 		solution = solver.getSolution();
 		System.out.print("\n");
 
 		JPanel squares = createSquares();	
 		add(squares);
 		JPanel buttons = createNumButtons();
 		add(buttons);	
 
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
 		populateBoard(sudokuBoard);
 		for (int row = 8 ; row >= 0; row--) {
 			for (int col = 0; col < 9; col++) {
 				boolean status;
 				if (editableArray[col][row]==1) {
 					status = true;
 					if (!sudokuBoard.numberIsLegal(row, col, 
 							  boardArray[col][row])) {
 						board[col][row].setForeground(Color.RED);
 						board[col][row].setBackground(Color.WHITE);
 					}
 				} else {
 					status = false ;
 				}
 				board[col][row].setEditable(status);
 			}
 		}
 		setVisible(true);
 	}
 
 	public BoardGUI(SudokuBoard solution) {
 		board = new Cell[9][9];
 		nothingCell = new Cell(-1, -1);
 		chosenCell = nothingCell;
 		sudokuBoard = solution;
 		sudokuBoard.printBoard();
 		System.out.print("\n");
 		JPanel squares = createSquares();		
 		add(squares);	
 		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
 		Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
 		setLocation((d.width / 2 - 175), (d.height / 2 - 275));
 		populateBoard(sudokuBoard);
 		setVisible(true);
 	}
 
 	/**
 	 * Creates all 81 cells. Uses a 2dim array to store all cells
 	 * We have two 2dim arrays. One to display the numbers
 	 * and one to store the sudoku.
 	 * @return
 	 */
 	public JPanel createSquares() {
 		JPanel squares = new JPanel();
 		squares.setLayout(new GridLayout(9, 9, 1, 1));
     	squares.setBackground(new Color(255, 231, 186));
 
 		for(int row = 8 ; row >= 0; row--) {
 			for(int col = 0; col < 9; col++) {
 				board[col][row] = new Cell(col, row);
 				squares.add(board[col][row]);
 				board[col][row].addMouseListener(new MouseListener() {
 
 					@Override
 					public void mouseClicked(MouseEvent e) {
 					}
 
 					@Override
 					public void mouseEntered(MouseEvent e) {
 					}
 
 					@Override
 					public void mouseExited(MouseEvent e) {
 					}
 
 					@Override
 					public void mousePressed(MouseEvent e) {
 						//Highlight cell chosen
 						//Set past chosenCell back to white
 						chosenCell.setBackground(Color.WHITE);
 						chosenCell = (Cell) e.getSource();
 						chosenCell.setBackground(Color.CYAN);
 					}
 
 					@Override
 					public void mouseReleased(MouseEvent e) {
 					}
 
 				});
 			}
 		}
 
 		return squares;
 	}
 
 	/**
 	 * Creates buttons from 1-9 to be used as input from the user
 	 * Puts the buttons on the bottom of the board.
 	 * @return
 	 */
 	public JPanel createNumButtons() {
 		JPanel numbers = new JPanel();
 		numbers.setLayout(new GridLayout(1, 1, 3, 1));
 		numbers.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
     	numbers.setBackground(new Color(255, 231, 186));
 
 		String keyLabels = "123456789";
 	    for (int i = 0; i < keyLabels.length(); i++) {
 	    	final String label = keyLabels.substring(i, i + 1);
 	        final JButton keyButton = new JButton(label);
 	        keyButton.setFont(new Font("Arial", Font.BOLD, 15));
 	        numbers.add(keyButton);
 	        keyButton.addActionListener(new ActionListener()
 	        {
 	        	public void actionPerformed(ActionEvent event) {
 	        		if(chosenCell.getEditable()) {
 	        			chosenCell.setFont(new Font("Lucida Console", Font.BOLD, 18));
 	        			//display the number
 	        			chosenCell.setText(keyButton.getText());
 	        			if(chosenCell != nothingCell) {
 	        				//Only change colour for some difficulties
 	        				if(difficulty == 1 || difficulty == 2) {
 	        					//check box, column and row, if it is legal. If it isnt colour it red, if not black
 	        					if (!sudokuBoard.numberIsLegal(chosenCell.getRow(), chosenCell.getCol(), 
 	        												  Integer.parseInt(keyButton.getText()))) {
 	        						chosenCell.setForeground(Color.RED);
 	        						chosenCell.setBackground(Color.WHITE);
 	        					} else {
 	        						chosenCell.setForeground(Color.BLACK);
 	        						chosenCell.setBackground(Color.WHITE);
 	        					}
 	        				}
 	    					//set the number in the sudukoBoard as well
 	        				sudokuBoard.setNumber(chosenCell.getCol(), chosenCell.getRow(), Integer.parseInt(keyButton.getText()));
 							chosenCell.setBackground(Color.WHITE);
 	        			}
 
         				chosenCell = nothingCell;
 	        		}
 	            }
 	        });
 	    }
 
 		return numbers;
 	}
 
 	/**
 	 * Delete a cell that can be edited
 	 * meaning we can delete a hint or a cell in the initial problem
 	 */
 	public void deleteCell() {
 		if(chosenCell.getEditable()) {
 		   chosenCell.setBackground(Color.WHITE);
 		   chosenCell.setText("");
 		   chosenCell.setEditable(true);
 		   chosenCell = nothingCell;
 		}
 	}
 
 	/**
 	 * Populate the board with the generated board
 	 * and display the board
 	 * @param sudoku
 	 */
 	public void populateBoard (SudokuBoard sudoku) {
 		for (int col = 0; col<9; col++) {
 			for (int row = 0; row<9; row++){
 				//want to out
 				String number = Integer.toString(sudoku.getNumber(col, row));
 				if(!number.equals("0")) {
 					board[col][row].setText(number);
 					board[col][row].setEditable(false);
 				}
 			}
 		}
 	}
 
 	public SudokuBoard getSolution () {
 		return solution;
 	}
 
 	/**
 	 * Check if the board is solved.
 	 * @return
 	 */
 	public boolean checkSolution() {
 		SudokuBoard solutions = solution;
 
 		for(int i = 0; i < 9; i++) {
 			for(int j = 0; j < 9; j++) {
 				if(solutions.getNumber(i, j) != sudokuBoard.getNumber(i, j)) {
 					return false;
 				}
 			}
 		}
 
 		return true;
 	}
 	
 	/**
 	 * reset the board to the initial board
 	 */
 	public void reset() {
 		for(int row = 0; row < 9; row++) {
 			for(int col = 0; col < 9; col++) {
 				if(board[col][row].getEditable()) {
 					board[col][row].setText("");
 					sudokuBoard.setNumber(col, row, 0);
 				}
 			}
 		}
 	}
 
 	/**
 	 * Display a number in a random unfilled cell
 	 */
 	public void displayHint() {
 		Random numGenerator = new Random();
 		int col = numGenerator.nextInt(9);
 		int row = numGenerator.nextInt(9);
 		String number = Integer.toString(sudokuBoard.getNumber(col, row));
 
 		while(!number.equals("0")) {
 			col = numGenerator.nextInt(9);
 			row = numGenerator.nextInt(9);
 			number = Integer.toString(sudokuBoard.getNumber(col, row));
 		}
 
 		sudokuBoard.setNumber(col, row, solution.getNumber(col, row));
 		board[col][row].setText(Integer.toString(solution.getNumber(col, row)));
 		board[col][row].setEditable(false);
 		board[col][row].setForeground(Color.MAGENTA);
 	}
 
 	/**
 	 * Save the current state of the game
 	 * with the time
 	 * @param time
 	 * @throws IOException
 	 */
	public void save (int time) throws IOException {
 		boolean[][] editableArray = new boolean[9][9];
 		for (int col = 0; col < 9; col++) {
 			for (int row = 0; row < 9; row++) {
 				editableArray[col][row] = board[col][row].getEditable();
 			}
 		}
		sudokuBoard.saveState(difficulty, time, editableArray);
 	}
 }
