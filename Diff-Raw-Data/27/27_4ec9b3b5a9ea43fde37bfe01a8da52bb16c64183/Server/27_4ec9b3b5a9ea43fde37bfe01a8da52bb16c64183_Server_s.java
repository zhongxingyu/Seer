 /*******************************************************************************
  * Copyright (c) 2012 MCForge.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Public License v3.0
  * which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/gpl.html
  ******************************************************************************/
 package net.mcforge.server;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 import java.net.URL;
 import java.security.NoSuchAlgorithmException;
 import java.security.SecureRandom;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Random;
 
 import net.mcforge.API.EventSystem;
 import net.mcforge.API.action.CmdAbort;
 import net.mcforge.API.io.ServerLogEvent;
 import net.mcforge.API.plugin.CommandHandler;
 import net.mcforge.API.plugin.GeneratorHandler;
 import net.mcforge.API.plugin.PluginHandler;
 import net.mcforge.API.server.ServerStartedEvent;
 import net.mcforge.chat.ChatColor;
 import net.mcforge.chat.Messages;
 import net.mcforge.groups.Group;
 import net.mcforge.iomodel.Player;
 import net.mcforge.networking.IOClient;
 import net.mcforge.networking.packets.PacketManager;
 import net.mcforge.sql.ISQL;
 import net.mcforge.sql.MySQL;
 import net.mcforge.sql.SQLite;
 import net.mcforge.system.Console;
 import net.mcforge.system.PrivilegesHandler;
 import net.mcforge.system.Serializer;
 import net.mcforge.system.heartbeat.Beat;
 import net.mcforge.system.heartbeat.ForgeBeat;
 import net.mcforge.system.heartbeat.Heart;
 import net.mcforge.system.heartbeat.MBeat;
 import net.mcforge.system.heartbeat.WBeat;
 import net.mcforge.system.ticker.Tick;
 import net.mcforge.system.ticker.Ticker;
 import net.mcforge.system.updater.Updatable;
 import net.mcforge.system.updater.UpdateService;
 import net.mcforge.system.updater.UpdateType;
 import net.mcforge.util.FileUtils;
 import net.mcforge.util.logger.LogInterface;
 import net.mcforge.util.logger.Logger;
 import net.mcforge.util.properties.Properties;
 import net.mcforge.world.Level;
 import net.mcforge.world.LevelHandler;
import net.mcforge.world.blocks.tracking.BlockTracker;
 import net.mcforge.world.generator.model.FlatGrass;
 import net.mcforge.world.generator.model.Forest;
 import net.mcforge.world.generator.model.Island;
 import net.mcforge.world.generator.model.Mountains;
 import net.mcforge.world.generator.model.Ocean;
 import net.mcforge.world.generator.model.Pixel;
 import net.mcforge.world.generator.model.Rainbow;
 import net.mcforge.world.generator.model.Space;
 
 public final class Server implements LogInterface, Updatable, Tick {
     private PacketManager pm;
     private final java.util.logging.Logger log = java.util.logging.Logger.getLogger("MCForge");
     private LevelHandler lm;
     private Logger logger;
     private Ticker ticker;
     private CommandHandler ch;
     private PrivilegesHandler prh;
     private GeneratorHandler gh;
     private UpdateService us;
     private Properties p;
     private PluginHandler ph;
     private Beat heartbeater;
     private EventSystem es;
     private String Salt;
     private ISQL sql;
     private Console console;
     private Messages m;
     private int oldsize;
     private ArrayList<IOClient> cache;
    private BlockTracker bt;
     private ArrayList<Player> pcache;
     public static final String[] devs = new String []{ "Dmitchell", "501st_commander", "Lavoaster", "Alem_Zupa", "QuantumParticle", "BeMacized", "Shade2010", "edh649", "hypereddie10", "Gamemakergm", "Serado", "Wouto1997", "cazzar", "givo" };
     /**
      * The remote IP of this server. If there was an error finding the remote IP
      * when the server started up, then this value may equal loopback IP (127.0.0.1)
      */
     public static String IP;
     /**
      * The name for currency on this server.
      */
     public String CurrencyName;
     /**
      * The URL hash for this server
      */
     public String hash;
     /**
      * Whether or not to verify names when players connect
      */
     public boolean VerifyNames;
 
     /**
      * The players currently on the server.
      * @deprecated Use {@link Server#getPlayers()}
      */
     @Deprecated
     public ArrayList<Player> players = new ArrayList<Player>();
     /**
      * Whether the server is running or not
      */
     public boolean Running;
 
     /**
      * Whether sand will use the new physics or old.
      */
     public boolean newSand;
     /**
      * The port of the server
      */
     public int Port;
     /**
      * How many players are allowed on the server
      */
     public int MaxPlayers;
     /**
      * The name of the server
      */
     public String Name;
     /**
      * The name of the server that will appear on the WoM list
      */
     public String altName;
     /**
      * The description of the server
      */
     public String description;
     /**
      * WoM Flags
      */
     public String flags;
     /**
      * The MoTD of the server (The message the player sees when first joining the server)
      */
     public String MOTD;
     /**
      * The main level (The level the user first joins when the player joins the server)
      */
     public String MainLevel;
     /**
      * Whether or not the server is public
      */    
     public boolean Public;
 
     /**
      * Server's default color.<br>
      * Default color is shown before every message the players receive if it doesn't already
      * have a color code in front 
      */
     public ChatColor defaultColor;
     /**
      * Whether levels will be loaded when /goto is used if they're not already loaded 
      */
     public boolean loadOnGoto = true;
     /**
      * The default filename for the system properties
      */
     public final String configpath = "system.config";
     /**
      * The version of MCForge this server runs
      */
     public final String VERSION = "6.0.0b6";
     /**
      * The version number of this MCForge server </br>
      * Where 600 would be 6.0.0 </br>
      * And where 600.6 would be 6.0.0b6
      */
     public final double VERSION_NUMBER = 600.6;
     
     static {
         try {
             URL url = new URL("http://server.mcforge.net/ip.php");
             BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
             IP = reader.readLine();
         } catch (Exception e) {
             IP = "127.0.0.1";
         }
     }
     
     /**
      * The handler that handles level loading,
      * level unloading and finding loaded
      * levels
      * @return
      *        The {@link LevelHandler}
      */
     public final LevelHandler getLevelHandler() {
         return lm;
     }
     /**
      * Gets the server's generator handler.
      * 
      * @return The {@link GeneratorHandler}
      */
     public final GeneratorHandler getGeneratorHandler() {
         return gh;
     }
     /**
      * The handler that handles events.
      * Use the EventSystem to register event or
      * call events
      * @return
      *        The {@link EventSystem}
      */
     public final EventSystem getEventSystem() {
         return es;
     }
     /**
      * Get the handler that handles the packets
      * @return
      *        The {@link PacketManager}
      */
     public final PacketManager getPacketManager() {
         return pm;
     }
     /**
      * Get the handler that handles the player
      * commands. Use this to add/remove commands
      * or excute commands
      * @return
      *        The {@link CommandHandler}
      */
     public final CommandHandler getCommandHandler() {
         return ch;
     }
 
     /**
      * Get the default class loader that is used to load
      * plugins and commands, write serialized objects, etc..
      * @return
      *        The default classloader used across the entire server.
      */
     public final ClassLoader getDefaultClassLoader() {
         return getPluginHandler().getClassLoader();
     }
     /**
      * Get the handler that handles the plugins
      * @return
      *        The {@link PluginHandler}
      */
     public final PluginHandler getPluginHandler() {
         return ph;
     }
 
     /**
      * Get the handler that handles MCForge staff privileges
      */
     public final PrivilegesHandler getPrivilegesHandler() {
         return prh;
     }
 
     /**
      * Get the console object that is controlling the server
      * @return
      *        The {@link Console} object
      */
     public final Console getConsole() {
         return console;
     }
     /**
      * The SQL object where you can execute
      * Queries
      * @return
      *        The {@link ISQL} object
      */
     public final ISQL getSQL() {
         return sql;
     }
 
     public final PrintWriter getLoggerOutput() {
         return getLogger().getWriter();
     }
 
     /**
      * Get the properties for the {@link Server#configpath} file
      * @return
      *        The {@link Properties} object
      */
     public final Properties getSystemProperties() {
         return p;
     }
 
     /**
     * Get the Block Tracker that is tracking all the block changes on the server.
     * @return
     *        The {@link BlockTracker} object
     */
    public final BlockTracker getBlockTracker() {
        return bt;
    }

    /**
      * Get the object that controls the updating of plugins
      * and other {@link Updatable} objects.
      * @return
      *       The {@link UpdateService} object
      */
     public final UpdateService getUpdateService() {
         return us;
     }
 
     /**
      * Gets the class that handles messages
      * @return The Message class
      */
     public final Messages getMessages() {
         return m;
     }
 
     /**
      * Save the system settings
      * @throws IOException 
      */
     public void saveSystemSettings() throws IOException {
         getSystemProperties().save("system.config");
     }
 
     /**
      * The contructor to make a new {@link Server} object
      * @param Name
      *            The default name of the server.
      *            This will be changed if the properties
      *            file has something different.
      * @param Port
      *            The default port of the server.
      *            This will be changed if the properties
      *            file has something different.
      * @param MOTD
      *            The MoTD message for the server.
      *            This will be changed if the properties
      *            file has something different.
      */
     public Server(String Name, int Port, String MOTD) {
         this.Port = Port;
         this.Name = Name;
         this.MOTD = MOTD;
     }
 
     /**
      * Get the salt.
      * <b>This method can only be called by heartbeaters and the Connect Packet.
      * If this method is called anywhere else, then a {@link IllegalAccessException} is thrown</b>
      * @return
      *        The server Salt
      * @throws IllegalAccessException
      *                               This is thrown when an attempt to call this method
      *                               is invalid.
      */
     public final String getSalt() throws IllegalAccessException {
         StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
         try {
             StackTraceElement e = stacks[2]; //The heartbeat class will always be the 3rd in the stacktrace if the heartbeat is being sent correctly
             Class<?> class_ = Class.forName(e.getClassName());
             class_.asSubclass(Heart.class);
             return Salt;
         } catch (ClassNotFoundException e1) { }
         catch (ClassCastException e2) { }
         catch (ArrayIndexOutOfBoundsException e3) { }
         try {
             if (stacks[4].getClassName().equals("net.mcforge.networking.packets.classicminecraft.Connect"))
                 return Salt;
         }
         catch (ArrayIndexOutOfBoundsException e3) { }
         throw new IllegalAccessException("The salt can only be accessed by the heartbeaters and the Connect packet!");
     }
 
     /**
      * Log an exception to the logger
      * This method will also invoke {@link Throwable#printStackTrace()}
      * @param t The exception to log
      */
     public void logError(Throwable t) {
         t.printStackTrace(getLoggerOutput());
         t.printStackTrace();
     }
 
     /**
      * Load the server properties such as the server {@link Server#Name}.
      * These properties will always load from the {@link Server#configpath}
      */
     public void loadSystemProperties() {
         Name = getSystemProperties().getValue("Server-Name");
         altName = getSystemProperties().getValue("WOM-Alternate-Name");
         MOTD = getSystemProperties().getValue("MOTD");
         Port = getSystemProperties().getInt("Port");
         MaxPlayers = getSystemProperties().getInt("Max-Players");
         Public = getSystemProperties().getBool("Public");
         description = getSystemProperties().getValue("WOM-Server-description");
         flags = getSystemProperties().getValue("WOM-Server-Flags");
         VerifyNames = getSystemProperties().getBool("Verify-Names");
         newSand = getSystemProperties().getBool("Advanced-Sand");
         CurrencyName = getSystemProperties().getValue("Money-Name");
         loadOnGoto = getSystemProperties().getBool("Load-On-Goto");
         if (getSystemProperties().hasValue("defaultColor"))
             defaultColor = ChatColor.parse(getSystemProperties().getValue("defaultColor"));
         else
             defaultColor = ChatColor.White;
     }
 
     /**
      * Start the logger.
      * The logger can be started before the server is started, but
      * this method is called in {@link Server#Start()}
      */
     public void startLogger() {
         Calendar cal = Calendar.getInstance();
         cal.clear(Calendar.HOUR);
         cal.clear(Calendar.MINUTE);
         cal.clear(Calendar.SECOND);
         cal.clear(Calendar.MILLISECOND);
         logger = new Logger("logs/" + cal.getTime() + ".txt", this);
         String filename = cal.getTime().toString().replace(" ", "-");
         String finalname = filename.split("-")[0] + "-" + filename.split("-")[1] + "-" + filename.split("-")[2];
         try {
             logger.ChangeFilePath("logs/" , finalname + ".txt");
         } catch (IOException e2) {
             System.out.println("logs/" + finalname + ".txt");
             e2.printStackTrace();
         }
         try {
             logger.Start(false);
         } catch (FileNotFoundException e) {
             e.printStackTrace();
         }
     }
 
     /**
      * Start the SQL service
      * @param set
      *           The SQL interface to use
      */
     public void startSQL(ISQL set) {
         if (set == null) {
             try {
                 sql = (ISQL)Class.forName(getSystemProperties().getValue("SQL-Driver")).newInstance();
             } catch (InstantiationException e) {
                 e.printStackTrace();
             } catch (IllegalAccessException e) {
                 e.printStackTrace();
             } catch (ClassNotFoundException e) {
                 e.printStackTrace();
             }
             sql.setPrefix(getSystemProperties().getValue("SQL-table-prefix"));
             if (sql instanceof MySQL) {
                 final MySQL mysql = (MySQL)sql;
                 mysql.setUsername(getSystemProperties().getValue("MySQL-username"));
                 mysql.setPassword(getSystemProperties().getValue("MySQL-password"));
                 mysql.setDatabase(getSystemProperties().getValue("MySQL-database-name"));
                 mysql.setIP(getSystemProperties().getValue("MySQL-IP"));
                 mysql.setPort(Integer.parseInt(getSystemProperties().getValue("MySQL-Port")));
             }
             else if (sql instanceof SQLite) {
                 final SQLite sqlite = (SQLite)sql;
                 sqlite.setFile(getSystemProperties().getValue("SQLite-File"));
             }
         }
         else
             sql = set;
         sql.connect(this);
         final String[] commands = new String[] {
                 "CREATE TABLE if not exists " + sql.getPrefix() + "_extra (name VARCHAR(20), setting TEXT, value BLOB);",
         };
         sql.executeQuery(commands);
         Log("SQL all set.");
     }
 
     /**
      * Start the SQL service
      */
     public void startSQL() {
         startSQL(null);
     }
 
     /**
      * Start the event system
      */
     public void startEvents() {
         if (es == null)
             es = new EventSystem(this);
     }
 
 
     public void startTicker() {
         ticker = new Ticker();
         ticker.startTick();
     }
 
     /**
      * Start listening to the port specified in {@link Server#Port}
      * and start accepting new connections.
      */
     public void startListening() {
         pm = new PacketManager(this);
         pm.startReading();
     }
 
     //TODO Documentation
     public void start() throws IllegalAccessException {
         start(true, new ServerStartupArgs());
     }
 
     public void start(ServerStartupArgs args) throws IllegalAccessException {
         start(true, args);
     }
 
     //TODO Documentation
     public void start(boolean startsql) throws IllegalAccessException {
         start(startsql, new ServerStartupArgs());
     }
 
     public void start(boolean startsql, ServerStartupArgs args) throws IllegalAccessException {
         Console c = null;
         try {
             StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
             StackTraceElement e = stacks[2];
             Class<?> class_ = Class.forName(e.getClassName());
             c = class_.asSubclass(Console.class).newInstance();
         } catch (Exception e) {
             try {
                 StackTraceElement[] stacks = Thread.currentThread().getStackTrace();
                 StackTraceElement ste = stacks[3];
                 Class<?> class_ = Class.forName(ste.getClassName());
                 c = class_.asSubclass(Console.class).newInstance();
             } catch (Exception ee) {
                 throw new IllegalAccessException("Server started from outside a console object!");
             }
         }
         start(c, startsql, args);
     }
 
     public void start(Console c, ServerStartupArgs args) {
         start(c, true, args);
     }
 
     public void start(Console c, boolean startSQL) {
         start(c, startSQL, new ServerStartupArgs());
     }
 
     /**
      * Start the server
      */
     @SuppressWarnings("restriction")
     public void start(Console console, boolean startSQL, ServerStartupArgs args) {
         if (Running)
             return;
         Running = true;
         startTicker();
         this.console = console;
         console.setServer(this);
         if (args.isLoadingEvents())
             startEvents();
         if (args.isLoadingLogger())
             startLogger();
         Log("=============================");
         Log("Starting MCForge v" + VERSION);
         ch = new CommandHandler(this);
         FileUtils.createFilesAndDirs();
         if (args.isLoadingGroups())
             Group.load(this);
         if (args.isLoadingProperties()) {
             p = Properties.init(this);
             if (!p.getBool("Verify-Names")) {
                 Log("!!WARNING!! You are running the server with verify names off, this means");
                 Log("anyone can login to the server with any username. Its recommended to turn this");
                 Log("this option on, if you know what your doing, then ignore this message.");
             }
             loadSystemProperties();
         }
         if (args.isLoadingUpdateService())
             us = new UpdateService(this);
         m = new Messages(this);
         if (args.isLoadingPlugins())
             ph = new PluginHandler(this);
         if (args.isLoadingGenerator())
             gh = new GeneratorHandler();
         startListening();
         if (args.isLoadingPrivileges())
             prh = new PrivilegesHandler(this);
         if (args.isLoadingPlugins())
             ph.loadplugins();
         try {
             if (args.isLoadingPrivileges())
                 prh.initialize();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         if (args.isLoadingPlugins())
             Log("Loaded plugins");
         if (args.isLoadingLevels()) {
             Serializer.getKryo().setClassLoader(getDefaultClassLoader());
             lm = new LevelHandler(this);
             MainLevel = getSystemProperties().getValue("MainLevel");
             if (!new File("levels/" + MainLevel + ".ggs").exists()) {
                 lm.newClassicLevel("Main", (short)64, (short)64, (short)64);
                 MainLevel = "Main";
             }
             lm.loadClassicLevels();
             Log("Loaded levels");
         }
         if (args.isLoadingUpdateService())
             us.getUpdateManager().add(this);
         SecureRandom sr = null;
         try {
             sr = SecureRandom.getInstance("SHA1PRNG");
         } catch (NoSuchAlgorithmException e1) {
             e1.printStackTrace();
         }
         for (int i = 0; i < 100; i++) {
             byte[] seedb = new byte[16];
             sr.nextBytes(seedb);
             Salt = new sun.misc.BASE64Encoder().encode(seedb).replace("=", "" + ((Salt != null) ? Salt.toCharArray()[0] : "A"));
             if (new Random().nextDouble() < .3)
                 break;
         }
         Salt = LetterOrNumber(Salt);
         Salt = Salt.substring(0, 16);
         Log("SALT: " + Salt);
         if (startSQL)
             startSQL();
         if (args.isLoadingHeartbeat()) {
             heartbeater = new Beat(this);
             heartbeater.addHeart(new MBeat());
             heartbeater.addHeart(new WBeat());
             heartbeater.addHeart(new ForgeBeat());
             heartbeater.startBeating();
         }
         Log("Created heartbeat");
        if (args.isLoadingBlockTracking()) {
            bt = new BlockTracker(this);
            Log("Created and Started the Block Tracker");
        }
         Log("Server url can be found in 'url.txt'");
 
         if (args.isLoadingGenerator()) {
             gh.addGenerator(new FlatGrass(this));
             gh.addGenerator(new Forest(this));
             gh.addGenerator(new Island(this));
             gh.addGenerator(new Mountains(this));
             gh.addGenerator(new Ocean(this));
             gh.addGenerator(new Pixel(this));
             gh.addGenerator(new Rainbow(this));
             gh.addGenerator(new Space(this));
         }
 
         if (args.isLoadingCommands())
             getCommandHandler().addCommand(new CmdAbort());
         if (args.isLoadingEvents()) {
             ServerStartedEvent sse = new ServerStartedEvent(this);
             es.callEvent(sse);
         }
         getTicker().addTick(this);
     }
 
     /**
      * Get all the {@link IOClient} connected to the server
      * @return
      *        An {@link ArrayList} of {@link IOClient}
      */
     public ArrayList<IOClient> getClients() {
         return pm.getConnectedClients();
     }
 
     /**
      * Get all the {@link Player} connected to the server
      * @return
      *         An {@link ArrayList} of {@link Player}
      */
     public List<Player> getPlayers() {
         if (getClients().equals(cache) && getClients().size() == oldsize)
             return Collections.unmodifiableList(pcache);
         pcache = new ArrayList<Player>();
         for (IOClient i : getClients()) {
             if (i instanceof Player)
                 pcache.add((Player)i);
         }
         cache = getClients();
         oldsize = cache.size();
         return Collections.unmodifiableList(pcache);
     }
 
     /**
      * Send a message to all the players on this server
      * @param message
      *               The message to send
      */
     public void sendGlobalMessage(String message) {
         m.serverBroadcast(message);
     }
 
     /**
      * Send a message to all players on the level <b>world</b>
      * @param message
      *               The message to send
      * @param world
      *             The world to send to
      */
     public void sendWorldMessage(String message, Level world) {
         sendWorldMessage(message, world.getName());
     }
 
     /**
      * Send a message to all the players on the level with the name <b>world</b>
      * @param message
      *               The message to send
      * @param world
      *             The world name to send to
      */
     public void sendWorldMessage(String message, String world) {
         m.worldBroadcast(message, world);
     }
 
 
     /**
      * Search for a player based on the name given.
      * A part of the name will be given and will find
      * the full name and player. If part of the name is given, and
      * more than 1 player is found, then it will return null.
      * @param name
      *            The full/part name of the player
      * @return
      *        The player found. If more than 1 player is found,
      *        then it will return null.
      */
     public Player findPlayer(String name) {
         Player toreturn = null;
         for (int i = 0; i < getPlayers().size(); i++) {
             if (name.equalsIgnoreCase(getPlayers().get(i).username))
                 return getPlayers().get(i);
             else if (getPlayers().get(i).username.toLowerCase().indexOf(name.toLowerCase()) != -1 && toreturn == null)
                 toreturn = getPlayers().get(i);
             else if (getPlayers().get(i).username.toLowerCase().indexOf(name.toLowerCase()) != -1 && toreturn != null)
                 return null;
         }
         return toreturn;
     }
     private static String LetterOrNumber(String string) {
         final String works = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz1234567890";
         String finals = "";
         boolean change = true;
         for (char c : string.toCharArray()) {
             for (char x : works.toCharArray()) {
                 if (x == c) {
                     change = false;
                     break;
                 }
             }
             if (change)
                 finals += works.toCharArray()[new Random().nextInt(works.toCharArray().length)];
             else
                 finals += c;
             change = true;
         }
         return finals;
     }
 
     /**
      * Stop the server, this will kick all the players on the server
      * and stop all server services.
      * @throws InterruptedException
      *                             if any thread has interrupted the current thread. The interrupted status of the current thread is cleared when this exception is thrown.
      * @throws IOException
      *                    If there is a problem saving the levels that are loaded
      */
     public void stop() throws InterruptedException, IOException {
         if (!Running)
             return;
         Running = false;
         Log("Stopping server...");
         getTicker().removeTick(this);
         for(Player p : players)
         {
             p.sendMessage("Stopping server...");
         }
         for (Level l : this.getLevelHandler().getLevelList()) {
             if (!l.getName().equals(MainLevel))
                 l.unload(this);
         }
         if (getLevelHandler().findLevel(MainLevel) != null) {
             getLevelHandler().findLevel(MainLevel).save();
             getLevelHandler().findLevel(MainLevel).unload(this);
         }
         lm.stopBackup();
         ticker.stopTick();
         logger.Stop();
        bt.dispose();
         heartbeater.stopBeating();
         ArrayList<Player> players = new ArrayList<Player>();
         for (Player p : getPlayers())
             players.add(p);
                 for (int i = 0; i < players.size(); i++)
                     players.get(i).kick("Server shutting down!");
                 Properties.reset();
                 pm.stopReading();
     }
 
     /**
      * Log something to the logs
      * @param log
      */
     public void Log(String log) {
         if (logger == null)
             return;
         logger.Log(log);
     }
 
     /**
      * Get the logger object
      * @return
      *        The {@link Logger} object
      */
     public Logger getLogger() {
         return logger;
     }
 
     /**
      * Add a task to be called every 10 milliseconds
      * @param t
      *         The {@link Tick} object to call
      * 
      * @deprecated Use {@link Server#getTicker()} and {@link Ticker#addTick(Tick)}
      */
     @Deprecated
     public void Add(Tick t) {
         ticker.addTick(t);
     }
 
     public Ticker getTicker() {
         return ticker;
     }
 
     /**
      * Remove a task from the Tick list
      * @param t
      *         The {@link Tick} object to remove
      * @deprecated Use {@link Server#getTicker()} and {@link Ticker#removeTick(Tick)}
      */
     @Deprecated
     public void Remove(Tick t) {
         ticker.removeTick(t);
     }
     
     @Override
     public void tick() {
         try {
             if (oldsize != getSystemProperties().getKeys().length) {
                 saveSystemSettings();
                 oldsize = getSystemProperties().getKeys().length;
             }
         } catch (IOException e) {
         }
     }
     
     @Override
     public boolean inSeperateThread() {
         return false;
     }
     
     @Override
     public int getTimeout() {
         return 50;
     }
 
     @Override
     public void onLog(String message) {
         //TODO ..colors?
                 ServerLogEvent sle = new ServerLogEvent(this, message, message.split("\\]")[1].trim());
                 if (es != null)
                     this.es.callEvent(sle);
                 System.out.println(message);
     }
     @Override
     public void onError(String message) {
         //TODO ..colors?
         ServerLogEvent sle = new ServerLogEvent(this, message, message.split("\\]")[1].trim());
         if (es != null)
             this.es.callEvent(sle);
         System.out.println(message);
         log.log(java.util.logging.Level.SEVERE, message);
     }
 
     /**
      * Calls {@link Server#findPlayer(String)}
      */
     public Player getPlayer(String name) {
         return findPlayer(name);
     }
     @Override
     public String getCheckURL() {
         return "http://update.mcforge.net/mcf6/current.txt";
     }
     @Override
     public String getDownloadURL() {
         return "www.mcforge.net";
     }
     @Override
     public UpdateType getUpdateType() {
         return UpdateType.Manual;
     }
     @Override
     public String getCurrentVersion() {
         return VERSION;
     }
     @Override
     public String getDownloadPath() {
         return "";
     }
     @Override
     public String getName() {
         return "MCForge";
     }
     @Override
     public void unload() {
         try {
             stop(); //This method shouldn't be called...
         } catch (InterruptedException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
     }
 }
