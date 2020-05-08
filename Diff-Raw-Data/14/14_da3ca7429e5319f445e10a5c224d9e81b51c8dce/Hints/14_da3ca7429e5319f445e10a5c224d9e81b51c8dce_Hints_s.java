 package paul_t.Hints;
 
 import java.util.HashMap;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event.Priority;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.plugin.PluginManager;
 
 public class Hints extends JavaPlugin {
     private final HintsPlayerListener playerListener = new HintsPlayerListener(this);
     private final HintsBlockListener blockListener = new HintsBlockListener(this);
     public static HashMap<Player, HashMap<String, Boolean>> PlayerList = new HashMap<Player, HashMap<String, Boolean>>();
     public static String Break;
     public static String Chat;
     public static String Bed;
     public static String Die;
     public static String Move;
     public static String Physics;
     public static String Build;
 
 
     public void onDisable() {
     	try {
     		PlayerList.clear(); //Make sure the memory is cleaned up on disable.
     		System.out.println("[Hints] Hints build #" + getBuild() + " disabled.");
     	} catch (Throwable e)
     	{
     		ErrorReporting.Report(e);
     	}
     }
 
     public void onEnable() {
     	try {
 	        PluginManager pm = getServer().getPluginManager();
 	        pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Priority.Monitor, this);
 	        pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Priority.Monitor, this);
 	        pm.registerEvent(Event.Type.PLAYER_MOVE, playerListener, Priority.Monitor, this);
 	        pm.registerEvent(Event.Type.BLOCK_BREAK, blockListener, Priority.Monitor, this);
 	        //pm.registerEvent(Event.Type.BLOCK_PHYSICS, blockListener, Priority.Monitor, this);
 	        //pm.registerEvent(Event.Type.BLOCK_CANBUILD, blockListener, Priority.Monitor, this);
 	        
 	        //Load the settings here... I'm thinking yaml for the messages, sql for storing the player data, but which... mysql anyone?
 	        Break = "[Hint] You must use the correct tool to break this block, and/or be outside of protected areas.";
 	        Chat = "[Hint] To chat to everyone on the server at once, type /global <message>, or /ch global to set your focus to global.";
 	        Bed = "[Hint] Sleeping at a bed saves your respawn point, but if the bed is destroyed or blocked, you will respawn at Dead Mans Point";
 	        Die = "[Hint] You have died, your items will remain for aproximately one hour, then will despawn.";
 	        Move = "[Hint] WASD for directions, space to jump, shift to crouch, flying, speedhacks etc are NOT allowed here.";
 	        Physics = "[Hint] Gravel and sand can fall down on you and kill you, or trap you.";
 	        Build = "[Hint] You can build magnificent structors, or hidden bases, but remember, build them out of strong materials or others may break into them!";
 	        
 	
 	        System.out.println( "[Hints] Hints build #" + getBuild() + " enabled." );
     	} catch (Throwable e)
     	{
     		ErrorReporting.Report(e);
     	}
     }
 
     public static int getBuild()
     {
    	return 1;
     }
 }
