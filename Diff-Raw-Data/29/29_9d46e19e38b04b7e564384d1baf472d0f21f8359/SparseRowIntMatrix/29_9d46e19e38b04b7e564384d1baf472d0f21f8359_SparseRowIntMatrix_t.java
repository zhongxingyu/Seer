 /**
  * SparseRowMatrix.java
  * 
  * history: 2009/04/02 1.0  LST created.
  */
 
 package de.ismll.table.impl;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.LineNumberReader;
 import java.io.Writer;
 import java.util.Arrays;
 
 import de.ismll.table.IntMatrix;
 import de.ismll.table.IntVector;
 import de.ismll.table.Matrices;
 
 /**
  * A simple matrix with sparse rows.
  * The sparsity structure cannot be changed after creation.
  * 
  * @author Lars Schmidt-Thieme, Andre Busche
  * @version 1.0
  * @version 1.1 [andre]
  */
 public class SparseRowIntMatrix implements IntMatrix {
 
 	public static boolean debug;
 	protected int numRows;
 	protected int numColumns;
 	// before making these fields public, have a look at #getStoredColumns
 	protected int[][] index;
 	// before making these fields public, have a look at #getStoredValues
 	protected int[][] value;
 
 	public SparseRowIntMatrix(int numRows, int numColumns, int[][] index, int[][] value) {
 		this.numRows = numRows;
 		this.numColumns = numColumns;
 		this.index = index;
 		this.value = value;
 	}
 
 	/**
 	 * Get the number of rows.
 	 */
 	public int getNumRows() { return numRows;}
 
 	/**
 	 * Get the number of columns.
 	 */
 	public int getNumColumns() { return numColumns;}
 
 
 	// ----------------------------------------------------------------------
 	// Cell contents (dense accessors):
 
 	/**
 	 * Get the value of a cell.
 	 * This is an expensive procedure as the cell has to be looked up!
 	 */
 	public int get(int rowIndex, int columnIndex) {
 		int idx = Arrays.binarySearch(index[rowIndex], columnIndex);
 		return idx < 0 ? 0 : value[rowIndex][idx];
 	}
 
 	/**
 	 * Set the value of a cell.
 	 * This is an expensive procedure as the cell has to be looked up!
 	 */
 	public void set(int rowIndex, int columnIndex, int val) {
 		int idx = Arrays.binarySearch(index[rowIndex], columnIndex);
 		if (idx == -1 && val != 0) {
 			System.err.println("ERROR: tried to set a non-stored cell.");
 			System.exit(-1);
 		} else
 			value[rowIndex][idx] = val;
 	}
 
 
 	// ----------------------------------------------------------------------
 	// Cell contents (sparse accessors):
 
 	/**
 	 * Get the stored columns in a row.
 	 */
 	public int[] getStoredColumns(int rowIndex) {
 		return index[rowIndex];
 	}
 
 	/**
 	 * Get the stored cell values in a row.
 	 */
 	public int[] getStoredValues(int rowIndex) {
 		return value[rowIndex];
 	}
 
 
 
 	public static void createFormatFile(File fn, int rowOffset) throws IOException {
 		// 1. pass: count #rows, #columns, #triples:
 		System.out.println("create format file for " + fn + " (1st pass)..." + " rowOffset = " + rowOffset);
 
 		LineNumberReader read = new LineNumberReader(new FileReader(fn));
 		int numTriples = 0;
 		int numRows = 0;
 		int numCols = 0;
 		String line=null;
 		while ((line = read.readLine())!=null) {
 			if (numTriples % 1000000 == 0)
 				System.out.print(numTriples + "\r");
 			String[] lne = line.split("\t");
 			int rowIdx = Integer.parseInt(lne[0])+ rowOffset;
 			int colIdx = Integer.parseInt(lne[1]);
//			float val = Float.parseFloat(lne[2]);
 
 			++numTriples;
 			if (rowIdx > numRows)
 				numRows = rowIdx;
 			if (colIdx > numCols)
 				numCols = colIdx;
 		}
 		read.close();
 
 		++numRows;
 		++numCols;
 		System.out.println();
 		System.out.println("detected sparse matrix " + numRows + " x " + numCols + " : " + numTriples + " triple");
 
 		// 2. pass: count #cells in each row:
 		System.out.println("create format file for " + fn + " (2nd pass)...");
 		int[] rowLength = new int[numRows];
 		read = new LineNumberReader(new FileReader(fn));
//		long lineNumber = 0;
 		for (int i = 0; i < numTriples; ++i) {
 			String[] lne=read.readLine().split("\t");
 
 			int rowIdx = Integer.parseInt(lne[0])+ rowOffset;
//			int colIdx = Integer.parseInt(lne[1]);
//			float val = Float.parseFloat(lne[2]);
 
 			if (rowIdx < 0)
 				System.out.println("line " + line);
//			++lineNumber;
 			++rowLength[rowIdx];
 		}
 		read.close();
 
 		// 3. write the format file:
 		BufferedWriter out = new BufferedWriter(new FileWriter(fn + ".format"));
 		out.write(numRows + "\t" + numCols + "\t" + numTriples + "\t" + rowOffset);
 		out.newLine();
 		out.write("" + rowLength[0]);
 		for (int i = 1; i < numRows; ++i)
 			out.write("\t" + rowLength[i]);
 		out.newLine();
 		out.close();
 	}
 
 
 	public static SparseRowIntMatrix read(File fn) throws IOException { return read(fn, -1); }
 
 	/**
 	 * Read a row-sparse matrix from a triples representation: rowIdx, colIdx, value.
 	 * Requires a format file with name <fn>.format that contains #rows, #cols, #triples \n #cells in row1, #cells in row2, ...
 	 */
 	public static SparseRowIntMatrix read(File fn, int showProgress) throws IOException {
 		// 1. read the format file:
 		File formatFile = new File(fn.getAbsoluteFile() + ".format");
 		if (!formatFile.exists()) {
 			System.out.println("Format file does not exist ... creating...");
 			createFormatFile(fn, 0);
 		}
 		System.out.print("Reading " + fn);
 		LineNumberReader readFormat = new LineNumberReader(new FileReader(formatFile));
 
 		String formatHeader = readFormat.readLine();
		if(null == formatHeader) {
			readFormat.close();
			throw new IOException("Could not read header format");
		}
 		String[] split = formatHeader.split("\t");
 
 		int numRows = Integer.parseInt(split[0]);
 		int numColumns = Integer.parseInt(split[1]);
 		int numTriples = Integer.parseInt(split[2]);
 		int rowOffset = Integer.parseInt(split[3]);
 
 		String counts = readFormat.readLine();
		if(null == counts) {
			readFormat.close();
			throw new IOException("Could not read header counts");
		}
 		String[] countsSplit = counts.split("\t");
 
 		int[] rowLength = new int[numRows];
 		int[][] index = new int[numRows][];
 		int[][] value = new int[numRows][];
 		for (int i = 0; i < numRows; ++i) {
 			rowLength[i] = Integer.parseInt(countsSplit[i]);
 			index[i] = new int[rowLength[i]];
 			value[i] = new int[rowLength[i]];
 		}
 		readFormat.close();
 
 		if (debug)
 			System.out.println("read sparse matrix " + numRows + " x " + numColumns + " : " + numTriples + " triple...");
 
 		// 2. read the triples:
 		LineNumberReader readData = new LineNumberReader(new FileReader(fn.getAbsoluteFile()));
 
 		int idx = 0;
 		int lastRowIdx = 0;
 		for (int i = 0; i < numTriples; ++i) {
 			if (i > 0 && i % showProgress == 0) {
 				System.out.print("\r " + i);
 			}
 			String dataLine = readData.readLine();
			if(null == dataLine) {
				readFormat.close();
				readData.close();
				throw new IOException("Could not read data at line " + i);
			}
 			String[] splitData = dataLine.split("\t");
 
 			int rowIdx = Integer.parseInt(splitData[0]) + rowOffset;
 			int colIdx = Integer.parseInt(splitData[1]);
 			int val = Integer.parseInt(splitData[2]);
 
 			if (rowIdx != lastRowIdx) {
 				lastRowIdx = rowIdx;
 				idx = 0;
 			}
 			index[rowIdx][idx] = colIdx;
 			value[rowIdx][idx] = val;
 			++idx;
 		}
 		readData.close();
 		System.out.println("done!");
 		return new SparseRowIntMatrix(numRows, numColumns, index, value);
 	}
 
 
 	public void write(Writer out, int rowOffset) throws IOException {
 		for (int i = 0; i < numRows; ++i) {
 			int[] idx = index[i];
 			int[] val = value[i];
 			for (int j = 0; j < idx.length; ++j)
 				out.write("" + (i+rowOffset)  + "\t" + idx[j] + "\t" + val[j] + "\n");
 		}
 	}
 
 
 	public void write(Writer out, int rowOffset, IntVector columnIndex) throws IOException {
 		for (int i = 0; i < numRows; ++i) {
 			int[] idx = index[i];
 			int[] val = value[i];
 			for (int j = 0; j < idx.length; ++j) {
 				int col = idx[j];
 				int colNew = columnIndex.get(col);
 				if (colNew != -1)
 					out.write("" + (i+rowOffset)  + "\t" + colNew + "\t" + val[j] + "\n");
 			}
 		}
 	}
 
 
 	public String toString() {
 		return Matrices.toString(this);
 	}
 
 	public Integer getExpensive(int row, int col) {
 		return Integer.valueOf(get(row, col));
 	}
 
 
 }
