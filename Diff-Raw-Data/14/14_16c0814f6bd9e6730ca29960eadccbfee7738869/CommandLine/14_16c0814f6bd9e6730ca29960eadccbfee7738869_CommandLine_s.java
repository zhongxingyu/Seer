 /*
  * Copyright 2011-2014 Jeroen Meetsma - IJsberg Automatisering BV
  *
  * This file is part of Iglu.
  *
  * Iglu is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Lesser General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * Iglu is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public License
  * along with Iglu.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.ijsberg.iglu.server.invocation;
 
 import org.ijsberg.iglu.util.collection.ArraySupport;
 import org.ijsberg.iglu.util.misc.StringSupport;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  */
 public class CommandLine {
 	private String[] unitIdentifierSequence;
 	private Object[] arguments;
 
 	/**
 	 * Splits command-line in command and arguments.
 	 * Expects a command and 0 or more arguments like so:
 	 * command(argument1,argument2,argument3)
 	 *
 	 * @param line Command line
 	 * @return String list, #0 is the command, the rest are arguments
 	 */
 	public static String[] splitCommandLine(String line) {
 		return StringSupport.split(line, "()", "").toArray(new String[0]);
 	}
 
 	public static String[] splitArguments(String line) {
 
//		System.out.println("==x==> " + line);
//		System.out.println("==x==> " + StringSupport.split(line, ",", ""));

 		List<String> result = new ArrayList<String>();
		for(String s : StringSupport.split(line, ", ", "\"")) {
			result.add(s);
 		}



		//String[] result = StringSupport.split(line, ",", "").toArray(new String[0]);
//		System.out.println(result);
 		return result.toArray(new String[0]);
 	}
 
 	/**
 	 * Splits command in cluster ID, component ID and method name.
 	 *
 	 * @param line
 	 * @return
 	 */
 	public static String[] splitCommand(String line) {
 		return StringSupport.split(line, ".", "").toArray(new String[0]);
 	}
 
 
 	public CommandLine(String commandLine) {
 		String[] commandAndArguments = splitCommandLine(commandLine);
 		unitIdentifierSequence = splitCommand(commandAndArguments[0]);
 
 		if (commandAndArguments.length > 1) {
 			arguments = splitArguments(commandAndArguments[1]);
 		} else {
 			arguments = new String[0];
 		}
 
 		if (unitIdentifierSequence.length < 2) {
 			throw new IllegalArgumentException("can not process command line: '" + commandLine + "'\nmissing method");
 		}
 	}
 
 	public Object[] getArguments() {
 		return arguments;
 	}
 
 	public String[] getUnitIdentifierSequence() {
 		return unitIdentifierSequence;
 	}
 
 	public String toString() {
 		return ArraySupport.format(unitIdentifierSequence, ".") + "(" + ArraySupport.format(arguments, ",") + ")";
 	}
 
 }
