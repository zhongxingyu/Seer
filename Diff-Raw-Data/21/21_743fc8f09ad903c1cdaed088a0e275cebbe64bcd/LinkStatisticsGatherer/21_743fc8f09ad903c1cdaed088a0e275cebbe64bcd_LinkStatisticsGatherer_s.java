 package org.jims.modules.crossbow.link;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import org.apache.log4j.Logger;
 import org.jims.modules.crossbow.enums.LinkStatisticTimePeriod;
 import org.jims.modules.crossbow.enums.LinkStatistics;
 import org.jims.modules.crossbow.exception.LinkException;
 import org.jims.modules.crossbow.lib.LinkHelper;
 
 /**
  * Thread gathering statistics
  *
  * @author robert boczek
  */
 public class LinkStatisticsGatherer {
 
     private LinkHelper linkHelper;
     private final String linkName;
     private static final Logger logger = Logger.getLogger(LinkStatisticsGatherer.class);
 
     public LinkStatisticsGatherer(String linkName) {
         this.linkName = linkName;
     }
 
     void setLinkHelper(LinkHelper linkHelper) {
         this.linkHelper = linkHelper;
     }
     private Thread minuteThread, fiveMinuteThread, hourThread, dayThread;
     private final LinkedList<Map<LinkStatistics, Long>> minuteValueList = new LinkedList<Map<LinkStatistics, Long>>();
     private final LinkedList<Map<LinkStatistics, Long>> fiveMinutesValueList = new LinkedList<Map<LinkStatistics, Long>>();
     private final LinkedList<Map<LinkStatistics, Long>> hourValueList = new LinkedList<Map<LinkStatistics, Long>>();
     private final LinkedList<Map<LinkStatistics, Long>> dayValueList = new LinkedList<Map<LinkStatistics, Long>>();
 
     private Map<LinkStatistics, Long> getEmtpyMap() {
         Map<LinkStatistics, Long> map = new HashMap<LinkStatistics, Long>();
         for (LinkStatistics linkStatistics : LinkStatistics.values()) {
             map.put(linkStatistics, 0L);
         }
         return map;
     }
 
     private void initContent() {
         
         Map<LinkStatistics, Long> map = getEmtpyMap();
 
         final LinkHelper helper = linkHelper;
 
         for (int i = 0; i < 10; i++) {
             minuteValueList.add(map);
             fiveMinutesValueList.add(map);
             hourValueList.add(map);
             dayValueList.add(map);
         }
 
         minuteThread = new Thread() {
 
             @Override
             public void run() {
                 try {
                     while (true) {
                         updateStatistics(minuteValueList, helper);
                        logger.info("Minute statistics for etherstub " + linkName + " updated");
                         Thread.sleep(6000);
 
                     }
                 } catch (Exception e) {
                     logger.error(e);
                 }
 
             }
         };//zawiera 10 wartosci
         minuteThread.start();
 
         fiveMinuteThread = new Thread() {
 
             @Override
             public void run() {
                 try {
                     while (true) {
                         updateStatistics(fiveMinutesValueList, helper);
                        logger.info("Five-minute statistics for etherstub " + linkName + " updated");
                         Thread.sleep(30000);
 
                     }
                 } catch (Exception e) {
                     logger.error(e);
                 }
 
             }
         };//zawiera 10 wartosci
         fiveMinuteThread.start();
 
         hourThread = new Thread() {
 
             @Override
             public void run() {
                 try {
                     while (true) {
                         updateStatistics(hourValueList, helper);
                        logger.info("Hourly statistics for etherstub " + linkName + " updated");
                         Thread.sleep(360000);
 
                     }
                 } catch (Exception e) {
                     logger.error(e);
                 }
 
             }
         };//zawiera 10 wartosci
         hourThread.start();
 
         dayThread = new Thread() {
 
             @Override
             public void run() {
                 try {
                     while (true) {
                         updateStatistics(dayValueList, helper);
                        logger.info("Daily statistics for etherstub " + linkName + " updated");
                         Thread.sleep(8640000);
 
                     }
                 } catch (Exception e) {
                     logger.error(e);
                 }
 
             }
         };//zawiera 10 wartosci
         dayThread.start();
     }
 
     private void updateStatistics(LinkedList<Map<LinkStatistics, Long>> valueList, final LinkHelper helper) {
 
         Map<LinkStatistics, Long> map = null;
         if (linkHelper != null) {
             map = new HashMap<LinkStatistics, Long>();
             for (LinkStatistics linkStatistics : LinkStatistics.values()) {
                 try {
                     map.put(linkStatistics, Long.valueOf(helper.getLinkStatistic(linkName, linkStatistics)));
 
                 } catch (LinkException ex) {
                     logger.error("Couldn't read etherstubs statistic", ex);
                     break;
                 } catch (Exception ex2) {
                     logger.error("Link Statistic Exception", ex2);
                 }
 
             }
 
         } else {
            logger.info("Couldn't read statistics as linkHelper was null");
             map = getEmtpyMap();
         }
         valueList.removeFirst();
         valueList.addLast(map);
 
     }
 
     public void start() {
         initContent();
     }
 
     public void stop() {
         
         interrupt(minuteThread);
         interrupt(fiveMinuteThread);
         interrupt(hourThread);
         interrupt(dayThread);
     }
 
     public List<Map<LinkStatistics, Long>> getStatistics(LinkStatisticTimePeriod period) {
 
         List<Map<LinkStatistics, Long>> map = null;
         if (LinkStatisticTimePeriod.DAILY.equals(period)) {
             map = dayValueList;
         } else if (LinkStatisticTimePeriod.HOURLY.equals(period)) {
             map = hourValueList;
         } else if (LinkStatisticTimePeriod.FIVE_MINUTELY.equals(period)) {
             map = fiveMinutesValueList;
         } else if (LinkStatisticTimePeriod.MINUTELY.equals(period)) {
             map = minuteValueList;
         }
 
         return map;
     }
 
     private void interrupt(Thread thread) {
         if(thread != null && thread.isAlive()) {
             thread.interrupt();
         }
     }
 }
 
