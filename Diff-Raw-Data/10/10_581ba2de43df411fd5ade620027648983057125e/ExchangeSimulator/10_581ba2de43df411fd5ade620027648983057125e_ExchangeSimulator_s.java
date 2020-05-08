 import java.util.Date;
import java.util.logging.Logger;
 
 /**
  * Analysis of mtgox:
  *  - marketorders (buy/sell at marketprice) execute immediately, therefore we do a very simple simulator in the first step
  *  - needs more analysis to make other orders (perhaps add to ordertype(marketorder/normalorder))
  * 
  * @author mpater
  *
  */
 public class ExchangeSimulator implements IExchange {
 
 	private Logger logger = Logger.getLogger(ExchangeSimulator.class.getName());
 
 	private double x;
 	private Wallet wallet = new Wallet(IConfiguration.BALANCE);
 
     @Override
 	//TODO get from Database?
     public Transaction getTicker() {
 		Transaction t = new Transaction(new Date(), Math.sin(x) + 1, x);
     	x++;
 		return t;
     }
 
     @Override
 	public Order placeBuyOrder(NormalOrder order) {
 		logger.info("Put buy order " + order);
 		//immediate transaction
 		wallet.withdraw(order.getPrice() + (IConfiguration.FEE_PERCENTAGE * order.getPrice()));
 		order.setState(Order.State.DONE);
 		return order;
     }
 
     @Override
 	public Order placeSellOrder(NormalOrder order) {
 		logger.info("Put sell order " + order);
 		//immediate transaction
 		wallet.deposit(order.getPrice());
 		order.setState(Order.State.DONE);
 		return order;
     }
 
     @Override
     public Order getCurrentOrder() {
 		return new NoOrder();
     }
 
     @Override
     public void cancelOrder(Order order) {
 		logger.info("Cancell order " + order);
     }
 
 	@Override
 	public Wallet getWallet() {
 		return wallet;
 	}
 
 	@Override
 	public Order placeBuyOrder(MarketOrder order) {
 		logger.info("Put buy order " + order);
 		//immediate transaction
 		wallet.withdraw(getTicker().getPrice() + (IConfiguration.FEE_PERCENTAGE * getTicker().getPrice()));
 		order.setState(Order.State.DONE);
 		return order;
 	}
 
 	@Override
 	public Order placeSellOrder(MarketOrder order) {
 		logger.info("Put sell order " + order);
 		//immediate transaction
 		wallet.deposit(getTicker().getPrice());
 		order.setState(Order.State.DONE);
 		return order;
 	}
 }
