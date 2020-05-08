 package simulator.strategy;
 
 import java.util.ArrayList;
 
 import simulator.Order;
 import simulator.OrderBooks;
 import simulator.Trade;
 import simulator.TradeEngine;
 
 public class Momentum extends AbstractStrategy implements Strategy {
 
 	TradeEngine tradeEngine;
	private String previousOrderType;
 	
 	public Momentum(OrderBooks books, TradeEngine tradeEngine) {
 		super(books);
 		this.tradeEngine   = tradeEngine;
		this.previousOrderType = "NO_ORDER";
 	}
 
 	@Override
 	public Order strategise() {
 		
 		double averageReturn = calculateAverageReturn();
 
		if (averageReturn > 0 && !previousOrderType.equals("B")) {
			previousOrderType = "B";
			return createOrder("ENTER", books.bestBidPrice() + 0.001, books.bestAskOrder().volume(), null, "B");
		} else if (averageReturn < 0 && !previousOrderType.equals("A")) {
			previousOrderType = "A";
			return createOrder("ENTER", books.bestAskPrice() - 0.001, books.bestBidOrder().volume(), null, "A");
 		}
 		
 		return Order.NO_ORDER;
 	}
 
 	@Override
 	public String getStrategyName() {
 		return "Momentum";
 	}
 	
 	
 	private double calculateAverageReturn() {
 		double sum = 0;
 		int    counter = 0;
 		double currentPrice;
 		double previousPrice;
 		ArrayList<Trade> trades = tradeEngine.getTradeList();
 		for (int i = 1; i < 11; i++) {
 			if (trades.size() - i - 1 >= 0 &&
 			    books.askListSize() > 0    &&
 			    books.bidListSize() > 0) {
 
 			    currentPrice  = trades.get(trades.size() - i).price();
 				previousPrice = trades.get(trades.size() - i - 1).price();
 				sum += (currentPrice - previousPrice) / previousPrice;
 				counter++;
 		    }
 		}
 		
 		return sum/counter;
 	}
 
 }
