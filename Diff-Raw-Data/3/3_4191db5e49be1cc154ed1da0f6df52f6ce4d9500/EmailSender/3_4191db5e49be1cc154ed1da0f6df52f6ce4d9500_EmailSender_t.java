 package com.abudko.reseller.huuto.query.notification.email;
 
 import javax.mail.BodyPart;
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeBodyPart;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 import javax.mail.internet.MimeMultipart;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.stereotype.Component;
 
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 import com.abudko.reseller.huuto.query.html.HtmlCreator;
 import com.abudko.reseller.huuto.query.html.list.QueryListResponse;
 import com.abudko.reseller.huuto.query.notification.ResponseProcessor;
 
 @Component
 public class EmailSender implements ResponseProcessor {
 
     private Log log = LogFactory.getLog(getClass());
 
     @Autowired
     private JavaMailSender mailSender;
 
     @Autowired
     private HtmlCreator htmlCreator;
 
     @Value("#{resellerProperties['email.username']}")
     private String senderAddress;
 
     @Value("#{resellerProperties['email.destination']}")
     private String destinationAddress;
 
     public void process(QueryListResponse response) {
         MimeMessage mail = null;
         try {
             mail = prepareMessage(response);
         } catch (MessagingException e) {
             String errorMsg = "Exception happened while creating mime email message";
             log.error(errorMsg, e);
             throw new EmailNotificationException(errorMsg);
         }
         this.mailSender.send(mail);
     }
 
     private MimeMessage prepareMessage(QueryListResponse response) throws MessagingException {
         MimeMessage mail = mailSender.createMimeMessage();
 
         setMessageSubject(mail, response);
         setMessageAddresses(mail);
         setMessageContent(mail, response);
 
         return mail;
     }
 
     private void setMessageContent(MimeMessage mail, QueryListResponse response) throws MessagingException {
         Multipart messageBody = composeMessageBody(response);
         mail.setContent(messageBody);
     }
 
     Multipart composeMessageBody(QueryListResponse response) throws MessagingException {
         String html = htmlCreator.generateHtmlForResponse(response);
 
         BodyPart mediaPart = new MimeBodyPart();
         mediaPart.setContent(html, "text/html");
 
         BodyPart textPart = new MimeBodyPart();
         textPart.setText(response.dump());
 
         Multipart messageBody = new MimeMultipart();
         messageBody.addBodyPart(textPart);
        messageBody.addBodyPart(mediaPart);
 
         return messageBody;
     }
 
     private void setMessageSubject(MimeMessage mail, QueryListResponse response) throws MessagingException {
         String subject = String.format("%s %s (%s)", response.getDescription(), response.getPrices(),
                 response.getFullPrice());
         mail.setSubject(subject);
     }
 
     private void setMessageAddresses(MimeMessage mail) throws AddressException, MessagingException {
         InternetAddress sender = InternetAddress.parse(senderAddress)[0];
         mail.setFrom(sender);
         InternetAddress destination = InternetAddress.parse(destinationAddress)[0];
         mail.setRecipient(RecipientType.TO, destination);
     }
 }
