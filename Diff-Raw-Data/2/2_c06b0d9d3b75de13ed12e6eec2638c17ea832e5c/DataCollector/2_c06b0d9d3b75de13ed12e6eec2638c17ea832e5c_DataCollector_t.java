 package com.ghlh.autotrade;
 
 import java.util.Calendar;
 import java.util.Date;
 import java.util.List;
 
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.StockdailyinfoVO;
 import com.ghlh.stockquotes.EastMoneyStockQuotesInquirer;
 import com.ghlh.stockquotes.InternetStockQuotesInquirer;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.util.EastMoneyUtil;
 
 public class DataCollector {
 
 	public final String[] zs = { "000001", "399001", "399005", "399006" };
 
 	public void collectDailyInfo(Date now, boolean isPanzhong) {
 		if (isPanzhong) {
 			int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
 			String table = "stockdailyinfo" + hour;
 			StockdailyinfoVO.TABLE_NAME = table;
 		}
 		collectStocks(now, 10);
 		collectStocks(now, 20);
 		collectDataForZS(now);
 		StockdailyinfoVO.TABLE_NAME = "stockdailyinfo";
 	}
 
 	private void collectStocks(Date now, int marketCode) {
 		List<StockQuotesBean> list = EastMoneyUtil.collectData(
				Constants.SZ_STOCK_COUNT, marketCode);
 		for (int i = 0; i < list.size(); i++) {
 			StockQuotesBean sqb = list.get(i);
 			if (sqb.getCurrentPrice() != 0) {
 				GhlhDAO.createStockDailyIinfo(sqb, now);
 			}
 		}
 	}
 
 	private void collectDataForZS(Date now) {
 		for (int i = 0; i < zs.length; i++) {
 			StockQuotesBean sqb = ((EastMoneyStockQuotesInquirer) InternetStockQuotesInquirer
 					.getEastMoneyInstance()).getZSStockQuotes(zs[i]);
 			if (sqb.getStockId().startsWith("0")) {
 				String stockId = sqb.getStockId();
 				stockId = "9" + stockId.substring(1);
 				sqb.setStockId(stockId);
 			}
 			GhlhDAO.createStockDailyIinfo(sqb, now);
 		}
 	}
 
 }
