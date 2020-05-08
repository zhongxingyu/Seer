 package upsilon;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.Thread.UncaughtExceptionHandler;
 import java.util.Properties;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.LogManager; 
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.slf4j.bridge.SLF4JBridgeHandler;
 
 import upsilon.configuration.DirectoryWatcher;
 import upsilon.configuration.FileChangeWatcher;
 import upsilon.configuration.XmlConfigurationLoader;
 import upsilon.dataStructures.CollectionOfStructures;
 import upsilon.dataStructures.StructureNode;
 import upsilon.dataStructures.StructurePeer;
 import upsilon.util.UPath; 
 import upsilon.util.ResourceResolver;
 import upsilon.util.SslUtil;
 import ch.qos.logback.classic.LoggerContext;
 import ch.qos.logback.classic.joran.JoranConfigurator;
 
 public class Main implements UncaughtExceptionHandler {
 	public static final Main instance = new Main();
 	private static File configurationOverridePath;
 	private static String releaseVersion;
 
 	private static final XmlConfigurationLoader xmlLoader = new XmlConfigurationLoader();
 
 	private static transient final Logger LOG = LoggerFactory.getLogger(Main.class);
 
 	public static File getConfigurationOverridePath() {
 		return Main.configurationOverridePath; 
 	}
 
 	public static String getVersion() {
 		if (Main.releaseVersion == null) {
 			Main.releaseVersion = Main.class.getPackage().getImplementationVersion();
 
 			try {
 				final Properties props = new Properties();
 				props.load(Main.class.getResourceAsStream("/releaseVersion.properties"));
 				Main.releaseVersion = props.getProperty("releaseVersion"); 
 			} catch (IOException | NullPointerException e) {
 				Main.LOG.warn("Could not get release version from jar.", e);
 			}
 
 			if ((Main.releaseVersion == null) || Main.releaseVersion.isEmpty()) {
 				Main.releaseVersion = "?";
 			}
 		}
 
 		return Main.releaseVersion;  
 	}
  
 	public static void main(final String[] args) throws Exception {		
 		if (args.length > 0) {
 			Main.configurationOverridePath = new File(args[0]); 
 			Main.xmlLoader.setUrl(new UPath(Main.configurationOverridePath, "config.xml"));
 		} else {
 			Main.xmlLoader.setUrl(new UPath("/etc/upsilon/config.xml"));
 		}  
 
 		Main.instance.startup();
 	}
 
 	private static void setupLogging() {
 		LogManager.getLogManager().getLogger("").setLevel(Level.FINEST);
 		SLF4JBridgeHandler.removeHandlersForRootLogger();
 		SLF4JBridgeHandler.install();
 
 		final File loggingConfiguration = new File(ResourceResolver.getInstance().getConfigDir(), "logging.xml");
 
 		try {
 			if (loggingConfiguration.exists()) {
 				Main.LOG.info("Logging override configuration exists, parsing: " + loggingConfiguration.getAbsolutePath());
 
 				final JoranConfigurator loggerConfigurator = new JoranConfigurator();
 				loggerConfigurator.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
 				loggerConfigurator.doConfigure(loggingConfiguration);
 			}
 		} catch (final Exception e) {
 			Main.LOG.warn("Could not set up logging config.", e);
 		}
 	}
 
 	public final StructureNode node = new StructureNode();
 
 	private final Vector<Daemon> daemons = new Vector<Daemon>();
 
 	public DaemonScheduler queueMaintainer;
 
 	public Vector<Daemon> getDaemons() {
 		return this.daemons;
 	}
 
 	public XmlConfigurationLoader getXmlConfigurationLoader() {
 		return Main.xmlLoader;
 	}
 
 	public String guessNodeType() {
 		return this.guessNodeType(Database.instance, Configuration.instance.peers);
 	}
 
 	public String guessNodeType(final Database db, final CollectionOfStructures<StructurePeer> peers) {
 		if ((db == null) && !peers.isEmpty()) {
 			return "service-node";
 		}
 
 		if ((db != null) && peers.isEmpty()) {
 			return "super-node";
 		}
 
 		if ((db == null) && peers.isEmpty()) {
 			return "useless-testing-node";
 		}
 
 		return "non-standard-node";
 	}
  
 	public void shutdown() {
 		for (final Daemon t : this.daemons) {
 			t.stop();  
 		}
 		
 		if (Database.instance != null) {
 			try {
 				Database.instance.disconnect();
 			} catch (Exception e) {    
 				LOG.error("SQL Error during disconnect: " + e.getMessage());
 			}
 		}
 		
 		DirectoryWatcher.stopAll();
 		RobustProcessExecutor.executingThreadPool.shutdown();
 		RobustProcessExecutor.monitoringThreadPool.shutdown(); 
 		FileChangeWatcher.stopAll();
 
 		Main.LOG.warn("All daemons have been requested to stop. Main application should now shutdown.");
 	}
 
 	private void startDaemon(final Daemon r) {
 		final Thread t = new Thread(r, r.getClass().getSimpleName());
 		this.daemons.add(r);
 
 		t.start();
 		t.setUncaughtExceptionHandler(this);
 		Thread.setDefaultUncaughtExceptionHandler(this);
 	}
 	
 	private void startup() throws Exception { 
 		Main.setupLogging();
 		SslUtil.init();
 
 		Main.LOG.info("Upsilon " + Main.getVersion());
 		Main.LOG.info("----------");
 		Main.LOG.debug("CP: " + System.getProperty("java.class.path"));
 		Main.LOG.trace("OS: " + System.getProperty("os.name"));
 
		try {
			Main.xmlLoader.load();
 			
			if (!Main.xmlLoader.getValidator().isParseClean()) {
				throw new IllegalStateException("Parse is unclean.");
			}
		} catch (IllegalStateException e) {
 			Main.xmlLoader.stopFileWatchers();
 
 			Main.LOG.error("Could not parse the initial configuration file. Upsilon cannot ever have a good configuration if it does not start off with a good configuration. Exiting.");
			return;
		}  
		  
		if (Configuration.instance.daemonRestEnabled) {
			this.startDaemon(new DaemonRest());
 		}
		
		this.startDaemon(new DaemonScheduler());

		Main.LOG.debug("Best guess at node type: " + this.guessNodeType());
 	}
 
 	@Override
 	public void uncaughtException(final Thread t, final Throwable e) {
 		Main.LOG.error("Exception on a critical thread [" + t.getName() + "], will now shutdown.", e);
 		this.shutdown();
 	}
 }
