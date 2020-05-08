 package com.ghlh.autotrade;
 
 import java.util.List;
 
 import com.ghlh.data.db.MonitorstockDAO;
 import com.ghlh.data.db.MonitorstockVO;
 import com.ghlh.strategy.OneTimeStrategy;
 import com.ghlh.util.ReflectUtil;
 import com.ghlh.util.StockMarketUtil;
 
 public class OneTimeJobUtil {
 
 	public static void processOneTimeStrategy(String oneTimeType) {
 		List monitorStocksList = MonitorstockDAO.getMonitorStock();
 		for (int i = 0; i < monitorStocksList.size(); i++) {
			MonitorstockVO monitorstockVO = (MonitorstockVO) monitorStocksList
					.get(i);
			OneTimeStrategy ts = (OneTimeStrategy) ReflectUtil
					.getClassInstance("com.ghlh.strategy",
							monitorstockVO.getTradealgorithm(), oneTimeType);
			ts.processStockTrade(monitorstockVO);
 		}
 	}
 
 }
