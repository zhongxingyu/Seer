 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package Communication.JMS;
 
 import CommunicationInterfaces.CompetitionDTO;
 import java.io.Serializable;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.jms.DeliveryMode;
 import javax.jms.JMSException;
 import javax.jms.ObjectMessage;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.jms.Topic;
 import javax.jms.TopicConnection;
 import javax.jms.TopicConnectionFactory;
 import javax.jms.TopicPublisher;
 import javax.jms.TopicSession;
 import javax.naming.InitialContext;
 
 /**
  * 
  * @author rafa
  * This class should be used
  * when a message should be added to an Topic
  * This Publisher is used for the UseCase when a Coach sends Invitiations to Teammembers
  * Always keep in Mind that when a Message should arrive at an Subscriber
  * The InitialSubcription must be performed
  */
 public class InvitationPublisher {
 
     Properties props;
     InitialContext context;
     TopicConnectionFactory topicConnectionFactory = null;
     TopicConnection topicConnection = null;
     TopicSession topicSession = null;
     Topic topic = null;
     String topicName = null;
     String factoryName = null;
     TopicPublisher topicPublisher = null;
     
     /**
      * 
      * @param connectionFactroyName - > the name of the Factory in the JMS
      * @param topicConnectionName   --> the name of the Topic
      * @param subject               --> the subject of the Message
      * @param messageBody           --> the body of the Message
      * @param competitionDate       --> the Date of the Competition
      */
     public void publishMessages(String connectionFactroyName, String topicConnectionName, CompetitionDTO competitionDTO) {
         
             try {
                 this.factoryName = connectionFactroyName;
                 this.topicName = topicConnectionName;
                 //SET THE PROPERTIES FOR JNDI
                 //NOT NECESSARYLY FOR RUNNING LOCAL BUT WHEN CALLING AN EXTERNAL JMS
                 //THIS SETTINGS SHALL BE SETTED
                 props = new Properties();
 
                 props.setProperty("java.naming.factory.initial",
                         "com.sun.enterprise.naming.SerialInitContextFactory");
 
                 props.setProperty("java.naming.factory.url.pkgs",
                         "com.sun.enterprise.naming");
 
                 props.setProperty("java.naming.factory.state",
                         "com.sun.corba.ee.impl.presentation.rmi.JNDIStateFactoryImpl");
 
                 // optional.  Defaults to localhost.  Only needed if web server is running 
                 // on a different host than the appserver    
                 props.setProperty("org.omg.CORBA.ORBInitialHost", "localhost");
 
                 // optional.  Defaults to 3700.  Only needed if target orb port is not 3700.
                 props.setProperty("org.omg.CORBA.ORBInitialPort", "3700");
 
                 //GET THE CONTEXT
                 //SHOULD ALSO WORK
                context = new InitialContext();
             //   context = new InitialContext(props);
                 //GET THE FACTORY
                 topicConnectionFactory = (TopicConnectionFactory) context.lookup(connectionFactroyName);
                 //GET A TOPICCONNECTION
                 topicConnection = topicConnectionFactory.createTopicConnection();
                 //START A SESSION
                 topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
                 //CHECK IF THE TOPIC IS THERE
                 topic = (Topic) context.lookup(topicConnectionName);
                 //GET A PUBLISHER and set the mode of the delivery to persistent to keep the date in the topic
                 topicPublisher = topicSession.createPublisher(topic);
                 topicPublisher.setDeliveryMode(DeliveryMode.PERSISTENT);
 
 
             } catch (JMSException ee) {
                 System.err.println("Connection problem in publishMessages: " + ee.toString());
                 if (topicConnection != null) {
                     try {
                         topicConnection.close();
                     } catch (Exception e) {
                         System.err.println("Connection problem in catchblock: " + e.toString());
                     }
                 }
             } catch (Exception e) {
                 System.err.println("Connection problem in catchblock: " + e.toString());
             }
             /**
              * initialization related things finished
              * now start with the message related things
             **/
             ObjectMessage message = null;
             
             try {
                 //message = topicSession.createTextMessage();
                 message = topicSession.createObjectMessage(competitionDTO);
                 System.out.println("PUBLISHER: Publishing messag");
                 topicPublisher.publish(message);
                 /*
                  * Send a non-text control message indicating end
                  * of messages.
                  */
                 topicPublisher.publish(topicSession.createMessage());
             } catch (JMSException e) {
                 System.err.println("Exception occurred in publishMessages: " + e.toString());
             }
         } 
 
     /**
      * Closes the connection.
      */
     public void finish() {
         if (topicConnection != null) {
             try {
                 topicConnection.close();
                 topicSession.close();
                 topicPublisher.close();
             } catch (JMSException e) {
                 System.err.println("Exception occurred in finish: " + e.toString());
             }
         }
     } 
 }
