 package pt.com.broker.performance.distributed;
 
 import java.util.concurrent.Callable;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.caudexorigo.cli.CliFactory;
 import org.caudexorigo.concurrent.Sleep;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.messaging.BrokerListener;
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
 	
 		NetSubscribe subscribe = new NetSubscribe(String.format("%s%s", TestManager.TEST_MANAGEMENT_LOCAL_MANAGERS, testManager.machineName), DestinationType.QUEUE);
 		testManager.brokerClient.addAsyncConsumer(subscribe, testManager);
 		
 		
 		while(true)
 		{
 			Sleep.time(5000);
 		}
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
 		
		executer = Executors.newFixedThreadPool(machineConfiguration.getConsumers().size());
 		
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
 		System.out.println("LocalManager.onMessage()");
 		
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
