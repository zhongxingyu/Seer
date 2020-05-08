 package fr.noogotte.useful_commands.command;
 
 import java.util.Map.Entry;
 
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.noogotte.useful_commands.component.HomeComponent;
 import fr.noogotte.useful_commands.component.HomeComponent.Home;
 
 @NestedCommands(name = "useful")
 public class HomeCommands extends UsefulCommands {
 
 	private HomeComponent hm;
 
 	public HomeCommands(HomeComponent homeComponent) {
 		this.hm = homeComponent;
 	}
 
 	@Command(name = "sethome")
 	public void setHome(Player sender, CommandArgs args) {
 		if (hm.haveHome(sender.getName())) {
 			throw new CommandError("Vous avez déja définis un home");
 		}
 
 		sender.sendMessage(ChatColor.GREEN + "Home définis.");
 		hm.addHome(sender, sender.getName());
 		hm.save();
 	}
 
 	@Command(name = "home", max = 1)
 	public void home(Player sender, CommandArgs args) {
 		String home_name = args.get(0, sender.getName());

 		if(!hm.haveHome(home_name)) {
 			if(home_name != sender.getName()) {
 				throw new CommandError(home_name + " n'a pas de home");
 			}
 			throw new CommandError("Vous n'avez pas de home !");
 		}
 
		if(home_name != sender.getName()) {
			if(!sender.hasPermission("useful.home.use." + home_name) || !sender.hasPermission("useful.home.use.other")){
				throw new CommandError("Vous n'avez la permission de vous téléporté au home de " + home_name);
			}
 		}
 
 		Home home = hm.getHome(home_name);
 		sender.sendMessage(ChatColor.GREEN + "Poof !");
 		sender.teleport(home.toLocation());
 	}
 
 	@Command(name = "deletehome", max = 1)
 	public void delHome(Player sender, CommandArgs args) {
 		String home_name = args.get(0, sender.getName());
 
 		if (!hm.haveHome(home_name)) {
 			if(home_name != sender.getName()) {
 				throw new CommandError(home_name + " n'a pas de home");
 			}
 			throw new CommandError("Vous n'avez pas de home !");
 		}
 
		if(home_name != sender.getName()) {
			if(!sender.hasPermission("useful.home.delhome." + home_name)) {
				throw new CommandError("Vous n'avez la permission de supprimer le home de " + home_name);
			}
		}

 		sender.sendMessage(ChatColor.GREEN + "Home supprimer. (" + home_name + ")");
 		hm.deleteHome(home_name);
 		hm.save();
 	}
 
 	@Command(name = "homes")
 	public void homes(Player sender, CommandArgs args) {
 		if(hm.isEmpty()) {
 			throw new CommandError("Aucun home de sauvegarder !");
 		}
 
 		sender.sendMessage(ChatColor.YELLOW +"Homes (" + hm.getNbHome() + "):");
 		for (Entry<String, Home> homes : hm.homes()) {
 			sender.sendMessage(ChatColor.YELLOW + " - " + homes.getKey());
 		}
 	}
 }
