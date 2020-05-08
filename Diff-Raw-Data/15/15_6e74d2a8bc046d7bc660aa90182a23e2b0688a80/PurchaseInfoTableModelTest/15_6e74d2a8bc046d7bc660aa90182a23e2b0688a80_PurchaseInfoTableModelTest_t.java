 package ee.ut.math.tvt.BSS;
 
 import static org.junit.Assert.assertEquals;
 
 import org.junit.Test;
 
 import ee.ut.math.tvt.salessystem.domain.data.SoldItem;
 import ee.ut.math.tvt.salessystem.domain.data.StockItem;
 import ee.ut.math.tvt.salessystem.ui.model.PurchaseInfoTableModel;
 
 public class PurchaseInfoTableModelTest {
 
 	PurchaseInfoTableModel model = new PurchaseInfoTableModel();
 	SoldItem i1 = new SoldItem(new StockItem((long) 1, "testItem1", "desc1", 3,
 			35), 2);
 
 	@Test
	public void testGetColumnValue() {
		long id = ((Long) model.getColumnValue(i1, 0)).longValue();
 		assertEquals(id, 1, 0.0001);
 		String name = (String) model.getColumnValue(i1, 1);
		assertEquals(name, "testItem1");
		double price = ((Double) model.getColumnValue(i1, 2)).doubleValue();
 		assertEquals(price, 3.35, 0.001);
		int quantity = ((Integer) model.getColumnValue(i1, 3)).intValue();
 		assertEquals(quantity, 2);
		double sum = ((Double) model.getColumnValue(i1, 4)).doubleValue();
 		assertEquals(sum, 6.70, 0.0001);
 	}
 
 	@Test(expected = IllegalArgumentException.class)
 	public void testHasEnoughInStock() {
 		model.addItem(i1);
 		model.getColumnValue(i1, 5);
 	}
 
 	@Test(expected = java.util.NoSuchElementException.class)
 	public void testQuantityInBasketByBarCodeException() {
 		model.getItemById((long) 20);
 	}
 
 
 }
