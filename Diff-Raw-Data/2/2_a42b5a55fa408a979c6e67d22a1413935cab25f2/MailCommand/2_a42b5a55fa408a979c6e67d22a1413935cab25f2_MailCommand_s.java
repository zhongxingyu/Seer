 package net.robbytu.banjoserver.bungee.mail;
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.plugin.Command;
 
 public class MailCommand extends Command {
     private final String usage = "/mail [send/read/clear] [user/page] [message]";
 
     public MailCommand() {
         super("mail", null, "psm");
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if(args.length > 2 && args[0].equalsIgnoreCase("send")) {
             Mail mail = new Mail();
 
             String message = "";
             for(int i = 2; i < args.length; i++) message += ((message.length() == 0) ? "" : " ") + args[i];
 
             mail.from_user = sender.getName();
             mail.to_user = args[1];
             mail.message = message;
 
             Mails.sendMail(mail);
 
             sender.sendMessage(ChatColor.GREEN + "Bericht is verstuurd.");
         }
         else if(args.length == 0 || ((args.length == 1 || args.length == 2) && args[0].equalsIgnoreCase("read"))) {
             Mail[] mails = null;
            if(args[1].matches("-?\\d+"))
                 mails = Mails.getMailForUser(sender.getName(), ((args.length == 2) ? Integer.parseInt(args[1]) : 0));
             else if(sender.hasPermission("bs.admin"))
                 mails = Mails.getMailForUser(args[1], 0);
             else
                 this.failCommand(sender, "Invalid arguments.");
 
             if(mails.length == 0) {
                 sender.sendMessage(ChatColor.GRAY + "Geen berichten");
                 return;
             }
 
             sender.sendMessage(ChatColor.AQUA + "Berichten" + ((args.length == 2) ? " - Pagina " + args[1] : ""));
 
             for(Mail mail : mails) {
                 sender.sendMessage(((mail.unread) ? ChatColor.WHITE : ChatColor.GRAY) + "" + ((mail.unread) ? ChatColor.BOLD : "") + mail.from_user + ": " + mail.message);
 
                 if(mail.unread) {
                     mail.unread = false;
                     Mails.updateMail(mail);
                 }
             }
         }
         else if(args.length == 1 && args[0].equalsIgnoreCase("clear")) {
             Mails.clearMailboxForUser(sender.getName());
             sender.sendMessage(ChatColor.GREEN + "Mailbox leeggemaakt.");
         }
         else {
             this.failCommand(sender, "Unknown argument");
         }
     }
 
     private void failCommand(CommandSender sender, String message) {
         sender.sendMessage(ChatColor.RED + message);
         sender.sendMessage(ChatColor.GRAY + "Usage: " + ChatColor.ITALIC + this.usage);
     }
 }
