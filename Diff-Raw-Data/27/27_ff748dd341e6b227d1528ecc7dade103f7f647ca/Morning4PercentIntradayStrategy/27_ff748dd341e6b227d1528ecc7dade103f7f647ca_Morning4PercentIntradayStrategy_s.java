 package com.ghlh.strategy.morning4percent;
 
 import java.util.Date;
 import java.util.List;
 
 import com.ghlh.autotrade.EventRecorder;
 import com.ghlh.autotrade.StockTradeIntradyMonitor;
 import com.ghlh.autotrade.StockTradeIntradyUtil;
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.MonitoringStrategy;
 import com.ghlh.strategy.TradeConstants;
 import com.ghlh.strategy.TradeUtil;
 import com.ghlh.tradeway.SoftwareTrader;
 
 public class Morning4PercentIntradayStrategy implements MonitoringStrategy {
 	public void processSell(StockTradeIntradyMonitor monitor,
 			StockQuotesBean sqb) {
 		List possibleSellList = monitor.getPossibleSellList();
 		for (int i = 0; i < possibleSellList.size(); i++) {
 			StocktradeVO stocktradeVO = (StocktradeVO) possibleSellList.get(i);
 			if (stocktradeVO.getStatus() != TradeConstants.STATUS_SUCCESS
 					&& stocktradeVO.getStatus() != TradeConstants.STATUS_FAILURE) {
 				if (stocktradeVO.getWinsellprice() == 0) {
 					sellWithOpenPrice(sqb, stocktradeVO);
 				} else {
 					StockTradeIntradyUtil.processSell(monitor, sqb);
 				}
 			}
 		}
 	}
 
 	private void sellWithOpenPrice(StockQuotesBean sqb,
 			StocktradeVO stocktradeVO) {
 		String message = TradeUtil.getOpenPriceSellMessage(
 				stocktradeVO.getStockid(),
 				stocktradeVO.getNumber(), sqb.getCurrentPrice());
 		EventRecorder.recordEvent(StockTradeIntradyUtil.class,
 				message);
 		SoftwareTrader.getInstance()
 				.sellStock(stocktradeVO.getStockid(),
 						stocktradeVO.getNumber());
 		stocktradeVO.setSelldate(new Date());
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(stocktradeVO.getId());
 		stocktradeVO1.setWhereId(true);
 		if (sqb.getCurrentPrice() > stocktradeVO.getBuyprice()) {
 			stocktradeVO.setStatus(TradeConstants.STATUS_SUCCESS);
 			stocktradeVO1.setWinsellprice(sqb.getCurrentPrice());
 			stocktradeVO1.setStatus(TradeConstants.STATUS_SUCCESS);
 		} else {
 			stocktradeVO.setStatus(TradeConstants.STATUS_FAILURE);
 			stocktradeVO1.setLostsellprice(sqb.getCurrentPrice());
 			stocktradeVO1.setStatus(TradeConstants.STATUS_FAILURE);
 		}
 		GhlhDAO.edit(stocktradeVO1);
 	}
 
 	public void processBuy(StockTradeIntradyMonitor monitor, StockQuotesBean sqb) {
 	}
 
 }
