 package eu.play_project.play_eventadapter.tests;
 
 import static eu.play_project.play_commons.constants.Event.EVENT_ID_SUFFIX;
 
 import java.util.Calendar;
 
 import javax.xml.namespace.QName;
 
 import junit.framework.Assert;
 
 import org.event_processing.events.types.UcTelcoCall;
 import org.junit.Test;
 import org.ontoware.rdf2go.model.Model;
 import org.ontoware.rdf2go.model.node.impl.URIImpl;
 import org.petalslink.dsb.commons.service.api.Service;
 import org.petalslink.dsb.notification.service.NotificationConsumerService;
 import org.petalslink.dsb.soap.CXFExposer;
 import org.petalslink.dsb.soap.api.Exposer;
 
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.abstraction.Notify;
 import com.ebmwebsourcing.wsstar.basenotification.datatypes.api.utils.WsnbException;
 import com.ebmwebsourcing.wsstar.wsnb.services.INotificationConsumer;
 
import eu.play_project.play_commons.constants.Constants;
 import eu.play_project.play_commons.constants.Stream;
 import eu.play_project.play_commons.eventtypes.EventHelpers;
 import eu.play_project.play_eventadapter.AbstractReceiver;
 import eu.play_project.play_eventadapter.AbstractSender;
 import eu.play_project.play_eventadapter.NoRdfEventException;
 
 public class SendAndReceiveTest {
 
	public static String notif = Constants.getProperties().getProperty("dsb.notify.endpoint");
 
 	@Test
 	public void testSendAndReceive() {
 			
 	    Service server = null;
 		NotificationConsumer dsbListener = null;
 	    
         try {
     		/*
     		 * Prepare temporary Notification endpoint for test:
     		 */
             QName interfaceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumer");
             QName serviceName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerService");
             QName endpointName = new QName("http://docs.oasis-open.org/wsn/bw-2",
                     "NotificationConsumerPort");
             System.out.println("Exposing notification endpoint at: " + notif);
             dsbListener = new NotificationConsumer();
             NotificationConsumerService service = new NotificationConsumerService(interfaceName,
                     serviceName, endpointName, "NotificationConsumerService.wsdl", notif,
                     dsbListener);
             Exposer exposer = new CXFExposer();
             server = exposer.expose(service);
             server.start();
             
             /*
              * Encode and send an event to the test endpoint:
              */
     		String eventId = EventHelpers.createRandomEventId("UnitTest");
     		UcTelcoCall event = new UcTelcoCall(EventHelpers.createEmptyModel(eventId),
 				eventId + EVENT_ID_SUFFIX, true);
     		event.setEndTime(Calendar.getInstance());
     		event.setStream(new URIImpl(Stream.TaxiUCCall.getUri()));
             
     		AbstractSender rdfSender = new AbstractSender(Stream.TaxiUCCall.getTopicQName());
     		rdfSender.setDsbNotify(notif);
     		rdfSender.notify(event);
  		} catch (Exception e) {
 			Assert.fail(e.getMessage());
 		} finally {
 			if (server != null) {
 				server.stop();
 			}
 		}
         
         /*
          * Wait for the event to be received:
          */
    		try {
 			synchronized (dsbListener) {
 				dsbListener.wait(1000);
 			}
 		} catch(InterruptedException e) {
 			Assert.fail(e.getMessage());
 		}
    		
    		/*
    		 * Check if the receiever really got the right event:
    		 */
    		Assert.assertEquals(3, dsbListener.result);
    		
    		
 		if (server != null) {
 			server.stop();
 		}
 	}
 	
 	private final class NotificationConsumer implements INotificationConsumer {
         AbstractReceiver rdfReceiver = new AbstractReceiver() {};
         public long result = 0;
 
         @Override
 		public void notify(Notify request) throws WsnbException {
         	
         	/*
         	 * Receive the event at test endpoint:
         	 */
         	try {
 				Model m = this.rdfReceiver.parseRdf(request);
 				this.result = m.size();
 			} catch (NoRdfEventException e) {
 				Assert.fail(e.getMessage());
 			} finally {
 				// Notify the other thread that is waiting:
 				synchronized (this) {
 					this.notifyAll();
 				}
 			}
         }
 		
 	}
 	
 }
