 package me.kukelekuuk00.cactusfarmfix;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 
 
 public class cactusfarmfix extends JavaPlugin {
 	public final HashMap<Player, ArrayList<Block>> cfUsers = new HashMap<Player, ArrayList<Block>>();
 	private final cactusfarmfixBlockListener blockListener = new cactusfarmfixBlockListener(this);	
 	Logger log = Logger.getLogger("Minecraft");
 	
 	public void onEnable()
 	{
 	log.info("cactus autofarm fix has been enabled.");
 	PluginManager pm = this.getServer().getPluginManager();
 	pm.registerEvent (Event.Type.BLOCK_PHYSICS, blockListener, Event.Priority.Normal, this);	
 	}
 		 
 	
 	 
 	public void onDisable()
 	{
 		log.info("cactus autofarm fix has been disabled.");
 	}
 }
