 package polly;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.jar.Attributes;
 import java.util.jar.JarFile;
 import java.util.jar.Manifest;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.jibble.pircbot.NickAlreadyInUseException;
 
 import de.skuzzle.polly.sdk.CompositeDisposable;
 import de.skuzzle.polly.sdk.Version;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
 import de.skuzzle.polly.sdk.exceptions.PluginException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 
 import polly.commandline.AbstractArgumentParser;
 import polly.commandline.ParameterException;
 import polly.commandline.PollyArgumentParser;
 import polly.core.BotConnectionSettings;
 import polly.core.CommandManagerImpl;
 import polly.core.ConversationManagerImpl;
 import polly.core.FormatManagerImpl;
 import polly.core.IrcManagerImpl;
 import polly.core.MyPollyImpl;
 import polly.core.PersistenceManagerImpl;
 import polly.core.PluginConfiguration;
 import polly.core.PluginManagerImpl;
 import polly.core.ShutdownManagerImpl;
 import polly.core.UserManagerImpl;
 import polly.data.Attribute;
 import polly.data.User;
 import polly.eventhandler.IrcLoggingHandler;
 import polly.eventhandler.IsGoneHandler;
 import polly.eventhandler.MessageHandler;
 import polly.eventhandler.TraceNickChangeHandler;
 import polly.events.DefaultEventProvider;
 import polly.events.EventProvider;
 import polly.persistence.DatabaseProperties;
 import polly.persistence.XmlCreator;
 import polly.telnet.TelnetServer;
 import polly.update.UpdateItem;
 import polly.update.UpdateManager;
 import polly.update.UpdateProperties;
 import polly.util.FileUtil;
 
 
 
 public class Polly {
     
     public static Version getPollyVersion() {
         String version = Polly.class.getPackage().getImplementationVersion();
         if (version == null) {
             return new Version(DEVELOP_VERSION);
         } else {
             return new Version(version);
         }
     }
     
     
     
     public static String getPid() {
         String[] parts = ManagementFactory.getRuntimeMXBean().getName().split("@");
         return parts[0];
     }
     
     
     public static File getPollyPath() {
         return new File(".");
     }
     
     private static String commandLine = "";
     public static String getCommandLine() {
         return commandLine;
     }
     
     
    private final static String DEVELOP_VERSION = "0.6.0";
     private final static String PLUGIN_FOLDER = "cfg/plugins/";
     private final static String CONFIG_FULL_PATH = "cfg/polly.cfg";
     private final static String PERSISTENCE_XML = "cfg/META-INF/persistence.xml";
     private final static String PERSISTENCE_UNIT = "polly";
     
     private static Logger logger = Logger.getLogger(Polly.class.getName());
     
     
     
     /**
      * Start polly.
      * 
      * @param args The commandline arguments passed to polly. This array may be empty
      */
     public synchronized static void main(String[] args) {
         for (String arg : args) {
             commandLine += arg + " ";
         }
         commandLine = commandLine.trim();
         new Polly(args);
     }
     
     
 
    
     private Polly(String[] args) {
         if (!FileUtil.waitFor("polly.installer.jar")) {
             System.out.println("Updates still running. Exiting!");
             return;
         }
         
         
         PollyConfiguration config = this.readConfig(CONFIG_FULL_PATH);
         this.parseArguments(args, config);
         
         logger.info("----------------------------------------------");
         logger.info("new polly session started!");
         logger.info("----------------------------------------------");
         logger.info("");
         logger.info("Config file read from '" + CONFIG_FULL_PATH + "'");
         logger.info("Polly command line arguments: " + Arrays.toString(args));
         logger.info("(Canonical: " + getCommandLine() + ")");
         Version version = getPollyVersion();
         logger.info("Version info: " + version);
 
         logger.trace("Configuration: \n" + config.toString());
         
         if (config.getPluginExcludes().length != 0) {
             logger.info("Following plugins are excluded: " + 
                     Arrays.toString(config.getPluginExcludes()));
         }
         if (config.getIgnoredCommands().length != 0) {
             logger.info("Following commands will be ignored: " + 
                     Arrays.toString(config.getIgnoredCommands()));
         }
         
         
         ExecutorService eventThreadPool = Executors.newFixedThreadPool
                 (config.getEventThreads(), new EventThreadFactory());
 
         EventProvider eventProvider = new DefaultEventProvider(eventThreadPool);
         
         FormatManagerImpl formatManager = new FormatManagerImpl(
                 config.getDateFormatString(),
                 config.getNumberFormatString());
         PluginManagerImpl pluginManager = new PluginManagerImpl();
 
         PersistenceManagerImpl persistence = new PersistenceManagerImpl();
         CommandManagerImpl commandManager = new CommandManagerImpl(
                 config.getIgnoredCommands());
         
         UserManagerImpl userManager = new UserManagerImpl(persistence, 
                 config.getDeclarationCachePath(),
                 eventProvider);
         IrcManagerImpl ircManager = new IrcManagerImpl(config.getNickName(),
                 eventProvider, config);
         
         ConversationManagerImpl conversationManager = new ConversationManagerImpl();
         MyPollyImpl myPolly = new MyPollyImpl(
                 commandManager, 
                 ircManager, 
                 pluginManager, 
                 config, 
                 persistence, 
                 userManager,
                 formatManager,
                 conversationManager);
         
         this.updateInstaller(config);
         this.checkUpdates(config, pluginManager, PLUGIN_FOLDER);
 
         this.setupPlugins(pluginManager, myPolly, config, PLUGIN_FOLDER);        
         this.setupDatabase(persistence, config, pluginManager, 
                 PERSISTENCE_XML, PERSISTENCE_UNIT);
         this.setupDefaultUser(userManager, config);
         this.setupIrc(ircManager, config, commandManager, userManager);
         
         /*
          * Configure shutdown list. Obey list order!
          */
         CompositeDisposable shutdownList = ShutdownManagerImpl.get().getShutdownList();
         shutdownList.add(pluginManager);
         shutdownList.add(ircManager);
         shutdownList.add(userManager);
         shutdownList.add(persistence);
         shutdownList.add(config);
         shutdownList.add(eventProvider);
         shutdownList.add(conversationManager);
         
         pluginManager.notifyPlugins();
         
         logger.info("Polly succesfully set up.");
     }
     
     
     
     private PollyConfiguration readConfig(String path) {
         try {
             PollyConfiguration config = new PollyConfiguration(path);
             PropertyConfigurator.configure(config.getLogConfigFile());
             return config;
         } catch (Exception e) {
             // Reply using stdout because logger may not be initialized
             System.out.println("Fehler beim Lesen der Konfigurationsdatei: " + 
                     e.getMessage());
             e.printStackTrace();
             System.exit(0);
         }
         
         // unreachable
         return null;
     }
     
     
     
     private void parseArguments(String[] args, PollyConfiguration config) {
         try {
             AbstractArgumentParser parser = new PollyArgumentParser(config);
             parser.parse(args);
             commandLine = parser.getCanonicalArguments();
         } catch (ParameterException e) {
             logger.fatal(e.getMessage(), e);
             this.printHelp();
             System.exit(0);
         }
     }
     
     
     
     private void printHelp() {
         System.out.println("Polly Parameterbersicht:");
         System.out.println("Name: \t\tParameter:\t Beschreibung:");
         System.out.println("(-log | -l) \t<'on'|'off'> \t Schaltet IRC " +
                 "log ein/aus.");
         System.out.println("(-nick | -n) \t<nickname> \t Nickname fr den Bot.");
         System.out.println("(-ident | -i) \t<ident> \t Passwort fr den Nickserv.");
         System.out.println("(-server | -s) \t<server> \t Server zu dem sich der Bot " +
                 "verbindet. Nutzt 6669 als Port wenn nicht anders angegeben.");
         System.out.println("(-port | -p) \t<port> \t\t Port fr den IRC-Server.");
         System.out.println("(-join | -j) \t<#c1,..#cn> \t Joined nur die/den " +
                 "angegebenen Channel.");
         System.out.println("(-update | -u) \t<'on'|'off'> \t Auto update ein/aus.");
         System.out.println("-telnet \t<'on'|'off'>\t Telnet-Server aktivieren.");
         System.out.println("-telnetport \t<port>\t\t Telnet-Port setzen.");
         System.out.println("(-help | -?) \t\t\t Diese bersicht anzeigen.");
         System.out.println("Beliebige Taste drcken zum Beenden.");
         try {
             System.in.read();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
     
     
     
     private void setupIrc(IrcManagerImpl ircManager, PollyConfiguration config, 
             CommandManagerImpl commandManager, UserManagerImpl userManager) {
         
         logger.info("Starting bot with settings: (" +
                 "Nick: " + config.getNickName() + 
                 ", Ident: *****" + 
                 ", Server: " + config.getServer() + 
                 ", Port: " + config.getPort() + 
                 ", Logging: " + config.getIrcLogging() + ")");
         
         if (config.getIrcLogging()) {
             IrcLoggingHandler ircConsoleLogger = new IrcLoggingHandler();
             ircManager.addMessageListener(ircConsoleLogger);
             ircManager.addNickChangeListener(ircConsoleLogger);
             ircManager.addJoinPartListener(ircConsoleLogger);
         }
         
         MessageHandler handler = new MessageHandler(commandManager, userManager, 
             config.getEncodingName());
         ircManager.addMessageListener(handler);
         ircManager.addNickChangeListener(new TraceNickChangeHandler(userManager));
         
         IsGoneHandler isGoneHandler = new IsGoneHandler(ircManager, userManager);
         ircManager.addUserSpottedListener(isGoneHandler);
         
         BotConnectionSettings settings = new BotConnectionSettings(
                 config.getNickName(), 
                 config.getServer(), 
                 config.getPort(), 
                 config.getIdent(),
                 config.getChannels());
         
         try {
             ircManager.connect(settings);
             logger.info("Connected!");
         } catch (NickAlreadyInUseException e) {
             logger.fatal("Connection rejected: nickname in use.", e);
             System.exit(0);
         } catch (Exception e) {
             logger.fatal("Connection failed: " + e.getMessage(), e);
             System.exit(0);
         }
         this.setupTelnetServer(config, ircManager, handler);
     }
     
     
     
     private void setupTelnetServer(PollyConfiguration config, IrcManagerImpl ircManager, 
             MessageListener handler) {
         if (config.enableTelnet()) {
             try {
                 TelnetServer server = new TelnetServer(config, ircManager, handler);
                 ShutdownManagerImpl.get().getShutdownList().add(server);
                 server.start();
             } catch (Exception e) {
                 logger.error("Error setting up telnet server.", e);
             }
         }
     }
     
     
     
     private void setupPlugins(PluginManagerImpl pluginManager, MyPollyImpl myPolly, 
             PollyConfiguration config, String pluginFolder) {
         
         try {
             pluginManager.loadFolder(pluginFolder, myPolly, config.getPluginExcludes());
         } catch (PluginException e) {
             logger.error("Plugin initialization error.", e);
         }
     }
     
     
     
     private void setupDatabase(PersistenceManagerImpl persistence, 
             PollyConfiguration config, 
             PluginManagerImpl pluginManager,
             String xmlPath,
             String persistenceUnit) {
         
         DatabaseProperties dp = new DatabaseProperties(
                 config.getDbPassword(), 
                 config.getDbUser(), 
                 config.getDbDriver(), 
                 config.getDbUrl());
         
         XmlCreator xc = new XmlCreator(
                 persistence.getEntities(), 
                 dp, 
                 persistenceUnit, 
                 pluginManager);
 
         persistence.registerEntity(User.class);
         persistence.registerEntity(Attribute.class);
         try {
             logger.debug("Writing persistence.xml to " + xmlPath);
             xc.writePersistenceXml(xmlPath);
             
             logger.debug("Connecting to database.");
             persistence.connect(persistenceUnit);
             
         } catch (IOException e) {
             logger.fatal("Could not write persistence.xml to " + xmlPath, e);
             System.exit(0);
         } catch (Exception e) {
             logger.fatal("Error while setting up database connection.", e);
             System.exit(0);
         }
     }
     
     
     
     private void setupDefaultUser(UserManagerImpl userManager, 
             PollyConfiguration config) {
         
         try {
             logger.info("Creating default user with name '" + 
                     config.getAdminUserName() + "'.");
             User admin = new User(
                     config.getAdminUserName(), 
                     "", 
                     config.getAdminUserLevel());
             
             admin.setHashedPassword(config.getAdminPasswordHash());
             userManager.addUser(admin);
         }  catch (UserExistsException e) {
             logger.debug("Default user already existed.");
         } catch (DatabaseException e) {
             logger.fatal("Database error", e);
         }
     }
     
     
     
     private void updateInstaller(PollyConfiguration config) {
         if (!config.getAutoUpdate()) {
             return;
         }
         
         UpdateItem ui = null;
         try {
             String version = "0.0.0";
             if ((new File("polly.installer.jar")).exists()) {
                 JarFile installer = new JarFile("polly.installer.jar");
                 Manifest m = installer.getManifest();
                 Attributes main = m.getMainAttributes();
                 version = main.getValue("Implementation-Version");
             }
             ui = new UpdateItem("polly.installer", new Version(version), 
                     new URL(config.getInstallerUpdateUrl()));
         } catch (IOException e) {
             logger.error("Error while updating installer", e);
         }
         
         UpdateManager um = new UpdateManager();
         logger.info("checking for new installer version...");
         List<UpdateProperties> update = um.collect(Collections.singletonList(ui));
         if (update.isEmpty()) {
             return;
         }
         
         logger.info("Downloading installer update...");
         List<File> files = um.downloadUpdates(update);
         if (files.size() != 1) {
             return;
         }
         logger.info("installing installer update...");
         try {
             File zip = files.get(0);
             File temp = FileUtil.createTempDirectory();
             FileUtil.unzip(zip, temp);
             FileUtil.copyContent(temp, Polly.getPollyPath());
             FileUtil.deleteRecursive(temp);
             logger.info("installer updated successfuly");
         } catch (IOException e) {
             logger.error("Error while updating installer.jar", e);
             return;
         }
     }
     
     
     
     private void checkUpdates(PollyConfiguration config, PluginManagerImpl pluginManager, 
                 String pluginFolder) {
         if (!config.getAutoUpdate()) {
             return;
         } else if (!(new File("polly.installer.jar")).exists()) {
             logger.error("'installer.jar' not found in polly root directory. " +
             		"Skipping updates");
             return;
         }
         List<PluginConfiguration> plugins = pluginManager.enumerate(pluginFolder, 
             config.getPluginExcludes());
         List<UpdateItem> updates = new LinkedList<UpdateItem>();
         
         try {
             updates.add(new UpdateItem("polly", Polly.getPollyVersion(), 
                     new URL(config.getUpdateUrl())));
         } catch (MalformedURLException e) {
             // please never reach
             logger.fatal("Unable to add update item for polly: " + 
                     config.getUpdateUrl(), e);
         }
         
         for (PluginConfiguration pc : plugins) {
             if (!pc.updateSupported()) {
                 continue;
             }
             try {
                 updates.add(UpdateItem.fromProperties(pc.props));
             } catch (Exception e) {
                 logger.error("Failed to create update item for plugin " + 
                     pc.getProperty(PluginConfiguration.PLUGIN_NAME), e);
             }
         }
         UpdateManager um = new UpdateManager();
         
         logger.debug("Collecting updates...");
         List<UpdateProperties> actualUpdates = um.collect(updates);
         if (actualUpdates.isEmpty()) {
             logger.info("No updates available.");
             return;
         }
         logger.debug("Downloading updates...");
         List<File> files = um.downloadUpdates(actualUpdates);
         
         logger.debug("Preparing to install downloaded updates.");
         String arg = !getCommandLine().equals("") 
             ? "-pp \"" + getCommandLine() + "\"" 
             : "";
         
         arg += " -f \"";
         for (File file : files) {
             arg += file.getAbsolutePath() + ";";
         }
         arg += "\"";
         
         try {
             logger.info("Launching installer...");
             String cmd = "java -jar polly.installer.jar " + arg;
             logger.trace("Command: " + cmd);
             Runtime.getRuntime().exec(cmd);
             ShutdownManagerImpl.get().shutdown(true);
         } catch (IOException e) {
             logger.fatal("Failed to start the installer. Deleting all downloads");
             for (File file : files) {
                 file.delete();
             }
         }
     }
 }
