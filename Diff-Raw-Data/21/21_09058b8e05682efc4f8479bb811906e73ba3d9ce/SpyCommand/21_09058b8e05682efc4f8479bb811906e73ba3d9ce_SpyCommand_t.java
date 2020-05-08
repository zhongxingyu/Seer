 package fr.noogotte.useful_commands.command;
 
 import java.util.List;
 
import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.util.Util;
 import fr.noogotte.useful_commands.component.SpyComponent;
 
 @NestedCommands(name = "useful")
 public class SpyCommand extends UsefulCommands {
 
     private SpyComponent sp;
 
     public SpyCommand(SpyComponent spyComponent) {
         this.sp = spyComponent;
     }
 
     @Command(name = "spy", min = 0, max = 1)
     public void spy(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .match(sender, "useful.spy.command.other");
 
         for (Player target : targets) {
         	sp.toggleSpyMode(target);
         	if(sp.isSpy(target)) {
        		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        			onlinePlayer.hidePlayer(target);
        		}
         		Util.broadcast("useful.spy.command.broadcast", target.getName()
         				+ " est passé(e) en Spy Mode.");
         	    Util.broadcast(ChatColor.YELLOW + target.getName()
         	    		+ " left the game");
         	} else {
         		Util.broadcast("useful.spy.command.broadcast", target.getName()
         				+ " n'est plus en Spy Mode.");
         	    Util.broadcast(ChatColor.YELLOW + target.getName()
         	    		+ " joined the game");
        		for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
        			onlinePlayer.showPlayer(target);
        		}
         	}
         }
     }
 
     @Command(name = "spy-list")
     public void spyls(CommandSender sender, CommandArgs args) {
         sender.sendMessage(ChatColor.GREEN + "Espion (" + sp.getNbSpy() + ") : ");
         for (Player player : sp.getSpy()) {
             sender.sendMessage(ChatColor.YELLOW + " - "
                     + player.getDisplayName());
         }
     }
 }
