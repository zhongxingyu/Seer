 package math.matrices;
 
 public abstract class AbstractMatrix implements IMatrix {
 
    // ****************** //
    // ***** FIELDS ***** //
    // ****************** //
 
    /**
     * The primitive representation of the matrix. It is an array of rows which
     * are also arrays.
     */
    private double[][] matrix;
 
    /**
     * The number of rows that are in this matrix.
     */
    private int        rows;
 
    /**
     * The number of columns that are in this matrix.
     */
    private int        columns;
 
    // ************************ //
    // ***** CONSTRUCTORS ***** //
    // ************************ //
 
    /**
     * Creates a new matrix with the specified array as a base. If the resulting
     * matrix is modified the original matrix will change as well.
     * 
     * @param a
     *           the base array of the matrix.
     */
    protected AbstractMatrix(double[][] a) {
       set(a);
    }
 
    /**
    * Creates a new matrix with a copy of specified matrix.
     * 
     * @param a
     *           the matrix to copy.
     */
    protected AbstractMatrix(IMatrix a) {
       double[][] m = new double[a.rows()][a.columns()];
       for (int i = 0; i < a.rows(); i++) {
          for (int j = 0; j < a.columns(); j++) {
             m[i][j] = a.get(i, j);
          }
       }
       set(m);
    }
 
    // ************************** //
    // ***** ACCESS METHODS ***** //
    // ************************** //
 
    /**
     * {@inheritDoc}
     */
    @Override
    public int rows() {
       return rows;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public int columns() {
       return columns;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double get(int row, int column) {
       return matrix[row][column];
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getRow(int r) {
       double[] v = new double[columns()];
       for (int i = 0; i < columns(); i++) {
          v[i] = get(r, i);
       }
       return v;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public double[] getColumn(int c) {
       double[] v = new double[rows()];
       for (int i = 0; i < rows(); i++) {
          v[i] = get(i, c);
       }
       return v;
    }
 
    // ******************************** //
    // ***** MUNIPULATION METHODS ***** //
    // ******************************** //
 
    /**
     * Sets the matrix to be the specified array. This method does not copy but
     * sets the array which will change if the matrix is modified.
     * 
     * @param a
     *           the new array for the matrix.
     * @return the matrix representation of the array.
     */
    protected AbstractMatrix set(double[][] a) {
       rows = a.length;
       columns = (rows == 0 ? 0 : a[0].length);
       matrix = a;
       return this;
    }
 
    /**
     * Sets the matrix to be equal to the specified matrix. This method does not
     * copy but sets the array which will change if the matrix is modified.
     * 
     * @param a
     *           the new matrix for the matrix.
     * @return the matrix representation of the array.
     */
    protected AbstractMatrix set(AbstractMatrix a) {
       return set(a.matrix);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix set(int row, int column, double n) {
       matrix[row][column] = n;
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix setRow(int r, double[] v) {
       for (int i = 0; i < columns; i++) {
          // matrix[r][i] = v[i];
          set(r, i, v[i]);
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix setColumn(int c, double[] v) {
       for (int i = 0; i < rows; i++) {
          // matrix[i][c] = v[i];
          set(i, c, v[i]);
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public IMatrix set(int r, int c, double[][] a) {
       for (int i = 0; i < a.length; i++) {
          for (int j = 0; j < a[0].length; j++) {
             // matrix[r + i][c + j] = a[i][j];
             set(r + i, c + j, a[i][j]);
          }
       }
       return this;
    }
 
    // ***************************** //
    // ***** OPERATION METHODS ***** //
    // ***************************** //
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix transpose() {
       double[][] transpose = new double[columns()][rows()];
       for (int i = 0; i < columns(); i++) {
          for (int j = 0; j < rows(); j++) {
             // transpose[i][j] = matrix[j][i];
             transpose[i][j] = get(j, i);
          }
       }
       return set(transpose);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix inverse() {
       throw new RuntimeException("Method not implemented.");
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix scale(double a) {
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < columns(); j++) {
             // matrix[i][j] = a * matrix[i][j];
             set(i, j, a * get(i, j));
          }
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix add(IMatrix a) {
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < columns(); j++) {
             // matrix[i][j] += a.get(i, j);
             set(i, j, get(i, j) + a.get(i, j));
          }
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix subtract(IMatrix a) {
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < columns(); j++) {
             // matrix[i][j] -= a.get(i, j);
             set(i, j, get(i, j) - a.get(i, j));
          }
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix multiply(IMatrix a) {
       double[][] c = new double[rows()][columns()];
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < a.columns(); j++) {
             double sum = 0;
             for (int k = 0; k < a.rows(); k++) {
                // sum += matrix[i][k] * a.matrix[k][j];
                sum += get(i, k) * a.get(k, j);
             }
             c[i][j] = sum;
          }
       }
       return set(c);
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix dotMultiply(IMatrix a) {
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < columns(); j++) {
             // matrix[i][j] *= a.get(i, j);
             set(i, j, get(i, j) * a.get(i, j));
          }
       }
       return this;
    }
 
    /**
     * {@inheritDoc}
     */
    @Override
    public AbstractMatrix dotDivide(IMatrix a) {
       for (int i = 0; i < rows(); i++) {
          for (int j = 0; j < columns(); j++) {
             // matrix[i][j] /= a.get(i, j);
             set(i, j, get(i, j) / a.get(i, j));
          }
       }
       return this;
    }
 
 }
