 package com.myrontuttle.fin.trade.web;
 
 import com.myrontuttle.fin.trade.api.TradeStrategyService;
 
 /**
  * Service locator for Evolve Service
  */
 public class StrategyAccess {
 	
 	private static TradeStrategyService tradeStrategyService;
 
 	public static TradeStrategyService getTradeStrategyService() {
 		return tradeStrategyService;
 	}
 
	public static void setTradeStrategyService(TradeStrategyService tradeStrategyService) {
 		StrategyAccess.tradeStrategyService = tradeStrategyService;
 	}
 
 }
