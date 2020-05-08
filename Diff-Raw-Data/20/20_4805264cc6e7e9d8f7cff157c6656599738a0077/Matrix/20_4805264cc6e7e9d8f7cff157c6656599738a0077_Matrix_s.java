 /**
  * Matrix.java
  */
 package artiano.core;
 
 
 /**
  * <p>Description: operation of matrix</p>
  * @author Nano.Michael
  * @version 1.0.0
  * @date 2013-8-20
  * @author (latest modification by Nano.Michael)
  * @function 
  * @since 1.0.0
  */
 public class Matrix{
 	//columns of the matrix
 	protected int cols = 0;
 	//rows of the matrix
 	protected int rows = 0;
 	//data of the matrix
 	protected double[] d = null;
 	//data rows
 	protected int dCols = 0;
 	//row range
 	protected Range rowRange = null;
 	//column range
 	protected Range colRange = null;
 	
 	private Matrix(){ }
 	
 	/**
 	 * constructor
 	 * @param rows - rows of the matrix
 	 * @param cols - columns of the matrix
 	 */
 	public Matrix(int rows, int cols){
 		this(rows, cols, new double[cols * rows]);
 	}
 	
 	/**
 	 * constructor
 	 * @param rows - rows of the matrix
 	 * @param cols - columns of the matrix
 	 * @param data - data
 	 */
 	public Matrix(int rows, int cols, double[] data){
 		if (cols <= 0 || rows <= 0)
 			throw new IllegalArgumentException("Matrix, columns and rows must be positive integer.");
 		this.cols = cols;
 		this.rows = rows;
 		this.d = data;
 		this.dCols = cols;
 		rowRange = new Range(0, rows);
 		colRange = new Range(0, cols);
 	}
 	
 	/**
 	 * get columns
 	 * @return - columns
 	 */
 	public int columns(){
 		return this.cols;
 	}
 	
 	/**
 	 * get rows
 	 * @return - rows
 	 */
 	public int rows(){
 		return this.rows;
 	}
 	
 	/**
 	 * row vector at row i of the matrix
 	 * @param i - row index
 	 * @return - row vector
 	 */
 	public Matrix row(int i){
 		return at(new Range(i, i+1), Range.all());
 	}
 	
 	/**
 	 * column vector at column i of the matrix
 	 * @param i - column index
 	 * @return - column vector
 	 */
 	public Matrix col(int i){
 		return at(Range.all(), new Range(i, i+1));
 	}
 	
 	/**
 	 * reshape the matrix
 	 * @param rows - rows after reshape
 	 * @param cols - columns after reshape
 	 * @return - reshaped matrix
 	 */
 	public Matrix reshape(int rows, int cols){
 		if (rows * cols != this.rows * this.cols)
 			throw new IllegalArgumentException("Matrix reshape, size not match.");
 		
 		return this;
 	}
 	
 	/**
 	 * create an matrix like A=u*I, I is unit matrix, u is scale
 	 * @param size - matrix size
 	 * @param scale - the value to set
 	 * @return - matrix
 	 */
 	public static Matrix unit(int size, double scale){
 		Matrix x = new Matrix(size, size);
 		for (int i = 0; i < size; i++)
 			x.set(i, i, scale);
 		return x;
 	}
 	
 	/**
 	 * get the value of row i and column j
 	 * @param i - row index
 	 * @param j - column index
 	 * @return - value of row i column j
 	 */
 	public double at(int i, int j){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		return d[(i + rowRange.begin()) * dCols + j + colRange.begin()];
 	}
 	
 	/**
 	 * get the sub-matrix, data will not be copied
 	 * @param row - row range
 	 * @param col - column range
 	 * @return - a sub-matrix of the matrix
 	 */
 	public Matrix at(Range row, Range col){
 		row = row.equals(Range.all()) ? this.rowRange:
 			new Range(row.begin() + this.rowRange.begin(), row.end() + this.rowRange.begin());
 		col = col.equals(Range.all()) ? this.colRange: 
 			new Range(col.begin() + this.colRange.begin(), col.end() + this.colRange.begin());
 		
 		if (!this.rowRange.isContain(row) || !this.colRange.isContain(col))
 			throw new IllegalArgumentException("Matrix at, out of range.");
 		Matrix x = new Matrix();
 		// row range of x
 		x.rowRange = row;
 		//column range of x
 		x.colRange = col;
 		x.dCols = dCols;
 		x.d = d;
 		x.rows = x.rowRange.length();
 		x.cols = x.colRange.length();
 		return x;
 	}
 	
 	/**
 	 * set row i column j to value
 	 * @param i - row index
 	 * @param j - column index
 	 * @param value - value to set
 	 */
 	public void set(int i, int j, double value){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		d[(i + rowRange.begin()) * dCols + j + colRange.begin()] = value;
 	}
 	
 	/**
 	 * transpose
 	 * @return - transpose of the matrix
 	 */
 	public Matrix t(){
 		Matrix x = new Matrix(cols, rows);
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j <cols; j++)
 				x.set(j, i, at(i, j));
 		return x;
 	}
 	
 	/**
 	 * matrix addition (z = x + y)
 	 * @param x 
 	 * @return - result
 	 */
 	public Matrix add(Matrix x){
 		return add(x,false);
 	}
 	
 	/**
 	 * matrix addition
 	 * @param x
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix add(Matrix x, boolean reserve){
 		if (rows != x.rows || cols != x.cols)
 			throw new IllegalArgumentException("Matrix add, size not match.");
 		Matrix y = reserve ? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j) + x.at(i, j));
 		return y;
 	}
 	
 	/**
 	 * matrix addition (z=x+y, x is a scale)
 	 * @param x
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix add(Number x, boolean reserve){
 		Matrix y = reserve ? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j) + x.doubleValue());
 		return y;
 	}
 	
 	/**
 	 * matrix addition (z=x+y, x is scale)
 	 * @param x
 	 * @return - result
 	 */
 	public Matrix add(Number x){
 		return add(x,false);
 	}
 	
 	/**
 	 * add a value to row i column j
 	 * @param i - row index
 	 * @param j - column index
 	 * @param value - value to add
 	 */
 	public void add(int i, int j, Number value){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		d[(i + rowRange.begin()) * dCols + j + colRange.begin()] += value.doubleValue();
 	}
 	
 	/**
 	 * value of row i column j subtract a value
 	 * @param i - row index
 	 * @param j - column index
 	 * @param value - value to subtract
 	 */
 	public void subtract(int i, int j, Number value){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		d[(i + rowRange.begin()) * dCols + j + colRange.begin()] -= value.doubleValue();
 	}
 	
 	/**
 	 * matrix subtraction (z=x-y)
 	 * @param x
 	 * @return - result
 	 */
 	public Matrix subtract(Matrix x){
 		return subtract(x,false);
 	}
 	
 	/**
 	 * matrix subtraction (z=x-y)
 	 * @param x
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix subtract(Matrix x, boolean reserve){
 		if (x.rows != rows || x.cols != cols)
 			throw new IllegalArgumentException("Matrix subtract, size not match.");
 		Matrix y = reserve ? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j) - x.at(i, j));
 		return y;
 	}
 	
 	/**
 	 * matrix subtraction (z=x-y, x is a scale)
 	 * @param x
 	 * @return - result
 	 */
 	public Matrix subtract(Number x){
 		return subtract(x, false);
 	}
 	
 	/**
 	 * matrix subtraction (z=x-y, x is a scale)
 	 * @param x
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix subtract(Number x, boolean reserve){
 		Matrix y = reserve ? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j) - x.doubleValue());
 		return y;
 	}
 	
 	/**
 	 * matrix multiplication (z=x*y)
 	 * @param x
 	 * @return - result
 	 */
 	public Matrix multiply(Matrix x){
 		if (x.rows != cols)
 			throw new IllegalArgumentException("Matrix multiplication, size not match.");
 		int m = rows;
 		int s = x.rows;
 		int n = x.cols;
 		Matrix y = new Matrix(m, n);
 		for (int i = 0; i < m; i++)
 			for (int j = 0; j < n; j++)
 				for (int k = 0; k < s; k++)
 					y.add(i, j ,at(i, k) * x.at(k, j));
 		return y;
 	}
 	
 	/**
 	 * matrix multiplication (z=x*y, x is scale)
 	 * @param x
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix multiply(Number x, boolean reserve){
 		Matrix y = reserve ? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j) * x.doubleValue());
 		return y;
 	}
 	
 	/**
 	 * matrix multiplication (z=x*y, x is scale)
 	 * @param x
 	 * @return - result
 	 */
 	public Matrix multiply(Number x){
 		return multiply(x,false);
 	}
 	
 	/**
 	 * matrix value at row i column j multiply a number
 	 * @param i - row index
 	 * @param j - column index
 	 * @param x - number to multiply
 	 */
 	public void multiply(int i, int j, Number x){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		set(i, j, at(i, j) * x.doubleValue());
 	}
 	
 	/**
 	 * matrix value at row i column j divide a number
 	 * @param i - row index
 	 * @param j - column index
 	 * @param x - number to divide
 	 */
 	public void divide(int i, int j, Number x){
 		if (i < 0 || i >= rows || j < 0 || j >= cols)
 			throw new IndexOutOfBoundsException("Matrix at, index out of range.");
 		set(i, j, at(i, j)/x.doubleValue());
 	}
 	
 	/**
 	 * matrix divide a number
 	 * @param x - number to divide
 	 * @return - result
 	 */
 	public Matrix divide(Number x){
 		return divide(x,false);
 	}
 	
 	/**
 	 * matrix divide a number
 	 * @param x - number to divide
 	 * @param reserve - indicate reserve the matrix whether or not
 	 * @return - result
 	 */
 	public Matrix divide(Number x, boolean reserve){
 		if (x.doubleValue() == 0.)
 			throw new ArithmeticException("Matrix divide, divisor is 0.");
 		Matrix y = reserve? new Matrix(rows, cols): this;
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				y.set(i, j, at(i, j)/x.doubleValue());
 		return y;
 	}
 	
 	/**
 	 * clone a matrix
 	 */
 	@Override
 	public Matrix clone(){
 		Matrix x = new Matrix(rows, cols);
 		for (int i = 0; i < rows; i++)
 			for (int j = 0; j < cols; j++)
 				x.set(i, j, at(i, j));
 		return x;
 	}
 	
 	/**
 	 * transpose x
 	 * @param x - input matrix
 	 * @return - the transposition of x
 	 */
 	public static double[][] transpose(double[][] x){
 		double[][] y = new double[x[0].length][x.length];
 		for (int i = 0; i < y.length; i++){
 			for (int j = 0; j < x.length; j++)
 				y[i][j] = x[j][i];
 		}
 		return y;
 	}
 	
 	/**
 	 * copy the specified matrix
 	 * @param x - source matrix
 	 * @return - the copy of x
 	 */
 	public static double[][] copy(double[][] x){
 		double[][] y = new double[x.length][x[0].length];
 		for (int i = 0; i < x.length; i++)
 			for (int j = 0; j < x[0].length; j++)
 				y[i][j] = x[i][j];
 		return y;
 	}
 	
 	/**
 	 * matrix addition (z = x+y)
 	 * @param x - matrix x
 	 * @param y - matrix y
 	 * @param reserveX - indicate reserve x whether or not
 	 * @return - result
 	 */
 	public static double[][] add(double[][] x, double[][] y, boolean reserveX){
 		if (x.length != y.length || x[0].length != y[0].length)
 			throw new IllegalArgumentException("Matrix add, size not match.");
 		double[][] z;
 		z = reserveX ?new double[x.length][x[0].length]: x;
 		for (int i = 0; i < x.length; i++)
 			for (int j = 0; j < x[0].length; j++)
 				z[i][j] = x[i][j] + y[i][j];
 		return z;
 	}
 	
 	/**
 	 * matrix subtract (z = x - y)
 	 * @param x - matrix x
 	 * @param y - matrix y
 	 * @param reserveX - indicate reserve x whether or not
 	 * @return - result
 	 */
 	public static double[][] subtract(double[][] x, double[][] y, boolean reserveX){
 		if (x.length != y.length || x[0].length != y[0].length)
 			throw new IllegalArgumentException("Matrix add, size not match.");
 		double[][] z;
 		z = reserveX ?new double[x.length][x[0].length]: x;
 		for (int i = 0; i < x.length; i++)
 			for (int j = 0; j < x[0].length; j++)
 				z[i][j] = x[i][j] - y[i][j];
 		return z;
 	}
 	
 	/**
 	 * matrix multiplication (z = x * y, y is scale)
 	 * @param x - matrix x
 	 * @param y - matrix y
 	 * @param reserveX - indicate reserve x or not
 	 * @return - result
 	 */
 	public static double[][] multiply(double[][] x, double y, boolean reserveX){
 		double[][] z;
 		z = reserveX ? new double[x.length][x[0].length]: x;
 		for (int i = 0; i < x.length; i++)
 			for (int j = 0; j < x[0].length; j++)
 				z[i][j] = x[i][j] * y;
 		return z;
 	}
 	
 	/**
 	 * compute the multiplication of x and y
 	 * @param x - left matrix
 	 * @param y - right matrix
 	 * @return - multiplication of x and y
 	 */
 	public static double[][] multiply(double[][] x, double[][] y){
 		if (x[0].length != y.length)
 			throw new IllegalArgumentException("Matrix multiply, size not match.");
 		
 		int m = x.length;
 		int s = y.length;
 		int n = y[0].length;
 		double[][] t = new double[x.length][y[0].length];
 		for (int i = 0; i < m; i++){
 			for (int j = 0; j < n; j++){
 				for (int k = 0; k < s; k++)
 					t[i][j] += x[i][k] * y[k][j];
 			}
 		}
 		return t;
 	}
 }
