 package org.lilian.util;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.ArrayRealVector;
 import org.apache.commons.math.linear.LUDecompositionImpl;
 import org.apache.commons.math.linear.RealMatrix;
 import org.apache.commons.math.linear.RealVector;
 
 import static java.lang.Math.*;
 
 public class MatrixTools
 {
 	/**
 	 *  Helper function to quickly create a matrix. Handy if you 
 	 *  have a set of values you want entered into a matrix.  
 	 *  
 	 *  Rows are separated by a semicolon, values in rows by commas. Whitespace 
 	 *  is ignored. 
 	 *  "1, 2, 3; 4, 5, 6" represents the matrix 
 	 *  <pre>
 	 *  1 2 3
 	 *  4 5 6
 	 *  </pre>
 	 */ 
 	public static RealMatrix toMatrix(String string)
 	{
 		// TODO Handle syntax problems and give helpful errors
 		
 		int numRows, numCols;
 				
 		String[] rows = string.split(";");
 		
 		numRows = rows.length;
 		numCols = rows[0].split(",").length;
 		String [] strVals;
 		
 		RealMatrix matrix = new Array2DRowRealMatrix(numRows, numCols);
 		
 		for(int i = 0; i < numRows; i++)
 		{
 			strVals = rows[i].split(",");
 			for(int j = 0; j < numCols; j++)
 			{
 				double v = Double.valueOf(strVals[j].trim());
 				matrix.setEntry(i, j, v);
 			}
 		}
 		
 		return matrix;
 	}	
 	
 	public static RealMatrix identity(int dim)
 	{
 		RealMatrix m = new Array2DRowRealMatrix(dim, dim);
 		
 		for(int i = 0; i < dim; i++)
 			m.setEntry(i, i, 1.0);
 		
 		return m;
 	}
 	
 	public static RealMatrix diag(RealVector v)
 	{
 		RealMatrix m = identity(v.getDimension());
 		diag(m, v);
 		
 		return m;
 	}
 	
 	/**
 	 * Sets the values of vector v on the diagonal of matrix m
 	 * @param v
 	 * @param m
 	 */
 	public static void diag(RealMatrix m, RealVector v)
 	{
 		for(int i = 0; i < v.getDimension(); i ++)
 			m.setEntry(i, i, v.getEntry(i));
 	}
 	
 	public static RealVector toVector(List<Double> values)
 	{
 		return new ArrayRealVector((Double[]) values.toArray());
 	}
 	
 
 	/**
 	 * Transforms a given set of angles to a rotation matrix.  
 	 * 
 	 * To represent arbitrary rotations in dimension d, <code>(d^2-d)/2</code> 
 	 * angles are required. Therefore, when this function is provided with a 
 	 * angles, it assumes a dimension of (1+sqrt(1+8a))/2.  
 	 * 
 	 * @param angles A list of angles 
 	 * @param dimension The target dimension of the rotation transformation 
 	 * 					represented by the resulting matrix 
 	 * @return A transformation matrix constructed for the given angles
 	 */
 	public static RealMatrix toRotationMatrix(List<Double> angles)
 	{
 		// calculate the dimension
 		double dimDouble = (1.0 + sqrt(1.0 + 8.0 * angles.size()))/2.0;
 		int dim = (int)Math.floor(dimDouble);
 		
 		RealMatrix left, right;
 		
 		double[] cosa = new double[angles.size()];
 		double[] sina = new double[angles.size()];		
 		for(int i = 0; i < angles.size(); i++)
 		{
 			cosa[i] = cos(angles.get(i));
 			sina[i] = sin(angles.get(i));
 		}
 		
 		int k;
 
 		left   = identity(dim);
 		right  = new Array2DRowRealMatrix(dim, dim);
 		
 		for(int i = 0; i < dim-1; i++)
 			for(int j = i+1; j < dim; j++)
 			{
 				// Reset the elementary rotation matrix (this should be faster
 				// than generating a new eye(dim) )
 				zero(right);
 				for(int m = 0; m < dim; m++)
 					right.setEntry(m, m, 1.0);
 
 				k = (((2 * dim - i - 1) * (i + 2))/2) - 2 * dim + j; 
 				// note that (2d-i-1)(i+2) is always an even number
 				// so we can keep the above equation in integers
 
 				right.setEntry(i, i,  cosa[k]);
 				right.setEntry(j, j,  cosa[k]);				
 				right.setEntry(i, j, -sina[k]);
 				right.setEntry(j, i,  sina[k]);
 	
 				// Multiply
 				left = left.multiply(right);
 			}
 	
 		return left;
 	}
 	
 	public static void zero(RealMatrix in)
 	{
 		// this can be optimized
 		for(int i = 0; i < in.getColumnDimension(); i++)
 			for(int j = 0; j < in.getRowDimension(); j++)
 				in.setEntry(i, j, 0.0);
 	}
 	
 	/**
 	 * Checks whether two matrices are equal, given a certain margin of error. 
 	 * Two matrices are considered equal if they have the same number of rows 
 	 * and columns and (m1.get(i, j) - m2.get(i,j)) &lquot; margin for all i 
 	 * and j. 
 	 * 
 	 * @param m1
 	 * @param m2
 	 * @return
 	 */	
 	public static boolean equals(RealMatrix m1, RealMatrix m2, double margin)
 	{
 		if(m1.getColumnDimension() != m2.getColumnDimension() || m1.getRowDimension() != m2.getRowDimension())
 			return false;
 
 		int numRows = m1.getRowDimension();
 		int numColumns = m1.getColumnDimension();
 		
 		for(int i = 0; i < numRows; i++)
 			for(int j = 0; j < numColumns; j++)
 				if( abs(m1.getEntry(i, j) - m2.getEntry(i, j)) >= margin)
 					return false;
 		
 		return true;
 	}
 	
 	public static boolean isInvertible(RealMatrix in)
 	{
		return ! new LUDecompositionImpl(in).getSolver().isNonSingular();
 	}
 	
 }
