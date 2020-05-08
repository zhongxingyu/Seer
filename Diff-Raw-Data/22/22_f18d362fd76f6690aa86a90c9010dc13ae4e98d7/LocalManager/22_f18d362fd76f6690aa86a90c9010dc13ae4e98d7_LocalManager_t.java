 package pt.com.broker.performance.distributed;
 
 import java.util.concurrent.CountDownLatch;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.atomic.AtomicLong;
 
 import org.caudexorigo.cli.CliFactory;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.text.RandomStringUtils;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.messaging.BrokerListener;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetAction.DestinationType;
 
 public class LocalManager implements BrokerListener
 {
 	private static final Logger log = LoggerFactory.getLogger(LocalManager.class);
 	
 	private ExecutorService executer;
 	
 	private BrokerClient brokerClient;
 
 	private String hostname;
 	private int port;
 
 	private String machineName;
	
 	public static void main(String[] args) throws Throwable
 	{
 		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);
 
 		LocalManager testManager = new LocalManager();
 
 		testManager.machineName = cargs.getMachineName();
 		
 		testManager.hostname = cargs.getHost();
 		testManager.port = cargs.getPort();
 
 		testManager.brokerClient = new BrokerClient(testManager.hostname, testManager.port);
 	
		if(cargs.warmup())
			testManager.warmUp();
 		
 		NetSubscribe subscribe = new NetSubscribe(String.format("%s%s", TestManager.TEST_MANAGEMENT_LOCAL_MANAGERS, testManager.machineName), DestinationType.QUEUE);
 		testManager.brokerClient.addAsyncConsumer(subscribe, testManager);
 				
 		while(true)
 		{
 			Sleep.time(5000);
 		}
 	}
 	
	private void warmUp()
 	{
 		
 		System.out.println("Warming up!");
 		
 		/*******************
 		 * Publish messages 
 		 ******************/		
 		
 		System.out.println(" - Publishing messages...");
 		
 		final int nrOfMsg = 20 * 1000; 
 		final int msgSize =  8 * 1024;
 		
 		String topic = "/topic/warmup/" + RandomStringUtils.randomAlphanumeric(10);
 		String content = RandomStringUtils.random(msgSize);
 		
 		NetBrokerMessage brokerMsg = new NetBrokerMessage(content);
 		
 		for(int i = 0; i != nrOfMsg; ++i)
 		{
 			brokerClient.publishMessage(brokerMsg, topic);
 		}
 		
 		
 		/*******************
 		 * Enqueue messages 
 		 ******************/
 		
 		System.out.println(" - Enqueueing and consuming messages...");
 		
 		String queueName = "/queue/warmup/" + RandomStringUtils.randomAlphanumeric(10);
 		
 		NetSubscribe subscribe = new NetSubscribe(queueName, DestinationType.QUEUE);
 		
 		final AtomicLong count = new AtomicLong(nrOfMsg);
 		
 		final CountDownLatch latch = new CountDownLatch(1);
 		
 		try
 		{
 			brokerClient.addAsyncConsumer(subscribe, new BrokerListener()
 			{				
 				@Override
 				public void onMessage(NetNotification message)
 				{
 					if( count.decrementAndGet() == 0)
 					{
 						latch.countDown();
 					}
 				}
 				
 				@Override
 				public boolean isAutoAck()
 				{
 					return true;
 				}
 			});
 		}
 		catch (Throwable e)
 		{
 			latch.countDown();
 		}
 		
 		for(int i = 0; i != nrOfMsg; ++i)
 		{
 			brokerClient.enqueueMessage(brokerMsg, queueName);
 		}
 		
 		try
 		{
 			latch.await();
 		}
 		catch (InterruptedException e)
 		{
 		}
 		
 		System.out.println("Warming up ended");
 		
 	}
 
 	private void startConsumer(String name)
 	{
 		final String consumerName = name;
 		
 		Runnable runnable = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					System.out.println("Consumer started: " +  consumerName);
 					new DistConsumerApp(hostname, port, consumerName);
 				}
 				catch (Throwable e)
 				{
 					log.error("Consumer initialization failed", e);
 				}
 			}
 		};
 		
 		executer.submit(runnable);
 	}
 
 	private void startProducer(String name)
 	{
 		final String producerName = name;
 		
 		Runnable runnable = new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				try
 				{
 					System.out.println("Producer started: " +  producerName);
 					new DistProducerApp(hostname, port, producerName);
 				}
 				catch (Throwable e)
 				{
 					log.error("Consumer initialization failed", e);
 				}
 			}
 		};
 		
 		executer.submit(runnable);
 	}
 	
 	private void startTest(MachineConfiguration machineConfiguration)
 	{
 		System.out.println(String.format("Starting test, using %s consumers and %s producers.", machineConfiguration.getConsumers().size(), machineConfiguration.getProducers().size()));
 		
 		int actors = 0;
 		if(machineConfiguration.getConsumers() != null)
 		{
 			actors += machineConfiguration.getConsumers().size();
 		}
 		
 		if(machineConfiguration.getProducers() != null)
 		{
 			actors += machineConfiguration.getProducers().size();
 		}
 		
 		executer = Executors.newFixedThreadPool(actors);
 		
 		for(String consumerName :  machineConfiguration.getConsumers() )
 		{
 			startConsumer(consumerName);
 		}
 		
 		for(String producerName :  machineConfiguration.getProducers() )
 		{
 			startProducer(producerName);
 		}
 	}
 	
 
 	private void stopTest(MachineConfiguration machineConfiguration)
 	{
 		//TODO: stop tests
 	}
 	
 
 	@Override
 	public boolean isAutoAck()
 	{
 		return true;
 	}
 
 	@Override
 	public void onMessage(NetNotification message)
 	{
 		try
 		{
 			MachineConfiguration machineConfiguration = MachineConfiguration.deserialize( message.getMessage().getPayload() );
 			if( ! machineConfiguration.isStop() )
 			{
 				startTest(machineConfiguration);
 			}
 			else
 			{
 				stopTest(machineConfiguration);
 			}
 
 		}
 		catch (Throwable t)
 		{
 			log.error("Failed to process received message", t);
 		}
 	}
 }
