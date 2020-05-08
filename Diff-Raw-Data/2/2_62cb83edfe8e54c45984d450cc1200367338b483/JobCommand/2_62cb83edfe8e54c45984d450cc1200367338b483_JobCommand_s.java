 /*  This file is part of TheGaffer.
  * 
  *  TheGaffer is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  TheGaffer is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with TheGaffer.  If not, see <http://www.gnu.org/licenses/>.
  */
 package co.mcme.thegaffer.commands;
 
 import co.mcme.thegaffer.GafferResponses.GafferResponse;
 import co.mcme.thegaffer.TheGaffer;
 import co.mcme.thegaffer.storage.Job;
 import co.mcme.thegaffer.storage.JobDatabase;
 import co.mcme.thegaffer.storage.JobKit;
 import co.mcme.thegaffer.utilities.CleanupUtil;
 import co.mcme.thegaffer.utilities.PermissionsUtil;
 import co.mcme.thegaffer.utilities.Util;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import org.bukkit.ChatColor;
 import org.bukkit.Sound;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.TabExecutor;
 import org.bukkit.entity.Player;
 import org.bukkit.util.ChatPaginator;
 
 public class JobCommand implements TabExecutor {
 
     private HashMap<Player, JobKit> invs = new HashMap();
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (!(sender instanceof Player)) {
             sender.sendMessage("You are not a player");
             return true;
         }
         if ((args.length > 0) && ((sender instanceof Player))) {
             Player player = (Player) sender;
             if (args.length >= 2) {
                 if (args[0].equalsIgnoreCase("stop")) {
                     if (player.hasPermission(PermissionsUtil.getCreatePermission())) {
                         if (args[1] != null) {
                             String jobname = args[1];
                             if (JobDatabase.getActiveJobs().containsKey(jobname)) {
                                 JobDatabase.deactivateJob(JobDatabase.getActiveJobs().get(jobname));
                                 player.sendMessage(ChatColor.GRAY + "Successfully closed the " + ChatColor.AQUA + jobname + ChatColor.GRAY + " job.");
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job found by that name.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide a job name.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You don't have permission.");
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("pause")) {
                     if (player.hasPermission(PermissionsUtil.getCreatePermission())) {
                         if (args[1] != null) {
                             String jobname = args[1];
                             if (JobDatabase.getActiveJobs().containsKey(jobname)) {
                                 Job target = JobDatabase.getActiveJobs().get(jobname);
                                 target.pauseJob(player.getName());
                                 player.sendMessage(ChatColor.GREEN + "Paused " + target.getName());
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job found by that name.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide a job name.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You don't have permission.");
                     }
                     return true;
                 }
                 if (args[0].equalsIgnoreCase("unpause")) {
                     if (player.hasPermission(PermissionsUtil.getCreatePermission())) {
                         if (args[1] != null) {
                             String jobname = args[1];
                             if (JobDatabase.getActiveJobs().containsKey(jobname)) {
                                 Job target = JobDatabase.getActiveJobs().get(jobname);
                                 target.unpauseJob(player.getName());
                                 player.sendMessage(ChatColor.GREEN + "Un paused " + target.getName());
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job found by that name.");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide a job name.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "You don't have permission.");
                     }
                     return true;
                 }
             }
             if (args[0].equalsIgnoreCase("prep")) {
                 if (player.hasPermission(PermissionsUtil.getCreatePermission())) {
                     if (invs.containsKey(player)) {
                         JobKit old = invs.get(player);
                         old.replaceInventory(player);
                         player.sendMessage(ChatColor.GREEN + "Recovered previous inventory.");
                         invs.remove(player);
                         player.playSound(player.getLocation(), Sound.ANVIL_LAND, 0.5f, 2f);
                     } else {
                         invs.put(player, new JobKit(player.getInventory()));
                         player.getInventory().clear();
                         player.sendMessage(ChatColor.GREEN + "Stored your inventory.");
                         player.sendMessage(ChatColor.GREEN + "When ready, run this command again to get your inventory back.");
                         player.playSound(player.getLocation(), Sound.ANVIL_LAND, 0.5f, 0.5f);
                         player.updateInventory();
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You don't have permission.");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("aero")) {
                 player.sendMessage(Util.dino);
                 return true;
             }
             if (args[0].equalsIgnoreCase("debug")) {
                 if (player.hasPermission(PermissionsUtil.getCreatePermission())) {
                     StringBuilder out = new StringBuilder();
                     out.append(ChatColor.DARK_PURPLE).append(ChatColor.BOLD).append("TheGaffer Debug").append("\n");
                     out.append(ChatColor.GRAY).append("Servlet port: ").append(ChatColor.AQUA).append(TheGaffer.getServletPort()).append("\n");
                     out.append(ChatColor.GRAY).append("Number of active jobs: ").append(ChatColor.AQUA).append(JobDatabase.getActiveJobs().size()).append("\n");
                     out.append(ChatColor.GRAY).append("Number of inactive jobs: ").append(ChatColor.AQUA).append(JobDatabase.getInactiveJobs().size()).append("\n");
                     out.append(ChatColor.GRAY).append("Number of jobs timing out: ").append(ChatColor.AQUA).append(CleanupUtil.getWaiting().size()).append("\n");
                     out.append(ChatColor.GRAY).append("Join permission: ").append(ChatColor.AQUA).append(PermissionsUtil.getJoinPermission().getName()).append("\n");
                     out.append(ChatColor.GRAY).append("Ignore protection permission: ").append(ChatColor.AQUA).append(PermissionsUtil.getIgnoreWorldProtection().getName()).append("\n");
                     out.append(ChatColor.GRAY).append("Create permission: ").append(ChatColor.AQUA).append(PermissionsUtil.getCreatePermission().getName()).append("\n");
                     player.sendMessage(out.toString());
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("check")) {
                 if (player.hasPermission(PermissionsUtil.getJoinPermission())) {
                     if (JobDatabase.getActiveJobs().size() > 0) {
                         StringBuilder out = new StringBuilder();
                         out.append(ChatColor.GRAY).append("Running Jobs:");
                         for (String jobName : JobDatabase.getActiveJobs().keySet()) {
                             Job job = JobDatabase.getActiveJobs().get(jobName);
                             out.append("\n").append(ChatColor.AQUA).append(jobName).append(ChatColor.GRAY).append(" with ").append(job.getOwner()).append(" (").append(job.getWorkers().size()).append(")");
                         }
                         player.sendMessage(out.toString());
                     } else {
                         player.sendMessage(ChatColor.GRAY + "No jobs currently running.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You don't have permission.");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("join")) {
                 if (player.hasPermission(PermissionsUtil.getJoinPermission())) {
                     if (JobDatabase.getActiveJobs().size() > 0) {
                         if (args.length > 1) {
                             if (JobDatabase.getActiveJobs().containsKey(args[1])) {
                                 Job jobToJoin = JobDatabase.getActiveJobs().get(args[1]);
                                 GafferResponse resp = jobToJoin.addWorker(player);
                                 if (resp.isSuccessful()) {
                                     player.sendMessage(ChatColor.GRAY + "You have joined the job " + ChatColor.AQUA + jobToJoin.getName());
                                 } else {
                                     player.sendMessage(ChatColor.RED + "Error: " + resp.getMessage().replaceAll("%name%", player.getName()).replaceAll("%job%", jobToJoin.getName()));
                                 }
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job running by the name of `" + args[1] + "`");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to join.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "No jobs currently running.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission.");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("info")) {
                 if (args.length > 1) {
                     if (JobDatabase.getActiveJobs().containsKey(args[1])) {
                         Job jobToJoin = JobDatabase.getActiveJobs().get(args[1]);
                         player.sendMessage(jobToJoin.getInfo());
                     } else if (JobDatabase.getInactiveJobs().containsKey(args[1])) {
                         Job jobToJoin = JobDatabase.getInactiveJobs().get(args[1]);
                         player.sendMessage(jobToJoin.getInfo());
                     } else {
                         player.sendMessage(ChatColor.RED + "No job found by the name of `" + args[1] + "`");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "What job would you like to get info on?");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("warpto")) {
                 if (player.hasPermission(PermissionsUtil.getJoinPermission())) {
                     if (JobDatabase.getActiveJobs().size() > 0) {
                         if (args.length > 1) {
                             if (JobDatabase.getActiveJobs().containsKey(args[1])) {
                                 Job jobToJoin = JobDatabase.getActiveJobs().get(args[1]);
                                 player.teleport(jobToJoin.getWarp().toBukkitLocation());
                                 player.sendMessage(ChatColor.GRAY + "Warped to " + ChatColor.AQUA + jobToJoin.getName());
                             } else {
                                 player.sendMessage(ChatColor.RED + "No job running by the name of `" + args[1] + "`");
                             }
                         } else {
                             player.sendMessage(ChatColor.RED + "You must provide the name of the job you would like to warp to.");
                         }
                     } else {
                         player.sendMessage(ChatColor.RED + "No jobs currently running.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You do not have permission.");
                 }
                 return true;
             }
             if (args[0].equalsIgnoreCase("archive")) {
                 if (player.hasPermission(PermissionsUtil.getJoinPermission())) {
                     if (JobDatabase.getInactiveJobs().size() > 0) {
                         StringBuilder out = new StringBuilder();
                         int pageNum = 1;
                         boolean first = true;
                         for (String jobName : JobDatabase.getInactiveJobs().keySet()) {
                             Job job = JobDatabase.getInactiveJobs().get(jobName);
                             if (!first) {
                                 out.append("\n");
                             }
                             out.append(ChatColor.AQUA).append(job.getName()).append(ChatColor.GRAY).append(" with ").append(job.getOwner()).append(" (").append(job.getWorkers().size()).append(")");
                             if (first) {
                                 first = false;
                             }
                         }
                         if (args.length > 1) {
                             pageNum = Integer.valueOf(args[1]);
                         }
                         ChatPaginator.ChatPage page = ChatPaginator.paginate(out.toString(), pageNum, ChatPaginator.AVERAGE_CHAT_PAGE_WIDTH, 8);
                         player.sendMessage(ChatColor.AQUA + "Job Archive Page: " + page.getPageNumber() + " of " + page.getTotalPages());
                         player.sendMessage(page.getLines());
                     } else {
                         player.sendMessage(ChatColor.GRAY + "No jobs found in archive.");
                     }
                 } else {
                     player.sendMessage(ChatColor.RED + "You don't have permission.");
                 }
                 return true;
             }
         }
         return false;
     }
 
     @Override
     public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
         if (args[0].equalsIgnoreCase("archive")) {
             return null;
         }
         if (args[0].equalsIgnoreCase("check")) {
             return null;
         }
         if (args[0].equalsIgnoreCase("info")) {
             List<String> jobs = new ArrayList();
             jobs.addAll(JobDatabase.getActiveJobs().keySet());
             jobs.addAll(JobDatabase.getInactiveJobs().keySet());
             return jobs;
         }
         if (args[0].equalsIgnoreCase("join") || args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("pause") || args[0].equalsIgnoreCase("unpause")) {
             List<String> jobs = new ArrayList();
             jobs.addAll(JobDatabase.getActiveJobs().keySet());
             return jobs;
         }
         List<String> actions = new ArrayList();
         actions.add("archive");
         actions.add("warpto");
         actions.add("info");
         actions.add("join");
         actions.add("check");
         if (sender.hasPermission(PermissionsUtil.getCreatePermission())) {
             actions.add("stop");
             actions.add("debug");
             actions.add("prep");
            actions.add("pasue");
             actions.add("unpause");
         }
         Collections.sort(actions);
         return actions;
     }
 }
