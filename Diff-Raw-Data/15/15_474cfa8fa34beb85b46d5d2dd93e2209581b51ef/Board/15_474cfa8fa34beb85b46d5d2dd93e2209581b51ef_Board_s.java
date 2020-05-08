 package game;
 
 import game.Cell.CellState;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Random;
 
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 
 @SuppressWarnings("serial")
 public class Board extends JPanel implements MouseListener {
 
 	public enum Difficulty {EASY,MEDIUM,HARD,EMPTY};
 
 	
 	// Used to store the number of cells on the board
 	// These are only set to 5 so that failing tests run and fail
 	private int boardWidth = 5;
 	private int boardHeight = 5;
 	private Game game;
 	
 	// Used to store the number of mines the board has
 	private int mineCount;
 	
 	// Used to store the cells. 
 	// The first arrayList is the board
 	// The second arrayList is each row of the board
 	private ArrayList<ArrayList<Cell>> gameBoard;
 	
 	// Used to aid in the drawing of the cells
 	public static final int cellWidth = 30;
 	public static final int cellHeight = 30;
 	
 	// Default constructor, creates an "EASY" game
 	public Board() {
 		this(Difficulty.EASY, null);
 	}
 	public Board(Difficulty difficulty) {
 		this(difficulty, null);
 	}
 	// Parameterized constructor, creates a game with the specified difficulty.
 	public Board(Difficulty difficulty, Game game) {
 		gameBoard = new ArrayList<ArrayList<Cell>>();
 		setBoardSpecifications(difficulty);
 		setSize(new Dimension(cellWidth*boardWidth, cellHeight*boardHeight));
 		initializeBoard();
 		placeRandomMines();
 		calcAdjacencies();
 		this.game = game;
 		addMouseListener(this);
 	}
 	
 	// Sets the specifications of the board based on the difficulty given (height, width, and mineCount)
 	private void setBoardSpecifications(Difficulty d) {
 		switch(d){
 		case EASY:
 			this.boardWidth=8;
 			this.boardHeight=8;
 			this.mineCount=10;
 			break;
 		case MEDIUM:
 			this.boardWidth=16;
 			this.boardHeight=16;
 			this.mineCount=40;
 			break;
 		case HARD:
 			this.boardWidth=30;
 			this.boardHeight=16;
 			this.mineCount=99;
 			break;
 		case EMPTY:
 			this.boardWidth=8;
 			this.boardHeight=8;
 			this.mineCount=0;
 			break;
 		default:
 			this.boardWidth=1;
 			this.boardHeight=1;
 			this.mineCount=0;
 			break;
 		}
 	}
 	
 	// Initializes the board based on the sizes that have previously set
 	private void initializeBoard() {
 		for (int row = 0; row < boardHeight; row++) {
 			gameBoard.add(new ArrayList<Cell>());
 			for (int col = 0; col < boardWidth; col++) {
 				gameBoard.get(row).add(new Cell(row, col));
 			}
 		}
 	}
 	
 	// Places mines randomly around the board
 	private void placeRandomMines() {
 		ArrayList<int[]> boardCells = new ArrayList<int[]>();
 		int[] cellCoords={0,0};
 		long seed = System.nanoTime();
 		for (int row = 0; row < boardHeight; row++) {
 			for (int col = 0; col < boardWidth; col++) {
 				boardCells.add(new int[] {row,col});
 			}
 		}
 		Collections.shuffle(boardCells, new Random(seed));
 		for(int iter=0;iter<this.mineCount;iter++){
 			cellCoords=boardCells.get(iter);
 			this.setIndividualMine(cellCoords[0], cellCoords[1]);
 		}
 	}
 	
 	// Calculates the adjacencies of all of the cells and sets their "minesAdjacent" variable
 	public void calcAdjacencies() {
 		int mines;
 		for (int row = 0; row < boardHeight; row++) {
 			for (int col = 0; col < boardWidth; col++) {
 				mines=0;
 				Cell cell =getCellAt(row,col);
 				for (int i = -1; i <= 1; i++) {
 					for (int j = -1; j <=1; j++) {
 						if (i != 0 || j != 0) {
 							// Check validity of the cell, and if it is a mine.
 							// If both are true, add one to the cell count
 							if (validCell(cell.getRow() + i, cell.getColumn() + j) &&getCellAt(cell.getRow() + i, cell.getColumn() + j).isMine()) {
 								mines++;
 							}
 						}
 					}
 				}
 				cell.setMinesAdjacent(mines);
 			}
 		}
 	}
 	
 	private boolean validCell(int i, int j) {
 		if(i<0 ||i>=boardHeight){return false;}
 		if(j<0|| j>=boardWidth){return false;}
 		return true;
 	}
 
 	/* 
 	 * This should be called by the board's mouse listener
 	 * (not yet created). This should attempt to set the cell 
 	 * that is passed in to "REVEALED".
 	 * As a part of this, logic should be included such that 
 	 * if a cell with a zero mine count is set to revealed, then it 
 	 * calls this on the cell above, below, left, and right of it. 
 	 */
 	public void revealCell(Cell cell) {
 		cell.revealCell();
 		
 		// If the mines adjacent is not 0, or if the cell was not revealed because it couldn't be, return
 		if(cell.getMinesAdjacent() != 0 || cell.getCellState() != CellState.REVEALED || cell.isMine()) {
 			return;
 		}
 		// Otherwise, we've revealed a cell that has no adjacent mines.
 		// So we need to reveal all the adjacent mines. This identifies those
 		ArrayList<Cell> cellsToReveal = new ArrayList<Cell>();
 		for(int i=cell.getRow()-1; i<=cell.getRow()+1; ++i) {
 			for(int j=cell.getColumn()-1; j<=cell.getColumn()+1; ++j) {
 				// If i and j is not the cell we just revealed
 				// AND i or j are valid cell coordinates
 				// OR the cell at i,j is not set to revealed, add the cell
 				if(((i != cell.getRow() || j != cell.getColumn()) && validCell(i, j)) && getCellAt(i,j).getCellState() != CellState.REVEALED)  {
 					cellsToReveal.add(getCellAt(i, j));
 				}				
 			}
 		}
 		
 		// RECURSION!! RUN!!
 		// Not really, this just calls this function on all adjacent cells. 
 		for(Cell iterCell: cellsToReveal) {
 			revealCell(iterCell);
 		}
 	}
 	
 	public Cell getCellAt(int row, int column) {
 		return gameBoard.get(row).get(column);
 	}
 	
 	// Paints the board by telling each cell to draw itself.
 	@Override
 	public void paintComponent(Graphics g) {
 		for (int row = 0; row < boardHeight; row++) {
 			for (int col = 0; col < boardWidth; col++) {
 				getCellAt(row, col).draw(g);
 			}
 		}
 	}
 
 	// Setters for the private variables. These can be removed if needed.
 	public void setBoardWidth(int boardWidth) {
 		this.boardWidth = boardWidth;
 	}
 	public void setBoardHeight(int boardHeight) {
 		this.boardHeight = boardHeight;
 	}
 	public void setMineCount(int mineCount) {
 		this.mineCount = mineCount;
 	}
 	public void setGameBoard(ArrayList<ArrayList<Cell>> gameBoard) {
 		this.gameBoard = gameBoard;
 	}
 	
 	// Getters for the private variables
 	public int getBoardWidth() {
 		return boardWidth;
 	}
 	public int getBoardHeight() {
 		return boardHeight;
 	}
 	public int getMineCount() {
 		return mineCount;
 	}
 	public ArrayList<ArrayList<Cell>> getGameBoard() {
 		return gameBoard;
 	}
 
 	/** Functions below this point are only used for
 	 * testing purposes. These should be not be directly called
 	 *  when implementing the game */
 	
 	// Allows an individual mine to be placed at a certain location
 	public void setIndividualMine(int row, int column) {
 		gameBoard.get(row).get(column).setMine(true);
 	}
 
 	@Override
 	public void mouseClicked(MouseEvent e) {
 		if (e.getButton() == MouseEvent.BUTTON1) {
 			revealCell(getCellAt(e.getY() / cellHeight, e.getX() / cellWidth));
 			repaint();
			if(getCellAt(e.getY() / cellHeight, e.getX()/cellWidth).isMine()) {
 				if(JOptionPane.showConfirmDialog(null, "You lost! New game?") == JOptionPane.YES_OPTION) {
 					game.resetGame();
 				} else {
 					System.exit(0);
 				}
 				
 			}
 			
 			
 		} else {
 			getCellAt(e.getY() / cellHeight, e.getX() / cellWidth).toggleFlag();
 			repaint();
 			if (getCellAt(e.getY() / cellHeight, e.getX() / cellWidth).getCellState().equals(CellState.FLAGGED)) {
 				game.decrementMineCount();
 			} else {
 				game.incrementMineCount();
 			}
 		}
 		
 		
 		
 	}
 
 	@Override
 	public void mouseEntered(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseExited(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mousePressed(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void mouseReleased(MouseEvent e) {
 		// TODO Auto-generated method stub
 		
 	}
 }
