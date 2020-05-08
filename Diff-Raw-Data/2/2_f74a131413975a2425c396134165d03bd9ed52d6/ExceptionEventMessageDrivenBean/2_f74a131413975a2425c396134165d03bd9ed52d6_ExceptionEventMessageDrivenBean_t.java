 package de.tud.in.middleware.jms;
 
 import static de.tud.in.middleware.jms.EventUtil.getNodeContent;
 import static de.tud.in.middleware.jms.EventUtil.getXMLDocument;
 
 import java.io.IOException;
 
 import javax.ejb.EJB;
 import javax.ejb.MessageDriven;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageListener;
 import javax.jms.TextMessage;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.w3c.dom.Document;
 import org.xml.sax.SAXException;
 
 import de.tud.in.middleware.customers.Customer;
 import de.tud.in.middleware.dao.OrderDAO;
 import de.tud.in.middleware.mail.MailHandler;
 import de.tud.in.middleware.order.CustomerOrder;
 import de.tud.in.middleware.order.OrderState;
 import de.tud.in.middleware.shipment.Shipment;
 import de.tud.in.middleware.shipment.Truck;
 
 /*
  * Um die Queue im Glassfish anzulegen: asadmin create-jms-resource
 * --restype javax.jms.Queue jms/ExceptionQueue
  */
 @MessageDriven(mappedName = "jms/ExceptionQueue")
 public class ExceptionEventMessageDrivenBean implements MessageListener {
 
 	@PersistenceContext
 	private EntityManager entityManager;
 	@EJB
 	private OrderDAO orderDAO;
 
 	@Override
 	public void onMessage(final Message message) {
 		String msgText;
 		try {
 			msgText = ((TextMessage) message).getText();
 			System.out.println("Received exception: " + msgText);
 
 			final Document document = getXMLDocument(msgText);
 			handleRequest(document);
 		} catch (final JMSException e) {
 			e.printStackTrace();
 		} catch (final ParserConfigurationException e) {
 			e.printStackTrace();
 		} catch (final SAXException e) {
 			e.printStackTrace();
 		} catch (final IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void handleRequest(final Document document) {
 		final int truckId = Integer.parseInt(getNodeContent(document, "truckId"));
 		final Truck truck = entityManager.find(Truck.class, truckId);
 
 		final String description = getNodeContent(document, "exceptionDescription");
 		final OrderState orderState = OrderState.EXCEPTION;
 		orderState.exceptionDescription = description;
 
 		for (final Shipment shipment : truck.getShipments()) {
 			final int orderId = shipment.getOrderId();
 			orderDAO.changeOrderState(orderId, orderState);
 			notifyClient(orderId, description);
 		}
 	}
 
 	private void notifyClient(final int orderId, final String description) {
 		final Customer customer = entityManager.find(CustomerOrder.class, orderId).getCustomer();
 		final String msgStr = String.format("<H2>Dear %s </H2>There has been an exception with your order #%d.<br>%s",
 				customer.getName(), orderId, description);
 		MailHandler.sendMail(customer.getEMail(), "Omazon Order Exception", msgStr);
 	}
 
 }
