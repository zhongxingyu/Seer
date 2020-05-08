 /*
  * MIPL: Mining Integrated Programming Language
  *
  * File: MatrixOperationTest.java
  * Author: Jin Hyung Park <jp2105@columbia.edu>
  * Reviewer: Young Hoon Jung <yj2244@columbia.edu>
  * Description: Matrix Operations Test Unit
  *
  */
 package edu.columbia.mipl.matops;
 
 import java.util.*;
 
 import junit.framework.Test;
 import junit.framework.TestCase;
 import junit.framework.TestSuite;
 import edu.columbia.mipl.datastr.*;
 import edu.columbia.mipl.matops.*;
 import edu.columbia.mipl.builtin.matrix.*;
 
 public class MatrixOperationTest extends TestCase {
 	double data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 	double data3x3_2[] = {2, 4, 6, 2, 4, 6, 2, 4, 6};
 	double data3x3_3[] = {2.0, 2.0, 0.0, -2.0, 1.0, 1.0, 3.0, 0.0, 1.0};
 	double data3x3_add_1_2[] = {3, 6, 9, 3, 6, 9, 3, 6, 9};
 	double data3x3_sub_1_2[] = {-1, -2, -3, -1, -2, -3, -1, -2, -3};
 	double data3x3_mult_1_2[] = {12, 24, 36, 12, 24, 36, 12, 24, 36};
 
 	double data1x3_1[] = {1, 2, 3};
 	double data3x1_2[] = {1, 2, 3};
 	double data1x1_mult_1_2[] = {14};
 	protected MatrixOperations matOpObj;
 
 	public static void main(String args[]) {
 		junit.textui.TestRunner.run (suite());
 	}
 	@Override
 	protected void setUp() {
 		matOpObj = new DefaultMatrixOperations();
 	}
 
 	public void testMatrixSame() {
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat3x3 = new PrimitiveMatrix((PrimitiveArray) mat3x3_1);
 
 		assertTrue(mat3x3.getData().equalsSemantically(mat3x3.getData()));
 	}
 
 	public void testMatrixAdd() {
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 
 		final PrimitiveDoubleArray mat3x3_3_add_1_2 = new PrimitiveDoubleArray(3, 3, data3x3_add_1_2);
         final PrimitiveMatrix matR = new PrimitiveMatrix(mat3x3_3_add_1_2);
 
 		PrimitiveMatrix mat = matOpObj.add(mat1, mat2);
 
 		assertTrue(mat.getData().equalsSemantically(matR.getData()));
 	}
 
 	public void testMatrixSub() {
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 
 		final PrimitiveDoubleArray mat3x3_3_sub_1_2 = new PrimitiveDoubleArray(3, 3, data3x3_sub_1_2);
         final PrimitiveMatrix matR = new PrimitiveMatrix(mat3x3_3_sub_1_2);
 
 		PrimitiveMatrix mat = matOpObj.sub(mat1, mat2);
 
 		assertTrue(mat.getData().equalsSemantically(matR.getData()));
 	}
 
 	public void testMatrixMult_3x3() {
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 
 		final PrimitiveDoubleArray mat3x3_3_mult_1_2 = new PrimitiveDoubleArray(3, 3, data3x3_mult_1_2);
         final PrimitiveMatrix matR = new PrimitiveMatrix(mat3x3_3_mult_1_2);
 
 		PrimitiveMatrix mat = matOpObj.mult(mat1, mat2);
 
 		assertTrue(mat.getData().equalsSemantically(matR.getData()));
 	}
 
 	public void testMatrixMult_1x3_3x1_1x1() {
 		final PrimitiveDoubleArray mat1x3_1 = new PrimitiveDoubleArray(1, 3, data1x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat1x3_1);
 
 		final PrimitiveDoubleArray mat3x1_2 = new PrimitiveDoubleArray(3, 1, data3x1_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x1_2);
 
 		final PrimitiveDoubleArray mat1x1_3_mult_1_2 = new PrimitiveDoubleArray(1, 1, data1x1_mult_1_2);
         final PrimitiveMatrix matR = new PrimitiveMatrix(mat1x1_3_mult_1_2);
 
 		PrimitiveMatrix mat = matOpObj.mult(mat1, mat2);
 
 		assertTrue(mat.getData().equalsSemantically(matR.getData()));
 	}
 
 	public void testMatrixMult_3x3_by_scalar() {
 		final double scalar = 2;
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 
 		PrimitiveMatrix mat = matOpObj.mult(mat1, scalar);
 
 		assertTrue(mat.getData().equalsSemantically(mat2.getData()));
 	}
 
 	public void testMatrix_Assign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 
 		matOpObj.assign(mat1, mat2);
 
 		assertTrue(mat1.getData().equalsSemantically(mat2.getData()));
 	}
 
 	public void testMatrix_addAssign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_2[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_3[] = {2, 4, 6, 2, 4, 6, 2, 4, 6};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray mat3x3_3 = new PrimitiveDoubleArray(3, 3, copy_data3x3_3);
         final PrimitiveMatrix mat3 = new PrimitiveMatrix(mat3x3_3);
 
 		matOpObj.addassign(mat1, mat2);
 
 		assertTrue(mat3.getData().equalsSemantically(mat1.getData()));
 	}
 
 	public void testMatrix_subAssign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_2[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_3[] = {0, 0, 0, 0, 0, 0, 0, 0, 0};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray mat3x3_3 = new PrimitiveDoubleArray(3, 3, copy_data3x3_3);
         final PrimitiveMatrix mat3 = new PrimitiveMatrix(mat3x3_3);
 
 		matOpObj.subassign(mat1, mat2);
 
 		assertTrue(mat3.getData().equalsSemantically(mat1.getData()));
 	}
 
 	public void testMatrix_multAssign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_2[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_3[] = {6, 12, 18, 6, 12, 18, 6, 12, 18};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray mat3x3_3 = new PrimitiveDoubleArray(3, 3, copy_data3x3_3);
         final PrimitiveMatrix mat3 = new PrimitiveMatrix(mat3x3_3);
 
 		matOpObj.multassign(mat1, mat2);
 
 		assertTrue(mat3.getData().equalsSemantically(mat1.getData()));
 	}
 
 	public void testMatrix_cellMultAssign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_2[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_3[] = {1, 4, 9, 1, 4, 9, 1, 4, 9};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray mat3x3_3 = new PrimitiveDoubleArray(3, 3, copy_data3x3_3);
         final PrimitiveMatrix mat3 = new PrimitiveMatrix(mat3x3_3);
 
 		matOpObj.cellmultassign(mat1, mat2);
 
 		assertTrue(mat3.getData().equalsSemantically(mat1.getData()));
 	}
 
 	public void testMatrix_cellDivAssign() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_2[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data3x3_3[] = {1, 1, 1, 1, 1, 1, 1, 1, 1};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray mat3x3_3 = new PrimitiveDoubleArray(3, 3, copy_data3x3_3);
         final PrimitiveMatrix mat3 = new PrimitiveMatrix(mat3x3_3);
 
 		matOpObj.celldivassign(mat1, mat2);
 
 		assertTrue(mat3.getData().equalsSemantically(mat1.getData()));
 	}
 
 	public void testMatrix_Transpose() {
 		final PrimitiveDoubleArray mat1x3_1 = new PrimitiveDoubleArray(1, 3, data1x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat1x3_1);
 		final PrimitiveDoubleArray mat3x1_1 = new PrimitiveDoubleArray(3, 1, data3x1_2);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x1_1);
 		PrimitiveMatrix mat = matOpObj.transpose(mat1);
 
 		assertTrue(mat.getData().equalsSemantically(mat2.getData()));
 	}
 
     /* There is no Determinant function on MatrixOperations */
     /*
 	public void testMatrix_Determinant() {
 		double data2x2_1[] = {2, 2, 1, 3};
 		final PrimitiveDoubleArray mat2x2_1 = new PrimitiveDoubleArray(2, 2, data2x2_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat2x2_1);
 
 		double det = matOpObj.determinant(mat1);
 
 		assertTrue(det == 4);
 	}
     */
 
     /* There is no Minor function on MatrixOperations */
     /*
 	public void testMatrix_Minor() {
 		double copy_data3x3_1[] = {1, 2, 3, 1, 2, 3, 1, 2, 3};
 		double copy_data2x2_1[] = {1, 3, 1, 3};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat2x2_1 = new PrimitiveDoubleArray(2, 2, copy_data2x2_1);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat2x2_1);
 
 		// remove 2nd row and 2nd col, so input row and col will be 1, 1
 		PrimitiveMatrix matI = matOpObj.minor(mat1, 1, 1);
 
 		assertTrue(matI.getData().equalsSemantically(mat2.getData()));
 	}
     */
 
 	public void testMatrix_Inverse() {
 		double copy_data3x3_1[] = {1, -1, 1, 0, 2, -1, 2, 3, 0};
 		double i_data3x3_1[] = {1, 0, 0, 0, 1, 0, 0, 0, 1};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray i_mat3x3 = new PrimitiveDoubleArray(3, 3, i_data3x3_1);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(i_mat3x3);
 
 		PrimitiveMatrix matI = matOpObj.inverse(mat1);
 		PrimitiveMatrix matII = matOpObj.mult(matI, mat1);
 
 		assertTrue(matII.getData().equalsSemantically(mat2.getData()));
 	}
 	
 	public void testMatrix_div() {
 		double copy_data3x3_1[] = {1, -1, 1, 0, 2, -1, 2, 3, 0};
 		double i_data3x3_1[] = {1, 0, 0, 0, 1, 0, 0, 0, 1};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray mat3x3_2 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(mat3x3_2);
 		final PrimitiveDoubleArray i_mat3x3 = new PrimitiveDoubleArray(3, 3, i_data3x3_1);
         final PrimitiveMatrix i_mat = new PrimitiveMatrix(i_mat3x3);
 
 		/* mat3x3_1 and mat3x3_2 are same matrix,
 		 * so, the division result will be the identity matrix i_mat3x3 */
 		PrimitiveMatrix matI = matOpObj.div(mat1, mat2);
 
 		assertTrue(matI.getData().equalsSemantically(i_mat.getData()));
 	}
 
 	public void testMatrix_sum() {
 		double copy_data3x3_1[] = {1, -1, 1, 
 					   0, 2, -1, 
 					   2, 3, 0};
 		double result = 7;
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 
 		double compare = matOpObj.sum(mat1);
 
 		assertTrue(compare == result);
 	}
 
 	public void testMatrix_rowsum() {
 		double copy_data3x3_1[] = {1, -1, 1, 
 					   0, 2, -1, 
 					   2, 3, 0};
 		double result_rowsum[] = {3, 4, 0};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray r_rowsum_a = new PrimitiveDoubleArray(1, 3, result_rowsum);
         final PrimitiveMatrix r_rowsum = new PrimitiveMatrix(r_rowsum_a);
 
 		PrimitiveMatrix matI = matOpObj.rowsum(mat1);
 
 		assertTrue(r_rowsum.getData().equalsSemantically(matI.getData()));
 	}
 
 	public void testMatrix_mean() {
 		double copy_data3x3_1[] = {1, -1, 1, 
 					   0, 2, -1, 
 					   2, 3, 0};
 		double result = 7.0/9;
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 
 		double compare = matOpObj.mean(mat1);
 
 		assertTrue(compare == result);
 	}
 
 	public void testMatrix_rowmean() {
 		double copy_data3x3_1[] = {1, -1, 1, 
 					   0, 2, -1, 
 					   2, 3, 0};
 		double result_rowmean[] = {1, 4.0/3, 0};
 		final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, copy_data3x3_1);
         final PrimitiveMatrix mat1 = new PrimitiveMatrix(mat3x3_1);
 		final PrimitiveDoubleArray r_rowmean = new PrimitiveDoubleArray(1, 3, result_rowmean);
         final PrimitiveMatrix mat2 = new PrimitiveMatrix(r_rowmean);
 
 		PrimitiveMatrix matI = matOpObj.rowmean(mat1);
 
 		assertTrue(mat2.getData().equalsSemantically(matI.getData()));
 	}
 
     public void testMatrix_addUnbound() {
         double data3x3_1[] = {1, 1, 1, 2, 2, 2, 3, 3, 3,};
         double data_unbound_row[] = {1, 1, 1};
         double result3x3_1[] = {2, 2, 2, 3, 3, 3, 4, 4, 4};
         final PrimitiveDoubleArray mat3x3_1 = new PrimitiveDoubleArray(3, 3, data3x3_1);
         final PrimitiveMatrix mat3x3 = new PrimitiveMatrix(mat3x3_1);
 
         final PrimitiveDoubleArray unbound_row = new PrimitiveDoubleArray(1, 3, data_unbound_row);
         final PrimitiveMatrix mat1x3 = new PrimitiveMatrix(unbound_row);
         final UnboundRowMatrix urMat = new UnboundRowMatrix(mat1x3);
 
         final PrimitiveDoubleArray resultData = new PrimitiveDoubleArray(3, 3, result3x3_1);
         final PrimitiveMatrix<Double> result3x3 = new PrimitiveMatrix(resultData);
 
        PrimitiveMatrix resultMat = matOpObj.add(mat3x3, urMat);
 
         assertTrue(resultMat.getData().equalsSemantically(result3x3.getData()));
     }
 
 	public static Test suite() {
 		return new TestSuite(MatrixOperationTest.class);
 	}
 }
