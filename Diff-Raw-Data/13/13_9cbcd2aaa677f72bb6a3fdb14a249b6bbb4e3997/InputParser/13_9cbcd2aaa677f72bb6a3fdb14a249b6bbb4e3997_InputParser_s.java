 import java.util.HashSet;
 import java.util.LinkedList;
 /**
  * Parses commands from View and turns it into commands 
  * for the MatrixOperations class.
  */
 public class InputParser {
 
 	private HashSet<String> keywords = new HashSet<String>();
 	private MatrixCalculator calculator;
 	public static final String WRONG_INPUT = "Jag förstår inte vad du menar!";
 	/**
 	 * Class constructor.
 	 */
 	public InputParser(MatrixCalculator calculator) {
 		keywords.add("invert");
 		keywords.add("multiply");
 		keywords.add("determinant");
 		keywords.add("add");
 		keywords.add("");
 		this.calculator = calculator;
 	}
 
 	/**
 	 * Takes a string and turns it into matrix operations.
 	 */
 	public String parseData(String input) {
 		try {
 			if (input == null) {
 				throw new IllegalArgumentException();
 			} else if (input.equals("")) {
 				return "";
 			}
 			
 			String[] rows = input.split("(](\\s)?\\[)|(\\[\\[)|(]])");
 
 			// clean up data
 			for (int i = 0; i < rows.length; i++) {
 				rows[i] = rows[i].trim().toLowerCase();
 			}
 			String keyword = rows[0];
 			rows[0] = "";
 
 			if (rows.length <= 0 ||	!keywords.contains(keyword)) {
 				throw new IllegalArgumentException();
 			}
 
 			LinkedList<Matrix> matrices = dataToMatrices(rows);
 			return doOperation(keyword, matrices);
 
 		} catch (IllegalArgumentException iae) {
 			return WRONG_INPUT;
 		}
 	}
 
 	/**
 	 * Executes the operation based on the keyword and matrices provided.
 	 */
 	private String doOperation(String keyword, LinkedList<Matrix> matrices) {
 		if (matrices.isEmpty()) {
 			throw new IllegalArgumentException();
 		}
 
 		if (keyword.equals("determinant")) {
 			return "" + calculator.findDeterminant(matrices.remove());
 		} else if (keyword.equals("invert")) {
 			return calculator.invert(matrices.remove()).toString();
 		} else if (keyword.equals("")) {
 			return matrices.remove().toString();
 		}
 		throw new IllegalArgumentException();
 	}
 
 	/**
 	 * Turns the input data to matrices.
 	 */
 	private LinkedList<Matrix> dataToMatrices(String[] rows) {
 		LinkedList<Matrix> matrices = new LinkedList<Matrix>();
 
 		double[][] matrix = new double[linesBeforeNextMatrix(rows, 0)][elementsInRow(rows[1])];
 		int row = 0;
 		int c = elementsInRow(rows[1]);
 		for (int i = 1; i < rows.length; i++) {
 			if (rows[i].equals("")) { // start new matrix
 				int r = linesBeforeNextMatrix(rows, i); 
 				c = elementsInRow(rows[i+1]);
 				matrices.add(new Matrix(matrix));
 				matrix = new double[r][c];
 				row = 0;
 				continue;
 			}
 			// continue old matrix
 			int cRow = elementsInRow(rows[i]);
 			if (cRow != c) {
 				throw new IllegalArgumentException();
 			}
 			String[] elements = rows[i].split("\\s");
 			for (int j = 0; j < elements.length; j++) {
 				matrix[row][j] = Double.parseDouble(elements[j]);
 			}
 			row++;
 		}
 		matrices.add(new Matrix(matrix));
 		return matrices;
 	}
 
 	private int linesBeforeNextMatrix(String[] rows, int start) {
 		for (int i = start+1; i < rows.length; i++) {
 			if (rows[i].equals("")) {
 				return i - start - 1;
 			}
 		}
 		return rows.length - start - 1;
 	}
 
 	/**
 	 * Return the number of elemens in this row.
 	 */
 	private int elementsInRow(String s) {
 		return ((s.toCharArray().length + 1)/2);
 	}
}
