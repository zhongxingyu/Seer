 package test.model.report;
 
 import generators.ItemGenerator;
 import generators.ProductGenerator;
 
 import java.text.DecimalFormat;
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.TreeMap;
 import java.util.TreeSet;
 
 import model.ConcreteProductContainerManager;
 import model.HomeInventoryTracker;
 import model.Item;
 import model.ItemManager;
 import model.Product;
 import model.ProductContainerManager;
 import model.ProductManager;
 import model.report.ProductStatisticsReport;
 import model.report.builder.ReportBuilder;
 
 import org.easymock.EasyMockSupport;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import builder.model.ItemBuilder;
 import builder.model.ProductBuilder;
 
 public class ProductStatisticsReportTest extends EasyMockSupport {
 	private static List<String> productsHeaders() {
 		return Arrays.asList("Description", "Barcode", "Size", "3-Month Supply",
 				"Supply: Cur/Avg", "Supply: Min/Max", "Supply Used/Added", "Shelf Life",
 				"Used Age: Avg/Max", "Cur Age: Avg/Max");
 	}
 
 	private final long millisPerDay = 24 * 3600 * 1000;
 	private ItemManager itemManager;
 
 	private ProductManager productManager;
 
 	private HomeInventoryTracker hit;
 	private ProductStatisticsReport report;
 	private ReportBuilder mockBuilder;
 	private ItemGenerator itemGenerator;
 	private ProductGenerator productGenerator;
 
 	@Before
 	public void setUp() throws Exception {
 		hit = new HomeInventoryTracker();
 		itemManager = hit.getItemManager();
 		productManager = hit.getProductManager();
 		report = new ProductStatisticsReport(itemManager, productManager);
 		mockBuilder = createMock(ReportBuilder.class);
 		itemGenerator = new ItemGenerator(itemManager);
 		productGenerator = new ProductGenerator(productManager);
 	}
 
 	@After
 	public void tearDown() throws Exception {
 	}
 
 	/*
 	 * * Creates Items at random dates within the time frame of the report (must be included)
 	 */
 
 	@Test
 	public void testEmptyReport() {
 		ProductStatisticsReport report = new ProductStatisticsReport(itemManager,
 				productManager);
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (3 Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		replayAll();
 
 		report.construct(mockBuilder, 3);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testGetAverageSupply() {
 		Date now = new Date();
 		int months = 3;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		// Test a product added just now with an item added at the same time
 		Product product = new ProductBuilder().creationDate(new Date(now.getTime() - 50000))
 				.productManager(productManager).build();
 		Item item = new ItemBuilder().entryDate(new Date(now.getTime() - 50000))
 				.manager(itemManager).build();
 
 		mockBuilder.addTableRow(asTableRow(product, months, 0 /* avgSupply */,
 				0 /* minSupply */, 0 /* maxSupply */, 0 /* usedSupply */, 0 /* addedSupply */,
 				0 /* avgUsedAge */, 0 /* maxUsedAge */, 0 /* curAvgAge */, 0 /* curMaxAge */));
 
 		// Test a product added two days ago with an item added at the same time
 		Product product2 = new ProductBuilder()
 				.creationDate(new Date(now.getTime() - 2 * millisPerDay))
 				.productManager(productManager).build();
 		Item item2 = new ItemBuilder().entryDate(product2.getCreationDate())
 				.manager(itemManager).build();
 
 		mockBuilder.addTableRow(asTableRow(product2, months, 1 /* avgSupply */,
 				1 /* minSupply */, 1 /* maxSupply */, 0 /* usedSupply */, 1 /* addedSupply */,
 				0 /* avgUsedAge */, 0 /* maxUsedAge */, 2 /* curAvgAge */, 2 /* curMaxAge */));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	@SuppressWarnings("deprecation")
 	@Test
 	public void testOneProductOneItem() {
 		System.out.println("One product one item");
 		ProductContainerManager productContainerManager = new ConcreteProductContainerManager();
 		ProductStatisticsReport report = new ProductStatisticsReport(itemManager,
 				productManager);
 		int months = 3;
 		Product product = productGenerator.createProductInDateRange(0, months);
 		Item item = itemGenerator.createItemAtRandomTime(product);
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (3 Months)");
 
 		mockBuilder.startTable(productsHeaders());
 		// d,b,pq,nms, curS/avS, minS/maxS, used/added, sl, usedAge:av/max, curAge:av/max
 		mockBuilder.addTableRow(asTableRow(product, months));
 		replayAll();
 
 		report.construct(mockBuilder, 3);
 
 		verifyAll();
 	}
 
 	@SuppressWarnings("deprecation")
 	@Test
 	public void testOneProductZeroItems() {
 		ProductContainerManager productContainerManager = new ConcreteProductContainerManager();
 		ProductStatisticsReport report = new ProductStatisticsReport(itemManager,
 				productManager);
 		int months = 3;
 		Product product = productGenerator.createProductInDateRange(0, months);
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (3 Months)");
 
 		mockBuilder.startTable(productsHeaders());
 		// d,b,pq,nms, curS/avS, minS/maxS, used/added, sl, usedAge:av/max, curAge:av/max
 		mockBuilder.addTableRow(asTableRow(product, months));
 		replayAll();
 
 		report.construct(mockBuilder, 3);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testPreviouslyRemovedItemsNotIncluded() {
 		ProductGenerator productGenerator = new ProductGenerator(productManager);
 		int months = 3;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 		Set<Product> productsInPeriod = new TreeSet<Product>();
 		for (int i = 0; i < 1000; i++) {
 			productsInPeriod.add(productGenerator.createProductInDateRange(0, months));
 		}
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		for (Product product : productsInPeriod)
 			mockBuilder.addTableRow(asTableRow(product, months));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testProductsIncluded100Months() {
 		ProductGenerator productGenerator = new ProductGenerator(productManager);
 		int months = 100;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 		Set<Product> productsInPeriod = new TreeSet<Product>();
 		for (int i = 0; i < 1; i++) {
 			Product product = productGenerator.createProductInDateRange(0, months);
 			productsInPeriod.add(product);
 			int numberOfItems = (int) (Math.random() * 100);
 			for (int j = 0; j < numberOfItems; j++)
 				itemGenerator.createItemAtRandomTime(product);
 		}
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		for (Product product : productsInPeriod)
 			mockBuilder.addTableRow(asTableRow(product, months));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testProductsIncluded12Months() {
 		ProductGenerator productGenerator = new ProductGenerator(productManager);
 		int months = 12;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 		Set<Product> productsInPeriod = new TreeSet<Product>();
 		for (int i = 0; i < 100; i++) {
 			productsInPeriod.add(productGenerator.createProductInDateRange(0, months));
 		}
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		for (Product product : productsInPeriod)
 			mockBuilder.addTableRow(asTableRow(product, months));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testProductsIncluded1Month() {
 		ProductGenerator productGenerator = new ProductGenerator(productManager);
 		int months = 1;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 		Set<Product> productsInPeriod = new TreeSet<Product>();
 		for (int i = 0; i < 100; i++) {
 			productsInPeriod.add(productGenerator.createProductInDateRange(0, months));
 		}
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		for (Product product : productsInPeriod)
 			mockBuilder.addTableRow(asTableRow(product, months));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	@Test
 	public void testProductsIncluded99Months() {
 		ProductGenerator productGenerator = new ProductGenerator(productManager);
 		int months = 99;
 		ReportBuilder mockBuilder = createMock(ReportBuilder.class);
 		Set<Product> productsInPeriod = new TreeSet<Product>();
 		for (int i = 0; i < 100; i++) {
 			productsInPeriod.add(productGenerator.createProductInDateRange(0, months));
 		}
 
 		// Expect:
 		mockBuilder.addDocumentTitle("Product Report (" + months + " Months)");
 
 		mockBuilder.startTable(productsHeaders());
 
 		for (Product product : productsInPeriod)
 			mockBuilder.addTableRow(asTableRow(product, months));
 
 		replayAll();
 
 		report.construct(mockBuilder, months);
 
 		verifyAll();
 	}
 
 	/*
 	 * Creates some additional Items beyond the report's time frame and removes them before the
 	 * report period begins; checks that they are not included in the report
 	 * 
 	 * Creates Items beyond the report's time frame and does not remove them, checks that they
 	 * are included in the report
 	 * 
 	 * Chooses some subset of the included items to remove at random times during the reporting
 	 * period and checks that the report counts these items as removed within the specified
 	 * period
 	 * 
 	 * Check that current supply level is correct
 	 * 
 	 * Check that average supply level is correct
 	 * 
 	 * Check that minimum supply level is correct
 	 * 
 	 * Check that maximum supply level is correct
 	 * 
 	 * Check that used supply is correct
 	 * 
 	 * Check that added supply is correct
 	 * 
 	 * Check that average used age is correct
 	 * 
 	 * Check that maximum used age is correct
 	 * 
 	 * Check that average current age is correct
 	 * 
 	 * Check that maximum current age is correct
 	 */
 
 	@SuppressWarnings("deprecation")
 	private List<String> asTableRow(Product product, int months) {
 		Date now = new Date();
 		Date startDate = new Date(now.getTime());
 		int currentYear = startDate.getYear();
 		int currentMonth = startDate.getMonth();
 		for (int i = 0; i < months; i++) {
 			currentMonth--;
 			if (currentMonth < 0) {
 				currentMonth = 11;
 				currentYear--;
 			}
 		}
 		startDate.setMonth(currentMonth);
		startDate.setMonth(currentYear);
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
 		return Arrays.asList(
 				product.getDescription(),
 				product.getBarcode(),
 				product.getProductQuantity().toString(),
 				Integer.toString(product.getThreeMonthSupply()),
 				Integer.toString(product.getCurrentSupply()) + "/"
						+ twoDForm.format(getAverageSupply(product, now)),
 				Integer.toString(getMinSupply(product, startDate)) + "/"
 						+ Integer.toString(getMaxSupply(product, startDate)),
 				"" + getUsedItemCount(product, startDate) + "/"
 						+ getAddedItemCount(product, startDate),
 				"" + product.getShelfLife() + " months",
 				twoDForm.format(averageUsedAge(product, startDate)) + "/"
 						+ twoDForm.format(maxUsedAge(product, startDate)),
 				twoDForm.format(averageCurrentAge(product)) + "/"
 						+ twoDForm.format(maxCurrentAge(product)));
 	}
 
 	/**
 	 * Returns a List of String that represents how this Product should appear in the
 	 * ProductStatistics table.
 	 * 
 	 * @param product
 	 *            Product to represent as row in the ProductStatistics table
 	 * @return a List of String that represents how this Product should appear in the
 	 *         ProductStatistics table
 	 */
 	private List<String> asTableRow(Product product, int months, double avgSupply,
 			int minSupply, int maxSupply, int usedSupply, int addedSupply, double avgUsedAge,
 			double maxUsedAge, double curAvgAge, double curMaxAge) {
 		DecimalFormat twoDForm = new DecimalFormat("#.##");
 		return Arrays.asList(product.getDescription(), product.getBarcode(), product
 				.getProductQuantity().toString(), "" + product.getThreeMonthSupply(), ""
 				+ product.getCurrentSupply() + "/" + twoDForm.format(avgSupply), ""
 				+ minSupply + "/" + "" + maxSupply, "" + usedSupply + "/" + addedSupply, ""
 				+ product.getShelfLife() + " months", twoDForm.format(avgUsedAge) + "/"
 				+ twoDForm.format(maxUsedAge),
 				twoDForm.format(curAvgAge) + "/" + twoDForm.format(curMaxAge));
 	}
 
 	private double averageCurrentAge(Product product) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		double totalDays = 0;
 		int totalItems = items.size();
 
 		if (totalItems == 0)
 			return 0;
 
 		Date today = new Date();
 		for (Item item : items) {
 			long diff = today.getTime() - item.getEntryDate().getTime();
 			totalDays += (diff / millisPerDay) + 1;
 		}
 		return totalDays / totalItems;
 	}
 
 	private double averageUsedAge(Product product, Date startPeriod) {
 		double daysStored = 0;
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
 		if (usedItems != null && usedItems.size() > 0) {
 			for (Item usedItem : usedItems) {
 				if (usedItem.getExitTime().after(startPeriod)) {
 					long timeStored = usedItem.getExitTime().getTime()
 							- usedItem.getEntryDate().getTime();
 					daysStored += (timeStored / millisPerDay) + 1;
 				}
 			}
 		} else {
 			return 0;
 		}
 
 		return (daysStored / getUsedItemCount(product, startPeriod));
 	}
 
 	@SuppressWarnings("deprecation")
 	private int getAddedItemCount(Product product, Date startDate) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		int count = 0;
 
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getEntryDate().after(startDate)
 						|| item.getEntryDate().getDate() == startDate.getDate())
 					count++;
 			}
 		}
 
 		for (Item item : items) {
 			if (item.getEntryDate().after(startDate)
 					|| item.getEntryDate().getDate() == startDate.getDate())
 				count++;
 		}
 		return count;
 	}
 
 	private double getAverageSupply(Product product, Date startDate) {
 		// yesterday's count
 		int current = getInitialCount(product, startDate);
 		double count = 0;
 
 		Date lastChange = startDate;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			int change = history.get(d);
 			long days = getDaysDifference(lastChange, d);
 			count += (days * current);
 			current += change;
 			lastChange = d;
 		}
 
 		Date today = new Date();
 		if (lastChange.before(today)) {
 			long days = getDaysDifference(lastChange, today) + 1; // for end of day
 			count += (days * current);
 		}
 
 		double averageSupply = count / (getDaysDifference(startDate, today) + 1);
 		if (averageSupply > 0) {
 			System.out.println("Average Supply Test: " + averageSupply + " "
 					+ startDate.toString());
 		}
 		return averageSupply;
 	}
 
 	private int getCurrentSupply(Product product) {
 		return itemManager.getItemsByProduct(product).size();
 	}
 
 	private long getDaysDifference(Date first, Date second) {
 		if (first.equals(second))
 			return 1;
 		if (first.after(second))
 			throw new IllegalArgumentException("Second date must be after first date");
 		long timeDiff = second.getTime() - first.getTime();
 		return (timeDiff / millisPerDay);
 	}
 
 	private Map<Date, Integer> getHistory(Product product, Date startDate) {
 		Map<Date, Integer> dateItemMap = new TreeMap<Date, Integer>();
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		// build sorted list of history
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getExitTime().after(startDate)) { // if item was removed this period
 					if (dateItemMap.containsKey(item.getExitTime())) {
 						int oldCount = dateItemMap.get(item.getExitTime());
 						dateItemMap.put(item.getExitTime(), --oldCount);
 					} else {
 						dateItemMap.put(item.getExitTime(), -1);
 					}
 				}
 				if (item.getEntryDate().after(startDate)) {
 					if (dateItemMap.containsKey(item.getEntryDate())) {
 						int oldCount = dateItemMap.get(item.getEntryDate());
 						dateItemMap.put(item.getEntryDate(), ++oldCount);
 					} else {
 						dateItemMap.put(item.getEntryDate(), 1);
 					}
 				}
 			}
 		}
 
 		for (Item item : itemManager.getItemsByProduct(product)) {
 			if (!item.getEntryDate().before(startDate)) {
 				if (dateItemMap.containsKey(item.getEntryDate())) {
 					int oldCount = dateItemMap.get(item.getEntryDate());
 					dateItemMap.put(item.getEntryDate(), ++oldCount);
 				} else {
 					dateItemMap.put(item.getEntryDate(), 1);
 				}
 			}
 		}
 
 		return dateItemMap;
 	}
 
 	/**
 	 * get yesterday's total
 	 * 
 	 * @param product
 	 * @param startPeriod
 	 * @return
 	 */
 	private int getInitialCount(Product product, Date startPeriod) {
 		Date today = new Date();
 		today.setDate(today.getDate() - 1);
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		Set<Item> removed = itemManager.getRemovedItemsByProduct().get(product);
 		int currentCount = items.size();
 		int removedDuringPeriod = 0;
 		int addedDuringPeriod = 0;
 
 		if (removed != null) {
 			for (Item item : removed) {
 				if (item.getExitTime().after(startPeriod))
 					removedDuringPeriod++;
 				if (item.getEntryDate().after(startPeriod))
 					addedDuringPeriod++;
 			}
 		}
 
 		if (items != null) {
 			for (Item item : items) {
 				if (item.getEntryDate().after(startPeriod))
 					addedDuringPeriod++;
 			}
 		}
 		return currentCount - addedDuringPeriod + removedDuringPeriod;
 	}
 
 	private int getMaxSupply(Product product, Date startDate) {
 		int current = getInitialCount(product, startDate);
 		int max = current;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			current += history.get(d);
 			max = Math.max(current, max);
 		}
 
 		return max;
 	}
 
 	private int getMinSupply(Product product, Date startDate) {
 		int current = getInitialCount(product, startDate);
 		int min = current;
 		Map<Date, Integer> history = getHistory(product, startDate);
 		Collection<Date> dates = history.keySet();
 
 		// recreate the usage
 		for (Date d : dates) {
 			current += history.get(d);
 			min = Math.min(current, min);
 		}
 
 		return min;
 	}
 
 	private int getUsedItemCount(Product product, Date startDate) {
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
 		int count = 0;
 		if (usedItems != null) {
 			for (Item item : usedItems) {
 				if (item.getExitTime().after(startDate))
 					count++;
 			}
 		}
 		return count;
 	}
 
 	private int maxCurrentAge(Product product) {
 		Set<Item> items = itemManager.getItemsByProduct(product);
 		int maxDays = 0;
 		Date now = new Date();
 		for (Item item : items) {
 			long difference = (now.getTime() - item.getEntryDate().getTime());
 			int daysInStorage = (int) (difference / millisPerDay) + 1;
 			if (daysInStorage > maxDays)
 				maxDays = daysInStorage;
 		}
 		return maxDays;
 	}
 
 	private long maxUsedAge(Product product, Date startPeriod) {
 		long maxDaysStored = 0;
 		Set<Item> usedItems = itemManager.getRemovedItemsByProduct().get(product);
 		if (usedItems != null) {
 			for (Item usedItem : usedItems) {
 				if (usedItem.getExitTime().after(startPeriod)) {
 					long timeStored = usedItem.getExitTime().getTime()
 							- usedItem.getEntryDate().getTime();
 					long daysStored = (timeStored / millisPerDay) + 1;
 					maxDaysStored = Math.max(maxDaysStored, daysStored);
 				}
 			}
 		} else {
 			return 0;
 		}
 
 		return maxDaysStored;
 	}
 
 }
