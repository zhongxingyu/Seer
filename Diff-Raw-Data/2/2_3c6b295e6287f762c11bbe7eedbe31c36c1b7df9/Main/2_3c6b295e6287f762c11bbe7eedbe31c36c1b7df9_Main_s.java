 /*
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *  http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing,
  * software distributed under the License is distributed on an
  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
  * KIND, either express or implied.  See the License for the
  * specific language governing permissions and limitations
  * under the License.
  */
 
 package org.apache.geronimo.gshell.cli;
 
 import org.apache.geronimo.gshell.ansi.ANSI;
 import org.apache.geronimo.gshell.application.ApplicationModelLocator;
 import org.apache.geronimo.gshell.application.settings.SettingsModelLocator;
 import org.apache.geronimo.gshell.clp.Argument;
 import org.apache.geronimo.gshell.clp.CommandLineProcessor;
 import org.apache.geronimo.gshell.clp.Option;
 import org.apache.geronimo.gshell.clp.Printer;
 import org.apache.geronimo.gshell.i18n.MessageSource;
 import org.apache.geronimo.gshell.i18n.ResourceBundleMessageSource;
 import org.apache.geronimo.gshell.io.IO;
 import org.apache.geronimo.gshell.model.application.ApplicationModel;
 import org.apache.geronimo.gshell.model.settings.SettingsModel;
 import org.apache.geronimo.gshell.notification.ExitNotification;
 import org.apache.geronimo.gshell.shell.Shell;
 import org.apache.geronimo.gshell.wisdom.builder.ShellBuilder;
 import org.apache.geronimo.gshell.wisdom.builder.ShellBuilderImpl;
 
 import java.util.List;
 import java.util.concurrent.atomic.AtomicReference;
 
 /**
  * Command-line bootstrap for GShell.
  *
  * @version $Rev$ $Date$
  */
 public class Main
 {
     //
     // NOTE: Do not use logging from this class, as it is used to configure
     //       the logging level with System properties, which will only get
     //       picked up on the initial loading of Log4j
     //
 
     private final IO io = new IO();
 
     private final MessageSource messages = new ResourceBundleMessageSource(getClass());
     
     //
     // TODO: Add flag to capture output to log file
     //       https://issues.apache.org/jira/browse/GSHELL-47
     //
 
     //
     // TODO: Add --application and --settings
     //
 
     //
     // TODO: Add --file <file>, which will run: source <file> 
     //
 
     @Option(name="-h", aliases={"--help"}, requireOverride=true)
     private boolean help;
 
     @Option(name="-V", aliases={"--version"}, requireOverride=true)
     private boolean version;
 
     @Option(name="-i", aliases={"--interactive"})
     private boolean interactive = true;
 
     private void setConsoleLogLevel(final String level) {
         System.setProperty("gshell.log.console.level", level);
     }
 
     @Option(name="-e", aliases={"--exception"})
     private void setException(boolean flag) {
     	if (flag) {
     		System.setProperty("gshell.show.stacktrace","true");
     	}
     }
     
     @Option(name="-d", aliases={"--debug"})
     private void setDebug(boolean flag) {
         if (flag) {
             setConsoleLogLevel("DEBUG");
             io.setVerbosity(IO.Verbosity.DEBUG);
         }
     }
 
     @Option(name="-X", aliases={"--trace"})
     private void setTrace(boolean flag) {
         if (flag) {
             setConsoleLogLevel("TRACE");
             io.setVerbosity(IO.Verbosity.DEBUG);
         }
     }
 
     @Option(name="-v", aliases={"--verbose"})
     private void setVerbose(boolean flag) {
         if (flag) {
             setConsoleLogLevel("INFO");
             io.setVerbosity(IO.Verbosity.VERBOSE);
         }
     }                                                 
 
     @Option(name="-q", aliases={"--quiet"})
     private void setQuiet(boolean flag) {
         if (flag) {
             setConsoleLogLevel("ERROR");
             io.setVerbosity(IO.Verbosity.QUIET);
         }
     }
 
     @Option(name="-c", aliases={"--commands"})
     private String commands;
 
     @Argument
     private List<String> commandArgs = null;
 
     @Option(name="-D", aliases={"--define"})
     private void setSystemProperty(final String nameValue) {
         assert nameValue != null;
 
         String name, value;
         int i = nameValue.indexOf("=");
 
         if (i == -1) {
             name = nameValue;
             value = Boolean.TRUE.toString();
         }
         else {
             name = nameValue.substring(0, i);
             value = nameValue.substring(i + 1, nameValue.length());
         }
         name = name.trim();
 
         System.setProperty(name, value);
     }
 
     @Option(name="-C", aliases={"--color"}, argumentRequired=true)
     private void enableAnsiColors(final boolean flag) {
         ANSI.setEnabled(flag);
     }
 
     @Option(name="-T", aliases={"--terminal"}, argumentRequired=true)
     private void setTerminalType(String type) {
         type = type.toLowerCase();
 
         //
         // FIXME: Provide an abstraction over the jline term stuff and its warts, and/or fork it and re-implement it to not be so broken
         //
 
         if ("unix".equals(type)) {
             type = "jline.UnixTerminal";
         }
         else if ("win".equals(type) || "windows".equals("type")) {
             type = "jline.WindowsTerminal";
         }
         else if ("false".equals(type) || "off".equals(type) || "none".equals(type)) {
             type = "jline.UnsupportedTerminal";
             
             //
             // HACK: Disable ANSI, for some reason UnsupportedTerminal reports ANSI as enabled, when it shouldn't
             //       as a temporary solution, could provide a Terminal instance which can delegate and fix warts like this
             //
             ANSI.setEnabled(false);
         }
 
         System.setProperty("jline.terminal", type);
     }
 
     public void boot(final String[] args) throws Exception {
         assert args != null;
 
         // Default is to be quiet
         setConsoleLogLevel("WARN");
 
         CommandLineProcessor clp = new CommandLineProcessor(this);
         clp.setStopAtNonOption(true);
         clp.process(args);
 
         // Setup a refereence for our exit code so our callback thread can tell if we've shutdown normally or not
         final AtomicReference<Integer> codeRef = new AtomicReference<Integer>();
         int code = ExitNotification.DEFAULT_CODE;
 
         Runtime.getRuntime().addShutdownHook(new Thread("GShell Shutdown Hook") {
             public void run() {
                 if (codeRef.get() == null) {
                     // Give the user a warning when the JVM shutdown abnormally, normal shutdown
                     // will set an exit code through the proper channels
 
                     if (!io.isSilent()) {
                         io.err.println();
                         io.err.println(messages.getMessage("warning.abnormalShutdown"));
                     }
                 }
 
                 io.flush();
             }
         });
 
         try {
             ShellBuilder builder = new ShellBuilderImpl();
             builder.setClassLoader(getClass().getClassLoader());
             builder.setIo(io);
 
             // Find our settings descriptor
             SettingsModel settingsModel = new SettingsModelLocator().locate();
             builder.setSettingsModel(settingsModel);
 
             // Find our application descriptor
             ApplicationModel applicationModel = new ApplicationModelLocator().locate();
             builder.setApplicationModel(applicationModel);
 
             // --help and --version need access to the application's information, so we have to handle these options late
             if (help|version) {
                 if (help) {
                     Printer printer = new Printer(clp);
                     printer.setMessageSource(messages);
                     printer.printUsage(io.out, applicationModel.getBranding().getProgramName());
                 }
                 else if (version) {
                     io.out.println(applicationModel.getVersion());
                     io.out.println();
                 }
 
                 io.out.flush();
 
                 throw new ExitNotification();
             }
 
             // Build the shell instance
             Shell gshell = builder.create();
 
             // clp gives us a list, but we need an array
             String[] _args = {};
             if (commandArgs != null) {
                commandArgs.toArray(new String[commandArgs.size()]);
             }
 
             if (commands != null) {
                 gshell.execute(commands);
             }
             else if (interactive) {
                 gshell.run(_args);
             }
             else {
                 gshell.execute(_args);
             }
         }
         catch (ExitNotification n) {
             code = n.code;
         }
 
         codeRef.set(code);
 
         System.exit(code);
     }
 
     public static void main(final String[] args) throws Exception {
         Main main = new Main();
         main.boot(args);
     }
 }
 
