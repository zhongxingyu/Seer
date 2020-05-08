 /*--------------------------------------------------------------------------
  *  Copyright 2008 utgenome.org
  *
  *  Licensed under the Apache License, Version 2.0 (the "License");
  *  you may not use this file except in compliance with the License.
  *  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *  Unless required by applicable law or agreed to in writing, software
  *  distributed under the License is distributed on an "AS IS" BASIS,
  *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *  See the License for the specific language governing permissions and
  *  limitations under the License.
  *--------------------------------------------------------------------------*/
 //--------------------------------------
 // utgb-shell Project
 //
 // UTGBShell.java 
 // Since: Jan 8, 2008
 //
 // $URL$ 
 // $Author$
 //--------------------------------------
 package org.utgenome.shell;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.lang.reflect.Modifier;
 import java.util.List;
 import java.util.Properties;
 import java.util.Set;
 import java.util.TreeMap;
 
 import org.utgenome.shell.Create.OverwriteMode;
 import org.xerial.util.FileResource;
 import org.xerial.util.ResourceFilter;
 import org.xerial.util.StringUtil;
 import org.xerial.util.io.VirtualFile;
 import org.xerial.util.log.LogLevel;
 import org.xerial.util.log.Logger;
 import org.xerial.util.log.SimpleLogWriter;
 import org.xerial.util.opt.Argument;
 import org.xerial.util.opt.Option;
 import org.xerial.util.opt.OptionParser;
 import org.xerial.util.opt.OptionParserException;
 
 /**
  * A command line client entry point
  * 
  * @author leo
  * 
  */
 public class UTGBShell {
 
 	static {
 	}
 
 	private static Logger _logger = Logger.getLogger(UTGBShell.class);
 
 	private static TreeMap<String, UTGBShellCommand> subCommandTable = new TreeMap<String, UTGBShellCommand>();
 
 	/**
 	 * search sub commands from the this package (org.utgenome.shell)
 	 */
 	static void searchSubCommands() {
 		String shellPackage = UTGBShell.class.getPackage().getName();
 		List<VirtualFile> classFileList = FileResource.listResources(shellPackage, new ResourceFilter() {
 			public boolean accept(String resourcePath) {
 				return resourcePath.endsWith(".class");
 			}
 		});
 		for (VirtualFile vf : classFileList) {
 			String logicalPath = vf.getLogicalPath();
 			int dot = logicalPath.lastIndexOf(".");
 			if (dot <= 0)
 				continue;
 			String className = shellPackage + "." + logicalPath.substring(0, dot).replaceAll("/", ".");
 			try {
 				Class<?> c = Class.forName(className, false, UTGBShell.class.getClassLoader());
 				if (!Modifier.isAbstract(c.getModifiers()) && UTGBShellCommand.class.isAssignableFrom(c)) {
 					// found a sub command class
 					UTGBShellCommand subCommand = (UTGBShellCommand) c.newInstance();
 					if (subCommand == null)
 						continue;
 					subCommandTable.put(subCommand.name(), subCommand);
 				}
 			}
 			catch (ClassNotFoundException e) {
 				continue;
 			}
 			catch (InstantiationException e) {
 				_logger.error(e);
 			}
 			catch (IllegalAccessException e) {
 				_logger.error(e);
 			}
 		}
 	}
 
 	static {
 		// search the all available sub commands
 		searchSubCommands();
 
 		// System.setProperty("com.apple.eawt.CocoaComponent.CompatibilityMode", "false");
 	}
 
 	public static class UTGBShellOption {
 
 		@Option(symbol = "h", longName = "help", description = "display help message")
 		private boolean displayHelp = false;
 
 		@Option(symbol = "v", longName = "version", description = "display version")
 		private boolean displayVersion = false;
 
 		@Argument(index = 0, required = false)
 		private String subCommand = null;
 
 		@Option(symbol = "l", longName = "loglevel", description = "set log level: TRACE, DEBUG, INFO(default), WARN, ERROR, FATAL")
 		private LogLevel logLevel = null;
 
 		@Option(symbol = "d", longName = "projectDir", description = "specify the project directory (default = current directory)")
 		public String projectDir = ".";
 
 		@Option(symbol = "e", longName = "env", varName = "test|development|production", description = "switch the configuration file (default: development)")
 		public String environment = "development";
 
 		@Option(symbol = "y", description = "(non-interactive mode) answer yes to all questions")
 		public boolean answerYes = false;
 
 	}
 
 	public static Set<String> getSubCommandNameSet() {
 		return subCommandTable.keySet();
 	}
 
 	/**
 	 * Run UTGB Shell commands
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void runCommand(String argLine) throws Exception {
 		runCommand(argLine.split("[\\s]+"));
 	}
 
 	public static void runCommand(UTGBShellOption opt, String argLine) throws Exception {
 		runCommand(opt, argLine.split("[\\s]+"));
 	}
 
 	public static void runCommand(UTGBShellOption opt, String[] args) throws Exception {
 
 		OptionParser optionParser = new OptionParser(opt);
 		optionParser.setIgnoreUnknownOption(true);
 
 		optionParser.parse(args);
 		String[] subCommandArgumetns = optionParser.getUnusedArguments();
 
 		if (opt.logLevel != null)
 			Logger.getRootLogger().setLogLevel(opt.logLevel);
 
 		Logger.getRootLogger().setLogWriter(new SimpleLogWriter(System.err));
 
 		if (opt.answerYes) {
 			ScaffoldGenerator.overwriteMode = OverwriteMode.YES_TO_ALL;
 		}
 
 		if (opt.subCommand != null) {
 			// go to sub command processing
 			UTGBShellCommand subCommand = subCommandTable.get(opt.subCommand);
 
 			if (subCommand != null) {
 				// Use the specified option holder for binding command-line parameters. If no option holder is
 				// given, use the sub command instance itself.
 				Object optionHolder = subCommand.getOptionHolder();
 				if (optionHolder == null)
 					optionHolder = subCommand;
 
 				OptionParser subCommandParser = new OptionParser(optionHolder);
 				subCommandParser.setIgnoreUnknownOption(true);
 				try {
 					subCommandParser.parse(subCommandArgumetns);
 					if (opt.displayHelp) {
 						String helpFile = String.format("help-%s.txt", subCommand.name());
 						System.out.println(loadUsage(helpFile));
 						subCommandParser.printUsage();
 						return;
 					}
 					else {
 						// copy the rest of the command line arguments
 						subCommand.execute(opt, subCommandArgumetns);
 						return;
 					}
 				}
 				catch (OptionParserException e) {
 					System.err.println(e.getMessage());
 					return;
 				}
 
 			}
 			else {
 				System.err.println("unknown subcommand: " + opt.subCommand);
 			}
 		}
 		else {
 			if (opt.displayHelp) {
 				// display help message
 				System.out.println(getProgramInfo());
 				BufferedReader helpReader = FileResource.open(UTGBShell.class, "help-message.txt");
 				String line;
 				while ((line = helpReader.readLine()) != null)
 					System.out.println(line);
 				// list command line options
 				optionParser.printUsage();
 				// list all sub commands
 				System.out.println("[sub commands]");
 				for (String subCommandName : subCommandTable.keySet()) {
 					UTGBShellCommand sc = subCommandTable.get(subCommandName);
 					System.out.format("  %-10s\t%s", subCommandName, sc.getOneLinerDescription());
 					System.out.println();
 				}
 				return;
 			}
 			if (opt.displayVersion) {
 				System.out.println(getProgramInfo());
 				return;
 			}
 		}
 
 		// display a short help message
 		System.out.println(getProgramInfo());
 		System.out.println("type --help for a list of the available sub commands.");
 
 	}
 
 	/**
 	 * Run UTGB Shell commands
 	 * 
 	 * @param args
 	 * @throws Exception
 	 */
 	public static void runCommand(String[] args) throws Exception {
 		runCommand(new UTGBShellOption(), args);
 	}
 
 	/**
 	 * Run UTGB Shell command. This method will terminates JVM with return code -1 when some error is observed. Thus, to
 	 * invoke UTGB Shell command inside the Java program, use {@link #runCommand(String[])} method, which does not
 	 * terminate the JVM.
 	 * 
 	 * 
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		try {
 			runCommand(args);
 		}
 		catch (UTGBShellException e) {
 			System.err.println(e.getMessage());
 			System.exit(1); // return error code
 		}
 		catch (OptionParserException e) {
 			System.err.println(e.getMessage());
 			System.exit(1); // return error code
 		}
 		catch (Exception e) {
 			e.printStackTrace(System.err);
 			System.exit(1); // return error code
 		}
 		catch (Error e) {
 			e.printStackTrace(System.err);
 			System.exit(1); // return error code
 		}
 	}
 
 	public static String loadUsage(String helpFileName) {
 		// display help messages
 		StringBuilder out = new StringBuilder();
 		try {
 			BufferedReader reader = FileResource.open(Create.class, helpFileName);
 			String line;
			if (reader == null)
				return "";
 			while ((line = reader.readLine()) != null) {
 				out.append(line);
 				out.append(StringUtil.NEW_LINE);
 			}
 			return out.toString();
 		}
 		catch (IOException e) {
 			_logger.warn(String.format("%s is not found in the org.utgenome.shell package", helpFileName));
 			return "";
 		}
 	}
 
 	public static String getProgramInfo() {
 		return "UTGB Shell: version " + getVersion();
 	}
 
 	public static String getVersion() {
 		String version = "(unknown)";
 		try {
 			// load the pom.xml file copied as a resource
 			InputStream pomIn = UTGBShell.class.getResourceAsStream("/META-INF/maven/org.utgenome/utgb-core/pom.properties");
 			if (pomIn != null) {
 				Properties prop = new Properties();
 				prop.load(pomIn);
 				version = prop.getProperty("version", version);
 			}
 		}
 		catch (IOException e) {
 			_logger.debug(e);
 		}
 		return version;
 	}
 }
