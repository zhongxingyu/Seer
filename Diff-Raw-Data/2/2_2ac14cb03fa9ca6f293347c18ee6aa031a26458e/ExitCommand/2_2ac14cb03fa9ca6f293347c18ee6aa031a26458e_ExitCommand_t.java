 package com.ballew.tools.cli.api.defaultcommands;
 
 import com.ballew.tools.cli.api.CLIContext;
 import com.ballew.tools.cli.api.Command;
 import com.ballew.tools.cli.api.CommandResult;
 import com.ballew.tools.cli.api.CommandResult.CommandResultType;
 import com.ballew.tools.cli.api.annotations.CLICommand;
 
 /**
  * Returns an exit result type. This will exit the application when executed.
  * @author Sean
  *
  */
@CLICommand(name="exit", description="Exits the application.")
 public class ExitCommand extends Command<CLIContext> {
 
 	@Override
 	public CommandResult innerExecute(CLIContext context) {
 		return new CommandResult(CommandResultType.EXIT, 0);
 	}
 
 }
