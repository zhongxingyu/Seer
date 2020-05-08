 package org.investovator.dataplaybackengine.scheduler;
 
 import org.investovator.core.data.api.CompanyStockTransactionsData;
 import org.investovator.core.data.api.utils.StockTradingData;
 import org.investovator.core.data.api.utils.TradingDataAttribute;
 import org.investovator.core.data.exeptions.DataAccessException;
 import org.investovator.core.data.exeptions.DataNotFoundException;
 import org.investovator.dataplaybackengine.events.*;
 import org.investovator.dataplaybackengine.exceptions.GameFinishedException;
 import org.investovator.dataplaybackengine.utils.DateUtils;
 
 import java.text.ParseException;
 import java.util.*;
 
 /**
  * @author: ishan
  * @version: ${Revision}
  */
 public class RealTimeEventTask extends TimerTask {
 
     //used to determine the cache size
     public static int CACHE_SIZE = 100;
 
     //start time
     private Date currentTime;
     //Data interface
     private CompanyStockTransactionsData dataApi;
 
     //to cache the stock trading data items. (will allow events to have the duplicate times)
     private PriorityQueue<StockUpdateEvent> dataCache;
 
     //to store the stocks to watch
     private ArrayList<String> stocks;
 
     private EventManager eventManager;
 
     //attributes to fetch
     ArrayList<TradingDataAttribute> attributes;
 
     private RealTimeEventTask(String[] stocksToWatch, CompanyStockTransactionsData api,
                               ArrayList<TradingDataAttribute> attributes) {
         this.dataApi = api;
         eventManager = new EventManager();
         this.attributes = attributes;
 
 
         Comparator<StockUpdateEvent> comparator = new StockEventComparator();
         dataCache = new PriorityQueue<StockUpdateEvent>(RealTimeEventTask.CACHE_SIZE, comparator);
         stocks = new ArrayList<String>(Arrays.asList(stocksToWatch));
 
     }
 
     public RealTimeEventTask(String[] stocksToWatch, String startT, String dateFormat, CompanyStockTransactionsData api,
                              ArrayList<TradingDataAttribute> attributes) {
         this(stocksToWatch, api, attributes);
 
         try {
             currentTime = DateUtils.dateStringToDateObject(startT, dateFormat);
         } catch (ParseException e) {
             e.printStackTrace();
         }
 
     }
 
     public RealTimeEventTask(String[] stocksToWatch, Date startT, CompanyStockTransactionsData api,
                              ArrayList<TradingDataAttribute> attributes) {
         this(stocksToWatch, api, attributes);
 
         currentTime = startT;
 
     }
 
     @Override
     public void run() {
 
         boolean inCache = fireEvents();
         //if no events were fired
         //TODO - this part would get called even if there was actually no stock events in the dataset in that time
         if (!inCache) {
             try {
                 refreshCache();
             } catch (GameFinishedException e) {
                 //notify the player
                 eventManager.notifyListeners(new PlaybackFinishedEvent());
                 //stop the timer
                 this.cancel();
             }
             fireEvents();
         }
         // in order to point to the next time interval
         currentTime = DateUtils.incrementTimeBySeconds(1, currentTime);
 
     }
 
     public void setObserver(PlaybackEventListener observer) {
         eventManager.addObserver(observer);
     }
 
 
     private void refreshCache() throws GameFinishedException {
 
         //to track whether at least a single stock has data in the future
         boolean hasData = false;
 
         //for each stock, search for events
         for (String stock : stocks) {
 
             try {
                 //todo- Assumed that the maximum resolution of "time" for the data in the data base is 1 second
                 StockTradingData data = dataApi.getTradingData(CompanyStockTransactionsData.DataType.TICKER,
                        stock, currentTime, null, RealTimeEventTask.CACHE_SIZE, attributes);
 
                 HashMap<Date, HashMap<TradingDataAttribute, String>> stockData = data.getTradingData();
 
                 //add each event to the cache
                 for (Date time : stockData.keySet()) {
                     StockUpdateEvent event = new StockUpdateEvent(stock, stockData.get(time), time);
                    dataCache.add(event);
                 }
 
                 hasData = true;
 
             } catch (DataNotFoundException e) {
                 hasData = false;
             } catch (DataAccessException e) {
                 hasData = false;
             }
 
 
         }
         //if none of the stocks has data in the future
         if (!hasData) {
             throw new GameFinishedException("Ticker");
         }
 
     }
 
     private boolean fireEvents() {
 
         boolean inCache = false;
 
         //while there are elements in the cache
         while (dataCache.size() > 0) {
             //non-destructively get the head of the queue
             StockUpdateEvent event = dataCache.peek();
             //if the event has occurred in the past
             if (event.getTime().before(currentTime)) {
                 //remove that item from the queue
                 dataCache.poll();
                 //fire the event
                 eventManager.notifyListeners(event);
                 inCache = true;
             }
             //if the rest of the events are events of the future
             else {
                 //do not loop anymore
                 break;
             }
 
         }
 
         return inCache;
     }
 
     public ArrayList<String> getStocks() {
         return stocks;
     }
 
     public void setDataApi(CompanyStockTransactionsData dataApi) {
         this.dataApi = dataApi;
     }
 
     public Date getCurrentTime() {
         return currentTime;
     }
 }
