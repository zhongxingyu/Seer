 package to.joe.j2mc.chat.command;
 
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
import to.joe.j2mc.chat.ChatFunctions;
 import to.joe.j2mc.chat.J2MC_Chat;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 import to.joe.j2mc.core.exceptions.BadPlayerMatchException;
 
 public class MessageCommand extends MasterCommand{
 	J2MC_Chat plugin;
 
 	public MessageCommand(J2MC_Chat j2mc_chat){
 		super(j2mc_chat);
 		this.plugin = j2mc_chat;
 	}
 	
 	@Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer) {
             if (args.length < 2) {
                 player.sendMessage(ChatColor.RED + "Correct usage: /msg player message");
                 return;
             }
             Player to;
             try{
             	to = J2MC_Manager.getVisibility().getPlayer(args[0], player);
             }catch(BadPlayerMatchException e){
             	player.sendMessage(ChatColor.RED + e.getMessage());
             	return;
             }
             if(player.equals(to)){
             	player.sendMessage(ChatColor.RED + "I think you're lonely.");
             	//return;
             }
            String message = J2MC_Manager.getCore().combineSplit(1, args, " ");
             String finalmessage = plugin.privatemessage_format;
             finalmessage = finalmessage.replace("%from", player.getDisplayName());
             finalmessage = finalmessage.replace("%to", to.getDisplayName());
             finalmessage = finalmessage.replace("%message", message);
             if(player.hasPermission("j2mc-chat.mute")){
             	player.sendMessage(ChatColor.RED + "You are muted");
             	for(Player plr : J2MC_Manager.getVisibility().getOnlinePlayers(null)){
             		if(plr.hasPermission("j2mc-chat.admin.nsa")){
             			plr.sendMessage(ChatColor.YELLOW + "[Mute Blocked] " + finalmessage.replace(ChatColor.WHITE.toString(), ChatColor.AQUA.toString()));
             		}
             	}
             	return;
             }
             player.sendMessage(finalmessage);
             to.sendMessage(finalmessage);
             plugin.getLogger().info(finalmessage);
         }
 	}
 	
 }
