 import java.text.DecimalFormat;
 
 
 public class MatrixCalculator {
 
 	
 	/**
 	 * Adds two Matrices together and returns the sum as a new Matrix.
 	 * @param A Matrix
 	 * @param B Matrix
 	 */
 	public Matrix add(Matrix A, Matrix B){
 		if( A.getNumRows() != B.getNumRows() || A.getNumCols() != B.getNumCols() ){
 			throw new IllegalArgumentException("Matrices differ in size.");
 		}
 		int n = A.getNumRows();
 		int m = A.getNumCols();
 		
 		double[][] sum  = new double [n][];
 		for(int i=0; i < n; i++){
 			sum[i] = new double [m];
 		}
 		
 		for(int i=0; i < n; i++){
 			for(int j=0; j < m; j++){
 				sum[i][j] = A.getElement(i, j) + B.getElement(i, j);
 			}
 		}
 		
 		return new Matrix(sum);
 
 	}
 	
 	/**
 	 * Multiplies two Matrices together and returns the sum as a new Matrix.
 	 * @param A Matrix
 	 * @param B Matrix
 	 */
 	public Matrix multiply(Matrix A, Matrix B){
 		if( A.getNumCols() != B.getNumRows()  ){
 			throw new IllegalArgumentException("Number of rows in A is not equal to the number of columns in B.");
 		}
 		
 		int n = A.getNumCols();
 		int m = A.getNumRows();
 		int p = B.getNumCols();
 		double[][] product = new double[m][p];
 		
 
 		for(int i=0; i < m; i++){
 			for(int j=0; j < p; j++){
 				for(int k=0; k < n; k++){
 					product[i][j] += A.getElement(i, k) * B.getElement(k, j);			
 				}
 			}
 		}
 		
 		return new Matrix(product);
 
 	}
 	
 	/**
 	 * Reduces Matrix A to reduced row echelon form.
 	 */
 	public Matrix gauss(Matrix A){
 		int m = A.getNumRows();
 		int n = A.getNumCols();
 		
 		for(int k = 0; k < m; k++){
 			int pivot=0;
 			
 			for(int j=k; j < m; j++){
 				if( pivot < Math.abs(A.getElement(j, k)) ){
 					pivot = j;
 				}
 			}
 			
 			if(A.getElement(pivot, k) == 0){
 				throw new IllegalArgumentException("Matrix is singular!");
 			}
 			
 			if(k != pivot)
 				A.swapRows(k, pivot);
 			
 			for(int i=k+1; i < m; i++){
 				for(int j = k+1; j < n; j++){
 					A.addRowMultipliedByScalar( (-1)*(A.getElement(i, k)/A.getElement(k,k)), i, k);
 					//double a = A.getElement(i, j) - A.getElement(k, j) * (A.getElement(i, k) / A.getElement(k, k) );
 					//A.setElement(i, j, a);
 				}
 			}
 			
 		}
 
 	
 		//Round down elements of I
 		for(int i = 0; i < n; i++){
 			for(int j = 0; j < n; j++){
 				roundTwoDecimals(A.getElement(i, j));
 			}
 		}
		
 		
 		return A;
 	}
 	
 	/**
 	 * Finds the determinant, if it exists.
 	 */
 	public double findDeterminant(Matrix A){
 		if(A.getNumCols() != A.getNumRows())
 			throw new IllegalArgumentException("Non-square matrix");
 		
 		int n = A.getNumCols();
 		
 
 		boolean negative=false;
 		
 		for(int k = 0; k < n; k++){
 			int pivot=0;
 			
 			for(int j=k; j < n; j++){
 				if( pivot < Math.abs(A.getElement(j, k)) ){
 					pivot = j;
 				}
 			}
 			
 			if(A.getElement(pivot, k) == 0){
 				throw new IllegalArgumentException("Matrix is singular!");
 			}
 			
 			if(k != pivot){
 				A.swapRows(k, pivot);
 
 				if(!negative){
 					negative = true;
 				}else{
 					negative = false;
 				}
 			}
 			
 			
 			for(int i=k+1; i < n; i++){
 				for(int j = k+1; j < n; j++){
 					A.addRowMultipliedByScalar( (-1)*(A.getElement(i, k)/A.getElement(k,k)), i, k);
 					//double a = A.getElement(i, j) - A.getElement(k, j) * (A.getElement(i, k) / A.getElement(k, k) );
 					//A.setElement(i, j, a);
 				}
 			}
 			
 		}
 		double det=A.getElement(0,0);
 		for(int i = 1; i < n; i++){
 			det *= A.getElement(i, i);
 		}
 		if(negative)
 			det *= (-1);
 		
 		return det;
 	}
 	
 
 	/**
 	 * Inverts an nxn matrix A
 	 * @param A
 	 * @throws IllegalArgumentException if
 	 * 			1) Matrix is not square
 	 * 			2) Matrix is singular.
 	 */
 	public Matrix invert(Matrix A){
 		if(A.getNumCols() != A.getNumRows())
 			throw new IllegalArgumentException("Non-square matrix");
 		
 		int n = A.getNumCols();
 		double [][] identity = new double[n][n];
 		for(int i = 0; i < n; i++)
 			identity[i][i] = 1;
 		
 		Matrix I = new Matrix(identity);
 		
 		for(int k = 0; k < n; k++){
 			int pivot=0;
 		
 			for(int j=k; j < n; j++){
 				if( pivot < Math.abs(A.getElement(j, k)) ){
 					pivot = j;
 				}
 			}
 			if(A.getElement(pivot, k) == 0){
 				throw new IllegalArgumentException("Matrix is singular!");
 			}
 			
 			if(k != pivot){
 				A.swapRows(k, pivot);
 				I.swapRows(k, pivot);
 			}
 			
 			for(int i=k+1; i < n; i++){
 				for(int j = k+1; j < n; j++){
 					double scalar = (-1)*(A.getElement(i, k)/A.getElement(k,k));
 					A.addRowMultipliedByScalar( scalar, i, k);
 					I.addRowMultipliedByScalar( scalar, i, k);
 					//double a = A.getElement(i, j) - A.getElement(k, j) * (A.getElement(i, k) / A.getElement(k, k) );
 					//A.setElement(i, j, a);
 				}
 			}
 			
 		}
 		
 
 		//Make all diagonal elements = 1 
 		for(int i = 0; i < n; i++){
 			double scalar = (1/A.getElement(i, i));
 			A.multiplyRowByScalar( scalar, i);
 			I.multiplyRowByScalar( scalar, i);
 		}
 
 
		//This might break completely
 		for(int i = n-1; i > 0; i--){
 			for(int j = 0; j < i; j++){
 				double scalar = (-1)*(A.getElement(i, i)*A.getElement(j,i));
 				A.addRowMultipliedByScalar( scalar, j, i);
 				I.addRowMultipliedByScalar( scalar, j, i);
 			}
 		}
 
 		//Round down elements of I
 		for(int i = 0; i < n; i++){
 			for(int j = 0; j < n; j++){
 				roundTwoDecimals(I.getElement(i, j));
 			}
 		}
 		
 		return I;
 	}
 	
 	double roundTwoDecimals(double d) {
         DecimalFormat twoDForm = new DecimalFormat("#.##");
         return Double.valueOf(twoDForm.format(d));
 	}
 }
