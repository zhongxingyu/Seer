 package at.ac.tuwien.complang.sbc11.jms;
 
 import java.util.Hashtable;
 
 import javax.jms.Connection;
 import javax.jms.ConnectionFactory;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
import javax.jms.MessageProducer;
 import javax.jms.Session;
 import javax.jms.TextMessage;
 import javax.naming.Context;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 
 import at.ac.tuwien.complang.sbc11.parts.Part;
 
 public class JMSCloudImpl{
 
 	Part read() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	void write(Part computerPart) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	void delete(Part computerPart) {
 		// TODO Auto-generated method stub
 
 	}
 	
 	public static void main(String[] args) 
 	{
 	
 		try 
 		{
	    Hashtable<String, String> properties = new Hashtable<String, String>();
 	    properties.put(Context.INITIAL_CONTEXT_FACTORY, 
 	                   "org.exolab.jms.jndi.InitialContextFactory");
 	    properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
 
 	    Context context = new InitialContext(properties);
 	    
 	    ConnectionFactory factory = 
 	            (ConnectionFactory) context.lookup("ConnectionFactory");
 	    Connection connection = factory.createConnection();
 	    
 
 	    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 	 
 	    Destination destination = (Destination) context.lookup("topic1");
 	 
 	    connection.start();
 	    MessageProducer sender = session.createProducer(destination);
 	    TextMessage message;
 		
 			message = session.createTextMessage("Hello World!");
 
 	    sender.send(message);
 	    
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		getMessage();
 	}
 	
 	public static void getMessage()
 	{
	    Hashtable<String, String> properties = new Hashtable<String, String>();
 	    properties.put(Context.INITIAL_CONTEXT_FACTORY, 
 	                   "org.exolab.jms.jndi.InitialContextFactory");
 	    properties.put(Context.PROVIDER_URL, "tcp://localhost:3035/");
 
 	    Context context;
 		try {
 			context = new InitialContext(properties);
 
 	    
 	    ConnectionFactory factory = 
 	            (ConnectionFactory) context.lookup("ConnectionFactory");
 	    Connection connection = factory.createConnection();
 	    
 	    Session session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 	    
 		Destination destination = (Destination) context.lookup("topic1");
 		MessageConsumer receiver = session.createConsumer(destination );
 	    receiver.setMessageListener(new MessageListener() 
 	    {
 	        public void onMessage(Message message) {
 	            TextMessage text = (TextMessage) message;
 	            System.out.println("Received message: " + text);
 	        }
 	    });
 
 	    // start the connection to enable message delivery
 	    connection.start();
 	    
 		} catch (NamingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 }
