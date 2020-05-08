 package org.investovator.controller.data;
 
 import org.investovator.controller.data.types.HistoryOrderData;
import org.investovator.core.excelimporter.HistoryData;
 
 import java.util.Date;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public interface HistoryDataAPI {
 
     public HistoryOrderData[] getTradingData(Date startTime, Date endTime, String stockId );
 
     public HistoryData getOHLCPData(Date day,String stockId);
 }
