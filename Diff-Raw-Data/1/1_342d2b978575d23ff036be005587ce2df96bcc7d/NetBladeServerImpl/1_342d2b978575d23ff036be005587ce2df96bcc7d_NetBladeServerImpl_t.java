 package de.infinityblade.netblade;
 
 import java.util.logging.*;
 import jline.console.ConsoleReader;
 import de.infinityblade.netblade.network.packets.*;
 import de.infinityblade.netblade.sql.SQL;
 import de.infinityblade.netblade.network.Client;
 import de.infinityblade.netblade.network.ConnectionManager;
 import de.infinityblade.netblade.network.ServerStatus;
 import de.infinityblade.netblade.scheduler.Scheduler;
 
 public abstract class NetBladeServerImpl implements NetBladeServer, Runnable
 {
 	protected Logger m_log;
 	protected SQL m_sql;
 	protected boolean m_isRunning = false;
 	protected ConsoleReader m_reader;
 	protected ServerConfiguration m_config;
 	public static float VERSION = 0.1f;
 	protected boolean m_isSqlEnabled = true;
 	protected ConnectionManager m_connectionManager;
 	protected ServerStatus m_status;
 	protected Scheduler m_scheduler;
 	protected Packet m_emptyPacket = new EmptyPacket();
 	protected long m_tickInterval = 50;
 
 	protected NetBladeServerImpl(ConnectionManager inManager)
 	{
 		NetBlade.setServer(this);
 		try
 		{
 			this.m_connectionManager = inManager;
 			this.m_reader = new ConsoleReader();
 			this.m_log = Logger.getLogger("NetBlade");
 			this.m_log.setUseParentHandlers(false);
 			ConsoleHandler handler = new JLineConsoleHandler(this.m_reader);
 			handler.setFormatter(new StripClassFormatter());
 			handler.setLevel(Level.ALL);
 			this.m_log.addHandler(handler);
 			FileHandler fileLog = new FileHandler("server.log", true);
 			fileLog.setFormatter(new StripClassFormatter());
 			fileLog.setLevel(Level.INFO);
 			this.m_log.addHandler(fileLog);
 			this.m_log.setLevel(Level.ALL);
 			this.m_connectionManager.setLogger(this.m_log);
 			LogManager.registerLogger(this.m_log);
 			this.m_config = new ServerConfiguration();
 			Runtime.getRuntime().addShutdownHook(new Thread()
 			{
 				public void run()
 				{
 					m_log.info("Server shutting down.");
 					try
 					{
 						if(m_sql != null)
 							m_sql.stop();
 
 						m_isRunning = false;
 						m_connectionManager.stop();
 					}
 					catch(Exception e)
 					{
 						e.printStackTrace();
 					}
 				}
 			});
 			this.m_scheduler = new Scheduler();
 		}
 		catch(Exception e)
 		{
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Client[] getClients()
 	{
 		return this.m_connectionManager.getClients();
 	}
 
 	@Override
 	public boolean load()
 	{
 		this.m_log.info("Starting up " + this.getClass().getSimpleName() + " version " + VERSION + ".");
 		this.m_connectionManager.start();
 		if(this.m_isSqlEnabled)
 		{
 			if(!this.startSQL())
 			{
 				this.shutdown();
 				return false;
 			}
 		}
 
 		return true;
 	}
 
 	@Override
 	public void start()
 	{
		this.m_isRunning = true;
 		if(this.m_isSqlEnabled)
 			new Thread(this.m_sql).start();
 
 		new Thread(this).start();
 		new Thread(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				NetBladeServerImpl.this.getConnectionManager().startListening();
 			}
 		}).start();
 		this.m_log.info("Server started.");
 		this.heart();
 	}
 
 	protected void heart()
 	{
 		long lastBeat = 0;
 		while(this.m_isRunning)
 		{
 			if(System.currentTimeMillis() - lastBeat >= this.m_tickInterval)
 			{
 				this.m_scheduler.tick();
 				this.onTick();
 			}
 		}
 	}
 
 	protected abstract void onTick();
 
 	protected abstract boolean startSQL();
 
 	public boolean isRunning()
 	{
 		return this.m_isRunning;
 	}
 
 	@Override
 	public int getPort()
 	{
 		return this.m_connectionManager.getPort();
 	}
 
 	@Override
 	public void setPort(int inPort)
 	{
 		this.m_connectionManager.setPort(inPort);
 	}
 
 	@Override
 	public Logger getLogger()
 	{
 		return this.m_log;
 	}
 
 	@Override
 	public SQL getSQLConnection()
 	{
 		if(!this.m_sql.isConnected() && this.m_isSqlEnabled)
 		{
 			this.m_log.fine("Lost connection to database.");
 			if(!this.m_sql.connect())
 				return null;
 		}
 		return this.m_sql;
 	}
 
 	protected void parseParameters(String[] args)
 	{
 		if(args.length < 2)
 			return;
 
 		for(int i = 0; i < args.length; i++)
 		{
 			if(!args[i].startsWith("-"))
 				continue;
 
 			StartupParameters parameter = StartupParameters.getByName(args[i].replace("-", ""));
 			if(parameter == null)
 				continue;
 
 			switch(parameter)
 			{
 				case LOG_LEVEL:
 					if(args.length <= i + 1)
 						break;
 
 					try
 					{
 						Level level = Level.parse(args[i + 1]);
 						this.m_log.setLevel(level);
 						this.m_log.config("Setting log level to " + level.toString());
 					}
 					catch(Exception e)
 					{
 						this.m_log.info("Unable to parse log level. Using default.");
 					}
 
 					break;
 				case FILE_LEVEL:
 					if(args.length <= i + 1)
 						break;
 
 					try
 					{
 						Level level = Level.parse(args[i + 1]);
 						for(Handler handler : this.m_log.getHandlers())
 						{
 							if(handler instanceof FileHandler)
 								handler.setLevel(level);
 						}
 
 						this.m_log.config("Setting file log level to " + level.toString());
 					}
 					catch(Exception e)
 					{
 						this.m_log.info("Unable to parse log level. Using default.");
 					}
 
 					break;
 				case MAX_CLIENTS:
 					if(args.length <= i + 1)
 						break;
 
 					try
 					{
 						int amount = Integer.parseInt(args[i + 1]);
 						this.m_connectionManager.setMaxClients(amount);
 					}
 					catch(Exception e)
 					{
 						this.m_log.info("Unable to parse client amount. Using default.");
 					}
 
 					break;
 				case PORT:
 					if(args.length <= i + 1)
 						break;
 
 					try
 					{
 						int port = Integer.parseInt(args[i + 1]);
 						if(port >  65535)
 						{
 							this.m_log.info("Specified port is too high. Using default.");
 							break;
 						}
 
 						this.setPort(port);
 					}
 					catch(Exception e)
 					{
 						this.m_log.info("Unable to parse port. Using default.");
 					}
 
 					break;
 			}
 		}
 	}
 
 	public void shutdown()
 	{
 		this.m_isRunning = false;
 		this.m_connectionManager.disconnectClients();
 		this.closeMysql();
 		this.m_connectionManager.stop();
 	}
 
 	public void closeMysql()
 	{
 		if(this.m_sql != null && this.m_sql.isConnected())
 			this.m_sql.stop();
 	}
 
 	public void dispatchCommand(String inCommand, String[] inArgs)
 	{
 		if(inCommand.equalsIgnoreCase("stop"))
 			this.shutdown();
 		else if(inCommand.equalsIgnoreCase("reload"))
 		{
 			this.reload();
 			this.m_log.info("Reload complete.");
 		}
 		else if(inCommand.equals("clear"))
 		{
 			try
 			{
 				this.m_reader.clearScreen();
 			}
 			catch(Exception e)
 			{
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public void run()
 	{
 		String line;
 		while(this.isRunning())
 		{
 			try
 			{
 				line = this.m_reader.readLine("> ");
 				this.m_log.fine("Dispatching command > " + line);
 				String[] split = (line.contains(" ") ? line.split(" ") : new String[] { line });
 				if(split.length == 1)
 					this.dispatchCommand(split[0], new String[0]);
 				else
 				{
 					String[] args = new String[split.length - 1];
 					System.arraycopy(split, 1, args, 0, args.length);
 					this.dispatchCommand(split[0], args);
 				}
 			}
 			catch(Exception e)
 			{
 				if(this.m_isRunning)
 					this.m_log.warning("Error while reading console input: " + e.getMessage());
 			}
 		}
 		this.m_log.fine("Stopped console input.");
 	}
 
 	public ServerConfiguration getConfig()
 	{
 		return this.m_config;
 	}
 
 	public void reload()
 	{
 		this.m_config.load();
 		if(this.m_isSqlEnabled)
 			this.m_sql.connect();
 	}
 
 	@Override
 	public ConnectionManager getConnectionManager()
 	{
 		return this.m_connectionManager;
 	}
 
 	@Override
 	public Client getClient(int inID)
 	{
 		return this.getConnectionManager().getClient(inID);
 	}
 
 	@Override
 	public Client[] getOnlineClients()
 	{
 		return this.getConnectionManager().getOnlineClients();
 	}
 
 	@Override
 	public boolean isFull()
 	{
 		return this.getOnlineClients().length == this.getMaxClients();
 	}
 
 	@Override
 	public int getFreeSpots()
 	{
 		return this.getMaxClients() - this.getOnlineClients().length;
 	}
 
 	@Override
 	public int getMaxClients()
 	{
 		return this.getConnectionManager().getMaxClients();
 	}
 
 	@Override
 	public void setStatus(ServerStatus inStatus)
 	{
 		if(inStatus != this.m_status)
 		{
 		    if((inStatus == ServerStatus.HEAVY_LOAD || inStatus == ServerStatus.FULL) && (this.m_status != ServerStatus.HEAVY_LOAD && this.m_status != ServerStatus.FULL))
 		        this.m_log.log(Level.parse("NOTICE"), "Server is under heavy load !");
 
 			this.m_log.fine("Server status went from " + this.m_status.name() + " to " + inStatus.name());
 			this.m_status = inStatus;
 		}
 	}
 
 	@Override
 	public ServerStatus getStatus()
 	{
 		return this.m_status;
 	}
 
 	@Override
 	public Scheduler getScheduler()
 	{
 	    return this.m_scheduler;
 	}
 
 	@Override
 	public Packet getEmptyPacket()
 	{
 		return this.m_emptyPacket;
 	}
 
 	public void setEncrypted(boolean inEncrypted)
 	{
 	    this.m_connectionManager.setEncrypted(inEncrypted);
 	}
 
 	public boolean isEncrypted()
 	{
 	    return this.m_connectionManager.isEncrypted();
 	}
 }
