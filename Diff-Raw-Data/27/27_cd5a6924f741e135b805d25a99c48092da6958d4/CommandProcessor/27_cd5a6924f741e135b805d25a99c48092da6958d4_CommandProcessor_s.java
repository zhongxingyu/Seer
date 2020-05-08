 package lambda.system;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Map;
 
 public class CommandProcessor
 {
 	private Map<String, CommandDelegate> delegates = new HashMap<String, CommandDelegate>();
 
 	public void add(String command, CommandDelegate d)
 	{
 		delegates.put(command, d);
 	}
 
 	public void invokeCommand(String input)
 	{
 		String[] args = input.split("\\s+");
 		String cmd = args[0];
 		CommandDelegate l = delegates.get(cmd);
 		if (l != null)
 		{
 			String[] params = Arrays.copyOfRange(args, 1, args.length);
 			l.commandInvoked(params);
 		}
 		else
 		{
 			System.out.println("- unknown command " + cmd);
 		}
 	}
 }
