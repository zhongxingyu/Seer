 package ru.ifmo.sunriser.view;
 
 import javax.microedition.lcdui.Command;
 
 /**
  *
  * @author vbatygin
  */
 public class CommandFactory {
 
     public static Command CANSEL_COMMAND = new TypedCommand("cansel", Command.OK, 0, CommandAction.CANSEL);
 
     public static Command BUILD_COMMAND = new TypedCommand("build", Command.OK, 0, CommandAction.CANSEL);
 
     public static Command REMOVE_COMMAND = new TypedCommand("remove", Command.OK, 0, CommandAction.CANSEL);
 
    public static Command INFO_COMMAND = new TypedCommand("remove", Command.OK, 0, CommandAction.INFO);
 
 
 }
