 package test.strangeforest.currencywatch.integration.db4o;
 
 import java.io.*;
 import java.util.*;
 
 import org.strangeforest.currencywatch.core.*;
 import org.strangeforest.currencywatch.db4o.*;
 import org.testng.annotations.*;
 
 import test.strangeforest.currencywatch.integration.*;
 
 import static org.testng.Assert.*;
 import static test.strangeforest.currencywatch.TestData.*;
 
 public class Db4oCurrencyRateProviderIT {
 
 	private UpdatableCurrencyRateProvider currencyRateProvider;
 
 	private static final String DB4O_DATA_FILE = "data/test-rates-db4o.db4o";
 	private static final String DB4O_DATA_FILE_UPGRADE = "data/upgrade-rates-db4o.db4o";
 
 	@BeforeClass
 	private void setUp() {
 		ITUtil.deleteFile(DB4O_DATA_FILE);
 		ITUtil.deleteFile(DB4O_DATA_FILE_UPGRADE);
 		currencyRateProvider = new Db4oCurrencyRateProvider(DB4O_DATA_FILE);
 		currencyRateProvider.init();
 	}
 
 	@AfterClass
 	private void cleanUp() {
 		currencyRateProvider.close();
 	}
 
 	@Test
 	public void setRate() {
 		currencyRateProvider.setRate(BASE_CURRENCY, CURRENCY, DATE, RATE);
 	}
 
 	@Test(dependsOnMethods = "setRate")
 	public void getRate() {
 		RateValue fetchedRate = currencyRateProvider.getRate(BASE_CURRENCY, CURRENCY, DATE);
 		assertEquals(fetchedRate, RATE);
 	}
 
 	@Test(dependsOnMethods = "getRate")
 	public void setRates() {
 		currencyRateProvider.setRates(BASE_CURRENCY, CURRENCY, RATES);
 	}
 
 	@Test(dependsOnMethods = "setRates")
 	public void getRates() {
 		Map<Date, RateValue> fetchedRates = currencyRateProvider.getRates(BASE_CURRENCY, CURRENCY, DATES);
 		assertEquals(fetchedRates, RATES);
 	}
 
 	@Test(dependsOnMethods = "getRates")
 	public void upgradeData() throws NoSuchFieldException, IllegalAccessException {
		try (CurrencyRateProvider provider = new Db4oCurrencyRateProvider(DB4O_DATA_FILE_UPGRADE)) {
			provider.init();
		}
 		assertTrue(new File(DB4O_DATA_FILE_UPGRADE).exists());
 
		try (CurrencyRateProvider provider = new Db4oCurrencyRateProvider(DB4O_DATA_FILE_UPGRADE, DataVersion.CURRENT_VERSION + 1)) {
			provider.init();
		}
 		assertTrue(new File(DB4O_DATA_FILE_UPGRADE).exists());
 	}
 }
