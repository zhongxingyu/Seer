 /*
  * RapidContext <http://www.rapidcontext.com/>
  * Copyright (c) 2007-2010 Per Cederberg. All rights reserved.
  *
  * This program is free software: you can redistribute it and/or
  * modify it under the terms of the BSD license.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
  *
  * See the RapidContext LICENSE.txt file for more details.
  */
 
 package org.rapidcontext.app;
 
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintWriter;
 import java.util.Properties;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.DefaultParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.lang.SystemUtils;
 import org.rapidcontext.app.ui.ControlPanel;
 import org.rapidcontext.util.ClassLoaderUtil;
 import org.rapidcontext.util.FileUtil;
 
 /**
  * The application start point, handling command-line parsing and
  * launching the application in the appropriate mode.
  *
  * @author   Per Cederberg
  * @version  1.0
  */
 public class Main {
 
     /**
      * The class logger.
      */
     private static final Logger LOG =
         Logger.getLogger(Main.class.getName());
 
     /**
      * The command-line usage information.
      */
     public static final String USAGE =
         "Usage: [1] rapidcontext [--app] [<options>]\n" +
         "       [2] rapidcontext --server [<options>]\n" +
         "       [3] rapidcontext [--script] [<options>] [<procedure> [<arg1> ...]]\n" +
         "\n" +
         "Alternative [1] is assumed when no procedure is specified.\n" +
         "Alternative [3] is assumed when a procedure is specified.";
 
     // Static initializer (fix for Mac UI)
     static {
         String str = "com.apple.mrj.application.apple.menu.about.name";
         System.setProperty(str, "RapidContext");
         System.setProperty("apple.awt.brushMetalLook", "true");
     }
 
     /**
      * Application entry point.
      *
      * @param args           the command-line parameters
      */
     public static void main(String[] args) {
         Options      options = new Options();
         OptionGroup  grp;
         Option       opt;
         CommandLine  cli;
 
         // Create main command-line options
         grp = new OptionGroup();
         opt = new Option(null, "app", false, "Launch in interactive application mode.");
         grp.addOption(opt);
         opt = new Option(null, "server", false, "Launch in server mode.");
         grp.addOption(opt);
         opt = new Option(null, "script", false, "Launch in script execution mode.");
         grp.addOption(opt);
         options.addOptionGroup(grp);
 
         // Create other command-line options
         options.addOption("h", "help", false, "Displays this help message,");
         opt = new Option("l", "local", true, "Use a specified local app directory.");
         opt.setArgName("dir");
         options.addOption(opt);
         opt = new Option("p", "port", true, "Use a specified port number (non-script mode).");
         opt.setArgName("number");
         options.addOption(opt);
         opt = new Option("d", "delay", true, "Add a delay after each command (script mode).");
         opt.setArgName("secs");
         options.addOption(opt);
         options.addOption("t", "trace", false, "Print detailed execution trace (script mode).");
         opt = new Option("u", "user", true, "Authenticate as a another user (script mode).");
         opt.setArgName("name");
         options.addOption(opt);
         options.addOption(null, "stdin", false, "Read commands from stdin (script mode).");
         opt = new Option("f", "file", true, "Read commands from a file (script mode).");
         opt.setArgName("file");
         options.addOption(opt);
 
         // Parse command-line arguments
         try {
             cli = new DefaultParser().parse(options, args);
             if (cli.hasOption("help")) {
                 exit(options, null);
             }
             args = cli.getArgs();
             if (cli.hasOption("app")) {
                 runApp(cli, args, options);
             } else if (cli.hasOption("server")) {
                 runServer(cli, args, options);
             } else if (cli.hasOption("script")) {
                 runScript(cli, args, options);
             } else if (args.length == 0 && !SystemUtils.isJavaAwtHeadless()) {
                 runApp(cli, args, options);
             } else {
                 runScript(cli, args, options);
             }
         } catch (ParseException e) {
             exit(options, e.getMessage());
         }
     }
 
     /**
      * Launches the interactive application mode.
      *
      * @param cli            the parsed command line
      * @param args           the additional arguments array
      * @param opts           the command-line options object
      */
     private static void runApp(CommandLine cli, String[] args, Options opts) {
         ServerApplication  app = createServer(cli, opts);
         ControlPanel       panel;
 
         if (args.length > 0) {
             exit(opts, "No arguments supported for app launch mode.");
         }
         if (SystemUtils.isJavaAwtHeadless()) {
             exit(opts, "Cannot launch app without graphical display.");
         }
         panel = new ControlPanel(app);
         panel.setVisible(true);
         panel.start();
     }
 
     /**
      * Launches the server mode.
      *
      * @param cli            the parsed command line
      * @param args           the additional arguments array
      * @param opts           the command-line options object
      */
     private static void runServer(CommandLine cli, String[] args, Options opts) {
         ServerApplication  app = createServer(cli, opts);
 
         if (args.length > 0) {
             exit(opts, "No arguments supported for server launch mode.");
         }
         try {
             app.start();
             writePortFile(app.appDir, app.port);
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "error starting server", e);
             exit(null, e.getMessage());
         }
         System.out.print("Server started -- http://localhost");
         if (app.port != 80) {
             System.out.print(":");
             System.out.print(app.port);
         }
         System.out.println("/");
         System.out.println("Press Ctrl-C to shutdown (or terminate process by other means).");
         System.out.println();
     }
 
     /**
      * Launches the script mode.
      *
      * @param cli            the parsed command line
      * @param args           the additional arguments array
      * @param opts           the command-line options object
      */
     private static void runScript(CommandLine cli, String[] args, Options opts) {
         ScriptApplication  app = new ScriptApplication();
         ServerApplication  server = createServer(cli, opts);
 
         app.appDir = server.appDir;
         app.localDir = server.localDir;
         app.user = cli.getOptionValue("user", System.getProperty("user.name"));
         try {
             app.delay = Integer.parseInt(cli.getOptionValue("delay", "0"));
         } catch (Exception e) {
             exit(opts, "Invalid delay number: " + cli.getOptionValue("delay"));
         }
         if (app.delay < 0 || app.delay > 3600) {
             exit(opts, "Invalid delay number, must be between 0 and 3600.");
         }
         app.trace = cli.hasOption("trace");
         try {
             if (cli.hasOption("stdin")) {
                 app.runStdin(args);
             } else if (cli.hasOption("file")) {
                 app.runFile(args, new File(cli.getOptionValue("file")));
             } else if (args.length <= 0) {
                 exit(opts, "No command specified for --script mode.");
             } else {
                 app.runSingle(args);
             }
         } catch (Exception e) {
             LOG.log(Level.SEVERE, "error running script", e);
             exit(null, e.getMessage());
         }
     }
 
     /**
      * Creates a server application instance from the command-line
      * arguments.
      *
      * @param cli            the parsed command line
      * @param opts           the command-line options object
      *
      * @return the server application created (not started)
      */
     private static ServerApplication createServer(CommandLine cli, Options opts) {
         ServerApplication  app = new ServerApplication();
 
         try {
             app.port = Integer.parseInt(cli.getOptionValue("port", "0"));
         } catch (Exception e) {
             exit(opts, "Invalid port number: " + cli.getOptionValue("port"));
         }
         if (app.port < 0 || app.port > 65535) {
             exit(opts, "Invalid port number, must be between 0 and 65535.");
         }
         app.appDir = locateAppDir();
         if (app.appDir == null) {
             exit(null, "Failed to locate application directory.");
         }
         app.appDir = app.appDir.getAbsoluteFile();
         app.localDir = app.appDir;
         if (cli.hasOption("local")) {
             app.localDir = new File(cli.getOptionValue("local"));
             if (!app.localDir.exists()) {
                 app.localDir.mkdirs();
             }
             if (!isDir(app.localDir, true)) {
                 exit(null, "Cannot write to local directory: " + app.localDir);
             }
             app.localDir = app.localDir.getAbsoluteFile();
             try {
                 setupLocalAppDir(app.appDir, app.localDir);
             } catch (IOException e) {
                 exit(null, "Failed to setup local directory: " + e.getMessage());
             }
         }
         return app;
     }
 
     /**
      * Exits the application with optional help and/or error messages.
      * This method WILL NOT RETURN.
      *
      * @param options        the command-line options, or null
      * @param error          the error message, or null
      */
     private static void exit(Options options, String error) {
         PrintWriter    out = new PrintWriter(System.err);
         HelpFormatter  fmt = new HelpFormatter();
 
         if (options != null) {
             out.println(USAGE);
             out.println();
             out.println("Options:");
             fmt.setOptionComparator(null);
             fmt.printOptions(out, 74, options, 2, 3);
             out.println();
         }
         if (error != null && error.length() > 0) {
             out.println("ERROR:");
             out.print("    ");
             out.println(error);
             out.println();
         }
         out.flush();
         System.exit(error == null ? 0 : 1);
     }
 
     /**
      * Attempts to locate the application directory based on the
      * current working directory and the class path.
      *
      * @return the application directory found, or
      *         null otherwise
      */
     private static File locateAppDir() {
         File[] dirs = { new File("."),
                         SystemUtils.getUserDir(),
                         ClassLoaderUtil.getLocation(ServerApplication.class) };
 
         for (int i = 0; i < dirs.length; i++) {
             File file = dirs[i];
             for (int j = 0; file != null && j < 4; j++) {
                 if (isAppDir(file)) {
                     return file;
                 }
                 file = file.getParentFile();
             }
         }
         return null;
     }
 
     /**
      * Sets up the local application directory if it is different
      * from the built-in application directory.
      *
      * @param appDir         the built-in application directory
      * @param localDir       the local application directory
      *
      * @throws IOException if the local directory couldn't be created
      */
     private static void setupLocalAppDir(File appDir, File localDir)
     throws IOException {
 
         if (!appDir.equals(localDir)) {
             appDir = new File(appDir, "plugin/local");
             localDir = new File(localDir, "plugin/local");
             if (!localDir.exists()) {
                 FileUtil.copy(appDir, localDir);
             }
         }
     }
 
     /**
      * Checks if the specified file is the application directory.
      *
      * @param file           the file to check
      *
      * @return true if the file matches, or
      *         false otherwise
      */
     private static boolean isAppDir(File file) {
         return file != null &&
                isDir(file, true) &&
                isDir(new File(file, "plugin"), true) &&
               (new File(file, "doc.zip")).canRead();
     }
 
     /**
      * Checks if the specified file is a readable directory.
      *
      * @param file           the file to check
      * @param write          the write check flag
      *
      * @return true if the file is a directory, or
      *         false otherwise
      */
     private static boolean isDir(File file, boolean write) {
         return file != null &&
                file.isDirectory() &&
                file.canRead() &&
                (!write || file.canWrite());
     }
 
     /**
      * Creates a file containing the current server port number.
      *
      * @param baseDir        the base directory
      * @param port           the port number
      */
     private static void writePortFile(File baseDir, int port) {
         File         dir = new File(baseDir, "var");
         File         file;
         PrintWriter  os;
 
         dir.mkdir();
         file = new File(dir, "server.port");
         try {
             os = new PrintWriter(new FileWriter(file, false));
             os.println(port);
             os.close();
             file.deleteOnExit();
         } catch (IOException ignore) {
             // Ignore errors writing port file
         }
     }
 
     /**
      * Returns the build information for the application.
      *
      * @return the build information
      */
     public static Properties buildInfo() {
         Properties info = new Properties();
         InputStream is = Main.class.getResourceAsStream("build.properties");
         try {
             info.load(is);
         } catch (IOException ignore) {
             // Ignore exception on loading properties
         } finally {
             try {
                 is.close();
             } catch (Exception ignore) {
                 // Ignore exception on closing file
             }
         }
         return info;
     }
 }
