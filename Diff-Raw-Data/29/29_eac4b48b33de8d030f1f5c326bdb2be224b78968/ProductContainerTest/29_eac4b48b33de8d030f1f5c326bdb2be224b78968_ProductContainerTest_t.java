 package model;
 
 import model.*;
 
 import static org.junit.Assert.*;
 
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 
 public class ProductContainerTest {
 	static StorageUnit storageUnit1;
 	static ProductGroup productGroup1;
 	static ProductGroup productGroup2;
 	static ProductGroup productGroup3;
 	static ProductGroup productGroup3Copy;
 	static Product product1;
 	static Product product1Copy;
 	static Product product2;
 	static Item item1;
 	static Item item1Copy;
 	static Item item2;
 	static StorageUnitManager storageUnitManager;
 	static ItemManager itemManager;
 	static ProductManager productManager;
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception {
 
 	}
 
 	@AfterClass
 	public static void tearDownAfterClass() throws Exception {
 		
 	}
 	
 	@Before	public void setUpBefore() throws Exception {
 		storageUnitManager = new MockStorageUnitManager();
 		itemManager = new MockItemManager();
 		productManager = new MockProductManager();
 		storageUnit1 = new StorageUnit("Cookie Jar");
		productGroup1 = new ProductGroup("Cookies", 
				new ProductQuantity(1, Unit.COUNT), Unit.KILOGRAMS);
		productGroup2 = new ProductGroup("Chocolate Chip Cookies", 
				new ProductQuantity(1, Unit.COUNT), Unit.KILOGRAMS);
		productGroup3 = new ProductGroup("No-Bake Cookies", 
				new ProductQuantity(1, Unit.COUNT), Unit.KILOGRAMS);
		productGroup3Copy = new ProductGroup("No-Bake Cookies", 
				new ProductQuantity(1, Unit.COUNT), Unit.KILOGRAMS);
		product1 = new Product("PROD1BARCODE", "Smelly socks", 0, 4, 
				new ProductQuantity(2, Unit.COUNT), productManager);
		product1Copy = new Product("PROD1BARCODE", "Smelly socks", 0, 4, 
				new ProductQuantity(2, Unit.COUNT), productManager);
		product2 = new Product("PROD2BARCODE00", "Green Jell-O", 365, 12, 
				new ProductQuantity(3, Unit.OUNCES), productManager);
 		item1 = new Item(new Barcode("400000001968"), product1, null, itemManager);
 		item1Copy = new Item(new Barcode("400000001968"), product1, null, itemManager);
 		item2 = new Item(new Barcode("400000001999"), product2, null, itemManager);
 	}
 
 	@After public void tearDownAfter() throws Exception {
 		
 	}
 
 	@Test
 	public void PGtestItems() {
 		System.out.print("Testing ProductGroup Item logic...");
 
 		assertEquals(0,productGroup1.getItemsSize());
 		assertFalse(productGroup1.contains(item1));
 		productGroup1.add(item1);
 		assertTrue(productGroup1.contains(item1));
 		productGroup1.add(item2);
 		productGroup1.add(item1Copy);
 		assertEquals(2,productGroup1.getItemsSize());
 		
 		productGroup1.remove(item1, itemManager);
 		assertEquals(1,productGroup1.getItemsSize());
 			
 		System.out.println("done.");
 	}
 
 	@Test
 	public void PGtestProducts() {
 		System.out.print("Testing ProductGroup Product logic...");
 
 		assertEquals(0,productGroup1.getProductsSize());
 		assertFalse(productGroup1.contains(product1));
 		productGroup1.add(product1);
 		assertTrue(productGroup1.contains(product1));
 		productGroup1.add(product2);
 		assertTrue(productGroup1.contains(product2));
 		
 		// Should not allow two top-level Products of the same name
 
 		assertEquals(2,productGroup1.getProductsSize());
 	
 		productGroup1.remove(product1);
 		assertEquals(1,productGroup1.getProductsSize());
 				
 		System.out.println("done.");
 	}
 	
 	@Test (expected= IllegalStateException.class)
 	public void testAddDuplicateProducts() {
 		productGroup1.add(product1);
 		productGroup1.add(product1Copy);
 	}
 	
 	@Test
 	public void PGtestProductGroups() {
 		System.out.print("Testing ProductGroup ProductGroup logic...");
 		assertEquals(0, productGroup1.getPGroupsSize());
 		assertTrue(productGroup1.canAddProductGroup(productGroup2));
 		productGroup1.add(productGroup2);
 		assertTrue(productGroup1.contains(productGroup2));
 		assertEquals(1, productGroup1.getPGroupsSize());
 		assertFalse(productGroup1.contains(productGroup3));
 		productGroup1.add(productGroup3);
 		assertTrue(productGroup1.contains(productGroup3));
 		assertEquals(2, productGroup1.getPGroupsSize());
 		// Don't allow duplicate PGs in a PG
 		assertFalse(productGroup1.canAddProductGroup(productGroup2));
 		
 		System.out.println("done.");
 	}
 	
 	@Test (expected= IllegalStateException.class)
 	public void testAddTwoIdenticalProductGroups() {
 		productGroup1.add(productGroup2);
 		productGroup1.add(productGroup2);
 	}
 	
 	@Test
 	public void SUtestItems() {
 		System.out.print("Testing StorageUnit Item logic...");
 		System.out.println("done.");
 	}
 	
 	@Test
 	public void SUtestProducts() {
 		System.out.print("Testing StorageUnit Product logic...");
 		System.out.println("done.");
 	}
 	
 	@Test
 	public void SUtestProductGroups() {
 		System.out.print("Testing StorageUnit ProductGroup logic...");
 		System.out.println("done.");
 	}
 	
 	@Test
 	public void testProductContainer() {
 		fail("Not yet implemented");
 	}
 	@Test
 	public void testGetName() {
 		assertTrue(productGroup1.getName().equals("Cookies"));
 		assertTrue(storageUnit1.getName().equals("Cookie Jar"));
 	}
 /*
 	@Test
 	public void testGetItems() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetProduct() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetCurrentSupplyProduct() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testGetCurrentSupply() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testAddItem() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testAddProduct() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testAddProductGroup() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testCanAddProductGroup() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testRemoveItemItemManager() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testMoveIntoContainer() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testContains() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testRemoveProductContainer() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testRemoveProduct() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testCanRemove() {
 		fail("Not yet implemented");
 	}
 
 
 	@Test
 	public void testClearAllItems() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testClearAllProducts() {
 		fail("Not yet implemented");
 	}
 	
 	@Test
 	public void testIsValidName() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testEqualsObject() {
 		fail("Not yet implemented");
 	}
 
 	@Test
 	public void testCompareTo() {
 		fail("Not yet implemented");
 	}
 	*/
 }
 
