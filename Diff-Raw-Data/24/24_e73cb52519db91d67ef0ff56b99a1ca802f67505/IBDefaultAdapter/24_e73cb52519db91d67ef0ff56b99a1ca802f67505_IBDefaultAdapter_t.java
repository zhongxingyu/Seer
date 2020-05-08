 package com.fhx.service.ib.marketdata;
 
 import java.io.EOFException;
 import java.net.SocketException;
 import java.util.Hashtable;
 import java.util.List;
 import java.util.Vector;
 
 import org.apache.log4j.Logger;
 
 import com.fhx.service.ib.order.IBOrderStateWrapper;
 import com.fhx.service.ib.order.IBOrderStatus;
 import com.ib.client.Contract;
 import com.ib.client.ContractDetails;
 import com.ib.client.EWrapper;
 import com.ib.client.EWrapperMsgGenerator;
 import com.ib.client.Execution;
 import com.ib.client.Order;
 import com.ib.client.OrderState;
 import com.ib.client.UnderComp;
 
 public class IBDefaultAdapter implements EWrapper {
 
 	private static Logger log = Logger.getLogger(IBDefaultAdapter.class);
 	private ConnectionState state = ConnectionState.DISCONNECTED;
 	private boolean requested;
 	private int clientId;
 	
 	// map reqId to symbol
 	private Hashtable<Integer, String> requestSymbols=new Hashtable<Integer, String>();
 	private List<IBService> subscriber = new Vector<IBService>();
 
 	public IBDefaultAdapter(int clientId) {
 
 		this.clientId = clientId;
 	}
 	public void setRequestSymbols(Hashtable<Integer, String> reqSymbols) {
 		this.requestSymbols = reqSymbols;
 	}
 
 	public void addIBServiceHandle(IBService client) {
 		this.subscriber.add(client);
 	}
 	
 	
 	@Override
 	public void connectionClosed() {
 
 		this.state = ConnectionState.DISCONNECTED;
 	}
 
 	@Override
 	public void error(Exception e) {
 
 		// we get EOFException and SocketException when TWS is closed
 		if (!(e instanceof EOFException || e instanceof SocketException)) {
 			log.error("ib error", e);
 		}
 	}
 
 	@Override
 	public void error(int id, int code, String errorMsg) {
 		String message = "xxxx error(int id, int code, String errorMsg) client: " + this.clientId + " id: " + id + " code: " + code + " " + errorMsg.replaceAll("\n", " ");
 		
 		IBEventData data = new IBEventData(message, IBEventType.Error);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 
 		switch (code) {
 
 		// order related error messages will usually come along with a orderStatus=Inactive
 		// which will lead to a cancellation of the GenericOrder. If there is no orderStatus=Inactive
 		// coming along, the GenericOrder has to be cancelled by us (potenially creating a "fake" OrderStatus)
 			case 201:
 
 				// Order rejected - reason:
 				// cancel the order
 				log.error(message);
 				break;
 
 			case 202:
 
 				// Order cancelled
 				// do nothing, since we cancelled the order ourself
 				log.debug(message);
 				break;
 
 			case 399:
 
 				// Order Message: Warning: Your order size is below the EUR 20000 IdealPro minimum and will be routed as an odd lot order.
 				// do nothing, this is ok for small FX Orders
 				log.debug(message);
 				break;
 
 			case 434:
 
 				// The order size cannot be zero
 				// This happens in a closing order using PctChange where the percentage is
 				// small enough to round to zero for each individual client account
 				log.debug(message);
 				break;
 
 			case 502:
 
 				// Couldn't connect to TWS
 				setState(ConnectionState.DISCONNECTED);
 				log.info(message);
 				break;
 
 			case 1100:
 
 				// Connectivity between IB and TWS has been lost.
 				setState(ConnectionState.CONNECTED);
 				log.info(message);
 				break;
 
 			case 1101:
 
 				// Connectivity between IB and TWS has been restored data lost.
 				setRequested(false);
 				setState(ConnectionState.READY);
 				//ServiceLocator.commonInstance().getMarketDataService().initWatchlist();
 				log.info(message);
 				break;
 
 			case 1102:
 
 				// Connectivity between IB and TWS has been restored data maintained.
 				if (isRequested()) {
 					setState(ConnectionState.SUBSCRIBED);
 				} else {
 					setState(ConnectionState.READY);
 					//ServiceLocator.commonInstance().getMarketDataService().initWatchlist();
 				}
 				log.info(message);
 				break;
 
 			case 2110:
 
 				// Connectivity between TWS and server is broken. It will be restored automatically.
 				setState(ConnectionState.CONNECTED);
 				log.info(message);
 				break;
 
 			case 2104:
 
 				// A market data farm is connected.
 				if (isRequested()) {
 					setState(ConnectionState.SUBSCRIBED);
 				} else {
 					setState(ConnectionState.READY);
 					//ServiceLocator.commonInstance().getMarketDataService().initWatchlist();
 				}
 				log.info(message);
 				break;
 
 			default:
 				if (code < 1000) {
 					log.error(message);
 				} else {
 					log.info(message);
 				}
 				break;
 		}
 	}
 
 	@Override
 	public void error(String str) {
 		log.error(str, new RuntimeException(str));
 	}
 
 	public ConnectionState getState() {
 		return this.state;
 	}
 
 	public void setState(ConnectionState state) {
 
 		if (this.state != state) {
 			log.debug("state: " + state);
 		}
 		this.state = state;
 	}
 
 	public boolean isRequested() {
 		return this.requested;
 	}
 
 	public void setRequested(boolean requested) {
 
 		if (this.requested != requested) {
 			log.debug("requested: " + requested);
 		}
 
 		this.requested = requested;
 	}
 
 	@Override
 	public synchronized void nextValidId(final int orderId) {
 		log.info("IDDD: orderId=" + orderId + ", nextValidId=" + EWrapperMsgGenerator.nextValidId(orderId));
 		
 		IBEventData data = new IBEventData(orderId, IBEventType.NextValidId);
 		data.setNextOrderId(orderId);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 	}
 
 	// Override EWrapper methods with default implementation
 
 	@Override
 	public void accountDownloadEnd(String accountName) {
 	}
 
 	@Override
 	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
 	}
 
 	@Override
 	public void contractDetails(int reqId, ContractDetails contractDetails) {
 	}
 
 	@Override
 	public void contractDetailsEnd(int reqId) {
 	}
 
 	@Override
 	public void currentTime(long time) {
 		log.info("xxxx currentTime() \n");
 	}
 
 	@Override
 	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
 	}
 
 	@Override
 	public void execDetails(int reqId, Contract contract, Execution execution) {
 		log.info("execDetails(int reqId=" + reqId + ", Contract contract=" + contract.toString() + ", Execution execution=" + execution.toString() + ")" ); 
 		
 		// propagateToIBOrderService()
 		IBEventData data = new IBEventData("xxxx orderId=" + reqId +", execution=" +execution.toString(), IBEventType.ExecDetails);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 		
 		IBOrderService.getInstance().addExecOrders(execution.m_execId, execution);
 	}
 
 	@Override
 	public void execDetailsEnd(int reqId) {
 		log.info("execDetails(int orderId=" + reqId +")");
 		
 		// propagateToIBOrderService()
 		IBEventData data = new IBEventData("xxxx orderId=" + reqId, IBEventType.ExecDetailsEnd);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 	}
 
 	@Override
 	public void fundamentalData(int reqId, String data) {
 	}
 
 	@Override
 	public void historicalData(int reqId, String date, double open, double high, double low, double close, int volume, int count, double wap, boolean hasGaps) {
 		
 		log.info("xxxx historicalData(reqId="+reqId+", Symbol="+requestSymbols.get(reqId)+", date="+date+",  open="+open+", close="+close+", volume="+volume+
 				 ", count="+count+", WAP="+wap+", hadGaps="+String.valueOf(hasGaps)+")");
 
 	}
 
 	@Override
 	public void managedAccounts(String accountsList) {
 	}
 
 	@Override
 	public void openOrder(int orderId, Contract contract, Order order, OrderState orderState) {
 		log.info("openOrder->orderId|"+orderId+"||contract|"+contract.m_symbol+"||order|"+order.m_totalQuantity+"||orderState|"+orderState.m_status); 
 		
 		// propagateToIBOrderService()
 		IBEventData data = new IBEventData(order, IBEventType.OpenOrder);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 		
 		// put open order detail into the map to keep track
 		IBOrderService.getInstance().addToOpenOrders(order);
 	}
 
 	@Override
 	public void openOrderEnd() {
 		log.info("xxxx -> openOrderEnd() \n");
 	}
 
 	@Override
 	public void orderStatus(int orderId, String status, int filled, int remaining, double avgFillPrice, int permId, int parentId, double lastFillPrice,
 			int clientId, String whyHeld) {
 		log.info("orderStatus->orderId|"+orderId+"||status|"+status+"||filled|"+filled+"||remaining|"+remaining+"||avgFillPrice|"+avgFillPrice+
 				 "||permId|"+permId+"||parentId|"+parentId+"||lastFillPrice|"+lastFillPrice+"||clientId|"+clientId+"||whyHeld|"+whyHeld ); 
 		
 		IBEventData data = new IBEventData(orderId+"|"+status, IBEventType.OrderStatus);
 		IBEventServiceImpl.getEventService().fireIBEvent(data);
 		
 		if(IBOrderStatus.Filled.toString().equals(status)) {
 			log.info("Moving orderId "+orderId+" from open to exec order list");
 			IBOrderService.getInstance().removeOpenOrder(orderId);
 			IBOrderService.getInstance().addToFilledOrders(orderId, 
 					 new IBOrderStateWrapper(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
 		}
 		else if(IBOrderStatus.Cancelled.toString().equals(status)) {
 			IBOrderService.getInstance().removeOpenOrder(orderId);
 			IBOrderService.getInstance().addToCancelledOrders(orderId, 
 					 new IBOrderStateWrapper(orderId, status, filled, remaining, avgFillPrice, permId, parentId, lastFillPrice, clientId, whyHeld));
 		}
 		else if(IBOrderStatus.Inactive.toString().equals(status)) {
 			log.error("Order is inactive due to system, exchange or other issues, details: "+
 					 "orderId|"+orderId+"||status|"+status+"||filled|"+filled+"||remaining|"+remaining+"||avgFillPrice|"+avgFillPrice+
 					 "||permId|"+permId+"||parentId|"+parentId+"||lastFillPrice|"+lastFillPrice+"||clientId|"+clientId+"||whyHeld|"+whyHeld);
 			
 			//Try to cancel the order if it's stuck in Inactive state
 		}
 	}
 
 	@Override
 	public void realtimeBar(int reqId, long time, double open, double high, double low, double close, long volume, double wap, int count) {
 	}
 
 	@Override
 	public void receiveFA(int faDataType, String xml) {
 	}
 
 	@Override
 	public void scannerData(int reqId, int rank, ContractDetails contractDetails, String distance, String benchmark, String projection, String legsStr) {
 	}
 
 	@Override
 	public void scannerDataEnd(int reqId) {
 	}
 
 	@Override
 	public void scannerParameters(String xml) {
 	}
 
 	@Override
 	public void tickEFP(int tickerId, int tickType, double basisPoints, String formattedBasisPoints, double impliedFuture, int holdDays, String futureExpiry,
 			double dividendImpact, double dividendsToExpiry) {
 	}
 
 	@Override
 	public void tickGeneric(int tickerId, int tickType, double value) {
 		log.info("tickGeneric->tickerId|"+tickerId+"||field|"+tickType+"||value|"+value);
 	}
 
 	@Override
 	public void tickPrice(int tickerId, int field, double price, int canAutoExecute) {
 		log.info("tickPrice->tickerId|"+tickerId+"||field|"+field+"||price|"+price+"||canAutoExecute|"+canAutoExecute);
 				
 	}
 
 	@Override
 	public void tickSize(int tickerId, int field, int size) {
 		log.info("tickSize->tickerId|"+tickerId+"||field|"+field+"||size|"+size);
 	}
 
 	@Override
 	public void tickSnapshotEnd(int reqId) {
 	}
 
 	@Override
 	public void tickString(int tickerId, int tickType, String value) {
 	}
 
 	@Override
 	public void tickOptionComputation(int arg0, int arg1, double arg2, double arg3, double arg4, double arg5, double arg6, double arg7, double arg8, double arg9) {
 
 	}
 
 	@Override
 	public void updateAccountTime(String timeStamp) {
 		log.info("updateAccountTime->" + timeStamp);
 	}
 
 	@Override
 	public void updateAccountValue(String key, String value, String currency, String accountName) {
 	}
 
 	@Override
 	public void updateMktDepth(int tickerId, int position, int operation, int side, double price, int size) {
 	}
 
 	@Override
 	public void updateMktDepthL2(int tickerId, int position, String marketMaker, int operation, int side, double price, int size) {
 	}
 
 	@Override
 	public void updateNewsBulletin(int msgId, int msgType, String message, String origExchange) {
 	}
 
 	@Override
 	public void updatePortfolio(Contract contract, int position, double marketPrice, double marketValue, double averageCost, double unrealizedPNL,
 			double realizedPNL, String accountName) {
 		// This method is called if we subscribe to account and position updates
		log.info("Got a position update: symbol|" + contract.m_symbol + "|open qty|" + position);
 		IBOrderService.getInstance().updatePosition(contract.m_symbol, position);
 	}
 }
