 package simulator.strategy;
 
 import java.util.ArrayList;
 
 import simulator.Order;
 import simulator.OrderBooks;
 import simulator.Trade;
 import simulator.TradeEngine;
 
 public class Momentum extends AbstractStrategy implements Strategy {
 
 	TradeEngine tradeEngine;
 	
 	public Momentum(OrderBooks books, TradeEngine tradeEngine) {
 		super(books);
 		this.tradeEngine = tradeEngine;
 	}
 
 	@Override
 	public Order strategise() {
 		
 		double averageReturn = calculateAverageReturn();
 		
 		if (averageReturn > 0) {
 			return createOrder(
 			    "ENTER",
 			    books.bestAskPrice(),
 			    books.bestBidOrder().volume(),
 			    "",
 			    "B" );
 		} else if (averageReturn < 0) {
			return createOrder(
			    "ENTER",
			    books.bestAskPrice(),
			    books.bestBidOrder().volume(),
			    "",
			    "A" );
 		}
 		
 		return null;
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
