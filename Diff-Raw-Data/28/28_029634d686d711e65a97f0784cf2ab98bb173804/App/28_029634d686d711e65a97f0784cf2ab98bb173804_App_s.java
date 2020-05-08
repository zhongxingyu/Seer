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
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.Validate;
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
         version = new Version(Constants.PACKAGE_BASE.toString() + "/version.properties");
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
 
     /**
      * Determine if debug is enabled by environment variable {@link Constants#ENVIRONMENT_VARIABLE_DEBUG}.
      *
      * @return by default {@code false} if environment is not present or false
      */
     boolean isDebug() {
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
 
         final Arguments args = validateArguments();
         final SubCommands subCommandName = SubCommands.forSubCommandName(args.getFirstArgument());
         final SubCommand cmd = createSubcommand(subCommandName);
         parseOptions(subCommandName, args, cmd);
         cmd.execute();
     }
 
     /**
      * Validates the command line arguments and creates the argument object.
      *
      * @return never {@code null}
      * @throws ApplicationException if too few arguments given
      */
     Arguments validateArguments() throws ApplicationException {
         final Arguments args = new Arguments(getArgs());
         final String firstArgument = args.getFirstArgument().trim();
 
        if ("-h".equals(firstArgument) || firstArgument.isEmpty()) {
             final StringBuilder errorMesage = new StringBuilder("No sub comamnd given!");
             errorMesage.append(Constants.DEFAULT_NEW_LINE)
                    .append("Usage: ").append(Constants.COMMAND_NAME).append(' ');
            errorMesage.append(StringUtils.join(SubCommands.implemented(), "|"));
             throw new ApplicationException(ExitCodeImpl.TOO_FEW_ARGUMENTS, errorMesage.toString(), null);
         }
 
         return args;
     }
 
     /**
      * Creates sub command object.
      *
      * @param subCommandType must not be {@code null}
      * @return never {@code null}
      * @throws ApplicationException if command is unknown
      */
     SubCommand createSubcommand(final SubCommands subCommandType) throws ApplicationException {
         Validate.notNull(subCommandType, "Sub command type must not be null!");
 
         try {
             return SubCommands.create(subCommandType, getIoStreams());
         } catch (final IllegalArgumentException ex) {
             throw new ApplicationException(
                     ExitCodeImpl.UNKNOWN_COMMAND,
                     ex.getMessage(),
                     ex);
         }
     }
 
     /**
      * Parses the command line options and set them to the command.
      *
      * Also checks if help is wanted.
      *
      * @param type must not be {@code null}
      * @param args must not be {@code null}
      * @param cmd must not be {@code null}
      * @throws ApplicationException if help is wanted
      */
     void parseOptions(final SubCommands type, final Arguments args, final SubCommand cmd)
        throws ApplicationException {
         Validate.notNull(type, "Type must not be null!");
         Validate.notNull(args, "Arguments must not be null!");
         Validate.notNull(cmd, "Sub command must not be null!");
         final JCommander optionsParser = new JCommander();
         optionsParser.setProgramName(Constants.COMMAND_NAME.toString());
         final Options opts;
 
         switch (type) {
             case CREATE:
                 opts = new CreateOptions();
                 optionsParser.addObject(opts);
                 break;
             case INSTALL:
                 opts = new InstallOptions();
                 optionsParser.addObject(opts);
                 break;
             case PUBLISH:
                 opts = new PublishOptions();
                 optionsParser.addObject(opts);
                 break;
             default:
                 LOG.warn(String.format("Unsupported sub command %s!", type));
                 opts = null;
         }
 
         optionsParser.parse(args.getTailArguments());
         cmd.setOptions(opts);
 
         if (null != opts && opts.isHelp()) {
             final StringBuilder errorMessage = new StringBuilder();
             optionsParser.usage(errorMessage);
             throw new ApplicationException(
                     ExitCodeImpl.OK,
                     errorMessage.toString(),
                     null);
         }
     }
 
 }
