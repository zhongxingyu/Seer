 package en.m477.EyeSpy.Listeners;
 
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.EventPriority;
 import org.bukkit.event.player.PlayerCommandPreprocessEvent;
 import org.bukkit.event.Listener;
 
 import en.m477.EyeSpy.Logging.Logging;
 
 public class CommandListener implements Listener {
 	
 	private static String Message;
 	
 	@EventHandler(priority = EventPriority.MONITOR)
 	public static void onPlayerCommand( PlayerCommandPreprocessEvent event ) {
 		Player player = event.getPlayer();
 		String name = player.getName();
		Logging.playerExists(name);
 		Message = event.getMessage();
 		Logging.addNewCommand(name, Message);
 	}
 }
