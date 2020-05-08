 
 public abstract class Command 
 {
 	protected String commandString;
	protected GameController controller;
 	
 	public abstract void execute();
 	
 	public String getCommand()
 	{
 		return commandString;
 	}

 }
