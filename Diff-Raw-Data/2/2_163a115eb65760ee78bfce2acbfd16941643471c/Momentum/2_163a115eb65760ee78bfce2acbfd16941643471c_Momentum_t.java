 package simulator.strategy;
 
 import java.util.LinkedList;
 
 import simulator.Order;
 import simulator.OrderBooks;
 import simulator.Trade;
 import simulator.TradeEngine;
 
 public class Momentum extends AbstractStrategy implements Strategy {
 
 	TradeEngine tradeEngine;
 	private String previousOrderType;
 
 	private Trade tradeReturn;
 	private LinkedList<Double> returnsList = new LinkedList<Double>();
 	
 	private final double nullReturn = 0;
 
 	//changeable parameters of the strategy
 	private int lookBackPeriod = 10;//how far to look back for computing avg returns
 	private double signalThreshold = 0;//avg returns threshold for generating orders
 	private double priceOffset = 0;//how much to offset generated order price by
 	
 	public Momentum(OrderBooks books, TradeEngine tradeEngine) {
 		super(books);
 		this.tradeEngine   = tradeEngine;
 		this.previousOrderType = "NO_ORDER";
 	}
 
 	public int getLookBackPeriod() {
 		return lookBackPeriod;
 	}
 
 	public void setLookBackPeriod(int lookBackPeriod) {
 		if (lookBackPeriod != 0) {
 			this.lookBackPeriod = lookBackPeriod;
 		}
 	}
 
 	public double getSignalThreshold() {
 		return signalThreshold;
 	}
 
 	public void setSignalThreshold(double signalThreshold) {
 		this.signalThreshold = signalThreshold;
 	}
 
 	public double getPriceOffset() {
 		return priceOffset;
 	}
 
 	public void setPriceOffset(double priceOffset) {
 		this.priceOffset = priceOffset;
 	}
 
 	@Override
 	public Order strategise() {
 
 		//for times when it is not appropriate to create an order,
 		//calcAvgReturns returns 0
 		double averageReturn = calcAvgReturns();
 
 		if (averageReturn > getSignalThreshold() && !previousOrderType.equals("B")) {
 			previousOrderType = "B";
 			return createOrder("ENTER", books.bestBidPrice() + getPriceOffset(), books.bestAskOrder().volume(), null, "B");
		} else if (averageReturn < -getSignalThreshold() && !previousOrderType.equals("A")) {
 			previousOrderType = "A";
 			return createOrder("ENTER", books.bestAskPrice() - getPriceOffset(), books.bestBidOrder().volume(), null, "A");
 		} else {
 			return Order.NO_ORDER;
 		}
 	}
 
 	@Override
 	public String getStrategyName() {
 		return "Momentum";
 	}
 
 	//currently public function of testing
 	public double computeReturn() {
 
 		if (tradeEngine.getTradeList().size() == 0) {
 			//if Tradelist == 0, can't compute any returns
 			return nullReturn;
 		}
 		if (tradeReturn == null) {
 			//occurs once only, first trade only
 			tradeReturn = tradeEngine.getTradeList().get(0);
 			return nullReturn;
 		}
 		//make sure a new trade has occurred before getting the return
 		if (tradeEngine.getTradeList().get(0) == tradeReturn) {
 			return nullReturn;
 		}
 		{
 			double returns =
 					(tradeEngine.getTradeList().get(0).price() - tradeReturn.price())
 					/tradeReturn.price();
 
 			//rounds to 5 decimal places
 			returns = Math.round(returns*10000);
 			returns /= 10000;
 
 			tradeReturn = tradeEngine.getTradeList().get(0);//set current trade as the previous trade now
 			returnsList.add(0, returns);//add newly calculated return into list
 			return returns;
 		}
 	}
 
 	private double calcAvgReturns() {
 		double returns = avgReturns();
 		computeReturn();
 		return returns;
 	}
 
 	//currently public function for testing
 	public double avgReturns() {
 
 		if (returnsList.size() % this.getLookBackPeriod() != 0 || returnsList.size() == 0) {
 			//if there haven't been enough returns to calculate the next set of avgReturns
 			//or if there is nothing in the list
 			return 0;
 		}
 		{
 			double avgReturns = nullReturn;
 			for (int i = 0; i < lookBackPeriod; i++) {
 				avgReturns += returnsList.get(i);
 			}
 			avgReturns /= lookBackPeriod;
 
 			return avgReturns;
 		}
 	}
 
 	@Override
 	public void reset() {
 		super.reset();
 		previousOrderType = "NO_ORDER";
 		tradeReturn = null;
 		returnsList = new LinkedList<Double>();
 	}
 
 }
