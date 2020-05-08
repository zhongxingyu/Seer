 package paybar.controller;
 
 import java.util.Date;
 import java.util.logging.Logger;
 
 import javax.ejb.ActivationConfigProperty;
 import javax.ejb.MessageDriven;
 import javax.inject.Inject;
 import javax.jms.Connection;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ObjectMessage;
 
 import at.ac.uibk.paybar.messages.Configuration;
 import at.ac.uibk.paybar.messages.TransactionMessage;
 
 import paybar.data.CouponResource;
 import paybar.data.DetailAccountResource;
 import paybar.data.TransactionResource;
 import paybar.model.DetailAccount;
 
 @MessageDriven(name = "DelayedTransactionProcessor", messageListenerInterface = MessageListener.class, activationConfig = {
 		@ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
 		@ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/test"),
 		@ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge") })
 public class DelayedTransactionProcessor implements MessageListener {
 
 	@Inject
 	private TransactionResource tr;
 
 	@Inject
 	private CouponResource cr;
 
 	@Inject
 	private DetailAccountResource dar;
 
 	// maybe this is sub-optimal...
 	private static final Logger log = Logger
 			.getLogger(DelayedTransactionProcessor.class.getName());
 
 	public void onMessage(Message message) {
 		Connection connection = null;
 		try {
 			ObjectMessage textMessage = (ObjectMessage) message;
 			at.ac.uibk.paybar.messages.TransactionMessage transactionMessage = (at.ac.uibk.paybar.messages.TransactionMessage) textMessage
 					.getObject();
 
 			String text = "DelayedTransactionProcessor got Message: "
 					+ "type: " + transactionMessage.getType()
 					+ " posOrBankId: " + transactionMessage.getPosOrBankId()
 					+ ", tanCode: " + transactionMessage.getCouponCode()
 					+ ", amount: " + transactionMessage.getAmount()
 					+ ", timestamp: " + transactionMessage.getTimestamp();
 
 			log.info(text);
 
 			int type = transactionMessage.getType();
 
 			switch (type) {
 			case TransactionMessage.TYPE_TRANSACTION: {
 				if (cr.isValidCoupon(transactionMessage.getCouponCode())) {
 					tr.createDebitTransaction(transactionMessage.getAmount(),
 							transactionMessage.getCouponCode(), text,
 							transactionMessage.getPosOrBankId(), new Date(
 									transactionMessage.getTimestamp()));
 					
 					DetailAccount da = dar.getUserByID(Long.valueOf(transactionMessage.getUserName()).longValue(), false);
 					da.setCredit(da.getCredit() - transactionMessage.getAmount());
 					
 					dar.updateDetailAccount(da);
 					
 				}
 				break;
 			}
 			case TransactionMessage.TYPE_CHARGE: {
 				/*
 				 * Create new transaction.
 				 */
 				long amount = transactionMessage.getAmount();
 
 				String userName = transactionMessage.getUserName();
 				DetailAccount da = dar.getUserByName(userName, false);
 				
 				Date now = new Date(transactionMessage.getTimestamp());
 
 				// TODO: extend message for extra information like credit card
 				// number?
				
				tr.createChargeTransactionByUsername(amount,
 						"Charging Account from Credit Card: "
 								+ transactionMessage.getCouponCode(),
 						transactionMessage.getPosOrBankId(), userName, now);
 				da.setCredit(da.getCredit());
 
 				break;
 			}
 			// TODO: throw exception
 			default:
 				log.info("ERROR handling TransactionMessage. Unsupported type.");
 				break;
 			}
 
 		} catch (Throwable e) {
 			e.printStackTrace();
 		} finally {
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (JMSException e) {
 					e.printStackTrace(); // To change body of catch statement
 											// use File | Settings | File
 											// Templates.
 				}
 			}
 		}
 	}
 }
