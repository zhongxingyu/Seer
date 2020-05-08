 package org.marketcetera.marketdata.interactivebrokers;
 
 import java.math.BigDecimal;
 import java.util.Date;
 import java.util.Hashtable;
 import java.util.concurrent.atomic.AtomicInteger;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.apache.log4j.Logger;
 
 import com.ib.client.Contract;
 import com.ib.client.ContractDetails;
 import com.ib.client.EClientSocket;
 import com.ib.client.EWrapper;
 import com.ib.client.Execution;
 import com.ib.client.Order;
 import com.ib.client.OrderState;
 import com.ib.client.UnderComp;
 
 public class IBFeedManager implements EWrapper {
 
 	private static Logger log = Logger.getLogger(IBFeedManager.class);
 	
 	private EClientSocket clientSocket;
 	
 	InteractiveBrokersFeed feed;
 	private Hashtable<Integer, String> requestSymbols=new Hashtable<Integer, String>();
 	private Hashtable<String, LatestMarketData> latestData=new Hashtable<String, LatestMarketData>();
 	
 	private final AtomicInteger requestCounter=new AtomicInteger(1);
 	private final AtomicLong tickCounter=new AtomicLong(0);
 	private boolean histRequestFlag = false;
 	private Contract histReqContract;
 	
 	public enum EventType {
 		Bid, Offer, Trade 
 	}
 	
 	public IBFeedManager(InteractiveBrokersFeed inFeed) {
 		feed=inFeed;
 		clientSocket=new EClientSocket(this);
 	}
 	
 	public boolean isConnected() {
 		return clientSocket.isConnected();
 	}
 	public void connect(String inHost, int inPort, int inClientId) {
 		System.out.println("xxxx IBFeedManager.connect: inHost=" + inHost + ", port=" + inPort + ", inClientId=" + inClientId);
 		clientSocket.eConnect(inHost, inPort , inClientId);
 	}
 	public void disconnect() {
 		clientSocket.eDisconnect();
 	}
 	public EClientSocket getClientSocket() {
 		return clientSocket;
 	}
 	
 	public void requestMarketData(InteractiveBrokersMessage inMessage, String inGenericTickList, boolean inSnapshot) {
 		
 		for (Contract contract : inMessage.getContracts()) {
 			int tickerId=requestCounter.getAndIncrement();
 			
 			requestSymbols.put(tickerId, contract.m_symbol);
 			getLatestMarketData(contract.m_symbol);
 
 			String logStr = String.format("XXXX IBFeedManager->requestMarketData() contract: %s, id: %d, type: %s \n", 
 					contract.m_symbol, tickerId, contract.m_secType);
 			log.info(logStr);
 			
 			System.out.println("XXXX: " + logStr);
 			clientSocket.reqMktData(tickerId, contract, inGenericTickList, inSnapshot);
 			
 			// pause
 			try {
 				Thread.sleep(100);
 			}
 			catch(Exception e) {
 				e.printStackTrace();
 			}
 			
 			// try historical data files yyyymmdd hh:mm:ss tmz
 			//clientSocket.reqHistoricalData(tickerId, contract, "20111206 16:00:00", "1 D", "1 min", "TRADES", 1, 1);
 			//clientSocket.reqHistoricalData(tickerId, contract, "20111206 16:00:00", "7200 S", "5 secs", "TRADES", 1, 1); // good
 			//clientSocket.reqHistoricalData(tickerId, contract, "20111209 16:00:00", "1800 S", "1 secs", "TRADES", 1, 1); // good
 			//clientSocket.reqHistoricalData(tickerId, contract, "20111205 16:00:00", "3600 S", "1 sec", "BID", 1, 1);
 			//void reqHistoricalData (int id, Contract contract, String endDateTime, String durationStr, String barSizeSetting, String whatToShow, int useRTH, int formatDate)
 			
 			histReqContract = contract;
 		}
 		
 		// have a backgroud thread request historical market data to keep the session alive
 	/*	if (histRequestFlag==false) {
 			log.info("start requesting reqHistoricalData: " +histReqContract.m_symbol);
 			ScheduledThreadPoolExecutor stpe = new ScheduledThreadPoolExecutor(1);
 			
 			stpe.scheduleAtFixedRate(new Runnable() {
 				@Override
 				public void run() {
 					int tickerId=requestCounter.getAndIncrement();
 					requestSymbols.put(tickerId, histReqContract.m_symbol);
 					
 					clientSocket.reqHistoricalData(tickerId, histReqContract, "20111206 16:00:00", "1 D", "1 min", "TRADES", 1, 1);
 					log.info("xxxx reqHistoricalData successful. " +histReqContract.m_symbol);
 				}		
 				
 			}, 0, 1, TimeUnit.MINUTES);
 			
 			histRequestFlag = true;
 			log.info("xxxx reqHistoricalData DONE. ");
 		}
 	*/	
 	}
 	@Override
 	public void accountDownloadEnd(String accountName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void bondContractDetails(int reqId, ContractDetails contractDetails) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void contractDetails(int reqId, ContractDetails contractDetails) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void contractDetailsEnd(int reqId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void currentTime(long time) {
 		// TODO Auto-generated method stub
 		System.out.println("xxxx IBFeedManager.currentTime: " + new Date(time));
 	}
 
 	@Override
 	public void deltaNeutralValidation(int reqId, UnderComp underComp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void execDetails(int reqId, Contract contract, Execution execution) {
 		// TODO Auto-generated method stub
 		System.out.println("xxxx IBFeedManager.execDetails(int reqId=" + reqId + ", Contract contract=" + contract.toString() + ", Execution execution=" + execution.toString() + ")" ); 
 	}
 
 	@Override
 	public void execDetailsEnd(int reqId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void fundamentalData(int reqId, String data) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void historicalData(int reqId, String date, double open,
 			double high, double low, double close, int volume, int count,
 			double WAP, boolean hasGaps) {
 		
 		// TODO Auto-generated method stub
 		//log.info(String.format("xxxx historicalData: reqId=%d, Symbol=%s, date=%s,  open=%f, close=%f, volume=%d, count=%d, WAP=%f, hadGaps=%s \n",
 		//		reqId, requestSymbols.get(reqId), date, open, close, volume, count, WAP, String.valueOf(hasGaps)));
 		
 	}
 
 	@Override
 	public void managedAccounts(String accountsList) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void nextValidId(int orderId) {
 		// TODO Auto-generated method stub
 		System.out.println("xxxx IBFeedManager.nextValidId: " + orderId);
 	}
 
 	@Override
 	public void openOrder(int orderId, Contract contract, Order order,
 			OrderState orderState) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void openOrderEnd() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void orderStatus(int orderId, String status, int filled,
 			int remaining, double avgFillPrice, int permId, int parentId,
 			double lastFillPrice, int clientId, String whyHeld) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void realtimeBar(int reqId, long time, double open, double high,
 			double low, double close, long volume, double wap, int count) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void receiveFA(int faDataType, String xml) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void scannerData(int reqId, int rank,
 			ContractDetails contractDetails, String distance, String benchmark,
 			String projection, String legsStr) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void scannerDataEnd(int reqId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void scannerParameters(String xml) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void tickEFP(int tickerId, int tickType, double basisPoints,
 			String formattedBasisPoints, double impliedFuture, int holdDays,
 			String futureExpiry, double dividendImpact, double dividendsToExpiry) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void tickGeneric(int tickerId, int tickType, double value) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	public void tickOptionComputation(int tickerId, int field,
 			double impliedVol, double delta, double modelPrice,
 			double pvDividend) {
 		// TODO Auto-generated method stub
 		log.info("xxxx compute tickOpeion.");
 			
 	}
 	private String getSymbolFromTickerId(int tickerId) {
 		
 		if (requestSymbols.containsKey(tickerId)) {
 			return requestSymbols.get(tickerId);
 		} else {
 			return "";
 		}
 	}
 	private LatestMarketData getLatestMarketData(String symbol) {
 		if (!latestData.containsKey(symbol.toUpperCase())) {
 			latestData.put(symbol.toUpperCase(), new LatestMarketData(symbol.toUpperCase()));
 		} 
 		return latestData.get(symbol.toUpperCase());
 	}
 	@Override
 	public void tickPrice(int tickerId, int field, double inPrice, int canAutoExecute) {
 
 		this.tickCounter.getAndIncrement();
 		
 		if (this.tickCounter.get() % 1000000 ==1) {
 			log.info("<- from IB (tickPrice) tickCounter: " +this.tickCounter.get());
 			log.info(String.format("xxxxxxxxxxxxxx int tickerId=%d, int field=%d, double inPrice=%f, int canAutoExe=%d \n", tickerId, field, inPrice, canAutoExecute));	
 		}
 		
 		BigDecimal price=new BigDecimal(inPrice);
 		
 		String symbol=getSymbolFromTickerId(tickerId);
 
 		// This is a little nutty, but because the IB api does not give price and size at the same time
 		// we need to keep track of the latest prics and size per symbol.
 		LatestMarketData latest=getLatestMarketData(symbol);
 		// last		
 		if (field==4) {
 			latest.getLastestTrade().setPrice(price);
 			feed.ibDataReceived(symbol, price, latest.getLastestTrade().getSize(), EventType.Trade);		
 		} else if (field==1) { // bid
 			latest.getLatestBid().setPrice(price);
 			feed.ibDataReceived(symbol, price, latest.getLatestBid().getSize(), EventType.Bid);			
 		} else if (field==2) { // offer
 			latest.getLatestOffer().setPrice(price);
 			feed.ibDataReceived(symbol, price, latest.getLatestOffer().getSize(), EventType.Offer);
 		}
 		
 	}
 
 	@Override
 	public void tickSize(int tickerId, int field, int inSize) {	
 		
 		this.tickCounter.getAndIncrement();
 		if (this.tickCounter.get() % 1000 ==1) {
 			log.info("<- from IB (tickSize) tickCounter: " +this.tickCounter.get());
 		}
 
 		BigDecimal size=new BigDecimal(inSize);
 		
 		String symbol=getSymbolFromTickerId(tickerId);
 		LatestMarketData latest=getLatestMarketData(symbol);
 		
 		if (field==0) {
 			latest.getLatestBid().setSize(size);
 			feed.ibDataReceived(symbol, latest.getLatestBid().getPrice(), size, EventType.Bid);
 		} else if (field==3) {
 			latest.getLatestOffer().setSize(size);
 			feed.ibDataReceived(symbol, latest.getLatestOffer().getPrice(), size, EventType.Offer);
 		} else if (field==5) {
 			latest.getLastestTrade().setSize(size);
 			feed.ibDataReceived(symbol, latest.getLastestTrade().getPrice(), size, EventType.Trade);
 		}		
 	}
 
 	@Override
 	public void tickSnapshotEnd(int reqId) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void tickString(int tickerId, int tickType, String value) {
 		// TODO Auto-generated method stub
 		System.out.println(String.format("xxxx IBFeedManager.tickString(int tickerId=%d, int tickType=%d, String value=%s)", tickerId, tickType, value));
 	}
 
 	@Override
 	public void updateAccountTime(String timeStamp) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateAccountValue(String key, String value, String currency,
 			String accountName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateMktDepth(int tickerId, int position, int operation,
 			int side, double price, int size) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateMktDepthL2(int tickerId, int position,
 			String marketMaker, int operation, int side, double price, int size) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updateNewsBulletin(int msgId, int msgType, String message,
 			String origExchange) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void updatePortfolio(Contract contract, int position,
 			double marketPrice, double marketValue, double averageCost,
 			double unrealizedPNL, double realizedPNL, String accountName) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void connectionClosed() {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public void error(Exception e) {
 		// TODO Auto-generated method stub
 		System.out.println(String.format("xxxx IBFeedManager.error(Exception e=%s)", e.getMessage()));		
 	}
 
 	@Override
 	public void error(String str) {
 		// TODO Auto-generated method stub
 		System.out.println(String.format("xxxx IBFeedManager.error(String str=%s)", str));
 	}
 
 	@Override
 	public void error(int id, int errorCode, String errorMsg) {
 		// TODO Auto-generated method stub
 		log.info(String.format("xxxx error(int id=%d, int errorCode=%d, String errorMsg=%s)", id, errorCode, errorMsg));
 		System.out.println(String.format("xxxx IBFeedManager.error(int id=%d, int errorCode=%d, String errorMsg=%s)", id, errorCode, errorMsg));
 	}
 
 	@Override
 	public void tickOptionComputation(int arg0, int arg1, double arg2,
 			double arg3, double arg4, double arg5, double arg6, double arg7,
 			double arg8, double arg9) {
 		// TODO Auto-generated method stub
 		
 	}
 	
 }
