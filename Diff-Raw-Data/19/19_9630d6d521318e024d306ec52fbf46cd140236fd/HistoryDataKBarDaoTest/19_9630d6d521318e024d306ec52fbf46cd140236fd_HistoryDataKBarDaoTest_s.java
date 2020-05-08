 package com.mengruojun.common.dao;
 
 import com.mengruojun.common.domain.HistoryDataKBar;
 import com.mengruojun.common.domain.Instrument;
 import com.mengruojun.common.domain.OHLC;
 import com.mengruojun.common.domain.TimeWindowType;
 import com.mengruojun.common.domain.enumerate.Currency;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.SessionFactory;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.TimeZone;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 
 public class HistoryDataKBarDaoTest extends BaseDaoTestCase {
   Log log = LogFactory.getLog(HistoryDataKBarDaoTest.class);
 
   SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
   SimpleDateFormat sdf_gmt8 = new SimpleDateFormat("yyyy.MM.dd HH:mm:ss Z");
 
   {
     sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
     logger.info("clientDataReceiver started");
   }
 
   @Autowired
   HistoryDataKBarDao historyDataKBarDao;
   @Autowired
   SessionFactory sessionFactory;
 
   @Before
   public void setUp() {
   }
 
   @Test
   public void getBar() {
     assertFalse(historyDataKBarDao.exists(0L));
   }
 
   @Test
   public void testTimeZone() throws ParseException {
     Date now = new Date();
 
     log.info("For GMT+8, now is :" + sdf_gmt8.format(now));
     log.info("For GMT+0, now is :" + sdf.format(now));
 
    log.info(sdf.format(new Date(1262311200000L)));
     log.info(sdf.parse("2010.01.05 00:00:00 +0000").getTime());
     log.info(sdf.parse("2013.02.22 00:00:00 +0000").getTime());
     log.info(sdf.parse("2010.01.06 00:00:00 +0000").getTime());
   }
 
     @Test
     public void convertLongToData() throws ParseException {
         log.info(sdf.format(new Date(1295447600000L)));
     }
 
 
 
   @Test
   public void save() throws ParseException {
     String startTime = "3000.01.01 00:00:00 +0000";
     String endTime = "3000.01.01 00:00:10 +0000";
     HistoryDataKBar bar = new HistoryDataKBar(new Instrument(Currency.EUR, Currency.USD), TimeWindowType.S10,
             sdf.parse(startTime).getTime(), sdf.parse(endTime).getTime(), new OHLC(2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d));
     bar = historyDataKBarDao.save(bar);
 
     HistoryDataKBar bar2 = historyDataKBarDao.get(bar.getId());
     assertEquals(bar2.getOpenTime(), sdf.parse(startTime).getTime(), 0);
 
     HistoryDataKBar bar3 = historyDataKBarDao.find(sdf.parse(startTime).getTime(), new Instrument(Currency.EUR, Currency.USD), TimeWindowType.S10);
     assertEquals(bar3.getOpenTime(), sdf.parse(startTime).getTime(), 0);
     assertEquals(bar2, bar3);
 
 
      startTime = "3000.01.01 00:00:10 +0000";
      endTime = "3000.01.01 00:00:20 +0000";
     HistoryDataKBar bar_2 = new HistoryDataKBar(new Instrument(Currency.EUR, Currency.USD), TimeWindowType.S10,
             sdf.parse(startTime).getTime(), sdf.parse(endTime).getTime(), new OHLC(2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d, 2d));
     bar_2 = historyDataKBarDao.save(bar_2);
 
     HistoryDataKBar latestBar = historyDataKBarDao.getLatestBarForPeriod(new Instrument(Currency.EUR, Currency.USD), TimeWindowType.S10);
     assertEquals(bar_2, latestBar);
 
 
 
 
 
   }
 }
