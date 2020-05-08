 package uk.co.harcourtprogramming.internetrelaycats;
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.List;
 import java.util.logging.ConsoleHandler;
 import java.util.logging.Formatter;
 import java.util.logging.Handler;
 import java.util.logging.Logger;
 import java.util.logging.LogRecord;
 import java.util.logging.Level;
 import java.io.IOException;
 import org.jibble.pircbot.Colors;
 import org.jibble.pircbot.PircBot;
 import org.jibble.pircbot.IrcException;
 import org.jibble.pircbot.User;
 
 public class RelayCat implements Runnable, IRelayCat
 {
 
 	@SuppressWarnings("PublicInnerClass")
 	public class Message implements IRelayCat
 	{
 		private final String message;
 		private final String nick;
 		private final String me = RelayCat.this.bot.getNick();
 		private final String channel;
 		private final boolean action;
 		private boolean dispose = false;
 
 		private Message(String message, String nick, String channel, boolean action)
 		{
 			this.message = Colors.removeFormattingAndColors(message);
 			this.nick = nick;
 			this.channel = channel;
 			this.action = action;
 		}
 
 		public boolean isAction()
 		{
 			return action;
 		}
 
 		public String getMessage()
 		{
 			return message;
 		}
 
 		public String getChannel()
 		{
 			return channel;
 		}
 
 		public String getSender()
 		{
 			return nick;
 		}
 
 		@Override
 		public String getNick()
 		{
 			return me;
 		}
 
 		public synchronized void reply(String message)
 		{
 			RelayCat.this.message(nick, message);
 		}
 
 		public synchronized void act(String action)
 		{
 			if (action == null || action.length() == 0) return;
 			final String target = (this.channel == null ? this.nick : this.channel);
 			RelayCat.this.bot.sendAction(target, action);
 		}
 
 		public synchronized void replyToAll(String message)
 		{
 			if (channel == null)
 			{
 				RelayCat.this.message(nick, message);
 			}
 			else
 			{
 				RelayCat.this.message(channel, message);
 			}
 		}
 
 		public void dispose()
 		{
 			dispose = true;
 		}
 
 		@Override
 		public void message(String target, String message)
 		{
 			if (dispose) return;
 			RelayCat.this.message(target, message);
 		}
 
 		@Override
 		public void act(String target, String message)
 		{
 			if (dispose) return;
 			RelayCat.this.act(target, nick);
 		}
 
 		@Override
 		public void join(String channel)
 		{
 			if (dispose) return;
 			RelayCat.this.join(channel);
 		}
 
 		@Override
 		public void leave(String channel)
 		{
 			if (dispose) return;
 			RelayCat.this.leave(channel);
 		}
 
 		@Override
 		public User[] names(String channel)
 		{
 			return RelayCat.this.names(channel);
 		}
 
 		@Override
 		public String[] channels()
 		{
 			return RelayCat.this.channels();
 		}
 	}
 
 	@SuppressWarnings("ProtectedInnerClass")
 	protected class CatBot extends PircBot
 	{
 		protected CatBot(String name)
 		{
 			this.setName(name);
 		}
 
 		@SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
 		public void onInput(boolean action, String sender, String channel, String data)
 		{
 			log.log(Level.FINE, "Input recieved from {0} (channel {1})",
 			    new Object[] {sender, channel});
 
 			final Message m = new Message(data, sender, channel, action);
 
 			synchronized(srvs)
 			{
 				for (MessageService s : msrvs)
 				{
					log.log(Level.INFO, "Input dispatched to {0}", s.toString());
 					try
 					{
 						s.handle(m);
 					}
 					catch (Throwable ex)
 					{
 						log.log(Level.SEVERE, "Error whilst passing input to " + s.toString(), ex);
 					}
 					if (m.dispose) break;
 				}
 			}
 		}
 
 		@Override
 		public void onMessage(String channel, String sender, String login, String hostname, String message)
 		{
 			onInput(false, sender, channel, message);
 		}
 
 		@Override
 		public void onPrivateMessage(String sender, String login, String hostname, String message)
 		{
 			onInput(false, sender, null, message);
 		}
 
 		@Override
 		public void onAction(String sender, String login, String hostname, String target, String action)
 		{
 			onInput(true, sender, (target.equals(getNick()) ? null : target), action);
 		}
 	}
 
 	private final static Logger log = Logger.getLogger("InternetRelayCat");
 	private final static Formatter form = new Formatter()
 	{
 		@Override
 		public String format(LogRecord l)
 		{
 			Calendar time = Calendar.getInstance();
 			time.setTimeInMillis(l.getMillis());
 
 			return String.format("[%2$tR %1$s] %3$s\n",
 			    l.getLevel().getLocalizedName(), time, formatMessage(l));
 		}
 	};
 	static
 	{
 		Handler h = new ConsoleHandler();
 		h.setFormatter(form);
 		log.addHandler(h);
 		log.setUseParentHandlers(false);
 	}
 
 	private final String host;
 	private final List<String> channels;
 	private final List<Service> srvs = new ArrayList<Service>();
 	private final List<MessageService> msrvs = new ArrayList<MessageService>();
 	private boolean dispose = false;
 
 	private final CatBot bot;
 
 	public RelayCat(final String name, final String host, final List<String> channels)
 	{
 		if (name==null || name.length()==0) throw new IllegalArgumentException("Name must be a non-empty String");
 		if (host==null) throw new IllegalArgumentException("Host must be supplied");
 
 		bot = new CatBot(name);
 
 		this.host = host;
 
 		if (channels == null)
 		{
 			this.channels = new ArrayList<String>(0);
 		}
 		else
 		{
 			this.channels = channels;
 		}
 		bot.setVerbose(false);
 	}
 
 	public void addService(Service s)
 	{
 		synchronized(srvs)
 		{
 			if (dispose) return;
 			log.log(Level.INFO, "Service Loaded: {0}@{1}",
 			    new Object[]{s.getClass().getSimpleName(), s.getId()});
 			srvs.add(s);
 			if (s instanceof MessageService)
 			{
 				msrvs.add((MessageService)s);
 				log.log(Level.INFO, "Service {0}@{1} loaded as MessageService.",
 				    new Object[]{s.getClass().getSimpleName(), s.getId()});
 			}
 			if (s instanceof ExternalService)
 			{
 				final ExternalService es = (ExternalService)s;
 				es.setInstance(this);
 				es.getThread().start();
 			}
 		}
 	}
 
 	@Override
 	public synchronized void run()
 	{
 		try
 		{
 			log.log(Level.INFO, "Connecting to ''{0}''", host);
 			bot.connect(host);
 			for (String channel : channels)
 			{
 				log.log(Level.INFO, "Joining ''{0}''", channel);
 				bot.joinChannel(channel);
 			}
 			log.log(Level.INFO, "Operations Running!");
 			wait();
 		}
 		catch (IOException ex)
 		{
 			log.log(Level.SEVERE, null, ex);
 		}
 		catch (IrcException ex)
 		{
 			log.log(Level.SEVERE, null, ex);
 		}
 		catch (InterruptedException ex)
 		{
 		}
 
 		// Shutdown procedure :)
 		synchronized(srvs)
 		{
 			dispose = true;
 			for (Service s : srvs) s.shutdown();
 		}
 		bot.quitServer();
 		bot.disconnect();
 		bot.dispose();
 	}
 
 	public synchronized void shutdown()
 	{
 		notifyAll(); // run() waits to stop thread being killed; exits when notified
 	}
 
 	private final Object transmissionLock = new Object();
 
 	@Override
 	public void message(String target, String message)
 	{
 		if (target == null || target.length() == 0) throw new IllegalArgumentException("Invalid target: null or empty string");
 		if (message == null || message.length() == 0) return;
 		synchronized (transmissionLock)
 		{
 			for (String line : message.split("\n"))
 			{
 				bot.sendMessage(target, line);
 			}
 		}
 	}
 
 	@Override
 	public void act(String target, String action)
 	{
 		if (target == null || target.length() == 0) throw new IllegalArgumentException("Invalid target: null or empty string");
 		if (action == null || action.length() == 0) return;
 		synchronized (transmissionLock)
 		{
 			bot.sendAction(target, action);
 		}
 
 	}
 
 	@Override
 	public void join(String channel)
 	{
 		bot.joinChannel(channel);
 	}
 
 	@Override
 	public void leave(String channel)
 	{
 		bot.partChannel(channel);
 	}
 
 	@Override
 	public String getNick()
 	{
 		return bot.getNick();
 	}
 
 	@Override
 	public User[] names(String channel)
 	{
 		return bot.getUsers(channel);
 	}
 
 	@Override
 	public String[] channels()
 	{
 		return bot.getChannels();
 	}
 }
 
