 package org.switchyard.quickstarts.helloworld.action;
 
 import static org.hamcrest.Matchers.*;
 import static org.junit.Assert.*;
 
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.switchyard.component.camel.model.RouteScanner;
 import org.switchyard.component.test.mixins.cdi.CDIMixIn;
 import org.switchyard.component.test.mixins.hornetq.HornetQMixIn;
 import org.switchyard.test.SwitchYardRunner;
 import org.switchyard.test.SwitchYardTestCaseConfig;
 
 @RunWith(SwitchYardRunner.class)
 @SwitchYardTestCaseConfig(
         config = SwitchYardTestCaseConfig.SWITCHYARD_XML,
         mixins = { CDIMixIn.class, HornetQMixIn.class },
         scanners = RouteScanner.class)
 public class HelloWorldActionTest {
     
     private static final String QUEUE_NAME = "quickstart_helloworld_action_Request";
     private static final String QUEUE_NAME_RESPONSE = "quickstart_helloworld_action_Response";
     private static final String TEST_MESSAGE = "Hello World Action";
     
     private HornetQMixIn hornetQMixIn;
     
     @Test
     public void sendJMS() throws Exception {
         // TODO add test store
         Session session = null;
         try {
             session = hornetQMixIn.createJMSSession();
             MessageProducer producer = session.createProducer(HornetQMixIn.getJMSQueue(QUEUE_NAME));
             Message message = hornetQMixIn.createJMSMessage(TEST_MESSAGE);
             producer.send(message);
             
             MessageConsumer consumer = session.createConsumer(HornetQMixIn.getJMSQueue(QUEUE_NAME_RESPONSE));
             Message receivedMessage = consumer.receive(3000);
             assertThat(receivedMessage, is(instanceOf(TextMessage.class)));
             assertThat(((TextMessage) receivedMessage).getText(), is(equalTo("\nBEFORE**\n" + TEST_MESSAGE
                     + "\nAFTER**\n")));
            assertThat(receivedMessage.getStringProperty("quickstart"), is(equalTo("hello_world_action")));
         } finally {
             HornetQMixIn.closeJMSSession(session);
         }
     }
     
 }
