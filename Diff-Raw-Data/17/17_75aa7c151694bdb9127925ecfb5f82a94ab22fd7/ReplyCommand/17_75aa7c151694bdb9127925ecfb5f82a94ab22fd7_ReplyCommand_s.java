 package to.joe.j2mc.chat.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.chat.J2MC_Chat;
 import to.joe.j2mc.core.J2MC_Core;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.log.LogColors;
 
 public class ReplyCommand extends MasterCommand<J2MC_Chat> {
 
     public ReplyCommand(J2MC_Chat j2mc_chat) {
         super(j2mc_chat);
     }
 
     @Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             if (!this.plugin.lastMessage.containsKey(player.getName())) {
                 sender.sendMessage(ChatColor.RED + "No one has messaged you anything. Forever alone </3");
                 return;
             }
             if (args.length < 1) {
                 sender.sendMessage(ChatColor.RED + "Please enter a message (usage: /reply <message>)");
                 return;
             }
             Player to = this.plugin.getServer().getPlayerExact(this.plugin.lastMessage.get(player.getName()));
             if (to == null || !player.canSee(to)) {
                 sender.sendMessage(ChatColor.RED + this.plugin.lastMessage.get(player.getName()) + " is no longer online :(");
                 return;
             }
             final String message = J2MC_Core.combineSplit(0, args, " ");
             String finalmessage = this.plugin.privatemessage_format;
             finalmessage = finalmessage.replace("%from", player.getDisplayName());
             finalmessage = finalmessage.replace("%to", to.getDisplayName());
             finalmessage = finalmessage.replace("%message", message);
             final String nsamessage = ChatColor.DARK_AQUA + "[NSA] " + finalmessage;
             for (Player plr : plugin.getServer().getOnlinePlayers()) {
                if (plr != null && plr.hasPermission("j2mc.chat.admin.nsa") && (!plr.equals(player) || !plr.equals(to))) {
                    plr.sendMessage(nsamessage);
                 }
             }
             player.sendMessage(finalmessage);
             to.sendMessage(finalmessage);
             this.plugin.lastMessage.put(to.getName(), player.getName());
             this.plugin.getLogger().info(LogColors.process(finalmessage));
         }
     }
 
 }
