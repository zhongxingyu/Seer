 package com.fhx.service.ib.marketdata;
 
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.concurrent.ConcurrentHashMap;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import com.fhx.service.ib.order.IBOrderSenderHelper;
 import com.fhx.service.ib.order.IBOrderStateWrapper;
 import com.ib.client.Execution;
 import com.ib.client.Order;
 
 /*
  * Listener class that gets callbacks on IBOrderEvent
  * Keeps list of open/exec/cancelled order states
  * 
  */
 public class IBOrderService extends IBOrderEventListener {
 
 	@SuppressWarnings("unused")
 	private static final long serialVersionUID = -7426452967133280762L;
 	private static Logger log = Logger.getLogger(IBOrderService.class);
 
 	private static IBClient client;
 
 	private static boolean simulation = Boolean.parseBoolean(System.getProperty("simulation"));
 
 	static IBOrderService INSTANCE = new IBOrderService();
 	
 	private IBOrderService() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 	}
 	
 	public static IBOrderService getInstance() {
 		if(INSTANCE != null)
 			return INSTANCE;
 		else
 			return new IBOrderService();
 	}
 	
 	public void handleInit() {
 
 		if (!simulation) {
 			client = IBClient.getDefaultInstance();
 		}
 	}
 	
 	public void handleInitWatchlist() {
 
 		if ((client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))
 				//&& !client.getIbAdapter().isRequested() && !simulation) {
 				&& !client.getIbAdapter().isRequested() ) {
 
 			client.getIbAdapter().setRequested(true);
 			client.getIbAdapter().setState(ConnectionState.SUBSCRIBED);
 
 		}
 	}
 	
 	public static void main(String[] args) {
 		
 		final IBOrderService ors = IBOrderService.getInstance();
 		IBEventServiceImpl.getEventService().addOrderEventListener(ors);
 		
 		try {
 			IBOrderSenderHelper osh = IBOrderSenderHelper.getInstance();
 			ors.handleInit();
 			Thread.sleep(10*1000);
 			ors.handleInitWatchlist();
 
 			// get all open orders if any
 			osh.reqOpenOrders();
 			osh.reqAccountUpdates();
 			
 			osh.sendNewOrder("IBM", "buy", 100, 0.99);
 			osh.sendNewOrder("IBM", "buy", 200, 0.99);
 			osh.sendNewOrder("IBM", "buy", 300, 0.99);
 			
 			Thread.sleep(5*1000);
 	
 			for(Order order : ors.getOpenOrders().values()) {
 				log.info("attempting to cancel orderId "+order.m_orderId);
 				osh.cancelOrderById(order.m_orderId);
 			}
 
 			
 	/*		while(true) {
 				// make sure the thread doesn't end
 				printOrders(ors.submittedOrders, "submitted ");
 				Thread.sleep(1*1000);
 				printOrders(ors.openOrders, "open ");
 				Thread.sleep(1*1000);
 				printOrders(ors.execOrders, "executed ");
 				Thread.sleep(1*1000);
 				printOrders(ors.cancelledOrders, "cancelled ");
 
 				log.info("main thread--sleeping...");
 				Thread.sleep(10*1000);
 				
 				// send test orders
 				int idx = (new Random()).nextInt(SYMBOLS.length);
 				ors.sendNewOrder(SYMBOLS[idx], "buy", 100, 0.99);
 				ors.reqOpenOrders();
 			}
 	*/
 		}
 		catch(Exception e) {
 			e.printStackTrace();
 		}
 		finally {
 			// make sure order failure is handled here
 		}
 		
 	}
 	
 	public static void printOrders(Map<Integer, Order> orders, String type) {
 		StringBuffer sb = new StringBuffer();
 		
 		sb.append(type + " orders: \n");
 		
 		for(Map.Entry<Integer, Order> ord : orders.entrySet())
 		{
 			Order o = ord.getValue();
 			sb.append("orderId="+ord.getKey());
			sb.append(",side="+o.m_action);
 			sb.append(",totalQty="+o.m_totalQuantity);
 			sb.append(",price="+o.m_lmtPrice);
 			sb.append("\n");
 		}
 		log.info(sb.toString());
 	}
 	
 	
 	// order management
 	private Map<Integer, Order> openOrders = new ConcurrentHashMap<Integer, Order>();
 	private Map<Integer, IBOrderStateWrapper> fillOrders = new ConcurrentHashMap<Integer, IBOrderStateWrapper>();
 	private Map<String, Execution> executionDetails = new ConcurrentHashMap<String, Execution>();
 	private Map<Integer, IBOrderStateWrapper> cancelledOrders = new ConcurrentHashMap<Integer, IBOrderStateWrapper>();
 	private static Map<String, Integer> m_ibOpenPositions = new ConcurrentHashMap<String, Integer>();
 	
 	public synchronized Map<Integer, Order> getOpenOrders() {
 		return Collections.unmodifiableMap(openOrders);
 	}
 
 	// should return a copy of this, i.e. Map copy = new LinkedHashMap(m);
 //	public synchronized Map<String, Execution> getExecOrders() {
 //		return Collections.unmodifiableMap(executionDetails);
 //	}
 	public void addExecOrders(String execId, Execution execution) {
 		this.executionDetails.put(execId, execution);
 	}
 
 	public synchronized Map<Integer, IBOrderStateWrapper> getFillOrders() {
 		return Collections.unmodifiableMap(fillOrders);
 	}
 
 	public synchronized Map<Integer, IBOrderStateWrapper> getCancelledOrders() {
 		return Collections.unmodifiableMap(cancelledOrders);
 	}
 	
 	public synchronized void addToOpenOrders(Order o) {
 		openOrders.put(o.m_orderId, o);
 	}
 	public synchronized void removeOpenOrder(int orderId) {
 		openOrders.remove(orderId);
 	}
 	public synchronized void addToCancelledOrders(int orderId, IBOrderStateWrapper orderState) {
 		cancelledOrders.put(orderId, orderState);
 	}
 	public synchronized void addToFilledOrders(int orderId, IBOrderStateWrapper orderState) {
 		fillOrders.put(orderId, orderState);
 	}
 	public synchronized void updatePosition(String symbol, int position) {
 		m_ibOpenPositions.put(symbol, new Integer(position));
 	}
 	public synchronized Map<String, Integer> getCurrenctPositions() {
 		return Collections.unmodifiableMap(m_ibOpenPositions); 
 	}
 
 	@Override
 	public void onIBEvent(IBEventData event) {
 		// TODO Auto-generated method stub
 		
 		if (event.getEventType()==IBEventType.NextValidId) {
 			RequestIDGenerator.singleton().initializeOrderId(event.getNextOrderId());
 		}
 		
 		log.info("onIBEvent callback: event type " + event.getEventType());
 		
 		if(openOrders.size()>0)
 			printOrders(openOrders, "open ");
 		if(executionDetails.size()>0);
 			//TODO: what to do with executions?
 	}
 	
 }
