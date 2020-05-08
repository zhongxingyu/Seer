 package littlegruz.autoruncommands.commands;
 
 import littlegruz.autoruncommands.CommandMain;
 
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class Repeat implements CommandExecutor{
 private CommandMain plugin;
    
    public Repeat(CommandMain instance){
       plugin = instance;
    }
    
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
       if(commandLabel.compareToIgnoreCase("addrepeatcommand") == 0){
          if(sender.hasPermission("autoruncommands.addrepeat")){
             if(args.length == 2){
                try{
                   final String command;
                   int interval, id;
                   
                   // Get the commands or stop if it does not exist
                   if(plugin.getCommandMap().get(args[0]) != null)
                      command = args[0];
                   else if(plugin.getCommandMap().get(args[0] + "[op]") != null)
                      command = args[0] + "[op]";
                   else{
                      sender.sendMessage("No command found with that identifier");
                      sender.sendMessage("Try \'/addacommand <identifier> <command> [args]\' first");
                      return true;
                   }
                   
                   interval = Integer.parseInt(args[1]);
                   
                   // If the command is found and is not already repeating, add it
                   // NOTE: All commands will be run by the console
                   if(plugin.getRunningRepeatMap().get(args[0]) == null){
                      id = plugin.getServer().getScheduler().scheduleAsyncRepeatingTask(plugin,  new Runnable() {
 
                         public void run() {
                            plugin.getServer().dispatchCommand(plugin.getServer().getConsoleSender(), plugin.getCommandMap().get(command));
                         }
                      }, interval * 20, interval * 20);
 
                      plugin.getRepeatMap().put(command, interval);
                      plugin.getRunningRepeatMap().put(command, id);
                      sender.sendMessage("That command will repeat every "
                            + interval + " seconds from now");
                   }
                  else{
                      sender.sendMessage("That command is already repeating");
                  }
                  return true;
                }catch(NumberFormatException e){
                   sender.sendMessage("Please enter an integer for the repeating inverval");
                }
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       else if(commandLabel.compareToIgnoreCase("removerepeatcommand") == 0){
          if(sender.hasPermission("autoruncommands.removerepeat")){
             if(args.length == 1){
                String command = args[0];
                
               if(plugin.getRunningRepeatMap().get(command) != null){
                   plugin.getServer().getScheduler().cancelTask(plugin.getRunningRepeatMap().get(command));
                   plugin.getRunningRepeatMap().remove(command);
                   plugin.getRepeatMap().remove(command);
                   sender.sendMessage("That command has now stopped repeating");
                }
               else if(plugin.getRunningRepeatMap().get(command + "[op]") != null){
                  plugin.getServer().getScheduler().cancelTask(plugin.getRunningRepeatMap().get(command + "[op]"));
                  plugin.getRunningRepeatMap().remove(command + "[op]");
                  plugin.getRepeatMap().remove(command + "[op]");
                  sender.sendMessage("That command has now stopped repeating");
               }
                else{
                  sender.sendMessage("No command with that identifier is running");
                }
               return true;
             }
             else
                sender.sendMessage("Wrong number of arguments");
          }
          else
             sender.sendMessage("You don't have sufficient permissions");
       }
       
       return false;
    }
 
 }
