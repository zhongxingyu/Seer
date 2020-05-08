 package de.skuzzle.polly.parsing.util;
 
 import de.skuzzle.polly.parsing.util.Matrix.DimensionException;
 
 
 /**
  * This class provides several static methods for operations on {@link Matrix matrices}.
  *  
  * @author Simon
  */
 public class MatrixUtils {
     
     
     
     /**
      * Calculates the determinant of quadratic matrices. This is done by creating an 
      * internal copy and transforming it into an echelon form. The determinant is then
      * calculated by building the product of the diagonal elements.
      *   
      * @param matrix The matrix to calculate the determinant from.
      * @return The determinant of the matrix.
      * @throws DimensionException if the matrix is not quadratic.
      */
     public static <K> K getDeterminant(Matrix<K> matrix) {
         if (matrix.getM() != matrix.getN()) {
             throw new DimensionException("illegal dimension");
         }
         Matrix<K> copy = new Matrix<K>(matrix);
         K product = matrix.getField().getMultiplicativeNeutral();
         toEchelon(copy);
         for (int i = 0; i < matrix.getM(); ++i) {
             product = matrix.getField().multiply(product, copy.get(i, i));
         }
         return product;
     }
     
     
     
     /**
      * Determines whether the given matrix is invertible. This is the case if the
      * determinant of the matrix is zero. For all non-quadratic matrices, this method
      * will instantly return <code>false</code>. All other matrices are considered 
      * invertible if their determinants is different from zero.
      * 
      * @param matrix The matrix to check.
      * @return Whether the matrix is invertible.
      */
     public static <K> boolean isInvertible(Matrix<K> matrix) {
         if (matrix.getM() != matrix.getN()) {
             return false;
         }
         K k = getDeterminant(matrix);
         return !k.equals(matrix.getField().getAdditiveNeutral());
     }
     
     
     
     /**
      * Checks whether the given matrix is the identical matrix. That is, the matrix must 
      * be quadratic and contain only zeros except for the diagonal elements which must be 
      * the one element of the underlying field. More specific, this method returns 
      * <code>true</code> if and only if the {@code matrix.getLeftId().equals(matrix}.
      * 
      * @param matrix The matrix to check.
      * @return
      */
     public static <K> boolean isIdentity(Matrix<K> matrix) {
         if (matrix.getN() != matrix.getN()) {
             return false;
         }
         return matrix.getLeftId().equals(matrix);
     }
 
     
     
     public static void main(String[] args) {
         Integer[][] m = {
             {1, 2, 3},
             {2, 3, 4},
             {4, 5, 6}
         };
         
         Matrix<Integer> matrix = new Matrix<Integer>(m, Fields.integerModulo(7));
         System.out.println(toGaussForm(matrix));
     }
     
     
     
     /**
      * Tries to calculate the inverted matrix to a given matrix.
      * 
      * @param matrix The matrix to invert.
      * @return An inverted matrix if possible.
      * @throws DimensionException if the given matrix is not quadratic.
      */
     public static <K> Matrix<K> invert(Matrix<K> matrix) {
         if (matrix.getN() != matrix.getM()) {
             throw new DimensionException("illegal dimension");
         }
         Matrix<K> result = Matrix.merge(matrix, matrix.getLeftId());
         toGaussForm(result);
        
         return Matrix.splitRight(result, matrix.getN());
     }
     
     
     
     /**
      * Transforms the given matrix into gaussian normal form. This is done in place, thus
      * the elements in the given matrix will be changed by this algorithm.
      * 
      * The matrix will be transformed into the following block form:<br/> 
      * {@code /E C\} <br/>
      * {@code \0 0/} <br/>
      * 
      * Where E is the identical matrix.
      * 
      * @param matrix The matrix to transform.
      * @returns The same matrix instance as given by the parameter.
      */
     public static <K> Matrix<K> toGaussForm(Matrix<K> matrix) {
         if (matrix.isZero()) {
             return matrix;
         }
         Field<K> f = matrix.getField();
         
         // first step: produce echolon form
         toEchelon(matrix);
         
         // second step: produce a one in each row on the diagonal. As by step 1, below
         // the diagonal only exists zeros. So the first element != 0 from the left on 
         // each row is the diagonal entry.
         // 
         // algorithm:
         //      * for each row 'i'
         //      * find the first column 'j' that contains no zero
         //      * multiply the whole row with the additive inverse of the element at 
         //            A[i, j] 
         for (int i = 0; i < matrix.getM(); ++i) {
             K pivot = matrix.get(i, i);
             K invPivot = f.getMultiplicativeInverse(pivot);
             matrix.multiplyRowWith(invPivot, i);
         }
         
         // third step: produce zeros above the diagonal of ones
         // algorithm: 
         //      * start at the last row 'i'
         //      * find the first column 'j' that contains no zero (as by step 2, it must 
         //            then contain a one)
         //      * if row consists of only zeros, continue with next row.
         //      * add k times row 'i' to all rows 'i1' above i with 
         //            k =  A[i, j] * A[i1, j]^-1 produce a zero in A[i1, j]
         for (int i = matrix.getM() - 1; i > 0; --i) {
             for (int i1 = 0; i1 < i; ++i1) {
                 K a_i1 = matrix.get(i1, i);
                 K factor = f.getAdditiveInverse(a_i1);
                 matrix.addMultipleToRow(factor, i, i1);
             }
         }
         
         return matrix;
     }
     
     
     
     /**
      * <p>Creates an echolon form of the given matrix. That is, each element below the 
      * main diagonal of the matrix will be zero afterwards.</p>
      * 
      * <p>This operation is done in place and thus changes the elements of the given 
      * matrix.</p>
      * 
      * @param matrix The matrix to transform into echolon form.
      * @returns The same matrix instance as given by the parameter.
      */
     public static <K> Matrix<K> toEchelon(Matrix<K> matrix) {
         Field<K> f = matrix.getField();
         
         // produce zeros below the diagonal
         for (int i = 0; i < matrix.getM() - 1; ++i) {
             for (int j = i + 1; j < matrix.getM(); ++j) {
                 
                 K a_i = matrix.get(i, i);
                 K a_j = matrix.get(j, i);
                 
                 K scalar = f.getAdditiveInverse(a_j);
                 
                 scalar = f.multiply(scalar, f.getMultiplicativeInverse(a_i));
                 matrix.addMultipleToRow(scalar, i, j);
             }
         }
         
         return matrix;
     }
     
     
     
     /**
      * Calculates the rank of the given matrix by transforming a copy of it into
      * gauss form and then counting the size {@code k} of the identity matrix in the
      * upper left corner of the gaussian matrix.
      * 
      * @param matrix The matrix which rank should be calculated.
      * @return The rank of the matrix.
      */
     public static <K> int rank(Matrix<K> matrix) {
         K one = matrix.getField().getMultiplicativeNeutral();
         Matrix<K> copy = new Matrix<K>(matrix);
         toGaussForm(copy);
         int i = 0;
         for(; i < copy.getM() && copy.get(i, i).equals(one); ++i);
         return i;
     }
     
     
     
     /*private static <K> Matrix<K> addToRow(
                 K scalar, int srcRow, int destRow, Matrix<K> matrix) {
         
         // create a matrix to multiply with
         Matrix<K> id = matrix.getRightId().set(destRow, srcRow, scalar);
         
         return id.multiplyWith(matrix);
     }
     
 
     
     private static <K> Matrix<K> switchRows(int srcRow, int destRow, Matrix<K> matrix) {
         K zero = matrix.getField().getAdditiveNeutral();
         K one = matrix.getField().getMultiplicativeNeutral();
         
         Matrix<K> id = matrix.getLeftId();
         id.set(srcRow, srcRow, zero);
         id.set(destRow, srcRow, one);
         id.set(destRow, destRow, zero);
         id.set(srcRow, destRow, one);
         
         return id.multiplyWith(matrix);
     } */
 }
