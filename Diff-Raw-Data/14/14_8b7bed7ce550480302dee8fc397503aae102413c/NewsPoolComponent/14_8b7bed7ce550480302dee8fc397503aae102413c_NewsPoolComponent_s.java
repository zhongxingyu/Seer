 package alma.news;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
import alma.common.vo.News;
 import alma.common.vo.Release;
 
 /**
  * NewsPool Service.
  * 
  * The news pool produces/retrieves some text, which belong to specific
  * categories (it is useless to model further its treatment), and send it to a
  * publish/subscribe component. The latter is accessed by a set of editors that
  * treat the news dispatch for which they are interested in, and thus, they have
  * subscribe to. The pool also sends the news dispatch <em>id</em> to the
  * editor-in-chief, in a <em>secure</em> and <em>acknowledged</em> way.
  * 
  * @author Sylvain Lecoy
  * @author Frederic Dumont
  * 
  */
 public class NewsPoolComponent {
 
 	public static Release release = new Release(0);
 	public static Message releaseMessage;
 	public static String destination;
 
 //	public NewsPoolComponent(String dest) {
 //		this.destination = dest;
 //	}
 
 	public static void main(String argc[]) {
 
 		Context context = null;
 		ConnectionFactory factory = null;
 		Connection connection = null;
 		String factoryName = "ConnectionFactory";
 
 		Destination dest = null;
 		Session session = null;
 		MessageProducer sender = null;
 
 		try {
 			// create the JNDI initial context.
 			context = new InitialContext();
 
 			// look up the ConnectionFactory
 			factory = (ConnectionFactory) context.lookup(factoryName);
 
 			// look up the Destination
 			// destination is the topic containing the release ("topic1")
 			dest = (Destination) context.lookup("topic1");
 
 			// create the connection
 			connection = factory.createConnection();
 
 			// create the session
 			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
 			// create the sender
 			sender = session.createProducer(dest);
 
 			// start the connection, to enable message sends
 			connection.start();
 
 			// create a release
			News news = new News();
			news.setTitle("Hello");
 			release.addNews(news);
 			releaseMessage = session.createObjectMessage(release);
 			
 			sender.send(releaseMessage);
 			System.out.println("Message :" + releaseMessage.getJMSMessageID() + "\n");
 
 		} catch (JMSException exception) {
 			exception.printStackTrace();
 		} catch (NamingException exception) {
 			exception.printStackTrace();
 		} finally {
 			// close the context
 			if (context != null) {
 				try {
 					context.close();
 				} catch (NamingException exception) {
 					exception.printStackTrace();
 				}
 			}
 
 			// close the connection
 			if (connection != null) {
 				try {
 					connection.close();
 				} catch (JMSException exception) {
 					exception.printStackTrace();
 				}
 			}
 		}
 	}
 
 }
