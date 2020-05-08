 /*******************************************************************************
  * Copyright (c) 2008, 2010 VMware Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *   VMware Inc. - initial contribution
  *******************************************************************************/
 
 package org.eclipse.virgo.kernel.shell.internal;
 
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.virgo.kernel.shell.Converter;
 import org.eclipse.virgo.kernel.shell.internal.converters.ConverterRegistry;
 import org.eclipse.virgo.kernel.shell.internal.parsing.ParsedCommand;
 import org.springframework.util.ReflectionUtils;
 
 
 /**
  * A <code>CommandInvoker</code> implementation that finds the command to invoke from a {@link CommandRegistry}.
  * <p />
  * 
  * <strong>Concurrent Semantics</strong><br />
  * 
  * Thread-safe.
  * 
  */
 final class CommandRegistryCommandInvoker implements CommandInvoker {
 
     private final CommandRegistry commandRegistry;
 
     private final ConverterRegistry converterRegistry;
 
     /**
      * @param commandRegistry
      * @param converterRegistry
      */
     CommandRegistryCommandInvoker(CommandRegistry commandRegistry, ConverterRegistry converterRegistry) {
         this.commandRegistry = commandRegistry;
         this.converterRegistry = converterRegistry;
     }
 
     public List<String> invokeCommand(ParsedCommand command) throws CommandNotFoundException, ParametersMismatchException {
         List<CommandDescriptor> commands = commandsOfCommandName(this.commandRegistry, command.getCommand());
 
         if (commands.isEmpty()) {
             throw new CommandNotFoundException();
         }
 
         String[] arguments = command.getArguments();
 
         String subcommandName = extractSubcommand(arguments);
 
         String[] subcommandArguments = extractSubcommandArguments(arguments);
 
         ParametersMismatchException lastException = null;
 
         for (CommandDescriptor commandDescriptor : commands) {
             List<String> objResult = null;
             String commandSubcommandName = commandDescriptor.getSubCommandName();
             String commandString = commandDescriptor.getCommandName();
             try {
                 if (commandSubcommandName != null && !commandSubcommandName.equals("")) {
                     if (isSubcommandMatch(commandSubcommandName, subcommandName)) {
                         commandString += " " + subcommandName;
                         objResult = attemptExecution(commandDescriptor, subcommandArguments);
                         return objResult;
                     }
                 } else {
                     objResult = attemptExecution(commandDescriptor, arguments);
                     return objResult;
                 }
             } catch (ParametersMismatchException e) {
                 lastException = new ParametersMismatchException("Command " + commandString + ": " + e.getMessage());
             }
         }
 
         if (lastException != null) {
             throw lastException;
         }
 
        throw new ParametersMismatchException("Command '" + command.getCommand() + "' expects a subcommand; try help vsh:" + command.getCommand());
     }
 
     private static boolean isSubcommandMatch(String commandSubcommandName, String subcommandName) {
         if (subcommandName == null) {
             return false;
         }
         if (commandSubcommandName != null) {
             if (commandSubcommandName.equals(subcommandName)) {
                 return true;
             }
         }
         return false;
     }
 
     private static String[] extractSubcommandArguments(String[] arguments) {
         if (arguments.length > 0) {
             String[] result = new String[arguments.length - 1];
             System.arraycopy(arguments, 1, result, 0, result.length);
             return result;
         }
         return null;
     }
 
     private static String extractSubcommand(String[] arguments) {
         if (arguments.length > 0) {
             return arguments[0];
         }
         return null;
     }
 
     @SuppressWarnings("unchecked")
     private List<String> attemptExecution(CommandDescriptor commandDescriptor, String[] arguments) throws ParametersMismatchException {
         Method method = commandDescriptor.getMethod();
 
         Object[] convertedArguments = convertArguments(method, arguments);
 
         ReflectionUtils.makeAccessible(method);
         return (List<String>) ReflectionUtils.invokeMethod(method, commandDescriptor.getTarget(), convertedArguments);
     }
 
     private static List<CommandDescriptor> commandsOfCommandName(final CommandRegistry commandRegistry, final String commandName) {
         List<CommandDescriptor> commands = new ArrayList<CommandDescriptor>();
         for (CommandDescriptor commandDescriptor : commandRegistry.getCommandDescriptors()) {
             if (commandDescriptor.getCommandName().equals(commandName)) {
                 commands.add(commandDescriptor);
             }
         }
         return commands;
     }
 
     private Object[] convertArguments(final Method method, final String[] arguments) throws ParametersMismatchException {
         Class<?>[] parameterTypes = method.getParameterTypes();
 
         if (parameterTypes.length != arguments.length) {
             throw new ParametersMismatchException("Incorrect number of parameters");
         }
 
         Object[] parameters = new Object[parameterTypes.length];
 
         for (int i = 0; i < parameterTypes.length; i++) {
 
             Object convertedParameter = convertArgument(arguments[i], parameterTypes[i]);
 
             if (convertedParameter != null) {
                 parameters[i] = convertedParameter;
             } else {
                 throw new ParametersMismatchException("Cannot convert parameter " + i + ".");
             }
         }
 
         return parameters;
     }
 
     private Object convertArgument(final String argument, Class<?> type) {
         Converter converter = this.converterRegistry.getConverter(type);
 
         if (converter == null) {
             return null;
         }
 
         try {
             return converter.convert(type, argument);
         } catch (Exception e) {
             return null;
         }
     }
 }
