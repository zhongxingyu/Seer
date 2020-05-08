 package ee.ut.math.tvt;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 
 public class StockItemTest {
 
//	@Test
 	public void testClone() {
 		StockItem test = new StockItem((long) 5, "Test", "Test", 15);
 		Object test2 = test.clone();
 		StockItem test3 = (StockItem) test2;
 		assertTrue(test.equals(test3));
 	}
 	@Test
 	public void testGetColumn() {
		StockItem test = new StockItem((long) 5, "Test", "Test", 15);
 		boolean flag1 = ((long) (test.getColumn(0))) == 5;
 		boolean flag2 = ((String) (test.getColumn(1))).equalsIgnoreCase("Test");
		boolean flag3 = ((String) (test.getColumn(2))).equalsIgnoreCase("Test");
		boolean flag4 = ((int) (test.getColumn(1))) == 15;
 		assertTrue(flag1 && flag2 && flag3 && flag4);
 	}
 }
