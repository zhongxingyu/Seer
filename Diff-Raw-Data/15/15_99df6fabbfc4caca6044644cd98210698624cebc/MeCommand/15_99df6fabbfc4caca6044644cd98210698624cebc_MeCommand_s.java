 package to.joe.j2mc.chat.command;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import to.joe.j2mc.chat.J2MC_Chat;
 import to.joe.j2mc.core.J2MC_Manager;
 import to.joe.j2mc.core.command.MasterCommand;
 
 public class MeCommand extends MasterCommand{
 
 	public MeCommand(J2MC_Chat j2mc_chat){
 		super(j2mc_chat);
 	}
 	
 	@Override
     public void exec(CommandSender sender, String commandName, String[] args, Player player, boolean isPlayer) {
         if (isPlayer && (args.length > 0)) {
			String message = J2MC_Manager.getCore().combineSplit(0, args, " ");
         	for(Player pl : J2MC_Manager.getVisibility().getOnlinePlayers(null)){
         		if(pl.hasPermission("j2mc-chat.recieveMe")){
                 	pl.sendMessage("*" + player.getDisplayName() + " " + message);
         		}
         	}
         }
 	}
 	
 }
