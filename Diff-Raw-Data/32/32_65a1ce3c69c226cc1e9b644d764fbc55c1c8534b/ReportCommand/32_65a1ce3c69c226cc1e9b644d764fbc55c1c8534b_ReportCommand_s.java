 package to.joe.j2mc.reports.command;
 
 import java.util.Date;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.event.MessageEvent;
 import to.joe.j2mc.reports.J2MC_Reports;
 import to.joe.j2mc.reports.Report;
 
 public class ReportCommand extends MasterCommand {
 
     J2MC_Reports plugin;
 
     public ReportCommand(J2MC_Reports Reports) {
         super(Reports);
         this.plugin = Reports;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             final String reportmessage = J2MC_Core.combineSplit(0, args, " ");
             if (args.length > 0) {
                 if (player.hasPermission("j2mc.reports.admin")) {
                     final String message = ChatColor.LIGHT_PURPLE + "Report from the field: <" + ChatColor.RED + player.getName() + ChatColor.LIGHT_PURPLE + "> " + reportmessage;
                     J2MC_Manager.getCore().adminAndLog(message);
                     this.plugin.getServer().getPluginManager().callEvent(new MessageEvent(MessageEvent.compile("ADMININFO"), ChatColor.stripColor(message)));
                     player.sendMessage(ChatColor.RED + "Report transmitted. Thank you soldier.");
                 } else {
                     final Report report = new Report(0, player.getLocation(), player.getName(), reportmessage, (new Date().getTime()) / 1000, false);
                     this.plugin.Manager.AddReportFromCommand(report);
                     player.sendMessage(ChatColor.RED + "Report received. Thanks! :)");
                     player.sendMessage(ChatColor.RED + "Assuming you gave a description, we will handle it");
                 }
             } else {
                 player.sendMessage(ChatColor.RED + "To report to the admins, say /report MESSAGE");
                 player.sendMessage(ChatColor.RED + "Where MESSAGE is what you want to tell them");
             }
         }
     }
 
 }
