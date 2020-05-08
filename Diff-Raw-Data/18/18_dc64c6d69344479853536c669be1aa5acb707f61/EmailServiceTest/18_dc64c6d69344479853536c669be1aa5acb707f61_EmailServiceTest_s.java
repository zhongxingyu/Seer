 package org.sukrupa.app.services;
 
 
 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.sukrupa.platform.config.AppConfiguration;
 
 import javax.mail.Address;
 import javax.mail.MessagingException;
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 
 import java.io.IOException;
 import java.util.Properties;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.mock;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class EmailServiceTest {
 
     private EmailService emailService;
     @Mock
     private  AppConfiguration appConfiguration;
 
 
     @Before
     public void setUp() {
         initMocks(this);
         emailService = new EmailService(appConfiguration);
     }
 
     @Test
     public void shouldSendEmail() throws MessagingException {
//        String toAddress = "aravindp@thoughtworks.com";
//        String subject="Hai";
//        String comments="Thanks";
//        when(appConfiguration.properties()).thenReturn(new Properties());
//        assertThat(emailService.sendEmail(toAddress,subject,comments), is(true));
//        Aravind to work on tests.
     }
 
     @Test
         public void shouldSendEmailEventually() throws MessagingException {
         //emailService.sendEmail("sabhinay@thoughtworks.com", "Testing Email service");
         // Anita, Sri, will come back and finish this off once we figured out how to test it
     }
 
     @Test
     public void shouldConvertStringToInternetAddress() throws AddressException {
         String testEmailAddress = "sabhinay@thoughtworks.com";
         InternetAddress expectedInternetAddress = new InternetAddress(testEmailAddress);
 
         InternetAddress actualInternetAddress = emailService.convertStringToInternetAddress(testEmailAddress);
 
         assertThat(actualInternetAddress, is(expectedInternetAddress));
     }
 
     @Test(expected = AddressException.class)
     public void shouldThrowAnAddressExceptionForInvalidEmailAddress() throws AddressException {
         String invalidEmailAddress = "notvalid@";
 
         emailService.convertStringToInternetAddress(invalidEmailAddress);
 
     }
 
     @Test
     public void shouldCreateMimeMessageAndRecipientAsTo() throws MessagingException {
         String excpectedSubject = "A subject";
         when(appConfiguration.properties()).thenReturn(new Properties());
 
         InternetAddress expectedRecipient = new InternetAddress("anitam@thoughtworks.com");
         MimeMessage mimeMessage = emailService.createMimeMessageWithSubjectAndRecipientAsTo(expectedRecipient, excpectedSubject);
 
         assertThat(mimeMessage.getSubject(), is(excpectedSubject));
        assertThat(mimeMessage.getRecipients(MimeMessage.RecipientType.TO)[0], is((Address)expectedRecipient));
     }
 
     @Test
     public void shouldCreateMessageWithCommentsAndRecipientAsTo() throws  MessagingException ,IOException{
         String subject="Hai";
         String comments="Thanks";
         InternetAddress recipient =new InternetAddress("aravindp@thoughtworks.com");
         when(appConfiguration.properties()).thenReturn(new Properties());
 
         MimeMessage message =emailService.createMimeMessageWithSubjectAndRecipientAsTo(recipient,subject,comments);
 
         assertThat(message.getRecipients(MimeMessage.RecipientType.TO)[0], is((Address)recipient));
         assertThat(message.getSubject(), is(subject));
         assertThat((String)message.getContent(),is(comments));
 
     }
 }
