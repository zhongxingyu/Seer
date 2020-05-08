 package edu.buet.cse.eia.ch02.v2.ejb;
 
 import javax.ejb.ActivationConfigProperty;
 import javax.ejb.MessageDriven;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.ObjectMessage;
 
 import org.apache.log4j.LogManager;
 import org.apache.log4j.Logger;
 
 import edu.buet.cse.eia.ch02.v2.model.Order;
 import edu.buet.cse.eia.ch02.v2.model.OrderStatus;
 import edu.buet.cse.eia.ch02.v2.persistence.OrderManager;
 
 @MessageDriven(activationConfig = 
   {@ActivationConfigProperty(propertyName = "destinationName", propertyValue = "jms/eia/ch02/v2/BillingQueue"),
    @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue")}, 
    mappedName = "jms/eia/ch02/v2/BillingQueue")
 public class BillingMDB implements MessageListener {
   private final Logger logger = LogManager.getLogger(getClass());
   
   @Override
   public void onMessage(Message message) {
     if (message instanceof ObjectMessage) {
       ObjectMessage objMessage = (ObjectMessage) message;
       
       try {
         Long id = (Long) objMessage.getObject();
         Order order = OrderManager.getOrder(id);
         bill(order);
       } catch (JMSException ex) {
        logger.error("Error while billing the order", ex);
       }
     }
   }
   
   private void bill(Order order) {
     logger.info(String.format("Order %d billed", order.getId()));
     logger.info(String.format("A/C %s charged", order.getBillingInfo().getAccountNumber()));
     order.setStatus(OrderStatus.COMPLETE);
   }
 }
