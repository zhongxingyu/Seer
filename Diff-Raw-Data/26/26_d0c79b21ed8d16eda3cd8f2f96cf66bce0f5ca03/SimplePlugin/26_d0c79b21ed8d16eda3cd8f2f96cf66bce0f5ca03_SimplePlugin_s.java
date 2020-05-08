 package aor.SimplePlugin;
 
 
 import java.util.HashMap;
 import java.util.logging.Logger;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.event.Event;
 import org.bukkit.plugin.Plugin;
 import org.bukkit.plugin.PluginManager;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.inventory.ShapedRecipe;
 import org.bukkit.inventory.ItemStack;
 
 
 
 public class SimplePlugin extends JavaPlugin {
 
 
 	static HashMap<String, SpellBook> playerBooks = new HashMap<String, SpellBook>();
 
 
 
 	//ClassListeners
 	private final SPPlayerListener playerListener = new SPPlayerListener(this);
 	private final SPBlockListener blockListener = new SPBlockListener(this);
 	//	private final SPPlayerListener entityListener = new SPPlayerListener(this);
 	//ClassListeners
 
 	Logger log = Logger.getLogger("Minecraft");//Define your logger
 
 
 	public void onDisable() {
 		log.info("Disabled message here, shown in console on startup");
 	}
 
 	public void onEnable() {
 
 		PluginManager pm = this.getServer().getPluginManager();
 		
 		//Custom recipe
 		ItemStack hoe = new ItemStack(Material.GOLD_HOE,1);
		ShapedRecipe septre = new ShapedRecipe(hoe);
		septre.shape("123","456","789");
 		// Set the ingredients for the custom crafting recipe
		septre.setIngredient('1', Material.STICK);
		septre.setIngredient('2', Material.GOLD_BLOCK);
		septre.setIngredient('3', Material.STICK);
		septre.setIngredient('5', Material.STICK);
		septre.setIngredient('8', Material.STICK);
 		
 		//Give it to the server. YEAAAH!
		this.getServer().addRecipe(septre);
 
 		// Register the listeners.
 
 		pm.registerEvent(Event.Type.PLAYER_QUIT, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_JOIN, playerListener, Event.Priority.Normal, this);
 		pm.registerEvent(Event.Type.PLAYER_INTERACT, playerListener, Event.Priority.Normal, this);
 
 
 
 	}
 	
 	
 	 public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
 		 
 		 if(cmd.getName().equalsIgnoreCase("spellinfo")){ // If the sender typed /basic then do the following...
 			 if (sender instanceof Player) // If they're a player.
 			 {
 				 Player player = (Player)sender;
 				 sender.sendMessage("Current spell: " + SimplePlugin.playerBooks.get(player.getName()).getCurrentSpell().getDescription()); // Give them the spell description.
 			 }
 			 else { sender.sendMessage("This command can only be used in-game."); } // They're not a player.
 		   return true;
 		 } //If this has happened the function will break and return true. if this hasn't happened the a value of false will be returned.
 		 return false; }
 
 
 	
 }
