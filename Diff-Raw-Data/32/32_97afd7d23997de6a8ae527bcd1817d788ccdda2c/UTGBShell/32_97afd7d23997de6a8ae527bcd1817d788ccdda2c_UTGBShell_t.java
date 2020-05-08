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
 
import org.xerial.util.log.LogLevel;
import org.xerial.util.log.Logger;
import org.xerial.util.log.SimpleLogWriter;
import org.xerial.util.opt.*;

import java.io.File;
import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Properties;
 import java.util.Set;
 
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
 
 	public static class UTGBShellOption extends GlobalCommandOption {
 
 		@Option(symbol = "l", longName = "loglevel", description = "set log level: TRACE, DEBUG, INFO(default), WARN, ERROR, FATAL")
 		private LogLevel logLevel = null;
 
 		@Option(symbol = "d", longName = "projectDir", description = "specify the project directory (default = current directory)")
 		public String projectDir = ".";
 
 		@Option(symbol = "e", longName = "env", varName = "test|development|production", description = "switch the configuration file (default: development)")
 		public String environment = "development";
 
 	}
 
 	public static Set<String> getSubCommandNameSet() {
 		CommandLauncher l = createCommandLauncher(new UTGBShellOption());
 		return l.getCommandNameSet();
 	}
 
 	/**
 	 * Run UTGB Shell commands
 	 * 
	 * @param argLine
 	 * @throws Exception
 	 */
 	public static void runCommand(String argLine) throws Exception {
 		runCommand(argLine.split("[\\s]+"));
 	}
 
 	public static void runCommand(UTGBShellOption opt, String argLine) throws Exception {
 		runCommand(opt, argLine.split("[\\s]+"));
 	}
 
 	private static CommandLauncher createCommandLauncher(final UTGBShellOption opt) {
 		// Prepare the command launcher
 		CommandLauncher launcher = new CommandLauncher();
 		CommandHelpMessage message = new CommandHelpMessage();
 		// Set the default help message
 		message.defaultHeader = getProgramInfo();
 		message.defaultMessage = "type --help for a list of the available sub commands.";
 		launcher.setMessage(message);
 		// Set the global command options
 		launcher.setGlobalCommandOption(opt);
 
 		// Load commands (implementation of the org.xerial.util.opt.Command) in the specified packages
 		launcher.addCommandsIn("org.utgenome.core.cui");
 		launcher.addCommandsIn("org.utgenome.shell");
 
 		return launcher;
 	}
 
 	public static void runCommand(final UTGBShellOption opt, String[] args) throws Exception {
 
 		// Prepare the command launcher
 		CommandLauncher launcher = createCommandLauncher(opt);
 
 		// Set the event handler
 		launcher.addEventHandler(new CommandLauncherEventHandler() {
 			public void afterReadingGlobalOptions(GlobalCommandOption o) {
 				// Set the log level
 				if (opt.logLevel != null)
 					Logger.getRootLogger().setLogLevel(opt.logLevel);
 				Logger.getRootLogger().setLogWriter(new SimpleLogWriter(System.err));
 			}
 
 		});
 
 		// Launch the command
 		launcher.execute(args);
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
 
 	public static String getProgramInfo() {
 		return "UTGB Shell: version " + getVersion();
 	}
 
 	public static String getVersion() {
 		String version = "(unknown)";
 		try {
 			// load the pom.xml file copied as a resource in utgb-core.jar

 			InputStream pomIn = UTGBShell.class.getResourceAsStream("/META-INF/maven/org.utgenome/utgb-core/pom.properties");
 			try {
 				if (pomIn == null) {
 					// If utgb-core is referenced in the workspace scope, use the
 					// utgb-core/src/main/resources/utgb-core.properties, which is created when utgb-core is
 					// compiled
					pomIn = new FileInputStream(new File(System.getProperty("prog.home", ""), "VERSION"));
 				}
 				if (pomIn != null) {
 					Properties prop = new Properties();
 					prop.load(pomIn);
                    String propertyName = "version";
					version = prop.getProperty(propertyName, version).replaceAll("[=:]", "");
 				}
 			}
 			finally {
 				if (pomIn != null)
 					pomIn.close();
 			}
 		}
 		catch (IOException e) {
 			_logger.debug(e);
 		}
 		return version;
 	}
 }
