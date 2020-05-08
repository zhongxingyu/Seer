 package com.abudko.reseller.huuto.notification.email.order;
 
 import static com.abudko.reseller.huuto.notification.email.order.OrderConfirmationEmailSender.CONTENT_MESSAGE_KEY;
 import static com.abudko.reseller.huuto.notification.email.order.OrderConfirmationEmailSender.SUBJECT_MESSAGE_KEY;
 import static org.junit.Assert.assertFalse;
 import static org.junit.Assert.assertTrue;
 import static org.mockito.Mockito.doThrow;
 import static org.mockito.Mockito.times;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 import java.io.IOException;
 import java.util.Locale;
 
 import javax.mail.MessagingException;
 import javax.mail.Multipart;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import javax.mail.internet.MimeMessage.RecipientType;
 
 import org.junit.Before;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.Mockito;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.slf4j.Logger;
 import org.springframework.context.ApplicationContext;
 import org.springframework.mail.javamail.JavaMailSender;
 import org.springframework.test.util.ReflectionTestUtils;
 
 import com.abudko.reseller.huuto.notification.email.EmailHtmlImageCreator;
 import com.abudko.reseller.huuto.order.ItemOrder;
 import com.abudko.reseller.huuto.query.exception.EmailNotificationException;
 import com.abudko.reseller.huuto.query.html.item.ItemResponse;
 
 @RunWith(MockitoJUnitRunner.class)
 public class OrderConfirmationEmailSenderTest {
 
     private static final String SENDER_ADDRESS = "sender@mail.com";
     private static final String DESTINATION_ADDRESS = "destination@mail.com";
     private static final String SUBJECT = "order %s confirmation";
     private static final String CONTENT = "message body";
 
     @Mock
     private Logger log;
 
     @Mock
     private MimeMessage message;
 
     @Mock
     private JavaMailSender mailSender;
 
     @Mock
     private EmailHtmlImageCreator htmlCreator;
 
     @Mock
     private ApplicationContext context;
 
     @InjectMocks
     private OrderConfirmationEmailSender emailConfirmationSender = new OrderConfirmationEmailSender();
 
     private ItemOrder order;
 
     @Before
     public void setup() {
         ReflectionTestUtils.setField(emailConfirmationSender, "senderAddress", SENDER_ADDRESS);
         ReflectionTestUtils.setField(emailConfirmationSender, "destinationAddress", DESTINATION_ADDRESS);
         when(mailSender.createMimeMessage()).thenReturn(message);
         order = createTestOrder();
 
         when(
                 context.getMessage(Mockito.eq(SUBJECT_MESSAGE_KEY), Mockito.any(Object[].class),
                         Mockito.any(Locale.class))).thenReturn(SUBJECT);
 
         when(
                 context.getMessage(Mockito.eq(CONTENT_MESSAGE_KEY), Mockito.any(Object[].class),
                         Mockito.any(Locale.class))).thenReturn(CONTENT);
     }
 
     private ItemOrder createTestOrder() {
         ItemResponse itemResponse = new ItemResponse();
         itemResponse.setImgBaseSrc("imgBaseSrc");
         itemResponse.setItemUrl("itemUrl");
 
         ItemOrder order = new ItemOrder();
 
         order.setCustomerEmail(SENDER_ADDRESS);
         order.setCustomerName("customerName");
         order.setCustomerPhone("customerPhone");
         order.setItemResponse(itemResponse);
         order.setText("text");
 
         return order;
     }
 
     @Test
     public void testSend() {
         emailConfirmationSender.send(order);
 
         verify(mailSender).send(message);
     }
 
     @Test
     public void testSendThrowException() throws AddressException, MessagingException {
         doThrow(new MessagingException()).when(message).setFrom(InternetAddress.parse(SENDER_ADDRESS)[0]);
 
         try {
             emailConfirmationSender.send(order);
         } catch (EmailNotificationException e) {
             verify(log).error(Mockito.anyString(), Mockito.any(EmailNotificationException.class));
         }
     }
 
     @Test
     public void testMessageFrom() throws AddressException, MessagingException {
         emailConfirmationSender.send(order);
 
         verify(message).setFrom(InternetAddress.parse(SENDER_ADDRESS)[0]);
     }
 
     @Test
     public void testMessageReplyTo() throws AddressException, MessagingException {
         emailConfirmationSender.send(order);
 
         verify(message, times(0)).setReplyTo(InternetAddress.parse(SENDER_ADDRESS));
     }
 
     @Test
     public void testMessageTo() throws AddressException, MessagingException {
         emailConfirmationSender.send(order);
 
         verify(message).setRecipient(RecipientType.TO, InternetAddress.parse(order.getCustomerEmail())[0]);
     }
 
     @Test
     public void testMessageSubject() throws AddressException, MessagingException {
        String subject = "Order " + order.getItemResponse().getId();
        when(
                context.getMessage(Mockito.eq(SUBJECT_MESSAGE_KEY), Mockito.any(Object[].class),
                        Mockito.any(Locale.class))).thenReturn(subject);
 
         emailConfirmationSender.send(order);
 
         verify(message).setSubject(subject, "UTF-8");
     }
 
     @Test
     public void testMessageContent() throws AddressException, MessagingException {
         String html = "html";
         when(htmlCreator.generateHtmlImage(order.getItemResponse().getImgBaseSrc())).thenReturn(html);
 
         emailConfirmationSender.send(order);
 
         verify(message).setContent(Mockito.any(Multipart.class));
         verify(htmlCreator).generateHtmlImage(order.getItemResponse().getImgBaseSrc());
     }
 
     @Test
     public void testMessageBodyText() throws MessagingException, IOException {
         Multipart messageBody = emailConfirmationSender.composeMessageBody(order);
 
         String messageBodyText = (String) messageBody.getBodyPart(0).getContent();
         assertTrue(messageBodyText, messageBodyText.contains(order.getCustomerEmail()));
         assertTrue(messageBodyText, messageBodyText.contains(order.getCustomerName()));
         assertTrue(messageBodyText, messageBodyText.contains(order.getItemResponse().getItemUrl()));
         assertTrue(messageBodyText, messageBodyText.contains(order.getText()));
     }
 
     @Test
     public void testMessageNotInBodyText() throws MessagingException, IOException {
         Multipart messageBody = emailConfirmationSender.composeMessageBody(order);
 
         String messageBodyText = (String) messageBody.getBodyPart(0).getContent();
         assertFalse(messageBodyText, messageBodyText.contains(order.getItemResponse().getImgBaseSrc()));
     }
 }
