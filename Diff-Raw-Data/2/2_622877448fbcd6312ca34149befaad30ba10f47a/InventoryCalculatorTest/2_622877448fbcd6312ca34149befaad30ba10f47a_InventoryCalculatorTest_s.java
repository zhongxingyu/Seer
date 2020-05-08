 package controllers.inventory;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import models.BaseProduct;
 import models.Coop;
 import models.Sale;
 
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import play.modules.siena.SienaFixtures;
 import play.test.UnitTest;
 
 public class InventoryCalculatorTest extends UnitTest {
 
 	@Before
 	public void setUp() {
//		SienaFixtures.deleteDatabase();
 		SienaFixtures.loadModels("test-data.yml");
 	}
 
 	@After
 	public void tearDown() {
 		SienaFixtures.deleteDatabase();
 	}
 
 	@Test
 	public void testCalculate() {
 		Coop coop = Coop.all().filter("title", "first").get();
 		List<Sale> sales = coop.sales.order("date").fetch();
 		assertEquals(3, sales.size());
 		InventoryResult inventoryResult = new InventoryCalculator(sales)
 				.calculate();
 		List<BaseProduct> bp = new ArrayList<BaseProduct>(inventoryResult.getBaseProducts());
 		assertEquals("[lentils, bread, butter]", bp.toString());
 		assertEquals("{}", inventoryResult.getInventory().get(sales.get(0)).toString());
 		Map<BaseProduct, Inventory> firstInv = inventoryResult.getInventory().get(sales.get(1));
 		assertEquals("Inventory{current=0.0, purchases=20.0, orders=4.0}", firstInv.get(bp.get(0)).toString());
 		assertEquals("Inventory{current=0.0, purchases=10.0, orders=5.0}", firstInv.get(bp.get(1)).toString());
 		Map<BaseProduct, Inventory> secondInv = inventoryResult.getInventory().get(sales.get(2));
 		assertEquals("Inventory{current=0.0, purchases=5.0, orders=4.5}", secondInv.get(bp.get(0)).toString());
 		assertEquals("Inventory{current=0.0, purchases=8.0, orders=10.0}", secondInv.get(bp.get(2)).toString());
 	}
 }
