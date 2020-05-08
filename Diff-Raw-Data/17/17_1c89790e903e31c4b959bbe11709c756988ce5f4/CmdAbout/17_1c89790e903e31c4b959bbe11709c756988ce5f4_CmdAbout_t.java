 package yt.bam.library.commands;
 
 import java.util.logging.Logger;
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.permissions.Permission;
 import yt.bam.library.BAMLibrary;
 import yt.bam.library.Helpers;
 import yt.bam.library.ICommand;
 
 /**
  * @author fr34kyn01535
  */
 
 public class CmdAbout implements ICommand{
         public static final Logger logger = Bukkit.getLogger();
 	@Override
 	public void execute(CommandSender sender, String commandLabel, String[] args) {
             Helpers.sendMessage(sender, ChatColor.GREEN + BAMLibrary.Plugin.getName()+" "+ChatColor.WHITE +BAMLibrary.Instance.Translation.getTranslation("COMMAND_ABOUT_BY")+ChatColor.GREEN +" "+BAMLibrary.Plugin.getDescription().getAuthors());
            Helpers.sendMessage(sender, ChatColor.GREEN + "Proudly presenting BAMcraft (bam.yt)");
         }
 
 	@Override
 	public String getHelp() {
 		return BAMLibrary.Instance.Translation.getTranslation("COMMAND_ABOUT_HELP");
 	}
 
 	@Override
 	public String getSyntax() {
 		return "/"+BAMLibrary.Instance.RootCommands[0]+" about";
 	}
 
 	@Override
 	public Permission getPermissions() {
 		return null;
 	}
         
         @Override
 	public String[] getName() {
 		return new String[] {"about"};
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
