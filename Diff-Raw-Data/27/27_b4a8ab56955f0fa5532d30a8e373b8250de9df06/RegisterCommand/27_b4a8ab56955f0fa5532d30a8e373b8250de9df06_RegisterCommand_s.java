 package pl.edu.agh.two.mud.server.command;
 
 import pl.edu.agh.two.mud.common.command.UICommand;
 import pl.edu.agh.two.mud.common.command.annotation.Alias;
 import pl.edu.agh.two.mud.common.command.annotation.OrderedParam;
 
 @Alias({"rejestruj", "rej"})
 public class RegisterCommand extends UICommand {
	
 	@OrderedParam(1)
 	private String login;
	
 	@OrderedParam(2)
 	private String email;
 
 	@Override
 	public String getDescription() {
 		return "rejestruj <login> <email>";
 	}
 
 	public String getLogin() {
 		return login;
 	}
 
 	public String getEmail() {
 		return email;
 	}
 
 }
