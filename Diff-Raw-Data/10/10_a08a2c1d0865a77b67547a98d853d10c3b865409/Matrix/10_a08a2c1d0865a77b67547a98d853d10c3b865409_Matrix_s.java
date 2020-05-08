 package com.lucasazzola.game.graphics;
 
 import java.util.Arrays;
 
 import com.lucasazzola.game.exception.MatrixIndexOutOfBoundsException;
 
 /**
  * Three dimensional Matrix class
  * 
  * @author Lucas Azzola
  * @since 16/8/2012
  */
 public class Matrix {
 
 	/**
 	 * Public Matrix
 	 */
	// R C
 	public float[][] m;
 
 	/**
 	 * Constructs a 3x3 matrix of values 0
 	 */
 	public Matrix() {
 		this.m = new float[3][3];
 		setElementsToValue(m, 0);
 	}
 
 	/**
 	 * Constructs a 3x3 matrix of values value
 	 * 
 	 * @param value
 	 *            the value to assign to all cells
 	 */
 	public Matrix(float value) {
 		this.m = new float[3][3];
 		setElementsToValue(m, value);
 	}
 
 	/**
 	 * Constructs a matrix with specified values
 	 * 
 	 * @param m11
 	 *            Matrix position m11
 	 * @param m12
 	 *            Matrix position m12
 	 * @param m13
 	 *            Matrix position m13
 	 * @param m21
 	 *            Matrix position m21
 	 * @param m22
 	 *            Matrix position m22
 	 * @param m23
 	 *            Matrix position m23
 	 * @param m31
 	 *            Matrix position m31
 	 * @param m32
 	 *            Matrix position m32
 	 * @param m33
 	 *            Matrix position m33
 	 */
 	public Matrix(float m11, float m12, float m13, float m21, float m22,
 			float m23, float m31, float m32, float m33) {
 		this.m = new float[3][3];
 
 		m[0][0] = m11;
 		m[0][1] = m12;
 		m[0][2] = m13;
 
 		m[1][0] = m21;
 		m[1][1] = m22;
 		m[1][2] = m23;
 
 		m[2][0] = m31;
 		m[2][1] = m32;
 		m[2][2] = m33;
 	}
 
 	/**
 	 * Constructs a matrix given a matrix array
 	 * 
 	 * @param m
 	 *            the matrix array
 	 * @throws MatrixIndexOutOfBoundsException
 	 *             when the array is not 3x3
 	 */
 	public Matrix(float[][] m) throws MatrixIndexOutOfBoundsException {
 		if (m.length != 3 || m[0].length != 3)
 			throw new MatrixIndexOutOfBoundsException();
 		this.m = m.clone();
 	}
 
 	/**
 	 * Constructs a scaling matrix from a Vector3
 	 * 
 	 * @param vector
 	 *            the vector3
 	 */
 	public Matrix(Vector3 vector) {
 		this.m = fromVector(vector);
 	}
 
 	/**
 	 * Constructs a matrix from 3 (column) Vector3s
 	 * 
 	 * @param v1
 	 *            the first column vector
 	 * @param v2
 	 *            the second column vector
 	 * @param v3
 	 *            the third column vector
 	 */
 	public Matrix(Vector3 v1, Vector3 v2, Vector3 v3) {
 		this.m = fromVectorsCols(v1, v2, v3);
 	}
 
 	/**
 	 * Constructs a Matrix given another Matrix
 	 * 
 	 * @param matrix
 	 */
 	public Matrix(Matrix matrix) {
 		this.m = matrix.m.clone();
 	}
 
 	/**
 	 * Sets all elements in m to a single value
 	 * 
 	 * @param m
 	 *            the matrix to set elements in
 	 * @param value
 	 *            the value to set
 	 */
 	private void setElementsToValue(float[][] m, float value) {
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[0].length; j++) {
 				m[i][j] = value;
 			}
 		}
 	}
 
 	/**
 	 * Increments this by another matrix
 	 * 
 	 * @param matrix
 	 *            the matrix to increment this by
 	 * @return this, for chaining
 	 */
 	public Matrix increment(Matrix matrix) {
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[0].length; j++) {
 				m[i][j] += matrix.m[i][j];
 			}
 		}
 		return this;
 	}
 
 	/**
 	 * Add two matrices
 	 * 
 	 * @param matrix
 	 *            the first Matrix to add
 	 * @param matrix
 	 *            the second Matrix to add
 	 * @return A new matrix which is the sum of the other two
 	 */
 	public static Matrix add(Matrix m1, Matrix m2) {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
 				out.m[i][j] = m1.m[i][j] + m2.m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Add two matrices
 	 * 
 	 * @param m1
 	 *            the first Matrix
 	 * @param m2
 	 *            the matrix to add
 	 * @param out
 	 *            the resulting Matrix, values will be assigned to this
 	 * @return the out param, for chainability
 	 */
 	public static Matrix add(Matrix m1, Matrix m2, Matrix out) {
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
 				out.m[i][j] = m1.m[i][j] + m2.m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Subtract one matrix from another
 	 * 
 	 * @param m1
 	 *            the first Matrix
 	 * @param m2
 	 *            the matrix to subtract
 	 * @return A new matrix which is m1 - m2
 	 */
 	public static Matrix subtract(Matrix m1, Matrix m2) {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
 				out.m[i][j] = m1.m[i][j] - m2.m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Subtract one matrix from another
 	 * 
 	 * @param m1
 	 *            the first Matrix
 	 * @param m2
 	 *            the matrix to subtract
 	 * @param out
 	 *            the resulting Matrix, values will be assigned to this
 	 * @return the out param, for chainability
 	 */
 	public static Matrix subtract(Matrix m1, Matrix m2, Matrix out) {
 		for (int i = 0; i < m2.m.length; i++) {
 			for (int j = 0; j < m2.m[0].length; j++) {
 				out.m[i][j] = m1.m[i][j] - m2.m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Gets the negated matrix
 	 * 
 	 * @return the negated matrix
 	 */
 	public Matrix negate() {
 		Matrix out = new Matrix();
 		for (int i = 0; i < m.length; i++) {
 			for (int j = 0; j < m[0].length; j++) {
 				out.m[i][j] = -m[i][j];
 			}
 		}
 		return out;
 	}
 
 	/**
 	 * Negates the Matrix passed, and returns itself
 	 * 
 	 * @param matrix
 	 *            the matrix to negate
 	 * @return the the reference to the matrix passed
 	 */
 	public static Matrix negate(Matrix matrix) {
 		for (int i = 0; i < matrix.m.length; i++) {
 			for (int j = 0; j < matrix.m[0].length; j++) {
 				matrix.m[i][j] = -matrix.m[i][j];
 			}
 		}
 		return matrix;
 	}
 
 	/**
 	 * Get a (scaling) matrix array from a Vector
 	 * 
 	 * @param vector
 	 *            the vector
 	 * @return the matrix {{x 0 0} {0 y 0} {0 0 z}}
 	 */
 	public static float[][] fromVector(Vector3 vector) {
 		float[][] m = { { vector.x, 0, 0 }, { 0, vector.y, 0 },
 				{ 0, 0, vector.z }, };
 		return m;
 	}
 
 	/**
 	 * Get a matrix array from three vectors, where a vector represents a single
 	 * column
 	 * 
 	 * @param v1
 	 *            the first column
 	 * @param v2
 	 *            the second column
 	 * @param v3
 	 *            the third column
 	 * @return a matrix array
 	 */
 	public static float[][] fromVectorsCols(Vector3 v1, Vector3 v2, Vector3 v3) {
 		float[][] m = { { v1.x, v2.x, v3.x }, { v1.y, v2.y, v3.y },
 				{ v1.z, v2.z, v3.z }, };
 		return m;
 	}
 
 	/**
 	 * Get a matrix array from three vectors, where a vector represents a single
 	 * row
 	 * 
 	 * @param v1
 	 *            the first row
 	 * @param v2
 	 *            the second row
 	 * @param v3
 	 *            the third row
 	 * @return a matrix array
 	 */
 	public static float[][] fromVectorsRows(Vector3 v1, Vector3 v2, Vector3 v3) {
 		float[][] m = { { v1.x, v1.y, v1.z }, { v2.x, v2.y, v2.z },
 				{ v3.x, v3.y, v3.z }, };
 		return m;
 	}
 
 	/**
 	 * Multiply a Vector3 by a 3x3 Matrix
 	 * 
 	 * @param matrix
 	 *            the Matrix
 	 * @return the resulting Vector3
 	 */
 	public Vector3 multiply(Vector3 vector) {
 		Vector3 out = new Vector3();
 		out.x = m[0][0] * vector.x + m[0][1] * vector.y + m[0][2] * vector.z;
 		out.y = m[1][0] * vector.x + m[1][1] * vector.y + m[1][2] * vector.z;
 		out.z = m[2][0] * vector.x + m[2][1] * vector.y + m[2][2] * vector.z;
 		return out;
 	}
 
 	/**
 	 * Multiply a Vector3 by a 3x3 Matrix
 	 * 
 	 * @param matrix
 	 *            the input Matrix
 	 * @param vector
 	 *            the input Vector
 	 * @param out
 	 *            the resulting Vector will be assigned to this vector
 	 * @return out, with the resulting vector values assigned
 	 */
 	public static Vector3 multiply(Matrix matrix, Vector3 vector, Vector3 out) {
 		// M11*V1 + M12*V2 + M13*V3
 		out.x = matrix.m[0][0] * vector.x + matrix.m[0][1] * vector.y
 				+ matrix.m[0][2] * vector.z;
 		// M21*V1 + M22*V2 + M23*V3
 		out.y = matrix.m[1][0] * vector.x + matrix.m[1][1] * vector.y
 				+ matrix.m[1][2] * vector.z;
 		// M31*V1 + M32*V2 + M33*V3
 		out.z = matrix.m[2][0] * vector.x + matrix.m[2][1] * vector.y
 				+ matrix.m[2][2] * vector.z;
 		return out;
 	}
 
 	/**
 	 * Sets the X Scale of the matrix (M11)
 	 * 
 	 * @param xScale
 	 *            the scale to apply
 	 */
 	public void setXScale(float xScale) {
 		m[0][0] = xScale;
 	}
 
 	/**
 	 * Sets the Y Scale of the matrix (M22)
 	 * 
 	 * @param yScale
 	 *            the scale to apply
 	 */
 	public void setYScale(float yScale) {
		m[0][0] = yScale;
 	}
 
 	/**
 	 * Sets the Z Scale of the matrix (M33)
 	 * 
 	 * @param zScale
 	 *            the scale to apply
 	 */
 	public void setZScale(float zScale) {
		m[0][0] = zScale;
 	}
 
 	/**
 	 * Sets all elements other than M11, M22 and M33 to 0.
 	 * 
 	 * @param toThis
 	 *            true to manipulate this, false to clone
 	 * @return a reference to this, or the new matrix
 	 */
 	public Matrix trimToScalarMatrix(boolean toThis) {
 		Matrix temp = toThis ? this : new Matrix(this);
 		temp.m[0][1] = 0;
 		temp.m[0][2] = 0;
 		temp.m[1][0] = 0;
 		temp.m[1][2] = 0;
 		temp.m[2][0] = 0;
 		temp.m[2][1] = 0;
 		return temp;
 	}
 
 	@Override
 	public int hashCode() {
 		return 31 + Arrays.hashCode(m);
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null || getClass() != obj.getClass())
 			return false;
 		Matrix other = (Matrix) obj;
 		if (!Arrays.deepEquals(m, other.m))
 			return false;
 		return true;
 	}
 
 	@Override
 	public String toString() {
 		return "Matrix [" + Arrays.toString(m) + "]";
 	}
 }
