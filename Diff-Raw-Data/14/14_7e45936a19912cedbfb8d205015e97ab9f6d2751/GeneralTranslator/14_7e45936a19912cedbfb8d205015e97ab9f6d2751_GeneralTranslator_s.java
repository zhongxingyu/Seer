 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council Daresbury Laboratory
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.jython.translator;
 
 import gda.jython.InterfaceProvider;
 
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * An abstract class to provide a translation facility for GDA scripting syntax.
  * <p>
  * Anything entered by the user (command line or script) should be passed through the translate method. If the syntax is
  * recognised, it is converted into 'real' jython so it may be passed to a Jython interpreter. Anything unrecognised, or
  * if an error occurs, then the string is returned untouched.
  */
 public class GeneralTranslator extends TranslatorBase implements Translator {
 	
 	private static final Logger logger = LoggerFactory.getLogger(GeneralTranslator.class);
 
 	static private final String symbols = "-+*/%><=,[]()";
 
 	/**
 	 * Translates a command identified by the translate() method. This ignores any spaces at start or end of line - it
 	 * assumes that translate() has removed any significant \n or \t characters.
 	 * 
 	 * @param thisGroup
 	 *            String
 	 * @return String
 	 */
 	@Override
 	public String translateGroup(String thisGroup) {
 		String originalGroup = thisGroup;
 		String prefix = "";
 		try {
 
 			// ignore comments
 			if (thisGroup.startsWith("#")) {
 				return thisGroup;
 			}
 
 			// remove part of line after comments
 			thisGroup = removeComments(thisGroup);
 
 			// tidy up []'s
 			thisGroup = GeneralTranslator.tidyBrackets(thisGroup);
 
 			// remove initial tabs into separate prefix string
 			while (thisGroup.startsWith(" ") || thisGroup.startsWith("\t")) {
 				prefix += thisGroup.substring(0, 1);
 				thisGroup = thisGroup.substring(1);
 			}
 
 			// split rest of line by space
 			String[] args = GeneralTranslator.splitGroup(thisGroup);
 
 			// if nothing in the string then return
 			if (args.length == 0) {
 				return prefix + thisGroup;
 			}
 
 			// assume first element in 'args' is the method, second is the name of the object and rest are the arguments
 			// of the method
 
 			// dynamically add a function to the extended syntax
 			else if (args[0].equals("alias")) {
 				// assume next arg is the name of the function
 				if (args.length == 2) {
 					aliases.add(args[1]);
 					thisGroup = "";
 				}
 			} else if (args[0].equals("vararg_alias")) {
 				// assume next arg is the name of the function
 				if (args.length == 2) {
 					vararg_aliases.add(args[1]);
 					thisGroup = "";
 				}
 			}
 			// prevent overwriting of scannables
 			else if (thisGroup.contains("=")) {
 				// extract the string we are interested in
 				String leftOfOperator = thisGroup.substring(0, thisGroup.indexOf("="));
 				String test = "'" + leftOfOperator.trim() + "'";
 
 				String eval = InterfaceProvider.getCommandRunner().evaluateCommand("dir()");
 				// test we are not trying to overwrite an aliased command
 				if (aliases.contains(leftOfOperator.trim()) || this.vararg_aliases.contains(leftOfOperator.trim())) {
 					if (eval.contains(test)){
 						thisGroup = "raise Exception(\"Trying to overwrite an aliased method: " + leftOfOperator.trim()
 						+ "\")";
 					}
 				}
 
 				// then test if our object is in the namespace
 				if (eval.contains(test)) {
 					// if it is, is it a Scannable?
 					String result = InterfaceProvider.getCommandRunner().evaluateCommand(
 							"isinstance(" + leftOfOperator + " ,Scannable)");
 
 					// if so, the raise an error rather than completing the
 					// code
 					if (result.equals("True")) {
 						thisGroup = "raise Exception(\"Trying to overwrite a Scannable: " + leftOfOperator.trim()
 								+ "\")";
 					}
 				}
 				// else do nothing
 			}
 			// else test for a dynamically added commands
 			else if (aliases.contains(args[0]) && args.length >= 1) {
 				args = splitGroup(removeTrailingComment(thisGroup));
 				thisGroup = args[0] + "(";
 
 				int i = 1;
 				for (; i < args.length; i++) {
 					thisGroup += args[i];
 					if (i < args.length - 1 && !thisGroup.endsWith("(") && !thisGroup.endsWith("[")){
 						if( i == args.length-2 ){
 							if( !args[i+1].startsWith(")") && !args[i+1].startsWith("]"))
 								thisGroup += ",";
 						}
 						else {
 							thisGroup += ",";
 						}
 					}
 				}
 
 				thisGroup += ")";
 			} else if (vararg_aliases.contains(args[0]) && args.length > 1) {
 				thisGroup = addBracketsToVarArgAlias(thisGroup);
 			} else if (vararg_aliases.contains(args[0]) && args.length == 1) {
 				args = splitGroup(removeTrailingComment(thisGroup));
 				thisGroup = args[0] + "()";
 			} else if (startsWithVarArgAlias(args[0])){
 				int bracketIndex = args[0].indexOf("(");
 				
 				if (!thisGroup.substring(bracketIndex + 1, bracketIndex + 2).equals("[")) {
 
 					String firstPart = args[0].substring(0, bracketIndex + 1);
 					String secondPart = "";
 					if (bracketIndex < args[0].length() + 1) {
 						secondPart = args[0].substring(bracketIndex + 1);
 					}
 					args[0] = firstPart + "[" + secondPart;
 
 					bracketIndex = args[args.length - 1].lastIndexOf(")");
 					firstPart = args[args.length - 1].substring(0, bracketIndex);
 					secondPart = args[args.length - 1].substring(bracketIndex);
 
 					args[args.length - 1] = firstPart + "]" + secondPart;
 
 					int i = 1;
 					thisGroup = args[0];
 					for (; i < args.length; i++) {
 						thisGroup += ",";
 						thisGroup += args[i];
 					}
 				}	
 			}
 			
 
 			if (thisGroup.startsWith("help(")) {
 				// this parses to a call to a Jython function defined in GDAJythonInterpreter.initialise
 				thisGroup = "_gda" + thisGroup;
 			} 
 
 		} catch (Exception e) {
 
 			if (!originalGroup.startsWith("#") && !originalGroup.endsWith("\\")) {
 				// if an error, return the original string
 				// and let jython interpreter handle any syntax errors
 				logger.debug("Error in Translator: " + originalGroup);
 			}
 
 			return originalGroup;
 		}
 		return prefix + thisGroup;
 	}
 
 	private String addBracketsToVarArgAlias(String thisGroup) {
 		String[] args;
 		args = splitGroup(removeTrailingComment(thisGroup));
 		thisGroup = args[0] + "([";
 
 		int i = 1;
 		for (; i < args.length; i++) {
 			thisGroup += args[i];
 			if (i < args.length - 1 && !thisGroup.endsWith("(") && !thisGroup.endsWith("[")){
 				if( i == args.length-2 ){
 					if( !args[i+1].startsWith(")") && !args[i+1].startsWith("]"))
 						thisGroup += ",";
 				}
 				else {
 					thisGroup += ",";
 				}
 			}
 		}
 
 		if (i == 1) {
 			thisGroup += "None";
 		}
 
 		thisGroup += "])";
 		return thisGroup;
 	}
 
 	private boolean startsWithVarArgAlias(String string) {
 		int index = string.indexOf("(");
 		if (index == -1){
 			return false;
 		}		
 		String firstPart = string.substring(0,index);
 		return vararg_aliases.contains(firstPart);
 	}
 
 	/**
 	 * Removes everything after any hash symbol
 	 * <p>
 	 * Be careful that
 	 * 
 	 * @param command
 	 * @return the command without the trailing comment
 	 */
 	protected static String removeTrailingComment(String command) {
 		if (command.contains("#")) {
 			return command.substring(0, command.indexOf("#"));
 		}
 		return command;
 	}
 
 	/**
 	 * inside pairs of brackets replaces spaces with commas. So when the command is later split by spaces, each bracket
 	 * group looks like a single element in the returned array.
 	 * 
 	 * @param original_command
 	 *            String
 	 * @return String
 	 */
 
 	protected static String tidyBrackets(String original_command) {
 
 		// return commands which are too short to tidy
 		if (original_command.length() <= 1) {
 			return original_command;
 		}
 
 		// return commands which look like Python list comprehensions
 		if (Pattern.matches(".*?\\[.*?for.*?in.*?\\].*?", original_command)) {
 			return original_command;
 		}
 
 		String command = original_command;
 		String commasAdded = "";
 		try {
 			int bracketDepth = 0;
 			// int braceDepth = 0; // too difficult to distinguish between operators, functions and variables inside()'s
 			// so do not auto add commas within these
 			boolean insideQuote = false;
 			boolean insideSingleQuote = false;
 			boolean insideDoubleQuote = false;
 			for (int i = 0; i < command.length(); i++) {
 				String thisElement = command.substring(i, i + 1);
 				// record if a pair of []s opens
 				if (thisElement.equals("[")) {
 					bracketDepth++;
 					commasAdded += thisElement;
 				}
 				// record if a pair of []s closes
 				else if (thisElement.equals("]")) {
 					bracketDepth--;
 					commasAdded += thisElement;
 				}
 				// record if a pair of "s opens or closes
 				else if (thisElement.equals("\"")) {
 					if (insideDoubleQuote) {
 						insideDoubleQuote = false;
 					} else {
 						insideDoubleQuote = true;
 					}
 					commasAdded += thisElement;
 				}
 				// record if a pair of `s opens or closes
 				else if (thisElement.equals("`")) {
 					if (insideQuote) {
 						insideQuote = false;
 					} else {
 						insideQuote = true;
 					}
 					commasAdded += thisElement;
 				}
 				// record if a pair of 's opens or closes
 				else if (thisElement.equals("'")) {
 					if (insideSingleQuote) {
 						insideSingleQuote = false;
 					} else {
 						insideSingleQuote = true;
 					}
 					commasAdded += thisElement;
 				}
 				// else if in []s but not in a quote and its a space
 				else if (!(insideQuote || insideSingleQuote || insideDoubleQuote)
 						&& (/* braceDepth >= 1 || */bracketDepth >= 1) && thisElement.equals(" ")) {
 
 					// if space is between ] and [, add a comma
 					if (previousPart(command, i) == ']' && nextPart(command, i) == '[') {
 						commasAdded += ",";
 					}
 
 					// ignoring spaces, if neither the previous nor next non-whitespace charactor is a comma or symbol,
 					// then add a comma
 					else if (!isNextPartASymbol(command, i) && !isPreviousPartASymbol(command, i)) {
 						commasAdded += ",";
 					} else {
 						commasAdded += " ";
 					}
 				}
 				// else simply add the next piece of the string
 				else {
 					commasAdded += thisElement;
 				}
 			}
 		} catch (Exception ex) {
 			return original_command;
 		}
 
 		return commasAdded;
 
 	}
 
 	/**
 	 * Ignoring whitespace, the is next character in the string after currentlocation an operator symbol or a comma?
 	 * 
 	 * @param string
 	 * @param currentlocation
 	 * @return boolean
 	 */
 	static boolean isNextPartASymbol(String string, int currentlocation) {
 		char nextPart = nextPart(string, currentlocation);
 		return StringUtils.contains(symbols, nextPart);
 	}
 
 	/**
 	 * Ignoring whitespace, the is previous character in the string before currentlocation an operator symbol or a
 	 * comma?
 	 * 
 	 * @param string
 	 * @param currentlocation
 	 * @return boolean
 	 */
 	static boolean isPreviousPartASymbol(String string, int currentlocation) {
 		char previousPart = previousPart(string, currentlocation);
 		return StringUtils.contains(symbols, previousPart);
 	}
 	
 	static char nextPart(String string, int currentlocation) {
 		String rightOfStart = StringUtils.stripToEmpty(StringUtils.substring(string, currentlocation));
 		char nextPart = rightOfStart.charAt(0);
 		return nextPart;
 	}
 	
 	static char previousPart(String string, int currentlocation) {
 		String leftOfStart = StringUtils.stripToEmpty(StringUtils.substring(string, 0, currentlocation));
 		char previousPart = leftOfStart.charAt(leftOfStart.length() - 1);
 		return previousPart;
 	}
 	
 	protected String removeComments(String command) {
 		int dblquoteLocation = command.lastIndexOf("\"");
 		int quoteLocation = command.lastIndexOf("'");
 
 		if (dblquoteLocation > quoteLocation) {
 			quoteLocation = dblquoteLocation;
 		}
 
 		int commentLocation = command.lastIndexOf("#");
 
 		if (commentLocation > quoteLocation) {
 			return command.substring(0, commentLocation);
 		}
 		return command;
 	}
 
 	public static String[] splitGroup(String during) {
 		String	[] output = new String[0];
 		StringBuffer buf = new StringBuffer();
 		int index=0;
 		String [] quoteTypes = new String[]{"'''", "r\"","r\'","\'","\""}; //put most complex first
 		while(index < during.length()){
 			if( during.regionMatches(index, " ", 0, 1) || during.regionMatches(index, ",", 0, 1)){
 				if(buf.length()>0){
 					output = (String [])ArrayUtils.add(output, buf.toString());
 					buf = new StringBuffer();
 				}
 				index++;
 				continue;
 			}
 			boolean quoteTypeFound=false;
 			for( String quoteType : quoteTypes ){
 				if( during.regionMatches(index, quoteType, 0, quoteType.length()) ){
 					if(buf.length()>0){
 						output = (String [])ArrayUtils.add(output, buf.toString());
 						buf = new StringBuffer();
 					}
 					buf.append(quoteType);
 					index+=quoteType.length();
 					//start of quote so go up to the matching quote - allowing for escaped versions
 					String endQuote=quoteType;
 					if( endQuote == "r\"")
 						endQuote= "\"";
 					if( endQuote == "r\'")
 						endQuote= "\'";
 					while(index < during.length()){
 						if( during.regionMatches(index, endQuote, 0, endQuote.length()) ){
 							buf.append( endQuote);
 							index+=endQuote.length();
 							output = (String [])ArrayUtils.add(output, buf.toString());
 							buf = new StringBuffer();
 							break;
 						}
 						char	charAt = during.charAt(index);
 						buf.append(during.charAt(index));
 						index++;
 						if( charAt == '\\'){
 							if( index <during.length()){
 								buf.append(during.charAt(index));
 								index++;
 							}
 						}
 					}
 					if( buf.length() > 0){
 						output = (String [])ArrayUtils.add(output, buf.toString());
 						buf = new StringBuffer();
 					}
 					quoteTypeFound=true;
 					break;
 				}
 			}
 			if(quoteTypeFound)
 				continue;
 			//add to StringBuffer
 			char	charAt = during.charAt(index);
 			buf.append(charAt);
 			index++;
 			if(charAt == '\\'){
 				if( index <during.length()){
 					buf.append(during.charAt(index));
 					index++;
 				}
 			}
 		}
 		if(buf.length()>0){
 			output = (String [])ArrayUtils.add(output, buf.toString());
 			buf = new StringBuffer();
 		}
 		//at end move over buf contents
 		return output;
 	}
 
 	@Override
 	public String getHelpMessage() {
 		String helpString = "";
 
 		helpString += "Available console commands in addition to Jython syntax:\\n";
 		helpString += "* help\\t-\\tprints this message\\n";
 		helpString += "* pos pseudoDevice\t-\tgets the current position of the object\\n";
 		helpString += "* pos pseudoDevice new_position\t-\tmoves the object to the new position\\n";
 		helpString += "* inc pseudoDevice increment\t-\tincremental move by the given amount\\n";
 		helpString += "* upos pseudoDevice new_position\t-\tpos command without print out during the move\\n";
 		helpString += "* uinc pseudoDevice increment\t-\tinc command without print out during the move\\n";
 		helpString += "* list\t-\tlist all the interfaces of existing objects\\n";
 		helpString += "* list interfaceName\t-\tlists all the objects of the given type (interface).  E.g. OE, Detector\\n";
 		helpString += "* list_defaults\t-\tlists all objects which would be used in a scan by default\\n";
 		helpString += "* add_default pseudoDevice\t-\tadd an object to the defaults list\\n";
 		helpString += "* remove_default pseudoDevice\t-\tremove an object from the defaults list\\n";
 		helpString += "* scan pseudoDevice start stop step [pseudoDevice2] [start] [[stop] step]\t-\tautomated movement of a group of pseudoDevices in concurrent steps, with data collected after each step\\n";
 		helpString += "* testscan pseudoDevice start stop step [pseudoDevice2] [start] [[stop] step]\t-\tas scan, except does not move anything and justs validates the parameters.\\n";
 		helpString += "* cscan pseudoDevice [centroid] width step [pseudoDevice2] [centroid] [width]\t-\tas scan, except uses different inputs\\n";
 		helpString += "* pscan pseudoDevice start step no_points [pseudoDevice2] start [step]\t-\tas scan, except uses different inputs\\n";
 		helpString += "* gscan pseudoDevice start stop step [pseudoDevice2] [start] [stop] [step]\t-\tgrid scan in which each dimension is moved separately.  Data is collected after each step.\\n";
 		helpString += "* tscan countertimer, numberOfPoints, pauseTime, collectTime\t-\tTime scan in which the positions of a list of pseudoDevices are collected at regular intervals. If no numberOfPoints supplied then the scan will not stop until halted.\\n";
 		helpString += "* timescan numberOfPoints, pauseTime, [collectTime]\t-\tTime scan in which the positions of a list of pseudoDevices are collected at regular intervals.\\n";
 		helpString += "* pause\t-\tduring a script, checks to see if the pause/resume button has been pressed\\n";
 		helpString += "* watch pseudoDevice\t-\tadds the scannable to the watch sub-panel in the terminal panel.\\n";
 		helpString += "* level pseudoDevice [value]\t-\tif value is declared then sets move order (level) of the scannable, else returns the current level.\\n";
 		helpString += "* alias functionName\t-\twhere functionName is a function in the global namespace.  This dynamically adds a function to the extended syntax.\\n";
 		helpString += "* run scriptName\t-\truns the named script.\\n";
 		helpString += "* record [on|off] \t-\tstarts/stops recording all terminal output to a file placed in the scripts directory\\n";
 
 		return helpString;
 	}
 
 	@Override
 	public void addAliasedCommand(String commandName) {
 		aliases.add(commandName);
 	}
 
 	@Override
 	public void addAliasedVarargCommand(String commandName) {
 		vararg_aliases.add(commandName);
 	}
 
 	@Override
 	public boolean ignoreRestOfLine(String thisGroup) {
		boolean containsComment = thisGroup.contains("#");
		if( containsComment)
			return true;
		return false;
 	}
 }
