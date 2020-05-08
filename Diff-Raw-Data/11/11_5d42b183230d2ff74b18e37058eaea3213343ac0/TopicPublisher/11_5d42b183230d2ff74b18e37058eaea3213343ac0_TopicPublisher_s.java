 package jms;
 
 import javax.jms.Connection;
 import javax.jms.DeliveryMode;
 import javax.jms.Destination;
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.MessageConsumer;
 import javax.jms.MessageListener;
 import javax.jms.MessageProducer;
 import javax.jms.Queue;
 import javax.jms.QueueConnection;
 import javax.jms.QueueSession;
 import javax.jms.Session;
 import javax.jms.Topic;
 
 import org.apache.activemq.ActiveMQConnectionFactory;
 
 import util.StockExchange;
 import util.StockQuote;
 
 public class TopicPublisher {
 	
     private Connection connection;
     private Session session;
     private MessageProducer publisher;
     private String url;
     private Topic topic;
 	private ActiveMQConnectionFactory factory;
 	private StockExchange exchange;
 	private Queue requestsQueue;
 	private MessageConsumer requestConsumer;
     
 	public TopicPublisher(String url, StockExchange exchange) {
 		this.exchange = exchange;
         if(url == null){
         	this.url = "tcp://localhost:61616";
         }else{
         	this.url = url;
         }
     	init();
     }
     
     public void init(){
 		factory = new ActiveMQConnectionFactory(url);
     	
         try {
 			connection = factory.createConnection();
 			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
 
 			requestsQueue = session.createQueue("requests");
 			requestConsumer = session.createConsumer(requestsQueue);
 			requestConsumer.setMessageListener(new MessageListener() {
				
				@Override
 				public void onMessage(Message arg0) {
					handleRequestMessage(arg0);
 				}
 			});
 			connection.start();
 
 		} catch (JMSException e) {
 			e.printStackTrace();
 		}
     }
     
 	private void handleRequestMessage(Message msg) {
 		System.out.println("request message received!");
 		try {
 			String stockName = msg.getStringProperty("stockName");
 			Destination replyDest = msg.getJMSReplyTo();
 			
 			QueueConnection qConnection = factory.createQueueConnection();
 			QueueSession qSession = qConnection.createQueueSession(false,
 					Session.AUTO_ACKNOWLEDGE);
 			MessageProducer mp = qSession.createProducer(replyDest);
 			mp.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 			mp.send(qSession.createObjectMessage(exchange
 					.getCurrentQuote(stockName)));
 		} catch (JMSException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
     public void publishObjectMessage(StockQuote quote){
         try {
         	topic = session.createTopic("dax."+quote.getName());
 			publisher = session.createProducer(topic);
 	        publisher.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
 			publisher.send(session.createObjectMessage(quote));
 		} catch (JMSException e) {
 			e.printStackTrace();
 		}
     }
     
     public void close(){
         try {
 			connection.stop();
 	        connection.close();
 		} catch (JMSException e) {
 			e.printStackTrace();
 		}
     }
 
 }
