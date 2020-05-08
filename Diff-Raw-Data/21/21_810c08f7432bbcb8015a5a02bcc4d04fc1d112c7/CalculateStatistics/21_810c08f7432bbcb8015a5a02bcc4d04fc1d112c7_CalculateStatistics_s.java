 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Server.AnalyticsServer;
 
 import Events.AuctionEvent;
 import Events.BidEvent;
 import Events.Event;
 import Events.StatisticsEvent;
 import Events.UserEvent;
 import java.rmi.RemoteException;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  *
  * @author daniela
  */
 public class CalculateStatistics implements Runnable {
 
     private AnalyticsServer server;
     private Event event;
     //<ID, Timestamp>
     private static HashMap<Long, Long> auctionList = new HashMap<Long, Long>();
     private static HashMap<Long, Long> successfulAuctionList = new HashMap<Long, Long>();
     //<UserName, Timestamp>
     private static HashMap<String, Long> userList = new HashMap<String, Long>();
     private static long serverStarttime;
     private static int userCounter = 0, auctionCounter = 0, bidCount = 0, successfulAuctions = 0, endedAuctions = 0;
     private static long userSessiontimeSum = 0, auctionTimeSum = 0;
     private static long sessiontimeMin = Long.MAX_VALUE, sessiontimeMax = 0, sessiontimeAvg = 0;
     private static double bidPriceMax = 0, bidCountPerMinute = 0;
 
     public CalculateStatistics(AnalyticsServer server) {
         this.server = server;
         serverStarttime = server.getStarttime();
     }
 
     public void calculate(Event event) {
         this.event = event;
         //System.out.println("calcevent vorhanden " + event != null);
         run();
 /*        CalculateStatistics task = new CalculateStatistics(server);
         task.setEvent(event);
         server.addTask(task);
         */
     }
     
     public void setEvent(Event event) {
         this.event = event;
     }
 
     public void run() {
 
         if (event != null) {
            if (event instanceof AuctionEvent) {
                 AuctionEvent auctionEvent = (AuctionEvent) event;
 
                 if (auctionEvent.getType().equals("AUCTION_STARTED")) {
                     auctionList.put(auctionEvent.getAuctionID(), auctionEvent.getTimestamp());
                     auctionCounter++;
 
                 } else if (auctionEvent.getType().equals("AUCTION_ENDED")) {
                     try {
                         long auctionStarttime = auctionList.get(auctionEvent.getAuctionID());
                         long currentAuctionTime = auctionEvent.getTimestamp() - auctionStarttime;
                         auctionTimeSum += currentAuctionTime;
                         endedAuctions ++;
 
                         long auctionTimeAvg = auctionTimeSum / endedAuctions;
                         server.processEvent(new StatisticsEvent("AUCTION_TIME_AVG", new Date().getTime(), auctionTimeAvg));
                     } catch (RemoteException ex) {
                         Logger.getLogger(CalculateStatistics.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
                 } else {
                     //invalid type
                 }
 
             } else if (event instanceof BidEvent) {
                 BidEvent bidEvent = (BidEvent) event;
 
                 try {
 
                     if (bidEvent.getType().equals("BID_PLACED") || bidEvent.getType().equals("BID_OVERBID")) {
                         bidCount++;
                         long currentTime = new Date().getTime();
                         bidCountPerMinute = (currentTime - serverStarttime) / 1000 / 60;
                         server.processEvent(new StatisticsEvent("BID_COUNT_PER_MINUTE", new Date().getTime(), bidCountPerMinute));
 
                         double currentBidPrice = bidEvent.getPrice();
                         if (currentBidPrice > bidPriceMax) {
                             bidPriceMax = currentBidPrice;
                             server.processEvent(new StatisticsEvent("BID_PRICE_MAX", new Date().getTime(), bidPriceMax));
                         }
 
                         if (!successfulAuctionList.containsKey(bidEvent.getAuctionID())) {
                             successfulAuctions++;
                             successfulAuctionList.put(bidEvent.getAuctionID(), bidEvent.getAuctionID());
                             double auctionSuccessRatio = ((double) successfulAuctions) / ((double) auctionCounter);
                             server.processEvent(new StatisticsEvent("AUCTION_SUCCESS_RATIO", new Date().getTime(), auctionSuccessRatio));
                         }
 
                         //} else if (bidEvent.getType().equals("BID_WON")) { //no statistic to calculate
                     } else {
                         //invalid type
                     }
                 } catch (RemoteException ex) {
                     Logger.getLogger(CalculateStatistics.class.getName()).log(Level.SEVERE, null, ex);
                 }
 
 
 
             } else if (event instanceof UserEvent) {
                 UserEvent userEvent = (UserEvent) event;
 
                 if (userEvent.getType().equals("USER_LOGIN")) {
                     userList.put(userEvent.getUserName(), userEvent.getTimestamp());
 
                 } else if (userEvent.getType().equals("USER_LOGOUT") || userEvent.getType().equals("USER_DISCONNECTED")) {
 
                     System.out.println("user " + userEvent.getUserName() +" logout - login timestamp " + userList.get(userEvent.getUserName()));
                     long currentSessiontime = userEvent.getTimestamp() - userList.get(userEvent.getUserName());
                     userSessiontimeSum += currentSessiontime;
                     userCounter++;
 
                     long newSessiontimeAvg = userSessiontimeSum / userCounter;
 
                     try {
                         if (newSessiontimeAvg != sessiontimeAvg) {
                             sessiontimeAvg = newSessiontimeAvg;
                             server.processEvent(new StatisticsEvent("USER_SESSION_TIME_AVG", new Date().getTime(), sessiontimeAvg));
                         }
 
                         if (currentSessiontime < sessiontimeMin) {
                             sessiontimeMin = currentSessiontime;
                             server.processEvent(new StatisticsEvent("USER_SESSION_TIME_MIN", new Date().getTime(), sessiontimeMin));
                         }
 
                         if (currentSessiontime > sessiontimeMax) {
                             sessiontimeMax = currentSessiontime;
                             server.processEvent(new StatisticsEvent("USER_SESSION_TIME_MAX", new Date().getTime(), sessiontimeMax));
                         }
                     } catch (RemoteException ex) {
                         Logger.getLogger(CalculateStatistics.class.getName()).log(Level.SEVERE, null, ex);
                     }
 
 
                     //} else if (userEvent.getType().equals("USER_DISCONNECTED")) {
                 } else {
                     //invalid type
                 }
 
             } else {
                 //e.g. StatisticsEvent - do nothing
             }
         }
 
 
     }
 
 }
