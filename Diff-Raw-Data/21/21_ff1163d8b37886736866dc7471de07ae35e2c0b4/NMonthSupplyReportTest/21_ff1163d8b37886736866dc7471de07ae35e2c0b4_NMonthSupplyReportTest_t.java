 package test.model.report;
 
 import static org.junit.Assert.assertTrue;
 
 import java.util.Arrays;
 import java.util.Date;
 import java.util.List;
 
 import model.ConcreteProductContainerManager;
 import model.ConcreteProductManager;
 import model.HomeInventoryTracker;
 import model.Product;
 import model.StorageUnit;
 import model.report.NMonthSupplyReport;
 import model.report.builder.ReportBuilder;
 
 import org.easymock.EasyMockSupport;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import builder.model.ItemBuilder;
 import builder.model.ProductBuilder;
 import builder.model.StorageUnitBuilder;
 
 public class NMonthSupplyReportTest extends EasyMockSupport {
 	private static List<String> productGroupsHeaders(int months) {
 		return Arrays.asList("Product Group", "Storage Unit", Integer.toString(months)
 				+ "-Month Supply", "Current Supply");
 	}
 
 	private static List<String> productsHeaders(int months) {
 		return Arrays.asList("Description", "Barcode", Integer.toString(months)
 				+ "-Month Supply", "Current Supply");
 	}
 
 	private HomeInventoryTracker hit;
 	private NMonthSupplyReport report;
 	private ReportBuilder mockBuilder;
 
 	@Before
 	public void setUp() throws Exception {
 		hit = new HomeInventoryTracker();
 		report = new NMonthSupplyReport(hit.getProductManager(),
 				hit.getProductContainerManager());
 		mockBuilder = createMock(ReportBuilder.class);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	@Test
 	public void testOnEmptyTreeFor3Months() {
 		NMonthSupplyReport report = new NMonthSupplyReport(new ConcreteProductManager(),
 				new ConcreteProductContainerManager());
 
 		// Expect:
 		mockBuilder.addDocumentTitle("3-Month Supply Report");
 
 		mockBuilder.addSectionTitle("Products");
 		mockBuilder.startTable(productsHeaders(3));
 
 		mockBuilder.addSectionTitle("Product Groups");
 		mockBuilder.startTable(productGroupsHeaders(3));
 
 		replayAll();
 
 		report.construct(mockBuilder, 3);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testOnEmptyTreeFor4Months() {
 		NMonthSupplyReport report = new NMonthSupplyReport(new ConcreteProductManager(),
 				new ConcreteProductContainerManager());
 
 		// Expect:
 		mockBuilder.addDocumentTitle("4-Month Supply Report");
 
 		mockBuilder.addSectionTitle("Products");
 		mockBuilder.startTable(productsHeaders(4));
 
 		mockBuilder.addSectionTitle("Product Groups");
 		mockBuilder.startTable(productGroupsHeaders(4));
 
 		replayAll();
 
 		report.construct(mockBuilder, 4);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testOnFilledTreeFor3Months() {
 		StorageUnit storageUnit = new StorageUnitBuilder().manager(
 				hit.getProductContainerManager()).build();
 		Product product2 = new ProductBuilder().productManager(hit.getProductManager())
 				.description("2").build();
 		Product product1 = new ProductBuilder().productManager(hit.getProductManager())
 				.description("1").build();
 		new ItemBuilder().product(product2).container(storageUnit).build();
 		new ItemBuilder().product(product2).container(storageUnit).build();
 		new ItemBuilder().product(product1).container(storageUnit).build();
 		new ItemBuilder().product(product1).container(storageUnit).build();
 
 		// Expect:
 		mockBuilder.addDocumentTitle("3-Month Supply Report");
 
 		mockBuilder.addSectionTitle("Products");
 		mockBuilder.startTable(productsHeaders(3));
 		mockBuilder.addTableRow(asTableRow(product1));
 		mockBuilder.addTableRow(asTableRow(product2));
 
 		mockBuilder.addSectionTitle("Product Groups");
 		mockBuilder.startTable(productGroupsHeaders(3));
 
 		replayAll();
 
 		report.construct(mockBuilder, 3);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testRunTime() {
 		report.construct(mockBuilder, 3);
 		Date firstRunTime = report.getLastRunTime();
 
 		try {
 			Thread.sleep(1000);
 		} catch (InterruptedException e) {
 			e.printStackTrace();
 		}
 
 		report.construct(mockBuilder, 3);
 		assertTrue(firstRunTime.before(report.getLastRunTime()));
 	}
 
 	/**
 	 * Returns a List of String that represents how this item should appear in the NMonthSupply
 	 * table.
 	 * 
 	 * @param item
 	 *            Item to represent as row in the NMonthSupply table
 	 * @return a List of String that represents how this item should appear in the NMonthSupply
 	 *         table
 	 */
 	private List<String> asTableRow(Product product) {
		return Arrays.asList(product.getDescription(), product.getBarcode(), product
				.getThreeMonthSupplyAsProductQuantity().toString(), product.getCurrentSupply()
 				.toString());
 	}
 }
