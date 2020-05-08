 package org.lilian.util;
 
 import static org.junit.Assert.*;
 import static org.lilian.util.Series.series;
 
 import java.util.ArrayList;
 import java.util.List;
 import static java.lang.Math.*;
 
 import org.apache.commons.math.linear.Array2DRowRealMatrix;
 import org.apache.commons.math.linear.RealMatrix;
 import org.junit.Test;
 
 public class MatrixToolsTest
 {
 
 	@Test 
 	public void testToString()
 	{
 		RealMatrix m = MatrixTools.toMatrix("0.0, 0.0; 0.0, 0.0");
 		assertEquals(new Array2DRowRealMatrix(2, 2), m);
 
 		m = MatrixTools.toMatrix("1.0, 0.0; 0.0, 1.0");
 		RealMatrix exp = new Array2DRowRealMatrix(new double[][] {{1.0, 0.0}, {0.0, 1.0}});
 		assertEquals(exp, m);
 		
 	}
 
 	@Test
 	public void test()
 	{	
 		RealMatrix a = MatrixTools.toMatrix("1.0,  0.0; 0.0, 1.0");		
 		RealMatrix b = MatrixTools.toMatrix("0.0, -1.0; 1.0, 0.0");
 		
 		System.out.println(a.multiply(b));		
 	}	
 	
 	@Test
 	public void testToRotationMatrix1()
 	{	
 		List<Double> angles;
  
 		RealMatrix expected = MatrixTools.toMatrix("0.0, -1.0; 1.0, 0.0");
 		angles = new ArrayList<Double>();
 		
 		angles.add(0.5 * PI); // 90 deg
 		
 		RealMatrix result = MatrixTools.toRotationMatrix(angles);
 		
 		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
 	}
 
 	@Test
 	public void testToRotationMatrix2()
 	{	
 
 		RealMatrix expected = MatrixTools.toMatrix("0.0, 0.0, 1.0; 0.0, -1.0, -0.0; 1.0, 0.0, 0.0");
 		List<Double> angles = new ArrayList<Double>();
 		for(int i = 0; i < 3; i ++)
 			angles.add(0.5 * PI); // 90 deg
 		
 		RealMatrix result = MatrixTools.toRotationMatrix(angles);
 		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
 	}
 	
 	@Test
 	public void testToRotationMatrix3()
 	{	
  		
  		RealMatrix expected = MatrixTools.toMatrix("0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 1.0; 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -0.0, 0.0, -1.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0; 0.0, -0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, -0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, 0.0, 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, -0.0; 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 0.0, -1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0; 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0");
  		List<Double> angles = new ArrayList<Double>();
  		for(int i = 0; i < 90; i ++)
  			angles.add(0.5 * PI); // 90 deg
  		
  		RealMatrix result = MatrixTools.toRotationMatrix(angles);
  		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
 	}
 
 	
 	@Test
 	public void testToRotationMatrix4()
 	{	
 		int n = 10;
 		RealMatrix expected = MatrixTools.identity(n);
 		
 		List<Double> angles = new ArrayList<Double>();
 		for(int i = 0; i < ((n*n)-n)/2; i ++)
 			angles.add(0.0);
 		
 		RealMatrix result = MatrixTools.toRotationMatrix(angles);
 		
 		assertTrue(MatrixTools.equals(result, expected, 1.0E-10));
 	}
 	
 	@Test
 	public void testToRotationMatrix5()
 	{	
 		int n = 30;
 		RealMatrix expected = MatrixTools.identity(n);
 		
 		List<Double> angles = new ArrayList<Double>();
 		for(int i = 0; i < ((n*n)-n)/2; i ++)
 			angles.add(0.5 * PI); 
 		
 		RealMatrix result = MatrixTools.toRotationMatrix(angles);
 		
 		for(int i : series(n))
 			for(int j : series(n))
 			{
 				double d0 = Math.abs(0.0 - Math.abs(result.getEntry(i, j)));				
 				double d1 = Math.abs(1.0 - Math.abs(result.getEntry(i, j)));
 			
 				assertTrue(d0 < 1.0E-10 || d1 < 1.0E-10);
 			}
 	}
 
	@Test
 	public void testIsInvertible()
 	{
 		RealMatrix mat = MatrixTools.identity(3);
 		
 		assertTrue(MatrixTools.isInvertible(mat));
		
		mat = MatrixTools.toMatrix("0, 0, 1; 0, 1, 0; 0, 0, 0");
		
		assertFalse(MatrixTools.isInvertible(mat));
	
 	}
 }
