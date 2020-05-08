 package com.fhx.service.ib.order;
 
 import java.util.concurrent.BlockingQueue;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 import org.marketcetera.trade.OrderSingle;
 
 import com.fhx.service.ib.marketdata.IBEventServiceImpl;
 import com.fhx.service.ib.marketdata.IBOrderService;
 
 public class IBOrderSender implements Runnable {
 	
 	private static Logger log = Logger.getLogger(IBOrderService.class);
 	
 	private IBOrderService ors = IBOrderService.getInstance();
 	private IBOrderSenderHelper orderHelper = IBOrderSenderHelper.getInstance();
 	private BlockingQueue<OrderSingle> m_orderQ;
 	
 	private static final ExecutorService sNotifierPool = Executors.newCachedThreadPool();
 	
 	public IBOrderSender(BlockingQueue<OrderSingle> q) {
 		m_orderQ = q;
 		
 		IBEventServiceImpl.getEventService().addOrderEventListener(ors);
 		ors.handleInit();			
 		ors.handleInitWatchlist();
 	}
 	
 	public void run() {
 		log.info("Starting IBOrderSender thread");
 		
 		try {
 			orderHelper.reqOpenOrders();
 			orderHelper.reqAccountUpdates();
 		} catch (Exception e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 			System.exit(1);
 		}
 		
 		while (true) {
 			try {
 				final OrderSingle order = m_orderQ.take();
 				log.info("took next order off q: " + order.getSide()+" "+order.getSymbol()+" "+order.getQuantity()+"@"+order.getPrice());
 				
 				// hand the notification chore to a thread from the thread pool
 		        sNotifierPool.submit(new Runnable() {
 		               public void run() {
 		            	   try {
 		            		   orderHelper.sendOrModifyOrder(order);
 		            	   } catch (Exception e) {
 		            		   // TODO Auto-generated catch block
 		            		   e.printStackTrace();
 		            	   }
 		               }
 		        });
 				
 				log.info("sent order to IB");
 				
 			} catch (InterruptedException e) {
 				log.error("thread interrupted error " + e.getMessage() );
 				//System.exit(1);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				log.error("thread exception " + e.getMessage() );
 				e.printStackTrace();
 			} 
 		}
 	}
 	
 	
 }
