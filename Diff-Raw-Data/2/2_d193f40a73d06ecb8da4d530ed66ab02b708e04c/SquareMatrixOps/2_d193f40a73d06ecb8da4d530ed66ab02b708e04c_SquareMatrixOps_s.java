 package matrixLib;
 
 /**
  * A library of common operations that are restricted to square matrices
  * @author Bryan Cuccioli
  */
 
 import matrixLib.exception.*;
 
 public class SquareMatrixOps {
 
 	/**
 	 * This is a numericall stable algorithm for computing the inverse of a lower triangular matrix.
 	 * It is called by inverse(), and has that the given matrix is square, triangular and invertible
 	 * as a precondition. It is numerically stable.
 	 * @param tri the square, nonsingular, lower triangular matrix to invert
 	 * @return the inverse of the given matrix, which is also lower triangular
 	 */
 	private static Matrix inverse_lt(Matrix tri) {
 		ComplexNumber[][] tri_inv = new ComplexNumber[tri.rows()][tri.rows()];
 
 		for (int j = 0; j < tri.rows(); j++) {
 			tri_inv[0][j] = new ComplexNumber(0,0);
 		}
 		
 		// compute the inverse of the triangular matrix, which is easier
 		tri_inv[0][0] = tri.getAt(0,0).reciprocal();
 		for (int i = 1; i < tri.rows(); i++) {
 			// just compute the reciprocal of the diagonal elements
 			tri_inv[i][i] = tri.getAt(i,i).reciprocal(); // nonsingular => nonzero
 			
 			for (int j = 0; j < i; j++) {
 				// found by solving L L^-1 = I
 				ComplexNumber sum = new ComplexNumber(0,0);
 				for (int k = j; k < i; k++) {
 					sum = sum.add(tri.getAt(i,k).multiply(tri_inv[k][j]));
 				}
 				tri_inv[i][j] = sum.divide(tri.getAt(i,i)).negative();
 			}
 			// inverse of lower triangular is lower triangular, so fill the rest with 0's
 			for (int j = i+1; j < tri.rows(); j++) {
 				tri_inv[i][j] = new ComplexNumber(0,0);
 			}
 		}
 		
 		return new Matrix(tri_inv);
 	}
 	
 	/**
 	 * Computes the inverse of the matrix via Gauss-Jordan elimination
 	 * @param m the matrix of which the inverse is computed
 	 * @return the corresponding inverse matrix
 	 * @throws NotSquareException the matrix is not square
 	 * @throws SingularMatrixException the given matrix is singular/not invertible
 	 */
 	public static Matrix inverse(Matrix m) throws NotSquareException, SingularMatrixException {
 		
 		if (m.rows() != m.cols()) {
 			throw new NotSquareException();
 		}
 
 		// test for common easy cases
 		if (Pattern.isUpperTriangular(m)) {
 			for (int i = 0; i < m.rows(); i++) {
 				// det. of tri. matrix is product of diagonals
 				if (m.getAt(i,i).isZero()) { // if there's a 0, det=0
 					throw new SingularMatrixException();
 				}
 			}
			return inverse_lt(m.transpose());
 		}
 		else if (Pattern.isLowerTriangular(m)) {
 			for (int i = 0; i < m.rows(); i++) {
 				// det. of tri. matrix is product of diagonals
 				if (m.getAt(i,i).isZero()) { // if there's a 0, det=0
 					throw new SingularMatrixException();
 				}
 			}
 			return inverse_lt(m);
 		}
 		// can factor a Hermitian matrix to make inversion easy
 		else if (Pattern.isHermitian(m)) {
 			boolean cholesky_failed = false;
 			Matrix tri = null;
 			try {
 				tri = Factorization.choleskyDecompose(m);
 			}
 			catch (NotPositiveDefiniteException e) {
 				cholesky_failed = true;
 			}
 			Matrix l_inv = inverse_lt(tri);
 			// if the Cholesky factorization didn't work, have to do normal inversion
 			if (!cholesky_failed) {
 				return l_inv.conjugateTranspose().multiply(l_inv);
 			}
 		}
 		
 		// otherwise we have to use the generic algorithm
 		ComplexNumber[][] augmented = new ComplexNumber[m.rows()][m.cols()*2];
 		// row reduce the augmented matrix and recover the inverse on the right
 		for (int i = 0; i < m.rows(); i++) {
 			for (int j = 0; j < m.cols()*2; j++) {
 				if (j < m.cols()) {
 					augmented[i][j] = m.getAt(i, j);
 				}
 				else {
 					augmented[i][j] = new ComplexNumber((i==j-m.cols())?1:0,0);
 				}
 			}
 		}
 		
 		Matrix aug_rref = (new Matrix(augmented)).rref();
 		
 		// if the left wasn't reduced to Id, inverse doesn't exist
 		for (int i = 0; i < m.rows(); i++) {
 			boolean all_zero = true;
 			for (int j = 0; j < m.cols(); j++) {
 				if (!aug_rref.getAt(i, j).isZero()) {
 					all_zero = false;
 					break;
 				}
 			}
 			if (all_zero) {
 				// there was an all 0 row; not identity
 				throw new SingularMatrixException();
 			}
 		}
 		
 		// recover the inverse matrix from the right side
 		ComplexNumber[][] inv = new ComplexNumber[m.rows()][m.cols()];
 		for (int i = 0; i < m.rows(); i++) {
 			for (int j = 0; j < m.cols(); j++) {
 				inv[i][j] = aug_rref.getAt(i, j+m.cols());
 			}
 		}
 		
 		return new Matrix(inv);
 	}
 	
 	/**
 	 * Returns the determinant of this matrix in O(n^3) time complexity
 	 * @param m the matrix whose determinant is computed
 	 * @return the determinant of the matrix
 	 * @throws NotSquareException the matrix is not square
 	 */
 	public static ComplexNumber determinant(Matrix m) throws NotSquareException {
 
 		if (m.rows() != m.cols()) {
 			throw new NotSquareException();
 		}
 		
 		// try shortcuts for common sizes first
 		if (m.rows() == 1) {
 			return m.getAt(0,0);
 		}
 		else if (m.rows() == 2) {
 			return m.getAt(0,0).multiply(m.getAt(1,1)).subtract(m.getAt(1,0).multiply(m.getAt(0,1)));
 		}
 		else { // rref(1) encodes the determinant in the upper left corner of the matrix
 			return m.rref(1).getAt(0, 0);
 		}
 	}
 	
 	/**
 	 * Returns the trace of the matrix, the sum of the diagonal elements
 	 * @param m the matrix whose trace to compute
 	 * @return the trace of the matrix
 	 * @throws NotSquareException the given matrix is not square
 	 */
 	public static ComplexNumber trace(Matrix m) throws NotSquareException {
 		
 		if (m.rows() != m.cols()) { // matrix must be square to find trace
 			throw new NotSquareException();
 		}
 		
 		// simply sum the diagonals
 		ComplexNumber tr = new ComplexNumber(0, 0);
 		for (int i = 0; i < m.rows(); i++) {
 			tr = tr.add(m.getAt(i, i));
 		}
 		
 		return tr;
 	}
 	
 	/**
 	 * This is a helper method for the QR algorithm. It finds the 'form' of the matrix, which
 	 * is either a row of zeros on the bottom except the rightmost element, two rows of zeros
 	 * on the bottom except for the rightmost 2x2 block, or neither
 	 * @param m the matrix whose 'form' we compute
 	 * @return 1, 2 or 0, with respect to the definition given in the description
 	 */
 	private static int getForm(Matrix m) {
 		
 		ComplexNumber[][] entries = m.getData();
 		int n = m.rows() - 2;
 		
 		// see if it is of form 2
 		boolean form2 = true;
 		for (int i = 0; i < n; i++) {
 			if (!entries[n][i].isZero() || !entries[n+1][i].isZero()) {
 				form2 = false; // this violates it being form 2
 			}
 		}
 		if (form2) return 2;
 		
 		n++; // we subsequently check only the bottom row
 		for (int i = 0; i < n; i++) {
 			if (!entries[n][i].isZero()) {
 				// isn't 1, and we already know it isn't 2
 				return 0;
 			}
 		}
 		
 		// no evidence against 1, but we know it isn't 2
 		return 1;
 	}
 
 	/**
 	 * Returns a list of the eigenvalues of the matrix by computing QR similarity transforms until
 	 * the matrix converges to a triangular matrix (i.e. implements the shifted QR algorithm)
 	 * @param m the matrix whose eigenvalues we compute
 	 * @return an array containing the eigenvalues of the matrix
 	 * @throws NotSquareException the given matrix is not square 
 	 */
 	public static ComplexNumber[] eigenvalues(Matrix m) {
 		
 		if (m.rows() != m.cols()) { // only square matrices have eigenvalues
 			throw new NotSquareException();
 		}
 		
 		Matrix curr = Pattern.hessenberg(m); // makes it converge sooner
 		ComplexNumber[] evals = new ComplexNumber[m.rows()];
 		int pos = 0; // where we are in the evals array
 		int size = m.rows(); // the size of the current array
 		
 		while (size > 2) { // once the matrix is 2x2, can just use the explicit form
 			Matrix shift = Pattern.diag(curr.getAt(size-1,size-1),size);
 			Matrix[] qr = Factorization.QRDecompose(curr.subtract(shift));
 			curr = qr[1].multiply(qr[0]).add(shift); // similarity transform, preserves spectrum
 			
 			int form = getForm(curr);
 			if (form == 1) { // the lower right element is an eigenvalue
 				size--; // we will operate on the upper left block of size one less
 				evals[pos++] = curr.getAt(size, size);
 			}
 			else if (form == 2) {
 				size -= 2; // operate on the upper left block of size two less
 				// apply formula for eigenvalues of a 2x2 matrix
 				ComplexNumber left = curr.getAt(size,size).add(curr.getAt(size+1,size+1));
 				ComplexNumber leftprime = curr.getAt(size,size).subtract(curr.getAt(size+1,size+1));
 				ComplexNumber right = curr.getAt(size,size+1).multiply(curr.getAt(size+1,size)).multiply(4).add(leftprime.multiply(leftprime)).sqrt();
 				evals[pos++] = left.add(right).multiply(.5);
 				evals[pos++] = left.subtract(right).multiply(.5);
 			}
 			if (size == 0) { // we have fully reduced the matrix
 				return evals;
 			}
 			if (form != 0) { // we found {1|2} eigenvalues, so reduce
 				// build the new matrix to operate on if an eval was found
 				ComplexNumber[][] newmat = new ComplexNumber[size][size];
 				for (int i = 0; i < size; i++) {
 					for (int j = 0; j < size; j++) {
 						newmat[i][j] = curr.getAt(i,j);
 					}
 				}
 				curr = new Matrix(newmat);
 			}
 		}
 		
 		if (size == 1) {
 			evals[pos++] = curr.getAt(0,0); // there is a 1x1 matrix remaining
 		}
 		else {
 			// size=2 in this branch; apply explicit 2x2 formula
 			ComplexNumber left = curr.getAt(0,0).add(curr.getAt(1,1));
 			ComplexNumber leftprime = curr.getAt(0,0).subtract(curr.getAt(1,1));
 			ComplexNumber right = curr.getAt(0,1).multiply(curr.getAt(1,0)).multiply(4).add(leftprime.multiply(leftprime)).sqrt();
 			evals[pos++] = left.add(right).multiply(.5);
 			evals[pos++] = left.subtract(right).multiply(.5);
 		}
 		
 		return evals;
 	}
 	
 	/**
 	 * Computes the normalized eigenvectors
 	 * @param m the matrix whose eigenvectors are computed
 	 * @return an array of normalized eigenvectors for the matrix
 	 * @throws NotSquareException the given matrix is not square
 	 */
 	public static Vector[] eigenvectors(Matrix m) throws NotSquareException {
 
 		if (m.rows() != m.cols()) { // needs to be square to have eigenvectors
 			throw new NotSquareException();
 		}
 		
 		return eigenvectors(m, SquareMatrixOps.eigenvalues(m));
 	}
 	
 	/**
 	 * Computes the normalized eigenvectors of the matrix corresponding to certain eigenvalues
 	 * @param m the matrix whose eigenvectors are being computed 
 	 * @param evals the list of eigenvalues to compute the associated eigenvectors of
 	 * @return an array of normalized eigenvectors for the matrix
 	 * @throws NotSquareException the given matrix is not square
 	 */
 	public static Vector[] eigenvectors(Matrix m, ComplexNumber[] evals) {
 		
 		if (m.rows() != m.cols()) { // check if square
 			throw new NotSquareException();
 		}
 		
 		Vector[] evecs = new Vector[evals.length];
 		int vec_pos = 0;
 		
 		// start with an initial guess vector of magnitude 1
 		ComplexNumber[] test_vec = new ComplexNumber[m.cols()];
 		double val = 1.0/Math.sqrt(m.cols()); // want magnitude=1
 		for (int i = 0; i < test_vec.length; i++) {
 			test_vec[i] = new ComplexNumber(val, 0);
 		}
 		
 		for (ComplexNumber ev : evals) { // compute eigenvector for each given eval
 			Matrix diag = null;
 			boolean inverted = false; // have we successfully inverted the matrix yet?
 			while (!inverted) {
 				inverted = true; // assume the matrix will invert successfully
 				try {
 					diag = SquareMatrixOps.inverse((new Matrix(m.getData())).subtract(Pattern.diag(ev, m.rows())));
 				}
 				catch (SingularMatrixException e) {
 					inverted = false; // the matrix was singular, so try again
 				}
 				if (!inverted) {
 					ev = ev.multiply(.999); // try again with a fudged eigenvalue
 				}
 			}
 			
 			Vector prev = new Vector(test_vec);
 			Vector next = diag.multiply(prev).normalize();
 			// continue applying to vectors until they stop changing
 			do {
 				prev = next; // for comparison
 				next = diag.multiply(prev).normalize();
 			} while (!next.equals(prev));
 			evecs[vec_pos++] = next; // found a normalized eigenvector
 		}
 		
 		return evecs;
 	}
 	
 	/**
 	 * Computes the value of the matrix raised to a particular power
 	 * @param m the matrix that is raised to the specified power
 	 * @param power the power to which the matrix is raised
 	 * @return the matrix raised to the specified power
 	 * @throws NotSquareException the supplied matrix is not square
 	 */
 	public static Matrix pow(Matrix m, int power) throws NotSquareException {
 		
 		if (m.rows() != m.cols()) {
 			// A^p is only really defined for square A
 			throw new NotSquareException();
 		}
 		
 		if (power == 0) {
 			// a matrix to the zero power is the identity matrix
 			return new Matrix(m.rows());
 		}
 		else if (power < 0) {
 			// A^-p = (A^-1)^p
 			return pow(SquareMatrixOps.inverse(m), -power);
 		}
 		else {
 			// multiply the matrix by itself a number of times
 			Matrix temp = new Matrix(m.getData());
 			for (int i = 0; i < power-1; i++) {
 				temp = temp.multiply(m);
 			}
 			return temp;
 		}
 	}
 }
