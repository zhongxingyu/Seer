package me.cdsandrade.testplugin;
 
 import java.lang.reflect.Method;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class TestPluginCommandExecutor implements CommandExecutor {
 	
 	public final TestPlugin plugin;
 	public final Logger logger = Logger.getLogger("TestPlugin");
 	
 	public TestPluginCommandExecutor(TestPlugin instance) {
 		plugin = instance;
 	}
 	
 	/**
 	 * onCommand() method kanged from CommandsEX to standardize triggers from multiple command classes.
 	 * @param sender - Source of the command
 	 * @param cmd - Command which was executed
 	 * @param label - Alias of the command which was executed
 	 * @param args - Passed command arguments
 	 * @return true if a valid command, otherwise false
 	 */
 	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 		String cmdName = cmd.getName().substring(0, 1).toUpperCase() + cmd.getName().substring(1).toLowerCase();
 		String alias = label.toLowerCase();
 
 		try {
 			Class<?>[] proto = new Class[] {CommandSender.class, String.class, String[].class};
 			Object[] params = new Object[] {sender, alias, args};
 			Class<?> c = Class.forName("me.cdsandrade.testplugin.commands.TestPluginCommand" + cmdName);
 			Method method = c.getDeclaredMethod("run", proto);
 			Object ret = method.invoke(null, params);
 			return Boolean.TRUE.equals(ret);
 		} catch (Throwable e) {
 			e.printStackTrace();
 		}
 		return true;
 		
 //		// Commands that target an online player should share common arg (0) and checker
 //		Player target = (Bukkit.getServer().getPlayer(args[0]));
 //		if (target == null) {
 //			sender.sendMessage(ChatColor.RED + "There's no player by that name online.");
 //			return false;
 //		}
 //		// Establish commands
 //		if (cmd.getName().equalsIgnoreCase("slay")) {
 //	        target.setHealth(0);
 //	        this.logger.info(sender.getName() + " slayed " + target.getName() + ".");
 //	        return true;
 //		} // End of /slay
 //		if (cmd.getName().equalsIgnoreCase("ignite")) {
 //			target.setFireTicks(10000);
 //			this.logger.info(sender.getName() + " ignited " + target.getName() + ".");
 //			return true;
 //		} // End of /ignite
 //		if (cmd.getName().equalsIgnoreCase("extinguish")) {
 //			// Check if target is on fire first, no point in extinguishing a regular player
 //			if (target.getFireTicks() > 0) {
 //				target.setFireTicks(0);
 //				this.logger.info(sender.getName() + " extinginguished " + target.getName() + ".");
 //				return true;
 //			} else {
 //				sender.sendMessage(ChatColor.RED + "That player is not on fire!");
 //				return false;
 //			}
 //		} // End of /extinguish
 //		if (cmd.getName().equalsIgnoreCase("explode")) {
 //			// TODO Improve method so that explosion doesn't do terrain damage but still attributes player death to an explosion (i.e. the system still says "X blew up".)
 //			float tntPower = 4F; //This is the explosion power - TNT explosions are 4F by default
 //		    target.getWorld().createExplosion(target.getLocation(), tntPower);
 //		    //target.setHealth(0);
 //		    this.logger.info(sender.getName() + " exploded " + target.getName() + ".");
 //		} // End of /explode
 //		// Inspect can only be run by a player
 //		if (cmd.getName().equalsIgnoreCase("inspect")) {
 //			target.getInventory();
 //			return true;
 //		} // End of /inspect
 //		return false;
 		
 	}
 
 }
