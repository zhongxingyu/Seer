 /* AWE - Amanzi Wireless Explorer
  * http://awe.amanzi.org
  * (C) 2008-2009, AmanziTel AB
  *
  * This library is provided under the terms of the Eclipse Public License
  * as described at http://www.eclipse.org/legal/epl-v10.html. Any use,
  * reproduction or distribution of the library constitutes recipient's
  * acceptance of this agreement.
  *
  * This library is distributed WITHOUT ANY WARRANTY; without even the
  * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  */
 
 package org.amanzi.neo.loader.etsi.commands;
 
 import java.util.HashMap;
 
 
 /**
  * Package of ETSI command
  * 
  * @author Lagutko_N
  * @since 1.0.0
  */
 public class ETSICommandPackage {
 	
 	/*
 	 * Map with commands
 	 */
 	private static HashMap<String, AbstractETSICommand> commandsMap = new HashMap<String, AbstractETSICommand>();
 	
 	static {
 		registerCommand(new CCI());
 		registerCommand(new CBS());
 		registerCommand(new CSQ());
 		registerCommand(new CNUM());
 		registerCommand(new CTSDC());
 		registerCommand(new ATD());
 		registerCommand(new ATH());
 		registerCommand(new ATA());
 		registerCommand(new UNSOLICITED());
 	}
 	
 	/**
 	 * Registers command
 	 * 
 	 * @param command ETSI command
 	 */
 	private static void registerCommand(AbstractETSICommand command) {
 		commandsMap.put(command.getName(), command);
 	}
 	
 	/**
 	 * Returns a command by it's name
 	 *
 	 * @param commandName name of command
 	 * @return command
 	 */
 	public static AbstractETSICommand getCommand(String commandName, CommandSyntax syntax) {
 		if (syntax == CommandSyntax.EXECUTE) {
 			for (String singleCommandName : commandsMap.keySet()) {
 				if (commandName.startsWith(singleCommandName)) {
 					return commandsMap.get(singleCommandName);
 				}
 			}
 			return null;
 		}
 		else {
			return commandsMap.get(commandName);
 		}
 	}
 	
 	public static CommandSyntax getCommandSyntax(String commandName) {
 		if (commandName.contains("?")) {
 			return CommandSyntax.READ;
 		}
 		if (commandName.contains("=")) {
 			return CommandSyntax.SET;
 		}
 		return CommandSyntax.EXECUTE;
 	}
 	
 	/**
 	 * Checks is it ETSI command
 	 *
 	 * @param commandName name of command
 	 * @return is it ETSI command
 	 */
 	public static boolean isETSICommand(String commandName) {
 		return commandName.toUpperCase().startsWith("AT");
 	}
 	
 	/**
 	 * Returns clear name of Command
 	 *
 	 * @param commandName name of command
 	 * @return clear name
 	 */
 	public static String getRealCommandName(String commandName) {
 		//if it's get syntax than command name should contain ?
 		int index = commandName.indexOf("?");
 		if (index < 0) {
 			//if it's set syntax than command name should contain =
 			index = commandName.indexOf("=");
 		}
 		if (index > 0) {
 			return commandName.substring(0, index);
 		}
 		else {
 			return commandName;
 		}
 	}
 
 }
