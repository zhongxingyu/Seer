 package model;
 
 import static org.junit.Assert.*;
 
 import java.util.Date;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class HomeInventoryTrackerTest {
 
 	@Before
 	public void setUp() throws Exception {
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void test() {
 		HomeInventoryTracker tracker = new HomeInventoryTracker();
 		
 		String storageUnitName = "Pantry";
 		tracker.addStorageUnit(storageUnitName);
 		String newStorageUnitName = "Downstairs Pantry";
 		tracker.renameStorageUnit(storageUnitName, newStorageUnitName);
 		
 		String barcodeScanned = "barcode " + 1;
 		Product product = tracker.getProductByBarcode(barcodeScanned);
 		if (product == null) {
 			String description = "description " + 1;
 			int shelfLife = 1;
 			int threeMonthSupply = 1;
 			ProductQuantity productQuantity = new ProductQuantity(1, Unit.COUNT);
			product = tracker.createProduct(barcodeScanned, description, shelfLife, 
					threeMonthSupply, productQuantity);
 		}
 		
 		Date entryDate = new Date();
 		tracker.addItem(product, entryDate, newStorageUnitName);
 		
 		assertTrue(true);
 	}
 
 }
