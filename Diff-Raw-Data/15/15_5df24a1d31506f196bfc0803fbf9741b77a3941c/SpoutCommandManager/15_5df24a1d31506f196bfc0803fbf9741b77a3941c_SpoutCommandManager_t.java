 package ch.minepvp.announcer.command.manager.spout;
 
 import ch.minepvp.announcer.Announcer;
 import ch.minepvp.announcer.command.CommandArgs;
 import ch.minepvp.announcer.command.manager.CommandManager;
 import org.spout.api.command.Command;
 import org.spout.api.command.CommandContext;
 import org.spout.api.command.CommandExecutor;
 import org.spout.api.command.CommandSource;
 import org.spout.api.exception.CommandException;
 
 import java.util.ArrayList;
 import java.util.List;
 
 public class SpoutCommandManager implements CommandManager, CommandExecutor {
 
     @Override
     public void processCommand(CommandSource commandSource, Command command, CommandContext args) throws CommandException {
 
         if (Announcer.getInstance().commandExist(command.getPreferredName())) {
 
             CommandArgs cmdArgs = new CommandArgs();
 
             cmdArgs.setCommand(command.getPreferredName());

            if ( args.length() > 0 ) {
                cmdArgs.setSubCommand(args.getString(0));
            }
 
             if ( args.length() > 1 ) {
 
                 for ( int i = 1; i < args.length(); i++ ) {
                     cmdArgs.addArgument( args.getString(i) );
                 }
 
             }
 
             Announcer.getInstance().getCommandHandler(command.getPreferredName())
                     .execute(commandSource.getName(), cmdArgs);
 
         }
 
     }
 
 }
