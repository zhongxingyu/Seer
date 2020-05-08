 package com.mengruojun.strategycenter.component.marketdata;
 
 import com.mengruojun.common.dao.HistoryDataKBarDao;
 import com.mengruojun.common.domain.HistoryDataKBar;
 import com.mengruojun.common.domain.Instrument;
 import com.mengruojun.common.domain.OHLC;
 import com.mengruojun.common.domain.TimeWindowType;
 import com.mengruojun.jms.domain.MarketDataMessage;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.SessionFactory;
 import org.junit.Before;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
 
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TimeZone;
 
 import static org.junit.Assert.*;
 import static org.junit.Assert.assertEquals;
 
 @ContextConfiguration(
         locations = {"classpath:/com/mengruojun/strategycenter/app-test.xml"})
 public class MarketDataManagerTest extends AbstractTransactionalJUnit4SpringContextTests {
   Log log = LogFactory.getLog(MarketDataManagerTest.class);
 
   SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
 
   List<HistoryDataKBar> loadedTestBars = new ArrayList<HistoryDataKBar>();
 
   {
     sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
   }
 
   @Autowired
   HistoryDataKBarDao historyDataKBarDao;
   @Autowired
   SessionFactory sessionFactory;
   @Autowired
   MarketDataManager marketDataManager;
 
   List<HistoryDataKBar> expectedKBarList = new ArrayList<HistoryDataKBar>();
 
   @Before
   public void setUp() {
     loadedTestBars = historyDataKBarDao.getBarsByOpenTimeRange(new Instrument("EUR/USD"),
             TimeWindowType.S10, 1267401600000L, 1267421590000L + TimeWindowType.S10.getTimeInMillis());
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.H4, 1267416000000L - TimeWindowType.H4.getTimeInMillis(), 1267416000000L,
             new OHLC(1.364, 1.36515, 1.3592, 1.3613, 71003.70,
                     1.36385, 1.36495, 1.35905, 1.36115, 56812.5)));  //OHLCV
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.H1, 1267416000000L - TimeWindowType.H1.getTimeInMillis(), 1267416000000L,
             new OHLC(1.3605, 1.3617, 1.3601, 1.3613, 11370.2,
                     1.3604, 1.3616, 1.36, 1.36115, 9115.0)));  //OHLCV
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.M30, 1267416000000L - TimeWindowType.M30.getTimeInMillis(), 1267416000000L,
             new OHLC(1.36015, 1.3617, 1.36015, 1.3613, 5958.1,
                     1.36, 1.3616, 1.36, 1.36115, 3861.1)));  //OHLCV
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.M5, 1267416000000L - TimeWindowType.M5.getTimeInMillis(), 1267416000000L,
             new OHLC(1.3613, 1.36145, 1.3611, 1.3613, 1319.6,
                     1.3612, 1.3613, 1.36095, 1.36115, 730.1)));  //OHLCV
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.M1, 1267416000000L - TimeWindowType.M1.getTimeInMillis(), 1267416000000L,
             new OHLC(1.3612, 1.3613, 1.36115, 1.3613, 197.3,
                     1.3611, 1.36115, 1.361, 1.36115, 195.0)));  //OHLCV
 
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.S30, 1267416000000L - TimeWindowType.S30.getTimeInMillis(), 1267416000000L,
             new OHLC(1.3613, 1.3613, 1.36125, 1.3613, 120.5,
                     1.36115, 1.36115, 1.3611, 1.36115, 121.1)));  //OHLCV
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.S20, 1267416000000L - TimeWindowType.S20.getTimeInMillis(), 1267416000000L,
             new OHLC(1.36125, 1.3613, 1.36125, 1.3613, 83.7,
                     1.3611, 1.36115, 1.3611, 1.36115, 62.7)));  //OHLCV
     expectedKBarList.add(new HistoryDataKBar(new Instrument("EUR/USD"), TimeWindowType.S10, 1267416000000L - TimeWindowType.S10.getTimeInMillis(), 1267416000000L,
             new OHLC(1.36125, 1.3613, 1.36125, 1.3613, 53.3,
                     1.3611, 1.36115, 1.3611, 1.36115, 35.4)));  //OHLCV
 
   }
 
   @Test
   public void pushTest() {
     log.info("loadedTestBars size is " + loadedTestBars.size());
     log.info("from:  " + sdf.format(new Date(1267401600000L)));
     log.info("to:  " + sdf.format(new Date(1267421590000L)));
 

     //test H4 bars
     for (HistoryDataKBar historyDataKBar : loadedTestBars) {
       Map<Instrument, MarketDataMessage> s10MdmMap = new HashMap<Instrument, MarketDataMessage>();
       MarketDataMessage mdm = MarketDataMessage.buildFromKBar(historyDataKBar);
       s10MdmMap.put(historyDataKBar.getInstrument(), mdm);
       marketDataManager.push(s10MdmMap);
       for (HistoryDataKBar expectedKBar : expectedKBarList) {
         verifyGeneratedBar(expectedKBar, historyDataKBar);
       }
     }
 
     //
   }
 
   /**
    * verify values, when the expectedKbar has the same closeTime with the current S10Bar
    *
    * @param expectedKBar  expectedKBar
    * @param currentS10Bar currentS10Bar
    */
   private void verifyGeneratedBar(HistoryDataKBar expectedKBar, HistoryDataKBar currentS10Bar) {
     if (currentS10Bar.getCloseTime().equals(expectedKBar.getCloseTime() - TimeWindowType.S10.getTimeInMillis())) {
       // now the calculatedBar is still not finished. So it should be null or last available bar;
       HistoryDataKBar calculatedBar = MarketDataManager.getKBarByEndTime_OnlySearch(expectedKBar.getCloseTime() - TimeWindowType.S10.getTimeInMillis(), currentS10Bar.getInstrument(), expectedKBar.getTimeWindowType());
       if (calculatedBar != null) {
         assertEquals(calculatedBar.getCloseTime() + calculatedBar.getTimeWindowType().getTimeInMillis(), expectedKBar.getCloseTime(), 0);
       }
     }
 
     if (currentS10Bar.getCloseTime().equals(expectedKBar.getCloseTime())) {
       // now the calculatedBar is just finished. So it should be not null;
       HistoryDataKBar calculatedBar = MarketDataManager.getKBarByEndTime_OnlySearch(expectedKBar.getCloseTime(), currentS10Bar.getInstrument(), expectedKBar.getTimeWindowType());
       assertNotNull(calculatedBar);
       log.info(calculatedBar.toString());
       verifyGeneratedBarValue(expectedKBar, calculatedBar);
     }
   }
 
   private void verifyGeneratedBarValue(HistoryDataKBar expectedKBar, HistoryDataKBar calculatedBar) {
     assertEquals(expectedKBar.getOhlc(), calculatedBar.getOhlc());
   }
 
 }
