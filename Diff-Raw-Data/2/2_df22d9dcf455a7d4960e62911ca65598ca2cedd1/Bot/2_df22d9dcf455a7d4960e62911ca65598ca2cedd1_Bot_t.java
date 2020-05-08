 /*
  * @author Kyle Kemp
  */
 package backend;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.management.ManagementFactory;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.lang.reflect.Modifier;
 import java.sql.SQLException;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Set;
 import java.util.concurrent.CopyOnWriteArrayList;
 import java.util.concurrent.ScheduledFuture;
 import java.util.concurrent.TimeUnit;
 
 import lombok.Getter;
 import lombok.Setter;
 import modules.Module;
 
 import org.pircbotx.Channel;
 import org.pircbotx.PircBotX;
 import org.pircbotx.User;
 import org.pircbotx.UtilSSLSocketFactory;
 import org.pircbotx.exception.IrcException;
 import org.pircbotx.hooks.events.DisconnectEvent;
 import org.reflections.Reflections;
 
 import commands.Command;
 
 /**
  * The Class Bot.
  */
 public class Bot extends PircBotX implements Constants {
 
 	/** The timer. */
 	private static TimerBackend timer = new TimerBackend(5);
 
 	/**
 	 * Gets the owners.
 	 * 
 	 * @return the owners
 	 */
 	@Getter
 	private static HashSet<String> owners = new HashSet<>();
 
 	/**
 	 * Gets the elevated.
 	 * 
 	 * @return the elevated
 	 */
 	@Getter
 	private static HashSet<String> elevated = new HashSet<>();
 
 	/**
 	 * Gets the banned.
 	 * 
 	 * @return the banned
 	 */
 	@Getter
 	private static HashSet<String> banned = new HashSet<>();
 
 	static {
 		List<HashMap<String, Object>> data = null;
 		try {
 			Database.createTable("bot_users",
 					"name char(25) not null, owner smallint, elevated smallint, banned smallint");
 			data = Database.select("select * from bot_users");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 
 		if (data != null) {
 			for (HashMap<String, Object> column : data) {
 				Object i = new Integer(0);
 				if (column.get("BANNED") != null
 						&& !(column.get("BANNED").equals(i)))
 					addBanned(column.get("NAME").toString());
 				if (column.get("OWNER") != null
 						&& !(column.get("OWNER").equals(i)))
 					addOwner(column.get("NAME").toString());
 				if (column.get("ELEVATED") != null
 						&& !(column.get("ELEVATED").equals(i)))
 					addElevated(column.get("NAME").toString());
 			}
 		}
 	}
 
 	// All of the Bot instances
 	/** The bots. */
 
 	/**
 	 * Gets the bots.
 	 * 
 	 * @return the bots
 	 */
 	@Getter
 	private static LinkedList<Bot> bots = new LinkedList<>();
 
 	// Constants
 	/** The Constant INTERNAL_VERSION. */
 	public final static String INTERNAL_VERSION = "2.04";
 
 	/** The Constant DEFAULT_SERVER. */
 	public final static String DEFAULT_SERVER = "irc.esper.net";
 
 	/** The Constant DEFAULT_NICKNAME. */
 	public final static String DEFAULT_NICKNAME = "Jar";
 
 	/** The Constant DEFAULT_PORT. */
 	public final static int DEFAULT_PORT = 6667;
 
 	// Variables
 
 	/**
 	 * Checks if is uses ssl.
 	 * 
 	 * @return true, if is uses ssl
 	 */
 	@Getter
 	private boolean usesSSL = false;
 
 	/**
 	 * Gets the server password.
 	 * 
 	 * @return the server password
 	 */
 	@Getter
 	private String serverPassword = "";
 
 	/**
 	 * Gets the identify pass.
 	 * 
 	 * @return the identify pass
 	 */
 	@Getter
 	private String identifyPass;
 
 	/**
 	 * Checks if is parses self.
 	 * 
 	 * @return true, if is parses self
 	 */
 	@Getter
 	/**
 	 * Sets the parses self.
 	 *
 	 * @param parsesSelf the new parses self
 	 * @deprecated
 	 */
 	@Setter
 	private boolean parsesSelf = false;
 
 	/**
 	 * Gets the bot mode.
 	 * 
 	 * @return the bot mode
 	 */
 	@Getter
 	/**
 	 * Sets the bot mode.
 	 *
 	 * @param botMode the new bot mode
 	 */
 	@Setter
 	private int botMode = ACCESS_DEVELOPMENT;
 
 	/**
 	 * Checks if is parses cmd.
 	 * 
 	 * @return true, if is parses cmd
 	 */
 	@Getter
 	/**
 	 * Sets the parses cmd.
 	 *
 	 * @param parsesCmd the new parses cmd
 	 */
 	@Setter
 	private boolean parsesCmd = true;
 
 	/**
 	 * Checks if is logs self.
 	 * 
 	 * @return true, if is logs self
 	 */
 	@Getter
 	/**
 	 * Sets the logs self.
 	 *
 	 * @param logsSelf the new logs self
 	 */
 	@Setter
 	private boolean logsSelf = true;
 
 	// Module comparator
 	/**
 	 * The Class ModComparator.
 	 */
 	private class ModComparator implements Comparator<Module> {
 
 		/*
 		 * (non-Javadoc)
 		 * 
 		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
 		 */
 		@Override
 		public int compare(Module o1, Module o2) {
 			if (o2.getPriorityLevel() - o1.getPriorityLevel() == 0)
 				return o1.getName().compareTo(o2.getName());
 			return o2.getPriorityLevel() - o1.getPriorityLevel();
 		}
 
 	}
 
 	/**
 	 * Gets the modules.
 	 * 
 	 * @return the modules
 	 */
 	@Getter
 	private CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();
 
 	// Constructors
 	/**
 	 * Instantiates a new bot.
 	 */
 	public Bot() {
 		this(DEFAULT_SERVER);
 	}
 
 	/**
 	 * Instantiates a new bot.
 	 * 
 	 * @param server
 	 *            the server
 	 */
 	public Bot(String server) {
 		this(server, DEFAULT_PORT);
 	}
 
 	/**
 	 * Instantiates a new bot.
 	 * 
 	 * @param server
 	 *            the server
 	 * @param port
 	 *            the port
 	 */
 	public Bot(String server, int port) {
 		this(server, port, false);
 	}
 
 	/**
 	 * Instantiates a new bot.
 	 * 
 	 * @param server
 	 *            the server
 	 * @param port
 	 *            the port
 	 * @param SSL
 	 *            the ssl
 	 */
 	public Bot(String server, int port, boolean SSL) {
 		this(server, port, SSL, DEFAULT_NICKNAME);
 	}
 
 	/**
 	 * Instantiates a new bot.
 	 * 
 	 * @param server
 	 *            the server
 	 * @param port
 	 *            the port
 	 * @param SSL
 	 *            the ssl
 	 * @param nick
 	 *            the nick
 	 */
 	public Bot(String server, int port, boolean SSL, String nick) {
 		this(server, port, SSL, nick, "");
 	}
 
 	/**
 	 * Instantiates a new bot.
 	 * 
 	 * @param server
 	 *            the server
 	 * @param port
 	 *            the port
 	 * @param SSL
 	 *            the ssl
 	 * @param nick
 	 *            the nick
 	 * @param serverPass
 	 *            the server pass
 	 */
 	public Bot(String server, int port, boolean SSL, String nick,
 			String serverPass) {
 		initialize();
 		loadModules();
 		connectToServer(server, port, SSL, nick, serverPass);
 
 	}
 
 	// Methods
 	/**
 	 * Connect to server.
 	 * 
 	 * @param server
 	 *            the server
 	 * @param port
 	 *            the port
 	 * @param SSL
 	 *            the ssl
 	 * @param nick
 	 *            the nick
 	 * @param serverPass
 	 *            the server pass
 	 */
 	private void connectToServer(String server, int port, boolean SSL,
 			String nick, String serverPass) {
 
 		this.setAutoNickChange(true);
 		this.setVerbose(true);
 		this.setAutoSplitMessage(true);
 		this.setMessageDelay(500);
 
 		this.setFinger("Don't finger me! Vivio v" + INTERNAL_VERSION);
 
 		this.setVersion("PircBotX~Vivio v" + INTERNAL_VERSION);
 		this.setLogin(nick);
 		this.setName(nick);
 		this.usesSSL = SSL;
 		this.serverPassword = serverPass;
 
 		try {
 			if (SSL)
 				this.connect(server, port, serverPass,
 						new UtilSSLSocketFactory().trustAllCertificates());
 			else
 				this.connect(server, port, serverPass);
 		} catch (IOException | IrcException e) {
 			e.printStackTrace();
 		}
 	}
 
 	// initialize the bot
 	/**
 	 * Initialize.
 	 */
 	private void initialize() {
 		bots.add(this);
 		this.setListenerManager(ListenerBuilder.getManager());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#identify(java.lang.String)
 	 */
 	@Override
 	public void identify(String pass) {
 		this.identifyPass = pass;
 		super.identify(pass);
 	}
 
 	/**
 	 * Load modules.
 	 */
 	public void loadModules() {
 		modules.clear();
 
 		loadModulesImpl("commands", Command.class);
 		loadModulesImpl("cmods", Command.class);
 		loadModulesImpl("modules", Module.class);
 		loadModulesImpl("games", Command.class);
 	}
 
 	/**
 	 * Load modules impl.
 	 * 
 	 * @param pkg
 	 *            the pkg
 	 * @param cls
 	 *            the cls
 	 */
 	private void loadModulesImpl(String pkg, Class<?> cls) {
 		Reflections reflections = new Reflections(pkg);
 		Set<?> classes = reflections.getSubTypesOf(cls);
 		for (Object c : classes) {
 			Class<?> cl = (Class<?>) c;
 			if (Modifier.isAbstract(cl.getModifiers()))
 				continue;
 			try {
 				addModule((Module) Class.forName(cl.getName()).newInstance());
 			} catch (InstantiationException | IllegalAccessException
 					| ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	// get the default bot -- this will almost always be the proper one to get.
 	/**
 	 * Gets the bot.
 	 * 
 	 * @return the bot
 	 */
 	public static Bot getBot() {
 		return bots.getFirst();
 	}
 
 	// invoke a specific method on all of the commands and modules
 	/**
 	 * Invoke all.
 	 * 
 	 * @param method
 	 *            the method
 	 * @param args
 	 *            the args
 	 */
 	public void invokeAll(String method, Object[] args) {
 		for (Module m : modules) {
 
 			if (!m.isActive())
 				continue;
 			if (botMode < m.getAccessMode())
 				continue;
 
 			// invoke methods onFoo
 			for (Method methodName : m.getClass().getMethods()) {
 				if (methodName.getName().equals(method)) {
 					try {
 						methodName.invoke(m, args);
 					} catch (IllegalAccessException | IllegalArgumentException
 							| InvocationTargetException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 
 		}
 	}
 
 	// parse commands coming in
 	/**
 	 * Check commands.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param message
 	 *            the message
 	 * @param chan
 	 *            the chan
 	 * @param forceExecute
 	 *            the force execute
 	 * @return true, if successful
 	 */
 	public boolean checkCommands(User user, String message, Channel chan,
 			boolean forceExecute) {
 
 		String commandString;
 		if (message.startsWith(getNick() + ", ")) {
 			commandString = message.split(" ")[1];
 			forceExecute = true;
 			message = message.substring(message.indexOf(" "));
 		} else
 			commandString = message.split(" ")[0];
 
 		if (commandString.equals(""))
 			return false;
 
 		String comm = commandString.substring(1);
 
 		for (Module m : modules) {
 			if (m instanceof Command) {
 				Command command = (Command) m;
 				if (!command.isActive())
 					continue;
 				if (botMode < command.getAccessMode())
 					continue;
 				if (!forceExecute && !messageHasCommand(message, command))
 					continue;
 				if (!command.hasAlias(forceExecute ? commandString : comm))
 					continue;
 				int level = getLevelForUser(user, chan);
 				if (level == LEVEL_BANNED)
 					continue;
 				if (level < command.getAccessLevel()) {
 					sendMessage(chan == null ? user.getNick() : chan.getName(),
 							"You are not allowed to execute this command.");
 					continue;
 				}
 				logMessage(chan, user, message);
 				command.execute(forceExecute ? commandString : comm, this,
 						chan, user, message.trim());
 				if (command.isStopsExecution())
 					return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Log message.
 	 * 
 	 * @param chan
 	 *            the chan
 	 * @param user
 	 *            the user
 	 * @param message
 	 *            the message
 	 */
 	public void logMessage(Channel chan, User user, String message) {
 		for (Channel c : getChannels()) {
 			if (c.getName().contains("-logs")) {
 				sendMessage(
 						c,
 						">> "
 								+ (chan != null ? chan.getName()
 										: "Private Message") + " <"
 								+ user.getNick() + " ("
 								+ getLevelForUser(user, chan) + ") @ "
 								+ user.getHostmask() + "> " + message);
 			}
 		}
 	}
 
 	/**
 	 * Message has command.
 	 * 
 	 * @param message
 	 *            the message
 	 * @param c
 	 *            the c
 	 * @return true, if successful
 	 */
 	private boolean messageHasCommand(String message, Command c) {
 		// System.out.println(message.substring(0,1).equals(c.getCmdSequence())+
 		// " " + message.startsWith(getNick()+","));
 		return message.substring(0, 1).equals(c.getCmdSequence())
           || message.startsWith(getNick() + ",") || message.startsWith(getNick() + ":");
 	}
 
 	/**
 	 * Check commands.
 	 * 
 	 * @param user
 	 *            the user
 	 * @param message
 	 *            the message
 	 * @param chan
 	 *            the chan
 	 * @return true, if successful
 	 */
 	public boolean checkCommands(User user, String message, Channel chan) {
 		return checkCommands(user, message, chan, false);
 	}
 
 	/**
 	 * Adds the module.
 	 * 
 	 * @param m
 	 *            the m
 	 */
 	private void addModule(Module m) {
 		this.modules.add(m);
 		List<Module> modules = Arrays.asList(this.modules
 				.toArray(new Module[0]));
 		Collections.sort(modules, new ModComparator());
 		this.modules.clear();
 		this.modules.addAll(modules);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#sendAction(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void sendAction(String target, String action) {
 		if (banned.contains(target))
 			return;
 		super.sendAction(target, action);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#sendMessage(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public void sendMessage(String target, String message) {
 		if (banned.contains(target))
 			return;
 		super.sendMessage(target, message);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#sendNotice(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public void sendNotice(String target, String notice) {
 		if (banned.contains(target))
 			return;
 		super.sendNotice(target, notice);
 	}
 
 	/**
 	 * Gets the level for user.
 	 * 
 	 * @param u
 	 *            the u
 	 * @param c
 	 *            the c
 	 * @return the level for user
 	 */
 	public static int getLevelForUser(User u, Channel c) {
 		// TODO identified users only?
 		// TODO check host mask of user and if it matches bot, they are owner
 		if (u == null)
 			return LEVEL_OWNER;
 		if (u.getNick().equals("NickServ"))
 			return LEVEL_BANNED;
 		if (owners.contains(u.getNick().toLowerCase()))
 			return LEVEL_OWNER;
 		if (c != null && c.isOp(u))
 			return LEVEL_OPERATOR;
 		if (elevated.contains(u.getNick().toLowerCase()))
 			return LEVEL_ELEVATED;
 		if (banned.contains(u.getNick().toLowerCase()))
 			return LEVEL_BANNED;
 		return LEVEL_NORMAL;
 	}
 
 	/**
 	 * Adds the banned.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void addBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set banned=1 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"
 						+ bnd + "', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.add(bnd);
 	}
 
 	/**
 	 * Removes the banned.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void removeBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set banned=0 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"
 						+ bnd + "', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.remove(bnd);
 	}
 
 	/**
 	 * Adds the owner.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void addOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set owner=1 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"
 						+ bnd + "', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.add(bnd);
 	}
 
 	/**
 	 * Removes the owner.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void removeOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set owner=0 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"
 						+ bnd + "', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.remove(bnd);
 	}
 
 	/**
 	 * Adds the elevated.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void addElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set elevated=1 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"
 						+ bnd + "', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.add(bnd);
 	}
 
 	/**
 	 * Removes the elevated.
 	 * 
 	 * @param bnd
 	 *            the bnd
 	 */
 	public static void removeElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if (Database.hasRow("select * from bot_users where name='" + bnd
 					+ "'")) {
 				Database.execRaw("update bot_users set elevated=0 where name='"
 						+ bnd + "'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"
 						+ bnd + "', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.remove(bnd);
 	}
 
 	/**
 	 * Checks if is in channel.
 	 * 
 	 * @param channel
 	 *            the channel
 	 * @return true, if is in channel
 	 */
 	public boolean isInChannel(String channel) {
 		channel = channel.trim();
 		for (Channel c : getChannels()) {
 			if (c.getName().equals(Util.formatChannel(channel)))
 				return true;
 		}
 		return false;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#joinChannel(java.lang.String)
 	 */
 	@Override
 	public void joinChannel(String s) {
 		super.joinChannel(Util.formatChannel(s));
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#joinChannel(java.lang.String,
 	 * java.lang.String)
 	 */
 	@Override
 	public void joinChannel(String s, String k) {
 		super.joinChannel(Util.formatChannel(s), k);
 	}
 
 	/**
 	 * Gets the timer threads.
 	 * 
 	 * @return the timer threads
 	 */
 	public HashSet<TimerThread> getTimerThreads() {
 		return timer.getThreads();
 	}
 
 	/**
 	 * Gets the bot by server.
 	 * 
 	 * @param server
 	 *            the server
 	 * @return the bot by server
 	 */
 	public static Bot getBotByServer(String server) {
 		if (getBots().size() == 0)
 			return null;
 		server = server.trim();
 		for (Bot b : getBots()) {
 			if (!b.isConnected())
 				continue;
 			if (b.getServer().equals(server)) {
 				return b;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Schedule task.
 	 * 
 	 * @param timerThread
 	 *            the timer thread
 	 * @param delayInSeconds
 	 *            the delay in seconds
 	 * @return the scheduled future
 	 */
 	public static ScheduledFuture<?> scheduleTask(TimerThread timerThread,
 			int delayInSeconds) {
 		return scheduleTask(timerThread, delayInSeconds, TimeUnit.SECONDS);
 	}
 
 	/**
 	 * Schedule task.
 	 * 
 	 * @param timerThread
 	 *            the timer thread
 	 * @param delay
 	 *            the delay
 	 * @param timeUnit
 	 *            the time unit
 	 * @return the scheduled future
 	 */
 	public static ScheduledFuture<?> scheduleTask(TimerThread timerThread,
 			long delay, TimeUnit timeUnit) {
 		return timer.scheduleTask(timerThread, delay, timeUnit);
 	}
 
 	/**
 	 * Schedule one shot task.
 	 * 
 	 * @param timerThread
 	 *            the timer thread
 	 * @param delayInSeconds
 	 *            the delay in seconds
 	 * @return the scheduled future
 	 */
 	public static ScheduledFuture<?> scheduleOneShotTask(
 			TimerThread timerThread, int delayInSeconds) {
 		return scheduleOneShotTask(timerThread, delayInSeconds,
 				TimeUnit.SECONDS);
 	}
 
 	/**
 	 * Schedule one shot task.
 	 * 
 	 * @param timerThread
 	 *            the timer thread
 	 * @param delay
 	 *            the delay
 	 * @param timeUnit
 	 *            the time unit
 	 * @return the scheduled future
 	 */
 	public static ScheduledFuture<?> scheduleOneShotTask(
 			TimerThread timerThread, long delay, TimeUnit timeUnit) {
 		return timer.schedule(timerThread, delay, timeUnit);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#getChannel(java.lang.String)
 	 */
 	@Override
 	public Channel getChannel(String name) {
 		return super.getChannel(Util.formatChannel(name));
 	}
 
 	// TODO figure out why onpart isn't being called for self
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#partChannel(org.pircbotx.Channel)
 	 */
 	@Override
 	public void partChannel(Channel channel) {
 
 		String query = "delete from channelremembermodule_channels where server='"
 				+ getServer() + "' and channel='" + channel.getName() + "'";
 		try {
 			Database.execRaw(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		super.partChannel(channel);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.pircbotx.PircBotX#partChannel(org.pircbotx.Channel,
 	 * java.lang.String)
 	 */
 	@Override
 	public void partChannel(Channel channel, String reason) {
 		String query = "delete from channelremembermodule_channels where server='"
 				+ getServer() + "' and channel='" + channel.getName() + "'";
 		try {
 			Database.execRaw(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		super.partChannel(channel, reason);
 	}
 	
 	/** 
 	 * Sun property pointing the main class and its arguments. 
 	 * Might not be defined on non Hotspot VM implementations.
 	 */
 	public static final String SUN_JAVA_COMMAND = "sun.java.command";
 
 	/**
 	 * Restart the current Java application
 	 * @author Leo Lewis http://java.dzone.com/articles/programmatically-restart-java
 	 * @param runBeforeRestart some custom code to be run before restarting
 	 * @throws IOException
 	 */
 	public void rebootProcess(Runnable runBeforeRestart) throws IOException {
 		try {
 			this.disconnect();
 			this.waitFor(DisconnectEvent.class);
 			// java binary
 			String java = System.getProperty("java.home") + "/bin/java";
 			// vm arguments
 			List<String> vmArguments = ManagementFactory.getRuntimeMXBean().getInputArguments();
 			StringBuffer vmArgsOneLine = new StringBuffer();
 			for (String arg : vmArguments) {
 				// if it's the agent argument : we ignore it otherwise the
 				// address of the old application and the new one will be in conflict
 				if (!arg.contains("-agentlib")) {
 					vmArgsOneLine.append(arg);
 					vmArgsOneLine.append(" ");
 				}
 			}
 			// init the command to execute, add the vm args
 			final StringBuffer cmd = new StringBuffer("\"" + java + "\" " + vmArgsOneLine);
 
 			// program main and program arguments
 			String[] mainCommand = System.getProperty(SUN_JAVA_COMMAND).split(" ");
 			// program main is a jar
 			if (mainCommand[0].endsWith(".jar")) {
 				// if it's a jar, add -jar mainJar
 				cmd.append("-jar " + new File(mainCommand[0]).getPath());
 			} else {
 				// else it's a .class, add the classpath and mainClass
 				cmd.append("-cp \"" + System.getProperty("java.class.path") + "\" " + mainCommand[0]);
 			}
 			// finally add program arguments
 			for (int i = 1; i < mainCommand.length; i++) {
 				cmd.append(" ");
 				cmd.append(mainCommand[i]);
 			}
 			// execute the command in a shutdown hook, to be sure that all the
 			// resources have been disposed before restarting the application
 			Runtime.getRuntime().addShutdownHook(new Thread() {
 				@Override
 				public void run() {
 					try {
 						Runtime.getRuntime().exec(cmd.toString());
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 				}
 			});
 			// execute some custom code before restarting
 			if (runBeforeRestart!= null) {
 				runBeforeRestart.run();
 			}
 			// exit
 			System.exit(0);
 		} catch (Exception e) {
 			// something went wrong
 			throw new IOException("Error while trying to restart the application", e);
 		}
 	}
 }
