 package fr.noogotte.useful_commands.command;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.command.ConsoleCommandSender;
 import org.bukkit.entity.Player;
 
 import fr.aumgn.bukkitutils.command.Command;
 import fr.aumgn.bukkitutils.command.NestedCommands;
 import fr.aumgn.bukkitutils.command.args.CommandArgs;
 import fr.aumgn.bukkitutils.command.exception.CommandError;
 import fr.aumgn.bukkitutils.util.Util;
 import fr.noogotte.useful_commands.component.ChatComponent;
 
 @NestedCommands(name = "useful")
 public class ChatCommands extends UsefulCommands {
 
     private final ChatComponent component;
 
     public ChatCommands(ChatComponent component) {
         this.component = component;
     }
 
     public void ensureNotMute(CommandSender sender) {
         if (sender instanceof Player && component.isMute((Player) sender)) {
             throw new CommandError("Vous êtes muet.");
         }
     }
 
    @Command(name = "me", min = 0, max = 0)
     public void me(CommandSender sender, CommandArgs args) {
         ensureNotMute(sender);
 
         String message = args.get(0);
         String name = displayNameFor(sender);
 
         Util.broadcast("useful.chat.me.channel",
                 ChatColor.DARK_PURPLE + "* "
                         + ChatColor.GREEN + name
                         + ChatColor.DARK_PURPLE + " " + message);
     }
 
     @Command(name = "tell", min = 2, max = 2)
     public void tell(CommandSender sender, CommandArgs args) {
         ensureNotMute(sender);
 
         List<CommandSender> targets;
         if (args.get(0).equals(ChatComponent.CONSOLE_MARKER)) {
             targets = Collections.<CommandSender>singletonList(
                     Bukkit.getConsoleSender());
         } else {
             targets = Collections.<CommandSender>unmodifiableList(
                     args.getPlayer(0).match());
         }
 
         privateMessage(sender, targets, args.get(1));
 
         if (targets.size() == 1) {
             component.registerConversation(sender, targets.get(0));
         }
     }
 
     @Command(name = "reply", min = 1, max = 1)
     public void reply(CommandSender sender, CommandArgs args) {
         ensureNotMute(sender);
 
         CommandSender target =  component.getConversationTarget(sender);
         if (target == null) {
             throw new CommandError("Aucune conversation en cours.");
         }
 
         privateMessage(sender, 
                 Collections.singletonList(target),
                 args.get(0));
     }
 
     private void privateMessage(CommandSender sender, List<CommandSender> targets, String message) {
         String senderName = displayNameFor(sender);
         StringBuilder receivers = new StringBuilder();
         for (CommandSender target : targets) {
             target.sendMessage(ChatColor.ITALIC.toString() + ChatColor.AQUA
                     + "De " + senderName + ChatColor.AQUA + " : "
                     + ChatColor.WHITE + " " + message);
 
             receivers.append(displayNameFor(target));
             receivers.append(" ");
 
             if (!(sender instanceof ConsoleCommandSender)
                     && component.getPlugin().getUsefulConfig().messageInConsole()) {
                 Bukkit.getConsoleSender().sendMessage(
                         "[MSG] de " + sender.getName() + " à "
                                 + target.getName() + ": " + message);
             }
         }
 
         sender.sendMessage(ChatColor.ITALIC.toString() + ChatColor.AQUA + "A "
                 + receivers + ":");
         sender.sendMessage("  " + message);
     }
 
     private String displayNameFor(CommandSender sender) {
         if (sender instanceof ConsoleCommandSender) {
             return "#Console";
         } else if (sender instanceof Player) {
             return ((Player) sender).getDisplayName();
         } else {
             return "$" + sender.getName();
         }
     }
 
     @Command(name = "mute", min = 0, max = 1)
     public void mute(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .match(sender, "useful.chat.mute.others");
 
         for (Player target : targets) {
             if (component.isMute(target)) {
                 continue;
             }
 
             component.mute(target);
             target.sendMessage(ChatColor.AQUA
                     + "Vous avez été rendu muet.");
             if (!target.equals(sender)) {
                 sender.sendMessage(ChatColor.GREEN
                         + target.getDisplayName()
                         + ChatColor.AQUA + " est maintenant muet.");
             }
         }
     }
 
     @Command(name = "unmute", min = 0, max = 1)
     public void unmute(CommandSender sender, CommandArgs args) {
         List<Player> targets = args.getPlayers(0)
                 .match(sender, "useful.chat.unmute.others");
 
         for (Player target : targets) {
             if (!component.isMute(target)) {
                 continue;
             }
 
             component.unmute(target);
             target.sendMessage(ChatColor.AQUA
                     + "Vous n'êtes plus muet.");
             if (!target.equals(sender)) {
                 sender.sendMessage(ChatColor.GREEN
                         + target.getDisplayName()
                         + ChatColor.AQUA + " n'est plus muet.");
             }
         }
     }
 
     @Command(name = "broadcast", argsFlags="cga", min = 1, max = 1)
     public void broadcast(CommandSender sender, CommandArgs args) {
         String message = args.get(0);
         String channel = null;
         if (args.hasFlag('a')) {
             channel = "useful.chat.broadcast.admin";
         } else if (args.hasFlag('g')) {
             channel = "group." + args.getArgFlag('g');
         } else if (args.hasFlag('p')) {
             channel = args.getArgFlag('p');
         }
 
         if (channel == null) {
             Util.broadcast(message);
         } else {
             Util.broadcast(channel, message);
         }
         if (sender instanceof ConsoleCommandSender) {
             sender.sendMessage(message);
         }
     }
 }
