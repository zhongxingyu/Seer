 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package fi.helsinki.cs.okkopa.mail.send;
 
 import com.icegreen.greenmail.util.DummySSLSocketFactory;
 import com.icegreen.greenmail.util.GreenMail;
 import com.icegreen.greenmail.util.ServerSetup;
 import fi.helsinki.cs.okkopa.Settings;
 import java.io.InputStream;
 import java.security.Security;
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import org.junit.After;
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import static org.junit.Assert.*;
 
 /**
  *
  * @author anttkaik
  */
 public class OKKoPaMessageTest {
     
     GreenMail greenMail;
     Properties props;
     
     public OKKoPaMessageTest() {
     }
     
     @BeforeClass
     public static void setUpClass() {
     }
     
     @AfterClass
     public static void tearDownClass() throws InterruptedException {
         Thread.sleep(5000);
     }
     
     @Before
     public void setUp() throws MessagingException {
         ServerSetup setup = new ServerSetup(4012, "localhost", ServerSetup.PROTOCOL_SMTPS);
         greenMail = new GreenMail(setup); //uses test ports by default
         greenMail.start();
        props = (new Settings("src/test/resources/smtptestsettings.xml")).getSettings();
         
         //XTrustProvider provider = new XTrustProvider();
         //provider.install();
         Security.setProperty("ssl.SocketFactory.provider", DummySSLSocketFactory.class.getName());
     }
     
     @After
     public void tearDown() {
         if (null!=greenMail) {
             greenMail.stop();
         }
     }
 
     @Test
     public void testSetText() throws Exception {
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setText("viestin sisalto");
         assertEquals(msg.body.getBodyPart(0).getContent().toString(), "viestin sisalto");
     }
 
     @Test
     public void testSetSubject() throws Exception {
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setSubject("aihe");
         assertEquals(msg.subject, "aihe");
     }
 
 
     @Test
     public void testReceiveAnything() throws Exception {    
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setSubject("greenmail testi");
         msg.setText("sisältö");
         msg.send();
         assertTrue(greenMail.waitForIncomingEmail(5000, 1));
         Message[] messages = greenMail.getReceivedMessages();
         System.out.println(messages.length + " messages");
     }
     
     
     @Test
     public void testReceiveSubject() throws Exception {
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setSubject("otsikko");
         msg.setText("sisältö");
         msg.send();
         assertTrue(greenMail.waitForIncomingEmail(5000, 1));
         Message[] messages = greenMail.getReceivedMessages();
         assertEquals(messages[0].getSubject(), "otsikko");
     }
     
     
     @Test
     public void testReceiveText() throws Exception {
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setSubject("otsikko");
         msg.setText("sisältö");
         msg.send();
         
         assertTrue(greenMail.waitForIncomingEmail(5000, 1));
         Message[] messages = greenMail.getReceivedMessages();
         Multipart mp = (Multipart) messages[0].getContent();
         assertEquals(mp.getBodyPart(0).getContent().toString(), "sisältö");
     }
     
     
     @Test
     public void testReceiveAttachment() throws Exception {
         OKKoPaMessage msg = new OKKoPaMessage("a", "b", props);
         msg.setSubject("otsikko");
         msg.setText("sisältö");
         InputStream is = getClass().getResourceAsStream("/text/TestAttachment.txt");
         msg.addAttachment(is, "text/plain", "TestAttachment.txt");
         msg.send();
         
         assertTrue(greenMail.waitForIncomingEmail(5000, 1));
         Message[] messages = greenMail.getReceivedMessages();
         Multipart mp = (Multipart) messages[0].getContent();
         assertEquals("liite", mp.getBodyPart(1).getContent().toString());
     }
     
 }
