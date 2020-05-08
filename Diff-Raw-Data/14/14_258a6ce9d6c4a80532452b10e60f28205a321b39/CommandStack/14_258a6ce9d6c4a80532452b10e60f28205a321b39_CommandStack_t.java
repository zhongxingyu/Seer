 
 import java.util.*;
 
 public class CommandStack {
 	//A stack of all commands said before
 	private Stack<Command> cStack;
 	
 	public final static String GO = "go";
 	public final static String UNDO = "undo";
	public final static String REDO = "redo";
 	public final static HashMap<String, String> reverseCommand = new HashMap<String, String>();
     static {
     	 reverseCommand.put("pick", "drop");
     	 reverseCommand.put("drop", "pick");
     	 reverseCommand.put("attack", "unattack");
     	 reverseCommand.put("unattack", "attack");
 	}
     public final static HashMap<String, String> reverseDirection = new HashMap<String, String>();
     static {
     	reverseDirection.put("north", "south");
     	reverseDirection.put("south", "north");
     	reverseDirection.put("east", "west");
     	reverseDirection.put("west", "east");
 	}
 	
 	public CommandStack()
 	{
 		cStack = new Stack<Command>();
 	}
 
 	public void add(Command c)
 	{
 		//Do not add undo or redo commands to a command stack
		if(c.getCommandWord().equals(UNDO)||c.getCommandWord().equals(REDO)) {
			return;
		}
 		
 		cStack.add(c);
 	}
 	
 	public Command pop()
 	{
 		//If the stack is empty, return a null pointer
 		if(cStack.isEmpty())return null;
 		
 		//Last command taken off the stack
 		Command old  = cStack.pop();
 		
 		//If word is GO, reverse direction
 		if(old.getCommandWord().equals(GO))
 		{
 			//return command with go and opposite direction of last go in commandStack
 			return new Command(old.getCommandWord(), reverseDirection.get(old.getSecondWord()));
 		}
 		//return reverse of first word and same second word of last command in commandStack
 		return new Command(reverseCommand.get(old.getCommandWord()), old.getSecondWord());
 	}
 }
