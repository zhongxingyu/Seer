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
 import co.mcme.thegaffer.GafferResponses.GenericResponse;
 import co.mcme.thegaffer.TheGaffer;
 import co.mcme.thegaffer.storage.Job;
 import co.mcme.thegaffer.storage.JobDatabase;
 import co.mcme.thegaffer.storage.JobItem;
 import co.mcme.thegaffer.storage.JobKit;
 import co.mcme.thegaffer.utilities.PermissionsUtil;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 import org.apache.commons.lang.StringUtils;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.conversations.Conversable;
 import org.bukkit.conversations.ConversationAbandonedEvent;
 import org.bukkit.conversations.ConversationAbandonedListener;
 import org.bukkit.conversations.ConversationContext;
 import org.bukkit.conversations.ConversationFactory;
 import org.bukkit.conversations.ConversationPrefix;
 import org.bukkit.conversations.FixedSetPrompt;
 import org.bukkit.conversations.MessagePrompt;
 import org.bukkit.conversations.NumericPrompt;
 import org.bukkit.conversations.Prompt;
 import org.bukkit.conversations.StringPrompt;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 
 public class JobAdminConversation implements CommandExecutor, ConversationAbandonedListener {
 
     private final ConversationFactory conversationFactory;
 
     private final List<String> actions = new ArrayList();
 
     public JobAdminConversation() {
         conversationFactory = new ConversationFactory(TheGaffer.getPluginInstance())
                 .withModality(true)
                 .withEscapeSequence("!cancel")
                 .withPrefix(new jobAdminPrefix())
                 .withFirstPrompt(new whichJobPrompt())
                 .withTimeout(60)
                 .thatExcludesNonPlayersWithMessage("You must be a player to send this command");
         actions.add("addhelper");
         actions.add("removehelper");
         actions.add("kickworker");
         actions.add("banworker");
         actions.add("unbanworker");
         actions.add("setwarp");
         actions.add("bringall");
         actions.add("listworkers");
         actions.add("inviteworker");
         actions.add("uninviteworker");
         actions.add("setradius");
         actions.add("setkit");
         Collections.sort(actions);
     }
 
     @Override
     public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
         if (sender instanceof Conversable && sender.hasPermission(PermissionsUtil.getCreatePermission())) {
             conversationFactory.buildConversation((Conversable) sender).begin();
             return true;
         } else {
             return false;
         }
     }
 
     @Override
     public void conversationAbandoned(ConversationAbandonedEvent abandonedEvent) {
         if (abandonedEvent.gracefulExit()) {
             abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.AQUA + "Jobadmin exited.");
         } else {
             abandonedEvent.getContext().getForWhom().sendRawMessage(ChatColor.AQUA + "Jobadmin timed out");
         }
     }
 
     public class jobAdminPrefix implements ConversationPrefix {
 
         @Override
         public String getPrefix(ConversationContext context) {
             String prefix = ChatColor.GRAY + "";
             String jobname = (String) context.getSessionData("jobname");
             if (jobname != null) {
                 prefix += "editing " + ChatColor.GOLD + jobname + ChatColor.AQUA + "\n";
             }
             return prefix;
         }
 
     }
 
     private class responsePrompt extends MessagePrompt {
 
         GafferResponse response;
         Prompt last;
 
         public responsePrompt(GafferResponse resp, Prompt lastPrompt) {
             response = resp;
             last = lastPrompt;
         }
 
         @Override
         public Prompt getNextPrompt(ConversationContext context) {
             if (response.isSuccessful()) {
                 return Prompt.END_OF_CONVERSATION;
             } else {
                 return last;
             }
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             if (response.isSuccessful()) {
                 return ChatColor.GREEN + "Success: " + response.getMessage().replaceAll("%name%", (String) context.getSessionData("inputname")).replaceAll("%job%", (String) context.getSessionData("jobname"));
             } else {
                 return ChatColor.RED + "Failure: "
                         + response.getMessage()
                         .replaceAll("%name%", (String) context.getSessionData("inputname"))
                         .replaceAll("%job%", (String) context.getSessionData("jobname"))
                         + "\n" + " Please try again or cancel with !cancel";
             }
         }
     }
 
     private class whichJobPrompt extends StringPrompt {
 
         @Override
         public String getPromptText(ConversationContext context) {
             if (context.getSessionData("jobname") == null) {
                return "What job would you like to modify? \n" + formatSet() + "\n" + "or exit with !cancel";
             } else {
                 return "That job is not running, please try again.";
             }
         }
 
         private String formatSet() {
             return StringUtils.join(JobDatabase.getActiveJobs().keySet(), ", ");
         }
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             context.setSessionData("jobname", input);
             if (JobDatabase.getActiveJobs().containsKey(input)) {
                 context.setSessionData("job", JobDatabase.getActiveJobs().get(input));
                 return new whichActionPrompt();
             } else {
                 return new whichJobPrompt();
             }
         }
     }
 
     private class whichActionPrompt extends FixedSetPrompt {
 
         public whichActionPrompt() {
             super(actions.toArray(new String[actions.size()]));
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             if (context.getSessionData("action") != null) {
                 return "Please enter a valid action \n" + formatFixedSet();
             } else {
                 return "What action would you like to perform? \n" + formatFixedSet();
             }
         }
 
         @Override
         public Prompt acceptValidatedInput(ConversationContext context, String input) {
             context.setSessionData("action", input);
             switch (input) {
                 case "addhelper": {
                     return new addHelperPrompt();
                 }
                 case "removehelper": {
                     return new removeHelperPrompt();
                 }
                 case "kickworker": {
                     return new kickWorkerPrompt();
                 }
                 case "banworker": {
                     return new banWorkerPrompt();
                 }
                 case "unbanworker": {
                     return new unbanWorkerPrompt();
                 }
                 case "setwarp": {
                     return new updateWarpPrompt();
                 }
                 case "bringall": {
                     return new bringallWorkersPrompt();
                 }
                 case "listworkers": {
                     return new listWorkersPrompt();
                 }
                 case "inviteworker": {
                     return new inviteWorkerPrompt();
                 }
                 case "uninviteworker": {
                     return new uninviteWorkerPrompt();
                 }
                 case "setradius": {
                     return new setRadiusPrompt();
                 }
                 case "setkit": {
                     return new setKitPrompt();
                 }
                 default: {
                     return new whichActionPrompt();
                 }
             }
         }
     }
 
     private class addHelperPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.addHelper(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to add as a helper?";
         }
     }
 
     private class removeHelperPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.removeHelper(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to remove as a helper?";
         }
     }
 
     private class kickWorkerPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.kickWorker(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to kick from the job?";
         }
     }
 
     private class banWorkerPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.banWorker(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to ban from the job?";
         }
     }
 
     private class unbanWorkerPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.unbanWorker(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to unban from the job?";
         }
     }
 
     private class updateWarpPrompt extends MessagePrompt {
 
         @Override
         public Prompt getNextPrompt(ConversationContext context) {
             Job job = (Job) context.getSessionData("job");
             job.updateLocation(((Player) context.getForWhom()).getLocation());
             return Prompt.END_OF_CONVERSATION;
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Successfully moved the warp to your location.";
         }
     }
 
     private class bringallWorkersPrompt extends MessagePrompt {
 
         @Override
         public Prompt getNextPrompt(ConversationContext context) {
             Job job = (Job) context.getSessionData("job");
             job.bringAllWorkers(((Player) context.getForWhom()).getLocation());
             return Prompt.END_OF_CONVERSATION;
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Brought all online workers to your location.";
         }
     }
 
     private class listWorkersPrompt extends MessagePrompt {
 
         @Override
         public Prompt getNextPrompt(ConversationContext context) {
             return Prompt.END_OF_CONVERSATION;
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             Job job = (Job) context.getSessionData("job");
             return StringUtils.join(job.getWorkers().toArray(new String[job.getWorkers().size()]), "\n");
         }
     }
 
     private class inviteWorkerPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.inviteWorker(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to invite to the job?";
         }
     }
 
     private class uninviteWorkerPrompt extends StringPrompt {
 
         @Override
         public Prompt acceptInput(ConversationContext context, String input) {
             Job job = (Job) context.getSessionData("job");
             context.setSessionData("inputname", input);
             GafferResponse response = job.uninviteWorker(TheGaffer.getServerInstance().getOfflinePlayer(input));
             return new responsePrompt(response, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Who would you like to uninvite from the job?";
         }
     }
 
     private class setRadiusPrompt extends NumericPrompt {
 
         @Override
         public Prompt acceptValidatedInput(ConversationContext context, Number input) {
             context.setSessionData("jobradius", input);
             Job job = (Job) context.getSessionData("job");
             job.updateJobRadius(input.intValue());
             return new responsePrompt(GenericResponse.SUCCESS, this);
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Should big should the job area be? (radius 0 - 1000)";
         }
     }
 
     private class setKitPrompt extends MessagePrompt {
 
         @Override
         public Prompt getNextPrompt(ConversationContext context) {
             Job job = (Job) context.getSessionData("job");
             JobKit kit = new JobKit(((Player) context.getForWhom()).getInventory());
             job.setKit(kit);
             for (String pname : job.getWorkers()) {
                 if (TheGaffer.getServerInstance().getOfflinePlayer(pname).isOnline()) {
                     job.getKit().replaceInventory(TheGaffer.getServerInstance().getOfflinePlayer(pname).getPlayer());
                 }
             }
             return Prompt.END_OF_CONVERSATION;
         }
 
         @Override
         public String getPromptText(ConversationContext context) {
             return "Successfully set the kit of the job to your inventory.";
         }
     }
 
 }
