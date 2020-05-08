 package yt.bam.library.commands;
 
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import yt.bam.library.BAMLibrary;
 import yt.bam.library.ICommand;
 
 /**
  * @author fr34kyn01535
  */
 
 public class CmdHelp implements ICommand{
         public static final Logger logger = Bukkit.getLogger();
 	@Override
 	public void execute(CommandSender sender, String commandLabel, String[] args) {
            sender.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+"#####################################################");
             sender.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+BAMLibrary.Plugin.getName()+" "+BAMLibrary.Plugin.getDescription().getVersion()+" by "+BAMLibrary.Plugin.getDescription().getAuthors());
             for(ICommand cmd : BAMLibrary.Instance.Commands){
                 if(cmd.getPermissions() == null || sender.hasPermission(cmd.getPermissions())){
                     sender.sendMessage(ChatColor.WHITE+cmd.getSyntax()+" - "+cmd.getHelp());
                     //if(cmd.getExtendedHelp()!=null){
                     //    sender.sendMessage(ChatColor.GRAY+""+ ChatColor.ITALIC+cmd.getExtendedHelp());
                     //}
                 }
             }
            sender.sendMessage(ChatColor.BOLD+""+ChatColor.GREEN+"#####################################################");
         }
 
 	@Override
 	public String getHelp() {
             return BAMLibrary.Instance.Translation.getTranslation("COMMAND_HELP_HELP");
 	}
 
 	@Override
 	public String getSyntax() {
 		return "/"+BAMLibrary.Instance.RootCommands[0]+" help";
 	}
 
 	@Override
 	public Permission getPermissions() {
 		return null;
 	}
         
         @Override
 	public String[] getName() {
 		return new String[] {"help"};
 	}
         @Override
         public String getExtendedHelp() {
             return null;
         }
         @Override
         public boolean allowedInConsole() {
             return true;
         }
 }
