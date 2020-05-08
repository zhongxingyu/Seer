 package pl.edu.agh.two.mud.server.command;
 
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.annotation.Alias;
 
 @Alias({ "logout", "lout" })
 public class LogOutUICommand extends UICommand {
 
 	@Override
 	public String getDescription() {
		return "Komenda wylogowania. Jej uzycie spowoduje wylogowanie sie.\n"
				+ "\t logout[lout]";
 	}
 
 }
