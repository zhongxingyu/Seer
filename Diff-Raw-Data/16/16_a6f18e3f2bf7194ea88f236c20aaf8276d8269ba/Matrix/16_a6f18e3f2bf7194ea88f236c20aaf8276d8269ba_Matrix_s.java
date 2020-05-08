 package math.matrices;
 
 /**
  * The object representation of a mathematical matrix. This class provides an
 * implementation for basic use.
  * 
  * @author Brian Norman
  * @version 0.1 beta
  */
 public class Matrix extends AbstractMatrix {
 
    // ************************ //
    // ***** CONSTRUCTORS ***** //
    // ************************ //
 
    /**
     * Creates a new matrix with no values and of size zero.
     */
    public Matrix() {
       super(new double[0][0]);
    }
 
    /**
     * Creates a new matrix of the specified size and with all values equaling
     * zero.
     * 
     * @param rows
     *           the number of rows in this matrix.
     * @param columns
     *           the number of columns in this matrix.
     */
    public Matrix(int rows, int columns) {
       super(new double[rows][columns]);
    }
 
    /**
     * Creates a new matrix with the specified array as a base. If the resulting
     * matrix is modified the original matrix will change as well.
     * 
     * @param a
     *           the base array of the matrix.
     */
    public Matrix(double[][] a) {
       super(a);
    }
 
    /**
    * Creates a new matrix with a copy of the specified matrix.
     * 
     * @param a
     *           the matrix to copy.
     */
   protected Matrix(Matrix a) {
       super(a);
    }
 
    // *************************** //
    // ***** UTILITY METHODS ***** //
    // *************************** //
 
    /**
     * {@inheritDoc}
     */
    @Override
    public Matrix copy() {
       return new Matrix(this);
    }
 
 }
