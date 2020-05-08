 package behavior;
 
 import java.util.ArrayList;
 import java.util.List;
 import slogo.Model;
 import slogo.Parser;
 import exceptions.ParameterException;
 
 
 /**
  * class used to implement ASK command
  * support multiple turtles
  * 
  * @author Richard Yang
  * 
  */
 
 public class Ask implements ICommand {
 
     /**
      * the parameter number of this certain command
      */
 
     public static final int PARAMETER_NUMBER = 2;
 
     private List<Integer> myActivatedTurtles;
     private Parser myParser = new Parser();
     private List<ICommand> myCommands;
 
     @Override
     public double move (Model model, int turtleNumber) throws Exception {
         model.clearFutureActivatedTurtles();
         model.activatedAllTurtles();
         model.replaceActivatedTurtles();
         int last = 0;
         for (int i : myActivatedTurtles) {
             model.addFutureActivatedTurtles(i);
             for (ICommand ic : myCommands) {
                 ic.move(model, i);
                System.out.println("COMMAND EXECUTED");
             }
             System.out.println("turtle activated:" + i);
             last = i;
         }
 
         return last;
     }
 
     @Override
     public void initialize (String[] information, Model model) throws Exception {
         if (information.length != PARAMETER_NUMBER) { throw new ParameterException(
                                                                                    "Parameter doesn't match for ask command:length"); }
         myActivatedTurtles = new ArrayList<Integer>();
         String[] mySplitedContent = myParser.splitBlanksInsideBracket(information[0]);
         for (String str : mySplitedContent) {
             if (!myParser.judgeNumeric(str)) { throw new ParameterException(
                                                                             "Parameter doesn't match for ASK: not numeric"); }
             myActivatedTurtles.add(Integer.parseInt(str));
         }
         System.out.println("activated turtle number :" + myActivatedTurtles.size());
         String myPrunedStringCommand = myParser.prune(information[1]);
         myCommands = createCommandsList(myPrunedStringCommand, model);
        System.out.println("number of commands in ask : " + myCommands.size());
 
     }
 
     /**
      * 
      * @param commands command inside the second bracket
      * @param model model we want to operate
      * @return
      * @throws Exception several exceptions with messages
      */
 
     public List<ICommand> createCommandsList (String commands, Model model) throws Exception {
         List<String[]> myListOfCommands = myParser.split(commands, model);
         return myParser.buildMultipleCommands(myListOfCommands, model);
     }
 
 }
