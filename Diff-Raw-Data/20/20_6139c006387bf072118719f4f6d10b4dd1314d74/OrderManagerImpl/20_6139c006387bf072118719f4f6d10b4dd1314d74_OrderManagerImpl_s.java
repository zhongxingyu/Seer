 package edu.uw.danco.broker;
 
 import edu.uw.ext.framework.broker.OrderDispatchFilter;
 import edu.uw.ext.framework.broker.OrderManager;
 import edu.uw.ext.framework.broker.OrderProcessor;
 import edu.uw.ext.framework.broker.OrderQueue;
 import edu.uw.ext.framework.order.StopBuyOrder;
 import edu.uw.ext.framework.order.StopSellOrder;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dcostinett
  * Date: 4/28/13
  * Time: 2:50 PM
  */
 public class OrderManagerImpl implements OrderManager {
 
     /** The stock ticker symbol being managed by this instance */
     private String symbol;
 
     /** The price of the current stock being managed */
     private int price;
 
     /** The processor that executes orders through the broker. */
     private OrderProcessor processor;
 
 
     /**
      * Constructor to be used by sub classes to finish initialization.
      */
     public OrderManagerImpl() {
     }
 
     /**
      * Constructor
      * @param symbol - the ticker symbol of the stock this instance is manage orders for
      * @param price - the current price of stock to be managed
      */
    public OrderManagerImpl(String symbol, int price) {
         this.symbol = symbol;
         this.price = price;
     }
 
 
     /**
      * Gets the stock ticker symbol for the stock managed by this stock manager
      * @return - the stock ticker symbol
      */
     @Override
     public String getSymbol() {
         return symbol;
     }
 
 
     /**
      * Respond to a stock price adjustment by setting threshold on dispatch filters.
      * @param price - the new price
      */
     @Override
     public void adjustPrice(int price) {
         this.price = price;
     }
 
 
     /**
      * Queue a stop buy order.
      * @param order - the order to queue
      */
     @Override
     public void queueOrder(final StopBuyOrder order) {
 
     }
 
 
     /**
      * Queue a stop sell order.
      * @param order - the order to queue
      */
     @Override
     public void queueOrder(final StopSellOrder order) {
 
     }
 
 
     /**
      * Registers the processor to be used during order processing. This will be passed on to the order queues as the
      * dispatch callback.
      * @param processor - the callback to be registered
      */
     @Override
     public void setOrderProcessor(final OrderProcessor processor) {
 
     }
 
 
     /**
      * Sets the stock ticker symbol
      * @param stockTickerSymbol - the stockTickerSymbol to set
      */
     protected void 	setStockTickerSymbol(final String stockTickerSymbol) {
         symbol = stockTickerSymbol;
     }
 
 
     /**
      *
      * @param stopBuyOrderFilter
      */
     protected void 	setStopBuyOrderFilter(final OrderDispatchFilter<Integer,StopBuyOrder> stopBuyOrderFilter) {
 
     }
 
 
     /**
      *
      * @param stopBuyOrderQueue
      */
     protected void 	setStopBuyOrderQueue(final OrderQueue<StopBuyOrder> stopBuyOrderQueue) {
 
     }
 
 
     /**
      *
      * @param stopSellOrderFilter
      */
     protected void 	setStopSellOrderFilter(final OrderDispatchFilter<Integer, StopSellOrder> stopSellOrderFilter) {
 
     }
 
 
     /**
      *
      * @param stopSellOrderQueue
      */
     protected void 	setStopSellOrderQueue(final OrderQueue<StopSellOrder> stopSellOrderQueue) {
 
     }
 }
