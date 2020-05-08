 package com.google.code.qualitas.integration.amqp;
 
 import java.util.Date;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.springframework.amqp.AmqpException;
 import org.springframework.amqp.core.AmqpTemplate;
 import org.springframework.amqp.core.Message;
 import org.springframework.amqp.core.MessageDeliveryMode;
 import org.springframework.amqp.core.MessageProperties;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.code.qualitas.integration.api.InstallationException;
 import com.google.code.qualitas.integration.api.InstallationService;
 import com.google.code.qualitas.integration.api.InstallationOrder;
 
 /**
  * The Class InstallationServiceImpl.
  */
 @Service
 public class InstallationServiceImpl implements InstallationService {
 
     /** The Constant QUALITAS_PROCESS_TYPE_HEADER. */
     private static final String QUALITAS_PROCESS_TYPE_HEADER = "qualitasprocesstype";
 
     /** The Constant QUALITAS_PROCESS_ID_HEADER. */
     private static final String QUALITAS_PROCESS_ID_HEADER = "qualitasprocessid";
 
     /** The Constant QUALITAS_USERNAME_HEADER. */
     private static final String QUALITAS_USERNAME_HEADER = "qualitasusername";
 
     private static final Log LOG = LogFactory.getLog(InstallationServiceImpl.class);
 
     /** The amqp template. */
     @Autowired
     private AmqpTemplate amqpTemplate;
 
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.google.code.qualitas.integration.api.InstallationService#install(
      * byte[], java.lang.String, java.lang.String, long,
      * com.google.code.qualitas.engines.api.core.ProcessType)
      */
     public void install(InstallationOrder processBundleInstallationOrder)
             throws InstallationException {
         MessageProperties messageProperties = new MessageProperties();
 
         // set standard properties
         messageProperties.setContentType(processBundleInstallationOrder.getContentType());
         messageProperties.setContentLength(processBundleInstallationOrder.getBundle().length);
         messageProperties.setTimestamp(new Date());
         messageProperties.setDeliveryMode(MessageDeliveryMode.PERSISTENT);
 
         // set qualitas-specific headers
         messageProperties.getHeaders().put(QUALITAS_PROCESS_TYPE_HEADER,
                 processBundleInstallationOrder.getProcessType());
         messageProperties.getHeaders().put(QUALITAS_USERNAME_HEADER,
                 processBundleInstallationOrder.getUsername());
         messageProperties.getHeaders().put(QUALITAS_PROCESS_ID_HEADER,
                 processBundleInstallationOrder.getProcessId());
 
         Message message = new Message(processBundleInstallationOrder.getBundle(), 
                 messageProperties);
 
         // send the messages
         try {
             amqpTemplate.send(message);
         } catch (AmqpException e) {
             String msg = "Caugh amqp exception while trying to send a message";
             LOG.error(msg, e);
             throw new InstallationException(msg, e);
         }
     }
 
 }
