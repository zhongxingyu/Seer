 package pl.edu.agh.two.mud.server.command;
 
 import pl.edu.agh.two.mud.common.command.Command;
 
public class SendMessageToUserCommand extends Command {
 
 	private String message;
 
 	public SendMessageToUserCommand(String errorMessage) {
 		this.message = errorMessage;
 	}
 
 	public String getMessage() {
 		return message;
 	}
 
 }
