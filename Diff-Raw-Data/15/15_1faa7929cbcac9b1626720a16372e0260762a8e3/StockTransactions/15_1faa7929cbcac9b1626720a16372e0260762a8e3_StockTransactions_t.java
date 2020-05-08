 package unitTests;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Locale;
 
 import org.junit.*;
 import org.junit.runner.*;
 import vsp.*;
 import vsp.dal.requests.Orders;
 import vsp.dal.requests.PortfolioEntries;
 import vsp.dal.requests.Stocks;
 import vsp.dal.requests.Transactions;
 import vsp.dal.requests.Users;
 import vsp.dataObject.DividendInfo;
 import vsp.dataObject.HistoricalStockInfo;
 import vsp.dataObject.Order;
 import vsp.dataObject.PortfolioData;
 import vsp.dataObject.Stock;
 import vsp.dataObject.StockInfo;
 import vsp.dataObject.StockTransaction;
 import vsp.utils.Enumeration.OrderAction;
 import vsp.utils.Enumeration.OrderState;
 import vsp.utils.Enumeration.OrderType;
 import vsp.utils.Enumeration.TimeInForce;
 
 @RunWith(OrderedRunner.class)
 public class StockTransactions
 {
 	private final SimpleDateFormat historicalDateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
 	private final VspServiceProvider vsp = new VspServiceProvider();
 	private final StockInfoServiceProvider stockInfoSP = new StockInfoServiceProvider(); 
 	private final static String userName = "test";
 	
 	@BeforeClass
 	public static void setUpBeforeClass() throws Exception
 	{
 		// delete all stocks (should delete all portfolio entries, orders and transactions too because of the DB constraints)
 		List<Stock> stocks = Stocks.getAllStocks();
 		for (Stock stock : stocks)
 		{
 			Stocks.deleteStock(stock.getStockSymbol());
 		}
 		
 		// ensure all stocks are gone
 		stocks = Stocks.getAllStocks();
 		Assert.assertNotNull(stocks);
 		Assert.assertTrue(stocks.size() == 0);
 		
 		// ensure all owned stocks are gone
 		List<PortfolioData> ownedItems = PortfolioEntries.requestAllUserStocks(userName);
 		Assert.assertNotNull(ownedItems);
 		Assert.assertTrue(ownedItems.size() == 0);
 		
 		// ensure all orders are gone
 		List<Order> orders = Orders.getOrdersForUser(userName);
 		Assert.assertNotNull(orders);
 		Assert.assertTrue(orders.size() == 0);
 		
 		// ensure all transactions are gone
 		List<StockTransaction> transactions = Transactions.getTransactionsForUser(userName);
 		Assert.assertNotNull(transactions);
 		Assert.assertTrue(transactions.size() == 0);
 		
 		// set user balance to 0
 		Users.updateBalance(userName, 0.0);
 	}
 	
 	@Test
 	@unitTests.Order(order=1)
 	public void getStockInfo()
 	{
 		StockInfo stockInfo = stockInfoSP.requestCurrentStockData("AAPL");
 		Assert.assertNotNull("Unable to retrieve stock info.", stockInfo);
 	}
 	
 	@Test
 	@unitTests.Order(order=2)
 	public void getStockSearchResults()
 	{
 		List<Stock> stocks = stockInfoSP.searchForStocks("AAPL");
 		Assert.assertNotNull("Unable to retrieve search results.", stocks);
		Assert.assertTrue(stocks.size() >= 1);
 	}
 	
 	@Test
 	@unitTests.Order(order=3)
 	public void unreachableStockInfoProviderTests() throws Exception
 	{
 		StockInfoServiceProvider sisp = new StockInfoServiceProvider();
 		sisp.setCurrentInfoBaseUrl("http://127.0.0.1"); // should timeout
 		sisp.setHistoryBaseUrl("http://127.0.0.1"); // should timeout
 		
 		StockInfo stockInfo = sisp.requestCurrentStockData("AAPL");
 		Assert.assertNull("Should not be returning any results for current stock data.", stockInfo);
 		
 		Assert.assertFalse(sisp.isWithinTradingHours());
 		
 		List<String> symbols = new ArrayList<String>();
 		symbols.add("AAPL");
 		symbols.add("GOOG");
 		List<StockInfo> currentResults = sisp.requestCurrentStockData(symbols);
 		Assert.assertNotNull(currentResults);
 		Assert.assertTrue(currentResults.size() == 0);
 		
 		Date since = historicalDateFormat.parse("2011-10-01");
 		List<HistoricalStockInfo> historicalResults = sisp.requestDailyHistoricalStockData("AAPL", since);
 		Assert.assertNotNull(historicalResults);
 		Assert.assertTrue(historicalResults.size() == 0);
 		
 		historicalResults = sisp.requestHistoricalStockData("AAPL", 3);
 		Assert.assertNotNull(historicalResults);
 		Assert.assertTrue(historicalResults.size() == 0);
 		
 		HistoricalStockInfo historicalStockInfo = sisp.requestHistoricalStockDataForDay("AAPL", since);
 		Assert.assertNull("Should not be returning any results for historical stock data.", historicalStockInfo);
 		
 		List<DividendInfo> dividendResults = sisp.requestHistoricalDividendInfoSince("AAPL", since);
 		Assert.assertNotNull(dividendResults);
 		Assert.assertTrue(dividendResults.size() == 0);
 	}
 	
 	@Test
 	@unitTests.Order(order=4)
 	public void showUserTransactionHistory()  throws Exception
 	{		
 		// TODO:  ensure there is at least one transaction before calling this unit test (or add some default ones to database)
 		List<StockTransaction> transactions = vsp.getTransactionHistory(userName);
 		Assert.assertNotNull("Unable to retrieve transaction history.", transactions);
 		Assert.assertTrue(transactions.size() > 0);
 	}
 	
 	@Test
 	@unitTests.Order(order=5)
 	public void showUserPendingOrdersWhenNoneExist() throws Exception
 	{
 		List<Order> orders = vsp.getPendingOrders(userName);
 		Assert.assertNotNull("Unable to retrieve pending orders.", orders);
 		Assert.assertTrue(orders.size() == 0);
 	}
 	
 	@Test
 	@unitTests.Order(order=6)
 	public void buyStockInsufficientFunds()
 	{
 		// TODO: implement
 		Assert.fail("May need to be executed manually, as sufficient funds are calculated in the JSP (or we can move to createOrder method in VSP).");
 	}
 	
 	@Test
 	@unitTests.Order(order=7)
 	public void buyMarketOrderStockInsufficientFunds() throws Exception
 	{
 		// TODO: implement
 		Assert.fail("May need to be executed manually, as sufficient funds are calculated in the JSP (or we can move to createOrder method in VSP).");
 		
 		// return funds now that "insufficient" tests are over
 		Users.updateBalance(userName, Users.DEFAULT_BALANCE);
 	}
 	
 	@Test
 	@unitTests.Order(order=8)
 	public void buyStock()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=9)
 	public void buyMarketOrderStock() throws Exception
 	{
		Order order = null;
		try
		{
			order = vsp.createOrder(userName, 
 				OrderAction.BUY.toString(), 
 				"MSFT", 
 				"100", 
 				OrderType.MARKET.toString(),
 				TimeInForce.DAY.toString(), // only DAY is supported for a MARKET order
 				"0.0",
 				"0.0"
 				);
		}
		catch (Exception ex)
		{
			Assert.fail("Unhandled exception: " + ex.getLocalizedMessage());
		}
		
 		Assert.assertNotNull("Unable to create order.", order);
 		Assert.assertNotNull("Order ID is null.", order.getId());
 	}
 		
 	@Test
 	@unitTests.Order(order=10)
 	public void buyLimitOrderStockLimitPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=11)
 	public void buyLimitOrderStockLimitPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=12)
 	public void buyStopOrderStockStopPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=13)
 	public void buyStopOrderStockStopPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=14)
 	public void buyStopLimitOrderStockStopPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=15)
 	public void buyStopLimitOrderStockStopPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=16)
 	public void dayOrderExecution()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=17)
 	public void dayOrderExpired()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=18)
 	public void goodUntilCancelledExecution()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=19)
 	public void goodUntilCancelledExpired()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=20)
 	public void fillOrKillFilled()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=21)
 	public void fillOrKillKilled()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test	
 	@unitTests.Order(order=22)
 	public void immediateOrCancel()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=23)
 	public void displayUserPortfolio() throws Exception
 	{
 		// TODO: implement (user must own some stuff first)
 		Assert.fail("May need to be executed manually");
 	}
 	
 	@Test
 	@unitTests.Order(order=24)
 	public void showUserPendingOrdersWhenSomeExist() throws Exception
 	{
 		// TODO:  ensure that at least one order has been submitted that will remain in a pending state for a few seconds
 		List<Order> orders = vsp.getPendingOrders(userName);
 		Assert.assertNotNull("Unable to retrieve pending orders.", orders);
 		Assert.assertTrue(orders.size() > 0);
 	}
 	
 	@Test
 	@unitTests.Order(order=25)
 	public void cancelPendingOrder() throws Exception
 	{
 		List<Order> pendingOrders = vsp.getPendingOrders(userName);
 		Assert.assertNotNull("Unable to retrieve pending orders.", pendingOrders);
 		Assert.assertTrue(pendingOrders.size() > 0);
 		
 		Order order = pendingOrders.get(0);
 		Assert.assertTrue(order.getState() == OrderState.PENDING);
 		vsp.cancelOrder(userName, order.getId());
 		
 		order = Orders.getOrderById(order.getId());
 		Assert.assertTrue(order.getState() == OrderState.CANCELLED);
 	}
 	
 	@Test
 	@unitTests.Order(order=26)
 	public void sellStockMoreThanOwned()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=27)
 	public void sellStock()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 		
 	@Test
 	@unitTests.Order(order=28)
 	public void sellStockMarketOrder()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=29)
 	public void sellStockLimitOrderLimitPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=30)
 	public void sellStockLimitOrderLimitPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=31)
 	public void sellStockStopLossOrderStopPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=32)
 	public void sellStockStopLossOrderStopPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=33)
 	public void sellStockStopLimitOrderStopPriceMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 	
 	@Test
 	@unitTests.Order(order=34)
 	public void sellStockStopLimitOrderStopPriceNotMet()
 	{
 		// TODO: implement
 		Assert.fail("Not yet implemented");
 	}
 }
