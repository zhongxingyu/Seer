 package de.helwich.sudoku.solve;
 
 
 import static org.junit.Assert.*;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Random;
 import java.util.Set;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 
 /**
  * 
  * 
  * @author Hendrik Helwich
  */
 public class XorMatrixTest {
 
 	private static final Random random = new Random();
 
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 	}
 
 	@Before
 	public void setUp() throws Exception {
 	}
 	
 	@Test
 	@Ignore
 	public void test3() {
 		XorMatrixFactory factory = new XorMatrixFactory(7);
 		factory.addXorColumn(0,1,2);
 		factory.addXorColumn(3,4,5);
 		factory.addXorColumn(0,3);
 		factory.addXorColumn(1,4,6);
 		XorMatrix matrix = factory.createXorMatrix();
 		removeRow(matrix, 6,  2, 5);
 	}
 	
 	@Test
 	@Ignore
 	public void test2() {
 		XorMatrixFactory factory = new XorMatrixFactory(16);
 		factory.addXorColumn(2,3,4,5,6);
 		factory.addXorColumn(9,10,11,12,13);
 		factory.addXorColumn(0,3,7,10,14);
 		factory.addXorColumn(1,5,8,12,15);
 		XorMatrix matrix = factory.createXorMatrix();
 		removeRow(matrix, 0);
 		removeRow(matrix, 1);
 		removeRow(matrix, 7);
 		removeRow(matrix, 8);
 		removeRow(matrix, 14);
 		removeRow(matrix, 15, 2,4,6,9,11,13);
 		// now rows 2,4,6,9,11,13 should be removed by the matrix
 	}
 	
 	@Test
 	public void testSingle() {
 		XorMatrixFactory factory = new XorMatrixFactory(5);
		factory.addXorColumn(2,3);
 		factory.addXorColumn(2,4);
		factory.addXorColumn(0,1);
		factory.addXorColumn(0,3);
 		XorMatrix matrix = factory.createXorMatrix();
 		System.out.println(matrix);
		removeRow(matrix, 1,  3,4);
 	}
 	
 	private void removeRow(XorMatrix matrix, int row, final int... expectedRows) {
 		Arrays.sort(expectedRows);
 		TestMatrixChangeHandler handler = new TestMatrixChangeHandler(row, expectedRows);
 		matrix.addChangeHandler(handler);
 		matrix.removeRow(row);
 		matrix.removeChangeHandler(handler);
 		handler.finish();
 	}
 
 	@Test
 	public void testMain() {
 		int height = 5; // cell count
		int width = 4; // cell set count
 		int testCount = 100000;
 
 		assert height > 0 && height <= 30 && width > 0;
 		
 		for (int i = 0; i < testCount; i++) {
 			int[] xorMatrix = createRandomXorMatrix(height, width);
 			List<Integer> dispensableRows = getDispensableRows(xorMatrix, height);
 			System.out.println("dispensable rows: " + dispensableRows);
 			removeRows(xorMatrix, dispensableRows); // minimize matrix
 			XorMatrix matrix = createXorMatrix(xorMatrix, height);
 			System.out.println(matrix);
 			List<Integer> rows = getNotEmptyRows(xorMatrix, height);
 			while (rows.size() > 0) {
 				int rowToDelete = rows.get(random.nextInt(rows.size()));
 				List<Integer> rowAsList = new LinkedList<Integer>();
 				rowAsList.add(rowToDelete);
 				removeRows(xorMatrix, rowAsList);
 				List<Integer> drows = getDispensableRows(xorMatrix, height);
 				System.out.println("remove row "+rowToDelete+" => dispensable rows "+drows);
 				removeRows(xorMatrix, drows);
 				removeRow(matrix, rowToDelete, convIntListToArray(drows));
 				System.out.println(matrix);
 				rows = getNotEmptyRows(xorMatrix, height);
 				break;
 			}
 		}
 	}
 
 
 	private static List<Integer> getNotEmptyRows(int[] xorMatrix, int height) {
 		// store indices of all rows which are not empty
 		List<Integer> rows = new LinkedList<Integer>();
 		for (int i = 0; i < height; i++)
 			if (!isEmptyRow(xorMatrix, i))
 				rows.add(i);
 		return rows;
 	}
 
 	private static XorMatrix createXorMatrix(int[] xorMatrix, int height) {
 		XorMatrixFactory factory = new XorMatrixFactory(height);
 		Set<Integer> usedColumns = new HashSet<Integer>();
 		List<Integer> rows = new LinkedList<Integer>();
 		for (int column : xorMatrix) {
 			if (column != 0 && !usedColumns.contains(column)) {
 				for (int row = 0; row < height; row++)
 					if ((column & (1 << row)) != 0)
 						rows.add(row);
 				usedColumns.add(column);
 				
 				int[] rowsa = convIntListToArray(rows);
 				factory.addXorColumn(rowsa);
 				rows.clear();
 			}
 		}
 		return factory.createXorMatrix();
 	}
 	
 	private static int[] convIntListToArray(List<Integer> list) {
 		int[] array = new int[list.size()];
 		for (int i = 0; i < array.length; i++)
 			array[i] = list.get(i);
 		return array;
 	}
 	
 	private static List<Integer> convIntArrayToList(int[] array) {
 		List<Integer> list = new ArrayList<Integer>(array.length+1);
 		for (int i = 0; i < array.length; i++)
 			list.add(array[i]);
 		return list;
 	}
 
 	private void removeRows(int[] xorMatrix, List<Integer> rows) {
 		int mask = 0;
 		for (int i: rows)
 			mask |= 1 << i;
 		mask = ~mask;
 		for (int i = 0; i < xorMatrix.length; i++)
 			xorMatrix[i] &= mask;
 	}
 
 	private static final int[] createRandomXorMatrix(int height, int width) {
 		int[] matrix = new int[width];
 		for (int i = 0; i < width; i++)
 			matrix[i] = random.nextInt(1 << height);
 		return matrix;
 	}
 
 	private static final List<Integer> getDispensableRows(int[] xorMatrix, int height) {
 		// store indices of all rows which are not empty (they are removable) 
 		List<Integer> rows = getNotEmptyRows(xorMatrix, height);
 		// check every possible assignment and maybe remove row indices from the list 
 		for (int i = 0; i < (1 << height); i++) { // iterate over all possible assignments
 			if (isValidAssignment(xorMatrix, height, i)) {
 				int j = 0;
 				int current = i;
 				while (current != 0) {
 					if ((current & 1) != 0) {
 						try {
 							rows.remove((Integer) j);
 						} catch (IndexOutOfBoundsException e) {
 						}
 					}
 					j++;
 					current >>>= 1;
 				}
 			}
 		}
 		return rows;
 	}
 
 	private static final boolean isEmptyRow(int[] xorMatrix, int row) {
 		for (int col = 0; col < xorMatrix.length; col++)
 			if ((xorMatrix[col] & (1<<row)) != 0)
 				return false;
 		return true;
 	}
 
 	private static final boolean isValidAssignment(int[] xorMatrix, int height, int assgn) {
 		for (int col = 0; col < xorMatrix.length; col++) {
 			boolean isEmptyCol = true;
 			boolean foundMatch = false;
 			for (int row = 0; row < height; row++) {
 				if ((xorMatrix[col] & (1 << row)) != 0) { // matrix element (row, col) is set
 					isEmptyCol = false;
 					if ((assgn & (1 << row)) != 0) { // row is set in assignment
 						if (foundMatch)
 							return false;
 						else
 							foundMatch = true;
 					}
 				}
 			}
 			if (!isEmptyCol && !foundMatch)
 				return false;
 		}
 		return true;
 	}
 	
 	private static void printXorMatrix(int[] xorMatrix, int height) {
 		for (int row = 0; row < height; row++) {
 			System.out.print(row+"  | ");
 			for (int col = 0; col < xorMatrix.length; col++) {
 				boolean set = ((xorMatrix[col] & (1 << row)) != 0);
 				System.out.print(set ? "X " : "  ");
 			}
 			System.out.println("|");
 		}
 	}
 
 	
 
 
 	final private class TestMatrixChangeHandler implements XorMatrixChangeHandler {
 		
 		private final List<Integer> expectedRows;
 		private final boolean[] removedRows;
 		
 		public TestMatrixChangeHandler(int row, int[] expectedRows) {
 			this.expectedRows = convIntArrayToList(expectedRows);
 			this.expectedRows.add(row);
 			Collections.sort(this.expectedRows);
 			removedRows = new boolean[this.expectedRows.size()];
 		}
 		
 		@Override
 		public void onRemoveRow(int row) {
 			int rowIdx = Collections.binarySearch(expectedRows, row);
 			assertTrue("row "+row+" should not be removed", rowIdx >= 0);
 			removedRows[rowIdx] = true;
 		}
 		
 		public void finish() {
 			int rowIdx = 0;
 			for (boolean removedRow : removedRows) {
 				assertTrue("row "+expectedRows.get(rowIdx)+" should have been removed", removedRow);
 				rowIdx++;
 			}
 		}
 		
 	}
 	
 }
 
 
 
