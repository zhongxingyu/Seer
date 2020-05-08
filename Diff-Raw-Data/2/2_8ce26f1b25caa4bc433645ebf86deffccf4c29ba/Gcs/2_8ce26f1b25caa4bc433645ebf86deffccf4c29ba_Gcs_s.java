 package pt.com.gcs.messaging;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.Collections;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.mina.common.ConnectFuture;
 import org.apache.mina.common.DefaultIoFilterChainBuilder;
 import org.apache.mina.common.IoSession;
 import org.apache.mina.filter.codec.ProtocolCodecFilter;
 import org.apache.mina.filter.executor.ExecutorFilter;
 import org.apache.mina.filter.traffic.ReadThrottleFilter;
 import org.apache.mina.filter.traffic.ReadThrottlePolicy;
 import org.apache.mina.filter.traffic.WriteThrottleFilter;
 import org.apache.mina.filter.traffic.WriteThrottlePolicy;
 import org.apache.mina.transport.socket.SocketAcceptor;
 import org.apache.mina.transport.socket.SocketConnector;
 import org.apache.mina.transport.socket.SocketSessionConfig;
 import org.apache.mina.transport.socket.nio.NioSocketAcceptor;
 import org.apache.mina.transport.socket.nio.NioSocketConnector;
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.CustomExecutors;
 import org.caudexorigo.concurrent.Sleep;
 import org.caudexorigo.text.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.gcs.conf.AgentInfo;
 import pt.com.gcs.conf.WorldMap;
 import pt.com.gcs.net.Peer;
 import pt.com.gcs.net.codec.GcsCodec;
 import pt.com.gcs.tasks.Connect;
 import pt.com.gcs.tasks.GcsExecutor;
 
 public class Gcs
 {
 	private static Logger log = LoggerFactory.getLogger(Gcs.class);
 
 	private static final int NCPU = Runtime.getRuntime().availableProcessors();
 
 	private static final int IO_THREADS = NCPU + 1;
 
 	private static final String SERVICE_NAME = "SAPO GCS";
 
 	private static final int ONE_K = 1024;
 
 	private static final Gcs instance = new Gcs();
 
 	private SocketAcceptor acceptor;
 
 	private SocketConnector connector;
 
 	private Gcs()
 	{
 		log.info("{} starting.", SERVICE_NAME);
 		try
 		{
 		
 			startAcceptor(AgentInfo.getAgentPort());
 			startConnector();
 			connectToAllPeers();
 			
 			GcsExecutor.scheduleWithFixedDelay(new QueueAwaker(), 5, 5, TimeUnit.SECONDS);
 			GcsExecutor.scheduleWithFixedDelay(new QueueCounter(), 20, 20, TimeUnit.SECONDS);
 			GcsExecutor.scheduleWithFixedDelay(new WorldMapMonitor(), 120, 120, TimeUnit.SECONDS);
 		}
 		catch (Throwable t)
 		{
 			Throwable rootCause = ErrorAnalyser.findRootCause(t);
 			log.error(rootCause.getMessage(), rootCause);
 			Shutdown.now();
 		}
 		Sleep.time(AgentInfo.getInitialDelay());
 
 	}
 
 	private void startAcceptor(int portNumber) throws IOException
 	{
 		acceptor = new NioSocketAcceptor(IO_THREADS);
 
 		acceptor.setReuseAddress(true);
 		((SocketSessionConfig) acceptor.getSessionConfig()).setReuseAddress(true);
 		((SocketSessionConfig) acceptor.getSessionConfig()).setTcpNoDelay(false);
 
 		acceptor.setBacklog(100);
 
 		DefaultIoFilterChainBuilder filterChainBuilder = acceptor.getFilterChain();
 
 		ReadThrottleFilter readThrottleFilter = new ReadThrottleFilter(Executors.newSingleThreadScheduledExecutor(), ReadThrottlePolicy.BLOCK, 256 * ONE_K, 512 * ONE_K, 1024 * ONE_K);
 		WriteThrottleFilter writeThrottleFilter = new WriteThrottleFilter(WriteThrottlePolicy.BLOCK, 0, 128 * ONE_K, 0, 256 * ONE_K, 0, 512 * ONE_K);
 
 		// Add CPU-bound job first,
 		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));
 		// and then a thread pool.
 		filterChainBuilder.addLast("executor", new ExecutorFilter(CustomExecutors.newThreadPool(16)));
 		filterChainBuilder.addLast("readThrottleFilter", readThrottleFilter);
 		filterChainBuilder.addLast("writeThrottleFilter", writeThrottleFilter);
 
 		acceptor.setHandler(new GcsAcceptorProtocolHandler());
 
 		// Bind
 		acceptor.bind(new InetSocketAddress(portNumber));
 
 		String localAddr = acceptor.getLocalAddress().toString();
 		log.info("{} listening on: '{}'.", SERVICE_NAME, localAddr);
 	}
 
 	private void startConnector()
 	{
 		connector = new NioSocketConnector(IO_THREADS);
 		
 		DefaultIoFilterChainBuilder filterChainBuilder = connector.getFilterChain();
 
 		// Add CPU-bound job first,
 		filterChainBuilder.addLast("GCS_CODEC", new ProtocolCodecFilter(new GcsCodec()));
 
 		// and then a thread pool.
 		ReadThrottleFilter readThrottleFilter = new ReadThrottleFilter(Executors.newSingleThreadScheduledExecutor(), ReadThrottlePolicy.BLOCK, 128 * ONE_K, 256 * ONE_K, 512 * ONE_K);
 		filterChainBuilder.addLast("executor", new ExecutorFilter(CustomExecutors.newThreadPool(16)));
 		filterChainBuilder.addLast("readThrottleFilter", readThrottleFilter);
 
 		// filterChainBuilder.addLast("executor", new ExecutorFilter(new
 		// OrderedThreadPoolExecutor(0, 16, 30, TimeUnit.SECONDS, new
 		// IoEventQueueThrottle(2 * 65536))));
 
 		connector.setHandler(new GcsRemoteProtocolHandler());
 		// connector.setConnectTimeout(2);
 	}
 	
 	private void connectToAllPeers()
 	{
 		List<Peer> peerList = WorldMap.getPeerList();
 		for (Peer peer : peerList)
 		{
 			GcsExecutor.execute(new Connect(peer));
 		}
 	}
 
 
 
 	public static void connect(String host, int port)
 	{
 		SocketAddress addr = new InetSocketAddress(host, port);
 		connect(addr);
 	}
 
 	public static void connect(SocketAddress address)
 	{
 		String message = "Connecting to '{}'.";
 		log.info(message, address.toString());
 
 		ConnectFuture cf = instance.connector.connect(address).awaitUninterruptibly();
 		Sleep.time(2000);
 		while (!cf.isConnected())
 		{
 			log.info(message, address.toString());
 			cf = instance.connector.connect(address).awaitUninterruptibly();
 			Sleep.time(2000);
 		}
 	}
 	
 	public static void init()
 	{
 		instance.iinit();
 	}
 
 	private void iinit()
 	{
 		String[] virtual_queues = DbStorage.getVirtualQueuesNames();
 
 		for (String vqueue : virtual_queues)
 		{
 			System.out.println(vqueue);
 			iaddQueueConsumer(vqueue, null);
 		}	
 		log.info("{} initialized.", SERVICE_NAME);
 	}
 
 	public static void publish(Message message)
 	{
 		instance.ipublish(message);
 	}
 
 	private void ipublish(final Message message)
 	{
 		message.setType(MessageType.COM_TOPIC);
 		LocalTopicConsumers.notify(message);
 		RemoteTopicConsumers.notify(message);
 	}
 
 	public static void enqueue(final Message message)
 	{
 		instance.ienqueue(message);
 	}
 
 	private void ienqueue(final Message message)
 	{
 		QueueProcessorList.get(message.getDestination()).store(message);
 	}
 
 	public static void ackMessage(String queueName, final String msgId)
 	{
 		instance.iackMessage(queueName, msgId);
 	}
 
 	private void iackMessage(String queueName, final String msgId)
 	{
 		QueueProcessorList.get(queueName).ack(msgId);
 	}
 
 	public static void addTopicConsumer(String topicName, MessageListener listener)
 	{
 		instance.iaddTopicConsumer(topicName, listener);
 	}
 
 	private void iaddTopicConsumer(String topicName, MessageListener listener)
 	{
 		if (listener != null)
 		{
 			LocalTopicConsumers.add(topicName, listener, true);
 		}
 	}
 
 	public static void addQueueConsumer(String queueName, MessageListener listener)
 	{
 		instance.iaddQueueConsumer(queueName, listener);
 	}
 
 	private void iaddQueueConsumer(String queueName, MessageListener listener)
 	{	
 		if (StringUtils.contains(queueName, "@"))
 		{
 			DispatcherList.create(queueName);
 		}
 
 		if (listener != null)
 		{			
 			LocalQueueConsumers.add(queueName, listener);
 		}
 	}
 
 	public static void removeTopicConsumer(MessageListener listener)
 	{
 		LocalTopicConsumers.remove(listener);
 	}
 
 	public static void removeQueueConsumer(MessageListener listener)
 	{
 		LocalQueueConsumers.remove(listener);
 	}
 
 	public static List<Peer> getPeerList()
 	{
 		return WorldMap.getPeerList();
 	}
 
 	public static Set<IoSession> getManagedConnectorSessions()
 	{
 		return Collections.unmodifiableSet(instance.connector.getManagedSessions());
 	}
 
 	public static Set<IoSession> getManagedAcceptorSessions()
 	{
 		return Collections.unmodifiableSet(instance.acceptor.getManagedSessions());
 	}
 
 }
