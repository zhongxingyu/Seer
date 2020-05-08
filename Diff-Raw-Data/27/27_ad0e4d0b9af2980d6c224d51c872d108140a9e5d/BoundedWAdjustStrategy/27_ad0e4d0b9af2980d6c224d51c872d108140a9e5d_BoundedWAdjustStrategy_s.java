 package com.myrontuttle.fin.trade.strategies;
 
 import java.util.Map;
 
 import com.myrontuttle.fin.trade.api.AlertService;
 import com.myrontuttle.fin.trade.api.AvailableAlert;
 import com.myrontuttle.fin.trade.api.PortfolioService;
 import com.myrontuttle.fin.trade.api.QuoteService;
 import com.myrontuttle.fin.trade.api.SelectedAlert;
 import com.myrontuttle.fin.trade.api.TradeStrategyService;
 
 /**
  * Provides a trade manager with a basic trade policy:
  * When a trade is opened a box is drawn to bound the trade.  Think of a time/price chart.
  * The left side of the box is the time the trade opened
  * The bottom (top when shorting) of the box is the acceptable loss
  * The right side of the box is the time the trade will close if nothing else happens
  * The top (bottom when shorting) of the box is the point when the box is adjusted like a trailing stop loss
  * 
  * @author Myron Tuttle
  */
 public class BoundedWAdjustStrategy extends BoundedStrategy {
 	
 	public final static String NAME = "Bounded With Adjustment";
 	public final static String DESCRIPTION = "Creates bounds around a trade to exit after a certain time or " +
 			"percent loss. Adjusts the bounds if a gain is reached.";
 	
 	public final static String ADJUST = "Adjust";
 
 	public static String[] availableTradeActions = new String[]{
 		OPEN,
 		ADJUST,
 		CLOSE
 	};
 
 	public static void takeAction(Event event, Trade t, 
 			PortfolioService portfolioService, QuoteService quoteService,
 			AlertService alertService, TradeStrategyService tradeStrategyService) throws Exception {
 
 		String actionType = event.getActionType();
 		if (actionType.equals(OPEN)) {
 			openTrade(t, portfolioService, quoteService, alertService, tradeStrategyService);
 		} else if (actionType.equals(ADJUST)) {
 			adjustTrade(t, portfolioService, quoteService, alertService, tradeStrategyService);
 		} else if (actionType.equals(CLOSE)) {
 			closeTrade(t, portfolioService, alertService, tradeStrategyService);
 		}
 	}
 
 	public static String[] describeTrade(Trade trade, PortfolioService portfolioService) throws Exception {
 		Map<String, Integer> parameters = trade.getParameters();
 		if (parameters == null || parameters.size() == 0) {
 			return new String[0];
 		}
 
 		String userId = trade.getUserId();
 		String openOrderType = portfolioService.
 									openOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)];
 		String closeOrderType = portfolioService.
 									closeOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)];
 		
 		String[] desc = new String[4];
 		desc[0] = "If an alert fires for " + trade.getSymbol() + " then " + portfolioService.
 				openOrderTypesAvailable(userId)[parameters.get(OPEN_ORDER)] + " it with " +
 				parameters.get(TRADE_ALLOC) + "% of portfolio.";
 
 		if (portfolioService.priceRiseGood(openOrderType)) {
 
 			desc[1] = "If the price of " + trade.getSymbol() + " falls below " + parameters.get(PERCENT_BELOW) +
 					"% then " + closeOrderType + " it.";
 
 			desc[2] = "If the price of " + trade.getSymbol() + " rises above " + parameters.get(PERCENT_ABOVE) +
 					"% then adjust these instructions.";
 		} else {
 
 			desc[1] = "If the price of " + trade.getSymbol() + " rises above " + parameters.get(PERCENT_ABOVE) +
 					"% then " + closeOrderType + " it.";
 
 			desc[2] = "If the price of " + trade.getSymbol() + " falls below " + parameters.get(PERCENT_BELOW) +
 					"% then adjust these instructions.";
 		}
 		
 		desc[3] = "If the position in " + trade.getSymbol() + " lasts longer than " + 
 				parameters.get(TIME_LIMIT) + " seconds then " + 
 				closeOrderType + " it.";
 				
 		return desc;
 	}
 
 	protected static void openTrade(Trade trade, 
 			PortfolioService portfolioService,
 			QuoteService quoteService,
 			AlertService alertService,
 			TradeStrategyService tradeStrategyService) throws Exception {
 		
 		String userId = trade.getUserId();
 		String portfolioId = trade.getPortfolioId();
 		if (portfolioService.openOrderTypesAvailable(userId).length != 
 				portfolioService.closeOrderTypesAvailable(userId).length) {
 			throw new Exception("Open and close order types must match.  Trade not made.");
 		}
 		Map<String, Integer> tradeParams = trade.getParameters();
 		
 		String openOrderType = portfolioService.
 									openOrderTypesAvailable(userId)[tradeParams.get(OPEN_ORDER)];
 		
 		boolean priceRiseGood = portfolioService.priceRiseGood(openOrderType);
 		
 		try {
 			double portfolioBalance = portfolioService.getAvailableBalance(userId, portfolioId);
 			double maxTradeAmount = portfolioBalance * (tradeParams.get(TRADE_ALLOC) / 100.0);
 			double currentPrice = quoteService.getLast(userId, trade.getSymbol());
 
 			if (currentPrice <= maxTradeAmount) {
 
 				// Open position
 				int quantity = (int)Math.floor(maxTradeAmount / currentPrice);
 				portfolioService.openPosition(userId, portfolioId, trade.getSymbol(), 
 						quantity, openOrderType);
 				
 				// stop loss
 				createStopTrade(trade, currentPrice, priceRiseGood, alertService, tradeStrategyService);
 				
 				// time in trade
 				createTimeLimit(trade, tradeParams.get(TIME_LIMIT), tradeStrategyService);
 				
 				// adjustment at
 				createAdjustment(trade, currentPrice, priceRiseGood, alertService, tradeStrategyService);
 				
 			} else {
 				throw new Exception("Not enough allocated to trade " + trade.getSymbol() +
 						". Current Price(" + currentPrice + ") > Max Allowed Trade Amount (" + 
 						maxTradeAmount + ")");
 				
 			}			
 		} catch (Exception e) {
 			throw new Exception("Unable to complete trade. " + e.getMessage());
 		}
 	}
 
 	private static void adjustTrade(Trade trade, 
 			PortfolioService portfolioService,
 			QuoteService quoteService,
 			AlertService alertService,
 			TradeStrategyService tradeStrategyService) throws Exception {
 		
 		if (!tradeStrategyService.tradeExists(trade.getTradeId())) {
 			throw new Exception("Trade isn't open. Can't make adjustment");
 		}
 		String userId = trade.getUserId();
 		String symbol = trade.getSymbol();
 		
 		Map<String, Integer> tradeParams = trade.getParameters();
 		String closeOrderType = portfolioService.
 				closeOrderTypesAvailable(userId)[tradeParams.get(OPEN_ORDER)];
 		boolean priceRiseGood = portfolioService.priceRiseGood(closeOrderType);
 		
 		try {
 			// Remove existing events since we're adding new ones
 			deleteTradeAlerts(trade, tradeStrategyService, alertService);
 			tradeStrategyService.removeAllTradeEvents(trade.getTradeId());
 			
 			double currentPrice = quoteService.getLast(userId, symbol);
 			
 			// stop loss
 			createStopTrade(trade, currentPrice, priceRiseGood, alertService, tradeStrategyService);
 			
 			// time in trade
 			createTimeLimit(trade, tradeParams.get(TIME_LIMIT), tradeStrategyService);
 			
 			// adjustment at
 			createAdjustment(trade, currentPrice, priceRiseGood, alertService, tradeStrategyService);
 			
 		} catch (Exception e) {
 			throw new Exception("Unable to adjust trade. " + e.getMessage());
 		}
 	}
 
 	private static void createAdjustment(Trade trade, double currentPrice, boolean priceRiseGood,
 								AlertService alertService, TradeStrategyService tradeStrategyService) 
 													throws Exception {
 
 		String alertUserId = trade.getAlertUserId();
 		AvailableAlert alertWhen = (priceRiseGood) ? alertService.getPriceAboveAlert(alertUserId) :
 										alertService.getPriceBelowAlert(alertUserId);
 		String alertType = (priceRiseGood) ? PERCENT_BELOW : PERCENT_ABOVE;
 		
 		double adjustmentPrice = currentPrice + 
 					(trade.getParameters().get(alertType) / 100) * currentPrice;
 		SelectedAlert adjustmentAlert = new SelectedAlert(alertWhen.getId(),
 											alertWhen.getCondition(),
 											trade.getSymbol(),
 											adjustmentPrice);
 		String[] alertIds = alertService.setupAlerts(alertUserId, adjustmentAlert);
 		for(String alertId : alertIds) {
			tradeStrategyService.setTradeEvent(trade.getTradeId(), alertWhen.getCondition(), CLOSE, alertId);
 		}
 	}
 }
