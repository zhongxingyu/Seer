 package com.tictactoe.table;
 
 /**
  * The <code>Table</code> class forms the basic framework of grid like structures.
  * Used to create a game grid in the tic tac toe game class.
  * Cells, Rows, Columns and Diagonals are nested in this class to properly
  * define the inter-relationships and not consume excess memory, while
  * still implementing pure object oriented programming (ie) Tables contain
  * rows, columns, cells, while rows, columns also contain cells, which are
  * not duplicates but references to the actual cells of the table itself.
  * @author Kenshin Himura
  */
 public class Table implements Cloneable{
 	/**
 	 * The <code>Cell</code> is the basic building block of the table.
 	 * In addition to remembering of which table it is a part, it also
 	 * returns rows and columns it belongs to and also has row and column indices
 	 * which can be set during construction of the table.
 	 * @author Kenshin Himura
 	 */
 	public class Cell {
 		/**
 		 * Represents the value of the cell.
 		 * Represents a character.
 		 * Usually will be set to '-'(empty cell),'X'(user),OR 'O' or 'P'(AI)
 		 */
 		private char value;
 		/**
 		 * Holds the row index of the cell in the parent table.
 		 */
 		private int rowIndex;
 		/**
 		 * Holds the column index of the cell in the parent table.
 		 */
 		private int colIndex;
 		/**
 		 * Constructor of Cell.
 		 * Sets the default value of the cell to <code>'-'</code>
 		 */
 		public Cell()
 		{
 			this.value='-';
 		}
 		/**
 		 * <code>getCol()</code> is used to get the parent column of the cell.
 		 * It returns a reference of type Col, which is the class which represents columns.
 		 * @return	Column of the cell
 		 */
 		public Col getCol()
 		{
 			return Table.this.getCol(colIndex);
 		}
 		/**
 		 * This method is used to get the column index of the cell.
 		 * @return	Column Index of the cell
 		 */
 		public int getColIndex()
 		{
 			return colIndex;
 		}
 		/**
 		 * <code>getRow()</code> is used to get the parent row of the cell.
 		 * It returns a reference of type Row, which is the class which represents rows.
 		 * @return	Row of the cell
 		 */
 		public Row getRow()
 		{
 			return Table.this.getRow(rowIndex);
 		}
 		/**
 		 * This method is used to get the row index of the cell.
 		 * @return	Row Index of the cell
 		 */
 		public int getRowIndex()
 		{
 			return rowIndex;
 		}
 		/**
 		 * <code>getTable()</code> is used to get the parent Table of the cell.
 		 * It returns a reference of type Table, which is the class which represents tables.
 		 * @return	Table(parent) of the cell
 		 */
 		public Table getTable()
 		{
 			return Table.this;
 		}
 		/**
 		 * This method is used to obtain the private member <code>value</code> of the cell
 		 * @return Value of cell
 		 */
 		public char getValue()
 		{
 			return value;
 		}
 		/**
 		 * This method sets the column index of the cell.
 		 * Used automatically during Table construction.
 		 * Need/Must not be used anywhere else.
 		 */
 		public void setColIndex(int colIndex)
 		{
 			this.colIndex=colIndex;
 		}
 		/**
 		 * This method sets the row index of the cell.
 		 * Used automatically during Table construction.
 		 * Need/Must not be used anywhere else.
 		 */
 		public void setRowIndex(int rowIndex)
 		{
 			this.rowIndex=rowIndex;
 		}
 		/**
 		 * This is the generic set function of the <code>value</code>
 		 * member of the class <code>Cell</code>. Since it is a private member, the set
 		 * function is used to set it.
 		 * @param value
 		 */
 		public void setValue(char value)
 		{
 			this.value=value;
 		}
 	}
 	/**
 	 * The <code>Col</code> class represents the column of a table.
 	 * It also remembers its corresponding table and its index in the table,
 	 * and also the number of rows in the table as so many cells should be
 	 * instantiated during construction.
 	 * @author Kenshin Himura
 	 *
 	 */
 	public class Col {
 		/**
 		 * An array of type <code>Cell</code>.
 		 * Holds the cells of the column, row-wise
 		 */
 		private Cell cells[];
 		/**
 		 * Holds the column index of the colummn in its parent table.
 		 */
 		private int colIndex;
 		/**
 		 * Default constructor of class
 		 * <code>Col</code>, which represents columns of a <code>Table</code> class.
 		 */
 		public Col()
 		{
 			cells=new Cell[noOfRows];
 			for(int i=0;i<noOfRows;i++)
 				cells[i]=new Cell();
 		}
 		/**
 		 * This method is used to get a specific cell of a column(j) using its row index.
 		 * @param rowIndex (i)
 		 * @return Cell of the particular row index(i) in the column(j),
 		 * (i.e.)Cell[i][j] of Table
 		 */
 		public Cell getCell(int rowIndex)
 		{
 			return cells[rowIndex];
 		}
 		/**
 		 * This method is used to set a cell to the column by specifying its row index
 		 * @param rowIndex row index of the cell
 		 * @param cell Cell to be set
 		 */
 		public void setCell(int rowIndex, Cell cell)
 		{
 			this.cells[rowIndex]=cell;
 		}
 		/**
 		 * This method is used to get all the cells of the particular column.
 		 * @return All cells of column
 		 */
 		public Cell[] getCells()
 		{
 			return cells;
 		}
 		/**
 		 * This method is used to get the column index of the column in table it pertains to.
 		 * @return Column index
 		 */
 		public int getColIndex() {
 			return colIndex;
 		}
 		/**
 		 * <code>getTable()</code> is used to get the parent <code>Table</code> of the column.
 		 * It returns a reference of type <code>Table</code>, which is the class which represents tables.
 		 * @return	Table(parent) of the column
 		 */
 		public Table getTable()
 		{
 			return Table.this;
 		}
 		/**
 		 * This method is used to check if the column is complete,
 		 * (i.e.) filled with the same characters.
 		 * Note that '-' denotes an empty cell and does not fulfill the completion criterion.
 		 * @return -1 if not Complete, 1 if X is Complete, 0 if O is Complete
 		 */
 		public int isComplete()
 		{
 			if(cells[0].getValue()!='-')
 			{
 				int j=0;
 				for(int i=0;i<(noOfRows-1);i++)
 				{
 					if(cells[i].getValue()==cells[i+1].getValue())
 						j++;
 				}
 				if(j==(noOfRows-1))
 				{
 					if(cells[0].getValue()=='X')
 						return 1;
 					else
 						return 0;
 				}
 			}
 			return -1;	
 		}
 		/**
 		 * Generic setter method to set column index of the column of the table
 		 * @param colIndex
 		 */
 		public void setColIndex(int colIndex) {
 			this.colIndex = colIndex;
 		}
 	}
 	/**
 	 * The <code>Diag</code> class is simply the instance of a diagonal in square tables,
 	 * used here as it will prove useful to check if diagonals have been completed
 	 * in a game of tic tac toe. Needless to point out, it has to remember its table and its max no.
 	 * of elements.
 	 * @author Kenshin Himura
 	 *
 	 */
 	public class Diag {
 		/**
 		 * An array of type <code>Cell</code>.
 		 * Holds the cells of the diagonal, row-wise
 		 */
 		private Cell cells[];
 		/**
 		 * Default constructor of the <code>Diag</code> class
 		 */
 		public Diag()
 		{
 			cells=new Cell[noOfRows];
 			for(int i=0;i<noOfRows;i++)
 				cells[i]=new Cell();
 		}
 		/**
 		 * This method is used to get a specific cell of a diagonal using its index(i).
 		 * @param index (i)
 		 * @return Cell of the particular row index(i) in the column(i),
 		 * (i.e.)Cell[i][i] of Table
 		 */
 		public Cell getCell(int index)
 		{
 			return cells[index];
 		}
 		/**
 		 * This method is used to get all the cells of the particular diagonal.
 		 * @return All cells of the diagonal
 		 */
 		public Cell[] getCells()
 		{
 			return cells;
 		}
 		/**
 		 * This method is used to check if the diagonal is complete,
 		 * (i.e.) filled with the same characters.
 		 * Note that '-' denotes an empty cell and does not fulfill the completion criterion.
 		 * @return -1 if not Complete, 1 if X is Complete, 0 if O is Complete
 		 */
 		public int isComplete()
 		{
 			if(cells[0].getValue()!='-')
 			{
 				int j=0;
 				for(int i=0;i<(noOfRows-1);i++)
 				{
 					if(cells[i].getValue()==cells[i+1].getValue())
 						j++;
 				}
 				if(j==(noOfRows-1))
 				{
 					if(cells[0].getValue()=='X')
 						return 1;
 					else
 						return 0;
 				}
 			}
 			return -1;	
 		}
 		/**
 		 * <code>getTable()</code> is used to get the parent <code>Table</code> of the diagonal.
 		 * It returns a reference of type <code>Table</code>, which is the class which represents tables.
 		 * @return	Table(parent) of the diagonal
 		 */
 		public Table getTable()
 		{
 			return Table.this;
 		}
 		/**
 		 * Generic setter method to set the cell of an index of a diagonal of the table
 		 * @param i index of the cell in the diagonal
 		 * @param cell Cell to be set
 		 */
 		public void setCell(int i, Cell cell) {
 			this.cells[i]=cell;
 		}
 	}
 	/**
 	 * The <code>Row</code> class represents the row of a table.
 	 * It also remembers its corresponding table and its index in the table,
 	 * and also the number of columns in the table as so many cells should be
 	 * instantiated during construction.
 	 * @author Kenshin Himura
 	 *
 	 */
 	public class Row {
 		/**
 		 * An array of type <code>Cell</code>.
 		 * Holds the cells of the row, column-wise
 		 */
 		private Cell cells[];
 		/**
 		 * Holds the column index of the row in its parent table.
 		 */
 		private int rowIndex;
 		/**
 		 * Default constructor of the <code>Row</code> class
 		 */
 		public Row()
 		{
 			cells=new Cell[noOfCols];
 			for(int i=0;i<noOfCols;i++)
 				cells[i]=new Cell();
 		}
 		/**
 		 * This method is used to get a specific cell of a row(i) using its column index.
 		 * @param colIndex (j)
 		 * @return Cell of the particular column index(j) in the row(i),
 		 * (i.e.)Cell[i][j] of Table
 		 */
 		public Cell getCell(int colIndex)
 		{
 			return cells[colIndex];
 		}
 		/**
 		 * This method is used to set a cell to the row by specifying its col index
 		 * @param colIndex column index of the cell
 		 * @param cell Cell to be set
 		 */
 		public void setCell(int colIndex, Cell cell)
 		{
 			this.cells[colIndex]=cell;
 		}
 		/**
 		 * This method is used to get all the cells of the particular row.
 		 * @return All cells of the row
 		 */
 		public Cell[] getCells()
 		{
 			return cells;
 		}
 		/**
 		 * This method is used to get the row index of the row in table it pertains to.
 		 * @return Row index
 		 */
 		public int getRowIndex() {
 			return rowIndex;
 		}
 		/**
 		 * <code>getTable()</code> is used to get the parent <code>Table</code> of the row.
 		 * It returns a reference of type <code>Table</code>, which is the class which represents tables.
 		 * @return	Table(parent) of the row
 		 */
 		public Table getTable()
 		{
 			return Table.this;
 		}
 		/**
 		 * This method is used to check if the row is complete,
 		 * (i.e.) filled with the same characters.
 		 * Note that '-' denotes an empty cell and does not fulfill the completion criterion.
 		 * @return -1 if not Complete, 1 if X is Complete, 0 if O is Complete
 		 */
 		public int isComplete()
 		{
 			if(cells[0].getValue()!='-')
 			{
 				int j=0;
 				for(int i=0;i<(noOfCols-1);i++)
 				{
 					if(cells[i].getValue()==cells[i+1].getValue())
 						j++;
 				}
 				if(j==(noOfCols-1))
 				{
 					if(cells[0].getValue()=='X')
 						return 1;
 					else
 						return 0;
 				}
 			}
 			return -1;	
 		}
 		/**
 		 * Generic setter method to set row index of the row of the table
 		 * @param rowIndex
 		 */
 		public void setRowIndex(int rowIndex) {
 			this.rowIndex = rowIndex;
 		}
 	}
 	/**
 	 * An array of type <code>Cell</code>.
 	 * Holds the cells of the table, row-wise,column-wise, maximum of noOfRowsxnoOfColumns cells
 	 */
 	private Cell cells[];
 	/**
 	 * An array of type <code>Row</code>.
 	 * Holds the rows of the table, column-wise.
 	 */
 	private Row rows[];
 	/**
 	 * An array of type <code>Col</code>.
 	 * Holds the columns of the table, row-wise.
 	 */
 	private Col cols[];
 	/**
 	 * An array of type <code>Diag</code>.
 	 * Holds the diagonals of the table, row-wise.
 	 */
 	private Diag diags[];
 	/**
 	 * Holds the number of rows of the table at any time.
 	 */
 	private int noOfRows;
 	/**
 	 * Holds the number of columns of the table at any time.
 	 */
 	private int noOfCols;
 	/**
 	 * Holds the number of cells (rows * cols) of the table at any time.
 	 * Set to 0 if the number of rows and columns are zero.
 	 */
 	private int sizeOfTable;
 	/**
 	 * Represents the score of the table in a float value.
 	 */
 	private float score=0;
 	/**
 	 * Default Constructor of <code>Table</code> class.
 	 * Made to exit the program to prevent accidental debug errors.
 	 * Never used, simply defined to define other constructors.
 	 */
 	public Table()
 	{
 		System.exit(0);
 	}
 	/**
 	 * This is the constructor of class <code>Table</code> which is used in the program.
 	 * It takes two parameters which may not be equal and constructs a table with the specified number of rows and columns and sets proper references
 	 * to the cells of the table so that they can be accessed both by the <code>Row</code> objects and <code>Col</code> objects of the <code>Table</code> object.
 	 * Also, the constructor calls the <code>init()</code> function of the <code>Table</code> class, to initialize the <code>Table</code>.
 	 * @param noOfRows The number of rows in the table to be created.
 	 * @param noOfCols The number of columns in the table to be created.
 	 */
 	public Table(int noOfRows,int noOfCols)
 	{
 		this.noOfRows=noOfRows;
 		this.noOfCols=noOfCols;
 		this.sizeOfTable=noOfRows*noOfCols;
 		rows=new Row[this.noOfRows];
 		for(int i=0;i<noOfRows;i++)
 			rows[i]=new Row();
 		cols=new Col[this.noOfCols];
 		for(int i=0;i<noOfCols;i++)
 			cols[i]=new Col();
 		cells=new Cell[this.sizeOfTable];
 		for(int i=0;i<sizeOfTable;i++)
 			cells[i]=new Cell();
 		diags=new Diag[2];
 		for(int i=0;i<2;i++)
 			diags[i]=new Diag();
 		init();
 	}
 	/**
 	 * This method is used to obtain the cells of a specific column of the table using its column index.
 	 * @param colIndex Index of the column in the table
 	 * @return Array of type <code>Cell</code> of the specified column of the table
 	 */
 	public Col getCol(int colIndex) {
 		return cols[colIndex];
 	}
 	/**
 	 * Generic get method to get the number of columns of the <code>Table</code>.
 	 * Almost never used.
 	 * @return The number of columns of the <code>Table</code>.
 	 */
 	public int getNoOfCols() {
 		return noOfCols;
 	}
 	/**
 	 * Generic get method to get the number of rows of the <code>Table</code>.
 	 * Almost never used.
 	 * @return The number of rows of the <code>Table</code>.
 	 */
 	public int getNoOfRows() {
 		return noOfRows;
 	}
 	/**
 	 * This method is used to obtain the cells of a specific row of the table using its column index.
 	 * @param rowIndex Index of the row in the table
 	 * @return Array of type <code>Cell</code> of the specified row of the table
 	 */
 	public Row getRow(int rowIndex) {
 		return rows[rowIndex];
 	}
 	/**
 	 * This method is used to initialize the </code>Table</code>.
 	 * References are set properly here.
 	 */
 	public void init()
 	{
 		int index;
 		for(int i=0;i<this.noOfRows;i++)
 			for(int j=0;j<this.noOfCols;j++)
 			{
				index=(i*noOfRows)+j;
 				getRow(i).setCell(j, cells[index]);
 				getCol(j).setCell(i, cells[index]);
 				getRow(i).getCell(j).setColIndex(j);
 				getRow(i).getCell(j).setRowIndex(i);
 				getRow(i).setRowIndex(i);
 				getCol(j).setColIndex(j);
 				//left diagonal
 				if(i==j)
 					diags[0].setCell(i,cells[index]);
 				//right diagonal
 				if((i+j)==noOfRows)
 					diags[1].setCell(i,cells[index]);
 			}
 	}
 	/**
 	 * This method is used to print the <code>Table</code>, either for debugging or display purposes.
 	 * The printing is in the form of a matrix of the no. of rows and columns as of the table used.
 	 */
 	public void printTable()
 	{
 		for(int i=0;i<noOfRows;i++)
 		{
 			for(int j=0;j<noOfCols;j++)
 				System.out.print(getRow(i).getCell(j).getValue()+" ");
 			System.out.println();
 		}
 	}
 	/**
 	 * This method is used to update a specific cell of the table with a specific value(character)
 	 * @param index Index of the cell of the table to be updated, can take values between 0 to (sizeOfTable-1).
 	 * @param updateChar Character to be updated in the specified <code>Cell</code>'s value data member.
 	 */
 	public void updateTable(int index, char updateChar)
 	{
 		cells[index].setValue(updateChar);
 	}
 	/**
 	 * This method is used to check if the table is complete,
 	 * (i.e.) if any rows, columns or diagonals are filled with the same characters.
 	 * Note that '-' denotes an empty cell and does not fulfill the completion criterion.
 	 * @return -1 if not Complete, 1 if X is Complete, 0 if O is Complete
 	 */
 	public int isComplete()
 	{
 		for(int i=0;i<2;i++)
 			if(diags[i].isComplete()!=-1)
 				return diags[i].isComplete();
 		for(int i=0;i<this.noOfRows;i++)
 			if(rows[i].isComplete()!=-1)
 				return rows[i].isComplete();
 		for(int i=0;i<this.noOfCols;i++)
 			if(cols[i].isComplete()!=-1)
 				return cols[i].isComplete();
 		return -1;	
 	}
 	/**
 	 * This method is used to check if a given cell of a table is empty.
 	 * @param index Index of the cell of the table, can take values from 0 to (sizeOfTable-1).
 	 * @return <code>True</code> if the specified <code>Cell</code> is empty, <code>False</code> otherwise.
 	 */
 	public boolean isEmpty(int index)
 	{
 		if(cells[index].getValue()=='-')
 			return true;
 		return false;
 	}
 	/**
 	 * This method is used to get the total number of 'X's in the table.
 	 * Used in calculation of score.
 	 * Made private because it has no need elsewhere. 
 	 * @return The total number of 'X's in the table.
 	 */
 	private int getNoOfXs()
 	{
 		int returnValue=0;
 		for(int i=0;i<9;i++)
 		{
 			if(cells[i].getValue()=='X')
 				returnValue++;
 		}
 		return returnValue;
 	}
 	/**
 	 * This method is used to get the total number of 'O's or 'P's in the table.
 	 * Used in calculation of score.
 	 * Made private because it has no need elsewhere. 
 	 * @return The total number of 'O's or 'P's in the table.
 	 */
 	private int getNoOfOs()
 	{
 		int returnValue=0;
 		for(int i=0;i<9;i++)
 		{
 			if((cells[i].getValue()!='X')&&(cells[i].getValue()!='-'))
 				returnValue++;
 		}
 		return returnValue;
 	}
 	/**
 	 * This method is used to get the total number of blank cells in the table.
 	 * Used in calculation of score, and in the generation of next possible moves. 
 	 * @return The total number of blank cells in the table.
 	 */
 	public int getNoOfDs()
 	{
 		int returnValue=0;
 		for(int i=0;i<9;i++)
 		{
 			if(isEmpty(i))
 				returnValue++;
 		}
 		return returnValue;
 	}
 	/**
 	 * This method is used to get the score of the current Table.
 	 * The score calculation is made here.
 	 * **This implementation may be changed in the future.
 	 * @return Score of the table as a float value.
 	 */
 	public float getScore() {
 		score=0;
 		score+=(0.2*getNoOfOs());
 		score-=(0.3*getNoOfXs());
 		score-=(0.5*getNoOfDs());
 		if(isComplete()==0)
 			score+=9;
 		if(isComplete()==1)
 			score-=9;
 		//calculate score
 		return score;
 	}
 	/**
 	 * Clone method, which overrides Object clone(), is used in place of a copy constructor
 	 * @return Clone of the specified table if possible, else <code>null</code>
 	 */	
 	public Table clone()
 	{
 			Table returnTable=new Table(this.noOfRows,this.noOfCols);
 			for(int i=0;i<this.sizeOfTable;i++)
 			{
 				returnTable.cells[i]=new Cell();
 				returnTable.cells[i].setValue(this.cells[i].getValue());
 			}
 			returnTable.init();
 			return returnTable;
 	}
 }
