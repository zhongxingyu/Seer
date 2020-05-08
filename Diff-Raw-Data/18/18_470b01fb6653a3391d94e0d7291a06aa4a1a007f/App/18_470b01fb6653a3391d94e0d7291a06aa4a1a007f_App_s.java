 /*
  *  LICENSE
  *
  * "THE BEER-WARE LICENSE" (Revision 43):
  * "Sven Strittmatter" <weltraumschaf@googlemail.com> wrote this file.
  * As long as you retain this notice you can do whatever you want with
  * this stuff. If we meet some day, and you think this stuff is worth it,
  * you can buy me a non alcohol-free beer in return.
  *
  * Copyright (C) 2012 "Sven Strittmatter" <weltraumschaf@googlemail.com>
  */
 package de.weltraumschaf.juberblog;
 
 import com.beust.jcommander.JCommander;
 import de.weltraumschaf.commons.ApplicationException;
 import de.weltraumschaf.commons.IOStreams;
 import de.weltraumschaf.commons.InvokableAdapter;
 import de.weltraumschaf.commons.Version;
 import de.weltraumschaf.juberblog.cmd.SubCommand;
 import de.weltraumschaf.juberblog.cmd.SubCommands;
 import de.weltraumschaf.juberblog.opt.CreateOptions;
 import de.weltraumschaf.juberblog.opt.InstallOptions;
 import de.weltraumschaf.juberblog.opt.Options;
 import de.weltraumschaf.juberblog.opt.PublishOptions;
 import java.io.UnsupportedEncodingException;
 import org.apache.log4j.Logger;
 
 /**
  * Main class.
  *
  * @author Sven Strittmatter <weltraumschaf@googlemail.com>
  */
 public class App extends InvokableAdapter {
 
     /**
      * Log facility.
      */
     private static final Logger LOG = Logger.getLogger(App.class);
     /**
      * Version information.
      */
     private final Version version;
 
     /**
      * Dedicated constructor.
      *
      * @param args must not be {@code null}
      */
     public App(final String[] args) {
         super(args);
         version = new Version("/de/weltraumschaf/juberblog/version.properties");
     }
 
     /**
      * Main entry point of VM.
      *
      * @param args cli arguments from VM
      */
     public static void main(final String[] args) {
         final App invokable = new App(args);
 
         try {
             InvokableAdapter.main(invokable, IOStreams.newDefault(), invokable.isDebug());
         } catch (UnsupportedEncodingException ex) {
             LOG.fatal("Can't create IO streams!", ex);
             invokable.exit(-1);
         }
     }
 
     private boolean isDebug() {
         final String debug = System.getenv(Constants.ENVIRONMENT_VARIABLE_DEBUG.toString());
 
         if (null == debug || debug.isEmpty()) {
             return false;
         }
 
         if ("true".equalsIgnoreCase(debug.trim())) {
             return true;
         }
 
         return false;
     }
 
     @Override
     public void execute() throws Exception {
         version.load();
         registerShutdownHook(new Runnable() {
             @Override
             public void run() {
             }
         });
 
 
         final Arguments args = new Arguments(getArgs());
 
         if (args.getFirstArgument().isEmpty()) {
             final StringBuilder errorMesage = new StringBuilder("No sub comamnd given!");
             errorMesage.append(Constants.DEFAULT_NEW_LINE)
                     .append("Usage: ").append(Constants.COMMAND_NAME).append(' ');
             boolean first = true;
 
             for (final SubCommands cmd : SubCommands.values()) {
                 if (!first) {
                     errorMesage.append('|');
                 }
 
                 errorMesage.append(cmd.getSubCommandName());
                 first = false;
             }
 
             throw new ApplicationException(ExitCodeImpl.TOO_FEW_ARGUMENTS, errorMesage.toString(), null);
         }
 
         final SubCommands subCommandName = SubCommands.forSubCommandName(args.getFirstArgument());
         final SubCommand cmd;
 
         try {
             cmd = SubCommands.create(subCommandName, getIoStreams());
         } catch (final IllegalArgumentException ex) {
             throw new ApplicationException(
                     ExitCodeImpl.UNKNOWN_COMMAND,
                     ex.getMessage(),
                     ex);
         }
 
         final JCommander optionsParser = new JCommander();
         optionsParser.setProgramName(Constants.COMMAND_NAME.toString());
         final Options opts;
 
         switch (subCommandName) {
             case CREATE:
                 opts = new CreateOptions();
                 optionsParser.addObject(opts);
                 break;
             case INSTALL:
                 opts = new InstallOptions();
                optionsParser.addObject(new InstallOptions());
                 break;
             case PUBLISH:
                 opts = new PublishOptions();
                optionsParser.addObject(new PublishOptions());
                 break;
             default:
                 opts = null;
         }
 
         optionsParser.parse(args.getTailArguments());
         cmd.setOptions(opts);
 
        if (opts.isHelp()) {
             final StringBuilder errorMessage = new StringBuilder();
             optionsParser.usage(errorMessage);
             throw new ApplicationException(
                     ExitCodeImpl.OK,
                     errorMessage.toString(),
                     null);
         }
 
         cmd.execute();
     }
 
 }
