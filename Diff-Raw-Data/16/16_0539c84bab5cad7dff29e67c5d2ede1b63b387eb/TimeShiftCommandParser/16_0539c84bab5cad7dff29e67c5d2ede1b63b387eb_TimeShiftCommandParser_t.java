 package tazzernator.cjc.timeshift;
 
 import java.util.ArrayList;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 //import org.bukkit.Server;
 import org.bukkit.World;
 
 public class TimeShiftCommandParser  { //extends PlayerListener
 	private TimeShift instance;
 	//show up in a lot of places. Actual string only shows up here. Some places still need adjustment
 	private final String cmd = "shift";
 	public static final String cmdPerm = "timeshift.shift"; // currently used in Player Listener
 	private final String cmdStart = "timeshift.startup";
 	ArrayList<String> data = new ArrayList<String>();
 
 	
 	public TimeShiftCommandParser(TimeShift instance) {
 		this.instance = instance;
 	}
 	
 	//---------------------- setting persistent settings
 
 	private void setPersist(int setting, Player player, String[] split) {
 
 		if (split.length > 2) {
 			for (int i = 2; i < split.length; i++) {
 				World w = this.instance.getServer().getWorld(split[i]);
 				if (w != null) {
 					TimeShiftFileReaderWriter.persistentWriter(setting, w);
 					printPersist(setting, player);
 				} else {
 					player.sendMessage("The world " + split[i] + " doesn't exist.");
 				}
 			}
 		} else {
 			TimeShiftFileReaderWriter.persistentWriter(setting, player.getWorld());
 			printPersist(setting, player);
 		}
 	}
 	
 	private void printPersist(int setting, Player player) {
 		if (setting == 0) {
 			player.sendMessage("Server will loop day on startup");
 		} else if (setting == -1) {
 			player.sendMessage("Server will not loop on startup");
 		} else {
 			player.sendMessage("Server will loop night on startup");
 		}
 	}
 	
 	// ------------------ setting temporary settings
 
 	private void setSetting(int setting, Player player, String[] split) {
 		if (split.length > 1) {
 			for (int i = 1; i < split.length; i++) {
 				if (setSettingByName(setting, split[i])) {
 					instance.getServer().broadcastMessage("The time suddenly " + cmd + "s on [" + split[i] + "]!");
 				} else {
 					player.sendMessage("The world " + split[i] + " doesn't exist.");
 				}
 			}
 		} else {
 			instance.getServer().broadcastMessage("The time suddenly " + cmd + "s on [" + player.getWorld().getName() + "]!");
 			setSettingPlayer(setting, player);
 		}
 	}
 	
 	// changes one of the servers in memory.
 	private void setSettingPlayer(int setting, Player player) {
 		World w = instance.getServer().getWorld(player.getWorld().getName());
 		//if the previous value at key was null, the world never got a timer.
 		if (TimeShift.settings.put(w.getName(), setting) == null) {
 			instance.scheduleTimer(w);
 		}
 	}
 	
 	
 	//same, by worldname instead of by player.
 	private Boolean setSettingByName(int setting, String world) {
 		World w = this.instance.getServer().getWorld(world);
 		if (w == null) {
 			return false;
 		} else {
 			// if the previous value at key was null, the world never got a
 			// timer.
 			if (TimeShift.settings.put(w.getName(), setting) == null) {
 				instance.scheduleTimer(w);
 			}
 			return true;
 		}
 	}
 
 	
 	//checks if a player has permission to use permission.
 	public boolean checkPermissions(Player p, String permission) {
 		if (TimeShift.Permissions != null) {
 			if (!TimeShift.Permissions.has(p, permission)) {
 				return false;
 			}
 		}		
 		return true;
 	}
 	
 	public boolean checkShift(Player player){
 		if (checkPermissions(player, cmdPerm)) return true;	
 		return false;
 	}
 	
 	private boolean checkShiftError(Player player) {
 		if (checkShift(player)) return false;	
 		player.sendMessage("You need " + cmdPerm + " permission.");
 		return true;
 	}
 	
 	public boolean checkStartup(Player player){
 		if (checkPermissions(player, cmdStart)) return true;	
 		return false;
 	}
 	//feb 17th and 18th
 	//reworked to be 'handleCommand' and to not handle /time, only /shift commands, which are claimed in yml.
 	//this has caused this class to part into a commandparser and a playerlistener.
 	//the player listener handles only the /time command, and hopefully does so in a way that does not
 	//interfere with other plugins. It may not be triggered still if another plugin claims /time.
 	
 	//also, general fixes.
 	//moved permission checking into a function, simplified setSetting, simplified world-finding
 	//changed from vectors to mapping world names to (int) values.
 	
 	// feb. 5 2011
 	// mostly rewritten to do switch/case instead of if/else
 	// also added new functionality:
 	// --optional Permissions support
 	// --Ability to set an on server-load/server-reload default
 	
 	public boolean handleCommand(CommandSender sender, Command command, String commandLabel, String[] split) throws NullPointerException,ArrayIndexOutOfBoundsException {
 	//get basic info about 
         String commandName = command.getName().toLowerCase();
 	//	String[] split = args;
         //cast sender to player, check if it 'worked' and return if it didn't.
 		Player player;
 		if (sender instanceof Player) {
 			player = (Player) sender;
 		} else {
 			System.out.println("Do you want to be able to use " + TimeShift.name + " commands from the console? Ask cjc. Maybe.");
 			System.out.println("You would have to specify a world then...");
 			return false;
 		}
 
 		//check that user has one of the permissions at least.
 		if (checkShift(player) || checkStartup(player)) {
 		
 		/*
 		 * Depending on the player's command, the number in the temp file is
 		 * changed. The number is checked by the Time class every second or so
 		 * to determine what the time should be in the server.
 		 */
 		//
 		if (commandName.equals(cmd)) {
 			// shift command used, only checking for future ability to. right now, it'd be assumed.
 
 			if (split.length == 0) {
 				//if it didn't have args, it's a help request. check what permission level they have and display customized permissions.
 				//if permissions isn't installed, checkPermissions returns true, so users will see "full help"
 				if (checkStartup(player) && checkShift(player)) {
 					//has both
 					player.sendMessage("Usage: /" + cmd + " day | night | stop | startup <x>");
 				} else if (checkShift(player)) {
 					//has shift only
 					player.sendMessage("Usage: /" + cmd + " day | night | stop");
 				} else {
 					//has startup only
 					player.sendMessage("Usage: /" + cmd + " startup [day | night | stop] -- sets startup and /reload behavior only.");
 				}
 				//command handled
 				return true;
 			}
 			
 			//length was at least 1.
 			//match a string to enum.
 			TierOne T1 = null;
 			try {
 				// try to match first argument to argument list
 				T1 = asTierOne(split[0]);
 				if (T1 == null) {
 					//this means a false 1st argument was used.
 					//returning false displays the "full" help from the uml file.
 					//the /time command is not included under its own command so that bukkit won't touch it.
 					return false;
 				}
 			} catch (NullPointerException ex) {
 			}
 			switch (T1) {
 			//self explanatory
 			case DAY:
 				if (checkShiftError(player)) return true; // displays a "need permission" message. May be a good thing to log too... if anyone cared...
 				setSetting(0,player, split);
 				return true;
 			case NIGHT:
 				if (checkShiftError(player)) return true;
 				setSetting(13800,player, split);
 				instance.getServer().broadcastMessage("The time suddenly " + cmd + "s!");
 				return true;
 			case STOP:
 				if (checkShiftError(player)) return true;
 				setSetting(-1,player, split);
 				instance.getServer().broadcastMessage("Time appears to be back to normal...");
 				return true;
 			case STARTUP:
 				// if case is startup, check for permissions.
 				if (!checkStartup(player)) {
 					player.sendMessage("You need " + cmdStart + " permission.");
 					return true;	
 				}
 				//only got the startup command
 				if (split.length == 1) {
					player.sendMessage("Startup Usage: /" + cmd + " startup [day | night | stop]\n/" + cmd + " startup day -- automatically loop day when the server\nis /reload 'ed or restarted.");
 					return true;
 				}
 				//trying to get a string to match enum value, null if not matched
 				TierOne T2 = null;
 				try {
 					T2 = asTierOne(split[1]);
 					if (T2 == null) {
 						//improper usage, displays full help
 						return false;
 					}
 				} catch (NullPointerException ex) {
 				}
 				//self explanatory.
 				//add multi-world-persistent-set.
 				//already added for setSetting
 				//now add for persistentWriter
 				switch (T2) {
 				case DAY:
 					setPersist(0, player, split);
 					return true;
 				case NIGHT:
 					setPersist(13800, player, split);
 					return true;
 				case STOP:
 					setPersist(-1, player, split);
 					return true;
 				case STARTUP:
 					player.sendMessage("Why would you even want to try that? Are you trying to make the universe implode?");
 					return false;
 				default:
 					break;
 				}
 				break;
 			default:
 				break;
 			}
 			//this return should never be reached
 			return false;
 		} 
 		
 	} else {
 		//has neither permission
 		player.sendMessage("You don't have permission to use /" + cmd + ".");
 		return true;
 	}
 		//never returned true, also shouldn't be reached. may be reached if the user changes the yml file. 
 		return false;
 	}
 
 	//should be renamed, along with the enum. Provides an enum value that matches a string, if possible. If not possible, returns null.
 	//function must then check to see if it got null, in which case, string wasn't in enum.
 	private static TierOne asTierOne(String str) {
 		for (TierOne t1 : TierOne.values()) {
 			if (t1.name().equalsIgnoreCase(str)) {
 				return t1;
 			}
 		}
 		return null;
 	}
 	
 	//actually kinda TierAll. 2nd tier is startup, and I just parsed the extra.
 	private enum TierOne {
 		DAY, NIGHT, STOP, STARTUP
 	}
 
 }
