 package pl.touk.smx4.paxexamTest;
 
 import junit.framework.Assert;
 import org.apache.servicemix.nmr.api.event.ExchangeListener;
 import org.junit.Test;
 import pl.touk.smx4.paxexam.Listener;
 
 import static junit.framework.Assert.assertEquals;
 
 public class SendMessageTest extends BasicIntegrationTest {
 
     @Test
     public void sendMessage() throws Exception {
         Thread.sleep(1000);
 
        sendMessage("{http://touk.pl/smx4/paxexam}service1", "<request/>");
 
         Assert.assertEquals(1, ((Listener) getBean(ExchangeListener.class, "listener")).exchanges.size());
 
 
     }
 
 }
