 package org.investovator.dataPlayBackEngine.scheduler;
 
import org.investovator.core.data.HistoryDataAPI;
import org.investovator.core.data.types.HistoryOrderData;
 import org.investovator.dataPlayBackEngine.data.BogusHistoryDataGenerator;
 import org.investovator.dataPlayBackEngine.events.EventManager;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.*;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class EventTask extends TimerTask {
     int i=0;
 
     //stock to playback
     private String stockId;
     //start time
     private Date currentTime;
     //Data interface
     HistoryDataAPI dataApi=new BogusHistoryDataGenerator();
 
     EventManager eventManager;
 
 
     public EventTask(String stockId, String startT) {
         this.stockId = stockId;
         eventManager=new EventManager();
 
         //String sourceDate=currentTime;
         SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-kk-mm-ss"); //should be in format year-month-date-24hr-minute-second
         try {
              currentTime =format.parse(startT);
 
         } catch (ParseException e) {
             e.printStackTrace();
         }
         ;
     }
 
     @Override
     public void run() {
 
         HistoryOrderData[] data=dataApi.getTradingData(currentTime,incrementTimeBySeconds(1),"goog");
         //print the data
         for(HistoryOrderData d:data){
             eventManager.notifyListners(d);
         }
 
         // in order to point to the next time interval
         currentTime=incrementTimeBySeconds(1);
     }
 
     /**
      * Increases the current time by the given number of seconds
      *
      * @param seconds the number of seconds to increase by
      */
     private Date incrementTimeBySeconds(int seconds){
         Calendar cal = Calendar.getInstance();
         cal.setTime(currentTime);
         cal.add(Calendar.SECOND, seconds); //minus number would decrement the days
         return cal.getTime();
 
     }
 
     public void setObserver(Observer observer){
         eventManager.addObserver(observer);
     }
 }
