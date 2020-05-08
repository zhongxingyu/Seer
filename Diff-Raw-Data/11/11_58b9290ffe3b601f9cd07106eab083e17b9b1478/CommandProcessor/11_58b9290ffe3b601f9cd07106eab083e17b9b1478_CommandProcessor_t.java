 package pi.interpreter;
 
 import java.util.Map;
 import java.util.TreeMap;
 
 public class CommandProcessor
 {
   public static final int EXIT_SUCCESS = 0;
   public static final int EXIT_FAILURE = -1;
   
   private Map<String, Command> _dictionary;
 
   // *************************************************************************
   // CONSTRUCTORS
   // *************************************************************************
 
   public CommandProcessor()
   {
     _dictionary = new TreeMap<String, Command>();
   }
 
   public CommandProcessor(Map<String, Command> dictionary)
   {
     _dictionary = dictionary;
   }
 
   // *************************************************************************
   // METHODS
   // *************************************************************************
 
   public int exec(String cmd_line, Displayer displayer)
   {
     String[] args = cmd_line.split("\\s");
    Command cmd = this.getCmd(args[0]);
    if (cmd == null)
      {
        displayer.out.println(args[0] + " : command not found");
        return CommandProcessor.EXIT_FAILURE;
      }
    
    return cmd.exec(args, displayer);
   }
   
   public void addCmd(Command cmd)
   {
     this.addCmd(cmd, null);
   }
   
   public void addCmd(Command cmd, String[] aliases)
   {
     _dictionary.put(cmd.getLabel(), cmd);
     this.addAliases(cmd, aliases);
   }
   
   public void addAliases(Command cmd, String[] aliases)
   {
     if(aliases != null)
       for(String alias: aliases)
         _dictionary.put(alias, cmd);
   }
   
   public Command rmCmd(String cmd_name)
   {
     return _dictionary.remove(cmd_name);
   }
 
   public Command getCmd(String cmd_name)
   {
     return _dictionary.get(cmd_name);
   }
 };
