 package pt.com.gcs.messaging;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.LinkedHashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 
 import org.caudexorigo.ErrorAnalyser;
 import org.caudexorigo.Shutdown;
 import org.caudexorigo.concurrent.CustomExecutors;
 import org.caudexorigo.concurrent.Sleep;
 import org.jboss.netty.bootstrap.ClientBootstrap;
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.ChannelFuture;
 import org.jboss.netty.channel.ChannelPipeline;
 import org.jboss.netty.channel.ChannelPipelineFactory;
 import org.jboss.netty.channel.Channels;
 import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import pt.com.broker.types.NetAction.DestinationType;
 import pt.com.gcs.conf.GcsInfo;
 import pt.com.gcs.conf.GlobalConfig;
 import pt.com.gcs.messaging.QueueProcessorList.MaximumQueuesAllowedReachedException;
 import pt.com.gcs.net.Peer;
 import pt.com.gcs.net.codec.GcsDecoder;
 import pt.com.gcs.net.codec.GcsEncoder;
 
 /**
  * Gcs is a facade for handling several message related functionality such as publish, acknowledge, etc.
  * 
  */
 
 public class Gcs
 {
 	private static Logger log = LoggerFactory.getLogger(Gcs.class);
 
 	private static final int NCPU = Runtime.getRuntime().availableProcessors();
 
 	private static final int IO_THREADS = NCPU + 1;
 
 	private static final String SERVICE_NAME = "SAPO GCS";
 
 	private static final Gcs instance = new Gcs();
 
 	public static final int RECOVER_INTERVAL = 50;
 
 	public static final int RECONNECT_INTERVAL = 5000;
 
 	private Set<Channel> agentsConnection = new HashSet<Channel>();
 	
 	private ClientBootstrap connector;
 	
 	public static void ackMessage(String queueName, final String msgId)
 	{
 		instance.iackMessage(queueName, msgId);
 	}
 
 	public static void addAsyncConsumer(String destinationName, MessageListener listener)
 	{
 		if (listener.getSourceDestinationType() == DestinationType.TOPIC)
 		{
 			instance.iaddTopicConsumer(destinationName, listener);
 		}
 		else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
 		{
 			instance.iaddQueueConsumer(destinationName, listener);
 		}
 	}
 
 	protected static void connect(SocketAddress address)
 	{
 		if (GlobalConfig.contains((InetSocketAddress) address))
 		{
 			log.info("Connecting to '{}'.", address.toString());
 
 			ChannelFuture cf = instance.connector.connect(address).awaitUninterruptibly();
 
 			if (!cf.isSuccess())
 			{
 				GcsExecutor.schedule(new Connect(address), RECONNECT_INTERVAL, TimeUnit.MILLISECONDS);
 			}
 			else
 			{
 				synchronized (instance.agentsConnection)
 				{
 					instance.agentsConnection.add(cf.getChannel());
 				}
 			}
 			
 		}
 		else
 		{
 			log.info("Peer '{}' does not appear in the world map, it will be ignored.", address.toString());
 		}
 	}
 
 	public static boolean enqueue(final InternalMessage message)
 	{
 		return instance.ienqueue(message, null);
 	}
 
 	public static boolean enqueue(InternalMessage message, String queueName)
 	{
 		return instance.ienqueue(message, queueName);
 	}
 
 	protected static void reloadWorldMap()
 	{
 		log.info("Reloading the world map");
 		Set<Channel> connectedSessions = getManagedConnectorSessions();
 		
 		ArrayList<Channel> sessionsToClose = new ArrayList<Channel>(connectedSessions.size());
 		
 		for (Channel channel : connectedSessions)
 		{
 			InetSocketAddress inet = (InetSocketAddress) channel.getRemoteAddress();
 
 			// remove connections to agents that were removed from world map
 			if (!GlobalConfig.contains(inet))
 			{
 				log.info("Remove peer '{}'", inet.toString());
 				sessionsToClose.add(channel);
 			}
 		}
 		for(Channel channel : sessionsToClose)
 		{
 			channel.close();
 		}
 		
 		List<InetSocketAddress> remoteSessions = new ArrayList<InetSocketAddress>(connectedSessions.size());
 		for (Channel channel : connectedSessions)
 		{
 			remoteSessions.add((InetSocketAddress) channel.getRemoteAddress());
 		}
 
 		List<Peer> peerList = GlobalConfig.getPeerList();
 		for (Peer peer : peerList)
 		{
 			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
 			// Connect only if not already connected
 			if (!remoteSessions.contains(addr))
 			{
 				connect(addr);
 			}
 		}		
 	}
 
 	protected static Set<Channel> getManagedConnectorSessions()
 	{
 		return new LinkedHashSet<Channel>(instance.agentsConnection);
 	}
 
 	protected static List<Peer> getPeerList()
 	{
 		return GlobalConfig.getPeerList();
 	}
 
 	public static void destroy()
 	{
 		instance.idestroy();
 	}
 
 	public static void init()
 	{
 		instance.iinit();
 	}
 
 	public static void publish(InternalMessage message)
 	{
 		instance.ipublish(message);
 	}
 
 	public static void removeAsyncConsumer(MessageListener listener)
 	{
 		if (listener.getSourceDestinationType() == DestinationType.TOPIC)
 		{
 			LocalTopicConsumers.remove(listener);
 		}
 		else if (listener.getSourceDestinationType() == DestinationType.QUEUE)
 		{
 			LocalQueueConsumers.remove(listener);
 		}
 
 	}
 
 
 	private Gcs()
 	{
 		log.info("{} starting.", SERVICE_NAME);
 		try
 		{
 			startAcceptor(GcsInfo.getAgentPort());
 			startConnector();
 
 			GcsExecutor.scheduleWithFixedDelay(new QueueAwaker(), RECOVER_INTERVAL, RECOVER_INTERVAL, TimeUnit.MILLISECONDS);
 			GcsExecutor.scheduleWithFixedDelay(new QueueCounter(), 20, 20, TimeUnit.SECONDS);
 			GcsExecutor.scheduleWithFixedDelay(new GlobalConfigMonitor(), 30, 30, TimeUnit.SECONDS);
 			
			GcsExecutor.scheduleWithFixedDelay(new ExpiredMessagesDeleter(), 10, 10, TimeUnit.MINUTES);
 		}
 		catch (Throwable t)
 		{
 			Throwable rootCause = ErrorAnalyser.findRootCause(t);
 			log.error(rootCause.getMessage(), rootCause);
 			Shutdown.now();
 		}
 		Sleep.time(GcsInfo.getInitialDelay());
 
 	}
 
 	private void connectToAllPeers()
 	{
 		List<Peer> peerList = GlobalConfig.getPeerList();
 		for (Peer peer : peerList)
 		{
 			SocketAddress addr = new InetSocketAddress(peer.getHost(), peer.getPort());
 			connect(addr);
 		}
 	}
 
 	private void iackMessage(String queueName, final String msgId)
 	{
 		try
 		{
 			QueueProcessorList.get(queueName).ack(msgId);
 		}
 		catch (MaximumQueuesAllowedReachedException e)
 		{
 			// This never happens
 		}
 	}
 
 	private void iaddQueueConsumer(String queueName, MessageListener listener)
 	{
 		try
 		{
 			QueueProcessorList.get(queueName);
 		}
 		catch (MaximumQueuesAllowedReachedException e)
 		{
 			// This never happens
 		}
 
 		if (listener != null)
 		{
 			LocalQueueConsumers.add(queueName, listener);
 		}
 	}
 
 	private void iaddTopicConsumer(String topicName, MessageListener listener)
 	{
 		if (listener != null)
 		{
 			LocalTopicConsumers.add(topicName, listener, true);
 		}
 	}
 
 	private boolean ienqueue(InternalMessage message, String queueName)
 	{
 		try
 		{
 			QueueProcessorList.get((queueName != null) ? queueName : message.getDestination()).store(message, true);
 			return true;
 		}
 		catch (MaximumQueuesAllowedReachedException e)
 		{
 			log.error("Tried to create a new queue ('{}'). Not allowed because the limit was reached", queueName);
 		}
 		return false;
 	}
 
 	private void iinit()
 	{
 		String[] virtual_queues = VirtualQueueStorage.getVirtualQueueNames();
 
 		for (String vqueue : virtual_queues)
 		{
 			log.debug("Add VirtualQueue '{}' from storage", vqueue);
 			iaddQueueConsumer(vqueue, null);
 		}
 
 		String[] queues = BDBEnviroment.getQueueNames();
 
 		for (String queueName : queues)
 		{
 			QueueProcessor queueProcessor = null;
 			try
 			{
 				queueProcessor = QueueProcessorList.get(queueName);
 			}
 			catch (MaximumQueuesAllowedReachedException e)
 			{
 				// This never happens
 			}
 			if(queueProcessor != null)
 			{
 				if( (queueProcessor.getCounter() == 0 ) && (!queueName.contains("@")) )
 				{
 					log.info(String.format("Removing queue '%s' because it has no messages and it's not a VirtualQueue.", queueName));
 					QueueProcessorList.remove(queueName);					
 				}
 			}
 		}
 
 		connectToAllPeers();
 
 		Shutdown.isShutingDown();
 
 		log.info("{} initialized.", SERVICE_NAME);
 	}
 
 	private void idestroy()
 	{
 		try
 		{
 			System.out.println("Flush buffers");
 			BDBEnviroment.sync();
 		}
 		catch (Throwable te)
 		{
 			log.error(te.getMessage(), te);
 		}
 	}
 
 	private void ipublish(final InternalMessage message)
 	{
 		message.setType(MessageType.COM_TOPIC);
 		LocalTopicConsumers.notify(message);
 		RemoteTopicConsumers.notify(message);
 	}
 
 	private void startAcceptor(int portNumber) throws IOException
 	{
 		ThreadPoolExecutor tpe_io = CustomExecutors.newThreadPool(IO_THREADS, "gcs-io");
 		ThreadPoolExecutor tpe_workers = CustomExecutors.newThreadPool(IO_THREADS * 5, "broker-worker");
 
 		ChannelFactory factory = new NioServerSocketChannelFactory(tpe_io, tpe_workers);
 		ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
 		bootstrap.setOption("child.tcpNoDelay", true);
 		bootstrap.setOption("child.keepAlive", true);
 		bootstrap.setOption("child.receiveBufferSize", 128 * 1024);
 		bootstrap.setOption("child.sendBufferSize", 128 * 1024);
 		bootstrap.setOption("reuseAddress", true);
 		bootstrap.setOption("backlog", 1024);
 		
 		ChannelPipelineFactory serverPipelineFactory = new ChannelPipelineFactory()
 		{
 			@Override
 			public ChannelPipeline getPipeline() throws Exception
 			{
 				ChannelPipeline pipeline = Channels.pipeline();
 
 				pipeline.addLast("broker-encoder", new GcsEncoder());
 
 				pipeline.addLast("broker-decoder", new GcsDecoder());
 
 				pipeline.addLast("broker-handler", new GcsAcceptorProtocolHandler());
 
 				return pipeline;
 			}
 		};
 
 		bootstrap.setPipelineFactory(serverPipelineFactory);
 		
 		InetSocketAddress inet = new InetSocketAddress("0.0.0.0", portNumber);
 		bootstrap.bind(inet);
 		log.info("SAPO-BROKER Listening on: '{}'.", inet.toString());
 		log.info("{} listening on: '{}'.", SERVICE_NAME, inet.toString());
 	}
 	
     private void startConnector()
 	{
     	ThreadPoolExecutor tpe_io = CustomExecutors.newThreadPool(IO_THREADS, "gcs-io");
 		ThreadPoolExecutor tpe_workers = CustomExecutors.newThreadPool(IO_THREADS * 5, "gcs-worker");
     	
 		ClientBootstrap bootstrap = new ClientBootstrap( new NioClientSocketChannelFactory( tpe_io, tpe_workers) );
 
 		
 		ChannelPipelineFactory connectorPipelineFactory = new ChannelPipelineFactory()
 		{
 			@Override
 			public ChannelPipeline getPipeline() throws Exception
 			{
 				ChannelPipeline pipeline = Channels.pipeline();
 
 				pipeline.addLast("broker-encoder", new GcsEncoder());
 
 				pipeline.addLast("broker-decoder", new GcsDecoder());
 
 				pipeline.addLast("broker-handler", new GcsRemoteProtocolHandler());
 
 				return pipeline;
 			}
 		};
 		
         bootstrap.setPipelineFactory(connectorPipelineFactory);
         
         bootstrap.setOption("child.keepAlive", true);
 		bootstrap.setOption("child.receiveBufferSize", 128 * 1024);
 		bootstrap.setOption("child.sendBufferSize", 128 * 1024);
         bootstrap.setOption("connectTimeoutMillis", 5000);
 
         this.connector = bootstrap;
 	}
 
 
 	public synchronized static void deleteQueue(String queueName)
 	{
 		QueueProcessorList.remove(queueName);
 	}
 
 	public static void remoteSessionClosed(Channel channel)
 	{
 		synchronized (instance.agentsConnection)
 		{
 			instance.agentsConnection.remove(channel);
 		}		
 	}
 
 }
