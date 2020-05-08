 package edu.uw.danco.broker;
 
 import edu.uw.ext.framework.account.Account;
 import edu.uw.ext.framework.account.AccountException;
 import edu.uw.ext.framework.account.AccountManager;
 import edu.uw.ext.framework.broker.*;
 import edu.uw.ext.framework.exchange.ExchangeEvent;
 import edu.uw.ext.framework.exchange.ExchangeListener;
 import edu.uw.ext.framework.exchange.StockExchange;
 import edu.uw.ext.framework.exchange.StockQuote;
 import edu.uw.ext.framework.order.*;
 
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.Map;
 import java.util.TreeMap;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 /**
  * Created with IntelliJ IDEA.
  * User: dcostinett
  * Date: 4/28/13
  * Time: 2:14 PM
  */
 public class BrokerImpl implements Broker, ExchangeListener {
     /** The logger */
     private static final Logger LOGGER = Logger.getLogger(BrokerImpl.class.getName());
 
     /** The name of this broker instance */
     private String brokerName;
 
     /** The account manager used by this broker */
     private AccountManager acctManager;
 
     /** The stock exchange used by this broker */
     private StockExchange exchange;
 
     /** The collection of OrderManagers */
     Map<String, OrderManager> orderManagers;
 
     /** The Dispatch Filter for market orders */
     private MarketDispatchFilter marketDispatchFilter;
 
     /** The market order queue */
     private OrderQueue<Order> marketOrders;
 
     /** The ExecutorService that will process the dispatched orders */
     final ExecutorService dispatcher;
 
 
     /**
      * Constructor for sub classes
      */
     protected BrokerImpl() {
        dispatcher = Executors.newFixedThreadPool(8);
     }
 
 
     /**
      * Constructor
      * @param brokerName - name of the broker
      * @param acctManager - the account manager used by this broker
      * @param exchange - the stock exchange used by this broker
      */
     public BrokerImpl(final String brokerName, final AccountManager acctManager, final StockExchange exchange) {
         this.brokerName = brokerName;
         this.acctManager = acctManager;
         this.exchange = exchange;
         // each OrderManager has 2 queues, plus we need 2 queues for each market order type,
         // and we need a thread for each queue
        dispatcher = Executors.newFixedThreadPool(8);
 
         final OrderProcessor processor = new StockTraderOrderProcessor(acctManager, exchange);
         marketDispatchFilter = new MarketDispatchFilter(exchange.isOpen());
         marketOrders = new OrderQueueImpl<Order>(marketDispatchFilter, dispatcher);
         marketOrders.setOrderProcessor(processor);
 
         String[] stockTickers = exchange.getTickers();
         orderManagers = new TreeMap<String, OrderManager>();
 
         final OrderProcessor orderProc = new MoveToMarketQueueProcessor(marketOrders);
         for (String stockTicker : stockTickers) {
             StockQuote quote = exchange.getQuote(stockTicker);
             OrderManager orderManager = new OrderManagerImpl(quote.getTicker(), quote.getPrice(), dispatcher);
             orderManager.setOrderProcessor(orderProc);
             orderManagers.put(stockTicker, orderManager);
         }
         // think about making a view of the HashMap an immutable map?
 
         exchange.addExchangeListener(this);     //when adding self as listener, always do it as the last thing.
     }
 
     /**
      * Returns the name of this broker
      * @return - the value for the broker's name
      */
     @Override
     public String getName() {
         return brokerName;
     }
 
 
     /**
      * Sets the broker name.
      *
      * @param name the name to use for the broker
      */
     protected void setName(final String name) {
         this.brokerName = name;
     }
 
 
     /**
      * Create an account with this broker
      * @param username - the username for the account
      * @param password - the value to be used for the password
      * @param balance - the initial balance
      * @return - the account created with the provided values
      * @throws BrokerException
      */
     @Override
     public Account createAccount(String username, String password, int balance) throws BrokerException {
         Account account = null;
         try {
             account = acctManager.createAccount(username, password, balance);
         } catch (final AccountException e) {
             LOGGER.log(Level.SEVERE, "Unable to create account for " + username, e);
             throw new BrokerException(e);
         }
 
         return account;
     }
 
 
     /**
      * Remove the specified account
      * @param username - account name to remove
      * @throws BrokerException
      */
     @Override
     public void deleteAccount(String username) throws BrokerException {
         try {
             acctManager.deleteAccount(username);
         } catch (AccountException e) {
             LOGGER.log(Level.SEVERE, "Unable to delete account " + username, e);
             throw new BrokerException(e);
         }
     }
 
 
     /**
      * Get the account specified by the username
      * @param username - the name of the account to return
      * @param password - the password for the account
      * @return - the account if it is found
      * @throws BrokerException
      */
     @Override
     public Account getAccount(final String username, final String password) throws BrokerException {
         Account account = null;
         try {
             account = acctManager.getAccount(username);
 
             if (account == null || !acctManager.validateLogin(username, password)) {
                 throw new BrokerException("Unable to retrieve requested account: " + username);
             }
         } catch (AccountException e) {
             LOGGER.log(Level.SEVERE, "Unable to retrieve account with name: " + username, e);
             throw new BrokerException(e);
         }
         return account;
     }
 
 
     /**
      * Gets the current market stock quote reflecting trading price
      * @param ticker - the stock symbol
      * @return - a current market quote
      * @throws BrokerException
      */
     @Override
     public StockQuote requestQuote(String ticker) throws BrokerException {
         return exchange.getQuote(ticker);
     }
 
 
     /**
      * Enqueue a buy order
      * @param order - the order for a specific stock and number of shares
      * @throws BrokerException
      */
     @Override
     public void placeOrder(MarketBuyOrder order) throws BrokerException {
         marketOrders.enqueue(order);
     }
 
 
     /**
      * Place a market order
      * @param order - the order to place for a specific stock and number of shares
      * @throws BrokerException
      */
     @Override
     public void placeOrder(MarketSellOrder order) throws BrokerException {
         marketOrders.enqueue(order);
     }
 
 
     /**
      * Place a StopBuy order
      * @param order - the order to place for a specific stock at a specific price
      * @throws BrokerException
      */
     @Override
     public void placeOrder(StopBuyOrder order) throws BrokerException {
         OrderManager manager = orderManagers.get(order.getStockTicker());
         if (manager == null) {
             LOGGER.log(Level.SEVERE, "Unable to retrieve order manager for ticker: " + order.getStockTicker());
         } else {
             manager.queueOrder(order);
         }
     }
 
 
     /**
      * Place a StopSell order
      * @param order - the order to place for a specific stock at a specified price
      * @throws BrokerException
      */
     @Override
     public void placeOrder(StopSellOrder order) throws BrokerException {
         OrderManager manager = orderManagers.get(order.getStockTicker());
         if (manager == null) {
             LOGGER.log(Level.SEVERE, "Unable to retrieve order manager for ticker: " + order.getStockTicker());
         } else {
             manager.queueOrder(order);
         }
     }
 
 
     /**
      * Close the exchange.
      * @throws BrokerException
      */
     @Override
     public void close() throws BrokerException {
         try {
             exchange.removeExchangeListener(this);
             acctManager.close();
             dispatcher.shutdown();
             orderManagers = null;
         } catch (AccountException e) {
             LOGGER.log(Level.WARNING, "Unable to close account", e);
         }
     }
 
 
     /**
      * Event handler for opening the exchange
      * @param event - the exchange opened event
      */
     @Override
     public void exchangeOpened(ExchangeEvent event) {
         LOGGER.info("Exchange opened");
         marketDispatchFilter.setThreshold(Boolean.TRUE);
         marketOrders.dispatchOrders();
     }
 
 
     /**
      * Event handler for closing the exchange
      * @param event -
      */
     @Override
     public void exchangeClosed(ExchangeEvent event) {
         LOGGER.info("Exchange closed");
         marketDispatchFilter.setThreshold(Boolean.FALSE);
     }
 
 
     /**
      * Event handler for a change in price
      * @param event - the change for a specific stock
      */
     @Override
     public void priceChanged(ExchangeEvent event) {
         OrderManager manager = orderManagers.get(event.getTicker());
         if (manager == null) {
             LOGGER.log(Level.SEVERE, "Unable to retrieve order manager for ticker: " + event.getTicker());
         } else {
             manager.adjustPrice(event.getPrice());
         }
     }
 
     /**
      * Sets the account manager.
      * @param accountManager - the account manager
      */
     protected void setAccountManager(final AccountManager accountManager) {
         this.acctManager = accountManager;
     }
 
 
     /**
      * Sets the market order queue's dispatch filter.
      * @param marketDispatchFilter - the market dispatch filter
      */
     protected void setMarketDispatchFilter(final MarketDispatchFilter marketDispatchFilter) {
         this.marketDispatchFilter = marketDispatchFilter;
     }
 
 
     /**
      * Sets the market order queue.
      * @param marketOrders - queue for market orders
      */
     protected void setMarketOrderQueue(final OrderQueue<Order> marketOrders) {
         this.marketOrders = marketOrders;
     }
 
 
     /**
      * Sets the stock exchange.
      * @param stockExchange - the exchange for trading
      */
     protected void setStockExchange(final StockExchange stockExchange) {
         this.exchange = stockExchange;
     }
 
 }
