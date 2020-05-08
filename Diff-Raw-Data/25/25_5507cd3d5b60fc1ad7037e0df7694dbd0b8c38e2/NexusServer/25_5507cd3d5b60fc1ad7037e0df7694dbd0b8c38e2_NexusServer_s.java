 package com.nexus;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.InetSocketAddress;
 
 import com.nexus.users.User;
 import com.nexus.webserver.WebServer;
 import com.nexus.websocket.WebsocketEngine;
 import com.nexus.client.*;
 import com.nexus.config.ConfigHelper;
 import com.nexus.playlist.PlaylistManager;
 import com.nexus.profiling.CPUProfilingCorePacket;
 import com.nexus.music.MusicDatabase;
 import com.nexus.scheduler.Scheduler;
 import com.nexus.audio.IndigoServer;
 import com.nexus.client.pushnotification.PushNotificationButtonManager;
 import com.nexus.social.SocialEngine;
 import com.nexus.profiling.Profiler;
 
 public class NexusServer {
 
 	public final String NexusVersion = "@NEXUSVERSION@"; // These variables get filled in by ant
 	public final String NexusBuild = "@NEXUSBUILD@";
 	public final String NexusBuilder = "@NEXUSBUILDER@";
 	
 	public static NexusServer Instance;
 
 	public AuthenticationManager AuthenticationManager;
 	public ClientManager ClientManager;
 	public PlaylistManager PlaylistManager;
 	public PushNotificationButtonManager PushNotificationButtonManager;
 	public MusicDatabase MusicDatabase;
 	public ConfigHelper Config;
 	public WebsocketEngine WebsocketEngine;
 	public Scheduler Scheduler;
 	public IndigoServer IndigoServer;
 	public SocialEngine SocialEngine;
 	public Profiler Profiler;
 	
 	public Log log;
 	public boolean IsDebug = false;
 	public User SystemUser;
 	
 	private int ProfilingTicks = 0;
 	
 	public Timer Timer;
 	
 	private boolean ShutdownRequested = false;
 	
 	public static void main(String[] args) {
 		Instance = new NexusServer();
 		Instance.RunServer(args);
 	}
 	
 	public void RunServer(String[] args){
 		Instance = this;
 		boolean ApplicationRunning = true;
 		
 		Runtime.getRuntime().addShutdownHook(new Thread(new ServerShutdownThread()));
 		
 		try{ //I configured eclipse to give a extra debug argument, if this is set, don't use the console colors!
 			for (String s: args) {
 	            if(s.equalsIgnoreCase("debug")){
 	            	IsDebug = true;
 	            }
 	        }
 		}catch(Exception e){}
 
 		while (ApplicationRunning) {
 			Instance = this;
 
 			this.Profiler = new Profiler();
 			Profiler.StartNexusStartup();
 			
 			this.log = new Log();
 			this.Config = new ConfigHelper("nexus.conf", this);
 
 			this.log.setLogLevel(Config.GetConfig().Nexus.LogLevel);
 			this.log.UseColors = !IsDebug;
 			
 			this.log.write("Starting NexusServer " + (this.NexusVersion.contains("NEXUSVERSION") ? "development version" : "version " + this.NexusVersion + ", " + this.NexusBuilder + " build #" + this.NexusBuild), LogLevel.INFO);
 			
 			this.AuthenticationManager = new AuthenticationManager();
 			this.ClientManager = new ClientManager(Instance);
 			this.PlaylistManager = new PlaylistManager(Instance);
 			this.PushNotificationButtonManager = new PushNotificationButtonManager();
 			this.MusicDatabase = new MusicDatabase(Instance);
 			this.Scheduler = new Scheduler(Instance);
 			this.Scheduler.Start();
 			this.SocialEngine = new SocialEngine(this.Config.GetConfig());
 			this.SocialEngine.Init();
 			this.WebsocketEngine = new WebsocketEngine(new InetSocketAddress(9002), Instance);
 			this.WebsocketEngine.start();
 			this.Timer = new Timer(40, Instance);
 			this.Timer.Start();
 			
 			MySQLHelper.Startup();
 			this.AuthenticationManager.KillAllTokens();
 			this.AuthenticationManager.UpdateTimestamps();
 			
 			//this.IndigoServer = new IndigoServer(Instance, "192.168.178.14",12345);
 			//this.IndigoServer.GetSendQueue().addToSendQueue("TEST1");
 			
 			this.SystemUser = new User();
 			this.SystemUser.Fullname = "Nexus";
 			this.SystemUser.Username = "System";
 			this.SystemUser.Email = "system@localhost";
 			this.SystemUser.CanReceiveNotify = false;
 			
 			File htdocslocation = new File("htdocs/");
 			if(!htdocslocation.exists() && !IsDebug){
 				log.write("Can\'t find htdocs, cloning from the repository", LogLevel.INFO);
 				Runtime rn = Runtime.getRuntime();
 				String[] s = new String[]{"git","clone","git://github.com/TeamNexus/nexus-webclient.git","htdocs"};
 				try{
 					rn.exec(s);
 				}catch (IOException e){}
 			}
 			
 			int port = Config.GetConfig().Nexus.Port;
 			
 			WebServer WebServer = new WebServer(port, log);
 			Thread WebServerThread = new Thread(WebServer);
 			log.write("Server starting...", "Webserver", LogLevel.INFO);
 			WebServerThread.start();
 			
 			Profiler.EndNexusStartup();
 
			try {
 				WebServerThread.join();
			} catch (InterruptedException e) { }
 
 			try{
 				AuthenticationManager.KillAllTokens();
 				Scheduler.Stop();
 				Timer.Stop();
				WebsocketEngine.KillAll();
 				WebsocketEngine.stop();
 				IndigoServer.kill();
 			}catch(Exception e){}
 
 			if(WebServer.requestedRestart()){
 				Config = null;
 				WebServer = null;
 				WebServerThread = null;
 				log.write("Restarting server...", "Webserver", LogLevel.INFO);
 				log = null;
 				AuthenticationManager = null;
 				ClientManager = null;
 				PlaylistManager = null;
 				MusicDatabase = null;
 				WebsocketEngine = null;
 				PushNotificationButtonManager = null;
 				Scheduler = null;
 				Timer = null;
 				IndigoServer = null;
 				System.gc(); //free up some memory
 			}else{
 				ShutdownRequested = true;
 				ApplicationRunning = false;
 			}
 		}
 
 		log.write("Server stopped.", "Webserver", LogLevel.INFO);
 	}
 	
 	public void OnTick(){
 		this.Profiler.StartTickProfiling();
 		Instance.ClientManager.OnTick();
 		Instance.PlaylistManager.OnTick();
 		Instance.AuthenticationManager.OnTick();
 		this.Profiler.OnTick();
 		
 		ProfilingTicks ++;
 		if(ProfilingTicks == 200){
 			CPUProfilingCorePacket Profiling = this.Profiler.GetCoreCPUProfilingData();
 			if((Profiling.RamUsed / Profiling.RamAllocated) * 100 >= 75){
 				System.gc();
 			}
 			ProfilingTicks = 0;
 		}
 		this.Profiler.EndTickProfiling();
 	}
 	
 	public void ShutdownServer(){
 		ShutdownRequested = true;
 		try{
 			Scheduler.Stop();
 			Timer.Stop();
 			WebsocketEngine.KillAll();
 			WebsocketEngine.stop();
 		}catch(Exception e){}
 		System.exit(0);
 	}
 	
 	public void OnUnexpectedServerShutdown(){
 		if(ShutdownRequested) return;
 		this.WebsocketEngine.KillAll();
 		try{
 			Scheduler.Stop();
 			Timer.Stop();
 			WebsocketEngine.KillAll();
 			WebsocketEngine.stop();
 		}catch(Exception e){}
 		log.write("Somebody's closing me!", LogLevel.SEVERE);
 	}
 	
 	public String GetLongServerVersionString(){
 		if(NexusBuild.contains("NEXUSBUILD")){
 			return "Devbuild";
 		}else{
 			return  "Version " + NexusVersion + " " + NexusBuilder + " build #" + NexusBuild;
 		}
 	}
 	
 	public String GetShortServerVersionString(){
 		if(NexusBuild.contains("NEXUSBUILD")){
 			return "Devbuild";
 		}else{
 			return  NexusBuilder + "/" + NexusVersion + " #" + NexusBuild;
 		}
 	}
 
 }
