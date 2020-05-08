 package net.erbros.Lottery;
 
 import java.util.ArrayList;
 import java.util.Timer;
 import java.util.TimerTask;
 import java.util.logging.Logger;
 
 import net.erbros.Lottery.register.payment.Method;
 import net.erbros.Lottery.register.payment.Methods;
 
 import org.bukkit.Bukkit;
 import org.bukkit.configuration.file.FileConfiguration;
 import org.bukkit.event.Event;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class Lottery extends JavaPlugin {
 
 	protected static Integer cost;
 	protected Integer hours;
 	protected static Long nextexec;
 	public Method Method = null;
     public Methods Methods = null;
 	public Boolean timerStarted = false;
 	protected static Boolean useiConomy;
 	protected static Integer material;
 	protected Integer extraInPot;
 	protected Boolean broadcastBuying;
 	protected Boolean welcomeMessage;
 	protected Integer netPayout;
 	protected Boolean clearExtraInPot;
 	protected Integer maxTicketsEachUser;
 	protected Integer numberOfTicketsAvailable;
 	protected Integer jackpot;
     protected ArrayList<String> msgWelcome;
     public FileConfiguration config;
 	// Starting timer we are going to use for scheduling.
 	Timer timer;
 
 	private static PlayerJoinListener PlayerListener = null;
 	public PluginDescriptionFile info = null;
 	protected static org.bukkit.Server server = null;
 	protected MainCommandExecutor MainCommand = null;
 	public Etc etc;
 
 	// Doing some logging. Thanks cyklo
 	protected static final Logger log = Logger.getLogger("Minecraft");;
 
 	@Override
 	public void onDisable() {
 		// Disable all running timers.
 		Bukkit.getServer().getScheduler().cancelTasks(this);
 		info = null;
 
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version "
 				+ pdfFile.getVersion() + " has been unloaded.");
 		etc.debugMsg(getDescription().getName()
 				+ ": has been disabled (including timers).");
 	}
 
 	@Override
 	public void onEnable() {
 		
 
     // Gets version number and writes out starting line to console.
 	    PluginDescriptionFile pdfFile = this.getDescription();
 	    System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled");
 	    
 	    etc = new Etc (this);
 		// Lets find some configs
        log.info("onEnable1");
 		config = getConfig();
 	  
 		config.options().copyDefaults(true);
 		saveConfig();
		
        log.info("ticketsSold(): " + String.valueOf(etc.ticketsSold()));
        log.info("onEnable2");
 		etc.loadConfig();
 		
 		
 		server = getServer();
 		// Do we need iConomy?
 		if (useiConomy == true) {
 			// Event Registration
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLUGIN_ENABLE, new PluginListener(this),
 					Priority.Monitor, this);
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLUGIN_DISABLE, new PluginListener(this),
 					Priority.Monitor, this);
 		}
 		if (welcomeMessage == true) {
 			PlayerListener = new PlayerJoinListener(this);
 			getServer().getPluginManager().registerEvent(
 					Event.Type.PLAYER_JOIN, PlayerListener, Priority.Monitor,
 					this);
 		}
 
 		// Listen for some player interaction perhaps? Thanks to cyklo :)
 
         getCommand("lottery").setExecutor(MainCommand);
 
 		// Is the date we are going to draw the lottery set? If not, we should
 		// do it.
 		if (nextexec == 0) {
 
 			// Set first time to be config hours later? Millisecs, * 1000.
 			nextexec = System.currentTimeMillis() + extendTime();
 			config.set("config.nextexec", nextexec);
 
 			saveConfig();
 		} else {
 			nextexec = config.getLong("config.nextexec");
 		}
 
 		// Start the timer for the first time.
 		StartTimerSchedule(false);
 
 		// This could, and should, probably be fixed nicer, but for now it'll
 		// have to do.
 		// Adding timer that waits the time between nextexec and time now.
 
 	}
 
 	public static org.bukkit.Server getBukkitServer() {
 		return server;
 	}
 
 
 	void StartTimerSchedule(boolean drawAtOnce) {
 
 		long extendtime = 0;
 		// Cancel any existing timers.
 		if (timerStarted == true) {
 			// Let's try and stop any running threads.
 			try {
 				Bukkit.getServer().getScheduler().cancelTasks((Plugin) this);
 			} catch (ClassCastException exception) {
 			}
 			;
 
 			extendtime = extendTime();
 		} else {
 			// Get time until lottery drawing.
 			extendtime = nextexec - System.currentTimeMillis();
 		}
 		// What if the admin changed the config to a shorter time? lets check,
 		// and if
 		// that is the case, lets use the new time.
 		if (System.currentTimeMillis() + extendTime() < nextexec) {
 			nextexec = System.currentTimeMillis() + extendTime();
 
 			config.set("config.nextexec", Lottery.nextexec);
 			saveConfig();
 		}
 
 		// If the time is passed (perhaps the server was offline?), draw lottery
 		// at once.
 		if (extendtime <= 0) {
 			extendtime = 1000;
 			etc.debugMsg("Seems we need to make a draw at once!");
 		}
 
 		// Is the drawAtOnce boolean set to true? In that case, do drawing in a
 		// few secs.
 		if (drawAtOnce) {
 			extendtime = 100;
 			config.set("config.nextexec", System.currentTimeMillis() + 100);
 			nextexec = System.currentTimeMillis() + 100;
 			etc.debugMsg("DRAW NOW");
 		}
 
 		// Delay in server ticks. 20 ticks = 1 second.
 		extendtime = extendtime / 1000 * 20;
 
 		checkWhatMethodToUse(extendtime);
 
 		// Timer is now started, let it know.
 		timerStarted = true;
 	}
 
 	class LotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			// Get new config.
 			etc.debugMsg("Doing a lottery draw");
 			
 			Lottery.nextexec = config.getLong("config.nextexec");
 
 			if (Lottery.nextexec > 0
 					&& System.currentTimeMillis() + 1000 >= Lottery.nextexec) {
 				// Get the winner, if any. And remove file so we are ready for
 				// new round.
 				etc.debugMsg("Getting winner.");
 				if (etc.getWinner() == false) {
 					etc.debugMsg("Failed getting winner");
 				}
 				Lottery.nextexec = System.currentTimeMillis() + extendTime();
 
 				config.set("config.nextexec", Lottery.nextexec);
 				saveConfig();
 			}
 			// Call a new timer.
 			StartTimerSchedule(false);
 		}
 	}
 
 	class extendLotteryDraw extends TimerTask {
 		public void run() {
 			// Cancel timer.
 			try {
 				Bukkit.getServer().getScheduler().cancelTasks((Plugin) this);
 			} catch (ClassCastException exception) {
 			}
 			;
 
 			nextexec = config.getLong("config.nextexec");
 
 			long extendtime = 0;
 
 			// How much time left? Below 0?
 			if (nextexec < System.currentTimeMillis()) {
 				extendtime = 3000;
 			} else {
 				extendtime = nextexec - System.currentTimeMillis();
 			}
 			// Delay in server ticks. 20 ticks = 1 second.
 			extendtime = extendtime / 1000 * 20;
 
 			checkWhatMethodToUse(extendtime);
 
 		}
 	}
 	
 
     void checkWhatMethodToUse(long extendtime) {
         // Is this very long until? On servers with lag and long between
         // restarts there might be a very long time between when server
         // should have drawn winner and when it will draw. Perhaps help the
         // server a bit by only scheduling for half the lengt at a time?
         // But only if its more than 5 seconds left.
         if (extendtime < 5 * 20) {
             Bukkit.getServer()
                     .getScheduler()
                     .scheduleAsyncDelayedTask((Plugin) this, new LotteryDraw(),
                             extendtime);
             etc.debugMsg("LotteryDraw() " + extendtime + 100);
         } else {
             extendtime = extendtime / 15;
             Bukkit.getServer()
                     .getScheduler()
                     .scheduleAsyncDelayedTask((Plugin) this,
                             new extendLotteryDraw(), extendtime);
             etc.debugMsg("extendLotteryDraw() " + extendtime);
         }
         // For bugtesting:
     }
     
 
     public long extendTime() {
         hours = config.getInt("config.hours");
         Long extendTime = Long.parseLong(hours.toString()) * 60 * 60 * 1000;
         etc.debugMsg("extendTime: " + extendTime);
         return extendTime;
     }
 
 }
