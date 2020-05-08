 package simulator;
 public class DumbStrategy implements Strategy {
     
     OrderBooks orderBooks;
     
     public DumbStrategy(OrderBooks orderBooks) {
         this.orderBooks = orderBooks;
     }
     
     @Override
     public Order generateOrder() {
     	if (orderBooks != null) {
     		if (orderBooks.askListSize() != 0) {
     			return new Order(orderBooks.bestAskOrder().instrument(), orderBooks.bestAskOrder().date(), orderBooks.bestAskOrder().time(), new String("ENTER"), orderBooks.bestAskOrder().price(), orderBooks.bestAskOrder().volume(), orderBooks.bestAskOrder().qualifiers(), new Long("0"), new Long("1234"), new Long("0"), "B");
     		}
     		if (orderBooks.bidListSize() != 0) {
    			return new Order(orderBooks.bestBidOrder().instrument(), orderBooks.bestBidOrder().date(), orderBooks.bestBidOrder().time(), new String("ENTER"), 0, orderBooks.bestBidOrder().volume(), orderBooks.bestBidOrder().qualifiers(), new Long("0"), new Long("0"), new Long("3456"), "A");
     		}
     	}
         return Order.NO_ORDER;
     }
 
 	@Override
 	public String getStrategyName() {
 		return "DumbStrategy";
 	}
     
 }
