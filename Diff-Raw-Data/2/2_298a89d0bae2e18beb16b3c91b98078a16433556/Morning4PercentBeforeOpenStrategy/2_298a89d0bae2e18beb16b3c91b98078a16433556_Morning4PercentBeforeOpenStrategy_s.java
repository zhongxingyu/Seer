 package com.ghlh.strategy.morning4percent;
 
 import java.util.List;
 
 import com.ghlh.autotrade.EventRecorder;
 import com.ghlh.data.db.MonitorstockVO;
 import com.ghlh.data.db.StocktradeDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.InternetStockQuotesInquirer;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.AdditionInfoUtil;
 import com.ghlh.strategy.BuyStockBean;
 import com.ghlh.strategy.OneTimeStrategy;
 import com.ghlh.strategy.TradeConstants;
 import com.ghlh.strategy.TradeUtil;
 import com.ghlh.util.MathUtil;
 
 public class Morning4PercentBeforeOpenStrategy implements OneTimeStrategy {
 	public void processStockTrade(MonitorstockVO monitorstockVO) {
 		List stockTradeList = StocktradeDAO
 				.getUnfinishedTradeRecords(monitorstockVO.getStockid(),
 						monitorstockVO.getTradealgorithm());
		if (stockTradeList.size() >= 0) {
 			dealSell(monitorstockVO, stockTradeList);
 		}
 	}
 
 	private void dealSell(MonitorstockVO monitorstockVO, List stockTradeList) {
 		StockQuotesBean stockQuotesBean = InternetStockQuotesInquirer
 				.getInstance().getStockQuotesBean(monitorstockVO.getStockid());
 		double currentPrice = stockQuotesBean.getCurrentPrice();
 		if (currentPrice == 0) {
 			currentPrice = stockQuotesBean.getYesterdayClose();
 		}
 
 		double possibleMaxPrice = currentPrice * TradeConstants.MAX_ZF;
 		double possibleMinPrice = currentPrice * TradeConstants.MAX_DF;
 
 		StocktradeVO stocktradeVO = (StocktradeVO) stockTradeList.get(0);
 		AdditionalInfoBean aib = (AdditionalInfoBean) AdditionInfoUtil
 				.parseAdditionalInfoBean(monitorstockVO.getAdditioninfo(),
 						monitorstockVO.getTradealgorithm());
 		if (aib.getTargetZf() != 0) {
 			double winSellPrice = stocktradeVO.getBuyprice()
 					* (1 + aib.getTargetZf());
 			winSellPrice = MathUtil.formatDoubleWith2QuanShe(winSellPrice);
 			stocktradeVO.setWinsellprice(winSellPrice);
 			if (aib.getLostDf() > 0) {
 				double lostSellPrice = stocktradeVO.getBuyprice()
 						* (1 - aib.getLostDf());
 				lostSellPrice = MathUtil
 						.formatDoubleWith2QuanShe(lostSellPrice);
 				stocktradeVO.setLostsellprice(lostSellPrice);
 			}
 			if (stocktradeVO.getWinsellprice() < possibleMaxPrice
 					|| stocktradeVO.getLostsellprice() > possibleMinPrice) {
 				TradeUtil.decideSellPrice(stocktradeVO);
 				String message = TradeUtil.getPendingSellMessage(
 						stocktradeVO.getStockid(), stocktradeVO.getNumber(),
 						stocktradeVO.getWinsellprice(),
 						stocktradeVO.getLostsellprice());
 				EventRecorder.recordEvent(this.getClass(), message);
 				TradeUtil.dealSell(stocktradeVO);
 			}
 		} else {
 			String message = TradeUtil.getOpenPriceSellMessage(
 					stocktradeVO.getStockid(), stocktradeVO.getNumber());
 			EventRecorder.recordEvent(this.getClass(), message);
 			TradeUtil.dealSell(stocktradeVO);
 		}
 	}
 }
