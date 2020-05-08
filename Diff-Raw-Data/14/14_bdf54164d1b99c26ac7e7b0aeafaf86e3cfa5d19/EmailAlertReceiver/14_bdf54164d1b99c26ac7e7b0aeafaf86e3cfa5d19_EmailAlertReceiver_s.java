 package com.myrontuttle.fin.trade.alertreceivers;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import com.myrontuttle.fin.trade.api.AlertAction;
 import com.myrontuttle.fin.trade.api.AlertReceiverService;
 import com.myrontuttle.fin.trade.api.TradeStrategy;
 
 /**
  * Receives alerts sent via email
  * @author Myron Tuttle
  */
 public class EmailAlertReceiver implements AlertReceiverService {
 
 	private final static int NUM_THREADS = 1;
 	private final static int PERIOD = 60;
 	private final static int IMAPS_PORT = 993;
 	
 	private final TradeStrategy tradeStrategy;
 	private List<AlertAction> alertActionList;
 
 	private ScheduledExecutorService ses;
 	private ScheduledFuture<?> sf;
 
 	EmailAlertReceiver(TradeStrategy tradeStrategy) {
 		this.tradeStrategy = tradeStrategy;
 		this.alertActionList = new LinkedList<AlertAction>();
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#startReceiving(HashMap<String, String>)
 	 */
 	@Override
 	public boolean startReceiving(HashMap<String, String> connectionDetails) {
 		String host = connectionDetails.get("host");
 		String user = connectionDetails.get("user");
 		String password = connectionDetails.get("password");
 		
 		if (host == null || user == null || password == null) {
 			return false;
 		} else {
 			int period, port;
 			try {
 				period = Integer.parseInt(connectionDetails.get("period"));
 			} catch (NumberFormatException nfe) {
 				period = PERIOD;
 			}
 			try {
 				port = Integer.parseInt(connectionDetails.get("port"));
 			} catch (NumberFormatException nfe) {
 				port = IMAPS_PORT;
 			}
 	    	MailRetriever mailRetriever = new MailRetriever(this, host, port, user, password);
 	        this.sf = ses.scheduleAtFixedRate(mailRetriever, 0, period, TimeUnit.SECONDS);
 
 	        this.ses = Executors.newScheduledThreadPool(NUM_THREADS);
 	        return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#stopReceiving()
 	 */
 	@Override
 	public boolean stopReceiving() {
 
        // Close scheduled service
        sf.cancel(true);
        ses.shutdown();
        
		return false;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#watchFor(com.myrontuttle.adaptivetrader.AlertAction[])
 	 */
 	@Override
 	public void watchFor(AlertAction... alertTrades) {
 		for (AlertAction alertTrade : alertTrades) {
 			alertActionList.add(alertTrade);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#stopWatchingFor(com.myrontuttle.adaptivetrader.AlertAction)
 	 */
 	@Override
 	public void stopWatchingFor(AlertAction alertAction) {
 		alertActionList.remove(alertAction);
 	}
 	
 	/**
 	 * Tests an incoming message to see if it matches one of the existing alert conditions
 	 * If it does, tells the MoneyManager to execute the associated trade or order on the 
 	 * portfolio with portfolioId
 	 * @param condition The alert condition to match
 	 * @return The number of time the condition matched an existing alert's condition
 	 */
 	@Override
 	public int matchAlert(String condition) {
 		int matches = 0;
 		for (AlertAction action : alertActionList) {
 			if (action.alertMatches(condition)) {
 				System.out.println("Alert Matched");
 				try {
 					tradeStrategy.takeAction(action);
 				} catch (Exception e) {
 					System.out.println("Unable to affect trade." + e.getMessage());
 				}
 				alertActionList.remove(action);
 				matches++;
 			}
 		}
 		return matches;
 	}
 }
