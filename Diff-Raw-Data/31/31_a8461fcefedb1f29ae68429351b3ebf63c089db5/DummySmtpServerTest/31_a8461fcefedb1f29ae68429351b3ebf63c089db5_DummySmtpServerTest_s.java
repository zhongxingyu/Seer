 /* vim: set ts=2 et sw=2 cindent fo=qroca: */
 
 package com.globant.katari.tools;
 
 import java.util.Properties;
 
 import static org.junit.Assert.assertThat;
 import static org.hamcrest.CoreMatchers.*;
 
 import org.junit.After;
 import org.junit.Test;
 
 import javax.mail.Message;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.Message.RecipientType;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import com.dumbster.smtp.SmtpMessage;
 
 /* Tests the cas service builder.
  */
 public class DummySmtpServerTest {
   
   private DummySmtpServer server = null;
 
   @After
   public void tearDown() {
     if (server != null) {
       server.stop();
       server = null;
     }
   }
 
   @Test
   public void testStart() throws Exception {
     server = DummySmtpServer.start(0);
 
     // send a mail and verify that it is received.
     Properties props = new Properties();
     props.put("mail.smtp.host", "localhost");
     props.put("mail.smtp.port", String.valueOf(server.getPortNumber()));
 
    Session mailSession = Session.getDefaultInstance(props);
     Message sentMessage = new MimeMessage(mailSession);
     sentMessage.setFrom(new InternetAddress("from@blah"));
     sentMessage.setRecipient(RecipientType.TO, new InternetAddress("to@blah"));
     sentMessage.setSubject("the subject");
     sentMessage.setText("the body");
     Transport.send(sentMessage);
 
     assertThat(server.getReceivedEmailSize(), is(1));
     SmtpMessage message = server.iterator().next();
     assertThat(message.getBody(), is("the body"));
   }
 }
 
