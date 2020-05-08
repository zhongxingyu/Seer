 import java.util.ArrayList;
 
 /**
 * Class that allows creation, modification and access to
 *  a Sudoku board
 * @author Hayden
 * @version 0.1
 * 
 */
 
 public class SudokuBoard implements Board {
 
 	// TODO Javadoc this
 	public SudokuBoard(int size, int difficulty)
 	{	
 		boardSize = size;
 		board = new ArrayList<ArrayList<BoardCell>>();
 		
 		BoardGenerator generator = new BoardGenerator(difficulty, boardSize);
 		
 		for (int i = 0; i < boardSize; i++)
 		{
 			board.add(new ArrayList<BoardCell>());
 			ArrayList<BoardCell> row = board.get(i);
 			for (int j = 0; j < boardSize; j++)
 			{ 
				row.add(new BoardCell(generator.getValue(i, j), generator.isVisible(i, j), boardSize));
 			}
 		}
 	}
 	
 	// TODO Javadoc this
 	public int getCellValue(int row, int col)
 	{
 		return getCell(row, col).getValue();
 	}
 	
 	public void setCellValue(int row, int col, int number)
 	{
 		getCell(row, col).setValue(number);
 	}
 	
 	public boolean isVisibleCellValue(int row, int col)
 	{
 		return getCell(row, col).isVisible();
 	}
 		
 	public boolean isVisibleCellTemp(int row, int col, int number)
 	{
 		return getCell(row, col).issetTemp(number);
 	}
 		
 	public void setCellTemp(int row, int col, int number, boolean isSet)
 	{
 		getCell(row, col).setTemp(number, isSet);
 	}
 	
 	public boolean isCorrectBoard()
 	{
 		boolean allCorrect = true;
 		for (int i = 0; i < boardSize; i++)
 		{
 			for (int j = 0; i < boardSize; j++)
 			{
 				if (!isCorrectCell(i, j))
 				{
 					allCorrect = false;
 				}
 			}
 		}
 		return allCorrect;
 	}
 
 	// TODO Javadoc this
 	public boolean isCorrectCell(int row, int col)
 	{
 		return getCell(row, col).isCorrect();
 	}
 	
 	// TODO Javadoc this
 	private BoardCell getCell(int row, int col)
 	{
 		return board.get(row).get(col);
 	}
 		
 
 	// TODO: Remove later
 	public void print()
 	{
 		for (int i = 0; i < boardSize; i++)
 		{
 			for (int j = 0; j < boardSize; j++)
 			{
 				System.out.print(this.getCellValue(i, j) + " ");
 			}
 			System.out.print("\n");
 		}		
 	}
 	
 	private ArrayList<ArrayList<BoardCell>> board;
 	private static int boardSize;
 	
 	
 }
