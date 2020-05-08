 package com.nexus;
 
 import java.io.File;
 import java.io.PrintStream;
 import java.net.InetSocketAddress;
 import java.util.ArrayList;
 import java.util.logging.Level;
 
 import com.nexus.api.ApiFunctionLoader;
 import com.nexus.assets.AssetManager;
 import com.nexus.audio.IndigoServer;
 import com.nexus.classloading.NexusClassLoader;
 import com.nexus.client.ClientLoader;
 import com.nexus.client.ClientManager;
 import com.nexus.client.pushnotification.PushNotificationButtonManager;
 import com.nexus.clientupdater.ClientUpdater;
 import com.nexus.config.NexusConfig;
 import com.nexus.event.EventBus;
 import com.nexus.event.events.ServerEvent;
 import com.nexus.eventscheduler.EventScheduler;
 import com.nexus.indigo.IndigoFunctionLoader;
 import com.nexus.interfaces.IStaticLoader;
 import com.nexus.logging.NexusLog;
 import com.nexus.logging.NexusMainLogger;
 import com.nexus.mail.Mailer;
 import com.nexus.main.AuthenticationManager;
 import com.nexus.main.ConsoleColors;
 import com.nexus.main.EnumShutdownCause;
 import com.nexus.main.RamProfiler;
 import com.nexus.main.ServerShutdownThread;
 import com.nexus.main.ShutdownManager;
 import com.nexus.mysql.MySQLHelper;
 import com.nexus.notify.NexusNotify;
 import com.nexus.playlist.PlaylistManager;
 import com.nexus.profiling.Profiler;
 import com.nexus.scheduler.Scheduler;
 import com.nexus.sessions.SessionResumer;
 import com.nexus.social.SocialEngine;
 import com.nexus.time.SynchronisationHandler;
 import com.nexus.time.ticks.TickHandler;
 import com.nexus.time.ticks.Timer;
 import com.nexus.users.User;
 import com.nexus.users.UserPool;
 import com.nexus.webserver.WebServer;
 import com.nexus.webserver.WebServerHandlerLoader;
 import com.nexus.websocket.WebsocketEngine;
 
 /*
  * TODO:
  * 
  *  - Playlist refresh on open
  *  - Scheduled in next 24 hours
  * 
  */
 
 public class NexusServer{
 	
 	public static NexusServer Instance;
 	
 	public static final ArrayList<IStaticLoader> StaticLoaders = new ArrayList<IStaticLoader>();
 	
 	public final SynchronisationHandler TimeSynchronisationHandler = new SynchronisationHandler();
 	public final File ConfigDir = new File(System.getProperty("nexus.configDir", "config"));
 	
 	public SessionResumer SessionResumer;
 	
 	public AuthenticationManager AuthenticationManager;
 	public ClientManager ClientManager;
 	public PlaylistManager PlaylistManager;
 	public PushNotificationButtonManager PushNotificationButtonManager;
 	public WebsocketEngine WebsocketEngine;
 	public EventScheduler EventScheduler;
 	public IndigoServer IndigoServer;
 	public SocialEngine SocialEngine;
 	public Profiler Profiler;
 	public WebServer WebServer;
 	public Scheduler Scheduler;
 	public AssetManager AssetManager;
 	public NexusNotify Notify;
 	public UserPool UserPool;
 	
 	public static final EventBus EventBus = new EventBus();
 	
 	public User SystemUser;
 	
 	public NexusClassLoader ClassLoader;
 	
 	public Timer Timer;
 	
 	static{
 		StaticLoaders.add(new ApiFunctionLoader());
 		StaticLoaders.add(new ClientLoader());
 		StaticLoaders.add(new WebServerHandlerLoader());
 		StaticLoaders.add(new IndigoFunctionLoader());
 	}
 	
 	public static void main(){
 		Instance = new NexusServer();
 		Instance.RunServer();
 	}
 	
 	public NexusServer(){
 		NexusLog.info("Starting nexus-java server, version %s", NexusVersion.getVersion());
 		this.ClassLoader = Start.ClassLoader;
 	}
 	
 	public void RunServer(){
 		NexusServer.Instance = this;
 		
 		NexusConfig.load();
 		
 		Thread ShutdownHook = new Thread(new ServerShutdownThread());
 		ShutdownHook.setName("ShutdownHook");
 		Runtime.getRuntime().addShutdownHook(ShutdownHook);
 		
 		try{
 			this.TimeSynchronisationHandler.TimeSynchroniser.SynchroniseTime();
 			NexusLog.info("Using %s as a time synchroniser", this.TimeSynchronisationHandler.TimeSynchroniser.toString());
 		}catch(Exception e){
 			this.TimeSynchronisationHandler.FailedTimeSyncs++;
 		}
 		
 		while (!ShutdownManager.ShouldQuitMainLoop()){
 			try{
 				this.Profiler = new Profiler();
 				this.Profiler.StartNexusStartup();
 				
 				NexusMainLogger.SetMinLevel(NexusConfig.LOGGING_LEVEL);
 				NexusMainLogger.log(Level.OFF, "Console messages will be logged from level %s", NexusMainLogger.GetMinLevel().toString());
 				NexusMainLogger.log(Level.OFF, "Log file will log all messages");
 				
 				this.Timer = new Timer(40, Instance);
 				this.Timer.Start();
 
 				TickHandler.RegisterScheduledTickHandler(new RamProfiler());
 				TickHandler.RegisterScheduledTickHandler(this.TimeSynchronisationHandler);
 				
 				ClientUpdater.Init();
 				MySQLHelper.Startup();
 				Mailer.Init();
 				
 				this.LoadStaticLoaders();
 				
 				this.SessionResumer = new SessionResumer();
 				this.SessionResumer.InitAndResume();
 				this.SessionResumer.ResumeThread.join();
 				
 				this.UserPool.CacheUsers();
 				
 				EventBus.post(new ServerEvent.Init());
 				
 				this.AuthenticationManager.KillAllTokens();
 				this.AuthenticationManager.UpdateTimestamps();
 				
 				// this.IndigoServer = new IndigoServer(Instance, "192.168.178.14",12345);
 				// this.IndigoServer.GetSendQueue().addToSendQueue("TEST1");
 				
 				this.SystemUser = new User();
 				this.SystemUser.Fullname = "Nexus";
 				this.SystemUser.Username = "System";
 				this.SystemUser.Email = "system@localhost";
 				this.SystemUser.CanReceiveNotify = false;
 				
 				this.Notify.ReadConversationsFromDatabase();
 				
 				ClientUpdater.CheckExists();
 				ClientUpdater.CheckUpdate();
 				
 				this.WebServer = new WebServer(new InetSocketAddress(NexusConfig.WEBSERVER_PORT));
 				this.WebServer.Start();
 				
 				ShutdownManager.Start();
 				EventBus.post(new ServerEvent.Ready());
 				NexusLog.info("Nexus is ready to be used!");
 				
 				if(NexusMainLogger.GetMinLevel() == Level.OFF){
 					PrintStream out = NexusMainLogger.OutCache;
 					out.println(ConsoleColors.GREEN + "    _   _________  ____  _______");
 					out.println(ConsoleColors.GREEN + "   / | / / ____/ |/ / / / / ___/");
 					out.println(ConsoleColors.GREEN + "  /  |/ / __/  |   / / / /\\__ \\ ");
 					out.println(ConsoleColors.GREEN + " / /|  / /___ /   / /_/ /___/ / ");
 					out.println(ConsoleColors.GREEN + "/_/ |_/_____//_/|_\\____//____/  ");
 				}
 				
 				Profiler.EndNexusStartup();
 				
 				ShutdownManager.Join(); // Locks the main thread until the
 										// application shuts down or restarts
 				
 				Scheduler.Stop();
 				Timer.Stop();
				//IndigoServer.kill();
 				WebServer.Stop();
 				WebsocketEngine.KillAll(ShutdownManager.ShouldReboot() ? EnumShutdownCause.RESTART : EnumShutdownCause.SHUTDOWN);
 				Thread.sleep(1000);
 				WebsocketEngine.stop();
 				
 				NexusConfig.save();
 				
 				if(ShutdownManager.ShouldReboot()){
 					WebServer = null;
 					NexusLog.info("Restarting server...");
 					AuthenticationManager = null;
 					ClientManager = null;
 					PlaylistManager = null;
 					WebsocketEngine = null;
 					PushNotificationButtonManager = null;
 					Scheduler = null;
 					Timer = null;
 					IndigoServer = null;
 					System.gc();
 				}else{
 					ShutdownManager.RequestShutdown();
 				}
 			}catch(Exception e){
 				NexusLog.log(Level.SEVERE, e, "Error in main nexus loop");
 				ShutdownManager.ShutdownServer(EnumShutdownCause.CRASH);
 			}
 		}
 		
 		NexusLog.info("Server stopped.");
 	}
 	
 	public void LoadStaticLoaders(){
 		for(IStaticLoader loader : StaticLoaders){
 			loader.Load();
 		}
 	}
 }
