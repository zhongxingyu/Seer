 package me.tehbeard.BeardStat.commands;
 
 import me.tehbeard.BeardStat.BeardStat;
 import me.tehbeard.BeardStat.commands.formatters.FormatFactory;
 import me.tehbeard.BeardStat.commands.interactive.FindPlayerPrompt;
 import me.tehbeard.BeardStat.commands.interactive.SelectCategoryPrompt;
 import me.tehbeard.BeardStat.commands.interactive.SelectStatisticPrompt;
 import me.tehbeard.BeardStat.commands.interactive.SetSelfPrompt;
 import me.tehbeard.BeardStat.commands.interactive.ShowStatisticPrompt;
 import me.tehbeard.BeardStat.containers.PlayerStatBlob;
 import me.tehbeard.BeardStat.containers.PlayerStatManager;
 import me.tehbeard.utils.commands.ArgumentPack;
 import me.tehbeard.vocalise.parser.PromptBuilder;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.conversations.Conversable;
 import org.bukkit.conversations.Conversation;
 import org.bukkit.conversations.ConversationAbandonedEvent;
 import org.bukkit.conversations.ConversationAbandonedListener;
 import org.bukkit.conversations.ExactMatchConversationCanceller;
 import org.bukkit.entity.Player;
 
 public class StatCommand implements CommandExecutor {
 
     private PlayerStatManager playerStatManager;
 
 
     private PromptBuilder builder;
     private ExactMatchConversationCanceller canceller = new ExactMatchConversationCanceller("/exit");
 
     @SuppressWarnings("unchecked")
     public StatCommand(PlayerStatManager playerStatManager) {
         this.playerStatManager = playerStatManager;
         builder = new PromptBuilder(BeardStat.self());
         builder.AddPrompts(
                 SelectCategoryPrompt.class,
                 SelectStatisticPrompt.class,
                 ShowStatisticPrompt.class,
                 SetSelfPrompt.class,
                 FindPlayerPrompt.class);
 
         builder.load(BeardStat.self().getResource("interactive.yml"));
     }
 
 
     public boolean onCommand(CommandSender sender, Command command, String cmdLabel, String[] args) {
 
 
         if (!BeardStat.hasPermission(sender, "command.stat")) {
             BeardStat.sendNoPermissionError(sender);
             return true;
         }
 
         
         ArgumentPack arguments = new ArgumentPack(new String[] {"i","h"}, new String[] {"p","s"}, args);
         
         String player = null;
                 
         if(BeardStat.hasPermission(sender, "command.stat.other")) {
                 player = arguments.getOption("p");
         }
         
         if(player == null && sender instanceof Player){
             player = ((Player)sender).getName();
         }
 
         if(player == null ||arguments.getFlag("h")){
             sendHelpMessage(sender);
             return true;
         }
 
         if(arguments.getFlag("i")){
             sender.sendMessage(ChatColor.GOLD + "Entering interactive mode, type /exit to leave interactive mode");
             Conversation c = builder.makeConversation((Conversable) sender);
            c.getCancellers().add(canceller.clone());
            
             
             c.addConversationAbandonedListener(new ConversationAbandonedListener(){
 
                 public void conversationAbandoned(
                         ConversationAbandonedEvent event) {
                     event.getContext().getForWhom().sendRawMessage(ChatColor.GOLD + "Leaving interactive stats mode");
                     
                 }
                 
             });
             
             return true;
         }
         
         if(arguments.getOption("s")!=null){
             String stat = arguments.getOption("s");
             if(stat.split("\\.").length == 2){
                 PlayerStatBlob blob = playerStatManager.findPlayerBlob(player);
                 if(blob == null){
                     sender.sendMessage(ChatColor.RED + "Could not find player");
                     return true;
                 }
                 
                 if(blob.hasStat(stat.split("\\.")[0], stat.split("\\.")[1])){
                     sender.sendMessage(ChatColor.GOLD + stat + " = " + ChatColor.WHITE + FormatFactory.formatStat( blob.getStat(stat.split("\\.")[0], stat.split("\\.")[1])));
                 }
                 else
                 {
                     sender.sendMessage("Stat not found");
                 }
                 
             }
             else
             {
                 sender.sendMessage(ChatColor.RED + "Invalid stat");
                 return true;
             }
         }
         
         Bukkit.dispatchCommand(sender, "statpage " + player + " default");
         
         
         //TODO: FINISH UP
         
 
 
         return true;
     }
 
     public static void sendHelpMessage(CommandSender sender) {
         sender.sendMessage(ChatColor.BLUE + "Stats Help page");
         sender.sendMessage(ChatColor.BLUE + "/stats:" + ChatColor.GOLD + " Default display of your stats");
         sender.sendMessage(ChatColor.BLUE + "/stats [flags]:");
         sender.sendMessage(ChatColor.BLUE + "-h :" + ChatColor.GOLD + " This page");
         sender.sendMessage(ChatColor.BLUE + "-i :" + ChatColor.GOLD + " Interactive stats menu");
         sender.sendMessage(ChatColor.BLUE + "-p [player]:" + ChatColor.GOLD + " view [player]'s stats");
         sender.sendMessage(ChatColor.BLUE + "-s [stat] :" + ChatColor.GOLD + " view this stat (format category.statistic)");
         sender.sendMessage(ChatColor.BLUE + "/statpage :" + ChatColor.GOLD + " list available stat pages");
         sender.sendMessage(ChatColor.BLUE + "/statpage [user] page :" + ChatColor.GOLD + " show a specific stat page");
         if (BeardStat.hasPermission(sender, "command.laston")) {
             sender.sendMessage(ChatColor.BLUE + "/laston [user] :" + ChatColor.GOLD + " show when you [or user] was last on");
         }
         if (BeardStat.hasPermission(sender, "command.laston")) {
             sender.sendMessage(ChatColor.BLUE + "/firston [user] :" + ChatColor.GOLD + " show when you [or user] was first on");
         }
         if (BeardStat.hasPermission(sender, "command.played")) {
             sender.sendMessage(ChatColor.BLUE + "/played [user] :" + ChatColor.GOLD + " shows how long you [or user] have played");
         }
 
     }
 
    
 
     public static void SendPlayerStats(CommandSender sender, PlayerStatBlob blob) {
         if (blob != null && blob.getStat("stats", "playedfor").getValue() != 0) {
             sender.sendMessage(ChatColor.GOLD + "-= " + blob.getName() + "'s Stats =-");
 
             long seconds = blob.getStat("stats", "playedfor").getValue();
             if (sender instanceof Player) {
                 seconds += BeardStat.self().getStatManager().getSessionTime(((Player) sender).getName());
             }
             sender.sendMessage(playedCommand.GetPlayedString(seconds));
 
             Bukkit.dispatchCommand(sender, "statpage " + blob.getName() + " default");
         }
         else {
             sender.sendMessage(ChatColor.RED + "Player not found.");
         }
     }
 }
