 package multiplexer.jmx.server;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.net.SocketAddress;
 import java.util.ArrayList;
 import java.util.Formatter;
 import java.util.List;
 import java.util.Map;
 import java.util.concurrent.Executors;
 import java.util.concurrent.TimeUnit;
 
 import multiplexer.jmx.client.SendingMethod;
 import multiplexer.jmx.exceptions.NoPeerForPeerIdException;
 import multiplexer.jmx.exceptions.NoPeerForTypeException;
 import multiplexer.jmx.internal.ByteCountingHandler;
 import multiplexer.jmx.internal.Connection;
 import multiplexer.jmx.internal.ConnectionsManager;
 import multiplexer.jmx.internal.MessageCountingHandler;
 import multiplexer.jmx.internal.MessageReceivedListener;
 import multiplexer.jmx.util.LongDeltaCounter;
 import multiplexer.protocol.Constants.MessageTypes;
 import multiplexer.protocol.Constants.PeerTypes;
 import multiplexer.protocol.Protocol.BackendForPacketSearch;
 import multiplexer.protocol.Protocol.MultiplexerMessage;
 import multiplexer.protocol.Protocol.MultiplexerMessageDescription;
 import multiplexer.protocol.Protocol.MultiplexerPeerDescription;
 import multiplexer.protocol.Protocol.MultiplexerRules;
 import multiplexer.protocol.Protocol.MultiplexerMessageDescription.RoutingRule;
 
 import org.jboss.netty.bootstrap.ServerBootstrap;
 import org.jboss.netty.channel.Channel;
 import org.jboss.netty.channel.ChannelFactory;
 import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
 import org.kohsuke.args4j.CmdLineException;
 import org.kohsuke.args4j.CmdLineParser;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.common.collect.Maps;
 import com.google.protobuf.InvalidProtocolBufferException;
 import com.google.protobuf.TextFormat;
 import com.google.protobuf.TextFormat.ParseException;
 
 /**
  * @author Piotr Findeisen
  */
 // TODO javadoc
 public class JmxServer implements MessageReceivedListener, Runnable {
 
 	public static final String UNKOWN_TYPE_NAME = "unknown";
 	public static final String UNNAMED_TYPE_NAME = "unnamed";
 
 	private static final Logger logger = LoggerFactory
 		.getLogger(JmxServer.class);
 
 	protected ConnectionsManager connectionsManager;
 	protected SocketAddress serverAddress;
 	private SocketAddress serverEffectiveAddress;
 
 	protected Map<String, Integer> peerTypeNamesToPeerTypeIds = Maps
 		.newHashMap();
 	protected Map<Integer, MultiplexerMessageDescription> messageTypeIdsToDescription = Maps
 		.newHashMap();
 
 	protected long transferUpdateIntervalMillis = 1000;
 
 	private volatile boolean running = true;
 
 	private ServerChannelPipelineFactory channelPipelineFactory;
 
 	private int localPort = -1;
 
 	public JmxServer(SocketAddress serverAddress) {
 		this.serverAddress = serverAddress;
 	}
 
 	public int getLocalPort() {
 		return localPort;
 	}
 
 	public void run() {
 
 		logger.debug("starting {} @ {}", JmxServer.class.getSimpleName(),
 			serverAddress);
 
 		try {
 			// Configure the server.
 			ChannelFactory factory = new NioServerSocketChannelFactory(
 				Executors.newCachedThreadPool(), Executors
 					.newCachedThreadPool());
 			ServerBootstrap bootstrap = new ServerBootstrap(factory);
 
 			// initialize the connectionsManager
 			connectionsManager = new ConnectionsManager(PeerTypes.MULTIPLEXER,
 				bootstrap);
 			channelPipelineFactory = new ServerChannelPipelineFactory(bootstrap
 				.getPipelineFactory());
 			bootstrap.setPipelineFactory(channelPipelineFactory);
 			connectionsManager.setMessageReceivedListener(this);
 
 			// Bind & start the server.
 			Channel listeningChannel = bootstrap.bind(serverAddress);
 			if (listeningChannel.getLocalAddress() instanceof InetSocketAddress) {
 				localPort = ((InetSocketAddress) listeningChannel
 					.getLocalAddress()).getPort();
 			}
 
 			serverEffectiveAddress = serverAddress;
 			if (serverAddress instanceof InetSocketAddress
 				&& ((InetSocketAddress) serverAddress).getPort() == 0) {
 				assert localPort > 0;
 				serverEffectiveAddress = new InetSocketAddress(
 					((InetSocketAddress) serverAddress).getHostName(),
 					localPort);
 			}
 			logger.info("started {} @ {}", JmxServer.class.getSimpleName(),
 				serverEffectiveAddress);

 			synchronized (this) {
 				this.notifyAll();
 			}
 
 			loopPrintingStatistics();
 
 		} finally {
 			try {
 				connectionsManager.shutdown();
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	private void loopPrintingStatistics() {
 		LongDeltaCounter bytesIn = new LongDeltaCounter();
 		LongDeltaCounter bytesOut = new LongDeltaCounter();
 		LongDeltaCounter messagesIn = new LongDeltaCounter();
 		LongDeltaCounter messagesOut = new LongDeltaCounter();
 		LongDeltaCounter time = new LongDeltaCounter(System.currentTimeMillis());
 
 		final ByteCountingHandler bytesCounter = channelPipelineFactory
 			.getByteCountingHandler();
 		final MessageCountingHandler messageCounter = channelPipelineFactory
 			.getMessageCountingHandler();
 
 		while (running) {
 			try {
 				Thread.sleep(transferUpdateIntervalMillis);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 				break;
 			}
 
 			// timeDelta is double so that division results are not rounded.
 			double timeDelta = time.deltaTo(System.currentTimeMillis()) * 1.0
 				/ TimeUnit.SECONDS.toMillis(1);
 			if (timeDelta <= 0) {
 				// system clock changed?
 				continue;
 			}
 
 			System.err.format("IN: %s %9.2f msg/s      OUT: %s %9.2f msg/s%n",
 				renderBytesPerSecondCount(bytesIn.deltaTo(bytesCounter
 					.getBytesInCount())
 					/ timeDelta), messagesIn.deltaTo(messageCounter
 					.getMessagesInCount())
 					/ timeDelta, renderBytesPerSecondCount(bytesOut
 					.deltaTo(bytesCounter.getBytesOutCount())
 					/ timeDelta), messagesOut.deltaTo(messageCounter
 					.getMessagesOutCount())
 					/ timeDelta);
 		}
 	}
 
 	private static String renderBytesPerSecondCount(double bytes) {
 		assert bytes >= 0;
 		Formatter formatter = new Formatter();
 		if (bytes >= 1024 * 1024) {
 			formatter.format("%7.2f MiB/s", bytes / 1024 / 1024);
 		} else if (bytes >= 1024) {
 			formatter.format("%7.2f KiB/s", bytes / 1024);
 		} else {
 			formatter.format("%7.2f B/s  ", bytes);
 		}
 		return formatter.toString();
 	}
 
 	public void shutdown() {
 		running = false;
 		logger.info("stopping {} @ {}", JmxServer.class.getSimpleName(),
 			serverEffectiveAddress);
 	}
 
 	protected MultiplexerMessageDescription registerMessageDescription(
 		MultiplexerMessageDescription description) {
 		assert description.hasType();
 		return messageTypeIdsToDescription.put(description.getType(),
 			description);
 	}
 
 	public void loadMessageDefinitions(MultiplexerRules additionalRules) {
 		for (MultiplexerPeerDescription peerDesc : additionalRules
 			.getPeerList()) {
 			if (!peerDesc.hasName() || !peerDesc.hasType()) {
 				logger.error(
 					"MultiplexerPeerDescription without name or type:\n{}",
 					peerDesc);
 				continue;
 			}
 			if (peerTypeNamesToPeerTypeIds.containsKey(peerDesc.getName())) {
 				logger.error("Peer name '{}' already exists.", peerDesc
 					.getName());
 				continue;
 			}
 			peerTypeNamesToPeerTypeIds.put(peerDesc.getName(), peerDesc
 				.getType());
 		}
 		for (MultiplexerMessageDescription msgd : additionalRules.getTypeList()) {
 
 			if (!msgd.hasType()) {
 				logger.error("MultiplexerMessageDescription without type:\n{}",
 					msgd);
 				continue;
 			}
 
 			MultiplexerMessageDescription.Builder msgdCopy = MultiplexerMessageDescription
 				.newBuilder();
 			if (msgd.hasName()) {
 				msgdCopy.setName(msgd.getName());
 			}
 			msgdCopy.setType(msgd.getType());
 
 			// Convert the 'to' list using peer' name→ID lookup.
 			for (MultiplexerMessageDescription.RoutingRule rRule : msgd
 				.getToList()) {
 
 				if (!rRule.hasPeer()) {
 					logger.error("RoutingRule without peer name:\n{}", rRule);
 					continue;
 				}
 				if (!peerTypeNamesToPeerTypeIds.containsKey(rRule.getPeer())) {
 					logger.error("Unknown peer name: '{}'", rRule.getPeer());
 					continue;
 				}
 				int peerId = peerTypeNamesToPeerTypeIds.get(rRule.getPeer());
 				if (rRule.hasPeerType() && rRule.getPeerType() != peerId) {
 					logger
 						.error(
 							"RoutingRule has both peer name and ID but ID is wrong:\n{}",
 							rRule);
 					continue;
 				}
 
 				// Create a copy of rRule with 'peerType' set (and 'peer'
 				// cleared ─ it's no longer used).
 				msgdCopy.addTo(MultiplexerMessageDescription.RoutingRule
 					.newBuilder(rRule).clearPeer().setPeerType(peerId));
 
 			}
 			registerMessageDescription(msgdCopy.build());
 		}
 	}
 
 	public void loadMessageDefinitions(File file) throws ParseException,
 		FileNotFoundException, IOException {
 		MultiplexerRules.Builder rulesBuilder = MultiplexerRules.newBuilder();
 		TextFormat.merge(new FileReader(file), rulesBuilder);
 		MultiplexerRules additionalRules = rulesBuilder.build();
 		loadMessageDefinitions(additionalRules);
 	}
 
 	public void loadMessageDefinitionsFromFile(String fileName)
 		throws ParseException, FileNotFoundException, IOException {
 		loadMessageDefinitions(new File(fileName));
 	}
 
 	public void onMessageReceived(MultiplexerMessage message,
 		Connection connection) {
 
 		// TODO Auto-generated method stub
 
 		logger.debug("message received\n{}\n", message);
 
 		// routing based on to
 		if (message.hasTo()) {
 			schedule(message);
 			return;
 		}
 
 		// routing based on overridden rules
 		if (message.getOverrideRrulesCount() != 0) {
 			assert message.getOverrideRrulesCount() > 0;
 			schedule(message, message.getOverrideRrulesList());
 			return;
 		}
 
 		// routing based on type
 		if (!message.hasType()) {
 			logger.warn("message without type received\n{}\n", message);
 			return;
 		}
 		switch (message.getType()) {
 		case MessageTypes.PING:
 			if (!message.hasFrom()) {
 				logger.warn("received PING without from and to set:\n{}\n",
 					message);
 			} else {
 				MultiplexerMessage response = connectionsManager
 					.createMessageBuilder().setMessage(message.getMessage())
 					.setType(MessageTypes.PING).setTo(message.getFrom())
 					.build();
 				schedule(response);
 			}
 			break;
 
 		default:
 			if (message.getType() > MessageTypes.MAX_MULTIPLEXER_META_PACKET) {
 				MultiplexerMessageDescription msgDesc = messageTypeIdsToDescription
 					.get(message.getType());
 				if (msgDesc == null) {
 					// fall through
 				} else {
 					schedule(message, msgDesc.getToList());
 					break;
 				}
 			} else {
 				// fall through
 			}
 		case MessageTypes.BACKEND_ERROR:
 		case MessageTypes.DELIVERY_ERROR:
 		case MessageTypes.CONNECTION_WELCOME:
 		case MessageTypes.HEARTBIT:
 		case MessageTypes.REQUEST_RECEIVED:
 			logger.warn("don't know what to do with message type {} ({})",
 				message.getType(), getMessageTypeName(message.getType()));
 			break;
 
 		case MessageTypes.BACKEND_FOR_PACKET_SEARCH:
 			// TODO THIS IS VERY IMPORTANT !!!
 			try {
 				BackendForPacketSearch backendSearchMessage = BackendForPacketSearch
 					.parseFrom(message.getMessage());
 				MultiplexerMessageDescription msgDesc = messageTypeIdsToDescription
 					.get(backendSearchMessage.getPacketType());
 				if (msgDesc == null || msgDesc.getToCount() == 0) {
 					// TODO error
 				} else {
 					RoutingRule routingRule = msgDesc.getTo(0);
 					routingRule = RoutingRule.newBuilder(routingRule).setWhom(
 						RoutingRule.Whom.ALL).setReportDeliveryError(true)
 						.setIncludeOriginalPacketInReport(false).build();
 					List<RoutingRule> ruleSingleton = new ArrayList<RoutingRule>(1);
 					ruleSingleton.add(routingRule);
 					schedule(message, ruleSingleton);
 				}
 			} catch (InvalidProtocolBufferException e) {
 				// TODO return error
 				e.printStackTrace();
 			}
 			break;
 		}
 	}
 
 	private void schedule(MultiplexerMessage message,
 		List<RoutingRule> routingRules) {
 
 		for (RoutingRule rule : routingRules) {
 			try {
 				connectionsManager.sendMessage(message, SendingMethod.via(rule
 					.getPeerType(), rule.getWhom()));
 			} catch (NoPeerForTypeException e) {
 				// TODO should we send delivery error?
 				e.printStackTrace();
 			}
 		}
 	}
 
 	/**
 	 * Send {@code message} to a client directly connected to this server and
 	 * having ID {@code message.getType()}.
 	 * 
 	 * @param message
 	 *            to be sent
 	 */
 	void schedule(MultiplexerMessage message) {
 		assert message.hasTo();
 		try {
 			connectionsManager.sendMessage(message, SendingMethod.via(message
 				.getTo()));
 		} catch (NoPeerForPeerIdException e) {
 			logger.warn("message #{} to {} while it's not connected", message
 				.getId(), message.getTo());
 		}
 	}
 
 	String getMessageTypeName(int type) {
 		String name;
 		name = MessageTypes.getConstantsNames().get(type);
 		if (name != null)
 			return name;
 		MultiplexerMessageDescription msgDesc = messageTypeIdsToDescription
 			.get(type);
 		if (msgDesc != null) {
 			if (msgDesc.hasName())
 				return msgDesc.getName();
 			else
 				return UNNAMED_TYPE_NAME;
 		}
 		return UNKOWN_TYPE_NAME;
 	}
 
 	public long getTransferUpdateIntervalMillis() {
 		return transferUpdateIntervalMillis;
 	}
 
 	public void setTransferUpdateIntervalMillis(
 		long transferUpdateIntervalMillis) {
 		this.transferUpdateIntervalMillis = transferUpdateIntervalMillis;
 	}
 
 	public SocketAddress getServerAddress() {
 		return serverAddress;
 	}
 
 	public void setServerAddress(SocketAddress serverAddress) {
 		this.serverAddress = serverAddress;
 	}
 
 	/**
 	 * @param args
 	 * @throws IOException
 	 * @throws FileNotFoundException
 	 * @throws ParseException
 	 */
 	public static void main(String[] args) throws ParseException,
 		FileNotFoundException, IOException {
 
 		Options options = new Options();
 		CmdLineParser optionsParser = new CmdLineParser(options);
 		try {
 			optionsParser.parseArgument(args);
 		} catch (CmdLineException e) {
 			usage(e.getMessage(), optionsParser);
 			System.exit(1);
 		}
 
 		// initialize the server
 		JmxServer server = new JmxServer(new InetSocketAddress(
 			options.localHost, options.localPort));
 		for (String fileName : options.rulesFiles) {
 			server.loadMessageDefinitionsFromFile(fileName);
 		}
 		server
 			.setTransferUpdateIntervalMillis(options.transferUpdateIntervalMillis);
 
 		server.run();
 	}
 
 	private static void usage(String error, CmdLineParser optionsParser) {
 		System.err.println(error);
 		System.err.println("java " + JmxServer.class.getName()
 			+ " [options...] <multiplexer rules file>");
 		System.err
 			.println("java -jar ....jar server [options...] <multiplexer rules file>");
 		System.err.println();
 		System.err.println("Available options are listed below.");
 		optionsParser.printUsage(System.err);
 	}
 }
