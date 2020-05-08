 /**
  * 
  */
 
 package com.zygon.trade.execution.exchange.simulation;
 
 import com.xeiam.xchange.dto.Order;
 import com.xeiam.xchange.dto.account.AccountInfo;
 import com.xeiam.xchange.dto.marketdata.OrderBook;
 import com.xeiam.xchange.dto.trade.LimitOrder;
 import com.xeiam.xchange.dto.trade.MarketOrder;
 import com.xeiam.xchange.dto.trade.Wallet;
 import com.zygon.data.Context;
import com.zygon.data.EventFeed;
 import com.zygon.data.Handler;
 import com.zygon.trade.execution.MarketConditions;
 import com.zygon.trade.execution.OrderBookProvider;
 import com.zygon.trade.execution.OrderProvider;
 import com.zygon.trade.execution.TradeExecutor;
 import com.zygon.trade.execution.AccountController;
 import com.zygon.trade.execution.ExchangeException;
 import com.zygon.trade.execution.exchange.AccoutWalletUpdate;
 import com.zygon.trade.execution.exchange.Exchange;
 import com.zygon.trade.execution.exchange.ExchangeEvent;
 import com.zygon.trade.execution.exchange.TickerEvent;
 import com.zygon.trade.execution.exchange.TradeCancelEvent;
 import com.zygon.trade.execution.exchange.TradeFillEvent;
 import com.zygon.trade.market.data.Ticker;
 import com.zygon.trade.market.data.mtgox.MtGoxFeed;
 import java.math.BigDecimal;
 import java.math.RoundingMode;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.concurrent.ArrayBlockingQueue;
 import org.joda.money.BigMoney;
 import org.joda.money.CurrencyUnit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * TODO: send account update messages to the broker so it can make volume
  * analysis.
  *
  * @author zygon
  */
 public class SimulationExchange extends Exchange implements Handler<Ticker> {
     
     public static SimulationExchange createInstance() {
         return new SimulationExchange("joe", 
                 new Wallet[]{
                     new Wallet("USD", BigMoney.of(CurrencyUnit.USD, 1000.0)),
                     new Wallet("BTC", BigMoney.of(CurrencyUnit.of("BTC"), 10.0))
                 }, 
                 new MarketConditions("mtgox"));
     }
     
     private static final double EXCHANGE_FEE = 0.006; // percentage
 
     private static class WalletInfo {
         private double ammount = 0.0;
         private final String currency;
         private double high = 0.0;
         private double low = 0.0;
 
         public WalletInfo(Wallet wallet) {
             this.currency = wallet.getCurrency();
             this.ammount = wallet.getBalance().getAmount().doubleValue();
             this.high = this.ammount;
             this.low = this.ammount;
         }
 
         public double getAmmount() {
             return ammount;
         }
 
         public String getCurrency() {
             return currency;
         }
 
         public double getHigh() {
             return high;
         }
 
         public double getLow() {
             return low;
         }
         
         private Wallet getWallet() {
             return new Wallet(this.currency, BigMoney.of(CurrencyUnit.getInstance(this.currency), this.ammount));
         }
     }
     
     public static final class SimulationAccountController implements AccountController {
 
         private ArrayBlockingQueue<ExchangeEvent> exchangeEvents;
         private Map<CurrencyUnit, WalletInfo> walletsByCurrency = new HashMap<>();
         private final String user;
         private double fees = 0.0;
         
         public SimulationAccountController(String user, Wallet ...wallets) {
             this.user = user;
             
             for (Wallet wallet : wallets) {
                 this.walletsByCurrency.put(CurrencyUnit.getInstance(wallet.getCurrency()), new WalletInfo(wallet));
             }
         }
         
         public synchronized void addFee(BigDecimal fee) {
             this.fees += fee.doubleValue();
         }
         
         public synchronized void add(BigDecimal ammount, CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             
             wallet.ammount += ammount.doubleValue();
             wallet.high = Math.max(wallet.getHigh(), wallet.getAmmount());
             
             this.exchangeEvents.add(new AccoutWalletUpdate(user, wallet.getWallet()));
         }
         
         public synchronized void subtract(BigDecimal ammount, CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             
             wallet.ammount -= ammount.doubleValue();
             wallet.low = Math.min(wallet.getLow(), wallet.getAmmount());
             
             this.exchangeEvents.add(new AccoutWalletUpdate(user, wallet.getWallet()));
         }
         
         @Override
         public AccountInfo getAccountInfo(String username) {
             List<Wallet> wallets = new ArrayList<>();
             for (WalletInfo info : this.walletsByCurrency.values()) {
                 wallets.add(info.getWallet());
             }
             
             return new AccountInfo(this.user, wallets);
         }
 
         public double getBalance(CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             return wallet.getAmmount();
         }
 
         public Set<CurrencyUnit> getCurrencies() {
             return this.walletsByCurrency.keySet();
         }
         
         public double getFees() {
             return this.fees;
         }
 
         @Override
         public double getMaximumDrawDown(String username, CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             return wallet.getHigh() - wallet.getLow();
         }
         
         @Override
         public double getHigh(String username, CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             return wallet.getHigh();
         }
 
         @Override
         public double getLow(String username, CurrencyUnit currency) {
             WalletInfo wallet = this.walletsByCurrency.get(currency);
             return wallet.getLow();
         }
         
         public void setExchangeEvents(ArrayBlockingQueue<ExchangeEvent> exchangeEvents) {
             this.exchangeEvents = exchangeEvents;
         }
     }
     
     public static final class SimulationOrderBookProvider implements OrderBookProvider {
 
         private final List<LimitOrder> orders = new ArrayList<>();
         
         @Override
         public void getOpenOrders(List<LimitOrder> orders) {
             orders.addAll(this.orders);
         }
 
         @Override
         public void getOrderBook(String username, OrderBook orders, String tradeableIdentifer, String currency) {
             
         }
     }
     
     public static final class SimulationOrderProvider implements OrderProvider {
 
         @Override
         public LimitOrder getLimitOrder(String id, Order.OrderType type, double tradableAmount, String tradableIdentifier, String transactionCurrency, double limitPrice) {
             return new LimitOrder(type, BigDecimal.valueOf(tradableAmount), tradableIdentifier, transactionCurrency, id, new Date(), BigMoney.of(CurrencyUnit.USD, limitPrice));
         }
 
         @Override
         public MarketOrder getMarketOrder(String id, Order.OrderType type, double tradableAmount, String tradableIdentifier, String transactionCurrency) {
             return new MarketOrder(type, BigDecimal.valueOf(tradableAmount), tradableIdentifier, transactionCurrency, id, new Date());
         }
     }
     
     public static final class SimulationTradeExecutor implements TradeExecutor {
 
         private final Logger log = LoggerFactory.getLogger(SimulationTradeExecutor.class);
         
         private ArrayBlockingQueue<ExchangeEvent> exchangeEvents;
         private SimulationAccountController accntController;
         private BigDecimal price = null;
         
         @Override
         public void cancel(String username, String orderId) {
             this.log.info("Cancelling orderId: {}", orderId);
             this.exchangeEvents.add(new TradeCancelEvent(orderId, "unknown reason"));
         }
 
         @Override
         public String execute(String accountId, Order order) {
             BigDecimal marketPrice = this.price;
             
             this.log.info("Executing order: {} at price {}", order, marketPrice);
             
             // Because this is a market order we're just estimating what the market price might be.
             BigDecimal totalCost = order.getTradableAmount().multiply(marketPrice);
             
             BigDecimal fee = totalCost.multiply(BigDecimal.valueOf(EXCHANGE_FEE));
             this.accntController.addFee(fee);
             
             totalCost = totalCost.subtract(fee);
             
             // simulate a buy by adding the tradable and subtracting the transaction currency
             if (order.getType() == Order.OrderType.BID) {
                 this.accntController.add(order.getTradableAmount(), CurrencyUnit.of(order.getTradableIdentifier()));
                 this.accntController.subtract(totalCost, CurrencyUnit.of(order.getTransactionCurrency()));
             } else {
                 // simulate a sell by subtracting the tradable and adding the transaction currency
                 this.accntController.subtract(order.getTradableAmount(), CurrencyUnit.of(order.getTradableIdentifier()));
                 this.accntController.add(totalCost, CurrencyUnit.of(order.getTransactionCurrency()));
             }
             
             this.exchangeEvents.add(new TradeFillEvent(order.getId(), TradeFillEvent.Fill.FULL, marketPrice.doubleValue(), order.getTradableAmount().doubleValue()));
             
             for (CurrencyUnit unit : this.accntController.getCurrencies()) {
                 this.log.info("Account balance {}, high {}, low {}, max drawdown {}", 
                         this.accntController.getBalance(unit), 
                         this.accntController.getHigh(accountId, unit), 
                         this.accntController.getLow(accountId, unit),
                         this.accntController.getMaximumDrawDown(accountId, unit));
             }
             this.log.info("Total fees {}", this.accntController.getFees());
             
             return "orderid:"+order.getId();
         }
 
         public void setExchangeEvents(ArrayBlockingQueue<ExchangeEvent> exchangeEvents) {
             this.exchangeEvents = exchangeEvents;
         }
         
         public void setAccntController(SimulationAccountController accntController) {
             this.accntController = accntController;
         }
         
         private void setPrice(BigDecimal price) {
             this.price = price;
         }
     }
     
     private final Properties properties = new Properties();
     
     {
         properties.setProperty("class", "com.zygon.trade.market.data.mtgox.MtGoxFeed");
         properties.setProperty("tradeable", "BTC");
         properties.setProperty("currency", "USD");
     }
     
     private final MtGoxFeed tickerFeed = new MtGoxFeed(new Context(this.properties));
     
     private final ArrayBlockingQueue<ExchangeEvent> exchangeEvents = new ArrayBlockingQueue<ExchangeEvent>(100);
     
     public SimulationExchange(String username, Wallet[] wallets, MarketConditions marketConditions) {
         super(new SimulationAccountController(username, wallets),
               new SimulationOrderBookProvider(),
               new SimulationOrderProvider(),
               new SimulationTradeExecutor());
         
          SimulationTradeExecutor simTradeExecutor = (SimulationTradeExecutor) this.getTradeExecutor();
          simTradeExecutor.setAccntController((SimulationAccountController)this.getAccountController());
          simTradeExecutor.setExchangeEvents(this.exchangeEvents);
          
          SimulationAccountController simAccountController = (SimulationAccountController) this.getAccountController();
          simAccountController.setExchangeEvents(this.exchangeEvents);
          
          this.tickerFeed.register(this);
     }
     
     @Override
     protected ExchangeEvent getEvent() throws ExchangeException {
         try { 
             return this.exchangeEvents.take(); 
         } catch (InterruptedException ignore) {
             // ignore
         }
         return null;
     }
     
     @Override
     public void handle(Ticker r) {
         SimulationTradeExecutor tradeExecutor = (SimulationTradeExecutor) this.getTradeExecutor();
         
         tradeExecutor.setPrice(r.getAsk().plus(r.getBid()).dividedBy(2, RoundingMode.UP).getAmount());
         
         try { this.exchangeEvents.put(new TickerEvent(r)); } catch (InterruptedException ignore) {}
     }
 }
