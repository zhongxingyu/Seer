 package com.ghlh.autotrade;
 
 import java.util.Date;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.quartz.Job;
 import org.quartz.JobExecutionContext;
 import org.quartz.JobExecutionException;
 
 import com.ghlh.data.db.GhlhDAO;
 import com.ghlh.data.db.MonitorstockDAO;
 import com.ghlh.data.db.MonitorstockVO;
 import com.ghlh.data.db.StockdailyinfoVO;
 import com.ghlh.data.db.StocktradeDAO;
 import com.ghlh.data.db.StocktradeVO;
 import com.ghlh.stockquotes.InternetStockQuotesInquirer;
 import com.ghlh.stockquotes.StockQuotesBean;
 import com.ghlh.strategy.TradeConstants;
 import com.ghlh.strategy.TradeUtil;
 
 public class AutoTradeAfterCloseJob implements Job {
 	private static Logger logger = Logger
 			.getLogger(AutoTradeAfterCloseJob.class);
 
 	public void execute(JobExecutionContext arg0) throws JobExecutionException {
 		String message = "ʼ15:05̺";
 		EventRecorder.recordEvent(this.getClass(), message);
 		processPredoneOrders();
 		collectStockDailyInfo();
 		message = "15:05̺";
 		EventRecorder.recordEvent(this.getClass(), message);
 	}

 	private void collectStockDailyInfo() {
		for (int i = 1; i < 1000; i++) {
 			String stockId = this.getStockCYBId(i);
 			StockQuotesBean sqb = InternetStockQuotesInquirer.getEastMoneyInstance()
 					.getStockQuotesBean(stockId);
 			createStockDailyIinfo(stockId, sqb);
 		}
 
		for (int i = 1; i < 1000; i++) {
 			String stockId = this.getStockSMBId(i);
 			StockQuotesBean sqb = InternetStockQuotesInquirer.getEastMoneyInstance()
 					.getStockQuotesBean(stockId);
 			createStockDailyIinfo(stockId, sqb);
 		}
 	}
 
 	private void createStockDailyIinfo(String stockId, StockQuotesBean sqb) {
 		if (sqb != null) {
 			StockdailyinfoVO stockdailyinfoVO = new StockdailyinfoVO();
 			stockdailyinfoVO.setStockid(stockId);
 			stockdailyinfoVO.setDate(new Date());
 			stockdailyinfoVO.setTodayopenprice(sqb.getTodayOpen());
 			stockdailyinfoVO.setCurrentprice(sqb.getCurrentPrice());
 			stockdailyinfoVO.setHighestprice(sqb.getHighestPrice());
 			stockdailyinfoVO.setLowestprice(sqb.getLowestPrice());
 			stockdailyinfoVO.setYesterdaycloseprice(sqb.getYesterdayClose());
 			stockdailyinfoVO.setZde(sqb.getZde());
 			stockdailyinfoVO.setZdf(sqb.getZdf());
 			stockdailyinfoVO.setHsl(sqb.getHsl());
 			stockdailyinfoVO.setCreatedtime(new Date());
 			stockdailyinfoVO.setLastmodifiedtime(new Date());
 			GhlhDAO.create(stockdailyinfoVO);
 		}
 	}
 
 	public static void main(String[] args){
 		new AutoTradeAfterCloseJob().collectStockDailyInfo();
 	}
 	private String getStockSMBId(int no) {
 		String result = no + "";
 		if ((no + "").length() == 1) {
 			result = "00" + no;
 		}
 		if ((no + "").length() == 2) {
 			result = "0" + no;
 		}
 		return "002" + result;
 	}
 	
 	private String getStockCYBId(int no) {
 		String result = no + "";
 		if ((no + "").length() == 1) {
 			result = "00" + no;
 		}
 		if ((no + "").length() == 2) {
 			result = "0" + no;
 		}
 		return "300" + result;
 	}
 
 	private void processPredoneOrders() {
 		List unfinishedTrades = StocktradeDAO.getUnfinishedTradeRecords();
 		for (int i = 0; i < unfinishedTrades.size(); i++) {
 			StocktradeVO stVO = (StocktradeVO) unfinishedTrades.get(i);
 			String eventMessage = null;
 			if (stVO.getStatus() == TradeConstants.STATUS_POSSIBLE_SELL
 					|| stVO.getStatus() == TradeConstants.STATUS_T_0_BUY) {
 				eventMessage = TradeUtil.getAfterCloseEventMessage(stVO);
 				StocktradeDAO.updateStocktradeStatus(stVO.getId(),
 						TradeConstants.STATUS_HOLDING);
 			}
 			if (stVO.getStatus() == TradeConstants.STATUS_PENDING_BUY) {
 				eventMessage = TradeUtil.getAfterCloseEventMessage(stVO);
 				StocktradeDAO.removeStocktrade(stVO.getId());
 			}
 			if (eventMessage != null) {
 				EventRecorder.recordEvent(this.getClass(), eventMessage);
 			}
 		}
 	}
 }
