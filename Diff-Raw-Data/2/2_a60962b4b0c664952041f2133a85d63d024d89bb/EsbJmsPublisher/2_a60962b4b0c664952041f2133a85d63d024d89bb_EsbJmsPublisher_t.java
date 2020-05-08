 /*
  * @author Rob Urquhart
  */
 
 package edu.internet2.middleware.grouper.changeLog.esb.consumer;
 
 import org.apache.commons.logging.Log;
 
 import edu.internet2.middleware.grouper.app.loader.GrouperLoaderConfig;
 import edu.internet2.middleware.grouper.esb.listener.EsbListenerBase;
 import edu.internet2.middleware.grouper.util.GrouperUtil;
 import org.springframework.context.*;
 import org.springframework.context.support.*;
import edu.internet2.middleware.grouper.changeLog.esb.consumer.JmsConnectionBean;
 
 /**
  * 
  * Class to send Grouper events to JMS server, formatted as JSON strings
  * Copied to /opt/grouper/src/esb/edu/internet2/middleware/grouper/changeLog/esb/consumer
  *
  */
 public class EsbJmsPublisher extends EsbListenerBase {
 
   /** */
   private static final Log LOG = GrouperUtil.getLog(EsbJmsPublisher.class);
 
   private ApplicationContext applicationContext;
   private JmsConnectionBean jmsConnectionBean;
 
   /**
    * @see EsbListenerBase#dispatchEvent(String, String)
    */
   @Override
   public boolean dispatchEvent(String eventJsonString, String consumerName) {
 
     if (LOG.isDebugEnabled()) {
       LOG.debug("Consumer " + consumerName + " publishing "
           + GrouperUtil.indent(eventJsonString, false));
     }
 
     if (applicationContext == null) {
         String contextFileName = GrouperLoaderConfig.getPropertyString("changeLog.consumer."
         + consumerName + ".publisher.springContextFileName", "");
     if (LOG.isDebugEnabled()) {
       LOG.debug("Consumer " + consumerName + " loading Spring config from "
           + contextFileName);
     }
         applicationContext = new ClassPathXmlApplicationContext("file:" + contextFileName);
     }
     
     if (jmsConnectionBean==null) {
         jmsConnectionBean = (JmsConnectionBean)applicationContext.getBean("jmsConnectionBean");
     }
 
     jmsConnectionBean.send(eventJsonString);
     
     if (LOG.isDebugEnabled()) {
       LOG.debug("ESB JMS client " + consumerName + " sent message");
     }
     return true;
   }
 
   /**
    * 
    */
   @Override
   public void disconnect() {
     //do nothing, keep xmpp connections open a little longer
   }
 }
