 package tazzernator.cjc.timeshift;
 
 import org.bukkit.World;
 import org.bukkit.entity.Player;
 import org.bukkit.event.player.PlayerListener;
 import org.bukkit.event.player.PlayerChatEvent;
 
 public class TimeShiftPlayerListener extends PlayerListener {
 	private TimeShift plugin;
 
 	
 	public TimeShiftPlayerListener(TimeShift instance) {
 		this.plugin = instance;
 	}
 	
 	private void setSetting(int setting, Player player) {
 		World w = plugin.getServer().getWorld(player.getWorld().getName());
 		TimeShift.settings.put(w.getName(), setting);
 	}
 	
 	//handles /time command only in order to 'peacefully' use it?
	public void onPlayerCommand(PlayerChatEvent event) {
 		String[] split = event.getMessage().split(" ");
 		Player player = event.getPlayer();
 		if (split[0].equalsIgnoreCase("/time")) {
 			// time command cancels an active shift only
 			// check for permission to (cancel a) shift
 			if (TimeShift.Permissions != null) {
 				if (!TimeShift.Permissions.has(player, TimeShiftCommandParser.cmdPerm)) {
 					return;
 				}
 			}		
 			World w = player.getWorld();
 			try {
 				// TST should fix before it is ever an issue?
 				if (TimeShift.settings.get(w.getName()) != -1) {
 					setSetting(-1, player);
 					plugin.getServer().broadcastMessage("Time appears to be back to normal...");
 				}
 			} catch (ArrayIndexOutOfBoundsException e) {
 				System.out.println(TimeShift.name + " had a minor error with the /time command. Please report.");
 			}
 			// don't catch time commands?
 			return;
 		}
 	}
 }
