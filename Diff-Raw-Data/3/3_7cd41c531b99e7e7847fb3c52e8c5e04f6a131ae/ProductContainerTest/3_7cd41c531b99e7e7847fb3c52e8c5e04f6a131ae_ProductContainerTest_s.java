 package model;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import mocks.MockItemManager;
 import mocks.MockProductContainerManager;
 import mocks.MockProductManager;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 public class ProductContainerTest {
 	private ProductContainerManager pcManager;
 	private ItemManager itemManager;
 	private ProductManager productManager;
 	private StorageUnit storageUnit1;
 	private StorageUnit storageUnit2;
 	private ProductGroup productGroup1;
 	private ProductGroup productGroup2;
 	private ProductGroup productGroup3;
 	private Product product1;
 	private Product product2;
 	private Item item1;
 	private Item item1Copy;
 
 	@Before
 	public void setUp() throws Exception {
 		pcManager = new MockProductContainerManager();
 		itemManager = new MockItemManager();
 		productManager = new MockProductManager();
 		storageUnit1 = new StorageUnit("Cookie Jar", pcManager);
 		storageUnit2 = new StorageUnit("Playdough Bin", pcManager);
 		productGroup1 = new ProductGroup("Cookies", new ProductQuantity(1, Unit.COUNT),
 				Unit.KILOGRAMS, storageUnit1, pcManager);
 		productGroup2 = new ProductGroup("Chocolate Chip Cookies", new ProductQuantity(1,
 				Unit.COUNT), Unit.KILOGRAMS, storageUnit1, pcManager);
 		productGroup3 = new ProductGroup("No-Bake Cookies",
 				new ProductQuantity(1, Unit.COUNT), Unit.KILOGRAMS, storageUnit1, pcManager);
 		product1 = new Product("PROD1BARCODE", "Smelly socks", 0, 4, new ProductQuantity(1,
 				Unit.COUNT), productManager);
 		product2 = new Product("PROD2BARCODE00", "Green Jell-O", 365, 12, new ProductQuantity(
 				3.5f, Unit.OUNCES), productManager);
 		item1 = new Item(new Barcode("400000001968"), product1, storageUnit1, itemManager);
 		item1Copy = new Item(new Barcode("400000001968"), product1, storageUnit1, itemManager);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 		// Test the invariants!
 		assertFalse(productGroup1.getName() == null);
 		assertFalse(productGroup1.getName().equals(""));
 		assertTrue(productGroup1.getItemsSize() >= 0);
 		assertTrue(productGroup1.getProductsSize() >= 0);
 		assertTrue(productGroup1.getProductGroupsSize() >= 0);
 		assertFalse(storageUnit1.getName() == null);
 		assertFalse(storageUnit1.getName().equals(""));
 		assertTrue(storageUnit1.getItemsSize() >= 0);
 		assertTrue(storageUnit1.getProductsSize() >= 0);
 		assertTrue(storageUnit1.getProductGroupsSize() >= 0);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testAddDuplicateItems() {
 		storageUnit1.add(item1);
 		storageUnit1.add(item1Copy);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testAddDuplicateItems2() {
 		storageUnit1.add(item1);
 		productGroup1.add(productGroup2);
 		productGroup2.add(product1);
 		storageUnit1.add(productGroup2);
 		storageUnit1.add(item1Copy);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testAddDuplicateProductGroups() {
 		productGroup1.add(productGroup2);
 		productGroup1.add(productGroup2);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testAddDuplicateProducts() {
 		productGroup1.add(product1);
 
 		Product product1Copy = new Product("PROD1BARCODE", "Pair of Smelly socks", 0, 4,
 				new ProductQuantity(1, Unit.COUNT), productManager);
 
 		productGroup1.add(product1Copy);
 	}
 
 	@Test
 	public void testAddItem() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(productGroup2);
 		productGroup2.add(product1);
 		assertTrue(storageUnit1.add(item1));
 		assertTrue(productGroup2.contains(item1));
 	}
 
 	@Test
 	public void testAddItem2() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(productGroup2);
 		assertTrue(storageUnit1.add(item1));
 		assertFalse(productGroup2.contains(item1));
 		assertFalse(productGroup1.contains(item1));
 		assertTrue(storageUnit1.contains(item1));
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testAddTwoIdenticalProductGroups() {
 		productGroup1.add(productGroup2);
 		productGroup1.add(productGroup2);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testCannotRemoveProductGroup() {
 		storageUnit1.add(product1);
 		productGroup1.add(productGroup2);
 		storageUnit1.add(item1);
 		productGroup1.remove(productGroup2);
 	}
 
 	@Test
 	public void testCanRemove() {
 		assertTrue(productGroup1.canRemove());
 		assertTrue(storageUnit1.canRemove());
 		productGroup1.add(productGroup2);
 		productGroup2.add(product1);
		storageUnit1.add(item1);
 		storageUnit1.add(productGroup1);
 		assertFalse(productGroup1.canRemove());
 		assertFalse(storageUnit1.canRemove());
 		productGroup2.remove(item1, itemManager);
 		assertTrue(productGroup1.canRemove());
 		assertTrue(storageUnit1.canRemove());
 	}
 
 	@Test
 	public void testCanRemoveProduct() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(product1);
 		assertTrue(productGroup1.canRemove(product1));
 		storageUnit1.add(item1);
 		assertFalse(productGroup1.canRemove(product1));
 		productGroup1.remove(item1, itemManager);
 		assertTrue(productGroup1.canRemove(product1));
 	}
 
 	@Test
 	public void testGetCurrentSupply() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(productGroup2);
 		productGroup2.add(product1);
 		storageUnit1.add(item1);
 		storageUnit1.add(new Item(new Barcode("400000001920"), product1, null, itemManager));
 		System.out.println("Supply: " + storageUnit1.getCurrentSupply(item1.getProduct()));
 		assertTrue(storageUnit1.getCurrentSupply(item1.getProduct()).equals(
 				new ProductQuantity(4, Unit.COUNT)));
 		assertTrue(productGroup1.getCurrentSupply(item1.getProduct()).equals(
 				new ProductQuantity(2, Unit.COUNT)));
 		Item item2 = new Item(new Barcode("400000001999"), product2, null, itemManager);
 		assertTrue(storageUnit1.getCurrentSupply(item2.getProduct()).equals(
 				new ProductQuantity(0, Unit.OUNCES)));
 	}
 
 	@Test
 	public void testGetName() {
 		assertTrue(productGroup1.getName().equals("Cookies"));
 		assertTrue(storageUnit1.getName().equals("Cookie Jar"));
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalProductAdd() {
 		storageUnit1.add(productGroup1);
 		assertTrue(storageUnit1.canAddProduct(product1.getBarcode()));
 		productGroup1.add(product1);
 		productGroup1.add(productGroup2);
 		assertFalse(productGroup2.canAddProduct(product1.getBarcode()));
 		productGroup2.add(product1);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalProductAdd2() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(productGroup2);
 		assertTrue(storageUnit1.canAddProduct(product1.getBarcode()));
 		productGroup2.add(product1);
 		assertFalse(storageUnit1.canAddProduct(product1.getBarcode()));
 		storageUnit1.add(product1);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalProductAdd3() {
 		storageUnit1.add(productGroup1);
 		assertTrue(storageUnit1.canAddProduct(product1.getBarcode()));
 		storageUnit1.add(product1);
 		productGroup1.add(productGroup2);
 		assertFalse(productGroup2.canAddProduct(product1.getBarcode()));
 		productGroup2.add(product1);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testIllegalRemoveProduct() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(product1);
 		storageUnit1.add(item1);
 		productGroup1.remove(product1);
 	}
 
 	@Test
 	public void testInvariants() {
 		assertFalse(productGroup1.getName() == null);
 		assertFalse(productGroup1.getName().equals(""));
 		assertTrue(productGroup1.getItemsSize() >= 0);
 		assertTrue(productGroup1.getProductsSize() >= 0);
 		assertTrue(productGroup1.getProductGroupsSize() >= 0);
 	}
 
 	@Test
 	public void testMoveIntoContainer() {
 		storageUnit1.add(productGroup1);
 		productGroup1.add(product1);
 		storageUnit1.add(item1);
 		productGroup1.moveIntoContainer(item1, productGroup2);
 		assertTrue(productGroup2.contains(item1));
 		assertFalse(productGroup1.contains(item1));
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testMoveIntoContainerDuplicate() {
 		storageUnit1.add(item1);
 		storageUnit2.add(item1);
 		storageUnit1.moveIntoContainer(item1, storageUnit2);
 	}
 
 	@Test(expected = IllegalStateException.class)
 	public void testMoveIntoContainerFromEmpty() {
 		productGroup1.moveIntoContainer(item1, productGroup2);
 	}
 
 	@Test
 	public void testProductGroupItems() {
 		System.out.print("Testing ProductGroup Item logic...");
 		storageUnit1.add(productGroup1);
 		productGroup1.add(item1.getProduct());
 		assertEquals(0, productGroup1.getItemsSize());
 		assertFalse(productGroup1.contains(item1));
 
 		System.out.println("done.");
 	}
 
 	@Test
 	public void testProductGroupProductGroups() {
 		System.out.print("Testing ProductGroup ProductGroup logic...");
 		assertEquals(0, productGroup1.getProductGroupsSize());
 		assertTrue(productGroup1.canAddProductGroup(productGroup2));
 		productGroup1.add(productGroup2);
 		assertFalse(productGroup1.canAddProductGroup(productGroup2));
 		assertTrue(productGroup1.contains(productGroup2));
 		assertEquals(1, productGroup1.getProductGroupsSize());
 		assertFalse(productGroup1.contains(productGroup3));
 		productGroup1.add(productGroup3);
 		assertTrue(productGroup1.contains(productGroup3));
 		assertTrue(productGroup1.containsProductGroup(productGroup3.getName()));
 		assertFalse(productGroup1.canAddProductGroup(productGroup3.getName()));
 		assertEquals(2, productGroup1.getProductGroupsSize());
 		// Don't allow duplicate PGs in a PG
 		assertFalse(productGroup1.canAddProductGroup(productGroup2));
 		assertTrue(productGroup1.getProductGroup(productGroup3.getName())
 				.equals(productGroup3));
 		assertTrue(productGroup1.getProductGroup(productGroup2.getName())
 				.equals(productGroup2));
 		assertTrue(productGroup1.getProductGroup(productGroup1.getName()) == null);
 		System.out.println("done.");
 	}
 
 	@Test
 	public void testProductGroupProducts() {
 		System.out.print("Testing ProductGroup Product logic...");
 
 		assertEquals(0, productGroup1.getProductsSize());
 		assertFalse(productGroup1.contains(product1));
 		productGroup1.add(product1);
 		assertTrue(productGroup1.contains(product1));
 		productGroup1.add(product2);
 		assertTrue(productGroup1.contains(product2));
 		assertTrue(productGroup1.containsProduct(product2.getBarcode()));
 		// Should not allow two top-level Products of the same name
 		assertEquals(2, productGroup1.getProductsSize());
 
 		productGroup1.remove(product1);
 		assertEquals(1, productGroup1.getProductsSize());
 		assertTrue(productGroup1.getProductsIterator().next().equals(product2));
 
 		assertTrue(productGroup1.getProduct(product2.getBarcode()).equals(product2));
 		assertTrue(productGroup1.getProduct(product1.getBarcode()) == null);
 
 		System.out.println("done.");
 	}
 
 	@Test
 	public void testStorageUnitCanAddProduct() {
 		storageUnit1.add(productGroup1);
 		storageUnit2.add(productGroup2);
 		productGroup2.add(productGroup3);
 		productGroup3.add(product1);
 		assertTrue(storageUnit1.canAddProduct(product1.getBarcode()));
 		assertTrue(productGroup1.canAddProduct(product1.getBarcode()));
 		assertFalse(storageUnit2.canAddProduct(product1.getBarcode()));
 		assertFalse(productGroup2.canAddProduct(product1.getBarcode()));
 		assertFalse(productGroup3.canAddProduct(product1.getBarcode()));
 	}
 }
