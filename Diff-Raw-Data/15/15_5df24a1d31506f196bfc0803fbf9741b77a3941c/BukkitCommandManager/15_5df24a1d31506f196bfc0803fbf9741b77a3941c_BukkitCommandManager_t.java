 package ch.minepvp.announcer.command.manager.bukkit;
 
 import ch.minepvp.announcer.Announcer;
 import ch.minepvp.announcer.command.CommandArgs;
 import ch.minepvp.announcer.command.manager.CommandManager;
 import org.bukkit.command.Command;
 import org.bukkit.command.CommandExecutor;
 import org.bukkit.command.CommandSender;
 
 public class BukkitCommandManager implements CommandManager, CommandExecutor {
 
     @Override
     public boolean onCommand(CommandSender commandSender, Command command, String s, String[] args) {
 
         if ( Announcer.getInstance().commandExist(command.getName()) ) {
 
             CommandArgs cmdArgs = new CommandArgs();
 
             cmdArgs.setCommand( command.getName() );

            if ( args.length > 0 ) {
                cmdArgs.setSubCommand( args[0] );
            }
 
             if ( args.length > 1 ) {
 
                 for ( int i = 1; i < args.length; i++ ) {
                     cmdArgs.addArgument( args[i] );
                 }
 
             }
 
             Announcer.getInstance().getCommandHandler( command.getName() )
                     .execute( commandSender.getName(), cmdArgs );
 
             return true;
         }
 
         return false;
     }
 
 }
