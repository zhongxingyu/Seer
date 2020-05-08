 package matrixLib;
 
 import java.util.Arrays;
 import java.util.LinkedList;
 
 /**
  * Represents a matrix over either R or C
  * @author Bryan Cuccioli
  */
 public class Matrix {
 
 	private ComplexNumber[][] matrix;
 	private int rows, cols;
 	
 	/**
 	 * Constructs the nxn identity matrix
 	 * @param n the number of rows and columns in this identity matrix
 	 */
 	public Matrix(int n) {
 		
 		matrix = new ComplexNumber[n][n];
 		rows = n;
 		cols = n;
 		
 		for (int i = 0; i < n; i++) {
 			for (int j = 0; j < n; j++) {
 				matrix[i][j] = new ComplexNumber((i == j) ? 1 : 0, 0);
 			}
 		}
 	}
 
 	/**
 	 * Constructs the matrix that wraps the array mat[][]
 	 * @param mat the data to go in the matrix
 	 */
 	public Matrix(double[][] mat) {
 		
 		matrix = new ComplexNumber[mat.length][mat[0].length];
 		
 		for (int i = 0; i < mat.length; i++) {
 			for (int j = 0; j < mat[0].length; j++) {
 				matrix[i][j] = new ComplexNumber(mat[i][j], 0);
 			}
 		}
 		
 		rows = mat.length;
 		cols = mat[0].length; // the number of columns in the matrix
 	}
 	
 	/**
 	 * Constructs the matrix that wraps the array mat[][]
 	 * @param mat the data to go in the matrix
 	 * @throws DimensionMismatchException the supplied array does not have valid dimensions
 	 */
 	public Matrix(ComplexNumber[][] mat) throws DimensionMismatchException {
 		
 		if (mat.length == 0 || mat[0].length == 0) {
 			throw new DimensionMismatchException();
 		}
 		
 		matrix = new ComplexNumber[mat.length][mat[0].length];
 		
 		for (int i = 0; i < mat.length; i++) {
 			for (int j = 0; j < mat[0].length; j++) {
 				matrix[i][j] = mat[i][j];
 			}
 		}
 		
 		rows = mat.length;
 		cols = mat[0].length; // the number of columns in the matrix
 	}
 	
 	/**
 	 * Create a new unpopulated r by c matrix
 	 * @param r the number of rows in the matrix
 	 * @param c the number of columns in the matrix
 	 */
 	public Matrix(int r, int c) {
 		
 		matrix = new ComplexNumber[r][c];
 		this.rows = r;
 		this.cols = c;
 		
 		// build the zero matrix
 		for (int i = 0; i < r; i++) {
 			for (int j = 0; j < c; j++) {
 				matrix[i][j] = new ComplexNumber(0, 0);
 			}
 		}
 	}
 
 	/**
 	 * Constructs a matrix from a collection of vectors
 	 * @param vectors the vectors to create the matrix from
 	 * @s DimensionMismatchException the vectors do not all have the same dimension
 	 */
 	public Matrix(Vector[] vectors) throws DimensionMismatchException {
 		
 		this.rows = vectors[0].dim();
 		this.cols = vectors.length;
 		matrix = new ComplexNumber[rows][cols];
 		
 		for (int j = 0; j < cols; j++) {
 			if (j != 0 && vectors[j].dim() != vectors[j-1].dim()) {
 				// the vectors did not all have the same number of coordinates
 				 throw new DimensionMismatchException();
 			}
 			for (int i = 0; i < rows; i++) {
 				matrix[i][j] = vectors[j].getAt(i);
 			}
 		}
 	}
 	
 	/**
 	 * Retrieves the nth vector from the matrix
 	 * @param n the column/vector to get from the matrix
 	 * @return the nth vector from the matrix
 	 */
 	public Vector getVector(int n) {
 		
 		ComplexNumber[] entries = new ComplexNumber[rows()];
 		
 		for (int i = 0; i < rows(); i++) {
 			entries[i] = matrix[i][n];
 		}
 		
 		return new Vector(entries);
 	}
 	
 	/**
 	 * Gets the element at location (r,c) in the matrix
 	 * @param r the row to retrieve the element from
 	 * @param c the column to retrieve the element from
 	 * @throws ArrayIndexOutOfBoundsException trying to access an element out of the bounds of the matrix
 	 * @return the element in the matrix at (r,c)
 	 */
 	public ComplexNumber getAt(int r, int c) throws ArrayIndexOutOfBoundsException {
 		
 		if (r >= rows() || c >= cols()) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		
 		return matrix[r][c];
 	}
 	
 	/**
 	 * Sets the element at (r, c) to something
 	 * @param r the row of the element to set
 	 * @param c the column of the element to set
 	 * @param val the value to set (r, c) to
 	 * @throws ArrayIndexOutOfBoundsException trying to access an element out of the bounds of the matrix
 	 */
 	public void set(int r, int c, ComplexNumber val) throws ArrayIndexOutOfBoundsException {
 		
 		if (r >= rows() || c >= cols()) {
 			throw new ArrayIndexOutOfBoundsException();
 		}
 		
 		matrix[r][c] = val;
 	}
 	
 	/**
 	 * Returns the matrix transpose of this matrix
 	 * @return the transpsoe of this matrix
 	 */
 	public Matrix transpose() {
 		
 		ComplexNumber[][] trans = new ComplexNumber[cols][rows];
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				trans[i][j] = matrix[j][i];
 			}
 		}
 		
 		return new Matrix(trans);
 	}
 	
 	/**
 	 * Returns the conjugate transpose of this matrix
 	 * @return the matrix that is the conjugate transpose of this matrix
 	 */
 	public Matrix conjugateTranspose() {
 		
 		ComplexNumber[][] ct = new ComplexNumber[cols][rows];
 		
 		for (int i = 0; i < cols; i++) {
 			for (int j = 0; j < rows; j++) {
 				ct[i][j] = matrix[j][i].conjugate();
 			}
 		}
 		
 		return new Matrix(ct);
 	}
 	
 	/**
 	 * Returns the number of rows in the matrix
 	 * @return the number of rows
 	 */
 	public int rows() {
 		return rows;
 	}
 	
 	/**
 	 * Returns the number of columns in the matrix
 	 * @return the number of columns
 	 */
 	public int cols() {
 		return cols;
 	}
 
 	/**
 	 * Tells whether the matrix is composed entirely of real numbers
 	 * @return boolean value indicating whether the matrix is real
 	 */
 	public boolean isReal() {
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				if (matrix[i][j].Im() != 0) {
 					return false; // complex entry found
 				}
 			}
 		}
 		return true; // didn't find any complex entries
 	}
 	
 	/**
 	 * Returns the result of multiplying this matrix by m
 	 * @param m the matrix to multiply this one by
 	 * @return the matrix product this*m
 	 * @throws DimensionMismatchException the matrices do not have the right dimensions to be multiplied
 	 */
 	public Matrix multiply(Matrix m) throws DimensionMismatchException {
 		
 		// can only multiply matrices of dimension nxm by mxp
 		if (cols != m.rows()) {
 			 throw new DimensionMismatchException();
 		}
 		
 		ComplexNumber[][] prod = new ComplexNumber[rows()][m.cols()];
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < m.cols(); j++) {
 				prod[i][j] = new ComplexNumber(0,0);
 				for (int k = 0; k < cols; k++) {
 					prod[i][j] = prod[i][j].add(matrix[i][k].multiply(m.getAt(k, j)));
 				}
 			}
 		}
 		
 		return new Matrix(prod);
 	}
 	
 	/**
 	 * Multiplies this matrix by the given vector
 	 * @param v the vector by which to multiply the matrix
 	 * @return the product of this matrix and the given vector
 	 * @throws DimensionMismatchException the vector is not in the domain of this matrix
 	 */
 	public Vector multiply(Vector v) throws DimensionMismatchException {
 		
 		if (this.cols() != v.dim()) {
 			throw new DimensionMismatchException();
 		}
 		
 		Vector prod = new Vector(this.rows());
 		
 		for (int i = 0; i < rows(); i++) {
 			for (int j = 0; j < cols(); j++) {
 				prod.set(i, prod.getAt(i).add(matrix[i][j].multiply(v.getAt(j))));
 			}
 		}
 		
 		return prod;
 	}
 	
 	/**
 	 * Adds this matrix to the given matrix
 	 * @param m the matrix to add to this one
 	 * @return the matrix sum this+m
 	 * @throws DimensionMismatchException the matrices do not have matching dimensions
 	 */
 	public Matrix add(Matrix m) throws DimensionMismatchException {
 		
 		if (m.rows() != rows || m.cols() != cols) {
 			 throw new DimensionMismatchException();
 		}
 		
 		Matrix sum = new Matrix(rows, cols);
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				sum.set(i, j, matrix[i][j].add(m.getAt(i, j)));
 			}
 		}
 		
 		return sum;
 	}
 
 	/**
 	 * Subtracts the given matrix from this one
 	 * @param m the matrix to subtract from this one
 	 * @return the difference between this matrix and the given
 	 * @throws DimensionMismatchException the matrices do not have matching dimensions
 	 */
 	public Matrix subtract(Matrix m) throws DimensionMismatchException {
 		
 		if (m.rows() != rows || m.cols() != cols) {
 			 throw new DimensionMismatchException();
 		}
 		
 		Matrix diff = new Matrix(rows, cols);
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				diff.set(i, j, matrix[i][j].subtract(m.getAt(i, j)));
 			}
 		}
 		
 		return diff;
 	}
 	
 	/**
 	 * Multiplies each element of the matrix by a complex number
 	 * @param factor the scalar to multiply the matrix by
 	 * @return the matrix factor*this
 	 */
 	public Matrix multiply(ComplexNumber factor) {
 		
 		Matrix scaled = new Matrix(rows, cols);
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				scaled.set(i, j, matrix[i][j].multiply(factor));
 			}
 		}
 		
 		return scaled;
 	}
 
 	/**
 	 * Multiplies each element of the matrix by a real scalar
 	 * @param factor the scalar to multiply the matrix by
 	 * @return the matrix factor*this
 	 */
 	public Matrix multiply(double factor) {
 		
 		return multiply(new ComplexNumber(factor, 0));
 	}
 
 	/**
 	 * Computes an orthonormal basis for the column space of the matrix
 	 * using the numerically stable modified Gram-Schmidt procedure
 	 * @return the matrix whose columns are an orthonormal basis for the column space of the matrix
 	 */
 	public Matrix orthonormalize() {
 		
 		Vector[] result = new Vector[cols()];
 		for (int j = 0; j < cols(); j++) {
 			result[j] = this.getVector(j);
 		}
 		
 		for (int j = 0; j < cols(); j++) {
 			result[j] = result[j].normalize();
 			
 			for (int i = j+1; i < cols(); i++) {
 				result[i] = result[i].subtract(result[j].multiply(result[j].dot(result[i])).toVector());
 			}
 		}
 		
 		return new Matrix(result);
 	}
 	
 	/**
 	 * Gets the underlying data array for this matrix
 	 * @return the underlying data array for this matrix
 	 */
 	protected ComplexNumber[][] getData() {
 		
 		return this.matrix;
 	}
 	
 	/**
 	 * Gets the canonical basis for the image of the matrix
 	 * @return the vectors forming a basis for the image of the matrix
 	 */
 	public LinkedList<Vector> imageBasis() {
 		
 		Matrix rref = this.rref();
 		LinkedList<Vector> basis = new LinkedList<Vector>();
 		
 		for (int i = 0; i < rows(); i++) {
 			for (int j = 0; j < cols(); j++) {
 				if (!rref.getAt(i,j).isZero()) {
 					basis.add(getVector(j));
 					break;
 				}
 			}
 		}
 		
 		return basis;
 	}
 	
 	/**
 	 * Gets the singular values of the matrix
 	 * @return an array of singular values of the matrix, sorted in descending order by absolute value
 	 */
 	public ComplexNumber[] singularValues() {
 		
 		ComplexNumber[] sv = new ComplexNumber[cols()];
 		Matrix sm = new Matrix(this.conjugateTranspose().multiply(this).getData());
 		ComplexNumber[] evals = SquareMatrixOps.eigenvalues(sm);
 		
 		for (int i = 0; i < evals.length; i++) {
 			sv[i] = evals[i].sqrt();
 		}
 		
 		Arrays.sort(sv); // uses a tuned quicksort
 		return sv;
 	}
 	
 	/**
 	 * Converts this matrix to a vector through the canonical isomorphism
 	 * (reading the elements top to bottom, left to right)
 	 * @return a vector with the elements from this matrix
 	 */
 	public Vector toVector() {
 		
 		Vector v = new Vector(rows * cols);
 		
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				v.set(i*cols + j, getAt(i, j));
 			}
 		}
 		
 		return v;
 	}
 	
 	// helper method for rref() and determinant()
 	// type=0: rref; type=1: determinant
 	protected Matrix rref_stable(int type) {
 		
 		Matrix rref = new Matrix(matrix);
 		ComplexNumber det = new ComplexNumber(1,0);
 		
 		int i = 0, j = 0;
 		while (i < rows() && j < cols()) {
 			// find pivot in column j, starting in row i
 			int maxi = i;
 			for (int k = i+1; k < rows(); k++) {
 				if (rref.getAt(k,j).abs() > rref.getAt(maxi,j).abs()) {
 					maxi = k;
 				}
 			}
 			if (!rref.getAt(maxi,j).isZero()) {
 				// swap rows i and maxi
 				if (i != maxi) {
 					for (int a = 0; a < cols(); a++) {
 						ComplexNumber temp = rref.getAt(i, a);
 						rref.set(i, a, rref.getAt(maxi, a));
 						rref.set(maxi, a, temp);
 					}
 					// swapping rows negates the determinant
 					det = det.negative();
 				}
 				
 				// divide row i by [i,j]
 				ComplexNumber div = rref.getAt(i, j);
 				for (int a = 0; a < cols(); a++) {
 					rref.set(i, a, rref.getAt(i, a).divide(div));
 				}
 				// scaling the matrix scales the determinant
 				det = det.multiply(div);
 				
 				for (int u = i+1; u < rows(); u++) {
 					// subtract row i times [u,j] from row u - det unchanged
 					ComplexNumber c = rref.getAt(u, j); 
 					for (int a = 0; a < cols(); a++) {
 						rref.set(u, a, rref.getAt(u, a).subtract(rref.getAt(i, a).multiply(c)));
 					}
 				}
 				i++;
 			}
 			j++;
 		}
 		
 		// use back substitution to convert to rref
 		for (int r = rows()-1; r > 0; r--) {
 			// find the pivot column
 			int pivot = -1;
 			for (int k = 0; k < cols(); k++) {
 				if (!rref.getAt(r,k).isZero()) {
 					pivot = k;
 					break;
 				}
 			}
 			if (pivot != -1) { // if the row is empty it won't help
 				for (int u = 0; u < r; u++) {
 					ComplexNumber c_first = rref.getAt(u, pivot); // since [r,pivot]=1
 					rref.set(u, pivot, new ComplexNumber(0, 0));
 					for (int a = pivot+1; a < cols(); a++) {
 						rref.set(u, a, rref.getAt(u,a).subtract(c_first.multiply(rref.getAt(r, a))));
 					}
 				}
 			}
 		}
 
 		if (type == 0) {
 			return rref;
 		}
 		else {
 			if (Pattern.isIdentity(rref)) {
 				rref.set(0, 0, det);
 			}
 			else {
 				rref.set(0, 0, new ComplexNumber(0, 0));
 			}
 		}
 		
 		return rref;
 	}
 	
 	// helper method for public rref()
 	// used for computing either rref or the determinant
 	// type=0: rref, type=1: det
 	protected Matrix rref(int type) {
 		
 		Matrix rref = new Matrix(matrix);
 		ComplexNumber det = new ComplexNumber(1,0);
 		
 		int lead = 0;
 		for (int r = 0; r < rows(); r++) {
 			if (lead >= cols()) {
 				break;
 			}
 			int i = r;
 			while (rref.getAt(i, lead).isZero()) {
 				i++;
 				if (i == rows()) {
 					i = r;
 					lead++;
 					if (lead == cols()) {
 						if (type == 1) {
 							if (Pattern.isIdentity(rref)) {
 								rref.set(0, 0, det);
 							}
 							else {
 								rref.set(0, 0, new ComplexNumber(0, 0));
 							}
 						}
 						return rref;
 					}
 				}
 			}
 			
 			// swap rows i and r
 			if (i != r) {
 				for (int a = 0; a < cols(); a++) {
 					ComplexNumber temp = rref.getAt(i, a);
 					rref.set(i, a, rref.getAt(r, a));
 					rref.set(r, a, temp);
 				}
 				// swapping rows negates the determinant
 				det = det.multiply(-1);
 			}
 			
 			// divide row r by rref[r][lead]
 			ComplexNumber div = rref.getAt(r, lead);
 			for (int a = 0; a < cols(); a++) {
 				rref.set(r, a, rref.getAt(r, a).divide(div));
 			}
 			// scaling the matrix scales the determinant
 			det = det.multiply(div);
 			
 			for (int j = 0; j < rows(); j++) {
 				if (j != r) {
 					// subtract row r * -rref[j][lead] from row j
 					ComplexNumber c = rref.getAt(j, lead); 
 					for (int a = 0; a < cols(); a++) {
 						rref.set(j, a, rref.getAt(j, a).subtract(rref.getAt(r, a).multiply(c)));
 					}
 				}
 			}
 			lead++;
 		}
 		
 		if (type == 0) {
 			return rref;
 		}
 		else {
 			if (Pattern.isIdentity(rref)) {
 				rref.set(0, 0, det);
 			}
 			else {
 				rref.set(0, 0, new ComplexNumber(0, 0));
 			}
 		}
 		
 		return rref;
 	}
 	
 	/**
 	 * Computes the row-reduced echelon form of this Matrix by
 	 * Gaussian elimination with partial pivoting for numerical stability
 	 * @return the row-reduced echelon form of this matrix
 	 */
 	public Matrix rref() {
 		
 		return rref(0);
 		//return rref_stable(0);
 	}
 	
 	/**
 	 * Returns the rank of this matrix (the dimension of its image)
 	 * @return the rank of this matrix 
 	 */
 	public int rank() {
 		
 		int rk = 0;
 		
 		for (ComplexNumber[] row : this.rref().getData()) {
 			boolean all_zero = true;
 			for (ComplexNumber z : row) {
 				if (!z.isZero()) {
 					all_zero = false;
 					break;
 				}
 			}
 			if (!all_zero) {
 				rk++;
 			}
 		}
 		
 		return rk;
 	}
 	
 	/**
 	 * Returns the nullity of this matrix (the dimension of its kernel)
 	 * @return the nullity of this matrix
 	 */
 	public int nullity() {
 		
 		return cols() - rank();
 	}
 
 	/**
 	 * Computes a string representation of this matrix
 	 * @return a string representation of this matrix
 	 */
 	public String toString() {
 		
 		String mstr = "[";
 
 		for (int i = 0; i < rows; i++) {
 			if (i != 0) mstr += " "; // align left margin horizontally
 			mstr += "[";
 
 			for (int j = 0; j < cols; j++) {
 				mstr += getAt(i, j).toString();
 				if (j != cols - 1) mstr += ", ";
 				else {
 					mstr += "]";
 					if (i == rows - 1) mstr += "]";
 					else mstr += "\n";
 				}
 			}
 		}
 		
 		return mstr;
 	}
 	
 	/**
 	 * Tells if two matrices are equal (if all of their corresponding elements are equal)
 	 * @param m the matrix to compare this one too
 	 * @return whether the two matrices are equal
 	 */
 	public boolean equals(Matrix m) {
 		
 		// have to have matching dimension to be equal
 		if (rows != m.rows() || cols != m.cols()) {
 			return false;
 		}
 		
 		// each element has to match for them to be equal
 		for (int i = 0; i < rows; i++) {
 			for (int j = 0; j < cols; j++) {
 				if (!m.getAt(i, j).equals(matrix[i][j])) {
 					return false;
 				}
 			}
 		}
 		
 		return true; // if it got this far, they are equal
 	}
 }
