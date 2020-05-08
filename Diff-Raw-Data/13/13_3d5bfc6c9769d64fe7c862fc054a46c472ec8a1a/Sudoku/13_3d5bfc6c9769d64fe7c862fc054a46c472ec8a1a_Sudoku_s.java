 package avanwieringen.sudoku;
 
 import org.apache.commons.lang3.ArrayUtils;
 import org.apache.commons.lang3.CharUtils;
 import org.apache.commons.lang3.StringUtils;
 
import com.sun.tools.corba.se.idl.InvalidArgument;

import java.util.Arrays;

import avanwieringen.tools.*;

 /**
  * 
  * @author arjan
  *
  */
 public class Sudoku {
 	
 	/**
 	 * Values of the Sudoku
 	 */
 	protected Cell[][] cells;
 	
 	/**
 	 * The maximum value of a cell (equal to the row-, column- and nonet count)
 	 */
 	protected int maxValue;
 	
 	/**
 	 * The rows of the Sudoku
 	 */
 	protected CellCollection[] rows;
 	
 	/**
 	 * The columns of the Sudoku
 	 */
 	protected CellCollection[] columns;
 	
 	/**
 	 * The nonets of the Sudoku
 	 */
 	protected CellCollection[] nonets;
 
 	/**
 	 * Construct an empty 9^2 * 9^2 Sudoku
 	 */
 	public Sudoku() {
 		this(9);
 	}
 	
 	/**
 	 * Creates an empty n^2 x n^2 Sudoku
 	 * @param n
 	 */
 	public Sudoku(int n) {
 		this(StringUtils.repeat('.', (int) Math.pow(n, 4)));
 	}
 	
 	/**
 	 * Create a Sudoku of the given values
 	 * @param values A String of 81 characters specifying the values
 	 */
 	public Sudoku(String values) {
 		if(Math.pow(values.length(), 0.25) != (int)Math.pow(values.length(), 0.25)) {
 			throw new IllegalArgumentException("Values should be a String of n^2 * n^2 characters");
 		}
 		
 		// fill values and create relations
 		this.maxValue 		= (int)Math.pow(values.length(), 0.25);
 		this.cells 			= new Cell[this.maxValue][this.maxValue];
 		this.rows			= new CellCollection[this.maxValue];
 		this.columns 		= new CellCollection[this.maxValue];
 		this.nonets 		= new CellCollection[this.maxValue];
 		
 		int row;
 		int column;
 		int nonet;
 		Cell cell;
 		for(int i = 0; i < values.length(); i++) {
 			
 			if(i >= 0 && i < this.maxValue) {
 				this.rows[i] 	= new CellCollection(this.maxValue, CellCollection.Type.ROW);
 				this.columns[i] = new CellCollection(this.maxValue, CellCollection.Type.COLUMN);
 				this.nonets[i] 	= new CellCollection(this.maxValue, CellCollection.Type.NONET);
 			}
 			
 			row 	= (int)(i/this.maxValue);
 			column 	= (int)(i%this.maxValue);
 			nonet  	= (int) (((int)(row/Math.sqrt(this.maxValue)) * Math.sqrt(this.maxValue)) + ((int)(column / Math.sqrt(this.maxValue))));
 			cell    = new Cell();
 			this.cells[row][column] = cell;
 			this.setValue(row, column, this.parseValue(values.charAt(i)));			
 			this.rows[row].addCell(cell);
 			this.columns[column].addCell(cell);
 			this.nonets[nonet].addCell(cell);
 		}
 		
 		// calculate all possibilities
 		this.calculatePossibilities();
 	}
 	
 	/**
 	 * Sets a value of a specified cell
 	 * @param r The row-index (0-based)
 	 * @param c The column (0-based)
 	 * @param value The value to set
 	 */
 	public void setValue(int r, int c, int value) {
 		this.cells[r][c].setValue(value);
 	}
 	
 	/**
 	 * Returns the value belonging to the specified row and column (0-based)
 	 * @param r Row-index 0-based
 	 * @param c Column-index 0-based
 	 * @return Value
 	 */
 	public int getValue(int r, int c) {
 		return this.cells[r][c].getValue();
 	}
 	
 	/**
 	 * Returns the row count
 	 * @return Row count
 	 */
 	public int getRowCount() {
 		return this.maxValue;
 	}
 	
 	/**
 	 * Returns the Column count
 	 * @return Column count
 	 */
 	public int getColumnCount() {
 		return this.maxValue;
 	}
 	
 	/**
 	 * Returns the nonet count
 	 * @return Nonet count
 	 */
 	public int getNonetCount() {
 		return this.maxValue;
 	}
 	
 	/**
 	 * Returns the total cell count
 	 * @return Cell count
 	 */
 	public int getCellCount() {
 		return (int) Math.pow(this.maxValue, 2);
 	}
 	
 	/**
 	 * Reduces all possibilities for each Cell
 	 */
 	protected void calculatePossibilities() {
 		for(int r = 0; r < this.cells.length; r++) {
 			for(int c = 0; c < this.cells[r].length; c++) {
 				this.cells[r][c].calculatePossibilities();
 			}
 		}
 	}
 	
 	/**
 	 * Parses a char value to the corresponding integer value, counting '.', '0', ' ', '-' and'x' as empty values
 	 * @param value
 	 * @return the parsed value
 	 */
 	protected int parseValue(char value) {
 		char[] emptyValues = {'.', '0', ' ', '-', 'x'};
 		if(ArrayUtils.contains(emptyValues, value)) {
 			return 0;
 		} else {
 			return CharUtils.toIntValue(value);
 		}
 	}
 	
 	/**
 	 * Parses an int value to the corresponding integer value
 	 * @param value
 	 * @return the parsed value
 	 */
 	protected int parseValue(int value) {
 		if(value > this.maxValue || value < 0) {
 			throw new IndexOutOfBoundsException("Values must lie between 0 (empty) or " + this.maxValue);
 		}
 		return value;
 	}
 }
