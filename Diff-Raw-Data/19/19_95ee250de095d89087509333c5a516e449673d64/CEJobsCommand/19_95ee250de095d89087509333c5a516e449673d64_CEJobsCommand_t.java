 package me.ccattell.plugins.completeeconomy.commands;
 
 import java.util.List;
 import java.util.Set;
 import static me.ccattell.plugins.completeeconomy.CompleteEconomy.plugin;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 /**
  *
  * @author Charlie
  */
 public class CEJobsCommand implements CommandExecutor {
 
     public String prefix = plugin.configs.getJobConfig().getString("Jobs.Prefix");
     public String moduleName = ChatColor.BLUE + prefix + ChatColor.RESET + " ";
     public boolean DeleteOnQuit = plugin.configs.getJobConfig().getBoolean("Jobs.DeleteOnQuit");
     public String ReJoinPercent = plugin.configs.getJobConfig().getString("Jobs.ReJoinPercent");
    public boolean found_job;
     @Override
     public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
 
         if (cmd.getName().equalsIgnoreCase("jobs")) {
             // don't do anything unless it's our command
             Set<String> jobsList = plugin.configs.getJobConfig().getConfigurationSection("Jobs.Types").getKeys(false);
 
             Player player;
             if (sender instanceof Player) {
                 player = (Player) sender;
             } else {
                 sender.sendMessage(moduleName + "The jobs command cannot be used from the console!");
                 return true;
             }
             if (!sender.hasPermission("ce.jobs")) {
                 sender.sendMessage(moduleName + "You don't have permission to use jobs!");
                 return true;
             } else {
                 if (args.length == 0) { // if args.length != 2 - incorrect number of arguments?
                     player.sendMessage(moduleName + "Incorrect number of arguments");
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("list") && args.length == 1) {
                     player.sendMessage(moduleName + "Available jobs:");
                     for (String job : jobsList) {
                         player.sendMessage("    " + job);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("info") && args.length == 2) {
                    String job = args[1];
                    for (String jobs : jobsList) {
                        if (jobs.equalsIgnoreCase(job)) {
                            found_job = true;
                            break;
                        }else{
                            found_job = false;
                        }
                    }
                    if(found_job == true){
                    }else{
                         player.sendMessage(moduleName + "Could not find a job with that name, use /jobs list to find one!");
                         return true;
                     }
                    // check args[1] is in the jobs list
                     List<String> infoList = plugin.configs.getJobConfig().getStringList("Jobs.Types." + job);
                     player.sendMessage(moduleName + "Job description for " + job + ":");
                     // loop thru list
                     for (String info : infoList) {
                         //need to go one more layer deep and get info from skillsConfig on skills found associated with this job
                         // send message
                         player.sendMessage("    " + info);
                     }
                     return true;
                 } else if (args[0].equalsIgnoreCase("join") && args.length == 2) {
                     String job = args[1].toLowerCase();
                     // check args[1] is in the jobs list
                     if (!jobsList.contains(job)) {
                         player.sendMessage(moduleName + "Could not find a job with that name, use /jobs list to find out the jobs you can join!");
                         return true;
                     }
                     //check to see if player already has that job
                     //if(!player has job){
                     //    doInsert("CEJobs", player = player, job = job, status = active)
                     //}else if(player has inactive job){
                     //    doUpdate("CEJobs", status = active, level = level * ReJoinPercent, experience = 0 where player = player and job = job)
                     //}
                     return true;
                 } else if (args[0].equalsIgnoreCase("stats") && args.length == 1) {
                     //check to see if player has any jobs
                     //if (!player has job) {
                     //    player.sendMessage(moduleName + "Could not find any jobs you have, use /jobs list to find out the jobs you can join!");
                     //} else {
                     //    for (String info : infoList) {
                             // send message
                     //        player.sendMessage(info);
                     //    }
                     //}
                     return true;
                 } else if (args[0].equalsIgnoreCase("quit") && args.length == 2) {
                     String job = args[1].toLowerCase();
                     // check args[1] is in the jobs list
                     if (!jobsList.contains(job)) {
                         player.sendMessage(moduleName + "Could not find a job with that name, use /jobs stats to find out the jobs you have!");
                         return true;
                     }
                     if(DeleteOnQuit){
                      //   doDelete("CEJobs", player = player and job = job);
                     }else{
                      //   doUpdate("CEJobs", status = 'Inactive' where player = player and job = job);
                     }
                     return true;
                 } else {
                     player.sendMessage(moduleName + "Incorrect number of arguments");
                     // show them the proper usage
                     return false;
                 }
             }
         }
         return true;
     }
 }
