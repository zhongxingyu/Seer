 package math.matrices;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.Arrays;
 import static math.Math.*;
 
 /**
  * Class for matrix calculations.
  */
 public class Matrix {
 	protected float[][] values;
 
 	private static Class<? extends Matrix>[] sizeClasses;
 	private static NumberRange[][] sizeSizes;
 
 	// initializing size <-> class database
 	static {
 		sizeClasses = initArray(5);
 		sizeSizes = new NumberRange[5][];
 		sizeClasses[0] = Vector2.class;
 		sizeSizes[0] = new NumberRange[] { new NumberRange(1), new NumberRange(2) };
 		sizeClasses[1] = Vector3.class;
 		sizeSizes[1] = new NumberRange[] { new NumberRange(1), new NumberRange(3) };
 		sizeClasses[2] = Vector4.class;
 		sizeSizes[2] = new NumberRange[] { new NumberRange(1), new NumberRange(5) };
 		sizeClasses[3] = Vector.class;
 		sizeSizes[3] = new NumberRange[] { new NumberRange(1), new NumberRange() };
 		sizeClasses[4] = Matrix.class;
 		sizeSizes[4] = new NumberRange[] { new NumberRange(), new NumberRange() };
 	}
 
 	@SuppressWarnings("unchecked")
 	/**
 	 * This is a workaround for the java restriction to not have generic arrays.
 	 * @param size The size of the desired generic array.
 	 * @return Returns a generic array.
 	 */
 	private static Class<? extends Matrix>[] initArray(int size) {
 		return new Class[size];
 	}
 
 	/**
 	 * Searches for class types, that matches the dimensions of a matrix passed
 	 * to this function.
 	 * 
 	 * @param columns
 	 * @param rows
 	 * @return
 	 */
 	private static Class<? extends Matrix> getClassFromSize(int columns, int rows) {
 		for (int i = 0; i < sizeClasses.length; i++) {
 			if (sizeSizes[i][0].isInside(columns) && sizeSizes[i][1].isInside(rows))
 				return sizeClasses[i];
 		}
 		throw new RuntimeException("This error can not occur!");
 	}
 
 	/**
 	 * Initializes a matrix with the passed values. It will automatically parsed
 	 * to the best fitting class.
 	 * 
 	 * @param values
 	 *            The values of the new matrix, whereby the primary array
 	 *            contains the float arrays for the columns.
 	 * @return Returns a proper casted Matrix object.
 	 */
 	public static Matrix mat(float[][] values) {
 		Class<? extends Matrix> cl = getClassFromSize(values.length, values[0].length);
 		try {
 			Matrix mat = (Matrix) cl.getConstructors()[0].newInstance(new Object[] { values });
 			return mat;
 		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
 				| InvocationTargetException | SecurityException e) {
 			throw new RuntimeException("Error creating matrix from values!");
 		}
 	}
 
 	/**
 	 * Initializes new variable sized vector.
 	 * 
 	 * @param values
 	 * @return a Vector object
 	 */
 	public static Vector vec(float[] values) {
 		return (Vector) mat(new float[][] { values });
 	}
 
 	/**
 	 * Initializes new two-dimensional vector.
 	 * 
	 * @param values
 	 * @return a Vector2 object
 	 */
 	public static Vector2 vec2(float x, float y) {
 		return new Vector2(new float[][] { new float[] { x, y } });
 	}
 
 	/**
 	 * Initializes new three-dimensional vector.
 	 * 
	 * @param values
 	 * @return a Vector3 object
 	 */
 	public static Vector3 vec3(float x, float y, float z) {
 		return new Vector3(new float[][] { new float[] { x, y, z } });
 	}
 
 	/**
 	 * Initializes new four-dimensional vector.
 	 * 
	 * @param values
 	 * @return a Vector4 object
 	 */
 	public static Vector4 vec4(float x, float y, float z, float w) {
 		return new Vector4(new float[][] { new float[] { x, y, z, w } });
 	}
 
 	/**
 	 * A constructor that should not be called anywhere except for this class.
 	 * 
 	 * @param values
 	 *            The values of the matrix.
 	 */
 	public Matrix(float[][] values) {
 		this.values = values;
 	}
 
 	public float[][] getData() {
 		return this.values;
 	}
 
 	/**
 	 * Retrieves the size of this matrix.
 	 * 
 	 * @return Returns an Integer array whereby the first element is the column
 	 *         count and the second element is the row count.
 	 */
 	public int[] getSize() {
 		return new int[] { values.length, values[0].length };
 	}
 
 	/**
 	 * Converts the matrix into a one dimensional array. Mostly for
 	 * compatibility with OpenGL.
 	 * 
 	 * @return Returns 1D array.
 	 */
 	public float[] get1DData() {
 		float[] dat = new float[values.length * values[0].length];
 		for (int i = 0; i < values.length; i++) {
 			System.arraycopy(values, 0, dat, i * values[0].length, values[0].length);
 		}
 		return dat;
 	}
 
 	/**
 	 * @param column
 	 * @param row
 	 * @return Returns the value at the specific location in the matrix defined
 	 *         by the column and row.
 	 */
 	public float getValue(int column, int row) {
 		return values[column][row];
 	}
 
 	/**
 	 * Copies the Matrix object and returning an identical matrix. Modifying
 	 * values in that matrix will not change values of the other.
 	 * 
 	 * @return
 	 */
 	public Matrix copy() {
 		float[][] values = new float[this.values.length][this.values[0].length];
 		System.arraycopy(this.values, 0, values, 0, values.length);
 		return mat(values);
 	}
 
 	@Override
 	public String toString() {
 		String str = "";
 		for (float[] v : transpose().values) {
 			str += Arrays.toString(v) + '\n';
 		}
 		return str;
 	}
 
 	/**
 	 * Removes a column at a specific index from the matrix, therefore resizing
 	 * it.
 	 * 
 	 * @param index
 	 *            The index of the column that should be removed.
 	 * @return Returns the new matrix without the column.
 	 */
 	public Matrix removeColumn(int index) {
 		float[][] values = new float[this.values.length - 1][this.values[0].length];
 		int n = 0;
 		for (int i = 0; i < this.values.length; i++) {
 			if (i != index) {
 				values[n] = this.values[i];
 				n++;
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Removes a row at a specific index from the matrix, therefore resizing it.
 	 * 
 	 * @param index
 	 *            The index of the row that should be removed.
 	 * @return Returns the new matrix without the row.
 	 */
 	public Matrix removeRow(int index) {
 		float[][] values = new float[this.values.length][this.values[0].length - 1];
 		for (int i = 0; i < this.values.length; i++) {
 			int n = 0;
 			for (int j = 0; j < this.values.length; j++) {
 				if (j != index) {
 					values[i][n] = this.values[i][j];
 					n++;
 				}
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Swaps a column with another one.
 	 * 
 	 * @param a
 	 *            Index of first column.
 	 * @param b
 	 *            Index of second column.
 	 * @return Returns a matrix in which the column at index a and the column at
 	 *         index b are swapped.
 	 */
 	public Matrix swapColumns(int a, int b) {
 		Matrix mat = this.copy();
 		float[] temp = mat.values[a];
 		mat.values[a] = mat.values[b];
 		mat.values[b] = temp;
 		return mat;
 	}
 
 	/**
 	 * Swaps a row with another one.
 	 * 
 	 * @param a
 	 *            Index of first row.
 	 * @param b
 	 *            Index of second row.
 	 * @return Returns a matrix in which the row at index a and the row at index
 	 *         b are swapped.
 	 */
 	public Matrix swapRows(int a, int b) {
 		Matrix mat = this.copy();
 		for (int i = 0; i < this.getSize()[0]; i++) {
 			float temp = mat.values[i][a];
 			mat.values[i][a] = mat.values[i][b];
 			mat.values[i][b] = temp;
 		}
 		return mat;
 	}
 
 	// ----------------Start of calculation functions----------
 
 	/**
 	 * Swaps rows and columns
 	 * 
 	 * @return
 	 */
 	public Matrix transpose() {
 		float[][] values = new float[this.values[0].length][this.values.length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[j][i] = this.values[i][j];
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Conventional matrix multiplication, whereby the output matrix has the row
 	 * count of the first matrix and the column count of the second matrix.
 	 * 
 	 * @param mat
 	 *            The matrix to multiply with.
 	 * @return The result of the multiplication.
 	 */
 	public Matrix multiply(Matrix mat) {
 		float[][] values = new float[mat.values.length][this.values[0].length];
 		Matrix mat1 = transpose();
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[0].length; j++) {
 				Vector v0 = vec(mat1.values[j]);
 				Vector v1 = vec(mat.values[i]);
 				values[i][j] = v0.dot(v1);
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Adds two matrices together. Every element will be added component wise.
 	 * 
 	 * @param mat
 	 * @return
 	 */
 	public Matrix add(Matrix mat) {
 		if (mat.values.length != this.values.length | mat.values[0].length != this.values[0].length) {
 			System.err.println("Add not possible for differently sized matrices! Returning null.");
 			return null;
 		}
 		float[][] values = new float[mat.values.length][mat.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = values[i][j] + mat.values[i][j];
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Adds a value to all matrix elements.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public Matrix add(float value) {
 		float[][] values = new float[this.values.length][this.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = values[i][j] + value;
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Subtracts one matrix from another. Every element will be subtracted
 	 * component wise.
 	 * 
 	 * @param mat
 	 * @return
 	 */
 	public Matrix subtract(Matrix mat) {
 		if (mat.values.length != this.values.length | mat.values[0].length != this.values[0].length) {
 			System.err.println("Add not possible for differently sized matrices! Returning null.");
 			return null;
 		}
 		float[][] values = new float[mat.values.length][mat.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] - mat.values[i][j];
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Subtracts a value from all matrix elements.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public Matrix subtract(float value) {
 		float[][] values = new float[this.values.length][this.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] - value;
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Multiplies each element of the first matrix with each element of the
 	 * second matrix in a component wise manner.
 	 * 
	 * @param value
 	 * @return
 	 */
 	public Matrix componentMultiply(Matrix mat) {
 		if (mat.values.length != this.values.length | mat.values[0].length != this.values[0].length) {
 			System.err.println("Add not possible for differently sized matrices! Returning null.");
 			return null;
 		}
 		float[][] values = new float[mat.values.length][mat.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] * mat.values[i][j];
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Multiplies each element of the matrix with a value.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public Matrix multiply(float value) {
 		float[][] values = new float[this.values.length][this.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] * value;
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Divides each element of the first matrix with each element of the second
 	 * matrix in a component wise manner.
 	 * 
	 * @param value
 	 * @return
 	 */
 	public Matrix componentDivide(Matrix mat) {
 		if (mat.values.length != this.values.length | mat.values[0].length != this.values[0].length) {
 			System.err.println("Add not possible for differently sized matrices! Returning null.");
 			return null;
 		}
 		float[][] values = new float[mat.values.length][mat.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] / mat.values[i][j];
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Divides each element of the matrix with a value.
 	 * 
 	 * @param value
 	 * @return
 	 */
 	public Matrix componentDivide(float value) {
 		float[][] values = new float[this.values.length][this.values[0].length];
 		for (int i = 0; i < this.values.length; i++) {
 			for (int j = 0; j < this.values[0].length; j++) {
 				values[i][j] = this.values[i][j] / value;
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * Adds every element of the matrix together and returns the sum.
 	 * 
 	 * @return
 	 */
 	public float sum() {
 		float result = 0;
 		for (int i = 0; i < values.length; i++) {
 			for (int j = 0; j < values[0].length; j++) {
 				result += values[i][j];
 			}
 		}
 		return result;
 	}
 
 	/**
 	 * @return Returns the determinant of the matrix.
 	 */
 	public float getDeterminant() {
 		int n = getSize()[0];
 		if (n != getSize()[1]) {
 			System.err.println("Determinant only possible for square matrices. Returning NaN.");
 			return Float.NaN;
 		}
 		if (n == 2) {
 			return getValue(0, 0) * getValue(1, 1) - getValue(1, 0) * getValue(0, 1);
 		}
 		if (n == 3) {
 			return (getValue(0, 0) * getValue(1, 1) * getValue(2, 2))
 					+ (getValue(0, 1) * getValue(1, 2) * getValue(2, 0))
 					+ (getValue(0, 2) * getValue(1, 0) * getValue(2, 1))
 					- (getValue(0, 2) * getValue(1, 1) * getValue(2, 0))
 					- (getValue(0, 1) * getValue(1, 0) * getValue(2, 2))
 					- (getValue(0, 0) * getValue(1, 2) * getValue(2, 1));
 		}
 		float value = 0;
 		int j = 0;
 		for (int i = 0; i < n; i++) {
 			value += parity(i, j) * getValue(i, j)
 					* this.removeRow(j).removeColumn(i).getDeterminant();
 		}
 
 		return value;
 	}
 
 	/**
 	 * @return Returns the adjunct of the matrix.
 	 */
 	public Matrix getAdjunct() {
 		int n = getSize()[0];
 		if (n != getSize()[1]) {
 			System.err.println("Determinant only possible for square matrices. Returning NaN.");
 			return null;
 		}
 		float[][] values = new float[this.values.length][this.values[0].length];
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				values[j][i] = parity(i, j) * this.removeRow(j).removeColumn(i).getDeterminant();
 			}
 		}
 		return mat(values);
 	}
 
 	/**
 	 * @return Returns the inverse of the matrix.
 	 */
 	public Matrix getInverse() {
 		float det = this.getDeterminant();
 		if (det != 0)
 			return this.getAdjunct().multiply(1 / det);
 		return null;
 	}
 }
