 package to.joe.j2mc.reports.command;
 
 import java.util.ArrayList;
 
 import org.bukkit.ChatColor;
 import org.bukkit.Location;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.reports.J2MC_Reports;
 import to.joe.j2mc.reports.Report;
 
 public class ReportHandlingCommand extends MasterCommand {
 
     J2MC_Reports plugin;
 
     public ReportHandlingCommand(J2MC_Reports Reports) {
         super(Reports);
         this.plugin = Reports;
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (args.length == 0) {
             final ArrayList<Report> reps = this.plugin.Manager.getReports();
             final int size = reps.size();
             if (size == 0) {
                 sender.sendMessage(ChatColor.RED + "No reports. Hurray!");
                 return;
             }
             sender.sendMessage(ChatColor.DARK_PURPLE + "Found " + size + " reports:");
             for (final Report r : reps) {
                 if (!r.closed()) {
                     final Location location = r.getLocation();
                     final String x = ChatColor.GOLD.toString() + location.getBlockX() + ChatColor.DARK_PURPLE + ",";
                     final String y = ChatColor.GOLD.toString() + location.getBlockY() + ChatColor.DARK_PURPLE + ",";
                     final String z = ChatColor.GOLD.toString() + location.getBlockZ() + ChatColor.DARK_PURPLE;
                     sender.sendMessage(ChatColor.DARK_PURPLE + "[" + r.getID() + "][" + x + y + z + "]<" + ChatColor.GOLD + r.getUser() + ChatColor.DARK_PURPLE + "> " + ChatColor.WHITE + r.getMessage());
                 }
             }
         } else {
             final String action = args[0].toLowerCase();
             if (action.equals("close")) {
                 if (args.length > 1) {
                    final int id;
                    try{
                        id = Integer.parseInt(args[1]);
                    }catch(NumberFormatException e){
                        sender.sendMessage(ChatColor.RED + "ID needs to be a number you dolt");
                        return;
                    }
                     String reason = "";
                     if(args.length > 2){
                         reason = J2MC_Core.combineSplit(2, args, " ");
                     }else{
                         reason = "Closed";
                     }
                    if (id != 0 && (this.plugin.Manager.getReport(id) != null)) {
                         this.plugin.Manager.closeReport(id, sender.getName(), reason);
                         sender.sendMessage(ChatColor.DARK_PURPLE + "Report closed");
                    }else{
                        sender.sendMessage(ChatColor.RED + "No such report");
                     }
                 } else {
                     sender.sendMessage(ChatColor.DARK_PURPLE + "/r close ID [reason]");
                 }
             }
             if (action.equals("tp")) {
                 if (isPlayer) {
                     if (args.length > 1) {
                         final Report report = this.plugin.Manager.getReport(Integer.valueOf(args[1]));
                         if (report != null) {
                             player.teleport(report.getLocation());
                             player.sendMessage(ChatColor.DARK_PURPLE + "Wheeeeeeeee");
                         } else {
                             player.sendMessage(ChatColor.DARK_PURPLE + "Report not found");
                         }
                     } else {
                         player.sendMessage(ChatColor.DARK_PURPLE + "/r tp ID");
                     }
                 }
             }
         }
     }
 }
