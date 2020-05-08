 package net.robbytu.banjoserver.bungee.chat;/* vim: set expandtab tabstop=4 shiftwidth=4 softtabstop=4: */
 
 import net.md_5.bungee.api.ChatColor;
 import net.md_5.bungee.api.CommandSender;
 import net.md_5.bungee.api.connection.ProxiedPlayer;
 import net.md_5.bungee.api.plugin.Command;
 import net.robbytu.banjoserver.bungee.Main;
 import net.robbytu.banjoserver.bungee.mute.MuteUtil;
 
 import java.util.Random;
 
 public class SlapCommand extends Command {
 
     public SlapCommand() {
         super("slap", null);
     }
 
     @Override
     public void execute(CommandSender sender, String[] args) {
         if(MuteUtil.isMuted(sender.getName())) return;
 
         if(args.length == 0) return;
 
         String action = new String[] { "een keiharde klap op z'n bakkes",
                                        "een bitch slap",
                                        "een wedgie",
                                       "ebola" }[new Random().nextInt(4)];
 
 
         for(ProxiedPlayer player : Main.instance.getProxy().getPlayers()) {
             player.sendMessage(ChatColor.YELLOW + " * " + sender.getName() + " gaf " + args[0] + " " + action + " <3");
         }
     }
 }
