 
 public abstract class Command 
 {
 	protected String commandString;
	protected GameControllerInterface controller;
 	
 	public abstract void execute();
 	
 	public String getCommand()
 	{
 		return commandString;
 	}
	
 }
