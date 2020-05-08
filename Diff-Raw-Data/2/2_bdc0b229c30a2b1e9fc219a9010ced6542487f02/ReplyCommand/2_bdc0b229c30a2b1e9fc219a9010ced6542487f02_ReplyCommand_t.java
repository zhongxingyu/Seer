 package net.robbytu.banjoserver.bungee.pm;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 
 public class ReplyCommand extends Command {
     public ReplyCommand() {
         super("r", null, "reply");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if(!PmSession.replyTo.containsKey(Main.instance.getProxy().getPlayer(sender.getName()))) {
             sender.sendMessage(ChatColor.GRAY + "Je hebt geen bericht om op te reageren.");
             return;
         }
 
         if(args.length < 1) {
             sender.sendMessage(ChatColor.RED + "Geef een bericht op.");
             return;
         }
 
         ProxiedPlayer receiver = PmSession.replyTo.get(Main.instance.getProxy().getPlayer(sender.getName()));
 
         if(receiver == null) {
             sender.sendMessage(ChatColor.RED + args[0] + " is offline.");
             sender.sendMessage(ChatColor.GRAY + "Je kan nog wel een mail sturen met het /mail commando.");
 
             PmSession.replyTo.remove(Main.instance.getProxy().getPlayer(sender.getName()));
 
             return;
         }
 
         String message = "";
         for (int i = 0; i < args.length; i++) message += ((message.equals("")) ? "" : " ") + args[i];
 
         sender.sendMessage(ChatColor.GRAY + "[" + sender.getName() + " -> " + receiver.getName() + "] " + message);
        receiver.sendMessage(ChatColor.GRAY + "[" + sender.getName() + " -> " + receiver.getName() + "] " + message);
 
         if(PmSession.replyTo.containsKey(receiver)) PmSession.replyTo.remove(receiver);
         PmSession.replyTo.put(receiver, Main.instance.getProxy().getPlayer(sender.getName()));
     }
 }
