 package quoteSubMsgSelector;
 
 import javax.jms.JMSException;
 import javax.jms.Message;
 import javax.jms.QueueSession;
 import javax.jms.TextMessage;
 import javax.jms.TopicSession;
 import javax.jms.TopicSubscriber;
 
 import stocks.StockIdentifier;
 
 import quoteSubscriber.QuoteSubscriber;
 
 /**
  * Inherit from QuoteSubscriber, using the same constructor as superclass
  * Message selecting is performed by overriding createTopic method
  * Argument 'selector' is added when create TopicSubscriber
  */
 @SuppressWarnings("serial")
 public class MsgSelector extends QuoteSubscriber implements java.io.Serializable {
 	transient float upperThres;
 	transient float lowerThres;
 	transient public TopicSubscriber topicSubscriber;
 	
 	
 	public MsgSelector(StockIdentifier si, TopicSession topicSession,
 			QueueSession queueSession) {
 		super(si, topicSession, queueSession);
 	}
 
 	/**
 	 * Use topicSession to create topic and topicSubscriber
 	 */
 	@Override
 	public void createTopic() throws JMSException {
 		// System.out.println(" [Subscriber] Alert if rise/fall more than 5%");
 		String selector = "quote >= "+Float.toString(upperThres)+" OR quote <= "+Float.toString(lowerThres);
 		super.topic = topicSession.createTopic(subject);
 		super.topicSubscriber = topicSession.createSubscriber(topic, selector, false);
 		this.topicSubscriber = super.topicSubscriber;
 	}
 	
 	/**
 	 * this method is registered in JMS, is called when a message is available
 	 * Treat initialization messages and normal quote publish messages differently,
 	 * which is a pitfall due to the registrations of listener for both message are same method
 	 * should be improved
 	 * @see javax.jms.MessageListener#onMessage(javax.jms.Message)
 	 */
 	@Override
 	public void onMessage(Message msg) {
 		TextMessage textMsg;
 		String temp;
 		// System.out.println(msg);
 		if(msg instanceof TextMessage) {
 			textMsg = (TextMessage) msg;
 			try {
 				temp = textMsg.getText();
 				parseMessage(temp);
 			} catch (JMSException e) {
 				e.printStackTrace();
 			}
 			
 		}
 		
 		try {
 			if (msg.getJMSType() != null && msg.getJMSType().equals("Init")) {
 				// System.out.println(msg);
				this.upperThres = (float) (super.currentQuote * 1.02);
				this.lowerThres = (float) (super.currentQuote * 0.98);
 				System.out.print("\n [Subscriber] "+s.getValue()+"\n\t\t");
 				System.out.printf("%.2f", currentQuote);
 				System.out.println("\t\t| "+currentTime);
 				System.out.printf("\t\t| UpperThres: ");
 				System.out.printf("%.2f", this.upperThres);
 				System.out.printf("\t\t| LowerThres: ");
 				System.out.printf("%.2f", this.lowerThres);
 				System.out.print("\n");
 				init = true;
 				print = false;
 			} else {
 				System.out.print("\n [Subscriber] **ALERT** | "+s.getValue()+"\n\t\t");
 				System.out.printf("%.2f", currentQuote);
 				System.out.println("\t\t| "+currentTime);
 			}
 		} catch (JMSException e1) {
 			// TODO Auto-generated catch block
 			e1.printStackTrace();
 		}
 	}
 }
