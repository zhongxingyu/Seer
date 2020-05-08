 package com.fhx.service.ib.order;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.math.BigDecimal;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.marketcetera.trade.Factory;
 import org.marketcetera.trade.MSymbol;
 import org.marketcetera.trade.OrderID;
 import org.marketcetera.trade.OrderSingle;
 import org.marketcetera.trade.OrderType;
 import org.marketcetera.trade.Side;
 import org.marketcetera.trade.TimeInForce;
 
 import com.fhx.service.ib.marketdata.ConnectionState;
 import com.fhx.service.ib.marketdata.IBClient;
 import com.fhx.service.ib.marketdata.IBOrderService;
 import com.fhx.service.ib.marketdata.IBUtil;
 import com.fhx.service.ib.marketdata.RequestIDGenerator;
 import com.ib.client.Contract;
 import com.ib.client.Order;
 
 public class IBOrderSenderHelper {
 
 	@SuppressWarnings("unused")
 	private static final long serialVersionUID = -7426452967133280762L;
 	private static Logger log = Logger.getLogger(IBOrderSenderHelper.class);
 	protected final Properties config = new Properties();
 
 	private static IBClient client;
 
 	private static boolean simulation;
 	private static String accountCode;
 
 	static IBOrderSenderHelper INSTANCE = new IBOrderSenderHelper();
 	
 	private IBOrderSenderHelper() {
 		PropertyConfigurator.configure("conf/log4j.properties");
 		
 		try {
 			config.load(new FileInputStream("conf/statstream.properties"));
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 			return;
 		}
 		
 		simulation = Boolean.parseBoolean(config.getProperty("SIMULATION"));
 		accountCode = config.getProperty("ACCOUNT_CODE");
 		if (!simulation) {
 			client = IBClient.getDefaultInstance();
 		}
 		if(accountCode == null || "".equals(accountCode)) {
 			log.error("Must Specify account code to receive account update callbacks");
 		}
 	}
 	
 	public static IBOrderSenderHelper getInstance() {
 		if(INSTANCE != null)
 			return INSTANCE;
 		else
 			return new IBOrderSenderHelper();
 	}
 
 	public void requestIBCallbacks() {
 		try {
 			if(isIBConnected()) {
 				reqOpenOrders();
 				reqAccountUpdates();
 			}
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private boolean isIBConnected() {
 		if(client == null) {
 			log.error("Found null instance of ib client");
 			return false;
 		}
 		if(!(client.getIbAdapter().getState().equals(ConnectionState.READY)) && !(client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected, found state " + client.getIbAdapter().getState().toString());
 			return false;
 		}
 		return true;
 	}
 	
 	public void sendNewOrder(String symbol, String type, int size, double price) throws Exception {
 
 		int orderNumber = RequestIDGenerator.singleton().getNextOrderId();
 		//order.setNumber(orderNumber);
 		OrderSingle order = Factory.getInstance().createOrderSingle();
 		order.setOrderID(new OrderID(orderNumber+""));
 		
 		//order.setOrderType(OrderType.Market);
 		order.setOrderType(OrderType.Limit);
 		order.setQuantity(new BigDecimal(size));
 		order.setPrice(new BigDecimal(price));
 		
 		if (type.equalsIgnoreCase("buy"))
 			order.setSide(Side.Buy);
 		else if (type.equalsIgnoreCase("sell"))
 			order.setSide(Side.Sell);
 		else if (type.equalsIgnoreCase("sellshort"))
 			order.setSide(Side.SellShort); 
 			
 		order.setSymbol(new MSymbol(symbol));
 		order.setTimeInForce(TimeInForce.Day);
 		
 		sendOrModifyOrder(order);
 	}
 
 	public void modifyOrder(OrderSingle order) throws Exception {
 
 		sendOrModifyOrder(order);
 	}
 
 	/**
 	 * helper method to be used in both sendorder and modifyorder.
 	 * @throws Exception
 	 */
 	public void sendOrModifyOrder(OrderSingle order) throws Exception {
 
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected, found state " + client.getIbAdapter().getState().equals(ConnectionState.READY.toString()));
 			return;
 		}
 		
 		Contract contract = IBUtil.getContract(order.getSymbol().getFullSymbol());
 
 		com.ib.client.Order ibOrder = new com.ib.client.Order();
 		ibOrder.m_action = order.getSide().name();
 		ibOrder.m_orderType = IBUtil.getIBOrderType(order);
 		ibOrder.m_transmit = true;
 
 		ibOrder.m_totalQuantity = (int) order.getQuantity().intValue();
 
 		//set the limit price if order is a limit order or stop limit order
 		if (order.getOrderType().equals(OrderType.Limit)) {
 			ibOrder.m_lmtPrice = order.getPrice().doubleValue();
 		}
 
 		//set the stop price if order is a stop order or stop limit order
 		//if (order instanceof StopOrderInterface) {
 		//	ibOrder.m_auxPrice = ((StopOrderInterface) order).getStop().doubleValue();
 		//}
 
 		// progapate the order to all corresponding esper engines
 		//propagateOrder(order);
 
 		// place the order through IBClient
 		client.placeOrder(Integer.parseInt(order.getOrderID().getValue()), contract, ibOrder);
 
 		log.info("placed or modified order: Id " + order.getOrderID().toString());
 	}
 	
 	public void cancelOrder(OrderSingle order) throws Exception {
 	
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected");
 			return;
 		}
 	
 		client.cancelOrder(Integer.parseInt(order.getOrderID().getValue()));
 
 		log.info("requested order cancallation for order: " + order.getOrderID().toString());
 	}
 
 	public void cancelOrderById(int orderId) throws Exception {
 		
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected");
 			return;
 		}
 	
 		client.cancelOrder(orderId);
 
 		log.info("requested order cancallation for order: " + orderId);
 	}
 	
 	public void cancelAllOpenOrders() throws Exception {
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected");
 			return;
 		}
 	
 		Map<Integer, Order> openOrders = IBOrderService.getInstance().getOpenOrders();
 		for(Integer orderId: openOrders.keySet()) {
 			cancelOrderById(orderId.intValue());
 		}
 	}
 	
 	public void reqOpenOrders() throws Exception {
 		
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected");
 			return;
 		}
 	
 		//client.reqOpenOrders();
 		//log.info("reqOpenOrders: ");
 		
 		client.reqAllOpenOrders();
 		log.info("reqAllOpenOrders: ");
 	}
 	
 	public void reqAccountUpdates() throws Exception {
 		
 		if (!(client.getIbAdapter().getState().equals(ConnectionState.READY) || client.getIbAdapter().getState().equals(ConnectionState.SUBSCRIBED))) {
 			log.error("transaction cannot be executed, because IB is not connected");
 			return;
 		}
 		
 		client.reqAccountUpdates(true, accountCode);
 		log.info("reqAccountUpdates: ");
 	}
 }
