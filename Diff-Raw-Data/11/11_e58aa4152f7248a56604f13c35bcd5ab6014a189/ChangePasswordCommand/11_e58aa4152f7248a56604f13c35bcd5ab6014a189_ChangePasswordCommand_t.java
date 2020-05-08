 package net.robbytu.banjoserver.bungee.auth;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 
 public class ChangePasswordCommand  extends Command {
     public ChangePasswordCommand() {
         super("changepassword");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if(!AuthProvider.isAuthenticated(Main.instance.getProxy().getPlayer(sender.getName()))) {
             sender.sendMessage(ChatColor.RED + "Je bent niet ingelogd.");
             return;
         }
 
         if(args.length != 2) {
             sender.sendMessage(ChatColor.RED + "Geef het oude en nieuwe wachtwoord op.");
             return;
         }
 
        if(!AuthProvider.checkPassword(Main.instance.getProxy().getPlayer(sender.getName()), args[0])) {
             sender.sendMessage(" ");
             sender.sendMessage(ChatColor.RED + "Onjuist oud wachtwoord.");
             sender.sendMessage(ChatColor.GRAY + "Het wachtwoord is niet veranderd.");
             sender.sendMessage(" ");
             return;
         }
 
         AuthProvider.updatePassword(Main.instance.getProxy().getPlayer(sender.getName()), args[1]);
 
         sender.sendMessage(ChatColor.GREEN + "Je nieuwe wachtwoord is ingesteld.");
     }
 }
