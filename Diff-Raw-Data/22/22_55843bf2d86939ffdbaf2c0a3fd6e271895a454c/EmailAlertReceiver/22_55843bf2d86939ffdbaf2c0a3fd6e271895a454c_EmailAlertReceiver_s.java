 package com.myrontuttle.fin.trade.alertreceivers;
 
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.Executors;
 import java.util.concurrent.ScheduledExecutorService;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import com.myrontuttle.fin.trade.api.AlertAction;
 import com.myrontuttle.fin.trade.api.AlertReceiver;
 import com.myrontuttle.fin.trade.api.TradeStrategy;
 
 /**
  * Receives alerts sent via email
  * @author Myron Tuttle
  */
 public class EmailAlertReceiver implements AlertReceiver {
 
 	public final static String NAME = "EmailAlert";
 	public final static String HOST = "Host";
 	public final static String PORT = "Port";
 	public final static String USER = "User";
 	public final static String PASS = "Password";
 	public final static String PERIOD = "Period";
 	
 	private final static int NUM_THREADS = 1;
 	private final static int DEFAULT_PERIOD = 60;
 	private final static int IMAPS_PORT = 993;
 	private final static int IMAP_PORT = 143;
 	private final static int POP_PORT = 110;
 	
	private final static String GOOGLE_HOST = "imap.google.com";
 
 	private final ScheduledExecutorService ses;
 	private ScheduledFuture<?> sf;
 	
 	private TradeStrategy tradeStrategy;
 	private List<AlertAction> alertActionList;
 	
 	private boolean receiving;
 	private MailRetriever mailRetriever;
 
 	public EmailAlertReceiver() {
 		this.alertActionList = new LinkedList<AlertAction>();
 		this.receiving = false;
         this.ses = Executors.newScheduledThreadPool(NUM_THREADS);
 	}
 
 	@Override
 	public String getName() {
 		return NAME;
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#startReceiving(TradeStrategy, String, HashMap<String, String>)
 	 */
 	@Override
 	public boolean startReceiving(TradeStrategy tradeStrategy,
 									HashMap<String, String> connectionDetails) {
 		this.tradeStrategy = tradeStrategy;
 		String host = connectionDetails.get(HOST);
 		String user = connectionDetails.get(USER);
 		String password = connectionDetails.get(PASS);
 		
 		if (host == null || user == null || password == null) {
 			return false;
 		} else {
 			int period, port;
 			try {
 				period = Integer.parseInt(connectionDetails.get(PERIOD));
 			} catch (NumberFormatException nfe) {
 				period = DEFAULT_PERIOD;
 			}
 			try {
 				port = Integer.parseInt(connectionDetails.get(PORT));
 			} catch (NumberFormatException nfe) {
				if (host.equals(GOOGLE_HOST)) {
 					port = IMAPS_PORT;
 				} else if (host.contains("imap")) {
 					port = IMAP_PORT;
 				} else {
 					port = POP_PORT;
 				}
 			}
 			if (!receiving) {
 		    	this.mailRetriever = new MailRetriever(this, host, port, user, password);
 		    	this.sf = ses.scheduleAtFixedRate(mailRetriever, 0, period, TimeUnit.SECONDS);
 				
 			} else if ((!host.equals(mailRetriever.getHost()) ||
 				port != mailRetriever.getPort() ||
 				!user.equals(mailRetriever.getUser()) ||
 				!password.equals(mailRetriever.getPassword()) )) {
 				this.sf.cancel(true);
 				this.mailRetriever = new MailRetriever(this, host, port, user, password);
 			    this.sf = ses.scheduleAtFixedRate(mailRetriever, 0, period, TimeUnit.SECONDS);
 			}
 	        this.receiving = true;
 	        return true;
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#stopReceiving()
 	 */
 	@Override
 	public boolean stopReceiving() {
 
 		try {
 	        // Close scheduled service
 	        sf.cancel(true);
 	        ses.shutdown();
 	        receiving = false;
 		} catch (SecurityException se) {
 			System.out.println("Unable to stop receiving email. " + se.getMessage());
 			return false;
 		}
 		return true;
 	}
 	
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#watchFor(com.myrontuttle.adaptivetrader.AlertAction[])
 	 */
 	@Override
 	public void watchFor(AlertAction... alertActions) {
 		for (AlertAction alertAction : alertActions) {
 			alertActionList.add(alertAction);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#stopWatchingFor(com.myrontuttle.adaptivetrader.AlertAction)
 	 */
 	@Override
 	public void stopWatchingFor(AlertAction alertAction) {
 		alertActionList.remove(alertAction);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.myrontuttle.adaptivetrader.AlertReceiver#stopWatchingAll(String)
 	 */
 	@Override
 	public void stopWatchingAll(String userId) {
 		for (AlertAction alertAction : alertActionList) {
 			if (alertAction.getUserId().equals(userId)) {
 				alertActionList.remove(alertAction);
 			}
 		}
 	}
 	
 	/**
 	 * Tests an incoming message to see if it matches one of the existing alert conditions
 	 * If it does, tells the MoneyManager to execute the associated trade or order on the 
 	 * portfolio with portfolioId
 	 * @param userId
 	 * @param condition The alert condition to match
 	 * @return The number of time the condition matched an existing alert's condition
 	 */
 	@Override
 	public int matchAlert(String condition) {
 		int matches = 0;
 		for (AlertAction action : alertActionList) {
 			if (action.getAlert().getCondition().matches(condition)) {
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
