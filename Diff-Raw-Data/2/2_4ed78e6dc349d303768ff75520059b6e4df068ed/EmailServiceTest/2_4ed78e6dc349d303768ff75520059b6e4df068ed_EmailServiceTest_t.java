 package org.sukrupa.app.services;
 

 import org.junit.Before;
 import org.junit.Test;
 import org.mockito.Mock;
 import org.sukrupa.platform.config.AppConfiguration;
 
 
 import javax.mail.internet.AddressException;
 import javax.mail.internet.InternetAddress;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Mockito.verify;
 import static org.mockito.MockitoAnnotations.initMocks;
 
 public class EmailServiceTest {
 
     private EmailService emailService;
     @Mock
     AppConfiguration appConfiguration;
 
 
     @Before
     public void setUp() {
         initMocks(this);
         emailService = new EmailService(appConfiguration);
     }
 
     @Test
     public void shouldSendEmail() {
         emailService.sendEmail("sabhinay@thoughtworks.com", "Testing Email service");
         verify(appConfiguration).properties();
     }
 
     @Test
     public void shouldConvertStringToInternetAddress() throws AddressException {
         String testEmailAddress = "sabhinay@thoughtworks.com";
 
         InternetAddress actualInternetAddress = emailService.convertStringToInternetAddress(testEmailAddress);
         InternetAddress expectedInternetAddress = new InternetAddress(testEmailAddress);
         
         assertThat(actualInternetAddress, is(expectedInternetAddress));
     }
 }
