 package uk.co.harcourtprogramming.internetrelaycats;
 
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Logger;
 import java.util.logging.Level;
 import java.io.IOException;
 
 /**
  * <p>The main class for InternetRelayCats</p>
  */
 public class InternetRelayCat implements Runnable, RelayCat
 {
 
 	/**
 	 * <p>The logger for the bot</p>
 	 */
 	private final static Logger log = Logger.getLogger("InternetRelayCat");
 
 	static Logger getLogger()
 	{
 		return log;
 	}
 
 	/**
 	 * <p>The host to which we shall connect to when the the thread is run</p>
 	 */
 	private final String host;
 	/**
 	 * <p>The name that this bot should attempt to connect with</p>
 	 */
 	private final String name;
 	private final int port;
 	private final boolean ssl;
 	/**
 	 * <p>A list of channels to connect to when the thread is run</p>
 	 */
 	private final List<String> channels;
 	/**
 	 * <p>The list of currently activated {@link Service Services}</p>
 	 */
 	private final List<Service> srvs = new ArrayList<Service>();
 	/**
 	 * <p>The list of currently activated {@link MessageService MessageServices}</p>
 	 */
 	private final List<MessageService> msrvs = new ArrayList<MessageService>();
 	/**
 	 * <p>The list of currently activated {@link MessageService MessageServices}</p>
 	 */
 	private final List<FilterService> fsrvs = new ArrayList<FilterService>();
 	/**
 	 * <p>Flag to denote that the bot is currently exiting</p>
 	 */
 	private boolean dispose = false;
 
 	/**
 	 * <p>instance of the underlying bot interface</p>
 	 */
 	protected MewlerImpl bot;
 	protected final Object botLock = new Object();
 
 	/**
 	 * <p>Creates a InternetRelayCat instance</p>
 	 * <p>The instance is initialised, and services can be added, but does not
 	 * connect to the server specified in host until it is run, either by
 	 * calling the {@link #run() run} method directly, or executing it in a new
 	 * {@link Thread} with:
 	 * <pre>    new Thread(InternetRelayCat).start();</pre>
 	 * </p>
 	 * <p>A list of channels can be supplied to the constructor so that they
 	 * are joined when the server connection is made. Other channels can be
 	 * joined later with {@link RelayCat#join(java.lang.String)}</p>
 	 * @param name the name for the bot
 	 * @param host the host to connect to
 	 * @param channels a list of channels to connect to as soon as a connection
 	 * is established
 	 * @throws IllegalArgumentException if the name or host are not supplied
 	 */
 	public InternetRelayCat(final String name, final String host, final List<String> channels, int port, boolean ssl)
 	{
 		super();
 		if (host==null) throw new IllegalArgumentException("Host must be supplied");
 		if (name==null || name.length() == 0)  throw new IllegalArgumentException("Name must be supplied");
 
 		this.host = host;
 		this.name = name;
 		this.port = port;
 		this.ssl = ssl;
 
 		if (channels == null)
 		{
 			this.channels = new ArrayList<String>(0);
 		}
 		else
 		{
 			this.channels = new ArrayList<String>(channels);
 		}
 	}
 
 	/**
 	 * <p>Creates a InternetRelayCat instance</p>
 	 * <p>The instance is initialised, and services can be added, but does not
 	 * connect to the server specified in host until it is run, either by
 	 * calling the {@link #run() run} method directly, or executing it in a new
 	 * {@link Thread} with:
 	 * <pre>    new Thread(InternetRelayCat).start();</pre>
 	 * </p>
 	 * <p>A list of channels can be supplied to the constructor so that they
 	 * are joined when the server connection is made. Other channels can be
 	 * joined later with {@link RelayCat#join(java.lang.String)}</p>
 	 * @param name the name for the bot
 	 * @param host the host to connect to
 	 * @param channels a list of channels to connect to as soon as a connection
 	 * is established
 	 * @throws IllegalArgumentException if the name or host are not supplied
 	 */
 	public InternetRelayCat(final String name, final String host, final List<String> channels)
 	{
 		this(name, host, channels, 6667, false);
 	}
 
 	/**
 	 * <p>Adds a service to the InternetRelayCat</p>
 	 * <p>{@link MessageService Message Services} will be forwarded inputs</p>
 	 * <p>{@link ExternalService External Services} will be correctly
 	 * initialised, and their threads started</p>
 	 * <p>Note that {@link Service Services} that do not fall into one of the
 	 * two above categories will have to be supplied with access to the {@link
 	 * RelayCat interface} through external code; this behaviour is not
 	 * recommended</p>
 	 * @param s the service to add
 	 */
 	public void addService(Service s)
 	{
 		synchronized(srvs)
 		{
 			if (dispose) return;
 			if (s instanceof ExternalService)
 			{
 				final ExternalService es = (ExternalService)s;
 				if (es.getInstance() != this)
 					throw new IllegalArgumentException("Supplied External Service does not belong to this RelayCat instance");
 				es.getThread().start();
 			}
 			log.log(Level.INFO, "Service Loaded: {0}@{1}",
 			    new Object[]{s.getClass().getSimpleName(), s.getId()});
 			if (s instanceof MessageService)
 			{
 				msrvs.add((MessageService)s);
 				log.log(Level.INFO, "Service {0}@{1} loaded as MessageService.",
 				    new Object[]{s.getClass().getSimpleName(), s.getId()});
 			}
 			if (s instanceof FilterService)
 			{
 				fsrvs.add((FilterService)s);
				log.log(Level.INFO, "Service {0}@{1} loaded as FilterService.",
 				    new Object[]{s.getClass().getSimpleName(), s.getId()});
 			}
 			srvs.add(s);
 		}
 	}
 
 	protected MewlerImpl createBot(String host, int port, boolean ssl) throws UnknownHostException, IOException
 	{
 		return MewlerImpl.create(this, host, port, ssl);
 	}
 
 	/**
 	 * <p>Runs the bot</p>
 	 * <p>Not that this function will block until {@link #shutdown()} is called;
 	 * thus is it recommend to run the bot in a new thread:
 	 * <pre>    new Thread(InternetRelayCat).start();</pre></p>
 	 */
 	@Override
 	public synchronized void run()
 	{
 		// FIXME: This will no longer work due to re-connection ability
 		// TODO: Migrate this code to a Thead object, rather than runnable?
 		if (bot != null) return; // Prevents re-running
 
 		this.bot = connect();
 		if (bot == null)
 		{
 			shutdown();
 			return;
 		}
 		log.log(Level.INFO, "Operations Running!");
 
 		try
 		{
 			wait();
 		}
 		catch (InterruptedException ex)
 		{
 			shutdown();
 		}
 
 		bot.quit();
 		bot.dispose();
 	}
 
 	/**
 	 * <p>Unblocks a call to {@link #run() run}, causing the bot to exit</p>
 	 */
 	public synchronized void shutdown()
 	{
 		synchronized(srvs)
 		{
 			setDispose(true);
 			for (Service s : srvs) s.shutdown();
 		}
 		notifyAll(); // run() waits to stop thread being killed; exits when notified
 	}
 
 	@Override
 	public void message(String target, String message)
 	{
 		if (target == null || target.length() == 0) throw new IllegalArgumentException("Invalid target: null or empty string");
 		if (message == null || message.length() == 0) return;
 
 		OutboundMessage o = new OutboundMessage(target, message, false);
 
 		for (FilterService f : fsrvs)
 		{
 			o = f.filter(o);
 			if (o == null) return;
 			if (o.getTarget() == null || o.getTarget().length() == 0) return;
 			if (o.getMessage() == null || o.getMessage().length() == 0) return;
 		}
 
 		bot.message(o.getTarget(), o.getMessage());
 	}
 
 	@Override
 	public void act(String target, String action)
 	{
 		if (target == null || target.length() == 0) throw new IllegalArgumentException("Invalid target: null or empty string");
 		if (action == null || action.length() == 0) return;
 		OutboundMessage o = new OutboundMessage(target, action, true);
 
 		for (FilterService f : fsrvs)
 		{
 			o = f.filter(o);
 			if (o == null) return;
 			if (o.getTarget() == null || o.getTarget().length() == 0) return;
 			if (o.getMessage() == null || o.getMessage().length() == 0) return;
 		}
 
 		bot.act(o.getTarget(), o.getMessage());
 	}
 
 	@Override
 	public void join(String channel)
 	{
 		bot.join(channel);
 	}
 
 	@Override
 	public void leave(String channel)
 	{
 		bot.part(channel);
 	}
 
 	@Override
 	public String getNick()
 	{
 		if (bot == null) return null;
 		return bot.getNick();
 	}
 
 	@Override
 	public String[] names(String channel)
 	{
 		return new String[]{};// bot.getUsers(channel); TODO: Fixme
 	}
 
 	@Override
 	public String[] channels()
 	{
 		return new String[]{}; // bot.getChannels(); TODO: Fixme
 	}
 
 	@Override
 	public boolean isConnected()
 	{
 		return (bot != null);
 	}
 
 	/**
 	 * <p>Flag to denote that the bot is currently exiting</p>
 	 * @return the dispose
 	 */
 	protected boolean isDispose()
 	{
 		return dispose;
 	}
 
 	/**
 	 * <p>Flag to denote that the bot is currently exiting</p>
 	 * @param dispose the dispose to set
 	 */
 	protected void setDispose(boolean dispose)
 	{
 		this.dispose = dispose;
 	}
 
 	/**
 	 * <p>The list of currently activated {@link Service Services}</p>
 	 * @return the srvs
 	 */
 	protected Iterable<Service> getSrvs()
 	{
 		return srvs;
 	}
 
 	/**
 	 * <p>The list of currently activated {@link MessageService MessageServices}</p>
 	 * @return the msrvs
 	 */
 	protected Iterable<MessageService> getMsrvs()
 	{
 		return msrvs;
 	}
 
 	protected Iterable<FilterService> getFsrvs()
 	{
 		return fsrvs;
 	}
 
 	void onDisconnect()
 	{
 		synchronized (botLock)
 		{
 			if (bot == null) return;
 			bot = null;
 		}
 
 		log.log(Level.WARNING, "Disconnected from server.");
 		// TODO: Add controls for reconnection
 
 		if (isDispose()) return;
 
 		// FIXME: This is a mess.
 		new Thread("Reconnect Thread") {
 
 			@Override
 			public void run()
 			{
 				log.log(Level.WARNING, "Waiting 30s for reconnect.");
 				try
 				{
 					Thread.sleep(30000);
 				}
 				catch (InterruptedException ex)
 				{
 					throw new RuntimeException(ex);
 				}
 
 				if (isDispose()) return;
 				synchronized (botLock)
 				{
 					if (bot != null) return;
 					bot = connect();
 					// See if we actually achieved a connection
 					if (bot == null)
 						onDisconnect();
 				}
 			}
 		}.start();
 	}
 
 	private final Object connlock = new Object();
 
 	protected MewlerImpl connect()
 	{
 		MewlerImpl newbot;
 		try
 		{
 			log.log(Level.INFO, "Connecting to ''{0}''", host);
 			newbot = createBot(host, port, ssl);
 			newbot.connect(name, "", name);
 			for (String channel : channels)
 			{
 				log.log(Level.INFO, "Joining ''{0}''", channel);
 				newbot.join(channel);
 			}
 		}
 		catch (IOException ex)
 		{
 			log.log(Level.SEVERE, null, ex);
 			return null;
 		}
 
 		synchronized (connlock)
 		{
 			connlock.notifyAll();
 		}
 
 		return newbot;
 	}
 
 	// ~TODO: Consider this for being added to RelayCat
 	public void waitForConnection() throws InterruptedException
 	{
 		synchronized (connlock)
 		{
 			if (isConnected()) return;
 			connlock.wait();
 		}
 	}
 }
