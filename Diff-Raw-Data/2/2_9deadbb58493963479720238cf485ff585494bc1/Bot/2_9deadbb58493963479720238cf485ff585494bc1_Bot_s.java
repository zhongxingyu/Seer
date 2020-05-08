 /*
  * @author Kyle Kemp
  */
 package backend;
 
 import java.io.IOException;
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
 import org.reflections.Reflections;
 
 import commands.Command;
 
 public class Bot extends PircBotX implements Constants{
 	
 	private static TimerBackend timer = new TimerBackend(5);
 
 	@Getter private static HashSet<String> owners = new HashSet<>();
 	@Getter private static HashSet<String> elevated = new HashSet<>();
 	@Getter private static HashSet<String> banned = new HashSet<>();
 	
 	static {
 		List<HashMap<String,Object>> data = null;
 		try {
 			Database.createTable("bot_users", "name char(25) not null, owner smallint, elevated smallint, banned smallint");
 			data = Database.select("select * from bot_users");
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		
 		if(data != null) {
 			for(HashMap<String,Object> column : data) {
 				Object i = new Integer(0);
 				if(column.get("BANNED")!=null && !(column.get("BANNED").equals(i))) addBanned(column.get("NAME").toString());
 				if(column.get("OWNER")!=null && !(column.get("OWNER").equals(i))) addOwner(column.get("NAME").toString());
 				if(column.get("ELEVATED")!=null && !(column.get("ELEVATED").equals(i))) addElevated(column.get("NAME").toString());
 			}
 		}
 	}
 	
 	//All of the Bot instances
 	@Getter private static LinkedList<Bot> bots = new LinkedList<>();
 
 	//Constants
 	final static String INTERNAL_VERSION = "1.67";
 	public final static String DEFAULT_SERVER = "irc.esper.net";
 	public final static String DEFAULT_NICKNAME = "Jar";
 	public final static int DEFAULT_PORT = 6667;
 	
 	//Variables 
 	@Getter @Setter private boolean parsesSelf = false;
 	@Getter @Setter private int botMode = ACCESS_DEVELOPMENT;
 	@Getter @Setter private boolean parsesCmd = true;
 	@Getter @Setter private boolean logsSelf = true;
 	
 	//Module comparator
 	private class ModComparator implements Comparator<Module> {
 
 		@Override
 		public int compare(Module o1, Module o2) {
 			if(o2.getPriorityLevel() - o1.getPriorityLevel() == 0) return o1.getName().compareTo(o2.getName());
 			return o2.getPriorityLevel() - o1.getPriorityLevel();
 		}
 		
 	}
 	
 	@Getter
 	private CopyOnWriteArrayList<Module> modules = new CopyOnWriteArrayList<Module>();
 	
 	//Constructors
 	public Bot() {
 		this(DEFAULT_SERVER);
 	}
 
 	public Bot(String server) {
 		this(server, DEFAULT_PORT);
 	}
 
 	public Bot(String server, int port) {
 		this(server, port, false);
 	}
 
 	public Bot(String server, int port, boolean SSL) {
 		this(server, port, SSL, DEFAULT_NICKNAME);
 	}
 	
 	public Bot(String server, int port, boolean SSL, String nick)
 	{
 		this(server, port, SSL, nick,"");
 	}
 	
 	public Bot(String server, int port, boolean SSL, String nick,
 				String serverPass)
 	{
 		initialize();
 		loadModules();
 		connectToServer(server, port, SSL, nick, serverPass);
 		
 	}
 
 	//Methods
 	private void connectToServer(String server, int port, boolean SSL, String nick, String serverPass) {
 		
 		this.setAutoNickChange(true);
 		this.setVerbose(true);
 		this.setAutoSplitMessage(true);
 		this.setMessageDelay(500);
 		
 		this.setFinger("Don't finger me! Vivio v"+INTERNAL_VERSION);
 
 		this.setVersion("PircBotX~Vivio v" + INTERNAL_VERSION);
 		this.setLogin(nick);
 		this.setName(nick);
 
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
 
 	//initialize the bot
 	private void initialize() {
 		bots.add(this);
 		this.setListenerManager(ListenerBuilder.getManager());
 	}
 
 	public void loadModules() {
 		modules.clear();
 		
 		loadModulesImpl("commands", Command.class);
 		loadModulesImpl("cmods", Command.class);
 		loadModulesImpl("modules", Module.class);
 	}
 	
 	private void loadModulesImpl(String pkg, Class<?> cls) {
 		Reflections reflections = new Reflections(pkg);
 		Set<?> classes = reflections.getSubTypesOf(cls);
 		for(Object c : classes) {
 			Class<?> cl = (Class<?>) c;
 			if(Modifier.isAbstract(cl.getModifiers())) continue;
 			try {
 				addModule((Module) Class.forName(cl.getName()).newInstance());
 			} catch (InstantiationException | IllegalAccessException
 					| ClassNotFoundException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	//get the default bot -- this will almost always be the proper one to get.
 	public static Bot getBot() {
 		return bots.getFirst();
 	}
 	
 	//invoke a specific method on all of the commands and modules
 	public void invokeAll(String method, Object[] args) {
 		for(Module m : modules) {
 			
 			if(!m.isActive()) continue;
 			if(botMode < m.getAccessMode()) continue;
 			
 			//invoke methods onFoo
 			for(Method methodName : m.getClass().getMethods()) {
 				if(methodName.getName().equals(method)) {
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
 
 	//parse commands coming in
 	public boolean checkCommands(User user, String message, Channel chan, boolean forceExecute) {
 		
 		String commandString;
 		if(message.startsWith(getNick()+", ")) {
 			commandString = message.split(" ")[1];
 			forceExecute = true;
 			message = message.substring(message.indexOf(" "));
 		}
 		else
 			commandString = message.split(" ")[0];
 	
 		String comm = commandString.substring(1);
 				
 		for(Module m : modules) {
 			if(m instanceof Command) {
 				Command command = (Command) m;
 				if(!command.isActive()) continue;
 				if(botMode < command.getAccessMode()) continue;
 				if(!forceExecute && !messageHasCommand(message, command)) continue;
 				if(!command.hasAlias(forceExecute ? commandString : comm)) continue;
 				int level = getLevelForUser(user, chan);
 				if(level == LEVEL_BANNED) continue;
 				if(level < command.getAccessLevel()) {
 					sendMessage(chan == null ? user.getNick() : chan.getName(), "You are not allowed to execute this command.");
 					continue;
 				}
 				logMessage(chan, user, message);
 				command.execute(forceExecute ? commandString : comm, this, chan, user, message.trim());
 				if(command.isStopsExecution()) return false;
 			}
 		}
 		return true;
 	}
 	
 	public void logMessage(Channel chan, User user, String message) {
 		for(Channel c : getChannels()) {
 			if(c.getName().contains("-logs")) {
 				sendMessage(c, ">> "+(chan!=null ? chan.getName(): "Private Message") + " <" + user.getNick() + " ("+ getLevelForUser(user, chan)+") @ "+user.getHostmask()+"> " + message);
 			}
 		}
 	}
 	
 	private boolean messageHasCommand(String message, Command c) {
 		//System.out.println(message.substring(0,1).equals(c.getCmdSequence())+ " " + message.startsWith(getNick()+","));
 		return message.substring(0,1).equals(c.getCmdSequence()) || message.startsWith(getNick()+",");
 	}
 	
 	public boolean checkCommands(User user, String message, Channel chan) {
 		return checkCommands(user, message, chan, false);
 	}
 	
 	private void addModule(Module m) {
 		this.modules.add(m);
 		List<Module> modules = Arrays.asList(this.modules.toArray(new Module[0]));
 		Collections.sort(modules, new ModComparator());
 		this.modules.clear();
 		this.modules.addAll(modules);
 	}
 
 	@Override
 	public void sendAction(String target, String action) {
 		if(banned.contains(target)) return;
 		super.sendAction(target, action);
 	}
 
 	@Override
 	public void sendMessage(String target, String message) {
 		if(banned.contains(target)) return;
 		super.sendMessage(target, message);
 	}
 
 	@Override
 	public void sendNotice(String target, String notice) {
 		if(banned.contains(target)) return;
 		super.sendNotice(target, notice);
 	}
 
 	public static int getLevelForUser(User u, Channel c) {
 		//TODO identified users only?
 		if(u == null) return LEVEL_OWNER;
 		if(u.getNick().equals("NickServ")) return LEVEL_BANNED;
 		if(owners.contains(u.getNick().toLowerCase())) return LEVEL_OWNER;
 		if(c!=null && c.isOp(u)) return LEVEL_OPERATOR;
 		if(elevated.contains(u.getNick().toLowerCase())) return LEVEL_ELEVATED;
 		if(banned.contains(u.getNick().toLowerCase())) return LEVEL_BANNED;
 		return LEVEL_NORMAL;
 	}
 	
 	public static void addBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set banned=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.add(bnd);
 	}
 	
 	public static void removeBanned(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set banned=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, banned) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		banned.remove(bnd);
 	}
 	
 	public static void addOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set owner=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.add(bnd);
 	}
 	
 	public static void removeOwner(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set owner=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, owner) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		owners.remove(bnd);
 	}
 	
 	public static void addElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set elevated=1 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"+bnd+"', 1)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.add(bnd);
 	}
 	
 	public static void removeElevated(String bnd) {
 		bnd = bnd.trim().toLowerCase();
 		try {
 			if(Database.hasRow("select * from bot_users where name='"+bnd+"'")) {
 				Database.execRaw("update bot_users set elevated=0 where name='"+bnd+"'");
 			} else {
 				Database.execRaw("insert into bot_users (name, elevated) values ('"+bnd+"', 0)");
 			}
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		elevated.remove(bnd);
 	}
 
 	public boolean isInChannel(String channel) {
 		channel = channel.trim();
 		for(Channel c : getChannels()) {
 			if(c.getName().equals(Util.formatChannel(channel))) return true;
 		}
 		return false;
 	}
 
 	@Override
 	public void joinChannel(String s) {
 		super.joinChannel(Util.formatChannel(s));
 	}
 	
 	@Override
 	public void joinChannel(String s, String k) {
 		super.joinChannel(Util.formatChannel(s), k);
 	}
 	
 	public HashSet<TimerThread> getTimerThreads() {
 		return timer.getThreads();
 	}
 	
 	public static Bot getBotByServer(String server) {
 		server = server.trim();
 		for(Bot b : getBots())
 			if(b.getServer().equals(server)) return b;
 		return null;
 	}
 
 	public static ScheduledFuture<?> scheduleTask(TimerThread timerThread, int delayInSeconds) {
 		return scheduleTask(timerThread, delayInSeconds, TimeUnit.SECONDS);
 	}
 	
 	public static ScheduledFuture<?> scheduleTask(TimerThread timerThread, long delay, TimeUnit timeUnit) {
 		return timer.scheduleTask(timerThread, delay, timeUnit);
 	}
 	
 	public static ScheduledFuture<?> scheduleOneShotTask(TimerThread timerThread, int delayInSeconds) {
 		return scheduleOneShotTask(timerThread, delayInSeconds, TimeUnit.SECONDS);
 	}
 	
 	public static ScheduledFuture<?> scheduleOneShotTask(TimerThread timerThread, long delay, TimeUnit timeUnit) {
 		return timer.schedule(timerThread, delay, timeUnit);
 	}
 
 	@Override
 	public Channel getChannel(String name) {
 		return super.getChannel(Util.formatChannel(name));
 	}
 
 	//TODO figure out why onpart isn't being called for self
 	@Override
 	public void partChannel(Channel channel) {
 
 		String query = "delete from channelremembermodule_channels where server='"+getServer()+"' and channel='"+channel.getName()+"'";
 		try {
 			Database.execRaw(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		super.partChannel(channel);
 	}
 
 	@Override
 	public void partChannel(Channel channel, String reason) {
 		String query = "delete from channelremembermodule_channels where server='"+getServer()+"' and channel='"+channel.getName()+"'";
 		try {
 			Database.execRaw(query);
 		} catch (SQLException e) {
 			e.printStackTrace();
 		}
 		super.partChannel(channel, reason);
 	}
 }
