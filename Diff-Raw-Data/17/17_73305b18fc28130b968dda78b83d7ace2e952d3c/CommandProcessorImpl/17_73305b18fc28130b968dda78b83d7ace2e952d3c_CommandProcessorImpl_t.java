 package com.kremerk.commandprocessor;
 
 import java.lang.reflect.Method;
 import java.util.HashMap;
 
 import org.json.JSONArray;
 
 import com.kremerk.commandprocessor.exception.CommandProcessorException;
 
 public class CommandProcessorImpl implements CommandProcessor {
 
     public void addCommandSet(CommandSet commandSet) {
         commands.put(commandSet.getClass().getSimpleName(), commandSet);
     }
 
     public JSONArray processCommand(String commandSetName, String commandName, String... parameters) throws CommandProcessorException {
             return (JSONArray) _processCommand(commandSetName, commandName, parameters);
     }
 
     @Override
     public byte[] processBinaryCommand(String commandSetName, String commandName, String... parameters) throws CommandProcessorException {
         return (byte[]) _processCommand(commandSetName, commandName, parameters);
     }
 
     private Object _processCommand(String commandSetName, String commandName, String... parameters) throws CommandProcessorException {
         try {
             CommandSet commandSet = commands.get(commandSetName);
            if(commandSet == null) {
            	throw new CommandProcessorException("Command set " + commandSetName + " is null, did you add it to the command processor?");
            }
             Method command = commandSet.getClass().getDeclaredMethod(commandName, String[].class);
            if(command == null) {
            	throw new CommandProcessorException("Command " + commandName + " could not be found in the command set " + commandSetName + ", is it defined correctly?");
            }
             Object[] o = new Object[1];
             o[0] = parameters;
             return command.invoke(commandSet, o);
         }
         catch (Exception e) {
             throw new CommandProcessorException("There was an error getting the command to run.", e);
         }
     }
 
     private HashMap<String, CommandSet> commands = new HashMap<String, CommandSet>();
 
 }
