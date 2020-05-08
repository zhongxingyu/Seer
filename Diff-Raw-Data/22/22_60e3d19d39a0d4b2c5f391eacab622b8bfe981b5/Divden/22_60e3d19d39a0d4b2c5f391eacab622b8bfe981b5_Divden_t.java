 package com.sigseg.ib.divden;
 
 import au.com.ds.ef.*;
 import au.com.ds.ef.call.StateHandler;
 import com.ib.client.*;
 import com.ib.controller.OrderStatus;
 import com.ib.controller.OrderType;
 import com.ib.controller.Types;
 import org.slf4j.LoggerFactory;
 
 import java.util.Locale;
 import java.util.concurrent.Executor;
 
 /**
  * Divden implements the divden approach
  */
 public class Divden extends StatefulContext implements EWrapper,Constants {
     static org.slf4j.Logger log = LoggerFactory.getLogger(Divden.class);
     public static final int CLIENT_ID = 22;
     public static final int INVALID_ORDER_ID = -1;
 
     public class Input {
         private final static double CASH_WIN = 10.00;
         private final static int NUM_SHARES = 1;
         private final static double RISK_CURRENCY = 10000.00;
        private final static String SYMBOL="t"; // AT&T
         private final static double TRANSACTION_COST = 1.50;
         private final static int TWS_PORT = 7496;
 
         /* Input parameters */
         public String account = null;
         public double cashWin = CASH_WIN;
         public int numShares = NUM_SHARES;
         public double riskCurrency = RISK_CURRENCY;
         public boolean isShort = false;
         public String symbol = SYMBOL;
         public double transactionCost = TRANSACTION_COST;
         public int twsPort = TWS_PORT;
     }
     public Input input = new Input();
 
     public class Market {
         Double bidPrice = null;
         Double askPrice = null;
         Double lastPrice = null;
         boolean isComplete(){
             return
                 bidPrice!=null &&
                 askPrice!=null &&
                 lastPrice!=null;
         }
     }
     public Market market = new Market();
 
     public class BrokerOrder {
         String name;
         Integer shares;
         Double price;
         Double fee;
         OrderType orderType;
         Order order = new Order();
         OrdStatus ordStatus;
         void report(){
             log.info(String.format(Locale.US,
                 "%s: id:%d s:%d p:%.2f f:%.2f",
                 name,order.m_orderId,shares,price,fee
             ));
         }
     }
 
     public class OrderSuite {
         int nextValidOrderId = -1;
         Contract contract = new Contract();
         BrokerOrder in = new BrokerOrder();
         BrokerOrder out = new BrokerOrder();
         BrokerOrder stop = new BrokerOrder();
         BrokerOrder newOrderStatus;
         Double movePercentToWin;
         Double movePercentToLose;
         void report(){
             in.report();
             out.report();
             stop.report();
             log.info(String.format("Win: %.2f%% Stop: %.2f%%",
                 movePercentToWin,
                 movePercentToLose));
         }
         BrokerOrder[] getOrders(){
             BrokerOrder[] ret = new BrokerOrder[3];
             ret[0]=in;
             ret[1]=out;
             ret[2]=stop;
             return ret;
         }
         BrokerOrder findBrokerOrderByOrderId(int orderId) throws OrderNotFoundException {
             for(BrokerOrder bo : getOrders())
                 if (bo.order.m_orderId == orderId)
                     return bo;
             throw new OrderNotFoundException();
         }
         class OrderNotFoundException extends Exception{}
     }
     public OrderSuite os = new OrderSuite();
 
     public class OrdStatus {
         int orderId;
         OrderStatus orderStatus;
         int filled;
         int remaining;
         double avgFillPrice;
         int permId;
         int parentId;
         double lastFillPrice;
         int clientId;
         String whyHeld;
         OrdStatus(int orderId, OrderStatus orderStatus, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
             this.orderId = orderId;
             this.orderStatus = orderStatus;
             this.filled = filled;
             this.remaining = remaining;
             this.avgFillPrice = avgFillPrice;
             this.permId = permId;
             this.parentId = parentId;
             this.lastFillPrice = lastFillPrice;
             this.clientId = clientId;
             this.whyHeld = whyHeld;
         }
     }
 
 
     private EClientSocket ibServer = new EClientSocket(this);
 //    private final Logger log;
 
     // defining states
     private final State<Divden> SHOWING_REPORT = FlowBuilder.state("SHOWING_REPORT");
     private final State<Divden> CONNECTING = FlowBuilder.state("CONNECTING");
     private final State<Divden> REQUESTING_MARKET_DATA = FlowBuilder.state("REQUESTING_MARKET_DATA");
     private final State<Divden> WAITING_FOR_MARKET_DATA = FlowBuilder.state("WAITING_FOR_MARKET_DATA");
     private final State<Divden> CALCULATING_POSITION = FlowBuilder.state("CALCULATING_POSITION");
     private final State<Divden> ISSUING_ORDERS = FlowBuilder.state("ISSUING_ORDERS");
     private final State<Divden> WAIT_FOR_ORDERS_RECEIVED_LOOP = FlowBuilder.state("WAIT_FOR_ORDERS_RECEIVED_LOOP");
     private final State<Divden> WAIT_FOR_ORDERS_RECEIVED = FlowBuilder.state("WAIT_FOR_ORDERS_RECEIVED");
     private final State<Divden> WAITING_FOR_POSITION_ENTRY = FlowBuilder.state("WAITING_FOR_POSITION_ENTRY");
     private final State<Divden> ENTERING_POSITION = FlowBuilder.state("ENTERING_POSITION");
     private final State<Divden> WAITING_FOR_POSITION_EXIT = FlowBuilder.state("WAITING_FOR_POSITION_EXIT");
     private final State<Divden> EXITING_POSITION = FlowBuilder.state("EXITING_POSITION");
     private final State<Divden> REPORTING_PROFITS = FlowBuilder.state("REPORTING_PROFITS");
     private final State<Divden> ERROR = FlowBuilder.state("ERROR");
     private final State<Divden> COMPLETE = FlowBuilder.state("COMPLETE");
 
     // defining events
     private final Event<Divden> onReportShown = FlowBuilder.event("onReportShown");
     private final Event<Divden> onConnected = FlowBuilder.event("onConnected");
     private final Event<Divden> onConnectFailed = FlowBuilder.event("onConnectFailed");
     private final Event<Divden> onMarketDataRequested = FlowBuilder.event("onMarketDataRequested");
     private final Event<Divden> onMarketData = FlowBuilder.event("onMarketData");
     private final Event<Divden> onPositionCalculated = FlowBuilder.event("onPositionCalculated");
     private final Event<Divden> onOrdersIssued = FlowBuilder.event("onOrdersIssued");
     private final Event<Divden> onOrderSubmitted = FlowBuilder.event("onOrderSubmitted");
     private final Event<Divden> onAllOrdersSubmitted = FlowBuilder.event("onAllOrdersSubmitted");
     private final Event<Divden> onEntryFound = FlowBuilder.event("onEntryFound");
     private final Event<Divden> onPositionEntered = FlowBuilder.event("onPositionEntered");
     private final Event<Divden> onExitFound = FlowBuilder.event("onExitFound");
     private final Event<Divden> onPositionExited = FlowBuilder.event("onPositionExited");
     private final Event<Divden> onProfitsReported = FlowBuilder.event("onProfitsReported");
     private final Event<Divden> onErrorReported = FlowBuilder.event("onErrorReported");
 
     private EasyFlow<Divden> flow;
 
     private void initFlow() {
         if (flow != null) {
             return;
         }
         // build our FSM
         flow = FlowBuilder.from(SHOWING_REPORT).transit(
             onReportShown.to(CONNECTING)
         );
         FlowBuilder.from(CONNECTING).transit(
             onConnected.to(REQUESTING_MARKET_DATA),
             onConnectFailed.to(ERROR)
         );
         FlowBuilder.from(REQUESTING_MARKET_DATA).transit(
             onMarketDataRequested.to(WAITING_FOR_MARKET_DATA)
         );
         FlowBuilder.from(WAITING_FOR_MARKET_DATA).transit(
             onMarketData.ignoreOutOfState().to(CALCULATING_POSITION)
         );
         FlowBuilder.from(CALCULATING_POSITION).transit(
             onPositionCalculated.to(ISSUING_ORDERS)
         );
         FlowBuilder.from(ISSUING_ORDERS).transit(
             onOrdersIssued.to(WAIT_FOR_ORDERS_RECEIVED_LOOP)
         );
         FlowBuilder.from(WAIT_FOR_ORDERS_RECEIVED_LOOP).transit(
             onOrderSubmitted.to(WAIT_FOR_ORDERS_RECEIVED)
         );
         FlowBuilder.from(WAIT_FOR_ORDERS_RECEIVED).transit(
             onOrdersIssued.to(WAIT_FOR_ORDERS_RECEIVED_LOOP),
             onAllOrdersSubmitted.to(WAITING_FOR_POSITION_ENTRY)
         );
         FlowBuilder.from(WAITING_FOR_POSITION_ENTRY).transit(
             onEntryFound.to(ENTERING_POSITION)
         );
         FlowBuilder.from(ENTERING_POSITION).transit(
             onPositionEntered.to(WAITING_FOR_POSITION_EXIT)
         );
         FlowBuilder.from(WAITING_FOR_POSITION_EXIT).transit(
             onExitFound.to(EXITING_POSITION)
         );
         FlowBuilder.from(EXITING_POSITION).transit(
             onPositionExited.to(REPORTING_PROFITS)
         );
         FlowBuilder.from(ERROR).transit(
             onErrorReported.finish(COMPLETE)
         );
         FlowBuilder.from(REPORTING_PROFITS).transit(
             onProfitsReported.finish(COMPLETE)
         );
 
         flow.executor(new Executor() {
             @Override public void execute(Runnable runnable) { runnable.run(); }
         });
 
 //        flow.trace();
     }
 
     private void bindFlow() {
         SHOWING_REPORT.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
             log.info("Account:          {}", input.account);
             log.info("Direction:        {}", input.isShort ? "Short" : "Long");
             log.info("Symbol:           {}", input.symbol);
             log.info("Casgh Win:        {}", input.cashWin);
             log.info("Shares:           {}", input.numShares);
             log.info("Risk Currency:    {}", input.riskCurrency);
             log.info("Transaction Cost: {}", input.transactionCost);
             onReportShown.trigger(context);
             }
         });
         CONNECTING.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 ibServer.eConnect("localhost", input.twsPort, 0);
                 if (ibServer.isConnected()){
                     ibServer.reqAccountSummary(1, "All", "BuyingPower");
                     onConnected.trigger(context);
                 } else {
                     onConnectFailed.trigger(context);
                 }
             }
         });
         REQUESTING_MARKET_DATA.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 Contract c = os.contract;
 
                 String[] symbolParts = input.symbol.split(":");
                 c.m_symbol = symbolParts[0].toUpperCase();
                 if (symbolParts.length>1)
                     c.m_expiry = symbolParts[1];
 
                 try {
                     Symbol s = Symbol.valueOf(c.m_symbol);
                     c.m_secType = s.securityType.name();
                     c.m_exchange = s.exchange.name();
                     c.m_currency = s.currency.name();
                 } catch (IllegalArgumentException e){
                     c.m_secType = SecurityType.STK.name();
                     c.m_exchange = Exchange.SMART.name();
                     c.m_currency = Currency.USD.name();
                 }
 
                 ibServer.reqMktData(os.nextValidOrderId++, os.contract, JavaClient.GENERIC_TICKS, false);
                 onMarketDataRequested.trigger(context);
 
                 // DEBUG
                 /*
                 market.lastPrice = 35.53;
                 market.askPrice = market.bidPrice = market.lastPrice;
                 onMarketData.trigger(context);
                 */
             }
         });
         CALCULATING_POSITION.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,String.format("bid=%f last=%f ask=%f",
                     market.bidPrice,
                     market.askPrice,
                     market.lastPrice));
 
                 // In
                 os.in.orderType = OrderType.MKT;
                 os.in.name = "inn";
                 os.in.price = market.lastPrice;
                 os.in.shares = (int) (input.riskCurrency / market.lastPrice);
                 os.in.fee = Math.max(1.0,0.005* os.in.shares);
 
                 // Out
                 os.out.name = "out";
                 os.out.orderType = OrderType.LMT;
                 os.out.shares = os.in.shares - input.numShares;
                 os.out.price = Math.ceil( 100* (
                     (os.in.price* os.in.shares+2* os.in.fee+input.cashWin)/ os.out.shares)
                 )/100;
                 os.out.fee = Math.max(1.0,0.005* os.out.shares);
 
                 // Stop
                 os.stop.name = "stp";
                 os.stop.orderType = OrderType.STP;
                 os.stop.shares = os.in.shares;
                 os.stop.price = os.in.price - (os.out.price- os.in.price)*2;
                 os.stop.fee = Math.max(1.0,0.005* os.stop.shares);
 
                 // Aggregate
                 os.movePercentToWin = 100.0*(os.out.price - os.in.price) / os.in.price;
                 os.movePercentToLose = 100.0*(os.stop.price - os.in.price) / os.in.price;
 
 
                 onPositionCalculated.trigger(context);
             }
         });
         ISSUING_ORDERS.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
 
                 // Order
                 if (os.nextValidOrderId == INVALID_ORDER_ID)
                     throw new DivdenException("no valid order id given");
                 for (BrokerOrder o : os.getOrders()){
                     o.order.m_clientId = CLIENT_ID;
                     o.order.m_orderId = os.nextValidOrderId++;
                     o.order.m_orderType = o.orderType.getApiString();
                     o.order.m_lmtPrice = o.price;
                     o.order.m_totalQuantity = o.shares;
                     o.order.m_auxPrice = 0.0;
                     o.order.m_goodAfterTime = "";
                     o.order.m_goodTillDate = "";
                     o.order.m_account = input.account;
 
                    o.order.m_transmit = false;
                 }
                 if (input.isShort){
                     os.in.order.m_action = Types.Action.SSHORT.getApiString();
                     os.out.order.m_action = Types.Action.BUY.getApiString();
                 } else {
                     os.in.order.m_action = Types.Action.BUY.getApiString();
                     os.out.order.m_action = Types.Action.SELL.getApiString();
                    os.stop.order.m_action = Types.Action.SELL.getApiString();
                 }
 
                 os.report();
 
                 ibServer.placeOrder( os.in.order.m_orderId, os.contract, os.in.order );
                ibServer.placeOrder( os.out.order.m_orderId, os.contract, os.out.order );
                ibServer.placeOrder( os.stop.order.m_orderId, os.contract, os.stop.order );
 
                 onOrdersIssued.trigger(context);
             }
         });
         WAIT_FOR_ORDERS_RECEIVED.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 if (os.newOrderStatus==null){
                     logOutState(state,"First time here");
                 } else {
                     logOutState(state,"order: "+os.newOrderStatus.name);
                     if (
                         os.in.ordStatus!=null
 // TODO: Uncomment these when all the orders are submitted in ISSUING_ORDERS
                        && os.out.ordStatus!=null
                        && os.stop.ordStatus!=null
                     ) {
                         onAllOrdersSubmitted.trigger(context);  // Continue to next step
                     } else {
                         onOrdersIssued.trigger(context); // Wait for next one
                     }
                 }
             }
         });
         WAITING_FOR_POSITION_ENTRY.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"We're not that advanced yet");
                 onEntryFound.trigger(context);
             }
         });
         ENTERING_POSITION.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 onPositionEntered.trigger(context);
             }
         });
         WAITING_FOR_POSITION_EXIT.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 onExitFound.trigger(context);
             }
         });
         EXITING_POSITION.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 onPositionExited.trigger(context);
             }
         });
         REPORTING_PROFITS.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"");
                 onProfitsReported.trigger(context);
             }
         });
         ERROR.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logErrState(state,"ERROR EXIT");
             }
         });
         COMPLETE.whenEnter(new StateHandler<Divden>() {
             @Override
             public void call(State<Divden> state, Divden context) throws Exception {
                 logOutState(state,"That's all folks!");
                 System.exit(0);
             }
         });
     }
 
     private void logOutState(State state, String format, Object... objects){ log.info(state.toString() + ": " + format, objects); }
     private void logOutState(State state, String s){ log.info(state.toString() + ": " + s); }
     private void logErrState(State state, String format, Object... objects){ log.error(state.toString() + ": " + format, objects); }
     private void logErrState(State state, String s){ log.error(state.toString() + ": " + s); }
 
     public class DivdenException extends Exception{public DivdenException(String m){super(m);}}
 
     public Divden(){
         initFlow();
         bindFlow();
     }
 
     public void validate() throws DivdenException {
         if (input.account==null){
             throw new DivdenException("Account cannot be null");
         }
     }
 
     public void start() {
         flow.start(this);
     }
 
     @Override public void accountDownloadEnd(String accountName) { }
     @Override public void accountSummary(int reqId, String account, String tag, String value, String currency) {
         log.trace(String.format("reqId=%d account=%s tag=%s value=%s currency=%s",
             reqId, account, tag, value, currency));
     }
     @Override public void accountSummaryEnd(int reqId) { }
     @Override public void bondContractDetails(int reqId, ContractDetails contractDetails) { }
     @Override public void commissionReport(CommissionReport commissionReport) { }
     @Override public void connectionClosed() { }
     @Override public void contractDetails(int reqId, ContractDetails contractDetails) { }
     @Override public void contractDetailsEnd(int reqId) { }
     @Override public void currentTime(long time) { }
     @Override public void deltaNeutralValidation(int reqId, UnderComp underComp) { }
     @Override public void error(Exception e) {
         log.error("Exception: {}", e.getMessage());
     }
     @Override public void error(String str) {
         log.error(str);
     }
     @Override public void error(int id, int errorCode, String errorMsg) {
         log.error("id={} code={} msg={}", id, errorCode, errorMsg);
     }
     @Override public void execDetails(int reqId, Contract contract, Execution execution) { }
     @Override public void execDetailsEnd(int reqId) { }
     @Override public void fundamentalData(int reqId, String data) { }
     @Override public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double WAP, boolean hasGaps) { }
     @Override public void managedAccounts(String accountsList) { }
     @Override public void marketDataType(int reqId, int marketDataType) { }
     @Override public void nextValidId(int orderId) {
         os.nextValidOrderId = orderId;
         log.info("nextValidId={}", orderId);
     }
     @Override public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) { }
     @Override public void openOrderEnd() { }
     @Override public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice, int clientId, String whyHeld) {
         log.info(String.format(
             "orderId=%d status=%s filled=%d remaining=%d avgFillPrice=%f permId=%d parentId=%d lastFillPrice=%f clientId=%d whyHeld=%s",
             orderId,status,filled,remaining,avgFillPrice,permId,parentId,lastFillPrice,clientId,whyHeld
         ));
 
         if (status==null){
             log.warn("Null order status ignored");
         } else {
             OrderStatus orderStatus;
             try {
                 orderStatus = OrderStatus.valueOf(status);
                 switch (orderStatus){
                     case Submitted:
                     case PreSubmitted:
                         BrokerOrder bo = os.findBrokerOrderByOrderId(orderId);
                         bo.ordStatus = new OrdStatus(
                             orderId,orderStatus,filled,remaining,avgFillPrice,permId,parentId,lastFillPrice,clientId,whyHeld
                         );
                         os.newOrderStatus = bo;
                         onOrderSubmitted.trigger(this);
                         break;
                     default:
                         log.info("Ingoring order status: '{}'",status);
                 }
             } catch (IllegalArgumentException e){
                 log.error("Unknown order status: '{}'",status);
             } catch (OrderSuite.OrderNotFoundException e){
                 log.warn("Ignoring order status for unknown order: {}",orderId);
             }
         }
     }
     @Override public void position(String account, Contract contract, int pos, double avgCost) { }
     @Override public void positionEnd() { }
     @Override public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) { }
     @Override public void receiveFA(int faDataType, String xml) { }
     @Override public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) { }
     @Override public void scannerDataEnd(int reqId) { }
     @Override public void scannerParameters(String xml) { }
     @Override public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry, double dividendImpact, double dividendsToExpiry) { }
     @Override public void tickGeneric(int tickerId, int tickType, double value) { }
     @Override public void tickOptionComputation(int tickerId, int field, double impliedVol, double delta, double optPrice, double pvDividend, double gamma, double vega, double theta, double undPrice) { }
     @Override public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
         log.info(String.format(
             "tickerId=%d field=%s price=%f canAutoExecute=%b",
             tickerId,TickType.getField(field),price,canAutoExecute
         ));
         boolean newData = true;
         switch (field){
             case TickType.BID: market.bidPrice = price; break;
             case TickType.ASK: market.askPrice = price; break;
             case TickType.LAST: market.lastPrice = price; break;
             default: newData = false;
         }
         if (newData && market.isComplete()){
             onMarketData.trigger(this);
         }
     }
     @Override public void tickSize(int tickerId, int field, int size) { }
     @Override public void tickSnapshotEnd(int reqId) { }
     @Override public void tickString(int tickerId, int tickType, String value) { }
     @Override public void updateAccountTime(String timeStamp) { }
     @Override public void updateAccountValue(String key, String value, String currency, String accountName) { }
     @Override public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) { }
     @Override public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {}
     @Override public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) { }
     @Override public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL, double realizedPNL, String accountName) { }
 
 }
