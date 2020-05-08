 package com.IBTrading.Traders;
 
 import com.IBTrading.servlets.IBTradingAPI;
 import com.IBTrading.tradeparsers.JasonBondsTradeParser;
 import com.IBTrading.servlets.OrderStatus;
 
 public class JasonBondsTrader extends Trader{
 	// Passed parameters
 	private String tradeString;
 	
 	// Parsed trade information
 	JasonBondsTradeParser parser;
 	
 	// List of trader identifiers and their strings
 	private static String lastTraderString;
 	
 	// CONSTANTS
 	private final int SECONDS = 1000;
 	private final String BUY = "BUY";
 	private final String SELL = "SELL";
 	private static final int TRADERPERCENTAGE = 100;
 	private static final int MAXLEVERAGE = 4;
 	private static final int NOLEVERAGE = 1;
 	
 	public JasonBondsTrader(String newTrade, IBTradingAPI newTradingAPI, boolean newRealTimeSystem)
 	{	
 		super(newTradingAPI);
 		
 		// If we have already parsed this string, return
 		if( (newTrade != null) && (newTrade.equalsIgnoreCase(lastTraderString)) )
 		{
 			hasValidTrade = false;
 			System.out.println("Duplicate trade, " + tradeString);
 			return;
 		}
 		
 		lastTraderString = newTrade;
 		parser = new JasonBondsTradeParser(newTrade);
 		hasValidTrade = parser.parseTrade();
 		if(hasValidTrade == true)
 		{
 			lastTraderString = newTrade;
 			hasValidTrade = true;
 		}
 	}
 
 	// Initiates the trade with TWS
 	public String trade()
 	{
 		// Make the purchase
 		boolean isSimulation = false;
 		int simulationQuantity = (parser.quantity * TRADERPERCENTAGE) / 100;
 		int quantity;
 		int maxCash;
 		int maxCashForAdds = 5900;
 		int totalCash;
 		OrderStatus orderStatus, orderStatusSimulation;
 		
 		// Get the cash from our account
 		totalCash = (int) tradingAPI.getAvailableFunds(isSimulation);
 		
 		// If something went wrong and we were unable to get the cash
 		if(totalCash == 0)
 			totalCash = 5900;
 		
 		// Get the cash
 		maxCash = super.getCash(totalCash, MAXLEVERAGE);
 		
 		// If the price/share of the stock was not supplied, get this information from TWS
 		if(parser.price.equalsIgnoreCase("0.00"))
 		{
 			String marketData = "LAST_PRICE";  
 			int tickerID = tradingAPI.subscribeToMarketData(parser.symbol, isSimulation);
 		
 			// Wait until we have received the market data
 			while(tradingAPI.getMarketData(tickerID, marketData) == 0.0){};
 			
 			// Get the market price
 			parser.price = tradingAPI.getMarketData(tickerID, marketData) + "";
 		}
 		
 		try
 		{
 			if(parser.action.equalsIgnoreCase("Added"))
 				quantity = super.getQuantity(maxCashForAdds, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
 			else
 				quantity = super.getQuantity(maxCash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
 		}catch(Exception e)
 		{
 			e.printStackTrace();
 			quantity = (parser.quantity * TRADERPERCENTAGE) / 100;
 		}
 		
 		if(quantity <= 0)
 		{
 			return "Invalid quantity, " + quantity;
 		}
 		
 		// Place the order
 		orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation, null);
 		
 		if(orderStatus == null)
 			return "Unable to connect to TWS...";
 		
 		// Make the purchase with the Simulator
 		isSimulation = true;
 		orderStatusSimulation = tradingAPI.placeOrder(BUY, parser.symbol, simulationQuantity, isSimulation, null);  
 		
 		// Sleep for 60 seconds, then sell
 		try
 		{			
 			int timeTilSell = 60;  
 			boolean cashOnlyOrderFlag = false;
 			
 			// Check the desired information every second for 60 seconds
 			for(int numSeconds = 0; numSeconds < timeTilSell; numSeconds++)
 			{
 				//System.out.println("orderID = " + orderStatus.orderId + ", orderStatus = " + orderStatus.status);
 				
 				// If we were unable to buy due to the stock being not marginable
 				if( (orderStatus.status.equalsIgnoreCase("Inactive") == true) && (cashOnlyOrderFlag == false) )
 				{
 					System.out.println("Order was rendered inactive, trying again with our total cash, " + totalCash);
 					cashOnlyOrderFlag = true;
 					
 					// Make the trade using only cash (no leverage)
					isSimulation = false;
 					int cash = super.getCash(totalCash, NOLEVERAGE);
 					quantity = super.getQuantity(cash, Double.parseDouble(parser.price), TRADERPERCENTAGE, parser.quantity);
 					orderStatus = tradingAPI.placeOrder(BUY, parser.symbol, quantity, isSimulation, null);
 					
 					if( (orderStatus == null) || (orderStatus.status == null) )
 						return "Unable to connect to TWS...";
 					
 					// Sleep for the remaining time
 					Thread.sleep((timeTilSell - numSeconds) * SECONDS);
 					
 					// We are done sleeping, sell!
 					break;
 				}
 				
 				// Sleep for one second
 				Thread.sleep( 1 * SECONDS );
 			}
 			
 			
 			// If the order was unsuccessful, exit
 			if( (orderStatus == null) || (orderStatus.status == null) || (orderStatus.status.equalsIgnoreCase("Inactive") == true) 
 					|| orderStatus.status.equalsIgnoreCase("Cancelled") || orderStatus.status.equalsIgnoreCase("PendingCancel") )
 			{
 				return "We were unable to purchase - " + orderStatus.status;
 			}
 			
 			// Cancel the order if we have not purchased any stock
 			if(orderStatus.filled == 0)
 			{
 				isSimulation = false;
 				tradingAPI.cancelOrder(orderStatus, isSimulation);
 				return "Order canceled - we did not buy within the time limit";
 			}
 			// If we have not completed the order, complete it
 			else if(orderStatus.status.equalsIgnoreCase("Filled") == false)
 			{
 				isSimulation = false;
 				tradingAPI.cancelOrder(orderStatus, isSimulation);
 				
 				// Wait until we have either completed the order or it is cancelled
 				while( (orderStatus.status.equalsIgnoreCase("Cancelled") == false) &&
 						(orderStatus.status.equalsIgnoreCase("Filled") == false) ){};
 						
 				// Get the quantity to sell
 				quantity = orderStatus.filled;
 			}
 		}
 		catch ( InterruptedException e )
 		{
 			System.out.println( "awakened prematurely" );
 		}
 		
 		// Sell the stocks
 		isSimulation = false;
 		orderStatus = tradingAPI.placeOrder(SELL, parser.symbol, quantity, isSimulation, null);
 		
 		if(orderStatus == null)
 			return "Unable to connect to TWS...";
 		
 		// Sell the stocks over the simulator
 		isSimulation = true;
 		tradingAPI.placeOrder(SELL, parser.symbol, simulationQuantity, isSimulation, null);
 		
 		return null;
 	}
 }
