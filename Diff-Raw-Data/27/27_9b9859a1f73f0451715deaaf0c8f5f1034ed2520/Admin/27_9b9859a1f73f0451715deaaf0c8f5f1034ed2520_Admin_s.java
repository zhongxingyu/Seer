 package littlegruz.arpeegee.commands;
 
import littlegruz.arpeegee.ArpeegeeMain;

 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 public class Admin implements CommandExecutor{
   private ArpeegeeMain plugin;
   
   public Admin(ArpeegeeMain instance){
      plugin = instance;
    }
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
          String commandLabel, String[] args){
       if(sender.hasPermission("rpg.admin") && sender instanceof Player){
          Player playa = (Player) sender;
          if(cmd.getName().compareToIgnoreCase("levelup") == 0){
             playa.setLevel(playa.getLevel() + 1);
          }
          else if(cmd.getName().compareToIgnoreCase("leveldown") == 0){
             playa.setLevel(playa.getLevel() - 1);
          }
       }
       return true;
    }
 }
