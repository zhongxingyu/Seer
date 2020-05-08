 package de.skuzzle.polly.parsing.util;
 
 import java.util.Arrays;
 
 
 /**
  * <p>This class represents a rectangular matrix of size m times n, where m denotes the
  * number of rows and n the number of columns. When accessing single elements of a matrix
  * or iterating over it, we always use {@code i} to denote a row number and {@code j} to
  * denote column numbers.</p>
  * 
  * <p>Please note that row and column indexing starts at zero and thus the maximum row 
  * index is {@code matrix.getM() - 1} and the maximum column index is 
  * {@code matrix.getN() - 1}</p>
  * 
  * <p>As in linear algebra, a matrix can be created over any algebraic {@link Field}. This
  * abstract implementation allows full control of how the elements in the matrix are 
  * being summed or multiplied. For example one could create a matrix which uses the 
  * residue class of modulo 5 and all matrix operations will automatically conform to
  * those operation in that field. The class {@link Fields} contains several useful 
  * default implementations of the Field interface.</p>
  * 
  * <p>This class provides default matrix operations that are multiplication by a scalar,
  * matrix multiplication (not commutative!) and adding two matrices. Then there are some
  * methods for elementary row operations as swapping rows, multiplying a single row with 
  * a scalar and adding the multiple of one row to another.</p>
  *  
  * @author Simon
  *
  * @param <K> Type for the elements in this matrix.
  */
 public class Matrix<K> {
     
     
     
     /**
      * This exception is thrown whenever a matrix operations being performed on matrices 
      * with incompatible dimensions. For example, two matrices can only be multiplied if
      * the column count of the left matrix equals the row count of the right one.
      * 
      * @author Simon
      */
     public static class DimensionException extends RuntimeException {
 
         private static final long serialVersionUID = 1L;
         
         
         public DimensionException(String msg) {
             super(msg);
         }
     }
     
     
     
     /**
      * Creates the quadratic identical matrix of size n using the given field. The result
      * will contain only zeros except for the diagonal components which are set to one.
      * 
      * @param n The size of the matrix.
      * @param field The algebraic field which provides the zero and the one element.
      * @return An identical matrix of size n.
      */
     public static <K> Matrix<K> getId(int n, Field<K> field) {
         Matrix<K> result = new Matrix<K>(n, n, field);
         
         for (int i = 0; i < n; ++i) {
             for (int j = 0; j < n; ++j) {
                 if (i == j) {
                     result.matrix[i][j] = field.getMultiplicativeNeutral();
                 } else {
                     result.matrix[i][j] = field.getAdditiveNeutral();
                 }
             }
         }
         return result;
     }
     
     
     
     /**
      * Creates a quadratic matrix of size n over the given field containing only zeros.
      * 
      * @param n The size of the matrix.
      * @param field The algebraic field which provides the zero element.
      * @return A matrix containing only zeros.
      */
     public static <K> Matrix<K> getZero(int n, Field<K> field) {
         Matrix<K> result = new Matrix<K>(n, n, field);
         
         for (int i = 0; i < n; ++i) {
             for (int j = 0; j < n; ++j) {
                 result.matrix[i][j] = field.getAdditiveNeutral();
             }
         }
         return result;
     }
     
     
     
     /**
      * Merges two matrices together. That is, the columns of the right matrix are simply
      * appended to the left matrix. This operation is only possible of both matrices have
      * the same row count and use the same algebraic field.
      * 
      * <p>The result will be a new matrix of the size m times (left.n + right.n) but it
      * will contain the same elements (no copies!) as both source matrices.</p>
      *  
      * @param left The left matrix.
      * @param right The right matrix which columns are to be appended to the left matrix.
      * @return A new merged matrix.
      */
     public static <K> Matrix<K> merge(Matrix<K> left, Matrix<K> right) {
         if (left.getM() != right.getM()) {
             throw new DimensionException("dimension mismatch");
         } else if (!left.getField().equals(right.getField())) {
             throw new IllegalArgumentException("incompatible fields");
         }
         Matrix<K> result = new Matrix<K>(left.getM(), left.getN() + right.getN(), 
             left.getField());
         
         for (int i = 0; i < left.getM(); ++i) {
             for (int j = 0; j < left.getN() + right.getN(); ++j) {
                 if (j < left.getN()) {
                     result.set(i, j, left.get(i, j));
                 } else {
                     result.set(i, j, right.get(i, j - left.getN()));
                 }
             }
         }
         return result;
     }
     
     
     public static void main(String[] args) {
         Integer[][] m = {
             {1, 2, 0, 1, 0, 0},
             {2, 3, 0, 0, 1, 0},
             {3, 4, 1, 0, 0, 1}
         };
         
         Matrix<Integer> matrix = new Matrix<Integer>(m, Fields.integerModulo(5));
         System.out.println(splitRight(matrix, 2));
     }
     
     
     
     /**
      * <p>Creates a new matrix by splitting the given one at a certain column and 
      * returning a matrix which contains all elements to the left of that column. The 
      * column index is exclusive. Thus {@code splitLeft(matrix, matrix.getN())} will 
      * return an exact copy of the given matrix.</p>
      * 
      * <p>In general the result will have {@code matrix.getM()} rows and {@code j}
      * columns.</p>
      * 
      * @param matrix The matrix to split.
      * @param j The (exclusive) column index at which the matrix is splitted.
      * @return A new matrix containing the elements to the left of the column denoted by
      *          {@code j.}
      */
     public static <K> Matrix<K> splitLeft(Matrix<K> matrix, int j) {
         Matrix<K> result = new Matrix<K>(matrix.getM(), j, matrix.getField());
         
         for (int i = 0; i < matrix.getM(); ++i) {
             for (int j1 = 0; j1 < j; ++j1) {
                 result.set(i, j1, matrix.get(i, j1));
             }
         }
         return result;
     }
     
     
     
     /**
      * <p>Creates a new matrix by splitting the given one at a certain column and 
      * returning a matrix which contains all elements to the right of that column. The 
     * column index is inclusive. Thus {@code splitRight(matrix, 0)} will 
      * return an exact copy of the given matrix.</p>
      * 
      * <p>In general the result will have {@code matrix.getM()} rows and {@code j}
      * columns.</p>
      * 
      * @param matrix The matrix to split.
      * @param j The (inclusive) column index at which the matrix is splitted.
      * @return A new matrix containing the elements to the right of the column denoted by
      *          {@code j} including column {@code j} itself. 
      */
     public static <K> Matrix<K> splitRight(Matrix<K> matrix, int j) {
         Matrix<K> result = new Matrix<K>(matrix.getM(), matrix.getN() - j, 
             matrix.getField());
         
         for (int i = 0; i < matrix.getM(); ++i) {
             for (int j1 = j; j1 < matrix.getN(); ++j1) {
                 result.set(i, j1 - j, matrix.get(i, j1));
             }
         }
         return result;
     }
     
     
 
     private K[][] matrix;
     private Field<K> field;
     
     private int m; // row count
     private int n; // col count
     
     
     
     /**
      * <p>Creates a new empty matrix with the given dimensions which uses the given field 
      * for its algebraic operations.</p>
      * 
      * <p>Please note that empty means, this matrix only contains <code>null</code> and 
      * thus must be filled by the user using the {@link #set(int, int, Object)} 
      * method.</p>
      *  
      * @param m The number of rows in this matrix.
      * @param n The number of columns in this matrix.
      * @param field The algebraic field for this matrix.
      */
     @SuppressWarnings("unchecked")
     public Matrix(int m, int n, Field<K> field) {
         if (m == 0) {
             throw new DimensionException("m is 0");
         } else if (n == 0) {
             throw new DimensionException("n is 0");
         }
         this.field = field;
         this.matrix = (K[][]) new Object[m][n];
         this.m = m;
         this.n = n;
     }
     
     
     
     /**
      * Creates a new matrix as a copy of the given matrix. It will contain <b>copies</b> 
      * of each element and thus have the exact same size and will use the same algebraic 
      * field as the given matrix.
      * 
      * @param other The matrix to copy.
      */
     public Matrix(Matrix<K> other) {
         this(other.matrix, other.field);
     }
     
     
     
     /**
      * <p>Creates a new matrix which uses the given two-dimensional array as source for 
      * its elements and the given field for its algebraic operations. The first dimension
      * of the array will be interpreted as the rows and the second as the columns.</p>
      * 
      * <p>Please note that the given array is not copied, thus all operations done to
      * this matrix are reflected to the array and vice-versa.</p>
      * 
      * @param other The array to create this matrix from.
      * @param field The algebraic field for this matrix.
      */
     @SuppressWarnings("unchecked")
     public Matrix(K[][] other, Field<K> field) {
         this.field = field;
         this.m = this.getRows(other);
         this.n = this.getCols(other);
         
         this.matrix = (K[][]) new Object[this.m][this.n];
         for (int i = 0; i < this.m; ++i) {
             System.arraycopy(other[i], 0, this.matrix[i], 0, this.n);
         }
     }
     
     
     
     
     /**
      * <p>This constructor can be used to convert a one-dimensional vector into either a
      * row- or column based matrix representation.</p>
      * 
      * <p>If creating a column vector, the resulting matrix will have the dimensions
      * {@code m = 1} and {@code n = vector.length}. A row vector will have the dimensions
      * {@code m = vector.length} and {@code n = 1}</p>
      * 
      * @param vector One-dimensional vector which should be transformed into a matrix 
      *          representation.
      * @param isRow Whether the result should be a column- or a row base representation.
      * @param field The algebraic field for this matrix.
      */
     @SuppressWarnings("unchecked")
     public Matrix(K[] vector, boolean isRow, Field<K> field) {
         this.field = field;
         if (isRow) {
             this.m = vector.length;
             this.n = 1;
             this.matrix = (K[][]) new Object[this.m][this.n];
             for (int i = 0; i < this.m; ++i) {
                 this.matrix[i] = (K[]) new Object[] { vector[i] };
             }
         } else {
             this.m = 1;
             this.n = vector.length;
             this.matrix = (K[][]) new Object[this.m][this.n];
             System.arraycopy(vector, 0, this.matrix[0], 0, this.n);
         }
     }
     
     
     
     /**
      * <p>Creates the identical matrix of size m times m where m is the number of rows in 
      * this matrix.</p>
      * 
      * <p>This is intended to create matrices that can be multiplied with this one. 
      * Given the matrix A, the product of {@code A.getLeftId()*A} returns A.</p>
      * 
      * @return An identical Matrix of size m times m.
      */
     public Matrix<K> getLeftId() {
         return getId(this.m, this.field);
     }
     
     
     
     /**
      * <p>Creates the identical matrix of size n times n where n is the number of 
      * columns in this matrix.</p>
      * 
      * <p>This is intended to create matrices that this matrix can be multiplied with. 
      * Given the matrix A, the product of {@code A*A.getRightId()} returns A</p>
      * 
      * @return An identical Matrix of size n times n.
      */
     public Matrix<K> getRightId() {
         return getId(this.n, this.field);
     }
     
     
     
     /**
      * Creates a matrix of size m times m where m is the number of rows in this matrix 
      * which contains only zeros.
      * 
      * @return A new matrix containing only zeros.
      */
     public Matrix<K> getZero() {
         return getZero(this.m, this.field);
     }
     
     
     
     /**
      * Gets the underlying {@link Field} of this matrix which provides the elementary 
      * algebraic operations and values needed for most matrix operations. 
      * 
      * @return The underlying field instance.
      */
     public Field<K> getField() {
         return this.field;
     }
     
     
     
     /**
      * Gets the number of rows of this matrix.
      * 
      * @return The number of rows.
      */
     public int getM() {
         return this.m;
     }
     
     
     
     /**
      * Gets the number of columns of this matrix.
      * 
      * @return The number of columns.
      */
     public int getN() {
         return this.n;
     }
     
     
     
     /**
      * Gets the value of the component denoted by i and j. This will throw an
      * {@link ArrayIndexOutOfBoundsException} if either i or j exceed the bounds of this
      * matrix.
      * 
      * @param i The row index.
      * @param j The column index.
      * @return The element at the specified coordinates.
      */
     public K get(int i, int j) {
         return this.matrix[i][j];
     }
     
     
     
     /**
      * <p>Sets the component denoted by i and j of this matrix to k. This will throw an
      * {@link ArrayIndexOutOfBoundsException} if either i or j exceed the bounds of this
      * matrix.</p>
      * 
      * <p>This setter supports chaining as it returns an instance to 'this'. So it can
      * be easily called multiple times on the same matrix like 
      * {@code matrix.set(a, b, 10).set(c, d, 11).set(e, f, 12)}</p>
      * 
      * @param i The row index.
      * @param j The column index.
      * @param k The element to set that cell to.
      * @return The 'this' reference.
      */
     public Matrix<K> set(int i, int j, K k) {
         this.matrix[i][j] = k;
         return this;
     }
     
     
     
     /**
      * <p>Adds the other matrix to this by adding all elements pairwise. 
      * This operation is only possible if both matrices have the exact same size.</p>
      * 
      * <p>This operation creates a new matrix for the result and leaves this one 
      * untouched</p>
      * 
      * @param other The matrix to be added to this one.
      * @return A new matrix containing the result.
      * @throws DimensionException if the dimensions of this matrix and the other mismatch.
      */
     public Matrix<K> add(Matrix<K> other) {
         return this.add(other.matrix);
     }
     
     
     
     /**
      * <p>Adds the other matrix given as array to this by adding all elements pairwise. 
      * This operation is only possible if both matrices have the exact same size.</p>
      * 
      * <p>This operation creates a new matrix for the result and leaves this one 
      * untouched</p>
      * 
      * @param other The matrix to be added to this one.
      * @return A new matrix containing the result.
      * @throws DimensionException if the dimensions of this matrix and the other mismatch.
      */
     public Matrix<K> add(K[][] other) {
         if (!this.canAdd(other)) {
             throw new DimensionException("illegal dimension");
         }
         
         Matrix<K> result = new Matrix<K>(this.m, this.n, this.field);
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 result.matrix[i][j] = this.field.add(this.matrix[i][j], other[i][j]);
             }
         }
         return result;
     }
     
     
     
     /**
      * <p>Adds the other matrix to this by adding all elements pairwise. 
      * This operation is only possible if both matrices have the exact same size.</p>
      * 
      * <p>This operation creates operates in place and the result is stored in this
      * matrix instance.
      * 
      * @param other The matrix to be added to this one.
      * @return The 'this' reference.
      * @throws DimensionException if the dimensions of this matrix and the other mismatch.
      */
     public Matrix<K> addInPlace(Matrix<K> other) {
         return this.addInPlace(other.matrix);
     }
     
     
     
     /**
      * <p>Adds the other matrix given as array to this by adding all elements pairwise. 
      * This operation is only possible if both matrices have the exact same size.</p>
      * 
     * <p>This operation operates in place and the result is stored in this
     * matrix instance.</p>
      * 
      * @param other The matrix to be added to this one.
      * @return The 'this' reference.
      * @throws DimensionException if the dimensions of this matrix and the other mismatch.
      */
     public Matrix<K> addInPlace(K[][] other) {
         if (!this.canAdd(other)) {
             throw new DimensionException("illegal dimension");
         }
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 this.matrix[i][j] = this.field.add(this.matrix[i][j], other[i][j]);
             }
         }
         
         return this;
     }
     
     
     
     /**
      * Determines whether the other matrix can be added to this matrix. That is, if both
      * matrices have the same row- and column count.
      * 
      * @param other The matrix to add.
      * @return Whether the matrix can be added to this matrix.
      */
     public boolean canAdd(Matrix<K> other) {
         return this.canAdd(other.matrix);
     }
     
     
     
     /**
      * Determines whether the other matrix can be added to this matrix. That is, if both
      * matrices have the same row- and column count.
      * 
      * @param other The matrix to add.
      * @return Whether the matrix can be added to this matrix.
      */
     public boolean canAdd(K[][] other) {
         return this.getRows(other) == this.m && this.getCols(other) == this.n;
     }
     
     
     
     /**
      * Creates the transposed matrix for this one. Given this matrix has the size 
      * m times n, the size of the result matrix will be n times m.
      * 
      * @return A new matrix containing the result.
      */
     public Matrix<K> transpose() {
         Matrix<K> result = new Matrix<K>(this.n, this.m, this.field);
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 result.matrix[i][j] = this.matrix[j][i];
             }
         }
         
         return result;
     }
     
     
     
     /**
      * If this is a quadratic matrix, this method creates the transposed matrix in place
      * by mirroring each element on the diagonal elements.
      * 
      * @return The 'this' reference.
      * @throws DimensionException If this matrix is not quadratic.
      */
     public Matrix<K> transposeInPlace() {
         if (this.m != this.n) {
             throw new DimensionException("illegal dimension");
         }
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < i; ++j) {
                 this.swap(i, j, j, i);
             }
         }
         return this;
     }
 
 
 
     /**
      * Multiplies each element in this matrix with the given scalar. This operation 
      * creates a new matrix for the result and leaves this matrix untouched.
      * 
      * @param scalar The scalar to multiply this matrix with.
      * @return A new matrix containing the result.
      */
     public Matrix<K> multiplyWith(K scalar) {
         Matrix<K> result = new Matrix<K>(this.m, this.n, this.field);
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 result.matrix[i][j] = this.field.multiply(this.matrix[i][j], scalar);
             }
         }
         
         return result;
     }
     
     
     
     /**
      * Multiplies each element in this matrix with the given scalar. This method works
      * in place and changes the underlying matrix array of this instance.
      * 
      * @param scalar The scalar to multiply this matrix with.
      * @return The 'this' reference.
      */
     public Matrix<K> multiplyWithInPlace(K scalar) {
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 this.matrix[i][j] = this.field.multiply(this.matrix[i][j], scalar);
             }
         }
         return this;
     }
     
     
     
     /**
      * <p>Multiplies this matrix with an other matrix. This means that this
      * matrix is the left matrix and the other matrix is the right matrix. This matters as
      * multiplication of matrices is not commutative. This requires that the column count
      * of this matrix equals the row count of the other matrix. If not, a 
      * {@link DimensionException} is thrown.</p>
      * 
      * <p>This operation creates a new matrix for the result and leaves this and the 
      * other matrix untouched.</p>
      * 
      * <p>Given this matrix has the size l times m and the others size is m times n, the
      * result matrix will have the size l times n.</p>
      *  
      * @param other The matrix to multiply this with.
      * @return A new matrix with the result of the multiplication.
      */
     public Matrix<K> multiplyWith(Matrix<K> other) {
         return this.multiplyWith(other.matrix);
     }
     
     
     
     /**
      * <p>Multiplies this matrix with an other matrix given as array. This means that this
      * matrix is the left matrix and the other matrix is the right matrix. This matters as
      * multiplication of matrices is not commutative. This requires that the column count
      * of this matrix equals the row count of the other matrix. If not, a 
      * {@link DimensionException} is thrown.</p>
      * 
      * <p>This operation creates a new matrix for the result and leaves this and the 
      * other matrix untouched.</p>
      * 
      * <p>Given this matrix has the size l times m and the others size is m times n, the
      * result matrix will have the size l times n.</p>
      * 
      * @param other The matrix to multiply this with.
      * @return A new matrix with the result of the multiplication.
      */
     public Matrix<K> multiplyWith(K[][] other) {
         if (!this.canMultiplyWith(other)) {
             throw new DimensionException("illegal dimension");
         }
         
         Matrix<K> result = new Matrix<K>(this.m, this.getCols(other), this.field);
         K zero = this.field.getAdditiveNeutral();
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.getCols(other); ++j) {
                 
                 K sum = zero;
                 for (int k = 0; k < this.n; ++k) {
                     sum = this.field.add(sum, 
                         this.field.multiply(this.matrix[i][k], other[k][j]));
                 }
                 result.matrix[i][j] = sum;
                 
             }
         }
         
         return result;
     }
     
     
     
     /**
      * Multiplies the row 'row' with scalar.
      * 
      * @param scalar The scalar to multiply the row with.
      * @param row The row index.
      */
     public void multiplyRowWith(K scalar, int row) {
         for (int j = 0; j < this.n; ++j) {
             this.matrix[row][j] = this.field.multiply(scalar, this.matrix[row][j]);
         }
     }
     
     
     
     /**
      * Adds the product of scalar and each element of the row srcRow to the row destRow.
      * 
      * @param scalar The scalar to multiply the source row with.
      * @param srcRow The row to multiply with the scalar.
      * @param destRow The row to which the product of the multiplication is added.
      */
     public void addMultipleToRow(K scalar, int srcRow, int destRow) {
         for (int j = 0; j < this.n; ++j) {
             this.matrix[destRow][j] = this.field.add(this.matrix[destRow][j], 
                 this.field.multiply(scalar, this.matrix[srcRow][j]));
         }
     }
     
     
     
     /**
      * Swaps two rows of this matrix. That is done in constant time, as only two array
      * entries of the two-dimensional matrix must be swapped.
      * 
      * @param srcRow The first row.
      * @param destRow The second row.
      */
     public void swapRows(int srcRow, int destRow) {
         K[] tmp = this.matrix[srcRow];
         this.matrix[srcRow] = this.matrix[destRow];
         this.matrix[destRow] = tmp;
     }
     
     
     
     /**
      * Swaps two elements of this matrix.
      * 
      * @param srcRow The row index of the first element.
      * @param srcCol The column index of the first element
      * @param destRow The row index of the second element.
      * @param destCol The column index of the second element.
      */
     public void swap(int srcRow, int srcCol, int destRow, int destCol) {
         K tmp = this.matrix[srcRow][srcCol];
         this.matrix[srcRow][srcCol] = this.matrix[destRow][destCol];
         this.matrix[destRow][destCol] = tmp;
     }
     
     
     
     /**
      * Determines whether this matrix can be multiplied with the other matrix. This is the
      * case if this matrix' column count equals the other matrix' row count.
      * 
      * @param other The matrix to multiply this with.
      * @return Whether this matrix can be multiplied with the other matrix.
      */
     public boolean canMultiplyWith(Matrix<K> other) {
         return this.canMultiplyWith(other.matrix);
     }
 
 
     
     /**
      * Determines whether this matrix can be multiplied with the other matrix given as 
      * array. This is the case if this matrix' column count equals the other matrix' row 
      * count.
      * 
      * @param other The matrix to multiply this with.
      * @return Whether this matrix can be multiplied with the other matrix.
      */
     public boolean canMultiplyWith(K[][] other) {
         return this.n == this.getRows(other);
     }
     
     
     
     private int getRows(K[][] other) {
         return other.length;
     }
     
     
     
     private int getCols(K[][] other) {
         return other[0].length;
     }
     
     
     
     /**
      * Determines whether this is the zero matrix. This is the case if this matrix  
      * contains zero in every element.
      * 
      * @return <code>true</code> iff this matrix contains only zeros.
      */
     public boolean isZero() {
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 if (!this.matrix[i][j].equals(this.field.getAdditiveNeutral())) {
                     return false;
                 }
             }
         }
         return true;
     }
     
     
     
     /**
      * Determines whether this matrix is the identical matrix. That is, it must be
      * quadratic and may contain only zeros except on its diagonal where it must have 
      * ones.
      * 
      * @return <code>true</code> if this matrix is quadratic and the identical matrix.
      */
     public boolean isId() {
         if (this.m != this.n) {
             return false;
         }
         K zero = this.field.getAdditiveNeutral();
         K one = this.field.getMultiplicativeNeutral();
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 if (i == j && !this.matrix[i][j].equals(one)) {
                     return false;
                 } else if (!this.matrix.equals(zero)) {
                     return false;
                 }
             }
         }
         
         return true;
     }
     
     
     
     @Override
     public String toString() {
         StringBuilder b = new StringBuilder();
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 b.append(this.matrix[i][j] == null ? "null" : this.matrix[i][j].toString());
                 
                 if (j < this.n - 1) {
                     b.append(" ");
                 }
             }
             b.append("\n");
         }
         return b.toString();
     }
     
     
     
     /**
      * Creates a formatted String from this matrix in which each element is aligned using
      * the length of the longest entry in the same column. All columns will be 
      * right-aligned and separated by a single space.
      * 
      * @return A formatted String representation of this matrix.
      * @see #toAlignedString(String, String, boolean)
      */
     public String toAlignedString() {
         return this.toAlignedString(" ", "", false);
     }
     
     
     
     /**
      * Creates a formatted String from this matrix in which the alignment and delimiter 
      * characters can be chosen. Columns are aligned by padding each element by a number
      * of spaces until its length equals the length of the longest entry in the same 
      * column. The user can choose the column delimiter and additionally a string which is
     * put after each line. This can be useful for example to produce a LaTeX 
      * representation of this matrix (this.toAlignedString(" & ", "\\\\", true) will 
      * return a String suitable for LaTex documents). 
      * 
      * @param colDelimiter Column delimiter which is put between each column. If empty, 
      *          columns are not separated.
      * @param rowEnd A String which is put at the end of each row before the newline 
      *          character.
      * @param alignLeft If set to true, each column will be left aligned instead of right.
      * @return A formatted String representation of this matrix.
      */
     public String toAlignedString(String colDelimiter, String rowEnd, boolean alignLeft) {
         int[] colLengths = new int[this.n];
         
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 colLengths[j] = Math.max(colLengths[j], 
                     this.matrix[i][j].toString().length());
             }
         }
         
         
         StringBuilder b = new StringBuilder();        
         for (int i = 0; i < this.m; ++i) {
             for (int j = 0; j < this.n; ++j) {
                 int desiredLength = colLengths[j];
                 String s = this.matrix[i][j].toString();
                 
                 if (alignLeft) {
                     b.append(s);
                     this.padSpaces(desiredLength, s.length(), b);
                 } else {
                     this.padSpaces(desiredLength, s.length(), b);
                     b.append(s);
                 }
                 
                 if (j < this.n - 1) {
                     b.append(colDelimiter);
                 }
             }
             
             b.append(rowEnd);
             if (i < this.m - 1) {
                 b.append("\n");
             }
         }
         
         return b.toString();
     }
     
     
     
     private void padSpaces(int desiredLength, int currentLength, StringBuilder b) {
         int spaces = desiredLength - currentLength;
         if (spaces <= 0) {
             return;
         }
         for (int i = 0; i < spaces; ++i) {
             b.append(" ");
         }
     }
 
 
 
     @Override
     public int hashCode() {
         final int prime = 31;
         int result = 1;
         result = prime * result
             + ((this.field == null) ? 0 : this.field.hashCode());
         result = prime * result + Arrays.hashCode(this.matrix);
         return result;
     }
 
 
 
     @SuppressWarnings("rawtypes")
     @Override
     public boolean equals(Object obj) {
         if (this == obj) {
             return true;
         }
         if (obj == null) {
             return false;
         }
         if (!(obj instanceof Matrix)) {
             return false;
         }
         Matrix other = (Matrix) obj;
         if (this.field == null) {
             if (other.field != null) {
                 return false;
             }
         } else if (!this.field.equals(other.field)) {
             return false;
         } else if (this.m != other.m || this.n != other.n) {
             return false;
         }
         for (int i = 0; i < this.m; ++i) {
             if (!Arrays.equals(this.matrix[i], other.matrix[i])) {
                 return false;
             }
         }
         return true;
     }
 }
