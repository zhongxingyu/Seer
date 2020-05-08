 package stockmaster;
 
 import java.util.ArrayList;
 import java.util.Date;
 
 import stockmaster.algo.RideTheTideImpl;
 import stockmaster.algo.TradingAlgorithm;
 import stockmaster.manager.MarketDataManager;
 import stockmaster.manager.PortfolioManager;
 import stockmaster.marketdata.*;
 import stockmaster.marketdata.MarketDataEmulatorImpl.Market;
 import stockmaster.recorder.DataRecorder;
 import stockmaster.recorder.CSVFileRecorderImpl;
 import stockmaster.recorder.MongoRecorderImpl;
 import stockmaster.unit.PortfolioStock;
 import stockmaster.util.Log;
 import stockmaster.util.Log.LogLevel;
 
 /* Main Application
  * Starts up the application with predefined MarketData source and Algorithm.
  */
 public class StockMaster {
 	
 	// set debug level
 	public static LogLevel LOG_LEVEL = LogLevel.INFO;
 	
 
 
 	private MarketDataManager dataManager;
 	
 	
 	public StockMaster(MarketDataManager marketData) {
 		this.dataManager = marketData;
 	
 	}
 	
 	
 	public void start() {
 		dataManager.execute();
 	}
 	
 	public MarketDataManager getMarketDataManager() {
 		return dataManager;
 	}
 	
 	public static void main(String[] args) {
 		Log.logLevel = LOG_LEVEL;
 		System.out.println("Starting StockMaster in "+Log.logLevel+" mode.");
 		
 		// Starts the application using SGX Web Market Data 
 		StockMaster stockMaster = new StockMaster(new MarketDataManager());
     
 		//Define recorder to use with marketData
 		//Datetime format yyyyMMdd hh:mm:ss 
		stockMaster.getMarketDataManager().loadRecorder(new CSVFileRecorderImpl("", "SGX", Log.getFormattedDateTime("20120904 08:58:00"), Log.getFormattedDateTime("20120904 17:05:00")));
 		//stockMaster.getMarketDataManager().loadRecorder("sgx", Log.getFormattedDateTime("20120904 08:58:00"), Log.getFormattedDateTime("20120917 17:05:00")));
 		
 		// Define algorithm stock manager would use to monitor the market
        // stockMaster.getMarketDataManager().loadAlgo(new RideTheTideImpl(stockMaster));	
         stockMaster.start();
 
         /**
          * sample:
          * { "_id" : ObjectId("504ef83330040f1ede24eb3a"), "stockCode" : "TST", "stockName" : "test stock", "purchasedPrice" : 10.99, "quantity" : 25, "acquiredDate" : ISODate("2012-09-11T08:37:07.061Z"), "tradingAlorithm" : "class stockmaster.algo.RideTheTideImpl" }
          */
 //        PortfolioManager portfolioManager = PortfolioManager.getInstance();
 //        RideTheTideImpl algo = new RideTheTideImpl(stockMaster);
 //        PortfolioStock portfolioStock = new PortfolioStock("TST", "test stock", 10.99d, 25, algo);
 //        portfolioManager.addStockToPortfolio(portfolioStock,"my_portfolio");
 
 
 	}
 }
