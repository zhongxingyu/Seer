 package ee.ut.math.tvt;
 
 import static org.junit.Assert.*;
 
 import org.junit.Before;
 import org.junit.Test;
 
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 
 public class StockItemTest {
 
	private StockItem test;
	
	@Before
	public void setUp() {
		test = new StockItem((long) 5, "Test", "Test", 15,10);
	}
 	@Test
 	public void testClone() {
		StockItem test3 = (StockItem) test.clone();
		// Checking fields individually as test.equals(test3) will return false
		boolean flag1 = (test.getId() == test3.getId());
		boolean flag2 = test.getName().equals(test3.getName());
		boolean flag3 = test.getDescription().equals(test3.getDescription());
		boolean flag4 = test.getPrice() == test3.getPrice();
		boolean flag5 = test.getQuantity() == test3.getQuantity();
		assertTrue(flag1 && flag2 && flag3 && flag4 && flag5);
 	}
 
 	@Test
 	public void testGetColumn() {
 		boolean flag1 = ((long) (test.getColumn(0))) == 5;
 		boolean flag2 = ((String) (test.getColumn(1))).equalsIgnoreCase("Test");
 		boolean flag3 = ((Double) (test.getColumn(2))) == 15;
 		boolean flag4 = ((int) (test.getColumn(3))) == 10;
 		assertTrue(flag1 && flag2 && flag3 && flag4);
 	}
 }
