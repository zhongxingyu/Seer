 package agent;
asd
 import environment.*;
 import environment.Order.BuySell;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Random;
 
 import log.AgentLogger;
 import log.Logging;
 
 import setup.HighFrequencyTradingBehavior;
 import setup.MarketRules;
 import utilities.NoOrdersException;
 import utilities.Utils;
 
 public abstract class HFT implements HighFrequencyTradingBehavior, Logging, MarketRules, EntityThatCanSubmitOrders {
 	public static long nHFTs = 0;
 	protected long id;
 	protected long cash;
 	protected long wakeupTime;
 	protected long thinkingTime;
 	protected HashMap<Orderbook, Order> standingBuyOrders;
 	protected HashMap<Orderbook, Order> standingSellOrders;
 	protected ArrayList<Orderbook> orderbooks;
 	protected HashMap<Market, Integer> latencyToMarkets;
 	protected ArrayList<TransactionReceipt> receipts;
 	protected HashMap<Stock, Long> ownedStocks;
 	protected HashMap<Stock, Integer> stocksInStandingOrders;
 	protected ArrayList<Market> markets;
 	protected ArrayList<Stock> stocks;
 
 	protected long nSubmittedBuyOrders;
 	protected long nSubmittedSellOrders;
 	protected long nSubmittedCancellations;
 	protected long nFulfilledBuyOrders;
 	protected long nFulfilledSellOrders;
 
 	protected ArrayList<Order> orderHistory;
 
 	protected abstract long getWaitingTime();
 
 	public abstract void storeMarketInformation() throws NoOrdersException;
 
 	public abstract boolean executeStrategyAndSubmit();
 
 	public AgentLogger eventlog;
 	public AgentLogger datalog;
 
 	public HFT(long wealth, int[] stockIDs, int[] startingPortfolio, int[] marketIDs, int[] latencies) {
 		this.cash = wealth;
 		this.wakeupTime = World.getCurrentRound();
 		this.initialize(stockIDs, marketIDs, latencies);
 		this.setPortfolio(startingPortfolio);
 		World.addNewAgent(this);
 	}
 
 	public HFT(int[] stockIDs, int[] marketIDs, int[] latencies) {
 		if (randomStartWealth) {
 			this.cash = getRandomInitialTraderWealth();
 		} else {
 			this.cash = constantStartWealth;
 		}
 		this.wakeupTime = World.getCurrentRound();
 		this.initialize(stockIDs, marketIDs, latencies);
 		World.addNewAgent(this);
 	}
 
 	private void initialize(int[] stockIDs, int[] marketIDs, int[] marketLatencies) {
 		this.id = nHFTs;
 		nHFTs++;
 		this.nSubmittedBuyOrders = 0;
 		this.nSubmittedSellOrders = 0;
 		this.nFulfilledBuyOrders = 0;
 		this.nFulfilledSellOrders = 0;
 		this.receipts = new ArrayList<TransactionReceipt>();
 		this.standingBuyOrders = new HashMap<Orderbook, Order>(); // At present,
 																	// each
 																	// agent can
 																	// only
 																	// maintain
 																	// a single
 																	// order on
 																	// each side
 																	// of the
 																	// book
 		this.standingSellOrders = new HashMap<Orderbook, Order>();
 		this.orderbooks = new ArrayList<Orderbook>();
 		this.ownedStocks = new HashMap<Stock, Long>();
 		this.latencyToMarkets = new HashMap<Market, Integer>();
 		this.markets = new ArrayList<Market>();
 		this.stocks = new ArrayList<Stock>();
 
 		this.buildLatencyHashmap(marketIDs, marketLatencies);
 		this.markets.addAll(this.latencyToMarkets.keySet());
 		this.buildOrderbooksHashMap(marketIDs, stockIDs);
 		this.buildStocksList(stockIDs);
 		this.initializePortfolio();
 		if (keepOrderHistory) {
 			this.orderHistory = new ArrayList<Order>();
 		}
 	}
 
 	private void buildLatencyHashmap(int[] marketIDs, int[] marketLatencies) {
 		/*
 		 * Initalize latency HashMap
 		 */
 		if (!(marketIDs.length == marketLatencies.length)) {
 			World.errorLog.logError("Length of markets array and length of latenceis array must be the same.");
 		}
 
 		for (int i = 0; i < marketIDs.length; i++) {
 			this.latencyToMarkets.put(World.getMarketByNumber(i), marketLatencies[i]);
 		}
 	}
 
 	private void buildOrderbooksHashMap(int[] marketIDs, int[] stockIDs) {
 		for (int market : marketIDs) {
 			for (int stock : stockIDs) {
 				this.orderbooks.add(World.getOrderbookByNumbers(stock, market));
 			}
 		}
 	}
 
 	private void buildStocksList(int[] stockIDs) {
 		for (int i : stockIDs) {
 			Stock stock = World.getStockByNumber(i);
 			if (!this.stocks.contains(stock)) {
 				this.stocks.add(stock);
 			} else {
 				World.errorLog.logError("Problem when building stocks ArrayList in HFT agent (buildStockList): Tried to add the same stock more than one.");
 			}
 		}
 	}
 
 	private void initializePortfolio() {
 		/*
 		 * Initializes portfolio with constant or random amount as specified in
 		 * the interface.
 		 */
 		for (Stock stock : this.stocks) {
 			if (HighFrequencyTradingBehavior.randomStartStockAmount) {
 				World.errorLog.logError("Random start stock amount not implemented yet!");
 			} else {
 				this.ownedStocks.put(stock, HighFrequencyTradingBehavior.startStockAmount);
 			}
 		}
 	}
 
 	public void setPortfolio(int[] portfolio) {
 		/*
 		 * Used to give the agent a ustom portfolio
 		 */
 		if (portfolio.length != this.stocks.size()) {
 			World.errorLog.logError("Error when creating custom specified portfolio. Size of new portfolio does not match size of agetn stock array.");
 		}
 	}
 
 	// private void getRandomPortfolio(int[] stockIDs){
 	// int[] portfolio = new int[stockIDs.length];
 	// for(long i = 0; i < stockIDs.length; i++){
 	// portfolio[i] = Utils.getNonNegativeGaussianInteger(, std)
 	// }
 	// }
 
 	/*
 	 * World calls this function when it's time for the agent to trade
 	 */
 
 	public void requestMarketInformation() {
 		/*
 		 * Updates the agents wakeup time, which depends on the agents strategy
 		 * (that is, which markets he wants information from).
 		 */
 		// World.registerAgentAsWaiting(this);
 		this.wakeupTime = World.getCurrentRound() + this.getWaitingTime();
 	}
 
 	public void receiveMarketInformation() throws NoOrdersException {
 		/*
 		 * Method that deals with flow -Update agent wakeup time by adding
 		 * thinking time. -Read the received information and update internal
 		 * data structures
 		 */
 		World.agentRequestMarketInformation(this);
 		this.wakeupTime += this.thinkingTime;
 		try {
 			this.storeMarketInformation();
 		} catch (NoOrdersException e) {
 			throw e;
 		}
 	}
 
 //	private void updatePortfolio(TransactionReceipt receipt) {
 //		Stock stock = receipt.getStock();
 //		long volumeChange = receipt.getSignedVolume();
 //		long ownedAmountOfStockAfterTransaction = this.ownedStocks.get(stock) + volumeChange;
 //
 //		if (ownedAmountOfStockAfterTransaction < 0) {
 //			// if(!(receipt.getOriginalOrder().getBuySell() ==
 //			// Order.BuySell.SELL)) {
 //			// World.errorLog.logError("Error, because order volume was less than zero, but not a SELL order");
 //			// }
 //			if (MarketRules.allowsShortSelling) {
 //				/*
 //				 * If shortselling is allowed, the transaction is simply carried
 //				 * through
 //				 */
 //				this.receipts.add(receipt);
 //				this.ownedStocks.put(stock, ownedAmountOfStockAfterTransaction);
 //				World.warningLog.logOnelineEvent(String.format("Agent %s shorted a volume of %s of stock number %s", receipt.getOwner().getID(), receipt.getAbsoluteVolume(), receipt.getOriginalOrder().getStock().getID()));
 //			} else {
 //				/*
 //				 * If not, it has to be handled somehow.
 //				 */
 //				World.ruleViolationsLog.logShortSelling(receipt);
 //				World.errorLog.logError("Short selling was not allowed by market rules, but happened. Handling of this situation is not implemented yet");
 //			}
 //		}
 //		// else {
 //		// this.receipts.add(receipt);
 //		// this.ownedStocks.put(stock, ownedAmountOfStockAfterTransaction);
 //		// }
 //	}
 	
 	private void updatePortfolio(Stock stock, long signedVolume) {
 		long currentlyHolds = this.ownedStocks.get(stock);
 		this.ownedStocks.put(stock, currentlyHolds + signedVolume);
 	}
 	
 	public void receiveTransactionReceipt(TransactionReceipt receipt) {
 		this.eventlog.logAgentAction(String.format("Agent %s received a receipt for a %s order, id: %s", this.id, receipt.getBuySell(), receipt.getFilledOrder().getID()));
 		
 		Stock stock = receipt.getStock();
 //		long signedTransactionVolume = receipt.getSignedVolume();
 //		long transactionVolume = receipt.getUnsignedVolume();
 //		long receiptTotal = receipt.getAbsoluteTotal();
 		BuySell buysell = receipt.getFilledOrder().getBuySell();
 		
 		if(buysell == BuySell.BUY) {
 			/*
 			 * The agent is required to buy stocks, so we check to see if he has enough cash.
 			 */
 			this.nFulfilledBuyOrders += 1;
 			if (this.standingBuyOrders.containsKey(receipt.getOrderbook())) {
 				this.updateStandingOrder(receipt);
 			} else {
 				this.dealWithOrderForRemovedOrder(receipt);
 			}
 			
 			if(receipt.getAbsoluteTotal() >= this.cash) {
 				/*
 				 * The agent has to borrow money
 				 */
 				long amountToBorrow = Math.abs(this.cash - receipt.getAbsoluteTotal());
 				this.borrowCash(amountToBorrow);
 			} 
 			
 			if(this.cash >= receipt.getAbsoluteTotal()) {
 				/*
 				 * Agent has enough cash, so he adds the stocks to his portfolio
 				 */
 				this.updateCash(receipt.getSignedTotal());
 				this.updatePortfolio(stock, receipt.getSignedVolume());
 			} else {
 				System.out.println(this.cash);
 				System.out.println(receipt.getAbsoluteTotal());
 				World.errorLog.logError("Agent should be able to have enough cash by now to borrow stocks, but he didn't...");
 			}
 		} else if(buysell == Order.BuySell.SELL) {
 			
 			this.nFulfilledSellOrders += 1;
 			if (this.standingSellOrders.containsKey(receipt.getOrderbook())) {
 				this.updateStandingOrder(receipt);
 			} else {
 				this.dealWithOrderForRemovedOrder(receipt);
 			}
 			/*
 			 * The agent is required to sell stocks, so we check to see if he has them
 			 */
 			if(receipt.getUnsignedVolume() >= this.ownedStocks.get(stock)) {
 				/*
 				 * Agent does not have enough stocks, so he has to shortsell
 				 */
 				if(!MarketRules.allowsShortSelling) {
 					World.ruleViolationsLog.logShortSelling(receipt);
 					World.errorLog.logError("Short selling was not allowed by market rules, but happened. Handling of this situation is not implemented yet");
 				} else {
 					this.borrowStocks(stock, receipt.getSignedVolume());
 				}
 			}
 			/*
 			 * Both in the case of the agent having enough stocks or not, he has to do the same thing.
 			 */
 			this.updatePortfolio(stock, receipt.getSignedVolume());
 			this.updateCash(receipt.getSignedTotal());
 		}
 	}
 	
 	private void borrowStocks(Stock stock, long signedVolumeToBorrow) {
 		/*
 		 * Ath the moment, this function does nothing apart from updating the portfolio 
 		 */
 		this.updatePortfolio(stock, signedVolumeToBorrow);
 	}
 	
 //	private void updatePortfolio(TransactionReceipt receipt) {
 //		Stock stock = receipt.getStock();
 //		long volumeChange = receipt.getSignedVolume();
 //		long ownedAmountOfStockAfterTransaction = this.ownedStocks.get(stock) + volumeChange;
 //
 //		if (ownedAmountOfStockAfterTransaction < 0) {
 //			// if(!(receipt.getOriginalOrder().getBuySell() ==
 //			// Order.BuySell.SELL)) {
 //			// World.errorLog.logError("Error, because order volume was less than zero, but not a SELL order");
 //			// }
 //			if (MarketRules.allowsShortSelling) {
 //				/*
 //				 * If shortselling is allowed, the transaction is simply carried
 //				 * through
 //				 */
 //				this.receipts.add(receipt);
 //				this.ownedStocks.put(stock, ownedAmountOfStockAfterTransaction);
 //				World.warningLog.logOnelineEvent(String.format("Agent %s shorted a volume of %s of stock number %s", receipt.getOwner().getID(), receipt.getAbsoluteVolume(), receipt.getStock().getID()));
 //			} else {
 //				/*
 //				 * If not, it has to be handled somehow.
 //				 */
 //				World.ruleViolationsLog.logShortSelling(receipt);
 //				World.errorLog.logError("Short selling was not allowed by market rules, but happened. Handling of this situation is not implemented yet");
 //			}
 //		}
 //		// else {
 //		// this.receipts.add(receipt);
 //		// this.ownedStocks.put(stock, ownedAmountOfStockAfterTransaction);
 //		// }
 //	}
 
 		
 //	public void receiveTransactionReceipt(TransactionReceipt receipt) {
 //		/*
 //		 * When the agent receives a receipt, he knows that his order has been
 //		 * filled, so he
 //		 */
 //		// this.datalog.printOrderHistoryIdString();
 //		this.eventlog.logAgentAction(String.format("Agent %s received a receipt for a %s order, id: %s", this.id, receipt.getBuySell(), receipt.getFilledOrder().getID()));
 //		
 //		
 //		this.updatePortfolio(receipt);
 //		this.updateCash(receipt.getSignedTotal());
 //
 //		if (receipt.getBuySell() == BuySell.BUY) {
 //			this.nFulfilledBuyOrders += 1;
 //			if (this.standingBuyOrders.containsKey(receipt.getOrderbook())) {
 //				this.updateStandingOrder(receipt);
 //			} else {
 //				this.dealWithOrderForRemovedOrder(receipt);
 //			}
 //
 //		} else {
 //			this.nFulfilledSellOrders += 1;
 //			if (this.standingSellOrders.containsKey(receipt.getOrderbook())) {
 //				this.updateStandingOrder(receipt);
 //			} else {
 //				this.dealWithOrderForRemovedOrder(receipt);
 //			}
 //		}
 //	}
 
 
 	private void dealWithOrderForRemovedOrder(TransactionReceipt receipt) {
 		if (this.orderHistory.contains(receipt.getFilledOrder())) {
 			/*
 			 * The agent did submit the order at some point, but now it is no
 			 * longer in his standing order list. A likely explanation (the only
 			 * one that I've come up with so far) is that the order was filled
 			 * after the agent issued a cancellation.
 			 */
 			if (agentPaysWhenOrderIsFilledAfterSendingCancellation) {
 				this.nFulfilledBuyOrders += 1;
 				this.eventlog.logAgentAction(String.format("Agent %s had to fullfill an already cancelled order (id: %s)", receipt.getOwner().getID(), receipt.getFilledOrder().getID()));
 			} else {
 				World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he cancelled (id: %s), but market side handling of this situation has not been implemented yet", this.getID(), receipt.getFilledOrder().getID()));
 			}
 		} else {
 			World.errorLog.logError(String.format("Agent %s received a transaction receipt for an order that he didn't submit (id: %s)", this.getID(), receipt.getFilledOrder().getID()));
 		}
 	}
 
 	private void updateStockHoldings(Stock stock, BuySell buysell, long volume) {
 		/*
 		 * 
 		 */
 		
 	}
 	
 	private void updateStandingOrder(TransactionReceipt receipt) {
 		/*
 		 * When the agent receives a receipt for a filled order, he has to
 		 * update his local list of submitted orders. If the receipt volume is
 		 * less than the volume of the filled local order, he just subtracts the
 		 * difference from the local order. If they are equal, he removes the
 		 * local order. If it is more, he throws an error.
 		 */
 		
 		long receiptVolume = receipt.getUnsignedVolume();
 		long agentHoldingStockVolume = this.ownedStocks.get(receipt.getStock());
 		long agentCurrentCash = this.cash;
 		
 		if(receiptVolume > agentHoldingStockVolume) {
 			/*
 			 * 
 			 */
 		}
 		
 		
 		
 		Order standingOrder;
 		if (receipt.getBuySell() == BuySell.BUY) {
 			standingOrder = this.standingBuyOrders.get(receipt.getOrderbook());
 			if (receipt.getUnsignedVolume() == standingOrder.getCurrentAgentSideVolume()) {
 				this.standingBuyOrders.remove(receipt.getOrderbook());
 			}
 		} else {
 			standingOrder = this.standingSellOrders.get(receipt.getOrderbook());
 			if (receipt.getUnsignedVolume() == standingOrder.getCurrentAgentSideVolume()) {
 				this.standingSellOrders.remove(receipt.getOrderbook());
 			}
 		}
 
 		if (receipt.getUnsignedVolume() < standingOrder.getCurrentAgentSideVolume()) {
 			standingOrder.updateAgentSideVolumeByDifference(receipt.getSignedVolume());
 		}
 
 		if(receipt.getUnsignedVolume() > standingOrder.getCurrentAgentSideVolume()) {
 			this.eventlog.logAgentAction(String.format("Agent received a transaction receipt for %s units of stock %s at market %s. \n" +
 													   "\t For market order, the current agent side volume was %s, and the market side volume was %s", 
 														receipt.getUnsignedVolume(), receipt.getStock().getID(), receipt.getFilledOrder().getMarket().getID(), 
 														receipt.getFilledOrder().getCurrentAgentSideVolume(), receipt.getFilledOrder().getCurrentMarketSideVolume()));
 			if(!MarketRules.allowsShortSelling) {
 				World.errorLog.logError("Shortselling was not allowed, but happeed when agent received a transaction receipt for more than ");
 			}
 		}
 		// if(receipt.getAbsoluteVolume() > standingOrder.getInitialVolume()){
 		// World.errorLog.logError("Agent received a receipt for a buy order with a larger volume than the one he placed...");
 		// }
 
 	}
 	
 //	private void updateStandingOrder(TransactionReceipt receipt) {
 //		/*
 //		 * When the agent receives a receipt for a filled order, he has to
 //		 * update his local list of submitted orders. If the receipt volume is
 //		 * less than the volume of the filled local order, he just subtracts the
 //		 * difference from the local order. If they are equal, he removes the
 //		 * local order. If it is more, he throws an error.
 //		 */
 //		Order standingOrder;
 //		if (receipt.getBuySell() == BuySell.BUY) {
 //			standingOrder = this.standingBuyOrders.get(receipt.getOrderbook());
 //			if (receipt.getAbsoluteVolume() == standingOrder.getCurrentAgentSideVolume()) {
 //				this.standingBuyOrders.remove(receipt.getOrderbook());
 //			}
 //		} else {
 //			standingOrder = this.standingSellOrders.get(receipt.getOrderbook());
 //			if (receipt.getAbsoluteVolume() == standingOrder.getCurrentAgentSideVolume()) {
 //				this.standingSellOrders.remove(receipt.getOrderbook());
 //			}
 //		}
 //
 //		if (receipt.getAbsoluteVolume() < standingOrder.getCurrentAgentSideVolume()) {
 //			standingOrder.updateAgentSideVolumeByDifference(receipt.getSignedVolume());
 //		}
 //
 //		if(receipt.getAbsoluteVolume() > standingOrder.getCurrentAgentSideVolume()) {
 //			this.eventlog.logAgentAction(String.format("Agent received a transaction receipt for %s units of stock %s at market %s. \n" +
 //													   "\t For market order, the current agent side volume was %s, and the market side volume was %s", 
 //														receipt.getAbsoluteVolume(), receipt.getOriginalOrder().getStock().getID(), receipt.getOriginalOrder().getMarket().getID(), 
 //														receipt.getOriginalOrder().getCurrentAgentSideVolume(), receipt.getOriginalOrder().getCurrentMarketSideVolume()));
 //			if(!MarketRules.allowsShortSelling) {
 //				World.errorLog.logError("Shortselling was not allowed, but happeed when agent received a transaction receipt for more than ")
 //			}
 //		}
 //		// if(receipt.getAbsoluteVolume() > standingOrder.getInitialVolume()){
 //		// World.errorLog.logError("Agent received a receipt for a buy order with a larger volume than the one he placed...");
 //		// }
 //
 //	}
 
 	public long getTotalWealth() {
 		long totalWealth;
 		totalWealth = this.cash;
 		for (Stock stock : this.stocks) {
 			totalWealth += this.ownedStocks.get(stock) * stock.getEstimatedTradedPriceFromEndOfCurrentRound();
 		}
 		return totalWealth;
 	}
 
 	public long getCash() {
 		return cash;
 	}
 
 	public void updateCash(long amount) {
 		this.cash += amount;
 		if(this.cash < 0) {
 			World.errorLog.logError("Agent has negative cash, but he did not use the borrow function");
 		}
 	}
 	
 	public void borrowCash(long positiveAmount) {
 		if(positiveAmount < 0) {
 			World.errorLog.logError("An agent tried to borrow a negative amount of money. Amount must be positive");
 		} else {
 			this.updateCash(positiveAmount);
 			this.eventlog.logAgentAction(String.format("Agent %s borrowed %s cash", this.id, positiveAmount));
 		}
 	}
 
 	public long getID() {
 		return id;
 	}
 
 	public int getLatency(Market market) {
 		return this.latencyToMarkets.get(market);
 	}
 
 	public ArrayList<Orderbook> getOrderbooks() {
 		return this.orderbooks;
 	}
 
 	public long getWakeupTime() {
 		return this.wakeupTime;
 	}
 
 	public long getArrivalTimeToMarket(Market market) {
 		return this.getLatency(market) + World.getCurrentRound();
 	}
 
 	public static long getRandomInitialTraderWealth() {
 		Random r = new Random();
 		long w = 0;
 		while (w <= 0) {
 			w = (int) Math.rint(r.nextGaussian() * wealthStd + wealthMean);
 		}
 		return w;
 	}
 
 	public void hibernate() {
 		this.wakeupTime = World.getCurrentRound() + emptyOrderbookWaitTime;
 	}
 
 	protected void submitOrder(Order order) {
 		if (order.getBuySell() == Order.BuySell.BUY) {
 			this.standingBuyOrders.put(order.getOrderbook(), order);
 			this.nSubmittedBuyOrders += 1;
 		} else {
 			this.standingSellOrders.put(order.getOrderbook(), order);
 			this.nSubmittedSellOrders += 1;
 		}
 		if (keepOrderHistory) {
 			this.orderHistory.add(order);
 		}
 	}
 
 	public static HashMap<Market, Integer> getRandomLatencyHashMap(Market[] markets) {
 		HashMap<Market, Integer> latencyMap = new HashMap<Market, Integer>();
 
 		for (Market market : markets) {
 			int latency = Utils.getRandomUniformInteger(minimumLatency, maximumLatency);
 			latencyMap.put(market, latency);
 		}
 		return latencyMap;
 	}
 
 	protected void cancelOrder(OrderCancellation cancellation) {
 		this.nSubmittedCancellations += 1;
 	}
 
 	public long getnSubmittedBuyOrders() {
 		return nSubmittedBuyOrders;
 	}
 
 	public long getnSubmittedSellOrders() {
 		return nSubmittedSellOrders;
 	}
 
 	public long getnFulfilledBuyOrders() {
 		return nFulfilledBuyOrders;
 	}
 
 	public long getnFulfilledSellOrders() {
 		return nFulfilledSellOrders;
 	}
 
 	public long getnFullfilledOrders() {
 		return this.nFulfilledBuyOrders + this.nFulfilledSellOrders;
 	}
 
 	public long getnSubmittedOrders() {
 		return this.nSubmittedBuyOrders + this.nSubmittedSellOrders;
 	}
 
 	public long getnStandingBuyOrders() {
 		return this.standingBuyOrders.size();
 	}
 
 	public long getnStandingSellOrders() {
 		return this.standingSellOrders.size();
 	}
 
 	public long getnOrderCancellations() {
 		return this.nSubmittedCancellations;
 	}
 
 	public HashMap<Orderbook, Order> getStandingBuyOrders() {
 		return standingBuyOrders;
 	}
 
 	public HashMap<Orderbook, Order> getStandingSellOrders() {
 		return standingSellOrders;
 	}
 
 	public ArrayList<Order> getOrderHistory() {
 		return this.orderHistory;
 	}
 
 	public HashMap<Stock, Long> getOwnedStocks() {
 		return this.ownedStocks;
 	}
 
 	public ArrayList<Stock> getActiveStocks() {
 		return this.stocks;
 	}
 
 	public int getTransmissionDelayToMarket(Market market) {
 		return this.latencyToMarkets.get(market);
 	}
 
 }
