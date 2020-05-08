 package pt.com.broker.performance.distributed;
 
 import java.io.BufferedWriter;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.net.HttpURLConnection;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.TreeMap;
 import java.util.concurrent.CountDownLatch;
 
 import org.caudexorigo.cli.CliFactory;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.text.RandomStringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.client.BrokerClient;
 import pt.com.broker.client.messaging.BrokerListener;
 import pt.com.broker.performance.distributed.DistTestParams.ClientInfo;
 import pt.com.broker.performance.distributed.conf.ConfigurationInfo;
 import pt.com.broker.performance.distributed.conf.ConfigurationInfo.AgentInfo;
 import pt.com.broker.performance.distributed.conf.Consumers.Consumer;
 import pt.com.broker.performance.distributed.conf.Machines.Machine;
 import pt.com.broker.performance.distributed.conf.Producers.Producer;
 import pt.com.broker.performance.distributed.conf.Tests.Test;
 import pt.com.broker.types.NetBrokerMessage;
 import pt.com.broker.types.NetNotification;
 import pt.com.broker.types.NetSubscribe;
 import pt.com.broker.types.NetAction.DestinationType;
 
 public class TestManager implements BrokerListener
 {
 	private static final Logger log = LoggerFactory.getLogger(TestManager.class);
 
 	private static String TEST_MANAGEMENT_BASE = "/perf-test/management";
 	public static String TEST_MANAGEMENT_ACTION = TEST_MANAGEMENT_BASE + "/action/";
 	public static String TEST_MANAGEMENT_RESULT = TEST_MANAGEMENT_BASE + "/result";
 	public static String TEST_MANAGEMENT_LOCAL_MANAGERS = TEST_MANAGEMENT_BASE + "/localmanager/";
 	
 
 	private BrokerClient brokerClient;
 
 	private String hostname;
 	private int port;
 
 	private TreeMap<String, DistTestParams> tests = new TreeMap<String, DistTestParams>();
 
 	private TreeMap<String, List<TestResult>> results = new TreeMap<String, List<TestResult>>();
 
 	private HashMap<String, AgentInfo> agents;
 	private HashMap<String, MachineConfiguration> machineConfigurations = new HashMap<String, MachineConfiguration>();
 
 	private StringBuilder testResults = new StringBuilder();
 
 	private int numberOfMessages;
 	private int messageSize;
 
 	public static void main(String[] args) throws Throwable
 	{
 		final DistTestCliArgs cargs = CliFactory.parseArguments(DistTestCliArgs.class, args);
 
 		TestManager testManager = new TestManager();
 
 		testManager.numberOfMessages = cargs.getNumberOfMessages();
 		testManager.messageSize = cargs.getMessageLength();
 
 		testManager.hostname = cargs.getHost();
 		testManager.port = cargs.getPort();
 
 		testManager.init();
 
 		System.out.println(String.format("Test manger running..."));
 
 		testManager.start();
 
 		System.out.println(String.format("Tests ended!"));
 
 		for (String testname : testManager.results.keySet())
 		{
 			testManager.showTestResult(testname, testManager.tests.get(testname), testManager.results.get(testname));
 		}
 
 		testManager.stop();
 	}
 	
 	private void init()
 	{
 		ConfigurationInfo.init();
 		agents = ConfigurationInfo.getAgents();
 
 		loadMachineConfiguration();
 		addTests();
 	}
 
 	private void start()
 	{
 		try
 		{
 			brokerClient = new BrokerClient(hostname, port);
 			
 			NetSubscribe netSubscribe = new NetSubscribe(TEST_MANAGEMENT_RESULT, DestinationType.QUEUE);
 			
 			brokerClient.addAsyncConsumer(netSubscribe, this);
 			
 			System.out.println("Starting to configure participant machines");
 			
 			// Init remote consumers and producers
 			for(String machine : machineConfigurations.keySet())
 			{
 				MachineConfiguration machineConfiguration = machineConfigurations.get(machine);
 				byte[] data = machineConfiguration.serialize();
 				
 				NetBrokerMessage netBrokerMessage = new NetBrokerMessage(data);
 				String destination = String.format("%s%s", TEST_MANAGEMENT_LOCAL_MANAGERS, machineConfiguration.getMachineName());
 				
 				System.out.println("Sendind machine info to destination " + destination);
 				
 				brokerClient.enqueueMessage(netBrokerMessage, destination);
 			}
 			
 			// Give some time to init
 			Sleep.time(2000);
 			
 			
 			// Run tests			
 			for (String testName : tests.keySet())
 			{
 				DistTestParams distTestParams = tests.get(testName);
 				executeTest(distTestParams);
 			}
 			
 			System.out.println("Stoping remote machines");
 			for(String machine : machineConfigurations.keySet())
 			{
 				MachineConfiguration machineConfiguration = machineConfigurations.get(machine);
 				
 				machineConfiguration.setStop(true);
 				byte[] data = machineConfiguration.serialize();
 				machineConfiguration.setStop(false);
 				
 				NetBrokerMessage netBrokerMessage = new NetBrokerMessage(data);
 				
 				brokerClient.enqueueMessage(netBrokerMessage, String.format("%s%s", TEST_MANAGEMENT_LOCAL_MANAGERS, machineConfiguration.getMachineName()));
 			}
 			
 		}
 		catch (Throwable e)
 		{
 			log.error("Tests failed!", e);
 		}
 	}
 
 	private void stop()
 	{
 		brokerClient.close();
 		writeResult();
 	}
 
 	private void loadMachineConfiguration()
 	{
 		for(Machine machine : ConfigurationInfo.getConfiguration().getMachines().getMachine())
 		{
 			MachineConfiguration machineConfiguration = new MachineConfiguration(machine.getMachineName(), machine.getProducers(), machine.getConsumers());
 			
 			this.machineConfigurations.put(machineConfiguration.getMachineName(), machineConfiguration);
 			
 			System.out.println(String.format("Added machine info for machine : '%s'", machineConfiguration.getMachineName()));
 		}
 	}
 
 	private void addTests()
 	{
 		AgentInfo defaultAgent = ConfigurationInfo.getDefaultAgent();
 
 		for (Test t : ConfigurationInfo.getConfiguration().getTests().getTest())
 		{
 			String testName = t.getTestName();
 
 			// destination info
 			DestinationType destinationType = DestinationType.valueOf(t.getDestination().getDestinationType());
 			boolean isSyncConsumer = t.getDestination().isSyncConsumer().booleanValue();
 
 			// messages info
 			int messageSize = t.getMessages().getMessageSize().intValue();
 			if (this.messageSize != -1)
 			{
 				messageSize = this.messageSize;
 			}
 			int nrMessages = t.getMessages().getNumberOfMessages().intValue();
 			if (this.numberOfMessages != -1)
 			{
 				nrMessages = this.numberOfMessages;
 			}
 
 			String randName = RandomStringUtils.randomAlphanumeric(15);
 			DistTestParams distTestParams = new DistTestParams(testName, String.format("/perf/%s/%s", destinationType.toString().toLowerCase(), randName), destinationType, messageSize, nrMessages, isSyncConsumer);
 
 			/*
 			 * consumers info
 			 */
 			int consumerCount = t.getConsumers().getCount().intValue();
 			// get specified consumer info
 
 			for (Consumer consumer : t.getConsumers().getConsumer())
 			{
 				AgentInfo agentInfo = ConfigurationInfo.getAgents().get(consumer.getAgentId());
 
 				DistTestParams.ClientInfo clientInfo = new DistTestParams.ClientInfo(consumer.getName(), agentInfo.hostname, agentInfo.tcpPort);
 
 				distTestParams.getConsumers().put(consumer.getName(), clientInfo);
 			}
 			// generate remaining
 			for (int i = 1; i <= consumerCount; ++i)
 			{
 				String consumerName = String.format("consumer%s", i + "");
 				if (distTestParams.getConsumers().get(consumerName) != null)
 				{
 					continue;
 				}
 				DistTestParams.ClientInfo clientInfo = new DistTestParams.ClientInfo(consumerName, defaultAgent.hostname, defaultAgent.tcpPort);
 				distTestParams.getConsumers().put(clientInfo.getName(), clientInfo);
 			}
 
 			/*
 			 * producers info
 			 */
 			int producersCount = t.getProducers().getCount().intValue();
 			// get specified consumer info
 
 			for (Producer producer : t.getProducers().getProducer())
 			{
 				AgentInfo agentInfo = ConfigurationInfo.getAgents().get(producer.getAgentId());
 
 				DistTestParams.ClientInfo clientInfo = new DistTestParams.ClientInfo(producer.getName(), agentInfo.hostname, agentInfo.tcpPort);
 				distTestParams.getConsumers().put(producer.getName(), clientInfo);
 			}
 			// generate remaining
 			for (int i = 1; i <= producersCount; ++i)
 			{
 				String producerName = String.format("producer%s", i + "");
 				if (distTestParams.getConsumers().get(producerName) != null)
 				{
 					continue;
 				}
 				DistTestParams.ClientInfo clientInfo = new DistTestParams.ClientInfo(producerName, defaultAgent.hostname, defaultAgent.tcpPort);
 				distTestParams.getConsumers().put(clientInfo.getName(), clientInfo);
 			}
 
 			tests.put(distTestParams.getTestName(), distTestParams);
 
 			System.out.println(String.format("Test added: %s", distTestParams.getTestName()));
 		}
 
 	}
 
 	private volatile CountDownLatch testsCountDown;
 
 	private void consumerEnded(TestResult result)
 	{
 		System.out.println("Consumer ended: " + result.getActorName());
 		synchronized (results)
 		{
 			List<TestResult> resultsList = results.get(result.getTestName());
 			resultsList.add(result);
 		}
 
 		testsCountDown.countDown();
 	}
 
 	private void producerEnded(TestResult result)
 	{
 		System.out.println("Producer ended: " + result.getActorName());
 		testsCountDown.countDown();
 	}
 
 	@Override
 	public boolean isAutoAck()
 	{
 		return true;
 	}
 
 	@Override
 	public void onMessage(NetNotification message)
 	{
 		byte[] payload = message.getMessage().getPayload();
 
 		TestResult result = TestResult.deserialize(payload);
 
 		if (result.getActorType() == TestResult.ActorType.Consumer)
 		{
 			consumerEnded(result);
 		}
 		else
 		{
 			producerEnded(result);
 		}
 	}
 
 	private void executeTest(DistTestParams distTestParams)
 	{
 		testsCountDown = new CountDownLatch(distTestParams.getConsumers().size() + distTestParams.getProducers().size());
 
 		System.out.println(String.format("\nStarting test '%s'", distTestParams.getTestName()));
 
 		synchronized (results)
 		{
 			results.put(distTestParams.getTestName(), new ArrayList<TestResult>(distTestParams.getConsumers().size()));
 		}
 
 		for (String consumer : distTestParams.getConsumers().keySet())
 		{
 			ClientInfo clientInfo = distTestParams.getConsumers().get(consumer);
 
 			byte[] serializedData = distTestParams.serialize(clientInfo);
 			NetBrokerMessage netBrokerMsg = new NetBrokerMessage(serializedData);
 
 			brokerClient.enqueueMessage(netBrokerMsg, TEST_MANAGEMENT_ACTION + consumer);
 		}
 		// wait for 1s
 		Sleep.time(1000);
 		for (String producer : distTestParams.getProducers().keySet())
 		{
 			ClientInfo clientInfo = distTestParams.getProducers().get(producer);
 
 			byte[] serializedData = distTestParams.serialize(clientInfo);
 			NetBrokerMessage netBrokerMsg = new NetBrokerMessage(serializedData);
 
 			brokerClient.enqueueMessage(netBrokerMsg, TEST_MANAGEMENT_ACTION + producer);
 		}
 
 		try
 		{
 			testsCountDown.await(); // Eventually use a timeout to prevent deadlocks in case a actor fails
 			System.out.println(String.format("Test '%s' ended", distTestParams.getTestName()));
 			if (distTestParams.getDestinationType() == DestinationType.QUEUE)
 			{
 				for (String agentId : agents.keySet())
 				{
 					AgentInfo agentInfo = agents.get(agentId);
 					deleteQueue(agentInfo.hostname, agentInfo.httpPort, distTestParams.getDestination());
 				}
 			}
 		}
 		catch (InterruptedException e)
 		{
 			log.error("InterruptedException while waiting on testCountDown", e);
 		}
 
 	}
 	
 	
 
 	private void showTestResult(String testname, DistTestParams testParams, List<TestResult> testResults)
 	{
 		final double nano2second = (1000 * 1000 * 1000);
 
 		StringBuilder sb = new StringBuilder();
 
 		sb.append("\n--------------------------------------------------\n");
 		sb.append("TEST: " + testname);
 		sb.append(String.format("Consumers: %s, Producers: %s, Destination Type: %s, Sync Consumer: %s\nMessage size: %s, Number of messages: %s\n\n", testParams.getConsumers().size(), testParams.getProducers().size(), testParams.getDestinationType(), testParams.isSyncConsumer(), testParams.getMessageSize(), testParams.getNumberOfMessagesToSend()));
 
 		double timePerMsgAcc = 0;
 		double messagesPerSecondAcc = 0;
 
 		for (TestResult tRes : testResults)
 		{
 			double timePerMsg = (((tRes.getTime())) / tRes.getMessages()) / nano2second;
 			double messagesPerSecond = 1 / timePerMsg;
			sb.append(String.format("Consumer: %s, Messages: %s, Time: %,.2f, Messages/second: %,.2f", tRes.getActorName(), tRes.getMessages(), tRes.getTime() / nano2second, messagesPerSecond));
 
 			timePerMsgAcc += timePerMsg;
 			messagesPerSecondAcc += messagesPerSecond;
 		}
 
 		sb.append(String.format("\nAVERAGE: Messages/second: %,.3f", messagesPerSecondAcc / testResults.size()));
 
 		System.out.println(sb.toString());
 
 		this.testResults.append(sb);
 	}
 
 	
 	private void writeResult()
 	{
 		FileWriter fstream;
 		try
 		{
 			fstream = new FileWriter("results.txt");
 			BufferedWriter out = new BufferedWriter(fstream);
 			out.write(this.testResults.toString());
 			out.close();
 		}
 		catch (IOException e)
 		{
 			log.error("Failed to write file", e);
 		}
 
 	}
 
 	private static void deleteQueue(String hostname, int port, String queueName)
 	{
 		log.info("Deleting queue '{}', from host '{}'", queueName, hostname + ":" + port);
 
 		try
 		{
 			String agentUrl = String.format("http://%s:%s/broker/admin", hostname, port + "");
 
 			URL url = new URL(agentUrl);
 			URLConnection connection = url.openConnection();
 
 			HttpURLConnection httpUrlconn = (HttpURLConnection) connection;
 
 			httpUrlconn.setDoOutput(true);
 			httpUrlconn.setConnectTimeout(500);
 			httpUrlconn.setReadTimeout(60000);
 
 			OutputStreamWriter wr = new OutputStreamWriter(httpUrlconn.getOutputStream());
 			wr.write("QUEUE:" + queueName);
 
 			wr.flush();
 
 			int respCode = httpUrlconn.getResponseCode();
 			if (respCode == HttpURLConnection.HTTP_OK)
 			{
 				log.debug("Queue '{}' deleted", queueName);
 			}
 			else
 			{
 				log.debug("Failed to delete queue '{}'", queueName);
 			}
 
 			wr.close();
 		}
 		catch (Throwable t)
 		{
 			log.error(String.format("Failed to connect to agent '%s:%s' to delete queue '%s':.", hostname, port, queueName), t);
 		}
 	}
 
 }
