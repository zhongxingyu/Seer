 package pt.com.broker.performance.distributed;
 
 import java.io.UnsupportedEncodingException;
 
 import org.caudexorigo.cli.CliFactory;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.text.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.messaging.BrokerListener;
 import pt.com.broker.performance.distributed.TestResult.ActorType;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetAction.DestinationType;
 
 /**
  * Simple producer sample. Behavior is determined by command line arguments.
  * 
  */
 public class DistProducerApp implements BrokerListener
 {
 	public static final byte STOP_MESSAGE = (byte) 0;
 	public static final byte REGULAR_MESSAGE = (byte) 1;
 
 	private static final Logger log = LoggerFactory.getLogger(DistProducerApp.class);
 
 	private String host;
 	private int port;
 
 	private String actorName;
 
 	private BrokerClient brokerClient;
 	
 	public DistProducerApp(String host, int port, String actorName) throws Throwable
 	{
 		this.host = host;
 		this.port = port;
 		this.actorName = actorName;		
 	
 		String destination = TestManager.TEST_MANAGEMENT_ACTION + actorName;
 		brokerClient = new BrokerClient(host, port);
 	
 		NetSubscribe subscribe = new NetSubscribe(destination, DestinationType.QUEUE);
 
 		brokerClient.addAsyncConsumer(subscribe, this);
 
 		System.out.println(String.format("Producer '%s' running...", actorName));
 
 	}
 
 	public static void main(String[] args) throws Throwable
 	{
 		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);
 	
 		DistProducerApp producer = new DistProducerApp(cargs.getHost(), cargs.getPort(), cargs.getActorName());
 
 	
 
 		while (true)
 		{
 			Sleep.time(5000);
 		}
 
 	}
 
 	private void sendLoop(BrokerClient bk, int messageLength, int nrOfMessages, DestinationType destinationType, String destination) throws Throwable
 	{
 		
 		System.out.println(String.format("Producing %s messages with %s chars.", nrOfMessages, messageLength));
 		
 		final String regularMsgContent = RandomStringUtils.randomAlphanumeric(messageLength - 1);
 		final String stopMsgContent = nrOfMessages + "";
 
 		byte[] regularMessage = getMessage(REGULAR_MESSAGE, regularMsgContent);
 		byte[] stopMessage = getMessage(STOP_MESSAGE, stopMsgContent);
 
 		NetBrokerMessage brokerMessage = new NetBrokerMessage(regularMessage);
 		NetBrokerMessage stopBrokerMessage = new NetBrokerMessage(stopMessage);
 
 		for (int i = 0; i != nrOfMessages; ++i)
 		{
 
 			if (destinationType == DestinationType.QUEUE)
 			{
 				bk.enqueueMessage(brokerMessage, destination);
 			}
 			else
 			{
 				bk.publishMessage(brokerMessage, destination);
 			}
 		}
 
 		System.out.println(actorName + " sending stop messages");
 		for (int i = 0; i != 150; ++i)
 		{
 			if (destinationType == DestinationType.QUEUE)
 			{
 				bk.enqueueMessage(stopBrokerMessage, destination);
 			}
 			else
 			{
 				bk.publishMessage(stopBrokerMessage, destination);
 			}
 
 			if(destinationType == DestinationType.TOPIC)
 			{
 				Sleep.time(50);
 			}
 		}
 		
 		bk.close();
 	}
 
 	private byte[] getMessage(byte headerByte, String messageContent)
 	{
 		byte[] serializedContent = new byte[0];
 		try
 		{
 			serializedContent = messageContent.getBytes("UTF-8");
 		}
 		catch (UnsupportedEncodingException e)
 		{
 			// this will never happen
 		}
 		byte[] serializedMessage = new byte[serializedContent.length + 1];
 
 		serializedMessage[0] = headerByte;
 		System.arraycopy(serializedContent, 0, serializedMessage, 1, serializedContent.length);
 
 		return serializedMessage;
 	}
 
 	private void performTest(DistTestParams testParams)
 	{
 		System.out.println(actorName + " starting new test: " + testParams.getTestName());
 
 		try
 		{
 			BrokerClient bk = new BrokerClient(testParams.getClientInfo().getAgentHost(), testParams.getClientInfo().getPort());
 
 			sendLoop(bk, testParams.getMessageSize(), testParams.getNumberOfMessagesToSend(), testParams.getDestinationType(), testParams.getDestination());
 			
 			bk.close();
 
 			TestResult testResult = new TestResult(ActorType.Procucer, actorName, testParams.getTestName());
 			byte[] data = testResult.serialize();
 
 			NetBrokerMessage netBrokerMessage = new NetBrokerMessage(data);
 
 			String destination = TestManager.TEST_MANAGEMENT_RESULT;
 
 			brokerClient.enqueueMessage(netBrokerMessage, destination);
 			System.out.println(actorName + " ended test " + testParams.getTestName());
 		}
 		catch (Throwable t)
 		{
 			log.error("Test failed", t);
 		}
 	}
 
 	@Override
 	public boolean isAutoAck()
 	{
		return true;
 	}
 
 	@Override
 	public void onMessage(NetNotification notification)
 	{
 		System.out.println("DistProducerApp.onMessage()");
 		
 		byte[] testParams = notification.getMessage().getPayload();
 
 		DistTestParams distTestParams = DistTestParams.deserialize(testParams);
 
 		performTest(distTestParams);
 	}
 
 }
