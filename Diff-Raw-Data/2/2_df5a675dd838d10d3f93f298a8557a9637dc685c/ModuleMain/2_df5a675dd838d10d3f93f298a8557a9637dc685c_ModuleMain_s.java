 package edu.teco.dnd.module;
 
 import io.netty.bootstrap.Bootstrap;
 import io.netty.bootstrap.ChannelFactory;
 import io.netty.bootstrap.ServerBootstrap;
 import io.netty.channel.EventLoopGroup;
 import io.netty.channel.nio.NioEventLoopGroup;
 import io.netty.channel.oio.OioEventLoopGroup;
 import io.netty.channel.socket.nio.NioServerSocketChannel;
 import io.netty.channel.socket.nio.NioSocketChannel;
 import io.netty.channel.socket.oio.OioDatagramChannel;
 import io.netty.util.internal.logging.InternalLoggerFactory;
 
 import java.io.IOException;
 import java.net.InetSocketAddress;
 import java.security.NoSuchAlgorithmException;
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
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationInformationMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestApplicationInformationMessageHandler;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMessage;
 import edu.teco.dnd.module.messages.infoReq.RequestModuleInfoMsgHandler;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessage;
 import edu.teco.dnd.module.messages.joinStartApp.JoinApplicationMessageHandler;
 import edu.teco.dnd.module.messages.joinStartApp.StartApplicationMessage;
 import edu.teco.dnd.module.messages.killApp.KillAppMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.BlockMessage;
 import edu.teco.dnd.module.messages.loadStartBlock.LoadClassMessage;
 import edu.teco.dnd.module.messages.values.ValueMessage;
 import edu.teco.dnd.module.messages.values.ValueMessageAdapter;
 import edu.teco.dnd.module.messages.values.WhoHasBlockMessage;
 import edu.teco.dnd.module.permissions.ApplicationSecurityManager;
 import edu.teco.dnd.network.UDPMulticastBeacon;
 import edu.teco.dnd.network.logging.Log4j2LoggerFactory;
 import edu.teco.dnd.network.tcp.ClientBootstrapChannelFactory;
 import edu.teco.dnd.network.tcp.ServerBootstrapChannelFactory;
 import edu.teco.dnd.network.tcp.TCPConnectionManager;
 import edu.teco.dnd.server.TCPProtocol;
 import edu.teco.dnd.util.IndexedThreadFactory;
 import edu.teco.dnd.util.NetConnection;
 
 /**
  * The main class that is started on a ModuleInfo.
  */
 public final class ModuleMain {
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
 
 	/**
 	 * Should never be instantiated.
 	 */
 	private ModuleMain() {
 	}
 
 	/**
 	 * @param args
 	 *            ;-)
 	 */
 	public static void main(final String[] args) {
 		final Set<EventLoopGroup> eventLoopGroups = new HashSet<EventLoopGroup>();
 		TCPConnectionManager tcpConnectionManager;
 		final ConfigReader moduleConfig;
 		InternalLoggerFactory.setDefaultFactory(new Log4j2LoggerFactory());
 
 		String configPath = DEFAULT_CONFIG_PATH;
 		if (args.length > 0) {
 			LOGGER.debug("argument 0 is \"{}\"", args[0]);
 			if ("--help".equals(args[0]) || "-h".equals(args[0])) {
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
 			// FIXME: just calling exit is probably a bad idea
 			System.exit(-1);
 		}
 
 		ModuleShutdownHook shutdownHook = new ModuleShutdownHook(eventLoopGroups);
 		synchronized (shutdownHook) {
 			Module module = null;
 			try {
 				module = new Module(moduleConfig, tcpConnectionManager, shutdownHook);
 			} catch (final NoSuchAlgorithmException e) {
 				System.err.println("Missing algorithm: " + e);
 				System.exit(1);
 			}
 			registerHandlerAdapter(moduleConfig, tcpConnectionManager, module);
 		}
 
		System.out.println("ModuleInfo is up and running.");
 
 	}
 
 	/**
 	 * read the configuration file into a ConfigReader.
 	 * 
 	 * @param configPath
 	 *            path to configuration file
 	 * @return a configReader with the read configuration.
 	 */
 	private static ConfigReader getModuleConfig(final String configPath) {
 		ConfigReader moduleConfig = null;
 		try {
 			moduleConfig = new JsonConfig(configPath);
 		} catch (IOException e) {
 			LOGGER.warn("could not open file: \"{}\"", configPath);
 			return null;
 		} catch (Exception e) {
 			LOGGER.warn("could not load config: \"{}\"", configPath);
 			e.printStackTrace();
 			return null;
 		}
 		return moduleConfig;
 	}
 
 	/**
 	 * prepares the modules network. Notably sets up a TCPConnectionManager according to the given configuration.
 	 * 
 	 * @param moduleConfig
 	 *            the configuration used for setup.
 	 * @param eventLoopGroups
 	 *            set that will be filled with started threadGroups. Can be used to shut them down later.
 	 * @return a newly setup TCPConnectionManager.
 	 */
 	private static TCPConnectionManager prepareNetwork(ConfigReader moduleConfig,
 			final Set<EventLoopGroup> eventLoopGroups) {
 		UDPMulticastBeacon udpMulticastBeacon;
 
 		final EventLoopGroup networkGroup = new NioEventLoopGroup(0, new IndexedThreadFactory("network-"));
 		final EventLoopGroup applicationGroup = new NioEventLoopGroup(0, new IndexedThreadFactory("application-"));
 		final EventLoopGroup beaconGroup = new OioEventLoopGroup(0, new IndexedThreadFactory("beacon-"));
 		eventLoopGroups.add(networkGroup);
 		eventLoopGroups.add(applicationGroup);
 		eventLoopGroups.add(beaconGroup);
 
 		final ServerBootstrap serverBootstrap = new ServerBootstrap();
 		serverBootstrap.group(networkGroup, applicationGroup);
 		serverBootstrap.channel(NioServerSocketChannel.class);
 		final Bootstrap clientBootstrap = new Bootstrap();
 		clientBootstrap.group(networkGroup);
 		clientBootstrap.channel(NioSocketChannel.class);
 		final TCPConnectionManager tcpConnMan =
 				new TCPConnectionManager(new ServerBootstrapChannelFactory(serverBootstrap),
 						new ClientBootstrapChannelFactory(clientBootstrap), applicationGroup, moduleConfig.getUuid());
 		for (final InetSocketAddress address : moduleConfig.getListen()) {
 			tcpConnMan.startListening(address);
 		}
 
 		udpMulticastBeacon = new UDPMulticastBeacon(new ChannelFactory<OioDatagramChannel>() {
 			@Override
 			public OioDatagramChannel newChannel() {
 				return new OioDatagramChannel();
 			}
 		}, beaconGroup, applicationGroup, moduleConfig.getUuid(), moduleConfig.getAnnounceInterval(), TimeUnit.SECONDS);
 		udpMulticastBeacon.addListener(tcpConnMan);
 		final List<InetSocketAddress> announce = Arrays.asList(moduleConfig.getAnnounce());
 		udpMulticastBeacon.setAnnounceAddresses(announce);
 		for (final NetConnection address : moduleConfig.getMulticast()) {
 			udpMulticastBeacon.addAddress(address.getInterface(), address.getAddress());
 		}
 
 		return tcpConnMan;
 	}
 
 	/**
 	 * Registers Message Handlers and adapters for the module on the TCPConnectionManager. This is ModuleInfo specific
 	 * and not used by deploy.
 	 * 
 	 * @param moduleConfig
 	 *            the configuration according to which the module is set up.
 	 * @param tcpConnMan
 	 *            TCPConnectionManager to register handlers on.
 	 * @param module
 	 *            the Module the various handlers should use later.
 	 */
 	private static void registerHandlerAdapter(ConfigReader moduleConfig, TCPConnectionManager tcpConnMan, Module module) {
 		new TCPProtocol().initialize(tcpConnMan);
 		tcpConnMan.registerTypeAdapter(ValueMessage.class, new ValueMessageAdapter(module));
 
 		tcpConnMan.addHandler(JoinApplicationMessage.class, new JoinApplicationMessageHandler(module));
 		tcpConnMan.addHandler(RequestApplicationInformationMessage.class,
 				new RequestApplicationInformationMessageHandler(moduleConfig.getUuid(), module));
 		tcpConnMan.addHandler(RequestModuleInfoMessage.class, new RequestModuleInfoMsgHandler(moduleConfig));
 
 		tcpConnMan.addHandler(LoadClassMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(BlockMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(StartApplicationMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(KillAppMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(ValueMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(WhoHasBlockMessage.class, new MissingApplicationHandler());
 		tcpConnMan.addHandler(ShutdownModuleMessage.class, new ShutdownModuleHandler(module));
 	}
 }
