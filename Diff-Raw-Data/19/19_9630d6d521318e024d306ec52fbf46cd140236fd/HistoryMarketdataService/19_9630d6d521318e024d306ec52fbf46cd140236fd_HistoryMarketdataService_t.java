 package com.mengruojun.common.service;
 
 import com.mengruojun.common.dao.HistoryDataKBarDao;
 import com.mengruojun.common.domain.HistoryDataKBar;
 import com.mengruojun.common.domain.Instrument;
 import com.mengruojun.common.domain.TimeWindowType;
 import com.mengruojun.common.domain.enumerate.IndicatorType;
 import org.apache.log4j.Logger;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  */
 @Service
 public class HistoryMarketdataService {
   Logger logger = Logger.getLogger(this.getClass());
 
   @Autowired
   HistoryDataKBarDao historyDataKBarDao;
 
   /**
    * This method will be called by marketDate JMS Receiver .
    * This method will do the following on the fly.
    * <p/>
    * 1. save bar into DB
    * 2. generate bars into DB
    * 3. computing indicators and save into DB
    *
    * @param kbar kbar is only based on 10S bars
    */
   public void handle(HistoryDataKBar kbar) {
     if (kbar.getTimeWindowType() == TimeWindowType.S10) {
       List<TimeWindowType> timeWindowTypeList = Arrays.asList(TimeWindowType.values());
       List<IndicatorType> indicatorTypeList = Arrays.asList(IndicatorType.values());
       long barStartTime = kbar.getOpenTime();
       long barEndTime = kbar.getOpenTime() + kbar.getTimeWindowType().getTimeInMillis();
 
       // 1 save 10s bar into DB
       save10SBarIntoDB(kbar);
       // 2 generate bars in all timewindowTypes.
       generateAndSaveBarsInAllTimewindowTypes(timeWindowTypeList, barEndTime);
       // 3. computing indicators and save into DB
       generateIndicatorsForAllBars(indicatorTypeList, timeWindowTypeList, barEndTime);
     }
   }
 
   public void handle(List<HistoryDataKBar> kbars) {
     //if (kbar.getTimeWindowType() == TimeWindowType.S10) {
     try {
      // determine if the first record is alread existed.
      if (kbars != null) {
        HistoryDataKBar firstBar = kbars.get(0);
        if (historyDataKBarDao.find(firstBar.getOpenTime(), firstBar.getInstrument(), firstBar.getTimeWindowType()) != null) {
          logger.info("find a duplicated record. Remove it from batch list");
          kbars.remove(firstBar);
        }
      }

       historyDataKBarDao.batchSave(kbars);
       logger.debug("Saved Kbars size: " + kbars.size());
     } catch (Exception e) {
       logger.error("", e);
 
       logger.info("there might be already one duplicated record in batch mode, now insert one by one:");
       // when exception occurs, save one by one
      for (HistoryDataKBar bar : kbars) {
         handle(bar);
       }
     }
     //}
   }
 
   /**
    * generate indicators in indicatorTypeList for bars starting from barStartTime in timeWindowTypeList
    *
    * @param indicatorTypeList  indicator type list
    * @param timeWindowTypeList bars in time window type list
    * @param barEndTime         the indicators which are to be generated are should be associated with the bars end with the barEndTime
    */
   private void generateIndicatorsForAllBars(List<IndicatorType> indicatorTypeList, List<TimeWindowType> timeWindowTypeList, long barEndTime) {
 
   }
 
   /**
    * generate bars starting from barStartTime in timeWindowTypeList
    *
    * @param timeWindowTypeList timeWindowTypeList
    * @param barEndTime         end with the barEndTime
    */
   private void generateAndSaveBarsInAllTimewindowTypes(List<TimeWindowType> timeWindowTypeList, long barEndTime) {
 
 
   }
 
   /**
    * convert MarketDataMessage to HistoryDataKBar and save into db
    *
    * @param kbar kbar
    */
   private void save10SBarIntoDB(HistoryDataKBar kbar) {
     //todo cmeng -- consider if there is really a update case
     save(kbar);
 
   }
 
   /**
    * save kbar into db
    *
    * @param kbar kbar
    */
   private void save(HistoryDataKBar kbar) {
     //if(historyDataKBarDao.find(kbar.getOpenTime(), kbar.getInstrument(), kbar.getTimeWindowType()) == null){
     try {
       historyDataKBarDao.save(kbar);
       logger.debug("Saved a Kbar: " + kbar.toString());
     } catch (Exception e) {
       logger.error("there might be already one duplicated record", e);
     }
 
   }
 
   /**
    * Now we have only limited bars and indicators. In the future, we can add more indicators and bars.
    * So we may need to rebuild all Bars and indicators
    */
   public void rebuildAllBarsAndIndicators() {
     //to do cmeng
   }
 
   public HistoryDataKBar getLatestBarForPeriod(Instrument instrument, TimeWindowType timeWindowType) {
     return historyDataKBarDao.getLatestBarForPeriod(instrument, timeWindowType);
   }
 
 
 }
