 package org.diagnoser.model.internal;
 
 import org.diagnoser.model.internal.deviation.DeviationAtTime;
 import org.diagnoser.model.internal.exception.InvalidCommand;
 
 import java.security.InvalidParameterException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: eatttth
  * Date: 2/4/13
  * Time: 8:04 PM
  * To change this template use File | Settings | File Templates.
  */
 public class CellParser {
     public static HazidElement parse(String cell) throws InvalidCommand {
 
        String command = (cell.indexOf(";") != -1?cell.substring(0,cell.indexOf(";")).toUpperCase() : cell);
         String rest = (cell.length() > command.length() ? cell.substring(command.length()+1,cell.length()) : "");
 
         if (command.equals("REF")) {
             return new HazidRef(rest.split(";")[0],rest.split(";")[1]);
         } else if (command.equals("ROOTCAUSE")) {
             return new RootCause(rest);
         } else if (command.equals("EARLIER")) {
             return new DeviationAtTime(KeyWord.createEarlier(),rest);
         } else if (command.equals("LATER")) {
             return new DeviationAtTime(KeyWord.createLater(),rest);
         } else if (command.equals("NEVER-HAPPENED")) {
             return new DeviationAtTime(KeyWord.createNeverHappened(),rest);
         } else if (command.equals("GREATER")) {
             checkIfOutputArrayIsSingle(rest);
             return new DeviationAtTime(KeyWord.createGreater(EventParser.extractOutputMap(rest).keySet().iterator().next()),rest);
         } else if (command.equals("SMALLER")) {
             checkIfOutputArrayIsSingle(rest);
             return new DeviationAtTime(KeyWord.createSmaller(EventParser.extractOutputMap(rest).keySet().iterator().next()),rest);
         } else if (command.equals("NOTAVAILABLE")) {
             return new NotAvailable();
         } else {
             throw new InvalidCommand(command);
         }
 
     }
 
     private static void checkIfOutputArrayIsSingle(String part) throws InvalidCommand {
         if (EventParser.countOutputSize(part)!=1) {
             throw new InvalidCommand("Invalid output size in quantitative deviation, should be 1, was " + EventParser.countOutputSize(part));
         }
 
     }
 
 
 }
