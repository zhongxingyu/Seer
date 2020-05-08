 /*
 * utils - CommandLine.java - Copyright © 2008 David Roden
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package net.pterodactylus.util.cmdline;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import net.pterodactylus.util.validation.Validation;
 
 /**
  * Command-line parser.
  * <p>
  * This parser parses a {@link String} array containing the command-line
  * parameters into options as defined by
  * {@link #CommandLine(String[], Option...)}. Include code like the following in
  * your main startup method:
  *
  * <pre>
  * List&lt;Option&gt; options = new ArrayList&lt;Option&gt;();
  * options.add(new Option('h', &quot;help&quot;));
  * options.add(new Option('C', &quot;config-file&quot;, true));
  * CommandLine commandLine = new CommandLine(arguments, options);
  * </pre>
  *
  * After the command-line has been parsed successfully, querying it is quite
  * simple:
  *
  * <pre>
  * if (commandLine.getOption(&quot;h&quot;).isPresent()) {
  * 	showHelp();
  * 	return;
  * }
  * if (commandLine.getOption(&quot;C&quot;).isPresent()) {
  * 	String configFile = commandLine.getOption(&quot;C&quot;).getValue();
  * }
  * </pre>
  *
  * Additional arguments on the command line can be queried via the
  * {@link #getArguments()} method. A line like
  * <code>program -C config.txt file1.txt file2.txt</code> with the constructor
  * from above and the code below would result in the output below the code:
  *
  * <pre>
  * for (String argument : commandLine.getArguments()) {
  * 	System.out.println(argument);
  * }
  * </pre>
  *
  * Output:
  *
  * <pre>
  * file1.txt
  * file2.txt
  * </pre>
  *
  * @author <a href="mailto:bombe@pterodactylus.net">David ‘Bombe’ Roden</a>
  */
 public class CommandLine {
 
 	/** Mapping from short names to options. */
 	private final Map<Character, Option> shortNameOptions = new HashMap<Character, Option>();
 
 	/** Mapping from long names to options. */
 	private final Map<String, Option> longNameOptions = new HashMap<String, Option>();
 
 	/** The remaining arguments. */
 	private List<String> arguments = new ArrayList<String>();
 
 	/**
 	 * Creates a new command line and parses the given command-line arguments
 	 * according to the given options.
 	 *
 	 * @param commandLineArguments
 	 *            The command-line arguments
 	 * @param options
 	 *            The options
 	 * @throws CommandLineException
 	 *             if a command-line argument could not be parsed
 	 */
 	public CommandLine(String[] commandLineArguments, Collection<Option> options) throws CommandLineException {
 		this(commandLineArguments, options.toArray(new Option[options.size()]));
 	}
 
 	/**
 	 * Creates a new command line and parses the given command-line arguments
 	 * according to the given options.
 	 *
 	 * @param commandLineArguments
 	 *            The command-line arguments
 	 * @param options
 	 *            The options
 	 * @throws CommandLineException
 	 *             if a command-line argument could not be parsed
 	 */
 	public CommandLine(String[] commandLineArguments, Option... options) throws CommandLineException {
 		Validation.begin().isNotNull("commandLineArguments", commandLineArguments).check();
 		for (Option option : options) {
 			/* TODO - sanity checks */
 			if (option.getShortName() != 0) {
 				shortNameOptions.put(option.getShortName(), option);
 			}
 			if (option.getLongName() != null) {
 				longNameOptions.put(option.getLongName(), option);
 			}
 		}
 		int argumentCount = commandLineArguments.length;
 		boolean argumentsOnly = false;
 		List<Option> optionsNeedingParameters = new ArrayList<Option>();
 		for (int argumentIndex = 0; argumentIndex < argumentCount; argumentIndex++) {
 			String argument = commandLineArguments[argumentIndex];
 			if (!optionsNeedingParameters.isEmpty()) {
 				Option option = optionsNeedingParameters.remove(0);
 				option.setValue(argument);
 				continue;
 			}
 			if (argumentsOnly) {
 				arguments.add(argument);
 				continue;
 			}
 			if ("--".equals(argument)) {
 				argumentsOnly = true;
 				continue;
 			}
 			if (argument.startsWith("--")) {
 				String longName = argument.substring(2);
 				Option option = longNameOptions.get(longName);
 				if (option == null) {
 					throw new CommandLineException("unknown long name: " + longName);
 				}
 				if (option.needsParameter()) {
 					int equals = longName.indexOf('=');
 					if (equals == -1) {
 						optionsNeedingParameters.add(option);
 					} else {
 						option.setValue(longName.substring(equals + 1));
 					}
 				}
 				option.incrementCounter();
 				continue;
 			}
 			if (argument.startsWith("-")) {
 				String optionChars = argument.substring(1);
 				for (char optionChar : optionChars.toCharArray()) {
 					Option option = shortNameOptions.get(optionChar);
 					if (option == null) {
 						throw new CommandLineException("unknown short name: " + optionChar);
 					}
 					if (option.needsParameter()) {
 						optionsNeedingParameters.add(option);
 					}
 					option.incrementCounter();
 				}
 				continue;
 			}
 			arguments.add(argument);
 		}
 		if (!optionsNeedingParameters.isEmpty()) {
 			throw new CommandLineException("missing value for option " + optionsNeedingParameters.get(0));
 		}
 	}
 
 	/**
 	 * Returns the option with the given name. If there is no option with the
 	 * given short name, <code>null</code> is returned.
 	 *
 	 * @param name
 	 *            The short name of the option
 	 * @return The option, or <code>null</code> if no option could be found
 	 */
 	public Option getOption(char name) {
 		return shortNameOptions.get(name);
 	}
 
 	/**
 	 * Returns the option with the given name. If the name is longer than one
 	 * character and matches an option’s long name, that option is returned. If
 	 * the name is exactly one character long and matches an option’s short
 	 * name, that options is returned. Otherwise <code>null</code> is returned.
 	 *
 	 * @param name
 	 *            The long or short name of the option
 	 * @return The option, or <code>null</code> if no option could be found
 	 */
 	public Option getOption(String name) {
 		Validation.begin().isNotNull("name", name).check();
 		if ((name.length() > 1) && longNameOptions.containsKey(name)) {
 			return longNameOptions.get(name);
 		}
 		if ((name.length() == 1) && (shortNameOptions.containsKey(name.charAt(0)))) {
 			return shortNameOptions.get(name.charAt(0));
 		}
 		return null;
 	}
 
 	/**
 	 * Returns all remaining arguments from the original command-line arguments.
 	 *
 	 * @return The remaining arguments
 	 */
 	public String[] getArguments() {
 		return arguments.toArray(new String[arguments.size()]);
 	}
 
 }
