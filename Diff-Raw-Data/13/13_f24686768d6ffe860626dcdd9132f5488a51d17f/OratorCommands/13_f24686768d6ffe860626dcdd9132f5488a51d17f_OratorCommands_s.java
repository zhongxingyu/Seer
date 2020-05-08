 package fr.ethilvan.bukkit.orator;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.Commands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 
 public class OratorCommands implements Commands {
 
     private final OratorPlugin plugin;
 
     public OratorCommands(OratorPlugin plugin) {
         this.plugin = plugin;
     }
 
    @Command(name = "orator", flags = "p")
    public void orator(CommandSender sender, CommandArgs args) {
         Player player = args.getPlayer('p')
                 .valueWithPermOr("ev.orator.use.other", sender);
         if (plugin.isOrator(player)) {
             plugin.turnOff(player);
         } else {
             plugin.turnOn(player);
         }
     }
 
    @Command(name = "orator-time", max = 1, flags = "p")
    public void oratorTime(CommandSender sender, CommandArgs args) {
         Player player = args.getPlayer('p')
                 .valueWithPermOr("ev.orator.time.other", sender);
         if (args.length() == 0) {
             player.resetPlayerTime();
             player.sendMessage(ChatColor.GREEN + "Temps réinitalisé.");
         } else {
             int time = args.getTime(0).value();
             player.setPlayerTime(time, false);
             player.sendMessage(ChatColor.GREEN + "Le temps a été changé.");
         }
     }
 }
