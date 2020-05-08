 /**
  * 
  */
 package model;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 /**
  * @author Matthew
  *
  */
 @SuppressWarnings("deprecation")
 public class ItemTest {
 
 	private final Barcode validUPCABarcode = new Barcode("494180175762");
 	private final ItemManager itemManager = new MockItemManager();
 	private final ProductManager productManager = new MockProductManager();
 	private final ProductQuantity pq = new ProductQuantity(2.2f, Unit.FLUID_OUNCES);
 	private final Product product = new Product("validBarcode", "A product", 
 			3, 3, pq, productManager);
 	private final ProductGroup productGroup = new ProductGroup("Test product group", 
 			pq, Unit.GALLONS);
 	private final Date entryDateLastMonth = new Date(113, 0, 1, 12, 45, 45);
 	
 	private Item item;
 	
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@Before
 	public void setUp() throws Exception {
 		item = new Item(validUPCABarcode, product, productGroup, entryDateLastMonth, itemManager);
 		
 		// test Invariants
 		assertTrue(item.getProduct() != null);
 		assertTrue(item.getBarcode() != null);
 		assertTrue(item.getEntryDate() != null);
 		assertTrue(item.getExpirationDate() != null);
 	}
 
 	/**
 	 * @throws java.lang.Exception
 	 */
 	@After
 	public void tearDown() throws Exception {
 		// test Invariants
 		assertTrue(item.getProduct() != null);
 		assertTrue(item.getBarcode() != null);
 		assertTrue(item.getEntryDate() != null);
 		assertTrue(item.getExpirationDate() != null);
 	}
 
 	/**
 	 * Test method for {@link model.Item#Item(model.Barcode, model.Product,
 	 * model.ProductContainer, Date entryDate, ItemManager itemManager)}.
 	 */
 	@Test
 	public void testItem() {
		assertTrue(item.getBarcode().equals(validUPCABarcode));
 		assertTrue(item.getProduct().compareTo(product) == 0);
 		assertTrue(item.getContainer().equals(productGroup));
 		
 		assertTrue(item.getExitTime() == null);
 		
 		Date expiration = item.getExpirationDate();
 		Date entry = item.getEntryDate();
 		assertTrue(expiration != null);
 		assertTrue(entry != null);
 		
 		assertTrue(entry.getYear() == entryDateLastMonth.getYear());
 		assertTrue(entry.getMonth() == entryDateLastMonth.getMonth());
 		assertTrue(entry.getDate() == entryDateLastMonth.getDate());
 		assertTrue(entry.getHours() == entryDateLastMonth.getHours());
 		assertTrue(entry.getMinutes() == entryDateLastMonth.getMinutes());
 		
 		assertTrue(expiration.getYear() == entryDateLastMonth.getYear());
 		assertTrue(expiration.getMonth() == entryDateLastMonth.getMonth() + product.getShelfLife());
 		assertTrue(expiration.getDate() == entryDateLastMonth.getDate());
 		assertTrue(expiration.getHours() == entryDateLastMonth.getHours());
 		assertTrue(expiration.getMinutes() == entryDateLastMonth.getMinutes());
 	}
 	
 	/**
 	 * Test method for {@link model.Item#Item(model.Product, model.ProductContainer, 
 	 * ItemManager itemManager)}.
 	 */
 	@Test
 	public void testItemNoDateNoBarcode() {
 		item = new Item(product, productGroup, itemManager);
 		assertTrue(!item.getBarcode().equals(validUPCABarcode));
 		assertTrue(item.getProduct().compareTo(product) == 0);
 		assertTrue(item.getContainer().equals(productGroup));
 		
 		assertTrue(item.getExitTime() == null);
 		
 		Date expiration = item.getExpirationDate();
 		Date entry = item.getEntryDate();
 		assertTrue(expiration != null);
 		assertTrue(entry != null);
 		
 		assertTrue(entry.after(entryDateLastMonth));
 
 		assertTrue(expiration.getYear() == entry.getYear());
 		assertTrue(expiration.getMonth() == entry.getMonth() + product.getShelfLife());
 		assertTrue(expiration.getDate() == entry.getDate());
 		assertTrue(expiration.getHours() == entry.getHours());
 		assertTrue(expiration.getMinutes() == entry.getMinutes());
 	}
 	
 	/**
 	 * Test method for {@link model.Item#Item(java.lang.String, 
 	 * model.Product, model.ProductContainer)}.
 	 */
 	@Test
 	public void testItemInvalidBarcode() {
 		item = new Item(validUPCABarcode, product, productGroup, entryDateLastMonth, itemManager);
 	}
 
 	/**
 	 * Test method for {@link model.Item#remove()}.
 	 */
 	@Test
 	public void testRemove() {
 		Date exitTime = new Date();
 		item.remove();
 		assertTrue(item.getContainer() == null);
 		assertTrue(item.getExitTime().equals(exitTime));
 
 		item.remove();
 		assertTrue(item.getContainer() == null);
 		assertTrue(item.getExitTime().equals(exitTime));
 	}
 	
 	/**
 	 * Test method for {@link model.Item#compareTo(java.lang.Object)}.
 	 */
 	@Test
 	public void testCompareTo() {
 		Item sameItem = new Item(validUPCABarcode, product, productGroup, itemManager);
 		Item newItem = new Item(new Barcode("412345688919"), new Product("abc", "abcd", 
 				3, 3, pq, productManager), productGroup, itemManager);
 		assertTrue(item.compareTo(sameItem) == 0);
 		assertTrue(item.compareTo(newItem) != 0);
 	}
 }
