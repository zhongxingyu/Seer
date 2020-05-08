 package littlegruz.marioworld.commands;
 
 import littlegruz.marioworld.MarioMain;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class PlayerStuff implements CommandExecutor{
    private MarioMain plugin;
    
    public PlayerStuff(MarioMain instance){
       plugin = instance;
    }
 
    @Override
    public boolean onCommand(CommandSender sender, Command cmd,
          String commandLabel, String[] args){
       if(cmd.getName().compareToIgnoreCase("mario") == 0){
          if(args.length == 4){
             if(args[0].compareToIgnoreCase("coin") == 0){
                if(plugin.getPlayerMap().get(args[2]) != null){
                   if(args[1].compareToIgnoreCase("add") == 0)
                      plugin.getPlayerMap().get(args[2]).setCoins(plugin.getPlayerMap().get(args[2]).getCoins() + Integer.parseInt(args[3]));
                   else if(args[1].compareToIgnoreCase("sub") == 0)
                      plugin.getPlayerMap().get(args[2]).setCoins(plugin.getPlayerMap().get(args[2]).getCoins() - Integer.parseInt(args[3]));
                   else
                      return false;
                }
                else
                   return false;
             }
             else if(args[0].compareToIgnoreCase("life") == 0){
                if(plugin.getPlayerMap().get(args[2]) != null){
                   if(args[1].compareToIgnoreCase("add") == 0)
                      plugin.getPlayerMap().get(args[2]).setLives(plugin.getPlayerMap().get(args[2]).getLives() + Integer.parseInt(args[3]));
                   else if(args[1].compareToIgnoreCase("sub") == 0)
                      plugin.getPlayerMap().get(args[2]).setLives(plugin.getPlayerMap().get(args[2]).getLives() - Integer.parseInt(args[3]));
                   else
                      return false;
                }
                else
                   return false;
             }
             else
                return false;
             
             // Update GUI of player if they are online
             if(plugin.getServer().getPlayer(args[2]) != null && plugin.isSpoutEnabled())
                plugin.getGui().update(plugin.getServer().getPlayer(args[2]));
          }
          else
             sender.sendMessage(plugin.getCurrentRB().getString("WrongArguments"));
       }
       else if(cmd.getName().compareToIgnoreCase("marioscore") == 0){
          if(sender.hasPermission("marioworld.admincommands")){
             if(args.length == 1){
                if(plugin.getPlayerMap().get(args[0]) != null)
                   sender.sendMessage(plugin.getCurrentRB().getString("Coins") + ": " + Integer.toString(plugin.getPlayerMap().get(args[0]).getCoins()));
             }
             else
                sender.sendMessage(plugin.getCurrentRB().getString("WrongArguments"));
         }else
             sender.sendMessage(plugin.getCurrentRB().getString("PermissionDeny"));
       }
       else if(cmd.getName().compareToIgnoreCase("cpreturn") == 0){
          if(sender.hasPermission("marioworld.admincommands")){
             if(args.length == 1){
                if(plugin.getServer().getPlayer(args[0]) != null){
                  plugin.getServer().getPlayer(args[0]).teleport(plugin.getPlayerMap().get(args[0]).getCheckpoint());
                   sender.sendMessage("*woosh*");
                }
             }
             else
                sender.sendMessage(plugin.getCurrentRB().getString("WrongArguments"));
          }else
             sender.sendMessage(plugin.getCurrentRB().getString("PermissionDeny"));
       }
       return true;
    }
 
 }
