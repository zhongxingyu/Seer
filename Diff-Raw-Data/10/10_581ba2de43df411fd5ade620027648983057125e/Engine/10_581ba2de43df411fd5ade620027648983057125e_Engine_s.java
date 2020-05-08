import java.util.logging.Logger;
 
 public class Engine implements IEngine {
 	private Logger logger = Logger.getLogger(Engine.class.getName());
 
 	public void run(Database database, IExchange exchange, IAlgorithm algorithm) {
 		/* TODO JEFF? ich versteh das so: korrigier mich bitte. ich weiß nicht genau, wie das funktioniert. 
 		 * Fragen: 	wie ist die fitness definiert? 
 		 * 			wie lernt der algorithmus?
 		 * 			wieviel kaufen wir? 
 		 * 
 		*/
 		while (true) {
 			//TODO we can poll the ticker. perhaps rename it?
 			Transaction lastTicker = exchange.getTicker();
 			OrderType intend = algorithm.addTransaction(lastTicker);
 
 			Order currentOrder = exchange.getCurrentOrder();
 
 			//state machine needed for not immediate executed transactions
 			switch (currentOrder.getOrderType()) {
 			case BUY:
 				switch (intend) {
 				case BUY:
 					exchange.cancelOrder(currentOrder);
 					exchange.placeBuyOrder(new NormalOrder());
 					break;
 				case SELL:
 					exchange.cancelOrder(currentOrder);
 					exchange.placeSellOrder(new NormalOrder());
 					break;
 				case DO_NOTHING:
 					exchange.cancelOrder(currentOrder);
 					break;
 				}
 				break;
 			case DO_NOTHING:
 				switch (intend) {
 				case BUY:
 					exchange.placeBuyOrder(new NormalOrder());
 					break;
 				case SELL:
 					exchange.placeSellOrder(new NormalOrder());
 					break;
 				case DO_NOTHING:
 					break;
 				}
 				break;
 			case SELL:
 				switch (intend) {
 				case BUY:
 					exchange.cancelOrder(currentOrder);
 					exchange.placeBuyOrder(new NormalOrder());
 					break;
 				case SELL:
 					exchange.cancelOrder(currentOrder);
 					exchange.placeSellOrder(new NormalOrder());
 					break;
 				case DO_NOTHING:
 					exchange.cancelOrder(currentOrder);
 					break;
 				}
 				break;
 			default:
 				break;
 			}
 			logger.info(exchange.getWallet().toString());
 		}
 	}
 }
