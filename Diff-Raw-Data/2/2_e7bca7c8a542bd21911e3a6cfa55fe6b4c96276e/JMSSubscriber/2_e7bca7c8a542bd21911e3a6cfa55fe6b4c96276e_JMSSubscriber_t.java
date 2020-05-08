 /**
  * Copyright (c) 2013 HAN University of Applied Sciences
  * Arjan Oortgiese
  * Joëll Portier
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  */
 package nl.han.dare2date.jms;
 
 import nl.han.dare2date.logger.ILogger;
 import nl.han.dare2date.service.jms.util.JMSUtil;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import javax.jms.*;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import java.io.IOException;
 import java.util.Properties;
 
 public class JMSSubscriber implements IJMSSubscriber {
     private Context context;
     private ILogger logger;
     private Connection connection;
     private Session session;
     private MessageConsumer subscriber;
     private String clientID;
 
     @Autowired
     public void setLogger(ILogger logger) {
         this.logger = logger;
     }
 
     /**
      * Set the client ID.
      *
      * @param clientID Client ID.
      */
     public void setClientID(String clientID) {
         this.clientID = clientID;
     }
 
     public boolean connect() {
         try {
             context = getContext();
         } catch (NamingException e) {
             logger.error("Can't create context", e);
             return false;
         } catch (IOException e) {
             logger.error("Can't create context", e);
             return false;
         }
 
        if (clientID == null) {
             connection = JMSUtil.getConnection();
         } else {
             connection = JMSUtil.getConnection(clientID);
         }
 
         try {
             session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
         } catch (JMSException e) {
             logger.error("Can't create session", e);
             return false;
         }
 
         return true;
     }
 
     public boolean registerMessageListener(String topicName, MessageListener listener) {
         Topic topic = null;
         try {
             topic = (Topic)context.lookup(topicName);
         } catch (NamingException e) {
             logger.error("Can't find topic", e);
             return false;
         }
 
         try {
             subscriber =  session.createDurableSubscriber(topic, "jan");
         } catch (JMSException e) {
             logger.error("Can't create consumer", e);
             return false;
         }
 
         try {
             subscriber.setMessageListener(listener);
         } catch (JMSException e) {
             logger.error("Can't set listener", e);
             return false;
         }
 
         try {
             connection.start();
         } catch (JMSException e) {
             logger.error("Can't start connection", e);
             return false;
         }
 
         return false;
     }
 
     private static Context getContext() throws NamingException, IOException {
         Properties props = new Properties();
         props.load(JMSUtil.class.getClassLoader().
                 getResourceAsStream("jndi.properties"));
         return new InitialContext(props);
     }
 }
