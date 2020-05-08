 package net.serenity.moar;
 
 import java.io.File;
 import java.util.logging.Logger;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 import org.bukkit.Server;
 import org.bukkit.plugin.PluginDescriptionFile;
 import org.bukkit.plugin.PluginLoader;
 import org.bukkit.plugin.java.JavaPlugin;
 import org.bukkit.entity.HumanEntity;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.ChatColor;
 
 /**
  * Sets the stack count to 64 of whatever you're holding
  *
  * @author MonsieurApple
  */
 
 public class Moar extends JavaPlugin
 {
	int id;
 	protected static final Logger log = Logger.getLogger("Minecraft");
 
 	public void onDisable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 	   	log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is disabled!" );
 	}
 
 	public void onEnable()
 	{
 		PluginDescriptionFile pdfFile = this.getDescription();
 		log.info( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
 	}
 	
 	@Override
 	public boolean onCommand(CommandSender sender, Command command, String commandLabel, String[] args){
 		String commandName = command.getName();
 		Player player;
 		
 		if (sender instanceof Player)
 		{
 			player = (Player) sender;
 		}
 		else
 		{
 			return false;
 		}
 		
 		if(commandName.equalsIgnoreCase("m") || commandName.equalsIgnoreCase("moar"))
 		{
			if(player.getInventory().getItemInHand().getTypeId() > 0)
 			{
				player.getInventory().getItemInHand().setAmount(64);
 			}
 			if(args.length != 0)
 			{
 				if(Integer.parseInt(args[0]) > 1)
 				{
					player.getInventory().addItem(new ItemStack(player.getInventory().getItemInHand().getTypeId(), 64 * (Integer.valueOf(args[0]) - 1), (short)0));
 				}
 			}
 			player.sendMessage(ChatColor.GREEN + "Here's some moar!");
 			return true;
 		}
 		return false;
 	}
 }
