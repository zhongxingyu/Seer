 package jivko.test;
 
 import java.util.ArrayList;
 import java.util.List;
 import jivko.brain.movement.Command;
 import jivko.brain.movement.CommandsCenter;
 import jivko.util.ComPort;
 
 /**
  *
  * @author Sergii Smehov (smehov.com)
  */
 public class Commands implements Testable {
 
   private static String[] commandsToTest = {
    "DUMMY"
    ,"RIGHT_ARM_UP=500"
    ,"RIGHT_ARM_STOP=0"    
     ,"ROBOT_THINKING"    
     ,"HEAD_PAN_LEFT_45"
     ,"HEAD_PAN_RIGHT_45"
     ,"ROBOT_NORMAL"
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
     //ComPort newPort = new ComPort("/dev/ttyUSB1", 115200);
     //newPort.write("#7P1152T960\r\n");
     
     
     List<Command> commands = new ArrayList<>();
     
     for (String s : commandsToTest) {
       Command command = CommandsCenter.getInstance().getCommandEx(s);
       commands.add(command);
     }
     
     CommandsCenter.getInstance().executeCommandList(commands);
   }
 }
