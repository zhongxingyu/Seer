 package cn.mobiledaily.domain.mobile;
 
 import javapns.Push;
 import javapns.communication.exceptions.CommunicationException;
 import javapns.communication.exceptions.KeystoreException;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;
 import org.springframework.stereotype.Component;
 
 import java.util.List;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Johnson
  * Date: 3/4/13
  * Time: 11:28 PM
  * To change this template use File | Settings | File Templates.
  */
@Component(value = "pusher")
 public class Pusher {
     private static final Logger LOGGER = LoggerFactory.getLogger(Pusher.class);
    @Value("${mobile.apns.keystore}")
     private String keyStore;
    @Value("${mobile.apns.keystorepassword}")
     private String keyStorePassword;
 
     public void push(final String message, final String serviceToken) {
         try {
             Push.alert(message, keyStore, keyStorePassword, false, serviceToken);
         } catch (CommunicationException | KeystoreException e) {
             LOGGER.error(e.getMessage());
         }
     }
 
     public void push(final String message, final List<String> serviceTokens) {
         try {
             Push.alert(message, keyStore, keyStorePassword, false, serviceTokens);
         } catch (CommunicationException | KeystoreException e) {
             LOGGER.error(e.getMessage());
         }
     }

 }
