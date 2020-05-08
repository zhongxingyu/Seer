 package com.ghlh.strategy.backma10;
 
 import java.util.Date;
 import java.util.List;
 
 import com.common.util.IDGenerator;
 import com.ghlh.autotrade.EventRecorder;
 import com.ghlh.autotrade.StockTradeIntradyMonitor;
 import com.ghlh.autotrade.StockTradeIntradyUtil;
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.StockdailyinfoDAO;
 import com.ghlh.data.db.StockdailyinfoVO;
 import com.ghlh.data.db.StocktradeDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.AdditionInfoUtil;
 import com.ghlh.strategy.MonitoringStrategy;
 import com.ghlh.strategy.TradeConstants;
 import com.ghlh.strategy.TradeUtil;
 import com.ghlh.tradeway.SoftwareTrader;
 import com.ghlh.util.MathUtil;
 
 public class BackMA10IntradayStrategy implements MonitoringStrategy {
 	public void processSell(StockTradeIntradyMonitor monitor,
 			StockQuotesBean sqb) {
 		List possibleSellList = monitor.getPossibleSellList();
 		for (int i = 0; i < possibleSellList.size(); i++) {
 			StocktradeVO stocktradeVO = (StocktradeVO) possibleSellList.get(i);
 			if (sqb.getHighestPrice() >= stocktradeVO.getSellprice()) {
 				String message = TradeUtil.getConfirmedSellMessage(
 						stocktradeVO.getStockid(), stocktradeVO.getNumber(),
 						stocktradeVO.getSellprice());
 				EventRecorder.recordEvent(StockTradeIntradyUtil.class, message);
 				StocktradeDAO.updateStocktradeFinished(stocktradeVO.getId());
 			}
 		}
 
 	}
 
 	public void processBuy(StockTradeIntradyMonitor monitor, StockQuotesBean sqb) {
 		if (!Boolean
 				.parseBoolean(monitor.getMonitorstockVO().getOnmonitoring())) {
 			return;
 		}
 
 		List possibleSellList = monitor.getPossibleSellList();
 		if (possibleSellList.size() == 0) {
 			List previousDailyInfo = StockdailyinfoDAO.getPrevious9DaysInfo(sqb
 					.getStockId());
 			int days = previousDailyInfo.size();
 			double sumClosePrice = 0;
 			for (int i = 0; i < previousDailyInfo.size(); i++) {
 				StockdailyinfoVO dailyInfo = (StockdailyinfoVO) previousDailyInfo
 						.get(i);
				sumClosePrice += dailyInfo.getCloseprice();
 			}
 			double ma10Price = MathUtil
 					.formatDoubleWith2QuanJin((sumClosePrice + sqb
 							.getCurrentPrice()) / (days + 1));
 			if (sqb.getCurrentPrice() <= ma10Price) {
 				AdditionalInfoBean aib = (AdditionalInfoBean) AdditionInfoUtil
 						.parseAdditionalInfoBean(monitor.getMonitorstockVO()
 								.getAdditioninfo(),
 								BackMA10Constants.BACKMA10_STRATEGY_NAME);
 				int number = TradeUtil.getTradeNumber(aib.getTradeMoney(),
 						sqb.getCurrentPrice());
 				String message = TradeUtil.getConfirmedBuyMessage(monitor
 						.getMonitorstockVO().getStockid(), number, sqb
 						.getCurrentPrice());
 
 				EventRecorder.recordEvent(StockTradeIntradyUtil.class, message);
 				SoftwareTrader.getInstance().buyStock(sqb.getStockId(), number);
 				createBuyRecord(monitor, sqb, aib, number);
 			}
 		}
 	}
 
 	private void createBuyRecord(StockTradeIntradyMonitor monitor,
 			StockQuotesBean sqb, AdditionalInfoBean aib, int number) {
 		StocktradeVO stocktradeVO1 = new StocktradeVO();
 		stocktradeVO1.setId(IDGenerator.generateId("stocktrade"));
 		stocktradeVO1.setStockid(sqb.getStockId());
 		stocktradeVO1.setTradealgorithm(monitor.getMonitorstockVO()
 				.getTradealgorithm());
 		stocktradeVO1.setBuydate(new Date());
 		stocktradeVO1.setBuybaseprice(sqb.getCurrentPrice());
 		stocktradeVO1.setBuyprice(sqb.getCurrentPrice());
 		stocktradeVO1.setNumber(number);
 		double sellPrice = sqb.getCurrentPrice() * (1 + aib.getTargetZf());
 		sellPrice = MathUtil.formatDoubleWith2QuanShe(sellPrice);
 		stocktradeVO1.setSellprice(sellPrice);
 		stocktradeVO1.setCreatedtimestamp(new Date());
 		stocktradeVO1.setLastmodifiedtimestamp(new Date());
 		stocktradeVO1.setStatus(TradeConstants.STATUS_T_0_BUY);
 		GhlhDAO.create(stocktradeVO1);
 	}
 }
