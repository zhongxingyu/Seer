 package behavior;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.ResourceBundle;
 import slogo.Model;
 import slogo.Parser;
 import exceptions.NoSuchCommandException;
 import exceptions.NoSuchVariableException;
 import exceptions.SyntaxException;
 
 
 /**
  * Implements TO command
  * 
  * @author Jerry
  * 
  */
 public class To implements ICommand {
 
    public static final int PARAMETER_NUMBER = 3;
     private List<ICommand> myCommandList = new ArrayList<ICommand>();
     private String[] myVariables;
     private String[] myCommands;
     private String myName;
     private Parser myParser = new Parser();
 
     private ResourceBundle myResources;
 
     /**
      * resources of commands
      */
     private static final String DEFAULT_RESOURCE_PACKAGE = "resources.";
 
     /**
      * Initializes TO command
      * 
      * @param name name
      * @param variables variables
      * @param commands commands
      * @throws NoSuchCommandException No command exception
      * @throws SyntaxException Wrong syntax exception
      */
 
     public To () {
 
     }
 
     public void construct (String value, String firstBracket, String secondBracket, Model model)
                                                                                                 throws NoSuchCommandException,
                                                                                                 SyntaxException,
                                                                                                 NoSuchVariableException,
                                                                                                 NoSuchFieldException,
                                                                                                 SecurityException,
                                                                                                 IllegalArgumentException,
                                                                                                 IllegalAccessException {
 
         parse(value, firstBracket, secondBracket);
         myResources = ResourceBundle.getBundle(DEFAULT_RESOURCE_PACKAGE + "commands");
         map(myVariables, myCommands, model);
         model.addUserCommands(value, this);
     }
 
     public int checkLength () {
         if (myVariables.length == myCommands.length) {
             return 1;
         }
         else {
             return 0;
         }
     }
 
     public void parse (String value, String firstBracket, String secondBracket) {
 
         String firstBracketPruned = firstBracket.substring(1, firstBracket.length() - 1);
         String secondBracketPruned = secondBracket.substring(1, secondBracket.length() - 1);
         myVariables = firstBracketPruned.split("\\s+");
         myCommands = secondBracketPruned.split("\\s+");
         System.out.println(firstBracketPruned);
         System.out.println(secondBracketPruned);
     }
 
     /**
      * return name
      * 
      * @return
      */
     public String getName () {
         return myName;
     }
 
     /**
      * Maps stuff in brackets to respective lists
      * 
      * @param variables variables
      * @param commands commands
      * @throws NoSuchCommandException NoSuchCommandException
      * @throws SyntaxException SyntaxException
      * @throws NoSuchVariableException
      * @throws IllegalAccessException
      * @throws IllegalArgumentException
      * @throws SecurityException
      * @throws NoSuchFieldException
      */
     public void map (String[] variables, String[] commands, Model model)
                                                                         throws NoSuchCommandException,
                                                                         SyntaxException,
                                                                         NoSuchVariableException,
                                                                         NoSuchFieldException,
                                                                         SecurityException,
                                                                         IllegalArgumentException,
                                                                         IllegalAccessException {
         if (checkLength() == 0) { throw new NoSuchCommandException(); }
         for (int i = 0; i < commands.length; i++) {
             String command = commands[i];
             // System.out.println(Arrays.toString(command));
             String variable = variables[i];
             // System.out.println(Arrays.toString(variable));
 
             String[] str = { command, variable };
             ICommand myCommand = myParser.buildCommand(str, model);
             myCommandList.add(myCommand);
             // System.out.println(myCommandList.size());
         }
     }
 
     /**
      * Move
      * 
      * @param model the model
      * @param turtleNumber the turtle
      * @throws SyntaxException SyntaxException
      * @throws NoSuchVariableException 
      * @throws NoSuchCommandException 
      * @throws IllegalAccessException 
      * @throws IllegalArgumentException 
      * @throws SecurityException 
      * @throws NoSuchFieldException 
      */
     @Override
     public double move (Model model, int turtleNumber) throws SyntaxException, NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException, NoSuchCommandException, NoSuchVariableException {
         for (int i = 0; i < myCommandList.size(); i++) {
             myCommandList.get(i).move(model, turtleNumber);
         }
 
         return myCommandList.size();
     }
 
     @Override
     public void initialize (String[] information, Model model) throws SyntaxException,
                                                               NoSuchCommandException,
                                                               NoSuchVariableException,
                                                               NoSuchFieldException,
                                                               SecurityException,
                                                               IllegalArgumentException,
                                                               IllegalAccessException {
         System.out.println("initialize successful");
         System.out.println("subarray size" + information.length);
         construct(information[0], information[1], information[2], model);
     }
 
 }
