 package tazzernator.cjc.timeshift;
 
 //java imports
 //import java.io.File;
 import java.util.ArrayList;
 import java.util.Timer;
 //import java.util.Map;
 import java.util.HashMap;
 //import java.util.;
 
 //bukkit imports
 //import org.bukkit.Server; 
 import org.bukkit.World;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginDescriptionFile;
 //import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 //import org.bukkit.
 
 //permissions-related imports
 import com.nijikokun.bukkit.Permissions.Permissions;
 import com.nijiko.permissions.PermissionHandler;
 import org.bukkit.plugin.Plugin; //req. by permissions
 
 //feb 18 cjc moved read/write into own class/out of all other classes and to use yml to claim /shift
 //also moved to new file name, but not to a yml file, just a world=setting
 
 /**
  * TimeShift for bukkit
  * 
  * @author Tazzernator (Andrew Tajsic)
  * 
  */
 // includes
 public class TimeShift extends JavaPlugin {
 	
 	
 	//public memory
 	//Strings should always use this name
 	public static String name = "TimeShift";
 	// for permissions implementation
 	public static PermissionHandler Permissions = null;
 	// store server settings in key=worldname, int setting
 	public static HashMap<String,Integer> settings = new HashMap<String,Integer>();// = new AbstractMap<String,Integer>();
 	//public static Vector<String,Integer> settings = new Vector<String,Integer>();
 	// store path to TimeShift.time
 	public static String path;
 	
 	//public static String path2;
 	
 	//private memory
 	private Timer tick = null;
 	private int rate = 1000;
 	private final TimeShiftCommandParser commandParser = new TimeShiftCommandParser(this);
 	private final TimeShiftPlayerListener tspl = new TimeShiftPlayerListener(this);
 	//holds temporary file input
 	static ArrayList<String> data = new ArrayList<String>();
 	
 	//onDisable
 	public void onDisable() {
 		//stop the timers
 		tick.cancel();
 	}
 	
 	//onEnable
 	public void onEnable() {
 		try {
 	//	System.out.println("Time Shift Path2 : " + path2);
 	//	getConfiguration().
 		if (this.getDataFolder().exists()) {
 			path = this.getDataFolder().getPath() + "/" + name + ".startup";
 		} else {
 			if (this.getDataFolder().mkdirs()) {
 			path = this.getDataFolder().getPath() + "/" + name + ".startup";
 			} else {
 				System.out.println(name + " could not create necessary folder structure for settings.");
 			}
 		}
 		
 		//read file
 		TimeShiftFileReaderWriter.readSettings();
 		
 		setupPermissions();//setup permissions
 		
 		// Lets start the Timer instance.
 		//start one instance for each world
 		tick = new Timer(true);
 		for (World w : getServer().getWorlds()) {
 			scheduleTimer(w);
 		}
 		// Register our events
 		//this event only controls /time, nothing else. It attempts to use any /time commands to cancel an active shift
 		//without disrupting the command used. If other plugins don't use this method, we may not see /time commands.
 		PluginManager pm = getServer().getPluginManager();
		pm.registerEvent(Event.Type.PLAYER_COMMAND, tspl, Priority.Normal, this);
 
 		// Here we just output some info so we can check all is well
 		PluginDescriptionFile pdfFile = this.getDescription();
 		System.out.println(pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!");
 		} catch (Exception e) {
 			System.out.println("Exception thrown in onEnable " + name);
 			e.printStackTrace();
 		}
 	}
 	
 	public void scheduleTimer(World w) {
 		TimeShiftTimer tst = new TimeShiftTimer();
 		tst.world = w;
 		tst.index = w.getName();
 		tick.schedule(tst, 0, rate);
 	}
 	
 	
 	@Override
     public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args) {
      //   passes off the work of received commands to the class that was once built for that.
 		// this doesn't intercept the /time command, just the /shift commands.
 		// (right now, someone could have changed that) /time is handled by player events still
         return commandParser.handleCommand(sender, command, commandLabel, args);
     }
 
 	
 	//new setupPermissions courtesy of Acru
 	//http://forums.bukkit.org/posts/79813/
 	//changed this to TimeShift
 	private void setupPermissions() {
         Plugin test = this.getServer().getPluginManager().getPlugin("Permissions");
         if (TimeShift.Permissions == null) {
             if (test != null) {
                 this.getServer().getPluginManager().enablePlugin(test); // This line.
                 TimeShift.Permissions = ((Permissions)test).getHandler();
             }
         }
     }//modified setup method from Permissions thread by Niji
 }
