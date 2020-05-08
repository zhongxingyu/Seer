 package slogo;
 
 import java.lang.reflect.Field;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.ResourceBundle;
 import java.util.regex.Pattern;
 import behavior.ICommand;
 import exceptions.NoSuchCommandException;
 import exceptions.NoSuchVariableException;
 import exceptions.SyntaxException;
 
 
 /**
  * parse the command
  * 
  * @author Richard Yang
  *         Parses input
  * 
  * @author Richard, Jerry
  * 
  */
 public class Parser {
 
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
     private Map<String, ICommand> myUserToCommands = new HashMap<String, ICommand>();
 
     private Pattern myNumPattern;
     private Pattern myStrPattern;
     private Pattern mySpacePattern;
     public ResourceBundle myResources;
 
     /**
      * constructor
      * Constructs parser
      */
     public Parser () {
         myNumPattern = Pattern.compile("[0-9]*");
         myStrPattern = Pattern.compile("[a-zA-Z]*");
         mySpacePattern = Pattern.compile("[\\s]+");
         myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "commands");
     }
 
     /**
      * split the string
      * 
      * @param commands commands we want to split
      * @return splited string
      *         Splits commands
      * 
      * @param commands commands
      * @return
      * @throws NoSuchFieldException
      * @throws SecurityException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      * @throws NoSuchMethodException
      * 
      */
 
     public List<String[]> split (String s, Model model) throws NoSuchFieldException,
                                                        SecurityException, IllegalArgumentException,
                                                        IllegalAccessException {
         List<String> l = new LinkedList<String>();
         int depth = 0;
         StringBuilder sb = new StringBuilder();
         for (int i = 0; i < s.length(); i++) {
             char c = s.charAt(i);
             if (c == '[') {
                 depth += 1;
             }
             else if (c == ']') {
                 depth -= 1;
             }
             else if (c == ' ' && depth == 0) {
                l.add(sb.toString());
                 sb = new StringBuilder();
                 continue;
             }
             sb.append(c);
         }
         l.add(sb.toString());
 
         for (String g : l) {
             System.out.println("presplit: " + g);
         }
         return addCommands(l, model);
     }
 
     public List<String[]> addCommands (List<String> l, Model model) throws NoSuchFieldException,
                                                                    SecurityException,
                                                                    IllegalArgumentException,
                                                                    IllegalAccessException {
         List<String[]> commandArray = new ArrayList<String[]>();
         for (int i = 0; i < l.size(); i++) {
 
             if (myResources.containsKey(l.get(i))) {
                 ArrayList<String> temp = new ArrayList<String>();
                 String commandName = myResources.getString(l.get(i).toUpperCase());
 
                 Class<?> commandClass = null;
                 try {
                     commandClass = Class.forName("behavior." + commandName);
                     System.out.println("found class");
                 }
                 catch (ClassNotFoundException e) {
                     model.showMessage("class not found");
                 }
 
                 Field field = commandClass.getDeclaredField("PARAMETER_NUMBER");
                 int parameter = field.getInt(commandClass);
                 for (int j = 0; j < parameter; j++) {
                     temp.add(l.get(i + j));
                 }
                 String command[] = new String[temp.size()];
                 temp.toArray(command);
                 commandArray.add(command);
             }
 
         }
         System.out.println("size of command array " + commandArray.size());
         for (int i = 0; i < commandArray.size(); i++) {
             System.out.println("User input: " + Arrays.toString(commandArray.get(i)));
         }
         return commandArray;
     }
 
     /**
      * build a command through string we got
      * 
      * @param str splited input commands
      * @param model model we want to operate
      * @return command
      * @throws NoSuchCommandException
      * @throws SyntaxException
      * @throws NoSuchVariableException
      * @throws SecurityException
      * @throws NoSuchFieldException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      */
 
     public ICommand buildCommand (String[] str, Model model) throws SyntaxException,
                                                             NoSuchVariableException,
                                                             NoSuchCommandException,
                                                             NoSuchFieldException, SecurityException,
                                                             IllegalArgumentException,
                                                             IllegalAccessException {
         if (model.getUserCommands().containsKey(str[0])) {
             return (ICommand) model.getUserCommands().get(str[0]);
         }
         else if (!myResources.containsKey(str[0].toUpperCase())) {
             throw new NoSuchCommandException();
         }
         else {
             String[] subArray = subStringArray(str);
             String commandName = myResources.getString(str[0].toUpperCase());
 
             Class<?> commandClass = null;
             try {
                 commandClass = Class.forName("behavior." + commandName);
             }
             catch (ClassNotFoundException e) {
                 // model.showMessage("class not found");
             }
             Object o = null;
             try {
                 o = commandClass.newInstance();
 
             }
             catch (InstantiationException | IllegalAccessException e) {
 
                 // model.showMessage("illegal access");
 
             }
             ICommand myCommand = (ICommand) o;
             myCommand.initialize(subArray, model);
             return myCommand;
 
         }
     }
 
     /**
      * build multiple commands
      * 
      * @param commands command strings
      * @param model mode we want to operate
      * @return
      * @throws SyntaxException
      * @throws NoSuchCommandException
      * @throws NoSuchVariableException
      * @throws SecurityException
      * @throws NoSuchFieldException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      */
 
     public List<ICommand> buildMultipleCommands (List<String[]> commands, Model model)
                                                                                       throws SyntaxException,
                                                                                       NoSuchCommandException,
                                                                                       NoSuchVariableException,
                                                                                       NoSuchFieldException,
                                                                                       SecurityException,
                                                                                       IllegalArgumentException,
                                                                                       IllegalAccessException {
         if (commands == null) { return null; }
 
         List<ICommand> myCommandList = new ArrayList<ICommand>();
         for (int i = 0; i < commands.size(); i++) {
             String[] str = commands.get(i);
             myCommandList.add(buildCommand(str, model));
         }
 
         return myCommandList;
     }
 
     /**
      * delete first element of a string
      * 
      * @param str input string
      */
     public String[] subStringArray (String[] str) {
         int size = str.length;
         String[] subArray = new String[size - 1];
         for (int i = 0; i < size - 1; i++) {
             subArray[i] = str[i + 1];
         }
 
         return subArray;
     }
 
    
     public void parse (String command, List<ICommand> myCommandList, Model model)
                                                                                  throws NoSuchCommandException,
                                                                                  SyntaxException,
                                                                                  NoSuchVariableException,
                                                                                  NoSuchFieldException,
                                                                                  SecurityException,
                                                                                  IllegalArgumentException,
                                                                                  IllegalAccessException {
 
         myCommandList.addAll(buildMultipleCommands(split(command, model), model));
     }
 
 }
