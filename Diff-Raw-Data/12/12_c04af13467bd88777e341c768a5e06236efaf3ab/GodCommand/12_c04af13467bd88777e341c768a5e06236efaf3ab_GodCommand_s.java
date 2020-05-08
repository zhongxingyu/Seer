 package fr.noogotte.useful_commands.command;
 
 import java.util.HashSet;
 import java.util.Set;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 import org.bukkit.event.EventHandler;
 import org.bukkit.event.Listener;
 import org.bukkit.event.entity.EntityDamageByEntityEvent;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.CommandArgs;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.exception.CommandUsageError;
 import fr.noogotte.useful_commands.UsefulCommandPlugin;
 import fr.noogotte.useful_commands.exception.PlayerNotInServer;
 import fr.noogotte.useful_commands.listener.GodComponent;
 
 @NestedCommands(name = "useful")
 public class GodCommand extends UsefulCommands {
 
 	private GodComponent godComponent;
 
 	public GodCommand(GodComponent godComponent) {
 		this.godComponent = godComponent;
    }
 	
 	@Command(name = "god", min = 0, max = 1)
 	public void godCommand(Player player, CommandArgs args) {
 		if(args.length() == 0) {
 			if(godComponent.isGod(player)) {
 				player.sendMessage(ChatColor.AQUA + "Mode dieux arreté ");
 				godComponent.removeGod(player);
			} else {		
 				godComponent.setGod(player);
 				player.sendMessage(ChatColor.GREEN + "Vous êtes en mode Dieux tapez " + ChatColor.BLUE + " /god " + ChatColor.GREEN + " pour en resortir");
 			}
 		} else if (args.length() == 1) {
 			Player target = Bukkit.getPlayer(args.get(0));
 			if(target == null) {
 				throw new PlayerNotInServer();
 			} else if(godComponent.isGodOther(target)) {
 				player.sendMessage(ChatColor.AQUA + "Mode dieux arreté pour " + ChatColor.BLUE + target.getName());
 				godComponent.removeGodOther(target);
 			} else {
 				godComponent.setGodOther(target);
 				player.sendMessage(ChatColor.AQUA + "Vous avez mis le mode Dieux pour " + ChatColor.BLUE + target.getName());
 			}
 		} else {
 			throw new CommandUsageError("Argument " + args.get(0) + " inconnu.");
 		}
 	}
 }
