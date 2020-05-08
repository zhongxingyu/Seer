 package edu.teco.dnd.module;
 
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.oio.OioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.channel.socket.oio.OioDatagramChannel;
 import io.netty.util.internal.logging.InternalLoggerFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.TimeUnit;
 
 import org.apache.logging.log4j.LogManager;
 import org.apache.logging.log4j.Logger;
 
 import edu.teco.dnd.module.config.ConfigReader;
 import edu.teco.dnd.module.config.JsonConfig;
 import edu.teco.dnd.module.messages.generalModule.MissingApplicationHandler;
 import edu.teco.dnd.module.messages.generalModule.ShutdownModuleHandler;
 import edu.teco.dnd.module.messages.generalModule.ShutdownModuleMessage;
 import edu.teco.dnd.module.messages.infoReq.ApplicationListResponse;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.ModuleInfoMessageAdapter;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationListMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationListMsgHandler;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMsgHandler;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessageHandler;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationNak;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationAck;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationNak;
 import edu.teco.dnd.module.messages.killApp.KillAppAck;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppNak;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockAck;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockNak;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassAck;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassNak;
 import edu.teco.dnd.module.messages.values.BlockFoundResponse;
 import edu.teco.dnd.module.messages.values.ValueAck;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.ValueMessageAdapter;
 import edu.teco.dnd.module.messages.values.ValueNak;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.network.TCPConnectionManager;
 import edu.teco.dnd.network.UDPMulticastBeacon;
 import edu.teco.dnd.network.logging.Log4j2LoggerFactory;
 import edu.teco.dnd.network.messages.PeerMessage;
 import edu.teco.dnd.util.Base64Adapter;
 import edu.teco.dnd.util.IndexedThreadFactory;
 import edu.teco.dnd.util.InetSocketAddressAdapter;
 import edu.teco.dnd.util.NetConnection;
 import edu.teco.dnd.util.NetConnectionAdapter;
 
 /**
  * The main class that is started on a Module.
  */
 public class ModuleMain {
 	/**
 	 * The logger for this class.
 	 */
 	private static final Logger LOGGER = LogManager.getLogger(ModuleMain.class);
 
 	/**
 	 * Default path for config file.
 	 */
 	public static final String DEFAULT_CONFIG_PATH = "module.cfg";
 
 	/**
 	 * The default address used for multicast.
 	 */
 	public static final InetSocketAddress DEFAULT_MULTICAST_ADDRESS = new InetSocketAddress("225.0.0.1", 5000);
 
 	public static void main(final String[] args) {
 		final Set<EventLoopGroup> eventLoopGroups = new HashSet<EventLoopGroup>();
 		TCPConnectionManager tcpConnectionManager;
 		final ConfigReader moduleConfig;
 		InternalLoggerFactory.setDefaultFactory(new Log4j2LoggerFactory());
 
 		String configPath = DEFAULT_CONFIG_PATH;
 		if (args.length > 0) {
 			LOGGER.debug("argument 0 is \"{}\"", args[0]);
 			if (args[0].equals("--help") || args[0].equals("-h")) {
 				System.out.println("Parameters: [--help| $pathToConfig]");
 				System.out.println("\t--help: print this message");
 				System.out.println("\t$pathToConfig the path to the used config file.");
 				System.exit(0);
 			} else {
 				configPath = args[0];
 			}
 		}
 
 		moduleConfig = getModuleConfig(configPath);
 		if (moduleConfig == null) {
 			System.exit(1);
 		}
 		tcpConnectionManager = prepareNetwork(moduleConfig, eventLoopGroups);
 
 		try {
 			System.setSecurityManager(new ApplicationSecurityManager());
 		} catch (SecurityException se) {
 			LOGGER.fatal("Can not set SecurityManager.");
 			System.exit(-1);
 		}
 
 		ModuleShutdownHook shutdownHook = new ModuleShutdownHook(eventLoopGroups);
 		synchronized (shutdownHook) {
 			ModuleApplicationManager appMan =
 					new ModuleApplicationManager(moduleConfig, tcpConnectionManager, shutdownHook);
 			registerHandlerAdapter(moduleConfig, tcpConnectionManager, appMan);
 		}
 
 		System.out.println("Module is up and running.");
 
 	}
 
 	private static ConfigReader getModuleConfig(final String configPath) {
 		ConfigReader moduleConfig = null;
 		try {
 			moduleConfig = new JsonConfig(configPath);
 		} catch (IOException e) {
 			LOGGER.fatal("could not open file: \"{}\"", configPath);
 			return null;
 		} catch (Exception e) {
			LOGGER.catching(e);
 			LOGGER.fatal("could not load config: \"{}\"", configPath);
 			return null;
 		}
 		return moduleConfig;
 	}
 
 	private static TCPConnectionManager prepareNetwork(ConfigReader moduleConfig,
 			final Set<EventLoopGroup> eventLoopGroups) {
 		UDPMulticastBeacon udpMulticastBeacon;
 
 		final EventLoopGroup networkGroup = new NioEventLoopGroup(0, new IndexedThreadFactory("network-"));
 		final EventLoopGroup applicationGroup = new NioEventLoopGroup(0, new IndexedThreadFactory("application-"));
 		final EventLoopGroup beaconGroup = new OioEventLoopGroup(0, new IndexedThreadFactory("beacon-"));
 		eventLoopGroups.add(networkGroup);
 		eventLoopGroups.add(applicationGroup);
 		eventLoopGroups.add(beaconGroup);
 
 		final TCPConnectionManager tcpConnectionManager =
 				new TCPConnectionManager(networkGroup, applicationGroup, new ChannelFactory<NioServerSocketChannel>() {
 					@Override
 					public NioServerSocketChannel newChannel() {
 						return new NioServerSocketChannel();
 					}
 				}, new ChannelFactory<NioSocketChannel>() {
 					@Override
 					public NioSocketChannel newChannel() {
 						return new NioSocketChannel();
 					}
 				}, moduleConfig.getUuid());
 		for (final InetSocketAddress address : moduleConfig.getListen()) {
 			tcpConnectionManager.startListening(address);
 		}
 
 		udpMulticastBeacon = new UDPMulticastBeacon(new ChannelFactory<OioDatagramChannel>() {
 			@Override
 			public OioDatagramChannel newChannel() {
 				return new OioDatagramChannel();
 			}
 		}, beaconGroup, applicationGroup, moduleConfig.getUuid(), moduleConfig.getAnnounceInterval(), TimeUnit.SECONDS);
 		udpMulticastBeacon.addListener(tcpConnectionManager);
 		final List<InetSocketAddress> announce = Arrays.asList(moduleConfig.getAnnounce());
 		udpMulticastBeacon.setAnnounceAddresses(announce);
 		for (final NetConnection address : moduleConfig.getMulticast()) {
 			udpMulticastBeacon.addAddress(address.getInterface(), address.getAddress());
 		}
 
 		tcpConnectionManager.addMessageType(PeerMessage.class);
 
 		// final PeerExchanger peerExchanger = new PeerExchanger(connectionManager);
 		// peerExchanger.addModule(moduleConfig.getUuid(), announce);
 
 		return tcpConnectionManager;
 	}
 
 	public static void globalRegisterMessageAdapterType(TCPConnectionManager connectionManager) {
 		connectionManager.registerTypeAdapter(InetSocketAddress.class, new InetSocketAddressAdapter());
 		connectionManager.registerTypeAdapter(NetConnection.class, new NetConnectionAdapter());
 		connectionManager.registerTypeAdapter(byte[].class, new Base64Adapter());
 		connectionManager.registerTypeAdapter(ModuleInfoMessage.class, new ModuleInfoMessageAdapter());
 
 		connectionManager.addMessageType(JoinApplicationMessage.class);
 		connectionManager.addMessageType(JoinApplicationAck.class);
 		connectionManager.addMessageType(JoinApplicationNak.class);
 		connectionManager.addMessageType(ValueMessage.class);
 		connectionManager.addMessageType(WhoHasBlockMessage.class);
 		connectionManager.addMessageType(ValueNak.class);
 		connectionManager.addMessageType(ValueAck.class);
 		connectionManager.addMessageType(BlockFoundResponse.class);
 		connectionManager.addMessageType(LoadClassNak.class);
 		connectionManager.addMessageType(LoadClassMessage.class);
 		connectionManager.addMessageType(LoadClassAck.class);
 		connectionManager.addMessageType(BlockNak.class);
 		connectionManager.addMessageType(BlockMessage.class);
 		connectionManager.addMessageType(BlockAck.class);
 		connectionManager.addMessageType(KillAppNak.class);
 		connectionManager.addMessageType(KillAppAck.class);
 		connectionManager.addMessageType(KillAppMessage.class);
 		connectionManager.addMessageType(StartApplicationMessage.class);
 		connectionManager.addMessageType(StartApplicationAck.class);
 		connectionManager.addMessageType(StartApplicationNak.class);
 		connectionManager.addMessageType(RequestModuleInfoMessage.class);
 		connectionManager.addMessageType(RequestApplicationListMessage.class);
 		connectionManager.addMessageType(ApplicationListResponse.class);
 		connectionManager.addMessageType(ModuleInfoMessage.class);
 	}
 
 	private static void registerHandlerAdapter(ConfigReader moduleConfig, TCPConnectionManager connectionManager,
 			ModuleApplicationManager appMan) {
 		globalRegisterMessageAdapterType(connectionManager);
 		connectionManager.registerTypeAdapter(ValueMessage.class, new ValueMessageAdapter(appMan));
 
 		connectionManager.addHandler(JoinApplicationMessage.class, new JoinApplicationMessageHandler(appMan));
 		connectionManager.addHandler(RequestApplicationListMessage.class, new RequestApplicationListMsgHandler(
 				moduleConfig.getUuid(), appMan));
 		connectionManager.addHandler(RequestModuleInfoMessage.class, new RequestModuleInfoMsgHandler(moduleConfig));
 
 		// Module does not have application but received Message, handlers
 		connectionManager.addHandler(null, LoadClassMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, BlockMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, StartApplicationMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, KillAppMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, ValueMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, WhoHasBlockMessage.class, new MissingApplicationHandler());
 		connectionManager.addHandler(null, ShutdownModuleMessage.class, new ShutdownModuleHandler(appMan));
 	}
 }
