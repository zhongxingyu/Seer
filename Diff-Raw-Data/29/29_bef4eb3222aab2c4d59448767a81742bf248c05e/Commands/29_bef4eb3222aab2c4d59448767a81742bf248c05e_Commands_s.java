 package jivko.test;
 
 import java.util.ArrayList;
 import java.util.List;
 import jivko.brain.movement.Command;
 import jivko.brain.movement.CommandsCenter;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class Commands implements Testable {
 
   private static String[] commandsToTest = {
    //"ROBOT_ANGRY" 
    "RIGHT_HAND_CLOSE"
     /*,"ROBOT_THINKING"
     ,"DUMMY=2000"
     , "ROBOT_NORMAL"
     , "RIGHT_HAND_INDEX=2000"
     , "ROBOT_FUCK_TO_YOU"   
      ,"EYES_TILT_UP"*/
   };
   
   /**
    *
    * @throws Exception
    */
   public void test() throws Exception {
     List<Command> commands = new ArrayList<>();
     
     for (String s : commandsToTest) {
       Command command = CommandsCenter.getInstance().getCommandEx(s);
       commands.add(command);
     }
     
     CommandsCenter.getInstance().executeCommandList(commands);
   }
 }
