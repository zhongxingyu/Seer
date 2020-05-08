 /*******************************************************************************
  * Copyright (c) 2004, 2011 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.app.autagent;
 
 import java.io.BufferedReader;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.PrintStream;
 import java.net.ConnectException;
 import java.net.Socket;
 import java.net.UnknownHostException;
 
 import org.apache.commons.cli.CommandLine;
 import org.apache.commons.cli.CommandLineParser;
 import org.apache.commons.cli.HelpFormatter;
 import org.apache.commons.cli.Option;
 import org.apache.commons.cli.OptionBuilder;
 import org.apache.commons.cli.OptionGroup;
 import org.apache.commons.cli.Options;
 import org.apache.commons.cli.ParseException;
 import org.apache.commons.cli.PosixParser;
 import org.eclipse.equinox.app.IApplication;
 import org.eclipse.equinox.app.IApplicationContext;
 import org.eclipse.jubula.app.autagent.i18n.Messages;
 import org.eclipse.jubula.autagent.AutStarter;
 import org.eclipse.jubula.autagent.AutStarter.Verbosity;
 import org.eclipse.jubula.autagent.agent.AutAgent;
 import org.eclipse.jubula.autagent.desktop.DesktopIntegration;
 import org.eclipse.jubula.communication.connection.ConnectionState;
 import org.eclipse.jubula.tools.constants.EnvConstants;
 import org.eclipse.jubula.tools.exception.JBVersionException;
 import org.eclipse.jubula.tools.utils.EnvironmentUtils;
 import org.eclipse.osgi.util.NLS;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * 
  * @author BREDEX GmbH
  * @created Jun 21, 2011
  */
 public class AutAgentApplication implements IApplication {
 
     /**
      * constant hostname
      */
     private static final String HOSTNAME = "hostname"; //$NON-NLS-1$
 
     /**
      * commandline constant for port
      */
     private static final String COMMANDLINE_PORT = "port"; //$NON-NLS-1$
 
     /**
      * constant for autagent launcher
      */
     private static final String AUTAGENT_LAUNCHER = "autagent"; //$NON-NLS-1$
 
     /**
     * the constant for the default hostname
     */
     private static final String DEFAULT_HOSTNAME_LOCALHOST = "localhost"; //$NON-NLS-1$
 
     /** the logger */
     private static final Logger LOG = 
         LoggerFactory.getLogger(AutAgentApplication.class);
 
     /** constant for timeout when sending command to shutdown AUT Agent */
     private static final int TIMEOUT_SEND_STOP_CMD = 10000;
 
     /**
      * <code>COMMANDLINE_OPTION_STOP</code>
      */
     private static final String COMMANDLINE_OPTION_STOP = "stop"; //$NON-NLS-1$
 
     /**
      * command line argument: port number
      */
     private static final String COMMANDLINE_OPTION_PORT = "p"; //$NON-NLS-1$
 
     /**
      * command line argument: show help
      */
     private static final String COMMANDLINE_OPTION_HELP = "h"; //$NON-NLS-1$
 
     /**
      * command line argument: enable "lenient" mode
      */
     private static final String COMMANDLINE_OPTION_LENIENT = "l"; //$NON-NLS-1$
 
     /**
      * command line argument: verbose output
      */
     private static final String COMMANDLINE_OPTION_VERBOSE = "v"; //$NON-NLS-1$
 
     /**
      * command line argument: quiet output
      */
     private static final String COMMANDLINE_OPTION_QUIET = "q"; //$NON-NLS-1$
     
     /**
      * command line argument: start
      */
     private static final String COMMANDLINE_OPTION_START = "start"; //$NON-NLS-1$
 
     /** exit code in case of invalid options */
     private static final int EXIT_INVALID_OPTIONS = -1;
 
     /** exit code in case of option -h(elp) */
     private static final int EXIT_HELP_OPTION = 0;
 
     /** exit code in case of a security exception */
     private static final int EXIT_SECURITY_VIOLATION = 1;
 
     /** exit code in case of an io exception */
     private static final int EXIT_IO_EXCEPTION = 2;
 
     /** exit code in case of a version error between Client and AutStarter */
     private static final int EXIT_CLIENT_SERVER_VERSION_ERROR = 4;
 
     /**
      * 
      * {@inheritDoc}
      */
     public Object start(IApplicationContext context) throws Exception {
         String[] args = (String[])context.getArguments().get(
                 IApplicationContext.APPLICATION_ARGS);
         if (args == null) {
             args = new String[0];
         }
 
         
         // create the single instance here
         final AutStarter server = AutStarter.getInstance();
 
         CommandLineParser parser = new PosixParser();
         try {
             CommandLine cmd = parser.parse(createOptions(), args);
             if (cmd.hasOption(COMMANDLINE_OPTION_HELP)) {
                 printHelp();
                 return EXIT_HELP_OPTION;
             }
 
             int port = getPortNumber(cmd);
             
             if (cmd.hasOption(COMMANDLINE_OPTION_STOP)) {
                 String hostname = DEFAULT_HOSTNAME_LOCALHOST;
                 if (cmd.getOptionValue(COMMANDLINE_OPTION_STOP) != null) {
                     hostname = cmd.getOptionValue(COMMANDLINE_OPTION_STOP);
                 }
                 stopAutAgent(hostname, port);
             } else {
                 boolean killDuplicateAuts = 
                     !cmd.hasOption(COMMANDLINE_OPTION_LENIENT);
                 Verbosity verbosity = Verbosity.NORMAL;
                 if (cmd.hasOption(COMMANDLINE_OPTION_VERBOSE)) {
                     verbosity = Verbosity.VERBOSE;
                 } else if (cmd.hasOption(COMMANDLINE_OPTION_QUIET)) {
                     verbosity = Verbosity.QUIET;
                 }
 
                 DesktopIntegration di = 
                     new DesktopIntegration(server.getAgent());
                 di.setPort(port);
                 server.getAgent().addPropertyChangeListener(
                         AutAgent.PROP_NAME_AUTS, di);
 
                 server.start(port, killDuplicateAuts, verbosity, true);
             }
         } catch (ParseException pe) {
             String message = Messages.ParseExceptionInvalidOption;
             LOG.error(message, pe);
             printHelp();
             return EXIT_INVALID_OPTIONS;
         } catch (SecurityException se) {
             LOG.error(Messages.SecurityExceptionViolation, se);
             return EXIT_SECURITY_VIOLATION;
         } catch (IOException ioe) {
             String message = Messages.IOExceptionNotOpenSocket;
             LOG.error(message, ioe);
             return EXIT_IO_EXCEPTION;
         } catch (NumberFormatException nfe) {
             String message = Messages.NumberFormatExceptionInvalidValue;
             LOG.error(message, nfe);
             return EXIT_INVALID_OPTIONS;
         } catch (NullPointerException npe) {
             LOG.error(Messages.NullPointerExceptionNoCommandLine, npe);
             printHelp();
             return EXIT_INVALID_OPTIONS;
         } catch (JBVersionException ve) {
             LOG.error(ve.getMessage(), ve);
             return EXIT_CLIENT_SERVER_VERSION_ERROR;
         }
 
         return IApplication.EXIT_OK;
     }
 
     /**
      * 
      * {@inheritDoc}
      */
     public void stop() {
         // no-op
     }
 
     /**
      * method to create an options object, filled with all options
      *
      * @return the options
      */
     private static Options createOptions() {
         Options options = new Options();
 
         Option portOption = new Option(COMMANDLINE_OPTION_PORT, true,
                 Messages.CommandlineOptionPort);
         portOption.setArgName(COMMANDLINE_PORT);
         options.addOption(portOption);
 
         options.addOption(COMMANDLINE_OPTION_LENIENT, false,
                 Messages.CommandlineOptionLenient);
         options.addOption(COMMANDLINE_OPTION_HELP, false,
                 Messages.CommandlineOptionHelp);
 
         OptionGroup verbosityOptions = new OptionGroup();
         verbosityOptions.addOption(new Option(COMMANDLINE_OPTION_QUIET, false,
                 Messages.CommandlineOptionQuiet));
         verbosityOptions.addOption(new Option(COMMANDLINE_OPTION_VERBOSE,
                 false, Messages.CommandlineOptionVerbose));
         options.addOptionGroup(verbosityOptions);
 
         OptionGroup startStopOptions = new OptionGroup();
         startStopOptions.addOption(new Option(COMMANDLINE_OPTION_START, false,
                 Messages.CommandlineOptionStart));
 
         OptionBuilder.hasOptionalArg();
         Option stopOption = OptionBuilder.create(COMMANDLINE_OPTION_STOP);
         stopOption.setDescription(NLS.bind(Messages.OptionStopDescription,
                 DEFAULT_HOSTNAME_LOCALHOST));
         stopOption.setArgName(HOSTNAME);
         startStopOptions.addOption(stopOption);
         options.addOptionGroup(startStopOptions);
 
         return options;
     }
 
     /**
      * prints a formatted help text
      */
     private void printHelp() {
         HelpFormatter formatter = new HelpFormatter();
         formatter.printHelp(AUTAGENT_LAUNCHER, createOptions(), true);
     }
 
     /**
      * @param br
      *            the buffered reader which is used to determine whether the
      *            agent has shutdown itself
      */
     private void waitForAgentToTerminate(BufferedReader br) {
         // keep process and socket alive till agent has read the shutdown command
         boolean socketAlive = true;
         while (socketAlive) {
             try {
                 if (br.readLine() == null) {
                     socketAlive = false;
                 }
             } catch (IOException e) {
                 // ok here --> autagent has shut down itself
                 socketAlive = false;
             }
         }
     }
 
     /**
      * Retrieves and returns the value of the "port number" argument from the
      * given command line. If the argument is incorrectly formatted, an 
      * exception will be thrown. If the argument is not present, An attempt 
      * will be made to read the port from an environment variable. If the
      * environment variable is not present or incorrectly formatted, then a
      * default value is returned.
      * 
      * @param cmd The command line from which to retrieve the port number.
      * @return the port number
      */
     private int getPortNumber(CommandLine cmd) {
         int port = EnvConstants.AUT_AGENT_DEFAULT_PORT;
         if (cmd.hasOption(COMMANDLINE_OPTION_PORT)) {
             port = Integer.valueOf(cmd.getOptionValue(COMMANDLINE_OPTION_PORT))
                 .intValue();
         } else {
             int envPort = EnvironmentUtils.getAUTAgentEnvironmentPortNo();
             if (envPort > 0) {
                 port = envPort;
             }
             LOG.info(NLS.bind(Messages.InfoDefaultPort, port));
         }
         return port;
     }
 
     /**
      * Issues a "stop" command to the AUT Agent running on the given host
      * and port.
      * 
      * @param hostname The hostname to which to send the command. 
      * @param port The port on which to send the command.
      * @throws UnknownHostException
      * @throws IOException
      * @throws JBVersionException
      */
     private void stopAutAgent(String hostname, int port) 
         throws UnknownHostException, IOException, JBVersionException {
        
        try {
            Socket commandSocket = new Socket(hostname, port);
             InputStream inputStream = commandSocket.getInputStream();
            BufferedReader br = new BufferedReader(
                    new InputStreamReader(inputStream));
            ConnectionState.respondToTypeRequest(TIMEOUT_SEND_STOP_CMD,
                    br, inputStream, new PrintStream(commandSocket
                            .getOutputStream()),
                    ConnectionState.CLIENT_TYPE_COMMAND_SHUTDOWN);
             waitForAgentToTerminate(br);
         } catch (ConnectException ce) {
             System.out.println(NLS.bind(Messages.AUTAgentNotFound, hostname,
                    port));
         }
     }
 }
