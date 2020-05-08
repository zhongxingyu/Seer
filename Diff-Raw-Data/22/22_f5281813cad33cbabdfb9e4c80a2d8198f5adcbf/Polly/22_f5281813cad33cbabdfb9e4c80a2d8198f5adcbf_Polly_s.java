 package polly;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.util.Arrays;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 import org.jibble.pircbot.NickAlreadyInUseException;
 
 import de.skuzzle.polly.sdk.CompositeDisposable;
 import de.skuzzle.polly.sdk.eventlistener.MessageListener;
 import de.skuzzle.polly.sdk.exceptions.DatabaseException;
import de.skuzzle.polly.sdk.exceptions.DuplicatedSignatureException;
 import de.skuzzle.polly.sdk.exceptions.PluginException;
 import de.skuzzle.polly.sdk.exceptions.UserExistsException;
 
 import polly.commandline.AbstractArgumentParser;
 import polly.commandline.ParameterException;
 import polly.commandline.PollyArgumentParser;
 import polly.core.BotConnectionSettings;
 import polly.core.CommandManagerImpl;
 import polly.core.FormatManagerImpl;
 import polly.core.IrcManagerImpl;
 import polly.core.MyPollyImpl;
 import polly.core.PersistenceManagerImpl;
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
import polly.util.TestCommand;
 
 
 
 public class Polly {
     
     public static String getPid() {
         String[] parts = ManagementFactory.getRuntimeMXBean().getName().split("@");
         return parts[0];
     }
     
     
     public static File getPollyPath() {
         return new File(".");
     }
     
     private static String commandLine = "";
     public static String getCommandLine() {
         return commandLine.trim();
     }
     
     
 
     // private final static String CONFIG_FOLDER = "cfg/";
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
         new Polly(args);
     }
     
     
 
    
     private Polly(String[] args) {
         File f = new File(".");
         System.out.println(f.getAbsolutePath());
         
         
         PollyConfiguration config = this.readConfig(CONFIG_FULL_PATH);
         this.parseArguments(args, config);
         
         logger.info("----------------------------------------------");
         logger.info("new polly session started!");
         logger.info("----------------------------------------------");
         logger.info("");
         logger.info("Config file read from '" + CONFIG_FULL_PATH + "'");
         logger.info("Polly command line arguments: " + Arrays.toString(args));
         logger.info("Version info: " + 
                 Polly.class.getPackage().getImplementationVersion());
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
         
         MyPollyImpl myPolly = new MyPollyImpl(
                 commandManager, 
                 ircManager, 
                 pluginManager, 
                 config, 
                 persistence, 
                 userManager,
                 formatManager);
         
        /*
         * Remove this!
         */
        try {
            commandManager.registerCommand(new TestCommand(myPolly));
        } catch (DuplicatedSignatureException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
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
         ircManager.addJoinPartListener(isGoneHandler);
         ircManager.addQuitListener(isGoneHandler);
         
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
 }
