 package com.hexcore.test;
 import com.hexcore.cas.math.Vector2i;
 import com.hexcore.cas.model.TriangleGrid;
 
 import junit.framework.TestCase;
 
 public class TestTriangleGrid extends TestCase
 {
 	private final TriangleGrid g = new TriangleGrid(new Vector2i(5, 10));
 	
 	public void test1Width()
 	{
		int expectedResults = 5;
 		assertEquals(expectedResults, g.getWidth());
 	}
 	
 	public void test2Height()
 	{
		int expectedResults = 10;
 		assertEquals(expectedResults, g.getHeight());
 	}
 	
 	public void test3Size()
 	{
 		Vector2i expectedResults = new Vector2i(5, 10);
		assertEquals(expectedResults, g.getSize());
 	}
 	
 	/*
 	 * Need cell implementation for this test case
 	 * public void test4GetCell()
 	 * {
 	 * }
 	 */
 	
 	/*
 	 * Need cell implementation for this test case
 	 * public void test5GetNeighbours()
 	 * {
 	 * }
 	 */
 }
