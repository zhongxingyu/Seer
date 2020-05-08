 package org.mailoverlord.server.message;
 
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mailoverlord.server.config.ApplicationConfig;
 import org.mailoverlord.server.config.EmbeddedDataSourceConfig;
 import org.mailoverlord.server.config.JpaConfig;
 import org.mailoverlord.server.config.TestMailSessionConfig;
 import org.mailoverlord.server.entities.Message;
 import org.mailoverlord.server.repositories.MessageRepository;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.subethamail.smtp.client.SmartClient;
 import org.subethamail.smtp.server.SMTPServer;
 
 import javax.mail.*;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMultipart;
 import java.io.IOException;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 
 /**
  * Test for Database Message Handler Factory
  */
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(classes={JpaConfig.class, EmbeddedDataSourceConfig.class, TestMailSessionConfig.class, ApplicationConfig.class})
 public class DatabaseMessageHandlerFactoryTest {
 
     private static final Logger logger = LoggerFactory.getLogger(DatabaseMessageHandlerFactoryTest.class);
 
     private static final String MESSAGE_TEXT = "Hi";
     private static final String FROM = "from@test.com";
     private static final String TO1 = "to@from.com";
     private static final String TO2 = "to2@from.com";
     private static final String TO3 = "to3@from.com";
     private static final String HOST = "localhost";
     private static final int PORT = 2025;
 
     @Autowired
     SMTPServer server;
 
     @Autowired
     MessageRepository messageRepository;
 
     @Autowired
     Session session;
 
     @Test
     public void testSimpleMessage() {
 
         try {
             SmartClient client = new SmartClient(HOST, PORT, "test.com");
             client.from(FROM);
             client.to(TO1);
             client.to(TO2);
             client.dataStart();
             client.dataWrite(MESSAGE_TEXT.getBytes(), MESSAGE_TEXT.getBytes().length);
             client.dataEnd();
             client.quit();
             Iterable<Message> messages =  messageRepository.findByFrom(FROM);
             assertTrue("Message not found in database.", messages.iterator().hasNext());
             Message databaseMessage = messages.iterator().next();
             String expectedTo = String.format("%s,%s", TO1, TO2);
            logger.debug("Database Message To: {}", databaseMessage.getTo());
             assertEquals(expectedTo, databaseMessage.getTo());
            logger.debug("Database Message From: {}", databaseMessage.getFrom());
             assertEquals(FROM, databaseMessage.getFrom());
             String databaseString = new String(databaseMessage.getData());
             logger.info("Message Body: {}", databaseString);
             assertEquals(MESSAGE_TEXT, databaseString.trim());
             messageRepository.delete(databaseMessage);
         } catch (IOException e) {
             logger.error("Error while trying to send simple message.", e);
             throw new RuntimeException("Error while trying to send simple message", e);
         }
     }
 
     @Test
     public void testMimeMessage() throws MessagingException {
         MimeMessage mimeMessage = new MimeMessage(session);
         mimeMessage.setFrom(new InternetAddress(FROM));
         mimeMessage.addRecipients(javax.mail.Message.RecipientType.TO, TO1);
         mimeMessage.addRecipients(javax.mail.Message.RecipientType.CC, TO2);
         mimeMessage.addRecipients(javax.mail.Message.RecipientType.BCC, TO3);
 
         mimeMessage.setSubject("This is a test message");
 
         Multipart multipart = new MimeMultipart();
 
         BodyPart textPart = new MimeBodyPart();
         textPart.setText("This is the text message body...");
 
         BodyPart htmlPart = new MimeBodyPart();
         htmlPart.setContent("<HTML><BODY><H4>Large Html</H4></BODY></HTML>", "text/html");
 
         multipart.addBodyPart(textPart);
         multipart.addBodyPart(htmlPart);
 
         mimeMessage.setContent(multipart);
 
         Transport.send(mimeMessage);
 
         Iterable<Message> messages =  messageRepository.findByFrom(FROM);
         assertTrue("Message not found in database.", messages.iterator().hasNext());
         Message databaseMessage = messages.iterator().next();
         String expectedTo = String.format("%s,%s,%s", TO1, TO2, TO3);
         assertEquals(expectedTo, databaseMessage.getTo());
         assertEquals(FROM, databaseMessage.getFrom());
         messageRepository.delete(databaseMessage);
     }
 
 }
