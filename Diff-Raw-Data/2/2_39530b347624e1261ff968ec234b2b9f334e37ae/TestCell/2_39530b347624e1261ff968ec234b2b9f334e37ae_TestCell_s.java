 package com.hexcore.cas.model.test;
 
 import static org.junit.Assert.*;
 
 import org.junit.Test;
 
 import com.hexcore.cas.model.Cell;
 
 
 public class TestCell
 {
 	private Cell cell;
 	
 	@Test
 	public void test1CellConstructors()
 	{
 		cell = new Cell(3);
 		assertEquals(3, cell.getValueCount());
 		assertEquals(0.0, cell.getValue(0), 0.0);
 		assertEquals(0.0, cell.getValue(1), 0.0);
 		assertEquals(0.0, cell.getValue(2), 0.0);
 		
 		// Test initialising with an array
 		double[] testValues = {10.0, 5.0, 2.0};
 		cell =  new Cell(testValues);
 		assertEquals(3, cell.getValueCount());
 		assertEquals(10.0, cell.getValue(0), 0.0);
 		assertEquals(5.0, cell.getValue(1), 0.0);
 		assertEquals(2.0, cell.getValue(2), 0.0);
 		assertEquals(0.0, cell.getValue(-1), 0.0);
 		assertEquals(0.0, cell.getValue(3), 0.0);
 		
 		// Test copy constructor
 		Cell cell2 = new Cell(cell);
 		assertEquals(3, cell2.getValueCount());
 		assertEquals(10.0, cell2.getValue(0), 0.0);
 		assertEquals(5.0, cell2.getValue(1), 0.0);
 		assertEquals(2.0, cell2.getValue(2), 0.0);
 		assertEquals(0.0, cell2.getValue(-1), 0.0);
 		assertEquals(0.0, cell2.getValue(3), 0.0);
 		
 		// Ensure a proper copy was made
 		cell.setValue(0, 99.0);
 		cell.setValue(1, 98.0);
 		cell.setValue(2, 97.0);
 		
 		assertEquals(10.0, cell2.getValue(0), 0.0);
 		assertEquals(5.0, cell2.getValue(1), 0.0);
 		assertEquals(2.0, cell2.getValue(2), 0.0);	
 	}
 	
 	@Test
 	public void test2Values()
 	{
 		cell = new Cell(2);
 		cell.setValue(0, 1);
 		cell.setValue(1, 3);
 		
 		assertEquals(2, cell.getValueCount());
 		assertEquals(1.0, cell.getValue(0), 0.0);
 		assertEquals(3.0, cell.getValue(1), 0.0);
 		assertEquals(0.0, cell.getValue(-1), 0.0);
 		assertEquals(0.0, cell.getValue(3), 0.0);
 		
 		double[] testVals = cell.getValues();
 		assertEquals(1.0, testVals[0], 0.0);
 		assertEquals(3.0, testVals[1], 0.0);
 	}
 	
 	@Test
 	public void testPrivateProperties()
 	{
 		Cell c = new Cell(2);
 		c.setPrivateProperty("pp1", 23.2);
 		c.setPrivateProperty("pp2", 1);
 		
 		assertEquals(23.2, c.getPrivateProperty("pp1"), 0.0);
 		assertEquals(1, c.getPrivateProperty("pp2"), 0.0);
		assertEquals(0, c.getPrivateProperty("umm"), 0.0);
 	}
 }
