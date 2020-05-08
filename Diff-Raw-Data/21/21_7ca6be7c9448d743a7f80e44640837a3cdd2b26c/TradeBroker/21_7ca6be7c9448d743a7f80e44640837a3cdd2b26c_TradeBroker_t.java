 
 package com.zygon.trade.trade;
 
 import com.xeiam.xchange.dto.account.AccountInfo;
 import com.xeiam.xchange.dto.trade.MarketOrder;
 import com.zygon.trade.execution.ExchangeException;
 import com.zygon.trade.execution.exchange.Exchange;
 import com.zygon.trade.execution.exchange.ExchangeEvent;
 import com.zygon.trade.execution.exchange.ExchangeEventListener;
 import com.zygon.trade.execution.exchange.TickerEvent;
 import com.zygon.trade.execution.exchange.TradeFillEvent;
 import com.zygon.trade.market.data.Ticker;
 import com.zygon.trade.market.util.TickerUtil;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import org.joda.money.BigMoney;
 import org.joda.money.CurrencyUnit;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The purpose of this element is to take Trades from an agent/strategy
  * and marshal them into individual execution actions.  Also, monitoring
  * orders which have stop losses associated but the underlying exchange
  * does not support them.
  * 
  * TODO: volume strategy
  *
  * @author zygon
  */
 public class TradeBroker implements ExchangeEventListener {
 
     // TBD: might be nice to formalize this little thing.. add setters, etc
     public static class TradeSummary {
         private int totalOrdersExecuted = 0;
         private int totalTradeCount = 0;
         private int cancelledTradeCount = 0;
 
         public int getCancelledTradeCount() {
             return this.cancelledTradeCount;
         }
 
         public int getTotalTradeCount() {
             return this.totalTradeCount;
         }
     }
     
     // A nasty coarse grain lock
     private final Object tradeLock = new Object();
     private final ArrayList<TradePostMortem> finishedTrades = new ArrayList<TradePostMortem>();
     private final Logger log;
     private final Map<String, TradeMonitor> tradeSignalByTradeId = new HashMap<String, TradeMonitor>();
     private final String accountId;
     private final Exchange exchange;
     private final TradeSummary tradeSummary = new TradeSummary();
     
     private Ticker ticker = null;
     
     public TradeBroker(String accountId, Exchange exchange) {
         this.log = LoggerFactory.getLogger(TradeBroker.class);
         this.accountId = accountId;
         this.exchange = exchange;
         
         this.exchange.setListener(this);
     }
 
     // These are method to encode/decode into an order key.  When an order
     // key comes back to us for a fill, etc. we can find the original trade.
     // I'm not always a huge fan of this kind of string manipulation but 
     // it'll do for now.
     private static String generateOrderKey (String tradeId, String orderId) {
         return String.format("%s|%s", tradeId, orderId);
     }
     private static String generateOrderKey (int tradeId, int orderId) {
         return String.format("%d|%d", tradeId, orderId);
     }
     private static String getTradeId(String orderKey) {
         return orderKey.split("\\|")[0];
     }
     private static String getOrderId(String orderKey) {
         return orderKey.split("\\|")[1];
     }
     // TODO: persistant trade id - if the broker goes down and comes back up
     // with active orders then things are all fucked up.
     private static int tradeID = 0;
     private static int orderID = 0;
     
     public synchronized void activate(Trade trade) throws ExchangeException {
         
         // TBD: for now we just want one trade at a time.. we need a better
         // set of restrictions
         if (!this.tradeSignalByTradeId.isEmpty()) {
             return;
         }
         
         if (this.ticker == null) {
             this.log.trace("Broker is not ready. unable to activate trade: " + trade);
         }
         
         double currentPrice = this.getCurrentPrice();
         
         AccountInfo accountInfo = this.exchange.getAccountController().getAccountInfo(this.accountId);
         
         // TBD: perform real account/risk analsysis
         for (TradeSignal signal : trade.getTradeSignals()) {
             
             BigMoney signalCurrencybalance = accountInfo.getBalance(CurrencyUnit.of(signal.getCurrency()));
            double desiredVolume = signal.getVolumeObjective().getVolume(signalCurrencybalance.getAmount().doubleValue(), currentPrice);
             
             if (desiredVolume > signalCurrencybalance.getAmount().doubleValue()) {
                 
                 this.log.trace("Unable to fund trade: " + trade);
                 return;
             }
         }
         
         this.log.trace("Activating trade: " + trade);
         
         String tradeId = String.valueOf(tradeID);
         
         TradeMonitor monitor = new TradeMonitor();
         monitor.open(tradeId, System.currentTimeMillis(), currentPrice);
         
         for (TradeSignal signal : trade.getTradeSignals()) {
             // TODO: limit order
             // TBD: priority
             
             String orderId = String.valueOf(orderID++);
             
             signal.getPriceObjective().setPrice(currentPrice);
             
             BigMoney signalCurrencybalance = accountInfo.getBalance(CurrencyUnit.of(signal.getCurrency()));
            double desiredVolume = signal.getVolumeObjective().getVolume(signalCurrencybalance.getAmount().doubleValue(), currentPrice);
             
             String orderKey = generateOrderKey(tradeID, orderID);
             MarketOrder order = this.exchange.generateMarketOrder(orderKey, signal.getDecision().getType().getOrderType(), 
                              desiredVolume, signal.getTradeableIdentifier(), signal.getCurrency());
             
             this.exchange.placeOrder(this.accountId, order);
             
             monitor.addSignal(orderId, signal, desiredVolume);
             
             this.tradeSummary.totalOrdersExecuted ++;
             
             orderID ++;
         }
         
         synchronized (this.tradeLock) {
             this.tradeSignalByTradeId.put(tradeId, monitor);
         }
         
         trade.setId(tradeID);
         tradeID ++;
     }
     
     private double calculateClosingProfit (TradeType type, double entryPrice, double currentPrice, double volume) {
         
         double priceMargin = 0.0;
         
         switch (type) {
             case LONG:
                 priceMargin = currentPrice - entryPrice;
                 break;
             case SHORT:
                 priceMargin = entryPrice - currentPrice;
                 break;
         }
         
         double profit = priceMargin * volume;
         return profit;
     }
     
     public void cancelAll() {
         this.log.info("Cancelling all trades");
         // TODO: cancel active trades
     }
     
     public int getActiveTradeCount() {
         return this.tradeSignalByTradeId.size();
     }
     
     public TradeSummary getTradeSummary() {
         return this.tradeSummary;
     }
     
     private double getCurrentPrice() {
         return TickerUtil.getMidPrice(this.ticker);
     }
 
     public Exchange getExchange() {
         return this.exchange;
     }
     
     public void getFinishedTrades(Collection<TradePostMortem> col) {
     
         if (!this.finishedTrades.isEmpty()) {
             synchronized (this.tradeLock) {
                 col.addAll(this.finishedTrades);
                 this.finishedTrades.clear();
             }
         }
     }
     
     @Override
     public void notify(ExchangeEvent event) {
         this.log.trace("Received event: " + event);
         
         synchronized (this.tradeLock) {
             
             switch (event.getEventType()) {
                 case ACCOUNT_STATUS:
                     // TODO: get account balance for volume/risk adjustment
                     break;
                 case TICKER:
                     TickerEvent tickerEvent = (TickerEvent) event;
                     this.ticker = tickerEvent.getTicker();
                     
                     // ticker signifies a price change - we might need to close
                     // out some trades.
                     try {
                         this.processOpenTrades();
                     } catch (ExchangeException ee) {
                         this.log.error(null, ee);
                     }
                     
                     break;
                 case TRADE_CANCEL:
                     
                     throw new UnsupportedOperationException();
                     
 //                    TradeCancelEvent cancelEvent = (TradeCancelEvent) event;
 //                    TradeMonitor cancelledTradeMonitor = this.tradeSignalByTradeId.get(cancelEvent.getOrderId());
 //                    if (cancelledTradeMonitor != null) {
 //                        this.tradeSignalByTradeId.remove(cancelEvent.getOrderId());
 //                        
 //                        cancelledTradeMonitor.setEnd(System.currentTimeMillis());
 //                        
 //                        this.finishedTrades.add(new TradePostMortem(
 //                                new Signal(cancelledTradeMonitor.getSignal(cancelEvent.getOrderId()).getReason()), 
 //                                new Signal(cancelEvent.getReason()), 
 //                                cancelledTradeMonitor.getDuration(), 
 //                                0.0));
 //                        this.tradeSummary.cancelledTradeCount ++;
 //                        this.tradeSummary.totalTradeCount ++;
 //                    }
 //                    break;
                 case TRADE_FILL:
                     TradeFillEvent fillEvent = (TradeFillEvent) event;
                     String tradeId = getTradeId(fillEvent.getOrderID());
                     String orderId = getOrderId(fillEvent.getOrderID());
                     TradeMonitor filledTradeMonitor = this.tradeSignalByTradeId.get(tradeId);
                     
                     if (filledTradeMonitor != null) {
                         if (fillEvent.getFill() == TradeFillEvent.Fill.FULL) {
                             
                             // Tell the monitor that this specific trade signal has been closed
                             filledTradeMonitor.notifyComplete(orderId);
                             
                         } else {
                             // TBD: partial fill - might need to hold onto some
                             // additional meta data during a trade. and update
                             // with fill ammounts.
                         }
                         
                         if (!filledTradeMonitor.hasOpenSignals()) {
                             
                             // close the trade
                             filledTradeMonitor.close(System.currentTimeMillis(), this.getCurrentPrice());
                             
                             this.finishedTrades.add(new TradePostMortem(
                                     new Signal(filledTradeMonitor.getSignal(orderId).getReason()), 
                                     new Signal(ExchangeEvent.EventType.TRADE_FILL.name()), 
                                     filledTradeMonitor.getDuration(), 
                                     filledTradeMonitor.getProfit()));
                             
                             this.tradeSummary.totalTradeCount ++;
                             
                             this.tradeSignalByTradeId.remove(tradeId);
                         }
                     }
                     
                     break;
                 case TRADE_REJECTED:
                     break;
             }
         }
     }
     
     private void processOpenTrades() throws ExchangeException {
         double price = this.getCurrentPrice();
         
         synchronized (this.tradeLock) {
             Iterator<String> iter = this.tradeSignalByTradeId.keySet().iterator();
             while (iter.hasNext()) {
                 
                 String tradeId = iter.next();
                 TradeMonitor trade = this.tradeSignalByTradeId.get(tradeId);
                 Map<String, TradeSignal> tradeSignals = trade.getSignals();
                 
                 // Loop through the open trades and check if any of the signal
                 // can be closed.
                 
                 for (String orderId : tradeSignals.keySet()) {
                     TradeSignal tradeSignal = tradeSignals.get(orderId);
                     
                     PriceObjective priceObjective = tradeSignal.getPriceObjective();
                     Signal exitSignal = this.getExitSignal(tradeSignal, price, priceObjective);
                     
                     if (exitSignal != null) {
                         // We met the exit conditions - close the trade
                         this.log.info("Closing order " + orderId + " of trade " + trade.getTradeId() + " due to " + exitSignal.getName());
                         
                         // TBD: limit order
                         
                         String orderKey = generateOrderKey(tradeId, orderId);
                         MarketOrder closeOrder = this.exchange.generateMarketOrder(orderKey, tradeSignal.getTradeType().getCounterOrderType(), 
                                  trade.getEnterVolume(orderId), tradeSignal.getTradeableIdentifier(), tradeSignal.getCurrency());
 
                         this.exchange.placeOrder(this.accountId, closeOrder);
 
                         this.tradeSummary.totalOrdersExecuted ++;
                     }
                 }
             }
         }
     }
     
     private static final String EXIT_STOP_LOSS = "STOP_LOSS";
     private static final String EXIT_TAKE_PROFIT = "TAKE_PROFIT";
     
     /**
      * Returns a signal symbolizing that the trade should be closed, null otherwise.
      * @param signal
      * @param currentPrice
      * @param priceObjective
      * @return a signal symbolizing that the trade should be closed, null otherwise.
      */
     private Signal getExitSignal(TradeSignal signal, double currentPrice, PriceObjective priceObjective) {
         
         String exitSignal = null;
         
         switch (signal.getTradeType()) {
             case LONG:
                 if (currentPrice <= priceObjective.getStopLoss()) {
                     exitSignal = EXIT_STOP_LOSS;
                 } else if(currentPrice >= priceObjective.getTakeProfit()) {
                     exitSignal = EXIT_TAKE_PROFIT;
                 }
                 break;
             case SHORT:
                 if (currentPrice >= priceObjective.getStopLoss()) {
                     exitSignal = EXIT_STOP_LOSS;
                 } else if (currentPrice <= priceObjective.getTakeProfit()) {
                     exitSignal = EXIT_TAKE_PROFIT;
                 }
                 break;
         }
         
         return exitSignal != null ? new Signal(exitSignal) : null;
     }
 }
